# adminsale 도메인

기준 경로: `src/main/java/com/fream/back/domain/adminsale/` · 파일 10개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/admin/sale-bids` | `AdminSaleBidCommandController#createSaleBid` | `AdminSaleBidCommandService#createSaleBid` |
| DELETE | `/admin/sale-bids/{saleBidId}` | `AdminSaleBidCommandController#deleteSaleBid` | `AdminSaleBidCommandService#deleteSaleBid` |
| POST | `/admin/sale-bids/instant` | `AdminSaleBidCommandController#createInstantSale` | `AdminSaleBidCommandService#createInstantSaleBid` |
| POST | `/admin/sales/{saleId}/shipment` | `AdminSaleCommandController#createSellerShipment` | `AdminSaleCommandService#createSellerShipment` |
| GET | `/admin/sale-bids` | `AdminSaleBidQueryController#getSaleBids` | `AdminSaleBidQueryService#getSaleBids` |
| GET | `/admin/sale-bids/count` | `AdminSaleBidQueryController#getSaleBidStatusCounts` | `AdminSaleBidQueryService#getSaleBidStatusCounts` |
| GET | `/admin/sale-bids/{saleBidId}` | `AdminSaleBidQueryController#getSaleBidDetail` | `AdminSaleBidQueryService#getSaleBidDetail` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `AdminSaleBankAccountCommandService`
  - `createSaleBankAccount` → AdminSaleBankAccountRepository#save~
- `AdminSaleBidCommandService`
  - `createSaleBid` → AdminUserQueryService#findByEmail, AdminProductSizeQueryService#findById, AdminSaleCommandService#createSale, SaleBidRepository#save~
  - `createInstantSaleBid` → AdminSaleCommandService#createInstantSale, AdminOrderBidQueryService#findById, SaleBidRepository#save~
  - `deleteSaleBid` → SaleBidRepository#findById, AdminSaleCommandService#deleteSale, SaleBidRepository#delete~
- `AdminSaleBidQueryService`
  - `findById` → SaleBidRepository#findById
  - `findByOrderId` → SaleBidRepository#findByOrder_Id
  - `findBySaleId` → SaleBidRepository#findBySale_Id
  - `getSaleBids` → SaleBidRepository#findSaleBidsByFilters
  - `getSaleBidStatusCounts` → SaleBidRepository#countSaleBidsByStatus
  - `getSaleBidDetail` → SaleBidRepository#findSaleBidById
- `AdminSaleCommandService`
  - `createInstantSale` → AdminOrderBidQueryService#findById, AdminUserQueryService#findByEmail, SaleRepository#save~, AdminBankAccountQueryService#getBankAccount, AdminSaleBankAccountCommandService#createSaleBankAccount, AdminNotificationCommandService#createNotification
  - `createSale` → AdminBankAccountQueryService#getBankAccount, SaleRepository#save~, AdminSaleBankAccountCommandService#createSaleBankAccount, AdminNotificationCommandService#createNotification
  - `createSellerShipment` → AdminSellerShipmentCommandService#createSellerShipment, this#updateSaleStatus
  - `updateSaleStatus` → SaleRepository#findById~
  - `deleteSale` → SaleRepository#findById~, SaleRepository#delete~

## 3. 클래스 목록 (역할별)

### controller (3)

- `AdminSaleBidCommandController` controller/command/AdminSaleBidCommandController.java · public: createSaleBid, deleteSaleBid, createInstantSale · 주입: AdminSaleBidCommandService
- `AdminSaleCommandController` controller/command/AdminSaleCommandController.java · public: createSellerShipment · 주입: AdminSaleCommandService
- `AdminSaleBidQueryController` controller/query/AdminSaleBidQueryController.java · public: getSaleBids, getSaleBidStatusCounts, getSaleBidDetail · 주입: AdminSaleBidQueryService

### service-command (3)

- `AdminSaleBankAccountCommandService` service/command/AdminSaleBankAccountCommandService.java · public: createSaleBankAccount · 주입: AdminSaleBankAccountRepository
- `AdminSaleBidCommandService` service/command/AdminSaleBidCommandService.java · public: createSaleBid, createInstantSaleBid, deleteSaleBid · 주입: SaleBidRepository, AdminUserQueryService, AdminProductSizeQueryService, AdminSaleCommandService, AdminOrderBidQueryService
- `AdminSaleCommandService` service/command/AdminSaleCommandService.java · public: createInstantSale, createSale, createSellerShipment, updateSaleStatus, deleteSale · 주입: SaleRepository, AdminOrderBidQueryService, AdminNotificationCommandService, AdminUserQueryService, AdminSellerShipmentCommandService, AdminBankAccountQueryService, AdminSaleBankAccountCommandService

### service-query (1)

- `AdminSaleBidQueryService` service/query/AdminSaleBidQueryService.java · public: findById, findByOrderId, findBySaleId, getSaleBids, getSaleBidStatusCounts, getSaleBidDetail · 주입: SaleBidRepository

### repository (1)

- `AdminSaleBankAccountRepository` (interface) repository/AdminSaleBankAccountRepository.java · extends/implements JpaRepository

### custom (2)

- `AdminSaleBidRepositoryCustom` (interface) repository/AdminSaleBidRepositoryCustom.java · public: findSaleBidsByFilters, countSaleBidsByStatus, findSaleBidById
- `AdminSaleBidRepositoryImpl` repository/AdminSaleBidRepositoryImpl.java · extends/implements AdminSaleBidRepositoryCustom · public: findSaleBidsByFilters, countSaleBidsByStatus, findSaleBidById · 주입: JPAQueryFactory

### 다른 도메인 참조 (import·주입 기준)

- **adminnotification**: `AdminNotificationCommandService` ← AdminSaleCommandService
- **adminorder**: `AdminOrderBidQueryService` ← AdminSaleBidCommandService, AdminSaleCommandService
- **adminproduct**: `AdminProductSizeQueryService` ← AdminSaleBidCommandService
- **adminshipment**: `AdminSellerShipmentCommandService` ← AdminSaleCommandService
- **adminuser**: `AdminBankAccountQueryService` ← AdminSaleCommandService; `AdminUserQueryService` ← AdminSaleBidCommandService, AdminSaleCommandService
- **notification**: `NotificationCategory` ← AdminSaleCommandService; `NotificationType` ← AdminSaleCommandService
- **order**: `BidStatus` ← AdminSaleCommandService; `OrderBid` ← AdminSaleBidCommandService, AdminSaleCommandService
- **product**: `ProductSize` ← AdminSaleBidCommandService, AdminSaleCommandService
- **sale**: `BidStatus` ← AdminSaleBidCommandService, AdminSaleBidRepositoryImpl; `Sale` ← AdminSaleBankAccountCommandService, AdminSaleBidCommandService, AdminSaleCommandService; `SaleBankAccount` ← AdminSaleBankAccountCommandService, AdminSaleBankAccountRepository, AdminSaleCommandService; `SaleBid` ← AdminSaleBidCommandService, AdminSaleBidQueryService; `SaleBidRepository` ← AdminSaleBidCommandService, AdminSaleBidQueryService; `SaleRepository` ← AdminSaleCommandService; `SaleStatus` ← AdminSaleCommandService
- **shipment**: `SellerShipment` ← AdminSaleCommandService
- **user**: `User` ← AdminSaleBidCommandService, AdminSaleCommandService

### 이 도메인을 참조하는 다른 도메인

- **adminorder**: AdminSaleBidQueryService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

