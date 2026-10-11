# settlementshipment 도메인

기준 경로: `src/main/java/com/fream/back/domain/settlementshipment/` · 파일 7개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| PATCH | `/settlement/shipments/order/{shipmentId}/status` | `SettlementOrderShipmentCommandController#updateTrackingInfo` | `OrderShipmentCommandService#updateTrackingInfo` |
| POST | `/settlement/shipments/order/{shipmentId}/check-status` | `SettlementOrderShipmentCommandController#updateAndCheckStatus` | `OrderShipmentCommandService#updateAndCheckShipmentStatus` |
| POST | `/settlement/shipments/order/{shipmentId}/check-status-string` | `SettlementOrderShipmentCommandController#updateAndCheckStatusAsString` | `OrderShipmentCommandService#updateAndCheckShipmentStatus` |
| POST | `/settlement/shipments/seller` | `SettlementSellerShipmentCommandController#createSellerShipment` | `SettlementSellerShipmentCommandService#createSellerShipment` |
| PATCH | `/settlement/shipments/seller/{shipmentId}` | `SettlementSellerShipmentCommandController#updateShipment` | `SettlementSellerShipmentCommandService#updateShipment` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `SettlementBrowserManageStepListener`
  - `beforeStep` → PlaywrightBrowserManager#openBrowser
  - `afterStep` → PlaywrightBrowserManager#closeBrowser
- `SettlementSellerShipmentCommandService`
  - `createSellerShipment` → SaleRepository#findById~, SellerShipmentRepository#save~, WarehouseStorageCommandService#createSellerStorage
  - `updateShipment` → SellerShipmentRepository#findById~
- `SettlementShipmentItemProcessor`
  - `process` → PlaywrightBrowserManager#newPage, this#parseStatusFromHtml, this#mapToShipmentStatus, OrderShipmentCommandService#completeOrder, SettlementNotificationCommandService#notifyShipmentCompleted
- `SettlementUpdateShipmentStatusesJobConfig`
  - `updateShipmentStatusesJob` → this#updateShipmentStatusesStep
  - `updateShipmentStatusesStep` → this#shipmentItemReader, this#playwrightBrowserManager, this#shipmentJpaItemWriter, this#shipmentSkipListener
  - `shipmentItemProcessor` → CjTrackingPlaywright#getCurrentTrackingStatus, this#mapToShipmentStatus, OrderShipmentCommandService#completeOrder, SettlementNotificationCommandService#notifyShipmentCompleted

## 3. 클래스 목록 (역할별)

### controller (2)

- `SettlementOrderShipmentCommandController` controller/command/SettlementOrderShipmentCommandController.java · public: updateTrackingInfo, updateAndCheckStatus, updateAndCheckStatusAsString · 주입: OrderShipmentCommandService
- `SettlementSellerShipmentCommandController` controller/command/SettlementSellerShipmentCommandController.java · public: createSellerShipment, updateShipment · 주입: SettlementSellerShipmentCommandService

### service-command (1)

- `SettlementSellerShipmentCommandService` service/command/SettlementSellerShipmentCommandService.java · public: createSellerShipment, updateShipment · 주입: SellerShipmentRepository, SaleRepository, WarehouseStorageCommandService

### repository (1)

- `SettlementOrderShipmentRepository` (interface) repository/SettlementOrderShipmentRepository.java · extends/implements JpaRepository · public: findByStatusIn

### config (3)

- `SettlementBrowserManageStepListener` config/SettlementBrowserManageStepListener.java · extends/implements StepExecutionListenerSupport · public: beforeStep, afterStep · 주입: PlaywrightBrowserManager
- `SettlementShipmentItemProcessor` config/SettlementShipmentItemProcessor.java · extends/implements ItemProcessor · public: process · 주입: PlaywrightBrowserManager, SettlementNotificationCommandService, OrderShipmentCommandService
- `SettlementUpdateShipmentStatusesJobConfig` config/SettlementUpdateShipmentStatusesJobConfig.java · public: updateShipmentStatusesJob, updateShipmentStatusesStep, shipmentItemReader, shipmentItemProcessor, shipmentJpaItemWriter, shipmentSkipListener, playwrightBrowserManager · 주입: JobRepository, PlatformTransactionManager, EntityManagerFactory, SettlementOrderShipmentRepository, SettlementNotificationCommandService, OrderShipmentCommandService, CjTrackingPlaywright

### 다른 도메인 참조 (import·주입 기준)

- **sale**: `Sale` ← SettlementSellerShipmentCommandService; `SaleRepository` ← SettlementSellerShipmentCommandService; `SaleStatus` ← SettlementSellerShipmentCommandService
- **settlementnotification**: `SettlementNotificationCommandService` ← SettlementShipmentItemProcessor, SettlementUpdateShipmentStatusesJobConfig
- **shipment**: `OrderShipment` ← SettlementOrderShipmentRepository, SettlementShipmentItemProcessor, SettlementUpdateShipmentStatusesJobConfig; `OrderShipmentCommandService` ← SettlementOrderShipmentCommandController, SettlementShipmentItemProcessor, SettlementUpdateShipmentStatusesJobConfig; `SellerShipment` ← SettlementSellerShipmentCommandController, SettlementSellerShipmentCommandService; `SellerShipmentRepository` ← SettlementSellerShipmentCommandService; `ShipmentStatus` ← SettlementOrderShipmentCommandController, SettlementOrderShipmentRepository, SettlementSellerShipmentCommandService, SettlementShipmentItemProcessor, SettlementUpdateShipmentStatusesJobConfig
- **warehouseStorage**: `WarehouseStorageCommandService` ← SettlementSellerShipmentCommandService

### 이 도메인을 참조하는 다른 도메인

- **settlementsale**: SettlementSellerShipmentCommandService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

