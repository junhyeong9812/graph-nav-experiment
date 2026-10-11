# internalshipment 도메인

기준 경로: `src/main/java/com/fream/back/domain/internalshipment/` · 파일 7개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| PATCH | `/internal/shipments/order/{shipmentId}/status` | `InternalOrderShipmentCommandController#updateTrackingInfo` | `OrderShipmentCommandService#updateTrackingInfo` |
| POST | `/internal/shipments/order/{shipmentId}/check-status` | `InternalOrderShipmentCommandController#updateAndCheckStatus` | `OrderShipmentCommandService#updateAndCheckShipmentStatus` |
| POST | `/internal/shipments/order/{shipmentId}/check-status-string` | `InternalOrderShipmentCommandController#updateAndCheckStatusAsString` | `OrderShipmentCommandService#updateAndCheckShipmentStatus` |
| POST | `/internal/shipments/seller` | `InternalSellerShipmentCommandController#createSellerShipment` | `InternalSellerShipmentCommandService#createSellerShipment` |
| PATCH | `/internal/shipments/seller/{shipmentId}` | `InternalSellerShipmentCommandController#updateShipment` | `InternalSellerShipmentCommandService#updateShipment` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `InternalBrowserManageStepListener`
  - `beforeStep` → PlaywrightBrowserManager#openBrowser
  - `afterStep` → PlaywrightBrowserManager#closeBrowser
- `InternalSellerShipmentCommandService`
  - `createSellerShipment` → SaleRepository#findById~, SellerShipmentRepository#save~, WarehouseStorageCommandService#createSellerStorage
  - `updateShipment` → SellerShipmentRepository#findById~
- `InternalShipmentItemProcessor`
  - `process` → PlaywrightBrowserManager#newPage, this#parseStatusFromHtml, this#mapToShipmentStatus, OrderShipmentCommandService#completeOrder, InternalNotificationCommandService#notifyShipmentCompleted
- `InternalUpdateShipmentStatusesJobConfig`
  - `updateShipmentStatusesJob` → this#updateShipmentStatusesStep
  - `updateShipmentStatusesStep` → this#shipmentItemReader, this#playwrightBrowserManager, this#shipmentJpaItemWriter, this#shipmentSkipListener
  - `shipmentItemProcessor` → CjTrackingPlaywright#getCurrentTrackingStatus, this#mapToShipmentStatus, OrderShipmentCommandService#completeOrder, InternalNotificationCommandService#notifyShipmentCompleted

## 3. 클래스 목록 (역할별)

### controller (2)

- `InternalOrderShipmentCommandController` controller/command/InternalOrderShipmentCommandController.java · public: updateTrackingInfo, updateAndCheckStatus, updateAndCheckStatusAsString · 주입: OrderShipmentCommandService
- `InternalSellerShipmentCommandController` controller/command/InternalSellerShipmentCommandController.java · public: createSellerShipment, updateShipment · 주입: InternalSellerShipmentCommandService

### service-command (1)

- `InternalSellerShipmentCommandService` service/command/InternalSellerShipmentCommandService.java · public: createSellerShipment, updateShipment · 주입: SellerShipmentRepository, SaleRepository, WarehouseStorageCommandService

### repository (1)

- `InternalOrderShipmentRepository` (interface) repository/InternalOrderShipmentRepository.java · extends/implements JpaRepository · public: findByStatusIn

### config (3)

- `InternalBrowserManageStepListener` config/InternalBrowserManageStepListener.java · extends/implements StepExecutionListenerSupport · public: beforeStep, afterStep · 주입: PlaywrightBrowserManager
- `InternalShipmentItemProcessor` config/InternalShipmentItemProcessor.java · extends/implements ItemProcessor · public: process · 주입: PlaywrightBrowserManager, InternalNotificationCommandService, OrderShipmentCommandService
- `InternalUpdateShipmentStatusesJobConfig` config/InternalUpdateShipmentStatusesJobConfig.java · public: updateShipmentStatusesJob, updateShipmentStatusesStep, shipmentItemReader, shipmentItemProcessor, shipmentJpaItemWriter, shipmentSkipListener, playwrightBrowserManager · 주입: JobRepository, PlatformTransactionManager, EntityManagerFactory, InternalOrderShipmentRepository, InternalNotificationCommandService, OrderShipmentCommandService, CjTrackingPlaywright

### 다른 도메인 참조 (import·주입 기준)

- **internalnotification**: `InternalNotificationCommandService` ← InternalShipmentItemProcessor, InternalUpdateShipmentStatusesJobConfig
- **sale**: `Sale` ← InternalSellerShipmentCommandService; `SaleRepository` ← InternalSellerShipmentCommandService; `SaleStatus` ← InternalSellerShipmentCommandService
- **shipment**: `OrderShipment` ← InternalOrderShipmentRepository, InternalShipmentItemProcessor, InternalUpdateShipmentStatusesJobConfig; `OrderShipmentCommandService` ← InternalOrderShipmentCommandController, InternalShipmentItemProcessor, InternalUpdateShipmentStatusesJobConfig; `SellerShipment` ← InternalSellerShipmentCommandController, InternalSellerShipmentCommandService; `SellerShipmentRepository` ← InternalSellerShipmentCommandService; `ShipmentStatus` ← InternalOrderShipmentCommandController, InternalOrderShipmentRepository, InternalSellerShipmentCommandService, InternalShipmentItemProcessor, InternalUpdateShipmentStatusesJobConfig
- **warehouseStorage**: `WarehouseStorageCommandService` ← InternalSellerShipmentCommandService

### 이 도메인을 참조하는 다른 도메인

- **internalsale**: InternalSellerShipmentCommandService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

