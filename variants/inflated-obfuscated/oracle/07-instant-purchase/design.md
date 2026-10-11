# 07 즉시 구매 흐름: 과제 설계 메모

- 유형: question
- 기준: `target/fream-back`(82ec907 시점)

## 의도

즉시 구매 한 번이 5개 도메인을 거친다.

| 도메인 | 여기서 일어나는 일 |
|---|---|
| order | Xhq12·Xnd96·Xtw92 |
| shipment | Xfr60 |
| sale | Xzt63·Xpj42 |
| warehouseStorage | 판매자 창고 보관 상태 |
| payment·notification | 결제와 알림 |

에이전트가 이 사슬을 끝까지 따라가 상태 변화 순서와 알림을 맞히는지 본다. 사슬 중간에 다음 함정이 있다.

1. 결제 서비스 안에서 판매 상태와 알림을 처리한다. 결제 서비스까지 내려가야 한다.
2. 그 안에서 OrderBid가 아직 없어서 Xzt63 경로로 우회한다.
3. 주문 상태는 끝까지 바뀌지 않는다.

이 셋은 코드를 실제로 읽지 않으면 맞힐 수 없다.

## 정답 요약 (체크리스트 C1~C8)

1. `POST /order-bids/instant`로 들어와 다음 순서로 동기 호출한다. 응답 `orderId`는 실제로는 Xtw92 ID다.
   - `Xtw92CommandController.createInstantOrderBid`
   - `Xtw92CommandService.createInstantOrderBid`(@Transactional)
   - `Xhq12CommandService.createInstantOrder`
2. 배송지를 조회한 뒤 다음을 만든다.
   - Xhq12(`PENDING_PAYMENT`)
   - Xnd96
   - Xfr60(`PENDING`)
3. SaleBid를 바꾼다.
   - `assignOrder`로 그 자리에서 `MATCHED`가 된다.
   - `updateStatus(MATCHED)`를 다시 부르고 flush한다.
   - 판매가 창고 보관 상품이면 창고 상태를 `ASSOCIATED_WITH_ORDER`로 바꾼다.
4. `Xjt45CommandService.processPayment`로 결제한다.
   - 카드: 결제 대행사가 success일 때만 PAID다. 실패 응답이어도 PENDING으로 저장하고 isSuccess=true다(`setPaidAmount`). 흐름은 계속된다.
   - 계좌이체: PENDING→PENDING 전이 예외가 나서 전체가 롤백된다.
   - 일반: PAID다.
5. `notifyUsersAndUpdateSaleStatus`가 이어진다.
   - OrderBid가 아직 없으므로 `saleBidQueryService.findByOrderId`로 Sale에 도달한다.
   - 구매자와 판매자에게 알림을 보낸다(SHOPPING/BID).
   - Sale을 `SOLD`로 바꾼다(전이 검사 없음).
   - 예외는 삼킨다.
6. 알림은 `Xbp24CommandService.createNotification`이 처리한다. DB에 저장하고, Redis에 연결 키가 있을 때만 WebSocket으로 보낸다.
7. OrderBid를 만든다. `MATCHED`, `isInstantPurchase=true`이고 Xhq12·Sale에 연결된다.
8. 나머지 상태는 다음과 같다.
   - 주문은 `PENDING_PAYMENT`로 남는다.
   - 구매자의 `isWarehouseStorage`는 쓰이지 않는다.
   - SaleBid의 `isInstantSale`은 false로 남는다.

## 왜 탐색이 필요한가

- TASK.md에는 클래스·파일 이름이 없다. "즉시 구매"라는 단어로 grep을 시작하면 `/instant` 엔드포인트가 두 개 나온다(order-bids, sale-bids). 둘 중 구매 쪽을 골라야 한다.
- 판매 상태(SOLD)와 알림은 order·sale 패키지가 아니라 **payment 패키지의 private 메서드**에서 바뀐다. 컨트롤러에서 시작해 서비스 3단을 내려가야 보인다.
- "OrderBid가 아직 없다"는 사실은 두 가지를 함께 봐야 알 수 있다.
  - `Xtw92CommandService:142`의 호출 순서
  - `Xjt45CommandService:388`의 null 분기
- 결제 수단별 결과는 세 파일을 맞춰 봐야 한다.
  - `Xjt45` 엔티티의 필드 기본값과 `setPaidAmount`
  - `Xjt45Status` 전이표
  - 각 `create*Xjt45`
- 최종 주문 상태가 그대로라는 점은 "무엇이 없는가"를 확인해야 한다. 일반 입찰 주문 처리(Kafka 컨슈머)와 대비하면 드러난다.

## 순진한 오답 (예상)

- "결제 후 주문이 `PAYMENT_COMPLETED`→`PREPARING`이 된다." 일반 입찰 주문의 결제·배송 처리 흐름을 즉시 구매에 그대로 옮긴 답이다.
- "구매자가 창고 보관을 고르면 창고 보관이 생성되고 주문이 `COMPLETED`가 된다." `processPaymentAndShipment`를 보고 추정한 답이다.
- "OrderBid의 Sale을 통해 SOLD로 바꾼다." Xtw92 분기만 읽은 답이다.
- "계좌이체는 PENDING으로 저장되고 성공한다." 엔티티의 기본값을 놓친 답이다.
- "카드 실패면 isSuccess=false다." `setPaidAmount`를 놓친 답이다.
- "Kafka로 비동기 처리한다." 다른 결제 엔드포인트와 혼동한 답이다.

## 범위 밖

- 주소 조회의 복호화 문제는 버그 유형 과제와 겹칠 수 있어 묻지 않는다. 그래서 TASK.md는 "처리 흐름"을 묻고, 성공 여부는 결제 수단에 한정한다.
