# partnerpayment 도메인

기준 경로: `src/main/java/com/fream/back/domain/partnerpayment/` · 파일 10개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/partner/payment-info` | `PartnerPaymentInfoController#createPaymentInfo` | `this#extractEmailFromSecurityContext`, `PartnerPaymentInfoCommandService#createPaymentInfo` |
| DELETE | `/partner/payment-info/{id}` | `PartnerPaymentInfoController#deletePaymentInfo` | `this#extractEmailFromSecurityContext`, `PartnerPaymentInfoCommandService#deletePaymentInfo` |
| GET | `/partner/payment-info` | `PartnerPaymentInfoController#getPaymentInfos` | `this#extractEmailFromSecurityContext`, `PartnerPaymentInfoQueryService#getPaymentInfos` |
| GET | `/partner/payment-info/{id}` | `PartnerPaymentInfoController#getPaymentInfo` | `this#extractEmailFromSecurityContext`, `PartnerPaymentInfoQueryService#getPaymentInfo` |
| POST | `/partner/payment-info/test-payment` | `PartnerPaymentInfoController#testPayment` | `PartnerPortOneApiClient#requestTestPayment`, `PartnerPortOneApiClient#cancelTestPayment` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `PartnerPaymentCardUtils`
  - `validateCardInfo` → this#validateExpirationDate
  - `maskCardNumberForLogging` → this#maskCardNumber
- `PartnerPaymentEncryptionService`
  - `encrypt` → this#generateKey
  - `decrypt` → this#generateKey
- `PartnerPaymentEventConsumer`
  - `handlePaymentEvent` → this#isPaymentAlreadyProcessed, OrderRepository#findById~, PartnerUserQueryService#findByEmail, PaymentCommandService#processPayment, this#handlePaymentFailure
  - `handlePaymentRetryEvent` → this#isPaymentAlreadyProcessed, this#handlePaymentEvent
  - `isPaymentAlreadyProcessed` → PaymentRepository#findByOrder_Id
  - `handlePaymentFailure` → this#isRetryableError, PartnerPaymentEventProducer#sendPaymentRetryEvent
- `PartnerPaymentEventProducer`
  - `sendPaymentEvent` → kafka:payment-processing-topic
  - `sendPaymentRetryEvent` → kafka:payment-retry-topic
  - `sendPaymentEventSync` → kafka:payment-processing-topic
- `PartnerPaymentInfoCommandService`
  - `createPaymentInfo` → UserRepository#findByEmail, this#validateCardInfo, PartnerPortOneApiClient#requestTestPayment, PartnerPortOneApiClient#cancelTestPayment, this#maskCardNumber
  - `deletePaymentInfo` → UserRepository#findByEmail
- `PartnerPaymentInfoQueryService`
  - `getPaymentInfos` → PartnerPaymentInfoRepository#findAllByUser_Email
  - `getPaymentInfo` → PartnerPaymentInfoRepository#findByIdAndUser_Email, this#convertToDto
  - `getPaymentInfoEntity` → PartnerPaymentInfoRepository#findByIdAndUser_Email, PartnerPaymentEncryptionService#decrypt
  - `convertToDto` → PartnerPaymentEncryptionService#decrypt
- `PartnerPaymentQueryService`
  - `getPaymentDetails` → PaymentRepository#findById~, this#convertToDto
  - `getUserPayments` → PaymentRepository#findByOrder_User_EmailAndIsSuccess
  - `getSellerPayments` → PaymentRepository#findBySale_Seller_EmailAndIsSuccess
  - `findByImpUid` → PaymentRepository#findByImpUid
- `PartnerPortOneApiClient`
  - `requestTestPayment` → this#getAccessToken
  - `processCardPayment` → this#validatePaymentInfo, this#getAccessToken
  - `refundPayment` → this#getAccessToken
  - `cancelTestPayment` → this#refundPayment
  - `cancelPayment` → this#refundPayment

## 3. 클래스 목록 (역할별)

### controller (1)

- `PartnerPaymentInfoController` controller/command/PartnerPaymentInfoController.java · public: createPaymentInfo, deletePaymentInfo, getPaymentInfos, getPaymentInfo, testPayment · 주입: PartnerPaymentInfoCommandService, PartnerPaymentInfoQueryService, PartnerPortOneApiClient

### service-command (1)

- `PartnerPaymentInfoCommandService` service/command/PartnerPaymentInfoCommandService.java · public: createPaymentInfo, deletePaymentInfo · 주입: UserRepository, PartnerPortOneApiClient

### service-query (2)

- `PartnerPaymentInfoQueryService` service/query/PartnerPaymentInfoQueryService.java · public: getPaymentInfos, getPaymentInfo, getPaymentInfoEntity · 주입: PartnerPaymentInfoRepository, PartnerPaymentEncryptionService
- `PartnerPaymentQueryService` service/query/PartnerPaymentQueryService.java · public: getPaymentDetails, getUserPayments, getSellerPayments, findByImpUid · 주입: PaymentRepository

### service (2)

- `PartnerPortOneApiClient` portone/PartnerPortOneApiClient.java · public: getAccessToken, requestTestPayment, processCardPayment, refundPayment, cancelTestPayment, cancelPayment · 주입: RestTemplate
- `PartnerPaymentEncryptionService` service/PartnerPaymentEncryptionService.java · public: encrypt, decrypt

### repository (1)

- `PartnerPaymentInfoRepository` (interface) repository/PartnerPaymentInfoRepository.java · extends/implements JpaRepository · public: findAllByUser_Email, findByIdAndUser_Email

### event (2)

- `PartnerPaymentEventConsumer` service/kafka/PartnerPaymentEventConsumer.java · public: handlePaymentEvent, handlePaymentRetryEvent · 주입: PaymentCommandService, PartnerPaymentEventProducer, OrderRepository, PaymentRepository, PartnerUserQueryService
- `PartnerPaymentEventProducer` service/kafka/PartnerPaymentEventProducer.java · public: sendPaymentEvent, sendPaymentRetryEvent, sendPaymentEventSync · 주입: KafkaTemplate

### other (1)

- `PartnerPaymentCardUtils` util/PartnerPaymentCardUtils.java · public: validateCardInfo, maskCardNumber, maskCardPassword, maskBirthDate, maskCardNumberForLogging

### 다른 도메인 참조 (import·주입 기준)

- **order**: `Order` ← PartnerPaymentEventConsumer; `OrderRepository` ← PartnerPaymentEventConsumer
- **partneruser**: `PartnerUserQueryService` ← PartnerPaymentEventConsumer
- **payment**: `AccountPayment` ← PartnerPaymentQueryService; `CardPayment` ← PartnerPaymentQueryService; `GeneralPayment` ← PartnerPaymentQueryService; `Payment` ← PartnerPaymentEventConsumer, PartnerPaymentQueryService; `PaymentCommandService` ← PartnerPaymentEventConsumer; `PaymentEvent` ← PartnerPaymentEventConsumer, PartnerPaymentEventProducer; `PaymentInfo` ← PartnerPaymentInfoCommandService, PartnerPaymentInfoQueryService, PartnerPaymentInfoRepository, PartnerPortOneApiClient; `PaymentRepository` ← PartnerPaymentEventConsumer, PartnerPaymentQueryService
- **user**: `User` ← PartnerPaymentEventConsumer, PartnerPaymentInfoCommandService; `UserRepository` ← PartnerPaymentInfoCommandService

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

- 발행 kafka `payment-processing-topic`: `PartnerPaymentEventProducer#sendPaymentEvent` → `AdminPaymentEventConsumer#handlePaymentEvent` [adminpayment], `InternalPaymentEventConsumer#handlePaymentEvent` [internalpayment], `LegacyPaymentEventConsumer#handlePaymentEvent` [legacypayment], `PartnerPaymentEventConsumer#handlePaymentEvent` [partnerpayment], `PaymentEventConsumer#handlePaymentEvent` [payment], `SettlementPaymentEventConsumer#handlePaymentEvent` [settlementpayment]
- 발행 kafka `payment-retry-topic`: `PartnerPaymentEventProducer#sendPaymentRetryEvent` → `AdminPaymentEventConsumer#handlePaymentRetryEvent` [adminpayment], `InternalPaymentEventConsumer#handlePaymentRetryEvent` [internalpayment], `LegacyPaymentEventConsumer#handlePaymentRetryEvent` [legacypayment], `PartnerPaymentEventConsumer#handlePaymentRetryEvent` [partnerpayment], `PaymentEventConsumer#handlePaymentRetryEvent` [payment], `SettlementPaymentEventConsumer#handlePaymentRetryEvent` [settlementpayment]
- 발행 kafka `payment-processing-topic`: `PartnerPaymentEventProducer#sendPaymentEventSync` → `AdminPaymentEventConsumer#handlePaymentEvent` [adminpayment], `InternalPaymentEventConsumer#handlePaymentEvent` [internalpayment], `LegacyPaymentEventConsumer#handlePaymentEvent` [legacypayment], `PartnerPaymentEventConsumer#handlePaymentEvent` [partnerpayment], `PaymentEventConsumer#handlePaymentEvent` [payment], `SettlementPaymentEventConsumer#handlePaymentEvent` [settlementpayment]
- 구독 kafka `payment-processing-topic`: `PartnerPaymentEventConsumer#handlePaymentEvent` (groupId=payment-processing-group containerFactory=paymentEventKafkaListenerContainerFactory) ← `AdminPaymentEventProducer#sendPaymentEvent` [adminpayment], `AdminPaymentEventProducer#sendPaymentEventSync` [adminpayment], `InternalPaymentEventProducer#sendPaymentEvent` [internalpayment], `InternalPaymentEventProducer#sendPaymentEventSync` [internalpayment], `LegacyPaymentEventProducer#sendPaymentEvent` [legacypayment], `LegacyPaymentEventProducer#sendPaymentEventSync` [legacypayment], `PartnerPaymentEventProducer#sendPaymentEvent` [partnerpayment], `PartnerPaymentEventProducer#sendPaymentEventSync` [partnerpayment], `PaymentEventProducer#sendPaymentEvent` [payment], `PaymentEventProducer#sendPaymentEventSync` [payment], `SettlementPaymentEventProducer#sendPaymentEvent` [settlementpayment], `SettlementPaymentEventProducer#sendPaymentEventSync` [settlementpayment]
- 구독 kafka `payment-retry-topic`: `PartnerPaymentEventConsumer#handlePaymentRetryEvent` (groupId=payment-retry-group containerFactory=paymentEventKafkaListenerContainerFactory) ← `AdminPaymentEventProducer#sendPaymentRetryEvent` [adminpayment], `InternalPaymentEventProducer#sendPaymentRetryEvent` [internalpayment], `LegacyPaymentEventProducer#sendPaymentRetryEvent` [legacypayment], `PartnerPaymentEventProducer#sendPaymentRetryEvent` [partnerpayment], `PaymentEventProducer#sendPaymentRetryEvent` [payment], `SettlementPaymentEventProducer#sendPaymentRetryEvent` [settlementpayment]

## 6. AOP

(없음)

