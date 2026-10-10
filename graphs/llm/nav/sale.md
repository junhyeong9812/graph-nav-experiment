# sale 도메인 네비게이션 맵

> 기준 경로: `src/main/java/com/fream/back/domain/sale` (24 files). 코드에서 확인된 사실만 기재. 교차 흐름은 `nav/flow-trade.md`.

## 1. 개요
- 판매 측 거래 도메인: **판매 입찰(SaleBid)**, **판매(Sale)**, **정산 계좌 스냅샷(SaleBankAccount)**.
- 판매 입찰 생성 시 Sale(PENDING_SHIPMENT) + SaleBankAccount(user 도메인 등록 계좌 복사)가 함께 생성.
- 즉시 판매(`POST /sale-bids/instant`)는 기존 OrderBid에 Sale을 연결.
- 판매자 발송 등록은 `POST /sales/{saleId}/shipment` → shipment 도메인 `SellerShipmentCommandService`.

## 2. 패키지/파일 구조
```
src/main/java/com/fream/back/domain/sale/
├── controller/command/ SaleBidCommandController.java (/sale-bids), SaleCommandController.java (/sales)
├── controller/query/   SaleBidQueryController.java (/sale-bids)
├── dto/ InstantSaleRequestDto, SaleBidRequestDto, SaleBidResponseDto, SaleBidStatusCountDto, ShipmentRequestDto
├── entity/ Sale, SaleBid, SaleBankAccount, SaleStatus, BidStatus
├── exception/ SaleErrorCode, SaleException
├── repository/ SaleRepository, SaleBidRepository(+Custom, Impl=QueryDSL), SaleBankAccountRepository
└── service/
    ├── command/ SaleBidCommandService, SaleCommandService, SaleBankAccountCommandService
    └── query/   SaleBidQueryService
```

## 3. 엔티티
| 엔티티 | 주요 필드 | 관계 |
|---|---|---|
| `Sale` | returnAddress, postalCode, receiverPhone, isWarehouseStorage, status(SaleStatus) | seller(ManyToOne User), productSize, payment(1:1 mappedBy, `Payment`), saleBankAccount(1:1 mappedBy), saleBid(1:1 mappedBy), sellerShipment(1:1 mappedBy, `SellerShipment`) — cascade ALL |
| `SaleBid` | bidPrice, status(sale.entity.BidStatus, 기본 PENDING), isInstantSale | seller, productSize, sale(1:1 FK sale_id), order(1:1 FK order_id, `order.entity.Order`) |
| `SaleBankAccount` | bankName, accountNumber, accountHolder | sale(1:1 FK sale_id, not null) |

**SaleStatus** (`entity/SaleStatus.java` `canTransitionTo`):
| from | 허용 to |
|---|---|
| PENDING_SHIPMENT | IN_TRANSIT |
| IN_TRANSIT | IN_INSPECTION |
| IN_INSPECTION | FAILED_INSPECTION, IN_STORAGE |
| IN_STORAGE | ON_AUCTION, AUCTION_EXPIRED, SELLING |
| ON_AUCTION | SOLD, AUCTION_EXPIRED |
| SELLING | SOLD, AUCTION_EXPIRED, ON_AUCTION |
| SOLD / AUCTION_EXPIRED / FAILED_INSPECTION | (없음) |

**BidStatus** (`sale/entity/BidStatus.java`): PENDING, MATCHED, CANCELLED, COMPLETED — order 도메인과 **중복 정의된 별도 enum**.

상태 변경 메서드:
- `Sale#updateStatus(SaleStatus)` (Sale.java:73): **검증 없이 대입**. 검증은 `SaleCommandService#updateSaleStatus`에서만 수행.
- `Sale#assignSaleBid(saleBid)` (:59) → `saleBid.assignSale(this)` → SaleBid status=**MATCHED**.
- `SaleBid#assignSale` (:53), `#assignOrder` (:58): 둘 다 status=MATCHED. `#updateStatus`: 검증 없음. `markAsInstantSale()`(호출처 없음).
- `Sale#assignSaleBankAccount`, `#assignPayment`(Payment.assignSale 호출), `#assignSellerShipment`.

## 4. API 엔드포인트
모두 인증 필요(SecurityConfig `anyRequest().authenticated()`). 컨트롤러에 `@Valid` 없음 → DTO의 Bean Validation 어노테이션은 적용되지 않음.

| Method | Path | Controller#method | Service | 비고 |
|---|---|---|---|---|
| POST | /sale-bids | SaleBidCommandController#createSaleBid | SaleBidCommandService#createSaleBid | body `SaleBidRequestDto`(productSizeId, bidPrice, returnAddress, postalCode, receiverPhone, warehouseStorage). 응답 SaleBid id (raw Long) |
| DELETE | /sale-bids/{saleBidId} | SaleBidCommandController#deleteSaleBid | SaleBidCommandService#deleteSaleBid | 소유자 검증 없음 |
| POST | /sale-bids/instant | SaleBidCommandController#createInstantSale | SaleBidCommandService#createInstantSaleBid | body `InstantSaleRequestDto`(orderBidId, returnAddress, postalCode, receiverPhone). 응답 Sale id |
| GET | /sale-bids?saleBidStatus&saleStatus&page | SaleBidQueryController#getSaleBids | SaleBidQueryService#getSaleBids | |
| GET | /sale-bids/count | SaleBidQueryController#getSaleBidStatusCounts | SaleBidQueryService#getSaleBidStatusCounts | |
| GET | /sale-bids/{saleBidId} | SaleBidQueryController#getSaleBidDetail | SaleBidQueryService#getSaleBidDetail | 없으면 IllegalArgumentException |
| POST | /sales/{saleId}/shipment | SaleCommandController#createSellerShipment | SaleCommandService#createSellerShipment | body `ShipmentRequestDto`(courier, trackingNumber). 소유자 검증 없음 |

## 5. 서비스 계층
- **SaleBidCommandService**
  - `createSaleBid(...)` (:27): UserQueryService → ProductSizeQueryService → `SaleCommandService#createSale` → SaleBid(PENDING) 저장 → `sale.assignSaleBid(saleBid)` (→ MATCHED로 변경됨).
  - `createInstantSaleBid(orderBidId, ...)` (:59): `SaleCommandService#createInstantSale` → `OrderBidQueryService#findById` → SaleBid(MATCHED, bidPrice=orderBid.bidPrice, order=orderBid.order) 저장. 반환은 Sale id.
  - `deleteSaleBid` (:94): order 연결 시 IllegalStateException, sale 있으면 `SaleCommandService#deleteSale`.
- **SaleCommandService**
  - `createInstantSale(...)` (:38): Sale(PENDING_SHIPMENT; isWarehouseStorage 미설정=false) 저장, `BankAccountQueryService#getBankAccount` → `SaleBankAccountCommandService#createSaleBankAccount`, `orderBid.assignSale(sale)` + MATCHED, 구매자에게 `NotificationCommandService#createNotification(SHOPPING, BID)`.
  - `createSale(...)` (:95): 계좌 조회 → Sale(PENDING_SHIPMENT, isWarehouseStorage) 저장 → SaleBankAccount → 판매자 알림 "판매 입찰이 등록되었습니다.".
  - `createSellerShipment(saleId, courier, trackingNumber)` (:137): `SellerShipmentCommandService#createSellerShipment` → `updateSaleStatus(saleId, IN_TRANSIT)`.
  - `updateSaleStatus` (:148): `canTransitionTo` 실패 시 IllegalStateException. `System.out.println` 디버그 출력 있음.
  - `deleteSale` (:163).
- **SaleBankAccountCommandService#createSaleBankAccount**.
- **SaleBidQueryService**: `findById`(없으면 IllegalArgumentException), `findByOrderId`(없으면 **IllegalArgumentException**, null 반환 안 함), `findBySaleId`(null 반환), `getSaleBids`, `getSaleBidStatusCounts`, `getSaleBidDetail`.

## 6. Repositories
- `SaleRepository#findBySeller_EmailAndStatus`.
- `SaleBidRepository`: `findById`, `findByOrder_Id`, `findBySale_Id`. Custom(`SaleBidRepositoryImpl`, QueryDSL):
  - `findSaleBidsByFilters(email, saleBidStatus, saleStatus, pageable)`: join seller(on email) / productSize / productColor / product / thumbnailImage, leftJoin sale / sellerShipment.
  - `countSaleBidsByStatus(email)`: PENDING / MATCHED / (CANCELLED+COMPLETED).
  - `findSaleBidById(saleBidId, email)`.
- `SaleBankAccountRepository`: 기본 CRUD.

## 7. 외부 연동 / 비동기
- Kafka/스케줄러 없음. 알림은 notification 도메인 `NotificationCommandService#createNotification` 동기 호출.

## 8. 다른 도메인 의존
- user: `UserQueryService`, `BankAccountQueryService#getBankAccount`(`domain/user/service/bankaccount`), `BankAccountInfoDto`.
- product: `ProductSizeQueryService`. order: `OrderBidQueryService#findById`, `OrderBid#assignSale/updateStatus`.
- shipment: `SellerShipmentCommandService#createSellerShipment`. notification: `NotificationCommandService`.
- 역방향 사용처: order(`OrderBidCommandService`/`OrderCommandService`가 SaleBid 매칭), payment(`PaymentCommandService`가 Sale→SOLD), shipment(`OrderShipmentCommandService`가 Sale→SOLD, SaleBid→COMPLETED; `SellerShipmentCommandService`가 Sale→IN_STORAGE), warehouseStorage(Sale 연관), `DataInitializer`.

## 9. 예외
- `SaleErrorCode` prefix **S**: S001~S003(Sale), S101~S104(SaleBid), S201(계좌), S301(즉시판매), S401~S402(배송), S501(ACCESS_DENIED). `SaleException extends GlobalException`.
- **실제 sale 서비스 코드는 SaleException을 던지지 않음** — 모두 `IllegalArgumentException`/`IllegalStateException` 사용. SaleException은 `GlobalExceptionHandler`에서만 참조.
- ShipmentErrorCode도 S001~ 코드를 사용 → 코드 문자열 충돌.

## 10. 작업 팁 / 주의점
- 일반 판매 입찰도 `Sale#assignSaleBid`(Sale.java:59) 때문에 생성 직후 SaleBid.status=MATCHED. `countSaleBidsByStatus`의 PENDING 카운트에 잡히지 않음.
- `createSaleBid`는 isWarehouseStorage=true여도 WarehouseStorage를 만들지 않음 → 판매자 발송 시 `SellerShipmentCommandService`가 생성.
- `POST /sales/{saleId}/shipment` + 창고보관 판매: `SellerShipmentCommandService`가 먼저 Sale을 IN_STORAGE로 바꾼 뒤 `updateSaleStatus(IN_TRANSIT)` 호출 → IN_STORAGE→IN_TRANSIT 불허로 IllegalStateException (트랜잭션 롤백).
- IN_INSPECTION / FAILED_INSPECTION / ON_AUCTION / SELLING / AUCTION_EXPIRED를 설정하는 코드 없음 (검수 단계 미구현). `domain/inspection`은 거래와 무관.
- SOLD 전환은 payment(`notifyUsersAndUpdateSaleStatus`, 검증 없음)와 shipment(`updateSaleStatusToSold`, `canTransitionTo` 검증) 양쪽에 존재.
- Sale.payment(mappedBy)를 설정하는 서비스 코드 없음 — Payment는 항상 order 쪽에 연결.
