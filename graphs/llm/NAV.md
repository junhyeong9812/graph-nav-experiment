# NAV — fream-back 코드 지도

KREAM류 리셀 마켓. Spring Boot 3.4.1 / Java 17 / Gradle, JPA+QueryDSL(MySQL, test=H2), Redis, Kafka, Elasticsearch, STOMP, Batch, OAuth2, PortOne, OpenAI.
- 루트 패키지: `src/main/java/com/fream/back/` (`FreamBackApplication.java`)
- `domain/<도메인>/` 19개 + `global/`(설정·보안·예외·유틸). 설정: `src/main/resources/application*.yml`
- 테스트: `src/test/.../FreamBackApplicationTests.java`(contextLoads) 1개
- 아래 경로는 `src/main/java/com/fream/back/` 기준 상대경로.

## 공통 구조 (→ nav/conventions.md)
- 도메인 레이아웃: `controller/{command,query}`, `service/{command,query}`, `repository/`(QueryDSL은 `XxxRepositoryCustom`+`XxxRepositoryImpl`), `dto/`, `entity/`, `exception/`(`<D>ErrorCode` + `<D>Exception extends GlobalException`)
- 현재 사용자: `global/utils/SecurityUtils.extractEmailFromSecurityContext()`(principal은 email)
- 관리자 권한: URL 규칙이 아니라 코드에서 `domain/user/service/query/UserQueryService#checkAdminRole(email)`을 호출해 확인
- 예외 → `global/exception/GlobalExceptionHandler.java` → `global/response/ErrorResponse`
- 파일 업로드: `global/utils/FileUtils.saveFile`(루트 `/home/ubuntu/fream` 하드코딩)
- prod nginx가 `/api` 접두사를 떼고 넘기므로 컨트롤러 매핑에는 `/api`가 없음

## 보안 / 인증 (→ nav/global.md, nav/user.md)
- `global/config/security/SecurityConfig.java` 필터 순서: IpBlocking → Login(`POST /auth/login`) → Logout → TokenRefresh(`POST /auth/refresh`) → `JwtAuthenticationFilter`
- JWT는 쿠키 `ACCESS_TOKEN`/`REFRESH_TOKEN`으로만 주고받고 Authorization 헤더는 읽지 않음. Redis 화이트리스트: `domain/user/redis/AuthRedisService.java`
- 화이트리스트 경로 외에는 모두 authenticated. hasRole 규칙 없음(authorities null)
- OAuth2(google/naver/kakao): `domain/user/security/oauth2/`

## 도메인 일람
| 도메인 | 역할 | 주요 진입점 (`domain/` 기준) | 문서 |
|---|---|---|---|
| user | 회원·인증·프로필·팔로우/차단·계좌·포인트·등급·제재·관리자 | `user/controller/**`, `user/service/query/UserQueryService`, `user/entity/User` | nav/user.md |
| product | 상품/색상/사이즈·브랜드·카테고리·컬렉션·관심·ES 검색 | `product/controller/query/ProductQueryController`, `product/service/productColor/ProductColorCommandService`, `product/repository/ProductQueryDslRepository`, `product/elasticsearch/` | nav/product.md |
| order | 구매 입찰(OrderBid)·주문(Order/OrderItem) | `order/controller/{command,query}/Order*Controller`, `order/service/kafka/OrderEvent{Producer,Consumer}` | nav/order.md |
| sale | 판매 입찰(SaleBid)·판매(Sale)·판매 계좌 | `sale/controller/command/Sale*CommandController` | nav/sale.md |
| payment | 결제·결제수단(PaymentInfo)·PortOne | `payment/controller/command/PaymentInfoController`, `payment/portone/`, `payment/service/kafka/` | nav/payment.md |
| shipment | 구매자/판매자 배송·CJ 송장 추적 배치 | `shipment/controller/command/*ShipmentCommandController`, `global/config/ShipmentBatchScheduler` | nav/shipment.md |
| warehouseStorage | 창고 보관·보관 연장 | `warehouseStorage/controller/**` | nav/warehouseStorage.md |
| style | 스타일 피드·댓글·좋아요·해시태그·조회로그 | `style/controller/**`, `style/service/command/StyleCommandService`, `style/repository/StyleRepositoryCustomImpl` | nav/style.md |
| notification | 알림 저장 + STOMP 푸시 | `notification/service/command/NotificationCommandService`, `global/config/websocket/` | nav/notification.md |
| event | 브랜드 이벤트(+스케줄러, AOP) | `event/controller/**`, `event/scheduler/EventStatusScheduler` | nav/event.md |
| faq / inquiry / inspection / notice | CS 콘텐츠(FAQ, 1:1 문의, 검수기준, 공지) | 각 `controller/{command,query}` | nav/cs-content.md |
| chatQuestion | FAQ 기반 GPT 챗봇·사용량 로그 | `chatQuestion/controller/ChatController`, `global/config/GPTConfig` | nav/chatQuestion.md |
| address | 배송지(개인정보 암호화) | `address/controller/**` | nav/address.md |
| accessLog | 접속 로그(GeoIP, Kafka 선택) | `accessLog/controller/**` | nav/accessLog.md |
| weather | Open-Meteo 날씨 수집 | `weather/controller/WeatherQueryController` | nav/weather.md |
| monitoring | Kibana iframe 관리자 페이지 | `monitoring/controller/AdminMonitoringController` | nav/monitoring.md |

## 도메인 간 의존 (import 기준)
- order → address, payment, product, sale, shipment, user, warehouseStorage
- sale → notification, order, payment, product, shipment, user
- payment → notification, order, sale, user / shipment → notification, order, sale, user, warehouseStorage
- warehouseStorage → order, sale, user / style → order, product, user / product → order, style, user
- user → address, notification, payment, product, style / notification → order, user
- event → product, user / notice → notification, user / chatQuestion → faq, user / faq·inquiry·inspection·address → user
- accessLog, weather, monitoring은 다른 도메인을 import하지 않음

## 주요 흐름
- **거래**: 입찰(OrderBid/SaleBid) → Order 생성 → Kafka로 결제(PortOne)·배송 처리 → 판매자 발송 → 구매자 배송 또는 창고 보관. 검수 단계는 미구현 → `nav/flow-trade.md`
- **인증**: 로그인 필터 → JWT 쿠키 → Redis 화이트리스트 → `JwtAuthenticationFilter` → `nav/user.md`
- **알림**: 각 도메인 서비스가 `NotificationCommandService`를 호출 → DB 저장 → `/user/{email}/queue/notifications` 푸시 → `nav/notification.md`
- **조회 로그**: 상품/스타일 상세 → Kafka(아래 표) → 로그 저장
- **상품 검색**: QueryDSL(`/products/query`) 또는 ES(`/es/products`, 시작 시에만 재색인)

## Kafka 토픽 (리스너 위치)
| 토픽 | 생산 | 소비(group) |
|---|---|---|
| order-processing-topic / order-retry-topic | `OrderEventProducer` | `OrderEventConsumer`(order-processing-group / order-retry-group) |
| payment-processing-topic / payment-retry-topic | `PaymentEventProducer`(processing 쪽은 호출처 없음) | `PaymentEventConsumer`(payment-processing-group / payment-retry-group) |
| user-access-log-topic | `UserAccessLogProducer` | `UserAccessLogConsumer` + `_backup`(같은 group) |
| view-log-topic | `ViewEventProducer` | `ViewEventConsumer` |
| style-view-log-topic | `StyleViewEventProducer` | `StyleViewEventConsumer`(style-view-group) |
| notification-topic | `OrderEventConsumer`가 전송 | **소비자 없음** |

## 스케줄러
`EventStatusScheduler`·`PointExpirationScheduler`(매시), `global/config/ShipmentBatchScheduler`(6시간), `StyleViewLogBufferManager`(10초), `WeatherDataScheduler`(24시간), `AccessLogAopConfiguration`(10분), `FAQSearchOptimizationAspect`(1시간)

## 함정 (상세는 각 문서)
- `application.yml`의 활성 프로필은 prod이고, prod는 `ddl-auto: create`. `global/config/DataInitializer`는 프로필 조건 없이 매번 시드 데이터를 넣음
- 만료 토큰 예외를 JWT 필터에서 잡지 않음. refresh 시 새 refresh 쿠키를 내려주지 않음
- ErrorResponse의 message는 항상 enum 기본 문구. 코드 `S001`이 security·sale·shipment에서 중복
- event/faq/address/chatQuestion의 커스텀 AOP 어노테이션은 적용처가 없음
- 이벤트 캐시 이름이 `CacheConfig`에 없음. order/sale의 `BidStatus` enum 중복. 입찰은 생성 즉시 MATCHED

## nav/ 문서 목록
- `nav/conventions.md` — 컨벤션, 새 기능 체크리스트
- `nav/global.md` — global 패키지(보안·예외·Kafka/Redis/WebSocket/캐시·유틸)
- `nav/infra.md` — 프로필·yml·docker·nginx·CI·빌드
- `nav/flow-trade.md` — 거래 흐름·상태 전이
- 도메인별 문서: 위 도메인 일람 표의 "문서" 열 참고
