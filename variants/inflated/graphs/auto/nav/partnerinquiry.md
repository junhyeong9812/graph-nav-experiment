# partnerinquiry 도메인

기준 경로: `src/main/java/com/fream/back/domain/partnerinquiry/` · 파일 10개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/partner/inquiry` | `PartnerInquiryCommandController#createInquiry` | `PartnerUserQueryService#findByEmail`, `PartnerInquiryCommandService#createInquiry` |
| PUT | `/partner/inquiry/{inquiryId}` | `PartnerInquiryCommandController#updateInquiry` | `PartnerUserQueryService#findByEmail`, `PartnerInquiryCommandService#updateInquiry` |
| DELETE | `/partner/inquiry/{inquiryId}` | `PartnerInquiryCommandController#deleteInquiry` | `PartnerUserQueryService#findByEmail`, `PartnerInquiryCommandService#deleteInquiry` |
| POST | `/partner/inquiry/{inquiryId}/answer` | `PartnerInquiryCommandController#answerInquiry` | `PartnerUserQueryService#checkAdminRole`, `PartnerInquiryCommandService#answerInquiry` |
| PUT | `/partner/inquiry/{inquiryId}/status` | `PartnerInquiryCommandController#updateInquiryStatus` | `PartnerUserQueryService#checkAdminRole`, `PartnerInquiryCommandService#updateInquiryStatus` |
| GET | `/partner/inquiry/{inquiryId}` | `PartnerInquiryQueryController#getInquiry` | `PartnerUserQueryService#findByEmail`, `PartnerInquiryQueryService#getInquiry` |
| GET | `/partner/inquiry/my` | `PartnerInquiryQueryController#getMyInquiries` | `PartnerUserQueryService#findByEmail`, `PartnerInquiryQueryService#getUserInquiries` |
| GET | `/partner/inquiry` | `PartnerInquiryQueryController#getInquiries` | `PartnerUserQueryService#checkAdminRole`, `PartnerInquiryQueryService#getInquiries` |
| GET | `/partner/inquiry/status/{status}` | `PartnerInquiryQueryController#getInquiriesByStatus` | `PartnerUserQueryService#checkAdminRole`, `PartnerInquiryQueryService#getInquiriesByStatus` |
| GET | `/partner/inquiry/category/{category}` | `PartnerInquiryQueryController#getInquiriesByCategory` | `PartnerInquiryQueryService#getInquiriesByCategory` |
| GET | `/partner/inquiry/search` | `PartnerInquiryQueryController#searchInquiries` | `PartnerInquiryQueryService#searchInquiries` |
| GET | `/partner/inquiry/pending` | `PartnerInquiryQueryController#getPendingInquiries` | `PartnerUserQueryService#checkAdminRole`, `PartnerInquiryQueryService#getPendingInquiries` |
| GET | `/partner/inquiry/statistics` | `PartnerInquiryQueryController#getInquiryStatistics` | `PartnerUserQueryService#checkAdminRole`, `PartnerInquiryQueryService#getInquiryStatistics` |
| GET | `/partner/inquiry/files/{inquiryId}/{fileName}` | `PartnerInquiryQueryController#downloadInquiryFile` | `PartnerUserQueryService#findByEmail`, `PartnerInquiryQueryService#getInquiry` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `PartnerInquiryCommandServiceImpl`
  - `createInquiry` → UserRepository#findById~, PartnerInquiryRepository#save~, this#processImagesAndUpdateHtml, PartnerInquiryImageRepository#findAllByInquiry_Id
  - `updateInquiry` → PartnerInquiryRepository#findById~, PartnerInquiryImageRepository#findAllByInquiry_IdAndIsAnswerFalse, this#deleteImages, this#processImagesAndUpdateHtml, PartnerInquiryImageRepository#findAllByInquiry_Id
  - `deleteInquiry` → PartnerInquiryRepository#findById~, PartnerInquiryImageRepository#findAllByInquiry_Id, this#deleteImages, FileUtils#deleteDirectory, PartnerInquiryRepository#delete~
  - `updateInquiryStatus` → PartnerInquiryRepository#findById~, PartnerInquiryImageRepository#findAllByInquiry_Id
  - `answerInquiry` → PartnerInquiryRepository#findById~, this#processImagesAndUpdateHtml, PartnerInquiryImageRepository#findAllByInquiry_Id
  - `processImagesAndUpdateHtml` → FileUtils#saveFile, PartnerInquiryImageRepository#save~, this#updateHtmlImageSources
  - `deleteImages` → FileUtils#deleteFile, PartnerInquiryImageRepository#deleteAll~
- `PartnerInquiryQueryServiceImpl`
  - `getInquiry` → PartnerInquiryRepository#findById~, PartnerInquiryImageRepository#findAllByInquiry_Id
  - `getInquiries` → PartnerInquiryRepository#searchInquiries
  - `getUserInquiries` → UserRepository#findById~, PartnerInquiryRepository#searchInquiries
  - `getInquiriesByStatus` → PartnerInquiryRepository#searchInquiries
  - `getInquiriesByCategory` → PartnerInquiryRepository#searchInquiries
  - `searchInquiries` → PartnerInquiryRepository#findByTitleOrContentContaining
  - `getPendingInquiries` → PartnerInquiryRepository#findUnansweredInquiriesOrderByOldest
  - `getInquiryStatistics` → PartnerInquiryRepository#getInquiryStatistics

## 3. 클래스 목록 (역할별)

### controller (2)

- `PartnerInquiryCommandController` controller/command/PartnerInquiryCommandController.java · public: createInquiry, updateInquiry, deleteInquiry, answerInquiry, updateInquiryStatus · 주입: PartnerInquiryCommandService, PartnerUserQueryService
- `PartnerInquiryQueryController` controller/query/PartnerInquiryQueryController.java · public: getInquiry, getMyInquiries, getInquiries, getInquiriesByStatus, getInquiriesByCategory, searchInquiries, getPendingInquiries, getInquiryStatistics, downloadInquiryFile · 주입: PartnerInquiryQueryService, PartnerUserQueryService, FileUtils

### service-command (2)

- `PartnerInquiryCommandService` (interface) service/command/PartnerInquiryCommandService.java · public: createInquiry, updateInquiry, deleteInquiry, updateInquiryStatus, answerInquiry
- `PartnerInquiryCommandServiceImpl` service/command/PartnerInquiryCommandServiceImpl.java · extends/implements PartnerInquiryCommandService · public: createInquiry, updateInquiry, deleteInquiry, updateInquiryStatus, answerInquiry · 주입: PartnerInquiryRepository, PartnerInquiryImageRepository, UserRepository, FileUtils

### service-query (2)

- `PartnerInquiryQueryService` (interface) service/query/PartnerInquiryQueryService.java · public: getInquiry, getInquiries, getUserInquiries, getInquiriesByStatus, getInquiriesByCategory, searchInquiries, getPendingInquiries, getInquiryStatistics
- `PartnerInquiryQueryServiceImpl` service/query/PartnerInquiryQueryServiceImpl.java · extends/implements PartnerInquiryQueryService · public: getInquiry, getInquiries, getUserInquiries, getInquiriesByStatus, getInquiriesByCategory, searchInquiries, getPendingInquiries, getInquiryStatistics · 주입: PartnerInquiryRepository, PartnerInquiryImageRepository, UserRepository

### repository (2)

- `PartnerInquiryImageRepository` (interface) repository/PartnerInquiryImageRepository.java · extends/implements JpaRepository · public: findAllByInquiry_Id, findAllByInquiry_IdAndIsAnswerFalse, findAllByInquiry_IdAndIsAnswerTrue, deleteAllByInquiry_Id, countByInquiry_Id, findByInquiry_IdAndImageUrlContaining
- `PartnerInquiryRepository` (interface) repository/PartnerInquiryRepository.java · extends/implements JpaRepository, PartnerInquiryRepositoryCustom · public: findByUserIdOrderByCreatedDateDesc, findByStatus, findByCategory, findByCreatedDateBetween, findByTitleContaining, findByUserIdAndStatus, findAllUnansweredInquiries, countByUserId, countByStatus, countByCreatedDateBetween

### custom (2)

- `PartnerInquiryRepositoryCustom` (interface) repository/PartnerInquiryRepositoryCustom.java · public: searchInquiries, findInquiryWithUserDetails, findUnansweredInquiriesOrderByOldest, findByTitleOrContentContaining, getInquiryStatistics
- `PartnerInquiryRepositoryImpl` repository/PartnerInquiryRepositoryImpl.java · extends/implements PartnerInquiryRepositoryCustom · public: searchInquiries, findInquiryWithUserDetails, findUnansweredInquiriesOrderByOldest, findByTitleOrContentContaining, getInquiryStatistics · 주입: JPAQueryFactory

### 다른 도메인 참조 (import·주입 기준)

- **inquiry**: `Inquiry` ← PartnerInquiryCommandServiceImpl, PartnerInquiryQueryServiceImpl, PartnerInquiryRepository; `InquiryCategory` ← PartnerInquiryQueryController, PartnerInquiryQueryService, PartnerInquiryQueryServiceImpl, PartnerInquiryRepository, PartnerInquiryRepositoryImpl; `InquiryImage` ← PartnerInquiryCommandServiceImpl, PartnerInquiryImageRepository, PartnerInquiryQueryServiceImpl; `InquiryStatus` ← PartnerInquiryCommandController, PartnerInquiryCommandService, PartnerInquiryCommandServiceImpl, PartnerInquiryQueryController, PartnerInquiryQueryService, PartnerInquiryQueryServiceImpl, PartnerInquiryRepository, PartnerInquiryRepositoryImpl
- **partneruser**: `PartnerUserQueryService` ← PartnerInquiryCommandController, PartnerInquiryQueryController
- **user**: `User` ← PartnerInquiryCommandController, PartnerInquiryCommandServiceImpl, PartnerInquiryQueryController, PartnerInquiryQueryServiceImpl; `UserRepository` ← PartnerInquiryCommandServiceImpl, PartnerInquiryQueryServiceImpl

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

