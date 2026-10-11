# address 도메인

기준 경로: `src/main/java/com/fream/back/domain/address/` · 파일 20개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| PATCH | `/shipments/order/{shipmentId}/status` | `FaqAddressCommandController#updateTrackingInfo` | `FaqAddressCommandService#updateTrackingInfo` |
| POST | `/shipments/order/{shipmentId}/check-status` | `FaqAddressCommandController#updateAndCheckStatus` | `FaqAddressCommandService#updateAndCheckShipmentStatus` |
| POST | `/shipments/order/{shipmentId}/check-status-string` | `FaqAddressCommandController#updateAndCheckStatusAsString` | `FaqAddressCommandService#updateAndCheckShipmentStatus` |
| POST | `/shipments/seller` | `SellerAddressCommandController#createSellerShipment` | `SellerAddressCommandService#createSellerShipment` |
| PATCH | `/shipments/seller/{shipmentId}` | `SellerAddressCommandController#updateShipment` | `SellerAddressCommandService#updateShipment` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `AddressItemProcessor`
  - `process` → PlaywrightBrowserManager#newPage, this#parseStatusFromHtml, this#mapToShipmentStatus, FaqAddressCommandService#completeOrder, PaymentCommandService#notifyShipmentCompleted
- `BrowserManageStepListener`
  - `beforeStep` → PlaywrightBrowserManager#openBrowser
  - `afterStep` → PlaywrightBrowserManager#closeBrowser
- `FaqAddressCommandService`
  - `createOrderShipment` → FaqAddressRepository#save~
  - `updateShipmentStatus` → FaqAddressRepository#save~
  - `updateSaleStatusToSold` → StyleBidQueryService#findByOrderId, NoticeCommandService#updateWarehouseStatusToSold, PaymentCommandService#createNotification, FaqBidQueryService#findByOrderId
  - `completeOrder` → FaqBidQueryService#findByOrderId, this#updateSaleStatusToSold, PaymentCommandService#createNotification
  - `updateTrackingInfo` → FaqAddressRepository#findById~, PaymentCommandService#createNotification, this#updateSaleStatusToSold, StyleBidQueryService#findByOrderId, NoticeCommandService#updateWarehouseStatusToSold, this#updateOrderAndSaleBidStatusToCompleted
  - `updateShipmentStatuses` → FaqAddressRepository#findByStatusIn, CjTrackingPlaywright#getCurrentTrackingStatus, this#mapToShipmentStatus, PaymentCommandService#createNotification, FaqAddressRepository#save~
  - `updateOrderAndSaleBidStatusToCompleted` → FaqBidQueryService#findByOrderId, StyleBidQueryService#findByOrderId
  - `updateAndCheckShipmentStatus` → FaqAddressRepository#findById~, CjTrackingPlaywright#getCurrentTrackingStatus, this#mapToShipmentStatus, this#completeOrder
- `SellerAddressCommandService`
  - `createSellerShipment` → StyleRepository#findById~, SellerAddressRepository#save~, NoticeCommandService#createSellerStorage
  - `updateShipment` → SellerAddressRepository#findById~
- `UpdateAddressStatusesJobConfig`
  - `updateShipmentStatusesJob` → this#updateShipmentStatusesStep
  - `updateShipmentStatusesStep` → this#shipmentItemReader, this#playwrightBrowserManager, this#shipmentJpaItemWriter, this#shipmentSkipListener
  - `shipmentItemProcessor` → CjTrackingPlaywright#getCurrentTrackingStatus, this#mapToShipmentStatus, FaqAddressCommandService#completeOrder, PaymentCommandService#notifyShipmentCompleted

## 3. 클래스 목록 (역할별)

### controller (2)

- `FaqAddressCommandController` controller/command/FaqAddressCommandController.java · public: updateTrackingInfo, updateAndCheckStatus, updateAndCheckStatusAsString · 주입: FaqAddressCommandService
- `SellerAddressCommandController` controller/command/SellerAddressCommandController.java · public: createSellerShipment, updateShipment · 주입: SellerAddressCommandService

### service-command (2)

- `FaqAddressCommandService` service/command/FaqAddressCommandService.java · public: createOrderShipment, updateShipmentStatus, updateSaleStatusToSold, completeOrder, updateTrackingInfo, updateShipmentStatuses, updateAndCheckShipmentStatus · 주입: FaqAddressRepository, PaymentCommandService, StyleBidQueryService, FaqBidQueryService, NoticeCommandService, CjTrackingPlaywright
- `SellerAddressCommandService` service/command/SellerAddressCommandService.java · public: createSellerShipment, updateShipment · 주입: SellerAddressRepository, StyleRepository, NoticeCommandService

### repository (2)

- `FaqAddressRepository` (interface) repository/FaqAddressRepository.java · extends/implements JpaRepository · public: findByStatusIn
- `SellerAddressRepository` (interface) repository/SellerAddressRepository.java · extends/implements JpaRepository

### entity (3)

- `AddressStatus` (enum) entity/AddressStatus.java · public: canTransitionTo
- `FaqAddress` entity/FaqAddress.java · extends/implements BaseTimeEntity · public: assignOrder, updateTrackingInfo, updateStatus
- `SellerAddress` entity/SellerAddress.java · extends/implements BaseTimeEntity · public: assignSale, updateTrackingInfo, updateStatus

### config (3)

- `AddressItemProcessor` config/AddressItemProcessor.java · extends/implements ItemProcessor · public: process · 주입: PlaywrightBrowserManager, PaymentCommandService, FaqAddressCommandService
- `BrowserManageStepListener` config/BrowserManageStepListener.java · extends/implements StepExecutionListenerSupport · public: beforeStep, afterStep · 주입: PlaywrightBrowserManager
- `UpdateAddressStatusesJobConfig` config/UpdateAddressStatusesJobConfig.java · public: updateShipmentStatusesJob, updateShipmentStatusesStep, shipmentItemReader, shipmentItemProcessor, shipmentJpaItemWriter, shipmentSkipListener, playwrightBrowserManager · 주입: JobRepository, PlatformTransactionManager, EntityManagerFactory, FaqAddressRepository, PaymentCommandService, FaqAddressCommandService, CjTrackingPlaywright

### dto (6)

- `AddressResponseDto` dto/AddressResponseDto.java
- `AddressStatusResponse` dto/AddressStatusResponse.java
- `AddressUpdateRequestDto` dto/AddressUpdateRequestDto.java
- `FaqAddressRequestDto` dto/FaqAddressRequestDto.java
- `SellerAddressRequestDto` dto/SellerAddressRequestDto.java
- `TrackingNumberRequestDto` dto/TrackingNumberRequestDto.java

### exception (2)

- `AddressErrorCode` exception/AddressErrorCode.java
- `AddressException` exception/AddressException.java

### 다른 도메인 참조 (import·주입 기준)

- **faq**: `Faq` ← FaqAddress, FaqAddressCommandService; `FaqBid` ← FaqAddressCommandService; `FaqBidQueryService` ← FaqAddressCommandService; `FaqStatus` ← FaqAddressCommandService
- **notice**: `NoticeCommandService` ← FaqAddressCommandService, SellerAddressCommandService
- **payment**: `PaymentCategory` ← FaqAddressCommandService; `PaymentCommandService` ← AddressItemProcessor, FaqAddressCommandService, UpdateAddressStatusesJobConfig; `PaymentType` ← FaqAddressCommandService
- **product**: `Product` ← FaqAddressCommandService
- **style**: `BidStatus` ← FaqAddressCommandService; `Style` ← FaqAddressCommandService, SellerAddress, SellerAddressCommandService; `StyleBid` ← FaqAddressCommandService; `StyleBidQueryService` ← FaqAddressCommandService; `StyleRepository` ← SellerAddressCommandService; `StyleStatus` ← FaqAddressCommandService, SellerAddressCommandService

### 이 도메인을 참조하는 다른 도메인

- **faq**: FaqAddress, FaqAddressCommandService
- **global**: AddressStatus, FaqAddress, FaqAddressCommandService, SellerAddress, SellerAddressRepository
- **style**: SellerAddress, SellerAddressCommandService

## 4. JPA 엔티티 연관

- `FaqAddress` @Entity · extends BaseTimeEntity
  - order: @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "order_id", nullable = false) → `Faq` [faq]
- `SellerAddress` @Entity · extends BaseTimeEntity
  - sale: @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "sale_id", nullable = false) → `Style` [style]

다른 도메인 엔티티에서 들어오는 연관:

- `Faq.orderShipment` @OneToOne → `FaqAddress` [faq]
- `Style.sellerShipment` @OneToOne → `SellerAddress` [style]

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

