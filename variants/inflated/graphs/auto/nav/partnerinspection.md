# partnerinspection 도메인

기준 경로: `src/main/java/com/fream/back/domain/partnerinspection/` · 파일 8개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/partner/inspections` | `PartnerInspectionCommandController#createStandard` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerInspectionStandardCommandService#createStandard`, `this#clearInspectionCaches` |
| PUT | `/partner/inspections/{id}` | `PartnerInspectionCommandController#updateStandard` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerInspectionStandardCommandService#updateStandard`, `this#clearInspectionCaches` |
| DELETE | `/partner/inspections/{id}` | `PartnerInspectionCommandController#deleteStandard` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerInspectionStandardCommandService#deleteStandard`, `this#clearInspectionCaches` |
| GET | `/partner/inspections` | `PartnerInspectionQueryController#getStandards` | `PartnerInspectionStandardQueryService#searchStandards`, `PartnerInspectionStandardQueryService#getStandardsByCategory`, `PartnerInspectionStandardQueryService#getStandards` |
| GET | `/partner/inspections/{id}` | `PartnerInspectionQueryController#getStandard` | `PartnerInspectionStandardQueryService#getStandard` |
| GET | `/partner/inspections/files/{inspectionId}/{fileName}` | `PartnerInspectionQueryController#getInspectionFile` |  |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `PartnerInspectionFileStorageUtil`
  - `saveFiles` → this#saveFileInternal
  - `saveFileInternal` → this#validateFileExtension
- `PartnerInspectionStandardCommandService`
  - `createStandard` → this#validateStandardData, InspectionStandardRepository#save~, this#hasFiles, FileUtils#saveFile, this#updateImagePaths, this#saveStandardImages, this#toResponseDto
  - `updateStandard` → this#validateStandardData, InspectionStandardRepository#findById~, PartnerInspectionStandardImageRepository#findAllByInspectionStandardId, FileUtils#deleteFile, PartnerInspectionStandardImageRepository#delete~, this#hasFiles, FileUtils#saveFile, this#updateImagePaths, this#saveStandardImages, this#toResponseDto
  - `deleteStandard` → InspectionStandardRepository#findById~, PartnerInspectionStandardImageRepository#findAllByInspectionStandardId, FileUtils#deleteDirectory, PartnerInspectionStandardImageRepository#deleteAll~, InspectionStandardRepository#delete~
  - `saveStandardImages` → PartnerInspectionStandardImageRepository#save~
  - `toResponseDto` → PartnerInspectionStandardImageRepository#findAllByInspectionStandardId
- `PartnerInspectionStandardQueryService`
  - `getStandards` → InspectionStandardRepository#findAllWithPaging
  - `getStandardsByCategory` → InspectionStandardRepository#findByCategory
  - `searchStandards` → InspectionStandardRepository#searchStandards
  - `getStandard` → InspectionStandardRepository#findWithImagesById, this#toResponseDto
  - `toResponseDto` → PartnerInspectionStandardImageRepository#findAllByInspectionStandardId

## 3. 클래스 목록 (역할별)

### controller (2)

- `PartnerInspectionCommandController` controller/command/PartnerInspectionCommandController.java · public: createStandard, updateStandard, deleteStandard · 주입: PartnerInspectionStandardCommandService, PartnerUserQueryService, CacheManager
- `PartnerInspectionQueryController` controller/query/PartnerInspectionQueryController.java · public: getStandards, getStandard, getInspectionFile · 주입: PartnerInspectionStandardQueryService

### service-command (2)

- `PartnerInspectionFileStorageUtil` service/command/PartnerInspectionFileStorageUtil.java · public: saveFiles, extractImagePaths, updateImagePaths, deleteFiles, hasFiles
- `PartnerInspectionStandardCommandService` service/command/PartnerInspectionStandardCommandService.java · public: createStandard, updateStandard, deleteStandard · 주입: InspectionStandardRepository, PartnerInspectionStandardImageRepository, FileUtils

### service-query (1)

- `PartnerInspectionStandardQueryService` service/query/PartnerInspectionStandardQueryService.java · public: getStandards, getStandardsByCategory, searchStandards, getStandard · 주입: InspectionStandardRepository, PartnerInspectionStandardImageRepository

### repository (1)

- `PartnerInspectionStandardImageRepository` (interface) repository/PartnerInspectionStandardImageRepository.java · extends/implements JpaRepository · public: findAllByInspectionStandardId

### custom (2)

- `PartnerInspectionStandardRepositoryCustom` (interface) repository/PartnerInspectionStandardRepositoryCustom.java · public: searchStandards, searchStandardsByCategoryAndKeyword
- `PartnerInspectionStandardRepositoryImpl` repository/PartnerInspectionStandardRepositoryImpl.java · extends/implements PartnerInspectionStandardRepositoryCustom · public: searchStandards, searchStandardsByCategoryAndKeyword · 주입: JPAQueryFactory

### 다른 도메인 참조 (import·주입 기준)

- **inspection**: `InspectionCategory` ← PartnerInspectionQueryController, PartnerInspectionStandardCommandService, PartnerInspectionStandardQueryService, PartnerInspectionStandardRepositoryCustom, PartnerInspectionStandardRepositoryImpl; `InspectionStandard` ← PartnerInspectionStandardCommandService, PartnerInspectionStandardQueryService, PartnerInspectionStandardRepositoryCustom, PartnerInspectionStandardRepositoryImpl; `InspectionStandardImage` ← PartnerInspectionStandardCommandService, PartnerInspectionStandardImageRepository; `InspectionStandardRepository` ← PartnerInspectionStandardCommandService, PartnerInspectionStandardQueryService
- **partneruser**: `PartnerUserQueryService` ← PartnerInspectionCommandController

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

