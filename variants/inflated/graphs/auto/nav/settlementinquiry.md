# settlementinquiry 도메인

기준 경로: `src/main/java/com/fream/back/domain/settlementinquiry/` · 파일 10개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/settlement/inquiry` | `SettlementInquiryCommandController#createInquiry` | `SettlementUserQueryService#findByEmail`, `SettlementInquiryCommandService#createInquiry` |
| PUT | `/settlement/inquiry/{inquiryId}` | `SettlementInquiryCommandController#updateInquiry` | `SettlementUserQueryService#findByEmail`, `SettlementInquiryCommandService#updateInquiry` |
| DELETE | `/settlement/inquiry/{inquiryId}` | `SettlementInquiryCommandController#deleteInquiry` | `SettlementUserQueryService#findByEmail`, `SettlementInquiryCommandService#deleteInquiry` |
| POST | `/settlement/inquiry/{inquiryId}/answer` | `SettlementInquiryCommandController#answerInquiry` | `SettlementUserQueryService#checkAdminRole`, `SettlementInquiryCommandService#answerInquiry` |
| PUT | `/settlement/inquiry/{inquiryId}/status` | `SettlementInquiryCommandController#updateInquiryStatus` | `SettlementUserQueryService#checkAdminRole`, `SettlementInquiryCommandService#updateInquiryStatus` |
| GET | `/settlement/inquiry/{inquiryId}` | `SettlementInquiryQueryController#getInquiry` | `SettlementUserQueryService#findByEmail`, `SettlementInquiryQueryService#getInquiry` |
| GET | `/settlement/inquiry/my` | `SettlementInquiryQueryController#getMyInquiries` | `SettlementUserQueryService#findByEmail`, `SettlementInquiryQueryService#getUserInquiries` |
| GET | `/settlement/inquiry` | `SettlementInquiryQueryController#getInquiries` | `SettlementUserQueryService#checkAdminRole`, `SettlementInquiryQueryService#getInquiries` |
| GET | `/settlement/inquiry/status/{status}` | `SettlementInquiryQueryController#getInquiriesByStatus` | `SettlementUserQueryService#checkAdminRole`, `SettlementInquiryQueryService#getInquiriesByStatus` |
| GET | `/settlement/inquiry/category/{category}` | `SettlementInquiryQueryController#getInquiriesByCategory` | `SettlementInquiryQueryService#getInquiriesByCategory` |
| GET | `/settlement/inquiry/search` | `SettlementInquiryQueryController#searchInquiries` | `SettlementInquiryQueryService#searchInquiries` |
| GET | `/settlement/inquiry/pending` | `SettlementInquiryQueryController#getPendingInquiries` | `SettlementUserQueryService#checkAdminRole`, `SettlementInquiryQueryService#getPendingInquiries` |
| GET | `/settlement/inquiry/statistics` | `SettlementInquiryQueryController#getInquiryStatistics` | `SettlementUserQueryService#checkAdminRole`, `SettlementInquiryQueryService#getInquiryStatistics` |
| GET | `/settlement/inquiry/files/{inquiryId}/{fileName}` | `SettlementInquiryQueryController#downloadInquiryFile` | `SettlementUserQueryService#findByEmail`, `SettlementInquiryQueryService#getInquiry` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `SettlementInquiryCommandServiceImpl`
  - `createInquiry` → UserRepository#findById~, SettlementInquiryRepository#save~, this#processImagesAndUpdateHtml, SettlementInquiryImageRepository#findAllByInquiry_Id
  - `updateInquiry` → SettlementInquiryRepository#findById~, SettlementInquiryImageRepository#findAllByInquiry_IdAndIsAnswerFalse, this#deleteImages, this#processImagesAndUpdateHtml, SettlementInquiryImageRepository#findAllByInquiry_Id
  - `deleteInquiry` → SettlementInquiryRepository#findById~, SettlementInquiryImageRepository#findAllByInquiry_Id, this#deleteImages, FileUtils#deleteDirectory, SettlementInquiryRepository#delete~
  - `updateInquiryStatus` → SettlementInquiryRepository#findById~, SettlementInquiryImageRepository#findAllByInquiry_Id
  - `answerInquiry` → SettlementInquiryRepository#findById~, this#processImagesAndUpdateHtml, SettlementInquiryImageRepository#findAllByInquiry_Id
  - `processImagesAndUpdateHtml` → FileUtils#saveFile, SettlementInquiryImageRepository#save~, this#updateHtmlImageSources
  - `deleteImages` → FileUtils#deleteFile, SettlementInquiryImageRepository#deleteAll~
- `SettlementInquiryQueryServiceImpl`
  - `getInquiry` → SettlementInquiryRepository#findById~, SettlementInquiryImageRepository#findAllByInquiry_Id
  - `getInquiries` → SettlementInquiryRepository#searchInquiries
  - `getUserInquiries` → UserRepository#findById~, SettlementInquiryRepository#searchInquiries
  - `getInquiriesByStatus` → SettlementInquiryRepository#searchInquiries
  - `getInquiriesByCategory` → SettlementInquiryRepository#searchInquiries
  - `searchInquiries` → SettlementInquiryRepository#findByTitleOrContentContaining
  - `getPendingInquiries` → SettlementInquiryRepository#findUnansweredInquiriesOrderByOldest
  - `getInquiryStatistics` → SettlementInquiryRepository#getInquiryStatistics

## 3. 클래스 목록 (역할별)

### controller (2)

- `SettlementInquiryCommandController` controller/command/SettlementInquiryCommandController.java · public: createInquiry, updateInquiry, deleteInquiry, answerInquiry, updateInquiryStatus · 주입: SettlementInquiryCommandService, SettlementUserQueryService
- `SettlementInquiryQueryController` controller/query/SettlementInquiryQueryController.java · public: getInquiry, getMyInquiries, getInquiries, getInquiriesByStatus, getInquiriesByCategory, searchInquiries, getPendingInquiries, getInquiryStatistics, downloadInquiryFile · 주입: SettlementInquiryQueryService, SettlementUserQueryService, FileUtils

### service-command (2)

- `SettlementInquiryCommandService` (interface) service/command/SettlementInquiryCommandService.java · public: createInquiry, updateInquiry, deleteInquiry, updateInquiryStatus, answerInquiry
- `SettlementInquiryCommandServiceImpl` service/command/SettlementInquiryCommandServiceImpl.java · extends/implements SettlementInquiryCommandService · public: createInquiry, updateInquiry, deleteInquiry, updateInquiryStatus, answerInquiry · 주입: SettlementInquiryRepository, SettlementInquiryImageRepository, UserRepository, FileUtils

### service-query (2)

- `SettlementInquiryQueryService` (interface) service/query/SettlementInquiryQueryService.java · public: getInquiry, getInquiries, getUserInquiries, getInquiriesByStatus, getInquiriesByCategory, searchInquiries, getPendingInquiries, getInquiryStatistics
- `SettlementInquiryQueryServiceImpl` service/query/SettlementInquiryQueryServiceImpl.java · extends/implements SettlementInquiryQueryService · public: getInquiry, getInquiries, getUserInquiries, getInquiriesByStatus, getInquiriesByCategory, searchInquiries, getPendingInquiries, getInquiryStatistics · 주입: SettlementInquiryRepository, SettlementInquiryImageRepository, UserRepository

### repository (2)

- `SettlementInquiryImageRepository` (interface) repository/SettlementInquiryImageRepository.java · extends/implements JpaRepository · public: findAllByInquiry_Id, findAllByInquiry_IdAndIsAnswerFalse, findAllByInquiry_IdAndIsAnswerTrue, deleteAllByInquiry_Id, countByInquiry_Id, findByInquiry_IdAndImageUrlContaining
- `SettlementInquiryRepository` (interface) repository/SettlementInquiryRepository.java · extends/implements JpaRepository, SettlementInquiryRepositoryCustom · public: findByUserIdOrderByCreatedDateDesc, findByStatus, findByCategory, findByCreatedDateBetween, findByTitleContaining, findByUserIdAndStatus, findAllUnansweredInquiries, countByUserId, countByStatus, countByCreatedDateBetween

### custom (2)

- `SettlementInquiryRepositoryCustom` (interface) repository/SettlementInquiryRepositoryCustom.java · public: searchInquiries, findInquiryWithUserDetails, findUnansweredInquiriesOrderByOldest, findByTitleOrContentContaining, getInquiryStatistics
- `SettlementInquiryRepositoryImpl` repository/SettlementInquiryRepositoryImpl.java · extends/implements SettlementInquiryRepositoryCustom · public: searchInquiries, findInquiryWithUserDetails, findUnansweredInquiriesOrderByOldest, findByTitleOrContentContaining, getInquiryStatistics · 주입: JPAQueryFactory

### 다른 도메인 참조 (import·주입 기준)

- **inquiry**: `Inquiry` ← SettlementInquiryCommandServiceImpl, SettlementInquiryQueryServiceImpl, SettlementInquiryRepository; `InquiryCategory` ← SettlementInquiryQueryController, SettlementInquiryQueryService, SettlementInquiryQueryServiceImpl, SettlementInquiryRepository, SettlementInquiryRepositoryImpl; `InquiryImage` ← SettlementInquiryCommandServiceImpl, SettlementInquiryImageRepository, SettlementInquiryQueryServiceImpl; `InquiryStatus` ← SettlementInquiryCommandController, SettlementInquiryCommandService, SettlementInquiryCommandServiceImpl, SettlementInquiryQueryController, SettlementInquiryQueryService, SettlementInquiryQueryServiceImpl, SettlementInquiryRepository, SettlementInquiryRepositoryImpl
- **settlementuser**: `SettlementUserQueryService` ← SettlementInquiryCommandController, SettlementInquiryQueryController
- **user**: `User` ← SettlementInquiryCommandController, SettlementInquiryCommandServiceImpl, SettlementInquiryQueryController, SettlementInquiryQueryServiceImpl; `UserRepository` ← SettlementInquiryCommandServiceImpl, SettlementInquiryQueryServiceImpl

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

