# internalstyle 도메인

기준 경로: `src/main/java/com/fream/back/domain/internalstyle/` · 파일 39개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/internal/hashtags/commands` | `InternalHashtagCommandController#createHashtag` | `InternalHashtagCommandService#create` |
| PUT | `/internal/hashtags/commands/{id}` | `InternalHashtagCommandController#updateHashtag` | `InternalHashtagCommandService#update` |
| DELETE | `/internal/hashtags/commands/{id}` | `InternalHashtagCommandController#deleteHashtag` | `InternalHashtagCommandService#delete` |
| POST | `/internal/styles/commands` | `InternalStyleCommandController#createStyle` | `InternalStyleCommandService#createStyle`, `NginxCachePurgeUtil#purgeStyleCache` |
| POST | `/internal/styles/commands/{styleId}/view` | `InternalStyleCommandController#incrementViewCount` | `InternalStyleCommandService#incrementViewCount` |
| PUT | `/internal/styles/commands/{styleId}` | `InternalStyleCommandController#updateStyle` | `InternalStyleCommandService#updateStyle`, `NginxCachePurgeUtil#purgeStyleCache` |
| DELETE | `/internal/styles/commands/{styleId}` | `InternalStyleCommandController#deleteStyle` | `InternalStyleCommandService#deleteStyle`, `NginxCachePurgeUtil#purgeStyleCache` |
| POST | `/internal/styles/comments/commands` | `InternalStyleCommentCommandController#addComment` | `InternalStyleCommentCommandService#addComment` |
| PUT | `/internal/styles/comments/commands/{commentId}` | `InternalStyleCommentCommandController#updateComment` | `InternalStyleCommentCommandService#updateComment` |
| DELETE | `/internal/styles/comments/commands/{commentId}` | `InternalStyleCommentCommandController#deleteComment` | `InternalStyleCommentCommandService#deleteComment` |
| POST | `/internal/styles/comments/likes/commands/{commentId}/toggle` | `InternalStyleCommentLikeCommandController#toggleCommentLike` | `InternalStyleCommentLikeCommandService#toggleCommentLike` |
| POST | `/internal/styles/interests/commands/{styleId}/toggle` | `InternalStyleInterestCommandController#toggleInterest` | `InternalStyleInterestCommandService#toggleStyleInterest` |
| POST | `/internal/styles/likes/commands/{styleId}/toggle` | `InternalStyleLikeCommandController#toggleLike` | `InternalStyleLikeCommandService#toggleLike` |
| GET | `/internal/hashtags/queries/{id}` | `InternalHashtagQueryController#getHashtagById` | `InternalHashtagQueryService#findById` |
| GET | `/internal/hashtags/queries/name/{name}` | `InternalHashtagQueryController#getHashtagByName` | `InternalHashtagQueryService#findByName` |
| GET | `/internal/hashtags/queries` | `InternalHashtagQueryController#getAllHashtags` | `InternalHashtagQueryService#findAll` |
| GET | `/internal/hashtags/queries/popular` | `InternalHashtagQueryController#getPopularHashtags` | `InternalHashtagQueryService#findPopular` |
| GET | `/internal/hashtags/queries/search` | `InternalHashtagQueryController#searchHashtags` | `InternalHashtagQueryService#search` |
| GET | `/internal/hashtags/queries/autocomplete` | `InternalHashtagQueryController#autocompleteHashtags` | `InternalHashtagQueryService#autocomplete`, `InternalHashtagQueryService#smartAutocomplete` |
| GET | `/internal/styles/comments/queries/{styleId}` | `InternalStyleCommentQueryController#getCommentsByStyleId` | `InternalStyleCommentQueryService#getCommentsByStyleId` |
| GET | `/internal/styles/queries/{styleId}/hashtags` | `InternalStyleHashtagQueryController#getStyleHashtags` | `InternalStyleHashtagQueryService#getHashtagsByStyleId` |
| GET | `/internal/styles/queries/{styleId}` | `InternalStyleQueryController#getStyleDetail` | `InternalStyleQueryService#getStyleDetail`, `InternalStyleViewEventProducer#sendViewEvent` |
| GET | `/internal/styles/queries` | `InternalStyleQueryController#getFilteredStyles` | `InternalStyleQueryService#getFilteredStyles` |
| GET | `/internal/styles/queries/profile/{profileId}` | `InternalStyleQueryController#getStylesByProfile` | `InternalStyleQueryService#getStylesByProfile` |
| GET | `/internal/styles/queries/{styleId}/media/{fileName}` | `InternalStyleQueryController#getStyleMedia` |  |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `InternalHashtagCommandService`
  - `create` → InternalHashtagRepository#existsByName, InternalHashtagRepository#save~, this#convertToDto
  - `update` → InternalHashtagRepository#findById~, InternalHashtagRepository#existsByName, InternalHashtagRepository#save~, this#convertToDto
  - `delete` → InternalHashtagRepository#findById~, InternalStyleHashtagRepository#countByHashtagId, InternalHashtagRepository#delete~
- `InternalHashtagQueryService`
  - `findById` → InternalHashtagRepository#findById~, this#convertToDto
  - `findByName` → InternalHashtagRepository#findByName, this#convertToDto
  - `findAll` → InternalHashtagRepository#findAll~
  - `findPopular` → InternalHashtagRepository#findAllByOrderByCountDesc
  - `search` → InternalHashtagRepository#findByNameContainingOrderByCountDesc
  - `autocomplete` → this#isKoreanChosung, this#chosungAutocomplete, this#koreanAutocomplete, this#englishAutocomplete
  - `englishAutocomplete` → InternalHashtagRepository#findByNameStartingWithOrderByCountDesc
  - `koreanAutocomplete` → InternalHashtagRepository#findByNameContainingKeywordOrderByCountDesc
  - `chosungAutocomplete` → InternalHashtagRepository#findAll~, this#extractChosung
  - `smartAutocomplete` → this#containsKoreanCharacters, this#autocomplete
- `InternalMediaUrlCommandService`
  - `saveMediaFile` → this#validateMediaFile, FileUtils#saveFile, MediaUrlRepository#save~
  - `deleteMediaUrl` → FileUtils#deleteFile, MediaUrlRepository#delete~
- `InternalMediaUrlQueryService`
  - `findMediaUrlsByStyleId` → MediaUrlRepository#findByStyleId
  - `findMediaUrlById` → MediaUrlRepository#findById~
  - `findMediaUrlsByUrl` → MediaUrlRepository#findByUrl
- `InternalStyleCommandService`
  - `createStyle` → InternalProfileQueryService#getProfileByEmail, StyleRepository#save~, InternalStyleOrderItemCommandService#createStyleOrderItem, InternalMediaUrlCommandService#saveMediaFile, this#processHashtags
  - `processHashtags` → InternalHashtagRepository#findByName, InternalHashtagRepository#findById~, InternalHashtagCommandService#create, InternalStyleHashtagCommandService#addHashtagToStyle
  - `updateStyle` → StyleRepository#findById~, InternalMediaUrlCommandService#deleteMediaUrl, InternalMediaUrlCommandService#saveMediaFile, InternalStyleHashtagCommandService#removeAllHashtagsFromStyle, this#processHashtags, StyleRepository#save~
  - `deleteStyle` → StyleRepository#findById~, InternalStyleHashtagCommandService#removeAllHashtagsFromStyle, InternalMediaUrlCommandService#deleteMediaUrl, StyleRepository#delete~
  - `incrementViewCount` → StyleRepository#findById~, StyleRepository#save~
- `InternalStyleCommentCommandService`
  - `addComment` → InternalProfileQueryService#getProfileByEmail, InternalStyleQueryService#findStyleById, InternalStyleCommentRepository#findById~, InternalStyleCommentRepository#save~
  - `updateComment` → InternalStyleCommentRepository#findById~, InternalStyleCommentRepository#save~
  - `deleteComment` → InternalStyleCommentRepository#findById~, InternalStyleCommentRepository#delete~
- `InternalStyleCommentLikeCommandService`
  - `toggleCommentLike` → InternalProfileQueryService#getProfileByEmail, InternalStyleCommentQueryService#findById, InternalStyleCommentLikeRepository#findByCommentAndProfile, InternalStyleCommentLikeRepository#delete~, InternalStyleCommentLikeRepository#save~
- `InternalStyleCommentLikeQueryService`
  - `isCommentLikedByProfile` → InternalStyleCommentLikeRepository#existsByCommentIdAndProfileId
  - `findByCommentId` → InternalStyleCommentLikeRepository#findByCommentId
  - `getLikedCommentIds` → InternalProfileQueryService#getProfileByEmail, InternalStyleCommentLikeRepository#findByProfileIdAndCommentIdIn
- `InternalStyleCommentQueryService`
  - `getCommentsByStyleId` → InternalStyleCommentRepository#findRootCommentsByStyleId, InternalStyleCommentRepository#countCommentsByStyleId, InternalStyleCommentRepository#findRepliesByParentCommentId, InternalStyleCommentLikeQueryService#getLikedCommentIds, this#convertToResponseDtos, InternalProfileQueryService#getProfileByEmail
  - `findCommentById` → InternalStyleCommentRepository#findById~
  - `checkUserLikedComment` → InternalProfileQueryService#getProfileByEmail, InternalStyleCommentLikeQueryService#isCommentLikedByProfile
  - `findByStyleId` → InternalStyleCommentRepository#findByStyleId
  - `findById` → InternalStyleCommentRepository#findById~
  - `findByProfileId` → InternalStyleCommentRepository#findByProfileId
- `InternalStyleHashtagCommandService`
  - `addHashtagToStyle` → InternalStyleHashtagRepository#findByStyleIdAndHashtagId, InternalHashtagRepository#save~, InternalStyleHashtagRepository#save~
  - `removeAllHashtagsFromStyle` → InternalStyleHashtagRepository#findByStyleId, InternalHashtagRepository#save~, InternalStyleHashtagRepository#deleteByStyleId
  - `removeHashtagFromStyle` → InternalStyleHashtagRepository#findByStyleIdAndHashtagId, InternalHashtagRepository#save~, InternalStyleHashtagRepository#delete~
- `InternalStyleHashtagQueryService`
  - `getHashtagsByStyleId` → InternalStyleHashtagRepository#findByStyleId
  - `getHashtagsByStyleIds` → InternalStyleHashtagRepository#findByStyleIdIn
  - `getHashtagMapByStyleIds` → InternalStyleHashtagRepository#findByStyleIdIn
- `InternalStyleInterestCommandService`
  - `toggleStyleInterest` → InternalProfileQueryService#getProfileByEmail, InternalStyleQueryService#findStyleById, InternalStyleInterestRepository#findByStyleAndProfile, InternalStyleInterestRepository#delete~, InternalStyleInterestRepository#save~
- `InternalStyleInterestQueryService`
  - `isStyleInterestedByProfile` → InternalStyleInterestRepository#existsByStyleIdAndProfileId
  - `findByStyleId` → InternalStyleInterestRepository#findByStyleId
  - `getInterestedStyleIds` → InternalStyleInterestRepository#findByProfileIdAndStyleIdIn
  - `checkUserInterested` → InternalProfileQueryService#getProfileByEmail, InternalStyleInterestRepository#existsByStyleIdAndProfileId
- `InternalStyleLikeCommandService`
  - `toggleLike` → InternalProfileQueryService#getProfileByEmail, InternalStyleQueryService#findStyleById, InternalStyleLikeRepository#findByStyleAndProfile, InternalStyleLikeRepository#delete~, InternalStyleLikeRepository#save~
- `InternalStyleLikeQueryService`
  - `checkUserLiked` → InternalProfileQueryService#getProfileByEmail, InternalStyleLikeRepository#existsByStyleIdAndProfileId
  - `getLikedStyleIds` → InternalProfileQueryService#getProfileByEmail, InternalStyleLikeRepository#findByProfileIdAndStyleIdIn
  - `isLikedByProfile` → InternalStyleLikeRepository#existsByStyleIdAndProfileId
  - `findLikesByStyleId` → InternalStyleLikeRepository#findByStyleId
- `InternalStyleOrderItemCommandService`
  - `createStyleOrderItem` → InternalOrderItemQueryService#findById, StyleOrderItemRepository#save~
- `InternalStyleQueryService`
  - `findStyleById` → StyleRepository#findById~
  - `findStylesByProfileId` → StyleRepository#findByProfileId
  - `getFilteredStyles` → StyleRepository#filterStyles, InternalStyleHashtagQueryService#getHashtagMapByStyleIds, InternalProfileQueryService#getProfileByEmail, InternalStyleLikeQueryService#getLikedStyleIds, InternalStyleInterestQueryService#getInterestedStyleIds
  - `getStylesByProfile` → StyleRepository#getStylesByProfile, InternalStyleHashtagQueryService#getHashtagMapByStyleIds, InternalStyleLikeQueryService#getLikedStyleIds
  - `getStyleDetail` → StyleRepository#getStyleDetail, InternalStyleHashtagQueryService#getHashtagsByStyleId, InternalProfileQueryService#getProfileByEmail, InternalStyleLikeQueryService#isLikedByProfile, InternalStyleInterestQueryService#isStyleInterestedByProfile
- `InternalStyleViewEventConsumer`
  - `listen` → StyleRepository#findById~, StyleRepository#save~, this#flushBuffer
  - `flushBuffer` → InternalStyleViewLogRepository#saveAll~
- `InternalStyleViewEventKafkaConfig`
  - `styleViewEventKafkaTemplate` → this#styleViewEventProducerFactory
  - `styleViewEventKafkaListenerContainerFactory` → this#styleViewEventConsumerFactory
- `InternalStyleViewEventProducer`
  - `sendViewEvent` → kafka:style-view-log-topic

## 3. 클래스 목록 (역할별)

### controller (10)

- `InternalHashtagCommandController` controller/command/InternalHashtagCommandController.java · public: createHashtag, updateHashtag, deleteHashtag · 주입: InternalHashtagCommandService
- `InternalStyleCommandController` controller/command/InternalStyleCommandController.java · public: createStyle, incrementViewCount, updateStyle, deleteStyle · 주입: InternalStyleCommandService, NginxCachePurgeUtil
- `InternalStyleCommentCommandController` controller/command/InternalStyleCommentCommandController.java · public: addComment, updateComment, deleteComment · 주입: InternalStyleCommentCommandService
- `InternalStyleCommentLikeCommandController` controller/command/InternalStyleCommentLikeCommandController.java · public: toggleCommentLike · 주입: InternalStyleCommentLikeCommandService
- `InternalStyleInterestCommandController` controller/command/InternalStyleInterestCommandController.java · public: toggleInterest · 주입: InternalStyleInterestCommandService
- `InternalStyleLikeCommandController` controller/command/InternalStyleLikeCommandController.java · public: toggleLike · 주입: InternalStyleLikeCommandService
- `InternalHashtagQueryController` controller/query/InternalHashtagQueryController.java · public: getHashtagById, getHashtagByName, getAllHashtags, getPopularHashtags, searchHashtags, autocompleteHashtags · 주입: InternalHashtagQueryService
- `InternalStyleCommentQueryController` controller/query/InternalStyleCommentQueryController.java · public: getCommentsByStyleId · 주입: InternalStyleCommentQueryService
- `InternalStyleHashtagQueryController` controller/query/InternalStyleHashtagQueryController.java · public: getStyleHashtags · 주입: InternalStyleHashtagQueryService
- `InternalStyleQueryController` controller/query/InternalStyleQueryController.java · public: getStyleDetail, getFilteredStyles, getStylesByProfile, getStyleMedia · 주입: InternalStyleQueryService, InternalStyleViewEventProducer

### service-command (9)

- `InternalHashtagCommandService` service/command/InternalHashtagCommandService.java · public: create, update, delete · 주입: InternalHashtagRepository, InternalStyleHashtagRepository
- `InternalMediaUrlCommandService` service/command/InternalMediaUrlCommandService.java · public: saveMediaFile, deleteMediaUrl · 주입: MediaUrlRepository, FileUtils
- `InternalStyleCommandService` service/command/InternalStyleCommandService.java · public: createStyle, updateStyle, deleteStyle, incrementViewCount · 주입: StyleRepository, InternalProfileQueryService, InternalStyleOrderItemCommandService, InternalMediaUrlCommandService, InternalHashtagRepository, InternalHashtagCommandService, InternalStyleHashtagCommandService
- `InternalStyleCommentCommandService` service/command/InternalStyleCommentCommandService.java · public: addComment, updateComment, deleteComment · 주입: InternalStyleCommentRepository, InternalProfileQueryService, InternalStyleQueryService
- `InternalStyleCommentLikeCommandService` service/command/InternalStyleCommentLikeCommandService.java · public: toggleCommentLike · 주입: InternalStyleCommentLikeRepository, InternalProfileQueryService, InternalStyleCommentQueryService
- `InternalStyleHashtagCommandService` service/command/InternalStyleHashtagCommandService.java · public: addHashtagToStyle, removeAllHashtagsFromStyle, removeHashtagFromStyle · 주입: InternalStyleHashtagRepository, InternalHashtagRepository
- `InternalStyleInterestCommandService` service/command/InternalStyleInterestCommandService.java · public: toggleStyleInterest · 주입: InternalStyleInterestRepository, InternalProfileQueryService, InternalStyleQueryService
- `InternalStyleLikeCommandService` service/command/InternalStyleLikeCommandService.java · public: toggleLike · 주입: InternalStyleLikeRepository, InternalProfileQueryService, InternalStyleQueryService
- `InternalStyleOrderItemCommandService` service/command/InternalStyleOrderItemCommandService.java · public: createStyleOrderItem · 주입: StyleOrderItemRepository, InternalOrderItemQueryService

### service-query (8)

- `InternalHashtagQueryService` service/query/InternalHashtagQueryService.java · public: findById, findByName, findAll, findPopular, search, autocomplete, smartAutocomplete · 주입: InternalHashtagRepository
- `InternalMediaUrlQueryService` service/query/InternalMediaUrlQueryService.java · public: findMediaUrlsByStyleId, findMediaUrlById, findMediaUrlsByUrl · 주입: MediaUrlRepository
- `InternalStyleCommentLikeQueryService` service/query/InternalStyleCommentLikeQueryService.java · public: isCommentLikedByProfile, findByCommentId, getLikedCommentIds · 주입: InternalStyleCommentLikeRepository, InternalProfileQueryService
- `InternalStyleCommentQueryService` service/query/InternalStyleCommentQueryService.java · public: getCommentsByStyleId, findCommentById, checkUserLikedComment, findByStyleId, findById, findByProfileId · 주입: InternalStyleCommentRepository, InternalStyleCommentLikeQueryService, InternalProfileQueryService
- `InternalStyleHashtagQueryService` service/query/InternalStyleHashtagQueryService.java · public: getHashtagsByStyleId, getHashtagsByStyleIds, getHashtagMapByStyleIds · 주입: InternalStyleHashtagRepository
- `InternalStyleInterestQueryService` service/query/InternalStyleInterestQueryService.java · public: isStyleInterestedByProfile, findByStyleId, getInterestedStyleIds, checkUserInterested · 주입: InternalStyleInterestRepository, InternalProfileQueryService
- `InternalStyleLikeQueryService` service/query/InternalStyleLikeQueryService.java · public: checkUserLiked, getLikedStyleIds, isLikedByProfile, findLikesByStyleId · 주입: InternalStyleLikeRepository, InternalProfileQueryService
- `InternalStyleQueryService` service/query/InternalStyleQueryService.java · public: findStyleById, findStylesByProfileId, getFilteredStyles, getStylesByProfile, getStyleDetail · 주입: StyleRepository, InternalStyleLikeQueryService, InternalStyleInterestQueryService, InternalStyleHashtagQueryService, InternalProfileQueryService

### repository (7)

- `InternalHashtagRepository` (interface) repository/InternalHashtagRepository.java · extends/implements JpaRepository · public: findByName, findByNameStartingWithOrderByCountDesc, findByNameContainingKeywordOrderByCountDesc, findAllByOrderByCountDesc, findByNameContainingOrderByCountDesc, existsByName
- `InternalStyleCommentLikeRepository` (interface) repository/InternalStyleCommentLikeRepository.java · extends/implements JpaRepository · public: findByCommentAndProfile, findByCommentIdAndProfileId, existsByCommentIdAndProfileId, findByCommentId, findByProfileIdAndCommentIdIn, findCommentIdsByProfileIdAndCommentIdIn, countByCommentId, deleteByProfileIdAndCommentId
- `InternalStyleCommentRepository` (interface) repository/InternalStyleCommentRepository.java · extends/implements JpaRepository · public: findRootCommentsByStyleId, countCommentsByStyleId, findRepliesByParentCommentId, findByStyleId, findByProfileId
- `InternalStyleHashtagRepository` (interface) repository/InternalStyleHashtagRepository.java · extends/implements JpaRepository · public: findByStyleId, findByStyleIdIn, findStyleIdsByHashtagId, deleteByStyleId, findByStyleIdAndHashtagId, countByHashtagId
- `InternalStyleInterestRepository` (interface) repository/InternalStyleInterestRepository.java · extends/implements JpaRepository · public: existsByStyleIdAndProfileId, findByStyleId, findByStyleAndProfile, findByStyleIdAndProfileId, findByProfileIdAndStyleIdIn, findStyleIdsByProfileIdAndStyleIdIn, countByStyleId, deleteByProfileIdAndStyleId
- `InternalStyleLikeRepository` (interface) repository/InternalStyleLikeRepository.java · extends/implements JpaRepository · public: findByStyleAndProfile, existsByStyleIdAndProfileId, findByStyleId, findByProfileIdAndStyleId, findByProfileIdAndStyleIdIn, findStyleIdsByProfileIdAndStyleIdIn, countByStyleId, deleteByProfileIdAndStyleId
- `InternalStyleViewLogRepository` (interface) repository/InternalStyleViewLogRepository.java · extends/implements JpaRepository

### custom (2)

- `InternalStyleRepositoryCustom` (interface) repository/InternalStyleRepositoryCustom.java · public: filterStyles, getStyleDetail, getStylesByProfile, styleCountByColorIds
- `InternalStyleRepositoryCustomImpl` repository/InternalStyleRepositoryCustomImpl.java · extends/implements InternalStyleRepositoryCustom · public: filterStyles, getStyleDetail, getStylesByProfile, styleCountByColorIds · 주입: JPAQueryFactory

### event (2)

- `InternalStyleViewEventConsumer` service/kafka/InternalStyleViewEventConsumer.java · public: listen, flushBuffer · 주입: StyleRepository, InternalStyleViewLogRepository
- `InternalStyleViewEventProducer` service/kafka/InternalStyleViewEventProducer.java · public: sendViewEvent · 주입: KafkaTemplate

### config (1)

- `InternalStyleViewEventKafkaConfig` service/kafka/config/InternalStyleViewEventKafkaConfig.java · public: styleViewEventProducerFactory, styleViewEventKafkaTemplate, styleViewEventConsumerFactory, styleViewEventKafkaListenerContainerFactory

### 다른 도메인 참조 (import·주입 기준)

- **internalorder**: `InternalOrderItemQueryService` ← InternalStyleOrderItemCommandService
- **internaluser**: `InternalProfileQueryService` ← InternalStyleCommandService, InternalStyleCommentCommandService, InternalStyleCommentLikeCommandService, InternalStyleCommentLikeQueryService, InternalStyleCommentQueryService, InternalStyleInterestCommandService, InternalStyleInterestQueryService, InternalStyleLikeCommandService, InternalStyleLikeQueryService, InternalStyleQueryService
- **order**: `OrderItem` ← InternalStyleOrderItemCommandService
- **style**: `Hashtag` ← InternalHashtagCommandService, InternalHashtagQueryService, InternalHashtagRepository, InternalStyleCommandService, InternalStyleHashtagCommandService; `MediaUrl` ← InternalMediaUrlCommandService, InternalMediaUrlQueryService, InternalStyleCommandService; `MediaUrlRepository` ← InternalMediaUrlCommandService, InternalMediaUrlQueryService; `Style` ← InternalMediaUrlCommandService, InternalStyleCommandController, InternalStyleCommandService, InternalStyleCommentCommandService, InternalStyleHashtagCommandService, InternalStyleInterestCommandService, InternalStyleInterestRepository, InternalStyleLikeCommandService, InternalStyleLikeRepository, InternalStyleOrderItemCommandService, InternalStyleQueryService, InternalStyleViewEventConsumer; `StyleComment` ← InternalStyleCommentCommandController, InternalStyleCommentCommandService, InternalStyleCommentLikeCommandService, InternalStyleCommentLikeRepository, InternalStyleCommentQueryService, InternalStyleCommentRepository; `StyleCommentLike` ← InternalStyleCommentLikeCommandService, InternalStyleCommentLikeQueryService, InternalStyleCommentLikeRepository; `StyleHashtag` ← InternalStyleHashtagCommandService, InternalStyleHashtagQueryService, InternalStyleHashtagRepository; `StyleInterest` ← InternalStyleInterestCommandService, InternalStyleInterestQueryService, InternalStyleInterestRepository; `StyleLike` ← InternalStyleLikeCommandService, InternalStyleLikeQueryService, InternalStyleLikeRepository; `StyleOrderItem` ← InternalStyleCommandService, InternalStyleOrderItemCommandService; `StyleOrderItemRepository` ← InternalStyleOrderItemCommandService; `StyleRepository` ← InternalStyleCommandService, InternalStyleQueryService, InternalStyleViewEventConsumer; `StyleViewEvent` ← InternalStyleViewEventConsumer, InternalStyleViewEventKafkaConfig, InternalStyleViewEventProducer; `StyleViewLog` ← InternalStyleViewEventConsumer, InternalStyleViewLogRepository
- **user**: `Gender` ← InternalStyleQueryController, InternalStyleViewEventProducer; `Profile` ← InternalStyleCommandService, InternalStyleCommentCommandService, InternalStyleCommentLikeCommandService, InternalStyleCommentLikeQueryService, InternalStyleCommentLikeRepository, InternalStyleCommentQueryService, InternalStyleInterestCommandService, InternalStyleInterestQueryService, InternalStyleInterestRepository, InternalStyleLikeCommandService, InternalStyleLikeQueryService, InternalStyleLikeRepository, InternalStyleQueryService

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

- 발행 kafka `style-view-log-topic`: `InternalStyleViewEventProducer#sendViewEvent` → `AdminStyleViewEventConsumer#listen` [adminstyle], `InternalStyleViewEventConsumer#listen` [internalstyle], `LegacyStyleViewEventConsumer#listen` [legacystyle], `PartnerStyleViewEventConsumer#listen` [partnerstyle], `SettlementStyleViewEventConsumer#listen` [settlementstyle], `StyleViewEventConsumer#listen` [style]
- 구독 kafka `style-view-log-topic`: `InternalStyleViewEventConsumer#listen` (groupId=style-view-group containerFactory=styleViewEventKafkaListenerContainerFactory) ← `AdminStyleViewEventProducer#sendViewEvent` [adminstyle], `InternalStyleViewEventProducer#sendViewEvent` [internalstyle], `LegacyStyleViewEventProducer#sendViewEvent` [legacystyle], `PartnerStyleViewEventProducer#sendViewEvent` [partnerstyle], `SettlementStyleViewEventProducer#sendViewEvent` [settlementstyle], `StyleViewEventProducer#sendViewEvent` [style]

## 6. AOP

(없음)

