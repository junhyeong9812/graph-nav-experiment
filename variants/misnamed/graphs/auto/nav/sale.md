# sale 도메인

기준 경로: `src/main/java/com/fream/back/domain/sale/` · 파일 67개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/hashtags/commands` | `HashtagCommandController#createHashtag` | `HashtagCommandService#create` |
| PUT | `/hashtags/commands/{id}` | `HashtagCommandController#updateHashtag` | `HashtagCommandService#update` |
| DELETE | `/hashtags/commands/{id}` | `HashtagCommandController#deleteHashtag` | `HashtagCommandService#delete` |
| POST | `/styles/commands` | `SaleCommandController#createStyle` | `SaleCommandService#createStyle`, `NginxCachePurgeUtil#purgeStyleCache` |
| POST | `/styles/commands/{styleId}/view` | `SaleCommandController#incrementViewCount` | `SaleCommandService#incrementViewCount` |
| PUT | `/styles/commands/{styleId}` | `SaleCommandController#updateStyle` | `SaleCommandService#updateStyle`, `NginxCachePurgeUtil#purgeStyleCache` |
| DELETE | `/styles/commands/{styleId}` | `SaleCommandController#deleteStyle` | `SaleCommandService#deleteStyle`, `NginxCachePurgeUtil#purgeStyleCache` |
| POST | `/styles/comments/commands` | `SaleCommentCommandController#addComment` | `SaleCommentCommandService#addComment` |
| PUT | `/styles/comments/commands/{commentId}` | `SaleCommentCommandController#updateComment` | `SaleCommentCommandService#updateComment` |
| DELETE | `/styles/comments/commands/{commentId}` | `SaleCommentCommandController#deleteComment` | `SaleCommentCommandService#deleteComment` |
| POST | `/styles/comments/likes/commands/{commentId}/toggle` | `SaleCommentLikeCommandController#toggleCommentLike` | `SaleCommentLikeCommandService#toggleCommentLike` |
| POST | `/styles/interests/commands/{styleId}/toggle` | `SaleInterestCommandController#toggleInterest` | `SaleInterestCommandService#toggleStyleInterest` |
| POST | `/styles/likes/commands/{styleId}/toggle` | `SaleLikeCommandController#toggleLike` | `SaleLikeCommandService#toggleLike` |
| GET | `/hashtags/queries/{id}` | `HashtagQueryController#getHashtagById` | `HashtagQueryService#findById` |
| GET | `/hashtags/queries/name/{name}` | `HashtagQueryController#getHashtagByName` | `HashtagQueryService#findByName` |
| GET | `/hashtags/queries` | `HashtagQueryController#getAllHashtags` | `HashtagQueryService#findAll` |
| GET | `/hashtags/queries/popular` | `HashtagQueryController#getPopularHashtags` | `HashtagQueryService#findPopular` |
| GET | `/hashtags/queries/search` | `HashtagQueryController#searchHashtags` | `HashtagQueryService#search` |
| GET | `/hashtags/queries/autocomplete` | `HashtagQueryController#autocompleteHashtags` | `HashtagQueryService#autocomplete`, `HashtagQueryService#smartAutocomplete` |
| GET | `/styles/comments/queries/{styleId}` | `SaleCommentQueryController#getCommentsByStyleId` | `SaleCommentQueryService#getCommentsByStyleId` |
| GET | `/styles/queries/{styleId}/hashtags` | `SaleHashtagQueryController#getStyleHashtags` | `SaleHashtagQueryService#getHashtagsByStyleId` |
| GET | `/styles/queries/{styleId}` | `SaleQueryController#getStyleDetail` | `SaleQueryService#getStyleDetail`, `SaleViewEventProducer#sendViewEvent` |
| GET | `/styles/queries` | `SaleQueryController#getFilteredStyles` | `SaleQueryService#getFilteredStyles` |
| GET | `/styles/queries/profile/{profileId}` | `SaleQueryController#getStylesByProfile` | `SaleQueryService#getStylesByProfile` |
| GET | `/styles/queries/{styleId}/media/{fileName}` | `SaleQueryController#getStyleMedia` |  |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `HashtagCommandService`
  - `create` → HashtagRepository#existsByName, HashtagRepository#save~, this#convertToDto
  - `update` → HashtagRepository#findById~, HashtagRepository#existsByName, HashtagRepository#save~, this#convertToDto
  - `delete` → HashtagRepository#findById~, SaleHashtagRepository#countByHashtagId, HashtagRepository#delete~
- `HashtagQueryService`
  - `findById` → HashtagRepository#findById~, this#convertToDto
  - `findByName` → HashtagRepository#findByName, this#convertToDto
  - `findAll` → HashtagRepository#findAll~
  - `findPopular` → HashtagRepository#findAllByOrderByCountDesc
  - `search` → HashtagRepository#findByNameContainingOrderByCountDesc
  - `autocomplete` → this#isKoreanChosung, this#chosungAutocomplete, this#koreanAutocomplete, this#englishAutocomplete
  - `englishAutocomplete` → HashtagRepository#findByNameStartingWithOrderByCountDesc
  - `koreanAutocomplete` → HashtagRepository#findByNameContainingKeywordOrderByCountDesc
  - `chosungAutocomplete` → HashtagRepository#findAll~, this#extractChosung
  - `smartAutocomplete` → this#containsKoreanCharacters, this#autocomplete
- `MediaUrlCommandService`
  - `saveMediaFile` → this#validateMediaFile, FileUtils#saveFile, MediaUrlRepository#save~
  - `deleteMediaUrl` → FileUtils#deleteFile, MediaUrlRepository#delete~
- `MediaUrlQueryService`
  - `findMediaUrlsByStyleId` → MediaUrlRepository#findByStyleId
  - `findMediaUrlById` → MediaUrlRepository#findById~
  - `findMediaUrlsByUrl` → MediaUrlRepository#findByUrl
- `SaleCommandService`
  - `createStyle` → ProfileQueryService#getProfileByEmail, SaleRepository#save~, SaleFaqItemCommandService#createStyleOrderItem, MediaUrlCommandService#saveMediaFile, this#processHashtags
  - `processHashtags` → HashtagRepository#findByName, HashtagRepository#findById~, HashtagCommandService#create, SaleHashtagCommandService#addHashtagToStyle
  - `updateStyle` → SaleRepository#findById~, MediaUrlCommandService#deleteMediaUrl, MediaUrlCommandService#saveMediaFile, SaleHashtagCommandService#removeAllHashtagsFromStyle, this#processHashtags, SaleRepository#save~
  - `deleteStyle` → SaleRepository#findById~, SaleHashtagCommandService#removeAllHashtagsFromStyle, MediaUrlCommandService#deleteMediaUrl, SaleRepository#delete~
  - `incrementViewCount` → SaleRepository#findById~, SaleRepository#save~
- `SaleCommentCommandService`
  - `addComment` → ProfileQueryService#getProfileByEmail, SaleQueryService#findStyleById, SaleCommentRepository#findById~, SaleCommentRepository#save~
  - `updateComment` → SaleCommentRepository#findById~, SaleCommentRepository#save~
  - `deleteComment` → SaleCommentRepository#findById~, SaleCommentRepository#delete~
- `SaleCommentLikeCommandService`
  - `toggleCommentLike` → ProfileQueryService#getProfileByEmail, SaleCommentQueryService#findById, SaleCommentLikeRepository#findByCommentAndProfile, SaleCommentLikeRepository#delete~, SaleCommentLikeRepository#save~
- `SaleCommentLikeQueryService`
  - `isCommentLikedByProfile` → SaleCommentLikeRepository#existsByCommentIdAndProfileId
  - `findByCommentId` → SaleCommentLikeRepository#findByCommentId
  - `getLikedCommentIds` → ProfileQueryService#getProfileByEmail, SaleCommentLikeRepository#findByProfileIdAndCommentIdIn
- `SaleCommentQueryService`
  - `getCommentsByStyleId` → SaleCommentRepository#findRootCommentsByStyleId, SaleCommentRepository#countCommentsByStyleId, SaleCommentRepository#findRepliesByParentCommentId, SaleCommentLikeQueryService#getLikedCommentIds, this#convertToResponseDtos, ProfileQueryService#getProfileByEmail
  - `findCommentById` → SaleCommentRepository#findById~
  - `checkUserLikedComment` → ProfileQueryService#getProfileByEmail, SaleCommentLikeQueryService#isCommentLikedByProfile
  - `findByStyleId` → SaleCommentRepository#findByStyleId
  - `findById` → SaleCommentRepository#findById~
  - `findByProfileId` → SaleCommentRepository#findByProfileId
- `SaleFaqItemCommandService`
  - `createStyleOrderItem` → FaqItemQueryService#findById, SaleFaqItemRepository#save~
- `SaleHashtagCommandService`
  - `addHashtagToStyle` → SaleHashtagRepository#findByStyleIdAndHashtagId, HashtagRepository#save~, SaleHashtagRepository#save~
  - `removeAllHashtagsFromStyle` → SaleHashtagRepository#findByStyleId, HashtagRepository#save~, SaleHashtagRepository#deleteByStyleId
  - `removeHashtagFromStyle` → SaleHashtagRepository#findByStyleIdAndHashtagId, HashtagRepository#save~, SaleHashtagRepository#delete~
- `SaleHashtagQueryService`
  - `getHashtagsByStyleId` → SaleHashtagRepository#findByStyleId
  - `getHashtagsByStyleIds` → SaleHashtagRepository#findByStyleIdIn
  - `getHashtagMapByStyleIds` → SaleHashtagRepository#findByStyleIdIn
- `SaleInterestCommandService`
  - `toggleStyleInterest` → ProfileQueryService#getProfileByEmail, SaleQueryService#findStyleById, SaleInterestRepository#findByStyleAndProfile, SaleInterestRepository#delete~, SaleInterestRepository#save~
- `SaleInterestQueryService`
  - `isStyleInterestedByProfile` → SaleInterestRepository#existsByStyleIdAndProfileId
  - `findByStyleId` → SaleInterestRepository#findByStyleId
  - `getInterestedStyleIds` → SaleInterestRepository#findByProfileIdAndStyleIdIn
  - `checkUserInterested` → ProfileQueryService#getProfileByEmail, SaleInterestRepository#existsByStyleIdAndProfileId
- `SaleLikeCommandService`
  - `toggleLike` → ProfileQueryService#getProfileByEmail, SaleQueryService#findStyleById, SaleLikeRepository#findByStyleAndProfile, SaleLikeRepository#delete~, SaleLikeRepository#save~
- `SaleLikeQueryService`
  - `checkUserLiked` → ProfileQueryService#getProfileByEmail, SaleLikeRepository#existsByStyleIdAndProfileId
  - `getLikedStyleIds` → ProfileQueryService#getProfileByEmail, SaleLikeRepository#findByProfileIdAndStyleIdIn
  - `isLikedByProfile` → SaleLikeRepository#existsByStyleIdAndProfileId
  - `findLikesByStyleId` → SaleLikeRepository#findByStyleId
- `SaleQueryService`
  - `findStyleById` → SaleRepository#findById~
  - `findStylesByProfileId` → SaleRepository#findByProfileId
  - `getFilteredStyles` → SaleRepository#filterStyles, SaleHashtagQueryService#getHashtagMapByStyleIds, ProfileQueryService#getProfileByEmail, SaleLikeQueryService#getLikedStyleIds, SaleInterestQueryService#getInterestedStyleIds
  - `getStylesByProfile` → SaleRepository#getStylesByProfile, SaleHashtagQueryService#getHashtagMapByStyleIds, SaleLikeQueryService#getLikedStyleIds
  - `getStyleDetail` → SaleRepository#getStyleDetail, SaleHashtagQueryService#getHashtagsByStyleId, ProfileQueryService#getProfileByEmail, SaleLikeQueryService#isLikedByProfile, SaleInterestQueryService#isStyleInterestedByProfile
- `SaleViewEventConsumer`
  - `listen` → SaleRepository#findById~, SaleRepository#save~, this#flushBuffer
  - `flushBuffer` → SaleViewLogRepository#saveAll~
- `SaleViewEventKafkaConfig`
  - `styleViewEventKafkaTemplate` → this#styleViewEventProducerFactory
  - `styleViewEventKafkaListenerContainerFactory` → this#styleViewEventConsumerFactory
- `SaleViewEventProducer`
  - `sendViewEvent` → kafka:style-view-log-topic
- `SaleViewLogBufferManager`
  - `flushBufferPeriodically` → SaleViewEventConsumer#flushBuffer
  - `flushBufferOnShutdown` → SaleViewEventConsumer#flushBuffer

## 3. 클래스 목록 (역할별)

### controller (10)

- `HashtagCommandController` controller/command/HashtagCommandController.java · public: createHashtag, updateHashtag, deleteHashtag · 주입: HashtagCommandService
- `SaleCommandController` controller/command/SaleCommandController.java · public: createStyle, incrementViewCount, updateStyle, deleteStyle · 주입: SaleCommandService, NginxCachePurgeUtil
- `SaleCommentCommandController` controller/command/SaleCommentCommandController.java · public: addComment, updateComment, deleteComment · 주입: SaleCommentCommandService
- `SaleCommentLikeCommandController` controller/command/SaleCommentLikeCommandController.java · public: toggleCommentLike · 주입: SaleCommentLikeCommandService
- `SaleInterestCommandController` controller/command/SaleInterestCommandController.java · public: toggleInterest · 주입: SaleInterestCommandService
- `SaleLikeCommandController` controller/command/SaleLikeCommandController.java · public: toggleLike · 주입: SaleLikeCommandService
- `HashtagQueryController` controller/query/HashtagQueryController.java · public: getHashtagById, getHashtagByName, getAllHashtags, getPopularHashtags, searchHashtags, autocompleteHashtags · 주입: HashtagQueryService
- `SaleCommentQueryController` controller/query/SaleCommentQueryController.java · public: getCommentsByStyleId · 주입: SaleCommentQueryService
- `SaleHashtagQueryController` controller/query/SaleHashtagQueryController.java · public: getStyleHashtags · 주입: SaleHashtagQueryService
- `SaleQueryController` controller/query/SaleQueryController.java · public: getStyleDetail, getFilteredStyles, getStylesByProfile, getStyleMedia · 주입: SaleQueryService, SaleViewEventProducer

### service-command (9)

- `HashtagCommandService` service/command/HashtagCommandService.java · public: create, update, delete · 주입: HashtagRepository, SaleHashtagRepository
- `MediaUrlCommandService` service/command/MediaUrlCommandService.java · public: saveMediaFile, deleteMediaUrl · 주입: MediaUrlRepository, FileUtils
- `SaleCommandService` service/command/SaleCommandService.java · public: createStyle, updateStyle, deleteStyle, incrementViewCount · 주입: SaleRepository, ProfileQueryService, SaleFaqItemCommandService, MediaUrlCommandService, HashtagRepository, HashtagCommandService, SaleHashtagCommandService
- `SaleCommentCommandService` service/command/SaleCommentCommandService.java · public: addComment, updateComment, deleteComment · 주입: SaleCommentRepository, ProfileQueryService, SaleQueryService
- `SaleCommentLikeCommandService` service/command/SaleCommentLikeCommandService.java · public: toggleCommentLike · 주입: SaleCommentLikeRepository, ProfileQueryService, SaleCommentQueryService
- `SaleFaqItemCommandService` service/command/SaleFaqItemCommandService.java · public: createStyleOrderItem · 주입: SaleFaqItemRepository, FaqItemQueryService
- `SaleHashtagCommandService` service/command/SaleHashtagCommandService.java · public: addHashtagToStyle, removeAllHashtagsFromStyle, removeHashtagFromStyle · 주입: SaleHashtagRepository, HashtagRepository
- `SaleInterestCommandService` service/command/SaleInterestCommandService.java · public: toggleStyleInterest · 주입: SaleInterestRepository, ProfileQueryService, SaleQueryService
- `SaleLikeCommandService` service/command/SaleLikeCommandService.java · public: toggleLike · 주입: SaleLikeRepository, ProfileQueryService, SaleQueryService

### service-query (8)

- `HashtagQueryService` service/query/HashtagQueryService.java · public: findById, findByName, findAll, findPopular, search, autocomplete, smartAutocomplete · 주입: HashtagRepository
- `MediaUrlQueryService` service/query/MediaUrlQueryService.java · public: findMediaUrlsByStyleId, findMediaUrlById, findMediaUrlsByUrl · 주입: MediaUrlRepository
- `SaleCommentLikeQueryService` service/query/SaleCommentLikeQueryService.java · public: isCommentLikedByProfile, findByCommentId, getLikedCommentIds · 주입: SaleCommentLikeRepository, ProfileQueryService
- `SaleCommentQueryService` service/query/SaleCommentQueryService.java · public: getCommentsByStyleId, findCommentById, checkUserLikedComment, findByStyleId, findById, findByProfileId · 주입: SaleCommentRepository, SaleCommentLikeQueryService, ProfileQueryService
- `SaleHashtagQueryService` service/query/SaleHashtagQueryService.java · public: getHashtagsByStyleId, getHashtagsByStyleIds, getHashtagMapByStyleIds · 주입: SaleHashtagRepository
- `SaleInterestQueryService` service/query/SaleInterestQueryService.java · public: isStyleInterestedByProfile, findByStyleId, getInterestedStyleIds, checkUserInterested · 주입: SaleInterestRepository, ProfileQueryService
- `SaleLikeQueryService` service/query/SaleLikeQueryService.java · public: checkUserLiked, getLikedStyleIds, isLikedByProfile, findLikesByStyleId · 주입: SaleLikeRepository, ProfileQueryService
- `SaleQueryService` service/query/SaleQueryService.java · public: findStyleById, findStylesByProfileId, getFilteredStyles, getStylesByProfile, getStyleDetail · 주입: SaleRepository, SaleLikeQueryService, SaleInterestQueryService, SaleHashtagQueryService, ProfileQueryService

### service (1)

- `SaleViewLogBufferManager` service/scheduler/SaleViewLogBufferManager.java · public: flushBufferPeriodically, flushBufferOnShutdown · 주입: SaleViewEventConsumer

### repository (10)

- `HashtagRepository` (interface) repository/HashtagRepository.java · extends/implements JpaRepository · public: findByName, findByNameStartingWithOrderByCountDesc, findByNameContainingKeywordOrderByCountDesc, findAllByOrderByCountDesc, findByNameContainingOrderByCountDesc, existsByName
- `MediaUrlRepository` (interface) repository/MediaUrlRepository.java · extends/implements JpaRepository · public: findByStyleId, findByUrl
- `SaleCommentLikeRepository` (interface) repository/SaleCommentLikeRepository.java · extends/implements JpaRepository · public: findByCommentAndProfile, findByCommentIdAndProfileId, existsByCommentIdAndProfileId, findByCommentId, findByProfileIdAndCommentIdIn, findCommentIdsByProfileIdAndCommentIdIn, countByCommentId, deleteByProfileIdAndCommentId
- `SaleCommentRepository` (interface) repository/SaleCommentRepository.java · extends/implements JpaRepository · public: findRootCommentsByStyleId, countCommentsByStyleId, findRepliesByParentCommentId, findByStyleId, findByProfileId
- `SaleFaqItemRepository` (interface) repository/SaleFaqItemRepository.java · extends/implements JpaRepository
- `SaleHashtagRepository` (interface) repository/SaleHashtagRepository.java · extends/implements JpaRepository · public: findByStyleId, findByStyleIdIn, findStyleIdsByHashtagId, deleteByStyleId, findByStyleIdAndHashtagId, countByHashtagId
- `SaleInterestRepository` (interface) repository/SaleInterestRepository.java · extends/implements JpaRepository · public: existsByStyleIdAndProfileId, findByStyleId, findByStyleAndProfile, findByStyleIdAndProfileId, findByProfileIdAndStyleIdIn, findStyleIdsByProfileIdAndStyleIdIn, countByStyleId, deleteByProfileIdAndStyleId
- `SaleLikeRepository` (interface) repository/SaleLikeRepository.java · extends/implements JpaRepository · public: findByStyleAndProfile, existsByStyleIdAndProfileId, findByStyleId, findByProfileIdAndStyleId, findByProfileIdAndStyleIdIn, findStyleIdsByProfileIdAndStyleIdIn, countByStyleId, deleteByProfileIdAndStyleId
- `SaleRepository` (interface) repository/SaleRepository.java · extends/implements JpaRepository, SaleRepositoryCustom · public: findByProfileId
- `SaleViewLogRepository` (interface) repository/SaleViewLogRepository.java · extends/implements JpaRepository

### custom (2)

- `SaleRepositoryCustom` (interface) repository/SaleRepositoryCustom.java · public: filterStyles, getStyleDetail, getStylesByProfile, styleCountByColorIds
- `SaleRepositoryCustomImpl` repository/SaleRepositoryCustomImpl.java · extends/implements SaleRepositoryCustom · public: filterStyles, getStyleDetail, getStylesByProfile, styleCountByColorIds · 주입: JPAQueryFactory

### entity (10)

- `Hashtag` entity/Hashtag.java · extends/implements BaseTimeEntity · public: incrementCount, decrementCount
- `MediaUrl` entity/MediaUrl.java · public: assignStyle, unassignStyle
- `Sale` entity/Sale.java · extends/implements BaseTimeEntity · public: incrementViewCount, updateContent, addLike, removeLike, removeInterest, addStyleOrderItem, addDataStyleOrderItem, addMediaUrl, addDataMediaUrl, addComment, addInterest, assignProfile, removeMediaUrl, removeStyleOrderItem, addStyleHashtag, removeStyleHashtag
- `SaleComment` entity/SaleComment.java · extends/implements BaseTimeEntity · public: assignStyle, assignProfile, addChildComment, setParentComment, addLike, updateContent
- `SaleCommentLike` entity/SaleCommentLike.java · extends/implements BaseTimeEntity · public: assignComment, assignProfile
- `SaleFaqItem` entity/SaleFaqItem.java · public: assignStyle, unassignStyle, assignOrderItem
- `SaleHashtag` entity/SaleHashtag.java · extends/implements BaseTimeEntity · public: assignStyle, assignHashtag
- `SaleInterest` entity/SaleInterest.java · extends/implements BaseTimeEntity · public: assignStyle, assignProfile
- `SaleLike` entity/SaleLike.java · extends/implements BaseTimeEntity · public: assignStyle, assignProfile
- `SaleViewLog` entity/SaleViewLog.java · public: create, addViewedAt

### event (3)

- `SaleViewEvent` dto/kafka/SaleViewEvent.java
- `SaleViewEventConsumer` service/kafka/SaleViewEventConsumer.java · public: listen, flushBuffer · 주입: SaleRepository, SaleViewLogRepository
- `SaleViewEventProducer` service/kafka/SaleViewEventProducer.java · public: sendViewEvent · 주입: KafkaTemplate

### config (1)

- `SaleViewEventKafkaConfig` service/kafka/config/SaleViewEventKafkaConfig.java · public: styleViewEventProducerFactory, styleViewEventKafkaTemplate, styleViewEventConsumerFactory, styleViewEventKafkaListenerContainerFactory

### dto (11)

- `AddCommentRequestDto` dto/AddCommentRequestDto.java
- `HashtagCreateRequestDto` dto/HashtagCreateRequestDto.java
- `HashtagResponseDto` dto/HashtagResponseDto.java
- `HashtagUpdateRequestDto` dto/HashtagUpdateRequestDto.java
- `ProfileSaleResponseDto` dto/ProfileSaleResponseDto.java
- `SaleCommentResponseDto` dto/SaleCommentResponseDto.java
- `SaleCommentsResponseDto` dto/SaleCommentsResponseDto.java
- `SaleDetailResponseDto` dto/SaleDetailResponseDto.java
- `SaleFilterRequestDto` dto/SaleFilterRequestDto.java
- `SaleResponseDto` dto/SaleResponseDto.java
- `UpdateCommentRequestDto` dto/UpdateCommentRequestDto.java

### exception (2)

- `SaleErrorCode` exception/SaleErrorCode.java
- `SaleException` exception/SaleException.java

### 다른 도메인 참조 (import·주입 기준)

- **faq**: `FaqItem` ← SaleFaqItem, SaleFaqItemCommandService; `FaqItemQueryService` ← SaleFaqItemCommandService
- **product**: `Gender` ← SaleQueryController, SaleViewEvent, SaleViewEventProducer, SaleViewLog; `Profile` ← Sale, SaleCommandService, SaleComment, SaleCommentCommandService, SaleCommentLike, SaleCommentLikeCommandService, SaleCommentLikeQueryService, SaleCommentLikeRepository, SaleCommentQueryService, SaleInterest, SaleInterestCommandService, SaleInterestQueryService, SaleInterestRepository, SaleLike, SaleLikeCommandService, SaleLikeQueryService, SaleLikeRepository, SaleQueryService; `ProfileQueryService` ← SaleCommandService, SaleCommentCommandService, SaleCommentLikeCommandService, SaleCommentLikeQueryService, SaleCommentQueryService, SaleInterestCommandService, SaleInterestQueryService, SaleLikeCommandService, SaleLikeQueryService, SaleQueryService

### 이 도메인을 참조하는 다른 도메인

- **global**: MediaUrl, MediaUrlRepository, Sale, SaleFaqItem, SaleFaqItemRepository, SaleRepository, SaleViewLogBufferManager
- **product**: Sale
- **user**: SaleRepository

## 4. JPA 엔티티 연관

- `Hashtag` @Entity @Table(indexes = { @Index(name = "idx_hashtag_name", columnList = "name", unique = true), @Index(name = "idx_hashtag_count", columnList = "count DESC") }) · extends BaseTimeEntity
- `MediaUrl` @Entity
  - style: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "style_id", nullable = false) → `Sale`
- `Sale` @Entity · extends BaseTimeEntity
  - profile: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "profile_id", nullable = false) → `Profile` [product]
  - styleOrderItems: @OneToMany(mappedBy = "style", cascade = CascadeType.ALL, orphanRemoval = true) → `SaleFaqItem`
  - mediaUrls: @OneToMany(mappedBy = "style", cascade = CascadeType.ALL, orphanRemoval = true) → `MediaUrl`
  - likes: @OneToMany(mappedBy = "style", cascade = CascadeType.ALL, orphanRemoval = true) → `SaleLike`
  - comments: @OneToMany(mappedBy = "style", cascade = CascadeType.ALL, orphanRemoval = true) → `SaleComment`
  - interests: @OneToMany(mappedBy = "style", cascade = CascadeType.ALL, orphanRemoval = true) → `SaleInterest`
  - hashtags: @OneToMany(mappedBy = "style", cascade = CascadeType.ALL, orphanRemoval = true) → `SaleHashtag`
- `SaleComment` @Entity · extends BaseTimeEntity
  - style: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "style_id", nullable = false) → `Sale`
  - profile: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "profile_id", nullable = false) → `Profile` [product]
  - parentComment: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "parent_comment_id", nullable = true) → `SaleComment`
  - childComments: @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true) → `SaleComment`
  - likes: @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true) → `SaleCommentLike`
- `SaleCommentLike` @Entity · extends BaseTimeEntity
  - comment: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "comment_id", nullable = false) → `SaleComment`
  - profile: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "profile_id", nullable = false) → `Profile` [product]
- `SaleFaqItem` @Entity
  - style: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "style_id") → `Sale`
  - orderItem: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "order_item_id") → `FaqItem` [faq]
- `SaleHashtag` @Entity @Table(name = "style_hashtag", indexes = { @Index(name = "idx_stylehashtag_style", columnList = "style_id"), @Index(name = "idx_stylehashtag_hashtag", columnList = "hashtag_id"), @Index(name = "idx_stylehashtag_unique", columnList = "style_id, hashtag_id", unique = true) }) · extends BaseTimeEntity
  - style: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "style_id", nullable = false) → `Sale`
  - hashtag: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "hashtag_id", nullable = false) → `Hashtag`
- `SaleInterest` @Entity · extends BaseTimeEntity
  - style: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "style_id", nullable = false) → `Sale`
  - profile: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "profile_id", nullable = false) → `Profile` [product]
- `SaleLike` @Entity · extends BaseTimeEntity
  - style: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "style_id", nullable = false) → `Sale`
  - profile: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "profile_id", nullable = false) → `Profile` [product]
- `SaleViewLog` @Entity
  - style: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "style_id") → `Sale`

다른 도메인 엔티티에서 들어오는 연관:

- `Profile.styles` @OneToMany → `Sale` [product]

## 5. 이벤트·메시징

- 발행 kafka `style-view-log-topic`: `SaleViewEventProducer#sendViewEvent` → `SaleViewEventConsumer#listen` [sale]
- 구독 kafka `style-view-log-topic`: `SaleViewEventConsumer#listen` (groupId=style-view-group containerFactory=styleViewEventKafkaListenerContainerFactory) ← `SaleViewEventProducer#sendViewEvent` [sale]

## 6. AOP

(없음)

