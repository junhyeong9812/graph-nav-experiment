# 고객센터 / 콘텐츠 도메인 (notice, faq, inquiry, inspection, event)

> 패키지 루트: `src/main/java/com/fream/back/domain/` (아래 경로는 이 루트 기준 상대경로. 단 global 은 `src/main/java/com/fream/back/global/`).
> 이 문서는 코드를 읽고 확인한 사실만 담았다. 확인하지 못한 부분은 "(미확인)"으로 표시했다.

## 0. 공통 전제 (전 도메인 공통)

| 항목 | 사실 | 근거 |
|---|---|---|
| 인증 | `SecurityConfig`의 whitelist 경로가 아니면 모두 `anyRequest().authenticated()`. 이 문서의 도메인 경로(`/notices`, `/api/faq`, `/inquiry`, `/inspections`, `/events`)는 whitelist에 없으므로 **GET이나 파일 다운로드도 인증이 필요하다** | `global/config/security/SecurityConfig.java:51-52, 89-127` |
| 관리자 체크 | 컨트롤러에서 `SecurityContextHolder`나 `SecurityUtils.extractEmailFromSecurityContext()`로 email을 꺼낸 뒤 `UserQueryService.checkAdminRole(email)`을 호출한다. 이 메서드는 `Role.ADMIN`이 아니면 `UserException(INSUFFICIENT_PERMISSIONS)`을 던진다 | `domain/user/service/query/UserQueryService.java:25-37` |
| 페이징 | `spring.data.web.pageable.one-indexed-parameters: true`(page는 1부터), 기본 size 10, 최대 2000 | `src/main/resources/application.yml` |
| 파일 저장 | `global/utils/FileUtils`의 `BASE_DIR = "/home/ubuntu/fream"`(하드코딩). `saveFile(directory, prefix, file)`은 MIME이 `image/*` 또는 `video/*`인 파일만 받고, `prefix + UUID + 확장자` 이름으로 `/home/ubuntu/fream/{directory}`에 저장한다 | `global/utils/FileUtils.java:27, 66-` |
| 예외 처리 | 각 도메인의 `*Exception extends GlobalException`. `GlobalExceptionHandler`에 도메인별 `@ExceptionHandler`가 있다(FAQ, Inspection, Notice, Event, Inquiry, ChatQuestion) | `global/exception/GlobalExceptionHandler.java` |
| 캐시 | `global/config/CacheConfig`이 `@EnableCaching`과 `ConcurrentMapCacheManager`(bean명 `cacheManager`)를 선언하고, 캐시 이름을 **고정 목록으로** 등록한다: `faqList, faqDetail, faqCategoryList, faqSearchResults, inspectionStandards, inspectionStandardsByCategory, inspectionStandardDetail, inspectionStandardSearchResults`. CacheManager bean이 하나 더 있다(`accessLogCacheManager`, `domain/accessLog/config/AccessLogAopConfiguration.java:74`). 둘 다 `@Primary`가 아니다 | `global/config/CacheConfig.java` |
| 초기 데이터 | `global/config/DataInitializer`(CommandLineRunner)에 `createNoticeData()`, `createFAQData()`, `createInspectionStandardData()`가 있다 | `global/config/DataInitializer.java:198-226` |
| 테스트 | `src/test`에는 notice / faq / inquiry / inspection / event / chatQuestion 테스트가 **없다**(address, notification, order, payment, product, sale, style, user와 `PersistenceSmokeTest`만 있음) | `src/test/java/com/fream/back/domain/` |

---

## 1. notice (공지사항)

**역할**: 관리자가 공지사항을 등록·수정·삭제한다(HTML 본문 + 이미지/비디오 첨부). 등록하면 전체 사용자에게 알림을 보낸다. 조회, 카테고리 필터, 검색, 첨부파일 다운로드를 제공한다.

### 파일 맵
| 레이어 | 파일 |
|---|---|
| controller | `notice/controller/command/NoticeCommandController.java`, `notice/controller/query/NoticeQueryController.java` |
| service | `notice/service/command/NoticeCommandService.java`, `notice/service/query/NoticeQueryService.java`, `notice/service/command/NoticeFileStorageUtil.java`(**어디서도 주입되지 않는 미사용 클래스**) |
| repository | `notice/repository/NoticeRepository.java`, `NoticeRepositoryCustom.java`, `NoticeRepositoryImpl.java`(QueryDSL), `NoticeImageRepository.java` |
| entity | `notice/entity/Notice.java`, `NoticeImage.java`, `NoticeCategory.java` |
| dto | `notice/dto/NoticeCreateRequestDto.java`, `NoticeUpdateRequestDto.java`, `NoticeResponseDto.java` |
| exception | `notice/exception/NoticeErrorCode.java`(NOT001~NOT402), `NoticeException`, `NoticeNotFoundException`, `NoticeFileException`, `NoticePermissionException` |

### REST 엔드포인트
| HTTP | 경로 | Controller 메서드 | Service 메서드 | 관리자 |
|---|---|---|---|---|
| POST | `/notices` (multipart `@ModelAttribute`) | `NoticeCommandController.createNotice` | `NoticeCommandService.createNotice` | O |
| PUT | `/notices/{noticeId}` | `updateNotice` | `NoticeCommandService.updateNotice` | O |
| DELETE | `/notices/{noticeId}` | `deleteNotice` (204) | `NoticeCommandService.deleteNotice` | O |
| GET | `/notices/{noticeId}` | `NoticeQueryController.getNotice` | `NoticeQueryService.getNotice` | - |
| GET | `/notices/search?keyword=` | `searchNotices` | `NoticeQueryService.searchNotices` | - |
| GET | `/notices?category=` | `getNoticesByCategory` | `getNoticesByCategory`, category가 없으면 `getNotices` | - |
| GET | `/notices/files/{noticeId}/{fileName}` | `getNoticeFile` | `NoticeQueryService.getNoticeFileResource` | - |

### 엔티티 / enum
- `Notice`(BaseTimeEntity): category(`NoticeCategory`, STRING), title(255), content(`@Lob`), images(`@OneToMany` cascade ALL, orphanRemoval)
- `NoticeImage`: imageUrl(상대경로 `notice_{id}/파일명`), isVideo, notice(ManyToOne)
- `NoticeCategory`: `ANNOUNCEMENT, EVENT, SERVICE, OTHERS`

### 핵심 비즈니스 규칙
- 컨트롤러가 title, content, category 필수 여부를 검증한다(`validateCreateRequest` / `validateUpdateRequest`).
- 생성: 저장 → `NotificationCommandService.createNotificationForAll`(category `SHOPPING`, type `ANNOUNCEMENT`, 메시지 "새로운 공지사항: {title}") 호출. 알림이 실패해도 로그만 남기고 계속 진행한다 → 파일 저장 → HTML `<img src>`를 순서대로 `https://www.pinjun.xyz/api/notices/files/{id}/{fileName}`으로 치환 → `NoticeImage` 저장.
- 허용 확장자: 이미지 jpg/jpeg/png/gif, 비디오 mp4/avi/mov(`NoticeCommandService.validateFileExtension`).
- 수정: 요청 content의 `<img src>`(정규식으로 추출)에 없는 기존 `NoticeImage`는 파일과 DB에서 함께 삭제한 뒤 `newFiles`를 저장하고 치환한다.
- 삭제: 이미지가 있으면 `fileUtils.deleteDirectory("notice_{id}")`를 실행한 뒤 엔티티를 삭제한다.

### 파일 저장
- 저장: `FileUtils.saveFile("notice_{id}", "notice_", file)` → 실제 위치 `/home/ubuntu/fream/notice_{id}/`
- 다운로드: `NoticeQueryService.NOTICE_BASE_DIR = "/home/ubuntu/fream/notice"` → `/home/ubuntu/fream/notice/notice_{id}/{fileName}`에서 읽는다(path traversal 검사 있음).
- **주의(경로 불일치)**: 저장 경로(`/home/ubuntu/fream/notice_{id}`)와 다운로드 경로(`/home/ubuntu/fream/notice/notice_{id}`)가 코드상 다르다.

### 캐시 / 스케줄러 / ES / AOP
- 캐시, 스케줄러, Elasticsearch, aop 패키지 모두 없다.

### 다른 도메인 의존
- `notification`(`NotificationCommandService`, `NotificationRequestDTO`, `NotificationCategory`, `NotificationType`), `user`(`UserQueryService.checkAdminRole`), `global/utils/FileUtils`.

### gotchas
- `NoticeCreateRequestDto.files`에는 초기값이 없는데 서비스가 `requestDto.getFiles().isEmpty()`를 호출한다. 파일 파라미터 없이 요청하면 NPE가 날 수 있다(바인딩 결과는 미확인).
- `searchNotices`의 `@PageableDefault(sort = "createdDate,desc")`는 정렬 속성명을 `"createdDate,desc"` 문자열 그대로 넘긴다(다른 메서드는 `direction = DESC` 형식).
- 응답이 `ResponseDto`로 감싸지지 않는다(DTO나 Page를 그대로 반환). FAQ, inquiry, inspection과 다르다.

---

## 2. faq

**역할**: FAQ CRUD(관리자), 카테고리별 조회, 검색, 전체 조회. **chatQuestion의 GPT 프롬프트 데이터 소스**이다(`FAQQueryService.getAllFAQs()`).

### 파일 맵
| 레이어 | 파일 |
|---|---|
| controller | `faq/controller/command/FAQCommandController.java`, `faq/controller/query/FAQQueryController.java` |
| service | `faq/service/command/FAQCommandService.java`, `faq/service/query/FAQQueryService.java`, `faq/service/command/FAQFileStorageUtil.java`(**미사용**) |
| repository | `faq/repository/FAQRepository.java`, `FAQRepositoryCustom.java`, `FAQRepositoryImpl.java`(QueryDSL `searchFAQs`), `FAQImageRepository.java` |
| entity | `faq/entity/FAQ.java`, `FAQImage.java`, `FAQCategory.java` |
| dto | `faq/dto/FAQCreateRequestDto.java`, `FAQUpdateRequestDto.java`, `FAQResponseDto.java` |
| exception | `faq/exception/FAQErrorCode.java`(FAQ001~FAQ302), `FAQException`, `FAQNotFoundException`, `FAQFileException`, `FAQPermissionException` |
| aop | `faq/aop/FAQCacheAspect`, `FAQValidationAspect`, `FAQAuditAspect`, `FAQContentProcessingAspect`, `FAQSearchOptimizationAspect` + `faq/aop/annotation/*` |

### REST 엔드포인트 (base `/api/faq`)
| HTTP | 경로 | Controller | Service | 관리자 |
|---|---|---|---|---|
| POST | `/api/faq` (`@Valid @ModelAttribute`) | `FAQCommandController.createFAQ` | `FAQCommandService.createFAQ` | O |
| PUT | `/api/faq/{id}` | `updateFAQ` | `updateFAQ` | O |
| DELETE | `/api/faq/{id}` | `deleteFAQ` | `deleteFAQ` | O |
| GET | `/api/faq?category=` | `FAQQueryController.getFAQs` | `getFAQsByCategory` / `getFAQs` | - |
| GET | `/api/faq/{id}` | `getFAQ` | `getFAQ` | - |
| GET | `/api/faq/search?keyword=` | `searchFAQs` | `searchFAQs` | - |
| GET | `/api/faq/all` | `getAllFAQs` | `getAllFAQs` | - |
| GET | `/api/faq/files/{faqId}/{fileName}` | `downloadFAQFile` | (컨트롤러가 `/home/ubuntu/fream/faq/{faqId}`에서 직접 읽음) | - |

### 엔티티 / enum
- `FAQ`(BaseTimeEntity): category, question(100), answer(`@Lob`, HTML). `getFileDirectory()` → `"faq/{id}"`, `getImageUrlPath(fileName)` → `"/api/faq/files/{id}/{fileName}"`
- `FAQImage`: imageUrl(**파일명만**), faq(ManyToOne)
- `FAQCategory`: `POLICY, GENERAL, BUYING, SELLING`
- DTO 검증: category `@NotNull`, question `@NotBlank @Size(max=100)`, answer `@NotBlank`. Update DTO에는 `retainedImageUrls`, `newFiles`가 있다.

### 비즈니스 규칙 / 파일
- 이미지는 `FileUtils.saveFile("faq/{id}", "img_", file)`로 저장된다(`/home/ubuntu/fream/faq/{id}/`). 저장과 다운로드 경로가 일치한다.
- HTML 치환은 Jsoup으로 한다: `img[src^='data:image']`를 저장된 이미지 URL로 순서대로 바꾸고, 남은 이미지는 `<p><img ...></p>`로 body 끝에 붙인다(`FAQCommandService.updateHtmlImageSources`).
- 수정: `retainedImageUrls`에 없는 기존 이미지(파일명 기준)를 삭제한다. 삭제: `deleteDirectory(faq.getFileDirectory())`.

### 캐시 (Spring Cache, `CacheConfig`에 등록됨)
| 메서드 | 어노테이션 |
|---|---|
| `FAQQueryService.getFAQs` | `@Cacheable("faqList", key='all:'+page+':'+size)` |
| `getFAQ` | `@Cacheable("faqDetail", key=#id)` |
| `getFAQsByCategory` | `@Cacheable("faqCategoryList")` |
| `searchFAQs` | `@Cacheable("faqSearchResults")` |
| `getAllFAQs` | `@Cacheable("faqList", key='all')` |
| `FAQCommandService.create` | evict faqList, faqCategoryList, faqSearchResults (allEntries) |
| `update` / `delete` | 위 3개 + `faqDetail(#id)` evict |

- 페이지 캐시 키에 sort가 들어가지 않는다.

### AOP (faq/aop)
**중요: `@FAQCache`, `@FAQValidation`, `@FAQAudit`, `@FAQContentProcessing`, `@FAQSearchOptimization`은 main 코드 어디에도 붙어 있지 않다(grep으로 확인). 따라서 `@annotation(...)` 기반 `@Around` advice는 실행되지 않는다.** 다만 Aspect는 `@Component` bean이라 bean 수명주기 코드는 동작한다.

| Aspect | 대상 어노테이션 | 의도된 동작(코드 기준) | 실제로 동작하는 부분 |
|---|---|---|---|
| `FAQCacheAspect` (@Order 1) | `@FAQCache` | CacheManager를 이용한 캐시와 통계, 카테고리 무효화 | 없음 |
| `FAQValidationAspect` (@Order 1) | `@FAQValidation` | 길이, 금칙어, 중복, HTML/SQL 인젝션 검사 | `@PostConstruct` 초기화 |
| `FAQAuditAspect` | `@FAQAudit` | 감사 로그를 JdbcTemplate로 `faq_audit_log`에 INSERT, 조회수 집계 | 없음 |
| `FAQContentProcessingAspect` | `@FAQContentProcessing` | HTML sanitize/XSS, 이미지 태그 최적화, 요약 | 없음 |
| `FAQSearchOptimizationAspect` | `@FAQSearchOptimization` | 키워드 정규화, 동의어, 초성, 오타 교정, `faq_search_log` INSERT | `@PostConstruct init()`, **`@Scheduled(fixedDelay=3600000) updatePopularKeywords()`**(인메모리 로그 정리. 인기 키워드 조회 로직은 "구현 필요" 상태) |

### 다른 도메인 의존
- 사용하는 쪽: `user`(checkAdminRole), FileUtils. **사용되는 쪽**: `chatQuestion/service/ChatService`(`FAQQueryService.getAllFAQs`), `chatQuestion/service/GPTService`(`FAQResponseDto`).

### gotchas
- `FAQCommandController.checkAdminPermission`은 `AccessDeniedException`만 잡는다. 실제로 던져지는 것은 `UserException`이므로 FAQPermissionException으로 바뀌지 않고 그대로 전파된다.
- `getAllFAQs` 결과는 `faqList::all`로 캐시된다. FAQ를 변경하면 evict되므로 챗봇 프롬프트에 반영된다.

---

## 3. inquiry (1:1 문의)

**역할**: 사용자가 1:1 문의를 작성·수정·삭제한다(HTML + 이미지). 관리자는 답변, 상태 변경, 목록/통계 조회를 한다. 비공개 문의 접근을 제어한다.

### 파일 맵
| 레이어 | 파일 |
|---|---|
| controller | `inquiry/controller/command/InquiryCommandController.java`, `inquiry/controller/query/InquiryQueryController.java` |
| service | `inquiry/service/command/InquiryCommandService.java`(interface) + `InquiryCommandServiceImpl.java`, `inquiry/service/query/InquiryQueryService.java` + `InquiryQueryServiceImpl.java` |
| repository | `inquiry/repository/InquiryRepository.java`, `InquiryRepositoryCustom.java`, `InquiryRepositoryImpl.java`(QueryDSL: `searchInquiries`, `findByTitleOrContentContaining`, `findUnansweredInquiriesOrderByOldest`, `getInquiryStatistics`), `InquiryImageRepository.java` |
| entity | `inquiry/entity/Inquiry.java`(`inquiries`), `InquiryImage.java`(`inquiry_images`), `InquiryStatus.java`, `InquiryCategory.java` |
| dto | `InquiryCreateRequestDto`, `InquiryUpdateRequestDto`, `InquiryAnswerRequestDto`, `InquiryResponseDto`, `InquirySearchCondition`, `InquirySearchResultDto` |
| exception | `inquiry/exception/InquiryErrorCode.java`(`INQUIRY-400-1` ~ `INQUIRY-500-5`), `InquiryException`, `InquiryNotFoundException`, `InquiryFileException` |

### REST 엔드포인트 (base `/inquiry`, 응답은 `ResponseDto`)
| HTTP | 경로 | Controller | Service | 권한 |
|---|---|---|---|---|
| POST | `/inquiry` | `createInquiry` | `createInquiry(userId, dto)` | 로그인 |
| PUT | `/inquiry/{inquiryId}` | `updateInquiry` | `updateInquiry(id, userId, dto)` | 작성자 본인(서비스에서 확인) |
| DELETE | `/inquiry/{inquiryId}` | `deleteInquiry` | `deleteInquiry(id, userId, isAdmin)` | 본인 또는 ADMIN |
| POST | `/inquiry/{inquiryId}/answer` | `answerInquiry` | `answerInquiry` | 관리자(checkAdminRole) |
| PUT | `/inquiry/{inquiryId}/status?status=` | `updateInquiryStatus` | `updateInquiryStatus` | 관리자 |
| GET | `/inquiry/{inquiryId}` | `getInquiry` | `getInquiry(id, userId, isAdmin)` | 비공개면 본인 또는 관리자 |
| GET | `/inquiry/my` | `getMyInquiries` | `getUserInquiries` | 로그인 |
| GET | `/inquiry?status=&category=&keyword=` | `getInquiries` | `getInquiries(condition{isAdmin=true})` | 관리자 |
| GET | `/inquiry/status/{status}` | `getInquiriesByStatus` | `getInquiriesByStatus` | 관리자 |
| GET | `/inquiry/category/{category}` | `getInquiriesByCategory` | `getInquiriesByCategory`(isAdmin=false, 공개 문의만) | 로그인 |
| GET | `/inquiry/search?keyword=` | `searchInquiries` | `searchInquiries` → `findByTitleOrContentContaining` | 로그인 |
| GET | `/inquiry/pending` | `getPendingInquiries` | `getPendingInquiries` | 관리자 |
| GET | `/inquiry/statistics` | `getInquiryStatistics` | `getInquiryStatistics` | 관리자 |
| GET | `/inquiry/files/{inquiryId}/{fileName}` | `downloadInquiryFile` | 권한 확인용으로 `getInquiry` 호출 후 파일 반환 | 비공개면 본인 또는 관리자 |

### 엔티티 / enum
- `Inquiry`: title(100), content(TEXT, HTML), answer(TEXT), user(ManyToOne, NOT NULL), status(기본 `REQUESTED`), category, isPrivate, answeredBy, pushNotification. `getFileDirectory()` → `"inquiry/{id}"`, `getImageUrlPath()` → `"/api/inquiry/files/{id}/{fileName}"`
- `InquiryImage`: imageUrl(파일명), originalFileName, fileSize, isAnswer(답변 이미지 여부)
- `InquiryStatus`: `REQUESTED → CONFIRMED → ANSWERED`
- `InquiryCategory`: `PRODUCT, ORDER, DELIVERY, RETURN, ACCOUNT, ETC`

### 비즈니스 규칙
- 수정은 작성자만 할 수 있고, `ANSWERED` 상태에서는 수정할 수 없다(`INQUIRY_ALREADY_ANSWERED`).
- `Inquiry.setAnswer(answer, answeredBy)`를 호출하면 status가 자동으로 `ANSWERED`가 된다. answeredBy는 `InquiryAnswerRequestDto.getAnsweredBy()`에서 받는다.
- 관리자 여부는 컨트롤러가 `user.getRole().name().equals("ADMIN")`으로 판단해서 서비스에 넘긴다.
- 파일: `FileUtils.saveFile("inquiry/{id}", IMAGE_PREFIX, file)` → `/home/ubuntu/fream/inquiry/{id}/`. 다운로드 경로(`BASE_DIR + "inquiry/" + id`)와 일치한다. HTML 치환은 Jsoup으로 하며 FAQ와 같은 패턴이다.

### 캐시 / 스케줄러 / ES / AOP
- 모두 없다.

### 다른 도메인 의존
- `user`(`UserQueryService`, `User`, `UserRepository`), `global/utils/SecurityUtils`, `FileUtils`.

### gotchas
- `GET /inquiry/search`에는 관리자 체크가 없고, `findByTitleOrContentContaining`은 `isPrivate` 필터 없이 title/content만으로 검색한다. 비공개 문의가 검색 결과에 포함될 수 있다.
- `pushNotification`은 저장만 되고, inquiry 서비스에서 알림을 보내는 코드는 없다.
- 조회 실패 대부분이 `INQUIRY_NOT_FOUND` 코드로 감싸진다(실제 원인이 DB 오류여도 마찬가지다).

---

## 4. inspection (검수 기준)

**역할**: 카테고리별 검수 기준(HTML + 이미지)을 관리자가 CRUD하고, 사용자가 조회한다.

### 파일 맵
| 레이어 | 파일 |
|---|---|
| controller | `inspection/controller/command/InspectionCommandController.java`, `inspection/controller/query/InspectionQueryController.java` |
| service | `inspection/service/command/InspectionStandardCommandService.java`, `inspection/service/query/InspectionStandardQueryService.java`, `inspection/service/command/InspectionFileStorageUtil.java`(**미사용**) |
| repository | `InspectionStandardRepository`, `InspectionStandardRepositoryCustom`, `InspectionStandardRepositoryImpl`(QueryDSL), `InspectionStandardImageRepository` |
| entity | `InspectionStandard.java`(category, content `@Lob`), `InspectionStandardImage.java`, `InspectionCategory.java` |
| dto | `InspectionStandardCreateRequestDto`(category, content, files), `InspectionStandardUpdateRequestDto`(category, content, existingImageUrls, newFiles), `InspectionStandardResponseDto` |
| exception | `inspection/exception/InspectionErrorCode.java`(INS001~INS302) 외 4개 |

### REST 엔드포인트 (base `/inspections`, 응답은 `ResponseDto`)
| HTTP | 경로 | Controller | Service | 관리자 |
|---|---|---|---|---|
| POST | `/inspections` | `createStandard` | `createStandard` | O |
| PUT | `/inspections/{id}` | `updateStandard` | `updateStandard` | O |
| DELETE | `/inspections/{id}` | `deleteStandard` | `deleteStandard` | O |
| GET | `/inspections?category=&keyword=` | `getStandards` | keyword가 있으면 `searchStandards`, category가 있으면 `getStandardsByCategory`, 둘 다 없으면 `getStandards` | - |
| GET | `/inspections/{id}` | `getStandard` | `getStandard` | - |
| GET | `/inspections/files/{inspectionId}/{fileName}` | `getInspectionFile` | (컨트롤러가 직접 읽음) | - |

- `InspectionCategory`: `SHOES, OUTER, BAG, TECH, BEAUTY, PREMIUM_WATCH, PREMIUM_BAG`

### 파일
- 저장: `FileUtils.saveFile("inspection_{id}", "img_", file)` → `/home/ubuntu/fream/inspection_{id}/`. HTML `<img src>`를 `https://www.pinjun.xyz/api/inspections/files/{id}/{fileName}`으로 치환한다. 이미지 엔티티의 imageUrl은 `"inspection_{id}/파일명"`이다.
- 다운로드: `INSPECTION_BASE_DIR = "/home/ubuntu/fream/inspection"` + `inspection_{id}` → **notice와 같은 저장/다운로드 경로 불일치가 있다**.

### 캐시
- `InspectionStandardQueryService`에 `@Cacheable`: `inspectionStandards`, `inspectionStandardsByCategory`, `inspectionStandardSearchResults`, `inspectionStandardDetail`(모두 CacheConfig에 등록됨).
- **gotcha**: `InspectionStandardCommandService`에는 `@CacheEvict`가 **없다**. 생성·수정·삭제 후에도 캐시된 목록과 상세가 갱신되지 않는다(ConcurrentMap이고 TTL도 없음).

### 다른 도메인 의존 / AOP
- `user`(checkAdminRole), FileUtils. aop, 스케줄러, ES 없음.

---

## 5. event (브랜드 이벤트)

**역할**: 브랜드에 연결된 이벤트(썸네일과 상세 이미지 `SimpleImage`)를 관리자가 CRUD한다. 기간과 상태로 조회한다.

### 파일 맵
| 레이어 | 파일 |
|---|---|
| controller | `event/controller/command/EventCommandController.java`, `event/controller/query/EventQueryController.java` |
| service | `event/service/command/EventCommandService.java`, `SimpleImageCommandService.java`, `event/service/query/EventQueryService.java`, `SimpleImageQueryService.java` |
| repository | `EventRepository`(JPQL `@Query` 다수), `EventRepositoryCustom`, `EventRepositoryImpl`(QueryDSL `searchEvents`), `SimpleImageRepository`, `EventSearchRepository`(**아래 참고**) |
| entity | `Event.java`, `SimpleImage.java`, `EventStatus.java` |
| dto | `CreateEventRequest`, `UpdateEventRequest`, `EventListDto`, `EventDetailDto`, `EventSearchCondition` |
| scheduler | `event/scheduler/EventStatusScheduler.java` |
| exception | `event/exception/EventErrorCode.java`(EVENT-001~EVENT-010), `EventException`, `EventNotFoundException`, `ImageNotFoundException`, `InvalidEventDateException` |
| aop | `event/aop/EventValidationAspect`(1), `EventFileManagementAspect`(2), `EventMonitoringAspect`(3), `EventCachingAspect`(4), `EventBusinessRuleAspect`(5) + `event/aop/annotation/*` (괄호는 @Order) |

### REST 엔드포인트 (base `/events`, 응답은 ResponseDto로 감싸지 않음)
| HTTP | 경로 | Controller | Service | 관리자 |
|---|---|---|---|---|
| POST | `/events` (`@ModelAttribute CreateEventRequest` + `thumbnailFile`, `simpleImageFiles`) | `createEvent` | `EventCommandService.createEvent` | O |
| PATCH | `/events/{eventId}` (+ `keepImageFileNames`) | `updateEvent` | `updateEvent` | O |
| DELETE | `/events/{eventId}` | `deleteEvent` | `deleteEvent` | O |
| PATCH | `/events/{eventId}/status?status=` | `updateEventStatus` | `updateEventStatus` | O |
| GET | `/events/page` | `getAllEventsPaging` | `findAllEventsWithPaging` | - |
| GET | `/events/search?keyword&brandId&isActive&status&startDate&endDate` | `searchEvents` | `searchEvents` → `EventRepositoryImpl.searchEvents` | - |
| GET | `/events/active` | `getActiveEvents` | `findActiveEventsWithPaging` | - |
| GET | `/events/status/{status}` | `getEventsByStatus` | `findEventsByStatus` | - |
| GET | `/events` | `getAllEvents` | `findAllEvents` | - |
| GET | `/events/brands/{brandId}` | `getEventsByBrand` | `findEventsByBrandId` | - |
| GET | `/events/{eventId}` | `getEventDetail` | `findEventById` | - |
| GET | `/events/{eventId}/images/{fileName}` | `getEventImage` | (컨트롤러가 `fileUtils.existsFile("event/{id}", fileName)` 확인 후 반환) | - |

### 엔티티 / 상태
- `Event`(BaseTimeEntity): title, description(`@Lob`), startDate, endDate, thumbnailFileName, simpleImages(OneToMany cascade ALL, orphanRemoval), brand(ManyToOne `product.entity.Brand`).
- **`status`는 `@Transient`이다.** `getStatus()`가 날짜로 계산한다: startDate > now → `UPCOMING`, start < now < end → `ACTIVE`, 그 밖에는 `ENDED`. DB 컬럼이 아니다.
- `updateEventStatus`는 상태를 저장하지 않고 **날짜를 덮어쓴다**: UPCOMING은 start=now+1d, end=start+7d. ACTIVE는 now-1d ~ now+7d. ENDED는 now-14d ~ now-1d.
- 날짜 검증(`validateEventDates`): 둘 다 필수, start ≤ end, 기간은 최소 1시간.

### 파일
- 디렉토리 `event/{eventId}`(`/home/ubuntu/fream/event/{id}/`). 썸네일 prefix는 `thumbnail_{id}_`. SimpleImage는 `SimpleImageCommandService`가 저장한다. 수정할 때 `keepImageFileNames`에 없는 SimpleImage는 삭제한다.

### 스케줄러
- `EventStatusScheduler.checkEventStatus()`: `@Scheduled(cron = "0 0 * * * *")`(매시 정각), `@Transactional`. 클래스에 `@EnableScheduling`이 붙어 있다.
- 실제로는 **로그만 남긴다**(상태가 Transient이므로 변경할 대상이 없다). `findByStartDateBeforeAndEndDateAfter`로 조회한 뒤 `isUpcoming()`을 검사하므로 사실상 카운트는 항상 0이다.

### 캐시
- `EventQueryService`: `findEventById` → `@Cacheable("eventDetails")`, `findActiveEvents` → `@Cacheable("activeEvents")`(컨트롤러 미사용), `findEventsByBrandId` → `@Cacheable("brandEvents")`.
- **gotcha**: 이 세 캐시 이름은 `CacheConfig`의 고정 목록에도, `accessLogCacheManager`에도 없다. ConcurrentMapCacheManager에 cacheNames를 지정하면 동적 생성이 꺼지므로 "Cannot find cache" 오류가 날 가능성이 높다(런타임 동작은 미확인). 또 `@CacheEvict`가 없어서 수정·삭제 후 상세 캐시가 남는다.

### Elasticsearch
- 이 도메인에는 **ES 사용이 없다**. `event/repository/EventSearchRepository`는 이름과 달리 **QueryDSL(JPAQueryFactory) 기반** `@Repository`이고, **어디서도 주입되지 않는다**. 실제 검색은 `EventRepositoryImpl.searchEvents`가 한다.

### AOP (event/aop)
**`@EventValidation`, `@EventFileManagement`, `@EventMonitoring`, `@EventCaching`, `@EventBusinessRule` 모두 main 코드 어디에도 붙어 있지 않다. 따라서 `@Around("@annotation(...)")` advice는 실행되지 않는다.**

| Aspect | 의도(어노테이션 속성 기준) |
|---|---|
| `EventValidationAspect` | 기간(min/max), 과거 날짜, 브랜드 상태, 제목 중복, 상태 전이(`ALLOWED_TRANSITIONS`), 관리자 권한 |
| `EventFileManagementAspect` | 확장자/크기/개수, 리사이즈, WebP, 롤백, 백업(FileUtils 사용) |
| `EventMonitoringAspect` | 메서드/브랜드/사용자 메트릭, 라이프사이클, `scheduleAtFixedRate` 리포트(어노테이션 호출 시에만 스케줄됨) |
| `EventCachingAspect` | CacheManager 기반 캐시, 워밍업(`warmUpScheduler`), 상태 변경 시 무효화 |
| `EventBusinessRuleAspect` | 브랜드당 활성 이벤트 수, 생성 쿨다운, 수정/삭제 가능 기간, 아카이브 |

### 다른 도메인 의존
- `product`(`BrandQueryService`, `Brand`, `BrandNotFoundException`, QueryDSL `QBrand`), `user`(checkAdminRole), FileUtils. 다른 도메인이 event를 쓰는 곳은 `GlobalExceptionHandler`뿐이다.

### gotchas
- `EventCommandService`는 실패를 `RuntimeException("... 중 오류가 발생했습니다")`으로 감싸는 곳이 많다. 이 예외는 `EventException`이 아니므로 `GlobalExceptionHandler`의 `Exception` 핸들러로 간다.

---

## 6. 공통 패턴 (shared patterns)

| 패턴 | 사용 도메인 | 설명 |
|---|---|---|
| Command/Query 분리 | 전부 | `controller/command`, `controller/query`, `service/command`, `service/query` |
| 컨트롤러에서 관리자 체크 | 전부 | `userQueryService.checkAdminRole(email)`. Spring Security 역할 설정이나 `@PreAuthorize`는 쓰지 않는다 |
| HTML 본문 + 이미지 | notice, faq, inquiry, inspection | ① 엔티티 저장으로 id 확보 → ② `FileUtils.saveFile(dir, prefix, file)` → ③ 본문 `<img src>`를 API URL로 치환(notice/inspection은 정규식과 절대 URL `https://www.pinjun.xyz/api/...`, faq/inquiry는 Jsoup과 상대 URL `/api/...`) → ④ `*Image` 엔티티 저장. 수정 시 유지 목록(`existingImageUrls`/`retainedImageUrls`/본문 src)에 없는 이미지는 삭제한다 |
| 파일 다운로드 | notice, faq, inquiry, inspection, event | `GET .../files/{id}/{fileName}`(event는 `/{id}/images/{fileName}`). path normalize 후 base dir 밖이면 거부한다 |
| URL `/api` prefix | notice, inspection, faq, inquiry | 본문에 저장되는 URL에는 `/api/...`가 붙지만, 컨트롤러 매핑은 `/notices`, `/inspections`, `/inquiry`처럼 `/api`가 없다(FAQ만 컨트롤러 자체가 `/api/faq`). `/api`는 nginx 프록시가 붙이는 것으로 보이지만 확정하지 못했다(미확인) |
| `*FileStorageUtil` | notice, faq, inspection | 세 클래스 모두 정의만 있고 **미사용**이다. 실제로는 `global/utils/FileUtils`를 쓴다 |
| `NginxCachePurgeUtil` | **이 도메인들에서는 사용 안 함** | product / style 쪽만 사용한다(`ProductColorCommandService`, `ProductColorCommandController`, `StyleCommandController`) |
| 미적용 커스텀 AOP | faq, event (그리고 chatQuestion) | 어노테이션과 Aspect는 있지만 적용된 메서드가 없다. 기능을 추가할 때 "Aspect가 이미 처리해 준다"고 가정하면 안 된다 |
| 경로 하드코딩 | 전부 | `/home/ubuntu/fream...`. Windows 경로는 주석으로 남아 있다. 환경을 바꾸려면 `FileUtils.BASE_DIR`와 각 컨트롤러/서비스의 상수를 함께 고쳐야 한다 |

## 7. 자주 할 작업 → 여기를 보라

| 작업 | 위치 |
|---|---|
| 공지 등록 시 알림 내용/카테고리 변경 | `notice/service/command/NoticeCommandService.sendNotificationToAllUsers` |
| 공지/검수 이미지 다운로드 404 디버깅 | 저장 `FileUtils.BASE_DIR` + `notice_{id}`, 읽기 `NoticeQueryService.NOTICE_BASE_DIR` / `InspectionQueryController.INSPECTION_BASE_DIR` (경로 불일치) |
| 허용 파일 확장자 변경 | notice: `NoticeCommandService.ALLOWED_*_EXTENSIONS`. 공통 MIME 체크: `FileUtils.saveFile` |
| FAQ 카테고리 추가 | `faq/entity/FAQCategory.java`. 챗봇 프롬프트에도 그대로 반영된다(`GPTService.formatFAQData`) |
| FAQ 캐시 문제 | `FAQQueryService` / `FAQCommandService`의 `@Cacheable`/`@CacheEvict`, `global/config/CacheConfig` |
| 검수 기준 변경이 반영되지 않음 | `InspectionStandardCommandService`에 `@CacheEvict` 추가가 필요하다 |
| 새 캐시 이름 추가 | `global/config/CacheConfig`의 목록에 **반드시** 추가한다(동적 생성 안 됨) |
| 문의 상태/답변 로직 | `inquiry/entity/Inquiry.setAnswer/updateStatus`, `InquiryCommandServiceImpl` |
| 비공개 문의 노출 규칙 | `InquiryQueryServiceImpl.getInquiry`, `InquiryRepositoryImpl`(검색 조건의 isAdmin 처리) |
| 이벤트 상태 의미 변경 | `event/entity/Event.getStatus()`(Transient), `EventCommandService.updateEventStatus`, `EventStatusScheduler` |
| 이벤트 검색 조건 추가 | `event/dto/EventSearchCondition` + `event/repository/EventRepositoryImpl.searchEvents` (`EventSearchRepository`는 미사용) |
| 에러 코드 추가 | `<domain>/exception/<Domain>ErrorCode.java` + `global/exception/GlobalExceptionHandler` |
