# 고객센터/콘텐츠 도메인 네비게이션 맵 (faq · inquiry · inspection · notice)

> 기준 경로: `src/main/java/com/fream/back/domain/{faq,inquiry,inspection,notice}`. 코드에서 확인된 사실만 기재.

## 0. 공통 패턴 (4개 도메인 공유)
- **Command/Query 분리**: `controller/command/*CommandController` + `controller/query/*QueryController`(같은 `@RequestMapping` prefix), `service/command/*` + `service/query/*`.
- **관리자 체크**: 컨트롤러에서 SecurityContext의 principal(String email) 추출 → `UserQueryService.checkAdminRole(email)` (`domain/user/service/query/UserQueryService.java:25`, `Role.ADMIN` 아니면 `UserException(INSUFFICIENT_PERMISSIONS)`). FAQ/Inspection/Notice는 컨트롤러 내부 private `extractEmailFromSecurityContext()`를 따로 구현, Inquiry는 `global/utils/SecurityUtils.extractEmailFromSecurityContext()` 사용.
- **Security**: `global/config/security/SecurityConfig.java:51-52` whitelist에 이 도메인 경로가 없으므로 **GET 포함 전부 인증 필요**(`anyRequest().authenticated()`).
- **HTML 본문 + 이미지 업로드 패턴**: multipart `@ModelAttribute` DTO로 HTML 본문 + `files`/`newFiles` 수신 → `global/utils/FileUtils.saveFile(directory, prefix, file)`(저장 루트 `/home/ubuntu/fream`, 파일명 `prefix+UUID+확장자`, `image/*`·`video/*`만 허용) → 이미지 엔티티(`*Image`) 저장 → 본문 `<img src>`를 다운로드 API URL로 **치환** → 수정 시 "유지 목록"에 없는 이미지 파일/row 삭제 → 삭제 시 `fileUtils.deleteDirectory`.
- **파일 다운로드 엔드포인트**: 각 QueryController의 `GET /{prefix}/files/{id}/{fileName}` (경로 정규화 + `startsWith` 검사로 traversal 방지).
- **미사용 클래스**: `faq/service/command/FAQFileStorageUtil.java`, `inspection/service/command/InspectionFileStorageUtil.java`, `notice/service/command/NoticeFileStorageUtil.java` — 참조처 0건(grep). 실제 저장은 전부 `FileUtils`.
- **예외**: 각 `*Exception extends global.exception.GlobalException`, `*ErrorCode implements ErrorCode`; `global/exception/GlobalExceptionHandler.java`에 도메인별 `@ExceptionHandler`(FAQ L210, Inspection L233, Notice L256, Inquiry L505).
- **초기 데이터**: `global/config/DataInitializer.java`가 Notice/FAQ/InspectionStandard 샘플을 저장(L201/215/228).
- **캐시**: `global/config/CacheConfig.java` — `ConcurrentMapCacheManager`에 `faqList, faqDetail, faqCategoryList, faqSearchResults, inspectionStandards, inspectionStandardsByCategory, inspectionStandardDetail, inspectionStandardSearchResults`만 등록. Inquiry/Notice는 캐시 없음.

### 도메인별 경로/저장 위치 요약
| 도메인 | API prefix | 저장 디렉토리(FileUtils 기준) | 파일 prefix | 본문 img src 치환 결과 | 다운로드 API가 읽는 경로 |
|---|---|---|---|---|---|
| faq | `/api/faq` | `/home/ubuntu/fream/faq/{id}` | `img_` | `/api/faq/files/{id}/{file}` (`FAQ.getImageUrlPath`) | `/home/ubuntu/fream/faq/{id}/{file}` ✔ 일치 |
| inquiry | `/inquiry` | `/home/ubuntu/fream/inquiry/{id}` | `img_` | `/api/inquiry/files/{id}/{file}` (`Inquiry.getImageUrlPath`) | `/home/ubuntu/fream/inquiry/{id}/{file}` ✔ 일치 |
| inspection | `/inspections` | `/home/ubuntu/fream/inspection_{id}` | `img_` | `https://www.pinjun.xyz/api/inspections/files/{id}/{file}` | `/home/ubuntu/fream/inspection/inspection_{id}/{file}` ✘ **불일치** |
| notice | `/notices` | `/home/ubuntu/fream/notice_{id}` | `notice_` | `https://www.pinjun.xyz/api/notices/files/{id}/{file}` | `/home/ubuntu/fream/notice/notice_{id}/{file}` ✘ **불일치** |
- 근거: Inspection 저장 `InspectionStandardCommandService.java:82,89` vs 읽기 `InspectionQueryController.java:43`(`INSPECTION_BASE_DIR="/home/ubuntu/fream/inspection"`); Notice 저장 `NoticeCommandService.java:87,282` vs 읽기 `NoticeQueryService.java:42,162`(`NOTICE_BASE_DIR="/home/ubuntu/fream/notice"`).
- FAQ만 컨트롤러 prefix에 `/api`가 포함됨. 나머지는 치환 URL에만 `/api`가 붙음.

---

## 1. FAQ (`domain/faq`, 30 files)
### 개요
자주 묻는 질문(Q/A HTML) CRUD. 관리자만 작성. Spring Cache 적용. `chatQuestion` 도메인(GPT 챗봇)이 FAQ 전체를 컨텍스트로 사용.
### 파일
```
src/main/java/com/fream/back/domain/faq/
├── aop/ FAQAuditAspect, FAQCacheAspect(@Order(1)), FAQContentProcessingAspect, FAQSearchOptimizationAspect, FAQValidationAspect(@Order(1))
│   └── annotation/ FAQAudit, FAQCache, FAQContentProcessing, FAQSearchOptimization, FAQValidation
├── controller/command/FAQCommandController.java, controller/query/FAQQueryController.java
├── dto/ FAQCreateRequestDto, FAQUpdateRequestDto, FAQResponseDto
├── entity/ FAQ, FAQCategory, FAQImage
├── exception/ FAQErrorCode, FAQException, FAQFileException, FAQNotFoundException, FAQPermissionException
├── repository/ FAQRepository, FAQRepositoryCustom, FAQRepositoryImpl, FAQImageRepository
└── service/command/FAQCommandService.java, FAQFileStorageUtil.java(미사용); service/query/FAQQueryService.java
```
### 엔티티
- `FAQ` (BaseTimeEntity): `id`, `category`(`FAQCategory`, STRING, not null), `question`(len 100), `answer`(`@Lob`). `update(category, question, answer)`, `getFileDirectory()`="faq/{id}", `getImageUrlPath(file)`="/api/faq/files/{id}/{file}".
- `FAQImage`: `id`, `imageUrl`(파일명만), `faq`(ManyToOne LAZY, `faq_id`). `getFullImageUrl()`. FAQ→FAQImage 역방향 컬렉션 없음.
- `FAQCategory`: `POLICY, GENERAL, BUYING, SELLING`.
### API
| Method | Path | Controller#method | Service#method | 권한 |
|---|---|---|---|---|
| POST | `/api/faq` (multipart: category, question, answer, files) | `FAQCommandController#createFAQ` | `FAQCommandService#createFAQ` | ADMIN |
| PUT | `/api/faq/{id}` (+`retainedImageUrls`, `newFiles`) | `#updateFAQ` | `#updateFAQ` | ADMIN |
| DELETE | `/api/faq/{id}` | `#deleteFAQ` | `#deleteFAQ` | ADMIN |
| GET | `/api/faq?category=` | `FAQQueryController#getFAQs` | category 있으면 `#getFAQsByCategory`, 없으면 `#getFAQs` | 인증 |
| GET | `/api/faq/{id}` | `#getFAQ` | `#getFAQ` | 인증 |
| GET | `/api/faq/search?keyword=` | `#searchFAQs` | `#searchFAQs` | 인증 |
| GET | `/api/faq/all` | `#getAllFAQs` | `#getAllFAQs` | 인증 |
| GET | `/api/faq/files/{faqId}/{fileName}` | `#downloadFAQFile` | (컨트롤러에서 직접 파일 읽기) | 인증 |
- 응답은 `ResponseDto<...>` 래핑(`ResponseDto.success(...)`).
### 서비스
- `FAQCommandService`: `createFAQ`, `updateFAQ`(retainedImageUrls에 없는 FAQImage 파일+row 삭제 후 newFiles 처리), `deleteFAQ`(이미지 삭제 + `fileUtils.deleteDirectory("faq/{id}")`). 내부 `processImagesAndUpdateHtml` → `updateHtmlImageSources`: **Jsoup**으로 `img[src^='data:image']`를 순서대로 저장 이미지 URL로 교체, 남는 이미지는 `<p><img src=... alt="첨부 이미지"></p>`로 body 끝에 추가.
  - 캐시 무효화: create → `faqList/faqCategoryList/faqSearchResults` allEntries; update/delete → 추가로 `faqDetail#id`.
- `FAQQueryService`: `getFAQs`(`@Cacheable faqList 'all:page:size'`), `getFAQ`(`faqDetail #id`), `getFAQsByCategory`(`faqCategoryList`), `searchFAQs`(`faqSearchResults`), `getAllFAQs`(`faqList 'all'`).
### Repository
- `FAQRepository`: `@Query findWithImagesById`, `@Query findAllWithPaging`, `findByCategory`.
- `FAQRepositoryImpl#searchFAQs`(QueryDSL): question OR answer `like %keyword%`, 정렬 미적용.
- `FAQImageRepository#findAllByFaqId`.
### AOP (`faq/aop`) — 어노테이션 사용처 **0건**(grep) → `@Around` 로직은 현재 미동작
| Aspect | 포인트컷 | 내용 | 주입 |
|---|---|---|---|
| `FAQValidationAspect` (@Order(1)) | `@Pointcut("@annotation(...FAQValidation)")` + `@Around` | 질문/답변 길이, 파일(확장자·크기·시그니처), 카테고리, 금지어, HTML/SQL 인젝션 정규식, 중복, 관리자 권한; 실패 시 `onFailure` 정책 | FAQRepository |
| `FAQCacheAspect` (@Order(1)) | `@annotation(faqCache)` | CacheManager 기반 키 전략 캐싱, 카테고리/ID 무효화, 인기 FAQ 통계 | CacheManager |
| `FAQContentProcessingAspect` | `@annotation(contentProcessing)` | Create/Update DTO의 HTML을 Jsoup로 XSS 제거·화이트리스트 sanitize, img 태그 최적화(lazy), 링크 검증/새창, 이미지 리사이즈·압축 | — |
| `FAQSearchOptimizationAspect` | `@annotation(searchOptimization)` | 키워드 정규화·동의어·초성·오타 교정, 검색 로그 INSERT `faq_search_log`(JdbcTemplate), 결과 캐시 | JdbcTemplate, CacheManager |
| `FAQAuditAspect` | `@annotation(faqAudit)` | 감사 로그 INSERT `faq_audit_log`, `UPDATE faq SET view_count`(엔티티에 해당 필드 없음), IP/파라미터 마스킹 | JdbcTemplate |
- 단, `FAQSearchOptimizationAspect`는 빈이므로 `@PostConstruct init()`과 `@Scheduled(fixedDelay = 3600000) updatePopularKeywords()`는 **실제로 실행됨**(인기 검색어 로딩은 SQL 문자열만 있고 조회 미구현, 로그 Map 정리만 수행).
### 예외 (`FAQErrorCode`, prefix `FAQ`)
FAQ001 NOT_FOUND(404), FAQ002 QUERY_ERROR, FAQ003 SAVE_ERROR, FAQ004 DELETE_ERROR, FAQ005 UPDATE_ERROR(500), FAQ101 FILE_SAVE_ERROR, FAQ102 FILE_DELETE_ERROR(500), FAQ103 FILE_NOT_FOUND(404), FAQ104 UNSUPPORTED_FILE_TYPE(400), FAQ201 ADMIN_PERMISSION_REQUIRED(403), FAQ301 INVALID_CATEGORY, FAQ302 INVALID_REQUEST_DATA(400).
### 주의점
- `FAQCommandController#checkAdminPermission`(L131)은 `AccessDeniedException`만 catch하지만 `checkAdminRole`은 `UserException`을 던지므로 실제로는 FAQ201이 아니라 user 에러코드가 응답됨.
- 외부 의존: `domain/chatQuestion/service/ChatService.java:62`가 `FAQQueryService#getAllFAQs()` 호출 → 반환 DTO(`FAQResponseDto`) 변경 시 `GPTService`까지 영향.

---

## 2. Inquiry 1:1 문의 (`domain/inquiry`, 24 files)
### 개요
사용자 1:1 문의 작성/수정/삭제 + 관리자 답변/상태 변경. 비공개 문의 접근 제어. 서비스는 **인터페이스 + Impl** 구조(다른 3개와 다름).
### 파일
```
src/main/java/com/fream/back/domain/inquiry/
├── controller/command/InquiryCommandController.java, controller/query/InquiryQueryController.java
├── dto/ InquiryCreateRequestDto, InquiryUpdateRequestDto, InquiryAnswerRequestDto, InquiryResponseDto, InquirySearchCondition, InquirySearchResultDto
├── entity/ Inquiry, InquiryCategory, InquiryImage, InquiryStatus
├── exception/ InquiryErrorCode, InquiryException, InquiryFileException, InquiryNotFoundException
├── repository/ InquiryRepository, InquiryRepositoryCustom, InquiryRepositoryImpl, InquiryImageRepository
└── service/command/InquiryCommandService(+Impl), service/query/InquiryQueryService(+Impl)
```
### 엔티티
- `Inquiry` (`@Table("inquiries")`, BaseTimeEntity): `title`(100), `content`(TEXT, HTML), `answer`(TEXT), `user`(ManyToOne LAZY `user_id` not null → `domain.user.entity.User`), `status`(기본 `REQUESTED`), `category`, `isPrivate`, `answeredBy`, `pushNotification`.
  - `setAnswer(answer, answeredBy)` → status=ANSWERED; `updateStatus`; `updateInquiry(...)`(ANSWERED면 무시); `getFileDirectory()`="inquiry/{id}"; `getImageUrlPath`="/api/inquiry/files/{id}/{file}".
- `InquiryImage` (`inquiry_images`): `inquiry`(not null), `imageUrl`(파일명), `originalFileName`, `fileSize`, `isAnswer`(답변 이미지 여부).
- `InquiryStatus`: `REQUESTED(질문요청)`, `CONFIRMED(질문확인)`, `ANSWERED(답변완료)`.
- `InquiryCategory`: `PRODUCT, ORDER, DELIVERY, RETURN, ACCOUNT, ETC`.
### API (`/inquiry`)
| Method | Path | Controller#method | Service#method | 권한 |
|---|---|---|---|---|
| POST | `/inquiry` (multipart) | `InquiryCommandController#createInquiry` | `InquiryCommandService#createInquiry(userId, dto)` | 로그인 사용자 |
| PUT | `/inquiry/{inquiryId}` (+`newFiles`, `retainedImageUrls`) | `#updateInquiry` | `#updateInquiry` (작성자 본인 + 미답변만) | 본인 |
| DELETE | `/inquiry/{inquiryId}` | `#deleteInquiry` | `#deleteInquiry(id, userId, isAdmin)` | 본인 또는 ADMIN |
| POST | `/inquiry/{inquiryId}/answer` (multipart: answer, answeredBy, files) | `#answerInquiry` | `#answerInquiry` | ADMIN (`checkAdminRole`) |
| PUT | `/inquiry/{inquiryId}/status?status=` | `#updateInquiryStatus` | `#updateInquiryStatus` | ADMIN |
| GET | `/inquiry/{inquiryId}` | `InquiryQueryController#…`(L60) | `InquiryQueryService#getInquiry(id, userId, isAdmin)` | 비공개면 작성자/ADMIN |
| GET | `/inquiry/my` | (L84) | `#getUserInquiries` | 로그인 |
| GET | `/inquiry?status&category&keyword` | (L108~) | `#getInquiries(condition isAdmin=true)` | ADMIN |
| GET | `/inquiry/status/{status}` | (L150) | `#getInquiriesByStatus` | ADMIN |
| GET | `/inquiry/category/{category}` | (L176) | `#getInquiriesByCategory`(isAdmin=false) | 인증 |
| GET | `/inquiry/search?keyword=` | (L198) | `#searchInquiries` → `findByTitleOrContentContaining` | 인증 |
| GET | `/inquiry/pending` | (L219) | `#getPendingInquiries` | ADMIN |
| GET | `/inquiry/statistics` | (L241) | `#getInquiryStatistics` | ADMIN |
| GET | `/inquiry/files/{inquiryId}/{fileName}` | (L264) | 내부에서 `getInquiry`로 접근권한 확인 후 파일 반환 | 작성자/ADMIN(비공개) |
- isAdmin 판단: 컨트롤러에서 `user.getRole().name().equals("ADMIN")`(L67, L105, L284).
### 서비스
- `InquiryCommandServiceImpl`(`@Transactional`): create/update/delete/updateStatus/answer. 이미지: `processImagesAndUpdateHtml(inquiry, files, html, isAnswer)` → `fileUtils.saveFile("inquiry/{id}", "img_", f)` → Jsoup `img[src^='data:image']` 치환(FAQ와 동일 방식). 수정 시 `findAllByInquiry_IdAndIsAnswerFalse` 중 retainedImageUrls에 없는 것 삭제. 삭제 시 `deleteDirectory("inquiry/{id}")`.
- `InquiryQueryServiceImpl`(`readOnly`): getInquiry(비공개 접근 체크 L48), getInquiries, getUserInquiries, getInquiriesByStatus, getInquiriesByCategory, searchInquiries, getPendingInquiries, getInquiryStatistics.
### Repository
- `InquiryRepository`: 파생 쿼리(`findByUserIdOrderByCreatedDateDesc`, `findByStatus`, `findByCategory`, `findByCreatedDateBetween`, `findByTitleContaining`, `findByUserIdAndStatus`, count*), `@Query findAllUnansweredInquiries`.
- `InquiryRepositoryImpl`(QueryDSL, DTO projection + user/profile join): `searchInquiries(condition)`(userId/status/category/keyword/date, `!isAdmin`이면 `isPrivate=false OR user.id=userId` L79-80), `findInquiryWithUserDetails`, `findUnansweredInquiriesOrderByOldest`, `findByTitleOrContentContaining`, `getInquiryStatistics`(status별/category별/월별 count Map).
- `InquiryImageRepository`: `findAllByInquiry_Id`, `...IsAnswerFalse/True`, `deleteAllByInquiry_Id`, `countByInquiry_Id`, `@Query findByInquiry_IdAndImageUrlContaining`.
### 예외 (`InquiryErrorCode`, prefix `INQUIRY-{http}-{n}`)
INQUIRY-400-1~6(INVALID_INPUT, FILE_EMPTY, FILE_TOO_LARGE, FILE_UNSUPPORTED_TYPE, STATUS_INVALID, ALREADY_ANSWERED), INQUIRY-401-1 UNAUTHORIZED, INQUIRY-403-1 ACCESS_DENIED, 403-2 PRIVATE, INQUIRY-404-1 NOT_FOUND, 404-2 IMAGE_NOT_FOUND, INQUIRY-500-1~5(SAVE/UPDATE/DELETE/FILE_SAVE/FILE_DELETE).
### 주의점
- `pushNotification` 필드는 저장만 되고 답변 시 알림 발송 코드 없음(inquiry 패키지에 notification 참조 0건).
- `/inquiry/search`(`findByTitleOrContentContaining`)는 isPrivate 필터가 없음 → 비공개 문의도 결과에 포함(Impl L206~).
- `/inquiry/category/{category}`는 userId 없이 isAdmin=false 조건을 만들어 `inquiry.user.id.eq(condition.getUserId())`(null)로 호출됨(Impl L79-80).
- 조회 실패를 `INQUIRY_NOT_FOUND`로 래핑하는 catch-all이 많음(예: getInquiryStatistics).
- 의존: `domain.user`(User 엔티티, `UserRepository`, `UserQueryService#findByEmail/checkAdminRole`).

---

## 3. Inspection 검수 기준 (`domain/inspection`, 20 files)
### 개요
카테고리별 상품 검수 기준(HTML) CRUD. 관리자만 작성. 캐시 사용.
### 파일
```
src/main/java/com/fream/back/domain/inspection/
├── controller/command/InspectionCommandController.java, controller/query/InspectionQueryController.java
├── dto/ InspectionStandardCreateRequestDto, InspectionStandardUpdateRequestDto, InspectionStandardResponseDto
├── entity/ InspectionStandard, InspectionStandardImage, InspectionCategory
├── exception/ InspectionErrorCode, InspectionException, InspectionFileException, InspectionNotFoundException, InspectionPermissionException
├── repository/ InspectionStandardRepository, InspectionStandardRepositoryCustom, InspectionStandardRepositoryImpl, InspectionStandardImageRepository
└── service/command/InspectionStandardCommandService.java, InspectionFileStorageUtil.java(미사용); service/query/InspectionStandardQueryService.java
```
### 엔티티
- `InspectionStandard` (BaseTimeEntity): `category`(STRING), `content`(`@Lob`). `update(category, content)`.
- `InspectionStandardImage`: `imageUrl`(= `"inspection_{id}/파일명"` 상대경로), `inspectionStandard`(ManyToOne LAZY, not null).
- `InspectionCategory`: `SHOES, OUTER, BAG, TECH, BEAUTY, PREMIUM_WATCH, PREMIUM_BAG`.
### API (`/inspections`)
| Method | Path | Controller#method | Service#method | 권한 |
|---|---|---|---|---|
| POST | `/inspections` (multipart: category, content, files) | `InspectionCommandController#createStandard` | `InspectionStandardCommandService#createStandard` | ADMIN |
| PUT | `/inspections/{id}` (+`existingImageUrls`, `newFiles`) | `#updateStandard` | `#updateStandard` | ADMIN |
| DELETE | `/inspections/{id}` | `#deleteStandard` | `#deleteStandard` | ADMIN |
| GET | `/inspections?category&keyword` (기본 sort id DESC) | `InspectionQueryController#getStandards` | keyword → `searchStandards`, category → `getStandardsByCategory`, 없으면 `getStandards` | 인증 |
| GET | `/inspections/{id}` | (L89) | `#getStandard` | 인증 |
| GET | `/inspections/files/{inspectionId}/{fileName}` | (L105) | 컨트롤러 직접 파일 읽기 | 인증 |
### 서비스
- `InspectionStandardCommandService`: create(파일을 `inspection_{id}`에 `img_` prefix로 저장 → `updateImagePaths`로 본문 `<img src>`를 **정규식**(`<img\s+[^>]*src="..."`) 순서 치환 → `saveStandardImages`), update(기존 이미지 중 `existingImageUrls`에도 content에도 없는 것 삭제 → newFiles 저장/치환), delete(`deleteDirectory("inspection_{id}")`), `validateStandardData`.
- 캐시 무효화는 서비스가 아니라 **컨트롤러** `clearInspectionCaches()`(L77)가 `CacheManager.getCache(name).clear()`로 4개 캐시 전체 clear.
- `InspectionStandardQueryService`: `getStandards`(`@Cacheable inspectionStandards`), `getStandardsByCategory`(`inspectionStandardsByCategory`), `searchStandards`(`inspectionStandardSearchResults`), `getStandard`(`inspectionStandardDetail`).
### Repository
- `InspectionStandardRepository`: `@Query` `findWithImagesById`, `findAllWithPaging`(countQuery 별도), `findByCategory`(LEFT JOIN FETCH ... ON 구문).
- `InspectionStandardRepositoryImpl`(QueryDSL): `searchStandards(keyword)`, `searchStandardsByCategoryAndKeyword`.
- `InspectionStandardImageRepository#findAllByInspectionStandardId`.
### 예외 (`InspectionErrorCode`, prefix `INS`)
INS001 NOT_FOUND(404), INS002~005 QUERY/SAVE/DELETE/UPDATE(500), INS101 FILE_SAVE, INS102 FILE_DELETE(500), INS103 FILE_NOT_FOUND(404), INS104 UNSUPPORTED_FILE_TYPE(400), INS201 ADMIN_PERMISSION_REQUIRED(403), INS301 INVALID_CATEGORY, INS302 INVALID_REQUEST_DATA(400).
### 주의점
- **저장 경로/다운로드 경로 불일치**(0절 표). 이미지 관련 수정 시 `FileUtils` 기준 `/home/ubuntu/fream/inspection_{id}`와 `InspectionQueryController.INSPECTION_BASE_DIR`를 함께 확인.
- 치환 URL 도메인 `https://www.pinjun.xyz` 하드코딩(`InspectionStandardCommandService.java:44-45`).

---

## 4. Notice 공지사항 (`domain/notice`, 20 files)
### 개요
공지사항 CRUD(HTML + 이미지/비디오). 생성 시 **전체 사용자 알림 발송**(notification 도메인).
### 파일
```
src/main/java/com/fream/back/domain/notice/
├── controller/command/NoticeCommandController.java, controller/query/NoticeQueryController.java
├── dto/ NoticeCreateRequestDto, NoticeUpdateRequestDto, NoticeResponseDto
├── entity/ Notice, NoticeCategory, NoticeImage
├── exception/ NoticeErrorCode, NoticeException, NoticeFileException, NoticeNotFoundException, NoticePermissionException
├── repository/ NoticeRepository, NoticeRepositoryCustom, NoticeRepositoryImpl, NoticeImageRepository
└── service/command/NoticeCommandService.java, NoticeFileStorageUtil.java(미사용); service/query/NoticeQueryService.java
```
### 엔티티
- `Notice` (BaseTimeEntity, 인덱스 `idx_notice_category`, `idx_notice_created_date`): `category`, `title`(255), `content`(`@Lob`), `images`(`@OneToMany(mappedBy="notice", cascade=ALL, orphanRemoval=true)`). `addImage/removeImage/update/updateContent`.
- `NoticeImage` (BaseTimeEntity, 인덱스 `idx_notice_image_notice_id`): `imageUrl`(= `"notice_{id}/파일명"`), `isVideo`, `notice`. `getFileName()`, `getDirectory()`는 imageUrl을 `/` 기준 분리.
- `NoticeCategory`: `ANNOUNCEMENT(공지사항), EVENT(이벤트), SERVICE(서비스 안내), OTHERS(기타)`; `fromString()`(실패 시 OTHERS), `toString()`은 description 반환.
### API (`/notices`)
| Method | Path | Controller#method | Service#method | 권한 |
|---|---|---|---|---|
| POST | `/notices` (multipart: title, content, category, files) | `NoticeCommandController#createNotice` | `NoticeCommandService#createNotice` | ADMIN |
| PUT | `/notices/{noticeId}` (+`existingImageUrls`, `newFiles`) | `#updateNotice` | `#updateNotice` | ADMIN |
| DELETE | `/notices/{noticeId}` | `#deleteNotice` | `#deleteNotice` | ADMIN |
| GET | `/notices?category=` | `NoticeQueryController`(L95~) | category 있으면 `getNoticesByCategory`(`NoticeCategory.valueOf(category.toUpperCase())`), 없으면 `getNotices` | 인증 |
| GET | `/notices/{noticeId}` | (L39) | `#getNotice` | 인증 |
| GET | `/notices/search?keyword=` | (L70) | `#searchNotices` | 인증 |
| GET | `/notices/files/{noticeId}/{fileName}` | (L142) | `NoticeQueryService#getNoticeFileResource` | 인증 |
- `createNotice`/`updateNotice`의 `@ModelAttribute`에는 `@Valid`가 없음 → DTO의 `@NotBlank/@NotNull`이 컨트롤러 단에서 검증되지 않음.
### 서비스
- `NoticeCommandService`(`@Transactional`): 허용 확장자 이미지 `.jpg/.jpeg/.png/.gif`, 비디오 `.mp4/.avi/.mov`(`validateFileExtension`).
  - create: 저장 → `sendNotificationToAllUsers` (실패해도 catch로 무시) → 파일 `saveNoticeFiles("notice_{id}")`(prefix `notice_`) → `updateImageUrlsInContent`(정규식 src 치환, `https://www.pinjun.xyz/api/notices/files/{id}/{file}`) → `saveNoticeImageEntities`(isVideo 판정).
  - update: `extractImagePathsFromContent(content)`의 src 목록에 `NoticeImage.imageUrl`이 없으면 row+파일 삭제 → newFiles 저장/치환.
  - delete: `deleteDirectory("notice_{id}")` 후 삭제.
- `NoticeQueryService`(`readOnly`): `getNotices`, `getNotice`, `getNoticesByCategory`, `searchNotices`, `getNoticeFileResource`.
### 알림 연동
- `sendNotificationToAllUsers`(NoticeCommandService.java:485): `NotificationRequestDTO{category=NotificationCategory.SHOPPING, type=NotificationType.ANNOUNCEMENT, message="새로운 공지사항: "+title}` → `domain/notification/service/command/NotificationCommandService#createNotificationForAll`(모든 User 조회 후 생성, L72). 실패 시 `NOTICE_NOTIFICATION_ERROR` 던지지만 createNotice에서 catch.
### Repository
- `NoticeRepository`: `findWithImagesById`, `findByCategory`, `@Query findAllOrderByCreatedDateDesc`, `@Query findByCategoryOrderByCreatedDateDesc`.
- `NoticeRepositoryImpl`(QueryDSL): `searchNotices(keyword)`, `searchNoticesByCategoryAndKeyword`.
- `NoticeImageRepository`: `findAllByNoticeId`, `@Query findImagesToDelete`(NOT IN), `@Modifying deleteAllByNoticeId`, `@Modifying deleteByImageUrls`.
### 예외 (`NoticeErrorCode`, prefix `NOT`)
NOT001 NOT_FOUND(404), NOT002~005 QUERY/SAVE/DELETE/UPDATE(500), NOT101 FILE_SAVE, NOT102 FILE_DELETE(500), NOT103 FILE_NOT_FOUND(404), NOT104 UNSUPPORTED_FILE_TYPE(400), NOT201 ADMIN_PERMISSION_REQUIRED(403), NOT301 INVALID_CATEGORY, NOT302 INVALID_REQUEST_DATA(400), NOT401 NOTIFICATION_ERROR, NOT402 HTML_PARSING_ERROR(500).
### 주의점
- **저장 경로/다운로드 경로 불일치**(0절 표).
- update의 삭제 판정은 `NoticeImage.imageUrl`(상대경로 `notice_{id}/파일`)과 본문 src(치환 후 절대 URL) 문자열을 그대로 `contains` 비교(NoticeCommandService.java:141-145) — 형식이 달라 기존 이미지가 삭제 대상이 될 수 있음.
- 알림 발송(L79)이 파일 저장(L86~)보다 먼저 호출됨.

---

## 5. 다른 도메인 의존 요약
| 도메인 | 의존 대상 |
|---|---|
| faq | `user.UserQueryService`, `global.FileUtils`, `CacheConfig`; 역의존: `chatQuestion.ChatService/GPTService`, `DataInitializer` |
| inquiry | `user.entity.User`, `user.repository.UserRepository`, `UserQueryService`, `SecurityUtils`, `FileUtils` |
| inspection | `UserQueryService`, `FileUtils`, `CacheManager`; 역의존: `DataInitializer` |
| notice | `UserQueryService`, `FileUtils`, `notification.NotificationCommandService`(+DTO/enum); 역의존: `DataInitializer` |

## 6. 변경 작업 팁
- 새 콘텐츠 도메인/기능 추가 시: Command/Query 컨트롤러·서비스 분리, `checkAdminRole`로 관리자 체크, `FileUtils.saveFile(dir, prefix, file)` + 본문 src 치환 + `GET /{prefix}/files/{id}/{fileName}` 패턴을 따르되, **저장 dir과 다운로드 BASE_DIR 조합이 일치하는지**(faq/inquiry 방식) 확인.
- 저장 루트 `/home/ubuntu/fream`은 `FileUtils`와 각 QueryController/QueryService에 중복 하드코딩(Windows 경로는 주석).
- 캐시 추가 시 `global/config/CacheConfig.java`의 `setCacheNames` 목록에 이름을 등록해야 함(고정 목록).
- `*FileStorageUtil` 3종은 dead code이므로 수정 대상 아님.
