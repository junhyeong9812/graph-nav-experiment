# event 도메인

기준 경로: `src/main/java/com/fream/back/domain/event/` · 파일 35개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/events` | `EventCommandController#createEvent` | `UserQueryService#checkAdminRole`, `EventCommandService#createEvent` |
| PATCH | `/events/{eventId}` | `EventCommandController#updateEvent` | `UserQueryService#checkAdminRole`, `EventCommandService#updateEvent` |
| DELETE | `/events/{eventId}` | `EventCommandController#deleteEvent` | `UserQueryService#checkAdminRole`, `EventCommandService#deleteEvent` |
| PATCH | `/events/{eventId}/status` | `EventCommandController#updateEventStatus` | `UserQueryService#checkAdminRole`, `EventCommandService#updateEventStatus` |
| GET | `/events/page` | `EventQueryController#getAllEventsPaging` | `EventQueryService#findAllEventsWithPaging` |
| GET | `/events/search` | `EventQueryController#searchEvents` | `EventQueryService#searchEvents` |
| GET | `/events/active` | `EventQueryController#getActiveEvents` | `EventQueryService#findActiveEventsWithPaging` |
| GET | `/events/status/{status}` | `EventQueryController#getEventsByStatus` | `EventQueryService#findEventsByStatus` |
| GET | `/events` | `EventQueryController#getAllEvents` | `EventQueryService#findAllEvents` |
| GET | `/events/brands/{brandId}` | `EventQueryController#getEventsByBrand` | `EventQueryService#findEventsByBrandId` |
| GET | `/events/{eventId}` | `EventQueryController#getEventDetail` | `EventQueryService#findEventById` |
| GET | `/events/{eventId}/images/{fileName}` | `EventQueryController#getEventImage` | `FileUtils#existsFile` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `Event`
  - `getStatus` → this#isUpcoming, this#isActive
  - `getStatusDisplayName` → this#getStatus
- `EventBusinessRuleAspect`
  - `applyBusinessRules` → this#validateBusinessRule, this#postProcessBusinessRules, this#handleRuleViolation
  - `validateBusinessRule` → this#checkEventOverlap, this#checkBrandEventLimit, this#validateEventDates, this#validateStatusTransition, this#checkEditPermission, this#checkDeletePermission, this#checkFileSizeLimit, this#checkCreationFrequency, this#checkBrandStatus, this#checkAutoArchiveRule
  - `checkEventOverlap` → this#findOverlappingEvents
  - `checkBrandEventLimit` → this#extractBrandId, this#updateBrandActiveEvents
  - `validateStatusTransition` → EventRepository#findById~
  - `checkEditPermission` → this#extractEventId, EventRepository#findById~
  - `checkDeletePermission` → this#extractEventId, EventRepository#findById~
  - `checkCreationFrequency` → this#getUserKey
  - `checkBrandStatus` → this#extractBrandId, BrandQueryService#findById
  - `checkAutoArchiveRule` → this#extractEventId, EventRepository#findById~, this#archiveEvent
  - `postProcessBusinessRules` → this#handleAutoStatusChange, this#handleBrandDeactivation, this#handleAutoArchiving
  - `handleBrandDeactivation` → this#extractBrandId, this#markBrandEventsInactive, this#endBrandEventsImmediately, this#transferEventOwnership
  - `handleAutoArchiving` → this#extractEventStatus, this#extractEventId, EventRepository#findById~, this#archiveEvent
  - `archiveEvent` → this#getUserKey
  - `findOverlappingEvents` → EventRepository#findByBrandIdOrderByStartDateDesc
  - `updateBrandActiveEvents` → EventRepository#findByBrandIdOrderByStartDateDesc
- `EventCachingAspect`
  - `manageCaching` → this#generateCacheKey, this#determineCacheName, this#evaluateCacheCondition, this#isExpired, this#executeAndCache, this#logCacheMetrics, this#scheduleWarmUp
  - `generateCacheKey` → this#appendEventId, this#appendBrandId, this#appendUserId, this#appendStatus, this#appendTimeSegment, this#appendCompositeKey
  - `determineCacheName` → this#isAdminUser
  - `scheduleWarmUp` → this#warmUpFrequentData
  - `appendCompositeKey` → this#appendUserId, this#appendEventId, this#appendTimeSegment
- `EventCommandService`
  - `createEvent` → this#validateEventDates, BrandQueryService#findById, EventRepository#save~, FileUtils#saveFile, SimpleImageCommandService#createSimpleImages
  - `updateEvent` → this#validateEventDates, EventRepository#findById~, this#handleSimpleImagesUpdate, this#handleThumbnailUpdate, EventRepository#save~
  - `updateEventStatus` → EventRepository#findById~, EventRepository#save~
  - `handleSimpleImagesUpdate` → SimpleImageQueryService#findByEventId, FileUtils#deleteFile, SimpleImageRepository#delete~, SimpleImageCommandService#createSimpleImages
  - `handleThumbnailUpdate` → FileUtils#deleteFile, FileUtils#saveFile
  - `deleteEvent` → EventRepository#findById~, FileUtils#deleteFile, SimpleImageQueryService#findByEventId, EventRepository#delete~
- `EventFileManagementAspect`
  - `manageFiles` → this#preprocessFiles, this#postprocessFiles, this#logFileMetrics, this#rollbackFileOperations
  - `preprocessFiles` → this#validateFiles, this#resizeImages, this#convertToWebP, this#scanForVirus, this#createBackup
  - `validateFiles` → this#validateSingleFile
  - `resizeImages` → this#isImageFile, this#resizeImage
  - `postprocessFiles` → this#cleanupTempFiles
- `EventMonitoringAspect`
  - `monitorEvent` → this#shouldCollectMetric, this#extractUserEmail, this#trackUserActivity, this#trackEventLifecycle, this#recordSuccessMetrics, this#collectBrandStatistics, this#monitorFileOperations, this#checkAndAlertStatusChange, this#checkThresholdsAndAlert, this#scheduleMetricsReport
  - `trackEventLifecycle` → this#extractEventId, this#extractEventStatus, this#extractUserEmail
  - `collectBrandStatistics` → this#extractBrandId, this#calculateTotalFileSize
  - `checkAndAlertStatusChange` → this#extractEventId, this#extractEventStatus, this#sendStatusChangeNotification
  - `checkThresholdsAndAlert` → this#calculateTotalFileSize, this#checkCreationFrequency, this#calculateCacheHitRate
  - `generateMetricsReport` → this#calculateCacheHitRate
- `EventQueryService`
  - `findEventById` → EventRepository#findById~, SimpleImageQueryService#findByEventId, FileUtils#getFileUrl
  - `findActiveEvents` → EventRepository#findActiveEvents
  - `findEventsByBrandId` → EventRepository#findByBrandIdOrderByStartDateDesc
  - `searchEvents` → EventRepository#searchEvents
  - `findEventsByStatus` → EventRepository#findByStartDateAfterOrderByStartDateAsc, EventRepository#findActiveEvents, EventRepository#findByEndDateBeforeOrderByEndDateDesc
  - `findAllEventsWithPaging` → EventRepository#findAllWithBrandPaging
  - `findActiveEventsWithPaging` → EventRepository#findActiveEventsWithBrandPaging
  - `findAllEvents` → EventRepository#findAllWithBrand
  - `convertToEventListDto` → FileUtils#getFileUrl
- `EventRepositoryImpl`
  - `searchEvents` → this#keywordContains, this#brandIdEquals, this#isActiveEquals, this#startDateAfter, this#endDateBefore, this#statusEquals, this#getOrderSpecifier
- `EventSearchRepository`
  - `searchEvents` → this#keywordContains, this#brandIdEquals, this#isActiveEquals, this#startDateAfter, this#endDateBefore, this#getOrderSpecifier
- `EventStatusScheduler`
  - `checkEventStatus` → this#checkUpcomingToActiveEvents, this#checkActiveToEndedEvents
  - `checkUpcomingToActiveEvents` → EventRepository#findByStartDateBeforeAndEndDateAfter
  - `checkActiveToEndedEvents` → EventRepository#findByEndDateLessThanEqual
- `EventValidationAspect`
  - `validateEvent` → this#performValidation, this#validateAdminPermission, this#handleValidationFailure
  - `performValidation` → this#validateDateRange, this#validateBusinessRules, this#validateBrand, this#validateFiles, this#validateStatusTransition, this#checkDuplicates, this#checkCapacity, this#validatePermissions
  - `validateBusinessRules` → this#extractBrandId, EventRepository#findByBrandIdOrderByStartDateDesc
  - `validateBrand` → this#extractBrandId, BrandQueryService#findById
  - `validateFiles` → this#validateFileExtension
  - `validateStatusTransition` → EventRepository#findById~
  - `checkDuplicates` → this#extractTitle
- `SimpleImageCommandService`
  - `createSimpleImages` → FileUtils#saveFile, SimpleImageRepository#save~
  - `deleteAllByEvent` → SimpleImageRepository#deleteByEventId
  - `deleteSimpleImage` → SimpleImageRepository#findById~, FileUtils#deleteFile, SimpleImageRepository#delete~
- `SimpleImageQueryService`
  - `findByEventId` → SimpleImageRepository#findByEventId
  - `findById` → SimpleImageRepository#findById~

## 3. 클래스 목록 (역할별)

### controller (2)

- `EventCommandController` controller/command/EventCommandController.java · public: createEvent, updateEvent, deleteEvent, updateEventStatus · 주입: EventCommandService, UserQueryService
- `EventQueryController` controller/query/EventQueryController.java · public: getAllEventsPaging, searchEvents, getActiveEvents, getEventsByStatus, getAllEvents, getEventsByBrand, getEventDetail, getEventImage · 주입: EventQueryService, FileUtils

### service-command (2)

- `EventCommandService` service/command/EventCommandService.java · public: createEvent, updateEvent, updateEventStatus, deleteEvent · 주입: EventRepository, SimpleImageCommandService, SimpleImageQueryService, SimpleImageRepository, BrandQueryService, FileUtils
- `SimpleImageCommandService` service/command/SimpleImageCommandService.java · public: createSimpleImages, deleteAllByEvent, deleteSimpleImage · 주입: SimpleImageRepository, FileUtils

### service-query (2)

- `EventQueryService` service/query/EventQueryService.java · public: findEventById, findActiveEvents, findEventsByBrandId, searchEvents, findEventsByStatus, findAllEventsWithPaging, findActiveEventsWithPaging, findAllEvents · 주입: EventRepository, SimpleImageQueryService, FileUtils
- `SimpleImageQueryService` service/query/SimpleImageQueryService.java · public: findByEventId, findById · 주입: SimpleImageRepository

### repository (3)

- `EventRepository` (interface) repository/EventRepository.java · extends/implements JpaRepository, EventRepositoryCustom · public: findByBrandIdOrderByStartDateDesc, findActiveEvents, findByStartDateBeforeAndEndDateAfter, findByEndDateLessThanEqual, findByStartDateAfterOrderByStartDateAsc, findByEndDateBeforeOrderByEndDateDesc, findAllWithBrand, findAllWithBrandPaging, findByIdWithBrand, findActiveEventsWithBrandPaging, findByBrandIdWithBrand, findByBrandIdWithBrandPaging, findByIdWithBrandAndSimpleImages
- `EventSearchRepository` repository/EventSearchRepository.java · public: searchEvents · 주입: JPAQueryFactory
- `SimpleImageRepository` (interface) repository/SimpleImageRepository.java · extends/implements JpaRepository · public: deleteByEventId, findByEventId

### custom (2)

- `EventRepositoryCustom` (interface) repository/EventRepositoryCustom.java · public: searchEvents
- `EventRepositoryImpl` repository/EventRepositoryImpl.java · extends/implements EventRepositoryCustom · public: searchEvents · 주입: JPAQueryFactory

### entity (3)

- `Event` entity/Event.java · extends/implements BaseTimeEntity · public: addSimpleImage, updateThumbnailFileName, updateEvent, isActive, isUpcoming, isEnded, getStatus, getStatusDisplayName
- `EventStatus` (enum) entity/EventStatus.java · public: getDisplayName
- `SimpleImage` entity/SimpleImage.java · public: assignEvent

### aop (5)

- `EventBusinessRuleAspect` aop/EventBusinessRuleAspect.java · public: applyBusinessRules · 주입: EventRepository, BrandQueryService
- `EventCachingAspect` aop/EventCachingAspect.java · public: manageCaching, invalidateCacheOnStatusChange, invalidateBrandCache, clearAllCaches, generateCacheReport · 주입: CacheManager
- `EventFileManagementAspect` aop/EventFileManagementAspect.java · public: manageFiles · 주입: FileUtils
- `EventMonitoringAspect` aop/EventMonitoringAspect.java · public: monitorEvent
- `EventValidationAspect` aop/EventValidationAspect.java · public: validateEvent · 주입: EventRepository, BrandQueryService

### dto (5)

- `CreateEventRequest` dto/CreateEventRequest.java
- `EventDetailDto` dto/EventDetailDto.java
- `EventListDto` dto/EventListDto.java
- `EventSearchCondition` dto/EventSearchCondition.java
- `UpdateEventRequest` dto/UpdateEventRequest.java

### exception (5)

- `EventErrorCode` exception/EventErrorCode.java
- `EventException` exception/EventException.java
- `EventNotFoundException` exception/EventNotFoundException.java
- `ImageNotFoundException` exception/ImageNotFoundException.java
- `InvalidEventDateException` exception/InvalidEventDateException.java

### other (6)

- `EventBusinessRule` (@interface) aop/annotation/EventBusinessRule.java
- `EventCaching` (@interface) aop/annotation/EventCaching.java
- `EventFileManagement` (@interface) aop/annotation/EventFileManagement.java
- `EventMonitoring` (@interface) aop/annotation/EventMonitoring.java
- `EventValidation` (@interface) aop/annotation/EventValidation.java
- `EventStatusScheduler` scheduler/EventStatusScheduler.java · public: checkEventStatus · 주입: EventRepository

### 다른 도메인 참조 (import·주입 기준)

- **product**: `Brand` ← Event, EventBusinessRuleAspect, EventCommandService, EventValidationAspect; `BrandQueryService` ← EventBusinessRuleAspect, EventCommandService, EventValidationAspect
- **user**: `UserQueryService` ← EventCommandController

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

- `Event` @Entity · extends BaseTimeEntity
  - simpleImages: @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true) → `SimpleImage`
  - brand: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "brand_id") → `Brand` [product]
- `SimpleImage` @Entity
  - event: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "event_id") → `Event`

## 5. 이벤트·메시징

(없음)

## 6. AOP

- `EventBusinessRuleAspect#applyBusinessRules` @Around("@annotation(businessRule)") · 바인딩: `@EventBusinessRule`
- `EventCachingAspect#manageCaching` @Around("@annotation(eventCaching)") · 바인딩: `@EventCaching`
- `EventFileManagementAspect#manageFiles` @Around("@annotation(fileManagement)") · 바인딩: `@EventFileManagement`
- `EventMonitoringAspect#monitorEvent` @Around("@annotation(eventMonitoring)") · 바인딩: `@EventMonitoring`
- `EventValidationAspect#validateEvent` @Around("@annotation(eventValidation)") · 바인딩: `@EventValidation`

어노테이션 적용 지점(메서드에 붙은 곳):

- `@EventBusinessRule`: (없음)
- `@EventCaching`: (없음)
- `@EventFileManagement`: (없음)
- `@EventMonitoring`: (없음)
- `@EventValidation`: (없음)

