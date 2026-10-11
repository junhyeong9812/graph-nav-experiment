# legacyshipment 도메인

기준 경로: `src/main/java/com/fream/back/domain/legacyshipment/` · 파일 7개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| PATCH | `/legacy/shipments/order/{shipmentId}/status` | `LegacyOrderShipmentCommandController#updateTrackingInfo` | `OrderShipmentCommandService#updateTrackingInfo` |
| POST | `/legacy/shipments/order/{shipmentId}/check-status` | `LegacyOrderShipmentCommandController#updateAndCheckStatus` | `OrderShipmentCommandService#updateAndCheckShipmentStatus` |
| POST | `/legacy/shipments/order/{shipmentId}/check-status-string` | `LegacyOrderShipmentCommandController#updateAndCheckStatusAsString` | `OrderShipmentCommandService#updateAndCheckShipmentStatus` |
| POST | `/legacy/shipments/seller` | `LegacySellerShipmentCommandController#createSellerShipment` | `LegacySellerShipmentCommandService#createSellerShipment` |
| PATCH | `/legacy/shipments/seller/{shipmentId}` | `LegacySellerShipmentCommandController#updateShipment` | `LegacySellerShipmentCommandService#updateShipment` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `LegacyBrowserManageStepListener`
  - `beforeStep` → PlaywrightBrowserManager#openBrowser
  - `afterStep` → PlaywrightBrowserManager#closeBrowser
- `LegacySellerShipmentCommandService`
  - `createSellerShipment` → SaleRepository#findById~, SellerShipmentRepository#save~, WarehouseStorageCommandService#createSellerStorage
  - `updateShipment` → SellerShipmentRepository#findById~
- `LegacyShipmentItemProcessor`
  - `process` → PlaywrightBrowserManager#newPage, this#parseStatusFromHtml, this#mapToShipmentStatus, OrderShipmentCommandService#completeOrder, LegacyNotificationCommandService#notifyShipmentCompleted
- `LegacyUpdateShipmentStatusesJobConfig`
  - `updateShipmentStatusesJob` → this#updateShipmentStatusesStep
  - `updateShipmentStatusesStep` → this#shipmentItemReader, this#playwrightBrowserManager, this#shipmentJpaItemWriter, this#shipmentSkipListener
  - `shipmentItemProcessor` → CjTrackingPlaywright#getCurrentTrackingStatus, this#mapToShipmentStatus, OrderShipmentCommandService#completeOrder, LegacyNotificationCommandService#notifyShipmentCompleted

## 3. 클래스 목록 (역할별)

### controller (2)

- `LegacyOrderShipmentCommandController` controller/command/LegacyOrderShipmentCommandController.java · public: updateTrackingInfo, updateAndCheckStatus, updateAndCheckStatusAsString · 주입: OrderShipmentCommandService
- `LegacySellerShipmentCommandController` controller/command/LegacySellerShipmentCommandController.java · public: createSellerShipment, updateShipment · 주입: LegacySellerShipmentCommandService

### service-command (1)

- `LegacySellerShipmentCommandService` service/command/LegacySellerShipmentCommandService.java · public: createSellerShipment, updateShipment · 주입: SellerShipmentRepository, SaleRepository, WarehouseStorageCommandService

### repository (1)

- `LegacyOrderShipmentRepository` (interface) repository/LegacyOrderShipmentRepository.java · extends/implements JpaRepository · public: findByStatusIn

### config (3)

- `LegacyBrowserManageStepListener` config/LegacyBrowserManageStepListener.java · extends/implements StepExecutionListenerSupport · public: beforeStep, afterStep · 주입: PlaywrightBrowserManager
- `LegacyShipmentItemProcessor` config/LegacyShipmentItemProcessor.java · extends/implements ItemProcessor · public: process · 주입: PlaywrightBrowserManager, LegacyNotificationCommandService, OrderShipmentCommandService
- `LegacyUpdateShipmentStatusesJobConfig` config/LegacyUpdateShipmentStatusesJobConfig.java · public: updateShipmentStatusesJob, updateShipmentStatusesStep, shipmentItemReader, shipmentItemProcessor, shipmentJpaItemWriter, shipmentSkipListener, playwrightBrowserManager · 주입: JobRepository, PlatformTransactionManager, EntityManagerFactory, LegacyOrderShipmentRepository, LegacyNotificationCommandService, OrderShipmentCommandService, CjTrackingPlaywright

### 다른 도메인 참조 (import·주입 기준)

- **legacynotification**: `LegacyNotificationCommandService` ← LegacyShipmentItemProcessor, LegacyUpdateShipmentStatusesJobConfig
- **sale**: `Sale` ← LegacySellerShipmentCommandService; `SaleRepository` ← LegacySellerShipmentCommandService; `SaleStatus` ← LegacySellerShipmentCommandService
- **shipment**: `OrderShipment` ← LegacyOrderShipmentRepository, LegacyShipmentItemProcessor, LegacyUpdateShipmentStatusesJobConfig; `OrderShipmentCommandService` ← LegacyOrderShipmentCommandController, LegacyShipmentItemProcessor, LegacyUpdateShipmentStatusesJobConfig; `SellerShipment` ← LegacySellerShipmentCommandController, LegacySellerShipmentCommandService; `SellerShipmentRepository` ← LegacySellerShipmentCommandService; `ShipmentStatus` ← LegacyOrderShipmentCommandController, LegacyOrderShipmentRepository, LegacySellerShipmentCommandService, LegacyShipmentItemProcessor, LegacyUpdateShipmentStatusesJobConfig
- **warehouseStorage**: `WarehouseStorageCommandService` ← LegacySellerShipmentCommandService

### 이 도메인을 참조하는 다른 도메인

- **legacysale**: LegacySellerShipmentCommandService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

