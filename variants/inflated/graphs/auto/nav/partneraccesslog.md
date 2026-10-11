# partneraccesslog 도메인

기준 경로: `src/main/java/com/fream/back/domain/partneraccesslog/` · 파일 21개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/partner/access-log/commands/create` | `PartnerUserAccessLogCommandController#createAccessLog` | `this#enrichAccessLogWithRequestData`, `PartnerUserAccessLogCommandService#createAccessLog` |
| GET | `/partner/access-log/queries/today` | `PartnerUserAccessLogQueryController#getTodayAccessCount` | `PartnerUserAccessLogQueryService#getTodayUniqueVisitorCount` |
| GET | `/partner/access-log/queries/week` | `PartnerUserAccessLogQueryController#getWeekAccessCount` | `PartnerUserAccessLogQueryService#getRecent7DaysAccessCount` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `PartnerAccessLogAopConfiguration`
  - `logAopStatistics` → PartnerAccessLogExceptionAspect#getExceptionStats, PartnerAccessLogMethodLoggingAspect#getLoggingStats, PartnerAccessLogPerformanceAspect#getPerformanceStats, this#evaluateAopHealth
  - `evaluateAopHealth` → this#logImprovementSuggestions
  - `generateFinalAopReport` → PartnerAccessLogExceptionAspect#getExceptionStats, PartnerAccessLogMethodLoggingAspect#getLoggingStats, PartnerAccessLogPerformanceAspect#getPerformanceStats
- `PartnerAccessLogExceptionAspect`
  - `handleAccessLogException` → this#executeWithRetry, this#executeWithExceptionHandling
  - `executeWithRetry` → this#calculateBackoffTime, this#convertAndLogException
  - `executeWithExceptionHandling` → this#logException, this#convertAndLogException
  - `convertAndLogException` → this#convertToAccessLogException, this#logException
  - `convertToAccessLogException` → this#createSaveException, this#createQueryException, this#createKafkaException, this#createGeoIPException, this#createValidationException, this#createGeneralException
- `PartnerAccessLogMethodLoggingAspect`
  - `logMethodEntry` → this#getThreadMethodKey, this#buildEntryLogMessage, this#logByLevel
  - `logMethodExit` → this#getThreadMethodKey, this#buildExitLogMessage, this#logByLevel, this#logMethodStatistics
  - `logMethodException` → this#getThreadMethodKey
  - `buildEntryLogMessage` → this#formatParameters
  - `buildExitLogMessage` → this#formatReturnValue
  - `formatParameters` → this#maskSensitiveData
  - `formatReturnValue` → this#maskSensitiveData
- `PartnerAccessLogPerformanceAspect`
  - `monitorPerformance` → this#measureCurrentMemoryUsage, this#analyzeAndLogPerformance
  - `analyzeAndLogPerformance` → this#updateExecutionTimeStatistics, this#calculatePerformanceGrade, this#logPerformanceResults, this#collectPerformanceMetrics
  - `logPerformanceResults` → this#formatBytes, this#logSlowQueryDetails
- `PartnerUserAccessLogCommandService`
  - `createAccessLog` → this#validateLogDto, PartnerUserAccessLogProducer#sendAccessLog, this#enrichWithGeoData, this#mapToEntity, PartnerUserAccessLogRepository#save~
  - `enrichWithGeoData` → PartnerGeoIPService#getLocation
- `PartnerUserAccessLogConsumer`
  - `consume` → PartnerGeoIPService#getLocation, this#buildUserAccessLog, PartnerUserAccessLogRepository#save~
- `PartnerUserAccessLogConsumer_backup`
  - `consume` → PartnerGeoIPService#getLocation, this#buildUserAccessLog, PartnerUserAccessLogRepository#save~
- `PartnerUserAccessLogProducer`
  - `sendAccessLog` → kafka:user-access-log-topic
- `PartnerUserAccessLogProducer_backup`
  - `sendAccessLog` → kafka:user-access-log-topic
- `PartnerUserAccessLogQueryService`
  - `getTodayAccessCount` → PartnerUserAccessLogRepository#countTodayAccesses
  - `getTodayUniqueVisitorCount` → PartnerUserAccessLogRepository#countTodayUniqueVisitors
  - `getRecent7DaysAccessCount` → PartnerUserAccessLogRepository#findRecent7DaysAccessCount
- `PartnerUserAccessLogQueryService_backup`
  - `getTodayAccessCount` → PartnerUserAccessLogRepository#countTodayAccesses
  - `getTodayUniqueVisitorCount` → PartnerUserAccessLogRepository#countTodayUniqueVisitors
  - `getRecent7DaysAccessCount` → PartnerUserAccessLogRepository#findRecent7DaysAccessCount

## 3. 클래스 목록 (역할별)

### controller (2)

- `PartnerUserAccessLogCommandController` controller/command/PartnerUserAccessLogCommandController.java · public: createAccessLog · 주입: PartnerUserAccessLogCommandService
- `PartnerUserAccessLogQueryController` controller/query/PartnerUserAccessLogQueryController.java · public: getTodayAccessCount, getWeekAccessCount · 주입: PartnerUserAccessLogQueryService

### service-command (1)

- `PartnerUserAccessLogCommandService` service/command/PartnerUserAccessLogCommandService.java · public: createAccessLog · 주입: PartnerUserAccessLogRepository, PartnerGeoIPService, PartnerUserAccessLogProducer

### service-query (2)

- `PartnerUserAccessLogQueryService` service/query/PartnerUserAccessLogQueryService.java · public: getTodayAccessCount, getTodayUniqueVisitorCount, getRecent7DaysAccessCount · 주입: PartnerUserAccessLogRepository
- `PartnerUserAccessLogQueryService_backup` service/query/PartnerUserAccessLogQueryService_backup.java · public: getTodayAccessCount, getTodayUniqueVisitorCount, getRecent7DaysAccessCount · 주입: PartnerUserAccessLogRepository

### service (2)

- `PartnerGeoIPService` service/geo/PartnerGeoIPService.java · public: getLocation
- `PartnerGeoIPService_backup` service/geo/PartnerGeoIPService_backup.java · public: getLocation

### repository (1)

- `PartnerUserAccessLogRepository` (interface) repository/PartnerUserAccessLogRepository.java · extends/implements JpaRepository, PartnerUserAccessLogRepositoryCustom · public: existsByEmailOrIpAddress, findByIsAnonymous

### custom (2)

- `PartnerUserAccessLogRepositoryCustom` (interface) repository/PartnerUserAccessLogRepositoryCustom.java · public: countTodayAccesses, findRecent7DaysAccessCount, countTodayUniqueVisitors
- `PartnerUserAccessLogRepositoryImpl` repository/PartnerUserAccessLogRepositoryImpl.java · extends/implements PartnerUserAccessLogRepositoryCustom · public: countTodayAccesses, countTodayUniqueVisitors, findRecent7DaysAccessCount · 주입: JPAQueryFactory

### event (4)

- `PartnerUserAccessLogConsumer` service/kafka/PartnerUserAccessLogConsumer.java · public: consume · 주입: PartnerUserAccessLogRepository, PartnerGeoIPService
- `PartnerUserAccessLogConsumer_backup` service/kafka/PartnerUserAccessLogConsumer_backup.java · public: consume · 주입: PartnerUserAccessLogRepository, PartnerGeoIPService
- `PartnerUserAccessLogProducer` service/kafka/PartnerUserAccessLogProducer.java · public: sendAccessLog · 주입: KafkaTemplate
- `PartnerUserAccessLogProducer_backup` service/kafka/PartnerUserAccessLogProducer_backup.java · public: sendAccessLog · 주입: KafkaTemplate

### config (1)

- `PartnerAccessLogAopConfiguration` config/PartnerAccessLogAopConfiguration.java · public: initializeAopConfiguration, accessLogCacheManager, logAopStatistics, generateFinalAopReport, validateAopConfiguration · 주입: PartnerAccessLogExceptionAspect, PartnerAccessLogMethodLoggingAspect, PartnerAccessLogPerformanceAspect

### aop (3)

- `PartnerAccessLogExceptionAspect` aop/aspect/PartnerAccessLogExceptionAspect.java · public: handleAccessLogException, getExceptionStats
- `PartnerAccessLogMethodLoggingAspect` aop/aspect/PartnerAccessLogMethodLoggingAspect.java · public: logMethodEntry, logMethodExit, logMethodException, getLoggingStats
- `PartnerAccessLogPerformanceAspect` aop/aspect/PartnerAccessLogPerformanceAspect.java · public: monitorPerformance, getPerformanceStats, getMethodStats

### other (3)

- `PartnerAccessLogExceptionHandler` (@interface) aop/annotation/PartnerAccessLogExceptionHandler.java
- `PartnerAccessLogMethodLogger` (@interface) aop/annotation/PartnerAccessLogMethodLogger.java
- `PartnerAccessLogPerformanceMonitor` (@interface) aop/annotation/PartnerAccessLogPerformanceMonitor.java

### 다른 도메인 참조 (import·주입 기준)

- **accessLog**: `UserAccessLog` ← PartnerUserAccessLogCommandService, PartnerUserAccessLogConsumer, PartnerUserAccessLogConsumer_backup, PartnerUserAccessLogRepository

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

- 발행 kafka `user-access-log-topic`: `PartnerUserAccessLogProducer#sendAccessLog` → `UserAccessLogConsumer#consume` [accessLog], `UserAccessLogConsumer_backup#consume` [accessLog], `AdminUserAccessLogConsumer#consume` [adminaccesslog], `AdminUserAccessLogConsumer_backup#consume` [adminaccesslog], `InternalUserAccessLogConsumer#consume` [internalaccesslog], `InternalUserAccessLogConsumer_backup#consume` [internalaccesslog], `LegacyUserAccessLogConsumer#consume` [legacyaccesslog], `LegacyUserAccessLogConsumer_backup#consume` [legacyaccesslog], `PartnerUserAccessLogConsumer#consume` [partneraccesslog], `PartnerUserAccessLogConsumer_backup#consume` [partneraccesslog], `SettlementUserAccessLogConsumer#consume` [settlementaccesslog], `SettlementUserAccessLogConsumer_backup#consume` [settlementaccesslog]
- 발행 kafka `user-access-log-topic`: `PartnerUserAccessLogProducer_backup#sendAccessLog` → `UserAccessLogConsumer#consume` [accessLog], `UserAccessLogConsumer_backup#consume` [accessLog], `AdminUserAccessLogConsumer#consume` [adminaccesslog], `AdminUserAccessLogConsumer_backup#consume` [adminaccesslog], `InternalUserAccessLogConsumer#consume` [internalaccesslog], `InternalUserAccessLogConsumer_backup#consume` [internalaccesslog], `LegacyUserAccessLogConsumer#consume` [legacyaccesslog], `LegacyUserAccessLogConsumer_backup#consume` [legacyaccesslog], `PartnerUserAccessLogConsumer#consume` [partneraccesslog], `PartnerUserAccessLogConsumer_backup#consume` [partneraccesslog], `SettlementUserAccessLogConsumer#consume` [settlementaccesslog], `SettlementUserAccessLogConsumer_backup#consume` [settlementaccesslog]
- 구독 kafka `user-access-log-topic`: `PartnerUserAccessLogConsumer#consume` (groupId=user-access-log-group containerFactory=userAccessLogKafkaListenerContainerFactory) ← `UserAccessLogProducer#sendAccessLog` [accessLog], `UserAccessLogProducer_backup#sendAccessLog` [accessLog], `AdminUserAccessLogProducer#sendAccessLog` [adminaccesslog], `AdminUserAccessLogProducer_backup#sendAccessLog` [adminaccesslog], `InternalUserAccessLogProducer#sendAccessLog` [internalaccesslog], `InternalUserAccessLogProducer_backup#sendAccessLog` [internalaccesslog], `LegacyUserAccessLogProducer#sendAccessLog` [legacyaccesslog], `LegacyUserAccessLogProducer_backup#sendAccessLog` [legacyaccesslog], `PartnerUserAccessLogProducer#sendAccessLog` [partneraccesslog], `PartnerUserAccessLogProducer_backup#sendAccessLog` [partneraccesslog], `SettlementUserAccessLogProducer#sendAccessLog` [settlementaccesslog], `SettlementUserAccessLogProducer_backup#sendAccessLog` [settlementaccesslog]
- 구독 kafka `user-access-log-topic`: `PartnerUserAccessLogConsumer_backup#consume` (groupId=user-access-log-group containerFactory=userAccessLogKafkaListenerContainerFactory) ← `UserAccessLogProducer#sendAccessLog` [accessLog], `UserAccessLogProducer_backup#sendAccessLog` [accessLog], `AdminUserAccessLogProducer#sendAccessLog` [adminaccesslog], `AdminUserAccessLogProducer_backup#sendAccessLog` [adminaccesslog], `InternalUserAccessLogProducer#sendAccessLog` [internalaccesslog], `InternalUserAccessLogProducer_backup#sendAccessLog` [internalaccesslog], `LegacyUserAccessLogProducer#sendAccessLog` [legacyaccesslog], `LegacyUserAccessLogProducer_backup#sendAccessLog` [legacyaccesslog], `PartnerUserAccessLogProducer#sendAccessLog` [partneraccesslog], `PartnerUserAccessLogProducer_backup#sendAccessLog` [partneraccesslog], `SettlementUserAccessLogProducer#sendAccessLog` [settlementaccesslog], `SettlementUserAccessLogProducer_backup#sendAccessLog` [settlementaccesslog]

## 6. AOP

- `PartnerAccessLogExceptionAspect#handleAccessLogException` @Around("@annotation(exceptionHandler)") · 바인딩: `@PartnerAccessLogExceptionHandler`
- `PartnerAccessLogMethodLoggingAspect#logMethodEntry` @Before("@annotation(methodLogger)") · 바인딩: `@PartnerAccessLogMethodLogger`
- `PartnerAccessLogMethodLoggingAspect#logMethodExit` @AfterReturning(pointcut = "@annotation(methodLogger)", returning = "returnValue") · 바인딩: `@PartnerAccessLogMethodLogger`
- `PartnerAccessLogMethodLoggingAspect#logMethodException` @AfterThrowing(pointcut = "@annotation(methodLogger)", throwing = "exception") · 바인딩: `@PartnerAccessLogMethodLogger`
- `PartnerAccessLogPerformanceAspect#monitorPerformance` @Around("@annotation(monitor)") · 바인딩: `@PartnerAccessLogPerformanceMonitor`

어노테이션 적용 지점(메서드에 붙은 곳):

- `@PartnerAccessLogExceptionHandler`: `PartnerUserAccessLogCommandService#createAccessLog`, `PartnerUserAccessLogCommandService#validateLogDto`, `PartnerUserAccessLogCommandService#enrichWithGeoData`, `PartnerUserAccessLogCommandService#mapToEntity`, `PartnerGeoIPService#getLocation`, `PartnerUserAccessLogConsumer#consume`, `PartnerUserAccessLogConsumer#buildUserAccessLog`, `PartnerUserAccessLogProducer#sendAccessLog`, `PartnerUserAccessLogQueryService#getTodayAccessCount`, `PartnerUserAccessLogQueryService#getTodayUniqueVisitorCount`, `PartnerUserAccessLogQueryService#getRecent7DaysAccessCount`
- `@PartnerAccessLogMethodLogger`: `PartnerUserAccessLogCommandService#createAccessLog`, `PartnerUserAccessLogCommandService#validateLogDto`, `PartnerUserAccessLogCommandService#enrichWithGeoData`, `PartnerUserAccessLogCommandService#mapToEntity`, `PartnerGeoIPService#getLocation`, `PartnerUserAccessLogConsumer#consume`, `PartnerUserAccessLogConsumer#buildUserAccessLog`, `PartnerUserAccessLogProducer#sendAccessLog`, `PartnerUserAccessLogQueryService#getTodayAccessCount`, `PartnerUserAccessLogQueryService#getTodayUniqueVisitorCount`, `PartnerUserAccessLogQueryService#getRecent7DaysAccessCount`
- `@PartnerAccessLogPerformanceMonitor`: `PartnerUserAccessLogCommandService#createAccessLog`, `PartnerUserAccessLogCommandService#enrichWithGeoData`, `PartnerGeoIPService#getLocation`, `PartnerUserAccessLogConsumer#consume`, `PartnerUserAccessLogProducer#sendAccessLog`, `PartnerUserAccessLogQueryService#getTodayAccessCount`, `PartnerUserAccessLogQueryService#getTodayUniqueVisitorCount`, `PartnerUserAccessLogQueryService#getRecent7DaysAccessCount`

