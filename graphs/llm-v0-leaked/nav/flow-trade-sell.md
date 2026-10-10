# 흐름: 판매 거래 엔드투엔드 (판매 입찰 / 즉시 판매 → 발송 → 창고 → 구매자 배송 → 완료)

> 관련 문서: `nav/domain-sale.md`, `nav/domain-shipment.md`, `nav/domain-warehouse-storage.md`. order/payment 내부는 해당 문서 참고.
> 모든 경로는 `src/main/java/com/fream/back/` 기준 상대 경로를 축약해 표기 (`domain/...`, `global/...`).

## 0. 한눈에 보기
```
[A] 판매 입찰  POST /sale-bids ─┐                         [B] 즉시 판매 POST /sale-bids/instant (OrderBid 대상)
     Sale(PENDING_SHIPMENT)      │                              Sale(PENDING_SHIPMENT) + OrderBid.sale 연결
     SaleBid(→MATCHED*)          │                              SaleBid(MATCHED, order=OrderBid.order)
     SaleBankAccount             │                              SaleBankAccount
                                 ▼
[A'] 구매자 즉시 구매 POST /order-bids/instant (SaleBid 대상)
     Order + OrderShipment(PENDING) + 결제 → Sale SOLD (PaymentCommandService)
                                 ▼
[C] 판매자 발송  POST /shipments/seller  또는  POST /sales/{saleId}/shipment
     SellerShipment(IN_TRANSIT); 창고보관이면 WarehouseStorage(IN_STORAGE) + Sale IN_STORAGE
                                 ▼
[D] (검수 단계 코드 없음)
                                 ▼
[E] 구매자 운송장 등록 PATCH /shipments/order/{id}/status
     OrderShipment IN_TRANSIT, Order IN_TRANSIT, Sale SOLD(가능 시), 창고 SOLD, OrderBid/SaleBid COMPLETED
                                 ▼
[F] 배송 추적: 배치(6시간) 또는 POST /shipments/order/{id}/check-status
     CJ "배송완료" → OrderShipment DELIVERED, completeOrder → Order COMPLETED, 알림
```
`*` 일반 판매 입찰은 `sale.assignSaleBid()` 부작용으로 생성 직후 MATCHED가 됨 (Gotcha 1).

## 1. 판매 등록

### [A] 판매 입찰 (`POST /sale-bids`)
| # | 호출 | 파일 | 효과 |
|---|---|---|---|
| 1 | `SaleBidCommandController.createSaleBid` | `domain/sale/controller/command/SaleBidCommandController.java` | 이메일 추출, DTO 검증 없음(@Valid 없음) |
| 2 | `SaleBidCommandService.createSaleBid` | `domain/sale/service/command/SaleBidCommandService.java` | User/ProductSize 조회 |
| 3 | `SaleCommandService.createSale` | `domain/sale/service/command/SaleCommandService.java` | `BankAccountQueryService.getBankAccount`(없으면 예외) → **Sale(PENDING_SHIPMENT, isWarehouseStorage)** 저장 |
| 4 | `SaleBankAccountCommandService.createSaleBankAccount` | `domain/sale/service/command/SaleBankAccountCommandService.java` | **SaleBankAccount** (판매자 BankAccount 복사) + `sale.assignSaleBankAccount` |
| 5 | `NotificationCommandService.createNotification(seller, SHOPPING, BID, "판매 입찰이 등록되었습니다.")` | `domain/notification/...` | 판매자 알림 |
| 6 | `saleBidRepository.save(SaleBid PENDING)` → `sale.assignSaleBid(saleBid)` | | `SaleBid.assignSale()`가 **status=MATCHED** 설정 |

### [A'] 구매자가 판매 입찰을 즉시 구매 (order 도메인, 참고)
`POST /order-bids/instant` → `OrderBidCommandService.createInstantOrderBid` → `OrderCommandService.createInstantOrder`:
Order(PENDING_PAYMENT) → OrderItem → `OrderShipmentCommandService.createOrderShipment`(**OrderShipment PENDING**) → `saleBid.assignOrder(order)` + MATCHED → Sale이 창고보관이면 `WarehouseStorageCommandService.updateWarehouseStatus(sale, ASSOCIATED_WITH_ORDER)` → `PaymentCommandService.processPayment` → `notifyUsersAndUpdateSaleStatus` → OrderBid가 없으면 `saleBidQueryService.findByOrderId`로 Sale 찾아 판매자 알림 + **`sale.updateStatus(SOLD)`** (전환 규칙 미검사). 이후 `OrderBidCommandService`가 OrderBid(MATCHED, sale 연결, instantPurchase) 생성.
(주의: `notifyUsersAndUpdateSaleStatus`가 결제 시점에 실행되는데 OrderBid는 결제 후 생성되므로 SaleBid 경로를 탐. 예외는 로그만 남김.)

### [B] 즉시 판매 — 기존 구매 입찰(OrderBid)에 판매 (`POST /sale-bids/instant`)
| # | 호출 | 효과 |
|---|---|---|
| 1 | `SaleBidCommandController.createInstantSale` | |
| 2 | `SaleBidCommandService.createInstantSaleBid` → `SaleCommandService.createInstantSale` | OrderBid 조회 → **Sale(PENDING_SHIPMENT, isWarehouseStorage=false)** → SaleBankAccount → `orderBid.assignSale(sale)` + `orderBid.updateStatus(order.BidStatus.MATCHED)` → 구매자 알림 "구매 입찰에 판매자가 등록되었습니다. 확인해보세요!" |
| 3 | (다시 `SaleBidCommandService`) | OrderBid 재조회 → **SaleBid(MATCHED, bidPrice=OrderBid 가격, order=orderBid.getOrder())** 저장 → saleId 반환 |
- OrderBid 상태(PENDING 여부)/중복 매칭 검증 없음, `isInstantSale` 미설정.
- 구매자 결제·배송정보: 구매 입찰 생성 시 만들어진 Order(`OrderCommandService.createOrderFromBid`)에 대해 `POST /orders/{orderId}/process-payment-shipment` → Kafka `order-processing-topic` → `OrderEventConsumer.processCompleteOrder`: 결제(`PaymentCommandService.processPayment` → Sale SOLD 가능) → **OrderShipment PENDING** 생성 → Order PAYMENT_COMPLETED → 창고보관 구매면 `createOrderStorage` + Order 상태 PREPARING/IN_WAREHOUSE/COMPLETED, 아니면 PREPARING. (호출 순서 – 즉시 판매 전/후 결제 – 를 강제하는 코드는 확인되지 않음 (미확인))

## 2. 판매자 발송 [C]
두 진입점의 차이가 중요:
| 진입점 | 호출 체인 | Sale 상태 |
|---|---|---|
| `POST /shipments/seller` (`SellerShipmentCommandController.createSellerShipment`) | `SellerShipmentCommandService.createSellerShipment` | 창고보관: `IN_STORAGE`(직접 설정), 아니면 변경 없음 |
| `POST /sales/{saleId}/shipment` (`SaleCommandController.createSellerShipment`) | `SaleCommandService.createSellerShipment` → 위 메서드 → `SaleCommandService.updateSaleStatus(saleId, IN_TRANSIT)` | 비창고 PENDING_SHIPMENT→**IN_TRANSIT**. 창고보관(IN_STORAGE)·이미 SOLD면 `IllegalStateException`으로 전체 롤백 |

`SellerShipmentCommandService.createSellerShipment` 상세 (`domain/shipment/service/command/SellerShipmentCommandService.java`):
1. courier/tracking 공백 → `ShipmentException(TRACKING_INFO_REQUIRED)`.
2. Sale 조회 (`SALE_NOT_FOUND_FOR_SHIPMENT`).
3. **SellerShipment(IN_TRANSIT)** 저장, `sale.assignSellerShipment`.
4. `sale.isWarehouseStorage()` → `WarehouseStorageCommandService.createSellerStorage(sale, seller)` (**WarehouseStorage IN_STORAGE**, "Seller's Warehouse", 1개월) → `sale.updateStatus(IN_STORAGE)`.
- 운송장 수정: `PATCH /shipments/seller/{id}` → `updateShipment` (status=IN_TRANSIT 재설정).

## 3. 검수/입고 [D]
- `SaleStatus`에 `IN_INSPECTION`, `FAILED_INSPECTION`, `IN_STORAGE→ON_AUCTION/SELLING` 등이 정의되어 있으나 **이를 설정하는 서비스/엔드포인트 없음**. `domain/inspection`은 검수 기준(InspectionStandard) 게시물 도메인으로 Sale과 무관(grep).
- SellerShipment의 입고 완료(DELIVERED) 처리 코드 없음; 배치는 OrderShipment만 대상.

## 4. 구매자 배송 [E] (`PATCH /shipments/order/{shipmentId}/status`)
`OrderShipmentCommandService.updateTrackingInfo` (`domain/shipment/service/command/OrderShipmentCommandService.java`):
1. 공백 검사 → `TRACKING_INFO_REQUIRED`.
2. `shipment.updateTrackingInfo` → **OrderShipment IN_TRANSIT** (규칙 미검사).
3. `order.updateStatus(OrderStatus.IN_TRANSIT)` (OrderStatus 규칙상 SHIPMENT_STARTED에서만 허용 — Gotcha 4).
4. 구매자 알림 "상품이 출발하였습니다."
5. `updateSaleStatusToSold(orderId)`: SaleBid(orderId)→Sale이 `canTransitionTo(SOLD)`이면 **Sale SOLD**, 창고보관이면 `updateWarehouseStatusToSold`(**창고 SOLD**), 판매자 알림 "상품이 성공적으로 판매 완료되었습니다."; OrderBid→Sale도 동일 처리.
6. SaleBid의 Sale이 창고보관이면 다시 `updateWarehouseStatusToSold` (이미 SOLD면 예외 — Gotcha 5).
7. `updateOrderAndSaleBidStatusToCompleted`: **OrderBid MATCHED→COMPLETED, SaleBid MATCHED→COMPLETED** (실패는 로그만).

## 5. 배송 추적 [F]
### 배치 (주 경로)
`global/config/ShipmentBatchScheduler.scheduleShipmentStatusJob` (`cron = "0 0 */6 * * *"`) → `JobLauncher.run(updateShipmentStatusesJob, timestamp)` →
`domain/shipment/config/UpdateShipmentStatusesJobConfig`: Step chunk 50, skip(Exception) limit 50
- Reader `shipmentItemReader`: OrderShipment `status IN ('IN_TRANSIT','OUT_FOR_DELIVERY') AND trackingNumber IS NOT NULL`
- Listener `BrowserManageStepListener`: `PlaywrightBrowserManager.openBrowser()/closeBrowser()`
- Processor `ShipmentItemProcessor.process`: `browserManager.newPage()` → `https://trace.cjlogistics.com/next/tracking.html?wblNo={no}` → `waitForSelector("tbody#statusDetail tr")` → Jsoup으로 마지막 행 5번째 `td` 텍스트 → 매핑(`배송완료`→DELIVERED, `배송출발`→OUT_FOR_DELIVERY, 그 외→IN_TRANSIT)
  - DELIVERED: `orderShipment.updateStatus(DELIVERED)` → `OrderShipmentCommandService.completeOrder(orderId)` (Order **COMPLETED**, `updateSaleStatusToSold`, 구매자 알림 "[completeOrder] 주문이 완료 처리되었습니다.") → `NotificationCommandService.notifyShipmentCompleted(order)` ("[배송 완료] …")
  - OUT_FOR_DELIVERY / IN_TRANSIT: `updateStatus`
- Writer `shipmentJpaItemWriter` (JpaItemWriter)

### 단건 즉시 조회
`POST /shipments/order/{id}/check-status` (또는 `/check-status-string`) → `OrderShipmentCommandService.updateAndCheckShipmentStatus` → `global/utils/CjTrackingPlaywright.getCurrentTrackingStatus` (요청마다 Chromium 실행) → 동일 매핑 → DELIVERED면 `completeOrder`.

## 6. 정산
- 판매 정산 계좌는 `SaleBankAccount`로 Sale 생성 시 스냅샷 저장(`user` 도메인 `/bank-account`에서 등록한 BankAccount 기반).
- **정산(대금 지급) 처리 로직은 코드에서 찾을 수 없음** (grep "정산|settle" → BankAccount 관련 문구만).

## 7. 상태 머신 요약 (어떤 메서드가 어떤 값을 설정하는가)

### SaleStatus (`domain/sale/entity/SaleStatus.java`)
| 값 | 설정 위치 | 규칙 검사 |
|---|---|---|
| PENDING_SHIPMENT | `SaleCommandService.createSale`, `createInstantSale` | 생성 |
| IN_TRANSIT | `SaleCommandService.createSellerShipment` → `updateSaleStatus` | O |
| IN_STORAGE | `SellerShipmentCommandService.createSellerShipment` (창고보관) | X (직접) |
| SOLD | `PaymentCommandService.notifyUsersAndUpdateSaleStatus` | X |
| SOLD | `OrderShipmentCommandService.updateSaleStatusToSold` (← `updateTrackingInfo`, `completeOrder`) | O (`canTransitionTo(SOLD)`: ON_AUCTION/SELLING에서만 true) |
| IN_INSPECTION, FAILED_INSPECTION, ON_AUCTION, SELLING, AUCTION_EXPIRED | 설정 코드 없음 | — |

### sale BidStatus (`domain/sale/entity/BidStatus.java`)
| 값 | 설정 위치 |
|---|---|
| PENDING | `SaleBid` 기본값 / `createSaleBid` 빌더 (곧바로 MATCHED로 덮어씀) |
| MATCHED | `SaleBid.assignSale()`, `SaleBid.assignOrder()`, `createInstantSaleBid` 빌더, `OrderCommandService.createInstantOrder` |
| COMPLETED | `OrderShipmentCommandService.updateOrderAndSaleBidStatusToCompleted` (← `updateTrackingInfo`) |
| CANCELLED | 설정 코드 없음 (카운트 쿼리에서만 사용). 삭제는 `deleteSaleBid`로 물리 삭제 |

### ShipmentStatus (`domain/shipment/entity/ShipmentStatus.java`)
| 엔티티 | 값 | 설정 위치 |
|---|---|---|
| OrderShipment | PENDING | `OrderShipmentCommandService.createOrderShipment` |
| OrderShipment | IN_TRANSIT | `OrderShipment.updateTrackingInfo` (← `updateTrackingInfo`, `updateAndCheckShipmentStatus`), 배치/단건 매핑 결과 |
| OrderShipment | OUT_FOR_DELIVERY | 배치 Processor, `updateAndCheckShipmentStatus`, `updateShipmentStatuses`(미호출) |
| OrderShipment | DELIVERED | 배치 Processor, `updateAndCheckShipmentStatus` |
| SellerShipment | IN_TRANSIT | `SellerShipmentCommandService.createSellerShipment`, `updateShipment` |
| (공통) | SHIPPED, DELAYED, FAILED_DELIVERY, RETURNED, CANCELED | 설정 코드 없음 |

### WarehouseStatus (`domain/warehouseStorage/entity/WarehouseStatus.java`)
| 값 | 설정 위치 |
|---|---|
| IN_STORAGE | `WarehouseStorageCommandService.createSellerStorage` (판매자 발송 시), `createOrderStorage` (구매자 창고보관 결제 시) |
| ASSOCIATED_WITH_ORDER | `OrderCommandService.createInstantOrder` → `updateWarehouseStatus` |
| SOLD | `WarehouseStorageCommandService.updateWarehouseStatusToSold` (← `OrderShipmentCommandService.updateSaleStatusToSold`, `updateTrackingInfo`) |
| ON_AUCTION, REMOVED_FROM_STORAGE | 설정 코드 없음 |

### (참고) Order / order BidStatus 중 판매 흐름이 건드리는 값
- Order: IN_TRANSIT(`updateTrackingInfo`), COMPLETED(`completeOrder`, `updateShipmentStatuses`).
- OrderBid: MATCHED(`SaleCommandService.createInstantSale`), COMPLETED(`updateOrderAndSaleBidStatusToCompleted`).

## 8. 알림 (모두 `NotificationCategory.SHOPPING`, `NotificationType.BID`)
| 시점 | 수신 | 메시지 | 위치 |
|---|---|---|---|
| 판매 입찰 등록 | 판매자 | "판매 입찰이 등록되었습니다." | `SaleCommandService.createSale` |
| 즉시 판매 | 구매자 | "구매 입찰에 판매자가 등록되었습니다. 확인해보세요!" | `SaleCommandService.createInstantSale` |
| 결제 완료 | 판매자 등 | "구매자가 결제를 완료하였습니다. 판매 ID: …" | `PaymentCommandService.sendNotifications` |
| 운송장 등록 | 구매자 | "상품이 출발하였습니다. 주문 ID: …" | `OrderShipmentCommandService.updateTrackingInfo` |
| 판매 완료 | 판매자 | "상품이 성공적으로 판매 완료되었습니다. 판매 ID: …" | `updateSaleStatusToSold` |
| 주문 완료 | 구매자 | "[completeOrder] 주문이 완료 처리되었습니다. …" | `completeOrder` |
| 배송 완료(배치) | 구매자 | "[배송 완료] 상품이 배송 완료되었습니다. …" | `NotificationCommandService.notifyShipmentCompleted` |

## 9. Gotchas (흐름 관점)
1. 일반 판매 입찰도 생성 직후 SaleBid=MATCHED (`Sale.assignSaleBid`→`SaleBid.assignSale`).
2. 결제 시 `PaymentCommandService`가 Sale을 PENDING_SHIPMENT에서 바로 SOLD로 바꿈(규칙 미검사). 이후 `/sales/{saleId}/shipment`는 SOLD→IN_TRANSIT 불가로 실패, `updateSaleStatusToSold`는 SOLD→SOLD 불가로 스킵.
3. `SaleStatus` 규칙상 SOLD는 ON_AUCTION/SELLING에서만 가능한데 이 두 값을 설정하는 코드가 없어, `OrderShipmentCommandService.updateSaleStatusToSold`의 SOLD 전환은 실제로는 발동하지 않는 구조.
4. `updateTrackingInfo`의 Order IN_TRANSIT 전환은 `SHIPMENT_STARTED`에서만 허용되며 이를 설정하는 코드 없음 → PREPARING 주문에서 호출 시 실패(`SHIPMENT_NOT_FOUND`로 래핑).
5. 창고 SOLD 업데이트가 한 요청에서 두 번 호출될 수 있음(`updateSaleStatusToSold` 내부 + 직후).
6. `SaleBidQueryService.findByOrderId`는 SaleBid 없으면 예외 → SaleBid 없이 생성된 주문(순수 구매입찰 등)은 운송장 등록/배송완료 처리 실패.
7. 판매자 발송 진입점 두 개(`/shipments/seller` vs `/sales/{id}/shipment`)의 Sale 상태 처리 결과가 다름.
8. 테스트: sale 도메인 단위/리포지토리 테스트만 존재 (`src/test/java/com/fream/back/domain/sale/**`). shipment/warehouseStorage/배치 전용 테스트 없음.
