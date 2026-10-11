# internalaccesslog 도메인

기준 경로: `src/main/java/com/fream/back/domain/internalaccesslog/` · 파일 21개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/internal/access-log/commands/create` | `InternalUserAccessLogCommandController#createAccessLog` | `this#enrichAccessLogWithRequestData`, `InternalUserAccessLogCommandService#createAccessLog` |
| GET | `/internal/access-log/queries/today` | `InternalUserAccessLogQueryController#getTodayAccessCount` | `InternalUserAccessLogQueryService#getTodayUniqueVisitorCount` |
| GET | `/internal/access-log/queries/week` | `InternalUserAccessLogQueryController#getWeekAccessCount` | `InternalUserAccessLogQueryService#getRecent7DaysAccessCount` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `InternalAccessLogAopConfiguration`
  - `logAopStatistics` → InternalAccessLogExceptionAspect#getExceptionStats, InternalAccessLogMethodLoggingAspect#getLoggingStats, InternalAccessLogPerformanceAspect#getPerformanceStats, this#evaluateAopHealth
  - `evaluateAopHealth` → this#logImprovementSuggestions
  - `generateFinalAopReport` → InternalAccessLogExceptionAspect#getExceptionStats, InternalAccessLogMethodLoggingAspect#getLoggingStats, InternalAccessLogPerformanceAspect#getPerformanceStats
- `InternalAccessLogExceptionAspect`
  - `handleAccessLogException` → this#executeWithRetry, this#executeWithExceptionHandling
  - `executeWithRetry` → this#calculateBackoffTime, this#convertAndLogException
  - `executeWithExceptionHandling` → this#logException, this#convertAndLogException
  - `convertAndLogException` → this#convertToAccessLogException, this#logException
  - `convertToAccessLogException` → this#createSaveException, this#createQueryException, this#createKafkaException, this#createGeoIPException, this#createValidationException, this#createGeneralException
- `InternalAccessLogMethodLoggingAspect`
  - `logMethodEntry` → this#getThreadMethodKey, this#buildEntryLogMessage, this#logByLevel
  - `logMethodExit` → this#getThreadMethodKey, this#buildExitLogMessage, this#logByLevel, this#logMethodStatistics
  - `logMethodException` → this#getThreadMethodKey
  - `buildEntryLogMessage` → this#formatParameters
  - `buildExitLogMessage` → this#formatReturnValue
  - `formatParameters` → this#maskSensitiveData
  - `formatReturnValue` → this#maskSensitiveData
- `InternalAccessLogPerformanceAspect`
  - `monitorPerformance` → this#measureCurrentMemoryUsage, this#analyzeAndLogPerformance
  - `analyzeAndLogPerformance` → this#updateExecutionTimeStatistics, this#calculatePerformanceGrade, this#logPerformanceResults, this#collectPerformanceMetrics
  - `logPerformanceResults` → this#formatBytes, this#logSlowQueryDetails
- `InternalUserAccessLogCommandService`
  - `createAccessLog` → this#validateLogDto, InternalUserAccessLogProducer#sendAccessLog, this#enrichWithGeoData, this#mapToEntity, InternalUserAccessLogRepository#save~
  - `enrichWithGeoData` → InternalGeoIPService#getLocation
- `InternalUserAccessLogConsumer`
  - `consume` → InternalGeoIPService#getLocation, this#buildUserAccessLog, InternalUserAccessLogRepository#save~
- `InternalUserAccessLogConsumer_backup`
  - `consume` → InternalGeoIPService#getLocation, this#buildUserAccessLog, InternalUserAccessLogRepository#save~
- `InternalUserAccessLogProducer`
  - `sendAccessLog` → kafka:user-access-log-topic
- `InternalUserAccessLogProducer_backup`
  - `sendAccessLog` → kafka:user-access-log-topic
- `InternalUserAccessLogQueryService`
  - `getTodayAccessCount` → InternalUserAccessLogRepository#countTodayAccesses
  - `getTodayUniqueVisitorCount` → InternalUserAccessLogRepository#countTodayUniqueVisitors
  - `getRecent7DaysAccessCount` → InternalUserAccessLogRepository#findRecent7DaysAccessCount
- `InternalUserAccessLogQueryService_backup`
  - `getTodayAccessCount` → InternalUserAccessLogRepository#countTodayAccesses
  - `getTodayUniqueVisitorCount` → InternalUserAccessLogRepository#countTodayUniqueVisitors
  - `getRecent7DaysAccessCount` → InternalUserAccessLogRepository#findRecent7DaysAccessCount

## 3. 클래스 목록 (역할별)

### controller (2)

- `InternalUserAccessLogCommandController` controller/command/InternalUserAccessLogCommandController.java · public: createAccessLog · 주입: InternalUserAccessLogCommandService
- `InternalUserAccessLogQueryController` controller/query/InternalUserAccessLogQueryController.java · public: getTodayAccessCount, getWeekAccessCount · 주입: InternalUserAccessLogQueryService

### service-command (1)

- `InternalUserAccessLogCommandService` service/command/InternalUserAccessLogCommandService.java · public: createAccessLog · 주입: InternalUserAccessLogRepository, InternalGeoIPService, InternalUserAccessLogProducer

### service-query (2)

- `InternalUserAccessLogQueryService` service/query/InternalUserAccessLogQueryService.java · public: getTodayAccessCount, getTodayUniqueVisitorCount, getRecent7DaysAccessCount · 주입: InternalUserAccessLogRepository
- `InternalUserAccessLogQueryService_backup` service/query/InternalUserAccessLogQueryService_backup.java · public: getTodayAccessCount, getTodayUniqueVisitorCount, getRecent7DaysAccessCount · 주입: InternalUserAccessLogRepository

### service (2)

- `InternalGeoIPService` service/geo/InternalGeoIPService.java · public: getLocation
- `InternalGeoIPService_backup` service/geo/InternalGeoIPService_backup.java · public: getLocation

### repository (1)

- `InternalUserAccessLogRepository` (interface) repository/InternalUserAccessLogRepository.java · extends/implements JpaRepository, InternalUserAccessLogRepositoryCustom · public: existsByEmailOrIpAddress, findByIsAnonymous

### custom (2)

- `InternalUserAccessLogRepositoryCustom` (interface) repository/InternalUserAccessLogRepositoryCustom.java · public: countTodayAccesses, findRecent7DaysAccessCount, countTodayUniqueVisitors
- `InternalUserAccessLogRepositoryImpl` repository/InternalUserAccessLogRepositoryImpl.java · extends/implements InternalUserAccessLogRepositoryCustom · public: countTodayAccesses, countTodayUniqueVisitors, findRecent7DaysAccessCount · 주입: JPAQueryFactory

### event (4)

- `InternalUserAccessLogConsumer` service/kafka/InternalUserAccessLogConsumer.java · public: consume · 주입: InternalUserAccessLogRepository, InternalGeoIPService
- `InternalUserAccessLogConsumer_backup` service/kafka/InternalUserAccessLogConsumer_backup.java · public: consume · 주입: InternalUserAccessLogRepository, InternalGeoIPService
- `InternalUserAccessLogProducer` service/kafka/InternalUserAccessLogProducer.java · public: sendAccessLog · 주입: KafkaTemplate
- `InternalUserAccessLogProducer_backup` service/kafka/InternalUserAccessLogProducer_backup.java · public: sendAccessLog · 주입: KafkaTemplate

### config (1)

- `InternalAccessLogAopConfiguration` config/InternalAccessLogAopConfiguration.java · public: initializeAopConfiguration, accessLogCacheManager, logAopStatistics, generateFinalAopReport, validateAopConfiguration · 주입: InternalAccessLogExceptionAspect, InternalAccessLogMethodLoggingAspect, InternalAccessLogPerformanceAspect

### aop (3)

- `InternalAccessLogExceptionAspect` aop/aspect/InternalAccessLogExceptionAspect.java · public: handleAccessLogException, getExceptionStats
- `InternalAccessLogMethodLoggingAspect` aop/aspect/InternalAccessLogMethodLoggingAspect.java · public: logMethodEntry, logMethodExit, logMethodException, getLoggingStats
- `InternalAccessLogPerformanceAspect` aop/aspect/InternalAccessLogPerformanceAspect.java · public: monitorPerformance, getPerformanceStats, getMethodStats

### other (3)

- `InternalAccessLogExceptionHandler` (@interface) aop/annotation/InternalAccessLogExceptionHandler.java
- `InternalAccessLogMethodLogger` (@interface) aop/annotation/InternalAccessLogMethodLogger.java
- `InternalAccessLogPerformanceMonitor` (@interface) aop/annotation/InternalAccessLogPerformanceMonitor.java

### 다른 도메인 참조 (import·주입 기준)

- **accessLog**: `UserAccessLog` ← InternalUserAccessLogCommandService, InternalUserAccessLogConsumer, InternalUserAccessLogConsumer_backup, InternalUserAccessLogRepository

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

- 발행 kafka `user-access-log-topic`: `InternalUserAccessLogProducer#sendAccessLog` → `UserAccessLogConsumer#consume` [accessLog], `UserAccessLogConsumer_backup#consume` [accessLog], `AdminUserAccessLogConsumer#consume` [adminaccesslog], `AdminUserAccessLogConsumer_backup#consume` [adminaccesslog], `InternalUserAccessLogConsumer#consume` [internalaccesslog], `InternalUserAccessLogConsumer_backup#consume` [internalaccesslog], `LegacyUserAccessLogConsumer#consume` [legacyaccesslog], `LegacyUserAccessLogConsumer_backup#consume` [legacyaccesslog], `PartnerUserAccessLogConsumer#consume` [partneraccesslog], `PartnerUserAccessLogConsumer_backup#consume` [partneraccesslog], `SettlementUserAccessLogConsumer#consume` [settlementaccesslog], `SettlementUserAccessLogConsumer_backup#consume` [settlementaccesslog]
- 발행 kafka `user-access-log-topic`: `InternalUserAccessLogProducer_backup#sendAccessLog` → `UserAccessLogConsumer#consume` [accessLog], `UserAccessLogConsumer_backup#consume` [accessLog], `AdminUserAccessLogConsumer#consume` [adminaccesslog], `AdminUserAccessLogConsumer_backup#consume` [adminaccesslog], `InternalUserAccessLogConsumer#consume` [internalaccesslog], `InternalUserAccessLogConsumer_backup#consume` [internalaccesslog], `LegacyUserAccessLogConsumer#consume` [legacyaccesslog], `LegacyUserAccessLogConsumer_backup#consume` [legacyaccesslog], `PartnerUserAccessLogConsumer#consume` [partneraccesslog], `PartnerUserAccessLogConsumer_backup#consume` [partneraccesslog], `SettlementUserAccessLogConsumer#consume` [settlementaccesslog], `SettlementUserAccessLogConsumer_backup#consume` [settlementaccesslog]
- 구독 kafka `user-access-log-topic`: `InternalUserAccessLogConsumer#consume` (groupId=user-access-log-group containerFactory=userAccessLogKafkaListenerContainerFactory) ← `UserAccessLogProducer#sendAccessLog` [accessLog], `UserAccessLogProducer_backup#sendAccessLog` [accessLog], `AdminUserAccessLogProducer#sendAccessLog` [adminaccesslog], `AdminUserAccessLogProducer_backup#sendAccessLog` [adminaccesslog], `InternalUserAccessLogProducer#sendAccessLog` [internalaccesslog], `InternalUserAccessLogProducer_backup#sendAccessLog` [internalaccesslog], `LegacyUserAccessLogProducer#sendAccessLog` [legacyaccesslog], `LegacyUserAccessLogProducer_backup#sendAccessLog` [legacyaccesslog], `PartnerUserAccessLogProducer#sendAccessLog` [partneraccesslog], `PartnerUserAccessLogProducer_backup#sendAccessLog` [partneraccesslog], `SettlementUserAccessLogProducer#sendAccessLog` [settlementaccesslog], `SettlementUserAccessLogProducer_backup#sendAccessLog` [settlementaccesslog]
- 구독 kafka `user-access-log-topic`: `InternalUserAccessLogConsumer_backup#consume` (groupId=user-access-log-group containerFactory=userAccessLogKafkaListenerContainerFactory) ← `UserAccessLogProducer#sendAccessLog` [accessLog], `UserAccessLogProducer_backup#sendAccessLog` [accessLog], `AdminUserAccessLogProducer#sendAccessLog` [adminaccesslog], `AdminUserAccessLogProducer_backup#sendAccessLog` [adminaccesslog], `InternalUserAccessLogProducer#sendAccessLog` [internalaccesslog], `InternalUserAccessLogProducer_backup#sendAccessLog` [internalaccesslog], `LegacyUserAccessLogProducer#sendAccessLog` [legacyaccesslog], `LegacyUserAccessLogProducer_backup#sendAccessLog` [legacyaccesslog], `PartnerUserAccessLogProducer#sendAccessLog` [partneraccesslog], `PartnerUserAccessLogProducer_backup#sendAccessLog` [partneraccesslog], `SettlementUserAccessLogProducer#sendAccessLog` [settlementaccesslog], `SettlementUserAccessLogProducer_backup#sendAccessLog` [settlementaccesslog]

## 6. AOP

- `InternalAccessLogExceptionAspect#handleAccessLogException` @Around("@annotation(exceptionHandler)") · 바인딩: `@InternalAccessLogExceptionHandler`
- `InternalAccessLogMethodLoggingAspect#logMethodEntry` @Before("@annotation(methodLogger)") · 바인딩: `@InternalAccessLogMethodLogger`
- `InternalAccessLogMethodLoggingAspect#logMethodExit` @AfterReturning(pointcut = "@annotation(methodLogger)", returning = "returnValue") · 바인딩: `@InternalAccessLogMethodLogger`
- `InternalAccessLogMethodLoggingAspect#logMethodException` @AfterThrowing(pointcut = "@annotation(methodLogger)", throwing = "exception") · 바인딩: `@InternalAccessLogMethodLogger`
- `InternalAccessLogPerformanceAspect#monitorPerformance` @Around("@annotation(monitor)") · 바인딩: `@InternalAccessLogPerformanceMonitor`

어노테이션 적용 지점(메서드에 붙은 곳):

- `@InternalAccessLogExceptionHandler`: `InternalUserAccessLogCommandService#createAccessLog`, `InternalUserAccessLogCommandService#validateLogDto`, `InternalUserAccessLogCommandService#enrichWithGeoData`, `InternalUserAccessLogCommandService#mapToEntity`, `InternalGeoIPService#getLocation`, `InternalUserAccessLogConsumer#consume`, `InternalUserAccessLogConsumer#buildUserAccessLog`, `InternalUserAccessLogProducer#sendAccessLog`, `InternalUserAccessLogQueryService#getTodayAccessCount`, `InternalUserAccessLogQueryService#getTodayUniqueVisitorCount`, `InternalUserAccessLogQueryService#getRecent7DaysAccessCount`
- `@InternalAccessLogMethodLogger`: `InternalUserAccessLogCommandService#createAccessLog`, `InternalUserAccessLogCommandService#validateLogDto`, `InternalUserAccessLogCommandService#enrichWithGeoData`, `InternalUserAccessLogCommandService#mapToEntity`, `InternalGeoIPService#getLocation`, `InternalUserAccessLogConsumer#consume`, `InternalUserAccessLogConsumer#buildUserAccessLog`, `InternalUserAccessLogProducer#sendAccessLog`, `InternalUserAccessLogQueryService#getTodayAccessCount`, `InternalUserAccessLogQueryService#getTodayUniqueVisitorCount`, `InternalUserAccessLogQueryService#getRecent7DaysAccessCount`
- `@InternalAccessLogPerformanceMonitor`: `InternalUserAccessLogCommandService#createAccessLog`, `InternalUserAccessLogCommandService#enrichWithGeoData`, `InternalGeoIPService#getLocation`, `InternalUserAccessLogConsumer#consume`, `InternalUserAccessLogProducer#sendAccessLog`, `InternalUserAccessLogQueryService#getTodayAccessCount`, `InternalUserAccessLogQueryService#getTodayUniqueVisitorCount`, `InternalUserAccessLogQueryService#getRecent7DaysAccessCount`

