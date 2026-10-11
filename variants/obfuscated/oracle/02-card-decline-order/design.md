# 과제 02 설계 메모 — 카드 거절인데 주문이 결제 완료로 넘어감 (bug)

## 의도

증상은 **주문 처리(카프카 컨슈머)** 와 결제 내역 화면에서 보이지만, 원인은 **결제 엔티티**의 성공 플래그 규칙이다. order(kafka) → payment(service/command) → payment(entity, 상속 구조)로 내려가야 한다.

## 원인 (SHA 82ec907의 target/fream-back)

- `domain/m06/entity/Xbf54.java:60-63` `setPaidAmount`가 `isSuccess = paidAmount > 0`으로 둔다. 하위 엔티티 `Xqg94`·`Xjt86`·`Xkg34`의 빌더 생성자가 모두 `setPaidAmount`를 부른다.
- `Xbf54CommandService.createCardPayment`(:187-206)는 PG 응답이 `"success"`일 때만 `PAID` + `updateSuccessStatus(true)`를 하고, 거절이면 아무것도 안 한다. 그러나 이미 생성자에서 `isSuccess=true`가 되어 있다.
- `Xdg64EventConsumer.processCompleteOrder`(:171-178)는 `payment.isSuccess()`만 보고 진행한다 → 배송 생성, `PAYMENT_COMPLETED`→`PREPARING`, 성공 알림.
- `Xbf54Repository.findByOrder_User_EmailAndIsSuccess`(:23) "결제 완료 내역"에도 잡힌다.

## 정답 (`fix.patch`)

- `Xbf54.setPaidAmount`에서 금액 기반 성공 판정을 지운다. 성공은 `updateSuccessStatus(true)`로만 정한다. 성공 경로(카드 승인, 일반 결제, `Xnp67`)는 이미 명시적으로 `updateSuccessStatus(true)`를 부른다(전수 grep 확인).
- 대안 정답: `createCardPayment`의 거절 분기에서 `updateSuccessStatus(false)`를 하는 국소 수정도 hidden을 통과한다(hidden은 서비스·컨슈머·DB 조회 결과만 본다).
- 컨슈머만 `payment.getStatus() == PAID`로 바꾸는 수정은 주문 진행은 막지만 "결제 완료 내역"(`PaidHistoryHiddenTest`)과 `declinedCardPayment_isNotASuccessfulPayment`에서 실패한다.

## 왜 탐색이 필요한가

- TASK는 "카드 거절인데 주문이 배송 준비로 감 + 결제 완료 내역에 나옴"만 말한다. 보이는 테스트는 주문 처리 이벤트 진입점에서 주문 상태와 배송 생성만 본다.
- 컨슈머 코드는 `isSuccess()`를 제대로 검사하는 것처럼 보인다. 왜 true인지 알려면 결제 서비스의 카드 분기를 보고, 거기서 true로 만드는 곳이 없음을 확인한 뒤 엔티티 생성자 → 부모 `setPaidAmount`까지 가야 한다.
- 두 증상(주문 진행, 결제 내역)의 공통 원인을 찾아야 둘 다 고쳐진다.

## 순진한 오답

- 컨슈머에서만 상태 검사 → 결제 내역 증상이 남음(`PaidHistoryHiddenTest`·`declinedCardPayment_isNotASuccessfulPayment` 실패 — 원본 실행에서 두 테스트가 엔티티 플래그 때문에 실패함을 확인).
- (추정, 미실행) 카드 거절 시 `updateStatus(FAILED)` → `PENDING→FAILED` 전이가 허용되지 않아 예외. 결제 처리 자체가 예외로 끝나 주문은 멈추지만, 결제 레코드가 저장되지 않아 `PaidHistoryHiddenTest`에서 실패(거절 결제가 false 목록에 있어야 함). 상태 전이 표를 같이 바꾸면 기존 `PaymentEntityStateTest`(제외 목록)만 영향.
- `setPaidAmount`를 지우면서 일반 결제의 성공 처리까지 잃음 → `generalPayment_orderMovesToPreparing` 실패.

## regression-exclude

- `com.fream.back.domain.m06.PaymentCommandServiceTest`: `cardPaymentWithNonSuccessResponseStaysPendingButSuccessFlagFollowsAmount`가 거절 카드의 `isSuccess=true`를 고정한다.
- `com.fream.back.domain.m06.PaymentEntityStateTest`: `newPaymentStartsPendingAndPaidAmountDrivesSuccessFlag`가 "금액이 양수면 성공"을 고정한다.
- `com.fream.back.domain.m06.PaymentRepositoryTest`: 테스트 데이터를 `updateSuccessStatus` 없이 금액만으로 "성공 결제"로 만든다(`persistGeneral`). 정답 적용 시 성공 조회 2건이 실패한다. 대체 검증은 `PaidHistoryHiddenTest`가 실제 서비스 경로로 한다.

## 보이는 테스트의 배선

`visible/c02/DeclinedCardOrderTest`는 결제 명령 패키지의 구체 클래스만 실제로 만들고 나머지는 목으로 채우는 생성자 조립기를 쓴다(원인 클래스 이름을 쓰지 않기 위해). PG 클라이언트·결제수단 조회·결제 저장소는 거절 응답을 재현하려고 목으로 스텁한다.

## 남는 관련 결함(범위 밖)

- 거절 결제에도 `notifyUsersAndUpdateSaleStatus`가 판매를 SOLD로 바꾸고 결제 완료 알림을 보낸다. 판정에 넣지 않는다.
- 동기 경로 `Xdg64CommandService.processPaymentAndShipment`는 `isSuccess`를 보지 않는다(컨트롤러는 카프카 경로를 쓴다).
