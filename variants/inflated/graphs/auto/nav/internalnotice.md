# internalnotice 도메인

기준 경로: `src/main/java/com/fream/back/domain/internalnotice/` · 파일 8개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/internal/notices` | `InternalNoticeCommandController#createNotice` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `this#validateCreateRequest`, `InternalNoticeCommandService#createNotice` |
| PUT | `/internal/notices/{noticeId}` | `InternalNoticeCommandController#updateNotice` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `this#validateUpdateRequest`, `InternalNoticeCommandService#updateNotice` |
| DELETE | `/internal/notices/{noticeId}` | `InternalNoticeCommandController#deleteNotice` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `InternalNoticeCommandService#deleteNotice` |
| GET | `/internal/notices/{noticeId}` | `InternalNoticeQueryController#getNotice` | `InternalNoticeQueryService#getNotice` |
| GET | `/internal/notices/search` | `InternalNoticeQueryController#searchNotices` | `InternalNoticeQueryService#searchNotices` |
| GET | `/internal/notices` | `InternalNoticeQueryController#getNoticesByCategory` | `InternalNoticeQueryService#getNoticesByCategory`, `InternalNoticeQueryService#getNotices` |
| GET | `/internal/notices/files/{noticeId}/{fileName}` | `InternalNoticeQueryController#getNoticeFile` | `InternalNoticeQueryService#getNoticeFileResource`, `this#determineContentType` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `InternalNoticeCommandService`
  - `createNotice` → NoticeRepository#save~, this#sendNotificationToAllUsers, this#saveNoticeFiles, this#updateImageUrlsInContent, this#saveNoticeImageEntities, this#createResponseDto
  - `updateNotice` → NoticeRepository#findById~, this#extractImagePathsFromContent, InternalNoticeImageRepository#findAllByNoticeId, InternalNoticeImageRepository#deleteAll~, FileUtils#deleteFile, this#saveNoticeFiles, this#updateImageUrlsInContent, this#saveNoticeImageEntities, this#createResponseDto
  - `deleteNotice` → NoticeRepository#findById~, InternalNoticeImageRepository#findAllByNoticeId, InternalNoticeImageRepository#deleteAll~, FileUtils#deleteDirectory, NoticeRepository#delete~
  - `saveNoticeFiles` → this#validateFileExtension, FileUtils#saveFile
  - `saveNoticeImageEntities` → this#isVideoFile, InternalNoticeImageRepository#save~
  - `createResponseDto` → InternalNoticeImageRepository#findAllByNoticeId
  - `sendNotificationToAllUsers` → InternalNotificationCommandService#createNotificationForAll
- `InternalNoticeFileStorageUtil`
  - `saveFiles` → this#saveFileInternal
  - `saveFileInternal` → this#validateFileExtension
- `InternalNoticeQueryService`
  - `getNotices` → NoticeRepository#findAllOrderByCreatedDateDesc
  - `getNotice` → NoticeRepository#findById~, this#toResponseDto
  - `getNoticesByCategory` → NoticeRepository#findByCategoryOrderByCreatedDateDesc
  - `searchNotices` → NoticeRepository#searchNotices
  - `getFilePreview` → FileUtils#existsFile
  - `toResponseDto` → InternalNoticeImageRepository#findAllByNoticeId
- `InternalNoticeRepositoryImpl`
  - `searchNotices` → this#containsKeyword
  - `searchNoticesByCategoryAndKeyword` → this#containsKeyword

## 3. 클래스 목록 (역할별)

### controller (2)

- `InternalNoticeCommandController` controller/command/InternalNoticeCommandController.java · public: createNotice, updateNotice, deleteNotice · 주입: InternalNoticeCommandService, InternalUserQueryService
- `InternalNoticeQueryController` controller/query/InternalNoticeQueryController.java · public: getNotice, searchNotices, getNoticesByCategory, getNoticeFile · 주입: InternalNoticeQueryService

### service-command (2)

- `InternalNoticeCommandService` service/command/InternalNoticeCommandService.java · public: createNotice, updateNotice, deleteNotice · 주입: NoticeRepository, InternalNoticeImageRepository, InternalNotificationCommandService, FileUtils
- `InternalNoticeFileStorageUtil` service/command/InternalNoticeFileStorageUtil.java · public: saveFiles, deleteFiles, isVideo, fileExists, getFilePath, extractImagePaths, updateImagePaths

### service-query (1)

- `InternalNoticeQueryService` service/query/InternalNoticeQueryService.java · public: getNotices, getNotice, getNoticesByCategory, searchNotices, getNoticeFileResource, getFilePreview · 주입: NoticeRepository, InternalNoticeImageRepository, FileUtils

### repository (1)

- `InternalNoticeImageRepository` (interface) repository/InternalNoticeImageRepository.java · extends/implements JpaRepository · public: findAllByNoticeId, findImagesToDelete, deleteAllByNoticeId, deleteByImageUrls

### custom (2)

- `InternalNoticeRepositoryCustom` (interface) repository/InternalNoticeRepositoryCustom.java · public: searchNotices, searchNoticesByCategoryAndKeyword
- `InternalNoticeRepositoryImpl` repository/InternalNoticeRepositoryImpl.java · extends/implements InternalNoticeRepositoryCustom · public: searchNotices, searchNoticesByCategoryAndKeyword · 주입: JPAQueryFactory

### 다른 도메인 참조 (import·주입 기준)

- **internalnotification**: `InternalNotificationCommandService` ← InternalNoticeCommandService
- **internaluser**: `InternalUserQueryService` ← InternalNoticeCommandController
- **notice**: `Notice` ← InternalNoticeCommandService, InternalNoticeQueryService, InternalNoticeRepositoryCustom, InternalNoticeRepositoryImpl; `NoticeCategory` ← InternalNoticeCommandController, InternalNoticeQueryController, InternalNoticeQueryService, InternalNoticeRepositoryCustom, InternalNoticeRepositoryImpl; `NoticeImage` ← InternalNoticeCommandService, InternalNoticeImageRepository, InternalNoticeQueryService; `NoticeRepository` ← InternalNoticeCommandService, InternalNoticeQueryService
- **notification**: `NotificationCategory` ← InternalNoticeCommandService; `NotificationType` ← InternalNoticeCommandService

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

