# legacypayment 도메인

기준 경로: `src/main/java/com/fream/back/domain/legacypayment/` · 파일 10개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/legacy/payment-info` | `LegacyPaymentInfoController#createPaymentInfo` | `this#extractEmailFromSecurityContext`, `LegacyPaymentInfoCommandService#createPaymentInfo` |
| DELETE | `/legacy/payment-info/{id}` | `LegacyPaymentInfoController#deletePaymentInfo` | `this#extractEmailFromSecurityContext`, `LegacyPaymentInfoCommandService#deletePaymentInfo` |
| GET | `/legacy/payment-info` | `LegacyPaymentInfoController#getPaymentInfos` | `this#extractEmailFromSecurityContext`, `LegacyPaymentInfoQueryService#getPaymentInfos` |
| GET | `/legacy/payment-info/{id}` | `LegacyPaymentInfoController#getPaymentInfo` | `this#extractEmailFromSecurityContext`, `LegacyPaymentInfoQueryService#getPaymentInfo` |
| POST | `/legacy/payment-info/test-payment` | `LegacyPaymentInfoController#testPayment` | `LegacyPortOneApiClient#requestTestPayment`, `LegacyPortOneApiClient#cancelTestPayment` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `LegacyPaymentCardUtils`
  - `validateCardInfo` → this#validateExpirationDate
  - `maskCardNumberForLogging` → this#maskCardNumber
- `LegacyPaymentEncryptionService`
  - `encrypt` → this#generateKey
  - `decrypt` → this#generateKey
- `LegacyPaymentEventConsumer`
  - `handlePaymentEvent` → this#isPaymentAlreadyProcessed, OrderRepository#findById~, LegacyUserQueryService#findByEmail, PaymentCommandService#processPayment, this#handlePaymentFailure
  - `handlePaymentRetryEvent` → this#isPaymentAlreadyProcessed, this#handlePaymentEvent
  - `isPaymentAlreadyProcessed` → PaymentRepository#findByOrder_Id
  - `handlePaymentFailure` → this#isRetryableError, LegacyPaymentEventProducer#sendPaymentRetryEvent
- `LegacyPaymentEventProducer`
  - `sendPaymentEvent` → kafka:payment-processing-topic
  - `sendPaymentRetryEvent` → kafka:payment-retry-topic
  - `sendPaymentEventSync` → kafka:payment-processing-topic
- `LegacyPaymentInfoCommandService`
  - `createPaymentInfo` → UserRepository#findByEmail, this#validateCardInfo, LegacyPortOneApiClient#requestTestPayment, LegacyPortOneApiClient#cancelTestPayment, this#maskCardNumber
  - `deletePaymentInfo` → UserRepository#findByEmail
- `LegacyPaymentInfoQueryService`
  - `getPaymentInfos` → LegacyPaymentInfoRepository#findAllByUser_Email
  - `getPaymentInfo` → LegacyPaymentInfoRepository#findByIdAndUser_Email, this#convertToDto
  - `getPaymentInfoEntity` → LegacyPaymentInfoRepository#findByIdAndUser_Email, LegacyPaymentEncryptionService#decrypt
  - `convertToDto` → LegacyPaymentEncryptionService#decrypt
- `LegacyPaymentQueryService`
  - `getPaymentDetails` → PaymentRepository#findById~, this#convertToDto
  - `getUserPayments` → PaymentRepository#findByOrder_User_EmailAndIsSuccess
  - `getSellerPayments` → PaymentRepository#findBySale_Seller_EmailAndIsSuccess
  - `findByImpUid` → PaymentRepository#findByImpUid
- `LegacyPortOneApiClient`
  - `requestTestPayment` → this#getAccessToken
  - `processCardPayment` → this#validatePaymentInfo, this#getAccessToken
  - `refundPayment` → this#getAccessToken
  - `cancelTestPayment` → this#refundPayment
  - `cancelPayment` → this#refundPayment

## 3. 클래스 목록 (역할별)

### controller (1)

- `LegacyPaymentInfoController` controller/command/LegacyPaymentInfoController.java · public: createPaymentInfo, deletePaymentInfo, getPaymentInfos, getPaymentInfo, testPayment · 주입: LegacyPaymentInfoCommandService, LegacyPaymentInfoQueryService, LegacyPortOneApiClient

### service-command (1)

- `LegacyPaymentInfoCommandService` service/command/LegacyPaymentInfoCommandService.java · public: createPaymentInfo, deletePaymentInfo · 주입: UserRepository, LegacyPortOneApiClient

### service-query (2)

- `LegacyPaymentInfoQueryService` service/query/LegacyPaymentInfoQueryService.java · public: getPaymentInfos, getPaymentInfo, getPaymentInfoEntity · 주입: LegacyPaymentInfoRepository, LegacyPaymentEncryptionService
- `LegacyPaymentQueryService` service/query/LegacyPaymentQueryService.java · public: getPaymentDetails, getUserPayments, getSellerPayments, findByImpUid · 주입: PaymentRepository

### service (2)

- `LegacyPortOneApiClient` portone/LegacyPortOneApiClient.java · public: getAccessToken, requestTestPayment, processCardPayment, refundPayment, cancelTestPayment, cancelPayment · 주입: RestTemplate
- `LegacyPaymentEncryptionService` service/LegacyPaymentEncryptionService.java · public: encrypt, decrypt

### repository (1)

- `LegacyPaymentInfoRepository` (interface) repository/LegacyPaymentInfoRepository.java · extends/implements JpaRepository · public: findAllByUser_Email, findByIdAndUser_Email

### event (2)

- `LegacyPaymentEventConsumer` service/kafka/LegacyPaymentEventConsumer.java · public: handlePaymentEvent, handlePaymentRetryEvent · 주입: PaymentCommandService, LegacyPaymentEventProducer, OrderRepository, PaymentRepository, LegacyUserQueryService
- `LegacyPaymentEventProducer` service/kafka/LegacyPaymentEventProducer.java · public: sendPaymentEvent, sendPaymentRetryEvent, sendPaymentEventSync · 주입: KafkaTemplate

### other (1)

- `LegacyPaymentCardUtils` util/LegacyPaymentCardUtils.java · public: validateCardInfo, maskCardNumber, maskCardPassword, maskBirthDate, maskCardNumberForLogging

### 다른 도메인 참조 (import·주입 기준)

- **legacyuser**: `LegacyUserQueryService` ← LegacyPaymentEventConsumer
- **order**: `Order` ← LegacyPaymentEventConsumer; `OrderRepository` ← LegacyPaymentEventConsumer
- **payment**: `AccountPayment` ← LegacyPaymentQueryService; `CardPayment` ← LegacyPaymentQueryService; `GeneralPayment` ← LegacyPaymentQueryService; `Payment` ← LegacyPaymentEventConsumer, LegacyPaymentQueryService; `PaymentCommandService` ← LegacyPaymentEventConsumer; `PaymentEvent` ← LegacyPaymentEventConsumer, LegacyPaymentEventProducer; `PaymentInfo` ← LegacyPaymentInfoCommandService, LegacyPaymentInfoQueryService, LegacyPaymentInfoRepository, LegacyPortOneApiClient; `PaymentRepository` ← LegacyPaymentEventConsumer, LegacyPaymentQueryService
- **user**: `User` ← LegacyPaymentEventConsumer, LegacyPaymentInfoCommandService; `UserRepository` ← LegacyPaymentInfoCommandService

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

- 발행 kafka `payment-processing-topic`: `LegacyPaymentEventProducer#sendPaymentEvent` → `AdminPaymentEventConsumer#handlePaymentEvent` [adminpayment], `InternalPaymentEventConsumer#handlePaymentEvent` [internalpayment], `LegacyPaymentEventConsumer#handlePaymentEvent` [legacypayment], `PartnerPaymentEventConsumer#handlePaymentEvent` [partnerpayment], `PaymentEventConsumer#handlePaymentEvent` [payment], `SettlementPaymentEventConsumer#handlePaymentEvent` [settlementpayment]
- 발행 kafka `payment-retry-topic`: `LegacyPaymentEventProducer#sendPaymentRetryEvent` → `AdminPaymentEventConsumer#handlePaymentRetryEvent` [adminpayment], `InternalPaymentEventConsumer#handlePaymentRetryEvent` [internalpayment], `LegacyPaymentEventConsumer#handlePaymentRetryEvent` [legacypayment], `PartnerPaymentEventConsumer#handlePaymentRetryEvent` [partnerpayment], `PaymentEventConsumer#handlePaymentRetryEvent` [payment], `SettlementPaymentEventConsumer#handlePaymentRetryEvent` [settlementpayment]
- 발행 kafka `payment-processing-topic`: `LegacyPaymentEventProducer#sendPaymentEventSync` → `AdminPaymentEventConsumer#handlePaymentEvent` [adminpayment], `InternalPaymentEventConsumer#handlePaymentEvent` [internalpayment], `LegacyPaymentEventConsumer#handlePaymentEvent` [legacypayment], `PartnerPaymentEventConsumer#handlePaymentEvent` [partnerpayment], `PaymentEventConsumer#handlePaymentEvent` [payment], `SettlementPaymentEventConsumer#handlePaymentEvent` [settlementpayment]
- 구독 kafka `payment-processing-topic`: `LegacyPaymentEventConsumer#handlePaymentEvent` (groupId=payment-processing-group containerFactory=paymentEventKafkaListenerContainerFactory) ← `AdminPaymentEventProducer#sendPaymentEvent` [adminpayment], `AdminPaymentEventProducer#sendPaymentEventSync` [adminpayment], `InternalPaymentEventProducer#sendPaymentEvent` [internalpayment], `InternalPaymentEventProducer#sendPaymentEventSync` [internalpayment], `LegacyPaymentEventProducer#sendPaymentEvent` [legacypayment], `LegacyPaymentEventProducer#sendPaymentEventSync` [legacypayment], `PartnerPaymentEventProducer#sendPaymentEvent` [partnerpayment], `PartnerPaymentEventProducer#sendPaymentEventSync` [partnerpayment], `PaymentEventProducer#sendPaymentEvent` [payment], `PaymentEventProducer#sendPaymentEventSync` [payment], `SettlementPaymentEventProducer#sendPaymentEvent` [settlementpayment], `SettlementPaymentEventProducer#sendPaymentEventSync` [settlementpayment]
- 구독 kafka `payment-retry-topic`: `LegacyPaymentEventConsumer#handlePaymentRetryEvent` (groupId=payment-retry-group containerFactory=paymentEventKafkaListenerContainerFactory) ← `AdminPaymentEventProducer#sendPaymentRetryEvent` [adminpayment], `InternalPaymentEventProducer#sendPaymentRetryEvent` [internalpayment], `LegacyPaymentEventProducer#sendPaymentRetryEvent` [legacypayment], `PartnerPaymentEventProducer#sendPaymentRetryEvent` [partnerpayment], `PaymentEventProducer#sendPaymentRetryEvent` [payment], `SettlementPaymentEventProducer#sendPaymentRetryEvent` [settlementpayment]

## 6. AOP

(없음)

