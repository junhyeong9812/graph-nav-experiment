# partnernotice 도메인

기준 경로: `src/main/java/com/fream/back/domain/partnernotice/` · 파일 8개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/partner/notices` | `PartnerNoticeCommandController#createNotice` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `this#validateCreateRequest`, `PartnerNoticeCommandService#createNotice` |
| PUT | `/partner/notices/{noticeId}` | `PartnerNoticeCommandController#updateNotice` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `this#validateUpdateRequest`, `PartnerNoticeCommandService#updateNotice` |
| DELETE | `/partner/notices/{noticeId}` | `PartnerNoticeCommandController#deleteNotice` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `PartnerNoticeCommandService#deleteNotice` |
| GET | `/partner/notices/{noticeId}` | `PartnerNoticeQueryController#getNotice` | `PartnerNoticeQueryService#getNotice` |
| GET | `/partner/notices/search` | `PartnerNoticeQueryController#searchNotices` | `PartnerNoticeQueryService#searchNotices` |
| GET | `/partner/notices` | `PartnerNoticeQueryController#getNoticesByCategory` | `PartnerNoticeQueryService#getNoticesByCategory`, `PartnerNoticeQueryService#getNotices` |
| GET | `/partner/notices/files/{noticeId}/{fileName}` | `PartnerNoticeQueryController#getNoticeFile` | `PartnerNoticeQueryService#getNoticeFileResource`, `this#determineContentType` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `PartnerNoticeCommandService`
  - `createNotice` → NoticeRepository#save~, this#sendNotificationToAllUsers, this#saveNoticeFiles, this#updateImageUrlsInContent, this#saveNoticeImageEntities, this#createResponseDto
  - `updateNotice` → NoticeRepository#findById~, this#extractImagePathsFromContent, PartnerNoticeImageRepository#findAllByNoticeId, PartnerNoticeImageRepository#deleteAll~, FileUtils#deleteFile, this#saveNoticeFiles, this#updateImageUrlsInContent, this#saveNoticeImageEntities, this#createResponseDto
  - `deleteNotice` → NoticeRepository#findById~, PartnerNoticeImageRepository#findAllByNoticeId, PartnerNoticeImageRepository#deleteAll~, FileUtils#deleteDirectory, NoticeRepository#delete~
  - `saveNoticeFiles` → this#validateFileExtension, FileUtils#saveFile
  - `saveNoticeImageEntities` → this#isVideoFile, PartnerNoticeImageRepository#save~
  - `createResponseDto` → PartnerNoticeImageRepository#findAllByNoticeId
  - `sendNotificationToAllUsers` → PartnerNotificationCommandService#createNotificationForAll
- `PartnerNoticeFileStorageUtil`
  - `saveFiles` → this#saveFileInternal
  - `saveFileInternal` → this#validateFileExtension
- `PartnerNoticeQueryService`
  - `getNotices` → NoticeRepository#findAllOrderByCreatedDateDesc
  - `getNotice` → NoticeRepository#findById~, this#toResponseDto
  - `getNoticesByCategory` → NoticeRepository#findByCategoryOrderByCreatedDateDesc
  - `searchNotices` → NoticeRepository#searchNotices
  - `getFilePreview` → FileUtils#existsFile
  - `toResponseDto` → PartnerNoticeImageRepository#findAllByNoticeId
- `PartnerNoticeRepositoryImpl`
  - `searchNotices` → this#containsKeyword
  - `searchNoticesByCategoryAndKeyword` → this#containsKeyword

## 3. 클래스 목록 (역할별)

### controller (2)

- `PartnerNoticeCommandController` controller/command/PartnerNoticeCommandController.java · public: createNotice, updateNotice, deleteNotice · 주입: PartnerNoticeCommandService, PartnerUserQueryService
- `PartnerNoticeQueryController` controller/query/PartnerNoticeQueryController.java · public: getNotice, searchNotices, getNoticesByCategory, getNoticeFile · 주입: PartnerNoticeQueryService

### service-command (2)

- `PartnerNoticeCommandService` service/command/PartnerNoticeCommandService.java · public: createNotice, updateNotice, deleteNotice · 주입: NoticeRepository, PartnerNoticeImageRepository, PartnerNotificationCommandService, FileUtils
- `PartnerNoticeFileStorageUtil` service/command/PartnerNoticeFileStorageUtil.java · public: saveFiles, deleteFiles, isVideo, fileExists, getFilePath, extractImagePaths, updateImagePaths

### service-query (1)

- `PartnerNoticeQueryService` service/query/PartnerNoticeQueryService.java · public: getNotices, getNotice, getNoticesByCategory, searchNotices, getNoticeFileResource, getFilePreview · 주입: NoticeRepository, PartnerNoticeImageRepository, FileUtils

### repository (1)

- `PartnerNoticeImageRepository` (interface) repository/PartnerNoticeImageRepository.java · extends/implements JpaRepository · public: findAllByNoticeId, findImagesToDelete, deleteAllByNoticeId, deleteByImageUrls

### custom (2)

- `PartnerNoticeRepositoryCustom` (interface) repository/PartnerNoticeRepositoryCustom.java · public: searchNotices, searchNoticesByCategoryAndKeyword
- `PartnerNoticeRepositoryImpl` repository/PartnerNoticeRepositoryImpl.java · extends/implements PartnerNoticeRepositoryCustom · public: searchNotices, searchNoticesByCategoryAndKeyword · 주입: JPAQueryFactory

### 다른 도메인 참조 (import·주입 기준)

- **notice**: `Notice` ← PartnerNoticeCommandService, PartnerNoticeQueryService, PartnerNoticeRepositoryCustom, PartnerNoticeRepositoryImpl; `NoticeCategory` ← PartnerNoticeCommandController, PartnerNoticeQueryController, PartnerNoticeQueryService, PartnerNoticeRepositoryCustom, PartnerNoticeRepositoryImpl; `NoticeImage` ← PartnerNoticeCommandService, PartnerNoticeImageRepository, PartnerNoticeQueryService; `NoticeRepository` ← PartnerNoticeCommandService, PartnerNoticeQueryService
- **notification**: `NotificationCategory` ← PartnerNoticeCommandService; `NotificationType` ← PartnerNoticeCommandService
- **partnernotification**: `PartnerNotificationCommandService` ← PartnerNoticeCommandService
- **partneruser**: `PartnerUserQueryService` ← PartnerNoticeCommandController

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

