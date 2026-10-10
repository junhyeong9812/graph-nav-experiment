# warehouseStorage 도메인 네비게이션 맵

> 기준 경로: `src/main/java/com/fream/back/domain/warehouseStorage` (22 files). 코드에서 확인된 사실만 기재.

## 1. 개요
- 구매 후 창고 보관(Order 연결) 및 판매자 창고 보관(Sale 연결) 기록. 기본 보관기간 1개월, 사용자 보관기간 연장 API, 상태별 개수 조회.
- 생성/상태변경은 order·shipment 도메인에서 호출, 자체 API는 연장/카운트 2개.

## 2. 패키지/파일 구조
```
src/main/java/com/fream/back/domain/warehouseStorage/
├── controller/command/WarehouseStorageCommandController.java (/warehouse-storage/commands)
├── controller/query/WarehouseStorageQueryController.java     (/warehouse-storage/queries)
├── dto/ StorageExtensionRequestDto(storageId, newEndDate), StorageExtensionResponseDto, WarehouseStatusCountDto
├── entity/ WarehouseStorage, WarehouseStatus
├── exception/ WarehouseStorageErrorCode, WarehouseStorageException + 11 하위 예외
├── repository/WarehouseStorageRepository.java
└── service/command/WarehouseStorageCommandService.java, service/query/WarehouseStorageQueryService.java
```

## 3. 엔티티
`WarehouseStorage` (BaseTimeEntity 미상속): storageLocation, startDate, endDate(LocalDate), status(WarehouseStatus), isLinkedToOrder; user(ManyToOne, not null), order(1:1 FK nullable), sale(1:1 FK nullable).

**WarehouseStatus**: IN_STORAGE, ASSOCIATED_WITH_ORDER, REMOVED_FROM_STORAGE, ON_AUCTION, SOLD.

엔티티 메서드 (WarehouseStorage.java): `assignOrder`(:48, sale=null, isLinkedToOrder=true, status=ASSOCIATED_WITH_ORDER), `assignSale`(:55, order=null, status=IN_STORAGE), `markAsReturned`(REMOVED_FROM_STORAGE, 호출처 없음), `updateStatus`(검증 없음), `setStorageDates`, `extendStorage(days)`, `updateEndDate`(:84, 과거일 IllegalArgumentException), `getRemainingDays`, `isExpired`, `getTotalStorageDays`.

서비스 레벨 전이 검증 `WarehouseStorageCommandService#validateStatusTransition` (:232):
| from | 제약 |
|---|---|
| 동일 상태 | 허용 |
| IN_STORAGE | 모두 허용 |
| ASSOCIATED_WITH_ORDER | IN_STORAGE 불가 |
| ON_AUCTION | IN_STORAGE, SOLD만 |
| SOLD / REMOVED_FROM_STORAGE | 전부 불가 |

## 4. API 엔드포인트 (인증 필요)
| Method | Path | Controller#method | Service |
|---|---|---|---|
| POST | /warehouse-storage/commands/extend-period | WarehouseStorageCommandController#extendStoragePeriod | WarehouseStorageQueryService#findStorageById(이전 종료일) → WarehouseStorageCommandService#extendStoragePeriod |
| GET | /warehouse-storage/queries/status-count | WarehouseStorageQueryController#getWarehouseStatusCount | WarehouseStorageQueryService#getWarehouseStatusCount |

## 5. 서비스 계층
- **WarehouseStorageCommandService** (클래스 @Transactional)
  - `createOrderStorage(order, user)` (:33): 중복 시 예외. location "Default Location", **status IN_STORAGE**, start=today, end=+1개월. 호출: `OrderEventConsumer#processCompleteOrder`, `OrderCommandService#processPaymentAndShipment`, DataInitializer.
  - `createSellerStorage(sale, seller)` (:74): `findBySale` 중복검사, location "Seller's Warehouse", IN_STORAGE, +1개월. 호출: `SellerShipmentCommandService#createSellerShipment`.
  - `updateWarehouseStatus(sale, status)` (:117): 검증 후 변경. 호출: `OrderCommandService#createInstantOrder` (ASSOCIATED_WITH_ORDER).
  - `updateWarehouseStatusToSold(sale)` (:150): sale.isWarehouseStorage일 때만, 이미 SOLD면 InvalidStatusTransitionException. 호출: `OrderShipmentCommandService#updateTrackingInfo/updateSaleStatusToSold`.
  - `extendStoragePeriod(storageId, newEndDate, email)` (:189): 소유자 확인, SOLD/REMOVED면 불가, `updateEndDate(newEndDate)` (절대 날짜 지정, 일수 가산 아님).
- **WarehouseStorageQueryService**: `findBySale`(없으면 WarehouseStorageNotFoundException), `getWarehouseStatusCount(email)`(메모리에서 상태별 count), `findStorageById`.

## 6. Repositories
`WarehouseStorageRepository`: `findBySale(Sale)`, `findByUser_Email(String)`. 커스텀 쿼리 없음.

## 7. 외부 연동 / 비동기
- 없음 (만료 처리 스케줄러 없음; `StoragePeriodExpiredException`/`isExpired()` 사용처 없음).

## 8. 다른 도메인 의존
- order(`Order`, `Order#assignWarehouseStorage`), sale(`Sale`), user(`User`).
- 호출하는 쪽: order(`OrderCommandService`, `OrderEventConsumer`), shipment(`SellerShipmentCommandService`, `OrderShipmentCommandService`), `global/config/DataInitializer.java`.

## 9. 예외
- `WarehouseStorageErrorCode` prefix **WH**: WH001 NOT_FOUND, WH002 INVALID_WAREHOUSE_STATUS, WH003 INVALID_STATUS_TRANSITION, WH004 STORAGE_PERIOD_EXPIRED, WH005 PROCESSING_FAILED, WH006 ACCESS_DENIED, WH007 CAPACITY_EXCEEDED, WH008 STORAGE_EXTENSION_FAILED, WH009 LOCATION_ALLOCATION_FAILED, WH010 ITEM_STATUS_CHECK_FAILED. `WarehouseStorageException extends GlobalException`. order 쪽에서는 `OrderWarehouseStorageProcessingFailedException`(ORD503)으로 재래핑.

## 10. 작업 팁 / 주의점
- `createOrderStorage`는 builder로 order를 지정하고 `assignOrder()`를 거치지 않으므로 구매 보관 건도 status=IN_STORAGE, isLinkedToOrder=false. 이어지는 `Order#assignWarehouseStorage`도 `getOrder()==this`라 `assignOrder` 미호출.
- ASSOCIATED_WITH_ORDER는 즉시구매 시 **판매자 측(Sale 연결) 보관 레코드**에 설정됨.
- `updateWarehouseStatus`/`updateWarehouseStatusToSold`는 Sale 기준 조회만 지원(Order 기준 조회 메서드 없음).
- ON_AUCTION을 설정하는 코드 없음.
