# legacynotice 도메인

기준 경로: `src/main/java/com/fream/back/domain/legacynotice/` · 파일 8개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/legacy/notices` | `LegacyNoticeCommandController#createNotice` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `this#validateCreateRequest`, `LegacyNoticeCommandService#createNotice` |
| PUT | `/legacy/notices/{noticeId}` | `LegacyNoticeCommandController#updateNotice` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `this#validateUpdateRequest`, `LegacyNoticeCommandService#updateNotice` |
| DELETE | `/legacy/notices/{noticeId}` | `LegacyNoticeCommandController#deleteNotice` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `LegacyNoticeCommandService#deleteNotice` |
| GET | `/legacy/notices/{noticeId}` | `LegacyNoticeQueryController#getNotice` | `LegacyNoticeQueryService#getNotice` |
| GET | `/legacy/notices/search` | `LegacyNoticeQueryController#searchNotices` | `LegacyNoticeQueryService#searchNotices` |
| GET | `/legacy/notices` | `LegacyNoticeQueryController#getNoticesByCategory` | `LegacyNoticeQueryService#getNoticesByCategory`, `LegacyNoticeQueryService#getNotices` |
| GET | `/legacy/notices/files/{noticeId}/{fileName}` | `LegacyNoticeQueryController#getNoticeFile` | `LegacyNoticeQueryService#getNoticeFileResource`, `this#determineContentType` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `LegacyNoticeCommandService`
  - `createNotice` → NoticeRepository#save~, this#sendNotificationToAllUsers, this#saveNoticeFiles, this#updateImageUrlsInContent, this#saveNoticeImageEntities, this#createResponseDto
  - `updateNotice` → NoticeRepository#findById~, this#extractImagePathsFromContent, LegacyNoticeImageRepository#findAllByNoticeId, LegacyNoticeImageRepository#deleteAll~, FileUtils#deleteFile, this#saveNoticeFiles, this#updateImageUrlsInContent, this#saveNoticeImageEntities, this#createResponseDto
  - `deleteNotice` → NoticeRepository#findById~, LegacyNoticeImageRepository#findAllByNoticeId, LegacyNoticeImageRepository#deleteAll~, FileUtils#deleteDirectory, NoticeRepository#delete~
  - `saveNoticeFiles` → this#validateFileExtension, FileUtils#saveFile
  - `saveNoticeImageEntities` → this#isVideoFile, LegacyNoticeImageRepository#save~
  - `createResponseDto` → LegacyNoticeImageRepository#findAllByNoticeId
  - `sendNotificationToAllUsers` → LegacyNotificationCommandService#createNotificationForAll
- `LegacyNoticeFileStorageUtil`
  - `saveFiles` → this#saveFileInternal
  - `saveFileInternal` → this#validateFileExtension
- `LegacyNoticeQueryService`
  - `getNotices` → NoticeRepository#findAllOrderByCreatedDateDesc
  - `getNotice` → NoticeRepository#findById~, this#toResponseDto
  - `getNoticesByCategory` → NoticeRepository#findByCategoryOrderByCreatedDateDesc
  - `searchNotices` → NoticeRepository#searchNotices
  - `getFilePreview` → FileUtils#existsFile
  - `toResponseDto` → LegacyNoticeImageRepository#findAllByNoticeId
- `LegacyNoticeRepositoryImpl`
  - `searchNotices` → this#containsKeyword
  - `searchNoticesByCategoryAndKeyword` → this#containsKeyword

## 3. 클래스 목록 (역할별)

### controller (2)

- `LegacyNoticeCommandController` controller/command/LegacyNoticeCommandController.java · public: createNotice, updateNotice, deleteNotice · 주입: LegacyNoticeCommandService, LegacyUserQueryService
- `LegacyNoticeQueryController` controller/query/LegacyNoticeQueryController.java · public: getNotice, searchNotices, getNoticesByCategory, getNoticeFile · 주입: LegacyNoticeQueryService

### service-command (2)

- `LegacyNoticeCommandService` service/command/LegacyNoticeCommandService.java · public: createNotice, updateNotice, deleteNotice · 주입: NoticeRepository, LegacyNoticeImageRepository, LegacyNotificationCommandService, FileUtils
- `LegacyNoticeFileStorageUtil` service/command/LegacyNoticeFileStorageUtil.java · public: saveFiles, deleteFiles, isVideo, fileExists, getFilePath, extractImagePaths, updateImagePaths

### service-query (1)

- `LegacyNoticeQueryService` service/query/LegacyNoticeQueryService.java · public: getNotices, getNotice, getNoticesByCategory, searchNotices, getNoticeFileResource, getFilePreview · 주입: NoticeRepository, LegacyNoticeImageRepository, FileUtils

### repository (1)

- `LegacyNoticeImageRepository` (interface) repository/LegacyNoticeImageRepository.java · extends/implements JpaRepository · public: findAllByNoticeId, findImagesToDelete, deleteAllByNoticeId, deleteByImageUrls

### custom (2)

- `LegacyNoticeRepositoryCustom` (interface) repository/LegacyNoticeRepositoryCustom.java · public: searchNotices, searchNoticesByCategoryAndKeyword
- `LegacyNoticeRepositoryImpl` repository/LegacyNoticeRepositoryImpl.java · extends/implements LegacyNoticeRepositoryCustom · public: searchNotices, searchNoticesByCategoryAndKeyword · 주입: JPAQueryFactory

### 다른 도메인 참조 (import·주입 기준)

- **legacynotification**: `LegacyNotificationCommandService` ← LegacyNoticeCommandService
- **legacyuser**: `LegacyUserQueryService` ← LegacyNoticeCommandController
- **notice**: `Notice` ← LegacyNoticeCommandService, LegacyNoticeQueryService, LegacyNoticeRepositoryCustom, LegacyNoticeRepositoryImpl; `NoticeCategory` ← LegacyNoticeCommandController, LegacyNoticeQueryController, LegacyNoticeQueryService, LegacyNoticeRepositoryCustom, LegacyNoticeRepositoryImpl; `NoticeImage` ← LegacyNoticeCommandService, LegacyNoticeImageRepository, LegacyNoticeQueryService; `NoticeRepository` ← LegacyNoticeCommandService, LegacyNoticeQueryService
- **notification**: `NotificationCategory` ← LegacyNoticeCommandService; `NotificationType` ← LegacyNoticeCommandService

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

