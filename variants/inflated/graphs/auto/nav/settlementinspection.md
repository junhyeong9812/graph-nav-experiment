# settlementinspection 도메인

기준 경로: `src/main/java/com/fream/back/domain/settlementinspection/` · 파일 8개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/settlement/inspections` | `SettlementInspectionCommandController#createStandard` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementInspectionStandardCommandService#createStandard`, `this#clearInspectionCaches` |
| PUT | `/settlement/inspections/{id}` | `SettlementInspectionCommandController#updateStandard` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementInspectionStandardCommandService#updateStandard`, `this#clearInspectionCaches` |
| DELETE | `/settlement/inspections/{id}` | `SettlementInspectionCommandController#deleteStandard` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementInspectionStandardCommandService#deleteStandard`, `this#clearInspectionCaches` |
| GET | `/settlement/inspections` | `SettlementInspectionQueryController#getStandards` | `SettlementInspectionStandardQueryService#searchStandards`, `SettlementInspectionStandardQueryService#getStandardsByCategory`, `SettlementInspectionStandardQueryService#getStandards` |
| GET | `/settlement/inspections/{id}` | `SettlementInspectionQueryController#getStandard` | `SettlementInspectionStandardQueryService#getStandard` |
| GET | `/settlement/inspections/files/{inspectionId}/{fileName}` | `SettlementInspectionQueryController#getInspectionFile` |  |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `SettlementInspectionFileStorageUtil`
  - `saveFiles` → this#saveFileInternal
  - `saveFileInternal` → this#validateFileExtension
- `SettlementInspectionStandardCommandService`
  - `createStandard` → this#validateStandardData, InspectionStandardRepository#save~, this#hasFiles, FileUtils#saveFile, this#updateImagePaths, this#saveStandardImages, this#toResponseDto
  - `updateStandard` → this#validateStandardData, InspectionStandardRepository#findById~, SettlementInspectionStandardImageRepository#findAllByInspectionStandardId, FileUtils#deleteFile, SettlementInspectionStandardImageRepository#delete~, this#hasFiles, FileUtils#saveFile, this#updateImagePaths, this#saveStandardImages, this#toResponseDto
  - `deleteStandard` → InspectionStandardRepository#findById~, SettlementInspectionStandardImageRepository#findAllByInspectionStandardId, FileUtils#deleteDirectory, SettlementInspectionStandardImageRepository#deleteAll~, InspectionStandardRepository#delete~
  - `saveStandardImages` → SettlementInspectionStandardImageRepository#save~
  - `toResponseDto` → SettlementInspectionStandardImageRepository#findAllByInspectionStandardId
- `SettlementInspectionStandardQueryService`
  - `getStandards` → InspectionStandardRepository#findAllWithPaging
  - `getStandardsByCategory` → InspectionStandardRepository#findByCategory
  - `searchStandards` → InspectionStandardRepository#searchStandards
  - `getStandard` → InspectionStandardRepository#findWithImagesById, this#toResponseDto
  - `toResponseDto` → SettlementInspectionStandardImageRepository#findAllByInspectionStandardId

## 3. 클래스 목록 (역할별)

### controller (2)

- `SettlementInspectionCommandController` controller/command/SettlementInspectionCommandController.java · public: createStandard, updateStandard, deleteStandard · 주입: SettlementInspectionStandardCommandService, SettlementUserQueryService, CacheManager
- `SettlementInspectionQueryController` controller/query/SettlementInspectionQueryController.java · public: getStandards, getStandard, getInspectionFile · 주입: SettlementInspectionStandardQueryService

### service-command (2)

- `SettlementInspectionFileStorageUtil` service/command/SettlementInspectionFileStorageUtil.java · public: saveFiles, extractImagePaths, updateImagePaths, deleteFiles, hasFiles
- `SettlementInspectionStandardCommandService` service/command/SettlementInspectionStandardCommandService.java · public: createStandard, updateStandard, deleteStandard · 주입: InspectionStandardRepository, SettlementInspectionStandardImageRepository, FileUtils

### service-query (1)

- `SettlementInspectionStandardQueryService` service/query/SettlementInspectionStandardQueryService.java · public: getStandards, getStandardsByCategory, searchStandards, getStandard · 주입: InspectionStandardRepository, SettlementInspectionStandardImageRepository

### repository (1)

- `SettlementInspectionStandardImageRepository` (interface) repository/SettlementInspectionStandardImageRepository.java · extends/implements JpaRepository · public: findAllByInspectionStandardId

### custom (2)

- `SettlementInspectionStandardRepositoryCustom` (interface) repository/SettlementInspectionStandardRepositoryCustom.java · public: searchStandards, searchStandardsByCategoryAndKeyword
- `SettlementInspectionStandardRepositoryImpl` repository/SettlementInspectionStandardRepositoryImpl.java · extends/implements SettlementInspectionStandardRepositoryCustom · public: searchStandards, searchStandardsByCategoryAndKeyword · 주입: JPAQueryFactory

### 다른 도메인 참조 (import·주입 기준)

- **inspection**: `InspectionCategory` ← SettlementInspectionQueryController, SettlementInspectionStandardCommandService, SettlementInspectionStandardQueryService, SettlementInspectionStandardRepositoryCustom, SettlementInspectionStandardRepositoryImpl; `InspectionStandard` ← SettlementInspectionStandardCommandService, SettlementInspectionStandardQueryService, SettlementInspectionStandardRepositoryCustom, SettlementInspectionStandardRepositoryImpl; `InspectionStandardImage` ← SettlementInspectionStandardCommandService, SettlementInspectionStandardImageRepository; `InspectionStandardRepository` ← SettlementInspectionStandardCommandService, SettlementInspectionStandardQueryService
- **settlementuser**: `SettlementUserQueryService` ← SettlementInspectionCommandController

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

