# sale 도메인

기준 경로: `src/main/java/com/fream/back/domain/sale/` · 파일 24개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/sale-bids` | `SaleBidCommandController#createSaleBid` | `SaleBidCommandService#createSaleBid` |
| DELETE | `/sale-bids/{saleBidId}` | `SaleBidCommandController#deleteSaleBid` | `SaleBidCommandService#deleteSaleBid` |
| POST | `/sale-bids/instant` | `SaleBidCommandController#createInstantSale` | `SaleBidCommandService#createInstantSaleBid` |
| POST | `/sales/{saleId}/shipment` | `SaleCommandController#createSellerShipment` | `SaleCommandService#createSellerShipment` |
| GET | `/sale-bids` | `SaleBidQueryController#getSaleBids` | `SaleBidQueryService#getSaleBids` |
| GET | `/sale-bids/count` | `SaleBidQueryController#getSaleBidStatusCounts` | `SaleBidQueryService#getSaleBidStatusCounts` |
| GET | `/sale-bids/{saleBidId}` | `SaleBidQueryController#getSaleBidDetail` | `SaleBidQueryService#getSaleBidDetail` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `SaleBankAccountCommandService`
  - `createSaleBankAccount` → SaleBankAccountRepository#save~
- `SaleBidCommandService`
  - `createSaleBid` → UserQueryService#findByEmail, ProductSizeQueryService#findById, SaleCommandService#createSale, SaleBidRepository#save~
  - `createInstantSaleBid` → SaleCommandService#createInstantSale, OrderBidQueryService#findById, SaleBidRepository#save~
  - `deleteSaleBid` → SaleBidRepository#findById, SaleCommandService#deleteSale, SaleBidRepository#delete~
- `SaleBidQueryService`
  - `findById` → SaleBidRepository#findById
  - `findByOrderId` → SaleBidRepository#findByOrder_Id
  - `findBySaleId` → SaleBidRepository#findBySale_Id
  - `getSaleBids` → SaleBidRepository#findSaleBidsByFilters
  - `getSaleBidStatusCounts` → SaleBidRepository#countSaleBidsByStatus
  - `getSaleBidDetail` → SaleBidRepository#findSaleBidById
- `SaleBidStatusCountDto`
  - `getPendingRatio` → this#getTotalCount
  - `getMatchedRatio` → this#getTotalCount
  - `getCancelledOrCompletedRatio` → this#getTotalCount
- `SaleCommandService`
  - `createInstantSale` → OrderBidQueryService#findById, UserQueryService#findByEmail, SaleRepository#save~, BankAccountQueryService#getBankAccount, SaleBankAccountCommandService#createSaleBankAccount, NotificationCommandService#createNotification
  - `createSale` → BankAccountQueryService#getBankAccount, SaleRepository#save~, SaleBankAccountCommandService#createSaleBankAccount, NotificationCommandService#createNotification
  - `createSellerShipment` → SellerShipmentCommandService#createSellerShipment, this#updateSaleStatus
  - `updateSaleStatus` → SaleRepository#findById~
  - `deleteSale` → SaleRepository#findById~, SaleRepository#delete~

## 3. 클래스 목록 (역할별)

### controller (3)

- `SaleBidCommandController` controller/command/SaleBidCommandController.java · public: createSaleBid, deleteSaleBid, createInstantSale · 주입: SaleBidCommandService
- `SaleCommandController` controller/command/SaleCommandController.java · public: createSellerShipment · 주입: SaleCommandService
- `SaleBidQueryController` controller/query/SaleBidQueryController.java · public: getSaleBids, getSaleBidStatusCounts, getSaleBidDetail · 주입: SaleBidQueryService

### service-command (3)

- `SaleBankAccountCommandService` service/command/SaleBankAccountCommandService.java · public: createSaleBankAccount · 주입: SaleBankAccountRepository
- `SaleBidCommandService` service/command/SaleBidCommandService.java · public: createSaleBid, createInstantSaleBid, deleteSaleBid · 주입: SaleBidRepository, UserQueryService, ProductSizeQueryService, SaleCommandService, OrderBidQueryService
- `SaleCommandService` service/command/SaleCommandService.java · public: createInstantSale, createSale, createSellerShipment, updateSaleStatus, deleteSale · 주입: SaleRepository, OrderBidQueryService, NotificationCommandService, UserQueryService, SellerShipmentCommandService, BankAccountQueryService, SaleBankAccountCommandService

### service-query (1)

- `SaleBidQueryService` service/query/SaleBidQueryService.java · public: findById, findByOrderId, findBySaleId, getSaleBids, getSaleBidStatusCounts, getSaleBidDetail · 주입: SaleBidRepository

### repository (3)

- `SaleBankAccountRepository` (interface) repository/SaleBankAccountRepository.java · extends/implements JpaRepository
- `SaleBidRepository` (interface) repository/SaleBidRepository.java · extends/implements JpaRepository, SaleBidRepositoryCustom · public: findById, findByOrder_Id, findBySale_Id
- `SaleRepository` (interface) repository/SaleRepository.java · extends/implements JpaRepository · public: findBySeller_EmailAndStatus

### custom (2)

- `SaleBidRepositoryCustom` (interface) repository/SaleBidRepositoryCustom.java · public: findSaleBidsByFilters, countSaleBidsByStatus, findSaleBidById
- `SaleBidRepositoryImpl` repository/SaleBidRepositoryImpl.java · extends/implements SaleBidRepositoryCustom · public: findSaleBidsByFilters, countSaleBidsByStatus, findSaleBidById · 주입: JPAQueryFactory

### entity (5)

- `BidStatus` (enum) entity/BidStatus.java
- `Sale` entity/Sale.java · extends/implements BaseTimeEntity · public: assignSaleBankAccount, assignSaleBid, assignPayment, assignSellerShipment, updateStatus
- `SaleBankAccount` entity/SaleBankAccount.java · extends/implements BaseTimeEntity · public: assignSale
- `SaleBid` entity/SaleBid.java · extends/implements BaseTimeEntity · public: assignSale, assignOrder, markAsInstantSale, updateStatus
- `SaleStatus` (enum) entity/SaleStatus.java · public: canTransitionTo

### dto (5)

- `InstantSaleRequestDto` dto/InstantSaleRequestDto.java
- `SaleBidRequestDto` dto/SaleBidRequestDto.java
- `SaleBidResponseDto` dto/SaleBidResponseDto.java
- `SaleBidStatusCountDto` dto/SaleBidStatusCountDto.java
- `ShipmentRequestDto` dto/ShipmentRequestDto.java

### exception (2)

- `SaleErrorCode` exception/SaleErrorCode.java
- `SaleException` exception/SaleException.java

### 다른 도메인 참조 (import·주입 기준)

- **notification**: `NotificationCategory` ← SaleCommandService; `NotificationCommandService` ← SaleCommandService; `NotificationType` ← SaleCommandService
- **order**: `BidStatus` ← SaleCommandService; `Order` ← SaleBid; `OrderBid` ← SaleBidCommandService, SaleCommandService; `OrderBidQueryService` ← SaleBidCommandService, SaleCommandService
- **payment**: `Payment` ← Sale
- **product**: `ProductSize` ← Sale, SaleBid, SaleBidCommandService, SaleCommandService; `ProductSizeQueryService` ← SaleBidCommandService
- **shipment**: `SellerShipment` ← Sale, SaleCommandService; `SellerShipmentCommandService` ← SaleCommandService
- **user**: `BankAccountQueryService` ← SaleCommandService; `User` ← Sale, SaleBid, SaleBidCommandService, SaleCommandService; `UserQueryService` ← SaleBidCommandService, SaleCommandService

### 이 도메인을 참조하는 다른 도메인

- **global**: Sale, SaleBid, SaleBidRepository, SaleRepository, SaleStatus
- **order**: Sale, SaleBid, SaleBidQueryService
- **payment**: Sale, SaleBid, SaleBidQueryService, SaleStatus
- **shipment**: BidStatus, Sale, SaleBid, SaleBidQueryService, SaleRepository, SaleStatus
- **warehouseStorage**: Sale

## 4. JPA 엔티티 연관

- `Sale` @Entity · extends BaseTimeEntity
  - seller: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "seller_id") → `User` [user]
  - productSize: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_size_id") → `ProductSize` [product]
  - payment: @OneToOne(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true) → `Payment` [payment]
  - saleBankAccount: @OneToOne(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true) → `SaleBankAccount`
  - saleBid: @OneToOne(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true) → `SaleBid`
  - sellerShipment: @OneToOne(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true) → `SellerShipment` [shipment]
- `SaleBankAccount` @Entity · extends BaseTimeEntity
  - sale: @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "sale_id", nullable = false) → `Sale`
- `SaleBid` @Entity · extends BaseTimeEntity
  - seller: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "seller_id") → `User` [user]
  - productSize: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_size_id") → `ProductSize` [product]
  - sale: @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "sale_id") → `Sale`
  - order: @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "order_id") → `Order` [order]

다른 도메인 엔티티에서 들어오는 연관:

- `OrderBid.sale` @OneToOne → `Sale` [order]
- `Payment.sale` @OneToOne → `Sale` [payment]
- `SellerShipment.sale` @OneToOne → `Sale` [shipment]
- `WarehouseStorage.sale` @OneToOne → `Sale` [warehouseStorage]

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

