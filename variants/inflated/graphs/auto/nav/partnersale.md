# partnersale 도메인

기준 경로: `src/main/java/com/fream/back/domain/partnersale/` · 파일 10개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/partner/sale-bids` | `PartnerSaleBidCommandController#createSaleBid` | `PartnerSaleBidCommandService#createSaleBid` |
| DELETE | `/partner/sale-bids/{saleBidId}` | `PartnerSaleBidCommandController#deleteSaleBid` | `PartnerSaleBidCommandService#deleteSaleBid` |
| POST | `/partner/sale-bids/instant` | `PartnerSaleBidCommandController#createInstantSale` | `PartnerSaleBidCommandService#createInstantSaleBid` |
| POST | `/partner/sales/{saleId}/shipment` | `PartnerSaleCommandController#createSellerShipment` | `PartnerSaleCommandService#createSellerShipment` |
| GET | `/partner/sale-bids` | `PartnerSaleBidQueryController#getSaleBids` | `PartnerSaleBidQueryService#getSaleBids` |
| GET | `/partner/sale-bids/count` | `PartnerSaleBidQueryController#getSaleBidStatusCounts` | `PartnerSaleBidQueryService#getSaleBidStatusCounts` |
| GET | `/partner/sale-bids/{saleBidId}` | `PartnerSaleBidQueryController#getSaleBidDetail` | `PartnerSaleBidQueryService#getSaleBidDetail` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `PartnerSaleBankAccountCommandService`
  - `createSaleBankAccount` → PartnerSaleBankAccountRepository#save~
- `PartnerSaleBidCommandService`
  - `createSaleBid` → PartnerUserQueryService#findByEmail, PartnerProductSizeQueryService#findById, PartnerSaleCommandService#createSale, SaleBidRepository#save~
  - `createInstantSaleBid` → PartnerSaleCommandService#createInstantSale, PartnerOrderBidQueryService#findById, SaleBidRepository#save~
  - `deleteSaleBid` → SaleBidRepository#findById, PartnerSaleCommandService#deleteSale, SaleBidRepository#delete~
- `PartnerSaleBidQueryService`
  - `findById` → SaleBidRepository#findById
  - `findByOrderId` → SaleBidRepository#findByOrder_Id
  - `findBySaleId` → SaleBidRepository#findBySale_Id
  - `getSaleBids` → SaleBidRepository#findSaleBidsByFilters
  - `getSaleBidStatusCounts` → SaleBidRepository#countSaleBidsByStatus
  - `getSaleBidDetail` → SaleBidRepository#findSaleBidById
- `PartnerSaleCommandService`
  - `createInstantSale` → PartnerOrderBidQueryService#findById, PartnerUserQueryService#findByEmail, SaleRepository#save~, PartnerBankAccountQueryService#getBankAccount, PartnerSaleBankAccountCommandService#createSaleBankAccount, PartnerNotificationCommandService#createNotification
  - `createSale` → PartnerBankAccountQueryService#getBankAccount, SaleRepository#save~, PartnerSaleBankAccountCommandService#createSaleBankAccount, PartnerNotificationCommandService#createNotification
  - `createSellerShipment` → PartnerSellerShipmentCommandService#createSellerShipment, this#updateSaleStatus
  - `updateSaleStatus` → SaleRepository#findById~
  - `deleteSale` → SaleRepository#findById~, SaleRepository#delete~

## 3. 클래스 목록 (역할별)

### controller (3)

- `PartnerSaleBidCommandController` controller/command/PartnerSaleBidCommandController.java · public: createSaleBid, deleteSaleBid, createInstantSale · 주입: PartnerSaleBidCommandService
- `PartnerSaleCommandController` controller/command/PartnerSaleCommandController.java · public: createSellerShipment · 주입: PartnerSaleCommandService
- `PartnerSaleBidQueryController` controller/query/PartnerSaleBidQueryController.java · public: getSaleBids, getSaleBidStatusCounts, getSaleBidDetail · 주입: PartnerSaleBidQueryService

### service-command (3)

- `PartnerSaleBankAccountCommandService` service/command/PartnerSaleBankAccountCommandService.java · public: createSaleBankAccount · 주입: PartnerSaleBankAccountRepository
- `PartnerSaleBidCommandService` service/command/PartnerSaleBidCommandService.java · public: createSaleBid, createInstantSaleBid, deleteSaleBid · 주입: SaleBidRepository, PartnerUserQueryService, PartnerProductSizeQueryService, PartnerSaleCommandService, PartnerOrderBidQueryService
- `PartnerSaleCommandService` service/command/PartnerSaleCommandService.java · public: createInstantSale, createSale, createSellerShipment, updateSaleStatus, deleteSale · 주입: SaleRepository, PartnerOrderBidQueryService, PartnerNotificationCommandService, PartnerUserQueryService, PartnerSellerShipmentCommandService, PartnerBankAccountQueryService, PartnerSaleBankAccountCommandService

### service-query (1)

- `PartnerSaleBidQueryService` service/query/PartnerSaleBidQueryService.java · public: findById, findByOrderId, findBySaleId, getSaleBids, getSaleBidStatusCounts, getSaleBidDetail · 주입: SaleBidRepository

### repository (1)

- `PartnerSaleBankAccountRepository` (interface) repository/PartnerSaleBankAccountRepository.java · extends/implements JpaRepository

### custom (2)

- `PartnerSaleBidRepositoryCustom` (interface) repository/PartnerSaleBidRepositoryCustom.java · public: findSaleBidsByFilters, countSaleBidsByStatus, findSaleBidById
- `PartnerSaleBidRepositoryImpl` repository/PartnerSaleBidRepositoryImpl.java · extends/implements PartnerSaleBidRepositoryCustom · public: findSaleBidsByFilters, countSaleBidsByStatus, findSaleBidById · 주입: JPAQueryFactory

### 다른 도메인 참조 (import·주입 기준)

- **notification**: `NotificationCategory` ← PartnerSaleCommandService; `NotificationType` ← PartnerSaleCommandService
- **order**: `BidStatus` ← PartnerSaleCommandService; `OrderBid` ← PartnerSaleBidCommandService, PartnerSaleCommandService
- **partnernotification**: `PartnerNotificationCommandService` ← PartnerSaleCommandService
- **partnerorder**: `PartnerOrderBidQueryService` ← PartnerSaleBidCommandService, PartnerSaleCommandService
- **partnerproduct**: `PartnerProductSizeQueryService` ← PartnerSaleBidCommandService
- **partnershipment**: `PartnerSellerShipmentCommandService` ← PartnerSaleCommandService
- **partneruser**: `PartnerBankAccountQueryService` ← PartnerSaleCommandService; `PartnerUserQueryService` ← PartnerSaleBidCommandService, PartnerSaleCommandService
- **product**: `ProductSize` ← PartnerSaleBidCommandService, PartnerSaleCommandService
- **sale**: `BidStatus` ← PartnerSaleBidCommandService, PartnerSaleBidRepositoryImpl; `Sale` ← PartnerSaleBankAccountCommandService, PartnerSaleBidCommandService, PartnerSaleCommandService; `SaleBankAccount` ← PartnerSaleBankAccountCommandService, PartnerSaleBankAccountRepository, PartnerSaleCommandService; `SaleBid` ← PartnerSaleBidCommandService, PartnerSaleBidQueryService; `SaleBidRepository` ← PartnerSaleBidCommandService, PartnerSaleBidQueryService; `SaleRepository` ← PartnerSaleCommandService; `SaleStatus` ← PartnerSaleCommandService
- **shipment**: `SellerShipment` ← PartnerSaleCommandService
- **user**: `User` ← PartnerSaleBidCommandService, PartnerSaleCommandService

### 이 도메인을 참조하는 다른 도메인

- **partnerorder**: PartnerSaleBidQueryService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

