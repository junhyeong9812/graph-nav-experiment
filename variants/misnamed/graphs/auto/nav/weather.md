# weather 도메인

기준 경로: `src/main/java/com/fream/back/domain/weather/` · 파일 20개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/inspections` | `WeatherCommandController#createStandard` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `WeatherStandardCommandService#createStandard`, `this#clearInspectionCaches` |
| PUT | `/inspections/{id}` | `WeatherCommandController#updateStandard` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `WeatherStandardCommandService#updateStandard`, `this#clearInspectionCaches` |
| DELETE | `/inspections/{id}` | `WeatherCommandController#deleteStandard` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `WeatherStandardCommandService#deleteStandard`, `this#clearInspectionCaches` |
| GET | `/inspections` | `WeatherQueryController#getStandards` | `WeatherStandardQueryService#searchStandards`, `WeatherStandardQueryService#getStandardsByCategory`, `WeatherStandardQueryService#getStandards` |
| GET | `/inspections/{id}` | `WeatherQueryController#getStandard` | `WeatherStandardQueryService#getStandard` |
| GET | `/inspections/files/{inspectionId}/{fileName}` | `WeatherQueryController#getInspectionFile` |  |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `WeatherFileStorageUtil`
  - `saveFiles` → this#saveFileInternal
  - `saveFileInternal` → this#validateFileExtension
- `WeatherStandardCommandService`
  - `createStandard` → this#validateStandardData, WeatherStandardRepository#save~, this#hasFiles, FileUtils#saveFile, this#updateImagePaths, this#saveStandardImages, this#toResponseDto
  - `updateStandard` → this#validateStandardData, WeatherStandardRepository#findById~, WeatherStandardImageRepository#findAllByInspectionStandardId, FileUtils#deleteFile, WeatherStandardImageRepository#delete~, this#hasFiles, FileUtils#saveFile, this#updateImagePaths, this#saveStandardImages, this#toResponseDto
  - `deleteStandard` → WeatherStandardRepository#findById~, WeatherStandardImageRepository#findAllByInspectionStandardId, FileUtils#deleteDirectory, WeatherStandardImageRepository#deleteAll~, WeatherStandardRepository#delete~
  - `saveStandardImages` → WeatherStandardImageRepository#save~
  - `toResponseDto` → WeatherStandardImageRepository#findAllByInspectionStandardId
- `WeatherStandardQueryService`
  - `getStandards` → WeatherStandardRepository#findAllWithPaging
  - `getStandardsByCategory` → WeatherStandardRepository#findByCategory
  - `searchStandards` → WeatherStandardRepository#searchStandards
  - `getStandard` → WeatherStandardRepository#findWithImagesById, this#toResponseDto
  - `toResponseDto` → WeatherStandardImageRepository#findAllByInspectionStandardId

## 3. 클래스 목록 (역할별)

### controller (2)

- `WeatherCommandController` controller/command/WeatherCommandController.java · public: createStandard, updateStandard, deleteStandard · 주입: WeatherStandardCommandService, ProductQueryService, CacheManager
- `WeatherQueryController` controller/query/WeatherQueryController.java · public: getStandards, getStandard, getInspectionFile · 주입: WeatherStandardQueryService

### service-command (2)

- `WeatherFileStorageUtil` service/command/WeatherFileStorageUtil.java · public: saveFiles, extractImagePaths, updateImagePaths, deleteFiles, hasFiles
- `WeatherStandardCommandService` service/command/WeatherStandardCommandService.java · public: createStandard, updateStandard, deleteStandard · 주입: WeatherStandardRepository, WeatherStandardImageRepository, FileUtils

### service-query (1)

- `WeatherStandardQueryService` service/query/WeatherStandardQueryService.java · public: getStandards, getStandardsByCategory, searchStandards, getStandard · 주입: WeatherStandardRepository, WeatherStandardImageRepository

### repository (2)

- `WeatherStandardImageRepository` (interface) repository/WeatherStandardImageRepository.java · extends/implements JpaRepository · public: findAllByInspectionStandardId
- `WeatherStandardRepository` (interface) repository/WeatherStandardRepository.java · extends/implements JpaRepository, WeatherStandardRepositoryCustom · public: findWithImagesById, findAllWithPaging, findByCategory

### custom (2)

- `WeatherStandardRepositoryCustom` (interface) repository/WeatherStandardRepositoryCustom.java · public: searchStandards, searchStandardsByCategoryAndKeyword
- `WeatherStandardRepositoryImpl` repository/WeatherStandardRepositoryImpl.java · extends/implements WeatherStandardRepositoryCustom · public: searchStandards, searchStandardsByCategoryAndKeyword · 주입: JPAQueryFactory

### entity (3)

- `WeatherCategory` (enum) entity/WeatherCategory.java
- `WeatherStandard` entity/WeatherStandard.java · extends/implements BaseTimeEntity · public: update
- `WeatherStandardImage` entity/WeatherStandardImage.java

### dto (3)

- `WeatherStandardCreateRequestDto` dto/WeatherStandardCreateRequestDto.java
- `WeatherStandardResponseDto` dto/WeatherStandardResponseDto.java
- `WeatherStandardUpdateRequestDto` dto/WeatherStandardUpdateRequestDto.java

### exception (5)

- `WeatherErrorCode` exception/WeatherErrorCode.java
- `WeatherException` exception/WeatherException.java
- `WeatherFileException` exception/WeatherFileException.java
- `WeatherNotFoundException` exception/WeatherNotFoundException.java
- `WeatherPermissionException` exception/WeatherPermissionException.java

### 다른 도메인 참조 (import·주입 기준)

- **product**: `ProductQueryService` ← WeatherCommandController

### 이 도메인을 참조하는 다른 도메인

- **global**: WeatherCategory, WeatherStandard, WeatherStandardRepository

## 4. JPA 엔티티 연관

- `WeatherStandard` @Entity · extends BaseTimeEntity
- `WeatherStandardImage` @Entity
  - inspectionStandard: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "inspection_standard_id", nullable = false) → `WeatherStandard`

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

