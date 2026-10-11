# admininquiry 도메인

기준 경로: `src/main/java/com/fream/back/domain/admininquiry/` · 파일 10개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/admin/inquiry` | `AdminInquiryCommandController#createInquiry` | `AdminUserQueryService#findByEmail`, `AdminInquiryCommandService#createInquiry` |
| PUT | `/admin/inquiry/{inquiryId}` | `AdminInquiryCommandController#updateInquiry` | `AdminUserQueryService#findByEmail`, `AdminInquiryCommandService#updateInquiry` |
| DELETE | `/admin/inquiry/{inquiryId}` | `AdminInquiryCommandController#deleteInquiry` | `AdminUserQueryService#findByEmail`, `AdminInquiryCommandService#deleteInquiry` |
| POST | `/admin/inquiry/{inquiryId}/answer` | `AdminInquiryCommandController#answerInquiry` | `AdminUserQueryService#checkAdminRole`, `AdminInquiryCommandService#answerInquiry` |
| PUT | `/admin/inquiry/{inquiryId}/status` | `AdminInquiryCommandController#updateInquiryStatus` | `AdminUserQueryService#checkAdminRole`, `AdminInquiryCommandService#updateInquiryStatus` |
| GET | `/admin/inquiry/{inquiryId}` | `AdminInquiryQueryController#getInquiry` | `AdminUserQueryService#findByEmail`, `AdminInquiryQueryService#getInquiry` |
| GET | `/admin/inquiry/my` | `AdminInquiryQueryController#getMyInquiries` | `AdminUserQueryService#findByEmail`, `AdminInquiryQueryService#getUserInquiries` |
| GET | `/admin/inquiry` | `AdminInquiryQueryController#getInquiries` | `AdminUserQueryService#checkAdminRole`, `AdminInquiryQueryService#getInquiries` |
| GET | `/admin/inquiry/status/{status}` | `AdminInquiryQueryController#getInquiriesByStatus` | `AdminUserQueryService#checkAdminRole`, `AdminInquiryQueryService#getInquiriesByStatus` |
| GET | `/admin/inquiry/category/{category}` | `AdminInquiryQueryController#getInquiriesByCategory` | `AdminInquiryQueryService#getInquiriesByCategory` |
| GET | `/admin/inquiry/search` | `AdminInquiryQueryController#searchInquiries` | `AdminInquiryQueryService#searchInquiries` |
| GET | `/admin/inquiry/pending` | `AdminInquiryQueryController#getPendingInquiries` | `AdminUserQueryService#checkAdminRole`, `AdminInquiryQueryService#getPendingInquiries` |
| GET | `/admin/inquiry/statistics` | `AdminInquiryQueryController#getInquiryStatistics` | `AdminUserQueryService#checkAdminRole`, `AdminInquiryQueryService#getInquiryStatistics` |
| GET | `/admin/inquiry/files/{inquiryId}/{fileName}` | `AdminInquiryQueryController#downloadInquiryFile` | `AdminUserQueryService#findByEmail`, `AdminInquiryQueryService#getInquiry` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `AdminInquiryCommandServiceImpl`
  - `createInquiry` → UserRepository#findById~, AdminInquiryRepository#save~, this#processImagesAndUpdateHtml, AdminInquiryImageRepository#findAllByInquiry_Id
  - `updateInquiry` → AdminInquiryRepository#findById~, AdminInquiryImageRepository#findAllByInquiry_IdAndIsAnswerFalse, this#deleteImages, this#processImagesAndUpdateHtml, AdminInquiryImageRepository#findAllByInquiry_Id
  - `deleteInquiry` → AdminInquiryRepository#findById~, AdminInquiryImageRepository#findAllByInquiry_Id, this#deleteImages, FileUtils#deleteDirectory, AdminInquiryRepository#delete~
  - `updateInquiryStatus` → AdminInquiryRepository#findById~, AdminInquiryImageRepository#findAllByInquiry_Id
  - `answerInquiry` → AdminInquiryRepository#findById~, this#processImagesAndUpdateHtml, AdminInquiryImageRepository#findAllByInquiry_Id
  - `processImagesAndUpdateHtml` → FileUtils#saveFile, AdminInquiryImageRepository#save~, this#updateHtmlImageSources
  - `deleteImages` → FileUtils#deleteFile, AdminInquiryImageRepository#deleteAll~
- `AdminInquiryQueryServiceImpl`
  - `getInquiry` → AdminInquiryRepository#findById~, AdminInquiryImageRepository#findAllByInquiry_Id
  - `getInquiries` → AdminInquiryRepository#searchInquiries
  - `getUserInquiries` → UserRepository#findById~, AdminInquiryRepository#searchInquiries
  - `getInquiriesByStatus` → AdminInquiryRepository#searchInquiries
  - `getInquiriesByCategory` → AdminInquiryRepository#searchInquiries
  - `searchInquiries` → AdminInquiryRepository#findByTitleOrContentContaining
  - `getPendingInquiries` → AdminInquiryRepository#findUnansweredInquiriesOrderByOldest
  - `getInquiryStatistics` → AdminInquiryRepository#getInquiryStatistics

## 3. 클래스 목록 (역할별)

### controller (2)

- `AdminInquiryCommandController` controller/command/AdminInquiryCommandController.java · public: createInquiry, updateInquiry, deleteInquiry, answerInquiry, updateInquiryStatus · 주입: AdminInquiryCommandService, AdminUserQueryService
- `AdminInquiryQueryController` controller/query/AdminInquiryQueryController.java · public: getInquiry, getMyInquiries, getInquiries, getInquiriesByStatus, getInquiriesByCategory, searchInquiries, getPendingInquiries, getInquiryStatistics, downloadInquiryFile · 주입: AdminInquiryQueryService, AdminUserQueryService, FileUtils

### service-command (2)

- `AdminInquiryCommandService` (interface) service/command/AdminInquiryCommandService.java · public: createInquiry, updateInquiry, deleteInquiry, updateInquiryStatus, answerInquiry
- `AdminInquiryCommandServiceImpl` service/command/AdminInquiryCommandServiceImpl.java · extends/implements AdminInquiryCommandService · public: createInquiry, updateInquiry, deleteInquiry, updateInquiryStatus, answerInquiry · 주입: AdminInquiryRepository, AdminInquiryImageRepository, UserRepository, FileUtils

### service-query (2)

- `AdminInquiryQueryService` (interface) service/query/AdminInquiryQueryService.java · public: getInquiry, getInquiries, getUserInquiries, getInquiriesByStatus, getInquiriesByCategory, searchInquiries, getPendingInquiries, getInquiryStatistics
- `AdminInquiryQueryServiceImpl` service/query/AdminInquiryQueryServiceImpl.java · extends/implements AdminInquiryQueryService · public: getInquiry, getInquiries, getUserInquiries, getInquiriesByStatus, getInquiriesByCategory, searchInquiries, getPendingInquiries, getInquiryStatistics · 주입: AdminInquiryRepository, AdminInquiryImageRepository, UserRepository

### repository (2)

- `AdminInquiryImageRepository` (interface) repository/AdminInquiryImageRepository.java · extends/implements JpaRepository · public: findAllByInquiry_Id, findAllByInquiry_IdAndIsAnswerFalse, findAllByInquiry_IdAndIsAnswerTrue, deleteAllByInquiry_Id, countByInquiry_Id, findByInquiry_IdAndImageUrlContaining
- `AdminInquiryRepository` (interface) repository/AdminInquiryRepository.java · extends/implements JpaRepository, AdminInquiryRepositoryCustom · public: findByUserIdOrderByCreatedDateDesc, findByStatus, findByCategory, findByCreatedDateBetween, findByTitleContaining, findByUserIdAndStatus, findAllUnansweredInquiries, countByUserId, countByStatus, countByCreatedDateBetween

### custom (2)

- `AdminInquiryRepositoryCustom` (interface) repository/AdminInquiryRepositoryCustom.java · public: searchInquiries, findInquiryWithUserDetails, findUnansweredInquiriesOrderByOldest, findByTitleOrContentContaining, getInquiryStatistics
- `AdminInquiryRepositoryImpl` repository/AdminInquiryRepositoryImpl.java · extends/implements AdminInquiryRepositoryCustom · public: searchInquiries, findInquiryWithUserDetails, findUnansweredInquiriesOrderByOldest, findByTitleOrContentContaining, getInquiryStatistics · 주입: JPAQueryFactory

### 다른 도메인 참조 (import·주입 기준)

- **adminuser**: `AdminUserQueryService` ← AdminInquiryCommandController, AdminInquiryQueryController
- **inquiry**: `Inquiry` ← AdminInquiryCommandServiceImpl, AdminInquiryQueryServiceImpl, AdminInquiryRepository; `InquiryCategory` ← AdminInquiryQueryController, AdminInquiryQueryService, AdminInquiryQueryServiceImpl, AdminInquiryRepository, AdminInquiryRepositoryImpl; `InquiryImage` ← AdminInquiryCommandServiceImpl, AdminInquiryImageRepository, AdminInquiryQueryServiceImpl; `InquiryStatus` ← AdminInquiryCommandController, AdminInquiryCommandService, AdminInquiryCommandServiceImpl, AdminInquiryQueryController, AdminInquiryQueryService, AdminInquiryQueryServiceImpl, AdminInquiryRepository, AdminInquiryRepositoryImpl
- **user**: `User` ← AdminInquiryCommandController, AdminInquiryCommandServiceImpl, AdminInquiryQueryController, AdminInquiryQueryServiceImpl; `UserRepository` ← AdminInquiryCommandServiceImpl, AdminInquiryQueryServiceImpl

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

