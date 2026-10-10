# notice 도메인

기준 경로: `src/main/java/com/fream/back/domain/notice/` · 파일 20개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/notices` | `NoticeCommandController#createNotice` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `this#validateCreateRequest`, `NoticeCommandService#createNotice` |
| PUT | `/notices/{noticeId}` | `NoticeCommandController#updateNotice` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `this#validateUpdateRequest`, `NoticeCommandService#updateNotice` |
| DELETE | `/notices/{noticeId}` | `NoticeCommandController#deleteNotice` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `NoticeCommandService#deleteNotice` |
| GET | `/notices/{noticeId}` | `NoticeQueryController#getNotice` | `NoticeQueryService#getNotice` |
| GET | `/notices/search` | `NoticeQueryController#searchNotices` | `NoticeQueryService#searchNotices` |
| GET | `/notices` | `NoticeQueryController#getNoticesByCategory` | `NoticeQueryService#getNoticesByCategory`, `NoticeQueryService#getNotices` |
| GET | `/notices/files/{noticeId}/{fileName}` | `NoticeQueryController#getNoticeFile` | `NoticeQueryService#getNoticeFileResource`, `this#determineContentType` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `NoticeCommandService`
  - `createNotice` → NoticeRepository#save~, this#sendNotificationToAllUsers, this#saveNoticeFiles, this#updateImageUrlsInContent, this#saveNoticeImageEntities, this#createResponseDto
  - `updateNotice` → NoticeRepository#findById~, this#extractImagePathsFromContent, NoticeImageRepository#findAllByNoticeId, NoticeImageRepository#deleteAll~, FileUtils#deleteFile, this#saveNoticeFiles, this#updateImageUrlsInContent, this#saveNoticeImageEntities, this#createResponseDto
  - `deleteNotice` → NoticeRepository#findById~, NoticeImageRepository#findAllByNoticeId, NoticeImageRepository#deleteAll~, FileUtils#deleteDirectory, NoticeRepository#delete~
  - `saveNoticeFiles` → this#validateFileExtension, FileUtils#saveFile
  - `saveNoticeImageEntities` → this#isVideoFile, NoticeImageRepository#save~
  - `createResponseDto` → NoticeImageRepository#findAllByNoticeId
  - `sendNotificationToAllUsers` → NotificationCommandService#createNotificationForAll
- `NoticeFileStorageUtil`
  - `saveFiles` → this#saveFileInternal
  - `saveFileInternal` → this#validateFileExtension
- `NoticeQueryService`
  - `getNotices` → NoticeRepository#findAllOrderByCreatedDateDesc
  - `getNotice` → NoticeRepository#findById~, this#toResponseDto
  - `getNoticesByCategory` → NoticeRepository#findByCategoryOrderByCreatedDateDesc
  - `searchNotices` → NoticeRepository#searchNotices
  - `getFilePreview` → FileUtils#existsFile
  - `toResponseDto` → NoticeImageRepository#findAllByNoticeId
- `NoticeRepositoryImpl`
  - `searchNotices` → this#containsKeyword
  - `searchNoticesByCategoryAndKeyword` → this#containsKeyword

## 3. 클래스 목록 (역할별)

### controller (2)

- `NoticeCommandController` controller/command/NoticeCommandController.java · public: createNotice, updateNotice, deleteNotice · 주입: NoticeCommandService, UserQueryService
- `NoticeQueryController` controller/query/NoticeQueryController.java · public: getNotice, searchNotices, getNoticesByCategory, getNoticeFile · 주입: NoticeQueryService

### service-command (2)

- `NoticeCommandService` service/command/NoticeCommandService.java · public: createNotice, updateNotice, deleteNotice · 주입: NoticeRepository, NoticeImageRepository, NotificationCommandService, FileUtils
- `NoticeFileStorageUtil` service/command/NoticeFileStorageUtil.java · public: saveFiles, deleteFiles, isVideo, fileExists, getFilePath, extractImagePaths, updateImagePaths

### service-query (1)

- `NoticeQueryService` service/query/NoticeQueryService.java · public: getNotices, getNotice, getNoticesByCategory, searchNotices, getNoticeFileResource, getFilePreview · 주입: NoticeRepository, NoticeImageRepository, FileUtils

### repository (2)

- `NoticeImageRepository` (interface) repository/NoticeImageRepository.java · extends/implements JpaRepository · public: findAllByNoticeId, findImagesToDelete, deleteAllByNoticeId, deleteByImageUrls
- `NoticeRepository` (interface) repository/NoticeRepository.java · extends/implements JpaRepository, NoticeRepositoryCustom · public: findWithImagesById, findByCategory, findAllOrderByCreatedDateDesc, findByCategoryOrderByCreatedDateDesc

### custom (2)

- `NoticeRepositoryCustom` (interface) repository/NoticeRepositoryCustom.java · public: searchNotices, searchNoticesByCategoryAndKeyword
- `NoticeRepositoryImpl` repository/NoticeRepositoryImpl.java · extends/implements NoticeRepositoryCustom · public: searchNotices, searchNoticesByCategoryAndKeyword · 주입: JPAQueryFactory

### entity (3)

- `Notice` entity/Notice.java · extends/implements BaseTimeEntity · public: addImage, removeImage, update, updateContent
- `NoticeCategory` (enum) entity/NoticeCategory.java · public: fromString, toString
- `NoticeImage` entity/NoticeImage.java · extends/implements BaseTimeEntity · public: setNotice, getFileName, getDirectory

### dto (3)

- `NoticeCreateRequestDto` dto/NoticeCreateRequestDto.java
- `NoticeResponseDto` dto/NoticeResponseDto.java
- `NoticeUpdateRequestDto` dto/NoticeUpdateRequestDto.java

### exception (5)

- `NoticeErrorCode` exception/NoticeErrorCode.java
- `NoticeException` exception/NoticeException.java
- `NoticeFileException` exception/NoticeFileException.java
- `NoticeNotFoundException` exception/NoticeNotFoundException.java
- `NoticePermissionException` exception/NoticePermissionException.java

### 다른 도메인 참조 (import·주입 기준)

- **notification**: `NotificationCategory` ← NoticeCommandService; `NotificationCommandService` ← NoticeCommandService; `NotificationType` ← NoticeCommandService
- **user**: `UserQueryService` ← NoticeCommandController

### 이 도메인을 참조하는 다른 도메인

- **global**: Notice, NoticeCategory, NoticeRepository

## 4. JPA 엔티티 연관

- `Notice` @Entity @Table(indexes = { @Index(name = "idx_notice_category", columnList = "category"), @Index(name = "idx_notice_created_date", columnList = "created_date") }) · extends BaseTimeEntity
  - images: @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true) → `NoticeImage`
- `NoticeImage` @Entity @Table(indexes = { @Index(name = "idx_notice_image_notice_id", columnList = "notice_id") }) · extends BaseTimeEntity
  - notice: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "notice_id") → `Notice`

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

