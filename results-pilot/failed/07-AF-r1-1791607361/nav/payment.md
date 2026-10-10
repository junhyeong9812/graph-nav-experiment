# payment 도메인

기준 경로: `src/main/java/com/fream/back/domain/payment/` · 파일 39개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/payment-info` | `PaymentInfoController#createPaymentInfo` | `this#extractEmailFromSecurityContext`, `PaymentInfoCommandService#createPaymentInfo` |
| DELETE | `/payment-info/{id}` | `PaymentInfoController#deletePaymentInfo` | `this#extractEmailFromSecurityContext`, `PaymentInfoCommandService#deletePaymentInfo` |
| GET | `/payment-info` | `PaymentInfoController#getPaymentInfos` | `this#extractEmailFromSecurityContext`, `PaymentInfoQueryService#getPaymentInfos` |
| GET | `/payment-info/{id}` | `PaymentInfoController#getPaymentInfo` | `this#extractEmailFromSecurityContext`, `PaymentInfoQueryService#getPaymentInfo` |
| POST | `/payment-info/test-payment` | `PaymentInfoController#testPayment` | `PortOneApiClient#requestTestPayment`, `PortOneApiClient#cancelTestPayment` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `PaymentCardUtils`
  - `validateCardInfo` → this#validateExpirationDate
  - `maskCardNumberForLogging` → this#maskCardNumber
- `PaymentCommandService`
  - `processPayment` → this#validatePaymentRequest, this#createCardPayment, this#createAccountPayment, this#createGeneralPayment, this#notifyUsersAndUpdateSaleStatus
  - `createCardPayment` → PaymentInfoQueryService#getPaymentInfoEntity, PortOneApiClient#processCardPayment, PaymentRepository#save~
  - `createAccountPayment` → this#validateBank, PaymentRepository#save~
  - `createGeneralPayment` → PaymentRepository#save~
  - `handleRefundIfNecessary` → PortOneApiClient#cancelPayment, PaymentRepository#save~
  - `notifyUsersAndUpdateSaleStatus` → SaleBidQueryService#findByOrderId, this#sendNotifications
  - `sendNotifications` → NotificationCommandService#createNotification
  - `updatePaymentStatus` → PaymentRepository#save~
  - `refundPayment` → PaymentRepository#findById~, PortOneApiClient#cancelPayment, PaymentRepository#save~, this#sendRefundNotification
  - `sendRefundNotification` → NotificationCommandService#createNotification, SaleBidQueryService#findByOrderId
- `PaymentCompletedEvent`
  - `create` → this#generateEventId
- `PaymentEncryptionService`
  - `encrypt` → this#generateKey
  - `decrypt` → this#generateKey
- `PaymentEvent`
  - `create` → this#generateEventId
- `PaymentEventConsumer`
  - `handlePaymentEvent` → this#isPaymentAlreadyProcessed, OrderRepository#findById~, UserQueryService#findByEmail, PaymentCommandService#processPayment, this#handlePaymentFailure
  - `handlePaymentRetryEvent` → this#isPaymentAlreadyProcessed, this#handlePaymentEvent
  - `isPaymentAlreadyProcessed` → PaymentRepository#findByOrder_Id
  - `handlePaymentFailure` → this#isRetryableError, PaymentEventProducer#sendPaymentRetryEvent
- `PaymentEventProducer`
  - `sendPaymentEvent` → kafka:payment-processing-topic
  - `sendPaymentRetryEvent` → kafka:payment-retry-topic
  - `sendPaymentEventSync` → kafka:payment-processing-topic
- `PaymentInfoCommandService`
  - `createPaymentInfo` → UserRepository#findByEmail, this#validateCardInfo, PortOneApiClient#requestTestPayment, PortOneApiClient#cancelTestPayment, this#maskCardNumber
  - `deletePaymentInfo` → UserRepository#findByEmail
- `PaymentInfoQueryService`
  - `getPaymentInfos` → PaymentInfoRepository#findAllByUser_Email
  - `getPaymentInfo` → PaymentInfoRepository#findByIdAndUser_Email, this#convertToDto
  - `getPaymentInfoEntity` → PaymentInfoRepository#findByIdAndUser_Email, PaymentEncryptionService#decrypt
  - `convertToDto` → PaymentEncryptionService#decrypt
- `PaymentQueryService`
  - `getPaymentDetails` → PaymentRepository#findById~, this#convertToDto
  - `getUserPayments` → PaymentRepository#findByOrder_User_EmailAndIsSuccess
  - `getSellerPayments` → PaymentRepository#findBySale_Seller_EmailAndIsSuccess
  - `findByImpUid` → PaymentRepository#findByImpUid
- `PortOneApiClient`
  - `requestTestPayment` → this#getAccessToken
  - `processCardPayment` → this#validatePaymentInfo, this#getAccessToken
  - `refundPayment` → this#getAccessToken
  - `cancelTestPayment` → this#refundPayment
  - `cancelPayment` → this#refundPayment

## 3. 클래스 목록 (역할별)

### controller (1)

- `PaymentInfoController` controller/command/PaymentInfoController.java · public: createPaymentInfo, deletePaymentInfo, getPaymentInfos, getPaymentInfo, testPayment · 주입: PaymentInfoCommandService, PaymentInfoQueryService, PortOneApiClient

### service-command (2)

- `PaymentCommandService` service/command/PaymentCommandService.java · public: processPayment, updatePaymentStatus, refundPayment · 주입: PaymentRepository, PaymentInfoQueryService, PortOneApiClient, NotificationCommandService, SaleBidQueryService
- `PaymentInfoCommandService` service/command/PaymentInfoCommandService.java · public: createPaymentInfo, deletePaymentInfo · 주입: UserRepository, PortOneApiClient

### service-query (2)

- `PaymentInfoQueryService` service/query/PaymentInfoQueryService.java · public: getPaymentInfos, getPaymentInfo, getPaymentInfoEntity · 주입: PaymentInfoRepository, PaymentEncryptionService
- `PaymentQueryService` service/query/PaymentQueryService.java · public: getPaymentDetails, getUserPayments, getSellerPayments, findByImpUid · 주입: PaymentRepository

### service (2)

- `PortOneApiClient` portone/PortOneApiClient.java · public: getAccessToken, requestTestPayment, processCardPayment, refundPayment, cancelTestPayment, cancelPayment · 주입: RestTemplate
- `PaymentEncryptionService` service/PaymentEncryptionService.java · public: encrypt, decrypt

### repository (2)

- `PaymentInfoRepository` (interface) repository/PaymentInfoRepository.java · extends/implements JpaRepository · public: findAllByUser_Email, findByIdAndUser_Email
- `PaymentRepository` (interface) repository/PaymentRepository.java · extends/implements JpaRepository · public: findByOrder_User_EmailAndIsSuccess, findBySale_Seller_EmailAndIsSuccess, findByImpUid, findByOrder_Id, existsSuccessfulPaymentByOrderId, findByOrder_IdAndStatus, findProcessingPaymentsByOrderId, findPaymentsCreatedSince

### entity (7)

- `AccountPayment` entity/AccountPayment.java · extends/implements Payment · public: getImpUid
- `Bank` (enum) entity/Bank.java · public: getDisplayName
- `CardPayment` entity/CardPayment.java · extends/implements Payment · public: getImpUid
- `GeneralPayment` entity/GeneralPayment.java · extends/implements Payment · public: getImpUid
- `Payment` (abstract class) entity/Payment.java · extends/implements BaseTimeEntity · public: assignOrder, assignSale, assignUser, setPaidAmount, updateSuccessStatus, updateStatus, getImpUid
- `PaymentInfo` entity/PaymentInfo.java · public: assignUser, unassignUser
- `PaymentStatus` (enum) entity/PaymentStatus.java · public: canTransitionTo

### event (4)

- `PaymentCompletedEvent` dto/kafka/PaymentCompletedEvent.java · public: create, isPaymentSuccess, isPaymentFailed
- `PaymentEvent` dto/kafka/PaymentEvent.java · public: create, withRetry, isMaxRetryExceeded
- `PaymentEventConsumer` service/kafka/PaymentEventConsumer.java · public: handlePaymentEvent, handlePaymentRetryEvent · 주입: PaymentCommandService, PaymentEventProducer, OrderRepository, PaymentRepository, UserQueryService
- `PaymentEventProducer` service/kafka/PaymentEventProducer.java · public: sendPaymentEvent, sendPaymentRetryEvent, sendPaymentEventSync · 주입: KafkaTemplate

### dto (10)

- `AccountPaymentDto` dto/AccountPaymentDto.java
- `AccountPaymentRequestDto` dto/AccountPaymentRequestDto.java
- `CardPaymentDto` dto/CardPaymentDto.java
- `CardPaymentRequestDto` dto/CardPaymentRequestDto.java
- `GeneralPaymentDto` dto/GeneralPaymentDto.java
- `GeneralPaymentRequestDto` dto/GeneralPaymentRequestDto.java
- `PaymentDto` dto/PaymentDto.java
- `PaymentRequestDto` dto/PaymentRequestDto.java
- `PaymentInfoCreateDto` dto/paymentInfo/PaymentInfoCreateDto.java
- `PaymentInfoDto` dto/paymentInfo/PaymentInfoDto.java

### exception (8)

- `PaymentAccessDeniedException` exception/PaymentAccessDeniedException.java
- `PaymentApiException` exception/PaymentApiException.java
- `PaymentErrorCode` exception/PaymentErrorCode.java
- `PaymentException` exception/PaymentException.java
- `PaymentInfoNotFoundException` exception/PaymentInfoNotFoundException.java
- `PaymentNotFoundException` exception/PaymentNotFoundException.java
- `PaymentProcessingException` exception/PaymentProcessingException.java
- `UnknownPaymentTypeException` exception/UnknownPaymentTypeException.java

### other (1)

- `PaymentCardUtils` util/PaymentCardUtils.java · public: validateCardInfo, maskCardNumber, maskCardPassword, maskBirthDate, maskCardNumberForLogging

### 다른 도메인 참조 (import·주입 기준)

- **notification**: `NotificationCategory` ← PaymentCommandService; `NotificationCommandService` ← PaymentCommandService; `NotificationType` ← PaymentCommandService
- **order**: `Order` ← Payment, PaymentCommandService, PaymentEventConsumer; `OrderBid` ← PaymentCommandService; `OrderRepository` ← PaymentEventConsumer
- **sale**: `Sale` ← Payment, PaymentCommandService; `SaleBid` ← PaymentCommandService; `SaleBidQueryService` ← PaymentCommandService; `SaleStatus` ← PaymentCommandService
- **user**: `User` ← Payment, PaymentCommandService, PaymentEventConsumer, PaymentInfo, PaymentInfoCommandService; `UserQueryService` ← PaymentEventConsumer; `UserRepository` ← PaymentInfoCommandService

### 이 도메인을 참조하는 다른 도메인

- **global**: GeneralPayment, PaymentCommandService, PaymentEvent, PaymentRepository, PaymentStatus
- **order**: Payment, PaymentCommandService
- **sale**: Payment
- **user**: PaymentInfo

## 4. JPA 엔티티 연관

- `AccountPayment` @Entity @DiscriminatorValue("ACCOUNT") · extends Payment
- `CardPayment` @Entity @DiscriminatorValue("CARD") · extends Payment
- `GeneralPayment` @Entity @DiscriminatorValue("GENERAL") · extends Payment
- `Payment` @Entity @Inheritance(strategy = InheritanceType.SINGLE_TABLE) @DiscriminatorColumn(name = "payment_type") · extends BaseTimeEntity
  - order: @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "order_id") → `Order` [order]
  - sale: @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "sale_id") → `Sale` [sale]
  - user: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) → `User` [user]
- `PaymentInfo` @Entity
  - user: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") → `User` [user]

다른 도메인 엔티티에서 들어오는 연관:

- `Order.payment` @OneToOne → `Payment` [order]
- `Sale.payment` @OneToOne → `Payment` [sale]
- `User.paymentInfos` @OneToMany → `PaymentInfo` [user]

## 5. 이벤트·메시징

- 발행 kafka `payment-processing-topic`: `PaymentEventProducer#sendPaymentEvent` → `PaymentEventConsumer#handlePaymentEvent` [payment]
- 발행 kafka `payment-retry-topic`: `PaymentEventProducer#sendPaymentRetryEvent` → `PaymentEventConsumer#handlePaymentRetryEvent` [payment]
- 발행 kafka `payment-processing-topic`: `PaymentEventProducer#sendPaymentEventSync` → `PaymentEventConsumer#handlePaymentEvent` [payment]
- 구독 kafka `payment-processing-topic`: `PaymentEventConsumer#handlePaymentEvent` (groupId=payment-processing-group containerFactory=paymentEventKafkaListenerContainerFactory) ← `PaymentEventProducer#sendPaymentEvent` [payment], `PaymentEventProducer#sendPaymentEventSync` [payment]
- 구독 kafka `payment-retry-topic`: `PaymentEventConsumer#handlePaymentRetryEvent` (groupId=payment-retry-group containerFactory=paymentEventKafkaListenerContainerFactory) ← `PaymentEventProducer#sendPaymentRetryEvent` [payment]

## 6. AOP

(없음)

