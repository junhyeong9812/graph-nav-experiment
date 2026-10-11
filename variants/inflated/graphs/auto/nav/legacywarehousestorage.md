# legacywarehousestorage 도메인

기준 경로: `src/main/java/com/fream/back/domain/legacywarehousestorage/` · 파일 3개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/legacy/warehouse-storage/commands/extend-period` | `LegacyWarehouseStorageCommandController#extendStoragePeriod` | `LegacyWarehouseStorageQueryService#findStorageById`, `WarehouseStorageCommandService#extendStoragePeriod` |
| GET | `/legacy/warehouse-storage/queries/status-count` | `LegacyWarehouseStorageQueryController#getWarehouseStatusCount` | `LegacyWarehouseStorageQueryService#getWarehouseStatusCount` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `LegacyWarehouseStorageQueryService`
  - `findBySale` → WarehouseStorageRepository#findBySale
  - `getWarehouseStatusCount` → WarehouseStorageRepository#findByUser_Email, this#countByStatus
  - `findStorageById` → WarehouseStorageRepository#findById~

## 3. 클래스 목록 (역할별)

### controller (2)

- `LegacyWarehouseStorageCommandController` controller/command/LegacyWarehouseStorageCommandController.java · public: extendStoragePeriod · 주입: WarehouseStorageCommandService, LegacyWarehouseStorageQueryService
- `LegacyWarehouseStorageQueryController` controller/query/LegacyWarehouseStorageQueryController.java · public: getWarehouseStatusCount · 주입: LegacyWarehouseStorageQueryService

### service-query (1)

- `LegacyWarehouseStorageQueryService` service/query/LegacyWarehouseStorageQueryService.java · public: findBySale, getWarehouseStatusCount, findStorageById · 주입: WarehouseStorageRepository

### 다른 도메인 참조 (import·주입 기준)

- **sale**: `Sale` ← LegacyWarehouseStorageQueryService
- **warehouseStorage**: `WarehouseStatus` ← LegacyWarehouseStorageQueryService; `WarehouseStorage` ← LegacyWarehouseStorageCommandController, LegacyWarehouseStorageQueryService; `WarehouseStorageCommandService` ← LegacyWarehouseStorageCommandController; `WarehouseStorageRepository` ← LegacyWarehouseStorageQueryService

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

