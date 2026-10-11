# accessLog 도메인

기준 경로: `src/main/java/com/fream/back/domain/accessLog/` · 파일 24개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/inquiry` | `AccessLogCommandController#createInquiry` | `ProductQueryService#findByEmail`, `AccessLogCommandService#createInquiry` |
| PUT | `/inquiry/{inquiryId}` | `AccessLogCommandController#updateInquiry` | `ProductQueryService#findByEmail`, `AccessLogCommandService#updateInquiry` |
| DELETE | `/inquiry/{inquiryId}` | `AccessLogCommandController#deleteInquiry` | `ProductQueryService#findByEmail`, `AccessLogCommandService#deleteInquiry` |
| POST | `/inquiry/{inquiryId}/answer` | `AccessLogCommandController#answerInquiry` | `ProductQueryService#checkAdminRole`, `AccessLogCommandService#answerInquiry` |
| PUT | `/inquiry/{inquiryId}/status` | `AccessLogCommandController#updateInquiryStatus` | `ProductQueryService#checkAdminRole`, `AccessLogCommandService#updateInquiryStatus` |
| GET | `/inquiry/{inquiryId}` | `AccessLogQueryController#getInquiry` | `ProductQueryService#findByEmail`, `AccessLogQueryService#getInquiry` |
| GET | `/inquiry/my` | `AccessLogQueryController#getMyInquiries` | `ProductQueryService#findByEmail`, `AccessLogQueryService#getUserInquiries` |
| GET | `/inquiry` | `AccessLogQueryController#getInquiries` | `ProductQueryService#checkAdminRole`, `AccessLogQueryService#getInquiries` |
| GET | `/inquiry/status/{status}` | `AccessLogQueryController#getInquiriesByStatus` | `ProductQueryService#checkAdminRole`, `AccessLogQueryService#getInquiriesByStatus` |
| GET | `/inquiry/category/{category}` | `AccessLogQueryController#getInquiriesByCategory` | `AccessLogQueryService#getInquiriesByCategory` |
| GET | `/inquiry/search` | `AccessLogQueryController#searchInquiries` | `AccessLogQueryService#searchInquiries` |
| GET | `/inquiry/pending` | `AccessLogQueryController#getPendingInquiries` | `ProductQueryService#checkAdminRole`, `AccessLogQueryService#getPendingInquiries` |
| GET | `/inquiry/statistics` | `AccessLogQueryController#getInquiryStatistics` | `ProductQueryService#checkAdminRole`, `AccessLogQueryService#getInquiryStatistics` |
| GET | `/inquiry/files/{inquiryId}/{fileName}` | `AccessLogQueryController#downloadInquiryFile` | `ProductQueryService#findByEmail`, `AccessLogQueryService#getInquiry` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `AccessLogCommandServiceImpl`
  - `createInquiry` → ProductRepository#findById~, AccessLogRepository#save~, this#processImagesAndUpdateHtml, AccessLogImageRepository#findAllByInquiry_Id
  - `updateInquiry` → AccessLogRepository#findById~, AccessLogImageRepository#findAllByInquiry_IdAndIsAnswerFalse, this#deleteImages, this#processImagesAndUpdateHtml, AccessLogImageRepository#findAllByInquiry_Id
  - `deleteInquiry` → AccessLogRepository#findById~, AccessLogImageRepository#findAllByInquiry_Id, this#deleteImages, FileUtils#deleteDirectory, AccessLogRepository#delete~
  - `updateInquiryStatus` → AccessLogRepository#findById~, AccessLogImageRepository#findAllByInquiry_Id
  - `answerInquiry` → AccessLogRepository#findById~, this#processImagesAndUpdateHtml, AccessLogImageRepository#findAllByInquiry_Id
  - `processImagesAndUpdateHtml` → FileUtils#saveFile, AccessLogImageRepository#save~, this#updateHtmlImageSources
  - `deleteImages` → FileUtils#deleteFile, AccessLogImageRepository#deleteAll~
- `AccessLogQueryServiceImpl`
  - `getInquiry` → AccessLogRepository#findById~, AccessLogImageRepository#findAllByInquiry_Id
  - `getInquiries` → AccessLogRepository#searchInquiries
  - `getUserInquiries` → ProductRepository#findById~, AccessLogRepository#searchInquiries
  - `getInquiriesByStatus` → AccessLogRepository#searchInquiries
  - `getInquiriesByCategory` → AccessLogRepository#searchInquiries
  - `searchInquiries` → AccessLogRepository#findByTitleOrContentContaining
  - `getPendingInquiries` → AccessLogRepository#findUnansweredInquiriesOrderByOldest
  - `getInquiryStatistics` → AccessLogRepository#getInquiryStatistics

## 3. 클래스 목록 (역할별)

### controller (2)

- `AccessLogCommandController` controller/command/AccessLogCommandController.java · public: createInquiry, updateInquiry, deleteInquiry, answerInquiry, updateInquiryStatus · 주입: AccessLogCommandService, ProductQueryService
- `AccessLogQueryController` controller/query/AccessLogQueryController.java · public: getInquiry, getMyInquiries, getInquiries, getInquiriesByStatus, getInquiriesByCategory, searchInquiries, getPendingInquiries, getInquiryStatistics, downloadInquiryFile · 주입: AccessLogQueryService, ProductQueryService, FileUtils

### service-command (2)

- `AccessLogCommandService` (interface) service/command/AccessLogCommandService.java · public: createInquiry, updateInquiry, deleteInquiry, updateInquiryStatus, answerInquiry
- `AccessLogCommandServiceImpl` service/command/AccessLogCommandServiceImpl.java · extends/implements AccessLogCommandService · public: createInquiry, updateInquiry, deleteInquiry, updateInquiryStatus, answerInquiry · 주입: AccessLogRepository, AccessLogImageRepository, ProductRepository, FileUtils

### service-query (2)

- `AccessLogQueryService` (interface) service/query/AccessLogQueryService.java · public: getInquiry, getInquiries, getUserInquiries, getInquiriesByStatus, getInquiriesByCategory, searchInquiries, getPendingInquiries, getInquiryStatistics
- `AccessLogQueryServiceImpl` service/query/AccessLogQueryServiceImpl.java · extends/implements AccessLogQueryService · public: getInquiry, getInquiries, getUserInquiries, getInquiriesByStatus, getInquiriesByCategory, searchInquiries, getPendingInquiries, getInquiryStatistics · 주입: AccessLogRepository, AccessLogImageRepository, ProductRepository

### repository (2)

- `AccessLogImageRepository` (interface) repository/AccessLogImageRepository.java · extends/implements JpaRepository · public: findAllByInquiry_Id, findAllByInquiry_IdAndIsAnswerFalse, findAllByInquiry_IdAndIsAnswerTrue, deleteAllByInquiry_Id, countByInquiry_Id, findByInquiry_IdAndImageUrlContaining
- `AccessLogRepository` (interface) repository/AccessLogRepository.java · extends/implements JpaRepository, AccessLogRepositoryCustom · public: findByUserIdOrderByCreatedDateDesc, findByStatus, findByCategory, findByCreatedDateBetween, findByTitleContaining, findByUserIdAndStatus, findAllUnansweredInquiries, countByUserId, countByStatus, countByCreatedDateBetween

### custom (2)

- `AccessLogRepositoryCustom` (interface) repository/AccessLogRepositoryCustom.java · public: searchInquiries, findInquiryWithUserDetails, findUnansweredInquiriesOrderByOldest, findByTitleOrContentContaining, getInquiryStatistics
- `AccessLogRepositoryImpl` repository/AccessLogRepositoryImpl.java · extends/implements AccessLogRepositoryCustom · public: searchInquiries, findInquiryWithUserDetails, findUnansweredInquiriesOrderByOldest, findByTitleOrContentContaining, getInquiryStatistics · 주입: JPAQueryFactory

### entity (4)

- `AccessLog` entity/AccessLog.java · extends/implements BaseTimeEntity · public: setAnswer, updateStatus, updateInquiry, getFileDirectory, getImageUrlPath
- `AccessLogCategory` (enum) entity/AccessLogCategory.java · public: getDescription
- `AccessLogImage` entity/AccessLogImage.java · extends/implements BaseTimeEntity · public: setInquiry
- `AccessLogStatus` (enum) entity/AccessLogStatus.java · public: getDescription

### dto (6)

- `AccessLogAnswerRequestDto` dto/AccessLogAnswerRequestDto.java
- `AccessLogCreateRequestDto` dto/AccessLogCreateRequestDto.java
- `AccessLogResponseDto` dto/AccessLogResponseDto.java
- `AccessLogSearchCondition` dto/AccessLogSearchCondition.java
- `AccessLogSearchResultDto` dto/AccessLogSearchResultDto.java
- `AccessLogUpdateRequestDto` dto/AccessLogUpdateRequestDto.java

### exception (4)

- `AccessLogErrorCode` exception/AccessLogErrorCode.java
- `AccessLogException` exception/AccessLogException.java
- `AccessLogFileException` exception/AccessLogFileException.java
- `AccessLogNotFoundException` exception/AccessLogNotFoundException.java

### 다른 도메인 참조 (import·주입 기준)

- **product**: `Product` ← AccessLog, AccessLogCommandController, AccessLogCommandServiceImpl, AccessLogCreateRequestDto, AccessLogQueryController, AccessLogQueryServiceImpl; `ProductQueryService` ← AccessLogCommandController, AccessLogQueryController; `ProductRepository` ← AccessLogCommandServiceImpl, AccessLogQueryServiceImpl

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

- `AccessLog` @Entity @Table(name = "inquiries") · extends BaseTimeEntity
  - user: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) → `Product` [product]
- `AccessLogImage` @Entity @Table(name = "inquiry_images") · extends BaseTimeEntity
  - inquiry: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "inquiry_id", nullable = false) → `AccessLog`

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

