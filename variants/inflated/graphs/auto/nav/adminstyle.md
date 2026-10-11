# adminstyle 도메인

기준 경로: `src/main/java/com/fream/back/domain/adminstyle/` · 파일 39개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/admin/hashtags/commands` | `AdminHashtagCommandController#createHashtag` | `AdminHashtagCommandService#create` |
| PUT | `/admin/hashtags/commands/{id}` | `AdminHashtagCommandController#updateHashtag` | `AdminHashtagCommandService#update` |
| DELETE | `/admin/hashtags/commands/{id}` | `AdminHashtagCommandController#deleteHashtag` | `AdminHashtagCommandService#delete` |
| POST | `/admin/styles/commands` | `AdminStyleCommandController#createStyle` | `AdminStyleCommandService#createStyle`, `NginxCachePurgeUtil#purgeStyleCache` |
| POST | `/admin/styles/commands/{styleId}/view` | `AdminStyleCommandController#incrementViewCount` | `AdminStyleCommandService#incrementViewCount` |
| PUT | `/admin/styles/commands/{styleId}` | `AdminStyleCommandController#updateStyle` | `AdminStyleCommandService#updateStyle`, `NginxCachePurgeUtil#purgeStyleCache` |
| DELETE | `/admin/styles/commands/{styleId}` | `AdminStyleCommandController#deleteStyle` | `AdminStyleCommandService#deleteStyle`, `NginxCachePurgeUtil#purgeStyleCache` |
| POST | `/admin/styles/comments/commands` | `AdminStyleCommentCommandController#addComment` | `AdminStyleCommentCommandService#addComment` |
| PUT | `/admin/styles/comments/commands/{commentId}` | `AdminStyleCommentCommandController#updateComment` | `AdminStyleCommentCommandService#updateComment` |
| DELETE | `/admin/styles/comments/commands/{commentId}` | `AdminStyleCommentCommandController#deleteComment` | `AdminStyleCommentCommandService#deleteComment` |
| POST | `/admin/styles/comments/likes/commands/{commentId}/toggle` | `AdminStyleCommentLikeCommandController#toggleCommentLike` | `AdminStyleCommentLikeCommandService#toggleCommentLike` |
| POST | `/admin/styles/interests/commands/{styleId}/toggle` | `AdminStyleInterestCommandController#toggleInterest` | `AdminStyleInterestCommandService#toggleStyleInterest` |
| POST | `/admin/styles/likes/commands/{styleId}/toggle` | `AdminStyleLikeCommandController#toggleLike` | `AdminStyleLikeCommandService#toggleLike` |
| GET | `/admin/hashtags/queries/{id}` | `AdminHashtagQueryController#getHashtagById` | `AdminHashtagQueryService#findById` |
| GET | `/admin/hashtags/queries/name/{name}` | `AdminHashtagQueryController#getHashtagByName` | `AdminHashtagQueryService#findByName` |
| GET | `/admin/hashtags/queries` | `AdminHashtagQueryController#getAllHashtags` | `AdminHashtagQueryService#findAll` |
| GET | `/admin/hashtags/queries/popular` | `AdminHashtagQueryController#getPopularHashtags` | `AdminHashtagQueryService#findPopular` |
| GET | `/admin/hashtags/queries/search` | `AdminHashtagQueryController#searchHashtags` | `AdminHashtagQueryService#search` |
| GET | `/admin/hashtags/queries/autocomplete` | `AdminHashtagQueryController#autocompleteHashtags` | `AdminHashtagQueryService#autocomplete`, `AdminHashtagQueryService#smartAutocomplete` |
| GET | `/admin/styles/comments/queries/{styleId}` | `AdminStyleCommentQueryController#getCommentsByStyleId` | `AdminStyleCommentQueryService#getCommentsByStyleId` |
| GET | `/admin/styles/queries/{styleId}/hashtags` | `AdminStyleHashtagQueryController#getStyleHashtags` | `AdminStyleHashtagQueryService#getHashtagsByStyleId` |
| GET | `/admin/styles/queries/{styleId}` | `AdminStyleQueryController#getStyleDetail` | `AdminStyleQueryService#getStyleDetail`, `AdminStyleViewEventProducer#sendViewEvent` |
| GET | `/admin/styles/queries` | `AdminStyleQueryController#getFilteredStyles` | `AdminStyleQueryService#getFilteredStyles` |
| GET | `/admin/styles/queries/profile/{profileId}` | `AdminStyleQueryController#getStylesByProfile` | `AdminStyleQueryService#getStylesByProfile` |
| GET | `/admin/styles/queries/{styleId}/media/{fileName}` | `AdminStyleQueryController#getStyleMedia` |  |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `AdminHashtagCommandService`
  - `create` → AdminHashtagRepository#existsByName, AdminHashtagRepository#save~, this#convertToDto
  - `update` → AdminHashtagRepository#findById~, AdminHashtagRepository#existsByName, AdminHashtagRepository#save~, this#convertToDto
  - `delete` → AdminHashtagRepository#findById~, AdminStyleHashtagRepository#countByHashtagId, AdminHashtagRepository#delete~
- `AdminHashtagQueryService`
  - `findById` → AdminHashtagRepository#findById~, this#convertToDto
  - `findByName` → AdminHashtagRepository#findByName, this#convertToDto
  - `findAll` → AdminHashtagRepository#findAll~
  - `findPopular` → AdminHashtagRepository#findAllByOrderByCountDesc
  - `search` → AdminHashtagRepository#findByNameContainingOrderByCountDesc
  - `autocomplete` → this#isKoreanChosung, this#chosungAutocomplete, this#koreanAutocomplete, this#englishAutocomplete
  - `englishAutocomplete` → AdminHashtagRepository#findByNameStartingWithOrderByCountDesc
  - `koreanAutocomplete` → AdminHashtagRepository#findByNameContainingKeywordOrderByCountDesc
  - `chosungAutocomplete` → AdminHashtagRepository#findAll~, this#extractChosung
  - `smartAutocomplete` → this#containsKoreanCharacters, this#autocomplete
- `AdminMediaUrlCommandService`
  - `saveMediaFile` → this#validateMediaFile, FileUtils#saveFile, MediaUrlRepository#save~
  - `deleteMediaUrl` → FileUtils#deleteFile, MediaUrlRepository#delete~
- `AdminMediaUrlQueryService`
  - `findMediaUrlsByStyleId` → MediaUrlRepository#findByStyleId
  - `findMediaUrlById` → MediaUrlRepository#findById~
  - `findMediaUrlsByUrl` → MediaUrlRepository#findByUrl
- `AdminStyleCommandService`
  - `createStyle` → AdminProfileQueryService#getProfileByEmail, StyleRepository#save~, AdminStyleOrderItemCommandService#createStyleOrderItem, AdminMediaUrlCommandService#saveMediaFile, this#processHashtags
  - `processHashtags` → AdminHashtagRepository#findByName, AdminHashtagRepository#findById~, AdminHashtagCommandService#create, AdminStyleHashtagCommandService#addHashtagToStyle
  - `updateStyle` → StyleRepository#findById~, AdminMediaUrlCommandService#deleteMediaUrl, AdminMediaUrlCommandService#saveMediaFile, AdminStyleHashtagCommandService#removeAllHashtagsFromStyle, this#processHashtags, StyleRepository#save~
  - `deleteStyle` → StyleRepository#findById~, AdminStyleHashtagCommandService#removeAllHashtagsFromStyle, AdminMediaUrlCommandService#deleteMediaUrl, StyleRepository#delete~
  - `incrementViewCount` → StyleRepository#findById~, StyleRepository#save~
- `AdminStyleCommentCommandService`
  - `addComment` → AdminProfileQueryService#getProfileByEmail, AdminStyleQueryService#findStyleById, AdminStyleCommentRepository#findById~, AdminStyleCommentRepository#save~
  - `updateComment` → AdminStyleCommentRepository#findById~, AdminStyleCommentRepository#save~
  - `deleteComment` → AdminStyleCommentRepository#findById~, AdminStyleCommentRepository#delete~
- `AdminStyleCommentLikeCommandService`
  - `toggleCommentLike` → AdminProfileQueryService#getProfileByEmail, AdminStyleCommentQueryService#findById, AdminStyleCommentLikeRepository#findByCommentAndProfile, AdminStyleCommentLikeRepository#delete~, AdminStyleCommentLikeRepository#save~
- `AdminStyleCommentLikeQueryService`
  - `isCommentLikedByProfile` → AdminStyleCommentLikeRepository#existsByCommentIdAndProfileId
  - `findByCommentId` → AdminStyleCommentLikeRepository#findByCommentId
  - `getLikedCommentIds` → AdminProfileQueryService#getProfileByEmail, AdminStyleCommentLikeRepository#findByProfileIdAndCommentIdIn
- `AdminStyleCommentQueryService`
  - `getCommentsByStyleId` → AdminStyleCommentRepository#findRootCommentsByStyleId, AdminStyleCommentRepository#countCommentsByStyleId, AdminStyleCommentRepository#findRepliesByParentCommentId, AdminStyleCommentLikeQueryService#getLikedCommentIds, this#convertToResponseDtos, AdminProfileQueryService#getProfileByEmail
  - `findCommentById` → AdminStyleCommentRepository#findById~
  - `checkUserLikedComment` → AdminProfileQueryService#getProfileByEmail, AdminStyleCommentLikeQueryService#isCommentLikedByProfile
  - `findByStyleId` → AdminStyleCommentRepository#findByStyleId
  - `findById` → AdminStyleCommentRepository#findById~
  - `findByProfileId` → AdminStyleCommentRepository#findByProfileId
- `AdminStyleHashtagCommandService`
  - `addHashtagToStyle` → AdminStyleHashtagRepository#findByStyleIdAndHashtagId, AdminHashtagRepository#save~, AdminStyleHashtagRepository#save~
  - `removeAllHashtagsFromStyle` → AdminStyleHashtagRepository#findByStyleId, AdminHashtagRepository#save~, AdminStyleHashtagRepository#deleteByStyleId
  - `removeHashtagFromStyle` → AdminStyleHashtagRepository#findByStyleIdAndHashtagId, AdminHashtagRepository#save~, AdminStyleHashtagRepository#delete~
- `AdminStyleHashtagQueryService`
  - `getHashtagsByStyleId` → AdminStyleHashtagRepository#findByStyleId
  - `getHashtagsByStyleIds` → AdminStyleHashtagRepository#findByStyleIdIn
  - `getHashtagMapByStyleIds` → AdminStyleHashtagRepository#findByStyleIdIn
- `AdminStyleInterestCommandService`
  - `toggleStyleInterest` → AdminProfileQueryService#getProfileByEmail, AdminStyleQueryService#findStyleById, AdminStyleInterestRepository#findByStyleAndProfile, AdminStyleInterestRepository#delete~, AdminStyleInterestRepository#save~
- `AdminStyleInterestQueryService`
  - `isStyleInterestedByProfile` → AdminStyleInterestRepository#existsByStyleIdAndProfileId
  - `findByStyleId` → AdminStyleInterestRepository#findByStyleId
  - `getInterestedStyleIds` → AdminStyleInterestRepository#findByProfileIdAndStyleIdIn
  - `checkUserInterested` → AdminProfileQueryService#getProfileByEmail, AdminStyleInterestRepository#existsByStyleIdAndProfileId
- `AdminStyleLikeCommandService`
  - `toggleLike` → AdminProfileQueryService#getProfileByEmail, AdminStyleQueryService#findStyleById, AdminStyleLikeRepository#findByStyleAndProfile, AdminStyleLikeRepository#delete~, AdminStyleLikeRepository#save~
- `AdminStyleLikeQueryService`
  - `checkUserLiked` → AdminProfileQueryService#getProfileByEmail, AdminStyleLikeRepository#existsByStyleIdAndProfileId
  - `getLikedStyleIds` → AdminProfileQueryService#getProfileByEmail, AdminStyleLikeRepository#findByProfileIdAndStyleIdIn
  - `isLikedByProfile` → AdminStyleLikeRepository#existsByStyleIdAndProfileId
  - `findLikesByStyleId` → AdminStyleLikeRepository#findByStyleId
- `AdminStyleOrderItemCommandService`
  - `createStyleOrderItem` → AdminOrderItemQueryService#findById, StyleOrderItemRepository#save~
- `AdminStyleQueryService`
  - `findStyleById` → StyleRepository#findById~
  - `findStylesByProfileId` → StyleRepository#findByProfileId
  - `getFilteredStyles` → StyleRepository#filterStyles, AdminStyleHashtagQueryService#getHashtagMapByStyleIds, AdminProfileQueryService#getProfileByEmail, AdminStyleLikeQueryService#getLikedStyleIds, AdminStyleInterestQueryService#getInterestedStyleIds
  - `getStylesByProfile` → StyleRepository#getStylesByProfile, AdminStyleHashtagQueryService#getHashtagMapByStyleIds, AdminStyleLikeQueryService#getLikedStyleIds
  - `getStyleDetail` → StyleRepository#getStyleDetail, AdminStyleHashtagQueryService#getHashtagsByStyleId, AdminProfileQueryService#getProfileByEmail, AdminStyleLikeQueryService#isLikedByProfile, AdminStyleInterestQueryService#isStyleInterestedByProfile
- `AdminStyleViewEventConsumer`
  - `listen` → StyleRepository#findById~, StyleRepository#save~, this#flushBuffer
  - `flushBuffer` → AdminStyleViewLogRepository#saveAll~
- `AdminStyleViewEventKafkaConfig`
  - `styleViewEventKafkaTemplate` → this#styleViewEventProducerFactory
  - `styleViewEventKafkaListenerContainerFactory` → this#styleViewEventConsumerFactory
- `AdminStyleViewEventProducer`
  - `sendViewEvent` → kafka:style-view-log-topic

## 3. 클래스 목록 (역할별)

### controller (10)

- `AdminHashtagCommandController` controller/command/AdminHashtagCommandController.java · public: createHashtag, updateHashtag, deleteHashtag · 주입: AdminHashtagCommandService
- `AdminStyleCommandController` controller/command/AdminStyleCommandController.java · public: createStyle, incrementViewCount, updateStyle, deleteStyle · 주입: AdminStyleCommandService, NginxCachePurgeUtil
- `AdminStyleCommentCommandController` controller/command/AdminStyleCommentCommandController.java · public: addComment, updateComment, deleteComment · 주입: AdminStyleCommentCommandService
- `AdminStyleCommentLikeCommandController` controller/command/AdminStyleCommentLikeCommandController.java · public: toggleCommentLike · 주입: AdminStyleCommentLikeCommandService
- `AdminStyleInterestCommandController` controller/command/AdminStyleInterestCommandController.java · public: toggleInterest · 주입: AdminStyleInterestCommandService
- `AdminStyleLikeCommandController` controller/command/AdminStyleLikeCommandController.java · public: toggleLike · 주입: AdminStyleLikeCommandService
- `AdminHashtagQueryController` controller/query/AdminHashtagQueryController.java · public: getHashtagById, getHashtagByName, getAllHashtags, getPopularHashtags, searchHashtags, autocompleteHashtags · 주입: AdminHashtagQueryService
- `AdminStyleCommentQueryController` controller/query/AdminStyleCommentQueryController.java · public: getCommentsByStyleId · 주입: AdminStyleCommentQueryService
- `AdminStyleHashtagQueryController` controller/query/AdminStyleHashtagQueryController.java · public: getStyleHashtags · 주입: AdminStyleHashtagQueryService
- `AdminStyleQueryController` controller/query/AdminStyleQueryController.java · public: getStyleDetail, getFilteredStyles, getStylesByProfile, getStyleMedia · 주입: AdminStyleQueryService, AdminStyleViewEventProducer

### service-command (9)

- `AdminHashtagCommandService` service/command/AdminHashtagCommandService.java · public: create, update, delete · 주입: AdminHashtagRepository, AdminStyleHashtagRepository
- `AdminMediaUrlCommandService` service/command/AdminMediaUrlCommandService.java · public: saveMediaFile, deleteMediaUrl · 주입: MediaUrlRepository, FileUtils
- `AdminStyleCommandService` service/command/AdminStyleCommandService.java · public: createStyle, updateStyle, deleteStyle, incrementViewCount · 주입: StyleRepository, AdminProfileQueryService, AdminStyleOrderItemCommandService, AdminMediaUrlCommandService, AdminHashtagRepository, AdminHashtagCommandService, AdminStyleHashtagCommandService
- `AdminStyleCommentCommandService` service/command/AdminStyleCommentCommandService.java · public: addComment, updateComment, deleteComment · 주입: AdminStyleCommentRepository, AdminProfileQueryService, AdminStyleQueryService
- `AdminStyleCommentLikeCommandService` service/command/AdminStyleCommentLikeCommandService.java · public: toggleCommentLike · 주입: AdminStyleCommentLikeRepository, AdminProfileQueryService, AdminStyleCommentQueryService
- `AdminStyleHashtagCommandService` service/command/AdminStyleHashtagCommandService.java · public: addHashtagToStyle, removeAllHashtagsFromStyle, removeHashtagFromStyle · 주입: AdminStyleHashtagRepository, AdminHashtagRepository
- `AdminStyleInterestCommandService` service/command/AdminStyleInterestCommandService.java · public: toggleStyleInterest · 주입: AdminStyleInterestRepository, AdminProfileQueryService, AdminStyleQueryService
- `AdminStyleLikeCommandService` service/command/AdminStyleLikeCommandService.java · public: toggleLike · 주입: AdminStyleLikeRepository, AdminProfileQueryService, AdminStyleQueryService
- `AdminStyleOrderItemCommandService` service/command/AdminStyleOrderItemCommandService.java · public: createStyleOrderItem · 주입: StyleOrderItemRepository, AdminOrderItemQueryService

### service-query (8)

- `AdminHashtagQueryService` service/query/AdminHashtagQueryService.java · public: findById, findByName, findAll, findPopular, search, autocomplete, smartAutocomplete · 주입: AdminHashtagRepository
- `AdminMediaUrlQueryService` service/query/AdminMediaUrlQueryService.java · public: findMediaUrlsByStyleId, findMediaUrlById, findMediaUrlsByUrl · 주입: MediaUrlRepository
- `AdminStyleCommentLikeQueryService` service/query/AdminStyleCommentLikeQueryService.java · public: isCommentLikedByProfile, findByCommentId, getLikedCommentIds · 주입: AdminStyleCommentLikeRepository, AdminProfileQueryService
- `AdminStyleCommentQueryService` service/query/AdminStyleCommentQueryService.java · public: getCommentsByStyleId, findCommentById, checkUserLikedComment, findByStyleId, findById, findByProfileId · 주입: AdminStyleCommentRepository, AdminStyleCommentLikeQueryService, AdminProfileQueryService
- `AdminStyleHashtagQueryService` service/query/AdminStyleHashtagQueryService.java · public: getHashtagsByStyleId, getHashtagsByStyleIds, getHashtagMapByStyleIds · 주입: AdminStyleHashtagRepository
- `AdminStyleInterestQueryService` service/query/AdminStyleInterestQueryService.java · public: isStyleInterestedByProfile, findByStyleId, getInterestedStyleIds, checkUserInterested · 주입: AdminStyleInterestRepository, AdminProfileQueryService
- `AdminStyleLikeQueryService` service/query/AdminStyleLikeQueryService.java · public: checkUserLiked, getLikedStyleIds, isLikedByProfile, findLikesByStyleId · 주입: AdminStyleLikeRepository, AdminProfileQueryService
- `AdminStyleQueryService` service/query/AdminStyleQueryService.java · public: findStyleById, findStylesByProfileId, getFilteredStyles, getStylesByProfile, getStyleDetail · 주입: StyleRepository, AdminStyleLikeQueryService, AdminStyleInterestQueryService, AdminStyleHashtagQueryService, AdminProfileQueryService

### repository (7)

- `AdminHashtagRepository` (interface) repository/AdminHashtagRepository.java · extends/implements JpaRepository · public: findByName, findByNameStartingWithOrderByCountDesc, findByNameContainingKeywordOrderByCountDesc, findAllByOrderByCountDesc, findByNameContainingOrderByCountDesc, existsByName
- `AdminStyleCommentLikeRepository` (interface) repository/AdminStyleCommentLikeRepository.java · extends/implements JpaRepository · public: findByCommentAndProfile, findByCommentIdAndProfileId, existsByCommentIdAndProfileId, findByCommentId, findByProfileIdAndCommentIdIn, findCommentIdsByProfileIdAndCommentIdIn, countByCommentId, deleteByProfileIdAndCommentId
- `AdminStyleCommentRepository` (interface) repository/AdminStyleCommentRepository.java · extends/implements JpaRepository · public: findRootCommentsByStyleId, countCommentsByStyleId, findRepliesByParentCommentId, findByStyleId, findByProfileId
- `AdminStyleHashtagRepository` (interface) repository/AdminStyleHashtagRepository.java · extends/implements JpaRepository · public: findByStyleId, findByStyleIdIn, findStyleIdsByHashtagId, deleteByStyleId, findByStyleIdAndHashtagId, countByHashtagId
- `AdminStyleInterestRepository` (interface) repository/AdminStyleInterestRepository.java · extends/implements JpaRepository · public: existsByStyleIdAndProfileId, findByStyleId, findByStyleAndProfile, findByStyleIdAndProfileId, findByProfileIdAndStyleIdIn, findStyleIdsByProfileIdAndStyleIdIn, countByStyleId, deleteByProfileIdAndStyleId
- `AdminStyleLikeRepository` (interface) repository/AdminStyleLikeRepository.java · extends/implements JpaRepository · public: findByStyleAndProfile, existsByStyleIdAndProfileId, findByStyleId, findByProfileIdAndStyleId, findByProfileIdAndStyleIdIn, findStyleIdsByProfileIdAndStyleIdIn, countByStyleId, deleteByProfileIdAndStyleId
- `AdminStyleViewLogRepository` (interface) repository/AdminStyleViewLogRepository.java · extends/implements JpaRepository

### custom (2)

- `AdminStyleRepositoryCustom` (interface) repository/AdminStyleRepositoryCustom.java · public: filterStyles, getStyleDetail, getStylesByProfile, styleCountByColorIds
- `AdminStyleRepositoryCustomImpl` repository/AdminStyleRepositoryCustomImpl.java · extends/implements AdminStyleRepositoryCustom · public: filterStyles, getStyleDetail, getStylesByProfile, styleCountByColorIds · 주입: JPAQueryFactory

### event (2)

- `AdminStyleViewEventConsumer` service/kafka/AdminStyleViewEventConsumer.java · public: listen, flushBuffer · 주입: StyleRepository, AdminStyleViewLogRepository
- `AdminStyleViewEventProducer` service/kafka/AdminStyleViewEventProducer.java · public: sendViewEvent · 주입: KafkaTemplate

### config (1)

- `AdminStyleViewEventKafkaConfig` service/kafka/config/AdminStyleViewEventKafkaConfig.java · public: styleViewEventProducerFactory, styleViewEventKafkaTemplate, styleViewEventConsumerFactory, styleViewEventKafkaListenerContainerFactory

### 다른 도메인 참조 (import·주입 기준)

- **adminorder**: `AdminOrderItemQueryService` ← AdminStyleOrderItemCommandService
- **adminuser**: `AdminProfileQueryService` ← AdminStyleCommandService, AdminStyleCommentCommandService, AdminStyleCommentLikeCommandService, AdminStyleCommentLikeQueryService, AdminStyleCommentQueryService, AdminStyleInterestCommandService, AdminStyleInterestQueryService, AdminStyleLikeCommandService, AdminStyleLikeQueryService, AdminStyleQueryService
- **order**: `OrderItem` ← AdminStyleOrderItemCommandService
- **style**: `Hashtag` ← AdminHashtagCommandService, AdminHashtagQueryService, AdminHashtagRepository, AdminStyleCommandService, AdminStyleHashtagCommandService; `MediaUrl` ← AdminMediaUrlCommandService, AdminMediaUrlQueryService, AdminStyleCommandService; `MediaUrlRepository` ← AdminMediaUrlCommandService, AdminMediaUrlQueryService; `Style` ← AdminMediaUrlCommandService, AdminStyleCommandController, AdminStyleCommandService, AdminStyleCommentCommandService, AdminStyleHashtagCommandService, AdminStyleInterestCommandService, AdminStyleInterestRepository, AdminStyleLikeCommandService, AdminStyleLikeRepository, AdminStyleOrderItemCommandService, AdminStyleQueryService, AdminStyleViewEventConsumer; `StyleComment` ← AdminStyleCommentCommandController, AdminStyleCommentCommandService, AdminStyleCommentLikeCommandService, AdminStyleCommentLikeRepository, AdminStyleCommentQueryService, AdminStyleCommentRepository; `StyleCommentLike` ← AdminStyleCommentLikeCommandService, AdminStyleCommentLikeQueryService, AdminStyleCommentLikeRepository; `StyleHashtag` ← AdminStyleHashtagCommandService, AdminStyleHashtagQueryService, AdminStyleHashtagRepository; `StyleInterest` ← AdminStyleInterestCommandService, AdminStyleInterestQueryService, AdminStyleInterestRepository; `StyleLike` ← AdminStyleLikeCommandService, AdminStyleLikeQueryService, AdminStyleLikeRepository; `StyleOrderItem` ← AdminStyleCommandService, AdminStyleOrderItemCommandService; `StyleOrderItemRepository` ← AdminStyleOrderItemCommandService; `StyleRepository` ← AdminStyleCommandService, AdminStyleQueryService, AdminStyleViewEventConsumer; `StyleViewEvent` ← AdminStyleViewEventConsumer, AdminStyleViewEventKafkaConfig, AdminStyleViewEventProducer; `StyleViewLog` ← AdminStyleViewEventConsumer, AdminStyleViewLogRepository
- **user**: `Gender` ← AdminStyleQueryController, AdminStyleViewEventProducer; `Profile` ← AdminStyleCommandService, AdminStyleCommentCommandService, AdminStyleCommentLikeCommandService, AdminStyleCommentLikeQueryService, AdminStyleCommentLikeRepository, AdminStyleCommentQueryService, AdminStyleInterestCommandService, AdminStyleInterestQueryService, AdminStyleInterestRepository, AdminStyleLikeCommandService, AdminStyleLikeQueryService, AdminStyleLikeRepository, AdminStyleQueryService

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

- 발행 kafka `style-view-log-topic`: `AdminStyleViewEventProducer#sendViewEvent` → `AdminStyleViewEventConsumer#listen` [adminstyle], `InternalStyleViewEventConsumer#listen` [internalstyle], `LegacyStyleViewEventConsumer#listen` [legacystyle], `PartnerStyleViewEventConsumer#listen` [partnerstyle], `SettlementStyleViewEventConsumer#listen` [settlementstyle], `StyleViewEventConsumer#listen` [style]
- 구독 kafka `style-view-log-topic`: `AdminStyleViewEventConsumer#listen` (groupId=style-view-group containerFactory=styleViewEventKafkaListenerContainerFactory) ← `AdminStyleViewEventProducer#sendViewEvent` [adminstyle], `InternalStyleViewEventProducer#sendViewEvent` [internalstyle], `LegacyStyleViewEventProducer#sendViewEvent` [legacystyle], `PartnerStyleViewEventProducer#sendViewEvent` [partnerstyle], `SettlementStyleViewEventProducer#sendViewEvent` [settlementstyle], `StyleViewEventProducer#sendViewEvent` [style]

## 6. AOP

(없음)

