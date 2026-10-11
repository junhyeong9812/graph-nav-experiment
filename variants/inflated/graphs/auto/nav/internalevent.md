# internalevent 도메인

기준 경로: `src/main/java/com/fream/back/domain/internalevent/` · 파일 22개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/internal/events` | `InternalEventCommandController#createEvent` | `InternalUserQueryService#checkAdminRole`, `InternalEventCommandService#createEvent` |
| PATCH | `/internal/events/{eventId}` | `InternalEventCommandController#updateEvent` | `InternalUserQueryService#checkAdminRole`, `InternalEventCommandService#updateEvent` |
| DELETE | `/internal/events/{eventId}` | `InternalEventCommandController#deleteEvent` | `InternalUserQueryService#checkAdminRole`, `InternalEventCommandService#deleteEvent` |
| PATCH | `/internal/events/{eventId}/status` | `InternalEventCommandController#updateEventStatus` | `InternalUserQueryService#checkAdminRole`, `InternalEventCommandService#updateEventStatus` |
| GET | `/internal/events/page` | `InternalEventQueryController#getAllEventsPaging` | `InternalEventQueryService#findAllEventsWithPaging` |
| GET | `/internal/events/search` | `InternalEventQueryController#searchEvents` | `InternalEventQueryService#searchEvents` |
| GET | `/internal/events/active` | `InternalEventQueryController#getActiveEvents` | `InternalEventQueryService#findActiveEventsWithPaging` |
| GET | `/internal/events/status/{status}` | `InternalEventQueryController#getEventsByStatus` | `InternalEventQueryService#findEventsByStatus` |
| GET | `/internal/events` | `InternalEventQueryController#getAllEvents` | `InternalEventQueryService#findAllEvents` |
| GET | `/internal/events/brands/{brandId}` | `InternalEventQueryController#getEventsByBrand` | `InternalEventQueryService#findEventsByBrandId` |
| GET | `/internal/events/{eventId}` | `InternalEventQueryController#getEventDetail` | `InternalEventQueryService#findEventById` |
| GET | `/internal/events/{eventId}/images/{fileName}` | `InternalEventQueryController#getEventImage` | `FileUtils#existsFile` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `InternalEventBusinessRuleAspect`
  - `applyBusinessRules` → this#validateBusinessRule, this#postProcessBusinessRules, this#handleRuleViolation
  - `validateBusinessRule` → this#checkEventOverlap, this#checkBrandEventLimit, this#validateEventDates, this#validateStatusTransition, this#checkEditPermission, this#checkDeletePermission, this#checkFileSizeLimit, this#checkCreationFrequency, this#checkBrandStatus, this#checkAutoArchiveRule
  - `checkEventOverlap` → this#findOverlappingEvents
  - `checkBrandEventLimit` → this#extractBrandId, this#updateBrandActiveEvents
  - `validateStatusTransition` → InternalEventRepository#findById~
  - `checkEditPermission` → this#extractEventId, InternalEventRepository#findById~
  - `checkDeletePermission` → this#extractEventId, InternalEventRepository#findById~
  - `checkCreationFrequency` → this#getUserKey
  - `checkBrandStatus` → this#extractBrandId, InternalBrandQueryService#findById
  - `checkAutoArchiveRule` → this#extractEventId, InternalEventRepository#findById~, this#archiveEvent
  - `postProcessBusinessRules` → this#handleAutoStatusChange, this#handleBrandDeactivation, this#handleAutoArchiving
  - `handleBrandDeactivation` → this#extractBrandId, this#markBrandEventsInactive, this#endBrandEventsImmediately, this#transferEventOwnership
  - `handleAutoArchiving` → this#extractEventStatus, this#extractEventId, InternalEventRepository#findById~, this#archiveEvent
  - `archiveEvent` → this#getUserKey
  - `findOverlappingEvents` → InternalEventRepository#findByBrandIdOrderByStartDateDesc
  - `updateBrandActiveEvents` → InternalEventRepository#findByBrandIdOrderByStartDateDesc
- `InternalEventCachingAspect`
  - `manageCaching` → this#generateCacheKey, this#determineCacheName, this#evaluateCacheCondition, this#isExpired, this#executeAndCache, this#logCacheMetrics, this#scheduleWarmUp
  - `generateCacheKey` → this#appendEventId, this#appendBrandId, this#appendUserId, this#appendStatus, this#appendTimeSegment, this#appendCompositeKey
  - `determineCacheName` → this#isAdminUser
  - `scheduleWarmUp` → this#warmUpFrequentData
  - `appendCompositeKey` → this#appendUserId, this#appendEventId, this#appendTimeSegment
- `InternalEventCommandService`
  - `createEvent` → this#validateEventDates, InternalBrandQueryService#findById, InternalEventRepository#save~, FileUtils#saveFile, InternalSimpleImageCommandService#createSimpleImages
  - `updateEvent` → this#validateEventDates, InternalEventRepository#findById~, this#handleSimpleImagesUpdate, this#handleThumbnailUpdate, InternalEventRepository#save~
  - `updateEventStatus` → InternalEventRepository#findById~, InternalEventRepository#save~
  - `handleSimpleImagesUpdate` → InternalSimpleImageQueryService#findByEventId, FileUtils#deleteFile, InternalSimpleImageRepository#delete~, InternalSimpleImageCommandService#createSimpleImages
  - `handleThumbnailUpdate` → FileUtils#deleteFile, FileUtils#saveFile
  - `deleteEvent` → InternalEventRepository#findById~, FileUtils#deleteFile, InternalSimpleImageQueryService#findByEventId, InternalEventRepository#delete~
- `InternalEventFileManagementAspect`
  - `manageFiles` → this#preprocessFiles, this#postprocessFiles, this#logFileMetrics, this#rollbackFileOperations
  - `preprocessFiles` → this#validateFiles, this#resizeImages, this#convertToWebP, this#scanForVirus, this#createBackup
  - `validateFiles` → this#validateSingleFile
  - `resizeImages` → this#isImageFile, this#resizeImage
  - `postprocessFiles` → this#cleanupTempFiles
- `InternalEventMonitoringAspect`
  - `monitorEvent` → this#shouldCollectMetric, this#extractUserEmail, this#trackUserActivity, this#trackEventLifecycle, this#recordSuccessMetrics, this#collectBrandStatistics, this#monitorFileOperations, this#checkAndAlertStatusChange, this#checkThresholdsAndAlert, this#scheduleMetricsReport
  - `trackEventLifecycle` → this#extractEventId, this#extractEventStatus, this#extractUserEmail
  - `collectBrandStatistics` → this#extractBrandId, this#calculateTotalFileSize
  - `checkAndAlertStatusChange` → this#extractEventId, this#extractEventStatus, this#sendStatusChangeNotification
  - `checkThresholdsAndAlert` → this#calculateTotalFileSize, this#checkCreationFrequency, this#calculateCacheHitRate
  - `generateMetricsReport` → this#calculateCacheHitRate
- `InternalEventQueryService`
  - `findEventById` → InternalEventRepository#findById~, InternalSimpleImageQueryService#findByEventId, FileUtils#getFileUrl
  - `findActiveEvents` → InternalEventRepository#findActiveEvents
  - `findEventsByBrandId` → InternalEventRepository#findByBrandIdOrderByStartDateDesc
  - `searchEvents` → InternalEventRepository#searchEvents
  - `findEventsByStatus` → InternalEventRepository#findByStartDateAfterOrderByStartDateAsc, InternalEventRepository#findActiveEvents, InternalEventRepository#findByEndDateBeforeOrderByEndDateDesc
  - `findAllEventsWithPaging` → InternalEventRepository#findAllWithBrandPaging
  - `findActiveEventsWithPaging` → InternalEventRepository#findActiveEventsWithBrandPaging
  - `findAllEvents` → InternalEventRepository#findAllWithBrand
  - `convertToEventListDto` → FileUtils#getFileUrl
- `InternalEventRepositoryImpl`
  - `searchEvents` → this#keywordContains, this#brandIdEquals, this#isActiveEquals, this#startDateAfter, this#endDateBefore, this#statusEquals, this#getOrderSpecifier
- `InternalEventSearchRepository`
  - `searchEvents` → this#keywordContains, this#brandIdEquals, this#isActiveEquals, this#startDateAfter, this#endDateBefore, this#getOrderSpecifier
- `InternalEventStatusScheduler`
  - `checkEventStatus` → this#checkUpcomingToActiveEvents, this#checkActiveToEndedEvents
  - `checkUpcomingToActiveEvents` → InternalEventRepository#findByStartDateBeforeAndEndDateAfter
  - `checkActiveToEndedEvents` → InternalEventRepository#findByEndDateLessThanEqual
- `InternalEventValidationAspect`
  - `validateEvent` → this#performValidation, this#validateAdminPermission, this#handleValidationFailure
  - `performValidation` → this#validateDateRange, this#validateBusinessRules, this#validateBrand, this#validateFiles, this#validateStatusTransition, this#checkDuplicates, this#checkCapacity, this#validatePermissions
  - `validateBusinessRules` → this#extractBrandId, InternalEventRepository#findByBrandIdOrderByStartDateDesc
  - `validateBrand` → this#extractBrandId, InternalBrandQueryService#findById
  - `validateFiles` → this#validateFileExtension
  - `validateStatusTransition` → InternalEventRepository#findById~
  - `checkDuplicates` → this#extractTitle
- `InternalSimpleImageCommandService`
  - `createSimpleImages` → FileUtils#saveFile, InternalSimpleImageRepository#save~
  - `deleteAllByEvent` → InternalSimpleImageRepository#deleteByEventId
  - `deleteSimpleImage` → InternalSimpleImageRepository#findById~, FileUtils#deleteFile, InternalSimpleImageRepository#delete~
- `InternalSimpleImageQueryService`
  - `findByEventId` → InternalSimpleImageRepository#findByEventId
  - `findById` → InternalSimpleImageRepository#findById~

## 3. 클래스 목록 (역할별)

### controller (2)

- `InternalEventCommandController` controller/command/InternalEventCommandController.java · public: createEvent, updateEvent, deleteEvent, updateEventStatus · 주입: InternalEventCommandService, InternalUserQueryService
- `InternalEventQueryController` controller/query/InternalEventQueryController.java · public: getAllEventsPaging, searchEvents, getActiveEvents, getEventsByStatus, getAllEvents, getEventsByBrand, getEventDetail, getEventImage · 주입: InternalEventQueryService, FileUtils

### service-command (2)

- `InternalEventCommandService` service/command/InternalEventCommandService.java · public: createEvent, updateEvent, updateEventStatus, deleteEvent · 주입: InternalEventRepository, InternalSimpleImageCommandService, InternalSimpleImageQueryService, InternalSimpleImageRepository, InternalBrandQueryService, FileUtils
- `InternalSimpleImageCommandService` service/command/InternalSimpleImageCommandService.java · public: createSimpleImages, deleteAllByEvent, deleteSimpleImage · 주입: InternalSimpleImageRepository, FileUtils

### service-query (2)

- `InternalEventQueryService` service/query/InternalEventQueryService.java · public: findEventById, findActiveEvents, findEventsByBrandId, searchEvents, findEventsByStatus, findAllEventsWithPaging, findActiveEventsWithPaging, findAllEvents · 주입: InternalEventRepository, InternalSimpleImageQueryService, FileUtils
- `InternalSimpleImageQueryService` service/query/InternalSimpleImageQueryService.java · public: findByEventId, findById · 주입: InternalSimpleImageRepository

### repository (3)

- `InternalEventRepository` (interface) repository/InternalEventRepository.java · extends/implements JpaRepository, InternalEventRepositoryCustom · public: findByBrandIdOrderByStartDateDesc, findActiveEvents, findByStartDateBeforeAndEndDateAfter, findByEndDateLessThanEqual, findByStartDateAfterOrderByStartDateAsc, findByEndDateBeforeOrderByEndDateDesc, findAllWithBrand, findAllWithBrandPaging, findByIdWithBrand, findActiveEventsWithBrandPaging, findByBrandIdWithBrand, findByBrandIdWithBrandPaging, findByIdWithBrandAndSimpleImages
- `InternalEventSearchRepository` repository/InternalEventSearchRepository.java · public: searchEvents · 주입: JPAQueryFactory
- `InternalSimpleImageRepository` (interface) repository/InternalSimpleImageRepository.java · extends/implements JpaRepository · public: deleteByEventId, findByEventId

### custom (2)

- `InternalEventRepositoryCustom` (interface) repository/InternalEventRepositoryCustom.java · public: searchEvents
- `InternalEventRepositoryImpl` repository/InternalEventRepositoryImpl.java · extends/implements InternalEventRepositoryCustom · public: searchEvents · 주입: JPAQueryFactory

### aop (5)

- `InternalEventBusinessRuleAspect` aop/InternalEventBusinessRuleAspect.java · public: applyBusinessRules · 주입: InternalEventRepository, InternalBrandQueryService
- `InternalEventCachingAspect` aop/InternalEventCachingAspect.java · public: manageCaching, invalidateCacheOnStatusChange, invalidateBrandCache, clearAllCaches, generateCacheReport · 주입: CacheManager
- `InternalEventFileManagementAspect` aop/InternalEventFileManagementAspect.java · public: manageFiles · 주입: FileUtils
- `InternalEventMonitoringAspect` aop/InternalEventMonitoringAspect.java · public: monitorEvent
- `InternalEventValidationAspect` aop/InternalEventValidationAspect.java · public: validateEvent · 주입: InternalEventRepository, InternalBrandQueryService

### other (6)

- `InternalEventBusinessRule` (@interface) aop/annotation/InternalEventBusinessRule.java
- `InternalEventCaching` (@interface) aop/annotation/InternalEventCaching.java
- `InternalEventFileManagement` (@interface) aop/annotation/InternalEventFileManagement.java
- `InternalEventMonitoring` (@interface) aop/annotation/InternalEventMonitoring.java
- `InternalEventValidation` (@interface) aop/annotation/InternalEventValidation.java
- `InternalEventStatusScheduler` scheduler/InternalEventStatusScheduler.java · public: checkEventStatus · 주입: InternalEventRepository

### 다른 도메인 참조 (import·주입 기준)

- **event**: `Event` ← InternalEventBusinessRuleAspect, InternalEventCommandService, InternalEventMonitoringAspect, InternalEventQueryService, InternalEventRepository, InternalEventRepositoryCustom, InternalEventRepositoryImpl, InternalEventSearchRepository, InternalEventStatusScheduler, InternalEventValidationAspect, InternalSimpleImageCommandService; `EventStatus` ← InternalEventBusinessRuleAspect, InternalEventCachingAspect, InternalEventCommandController, InternalEventCommandService, InternalEventMonitoringAspect, InternalEventQueryController, InternalEventQueryService, InternalEventRepositoryImpl, InternalEventValidationAspect; `SimpleImage` ← InternalEventCommandService, InternalEventQueryService, InternalSimpleImageCommandService, InternalSimpleImageQueryService, InternalSimpleImageRepository
- **internalproduct**: `InternalBrandQueryService` ← InternalEventBusinessRuleAspect, InternalEventCommandService, InternalEventValidationAspect
- **internaluser**: `InternalUserQueryService` ← InternalEventCommandController
- **product**: `Brand` ← InternalEventBusinessRuleAspect, InternalEventCommandService, InternalEventValidationAspect

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

- `InternalEventBusinessRuleAspect#applyBusinessRules` @Around("@annotation(businessRule)") · 바인딩: `@InternalEventBusinessRule`
- `InternalEventCachingAspect#manageCaching` @Around("@annotation(eventCaching)") · 바인딩: `@InternalEventCaching`
- `InternalEventFileManagementAspect#manageFiles` @Around("@annotation(fileManagement)") · 바인딩: `@InternalEventFileManagement`
- `InternalEventMonitoringAspect#monitorEvent` @Around("@annotation(eventMonitoring)") · 바인딩: `@InternalEventMonitoring`
- `InternalEventValidationAspect#validateEvent` @Around("@annotation(eventValidation)") · 바인딩: `@InternalEventValidation`

어노테이션 적용 지점(메서드에 붙은 곳):

- `@InternalEventBusinessRule`: (없음)
- `@InternalEventCaching`: (없음)
- `@InternalEventFileManagement`: (없음)
- `@InternalEventMonitoring`: (없음)
- `@InternalEventValidation`: (없음)

