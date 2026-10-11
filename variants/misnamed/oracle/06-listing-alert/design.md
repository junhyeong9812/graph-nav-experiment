# 06 listing-alert — 설계 메모 (change)

## 의도

"판매 입찰이 새로 등록되면 그 상품 색상을 관심 등록한 회원에게 알림". 판매(sale) 등록 흐름 위에 상품 관심(product/Interest)과 알림(notification)을 얹고, 회원(user) 활성 여부를 거른다. 도메인 4개를 거친다. 05(shipment·user/point·notification)와 조합이 다르다.

## 정답 (fix.patch)

- `InterestRepository.findAllByProductColor(UserColor)` 추가. 색상별 관심자를 찾는 쿼리가 원래는 없다.
- `StyleBidCommandService`에 `InterestRepository`·`PaymentCommandService` 주입. `createSaleBid`가 저장한 뒤 `notifyInterestedUsers`를 부른다.
  - `productSize.getProductColor()`가 null이면 건너뛴다(기존 Mockito 테스트 보호).
  - 판매자 본인·`!user.isActive()`는 제외한다.
  - `PaymentType.FAVORITE`, 문구는 "관심 상품 {상품명} ({색상}) 사이즈 {size}에 새 판매 입찰이 등록되었습니다. 판매 희망가: {price}원".
- `createInstantSaleBid`(즉시 판매)에는 넣지 않는다.

## 왜 탐색이 필요한가

- 판매 입찰 등록 진입점(`StyleBidCommandService.createSaleBid`)과, 그것이 부르는 `StyleCommandService.createSale`(판매자 알림)·`createInstantSale`(즉시 판매)의 구분을 찾아야 한다.
- 관심 상품이 **색상 단위**(`Interest.productColor`)라는 점을 알아야 한다. 판매 입찰은 `UserSize`를 들고 있으므로 `UserSize → UserColor → User` 연관을 따라가야 한다.
- `InterestRepository`에 색상 기준 조회가 없다. 새 파생 쿼리를 쓰거나 `UserColor.interests` 컬렉션을 써야 한다.
- "관심 상품 관련 알림"은 `PaymentType.FAVORITE`이고, 현재 쓰는 곳이 없다.
- "계정 비활성화"는 `Product.isActive` 필드다.

## 순진한 오답

- `StyleCommandService.createSale`에 넣고 가격을 빼는 경우: 문구에 판매 희망가가 없다 → 실패(createSale에는 가격 인자가 없다).
- 상품(User) 단위로 관심자를 찾는 경우: 다른 색상 관심자까지 알림을 받는다 → `fansOfOtherColorsOrOtherProductsAreNotAlerted` 실패.
- 판매자·비활성 회원을 거르지 않는 경우 → `sellerAndInactiveMembersAreNotAlerted` 실패.
- 즉시 판매 경로(createInstantSale / Style 생성 공통 지점)에도 거는 경우 → `instantSaleToABuyBidDoesNotAlertFans` 실패.
- 기존 알림 유형(BID 등)을 쓰는 경우 → FAVORITE 필터에 걸리지 않아 실패.

## 테스트 하네스

05와 같다(`Wiring`: DB 실객체 + domain 서비스 실객체 + 외부 의존 mock + 스프링 이벤트 즉시 전달). 판정은 Payment 행이다.

- 보이는 테스트 1개: White 색상 관심자는 FAVORITE 1건(상품명 포함), Black 색상 관심자는 0건.
- hidden 7개: 같은 색상 활성 관심자 전원 1건·카테고리 SHOPPING·문구(상품명·사이즈·가격) / 다른 색상·다른 상품 관심자 알림 0건 / 판매자·비활성 제외 / 같은 색상 다른 사이즈도 알림 / 등록마다 1건 / 기존 판매 등록 동작(StyleBid·Style·정산계좌·판매자 BID 알림) 보존 / 즉시 판매는 알림 없음.

## 기존 테스트

- `SaleBidCommandServiceTest`는 `@InjectMocks`로 `StyleBidCommandService`를 만든다. 새 생성자 의존은 null로 들어가지만, 그 테스트의 `UserSize`에는 색상이 없다. 그래서 정답은 null 가드로 깨지지 않는다(정답 적용 후 hidden.sh의 domain 회귀 통과로 확인).
- 에이전트 구현이 색상 null을 가드하지 않으면 이 기존 테스트가 NPE로 깨진다. TASK가 기존 테스트 통과를 요구하므로 에이전트가 처리할 몫이다. regression-exclude는 비어 있다.
