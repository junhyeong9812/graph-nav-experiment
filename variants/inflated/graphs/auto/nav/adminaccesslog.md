# adminaccesslog 도메인

기준 경로: `src/main/java/com/fream/back/domain/adminaccesslog/` · 파일 21개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/admin/access-log/commands/create` | `AdminUserAccessLogCommandController#createAccessLog` | `this#enrichAccessLogWithRequestData`, `AdminUserAccessLogCommandService#createAccessLog` |
| GET | `/admin/access-log/queries/today` | `AdminUserAccessLogQueryController#getTodayAccessCount` | `AdminUserAccessLogQueryService#getTodayUniqueVisitorCount` |
| GET | `/admin/access-log/queries/week` | `AdminUserAccessLogQueryController#getWeekAccessCount` | `AdminUserAccessLogQueryService#getRecent7DaysAccessCount` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `AdminAccessLogAopConfiguration`
  - `logAopStatistics` → AdminAccessLogExceptionAspect#getExceptionStats, AdminAccessLogMethodLoggingAspect#getLoggingStats, AdminAccessLogPerformanceAspect#getPerformanceStats, this#evaluateAopHealth
  - `evaluateAopHealth` → this#logImprovementSuggestions
  - `generateFinalAopReport` → AdminAccessLogExceptionAspect#getExceptionStats, AdminAccessLogMethodLoggingAspect#getLoggingStats, AdminAccessLogPerformanceAspect#getPerformanceStats
- `AdminAccessLogExceptionAspect`
  - `handleAccessLogException` → this#executeWithRetry, this#executeWithExceptionHandling
  - `executeWithRetry` → this#calculateBackoffTime, this#convertAndLogException
  - `executeWithExceptionHandling` → this#logException, this#convertAndLogException
  - `convertAndLogException` → this#convertToAccessLogException, this#logException
  - `convertToAccessLogException` → this#createSaveException, this#createQueryException, this#createKafkaException, this#createGeoIPException, this#createValidationException, this#createGeneralException
- `AdminAccessLogMethodLoggingAspect`
  - `logMethodEntry` → this#getThreadMethodKey, this#buildEntryLogMessage, this#logByLevel
  - `logMethodExit` → this#getThreadMethodKey, this#buildExitLogMessage, this#logByLevel, this#logMethodStatistics
  - `logMethodException` → this#getThreadMethodKey
  - `buildEntryLogMessage` → this#formatParameters
  - `buildExitLogMessage` → this#formatReturnValue
  - `formatParameters` → this#maskSensitiveData
  - `formatReturnValue` → this#maskSensitiveData
- `AdminAccessLogPerformanceAspect`
  - `monitorPerformance` → this#measureCurrentMemoryUsage, this#analyzeAndLogPerformance
  - `analyzeAndLogPerformance` → this#updateExecutionTimeStatistics, this#calculatePerformanceGrade, this#logPerformanceResults, this#collectPerformanceMetrics
  - `logPerformanceResults` → this#formatBytes, this#logSlowQueryDetails
- `AdminUserAccessLogCommandService`
  - `createAccessLog` → this#validateLogDto, AdminUserAccessLogProducer#sendAccessLog, this#enrichWithGeoData, this#mapToEntity, AdminUserAccessLogRepository#save~
  - `enrichWithGeoData` → AdminGeoIPService#getLocation
- `AdminUserAccessLogConsumer`
  - `consume` → AdminGeoIPService#getLocation, this#buildUserAccessLog, AdminUserAccessLogRepository#save~
- `AdminUserAccessLogConsumer_backup`
  - `consume` → AdminGeoIPService#getLocation, this#buildUserAccessLog, AdminUserAccessLogRepository#save~
- `AdminUserAccessLogProducer`
  - `sendAccessLog` → kafka:user-access-log-topic
- `AdminUserAccessLogProducer_backup`
  - `sendAccessLog` → kafka:user-access-log-topic
- `AdminUserAccessLogQueryService`
  - `getTodayAccessCount` → AdminUserAccessLogRepository#countTodayAccesses
  - `getTodayUniqueVisitorCount` → AdminUserAccessLogRepository#countTodayUniqueVisitors
  - `getRecent7DaysAccessCount` → AdminUserAccessLogRepository#findRecent7DaysAccessCount
- `AdminUserAccessLogQueryService_backup`
  - `getTodayAccessCount` → AdminUserAccessLogRepository#countTodayAccesses
  - `getTodayUniqueVisitorCount` → AdminUserAccessLogRepository#countTodayUniqueVisitors
  - `getRecent7DaysAccessCount` → AdminUserAccessLogRepository#findRecent7DaysAccessCount

## 3. 클래스 목록 (역할별)

### controller (2)

- `AdminUserAccessLogCommandController` controller/command/AdminUserAccessLogCommandController.java · public: createAccessLog · 주입: AdminUserAccessLogCommandService
- `AdminUserAccessLogQueryController` controller/query/AdminUserAccessLogQueryController.java · public: getTodayAccessCount, getWeekAccessCount · 주입: AdminUserAccessLogQueryService

### service-command (1)

- `AdminUserAccessLogCommandService` service/command/AdminUserAccessLogCommandService.java · public: createAccessLog · 주입: AdminUserAccessLogRepository, AdminGeoIPService, AdminUserAccessLogProducer

### service-query (2)

- `AdminUserAccessLogQueryService` service/query/AdminUserAccessLogQueryService.java · public: getTodayAccessCount, getTodayUniqueVisitorCount, getRecent7DaysAccessCount · 주입: AdminUserAccessLogRepository
- `AdminUserAccessLogQueryService_backup` service/query/AdminUserAccessLogQueryService_backup.java · public: getTodayAccessCount, getTodayUniqueVisitorCount, getRecent7DaysAccessCount · 주입: AdminUserAccessLogRepository

### service (2)

- `AdminGeoIPService` service/geo/AdminGeoIPService.java · public: getLocation
- `AdminGeoIPService_backup` service/geo/AdminGeoIPService_backup.java · public: getLocation

### repository (1)

- `AdminUserAccessLogRepository` (interface) repository/AdminUserAccessLogRepository.java · extends/implements JpaRepository, AdminUserAccessLogRepositoryCustom · public: existsByEmailOrIpAddress, findByIsAnonymous

### custom (2)

- `AdminUserAccessLogRepositoryCustom` (interface) repository/AdminUserAccessLogRepositoryCustom.java · public: countTodayAccesses, findRecent7DaysAccessCount, countTodayUniqueVisitors
- `AdminUserAccessLogRepositoryImpl` repository/AdminUserAccessLogRepositoryImpl.java · extends/implements AdminUserAccessLogRepositoryCustom · public: countTodayAccesses, countTodayUniqueVisitors, findRecent7DaysAccessCount · 주입: JPAQueryFactory

### event (4)

- `AdminUserAccessLogConsumer` service/kafka/AdminUserAccessLogConsumer.java · public: consume · 주입: AdminUserAccessLogRepository, AdminGeoIPService
- `AdminUserAccessLogConsumer_backup` service/kafka/AdminUserAccessLogConsumer_backup.java · public: consume · 주입: AdminUserAccessLogRepository, AdminGeoIPService
- `AdminUserAccessLogProducer` service/kafka/AdminUserAccessLogProducer.java · public: sendAccessLog · 주입: KafkaTemplate
- `AdminUserAccessLogProducer_backup` service/kafka/AdminUserAccessLogProducer_backup.java · public: sendAccessLog · 주입: KafkaTemplate

### config (1)

- `AdminAccessLogAopConfiguration` config/AdminAccessLogAopConfiguration.java · public: initializeAopConfiguration, accessLogCacheManager, logAopStatistics, generateFinalAopReport, validateAopConfiguration · 주입: AdminAccessLogExceptionAspect, AdminAccessLogMethodLoggingAspect, AdminAccessLogPerformanceAspect

### aop (3)

- `AdminAccessLogExceptionAspect` aop/aspect/AdminAccessLogExceptionAspect.java · public: handleAccessLogException, getExceptionStats
- `AdminAccessLogMethodLoggingAspect` aop/aspect/AdminAccessLogMethodLoggingAspect.java · public: logMethodEntry, logMethodExit, logMethodException, getLoggingStats
- `AdminAccessLogPerformanceAspect` aop/aspect/AdminAccessLogPerformanceAspect.java · public: monitorPerformance, getPerformanceStats, getMethodStats

### other (3)

- `AdminAccessLogExceptionHandler` (@interface) aop/annotation/AdminAccessLogExceptionHandler.java
- `AdminAccessLogMethodLogger` (@interface) aop/annotation/AdminAccessLogMethodLogger.java
- `AdminAccessLogPerformanceMonitor` (@interface) aop/annotation/AdminAccessLogPerformanceMonitor.java

### 다른 도메인 참조 (import·주입 기준)

- **accessLog**: `UserAccessLog` ← AdminUserAccessLogCommandService, AdminUserAccessLogConsumer, AdminUserAccessLogConsumer_backup, AdminUserAccessLogRepository

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

- 발행 kafka `user-access-log-topic`: `AdminUserAccessLogProducer#sendAccessLog` → `UserAccessLogConsumer#consume` [accessLog], `UserAccessLogConsumer_backup#consume` [accessLog], `AdminUserAccessLogConsumer#consume` [adminaccesslog], `AdminUserAccessLogConsumer_backup#consume` [adminaccesslog], `InternalUserAccessLogConsumer#consume` [internalaccesslog], `InternalUserAccessLogConsumer_backup#consume` [internalaccesslog], `LegacyUserAccessLogConsumer#consume` [legacyaccesslog], `LegacyUserAccessLogConsumer_backup#consume` [legacyaccesslog], `PartnerUserAccessLogConsumer#consume` [partneraccesslog], `PartnerUserAccessLogConsumer_backup#consume` [partneraccesslog], `SettlementUserAccessLogConsumer#consume` [settlementaccesslog], `SettlementUserAccessLogConsumer_backup#consume` [settlementaccesslog]
- 발행 kafka `user-access-log-topic`: `AdminUserAccessLogProducer_backup#sendAccessLog` → `UserAccessLogConsumer#consume` [accessLog], `UserAccessLogConsumer_backup#consume` [accessLog], `AdminUserAccessLogConsumer#consume` [adminaccesslog], `AdminUserAccessLogConsumer_backup#consume` [adminaccesslog], `InternalUserAccessLogConsumer#consume` [internalaccesslog], `InternalUserAccessLogConsumer_backup#consume` [internalaccesslog], `LegacyUserAccessLogConsumer#consume` [legacyaccesslog], `LegacyUserAccessLogConsumer_backup#consume` [legacyaccesslog], `PartnerUserAccessLogConsumer#consume` [partneraccesslog], `PartnerUserAccessLogConsumer_backup#consume` [partneraccesslog], `SettlementUserAccessLogConsumer#consume` [settlementaccesslog], `SettlementUserAccessLogConsumer_backup#consume` [settlementaccesslog]
- 구독 kafka `user-access-log-topic`: `AdminUserAccessLogConsumer#consume` (groupId=user-access-log-group containerFactory=userAccessLogKafkaListenerContainerFactory) ← `UserAccessLogProducer#sendAccessLog` [accessLog], `UserAccessLogProducer_backup#sendAccessLog` [accessLog], `AdminUserAccessLogProducer#sendAccessLog` [adminaccesslog], `AdminUserAccessLogProducer_backup#sendAccessLog` [adminaccesslog], `InternalUserAccessLogProducer#sendAccessLog` [internalaccesslog], `InternalUserAccessLogProducer_backup#sendAccessLog` [internalaccesslog], `LegacyUserAccessLogProducer#sendAccessLog` [legacyaccesslog], `LegacyUserAccessLogProducer_backup#sendAccessLog` [legacyaccesslog], `PartnerUserAccessLogProducer#sendAccessLog` [partneraccesslog], `PartnerUserAccessLogProducer_backup#sendAccessLog` [partneraccesslog], `SettlementUserAccessLogProducer#sendAccessLog` [settlementaccesslog], `SettlementUserAccessLogProducer_backup#sendAccessLog` [settlementaccesslog]
- 구독 kafka `user-access-log-topic`: `AdminUserAccessLogConsumer_backup#consume` (groupId=user-access-log-group containerFactory=userAccessLogKafkaListenerContainerFactory) ← `UserAccessLogProducer#sendAccessLog` [accessLog], `UserAccessLogProducer_backup#sendAccessLog` [accessLog], `AdminUserAccessLogProducer#sendAccessLog` [adminaccesslog], `AdminUserAccessLogProducer_backup#sendAccessLog` [adminaccesslog], `InternalUserAccessLogProducer#sendAccessLog` [internalaccesslog], `InternalUserAccessLogProducer_backup#sendAccessLog` [internalaccesslog], `LegacyUserAccessLogProducer#sendAccessLog` [legacyaccesslog], `LegacyUserAccessLogProducer_backup#sendAccessLog` [legacyaccesslog], `PartnerUserAccessLogProducer#sendAccessLog` [partneraccesslog], `PartnerUserAccessLogProducer_backup#sendAccessLog` [partneraccesslog], `SettlementUserAccessLogProducer#sendAccessLog` [settlementaccesslog], `SettlementUserAccessLogProducer_backup#sendAccessLog` [settlementaccesslog]

## 6. AOP

- `AdminAccessLogExceptionAspect#handleAccessLogException` @Around("@annotation(exceptionHandler)") · 바인딩: `@AdminAccessLogExceptionHandler`
- `AdminAccessLogMethodLoggingAspect#logMethodEntry` @Before("@annotation(methodLogger)") · 바인딩: `@AdminAccessLogMethodLogger`
- `AdminAccessLogMethodLoggingAspect#logMethodExit` @AfterReturning(pointcut = "@annotation(methodLogger)", returning = "returnValue") · 바인딩: `@AdminAccessLogMethodLogger`
- `AdminAccessLogMethodLoggingAspect#logMethodException` @AfterThrowing(pointcut = "@annotation(methodLogger)", throwing = "exception") · 바인딩: `@AdminAccessLogMethodLogger`
- `AdminAccessLogPerformanceAspect#monitorPerformance` @Around("@annotation(monitor)") · 바인딩: `@AdminAccessLogPerformanceMonitor`

어노테이션 적용 지점(메서드에 붙은 곳):

- `@AdminAccessLogExceptionHandler`: `AdminUserAccessLogCommandService#createAccessLog`, `AdminUserAccessLogCommandService#validateLogDto`, `AdminUserAccessLogCommandService#enrichWithGeoData`, `AdminUserAccessLogCommandService#mapToEntity`, `AdminGeoIPService#getLocation`, `AdminUserAccessLogConsumer#consume`, `AdminUserAccessLogConsumer#buildUserAccessLog`, `AdminUserAccessLogProducer#sendAccessLog`, `AdminUserAccessLogQueryService#getTodayAccessCount`, `AdminUserAccessLogQueryService#getTodayUniqueVisitorCount`, `AdminUserAccessLogQueryService#getRecent7DaysAccessCount`
- `@AdminAccessLogMethodLogger`: `AdminUserAccessLogCommandService#createAccessLog`, `AdminUserAccessLogCommandService#validateLogDto`, `AdminUserAccessLogCommandService#enrichWithGeoData`, `AdminUserAccessLogCommandService#mapToEntity`, `AdminGeoIPService#getLocation`, `AdminUserAccessLogConsumer#consume`, `AdminUserAccessLogConsumer#buildUserAccessLog`, `AdminUserAccessLogProducer#sendAccessLog`, `AdminUserAccessLogQueryService#getTodayAccessCount`, `AdminUserAccessLogQueryService#getTodayUniqueVisitorCount`, `AdminUserAccessLogQueryService#getRecent7DaysAccessCount`
- `@AdminAccessLogPerformanceMonitor`: `AdminUserAccessLogCommandService#createAccessLog`, `AdminUserAccessLogCommandService#enrichWithGeoData`, `AdminGeoIPService#getLocation`, `AdminUserAccessLogConsumer#consume`, `AdminUserAccessLogProducer#sendAccessLog`, `AdminUserAccessLogQueryService#getTodayAccessCount`, `AdminUserAccessLogQueryService#getTodayUniqueVisitorCount`, `AdminUserAccessLogQueryService#getRecent7DaysAccessCount`

