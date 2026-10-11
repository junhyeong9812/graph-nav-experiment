# adminevent 도메인

기준 경로: `src/main/java/com/fream/back/domain/adminevent/` · 파일 22개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/admin/events` | `AdminEventCommandController#createEvent` | `AdminUserQueryService#checkAdminRole`, `AdminEventCommandService#createEvent` |
| PATCH | `/admin/events/{eventId}` | `AdminEventCommandController#updateEvent` | `AdminUserQueryService#checkAdminRole`, `AdminEventCommandService#updateEvent` |
| DELETE | `/admin/events/{eventId}` | `AdminEventCommandController#deleteEvent` | `AdminUserQueryService#checkAdminRole`, `AdminEventCommandService#deleteEvent` |
| PATCH | `/admin/events/{eventId}/status` | `AdminEventCommandController#updateEventStatus` | `AdminUserQueryService#checkAdminRole`, `AdminEventCommandService#updateEventStatus` |
| GET | `/admin/events/page` | `AdminEventQueryController#getAllEventsPaging` | `AdminEventQueryService#findAllEventsWithPaging` |
| GET | `/admin/events/search` | `AdminEventQueryController#searchEvents` | `AdminEventQueryService#searchEvents` |
| GET | `/admin/events/active` | `AdminEventQueryController#getActiveEvents` | `AdminEventQueryService#findActiveEventsWithPaging` |
| GET | `/admin/events/status/{status}` | `AdminEventQueryController#getEventsByStatus` | `AdminEventQueryService#findEventsByStatus` |
| GET | `/admin/events` | `AdminEventQueryController#getAllEvents` | `AdminEventQueryService#findAllEvents` |
| GET | `/admin/events/brands/{brandId}` | `AdminEventQueryController#getEventsByBrand` | `AdminEventQueryService#findEventsByBrandId` |
| GET | `/admin/events/{eventId}` | `AdminEventQueryController#getEventDetail` | `AdminEventQueryService#findEventById` |
| GET | `/admin/events/{eventId}/images/{fileName}` | `AdminEventQueryController#getEventImage` | `FileUtils#existsFile` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `AdminEventBusinessRuleAspect`
  - `applyBusinessRules` → this#validateBusinessRule, this#postProcessBusinessRules, this#handleRuleViolation
  - `validateBusinessRule` → this#checkEventOverlap, this#checkBrandEventLimit, this#validateEventDates, this#validateStatusTransition, this#checkEditPermission, this#checkDeletePermission, this#checkFileSizeLimit, this#checkCreationFrequency, this#checkBrandStatus, this#checkAutoArchiveRule
  - `checkEventOverlap` → this#findOverlappingEvents
  - `checkBrandEventLimit` → this#extractBrandId, this#updateBrandActiveEvents
  - `validateStatusTransition` → AdminEventRepository#findById~
  - `checkEditPermission` → this#extractEventId, AdminEventRepository#findById~
  - `checkDeletePermission` → this#extractEventId, AdminEventRepository#findById~
  - `checkCreationFrequency` → this#getUserKey
  - `checkBrandStatus` → this#extractBrandId, AdminBrandQueryService#findById
  - `checkAutoArchiveRule` → this#extractEventId, AdminEventRepository#findById~, this#archiveEvent
  - `postProcessBusinessRules` → this#handleAutoStatusChange, this#handleBrandDeactivation, this#handleAutoArchiving
  - `handleBrandDeactivation` → this#extractBrandId, this#markBrandEventsInactive, this#endBrandEventsImmediately, this#transferEventOwnership
  - `handleAutoArchiving` → this#extractEventStatus, this#extractEventId, AdminEventRepository#findById~, this#archiveEvent
  - `archiveEvent` → this#getUserKey
  - `findOverlappingEvents` → AdminEventRepository#findByBrandIdOrderByStartDateDesc
  - `updateBrandActiveEvents` → AdminEventRepository#findByBrandIdOrderByStartDateDesc
- `AdminEventCachingAspect`
  - `manageCaching` → this#generateCacheKey, this#determineCacheName, this#evaluateCacheCondition, this#isExpired, this#executeAndCache, this#logCacheMetrics, this#scheduleWarmUp
  - `generateCacheKey` → this#appendEventId, this#appendBrandId, this#appendUserId, this#appendStatus, this#appendTimeSegment, this#appendCompositeKey
  - `determineCacheName` → this#isAdminUser
  - `scheduleWarmUp` → this#warmUpFrequentData
  - `appendCompositeKey` → this#appendUserId, this#appendEventId, this#appendTimeSegment
- `AdminEventCommandService`
  - `createEvent` → this#validateEventDates, AdminBrandQueryService#findById, AdminEventRepository#save~, FileUtils#saveFile, AdminSimpleImageCommandService#createSimpleImages
  - `updateEvent` → this#validateEventDates, AdminEventRepository#findById~, this#handleSimpleImagesUpdate, this#handleThumbnailUpdate, AdminEventRepository#save~
  - `updateEventStatus` → AdminEventRepository#findById~, AdminEventRepository#save~
  - `handleSimpleImagesUpdate` → AdminSimpleImageQueryService#findByEventId, FileUtils#deleteFile, AdminSimpleImageRepository#delete~, AdminSimpleImageCommandService#createSimpleImages
  - `handleThumbnailUpdate` → FileUtils#deleteFile, FileUtils#saveFile
  - `deleteEvent` → AdminEventRepository#findById~, FileUtils#deleteFile, AdminSimpleImageQueryService#findByEventId, AdminEventRepository#delete~
- `AdminEventFileManagementAspect`
  - `manageFiles` → this#preprocessFiles, this#postprocessFiles, this#logFileMetrics, this#rollbackFileOperations
  - `preprocessFiles` → this#validateFiles, this#resizeImages, this#convertToWebP, this#scanForVirus, this#createBackup
  - `validateFiles` → this#validateSingleFile
  - `resizeImages` → this#isImageFile, this#resizeImage
  - `postprocessFiles` → this#cleanupTempFiles
- `AdminEventMonitoringAspect`
  - `monitorEvent` → this#shouldCollectMetric, this#extractUserEmail, this#trackUserActivity, this#trackEventLifecycle, this#recordSuccessMetrics, this#collectBrandStatistics, this#monitorFileOperations, this#checkAndAlertStatusChange, this#checkThresholdsAndAlert, this#scheduleMetricsReport
  - `trackEventLifecycle` → this#extractEventId, this#extractEventStatus, this#extractUserEmail
  - `collectBrandStatistics` → this#extractBrandId, this#calculateTotalFileSize
  - `checkAndAlertStatusChange` → this#extractEventId, this#extractEventStatus, this#sendStatusChangeNotification
  - `checkThresholdsAndAlert` → this#calculateTotalFileSize, this#checkCreationFrequency, this#calculateCacheHitRate
  - `generateMetricsReport` → this#calculateCacheHitRate
- `AdminEventQueryService`
  - `findEventById` → AdminEventRepository#findById~, AdminSimpleImageQueryService#findByEventId, FileUtils#getFileUrl
  - `findActiveEvents` → AdminEventRepository#findActiveEvents
  - `findEventsByBrandId` → AdminEventRepository#findByBrandIdOrderByStartDateDesc
  - `searchEvents` → AdminEventRepository#searchEvents
  - `findEventsByStatus` → AdminEventRepository#findByStartDateAfterOrderByStartDateAsc, AdminEventRepository#findActiveEvents, AdminEventRepository#findByEndDateBeforeOrderByEndDateDesc
  - `findAllEventsWithPaging` → AdminEventRepository#findAllWithBrandPaging
  - `findActiveEventsWithPaging` → AdminEventRepository#findActiveEventsWithBrandPaging
  - `findAllEvents` → AdminEventRepository#findAllWithBrand
  - `convertToEventListDto` → FileUtils#getFileUrl
- `AdminEventRepositoryImpl`
  - `searchEvents` → this#keywordContains, this#brandIdEquals, this#isActiveEquals, this#startDateAfter, this#endDateBefore, this#statusEquals, this#getOrderSpecifier
- `AdminEventSearchRepository`
  - `searchEvents` → this#keywordContains, this#brandIdEquals, this#isActiveEquals, this#startDateAfter, this#endDateBefore, this#getOrderSpecifier
- `AdminEventStatusScheduler`
  - `checkEventStatus` → this#checkUpcomingToActiveEvents, this#checkActiveToEndedEvents
  - `checkUpcomingToActiveEvents` → AdminEventRepository#findByStartDateBeforeAndEndDateAfter
  - `checkActiveToEndedEvents` → AdminEventRepository#findByEndDateLessThanEqual
- `AdminEventValidationAspect`
  - `validateEvent` → this#performValidation, this#validateAdminPermission, this#handleValidationFailure
  - `performValidation` → this#validateDateRange, this#validateBusinessRules, this#validateBrand, this#validateFiles, this#validateStatusTransition, this#checkDuplicates, this#checkCapacity, this#validatePermissions
  - `validateBusinessRules` → this#extractBrandId, AdminEventRepository#findByBrandIdOrderByStartDateDesc
  - `validateBrand` → this#extractBrandId, AdminBrandQueryService#findById
  - `validateFiles` → this#validateFileExtension
  - `validateStatusTransition` → AdminEventRepository#findById~
  - `checkDuplicates` → this#extractTitle
- `AdminSimpleImageCommandService`
  - `createSimpleImages` → FileUtils#saveFile, AdminSimpleImageRepository#save~
  - `deleteAllByEvent` → AdminSimpleImageRepository#deleteByEventId
  - `deleteSimpleImage` → AdminSimpleImageRepository#findById~, FileUtils#deleteFile, AdminSimpleImageRepository#delete~
- `AdminSimpleImageQueryService`
  - `findByEventId` → AdminSimpleImageRepository#findByEventId
  - `findById` → AdminSimpleImageRepository#findById~

## 3. 클래스 목록 (역할별)

### controller (2)

- `AdminEventCommandController` controller/command/AdminEventCommandController.java · public: createEvent, updateEvent, deleteEvent, updateEventStatus · 주입: AdminEventCommandService, AdminUserQueryService
- `AdminEventQueryController` controller/query/AdminEventQueryController.java · public: getAllEventsPaging, searchEvents, getActiveEvents, getEventsByStatus, getAllEvents, getEventsByBrand, getEventDetail, getEventImage · 주입: AdminEventQueryService, FileUtils

### service-command (2)

- `AdminEventCommandService` service/command/AdminEventCommandService.java · public: createEvent, updateEvent, updateEventStatus, deleteEvent · 주입: AdminEventRepository, AdminSimpleImageCommandService, AdminSimpleImageQueryService, AdminSimpleImageRepository, AdminBrandQueryService, FileUtils
- `AdminSimpleImageCommandService` service/command/AdminSimpleImageCommandService.java · public: createSimpleImages, deleteAllByEvent, deleteSimpleImage · 주입: AdminSimpleImageRepository, FileUtils

### service-query (2)

- `AdminEventQueryService` service/query/AdminEventQueryService.java · public: findEventById, findActiveEvents, findEventsByBrandId, searchEvents, findEventsByStatus, findAllEventsWithPaging, findActiveEventsWithPaging, findAllEvents · 주입: AdminEventRepository, AdminSimpleImageQueryService, FileUtils
- `AdminSimpleImageQueryService` service/query/AdminSimpleImageQueryService.java · public: findByEventId, findById · 주입: AdminSimpleImageRepository

### repository (3)

- `AdminEventRepository` (interface) repository/AdminEventRepository.java · extends/implements JpaRepository, AdminEventRepositoryCustom · public: findByBrandIdOrderByStartDateDesc, findActiveEvents, findByStartDateBeforeAndEndDateAfter, findByEndDateLessThanEqual, findByStartDateAfterOrderByStartDateAsc, findByEndDateBeforeOrderByEndDateDesc, findAllWithBrand, findAllWithBrandPaging, findByIdWithBrand, findActiveEventsWithBrandPaging, findByBrandIdWithBrand, findByBrandIdWithBrandPaging, findByIdWithBrandAndSimpleImages
- `AdminEventSearchRepository` repository/AdminEventSearchRepository.java · public: searchEvents · 주입: JPAQueryFactory
- `AdminSimpleImageRepository` (interface) repository/AdminSimpleImageRepository.java · extends/implements JpaRepository · public: deleteByEventId, findByEventId

### custom (2)

- `AdminEventRepositoryCustom` (interface) repository/AdminEventRepositoryCustom.java · public: searchEvents
- `AdminEventRepositoryImpl` repository/AdminEventRepositoryImpl.java · extends/implements AdminEventRepositoryCustom · public: searchEvents · 주입: JPAQueryFactory

### aop (5)

- `AdminEventBusinessRuleAspect` aop/AdminEventBusinessRuleAspect.java · public: applyBusinessRules · 주입: AdminEventRepository, AdminBrandQueryService
- `AdminEventCachingAspect` aop/AdminEventCachingAspect.java · public: manageCaching, invalidateCacheOnStatusChange, invalidateBrandCache, clearAllCaches, generateCacheReport · 주입: CacheManager
- `AdminEventFileManagementAspect` aop/AdminEventFileManagementAspect.java · public: manageFiles · 주입: FileUtils
- `AdminEventMonitoringAspect` aop/AdminEventMonitoringAspect.java · public: monitorEvent
- `AdminEventValidationAspect` aop/AdminEventValidationAspect.java · public: validateEvent · 주입: AdminEventRepository, AdminBrandQueryService

### other (6)

- `AdminEventBusinessRule` (@interface) aop/annotation/AdminEventBusinessRule.java
- `AdminEventCaching` (@interface) aop/annotation/AdminEventCaching.java
- `AdminEventFileManagement` (@interface) aop/annotation/AdminEventFileManagement.java
- `AdminEventMonitoring` (@interface) aop/annotation/AdminEventMonitoring.java
- `AdminEventValidation` (@interface) aop/annotation/AdminEventValidation.java
- `AdminEventStatusScheduler` scheduler/AdminEventStatusScheduler.java · public: checkEventStatus · 주입: AdminEventRepository

### 다른 도메인 참조 (import·주입 기준)

- **adminproduct**: `AdminBrandQueryService` ← AdminEventBusinessRuleAspect, AdminEventCommandService, AdminEventValidationAspect
- **adminuser**: `AdminUserQueryService` ← AdminEventCommandController
- **event**: `Event` ← AdminEventBusinessRuleAspect, AdminEventCommandService, AdminEventMonitoringAspect, AdminEventQueryService, AdminEventRepository, AdminEventRepositoryCustom, AdminEventRepositoryImpl, AdminEventSearchRepository, AdminEventStatusScheduler, AdminEventValidationAspect, AdminSimpleImageCommandService; `EventStatus` ← AdminEventBusinessRuleAspect, AdminEventCachingAspect, AdminEventCommandController, AdminEventCommandService, AdminEventMonitoringAspect, AdminEventQueryController, AdminEventQueryService, AdminEventRepositoryImpl, AdminEventValidationAspect; `SimpleImage` ← AdminEventCommandService, AdminEventQueryService, AdminSimpleImageCommandService, AdminSimpleImageQueryService, AdminSimpleImageRepository
- **product**: `Brand` ← AdminEventBusinessRuleAspect, AdminEventCommandService, AdminEventValidationAspect

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

- `AdminEventBusinessRuleAspect#applyBusinessRules` @Around("@annotation(businessRule)") · 바인딩: `@AdminEventBusinessRule`
- `AdminEventCachingAspect#manageCaching` @Around("@annotation(eventCaching)") · 바인딩: `@AdminEventCaching`
- `AdminEventFileManagementAspect#manageFiles` @Around("@annotation(fileManagement)") · 바인딩: `@AdminEventFileManagement`
- `AdminEventMonitoringAspect#monitorEvent` @Around("@annotation(eventMonitoring)") · 바인딩: `@AdminEventMonitoring`
- `AdminEventValidationAspect#validateEvent` @Around("@annotation(eventValidation)") · 바인딩: `@AdminEventValidation`

어노테이션 적용 지점(메서드에 붙은 곳):

- `@AdminEventBusinessRule`: (없음)
- `@AdminEventCaching`: (없음)
- `@AdminEventFileManagement`: (없음)
- `@AdminEventMonitoring`: (없음)
- `@AdminEventValidation`: (없음)

