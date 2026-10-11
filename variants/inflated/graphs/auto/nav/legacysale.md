# legacysale 도메인

기준 경로: `src/main/java/com/fream/back/domain/legacysale/` · 파일 10개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/legacy/sale-bids` | `LegacySaleBidCommandController#createSaleBid` | `LegacySaleBidCommandService#createSaleBid` |
| DELETE | `/legacy/sale-bids/{saleBidId}` | `LegacySaleBidCommandController#deleteSaleBid` | `LegacySaleBidCommandService#deleteSaleBid` |
| POST | `/legacy/sale-bids/instant` | `LegacySaleBidCommandController#createInstantSale` | `LegacySaleBidCommandService#createInstantSaleBid` |
| POST | `/legacy/sales/{saleId}/shipment` | `LegacySaleCommandController#createSellerShipment` | `LegacySaleCommandService#createSellerShipment` |
| GET | `/legacy/sale-bids` | `LegacySaleBidQueryController#getSaleBids` | `LegacySaleBidQueryService#getSaleBids` |
| GET | `/legacy/sale-bids/count` | `LegacySaleBidQueryController#getSaleBidStatusCounts` | `LegacySaleBidQueryService#getSaleBidStatusCounts` |
| GET | `/legacy/sale-bids/{saleBidId}` | `LegacySaleBidQueryController#getSaleBidDetail` | `LegacySaleBidQueryService#getSaleBidDetail` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `LegacySaleBankAccountCommandService`
  - `createSaleBankAccount` → LegacySaleBankAccountRepository#save~
- `LegacySaleBidCommandService`
  - `createSaleBid` → LegacyUserQueryService#findByEmail, LegacyProductSizeQueryService#findById, LegacySaleCommandService#createSale, SaleBidRepository#save~
  - `createInstantSaleBid` → LegacySaleCommandService#createInstantSale, LegacyOrderBidQueryService#findById, SaleBidRepository#save~
  - `deleteSaleBid` → SaleBidRepository#findById, LegacySaleCommandService#deleteSale, SaleBidRepository#delete~
- `LegacySaleBidQueryService`
  - `findById` → SaleBidRepository#findById
  - `findByOrderId` → SaleBidRepository#findByOrder_Id
  - `findBySaleId` → SaleBidRepository#findBySale_Id
  - `getSaleBids` → SaleBidRepository#findSaleBidsByFilters
  - `getSaleBidStatusCounts` → SaleBidRepository#countSaleBidsByStatus
  - `getSaleBidDetail` → SaleBidRepository#findSaleBidById
- `LegacySaleCommandService`
  - `createInstantSale` → LegacyOrderBidQueryService#findById, LegacyUserQueryService#findByEmail, SaleRepository#save~, LegacyBankAccountQueryService#getBankAccount, LegacySaleBankAccountCommandService#createSaleBankAccount, LegacyNotificationCommandService#createNotification
  - `createSale` → LegacyBankAccountQueryService#getBankAccount, SaleRepository#save~, LegacySaleBankAccountCommandService#createSaleBankAccount, LegacyNotificationCommandService#createNotification
  - `createSellerShipment` → LegacySellerShipmentCommandService#createSellerShipment, this#updateSaleStatus
  - `updateSaleStatus` → SaleRepository#findById~
  - `deleteSale` → SaleRepository#findById~, SaleRepository#delete~

## 3. 클래스 목록 (역할별)

### controller (3)

- `LegacySaleBidCommandController` controller/command/LegacySaleBidCommandController.java · public: createSaleBid, deleteSaleBid, createInstantSale · 주입: LegacySaleBidCommandService
- `LegacySaleCommandController` controller/command/LegacySaleCommandController.java · public: createSellerShipment · 주입: LegacySaleCommandService
- `LegacySaleBidQueryController` controller/query/LegacySaleBidQueryController.java · public: getSaleBids, getSaleBidStatusCounts, getSaleBidDetail · 주입: LegacySaleBidQueryService

### service-command (3)

- `LegacySaleBankAccountCommandService` service/command/LegacySaleBankAccountCommandService.java · public: createSaleBankAccount · 주입: LegacySaleBankAccountRepository
- `LegacySaleBidCommandService` service/command/LegacySaleBidCommandService.java · public: createSaleBid, createInstantSaleBid, deleteSaleBid · 주입: SaleBidRepository, LegacyUserQueryService, LegacyProductSizeQueryService, LegacySaleCommandService, LegacyOrderBidQueryService
- `LegacySaleCommandService` service/command/LegacySaleCommandService.java · public: createInstantSale, createSale, createSellerShipment, updateSaleStatus, deleteSale · 주입: SaleRepository, LegacyOrderBidQueryService, LegacyNotificationCommandService, LegacyUserQueryService, LegacySellerShipmentCommandService, LegacyBankAccountQueryService, LegacySaleBankAccountCommandService

### service-query (1)

- `LegacySaleBidQueryService` service/query/LegacySaleBidQueryService.java · public: findById, findByOrderId, findBySaleId, getSaleBids, getSaleBidStatusCounts, getSaleBidDetail · 주입: SaleBidRepository

### repository (1)

- `LegacySaleBankAccountRepository` (interface) repository/LegacySaleBankAccountRepository.java · extends/implements JpaRepository

### custom (2)

- `LegacySaleBidRepositoryCustom` (interface) repository/LegacySaleBidRepositoryCustom.java · public: findSaleBidsByFilters, countSaleBidsByStatus, findSaleBidById
- `LegacySaleBidRepositoryImpl` repository/LegacySaleBidRepositoryImpl.java · extends/implements LegacySaleBidRepositoryCustom · public: findSaleBidsByFilters, countSaleBidsByStatus, findSaleBidById · 주입: JPAQueryFactory

### 다른 도메인 참조 (import·주입 기준)

- **legacynotification**: `LegacyNotificationCommandService` ← LegacySaleCommandService
- **legacyorder**: `LegacyOrderBidQueryService` ← LegacySaleBidCommandService, LegacySaleCommandService
- **legacyproduct**: `LegacyProductSizeQueryService` ← LegacySaleBidCommandService
- **legacyshipment**: `LegacySellerShipmentCommandService` ← LegacySaleCommandService
- **legacyuser**: `LegacyBankAccountQueryService` ← LegacySaleCommandService; `LegacyUserQueryService` ← LegacySaleBidCommandService, LegacySaleCommandService
- **notification**: `NotificationCategory` ← LegacySaleCommandService; `NotificationType` ← LegacySaleCommandService
- **order**: `BidStatus` ← LegacySaleCommandService; `OrderBid` ← LegacySaleBidCommandService, LegacySaleCommandService
- **product**: `ProductSize` ← LegacySaleBidCommandService, LegacySaleCommandService
- **sale**: `BidStatus` ← LegacySaleBidCommandService, LegacySaleBidRepositoryImpl; `Sale` ← LegacySaleBankAccountCommandService, LegacySaleBidCommandService, LegacySaleCommandService; `SaleBankAccount` ← LegacySaleBankAccountCommandService, LegacySaleBankAccountRepository, LegacySaleCommandService; `SaleBid` ← LegacySaleBidCommandService, LegacySaleBidQueryService; `SaleBidRepository` ← LegacySaleBidCommandService, LegacySaleBidQueryService; `SaleRepository` ← LegacySaleCommandService; `SaleStatus` ← LegacySaleCommandService
- **shipment**: `SellerShipment` ← LegacySaleCommandService
- **user**: `User` ← LegacySaleBidCommandService, LegacySaleCommandService

### 이 도메인을 참조하는 다른 도메인

- **legacyorder**: LegacySaleBidQueryService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

