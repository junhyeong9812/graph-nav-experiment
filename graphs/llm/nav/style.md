# style 도메인 네비게이션

## 1. 개요
- KREAM "스타일" 피드: 사용자(Profile)가 구매한 주문상품(OrderItem)을 태그해 사진/영상 + 텍스트 + 해시태그 게시물(Style)을 작성.
- 부가 기능: 댓글/대댓글(1단계), 댓글 좋아요, 스타일 좋아요, 관심(북마크), 해시태그(자동완성 포함), 조회수 & 조회 로그(Kafka + 버퍼 + 스케줄러).
- CQRS 분리: `controller/command|query`, `service/command|query`. 미디어 파일은 로컬 디스크(`/home/ubuntu/fream/styles/{styleId}/`)에 저장하고 API로 서빙.
- 알림(notification) 도메인은 호출하지 않음(LIKE/COMMENT 알림 생성 코드 없음).

## 2. 패키지/파일 구조 (67 files)
```
src/main/java/com/fream/back/domain/style/
├── controller/command/
│   ├── StyleCommandController.java            # /styles/commands
│   ├── StyleCommentCommandController.java     # /styles/comments/commands
│   ├── StyleCommentLikeCommandController.java # /styles/comments/likes/commands
│   ├── StyleLikeCommandController.java        # /styles/likes/commands
│   ├── StyleInterestCommandController.java    # /styles/interests/commands
│   └── HashtagCommandController.java          # /hashtags/commands
├── controller/query/
│   ├── StyleQueryController.java              # /styles/queries (목록/상세/프로필별/미디어 파일)
│   ├── StyleHashtagQueryController.java       # /styles/queries/{styleId}/hashtags
│   ├── StyleCommentQueryController.java       # /styles/comments/queries
│   └── HashtagQueryController.java            # /hashtags/queries
├── dto/  AddCommentRequestDto, UpdateCommentRequestDto, StyleFilterRequestDto, StyleResponseDto,
│         StyleDetailResponseDto(+ProductInfoDto), ProfileStyleResponseDto, StyleCommentResponseDto,
│         StyleCommentsResponseDto, HashtagCreateRequestDto, HashtagUpdateRequestDto, HashtagResponseDto
├── dto/kafka/StyleViewEvent.java
├── entity/  Style, MediaUrl, StyleOrderItem, StyleLike, StyleInterest, StyleComment, StyleCommentLike,
│            Hashtag, StyleHashtag, StyleViewLog
├── exception/StyleErrorCode.java, StyleException.java
├── repository/  StyleRepository(+StyleRepositoryCustom, StyleRepositoryCustomImpl[QueryDSL]),
│                MediaUrlRepository, StyleOrderItemRepository, StyleLikeRepository, StyleInterestRepository,
│                StyleCommentRepository, StyleCommentLikeRepository, HashtagRepository, StyleHashtagRepository,
│                StyleViewLogRepository
├── service/command/  StyleCommandService, MediaUrlCommandService, StyleOrderItemCommandService,
│                     StyleCommentCommandService, StyleCommentLikeCommandService, StyleLikeCommandService,
│                     StyleInterestCommandService, HashtagCommandService, StyleHashtagCommandService
├── service/query/    StyleQueryService, MediaUrlQueryService, StyleCommentQueryService,
│                     StyleCommentLikeQueryService, StyleLikeQueryService, StyleInterestQueryService,
│                     HashtagQueryService, StyleHashtagQueryService
├── service/kafka/StyleViewEventProducer.java, StyleViewEventConsumer.java
├── service/kafka/config/StyleViewEventKafkaConfig.java
└── service/scheduler/StyleViewLogBufferManager.java
```

## 3. 엔티티 (`src/main/java/com/fream/back/domain/style/entity/`)
| 엔티티 | 주요 필드 / 관계 | 비고 |
|---|---|---|
| `Style` (BaseTimeEntity) | `profile`: ManyToOne LAZY → **user.Profile** (`profile_id` NOT NULL); `content`; `viewCount`(Long); OneToMany(cascade ALL, orphanRemoval): `styleOrderItems`, `mediaUrls`, `likes`, `comments`, `interests`, `hashtags`(StyleHashtag) | `incrementViewCount()`, `assignProfile()`는 `Profile.addStyle()` 양방향 동기화. `addDataXxx()`는 DataInitializer용(역방향 미설정) |
| `MediaUrl` | `url`(String, NOT NULL), `style` ManyToOne (`style_id` NOT NULL) | BaseTimeEntity 아님. url 형식 `/styles/queries/{styleId}/media/{fileName}` |
| `StyleOrderItem` | `style` ManyToOne, `orderItem` ManyToOne → **order.OrderItem** (`order_item_id`) | 스타일-구매상품 연결. BaseTimeEntity 아님 |
| `StyleLike` (BaseTimeEntity) | `style`, `profile` | 유니크 제약 없음(토글 로직으로만 보장) |
| `StyleInterest` (BaseTimeEntity) | `style`, `profile` | 동일 |
| `StyleComment` (BaseTimeEntity) | `style`, `profile`, `content`, `parentComment`(self ManyToOne, nullable), `childComments`(OneToMany cascade ALL), `likes`(StyleCommentLike, cascade ALL) | `updateContent()` 빈 값이면 IllegalArgumentException |
| `StyleCommentLike` (BaseTimeEntity) | `comment`(`comment_id`), `profile` | |
| `Hashtag` (BaseTimeEntity) | `name`(unique, '#' 제외), `count`(Long, 기본 0) | 인덱스 `idx_hashtag_name`(unique), `idx_hashtag_count`. `incrementCount/decrementCount`(0 미만 방지) |
| `StyleHashtag` (BaseTimeEntity) | `style`, `hashtag` | unique index `(style_id, hashtag_id)` |
| `StyleViewLog` | `viewedAt`, `userEmail`("anonymous" 가능), `userAge`, `userGender`(**user.Gender** enum, STRING), `style` ManyToOne | `static create(style, email, age, gender)` |

- 역방향: `src/main/java/com/fream/back/domain/user/entity/Profile.java` L52 `List<Style> styles`, `addStyle/addDataStyle/removeStyle`.
- 이 도메인 자체 enum 없음(`Gender`는 user 도메인).

## 4. API 엔드포인트
인증: `SecurityConfig`(`src/main/java/com/fream/back/global/config/security/SecurityConfig.java`) whitelist에 `/styles/**`, `/hashtags/**` 없음 → **전부 `anyRequest().authenticated()`** 대상. 컨트롤러 내부 이메일 추출은 `global/utils/SecurityUtils`(`extractEmailFromSecurityContext()`는 미인증 시 `SecurityUserNotFoundException` throw, `extractEmailOrAnonymous()`는 "anonymous").

### Style
| Method | Path | Controller#method | Service#method | 비고 |
|---|---|---|---|---|
| POST (multipart) | `/styles/commands` params: `orderItemIds`(필수 List), `content`(필수), `mediaFiles`(opt), `hashtags`(opt) | `StyleCommandController#createStyle` | `StyleCommandService#createStyle(email, orderItemIds, content, mediaFiles, hashtags)` | 응답 `Long` styleId |
| POST | `/styles/commands/{styleId}/view` | `StyleCommandController#incrementViewCount` | `StyleCommandService#incrementViewCount` | 레거시(“향후 제거” 로그) |
| PUT (multipart) | `/styles/commands/{styleId}` params: `content`, `newMediaFiles`, `existingUrlsFromFrontend`, `hashtags` (모두 opt) | `StyleCommandController#updateStyle` | `StyleCommandService#updateStyle` | **작성자 검증 없음** |
| DELETE | `/styles/commands/{styleId}` | `StyleCommandController#deleteStyle` | `StyleCommandService#deleteStyle` | **작성자 검증 없음** |
| GET | `/styles/queries` (`@ModelAttribute StyleFilterRequestDto` + Pageable) | `StyleQueryController#getFilteredStyles` | `StyleQueryService#getFilteredStyles(filter, pageable, email)` | 피드 |
| GET | `/styles/queries/{styleId}` | `StyleQueryController#getStyleDetail` | `StyleQueryService#getStyleDetail(styleId, email)` + `StyleViewEventProducer#sendViewEvent` | 조회 이벤트 발행 |
| GET | `/styles/queries/profile/{profileId}` | `StyleQueryController#getStylesByProfile` | `StyleQueryService#getStylesByProfile` | |
| GET | `/styles/queries/{styleId}/media/{fileName}` | `StyleQueryController#getStyleMedia` | (서비스 없음, 직접 파일 읽기) | `byte[]`, Content-Type probe |
| GET | `/styles/queries/{styleId}/hashtags` | `StyleHashtagQueryController#getStyleHashtags` | `StyleHashtagQueryService#getHashtagsByStyleId` | |

### 좋아요 / 관심
| Method | Path | Controller#method | Service#method |
|---|---|---|---|
| POST | `/styles/likes/commands/{styleId}/toggle` | `StyleLikeCommandController#toggleLike` | `StyleLikeCommandService#toggleLike(email, styleId)` |
| POST | `/styles/interests/commands/{styleId}/toggle` | `StyleInterestCommandController#toggleInterest` | `StyleInterestCommandService#toggleStyleInterest(email, styleId)` |
| POST | `/styles/comments/likes/commands/{commentId}/toggle` | `StyleCommentLikeCommandController#toggleCommentLike` | `StyleCommentLikeCommandService#toggleCommentLike(email, commentId)` |

### 댓글
| Method | Path | Controller#method | Service#method | 비고 |
|---|---|---|---|---|
| POST | `/styles/comments/commands` body `AddCommentRequestDto{styleId, content(≤1000), parentCommentId?}` | `StyleCommentCommandController#addComment` | `StyleCommentCommandService#addComment(email, styleId, content, parentCommentId)` | 응답이 **엔티티 `StyleComment`** 그대로 |
| PUT | `/styles/comments/commands/{commentId}` body `UpdateCommentRequestDto{updatedContent}` | `StyleCommentCommandController#updateComment` | `StyleCommentCommandService#updateComment(commentId, content)` | email 추출만, **작성자 검증 없음** |
| DELETE | `/styles/comments/commands/{commentId}` | `StyleCommentCommandController#deleteComment` | `StyleCommentCommandService#deleteComment(commentId)` | **작성자 검증 없음**, 대댓글 cascade 삭제 |
| GET | `/styles/comments/queries/{styleId}?page=0&size=10` | `StyleCommentQueryController#getCommentsByStyleId` | `StyleCommentQueryService#getCommentsByStyleId(styleId, email, page, size)` | 루트댓글 페이징 + replies |

### 해시태그
| Method | Path | Controller#method | Service#method | 비고 |
|---|---|---|---|---|
| POST | `/hashtags/commands` body `{name}` | `HashtagCommandController#createHashtag` | `HashtagCommandService#create` | 관리자 체크 주석 처리(`// SecurityUtils.checkAdminRole();`) |
| PUT | `/hashtags/commands/{id}` | `HashtagCommandController#updateHashtag` | `HashtagCommandService#update` | 동일 |
| DELETE | `/hashtags/commands/{id}` | `HashtagCommandController#deleteHashtag` | `HashtagCommandService#delete` | 사용 중이면 ST203 |
| GET | `/hashtags/queries/{id}` | `HashtagQueryController#getHashtagById` | `HashtagQueryService#findById` | |
| GET | `/hashtags/queries/name/{name}` | `#getHashtagByName` | `#findByName` | |
| GET | `/hashtags/queries` (Pageable) | `#getAllHashtags` | `#findAll` | |
| GET | `/hashtags/queries/popular` | `#getPopularHashtags` | `#findPopular` (count DESC) | |
| GET | `/hashtags/queries/search?keyword=` | `#searchHashtags` | `#search` (Containing, count DESC) | |
| GET | `/hashtags/queries/autocomplete?keyword=&limit=10&isKorean=` | `#autocompleteHashtags` | `isKorean` 있으면 `#autocomplete(keyword, limit, isKorean)`, 없으면 `#smartAutocomplete` | 초성 입력이면 `chosungAutocomplete`(전체 `findAll()` 후 메모리 필터) |

## 5. 서비스 계층
### Command
- `StyleCommandService` (`@Transactional`): 의존 `StyleRepository`, `ProfileQueryService`(user), `StyleOrderItemCommandService`, `MediaUrlCommandService`, `HashtagRepository`, `HashtagCommandService`, `StyleHashtagCommandService`.
  - `createStyle` L48, private `processHashtags` L149, `updateStyle` L199, `deleteStyle` L299, `incrementViewCount` L350.
- `MediaUrlCommandService`: `saveMediaFile(style, file)` L54 → `validateMediaFile`(이미지 jpeg/jpg/png/gif/bmp/webp, 비디오 mp4/mpeg/quicktime/x-msvideo/webm, **최대 10MB**) → `FileUtils.saveFile("styles/{styleId}", "media_", file)` → `MediaUrl(url="/styles/queries/{id}/media/{savedName}")` 저장. `deleteMediaUrl(mediaUrl)` L106 → `FileUtils.deleteFile` + `style.removeMediaUrl` + repo delete.
- `StyleOrderItemCommandService#createStyleOrderItem(orderItemId, style)` → `OrderItemQueryService#findById`(order 도메인) → `StyleOrderItem` 저장. (주문자 본인 여부 검증 없음)
- `StyleHashtagCommandService`: `addHashtagToStyle(style, hashtag)`(기존 링크 있으면 재사용, 없으면 `hashtag.incrementCount()`), `removeAllHashtagsFromStyle(styleId)`(count 감소 후 `deleteByStyleId`), `removeHashtagFromStyle(styleId, hashtagId)`.
- `HashtagCommandService`: `create`(trim, 중복 시 ST202), `update`(builder로 새 객체 생성 후 save — id 유지), `delete`(`countByHashtagId > 0` 이면 ST203).
- `StyleLikeCommandService#toggleLike`, `StyleInterestCommandService#toggleStyleInterest`, `StyleCommentLikeCommandService#toggleCommentLike`: `ProfileQueryService#getProfileByEmail` → 대상 조회(`StyleQueryService#findStyleById` / `StyleCommentQueryService#findById`) → `findByXxxAndProfile` 존재하면 delete, 없으면 save.
- `StyleCommentCommandService`: `addComment` L36 (부모 댓글이 이미 대댓글이면 ST303 "대댓글에는 댓글을 달 수 없습니다", 부모가 다른 style이면 ST303), `updateComment` L131, `deleteComment` L173.

### Query (`@Transactional(readOnly = true)`)
- `StyleQueryService`: `findStyleById` L45, `findStylesByProfileId` L68, `getFilteredStyles` L89, `getStylesByProfile` L170, `getStyleDetail` L242. 의존 `StyleLikeQueryService`, `StyleInterestQueryService`, `StyleHashtagQueryService`, `ProfileQueryService`.
- `StyleCommentQueryService`: `getCommentsByStyleId` L41 (루트댓글 페이지 → 루트별 `findRepliesByParentCommentId` 루프(N+1) → 좋아요 ID 셋 → DTO, `userProfileImageUrl` 포함), `findCommentById`, `checkUserLikedComment`, `findByStyleId`, `findById`, `findByProfileId`.
- `StyleLikeQueryService`: `checkUserLiked(email, styleId)`, `getLikedStyleIds(email, styleIds)`, `isLikedByProfile(styleId, profileId)`, `findLikesByStyleId`.
- `StyleInterestQueryService`: `isStyleInterestedByProfile`, `findByStyleId`, `getInterestedStyleIds(profileId, styleIds)`, `checkUserInterested(email, styleId)`.
- `StyleCommentLikeQueryService`: `isCommentLikedByProfile`, `findByCommentId`, `getLikedCommentIds(email, commentIds)`.
- `StyleHashtagQueryService`: `getHashtagsByStyleId`, `getHashtagsByStyleIds`, `getHashtagMapByStyleIds(styleIds)` → `Map<styleId, List<HashtagResponseDto>>`.
- `HashtagQueryService`: `findById`, `findByName`, `findAll`, `findPopular`, `search`, `autocomplete`(L173), `smartAutocomplete`(L292), private `englishAutocomplete`/`koreanAutocomplete`/`chosungAutocomplete`.
- `MediaUrlQueryService`: `findMediaUrlsByStyleId`, `findMediaUrlById`, `findMediaUrlsByUrl` (코드베이스 전체에서 호출처 없음 — grep 기준).

## 스타일 작성 흐름
1. `POST /styles/commands` → `StyleCommandController#createStyle` (`SecurityUtils.extractEmailFromSecurityContext()`).
2. `StyleCommandService#createStyle` (L48):
   1. email/content 검증(ST003).
   2. `ProfileQueryService#getProfileByEmail(email)` (user 도메인).
   3. `Style.builder().profile(..).content(..).viewCount(0L)` → `assignProfile` → `styleRepository.save`.
   4. orderItemIds 루프: `StyleOrderItemCommandService#createStyleOrderItem` → `OrderItemQueryService#findById` → save; `style.addStyleOrderItem`. **개별 실패는 로깅 후 계속**.
   5. mediaFiles 루프: `MediaUrlCommandService#saveMediaFile` → `FileUtils#saveFile` (`/home/ubuntu/fream/styles/{styleId}/media_{UUID}{ext}`). **개별 실패는 로깅 후 계속**.
   6. hashtags: `processHashtags` (L149) — `#` 제거 → `HashtagRepository#findByName` 있으면 사용, 없으면 `HashtagCommandService#create` → `StyleHashtagCommandService#addHashtagToStyle` (count++). **개별 실패는 로깅 후 계속**.
3. 응답: 생성된 styleId.
- 수정(`updateStyle` L199): content 변경 → `existingUrls`가 non-null이면 목록에 없는 MediaUrl 삭제(파일 포함) → 새 파일 저장 → `hashtagNames` non-null이면 `removeAllHashtagsFromStyle` 후 재등록.
- 삭제(`deleteStyle` L299): 해시태그 링크 제거(count--) → 미디어 파일/URL 삭제 → `styleRepository.delete` (likes/comments/interests/orderItems는 cascade).

## 피드 조회 흐름
1. `GET /styles/queries` → `StyleQueryController#getFilteredStyles` (L110) → `StyleQueryService#getFilteredStyles` (L89).
2. `StyleRepositoryCustomImpl#filterStyles` (L148, QueryDSL):
   - 1단계: style ⟕ profile ⟕ styleOrderItems ⟕ orderItem ⟕ productSize ⟕ productColor ⟕ product ⟕ brand, 필터 `brandName`(brand.name eq), `collectionName`(product.collection.name eq), `categoryId`(product.category.id eq), `profileName`(eq) → **distinct style id 전체 fetch**.
   - 2단계: `style.id in (ids)` 로 `StyleResponseDto` 생성자 projection(대표 미디어 = min(mediaUrl.id)의 url, viewCount, `likes.size()`), offset/limit.
   - 정렬: `sortBy == "popular"` → viewCount DESC, id DESC; 그 외 id DESC. total = 1단계 id 개수.
3. 서비스 후처리: `StyleHashtagQueryService#getHashtagMapByStyleIds` → 로그인 시 `StyleLikeQueryService#getLikedStyleIds`, `StyleInterestQueryService#getInterestedStyleIds` 로 `liked/interested` 세팅(실패 시 해시태그만 세팅).
- 상세: `GET /styles/queries/{styleId}` → `StyleQueryService#getStyleDetail` → `StyleRepositoryCustomImpl#getStyleDetail` (L237: mediaUrls(id ASC), productInfos(product/color별 group by, `min(salePrice)`), 본문 projection(likeCount, commentCount)) → hashtags, liked/interested 세팅 → 컨트롤러가 `SecurityUtils.extractUserInfo()`(age/gender) 읽고 `StyleViewEventProducer#sendViewEvent` 발행.
- 프로필별: `StyleRepositoryCustomImpl#getStylesByProfile` (L309) → `ProfileStyleResponseDto(id, 대표 mediaUrl, likeCount)` + 해시태그/liked.

## 6. Repositories
- `StyleRepository` (`JpaRepository<Style,Long>` + `StyleRepositoryCustom`): `findByProfileId`.
- `StyleRepositoryCustomImpl` (QueryDSL): `filterStyles` L148, `getStyleDetail` L237, `getStylesByProfile` L309, `styleCountByColorIds(colorIds)` L351 (productColorId → distinct style 수; product 도메인에서 사용). L35~ 에 주석 처리된 이전 버전 `filterStyles` 존재.
- `@Query` (JPQL) 보유:
  - `StyleCommentRepository`: `findRootCommentsByStyleId`(parent null, createdDate DESC, Page), `countCommentsByStyleId`, `findRepliesByParentCommentId`(createdDate DESC); 파생 `findByStyleId`, `findByProfileId`.
  - `StyleLikeRepository` / `StyleInterestRepository`: `existsByStyleIdAndProfileId`, `findByProfileIdAndStyleId`/`findByStyleIdAndProfileId`, `findByProfileIdAndStyleIdIn`, `findStyleIdsByProfileIdAndStyleIdIn`(Set), `countByStyleId`, `@Modifying deleteByProfileIdAndStyleId`; 파생 `findByStyleAndProfile`, `findByStyleId`.
  - `StyleCommentLikeRepository`: `findByCommentIdAndProfileId`, `existsByCommentIdAndProfileId`, `findByCommentId`, `findByProfileIdAndCommentIdIn`, `findCommentIdsByProfileIdAndCommentIdIn`, `countByCommentId`, `@Modifying deleteByProfileIdAndCommentId`; 파생 `findByCommentAndProfile`.
  - `HashtagRepository`: `findByNameContainingKeywordOrderByCountDesc`(@Query LIKE prefix OR contains); 파생 `findByName`, `findByNameStartingWithOrderByCountDesc`, `findAllByOrderByCountDesc`, `findByNameContainingOrderByCountDesc`, `existsByName`.
  - `StyleHashtagRepository`: `findStyleIdsByHashtagId`(@Query, Page<Long>); 파생 `findByStyleId`, `findByStyleIdIn`, `deleteByStyleId`, `findByStyleIdAndHashtagId`, `countByHashtagId`.
- `MediaUrlRepository`: `findByStyleId`, `findByUrl`. `StyleOrderItemRepository`, `StyleViewLogRepository`: 기본 CRUD만.

## 7. 외부 연동 / 비동기
### Kafka (조회 이벤트)
| 항목 | 값 |
|---|---|
| Topic | `style-view-log-topic` |
| Producer | `service/kafka/StyleViewEventProducer#sendViewEvent(styleId, email, age, gender)` — `KafkaTemplate<String, StyleViewEvent> styleViewEventKafkaTemplate`, key 없음, 예외는 로깅만 |
| Consumer | `service/kafka/StyleViewEventConsumer#listen` — `@KafkaListener(topics="style-view-log-topic", groupId="style-view-group", containerFactory="styleViewEventKafkaListenerContainerFactory")`, `@Transactional` |
| Config | `service/kafka/config/StyleViewEventKafkaConfig` — `${spring.kafka.bootstrap-servers}`, StringSerializer/JsonSerializer, `JsonDeserializer<>(StyleViewEvent.class)` |
| Payload | `StyleViewEvent{styleId, email, age, gender, viewedAt}` |
- Consumer 처리: `style.incrementViewCount()` + save → `StyleViewLog.create(...)` + `addViewedAt(event.viewedAt)` → in-memory `ConcurrentLinkedQueue` 버퍼에 추가 → 크기 ≥ `FLUSH_SIZE(30)` 이면 `flushBuffer()` (`styleViewLogRepository.saveAll`). 예외는 로깅만.
### 스케줄러
- `service/scheduler/StyleViewLogBufferManager#flushBufferPeriodically` — `@Scheduled(fixedDelay = 10000)` (10초). (`@EnableScheduling`은 다른 도메인 설정 클래스에 선언됨: event/accessLog/weather)
- `flushBufferOnShutdown()` — `src/main/java/com/fream/back/global/config/ApplicationShutdownConfig.java`의 `ContextClosedEvent` 리스너에서 호출.
### 파일 저장
- 저장: `src/main/java/com/fream/back/global/utils/FileUtils.java` `BASE_DIR="/home/ubuntu/fream"` (L27) → `/home/ubuntu/fream/styles/{styleId}/media_{UUID}{ext}`.
- 서빙: `StyleQueryController#getStyleMedia` — `baseDir` 하드코딩 `/home/ubuntu/fream` + `styles/{styleId}/{fileName}` (FileUtils 미사용).
- DB의 `MediaUrl.url` = `/styles/queries/{styleId}/media/{fileName}` (API 상대경로).

## 8. 다른 도메인과의 의존
- style → user: `Profile`(작성자/좋아요/관심/댓글 주체), `ProfileQueryService#getProfileByEmail`(10개 클래스에서 주입), `Gender`, `QProfile`.
- style → order: `OrderItem`(StyleOrderItem), `OrderItemQueryService#findById`, `QOrderItem`.
- style → product: `StyleRepositoryCustomImpl`이 `QProduct, QProductColor, QProductSize, QProductImage, QBrand` 사용(필터/상세 상품정보).
- style → global: `SecurityUtils`, `FileUtils`, `JwtAuthenticationFilter.UserInfo`, `GlobalException`.
- 다른 도메인 → style:
  - `src/main/java/com/fream/back/domain/product/service/product/ProductQueryService.java` L101: `styleRepository.styleCountByColorIds(colorIds)`
  - `src/main/java/com/fream/back/domain/product/elasticsearch/service/ProductColorSearchService.java` L103: 동일
  - `src/main/java/com/fream/back/domain/product/repository/ProductQueryDslRepository.java` L546~: `QStyle`, `QStyleOrderItem` 직접 사용(색상별 스타일 수)
  - `src/main/java/com/fream/back/domain/user/entity/Profile.java`: `styles` 컬렉션
  - `src/main/java/com/fream/back/global/config/DataInitializer.java`: Style/MediaUrl/StyleOrderItem 시드 데이터
  - `src/main/java/com/fream/back/global/config/ApplicationShutdownConfig.java`: 버퍼 flush
  - `src/main/java/com/fream/back/global/exception/GlobalExceptionHandler.java`: `StyleException` 처리

## 9. 예외
- `StyleException extends GlobalException`; `StyleErrorCode` 접두사 **`ST`**:
  | 코드 | enum | HTTP |
  |---|---|---|
  | ST001 | STYLE_NOT_FOUND | 404 |
  | ST002 | STYLE_ACCESS_DENIED | 403 (컨트롤러의 로그인 필요 체크에 사용) |
  | ST003 | STYLE_INVALID_REQUEST | 400 (대부분의 예상치 못한 예외 래핑에도 사용) |
  | ST101 | MEDIA_FILE_UPLOAD_FAILED | 500 |
  | ST102 | MEDIA_FILE_NOT_FOUND | 404 |
  | ST103 | MEDIA_FILE_INVALID | 400 |
  | ST201 | HASHTAG_NOT_FOUND | 404 |
  | ST202 | HASHTAG_ALREADY_EXISTS | 409 (빈 이름 생성 시에도 사용) |
  | ST203 | HASHTAG_IN_USE | 409 |
  | ST301 | COMMENT_NOT_FOUND | 404 |
  | ST302 | COMMENT_ACCESS_DENIED | 403 (사용처 없음) |
  | ST303 | COMMENT_CONTENT_INVALID | 400 |
  | ST401 | LIKE_OPERATION_FAILED | 500 |
  | ST402 | INTEREST_OPERATION_FAILED | 500 |
  | ST501 | STYLE_VIEW_EVENT_FAILED | 500 |

## 10. 작업 팁 / 주의점
- **권한 검증 부재**: `updateStyle`/`deleteStyle`(StyleCommandService L199/L299), `updateComment`/`deleteComment`(StyleCommentCommandService L131/L173)는 email을 받지 않으며 작성자 확인 없음. 해시태그 CUD의 관리자 체크는 주석 처리.
- **부분 실패 허용**: createStyle/updateStyle에서 주문아이템·미디어·해시태그 개별 오류는 catch 후 계속 → 일부 누락된 채 성공 응답 가능.
- `StyleOrderItemCommandService`는 orderItem이 요청 사용자의 주문인지 검증하지 않음.
- viewCount 증가 경로 2개: Kafka consumer(`StyleViewEventConsumer#listen`)와 레거시 `POST /styles/commands/{id}/view`. 상세 조회(`GET /styles/queries/{id}`)마다 이벤트 발행(중복 조회 방지 없음).
- 조회 로그는 메모리 버퍼 → 프로세스 비정상 종료 시 유실 가능, 다중 인스턴스 시 인스턴스별 버퍼.
- `filterStyles`: 필터된 전체 id를 먼저 메모리로 가져온 뒤 `IN` 쿼리 → 데이터 많으면 비용 큼. `StyleFilterRequestDto.isMainCategory`는 쿼리에서 사용되지 않음. `sortBy` 허용값 `popular|latest`(@Pattern이지만 컨트롤러에 `@Valid` 없음).
- `getStyleDetail` / 피드: 로그인 사용자 판정은 `email != null && !"anonymousUser"` — 상세 조회 시 "anonymous"는 `getProfileByEmail` 실패 후 catch로 처리됨. 단, `/styles/**`는 Spring Security상 인증 필수라 비로그인 요청은 필터에서 차단됨.
- `getFilteredStyles`/`getStylesByProfile` 컨트롤러는 `extractEmailFromSecurityContext()` 사용(미인증 시 예외).
- `addComment` 응답이 JPA 엔티티(`StyleComment`) 직렬화 — LAZY 연관(Profile/Style 등) 직렬화 이슈 주의.
- 댓글 조회는 루트 댓글마다 replies 쿼리(N+1). 대댓글은 1단계만 허용.
- Like/Interest/StyleCommentLike 테이블에 (style/comment, profile) unique 제약 없음 → 동시 토글 시 중복 가능.
- `Hashtag.count`는 `StyleHashtagCommandService`에서만 증감; `HashtagCommandService#update`는 builder로 새 인스턴스 생성 후 save(merge).
- 미디어 서빙 경로 하드코딩(`StyleQueryController` getStyleMedia, Windows 경로 주석 존재). 파일명 path traversal 검증 없음.
- 파일 업로드 상한: 서비스 10MB(MediaUrlCommandService L44), multipart는 1GB(application.yml).
