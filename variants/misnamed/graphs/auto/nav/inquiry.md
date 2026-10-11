# inquiry 도메인

기준 경로: `src/main/java/com/fream/back/domain/inquiry/` · 파일 33개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

타입 선언이 없는 파일(파싱 결과 클래스 0개): `src/main/java/com/fream/back/domain/inquiry/service/command/ProductInquiryCommandServicebackup.java`

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/access-log/commands/create` | `ProductInquiryCommandController#createAccessLog` | `this#enrichAccessLogWithRequestData`, `ProductInquiryCommandService#createAccessLog` |
| GET | `/access-log/queries/today` | `ProductInquiryQueryController#getTodayAccessCount` | `ProductInquiryQueryService#getTodayUniqueVisitorCount` |
| GET | `/access-log/queries/week` | `ProductInquiryQueryController#getWeekAccessCount` | `ProductInquiryQueryService#getRecent7DaysAccessCount` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `InquiryAopConfiguration`
  - `logAopStatistics` → InquiryExceptionAspect#getExceptionStats, InquiryMethodLoggingAspect#getLoggingStats, InquiryPerformanceAspect#getPerformanceStats, this#evaluateAopHealth
  - `evaluateAopHealth` → this#logImprovementSuggestions
  - `generateFinalAopReport` → InquiryExceptionAspect#getExceptionStats, InquiryMethodLoggingAspect#getLoggingStats, InquiryPerformanceAspect#getPerformanceStats
- `InquiryExceptionAspect`
  - `handleAccessLogException` → this#executeWithRetry, this#executeWithExceptionHandling
  - `executeWithRetry` → this#calculateBackoffTime, this#convertAndLogException
  - `executeWithExceptionHandling` → this#logException, this#convertAndLogException
  - `convertAndLogException` → this#convertToAccessLogException, this#logException
  - `convertToAccessLogException` → this#createSaveException, this#createQueryException, this#createKafkaException, this#createGeoIPException, this#createValidationException, this#createGeneralException
- `InquiryMethodLoggingAspect`
  - `logMethodEntry` → this#getThreadMethodKey, this#buildEntryLogMessage, this#logByLevel
  - `logMethodExit` → this#getThreadMethodKey, this#buildExitLogMessage, this#logByLevel, this#logMethodStatistics
  - `logMethodException` → this#getThreadMethodKey
  - `buildEntryLogMessage` → this#formatParameters
  - `buildExitLogMessage` → this#formatReturnValue
  - `formatParameters` → this#maskSensitiveData
  - `formatReturnValue` → this#maskSensitiveData
- `InquiryPerformanceAspect`
  - `monitorPerformance` → this#measureCurrentMemoryUsage, this#analyzeAndLogPerformance
  - `analyzeAndLogPerformance` → this#updateExecutionTimeStatistics, this#calculatePerformanceGrade, this#logPerformanceResults, this#collectPerformanceMetrics
  - `logPerformanceResults` → this#formatBytes, this#logSlowQueryDetails
- `ProductInquiryCommandService`
  - `createAccessLog` → this#validateLogDto, ProductInquiryProducer#sendAccessLog, this#enrichWithGeoData, this#mapToEntity, ProductInquiryRepository#save~
  - `enrichWithGeoData` → GeoIPService#getLocation
- `ProductInquiryConsumer`
  - `consume` → GeoIPService#getLocation, this#buildUserAccessLog, ProductInquiryRepository#save~
- `ProductInquiryConsumerbackup`
  - `consume` → GeoIPService#getLocation, this#buildUserAccessLog, ProductInquiryRepository#save~
- `ProductInquiryProducer`
  - `sendAccessLog` → kafka:user-access-log-topic
- `ProductInquiryProducerbackup`
  - `sendAccessLog` → kafka:user-access-log-topic
- `ProductInquiryQueryService`
  - `getTodayAccessCount` → ProductInquiryRepository#countTodayAccesses
  - `getTodayUniqueVisitorCount` → ProductInquiryRepository#countTodayUniqueVisitors
  - `getRecent7DaysAccessCount` → ProductInquiryRepository#findRecent7DaysAccessCount
- `ProductInquiryQueryServicebackup`
  - `getTodayAccessCount` → ProductInquiryRepository#countTodayAccesses
  - `getTodayUniqueVisitorCount` → ProductInquiryRepository#countTodayUniqueVisitors
  - `getRecent7DaysAccessCount` → ProductInquiryRepository#findRecent7DaysAccessCount

## 3. 클래스 목록 (역할별)

### controller (2)

- `ProductInquiryCommandController` controller/command/ProductInquiryCommandController.java · public: createAccessLog · 주입: ProductInquiryCommandService
- `ProductInquiryQueryController` controller/query/ProductInquiryQueryController.java · public: getTodayAccessCount, getWeekAccessCount · 주입: ProductInquiryQueryService

### service-command (1)

- `ProductInquiryCommandService` service/command/ProductInquiryCommandService.java · public: createAccessLog · 주입: ProductInquiryRepository, GeoIPService, ProductInquiryProducer

### service-query (2)

- `ProductInquiryQueryService` service/query/ProductInquiryQueryService.java · public: getTodayAccessCount, getTodayUniqueVisitorCount, getRecent7DaysAccessCount · 주입: ProductInquiryRepository
- `ProductInquiryQueryServicebackup` service/query/ProductInquiryQueryServicebackup.java · public: getTodayAccessCount, getTodayUniqueVisitorCount, getRecent7DaysAccessCount · 주입: ProductInquiryRepository

### service (2)

- `GeoIPService` service/geo/GeoIPService.java · public: getLocation
- `GeoIPService_backup` service/geo/GeoIPService_backup.java · public: getLocation

### repository (1)

- `ProductInquiryRepository` (interface) repository/ProductInquiryRepository.java · extends/implements JpaRepository, ProductInquiryRepositoryCustom · public: existsByEmailOrIpAddress, findByIsAnonymous

### custom (2)

- `ProductInquiryRepositoryCustom` (interface) repository/ProductInquiryRepositoryCustom.java · public: countTodayAccesses, findRecent7DaysAccessCount, countTodayUniqueVisitors
- `ProductInquiryRepositoryImpl` repository/ProductInquiryRepositoryImpl.java · extends/implements ProductInquiryRepositoryCustom · public: countTodayAccesses, countTodayUniqueVisitors, findRecent7DaysAccessCount · 주입: JPAQueryFactory

### entity (1)

- `ProductInquiry` entity/ProductInquiry.java

### event (4)

- `ProductInquiryConsumer` service/kafka/ProductInquiryConsumer.java · public: consume · 주입: ProductInquiryRepository, GeoIPService
- `ProductInquiryConsumerbackup` service/kafka/ProductInquiryConsumerbackup.java · public: consume · 주입: ProductInquiryRepository, GeoIPService
- `ProductInquiryProducer` service/kafka/ProductInquiryProducer.java · public: sendAccessLog · 주입: KafkaTemplate
- `ProductInquiryProducerbackup` service/kafka/ProductInquiryProducerbackup.java · public: sendAccessLog · 주입: KafkaTemplate

### config (1)

- `InquiryAopConfiguration` config/InquiryAopConfiguration.java · public: initializeAopConfiguration, accessLogCacheManager, logAopStatistics, generateFinalAopReport, validateAopConfiguration · 주입: InquiryExceptionAspect, InquiryMethodLoggingAspect, InquiryPerformanceAspect

### aop (3)

- `InquiryExceptionAspect` aop/aspect/InquiryExceptionAspect.java · public: handleAccessLogException, getExceptionStats
- `InquiryMethodLoggingAspect` aop/aspect/InquiryMethodLoggingAspect.java · public: logMethodEntry, logMethodExit, logMethodException, getLoggingStats
- `InquiryPerformanceAspect` aop/aspect/InquiryPerformanceAspect.java · public: monitorPerformance, getPerformanceStats, getMethodStats

### dto (3)

- `DailyAccessCountDto` dto/DailyAccessCountDto.java
- `ProductInquiryDto` dto/ProductInquiryDto.java
- `ProductInquiryEvent` dto/ProductInquiryEvent.java

### exception (7)

- `GeoIPException` exception/GeoIPException.java
- `InquiryErrorCode` exception/InquiryErrorCode.java
- `InquiryException` exception/InquiryException.java
- `InquiryKafkaException` exception/InquiryKafkaException.java
- `InquiryQueryException` exception/InquiryQueryException.java
- `InquirySaveException` exception/InquirySaveException.java
- `InvalidParameterException` exception/InvalidParameterException.java

### other (3)

- `InquiryExceptionHandler` (@interface) aop/annotation/InquiryExceptionHandler.java
- `InquiryMethodLogger` (@interface) aop/annotation/InquiryMethodLogger.java
- `InquiryPerformanceMonitor` (@interface) aop/annotation/InquiryPerformanceMonitor.java

### 다른 도메인 참조 (import·주입 기준)

(없음)

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

- `ProductInquiry` @Entity @Table(name = "user_access_log", indexes = { @Index(name = "idx_access_time", columnList = "ACCESS_TIME"), @Index(name = "idx_ip_access_time", columnList = "ipAddress,ACCESS_TIME") })

## 5. 이벤트·메시징

- 발행 kafka `user-access-log-topic`: `ProductInquiryProducer#sendAccessLog` → `ProductInquiryConsumer#consume` [inquiry], `ProductInquiryConsumerbackup#consume` [inquiry]
- 발행 kafka `user-access-log-topic`: `ProductInquiryProducerbackup#sendAccessLog` → `ProductInquiryConsumer#consume` [inquiry], `ProductInquiryConsumerbackup#consume` [inquiry]
- 구독 kafka `user-access-log-topic`: `ProductInquiryConsumer#consume` (groupId=user-access-log-group containerFactory=userAccessLogKafkaListenerContainerFactory) ← `ProductInquiryProducer#sendAccessLog` [inquiry], `ProductInquiryProducerbackup#sendAccessLog` [inquiry]
- 구독 kafka `user-access-log-topic`: `ProductInquiryConsumerbackup#consume` (groupId=user-access-log-group containerFactory=userAccessLogKafkaListenerContainerFactory) ← `ProductInquiryProducer#sendAccessLog` [inquiry], `ProductInquiryProducerbackup#sendAccessLog` [inquiry]

## 6. AOP

- `InquiryExceptionAspect#handleAccessLogException` @Around("@annotation(exceptionHandler)") · 바인딩: `@InquiryExceptionHandler`
- `InquiryMethodLoggingAspect#logMethodEntry` @Before("@annotation(methodLogger)") · 바인딩: `@InquiryMethodLogger`
- `InquiryMethodLoggingAspect#logMethodExit` @AfterReturning(pointcut = "@annotation(methodLogger)", returning = "returnValue") · 바인딩: `@InquiryMethodLogger`
- `InquiryMethodLoggingAspect#logMethodException` @AfterThrowing(pointcut = "@annotation(methodLogger)", throwing = "exception") · 바인딩: `@InquiryMethodLogger`
- `InquiryPerformanceAspect#monitorPerformance` @Around("@annotation(monitor)") · 바인딩: `@InquiryPerformanceMonitor`

어노테이션 적용 지점(메서드에 붙은 곳):

- `@InquiryExceptionHandler`: `ProductInquiryCommandService#createAccessLog`, `ProductInquiryCommandService#validateLogDto`, `ProductInquiryCommandService#enrichWithGeoData`, `ProductInquiryCommandService#mapToEntity`, `GeoIPService#getLocation`, `ProductInquiryConsumer#consume`, `ProductInquiryConsumer#buildUserAccessLog`, `ProductInquiryProducer#sendAccessLog`, `ProductInquiryQueryService#getTodayAccessCount`, `ProductInquiryQueryService#getTodayUniqueVisitorCount`, `ProductInquiryQueryService#getRecent7DaysAccessCount`
- `@InquiryMethodLogger`: `ProductInquiryCommandService#createAccessLog`, `ProductInquiryCommandService#validateLogDto`, `ProductInquiryCommandService#enrichWithGeoData`, `ProductInquiryCommandService#mapToEntity`, `GeoIPService#getLocation`, `ProductInquiryConsumer#consume`, `ProductInquiryConsumer#buildUserAccessLog`, `ProductInquiryProducer#sendAccessLog`, `ProductInquiryQueryService#getTodayAccessCount`, `ProductInquiryQueryService#getTodayUniqueVisitorCount`, `ProductInquiryQueryService#getRecent7DaysAccessCount`
- `@InquiryPerformanceMonitor`: `ProductInquiryCommandService#createAccessLog`, `ProductInquiryCommandService#enrichWithGeoData`, `GeoIPService#getLocation`, `ProductInquiryConsumer#consume`, `ProductInquiryProducer#sendAccessLog`, `ProductInquiryQueryService#getTodayAccessCount`, `ProductInquiryQueryService#getTodayUniqueVisitorCount`, `ProductInquiryQueryService#getRecent7DaysAccessCount`

