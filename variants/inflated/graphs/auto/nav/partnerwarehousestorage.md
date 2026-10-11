# partnerwarehousestorage 도메인

기준 경로: `src/main/java/com/fream/back/domain/partnerwarehousestorage/` · 파일 3개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/partner/warehouse-storage/commands/extend-period` | `PartnerWarehouseStorageCommandController#extendStoragePeriod` | `PartnerWarehouseStorageQueryService#findStorageById`, `WarehouseStorageCommandService#extendStoragePeriod` |
| GET | `/partner/warehouse-storage/queries/status-count` | `PartnerWarehouseStorageQueryController#getWarehouseStatusCount` | `PartnerWarehouseStorageQueryService#getWarehouseStatusCount` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `PartnerWarehouseStorageQueryService`
  - `findBySale` → WarehouseStorageRepository#findBySale
  - `getWarehouseStatusCount` → WarehouseStorageRepository#findByUser_Email, this#countByStatus
  - `findStorageById` → WarehouseStorageRepository#findById~

## 3. 클래스 목록 (역할별)

### controller (2)

- `PartnerWarehouseStorageCommandController` controller/command/PartnerWarehouseStorageCommandController.java · public: extendStoragePeriod · 주입: WarehouseStorageCommandService, PartnerWarehouseStorageQueryService
- `PartnerWarehouseStorageQueryController` controller/query/PartnerWarehouseStorageQueryController.java · public: getWarehouseStatusCount · 주입: PartnerWarehouseStorageQueryService

### service-query (1)

- `PartnerWarehouseStorageQueryService` service/query/PartnerWarehouseStorageQueryService.java · public: findBySale, getWarehouseStatusCount, findStorageById · 주입: WarehouseStorageRepository

### 다른 도메인 참조 (import·주입 기준)

- **sale**: `Sale` ← PartnerWarehouseStorageQueryService
- **warehouseStorage**: `WarehouseStatus` ← PartnerWarehouseStorageQueryService; `WarehouseStorage` ← PartnerWarehouseStorageCommandController, PartnerWarehouseStorageQueryService; `WarehouseStorageCommandService` ← PartnerWarehouseStorageCommandController; `WarehouseStorageRepository` ← PartnerWarehouseStorageQueryService

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

