# legacyorder 도메인

기준 경로: `src/main/java/com/fream/back/domain/legacyorder/` · 파일 13개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/legacy/order-bids` | `LegacyOrderBidCommandController#createOrderBid` | `LegacyOrderBidCommandService#createOrderBid` |
| DELETE | `/legacy/order-bids/{orderBidId}` | `LegacyOrderBidCommandController#deleteOrderBid` | `LegacyOrderBidCommandService#deleteOrderBid` |
| POST | `/legacy/order-bids/instant` | `LegacyOrderBidCommandController#createInstantOrderBid` | `this#enrichPaymentRequest`, `LegacyOrderBidCommandService#createInstantOrderBid` |
| POST | `/legacy/orders/{orderId}/process-payment-shipment` | `LegacyOrderCommandController#processPaymentAndShipment` | `LegacyOrderEventProducer#sendOrderProcessingEvent` |
| POST | `/legacy/orders/{orderId}/cancel` | `LegacyOrderCommandController#cancelOrderProcessing` |  |
| GET | `/legacy/order-bids` | `LegacyOrderBidQueryController#getOrderBids` | `LegacyOrderBidQueryService#getOrderBids` |
| GET | `/legacy/order-bids/count` | `LegacyOrderBidQueryController#getOrderBidStatusCounts` | `LegacyOrderBidQueryService#getOrderBidStatusCounts` |
| GET | `/legacy/order-bids/{orderBidId}` | `LegacyOrderBidQueryController#getOrderBidDetail` | `LegacyOrderBidQueryService#getOrderBidDetail` |
| GET | `/legacy/orders/{orderId}/status` | `LegacyOrderQueryController#getOrderStatus` | `OrderRepository#findById~`, `this#getStatusDescription`, `this#canCancelOrder` |
| GET | `/legacy/orders/{orderId}` | `LegacyOrderQueryController#getOrderDetails` | `OrderRepository#findById~`, `this#getStatusDescription` |
| GET | `/legacy/orders` | `LegacyOrderQueryController#getUserOrders` |  |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `LegacyOrderBidCommandService`
  - `createOrderBid` → this#validateCreateOrderBidInput, LegacyUserQueryService#findByEmail, LegacyProductSizeQueryService#findById, LegacyOrderCommandService#createOrderFromBid, this#createAndSaveOrderBid, this#handleOrderBidCreationException
  - `createAndSaveOrderBid` → OrderBidRepository#save~
  - `createInstantOrderBid` → this#validateInstantOrderBidInput, LegacyUserQueryService#findByEmail, this#getSaleBidAndValidate, LegacyOrderCommandService#createInstantOrder, this#handleOrderBidCreationException, OrderBidRepository#save~
  - `getSaleBidAndValidate` → LegacySaleBidQueryService#findById
  - `deleteOrderBid` → OrderBidRepository#findById~, LegacyOrderCommandService#deleteOrder, OrderBidRepository#delete~
- `LegacyOrderBidQueryService`
  - `findById` → OrderBidRepository#findById~
  - `findByOrderId` → OrderBidRepository#findByOrderId
  - `getOrderBids` → this#validateEmail, OrderBidRepository#findOrderBidsByFilters
  - `getOrderBidStatusCounts` → this#validateEmail, OrderBidRepository#countOrderBidsByStatus
  - `getOrderBidDetail` → this#validateEmail, OrderBidRepository#findOrderBidById
- `LegacyOrderCommandService`
  - `createOrderFromBid` → LegacyOrderItemCommandService#createOrderItem, OrderRepository#save~
  - `processPaymentAndShipment` → LegacyUserQueryService#findByEmail, OrderRepository#findById~, this#validatePaymentShipmentRequest, PaymentCommandService#processPayment, OrderShipmentCommandService#createOrderShipment, WarehouseStorageCommandService#createOrderStorage, LegacyOrderBidQueryService#findById
  - `createInstantOrder` → LegacyAddressQueryService#getAddress, OrderRepository#save~, LegacyOrderItemCommandService#createOrderItem, OrderShipmentCommandService#createOrderShipment, WarehouseStorageCommandService#updateWarehouseStatus, PaymentCommandService#processPayment
  - `deleteOrder` → OrderRepository#findById~, OrderRepository#delete~
- `LegacyOrderEventConsumer`
  - `handleOrderProcessingEvent` → this#isOrderAlreadyProcessed, OrderRepository#findById~, LegacyUserQueryService#findByEmail, this#processCompleteOrder, this#sendSuccessNotification, this#handleOrderProcessingFailure
  - `handleOrderRetryEvent` → this#sendFailureNotification, this#isOrderAlreadyProcessed, this#handleOrderProcessingEvent
  - `processCompleteOrder` → PaymentCommandService#processPayment, OrderShipmentCommandService#createOrderShipment, WarehouseStorageCommandService#createOrderStorage, OrderRepository#save~
  - `isOrderAlreadyProcessed` → OrderRepository#findById~
  - `handleOrderProcessingFailure` → this#isRetryableError, LegacyOrderEventProducer#sendOrderRetryEvent, this#sendFailureNotification
  - `sendSuccessNotification` → kafka:notification-topic
  - `sendFailureNotification` → this#isRetryableError, kafka:notification-topic
- `LegacyOrderEventProducer`
  - `sendOrderProcessingEvent` → kafka:order-processing-topic
  - `sendOrderRetryEvent` → kafka:order-retry-topic
  - `sendOrderProcessingEventSync` → kafka:order-processing-topic
- `LegacyOrderItemCommandService`
  - `createOrderItem` → OrderItemRepository#save~
- `LegacyOrderItemQueryService`
  - `findById` → OrderItemRepository#findById~

## 3. 클래스 목록 (역할별)

### controller (4)

- `LegacyOrderBidCommandController` controller/command/LegacyOrderBidCommandController.java · public: createOrderBid, deleteOrderBid, createInstantOrderBid · 주입: LegacyOrderBidCommandService
- `LegacyOrderCommandController` controller/command/LegacyOrderCommandController.java · public: processPaymentAndShipment, cancelOrderProcessing · 주입: LegacyOrderEventProducer
- `LegacyOrderBidQueryController` controller/query/LegacyOrderBidQueryController.java · public: getOrderBids, getOrderBidStatusCounts, getOrderBidDetail · 주입: LegacyOrderBidQueryService
- `LegacyOrderQueryController` controller/query/LegacyOrderQueryController.java · public: getOrderStatus, getOrderDetails, getUserOrders · 주입: OrderRepository

### service-command (3)

- `LegacyOrderBidCommandService` service/command/LegacyOrderBidCommandService.java · public: createOrderBid, createInstantOrderBid, matchOrderBid, deleteOrderBid · 주입: OrderBidRepository, LegacyOrderCommandService, LegacyProductSizeQueryService, LegacyUserQueryService, LegacySaleBidQueryService
- `LegacyOrderCommandService` service/command/LegacyOrderCommandService.java · public: createOrderFromBid, processPaymentAndShipment, createInstantOrder, deleteOrder · 주입: OrderRepository, PaymentCommandService, OrderShipmentCommandService, LegacyOrderItemCommandService, LegacyOrderBidQueryService, LegacyUserQueryService, WarehouseStorageCommandService, LegacyAddressQueryService, EntityManager
- `LegacyOrderItemCommandService` service/command/LegacyOrderItemCommandService.java · public: createOrderItem · 주입: OrderItemRepository

### service-query (2)

- `LegacyOrderBidQueryService` service/query/LegacyOrderBidQueryService.java · public: findById, findByOrderId, getOrderBids, getOrderBidStatusCounts, getOrderBidDetail · 주입: OrderBidRepository
- `LegacyOrderItemQueryService` service/query/LegacyOrderItemQueryService.java · public: findById · 주입: OrderItemRepository

### custom (2)

- `LegacyOrderBidRepositoryCustom` (interface) repository/LegacyOrderBidRepositoryCustom.java · public: findOrderBidsByFilters, countOrderBidsByStatus, findOrderBidById, tradeCountByColorIds
- `LegacyOrderBidRepositoryImpl` repository/LegacyOrderBidRepositoryImpl.java · extends/implements LegacyOrderBidRepositoryCustom · public: findOrderBidsByFilters, countOrderBidsByStatus, findOrderBidById, tradeCountByColorIds · 주입: JPAQueryFactory

### event (2)

- `LegacyOrderEventConsumer` service/kafka/LegacyOrderEventConsumer.java · public: handleOrderProcessingEvent, handleOrderRetryEvent · 주입: OrderRepository, PaymentCommandService, OrderShipmentCommandService, WarehouseStorageCommandService, LegacyUserQueryService, LegacyOrderEventProducer, SimpMessagingTemplate, KafkaTemplate
- `LegacyOrderEventProducer` service/kafka/LegacyOrderEventProducer.java · public: sendOrderProcessingEvent, sendOrderRetryEvent, sendOrderProcessingEventSync · 주입: KafkaTemplate

### 다른 도메인 참조 (import·주입 기준)

- **legacyaddress**: `LegacyAddressQueryService` ← LegacyOrderCommandService
- **legacyproduct**: `LegacyProductSizeQueryService` ← LegacyOrderBidCommandService
- **legacysale**: `LegacySaleBidQueryService` ← LegacyOrderBidCommandService
- **legacyuser**: `LegacyUserQueryService` ← LegacyOrderBidCommandService, LegacyOrderCommandService, LegacyOrderEventConsumer
- **order**: `BidStatus` ← LegacyOrderBidCommandService, LegacyOrderBidRepositoryImpl, LegacyOrderCommandService; `Order` ← LegacyOrderBidCommandService, LegacyOrderCommandService, LegacyOrderEventConsumer, LegacyOrderItemCommandService, LegacyOrderQueryController; `OrderBid` ← LegacyOrderBidCommandService, LegacyOrderBidQueryService; `OrderBidRepository` ← LegacyOrderBidCommandService, LegacyOrderBidQueryService; `OrderItem` ← LegacyOrderCommandService, LegacyOrderItemCommandService, LegacyOrderItemQueryService; `OrderItemRepository` ← LegacyOrderItemCommandService, LegacyOrderItemQueryService; `OrderProcessingEvent` ← LegacyOrderEventConsumer, LegacyOrderEventProducer; `OrderRepository` ← LegacyOrderCommandService, LegacyOrderEventConsumer, LegacyOrderQueryController; `OrderStatus` ← LegacyOrderCommandService, LegacyOrderEventConsumer, LegacyOrderQueryController
- **payment**: `Payment` ← LegacyOrderCommandService, LegacyOrderEventConsumer; `PaymentCommandService` ← LegacyOrderCommandService, LegacyOrderEventConsumer
- **product**: `ProductSize` ← LegacyOrderBidCommandService, LegacyOrderCommandService, LegacyOrderItemCommandService
- **sale**: `Sale` ← LegacyOrderBidCommandService, LegacyOrderCommandService; `SaleBid` ← LegacyOrderBidCommandService, LegacyOrderCommandService
- **shipment**: `OrderShipment` ← LegacyOrderCommandService, LegacyOrderEventConsumer; `OrderShipmentCommandService` ← LegacyOrderCommandService, LegacyOrderEventConsumer
- **user**: `User` ← LegacyOrderBidCommandService, LegacyOrderCommandService, LegacyOrderEventConsumer
- **warehouseStorage**: `WarehouseStatus` ← LegacyOrderCommandService; `WarehouseStorage` ← LegacyOrderCommandService, LegacyOrderEventConsumer; `WarehouseStorageCommandService` ← LegacyOrderCommandService, LegacyOrderEventConsumer

### 이 도메인을 참조하는 다른 도메인

- **legacysale**: LegacyOrderBidQueryService
- **legacystyle**: LegacyOrderItemQueryService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

- 발행 kafka `notification-topic`: `LegacyOrderEventConsumer#sendSuccessNotification` → (소스 내 구독 없음)
- 발행 kafka `notification-topic`: `LegacyOrderEventConsumer#sendFailureNotification` → (소스 내 구독 없음)
- 발행 kafka `order-processing-topic`: `LegacyOrderEventProducer#sendOrderProcessingEvent` → `AdminOrderEventConsumer#handleOrderProcessingEvent` [adminorder], `InternalOrderEventConsumer#handleOrderProcessingEvent` [internalorder], `LegacyOrderEventConsumer#handleOrderProcessingEvent` [legacyorder], `OrderEventConsumer#handleOrderProcessingEvent` [order], `PartnerOrderEventConsumer#handleOrderProcessingEvent` [partnerorder], `SettlementOrderEventConsumer#handleOrderProcessingEvent` [settlementorder]
- 발행 kafka `order-retry-topic`: `LegacyOrderEventProducer#sendOrderRetryEvent` → `AdminOrderEventConsumer#handleOrderRetryEvent` [adminorder], `InternalOrderEventConsumer#handleOrderRetryEvent` [internalorder], `LegacyOrderEventConsumer#handleOrderRetryEvent` [legacyorder], `OrderEventConsumer#handleOrderRetryEvent` [order], `PartnerOrderEventConsumer#handleOrderRetryEvent` [partnerorder], `SettlementOrderEventConsumer#handleOrderRetryEvent` [settlementorder]
- 발행 kafka `order-processing-topic`: `LegacyOrderEventProducer#sendOrderProcessingEventSync` → `AdminOrderEventConsumer#handleOrderProcessingEvent` [adminorder], `InternalOrderEventConsumer#handleOrderProcessingEvent` [internalorder], `LegacyOrderEventConsumer#handleOrderProcessingEvent` [legacyorder], `OrderEventConsumer#handleOrderProcessingEvent` [order], `PartnerOrderEventConsumer#handleOrderProcessingEvent` [partnerorder], `SettlementOrderEventConsumer#handleOrderProcessingEvent` [settlementorder]
- 구독 kafka `order-processing-topic`: `LegacyOrderEventConsumer#handleOrderProcessingEvent` (groupId=order-processing-group containerFactory=orderProcessingKafkaListenerContainerFactory) ← `AdminOrderEventProducer#sendOrderProcessingEvent` [adminorder], `AdminOrderEventProducer#sendOrderProcessingEventSync` [adminorder], `InternalOrderEventProducer#sendOrderProcessingEvent` [internalorder], `InternalOrderEventProducer#sendOrderProcessingEventSync` [internalorder], `LegacyOrderEventProducer#sendOrderProcessingEvent` [legacyorder], `LegacyOrderEventProducer#sendOrderProcessingEventSync` [legacyorder], `OrderEventProducer#sendOrderProcessingEvent` [order], `OrderEventProducer#sendOrderProcessingEventSync` [order], `PartnerOrderEventProducer#sendOrderProcessingEvent` [partnerorder], `PartnerOrderEventProducer#sendOrderProcessingEventSync` [partnerorder], `SettlementOrderEventProducer#sendOrderProcessingEvent` [settlementorder], `SettlementOrderEventProducer#sendOrderProcessingEventSync` [settlementorder]
- 구독 kafka `order-retry-topic`: `LegacyOrderEventConsumer#handleOrderRetryEvent` (groupId=order-retry-group containerFactory=orderProcessingKafkaListenerContainerFactory) ← `AdminOrderEventProducer#sendOrderRetryEvent` [adminorder], `InternalOrderEventProducer#sendOrderRetryEvent` [internalorder], `LegacyOrderEventProducer#sendOrderRetryEvent` [legacyorder], `OrderEventProducer#sendOrderRetryEvent` [order], `PartnerOrderEventProducer#sendOrderRetryEvent` [partnerorder], `SettlementOrderEventProducer#sendOrderRetryEvent` [settlementorder]

## 6. AOP

(없음)

