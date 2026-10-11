# notice 도메인

기준 경로: `src/main/java/com/fream/back/domain/notice/` · 파일 22개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/warehouse-storage/commands/extend-period` | `NoticeCommandController#extendStoragePeriod` | `NoticeQueryService#findStorageById`, `NoticeCommandService#extendStoragePeriod` |
| GET | `/warehouse-storage/queries/status-count` | `NoticeQueryController#getWarehouseStatusCount` | `NoticeQueryService#getWarehouseStatusCount` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `NoticeCommandService`
  - `createOrderStorage` → NoticeRepository#save~
  - `createSellerStorage` → NoticeRepository#findBySale, NoticeRepository#save~
  - `updateWarehouseStatus` → NoticeQueryService#findBySale, this#validateStatusTransition, NoticeRepository#save~
  - `updateWarehouseStatusToSold` → NoticeQueryService#findBySale, this#validateStatusTransition, NoticeRepository#save~
  - `extendStoragePeriod` → NoticeRepository#findById~, NoticeRepository#save~
- `NoticeQueryService`
  - `findBySale` → NoticeRepository#findBySale
  - `getWarehouseStatusCount` → NoticeRepository#findByUser_Email, this#countByStatus
  - `findStorageById` → NoticeRepository#findById~

## 3. 클래스 목록 (역할별)

### controller (2)

- `NoticeCommandController` controller/command/NoticeCommandController.java · public: extendStoragePeriod · 주입: NoticeCommandService, NoticeQueryService
- `NoticeQueryController` controller/query/NoticeQueryController.java · public: getWarehouseStatusCount · 주입: NoticeQueryService

### service-command (1)

- `NoticeCommandService` service/command/NoticeCommandService.java · public: createOrderStorage, createSellerStorage, updateWarehouseStatus, updateWarehouseStatusToSold, extendStoragePeriod · 주입: NoticeRepository, NoticeQueryService

### service-query (1)

- `NoticeQueryService` service/query/NoticeQueryService.java · public: findBySale, getWarehouseStatusCount, findStorageById · 주입: NoticeRepository

### repository (1)

- `NoticeRepository` (interface) repository/NoticeRepository.java · extends/implements JpaRepository · public: findBySale, findByUser_Email

### entity (2)

- `Notice` entity/Notice.java · public: assignOrder, assignSale, markAsReturned, updateStatus, setStorageDates, extendStorage, updateEndDate, getRemainingDays, isExpired, getTotalStorageDays
- `WarehouseStatus` (enum) entity/WarehouseStatus.java

### dto (3)

- `StorageExtensionRequestDto` dto/StorageExtensionRequestDto.java
- `StorageExtensionResponseDto` dto/StorageExtensionResponseDto.java
- `WarehouseStatusCountDto` dto/WarehouseStatusCountDto.java

### exception (12)

- `InvalidStatusTransitionException` exception/InvalidStatusTransitionException.java
- `InvalidWarehouseStatusException` exception/InvalidWarehouseStatusException.java
- `ItemStatusCheckFailedException` exception/ItemStatusCheckFailedException.java
- `NoticeAccessDeniedException` exception/NoticeAccessDeniedException.java
- `NoticeErrorCode` exception/NoticeErrorCode.java
- `NoticeException` exception/NoticeException.java
- `NoticeNotFoundException` exception/NoticeNotFoundException.java
- `NoticeProcessingFailedException` exception/NoticeProcessingFailedException.java
- `StorageExtensionFailedException` exception/StorageExtensionFailedException.java
- `StorageLocationAllocationFailedException` exception/StorageLocationAllocationFailedException.java
- `StoragePeriodExpiredException` exception/StoragePeriodExpiredException.java
- `WarehouseCapacityExceededException` exception/WarehouseCapacityExceededException.java

### 다른 도메인 참조 (import·주입 기준)

- **faq**: `Faq` ← Notice, NoticeCommandService
- **product**: `Product` ← Notice, NoticeCommandService
- **style**: `Style` ← Notice, NoticeCommandService, NoticeQueryService, NoticeRepository

### 이 도메인을 참조하는 다른 도메인

- **address**: NoticeCommandService
- **faq**: Notice, NoticeCommandService, WarehouseStatus
- **global**: Notice, NoticeCommandService, NoticeRepository

## 4. JPA 엔티티 연관

- `Notice` @Entity
  - user: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) → `Product` [product]
  - order: @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "order_id", nullable = true) → `Faq` [faq]
  - sale: @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "sale_id", nullable = true) → `Style` [style]

다른 도메인 엔티티에서 들어오는 연관:

- `Faq.warehouseStorage` @OneToOne → `Notice` [faq]

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

