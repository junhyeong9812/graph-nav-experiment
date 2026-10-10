# shipment 도메인 네비게이션 맵

> 기준 경로: `src/main/java/com/fream/back/domain/shipment` (20 files). 코드에서 확인된 사실만 기재.

## 1. 개요
- 구매자 배송(OrderShipment, Order 1:1)과 판매자 발송(SellerShipment, Sale 1:1).
- 운송장 등록 시 Order/Sale/Bid 상태 및 창고 상태를 연쇄 변경, CJ대한통운 추적은 Playwright 크롤링 + Spring Batch(6시간 주기).

## 2. 패키지/파일 구조
```
src/main/java/com/fream/back/domain/shipment/
├── config/ UpdateShipmentStatusesJobConfig.java (Batch Job), ShipmentItemProcessor.java, BrowserManageStepListener.java
├── controller/command/ OrderShipmentCommandController.java (/shipments/order), SellerShipmentCommandController.java (/shipments/seller)
├── dto/ OrderShipmentRequestDto(courier, trackingNumber), SellerShipmentRequestDto(saleId, courier, trackingNumber),
│        ShipmentResponseDto, ShipmentStatusResponse, ShipmentUpdateRequestDto, TrackingNumberRequestDto
├── entity/ OrderShipment, SellerShipment, ShipmentStatus
├── exception/ ShipmentErrorCode, ShipmentException
├── repository/ OrderShipmentRepository(findByStatusIn), SellerShipmentRepository
└── service/command/ OrderShipmentCommandService, SellerShipmentCommandService
```
global: `global/config/ShipmentBatchScheduler.java`, `global/config/BatchInfraConfig.java`(@EnableBatchProcessing, JobRepository), `global/utils/CjTrackingPlaywright.java`(@Configuration), `global/utils/PlaywrightBrowserManager.java`(@Component).

## 3. 엔티티
| 엔티티 | 필드 | 관계 |
|---|---|---|
| `OrderShipment` | receiverName, receiverPhone, postalCode, address, courier, trackingNumber, status | order(1:1 FK order_id, not null) |
| `SellerShipment` | courier, trackingNumber, status | sale(1:1 FK sale_id, not null) |

**ShipmentStatus** (`canTransitionTo` 추상 메서드):
| from | to |
|---|---|
| PENDING | SHIPPED, CANCELED |
| SHIPPED | IN_TRANSIT, RETURNED, CANCELED |
| IN_TRANSIT | OUT_FOR_DELIVERY, DELAYED, CANCELED, DELIVERED |
| OUT_FOR_DELIVERY | DELIVERED, FAILED_DELIVERY, CANCELED |
| DELAYED | IN_TRANSIT, CANCELED |
| FAILED_DELIVERY | RETURNED, OUT_FOR_DELIVERY, CANCELED |
| RETURNED | CANCELED |
| DELIVERED / CANCELED | 없음 |
- `updateTrackingInfo(courier, no)` (OrderShipment.java:41, SellerShipment.java:36): **검증 없이** status=IN_TRANSIT.
- `updateStatus(new)` (OrderShipment:47, SellerShipment:42): status null 또는 전이 허용 시만, 아니면 IllegalStateException. 같은 상태로의 전이(IN_TRANSIT→IN_TRANSIT)도 불허.

## 4. API 엔드포인트 (모두 인증 필요, 소유자 검증 없음)
| Method | Path | Controller#method | Service |
|---|---|---|---|
| PATCH | /shipments/order/{shipmentId}/status | OrderShipmentCommandController#updateTrackingInfo | OrderShipmentCommandService#updateTrackingInfo |
| POST | /shipments/order/{shipmentId}/check-status | #updateAndCheckStatus → `ShipmentStatusResponse` | OrderShipmentCommandService#updateAndCheckShipmentStatus |
| POST | /shipments/order/{shipmentId}/check-status-string | #updateAndCheckStatusAsString | 동일 |
| POST | /shipments/seller | SellerShipmentCommandController#createSellerShipment (body saleId) | SellerShipmentCommandService#createSellerShipment |
| PATCH | /shipments/seller/{shipmentId} | SellerShipmentCommandController#updateShipment | SellerShipmentCommandService#updateShipment |
- 판매자 발송은 sale 도메인 `POST /sales/{saleId}/shipment`(SaleCommandService#createSellerShipment)로도 가능 (추가로 Sale→IN_TRANSIT).
- SellerShipmentCommandController는 예외를 잡아 body 없는 상태코드만 반환.

## 5. 서비스 계층
- **OrderShipmentCommandService**
  - `createOrderShipment(order, ...)` (:56): status PENDING 저장. (order/OrderEventConsumer, OrderCommandService, DataInitializer에서 호출)
  - `updateTrackingInfo(shipmentId, courier, no)` (:239): `shipment.updateTrackingInfo` → `order.updateStatus(IN_TRANSIT)` → 구매자 알림 → `updateSaleStatusToSold(orderId)` → SaleBid의 Sale이 창고보관이면 `WarehouseStorageCommandService#updateWarehouseStatusToSold` → `updateOrderAndSaleBidStatusToCompleted` (OrderBid/SaleBid MATCHED→COMPLETED). 예외는 `ShipmentException(SHIPMENT_NOT_FOUND)`로 래핑.
  - `updateSaleStatusToSold(orderId)` (:112): SaleBid 경로/OrderBid 경로 각각 `canTransitionTo(SOLD)`면 SOLD + 판매자 알림, 창고보관이면 창고 SOLD.
  - `completeOrder(orderId)` (:191): `OrderBidQueryService#findByOrderId` → `order.updateStatus(COMPLETED)` → `updateSaleStatusToSold` → 구매자 알림.
  - `updateAndCheckShipmentStatus(...)` (:452): 송장 갱신 → `CjTrackingPlaywright#getCurrentTrackingStatus` → DELIVERED면 `completeOrder`, 배송출발이면 OUT_FOR_DELIVERY, 그 외 `updateStatus(IN_TRANSIT)`.
  - `updateShipmentStatuses()` (:309): 배치 대체 이전 버전, **호출처 없음**. `updateShipmentStatus` (:84).
  - 상태 텍스트 매핑 `mapToShipmentStatus`: "배송완료"→DELIVERED, "배송출발"→OUT_FOR_DELIVERY, 그 외→IN_TRANSIT.
- **SellerShipmentCommandService**
  - `createSellerShipment(saleId, courier, no)` (:39): SellerShipment(IN_TRANSIT) 저장, `sale.assignSellerShipment`; sale.isWarehouseStorage면 `WarehouseStorageCommandService#createSellerStorage` + `sale.updateStatus(IN_STORAGE)`(검증 없음).
  - `updateShipment(shipmentId, ...)` (:110): `updateTrackingInfo`.

## 6. Repositories
- `OrderShipmentRepository#findByStatusIn(List<ShipmentStatus>)`. `SellerShipmentRepository` 기본 CRUD. 배치 Reader는 JPQL 직접 사용.

## 7. 외부 연동 / 배치
- **스케줄**: `ShipmentBatchScheduler#scheduleShipmentStatusJob` `@Scheduled(cron = "0 0 */6 * * *")` → `JobLauncher.run(updateShipmentStatusesJob, timestamp param)`.
- **Job** `updateShipmentStatusesJob` → Step `updateShipmentStatusesStep` (chunk 50, faultTolerant skip(Exception) limit 50, SkipListener 로그):
  - Reader `shipmentItemReader`: `SELECT s FROM OrderShipment s WHERE s.status IN ('IN_TRANSIT','OUT_FOR_DELIVERY') AND s.trackingNumber IS NOT NULL` (page 50).
  - Processor: `new ShipmentItemProcessor(playwrightBrowserManager(), notificationService, orderService)` — 공유 브라우저에서 `https://trace.cjlogistics.com/next/tracking.html?wblNo=` 접속, `tbody#statusDetail tr` 마지막 행 5번째 td 파싱(Jsoup). DELIVERED → `updateStatus(DELIVERED)` + `OrderShipmentCommandService#completeOrder` + `NotificationCommandService#notifyShipmentCompleted`.
  - Writer `JpaItemWriter`. `BrowserManageStepListener`: beforeStep `openBrowser()`, afterStep `closeBrowser()`.
  - `UpdateShipmentStatusesJobConfig#shipmentItemProcessor()` 빈과 `parseCjLogisticsStatus`(Jsoup 직접 요청)은 Step에서 사용되지 않음.
- **CjTrackingPlaywright#getCurrentTrackingStatus(trackingNumber)**: 호출마다 Playwright 생성 + headless Chromium 실행 → 같은 URL/selector 파싱. 수동 API(`check-status`)에서 사용.
- **PlaywrightBrowserManager**: `openBrowser/closeBrowser`(synchronized), `newPage()`, `isInitialized()`. `@Component`이면서 JobConfig에서도 같은 이름의 `@Bean playwrightBrowserManager()` 정의.

## 8. 다른 도메인 의존
- order: `Order#updateStatus`, `OrderBidQueryService#findByOrderId`, order BidStatus. sale: `SaleBidQueryService#findByOrderId`, `Sale`, `SaleRepository`, sale BidStatus/SaleStatus.
- warehouseStorage: `createSellerStorage`, `updateWarehouseStatusToSold`. notification: `createNotification`, `notifyShipmentCompleted`.

## 9. 예외
- `ShipmentErrorCode` prefix **S**: S001~S005(일반/송장), S101~S103(주문배송), S201~S203(판매자배송), S301 TRACKING_HTML_PARSE_ERROR, S302 EXTERNAL_TRACKING_SERVICE_ERROR, S303 BROWSER_INITIALIZATION_ERROR. **SaleErrorCode(S001~)와 코드 충돌.** `ShipmentException extends GlobalException`.

## 10. 작업 팁 / 주의점
- `updateTrackingInfo`(OrderShipmentCommandService.java:260)는 `order.updateStatus(IN_TRANSIT)`인데 OrderStatus 전이표상 IN_TRANSIT는 SHIPMENT_STARTED에서만 가능하고 SHIPMENT_STARTED를 설정하는 코드가 없음 → PREPARING/COMPLETED 주문에서 InvalidOrderStatusException → ShipmentException(S001)로 래핑.
- `saleBidQueryService.findByOrderId`는 없으면 IllegalArgumentException을 던지므로 `if (saleBid != null)` 분기와 별개로 SaleBid 없는 주문에서 `updateSaleStatusToSold`/`updateTrackingInfo`가 예외로 끝남.
- `updateTrackingInfo` 시점(구매자 발송 시작)에 Sale→SOLD, Bid→COMPLETED 처리됨(배송 완료 시점 아님).
- `updateAndCheckShipmentStatus`에서 CJ 결과가 IN_TRANSIT이면 `updateStatus(IN_TRANSIT)`를 IN_TRANSIT 상태에서 호출 → IllegalStateException → S302로 래핑.
- `@Transactional private updateOrderAndSaleBidStatusToCompleted` — private이라 프록시 트랜잭션 미적용(내부 호출).
