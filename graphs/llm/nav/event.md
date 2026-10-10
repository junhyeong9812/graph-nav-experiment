# event 도메인 네비게이션 맵

> 기준 경로: `src/main/java/com/fream/back/domain/event` (35 files). 모든 내용은 코드에서 확인된 사실만 기재.

## 1. 개요
- 브랜드(product 도메인 `Brand`)에 연결된 **프로모션 이벤트** CRUD + 썸네일/본문 이미지(SimpleImage) 파일 관리.
- 이벤트 상태(`UPCOMING/ACTIVE/ENDED`)는 **DB 컬럼이 아니라 startDate/endDate로 매번 계산**됨 (`Event.getStatus()`).
- Command/Query 컨트롤러·서비스 분리 패턴. 관리자 체크는 컨트롤러에서 `UserQueryService.checkAdminRole(email)`로 수행.
- `aop/` 패키지에 5개 Aspect + 5개 커스텀 어노테이션이 있으나 **어디에도 어노테이션이 붙어있지 않아 현재 동작하지 않음**(아래 7절).

## 2. 패키지/파일 구조
```
src/main/java/com/fream/back/domain/event/
├── aop/
│   ├── EventBusinessRuleAspect.java     (@Order(5))
│   ├── EventCachingAspect.java          (@Order(4))
│   ├── EventFileManagementAspect.java   (@Order(2))
│   ├── EventMonitoringAspect.java       (@Order(3))
│   ├── EventValidationAspect.java       (@Order(1))
│   └── annotation/ EventBusinessRule, EventCaching, EventFileManagement, EventMonitoring, EventValidation
├── controller/
│   ├── command/EventCommandController.java
│   └── query/EventQueryController.java
├── dto/ CreateEventRequest, UpdateEventRequest, EventDetailDto, EventListDto, EventSearchCondition
├── entity/ Event, EventStatus, SimpleImage
├── exception/ EventErrorCode, EventException, EventNotFoundException, ImageNotFoundException, InvalidEventDateException
├── repository/ EventRepository, EventRepositoryCustom, EventRepositoryImpl (QueryDSL), EventSearchRepository (미사용), SimpleImageRepository
├── scheduler/EventStatusScheduler.java
└── service/
    ├── command/ EventCommandService, SimpleImageCommandService
    └── query/   EventQueryService, SimpleImageQueryService
```
공용: `src/main/java/com/fream/back/global/utils/FileUtils.java` (파일 저장/삭제/URL).

## 3. 엔티티
### `Event` (extends `global.entity.BaseTimeEntity` → createdDate/modifiedDate)
| 필드 | 비고 |
|---|---|
| `id` Long | IDENTITY |
| `title` String | not null |
| `description` String | `@Lob`, not null |
| `startDate`, `endDate` LocalDateTime | not null |
| `thumbnailFileName` String | 파일명만 저장 (생성 시 `""`로 초기화 후 갱신) |
| `simpleImages` List<SimpleImage> | `@OneToMany(mappedBy="event", cascade=ALL, orphanRemoval=true)` |
| `brand` Brand | `@ManyToOne(LAZY)`, `brand_id` → `domain.product.entity.Brand` |
| `status` EventStatus | `@Transient` (Event.java:53). `getStatus()`(L101)가 날짜로 계산해 반환 |

- 메서드: `addSimpleImage`, `updateThumbnailFileName`, `updateEvent(title, description, start, end)`(null이면 유지), `isActive()`(start < now < end), `isUpcoming()`(start > now), `isEnded()`(end <= now), `getStatus()`, `getStatusDisplayName()`.
### `SimpleImage`
- `id`, `savedFileName`(not null), `event`(`@ManyToOne(LAZY)`, `event_id`). `assignEvent(Event)`.
### `EventStatus` enum
- `UPCOMING("예정")`, `ACTIVE("진행 중")`, `ENDED("종료")` — `getDisplayName()`.

## 4. API 엔드포인트 (`@RequestMapping("/events")`)
관리자 체크 방식: `SecurityUtils.extractEmailFromSecurityContext()` → `UserQueryService.checkAdminRole(email)` (User.role != `Role.ADMIN`이면 `UserException(INSUFFICIENT_PERMISSIONS)`). SecurityConfig는 whitelist 외 `anyRequest().authenticated()`이므로 GET 포함 모든 `/events/**` 요청은 인증 필요(`global/config/security/SecurityConfig.java:51-52`, whitelist에 events 없음).

| Method | Path | Controller#method | Service#method | 권한 |
|---|---|---|---|---|
| POST | `/events` (multipart: `@ModelAttribute CreateEventRequest` + `thumbnailFile` + `simpleImageFiles`) | `EventCommandController#createEvent` | `EventCommandService#createEvent` | ADMIN |
| PATCH | `/events/{eventId}` (+`thumbnailFile`, `simpleImageFiles`, `keepImageFileNames`) | `#updateEvent` | `EventCommandService#updateEvent` | ADMIN |
| DELETE | `/events/{eventId}` | `#deleteEvent` | `EventCommandService#deleteEvent` | ADMIN |
| PATCH | `/events/{eventId}/status?status=` | `#updateEventStatus` | `EventCommandService#updateEventStatus` | ADMIN |
| GET | `/events/page` (기본 size10, sort id DESC) | `EventQueryController#getAllEventsPaging` | `EventQueryService#findAllEventsWithPaging` | 인증 |
| GET | `/events/search?keyword&brandId&isActive&status&startDate&endDate` | `#searchEvents` | `EventQueryService#searchEvents` → `EventRepositoryImpl#searchEvents` | 인증 |
| GET | `/events/active` (sort startDate DESC) | `#getActiveEvents` | `#findActiveEventsWithPaging` | 인증 |
| GET | `/events/status/{status}` | `#getEventsByStatus` | `#findEventsByStatus` | 인증 |
| GET | `/events` | `#getAllEvents` | `#findAllEvents` | 인증 |
| GET | `/events/brands/{brandId}` | `#getEventsByBrand` | `#findEventsByBrandId` (@Cacheable) | 인증 |
| GET | `/events/{eventId}` | `#getEventDetail` | `#findEventById` (@Cacheable) | 인증 |
| GET | `/events/{eventId}/images/{fileName}` | `#getEventImage` | (서비스 없음) `FileUtils.existsFile` + `FileSystemResource` 직접 반환, 확장자로 Content-Type 결정(jpg/jpeg/png/gif, 그 외 octet-stream) | 인증 |

## 5. 서비스 계층
- **`EventCommandService`** (`@Transactional`)
  - `createEvent(req, thumbnail, simpleImages)`: `validateEventDates` → `BrandQueryService.findById` (실패 시 `product.exception.BrandNotFoundException`) → Event 저장 → 썸네일 `fileUtils.saveFile("event/{id}", "thumbnail_{id}_", file)` → `SimpleImageCommandService.createSimpleImages`. 기타 Exception은 `RuntimeException`으로 래핑.
  - `updateEvent(id, req, thumbnail, newImages, keepImageFileNames)`: 날짜 검증 → 필드 갱신 → `handleSimpleImagesUpdate`(keep 목록에 없는 기존 이미지 파일+엔티티 삭제, 새 이미지 추가) → `handleThumbnailUpdate`(기존 썸네일 파일 삭제 후 새로 저장).
  - `updateEventStatus(id, newStatus)`: 상태가 계산값이므로 **날짜를 덮어써서** 상태를 맞춤 — UPCOMING: start=now+1d, end=start+7d / ACTIVE: start=now-1d, end=now+7d / ENDED: start=now-14d, end=now-1d.
  - `deleteEvent(id)`: 썸네일·SimpleImage 파일 삭제 후 `eventRepository.delete`(cascade로 SimpleImage row 삭제).
  - `validateEventDates`: null 금지, start ≤ end, 기간 최소 1시간 → `InvalidEventDateException`.
- **`SimpleImageCommandService`**: `createSimpleImages(event, files)`(prefix `simple_{eventId}_{순번}_`, 빈 파일 skip), `deleteAllByEvent(eventId)`, `deleteSimpleImage(imageId)`.
- **`EventQueryService`** (`readOnly`): `findEventById`(`@Cacheable("eventDetails", key=#eventId)`), `findActiveEvents`(`@Cacheable("activeEvents")`, 컨트롤러 미사용), `findEventsByBrandId`(`@Cacheable("brandEvents", key=#brandId)`), `searchEvents`, `findEventsByStatus`, `findAllEventsWithPaging`, `findActiveEventsWithPaging`, `findAllEvents`. DTO 변환 시 URL은 `fileUtils.getFileUrl("event/{id}", fileName)`.
- **`SimpleImageQueryService`**: `findByEventId`, `findById`(`ImageNotFoundException`).

## 6. Repositories
- `EventRepository extends JpaRepository<Event,Long>, EventRepositoryCustom`
  - 파생 쿼리: `findByBrandIdOrderByStartDateDesc`, `findByStartDateBeforeAndEndDateAfter`(스케줄러), `findByEndDateLessThanEqual`(스케줄러), `findByStartDateAfterOrderByStartDateAsc`, `findByEndDateBeforeOrderByEndDateDesc`.
  - `@Query`: `findActiveEvents(now)`, `findAllWithBrand`(JOIN FETCH), `findAllWithBrandPaging`, `findByIdWithBrand`, `findActiveEventsWithBrandPaging`, `findByBrandIdWithBrand`, `findByBrandIdWithBrandPaging`, `findByIdWithBrandAndSimpleImages`.
- `EventRepositoryImpl#searchEvents` (QueryDSL, `JPAQueryFactory`): brand fetchJoin, 조건 = title containsIgnoreCase / brand.id eq / isActive(날짜 비교) / startDate goe / endDate loe / status(날짜 조건으로 변환). 정렬 없으면 id DESC. `PageableExecutionUtils.getPage`.
- `EventSearchRepository`: 동일 기능의 구버전 QueryDSL 클래스 — **어디서도 참조되지 않음**(grep 확인).
- `SimpleImageRepository`: `@Query findByEventId`, `@Query("DELETE ...") deleteByEventId` — **`@Modifying` 없음**(SimpleImageRepository.java:12-13).

## 7. 외부 연동 / 비동기 / 파일
### 파일 저장 (`global/utils/FileUtils.java`)
- `BASE_DIR = "/home/ubuntu/fream"` (하드코딩, Windows 경로는 주석). 이벤트 디렉토리: `/home/ubuntu/fream/event/{eventId}/`.
- `saveFile(dir, prefix, file)`: Content-Type이 `image/*` 또는 `video/*`가 아니면 `UnsupportedFileTypeException`; 파일명 = `prefix + UUID + 원본확장자`; 반환값(파일명)만 DB 저장.
- `deleteFile`, `existsFile`, `deleteDirectory`, `isFileExist`, `saveMediaFile`(=saveFile).
- `getFileUrl(dir, name)` → `"/api/files/{dir}/{name}"`. **이 코드베이스에 `/files/**` 범용 컨트롤러는 없음**(grep 확인) → 실제 이미지 서빙은 `GET /events/{eventId}/images/{fileName}`.
- `EventQueryController`에도 `BASE_DIR`이 별도로 하드코딩됨(EventQueryController.java:35) — 경로 변경 시 FileUtils와 함께 수정.
- 이벤트 description은 HTML img src 재작성 로직 없음(FAQ/Notice/Inspection과 다름).

### 스케줄러 `EventStatusScheduler`
- `@Scheduled(cron = "0 0 * * * *")`(매시 정각) + `@Transactional` `checkEventStatus()`. 클래스에 `@EnableScheduling`.
- `findByStartDateBeforeAndEndDateAfter(now,now)` / `findByEndDateLessThanEqual(now)` 조회 후 **로그만 남김**(상태가 계산값이라 DB 변경 없음, 알림 없음).

### 캐싱
- `global/config/CacheConfig.java`: `ConcurrentMapCacheManager`에 `setCacheNames(faq*, inspection*)`만 등록. **`eventDetails`, `activeEvents`, `brandEvents`는 목록에 없음** → 고정 캐시 이름 모드에서는 미등록 캐시 사용 시 Spring이 "Cannot find cache named" 예외를 던지므로 `GET /events/{id}`, `/events/brands/{id}` 동작에 주의.
- event 측에 `@CacheEvict` 없음 → 수정/삭제 후에도 캐시 무효화 안 됨.

### AOP (`domain/event/aop`) — 전부 `@Around("@annotation(x)")` 기반, **현재 어노테이션 사용처 0건**
| Aspect | Order | 포인트컷 | 하는 일(코드 기준) | 주입 |
|---|---|---|---|---|
| `EventValidationAspect` | 1 | `@annotation(eventValidation)` | `@EventValidation.validations()`별 검증: DATE_RANGE(min 시간/max 일수/과거일), BUSINESS_RULE, BRAND_VALIDATION, FILE_VALIDATION(확장자/총용량), STATUS_VALIDATION, PERMISSION_CHECK(admin), DUPLICATE_CHECK, CAPACITY_CHECK; 실패 시 `failureAction` | EventRepository, BrandQueryService |
| `EventFileManagementAspect` | 2 | `@annotation(fileManagement)` | MultipartFile 검증(확장자/크기/개수), 리사이즈, WebP 변환·바이러스 스캔·백업(스텁), 실패 시 롤백, 메트릭 | FileUtils |
| `EventMonitoringAspect` | 3 | `@annotation(eventMonitoring)` | 실행시간/메모리/CPU, 사용자 활동, 라이프사이클, 브랜드 통계, 임계치 알림. `sendStatusChangeNotification`은 로그만 출력(L539) | — (인메모리 Map, ScheduledExecutor) |
| `EventCachingAspect` | 4 | `@annotation(eventCaching)` | `CacheManager` 기반 키 전략별 캐싱, TTL, 웜업, 무효화(`invalidateCacheOnStatusChange`, `invalidateBrandCache`, `clearAllCaches`) | CacheManager |
| `EventBusinessRuleAspect` | 5 | `@annotation(businessRule)` | 이벤트 중복 기간, 브랜드당 활성 이벤트 수(기본 5), 생성 쿨다운, 수정/삭제 가능 시점, 아카이빙(인메모리 리스트) 등 | EventRepository, BrandQueryService |
- 어노테이션 정의: `aop/annotation/*.java` (`@Target(METHOD, TYPE)`, RUNTIME). 적용하려면 서비스 메서드에 어노테이션을 붙여야 하며, 붙이는 순간 위 로직이 활성화됨.

## 8. 다른 도메인 의존
- `domain.product`: `entity.Brand`(ManyToOne), `service.brand.BrandQueryService#findById`, `exception.BrandNotFoundException`.
- `domain.user`: `service.query.UserQueryService#checkAdminRole` (컨트롤러).
- `global`: `BaseTimeEntity`, `FileUtils`, `SecurityUtils`, `GlobalException`/`ErrorCode`, `CacheConfig`.
- 역방향: 다른 도메인에서 event 참조는 `global/exception/GlobalExceptionHandler.java`(`@ExceptionHandler(EventException.class)`, L390)뿐.

## 9. 예외
- `EventException extends GlobalException`; `EventErrorCode implements ErrorCode`, prefix **`EVENT-`**:
  EVENT-001 EVENT_NOT_FOUND(404), 002 INVALID_EVENT_DATE(400), 003 INVALID_EVENT_DATA(400), 004 EVENT_TITLE_DUPLICATE(409), 005 EVENT_MODIFICATION_DENIED(403), 006 EVENT_DELETION_DENIED(403), 007 IMAGE_NOT_FOUND(404), 008 IMAGE_UPLOAD_FAILED(500), 009 INVALID_IMAGE_FORMAT(400), 010 BRAND_NOT_FOUND(404).
- 하위: `EventNotFoundException`, `ImageNotFoundException`, `InvalidEventDateException`.
- 서비스 내부 catch-all이 많아 `RuntimeException("...중 오류 발생")`으로 래핑되는 경로 존재(createEvent, 이미지 처리, deleteEvent).

## 10. 작업 팁 / 주의점
- 상태 필터링을 추가할 때는 컬럼이 아니라 **날짜 조건**으로 작성(`EventRepositoryImpl#statusEquals` 참고).
- `PATCH /events/{id}`에서 `keepImageFileNames`를 안 보내면 **기존 SimpleImage 전부 삭제**됨(null → 빈 리스트 처리).
- `UpdateEventRequest`의 title/description/startDate/endDate는 `@NotBlank/@NotNull` → 부분 수정 불가(전 필드 필요). `UpdateEventRequest.keepImageFileNames` 필드와 `@RequestParam keepImageFileNames`가 중복 존재; 서비스는 RequestParam 값을 사용.
- `PATCH /{id}/status`는 이벤트 날짜를 **덮어씀**(원래 기간 소실).
- `SimpleImageRepository.deleteByEventId`는 `@Modifying` 누락 → 호출 시 실패 가능(현재 `SimpleImageCommandService#deleteAllByEvent`에서만 사용, 컨트롤러 경로에선 미호출).
- 파일 디렉토리 규칙 `event/{eventId}` 문자열이 서비스/컨트롤러 여러 곳에 하드코딩.
- Event 캐시 미등록/미무효화 문제(7절) 참고.
- `EventSearchRepository`는 dead code; 검색 수정은 `EventRepositoryImpl`에서.
