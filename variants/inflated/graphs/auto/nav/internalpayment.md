# internalpayment 도메인

기준 경로: `src/main/java/com/fream/back/domain/internalpayment/` · 파일 10개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/internal/payment-info` | `InternalPaymentInfoController#createPaymentInfo` | `this#extractEmailFromSecurityContext`, `InternalPaymentInfoCommandService#createPaymentInfo` |
| DELETE | `/internal/payment-info/{id}` | `InternalPaymentInfoController#deletePaymentInfo` | `this#extractEmailFromSecurityContext`, `InternalPaymentInfoCommandService#deletePaymentInfo` |
| GET | `/internal/payment-info` | `InternalPaymentInfoController#getPaymentInfos` | `this#extractEmailFromSecurityContext`, `InternalPaymentInfoQueryService#getPaymentInfos` |
| GET | `/internal/payment-info/{id}` | `InternalPaymentInfoController#getPaymentInfo` | `this#extractEmailFromSecurityContext`, `InternalPaymentInfoQueryService#getPaymentInfo` |
| POST | `/internal/payment-info/test-payment` | `InternalPaymentInfoController#testPayment` | `InternalPortOneApiClient#requestTestPayment`, `InternalPortOneApiClient#cancelTestPayment` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `InternalPaymentCardUtils`
  - `validateCardInfo` → this#validateExpirationDate
  - `maskCardNumberForLogging` → this#maskCardNumber
- `InternalPaymentEncryptionService`
  - `encrypt` → this#generateKey
  - `decrypt` → this#generateKey
- `InternalPaymentEventConsumer`
  - `handlePaymentEvent` → this#isPaymentAlreadyProcessed, OrderRepository#findById~, InternalUserQueryService#findByEmail, PaymentCommandService#processPayment, this#handlePaymentFailure
  - `handlePaymentRetryEvent` → this#isPaymentAlreadyProcessed, this#handlePaymentEvent
  - `isPaymentAlreadyProcessed` → PaymentRepository#findByOrder_Id
  - `handlePaymentFailure` → this#isRetryableError, InternalPaymentEventProducer#sendPaymentRetryEvent
- `InternalPaymentEventProducer`
  - `sendPaymentEvent` → kafka:payment-processing-topic
  - `sendPaymentRetryEvent` → kafka:payment-retry-topic
  - `sendPaymentEventSync` → kafka:payment-processing-topic
- `InternalPaymentInfoCommandService`
  - `createPaymentInfo` → UserRepository#findByEmail, this#validateCardInfo, InternalPortOneApiClient#requestTestPayment, InternalPortOneApiClient#cancelTestPayment, this#maskCardNumber
  - `deletePaymentInfo` → UserRepository#findByEmail
- `InternalPaymentInfoQueryService`
  - `getPaymentInfos` → InternalPaymentInfoRepository#findAllByUser_Email
  - `getPaymentInfo` → InternalPaymentInfoRepository#findByIdAndUser_Email, this#convertToDto
  - `getPaymentInfoEntity` → InternalPaymentInfoRepository#findByIdAndUser_Email, InternalPaymentEncryptionService#decrypt
  - `convertToDto` → InternalPaymentEncryptionService#decrypt
- `InternalPaymentQueryService`
  - `getPaymentDetails` → PaymentRepository#findById~, this#convertToDto
  - `getUserPayments` → PaymentRepository#findByOrder_User_EmailAndIsSuccess
  - `getSellerPayments` → PaymentRepository#findBySale_Seller_EmailAndIsSuccess
  - `findByImpUid` → PaymentRepository#findByImpUid
- `InternalPortOneApiClient`
  - `requestTestPayment` → this#getAccessToken
  - `processCardPayment` → this#validatePaymentInfo, this#getAccessToken
  - `refundPayment` → this#getAccessToken
  - `cancelTestPayment` → this#refundPayment
  - `cancelPayment` → this#refundPayment

## 3. 클래스 목록 (역할별)

### controller (1)

- `InternalPaymentInfoController` controller/command/InternalPaymentInfoController.java · public: createPaymentInfo, deletePaymentInfo, getPaymentInfos, getPaymentInfo, testPayment · 주입: InternalPaymentInfoCommandService, InternalPaymentInfoQueryService, InternalPortOneApiClient

### service-command (1)

- `InternalPaymentInfoCommandService` service/command/InternalPaymentInfoCommandService.java · public: createPaymentInfo, deletePaymentInfo · 주입: UserRepository, InternalPortOneApiClient

### service-query (2)

- `InternalPaymentInfoQueryService` service/query/InternalPaymentInfoQueryService.java · public: getPaymentInfos, getPaymentInfo, getPaymentInfoEntity · 주입: InternalPaymentInfoRepository, InternalPaymentEncryptionService
- `InternalPaymentQueryService` service/query/InternalPaymentQueryService.java · public: getPaymentDetails, getUserPayments, getSellerPayments, findByImpUid · 주입: PaymentRepository

### service (2)

- `InternalPortOneApiClient` portone/InternalPortOneApiClient.java · public: getAccessToken, requestTestPayment, processCardPayment, refundPayment, cancelTestPayment, cancelPayment · 주입: RestTemplate
- `InternalPaymentEncryptionService` service/InternalPaymentEncryptionService.java · public: encrypt, decrypt

### repository (1)

- `InternalPaymentInfoRepository` (interface) repository/InternalPaymentInfoRepository.java · extends/implements JpaRepository · public: findAllByUser_Email, findByIdAndUser_Email

### event (2)

- `InternalPaymentEventConsumer` service/kafka/InternalPaymentEventConsumer.java · public: handlePaymentEvent, handlePaymentRetryEvent · 주입: PaymentCommandService, InternalPaymentEventProducer, OrderRepository, PaymentRepository, InternalUserQueryService
- `InternalPaymentEventProducer` service/kafka/InternalPaymentEventProducer.java · public: sendPaymentEvent, sendPaymentRetryEvent, sendPaymentEventSync · 주입: KafkaTemplate

### other (1)

- `InternalPaymentCardUtils` util/InternalPaymentCardUtils.java · public: validateCardInfo, maskCardNumber, maskCardPassword, maskBirthDate, maskCardNumberForLogging

### 다른 도메인 참조 (import·주입 기준)

- **internaluser**: `InternalUserQueryService` ← InternalPaymentEventConsumer
- **order**: `Order` ← InternalPaymentEventConsumer; `OrderRepository` ← InternalPaymentEventConsumer
- **payment**: `AccountPayment` ← InternalPaymentQueryService; `CardPayment` ← InternalPaymentQueryService; `GeneralPayment` ← InternalPaymentQueryService; `Payment` ← InternalPaymentEventConsumer, InternalPaymentQueryService; `PaymentCommandService` ← InternalPaymentEventConsumer; `PaymentEvent` ← InternalPaymentEventConsumer, InternalPaymentEventProducer; `PaymentInfo` ← InternalPaymentInfoCommandService, InternalPaymentInfoQueryService, InternalPaymentInfoRepository, InternalPortOneApiClient; `PaymentRepository` ← InternalPaymentEventConsumer, InternalPaymentQueryService
- **user**: `User` ← InternalPaymentEventConsumer, InternalPaymentInfoCommandService; `UserRepository` ← InternalPaymentInfoCommandService

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

- 발행 kafka `payment-processing-topic`: `InternalPaymentEventProducer#sendPaymentEvent` → `AdminPaymentEventConsumer#handlePaymentEvent` [adminpayment], `InternalPaymentEventConsumer#handlePaymentEvent` [internalpayment], `LegacyPaymentEventConsumer#handlePaymentEvent` [legacypayment], `PartnerPaymentEventConsumer#handlePaymentEvent` [partnerpayment], `PaymentEventConsumer#handlePaymentEvent` [payment], `SettlementPaymentEventConsumer#handlePaymentEvent` [settlementpayment]
- 발행 kafka `payment-retry-topic`: `InternalPaymentEventProducer#sendPaymentRetryEvent` → `AdminPaymentEventConsumer#handlePaymentRetryEvent` [adminpayment], `InternalPaymentEventConsumer#handlePaymentRetryEvent` [internalpayment], `LegacyPaymentEventConsumer#handlePaymentRetryEvent` [legacypayment], `PartnerPaymentEventConsumer#handlePaymentRetryEvent` [partnerpayment], `PaymentEventConsumer#handlePaymentRetryEvent` [payment], `SettlementPaymentEventConsumer#handlePaymentRetryEvent` [settlementpayment]
- 발행 kafka `payment-processing-topic`: `InternalPaymentEventProducer#sendPaymentEventSync` → `AdminPaymentEventConsumer#handlePaymentEvent` [adminpayment], `InternalPaymentEventConsumer#handlePaymentEvent` [internalpayment], `LegacyPaymentEventConsumer#handlePaymentEvent` [legacypayment], `PartnerPaymentEventConsumer#handlePaymentEvent` [partnerpayment], `PaymentEventConsumer#handlePaymentEvent` [payment], `SettlementPaymentEventConsumer#handlePaymentEvent` [settlementpayment]
- 구독 kafka `payment-processing-topic`: `InternalPaymentEventConsumer#handlePaymentEvent` (groupId=payment-processing-group containerFactory=paymentEventKafkaListenerContainerFactory) ← `AdminPaymentEventProducer#sendPaymentEvent` [adminpayment], `AdminPaymentEventProducer#sendPaymentEventSync` [adminpayment], `InternalPaymentEventProducer#sendPaymentEvent` [internalpayment], `InternalPaymentEventProducer#sendPaymentEventSync` [internalpayment], `LegacyPaymentEventProducer#sendPaymentEvent` [legacypayment], `LegacyPaymentEventProducer#sendPaymentEventSync` [legacypayment], `PartnerPaymentEventProducer#sendPaymentEvent` [partnerpayment], `PartnerPaymentEventProducer#sendPaymentEventSync` [partnerpayment], `PaymentEventProducer#sendPaymentEvent` [payment], `PaymentEventProducer#sendPaymentEventSync` [payment], `SettlementPaymentEventProducer#sendPaymentEvent` [settlementpayment], `SettlementPaymentEventProducer#sendPaymentEventSync` [settlementpayment]
- 구독 kafka `payment-retry-topic`: `InternalPaymentEventConsumer#handlePaymentRetryEvent` (groupId=payment-retry-group containerFactory=paymentEventKafkaListenerContainerFactory) ← `AdminPaymentEventProducer#sendPaymentRetryEvent` [adminpayment], `InternalPaymentEventProducer#sendPaymentRetryEvent` [internalpayment], `LegacyPaymentEventProducer#sendPaymentRetryEvent` [legacypayment], `PartnerPaymentEventProducer#sendPaymentRetryEvent` [partnerpayment], `PaymentEventProducer#sendPaymentRetryEvent` [payment], `SettlementPaymentEventProducer#sendPaymentRetryEvent` [settlementpayment]

## 6. AOP

(없음)

