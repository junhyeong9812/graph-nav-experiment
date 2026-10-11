# style 도메인

기준 경로: `src/main/java/com/fream/back/domain/style/` · 파일 24개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/sale-bids` | `StyleBidCommandController#createSaleBid` | `StyleBidCommandService#createSaleBid` |
| DELETE | `/sale-bids/{saleBidId}` | `StyleBidCommandController#deleteSaleBid` | `StyleBidCommandService#deleteSaleBid` |
| POST | `/sale-bids/instant` | `StyleBidCommandController#createInstantSale` | `StyleBidCommandService#createInstantSaleBid` |
| POST | `/sales/{saleId}/shipment` | `StyleCommandController#createSellerShipment` | `StyleCommandService#createSellerShipment` |
| GET | `/sale-bids` | `StyleBidQueryController#getSaleBids` | `StyleBidQueryService#getSaleBids` |
| GET | `/sale-bids/count` | `StyleBidQueryController#getSaleBidStatusCounts` | `StyleBidQueryService#getSaleBidStatusCounts` |
| GET | `/sale-bids/{saleBidId}` | `StyleBidQueryController#getSaleBidDetail` | `StyleBidQueryService#getSaleBidDetail` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `StyleBankAccountCommandService`
  - `createSaleBankAccount` → StyleBankAccountRepository#save~
- `StyleBidCommandService`
  - `createSaleBid` → ProductQueryService#findByEmail, UserSizeQueryService#findById, StyleCommandService#createSale, StyleBidRepository#save~
  - `createInstantSaleBid` → StyleCommandService#createInstantSale, FaqBidQueryService#findById, StyleBidRepository#save~
  - `deleteSaleBid` → StyleBidRepository#findById, StyleCommandService#deleteSale, StyleBidRepository#delete~
- `StyleBidQueryService`
  - `findById` → StyleBidRepository#findById
  - `findByOrderId` → StyleBidRepository#findByOrder_Id
  - `findBySaleId` → StyleBidRepository#findBySale_Id
  - `getSaleBids` → StyleBidRepository#findSaleBidsByFilters
  - `getSaleBidStatusCounts` → StyleBidRepository#countSaleBidsByStatus
  - `getSaleBidDetail` → StyleBidRepository#findSaleBidById
- `StyleBidStatusCountDto`
  - `getPendingRatio` → this#getTotalCount
  - `getMatchedRatio` → this#getTotalCount
  - `getCancelledOrCompletedRatio` → this#getTotalCount
- `StyleCommandService`
  - `createInstantSale` → FaqBidQueryService#findById, ProductQueryService#findByEmail, StyleRepository#save~, BankAccountQueryService#getBankAccount, StyleBankAccountCommandService#createSaleBankAccount, PaymentCommandService#createNotification
  - `createSale` → BankAccountQueryService#getBankAccount, StyleRepository#save~, StyleBankAccountCommandService#createSaleBankAccount, PaymentCommandService#createNotification
  - `createSellerShipment` → SellerAddressCommandService#createSellerShipment, this#updateSaleStatus
  - `updateSaleStatus` → StyleRepository#findById~
  - `deleteSale` → StyleRepository#findById~, StyleRepository#delete~

## 3. 클래스 목록 (역할별)

### controller (3)

- `StyleBidCommandController` controller/command/StyleBidCommandController.java · public: createSaleBid, deleteSaleBid, createInstantSale · 주입: StyleBidCommandService
- `StyleCommandController` controller/command/StyleCommandController.java · public: createSellerShipment · 주입: StyleCommandService
- `StyleBidQueryController` controller/query/StyleBidQueryController.java · public: getSaleBids, getSaleBidStatusCounts, getSaleBidDetail · 주입: StyleBidQueryService

### service-command (3)

- `StyleBankAccountCommandService` service/command/StyleBankAccountCommandService.java · public: createSaleBankAccount · 주입: StyleBankAccountRepository
- `StyleBidCommandService` service/command/StyleBidCommandService.java · public: createSaleBid, createInstantSaleBid, deleteSaleBid · 주입: StyleBidRepository, ProductQueryService, UserSizeQueryService, StyleCommandService, FaqBidQueryService
- `StyleCommandService` service/command/StyleCommandService.java · public: createInstantSale, createSale, createSellerShipment, updateSaleStatus, deleteSale · 주입: StyleRepository, FaqBidQueryService, PaymentCommandService, ProductQueryService, SellerAddressCommandService, BankAccountQueryService, StyleBankAccountCommandService

### service-query (1)

- `StyleBidQueryService` service/query/StyleBidQueryService.java · public: findById, findByOrderId, findBySaleId, getSaleBids, getSaleBidStatusCounts, getSaleBidDetail · 주입: StyleBidRepository

### repository (3)

- `StyleBankAccountRepository` (interface) repository/StyleBankAccountRepository.java · extends/implements JpaRepository
- `StyleBidRepository` (interface) repository/StyleBidRepository.java · extends/implements JpaRepository, StyleBidRepositoryCustom · public: findById, findByOrder_Id, findBySale_Id
- `StyleRepository` (interface) repository/StyleRepository.java · extends/implements JpaRepository · public: findBySeller_EmailAndStatus

### custom (2)

- `StyleBidRepositoryCustom` (interface) repository/StyleBidRepositoryCustom.java · public: findSaleBidsByFilters, countSaleBidsByStatus, findSaleBidById
- `StyleBidRepositoryImpl` repository/StyleBidRepositoryImpl.java · extends/implements StyleBidRepositoryCustom · public: findSaleBidsByFilters, countSaleBidsByStatus, findSaleBidById · 주입: JPAQueryFactory

### entity (5)

- `BidStatus` (enum) entity/BidStatus.java
- `Style` entity/Style.java · extends/implements BaseTimeEntity · public: assignSaleBankAccount, assignSaleBid, assignPayment, assignSellerShipment, updateStatus
- `StyleBankAccount` entity/StyleBankAccount.java · extends/implements BaseTimeEntity · public: assignSale
- `StyleBid` entity/StyleBid.java · extends/implements BaseTimeEntity · public: assignSale, assignOrder, markAsInstantSale, updateStatus
- `StyleStatus` (enum) entity/StyleStatus.java · public: canTransitionTo

### dto (5)

- `AddressRequestDto` dto/AddressRequestDto.java
- `InstantStyleRequestDto` dto/InstantStyleRequestDto.java
- `StyleBidRequestDto` dto/StyleBidRequestDto.java
- `StyleBidResponseDto` dto/StyleBidResponseDto.java
- `StyleBidStatusCountDto` dto/StyleBidStatusCountDto.java

### exception (2)

- `StyleErrorCode` exception/StyleErrorCode.java
- `StyleException` exception/StyleException.java

### 다른 도메인 참조 (import·주입 기준)

- **address**: `SellerAddress` ← Style, StyleCommandService; `SellerAddressCommandService` ← StyleCommandService
- **faq**: `BidStatus` ← StyleCommandService; `Faq` ← StyleBid; `FaqBid` ← StyleBidCommandService, StyleCommandService; `FaqBidQueryService` ← StyleBidCommandService, StyleCommandService
- **notification**: `Notification` ← Style
- **payment**: `PaymentCategory` ← StyleCommandService; `PaymentCommandService` ← StyleCommandService; `PaymentType` ← StyleCommandService
- **product**: `BankAccountQueryService` ← StyleCommandService; `Product` ← Style, StyleBid, StyleBidCommandService, StyleCommandService; `ProductQueryService` ← StyleBidCommandService, StyleCommandService
- **user**: `UserSize` ← Style, StyleBid, StyleBidCommandService, StyleCommandService; `UserSizeQueryService` ← StyleBidCommandService

### 이 도메인을 참조하는 다른 도메인

- **address**: BidStatus, Style, StyleBid, StyleBidQueryService, StyleRepository, StyleStatus
- **faq**: Style, StyleBid, StyleBidQueryService
- **global**: Style, StyleBid, StyleBidRepository, StyleRepository, StyleStatus
- **notice**: Style
- **notification**: Style, StyleBid, StyleBidQueryService, StyleStatus

## 4. JPA 엔티티 연관

- `Style` @Entity · extends BaseTimeEntity
  - seller: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "seller_id") → `Product` [product]
  - productSize: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_size_id") → `UserSize` [user]
  - payment: @OneToOne(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true) → `Notification` [notification]
  - saleBankAccount: @OneToOne(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true) → `StyleBankAccount`
  - saleBid: @OneToOne(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true) → `StyleBid`
  - sellerShipment: @OneToOne(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true) → `SellerAddress` [address]
- `StyleBankAccount` @Entity · extends BaseTimeEntity
  - sale: @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "sale_id", nullable = false) → `Style`
- `StyleBid` @Entity · extends BaseTimeEntity
  - seller: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "seller_id") → `Product` [product]
  - productSize: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_size_id") → `UserSize` [user]
  - sale: @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "sale_id") → `Style`
  - order: @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "order_id") → `Faq` [faq]

다른 도메인 엔티티에서 들어오는 연관:

- `SellerAddress.sale` @OneToOne → `Style` [address]
- `FaqBid.sale` @OneToOne → `Style` [faq]
- `Notice.sale` @OneToOne → `Style` [notice]
- `Notification.sale` @OneToOne → `Style` [notification]

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

