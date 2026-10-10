# NAV — 코드 구조 지도 (자동 추출)

`src/main/java`에서 javaparser로 기계 추출한 구조 사실만 담았다. 코드 주석은 옮기지 않았다.
도메인 상세는 `nav/<도메인>.md`, global 패키지는 `nav/global.md`: 엔드포인트 표, 호출 사슬, 클래스 목록(파일 경로), JPA 연관, 이벤트·Kafka, AOP.
표기: 도메인 패키지 = `src/main/java/com/fream/back/domain/<도메인>/` · `A#m` = 클래스 A의 메서드 m · `~` = 이름 기반 추정 · api = 컨트롤러 base path · ent = @Entity · dep = 참조하는 다른 도메인(서비스·리포지토리·엔티티·메시징 클래스) · msg = 발행 → 구독 도메인.

## 도메인

- **accessLog** (33) · api: UserAccessLogCommandController /access-log/commands, UserAccessLogQueryController /access-log/queries · ent: UserAccessLog · msg: kafka user-access-log-topic→accessLog
- **address** (25) · api: AddressCommandController /addresses, AddressQueryController /addresses · ent: Address · dep: user
- **chatQuestion** (43) · api: ChatController /chat, GPTUsageController /admin/gpt · ent: ChatQuestion, GPTUsageLog · dep: faq, user
- **event** (35) · api: EventCommandController /events, EventQueryController /events · ent: Event, SimpleImage · dep: product, user
- **faq** (30) · api: FAQCommandController /api/faq, FAQQueryController /api/faq · ent: FAQ, FAQImage · dep: user
- **inquiry** (24) · api: InquiryCommandController /inquiry, InquiryQueryController /inquiry · ent: Inquiry, InquiryImage · dep: user
- **inspection** (20) · api: InspectionCommandController /inspections, InspectionQueryController /inspections · ent: InspectionStandard, InspectionStandardImage · dep: user
- **monitoring** (5) · api: AdminMonitoringController /admin/monitoring
- **notice** (20) · api: NoticeCommandController /notices, NoticeQueryController /notices · ent: Notice, NoticeImage · dep: notification, user
- **notification** (14) · api: NotificationCommandController /notifications, NotificationQueryController /notifications · ent: Notification · dep: order, user
- **order** (54) · api: OrderBidCommandController /order-bids, OrderCommandController /orders, OrderBidQueryController /order-bids, OrderQueryController /orders · ent: Order, OrderBid, OrderItem · dep: address, payment, product, sale, shipment, user, warehouseStorage · msg: kafka notification-topic→(소스 내 구독 없음); kafka order-processing-topic→order; kafka order-retry-topic→order
- **payment** (39) · api: PaymentInfoController /payment-info · ent: AccountPayment, CardPayment, GeneralPayment, Payment, PaymentInfo · dep: notification, order, sale, user · msg: kafka payment-processing-topic→payment; kafka payment-retry-topic→payment
- **product** (106) · api: BrandController /brands, CategoryController /categories, CollectionController /collections, InterestCommandController /interests, ProductColorCommandController /product-colors, ProductCommandController /products/command, IndexedProductQueryController /products/indexed, InterestQueryController /interests, ProductQueryController /products/query, ProductQueryControllerForAdmin /admin/products/query, ProductColorSearchController /es/products · ent: Brand, Category, Collection, Interest, Product, ProductColor, ProductColorViewLog, ProductDetailImage, ProductImage, ProductPriceHistory, ProductSize · dep: order, style, user · msg: kafka view-log-topic→product
- **sale** (24) · api: SaleBidCommandController /sale-bids, SaleCommandController /sales, SaleBidQueryController /sale-bids · ent: Sale, SaleBankAccount, SaleBid · dep: notification, order, payment, product, shipment, user
- **shipment** (20) · api: OrderShipmentCommandController /shipments/order, SellerShipmentCommandController /shipments/seller · ent: OrderShipment, SellerShipment · dep: notification, order, sale, user, warehouseStorage
- **style** (67) · api: HashtagCommandController /hashtags/commands, StyleCommandController /styles/commands, StyleCommentCommandController /styles/comments/commands, StyleCommentLikeCommandController /styles/comments/likes/commands, StyleInterestCommandController /styles/interests/commands, StyleLikeCommandController /styles/likes/commands, HashtagQueryController /hashtags/queries, StyleCommentQueryController /styles/comments/queries, StyleHashtagQueryController /styles/queries, StyleQueryController /styles/queries · ent: Hashtag, MediaUrl, Style, StyleComment, StyleCommentLike, StyleHashtag, StyleInterest, StyleLike, StyleOrderItem, StyleViewLog · dep: order, user · msg: kafka style-view-log-topic→style
- **user** (117) · api: BankAccountController /bank-account, BlockedProfileController /profiles/blocked, FollowController /follows, ProfileController /profiles, AdminGradeController /admin/users/grades, AdminPointController /admin/users/points, AdminSanctionController /admin/users/sanctions, AdminUserController /admin/users, AdminAuthController /admin/auth, AuthController /auth, IdentityVerificationController /identity-verification, OAuth2AuthorizationController, OAuthController /oauth, UserCommandController /users, PointCommandController /points/commands, PointQueryController /points/queries, UserQueryController /users · ent: BankAccount, BlockedProfile, Follow, OAuthConnection, Point, Profile, User, UserGrade, UserSanction · dep: address, notification, payment, product, style
- **warehouseStorage** (22) · api: WarehouseStorageCommandController /warehouse-storage/commands, WarehouseStorageQueryController /warehouse-storage/queries · ent: WarehouseStorage · dep: order, sale, user
- **weather** (19) · api: WeatherQueryController /weather/query · ent: WeatherData

## global (`src/main/java/com/fream/back/global/`)

- **(root)** (1): FreamBackApplication
- **config** (13): ApplicationShutdownConfig, BatchInfraConfig, CacheConfig, DataInitializer, EmailBasedUserDestinationResolver, GPTConfig, LogViewerConfig, QueryDslConfig, RedisConfig, RestTemplateConfig, ShipmentBatchScheduler, SwaggerConfig, WebConfig
- **config.aop** (1): PersonalDataEncryptionAspect
- **config.kafka** (3): OrderKafkaConfig, PaymentKafkaConfig, UserAccessLogKafkaConfig
- **config.security** (9): EncoderConfig, JwtAuthenticationFilter, JwtTokenProvider, SecurityConfig, TokenDto, LoginAuthenticationFilter, LogoutAuthenticationFilter, TokenRefreshFilter, FilterAuthService
- **config.websocket** (2): WebSocketAuthInterceptor, WebSocketConfig
- **controller** (1): LogController
- **dto** (5): LogFileDTO, LogLineDTO, LogResponseDTO, ResponseDto, commonDto
- **entity** (2): BaseEntity, BaseTimeEntity
- **exception** (6): EntityNotFoundException, ErrorCode, GlobalErrorCode, GlobalException, GlobalExceptionHandler, InvalidRequestException
- **exception.file** (7): DirectoryCreationException, FileDownloadException, FileErrorCode, FileException, FileNotFoundException, FileUploadException, UnsupportedFileTypeException
- **exception.security** (8): ExpiredTokenException, InvalidRefreshTokenException, InvalidTokenException, SecurityErrorCode, SecurityException, SecurityUserNotFoundException, TokenCreationException, TokenNotFoundException
- **exception.websocket** (5): WebSocketAuthenticationException, WebSocketConnectionException, WebSocketErrorCode, WebSocketException, WebSocketMessageSendException
- **response** (1): ErrorResponse
- **security.filter** (1): IpBlockingFilter
- **security.redis** (1): IpBlockingRedisService
- **utils** (7): CjTrackingPlaywright, FileUtils, NginxCachePurgeUtil, PageUtils, PersonalDataEncryptionUtil, PlaywrightBrowserManager, SecurityUtils
