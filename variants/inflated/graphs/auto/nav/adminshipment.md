# adminshipment 도메인

기준 경로: `src/main/java/com/fream/back/domain/adminshipment/` · 파일 7개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| PATCH | `/admin/shipments/order/{shipmentId}/status` | `AdminOrderShipmentCommandController#updateTrackingInfo` | `OrderShipmentCommandService#updateTrackingInfo` |
| POST | `/admin/shipments/order/{shipmentId}/check-status` | `AdminOrderShipmentCommandController#updateAndCheckStatus` | `OrderShipmentCommandService#updateAndCheckShipmentStatus` |
| POST | `/admin/shipments/order/{shipmentId}/check-status-string` | `AdminOrderShipmentCommandController#updateAndCheckStatusAsString` | `OrderShipmentCommandService#updateAndCheckShipmentStatus` |
| POST | `/admin/shipments/seller` | `AdminSellerShipmentCommandController#createSellerShipment` | `AdminSellerShipmentCommandService#createSellerShipment` |
| PATCH | `/admin/shipments/seller/{shipmentId}` | `AdminSellerShipmentCommandController#updateShipment` | `AdminSellerShipmentCommandService#updateShipment` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `AdminBrowserManageStepListener`
  - `beforeStep` → PlaywrightBrowserManager#openBrowser
  - `afterStep` → PlaywrightBrowserManager#closeBrowser
- `AdminSellerShipmentCommandService`
  - `createSellerShipment` → SaleRepository#findById~, SellerShipmentRepository#save~, WarehouseStorageCommandService#createSellerStorage
  - `updateShipment` → SellerShipmentRepository#findById~
- `AdminShipmentItemProcessor`
  - `process` → PlaywrightBrowserManager#newPage, this#parseStatusFromHtml, this#mapToShipmentStatus, OrderShipmentCommandService#completeOrder, AdminNotificationCommandService#notifyShipmentCompleted
- `AdminUpdateShipmentStatusesJobConfig`
  - `updateShipmentStatusesJob` → this#updateShipmentStatusesStep
  - `updateShipmentStatusesStep` → this#shipmentItemReader, this#playwrightBrowserManager, this#shipmentJpaItemWriter, this#shipmentSkipListener
  - `shipmentItemProcessor` → CjTrackingPlaywright#getCurrentTrackingStatus, this#mapToShipmentStatus, OrderShipmentCommandService#completeOrder, AdminNotificationCommandService#notifyShipmentCompleted

## 3. 클래스 목록 (역할별)

### controller (2)

- `AdminOrderShipmentCommandController` controller/command/AdminOrderShipmentCommandController.java · public: updateTrackingInfo, updateAndCheckStatus, updateAndCheckStatusAsString · 주입: OrderShipmentCommandService
- `AdminSellerShipmentCommandController` controller/command/AdminSellerShipmentCommandController.java · public: createSellerShipment, updateShipment · 주입: AdminSellerShipmentCommandService

### service-command (1)

- `AdminSellerShipmentCommandService` service/command/AdminSellerShipmentCommandService.java · public: createSellerShipment, updateShipment · 주입: SellerShipmentRepository, SaleRepository, WarehouseStorageCommandService

### repository (1)

- `AdminOrderShipmentRepository` (interface) repository/AdminOrderShipmentRepository.java · extends/implements JpaRepository · public: findByStatusIn

### config (3)

- `AdminBrowserManageStepListener` config/AdminBrowserManageStepListener.java · extends/implements StepExecutionListenerSupport · public: beforeStep, afterStep · 주입: PlaywrightBrowserManager
- `AdminShipmentItemProcessor` config/AdminShipmentItemProcessor.java · extends/implements ItemProcessor · public: process · 주입: PlaywrightBrowserManager, AdminNotificationCommandService, OrderShipmentCommandService
- `AdminUpdateShipmentStatusesJobConfig` config/AdminUpdateShipmentStatusesJobConfig.java · public: updateShipmentStatusesJob, updateShipmentStatusesStep, shipmentItemReader, shipmentItemProcessor, shipmentJpaItemWriter, shipmentSkipListener, playwrightBrowserManager · 주입: JobRepository, PlatformTransactionManager, EntityManagerFactory, AdminOrderShipmentRepository, AdminNotificationCommandService, OrderShipmentCommandService, CjTrackingPlaywright

### 다른 도메인 참조 (import·주입 기준)

- **adminnotification**: `AdminNotificationCommandService` ← AdminShipmentItemProcessor, AdminUpdateShipmentStatusesJobConfig
- **sale**: `Sale` ← AdminSellerShipmentCommandService; `SaleRepository` ← AdminSellerShipmentCommandService; `SaleStatus` ← AdminSellerShipmentCommandService
- **shipment**: `OrderShipment` ← AdminOrderShipmentRepository, AdminShipmentItemProcessor, AdminUpdateShipmentStatusesJobConfig; `OrderShipmentCommandService` ← AdminOrderShipmentCommandController, AdminShipmentItemProcessor, AdminUpdateShipmentStatusesJobConfig; `SellerShipment` ← AdminSellerShipmentCommandController, AdminSellerShipmentCommandService; `SellerShipmentRepository` ← AdminSellerShipmentCommandService; `ShipmentStatus` ← AdminOrderShipmentCommandController, AdminOrderShipmentRepository, AdminSellerShipmentCommandService, AdminShipmentItemProcessor, AdminUpdateShipmentStatusesJobConfig
- **warehouseStorage**: `WarehouseStorageCommandService` ← AdminSellerShipmentCommandService

### 이 도메인을 참조하는 다른 도메인

- **adminsale**: AdminSellerShipmentCommandService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

