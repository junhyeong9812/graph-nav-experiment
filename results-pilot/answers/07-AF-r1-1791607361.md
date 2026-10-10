# 즉시 구매 처리 흐름 (POST /order-bids/instant)

경로는 모두 저장소 루트 기준 `src/main/java/com/fream/back/domain/...` 이다. 파일 경로 뒤의 `:줄`이 근거 위치다.

## 1. 진입 API · 처리 단계 · 동기/비동기 · 응답 ID

**API**: `POST /order-bids/instant` — `src/main/java/com/fream/back/domain/order/controller/command/OrderBidCommandController.java:22`(base `/order-bids`), `:89-90`.

**단계 (모두 한 HTTP 요청 안에서 순차 실행)**
1. 컨트롤러: 로그인 이메일 추출 → `enrichPaymentRequest`로 `userEmail` 설정, `orderId=null` 초기화 (`OrderBidCommandController.java:92,98,119-123`) → `OrderBidCommandService#createInstantOrderBid` 호출 (`:101-107`).
2. `OrderBidCommandService#createInstantOrderBid` (`src/main/java/com/fream/back/domain/order/service/command/OrderBidCommandService.java:118-149`, `@Transactional`): 입력 검증(:123,154-167) → 구매자 조회(:126) → SaleBid 조회·검증(:129, 172-184) → `OrderCommandService#createInstantOrder`(:133) → `OrderBid` 생성·저장(:142, 189-207).
3. `OrderCommandService#createInstantOrder` (`src/main/java/com/fream/back/domain/order/service/command/OrderCommandService.java:219-319`): 배송지 조회(:238) → Order 저장(:241-247) → `paymentRequest.setOrderId`(:250) → OrderItem(:255-256) → OrderShipment(:264-271) → SaleBid 매칭+flush(:279-283) → 판매 창고 상태 변경(:289-301) → 결제 `PaymentCommandService#processPayment`(:305).
4. `PaymentCommandService#processPayment` (`src/main/java/com/fream/back/domain/payment/service/command/PaymentCommandService.java:57-107`): 결제수단별 Payment 생성 → `notifyUsersAndUpdateSaleStatus`(:83)로 알림 + Sale=SOLD.

**동기/비동기**: **동기**. Kafka(`OrderEventProducer`)는 이 경로에서 쓰이지 않는다 — Kafka 경로는 별도 API `POST /orders/{orderId}/process-payment-shipment`(`OrderCommandController`)에서만 시작된다. 즉시 구매 경로 전체가 `OrderBidCommandService:118`의 하나의 `@Transactional`(내부 서비스들이 같은 트랜잭션에 참여) 안에서 끝나며, 결제 대행사(PortOne) 호출도 이 요청 스레드에서 동기 REST로 수행된다(`src/main/java/com/fream/back/domain/payment/portone/PortOneApiClient.java:229-295`, `PaymentCommandService.java:179`).

**응답 ID**: 컨트롤러 변수명은 `orderId`, 응답 메시지는 "즉시 구매가 성공적으로 처리되었습니다"이고 Javadoc도 "생성된 주문 ID"라 하지만, 실제로는 `createInstantOrderBid(...)`가 반환한 **`OrderBid`의 `getId()`**를 돌려준다 (`OrderBidCommandController.java:101-109`, 서비스 반환형 `OrderBid` :119). 즉 **Order ID가 아니라 OrderBid ID**다. (Order ID와 값이 같다는 보장 없음.)

## 2. 새로 만들어지는 엔티티와 상태 변화 순서

| 순서 | 동작 | 근거 | 직후 상태 값 |
|---|---|---|---|
| 1 | `Order` 생성·저장 (totalAmount = saleBid.bidPrice) | `OrderCommandService.java:241-247` | Order.status = `PENDING_PAYMENT` |
| 2 | `OrderItem` 생성 (saleBid의 productSize·가격) | `OrderCommandService.java:255-256` | (상태 필드 없음) |
| 3 | `OrderShipment` 생성 | `src/main/java/com/fream/back/domain/shipment/service/command/OrderShipmentCommandService.java:56-70` | Shipment.status = `PENDING` |
| 4 | 기존 `SaleBid`에 order 연결 + 상태 변경 후 flush | `OrderCommandService.java:279-283`, `src/main/java/com/fream/back/domain/sale/entity/SaleBid.java:58-61,66` | SaleBid.status = `MATCHED` (PENDING→MATCHED) |
| 5 | (판매가 창고 보관 판매일 때만) 판매자의 기존 `WarehouseStorage` 상태 변경 | `OrderCommandService.java:291-294`, `src/main/java/com/fream/back/domain/warehouseStorage/service/command/WarehouseStorageCommandService.java:117-133` | WarehouseStorage.status = `ASSOCIATED_WITH_ORDER` |
| 6 | `Payment`(Card/Account/General) 생성 | `PaymentCommandService.java:187-208, 250-262, 302-316` | 3번 항목 참조 |
| 7 | 결제 직후 판매 상태 변경 + 알림 | `PaymentCommandService.java:383-426` (SaleBid 경유 :391-405) | Sale.status = `SOLD` (`Sale.java:73-75`는 전이 검증 없이 대입) |
| 8 | `OrderBid` 생성·저장 (구매자, 즉시구매 플래그) | `OrderBidCommandService.java:189-207` | OrderBid.status = `MATCHED`, isInstantPurchase=`true` |

- 7번에서 `order.getOrderBid()`는 아직 null(OrderBid는 8번에서 생성)이라 SaleBid 쪽 분기(`saleBidQueryService.findByOrderId`, :388-406)로 Sale을 찾는다. 4번에서 flush했으므로 조회된다.
- Order의 상태는 1번 이후 **한 번도 바뀌지 않는다** (아래 5번 참조).

## 3. 결제 수단별 결과 · 성공 여부 · 카드 실패 시

공통 검증(`PaymentCommandService.java:117-146`): 주문 ID·이메일 일치, `paidAmount > 0`. **결제 금액이 SaleBid 가격/Order.totalAmount와 같은지는 검증하지 않는다.** (`InstantOrderRequestDto.paymentRequest`에는 `@Valid`가 없어 DTO 내부 제약도 적용되지 않음, `src/main/java/com/fream/back/domain/order/dto/InstantOrderRequestDto.java:28-29`.)

| 수단 | 처리 | Payment 결과 | 근거 |
|---|---|---|---|
| CARD | 저장된 PaymentInfo로 PortOne 카드 결제 API 호출 후 CardPayment 저장 | 응답 `status == "success"`이면 `PAID` + isSuccess=true. 그렇지 않으면 **상태/성공 플래그를 바꾸지 않고 그대로 저장** → `PENDING`, isSuccess=false | `PaymentCommandService.java:179, 203-208`, 기본값 `src/main/java/com/fream/back/domain/payment/entity/Payment.java:39,44` |
| ACCOUNT | 은행명만 검증, PG 호출 없음 | 항상 `PENDING` (isSuccess=false) | `PaymentCommandService.java:243-262` |
| GENERAL | impUid 필수 확인만, PG 재검증 없음 | 즉시 `PAID` + isSuccess=true | `PaymentCommandService.java:297-314` |

**즉시 구매가 끝까지 "성공"하는가**: 결제수단과 무관하게 예외만 안 나면 요청은 성공 응답을 반환한다. 계좌이체는 입금 확인 없이 PENDING인 채로, 일반결제는 PG 재확인 없이 PAID로 곧바로 통과한다. 그리고 결제 상태와 무관하게 7번(알림 + Sale=SOLD)이 실행된다 (`notifyUsersAndUpdateSaleStatus`는 payment 상태를 보지 않음, :383-426). 따라서 계좌이체는 입금 전에 판매가 SOLD·알림 "결제가 완료되었습니다"가 나간다.

**카드 결제를 PG가 실패로 응답하면**
- (a) PortOne 응답이 HTTP≠200 또는 `code≠0` → `PortOneApiClient`가 `PaymentProcessingException` 발생 (`PortOneApiClient.java:270-275`) → `processPayment` 재던짐(:89-94) → `createInstantOrder`가 `OrderPaymentProcessingFailedException`으로 감쌈(:306-309) → `createInstantOrderBid`에서 `OrderException`이라 그대로 전파(:145-146, 212-217). 전체가 하나의 트랜잭션이므로 Order/OrderItem/OrderShipment/SaleBid 매칭/창고 상태 변경이 **모두 롤백**되고 클라이언트는 에러 응답을 받는다.
- (b) PortOne 응답이 `code==0`이지만 `response.status`가 `"success"`가 아닌 경우(예: 결제 건 자체는 생성됐으나 카드사 거절 등 `failed`) → 클라이언트가 이를 예외로 보지 않고 위 표의 "그대로 저장" 경로를 탄다. CardPayment는 `PENDING`/isSuccess=false로 저장되고 **예외가 없으므로 그대로 7·8번이 진행**된다: 구매자·판매자에게 "결제 완료" 알림이 나가고 Sale=SOLD, SaleBid=MATCHED, OrderBid=MATCHED로 확정된다. 즉 결제 실패인데 즉시 구매는 성공 응답이 된다. (코드상 `"success"`가 아닌 경우의 else/예외 처리가 없다: `PaymentCommandService.java:203-206`.) 이 분기가 실제로 발생하는지는 PortOne이 거절을 code 0으로 주는지에 달려 있어 외부 동작이지만, 코드는 이를 막지 않는다.

## 4. 알림: 수신자 · 시점 · 방식

- **수신자 (2명)**, `PaymentCommandService#sendNotifications` (:434-462):
  - 구매자(`order.getUser()`): "결제가 완료되었습니다. 주문 ID: {orderId}" (:436-443)
  - 판매자(`sale.getSeller()`): "구매자가 결제를 완료하였습니다. 판매 ID: {saleId}" (:447-454)
  - 둘 다 `NotificationCategory.SHOPPING`, `NotificationType.BID`.
- **시점**: 결제(Payment 저장) 직후, 같은 요청·같은 트랜잭션 안에서 Sale=SOLD 변경 직전 (:416-419). OrderBid 생성(8번)보다 앞선다. 알림 전송 실패는 로그만 남기고 무시된다 (:457-461, 421-425).
- **방식**: `NotificationCommandService#createNotification` (`src/main/java/com/fream/back/domain/notification/service/command/NotificationCommandService.java:44-57`)이 Notification을 **DB에 저장**하고, 수신자가 WebSocket 접속 중(Redis 키 `WebSocket:User:{email}` 존재)이면 STOMP `/queue/notifications`로 즉시 푸시한다(:303-314). 접속 중이 아니면 저장만 되고 푸시는 없다(:320-322). 이 경로에서 Kafka `notification-topic`은 사용되지 않는다(그 토픽은 `OrderEventConsumer`의 별도 경로 전용).
- 배송 완료 시의 "판매 완료" 알림(`OrderShipmentCommandService.java:111-140`)은 이후 배송 흐름에서 나가며 즉시 구매 요청 중에는 발송되지 않는다.

## 5. 주문 최종 상태 · 창고 보관 선택 시

**최종 상태**: 요청 처리가 끝났을 때 Order.status는 **`PENDING_PAYMENT`(결제 대기)** 그대로다. 즉시 구매 경로(`createInstantOrder`)에는 `order.updateStatus(...)` 호출이 전혀 없다(`OrderCommandService.java:219-319`). `PAYMENT_COMPLETED→PREPARING`(및 창고 시 `IN_WAREHOUSE→COMPLETED`)로 바꾸는 코드는 `processPaymentAndShipment`(:163-177)와 Kafka `OrderEventConsumer`(:192-217)에만 있고, 이 둘은 즉시 구매에서 호출되지 않는다. 결제가 PAID여도 주문은 PENDING_PAYMENT이며, `Order.payment`에 `assignPayment`도 하지 않는다(반환값 폐기, :305). 반면 SaleBid·OrderBid는 MATCHED, Sale은 SOLD, Shipment는 PENDING이므로 도메인 간 상태가 불일치한다. 이후 상태는 배송 도메인(`OrderShipmentCommandService` :204, :260, :355 등의 COMPLETEsrc/main/java/com/fream/back/domain/IN_TRANSIT 전환)이 별도로 진행해야 한다. (`COMPLETED` 전이 규칙상 PENDING_PAYMENT→COMPLETED는 허용됨, `OrderStatus.java` ALLOWED_TRANSITIONS.)

**"창고 보관" 선택(요청의 `warehouseStorage=true`) 시**: **달라지는 것이 없다.** `isWarehouseStorage` 인자는 `createInstantOrder` 시그니처(:220-221)로 전달되지만 본문에서 한 번도 사용되지 않는다(컨트롤러 로그 `OrderBidCommandController.java:94-95`와 전달 :105뿐). 따라서
- 구매자 몫의 `WarehouseStorage`가 생성되지 않고(`createOrderStorage` 미호출),
- Order가 `IN_WAREHOUSE`/`COMPLETED`로 가지 않으며,
- 배송 정보(OrderShipment)도 구매자가 보낸 `addressId` 주소로 그대로 생성된다(:264-271).

창고 관련으로 실제로 동작하는 것은 **판매자가 창고 보관으로 올린 판매(`sale.isWarehouseStorage()==true`)**일 때 그 판매에 연결된 기존 `WarehouseStorage`를 `ASSOCIATED_WITH_ORDER`로 바꾸는 것뿐이다(`OrderCommandService.java:289-301`; 창고 정보가 없으면 `WarehouseStorageNotFoundException`→`OrderWarehouseStorageProcessingFailedException`으로 전체 롤백, `WarehouseStorageQueryService.java:30-33`). 이는 구매자의 창고 보관 선택과 무관하다. 구매자 창고 보관을 실제로 처리하는 코드는 `processPaymentAndShipment`(:163-172)와 Kafka 경로의 `createOrderStorage`뿐이다.
