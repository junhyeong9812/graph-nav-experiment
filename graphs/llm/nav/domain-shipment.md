# 도메인: shipment (배송: 판매자→창고 / 창고→구매자, 배송 추적 배치)

> 기준 패키지: `com.fream.back.domain.shipment` — 경로 `src/main/java/com/fream/back/domain/shipment/`
> 관련 global: `global/config/ShipmentBatchScheduler.java`, `global/config/BatchInfraConfig.java`, `global/utils/CjTrackingPlaywright.java`, `global/utils/PlaywrightBrowserManager.java`

## 1. 역할 요약
- `SellerShipment`: 판매자가 상품을 창고(검수센터)로 보내는 발송 정보 (Sale 1:1).
- `OrderShipment`: 구매자에게 보내는 배송 정보 (Order 1:1). 주문 결제 시 order 도메인이 PENDING으로 생성.
- 운송장 등록 시 Order→IN_TRANSIT, Sale→SOLD, 창고→SOLD, 입찰→COMPLETED 등 거래 후속 상태를 일괄 처리.
- CJ대한통운 추적 페이지를 **Playwright(headless Chromium)** 로 렌더링 + **Jsoup**으로 파싱해 배송 상태를 갱신 (단건 API + 6시간 주기 Spring Batch).

## 2. 패키지/파일 맵
| 레이어 | 경로 | 용도 |
|---|---|---|
| controller | `src/main/java/com/fream/back/domain/shipment/controller/command/OrderShipmentCommandController.java` | 구매자 배송 운송장 등록, 단건 상태 즉시 조회 |
| controller | `src/main/java/com/fream/back/domain/shipment/controller/command/SellerShipmentCommandController.java` | 판매자 발송 생성/수정 |
| service | `src/main/java/com/fream/back/domain/shipment/service/command/OrderShipmentCommandService.java` | OrderShipment 생성, 운송장 등록, 주문 완료, 판매 SOLD, 상태 매핑 |
| service | `src/main/java/com/fream/back/domain/shipment/service/command/SellerShipmentCommandService.java` | SellerShipment 생성/수정, 창고보관 생성 |
| batch | `src/main/java/com/fream/back/domain/shipment/config/UpdateShipmentStatusesJobConfig.java` | Job/Step/Reader/Writer/SkipListener, `PlaywrightBrowserManager` @Bean |
| batch | `src/main/java/com/fream/back/domain/shipment/config/ShipmentItemProcessor.java` | Step에서 실제 사용되는 Processor (공유 브라우저로 CJ 조회) |
| batch | `src/main/java/com/fream/back/domain/shipment/config/BrowserManageStepListener.java` | Step 전/후 브라우저 open/close |
| entity | `src/main/java/com/fream/back/domain/shipment/entity/OrderShipment.java` | 구매자 배송 |
| entity | `src/main/java/com/fream/back/domain/shipment/entity/SellerShipment.java` | 판매자 발송 |
| entity | `src/main/java/com/fream/back/domain/shipment/entity/ShipmentStatus.java` | 상태 enum (상수별 `canTransitionTo`) |
| repository | `src/main/java/com/fream/back/domain/shipment/repository/OrderShipmentRepository.java` | `findByStatusIn(List)` |
| repository | `src/main/java/com/fream/back/domain/shipment/repository/SellerShipmentRepository.java` | 기본 JPA |
| dto | `src/main/java/com/fream/back/domain/shipment/dto/OrderShipmentRequestDto.java` | courier, trackingNumber (검증 어노테이션 없음) |
| dto | `src/main/java/com/fream/back/domain/shipment/dto/SellerShipmentRequestDto.java` | saleId, courier, trackingNumber |
| dto | `src/main/java/com/fream/back/domain/shipment/dto/ShipmentStatusResponse.java` | `{status}` |
| dto | `ShipmentResponseDto.java`, `ShipmentUpdateRequestDto.java`, `TrackingNumberRequestDto.java` (같은 dto 패키지) | 컨트롤러에서 미사용 |
| exception | `src/main/java/com/fream/back/domain/shipment/exception/ShipmentErrorCode.java`, `ShipmentException.java` | 에러코드 |
| global | `src/main/java/com/fream/back/global/config/ShipmentBatchScheduler.java` | `@Scheduled(cron = "0 0 */6 * * *")` 잡 실행 |
| global | `src/main/java/com/fream/back/global/config/BatchInfraConfig.java` | `@EnableBatchProcessing`, JobRepository(MYSQL 타입) |
| global | `src/main/java/com/fream/back/global/utils/CjTrackingPlaywright.java` | 호출마다 Playwright 생성/종료하는 단건 조회 (`@Configuration` 빈) |
| global | `src/main/java/com/fream/back/global/utils/PlaywrightBrowserManager.java` | 브라우저 재사용 관리자 (`@Component`, synchronized open/close, `newPage`) |

## 3. REST 엔드포인트
| HTTP | 경로 | 컨트롤러 메서드 | 서비스 메서드 | 응답 |
|---|---|---|---|---|
| PATCH | `/shipments/order/{shipmentId}/status` | `OrderShipmentCommandController.updateTrackingInfo` | `OrderShipmentCommandService.updateTrackingInfo` | 200 (예외는 GlobalExceptionHandler) |
| POST | `/shipments/order/{shipmentId}/check-status` | `OrderShipmentCommandController.updateAndCheckStatus` | `OrderShipmentCommandService.updateAndCheckShipmentStatus` | `ShipmentStatusResponse` / 실패 시 body 없는 에러 상태 |
| POST | `/shipments/order/{shipmentId}/check-status-string` | `OrderShipmentCommandController.updateAndCheckStatusAsString` | 동일 | 상태 문자열 / `"Error: ..."` |
| POST | `/shipments/seller` | `SellerShipmentCommandController.createSellerShipment` | `SellerShipmentCommandService.createSellerShipment` | `Long` shipmentId / 실패 시 body 없음 |
| PATCH | `/shipments/seller/{shipmentId}` | `SellerShipmentCommandController.updateShipment` | `SellerShipmentCommandService.updateShipment` | 200 |
| (sale 도메인) POST | `/sales/{saleId}/shipment` | `SaleCommandController.createSellerShipment` | `SaleCommandService.createSellerShipment` → `SellerShipmentCommandService.createSellerShipment` + Sale IN_TRANSIT | 200 |

- 컨트롤러 2개는 try/catch로 `ShipmentException`→`errorCode.status`, 기타→400 으로 직접 응답 (PATCH `/status` 제외). 사용자/소유자 검증 없음.

## 4. 핵심 엔티티와 관계
| 엔티티 | 필드 | 연관 |
|---|---|---|
| `OrderShipment` (BaseTimeEntity) | receiverName, receiverPhone, postalCode, address, courier, trackingNumber, `status` | `order` OneToOne(FK order_id, not null) `order.Order` (역방향 `Order.orderShipment`) |
| `SellerShipment` (BaseTimeEntity) | courier, trackingNumber, `status` | `sale` OneToOne(FK sale_id, not null) (역방향 `Sale.sellerShipment`) |

메서드: `updateTrackingInfo(courier, tracking)` → 필드 세팅 + **status=IN_TRANSIT 강제(전환 규칙 미검사)**; `updateStatus(new)` → `status==null || canTransitionTo` 아니면 `IllegalStateException`.

### ShipmentStatus 전환표
| 현재 | 허용 |
|---|---|
| PENDING | SHIPPED, CANCELED |
| SHIPPED | IN_TRANSIT, RETURNED, CANCELED |
| IN_TRANSIT | OUT_FOR_DELIVERY, DELAYED, CANCELED, DELIVERED |
| OUT_FOR_DELIVERY | DELIVERED, FAILED_DELIVERY, CANCELED |
| DELIVERED | 없음 |
| RETURNED | CANCELED |
| CANCELED | 없음 |
| DELAYED | IN_TRANSIT, CANCELED |
| FAILED_DELIVERY | RETURNED, OUT_FOR_DELIVERY, CANCELED |

자기 자신으로의 전환(예: IN_TRANSIT→IN_TRANSIT)은 **불허**.

### CJ 상태 텍스트 매핑 (`mapToShipmentStatus`, Service/Processor/JobConfig에 동일 사본 3개)
| CJ 텍스트 (tbody#statusDetail 마지막 tr의 5번째 td) | ShipmentStatus |
|---|---|
| `배송완료` | DELIVERED |
| `배송출발` | OUT_FOR_DELIVERY |
| 그 외 전부 | IN_TRANSIT |

## 5. 주요 서비스 메서드와 비즈니스 규칙
### OrderShipmentCommandService
| 메서드 | 동작 |
|---|---|
| `createOrderShipment(order, name, phone, postal, addr)` | status=PENDING 저장. 호출처: `OrderCommandService.processPaymentAndShipment`, `OrderCommandService.createInstantOrder`, `OrderEventConsumer.processCompleteOrder`(Kafka) |
| `updateTrackingInfo(shipmentId, courier, tracking)` | courier/tracking 공백이면 `TRACKING_INFO_REQUIRED` → `shipment.updateTrackingInfo`(IN_TRANSIT) → `order.updateStatus(OrderStatus.IN_TRANSIT)` → 구매자 알림 "상품이 출발하였습니다" → `updateSaleStatusToSold(orderId)` → SaleBid의 Sale이 창고보관이면 `warehouseStorageCommandService.updateWarehouseStatusToSold` → `updateOrderAndSaleBidStatusToCompleted`. 기타 예외는 `SHIPMENT_NOT_FOUND`로 래핑 |
| `updateSaleStatusToSold(orderId)` | ① `saleBidQueryService.findByOrderId` (없으면 IllegalArgumentException — try 밖) → Sale이 `canTransitionTo(SOLD)`이면 SOLD, 창고보관이면 창고 SOLD, 판매자 알림. ② OrderBid 기반으로 같은 처리(없으면 warn 후 진행) |
| `completeOrder(orderId)` | OrderBid 조회 → `order.updateStatus(COMPLETED)` → `updateSaleStatusToSold` → 구매자 알림. 모든 예외를 `ORDER_NOT_FOUND_FOR_SHIPMENT`로 래핑 |
| `updateAndCheckShipmentStatus(id, courier, tracking)` | 운송장 갱신(IN_TRANSIT) → `CjTrackingPlaywright.getCurrentTrackingStatus` → DELIVERED면 `updateStatus(DELIVERED)`+`completeOrder`, 배송출발이면 OUT_FOR_DELIVERY, 그 외 `updateStatus(IN_TRANSIT)`. 기타 예외는 `EXTERNAL_TRACKING_SERVICE_ERROR` |
| `updateShipmentStatuses()` | IN_TRANSIT/OUT_FOR_DELIVERY 전체 조회 후 CJ 조회·갱신, DELIVERED면 Order COMPLETED+알림. **호출처 없음**(배치가 대체) |
| `updateShipmentStatus(shipment, status)` | 전환 실패 시 `INVALID_SHIPMENT_STATUS_TRANSITION` |
| `updateOrderAndSaleBidStatusToCompleted(orderId)` (private @Transactional) | OrderBid MATCHED→COMPLETED, SaleBid MATCHED→COMPLETED. 예외는 로그만 |

### SellerShipmentCommandService
| 메서드 | 동작 |
|---|---|
| `createSellerShipment(saleId, courier, tracking)` | 공백 검사(`TRACKING_INFO_REQUIRED`) → Sale 조회(`SALE_NOT_FOUND_FOR_SHIPMENT`) → SellerShipment(IN_TRANSIT) 저장, `sale.assignSellerShipment` → `sale.isWarehouseStorage()`면 `WarehouseStorageCommandService.createSellerStorage(sale, seller)` + `sale.updateStatus(IN_STORAGE)` (전환 규칙 미검사) |
| `updateShipment(shipmentId, courier, tracking)` | `updateTrackingInfo`(IN_TRANSIT). 없으면 `SELLER_SHIPMENT_NOT_FOUND` |

## 6. 배치: 배송 상태 자동 갱신
| 항목 | 내용 |
|---|---|
| 스케줄 | `ShipmentBatchScheduler.scheduleShipmentStatusJob` — `@Scheduled(cron = "0 0 */6 * * *")` (매 6시간 정각, 0/6/12/18시). JobParameter `timestamp=System.currentTimeMillis()`. 예외는 `printStackTrace`만 |
| 스케줄링 활성화 | `@EnableScheduling`은 다른 클래스(`EventStatusScheduler`, `AccessLogAopConfiguration`, `WeatherDataScheduler`)에 선언되어 전역 적용 |
| 배치 인프라 | `BatchInfraConfig`: `@EnableBatchProcessing`, `JobRepositoryFactoryBean`(DataSource, `DatabaseType.MYSQL`). 스키마: `application-prod.yml` `spring.batch.jdbc.initialize-schema: never`, `application-local.yml.example`는 `always` |
| Job | `updateShipmentStatusesJob` → Step `updateShipmentStatusesStep` |
| Step | chunk 50 (`CHUNK_SIZE`), `faultTolerant().skip(Exception.class).skipLimit(50)`, listeners: `shipmentSkipListener`(로그), `BrowserManageStepListener` |
| Reader | `shipmentItemReader` (`@StepScope` `JpaPagingItemReader`, pageSize 50): `SELECT s FROM OrderShipment s WHERE s.status IN ('IN_TRANSIT','OUT_FOR_DELIVERY') AND s.trackingNumber IS NOT NULL` |
| Processor | **`new ShipmentItemProcessor(playwrightBrowserManager(), notificationService, orderService)`** — 같은 클래스의 `shipmentItemProcessor()` @Bean(CjTrackingPlaywright 사용 람다)은 Step에 연결되지 않음 |
| Writer | `shipmentJpaItemWriter` (`JpaItemWriter`, merge) |

### ShipmentItemProcessor.process 흐름
1. trackingNumber 공백 → `TRACKING_NUMBER_INVALID`.
2. `browserManager.newPage()` (미초기화 시 자동 `openBrowser`) → timeout 30s → `https://trace.cjlogistics.com/next/tracking.html?wblNo={tracking}` navigate → `waitForSelector("tbody#statusDetail tr")` → `page.content()`.
3. Jsoup 파싱: 행 없음/텍스트 공백/인덱스 오류 → `TRACKING_HTML_PARSE_ERROR`.
4. 매핑 상태가 현재와 같으면 그대로 반환.
5. DELIVERED → `orderShipment.updateStatus(DELIVERED)` → `OrderShipmentCommandService.completeOrder(orderId)` → `NotificationCommandService.notifyShipmentCompleted(order)`.
   OUT_FOR_DELIVERY → updateStatus. 그 외 → `updateStatus(IN_TRANSIT)`.
6. 비-ShipmentException은 `EXTERNAL_TRACKING_SERVICE_ERROR`로 래핑 → skip 처리. finally에서 page.close.

### Playwright 구성
| 클래스 | 수명 | 옵션 |
|---|---|---|
| `PlaywrightBrowserManager` | Step 동안 브라우저 1개 재사용 (`BrowserManageStepListener.beforeStep→openBrowser`, `afterStep→closeBrowser`) | headless=true, slowMo=100 |
| `CjTrackingPlaywright.getCurrentTrackingStatus` | 호출마다 Playwright/Browser 생성 후 finally에서 종료 | headless=true, slowMo=100, selector timeout 30s |

`PlaywrightBrowserManager`는 `@Component`이면서 `UpdateShipmentStatusesJobConfig.playwrightBrowserManager()` @Bean으로도 같은 이름으로 정의됨.

## 7. 다른 도메인과의 의존
| 방향 | 내용 |
|---|---|
| shipment → order | `Order.updateStatus(IN_TRANSIT/COMPLETED)`, `OrderBidQueryService.findByOrderId`, `OrderBid.updateStatus(COMPLETED)` |
| shipment → sale | `SaleRepository`, `SaleBidQueryService.findByOrderId`, `Sale.updateStatus(SOLD/IN_STORAGE)`, `SaleBid.updateStatus(COMPLETED)` |
| shipment → warehouseStorage | `createSellerStorage`, `updateWarehouseStatusToSold` |
| shipment → notification | `createNotification(…, SHOPPING, BID, …)`, `notifyShipmentCompleted` |
| order → shipment | `OrderCommandService`(createOrderShipment), `OrderEventConsumer`(Kafka `order-processing-topic` 처리 중 createOrderShipment), `Order.orderShipment`, `OrderBidRepositoryImpl`(QOrderShipment 조회) |
| sale → shipment | `SaleCommandService.createSellerShipment`, `SaleBidRepositoryImpl`(sellerShipment.status 프로젝션) |
| global | `DataInitializer`(SellerShipment IN_TRANSIT 시드), `GlobalExceptionHandler`(`ShipmentException`) |

Redis: 사용 없음. Kafka: 이 도메인에서 직접 사용 없음(order Kafka consumer가 이 도메인 서비스를 호출).

## 8. 예외/에러코드
`ShipmentErrorCode` (`src/main/java/com/fream/back/domain/shipment/exception/ShipmentErrorCode.java`):
S001 SHIPMENT_NOT_FOUND(404), S002 INVALID_SHIPMENT_STATUS_TRANSITION(400), S003 TRACKING_NUMBER_INVALID(400), S004 TRACKING_INFO_REQUIRED(400), S005 TRACKING_SERVICE_UNAVAILABLE(503), S101 ORDER_SHIPMENT_NOT_FOUND(404), S102 ORDER_NOT_FOUND_FOR_SHIPMENT(404), S103 ORDER_ALREADY_COMPLETED(400), S201 SELLER_SHIPMENT_NOT_FOUND(404), S202 SALE_NOT_FOUND_FOR_SHIPMENT(404), S203 SALE_ALREADY_COMPLETED(400), S301 TRACKING_HTML_PARSE_ERROR(500), S302 EXTERNAL_TRACKING_SERVICE_ERROR(503), S303 BROWSER_INITIALIZATION_ERROR(500).
코드 문자열이 `SaleErrorCode`(S001…)와 겹침.

## 9. 관련 테스트
- `src/test/java/com/fream/back/domain/shipment/**` **없음**. 배치/Playwright 테스트 없음.
- 간접: `src/test/java/com/fream/back/domain/sale/service/SaleCommandServiceTest.java`(createSellerShipment 위임), `src/test/java/com/fream/back/domain/order/OrderCommandServiceTest.java`, `src/test/java/com/fream/back/domain/notification/service/NotificationCommandServiceTest.java`에서 shipment 참조(grep).

## 10. 자주 할 작업 → 여기를 보라
| 작업 | 위치 |
|---|---|
| 배치 주기 변경 | `ShipmentBatchScheduler` cron |
| CJ 페이지 구조 변경 대응 | `ShipmentItemProcessor`(배치), `CjTrackingPlaywright`(단건 API) — URL/selector/`td` 인덱스 4. `UpdateShipmentStatusesJobConfig.parseCjLogisticsStatus`는 미사용 |
| 상태 텍스트 매핑 추가 | `mapToShipmentStatus` 3곳 모두 수정 (Service, Processor, JobConfig) |
| 배송완료 후 후속 처리 | `OrderShipmentCommandService.completeOrder` / `updateSaleStatusToSold` |
| 운송장 등록 시 후속 처리 | `OrderShipmentCommandService.updateTrackingInfo` |
| 판매자 발송 시 창고 처리 | `SellerShipmentCommandService.createSellerShipment` |
| 전환 규칙 | `ShipmentStatus` |

## 11. Gotchas (코드로 확인)
1. `updateAndCheckShipmentStatus`: `updateTrackingInfo`가 먼저 status=IN_TRANSIT로 만들기 때문에, CJ 결과가 "그 외"(IN_TRANSIT)면 `updateStatus(IN_TRANSIT)`가 IN_TRANSIT→IN_TRANSIT 불허로 `IllegalStateException` → `EXTERNAL_TRACKING_SERVICE_ERROR`(503).
2. 배치 Processor: 현재 OUT_FOR_DELIVERY인데 CJ 결과가 IN_TRANSIT으로 매핑되면 전환 불가 예외 → skip.
3. `updateTrackingInfo`의 `order.updateStatus(OrderStatus.IN_TRANSIT)`는 `OrderStatus` 규칙상 `SHIPMENT_STARTED`에서만 허용되는데 `SHIPMENT_STARTED`로 설정하는 코드는 없음(grep: `OrderQueryController` 표시용만). PREPARING 등에서 호출하면 `InvalidOrderStatusException` → `SHIPMENT_NOT_FOUND`로 래핑됨.
4. `updateTrackingInfo`는 `updateSaleStatusToSold` 내부와 그 직후에 **두 번** `updateWarehouseStatusToSold`를 호출할 수 있음. 첫 호출에서 SOLD가 되면 두 번째 호출이 "이미 판매 완료" `InvalidStatusTransitionException`을 던짐 (Sale이 SOLD로 전환 가능한 상태 ON_AUCTION/SELLING일 때만 첫 호출 발생).
5. `saleBidQueryService.findByOrderId`는 null이 아니라 예외를 던짐 → SaleBid가 없는 주문은 `updateSaleStatusToSold`/`completeOrder`/`updateTrackingInfo` 전체 실패. 코드의 `if (saleBid != null)` 분기는 사실상 항상 true.
6. 배송완료(배치·단건) 시 Order는 `completeOrder`에서 `IN_TRANSIT→COMPLETED` 전환.
7. `updateOrderAndSaleBidStatusToCompleted`는 private + `@Transactional`(프록시 미적용), 호출자 트랜잭션에서 실행.
8. 컨트롤러가 `@Validated`를 쓰지만 shipment DTO들에는 제약 어노테이션이 없음. 검증은 서비스의 공백 검사뿐.
9. `CjTrackingPlaywright`는 요청마다 Chromium을 띄움(느림, 동기 HTTP 요청 안에서 실행). 배치는 공유 브라우저 사용.
10. `SellerShipment`가 생성돼도 `/shipments/seller` 경로는 Sale을 IN_TRANSIT으로 바꾸지 않음(창고보관이면 IN_STORAGE로만 변경). `/sales/{saleId}/shipment`는 IN_TRANSIT 전환을 추가로 수행 (창고보관 Sale이면 실패 — `domain-sale.md` Gotcha 8).
11. SellerShipment 상태를 이후 갱신하는 코드(입고/검수 완료 등)는 없음 — 배치는 OrderShipment만 대상.
