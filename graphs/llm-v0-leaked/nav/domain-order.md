# 도메인: order (구매 주문 / 구매 입찰)

> 기준 경로: `src/main/java/com/fream/back/domain/order/` (이하 `order/`)
> 관련 문서: [domain-payment.md](domain-payment.md), [flow-trade-buy.md](flow-trade-buy.md)

## 1. 역할 요약

- 구매자 측 거래 엔티티 관리: `Order`(주문), `OrderItem`(주문 항목), `OrderBid`(구매 입찰).
- 두 가지 진입 경로
  - **구매 입찰** `POST /order-bids` → `Order(PENDING_PAYMENT)` + `OrderBid` 생성 (결제는 나중에 `/orders/{id}/process-payment-shipment`로 Kafka 비동기 처리).
  - **즉시 구매** `POST /order-bids/instant` → 기존 `SaleBid`에 대해 Order/OrderItem/OrderShipment 생성 + SaleBid 매칭 + **동기 결제**까지 한 트랜잭션에서 처리.
- 결제·배송 처리는 Kafka(`order-processing-topic`) 컨슈머 `OrderEventConsumer`가 담당하며, 결과를 WebSocket(STOMP)과 Kafka `notification-topic`으로 알림.
- CQRS 스타일로 `controller/command|query`, `service/command|query` 분리.

## 2. 패키지/파일 맵

| 레이어 | 파일 (repo 루트 기준) | 용도 |
|---|---|---|
| Controller(command) | `src/main/java/com/fream/back/domain/order/controller/command/OrderBidCommandController.java` | 구매 입찰 생성/삭제, 즉시 구매 |
| | `src/main/java/com/fream/back/domain/order/controller/command/OrderCommandController.java` | 결제·배송 처리 요청(Kafka 발행), 취소(스텁) |
| Controller(query) | `src/main/java/com/fream/back/domain/order/controller/query/OrderBidQueryController.java` | 구매 입찰 목록/개수/상세 |
| | `src/main/java/com/fream/back/domain/order/controller/query/OrderQueryController.java` | 주문 상태/상세/목록(목록은 스텁). `OrderRepository` 직접 사용 |
| Service(command) | `src/main/java/com/fream/back/domain/order/service/command/OrderBidCommandService.java` | OrderBid 생성/즉시구매/매칭/삭제 |
| | `src/main/java/com/fream/back/domain/order/service/command/OrderCommandService.java` | Order 생성(입찰/즉시), 결제·배송 처리(동기 버전), 삭제 |
| | `src/main/java/com/fream/back/domain/order/service/command/OrderItemCommandService.java` | OrderItem 생성 (quantity=1 고정) |
| Service(query) | `src/main/java/com/fream/back/domain/order/service/query/OrderBidQueryService.java` | OrderBid 조회(`findById`, `findByOrderId`, 목록/개수/상세) — 타 도메인에서 많이 사용 |
| | `src/main/java/com/fream/back/domain/order/service/query/OrderItemQueryService.java` | OrderItem 단건 조회 |
| Kafka | `src/main/java/com/fream/back/domain/order/service/kafka/OrderEventProducer.java` | `order-processing-topic`, `order-retry-topic` 발행 |
| | `src/main/java/com/fream/back/domain/order/service/kafka/OrderEventConsumer.java` | 위 토픽 소비 → 결제·배송·창고·상태 전이 + 알림 |
| | `src/main/java/com/fream/back/domain/order/dto/kafka/OrderProcessingEvent.java` | Kafka 이벤트 DTO (maxRetry=3) |
| Kafka 설정(global) | `src/main/java/com/fream/back/global/config/kafka/OrderKafkaConfig.java` | Producer/Consumer factory, `orderProcessingKafkaListenerContainerFactory`, `notificationKafkaTemplate` |
| Entity | `src/main/java/com/fream/back/domain/order/entity/Order.java` | 주문 (table `orders`) |
| | `src/main/java/com/fream/back/domain/order/entity/OrderItem.java` | 주문 항목 |
| | `src/main/java/com/fream/back/domain/order/entity/OrderBid.java` | 구매 입찰 |
| | `src/main/java/com/fream/back/domain/order/entity/OrderStatus.java` | 주문 상태 + 전이 테이블 |
| | `src/main/java/com/fream/back/domain/order/entity/BidStatus.java` | 구매 입찰 상태 |
| Repository | `src/main/java/com/fream/back/domain/order/repository/OrderRepository.java` | `findByUser_EmailAndStatus`, `findByUserAndStatus` |
| | `src/main/java/com/fream/back/domain/order/repository/OrderItemRepository.java` | 파생 쿼리 4개 |
| | `src/main/java/com/fream/back/domain/order/repository/OrderBidRepository.java` | `findByOrderId` + Custom |
| | `src/main/java/com/fream/back/domain/order/repository/OrderBidRepositoryCustom.java` / `OrderBidRepositoryImpl.java` | QueryDSL: 필터 목록, 상태별 개수, 상세, 색상별 거래 수 |
| DTO | `src/main/java/com/fream/back/domain/order/dto/OrderBidRequestDto.java` | `productSizeId`(@NotNull), `bidPrice`(@Min 1), `userEmail`(미사용) |
| | `src/main/java/com/fream/back/domain/order/dto/InstantOrderRequestDto.java` | `saleBidId`, `addressId`, `warehouseStorage`, `paymentRequest`(다형 `PaymentRequestDto`) |
| | `src/main/java/com/fream/back/domain/order/dto/PayAndShipmentRequestDto.java` | `paymentRequest` + 수령인/연락처/우편번호/주소 + `warehouseStorage` |
| | `src/main/java/com/fream/back/domain/order/dto/OrderBidResponseDto.java`, `OrderBidStatusCountDto.java` | 조회 응답 |
| Exception | `src/main/java/com/fream/back/domain/order/exception/*` | `OrderErrorCode` + `OrderException` + 24개 하위 예외 (§7) |

## 3. REST 엔드포인트

`server.servlet.context-path` 설정은 application*.yml에서 발견되지 않음. 인증 이메일은 `global/utils/SecurityUtils.extractAndValidateEmailForOrder[Bid]()`로 추출(실패 시 `OrderAccessDeniedException`/`OrderBidAccessDeniedException`).

| HTTP | 경로 | Controller 메서드 | Service 호출 | 비고 |
|---|---|---|---|---|
| POST | `/order-bids` | `OrderBidCommandController.createOrderBid` | `OrderBidCommandService.createOrderBid(email, productSizeId, bidPrice)` | 응답 data = orderBidId |
| DELETE | `/order-bids/{orderBidId}` | `OrderBidCommandController.deleteOrderBid` | `OrderBidCommandService.deleteOrderBid(orderBidId)` | **소유자 검증 없음** (email은 로그만) |
| POST | `/order-bids/instant` | `OrderBidCommandController.createInstantOrderBid` | `OrderBidCommandService.createInstantOrderBid(...)` | `paymentRequest.userEmail=email`, `orderId=null`로 덮어씀. 응답 data는 변수명이 orderId지만 실제로는 **OrderBid id** (`.getId()` on OrderBid) |
| GET | `/order-bids?bidStatus=&orderStatus=&page..` | `OrderBidQueryController.getOrderBids` | `OrderBidQueryService.getOrderBids` | 문자열로 enum 비교 |
| GET | `/order-bids/count` | `OrderBidQueryController.getOrderBidStatusCounts` | `OrderBidQueryService.getOrderBidStatusCounts` | |
| GET | `/order-bids/{orderBidId}` | `OrderBidQueryController.getOrderBidDetail` | `OrderBidQueryService.getOrderBidDetail` | 없으면 `OrderBidNotFoundException` |
| POST | `/orders/{orderId}/process-payment-shipment` | `OrderCommandController.processPaymentAndShipment` | `OrderEventProducer.sendOrderProcessingEvent` (Kafka) | **202 ACCEPTED**, `websocketTopic=/topic/order/{id}` 반환 |
| POST | `/orders/{orderId}/cancel` | `OrderCommandController.cancelOrderProcessing` | 없음 | **스텁**: 아무 처리 없이 CANCELLED 응답 |
| GET | `/orders/{orderId}/status` | `OrderQueryController.getOrderStatus` | `OrderRepository.findById` 직접 | 소유자 아니면 403. payment/shipment/warehouse 요약 포함 |
| GET | `/orders/{orderId}` | `OrderQueryController.getOrderDetails` | `OrderRepository.findById` 직접 | |
| GET | `/orders?page&size&status` | `OrderQueryController.getUserOrders` | 없음 | **스텁**: 항상 빈 목록 |

`OrderCommandController` 안에 기존 동기 버전(`orderCommandService.processPaymentAndShipment`) 호출 코드가 주석 처리되어 남아 있음.

## 4. 핵심 엔티티와 관계

```
User 1─* Order 1─* OrderItem *─1 ProductSize
          │ 1─1 Payment (mappedBy="order", cascade ALL, orphanRemoval)   ← payment 도메인
          │ 1─1 OrderShipment (mappedBy)                                ← shipment 도메인
          │ 1─1 WarehouseStorage (mappedBy)                             ← warehouseStorage 도메인
          │ 1─1 OrderBid (mappedBy="order", cascade ALL)
OrderBid *─1 User, *─1 ProductSize, 1─1 Order(FK order_id), 1─1 Sale(FK sale_id)
SaleBid(sale 도메인) ─ order (FK) 로 Order 참조
```

| 엔티티 | 주요 필드 | 메서드 |
|---|---|---|
| `Order` (`orders`) | `totalAmount`, `discountAmount`, `usedPoints`(int), `status: OrderStatus` | `addOrderItem`, `assignPayment`, `assignOrderShipment`, `assignWarehouseStorage`, `assignOrderBid`, `updateStatus`(전이 검증; 현재 null이면 무조건 허용, 아니면 `InvalidOrderStatusException`) |
| `OrderItem` | `quantity`, `price` | `addOrder` |
| `OrderBid` | `bidPrice`, `status`(기본 PENDING), `isInstantPurchase` | `assignOrder`(order≠null이면 **status=MATCHED**), `assignSale`(**status=MATCHED**), `markAsInstantPurchase`, `updateStatus`(검증 없음) |

### OrderStatus 전이 테이블 (`OrderStatus.ALLOWED_TRANSITIONS`)

| From | 허용 To |
|---|---|
| PENDING_PAYMENT | PAYMENT_COMPLETED, COMPLETED, IN_WAREHOUSE |
| PAYMENT_COMPLETED | PREPARING, REFUND_REQUESTED, IN_WAREHOUSE |
| PREPARING | IN_WAREHOUSE, SHIPMENT_STARTED |
| IN_WAREHOUSE | SHIPMENT_STARTED, COMPLETED |
| SHIPMENT_STARTED | IN_TRANSIT |
| IN_TRANSIT | COMPLETED |
| REFUND_REQUESTED | REFUNDED |
| COMPLETED, REFUNDED | (없음) |

### BidStatus (order) — `PENDING, MATCHED, CANCELLED, COMPLETED`

## 5. 주요 서비스 메서드와 비즈니스 규칙

| 메서드 | 트랜잭션 | 규칙 / 동작 |
|---|---|---|
| `OrderBidCommandService.createOrderBid` | `@Transactional` | email/productSizeId/bidPrice>0 검증 → `UserQueryService.findByEmail` → `ProductSizeQueryService.findById`(없으면 `ProductSizeNotFoundException`) → `OrderCommandService.createOrderFromBid` → OrderBid 빌드(PENDING) 후 `assignOrder(order)` → **결과적으로 MATCHED로 저장**. 비-OrderException은 `OrderBidCreationFailedException`으로 래핑 |
| `OrderBidCommandService.createInstantOrderBid` | `@Transactional` | 입력 검증 → `SaleBidQueryService.findById`(없으면 IllegalArgumentException → ORD102로 래핑) → sale null이면 `InvalidOrderBidDataException` → `OrderCommandService.createInstantOrder` → OrderBid(MATCHED, `sale` 세팅, `isInstantPurchase=true`) 저장 |
| `OrderBidCommandService.matchOrderBid` | `@Transactional` | 이미 MATCHED면 `OrderBidAlreadyMatchedException`. **main 코드 내 호출처 없음** |
| `OrderBidCommandService.deleteOrderBid` | `@Transactional` | sale 연결 시/MATCHED 시 `OrderBidAlreadyMatchedException` → 연결 Order 삭제 → OrderBid 삭제 |
| `OrderCommandService.createOrderFromBid` | `@Transactional` | OrderItem(order=null) 먼저 저장 → Order(totalAmount=bidPrice, PENDING_PAYMENT) → `addOrderItem` → save |
| `OrderCommandService.createInstantOrder` | `@Transactional` | `AddressQueryService.getAddress(email, addressId)` → Order(totalAmount=saleBid.bidPrice, PENDING_PAYMENT) 저장 → `paymentRequest.setOrderId(order.id)` → OrderItem → `OrderShipmentCommandService.createOrderShipment`(주소록 정보 사용) → `saleBid.assignOrder(order)` + `saleBid.updateStatus(sale.BidStatus.MATCHED)` + `entityManager.flush()` → sale이 창고보관이면 `WarehouseStorageCommandService.updateWarehouseStatus(sale, ASSOCIATED_WITH_ORDER)` → `PaymentCommandService.processPayment` (실패 시 `OrderPaymentProcessingFailedException`). **Order 상태는 변경하지 않음**, `isWarehouseStorage` 파라미터는 사용되지 않음 |
| `OrderCommandService.processPaymentAndShipment` | `@Transactional` | 동기 버전. **현재 컨트롤러에서 호출되지 않음**(테스트에서만). 결제 → 배송 생성 → 상태 전이(창고: PAYMENT_COMPLETED→PREPARING→IN_WAREHOUSE→창고생성→COMPLETED / 배송: PAYMENT_COMPLETED→PREPARING) → `orderBidQueryService.findById(orderId)`로 OrderBid MATCHED (아래 Gotcha) |
| `OrderCommandService.deleteOrder` | `@Transactional` | 없으면 `OrderNotFoundException` |
| `OrderEventConsumer.handleOrderProcessingEvent` | `@Transactional` + manual ack | 멱등성: Order.status≠PENDING_PAYMENT면 skip → 소유자 검증 → `processCompleteOrder` → 성공 알림 → ack. 실패 시 `handleOrderProcessingFailure` |
| `OrderEventConsumer.processCompleteOrder` | (위 트랜잭션) | `processPayment` → `payment.isSuccess()` false면 RuntimeException → OrderShipment 생성 → PAYMENT_COMPLETED → 창고면 `createOrderStorage` 후 PREPARING→IN_WAREHOUSE→COMPLETED (각각 canTransitionTo 가드) / 배송이면 PREPARING → save. **OrderBid 상태는 건드리지 않음** |

## 6. 다른 도메인과의 의존

### order → 타 도메인 (호출)
| 대상 | 사용 위치 | 용도 |
|---|---|---|
| payment `PaymentCommandService.processPayment` | `OrderCommandService`, `OrderEventConsumer` | 결제 |
| shipment `OrderShipmentCommandService.createOrderShipment` | `OrderCommandService`, `OrderEventConsumer` | 구매자 배송정보(ShipmentStatus.PENDING) |
| warehouseStorage `WarehouseStorageCommandService.createOrderStorage / updateWarehouseStatus` | 동일 | 구매자 창고보관 생성 / 판매자 창고 상태 ASSOCIATED_WITH_ORDER |
| sale `SaleBidQueryService.findById`, `SaleBid.assignOrder/updateStatus` | `OrderBidCommandService`, `OrderCommandService` | 즉시 구매 매칭 |
| user `UserQueryService.findByEmail`, address `AddressQueryService.getAddress`, product `ProductSizeQueryService.findById` | 서비스 전반 | 조회 |
| WebSocket `SimpMessagingTemplate` | `OrderEventConsumer` | `/user/{email}/queue/notifications`, `/topic/order/{orderId}` |

### 타 도메인 → order (역참조)
| 사용처 | 내용 |
|---|---|
| `domain/sale/service/command/SaleCommandService.createInstantSale` | `OrderBidQueryService.findById` → `orderBid.assignSale(sale)` + `updateStatus(MATCHED)` + 구매자 알림 |
| `domain/sale/service/command/SaleBidCommandService.createInstantSaleBid` | OrderBid의 Order로 SaleBid(MATCHED) 생성 |
| `domain/shipment/service/command/OrderShipmentCommandService` | `updateTrackingInfo`: Order→IN_TRANSIT; `completeOrder`/배치: Order→COMPLETED; `updateOrderAndSaleBidStatusToCompleted`: OrderBid MATCHED→COMPLETED |
| `domain/shipment/config/UpdateShipmentStatusesJobConfig`, `ShipmentItemProcessor` | 배치에서 `completeOrder` 호출 (`global/config/ShipmentBatchScheduler` cron `0 0 */6 * * *`) |
| `domain/product/service/product/ProductQueryService`, `product/elasticsearch/service/ProductColorSearchService` | `OrderBidRepository.tradeCountByColorIds` (COMPLETED 입찰 수) |
| `domain/product/repository/ProductQueryDslRepository` | `QOrderBid`, `QOrderItem`, order `BidStatus.COMPLETED` |
| `domain/style/*` | `StyleOrderItem` → `OrderItem` 연결 |
| `domain/payment/repository/PaymentRepository` | `Payment.order.orderBid.sale.seller` JPQL 조인 |
| `global/config/DataInitializer` | 샘플 Order/OrderBid 생성 |

### Kafka
| 토픽 | 그룹 | 발행 | 소비 | DTO |
|---|---|---|---|---|
| `order-processing-topic` | `order-processing-group` | `OrderEventProducer.sendOrderProcessingEvent` (key=orderId) | `OrderEventConsumer.handleOrderProcessingEvent` | `OrderProcessingEvent` |
| `order-retry-topic` | `order-retry-group` | `OrderEventProducer.sendOrderRetryEvent` (retryCount+1) | `OrderEventConsumer.handleOrderRetryEvent` (sleep min(1000*n,5000)ms) | `OrderProcessingEvent` |
| `notification-topic` | — | `OrderEventConsumer.send{Success,Failure}Notification` (`Map<String,Object>`) | **main 코드에 컨슈머 없음** | Map |

- Container factory: concurrency=2, `AckMode.MANUAL_IMMEDIATE`, auto-commit off, max-poll 5. 토픽/그룹명은 코드에 하드코딩 (prod yml에 `spring.kafka.order.*`로도 기재돼 있으나 코드가 참조하지 않음).
- 재시도 가능 판정: `IllegalArgumentException`이 아니고 메시지에 "찾을 수 없습니다"가 없을 때.
- 스케줄러/Redis: order 패키지 내 없음.

## 7. 예외/에러코드

- 위치: `order/exception/OrderErrorCode.java` (ORDxxx), 베이스 `OrderException extends GlobalException`, 처리: `global/exception/GlobalExceptionHandler.handleOrderException`.
- 코드 대역: `ORD0xx` 주문, `ORD1xx` 주문입찰, `ORD2xx` 주문항목, `ORD3xx` 접근권한(403), `ORD4xx` 입력값(400), `ORD5xx` 외부 리소스(상품사이즈/판매입찰/창고).
- 하위 예외는 대부분 1:1로 코드에 매핑 (예: `OrderNotFoundException`→ORD001, `OrderBidAlreadyMatchedException`→ORD107, `OrderWarehouseStorageProcessingFailedException`→ORD503). 예외: `InvalidOrderBidException`과 `InvalidOrderBidDataException` 둘 다 `INVALID_ORDER_BID_DATA`.
- 코드만 있고 전용 예외 클래스가 없는 것: `ORDER_UPDATE_FAILED`, `ORDER_BID_UPDATE_FAILED`, `ORDER_BID_CANNOT_BE_DELETED`.
- 공통 패턴: 서비스는 `catch(Exception)` 후 `OrderException`이면 재던짐, 아니면 `*FailedException`으로 래핑.
- `OrderQueryController`는 예외를 던지지 않고 `IllegalArgumentException`을 잡아 `ResponseDto.error("ORDER_NOT_FOUND", ...)`로 직접 응답.

## 8. 관련 테스트

| 파일 | 종류 | 내용 |
|---|---|---|
| `src/test/java/com/fream/back/domain/order/OrderBidCommandServiceTest.java` | Mockito | 생성 시 MATCHED 저장, 즉시구매, 삭제 가드, matchOrderBid |
| `src/test/java/com/fream/back/domain/order/OrderCommandServiceTest.java` | Mockito | createOrderFromBid, processPaymentAndShipment(배송/창고/권한/상태오류), createInstantOrder |
| `src/test/java/com/fream/back/domain/order/OrderEntityStateTest.java` | 순수 단위 | OrderStatus 전이 테이블, OrderBid 양방향/상태 |
| `src/test/java/com/fream/back/domain/order/OrderBidRepositoryTest.java` | `@DataJpaTest` | QueryDSL 필터/페이징/개수/거래수 |

Kafka 컨슈머(`OrderEventConsumer`)와 컨트롤러에 대한 테스트는 없음.

## 9. 자주 할 작업 → 여기를 보라

| 작업 | 위치 |
|---|---|
| 주문 상태 전이 규칙 변경 | `order/entity/OrderStatus.java` `ALLOWED_TRANSITIONS` + `OrderEntityStateTest` |
| 결제 후 주문 상태 흐름 수정 | `order/service/kafka/OrderEventConsumer.processCompleteOrder` (실제 경로). `OrderCommandService.processPaymentAndShipment`는 미사용 동기 버전 |
| 즉시 구매 로직 | `OrderBidCommandService.createInstantOrderBid` → `OrderCommandService.createInstantOrder` |
| 구매 입찰 목록 조회 컬럼/필터 | `OrderBidRepositoryImpl.findOrderBidsByFilters` + `OrderBidResponseDto` (생성자 projection 순서 주의) |
| 주문 알림 메시지 | `OrderEventConsumer.sendSuccessNotification/sendFailureNotification`, 결제 알림은 `PaymentCommandService.sendNotifications` |
| Kafka 설정 | `global/config/kafka/OrderKafkaConfig.java` |
| 새 예외 추가 | `OrderErrorCode`에 코드 추가 → `OrderException` 하위 클래스 생성 |

## 10. Gotchas (코드에서 확인됨)

1. **BidStatus enum이 두 개**: `order/entity/BidStatus.java`와 `sale/entity/BidStatus.java`. 값(`PENDING, MATCHED, CANCELLED, COMPLETED`)과 주석까지 동일하지만 별개 타입이라 혼용 불가. `OrderCommandService`, `OrderShipmentCommandService`는 FQCN(`com.fream.back.domain.sale.entity.BidStatus.MATCHED`)으로 구분해서 씀. import 주의.
2. **일반 구매 입찰이 생성 즉시 MATCHED**: `createOrderBid`가 status=PENDING으로 빌드한 뒤 `orderBid.assignOrder(order)`를 호출하는데, 이 메서드가 status를 MATCHED로 바꿈 (테스트 `createOrderBidLinksOrderAndIsSavedAsMatched`도 이를 기대). 결과적으로
   - `deleteOrderBid`는 MATCHED를 거부 → 일반 입찰은 삭제 불가.
   - `countOrderBidsByStatus`의 pendingCount는 사실상 0.
   - 판매자의 즉시 판매(`SaleCommandService.createInstantSale`)는 상태 검사 없이 assignSale만 함.
3. `OrderCommandService.processPaymentAndShipment` 5단계가 `orderBidQueryService.findById(orderId)` — **OrderBid id 자리에 orderId**를 넘김(`findByOrderId`가 있음). 예외는 삼켜짐.
4. 즉시 구매(`createInstantOrder`)는 결제 후에도 Order 상태를 **PENDING_PAYMENT로 둠**, 구매자 창고보관 플래그(`isWarehouseStorage`) 미사용. 이후 `/orders/{id}/process-payment-shipment`를 호출하면 컨슈머 멱등성 검사(PENDING_PAYMENT 여부)를 통과해 결제가 다시 수행될 수 있음.
5. 즉시 구매 결제 시점엔 OrderBid가 아직 생성 전이라 `PaymentCommandService.notifyUsersAndUpdateSaleStatus`는 SaleBid 경로(`saleBidQueryService.findByOrderId`)로 Sale을 찾음.
6. `/orders/{orderId}/cancel`, `GET /orders`는 실제 동작이 없는 스텁.
7. `DELETE /order-bids/{id}`는 소유자 확인을 하지 않음.
8. `createInstantOrderBid` 응답 메시지에서 "orderId"라는 이름으로 OrderBid id를 반환.
9. `SaleBidQueryService.findById`는 없으면 null이 아니라 `IllegalArgumentException`을 던짐 → `getSaleBidAndValidate`의 null 체크는 실행되지 않고 최종 응답은 `ORD102`(OrderBidCreationFailed).
10. `OrderEventConsumer`는 `@Transactional` 메서드 안에서 예외를 잡고 ack함. 내부에서 호출한 `@Transactional` 프록시 빈(예: `PaymentCommandService`)이 RuntimeException을 던지면 Spring 기본 동작상 트랜잭션이 rollback-only로 표시되어 커밋 시점에 `UnexpectedRollbackException`이 날 수 있음 (런타임 동작은 미확인).
11. `OrderStatus`의 `SHIPMENT_STARTED`, `REFUND_REQUESTED`, `REFUNDED`를 세팅하는 코드는 main에 없음. `OrderShipmentCommandService.updateTrackingInfo`가 `IN_TRANSIT`으로 바꾸는데, 전이 테이블상 `PREPARING → IN_TRANSIT`은 허용되지 않음(`SHIPMENT_STARTED` 경유 필요) → 일반 배송 주문에선 `InvalidOrderStatusException` 발생 가능.
12. `CANCELLED` (order BidStatus)를 세팅하는 코드 없음.
