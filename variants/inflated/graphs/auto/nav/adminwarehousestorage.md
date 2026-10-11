# adminwarehousestorage 도메인

기준 경로: `src/main/java/com/fream/back/domain/adminwarehousestorage/` · 파일 3개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/admin/warehouse-storage/commands/extend-period` | `AdminWarehouseStorageCommandController#extendStoragePeriod` | `AdminWarehouseStorageQueryService#findStorageById`, `WarehouseStorageCommandService#extendStoragePeriod` |
| GET | `/admin/warehouse-storage/queries/status-count` | `AdminWarehouseStorageQueryController#getWarehouseStatusCount` | `AdminWarehouseStorageQueryService#getWarehouseStatusCount` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `AdminWarehouseStorageQueryService`
  - `findBySale` → WarehouseStorageRepository#findBySale
  - `getWarehouseStatusCount` → WarehouseStorageRepository#findByUser_Email, this#countByStatus
  - `findStorageById` → WarehouseStorageRepository#findById~

## 3. 클래스 목록 (역할별)

### controller (2)

- `AdminWarehouseStorageCommandController` controller/command/AdminWarehouseStorageCommandController.java · public: extendStoragePeriod · 주입: WarehouseStorageCommandService, AdminWarehouseStorageQueryService
- `AdminWarehouseStorageQueryController` controller/query/AdminWarehouseStorageQueryController.java · public: getWarehouseStatusCount · 주입: AdminWarehouseStorageQueryService

### service-query (1)

- `AdminWarehouseStorageQueryService` service/query/AdminWarehouseStorageQueryService.java · public: findBySale, getWarehouseStatusCount, findStorageById · 주입: WarehouseStorageRepository

### 다른 도메인 참조 (import·주입 기준)

- **sale**: `Sale` ← AdminWarehouseStorageQueryService
- **warehouseStorage**: `WarehouseStatus` ← AdminWarehouseStorageQueryService; `WarehouseStorage` ← AdminWarehouseStorageCommandController, AdminWarehouseStorageQueryService; `WarehouseStorageCommandService` ← AdminWarehouseStorageCommandController; `WarehouseStorageRepository` ← AdminWarehouseStorageQueryService

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

