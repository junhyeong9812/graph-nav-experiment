# settlementorder 도메인

기준 경로: `src/main/java/com/fream/back/domain/settlementorder/` · 파일 13개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/settlement/order-bids` | `SettlementOrderBidCommandController#createOrderBid` | `SettlementOrderBidCommandService#createOrderBid` |
| DELETE | `/settlement/order-bids/{orderBidId}` | `SettlementOrderBidCommandController#deleteOrderBid` | `SettlementOrderBidCommandService#deleteOrderBid` |
| POST | `/settlement/order-bids/instant` | `SettlementOrderBidCommandController#createInstantOrderBid` | `this#enrichPaymentRequest`, `SettlementOrderBidCommandService#createInstantOrderBid` |
| POST | `/settlement/orders/{orderId}/process-payment-shipment` | `SettlementOrderCommandController#processPaymentAndShipment` | `SettlementOrderEventProducer#sendOrderProcessingEvent` |
| POST | `/settlement/orders/{orderId}/cancel` | `SettlementOrderCommandController#cancelOrderProcessing` |  |
| GET | `/settlement/order-bids` | `SettlementOrderBidQueryController#getOrderBids` | `SettlementOrderBidQueryService#getOrderBids` |
| GET | `/settlement/order-bids/count` | `SettlementOrderBidQueryController#getOrderBidStatusCounts` | `SettlementOrderBidQueryService#getOrderBidStatusCounts` |
| GET | `/settlement/order-bids/{orderBidId}` | `SettlementOrderBidQueryController#getOrderBidDetail` | `SettlementOrderBidQueryService#getOrderBidDetail` |
| GET | `/settlement/orders/{orderId}/status` | `SettlementOrderQueryController#getOrderStatus` | `OrderRepository#findById~`, `this#getStatusDescription`, `this#canCancelOrder` |
| GET | `/settlement/orders/{orderId}` | `SettlementOrderQueryController#getOrderDetails` | `OrderRepository#findById~`, `this#getStatusDescription` |
| GET | `/settlement/orders` | `SettlementOrderQueryController#getUserOrders` |  |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `SettlementOrderBidCommandService`
  - `createOrderBid` → this#validateCreateOrderBidInput, SettlementUserQueryService#findByEmail, SettlementProductSizeQueryService#findById, SettlementOrderCommandService#createOrderFromBid, this#createAndSaveOrderBid, this#handleOrderBidCreationException
  - `createAndSaveOrderBid` → OrderBidRepository#save~
  - `createInstantOrderBid` → this#validateInstantOrderBidInput, SettlementUserQueryService#findByEmail, this#getSaleBidAndValidate, SettlementOrderCommandService#createInstantOrder, this#handleOrderBidCreationException, OrderBidRepository#save~
  - `getSaleBidAndValidate` → SettlementSaleBidQueryService#findById
  - `deleteOrderBid` → OrderBidRepository#findById~, SettlementOrderCommandService#deleteOrder, OrderBidRepository#delete~
- `SettlementOrderBidQueryService`
  - `findById` → OrderBidRepository#findById~
  - `findByOrderId` → OrderBidRepository#findByOrderId
  - `getOrderBids` → this#validateEmail, OrderBidRepository#findOrderBidsByFilters
  - `getOrderBidStatusCounts` → this#validateEmail, OrderBidRepository#countOrderBidsByStatus
  - `getOrderBidDetail` → this#validateEmail, OrderBidRepository#findOrderBidById
- `SettlementOrderCommandService`
  - `createOrderFromBid` → SettlementOrderItemCommandService#createOrderItem, OrderRepository#save~
  - `processPaymentAndShipment` → SettlementUserQueryService#findByEmail, OrderRepository#findById~, this#validatePaymentShipmentRequest, PaymentCommandService#processPayment, OrderShipmentCommandService#createOrderShipment, WarehouseStorageCommandService#createOrderStorage, SettlementOrderBidQueryService#findById
  - `createInstantOrder` → SettlementAddressQueryService#getAddress, OrderRepository#save~, SettlementOrderItemCommandService#createOrderItem, OrderShipmentCommandService#createOrderShipment, WarehouseStorageCommandService#updateWarehouseStatus, PaymentCommandService#processPayment
  - `deleteOrder` → OrderRepository#findById~, OrderRepository#delete~
- `SettlementOrderEventConsumer`
  - `handleOrderProcessingEvent` → this#isOrderAlreadyProcessed, OrderRepository#findById~, SettlementUserQueryService#findByEmail, this#processCompleteOrder, this#sendSuccessNotification, this#handleOrderProcessingFailure
  - `handleOrderRetryEvent` → this#sendFailureNotification, this#isOrderAlreadyProcessed, this#handleOrderProcessingEvent
  - `processCompleteOrder` → PaymentCommandService#processPayment, OrderShipmentCommandService#createOrderShipment, WarehouseStorageCommandService#createOrderStorage, OrderRepository#save~
  - `isOrderAlreadyProcessed` → OrderRepository#findById~
  - `handleOrderProcessingFailure` → this#isRetryableError, SettlementOrderEventProducer#sendOrderRetryEvent, this#sendFailureNotification
  - `sendSuccessNotification` → kafka:notification-topic
  - `sendFailureNotification` → this#isRetryableError, kafka:notification-topic
- `SettlementOrderEventProducer`
  - `sendOrderProcessingEvent` → kafka:order-processing-topic
  - `sendOrderRetryEvent` → kafka:order-retry-topic
  - `sendOrderProcessingEventSync` → kafka:order-processing-topic
- `SettlementOrderItemCommandService`
  - `createOrderItem` → OrderItemRepository#save~
- `SettlementOrderItemQueryService`
  - `findById` → OrderItemRepository#findById~

## 3. 클래스 목록 (역할별)

### controller (4)

- `SettlementOrderBidCommandController` controller/command/SettlementOrderBidCommandController.java · public: createOrderBid, deleteOrderBid, createInstantOrderBid · 주입: SettlementOrderBidCommandService
- `SettlementOrderCommandController` controller/command/SettlementOrderCommandController.java · public: processPaymentAndShipment, cancelOrderProcessing · 주입: SettlementOrderEventProducer
- `SettlementOrderBidQueryController` controller/query/SettlementOrderBidQueryController.java · public: getOrderBids, getOrderBidStatusCounts, getOrderBidDetail · 주입: SettlementOrderBidQueryService
- `SettlementOrderQueryController` controller/query/SettlementOrderQueryController.java · public: getOrderStatus, getOrderDetails, getUserOrders · 주입: OrderRepository

### service-command (3)

- `SettlementOrderBidCommandService` service/command/SettlementOrderBidCommandService.java · public: createOrderBid, createInstantOrderBid, matchOrderBid, deleteOrderBid · 주입: OrderBidRepository, SettlementOrderCommandService, SettlementProductSizeQueryService, SettlementUserQueryService, SettlementSaleBidQueryService
- `SettlementOrderCommandService` service/command/SettlementOrderCommandService.java · public: createOrderFromBid, processPaymentAndShipment, createInstantOrder, deleteOrder · 주입: OrderRepository, PaymentCommandService, OrderShipmentCommandService, SettlementOrderItemCommandService, SettlementOrderBidQueryService, SettlementUserQueryService, WarehouseStorageCommandService, SettlementAddressQueryService, EntityManager
- `SettlementOrderItemCommandService` service/command/SettlementOrderItemCommandService.java · public: createOrderItem · 주입: OrderItemRepository

### service-query (2)

- `SettlementOrderBidQueryService` service/query/SettlementOrderBidQueryService.java · public: findById, findByOrderId, getOrderBids, getOrderBidStatusCounts, getOrderBidDetail · 주입: OrderBidRepository
- `SettlementOrderItemQueryService` service/query/SettlementOrderItemQueryService.java · public: findById · 주입: OrderItemRepository

### custom (2)

- `SettlementOrderBidRepositoryCustom` (interface) repository/SettlementOrderBidRepositoryCustom.java · public: findOrderBidsByFilters, countOrderBidsByStatus, findOrderBidById, tradeCountByColorIds
- `SettlementOrderBidRepositoryImpl` repository/SettlementOrderBidRepositoryImpl.java · extends/implements SettlementOrderBidRepositoryCustom · public: findOrderBidsByFilters, countOrderBidsByStatus, findOrderBidById, tradeCountByColorIds · 주입: JPAQueryFactory

### event (2)

- `SettlementOrderEventConsumer` service/kafka/SettlementOrderEventConsumer.java · public: handleOrderProcessingEvent, handleOrderRetryEvent · 주입: OrderRepository, PaymentCommandService, OrderShipmentCommandService, WarehouseStorageCommandService, SettlementUserQueryService, SettlementOrderEventProducer, SimpMessagingTemplate, KafkaTemplate
- `SettlementOrderEventProducer` service/kafka/SettlementOrderEventProducer.java · public: sendOrderProcessingEvent, sendOrderRetryEvent, sendOrderProcessingEventSync · 주입: KafkaTemplate

### 다른 도메인 참조 (import·주입 기준)

- **order**: `BidStatus` ← SettlementOrderBidCommandService, SettlementOrderBidRepositoryImpl, SettlementOrderCommandService; `Order` ← SettlementOrderBidCommandService, SettlementOrderCommandService, SettlementOrderEventConsumer, SettlementOrderItemCommandService, SettlementOrderQueryController; `OrderBid` ← SettlementOrderBidCommandService, SettlementOrderBidQueryService; `OrderBidRepository` ← SettlementOrderBidCommandService, SettlementOrderBidQueryService; `OrderItem` ← SettlementOrderCommandService, SettlementOrderItemCommandService, SettlementOrderItemQueryService; `OrderItemRepository` ← SettlementOrderItemCommandService, SettlementOrderItemQueryService; `OrderProcessingEvent` ← SettlementOrderEventConsumer, SettlementOrderEventProducer; `OrderRepository` ← SettlementOrderCommandService, SettlementOrderEventConsumer, SettlementOrderQueryController; `OrderStatus` ← SettlementOrderCommandService, SettlementOrderEventConsumer, SettlementOrderQueryController
- **payment**: `Payment` ← SettlementOrderCommandService, SettlementOrderEventConsumer; `PaymentCommandService` ← SettlementOrderCommandService, SettlementOrderEventConsumer
- **product**: `ProductSize` ← SettlementOrderBidCommandService, SettlementOrderCommandService, SettlementOrderItemCommandService
- **sale**: `Sale` ← SettlementOrderBidCommandService, SettlementOrderCommandService; `SaleBid` ← SettlementOrderBidCommandService, SettlementOrderCommandService
- **settlementaddress**: `SettlementAddressQueryService` ← SettlementOrderCommandService
- **settlementproduct**: `SettlementProductSizeQueryService` ← SettlementOrderBidCommandService
- **settlementsale**: `SettlementSaleBidQueryService` ← SettlementOrderBidCommandService
- **settlementuser**: `SettlementUserQueryService` ← SettlementOrderBidCommandService, SettlementOrderCommandService, SettlementOrderEventConsumer
- **shipment**: `OrderShipment` ← SettlementOrderCommandService, SettlementOrderEventConsumer; `OrderShipmentCommandService` ← SettlementOrderCommandService, SettlementOrderEventConsumer
- **user**: `User` ← SettlementOrderBidCommandService, SettlementOrderCommandService, SettlementOrderEventConsumer
- **warehouseStorage**: `WarehouseStatus` ← SettlementOrderCommandService; `WarehouseStorage` ← SettlementOrderCommandService, SettlementOrderEventConsumer; `WarehouseStorageCommandService` ← SettlementOrderCommandService, SettlementOrderEventConsumer

### 이 도메인을 참조하는 다른 도메인

- **settlementsale**: SettlementOrderBidQueryService
- **settlementstyle**: SettlementOrderItemQueryService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

- 발행 kafka `notification-topic`: `SettlementOrderEventConsumer#sendSuccessNotification` → (소스 내 구독 없음)
- 발행 kafka `notification-topic`: `SettlementOrderEventConsumer#sendFailureNotification` → (소스 내 구독 없음)
- 발행 kafka `order-processing-topic`: `SettlementOrderEventProducer#sendOrderProcessingEvent` → `AdminOrderEventConsumer#handleOrderProcessingEvent` [adminorder], `InternalOrderEventConsumer#handleOrderProcessingEvent` [internalorder], `LegacyOrderEventConsumer#handleOrderProcessingEvent` [legacyorder], `OrderEventConsumer#handleOrderProcessingEvent` [order], `PartnerOrderEventConsumer#handleOrderProcessingEvent` [partnerorder], `SettlementOrderEventConsumer#handleOrderProcessingEvent` [settlementorder]
- 발행 kafka `order-retry-topic`: `SettlementOrderEventProducer#sendOrderRetryEvent` → `AdminOrderEventConsumer#handleOrderRetryEvent` [adminorder], `InternalOrderEventConsumer#handleOrderRetryEvent` [internalorder], `LegacyOrderEventConsumer#handleOrderRetryEvent` [legacyorder], `OrderEventConsumer#handleOrderRetryEvent` [order], `PartnerOrderEventConsumer#handleOrderRetryEvent` [partnerorder], `SettlementOrderEventConsumer#handleOrderRetryEvent` [settlementorder]
- 발행 kafka `order-processing-topic`: `SettlementOrderEventProducer#sendOrderProcessingEventSync` → `AdminOrderEventConsumer#handleOrderProcessingEvent` [adminorder], `InternalOrderEventConsumer#handleOrderProcessingEvent` [internalorder], `LegacyOrderEventConsumer#handleOrderProcessingEvent` [legacyorder], `OrderEventConsumer#handleOrderProcessingEvent` [order], `PartnerOrderEventConsumer#handleOrderProcessingEvent` [partnerorder], `SettlementOrderEventConsumer#handleOrderProcessingEvent` [settlementorder]
- 구독 kafka `order-processing-topic`: `SettlementOrderEventConsumer#handleOrderProcessingEvent` (groupId=order-processing-group containerFactory=orderProcessingKafkaListenerContainerFactory) ← `AdminOrderEventProducer#sendOrderProcessingEvent` [adminorder], `AdminOrderEventProducer#sendOrderProcessingEventSync` [adminorder], `InternalOrderEventProducer#sendOrderProcessingEvent` [internalorder], `InternalOrderEventProducer#sendOrderProcessingEventSync` [internalorder], `LegacyOrderEventProducer#sendOrderProcessingEvent` [legacyorder], `LegacyOrderEventProducer#sendOrderProcessingEventSync` [legacyorder], `OrderEventProducer#sendOrderProcessingEvent` [order], `OrderEventProducer#sendOrderProcessingEventSync` [order], `PartnerOrderEventProducer#sendOrderProcessingEvent` [partnerorder], `PartnerOrderEventProducer#sendOrderProcessingEventSync` [partnerorder], `SettlementOrderEventProducer#sendOrderProcessingEvent` [settlementorder], `SettlementOrderEventProducer#sendOrderProcessingEventSync` [settlementorder]
- 구독 kafka `order-retry-topic`: `SettlementOrderEventConsumer#handleOrderRetryEvent` (groupId=order-retry-group containerFactory=orderProcessingKafkaListenerContainerFactory) ← `AdminOrderEventProducer#sendOrderRetryEvent` [adminorder], `InternalOrderEventProducer#sendOrderRetryEvent` [internalorder], `LegacyOrderEventProducer#sendOrderRetryEvent` [legacyorder], `OrderEventProducer#sendOrderRetryEvent` [order], `PartnerOrderEventProducer#sendOrderRetryEvent` [partnerorder], `SettlementOrderEventProducer#sendOrderRetryEvent` [settlementorder]

## 6. AOP

(없음)

