# faq 도메인

기준 경로: `src/main/java/com/fream/back/domain/faq/` · 파일 54개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/order-bids` | `FaqBidCommandController#createOrderBid` | `FaqBidCommandService#createOrderBid` |
| DELETE | `/order-bids/{orderBidId}` | `FaqBidCommandController#deleteOrderBid` | `FaqBidCommandService#deleteOrderBid` |
| POST | `/order-bids/instant` | `FaqBidCommandController#createInstantOrderBid` | `this#enrichPaymentRequest`, `FaqBidCommandService#createInstantOrderBid` |
| POST | `/orders/{orderId}/process-payment-shipment` | `FaqCommandController#processPaymentAndShipment` | `FaqEventProducer#sendOrderProcessingEvent` |
| POST | `/orders/{orderId}/cancel` | `FaqCommandController#cancelOrderProcessing` |  |
| GET | `/order-bids` | `FaqBidQueryController#getOrderBids` | `FaqBidQueryService#getOrderBids` |
| GET | `/order-bids/count` | `FaqBidQueryController#getOrderBidStatusCounts` | `FaqBidQueryService#getOrderBidStatusCounts` |
| GET | `/order-bids/{orderBidId}` | `FaqBidQueryController#getOrderBidDetail` | `FaqBidQueryService#getOrderBidDetail` |
| GET | `/orders/{orderId}/status` | `FaqQueryController#getOrderStatus` | `FaqRepository#findById~`, `this#getStatusDescription`, `this#canCancelOrder` |
| GET | `/orders/{orderId}` | `FaqQueryController#getOrderDetails` | `FaqRepository#findById~`, `this#getStatusDescription` |
| GET | `/orders` | `FaqQueryController#getUserOrders` |  |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `FaqBidCommandService`
  - `createOrderBid` → this#validateCreateOrderBidInput, ProductQueryService#findByEmail, UserSizeQueryService#findById, FaqCommandService#createOrderFromBid, this#createAndSaveOrderBid, this#handleOrderBidCreationException
  - `createAndSaveOrderBid` → FaqBidRepository#save~
  - `createInstantOrderBid` → this#validateInstantOrderBidInput, ProductQueryService#findByEmail, this#getSaleBidAndValidate, FaqCommandService#createInstantOrder, this#handleOrderBidCreationException, FaqBidRepository#save~
  - `getSaleBidAndValidate` → StyleBidQueryService#findById
  - `deleteOrderBid` → FaqBidRepository#findById~, FaqCommandService#deleteOrder, FaqBidRepository#delete~
- `FaqBidQueryService`
  - `findById` → FaqBidRepository#findById~
  - `findByOrderId` → FaqBidRepository#findByOrderId
  - `getOrderBids` → this#validateEmail, FaqBidRepository#findOrderBidsByFilters
  - `getOrderBidStatusCounts` → this#validateEmail, FaqBidRepository#countOrderBidsByStatus
  - `getOrderBidDetail` → this#validateEmail, FaqBidRepository#findOrderBidById
- `FaqCommandService`
  - `createOrderFromBid` → FaqItemCommandService#createOrderItem, FaqRepository#save~
  - `processPaymentAndShipment` → ProductQueryService#findByEmail, FaqRepository#findById~, this#validatePaymentShipmentRequest, NotificationCommandService#processPayment, FaqAddressCommandService#createOrderShipment, NoticeCommandService#createOrderStorage, FaqBidQueryService#findById
  - `createInstantOrder` → ShipmentQueryService#getAddress, FaqRepository#save~, FaqItemCommandService#createOrderItem, FaqAddressCommandService#createOrderShipment, NoticeCommandService#updateWarehouseStatus, NotificationCommandService#processPayment
  - `deleteOrder` → FaqRepository#findById~, FaqRepository#delete~
- `FaqEventConsumer`
  - `handleOrderProcessingEvent` → this#isOrderAlreadyProcessed, FaqRepository#findById~, ProductQueryService#findByEmail, this#processCompleteOrder, this#sendSuccessNotification, this#handleOrderProcessingFailure
  - `handleOrderRetryEvent` → this#sendFailureNotification, this#isOrderAlreadyProcessed, this#handleOrderProcessingEvent
  - `processCompleteOrder` → NotificationCommandService#processPayment, FaqAddressCommandService#createOrderShipment, NoticeCommandService#createOrderStorage, FaqRepository#save~
  - `isOrderAlreadyProcessed` → FaqRepository#findById~
  - `handleOrderProcessingFailure` → this#isRetryableError, FaqEventProducer#sendOrderRetryEvent, this#sendFailureNotification
  - `sendSuccessNotification` → kafka:notification-topic
  - `sendFailureNotification` → this#isRetryableError, kafka:notification-topic
- `FaqEventProducer`
  - `sendOrderProcessingEvent` → kafka:order-processing-topic
  - `sendOrderRetryEvent` → kafka:order-retry-topic
  - `sendOrderProcessingEventSync` → kafka:order-processing-topic
- `FaqItemCommandService`
  - `createOrderItem` → FaqItemRepository#save~
- `FaqItemQueryService`
  - `findById` → FaqItemRepository#findById~
- `FaqProcessingEvent`
  - `create` → this#generateEventId

## 3. 클래스 목록 (역할별)

### controller (4)

- `FaqBidCommandController` controller/command/FaqBidCommandController.java · public: createOrderBid, deleteOrderBid, createInstantOrderBid · 주입: FaqBidCommandService
- `FaqCommandController` controller/command/FaqCommandController.java · public: processPaymentAndShipment, cancelOrderProcessing · 주입: FaqEventProducer
- `FaqBidQueryController` controller/query/FaqBidQueryController.java · public: getOrderBids, getOrderBidStatusCounts, getOrderBidDetail · 주입: FaqBidQueryService
- `FaqQueryController` controller/query/FaqQueryController.java · public: getOrderStatus, getOrderDetails, getUserOrders · 주입: FaqRepository

### service-command (3)

- `FaqBidCommandService` service/command/FaqBidCommandService.java · public: createOrderBid, createInstantOrderBid, matchOrderBid, deleteOrderBid · 주입: FaqBidRepository, FaqCommandService, UserSizeQueryService, ProductQueryService, StyleBidQueryService
- `FaqCommandService` service/command/FaqCommandService.java · public: createOrderFromBid, processPaymentAndShipment, createInstantOrder, deleteOrder · 주입: FaqRepository, NotificationCommandService, FaqAddressCommandService, FaqItemCommandService, FaqBidQueryService, ProductQueryService, NoticeCommandService, ShipmentQueryService, EntityManager
- `FaqItemCommandService` service/command/FaqItemCommandService.java · public: createOrderItem · 주입: FaqItemRepository

### service-query (2)

- `FaqBidQueryService` service/query/FaqBidQueryService.java · public: findById, findByOrderId, getOrderBids, getOrderBidStatusCounts, getOrderBidDetail · 주입: FaqBidRepository
- `FaqItemQueryService` service/query/FaqItemQueryService.java · public: findById · 주입: FaqItemRepository

### repository (3)

- `FaqBidRepository` (interface) repository/FaqBidRepository.java · extends/implements JpaRepository, FaqBidRepositoryCustom · public: findByOrderId
- `FaqItemRepository` (interface) repository/FaqItemRepository.java · extends/implements JpaRepository · public: findByOrder_Id, findByProductSize, findByQuantityGreaterThan, findByOrderIn
- `FaqRepository` (interface) repository/FaqRepository.java · extends/implements JpaRepository · public: findByUser_EmailAndStatus, findByUserAndStatus

### custom (2)

- `FaqBidRepositoryCustom` (interface) repository/FaqBidRepositoryCustom.java · public: findOrderBidsByFilters, countOrderBidsByStatus, findOrderBidById, tradeCountByColorIds
- `FaqBidRepositoryImpl` repository/FaqBidRepositoryImpl.java · extends/implements FaqBidRepositoryCustom · public: findOrderBidsByFilters, countOrderBidsByStatus, findOrderBidById, tradeCountByColorIds · 주입: JPAQueryFactory

### entity (5)

- `BidStatus` (enum) entity/BidStatus.java
- `Faq` entity/Faq.java · extends/implements BaseTimeEntity · public: addOrderItem, assignPayment, assignOrderShipment, assignWarehouseStorage, updateStatus, assignOrderBid
- `FaqBid` entity/FaqBid.java · extends/implements BaseTimeEntity · public: assignOrder, assignSale, markAsInstantPurchase, updateStatus
- `FaqItem` entity/FaqItem.java · extends/implements BaseTimeEntity · public: addOrder
- `FaqStatus` (enum) entity/FaqStatus.java · public: canTransitionTo, getAllowedNextStates

### event (3)

- `FaqProcessingEvent` dto/kafka/FaqProcessingEvent.java · public: create, withRetry, withStep, isMaxRetryExceeded
- `FaqEventConsumer` service/kafka/FaqEventConsumer.java · public: handleOrderProcessingEvent, handleOrderRetryEvent · 주입: FaqRepository, NotificationCommandService, FaqAddressCommandService, NoticeCommandService, ProductQueryService, FaqEventProducer, SimpMessagingTemplate, KafkaTemplate
- `FaqEventProducer` service/kafka/FaqEventProducer.java · public: sendOrderProcessingEvent, sendOrderRetryEvent, sendOrderProcessingEventSync · 주입: KafkaTemplate

### dto (5)

- `FaqBidRequestDto` dto/FaqBidRequestDto.java
- `FaqBidResponseDto` dto/FaqBidResponseDto.java
- `FaqBidStatusCountDto` dto/FaqBidStatusCountDto.java
- `InstantFaqRequestDto` dto/InstantFaqRequestDto.java
- `PayAndAddressRequestDto` dto/PayAndAddressRequestDto.java

### exception (27)

- `FaqAccessDeniedException` exception/FaqAccessDeniedException.java
- `FaqAddressProcessingFailedException` exception/FaqAddressProcessingFailedException.java
- `FaqBidAccessDeniedException` exception/FaqBidAccessDeniedException.java
- `FaqBidAlreadyMatchedException` exception/FaqBidAlreadyMatchedException.java
- `FaqBidCreationFailedException` exception/FaqBidCreationFailedException.java
- `FaqBidDeletionFailedException` exception/FaqBidDeletionFailedException.java
- `FaqBidMatchingFailedException` exception/FaqBidMatchingFailedException.java
- `FaqBidNotFoundException` exception/FaqBidNotFoundException.java
- `FaqCreationFailedException` exception/FaqCreationFailedException.java
- `FaqDeletionFailedException` exception/FaqDeletionFailedException.java
- `FaqErrorCode` exception/FaqErrorCode.java
- `FaqException` exception/FaqException.java
- `FaqItemCreationFailedException` exception/FaqItemCreationFailedException.java
- `FaqItemNotFoundException` exception/FaqItemNotFoundException.java
- `FaqNotFoundException` exception/FaqNotFoundException.java
- `FaqNoticeProcessingFailedException` exception/FaqNoticeProcessingFailedException.java
- `FaqNotificationProcessingFailedException` exception/FaqNotificationProcessingFailedException.java
- `FaqStatusUpdateFailedException` exception/FaqStatusUpdateFailedException.java
- `InvalidBidPriceException` exception/InvalidBidPriceException.java
- `InvalidFaqBidDataException` exception/InvalidFaqBidDataException.java
- `InvalidFaqBidException` exception/InvalidFaqBidException.java
- `InvalidFaqBidStatusException` exception/InvalidFaqBidStatusException.java
- `InvalidFaqDataException` exception/InvalidFaqDataException.java
- `InvalidFaqStatusException` exception/InvalidFaqStatusException.java
- `InvalidNotificationAddressDataException` exception/InvalidNotificationAddressDataException.java
- `StyleBidNotFoundException` exception/StyleBidNotFoundException.java
- `UserSizeNotFoundException` exception/UserSizeNotFoundException.java

### 다른 도메인 참조 (import·주입 기준)

- **address**: `FaqAddress` ← Faq, FaqCommandService, FaqEventConsumer; `FaqAddressCommandService` ← FaqCommandService, FaqEventConsumer
- **notice**: `Notice` ← Faq, FaqCommandService, FaqEventConsumer; `NoticeCommandService` ← FaqCommandService, FaqEventConsumer; `WarehouseStatus` ← FaqCommandService
- **notification**: `Notification` ← Faq, FaqCommandService, FaqEventConsumer; `NotificationCommandService` ← FaqCommandService, FaqEventConsumer
- **product**: `Product` ← Faq, FaqBid, FaqBidCommandService, FaqCommandService, FaqEventConsumer, FaqRepository; `ProductQueryService` ← FaqBidCommandService, FaqCommandService, FaqEventConsumer
- **shipment**: `ShipmentQueryService` ← FaqCommandService
- **style**: `Style` ← FaqBid, FaqBidCommandService, FaqCommandService; `StyleBid` ← FaqBidCommandService, FaqCommandService; `StyleBidQueryService` ← FaqBidCommandService
- **user**: `UserSize` ← FaqBid, FaqBidCommandService, FaqCommandService, FaqItem, FaqItemCommandService, FaqItemRepository; `UserSizeQueryService` ← FaqBidCommandService

### 이 도메인을 참조하는 다른 도메인

- **address**: Faq, FaqBid, FaqBidQueryService, FaqStatus
- **global**: FaqBidRepository, FaqItemRepository, FaqProcessingEvent, FaqRepository
- **notice**: Faq
- **notification**: Faq, FaqBid, FaqRepository
- **payment**: Faq
- **sale**: FaqItem, FaqItemQueryService
- **style**: BidStatus, Faq, FaqBid, FaqBidQueryService
- **user**: BidStatus, FaqBidRepository

## 4. JPA 엔티티 연관

- `Faq` @Entity @Table(name = "orders") · extends BaseTimeEntity
  - user: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") → `Product` [product]
  - payment: @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true) → `Notification` [notification]
  - orderShipment: @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true) → `FaqAddress` [address]
  - warehouseStorage: @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true) → `Notice` [notice]
  - orderItems: @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true) → `FaqItem`
  - orderBid: @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true) → `FaqBid`
- `FaqBid` @Entity · extends BaseTimeEntity
  - user: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") → `Product` [product]
  - productSize: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_size_id") → `UserSize` [user]
  - order: @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "order_id") → `Faq`
  - sale: @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "sale_id") → `Style` [style]
- `FaqItem` @Entity · extends BaseTimeEntity
  - order: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "order_id") → `Faq`
  - productSize: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_size_id") → `UserSize` [user]

다른 도메인 엔티티에서 들어오는 연관:

- `FaqAddress.order` @OneToOne → `Faq` [address]
- `Notice.order` @OneToOne → `Faq` [notice]
- `Notification.order` @OneToOne → `Faq` [notification]
- `SaleFaqItem.orderItem` @ManyToOne → `FaqItem` [sale]
- `StyleBid.order` @OneToOne → `Faq` [style]

## 5. 이벤트·메시징

- 발행 kafka `notification-topic`: `FaqEventConsumer#sendSuccessNotification` → (소스 내 구독 없음)
- 발행 kafka `notification-topic`: `FaqEventConsumer#sendFailureNotification` → (소스 내 구독 없음)
- 발행 kafka `order-processing-topic`: `FaqEventProducer#sendOrderProcessingEvent` → `FaqEventConsumer#handleOrderProcessingEvent` [faq]
- 발행 kafka `order-retry-topic`: `FaqEventProducer#sendOrderRetryEvent` → `FaqEventConsumer#handleOrderRetryEvent` [faq]
- 발행 kafka `order-processing-topic`: `FaqEventProducer#sendOrderProcessingEventSync` → `FaqEventConsumer#handleOrderProcessingEvent` [faq]
- 구독 kafka `order-processing-topic`: `FaqEventConsumer#handleOrderProcessingEvent` (groupId=order-processing-group containerFactory=orderProcessingKafkaListenerContainerFactory) ← `FaqEventProducer#sendOrderProcessingEvent` [faq], `FaqEventProducer#sendOrderProcessingEventSync` [faq]
- 구독 kafka `order-retry-topic`: `FaqEventConsumer#handleOrderRetryEvent` (groupId=order-retry-group containerFactory=orderProcessingKafkaListenerContainerFactory) ← `FaqEventProducer#sendOrderRetryEvent` [faq]

## 6. AOP

(없음)

