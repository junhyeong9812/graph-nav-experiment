# payment 도메인 네비게이션 맵

> 기준 경로: `src/main/java/com/fream/back/domain/payment` (39 files, portone 포함). 코드에서 확인된 사실만 기재.

## 1. 개요
- 주문 결제(Payment, SINGLE_TABLE 상속: CARD/ACCOUNT/GENERAL), 사용자 카드 정보(PaymentInfo) 관리, PortOne(아임포트 v1 REST) 연동.
- 결제 실행 진입점은 `PaymentCommandService#processPayment` 하나 — order 도메인(Kafka consumer, 즉시구매)에서 호출. **payment 자체 결제 REST API는 없음** (컨트롤러는 PaymentInfo용뿐).
- Payment Kafka(producer/consumer)는 존재하지만 producer를 호출하는 코드가 없음.

## 2. 패키지/파일 구조
```
src/main/java/com/fream/back/domain/payment/
├── controller/command/PaymentInfoController.java   (/payment-info)
├── dto/ PaymentRequestDto(abstract, Jackson 다형성) + Card/Account/GeneralPaymentRequestDto,
│        PaymentDto, Card/Account/GeneralPaymentDto
├── dto/kafka/ PaymentEvent, PaymentCompletedEvent(미사용)
├── dto/paymentInfo/ PaymentInfoCreateDto, PaymentInfoDto
├── entity/ Payment(abstract), CardPayment, AccountPayment, GeneralPayment, PaymentInfo, PaymentStatus, Bank
├── exception/ PaymentErrorCode, PaymentException + 7 하위 예외
├── portone/PortOneApiClient.java
├── repository/ PaymentRepository(@Query), PaymentInfoRepository
├── service/ PaymentEncryptionService
├── service/command/ PaymentCommandService, PaymentInfoCommandService
├── service/kafka/   PaymentEventProducer, PaymentEventConsumer
├── service/query/   PaymentQueryService(외부 호출처 없음), PaymentInfoQueryService
└── util/PaymentCardUtils.java (마스킹)
```
관련 global: `src/main/java/com/fream/back/global/config/kafka/PaymentKafkaConfig.java`.

## 3. 엔티티
| 엔티티 | 주요 필드 | 관계 |
|---|---|---|
| `Payment` (abstract, `@Inheritance(SINGLE_TABLE)`, discriminator `payment_type`) | paidAmount, isSuccess(기본 false), paymentDate, status(PaymentStatus, 기본 PENDING) | order(1:1 FK order_id), sale(1:1 FK sale_id), user(ManyToOne, not null). `assignOrder`↔`assignSale` 상호 배타(다른 쪽 null) |
| `CardPayment` ("CARD") | cardNumber, cardPassword, cardExpiration, birthDate, cardType, impUid, receiptUrl, pgProvider, pgTid | |
| `AccountPayment` ("ACCOUNT") | impUid, bankName, accountNumber, accountHolder, receiptRequested | |
| `GeneralPayment` ("GENERAL") | impUid, pgProvider, receiptUrl, buyerName, buyerEmail | |
| `PaymentInfo` | cardNumber, cardPassword, expirationDate, birthDate | user(ManyToOne). `User.paymentInfos` 컬렉션에서 관리 |

- `Bank` enum: INDUSTRIAL, CORPORATE, KB, NH, WOORI, SC, DAEGU, BUSAN, GWANGJU, JEJU, JEONBUK, GYEONGNAM, SHINHAN, HANA, KAKAO.
- **PaymentStatus** (`canTransitionTo`): PENDING→PAID; PAID→REFUND_REQUESTED|REFUNDED; REFUND_REQUESTED→REFUNDED; REFUNDED/FAILED→없음. (FAILED를 설정하는 코드 없음)
- 상태 메서드: `Payment#updateStatus` (Payment.java:70, 불허 시 IllegalStateException), `#setPaidAmount` (paidAmount>0이면 **isSuccess=true**), `#updateSuccessStatus(true)` → paymentDate=now.

## 4. API 엔드포인트 (`PaymentInfoController`, 모두 인증 필요)
| Method | Path | Controller#method | Service |
|---|---|---|---|
| POST | /payment-info | #createPaymentInfo | PaymentInfoCommandService#createPaymentInfo |
| DELETE | /payment-info/{id} | #deletePaymentInfo | PaymentInfoCommandService#deletePaymentInfo |
| GET | /payment-info | #getPaymentInfos | PaymentInfoQueryService#getPaymentInfos |
| GET | /payment-info/{id} | #getPaymentInfo | PaymentInfoQueryService#getPaymentInfo |
| POST | /payment-info/test-payment | #testPayment | PortOneApiClient#requestTestPayment → #cancelTestPayment 직접 호출 |
- 이메일은 컨트롤러 private `extractEmailFromSecurityContext()` (principal이 String일 때).
- 결제 요청 body는 order API(`/orders/{id}/process-payment-shipment`, `/order-bids/instant`)의 `paymentRequest` 필드로 전달: `paymentType`(CARD|ACCOUNT|GENERAL, `@JsonTypeInfo` 판별자), paidAmount(≥100), orderId, userEmail + 타입별(CARD: paymentInfoId / ACCOUNT: bankName, accountNumber, accountHolder, receiptRequested / GENERAL: impUid, pgProvider, receiptUrl, buyerName, buyerEmail).

## 5. 서비스 계층
- **PaymentCommandService** (클래스 레벨 @Transactional)
  - `processPayment(order, user, requestDto)` (:57): `validatePaymentRequest`(order.id==dto.orderId, email 일치, 금액>0) → `getResolvedPaymentType()` 분기:
    - CARD `createCardPayment` (:157): `PaymentInfoQueryService#getPaymentInfoEntity`(복호화) → `PortOneApiClient#processCardPayment` → CardPayment 저장. 응답 `status=="success"`면 PAID + updateSuccessStatus(true).
    - ACCOUNT `createAccountPayment` (:236): `Bank.valueOf` 검증 → `updateStatus(PENDING)` 호출.
    - GENERAL `createGeneralPayment` (:290): impUid 필수, PortOne 검증 호출 없이 바로 PAID + success.
    - 이후 `notifyUsersAndUpdateSaleStatus(order, payment)` (:383): order.orderBid.sale 또는 `SaleBidQueryService#findByOrderId(order.id).sale` 찾아 구매자/판매자 알림 + `sale.updateStatus(SOLD)`. 예외는 로그만.
  - `updatePaymentStatus(payment, status)` (:498), `refundPayment(paymentId)` (:543: PortOne cancel 성공→REFUNDED+환불 알림, 실패→REFUND_REQUESTED) — **둘 다 호출처 없음** (환불 API 미노출). private `handleRefundIfNecessary`도 미사용.
- **PaymentInfoCommandService**
  - `createPaymentInfo(email, dto)`: 최대 5개 제한 → `validateCardInfo` → PortOne 100원 테스트 결제 → 즉시 환불 → 성공 시 `user.addPaymentInfo(PaymentInfo)` (평문 그대로 저장).
  - `deletePaymentInfo(email, id)`.
- **PaymentInfoQueryService**: `getPaymentInfos`, `getPaymentInfo`(마스킹 DTO), `getPaymentInfoEntity`(PaymentEncryptionService#decrypt로 복호화한 새 PaymentInfo 반환).
- **PaymentEncryptionService**: AES, `payment.encryption.secret-key/salt/iv`. `encrypt()`/`decrypt()`; decrypt 실패 시 IllegalStateException.
- **PaymentQueryService**: `getPaymentDetails`, `getUserPayments`, `getSellerPayments`, `findByImpUid` — 외부 호출처 없음.

## 6. Repositories
- `PaymentRepository` (전부 `@Query`):
  - `findByOrder_User_EmailAndIsSuccess(email, isSuccess)`
  - `findBySale_Seller_EmailAndIsSuccess` — 실제 JPQL은 `p.order.orderBid.sale.seller.email` 경로로 조인
  - `findByImpUid` (CardPayment만), `findByOrder_Id`, `existsSuccessfulPaymentByOrderId`, `findByOrder_IdAndStatus`, `findPaymentsCreatedSince`
  - `findProcessingPaymentsByOrderId`: `status IN ('PENDING','PROCESSING')` — PaymentStatus에 PROCESSING 값 없음 (PaymentRepository.java:65)
- `PaymentInfoRepository`: `findAllByUser_Email`, `findByIdAndUser_Email`.

## 7. 외부 연동 / 비동기
**PortOne** (`portone/PortOneApiClient.java`, BASE_URL `https://api.iamport.kr`, 설정 `imp.key`, `imp.secret`, `imp.pg`, `imp.storeId`):
| 메서드 | Endpoint | 용도 |
|---|---|---|
| `getAccessToken()` | POST /users/getToken | 토큰 |
| `requestTestPayment(PaymentInfoCreateDto)` | POST /subscribe/payments/onetime | 100원, pg "nice" 키인 결제 |
| `processCardPayment(PaymentInfo, amount)` | POST /subscribe/payments/onetime | 실제 카드 결제 (merchant_uid=UUID) |
| `refundPayment(impUid)` | POST /payments/cancel | 환불 |
| `cancelTestPayment` / `cancelPayment` | → `refundPayment` 재사용 | |

**Kafka**:
| 토픽 | Producer | Consumer (groupId, factory) |
|---|---|---|
| `payment-processing-topic` | PaymentEventProducer#sendPaymentEvent / #sendPaymentEventSync (**호출처 없음**) | PaymentEventConsumer#handlePaymentEvent (`payment-processing-group`, `paymentEventKafkaListenerContainerFactory`) |
| `payment-retry-topic` | PaymentEventProducer#sendPaymentRetryEvent (consumer 실패 시) | PaymentEventConsumer#handlePaymentRetryEvent (`payment-retry-group`) |
- Consumer: 멱등성(`findByOrder_Id` 결과 isSuccess면 skip) → 주문/사용자 확인 → `processPayment`. 실패 시 IllegalArgumentException/"찾을 수 없습니다" 아니면 retry 발행. 재시도 시 `Thread.sleep(min(1000*n,5000))`. `PaymentEvent.isMaxRetryExceeded()` = retryCount ≥ 3.
- `PaymentKafkaConfig`: concurrency 1, AckMode MANUAL_IMMEDIATE, auto-commit false.

## 8. 다른 도메인 의존
- order: `Order`, `OrderBid` (엔티티 탐색), `OrderRepository`(consumer). sale: `SaleBidQueryService#findByOrderId`, `Sale#updateStatus(SOLD)`.
- notification: `NotificationCommandService#createNotification` (SHOPPING/BID, 환불 시 REFUND). user: `User`, `UserRepository`, `UserQueryService`.
- 역방향: order(`OrderCommandService`, `OrderEventConsumer`), user(`User.paymentInfos`), `DataInitializer`.

## 9. 예외
- `PaymentErrorCode` prefix **PAY**: PAY001~009(결제 처리/취소/조회/검증/상태/금액/유형), PAY101~106(결제정보, 106=LIMIT_EXCEEDED), PAY201~204(API/토큰), PAY301~302(접근), PAY401~402(은행/계좌).
- `PaymentException extends GlobalException`; 하위: PaymentApiException, PaymentProcessingException, PaymentInfoNotFoundException, PaymentNotFoundException, PaymentAccessDeniedException, UnknownPaymentTypeException.

## 10. 작업 팁 / 주의점
- **ACCOUNT 결제 실패 가능성**: `createAccountPayment`(PaymentCommandService.java:260)가 기본값 PENDING 상태에서 `updateStatus(PENDING)` 호출 → `PENDING.canTransitionTo(PENDING)`=false → IllegalStateException → PaymentProcessingException으로 래핑.
- PaymentInfo는 저장 시 암호화하지 않지만(`PaymentInfoCommandService`) 조회 시 `decrypt()` 수행(`PaymentInfoQueryService:151,188`) → 평문 데이터면 "민감 정보 복호화 실패". `encrypt()` 호출처 없음.
- `isSuccess`는 금액 >0이면 생성 시점부터 true (`setPaidAmount`). 카드 결제가 PortOne 응답 status≠success여도 isSuccess=true, status=PENDING일 수 있음. OrderEventConsumer는 `payment.isSuccess()`로 성공 판정.
- `notifyUsersAndUpdateSaleStatus`의 `Sale#updateStatus(SOLD)`는 SaleStatus 전이 검증 없이 대입.
- `PaymentEventConsumer`의 사용자 불일치 시 `new PaymentException(null, ...)` (ErrorCode null).
- 설정 `payment.api.timeout`, `payment.retry.*`는 `@Value` 기본값 있음.
