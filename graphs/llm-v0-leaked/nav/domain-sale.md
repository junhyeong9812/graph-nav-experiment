# 도메인: sale (판매 / 판매 입찰)

> 기준 패키지: `com.fream.back.domain.sale` — 경로 `src/main/java/com/fream/back/domain/sale/`
> 엔드투엔드 판매 흐름은 `nav/flow-trade-sell.md`, 배송은 `nav/domain-shipment.md`, 창고는 `nav/domain-warehouse-storage.md` 참고.

## 1. 역할 요약
- 판매자가 **판매 입찰(SaleBid)** 을 등록하거나, 기존 **구매 입찰(OrderBid)에 즉시 판매**하는 기능.
- 판매 1건 = `Sale`(판매 상태/반송 주소/창고보관 여부) + `SaleBid`(가격/입찰 상태) + `SaleBankAccount`(정산 계좌 스냅샷, 생성 시 `user` 도메인의 BankAccount에서 복사).
- 판매자 발송(SellerShipment) 등록 진입점(`POST /sales/{saleId}/shipment`)도 이 도메인에 있으나 실제 생성은 `shipment` 도메인의 `SellerShipmentCommandService`가 수행.
- 판매 상태 `SOLD` 전환은 이 도메인이 아니라 `payment`(결제 시)와 `shipment`(구매자 운송장 등록/배송완료 시)에서 수행됨.

## 2. 패키지/파일 맵
| 레이어 | 경로 (repo root 기준) | 용도 |
|---|---|---|
| controller | `src/main/java/com/fream/back/domain/sale/controller/command/SaleBidCommandController.java` | 판매 입찰 생성/삭제, 즉시 판매 |
| controller | `src/main/java/com/fream/back/domain/sale/controller/command/SaleCommandController.java` | 판매자 발송 정보 등록 |
| controller | `src/main/java/com/fream/back/domain/sale/controller/query/SaleBidQueryController.java` | 내 판매 입찰 목록/카운트/상세 |
| service | `src/main/java/com/fream/back/domain/sale/service/command/SaleBidCommandService.java` | SaleBid 생성/즉시판매/삭제 |
| service | `src/main/java/com/fream/back/domain/sale/service/command/SaleCommandService.java` | Sale 생성, 즉시판매 Sale 생성, 상태 전환, SellerShipment 위임 |
| service | `src/main/java/com/fream/back/domain/sale/service/command/SaleBankAccountCommandService.java` | SaleBankAccount 저장 |
| service | `src/main/java/com/fream/back/domain/sale/service/query/SaleBidQueryService.java` | SaleBid 조회 (타 도메인에서도 사용) |
| entity | `src/main/java/com/fream/back/domain/sale/entity/Sale.java` | 판매 엔티티 |
| entity | `src/main/java/com/fream/back/domain/sale/entity/SaleBid.java` | 판매 입찰 엔티티 |
| entity | `src/main/java/com/fream/back/domain/sale/entity/SaleBankAccount.java` | 판매 정산 계좌 |
| entity | `src/main/java/com/fream/back/domain/sale/entity/SaleStatus.java` | 판매 상태 enum + `canTransitionTo` |
| entity | `src/main/java/com/fream/back/domain/sale/entity/BidStatus.java` | 판매 입찰 상태 enum (order 도메인에 동명 enum 별도 존재) |
| repository | `src/main/java/com/fream/back/domain/sale/repository/SaleRepository.java` | `findBySeller_EmailAndStatus` |
| repository | `src/main/java/com/fream/back/domain/sale/repository/SaleBidRepository.java` | `findByOrder_Id`, `findBySale_Id` + Custom |
| repository | `src/main/java/com/fream/back/domain/sale/repository/SaleBidRepositoryCustom.java` / `SaleBidRepositoryImpl.java` | QueryDSL 목록/카운트/상세 |
| repository | `src/main/java/com/fream/back/domain/sale/repository/SaleBankAccountRepository.java` | 기본 JPA |
| dto | `src/main/java/com/fream/back/domain/sale/dto/SaleBidRequestDto.java` | 판매 입찰 요청 (productSizeId, bidPrice, returnAddress, postalCode, receiverPhone, warehouseStorage) |
| dto | `src/main/java/com/fream/back/domain/sale/dto/InstantSaleRequestDto.java` | 즉시 판매 요청 (orderBidId, 반송 정보) |
| dto | `src/main/java/com/fream/back/domain/sale/dto/ShipmentRequestDto.java` | 판매자 발송 (courier, trackingNumber) |
| dto | `src/main/java/com/fream/back/domain/sale/dto/SaleBidResponseDto.java` | 목록/상세 응답 (상태 한글 변환 헬퍼 포함) |
| dto | `src/main/java/com/fream/back/domain/sale/dto/SaleBidStatusCountDto.java` | 상태별 개수 |
| exception | `src/main/java/com/fream/back/domain/sale/exception/SaleErrorCode.java`, `SaleException.java` | 에러코드 (현재 미사용, 아래 참조) |

## 3. REST 엔드포인트
| HTTP | 경로 | 컨트롤러 메서드 | 서비스 메서드 | 응답 |
|---|---|---|---|---|
| POST | `/sale-bids` | `SaleBidCommandController.createSaleBid` | `SaleBidCommandService.createSaleBid` | `Long` saleBidId |
| DELETE | `/sale-bids/{saleBidId}` | `SaleBidCommandController.deleteSaleBid` | `SaleBidCommandService.deleteSaleBid` | 200 |
| POST | `/sale-bids/instant` | `SaleBidCommandController.createInstantSale` | `SaleBidCommandService.createInstantSaleBid` → `SaleCommandService.createInstantSale` | `Long` **saleId** (saleBidId 아님) |
| POST | `/sales/{saleId}/shipment` | `SaleCommandController.createSellerShipment` | `SaleCommandService.createSellerShipment` → `SellerShipmentCommandService.createSellerShipment` | 200 |
| GET | `/sale-bids?saleBidStatus=&saleStatus=&page..` | `SaleBidQueryController.getSaleBids` | `SaleBidQueryService.getSaleBids` → `SaleBidRepositoryImpl.findSaleBidsByFilters` | `ResponseDto<Page<SaleBidResponseDto>>` |
| GET | `/sale-bids/count` | `SaleBidQueryController.getSaleBidStatusCounts` | `SaleBidQueryService.getSaleBidStatusCounts` | `ResponseDto<SaleBidStatusCountDto>` |
| GET | `/sale-bids/{saleBidId}` | `SaleBidQueryController.getSaleBidDetail` | `SaleBidQueryService.getSaleBidDetail` | `ResponseDto<SaleBidResponseDto>` |

- 사용자 식별: `SecurityUtils.extractEmailFromSecurityContext()` (delete, `/sales/{id}/shipment`는 이메일/소유자 검증 없음).
- `SecurityConfig`는 화이트리스트 외 `anyRequest().authenticated()`; 이 도메인 경로는 화이트리스트 grep 결과 없음.

## 4. 핵심 엔티티와 관계
| 엔티티 | 주요 필드 | 연관관계 |
|---|---|---|
| `Sale` (extends BaseTimeEntity) | returnAddress, postalCode, receiverPhone, `isWarehouseStorage`, `status: SaleStatus` | `seller` ManyToOne User; `productSize` ManyToOne ProductSize; `payment` OneToOne(mappedBy=sale, cascade ALL) `payment.Payment`; `saleBankAccount` OneToOne(mappedBy); `saleBid` OneToOne(mappedBy); `sellerShipment` OneToOne(mappedBy) `shipment.SellerShipment` |
| `SaleBid` | `bidPrice`(int), `status: BidStatus`(기본 PENDING), `isInstantSale`(기본 false) | `seller` ManyToOne User; `productSize` ManyToOne; `sale` OneToOne(FK sale_id); `order` OneToOne(FK order_id) `order.Order` |
| `SaleBankAccount` | bankName, accountNumber, accountHolder | `sale` OneToOne(FK sale_id, not null) |

외부에서 Sale을 참조: `order.entity.OrderBid.sale`(FK sale_id), `payment.entity.Payment.sale`, `shipment.entity.SellerShipment.sale`, `warehouseStorage.entity.WarehouseStorage.sale`.

### SaleStatus (`SaleStatus.canTransitionTo`)
| 현재 | 허용 다음 상태 |
|---|---|
| PENDING_SHIPMENT | IN_TRANSIT |
| IN_TRANSIT | IN_INSPECTION |
| IN_INSPECTION | FAILED_INSPECTION, IN_STORAGE |
| IN_STORAGE | ON_AUCTION, AUCTION_EXPIRED, SELLING |
| ON_AUCTION | SOLD, AUCTION_EXPIRED |
| SELLING | SOLD, AUCTION_EXPIRED, ON_AUCTION |
| SOLD / AUCTION_EXPIRED / FAILED_INSPECTION | 없음 |

`Sale.updateStatus()`는 규칙을 검사하지 않음. 규칙 검사는 `SaleCommandService.updateSaleStatus`와 `OrderShipmentCommandService.updateSaleStatusToSold`만 수행.

### sale `BidStatus`
`PENDING, MATCHED, CANCELLED, COMPLETED`. `SaleBid.assignSale()`/`assignOrder()`는 **호출 즉시 status=MATCHED** 로 바꿈.

## 5. 주요 서비스 메서드와 비즈니스 규칙
| 메서드 | 동작 | 예외 |
|---|---|---|
| `SaleBidCommandService.createSaleBid(email, productSizeId, bidPrice, returnAddress, postalCode, receiverPhone, isWarehouseStorage)` @Transactional | User 조회 → ProductSize 조회 → `SaleCommandService.createSale` → SaleBid(PENDING) save → `sale.assignSaleBid(saleBid)` (→ SaleBid가 **MATCHED** 로 바뀜) | ProductSize 없음: `IllegalArgumentException` |
| `SaleCommandService.createSale(...)` | `BankAccountQueryService.getBankAccount(email)` → Sale(PENDING_SHIPMENT, isWarehouseStorage) save → SaleBankAccount 생성/연결 → 판매자에게 알림 "판매 입찰이 등록되었습니다." | 계좌 없으면 user 도메인의 `BankAccountNotFoundException` |
| `SaleBidCommandService.createInstantSaleBid(orderBidId, email, ...)` | `SaleCommandService.createInstantSale` 먼저 호출 → OrderBid 재조회 → SaleBid(MATCHED, bidPrice=orderBid.bidPrice, order=orderBid.order) 생성/저장 → saleId 반환 | OrderBid 없음: `IllegalArgumentException` |
| `SaleCommandService.createInstantSale(...)` | OrderBid 조회 → Sale(PENDING_SHIPMENT, productSize=orderBid.productSize, isWarehouseStorage 미설정=false) save → SaleBankAccount → `orderBid.assignSale(sale)` + `orderBid.updateStatus(order.BidStatus.MATCHED)` → 구매자에게 알림 "구매 입찰에 판매자가 등록되었습니다." | OrderBid 없음: `IllegalArgumentException`. OrderBid 상태/중복 매칭 검증 없음 |
| `SaleBidCommandService.deleteSaleBid(id)` | order 연결 시 거부, sale 있으면 `SaleCommandService.deleteSale` 후 SaleBid 삭제 | `IllegalArgumentException`(없음), `IllegalStateException`(Order 연결) |
| `SaleCommandService.createSellerShipment(saleId, courier, tracking)` | `SellerShipmentCommandService.createSellerShipment` → `updateSaleStatus(saleId, IN_TRANSIT)` | 전환 불가 시 `IllegalStateException` |
| `SaleCommandService.updateSaleStatus(saleId, status)` | `canTransitionTo` 검사 후 변경 (System.out 디버그 출력 있음) | `IllegalArgumentException`, `IllegalStateException` |
| `SaleBidQueryService.findByOrderId(orderId)` | 없으면 **예외** (`IllegalArgumentException`) | — |
| `SaleBidQueryService.findBySaleId(saleId)` | 없으면 `null` | — |
| `SaleBidRepositoryImpl.findSaleBidsByFilters` | seller.email 조인 조건, 상태는 `stringValue().eq(문자열)`, Product/Color/thumbnailImage inner join, Sale/SellerShipment left join, createdDate desc | — |
| `SaleBidRepositoryImpl.countSaleBidsByStatus` | pending / matched / (cancelled+completed) 3개 카운트 | — |
| `SaleBidRepositoryImpl.findSaleBidById(id, email)` | 소유자 아니면 null → 컨트롤러가 `IllegalArgumentException` | — |

## 6. 다른 도메인과의 의존
| 방향 | 대상 | 내용 |
|---|---|---|
| sale → user | `UserQueryService.findByEmail`, `BankAccountQueryService.getBankAccount` | 판매자, 정산 계좌 |
| sale → product | `ProductSizeQueryService.findById` | |
| sale → order | `OrderBidQueryService.findById`, `OrderBid.assignSale/updateStatus` | 즉시 판매 |
| sale → notification | `NotificationCommandService.createNotification(…, SHOPPING, BID, msg)` | |
| sale → shipment | `SellerShipmentCommandService.createSellerShipment` | |
| order → sale | `OrderBidCommandService.createInstantOrderBid`(즉시 구매: `SaleBidQueryService.findById`), `OrderCommandService.createInstantOrder`(`saleBid.assignOrder`, MATCHED, 창고상태 ASSOCIATED_WITH_ORDER) | |
| payment → sale | `PaymentCommandService.notifyUsersAndUpdateSaleStatus` → `sale.updateStatus(SOLD)` (전환 규칙 미검사) | |
| shipment → sale | `OrderShipmentCommandService.updateSaleStatusToSold`, `updateOrderAndSaleBidStatusToCompleted`(SaleBid MATCHED→COMPLETED); `SellerShipmentCommandService`(Sale→IN_STORAGE) | |
| warehouseStorage → sale | `WarehouseStorage.sale`, `findBySale` | |
| global | `DataInitializer`(시드: Sale PENDING_SHIPMENT 등), `GlobalExceptionHandler`(`SaleException` 핸들러) | |

Kafka / Redis / 스케줄러: 이 도메인 내 사용 없음 (grep 확인).

## 7. 예외/에러코드
- `SaleErrorCode` (S001~S501), `SaleException extends GlobalException` — **어디서도 throw 되지 않음**(grep 결과 0건). 실제로는 `IllegalArgumentException`/`IllegalStateException`을 던지며 `GlobalExceptionHandler.handleException`(Exception.class)에서 **500** 으로 응답됨.
- 코드 접두사 `S001`, `S002` 등이 `ShipmentErrorCode`와 **중복**.

## 8. 관련 테스트
| 파일 | 종류 | 내용 |
|---|---|---|
| `src/test/java/com/fream/back/domain/sale/entity/SaleEntityTest.java` | 단위 | SaleStatus 전환 매트릭스, SaleBid 기본값, assignSale/assignOrder→MATCHED, `updateStatus`가 규칙 미검사 |
| `src/test/java/com/fream/back/domain/sale/service/SaleBidCommandServiceTest.java` | Mockito | createSaleBid가 MATCHED로 끝남, 즉시판매 가격/순서, 삭제 규칙 |
| `src/test/java/com/fream/back/domain/sale/service/SaleCommandServiceTest.java` | Mockito | createSale/createInstantSale/updateSaleStatus/createSellerShipment/deleteSale |
| `src/test/java/com/fream/back/domain/sale/repository/SaleBidRepositoryTest.java` | `@DataJpaTest` | QueryDSL 필터/프로젝션/페이징/카운트/소유자 검사 |
| `src/test/java/com/fream/back/domain/sale/dto/SaleBidDtoTest.java` | 단위 | DTO |

## 9. 자주 할 작업 → 여기를 보라
| 작업 | 위치 |
|---|---|
| 판매 상태 전환 규칙 변경 | `SaleStatus.canTransitionTo` + `SaleEntityTest.saleStatusTransitionMatrix` |
| 판매 입찰 생성 로직/검증 추가 | `SaleBidCommandService.createSaleBid`, `SaleCommandService.createSale` |
| 즉시 판매 검증(OrderBid 상태 등) 추가 | `SaleCommandService.createInstantSale` |
| 내 판매 목록 필드/필터 추가 | `SaleBidRepositoryImpl` + `SaleBidResponseDto` 생성자 순서(Projections.constructor) |
| SOLD 처리 위치 | `PaymentCommandService.notifyUsersAndUpdateSaleStatus`, `OrderShipmentCommandService.updateSaleStatusToSold` |
| 에러코드 도입 | `SaleErrorCode` (현재 미사용) |

## 10. Gotchas (코드로 확인)
1. **일반 판매 입찰이 생성 직후 MATCHED**: `createSaleBid`에서 `sale.assignSaleBid()` → `SaleBid.assignSale()`가 status=MATCHED로 설정 (테스트 `createSaleBidCreatesSaleAndBidAndEndsMatched`가 이 동작을 고정). 따라서 `/sale-bids/count`의 pending은 일반 경로로는 증가하지 않음.
2. **DTO 검증 미적용**: 이 도메인 컨트롤러는 `@Valid` 없이 `@RequestBody` 사용 → `SaleBidRequestDto`/`InstantSaleRequestDto`/`ShipmentRequestDto`의 `@NotBlank/@Pattern/@Min` 무시됨.
3. `SaleBidRequestDto.warehouseStorage = false`는 `@Builder.Default` 없이 선언 (빌더 사용 시 초기값 무시 가능).
4. 즉시 판매(`createInstantSaleBid`)는 `isInstantSale` 플래그를 설정하지 않음(`markAsInstantSale()` 호출처 0건), `Sale.isWarehouseStorage`도 항상 false.
5. 즉시 판매 응답은 saleId, 판매 입찰 응답은 saleBidId.
6. `deleteSaleBid`는 소유자 확인 없음. 이미 MATCHED라도 order가 null이면 삭제됨.
7. sale `BidStatus`와 `order.entity.BidStatus`가 동명 enum — `SaleCommandService`는 order 쪽을 import, `SaleBidCommandService`는 sale 쪽을 import.
8. 창고보관 판매에 `POST /sales/{saleId}/shipment` 호출 시: `SellerShipmentCommandService`가 Sale을 `IN_STORAGE`로 바꾼 뒤 `updateSaleStatus(IN_TRANSIT)`가 `IN_STORAGE→IN_TRANSIT` 불가로 `IllegalStateException` → 트랜잭션 롤백(500). 결제로 이미 SOLD가 된 Sale도 동일하게 실패. (`/shipments/seller`는 IN_TRANSIT 전환을 하지 않아 통과)
9. `IN_INSPECTION`, `FAILED_INSPECTION`, `ON_AUCTION`, `SELLING`, `AUCTION_EXPIRED`로 설정하는 코드 없음 (enum/표시용 문자열만 존재).
