# 07 즉시 구매 흐름: 채점 체크리스트

- 경로는 `target/fream-back` 루트 기준이다. 아래에서 `…/`는 `src/main/java/com/fream/back/`를 줄인 것이다.
- 항목마다 만점 1점, 부분 0.5점, 0점이다. 총 8점.
- 근거 `파일:줄`이 없거나 엉뚱한 파일을 가리키면 그 항목은 최대 0.5점이다. 줄 번호가 ±5줄 안이면 맞는 근거로 본다.

## C1. 진입 경로와 처리 방식

- **맞혀야 할 것:**
  - `POST /order-bids/instant`로 들어온다.
  - 컨트롤러 → 주문 입찰 서비스의 즉시 구매 생성 → 주문 서비스의 즉시 주문 생성 순으로 호출한다.
  - 한 트랜잭션 안에서 **동기**로 처리한다. Kafka를 거치지 않는다. 일반 입찰 주문의 결제·배송 요청과 다르다.
  - 응답의 `orderId`는 실제로는 **주문 입찰(FaqBid) ID**다.
- **근거:**
  - `…/domain/order/controller/command/FaqBidCommandController.java:89-110` (`.getId()`는 OrderBid의 것)
  - `…/domain/order/service/command/FaqBidCommandService.java:118-149`
  - `…/domain/order/service/command/FaqCommandService.java:219-220`
- **부분 점수:** 진입 API와 서비스 사슬은 맞혔지만 응답 ID 정체나 동기 여부 중 하나가 없거나 틀리면 0.5점.

## C2. 주문 쪽 생성물과 초기 상태

- **맞혀야 할 것:** 배송지 조회 결과로 다음 세 엔티티를 이 순서로 만든다.
  1. Faq: `PENDING_PAYMENT`, 금액 = 판매 입찰가
  2. FaqItem: 수량 1
  3. FaqAddress: 상태 `PENDING`, 배송지 정보 복사
- **근거:**
  - `…/domain/order/service/command/FaqCommandService.java:238-271`
  - `…/domain/shipment/service/command/FaqAddressCommandService.java:61-68`
- **부분 점수:** 셋 중 둘만 맞거나 상태 값이 빠지면 0.5점.

## C3. 판매 입찰 매칭과 판매자 창고 보관

- **맞혀야 할 것:**
  - SaleBid에 주문을 연결하면 그 자리에서 상태가 `MATCHED`가 된다(`assignOrder`). 이어 `updateStatus(MATCHED)`를 다시 부르고 flush한다.
  - 판매(Style)가 창고 보관 상품이면 그 창고 보관 상태를 `ASSOCIATED_WITH_ORDER`로 바꾼다.
- **근거:**
  - `…/domain/order/service/command/FaqCommandService.java:279-294`
  - `…/domain/sale/entity/StyleBid.java:58-61`
- **부분 점수:** StyleBid MATCHED만 맞히고 창고 보관 상태 변경이 없으면 0.5점.

## C4. 결제 수단별 결과

- **맞혀야 할 것:**
  - **카드:** 결제 대행사 응답이 `"success"`일 때만 `PAID`가 된다. 실패 응답이면 결제는 `PENDING`으로 저장된다. 그런데도 금액이 양수라 `isSuccess=true`가 되고, 즉시 구매는 성공으로 끝난다(성공 여부를 검사하지 않는다).
  - **계좌이체:** 새 결제는 이미 `PENDING`인데 다시 `PENDING`으로 바꾸려다 예외가 난다. 그래서 즉시 구매 전체가 실패하고 롤백된다.
  - **일반:** `PAID`이고 `isSuccess=true`다.
- **근거:**
  - `…/domain/payment/service/command/NotificationCommandService.java:67-80, 203-206, 260, 313-314`
  - `…/domain/payment/entity/Notification.java:44, 60-63, 70-75`
  - `…/domain/payment/entity/NotificationStatus.java:12-13`
  - `…/domain/order/service/command/FaqCommandService.java:304-309`
- **부분 점수:** 세 수단 중 두 가지를 실제 동작대로 맞히면 0.5점. 계좌이체를 "PENDING으로 성공"이라고 답하면 이 항목은 최대 0.5점.

## C5. 판매 완료(SOLD) 처리와 그 경로

- **맞혀야 할 것:**
  - 결제 직후 Sale을 `SOLD`로 바꾼다. Sale은 전이 검사 없이 바꾼다.
  - 이 시점에는 OrderBid가 아직 없다. OrderBid는 주문 생성이 끝난 뒤 만들어진다. 그래서 `order.getOrderBid()`가 null이고, 주문 ID로 SaleBid를 찾아 Sale에 도달한다.
  - 알림·상태 변경 중 예외는 로그만 남기고 삼킨다.
- **근거:**
  - `…/domain/payment/service/command/NotificationCommandService.java:83, 383-425`
  - `…/domain/sale/entity/Style.java:73-75`
  - `…/domain/order/service/command/FaqBidCommandService.java:142`
- **부분 점수:** SOLD 전환만 맞히고 "OrderBid가 없어 StyleBid 경로로 간다"는 점이 없으면 0.5점.

## C6. 알림 대상·내용·전달 방식

- **맞혀야 할 것:**
  - 구매자는 "결제가 완료되었습니다"를 받는다.
  - 판매자는 "구매자가 결제를 완료하였습니다"를 받는다.
  - 둘 다 카테고리 `SHOPPING`, 타입 `BID`다. Sale을 SOLD로 바꾸기 **전에** 보낸다.
  - 알림은 DB에 저장된다. Redis에 WebSocket 연결 키(`WebSocket:Product:<email>`)가 있는 사용자에게만 `/queue/notifications`로 실시간 전송한다.
- **근거:**
  - `…/domain/payment/service/command/NotificationCommandService.java:403-404, 434-456`
  - `…/domain/notification/service/command/PaymentCommandService.java:44-59, 303-313`
- **부분 점수:** 대상 두 명만 맞고 전달 방식(저장 + 연결 시 WebSocket)이 없으면 0.5점.

## C7. 주문 입찰(FaqBid) 생성

- **맞혀야 할 것:**
  - 주문 생성이 끝난 뒤 OrderBid를 만든다.
  - 상태 `MATCHED`, 입찰가 = 판매 입찰가, Faq·Style 연결, `isInstantPurchase=true`다.
- **근거:** `…/domain/order/service/command/FaqBidCommandService.java:189-207`
- **부분 점수:** 생성은 맞지만 상태나 즉시 구매 플래그 중 하나가 빠지면 0.5점.

## C8. 최종 주문 상태와 바뀌지 않는 것

- **맞혀야 할 것:**
  - 주문은 끝까지 `PENDING_PAYMENT`다. 즉시 주문 생성은 주문 상태를 바꾸지 않는다.
  - 구매자가 보낸 창고 보관 여부는 즉시 주문 생성에 전달되지만 쓰이지 않는다. 구매자용 창고 보관(Notice)은 만들어지지 않고 상태도 같다. 일반 입찰 주문의 결제·배송 처리는 `PAYMENT_COMPLETED`→`PREPARING`(창고면 `COMPLETED`)로 바꾸므로 이와 다르다.
  - 판매 입찰의 즉시 판매 플래그(`isInstantSale`)는 세팅되지 않는다.
- **근거:**
  - `…/domain/order/service/command/FaqCommandService.java:220-221, 241-245` (`isWarehouseStorage` 미사용, `updateStatus` 호출 없음)
  - 대비: `…/domain/order/service/kafka/FaqEventConsumer.java:192-220`
  - `…/domain/sale/entity/StyleBid.java:62-64` (호출처 없음)
- **부분 점수:** `PENDING_PAYMENT`와 창고 보관 미사용 중 하나만 맞히면 0.5점. 최종 상태를 `PAYMENT_COMPLETED`·`PREPARING`·`COMPLETED`로 답하면 0점.

## 채점 메모

- 주소 조회(배송지 복호화)가 실제 데이터에서 실패할 수 있다는 언급은 감점하지 않는다. 가점도 없다.
- 결제 처리 순서는 다음과 같다. 문항별 순서가 이와 어긋나면 해당 항목에서 0.5점을 깎는다.
  1. Faq·FaqItem·FaqAddress 생성
  2. StyleBid MATCHED
  3. 창고 보관 상태 변경
  4. 결제
  5. 알림
  6. Style SOLD
  7. FaqBid 생성
