# 08 구매자 배송 흐름: 과제 설계 메모

- 유형: question
- 기준: `target/fream-back`(82ec907 시점)

## 의도

07(즉시 구매)과 겹치지 않는 흐름을 고른다. 결제 이후의 배송을 다룬다.

- 운송장 등록(REST)
- 배송 완료 감지(스케줄러 → Spring Batch → Playwright 조회)
- 완료 후 연쇄 변경

연쇄 변경은 4개 도메인에 걸친다.

| 도메인 | 바뀌는 것 |
|---|---|
| order | 주문 상태 |
| sale | 판매 SOLD |
| warehouseStorage | 창고 상태 |
| notification | 알림 |

이 과제가 재는 것은 다음 세 가지다.

1. 배치 설정 파일 안에 미사용 processor 빈이 있고, 서비스에도 호출처 없는 `updateShipmentStatuses()`가 있다. 이 둘을 피해 **실제로 실행되는 코드**를 찾아내는가.
2. 주문 상태 전이표(order 도메인)와 결제 단계에서 남겨 둔 주문 상태(Kafka 컨슈머·즉시 구매 서비스)를 맞춰 보고, **흐름이 실제로는 끝까지 가지 않는다**는 점을 밝혀내는가.
3. 알림이 몇 건, 누구에게 가는지 세 메서드에 흩어진 호출을 모아 세는가.

## 정답 요약 (체크리스트 D1~D8)

1. **운송장 등록:** `PATCH /shipments/order/{shipmentId}/status`로 들어와 `OrderShipmentCommandService.updateTrackingInfo`를 부른다. 코드상 의도는 다음 순서다.
   1. 배송 `IN_TRANSIT`(엔티티가 직접 대입)
   2. 주문 `IN_TRANSIT`
   3. 구매자에게 "출발" 알림
   4. 판매 SOLD와 판매자 알림
   5. 창고 SOLD
   6. 입찰 `COMPLETED`
2. **실제 결과:** 주문 `IN_TRANSIT`으로는 `SHIPMENT_STARTED`에서만 갈 수 있다. 그런데 `SHIPMENT_STARTED`를 세팅하는 코드가 없다. 그래서 어떤 주문이든 `InvalidOrderStatusException`이 나고, `ShipmentException(SHIPMENT_NOT_FOUND, 404)`로 감싸져 롤백된다.
3. **감지:** `ShipmentBatchScheduler`(cron `0 0 */6 * * *`)가 `updateShipmentStatusesJob`을 실행한다. 리더 조건은 `IN_TRANSIT`/`OUT_FOR_DELIVERY` 상태이면서 운송장 번호가 있는 것이고, 청크는 50이다.
4. **실행 코드:** `new ShipmentItemProcessor(...)`다. 빈 람다와 서비스 메서드는 쓰이지 않는다. 매핑은 "배송완료"→DELIVERED, "배송출발"→OUT_FOR_DELIVERY, 그 밖→IN_TRANSIT이다.
5. **DELIVERED 이후:** 다음 순서로 진행된다. 구매자 알림은 2건이다.
   1. 배송 DELIVERED
   2. `completeOrder`
      1. OrderBid 필수
      2. 주문 COMPLETED
      3. `updateSaleStatusToSold`(전이 가능 시 판매 SOLD, 창고 SOLD, 판매자 알림)
      4. 구매자 "[completeOrder]" 알림
   3. `notifyShipmentCompleted`로 구매자 "[배송 완료]" 알림
6. **입찰 상태:** 배송 완료 경로는 OrderBid·SaleBid를 `COMPLETED`로 바꾸지 않는다. `COMPLETED`로 바꾸는 코드는 실패하는 운송장 등록 경로에만 있다.
7. **주문 종류별 차이:**
   - (a) 실배송 일반 입찰 주문은 `PREPARING`이고, `PREPARING`에서 `COMPLETED`로 가는 전이는 불가라서 실패한다.
   - (b) 즉시 구매 주문은 `PENDING_PAYMENT`이고, `PENDING_PAYMENT`에서 `COMPLETED`로 가는 전이는 허용된다. 다만 판매는 이미 SOLD라서 판매자 알림 없이 구매자 알림 2건만 간다.
8. **실패 처리:** `faultTolerant().skip(Exception).skipLimit(50)`이다. 실패한 건은 SkipListener가 로그를 남기고 skip되며, 나머지 건은 계속 처리된다.

## 왜 탐색이 필요한가

- TASK.md에는 클래스·파일 이름이 없다. "운송장"·"배송 완료"로 찾으면 후보가 여러 곳으로 갈린다.
  - shipment 도메인 서비스
  - `shipment/config`의 배치 설정
  - `global/config`의 스케줄러
- 스케줄러는 shipment 패키지가 아니라 `global/config`에 있다.
- 배치 설정 파일 하나에 처리 로직이 두 벌 있다(빈 람다와 별도 클래스). 스텝 빌더의 `.processor(...)` 인자를 봐야 실제로 쓰는 쪽을 가릴 수 있다.
- "끝까지 가지 않는다"는 결론은 shipment 도메인만 봐서는 나오지 않는다. 다음 세 곳을 함께 봐야 한다.
  - order 도메인의 `OrderStatus` 전이표
  - 결제 이후 주문 상태를 정하는 order Kafka 컨슈머
  - 즉시 구매 서비스(07과 공유하는 사실)
- 알림 건수는 세 곳에 흩어진 호출을 모아야 한다.
  - `completeOrder`
  - `updateSaleStatusToSold`
  - `NotificationCommandService.notifyShipmentCompleted`

## 순진한 오답 (예상)

- "운송장 등록 시 주문이 `IN_TRANSIT`이 되고 판매가 SOLD가 된다." 의도만 읽고 전이표를 확인하지 않은 답이다.
- "배송 완료는 서비스의 `updateShipmentStatuses()`가 주기적으로 처리한다." 이름만 보고 호출처를 확인하지 않은 답이다.
- "배송 완료 시 입찰이 COMPLETED가 된다." `updateOrderAndSaleBidStatusToCompleted`를 배송 완료 경로로 오인한 답이다.
- "배송 완료 시 주문은 항상 COMPLETED가 된다." 주문 종류별 직전 상태를 확인하지 않은 답이다.
- "구매자는 배송 완료 알림 1건을 받는다." `completeOrder` 안의 알림을 놓친 답이다.

## 범위 밖·채점 유의

- skip된 건의 배송 `DELIVERED` 변경이 남는지는 Spring Batch 청크 트랜잭션 동작에 달려 있다. 그래서 채점하지 않는다.
- 즉시 조회 API(`check-status`)의 세부(같은 상태로 다시 `IN_TRANSIT`을 넣으면 예외가 나는 점 등)는 묻지 않는다. 언급해도 감점하지 않는다.
- 판매자 발송(SellerShipment) 흐름은 범위 밖이다.
