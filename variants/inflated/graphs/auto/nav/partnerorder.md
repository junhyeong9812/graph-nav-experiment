# partnerorder 도메인

기준 경로: `src/main/java/com/fream/back/domain/partnerorder/` · 파일 13개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/partner/order-bids` | `PartnerOrderBidCommandController#createOrderBid` | `PartnerOrderBidCommandService#createOrderBid` |
| DELETE | `/partner/order-bids/{orderBidId}` | `PartnerOrderBidCommandController#deleteOrderBid` | `PartnerOrderBidCommandService#deleteOrderBid` |
| POST | `/partner/order-bids/instant` | `PartnerOrderBidCommandController#createInstantOrderBid` | `this#enrichPaymentRequest`, `PartnerOrderBidCommandService#createInstantOrderBid` |
| POST | `/partner/orders/{orderId}/process-payment-shipment` | `PartnerOrderCommandController#processPaymentAndShipment` | `PartnerOrderEventProducer#sendOrderProcessingEvent` |
| POST | `/partner/orders/{orderId}/cancel` | `PartnerOrderCommandController#cancelOrderProcessing` |  |
| GET | `/partner/order-bids` | `PartnerOrderBidQueryController#getOrderBids` | `PartnerOrderBidQueryService#getOrderBids` |
| GET | `/partner/order-bids/count` | `PartnerOrderBidQueryController#getOrderBidStatusCounts` | `PartnerOrderBidQueryService#getOrderBidStatusCounts` |
| GET | `/partner/order-bids/{orderBidId}` | `PartnerOrderBidQueryController#getOrderBidDetail` | `PartnerOrderBidQueryService#getOrderBidDetail` |
| GET | `/partner/orders/{orderId}/status` | `PartnerOrderQueryController#getOrderStatus` | `OrderRepository#findById~`, `this#getStatusDescription`, `this#canCancelOrder` |
| GET | `/partner/orders/{orderId}` | `PartnerOrderQueryController#getOrderDetails` | `OrderRepository#findById~`, `this#getStatusDescription` |
| GET | `/partner/orders` | `PartnerOrderQueryController#getUserOrders` |  |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `PartnerOrderBidCommandService`
  - `createOrderBid` → this#validateCreateOrderBidInput, PartnerUserQueryService#findByEmail, PartnerProductSizeQueryService#findById, PartnerOrderCommandService#createOrderFromBid, this#createAndSaveOrderBid, this#handleOrderBidCreationException
  - `createAndSaveOrderBid` → OrderBidRepository#save~
  - `createInstantOrderBid` → this#validateInstantOrderBidInput, PartnerUserQueryService#findByEmail, this#getSaleBidAndValidate, PartnerOrderCommandService#createInstantOrder, this#handleOrderBidCreationException, OrderBidRepository#save~
  - `getSaleBidAndValidate` → PartnerSaleBidQueryService#findById
  - `deleteOrderBid` → OrderBidRepository#findById~, PartnerOrderCommandService#deleteOrder, OrderBidRepository#delete~
- `PartnerOrderBidQueryService`
  - `findById` → OrderBidRepository#findById~
  - `findByOrderId` → OrderBidRepository#findByOrderId
  - `getOrderBids` → this#validateEmail, OrderBidRepository#findOrderBidsByFilters
  - `getOrderBidStatusCounts` → this#validateEmail, OrderBidRepository#countOrderBidsByStatus
  - `getOrderBidDetail` → this#validateEmail, OrderBidRepository#findOrderBidById
- `PartnerOrderCommandService`
  - `createOrderFromBid` → PartnerOrderItemCommandService#createOrderItem, OrderRepository#save~
  - `processPaymentAndShipment` → PartnerUserQueryService#findByEmail, OrderRepository#findById~, this#validatePaymentShipmentRequest, PaymentCommandService#processPayment, OrderShipmentCommandService#createOrderShipment, WarehouseStorageCommandService#createOrderStorage, PartnerOrderBidQueryService#findById
  - `createInstantOrder` → PartnerAddressQueryService#getAddress, OrderRepository#save~, PartnerOrderItemCommandService#createOrderItem, OrderShipmentCommandService#createOrderShipment, WarehouseStorageCommandService#updateWarehouseStatus, PaymentCommandService#processPayment
  - `deleteOrder` → OrderRepository#findById~, OrderRepository#delete~
- `PartnerOrderEventConsumer`
  - `handleOrderProcessingEvent` → this#isOrderAlreadyProcessed, OrderRepository#findById~, PartnerUserQueryService#findByEmail, this#processCompleteOrder, this#sendSuccessNotification, this#handleOrderProcessingFailure
  - `handleOrderRetryEvent` → this#sendFailureNotification, this#isOrderAlreadyProcessed, this#handleOrderProcessingEvent
  - `processCompleteOrder` → PaymentCommandService#processPayment, OrderShipmentCommandService#createOrderShipment, WarehouseStorageCommandService#createOrderStorage, OrderRepository#save~
  - `isOrderAlreadyProcessed` → OrderRepository#findById~
  - `handleOrderProcessingFailure` → this#isRetryableError, PartnerOrderEventProducer#sendOrderRetryEvent, this#sendFailureNotification
  - `sendSuccessNotification` → kafka:notification-topic
  - `sendFailureNotification` → this#isRetryableError, kafka:notification-topic
- `PartnerOrderEventProducer`
  - `sendOrderProcessingEvent` → kafka:order-processing-topic
  - `sendOrderRetryEvent` → kafka:order-retry-topic
  - `sendOrderProcessingEventSync` → kafka:order-processing-topic
- `PartnerOrderItemCommandService`
  - `createOrderItem` → OrderItemRepository#save~
- `PartnerOrderItemQueryService`
  - `findById` → OrderItemRepository#findById~

## 3. 클래스 목록 (역할별)

### controller (4)

- `PartnerOrderBidCommandController` controller/command/PartnerOrderBidCommandController.java · public: createOrderBid, deleteOrderBid, createInstantOrderBid · 주입: PartnerOrderBidCommandService
- `PartnerOrderCommandController` controller/command/PartnerOrderCommandController.java · public: processPaymentAndShipment, cancelOrderProcessing · 주입: PartnerOrderEventProducer
- `PartnerOrderBidQueryController` controller/query/PartnerOrderBidQueryController.java · public: getOrderBids, getOrderBidStatusCounts, getOrderBidDetail · 주입: PartnerOrderBidQueryService
- `PartnerOrderQueryController` controller/query/PartnerOrderQueryController.java · public: getOrderStatus, getOrderDetails, getUserOrders · 주입: OrderRepository

### service-command (3)

- `PartnerOrderBidCommandService` service/command/PartnerOrderBidCommandService.java · public: createOrderBid, createInstantOrderBid, matchOrderBid, deleteOrderBid · 주입: OrderBidRepository, PartnerOrderCommandService, PartnerProductSizeQueryService, PartnerUserQueryService, PartnerSaleBidQueryService
- `PartnerOrderCommandService` service/command/PartnerOrderCommandService.java · public: createOrderFromBid, processPaymentAndShipment, createInstantOrder, deleteOrder · 주입: OrderRepository, PaymentCommandService, OrderShipmentCommandService, PartnerOrderItemCommandService, PartnerOrderBidQueryService, PartnerUserQueryService, WarehouseStorageCommandService, PartnerAddressQueryService, EntityManager
- `PartnerOrderItemCommandService` service/command/PartnerOrderItemCommandService.java · public: createOrderItem · 주입: OrderItemRepository

### service-query (2)

- `PartnerOrderBidQueryService` service/query/PartnerOrderBidQueryService.java · public: findById, findByOrderId, getOrderBids, getOrderBidStatusCounts, getOrderBidDetail · 주입: OrderBidRepository
- `PartnerOrderItemQueryService` service/query/PartnerOrderItemQueryService.java · public: findById · 주입: OrderItemRepository

### custom (2)

- `PartnerOrderBidRepositoryCustom` (interface) repository/PartnerOrderBidRepositoryCustom.java · public: findOrderBidsByFilters, countOrderBidsByStatus, findOrderBidById, tradeCountByColorIds
- `PartnerOrderBidRepositoryImpl` repository/PartnerOrderBidRepositoryImpl.java · extends/implements PartnerOrderBidRepositoryCustom · public: findOrderBidsByFilters, countOrderBidsByStatus, findOrderBidById, tradeCountByColorIds · 주입: JPAQueryFactory

### event (2)

- `PartnerOrderEventConsumer` service/kafka/PartnerOrderEventConsumer.java · public: handleOrderProcessingEvent, handleOrderRetryEvent · 주입: OrderRepository, PaymentCommandService, OrderShipmentCommandService, WarehouseStorageCommandService, PartnerUserQueryService, PartnerOrderEventProducer, SimpMessagingTemplate, KafkaTemplate
- `PartnerOrderEventProducer` service/kafka/PartnerOrderEventProducer.java · public: sendOrderProcessingEvent, sendOrderRetryEvent, sendOrderProcessingEventSync · 주입: KafkaTemplate

### 다른 도메인 참조 (import·주입 기준)

- **order**: `BidStatus` ← PartnerOrderBidCommandService, PartnerOrderBidRepositoryImpl, PartnerOrderCommandService; `Order` ← PartnerOrderBidCommandService, PartnerOrderCommandService, PartnerOrderEventConsumer, PartnerOrderItemCommandService, PartnerOrderQueryController; `OrderBid` ← PartnerOrderBidCommandService, PartnerOrderBidQueryService; `OrderBidRepository` ← PartnerOrderBidCommandService, PartnerOrderBidQueryService; `OrderItem` ← PartnerOrderCommandService, PartnerOrderItemCommandService, PartnerOrderItemQueryService; `OrderItemRepository` ← PartnerOrderItemCommandService, PartnerOrderItemQueryService; `OrderProcessingEvent` ← PartnerOrderEventConsumer, PartnerOrderEventProducer; `OrderRepository` ← PartnerOrderCommandService, PartnerOrderEventConsumer, PartnerOrderQueryController; `OrderStatus` ← PartnerOrderCommandService, PartnerOrderEventConsumer, PartnerOrderQueryController
- **partneraddress**: `PartnerAddressQueryService` ← PartnerOrderCommandService
- **partnerproduct**: `PartnerProductSizeQueryService` ← PartnerOrderBidCommandService
- **partnersale**: `PartnerSaleBidQueryService` ← PartnerOrderBidCommandService
- **partneruser**: `PartnerUserQueryService` ← PartnerOrderBidCommandService, PartnerOrderCommandService, PartnerOrderEventConsumer
- **payment**: `Payment` ← PartnerOrderCommandService, PartnerOrderEventConsumer; `PaymentCommandService` ← PartnerOrderCommandService, PartnerOrderEventConsumer
- **product**: `ProductSize` ← PartnerOrderBidCommandService, PartnerOrderCommandService, PartnerOrderItemCommandService
- **sale**: `Sale` ← PartnerOrderBidCommandService, PartnerOrderCommandService; `SaleBid` ← PartnerOrderBidCommandService, PartnerOrderCommandService
- **shipment**: `OrderShipment` ← PartnerOrderCommandService, PartnerOrderEventConsumer; `OrderShipmentCommandService` ← PartnerOrderCommandService, PartnerOrderEventConsumer
- **user**: `User` ← PartnerOrderBidCommandService, PartnerOrderCommandService, PartnerOrderEventConsumer
- **warehouseStorage**: `WarehouseStatus` ← PartnerOrderCommandService; `WarehouseStorage` ← PartnerOrderCommandService, PartnerOrderEventConsumer; `WarehouseStorageCommandService` ← PartnerOrderCommandService, PartnerOrderEventConsumer

### 이 도메인을 참조하는 다른 도메인

- **partnersale**: PartnerOrderBidQueryService
- **partnerstyle**: PartnerOrderItemQueryService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

- 발행 kafka `notification-topic`: `PartnerOrderEventConsumer#sendSuccessNotification` → (소스 내 구독 없음)
- 발행 kafka `notification-topic`: `PartnerOrderEventConsumer#sendFailureNotification` → (소스 내 구독 없음)
- 발행 kafka `order-processing-topic`: `PartnerOrderEventProducer#sendOrderProcessingEvent` → `AdminOrderEventConsumer#handleOrderProcessingEvent` [adminorder], `InternalOrderEventConsumer#handleOrderProcessingEvent` [internalorder], `LegacyOrderEventConsumer#handleOrderProcessingEvent` [legacyorder], `OrderEventConsumer#handleOrderProcessingEvent` [order], `PartnerOrderEventConsumer#handleOrderProcessingEvent` [partnerorder], `SettlementOrderEventConsumer#handleOrderProcessingEvent` [settlementorder]
- 발행 kafka `order-retry-topic`: `PartnerOrderEventProducer#sendOrderRetryEvent` → `AdminOrderEventConsumer#handleOrderRetryEvent` [adminorder], `InternalOrderEventConsumer#handleOrderRetryEvent` [internalorder], `LegacyOrderEventConsumer#handleOrderRetryEvent` [legacyorder], `OrderEventConsumer#handleOrderRetryEvent` [order], `PartnerOrderEventConsumer#handleOrderRetryEvent` [partnerorder], `SettlementOrderEventConsumer#handleOrderRetryEvent` [settlementorder]
- 발행 kafka `order-processing-topic`: `PartnerOrderEventProducer#sendOrderProcessingEventSync` → `AdminOrderEventConsumer#handleOrderProcessingEvent` [adminorder], `InternalOrderEventConsumer#handleOrderProcessingEvent` [internalorder], `LegacyOrderEventConsumer#handleOrderProcessingEvent` [legacyorder], `OrderEventConsumer#handleOrderProcessingEvent` [order], `PartnerOrderEventConsumer#handleOrderProcessingEvent` [partnerorder], `SettlementOrderEventConsumer#handleOrderProcessingEvent` [settlementorder]
- 구독 kafka `order-processing-topic`: `PartnerOrderEventConsumer#handleOrderProcessingEvent` (groupId=order-processing-group containerFactory=orderProcessingKafkaListenerContainerFactory) ← `AdminOrderEventProducer#sendOrderProcessingEvent` [adminorder], `AdminOrderEventProducer#sendOrderProcessingEventSync` [adminorder], `InternalOrderEventProducer#sendOrderProcessingEvent` [internalorder], `InternalOrderEventProducer#sendOrderProcessingEventSync` [internalorder], `LegacyOrderEventProducer#sendOrderProcessingEvent` [legacyorder], `LegacyOrderEventProducer#sendOrderProcessingEventSync` [legacyorder], `OrderEventProducer#sendOrderProcessingEvent` [order], `OrderEventProducer#sendOrderProcessingEventSync` [order], `PartnerOrderEventProducer#sendOrderProcessingEvent` [partnerorder], `PartnerOrderEventProducer#sendOrderProcessingEventSync` [partnerorder], `SettlementOrderEventProducer#sendOrderProcessingEvent` [settlementorder], `SettlementOrderEventProducer#sendOrderProcessingEventSync` [settlementorder]
- 구독 kafka `order-retry-topic`: `PartnerOrderEventConsumer#handleOrderRetryEvent` (groupId=order-retry-group containerFactory=orderProcessingKafkaListenerContainerFactory) ← `AdminOrderEventProducer#sendOrderRetryEvent` [adminorder], `InternalOrderEventProducer#sendOrderRetryEvent` [internalorder], `LegacyOrderEventProducer#sendOrderRetryEvent` [legacyorder], `OrderEventProducer#sendOrderRetryEvent` [order], `PartnerOrderEventProducer#sendOrderRetryEvent` [partnerorder], `SettlementOrderEventProducer#sendOrderRetryEvent` [settlementorder]

## 6. AOP

(없음)

