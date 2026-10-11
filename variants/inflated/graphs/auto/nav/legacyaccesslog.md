# legacyaccesslog 도메인

기준 경로: `src/main/java/com/fream/back/domain/legacyaccesslog/` · 파일 21개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/legacy/access-log/commands/create` | `LegacyUserAccessLogCommandController#createAccessLog` | `this#enrichAccessLogWithRequestData`, `LegacyUserAccessLogCommandService#createAccessLog` |
| GET | `/legacy/access-log/queries/today` | `LegacyUserAccessLogQueryController#getTodayAccessCount` | `LegacyUserAccessLogQueryService#getTodayUniqueVisitorCount` |
| GET | `/legacy/access-log/queries/week` | `LegacyUserAccessLogQueryController#getWeekAccessCount` | `LegacyUserAccessLogQueryService#getRecent7DaysAccessCount` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `LegacyAccessLogAopConfiguration`
  - `logAopStatistics` → LegacyAccessLogExceptionAspect#getExceptionStats, LegacyAccessLogMethodLoggingAspect#getLoggingStats, LegacyAccessLogPerformanceAspect#getPerformanceStats, this#evaluateAopHealth
  - `evaluateAopHealth` → this#logImprovementSuggestions
  - `generateFinalAopReport` → LegacyAccessLogExceptionAspect#getExceptionStats, LegacyAccessLogMethodLoggingAspect#getLoggingStats, LegacyAccessLogPerformanceAspect#getPerformanceStats
- `LegacyAccessLogExceptionAspect`
  - `handleAccessLogException` → this#executeWithRetry, this#executeWithExceptionHandling
  - `executeWithRetry` → this#calculateBackoffTime, this#convertAndLogException
  - `executeWithExceptionHandling` → this#logException, this#convertAndLogException
  - `convertAndLogException` → this#convertToAccessLogException, this#logException
  - `convertToAccessLogException` → this#createSaveException, this#createQueryException, this#createKafkaException, this#createGeoIPException, this#createValidationException, this#createGeneralException
- `LegacyAccessLogMethodLoggingAspect`
  - `logMethodEntry` → this#getThreadMethodKey, this#buildEntryLogMessage, this#logByLevel
  - `logMethodExit` → this#getThreadMethodKey, this#buildExitLogMessage, this#logByLevel, this#logMethodStatistics
  - `logMethodException` → this#getThreadMethodKey
  - `buildEntryLogMessage` → this#formatParameters
  - `buildExitLogMessage` → this#formatReturnValue
  - `formatParameters` → this#maskSensitiveData
  - `formatReturnValue` → this#maskSensitiveData
- `LegacyAccessLogPerformanceAspect`
  - `monitorPerformance` → this#measureCurrentMemoryUsage, this#analyzeAndLogPerformance
  - `analyzeAndLogPerformance` → this#updateExecutionTimeStatistics, this#calculatePerformanceGrade, this#logPerformanceResults, this#collectPerformanceMetrics
  - `logPerformanceResults` → this#formatBytes, this#logSlowQueryDetails
- `LegacyUserAccessLogCommandService`
  - `createAccessLog` → this#validateLogDto, LegacyUserAccessLogProducer#sendAccessLog, this#enrichWithGeoData, this#mapToEntity, LegacyUserAccessLogRepository#save~
  - `enrichWithGeoData` → LegacyGeoIPService#getLocation
- `LegacyUserAccessLogConsumer`
  - `consume` → LegacyGeoIPService#getLocation, this#buildUserAccessLog, LegacyUserAccessLogRepository#save~
- `LegacyUserAccessLogConsumer_backup`
  - `consume` → LegacyGeoIPService#getLocation, this#buildUserAccessLog, LegacyUserAccessLogRepository#save~
- `LegacyUserAccessLogProducer`
  - `sendAccessLog` → kafka:user-access-log-topic
- `LegacyUserAccessLogProducer_backup`
  - `sendAccessLog` → kafka:user-access-log-topic
- `LegacyUserAccessLogQueryService`
  - `getTodayAccessCount` → LegacyUserAccessLogRepository#countTodayAccesses
  - `getTodayUniqueVisitorCount` → LegacyUserAccessLogRepository#countTodayUniqueVisitors
  - `getRecent7DaysAccessCount` → LegacyUserAccessLogRepository#findRecent7DaysAccessCount
- `LegacyUserAccessLogQueryService_backup`
  - `getTodayAccessCount` → LegacyUserAccessLogRepository#countTodayAccesses
  - `getTodayUniqueVisitorCount` → LegacyUserAccessLogRepository#countTodayUniqueVisitors
  - `getRecent7DaysAccessCount` → LegacyUserAccessLogRepository#findRecent7DaysAccessCount

## 3. 클래스 목록 (역할별)

### controller (2)

- `LegacyUserAccessLogCommandController` controller/command/LegacyUserAccessLogCommandController.java · public: createAccessLog · 주입: LegacyUserAccessLogCommandService
- `LegacyUserAccessLogQueryController` controller/query/LegacyUserAccessLogQueryController.java · public: getTodayAccessCount, getWeekAccessCount · 주입: LegacyUserAccessLogQueryService

### service-command (1)

- `LegacyUserAccessLogCommandService` service/command/LegacyUserAccessLogCommandService.java · public: createAccessLog · 주입: LegacyUserAccessLogRepository, LegacyGeoIPService, LegacyUserAccessLogProducer

### service-query (2)

- `LegacyUserAccessLogQueryService` service/query/LegacyUserAccessLogQueryService.java · public: getTodayAccessCount, getTodayUniqueVisitorCount, getRecent7DaysAccessCount · 주입: LegacyUserAccessLogRepository
- `LegacyUserAccessLogQueryService_backup` service/query/LegacyUserAccessLogQueryService_backup.java · public: getTodayAccessCount, getTodayUniqueVisitorCount, getRecent7DaysAccessCount · 주입: LegacyUserAccessLogRepository

### service (2)

- `LegacyGeoIPService` service/geo/LegacyGeoIPService.java · public: getLocation
- `LegacyGeoIPService_backup` service/geo/LegacyGeoIPService_backup.java · public: getLocation

### repository (1)

- `LegacyUserAccessLogRepository` (interface) repository/LegacyUserAccessLogRepository.java · extends/implements JpaRepository, LegacyUserAccessLogRepositoryCustom · public: existsByEmailOrIpAddress, findByIsAnonymous

### custom (2)

- `LegacyUserAccessLogRepositoryCustom` (interface) repository/LegacyUserAccessLogRepositoryCustom.java · public: countTodayAccesses, findRecent7DaysAccessCount, countTodayUniqueVisitors
- `LegacyUserAccessLogRepositoryImpl` repository/LegacyUserAccessLogRepositoryImpl.java · extends/implements LegacyUserAccessLogRepositoryCustom · public: countTodayAccesses, countTodayUniqueVisitors, findRecent7DaysAccessCount · 주입: JPAQueryFactory

### event (4)

- `LegacyUserAccessLogConsumer` service/kafka/LegacyUserAccessLogConsumer.java · public: consume · 주입: LegacyUserAccessLogRepository, LegacyGeoIPService
- `LegacyUserAccessLogConsumer_backup` service/kafka/LegacyUserAccessLogConsumer_backup.java · public: consume · 주입: LegacyUserAccessLogRepository, LegacyGeoIPService
- `LegacyUserAccessLogProducer` service/kafka/LegacyUserAccessLogProducer.java · public: sendAccessLog · 주입: KafkaTemplate
- `LegacyUserAccessLogProducer_backup` service/kafka/LegacyUserAccessLogProducer_backup.java · public: sendAccessLog · 주입: KafkaTemplate

### config (1)

- `LegacyAccessLogAopConfiguration` config/LegacyAccessLogAopConfiguration.java · public: initializeAopConfiguration, accessLogCacheManager, logAopStatistics, generateFinalAopReport, validateAopConfiguration · 주입: LegacyAccessLogExceptionAspect, LegacyAccessLogMethodLoggingAspect, LegacyAccessLogPerformanceAspect

### aop (3)

- `LegacyAccessLogExceptionAspect` aop/aspect/LegacyAccessLogExceptionAspect.java · public: handleAccessLogException, getExceptionStats
- `LegacyAccessLogMethodLoggingAspect` aop/aspect/LegacyAccessLogMethodLoggingAspect.java · public: logMethodEntry, logMethodExit, logMethodException, getLoggingStats
- `LegacyAccessLogPerformanceAspect` aop/aspect/LegacyAccessLogPerformanceAspect.java · public: monitorPerformance, getPerformanceStats, getMethodStats

### other (3)

- `LegacyAccessLogExceptionHandler` (@interface) aop/annotation/LegacyAccessLogExceptionHandler.java
- `LegacyAccessLogMethodLogger` (@interface) aop/annotation/LegacyAccessLogMethodLogger.java
- `LegacyAccessLogPerformanceMonitor` (@interface) aop/annotation/LegacyAccessLogPerformanceMonitor.java

### 다른 도메인 참조 (import·주입 기준)

- **accessLog**: `UserAccessLog` ← LegacyUserAccessLogCommandService, LegacyUserAccessLogConsumer, LegacyUserAccessLogConsumer_backup, LegacyUserAccessLogRepository

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

- 발행 kafka `user-access-log-topic`: `LegacyUserAccessLogProducer#sendAccessLog` → `UserAccessLogConsumer#consume` [accessLog], `UserAccessLogConsumer_backup#consume` [accessLog], `AdminUserAccessLogConsumer#consume` [adminaccesslog], `AdminUserAccessLogConsumer_backup#consume` [adminaccesslog], `InternalUserAccessLogConsumer#consume` [internalaccesslog], `InternalUserAccessLogConsumer_backup#consume` [internalaccesslog], `LegacyUserAccessLogConsumer#consume` [legacyaccesslog], `LegacyUserAccessLogConsumer_backup#consume` [legacyaccesslog], `PartnerUserAccessLogConsumer#consume` [partneraccesslog], `PartnerUserAccessLogConsumer_backup#consume` [partneraccesslog], `SettlementUserAccessLogConsumer#consume` [settlementaccesslog], `SettlementUserAccessLogConsumer_backup#consume` [settlementaccesslog]
- 발행 kafka `user-access-log-topic`: `LegacyUserAccessLogProducer_backup#sendAccessLog` → `UserAccessLogConsumer#consume` [accessLog], `UserAccessLogConsumer_backup#consume` [accessLog], `AdminUserAccessLogConsumer#consume` [adminaccesslog], `AdminUserAccessLogConsumer_backup#consume` [adminaccesslog], `InternalUserAccessLogConsumer#consume` [internalaccesslog], `InternalUserAccessLogConsumer_backup#consume` [internalaccesslog], `LegacyUserAccessLogConsumer#consume` [legacyaccesslog], `LegacyUserAccessLogConsumer_backup#consume` [legacyaccesslog], `PartnerUserAccessLogConsumer#consume` [partneraccesslog], `PartnerUserAccessLogConsumer_backup#consume` [partneraccesslog], `SettlementUserAccessLogConsumer#consume` [settlementaccesslog], `SettlementUserAccessLogConsumer_backup#consume` [settlementaccesslog]
- 구독 kafka `user-access-log-topic`: `LegacyUserAccessLogConsumer#consume` (groupId=user-access-log-group containerFactory=userAccessLogKafkaListenerContainerFactory) ← `UserAccessLogProducer#sendAccessLog` [accessLog], `UserAccessLogProducer_backup#sendAccessLog` [accessLog], `AdminUserAccessLogProducer#sendAccessLog` [adminaccesslog], `AdminUserAccessLogProducer_backup#sendAccessLog` [adminaccesslog], `InternalUserAccessLogProducer#sendAccessLog` [internalaccesslog], `InternalUserAccessLogProducer_backup#sendAccessLog` [internalaccesslog], `LegacyUserAccessLogProducer#sendAccessLog` [legacyaccesslog], `LegacyUserAccessLogProducer_backup#sendAccessLog` [legacyaccesslog], `PartnerUserAccessLogProducer#sendAccessLog` [partneraccesslog], `PartnerUserAccessLogProducer_backup#sendAccessLog` [partneraccesslog], `SettlementUserAccessLogProducer#sendAccessLog` [settlementaccesslog], `SettlementUserAccessLogProducer_backup#sendAccessLog` [settlementaccesslog]
- 구독 kafka `user-access-log-topic`: `LegacyUserAccessLogConsumer_backup#consume` (groupId=user-access-log-group containerFactory=userAccessLogKafkaListenerContainerFactory) ← `UserAccessLogProducer#sendAccessLog` [accessLog], `UserAccessLogProducer_backup#sendAccessLog` [accessLog], `AdminUserAccessLogProducer#sendAccessLog` [adminaccesslog], `AdminUserAccessLogProducer_backup#sendAccessLog` [adminaccesslog], `InternalUserAccessLogProducer#sendAccessLog` [internalaccesslog], `InternalUserAccessLogProducer_backup#sendAccessLog` [internalaccesslog], `LegacyUserAccessLogProducer#sendAccessLog` [legacyaccesslog], `LegacyUserAccessLogProducer_backup#sendAccessLog` [legacyaccesslog], `PartnerUserAccessLogProducer#sendAccessLog` [partneraccesslog], `PartnerUserAccessLogProducer_backup#sendAccessLog` [partneraccesslog], `SettlementUserAccessLogProducer#sendAccessLog` [settlementaccesslog], `SettlementUserAccessLogProducer_backup#sendAccessLog` [settlementaccesslog]

## 6. AOP

- `LegacyAccessLogExceptionAspect#handleAccessLogException` @Around("@annotation(exceptionHandler)") · 바인딩: `@LegacyAccessLogExceptionHandler`
- `LegacyAccessLogMethodLoggingAspect#logMethodEntry` @Before("@annotation(methodLogger)") · 바인딩: `@LegacyAccessLogMethodLogger`
- `LegacyAccessLogMethodLoggingAspect#logMethodExit` @AfterReturning(pointcut = "@annotation(methodLogger)", returning = "returnValue") · 바인딩: `@LegacyAccessLogMethodLogger`
- `LegacyAccessLogMethodLoggingAspect#logMethodException` @AfterThrowing(pointcut = "@annotation(methodLogger)", throwing = "exception") · 바인딩: `@LegacyAccessLogMethodLogger`
- `LegacyAccessLogPerformanceAspect#monitorPerformance` @Around("@annotation(monitor)") · 바인딩: `@LegacyAccessLogPerformanceMonitor`

어노테이션 적용 지점(메서드에 붙은 곳):

- `@LegacyAccessLogExceptionHandler`: `LegacyUserAccessLogCommandService#createAccessLog`, `LegacyUserAccessLogCommandService#validateLogDto`, `LegacyUserAccessLogCommandService#enrichWithGeoData`, `LegacyUserAccessLogCommandService#mapToEntity`, `LegacyGeoIPService#getLocation`, `LegacyUserAccessLogConsumer#consume`, `LegacyUserAccessLogConsumer#buildUserAccessLog`, `LegacyUserAccessLogProducer#sendAccessLog`, `LegacyUserAccessLogQueryService#getTodayAccessCount`, `LegacyUserAccessLogQueryService#getTodayUniqueVisitorCount`, `LegacyUserAccessLogQueryService#getRecent7DaysAccessCount`
- `@LegacyAccessLogMethodLogger`: `LegacyUserAccessLogCommandService#createAccessLog`, `LegacyUserAccessLogCommandService#validateLogDto`, `LegacyUserAccessLogCommandService#enrichWithGeoData`, `LegacyUserAccessLogCommandService#mapToEntity`, `LegacyGeoIPService#getLocation`, `LegacyUserAccessLogConsumer#consume`, `LegacyUserAccessLogConsumer#buildUserAccessLog`, `LegacyUserAccessLogProducer#sendAccessLog`, `LegacyUserAccessLogQueryService#getTodayAccessCount`, `LegacyUserAccessLogQueryService#getTodayUniqueVisitorCount`, `LegacyUserAccessLogQueryService#getRecent7DaysAccessCount`
- `@LegacyAccessLogPerformanceMonitor`: `LegacyUserAccessLogCommandService#createAccessLog`, `LegacyUserAccessLogCommandService#enrichWithGeoData`, `LegacyGeoIPService#getLocation`, `LegacyUserAccessLogConsumer#consume`, `LegacyUserAccessLogProducer#sendAccessLog`, `LegacyUserAccessLogQueryService#getTodayAccessCount`, `LegacyUserAccessLogQueryService#getTodayUniqueVisitorCount`, `LegacyUserAccessLogQueryService#getRecent7DaysAccessCount`

