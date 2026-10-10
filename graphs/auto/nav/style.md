# style 도메인

기준 경로: `src/main/java/com/fream/back/domain/style/` · 파일 67개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/hashtags/commands` | `HashtagCommandController#createHashtag` | `HashtagCommandService#create` |
| PUT | `/hashtags/commands/{id}` | `HashtagCommandController#updateHashtag` | `HashtagCommandService#update` |
| DELETE | `/hashtags/commands/{id}` | `HashtagCommandController#deleteHashtag` | `HashtagCommandService#delete` |
| POST | `/styles/commands` | `StyleCommandController#createStyle` | `StyleCommandService#createStyle`, `NginxCachePurgeUtil#purgeStyleCache` |
| POST | `/styles/commands/{styleId}/view` | `StyleCommandController#incrementViewCount` | `StyleCommandService#incrementViewCount` |
| PUT | `/styles/commands/{styleId}` | `StyleCommandController#updateStyle` | `StyleCommandService#updateStyle`, `NginxCachePurgeUtil#purgeStyleCache` |
| DELETE | `/styles/commands/{styleId}` | `StyleCommandController#deleteStyle` | `StyleCommandService#deleteStyle`, `NginxCachePurgeUtil#purgeStyleCache` |
| POST | `/styles/comments/commands` | `StyleCommentCommandController#addComment` | `StyleCommentCommandService#addComment` |
| PUT | `/styles/comments/commands/{commentId}` | `StyleCommentCommandController#updateComment` | `StyleCommentCommandService#updateComment` |
| DELETE | `/styles/comments/commands/{commentId}` | `StyleCommentCommandController#deleteComment` | `StyleCommentCommandService#deleteComment` |
| POST | `/styles/comments/likes/commands/{commentId}/toggle` | `StyleCommentLikeCommandController#toggleCommentLike` | `StyleCommentLikeCommandService#toggleCommentLike` |
| POST | `/styles/interests/commands/{styleId}/toggle` | `StyleInterestCommandController#toggleInterest` | `StyleInterestCommandService#toggleStyleInterest` |
| POST | `/styles/likes/commands/{styleId}/toggle` | `StyleLikeCommandController#toggleLike` | `StyleLikeCommandService#toggleLike` |
| GET | `/hashtags/queries/{id}` | `HashtagQueryController#getHashtagById` | `HashtagQueryService#findById` |
| GET | `/hashtags/queries/name/{name}` | `HashtagQueryController#getHashtagByName` | `HashtagQueryService#findByName` |
| GET | `/hashtags/queries` | `HashtagQueryController#getAllHashtags` | `HashtagQueryService#findAll` |
| GET | `/hashtags/queries/popular` | `HashtagQueryController#getPopularHashtags` | `HashtagQueryService#findPopular` |
| GET | `/hashtags/queries/search` | `HashtagQueryController#searchHashtags` | `HashtagQueryService#search` |
| GET | `/hashtags/queries/autocomplete` | `HashtagQueryController#autocompleteHashtags` | `HashtagQueryService#autocomplete`, `HashtagQueryService#smartAutocomplete` |
| GET | `/styles/comments/queries/{styleId}` | `StyleCommentQueryController#getCommentsByStyleId` | `StyleCommentQueryService#getCommentsByStyleId` |
| GET | `/styles/queries/{styleId}/hashtags` | `StyleHashtagQueryController#getStyleHashtags` | `StyleHashtagQueryService#getHashtagsByStyleId` |
| GET | `/styles/queries/{styleId}` | `StyleQueryController#getStyleDetail` | `StyleQueryService#getStyleDetail`, `StyleViewEventProducer#sendViewEvent` |
| GET | `/styles/queries` | `StyleQueryController#getFilteredStyles` | `StyleQueryService#getFilteredStyles` |
| GET | `/styles/queries/profile/{profileId}` | `StyleQueryController#getStylesByProfile` | `StyleQueryService#getStylesByProfile` |
| GET | `/styles/queries/{styleId}/media/{fileName}` | `StyleQueryController#getStyleMedia` |  |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `HashtagCommandService`
  - `create` → HashtagRepository#existsByName, HashtagRepository#save~, this#convertToDto
  - `update` → HashtagRepository#findById~, HashtagRepository#existsByName, HashtagRepository#save~, this#convertToDto
  - `delete` → HashtagRepository#findById~, StyleHashtagRepository#countByHashtagId, HashtagRepository#delete~
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
- `StyleCommandService`
  - `createStyle` → ProfileQueryService#getProfileByEmail, StyleRepository#save~, StyleOrderItemCommandService#createStyleOrderItem, MediaUrlCommandService#saveMediaFile, this#processHashtags
  - `processHashtags` → HashtagRepository#findByName, HashtagRepository#findById~, HashtagCommandService#create, StyleHashtagCommandService#addHashtagToStyle
  - `updateStyle` → StyleRepository#findById~, MediaUrlCommandService#deleteMediaUrl, MediaUrlCommandService#saveMediaFile, StyleHashtagCommandService#removeAllHashtagsFromStyle, this#processHashtags, StyleRepository#save~
  - `deleteStyle` → StyleRepository#findById~, StyleHashtagCommandService#removeAllHashtagsFromStyle, MediaUrlCommandService#deleteMediaUrl, StyleRepository#delete~
  - `incrementViewCount` → StyleRepository#findById~, StyleRepository#save~
- `StyleCommentCommandService`
  - `addComment` → ProfileQueryService#getProfileByEmail, StyleQueryService#findStyleById, StyleCommentRepository#findById~, StyleCommentRepository#save~
  - `updateComment` → StyleCommentRepository#findById~, StyleCommentRepository#save~
  - `deleteComment` → StyleCommentRepository#findById~, StyleCommentRepository#delete~
- `StyleCommentLikeCommandService`
  - `toggleCommentLike` → ProfileQueryService#getProfileByEmail, StyleCommentQueryService#findById, StyleCommentLikeRepository#findByCommentAndProfile, StyleCommentLikeRepository#delete~, StyleCommentLikeRepository#save~
- `StyleCommentLikeQueryService`
  - `isCommentLikedByProfile` → StyleCommentLikeRepository#existsByCommentIdAndProfileId
  - `findByCommentId` → StyleCommentLikeRepository#findByCommentId
  - `getLikedCommentIds` → ProfileQueryService#getProfileByEmail, StyleCommentLikeRepository#findByProfileIdAndCommentIdIn
- `StyleCommentQueryService`
  - `getCommentsByStyleId` → StyleCommentRepository#findRootCommentsByStyleId, StyleCommentRepository#countCommentsByStyleId, StyleCommentRepository#findRepliesByParentCommentId, StyleCommentLikeQueryService#getLikedCommentIds, this#convertToResponseDtos, ProfileQueryService#getProfileByEmail
  - `findCommentById` → StyleCommentRepository#findById~
  - `checkUserLikedComment` → ProfileQueryService#getProfileByEmail, StyleCommentLikeQueryService#isCommentLikedByProfile
  - `findByStyleId` → StyleCommentRepository#findByStyleId
  - `findById` → StyleCommentRepository#findById~
  - `findByProfileId` → StyleCommentRepository#findByProfileId
- `StyleHashtagCommandService`
  - `addHashtagToStyle` → StyleHashtagRepository#findByStyleIdAndHashtagId, HashtagRepository#save~, StyleHashtagRepository#save~
  - `removeAllHashtagsFromStyle` → StyleHashtagRepository#findByStyleId, HashtagRepository#save~, StyleHashtagRepository#deleteByStyleId
  - `removeHashtagFromStyle` → StyleHashtagRepository#findByStyleIdAndHashtagId, HashtagRepository#save~, StyleHashtagRepository#delete~
- `StyleHashtagQueryService`
  - `getHashtagsByStyleId` → StyleHashtagRepository#findByStyleId
  - `getHashtagsByStyleIds` → StyleHashtagRepository#findByStyleIdIn
  - `getHashtagMapByStyleIds` → StyleHashtagRepository#findByStyleIdIn
- `StyleInterestCommandService`
  - `toggleStyleInterest` → ProfileQueryService#getProfileByEmail, StyleQueryService#findStyleById, StyleInterestRepository#findByStyleAndProfile, StyleInterestRepository#delete~, StyleInterestRepository#save~
- `StyleInterestQueryService`
  - `isStyleInterestedByProfile` → StyleInterestRepository#existsByStyleIdAndProfileId
  - `findByStyleId` → StyleInterestRepository#findByStyleId
  - `getInterestedStyleIds` → StyleInterestRepository#findByProfileIdAndStyleIdIn
  - `checkUserInterested` → ProfileQueryService#getProfileByEmail, StyleInterestRepository#existsByStyleIdAndProfileId
- `StyleLikeCommandService`
  - `toggleLike` → ProfileQueryService#getProfileByEmail, StyleQueryService#findStyleById, StyleLikeRepository#findByStyleAndProfile, StyleLikeRepository#delete~, StyleLikeRepository#save~
- `StyleLikeQueryService`
  - `checkUserLiked` → ProfileQueryService#getProfileByEmail, StyleLikeRepository#existsByStyleIdAndProfileId
  - `getLikedStyleIds` → ProfileQueryService#getProfileByEmail, StyleLikeRepository#findByProfileIdAndStyleIdIn
  - `isLikedByProfile` → StyleLikeRepository#existsByStyleIdAndProfileId
  - `findLikesByStyleId` → StyleLikeRepository#findByStyleId
- `StyleOrderItemCommandService`
  - `createStyleOrderItem` → OrderItemQueryService#findById, StyleOrderItemRepository#save~
- `StyleQueryService`
  - `findStyleById` → StyleRepository#findById~
  - `findStylesByProfileId` → StyleRepository#findByProfileId
  - `getFilteredStyles` → StyleRepository#filterStyles, StyleHashtagQueryService#getHashtagMapByStyleIds, ProfileQueryService#getProfileByEmail, StyleLikeQueryService#getLikedStyleIds, StyleInterestQueryService#getInterestedStyleIds
  - `getStylesByProfile` → StyleRepository#getStylesByProfile, StyleHashtagQueryService#getHashtagMapByStyleIds, StyleLikeQueryService#getLikedStyleIds
  - `getStyleDetail` → StyleRepository#getStyleDetail, StyleHashtagQueryService#getHashtagsByStyleId, ProfileQueryService#getProfileByEmail, StyleLikeQueryService#isLikedByProfile, StyleInterestQueryService#isStyleInterestedByProfile
- `StyleViewEventConsumer`
  - `listen` → StyleRepository#findById~, StyleRepository#save~, this#flushBuffer
  - `flushBuffer` → StyleViewLogRepository#saveAll~
- `StyleViewEventKafkaConfig`
  - `styleViewEventKafkaTemplate` → this#styleViewEventProducerFactory
  - `styleViewEventKafkaListenerContainerFactory` → this#styleViewEventConsumerFactory
- `StyleViewEventProducer`
  - `sendViewEvent` → kafka:style-view-log-topic
- `StyleViewLogBufferManager`
  - `flushBufferPeriodically` → StyleViewEventConsumer#flushBuffer
  - `flushBufferOnShutdown` → StyleViewEventConsumer#flushBuffer

## 3. 클래스 목록 (역할별)

### controller (10)

- `HashtagCommandController` controller/command/HashtagCommandController.java · public: createHashtag, updateHashtag, deleteHashtag · 주입: HashtagCommandService
- `StyleCommandController` controller/command/StyleCommandController.java · public: createStyle, incrementViewCount, updateStyle, deleteStyle · 주입: StyleCommandService, NginxCachePurgeUtil
- `StyleCommentCommandController` controller/command/StyleCommentCommandController.java · public: addComment, updateComment, deleteComment · 주입: StyleCommentCommandService
- `StyleCommentLikeCommandController` controller/command/StyleCommentLikeCommandController.java · public: toggleCommentLike · 주입: StyleCommentLikeCommandService
- `StyleInterestCommandController` controller/command/StyleInterestCommandController.java · public: toggleInterest · 주입: StyleInterestCommandService
- `StyleLikeCommandController` controller/command/StyleLikeCommandController.java · public: toggleLike · 주입: StyleLikeCommandService
- `HashtagQueryController` controller/query/HashtagQueryController.java · public: getHashtagById, getHashtagByName, getAllHashtags, getPopularHashtags, searchHashtags, autocompleteHashtags · 주입: HashtagQueryService
- `StyleCommentQueryController` controller/query/StyleCommentQueryController.java · public: getCommentsByStyleId · 주입: StyleCommentQueryService
- `StyleHashtagQueryController` controller/query/StyleHashtagQueryController.java · public: getStyleHashtags · 주입: StyleHashtagQueryService
- `StyleQueryController` controller/query/StyleQueryController.java · public: getStyleDetail, getFilteredStyles, getStylesByProfile, getStyleMedia · 주입: StyleQueryService, StyleViewEventProducer

### service-command (9)

- `HashtagCommandService` service/command/HashtagCommandService.java · public: create, update, delete · 주입: HashtagRepository, StyleHashtagRepository
- `MediaUrlCommandService` service/command/MediaUrlCommandService.java · public: saveMediaFile, deleteMediaUrl · 주입: MediaUrlRepository, FileUtils
- `StyleCommandService` service/command/StyleCommandService.java · public: createStyle, updateStyle, deleteStyle, incrementViewCount · 주입: StyleRepository, ProfileQueryService, StyleOrderItemCommandService, MediaUrlCommandService, HashtagRepository, HashtagCommandService, StyleHashtagCommandService
- `StyleCommentCommandService` service/command/StyleCommentCommandService.java · public: addComment, updateComment, deleteComment · 주입: StyleCommentRepository, ProfileQueryService, StyleQueryService
- `StyleCommentLikeCommandService` service/command/StyleCommentLikeCommandService.java · public: toggleCommentLike · 주입: StyleCommentLikeRepository, ProfileQueryService, StyleCommentQueryService
- `StyleHashtagCommandService` service/command/StyleHashtagCommandService.java · public: addHashtagToStyle, removeAllHashtagsFromStyle, removeHashtagFromStyle · 주입: StyleHashtagRepository, HashtagRepository
- `StyleInterestCommandService` service/command/StyleInterestCommandService.java · public: toggleStyleInterest · 주입: StyleInterestRepository, ProfileQueryService, StyleQueryService
- `StyleLikeCommandService` service/command/StyleLikeCommandService.java · public: toggleLike · 주입: StyleLikeRepository, ProfileQueryService, StyleQueryService
- `StyleOrderItemCommandService` service/command/StyleOrderItemCommandService.java · public: createStyleOrderItem · 주입: StyleOrderItemRepository, OrderItemQueryService

### service-query (8)

- `HashtagQueryService` service/query/HashtagQueryService.java · public: findById, findByName, findAll, findPopular, search, autocomplete, smartAutocomplete · 주입: HashtagRepository
- `MediaUrlQueryService` service/query/MediaUrlQueryService.java · public: findMediaUrlsByStyleId, findMediaUrlById, findMediaUrlsByUrl · 주입: MediaUrlRepository
- `StyleCommentLikeQueryService` service/query/StyleCommentLikeQueryService.java · public: isCommentLikedByProfile, findByCommentId, getLikedCommentIds · 주입: StyleCommentLikeRepository, ProfileQueryService
- `StyleCommentQueryService` service/query/StyleCommentQueryService.java · public: getCommentsByStyleId, findCommentById, checkUserLikedComment, findByStyleId, findById, findByProfileId · 주입: StyleCommentRepository, StyleCommentLikeQueryService, ProfileQueryService
- `StyleHashtagQueryService` service/query/StyleHashtagQueryService.java · public: getHashtagsByStyleId, getHashtagsByStyleIds, getHashtagMapByStyleIds · 주입: StyleHashtagRepository
- `StyleInterestQueryService` service/query/StyleInterestQueryService.java · public: isStyleInterestedByProfile, findByStyleId, getInterestedStyleIds, checkUserInterested · 주입: StyleInterestRepository, ProfileQueryService
- `StyleLikeQueryService` service/query/StyleLikeQueryService.java · public: checkUserLiked, getLikedStyleIds, isLikedByProfile, findLikesByStyleId · 주입: StyleLikeRepository, ProfileQueryService
- `StyleQueryService` service/query/StyleQueryService.java · public: findStyleById, findStylesByProfileId, getFilteredStyles, getStylesByProfile, getStyleDetail · 주입: StyleRepository, StyleLikeQueryService, StyleInterestQueryService, StyleHashtagQueryService, ProfileQueryService

### service (1)

- `StyleViewLogBufferManager` service/scheduler/StyleViewLogBufferManager.java · public: flushBufferPeriodically, flushBufferOnShutdown · 주입: StyleViewEventConsumer

### repository (10)

- `HashtagRepository` (interface) repository/HashtagRepository.java · extends/implements JpaRepository · public: findByName, findByNameStartingWithOrderByCountDesc, findByNameContainingKeywordOrderByCountDesc, findAllByOrderByCountDesc, findByNameContainingOrderByCountDesc, existsByName
- `MediaUrlRepository` (interface) repository/MediaUrlRepository.java · extends/implements JpaRepository · public: findByStyleId, findByUrl
- `StyleCommentLikeRepository` (interface) repository/StyleCommentLikeRepository.java · extends/implements JpaRepository · public: findByCommentAndProfile, findByCommentIdAndProfileId, existsByCommentIdAndProfileId, findByCommentId, findByProfileIdAndCommentIdIn, findCommentIdsByProfileIdAndCommentIdIn, countByCommentId, deleteByProfileIdAndCommentId
- `StyleCommentRepository` (interface) repository/StyleCommentRepository.java · extends/implements JpaRepository · public: findRootCommentsByStyleId, countCommentsByStyleId, findRepliesByParentCommentId, findByStyleId, findByProfileId
- `StyleHashtagRepository` (interface) repository/StyleHashtagRepository.java · extends/implements JpaRepository · public: findByStyleId, findByStyleIdIn, findStyleIdsByHashtagId, deleteByStyleId, findByStyleIdAndHashtagId, countByHashtagId
- `StyleInterestRepository` (interface) repository/StyleInterestRepository.java · extends/implements JpaRepository · public: existsByStyleIdAndProfileId, findByStyleId, findByStyleAndProfile, findByStyleIdAndProfileId, findByProfileIdAndStyleIdIn, findStyleIdsByProfileIdAndStyleIdIn, countByStyleId, deleteByProfileIdAndStyleId
- `StyleLikeRepository` (interface) repository/StyleLikeRepository.java · extends/implements JpaRepository · public: findByStyleAndProfile, existsByStyleIdAndProfileId, findByStyleId, findByProfileIdAndStyleId, findByProfileIdAndStyleIdIn, findStyleIdsByProfileIdAndStyleIdIn, countByStyleId, deleteByProfileIdAndStyleId
- `StyleOrderItemRepository` (interface) repository/StyleOrderItemRepository.java · extends/implements JpaRepository
- `StyleRepository` (interface) repository/StyleRepository.java · extends/implements JpaRepository, StyleRepositoryCustom · public: findByProfileId
- `StyleViewLogRepository` (interface) repository/StyleViewLogRepository.java · extends/implements JpaRepository

### custom (2)

- `StyleRepositoryCustom` (interface) repository/StyleRepositoryCustom.java · public: filterStyles, getStyleDetail, getStylesByProfile, styleCountByColorIds
- `StyleRepositoryCustomImpl` repository/StyleRepositoryCustomImpl.java · extends/implements StyleRepositoryCustom · public: filterStyles, getStyleDetail, getStylesByProfile, styleCountByColorIds · 주입: JPAQueryFactory

### entity (10)

- `Hashtag` entity/Hashtag.java · extends/implements BaseTimeEntity · public: incrementCount, decrementCount
- `MediaUrl` entity/MediaUrl.java · public: assignStyle, unassignStyle
- `Style` entity/Style.java · extends/implements BaseTimeEntity · public: incrementViewCount, updateContent, addLike, removeLike, removeInterest, addStyleOrderItem, addDataStyleOrderItem, addMediaUrl, addDataMediaUrl, addComment, addInterest, assignProfile, removeMediaUrl, removeStyleOrderItem, addStyleHashtag, removeStyleHashtag
- `StyleComment` entity/StyleComment.java · extends/implements BaseTimeEntity · public: assignStyle, assignProfile, addChildComment, setParentComment, addLike, updateContent
- `StyleCommentLike` entity/StyleCommentLike.java · extends/implements BaseTimeEntity · public: assignComment, assignProfile
- `StyleHashtag` entity/StyleHashtag.java · extends/implements BaseTimeEntity · public: assignStyle, assignHashtag
- `StyleInterest` entity/StyleInterest.java · extends/implements BaseTimeEntity · public: assignStyle, assignProfile
- `StyleLike` entity/StyleLike.java · extends/implements BaseTimeEntity · public: assignStyle, assignProfile
- `StyleOrderItem` entity/StyleOrderItem.java · public: assignStyle, unassignStyle, assignOrderItem
- `StyleViewLog` entity/StyleViewLog.java · public: create, addViewedAt

### event (3)

- `StyleViewEvent` dto/kafka/StyleViewEvent.java
- `StyleViewEventConsumer` service/kafka/StyleViewEventConsumer.java · public: listen, flushBuffer · 주입: StyleRepository, StyleViewLogRepository
- `StyleViewEventProducer` service/kafka/StyleViewEventProducer.java · public: sendViewEvent · 주입: KafkaTemplate

### config (1)

- `StyleViewEventKafkaConfig` service/kafka/config/StyleViewEventKafkaConfig.java · public: styleViewEventProducerFactory, styleViewEventKafkaTemplate, styleViewEventConsumerFactory, styleViewEventKafkaListenerContainerFactory

### dto (11)

- `AddCommentRequestDto` dto/AddCommentRequestDto.java
- `HashtagCreateRequestDto` dto/HashtagCreateRequestDto.java
- `HashtagResponseDto` dto/HashtagResponseDto.java
- `HashtagUpdateRequestDto` dto/HashtagUpdateRequestDto.java
- `ProfileStyleResponseDto` dto/ProfileStyleResponseDto.java
- `StyleCommentResponseDto` dto/StyleCommentResponseDto.java
- `StyleCommentsResponseDto` dto/StyleCommentsResponseDto.java
- `StyleDetailResponseDto` dto/StyleDetailResponseDto.java
- `StyleFilterRequestDto` dto/StyleFilterRequestDto.java
- `StyleResponseDto` dto/StyleResponseDto.java
- `UpdateCommentRequestDto` dto/UpdateCommentRequestDto.java

### exception (2)

- `StyleErrorCode` exception/StyleErrorCode.java
- `StyleException` exception/StyleException.java

### 다른 도메인 참조 (import·주입 기준)

- **order**: `OrderItem` ← StyleOrderItem, StyleOrderItemCommandService; `OrderItemQueryService` ← StyleOrderItemCommandService
- **user**: `Gender` ← StyleQueryController, StyleViewEvent, StyleViewEventProducer, StyleViewLog; `Profile` ← Style, StyleCommandService, StyleComment, StyleCommentCommandService, StyleCommentLike, StyleCommentLikeCommandService, StyleCommentLikeQueryService, StyleCommentLikeRepository, StyleCommentQueryService, StyleInterest, StyleInterestCommandService, StyleInterestQueryService, StyleInterestRepository, StyleLike, StyleLikeCommandService, StyleLikeQueryService, StyleLikeRepository, StyleQueryService; `ProfileQueryService` ← StyleCommandService, StyleCommentCommandService, StyleCommentLikeCommandService, StyleCommentLikeQueryService, StyleCommentQueryService, StyleInterestCommandService, StyleInterestQueryService, StyleLikeCommandService, StyleLikeQueryService, StyleQueryService

### 이 도메인을 참조하는 다른 도메인

- **global**: MediaUrl, MediaUrlRepository, Style, StyleOrderItem, StyleOrderItemRepository, StyleRepository, StyleViewLogBufferManager
- **product**: StyleRepository
- **user**: Style

## 4. JPA 엔티티 연관

- `Hashtag` @Entity @Table(indexes = { @Index(name = "idx_hashtag_name", columnList = "name", unique = true), @Index(name = "idx_hashtag_count", columnList = "count DESC") }) · extends BaseTimeEntity
- `MediaUrl` @Entity
  - style: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "style_id", nullable = false) → `Style`
- `Style` @Entity · extends BaseTimeEntity
  - profile: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "profile_id", nullable = false) → `Profile` [user]
  - styleOrderItems: @OneToMany(mappedBy = "style", cascade = CascadeType.ALL, orphanRemoval = true) → `StyleOrderItem`
  - mediaUrls: @OneToMany(mappedBy = "style", cascade = CascadeType.ALL, orphanRemoval = true) → `MediaUrl`
  - likes: @OneToMany(mappedBy = "style", cascade = CascadeType.ALL, orphanRemoval = true) → `StyleLike`
  - comments: @OneToMany(mappedBy = "style", cascade = CascadeType.ALL, orphanRemoval = true) → `StyleComment`
  - interests: @OneToMany(mappedBy = "style", cascade = CascadeType.ALL, orphanRemoval = true) → `StyleInterest`
  - hashtags: @OneToMany(mappedBy = "style", cascade = CascadeType.ALL, orphanRemoval = true) → `StyleHashtag`
- `StyleComment` @Entity · extends BaseTimeEntity
  - style: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "style_id", nullable = false) → `Style`
  - profile: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "profile_id", nullable = false) → `Profile` [user]
  - parentComment: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "parent_comment_id", nullable = true) → `StyleComment`
  - childComments: @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true) → `StyleComment`
  - likes: @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true) → `StyleCommentLike`
- `StyleCommentLike` @Entity · extends BaseTimeEntity
  - comment: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "comment_id", nullable = false) → `StyleComment`
  - profile: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "profile_id", nullable = false) → `Profile` [user]
- `StyleHashtag` @Entity @Table(indexes = { @Index(name = "idx_stylehashtag_style", columnList = "style_id"), @Index(name = "idx_stylehashtag_hashtag", columnList = "hashtag_id"), @Index(name = "idx_stylehashtag_unique", columnList = "style_id, hashtag_id", unique = true) }) · extends BaseTimeEntity
  - style: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "style_id", nullable = false) → `Style`
  - hashtag: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "hashtag_id", nullable = false) → `Hashtag`
- `StyleInterest` @Entity · extends BaseTimeEntity
  - style: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "style_id", nullable = false) → `Style`
  - profile: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "profile_id", nullable = false) → `Profile` [user]
- `StyleLike` @Entity · extends BaseTimeEntity
  - style: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "style_id", nullable = false) → `Style`
  - profile: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "profile_id", nullable = false) → `Profile` [user]
- `StyleOrderItem` @Entity
  - style: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "style_id") → `Style`
  - orderItem: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "order_item_id") → `OrderItem` [order]
- `StyleViewLog` @Entity
  - style: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "style_id") → `Style`

다른 도메인 엔티티에서 들어오는 연관:

- `Profile.styles` @OneToMany → `Style` [user]

## 5. 이벤트·메시징

- 발행 kafka `style-view-log-topic`: `StyleViewEventProducer#sendViewEvent` → `StyleViewEventConsumer#listen` [style]
- 구독 kafka `style-view-log-topic`: `StyleViewEventConsumer#listen` (groupId=style-view-group containerFactory=styleViewEventKafkaListenerContainerFactory) ← `StyleViewEventProducer#sendViewEvent` [style]

## 6. AOP

(없음)

