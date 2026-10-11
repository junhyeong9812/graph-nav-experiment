# accessLog 도메인

기준 경로: `src/main/java/com/fream/back/domain/accessLog/` · 파일 33개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

타입 선언이 없는 파일(파싱 결과 클래스 0개): `src/main/java/com/fream/back/domain/accessLog/service/command/UserAccessLogCommandService_backup.java`

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/access-log/commands/create` | `UserAccessLogCommandController#createAccessLog` | `this#enrichAccessLogWithRequestData`, `UserAccessLogCommandService#createAccessLog` |
| GET | `/access-log/queries/today` | `UserAccessLogQueryController#getTodayAccessCount` | `UserAccessLogQueryService#getTodayUniqueVisitorCount` |
| GET | `/access-log/queries/week` | `UserAccessLogQueryController#getWeekAccessCount` | `UserAccessLogQueryService#getRecent7DaysAccessCount` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `AccessLogAopConfiguration`
  - `logAopStatistics` → AccessLogExceptionAspect#getExceptionStats, AccessLogMethodLoggingAspect#getLoggingStats, AccessLogPerformanceAspect#getPerformanceStats, this#evaluateAopHealth
  - `evaluateAopHealth` → this#logImprovementSuggestions
  - `generateFinalAopReport` → AccessLogExceptionAspect#getExceptionStats, AccessLogMethodLoggingAspect#getLoggingStats, AccessLogPerformanceAspect#getPerformanceStats
- `AccessLogExceptionAspect`
  - `handleAccessLogException` → this#executeWithRetry, this#executeWithExceptionHandling
  - `executeWithRetry` → this#calculateBackoffTime, this#convertAndLogException
  - `executeWithExceptionHandling` → this#logException, this#convertAndLogException
  - `convertAndLogException` → this#convertToAccessLogException, this#logException
  - `convertToAccessLogException` → this#createSaveException, this#createQueryException, this#createKafkaException, this#createGeoIPException, this#createValidationException, this#createGeneralException
- `AccessLogMethodLoggingAspect`
  - `logMethodEntry` → this#getThreadMethodKey, this#buildEntryLogMessage, this#logByLevel
  - `logMethodExit` → this#getThreadMethodKey, this#buildExitLogMessage, this#logByLevel, this#logMethodStatistics
  - `logMethodException` → this#getThreadMethodKey
  - `buildEntryLogMessage` → this#formatParameters
  - `buildExitLogMessage` → this#formatReturnValue
  - `formatParameters` → this#maskSensitiveData
  - `formatReturnValue` → this#maskSensitiveData
- `AccessLogPerformanceAspect`
  - `monitorPerformance` → this#measureCurrentMemoryUsage, this#analyzeAndLogPerformance
  - `analyzeAndLogPerformance` → this#updateExecutionTimeStatistics, this#calculatePerformanceGrade, this#logPerformanceResults, this#collectPerformanceMetrics
  - `logPerformanceResults` → this#formatBytes, this#logSlowQueryDetails
- `UserAccessLogCommandService`
  - `createAccessLog` → this#validateLogDto, UserAccessLogProducer#sendAccessLog, this#enrichWithGeoData, this#mapToEntity, UserAccessLogRepository#save~
  - `enrichWithGeoData` → GeoIPService#getLocation
- `UserAccessLogConsumer`
  - `consume` → GeoIPService#getLocation, this#buildUserAccessLog, UserAccessLogRepository#save~
- `UserAccessLogConsumer_backup`
  - `consume` → GeoIPService#getLocation, this#buildUserAccessLog, UserAccessLogRepository#save~
- `UserAccessLogProducer`
  - `sendAccessLog` → kafka:user-access-log-topic
- `UserAccessLogProducer_backup`
  - `sendAccessLog` → kafka:user-access-log-topic
- `UserAccessLogQueryService`
  - `getTodayAccessCount` → UserAccessLogRepository#countTodayAccesses
  - `getTodayUniqueVisitorCount` → UserAccessLogRepository#countTodayUniqueVisitors
  - `getRecent7DaysAccessCount` → UserAccessLogRepository#findRecent7DaysAccessCount
- `UserAccessLogQueryService_backup`
  - `getTodayAccessCount` → UserAccessLogRepository#countTodayAccesses
  - `getTodayUniqueVisitorCount` → UserAccessLogRepository#countTodayUniqueVisitors
  - `getRecent7DaysAccessCount` → UserAccessLogRepository#findRecent7DaysAccessCount

## 3. 클래스 목록 (역할별)

### controller (2)

- `UserAccessLogCommandController` controller/command/UserAccessLogCommandController.java · public: createAccessLog · 주입: UserAccessLogCommandService
- `UserAccessLogQueryController` controller/query/UserAccessLogQueryController.java · public: getTodayAccessCount, getWeekAccessCount · 주입: UserAccessLogQueryService

### service-command (1)

- `UserAccessLogCommandService` service/command/UserAccessLogCommandService.java · public: createAccessLog · 주입: UserAccessLogRepository, GeoIPService, UserAccessLogProducer

### service-query (2)

- `UserAccessLogQueryService` service/query/UserAccessLogQueryService.java · public: getTodayAccessCount, getTodayUniqueVisitorCount, getRecent7DaysAccessCount · 주입: UserAccessLogRepository
- `UserAccessLogQueryService_backup` service/query/UserAccessLogQueryService_backup.java · public: getTodayAccessCount, getTodayUniqueVisitorCount, getRecent7DaysAccessCount · 주입: UserAccessLogRepository

### service (2)

- `GeoIPService` service/geo/GeoIPService.java · public: getLocation
- `GeoIPService_backup` service/geo/GeoIPService_backup.java · public: getLocation

### repository (1)

- `UserAccessLogRepository` (interface) repository/UserAccessLogRepository.java · extends/implements JpaRepository, UserAccessLogRepositoryCustom · public: existsByEmailOrIpAddress, findByIsAnonymous

### custom (2)

- `UserAccessLogRepositoryCustom` (interface) repository/UserAccessLogRepositoryCustom.java · public: countTodayAccesses, findRecent7DaysAccessCount, countTodayUniqueVisitors
- `UserAccessLogRepositoryImpl` repository/UserAccessLogRepositoryImpl.java · extends/implements UserAccessLogRepositoryCustom · public: countTodayAccesses, countTodayUniqueVisitors, findRecent7DaysAccessCount · 주입: JPAQueryFactory

### entity (1)

- `UserAccessLog` entity/UserAccessLog.java

### event (4)

- `UserAccessLogConsumer` service/kafka/UserAccessLogConsumer.java · public: consume · 주입: UserAccessLogRepository, GeoIPService
- `UserAccessLogConsumer_backup` service/kafka/UserAccessLogConsumer_backup.java · public: consume · 주입: UserAccessLogRepository, GeoIPService
- `UserAccessLogProducer` service/kafka/UserAccessLogProducer.java · public: sendAccessLog · 주입: KafkaTemplate
- `UserAccessLogProducer_backup` service/kafka/UserAccessLogProducer_backup.java · public: sendAccessLog · 주입: KafkaTemplate

### config (1)

- `AccessLogAopConfiguration` config/AccessLogAopConfiguration.java · public: initializeAopConfiguration, accessLogCacheManager, logAopStatistics, generateFinalAopReport, validateAopConfiguration · 주입: AccessLogExceptionAspect, AccessLogMethodLoggingAspect, AccessLogPerformanceAspect

### aop (3)

- `AccessLogExceptionAspect` aop/aspect/AccessLogExceptionAspect.java · public: handleAccessLogException, getExceptionStats
- `AccessLogMethodLoggingAspect` aop/aspect/AccessLogMethodLoggingAspect.java · public: logMethodEntry, logMethodExit, logMethodException, getLoggingStats
- `AccessLogPerformanceAspect` aop/aspect/AccessLogPerformanceAspect.java · public: monitorPerformance, getPerformanceStats, getMethodStats

### dto (3)

- `DailyAccessCountDto` dto/DailyAccessCountDto.java
- `UserAccessLogDto` dto/UserAccessLogDto.java
- `UserAccessLogEvent` dto/UserAccessLogEvent.java

### exception (7)

- `AccessLogErrorCode` exception/AccessLogErrorCode.java
- `AccessLogException` exception/AccessLogException.java
- `AccessLogKafkaException` exception/AccessLogKafkaException.java
- `AccessLogQueryException` exception/AccessLogQueryException.java
- `AccessLogSaveException` exception/AccessLogSaveException.java
- `GeoIPException` exception/GeoIPException.java
- `InvalidParameterException` exception/InvalidParameterException.java

### other (3)

- `AccessLogExceptionHandler` (@interface) aop/annotation/AccessLogExceptionHandler.java
- `AccessLogMethodLogger` (@interface) aop/annotation/AccessLogMethodLogger.java
- `AccessLogPerformanceMonitor` (@interface) aop/annotation/AccessLogPerformanceMonitor.java

### 다른 도메인 참조 (import·주입 기준)

(없음)

### 이 도메인을 참조하는 다른 도메인

- **adminaccesslog**: UserAccessLog
- **internalaccesslog**: UserAccessLog
- **legacyaccesslog**: UserAccessLog
- **partneraccesslog**: UserAccessLog
- **settlementaccesslog**: UserAccessLog

## 4. JPA 엔티티 연관

- `UserAccessLog` @Entity @Table(name = "user_access_log", indexes = { @Index(name = "idx_access_time", columnList = "ACCESS_TIME"), @Index(name = "idx_ip_access_time", columnList = "ipAddress,ACCESS_TIME") })

## 5. 이벤트·메시징

- 발행 kafka `user-access-log-topic`: `UserAccessLogProducer#sendAccessLog` → `UserAccessLogConsumer#consume` [accessLog], `UserAccessLogConsumer_backup#consume` [accessLog], `AdminUserAccessLogConsumer#consume` [adminaccesslog], `AdminUserAccessLogConsumer_backup#consume` [adminaccesslog], `InternalUserAccessLogConsumer#consume` [internalaccesslog], `InternalUserAccessLogConsumer_backup#consume` [internalaccesslog], `LegacyUserAccessLogConsumer#consume` [legacyaccesslog], `LegacyUserAccessLogConsumer_backup#consume` [legacyaccesslog], `PartnerUserAccessLogConsumer#consume` [partneraccesslog], `PartnerUserAccessLogConsumer_backup#consume` [partneraccesslog], `SettlementUserAccessLogConsumer#consume` [settlementaccesslog], `SettlementUserAccessLogConsumer_backup#consume` [settlementaccesslog]
- 발행 kafka `user-access-log-topic`: `UserAccessLogProducer_backup#sendAccessLog` → `UserAccessLogConsumer#consume` [accessLog], `UserAccessLogConsumer_backup#consume` [accessLog], `AdminUserAccessLogConsumer#consume` [adminaccesslog], `AdminUserAccessLogConsumer_backup#consume` [adminaccesslog], `InternalUserAccessLogConsumer#consume` [internalaccesslog], `InternalUserAccessLogConsumer_backup#consume` [internalaccesslog], `LegacyUserAccessLogConsumer#consume` [legacyaccesslog], `LegacyUserAccessLogConsumer_backup#consume` [legacyaccesslog], `PartnerUserAccessLogConsumer#consume` [partneraccesslog], `PartnerUserAccessLogConsumer_backup#consume` [partneraccesslog], `SettlementUserAccessLogConsumer#consume` [settlementaccesslog], `SettlementUserAccessLogConsumer_backup#consume` [settlementaccesslog]
- 구독 kafka `user-access-log-topic`: `UserAccessLogConsumer#consume` (groupId=user-access-log-group containerFactory=userAccessLogKafkaListenerContainerFactory) ← `UserAccessLogProducer#sendAccessLog` [accessLog], `UserAccessLogProducer_backup#sendAccessLog` [accessLog], `AdminUserAccessLogProducer#sendAccessLog` [adminaccesslog], `AdminUserAccessLogProducer_backup#sendAccessLog` [adminaccesslog], `InternalUserAccessLogProducer#sendAccessLog` [internalaccesslog], `InternalUserAccessLogProducer_backup#sendAccessLog` [internalaccesslog], `LegacyUserAccessLogProducer#sendAccessLog` [legacyaccesslog], `LegacyUserAccessLogProducer_backup#sendAccessLog` [legacyaccesslog], `PartnerUserAccessLogProducer#sendAccessLog` [partneraccesslog], `PartnerUserAccessLogProducer_backup#sendAccessLog` [partneraccesslog], `SettlementUserAccessLogProducer#sendAccessLog` [settlementaccesslog], `SettlementUserAccessLogProducer_backup#sendAccessLog` [settlementaccesslog]
- 구독 kafka `user-access-log-topic`: `UserAccessLogConsumer_backup#consume` (groupId=user-access-log-group containerFactory=userAccessLogKafkaListenerContainerFactory) ← `UserAccessLogProducer#sendAccessLog` [accessLog], `UserAccessLogProducer_backup#sendAccessLog` [accessLog], `AdminUserAccessLogProducer#sendAccessLog` [adminaccesslog], `AdminUserAccessLogProducer_backup#sendAccessLog` [adminaccesslog], `InternalUserAccessLogProducer#sendAccessLog` [internalaccesslog], `InternalUserAccessLogProducer_backup#sendAccessLog` [internalaccesslog], `LegacyUserAccessLogProducer#sendAccessLog` [legacyaccesslog], `LegacyUserAccessLogProducer_backup#sendAccessLog` [legacyaccesslog], `PartnerUserAccessLogProducer#sendAccessLog` [partneraccesslog], `PartnerUserAccessLogProducer_backup#sendAccessLog` [partneraccesslog], `SettlementUserAccessLogProducer#sendAccessLog` [settlementaccesslog], `SettlementUserAccessLogProducer_backup#sendAccessLog` [settlementaccesslog]

## 6. AOP

- `AccessLogExceptionAspect#handleAccessLogException` @Around("@annotation(exceptionHandler)") · 바인딩: `@AccessLogExceptionHandler`
- `AccessLogMethodLoggingAspect#logMethodEntry` @Before("@annotation(methodLogger)") · 바인딩: `@AccessLogMethodLogger`
- `AccessLogMethodLoggingAspect#logMethodExit` @AfterReturning(pointcut = "@annotation(methodLogger)", returning = "returnValue") · 바인딩: `@AccessLogMethodLogger`
- `AccessLogMethodLoggingAspect#logMethodException` @AfterThrowing(pointcut = "@annotation(methodLogger)", throwing = "exception") · 바인딩: `@AccessLogMethodLogger`
- `AccessLogPerformanceAspect#monitorPerformance` @Around("@annotation(monitor)") · 바인딩: `@AccessLogPerformanceMonitor`

어노테이션 적용 지점(메서드에 붙은 곳):

- `@AccessLogExceptionHandler`: `UserAccessLogCommandService#createAccessLog`, `UserAccessLogCommandService#validateLogDto`, `UserAccessLogCommandService#enrichWithGeoData`, `UserAccessLogCommandService#mapToEntity`, `GeoIPService#getLocation`, `UserAccessLogConsumer#consume`, `UserAccessLogConsumer#buildUserAccessLog`, `UserAccessLogProducer#sendAccessLog`, `UserAccessLogQueryService#getTodayAccessCount`, `UserAccessLogQueryService#getTodayUniqueVisitorCount`, `UserAccessLogQueryService#getRecent7DaysAccessCount`
- `@AccessLogMethodLogger`: `UserAccessLogCommandService#createAccessLog`, `UserAccessLogCommandService#validateLogDto`, `UserAccessLogCommandService#enrichWithGeoData`, `UserAccessLogCommandService#mapToEntity`, `GeoIPService#getLocation`, `UserAccessLogConsumer#consume`, `UserAccessLogConsumer#buildUserAccessLog`, `UserAccessLogProducer#sendAccessLog`, `UserAccessLogQueryService#getTodayAccessCount`, `UserAccessLogQueryService#getTodayUniqueVisitorCount`, `UserAccessLogQueryService#getRecent7DaysAccessCount`
- `@AccessLogPerformanceMonitor`: `UserAccessLogCommandService#createAccessLog`, `UserAccessLogCommandService#enrichWithGeoData`, `GeoIPService#getLocation`, `UserAccessLogConsumer#consume`, `UserAccessLogProducer#sendAccessLog`, `UserAccessLogQueryService#getTodayAccessCount`, `UserAccessLogQueryService#getTodayUniqueVisitorCount`, `UserAccessLogQueryService#getRecent7DaysAccessCount`

