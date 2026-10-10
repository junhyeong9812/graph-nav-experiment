# 흐름: 구매(Buy) 거래 end-to-end

> 관련 문서: [domain-order.md](domain-order.md), [domain-payment.md](domain-payment.md)
> 경로 약어: `order/` = `src/main/java/com/fream/back/domain/order/`, `payment/` = `.../domain/payment/`, `sale/`, `shipment/`, `warehouseStorage/`, `notification/` 동일 규칙.

구매 경로는 세 가지다.

| 경로 | 시작 API | 결제 시점 | 결제 방식 |
|---|---|---|---|
| A. 구매 입찰 | `POST /order-bids` | 나중에 (C) | Kafka 비동기 |
| B. 즉시 구매 (SaleBid 대상) | `POST /order-bids/instant` | 요청 안에서 동기 | 같은 트랜잭션 |
| C. 입찰 주문 결제·배송 | `POST /orders/{orderId}/process-payment-shipment` | Kafka 컨슈머 | `order-processing-topic` |

판매자가 구매 입찰에 응하는 경로(`POST /sale-bids/instant`)는 A와 C 사이에서 일어난다.

---

## A. 구매 입찰 생성 — `POST /order-bids`

```
OrderBidCommandController.createOrderBid                       order/controller/command/OrderBidCommandController.java
 └ SecurityUtils.extractAndValidateEmailForOrderBid             global/utils/SecurityUtils.java
 └ OrderBidCommandService.createOrderBid  @Transactional        order/service/command/OrderBidCommandService.java
    ├ validateCreateOrderBidInput  (email, productSizeId, bidPrice>0 → InvalidBidPriceException)
    ├ UserQueryService.findByEmail
    ├ ProductSizeQueryService.findById  (없으면 ProductSizeNotFoundException)
    ├ OrderCommandService.createOrderFromBid                    order/service/command/OrderCommandService.java
    │   ├ OrderItemCommandService.createOrderItem(null, size, bidPrice)  quantity=1
    │   ├ Order.builder(totalAmount=bidPrice, status=PENDING_PAYMENT)
    │   └ order.addOrderItem → orderRepository.save
    └ createAndSaveOrderBid: OrderBid(status=PENDING) → orderBid.assignOrder(order) ⇒ status=MATCHED → save
```
결과: `Order=PENDING_PAYMENT`, `OrderBid=MATCHED`(assignOrder 부작용, 판매자 미정 상태여도 MATCHED), Payment/Shipment 없음.

### A'. 판매자가 구매 입찰에 즉시 판매 — `POST /sale-bids/instant` (sale 도메인)
```
SaleBidCommandController (sale/controller/command/SaleBidCommandController.java)
 └ SaleBidCommandService.createInstantSaleBid  @Transactional   sale/service/command/SaleBidCommandService.java
    ├ SaleCommandService.createInstantSale                      sale/service/command/SaleCommandService.java
    │   ├ OrderBidQueryService.findById(orderBidId)
    │   ├ Sale(status=PENDING_SHIPMENT) 저장, SaleBankAccount 생성
    │   ├ orderBid.assignSale(sale); orderBid.updateStatus(order.BidStatus.MATCHED)
    │   └ NotificationCommandService.createNotification(구매자, SHOPPING, BID, "구매 입찰에 판매자가 등록되었습니다...")
    └ SaleBid(status=sale.BidStatus.MATCHED, sale, order=orderBid.getOrder()) 저장
```
OrderBid 상태 검사(PENDING 여부)는 하지 않는다.

---

## B. 즉시 구매 — `POST /order-bids/instant`

요청: `InstantOrderRequestDto { saleBidId, addressId, warehouseStorage, paymentRequest(paymentType=CARD|ACCOUNT|GENERAL ...) }`

```
OrderBidCommandController.createInstantOrderBid
 ├ enrichPaymentRequest: paymentRequest.userEmail=email, orderId=null
 └ OrderBidCommandService.createInstantOrderBid  @Transactional
    ├ validateInstantOrderBidInput
    ├ UserQueryService.findByEmail
    ├ getSaleBidAndValidate → SaleBidQueryService.findById (없으면 IllegalArgumentException → ORD102), sale null → InvalidOrderBidDataException
    ├ OrderCommandService.createInstantOrder  @Transactional(참여)
    │   1 AddressQueryService.getAddress(email, addressId)
    │   2 Order(totalAmount=saleBid.bidPrice, PENDING_PAYMENT) save
    │   3 paymentRequest.setOrderId(order.id)
    │   4 OrderItemCommandService.createOrderItem(order, saleBid.productSize, saleBid.bidPrice)
    │   5 OrderShipmentCommandService.createOrderShipment(order, 주소록 수령인/전화/우편/주소)   shipment/service/command/OrderShipmentCommandService.java  (ShipmentStatus.PENDING)
    │   6 saleBid.assignOrder(order) (+status MATCHED) ; saleBid.updateStatus(sale.BidStatus.MATCHED) ; entityManager.flush()
    │   7 sale.isWarehouseStorage() 이면 WarehouseStorageCommandService.updateWarehouseStatus(sale, ASSOCIATED_WITH_ORDER)   warehouseStorage/service/command/WarehouseStorageCommandService.java
    │   8 PaymentCommandService.processPayment(order, buyer, paymentRequest)   ← §D
    └ createInstantOrderBid(order, saleBid, sale): OrderBid(MATCHED, sale, bidPrice=saleBid.bidPrice) → assignOrder → markAsInstantPurchase → save
```
결과: `Order=PENDING_PAYMENT`(상태 갱신 코드 없음), `OrderBid=MATCHED, isInstantPurchase=true`, `SaleBid=MATCHED`, Payment 생성(타입별 상태 §D), `Sale=SOLD`(§D 성공 시), 구매자 창고보관 플래그(`warehouseStorage`)는 사용되지 않음.
어느 단계든 예외 발생 시 전체 트랜잭션 롤백 (PortOne 카드 승인은 외부에서 이미 완료됐을 수 있으며 자동 취소 코드는 없음).

---

## C. 입찰 주문 결제·배송 — `POST /orders/{orderId}/process-payment-shipment`

요청: `PayAndShipmentRequestDto { paymentRequest, receiverName, receiverPhone, postalCode, address, warehouseStorage }`
※ `paymentRequest.orderId`/`userEmail`은 컨트롤러가 채우지 않으므로 **클라이언트가 직접 넣어야** `processPayment` 검증을 통과한다.

```
OrderCommandController.processPaymentAndShipment              order/controller/command/OrderCommandController.java
 └ OrderEventProducer.sendOrderProcessingEvent(orderId, email, dto)   order/service/kafka/OrderEventProducer.java
     topic=order-processing-topic, key=orderId, OrderProcessingEvent(retryCount=0, eventId=order_processing_{id}_{ms})
 ← 202 ACCEPTED { status: PROCESSING, websocketTopic: /topic/order/{id}, statusCheckUrl: /api/orders/{id}/status }

[Kafka] OrderEventConsumer.handleOrderProcessingEvent  @Transactional, group=order-processing-group   order/service/kafka/OrderEventConsumer.java
 1 isOrderAlreadyProcessed: Order.status != PENDING_PAYMENT → ack & return
 2 orderRepository.findById / UserQueryService.findByEmail / 소유자 검증 (불일치 → IllegalArgumentException)
 3 processCompleteOrder
    a PaymentCommandService.processPayment → order.assignPayment(payment)
      payment.isSuccess()==false → RuntimeException
    b OrderShipmentCommandService.createOrderShipment(요청 수령인 정보) → order.assignOrderShipment
    c order.updateStatus(PAYMENT_COMPLETED)
    d warehouseStorage=true:  WarehouseStorageCommandService.createOrderStorage(order,user) (WarehouseStatus.IN_STORAGE, 1개월)
                               → PREPARING → IN_WAREHOUSE → COMPLETED
      warehouseStorage=false: → PREPARING
    e orderRepository.save
 4 sendSuccessNotification: WebSocket convertAndSendToUser(email,"/queue/notifications") + convertAndSend("/topic/order/{id}")
                            + Kafka notification-topic {type: ORDER_COMPLETION}
 5 ack
 실패 → handleOrderProcessingFailure (§F)
```
OrderBid 상태는 이 경로에서 바뀌지 않는다(이미 MATCHED). 미사용 동기 버전 `OrderCommandService.processPaymentAndShipment`도 같은 상태 전이를 수행한다.

---

## D. 결제 처리 공통 — `PaymentCommandService.processPayment` (`payment/service/command/PaymentCommandService.java`, 클래스 `@Transactional`)

```
validatePaymentRequest: order.id==dto.orderId, user.email==dto.userEmail, paidAmount>0  (위반 → PAY004/PAY007)
switch dto.getResolvedPaymentType()
 CARD    → createCardPayment
             PaymentInfoQueryService.getPaymentInfoEntity(email, paymentInfoId)  (decrypt 사본)
             PortOneApiClient.processCardPayment(info, amount)                   payment/portone/PortOneApiClient.java
               getAccessToken: POST https://api.iamport.kr/users/getToken
               POST /subscribe/payments/onetime (pg=nice, merchant_uid=UUID), code!=0 → PaymentProcessingException
             CardPayment(impUid, receiptUrl, pgProvider, pgTid, cardType=card_name) ; response.status=="success" → PAID + success
 ACCOUNT → createAccountPayment: Bank.valueOf 검증 → updateStatus(PENDING) ⇒ IllegalStateException ⇒ 항상 PAY001 실패
 GENERAL → createGeneralPayment: impUid 필수 → success=true, PAID (PortOne 조회 검증 없음)
notifyUsersAndUpdateSaleStatus (예외는 로그만)
 Sale 찾기: order.getOrderBid().getSale()  또는 (OrderBid 없음 = 즉시구매 B) SaleBidQueryService.findByOrderId(order.id).getSale()
 NotificationCommandService.createNotification(구매자, SHOPPING, BID, "결제가 완료되었습니다. 주문 ID: ..")
 NotificationCommandService.createNotification(판매자, SHOPPING, BID, "구매자가 결제를 완료하였습니다. 판매 ID: ..")
 sale.updateStatus(SaleStatus.SOLD)   (Sale.updateStatus는 전이 검증 없음)
```

---

## E. 결제 이후 배송/완료 (shipment 도메인, 참고)

| 트리거 | 메서드 | 효과 |
|---|---|---|
| `PATCH /shipments/order/{shipmentId}/status` (운송장 등록) | `OrderShipmentCommandService.updateTrackingInfo` | `order.updateStatus(IN_TRANSIT)`, 구매자 알림 "상품이 출발하였습니다", `updateSaleStatusToSold`, 판매자 창고보관이면 `updateWarehouseStatusToSold`, `updateOrderAndSaleBidStatusToCompleted` → **OrderBid MATCHED→COMPLETED**, SaleBid MATCHED→COMPLETED |
| `POST /shipments/order/{id}/check-status[-string]` | `updateAndCheckShipmentStatus` → DELIVERED이면 `completeOrder` | `Order→COMPLETED`, Sale SOLD, 구매자 알림 |
| 배치 `updateShipmentStatusesJob` (`global/config/ShipmentBatchScheduler`, cron `0 0 */6 * * *`) | `ShipmentItemProcessor`/`UpdateShipmentStatusesJobConfig` → `completeOrder` | 동일 |
| `OrderShipmentCommandService.updateShipmentStatuses` | DELIVERED 시 | `Order→COMPLETED` + 알림 |

주의: OrderStatus 전이표에서 `PREPARING → IN_TRANSIT`은 허용되지 않음(`SHIPMENT_STARTED` 경유 필요)이며 `SHIPMENT_STARTED`를 세팅하는 코드는 없다. 따라서 C의 일반 배송(PREPARING) 주문에 `updateTrackingInfo`를 호출하면 `InvalidOrderStatusException`이 발생해 `ShipmentException`으로 래핑된다(코드상 추론). 즉시 구매(B) 주문은 PENDING_PAYMENT에 머물러 역시 IN_TRANSIT 전이 불가.

---

## F. 실패 / 재시도 / 환불 경로

| 상황 | 처리 |
|---|---|
| A/B 동기 처리 중 예외 | `OrderException`은 그대로, 그 외는 `OrderBidCreationFailedException`(ORD102) / `OrderCreationFailedException`(ORD002) / `OrderPaymentProcessingFailedException`(ORD005) 등으로 래핑 → `GlobalExceptionHandler` 응답, 트랜잭션 롤백 |
| C 발행 실패 | 컨트롤러가 500 `ORDER_PROCESSING_FAILED` 직접 응답 |
| C 컨슈머 실패 – 재시도 가능 (IllegalArgumentException 아님 & 메시지에 "찾을 수 없습니다" 없음) | `OrderEventProducer.sendOrderRetryEvent` → `order-retry-topic` (retryCount+1). `handleOrderRetryEvent`: retryCount>=3 → 실패 알림, 아니면 sleep(min(1000·n, 5000)) 후 `handleOrderProcessingEvent` 재호출 |
| C 컨슈머 실패 – 재시도 불가 | `sendFailureNotification`: WebSocket(`/queue/notifications`, `/topic/order/{id}`) + Kafka `notification-topic` {type: ORDER_FAILURE} |
| 모든 경우 | 메시지는 ack됨 (DLQ 없음) |
| 결제 PG 승인 후 후속 단계 실패 | DB는 롤백되지만 PortOne 승인 취소 호출 코드는 없음 (`handleRefundIfNecessary`는 private 미사용) |
| 환불 | `PaymentCommandService.refundPayment(paymentId)`: PAID만 허용 → `PortOneApiClient.cancelPayment`(POST `/payments/cancel`) 성공 시 REFUNDED + 구매자/판매자 REFUND 알림, 실패 시 REFUND_REQUESTED. **호출하는 컨트롤러/서비스 없음**. Order의 REFUND_REQUESTED/REFUNDED, Sale 상태는 변경하지 않음 |
| 주문 취소 `POST /orders/{id}/cancel` | 스텁 (아무 처리 없음) |
| 입찰 삭제 `DELETE /order-bids/{id}` | sale 연결 또는 MATCHED면 거부 → A로 만든 입찰은 항상 MATCHED라 사실상 삭제 불가 |

`payment-processing-topic`/`payment-retry-topic`(`PaymentEventConsumer`)은 발행자가 없어 이 흐름에 참여하지 않는다.

---

## G. 상태 머신 요약 (누가 어떤 값을 세팅하는가)

### OrderStatus (`order/entity/OrderStatus.java`, `Order.updateStatus`가 전이 검증)
| 값 | 세팅 위치 |
|---|---|
| PENDING_PAYMENT | `OrderCommandService.createOrderFromBid`, `createInstantOrder` (빌더) |
| PAYMENT_COMPLETED | `OrderEventConsumer.processCompleteOrder`, `OrderCommandService.processPaymentAndShipment`(미사용) |
| PREPARING | 위 두 곳 |
| IN_WAREHOUSE | 위 두 곳 (창고 보관 시), `DataInitializer` |
| COMPLETED | 위 두 곳(창고 보관 시 즉시), `OrderShipmentCommandService.completeOrder`, `updateShipmentStatuses` |
| IN_TRANSIT | `OrderShipmentCommandService.updateTrackingInfo` |
| SHIPMENT_STARTED, REFUND_REQUESTED, REFUNDED | 세팅 코드 없음 |

### BidStatus — order (`order/entity/BidStatus.java`, `OrderBid.updateStatus`는 검증 없음)
| 값 | 세팅 위치 |
|---|---|
| PENDING | 빌더 기본값/`createAndSaveOrderBid` (직후 assignOrder로 덮어씀) |
| MATCHED | `OrderBid.assignOrder`/`assignSale`, `createInstantOrderBid`, `SaleCommandService.createInstantSale`, `OrderBidCommandService.matchOrderBid`(미사용), `OrderCommandService.processPaymentAndShipment`(미사용) |
| COMPLETED | `OrderShipmentCommandService.updateOrderAndSaleBidStatusToCompleted` (updateTrackingInfo 경유) |
| CANCELLED | 세팅 코드 없음 |

### BidStatus — sale (`sale/entity/BidStatus.java`, 값은 order 쪽과 동일한 별도 enum)
| 값 | 세팅 위치 (본 흐름 관련) |
|---|---|
| MATCHED | `OrderCommandService.createInstantOrder`(`saleBid.assignOrder` + `updateStatus`), `SaleBidCommandService.createInstantSaleBid` |
| COMPLETED | `OrderShipmentCommandService.updateOrderAndSaleBidStatusToCompleted` |

### PaymentStatus (`payment/entity/PaymentStatus.java`, `Payment.updateStatus`가 검증)
| 값 | 세팅 위치 |
|---|---|
| PENDING | 엔티티 기본값 (카드 응답 status≠"success"면 그대로 유지) |
| PAID | `createCardPayment`(success 시), `createGeneralPayment` |
| REFUNDED | `refundPayment`(성공), `handleRefundIfNecessary`(미사용) |
| REFUND_REQUESTED | `refundPayment`(PG 환불 실패 시) |
| FAILED | 세팅 코드 없음 |
`Payment.isSuccess`는 status와 별개로 `setPaidAmount(>0)` 시 true가 됨.

### 부수 효과 (타 도메인 상태)
| 엔티티 | 값 | 세팅 위치 |
|---|---|---|
| `Sale.status` | SOLD | `PaymentCommandService.notifyUsersAndUpdateSaleStatus`, `OrderShipmentCommandService.updateSaleStatusToSold` |
| `WarehouseStorage`(판매자) | ASSOCIATED_WITH_ORDER | `OrderCommandService.createInstantOrder` (sale.isWarehouseStorage) |
| `WarehouseStorage`(판매자) | SOLD | `OrderShipmentCommandService.updateTrackingInfo` → `updateWarehouseStatusToSold` |
| `WarehouseStorage`(구매자) | IN_STORAGE (신규) | `WarehouseStorageCommandService.createOrderStorage` (C, warehouseStorage=true) |
| `OrderShipment` | PENDING (신규) | `OrderShipmentCommandService.createOrderShipment` (B, C) |

## H. 알림 정리

| 시점 | 채널 | 대상/내용 |
|---|---|---|
| 판매자가 구매 입찰 수락(A') | `NotificationCommandService.createNotification` (DB + 실시간) | 구매자: "구매 입찰에 판매자가 등록되었습니다" |
| 결제 성공(D) | 동일 | 구매자 "결제가 완료되었습니다", 판매자 "구매자가 결제를 완료하였습니다" |
| C 성공/실패 | `SimpMessagingTemplate` `/user/{email}/queue/notifications`, `/topic/order/{orderId}` + Kafka `notification-topic`(컨슈머 없음) | ORDER_COMPLETED / ORDER_FAILED |
| 운송장 등록/배송 완료(E) | `createNotification` | 구매자 "상품이 출발하였습니다" / 완료 알림 |
| 환불(F, 미연결) | `createNotification` (REFUND) | 구매자·판매자 |
