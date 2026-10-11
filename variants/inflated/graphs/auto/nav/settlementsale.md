# settlementsale 도메인

기준 경로: `src/main/java/com/fream/back/domain/settlementsale/` · 파일 10개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/settlement/sale-bids` | `SettlementSaleBidCommandController#createSaleBid` | `SettlementSaleBidCommandService#createSaleBid` |
| DELETE | `/settlement/sale-bids/{saleBidId}` | `SettlementSaleBidCommandController#deleteSaleBid` | `SettlementSaleBidCommandService#deleteSaleBid` |
| POST | `/settlement/sale-bids/instant` | `SettlementSaleBidCommandController#createInstantSale` | `SettlementSaleBidCommandService#createInstantSaleBid` |
| POST | `/settlement/sales/{saleId}/shipment` | `SettlementSaleCommandController#createSellerShipment` | `SettlementSaleCommandService#createSellerShipment` |
| GET | `/settlement/sale-bids` | `SettlementSaleBidQueryController#getSaleBids` | `SettlementSaleBidQueryService#getSaleBids` |
| GET | `/settlement/sale-bids/count` | `SettlementSaleBidQueryController#getSaleBidStatusCounts` | `SettlementSaleBidQueryService#getSaleBidStatusCounts` |
| GET | `/settlement/sale-bids/{saleBidId}` | `SettlementSaleBidQueryController#getSaleBidDetail` | `SettlementSaleBidQueryService#getSaleBidDetail` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `SettlementSaleBankAccountCommandService`
  - `createSaleBankAccount` → SettlementSaleBankAccountRepository#save~
- `SettlementSaleBidCommandService`
  - `createSaleBid` → SettlementUserQueryService#findByEmail, SettlementProductSizeQueryService#findById, SettlementSaleCommandService#createSale, SaleBidRepository#save~
  - `createInstantSaleBid` → SettlementSaleCommandService#createInstantSale, SettlementOrderBidQueryService#findById, SaleBidRepository#save~
  - `deleteSaleBid` → SaleBidRepository#findById, SettlementSaleCommandService#deleteSale, SaleBidRepository#delete~
- `SettlementSaleBidQueryService`
  - `findById` → SaleBidRepository#findById
  - `findByOrderId` → SaleBidRepository#findByOrder_Id
  - `findBySaleId` → SaleBidRepository#findBySale_Id
  - `getSaleBids` → SaleBidRepository#findSaleBidsByFilters
  - `getSaleBidStatusCounts` → SaleBidRepository#countSaleBidsByStatus
  - `getSaleBidDetail` → SaleBidRepository#findSaleBidById
- `SettlementSaleCommandService`
  - `createInstantSale` → SettlementOrderBidQueryService#findById, SettlementUserQueryService#findByEmail, SaleRepository#save~, SettlementBankAccountQueryService#getBankAccount, SettlementSaleBankAccountCommandService#createSaleBankAccount, SettlementNotificationCommandService#createNotification
  - `createSale` → SettlementBankAccountQueryService#getBankAccount, SaleRepository#save~, SettlementSaleBankAccountCommandService#createSaleBankAccount, SettlementNotificationCommandService#createNotification
  - `createSellerShipment` → SettlementSellerShipmentCommandService#createSellerShipment, this#updateSaleStatus
  - `updateSaleStatus` → SaleRepository#findById~
  - `deleteSale` → SaleRepository#findById~, SaleRepository#delete~

## 3. 클래스 목록 (역할별)

### controller (3)

- `SettlementSaleBidCommandController` controller/command/SettlementSaleBidCommandController.java · public: createSaleBid, deleteSaleBid, createInstantSale · 주입: SettlementSaleBidCommandService
- `SettlementSaleCommandController` controller/command/SettlementSaleCommandController.java · public: createSellerShipment · 주입: SettlementSaleCommandService
- `SettlementSaleBidQueryController` controller/query/SettlementSaleBidQueryController.java · public: getSaleBids, getSaleBidStatusCounts, getSaleBidDetail · 주입: SettlementSaleBidQueryService

### service-command (3)

- `SettlementSaleBankAccountCommandService` service/command/SettlementSaleBankAccountCommandService.java · public: createSaleBankAccount · 주입: SettlementSaleBankAccountRepository
- `SettlementSaleBidCommandService` service/command/SettlementSaleBidCommandService.java · public: createSaleBid, createInstantSaleBid, deleteSaleBid · 주입: SaleBidRepository, SettlementUserQueryService, SettlementProductSizeQueryService, SettlementSaleCommandService, SettlementOrderBidQueryService
- `SettlementSaleCommandService` service/command/SettlementSaleCommandService.java · public: createInstantSale, createSale, createSellerShipment, updateSaleStatus, deleteSale · 주입: SaleRepository, SettlementOrderBidQueryService, SettlementNotificationCommandService, SettlementUserQueryService, SettlementSellerShipmentCommandService, SettlementBankAccountQueryService, SettlementSaleBankAccountCommandService

### service-query (1)

- `SettlementSaleBidQueryService` service/query/SettlementSaleBidQueryService.java · public: findById, findByOrderId, findBySaleId, getSaleBids, getSaleBidStatusCounts, getSaleBidDetail · 주입: SaleBidRepository

### repository (1)

- `SettlementSaleBankAccountRepository` (interface) repository/SettlementSaleBankAccountRepository.java · extends/implements JpaRepository

### custom (2)

- `SettlementSaleBidRepositoryCustom` (interface) repository/SettlementSaleBidRepositoryCustom.java · public: findSaleBidsByFilters, countSaleBidsByStatus, findSaleBidById
- `SettlementSaleBidRepositoryImpl` repository/SettlementSaleBidRepositoryImpl.java · extends/implements SettlementSaleBidRepositoryCustom · public: findSaleBidsByFilters, countSaleBidsByStatus, findSaleBidById · 주입: JPAQueryFactory

### 다른 도메인 참조 (import·주입 기준)

- **notification**: `NotificationCategory` ← SettlementSaleCommandService; `NotificationType` ← SettlementSaleCommandService
- **order**: `BidStatus` ← SettlementSaleCommandService; `OrderBid` ← SettlementSaleBidCommandService, SettlementSaleCommandService
- **product**: `ProductSize` ← SettlementSaleBidCommandService, SettlementSaleCommandService
- **sale**: `BidStatus` ← SettlementSaleBidCommandService, SettlementSaleBidRepositoryImpl; `Sale` ← SettlementSaleBankAccountCommandService, SettlementSaleBidCommandService, SettlementSaleCommandService; `SaleBankAccount` ← SettlementSaleBankAccountCommandService, SettlementSaleBankAccountRepository, SettlementSaleCommandService; `SaleBid` ← SettlementSaleBidCommandService, SettlementSaleBidQueryService; `SaleBidRepository` ← SettlementSaleBidCommandService, SettlementSaleBidQueryService; `SaleRepository` ← SettlementSaleCommandService; `SaleStatus` ← SettlementSaleCommandService
- **settlementnotification**: `SettlementNotificationCommandService` ← SettlementSaleCommandService
- **settlementorder**: `SettlementOrderBidQueryService` ← SettlementSaleBidCommandService, SettlementSaleCommandService
- **settlementproduct**: `SettlementProductSizeQueryService` ← SettlementSaleBidCommandService
- **settlementshipment**: `SettlementSellerShipmentCommandService` ← SettlementSaleCommandService
- **settlementuser**: `SettlementBankAccountQueryService` ← SettlementSaleCommandService; `SettlementUserQueryService` ← SettlementSaleBidCommandService, SettlementSaleCommandService
- **shipment**: `SellerShipment` ← SettlementSaleCommandService
- **user**: `User` ← SettlementSaleBidCommandService, SettlementSaleCommandService

### 이 도메인을 참조하는 다른 도메인

- **settlementorder**: SettlementSaleBidQueryService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

