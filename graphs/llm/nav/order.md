# order 도메인 네비게이션 맵

> 기준 경로: `src/main/java/com/fream/back/domain/order` (54 files). 코드에서 확인된 사실만 기재. 교차 흐름은 `nav/flow-trade.md` 참고.

## 1. 개요
- 구매 측 거래 도메인: **구매 입찰(OrderBid)**, **주문(Order)**, **주문 항목(OrderItem)**.
- 일반 구매 입찰 생성 시 `Order(PENDING_PAYMENT)`가 즉시 함께 생성되고, 결제/배송지 입력은 `POST /orders/{orderId}/process-payment-shipment` → **Kafka(`order-processing-topic`) 비동기** 처리.
- 즉시 구매(`POST /order-bids/instant`)는 기존 SaleBid에 매칭 + 결제를 **동기**로 한 트랜잭션에서 처리.
- 결제는 payment, 배송은 shipment, 창고보관은 warehouseStorage 도메인 서비스 호출.

## 2. 패키지/파일 구조
```
src/main/java/com/fream/back/domain/order/
├── controller/command/ OrderBidCommandController.java (/order-bids), OrderCommandController.java (/orders)
├── controller/query/   OrderBidQueryController.java (/order-bids), OrderQueryController.java (/orders, OrderRepository 직접 사용)
├── dto/ InstantOrderRequestDto, OrderBidRequestDto, OrderBidResponseDto, OrderBidStatusCountDto, PayAndShipmentRequestDto
├── dto/kafka/OrderProcessingEvent.java
├── entity/ Order, OrderBid, OrderItem, OrderStatus, BidStatus
├── exception/ OrderErrorCode, OrderException + 25개 하위 예외 (SaleBidNotFoundException, ProductSizeNotFoundException 포함)
├── repository/ OrderRepository, OrderBidRepository(+Custom, Impl=QueryDSL), OrderItemRepository
└── service/
    ├── command/ OrderBidCommandService, OrderCommandService, OrderItemCommandService
    ├── kafka/   OrderEventProducer, OrderEventConsumer
    └── query/   OrderBidQueryService, OrderItemQueryService
```
관련 global: `src/main/java/com/fream/back/global/config/kafka/OrderKafkaConfig.java`, `src/main/java/com/fream/back/global/utils/SecurityUtils.java` (`extractAndValidateEmailForOrderBid`, `extractAndValidateEmailForOrder`).

## 3. 엔티티
| 엔티티 (테이블) | 주요 필드 | 관계 |
|---|---|---|
| `Order` (`orders`) extends BaseTimeEntity | totalAmount, discountAmount, usedPoints(int), status(OrderStatus) | user(ManyToOne `User`), payment(1:1 mappedBy, `Payment`), orderShipment(1:1 mappedBy, `OrderShipment`), warehouseStorage(1:1 mappedBy, `WarehouseStorage`), orderItems(1:N), orderBid(1:1 mappedBy) — 모두 cascade ALL + orphanRemoval |
| `OrderBid` | bidPrice, status(order.entity.BidStatus, 기본 PENDING), isInstantPurchase | user, productSize(`ProductSize`), order(1:1 FK order_id), sale(1:1 FK sale_id, `sale.entity.Sale`) |
| `OrderItem` | quantity(항상 1), price | order(ManyToOne), productSize(ManyToOne). style 도메인 `StyleOrderItem`이 참조 |

**OrderStatus** (`entity/OrderStatus.java`, `canTransitionTo` 맵 기반):
| from | 허용 to |
|---|---|
| PENDING_PAYMENT | PAYMENT_COMPLETED, COMPLETED, IN_WAREHOUSE |
| PAYMENT_COMPLETED | PREPARING, REFUND_REQUESTED, IN_WAREHOUSE |
| PREPARING | IN_WAREHOUSE, SHIPMENT_STARTED |
| IN_WAREHOUSE | SHIPMENT_STARTED, COMPLETED |
| SHIPMENT_STARTED | IN_TRANSIT |
| IN_TRANSIT | COMPLETED |
| REFUND_REQUESTED | REFUNDED |
| COMPLETED / REFUNDED | (없음) |

**BidStatus** (`order/entity/BidStatus.java`): PENDING, MATCHED, CANCELLED, COMPLETED. (sale 도메인에 동일 이름·동일 값의 별도 enum 존재)

상태 변경 메서드:
- `Order#updateStatus(OrderStatus)` (Order.java:113): status==null 이거나 `canTransitionTo` 통과 시만 변경, 아니면 `InvalidOrderStatusException`.
- `OrderBid#assignOrder(Order)` (OrderBid.java:60): order != null이면 **status=MATCHED** 로 설정 + 양방향 연결.
- `OrderBid#assignSale(Sale)` (:89): status=MATCHED. `OrderBid#updateStatus` (:106): 검증 없이 대입. `markAsInstantPurchase()`.
- 연관 편의: `Order#assignPayment/assignOrderShipment/assignWarehouseStorage/assignOrderBid/addOrderItem`.

## 4. API 엔드포인트
SecurityConfig: 화이트리스트 외 `anyRequest().authenticated()` → 아래 전부 인증 필요. 이메일은 SecurityContext에서 추출.

| Method | Path | Controller#method | Service | 비고 |
|---|---|---|---|---|
| POST | /order-bids | OrderBidCommandController#createOrderBid | OrderBidCommandService#createOrderBid | body `OrderBidRequestDto`(productSizeId, bidPrice). 응답 OrderBid id |
| DELETE | /order-bids/{orderBidId} | OrderBidCommandController#deleteOrderBid | OrderBidCommandService#deleteOrderBid | 소유자 검증 없음 (email은 로그용) |
| POST | /order-bids/instant | OrderBidCommandController#createInstantOrderBid | OrderBidCommandService#createInstantOrderBid | body `InstantOrderRequestDto`(saleBidId, addressId, warehouseStorage, paymentRequest). `enrichPaymentRequest`가 userEmail 설정. 응답은 **OrderBid id**(변수명 orderId) |
| GET | /order-bids?bidStatus&orderStatus&page | OrderBidQueryController#getOrderBids | OrderBidQueryService#getOrderBids → `findOrderBidsByFilters` | |
| GET | /order-bids/count | OrderBidQueryController#getOrderBidStatusCounts | OrderBidQueryService#getOrderBidStatusCounts | |
| GET | /order-bids/{orderBidId} | OrderBidQueryController#getOrderBidDetail | OrderBidQueryService#getOrderBidDetail | 본인 것만(email 조건) |
| POST | /orders/{orderId}/process-payment-shipment | OrderCommandController#processPaymentAndShipment | OrderEventProducer#sendOrderProcessingEvent (Kafka) | **202 Accepted** 즉시 반환, websocketTopic `/topic/order/{orderId}` 안내 |
| POST | /orders/{orderId}/cancel | OrderCommandController#cancelOrderProcessing | (없음) | TODO 스텁: 항상 CANCELLED 응답, 실제 처리 없음 |
| GET | /orders/{orderId}/status | OrderQueryController#getOrderStatus | OrderRepository#findById 직접 | 소유자 아니면 403 |
| GET | /orders/{orderId} | OrderQueryController#getOrderDetails | OrderRepository#findById 직접 | |
| GET | /orders?page&size&status | OrderQueryController#getUserOrders | (없음) | TODO 스텁: 항상 빈 목록 |

## 5. 서비스 계층
- **OrderBidCommandService**
  - `createOrderBid(email, productSizeId, bidPrice)` (:47): UserQueryService#findByEmail → ProductSizeQueryService#findById → `OrderCommandService#createOrderFromBid` → OrderBid 빌드 후 `assignOrder(order)` → 저장.
  - `createInstantOrderBid(buyerEmail, saleBidId, addressId, isWarehouseStorage, paymentRequest)` (:119): `SaleBidQueryService#findById` → `OrderCommandService#createInstantOrder` → OrderBid(MATCHED, sale 지정, isInstantPurchase=true) 저장.
  - `matchOrderBid(OrderBid)` (:228): 호출처 없음.
  - `deleteOrderBid(orderBidId)` (:257): sale 연결 또는 MATCHED면 `OrderBidAlreadyMatchedException`; order 있으면 `OrderCommandService#deleteOrder` 후 삭제.
- **OrderCommandService**
  - `createOrderFromBid(user, productSize, bidPrice)` (:59): `OrderItemCommandService#createOrderItem` + Order(PENDING_PAYMENT, totalAmount=bidPrice) 저장.
  - `createInstantOrder(...)` (:220): `AddressQueryService#getAddress` → Order(PENDING_PAYMENT) 저장 → `paymentRequest.setOrderId` → OrderItem → `OrderShipmentCommandService#createOrderShipment`(주소록 정보) → `saleBid.assignOrder(order)`+MATCHED, `entityManager.flush()` → sale.isWarehouseStorage면 `WarehouseStorageCommandService#updateWarehouseStatus(sale, ASSOCIATED_WITH_ORDER)` → `PaymentCommandService#processPayment`. **Order 상태는 PENDING_PAYMENT 그대로 유지, `order.assignPayment` 미호출.**
  - `processPaymentAndShipment(...)` (:112): 동기 버전. 컨트롤러 호출은 주석 처리되어 현재 **호출처 없음**.
  - `deleteOrder(orderId)` (:328).
- **OrderItemCommandService#createOrderItem(order, productSize, price)**: quantity=1.
- **OrderEventProducer** (`service/kafka`): `sendOrderProcessingEvent` → `order-processing-topic` (key=orderId), `sendOrderRetryEvent` → `order-retry-topic` (retryCount≥3이면 중단), `sendOrderProcessingEventSync`(호출처 없음).
- **OrderEventConsumer**: 아래 7절.
- **OrderBidQueryService**: `findById`, `findByOrderId`, `getOrderBids`, `getOrderBidStatusCounts`, `getOrderBidDetail`. **OrderItemQueryService#findById** (style 도메인에서 사용).

## 6. Repositories
- `OrderRepository`: `findByUser_EmailAndStatus`, `findByUserAndStatus`.
- `OrderBidRepository`: `findByOrderId`. Custom(`OrderBidRepositoryImpl`, QueryDSL):
  - `findOrderBidsByFilters(email, bidStatus, orderStatus, pageable)`: orderBid ⨝ user/productSize/productColor/product/thumbnailImage, leftJoin order/orderShipment → `OrderBidResponseDto` projection.
  - `countOrderBidsByStatus(email)`: PENDING / MATCHED / (CANCELLED+COMPLETED) 개수.
  - `findOrderBidById(orderBidId, email)`.
  - `tradeCountByColorIds(colorIds)`: status=COMPLETED OrderBid를 productColor별 count → product 도메인 `ProductQueryService`, `ProductColorSearchService`가 사용.
- `OrderItemRepository`: `findByOrder_Id`, `findByProductSize`, `findByQuantityGreaterThan`, `findByOrderIn`.
- product 도메인 `ProductQueryDslRepository`도 `QOrderBid`, `QOrderItem`을 직접 사용.

## 7. 외부 연동 / 비동기 (Kafka)
| 토픽 | Producer | Consumer (groupId, containerFactory) |
|---|---|---|
| `order-processing-topic` | OrderEventProducer#sendOrderProcessingEvent | OrderEventConsumer#handleOrderProcessingEvent (`order-processing-group`, `orderProcessingKafkaListenerContainerFactory`) |
| `order-retry-topic` | OrderEventProducer#sendOrderRetryEvent | OrderEventConsumer#handleOrderRetryEvent (`order-retry-group`) |
| `notification-topic` | OrderEventConsumer#sendSuccessNotification / sendFailureNotification (`notificationKafkaTemplate`, Map payload) | **코드 내 @KafkaListener 없음** |

- `OrderKafkaConfig`: JSON (de)serializer, `ENABLE_AUTO_COMMIT=false`, AckMode `MANUAL_IMMEDIATE`, concurrency 2, MAX_POLL_RECORDS 5, `notificationKafkaTemplate` 빈 정의.
- `OrderEventConsumer#handleOrderProcessingEvent` (:61, @Transactional): 멱등성(`isOrderAlreadyProcessed`: status != PENDING_PAYMENT면 skip) → 소유자 확인 → `processCompleteOrder` → 성공 알림 → ack. 실패 시 `handleOrderProcessingFailure`: IllegalArgumentException 이거나 메시지에 "찾을 수 없습니다" 포함이면 재시도 안 함 + 실패 알림, 그 외 retry 토픽 발행.
- `processCompleteOrder` (:166): `PaymentCommandService#processPayment` → `order.assignPayment` → `payment.isSuccess()` false면 RuntimeException → `OrderShipmentCommandService#createOrderShipment` → `PAYMENT_COMPLETED` → 창고보관이면 `WarehouseStorageCommandService#createOrderStorage` + PREPARING→IN_WAREHOUSE→COMPLETED, 아니면 PREPARING.
- `handleOrderRetryEvent` (:121): `Thread.sleep(min(1000*retryCount, 5000))` 후 `handleOrderProcessingEvent` 재호출.
- WebSocket 알림: `SimpMessagingTemplate.convertAndSendToUser(email, "/queue/notifications")` + `/topic/order/{orderId}`.
- `OrderProcessingEvent`: orderId, userEmail, requestDto(PayAndShipmentRequestDto), eventCreatedAt, retryCount, eventId, processingStep. `isMaxRetryExceeded()` = retryCount ≥ 3.
- 설정값 `application-prod.yml`의 `spring.kafka.order.*`(토픽/그룹명)이 있으나 코드에서는 문자열 하드코딩 사용.

## 8. 다른 도메인 의존
- user: `UserQueryService#findByEmail`, `User`. address: `AddressQueryService#getAddress`. product: `ProductSizeQueryService#findById`, `ProductSize`.
- sale: `SaleBidQueryService#findById`, `SaleBid#assignOrder/updateStatus`, `Sale` (OrderBid.sale).
- payment: `PaymentCommandService#processPayment`, `PaymentRequestDto`.
- shipment: `OrderShipmentCommandService#createOrderShipment`.
- warehouseStorage: `WarehouseStorageCommandService#createOrderStorage`, `#updateWarehouseStatus`.
- 역방향 사용처: payment(`PaymentCommandService`, `PaymentEventConsumer`→OrderRepository), shipment(`OrderShipmentCommandService`→OrderBidQueryService), sale(`SaleCommandService`, `SaleBidCommandService`→OrderBidQueryService), product(tradeCount), style(OrderItem), `global/config/DataInitializer.java`(시드 데이터).

## 9. 예외
- `OrderException extends GlobalException`, `OrderErrorCode implements ErrorCode`, 코드 prefix **ORD**:
  ORD001~008(주문: NOT_FOUND, CREATION/UPDATE/DELETION_FAILED, PAYMENT/SHIPMENT_PROCESSING_FAILED, STATUS_UPDATE_FAILED, INVALID_ORDER_STATUS), ORD101~108(입찰), ORD201~202(항목), ORD301~302(접근권한), ORD401~404(입력값), ORD501 PRODUCT_SIZE_NOT_FOUND, ORD502 SALE_BID_NOT_FOUND, ORD503 WAREHOUSE_STORAGE_PROCESSING_FAILED.
- `GlobalExceptionHandler#handleOrderException`에서 처리. `OrderQueryController`/`OrderCommandController`는 자체 try-catch로 `ResponseDto.error(...)` 반환.

## 10. 작업 팁 / 주의점
- **BidStatus enum 중복**: `domain/order/entity/BidStatus` 와 `domain/sale/entity/BidStatus` 가 별개 타입. 혼용 시 FQN 필요 (예: `OrderCommandService.java:280`, `OrderShipmentCommandService`의 `updateOrderAndSaleBidStatusToCompleted`).
- 일반 입찰 생성 시 `createAndSaveOrderBid`(OrderBidCommandService.java:90~103)가 `.status(PENDING)`으로 빌드하지만 곧바로 `orderBid.assignOrder(order)`가 status를 **MATCHED**로 바꿈 → 저장 시점부터 MATCHED. 결과적으로 `deleteOrderBid`의 MATCHED 검사(:269)에 걸려 일반 입찰 삭제가 막힘.
- `OrderCommandService#processPaymentAndShipment`(:188)는 `orderBidQueryService.findById(orderId)` — OrderBid id 자리에 orderId를 넘김(현재 호출처 없음).
- 결제 요청 검증(`PaymentCommandService#validatePaymentRequest`)은 `paymentRequest.orderId == order.id`, `paymentRequest.userEmail == user.email`을 요구. `/orders/{id}/process-payment-shipment`는 이를 보강하지 않으므로 클라이언트가 body에 넣어야 함. `/order-bids/instant`는 컨트롤러가 email 설정, 서비스가 orderId 설정.
- `OrderEventConsumer`의 멱등성 판정은 Order status 기반. `order.updateStatus` 직접 호출 전 `canTransitionTo` 확인.
- 즉시 구매 경로에서는 Order가 PENDING_PAYMENT로 남음 (상태 갱신 코드 없음).
- `OrderStatus.SHIPMENT_STARTED`, `REFUND_REQUESTED`, `REFUNDED`를 설정하는 코드 없음.
- 사용자 포인트(usedPoints)/할인은 0으로만 세팅, 포인트 차감 로직 없음.
