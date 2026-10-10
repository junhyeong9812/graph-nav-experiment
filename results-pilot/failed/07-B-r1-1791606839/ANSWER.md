# 즉시 구매 처리 흐름 분석

경로 표기: `B` = `src/main/java/com/fream/back/domain`

## 1. 진입 API와 처리 단계, 동기/비동기, 응답 ID

1. **진입 API**: `POST /order-bids/instant` (클래스 `@RequestMapping("/order-bids")` + `@PostMapping("/instant")`).
   - 근거: `B/order/controller/command/OrderBidCommandController.java:22`, `:89-90`
2. **컨트롤러 단계**
   - 이메일을 SecurityUtils로 추출·검증 (`OrderBidCommandController.java:92`).
   - `enrichPaymentRequest`에서 결제 DTO에 `userEmail`을 채우고 `orderId=null`로 초기화 (`:98`, `:119-122`).
   - `orderBidCommandService.createInstantOrderBid(...)` 호출 (`:101-107`).
3. **서비스 단계 (모두 하나의 `@Transactional` 안에서 동기 실행)**
   - `OrderBidCommandService.createInstantOrderBid` (`B/order/service/command/OrderBidCommandService.java:118-149`)
     1. 입력 검증 (`:123`, `:154-167`)
     2. 구매자 조회 (`:126`)
     3. SaleBid/Sale 조회·검증 (`:129-130`, `:172-184`)
     4. `orderCommandService.createInstantOrder(...)` 호출 (`:133-139`)
     5. 반환된 Order로 OrderBid 생성·저장 (`:142`, `:189-207`)
   - `OrderCommandService.createInstantOrder` (`B/order/service/command/OrderCommandService.java:219-319`)
     1. 주소 조회 (`:238`)
     2. Order 생성·저장, 상태 `PENDING_PAYMENT` (`:241-247`)
     3. 결제 DTO에 orderId 세팅 (`:250`)
     4. OrderItem 생성 (`:255-256`)
     5. OrderShipment 생성 (`:264-271`)
     6. SaleBid 매칭 + `entityManager.flush()` (`:279-283`)
     7. 판매자가 창고 보관을 선택한 Sale이면 창고 상태 갱신 (`:291-294`)
     8. 결제 처리 `paymentCommandService.processPayment` (`:305`)
4. **동기/비동기**: 즉시 구매는 **완전 동기**이다. 요청 스레드에서 DB 처리와 PortOne HTTP 호출(카드)까지 끝낸 뒤 응답한다.
   - 즉시 구매 경로에는 Kafka 발행이 없다 (`OrderBidCommandService`와 `createInstantOrder`에 Kafka 호출 없음).
   - Kafka 비동기 처리(`OrderEventProducer.sendOrderProcessingEvent` → `OrderEventConsumer`)는 별개의 API인 `POST /orders/{orderId}/process-payment-shipment`용이다.
   - 근거: `B/order/controller/command/OrderCommandController.java:42-54`, `B/order/service/kafka/OrderEventConsumer.java:55-60`
   - `OrderEventConsumer`의 `ORDER_COMPLETED` WebSocket 알림, `PAYMENT_COMPLETED` 전이, 창고 보관 완료 처리는 **즉시 구매에서는 실행되지 않는다**.
5. **응답으로 돌려주는 ID**: 변수명은 `orderId`이지만 실제 값은 **OrderBid의 ID**이다. 서비스가 `OrderBid`를 반환하고 컨트롤러가 그 `.getId()`를 쓰기 때문이다. Order ID가 아니다.
   - 근거: `OrderBidCommandController.java:101-107` (`Long orderId = ...createInstantOrderBid(...).getId()`), `OrderBidCommandService.java:119` (반환 타입 `OrderBid`)
   - 응답 본문은 `ResponseDto.success(orderId, "즉시 구매가 성공적으로 처리되었습니다.")` (`:109`)이다.

## 2. 생성 엔티티와 상태 변화 순서

각 단계 직후 상태는 아래와 같다.

| 순서 | 동작 | 직후 상태 | 근거 |
|---|---|---|---|
| 1 | **Order** 생성·저장 | `Order.status = PENDING_PAYMENT`, `totalAmount = saleBid.bidPrice` | `OrderCommandService.java:241-247` |
| 2 | **OrderItem** 생성 후 Order에 추가 | (상태 필드 없음) | `OrderCommandService.java:255-256` |
| 3 | **OrderShipment** 생성 | `ShipmentStatus.PENDING` | `B/shipment/service/command/OrderShipmentCommandService.java:61-68`, `OrderCommandService.java:264-271` |
| 4 | **기존 SaleBid** 갱신 (`assignOrder` → `updateStatus(MATCHED)`) | `SaleBid.status = MATCHED`, `order` 연결, 즉시 flush | `OrderCommandService.java:279-283`, `B/sale/entity/SaleBid.java:58-60`, `:66-67` |
| 5 | (조건부) **WarehouseStorage** 상태 갱신 | `ASSOCIATED_WITH_ORDER` | `OrderCommandService.java:291-294`, `B/warehouseStorage/service/command/WarehouseStorageCommandService.java:117-133` |
| 6 | **Payment** 생성 (카드/계좌/일반) | 결제 수단별로 다름 (3번 참고) | `PaymentCommandService.java:57-80` |
| 7 | **기존 Sale** 상태 변경 | `Sale.status = SOLD` (결제 성공 여부 무관) | `PaymentCommandService.java:83`, `:391-405` |
| 8 | **OrderBid** 생성·저장 | `OrderBid.status = MATCHED`, `isInstantPurchase = true`, `sale` 연결 | `OrderBidCommandService.java:189-206`, `B/order/entity/OrderBid.java:60-82`, `:97-99` |

세부 사항은 다음과 같다.

- **5단계의 조건**: 조건은 구매자 요청 값이 아니라 판매자가 등록한 `Sale.isWarehouseStorage`이다 (`B/sale/entity/Sale.java:43`).
- **7단계의 순서**: 이 시점에는 OrderBid가 아직 없다 (8단계에서 생성).
  - `order.getOrderBid()`가 null이어서 `saleBidQueryService.findByOrderId`로 SaleBid → Sale을 찾는다 (`PaymentCommandService.java:388-405`).
  - 이 경로가 4단계의 `assignOrder` + flush에 의존한다.
  - `Sale.updateStatus`는 전이 검증 없이 값을 대입한다 (`Sale.java:73-74`).
- **Order 상태는 한 번도 바뀌지 않는다**: `createInstantOrder`/`createInstantOrderBid`에는 `order.updateStatus(...)` 호출이 없다 (`OrderCommandService.java:219-319`, `OrderBidCommandService.java:118-207`).
  - `PAYMENT_COMPLETED`/`PREPARING` 전이는 `processPaymentAndShipment`(`OrderCommandService.java:165-176`)와 Kafka consumer(`OrderEventConsumer.java:195`)에만 있고, 즉시 구매는 이 둘을 호출하지 않는다.
- **`order.assignPayment(payment)`도 호출되지 않는다**: `createInstantOrder`는 반환된 Payment를 버린다 (`OrderCommandService.java:305`).
  - Payment 쪽에서 `assignOrder(order)`는 호출된다 (`PaymentCommandService.java:200`, `:258`, `:311`).

## 3. 결제 수단별 결과

`processPayment`는 `getResolvedPaymentType()`으로 분기한다 (`PaymentCommandService.java:67-80`). 알림·Sale 변경은 결제 수단과 결과를 가리지 않고 공통으로 실행된다 (`:83`).

| 수단 | 처리 | Payment 상태 | 근거 |
|---|---|---|---|
| **CARD** | PortOne `processCardPayment` 호출 → 응답의 `status`가 `"success"`면 PAID | 성공: `PAID`, `isSuccess=true` / 응답이 success가 아니면 기본값 `PENDING`, `isSuccess=false` 그대로 저장 | `PaymentCommandService.java:179`, `:187-208`, `B/payment/entity/Payment.java:39`, `:44` |
| **ACCOUNT** (계좌이체) | 은행명만 검증, 외부 호출 없음 | 항상 `PENDING` (입금 확인 로직 없음) | `PaymentCommandService.java:243-262` |
| **GENERAL** (일반) | `impUid`만 검증, PortOne 서버 검증 없음 | 항상 `PAID`, `isSuccess=true` | `PaymentCommandService.java:297-314` |

**즉시 구매가 끝까지 성공하는지**

- 어떤 수단이든 예외가 없으면 요청은 성공(200)으로 끝난다. 결제 성공 여부를 확인하는 코드가 없다.
  - `createInstantOrder`는 `processPayment` 반환값을 사용·검사하지 않는다 (`OrderCommandService.java:305`).
  - 비동기 consumer에는 있는 `if (!payment.isSuccess()) throw` (`OrderEventConsumer.java:174-176`)가 즉시 구매 경로에는 없다.
- 그 결과 **계좌이체는 입금 전(PENDING)인데도** 주문·SaleBid MATCHED·Sale SOLD·구매자와 판매자 알림까지 진행된다.
- 컨트롤러는 항상 "즉시 구매가 성공적으로 처리되었습니다."를 반환한다 (`OrderBidCommandController.java:109`).
- 이 흐름에서 Order는 `PENDING_PAYMENT`로 남는다 (2번 참고).

**결제 대행사가 카드 결제를 실패로 응답하면**

카드 실패 응답은 두 갈래다.

1. **PortOne 응답이 `code != 0` 또는 HTTP 비 200**: `PortOneApiClient.processCardPayment`가 `PaymentProcessingException`을 던진다 (`B/payment/portone/PortOneApiClient.java:270-274`). 처리는 다음 순서로 진행된다.
   - `PaymentCommandService`가 이를 재전파한다 (`:89-94`).
   - `createInstantOrder`가 `OrderPaymentProcessingFailedException`으로 감싼다 (`OrderCommandService.java:306-309`).
   - `OrderBidCommandService`가 `OrderException`이면 그대로 던진다 (`:213-214`).
   - 전체 `@Transactional`이 롤백된다. Order, OrderShipment, SaleBid MATCHED, 알림이 모두 되돌려진다.
   - 구매자에게는 오류 응답이 간다.
2. **`code == 0`이지만 `response.status != "success"`** (예: PG사 승인 거절이 `status: failed`로 오는 경우): 예외가 나지 않는다.
   - `status`가 `"success"`가 아니면 `PAID`로 올리지 않는다 (`PaymentCommandService.java:203-206`).
   - CardPayment는 `PENDING`, `isSuccess=false`로 저장된다 (`:208`).
   - 이후 흐름은 정상 성공과 동일하다. SaleBid MATCHED, Sale SOLD, "결제가 완료되었습니다" 알림, OrderBid MATCHED가 모두 일어나고 HTTP 200이 반환된다.
   - 즉, 코드가 의도한 "결제 실패 → 주문 실패"가 이 경로에서는 지켜지지 않는다.

`@Retryable`은 `PaymentApiException`(통신 오류)에만 적용된다. 설정은 3회, 1초 간격이다 (`PortOneApiClient.java:224-228`).

## 4. 알림 대상, 시점, 전달 방식

- **누가 받나**
  - 구매자: "결제가 완료되었습니다. 주문 ID: {orderId}" (`PaymentCommandService.java:436-443`)
  - 판매자 (`sale.getSeller()`): "구매자가 결제를 완료하였습니다. 판매 ID: {saleId}" (`PaymentCommandService.java:447-454`)
  - 두 알림 모두 `NotificationCategory.SHOPPING`, `NotificationType.BID`이다.
- **언제**
  - 결제 Payment 저장 직후 `notifyUsersAndUpdateSaleStatus`에서 발송된다 (`PaymentCommandService.java:83`, `:383-419`).
  - 즉 OrderBid 생성보다 앞서고, **같은 트랜잭션 안**에서 즉시(동기) 발송된다.
  - 카드 결제 실패(PENDING)와 계좌이체 PENDING에서도 같은 문구로 발송된다. 결제 결과 검사가 없기 때문이다.
- **어떻게**
  - `NotificationCommandService.createNotification`이 Notification 엔티티를 DB에 저장한다 (`B/notification/service/command/NotificationCommandService.java:44-54`).
  - 이어서 수신자가 Redis 키 `WebSocket:User:{email}`로 연결 중으로 표시된 경우에만 WebSocket `convertAndSendToUser(email, "/queue/notifications", dto)`로 실시간 푸시한다 (`:57`, `:303-313`).
  - 연결이 없으면 DB 저장만 하며, 이후 조회로 확인하는 방식이다 (`:320-321`).
  - 전송 예외는 로깅만 한다 (`:315-319`). `sendNotifications` 전체도 예외를 삼킨다 (`PaymentCommandService.java:457-461`).
  - Kafka 알림 토픽은 사용하지 않는다.
- **즉시 구매에서 발송되지 않는 알림**
  - `OrderEventConsumer`의 "주문 완료" WebSocket(`/user/.../queue/notifications`, `/topic/order/{id}`)과 `notification-topic` Kafka 이벤트는 비동기 경로(`process-payment-shipment`) 전용이다 (`OrderEventConsumer.java:287-318`).
- **롤백 주의**: 알림 저장이 트랜잭션 안에서 일어나므로, 이후 단계(OrderBid 저장 등)가 실패해 롤백되면 DB 알림 레코드도 롤백된다. 이미 WebSocket으로 나간 푸시는 취소되지 않는다.

## 5. 최종 주문 상태와 "창고 보관" 선택 시 차이

**최종 주문 상태**

- `Order.status = PENDING_PAYMENT` (결제 대기)이다. 카드/일반 결제 성공 시에도 마찬가지이다.
  - 근거: 생성 시 `PENDING_PAYMENT` (`OrderCommandService.java:244`), 이후 `updateStatus` 호출 없음 (2번 참고).
  - `PAYMENT_COMPLETED`로 바뀌는 곳은 `processPaymentAndShipment`와 Kafka consumer뿐이다 (`OrderCommandService.java:165`, `OrderEventConsumer.java:195`).
- 같이 남는 상태는 다음과 같다.
  - OrderShipment: `PENDING`
  - SaleBid / OrderBid: `MATCHED`
  - Sale: `SOLD`
  - Payment: 수단별 `PAID` 또는 `PENDING`

**구매자가 `warehouseStorage=true`를 선택한 경우**

- **달라지는 것은 없다 (값이 무시된다).**
  - 컨트롤러가 `requestDto.isWarehouseStorage()`를 서비스로 넘긴다 (`OrderBidCommandController.java:104-105`).
  - `OrderBidCommandService`도 `createInstantOrder`로 전달한다 (`OrderBidCommandService.java:133-139`).
  - 하지만 `createInstantOrder` 본문에서 `isWarehouseStorage` 파라미터는 한 번도 읽히지 않는다 (`OrderCommandService.java:220-319`).
  - 창고 보관 분기 로직(`createOrderStorage`, `IN_WAREHOUSE`, `COMPLETED` 전이)은 즉시 구매 경로에 없다. 그 로직은 `OrderCommandService.java:163-172`와 `OrderEventConsumer.java:197-212`에만 있다.
- 따라서 `true`/`false` 모두 결과가 같다.
  - 구매자용 WarehouseStorage는 생성되지 않는다.
  - Order는 `IN_WAREHOUSE`/`COMPLETED`로 가지 않는다.
- 창고 관련 동작은 구매자 선택이 아니라 **판매자 쪽** `Sale.isWarehouseStorage`가 true일 때만 일어난다.
  - 해당 판매의 기존 WarehouseStorage 상태를 `ASSOCIATED_WITH_ORDER`로 바꾼다 (`OrderCommandService.java:291-294`).
