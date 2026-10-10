# 대상 코드베이스 의심 지점 목록

- 대상: `target/fream-back`(fream-back `4fd438f` + 빌드 수선)
- 출처: 2026-10-10 기존 동작 고정 테스트 작성 워커 4명(w1~w4)의 보고
- 테스트는 아래 동작을 **현재 그대로** 고정했고, 고치지 않았다.
- 메인이 코드로 직접 확인한 항목은 ✔로 표시했다.
- 용도: 다음 작업에서 실험 과제(버그 수정·변경 요청)의 재료로 쓴다.
- **에이전트에게 주는 사본(`target/`)에는 넣지 않는다.** 이 파일은 `docs/`에만 둔다.

경로는 `src/main/java/com/fream/back/` 기준이다.

## 기동

| # | 위치 | 내용 |
|---|---|---|
| S1 ✔ | `global/config/RedisConfig.java:157` | `stringRedisTemplate` 빈 이름이 Boot 자동 설정과 겹쳐 앱이 기동되지 않는다(`BeanDefinitionOverrideException`). 2025-08 `c91c9e9`부터 있었다. |

## user · address (w1)

| # | 위치 | 내용 |
|---|---|---|
| U1 | `global/utils/PersonalDataEncryptionUtil.java:154` | 결정적 암호화는 SHA-256(secretKey+입력)으로 만든 키를 쓰고 복호화는 secretKey를 쓴다. 실제 데이터로 주소를 조회하면 항상 `ADDRESS_QUERY_ERROR`가 난다. |
| U2 | `domain/address/service/command/AddressCommandService.java:32,81,137` vs `AddressQueryService` | 명령은 평문 이메일로, 조회는 암호화한 이메일로 사용자를 찾는다. |
| U3 | `domain/user/entity/User.java:256`, `UserCommandService.java:133` | 추천인을 연결하면 내 추천 코드가 추천인 코드로 덮어써진다. unique 충돌 가능. |
| U4 | `domain/user/entity/User.java:350` | `calculateTotalPoints`가 남은 금액이 아니라 amount를 더한다. |
| U5 | `domain/user/service/point/PointCommandService.java:109` | 포인트를 쓰면 적립 기록의 사유가 사용 사유로 바뀌고, 사용 내역은 따로 남지 않는다. |
| U6 | `domain/user/repository/PointRepository.java:25` | 가용 포인트 합계에 만료일이 지난 AVAILABLE 포인트도 들어간다. |
| U7 | `domain/address/service/command/AddressCommandService.java:67` | 새 주소가 `user.getAddresses()`에 추가되지 않는다. |
| U8 | `domain/user/service/query/UserQueryService.java:62` | 전화번호로 이메일을 찾을 때 예외 메시지가 깨진다(email용 생성자에 문장을 넘김). |
| U9 | `FollowCommandService.java:81`, `UserCommandService` | 예외 종류가 원인과 맞지 않는다. 예상 못 한 오류를 AlreadyFollowing으로, 시스템 오류를 USER_NOT_FOUND로, 동의 누락을 INVALID_VERIFICATION_CODE로 감싼다. |
| U10 | `domain/user/repository/BlockedProfileRepository.java:21` | `List<Long>`을 반환하는 파생 쿼리라 실행하면 타입 변환에 실패할 가능성이 있다. |
| U11 | `UserSanctionSpecifications.java:24,30` | userId와 email 조건을 함께 주면 `root.join("user")`가 두 번 들어간다. |

## product · style (w2)

| # | 위치 | 내용 |
|---|---|---|
| P1 | `ProductQueryDslRepository.java:197` | 검색 total을 세는 단위(상품)와 내용의 단위(상품×색상 행)가 다르다. 그래서 total이 페이지 위치에 따라 달라진다(0페이지 3, 마지막 4). |
| P2 | `ProductQueryDslRepository.java:98,359,470` | 사이즈와 관심을 함께 조인해, 관심 수가 사이즈 수만큼 곱해진다. |
| P3 | `ProductQueryDslRepository.java:229~` | `countProductsByFilter`가 컬렉션 필터를 무시한다. |
| P4 | `ProductQueryDslRepository.java:388` | 상세 조회의 사이즈 목록을 색상명으로만 걸러, 다른 상품의 같은 색상 사이즈가 섞인다. |
| P5 | `ProductQueryDslRepository.java:462` | `searchProductsByColorIds`가 썸네일 없는 색상을 빠뜨린다(암묵 inner join). |
| P6 ✔ | `domain/product/service/interest/InterestCommandService.java:98-102` | 색상별 관심 일괄 삭제가 1건 이상이면 `ConcurrentModificationException`으로 항상 실패한다. 실패 직전에 첫 건은 이미 지워져 있다. |
| P7 | `ProductCommandService.java:161` | 수정 응답의 gender가 엔티티 값이 아니라 요청 값이다. 요청에 gender가 없으면 null이 된다. |
| P8 | `ProductSizeCommandService.java:86,96,240` | 유효한 사이즈를 저장한 뒤 무효 사이즈에서 예외를 던져 롤백에 기댄다. 에러 코드를 다시 포장한다. |
| P9 | `domain/style/entity/Style.java:39,60` | viewCount 기본값이 없어 빌더로 만들면 `incrementViewCount`에서 NPE가 난다. |
| P10 | `HashtagCommandService.java:38,41` | 빈 이름에 `HASHTAG_ALREADY_EXISTS`(409)를 쓴다. 주석은 "소문자 변환"이라고 하지만 실제로는 trim만 한다. |
| P11 | `StyleRepositoryCustomImpl.java:316~` | `getStylesByProfile`에 정렬이 없어 순서가 DB에 의존한다. |

## order · payment (w3)

| # | 위치 | 내용 |
|---|---|---|
| O1 ✔ | `domain/payment/service/command/PaymentCommandService.java:260` | 새 결제가 이미 PENDING인데 다시 PENDING으로 바꿔 예외가 난다. 그래서 계좌이체 결제는 항상 실패한다. |
| O2 | `OrderBidCommandService.java:100,~252` | 일반 주문 입찰이 생성 즉시 MATCHED가 되어 삭제할 수 없고, pending 카운트에도 잡히지 않는다. |
| O3 | `OrderCommandService.java:188` | 주문 ID를 OrderBid ID 조회(`findById`)에 넘긴다. |
| O4 | `OrderBidRepositoryImpl.java:90` | 내용 쿼리는 썸네일을 inner join하고 count 쿼리는 하지 않아 total이 맞지 않는다. |
| O5 | `PaymentInfoCommandService.java:78`, `PaymentInfoQueryService.java:151,188` | 카드 정보를 평문으로 저장하고 조회 때는 복호화해서, 조회가 실패한다. |
| O6 | `PaymentInfoCommandService.java:51` | 카드 등록 때 만료를 검사하지 않는다. |
| O7 ✔ | `domain/payment/entity/Payment.java:62` | 금액만 양수면 `isSuccess=true`가 된다. PortOne 응답이 실패여도 성공 조회에 잡힌다. |
| O8 | `PaymentCommandService.java:188-189` | 카드 전체 번호와 비밀번호 앞 2자리를 그대로 저장한다. |
| O9 ✔ | `domain/payment/repository/PaymentRepository.java:65` | enum에 없는 리터럴 `'PROCESSING'`을 쓴다(H2에서 실행 오류). |
| O10 | `domain/order/entity/OrderStatus.java:24` | PENDING_PAYMENT에서 COMPLETED로 바로 가는 전이를 허용한다. |

## sale · notification (w4)

| # | 위치 | 내용 |
|---|---|---|
| N1 | `SaleBidCommandService.java:53`, `SaleBid.java:55` | 판매 입찰이 생성 즉시 MATCHED가 된다(O2와 같은 패턴). |
| N2 | `SaleBidRepositoryImpl.java:84-88 vs 101-109` | 본문은 썸네일을 inner join하고 count는 하지 않는다(O4와 같은 패턴). |
| N3 | `SaleBid.markAsInstantSale()` | 호출하는 곳이 없어 즉시 판매도 `isInstantSale=false`다. |
| N4 ✔ | `NotificationQueryService.java:160`, `NotificationRepositoryCustomImpl.java:66` | `Pageable.unpaged()`에서 `getOffset()`을 불러 예외가 난다. 그래서 안 읽은 알림 수 조회가 항상 실패한다. 컨트롤러 `NotificationQueryController.java:165`가 이 조회를 쓴다. |
| N5 | `NotificationCommandService.java:364`, `NotificationQueryService.java:557` | createdDate가 null이면 NPE가 난다(정상 흐름에서는 @PrePersist가 채운다). |
| N6 | `NotificationCommandService.java:227` | 배송 완료 알림 유형이 TRADE가 아니라 BID다. |
| N7 | `SaleCommandService.java:153-155`, `Sale.updateStatus` | 엔티티는 상태 전이를 검사하지 않고, `System.out.println`이 남아 있다. |
| N8 | `SaleCommandService.createSellerShipment` | 배송을 먼저 만들고 나서 상태 전환을 검사한다. |

## 반복되는 패턴

- **페이지 total 불일치:** 내용 쿼리와 count 쿼리의 조인이 다르다(P1, O4, N2).
- **생성 즉시 MATCHED:** 입찰이 만들어지자마자 매칭 상태가 된다(O2, N1).
- **암호화 왕복 불일치:** 저장할 때와 읽을 때의 처리가 다르다(U1, U2, O5).
