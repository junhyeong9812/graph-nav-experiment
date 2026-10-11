# settlementnotice 도메인

기준 경로: `src/main/java/com/fream/back/domain/settlementnotice/` · 파일 8개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/settlement/notices` | `SettlementNoticeCommandController#createNotice` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `this#validateCreateRequest`, `SettlementNoticeCommandService#createNotice` |
| PUT | `/settlement/notices/{noticeId}` | `SettlementNoticeCommandController#updateNotice` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `this#validateUpdateRequest`, `SettlementNoticeCommandService#updateNotice` |
| DELETE | `/settlement/notices/{noticeId}` | `SettlementNoticeCommandController#deleteNotice` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `SettlementNoticeCommandService#deleteNotice` |
| GET | `/settlement/notices/{noticeId}` | `SettlementNoticeQueryController#getNotice` | `SettlementNoticeQueryService#getNotice` |
| GET | `/settlement/notices/search` | `SettlementNoticeQueryController#searchNotices` | `SettlementNoticeQueryService#searchNotices` |
| GET | `/settlement/notices` | `SettlementNoticeQueryController#getNoticesByCategory` | `SettlementNoticeQueryService#getNoticesByCategory`, `SettlementNoticeQueryService#getNotices` |
| GET | `/settlement/notices/files/{noticeId}/{fileName}` | `SettlementNoticeQueryController#getNoticeFile` | `SettlementNoticeQueryService#getNoticeFileResource`, `this#determineContentType` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `SettlementNoticeCommandService`
  - `createNotice` → NoticeRepository#save~, this#sendNotificationToAllUsers, this#saveNoticeFiles, this#updateImageUrlsInContent, this#saveNoticeImageEntities, this#createResponseDto
  - `updateNotice` → NoticeRepository#findById~, this#extractImagePathsFromContent, SettlementNoticeImageRepository#findAllByNoticeId, SettlementNoticeImageRepository#deleteAll~, FileUtils#deleteFile, this#saveNoticeFiles, this#updateImageUrlsInContent, this#saveNoticeImageEntities, this#createResponseDto
  - `deleteNotice` → NoticeRepository#findById~, SettlementNoticeImageRepository#findAllByNoticeId, SettlementNoticeImageRepository#deleteAll~, FileUtils#deleteDirectory, NoticeRepository#delete~
  - `saveNoticeFiles` → this#validateFileExtension, FileUtils#saveFile
  - `saveNoticeImageEntities` → this#isVideoFile, SettlementNoticeImageRepository#save~
  - `createResponseDto` → SettlementNoticeImageRepository#findAllByNoticeId
  - `sendNotificationToAllUsers` → SettlementNotificationCommandService#createNotificationForAll
- `SettlementNoticeFileStorageUtil`
  - `saveFiles` → this#saveFileInternal
  - `saveFileInternal` → this#validateFileExtension
- `SettlementNoticeQueryService`
  - `getNotices` → NoticeRepository#findAllOrderByCreatedDateDesc
  - `getNotice` → NoticeRepository#findById~, this#toResponseDto
  - `getNoticesByCategory` → NoticeRepository#findByCategoryOrderByCreatedDateDesc
  - `searchNotices` → NoticeRepository#searchNotices
  - `getFilePreview` → FileUtils#existsFile
  - `toResponseDto` → SettlementNoticeImageRepository#findAllByNoticeId
- `SettlementNoticeRepositoryImpl`
  - `searchNotices` → this#containsKeyword
  - `searchNoticesByCategoryAndKeyword` → this#containsKeyword

## 3. 클래스 목록 (역할별)

### controller (2)

- `SettlementNoticeCommandController` controller/command/SettlementNoticeCommandController.java · public: createNotice, updateNotice, deleteNotice · 주입: SettlementNoticeCommandService, SettlementUserQueryService
- `SettlementNoticeQueryController` controller/query/SettlementNoticeQueryController.java · public: getNotice, searchNotices, getNoticesByCategory, getNoticeFile · 주입: SettlementNoticeQueryService

### service-command (2)

- `SettlementNoticeCommandService` service/command/SettlementNoticeCommandService.java · public: createNotice, updateNotice, deleteNotice · 주입: NoticeRepository, SettlementNoticeImageRepository, SettlementNotificationCommandService, FileUtils
- `SettlementNoticeFileStorageUtil` service/command/SettlementNoticeFileStorageUtil.java · public: saveFiles, deleteFiles, isVideo, fileExists, getFilePath, extractImagePaths, updateImagePaths

### service-query (1)

- `SettlementNoticeQueryService` service/query/SettlementNoticeQueryService.java · public: getNotices, getNotice, getNoticesByCategory, searchNotices, getNoticeFileResource, getFilePreview · 주입: NoticeRepository, SettlementNoticeImageRepository, FileUtils

### repository (1)

- `SettlementNoticeImageRepository` (interface) repository/SettlementNoticeImageRepository.java · extends/implements JpaRepository · public: findAllByNoticeId, findImagesToDelete, deleteAllByNoticeId, deleteByImageUrls

### custom (2)

- `SettlementNoticeRepositoryCustom` (interface) repository/SettlementNoticeRepositoryCustom.java · public: searchNotices, searchNoticesByCategoryAndKeyword
- `SettlementNoticeRepositoryImpl` repository/SettlementNoticeRepositoryImpl.java · extends/implements SettlementNoticeRepositoryCustom · public: searchNotices, searchNoticesByCategoryAndKeyword · 주입: JPAQueryFactory

### 다른 도메인 참조 (import·주입 기준)

- **notice**: `Notice` ← SettlementNoticeCommandService, SettlementNoticeQueryService, SettlementNoticeRepositoryCustom, SettlementNoticeRepositoryImpl; `NoticeCategory` ← SettlementNoticeCommandController, SettlementNoticeQueryController, SettlementNoticeQueryService, SettlementNoticeRepositoryCustom, SettlementNoticeRepositoryImpl; `NoticeImage` ← SettlementNoticeCommandService, SettlementNoticeImageRepository, SettlementNoticeQueryService; `NoticeRepository` ← SettlementNoticeCommandService, SettlementNoticeQueryService
- **notification**: `NotificationCategory` ← SettlementNoticeCommandService; `NotificationType` ← SettlementNoticeCommandService
- **settlementnotification**: `SettlementNotificationCommandService` ← SettlementNoticeCommandService
- **settlementuser**: `SettlementUserQueryService` ← SettlementNoticeCommandController

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

