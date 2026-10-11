# settlementevent 도메인

기준 경로: `src/main/java/com/fream/back/domain/settlementevent/` · 파일 22개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/settlement/events` | `SettlementEventCommandController#createEvent` | `SettlementUserQueryService#checkAdminRole`, `SettlementEventCommandService#createEvent` |
| PATCH | `/settlement/events/{eventId}` | `SettlementEventCommandController#updateEvent` | `SettlementUserQueryService#checkAdminRole`, `SettlementEventCommandService#updateEvent` |
| DELETE | `/settlement/events/{eventId}` | `SettlementEventCommandController#deleteEvent` | `SettlementUserQueryService#checkAdminRole`, `SettlementEventCommandService#deleteEvent` |
| PATCH | `/settlement/events/{eventId}/status` | `SettlementEventCommandController#updateEventStatus` | `SettlementUserQueryService#checkAdminRole`, `SettlementEventCommandService#updateEventStatus` |
| GET | `/settlement/events/page` | `SettlementEventQueryController#getAllEventsPaging` | `SettlementEventQueryService#findAllEventsWithPaging` |
| GET | `/settlement/events/search` | `SettlementEventQueryController#searchEvents` | `SettlementEventQueryService#searchEvents` |
| GET | `/settlement/events/active` | `SettlementEventQueryController#getActiveEvents` | `SettlementEventQueryService#findActiveEventsWithPaging` |
| GET | `/settlement/events/status/{status}` | `SettlementEventQueryController#getEventsByStatus` | `SettlementEventQueryService#findEventsByStatus` |
| GET | `/settlement/events` | `SettlementEventQueryController#getAllEvents` | `SettlementEventQueryService#findAllEvents` |
| GET | `/settlement/events/brands/{brandId}` | `SettlementEventQueryController#getEventsByBrand` | `SettlementEventQueryService#findEventsByBrandId` |
| GET | `/settlement/events/{eventId}` | `SettlementEventQueryController#getEventDetail` | `SettlementEventQueryService#findEventById` |
| GET | `/settlement/events/{eventId}/images/{fileName}` | `SettlementEventQueryController#getEventImage` | `FileUtils#existsFile` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `SettlementEventBusinessRuleAspect`
  - `applyBusinessRules` → this#validateBusinessRule, this#postProcessBusinessRules, this#handleRuleViolation
  - `validateBusinessRule` → this#checkEventOverlap, this#checkBrandEventLimit, this#validateEventDates, this#validateStatusTransition, this#checkEditPermission, this#checkDeletePermission, this#checkFileSizeLimit, this#checkCreationFrequency, this#checkBrandStatus, this#checkAutoArchiveRule
  - `checkEventOverlap` → this#findOverlappingEvents
  - `checkBrandEventLimit` → this#extractBrandId, this#updateBrandActiveEvents
  - `validateStatusTransition` → SettlementEventRepository#findById~
  - `checkEditPermission` → this#extractEventId, SettlementEventRepository#findById~
  - `checkDeletePermission` → this#extractEventId, SettlementEventRepository#findById~
  - `checkCreationFrequency` → this#getUserKey
  - `checkBrandStatus` → this#extractBrandId, SettlementBrandQueryService#findById
  - `checkAutoArchiveRule` → this#extractEventId, SettlementEventRepository#findById~, this#archiveEvent
  - `postProcessBusinessRules` → this#handleAutoStatusChange, this#handleBrandDeactivation, this#handleAutoArchiving
  - `handleBrandDeactivation` → this#extractBrandId, this#markBrandEventsInactive, this#endBrandEventsImmediately, this#transferEventOwnership
  - `handleAutoArchiving` → this#extractEventStatus, this#extractEventId, SettlementEventRepository#findById~, this#archiveEvent
  - `archiveEvent` → this#getUserKey
  - `findOverlappingEvents` → SettlementEventRepository#findByBrandIdOrderByStartDateDesc
  - `updateBrandActiveEvents` → SettlementEventRepository#findByBrandIdOrderByStartDateDesc
- `SettlementEventCachingAspect`
  - `manageCaching` → this#generateCacheKey, this#determineCacheName, this#evaluateCacheCondition, this#isExpired, this#executeAndCache, this#logCacheMetrics, this#scheduleWarmUp
  - `generateCacheKey` → this#appendEventId, this#appendBrandId, this#appendUserId, this#appendStatus, this#appendTimeSegment, this#appendCompositeKey
  - `determineCacheName` → this#isAdminUser
  - `scheduleWarmUp` → this#warmUpFrequentData
  - `appendCompositeKey` → this#appendUserId, this#appendEventId, this#appendTimeSegment
- `SettlementEventCommandService`
  - `createEvent` → this#validateEventDates, SettlementBrandQueryService#findById, SettlementEventRepository#save~, FileUtils#saveFile, SettlementSimpleImageCommandService#createSimpleImages
  - `updateEvent` → this#validateEventDates, SettlementEventRepository#findById~, this#handleSimpleImagesUpdate, this#handleThumbnailUpdate, SettlementEventRepository#save~
  - `updateEventStatus` → SettlementEventRepository#findById~, SettlementEventRepository#save~
  - `handleSimpleImagesUpdate` → SettlementSimpleImageQueryService#findByEventId, FileUtils#deleteFile, SettlementSimpleImageRepository#delete~, SettlementSimpleImageCommandService#createSimpleImages
  - `handleThumbnailUpdate` → FileUtils#deleteFile, FileUtils#saveFile
  - `deleteEvent` → SettlementEventRepository#findById~, FileUtils#deleteFile, SettlementSimpleImageQueryService#findByEventId, SettlementEventRepository#delete~
- `SettlementEventFileManagementAspect`
  - `manageFiles` → this#preprocessFiles, this#postprocessFiles, this#logFileMetrics, this#rollbackFileOperations
  - `preprocessFiles` → this#validateFiles, this#resizeImages, this#convertToWebP, this#scanForVirus, this#createBackup
  - `validateFiles` → this#validateSingleFile
  - `resizeImages` → this#isImageFile, this#resizeImage
  - `postprocessFiles` → this#cleanupTempFiles
- `SettlementEventMonitoringAspect`
  - `monitorEvent` → this#shouldCollectMetric, this#extractUserEmail, this#trackUserActivity, this#trackEventLifecycle, this#recordSuccessMetrics, this#collectBrandStatistics, this#monitorFileOperations, this#checkAndAlertStatusChange, this#checkThresholdsAndAlert, this#scheduleMetricsReport
  - `trackEventLifecycle` → this#extractEventId, this#extractEventStatus, this#extractUserEmail
  - `collectBrandStatistics` → this#extractBrandId, this#calculateTotalFileSize
  - `checkAndAlertStatusChange` → this#extractEventId, this#extractEventStatus, this#sendStatusChangeNotification
  - `checkThresholdsAndAlert` → this#calculateTotalFileSize, this#checkCreationFrequency, this#calculateCacheHitRate
  - `generateMetricsReport` → this#calculateCacheHitRate
- `SettlementEventQueryService`
  - `findEventById` → SettlementEventRepository#findById~, SettlementSimpleImageQueryService#findByEventId, FileUtils#getFileUrl
  - `findActiveEvents` → SettlementEventRepository#findActiveEvents
  - `findEventsByBrandId` → SettlementEventRepository#findByBrandIdOrderByStartDateDesc
  - `searchEvents` → SettlementEventRepository#searchEvents
  - `findEventsByStatus` → SettlementEventRepository#findByStartDateAfterOrderByStartDateAsc, SettlementEventRepository#findActiveEvents, SettlementEventRepository#findByEndDateBeforeOrderByEndDateDesc
  - `findAllEventsWithPaging` → SettlementEventRepository#findAllWithBrandPaging
  - `findActiveEventsWithPaging` → SettlementEventRepository#findActiveEventsWithBrandPaging
  - `findAllEvents` → SettlementEventRepository#findAllWithBrand
  - `convertToEventListDto` → FileUtils#getFileUrl
- `SettlementEventRepositoryImpl`
  - `searchEvents` → this#keywordContains, this#brandIdEquals, this#isActiveEquals, this#startDateAfter, this#endDateBefore, this#statusEquals, this#getOrderSpecifier
- `SettlementEventSearchRepository`
  - `searchEvents` → this#keywordContains, this#brandIdEquals, this#isActiveEquals, this#startDateAfter, this#endDateBefore, this#getOrderSpecifier
- `SettlementEventStatusScheduler`
  - `checkEventStatus` → this#checkUpcomingToActiveEvents, this#checkActiveToEndedEvents
  - `checkUpcomingToActiveEvents` → SettlementEventRepository#findByStartDateBeforeAndEndDateAfter
  - `checkActiveToEndedEvents` → SettlementEventRepository#findByEndDateLessThanEqual
- `SettlementEventValidationAspect`
  - `validateEvent` → this#performValidation, this#validateAdminPermission, this#handleValidationFailure
  - `performValidation` → this#validateDateRange, this#validateBusinessRules, this#validateBrand, this#validateFiles, this#validateStatusTransition, this#checkDuplicates, this#checkCapacity, this#validatePermissions
  - `validateBusinessRules` → this#extractBrandId, SettlementEventRepository#findByBrandIdOrderByStartDateDesc
  - `validateBrand` → this#extractBrandId, SettlementBrandQueryService#findById
  - `validateFiles` → this#validateFileExtension
  - `validateStatusTransition` → SettlementEventRepository#findById~
  - `checkDuplicates` → this#extractTitle
- `SettlementSimpleImageCommandService`
  - `createSimpleImages` → FileUtils#saveFile, SettlementSimpleImageRepository#save~
  - `deleteAllByEvent` → SettlementSimpleImageRepository#deleteByEventId
  - `deleteSimpleImage` → SettlementSimpleImageRepository#findById~, FileUtils#deleteFile, SettlementSimpleImageRepository#delete~
- `SettlementSimpleImageQueryService`
  - `findByEventId` → SettlementSimpleImageRepository#findByEventId
  - `findById` → SettlementSimpleImageRepository#findById~

## 3. 클래스 목록 (역할별)

### controller (2)

- `SettlementEventCommandController` controller/command/SettlementEventCommandController.java · public: createEvent, updateEvent, deleteEvent, updateEventStatus · 주입: SettlementEventCommandService, SettlementUserQueryService
- `SettlementEventQueryController` controller/query/SettlementEventQueryController.java · public: getAllEventsPaging, searchEvents, getActiveEvents, getEventsByStatus, getAllEvents, getEventsByBrand, getEventDetail, getEventImage · 주입: SettlementEventQueryService, FileUtils

### service-command (2)

- `SettlementEventCommandService` service/command/SettlementEventCommandService.java · public: createEvent, updateEvent, updateEventStatus, deleteEvent · 주입: SettlementEventRepository, SettlementSimpleImageCommandService, SettlementSimpleImageQueryService, SettlementSimpleImageRepository, SettlementBrandQueryService, FileUtils
- `SettlementSimpleImageCommandService` service/command/SettlementSimpleImageCommandService.java · public: createSimpleImages, deleteAllByEvent, deleteSimpleImage · 주입: SettlementSimpleImageRepository, FileUtils

### service-query (2)

- `SettlementEventQueryService` service/query/SettlementEventQueryService.java · public: findEventById, findActiveEvents, findEventsByBrandId, searchEvents, findEventsByStatus, findAllEventsWithPaging, findActiveEventsWithPaging, findAllEvents · 주입: SettlementEventRepository, SettlementSimpleImageQueryService, FileUtils
- `SettlementSimpleImageQueryService` service/query/SettlementSimpleImageQueryService.java · public: findByEventId, findById · 주입: SettlementSimpleImageRepository

### repository (3)

- `SettlementEventRepository` (interface) repository/SettlementEventRepository.java · extends/implements JpaRepository, SettlementEventRepositoryCustom · public: findByBrandIdOrderByStartDateDesc, findActiveEvents, findByStartDateBeforeAndEndDateAfter, findByEndDateLessThanEqual, findByStartDateAfterOrderByStartDateAsc, findByEndDateBeforeOrderByEndDateDesc, findAllWithBrand, findAllWithBrandPaging, findByIdWithBrand, findActiveEventsWithBrandPaging, findByBrandIdWithBrand, findByBrandIdWithBrandPaging, findByIdWithBrandAndSimpleImages
- `SettlementEventSearchRepository` repository/SettlementEventSearchRepository.java · public: searchEvents · 주입: JPAQueryFactory
- `SettlementSimpleImageRepository` (interface) repository/SettlementSimpleImageRepository.java · extends/implements JpaRepository · public: deleteByEventId, findByEventId

### custom (2)

- `SettlementEventRepositoryCustom` (interface) repository/SettlementEventRepositoryCustom.java · public: searchEvents
- `SettlementEventRepositoryImpl` repository/SettlementEventRepositoryImpl.java · extends/implements SettlementEventRepositoryCustom · public: searchEvents · 주입: JPAQueryFactory

### aop (5)

- `SettlementEventBusinessRuleAspect` aop/SettlementEventBusinessRuleAspect.java · public: applyBusinessRules · 주입: SettlementEventRepository, SettlementBrandQueryService
- `SettlementEventCachingAspect` aop/SettlementEventCachingAspect.java · public: manageCaching, invalidateCacheOnStatusChange, invalidateBrandCache, clearAllCaches, generateCacheReport · 주입: CacheManager
- `SettlementEventFileManagementAspect` aop/SettlementEventFileManagementAspect.java · public: manageFiles · 주입: FileUtils
- `SettlementEventMonitoringAspect` aop/SettlementEventMonitoringAspect.java · public: monitorEvent
- `SettlementEventValidationAspect` aop/SettlementEventValidationAspect.java · public: validateEvent · 주입: SettlementEventRepository, SettlementBrandQueryService

### other (6)

- `SettlementEventBusinessRule` (@interface) aop/annotation/SettlementEventBusinessRule.java
- `SettlementEventCaching` (@interface) aop/annotation/SettlementEventCaching.java
- `SettlementEventFileManagement` (@interface) aop/annotation/SettlementEventFileManagement.java
- `SettlementEventMonitoring` (@interface) aop/annotation/SettlementEventMonitoring.java
- `SettlementEventValidation` (@interface) aop/annotation/SettlementEventValidation.java
- `SettlementEventStatusScheduler` scheduler/SettlementEventStatusScheduler.java · public: checkEventStatus · 주입: SettlementEventRepository

### 다른 도메인 참조 (import·주입 기준)

- **event**: `Event` ← SettlementEventBusinessRuleAspect, SettlementEventCommandService, SettlementEventMonitoringAspect, SettlementEventQueryService, SettlementEventRepository, SettlementEventRepositoryCustom, SettlementEventRepositoryImpl, SettlementEventSearchRepository, SettlementEventStatusScheduler, SettlementEventValidationAspect, SettlementSimpleImageCommandService; `EventStatus` ← SettlementEventBusinessRuleAspect, SettlementEventCachingAspect, SettlementEventCommandController, SettlementEventCommandService, SettlementEventMonitoringAspect, SettlementEventQueryController, SettlementEventQueryService, SettlementEventRepositoryImpl, SettlementEventValidationAspect; `SimpleImage` ← SettlementEventCommandService, SettlementEventQueryService, SettlementSimpleImageCommandService, SettlementSimpleImageQueryService, SettlementSimpleImageRepository
- **product**: `Brand` ← SettlementEventBusinessRuleAspect, SettlementEventCommandService, SettlementEventValidationAspect
- **settlementproduct**: `SettlementBrandQueryService` ← SettlementEventBusinessRuleAspect, SettlementEventCommandService, SettlementEventValidationAspect
- **settlementuser**: `SettlementUserQueryService` ← SettlementEventCommandController

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

- `SettlementEventBusinessRuleAspect#applyBusinessRules` @Around("@annotation(businessRule)") · 바인딩: `@SettlementEventBusinessRule`
- `SettlementEventCachingAspect#manageCaching` @Around("@annotation(eventCaching)") · 바인딩: `@SettlementEventCaching`
- `SettlementEventFileManagementAspect#manageFiles` @Around("@annotation(fileManagement)") · 바인딩: `@SettlementEventFileManagement`
- `SettlementEventMonitoringAspect#monitorEvent` @Around("@annotation(eventMonitoring)") · 바인딩: `@SettlementEventMonitoring`
- `SettlementEventValidationAspect#validateEvent` @Around("@annotation(eventValidation)") · 바인딩: `@SettlementEventValidation`

어노테이션 적용 지점(메서드에 붙은 곳):

- `@SettlementEventBusinessRule`: (없음)
- `@SettlementEventCaching`: (없음)
- `@SettlementEventFileManagement`: (없음)
- `@SettlementEventMonitoring`: (없음)
- `@SettlementEventValidation`: (없음)

