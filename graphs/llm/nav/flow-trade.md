# 거래(Trade) 교차 도메인 흐름

> order / sale / payment / shipment / warehouseStorage 간 실제 호출 흐름. 경로 접두사 `src/main/java/com/fream/back/` 생략 시 `domain/...` 기준. 코드에서 확인된 사실만.

## 1. 구매 입찰 → 주문 → 결제 → 배송/창고
1. `POST /order-bids` → `order/controller/command/OrderBidCommandController#createOrderBid` → `order/service/command/OrderBidCommandService#createOrderBid`
2. → `OrderCommandService#createOrderFromBid`: `OrderItemCommandService#createOrderItem` + **Order(PENDING_PAYMENT)** 저장
3. → OrderBid 저장, `OrderBid#assignOrder` 때문에 **status=MATCHED** (PENDING 아님)
4. 결제/배송지: `POST /orders/{orderId}/process-payment-shipment` → `OrderCommandController#processPaymentAndShipment` → `order/service/kafka/OrderEventProducer#sendOrderProcessingEvent` (topic `order-processing-topic`) → **202**
5. `OrderEventConsumer#handleOrderProcessingEvent` → `processCompleteOrder`:
   - `payment/service/command/PaymentCommandService#processPayment` (CARD면 `PortOneApiClient#processCardPayment`) → `Order#assignPayment`
   - `isSuccess` false면 실패 → retry 토픽 / 실패 알림
   - `shipment/.../OrderShipmentCommandService#createOrderShipment` (ShipmentStatus PENDING)
   - Order → PAYMENT_COMPLETED; 창고보관이면 `WarehouseStorageCommandService#createOrderStorage` + PREPARING→IN_WAREHOUSE→COMPLETED, 아니면 PREPARING
   - 성공 시 WebSocket(`/user/{email}/queue/notifications`, `/topic/order/{id}`) + `notification-topic` 발행(코드 내 consumer 없음)
6. 상태 확인: `GET /orders/{orderId}/status` (`OrderQueryController#getOrderStatus`)

## 2. 즉시 구매 (기존 SaleBid 매칭)
`POST /order-bids/instant` → `OrderBidCommandController#createInstantOrderBid`(paymentRequest.userEmail 설정) → `OrderBidCommandService#createInstantOrderBid` → `SaleBidQueryService#findById` → `OrderCommandService#createInstantOrder`:
1. `AddressQueryService#getAddress` → Order(PENDING_PAYMENT) 저장 → paymentRequest.orderId 설정 → OrderItem
2. `OrderShipmentCommandService#createOrderShipment`(주소록 정보)
3. `SaleBid#assignOrder` + MATCHED, flush
4. Sale이 창고보관이면 `WarehouseStorageCommandService#updateWarehouseStatus(sale, ASSOCIATED_WITH_ORDER)`
5. `PaymentCommandService#processPayment` (동기) → `notifyUsersAndUpdateSaleStatus`: orderBid 아직 없음 → `SaleBidQueryService#findByOrderId` → 구매자/판매자 알림 + **Sale→SOLD**
6. 이후 OrderBid(MATCHED, isInstantPurchase, sale) 저장. **Order는 PENDING_PAYMENT 유지.** 응답 id는 OrderBid id.

## 3. 판매 입찰 / 즉시 판매
- 판매 입찰: `POST /sale-bids` → `sale/controller/command/SaleBidCommandController#createSaleBid` → `SaleBidCommandService#createSaleBid` → `SaleCommandService#createSale`(Sale PENDING_SHIPMENT + `BankAccountQueryService#getBankAccount` → SaleBankAccount + 판매자 알림) → SaleBid 저장 → `Sale#assignSaleBid` (SaleBid **MATCHED**).
- 즉시 판매(OrderBid 매칭): `POST /sale-bids/instant` → `SaleBidCommandService#createInstantSaleBid` → `SaleCommandService#createInstantSale`(Sale PENDING_SHIPMENT, SaleBankAccount, `OrderBid#assignSale`+MATCHED, 구매자 알림) → SaleBid(MATCHED, order=orderBid.order) 저장. 결제/주문 상태는 변경 안 함 (구매자가 4번 결제 API 호출 시 `notifyUsersAndUpdateSaleStatus`가 Sale→SOLD).

## 4. 결제 처리 / 실패 / 환불
- 유일한 결제 실행: `PaymentCommandService#processPayment` (호출: `OrderEventConsumer#processCompleteOrder`, `OrderCommandService#createInstantOrder`).
- 타입: CARD(PortOne `/subscribe/payments/onetime`, 저장 카드 복호화), ACCOUNT(`updateStatus(PENDING)`로 IllegalStateException 발생 → 실패), GENERAL(클라이언트 impUid 신뢰, PAID).
- 실패: 주문 Kafka 경로는 `OrderEventConsumer#handleOrderProcessingFailure` → `order-retry-topic`(최대 3회, sleep backoff) 또는 실패 알림. 트랜잭션 롤백으로 Order는 PENDING_PAYMENT 유지.
- Payment Kafka(`payment-processing-topic`/`payment-retry-topic`, `PaymentEventConsumer`)는 producer 호출처가 없어 사실상 미사용.
- 환불: `PaymentCommandService#refundPayment`(PortOne `/payments/cancel`) 존재하나 API/호출처 없음. Order REFUND_* 설정 코드 없음.

## 5. 판매자 발송 → (검수) → 구매자 배송 → CJ 추적
1. 판매자 발송: `POST /sales/{saleId}/shipment` → `SaleCommandService#createSellerShipment` → `SellerShipmentCommandService#createSellerShipment`(SellerShipment IN_TRANSIT; 창고보관이면 `createSellerStorage` + Sale→IN_STORAGE) → `SaleCommandService#updateSaleStatus(IN_TRANSIT)` (창고보관이면 IN_STORAGE→IN_TRANSIT 불허로 실패). 대안 `POST /shipments/seller`는 Sale 상태를 IN_TRANSIT로 바꾸지 않음.
2. 검수: **구현 없음** (IN_INSPECTION/FAILED_INSPECTION 설정 코드 없음).
3. 구매자 송장 등록: `PATCH /shipments/order/{shipmentId}/status` → `OrderShipmentCommandService#updateTrackingInfo`: OrderShipment→IN_TRANSIT, `Order#updateStatus(IN_TRANSIT)`(PREPARING에서 불허 → 실패), 알림, `updateSaleStatusToSold`, 창고 SOLD, OrderBid/SaleBid→COMPLETED.
4. 수동 추적: `POST /shipments/order/{id}/check-status` → `updateAndCheckShipmentStatus` → `global/utils/CjTrackingPlaywright#getCurrentTrackingStatus` → DELIVERED면 `completeOrder`.
5. 배치: `global/config/ShipmentBatchScheduler#scheduleShipmentStatusJob`(cron `0 0 */6 * * *`) → `shipment/config/UpdateShipmentStatusesJobConfig` `updateShipmentStatusesJob` → `ShipmentItemProcessor#process`(PlaywrightBrowserManager 공유 브라우저) → DELIVERED: `OrderShipmentCommandService#completeOrder`(Order→COMPLETED, Sale→SOLD) + `NotificationCommandService#notifyShipmentCompleted`.

## 6. 창고 보관 / 연장
- 구매 보관 생성: `OrderEventConsumer#processCompleteOrder` → `WarehouseStorageCommandService#createOrderStorage` (IN_STORAGE, +1개월).
- 판매자 보관 생성: `SellerShipmentCommandService#createSellerShipment` → `createSellerStorage`.
- 즉시구매 시 판매자 보관 → ASSOCIATED_WITH_ORDER; 구매자 송장 등록 시 → SOLD (`updateWarehouseStatusToSold`).
- 연장: `POST /warehouse-storage/commands/extend-period` → `WarehouseStorageCommandService#extendStoragePeriod` (newEndDate로 교체, 소유자·상태 검증). 만료 스케줄러 없음.

## 7. 다른 도메인 호출
- notification `NotificationCommandService#createNotification`: SaleCommandService(판매입찰 등록/즉시판매), PaymentCommandService(결제완료/환불), OrderShipmentCommandService(출발/판매완료/주문완료), `notifyShipmentCompleted`(배치).
- WebSocket `SimpMessagingTemplate` + Kafka `notification-topic`: OrderEventConsumer.
- user: UserQueryService, BankAccountQueryService(정산계좌), PaymentInfo(User.paymentInfos). address: AddressQueryService. product: ProductSizeQueryService; product가 `OrderBidRepository#tradeCountByColorIds`(COMPLETED 수) 사용.
- **포인트**: Order.usedPoints는 0 고정, user 포인트 차감/적립 호출 없음.

## 8. 상태 전이 표 (설정 위치)
**OrderStatus**
| 상태 | 설정 메서드 |
|---|---|
| PENDING_PAYMENT | `OrderCommandService#createOrderFromBid`, `#createInstantOrder` |
| PAYMENT_COMPLETED, PREPARING | `OrderEventConsumer#processCompleteOrder` (+ 미사용 `OrderCommandService#processPaymentAndShipment`) |
| IN_WAREHOUSE → COMPLETED | `OrderEventConsumer#processCompleteOrder` (창고보관) |
| IN_TRANSIT | `OrderShipmentCommandService#updateTrackingInfo` |
| COMPLETED | `OrderShipmentCommandService#completeOrder`, 미사용 `#updateShipmentStatuses` |
| SHIPMENT_STARTED, REFUND_REQUESTED, REFUNDED | 설정 코드 없음 |

**SaleStatus**
| 상태 | 설정 메서드 |
|---|---|
| PENDING_SHIPMENT | `SaleCommandService#createSale`, `#createInstantSale` |
| IN_TRANSIT | `SaleCommandService#createSellerShipment` (→ updateSaleStatus) |
| IN_STORAGE | `SellerShipmentCommandService#createSellerShipment` (창고보관) |
| SOLD | `PaymentCommandService#notifyUsersAndUpdateSaleStatus`(검증 없음), `OrderShipmentCommandService#updateSaleStatusToSold` |
| IN_INSPECTION, FAILED_INSPECTION, ON_AUCTION, SELLING, AUCTION_EXPIRED | 없음 |

**BidStatus (OrderBid / SaleBid)**
| 상태 | 설정 |
|---|---|
| MATCHED | `OrderBid#assignOrder/assignSale`, `SaleBid#assignSale/assignOrder`, `OrderCommandService#createInstantOrder`, `SaleCommandService#createInstantSale` |
| COMPLETED | `OrderShipmentCommandService#updateOrderAndSaleBidStatusToCompleted` (updateTrackingInfo에서) |
| PENDING (실질적으로 유지 안 됨), CANCELLED (없음) | |

**ShipmentStatus (OrderShipment / SellerShipment)**
| 상태 | 설정 |
|---|---|
| PENDING | `OrderShipmentCommandService#createOrderShipment` |
| IN_TRANSIT | `OrderShipment/SellerShipment#updateTrackingInfo`, `SellerShipmentCommandService#createSellerShipment`, 배치/수동 체크 |
| OUT_FOR_DELIVERY, DELIVERED | `ShipmentItemProcessor#process`, `OrderShipmentCommandService#updateAndCheckShipmentStatus` |

**PaymentStatus**: PAID(`createCardPayment` 성공 응답, `createGeneralPayment`), REFUNDED/REFUND_REQUESTED(`refundPayment`, 미호출).

**WarehouseStatus**: IN_STORAGE(`createOrderStorage`, `createSellerStorage`), ASSOCIATED_WITH_ORDER(`OrderCommandService#createInstantOrder`), SOLD(`updateWarehouseStatusToSold`).
