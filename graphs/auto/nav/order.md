# order 도메인

기준 경로: `src/main/java/com/fream/back/domain/order/` · 파일 54개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/order-bids` | `OrderBidCommandController#createOrderBid` | `OrderBidCommandService#createOrderBid` |
| DELETE | `/order-bids/{orderBidId}` | `OrderBidCommandController#deleteOrderBid` | `OrderBidCommandService#deleteOrderBid` |
| POST | `/order-bids/instant` | `OrderBidCommandController#createInstantOrderBid` | `this#enrichPaymentRequest`, `OrderBidCommandService#createInstantOrderBid` |
| POST | `/orders/{orderId}/process-payment-shipment` | `OrderCommandController#processPaymentAndShipment` | `OrderEventProducer#sendOrderProcessingEvent` |
| POST | `/orders/{orderId}/cancel` | `OrderCommandController#cancelOrderProcessing` |  |
| GET | `/order-bids` | `OrderBidQueryController#getOrderBids` | `OrderBidQueryService#getOrderBids` |
| GET | `/order-bids/count` | `OrderBidQueryController#getOrderBidStatusCounts` | `OrderBidQueryService#getOrderBidStatusCounts` |
| GET | `/order-bids/{orderBidId}` | `OrderBidQueryController#getOrderBidDetail` | `OrderBidQueryService#getOrderBidDetail` |
| GET | `/orders/{orderId}/status` | `OrderQueryController#getOrderStatus` | `OrderRepository#findById~`, `this#getStatusDescription`, `this#canCancelOrder` |
| GET | `/orders/{orderId}` | `OrderQueryController#getOrderDetails` | `OrderRepository#findById~`, `this#getStatusDescription` |
| GET | `/orders` | `OrderQueryController#getUserOrders` |  |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `OrderBidCommandService`
  - `createOrderBid` → this#validateCreateOrderBidInput, UserQueryService#findByEmail, ProductSizeQueryService#findById, OrderCommandService#createOrderFromBid, this#createAndSaveOrderBid, this#handleOrderBidCreationException
  - `createAndSaveOrderBid` → OrderBidRepository#save~
  - `createInstantOrderBid` → this#validateInstantOrderBidInput, UserQueryService#findByEmail, this#getSaleBidAndValidate, OrderCommandService#createInstantOrder, this#handleOrderBidCreationException, OrderBidRepository#save~
  - `getSaleBidAndValidate` → SaleBidQueryService#findById
  - `deleteOrderBid` → OrderBidRepository#findById~, OrderCommandService#deleteOrder, OrderBidRepository#delete~
- `OrderBidQueryService`
  - `findById` → OrderBidRepository#findById~
  - `findByOrderId` → OrderBidRepository#findByOrderId
  - `getOrderBids` → this#validateEmail, OrderBidRepository#findOrderBidsByFilters
  - `getOrderBidStatusCounts` → this#validateEmail, OrderBidRepository#countOrderBidsByStatus
  - `getOrderBidDetail` → this#validateEmail, OrderBidRepository#findOrderBidById
- `OrderCommandService`
  - `createOrderFromBid` → OrderItemCommandService#createOrderItem, OrderRepository#save~
  - `processPaymentAndShipment` → UserQueryService#findByEmail, OrderRepository#findById~, this#validatePaymentShipmentRequest, PaymentCommandService#processPayment, OrderShipmentCommandService#createOrderShipment, WarehouseStorageCommandService#createOrderStorage, OrderBidQueryService#findById
  - `createInstantOrder` → AddressQueryService#getAddress, OrderRepository#save~, OrderItemCommandService#createOrderItem, OrderShipmentCommandService#createOrderShipment, WarehouseStorageCommandService#updateWarehouseStatus, PaymentCommandService#processPayment
  - `deleteOrder` → OrderRepository#findById~, OrderRepository#delete~
- `OrderEventConsumer`
  - `handleOrderProcessingEvent` → this#isOrderAlreadyProcessed, OrderRepository#findById~, UserQueryService#findByEmail, this#processCompleteOrder, this#sendSuccessNotification, this#handleOrderProcessingFailure
  - `handleOrderRetryEvent` → this#sendFailureNotification, this#isOrderAlreadyProcessed, this#handleOrderProcessingEvent
  - `processCompleteOrder` → PaymentCommandService#processPayment, OrderShipmentCommandService#createOrderShipment, WarehouseStorageCommandService#createOrderStorage, OrderRepository#save~
  - `isOrderAlreadyProcessed` → OrderRepository#findById~
  - `handleOrderProcessingFailure` → this#isRetryableError, OrderEventProducer#sendOrderRetryEvent, this#sendFailureNotification
  - `sendSuccessNotification` → kafka:notification-topic
  - `sendFailureNotification` → this#isRetryableError, kafka:notification-topic
- `OrderEventProducer`
  - `sendOrderProcessingEvent` → kafka:order-processing-topic
  - `sendOrderRetryEvent` → kafka:order-retry-topic
  - `sendOrderProcessingEventSync` → kafka:order-processing-topic
- `OrderItemCommandService`
  - `createOrderItem` → OrderItemRepository#save~
- `OrderItemQueryService`
  - `findById` → OrderItemRepository#findById~
- `OrderProcessingEvent`
  - `create` → this#generateEventId

## 3. 클래스 목록 (역할별)

### controller (4)

- `OrderBidCommandController` controller/command/OrderBidCommandController.java · public: createOrderBid, deleteOrderBid, createInstantOrderBid · 주입: OrderBidCommandService
- `OrderCommandController` controller/command/OrderCommandController.java · public: processPaymentAndShipment, cancelOrderProcessing · 주입: OrderEventProducer
- `OrderBidQueryController` controller/query/OrderBidQueryController.java · public: getOrderBids, getOrderBidStatusCounts, getOrderBidDetail · 주입: OrderBidQueryService
- `OrderQueryController` controller/query/OrderQueryController.java · public: getOrderStatus, getOrderDetails, getUserOrders · 주입: OrderRepository

### service-command (3)

- `OrderBidCommandService` service/command/OrderBidCommandService.java · public: createOrderBid, createInstantOrderBid, matchOrderBid, deleteOrderBid · 주입: OrderBidRepository, OrderCommandService, ProductSizeQueryService, UserQueryService, SaleBidQueryService
- `OrderCommandService` service/command/OrderCommandService.java · public: createOrderFromBid, processPaymentAndShipment, createInstantOrder, deleteOrder · 주입: OrderRepository, PaymentCommandService, OrderShipmentCommandService, OrderItemCommandService, OrderBidQueryService, UserQueryService, WarehouseStorageCommandService, AddressQueryService, EntityManager
- `OrderItemCommandService` service/command/OrderItemCommandService.java · public: createOrderItem · 주입: OrderItemRepository

### service-query (2)

- `OrderBidQueryService` service/query/OrderBidQueryService.java · public: findById, findByOrderId, getOrderBids, getOrderBidStatusCounts, getOrderBidDetail · 주입: OrderBidRepository
- `OrderItemQueryService` service/query/OrderItemQueryService.java · public: findById · 주입: OrderItemRepository

### repository (3)

- `OrderBidRepository` (interface) repository/OrderBidRepository.java · extends/implements JpaRepository, OrderBidRepositoryCustom · public: findByOrderId
- `OrderItemRepository` (interface) repository/OrderItemRepository.java · extends/implements JpaRepository · public: findByOrder_Id, findByProductSize, findByQuantityGreaterThan, findByOrderIn
- `OrderRepository` (interface) repository/OrderRepository.java · extends/implements JpaRepository · public: findByUser_EmailAndStatus, findByUserAndStatus

### custom (2)

- `OrderBidRepositoryCustom` (interface) repository/OrderBidRepositoryCustom.java · public: findOrderBidsByFilters, countOrderBidsByStatus, findOrderBidById, tradeCountByColorIds
- `OrderBidRepositoryImpl` repository/OrderBidRepositoryImpl.java · extends/implements OrderBidRepositoryCustom · public: findOrderBidsByFilters, countOrderBidsByStatus, findOrderBidById, tradeCountByColorIds · 주입: JPAQueryFactory

### entity (5)

- `BidStatus` (enum) entity/BidStatus.java
- `Order` entity/Order.java · extends/implements BaseTimeEntity · public: addOrderItem, assignPayment, assignOrderShipment, assignWarehouseStorage, updateStatus, assignOrderBid
- `OrderBid` entity/OrderBid.java · extends/implements BaseTimeEntity · public: assignOrder, assignSale, markAsInstantPurchase, updateStatus
- `OrderItem` entity/OrderItem.java · extends/implements BaseTimeEntity · public: addOrder
- `OrderStatus` (enum) entity/OrderStatus.java · public: canTransitionTo, getAllowedNextStates

### event (3)

- `OrderProcessingEvent` dto/kafka/OrderProcessingEvent.java · public: create, withRetry, withStep, isMaxRetryExceeded
- `OrderEventConsumer` service/kafka/OrderEventConsumer.java · public: handleOrderProcessingEvent, handleOrderRetryEvent · 주입: OrderRepository, PaymentCommandService, OrderShipmentCommandService, WarehouseStorageCommandService, UserQueryService, OrderEventProducer, SimpMessagingTemplate, KafkaTemplate
- `OrderEventProducer` service/kafka/OrderEventProducer.java · public: sendOrderProcessingEvent, sendOrderRetryEvent, sendOrderProcessingEventSync · 주입: KafkaTemplate

### dto (5)

- `InstantOrderRequestDto` dto/InstantOrderRequestDto.java
- `OrderBidRequestDto` dto/OrderBidRequestDto.java
- `OrderBidResponseDto` dto/OrderBidResponseDto.java
- `OrderBidStatusCountDto` dto/OrderBidStatusCountDto.java
- `PayAndShipmentRequestDto` dto/PayAndShipmentRequestDto.java

### exception (27)

- `InvalidBidPriceException` exception/InvalidBidPriceException.java
- `InvalidOrderBidDataException` exception/InvalidOrderBidDataException.java
- `InvalidOrderBidException` exception/InvalidOrderBidException.java
- `InvalidOrderBidStatusException` exception/InvalidOrderBidStatusException.java
- `InvalidOrderDataException` exception/InvalidOrderDataException.java
- `InvalidOrderStatusException` exception/InvalidOrderStatusException.java
- `InvalidPaymentShipmentDataException` exception/InvalidPaymentShipmentDataException.java
- `OrderAccessDeniedException` exception/OrderAccessDeniedException.java
- `OrderBidAccessDeniedException` exception/OrderBidAccessDeniedException.java
- `OrderBidAlreadyMatchedException` exception/OrderBidAlreadyMatchedException.java
- `OrderBidCreationFailedException` exception/OrderBidCreationFailedException.java
- `OrderBidDeletionFailedException` exception/OrderBidDeletionFailedException.java
- `OrderBidMatchingFailedException` exception/OrderBidMatchingFailedException.java
- `OrderBidNotFoundException` exception/OrderBidNotFoundException.java
- `OrderCreationFailedException` exception/OrderCreationFailedException.java
- `OrderDeletionFailedException` exception/OrderDeletionFailedException.java
- `OrderErrorCode` exception/OrderErrorCode.java
- `OrderException` exception/OrderException.java
- `OrderItemCreationFailedException` exception/OrderItemCreationFailedException.java
- `OrderItemNotFoundException` exception/OrderItemNotFoundException.java
- `OrderNotFoundException` exception/OrderNotFoundException.java
- `OrderPaymentProcessingFailedException` exception/OrderPaymentProcessingFailedException.java
- `OrderShipmentProcessingFailedException` exception/OrderShipmentProcessingFailedException.java
- `OrderStatusUpdateFailedException` exception/OrderStatusUpdateFailedException.java
- `OrderWarehouseStorageProcessingFailedException` exception/OrderWarehouseStorageProcessingFailedException.java
- `ProductSizeNotFoundException` exception/ProductSizeNotFoundException.java
- `SaleBidNotFoundException` exception/SaleBidNotFoundException.java

### 다른 도메인 참조 (import·주입 기준)

- **address**: `AddressQueryService` ← OrderCommandService
- **payment**: `Payment` ← Order, OrderCommandService, OrderEventConsumer; `PaymentCommandService` ← OrderCommandService, OrderEventConsumer
- **product**: `ProductSize` ← OrderBid, OrderBidCommandService, OrderCommandService, OrderItem, OrderItemCommandService, OrderItemRepository; `ProductSizeQueryService` ← OrderBidCommandService
- **sale**: `Sale` ← OrderBid, OrderBidCommandService, OrderCommandService; `SaleBid` ← OrderBidCommandService, OrderCommandService; `SaleBidQueryService` ← OrderBidCommandService
- **shipment**: `OrderShipment` ← Order, OrderCommandService, OrderEventConsumer; `OrderShipmentCommandService` ← OrderCommandService, OrderEventConsumer
- **user**: `User` ← Order, OrderBid, OrderBidCommandService, OrderCommandService, OrderEventConsumer, OrderRepository; `UserQueryService` ← OrderBidCommandService, OrderCommandService, OrderEventConsumer
- **warehouseStorage**: `WarehouseStatus` ← OrderCommandService; `WarehouseStorage` ← Order, OrderCommandService, OrderEventConsumer; `WarehouseStorageCommandService` ← OrderCommandService, OrderEventConsumer

### 이 도메인을 참조하는 다른 도메인

- **global**: OrderBidRepository, OrderItemRepository, OrderProcessingEvent, OrderRepository
- **notification**: Order
- **payment**: Order, OrderBid, OrderRepository
- **product**: BidStatus, OrderBidRepository
- **sale**: BidStatus, Order, OrderBid, OrderBidQueryService
- **shipment**: Order, OrderBid, OrderBidQueryService, OrderStatus
- **style**: OrderItem, OrderItemQueryService
- **warehouseStorage**: Order

## 4. JPA 엔티티 연관

- `Order` @Entity @Table(name = "orders") · extends BaseTimeEntity
  - user: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") → `User` [user]
  - payment: @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true) → `Payment` [payment]
  - orderShipment: @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true) → `OrderShipment` [shipment]
  - warehouseStorage: @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true) → `WarehouseStorage` [warehouseStorage]
  - orderItems: @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true) → `OrderItem`
  - orderBid: @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true) → `OrderBid`
- `OrderBid` @Entity · extends BaseTimeEntity
  - user: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") → `User` [user]
  - productSize: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_size_id") → `ProductSize` [product]
  - order: @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "order_id") → `Order`
  - sale: @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "sale_id") → `Sale` [sale]
- `OrderItem` @Entity · extends BaseTimeEntity
  - order: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "order_id") → `Order`
  - productSize: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_size_id") → `ProductSize` [product]

다른 도메인 엔티티에서 들어오는 연관:

- `Payment.order` @OneToOne → `Order` [payment]
- `SaleBid.order` @OneToOne → `Order` [sale]
- `OrderShipment.order` @OneToOne → `Order` [shipment]
- `StyleOrderItem.orderItem` @ManyToOne → `OrderItem` [style]
- `WarehouseStorage.order` @OneToOne → `Order` [warehouseStorage]

## 5. 이벤트·메시징

- 발행 kafka `notification-topic`: `OrderEventConsumer#sendSuccessNotification` → (소스 내 구독 없음)
- 발행 kafka `notification-topic`: `OrderEventConsumer#sendFailureNotification` → (소스 내 구독 없음)
- 발행 kafka `order-processing-topic`: `OrderEventProducer#sendOrderProcessingEvent` → `OrderEventConsumer#handleOrderProcessingEvent` [order]
- 발행 kafka `order-retry-topic`: `OrderEventProducer#sendOrderRetryEvent` → `OrderEventConsumer#handleOrderRetryEvent` [order]
- 발행 kafka `order-processing-topic`: `OrderEventProducer#sendOrderProcessingEventSync` → `OrderEventConsumer#handleOrderProcessingEvent` [order]
- 구독 kafka `order-processing-topic`: `OrderEventConsumer#handleOrderProcessingEvent` (groupId=order-processing-group containerFactory=orderProcessingKafkaListenerContainerFactory) ← `OrderEventProducer#sendOrderProcessingEvent` [order], `OrderEventProducer#sendOrderProcessingEventSync` [order]
- 구독 kafka `order-retry-topic`: `OrderEventConsumer#handleOrderRetryEvent` (groupId=order-retry-group containerFactory=orderProcessingKafkaListenerContainerFactory) ← `OrderEventProducer#sendOrderRetryEvent` [order]

## 6. AOP

(없음)

