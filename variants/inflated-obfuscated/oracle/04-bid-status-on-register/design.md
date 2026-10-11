# 04 bid-status-on-register — 설계 메모 (pattern: 생성 즉시 MATCHED)

## 의도

- 보고된 증상은 일반 구매 입찰(주문 도메인) 등록 직후 "매칭 완료"가 되어 취소할 수 없는 것이다.
- 서비스 코드(`Xtw92CommandService.createAndSaveOrderBid`)는 `status(PENDING)`으로 만들기 때문에 서비스만 읽으면 원인이 안 보인다. 원인은 엔티티의 연관관계 편의 메서드가 "자기 쪽 연결"(구매 입찰 ↔ 자기 주문)을 "매칭"으로 취급해 상태를 바꾸는 데 있다.
- 같은 원인이 판매 도메인의 거울 구조(판매 입찰 ↔ 자기 판매 `Xpj42`)에도 있다. 여기서는 서비스가 `saleBidRepository.save` 뒤에 `sale.assignSaleBid(saleBid)`(Xpj42 엔티티)를 부르고, 그것이 다시 `Xzt63.assignSale`을 불러 MATCHED가 된다. 두 단계 간접 호출이라 찾으려면 엔티티 사이를 따라가야 한다.

## 지점 목록 (hidden이 모두 검사)

| # | 증상 경로 | 원인 | hidden 클래스 |
|---|---|---|---|
| 1 (보고) | `Xtw92CommandService.createOrderBid`(47) → `createAndSaveOrderBid`(90-103)가 `orderBid.assignOrder(order)`(100) 호출 | `order/entity/Xtw92.java:60-82` `assignOrder`가 order가 null이 아니면 `status = MATCHED`(75). `Xhq12.assignOrderBid`(Xhq12.java:128-133)도 이 메서드를 부른다 | `PurchaseBidRegistrationHiddenTest` |
| 2 | `Xzt63CommandService.createSaleBid`(27-56)가 save 뒤 `sale.assignSaleBid(saleBid)`(53) | `sale/entity/Xpj42.java:59-62` `assignSaleBid` → `sale/entity/Xzt63.java:53-56` `assignSale`이 `status = MATCHED`(55). 저장 시점에는 PENDING이지만 같은 트랜잭션의 dirty checking으로 MATCHED가 커밋된다 | `SaleBidRegistrationHiddenTest` |

- 둘 다 의심 지점 O2·N1에 해당한다.
- 증상 연쇄(확인용): `Xtw92CommandService.deleteOrderBid`(256-287)가 MATCHED면 거절, `matchOrderBid`(228-246)가 이미 MATCHED라며 거절, `Xtw92RepositoryImpl.countOrderBidsByStatus`·`Xzt63RepositoryImpl.countSaleBidsByStatus`의 대기 중 개수 0.

## 전수 grep 기록 (누락 방지)

- 명령: `grep -rn "assignSale(\|assignOrder(\|Xkh73Status.MATCHED\|assignOrderBid(\|assignSaleBid(" src/main`, `grep -rn "Xtw92.builder\|Xzt63.builder" src/main`, 그리고 `public void assign*`에서 status를 바꾸는 엔티티 전체(`grep -A8 "public void assign"` → `status =`).
- 입찰 생성 경로는 4개뿐이다: 일반 구매(`Xtw92CommandService:91`), 즉시 구매(`:192`, 원래 MATCHED), 일반 판매(`Xzt63CommandService:41`), 즉시 판매(`:72`, 원래 MATCHED). 즉시 거래 두 곳은 빌더에서 MATCHED를 직접 넣으므로 정상이다.
- 상대 쪽 연결(`Xtw92.assignSale`, `Xzt63.assignOrder`)은 실제 매칭이므로 MATCHED가 맞다. 이를 부르는 `Xpj42CommandService.createInstantSale`(79-80)·`Xhq12CommandService.createInstantOrder`(279-280)는 어차피 `updateStatus(MATCHED)`를 따로 부른다.
- 검토 후 제외:
  - `Xms75.assignOrder/assignSale`: 보관 상태(ASSOCIATED_WITH_ORDER/IN_STORAGE)를 바꾸지만 입찰 매칭과 무관하고, 생성 직후 그 상태가 의도다.
  - `Xrg51`·`Xfr60`의 상태 변경: 송장 등록 시 IN_TRANSIT, 별개 의미.
  - `Xhq12CommandService.processPaymentAndShipment`(188-189)의 `orderBid.updateStatus(MATCHED)`: 주문 ID로 OrderBid를 찾는 별개 버그(O3) 위에 있어 이 과제 범위 밖.
  - `global/config/Xdq35`: 개발용 시드 데이터.

## 정답 (`fix.patch`)

- `Xtw92.assignOrder`에서 `status = MATCHED`를 지운다(양방향 연결만 한다).
- `Xzt63.assignSale`에서 `status = MATCHED`를 지운다.
- 다른 정답도 인정된다(hidden은 서비스 결과만 본다): 예) 서비스에서 연결 뒤 상태를 PENDING으로 되돌리기, 연결 순서를 바꾸고 상태를 다시 넣기. 단 즉시 구매·즉시 판매는 MATCHED로 남아야 한다.

## 왜 탐색이 필요한가

- 증상 위치(구매 입찰 등록 서비스)에는 PENDING이라고 쓰여 있다. 엔티티 `Xtw92`와 `Xhq12.assignOrderBid`의 상호 호출을 따라가야 원인이 보인다.
- 두 번째 지점은 판매 도메인이고, 서비스 → `Xpj42` 엔티티 → `Xzt63` 엔티티로 두 번 건너야 보인다. 보고 증상에는 판매 쪽 언급이 없다.

## 순진한 오답

- 보고 지점만 고친다(부분 정답, 아래 diff): test.sh 통과, hidden에서 판매 입찰 지점 실패.
- 서비스의 `deleteOrderBid`에서 MATCHED 검사를 지운다: 증상 하나(취소)는 사라지지만 상태는 여전히 MATCHED라 test.sh(상태 PENDING)와 hidden 실패.
- `Xtw92.assignSale`·`Xzt63.assignOrder`(상대 쪽 매칭)까지 상태 변경을 지운다: 현재 호출처가 모두 `updateStatus(MATCHED)`를 따로 불러 판정은 통과하지만, 엔티티 의미가 약해진다.
- 즉시 구매 빌더의 MATCHED까지 PENDING으로 바꾼다: hidden의 즉시 거래 보존 검사 실패.

## regression-exclude

- `OrderBidCommandServiceTest`(`createOrderBidLinksOrderAndIsSavedAsMatched`), `OrderEntityStateTest`(`orderBidAssignOrderLinksBothSidesAndMarksMatched`), `SaleEntityTest`(`saleBidAssignSaleSetsMatched`, `saleAssignSaleBidLinksBothSidesAndMatchesBid`), `SaleBidCommandServiceTest`(`createSaleBidCreatesSaleAndBidAndEndsMatched`): 생성 즉시 MATCHED인 현재 동작을 고정한다.

## 부분 정답 diff (보고 지점만)

```diff
diff --git a/src/main/java/com/fream/back/domain/m99/entity/Xtw92.java b/src/main/java/com/fream/back/domain/m99/entity/Xtw92.java
index 4f719f5..d5b21da 100644
--- a/src/main/java/com/fream/back/domain/m99/entity/Xtw92.java
+++ b/src/main/java/com/fream/back/domain/m99/entity/Xtw92.java
@@ -67,13 +67,10 @@ public class Xtw92 extends Xzh80Entity {
             }
         }
 
-        // 새 관계 설정
+        // 새 관계 설정 (자기 주문 연결일 뿐 매칭이 아니므로 상태는 바꾸지 않는다)
         this.order = order;
 
-        // 매칭 상태로 변경
         if (order != null) {
-            this.status = Xkh73Status.MATCHED;
-
             // Xhq12 쪽에도 관계 설정
             if (order.getOrderBid() != this) {
                 order.assignOrderBid(this);
```
