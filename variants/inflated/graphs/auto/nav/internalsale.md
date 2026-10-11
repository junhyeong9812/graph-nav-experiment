# internalsale 도메인

기준 경로: `src/main/java/com/fream/back/domain/internalsale/` · 파일 10개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/internal/sale-bids` | `InternalSaleBidCommandController#createSaleBid` | `InternalSaleBidCommandService#createSaleBid` |
| DELETE | `/internal/sale-bids/{saleBidId}` | `InternalSaleBidCommandController#deleteSaleBid` | `InternalSaleBidCommandService#deleteSaleBid` |
| POST | `/internal/sale-bids/instant` | `InternalSaleBidCommandController#createInstantSale` | `InternalSaleBidCommandService#createInstantSaleBid` |
| POST | `/internal/sales/{saleId}/shipment` | `InternalSaleCommandController#createSellerShipment` | `InternalSaleCommandService#createSellerShipment` |
| GET | `/internal/sale-bids` | `InternalSaleBidQueryController#getSaleBids` | `InternalSaleBidQueryService#getSaleBids` |
| GET | `/internal/sale-bids/count` | `InternalSaleBidQueryController#getSaleBidStatusCounts` | `InternalSaleBidQueryService#getSaleBidStatusCounts` |
| GET | `/internal/sale-bids/{saleBidId}` | `InternalSaleBidQueryController#getSaleBidDetail` | `InternalSaleBidQueryService#getSaleBidDetail` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `InternalSaleBankAccountCommandService`
  - `createSaleBankAccount` → InternalSaleBankAccountRepository#save~
- `InternalSaleBidCommandService`
  - `createSaleBid` → InternalUserQueryService#findByEmail, InternalProductSizeQueryService#findById, InternalSaleCommandService#createSale, SaleBidRepository#save~
  - `createInstantSaleBid` → InternalSaleCommandService#createInstantSale, InternalOrderBidQueryService#findById, SaleBidRepository#save~
  - `deleteSaleBid` → SaleBidRepository#findById, InternalSaleCommandService#deleteSale, SaleBidRepository#delete~
- `InternalSaleBidQueryService`
  - `findById` → SaleBidRepository#findById
  - `findByOrderId` → SaleBidRepository#findByOrder_Id
  - `findBySaleId` → SaleBidRepository#findBySale_Id
  - `getSaleBids` → SaleBidRepository#findSaleBidsByFilters
  - `getSaleBidStatusCounts` → SaleBidRepository#countSaleBidsByStatus
  - `getSaleBidDetail` → SaleBidRepository#findSaleBidById
- `InternalSaleCommandService`
  - `createInstantSale` → InternalOrderBidQueryService#findById, InternalUserQueryService#findByEmail, SaleRepository#save~, InternalBankAccountQueryService#getBankAccount, InternalSaleBankAccountCommandService#createSaleBankAccount, InternalNotificationCommandService#createNotification
  - `createSale` → InternalBankAccountQueryService#getBankAccount, SaleRepository#save~, InternalSaleBankAccountCommandService#createSaleBankAccount, InternalNotificationCommandService#createNotification
  - `createSellerShipment` → InternalSellerShipmentCommandService#createSellerShipment, this#updateSaleStatus
  - `updateSaleStatus` → SaleRepository#findById~
  - `deleteSale` → SaleRepository#findById~, SaleRepository#delete~

## 3. 클래스 목록 (역할별)

### controller (3)

- `InternalSaleBidCommandController` controller/command/InternalSaleBidCommandController.java · public: createSaleBid, deleteSaleBid, createInstantSale · 주입: InternalSaleBidCommandService
- `InternalSaleCommandController` controller/command/InternalSaleCommandController.java · public: createSellerShipment · 주입: InternalSaleCommandService
- `InternalSaleBidQueryController` controller/query/InternalSaleBidQueryController.java · public: getSaleBids, getSaleBidStatusCounts, getSaleBidDetail · 주입: InternalSaleBidQueryService

### service-command (3)

- `InternalSaleBankAccountCommandService` service/command/InternalSaleBankAccountCommandService.java · public: createSaleBankAccount · 주입: InternalSaleBankAccountRepository
- `InternalSaleBidCommandService` service/command/InternalSaleBidCommandService.java · public: createSaleBid, createInstantSaleBid, deleteSaleBid · 주입: SaleBidRepository, InternalUserQueryService, InternalProductSizeQueryService, InternalSaleCommandService, InternalOrderBidQueryService
- `InternalSaleCommandService` service/command/InternalSaleCommandService.java · public: createInstantSale, createSale, createSellerShipment, updateSaleStatus, deleteSale · 주입: SaleRepository, InternalOrderBidQueryService, InternalNotificationCommandService, InternalUserQueryService, InternalSellerShipmentCommandService, InternalBankAccountQueryService, InternalSaleBankAccountCommandService

### service-query (1)

- `InternalSaleBidQueryService` service/query/InternalSaleBidQueryService.java · public: findById, findByOrderId, findBySaleId, getSaleBids, getSaleBidStatusCounts, getSaleBidDetail · 주입: SaleBidRepository

### repository (1)

- `InternalSaleBankAccountRepository` (interface) repository/InternalSaleBankAccountRepository.java · extends/implements JpaRepository

### custom (2)

- `InternalSaleBidRepositoryCustom` (interface) repository/InternalSaleBidRepositoryCustom.java · public: findSaleBidsByFilters, countSaleBidsByStatus, findSaleBidById
- `InternalSaleBidRepositoryImpl` repository/InternalSaleBidRepositoryImpl.java · extends/implements InternalSaleBidRepositoryCustom · public: findSaleBidsByFilters, countSaleBidsByStatus, findSaleBidById · 주입: JPAQueryFactory

### 다른 도메인 참조 (import·주입 기준)

- **internalnotification**: `InternalNotificationCommandService` ← InternalSaleCommandService
- **internalorder**: `InternalOrderBidQueryService` ← InternalSaleBidCommandService, InternalSaleCommandService
- **internalproduct**: `InternalProductSizeQueryService` ← InternalSaleBidCommandService
- **internalshipment**: `InternalSellerShipmentCommandService` ← InternalSaleCommandService
- **internaluser**: `InternalBankAccountQueryService` ← InternalSaleCommandService; `InternalUserQueryService` ← InternalSaleBidCommandService, InternalSaleCommandService
- **notification**: `NotificationCategory` ← InternalSaleCommandService; `NotificationType` ← InternalSaleCommandService
- **order**: `BidStatus` ← InternalSaleCommandService; `OrderBid` ← InternalSaleBidCommandService, InternalSaleCommandService
- **product**: `ProductSize` ← InternalSaleBidCommandService, InternalSaleCommandService
- **sale**: `BidStatus` ← InternalSaleBidCommandService, InternalSaleBidRepositoryImpl; `Sale` ← InternalSaleBankAccountCommandService, InternalSaleBidCommandService, InternalSaleCommandService; `SaleBankAccount` ← InternalSaleBankAccountCommandService, InternalSaleBankAccountRepository, InternalSaleCommandService; `SaleBid` ← InternalSaleBidCommandService, InternalSaleBidQueryService; `SaleBidRepository` ← InternalSaleBidCommandService, InternalSaleBidQueryService; `SaleRepository` ← InternalSaleCommandService; `SaleStatus` ← InternalSaleCommandService
- **shipment**: `SellerShipment` ← InternalSaleCommandService
- **user**: `User` ← InternalSaleBidCommandService, InternalSaleCommandService

### 이 도메인을 참조하는 다른 도메인

- **internalorder**: InternalSaleBidQueryService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

