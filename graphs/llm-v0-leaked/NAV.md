# NAV — fream-back 코드 지도 (진입 인덱스)

Spring Boot 3.4.1 / Java 17 / Gradle. 리셀 거래 플랫폼 백엔드.
루트 패키지 `src/main/java/com/fream/back/` (아래 `D/` = `.../back/domain/`, `G/` = `.../back/global/`).
JPA+QueryDSL(MySQL, 테스트는 H2), Redis(primary/replica), Kafka, Elasticsearch, Spring Batch, WebSocket(STOMP), Playwright, PortOne, OpenAI.

## 공통 구조
- 도메인마다 `controller|service/{command,query}`(CQRS식 분리), `dto`, `entity`, `repository`(+`*RepositoryCustom/Impl` QueryDSL), `exception`(`XxxErrorCode`+`XxxException`), 일부 `aop`.
- 예외: `ErrorCode` → 도메인 `XxxErrorCode` → `GlobalException` → `G/exception/GlobalExceptionHandler.java` → `ErrorResponse`.
- 현재 사용자 email: `G/utils/SecurityUtils.java`. 관리자 체크: `UserQueryService.checkAdminRole` (거의 모든 admin API).
- 파일 저장: `G/utils/FileUtils.java` (BASE_DIR `/home/ubuntu/fream` 하드코딩).
- prod nginx가 `/api/` prefix를 떼고 전달 → 컨트롤러 매핑에는 대부분 `/api` 없음.

## 도메인 (역할 · 주요 진입점 · 상세)
| 도메인 | 역할 | 진입점 (`D/` 기준) | 상세 |
|---|---|---|---|
| user + 보안 | 회원/프로필/팔로우/차단/계좌/포인트/등급/제재, JWT 쿠키 인증, OAuth2 | `user/service/command/UserCommandService`, `user/service/query/UserQueryService`, `G/config/security/SecurityConfig`, `JwtAuthenticationFilter` | nav/domain-user.md, nav/flow-auth-security.md |
| product | 카탈로그 Product→ProductColor→ProductSize, Brand/Category/Collection/Interest, ES 검색 | `product/controller/query/ProductQueryController`, `product/repository/ProductQueryDslRepository`, `product/service/productColor/ProductColorCommandService`, `product/elasticsearch/service/ProductColorSearchService` | nav/domain-product.md, nav/flow-product-search.md |
| order | 구매 입찰(OrderBid), Order/OrderItem, 즉시구매 | `order/service/command/OrderBidCommandService`, `OrderCommandService`, `order/service/kafka/OrderEventConsumer` | nav/domain-order.md |
| payment | Payment(SINGLE_TABLE: CARD/ACCOUNT/GENERAL), 저장카드 PaymentInfo, PortOne | `payment/service/command/PaymentCommandService`, `payment/portone/PortOneApiClient` | nav/domain-payment.md |
| sale | 판매 입찰(SaleBid), 즉시판매, Sale, SaleBankAccount | `sale/service/command/SaleBidCommandService`, `SaleCommandService` | nav/domain-sale.md |
| shipment | SellerShipment(판매자→창고), OrderShipment(→구매자), CJ 배송조회 배치 | `shipment/service/command/OrderShipmentCommandService`, `shipment/config/UpdateShipmentStatusesJobConfig`, `G/config/ShipmentBatchScheduler` | nav/domain-shipment.md |
| warehouseStorage | 창고 보관 기록(타 도메인이 생성), 기간 연장 | `warehouseStorage/service/command/WarehouseStorageCommandService` | nav/domain-warehouse-storage.md |
| style | 스타일 SNS 게시물/댓글/좋아요/관심/해시태그/미디어 | `style/service/command/StyleCommandService`, `MediaUrlCommandService`, `style/repository/StyleRepositoryCustomImpl` | nav/domain-style.md |
| notice/faq/inquiry/inspection/event | 공지, FAQ(캐시), 1:1 문의, 검수기준, 브랜드 이벤트 | 각 `controller/{command,query}`, `notice/service/command/NoticeCommandService`, `inquiry/service/command/InquiryCommandServiceImpl` | nav/domain-cs-content.md |
| chatQuestion | FAQ를 프롬프트에 넣은 GPT 챗봇, 사용량 로그 | `chatQuestion/service/ChatService`, `GPTService`, `G/config/GPTConfig` | nav/domain-chat-gpt.md |
| notification | 알림 REST + STOMP 푸시 | `notification/service/command/NotificationCommandService` | nav/domain-misc.md |
| accessLog/monitoring/address/weather | 접속로그(GeoIP/Kafka), Kibana iframe, 주소(암호화), Open-Meteo 날씨 | 각 도메인 controller/service | nav/domain-misc.md |
| global | config, Kafka, Redis, 캐시, WebSocket, 예외, utils | `G/config/*`, `G/exception/*`, `G/utils/*` | nav/global-infra.md |

## 도메인 간 주요 흐름
- **인증**: IpBlockingFilter → Login → Logout → TokenRefresh → JwtAuthentication 필터. 토큰은 HttpOnly 쿠키 `ACCESS_TOKEN`/`REFRESH_TOKEN` + Redis 화이트리스트. → nav/flow-auth-security.md
- **구매**: `POST /order-bids` → Order(PENDING_PAYMENT)+OrderBid → `POST /orders/{id}/process-payment-shipment` → Kafka `order-processing-topic` → `OrderEventConsumer` → `PaymentCommandService.processPayment` → Sale SOLD + 알림, OrderShipment/WarehouseStorage 생성. 즉시구매 `POST /order-bids/instant`는 한 트랜잭션에서 동기로 처리(`OrderCommandService.createInstantOrder`). → nav/flow-trade-buy.md
- **판매**: `POST /sale-bids` / `/sale-bids/instant`(OrderBid 대상) → Sale(PENDING_SHIPMENT)+SaleBid+SaleBankAccount → SellerShipment → 창고 → OrderShipment 송장 등록 → 6시간마다 배치가 CJ 배송조회(Playwright) → DELIVERED → `completeOrder`. → nav/flow-trade-sell.md
- **상품 검색**: QueryDSL `GET /products/query`, `/products/indexed/search`, ES `GET /es/products`(+autocomplete). ES 인덱스 `product-colors`는 앱 시작 시에만 재생성. 상세 조회 시 Kafka `view-log-topic` 발행. → nav/flow-product-search.md
- **의존 허브**: `UserQueryService`(거의 전 도메인), `NotificationCommandService`(user/notice/payment/sale/shipment), `ProductSize`(OrderBid/OrderItem/Sale/SaleBid FK), `ProfileQueryService`(style).

## Kafka 토픽 (토픽명은 Java에 하드코딩)
`order-processing-topic`/`order-retry-topic`(order), `payment-processing-topic`/`payment-retry-topic`(payment, producer 호출처 없음), `notification-topic`(consumer 없음), `user-access-log-topic`(accessLog, prod에서는 `use-kafka=false`), `view-log-topic`(product), `style-view-log-topic`(style). 상세는 nav/global-infra.md.

## 스케줄러
ShipmentBatchScheduler(6h, 배치) · PointExpirationScheduler(매시) · EventStatusScheduler(매시, 로그만) · StyleViewLogBufferManager(10s) · FAQSearchOptimizationAspect(1h) · AccessLogAopConfiguration(10m) · WeatherDataScheduler(기본 24h).

## 빌드·테스트
- `./gradlew test`. `application.yml`의 active profile은 **prod**(`ddl-auto: create`). 테스트 프로필은 `src/main/resources/application-test.yml`의 H2(MySQL 모드)를 쓴다.
- 테스트 45개: `src/test/java/com/fream/back/domain/{address,notification,order,payment,product,sale,style,user}`. 보안/shipment/CS 도메인은 테스트 없음.
- **주의**: 일부 테스트는 현재의 버그 동작을 그대로 기대값으로 고정해 두었다(product 검색 카운트, OrderBid MATCHED, 계좌결제 실패 등). 버그를 고치면 해당 테스트도 함께 수정해야 한다. 각 nav 문서의 Gotchas 참고.
- 상세: nav/build-run-config.md

## nav/ 문서 목록
- nav/domain-user.md — user 도메인 전체
- nav/flow-auth-security.md — 필터 체인, JWT/Redis, OAuth2, IP 차단, 권한
- nav/domain-product.md
- nav/flow-product-search.md — QueryDSL vs ES 검색, 조회 이벤트 Kafka
- nav/domain-order.md
- nav/domain-payment.md — payment 상속 구조, PortOne, PaymentInfo, 암호화
- nav/flow-trade-buy.md — 구매 end-to-end 호출 체인과 상태 머신
- nav/domain-sale.md
- nav/domain-shipment.md — 배송 엔티티, Spring Batch, CJ 스크래핑
- nav/domain-warehouse-storage.md
- nav/flow-trade-sell.md — 판매 end-to-end 호출 체인과 상태 머신
- nav/domain-style.md — 스타일 SNS, 조회수 Kafka 파이프라인
- nav/domain-cs-content.md — notice/faq/inquiry/inspection/event, AOP
- nav/domain-chat-gpt.md — GPT 챗봇
- nav/domain-misc.md — accessLog/monitoring/notification/address/weather
- nav/global-infra.md — config, Kafka/Redis/캐시/WebSocket 전체 목록, 예외 패턴, utils
- nav/build-run-config.md — 스택, 프로필, docker, CI, 테스트 인프라
