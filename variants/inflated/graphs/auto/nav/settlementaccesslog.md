# settlementaccesslog 도메인

기준 경로: `src/main/java/com/fream/back/domain/settlementaccesslog/` · 파일 21개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/settlement/access-log/commands/create` | `SettlementUserAccessLogCommandController#createAccessLog` | `this#enrichAccessLogWithRequestData`, `SettlementUserAccessLogCommandService#createAccessLog` |
| GET | `/settlement/access-log/queries/today` | `SettlementUserAccessLogQueryController#getTodayAccessCount` | `SettlementUserAccessLogQueryService#getTodayUniqueVisitorCount` |
| GET | `/settlement/access-log/queries/week` | `SettlementUserAccessLogQueryController#getWeekAccessCount` | `SettlementUserAccessLogQueryService#getRecent7DaysAccessCount` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `SettlementAccessLogAopConfiguration`
  - `logAopStatistics` → SettlementAccessLogExceptionAspect#getExceptionStats, SettlementAccessLogMethodLoggingAspect#getLoggingStats, SettlementAccessLogPerformanceAspect#getPerformanceStats, this#evaluateAopHealth
  - `evaluateAopHealth` → this#logImprovementSuggestions
  - `generateFinalAopReport` → SettlementAccessLogExceptionAspect#getExceptionStats, SettlementAccessLogMethodLoggingAspect#getLoggingStats, SettlementAccessLogPerformanceAspect#getPerformanceStats
- `SettlementAccessLogExceptionAspect`
  - `handleAccessLogException` → this#executeWithRetry, this#executeWithExceptionHandling
  - `executeWithRetry` → this#calculateBackoffTime, this#convertAndLogException
  - `executeWithExceptionHandling` → this#logException, this#convertAndLogException
  - `convertAndLogException` → this#convertToAccessLogException, this#logException
  - `convertToAccessLogException` → this#createSaveException, this#createQueryException, this#createKafkaException, this#createGeoIPException, this#createValidationException, this#createGeneralException
- `SettlementAccessLogMethodLoggingAspect`
  - `logMethodEntry` → this#getThreadMethodKey, this#buildEntryLogMessage, this#logByLevel
  - `logMethodExit` → this#getThreadMethodKey, this#buildExitLogMessage, this#logByLevel, this#logMethodStatistics
  - `logMethodException` → this#getThreadMethodKey
  - `buildEntryLogMessage` → this#formatParameters
  - `buildExitLogMessage` → this#formatReturnValue
  - `formatParameters` → this#maskSensitiveData
  - `formatReturnValue` → this#maskSensitiveData
- `SettlementAccessLogPerformanceAspect`
  - `monitorPerformance` → this#measureCurrentMemoryUsage, this#analyzeAndLogPerformance
  - `analyzeAndLogPerformance` → this#updateExecutionTimeStatistics, this#calculatePerformanceGrade, this#logPerformanceResults, this#collectPerformanceMetrics
  - `logPerformanceResults` → this#formatBytes, this#logSlowQueryDetails
- `SettlementUserAccessLogCommandService`
  - `createAccessLog` → this#validateLogDto, SettlementUserAccessLogProducer#sendAccessLog, this#enrichWithGeoData, this#mapToEntity, SettlementUserAccessLogRepository#save~
  - `enrichWithGeoData` → SettlementGeoIPService#getLocation
- `SettlementUserAccessLogConsumer`
  - `consume` → SettlementGeoIPService#getLocation, this#buildUserAccessLog, SettlementUserAccessLogRepository#save~
- `SettlementUserAccessLogConsumer_backup`
  - `consume` → SettlementGeoIPService#getLocation, this#buildUserAccessLog, SettlementUserAccessLogRepository#save~
- `SettlementUserAccessLogProducer`
  - `sendAccessLog` → kafka:user-access-log-topic
- `SettlementUserAccessLogProducer_backup`
  - `sendAccessLog` → kafka:user-access-log-topic
- `SettlementUserAccessLogQueryService`
  - `getTodayAccessCount` → SettlementUserAccessLogRepository#countTodayAccesses
  - `getTodayUniqueVisitorCount` → SettlementUserAccessLogRepository#countTodayUniqueVisitors
  - `getRecent7DaysAccessCount` → SettlementUserAccessLogRepository#findRecent7DaysAccessCount
- `SettlementUserAccessLogQueryService_backup`
  - `getTodayAccessCount` → SettlementUserAccessLogRepository#countTodayAccesses
  - `getTodayUniqueVisitorCount` → SettlementUserAccessLogRepository#countTodayUniqueVisitors
  - `getRecent7DaysAccessCount` → SettlementUserAccessLogRepository#findRecent7DaysAccessCount

## 3. 클래스 목록 (역할별)

### controller (2)

- `SettlementUserAccessLogCommandController` controller/command/SettlementUserAccessLogCommandController.java · public: createAccessLog · 주입: SettlementUserAccessLogCommandService
- `SettlementUserAccessLogQueryController` controller/query/SettlementUserAccessLogQueryController.java · public: getTodayAccessCount, getWeekAccessCount · 주입: SettlementUserAccessLogQueryService

### service-command (1)

- `SettlementUserAccessLogCommandService` service/command/SettlementUserAccessLogCommandService.java · public: createAccessLog · 주입: SettlementUserAccessLogRepository, SettlementGeoIPService, SettlementUserAccessLogProducer

### service-query (2)

- `SettlementUserAccessLogQueryService` service/query/SettlementUserAccessLogQueryService.java · public: getTodayAccessCount, getTodayUniqueVisitorCount, getRecent7DaysAccessCount · 주입: SettlementUserAccessLogRepository
- `SettlementUserAccessLogQueryService_backup` service/query/SettlementUserAccessLogQueryService_backup.java · public: getTodayAccessCount, getTodayUniqueVisitorCount, getRecent7DaysAccessCount · 주입: SettlementUserAccessLogRepository

### service (2)

- `SettlementGeoIPService` service/geo/SettlementGeoIPService.java · public: getLocation
- `SettlementGeoIPService_backup` service/geo/SettlementGeoIPService_backup.java · public: getLocation

### repository (1)

- `SettlementUserAccessLogRepository` (interface) repository/SettlementUserAccessLogRepository.java · extends/implements JpaRepository, SettlementUserAccessLogRepositoryCustom · public: existsByEmailOrIpAddress, findByIsAnonymous

### custom (2)

- `SettlementUserAccessLogRepositoryCustom` (interface) repository/SettlementUserAccessLogRepositoryCustom.java · public: countTodayAccesses, findRecent7DaysAccessCount, countTodayUniqueVisitors
- `SettlementUserAccessLogRepositoryImpl` repository/SettlementUserAccessLogRepositoryImpl.java · extends/implements SettlementUserAccessLogRepositoryCustom · public: countTodayAccesses, countTodayUniqueVisitors, findRecent7DaysAccessCount · 주입: JPAQueryFactory

### event (4)

- `SettlementUserAccessLogConsumer` service/kafka/SettlementUserAccessLogConsumer.java · public: consume · 주입: SettlementUserAccessLogRepository, SettlementGeoIPService
- `SettlementUserAccessLogConsumer_backup` service/kafka/SettlementUserAccessLogConsumer_backup.java · public: consume · 주입: SettlementUserAccessLogRepository, SettlementGeoIPService
- `SettlementUserAccessLogProducer` service/kafka/SettlementUserAccessLogProducer.java · public: sendAccessLog · 주입: KafkaTemplate
- `SettlementUserAccessLogProducer_backup` service/kafka/SettlementUserAccessLogProducer_backup.java · public: sendAccessLog · 주입: KafkaTemplate

### config (1)

- `SettlementAccessLogAopConfiguration` config/SettlementAccessLogAopConfiguration.java · public: initializeAopConfiguration, accessLogCacheManager, logAopStatistics, generateFinalAopReport, validateAopConfiguration · 주입: SettlementAccessLogExceptionAspect, SettlementAccessLogMethodLoggingAspect, SettlementAccessLogPerformanceAspect

### aop (3)

- `SettlementAccessLogExceptionAspect` aop/aspect/SettlementAccessLogExceptionAspect.java · public: handleAccessLogException, getExceptionStats
- `SettlementAccessLogMethodLoggingAspect` aop/aspect/SettlementAccessLogMethodLoggingAspect.java · public: logMethodEntry, logMethodExit, logMethodException, getLoggingStats
- `SettlementAccessLogPerformanceAspect` aop/aspect/SettlementAccessLogPerformanceAspect.java · public: monitorPerformance, getPerformanceStats, getMethodStats

### other (3)

- `SettlementAccessLogExceptionHandler` (@interface) aop/annotation/SettlementAccessLogExceptionHandler.java
- `SettlementAccessLogMethodLogger` (@interface) aop/annotation/SettlementAccessLogMethodLogger.java
- `SettlementAccessLogPerformanceMonitor` (@interface) aop/annotation/SettlementAccessLogPerformanceMonitor.java

### 다른 도메인 참조 (import·주입 기준)

- **accessLog**: `UserAccessLog` ← SettlementUserAccessLogCommandService, SettlementUserAccessLogConsumer, SettlementUserAccessLogConsumer_backup, SettlementUserAccessLogRepository

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

- 발행 kafka `user-access-log-topic`: `SettlementUserAccessLogProducer#sendAccessLog` → `UserAccessLogConsumer#consume` [accessLog], `UserAccessLogConsumer_backup#consume` [accessLog], `AdminUserAccessLogConsumer#consume` [adminaccesslog], `AdminUserAccessLogConsumer_backup#consume` [adminaccesslog], `InternalUserAccessLogConsumer#consume` [internalaccesslog], `InternalUserAccessLogConsumer_backup#consume` [internalaccesslog], `LegacyUserAccessLogConsumer#consume` [legacyaccesslog], `LegacyUserAccessLogConsumer_backup#consume` [legacyaccesslog], `PartnerUserAccessLogConsumer#consume` [partneraccesslog], `PartnerUserAccessLogConsumer_backup#consume` [partneraccesslog], `SettlementUserAccessLogConsumer#consume` [settlementaccesslog], `SettlementUserAccessLogConsumer_backup#consume` [settlementaccesslog]
- 발행 kafka `user-access-log-topic`: `SettlementUserAccessLogProducer_backup#sendAccessLog` → `UserAccessLogConsumer#consume` [accessLog], `UserAccessLogConsumer_backup#consume` [accessLog], `AdminUserAccessLogConsumer#consume` [adminaccesslog], `AdminUserAccessLogConsumer_backup#consume` [adminaccesslog], `InternalUserAccessLogConsumer#consume` [internalaccesslog], `InternalUserAccessLogConsumer_backup#consume` [internalaccesslog], `LegacyUserAccessLogConsumer#consume` [legacyaccesslog], `LegacyUserAccessLogConsumer_backup#consume` [legacyaccesslog], `PartnerUserAccessLogConsumer#consume` [partneraccesslog], `PartnerUserAccessLogConsumer_backup#consume` [partneraccesslog], `SettlementUserAccessLogConsumer#consume` [settlementaccesslog], `SettlementUserAccessLogConsumer_backup#consume` [settlementaccesslog]
- 구독 kafka `user-access-log-topic`: `SettlementUserAccessLogConsumer#consume` (groupId=user-access-log-group containerFactory=userAccessLogKafkaListenerContainerFactory) ← `UserAccessLogProducer#sendAccessLog` [accessLog], `UserAccessLogProducer_backup#sendAccessLog` [accessLog], `AdminUserAccessLogProducer#sendAccessLog` [adminaccesslog], `AdminUserAccessLogProducer_backup#sendAccessLog` [adminaccesslog], `InternalUserAccessLogProducer#sendAccessLog` [internalaccesslog], `InternalUserAccessLogProducer_backup#sendAccessLog` [internalaccesslog], `LegacyUserAccessLogProducer#sendAccessLog` [legacyaccesslog], `LegacyUserAccessLogProducer_backup#sendAccessLog` [legacyaccesslog], `PartnerUserAccessLogProducer#sendAccessLog` [partneraccesslog], `PartnerUserAccessLogProducer_backup#sendAccessLog` [partneraccesslog], `SettlementUserAccessLogProducer#sendAccessLog` [settlementaccesslog], `SettlementUserAccessLogProducer_backup#sendAccessLog` [settlementaccesslog]
- 구독 kafka `user-access-log-topic`: `SettlementUserAccessLogConsumer_backup#consume` (groupId=user-access-log-group containerFactory=userAccessLogKafkaListenerContainerFactory) ← `UserAccessLogProducer#sendAccessLog` [accessLog], `UserAccessLogProducer_backup#sendAccessLog` [accessLog], `AdminUserAccessLogProducer#sendAccessLog` [adminaccesslog], `AdminUserAccessLogProducer_backup#sendAccessLog` [adminaccesslog], `InternalUserAccessLogProducer#sendAccessLog` [internalaccesslog], `InternalUserAccessLogProducer_backup#sendAccessLog` [internalaccesslog], `LegacyUserAccessLogProducer#sendAccessLog` [legacyaccesslog], `LegacyUserAccessLogProducer_backup#sendAccessLog` [legacyaccesslog], `PartnerUserAccessLogProducer#sendAccessLog` [partneraccesslog], `PartnerUserAccessLogProducer_backup#sendAccessLog` [partneraccesslog], `SettlementUserAccessLogProducer#sendAccessLog` [settlementaccesslog], `SettlementUserAccessLogProducer_backup#sendAccessLog` [settlementaccesslog]

## 6. AOP

- `SettlementAccessLogExceptionAspect#handleAccessLogException` @Around("@annotation(exceptionHandler)") · 바인딩: `@SettlementAccessLogExceptionHandler`
- `SettlementAccessLogMethodLoggingAspect#logMethodEntry` @Before("@annotation(methodLogger)") · 바인딩: `@SettlementAccessLogMethodLogger`
- `SettlementAccessLogMethodLoggingAspect#logMethodExit` @AfterReturning(pointcut = "@annotation(methodLogger)", returning = "returnValue") · 바인딩: `@SettlementAccessLogMethodLogger`
- `SettlementAccessLogMethodLoggingAspect#logMethodException` @AfterThrowing(pointcut = "@annotation(methodLogger)", throwing = "exception") · 바인딩: `@SettlementAccessLogMethodLogger`
- `SettlementAccessLogPerformanceAspect#monitorPerformance` @Around("@annotation(monitor)") · 바인딩: `@SettlementAccessLogPerformanceMonitor`

어노테이션 적용 지점(메서드에 붙은 곳):

- `@SettlementAccessLogExceptionHandler`: `SettlementUserAccessLogCommandService#createAccessLog`, `SettlementUserAccessLogCommandService#validateLogDto`, `SettlementUserAccessLogCommandService#enrichWithGeoData`, `SettlementUserAccessLogCommandService#mapToEntity`, `SettlementGeoIPService#getLocation`, `SettlementUserAccessLogConsumer#consume`, `SettlementUserAccessLogConsumer#buildUserAccessLog`, `SettlementUserAccessLogProducer#sendAccessLog`, `SettlementUserAccessLogQueryService#getTodayAccessCount`, `SettlementUserAccessLogQueryService#getTodayUniqueVisitorCount`, `SettlementUserAccessLogQueryService#getRecent7DaysAccessCount`
- `@SettlementAccessLogMethodLogger`: `SettlementUserAccessLogCommandService#createAccessLog`, `SettlementUserAccessLogCommandService#validateLogDto`, `SettlementUserAccessLogCommandService#enrichWithGeoData`, `SettlementUserAccessLogCommandService#mapToEntity`, `SettlementGeoIPService#getLocation`, `SettlementUserAccessLogConsumer#consume`, `SettlementUserAccessLogConsumer#buildUserAccessLog`, `SettlementUserAccessLogProducer#sendAccessLog`, `SettlementUserAccessLogQueryService#getTodayAccessCount`, `SettlementUserAccessLogQueryService#getTodayUniqueVisitorCount`, `SettlementUserAccessLogQueryService#getRecent7DaysAccessCount`
- `@SettlementAccessLogPerformanceMonitor`: `SettlementUserAccessLogCommandService#createAccessLog`, `SettlementUserAccessLogCommandService#enrichWithGeoData`, `SettlementGeoIPService#getLocation`, `SettlementUserAccessLogConsumer#consume`, `SettlementUserAccessLogProducer#sendAccessLog`, `SettlementUserAccessLogQueryService#getTodayAccessCount`, `SettlementUserAccessLogQueryService#getTodayUniqueVisitorCount`, `SettlementUserAccessLogQueryService#getRecent7DaysAccessCount`

