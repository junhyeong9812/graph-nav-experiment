# partnershipment 도메인

기준 경로: `src/main/java/com/fream/back/domain/partnershipment/` · 파일 7개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| PATCH | `/partner/shipments/order/{shipmentId}/status` | `PartnerOrderShipmentCommandController#updateTrackingInfo` | `OrderShipmentCommandService#updateTrackingInfo` |
| POST | `/partner/shipments/order/{shipmentId}/check-status` | `PartnerOrderShipmentCommandController#updateAndCheckStatus` | `OrderShipmentCommandService#updateAndCheckShipmentStatus` |
| POST | `/partner/shipments/order/{shipmentId}/check-status-string` | `PartnerOrderShipmentCommandController#updateAndCheckStatusAsString` | `OrderShipmentCommandService#updateAndCheckShipmentStatus` |
| POST | `/partner/shipments/seller` | `PartnerSellerShipmentCommandController#createSellerShipment` | `PartnerSellerShipmentCommandService#createSellerShipment` |
| PATCH | `/partner/shipments/seller/{shipmentId}` | `PartnerSellerShipmentCommandController#updateShipment` | `PartnerSellerShipmentCommandService#updateShipment` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `PartnerBrowserManageStepListener`
  - `beforeStep` → PlaywrightBrowserManager#openBrowser
  - `afterStep` → PlaywrightBrowserManager#closeBrowser
- `PartnerSellerShipmentCommandService`
  - `createSellerShipment` → SaleRepository#findById~, SellerShipmentRepository#save~, WarehouseStorageCommandService#createSellerStorage
  - `updateShipment` → SellerShipmentRepository#findById~
- `PartnerShipmentItemProcessor`
  - `process` → PlaywrightBrowserManager#newPage, this#parseStatusFromHtml, this#mapToShipmentStatus, OrderShipmentCommandService#completeOrder, PartnerNotificationCommandService#notifyShipmentCompleted
- `PartnerUpdateShipmentStatusesJobConfig`
  - `updateShipmentStatusesJob` → this#updateShipmentStatusesStep
  - `updateShipmentStatusesStep` → this#shipmentItemReader, this#playwrightBrowserManager, this#shipmentJpaItemWriter, this#shipmentSkipListener
  - `shipmentItemProcessor` → CjTrackingPlaywright#getCurrentTrackingStatus, this#mapToShipmentStatus, OrderShipmentCommandService#completeOrder, PartnerNotificationCommandService#notifyShipmentCompleted

## 3. 클래스 목록 (역할별)

### controller (2)

- `PartnerOrderShipmentCommandController` controller/command/PartnerOrderShipmentCommandController.java · public: updateTrackingInfo, updateAndCheckStatus, updateAndCheckStatusAsString · 주입: OrderShipmentCommandService
- `PartnerSellerShipmentCommandController` controller/command/PartnerSellerShipmentCommandController.java · public: createSellerShipment, updateShipment · 주입: PartnerSellerShipmentCommandService

### service-command (1)

- `PartnerSellerShipmentCommandService` service/command/PartnerSellerShipmentCommandService.java · public: createSellerShipment, updateShipment · 주입: SellerShipmentRepository, SaleRepository, WarehouseStorageCommandService

### repository (1)

- `PartnerOrderShipmentRepository` (interface) repository/PartnerOrderShipmentRepository.java · extends/implements JpaRepository · public: findByStatusIn

### config (3)

- `PartnerBrowserManageStepListener` config/PartnerBrowserManageStepListener.java · extends/implements StepExecutionListenerSupport · public: beforeStep, afterStep · 주입: PlaywrightBrowserManager
- `PartnerShipmentItemProcessor` config/PartnerShipmentItemProcessor.java · extends/implements ItemProcessor · public: process · 주입: PlaywrightBrowserManager, PartnerNotificationCommandService, OrderShipmentCommandService
- `PartnerUpdateShipmentStatusesJobConfig` config/PartnerUpdateShipmentStatusesJobConfig.java · public: updateShipmentStatusesJob, updateShipmentStatusesStep, shipmentItemReader, shipmentItemProcessor, shipmentJpaItemWriter, shipmentSkipListener, playwrightBrowserManager · 주입: JobRepository, PlatformTransactionManager, EntityManagerFactory, PartnerOrderShipmentRepository, PartnerNotificationCommandService, OrderShipmentCommandService, CjTrackingPlaywright

### 다른 도메인 참조 (import·주입 기준)

- **partnernotification**: `PartnerNotificationCommandService` ← PartnerShipmentItemProcessor, PartnerUpdateShipmentStatusesJobConfig
- **sale**: `Sale` ← PartnerSellerShipmentCommandService; `SaleRepository` ← PartnerSellerShipmentCommandService; `SaleStatus` ← PartnerSellerShipmentCommandService
- **shipment**: `OrderShipment` ← PartnerOrderShipmentRepository, PartnerShipmentItemProcessor, PartnerUpdateShipmentStatusesJobConfig; `OrderShipmentCommandService` ← PartnerOrderShipmentCommandController, PartnerShipmentItemProcessor, PartnerUpdateShipmentStatusesJobConfig; `SellerShipment` ← PartnerSellerShipmentCommandController, PartnerSellerShipmentCommandService; `SellerShipmentRepository` ← PartnerSellerShipmentCommandService; `ShipmentStatus` ← PartnerOrderShipmentCommandController, PartnerOrderShipmentRepository, PartnerSellerShipmentCommandService, PartnerShipmentItemProcessor, PartnerUpdateShipmentStatusesJobConfig
- **warehouseStorage**: `WarehouseStorageCommandService` ← PartnerSellerShipmentCommandService

### 이 도메인을 참조하는 다른 도메인

- **partnersale**: PartnerSellerShipmentCommandService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

