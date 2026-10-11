# NAV — 코드 구조 지도 (자동 추출)

`src/main/java`에서 javaparser로 기계 추출한 구조 사실만 담았다. 코드 주석은 옮기지 않았다.
도메인 상세는 `nav/<도메인>.md`, global 패키지는 `nav/global.md`: 엔드포인트 표, 호출 사슬, 클래스 목록(파일 경로), JPA 연관, 이벤트·Kafka, AOP.
표기: 도메인 패키지 = `src/main/java/com/fream/back/domain/<도메인>/` · `A#m` = 클래스 A의 메서드 m · `~` = 이름 기반 추정 · api = 컨트롤러 base path · ent = @Entity · dep = 참조하는 다른 도메인(서비스·리포지토리·엔티티·메시징 클래스) · msg = 발행 → 구독 도메인.

## 도메인

- **m01** (43) · api: Xff19Controller /admin/gpt, Xwr56Controller /chat · ent: Xqh36, Xvb31 · dep: m13, m19
- **m02** (106) · api: Xfw75Controller /brands, Xnh44Controller /collections, Xtx29Controller /categories, Xfp92CommandController /product-colors, Xkr46CommandController /products/command, Xrf21CommandController /interests, Xkr46QueryController /products/query, Xpv24 /admin/products/query, Xrf21QueryController /interests, Xvw73QueryController /products/indexed, Xfp92SearchController /es/products · ent: Xcz24, Xdh20, Xfp92, Xfw75, Xkj24, Xkr46, Xnh44, Xrf21, Xrp21, Xtx29, Xzq99 · dep: m03, m13, m16 · msg: kafka view-log-topic→m02
- **m03** (67) · api: Xcr44CommandController /styles/commands, Xkd33CommandController /styles/likes/commands, Xkp20CommandController /styles/interests/commands, Xqg40CommandController /styles/comments/commands, Xtz28CommandController /hashtags/commands, Xwq96CommandController /styles/comments/likes/commands, Xcr44QueryController /styles/queries, Xqg40QueryController /styles/comments/queries, Xtz28QueryController /hashtags/queries, Xxr14QueryController /styles/queries · ent: Xcr44, Xdw14, Xkd33, Xkp20, Xng32, Xqg40, Xsc53, Xtz28, Xwq96, Xxr14 · dep: m13, m16 · msg: kafka style-view-log-topic→m03
- **m04** (35) · api: EventCommandController /events, EventQueryController /events · ent: Event, Xcg74 · dep: m02, m13
- **m05** (19) · api: Xpb88QueryController /weather/query · ent: Xwv44
- **m06** (39) · api: Xbf54InfoController /payment-info · ent: Xbf54, Xbf54Info, Xjt86, Xkg34, Xqg94 · dep: m10, m13, m15, m16 · msg: kafka payment-processing-topic→m06; kafka payment-retry-topic→m06
- **m07** (22) · api: Xnn94CommandController /warehouse-storage/commands, Xnn94QueryController /warehouse-storage/queries · ent: Xnn94 · dep: m10, m13, m16
- **m08** (5) · api: Xrh55Controller /admin/monitoring
- **m09** (24) · api: Xms37CommandController /inquiry, Xms37QueryController /inquiry · ent: Xms37, Xxv45 · dep: m13
- **m10** (24) · api: Xgg74CommandController /sales, Xzz58CommandController /sale-bids, Xzz58QueryController /sale-bids · ent: Xgg74, Xgz75, Xzz58 · dep: m02, m06, m12, m13, m15, m16
- **m11** (25) · api: Xms98CommandController /addresses, Xms98QueryController /addresses · ent: Xms98 · dep: m13
- **m12** (20) · api: Xhx14CommandController /shipments/order, Xxn30CommandController /shipments/seller · ent: Xhx14, Xxn30 · dep: m07, m10, m13, m15, m16
- **m13** (117) · api: Xbn78Controller /profiles, Xbr34Controller /bank-account, Xqw27Controller /follows, Xwq37Controller /profiles/blocked, Xbw41Controller /admin/users/grades, Xnn43Controller /admin/users/points, Xrq63Controller /admin/users, Xxp37Controller /admin/users/sanctions, Xbr72Controller /auth, Xdk77Controller /identity-verification, Xfx39Controller /admin/auth, Xgg19Controller, Xrd81CommandController /users, Xzs11Controller /oauth, Xnj18CommandController /points/commands, Xnj18QueryController /points/queries, Xrd81QueryController /users · ent: Xbn78, Xbr34, Xhd56, Xkx85, Xmg91, Xnj18, Xqw27, Xrd81, Xwq37 · dep: m02, m03, m06, m11, m15
- **m14** (20) · api: Xfp46CommandController /inspections, Xfp46QueryController /inspections · ent: Xnf79, Xtb30 · dep: m13
- **m15** (14) · api: Xkh73CommandController /notifications, Xkh73QueryController /notifications · ent: Xkh73 · dep: m13, m16
- **m16** (54) · api: Xdg64CommandController /orders, Xzj27CommandController /order-bids, Xdg64QueryController /orders, Xzj27QueryController /order-bids · ent: Xdg64, Xhm58, Xzj27 · dep: m02, m06, m07, m10, m11, m12, m13 · msg: kafka notification-topic→(소스 내 구독 없음); kafka order-processing-topic→m16; kafka order-retry-topic→m16
- **m17** (20) · api: Xpk42CommandController /notices, Xpk42QueryController /notices · ent: Xpk42, Xzv15 · dep: m13, m15
- **m18** (33) · api: Xvh65CommandController /access-log/commands, Xvh65QueryController /access-log/queries · ent: Xvh65 · msg: kafka user-access-log-topic→m18
- **m19** (30) · api: Xbx83CommandController /api/faq, Xbx83QueryController /api/faq · ent: Xbx83, Xgr87 · dep: m13

## global (`src/main/java/com/fream/back/global/`)

- **(root)** (1): FreamBackApplication
- **config** (13): CacheConfig, RedisConfig, WebConfig, Xct70Config, Xdx96Config, Xgd66Config, Xnp67, Xnp72Config, Xpb98BatchScheduler, Xsd33Config, Xwp93, Xxm22TemplateConfig, Xxm98Config
- **config.aop** (1): Xtf80Aspect
- **config.kafka** (3): Xbf54KafkaConfig, Xdg64KafkaConfig, Xvh65KafkaConfig
- **config.security** (9): SecurityConfig, Xsc93Provider, Xxb74Filter, Xzt60Config, Xfx15Dto, Xkx62Filter, Xxt21Filter, Xxz63Filter, Xfj19Service
- **config.websocket** (2): WebSocketConfig, Xqs35Interceptor
- **controller** (1): Xgk12Controller
- **dto** (5): ResponseDto, Xcm11DTO, Xgk12ResponseDTO, Xmx58DTO, commonDto
- **entity** (2): BaseEntity, Xwb79Entity
- **exception** (6): ErrorCode, Xfh56RequestException, Xgh87Exception, Xks43ErrorCode, Xks43Exception, Xks43ExceptionHandler
- **exception.file** (7): Xdg50Exception, Xgw12Exception, Xnr13TypeException, Xpz63ErrorCode, Xpz63Exception, Xtp86Exception, Xxb18Exception
- **exception.security** (8): SecurityErrorCode, SecurityException, Xdc78Exception, Xdr94Exception, Xkt72Exception, Xrg22Exception, Xwm26Exception, Xxt33Exception
- **exception.websocket** (5): WebSocketErrorCode, WebSocketException, Xbb59Exception, Xfw87Exception, Xtb29Exception
- **response** (1): ErrorResponse
- **security.filter** (1): Xgp51Filter
- **security.redis** (1): Xgp51RedisService
- **utils** (7): PageUtils, SecurityUtils, Xgz45Util, Xpz63Utils, Xrk74, Xtf80Util, Xxx20
