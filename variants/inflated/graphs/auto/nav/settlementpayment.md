# settlementpayment 도메인

기준 경로: `src/main/java/com/fream/back/domain/settlementpayment/` · 파일 10개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/settlement/payment-info` | `SettlementPaymentInfoController#createPaymentInfo` | `this#extractEmailFromSecurityContext`, `SettlementPaymentInfoCommandService#createPaymentInfo` |
| DELETE | `/settlement/payment-info/{id}` | `SettlementPaymentInfoController#deletePaymentInfo` | `this#extractEmailFromSecurityContext`, `SettlementPaymentInfoCommandService#deletePaymentInfo` |
| GET | `/settlement/payment-info` | `SettlementPaymentInfoController#getPaymentInfos` | `this#extractEmailFromSecurityContext`, `SettlementPaymentInfoQueryService#getPaymentInfos` |
| GET | `/settlement/payment-info/{id}` | `SettlementPaymentInfoController#getPaymentInfo` | `this#extractEmailFromSecurityContext`, `SettlementPaymentInfoQueryService#getPaymentInfo` |
| POST | `/settlement/payment-info/test-payment` | `SettlementPaymentInfoController#testPayment` | `SettlementPortOneApiClient#requestTestPayment`, `SettlementPortOneApiClient#cancelTestPayment` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `SettlementPaymentCardUtils`
  - `validateCardInfo` → this#validateExpirationDate
  - `maskCardNumberForLogging` → this#maskCardNumber
- `SettlementPaymentEncryptionService`
  - `encrypt` → this#generateKey
  - `decrypt` → this#generateKey
- `SettlementPaymentEventConsumer`
  - `handlePaymentEvent` → this#isPaymentAlreadyProcessed, OrderRepository#findById~, SettlementUserQueryService#findByEmail, PaymentCommandService#processPayment, this#handlePaymentFailure
  - `handlePaymentRetryEvent` → this#isPaymentAlreadyProcessed, this#handlePaymentEvent
  - `isPaymentAlreadyProcessed` → PaymentRepository#findByOrder_Id
  - `handlePaymentFailure` → this#isRetryableError, SettlementPaymentEventProducer#sendPaymentRetryEvent
- `SettlementPaymentEventProducer`
  - `sendPaymentEvent` → kafka:payment-processing-topic
  - `sendPaymentRetryEvent` → kafka:payment-retry-topic
  - `sendPaymentEventSync` → kafka:payment-processing-topic
- `SettlementPaymentInfoCommandService`
  - `createPaymentInfo` → UserRepository#findByEmail, this#validateCardInfo, SettlementPortOneApiClient#requestTestPayment, SettlementPortOneApiClient#cancelTestPayment, this#maskCardNumber
  - `deletePaymentInfo` → UserRepository#findByEmail
- `SettlementPaymentInfoQueryService`
  - `getPaymentInfos` → SettlementPaymentInfoRepository#findAllByUser_Email
  - `getPaymentInfo` → SettlementPaymentInfoRepository#findByIdAndUser_Email, this#convertToDto
  - `getPaymentInfoEntity` → SettlementPaymentInfoRepository#findByIdAndUser_Email, SettlementPaymentEncryptionService#decrypt
  - `convertToDto` → SettlementPaymentEncryptionService#decrypt
- `SettlementPaymentQueryService`
  - `getPaymentDetails` → PaymentRepository#findById~, this#convertToDto
  - `getUserPayments` → PaymentRepository#findByOrder_User_EmailAndIsSuccess
  - `getSellerPayments` → PaymentRepository#findBySale_Seller_EmailAndIsSuccess
  - `findByImpUid` → PaymentRepository#findByImpUid
- `SettlementPortOneApiClient`
  - `requestTestPayment` → this#getAccessToken
  - `processCardPayment` → this#validatePaymentInfo, this#getAccessToken
  - `refundPayment` → this#getAccessToken
  - `cancelTestPayment` → this#refundPayment
  - `cancelPayment` → this#refundPayment

## 3. 클래스 목록 (역할별)

### controller (1)

- `SettlementPaymentInfoController` controller/command/SettlementPaymentInfoController.java · public: createPaymentInfo, deletePaymentInfo, getPaymentInfos, getPaymentInfo, testPayment · 주입: SettlementPaymentInfoCommandService, SettlementPaymentInfoQueryService, SettlementPortOneApiClient

### service-command (1)

- `SettlementPaymentInfoCommandService` service/command/SettlementPaymentInfoCommandService.java · public: createPaymentInfo, deletePaymentInfo · 주입: UserRepository, SettlementPortOneApiClient

### service-query (2)

- `SettlementPaymentInfoQueryService` service/query/SettlementPaymentInfoQueryService.java · public: getPaymentInfos, getPaymentInfo, getPaymentInfoEntity · 주입: SettlementPaymentInfoRepository, SettlementPaymentEncryptionService
- `SettlementPaymentQueryService` service/query/SettlementPaymentQueryService.java · public: getPaymentDetails, getUserPayments, getSellerPayments, findByImpUid · 주입: PaymentRepository

### service (2)

- `SettlementPortOneApiClient` portone/SettlementPortOneApiClient.java · public: getAccessToken, requestTestPayment, processCardPayment, refundPayment, cancelTestPayment, cancelPayment · 주입: RestTemplate
- `SettlementPaymentEncryptionService` service/SettlementPaymentEncryptionService.java · public: encrypt, decrypt

### repository (1)

- `SettlementPaymentInfoRepository` (interface) repository/SettlementPaymentInfoRepository.java · extends/implements JpaRepository · public: findAllByUser_Email, findByIdAndUser_Email

### event (2)

- `SettlementPaymentEventConsumer` service/kafka/SettlementPaymentEventConsumer.java · public: handlePaymentEvent, handlePaymentRetryEvent · 주입: PaymentCommandService, SettlementPaymentEventProducer, OrderRepository, PaymentRepository, SettlementUserQueryService
- `SettlementPaymentEventProducer` service/kafka/SettlementPaymentEventProducer.java · public: sendPaymentEvent, sendPaymentRetryEvent, sendPaymentEventSync · 주입: KafkaTemplate

### other (1)

- `SettlementPaymentCardUtils` util/SettlementPaymentCardUtils.java · public: validateCardInfo, maskCardNumber, maskCardPassword, maskBirthDate, maskCardNumberForLogging

### 다른 도메인 참조 (import·주입 기준)

- **order**: `Order` ← SettlementPaymentEventConsumer; `OrderRepository` ← SettlementPaymentEventConsumer
- **payment**: `AccountPayment` ← SettlementPaymentQueryService; `CardPayment` ← SettlementPaymentQueryService; `GeneralPayment` ← SettlementPaymentQueryService; `Payment` ← SettlementPaymentEventConsumer, SettlementPaymentQueryService; `PaymentCommandService` ← SettlementPaymentEventConsumer; `PaymentEvent` ← SettlementPaymentEventConsumer, SettlementPaymentEventProducer; `PaymentInfo` ← SettlementPaymentInfoCommandService, SettlementPaymentInfoQueryService, SettlementPaymentInfoRepository, SettlementPortOneApiClient; `PaymentRepository` ← SettlementPaymentEventConsumer, SettlementPaymentQueryService
- **settlementuser**: `SettlementUserQueryService` ← SettlementPaymentEventConsumer
- **user**: `User` ← SettlementPaymentEventConsumer, SettlementPaymentInfoCommandService; `UserRepository` ← SettlementPaymentInfoCommandService

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

- 발행 kafka `payment-processing-topic`: `SettlementPaymentEventProducer#sendPaymentEvent` → `AdminPaymentEventConsumer#handlePaymentEvent` [adminpayment], `InternalPaymentEventConsumer#handlePaymentEvent` [internalpayment], `LegacyPaymentEventConsumer#handlePaymentEvent` [legacypayment], `PartnerPaymentEventConsumer#handlePaymentEvent` [partnerpayment], `PaymentEventConsumer#handlePaymentEvent` [payment], `SettlementPaymentEventConsumer#handlePaymentEvent` [settlementpayment]
- 발행 kafka `payment-retry-topic`: `SettlementPaymentEventProducer#sendPaymentRetryEvent` → `AdminPaymentEventConsumer#handlePaymentRetryEvent` [adminpayment], `InternalPaymentEventConsumer#handlePaymentRetryEvent` [internalpayment], `LegacyPaymentEventConsumer#handlePaymentRetryEvent` [legacypayment], `PartnerPaymentEventConsumer#handlePaymentRetryEvent` [partnerpayment], `PaymentEventConsumer#handlePaymentRetryEvent` [payment], `SettlementPaymentEventConsumer#handlePaymentRetryEvent` [settlementpayment]
- 발행 kafka `payment-processing-topic`: `SettlementPaymentEventProducer#sendPaymentEventSync` → `AdminPaymentEventConsumer#handlePaymentEvent` [adminpayment], `InternalPaymentEventConsumer#handlePaymentEvent` [internalpayment], `LegacyPaymentEventConsumer#handlePaymentEvent` [legacypayment], `PartnerPaymentEventConsumer#handlePaymentEvent` [partnerpayment], `PaymentEventConsumer#handlePaymentEvent` [payment], `SettlementPaymentEventConsumer#handlePaymentEvent` [settlementpayment]
- 구독 kafka `payment-processing-topic`: `SettlementPaymentEventConsumer#handlePaymentEvent` (groupId=payment-processing-group containerFactory=paymentEventKafkaListenerContainerFactory) ← `AdminPaymentEventProducer#sendPaymentEvent` [adminpayment], `AdminPaymentEventProducer#sendPaymentEventSync` [adminpayment], `InternalPaymentEventProducer#sendPaymentEvent` [internalpayment], `InternalPaymentEventProducer#sendPaymentEventSync` [internalpayment], `LegacyPaymentEventProducer#sendPaymentEvent` [legacypayment], `LegacyPaymentEventProducer#sendPaymentEventSync` [legacypayment], `PartnerPaymentEventProducer#sendPaymentEvent` [partnerpayment], `PartnerPaymentEventProducer#sendPaymentEventSync` [partnerpayment], `PaymentEventProducer#sendPaymentEvent` [payment], `PaymentEventProducer#sendPaymentEventSync` [payment], `SettlementPaymentEventProducer#sendPaymentEvent` [settlementpayment], `SettlementPaymentEventProducer#sendPaymentEventSync` [settlementpayment]
- 구독 kafka `payment-retry-topic`: `SettlementPaymentEventConsumer#handlePaymentRetryEvent` (groupId=payment-retry-group containerFactory=paymentEventKafkaListenerContainerFactory) ← `AdminPaymentEventProducer#sendPaymentRetryEvent` [adminpayment], `InternalPaymentEventProducer#sendPaymentRetryEvent` [internalpayment], `LegacyPaymentEventProducer#sendPaymentRetryEvent` [legacypayment], `PartnerPaymentEventProducer#sendPaymentRetryEvent` [partnerpayment], `PaymentEventProducer#sendPaymentRetryEvent` [payment], `SettlementPaymentEventProducer#sendPaymentRetryEvent` [settlementpayment]

## 6. AOP

(없음)

