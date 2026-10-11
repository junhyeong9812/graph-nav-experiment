# legacyevent 도메인

기준 경로: `src/main/java/com/fream/back/domain/legacyevent/` · 파일 22개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/legacy/events` | `LegacyEventCommandController#createEvent` | `LegacyUserQueryService#checkAdminRole`, `LegacyEventCommandService#createEvent` |
| PATCH | `/legacy/events/{eventId}` | `LegacyEventCommandController#updateEvent` | `LegacyUserQueryService#checkAdminRole`, `LegacyEventCommandService#updateEvent` |
| DELETE | `/legacy/events/{eventId}` | `LegacyEventCommandController#deleteEvent` | `LegacyUserQueryService#checkAdminRole`, `LegacyEventCommandService#deleteEvent` |
| PATCH | `/legacy/events/{eventId}/status` | `LegacyEventCommandController#updateEventStatus` | `LegacyUserQueryService#checkAdminRole`, `LegacyEventCommandService#updateEventStatus` |
| GET | `/legacy/events/page` | `LegacyEventQueryController#getAllEventsPaging` | `LegacyEventQueryService#findAllEventsWithPaging` |
| GET | `/legacy/events/search` | `LegacyEventQueryController#searchEvents` | `LegacyEventQueryService#searchEvents` |
| GET | `/legacy/events/active` | `LegacyEventQueryController#getActiveEvents` | `LegacyEventQueryService#findActiveEventsWithPaging` |
| GET | `/legacy/events/status/{status}` | `LegacyEventQueryController#getEventsByStatus` | `LegacyEventQueryService#findEventsByStatus` |
| GET | `/legacy/events` | `LegacyEventQueryController#getAllEvents` | `LegacyEventQueryService#findAllEvents` |
| GET | `/legacy/events/brands/{brandId}` | `LegacyEventQueryController#getEventsByBrand` | `LegacyEventQueryService#findEventsByBrandId` |
| GET | `/legacy/events/{eventId}` | `LegacyEventQueryController#getEventDetail` | `LegacyEventQueryService#findEventById` |
| GET | `/legacy/events/{eventId}/images/{fileName}` | `LegacyEventQueryController#getEventImage` | `FileUtils#existsFile` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `LegacyEventBusinessRuleAspect`
  - `applyBusinessRules` → this#validateBusinessRule, this#postProcessBusinessRules, this#handleRuleViolation
  - `validateBusinessRule` → this#checkEventOverlap, this#checkBrandEventLimit, this#validateEventDates, this#validateStatusTransition, this#checkEditPermission, this#checkDeletePermission, this#checkFileSizeLimit, this#checkCreationFrequency, this#checkBrandStatus, this#checkAutoArchiveRule
  - `checkEventOverlap` → this#findOverlappingEvents
  - `checkBrandEventLimit` → this#extractBrandId, this#updateBrandActiveEvents
  - `validateStatusTransition` → LegacyEventRepository#findById~
  - `checkEditPermission` → this#extractEventId, LegacyEventRepository#findById~
  - `checkDeletePermission` → this#extractEventId, LegacyEventRepository#findById~
  - `checkCreationFrequency` → this#getUserKey
  - `checkBrandStatus` → this#extractBrandId, LegacyBrandQueryService#findById
  - `checkAutoArchiveRule` → this#extractEventId, LegacyEventRepository#findById~, this#archiveEvent
  - `postProcessBusinessRules` → this#handleAutoStatusChange, this#handleBrandDeactivation, this#handleAutoArchiving
  - `handleBrandDeactivation` → this#extractBrandId, this#markBrandEventsInactive, this#endBrandEventsImmediately, this#transferEventOwnership
  - `handleAutoArchiving` → this#extractEventStatus, this#extractEventId, LegacyEventRepository#findById~, this#archiveEvent
  - `archiveEvent` → this#getUserKey
  - `findOverlappingEvents` → LegacyEventRepository#findByBrandIdOrderByStartDateDesc
  - `updateBrandActiveEvents` → LegacyEventRepository#findByBrandIdOrderByStartDateDesc
- `LegacyEventCachingAspect`
  - `manageCaching` → this#generateCacheKey, this#determineCacheName, this#evaluateCacheCondition, this#isExpired, this#executeAndCache, this#logCacheMetrics, this#scheduleWarmUp
  - `generateCacheKey` → this#appendEventId, this#appendBrandId, this#appendUserId, this#appendStatus, this#appendTimeSegment, this#appendCompositeKey
  - `determineCacheName` → this#isAdminUser
  - `scheduleWarmUp` → this#warmUpFrequentData
  - `appendCompositeKey` → this#appendUserId, this#appendEventId, this#appendTimeSegment
- `LegacyEventCommandService`
  - `createEvent` → this#validateEventDates, LegacyBrandQueryService#findById, LegacyEventRepository#save~, FileUtils#saveFile, LegacySimpleImageCommandService#createSimpleImages
  - `updateEvent` → this#validateEventDates, LegacyEventRepository#findById~, this#handleSimpleImagesUpdate, this#handleThumbnailUpdate, LegacyEventRepository#save~
  - `updateEventStatus` → LegacyEventRepository#findById~, LegacyEventRepository#save~
  - `handleSimpleImagesUpdate` → LegacySimpleImageQueryService#findByEventId, FileUtils#deleteFile, LegacySimpleImageRepository#delete~, LegacySimpleImageCommandService#createSimpleImages
  - `handleThumbnailUpdate` → FileUtils#deleteFile, FileUtils#saveFile
  - `deleteEvent` → LegacyEventRepository#findById~, FileUtils#deleteFile, LegacySimpleImageQueryService#findByEventId, LegacyEventRepository#delete~
- `LegacyEventFileManagementAspect`
  - `manageFiles` → this#preprocessFiles, this#postprocessFiles, this#logFileMetrics, this#rollbackFileOperations
  - `preprocessFiles` → this#validateFiles, this#resizeImages, this#convertToWebP, this#scanForVirus, this#createBackup
  - `validateFiles` → this#validateSingleFile
  - `resizeImages` → this#isImageFile, this#resizeImage
  - `postprocessFiles` → this#cleanupTempFiles
- `LegacyEventMonitoringAspect`
  - `monitorEvent` → this#shouldCollectMetric, this#extractUserEmail, this#trackUserActivity, this#trackEventLifecycle, this#recordSuccessMetrics, this#collectBrandStatistics, this#monitorFileOperations, this#checkAndAlertStatusChange, this#checkThresholdsAndAlert, this#scheduleMetricsReport
  - `trackEventLifecycle` → this#extractEventId, this#extractEventStatus, this#extractUserEmail
  - `collectBrandStatistics` → this#extractBrandId, this#calculateTotalFileSize
  - `checkAndAlertStatusChange` → this#extractEventId, this#extractEventStatus, this#sendStatusChangeNotification
  - `checkThresholdsAndAlert` → this#calculateTotalFileSize, this#checkCreationFrequency, this#calculateCacheHitRate
  - `generateMetricsReport` → this#calculateCacheHitRate
- `LegacyEventQueryService`
  - `findEventById` → LegacyEventRepository#findById~, LegacySimpleImageQueryService#findByEventId, FileUtils#getFileUrl
  - `findActiveEvents` → LegacyEventRepository#findActiveEvents
  - `findEventsByBrandId` → LegacyEventRepository#findByBrandIdOrderByStartDateDesc
  - `searchEvents` → LegacyEventRepository#searchEvents
  - `findEventsByStatus` → LegacyEventRepository#findByStartDateAfterOrderByStartDateAsc, LegacyEventRepository#findActiveEvents, LegacyEventRepository#findByEndDateBeforeOrderByEndDateDesc
  - `findAllEventsWithPaging` → LegacyEventRepository#findAllWithBrandPaging
  - `findActiveEventsWithPaging` → LegacyEventRepository#findActiveEventsWithBrandPaging
  - `findAllEvents` → LegacyEventRepository#findAllWithBrand
  - `convertToEventListDto` → FileUtils#getFileUrl
- `LegacyEventRepositoryImpl`
  - `searchEvents` → this#keywordContains, this#brandIdEquals, this#isActiveEquals, this#startDateAfter, this#endDateBefore, this#statusEquals, this#getOrderSpecifier
- `LegacyEventSearchRepository`
  - `searchEvents` → this#keywordContains, this#brandIdEquals, this#isActiveEquals, this#startDateAfter, this#endDateBefore, this#getOrderSpecifier
- `LegacyEventStatusScheduler`
  - `checkEventStatus` → this#checkUpcomingToActiveEvents, this#checkActiveToEndedEvents
  - `checkUpcomingToActiveEvents` → LegacyEventRepository#findByStartDateBeforeAndEndDateAfter
  - `checkActiveToEndedEvents` → LegacyEventRepository#findByEndDateLessThanEqual
- `LegacyEventValidationAspect`
  - `validateEvent` → this#performValidation, this#validateAdminPermission, this#handleValidationFailure
  - `performValidation` → this#validateDateRange, this#validateBusinessRules, this#validateBrand, this#validateFiles, this#validateStatusTransition, this#checkDuplicates, this#checkCapacity, this#validatePermissions
  - `validateBusinessRules` → this#extractBrandId, LegacyEventRepository#findByBrandIdOrderByStartDateDesc
  - `validateBrand` → this#extractBrandId, LegacyBrandQueryService#findById
  - `validateFiles` → this#validateFileExtension
  - `validateStatusTransition` → LegacyEventRepository#findById~
  - `checkDuplicates` → this#extractTitle
- `LegacySimpleImageCommandService`
  - `createSimpleImages` → FileUtils#saveFile, LegacySimpleImageRepository#save~
  - `deleteAllByEvent` → LegacySimpleImageRepository#deleteByEventId
  - `deleteSimpleImage` → LegacySimpleImageRepository#findById~, FileUtils#deleteFile, LegacySimpleImageRepository#delete~
- `LegacySimpleImageQueryService`
  - `findByEventId` → LegacySimpleImageRepository#findByEventId
  - `findById` → LegacySimpleImageRepository#findById~

## 3. 클래스 목록 (역할별)

### controller (2)

- `LegacyEventCommandController` controller/command/LegacyEventCommandController.java · public: createEvent, updateEvent, deleteEvent, updateEventStatus · 주입: LegacyEventCommandService, LegacyUserQueryService
- `LegacyEventQueryController` controller/query/LegacyEventQueryController.java · public: getAllEventsPaging, searchEvents, getActiveEvents, getEventsByStatus, getAllEvents, getEventsByBrand, getEventDetail, getEventImage · 주입: LegacyEventQueryService, FileUtils

### service-command (2)

- `LegacyEventCommandService` service/command/LegacyEventCommandService.java · public: createEvent, updateEvent, updateEventStatus, deleteEvent · 주입: LegacyEventRepository, LegacySimpleImageCommandService, LegacySimpleImageQueryService, LegacySimpleImageRepository, LegacyBrandQueryService, FileUtils
- `LegacySimpleImageCommandService` service/command/LegacySimpleImageCommandService.java · public: createSimpleImages, deleteAllByEvent, deleteSimpleImage · 주입: LegacySimpleImageRepository, FileUtils

### service-query (2)

- `LegacyEventQueryService` service/query/LegacyEventQueryService.java · public: findEventById, findActiveEvents, findEventsByBrandId, searchEvents, findEventsByStatus, findAllEventsWithPaging, findActiveEventsWithPaging, findAllEvents · 주입: LegacyEventRepository, LegacySimpleImageQueryService, FileUtils
- `LegacySimpleImageQueryService` service/query/LegacySimpleImageQueryService.java · public: findByEventId, findById · 주입: LegacySimpleImageRepository

### repository (3)

- `LegacyEventRepository` (interface) repository/LegacyEventRepository.java · extends/implements JpaRepository, LegacyEventRepositoryCustom · public: findByBrandIdOrderByStartDateDesc, findActiveEvents, findByStartDateBeforeAndEndDateAfter, findByEndDateLessThanEqual, findByStartDateAfterOrderByStartDateAsc, findByEndDateBeforeOrderByEndDateDesc, findAllWithBrand, findAllWithBrandPaging, findByIdWithBrand, findActiveEventsWithBrandPaging, findByBrandIdWithBrand, findByBrandIdWithBrandPaging, findByIdWithBrandAndSimpleImages
- `LegacyEventSearchRepository` repository/LegacyEventSearchRepository.java · public: searchEvents · 주입: JPAQueryFactory
- `LegacySimpleImageRepository` (interface) repository/LegacySimpleImageRepository.java · extends/implements JpaRepository · public: deleteByEventId, findByEventId

### custom (2)

- `LegacyEventRepositoryCustom` (interface) repository/LegacyEventRepositoryCustom.java · public: searchEvents
- `LegacyEventRepositoryImpl` repository/LegacyEventRepositoryImpl.java · extends/implements LegacyEventRepositoryCustom · public: searchEvents · 주입: JPAQueryFactory

### aop (5)

- `LegacyEventBusinessRuleAspect` aop/LegacyEventBusinessRuleAspect.java · public: applyBusinessRules · 주입: LegacyEventRepository, LegacyBrandQueryService
- `LegacyEventCachingAspect` aop/LegacyEventCachingAspect.java · public: manageCaching, invalidateCacheOnStatusChange, invalidateBrandCache, clearAllCaches, generateCacheReport · 주입: CacheManager
- `LegacyEventFileManagementAspect` aop/LegacyEventFileManagementAspect.java · public: manageFiles · 주입: FileUtils
- `LegacyEventMonitoringAspect` aop/LegacyEventMonitoringAspect.java · public: monitorEvent
- `LegacyEventValidationAspect` aop/LegacyEventValidationAspect.java · public: validateEvent · 주입: LegacyEventRepository, LegacyBrandQueryService

### other (6)

- `LegacyEventBusinessRule` (@interface) aop/annotation/LegacyEventBusinessRule.java
- `LegacyEventCaching` (@interface) aop/annotation/LegacyEventCaching.java
- `LegacyEventFileManagement` (@interface) aop/annotation/LegacyEventFileManagement.java
- `LegacyEventMonitoring` (@interface) aop/annotation/LegacyEventMonitoring.java
- `LegacyEventValidation` (@interface) aop/annotation/LegacyEventValidation.java
- `LegacyEventStatusScheduler` scheduler/LegacyEventStatusScheduler.java · public: checkEventStatus · 주입: LegacyEventRepository

### 다른 도메인 참조 (import·주입 기준)

- **event**: `Event` ← LegacyEventBusinessRuleAspect, LegacyEventCommandService, LegacyEventMonitoringAspect, LegacyEventQueryService, LegacyEventRepository, LegacyEventRepositoryCustom, LegacyEventRepositoryImpl, LegacyEventSearchRepository, LegacyEventStatusScheduler, LegacyEventValidationAspect, LegacySimpleImageCommandService; `EventStatus` ← LegacyEventBusinessRuleAspect, LegacyEventCachingAspect, LegacyEventCommandController, LegacyEventCommandService, LegacyEventMonitoringAspect, LegacyEventQueryController, LegacyEventQueryService, LegacyEventRepositoryImpl, LegacyEventValidationAspect; `SimpleImage` ← LegacyEventCommandService, LegacyEventQueryService, LegacySimpleImageCommandService, LegacySimpleImageQueryService, LegacySimpleImageRepository
- **legacyproduct**: `LegacyBrandQueryService` ← LegacyEventBusinessRuleAspect, LegacyEventCommandService, LegacyEventValidationAspect
- **legacyuser**: `LegacyUserQueryService` ← LegacyEventCommandController
- **product**: `Brand` ← LegacyEventBusinessRuleAspect, LegacyEventCommandService, LegacyEventValidationAspect

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

- `LegacyEventBusinessRuleAspect#applyBusinessRules` @Around("@annotation(businessRule)") · 바인딩: `@LegacyEventBusinessRule`
- `LegacyEventCachingAspect#manageCaching` @Around("@annotation(eventCaching)") · 바인딩: `@LegacyEventCaching`
- `LegacyEventFileManagementAspect#manageFiles` @Around("@annotation(fileManagement)") · 바인딩: `@LegacyEventFileManagement`
- `LegacyEventMonitoringAspect#monitorEvent` @Around("@annotation(eventMonitoring)") · 바인딩: `@LegacyEventMonitoring`
- `LegacyEventValidationAspect#validateEvent` @Around("@annotation(eventValidation)") · 바인딩: `@LegacyEventValidation`

어노테이션 적용 지점(메서드에 붙은 곳):

- `@LegacyEventBusinessRule`: (없음)
- `@LegacyEventCaching`: (없음)
- `@LegacyEventFileManagement`: (없음)
- `@LegacyEventMonitoring`: (없음)
- `@LegacyEventValidation`: (없음)

