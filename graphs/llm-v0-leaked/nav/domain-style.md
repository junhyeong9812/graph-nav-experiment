# domain `style` 네비게이션

## 1. 역할 요약
- SNS형 "스타일" 게시물 도메인: 구매한 주문 아이템(`OrderItem`)을 연결한 착용 사진/영상 게시물 작성·수정·삭제, 댓글/대댓글(2단계), 좋아요(게시물/댓글), 관심(북마크), 해시태그(자동완성 포함), 미디어 파일 로컬 저장/서빙.
- 조회수: 상세 조회(`GET /styles/queries/{styleId}`) 시 Kafka 이벤트를 발행하고, consumer가 `Style.viewCount` 증가 + `StyleViewLog` 버퍼링 일괄 저장. 10초 주기 스케줄러로 버퍼 flush.
- CQRS식 분리: `controller/command` + `service/command` (쓰기), `controller/query` + `service/query` (읽기, `@Transactional(readOnly = true)`).
- 루트 경로 prefix: `src/main/java/com/fream/back/domain/style/` (이하 `style/`로 표기)

## 2. 패키지/파일 맵

### controller
| 파일 | 용도 |
|---|---|
| `style/controller/command/StyleCommandController.java` | 스타일 생성/수정/삭제, 레거시 뷰카운트 API, Nginx 캐시 퍼지 호출 |
| `style/controller/command/StyleCommentCommandController.java` | 댓글 생성/수정/삭제 |
| `style/controller/command/StyleLikeCommandController.java` | 스타일 좋아요 토글 |
| `style/controller/command/StyleInterestCommandController.java` | 스타일 관심 토글 |
| `style/controller/command/StyleCommentLikeCommandController.java` | 댓글 좋아요 토글 |
| `style/controller/command/HashtagCommandController.java` | 해시태그 생성/수정/삭제 |
| `style/controller/query/StyleQueryController.java` | 스타일 상세(+Kafka 뷰 이벤트 발행), 필터 목록, 프로필별 목록, 미디어 파일 바이트 서빙 |
| `style/controller/query/StyleHashtagQueryController.java` | 특정 스타일의 해시태그 목록 |
| `style/controller/query/StyleCommentQueryController.java` | 스타일 댓글 목록(루트 페이징 + 대댓글) |
| `style/controller/query/HashtagQueryController.java` | 해시태그 단건/목록/인기/검색/자동완성 |

### service
| 파일 | 용도 |
|---|---|
| `style/service/command/StyleCommandService.java` | 스타일 CRUD 오케스트레이션(주문아이템 연결, 미디어, 해시태그 처리 `processHashtags`) |
| `style/service/command/MediaUrlCommandService.java` | 미디어 파일 검증·저장(`FileUtils`)·삭제, `MediaUrl` 엔티티 관리 |
| `style/service/command/StyleOrderItemCommandService.java` | `StyleOrderItem` 생성 (`OrderItemQueryService.findById`) |
| `style/service/command/HashtagCommandService.java` | 해시태그 create/update/delete |
| `style/service/command/StyleHashtagCommandService.java` | 스타일-해시태그 연결 추가/제거 + `Hashtag.count` 증감 |
| `style/service/command/StyleCommentCommandService.java` | 댓글 add/update/delete |
| `style/service/command/StyleLikeCommandService.java` | 좋아요 토글 |
| `style/service/command/StyleInterestCommandService.java` | 관심 토글 |
| `style/service/command/StyleCommentLikeCommandService.java` | 댓글 좋아요 토글 |
| `style/service/query/StyleQueryService.java` | `findStyleById`, 필터 목록/프로필 목록/상세 + liked/interested/hashtags 주입 |
| `style/service/query/StyleCommentQueryService.java` | 댓글 트리 조회, `findById`, `findByStyleId` 등 |
| `style/service/query/StyleLikeQueryService.java` | 좋아요 여부/ID Set 조회 |
| `style/service/query/StyleInterestQueryService.java` | 관심 여부/ID Set 조회 |
| `style/service/query/StyleCommentLikeQueryService.java` | 댓글 좋아요 여부/ID Set 조회 |
| `style/service/query/StyleHashtagQueryService.java` | 스타일별 해시태그 목록/Map 조회 |
| `style/service/query/HashtagQueryService.java` | 해시태그 조회·검색·자동완성(영문/한글/초성) |
| `style/service/query/MediaUrlQueryService.java` | MediaUrl 조회 헬퍼 (컨트롤러에서 사용 안 함) |
| `style/service/kafka/StyleViewEventProducer.java` | `style-view-log-topic`으로 `StyleViewEvent` 전송 |
| `style/service/kafka/StyleViewEventConsumer.java` | `@KafkaListener` – viewCount 증가 + 로그 버퍼링/flush |
| `style/service/kafka/config/StyleViewEventKafkaConfig.java` | 전용 ProducerFactory/KafkaTemplate/ConsumerFactory/ListenerContainerFactory |
| `style/service/scheduler/StyleViewLogBufferManager.java` | `@Scheduled(fixedDelay = 10000)` 버퍼 flush, 종료 시 flush |

### entity / repository / dto / exception
| 파일 | 용도 |
|---|---|
| `style/entity/Style.java` | 게시물 (profile, content, viewCount, 자식 컬렉션들) |
| `style/entity/StyleComment.java` | 댓글 (self-reference parent/child) |
| `style/entity/StyleLike.java`, `StyleInterest.java`, `StyleCommentLike.java` | 좋아요/관심/댓글좋아요 (Profile 연결) |
| `style/entity/Hashtag.java`, `StyleHashtag.java` | 해시태그 마스터 / 스타일 연결 테이블 |
| `style/entity/MediaUrl.java` | 미디어 URL |
| `style/entity/StyleOrderItem.java` | 스타일-주문아이템 연결 |
| `style/entity/StyleViewLog.java` | 조회 로그 (email/age/gender/viewedAt) |
| `style/repository/StyleRepository.java` | `JpaRepository<Style,Long>, StyleRepositoryCustom` + `findByProfileId` |
| `style/repository/StyleRepositoryCustom.java` / `StyleRepositoryCustomImpl.java` | QueryDSL: `filterStyles`, `getStyleDetail`, `getStylesByProfile`, `styleCountByColorIds` |
| `style/repository/StyleCommentRepository.java`, `StyleLikeRepository.java`, `StyleInterestRepository.java`, `StyleCommentLikeRepository.java`, `HashtagRepository.java`, `StyleHashtagRepository.java`, `MediaUrlRepository.java`, `StyleOrderItemRepository.java`, `StyleViewLogRepository.java` | Spring Data JPA (+JPQL `@Query`) |
| `style/dto/*.java` | 요청/응답 DTO (`StyleResponseDto`, `StyleDetailResponseDto`(+내부 `ProductInfoDto`), `ProfileStyleResponseDto`, `StyleFilterRequestDto`, `StyleComment(s)ResponseDto`, `AddCommentRequestDto`, `UpdateCommentRequestDto`, `Hashtag*Dto`) |
| `style/dto/kafka/StyleViewEvent.java` | Kafka 메시지 (styleId, email, age, gender, viewedAt) |
| `style/exception/StyleErrorCode.java`, `StyleException.java` | 에러코드 enum(ST*), `GlobalException` 하위 예외 |

## 3. REST 엔드포인트

| Method | Path | Controller 메서드 | Service 메서드 |
|---|---|---|---|
| POST | `/styles/commands` (multipart: `orderItemIds`, `content`, `mediaFiles?`, `hashtags?`) | `StyleCommandController.createStyle` | `StyleCommandService.createStyle` → 응답 body = styleId(Long) |
| POST | `/styles/commands/{styleId}/view` (레거시) | `StyleCommandController.incrementViewCount` | `StyleCommandService.incrementViewCount` |
| PUT | `/styles/commands/{styleId}` (`content?`, `newMediaFiles?`, `existingUrlsFromFrontend?`, `hashtags?`) | `StyleCommandController.updateStyle` | `StyleCommandService.updateStyle` |
| DELETE | `/styles/commands/{styleId}` | `StyleCommandController.deleteStyle` | `StyleCommandService.deleteStyle` |
| POST | `/styles/comments/commands` (JSON `AddCommentRequestDto`) | `StyleCommentCommandController.addComment` | `StyleCommentCommandService.addComment` |
| PUT | `/styles/comments/commands/{commentId}` (JSON `updatedContent`) | `StyleCommentCommandController.updateComment` | `StyleCommentCommandService.updateComment` |
| DELETE | `/styles/comments/commands/{commentId}` | `StyleCommentCommandController.deleteComment` | `StyleCommentCommandService.deleteComment` |
| POST | `/styles/likes/commands/{styleId}/toggle` | `StyleLikeCommandController.toggleLike` | `StyleLikeCommandService.toggleLike` |
| POST | `/styles/interests/commands/{styleId}/toggle` | `StyleInterestCommandController.toggleInterest` | `StyleInterestCommandService.toggleStyleInterest` |
| POST | `/styles/comments/likes/commands/{commentId}/toggle` | `StyleCommentLikeCommandController.toggleCommentLike` | `StyleCommentLikeCommandService.toggleCommentLike` |
| POST | `/hashtags/commands` | `HashtagCommandController.createHashtag` | `HashtagCommandService.create` |
| PUT | `/hashtags/commands/{id}` | `HashtagCommandController.updateHashtag` | `HashtagCommandService.update` |
| DELETE | `/hashtags/commands/{id}` | `HashtagCommandController.deleteHashtag` | `HashtagCommandService.delete` |
| GET | `/styles/queries/{styleId}` | `StyleQueryController.getStyleDetail` | `StyleQueryService.getStyleDetail` + `StyleViewEventProducer.sendViewEvent` |
| GET | `/styles/queries` (`@ModelAttribute StyleFilterRequestDto`, Pageable) | `StyleQueryController.getFilteredStyles` | `StyleQueryService.getFilteredStyles` |
| GET | `/styles/queries/profile/{profileId}` | `StyleQueryController.getStylesByProfile` | `StyleQueryService.getStylesByProfile` |
| GET | `/styles/queries/{styleId}/media/{fileName}` | `StyleQueryController.getStyleMedia` | (서비스 없음, 컨트롤러에서 `/home/ubuntu/fream/styles/{styleId}/{fileName}` 직접 읽음) |
| GET | `/styles/queries/{styleId}/hashtags` | `StyleHashtagQueryController.getStyleHashtags` | `StyleHashtagQueryService.getHashtagsByStyleId` |
| GET | `/styles/comments/queries/{styleId}?page=0&size=10` | `StyleCommentQueryController.getCommentsByStyleId` | `StyleCommentQueryService.getCommentsByStyleId` |
| GET | `/hashtags/queries/{id}` | `HashtagQueryController.getHashtagById` | `HashtagQueryService.findById` |
| GET | `/hashtags/queries/name/{name}` | `HashtagQueryController.getHashtagByName` | `HashtagQueryService.findByName` |
| GET | `/hashtags/queries` | `HashtagQueryController.getAllHashtags` | `HashtagQueryService.findAll` |
| GET | `/hashtags/queries/popular` | `HashtagQueryController.getPopularHashtags` | `HashtagQueryService.findPopular` |
| GET | `/hashtags/queries/search?keyword=` | `HashtagQueryController.searchHashtags` | `HashtagQueryService.search` |
| GET | `/hashtags/queries/autocomplete?keyword=&limit=10&isKorean=` | `HashtagQueryController.autocompleteHashtags` | `isKorean` 있으면 `autocomplete`, 없으면 `smartAutocomplete` |

- 인증: 쓰기 컨트롤러들은 `SecurityUtils.extractEmailFromSecurityContext()`로 email 추출 후 null/빈값/`"anonymousUser"`이면 `STYLE_ACCESS_DENIED`. 단, `updateStyle`/`deleteStyle`/`incrementViewCount` 및 해시태그 command 3종은 email 검사 없음(관리자 체크 `SecurityUtils.checkAdminRole()`은 주석 처리).
- 상세 조회는 `SecurityUtils.extractEmailOrAnonymous()`, 로그인 시 `SecurityUtils.extractUserInfo()`(`JwtAuthenticationFilter.UserInfo`)에서 age/gender 추출(기본 0/`Gender.OTHER`).
- `/styles/commands` 생성/수정/삭제 성공 후 `NginxCachePurgeUtil.purgeStyleCache()` 호출 (PURGE `{nginxUrl}/purge_styles/api/styles/queries`), 실패 시 warn만.

## 4. 핵심 엔티티와 관계

| 엔티티 | 주요 필드/관계 | 비고 |
|---|---|---|
| `Style` (BaseTimeEntity) | `@ManyToOne(LAZY) profile`(nullable=false), `content`, `viewCount`(Long, 기본값 없음) ; `@OneToMany(mappedBy="style", cascade=ALL, orphanRemoval=true)`: `styleOrderItems`, `mediaUrls`, `likes`, `comments`, `interests`, `hashtags`(StyleHashtag) | `StyleViewLog`은 Style 쪽 컬렉션 없음 |
| `StyleComment` (BaseTimeEntity) | `style`, `profile`, `content`, `parentComment`(self, nullable), `childComments`(cascade ALL), `likes`(StyleCommentLike, cascade ALL) | `updateContent`가 blank면 `IllegalArgumentException` |
| `StyleLike` / `StyleInterest` (BaseTimeEntity) | `style`, `profile` | DB unique 제약 없음 |
| `StyleCommentLike` (BaseTimeEntity) | `comment`, `profile` | |
| `Hashtag` (BaseTimeEntity) | `name`(unique, `#` 제외), `count`(기본 0) | 인덱스 `idx_hashtag_name`(unique), `idx_hashtag_count`; `decrementCount`는 0 미만 방지 |
| `StyleHashtag` (BaseTimeEntity) | `style`, `hashtag` | unique 인덱스 `(style_id, hashtag_id)` |
| `MediaUrl` | `url`, `style`(nullable=false) | BaseTimeEntity 미상속 |
| `StyleOrderItem` | `style`, `orderItem`(→ `domain.order.entity.OrderItem`) | BaseTimeEntity 미상속 |
| `StyleViewLog` | `viewedAt`, `userEmail`, `userAge`, `userGender`(`domain.user.entity.Gender`, STRING), `style` | |

- 역방향: `domain/user/entity/Profile.java`에 `@OneToMany(mappedBy="profile", cascade=ALL, orphanRemoval=true) List<Style> styles` 및 `addStyle/addDataStyle/removeStyle`. `Style.assignProfile`이 양방향 동기화.
- Product 연결은 직접 FK 없음: `StyleOrderItem → OrderItem → productSize → productColor → product (→ brand/collection/category)` 조인 경로를 QueryDSL에서 사용.

## 5. 주요 서비스 메서드와 비즈니스 규칙

### StyleCommandService
- `createStyle(email, orderItemIds, content, mediaFiles, hashtagNames)`: email/content 필수 → `ProfileQueryService.getProfileByEmail` → `Style(viewCount=0L)` 저장 → 주문아이템별 `StyleOrderItemCommandService.createStyleOrderItem`, 미디어별 `MediaUrlCommandService.saveMediaFile`, 해시태그 `processHashtags`. **개별 주문아이템/미디어/해시태그 실패는 로그만 남기고 계속 진행** (부분 성공).
- `processHashtags`: 앞의 `#` 1개 제거, 빈 문자열 skip, `findByName`으로 재사용 또는 `HashtagCommandService.create` 후 재조회, `StyleHashtagCommandService.addHashtagToStyle` (중복 연결이면 기존 반환, 신규면 `count++`). 대소문자 변환 없음.
- `updateStyle`: content가 null 아니고 다르면 변경; `existingUrls != null`이면 목록에 없는 MediaUrl 삭제(파일 포함); 새 파일 추가; `hashtagNames != null`이면 전체 해시태그 제거(`removeAllHashtagsFromStyle`, count 감소) 후 재등록. null 리스트는 "변경 없음".
- `deleteStyle`: 해시태그 연결 제거(실패 무시) → 미디어 파일/엔티티 삭제(개별 실패 무시) → `styleRepository.delete` (나머지는 cascade).
- `incrementViewCount`: 레거시 API용, `STYLE_NOT_FOUND` 가능.

### MediaUrlCommandService
- 검증(`validateMediaFile`): 비어있지 않음, ≤ 10MB, content-type ∈ 이미지(jpeg/jpg/png/gif/bmp/webp) 또는 비디오(mp4/mpeg/quicktime/x-msvideo/webm), 원본 파일명 존재. 위반 시 `MEDIA_FILE_INVALID`.
- 저장: `fileUtils.saveFile("styles/{styleId}", "media_", file)` (BASE_DIR `/home/ubuntu/fream`, `global/utils/FileUtils.java`) → URL `/styles/queries/{styleId}/media/{savedFileName}` 저장. 실패 시 `MEDIA_FILE_UPLOAD_FAILED`.
- 삭제: URL 마지막 `/` 뒤를 파일명으로 `fileUtils.deleteFile` (없으면 warn) → `style.removeMediaUrl` + repository delete.

### 댓글/좋아요/관심
- `StyleCommentCommandService.addComment`: 대댓글은 1단계만 허용(부모가 이미 대댓글이면 `COMMENT_CONTENT_INVALID`), 부모 댓글은 같은 스타일이어야 함, 부모 없으면 `COMMENT_NOT_FOUND`.
- `updateComment`/`deleteComment`: **작성자 소유권 검사 없음** (컨트롤러 주석은 "서비스에서 수행"이라 하지만 실제 코드에 없음). 삭제 시 자식 댓글은 cascade 삭제.
- 토글 3종(`toggleLike`, `toggleStyleInterest`, `toggleCommentLike`): `findByXAndProfile` 존재 시 delete, 없으면 생성. 예상외 오류는 `LIKE_OPERATION_FAILED`/`INTEREST_OPERATION_FAILED`.

### 조회
- `StyleRepositoryCustomImpl.filterStyles`: 1단계로 brand.name / product.collection.name / product.category.id / profile.profileName 조건으로 distinct style ID 전체 조회 → 2단계에서 `StyleResponseDto` 생성자 프로젝션(첫 MediaUrl = min(id), `likes.size()`), `sortBy="popular"`이면 `viewCount desc, id desc`, 그 외 `id desc`. total = 1단계 ID 개수. `StyleFilterRequestDto.isMainCategory`는 사용되지 않음.
- `getStyleDetail`: mediaUrls(id asc), productInfos(`groupBy(product.id, productColor.id)`, `min(salePrice)`), likeCount/commentCount. `interestCount`, `modifiedDate`는 설정되지 않음. 없으면 null → 서비스에서 `STYLE_NOT_FOUND`.
- `styleCountByColorIds(colorIds)`: colorId별 distinct style 수 — product 도메인에서 사용.
- `StyleQueryService.getFilteredStyles/getStylesByProfile/getStyleDetail`: 해시태그 Map 일괄 주입, 로그인 사용자면 liked/interested 주입(실패해도 무시). 프로필 목록은 liked만.
- `StyleCommentQueryService.getCommentsByStyleId`: 루트 댓글 페이징(최신순) + 루트마다 `findRepliesByParentCommentId` 개별 쿼리(N+1), likeCount는 `getLikes().size()`(lazy 로딩), 응답에 현재 사용자 `userProfileImageUrl` 포함. page<0 → 0, size∉(0,100] → 10.
- `HashtagQueryService` 자동완성: 키워드가 전부 초성(ㄱ~ㅎ)이면 `chosungAutocomplete`(**`findAll()` 전체 로딩 후 메모리 필터**), isKorean이면 `findByNameContainingKeywordOrderByCountDesc`(대소문자 무시 LIKE), 아니면 `findByNameStartingWithOrderByCountDesc`(prefix, 대소문자 구분).
- `HashtagCommandService`: `create`는 trim 후 중복이면 `HASHTAG_ALREADY_EXISTS`; `update`는 같은 id로 새 인스턴스 빌드 후 save(merge); `delete`는 연결된 StyleHashtag가 있으면 `HASHTAG_IN_USE`.

## 6. Kafka 조회 이벤트 파이프라인 & 스케줄러

| 단계 | 위치 | 내용 |
|---|---|---|
| 발행 | `StyleQueryController.getStyleDetail` → `StyleViewEventProducer.sendViewEvent` | 상세 조회 응답 직전 발행. 실패 시 로그만 |
| 토픽 | `style-view-log-topic` (상수, producer/consumer 각각 하드코딩) | key 없음, value JSON(`JsonSerializer`) |
| 설정 | `StyleViewEventKafkaConfig` | `${spring.kafka.bootstrap-servers}` 사용, 빈: `styleViewEventKafkaTemplate`, `styleViewEventKafkaListenerContainerFactory` |
| 소비 | `StyleViewEventConsumer.listen` (groupId `style-view-group`, `@Transactional`) | Style 조회 → `incrementViewCount()` + save → `StyleViewLog.create` 후 `viewedAt`을 이벤트 시각으로 덮어씀 → 메모리 `ConcurrentLinkedQueue` 버퍼에 추가 → 30건 이상이면 `flushBuffer()` |
| 저장 | `flushBuffer()` → `StyleViewLogRepository.saveAll` | |
| 스케줄러 | `StyleViewLogBufferManager.flushBufferPeriodically` `@Scheduled(fixedDelay = 10000)` (cron 아님, 10초) | 버퍼 flush |
| 종료 | `global/config/ApplicationShutdownConfig.java` (`ContextClosedEvent`) → `flushBufferOnShutdown()` | |

- consumer 예외는 모두 catch 후 로그만(재시도/DLT 없음).
- `@EnableScheduling`은 style 도메인 안에 없음 (다른 곳 `domain/event/scheduler/EventStatusScheduler.java`, `domain/accessLog/config/AccessLogAopConfiguration.java`에 선언).

## 7. 다른 도메인/전역 의존

| 대상 | 사용처 |
|---|---|
| `domain.user.entity.Profile` / `QProfile` | 모든 사용자 연관 엔티티, QueryDSL |
| `domain.user.service.profile.ProfileQueryService.getProfileByEmail` | 커맨드/쿼리 서비스 다수 |
| `domain.user.entity.Gender` | Kafka 이벤트, StyleViewLog |
| `domain.order.entity.OrderItem`, `order.service.query.OrderItemQueryService.findById` | `StyleOrderItem`, `StyleOrderItemCommandService` |
| `domain.product.entity.*` (QProduct, QProductColor, QProductSize, QProductImage, QBrand) | `StyleRepositoryCustomImpl` 조인 |
| `global.utils.FileUtils`, `NginxCachePurgeUtil`, `SecurityUtils`, `global.config.security.JwtAuthenticationFilter` | 파일/캐시퍼지/인증 |
| `global.exception.GlobalException`, `ErrorCode`, `GlobalExceptionHandler.handleStyleException` | 예외 처리 |

역방향 (style을 사용하는 외부 코드):
- `domain/product/service/product/ProductQueryService.java`, `domain/product/elasticsearch/service/ProductColorSearchService.java` → `StyleRepository.styleCountByColorIds`
- `domain/product/repository/ProductQueryDslRepository.java` → `QStyle`, `QStyleOrderItem`
- `domain/user/entity/Profile.java` → `Style` 컬렉션
- `global/config/DataInitializer.java` → Style/MediaUrl/StyleOrderItem 및 리포지토리 (초기 데이터)
- `global/config/ApplicationShutdownConfig.java` → `StyleViewLogBufferManager`

- notification 도메인 연동: style 패키지 내 참조 없음 (grep 결과 0건).
- Redis / `@Cacheable`: style 패키지 내 사용 없음. 캐시는 Nginx 캐시 퍼지만 사용.

## 8. 예외/에러코드
- `style/exception/StyleErrorCode.java` (implements `global.exception.ErrorCode`): ST001 STYLE_NOT_FOUND(404), ST002 STYLE_ACCESS_DENIED(403), ST003 STYLE_INVALID_REQUEST(400), ST101 MEDIA_FILE_UPLOAD_FAILED(500), ST102 MEDIA_FILE_NOT_FOUND(404), ST103 MEDIA_FILE_INVALID(400), ST201 HASHTAG_NOT_FOUND(404), ST202 HASHTAG_ALREADY_EXISTS(409), ST203 HASHTAG_IN_USE(409), ST301 COMMENT_NOT_FOUND(404), ST302 COMMENT_ACCESS_DENIED(403, 미사용), ST303 COMMENT_CONTENT_INVALID(400), ST401 LIKE_OPERATION_FAILED(500), ST402 INTEREST_OPERATION_FAILED(500), ST501 STYLE_VIEW_EVENT_FAILED(500).
- `style/exception/StyleException.java` → `global/exception/GlobalExceptionHandler.java`의 `@ExceptionHandler(StyleException.class)`.
- 패턴: 서비스/컨트롤러가 `catch (StyleException) { throw }` / `catch (Exception) { wrap STYLE_INVALID_REQUEST 등 }` 반복.

## 9. 관련 테스트 (`src/test/java/com/fream/back/domain/style/`)
| 파일 | 종류 | 커버 |
|---|---|---|
| `repository/StyleRepositoryCustomImplTest.java` | `@DataJpaTest` + `@Import(QueryDslConfig.class)` | filterStyles(정렬/필터/페이징 total), getStyleDetail, getStylesByProfile, styleCountByColorIds |
| `repository/StyleRelationRepositoryTest.java` | `@DataJpaTest` | 좋아요 존재/카운트/ID Set, 해시태그 검색 대소문자 규칙, StyleHashtag 카운트 |
| `service/StyleCommandServiceTest.java` | Mockito | create 부분 실패 허용, 해시태그 `#` 제거/재사용, update/delete, viewCount null → STYLE_INVALID_REQUEST 래핑 |
| `service/HashtagCommandServiceTest.java` | Mockito | create(trim, 대소문자 유지, blank → ALREADY_EXISTS 코드), update 새 인스턴스, delete IN_USE, StyleHashtag 연결/카운트 |
| `service/StyleCommentCommandServiceTest.java` | Mockito | 댓글/대댓글 규칙, 수정, 삭제 |
| `service/StyleLikeAndInterestCommandServiceTest.java` | Mockito | 좋아요/관심 토글, 예외 매핑 |
- Kafka consumer/producer, 스케줄러, 컨트롤러, 쿼리 서비스 테스트는 없음.

## 10. 자주 할 작업 → 여기를 보라
| 작업 | 위치 |
|---|---|
| 피드 필터 조건/정렬 추가 | `StyleFilterRequestDto` + `StyleRepositoryCustomImpl.filterStyles` (1단계 filterBuilder / 2단계 orderBy) + `StyleResponseDto` 생성자(프로젝션 인자 순서 일치) |
| 상세 응답 필드 추가 | `StyleDetailResponseDto` + `StyleRepositoryCustomImpl.getStyleDetail` (`Projections.fields` alias 일치) + `StyleQueryService.getStyleDetail` |
| 업로드 허용 형식/크기 변경 | `MediaUrlCommandService` 상수 `ALLOWED_*_TYPES`, `MAX_FILE_SIZE` (+ `FileUtils.saveFile` 자체 MIME 체크) |
| 미디어 저장 경로 변경 | `FileUtils.BASE_DIR`, `MediaUrlCommandService.STYLE_FOLDER_PREFIX/API_URL_PREFIX`, `StyleQueryController.getStyleMedia`의 하드코딩 `baseDir` — 3곳 동시 |
| 해시태그 정규화 규칙 | `StyleCommandService.processHashtags`, `HashtagCommandController`(`#` 제거), `HashtagCommandService.create/update` |
| 댓글 권한 검사 추가 | `StyleCommentCommandService.updateComment/deleteComment` (`COMMENT_ACCESS_DENIED` 이미 정의됨) |
| 스타일 수정/삭제 권한 | `StyleCommandController.updateStyle/deleteStyle` 및 서비스 (현재 소유자 검사 없음) |
| 조회수/조회 로그 로직 | `StyleViewEventConsumer`, `StyleViewLogBufferManager`, `StyleViewEventKafkaConfig` |
| 상품 화면의 스타일 수 | `StyleRepositoryCustomImpl.styleCountByColorIds` (product 도메인이 호출) |
| 에러코드 추가 | `StyleErrorCode` |

## 11. Gotchas (코드에서 확인됨)
- 리포지토리 구조: `StyleRepository` 인터페이스가 실제로 존재하며 `JpaRepository<Style, Long>, StyleRepositoryCustom`을 함께 상속. 구현은 `StyleRepositoryCustomImpl`(Spring Data `Impl` 접미사 규칙, `JPAQueryFactory` 주입). `StyleRepositoryCustomImpl`에 구버전 `filterStyles`가 대량 주석으로 남아 있음.
- `Style.viewCount`는 `@Builder.Default` 없음 → `createStyle` 외 경로(예: `DataInitializer`, 테스트 빌더)로 생성되면 null일 수 있고 `incrementViewCount()`가 NPE. Consumer에서는 예외가 삼켜져 조회수가 조용히 증가하지 않음.
- Consumer 버퍼는 인스턴스 메모리 큐 → 프로세스 비정상 종료 시 최대 30건/10초분 로그 유실 가능. `listen`의 `@Transactional`과 `flushBuffer` 내부 호출은 같은 트랜잭션 내 실행(자기 호출).
- `GET /styles/queries/{styleId}/media/{fileName}`: `fileName`을 그대로 경로에 연결(경로 정규화/검증 없음). baseDir가 컨트롤러에 하드코딩.
- `StyleCommentCommandController.addComment`는 `StyleComment` **엔티티를 그대로 응답 body로 반환** (LAZY 연관/순환 직렬화 주의).
- `StyleCommandController.createStyle`은 `orderItemIds.size()`를 null 검사 전에 로그에서 호출. `@RequestParam("orderItemIds")`는 required이므로 실무상 null은 바인딩 단계에서 걸림.
- `HashtagCommandService.create`는 blank 이름일 때 `HASHTAG_ALREADY_EXISTS` 코드를 던짐(테스트가 이 동작을 고정). 주석의 "소문자 변환"은 실제로 하지 않음.
- `HashtagQueryService.extractChosung`은 `(c - '가') / 588 + 'ㄱ'`로 계산 — 호환 자모(U+3131~)는 초성 순서와 연속이 아니므로(ㄱ,ㄲ,ㄳ,ㄴ...) ㄱ/ㄲ 이외 초성은 잘못 매핑됨(예: '나' → 'ㄳ'). 초성 검색 결과가 부정확할 수 있음.
- `MediaUrlCommandService.saveMediaFile`은 `style.addMediaUrl`을 호출하지 않음 → 같은 트랜잭션에서 `style.getMediaUrls()`에 신규 미디어 미반영.
- `StyleOrderItem`은 같은 `OrderItem`을 여러 스타일에 연결 가능(검증 없음, 주석으로 명시). 주문 소유자 검증도 없음.
- `StyleLike`/`StyleInterest`/`StyleCommentLike`에 DB unique 제약이 없어 동시 토글 시 중복 가능.
- 레거시 `POST /styles/commands/{styleId}/view`와 Kafka 경로가 둘 다 viewCount를 증가시킴.
- `StyleFilterRequestDto.isMainCategory`, `StyleDetailResponseDto.interestCount/modifiedDate`, `StyleResponseDto.createdDate/modifiedDate`는 쿼리에서 채워지지 않음.
- `filterStyles`는 필터 결과 ID 전체를 메모리로 가져와 `IN` 절로 재조회 → 데이터가 많으면 비용 큼.
