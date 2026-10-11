# adminorder 도메인

기준 경로: `src/main/java/com/fream/back/domain/adminorder/` · 파일 13개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/admin/order-bids` | `AdminOrderBidCommandController#createOrderBid` | `AdminOrderBidCommandService#createOrderBid` |
| DELETE | `/admin/order-bids/{orderBidId}` | `AdminOrderBidCommandController#deleteOrderBid` | `AdminOrderBidCommandService#deleteOrderBid` |
| POST | `/admin/order-bids/instant` | `AdminOrderBidCommandController#createInstantOrderBid` | `this#enrichPaymentRequest`, `AdminOrderBidCommandService#createInstantOrderBid` |
| POST | `/admin/orders/{orderId}/process-payment-shipment` | `AdminOrderCommandController#processPaymentAndShipment` | `AdminOrderEventProducer#sendOrderProcessingEvent` |
| POST | `/admin/orders/{orderId}/cancel` | `AdminOrderCommandController#cancelOrderProcessing` |  |
| GET | `/admin/order-bids` | `AdminOrderBidQueryController#getOrderBids` | `AdminOrderBidQueryService#getOrderBids` |
| GET | `/admin/order-bids/count` | `AdminOrderBidQueryController#getOrderBidStatusCounts` | `AdminOrderBidQueryService#getOrderBidStatusCounts` |
| GET | `/admin/order-bids/{orderBidId}` | `AdminOrderBidQueryController#getOrderBidDetail` | `AdminOrderBidQueryService#getOrderBidDetail` |
| GET | `/admin/orders/{orderId}/status` | `AdminOrderQueryController#getOrderStatus` | `OrderRepository#findById~`, `this#getStatusDescription`, `this#canCancelOrder` |
| GET | `/admin/orders/{orderId}` | `AdminOrderQueryController#getOrderDetails` | `OrderRepository#findById~`, `this#getStatusDescription` |
| GET | `/admin/orders` | `AdminOrderQueryController#getUserOrders` |  |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `AdminOrderBidCommandService`
  - `createOrderBid` → this#validateCreateOrderBidInput, AdminUserQueryService#findByEmail, AdminProductSizeQueryService#findById, AdminOrderCommandService#createOrderFromBid, this#createAndSaveOrderBid, this#handleOrderBidCreationException
  - `createAndSaveOrderBid` → OrderBidRepository#save~
  - `createInstantOrderBid` → this#validateInstantOrderBidInput, AdminUserQueryService#findByEmail, this#getSaleBidAndValidate, AdminOrderCommandService#createInstantOrder, this#handleOrderBidCreationException, OrderBidRepository#save~
  - `getSaleBidAndValidate` → AdminSaleBidQueryService#findById
  - `deleteOrderBid` → OrderBidRepository#findById~, AdminOrderCommandService#deleteOrder, OrderBidRepository#delete~
- `AdminOrderBidQueryService`
  - `findById` → OrderBidRepository#findById~
  - `findByOrderId` → OrderBidRepository#findByOrderId
  - `getOrderBids` → this#validateEmail, OrderBidRepository#findOrderBidsByFilters
  - `getOrderBidStatusCounts` → this#validateEmail, OrderBidRepository#countOrderBidsByStatus
  - `getOrderBidDetail` → this#validateEmail, OrderBidRepository#findOrderBidById
- `AdminOrderCommandService`
  - `createOrderFromBid` → AdminOrderItemCommandService#createOrderItem, OrderRepository#save~
  - `processPaymentAndShipment` → AdminUserQueryService#findByEmail, OrderRepository#findById~, this#validatePaymentShipmentRequest, PaymentCommandService#processPayment, OrderShipmentCommandService#createOrderShipment, WarehouseStorageCommandService#createOrderStorage, AdminOrderBidQueryService#findById
  - `createInstantOrder` → AdminAddressQueryService#getAddress, OrderRepository#save~, AdminOrderItemCommandService#createOrderItem, OrderShipmentCommandService#createOrderShipment, WarehouseStorageCommandService#updateWarehouseStatus, PaymentCommandService#processPayment
  - `deleteOrder` → OrderRepository#findById~, OrderRepository#delete~
- `AdminOrderEventConsumer`
  - `handleOrderProcessingEvent` → this#isOrderAlreadyProcessed, OrderRepository#findById~, AdminUserQueryService#findByEmail, this#processCompleteOrder, this#sendSuccessNotification, this#handleOrderProcessingFailure
  - `handleOrderRetryEvent` → this#sendFailureNotification, this#isOrderAlreadyProcessed, this#handleOrderProcessingEvent
  - `processCompleteOrder` → PaymentCommandService#processPayment, OrderShipmentCommandService#createOrderShipment, WarehouseStorageCommandService#createOrderStorage, OrderRepository#save~
  - `isOrderAlreadyProcessed` → OrderRepository#findById~
  - `handleOrderProcessingFailure` → this#isRetryableError, AdminOrderEventProducer#sendOrderRetryEvent, this#sendFailureNotification
  - `sendSuccessNotification` → kafka:notification-topic
  - `sendFailureNotification` → this#isRetryableError, kafka:notification-topic
- `AdminOrderEventProducer`
  - `sendOrderProcessingEvent` → kafka:order-processing-topic
  - `sendOrderRetryEvent` → kafka:order-retry-topic
  - `sendOrderProcessingEventSync` → kafka:order-processing-topic
- `AdminOrderItemCommandService`
  - `createOrderItem` → OrderItemRepository#save~
- `AdminOrderItemQueryService`
  - `findById` → OrderItemRepository#findById~

## 3. 클래스 목록 (역할별)

### controller (4)

- `AdminOrderBidCommandController` controller/command/AdminOrderBidCommandController.java · public: createOrderBid, deleteOrderBid, createInstantOrderBid · 주입: AdminOrderBidCommandService
- `AdminOrderCommandController` controller/command/AdminOrderCommandController.java · public: processPaymentAndShipment, cancelOrderProcessing · 주입: AdminOrderEventProducer
- `AdminOrderBidQueryController` controller/query/AdminOrderBidQueryController.java · public: getOrderBids, getOrderBidStatusCounts, getOrderBidDetail · 주입: AdminOrderBidQueryService
- `AdminOrderQueryController` controller/query/AdminOrderQueryController.java · public: getOrderStatus, getOrderDetails, getUserOrders · 주입: OrderRepository

### service-command (3)

- `AdminOrderBidCommandService` service/command/AdminOrderBidCommandService.java · public: createOrderBid, createInstantOrderBid, matchOrderBid, deleteOrderBid · 주입: OrderBidRepository, AdminOrderCommandService, AdminProductSizeQueryService, AdminUserQueryService, AdminSaleBidQueryService
- `AdminOrderCommandService` service/command/AdminOrderCommandService.java · public: createOrderFromBid, processPaymentAndShipment, createInstantOrder, deleteOrder · 주입: OrderRepository, PaymentCommandService, OrderShipmentCommandService, AdminOrderItemCommandService, AdminOrderBidQueryService, AdminUserQueryService, WarehouseStorageCommandService, AdminAddressQueryService, EntityManager
- `AdminOrderItemCommandService` service/command/AdminOrderItemCommandService.java · public: createOrderItem · 주입: OrderItemRepository

### service-query (2)

- `AdminOrderBidQueryService` service/query/AdminOrderBidQueryService.java · public: findById, findByOrderId, getOrderBids, getOrderBidStatusCounts, getOrderBidDetail · 주입: OrderBidRepository
- `AdminOrderItemQueryService` service/query/AdminOrderItemQueryService.java · public: findById · 주입: OrderItemRepository

### custom (2)

- `AdminOrderBidRepositoryCustom` (interface) repository/AdminOrderBidRepositoryCustom.java · public: findOrderBidsByFilters, countOrderBidsByStatus, findOrderBidById, tradeCountByColorIds
- `AdminOrderBidRepositoryImpl` repository/AdminOrderBidRepositoryImpl.java · extends/implements AdminOrderBidRepositoryCustom · public: findOrderBidsByFilters, countOrderBidsByStatus, findOrderBidById, tradeCountByColorIds · 주입: JPAQueryFactory

### event (2)

- `AdminOrderEventConsumer` service/kafka/AdminOrderEventConsumer.java · public: handleOrderProcessingEvent, handleOrderRetryEvent · 주입: OrderRepository, PaymentCommandService, OrderShipmentCommandService, WarehouseStorageCommandService, AdminUserQueryService, AdminOrderEventProducer, SimpMessagingTemplate, KafkaTemplate
- `AdminOrderEventProducer` service/kafka/AdminOrderEventProducer.java · public: sendOrderProcessingEvent, sendOrderRetryEvent, sendOrderProcessingEventSync · 주입: KafkaTemplate

### 다른 도메인 참조 (import·주입 기준)

- **adminaddress**: `AdminAddressQueryService` ← AdminOrderCommandService
- **adminproduct**: `AdminProductSizeQueryService` ← AdminOrderBidCommandService
- **adminsale**: `AdminSaleBidQueryService` ← AdminOrderBidCommandService
- **adminuser**: `AdminUserQueryService` ← AdminOrderBidCommandService, AdminOrderCommandService, AdminOrderEventConsumer
- **order**: `BidStatus` ← AdminOrderBidCommandService, AdminOrderBidRepositoryImpl, AdminOrderCommandService; `Order` ← AdminOrderBidCommandService, AdminOrderCommandService, AdminOrderEventConsumer, AdminOrderItemCommandService, AdminOrderQueryController; `OrderBid` ← AdminOrderBidCommandService, AdminOrderBidQueryService; `OrderBidRepository` ← AdminOrderBidCommandService, AdminOrderBidQueryService; `OrderItem` ← AdminOrderCommandService, AdminOrderItemCommandService, AdminOrderItemQueryService; `OrderItemRepository` ← AdminOrderItemCommandService, AdminOrderItemQueryService; `OrderProcessingEvent` ← AdminOrderEventConsumer, AdminOrderEventProducer; `OrderRepository` ← AdminOrderCommandService, AdminOrderEventConsumer, AdminOrderQueryController; `OrderStatus` ← AdminOrderCommandService, AdminOrderEventConsumer, AdminOrderQueryController
- **payment**: `Payment` ← AdminOrderCommandService, AdminOrderEventConsumer; `PaymentCommandService` ← AdminOrderCommandService, AdminOrderEventConsumer
- **product**: `ProductSize` ← AdminOrderBidCommandService, AdminOrderCommandService, AdminOrderItemCommandService
- **sale**: `Sale` ← AdminOrderBidCommandService, AdminOrderCommandService; `SaleBid` ← AdminOrderBidCommandService, AdminOrderCommandService
- **shipment**: `OrderShipment` ← AdminOrderCommandService, AdminOrderEventConsumer; `OrderShipmentCommandService` ← AdminOrderCommandService, AdminOrderEventConsumer
- **user**: `User` ← AdminOrderBidCommandService, AdminOrderCommandService, AdminOrderEventConsumer
- **warehouseStorage**: `WarehouseStatus` ← AdminOrderCommandService; `WarehouseStorage` ← AdminOrderCommandService, AdminOrderEventConsumer; `WarehouseStorageCommandService` ← AdminOrderCommandService, AdminOrderEventConsumer

### 이 도메인을 참조하는 다른 도메인

- **adminsale**: AdminOrderBidQueryService
- **adminstyle**: AdminOrderItemQueryService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

- 발행 kafka `notification-topic`: `AdminOrderEventConsumer#sendSuccessNotification` → (소스 내 구독 없음)
- 발행 kafka `notification-topic`: `AdminOrderEventConsumer#sendFailureNotification` → (소스 내 구독 없음)
- 발행 kafka `order-processing-topic`: `AdminOrderEventProducer#sendOrderProcessingEvent` → `AdminOrderEventConsumer#handleOrderProcessingEvent` [adminorder], `InternalOrderEventConsumer#handleOrderProcessingEvent` [internalorder], `LegacyOrderEventConsumer#handleOrderProcessingEvent` [legacyorder], `OrderEventConsumer#handleOrderProcessingEvent` [order], `PartnerOrderEventConsumer#handleOrderProcessingEvent` [partnerorder], `SettlementOrderEventConsumer#handleOrderProcessingEvent` [settlementorder]
- 발행 kafka `order-retry-topic`: `AdminOrderEventProducer#sendOrderRetryEvent` → `AdminOrderEventConsumer#handleOrderRetryEvent` [adminorder], `InternalOrderEventConsumer#handleOrderRetryEvent` [internalorder], `LegacyOrderEventConsumer#handleOrderRetryEvent` [legacyorder], `OrderEventConsumer#handleOrderRetryEvent` [order], `PartnerOrderEventConsumer#handleOrderRetryEvent` [partnerorder], `SettlementOrderEventConsumer#handleOrderRetryEvent` [settlementorder]
- 발행 kafka `order-processing-topic`: `AdminOrderEventProducer#sendOrderProcessingEventSync` → `AdminOrderEventConsumer#handleOrderProcessingEvent` [adminorder], `InternalOrderEventConsumer#handleOrderProcessingEvent` [internalorder], `LegacyOrderEventConsumer#handleOrderProcessingEvent` [legacyorder], `OrderEventConsumer#handleOrderProcessingEvent` [order], `PartnerOrderEventConsumer#handleOrderProcessingEvent` [partnerorder], `SettlementOrderEventConsumer#handleOrderProcessingEvent` [settlementorder]
- 구독 kafka `order-processing-topic`: `AdminOrderEventConsumer#handleOrderProcessingEvent` (groupId=order-processing-group containerFactory=orderProcessingKafkaListenerContainerFactory) ← `AdminOrderEventProducer#sendOrderProcessingEvent` [adminorder], `AdminOrderEventProducer#sendOrderProcessingEventSync` [adminorder], `InternalOrderEventProducer#sendOrderProcessingEvent` [internalorder], `InternalOrderEventProducer#sendOrderProcessingEventSync` [internalorder], `LegacyOrderEventProducer#sendOrderProcessingEvent` [legacyorder], `LegacyOrderEventProducer#sendOrderProcessingEventSync` [legacyorder], `OrderEventProducer#sendOrderProcessingEvent` [order], `OrderEventProducer#sendOrderProcessingEventSync` [order], `PartnerOrderEventProducer#sendOrderProcessingEvent` [partnerorder], `PartnerOrderEventProducer#sendOrderProcessingEventSync` [partnerorder], `SettlementOrderEventProducer#sendOrderProcessingEvent` [settlementorder], `SettlementOrderEventProducer#sendOrderProcessingEventSync` [settlementorder]
- 구독 kafka `order-retry-topic`: `AdminOrderEventConsumer#handleOrderRetryEvent` (groupId=order-retry-group containerFactory=orderProcessingKafkaListenerContainerFactory) ← `AdminOrderEventProducer#sendOrderRetryEvent` [adminorder], `InternalOrderEventProducer#sendOrderRetryEvent` [internalorder], `LegacyOrderEventProducer#sendOrderRetryEvent` [legacyorder], `OrderEventProducer#sendOrderRetryEvent` [order], `PartnerOrderEventProducer#sendOrderRetryEvent` [partnerorder], `SettlementOrderEventProducer#sendOrderRetryEvent` [settlementorder]

## 6. AOP

(없음)

