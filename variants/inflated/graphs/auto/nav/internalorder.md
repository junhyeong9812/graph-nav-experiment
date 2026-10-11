# internalorder 도메인

기준 경로: `src/main/java/com/fream/back/domain/internalorder/` · 파일 13개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/internal/order-bids` | `InternalOrderBidCommandController#createOrderBid` | `InternalOrderBidCommandService#createOrderBid` |
| DELETE | `/internal/order-bids/{orderBidId}` | `InternalOrderBidCommandController#deleteOrderBid` | `InternalOrderBidCommandService#deleteOrderBid` |
| POST | `/internal/order-bids/instant` | `InternalOrderBidCommandController#createInstantOrderBid` | `this#enrichPaymentRequest`, `InternalOrderBidCommandService#createInstantOrderBid` |
| POST | `/internal/orders/{orderId}/process-payment-shipment` | `InternalOrderCommandController#processPaymentAndShipment` | `InternalOrderEventProducer#sendOrderProcessingEvent` |
| POST | `/internal/orders/{orderId}/cancel` | `InternalOrderCommandController#cancelOrderProcessing` |  |
| GET | `/internal/order-bids` | `InternalOrderBidQueryController#getOrderBids` | `InternalOrderBidQueryService#getOrderBids` |
| GET | `/internal/order-bids/count` | `InternalOrderBidQueryController#getOrderBidStatusCounts` | `InternalOrderBidQueryService#getOrderBidStatusCounts` |
| GET | `/internal/order-bids/{orderBidId}` | `InternalOrderBidQueryController#getOrderBidDetail` | `InternalOrderBidQueryService#getOrderBidDetail` |
| GET | `/internal/orders/{orderId}/status` | `InternalOrderQueryController#getOrderStatus` | `OrderRepository#findById~`, `this#getStatusDescription`, `this#canCancelOrder` |
| GET | `/internal/orders/{orderId}` | `InternalOrderQueryController#getOrderDetails` | `OrderRepository#findById~`, `this#getStatusDescription` |
| GET | `/internal/orders` | `InternalOrderQueryController#getUserOrders` |  |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `InternalOrderBidCommandService`
  - `createOrderBid` → this#validateCreateOrderBidInput, InternalUserQueryService#findByEmail, InternalProductSizeQueryService#findById, InternalOrderCommandService#createOrderFromBid, this#createAndSaveOrderBid, this#handleOrderBidCreationException
  - `createAndSaveOrderBid` → OrderBidRepository#save~
  - `createInstantOrderBid` → this#validateInstantOrderBidInput, InternalUserQueryService#findByEmail, this#getSaleBidAndValidate, InternalOrderCommandService#createInstantOrder, this#handleOrderBidCreationException, OrderBidRepository#save~
  - `getSaleBidAndValidate` → InternalSaleBidQueryService#findById
  - `deleteOrderBid` → OrderBidRepository#findById~, InternalOrderCommandService#deleteOrder, OrderBidRepository#delete~
- `InternalOrderBidQueryService`
  - `findById` → OrderBidRepository#findById~
  - `findByOrderId` → OrderBidRepository#findByOrderId
  - `getOrderBids` → this#validateEmail, OrderBidRepository#findOrderBidsByFilters
  - `getOrderBidStatusCounts` → this#validateEmail, OrderBidRepository#countOrderBidsByStatus
  - `getOrderBidDetail` → this#validateEmail, OrderBidRepository#findOrderBidById
- `InternalOrderCommandService`
  - `createOrderFromBid` → InternalOrderItemCommandService#createOrderItem, OrderRepository#save~
  - `processPaymentAndShipment` → InternalUserQueryService#findByEmail, OrderRepository#findById~, this#validatePaymentShipmentRequest, PaymentCommandService#processPayment, OrderShipmentCommandService#createOrderShipment, WarehouseStorageCommandService#createOrderStorage, InternalOrderBidQueryService#findById
  - `createInstantOrder` → InternalAddressQueryService#getAddress, OrderRepository#save~, InternalOrderItemCommandService#createOrderItem, OrderShipmentCommandService#createOrderShipment, WarehouseStorageCommandService#updateWarehouseStatus, PaymentCommandService#processPayment
  - `deleteOrder` → OrderRepository#findById~, OrderRepository#delete~
- `InternalOrderEventConsumer`
  - `handleOrderProcessingEvent` → this#isOrderAlreadyProcessed, OrderRepository#findById~, InternalUserQueryService#findByEmail, this#processCompleteOrder, this#sendSuccessNotification, this#handleOrderProcessingFailure
  - `handleOrderRetryEvent` → this#sendFailureNotification, this#isOrderAlreadyProcessed, this#handleOrderProcessingEvent
  - `processCompleteOrder` → PaymentCommandService#processPayment, OrderShipmentCommandService#createOrderShipment, WarehouseStorageCommandService#createOrderStorage, OrderRepository#save~
  - `isOrderAlreadyProcessed` → OrderRepository#findById~
  - `handleOrderProcessingFailure` → this#isRetryableError, InternalOrderEventProducer#sendOrderRetryEvent, this#sendFailureNotification
  - `sendSuccessNotification` → kafka:notification-topic
  - `sendFailureNotification` → this#isRetryableError, kafka:notification-topic
- `InternalOrderEventProducer`
  - `sendOrderProcessingEvent` → kafka:order-processing-topic
  - `sendOrderRetryEvent` → kafka:order-retry-topic
  - `sendOrderProcessingEventSync` → kafka:order-processing-topic
- `InternalOrderItemCommandService`
  - `createOrderItem` → OrderItemRepository#save~
- `InternalOrderItemQueryService`
  - `findById` → OrderItemRepository#findById~

## 3. 클래스 목록 (역할별)

### controller (4)

- `InternalOrderBidCommandController` controller/command/InternalOrderBidCommandController.java · public: createOrderBid, deleteOrderBid, createInstantOrderBid · 주입: InternalOrderBidCommandService
- `InternalOrderCommandController` controller/command/InternalOrderCommandController.java · public: processPaymentAndShipment, cancelOrderProcessing · 주입: InternalOrderEventProducer
- `InternalOrderBidQueryController` controller/query/InternalOrderBidQueryController.java · public: getOrderBids, getOrderBidStatusCounts, getOrderBidDetail · 주입: InternalOrderBidQueryService
- `InternalOrderQueryController` controller/query/InternalOrderQueryController.java · public: getOrderStatus, getOrderDetails, getUserOrders · 주입: OrderRepository

### service-command (3)

- `InternalOrderBidCommandService` service/command/InternalOrderBidCommandService.java · public: createOrderBid, createInstantOrderBid, matchOrderBid, deleteOrderBid · 주입: OrderBidRepository, InternalOrderCommandService, InternalProductSizeQueryService, InternalUserQueryService, InternalSaleBidQueryService
- `InternalOrderCommandService` service/command/InternalOrderCommandService.java · public: createOrderFromBid, processPaymentAndShipment, createInstantOrder, deleteOrder · 주입: OrderRepository, PaymentCommandService, OrderShipmentCommandService, InternalOrderItemCommandService, InternalOrderBidQueryService, InternalUserQueryService, WarehouseStorageCommandService, InternalAddressQueryService, EntityManager
- `InternalOrderItemCommandService` service/command/InternalOrderItemCommandService.java · public: createOrderItem · 주입: OrderItemRepository

### service-query (2)

- `InternalOrderBidQueryService` service/query/InternalOrderBidQueryService.java · public: findById, findByOrderId, getOrderBids, getOrderBidStatusCounts, getOrderBidDetail · 주입: OrderBidRepository
- `InternalOrderItemQueryService` service/query/InternalOrderItemQueryService.java · public: findById · 주입: OrderItemRepository

### custom (2)

- `InternalOrderBidRepositoryCustom` (interface) repository/InternalOrderBidRepositoryCustom.java · public: findOrderBidsByFilters, countOrderBidsByStatus, findOrderBidById, tradeCountByColorIds
- `InternalOrderBidRepositoryImpl` repository/InternalOrderBidRepositoryImpl.java · extends/implements InternalOrderBidRepositoryCustom · public: findOrderBidsByFilters, countOrderBidsByStatus, findOrderBidById, tradeCountByColorIds · 주입: JPAQueryFactory

### event (2)

- `InternalOrderEventConsumer` service/kafka/InternalOrderEventConsumer.java · public: handleOrderProcessingEvent, handleOrderRetryEvent · 주입: OrderRepository, PaymentCommandService, OrderShipmentCommandService, WarehouseStorageCommandService, InternalUserQueryService, InternalOrderEventProducer, SimpMessagingTemplate, KafkaTemplate
- `InternalOrderEventProducer` service/kafka/InternalOrderEventProducer.java · public: sendOrderProcessingEvent, sendOrderRetryEvent, sendOrderProcessingEventSync · 주입: KafkaTemplate

### 다른 도메인 참조 (import·주입 기준)

- **internaladdress**: `InternalAddressQueryService` ← InternalOrderCommandService
- **internalproduct**: `InternalProductSizeQueryService` ← InternalOrderBidCommandService
- **internalsale**: `InternalSaleBidQueryService` ← InternalOrderBidCommandService
- **internaluser**: `InternalUserQueryService` ← InternalOrderBidCommandService, InternalOrderCommandService, InternalOrderEventConsumer
- **order**: `BidStatus` ← InternalOrderBidCommandService, InternalOrderBidRepositoryImpl, InternalOrderCommandService; `Order` ← InternalOrderBidCommandService, InternalOrderCommandService, InternalOrderEventConsumer, InternalOrderItemCommandService, InternalOrderQueryController; `OrderBid` ← InternalOrderBidCommandService, InternalOrderBidQueryService; `OrderBidRepository` ← InternalOrderBidCommandService, InternalOrderBidQueryService; `OrderItem` ← InternalOrderCommandService, InternalOrderItemCommandService, InternalOrderItemQueryService; `OrderItemRepository` ← InternalOrderItemCommandService, InternalOrderItemQueryService; `OrderProcessingEvent` ← InternalOrderEventConsumer, InternalOrderEventProducer; `OrderRepository` ← InternalOrderCommandService, InternalOrderEventConsumer, InternalOrderQueryController; `OrderStatus` ← InternalOrderCommandService, InternalOrderEventConsumer, InternalOrderQueryController
- **payment**: `Payment` ← InternalOrderCommandService, InternalOrderEventConsumer; `PaymentCommandService` ← InternalOrderCommandService, InternalOrderEventConsumer
- **product**: `ProductSize` ← InternalOrderBidCommandService, InternalOrderCommandService, InternalOrderItemCommandService
- **sale**: `Sale` ← InternalOrderBidCommandService, InternalOrderCommandService; `SaleBid` ← InternalOrderBidCommandService, InternalOrderCommandService
- **shipment**: `OrderShipment` ← InternalOrderCommandService, InternalOrderEventConsumer; `OrderShipmentCommandService` ← InternalOrderCommandService, InternalOrderEventConsumer
- **user**: `User` ← InternalOrderBidCommandService, InternalOrderCommandService, InternalOrderEventConsumer
- **warehouseStorage**: `WarehouseStatus` ← InternalOrderCommandService; `WarehouseStorage` ← InternalOrderCommandService, InternalOrderEventConsumer; `WarehouseStorageCommandService` ← InternalOrderCommandService, InternalOrderEventConsumer

### 이 도메인을 참조하는 다른 도메인

- **internalsale**: InternalOrderBidQueryService
- **internalstyle**: InternalOrderItemQueryService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

- 발행 kafka `notification-topic`: `InternalOrderEventConsumer#sendSuccessNotification` → (소스 내 구독 없음)
- 발행 kafka `notification-topic`: `InternalOrderEventConsumer#sendFailureNotification` → (소스 내 구독 없음)
- 발행 kafka `order-processing-topic`: `InternalOrderEventProducer#sendOrderProcessingEvent` → `AdminOrderEventConsumer#handleOrderProcessingEvent` [adminorder], `InternalOrderEventConsumer#handleOrderProcessingEvent` [internalorder], `LegacyOrderEventConsumer#handleOrderProcessingEvent` [legacyorder], `OrderEventConsumer#handleOrderProcessingEvent` [order], `PartnerOrderEventConsumer#handleOrderProcessingEvent` [partnerorder], `SettlementOrderEventConsumer#handleOrderProcessingEvent` [settlementorder]
- 발행 kafka `order-retry-topic`: `InternalOrderEventProducer#sendOrderRetryEvent` → `AdminOrderEventConsumer#handleOrderRetryEvent` [adminorder], `InternalOrderEventConsumer#handleOrderRetryEvent` [internalorder], `LegacyOrderEventConsumer#handleOrderRetryEvent` [legacyorder], `OrderEventConsumer#handleOrderRetryEvent` [order], `PartnerOrderEventConsumer#handleOrderRetryEvent` [partnerorder], `SettlementOrderEventConsumer#handleOrderRetryEvent` [settlementorder]
- 발행 kafka `order-processing-topic`: `InternalOrderEventProducer#sendOrderProcessingEventSync` → `AdminOrderEventConsumer#handleOrderProcessingEvent` [adminorder], `InternalOrderEventConsumer#handleOrderProcessingEvent` [internalorder], `LegacyOrderEventConsumer#handleOrderProcessingEvent` [legacyorder], `OrderEventConsumer#handleOrderProcessingEvent` [order], `PartnerOrderEventConsumer#handleOrderProcessingEvent` [partnerorder], `SettlementOrderEventConsumer#handleOrderProcessingEvent` [settlementorder]
- 구독 kafka `order-processing-topic`: `InternalOrderEventConsumer#handleOrderProcessingEvent` (groupId=order-processing-group containerFactory=orderProcessingKafkaListenerContainerFactory) ← `AdminOrderEventProducer#sendOrderProcessingEvent` [adminorder], `AdminOrderEventProducer#sendOrderProcessingEventSync` [adminorder], `InternalOrderEventProducer#sendOrderProcessingEvent` [internalorder], `InternalOrderEventProducer#sendOrderProcessingEventSync` [internalorder], `LegacyOrderEventProducer#sendOrderProcessingEvent` [legacyorder], `LegacyOrderEventProducer#sendOrderProcessingEventSync` [legacyorder], `OrderEventProducer#sendOrderProcessingEvent` [order], `OrderEventProducer#sendOrderProcessingEventSync` [order], `PartnerOrderEventProducer#sendOrderProcessingEvent` [partnerorder], `PartnerOrderEventProducer#sendOrderProcessingEventSync` [partnerorder], `SettlementOrderEventProducer#sendOrderProcessingEvent` [settlementorder], `SettlementOrderEventProducer#sendOrderProcessingEventSync` [settlementorder]
- 구독 kafka `order-retry-topic`: `InternalOrderEventConsumer#handleOrderRetryEvent` (groupId=order-retry-group containerFactory=orderProcessingKafkaListenerContainerFactory) ← `AdminOrderEventProducer#sendOrderRetryEvent` [adminorder], `InternalOrderEventProducer#sendOrderRetryEvent` [internalorder], `LegacyOrderEventProducer#sendOrderRetryEvent` [legacyorder], `OrderEventProducer#sendOrderRetryEvent` [order], `PartnerOrderEventProducer#sendOrderRetryEvent` [partnerorder], `SettlementOrderEventProducer#sendOrderRetryEvent` [settlementorder]

## 6. AOP

(없음)

