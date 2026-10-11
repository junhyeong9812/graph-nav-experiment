# 08 구매자 배송 흐름: 채점 체크리스트

- 경로는 `target/fream-back` 루트 기준이다. 아래에서 `…/`는 `src/main/java/com/fream/back/`를 줄인 것이다.
- 항목마다 만점 1점, 부분 0.5점, 0점이다. 총 8점.
- 근거 `파일:줄`이 없거나 엉뚱한 파일을 가리키면 그 항목은 최대 0.5점이다. 줄 번호가 ±5줄 안이면 맞는 근거로 본다.

## D1. 운송장 등록의 경로와 의도된 순서

- **맞혀야 할 것:** `PATCH /shipments/order/{shipmentId}/status`로 들어와 `updateTrackingInfo`를 부른다. 의도된 순서는 다음과 같다.
  1. 택배사·운송장 번호를 검증한다.
  2. 배송의 택배사·운송장을 기록하고 배송 상태를 `IN_TRANSIT`으로 바꾼다. 엔티티가 전이표를 거치지 않고 직접 대입한다.
  3. 주문을 `IN_TRANSIT`으로 바꾼다.
  4. 구매자에게 "상품이 출발하였습니다" 알림을 보낸다.
  5. 판매를 SOLD로 바꾸고 판매자에게 알린다.
  6. 창고 보관 상품이면 창고 상태를 SOLD로 바꾼다.
  7. 주문 입찰·판매 입찰을 `COMPLETED`로 바꾼다.
- **근거:**
  - `…/domain/shipment/controller/command/FaqAddressCommandController.java:33-45`
  - `…/domain/shipment/service/command/FaqAddressCommandService.java:238-289`
  - `…/domain/shipment/entity/FaqAddress.java:41-45`
- **부분 점수:** 경로와 주문 `IN_TRANSIT` 변경은 맞혔지만 뒤쪽 단계(알림·SOLD·입찰 COMPLETED) 중 둘 이상이 빠지면 0.5점.

## D2. 운송장 등록의 실제 결과: 실패하고 롤백된다

- **맞혀야 할 것:**
  - 주문 상태 전이표에서 `IN_TRANSIT`으로는 `SHIPMENT_STARTED`에서만 갈 수 있다. 그런데 주문을 `SHIPMENT_STARTED`로 바꾸는 코드가 없다.
  - 실배송 일반 주문은 `PREPARING` 상태다. 그래서 `InvalidFaqStatusException`이 난다.
  - 이 예외는 `AddressException(SHIPMENT_NOT_FOUND)`로 감싸진다. 응답은 S001, HTTP 404다.
  - `@Transactional`이 롤백해서 운송장 정보도 저장되지 않고 알림도 없다.
  - 즉시 구매(`PENDING_PAYMENT`)나 창고 보관(`COMPLETED`) 주문도 같은 이유로 실패한다.
- **근거:**
  - `…/domain/order/entity/FaqStatus.java:26, 28`
  - `…/domain/order/entity/Faq.java:113-121`
  - `…/domain/shipment/service/command/FaqAddressCommandService.java:260, 295-301`
  - `…/domain/shipment/exception/AddressErrorCode.java:12`
  - `…/domain/order/service/kafka/FaqEventConsumer.java:214-218` (실배송 주문 = PREPARING)
- **부분 점수:** "주문 상태 전이 때문에 실패한다"까지만 맞고 결과(롤백·응답 코드) 중 하나가 없으면 0.5점. "성공한다"고 답하면 0점.

## D3. 배송 완료 감지: 주기와 대상

- **맞혀야 할 것:**
  - `AddressBatchScheduler`가 cron `0 0 */6 * * *`(6시간마다 정각)로 Spring Batch 잡 `updateShipmentStatusesJob`을 실행한다.
  - 리더는 상태가 `IN_TRANSIT` 또는 `OUT_FOR_DELIVERY`이고 운송장 번호가 있는 OrderShipment를 50건 단위로 읽는다.
  - (가점 없음, 감점 없음) 즉시 조회 API `POST /shipments/order/{id}/check-status`도 같은 판정을 한다.
- **근거:**
  - `…/global/config/AddressBatchScheduler.java:34-40`
  - `…/domain/shipment/config/UpdateAddressStatusesJobConfig.java:51, 55-59, 87-96`
- **부분 점수:** 배치·스케줄러는 맞혔지만 주기나 조회 조건 중 하나가 틀리면 0.5점.

## D4. 실제로 실행되는 처리 코드와 상태 매핑

- **맞혀야 할 것:**
  - 스텝의 processor는 `new AddressItemProcessor(...)`다. Playwright로 CJ 조회 페이지를 열고, 마지막 행의 5번째 칸을 읽는다.
  - 같은 설정 파일의 `@Bean shipmentItemProcessor()` 람다는 스텝에 연결되지 않았다.
  - `FaqAddressCommandService.updateShipmentStatuses()`는 호출처가 없다.
  - 매핑은 다음과 같다.
    - "배송완료" → DELIVERED
    - "배송출발" → OUT_FOR_DELIVERY
    - 그 밖 → IN_TRANSIT
    - 현재 상태와 같으면 아무것도 하지 않는다.
- **근거:**
  - `…/domain/shipment/config/UpdateAddressStatusesJobConfig.java:69-73` (실제 연결), `104-177` (미사용 빈)
  - `…/domain/shipment/config/AddressItemProcessor.java:72-105, 220-226`
  - `…/domain/shipment/service/command/FaqAddressCommandService.java:308-309` (호출처 없음)
- **부분 점수:** 실행 코드로 미사용 람다나 서비스의 `updateShipmentStatuses()`를 지목했지만 매핑은 맞으면 0.5점.

## D5. 배송 완료 판정 이후 순서와 알림

- **맞혀야 할 것:**
  1. 배송을 `DELIVERED`로 바꾼다.
  2. `completeOrder`를 부른다.
     1. 주문 ID로 OrderBid를 찾는다. 없으면 실패한다.
     2. 주문을 `COMPLETED`로 바꾼다(전이 검사).
     3. `updateSaleStatusToSold`를 부른다. StyleBid→Sale이 SOLD로 갈 수 있으면 SOLD로 바꾼다. 창고 보관이면 창고 상태도 SOLD로 바꾼다. 판매자에게 "상품이 성공적으로 판매 완료되었습니다"를 보낸다. FaqBid→Style 쪽도 같은 검사를 한다.
     4. 구매자에게 "[completeOrder] 주문이 완료 처리되었습니다"를 보낸다.
  3. processor가 `notifyShipmentCompleted`로 구매자에게 "[배송 완료] 상품이 배송 완료되었습니다"를 한 번 더 보낸다.
  - 결과적으로 구매자 알림은 2건이다. 알림은 모두 SHOPPING/BID다.
- **근거:**
  - `…/domain/shipment/config/AddressItemProcessor.java:108-117`
  - `…/domain/shipment/service/command/FaqAddressCommandService.java:111-183, 190-218`
  - `…/domain/notification/service/command/PaymentCommandService.java:219-229`
- **부분 점수:** 주문 COMPLETED와 알림은 맞혔지만 판매 SOLD 처리나 구매자 알림 2건 중 하나가 빠지면 0.5점.

## D6. 배송 완료 경로에서 입찰 상태는 그대로다

- **맞혀야 할 것:**
  - FaqBid·SaleBid를 `COMPLETED`로 바꾸는 `updateOrderAndSaleBidStatusToCompleted`는 운송장 등록 경로에서만 불린다. 그런데 그 경로는 D2에서 실패한다.
  - 배송 완료 경로(`completeOrder`)는 입찰 상태를 바꾸지 않는다. 입찰은 `MATCHED`로 남는다.
- **근거:**
  - `…/domain/shipment/service/command/FaqAddressCommandService.java:289, 412-440` (호출처는 289 하나)
  - 같은 파일 `190-229` (completeOrder에 입찰 변경 없음)
- **부분 점수:** "입찰 상태가 COMPLETED가 되지 않는다"는 맞혔지만 이유(호출 경로)가 없으면 0.5점. "배송 완료 시 입찰이 COMPLETED가 된다"고 답하면 0점.

## D7. 주문 종류별 차이

- **(a) 실배송 일반 입찰 주문:**
  - 주문이 `PREPARING`이다. `PREPARING`에서 `COMPLETED`로 가는 전이는 허용되지 않는다(허용: `IN_WAREHOUSE`, `SHIPMENT_STARTED`).
  - 그래서 `completeOrder`가 `AddressException(ORDER_NOT_FOUND_FOR_SHIPMENT)`를 던진다. 주문 완료·판매 SOLD·알림이 일어나지 않는다.
- **(b) 즉시 구매 주문:**
  - 주문이 `PENDING_PAYMENT`로 남아 있다. `PENDING_PAYMENT`에서 `COMPLETED`로 가는 전이는 허용되므로 완료된다.
  - 판매는 결제 때 이미 SOLD가 됐다. SOLD는 다른 상태로 갈 수 없으므로 판매 상태·창고 갱신과 판매자 알림은 건너뛴다. 구매자 알림 2건만 간다.
- **근거:**
  - `…/domain/order/entity/FaqStatus.java:24, 26`
  - `…/domain/shipment/service/command/FaqAddressCommandService.java:204, 221-227, 119, 156`
  - `…/domain/sale/entity/StyleStatus.java:23`
  - `…/domain/payment/service/command/NotificationCommandService.java:404`
  - `…/domain/order/service/kafka/FaqEventConsumer.java:214-218`
- **부분 점수:** (a)와 (b) 중 하나만 맞으면 0.5점.

## D8. 한 건 실패 시 처리

- **맞혀야 할 것:**
  - 스텝은 `faultTolerant().skip(Exception.class).skipLimit(50)`이다. 실패한 건은 skip되어 SkipListener가 로그만 남기고, 나머지 건은 계속 처리된다.
  - processor 안의 예외는 `AddressException`으로 감싸진다.
  - 스케줄러는 잡 실행 예외를 `printStackTrace`로 삼킨다.
  - 배송 완료 알림(`notifyShipmentCompleted`) 실패는 로그만 남긴다.
- **근거:**
  - `…/domain/shipment/config/UpdateAddressStatusesJobConfig.java:75-78, 251-287`
  - `…/domain/shipment/config/AddressItemProcessor.java:133-145`
  - `…/global/config/AddressBatchScheduler.java:41-43`
  - `…/domain/notification/service/command/PaymentCommandService.java:230-233`
- **부분 점수:** skip으로 나머지가 계속된다는 것만 맞히고 근거 설정(skip/skipLimit)이 없으면 0.5점.

## 채점 메모

- skip된 건의 `DELIVERED` 변경이 DB에 남는지는 Spring Batch의 청크 롤백·재처리 동작에 달려 있어 채점하지 않는다. 어느 쪽으로 답해도 감점하지 않는다.
- 즉시 조회 API(`updateAndCheckShipmentStatus`)를 추가로 설명한 답은 감점하지 않는다. 이 경로는 `completeOrder`만 부르고 `notifyShipmentCompleted`는 부르지 않는다(`FaqAddressCommandService.java:483-489`).
