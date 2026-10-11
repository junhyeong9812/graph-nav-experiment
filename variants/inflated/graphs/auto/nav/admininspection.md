# admininspection 도메인

기준 경로: `src/main/java/com/fream/back/domain/admininspection/` · 파일 8개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/admin/inspections` | `AdminInspectionCommandController#createStandard` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminInspectionStandardCommandService#createStandard`, `this#clearInspectionCaches` |
| PUT | `/admin/inspections/{id}` | `AdminInspectionCommandController#updateStandard` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminInspectionStandardCommandService#updateStandard`, `this#clearInspectionCaches` |
| DELETE | `/admin/inspections/{id}` | `AdminInspectionCommandController#deleteStandard` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminInspectionStandardCommandService#deleteStandard`, `this#clearInspectionCaches` |
| GET | `/admin/inspections` | `AdminInspectionQueryController#getStandards` | `AdminInspectionStandardQueryService#searchStandards`, `AdminInspectionStandardQueryService#getStandardsByCategory`, `AdminInspectionStandardQueryService#getStandards` |
| GET | `/admin/inspections/{id}` | `AdminInspectionQueryController#getStandard` | `AdminInspectionStandardQueryService#getStandard` |
| GET | `/admin/inspections/files/{inspectionId}/{fileName}` | `AdminInspectionQueryController#getInspectionFile` |  |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `AdminInspectionFileStorageUtil`
  - `saveFiles` → this#saveFileInternal
  - `saveFileInternal` → this#validateFileExtension
- `AdminInspectionStandardCommandService`
  - `createStandard` → this#validateStandardData, InspectionStandardRepository#save~, this#hasFiles, FileUtils#saveFile, this#updateImagePaths, this#saveStandardImages, this#toResponseDto
  - `updateStandard` → this#validateStandardData, InspectionStandardRepository#findById~, AdminInspectionStandardImageRepository#findAllByInspectionStandardId, FileUtils#deleteFile, AdminInspectionStandardImageRepository#delete~, this#hasFiles, FileUtils#saveFile, this#updateImagePaths, this#saveStandardImages, this#toResponseDto
  - `deleteStandard` → InspectionStandardRepository#findById~, AdminInspectionStandardImageRepository#findAllByInspectionStandardId, FileUtils#deleteDirectory, AdminInspectionStandardImageRepository#deleteAll~, InspectionStandardRepository#delete~
  - `saveStandardImages` → AdminInspectionStandardImageRepository#save~
  - `toResponseDto` → AdminInspectionStandardImageRepository#findAllByInspectionStandardId
- `AdminInspectionStandardQueryService`
  - `getStandards` → InspectionStandardRepository#findAllWithPaging
  - `getStandardsByCategory` → InspectionStandardRepository#findByCategory
  - `searchStandards` → InspectionStandardRepository#searchStandards
  - `getStandard` → InspectionStandardRepository#findWithImagesById, this#toResponseDto
  - `toResponseDto` → AdminInspectionStandardImageRepository#findAllByInspectionStandardId

## 3. 클래스 목록 (역할별)

### controller (2)

- `AdminInspectionCommandController` controller/command/AdminInspectionCommandController.java · public: createStandard, updateStandard, deleteStandard · 주입: AdminInspectionStandardCommandService, AdminUserQueryService, CacheManager
- `AdminInspectionQueryController` controller/query/AdminInspectionQueryController.java · public: getStandards, getStandard, getInspectionFile · 주입: AdminInspectionStandardQueryService

### service-command (2)

- `AdminInspectionFileStorageUtil` service/command/AdminInspectionFileStorageUtil.java · public: saveFiles, extractImagePaths, updateImagePaths, deleteFiles, hasFiles
- `AdminInspectionStandardCommandService` service/command/AdminInspectionStandardCommandService.java · public: createStandard, updateStandard, deleteStandard · 주입: InspectionStandardRepository, AdminInspectionStandardImageRepository, FileUtils

### service-query (1)

- `AdminInspectionStandardQueryService` service/query/AdminInspectionStandardQueryService.java · public: getStandards, getStandardsByCategory, searchStandards, getStandard · 주입: InspectionStandardRepository, AdminInspectionStandardImageRepository

### repository (1)

- `AdminInspectionStandardImageRepository` (interface) repository/AdminInspectionStandardImageRepository.java · extends/implements JpaRepository · public: findAllByInspectionStandardId

### custom (2)

- `AdminInspectionStandardRepositoryCustom` (interface) repository/AdminInspectionStandardRepositoryCustom.java · public: searchStandards, searchStandardsByCategoryAndKeyword
- `AdminInspectionStandardRepositoryImpl` repository/AdminInspectionStandardRepositoryImpl.java · extends/implements AdminInspectionStandardRepositoryCustom · public: searchStandards, searchStandardsByCategoryAndKeyword · 주입: JPAQueryFactory

### 다른 도메인 참조 (import·주입 기준)

- **adminuser**: `AdminUserQueryService` ← AdminInspectionCommandController
- **inspection**: `InspectionCategory` ← AdminInspectionQueryController, AdminInspectionStandardCommandService, AdminInspectionStandardQueryService, AdminInspectionStandardRepositoryCustom, AdminInspectionStandardRepositoryImpl; `InspectionStandard` ← AdminInspectionStandardCommandService, AdminInspectionStandardQueryService, AdminInspectionStandardRepositoryCustom, AdminInspectionStandardRepositoryImpl; `InspectionStandardImage` ← AdminInspectionStandardCommandService, AdminInspectionStandardImageRepository; `InspectionStandardRepository` ← AdminInspectionStandardCommandService, AdminInspectionStandardQueryService

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

