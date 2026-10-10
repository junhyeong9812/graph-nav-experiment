# 도메인: warehouseStorage (창고 보관)

> 기준 패키지: `com.fream.back.domain.warehouseStorage` — 경로 `src/main/java/com/fream/back/domain/warehouseStorage/`

## 1. 역할 요약
- 판매자 상품(창고보관 판매) 또는 구매자 상품(창고보관 구매)을 창고에 보관하는 기록 관리.
- 보관 레코드 생성은 다른 도메인이 호출: 판매자 발송 시(`SellerShipmentCommandService`), 주문 결제 시(`OrderCommandService`, `OrderEventConsumer`).
- 자체 API는 상태별 개수 조회와 보관 기간 연장 2개뿐.

## 2. 패키지/파일 맵
| 레이어 | 경로 | 용도 |
|---|---|---|
| controller | `src/main/java/com/fream/back/domain/warehouseStorage/controller/command/WarehouseStorageCommandController.java` | 보관 기간 연장 |
| controller | `src/main/java/com/fream/back/domain/warehouseStorage/controller/query/WarehouseStorageQueryController.java` | 상태별 개수 |
| service | `src/main/java/com/fream/back/domain/warehouseStorage/service/command/WarehouseStorageCommandService.java` | 생성(주문/판매), 상태 변경, SOLD 처리, 기간 연장, 전환 검증 (클래스 레벨 `@Transactional`) |
| service | `src/main/java/com/fream/back/domain/warehouseStorage/service/query/WarehouseStorageQueryService.java` | findBySale, 상태 카운트, findStorageById |
| entity | `src/main/java/com/fream/back/domain/warehouseStorage/entity/WarehouseStorage.java` | 보관 엔티티 (BaseTimeEntity 미상속) |
| entity | `src/main/java/com/fream/back/domain/warehouseStorage/entity/WarehouseStatus.java` | 상태 enum |
| repository | `src/main/java/com/fream/back/domain/warehouseStorage/repository/WarehouseStorageRepository.java` | `findBySale(Sale)`, `findByUser_Email` |
| dto | `src/main/java/com/fream/back/domain/warehouseStorage/dto/StorageExtensionRequestDto.java` | storageId, newEndDate |
| dto | `src/main/java/com/fream/back/domain/warehouseStorage/dto/StorageExtensionResponseDto.java` | success/message/storageId/newEndDate/extendedDays |
| dto | `src/main/java/com/fream/back/domain/warehouseStorage/dto/WarehouseStatusCountDto.java` | 5개 상태 카운트 |
| exception | `src/main/java/com/fream/back/domain/warehouseStorage/exception/` | `WarehouseStorageErrorCode`, 기반 `WarehouseStorageException` + 하위 예외 11종 |

## 3. REST 엔드포인트
| HTTP | 경로 | 컨트롤러 메서드 | 서비스 메서드 |
|---|---|---|---|
| POST | `/warehouse-storage/commands/extend-period` | `WarehouseStorageCommandController.extendStoragePeriod` | `WarehouseStorageQueryService.findStorageById`(이전 종료일) → `WarehouseStorageCommandService.extendStoragePeriod` → `StorageExtensionResponseDto.success` |
| GET | `/warehouse-storage/queries/status-count` | `WarehouseStorageQueryController.getWarehouseStatusCount` | `WarehouseStorageQueryService.getWarehouseStatusCount(email)` |

이메일 없으면 `WarehouseStorageAccessDeniedException`. 연장 컨트롤러는 storageId/newEndDate null, 과거 날짜를 `StorageExtensionFailedException`으로 선검증.

## 4. 핵심 엔티티와 관계
`WarehouseStorage`: id, `user` ManyToOne(FK user_id, not null), `order` OneToOne(FK order_id, nullable, 역방향 `Order.warehouseStorage`), `sale` OneToOne(FK sale_id, nullable), storageLocation, startDate, endDate, `status: WarehouseStatus`, `isLinkedToOrder`.

엔티티 메서드: `assignOrder`(sale=null, isLinkedToOrder=true, status=ASSOCIATED_WITH_ORDER), `assignSale`(order=null, IN_STORAGE), `markAsReturned`(REMOVED_FROM_STORAGE, 호출처 없음), `updateStatus`, `setStorageDates`, `extendStorage`, `updateEndDate`(과거면 IllegalArgumentException), `getRemainingDays`, `isExpired`, `getTotalStorageDays`.

### WarehouseStatus 및 전환 (`WarehouseStorageCommandService.validateStatusTransition`, private)
| 현재 | 규칙 |
|---|---|
| (동일 상태) | 항상 허용 |
| IN_STORAGE | 모든 상태로 허용 |
| ASSOCIATED_WITH_ORDER | IN_STORAGE로만 불가 |
| ON_AUCTION | IN_STORAGE, SOLD만 허용 |
| SOLD | 전환 불가 |
| REMOVED_FROM_STORAGE | 전환 불가 |
위반 시 `InvalidStatusTransitionException`(WH003).

### 상태 설정 위치
| 값 | 설정 메서드 |
|---|---|
| IN_STORAGE | `createSellerStorage`, `createOrderStorage` (빌더) |
| ASSOCIATED_WITH_ORDER | `OrderCommandService.createInstantOrder` → `updateWarehouseStatus(sale, ASSOCIATED_WITH_ORDER)` (창고보관 Sale을 즉시 구매할 때) |
| SOLD | `updateWarehouseStatusToSold(sale)` ← `OrderShipmentCommandService.updateSaleStatusToSold` / `updateTrackingInfo` |
| ON_AUCTION, REMOVED_FROM_STORAGE | 설정 코드 없음 (`markAsReturned` 미호출) |

## 5. 주요 서비스 메서드
| 메서드 | 규칙 / 예외 |
|---|---|
| `createOrderStorage(order, user)` | null 검사, `order.getWarehouseStorage()!=null`이면 실패; location "Default Location", IN_STORAGE, 오늘~+1개월. 기타 예외 → `WarehouseStorageProcessingFailedException` |
| `createSellerStorage(sale, seller)` | null 검사, `findBySale` 존재 시 실패; location "Seller's Warehouse", IN_STORAGE, 오늘~+1개월 |
| `updateWarehouseStatus(sale, newStatus)` | `findBySale`(없으면 `WarehouseStorageNotFoundException`) → 전환 검증 → 변경 |
| `updateWarehouseStatusToSold(sale)` | `sale.isWarehouseStorage()`일 때만; 이미 SOLD면 `InvalidStatusTransitionException` |
| `extendStoragePeriod(storageId, newEndDate, email)` | 없음 → NotFound, 소유자 불일치 → AccessDenied, SOLD/REMOVED → `StorageExtensionFailedException`, `updateEndDate`. 이전보다 이른 날짜(미래라면)도 허용됨 |
| `getWarehouseStatusCount(email)` | 사용자 전체 조회 후 메모리 카운트 |

## 6. 다른 도메인과의 의존
| 호출자 | 호출 |
|---|---|
| `shipment/service/command/SellerShipmentCommandService.createSellerShipment` | `createSellerStorage` (Sale.isWarehouseStorage일 때) |
| `shipment/service/command/OrderShipmentCommandService` | `updateWarehouseStatusToSold` |
| `order/service/command/OrderCommandService.processPaymentAndShipment` | `createOrderStorage` + `order.assignWarehouseStorage` |
| `order/service/command/OrderCommandService.createInstantOrder` | `updateWarehouseStatus(sale, ASSOCIATED_WITH_ORDER)` |
| `order/service/kafka/OrderEventConsumer.processCompleteOrder` | `createOrderStorage` (Kafka `order-processing-topic` 경로) |
| `global/config/DataInitializer` | 시드 |
| `global/exception/GlobalExceptionHandler.handleWarehouseStorageException` | 예외 응답 |

Kafka/Redis/스케줄러: 이 도메인 내 없음 (만료 처리 스케줄러 없음).

## 7. 예외/에러코드
`WarehouseStorageErrorCode`: WH001 NOT_FOUND(404), WH002 INVALID_WAREHOUSE_STATUS(400), WH003 INVALID_STATUS_TRANSITION(400), WH004 STORAGE_PERIOD_EXPIRED(400), WH005 PROCESSING_FAILED(500), WH006 ACCESS_DENIED(403), WH007 CAPACITY_EXCEEDED(400), WH008 STORAGE_EXTENSION_FAILED(400), WH009 STORAGE_LOCATION_ALLOCATION_FAILED(500), WH010 ITEM_STATUS_CHECK_FAILED(500). 각 코드별 예외 클래스가 `exception/` 에 존재.

## 8. 관련 테스트
`src/test/java/com/fream/back/domain/warehouseStorage/**` 없음. 간접: `src/test/java/com/fream/back/domain/order/OrderCommandServiceTest.java` (grep으로 warehouse 언급 확인).

## 9. 자주 할 작업 → 여기를 보라
| 작업 | 위치 |
|---|---|
| 전환 규칙 변경 | `WarehouseStorageCommandService.validateStatusTransition` |
| 기본 보관 기간/위치 | `createOrderStorage`, `createSellerStorage` |
| 연장 정책(최대기간/요금) | `extendStoragePeriod` + 컨트롤러 선검증 |
| 만료/반송 처리 추가 | `WarehouseStorage.isExpired`, `markAsReturned` (현재 호출처 없음) |

## 10. Gotchas
1. `createOrderStorage`는 빌더로 order를 넣어 **status=IN_STORAGE, isLinkedToOrder=false** 로 저장 (`assignOrder`를 거치지 않음). 이후 `Order.assignWarehouseStorage`도 `getOrder()==this`라 `assignOrder`를 호출하지 않음.
2. 연장 컨트롤러는 서비스 호출 전 `findStorageById`로 엔티티를 별도 조회(이전 종료일 계산용).
3. `startDate/endDate`가 null인 레코드면 `getRemainingDays` 등이 NPE 가능 (생성 메서드는 항상 설정).
4. `updateWarehouseStatusToSold`는 `sale.isWarehouseStorage()==false`면 조용히 무시.
