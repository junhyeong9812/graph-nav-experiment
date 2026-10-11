# legacyinquiry 도메인

기준 경로: `src/main/java/com/fream/back/domain/legacyinquiry/` · 파일 10개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/legacy/inquiry` | `LegacyInquiryCommandController#createInquiry` | `LegacyUserQueryService#findByEmail`, `LegacyInquiryCommandService#createInquiry` |
| PUT | `/legacy/inquiry/{inquiryId}` | `LegacyInquiryCommandController#updateInquiry` | `LegacyUserQueryService#findByEmail`, `LegacyInquiryCommandService#updateInquiry` |
| DELETE | `/legacy/inquiry/{inquiryId}` | `LegacyInquiryCommandController#deleteInquiry` | `LegacyUserQueryService#findByEmail`, `LegacyInquiryCommandService#deleteInquiry` |
| POST | `/legacy/inquiry/{inquiryId}/answer` | `LegacyInquiryCommandController#answerInquiry` | `LegacyUserQueryService#checkAdminRole`, `LegacyInquiryCommandService#answerInquiry` |
| PUT | `/legacy/inquiry/{inquiryId}/status` | `LegacyInquiryCommandController#updateInquiryStatus` | `LegacyUserQueryService#checkAdminRole`, `LegacyInquiryCommandService#updateInquiryStatus` |
| GET | `/legacy/inquiry/{inquiryId}` | `LegacyInquiryQueryController#getInquiry` | `LegacyUserQueryService#findByEmail`, `LegacyInquiryQueryService#getInquiry` |
| GET | `/legacy/inquiry/my` | `LegacyInquiryQueryController#getMyInquiries` | `LegacyUserQueryService#findByEmail`, `LegacyInquiryQueryService#getUserInquiries` |
| GET | `/legacy/inquiry` | `LegacyInquiryQueryController#getInquiries` | `LegacyUserQueryService#checkAdminRole`, `LegacyInquiryQueryService#getInquiries` |
| GET | `/legacy/inquiry/status/{status}` | `LegacyInquiryQueryController#getInquiriesByStatus` | `LegacyUserQueryService#checkAdminRole`, `LegacyInquiryQueryService#getInquiriesByStatus` |
| GET | `/legacy/inquiry/category/{category}` | `LegacyInquiryQueryController#getInquiriesByCategory` | `LegacyInquiryQueryService#getInquiriesByCategory` |
| GET | `/legacy/inquiry/search` | `LegacyInquiryQueryController#searchInquiries` | `LegacyInquiryQueryService#searchInquiries` |
| GET | `/legacy/inquiry/pending` | `LegacyInquiryQueryController#getPendingInquiries` | `LegacyUserQueryService#checkAdminRole`, `LegacyInquiryQueryService#getPendingInquiries` |
| GET | `/legacy/inquiry/statistics` | `LegacyInquiryQueryController#getInquiryStatistics` | `LegacyUserQueryService#checkAdminRole`, `LegacyInquiryQueryService#getInquiryStatistics` |
| GET | `/legacy/inquiry/files/{inquiryId}/{fileName}` | `LegacyInquiryQueryController#downloadInquiryFile` | `LegacyUserQueryService#findByEmail`, `LegacyInquiryQueryService#getInquiry` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `LegacyInquiryCommandServiceImpl`
  - `createInquiry` → UserRepository#findById~, LegacyInquiryRepository#save~, this#processImagesAndUpdateHtml, LegacyInquiryImageRepository#findAllByInquiry_Id
  - `updateInquiry` → LegacyInquiryRepository#findById~, LegacyInquiryImageRepository#findAllByInquiry_IdAndIsAnswerFalse, this#deleteImages, this#processImagesAndUpdateHtml, LegacyInquiryImageRepository#findAllByInquiry_Id
  - `deleteInquiry` → LegacyInquiryRepository#findById~, LegacyInquiryImageRepository#findAllByInquiry_Id, this#deleteImages, FileUtils#deleteDirectory, LegacyInquiryRepository#delete~
  - `updateInquiryStatus` → LegacyInquiryRepository#findById~, LegacyInquiryImageRepository#findAllByInquiry_Id
  - `answerInquiry` → LegacyInquiryRepository#findById~, this#processImagesAndUpdateHtml, LegacyInquiryImageRepository#findAllByInquiry_Id
  - `processImagesAndUpdateHtml` → FileUtils#saveFile, LegacyInquiryImageRepository#save~, this#updateHtmlImageSources
  - `deleteImages` → FileUtils#deleteFile, LegacyInquiryImageRepository#deleteAll~
- `LegacyInquiryQueryServiceImpl`
  - `getInquiry` → LegacyInquiryRepository#findById~, LegacyInquiryImageRepository#findAllByInquiry_Id
  - `getInquiries` → LegacyInquiryRepository#searchInquiries
  - `getUserInquiries` → UserRepository#findById~, LegacyInquiryRepository#searchInquiries
  - `getInquiriesByStatus` → LegacyInquiryRepository#searchInquiries
  - `getInquiriesByCategory` → LegacyInquiryRepository#searchInquiries
  - `searchInquiries` → LegacyInquiryRepository#findByTitleOrContentContaining
  - `getPendingInquiries` → LegacyInquiryRepository#findUnansweredInquiriesOrderByOldest
  - `getInquiryStatistics` → LegacyInquiryRepository#getInquiryStatistics

## 3. 클래스 목록 (역할별)

### controller (2)

- `LegacyInquiryCommandController` controller/command/LegacyInquiryCommandController.java · public: createInquiry, updateInquiry, deleteInquiry, answerInquiry, updateInquiryStatus · 주입: LegacyInquiryCommandService, LegacyUserQueryService
- `LegacyInquiryQueryController` controller/query/LegacyInquiryQueryController.java · public: getInquiry, getMyInquiries, getInquiries, getInquiriesByStatus, getInquiriesByCategory, searchInquiries, getPendingInquiries, getInquiryStatistics, downloadInquiryFile · 주입: LegacyInquiryQueryService, LegacyUserQueryService, FileUtils

### service-command (2)

- `LegacyInquiryCommandService` (interface) service/command/LegacyInquiryCommandService.java · public: createInquiry, updateInquiry, deleteInquiry, updateInquiryStatus, answerInquiry
- `LegacyInquiryCommandServiceImpl` service/command/LegacyInquiryCommandServiceImpl.java · extends/implements LegacyInquiryCommandService · public: createInquiry, updateInquiry, deleteInquiry, updateInquiryStatus, answerInquiry · 주입: LegacyInquiryRepository, LegacyInquiryImageRepository, UserRepository, FileUtils

### service-query (2)

- `LegacyInquiryQueryService` (interface) service/query/LegacyInquiryQueryService.java · public: getInquiry, getInquiries, getUserInquiries, getInquiriesByStatus, getInquiriesByCategory, searchInquiries, getPendingInquiries, getInquiryStatistics
- `LegacyInquiryQueryServiceImpl` service/query/LegacyInquiryQueryServiceImpl.java · extends/implements LegacyInquiryQueryService · public: getInquiry, getInquiries, getUserInquiries, getInquiriesByStatus, getInquiriesByCategory, searchInquiries, getPendingInquiries, getInquiryStatistics · 주입: LegacyInquiryRepository, LegacyInquiryImageRepository, UserRepository

### repository (2)

- `LegacyInquiryImageRepository` (interface) repository/LegacyInquiryImageRepository.java · extends/implements JpaRepository · public: findAllByInquiry_Id, findAllByInquiry_IdAndIsAnswerFalse, findAllByInquiry_IdAndIsAnswerTrue, deleteAllByInquiry_Id, countByInquiry_Id, findByInquiry_IdAndImageUrlContaining
- `LegacyInquiryRepository` (interface) repository/LegacyInquiryRepository.java · extends/implements JpaRepository, LegacyInquiryRepositoryCustom · public: findByUserIdOrderByCreatedDateDesc, findByStatus, findByCategory, findByCreatedDateBetween, findByTitleContaining, findByUserIdAndStatus, findAllUnansweredInquiries, countByUserId, countByStatus, countByCreatedDateBetween

### custom (2)

- `LegacyInquiryRepositoryCustom` (interface) repository/LegacyInquiryRepositoryCustom.java · public: searchInquiries, findInquiryWithUserDetails, findUnansweredInquiriesOrderByOldest, findByTitleOrContentContaining, getInquiryStatistics
- `LegacyInquiryRepositoryImpl` repository/LegacyInquiryRepositoryImpl.java · extends/implements LegacyInquiryRepositoryCustom · public: searchInquiries, findInquiryWithUserDetails, findUnansweredInquiriesOrderByOldest, findByTitleOrContentContaining, getInquiryStatistics · 주입: JPAQueryFactory

### 다른 도메인 참조 (import·주입 기준)

- **inquiry**: `Inquiry` ← LegacyInquiryCommandServiceImpl, LegacyInquiryQueryServiceImpl, LegacyInquiryRepository; `InquiryCategory` ← LegacyInquiryQueryController, LegacyInquiryQueryService, LegacyInquiryQueryServiceImpl, LegacyInquiryRepository, LegacyInquiryRepositoryImpl; `InquiryImage` ← LegacyInquiryCommandServiceImpl, LegacyInquiryImageRepository, LegacyInquiryQueryServiceImpl; `InquiryStatus` ← LegacyInquiryCommandController, LegacyInquiryCommandService, LegacyInquiryCommandServiceImpl, LegacyInquiryQueryController, LegacyInquiryQueryService, LegacyInquiryQueryServiceImpl, LegacyInquiryRepository, LegacyInquiryRepositoryImpl
- **legacyuser**: `LegacyUserQueryService` ← LegacyInquiryCommandController, LegacyInquiryQueryController
- **user**: `User` ← LegacyInquiryCommandController, LegacyInquiryCommandServiceImpl, LegacyInquiryQueryController, LegacyInquiryQueryServiceImpl; `UserRepository` ← LegacyInquiryCommandServiceImpl, LegacyInquiryQueryServiceImpl

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

