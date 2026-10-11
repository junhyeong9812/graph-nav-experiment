# partnerstyle 도메인

기준 경로: `src/main/java/com/fream/back/domain/partnerstyle/` · 파일 39개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/partner/hashtags/commands` | `PartnerHashtagCommandController#createHashtag` | `PartnerHashtagCommandService#create` |
| PUT | `/partner/hashtags/commands/{id}` | `PartnerHashtagCommandController#updateHashtag` | `PartnerHashtagCommandService#update` |
| DELETE | `/partner/hashtags/commands/{id}` | `PartnerHashtagCommandController#deleteHashtag` | `PartnerHashtagCommandService#delete` |
| POST | `/partner/styles/commands` | `PartnerStyleCommandController#createStyle` | `PartnerStyleCommandService#createStyle`, `NginxCachePurgeUtil#purgeStyleCache` |
| POST | `/partner/styles/commands/{styleId}/view` | `PartnerStyleCommandController#incrementViewCount` | `PartnerStyleCommandService#incrementViewCount` |
| PUT | `/partner/styles/commands/{styleId}` | `PartnerStyleCommandController#updateStyle` | `PartnerStyleCommandService#updateStyle`, `NginxCachePurgeUtil#purgeStyleCache` |
| DELETE | `/partner/styles/commands/{styleId}` | `PartnerStyleCommandController#deleteStyle` | `PartnerStyleCommandService#deleteStyle`, `NginxCachePurgeUtil#purgeStyleCache` |
| POST | `/partner/styles/comments/commands` | `PartnerStyleCommentCommandController#addComment` | `PartnerStyleCommentCommandService#addComment` |
| PUT | `/partner/styles/comments/commands/{commentId}` | `PartnerStyleCommentCommandController#updateComment` | `PartnerStyleCommentCommandService#updateComment` |
| DELETE | `/partner/styles/comments/commands/{commentId}` | `PartnerStyleCommentCommandController#deleteComment` | `PartnerStyleCommentCommandService#deleteComment` |
| POST | `/partner/styles/comments/likes/commands/{commentId}/toggle` | `PartnerStyleCommentLikeCommandController#toggleCommentLike` | `PartnerStyleCommentLikeCommandService#toggleCommentLike` |
| POST | `/partner/styles/interests/commands/{styleId}/toggle` | `PartnerStyleInterestCommandController#toggleInterest` | `PartnerStyleInterestCommandService#toggleStyleInterest` |
| POST | `/partner/styles/likes/commands/{styleId}/toggle` | `PartnerStyleLikeCommandController#toggleLike` | `PartnerStyleLikeCommandService#toggleLike` |
| GET | `/partner/hashtags/queries/{id}` | `PartnerHashtagQueryController#getHashtagById` | `PartnerHashtagQueryService#findById` |
| GET | `/partner/hashtags/queries/name/{name}` | `PartnerHashtagQueryController#getHashtagByName` | `PartnerHashtagQueryService#findByName` |
| GET | `/partner/hashtags/queries` | `PartnerHashtagQueryController#getAllHashtags` | `PartnerHashtagQueryService#findAll` |
| GET | `/partner/hashtags/queries/popular` | `PartnerHashtagQueryController#getPopularHashtags` | `PartnerHashtagQueryService#findPopular` |
| GET | `/partner/hashtags/queries/search` | `PartnerHashtagQueryController#searchHashtags` | `PartnerHashtagQueryService#search` |
| GET | `/partner/hashtags/queries/autocomplete` | `PartnerHashtagQueryController#autocompleteHashtags` | `PartnerHashtagQueryService#autocomplete`, `PartnerHashtagQueryService#smartAutocomplete` |
| GET | `/partner/styles/comments/queries/{styleId}` | `PartnerStyleCommentQueryController#getCommentsByStyleId` | `PartnerStyleCommentQueryService#getCommentsByStyleId` |
| GET | `/partner/styles/queries/{styleId}/hashtags` | `PartnerStyleHashtagQueryController#getStyleHashtags` | `PartnerStyleHashtagQueryService#getHashtagsByStyleId` |
| GET | `/partner/styles/queries/{styleId}` | `PartnerStyleQueryController#getStyleDetail` | `PartnerStyleQueryService#getStyleDetail`, `PartnerStyleViewEventProducer#sendViewEvent` |
| GET | `/partner/styles/queries` | `PartnerStyleQueryController#getFilteredStyles` | `PartnerStyleQueryService#getFilteredStyles` |
| GET | `/partner/styles/queries/profile/{profileId}` | `PartnerStyleQueryController#getStylesByProfile` | `PartnerStyleQueryService#getStylesByProfile` |
| GET | `/partner/styles/queries/{styleId}/media/{fileName}` | `PartnerStyleQueryController#getStyleMedia` |  |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `PartnerHashtagCommandService`
  - `create` → PartnerHashtagRepository#existsByName, PartnerHashtagRepository#save~, this#convertToDto
  - `update` → PartnerHashtagRepository#findById~, PartnerHashtagRepository#existsByName, PartnerHashtagRepository#save~, this#convertToDto
  - `delete` → PartnerHashtagRepository#findById~, PartnerStyleHashtagRepository#countByHashtagId, PartnerHashtagRepository#delete~
- `PartnerHashtagQueryService`
  - `findById` → PartnerHashtagRepository#findById~, this#convertToDto
  - `findByName` → PartnerHashtagRepository#findByName, this#convertToDto
  - `findAll` → PartnerHashtagRepository#findAll~
  - `findPopular` → PartnerHashtagRepository#findAllByOrderByCountDesc
  - `search` → PartnerHashtagRepository#findByNameContainingOrderByCountDesc
  - `autocomplete` → this#isKoreanChosung, this#chosungAutocomplete, this#koreanAutocomplete, this#englishAutocomplete
  - `englishAutocomplete` → PartnerHashtagRepository#findByNameStartingWithOrderByCountDesc
  - `koreanAutocomplete` → PartnerHashtagRepository#findByNameContainingKeywordOrderByCountDesc
  - `chosungAutocomplete` → PartnerHashtagRepository#findAll~, this#extractChosung
  - `smartAutocomplete` → this#containsKoreanCharacters, this#autocomplete
- `PartnerMediaUrlCommandService`
  - `saveMediaFile` → this#validateMediaFile, FileUtils#saveFile, MediaUrlRepository#save~
  - `deleteMediaUrl` → FileUtils#deleteFile, MediaUrlRepository#delete~
- `PartnerMediaUrlQueryService`
  - `findMediaUrlsByStyleId` → MediaUrlRepository#findByStyleId
  - `findMediaUrlById` → MediaUrlRepository#findById~
  - `findMediaUrlsByUrl` → MediaUrlRepository#findByUrl
- `PartnerStyleCommandService`
  - `createStyle` → PartnerProfileQueryService#getProfileByEmail, StyleRepository#save~, PartnerStyleOrderItemCommandService#createStyleOrderItem, PartnerMediaUrlCommandService#saveMediaFile, this#processHashtags
  - `processHashtags` → PartnerHashtagRepository#findByName, PartnerHashtagRepository#findById~, PartnerHashtagCommandService#create, PartnerStyleHashtagCommandService#addHashtagToStyle
  - `updateStyle` → StyleRepository#findById~, PartnerMediaUrlCommandService#deleteMediaUrl, PartnerMediaUrlCommandService#saveMediaFile, PartnerStyleHashtagCommandService#removeAllHashtagsFromStyle, this#processHashtags, StyleRepository#save~
  - `deleteStyle` → StyleRepository#findById~, PartnerStyleHashtagCommandService#removeAllHashtagsFromStyle, PartnerMediaUrlCommandService#deleteMediaUrl, StyleRepository#delete~
  - `incrementViewCount` → StyleRepository#findById~, StyleRepository#save~
- `PartnerStyleCommentCommandService`
  - `addComment` → PartnerProfileQueryService#getProfileByEmail, PartnerStyleQueryService#findStyleById, PartnerStyleCommentRepository#findById~, PartnerStyleCommentRepository#save~
  - `updateComment` → PartnerStyleCommentRepository#findById~, PartnerStyleCommentRepository#save~
  - `deleteComment` → PartnerStyleCommentRepository#findById~, PartnerStyleCommentRepository#delete~
- `PartnerStyleCommentLikeCommandService`
  - `toggleCommentLike` → PartnerProfileQueryService#getProfileByEmail, PartnerStyleCommentQueryService#findById, PartnerStyleCommentLikeRepository#findByCommentAndProfile, PartnerStyleCommentLikeRepository#delete~, PartnerStyleCommentLikeRepository#save~
- `PartnerStyleCommentLikeQueryService`
  - `isCommentLikedByProfile` → PartnerStyleCommentLikeRepository#existsByCommentIdAndProfileId
  - `findByCommentId` → PartnerStyleCommentLikeRepository#findByCommentId
  - `getLikedCommentIds` → PartnerProfileQueryService#getProfileByEmail, PartnerStyleCommentLikeRepository#findByProfileIdAndCommentIdIn
- `PartnerStyleCommentQueryService`
  - `getCommentsByStyleId` → PartnerStyleCommentRepository#findRootCommentsByStyleId, PartnerStyleCommentRepository#countCommentsByStyleId, PartnerStyleCommentRepository#findRepliesByParentCommentId, PartnerStyleCommentLikeQueryService#getLikedCommentIds, this#convertToResponseDtos, PartnerProfileQueryService#getProfileByEmail
  - `findCommentById` → PartnerStyleCommentRepository#findById~
  - `checkUserLikedComment` → PartnerProfileQueryService#getProfileByEmail, PartnerStyleCommentLikeQueryService#isCommentLikedByProfile
  - `findByStyleId` → PartnerStyleCommentRepository#findByStyleId
  - `findById` → PartnerStyleCommentRepository#findById~
  - `findByProfileId` → PartnerStyleCommentRepository#findByProfileId
- `PartnerStyleHashtagCommandService`
  - `addHashtagToStyle` → PartnerStyleHashtagRepository#findByStyleIdAndHashtagId, PartnerHashtagRepository#save~, PartnerStyleHashtagRepository#save~
  - `removeAllHashtagsFromStyle` → PartnerStyleHashtagRepository#findByStyleId, PartnerHashtagRepository#save~, PartnerStyleHashtagRepository#deleteByStyleId
  - `removeHashtagFromStyle` → PartnerStyleHashtagRepository#findByStyleIdAndHashtagId, PartnerHashtagRepository#save~, PartnerStyleHashtagRepository#delete~
- `PartnerStyleHashtagQueryService`
  - `getHashtagsByStyleId` → PartnerStyleHashtagRepository#findByStyleId
  - `getHashtagsByStyleIds` → PartnerStyleHashtagRepository#findByStyleIdIn
  - `getHashtagMapByStyleIds` → PartnerStyleHashtagRepository#findByStyleIdIn
- `PartnerStyleInterestCommandService`
  - `toggleStyleInterest` → PartnerProfileQueryService#getProfileByEmail, PartnerStyleQueryService#findStyleById, PartnerStyleInterestRepository#findByStyleAndProfile, PartnerStyleInterestRepository#delete~, PartnerStyleInterestRepository#save~
- `PartnerStyleInterestQueryService`
  - `isStyleInterestedByProfile` → PartnerStyleInterestRepository#existsByStyleIdAndProfileId
  - `findByStyleId` → PartnerStyleInterestRepository#findByStyleId
  - `getInterestedStyleIds` → PartnerStyleInterestRepository#findByProfileIdAndStyleIdIn
  - `checkUserInterested` → PartnerProfileQueryService#getProfileByEmail, PartnerStyleInterestRepository#existsByStyleIdAndProfileId
- `PartnerStyleLikeCommandService`
  - `toggleLike` → PartnerProfileQueryService#getProfileByEmail, PartnerStyleQueryService#findStyleById, PartnerStyleLikeRepository#findByStyleAndProfile, PartnerStyleLikeRepository#delete~, PartnerStyleLikeRepository#save~
- `PartnerStyleLikeQueryService`
  - `checkUserLiked` → PartnerProfileQueryService#getProfileByEmail, PartnerStyleLikeRepository#existsByStyleIdAndProfileId
  - `getLikedStyleIds` → PartnerProfileQueryService#getProfileByEmail, PartnerStyleLikeRepository#findByProfileIdAndStyleIdIn
  - `isLikedByProfile` → PartnerStyleLikeRepository#existsByStyleIdAndProfileId
  - `findLikesByStyleId` → PartnerStyleLikeRepository#findByStyleId
- `PartnerStyleOrderItemCommandService`
  - `createStyleOrderItem` → PartnerOrderItemQueryService#findById, StyleOrderItemRepository#save~
- `PartnerStyleQueryService`
  - `findStyleById` → StyleRepository#findById~
  - `findStylesByProfileId` → StyleRepository#findByProfileId
  - `getFilteredStyles` → StyleRepository#filterStyles, PartnerStyleHashtagQueryService#getHashtagMapByStyleIds, PartnerProfileQueryService#getProfileByEmail, PartnerStyleLikeQueryService#getLikedStyleIds, PartnerStyleInterestQueryService#getInterestedStyleIds
  - `getStylesByProfile` → StyleRepository#getStylesByProfile, PartnerStyleHashtagQueryService#getHashtagMapByStyleIds, PartnerStyleLikeQueryService#getLikedStyleIds
  - `getStyleDetail` → StyleRepository#getStyleDetail, PartnerStyleHashtagQueryService#getHashtagsByStyleId, PartnerProfileQueryService#getProfileByEmail, PartnerStyleLikeQueryService#isLikedByProfile, PartnerStyleInterestQueryService#isStyleInterestedByProfile
- `PartnerStyleViewEventConsumer`
  - `listen` → StyleRepository#findById~, StyleRepository#save~, this#flushBuffer
  - `flushBuffer` → PartnerStyleViewLogRepository#saveAll~
- `PartnerStyleViewEventKafkaConfig`
  - `styleViewEventKafkaTemplate` → this#styleViewEventProducerFactory
  - `styleViewEventKafkaListenerContainerFactory` → this#styleViewEventConsumerFactory
- `PartnerStyleViewEventProducer`
  - `sendViewEvent` → kafka:style-view-log-topic

## 3. 클래스 목록 (역할별)

### controller (10)

- `PartnerHashtagCommandController` controller/command/PartnerHashtagCommandController.java · public: createHashtag, updateHashtag, deleteHashtag · 주입: PartnerHashtagCommandService
- `PartnerStyleCommandController` controller/command/PartnerStyleCommandController.java · public: createStyle, incrementViewCount, updateStyle, deleteStyle · 주입: PartnerStyleCommandService, NginxCachePurgeUtil
- `PartnerStyleCommentCommandController` controller/command/PartnerStyleCommentCommandController.java · public: addComment, updateComment, deleteComment · 주입: PartnerStyleCommentCommandService
- `PartnerStyleCommentLikeCommandController` controller/command/PartnerStyleCommentLikeCommandController.java · public: toggleCommentLike · 주입: PartnerStyleCommentLikeCommandService
- `PartnerStyleInterestCommandController` controller/command/PartnerStyleInterestCommandController.java · public: toggleInterest · 주입: PartnerStyleInterestCommandService
- `PartnerStyleLikeCommandController` controller/command/PartnerStyleLikeCommandController.java · public: toggleLike · 주입: PartnerStyleLikeCommandService
- `PartnerHashtagQueryController` controller/query/PartnerHashtagQueryController.java · public: getHashtagById, getHashtagByName, getAllHashtags, getPopularHashtags, searchHashtags, autocompleteHashtags · 주입: PartnerHashtagQueryService
- `PartnerStyleCommentQueryController` controller/query/PartnerStyleCommentQueryController.java · public: getCommentsByStyleId · 주입: PartnerStyleCommentQueryService
- `PartnerStyleHashtagQueryController` controller/query/PartnerStyleHashtagQueryController.java · public: getStyleHashtags · 주입: PartnerStyleHashtagQueryService
- `PartnerStyleQueryController` controller/query/PartnerStyleQueryController.java · public: getStyleDetail, getFilteredStyles, getStylesByProfile, getStyleMedia · 주입: PartnerStyleQueryService, PartnerStyleViewEventProducer

### service-command (9)

- `PartnerHashtagCommandService` service/command/PartnerHashtagCommandService.java · public: create, update, delete · 주입: PartnerHashtagRepository, PartnerStyleHashtagRepository
- `PartnerMediaUrlCommandService` service/command/PartnerMediaUrlCommandService.java · public: saveMediaFile, deleteMediaUrl · 주입: MediaUrlRepository, FileUtils
- `PartnerStyleCommandService` service/command/PartnerStyleCommandService.java · public: createStyle, updateStyle, deleteStyle, incrementViewCount · 주입: StyleRepository, PartnerProfileQueryService, PartnerStyleOrderItemCommandService, PartnerMediaUrlCommandService, PartnerHashtagRepository, PartnerHashtagCommandService, PartnerStyleHashtagCommandService
- `PartnerStyleCommentCommandService` service/command/PartnerStyleCommentCommandService.java · public: addComment, updateComment, deleteComment · 주입: PartnerStyleCommentRepository, PartnerProfileQueryService, PartnerStyleQueryService
- `PartnerStyleCommentLikeCommandService` service/command/PartnerStyleCommentLikeCommandService.java · public: toggleCommentLike · 주입: PartnerStyleCommentLikeRepository, PartnerProfileQueryService, PartnerStyleCommentQueryService
- `PartnerStyleHashtagCommandService` service/command/PartnerStyleHashtagCommandService.java · public: addHashtagToStyle, removeAllHashtagsFromStyle, removeHashtagFromStyle · 주입: PartnerStyleHashtagRepository, PartnerHashtagRepository
- `PartnerStyleInterestCommandService` service/command/PartnerStyleInterestCommandService.java · public: toggleStyleInterest · 주입: PartnerStyleInterestRepository, PartnerProfileQueryService, PartnerStyleQueryService
- `PartnerStyleLikeCommandService` service/command/PartnerStyleLikeCommandService.java · public: toggleLike · 주입: PartnerStyleLikeRepository, PartnerProfileQueryService, PartnerStyleQueryService
- `PartnerStyleOrderItemCommandService` service/command/PartnerStyleOrderItemCommandService.java · public: createStyleOrderItem · 주입: StyleOrderItemRepository, PartnerOrderItemQueryService

### service-query (8)

- `PartnerHashtagQueryService` service/query/PartnerHashtagQueryService.java · public: findById, findByName, findAll, findPopular, search, autocomplete, smartAutocomplete · 주입: PartnerHashtagRepository
- `PartnerMediaUrlQueryService` service/query/PartnerMediaUrlQueryService.java · public: findMediaUrlsByStyleId, findMediaUrlById, findMediaUrlsByUrl · 주입: MediaUrlRepository
- `PartnerStyleCommentLikeQueryService` service/query/PartnerStyleCommentLikeQueryService.java · public: isCommentLikedByProfile, findByCommentId, getLikedCommentIds · 주입: PartnerStyleCommentLikeRepository, PartnerProfileQueryService
- `PartnerStyleCommentQueryService` service/query/PartnerStyleCommentQueryService.java · public: getCommentsByStyleId, findCommentById, checkUserLikedComment, findByStyleId, findById, findByProfileId · 주입: PartnerStyleCommentRepository, PartnerStyleCommentLikeQueryService, PartnerProfileQueryService
- `PartnerStyleHashtagQueryService` service/query/PartnerStyleHashtagQueryService.java · public: getHashtagsByStyleId, getHashtagsByStyleIds, getHashtagMapByStyleIds · 주입: PartnerStyleHashtagRepository
- `PartnerStyleInterestQueryService` service/query/PartnerStyleInterestQueryService.java · public: isStyleInterestedByProfile, findByStyleId, getInterestedStyleIds, checkUserInterested · 주입: PartnerStyleInterestRepository, PartnerProfileQueryService
- `PartnerStyleLikeQueryService` service/query/PartnerStyleLikeQueryService.java · public: checkUserLiked, getLikedStyleIds, isLikedByProfile, findLikesByStyleId · 주입: PartnerStyleLikeRepository, PartnerProfileQueryService
- `PartnerStyleQueryService` service/query/PartnerStyleQueryService.java · public: findStyleById, findStylesByProfileId, getFilteredStyles, getStylesByProfile, getStyleDetail · 주입: StyleRepository, PartnerStyleLikeQueryService, PartnerStyleInterestQueryService, PartnerStyleHashtagQueryService, PartnerProfileQueryService

### repository (7)

- `PartnerHashtagRepository` (interface) repository/PartnerHashtagRepository.java · extends/implements JpaRepository · public: findByName, findByNameStartingWithOrderByCountDesc, findByNameContainingKeywordOrderByCountDesc, findAllByOrderByCountDesc, findByNameContainingOrderByCountDesc, existsByName
- `PartnerStyleCommentLikeRepository` (interface) repository/PartnerStyleCommentLikeRepository.java · extends/implements JpaRepository · public: findByCommentAndProfile, findByCommentIdAndProfileId, existsByCommentIdAndProfileId, findByCommentId, findByProfileIdAndCommentIdIn, findCommentIdsByProfileIdAndCommentIdIn, countByCommentId, deleteByProfileIdAndCommentId
- `PartnerStyleCommentRepository` (interface) repository/PartnerStyleCommentRepository.java · extends/implements JpaRepository · public: findRootCommentsByStyleId, countCommentsByStyleId, findRepliesByParentCommentId, findByStyleId, findByProfileId
- `PartnerStyleHashtagRepository` (interface) repository/PartnerStyleHashtagRepository.java · extends/implements JpaRepository · public: findByStyleId, findByStyleIdIn, findStyleIdsByHashtagId, deleteByStyleId, findByStyleIdAndHashtagId, countByHashtagId
- `PartnerStyleInterestRepository` (interface) repository/PartnerStyleInterestRepository.java · extends/implements JpaRepository · public: existsByStyleIdAndProfileId, findByStyleId, findByStyleAndProfile, findByStyleIdAndProfileId, findByProfileIdAndStyleIdIn, findStyleIdsByProfileIdAndStyleIdIn, countByStyleId, deleteByProfileIdAndStyleId
- `PartnerStyleLikeRepository` (interface) repository/PartnerStyleLikeRepository.java · extends/implements JpaRepository · public: findByStyleAndProfile, existsByStyleIdAndProfileId, findByStyleId, findByProfileIdAndStyleId, findByProfileIdAndStyleIdIn, findStyleIdsByProfileIdAndStyleIdIn, countByStyleId, deleteByProfileIdAndStyleId
- `PartnerStyleViewLogRepository` (interface) repository/PartnerStyleViewLogRepository.java · extends/implements JpaRepository

### custom (2)

- `PartnerStyleRepositoryCustom` (interface) repository/PartnerStyleRepositoryCustom.java · public: filterStyles, getStyleDetail, getStylesByProfile, styleCountByColorIds
- `PartnerStyleRepositoryCustomImpl` repository/PartnerStyleRepositoryCustomImpl.java · extends/implements PartnerStyleRepositoryCustom · public: filterStyles, getStyleDetail, getStylesByProfile, styleCountByColorIds · 주입: JPAQueryFactory

### event (2)

- `PartnerStyleViewEventConsumer` service/kafka/PartnerStyleViewEventConsumer.java · public: listen, flushBuffer · 주입: StyleRepository, PartnerStyleViewLogRepository
- `PartnerStyleViewEventProducer` service/kafka/PartnerStyleViewEventProducer.java · public: sendViewEvent · 주입: KafkaTemplate

### config (1)

- `PartnerStyleViewEventKafkaConfig` service/kafka/config/PartnerStyleViewEventKafkaConfig.java · public: styleViewEventProducerFactory, styleViewEventKafkaTemplate, styleViewEventConsumerFactory, styleViewEventKafkaListenerContainerFactory

### 다른 도메인 참조 (import·주입 기준)

- **order**: `OrderItem` ← PartnerStyleOrderItemCommandService
- **partnerorder**: `PartnerOrderItemQueryService` ← PartnerStyleOrderItemCommandService
- **partneruser**: `PartnerProfileQueryService` ← PartnerStyleCommandService, PartnerStyleCommentCommandService, PartnerStyleCommentLikeCommandService, PartnerStyleCommentLikeQueryService, PartnerStyleCommentQueryService, PartnerStyleInterestCommandService, PartnerStyleInterestQueryService, PartnerStyleLikeCommandService, PartnerStyleLikeQueryService, PartnerStyleQueryService
- **style**: `Hashtag` ← PartnerHashtagCommandService, PartnerHashtagQueryService, PartnerHashtagRepository, PartnerStyleCommandService, PartnerStyleHashtagCommandService; `MediaUrl` ← PartnerMediaUrlCommandService, PartnerMediaUrlQueryService, PartnerStyleCommandService; `MediaUrlRepository` ← PartnerMediaUrlCommandService, PartnerMediaUrlQueryService; `Style` ← PartnerMediaUrlCommandService, PartnerStyleCommandController, PartnerStyleCommandService, PartnerStyleCommentCommandService, PartnerStyleHashtagCommandService, PartnerStyleInterestCommandService, PartnerStyleInterestRepository, PartnerStyleLikeCommandService, PartnerStyleLikeRepository, PartnerStyleOrderItemCommandService, PartnerStyleQueryService, PartnerStyleViewEventConsumer; `StyleComment` ← PartnerStyleCommentCommandController, PartnerStyleCommentCommandService, PartnerStyleCommentLikeCommandService, PartnerStyleCommentLikeRepository, PartnerStyleCommentQueryService, PartnerStyleCommentRepository; `StyleCommentLike` ← PartnerStyleCommentLikeCommandService, PartnerStyleCommentLikeQueryService, PartnerStyleCommentLikeRepository; `StyleHashtag` ← PartnerStyleHashtagCommandService, PartnerStyleHashtagQueryService, PartnerStyleHashtagRepository; `StyleInterest` ← PartnerStyleInterestCommandService, PartnerStyleInterestQueryService, PartnerStyleInterestRepository; `StyleLike` ← PartnerStyleLikeCommandService, PartnerStyleLikeQueryService, PartnerStyleLikeRepository; `StyleOrderItem` ← PartnerStyleCommandService, PartnerStyleOrderItemCommandService; `StyleOrderItemRepository` ← PartnerStyleOrderItemCommandService; `StyleRepository` ← PartnerStyleCommandService, PartnerStyleQueryService, PartnerStyleViewEventConsumer; `StyleViewEvent` ← PartnerStyleViewEventConsumer, PartnerStyleViewEventKafkaConfig, PartnerStyleViewEventProducer; `StyleViewLog` ← PartnerStyleViewEventConsumer, PartnerStyleViewLogRepository
- **user**: `Gender` ← PartnerStyleQueryController, PartnerStyleViewEventProducer; `Profile` ← PartnerStyleCommandService, PartnerStyleCommentCommandService, PartnerStyleCommentLikeCommandService, PartnerStyleCommentLikeQueryService, PartnerStyleCommentLikeRepository, PartnerStyleCommentQueryService, PartnerStyleInterestCommandService, PartnerStyleInterestQueryService, PartnerStyleInterestRepository, PartnerStyleLikeCommandService, PartnerStyleLikeQueryService, PartnerStyleLikeRepository, PartnerStyleQueryService

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

- 발행 kafka `style-view-log-topic`: `PartnerStyleViewEventProducer#sendViewEvent` → `AdminStyleViewEventConsumer#listen` [adminstyle], `InternalStyleViewEventConsumer#listen` [internalstyle], `LegacyStyleViewEventConsumer#listen` [legacystyle], `PartnerStyleViewEventConsumer#listen` [partnerstyle], `SettlementStyleViewEventConsumer#listen` [settlementstyle], `StyleViewEventConsumer#listen` [style]
- 구독 kafka `style-view-log-topic`: `PartnerStyleViewEventConsumer#listen` (groupId=style-view-group containerFactory=styleViewEventKafkaListenerContainerFactory) ← `AdminStyleViewEventProducer#sendViewEvent` [adminstyle], `InternalStyleViewEventProducer#sendViewEvent` [internalstyle], `LegacyStyleViewEventProducer#sendViewEvent` [legacystyle], `PartnerStyleViewEventProducer#sendViewEvent` [partnerstyle], `SettlementStyleViewEventProducer#sendViewEvent` [settlementstyle], `StyleViewEventProducer#sendViewEvent` [style]

## 6. AOP

(없음)

