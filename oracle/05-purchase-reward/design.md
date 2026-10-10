# 05 purchase-reward — 설계 메모 (change)

## 의도

"배송 완료 시 구매 금액 1% 포인트 적립 + 혜택 알림". 배송(shipment) 흐름 위에 사용자 포인트(user/point)와 알림(notification)을 얹어야 한다. 도메인 3개(shipment·user·notification)와 주문(order) 엔티티를 거친다.

## 정답 (fix.patch)

- `PointCommandService.addPurchaseRewardPoint(User, Long orderId, int total)` 추가: `total/100`, 0이면 적립 안 함. 기존 `Point(User,int,String)` 생성자(90일·AVAILABLE)를 그대로 쓴다.
- `OrderShipmentCommandService`에 `PointCommandService` 주입, private `rewardPurchasePoint(Order)` 추가(적립 + `NotificationType.BENEFIT` 알림, 문구에 "{n}P").
- 호출 위치 2곳:
  1. `completeOrder` 끝(구매자 완료 알림 뒤). `updateAndCheckShipmentStatus`(DELIVERED)와 배치 `ShipmentItemProcessor.process`가 이 메서드를 거친다.
  2. `updateShipmentStatuses`의 DELIVERED 분기. 이 경로는 `completeOrder`를 부르지 않고 주문 상태를 직접 COMPLETED로 바꾼다.

## 왜 탐색이 필요한가

- TASK는 "배송 완료"라는 업무 용어만 준다. 배송 완료가 처리되는 곳을 찾아야 한다: `OrderShipmentCommandService`의 `completeOrder`·`updateShipmentStatuses`·`updateAndCheckShipmentStatus`, 배치 `shipment/config/ShipmentItemProcessor`.
- **배송 완료 경로가 둘로 갈라져 있다**(completeOrder 경유 / updateShipmentStatuses 직접 처리). 한 곳만 고치면 hidden에서 실패한다.
- 포인트 적립 규칙(유효기간 90일·AVAILABLE)은 `user/service/point/PointCommandService`와 `user/entity/Point`에 있다. "기존 적립 규칙"을 따르려면 이곳을 읽어야 한다.
- "혜택 알림"은 `NotificationType.BENEFIT("혜택 관련 알림")`에 대응한다. 현재 쓰는 곳이 없어 enum을 열어 봐야 안다.

## 순진한 오답

- `completeOrder`에만 넣는 경우: 주기 갱신(`updateShipmentStatuses`) 경로에서 적립되지 않는다 → `periodicTrackingUpdate*` 실패.
- `updateAndCheckShipmentStatus`와 `completeOrder` 양쪽에 넣는 경우: 이중 적립이 된다 → `immediateTrackingCheckRewardsExactlyOnce` 실패.
- `PointCommandService.addPoint(email, …)`를 그대로 쓰는 경우: 동작은 맞다(검증상 0P는 미리 걸러야 함). 0P를 거르지 않으면 `InvalidPointAmountException`이 나서 완료 처리가 깨진다 → `rewardBelowOnePoint…` 실패.
- 할인·결제 금액(payment) 기준으로 계산하는 경우: 테스트 주문에는 결제가 없다(TASK가 "주문 총액" 기준을 명시).
- Kafka 등 비동기로 보내는 경우: TASK 7항이 금지한다. 테스트에서는 KafkaTemplate이 mock이라 반영되지 않는다.

## 테스트 하네스

- `@DataJpaTest`(H2) + `Wiring`(테스트 소스에 포함): 컨텍스트에 있는 빈(리포지토리·EntityManager)은 그대로 쓰고, `com.fream.back.domain`의 @Service/@Component는 실제 객체로 생성자 주입해 만든다. 나머지(Redis·WebSocket·CjTrackingPlaywright 등)는 mock이다. `ApplicationEventPublisher` 이벤트는 domain의 `@EventListener`/`@TransactionalEventListener`로 즉시 전달한다.
  - 그래서 구현이 어떤 서비스·리포지토리·스프링 이벤트를 쓰든 판정은 DB 결과(Point·Notification 행)로 한다.
- 보이는 테스트 1개: `completeOrder` → 구매자 Point 1건, 1234P.
- hidden 8개: 1%·버림·90일·AVAILABLE / BENEFIT 알림·문구·기존 알림 보존·Sale SOLD / 판매자 미적립 / 주기 갱신 경로 적립 / 여러 주문 각각 적립 / 배송출발은 미적립 / 즉시 조회 경로 1회만 / 99원 주문은 미적립이되 완료는 성공.

## 기존 테스트

- `OrderShipmentCommandService`를 직접 생성하는 기존 테스트는 없다(`OrderCommandServiceTest`는 mock으로만 쓴다). 정답은 기존 294개를 깨지 않는다. regression-exclude는 비어 있다.
- 에이전트가 다른 서비스(예: Mockito `@InjectMocks`로 만드는 서비스)에 새 의존성을 추가하면 기존 테스트에 null이 주입돼 깨질 수 있다. TASK가 "기존 테스트는 모두 통과"를 요구하므로 이 경우 에이전트가 테스트를 고쳐야 한다. hidden.sh는 사본의 테스트를 그대로 돌린다.
