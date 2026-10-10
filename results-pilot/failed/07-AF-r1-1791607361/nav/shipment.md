# shipment 도메인

기준 경로: `src/main/java/com/fream/back/domain/shipment/` · 파일 20개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| PATCH | `/shipments/order/{shipmentId}/status` | `OrderShipmentCommandController#updateTrackingInfo` | `OrderShipmentCommandService#updateTrackingInfo` |
| POST | `/shipments/order/{shipmentId}/check-status` | `OrderShipmentCommandController#updateAndCheckStatus` | `OrderShipmentCommandService#updateAndCheckShipmentStatus` |
| POST | `/shipments/order/{shipmentId}/check-status-string` | `OrderShipmentCommandController#updateAndCheckStatusAsString` | `OrderShipmentCommandService#updateAndCheckShipmentStatus` |
| POST | `/shipments/seller` | `SellerShipmentCommandController#createSellerShipment` | `SellerShipmentCommandService#createSellerShipment` |
| PATCH | `/shipments/seller/{shipmentId}` | `SellerShipmentCommandController#updateShipment` | `SellerShipmentCommandService#updateShipment` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `BrowserManageStepListener`
  - `beforeStep` → PlaywrightBrowserManager#openBrowser
  - `afterStep` → PlaywrightBrowserManager#closeBrowser
- `OrderShipmentCommandService`
  - `createOrderShipment` → OrderShipmentRepository#save~
  - `updateShipmentStatus` → OrderShipmentRepository#save~
  - `updateSaleStatusToSold` → SaleBidQueryService#findByOrderId, WarehouseStorageCommandService#updateWarehouseStatusToSold, NotificationCommandService#createNotification, OrderBidQueryService#findByOrderId
  - `completeOrder` → OrderBidQueryService#findByOrderId, this#updateSaleStatusToSold, NotificationCommandService#createNotification
  - `updateTrackingInfo` → OrderShipmentRepository#findById~, NotificationCommandService#createNotification, this#updateSaleStatusToSold, SaleBidQueryService#findByOrderId, WarehouseStorageCommandService#updateWarehouseStatusToSold, this#updateOrderAndSaleBidStatusToCompleted
  - `updateShipmentStatuses` → OrderShipmentRepository#findByStatusIn, CjTrackingPlaywright#getCurrentTrackingStatus, this#mapToShipmentStatus, NotificationCommandService#createNotification, OrderShipmentRepository#save~
  - `updateOrderAndSaleBidStatusToCompleted` → OrderBidQueryService#findByOrderId, SaleBidQueryService#findByOrderId
  - `updateAndCheckShipmentStatus` → OrderShipmentRepository#findById~, CjTrackingPlaywright#getCurrentTrackingStatus, this#mapToShipmentStatus, this#completeOrder
- `SellerShipmentCommandService`
  - `createSellerShipment` → SaleRepository#findById~, SellerShipmentRepository#save~, WarehouseStorageCommandService#createSellerStorage
  - `updateShipment` → SellerShipmentRepository#findById~
- `ShipmentItemProcessor`
  - `process` → PlaywrightBrowserManager#newPage, this#parseStatusFromHtml, this#mapToShipmentStatus, OrderShipmentCommandService#completeOrder, NotificationCommandService#notifyShipmentCompleted
- `UpdateShipmentStatusesJobConfig`
  - `updateShipmentStatusesJob` → this#updateShipmentStatusesStep
  - `updateShipmentStatusesStep` → this#shipmentItemReader, this#playwrightBrowserManager, this#shipmentJpaItemWriter, this#shipmentSkipListener
  - `shipmentItemProcessor` → CjTrackingPlaywright#getCurrentTrackingStatus, this#mapToShipmentStatus, OrderShipmentCommandService#completeOrder, NotificationCommandService#notifyShipmentCompleted

## 3. 클래스 목록 (역할별)

### controller (2)

- `OrderShipmentCommandController` controller/command/OrderShipmentCommandController.java · public: updateTrackingInfo, updateAndCheckStatus, updateAndCheckStatusAsString · 주입: OrderShipmentCommandService
- `SellerShipmentCommandController` controller/command/SellerShipmentCommandController.java · public: createSellerShipment, updateShipment · 주입: SellerShipmentCommandService

### service-command (2)

- `OrderShipmentCommandService` service/command/OrderShipmentCommandService.java · public: createOrderShipment, updateShipmentStatus, updateSaleStatusToSold, completeOrder, updateTrackingInfo, updateShipmentStatuses, updateAndCheckShipmentStatus · 주입: OrderShipmentRepository, NotificationCommandService, SaleBidQueryService, OrderBidQueryService, WarehouseStorageCommandService, CjTrackingPlaywright
- `SellerShipmentCommandService` service/command/SellerShipmentCommandService.java · public: createSellerShipment, updateShipment · 주입: SellerShipmentRepository, SaleRepository, WarehouseStorageCommandService

### repository (2)

- `OrderShipmentRepository` (interface) repository/OrderShipmentRepository.java · extends/implements JpaRepository · public: findByStatusIn
- `SellerShipmentRepository` (interface) repository/SellerShipmentRepository.java · extends/implements JpaRepository

### entity (3)

- `OrderShipment` entity/OrderShipment.java · extends/implements BaseTimeEntity · public: assignOrder, updateTrackingInfo, updateStatus
- `SellerShipment` entity/SellerShipment.java · extends/implements BaseTimeEntity · public: assignSale, updateTrackingInfo, updateStatus
- `ShipmentStatus` (enum) entity/ShipmentStatus.java · public: canTransitionTo

### config (3)

- `BrowserManageStepListener` config/BrowserManageStepListener.java · extends/implements StepExecutionListenerSupport · public: beforeStep, afterStep · 주입: PlaywrightBrowserManager
- `ShipmentItemProcessor` config/ShipmentItemProcessor.java · extends/implements ItemProcessor · public: process · 주입: PlaywrightBrowserManager, NotificationCommandService, OrderShipmentCommandService
- `UpdateShipmentStatusesJobConfig` config/UpdateShipmentStatusesJobConfig.java · public: updateShipmentStatusesJob, updateShipmentStatusesStep, shipmentItemReader, shipmentItemProcessor, shipmentJpaItemWriter, shipmentSkipListener, playwrightBrowserManager · 주입: JobRepository, PlatformTransactionManager, EntityManagerFactory, OrderShipmentRepository, NotificationCommandService, OrderShipmentCommandService, CjTrackingPlaywright

### dto (6)

- `OrderShipmentRequestDto` dto/OrderShipmentRequestDto.java
- `SellerShipmentRequestDto` dto/SellerShipmentRequestDto.java
- `ShipmentResponseDto` dto/ShipmentResponseDto.java
- `ShipmentStatusResponse` dto/ShipmentStatusResponse.java
- `ShipmentUpdateRequestDto` dto/ShipmentUpdateRequestDto.java
- `TrackingNumberRequestDto` dto/TrackingNumberRequestDto.java

### exception (2)

- `ShipmentErrorCode` exception/ShipmentErrorCode.java
- `ShipmentException` exception/ShipmentException.java

### 다른 도메인 참조 (import·주입 기준)

- **notification**: `NotificationCategory` ← OrderShipmentCommandService; `NotificationCommandService` ← OrderShipmentCommandService, ShipmentItemProcessor, UpdateShipmentStatusesJobConfig; `NotificationType` ← OrderShipmentCommandService
- **order**: `Order` ← OrderShipment, OrderShipmentCommandService; `OrderBid` ← OrderShipmentCommandService; `OrderBidQueryService` ← OrderShipmentCommandService; `OrderStatus` ← OrderShipmentCommandService
- **sale**: `BidStatus` ← OrderShipmentCommandService; `Sale` ← OrderShipmentCommandService, SellerShipment, SellerShipmentCommandService; `SaleBid` ← OrderShipmentCommandService; `SaleBidQueryService` ← OrderShipmentCommandService; `SaleRepository` ← SellerShipmentCommandService; `SaleStatus` ← OrderShipmentCommandService, SellerShipmentCommandService
- **user**: `User` ← OrderShipmentCommandService
- **warehouseStorage**: `WarehouseStorageCommandService` ← OrderShipmentCommandService, SellerShipmentCommandService

### 이 도메인을 참조하는 다른 도메인

- **global**: OrderShipment, OrderShipmentCommandService, SellerShipment, SellerShipmentRepository, ShipmentStatus
- **order**: OrderShipment, OrderShipmentCommandService
- **sale**: SellerShipment, SellerShipmentCommandService

## 4. JPA 엔티티 연관

- `OrderShipment` @Entity · extends BaseTimeEntity
  - order: @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "order_id", nullable = false) → `Order` [order]
- `SellerShipment` @Entity · extends BaseTimeEntity
  - sale: @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "sale_id", nullable = false) → `Sale` [sale]

다른 도메인 엔티티에서 들어오는 연관:

- `Order.orderShipment` @OneToOne → `OrderShipment` [order]
- `Sale.sellerShipment` @OneToOne → `SellerShipment` [sale]

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

