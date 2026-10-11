# notification 도메인

기준 경로: `src/main/java/com/fream/back/domain/notification/` · 파일 39개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/payment-info` | `NotificationInfoController#createPaymentInfo` | `this#extractEmailFromSecurityContext`, `NotificationInfoCommandService#createPaymentInfo` |
| DELETE | `/payment-info/{id}` | `NotificationInfoController#deletePaymentInfo` | `this#extractEmailFromSecurityContext`, `NotificationInfoCommandService#deletePaymentInfo` |
| GET | `/payment-info` | `NotificationInfoController#getPaymentInfos` | `this#extractEmailFromSecurityContext`, `NotificationInfoQueryService#getPaymentInfos` |
| GET | `/payment-info/{id}` | `NotificationInfoController#getPaymentInfo` | `this#extractEmailFromSecurityContext`, `NotificationInfoQueryService#getPaymentInfo` |
| POST | `/payment-info/test-payment` | `NotificationInfoController#testPayment` | `PortOneApiClient#requestTestPayment`, `PortOneApiClient#cancelTestPayment` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `NotificationCardUtils`
  - `validateCardInfo` → this#validateExpirationDate
  - `maskCardNumberForLogging` → this#maskCardNumber
- `NotificationCommandService`
  - `processPayment` → this#validatePaymentRequest, this#createCardPayment, this#createAccountPayment, this#createGeneralPayment, this#notifyUsersAndUpdateSaleStatus
  - `createCardPayment` → NotificationInfoQueryService#getPaymentInfoEntity, PortOneApiClient#processCardPayment, NotificationRepository#save~
  - `createAccountPayment` → this#validateBank, NotificationRepository#save~
  - `createGeneralPayment` → NotificationRepository#save~
  - `handleRefundIfNecessary` → PortOneApiClient#cancelPayment, NotificationRepository#save~
  - `notifyUsersAndUpdateSaleStatus` → StyleBidQueryService#findByOrderId, this#sendNotifications
  - `sendNotifications` → PaymentCommandService#createNotification
  - `updatePaymentStatus` → NotificationRepository#save~
  - `refundPayment` → NotificationRepository#findById~, PortOneApiClient#cancelPayment, NotificationRepository#save~, this#sendRefundNotification
  - `sendRefundNotification` → PaymentCommandService#createNotification, StyleBidQueryService#findByOrderId
- `NotificationCompletedEvent`
  - `create` → this#generateEventId
- `NotificationEncryptionService`
  - `encrypt` → this#generateKey
  - `decrypt` → this#generateKey
- `NotificationEvent`
  - `create` → this#generateEventId
- `NotificationEventConsumer`
  - `handlePaymentEvent` → this#isPaymentAlreadyProcessed, FaqRepository#findById~, ProductQueryService#findByEmail, NotificationCommandService#processPayment, this#handlePaymentFailure
  - `handlePaymentRetryEvent` → this#isPaymentAlreadyProcessed, this#handlePaymentEvent
  - `isPaymentAlreadyProcessed` → NotificationRepository#findByOrder_Id
  - `handlePaymentFailure` → this#isRetryableError, NotificationEventProducer#sendPaymentRetryEvent
- `NotificationEventProducer`
  - `sendPaymentEvent` → kafka:payment-processing-topic
  - `sendPaymentRetryEvent` → kafka:payment-retry-topic
  - `sendPaymentEventSync` → kafka:payment-processing-topic
- `NotificationInfoCommandService`
  - `createPaymentInfo` → ProductRepository#findByEmail, this#validateCardInfo, PortOneApiClient#requestTestPayment, PortOneApiClient#cancelTestPayment, this#maskCardNumber
  - `deletePaymentInfo` → ProductRepository#findByEmail
- `NotificationInfoQueryService`
  - `getPaymentInfos` → NotificationInfoRepository#findAllByUser_Email
  - `getPaymentInfo` → NotificationInfoRepository#findByIdAndUser_Email, this#convertToDto
  - `getPaymentInfoEntity` → NotificationInfoRepository#findByIdAndUser_Email, NotificationEncryptionService#decrypt
  - `convertToDto` → NotificationEncryptionService#decrypt
- `NotificationQueryService`
  - `getPaymentDetails` → NotificationRepository#findById~, this#convertToDto
  - `getUserPayments` → NotificationRepository#findByOrder_User_EmailAndIsSuccess
  - `getSellerPayments` → NotificationRepository#findBySale_Seller_EmailAndIsSuccess
  - `findByImpUid` → NotificationRepository#findByImpUid
- `PortOneApiClient`
  - `requestTestPayment` → this#getAccessToken
  - `processCardPayment` → this#validatePaymentInfo, this#getAccessToken
  - `refundPayment` → this#getAccessToken
  - `cancelTestPayment` → this#refundPayment
  - `cancelPayment` → this#refundPayment

## 3. 클래스 목록 (역할별)

### controller (1)

- `NotificationInfoController` controller/command/NotificationInfoController.java · public: createPaymentInfo, deletePaymentInfo, getPaymentInfos, getPaymentInfo, testPayment · 주입: NotificationInfoCommandService, NotificationInfoQueryService, PortOneApiClient

### service-command (2)

- `NotificationCommandService` service/command/NotificationCommandService.java · public: processPayment, updatePaymentStatus, refundPayment · 주입: NotificationRepository, NotificationInfoQueryService, PortOneApiClient, PaymentCommandService, StyleBidQueryService
- `NotificationInfoCommandService` service/command/NotificationInfoCommandService.java · public: createPaymentInfo, deletePaymentInfo · 주입: ProductRepository, PortOneApiClient

### service-query (2)

- `NotificationInfoQueryService` service/query/NotificationInfoQueryService.java · public: getPaymentInfos, getPaymentInfo, getPaymentInfoEntity · 주입: NotificationInfoRepository, NotificationEncryptionService
- `NotificationQueryService` service/query/NotificationQueryService.java · public: getPaymentDetails, getUserPayments, getSellerPayments, findByImpUid · 주입: NotificationRepository

### service (2)

- `PortOneApiClient` portone/PortOneApiClient.java · public: getAccessToken, requestTestPayment, processCardPayment, refundPayment, cancelTestPayment, cancelPayment · 주입: RestTemplate
- `NotificationEncryptionService` service/NotificationEncryptionService.java · public: encrypt, decrypt

### repository (2)

- `NotificationInfoRepository` (interface) repository/NotificationInfoRepository.java · extends/implements JpaRepository · public: findAllByUser_Email, findByIdAndUser_Email
- `NotificationRepository` (interface) repository/NotificationRepository.java · extends/implements JpaRepository · public: findByOrder_User_EmailAndIsSuccess, findBySale_Seller_EmailAndIsSuccess, findByImpUid, findByOrder_Id, existsSuccessfulPaymentByOrderId, findByOrder_IdAndStatus, findProcessingPaymentsByOrderId, findPaymentsCreatedSince

### entity (7)

- `AccountNotification` entity/AccountNotification.java · extends/implements Notification · public: getImpUid
- `Bank` (enum) entity/Bank.java · public: getDisplayName
- `CardNotification` entity/CardNotification.java · extends/implements Notification · public: getImpUid
- `GeneralNotification` entity/GeneralNotification.java · extends/implements Notification · public: getImpUid
- `Notification` (abstract class) entity/Notification.java · extends/implements BaseTimeEntity · public: assignOrder, assignSale, assignUser, setPaidAmount, updateSuccessStatus, updateStatus, getImpUid
- `NotificationInfo` entity/NotificationInfo.java · public: assignUser, unassignUser
- `NotificationStatus` (enum) entity/NotificationStatus.java · public: canTransitionTo

### event (4)

- `NotificationCompletedEvent` dto/kafka/NotificationCompletedEvent.java · public: create, isPaymentSuccess, isPaymentFailed
- `NotificationEvent` dto/kafka/NotificationEvent.java · public: create, withRetry, isMaxRetryExceeded
- `NotificationEventConsumer` service/kafka/NotificationEventConsumer.java · public: handlePaymentEvent, handlePaymentRetryEvent · 주입: NotificationCommandService, NotificationEventProducer, FaqRepository, NotificationRepository, ProductQueryService
- `NotificationEventProducer` service/kafka/NotificationEventProducer.java · public: sendPaymentEvent, sendPaymentRetryEvent, sendPaymentEventSync · 주입: KafkaTemplate

### dto (10)

- `AccountNotificationDto` dto/AccountNotificationDto.java
- `AccountNotificationRequestDto` dto/AccountNotificationRequestDto.java
- `CardNotificationDto` dto/CardNotificationDto.java
- `CardNotificationRequestDto` dto/CardNotificationRequestDto.java
- `GeneralNotificationDto` dto/GeneralNotificationDto.java
- `GeneralNotificationRequestDto` dto/GeneralNotificationRequestDto.java
- `NotificationDto` dto/NotificationDto.java
- `NotificationRequestDto` dto/NotificationRequestDto.java
- `NotificationInfoCreateDto` dto/paymentInfo/NotificationInfoCreateDto.java
- `NotificationInfoDto` dto/paymentInfo/NotificationInfoDto.java

### exception (8)

- `NotificationAccessDeniedException` exception/NotificationAccessDeniedException.java
- `NotificationApiException` exception/NotificationApiException.java
- `NotificationErrorCode` exception/NotificationErrorCode.java
- `NotificationException` exception/NotificationException.java
- `NotificationInfoNotFoundException` exception/NotificationInfoNotFoundException.java
- `NotificationNotFoundException` exception/NotificationNotFoundException.java
- `NotificationProcessingException` exception/NotificationProcessingException.java
- `UnknownNotificationTypeException` exception/UnknownNotificationTypeException.java

### other (1)

- `NotificationCardUtils` util/NotificationCardUtils.java · public: validateCardInfo, maskCardNumber, maskCardPassword, maskBirthDate, maskCardNumberForLogging

### 다른 도메인 참조 (import·주입 기준)

- **faq**: `Faq` ← Notification, NotificationCommandService, NotificationEventConsumer; `FaqBid` ← NotificationCommandService; `FaqRepository` ← NotificationEventConsumer
- **payment**: `PaymentCategory` ← NotificationCommandService; `PaymentCommandService` ← NotificationCommandService; `PaymentType` ← NotificationCommandService
- **product**: `Product` ← Notification, NotificationCommandService, NotificationEventConsumer, NotificationInfo, NotificationInfoCommandService; `ProductQueryService` ← NotificationEventConsumer; `ProductRepository` ← NotificationInfoCommandService
- **style**: `Style` ← Notification, NotificationCommandService; `StyleBid` ← NotificationCommandService; `StyleBidQueryService` ← NotificationCommandService; `StyleStatus` ← NotificationCommandService

### 이 도메인을 참조하는 다른 도메인

- **faq**: Notification, NotificationCommandService
- **global**: GeneralNotification, NotificationCommandService, NotificationEvent, NotificationRepository, NotificationStatus
- **product**: NotificationInfo
- **style**: Notification

## 4. JPA 엔티티 연관

- `AccountNotification` @Entity @DiscriminatorValue("ACCOUNT") · extends Notification
- `CardNotification` @Entity @DiscriminatorValue("CARD") · extends Notification
- `GeneralNotification` @Entity @DiscriminatorValue("GENERAL") · extends Notification
- `Notification` @Entity @Inheritance(strategy = InheritanceType.SINGLE_TABLE) @DiscriminatorColumn(name = "payment_type") · extends BaseTimeEntity
  - order: @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "order_id") → `Faq` [faq]
  - sale: @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "sale_id") → `Style` [style]
  - user: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) → `Product` [product]
- `NotificationInfo` @Entity
  - user: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") → `Product` [product]

다른 도메인 엔티티에서 들어오는 연관:

- `Faq.payment` @OneToOne → `Notification` [faq]
- `Product.paymentInfos` @OneToMany → `NotificationInfo` [product]
- `Style.payment` @OneToOne → `Notification` [style]

## 5. 이벤트·메시징

- 발행 kafka `payment-processing-topic`: `NotificationEventProducer#sendPaymentEvent` → `NotificationEventConsumer#handlePaymentEvent` [notification]
- 발행 kafka `payment-retry-topic`: `NotificationEventProducer#sendPaymentRetryEvent` → `NotificationEventConsumer#handlePaymentRetryEvent` [notification]
- 발행 kafka `payment-processing-topic`: `NotificationEventProducer#sendPaymentEventSync` → `NotificationEventConsumer#handlePaymentEvent` [notification]
- 구독 kafka `payment-processing-topic`: `NotificationEventConsumer#handlePaymentEvent` (groupId=payment-processing-group containerFactory=paymentEventKafkaListenerContainerFactory) ← `NotificationEventProducer#sendPaymentEvent` [notification], `NotificationEventProducer#sendPaymentEventSync` [notification]
- 구독 kafka `payment-retry-topic`: `NotificationEventConsumer#handlePaymentRetryEvent` (groupId=payment-retry-group containerFactory=paymentEventKafkaListenerContainerFactory) ← `NotificationEventProducer#sendPaymentRetryEvent` [notification]

## 6. AOP

(없음)

