# warehouseStorage 도메인

기준 경로: `src/main/java/com/fream/back/domain/warehouseStorage/` · 파일 22개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/warehouse-storage/commands/extend-period` | `WarehouseStorageCommandController#extendStoragePeriod` | `WarehouseStorageQueryService#findStorageById`, `WarehouseStorageCommandService#extendStoragePeriod` |
| GET | `/warehouse-storage/queries/status-count` | `WarehouseStorageQueryController#getWarehouseStatusCount` | `WarehouseStorageQueryService#getWarehouseStatusCount` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `WarehouseStorageCommandService`
  - `createOrderStorage` → WarehouseStorageRepository#save~
  - `createSellerStorage` → WarehouseStorageRepository#findBySale, WarehouseStorageRepository#save~
  - `updateWarehouseStatus` → WarehouseStorageQueryService#findBySale, this#validateStatusTransition, WarehouseStorageRepository#save~
  - `updateWarehouseStatusToSold` → WarehouseStorageQueryService#findBySale, this#validateStatusTransition, WarehouseStorageRepository#save~
  - `extendStoragePeriod` → WarehouseStorageRepository#findById~, WarehouseStorageRepository#save~
- `WarehouseStorageQueryService`
  - `findBySale` → WarehouseStorageRepository#findBySale
  - `getWarehouseStatusCount` → WarehouseStorageRepository#findByUser_Email, this#countByStatus
  - `findStorageById` → WarehouseStorageRepository#findById~

## 3. 클래스 목록 (역할별)

### controller (2)

- `WarehouseStorageCommandController` controller/command/WarehouseStorageCommandController.java · public: extendStoragePeriod · 주입: WarehouseStorageCommandService, WarehouseStorageQueryService
- `WarehouseStorageQueryController` controller/query/WarehouseStorageQueryController.java · public: getWarehouseStatusCount · 주입: WarehouseStorageQueryService

### service-command (1)

- `WarehouseStorageCommandService` service/command/WarehouseStorageCommandService.java · public: createOrderStorage, createSellerStorage, updateWarehouseStatus, updateWarehouseStatusToSold, extendStoragePeriod · 주입: WarehouseStorageRepository, WarehouseStorageQueryService

### service-query (1)

- `WarehouseStorageQueryService` service/query/WarehouseStorageQueryService.java · public: findBySale, getWarehouseStatusCount, findStorageById · 주입: WarehouseStorageRepository

### repository (1)

- `WarehouseStorageRepository` (interface) repository/WarehouseStorageRepository.java · extends/implements JpaRepository · public: findBySale, findByUser_Email

### entity (2)

- `WarehouseStatus` (enum) entity/WarehouseStatus.java
- `WarehouseStorage` entity/WarehouseStorage.java · public: assignOrder, assignSale, markAsReturned, updateStatus, setStorageDates, extendStorage, updateEndDate, getRemainingDays, isExpired, getTotalStorageDays

### dto (3)

- `StorageExtensionRequestDto` dto/StorageExtensionRequestDto.java
- `StorageExtensionResponseDto` dto/StorageExtensionResponseDto.java
- `WarehouseStatusCountDto` dto/WarehouseStatusCountDto.java

### exception (12)

- `InvalidStatusTransitionException` exception/InvalidStatusTransitionException.java
- `InvalidWarehouseStatusException` exception/InvalidWarehouseStatusException.java
- `ItemStatusCheckFailedException` exception/ItemStatusCheckFailedException.java
- `StorageExtensionFailedException` exception/StorageExtensionFailedException.java
- `StorageLocationAllocationFailedException` exception/StorageLocationAllocationFailedException.java
- `StoragePeriodExpiredException` exception/StoragePeriodExpiredException.java
- `WarehouseCapacityExceededException` exception/WarehouseCapacityExceededException.java
- `WarehouseStorageAccessDeniedException` exception/WarehouseStorageAccessDeniedException.java
- `WarehouseStorageErrorCode` exception/WarehouseStorageErrorCode.java
- `WarehouseStorageException` exception/WarehouseStorageException.java
- `WarehouseStorageNotFoundException` exception/WarehouseStorageNotFoundException.java
- `WarehouseStorageProcessingFailedException` exception/WarehouseStorageProcessingFailedException.java

### 다른 도메인 참조 (import·주입 기준)

- **order**: `Order` ← WarehouseStorage, WarehouseStorageCommandService
- **sale**: `Sale` ← WarehouseStorage, WarehouseStorageCommandService, WarehouseStorageQueryService, WarehouseStorageRepository
- **user**: `User` ← WarehouseStorage, WarehouseStorageCommandService

### 이 도메인을 참조하는 다른 도메인

- **global**: WarehouseStorage, WarehouseStorageCommandService, WarehouseStorageRepository
- **order**: WarehouseStatus, WarehouseStorage, WarehouseStorageCommandService
- **shipment**: WarehouseStorageCommandService

## 4. JPA 엔티티 연관

- `WarehouseStorage` @Entity
  - user: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) → `User` [user]
  - order: @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "order_id", nullable = true) → `Order` [order]
  - sale: @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "sale_id", nullable = true) → `Sale` [sale]

다른 도메인 엔티티에서 들어오는 연관:

- `Order.warehouseStorage` @OneToOne → `WarehouseStorage` [order]

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

