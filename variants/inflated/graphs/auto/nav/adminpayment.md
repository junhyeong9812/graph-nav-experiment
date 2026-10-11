# adminpayment 도메인

기준 경로: `src/main/java/com/fream/back/domain/adminpayment/` · 파일 10개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/admin/payment-info` | `AdminPaymentInfoController#createPaymentInfo` | `this#extractEmailFromSecurityContext`, `AdminPaymentInfoCommandService#createPaymentInfo` |
| DELETE | `/admin/payment-info/{id}` | `AdminPaymentInfoController#deletePaymentInfo` | `this#extractEmailFromSecurityContext`, `AdminPaymentInfoCommandService#deletePaymentInfo` |
| GET | `/admin/payment-info` | `AdminPaymentInfoController#getPaymentInfos` | `this#extractEmailFromSecurityContext`, `AdminPaymentInfoQueryService#getPaymentInfos` |
| GET | `/admin/payment-info/{id}` | `AdminPaymentInfoController#getPaymentInfo` | `this#extractEmailFromSecurityContext`, `AdminPaymentInfoQueryService#getPaymentInfo` |
| POST | `/admin/payment-info/test-payment` | `AdminPaymentInfoController#testPayment` | `AdminPortOneApiClient#requestTestPayment`, `AdminPortOneApiClient#cancelTestPayment` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `AdminPaymentCardUtils`
  - `validateCardInfo` → this#validateExpirationDate
  - `maskCardNumberForLogging` → this#maskCardNumber
- `AdminPaymentEncryptionService`
  - `encrypt` → this#generateKey
  - `decrypt` → this#generateKey
- `AdminPaymentEventConsumer`
  - `handlePaymentEvent` → this#isPaymentAlreadyProcessed, OrderRepository#findById~, AdminUserQueryService#findByEmail, PaymentCommandService#processPayment, this#handlePaymentFailure
  - `handlePaymentRetryEvent` → this#isPaymentAlreadyProcessed, this#handlePaymentEvent
  - `isPaymentAlreadyProcessed` → PaymentRepository#findByOrder_Id
  - `handlePaymentFailure` → this#isRetryableError, AdminPaymentEventProducer#sendPaymentRetryEvent
- `AdminPaymentEventProducer`
  - `sendPaymentEvent` → kafka:payment-processing-topic
  - `sendPaymentRetryEvent` → kafka:payment-retry-topic
  - `sendPaymentEventSync` → kafka:payment-processing-topic
- `AdminPaymentInfoCommandService`
  - `createPaymentInfo` → UserRepository#findByEmail, this#validateCardInfo, AdminPortOneApiClient#requestTestPayment, AdminPortOneApiClient#cancelTestPayment, this#maskCardNumber
  - `deletePaymentInfo` → UserRepository#findByEmail
- `AdminPaymentInfoQueryService`
  - `getPaymentInfos` → AdminPaymentInfoRepository#findAllByUser_Email
  - `getPaymentInfo` → AdminPaymentInfoRepository#findByIdAndUser_Email, this#convertToDto
  - `getPaymentInfoEntity` → AdminPaymentInfoRepository#findByIdAndUser_Email, AdminPaymentEncryptionService#decrypt
  - `convertToDto` → AdminPaymentEncryptionService#decrypt
- `AdminPaymentQueryService`
  - `getPaymentDetails` → PaymentRepository#findById~, this#convertToDto
  - `getUserPayments` → PaymentRepository#findByOrder_User_EmailAndIsSuccess
  - `getSellerPayments` → PaymentRepository#findBySale_Seller_EmailAndIsSuccess
  - `findByImpUid` → PaymentRepository#findByImpUid
- `AdminPortOneApiClient`
  - `requestTestPayment` → this#getAccessToken
  - `processCardPayment` → this#validatePaymentInfo, this#getAccessToken
  - `refundPayment` → this#getAccessToken
  - `cancelTestPayment` → this#refundPayment
  - `cancelPayment` → this#refundPayment

## 3. 클래스 목록 (역할별)

### controller (1)

- `AdminPaymentInfoController` controller/command/AdminPaymentInfoController.java · public: createPaymentInfo, deletePaymentInfo, getPaymentInfos, getPaymentInfo, testPayment · 주입: AdminPaymentInfoCommandService, AdminPaymentInfoQueryService, AdminPortOneApiClient

### service-command (1)

- `AdminPaymentInfoCommandService` service/command/AdminPaymentInfoCommandService.java · public: createPaymentInfo, deletePaymentInfo · 주입: UserRepository, AdminPortOneApiClient

### service-query (2)

- `AdminPaymentInfoQueryService` service/query/AdminPaymentInfoQueryService.java · public: getPaymentInfos, getPaymentInfo, getPaymentInfoEntity · 주입: AdminPaymentInfoRepository, AdminPaymentEncryptionService
- `AdminPaymentQueryService` service/query/AdminPaymentQueryService.java · public: getPaymentDetails, getUserPayments, getSellerPayments, findByImpUid · 주입: PaymentRepository

### service (2)

- `AdminPortOneApiClient` portone/AdminPortOneApiClient.java · public: getAccessToken, requestTestPayment, processCardPayment, refundPayment, cancelTestPayment, cancelPayment · 주입: RestTemplate
- `AdminPaymentEncryptionService` service/AdminPaymentEncryptionService.java · public: encrypt, decrypt

### repository (1)

- `AdminPaymentInfoRepository` (interface) repository/AdminPaymentInfoRepository.java · extends/implements JpaRepository · public: findAllByUser_Email, findByIdAndUser_Email

### event (2)

- `AdminPaymentEventConsumer` service/kafka/AdminPaymentEventConsumer.java · public: handlePaymentEvent, handlePaymentRetryEvent · 주입: PaymentCommandService, AdminPaymentEventProducer, OrderRepository, PaymentRepository, AdminUserQueryService
- `AdminPaymentEventProducer` service/kafka/AdminPaymentEventProducer.java · public: sendPaymentEvent, sendPaymentRetryEvent, sendPaymentEventSync · 주입: KafkaTemplate

### other (1)

- `AdminPaymentCardUtils` util/AdminPaymentCardUtils.java · public: validateCardInfo, maskCardNumber, maskCardPassword, maskBirthDate, maskCardNumberForLogging

### 다른 도메인 참조 (import·주입 기준)

- **adminuser**: `AdminUserQueryService` ← AdminPaymentEventConsumer
- **order**: `Order` ← AdminPaymentEventConsumer; `OrderRepository` ← AdminPaymentEventConsumer
- **payment**: `AccountPayment` ← AdminPaymentQueryService; `CardPayment` ← AdminPaymentQueryService; `GeneralPayment` ← AdminPaymentQueryService; `Payment` ← AdminPaymentEventConsumer, AdminPaymentQueryService; `PaymentCommandService` ← AdminPaymentEventConsumer; `PaymentEvent` ← AdminPaymentEventConsumer, AdminPaymentEventProducer; `PaymentInfo` ← AdminPaymentInfoCommandService, AdminPaymentInfoQueryService, AdminPaymentInfoRepository, AdminPortOneApiClient; `PaymentRepository` ← AdminPaymentEventConsumer, AdminPaymentQueryService
- **user**: `User` ← AdminPaymentEventConsumer, AdminPaymentInfoCommandService; `UserRepository` ← AdminPaymentInfoCommandService

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

- 발행 kafka `payment-processing-topic`: `AdminPaymentEventProducer#sendPaymentEvent` → `AdminPaymentEventConsumer#handlePaymentEvent` [adminpayment], `InternalPaymentEventConsumer#handlePaymentEvent` [internalpayment], `LegacyPaymentEventConsumer#handlePaymentEvent` [legacypayment], `PartnerPaymentEventConsumer#handlePaymentEvent` [partnerpayment], `PaymentEventConsumer#handlePaymentEvent` [payment], `SettlementPaymentEventConsumer#handlePaymentEvent` [settlementpayment]
- 발행 kafka `payment-retry-topic`: `AdminPaymentEventProducer#sendPaymentRetryEvent` → `AdminPaymentEventConsumer#handlePaymentRetryEvent` [adminpayment], `InternalPaymentEventConsumer#handlePaymentRetryEvent` [internalpayment], `LegacyPaymentEventConsumer#handlePaymentRetryEvent` [legacypayment], `PartnerPaymentEventConsumer#handlePaymentRetryEvent` [partnerpayment], `PaymentEventConsumer#handlePaymentRetryEvent` [payment], `SettlementPaymentEventConsumer#handlePaymentRetryEvent` [settlementpayment]
- 발행 kafka `payment-processing-topic`: `AdminPaymentEventProducer#sendPaymentEventSync` → `AdminPaymentEventConsumer#handlePaymentEvent` [adminpayment], `InternalPaymentEventConsumer#handlePaymentEvent` [internalpayment], `LegacyPaymentEventConsumer#handlePaymentEvent` [legacypayment], `PartnerPaymentEventConsumer#handlePaymentEvent` [partnerpayment], `PaymentEventConsumer#handlePaymentEvent` [payment], `SettlementPaymentEventConsumer#handlePaymentEvent` [settlementpayment]
- 구독 kafka `payment-processing-topic`: `AdminPaymentEventConsumer#handlePaymentEvent` (groupId=payment-processing-group containerFactory=paymentEventKafkaListenerContainerFactory) ← `AdminPaymentEventProducer#sendPaymentEvent` [adminpayment], `AdminPaymentEventProducer#sendPaymentEventSync` [adminpayment], `InternalPaymentEventProducer#sendPaymentEvent` [internalpayment], `InternalPaymentEventProducer#sendPaymentEventSync` [internalpayment], `LegacyPaymentEventProducer#sendPaymentEvent` [legacypayment], `LegacyPaymentEventProducer#sendPaymentEventSync` [legacypayment], `PartnerPaymentEventProducer#sendPaymentEvent` [partnerpayment], `PartnerPaymentEventProducer#sendPaymentEventSync` [partnerpayment], `PaymentEventProducer#sendPaymentEvent` [payment], `PaymentEventProducer#sendPaymentEventSync` [payment], `SettlementPaymentEventProducer#sendPaymentEvent` [settlementpayment], `SettlementPaymentEventProducer#sendPaymentEventSync` [settlementpayment]
- 구독 kafka `payment-retry-topic`: `AdminPaymentEventConsumer#handlePaymentRetryEvent` (groupId=payment-retry-group containerFactory=paymentEventKafkaListenerContainerFactory) ← `AdminPaymentEventProducer#sendPaymentRetryEvent` [adminpayment], `InternalPaymentEventProducer#sendPaymentRetryEvent` [internalpayment], `LegacyPaymentEventProducer#sendPaymentRetryEvent` [legacypayment], `PartnerPaymentEventProducer#sendPaymentRetryEvent` [partnerpayment], `PaymentEventProducer#sendPaymentRetryEvent` [payment], `SettlementPaymentEventProducer#sendPaymentRetryEvent` [settlementpayment]

## 6. AOP

(없음)

