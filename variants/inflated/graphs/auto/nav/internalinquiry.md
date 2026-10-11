# internalinquiry 도메인

기준 경로: `src/main/java/com/fream/back/domain/internalinquiry/` · 파일 10개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/internal/inquiry` | `InternalInquiryCommandController#createInquiry` | `InternalUserQueryService#findByEmail`, `InternalInquiryCommandService#createInquiry` |
| PUT | `/internal/inquiry/{inquiryId}` | `InternalInquiryCommandController#updateInquiry` | `InternalUserQueryService#findByEmail`, `InternalInquiryCommandService#updateInquiry` |
| DELETE | `/internal/inquiry/{inquiryId}` | `InternalInquiryCommandController#deleteInquiry` | `InternalUserQueryService#findByEmail`, `InternalInquiryCommandService#deleteInquiry` |
| POST | `/internal/inquiry/{inquiryId}/answer` | `InternalInquiryCommandController#answerInquiry` | `InternalUserQueryService#checkAdminRole`, `InternalInquiryCommandService#answerInquiry` |
| PUT | `/internal/inquiry/{inquiryId}/status` | `InternalInquiryCommandController#updateInquiryStatus` | `InternalUserQueryService#checkAdminRole`, `InternalInquiryCommandService#updateInquiryStatus` |
| GET | `/internal/inquiry/{inquiryId}` | `InternalInquiryQueryController#getInquiry` | `InternalUserQueryService#findByEmail`, `InternalInquiryQueryService#getInquiry` |
| GET | `/internal/inquiry/my` | `InternalInquiryQueryController#getMyInquiries` | `InternalUserQueryService#findByEmail`, `InternalInquiryQueryService#getUserInquiries` |
| GET | `/internal/inquiry` | `InternalInquiryQueryController#getInquiries` | `InternalUserQueryService#checkAdminRole`, `InternalInquiryQueryService#getInquiries` |
| GET | `/internal/inquiry/status/{status}` | `InternalInquiryQueryController#getInquiriesByStatus` | `InternalUserQueryService#checkAdminRole`, `InternalInquiryQueryService#getInquiriesByStatus` |
| GET | `/internal/inquiry/category/{category}` | `InternalInquiryQueryController#getInquiriesByCategory` | `InternalInquiryQueryService#getInquiriesByCategory` |
| GET | `/internal/inquiry/search` | `InternalInquiryQueryController#searchInquiries` | `InternalInquiryQueryService#searchInquiries` |
| GET | `/internal/inquiry/pending` | `InternalInquiryQueryController#getPendingInquiries` | `InternalUserQueryService#checkAdminRole`, `InternalInquiryQueryService#getPendingInquiries` |
| GET | `/internal/inquiry/statistics` | `InternalInquiryQueryController#getInquiryStatistics` | `InternalUserQueryService#checkAdminRole`, `InternalInquiryQueryService#getInquiryStatistics` |
| GET | `/internal/inquiry/files/{inquiryId}/{fileName}` | `InternalInquiryQueryController#downloadInquiryFile` | `InternalUserQueryService#findByEmail`, `InternalInquiryQueryService#getInquiry` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `InternalInquiryCommandServiceImpl`
  - `createInquiry` → UserRepository#findById~, InternalInquiryRepository#save~, this#processImagesAndUpdateHtml, InternalInquiryImageRepository#findAllByInquiry_Id
  - `updateInquiry` → InternalInquiryRepository#findById~, InternalInquiryImageRepository#findAllByInquiry_IdAndIsAnswerFalse, this#deleteImages, this#processImagesAndUpdateHtml, InternalInquiryImageRepository#findAllByInquiry_Id
  - `deleteInquiry` → InternalInquiryRepository#findById~, InternalInquiryImageRepository#findAllByInquiry_Id, this#deleteImages, FileUtils#deleteDirectory, InternalInquiryRepository#delete~
  - `updateInquiryStatus` → InternalInquiryRepository#findById~, InternalInquiryImageRepository#findAllByInquiry_Id
  - `answerInquiry` → InternalInquiryRepository#findById~, this#processImagesAndUpdateHtml, InternalInquiryImageRepository#findAllByInquiry_Id
  - `processImagesAndUpdateHtml` → FileUtils#saveFile, InternalInquiryImageRepository#save~, this#updateHtmlImageSources
  - `deleteImages` → FileUtils#deleteFile, InternalInquiryImageRepository#deleteAll~
- `InternalInquiryQueryServiceImpl`
  - `getInquiry` → InternalInquiryRepository#findById~, InternalInquiryImageRepository#findAllByInquiry_Id
  - `getInquiries` → InternalInquiryRepository#searchInquiries
  - `getUserInquiries` → UserRepository#findById~, InternalInquiryRepository#searchInquiries
  - `getInquiriesByStatus` → InternalInquiryRepository#searchInquiries
  - `getInquiriesByCategory` → InternalInquiryRepository#searchInquiries
  - `searchInquiries` → InternalInquiryRepository#findByTitleOrContentContaining
  - `getPendingInquiries` → InternalInquiryRepository#findUnansweredInquiriesOrderByOldest
  - `getInquiryStatistics` → InternalInquiryRepository#getInquiryStatistics

## 3. 클래스 목록 (역할별)

### controller (2)

- `InternalInquiryCommandController` controller/command/InternalInquiryCommandController.java · public: createInquiry, updateInquiry, deleteInquiry, answerInquiry, updateInquiryStatus · 주입: InternalInquiryCommandService, InternalUserQueryService
- `InternalInquiryQueryController` controller/query/InternalInquiryQueryController.java · public: getInquiry, getMyInquiries, getInquiries, getInquiriesByStatus, getInquiriesByCategory, searchInquiries, getPendingInquiries, getInquiryStatistics, downloadInquiryFile · 주입: InternalInquiryQueryService, InternalUserQueryService, FileUtils

### service-command (2)

- `InternalInquiryCommandService` (interface) service/command/InternalInquiryCommandService.java · public: createInquiry, updateInquiry, deleteInquiry, updateInquiryStatus, answerInquiry
- `InternalInquiryCommandServiceImpl` service/command/InternalInquiryCommandServiceImpl.java · extends/implements InternalInquiryCommandService · public: createInquiry, updateInquiry, deleteInquiry, updateInquiryStatus, answerInquiry · 주입: InternalInquiryRepository, InternalInquiryImageRepository, UserRepository, FileUtils

### service-query (2)

- `InternalInquiryQueryService` (interface) service/query/InternalInquiryQueryService.java · public: getInquiry, getInquiries, getUserInquiries, getInquiriesByStatus, getInquiriesByCategory, searchInquiries, getPendingInquiries, getInquiryStatistics
- `InternalInquiryQueryServiceImpl` service/query/InternalInquiryQueryServiceImpl.java · extends/implements InternalInquiryQueryService · public: getInquiry, getInquiries, getUserInquiries, getInquiriesByStatus, getInquiriesByCategory, searchInquiries, getPendingInquiries, getInquiryStatistics · 주입: InternalInquiryRepository, InternalInquiryImageRepository, UserRepository

### repository (2)

- `InternalInquiryImageRepository` (interface) repository/InternalInquiryImageRepository.java · extends/implements JpaRepository · public: findAllByInquiry_Id, findAllByInquiry_IdAndIsAnswerFalse, findAllByInquiry_IdAndIsAnswerTrue, deleteAllByInquiry_Id, countByInquiry_Id, findByInquiry_IdAndImageUrlContaining
- `InternalInquiryRepository` (interface) repository/InternalInquiryRepository.java · extends/implements JpaRepository, InternalInquiryRepositoryCustom · public: findByUserIdOrderByCreatedDateDesc, findByStatus, findByCategory, findByCreatedDateBetween, findByTitleContaining, findByUserIdAndStatus, findAllUnansweredInquiries, countByUserId, countByStatus, countByCreatedDateBetween

### custom (2)

- `InternalInquiryRepositoryCustom` (interface) repository/InternalInquiryRepositoryCustom.java · public: searchInquiries, findInquiryWithUserDetails, findUnansweredInquiriesOrderByOldest, findByTitleOrContentContaining, getInquiryStatistics
- `InternalInquiryRepositoryImpl` repository/InternalInquiryRepositoryImpl.java · extends/implements InternalInquiryRepositoryCustom · public: searchInquiries, findInquiryWithUserDetails, findUnansweredInquiriesOrderByOldest, findByTitleOrContentContaining, getInquiryStatistics · 주입: JPAQueryFactory

### 다른 도메인 참조 (import·주입 기준)

- **inquiry**: `Inquiry` ← InternalInquiryCommandServiceImpl, InternalInquiryQueryServiceImpl, InternalInquiryRepository; `InquiryCategory` ← InternalInquiryQueryController, InternalInquiryQueryService, InternalInquiryQueryServiceImpl, InternalInquiryRepository, InternalInquiryRepositoryImpl; `InquiryImage` ← InternalInquiryCommandServiceImpl, InternalInquiryImageRepository, InternalInquiryQueryServiceImpl; `InquiryStatus` ← InternalInquiryCommandController, InternalInquiryCommandService, InternalInquiryCommandServiceImpl, InternalInquiryQueryController, InternalInquiryQueryService, InternalInquiryQueryServiceImpl, InternalInquiryRepository, InternalInquiryRepositoryImpl
- **internaluser**: `InternalUserQueryService` ← InternalInquiryCommandController, InternalInquiryQueryController
- **user**: `User` ← InternalInquiryCommandController, InternalInquiryCommandServiceImpl, InternalInquiryQueryController, InternalInquiryQueryServiceImpl; `UserRepository` ← InternalInquiryCommandServiceImpl, InternalInquiryQueryServiceImpl

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

