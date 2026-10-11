# partnerevent 도메인

기준 경로: `src/main/java/com/fream/back/domain/partnerevent/` · 파일 22개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/partner/events` | `PartnerEventCommandController#createEvent` | `PartnerUserQueryService#checkAdminRole`, `PartnerEventCommandService#createEvent` |
| PATCH | `/partner/events/{eventId}` | `PartnerEventCommandController#updateEvent` | `PartnerUserQueryService#checkAdminRole`, `PartnerEventCommandService#updateEvent` |
| DELETE | `/partner/events/{eventId}` | `PartnerEventCommandController#deleteEvent` | `PartnerUserQueryService#checkAdminRole`, `PartnerEventCommandService#deleteEvent` |
| PATCH | `/partner/events/{eventId}/status` | `PartnerEventCommandController#updateEventStatus` | `PartnerUserQueryService#checkAdminRole`, `PartnerEventCommandService#updateEventStatus` |
| GET | `/partner/events/page` | `PartnerEventQueryController#getAllEventsPaging` | `PartnerEventQueryService#findAllEventsWithPaging` |
| GET | `/partner/events/search` | `PartnerEventQueryController#searchEvents` | `PartnerEventQueryService#searchEvents` |
| GET | `/partner/events/active` | `PartnerEventQueryController#getActiveEvents` | `PartnerEventQueryService#findActiveEventsWithPaging` |
| GET | `/partner/events/status/{status}` | `PartnerEventQueryController#getEventsByStatus` | `PartnerEventQueryService#findEventsByStatus` |
| GET | `/partner/events` | `PartnerEventQueryController#getAllEvents` | `PartnerEventQueryService#findAllEvents` |
| GET | `/partner/events/brands/{brandId}` | `PartnerEventQueryController#getEventsByBrand` | `PartnerEventQueryService#findEventsByBrandId` |
| GET | `/partner/events/{eventId}` | `PartnerEventQueryController#getEventDetail` | `PartnerEventQueryService#findEventById` |
| GET | `/partner/events/{eventId}/images/{fileName}` | `PartnerEventQueryController#getEventImage` | `FileUtils#existsFile` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `PartnerEventBusinessRuleAspect`
  - `applyBusinessRules` → this#validateBusinessRule, this#postProcessBusinessRules, this#handleRuleViolation
  - `validateBusinessRule` → this#checkEventOverlap, this#checkBrandEventLimit, this#validateEventDates, this#validateStatusTransition, this#checkEditPermission, this#checkDeletePermission, this#checkFileSizeLimit, this#checkCreationFrequency, this#checkBrandStatus, this#checkAutoArchiveRule
  - `checkEventOverlap` → this#findOverlappingEvents
  - `checkBrandEventLimit` → this#extractBrandId, this#updateBrandActiveEvents
  - `validateStatusTransition` → PartnerEventRepository#findById~
  - `checkEditPermission` → this#extractEventId, PartnerEventRepository#findById~
  - `checkDeletePermission` → this#extractEventId, PartnerEventRepository#findById~
  - `checkCreationFrequency` → this#getUserKey
  - `checkBrandStatus` → this#extractBrandId, PartnerBrandQueryService#findById
  - `checkAutoArchiveRule` → this#extractEventId, PartnerEventRepository#findById~, this#archiveEvent
  - `postProcessBusinessRules` → this#handleAutoStatusChange, this#handleBrandDeactivation, this#handleAutoArchiving
  - `handleBrandDeactivation` → this#extractBrandId, this#markBrandEventsInactive, this#endBrandEventsImmediately, this#transferEventOwnership
  - `handleAutoArchiving` → this#extractEventStatus, this#extractEventId, PartnerEventRepository#findById~, this#archiveEvent
  - `archiveEvent` → this#getUserKey
  - `findOverlappingEvents` → PartnerEventRepository#findByBrandIdOrderByStartDateDesc
  - `updateBrandActiveEvents` → PartnerEventRepository#findByBrandIdOrderByStartDateDesc
- `PartnerEventCachingAspect`
  - `manageCaching` → this#generateCacheKey, this#determineCacheName, this#evaluateCacheCondition, this#isExpired, this#executeAndCache, this#logCacheMetrics, this#scheduleWarmUp
  - `generateCacheKey` → this#appendEventId, this#appendBrandId, this#appendUserId, this#appendStatus, this#appendTimeSegment, this#appendCompositeKey
  - `determineCacheName` → this#isAdminUser
  - `scheduleWarmUp` → this#warmUpFrequentData
  - `appendCompositeKey` → this#appendUserId, this#appendEventId, this#appendTimeSegment
- `PartnerEventCommandService`
  - `createEvent` → this#validateEventDates, PartnerBrandQueryService#findById, PartnerEventRepository#save~, FileUtils#saveFile, PartnerSimpleImageCommandService#createSimpleImages
  - `updateEvent` → this#validateEventDates, PartnerEventRepository#findById~, this#handleSimpleImagesUpdate, this#handleThumbnailUpdate, PartnerEventRepository#save~
  - `updateEventStatus` → PartnerEventRepository#findById~, PartnerEventRepository#save~
  - `handleSimpleImagesUpdate` → PartnerSimpleImageQueryService#findByEventId, FileUtils#deleteFile, PartnerSimpleImageRepository#delete~, PartnerSimpleImageCommandService#createSimpleImages
  - `handleThumbnailUpdate` → FileUtils#deleteFile, FileUtils#saveFile
  - `deleteEvent` → PartnerEventRepository#findById~, FileUtils#deleteFile, PartnerSimpleImageQueryService#findByEventId, PartnerEventRepository#delete~
- `PartnerEventFileManagementAspect`
  - `manageFiles` → this#preprocessFiles, this#postprocessFiles, this#logFileMetrics, this#rollbackFileOperations
  - `preprocessFiles` → this#validateFiles, this#resizeImages, this#convertToWebP, this#scanForVirus, this#createBackup
  - `validateFiles` → this#validateSingleFile
  - `resizeImages` → this#isImageFile, this#resizeImage
  - `postprocessFiles` → this#cleanupTempFiles
- `PartnerEventMonitoringAspect`
  - `monitorEvent` → this#shouldCollectMetric, this#extractUserEmail, this#trackUserActivity, this#trackEventLifecycle, this#recordSuccessMetrics, this#collectBrandStatistics, this#monitorFileOperations, this#checkAndAlertStatusChange, this#checkThresholdsAndAlert, this#scheduleMetricsReport
  - `trackEventLifecycle` → this#extractEventId, this#extractEventStatus, this#extractUserEmail
  - `collectBrandStatistics` → this#extractBrandId, this#calculateTotalFileSize
  - `checkAndAlertStatusChange` → this#extractEventId, this#extractEventStatus, this#sendStatusChangeNotification
  - `checkThresholdsAndAlert` → this#calculateTotalFileSize, this#checkCreationFrequency, this#calculateCacheHitRate
  - `generateMetricsReport` → this#calculateCacheHitRate
- `PartnerEventQueryService`
  - `findEventById` → PartnerEventRepository#findById~, PartnerSimpleImageQueryService#findByEventId, FileUtils#getFileUrl
  - `findActiveEvents` → PartnerEventRepository#findActiveEvents
  - `findEventsByBrandId` → PartnerEventRepository#findByBrandIdOrderByStartDateDesc
  - `searchEvents` → PartnerEventRepository#searchEvents
  - `findEventsByStatus` → PartnerEventRepository#findByStartDateAfterOrderByStartDateAsc, PartnerEventRepository#findActiveEvents, PartnerEventRepository#findByEndDateBeforeOrderByEndDateDesc
  - `findAllEventsWithPaging` → PartnerEventRepository#findAllWithBrandPaging
  - `findActiveEventsWithPaging` → PartnerEventRepository#findActiveEventsWithBrandPaging
  - `findAllEvents` → PartnerEventRepository#findAllWithBrand
  - `convertToEventListDto` → FileUtils#getFileUrl
- `PartnerEventRepositoryImpl`
  - `searchEvents` → this#keywordContains, this#brandIdEquals, this#isActiveEquals, this#startDateAfter, this#endDateBefore, this#statusEquals, this#getOrderSpecifier
- `PartnerEventSearchRepository`
  - `searchEvents` → this#keywordContains, this#brandIdEquals, this#isActiveEquals, this#startDateAfter, this#endDateBefore, this#getOrderSpecifier
- `PartnerEventStatusScheduler`
  - `checkEventStatus` → this#checkUpcomingToActiveEvents, this#checkActiveToEndedEvents
  - `checkUpcomingToActiveEvents` → PartnerEventRepository#findByStartDateBeforeAndEndDateAfter
  - `checkActiveToEndedEvents` → PartnerEventRepository#findByEndDateLessThanEqual
- `PartnerEventValidationAspect`
  - `validateEvent` → this#performValidation, this#validateAdminPermission, this#handleValidationFailure
  - `performValidation` → this#validateDateRange, this#validateBusinessRules, this#validateBrand, this#validateFiles, this#validateStatusTransition, this#checkDuplicates, this#checkCapacity, this#validatePermissions
  - `validateBusinessRules` → this#extractBrandId, PartnerEventRepository#findByBrandIdOrderByStartDateDesc
  - `validateBrand` → this#extractBrandId, PartnerBrandQueryService#findById
  - `validateFiles` → this#validateFileExtension
  - `validateStatusTransition` → PartnerEventRepository#findById~
  - `checkDuplicates` → this#extractTitle
- `PartnerSimpleImageCommandService`
  - `createSimpleImages` → FileUtils#saveFile, PartnerSimpleImageRepository#save~
  - `deleteAllByEvent` → PartnerSimpleImageRepository#deleteByEventId
  - `deleteSimpleImage` → PartnerSimpleImageRepository#findById~, FileUtils#deleteFile, PartnerSimpleImageRepository#delete~
- `PartnerSimpleImageQueryService`
  - `findByEventId` → PartnerSimpleImageRepository#findByEventId
  - `findById` → PartnerSimpleImageRepository#findById~

## 3. 클래스 목록 (역할별)

### controller (2)

- `PartnerEventCommandController` controller/command/PartnerEventCommandController.java · public: createEvent, updateEvent, deleteEvent, updateEventStatus · 주입: PartnerEventCommandService, PartnerUserQueryService
- `PartnerEventQueryController` controller/query/PartnerEventQueryController.java · public: getAllEventsPaging, searchEvents, getActiveEvents, getEventsByStatus, getAllEvents, getEventsByBrand, getEventDetail, getEventImage · 주입: PartnerEventQueryService, FileUtils

### service-command (2)

- `PartnerEventCommandService` service/command/PartnerEventCommandService.java · public: createEvent, updateEvent, updateEventStatus, deleteEvent · 주입: PartnerEventRepository, PartnerSimpleImageCommandService, PartnerSimpleImageQueryService, PartnerSimpleImageRepository, PartnerBrandQueryService, FileUtils
- `PartnerSimpleImageCommandService` service/command/PartnerSimpleImageCommandService.java · public: createSimpleImages, deleteAllByEvent, deleteSimpleImage · 주입: PartnerSimpleImageRepository, FileUtils

### service-query (2)

- `PartnerEventQueryService` service/query/PartnerEventQueryService.java · public: findEventById, findActiveEvents, findEventsByBrandId, searchEvents, findEventsByStatus, findAllEventsWithPaging, findActiveEventsWithPaging, findAllEvents · 주입: PartnerEventRepository, PartnerSimpleImageQueryService, FileUtils
- `PartnerSimpleImageQueryService` service/query/PartnerSimpleImageQueryService.java · public: findByEventId, findById · 주입: PartnerSimpleImageRepository

### repository (3)

- `PartnerEventRepository` (interface) repository/PartnerEventRepository.java · extends/implements JpaRepository, PartnerEventRepositoryCustom · public: findByBrandIdOrderByStartDateDesc, findActiveEvents, findByStartDateBeforeAndEndDateAfter, findByEndDateLessThanEqual, findByStartDateAfterOrderByStartDateAsc, findByEndDateBeforeOrderByEndDateDesc, findAllWithBrand, findAllWithBrandPaging, findByIdWithBrand, findActiveEventsWithBrandPaging, findByBrandIdWithBrand, findByBrandIdWithBrandPaging, findByIdWithBrandAndSimpleImages
- `PartnerEventSearchRepository` repository/PartnerEventSearchRepository.java · public: searchEvents · 주입: JPAQueryFactory
- `PartnerSimpleImageRepository` (interface) repository/PartnerSimpleImageRepository.java · extends/implements JpaRepository · public: deleteByEventId, findByEventId

### custom (2)

- `PartnerEventRepositoryCustom` (interface) repository/PartnerEventRepositoryCustom.java · public: searchEvents
- `PartnerEventRepositoryImpl` repository/PartnerEventRepositoryImpl.java · extends/implements PartnerEventRepositoryCustom · public: searchEvents · 주입: JPAQueryFactory

### aop (5)

- `PartnerEventBusinessRuleAspect` aop/PartnerEventBusinessRuleAspect.java · public: applyBusinessRules · 주입: PartnerEventRepository, PartnerBrandQueryService
- `PartnerEventCachingAspect` aop/PartnerEventCachingAspect.java · public: manageCaching, invalidateCacheOnStatusChange, invalidateBrandCache, clearAllCaches, generateCacheReport · 주입: CacheManager
- `PartnerEventFileManagementAspect` aop/PartnerEventFileManagementAspect.java · public: manageFiles · 주입: FileUtils
- `PartnerEventMonitoringAspect` aop/PartnerEventMonitoringAspect.java · public: monitorEvent
- `PartnerEventValidationAspect` aop/PartnerEventValidationAspect.java · public: validateEvent · 주입: PartnerEventRepository, PartnerBrandQueryService

### other (6)

- `PartnerEventBusinessRule` (@interface) aop/annotation/PartnerEventBusinessRule.java
- `PartnerEventCaching` (@interface) aop/annotation/PartnerEventCaching.java
- `PartnerEventFileManagement` (@interface) aop/annotation/PartnerEventFileManagement.java
- `PartnerEventMonitoring` (@interface) aop/annotation/PartnerEventMonitoring.java
- `PartnerEventValidation` (@interface) aop/annotation/PartnerEventValidation.java
- `PartnerEventStatusScheduler` scheduler/PartnerEventStatusScheduler.java · public: checkEventStatus · 주입: PartnerEventRepository

### 다른 도메인 참조 (import·주입 기준)

- **event**: `Event` ← PartnerEventBusinessRuleAspect, PartnerEventCommandService, PartnerEventMonitoringAspect, PartnerEventQueryService, PartnerEventRepository, PartnerEventRepositoryCustom, PartnerEventRepositoryImpl, PartnerEventSearchRepository, PartnerEventStatusScheduler, PartnerEventValidationAspect, PartnerSimpleImageCommandService; `EventStatus` ← PartnerEventBusinessRuleAspect, PartnerEventCachingAspect, PartnerEventCommandController, PartnerEventCommandService, PartnerEventMonitoringAspect, PartnerEventQueryController, PartnerEventQueryService, PartnerEventRepositoryImpl, PartnerEventValidationAspect; `SimpleImage` ← PartnerEventCommandService, PartnerEventQueryService, PartnerSimpleImageCommandService, PartnerSimpleImageQueryService, PartnerSimpleImageRepository
- **partnerproduct**: `PartnerBrandQueryService` ← PartnerEventBusinessRuleAspect, PartnerEventCommandService, PartnerEventValidationAspect
- **partneruser**: `PartnerUserQueryService` ← PartnerEventCommandController
- **product**: `Brand` ← PartnerEventBusinessRuleAspect, PartnerEventCommandService, PartnerEventValidationAspect

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

- `PartnerEventBusinessRuleAspect#applyBusinessRules` @Around("@annotation(businessRule)") · 바인딩: `@PartnerEventBusinessRule`
- `PartnerEventCachingAspect#manageCaching` @Around("@annotation(eventCaching)") · 바인딩: `@PartnerEventCaching`
- `PartnerEventFileManagementAspect#manageFiles` @Around("@annotation(fileManagement)") · 바인딩: `@PartnerEventFileManagement`
- `PartnerEventMonitoringAspect#monitorEvent` @Around("@annotation(eventMonitoring)") · 바인딩: `@PartnerEventMonitoring`
- `PartnerEventValidationAspect#validateEvent` @Around("@annotation(eventValidation)") · 바인딩: `@PartnerEventValidation`

어노테이션 적용 지점(메서드에 붙은 곳):

- `@PartnerEventBusinessRule`: (없음)
- `@PartnerEventCaching`: (없음)
- `@PartnerEventFileManagement`: (없음)
- `@PartnerEventMonitoring`: (없음)
- `@PartnerEventValidation`: (없음)

