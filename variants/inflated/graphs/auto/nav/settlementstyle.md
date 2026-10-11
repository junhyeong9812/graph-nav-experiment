# settlementstyle 도메인

기준 경로: `src/main/java/com/fream/back/domain/settlementstyle/` · 파일 39개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/settlement/hashtags/commands` | `SettlementHashtagCommandController#createHashtag` | `SettlementHashtagCommandService#create` |
| PUT | `/settlement/hashtags/commands/{id}` | `SettlementHashtagCommandController#updateHashtag` | `SettlementHashtagCommandService#update` |
| DELETE | `/settlement/hashtags/commands/{id}` | `SettlementHashtagCommandController#deleteHashtag` | `SettlementHashtagCommandService#delete` |
| POST | `/settlement/styles/commands` | `SettlementStyleCommandController#createStyle` | `SettlementStyleCommandService#createStyle`, `NginxCachePurgeUtil#purgeStyleCache` |
| POST | `/settlement/styles/commands/{styleId}/view` | `SettlementStyleCommandController#incrementViewCount` | `SettlementStyleCommandService#incrementViewCount` |
| PUT | `/settlement/styles/commands/{styleId}` | `SettlementStyleCommandController#updateStyle` | `SettlementStyleCommandService#updateStyle`, `NginxCachePurgeUtil#purgeStyleCache` |
| DELETE | `/settlement/styles/commands/{styleId}` | `SettlementStyleCommandController#deleteStyle` | `SettlementStyleCommandService#deleteStyle`, `NginxCachePurgeUtil#purgeStyleCache` |
| POST | `/settlement/styles/comments/commands` | `SettlementStyleCommentCommandController#addComment` | `SettlementStyleCommentCommandService#addComment` |
| PUT | `/settlement/styles/comments/commands/{commentId}` | `SettlementStyleCommentCommandController#updateComment` | `SettlementStyleCommentCommandService#updateComment` |
| DELETE | `/settlement/styles/comments/commands/{commentId}` | `SettlementStyleCommentCommandController#deleteComment` | `SettlementStyleCommentCommandService#deleteComment` |
| POST | `/settlement/styles/comments/likes/commands/{commentId}/toggle` | `SettlementStyleCommentLikeCommandController#toggleCommentLike` | `SettlementStyleCommentLikeCommandService#toggleCommentLike` |
| POST | `/settlement/styles/interests/commands/{styleId}/toggle` | `SettlementStyleInterestCommandController#toggleInterest` | `SettlementStyleInterestCommandService#toggleStyleInterest` |
| POST | `/settlement/styles/likes/commands/{styleId}/toggle` | `SettlementStyleLikeCommandController#toggleLike` | `SettlementStyleLikeCommandService#toggleLike` |
| GET | `/settlement/hashtags/queries/{id}` | `SettlementHashtagQueryController#getHashtagById` | `SettlementHashtagQueryService#findById` |
| GET | `/settlement/hashtags/queries/name/{name}` | `SettlementHashtagQueryController#getHashtagByName` | `SettlementHashtagQueryService#findByName` |
| GET | `/settlement/hashtags/queries` | `SettlementHashtagQueryController#getAllHashtags` | `SettlementHashtagQueryService#findAll` |
| GET | `/settlement/hashtags/queries/popular` | `SettlementHashtagQueryController#getPopularHashtags` | `SettlementHashtagQueryService#findPopular` |
| GET | `/settlement/hashtags/queries/search` | `SettlementHashtagQueryController#searchHashtags` | `SettlementHashtagQueryService#search` |
| GET | `/settlement/hashtags/queries/autocomplete` | `SettlementHashtagQueryController#autocompleteHashtags` | `SettlementHashtagQueryService#autocomplete`, `SettlementHashtagQueryService#smartAutocomplete` |
| GET | `/settlement/styles/comments/queries/{styleId}` | `SettlementStyleCommentQueryController#getCommentsByStyleId` | `SettlementStyleCommentQueryService#getCommentsByStyleId` |
| GET | `/settlement/styles/queries/{styleId}/hashtags` | `SettlementStyleHashtagQueryController#getStyleHashtags` | `SettlementStyleHashtagQueryService#getHashtagsByStyleId` |
| GET | `/settlement/styles/queries/{styleId}` | `SettlementStyleQueryController#getStyleDetail` | `SettlementStyleQueryService#getStyleDetail`, `SettlementStyleViewEventProducer#sendViewEvent` |
| GET | `/settlement/styles/queries` | `SettlementStyleQueryController#getFilteredStyles` | `SettlementStyleQueryService#getFilteredStyles` |
| GET | `/settlement/styles/queries/profile/{profileId}` | `SettlementStyleQueryController#getStylesByProfile` | `SettlementStyleQueryService#getStylesByProfile` |
| GET | `/settlement/styles/queries/{styleId}/media/{fileName}` | `SettlementStyleQueryController#getStyleMedia` |  |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `SettlementHashtagCommandService`
  - `create` → SettlementHashtagRepository#existsByName, SettlementHashtagRepository#save~, this#convertToDto
  - `update` → SettlementHashtagRepository#findById~, SettlementHashtagRepository#existsByName, SettlementHashtagRepository#save~, this#convertToDto
  - `delete` → SettlementHashtagRepository#findById~, SettlementStyleHashtagRepository#countByHashtagId, SettlementHashtagRepository#delete~
- `SettlementHashtagQueryService`
  - `findById` → SettlementHashtagRepository#findById~, this#convertToDto
  - `findByName` → SettlementHashtagRepository#findByName, this#convertToDto
  - `findAll` → SettlementHashtagRepository#findAll~
  - `findPopular` → SettlementHashtagRepository#findAllByOrderByCountDesc
  - `search` → SettlementHashtagRepository#findByNameContainingOrderByCountDesc
  - `autocomplete` → this#isKoreanChosung, this#chosungAutocomplete, this#koreanAutocomplete, this#englishAutocomplete
  - `englishAutocomplete` → SettlementHashtagRepository#findByNameStartingWithOrderByCountDesc
  - `koreanAutocomplete` → SettlementHashtagRepository#findByNameContainingKeywordOrderByCountDesc
  - `chosungAutocomplete` → SettlementHashtagRepository#findAll~, this#extractChosung
  - `smartAutocomplete` → this#containsKoreanCharacters, this#autocomplete
- `SettlementMediaUrlCommandService`
  - `saveMediaFile` → this#validateMediaFile, FileUtils#saveFile, MediaUrlRepository#save~
  - `deleteMediaUrl` → FileUtils#deleteFile, MediaUrlRepository#delete~
- `SettlementMediaUrlQueryService`
  - `findMediaUrlsByStyleId` → MediaUrlRepository#findByStyleId
  - `findMediaUrlById` → MediaUrlRepository#findById~
  - `findMediaUrlsByUrl` → MediaUrlRepository#findByUrl
- `SettlementStyleCommandService`
  - `createStyle` → SettlementProfileQueryService#getProfileByEmail, StyleRepository#save~, SettlementStyleOrderItemCommandService#createStyleOrderItem, SettlementMediaUrlCommandService#saveMediaFile, this#processHashtags
  - `processHashtags` → SettlementHashtagRepository#findByName, SettlementHashtagRepository#findById~, SettlementHashtagCommandService#create, SettlementStyleHashtagCommandService#addHashtagToStyle
  - `updateStyle` → StyleRepository#findById~, SettlementMediaUrlCommandService#deleteMediaUrl, SettlementMediaUrlCommandService#saveMediaFile, SettlementStyleHashtagCommandService#removeAllHashtagsFromStyle, this#processHashtags, StyleRepository#save~
  - `deleteStyle` → StyleRepository#findById~, SettlementStyleHashtagCommandService#removeAllHashtagsFromStyle, SettlementMediaUrlCommandService#deleteMediaUrl, StyleRepository#delete~
  - `incrementViewCount` → StyleRepository#findById~, StyleRepository#save~
- `SettlementStyleCommentCommandService`
  - `addComment` → SettlementProfileQueryService#getProfileByEmail, SettlementStyleQueryService#findStyleById, SettlementStyleCommentRepository#findById~, SettlementStyleCommentRepository#save~
  - `updateComment` → SettlementStyleCommentRepository#findById~, SettlementStyleCommentRepository#save~
  - `deleteComment` → SettlementStyleCommentRepository#findById~, SettlementStyleCommentRepository#delete~
- `SettlementStyleCommentLikeCommandService`
  - `toggleCommentLike` → SettlementProfileQueryService#getProfileByEmail, SettlementStyleCommentQueryService#findById, SettlementStyleCommentLikeRepository#findByCommentAndProfile, SettlementStyleCommentLikeRepository#delete~, SettlementStyleCommentLikeRepository#save~
- `SettlementStyleCommentLikeQueryService`
  - `isCommentLikedByProfile` → SettlementStyleCommentLikeRepository#existsByCommentIdAndProfileId
  - `findByCommentId` → SettlementStyleCommentLikeRepository#findByCommentId
  - `getLikedCommentIds` → SettlementProfileQueryService#getProfileByEmail, SettlementStyleCommentLikeRepository#findByProfileIdAndCommentIdIn
- `SettlementStyleCommentQueryService`
  - `getCommentsByStyleId` → SettlementStyleCommentRepository#findRootCommentsByStyleId, SettlementStyleCommentRepository#countCommentsByStyleId, SettlementStyleCommentRepository#findRepliesByParentCommentId, SettlementStyleCommentLikeQueryService#getLikedCommentIds, this#convertToResponseDtos, SettlementProfileQueryService#getProfileByEmail
  - `findCommentById` → SettlementStyleCommentRepository#findById~
  - `checkUserLikedComment` → SettlementProfileQueryService#getProfileByEmail, SettlementStyleCommentLikeQueryService#isCommentLikedByProfile
  - `findByStyleId` → SettlementStyleCommentRepository#findByStyleId
  - `findById` → SettlementStyleCommentRepository#findById~
  - `findByProfileId` → SettlementStyleCommentRepository#findByProfileId
- `SettlementStyleHashtagCommandService`
  - `addHashtagToStyle` → SettlementStyleHashtagRepository#findByStyleIdAndHashtagId, SettlementHashtagRepository#save~, SettlementStyleHashtagRepository#save~
  - `removeAllHashtagsFromStyle` → SettlementStyleHashtagRepository#findByStyleId, SettlementHashtagRepository#save~, SettlementStyleHashtagRepository#deleteByStyleId
  - `removeHashtagFromStyle` → SettlementStyleHashtagRepository#findByStyleIdAndHashtagId, SettlementHashtagRepository#save~, SettlementStyleHashtagRepository#delete~
- `SettlementStyleHashtagQueryService`
  - `getHashtagsByStyleId` → SettlementStyleHashtagRepository#findByStyleId
  - `getHashtagsByStyleIds` → SettlementStyleHashtagRepository#findByStyleIdIn
  - `getHashtagMapByStyleIds` → SettlementStyleHashtagRepository#findByStyleIdIn
- `SettlementStyleInterestCommandService`
  - `toggleStyleInterest` → SettlementProfileQueryService#getProfileByEmail, SettlementStyleQueryService#findStyleById, SettlementStyleInterestRepository#findByStyleAndProfile, SettlementStyleInterestRepository#delete~, SettlementStyleInterestRepository#save~
- `SettlementStyleInterestQueryService`
  - `isStyleInterestedByProfile` → SettlementStyleInterestRepository#existsByStyleIdAndProfileId
  - `findByStyleId` → SettlementStyleInterestRepository#findByStyleId
  - `getInterestedStyleIds` → SettlementStyleInterestRepository#findByProfileIdAndStyleIdIn
  - `checkUserInterested` → SettlementProfileQueryService#getProfileByEmail, SettlementStyleInterestRepository#existsByStyleIdAndProfileId
- `SettlementStyleLikeCommandService`
  - `toggleLike` → SettlementProfileQueryService#getProfileByEmail, SettlementStyleQueryService#findStyleById, SettlementStyleLikeRepository#findByStyleAndProfile, SettlementStyleLikeRepository#delete~, SettlementStyleLikeRepository#save~
- `SettlementStyleLikeQueryService`
  - `checkUserLiked` → SettlementProfileQueryService#getProfileByEmail, SettlementStyleLikeRepository#existsByStyleIdAndProfileId
  - `getLikedStyleIds` → SettlementProfileQueryService#getProfileByEmail, SettlementStyleLikeRepository#findByProfileIdAndStyleIdIn
  - `isLikedByProfile` → SettlementStyleLikeRepository#existsByStyleIdAndProfileId
  - `findLikesByStyleId` → SettlementStyleLikeRepository#findByStyleId
- `SettlementStyleOrderItemCommandService`
  - `createStyleOrderItem` → SettlementOrderItemQueryService#findById, StyleOrderItemRepository#save~
- `SettlementStyleQueryService`
  - `findStyleById` → StyleRepository#findById~
  - `findStylesByProfileId` → StyleRepository#findByProfileId
  - `getFilteredStyles` → StyleRepository#filterStyles, SettlementStyleHashtagQueryService#getHashtagMapByStyleIds, SettlementProfileQueryService#getProfileByEmail, SettlementStyleLikeQueryService#getLikedStyleIds, SettlementStyleInterestQueryService#getInterestedStyleIds
  - `getStylesByProfile` → StyleRepository#getStylesByProfile, SettlementStyleHashtagQueryService#getHashtagMapByStyleIds, SettlementStyleLikeQueryService#getLikedStyleIds
  - `getStyleDetail` → StyleRepository#getStyleDetail, SettlementStyleHashtagQueryService#getHashtagsByStyleId, SettlementProfileQueryService#getProfileByEmail, SettlementStyleLikeQueryService#isLikedByProfile, SettlementStyleInterestQueryService#isStyleInterestedByProfile
- `SettlementStyleViewEventConsumer`
  - `listen` → StyleRepository#findById~, StyleRepository#save~, this#flushBuffer
  - `flushBuffer` → SettlementStyleViewLogRepository#saveAll~
- `SettlementStyleViewEventKafkaConfig`
  - `styleViewEventKafkaTemplate` → this#styleViewEventProducerFactory
  - `styleViewEventKafkaListenerContainerFactory` → this#styleViewEventConsumerFactory
- `SettlementStyleViewEventProducer`
  - `sendViewEvent` → kafka:style-view-log-topic

## 3. 클래스 목록 (역할별)

### controller (10)

- `SettlementHashtagCommandController` controller/command/SettlementHashtagCommandController.java · public: createHashtag, updateHashtag, deleteHashtag · 주입: SettlementHashtagCommandService
- `SettlementStyleCommandController` controller/command/SettlementStyleCommandController.java · public: createStyle, incrementViewCount, updateStyle, deleteStyle · 주입: SettlementStyleCommandService, NginxCachePurgeUtil
- `SettlementStyleCommentCommandController` controller/command/SettlementStyleCommentCommandController.java · public: addComment, updateComment, deleteComment · 주입: SettlementStyleCommentCommandService
- `SettlementStyleCommentLikeCommandController` controller/command/SettlementStyleCommentLikeCommandController.java · public: toggleCommentLike · 주입: SettlementStyleCommentLikeCommandService
- `SettlementStyleInterestCommandController` controller/command/SettlementStyleInterestCommandController.java · public: toggleInterest · 주입: SettlementStyleInterestCommandService
- `SettlementStyleLikeCommandController` controller/command/SettlementStyleLikeCommandController.java · public: toggleLike · 주입: SettlementStyleLikeCommandService
- `SettlementHashtagQueryController` controller/query/SettlementHashtagQueryController.java · public: getHashtagById, getHashtagByName, getAllHashtags, getPopularHashtags, searchHashtags, autocompleteHashtags · 주입: SettlementHashtagQueryService
- `SettlementStyleCommentQueryController` controller/query/SettlementStyleCommentQueryController.java · public: getCommentsByStyleId · 주입: SettlementStyleCommentQueryService
- `SettlementStyleHashtagQueryController` controller/query/SettlementStyleHashtagQueryController.java · public: getStyleHashtags · 주입: SettlementStyleHashtagQueryService
- `SettlementStyleQueryController` controller/query/SettlementStyleQueryController.java · public: getStyleDetail, getFilteredStyles, getStylesByProfile, getStyleMedia · 주입: SettlementStyleQueryService, SettlementStyleViewEventProducer

### service-command (9)

- `SettlementHashtagCommandService` service/command/SettlementHashtagCommandService.java · public: create, update, delete · 주입: SettlementHashtagRepository, SettlementStyleHashtagRepository
- `SettlementMediaUrlCommandService` service/command/SettlementMediaUrlCommandService.java · public: saveMediaFile, deleteMediaUrl · 주입: MediaUrlRepository, FileUtils
- `SettlementStyleCommandService` service/command/SettlementStyleCommandService.java · public: createStyle, updateStyle, deleteStyle, incrementViewCount · 주입: StyleRepository, SettlementProfileQueryService, SettlementStyleOrderItemCommandService, SettlementMediaUrlCommandService, SettlementHashtagRepository, SettlementHashtagCommandService, SettlementStyleHashtagCommandService
- `SettlementStyleCommentCommandService` service/command/SettlementStyleCommentCommandService.java · public: addComment, updateComment, deleteComment · 주입: SettlementStyleCommentRepository, SettlementProfileQueryService, SettlementStyleQueryService
- `SettlementStyleCommentLikeCommandService` service/command/SettlementStyleCommentLikeCommandService.java · public: toggleCommentLike · 주입: SettlementStyleCommentLikeRepository, SettlementProfileQueryService, SettlementStyleCommentQueryService
- `SettlementStyleHashtagCommandService` service/command/SettlementStyleHashtagCommandService.java · public: addHashtagToStyle, removeAllHashtagsFromStyle, removeHashtagFromStyle · 주입: SettlementStyleHashtagRepository, SettlementHashtagRepository
- `SettlementStyleInterestCommandService` service/command/SettlementStyleInterestCommandService.java · public: toggleStyleInterest · 주입: SettlementStyleInterestRepository, SettlementProfileQueryService, SettlementStyleQueryService
- `SettlementStyleLikeCommandService` service/command/SettlementStyleLikeCommandService.java · public: toggleLike · 주입: SettlementStyleLikeRepository, SettlementProfileQueryService, SettlementStyleQueryService
- `SettlementStyleOrderItemCommandService` service/command/SettlementStyleOrderItemCommandService.java · public: createStyleOrderItem · 주입: StyleOrderItemRepository, SettlementOrderItemQueryService

### service-query (8)

- `SettlementHashtagQueryService` service/query/SettlementHashtagQueryService.java · public: findById, findByName, findAll, findPopular, search, autocomplete, smartAutocomplete · 주입: SettlementHashtagRepository
- `SettlementMediaUrlQueryService` service/query/SettlementMediaUrlQueryService.java · public: findMediaUrlsByStyleId, findMediaUrlById, findMediaUrlsByUrl · 주입: MediaUrlRepository
- `SettlementStyleCommentLikeQueryService` service/query/SettlementStyleCommentLikeQueryService.java · public: isCommentLikedByProfile, findByCommentId, getLikedCommentIds · 주입: SettlementStyleCommentLikeRepository, SettlementProfileQueryService
- `SettlementStyleCommentQueryService` service/query/SettlementStyleCommentQueryService.java · public: getCommentsByStyleId, findCommentById, checkUserLikedComment, findByStyleId, findById, findByProfileId · 주입: SettlementStyleCommentRepository, SettlementStyleCommentLikeQueryService, SettlementProfileQueryService
- `SettlementStyleHashtagQueryService` service/query/SettlementStyleHashtagQueryService.java · public: getHashtagsByStyleId, getHashtagsByStyleIds, getHashtagMapByStyleIds · 주입: SettlementStyleHashtagRepository
- `SettlementStyleInterestQueryService` service/query/SettlementStyleInterestQueryService.java · public: isStyleInterestedByProfile, findByStyleId, getInterestedStyleIds, checkUserInterested · 주입: SettlementStyleInterestRepository, SettlementProfileQueryService
- `SettlementStyleLikeQueryService` service/query/SettlementStyleLikeQueryService.java · public: checkUserLiked, getLikedStyleIds, isLikedByProfile, findLikesByStyleId · 주입: SettlementStyleLikeRepository, SettlementProfileQueryService
- `SettlementStyleQueryService` service/query/SettlementStyleQueryService.java · public: findStyleById, findStylesByProfileId, getFilteredStyles, getStylesByProfile, getStyleDetail · 주입: StyleRepository, SettlementStyleLikeQueryService, SettlementStyleInterestQueryService, SettlementStyleHashtagQueryService, SettlementProfileQueryService

### repository (7)

- `SettlementHashtagRepository` (interface) repository/SettlementHashtagRepository.java · extends/implements JpaRepository · public: findByName, findByNameStartingWithOrderByCountDesc, findByNameContainingKeywordOrderByCountDesc, findAllByOrderByCountDesc, findByNameContainingOrderByCountDesc, existsByName
- `SettlementStyleCommentLikeRepository` (interface) repository/SettlementStyleCommentLikeRepository.java · extends/implements JpaRepository · public: findByCommentAndProfile, findByCommentIdAndProfileId, existsByCommentIdAndProfileId, findByCommentId, findByProfileIdAndCommentIdIn, findCommentIdsByProfileIdAndCommentIdIn, countByCommentId, deleteByProfileIdAndCommentId
- `SettlementStyleCommentRepository` (interface) repository/SettlementStyleCommentRepository.java · extends/implements JpaRepository · public: findRootCommentsByStyleId, countCommentsByStyleId, findRepliesByParentCommentId, findByStyleId, findByProfileId
- `SettlementStyleHashtagRepository` (interface) repository/SettlementStyleHashtagRepository.java · extends/implements JpaRepository · public: findByStyleId, findByStyleIdIn, findStyleIdsByHashtagId, deleteByStyleId, findByStyleIdAndHashtagId, countByHashtagId
- `SettlementStyleInterestRepository` (interface) repository/SettlementStyleInterestRepository.java · extends/implements JpaRepository · public: existsByStyleIdAndProfileId, findByStyleId, findByStyleAndProfile, findByStyleIdAndProfileId, findByProfileIdAndStyleIdIn, findStyleIdsByProfileIdAndStyleIdIn, countByStyleId, deleteByProfileIdAndStyleId
- `SettlementStyleLikeRepository` (interface) repository/SettlementStyleLikeRepository.java · extends/implements JpaRepository · public: findByStyleAndProfile, existsByStyleIdAndProfileId, findByStyleId, findByProfileIdAndStyleId, findByProfileIdAndStyleIdIn, findStyleIdsByProfileIdAndStyleIdIn, countByStyleId, deleteByProfileIdAndStyleId
- `SettlementStyleViewLogRepository` (interface) repository/SettlementStyleViewLogRepository.java · extends/implements JpaRepository

### custom (2)

- `SettlementStyleRepositoryCustom` (interface) repository/SettlementStyleRepositoryCustom.java · public: filterStyles, getStyleDetail, getStylesByProfile, styleCountByColorIds
- `SettlementStyleRepositoryCustomImpl` repository/SettlementStyleRepositoryCustomImpl.java · extends/implements SettlementStyleRepositoryCustom · public: filterStyles, getStyleDetail, getStylesByProfile, styleCountByColorIds · 주입: JPAQueryFactory

### event (2)

- `SettlementStyleViewEventConsumer` service/kafka/SettlementStyleViewEventConsumer.java · public: listen, flushBuffer · 주입: StyleRepository, SettlementStyleViewLogRepository
- `SettlementStyleViewEventProducer` service/kafka/SettlementStyleViewEventProducer.java · public: sendViewEvent · 주입: KafkaTemplate

### config (1)

- `SettlementStyleViewEventKafkaConfig` service/kafka/config/SettlementStyleViewEventKafkaConfig.java · public: styleViewEventProducerFactory, styleViewEventKafkaTemplate, styleViewEventConsumerFactory, styleViewEventKafkaListenerContainerFactory

### 다른 도메인 참조 (import·주입 기준)

- **order**: `OrderItem` ← SettlementStyleOrderItemCommandService
- **settlementorder**: `SettlementOrderItemQueryService` ← SettlementStyleOrderItemCommandService
- **settlementuser**: `SettlementProfileQueryService` ← SettlementStyleCommandService, SettlementStyleCommentCommandService, SettlementStyleCommentLikeCommandService, SettlementStyleCommentLikeQueryService, SettlementStyleCommentQueryService, SettlementStyleInterestCommandService, SettlementStyleInterestQueryService, SettlementStyleLikeCommandService, SettlementStyleLikeQueryService, SettlementStyleQueryService
- **style**: `Hashtag` ← SettlementHashtagCommandService, SettlementHashtagQueryService, SettlementHashtagRepository, SettlementStyleCommandService, SettlementStyleHashtagCommandService; `MediaUrl` ← SettlementMediaUrlCommandService, SettlementMediaUrlQueryService, SettlementStyleCommandService; `MediaUrlRepository` ← SettlementMediaUrlCommandService, SettlementMediaUrlQueryService; `Style` ← SettlementMediaUrlCommandService, SettlementStyleCommandController, SettlementStyleCommandService, SettlementStyleCommentCommandService, SettlementStyleHashtagCommandService, SettlementStyleInterestCommandService, SettlementStyleInterestRepository, SettlementStyleLikeCommandService, SettlementStyleLikeRepository, SettlementStyleOrderItemCommandService, SettlementStyleQueryService, SettlementStyleViewEventConsumer; `StyleComment` ← SettlementStyleCommentCommandController, SettlementStyleCommentCommandService, SettlementStyleCommentLikeCommandService, SettlementStyleCommentLikeRepository, SettlementStyleCommentQueryService, SettlementStyleCommentRepository; `StyleCommentLike` ← SettlementStyleCommentLikeCommandService, SettlementStyleCommentLikeQueryService, SettlementStyleCommentLikeRepository; `StyleHashtag` ← SettlementStyleHashtagCommandService, SettlementStyleHashtagQueryService, SettlementStyleHashtagRepository; `StyleInterest` ← SettlementStyleInterestCommandService, SettlementStyleInterestQueryService, SettlementStyleInterestRepository; `StyleLike` ← SettlementStyleLikeCommandService, SettlementStyleLikeQueryService, SettlementStyleLikeRepository; `StyleOrderItem` ← SettlementStyleCommandService, SettlementStyleOrderItemCommandService; `StyleOrderItemRepository` ← SettlementStyleOrderItemCommandService; `StyleRepository` ← SettlementStyleCommandService, SettlementStyleQueryService, SettlementStyleViewEventConsumer; `StyleViewEvent` ← SettlementStyleViewEventConsumer, SettlementStyleViewEventKafkaConfig, SettlementStyleViewEventProducer; `StyleViewLog` ← SettlementStyleViewEventConsumer, SettlementStyleViewLogRepository
- **user**: `Gender` ← SettlementStyleQueryController, SettlementStyleViewEventProducer; `Profile` ← SettlementStyleCommandService, SettlementStyleCommentCommandService, SettlementStyleCommentLikeCommandService, SettlementStyleCommentLikeQueryService, SettlementStyleCommentLikeRepository, SettlementStyleCommentQueryService, SettlementStyleInterestCommandService, SettlementStyleInterestQueryService, SettlementStyleInterestRepository, SettlementStyleLikeCommandService, SettlementStyleLikeQueryService, SettlementStyleLikeRepository, SettlementStyleQueryService

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

- 발행 kafka `style-view-log-topic`: `SettlementStyleViewEventProducer#sendViewEvent` → `AdminStyleViewEventConsumer#listen` [adminstyle], `InternalStyleViewEventConsumer#listen` [internalstyle], `LegacyStyleViewEventConsumer#listen` [legacystyle], `PartnerStyleViewEventConsumer#listen` [partnerstyle], `SettlementStyleViewEventConsumer#listen` [settlementstyle], `StyleViewEventConsumer#listen` [style]
- 구독 kafka `style-view-log-topic`: `SettlementStyleViewEventConsumer#listen` (groupId=style-view-group containerFactory=styleViewEventKafkaListenerContainerFactory) ← `AdminStyleViewEventProducer#sendViewEvent` [adminstyle], `InternalStyleViewEventProducer#sendViewEvent` [internalstyle], `LegacyStyleViewEventProducer#sendViewEvent` [legacystyle], `PartnerStyleViewEventProducer#sendViewEvent` [partnerstyle], `SettlementStyleViewEventProducer#sendViewEvent` [settlementstyle], `StyleViewEventProducer#sendViewEvent` [style]

## 6. AOP

(없음)

