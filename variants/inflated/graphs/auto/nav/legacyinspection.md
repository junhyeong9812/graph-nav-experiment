# legacyinspection 도메인

기준 경로: `src/main/java/com/fream/back/domain/legacyinspection/` · 파일 8개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/legacy/inspections` | `LegacyInspectionCommandController#createStandard` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyInspectionStandardCommandService#createStandard`, `this#clearInspectionCaches` |
| PUT | `/legacy/inspections/{id}` | `LegacyInspectionCommandController#updateStandard` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyInspectionStandardCommandService#updateStandard`, `this#clearInspectionCaches` |
| DELETE | `/legacy/inspections/{id}` | `LegacyInspectionCommandController#deleteStandard` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyInspectionStandardCommandService#deleteStandard`, `this#clearInspectionCaches` |
| GET | `/legacy/inspections` | `LegacyInspectionQueryController#getStandards` | `LegacyInspectionStandardQueryService#searchStandards`, `LegacyInspectionStandardQueryService#getStandardsByCategory`, `LegacyInspectionStandardQueryService#getStandards` |
| GET | `/legacy/inspections/{id}` | `LegacyInspectionQueryController#getStandard` | `LegacyInspectionStandardQueryService#getStandard` |
| GET | `/legacy/inspections/files/{inspectionId}/{fileName}` | `LegacyInspectionQueryController#getInspectionFile` |  |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `LegacyInspectionFileStorageUtil`
  - `saveFiles` → this#saveFileInternal
  - `saveFileInternal` → this#validateFileExtension
- `LegacyInspectionStandardCommandService`
  - `createStandard` → this#validateStandardData, InspectionStandardRepository#save~, this#hasFiles, FileUtils#saveFile, this#updateImagePaths, this#saveStandardImages, this#toResponseDto
  - `updateStandard` → this#validateStandardData, InspectionStandardRepository#findById~, LegacyInspectionStandardImageRepository#findAllByInspectionStandardId, FileUtils#deleteFile, LegacyInspectionStandardImageRepository#delete~, this#hasFiles, FileUtils#saveFile, this#updateImagePaths, this#saveStandardImages, this#toResponseDto
  - `deleteStandard` → InspectionStandardRepository#findById~, LegacyInspectionStandardImageRepository#findAllByInspectionStandardId, FileUtils#deleteDirectory, LegacyInspectionStandardImageRepository#deleteAll~, InspectionStandardRepository#delete~
  - `saveStandardImages` → LegacyInspectionStandardImageRepository#save~
  - `toResponseDto` → LegacyInspectionStandardImageRepository#findAllByInspectionStandardId
- `LegacyInspectionStandardQueryService`
  - `getStandards` → InspectionStandardRepository#findAllWithPaging
  - `getStandardsByCategory` → InspectionStandardRepository#findByCategory
  - `searchStandards` → InspectionStandardRepository#searchStandards
  - `getStandard` → InspectionStandardRepository#findWithImagesById, this#toResponseDto
  - `toResponseDto` → LegacyInspectionStandardImageRepository#findAllByInspectionStandardId

## 3. 클래스 목록 (역할별)

### controller (2)

- `LegacyInspectionCommandController` controller/command/LegacyInspectionCommandController.java · public: createStandard, updateStandard, deleteStandard · 주입: LegacyInspectionStandardCommandService, LegacyUserQueryService, CacheManager
- `LegacyInspectionQueryController` controller/query/LegacyInspectionQueryController.java · public: getStandards, getStandard, getInspectionFile · 주입: LegacyInspectionStandardQueryService

### service-command (2)

- `LegacyInspectionFileStorageUtil` service/command/LegacyInspectionFileStorageUtil.java · public: saveFiles, extractImagePaths, updateImagePaths, deleteFiles, hasFiles
- `LegacyInspectionStandardCommandService` service/command/LegacyInspectionStandardCommandService.java · public: createStandard, updateStandard, deleteStandard · 주입: InspectionStandardRepository, LegacyInspectionStandardImageRepository, FileUtils

### service-query (1)

- `LegacyInspectionStandardQueryService` service/query/LegacyInspectionStandardQueryService.java · public: getStandards, getStandardsByCategory, searchStandards, getStandard · 주입: InspectionStandardRepository, LegacyInspectionStandardImageRepository

### repository (1)

- `LegacyInspectionStandardImageRepository` (interface) repository/LegacyInspectionStandardImageRepository.java · extends/implements JpaRepository · public: findAllByInspectionStandardId

### custom (2)

- `LegacyInspectionStandardRepositoryCustom` (interface) repository/LegacyInspectionStandardRepositoryCustom.java · public: searchStandards, searchStandardsByCategoryAndKeyword
- `LegacyInspectionStandardRepositoryImpl` repository/LegacyInspectionStandardRepositoryImpl.java · extends/implements LegacyInspectionStandardRepositoryCustom · public: searchStandards, searchStandardsByCategoryAndKeyword · 주입: JPAQueryFactory

### 다른 도메인 참조 (import·주입 기준)

- **inspection**: `InspectionCategory` ← LegacyInspectionQueryController, LegacyInspectionStandardCommandService, LegacyInspectionStandardQueryService, LegacyInspectionStandardRepositoryCustom, LegacyInspectionStandardRepositoryImpl; `InspectionStandard` ← LegacyInspectionStandardCommandService, LegacyInspectionStandardQueryService, LegacyInspectionStandardRepositoryCustom, LegacyInspectionStandardRepositoryImpl; `InspectionStandardImage` ← LegacyInspectionStandardCommandService, LegacyInspectionStandardImageRepository; `InspectionStandardRepository` ← LegacyInspectionStandardCommandService, LegacyInspectionStandardQueryService
- **legacyuser**: `LegacyUserQueryService` ← LegacyInspectionCommandController

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

