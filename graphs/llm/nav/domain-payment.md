# 도메인: payment (결제 / 결제수단 / PortOne)

> 기준 경로: `src/main/java/com/fream/back/domain/payment/` (이하 `payment/`)
> 관련 문서: [domain-order.md](domain-order.md), [flow-trade-buy.md](flow-trade-buy.md)

## 1. 역할 요약

- 주문(`Order`)에 대한 결제 엔티티(`Payment` 추상 + `CardPayment`/`AccountPayment`/`GeneralPayment`) 생성과 환불.
- 사용자 저장 카드(`PaymentInfo`) 등록/삭제/조회 — 등록 시 PortOne으로 100원 테스트 결제 후 즉시 환불해 카드 유효성 검증.
- 외부 PG 연동: `PortOneApiClient` (iamport REST API, `https://api.iamport.kr`).
- 결제 완료 시 구매자/판매자 알림 생성 + `Sale` 상태를 `SOLD`로 변경.
- 결제용 Kafka 토픽/컨슈머가 존재하나 **main 코드에서 발행하는 곳이 없음** (§6).
- 결제 자체를 위한 REST 엔드포인트는 없음 — 결제는 order 도메인 경유로만 실행됨. REST는 `PaymentInfo`(카드 관리)만.

## 2. 패키지/파일 맵

| 레이어 | 파일 (repo 루트 기준) | 용도 |
|---|---|---|
| Controller | `src/main/java/com/fream/back/domain/payment/controller/command/PaymentInfoController.java` | 카드 등록/삭제/조회, 테스트 결제 |
| Service(command) | `src/main/java/com/fream/back/domain/payment/service/command/PaymentCommandService.java` | `processPayment`, `refundPayment`, `updatePaymentStatus`, 알림·Sale SOLD |
| | `src/main/java/com/fream/back/domain/payment/service/command/PaymentInfoCommandService.java` | 카드 등록(테스트결제+환불)/삭제 |
| Service(query) | `src/main/java/com/fream/back/domain/payment/service/query/PaymentQueryService.java` | 결제 상세/구매자·판매자 결제 목록/impUid 조회 (**main 내 호출처 없음**) |
| | `src/main/java/com/fream/back/domain/payment/service/query/PaymentInfoQueryService.java` | 카드 목록/단건(마스킹), `getPaymentInfoEntity`(복호화 사본) |
| 암호화 | `src/main/java/com/fream/back/domain/payment/service/PaymentEncryptionService.java` | AES/CBC/PKCS5 + PBKDF2WithHmacSHA256(65536회, 256bit), 고정 IV. `payment.encryption.{secret-key,salt,iv}` |
| PG 클라이언트 | `src/main/java/com/fream/back/domain/payment/portone/PortOneApiClient.java` | 토큰 발급, 테스트결제, 카드 키인 결제, 환불 |
| Util | `src/main/java/com/fream/back/domain/payment/util/PaymentCardUtils.java` | 카드정보 검증, 마스킹 |
| Kafka | `src/main/java/com/fream/back/domain/payment/service/kafka/PaymentEventProducer.java` | `payment-processing-topic`, `payment-retry-topic` 발행 |
| | `src/main/java/com/fream/back/domain/payment/service/kafka/PaymentEventConsumer.java` | 위 토픽 소비 → `processPayment` |
| | `src/main/java/com/fream/back/domain/payment/dto/kafka/PaymentEvent.java` | 결제 이벤트 DTO |
| | `src/main/java/com/fream/back/domain/payment/dto/kafka/PaymentCompletedEvent.java` | 결제 완료 이벤트 DTO (**main 내 사용처 없음**) |
| Kafka 설정(global) | `src/main/java/com/fream/back/global/config/kafka/PaymentKafkaConfig.java` | `paymentEventKafkaTemplate`, `paymentEventKafkaListenerContainerFactory`(concurrency 1, MANUAL_IMMEDIATE) |
| Entity | `payment/entity/Payment.java` | 추상 결제 (SINGLE_TABLE) |
| | `payment/entity/CardPayment.java` / `AccountPayment.java` / `GeneralPayment.java` | 하위 타입 (`payment_type` = CARD/ACCOUNT/GENERAL) |
| | `payment/entity/PaymentInfo.java` | 저장 카드 |
| | `payment/entity/PaymentStatus.java` | 결제 상태 + 전이 |
| | `payment/entity/Bank.java` | 지원 은행 enum (15개, displayName) |
| Repository | `payment/repository/PaymentRepository.java` | JPQL 쿼리 다수 |
| | `payment/repository/PaymentInfoRepository.java` | `findAllByUser_Email`, `findByIdAndUser_Email` |
| DTO(요청) | `payment/dto/PaymentRequestDto.java` (+ `CardPaymentRequestDto`, `AccountPaymentRequestDto`, `GeneralPaymentRequestDto`) | Jackson 다형(`paymentType` 프로퍼티) |
| DTO(응답) | `payment/dto/PaymentDto.java`, `CardPaymentDto.java`, `AccountPaymentDto.java`, `GeneralPaymentDto.java` | `PaymentQueryService` 응답 |
| DTO(카드) | `payment/dto/paymentInfo/PaymentInfoCreateDto.java`, `PaymentInfoDto.java` | 카드 등록 요청 / 마스킹 응답 |
| Exception | `payment/exception/*` | `PaymentErrorCode` + `PaymentException` + 6개 하위 예외 |

## 3. REST 엔드포인트 (`PaymentInfoController`, class `@RequestMapping("/payment-info")`)

이메일은 컨트롤러 private `extractEmailFromSecurityContext()`로 추출(principal이 String이 아니면 `PAYMENT_INFO_ACCESS_DENIED`).

| HTTP | 경로 | Controller 메서드 | Service 호출 |
|---|---|---|---|
| POST | `/payment-info` | `createPaymentInfo` | `PaymentInfoCommandService.createPaymentInfo(email, dto)` |
| DELETE | `/payment-info/{id}` | `deletePaymentInfo` | `PaymentInfoCommandService.deletePaymentInfo(email, id)` |
| GET | `/payment-info` | `getPaymentInfos` | `PaymentInfoQueryService.getPaymentInfos(email)` |
| GET | `/payment-info/{id}` | `getPaymentInfo` | `PaymentInfoQueryService.getPaymentInfo(email, id)` |
| POST | `/payment-info/test-payment` | `testPayment` | `PortOneApiClient.requestTestPayment` → `cancelTestPayment` (이메일 추출 없음, 저장 없음) |

결제 실행 관련 엔드포인트는 order 도메인: `POST /order-bids/instant`(동기), `POST /orders/{orderId}/process-payment-shipment`(Kafka).

## 4. 핵심 엔티티와 관계

### 상속: `Payment` — `@Inheritance(SINGLE_TABLE)`, `@DiscriminatorColumn(name="payment_type")`

| 클래스 | Discriminator | 추가 필드 |
|---|---|---|
| `Payment` (abstract) | — | `id`, `order`(1:1 FK `order_id`), `sale`(1:1 FK `sale_id`), `user`(N:1, not null), `paidAmount`(double), `isSuccess`, `paymentDate`, `status`(기본 PENDING) |
| `CardPayment` | `CARD` | cardNumber, cardPassword, cardExpiration, birthDate, cardType, impUid, receiptUrl, pgProvider, pgTid |
| `AccountPayment` | `ACCOUNT` | impUid(생성자에서 세팅 안 함), bankName, accountNumber, accountHolder, receiptRequested |
| `GeneralPayment` | `GENERAL` | impUid, pgProvider, receiptUrl, buyerName, buyerEmail (빌더의 `status` 인자는 무시됨) |

- `assignOrder`/`assignSale`은 **상호 배타** (하나 세팅 시 다른 쪽 null).
- `setPaidAmount(x)`가 `isSuccess = x > 0`으로 설정 → 금액만 양수면 성공 플래그 true.
- `updateSuccessStatus(true)` 시 `paymentDate=now`.
- `updateStatus`는 `PaymentStatus.canTransitionTo` 위반 시 `IllegalStateException` (같은 상태로의 전이도 거부).
- 역방향: `Order.payment`(mappedBy, cascade ALL), `Sale`에도 `Payment` 참조(sale 도메인), `User.paymentInfos`(cascade ALL, orphanRemoval).

### PaymentStatus 전이 (`PaymentStatus.canTransitionTo`)

| From | 허용 To |
|---|---|
| PENDING | PAID |
| PAID | REFUND_REQUESTED, REFUNDED |
| REFUND_REQUESTED | REFUNDED |
| REFUNDED, FAILED | (없음) |

### PaymentInfo
`user`(N:1), `cardNumber`, `cardPassword`, `expirationDate`, `birthDate`. 사용자당 최대 5개.

### 요청 DTO 다형성
`PaymentRequestDto`: `@JsonTypeInfo(property="paymentType", visible=true)` → `CARD`/`ACCOUNT`/`GENERAL`. 공통 필드 `paymentType`, `paidAmount`(@Min 100), `orderId`, `userEmail`. 분기는 `getResolvedPaymentType()`(실제 서브클래스 기준).
- Card: `paymentInfoId` / Account: `bankName`, `accountNumber`(숫자·하이픈), `accountHolder`, `receiptRequested` / General: `impUid`(필수), `pgProvider`, `receiptUrl`, `buyerName`, `buyerEmail`.

## 5. 주요 서비스 메서드와 비즈니스 규칙

`PaymentCommandService`는 **클래스 레벨 `@Transactional`**.

| 메서드 | 규칙 / 동작 |
|---|---|
| `processPayment(order, user, dto)` | `validatePaymentRequest`: order/user/dto null 체크, `order.id == dto.orderId`, `user.email == dto.userEmail`, `paidAmount > 0` (주문 금액과의 일치 검사는 없음) → 타입별 생성 → `notifyUsersAndUpdateSaleStatus` → 반환. 비-PaymentException은 `PaymentProcessingException`(PAY001)으로 래핑 |
| `createCardPayment` (private) | `PaymentInfoQueryService.getPaymentInfoEntity`(복호화 사본) → `PortOneApiClient.processCardPayment(info, amount)` → CardPayment 저장. 응답 `status == "success"`일 때만 PAID + success=true |
| `createAccountPayment` (private) | `Bank.valueOf(bankName.toUpperCase())`로 은행 검증(실패 시 `INVALID_BANK_INFO`) → AccountPayment 생성 → `updateStatus(PENDING)` 호출 → **PENDING→PENDING 전이 불가로 항상 실패** (§10) |
| `createGeneralPayment` (private) | impUid 필수 → GeneralPayment 생성, success=true, PAID. **PortOne 서버 검증(조회) 없이 클라이언트 값 신뢰** |
| `notifyUsersAndUpdateSaleStatus` (private) | `order.getOrderBid().getSale()` 또는 (OrderBid 없으면) `saleBidQueryService.findByOrderId(order.id).getSale()` → 구매자/판매자 알림(`NotificationCategory.SHOPPING`, `NotificationType.BID`) → `sale.updateStatus(SaleStatus.SOLD)`. 모든 예외는 로그만 |
| `refundPayment(paymentId)` | 상태가 REFUND_REQUESTED로 갈 수 있어야 함(=PAID) → impUid 필수 → `portOneApiClient.cancelPayment` 성공 시 REFUNDED + 환불 알림(`NotificationType.REFUND`), 실패 시 REFUND_REQUESTED 저장 후 실패 메시지 반환. **main 내 호출처 없음** |
| `updatePaymentStatus(payment, status)` | 전이 검증 후 저장. **main 내 호출처 없음** |
| `handleRefundIfNecessary` (private) | 미사용 |
| `PaymentInfoCommandService.createPaymentInfo` | 최대 5개(`PAYMENT_INFO_LIMIT_EXCEEDED`) → 형식 검증(카드 15~16자리, `MM/YY`, 생년월일 6자리, 비번 2자리) → `requestTestPayment` → `cancelTestPayment` 성공 시 `user.addPaymentInfo` (cascade 저장). **암호화 없이 평문 저장** |
| `PaymentInfoCommandService.deletePaymentInfo` | 사용자 소유 카드 중 id 일치 항목 `user.removePaymentInfo` (orphanRemoval) |
| `PaymentInfoQueryService.getPaymentInfos/getPaymentInfo` | 저장값을 `decrypt` 후 마스킹 |
| `PaymentInfoQueryService.getPaymentInfoEntity` | cardNumber/cardPassword/birthDate `decrypt`한 **비영속 사본** 반환 (expirationDate는 그대로) |

### PortOneApiClient

| 메서드 | 엔드포인트 | 비고 |
|---|---|---|
| `getAccessToken()` | `POST /users/getToken` | `imp.key`, `imp.secret` |
| `requestTestPayment(dto)` | `POST /subscribe/payments/onetime` | pg=`nice`, amount=100, merchant_uid=UUID. impUid 반환 |
| `processCardPayment(info, amount)` | `POST /subscribe/payments/onetime` | pg=`nice` 하드코딩(`imp.pg` 값은 주입되나 미사용), `code != 0`이면 `PaymentProcessingException` |
| `refundPayment(impUid)` / `cancelPayment` / `cancelTestPayment` | `POST /payments/cancel` | 성공 여부 boolean (`code == 0`) |

- 설정 키: `imp.key`, `imp.secret`, `imp.pg`, `imp.storeId`, `payment.api.timeout`(10000), `payment.retry.max-attempts`(3), `payment.retry.delay-ms`(1000) — 뒤 3개 및 `storeId`, `pg`는 코드에서 사용되지 않음.
- `@Retryable(PaymentApiException, maxAttempts=3)`이 붙어 있으나 프로젝트에서 `@EnableRetry`를 찾을 수 없음 → 실제 재시도 동작 여부 (미확인). 또한 같은 클래스 내부 호출(`cancelPayment → refundPayment`)은 프록시를 거치지 않음.

## 6. 다른 도메인과의 의존 / Kafka

### payment → 타 도메인
| 대상 | 용도 |
|---|---|
| order `Order`, `OrderBid` 엔티티 | 결제 대상, Sale 탐색 |
| sale `SaleBidQueryService.findByOrderId`, `Sale.updateStatus(SOLD)` | 판매 완료 처리 (`Sale.updateStatus`는 전이 검증 없음) |
| notification `NotificationCommandService.createNotification` | DB 저장 + 실시간 전송 |
| user `UserRepository`, `UserQueryService`, `User.addPaymentInfo/removePaymentInfo` | 카드 관리 |

### 타 도메인 → payment
| 사용처 | 내용 |
|---|---|
| `order/service/command/OrderCommandService` | `processPayment` (즉시구매, 동기버전) |
| `order/service/kafka/OrderEventConsumer` | `processPayment` (입찰 주문 결제) |
| `order/controller/query/OrderQueryController` | `order.getPayment()` 상태 노출 |
| `sale/entity/Sale` | `Payment` 참조 |
| `user/entity/User` | `paymentInfos` |
| `global/config/DataInitializer` | `GeneralPayment` 샘플 생성 |

### Kafka
| 토픽 | 그룹 | 발행 | 소비 | DTO |
|---|---|---|---|---|
| `payment-processing-topic` | `payment-processing-group` | `PaymentEventProducer.sendPaymentEvent`/`sendPaymentEventSync` — **main 내 호출처 없음** | `PaymentEventConsumer.handlePaymentEvent` | `PaymentEvent` |
| `payment-retry-topic` | `payment-retry-group` | `PaymentEventProducer.sendPaymentRetryEvent` (컨슈머 실패 시) | `PaymentEventConsumer.handlePaymentRetryEvent` | `PaymentEvent` |

- 컨슈머 멱등성: `paymentRepository.findByOrder_Id(orderId)`의 `isSuccess`가 true면 skip.
- 결과적으로 실제 결제 Kafka 경로는 order 도메인의 `order-processing-topic` 하나뿐.
- 스케줄러/Redis: payment 패키지 내 없음.

## 7. 예외/에러코드

- 위치: `payment/exception/PaymentErrorCode.java`, 베이스 `PaymentException extends GlobalException`, 처리: `GlobalExceptionHandler.handlePaymentException`.
- 코드 대역: `PAY0xx` 결제 일반, `PAY1xx` 결제정보(카드), `PAY2xx` 외부 API(PortOne), `PAY3xx` 접근권한(403), `PAY4xx` 은행/계좌.
- 하위 예외: `PaymentApiException`(PAY201), `PaymentProcessingException`(PAY001), `PaymentNotFoundException`, `PaymentInfoNotFoundException`, `PaymentAccessDeniedException`, `UnknownPaymentTypeException`. 그 외는 `new PaymentException(PaymentErrorCode.X, msg)`로 직접 생성.
- `PaymentEventConsumer`에서 `new PaymentException(null, "...")`처럼 errorCode null로 생성하는 곳이 있음.

## 8. 관련 테스트

| 파일 | 종류 | 내용 |
|---|---|---|
| `src/test/java/com/fream/back/domain/payment/PaymentCommandServiceTest.java` | Mockito | General/Card/Account 결제, 검증, 알림·Sale SOLD, 환불, 상태변경. `accountPaymentWithValidBankFailsOnPendingToPendingTransition`이 현재 버그 동작을 고정 |
| `src/test/java/com/fream/back/domain/payment/PaymentInfoServiceTest.java` | Mockito | 카드 등록(평문 저장 확인), 5개 제한, 삭제, 조회 마스킹/복호화, 암호화 round-trip |
| `src/test/java/com/fream/back/domain/payment/PaymentQueryServiceTest.java` | Mockito | 서브타입별 DTO 매핑, 목록, impUid |
| `src/test/java/com/fream/back/domain/payment/PaymentEntityStateTest.java` | 단위 | 전이 테이블, paidAmount→isSuccess, assignOrder/Sale 배타 |
| `src/test/java/com/fream/back/domain/payment/PaymentRepositoryTest.java` | `@DataJpaTest` | JPQL 쿼리 |
| `src/test/java/com/fream/back/domain/payment/PaymentCardUtilsTest.java` | 단위 | 마스킹/검증, resolvedType |

PortOneApiClient, Kafka 컨슈머, 컨트롤러 테스트는 없음.

## 9. 자주 할 작업 → 여기를 보라

| 작업 | 위치 |
|---|---|
| 새 결제 수단 추가 | `Payment` 서브클래스 + `@DiscriminatorValue`, `PaymentRequestDto` `@JsonSubTypes` 및 `getResolvedPaymentType`, `PaymentCommandService.processPayment` switch, `PaymentQueryService` DTO 매핑 |
| 결제 상태 전이 변경 | `payment/entity/PaymentStatus.canTransitionTo` + `PaymentEntityStateTest` |
| PortOne 요청 파라미터(pg 등) | `payment/portone/PortOneApiClient` |
| 결제 후 Sale/알림 처리 | `PaymentCommandService.notifyUsersAndUpdateSaleStatus`, `sendNotifications` |
| 환불 기능 노출 | `PaymentCommandService.refundPayment` (컨트롤러 없음, 신규 연결 필요) |
| 카드 저장 암호화 | `PaymentInfoCommandService.createPaymentInfo`(현재 평문) ↔ `PaymentInfoQueryService`(복호화) |
| 결제 조회 API 추가 | `PaymentQueryService` (서비스는 있으나 컨트롤러 없음) |

## 10. Gotchas (코드에서 확인됨)

1. **계좌 결제 항상 실패**: `createAccountPayment`가 PENDING 상태에서 `updateStatus(PENDING)` 호출 → `PaymentStatus.PENDING.canTransitionTo(PENDING)`이 false → `IllegalStateException` → `PaymentProcessingException`. (테스트가 이 동작을 고정)
2. **카드 정보 암호화 불일치**: 등록(`createPaymentInfo`)은 평문 저장, 조회/결제(`PaymentInfoQueryService`)는 `decrypt` 호출. `PaymentEncryptionService.encrypt`는 main 코드 어디에서도 호출되지 않음 → 평문 데이터에 decrypt 시 실패하면 `PAYMENT_RETRIEVAL_FAILED`. 또한 목록 마스킹 시 cardPassword는 복호화하지 않고 그대로 마스킹.
3. **isSuccess와 status 불일치 가능**: `setPaidAmount`가 금액>0이면 `isSuccess=true`로 설정하므로, 카드 결제 응답이 `status != "success"`이어도 status=PENDING인데 `isSuccess=true`. `OrderEventConsumer`는 `payment.isSuccess()`로 성공을 판정함.
4. 결제 금액(`dto.paidAmount`)을 `Order.totalAmount`와 비교하지 않음. GeneralPayment는 impUid를 PortOne에 조회해 검증하지 않음.
5. `PaymentRepository.findProcessingPaymentsByOrderId`의 JPQL이 `'PROCESSING'` 상태를 참조하지만 `PaymentStatus`에 해당 값 없음.
6. `PaymentStatus.FAILED`를 세팅하는 코드 없음 (`PaymentCompletedEvent.isPaymentFailed`에서 비교만).
7. `GeneralPayment` 빌더의 `status` 파라미터와 `AccountPayment.impUid`는 세팅되지 않음 → 계좌 결제는 impUid가 없어 `refundPayment` 불가.
8. `saleBidQueryService.findByOrderId`는 없으면 `IllegalArgumentException`을 던짐(null 아님) → `notifyUsersAndUpdateSaleStatus`의 null 분기는 실행되지 않고 catch로 빠짐. 해당 쿼리 서비스가 `@Transactional(readOnly=true)` 프록시이므로 바깥 트랜잭션이 rollback-only로 표시될 가능성이 있음 (Spring 기본 전파 규칙 기준, 런타임 미확인).
9. `PaymentQueryService`, `refundPayment`, `updatePaymentStatus`, `PaymentEventProducer.sendPaymentEvent`, `PaymentCompletedEvent`는 테스트 외 호출처 없음.
10. `PaymentInfo`에는 클래스 레벨 `@Builder`와 생성자 레벨 `@Builder`가 함께 선언되어 있음 (`getPaymentInfoEntity`는 빌더에서 `.id(...)` 사용). 수정 시 Lombok 동작 확인 필요.
