# inspection 도메인

기준 경로: `src/main/java/com/fream/back/domain/inspection/` · 파일 20개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/inspections` | `InspectionCommandController#createStandard` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `InspectionStandardCommandService#createStandard`, `this#clearInspectionCaches` |
| PUT | `/inspections/{id}` | `InspectionCommandController#updateStandard` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `InspectionStandardCommandService#updateStandard`, `this#clearInspectionCaches` |
| DELETE | `/inspections/{id}` | `InspectionCommandController#deleteStandard` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `InspectionStandardCommandService#deleteStandard`, `this#clearInspectionCaches` |
| GET | `/inspections` | `InspectionQueryController#getStandards` | `InspectionStandardQueryService#searchStandards`, `InspectionStandardQueryService#getStandardsByCategory`, `InspectionStandardQueryService#getStandards` |
| GET | `/inspections/{id}` | `InspectionQueryController#getStandard` | `InspectionStandardQueryService#getStandard` |
| GET | `/inspections/files/{inspectionId}/{fileName}` | `InspectionQueryController#getInspectionFile` |  |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `InspectionFileStorageUtil`
  - `saveFiles` → this#saveFileInternal
  - `saveFileInternal` → this#validateFileExtension
- `InspectionStandardCommandService`
  - `createStandard` → this#validateStandardData, InspectionStandardRepository#save~, this#hasFiles, FileUtils#saveFile, this#updateImagePaths, this#saveStandardImages, this#toResponseDto
  - `updateStandard` → this#validateStandardData, InspectionStandardRepository#findById~, InspectionStandardImageRepository#findAllByInspectionStandardId, FileUtils#deleteFile, InspectionStandardImageRepository#delete~, this#hasFiles, FileUtils#saveFile, this#updateImagePaths, this#saveStandardImages, this#toResponseDto
  - `deleteStandard` → InspectionStandardRepository#findById~, InspectionStandardImageRepository#findAllByInspectionStandardId, FileUtils#deleteDirectory, InspectionStandardImageRepository#deleteAll~, InspectionStandardRepository#delete~
  - `saveStandardImages` → InspectionStandardImageRepository#save~
  - `toResponseDto` → InspectionStandardImageRepository#findAllByInspectionStandardId
- `InspectionStandardQueryService`
  - `getStandards` → InspectionStandardRepository#findAllWithPaging
  - `getStandardsByCategory` → InspectionStandardRepository#findByCategory
  - `searchStandards` → InspectionStandardRepository#searchStandards
  - `getStandard` → InspectionStandardRepository#findWithImagesById, this#toResponseDto
  - `toResponseDto` → InspectionStandardImageRepository#findAllByInspectionStandardId

## 3. 클래스 목록 (역할별)

### controller (2)

- `InspectionCommandController` controller/command/InspectionCommandController.java · public: createStandard, updateStandard, deleteStandard · 주입: InspectionStandardCommandService, UserQueryService, CacheManager
- `InspectionQueryController` controller/query/InspectionQueryController.java · public: getStandards, getStandard, getInspectionFile · 주입: InspectionStandardQueryService

### service-command (2)

- `InspectionFileStorageUtil` service/command/InspectionFileStorageUtil.java · public: saveFiles, extractImagePaths, updateImagePaths, deleteFiles, hasFiles
- `InspectionStandardCommandService` service/command/InspectionStandardCommandService.java · public: createStandard, updateStandard, deleteStandard · 주입: InspectionStandardRepository, InspectionStandardImageRepository, FileUtils

### service-query (1)

- `InspectionStandardQueryService` service/query/InspectionStandardQueryService.java · public: getStandards, getStandardsByCategory, searchStandards, getStandard · 주입: InspectionStandardRepository, InspectionStandardImageRepository

### repository (2)

- `InspectionStandardImageRepository` (interface) repository/InspectionStandardImageRepository.java · extends/implements JpaRepository · public: findAllByInspectionStandardId
- `InspectionStandardRepository` (interface) repository/InspectionStandardRepository.java · extends/implements JpaRepository, InspectionStandardRepositoryCustom · public: findWithImagesById, findAllWithPaging, findByCategory

### custom (2)

- `InspectionStandardRepositoryCustom` (interface) repository/InspectionStandardRepositoryCustom.java · public: searchStandards, searchStandardsByCategoryAndKeyword
- `InspectionStandardRepositoryImpl` repository/InspectionStandardRepositoryImpl.java · extends/implements InspectionStandardRepositoryCustom · public: searchStandards, searchStandardsByCategoryAndKeyword · 주입: JPAQueryFactory

### entity (3)

- `InspectionCategory` (enum) entity/InspectionCategory.java
- `InspectionStandard` entity/InspectionStandard.java · extends/implements BaseTimeEntity · public: update
- `InspectionStandardImage` entity/InspectionStandardImage.java

### dto (3)

- `InspectionStandardCreateRequestDto` dto/InspectionStandardCreateRequestDto.java
- `InspectionStandardResponseDto` dto/InspectionStandardResponseDto.java
- `InspectionStandardUpdateRequestDto` dto/InspectionStandardUpdateRequestDto.java

### exception (5)

- `InspectionErrorCode` exception/InspectionErrorCode.java
- `InspectionException` exception/InspectionException.java
- `InspectionFileException` exception/InspectionFileException.java
- `InspectionNotFoundException` exception/InspectionNotFoundException.java
- `InspectionPermissionException` exception/InspectionPermissionException.java

### 다른 도메인 참조 (import·주입 기준)

- **user**: `UserQueryService` ← InspectionCommandController

### 이 도메인을 참조하는 다른 도메인

- **global**: InspectionCategory, InspectionStandard, InspectionStandardRepository

## 4. JPA 엔티티 연관

- `InspectionStandard` @Entity · extends BaseTimeEntity
- `InspectionStandardImage` @Entity
  - inspectionStandard: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "inspection_standard_id", nullable = false) → `InspectionStandard`

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

