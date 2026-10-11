# warehouseStorage 도메인

기준 경로: `src/main/java/com/fream/back/domain/warehouseStorage/` · 파일 20개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/notices` | `WarehouseStorageCommandController#createNotice` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `this#validateCreateRequest`, `WarehouseStorageCommandService#createNotice` |
| PUT | `/notices/{noticeId}` | `WarehouseStorageCommandController#updateNotice` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `this#validateUpdateRequest`, `WarehouseStorageCommandService#updateNotice` |
| DELETE | `/notices/{noticeId}` | `WarehouseStorageCommandController#deleteNotice` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `WarehouseStorageCommandService#deleteNotice` |
| GET | `/notices/{noticeId}` | `WarehouseStorageQueryController#getNotice` | `WarehouseStorageQueryService#getNotice` |
| GET | `/notices/search` | `WarehouseStorageQueryController#searchNotices` | `WarehouseStorageQueryService#searchNotices` |
| GET | `/notices` | `WarehouseStorageQueryController#getNoticesByCategory` | `WarehouseStorageQueryService#getNoticesByCategory`, `WarehouseStorageQueryService#getNotices` |
| GET | `/notices/files/{noticeId}/{fileName}` | `WarehouseStorageQueryController#getNoticeFile` | `WarehouseStorageQueryService#getNoticeFileResource`, `this#determineContentType` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `WarehouseStorageCommandService`
  - `createNotice` → WarehouseStorageRepository#save~, this#sendNotificationToAllUsers, this#saveNoticeFiles, this#updateImageUrlsInContent, this#saveNoticeImageEntities, this#createResponseDto
  - `updateNotice` → WarehouseStorageRepository#findById~, this#extractImagePathsFromContent, WarehouseStorageImageRepository#findAllByNoticeId, WarehouseStorageImageRepository#deleteAll~, FileUtils#deleteFile, this#saveNoticeFiles, this#updateImageUrlsInContent, this#saveNoticeImageEntities, this#createResponseDto
  - `deleteNotice` → WarehouseStorageRepository#findById~, WarehouseStorageImageRepository#findAllByNoticeId, WarehouseStorageImageRepository#deleteAll~, FileUtils#deleteDirectory, WarehouseStorageRepository#delete~
  - `saveNoticeFiles` → this#validateFileExtension, FileUtils#saveFile
  - `saveNoticeImageEntities` → this#isVideoFile, WarehouseStorageImageRepository#save~
  - `createResponseDto` → WarehouseStorageImageRepository#findAllByNoticeId
  - `sendNotificationToAllUsers` → PaymentCommandService#createNotificationForAll
- `WarehouseStorageFileStorageUtil`
  - `saveFiles` → this#saveFileInternal
  - `saveFileInternal` → this#validateFileExtension
- `WarehouseStorageQueryService`
  - `getNotices` → WarehouseStorageRepository#findAllOrderByCreatedDateDesc
  - `getNotice` → WarehouseStorageRepository#findById~, this#toResponseDto
  - `getNoticesByCategory` → WarehouseStorageRepository#findByCategoryOrderByCreatedDateDesc
  - `searchNotices` → WarehouseStorageRepository#searchNotices
  - `getFilePreview` → FileUtils#existsFile
  - `toResponseDto` → WarehouseStorageImageRepository#findAllByNoticeId
- `WarehouseStorageRepositoryImpl`
  - `searchNotices` → this#containsKeyword
  - `searchNoticesByCategoryAndKeyword` → this#containsKeyword

## 3. 클래스 목록 (역할별)

### controller (2)

- `WarehouseStorageCommandController` controller/command/WarehouseStorageCommandController.java · public: createNotice, updateNotice, deleteNotice · 주입: WarehouseStorageCommandService, ProductQueryService
- `WarehouseStorageQueryController` controller/query/WarehouseStorageQueryController.java · public: getNotice, searchNotices, getNoticesByCategory, getNoticeFile · 주입: WarehouseStorageQueryService

### service-command (2)

- `WarehouseStorageCommandService` service/command/WarehouseStorageCommandService.java · public: createNotice, updateNotice, deleteNotice · 주입: WarehouseStorageRepository, WarehouseStorageImageRepository, PaymentCommandService, FileUtils
- `WarehouseStorageFileStorageUtil` service/command/WarehouseStorageFileStorageUtil.java · public: saveFiles, deleteFiles, isVideo, fileExists, getFilePath, extractImagePaths, updateImagePaths

### service-query (1)

- `WarehouseStorageQueryService` service/query/WarehouseStorageQueryService.java · public: getNotices, getNotice, getNoticesByCategory, searchNotices, getNoticeFileResource, getFilePreview · 주입: WarehouseStorageRepository, WarehouseStorageImageRepository, FileUtils

### repository (2)

- `WarehouseStorageImageRepository` (interface) repository/WarehouseStorageImageRepository.java · extends/implements JpaRepository · public: findAllByNoticeId, findImagesToDelete, deleteAllByNoticeId, deleteByImageUrls
- `WarehouseStorageRepository` (interface) repository/WarehouseStorageRepository.java · extends/implements JpaRepository, WarehouseStorageRepositoryCustom · public: findWithImagesById, findByCategory, findAllOrderByCreatedDateDesc, findByCategoryOrderByCreatedDateDesc

### custom (2)

- `WarehouseStorageRepositoryCustom` (interface) repository/WarehouseStorageRepositoryCustom.java · public: searchNotices, searchNoticesByCategoryAndKeyword
- `WarehouseStorageRepositoryImpl` repository/WarehouseStorageRepositoryImpl.java · extends/implements WarehouseStorageRepositoryCustom · public: searchNotices, searchNoticesByCategoryAndKeyword · 주입: JPAQueryFactory

### entity (3)

- `WarehouseStorage` entity/WarehouseStorage.java · extends/implements BaseTimeEntity · public: addImage, removeImage, update, updateContent
- `WarehouseStorageCategory` (enum) entity/WarehouseStorageCategory.java · public: fromString, toString
- `WarehouseStorageImage` entity/WarehouseStorageImage.java · extends/implements BaseTimeEntity · public: setNotice, getFileName, getDirectory

### dto (3)

- `WarehouseStorageCreateRequestDto` dto/WarehouseStorageCreateRequestDto.java
- `WarehouseStorageResponseDto` dto/WarehouseStorageResponseDto.java
- `WarehouseStorageUpdateRequestDto` dto/WarehouseStorageUpdateRequestDto.java

### exception (5)

- `WarehouseStorageErrorCode` exception/WarehouseStorageErrorCode.java
- `WarehouseStorageException` exception/WarehouseStorageException.java
- `WarehouseStorageFileException` exception/WarehouseStorageFileException.java
- `WarehouseStorageNotFoundException` exception/WarehouseStorageNotFoundException.java
- `WarehouseStoragePermissionException` exception/WarehouseStoragePermissionException.java

### 다른 도메인 참조 (import·주입 기준)

- **payment**: `PaymentCategory` ← WarehouseStorageCommandService; `PaymentCommandService` ← WarehouseStorageCommandService; `PaymentType` ← WarehouseStorageCommandService
- **product**: `ProductQueryService` ← WarehouseStorageCommandController

### 이 도메인을 참조하는 다른 도메인

- **global**: WarehouseStorage, WarehouseStorageCategory, WarehouseStorageRepository

## 4. JPA 엔티티 연관

- `WarehouseStorage` @Entity @Table(name = "notice", indexes = { @Index(name = "idx_notice_category", columnList = "category"), @Index(name = "idx_notice_created_date", columnList = "created_date") }) · extends BaseTimeEntity
  - images: @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true) → `WarehouseStorageImage`
- `WarehouseStorageImage` @Entity @Table(name = "notice_image", indexes = { @Index(name = "idx_notice_image_notice_id", columnList = "notice_id") }) · extends BaseTimeEntity
  - notice: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "notice_id") → `WarehouseStorage`

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

