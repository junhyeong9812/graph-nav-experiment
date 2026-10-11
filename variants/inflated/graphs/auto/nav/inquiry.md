# inquiry 도메인

기준 경로: `src/main/java/com/fream/back/domain/inquiry/` · 파일 24개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/inquiry` | `InquiryCommandController#createInquiry` | `UserQueryService#findByEmail`, `InquiryCommandService#createInquiry` |
| PUT | `/inquiry/{inquiryId}` | `InquiryCommandController#updateInquiry` | `UserQueryService#findByEmail`, `InquiryCommandService#updateInquiry` |
| DELETE | `/inquiry/{inquiryId}` | `InquiryCommandController#deleteInquiry` | `UserQueryService#findByEmail`, `InquiryCommandService#deleteInquiry` |
| POST | `/inquiry/{inquiryId}/answer` | `InquiryCommandController#answerInquiry` | `UserQueryService#checkAdminRole`, `InquiryCommandService#answerInquiry` |
| PUT | `/inquiry/{inquiryId}/status` | `InquiryCommandController#updateInquiryStatus` | `UserQueryService#checkAdminRole`, `InquiryCommandService#updateInquiryStatus` |
| GET | `/inquiry/{inquiryId}` | `InquiryQueryController#getInquiry` | `UserQueryService#findByEmail`, `InquiryQueryService#getInquiry` |
| GET | `/inquiry/my` | `InquiryQueryController#getMyInquiries` | `UserQueryService#findByEmail`, `InquiryQueryService#getUserInquiries` |
| GET | `/inquiry` | `InquiryQueryController#getInquiries` | `UserQueryService#checkAdminRole`, `InquiryQueryService#getInquiries` |
| GET | `/inquiry/status/{status}` | `InquiryQueryController#getInquiriesByStatus` | `UserQueryService#checkAdminRole`, `InquiryQueryService#getInquiriesByStatus` |
| GET | `/inquiry/category/{category}` | `InquiryQueryController#getInquiriesByCategory` | `InquiryQueryService#getInquiriesByCategory` |
| GET | `/inquiry/search` | `InquiryQueryController#searchInquiries` | `InquiryQueryService#searchInquiries` |
| GET | `/inquiry/pending` | `InquiryQueryController#getPendingInquiries` | `UserQueryService#checkAdminRole`, `InquiryQueryService#getPendingInquiries` |
| GET | `/inquiry/statistics` | `InquiryQueryController#getInquiryStatistics` | `UserQueryService#checkAdminRole`, `InquiryQueryService#getInquiryStatistics` |
| GET | `/inquiry/files/{inquiryId}/{fileName}` | `InquiryQueryController#downloadInquiryFile` | `UserQueryService#findByEmail`, `InquiryQueryService#getInquiry` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `InquiryCommandServiceImpl`
  - `createInquiry` → UserRepository#findById~, InquiryRepository#save~, this#processImagesAndUpdateHtml, InquiryImageRepository#findAllByInquiry_Id
  - `updateInquiry` → InquiryRepository#findById~, InquiryImageRepository#findAllByInquiry_IdAndIsAnswerFalse, this#deleteImages, this#processImagesAndUpdateHtml, InquiryImageRepository#findAllByInquiry_Id
  - `deleteInquiry` → InquiryRepository#findById~, InquiryImageRepository#findAllByInquiry_Id, this#deleteImages, FileUtils#deleteDirectory, InquiryRepository#delete~
  - `updateInquiryStatus` → InquiryRepository#findById~, InquiryImageRepository#findAllByInquiry_Id
  - `answerInquiry` → InquiryRepository#findById~, this#processImagesAndUpdateHtml, InquiryImageRepository#findAllByInquiry_Id
  - `processImagesAndUpdateHtml` → FileUtils#saveFile, InquiryImageRepository#save~, this#updateHtmlImageSources
  - `deleteImages` → FileUtils#deleteFile, InquiryImageRepository#deleteAll~
- `InquiryQueryServiceImpl`
  - `getInquiry` → InquiryRepository#findById~, InquiryImageRepository#findAllByInquiry_Id
  - `getInquiries` → InquiryRepository#searchInquiries
  - `getUserInquiries` → UserRepository#findById~, InquiryRepository#searchInquiries
  - `getInquiriesByStatus` → InquiryRepository#searchInquiries
  - `getInquiriesByCategory` → InquiryRepository#searchInquiries
  - `searchInquiries` → InquiryRepository#findByTitleOrContentContaining
  - `getPendingInquiries` → InquiryRepository#findUnansweredInquiriesOrderByOldest
  - `getInquiryStatistics` → InquiryRepository#getInquiryStatistics

## 3. 클래스 목록 (역할별)

### controller (2)

- `InquiryCommandController` controller/command/InquiryCommandController.java · public: createInquiry, updateInquiry, deleteInquiry, answerInquiry, updateInquiryStatus · 주입: InquiryCommandService, UserQueryService
- `InquiryQueryController` controller/query/InquiryQueryController.java · public: getInquiry, getMyInquiries, getInquiries, getInquiriesByStatus, getInquiriesByCategory, searchInquiries, getPendingInquiries, getInquiryStatistics, downloadInquiryFile · 주입: InquiryQueryService, UserQueryService, FileUtils

### service-command (2)

- `InquiryCommandService` (interface) service/command/InquiryCommandService.java · public: createInquiry, updateInquiry, deleteInquiry, updateInquiryStatus, answerInquiry
- `InquiryCommandServiceImpl` service/command/InquiryCommandServiceImpl.java · extends/implements InquiryCommandService · public: createInquiry, updateInquiry, deleteInquiry, updateInquiryStatus, answerInquiry · 주입: InquiryRepository, InquiryImageRepository, UserRepository, FileUtils

### service-query (2)

- `InquiryQueryService` (interface) service/query/InquiryQueryService.java · public: getInquiry, getInquiries, getUserInquiries, getInquiriesByStatus, getInquiriesByCategory, searchInquiries, getPendingInquiries, getInquiryStatistics
- `InquiryQueryServiceImpl` service/query/InquiryQueryServiceImpl.java · extends/implements InquiryQueryService · public: getInquiry, getInquiries, getUserInquiries, getInquiriesByStatus, getInquiriesByCategory, searchInquiries, getPendingInquiries, getInquiryStatistics · 주입: InquiryRepository, InquiryImageRepository, UserRepository

### repository (2)

- `InquiryImageRepository` (interface) repository/InquiryImageRepository.java · extends/implements JpaRepository · public: findAllByInquiry_Id, findAllByInquiry_IdAndIsAnswerFalse, findAllByInquiry_IdAndIsAnswerTrue, deleteAllByInquiry_Id, countByInquiry_Id, findByInquiry_IdAndImageUrlContaining
- `InquiryRepository` (interface) repository/InquiryRepository.java · extends/implements JpaRepository, InquiryRepositoryCustom · public: findByUserIdOrderByCreatedDateDesc, findByStatus, findByCategory, findByCreatedDateBetween, findByTitleContaining, findByUserIdAndStatus, findAllUnansweredInquiries, countByUserId, countByStatus, countByCreatedDateBetween

### custom (2)

- `InquiryRepositoryCustom` (interface) repository/InquiryRepositoryCustom.java · public: searchInquiries, findInquiryWithUserDetails, findUnansweredInquiriesOrderByOldest, findByTitleOrContentContaining, getInquiryStatistics
- `InquiryRepositoryImpl` repository/InquiryRepositoryImpl.java · extends/implements InquiryRepositoryCustom · public: searchInquiries, findInquiryWithUserDetails, findUnansweredInquiriesOrderByOldest, findByTitleOrContentContaining, getInquiryStatistics · 주입: JPAQueryFactory

### entity (4)

- `Inquiry` entity/Inquiry.java · extends/implements BaseTimeEntity · public: setAnswer, updateStatus, updateInquiry, getFileDirectory, getImageUrlPath
- `InquiryCategory` (enum) entity/InquiryCategory.java · public: getDescription
- `InquiryImage` entity/InquiryImage.java · extends/implements BaseTimeEntity · public: setInquiry
- `InquiryStatus` (enum) entity/InquiryStatus.java · public: getDescription

### dto (6)

- `InquiryAnswerRequestDto` dto/InquiryAnswerRequestDto.java
- `InquiryCreateRequestDto` dto/InquiryCreateRequestDto.java
- `InquiryResponseDto` dto/InquiryResponseDto.java
- `InquirySearchCondition` dto/InquirySearchCondition.java
- `InquirySearchResultDto` dto/InquirySearchResultDto.java
- `InquiryUpdateRequestDto` dto/InquiryUpdateRequestDto.java

### exception (4)

- `InquiryErrorCode` exception/InquiryErrorCode.java
- `InquiryException` exception/InquiryException.java
- `InquiryFileException` exception/InquiryFileException.java
- `InquiryNotFoundException` exception/InquiryNotFoundException.java

### 다른 도메인 참조 (import·주입 기준)

- **user**: `User` ← Inquiry, InquiryCommandController, InquiryCommandServiceImpl, InquiryCreateRequestDto, InquiryQueryController, InquiryQueryServiceImpl; `UserQueryService` ← InquiryCommandController, InquiryQueryController; `UserRepository` ← InquiryCommandServiceImpl, InquiryQueryServiceImpl

### 이 도메인을 참조하는 다른 도메인

- **admininquiry**: Inquiry, InquiryCategory, InquiryImage, InquiryStatus
- **internalinquiry**: Inquiry, InquiryCategory, InquiryImage, InquiryStatus
- **legacyinquiry**: Inquiry, InquiryCategory, InquiryImage, InquiryStatus
- **partnerinquiry**: Inquiry, InquiryCategory, InquiryImage, InquiryStatus
- **settlementinquiry**: Inquiry, InquiryCategory, InquiryImage, InquiryStatus

## 4. JPA 엔티티 연관

- `Inquiry` @Entity @Table(name = "inquiries") · extends BaseTimeEntity
  - user: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) → `User` [user]
- `InquiryImage` @Entity @Table(name = "inquiry_images") · extends BaseTimeEntity
  - inquiry: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "inquiry_id", nullable = false) → `Inquiry`

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

