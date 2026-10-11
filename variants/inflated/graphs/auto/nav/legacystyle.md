# legacystyle 도메인

기준 경로: `src/main/java/com/fream/back/domain/legacystyle/` · 파일 39개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/legacy/hashtags/commands` | `LegacyHashtagCommandController#createHashtag` | `LegacyHashtagCommandService#create` |
| PUT | `/legacy/hashtags/commands/{id}` | `LegacyHashtagCommandController#updateHashtag` | `LegacyHashtagCommandService#update` |
| DELETE | `/legacy/hashtags/commands/{id}` | `LegacyHashtagCommandController#deleteHashtag` | `LegacyHashtagCommandService#delete` |
| POST | `/legacy/styles/commands` | `LegacyStyleCommandController#createStyle` | `LegacyStyleCommandService#createStyle`, `NginxCachePurgeUtil#purgeStyleCache` |
| POST | `/legacy/styles/commands/{styleId}/view` | `LegacyStyleCommandController#incrementViewCount` | `LegacyStyleCommandService#incrementViewCount` |
| PUT | `/legacy/styles/commands/{styleId}` | `LegacyStyleCommandController#updateStyle` | `LegacyStyleCommandService#updateStyle`, `NginxCachePurgeUtil#purgeStyleCache` |
| DELETE | `/legacy/styles/commands/{styleId}` | `LegacyStyleCommandController#deleteStyle` | `LegacyStyleCommandService#deleteStyle`, `NginxCachePurgeUtil#purgeStyleCache` |
| POST | `/legacy/styles/comments/commands` | `LegacyStyleCommentCommandController#addComment` | `LegacyStyleCommentCommandService#addComment` |
| PUT | `/legacy/styles/comments/commands/{commentId}` | `LegacyStyleCommentCommandController#updateComment` | `LegacyStyleCommentCommandService#updateComment` |
| DELETE | `/legacy/styles/comments/commands/{commentId}` | `LegacyStyleCommentCommandController#deleteComment` | `LegacyStyleCommentCommandService#deleteComment` |
| POST | `/legacy/styles/comments/likes/commands/{commentId}/toggle` | `LegacyStyleCommentLikeCommandController#toggleCommentLike` | `LegacyStyleCommentLikeCommandService#toggleCommentLike` |
| POST | `/legacy/styles/interests/commands/{styleId}/toggle` | `LegacyStyleInterestCommandController#toggleInterest` | `LegacyStyleInterestCommandService#toggleStyleInterest` |
| POST | `/legacy/styles/likes/commands/{styleId}/toggle` | `LegacyStyleLikeCommandController#toggleLike` | `LegacyStyleLikeCommandService#toggleLike` |
| GET | `/legacy/hashtags/queries/{id}` | `LegacyHashtagQueryController#getHashtagById` | `LegacyHashtagQueryService#findById` |
| GET | `/legacy/hashtags/queries/name/{name}` | `LegacyHashtagQueryController#getHashtagByName` | `LegacyHashtagQueryService#findByName` |
| GET | `/legacy/hashtags/queries` | `LegacyHashtagQueryController#getAllHashtags` | `LegacyHashtagQueryService#findAll` |
| GET | `/legacy/hashtags/queries/popular` | `LegacyHashtagQueryController#getPopularHashtags` | `LegacyHashtagQueryService#findPopular` |
| GET | `/legacy/hashtags/queries/search` | `LegacyHashtagQueryController#searchHashtags` | `LegacyHashtagQueryService#search` |
| GET | `/legacy/hashtags/queries/autocomplete` | `LegacyHashtagQueryController#autocompleteHashtags` | `LegacyHashtagQueryService#autocomplete`, `LegacyHashtagQueryService#smartAutocomplete` |
| GET | `/legacy/styles/comments/queries/{styleId}` | `LegacyStyleCommentQueryController#getCommentsByStyleId` | `LegacyStyleCommentQueryService#getCommentsByStyleId` |
| GET | `/legacy/styles/queries/{styleId}/hashtags` | `LegacyStyleHashtagQueryController#getStyleHashtags` | `LegacyStyleHashtagQueryService#getHashtagsByStyleId` |
| GET | `/legacy/styles/queries/{styleId}` | `LegacyStyleQueryController#getStyleDetail` | `LegacyStyleQueryService#getStyleDetail`, `LegacyStyleViewEventProducer#sendViewEvent` |
| GET | `/legacy/styles/queries` | `LegacyStyleQueryController#getFilteredStyles` | `LegacyStyleQueryService#getFilteredStyles` |
| GET | `/legacy/styles/queries/profile/{profileId}` | `LegacyStyleQueryController#getStylesByProfile` | `LegacyStyleQueryService#getStylesByProfile` |
| GET | `/legacy/styles/queries/{styleId}/media/{fileName}` | `LegacyStyleQueryController#getStyleMedia` |  |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `LegacyHashtagCommandService`
  - `create` → LegacyHashtagRepository#existsByName, LegacyHashtagRepository#save~, this#convertToDto
  - `update` → LegacyHashtagRepository#findById~, LegacyHashtagRepository#existsByName, LegacyHashtagRepository#save~, this#convertToDto
  - `delete` → LegacyHashtagRepository#findById~, LegacyStyleHashtagRepository#countByHashtagId, LegacyHashtagRepository#delete~
- `LegacyHashtagQueryService`
  - `findById` → LegacyHashtagRepository#findById~, this#convertToDto
  - `findByName` → LegacyHashtagRepository#findByName, this#convertToDto
  - `findAll` → LegacyHashtagRepository#findAll~
  - `findPopular` → LegacyHashtagRepository#findAllByOrderByCountDesc
  - `search` → LegacyHashtagRepository#findByNameContainingOrderByCountDesc
  - `autocomplete` → this#isKoreanChosung, this#chosungAutocomplete, this#koreanAutocomplete, this#englishAutocomplete
  - `englishAutocomplete` → LegacyHashtagRepository#findByNameStartingWithOrderByCountDesc
  - `koreanAutocomplete` → LegacyHashtagRepository#findByNameContainingKeywordOrderByCountDesc
  - `chosungAutocomplete` → LegacyHashtagRepository#findAll~, this#extractChosung
  - `smartAutocomplete` → this#containsKoreanCharacters, this#autocomplete
- `LegacyMediaUrlCommandService`
  - `saveMediaFile` → this#validateMediaFile, FileUtils#saveFile, MediaUrlRepository#save~
  - `deleteMediaUrl` → FileUtils#deleteFile, MediaUrlRepository#delete~
- `LegacyMediaUrlQueryService`
  - `findMediaUrlsByStyleId` → MediaUrlRepository#findByStyleId
  - `findMediaUrlById` → MediaUrlRepository#findById~
  - `findMediaUrlsByUrl` → MediaUrlRepository#findByUrl
- `LegacyStyleCommandService`
  - `createStyle` → LegacyProfileQueryService#getProfileByEmail, StyleRepository#save~, LegacyStyleOrderItemCommandService#createStyleOrderItem, LegacyMediaUrlCommandService#saveMediaFile, this#processHashtags
  - `processHashtags` → LegacyHashtagRepository#findByName, LegacyHashtagRepository#findById~, LegacyHashtagCommandService#create, LegacyStyleHashtagCommandService#addHashtagToStyle
  - `updateStyle` → StyleRepository#findById~, LegacyMediaUrlCommandService#deleteMediaUrl, LegacyMediaUrlCommandService#saveMediaFile, LegacyStyleHashtagCommandService#removeAllHashtagsFromStyle, this#processHashtags, StyleRepository#save~
  - `deleteStyle` → StyleRepository#findById~, LegacyStyleHashtagCommandService#removeAllHashtagsFromStyle, LegacyMediaUrlCommandService#deleteMediaUrl, StyleRepository#delete~
  - `incrementViewCount` → StyleRepository#findById~, StyleRepository#save~
- `LegacyStyleCommentCommandService`
  - `addComment` → LegacyProfileQueryService#getProfileByEmail, LegacyStyleQueryService#findStyleById, LegacyStyleCommentRepository#findById~, LegacyStyleCommentRepository#save~
  - `updateComment` → LegacyStyleCommentRepository#findById~, LegacyStyleCommentRepository#save~
  - `deleteComment` → LegacyStyleCommentRepository#findById~, LegacyStyleCommentRepository#delete~
- `LegacyStyleCommentLikeCommandService`
  - `toggleCommentLike` → LegacyProfileQueryService#getProfileByEmail, LegacyStyleCommentQueryService#findById, LegacyStyleCommentLikeRepository#findByCommentAndProfile, LegacyStyleCommentLikeRepository#delete~, LegacyStyleCommentLikeRepository#save~
- `LegacyStyleCommentLikeQueryService`
  - `isCommentLikedByProfile` → LegacyStyleCommentLikeRepository#existsByCommentIdAndProfileId
  - `findByCommentId` → LegacyStyleCommentLikeRepository#findByCommentId
  - `getLikedCommentIds` → LegacyProfileQueryService#getProfileByEmail, LegacyStyleCommentLikeRepository#findByProfileIdAndCommentIdIn
- `LegacyStyleCommentQueryService`
  - `getCommentsByStyleId` → LegacyStyleCommentRepository#findRootCommentsByStyleId, LegacyStyleCommentRepository#countCommentsByStyleId, LegacyStyleCommentRepository#findRepliesByParentCommentId, LegacyStyleCommentLikeQueryService#getLikedCommentIds, this#convertToResponseDtos, LegacyProfileQueryService#getProfileByEmail
  - `findCommentById` → LegacyStyleCommentRepository#findById~
  - `checkUserLikedComment` → LegacyProfileQueryService#getProfileByEmail, LegacyStyleCommentLikeQueryService#isCommentLikedByProfile
  - `findByStyleId` → LegacyStyleCommentRepository#findByStyleId
  - `findById` → LegacyStyleCommentRepository#findById~
  - `findByProfileId` → LegacyStyleCommentRepository#findByProfileId
- `LegacyStyleHashtagCommandService`
  - `addHashtagToStyle` → LegacyStyleHashtagRepository#findByStyleIdAndHashtagId, LegacyHashtagRepository#save~, LegacyStyleHashtagRepository#save~
  - `removeAllHashtagsFromStyle` → LegacyStyleHashtagRepository#findByStyleId, LegacyHashtagRepository#save~, LegacyStyleHashtagRepository#deleteByStyleId
  - `removeHashtagFromStyle` → LegacyStyleHashtagRepository#findByStyleIdAndHashtagId, LegacyHashtagRepository#save~, LegacyStyleHashtagRepository#delete~
- `LegacyStyleHashtagQueryService`
  - `getHashtagsByStyleId` → LegacyStyleHashtagRepository#findByStyleId
  - `getHashtagsByStyleIds` → LegacyStyleHashtagRepository#findByStyleIdIn
  - `getHashtagMapByStyleIds` → LegacyStyleHashtagRepository#findByStyleIdIn
- `LegacyStyleInterestCommandService`
  - `toggleStyleInterest` → LegacyProfileQueryService#getProfileByEmail, LegacyStyleQueryService#findStyleById, LegacyStyleInterestRepository#findByStyleAndProfile, LegacyStyleInterestRepository#delete~, LegacyStyleInterestRepository#save~
- `LegacyStyleInterestQueryService`
  - `isStyleInterestedByProfile` → LegacyStyleInterestRepository#existsByStyleIdAndProfileId
  - `findByStyleId` → LegacyStyleInterestRepository#findByStyleId
  - `getInterestedStyleIds` → LegacyStyleInterestRepository#findByProfileIdAndStyleIdIn
  - `checkUserInterested` → LegacyProfileQueryService#getProfileByEmail, LegacyStyleInterestRepository#existsByStyleIdAndProfileId
- `LegacyStyleLikeCommandService`
  - `toggleLike` → LegacyProfileQueryService#getProfileByEmail, LegacyStyleQueryService#findStyleById, LegacyStyleLikeRepository#findByStyleAndProfile, LegacyStyleLikeRepository#delete~, LegacyStyleLikeRepository#save~
- `LegacyStyleLikeQueryService`
  - `checkUserLiked` → LegacyProfileQueryService#getProfileByEmail, LegacyStyleLikeRepository#existsByStyleIdAndProfileId
  - `getLikedStyleIds` → LegacyProfileQueryService#getProfileByEmail, LegacyStyleLikeRepository#findByProfileIdAndStyleIdIn
  - `isLikedByProfile` → LegacyStyleLikeRepository#existsByStyleIdAndProfileId
  - `findLikesByStyleId` → LegacyStyleLikeRepository#findByStyleId
- `LegacyStyleOrderItemCommandService`
  - `createStyleOrderItem` → LegacyOrderItemQueryService#findById, StyleOrderItemRepository#save~
- `LegacyStyleQueryService`
  - `findStyleById` → StyleRepository#findById~
  - `findStylesByProfileId` → StyleRepository#findByProfileId
  - `getFilteredStyles` → StyleRepository#filterStyles, LegacyStyleHashtagQueryService#getHashtagMapByStyleIds, LegacyProfileQueryService#getProfileByEmail, LegacyStyleLikeQueryService#getLikedStyleIds, LegacyStyleInterestQueryService#getInterestedStyleIds
  - `getStylesByProfile` → StyleRepository#getStylesByProfile, LegacyStyleHashtagQueryService#getHashtagMapByStyleIds, LegacyStyleLikeQueryService#getLikedStyleIds
  - `getStyleDetail` → StyleRepository#getStyleDetail, LegacyStyleHashtagQueryService#getHashtagsByStyleId, LegacyProfileQueryService#getProfileByEmail, LegacyStyleLikeQueryService#isLikedByProfile, LegacyStyleInterestQueryService#isStyleInterestedByProfile
- `LegacyStyleViewEventConsumer`
  - `listen` → StyleRepository#findById~, StyleRepository#save~, this#flushBuffer
  - `flushBuffer` → LegacyStyleViewLogRepository#saveAll~
- `LegacyStyleViewEventKafkaConfig`
  - `styleViewEventKafkaTemplate` → this#styleViewEventProducerFactory
  - `styleViewEventKafkaListenerContainerFactory` → this#styleViewEventConsumerFactory
- `LegacyStyleViewEventProducer`
  - `sendViewEvent` → kafka:style-view-log-topic

## 3. 클래스 목록 (역할별)

### controller (10)

- `LegacyHashtagCommandController` controller/command/LegacyHashtagCommandController.java · public: createHashtag, updateHashtag, deleteHashtag · 주입: LegacyHashtagCommandService
- `LegacyStyleCommandController` controller/command/LegacyStyleCommandController.java · public: createStyle, incrementViewCount, updateStyle, deleteStyle · 주입: LegacyStyleCommandService, NginxCachePurgeUtil
- `LegacyStyleCommentCommandController` controller/command/LegacyStyleCommentCommandController.java · public: addComment, updateComment, deleteComment · 주입: LegacyStyleCommentCommandService
- `LegacyStyleCommentLikeCommandController` controller/command/LegacyStyleCommentLikeCommandController.java · public: toggleCommentLike · 주입: LegacyStyleCommentLikeCommandService
- `LegacyStyleInterestCommandController` controller/command/LegacyStyleInterestCommandController.java · public: toggleInterest · 주입: LegacyStyleInterestCommandService
- `LegacyStyleLikeCommandController` controller/command/LegacyStyleLikeCommandController.java · public: toggleLike · 주입: LegacyStyleLikeCommandService
- `LegacyHashtagQueryController` controller/query/LegacyHashtagQueryController.java · public: getHashtagById, getHashtagByName, getAllHashtags, getPopularHashtags, searchHashtags, autocompleteHashtags · 주입: LegacyHashtagQueryService
- `LegacyStyleCommentQueryController` controller/query/LegacyStyleCommentQueryController.java · public: getCommentsByStyleId · 주입: LegacyStyleCommentQueryService
- `LegacyStyleHashtagQueryController` controller/query/LegacyStyleHashtagQueryController.java · public: getStyleHashtags · 주입: LegacyStyleHashtagQueryService
- `LegacyStyleQueryController` controller/query/LegacyStyleQueryController.java · public: getStyleDetail, getFilteredStyles, getStylesByProfile, getStyleMedia · 주입: LegacyStyleQueryService, LegacyStyleViewEventProducer

### service-command (9)

- `LegacyHashtagCommandService` service/command/LegacyHashtagCommandService.java · public: create, update, delete · 주입: LegacyHashtagRepository, LegacyStyleHashtagRepository
- `LegacyMediaUrlCommandService` service/command/LegacyMediaUrlCommandService.java · public: saveMediaFile, deleteMediaUrl · 주입: MediaUrlRepository, FileUtils
- `LegacyStyleCommandService` service/command/LegacyStyleCommandService.java · public: createStyle, updateStyle, deleteStyle, incrementViewCount · 주입: StyleRepository, LegacyProfileQueryService, LegacyStyleOrderItemCommandService, LegacyMediaUrlCommandService, LegacyHashtagRepository, LegacyHashtagCommandService, LegacyStyleHashtagCommandService
- `LegacyStyleCommentCommandService` service/command/LegacyStyleCommentCommandService.java · public: addComment, updateComment, deleteComment · 주입: LegacyStyleCommentRepository, LegacyProfileQueryService, LegacyStyleQueryService
- `LegacyStyleCommentLikeCommandService` service/command/LegacyStyleCommentLikeCommandService.java · public: toggleCommentLike · 주입: LegacyStyleCommentLikeRepository, LegacyProfileQueryService, LegacyStyleCommentQueryService
- `LegacyStyleHashtagCommandService` service/command/LegacyStyleHashtagCommandService.java · public: addHashtagToStyle, removeAllHashtagsFromStyle, removeHashtagFromStyle · 주입: LegacyStyleHashtagRepository, LegacyHashtagRepository
- `LegacyStyleInterestCommandService` service/command/LegacyStyleInterestCommandService.java · public: toggleStyleInterest · 주입: LegacyStyleInterestRepository, LegacyProfileQueryService, LegacyStyleQueryService
- `LegacyStyleLikeCommandService` service/command/LegacyStyleLikeCommandService.java · public: toggleLike · 주입: LegacyStyleLikeRepository, LegacyProfileQueryService, LegacyStyleQueryService
- `LegacyStyleOrderItemCommandService` service/command/LegacyStyleOrderItemCommandService.java · public: createStyleOrderItem · 주입: StyleOrderItemRepository, LegacyOrderItemQueryService

### service-query (8)

- `LegacyHashtagQueryService` service/query/LegacyHashtagQueryService.java · public: findById, findByName, findAll, findPopular, search, autocomplete, smartAutocomplete · 주입: LegacyHashtagRepository
- `LegacyMediaUrlQueryService` service/query/LegacyMediaUrlQueryService.java · public: findMediaUrlsByStyleId, findMediaUrlById, findMediaUrlsByUrl · 주입: MediaUrlRepository
- `LegacyStyleCommentLikeQueryService` service/query/LegacyStyleCommentLikeQueryService.java · public: isCommentLikedByProfile, findByCommentId, getLikedCommentIds · 주입: LegacyStyleCommentLikeRepository, LegacyProfileQueryService
- `LegacyStyleCommentQueryService` service/query/LegacyStyleCommentQueryService.java · public: getCommentsByStyleId, findCommentById, checkUserLikedComment, findByStyleId, findById, findByProfileId · 주입: LegacyStyleCommentRepository, LegacyStyleCommentLikeQueryService, LegacyProfileQueryService
- `LegacyStyleHashtagQueryService` service/query/LegacyStyleHashtagQueryService.java · public: getHashtagsByStyleId, getHashtagsByStyleIds, getHashtagMapByStyleIds · 주입: LegacyStyleHashtagRepository
- `LegacyStyleInterestQueryService` service/query/LegacyStyleInterestQueryService.java · public: isStyleInterestedByProfile, findByStyleId, getInterestedStyleIds, checkUserInterested · 주입: LegacyStyleInterestRepository, LegacyProfileQueryService
- `LegacyStyleLikeQueryService` service/query/LegacyStyleLikeQueryService.java · public: checkUserLiked, getLikedStyleIds, isLikedByProfile, findLikesByStyleId · 주입: LegacyStyleLikeRepository, LegacyProfileQueryService
- `LegacyStyleQueryService` service/query/LegacyStyleQueryService.java · public: findStyleById, findStylesByProfileId, getFilteredStyles, getStylesByProfile, getStyleDetail · 주입: StyleRepository, LegacyStyleLikeQueryService, LegacyStyleInterestQueryService, LegacyStyleHashtagQueryService, LegacyProfileQueryService

### repository (7)

- `LegacyHashtagRepository` (interface) repository/LegacyHashtagRepository.java · extends/implements JpaRepository · public: findByName, findByNameStartingWithOrderByCountDesc, findByNameContainingKeywordOrderByCountDesc, findAllByOrderByCountDesc, findByNameContainingOrderByCountDesc, existsByName
- `LegacyStyleCommentLikeRepository` (interface) repository/LegacyStyleCommentLikeRepository.java · extends/implements JpaRepository · public: findByCommentAndProfile, findByCommentIdAndProfileId, existsByCommentIdAndProfileId, findByCommentId, findByProfileIdAndCommentIdIn, findCommentIdsByProfileIdAndCommentIdIn, countByCommentId, deleteByProfileIdAndCommentId
- `LegacyStyleCommentRepository` (interface) repository/LegacyStyleCommentRepository.java · extends/implements JpaRepository · public: findRootCommentsByStyleId, countCommentsByStyleId, findRepliesByParentCommentId, findByStyleId, findByProfileId
- `LegacyStyleHashtagRepository` (interface) repository/LegacyStyleHashtagRepository.java · extends/implements JpaRepository · public: findByStyleId, findByStyleIdIn, findStyleIdsByHashtagId, deleteByStyleId, findByStyleIdAndHashtagId, countByHashtagId
- `LegacyStyleInterestRepository` (interface) repository/LegacyStyleInterestRepository.java · extends/implements JpaRepository · public: existsByStyleIdAndProfileId, findByStyleId, findByStyleAndProfile, findByStyleIdAndProfileId, findByProfileIdAndStyleIdIn, findStyleIdsByProfileIdAndStyleIdIn, countByStyleId, deleteByProfileIdAndStyleId
- `LegacyStyleLikeRepository` (interface) repository/LegacyStyleLikeRepository.java · extends/implements JpaRepository · public: findByStyleAndProfile, existsByStyleIdAndProfileId, findByStyleId, findByProfileIdAndStyleId, findByProfileIdAndStyleIdIn, findStyleIdsByProfileIdAndStyleIdIn, countByStyleId, deleteByProfileIdAndStyleId
- `LegacyStyleViewLogRepository` (interface) repository/LegacyStyleViewLogRepository.java · extends/implements JpaRepository

### custom (2)

- `LegacyStyleRepositoryCustom` (interface) repository/LegacyStyleRepositoryCustom.java · public: filterStyles, getStyleDetail, getStylesByProfile, styleCountByColorIds
- `LegacyStyleRepositoryCustomImpl` repository/LegacyStyleRepositoryCustomImpl.java · extends/implements LegacyStyleRepositoryCustom · public: filterStyles, getStyleDetail, getStylesByProfile, styleCountByColorIds · 주입: JPAQueryFactory

### event (2)

- `LegacyStyleViewEventConsumer` service/kafka/LegacyStyleViewEventConsumer.java · public: listen, flushBuffer · 주입: StyleRepository, LegacyStyleViewLogRepository
- `LegacyStyleViewEventProducer` service/kafka/LegacyStyleViewEventProducer.java · public: sendViewEvent · 주입: KafkaTemplate

### config (1)

- `LegacyStyleViewEventKafkaConfig` service/kafka/config/LegacyStyleViewEventKafkaConfig.java · public: styleViewEventProducerFactory, styleViewEventKafkaTemplate, styleViewEventConsumerFactory, styleViewEventKafkaListenerContainerFactory

### 다른 도메인 참조 (import·주입 기준)

- **legacyorder**: `LegacyOrderItemQueryService` ← LegacyStyleOrderItemCommandService
- **legacyuser**: `LegacyProfileQueryService` ← LegacyStyleCommandService, LegacyStyleCommentCommandService, LegacyStyleCommentLikeCommandService, LegacyStyleCommentLikeQueryService, LegacyStyleCommentQueryService, LegacyStyleInterestCommandService, LegacyStyleInterestQueryService, LegacyStyleLikeCommandService, LegacyStyleLikeQueryService, LegacyStyleQueryService
- **order**: `OrderItem` ← LegacyStyleOrderItemCommandService
- **style**: `Hashtag` ← LegacyHashtagCommandService, LegacyHashtagQueryService, LegacyHashtagRepository, LegacyStyleCommandService, LegacyStyleHashtagCommandService; `MediaUrl` ← LegacyMediaUrlCommandService, LegacyMediaUrlQueryService, LegacyStyleCommandService; `MediaUrlRepository` ← LegacyMediaUrlCommandService, LegacyMediaUrlQueryService; `Style` ← LegacyMediaUrlCommandService, LegacyStyleCommandController, LegacyStyleCommandService, LegacyStyleCommentCommandService, LegacyStyleHashtagCommandService, LegacyStyleInterestCommandService, LegacyStyleInterestRepository, LegacyStyleLikeCommandService, LegacyStyleLikeRepository, LegacyStyleOrderItemCommandService, LegacyStyleQueryService, LegacyStyleViewEventConsumer; `StyleComment` ← LegacyStyleCommentCommandController, LegacyStyleCommentCommandService, LegacyStyleCommentLikeCommandService, LegacyStyleCommentLikeRepository, LegacyStyleCommentQueryService, LegacyStyleCommentRepository; `StyleCommentLike` ← LegacyStyleCommentLikeCommandService, LegacyStyleCommentLikeQueryService, LegacyStyleCommentLikeRepository; `StyleHashtag` ← LegacyStyleHashtagCommandService, LegacyStyleHashtagQueryService, LegacyStyleHashtagRepository; `StyleInterest` ← LegacyStyleInterestCommandService, LegacyStyleInterestQueryService, LegacyStyleInterestRepository; `StyleLike` ← LegacyStyleLikeCommandService, LegacyStyleLikeQueryService, LegacyStyleLikeRepository; `StyleOrderItem` ← LegacyStyleCommandService, LegacyStyleOrderItemCommandService; `StyleOrderItemRepository` ← LegacyStyleOrderItemCommandService; `StyleRepository` ← LegacyStyleCommandService, LegacyStyleQueryService, LegacyStyleViewEventConsumer; `StyleViewEvent` ← LegacyStyleViewEventConsumer, LegacyStyleViewEventKafkaConfig, LegacyStyleViewEventProducer; `StyleViewLog` ← LegacyStyleViewEventConsumer, LegacyStyleViewLogRepository
- **user**: `Gender` ← LegacyStyleQueryController, LegacyStyleViewEventProducer; `Profile` ← LegacyStyleCommandService, LegacyStyleCommentCommandService, LegacyStyleCommentLikeCommandService, LegacyStyleCommentLikeQueryService, LegacyStyleCommentLikeRepository, LegacyStyleCommentQueryService, LegacyStyleInterestCommandService, LegacyStyleInterestQueryService, LegacyStyleInterestRepository, LegacyStyleLikeCommandService, LegacyStyleLikeQueryService, LegacyStyleLikeRepository, LegacyStyleQueryService

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

- 발행 kafka `style-view-log-topic`: `LegacyStyleViewEventProducer#sendViewEvent` → `AdminStyleViewEventConsumer#listen` [adminstyle], `InternalStyleViewEventConsumer#listen` [internalstyle], `LegacyStyleViewEventConsumer#listen` [legacystyle], `PartnerStyleViewEventConsumer#listen` [partnerstyle], `SettlementStyleViewEventConsumer#listen` [settlementstyle], `StyleViewEventConsumer#listen` [style]
- 구독 kafka `style-view-log-topic`: `LegacyStyleViewEventConsumer#listen` (groupId=style-view-group containerFactory=styleViewEventKafkaListenerContainerFactory) ← `AdminStyleViewEventProducer#sendViewEvent` [adminstyle], `InternalStyleViewEventProducer#sendViewEvent` [internalstyle], `LegacyStyleViewEventProducer#sendViewEvent` [legacystyle], `PartnerStyleViewEventProducer#sendViewEvent` [partnerstyle], `SettlementStyleViewEventProducer#sendViewEvent` [settlementstyle], `StyleViewEventProducer#sendViewEvent` [style]

## 6. AOP

(없음)

