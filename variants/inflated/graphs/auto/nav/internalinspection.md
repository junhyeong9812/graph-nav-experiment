# internalinspection 도메인

기준 경로: `src/main/java/com/fream/back/domain/internalinspection/` · 파일 8개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/internal/inspections` | `InternalInspectionCommandController#createStandard` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalInspectionStandardCommandService#createStandard`, `this#clearInspectionCaches` |
| PUT | `/internal/inspections/{id}` | `InternalInspectionCommandController#updateStandard` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalInspectionStandardCommandService#updateStandard`, `this#clearInspectionCaches` |
| DELETE | `/internal/inspections/{id}` | `InternalInspectionCommandController#deleteStandard` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalInspectionStandardCommandService#deleteStandard`, `this#clearInspectionCaches` |
| GET | `/internal/inspections` | `InternalInspectionQueryController#getStandards` | `InternalInspectionStandardQueryService#searchStandards`, `InternalInspectionStandardQueryService#getStandardsByCategory`, `InternalInspectionStandardQueryService#getStandards` |
| GET | `/internal/inspections/{id}` | `InternalInspectionQueryController#getStandard` | `InternalInspectionStandardQueryService#getStandard` |
| GET | `/internal/inspections/files/{inspectionId}/{fileName}` | `InternalInspectionQueryController#getInspectionFile` |  |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `InternalInspectionFileStorageUtil`
  - `saveFiles` → this#saveFileInternal
  - `saveFileInternal` → this#validateFileExtension
- `InternalInspectionStandardCommandService`
  - `createStandard` → this#validateStandardData, InspectionStandardRepository#save~, this#hasFiles, FileUtils#saveFile, this#updateImagePaths, this#saveStandardImages, this#toResponseDto
  - `updateStandard` → this#validateStandardData, InspectionStandardRepository#findById~, InternalInspectionStandardImageRepository#findAllByInspectionStandardId, FileUtils#deleteFile, InternalInspectionStandardImageRepository#delete~, this#hasFiles, FileUtils#saveFile, this#updateImagePaths, this#saveStandardImages, this#toResponseDto
  - `deleteStandard` → InspectionStandardRepository#findById~, InternalInspectionStandardImageRepository#findAllByInspectionStandardId, FileUtils#deleteDirectory, InternalInspectionStandardImageRepository#deleteAll~, InspectionStandardRepository#delete~
  - `saveStandardImages` → InternalInspectionStandardImageRepository#save~
  - `toResponseDto` → InternalInspectionStandardImageRepository#findAllByInspectionStandardId
- `InternalInspectionStandardQueryService`
  - `getStandards` → InspectionStandardRepository#findAllWithPaging
  - `getStandardsByCategory` → InspectionStandardRepository#findByCategory
  - `searchStandards` → InspectionStandardRepository#searchStandards
  - `getStandard` → InspectionStandardRepository#findWithImagesById, this#toResponseDto
  - `toResponseDto` → InternalInspectionStandardImageRepository#findAllByInspectionStandardId

## 3. 클래스 목록 (역할별)

### controller (2)

- `InternalInspectionCommandController` controller/command/InternalInspectionCommandController.java · public: createStandard, updateStandard, deleteStandard · 주입: InternalInspectionStandardCommandService, InternalUserQueryService, CacheManager
- `InternalInspectionQueryController` controller/query/InternalInspectionQueryController.java · public: getStandards, getStandard, getInspectionFile · 주입: InternalInspectionStandardQueryService

### service-command (2)

- `InternalInspectionFileStorageUtil` service/command/InternalInspectionFileStorageUtil.java · public: saveFiles, extractImagePaths, updateImagePaths, deleteFiles, hasFiles
- `InternalInspectionStandardCommandService` service/command/InternalInspectionStandardCommandService.java · public: createStandard, updateStandard, deleteStandard · 주입: InspectionStandardRepository, InternalInspectionStandardImageRepository, FileUtils

### service-query (1)

- `InternalInspectionStandardQueryService` service/query/InternalInspectionStandardQueryService.java · public: getStandards, getStandardsByCategory, searchStandards, getStandard · 주입: InspectionStandardRepository, InternalInspectionStandardImageRepository

### repository (1)

- `InternalInspectionStandardImageRepository` (interface) repository/InternalInspectionStandardImageRepository.java · extends/implements JpaRepository · public: findAllByInspectionStandardId

### custom (2)

- `InternalInspectionStandardRepositoryCustom` (interface) repository/InternalInspectionStandardRepositoryCustom.java · public: searchStandards, searchStandardsByCategoryAndKeyword
- `InternalInspectionStandardRepositoryImpl` repository/InternalInspectionStandardRepositoryImpl.java · extends/implements InternalInspectionStandardRepositoryCustom · public: searchStandards, searchStandardsByCategoryAndKeyword · 주입: JPAQueryFactory

### 다른 도메인 참조 (import·주입 기준)

- **inspection**: `InspectionCategory` ← InternalInspectionQueryController, InternalInspectionStandardCommandService, InternalInspectionStandardQueryService, InternalInspectionStandardRepositoryCustom, InternalInspectionStandardRepositoryImpl; `InspectionStandard` ← InternalInspectionStandardCommandService, InternalInspectionStandardQueryService, InternalInspectionStandardRepositoryCustom, InternalInspectionStandardRepositoryImpl; `InspectionStandardImage` ← InternalInspectionStandardCommandService, InternalInspectionStandardImageRepository; `InspectionStandardRepository` ← InternalInspectionStandardCommandService, InternalInspectionStandardQueryService
- **internaluser**: `InternalUserQueryService` ← InternalInspectionCommandController

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

