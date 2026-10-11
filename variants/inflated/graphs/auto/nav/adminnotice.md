# adminnotice 도메인

기준 경로: `src/main/java/com/fream/back/domain/adminnotice/` · 파일 8개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/admin/notices` | `AdminNoticeCommandController#createNotice` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `this#validateCreateRequest`, `AdminNoticeCommandService#createNotice` |
| PUT | `/admin/notices/{noticeId}` | `AdminNoticeCommandController#updateNotice` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `this#validateUpdateRequest`, `AdminNoticeCommandService#updateNotice` |
| DELETE | `/admin/notices/{noticeId}` | `AdminNoticeCommandController#deleteNotice` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `AdminNoticeCommandService#deleteNotice` |
| GET | `/admin/notices/{noticeId}` | `AdminNoticeQueryController#getNotice` | `AdminNoticeQueryService#getNotice` |
| GET | `/admin/notices/search` | `AdminNoticeQueryController#searchNotices` | `AdminNoticeQueryService#searchNotices` |
| GET | `/admin/notices` | `AdminNoticeQueryController#getNoticesByCategory` | `AdminNoticeQueryService#getNoticesByCategory`, `AdminNoticeQueryService#getNotices` |
| GET | `/admin/notices/files/{noticeId}/{fileName}` | `AdminNoticeQueryController#getNoticeFile` | `AdminNoticeQueryService#getNoticeFileResource`, `this#determineContentType` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `AdminNoticeCommandService`
  - `createNotice` → NoticeRepository#save~, this#sendNotificationToAllUsers, this#saveNoticeFiles, this#updateImageUrlsInContent, this#saveNoticeImageEntities, this#createResponseDto
  - `updateNotice` → NoticeRepository#findById~, this#extractImagePathsFromContent, AdminNoticeImageRepository#findAllByNoticeId, AdminNoticeImageRepository#deleteAll~, FileUtils#deleteFile, this#saveNoticeFiles, this#updateImageUrlsInContent, this#saveNoticeImageEntities, this#createResponseDto
  - `deleteNotice` → NoticeRepository#findById~, AdminNoticeImageRepository#findAllByNoticeId, AdminNoticeImageRepository#deleteAll~, FileUtils#deleteDirectory, NoticeRepository#delete~
  - `saveNoticeFiles` → this#validateFileExtension, FileUtils#saveFile
  - `saveNoticeImageEntities` → this#isVideoFile, AdminNoticeImageRepository#save~
  - `createResponseDto` → AdminNoticeImageRepository#findAllByNoticeId
  - `sendNotificationToAllUsers` → AdminNotificationCommandService#createNotificationForAll
- `AdminNoticeFileStorageUtil`
  - `saveFiles` → this#saveFileInternal
  - `saveFileInternal` → this#validateFileExtension
- `AdminNoticeQueryService`
  - `getNotices` → NoticeRepository#findAllOrderByCreatedDateDesc
  - `getNotice` → NoticeRepository#findById~, this#toResponseDto
  - `getNoticesByCategory` → NoticeRepository#findByCategoryOrderByCreatedDateDesc
  - `searchNotices` → NoticeRepository#searchNotices
  - `getFilePreview` → FileUtils#existsFile
  - `toResponseDto` → AdminNoticeImageRepository#findAllByNoticeId
- `AdminNoticeRepositoryImpl`
  - `searchNotices` → this#containsKeyword
  - `searchNoticesByCategoryAndKeyword` → this#containsKeyword

## 3. 클래스 목록 (역할별)

### controller (2)

- `AdminNoticeCommandController` controller/command/AdminNoticeCommandController.java · public: createNotice, updateNotice, deleteNotice · 주입: AdminNoticeCommandService, AdminUserQueryService
- `AdminNoticeQueryController` controller/query/AdminNoticeQueryController.java · public: getNotice, searchNotices, getNoticesByCategory, getNoticeFile · 주입: AdminNoticeQueryService

### service-command (2)

- `AdminNoticeCommandService` service/command/AdminNoticeCommandService.java · public: createNotice, updateNotice, deleteNotice · 주입: NoticeRepository, AdminNoticeImageRepository, AdminNotificationCommandService, FileUtils
- `AdminNoticeFileStorageUtil` service/command/AdminNoticeFileStorageUtil.java · public: saveFiles, deleteFiles, isVideo, fileExists, getFilePath, extractImagePaths, updateImagePaths

### service-query (1)

- `AdminNoticeQueryService` service/query/AdminNoticeQueryService.java · public: getNotices, getNotice, getNoticesByCategory, searchNotices, getNoticeFileResource, getFilePreview · 주입: NoticeRepository, AdminNoticeImageRepository, FileUtils

### repository (1)

- `AdminNoticeImageRepository` (interface) repository/AdminNoticeImageRepository.java · extends/implements JpaRepository · public: findAllByNoticeId, findImagesToDelete, deleteAllByNoticeId, deleteByImageUrls

### custom (2)

- `AdminNoticeRepositoryCustom` (interface) repository/AdminNoticeRepositoryCustom.java · public: searchNotices, searchNoticesByCategoryAndKeyword
- `AdminNoticeRepositoryImpl` repository/AdminNoticeRepositoryImpl.java · extends/implements AdminNoticeRepositoryCustom · public: searchNotices, searchNoticesByCategoryAndKeyword · 주입: JPAQueryFactory

### 다른 도메인 참조 (import·주입 기준)

- **adminnotification**: `AdminNotificationCommandService` ← AdminNoticeCommandService
- **adminuser**: `AdminUserQueryService` ← AdminNoticeCommandController
- **notice**: `Notice` ← AdminNoticeCommandService, AdminNoticeQueryService, AdminNoticeRepositoryCustom, AdminNoticeRepositoryImpl; `NoticeCategory` ← AdminNoticeCommandController, AdminNoticeQueryController, AdminNoticeQueryService, AdminNoticeRepositoryCustom, AdminNoticeRepositoryImpl; `NoticeImage` ← AdminNoticeCommandService, AdminNoticeImageRepository, AdminNoticeQueryService; `NoticeRepository` ← AdminNoticeCommandService, AdminNoticeQueryService
- **notification**: `NotificationCategory` ← AdminNoticeCommandService; `NotificationType` ← AdminNoticeCommandService

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

