# NAV — 코드 구조 지도 (자동 추출)

`src/main/java`에서 javaparser로 기계 추출한 구조 사실만 담았다. 코드 주석은 옮기지 않았다.
도메인 상세는 `nav/<도메인>.md`, global 패키지는 `nav/global.md`: 엔드포인트 표, 호출 사슬, 클래스 목록(파일 경로), JPA 연관, 이벤트·Kafka, AOP.
표기: 도메인 패키지 = `src/main/java/com/fream/back/domain/<도메인>/` · `A#m` = 클래스 A의 메서드 m · `~` = 이름 기반 추정 · api = 컨트롤러 base path · ent = @Entity · dep = 참조하는 다른 도메인(서비스·리포지토리·엔티티·메시징 클래스) · msg = 발행 → 구독 도메인.

## 도메인

- **accessLog** (24) · api: AccessLogCommandController /inquiry, AccessLogQueryController /inquiry · ent: AccessLog, AccessLogImage · dep: product
- **address** (20) · api: FaqAddressCommandController /shipments/order, SellerAddressCommandController /shipments/seller · ent: FaqAddress, SellerAddress · dep: faq, notice, payment, product, style
- **chatQuestion** (5) · api: AdminChatQuestionController /admin/monitoring
- **event** (35) · api: EventCommandController /events, EventQueryController /events · ent: Event, SimpleImage · dep: product, user
- **faq** (54) · api: FaqBidCommandController /order-bids, FaqCommandController /orders, FaqBidQueryController /order-bids, FaqQueryController /orders · ent: Faq, FaqBid, FaqItem · dep: address, notice, notification, product, shipment, style, user · msg: kafka notification-topic→(소스 내 구독 없음); kafka order-processing-topic→faq; kafka order-retry-topic→faq
- **inquiry** (33) · api: ProductInquiryCommandController /access-log/commands, ProductInquiryQueryController /access-log/queries · ent: ProductInquiry · msg: kafka user-access-log-topic→inquiry
- **inspection** (19) · api: InspectionQueryController /weather/query · ent: InspectionData
- **monitoring** (43) · api: ChatController /chat, GPTUsageController /admin/gpt · ent: GPTUsageLog, Monitoring · dep: order, product
- **notice** (22) · api: NoticeCommandController /warehouse-storage/commands, NoticeQueryController /warehouse-storage/queries · ent: Notice · dep: faq, product, style
- **notification** (39) · api: NotificationInfoController /payment-info · ent: AccountNotification, CardNotification, GeneralNotification, Notification, NotificationInfo · dep: faq, payment, product, style · msg: kafka payment-processing-topic→notification; kafka payment-retry-topic→notification
- **order** (30) · api: OrderCommandController /api/faq, OrderQueryController /api/faq · ent: Order, OrderImage · dep: product
- **payment** (14) · api: PaymentCommandController /notifications, PaymentQueryController /notifications · ent: Payment · dep: faq, product
- **product** (117) · api: BankAccountController /bank-account, BlockedProfileController /profiles/blocked, FollowController /follows, ProfileController /profiles, AdminGradeController /admin/users/grades, AdminPointController /admin/users/points, AdminProductController /admin/users, AdminSanctionController /admin/users/sanctions, AdminAuthController /admin/auth, AuthController /auth, IdentityVerificationController /identity-verification, OAuth2AuthorizationController, OAuthController /oauth, ProductCommandController /users, PointCommandController /points/commands, PointQueryController /points/queries, ProductQueryController /users · ent: BankAccount, BlockedProfile, Follow, OAuthConnection, Point, Product, ProductGrade, ProductSanction, Profile · dep: notification, payment, sale, shipment, user
- **sale** (67) · api: HashtagCommandController /hashtags/commands, SaleCommandController /styles/commands, SaleCommentCommandController /styles/comments/commands, SaleCommentLikeCommandController /styles/comments/likes/commands, SaleInterestCommandController /styles/interests/commands, SaleLikeCommandController /styles/likes/commands, HashtagQueryController /hashtags/queries, SaleCommentQueryController /styles/comments/queries, SaleHashtagQueryController /styles/queries, SaleQueryController /styles/queries · ent: Hashtag, MediaUrl, Sale, SaleComment, SaleCommentLike, SaleFaqItem, SaleHashtag, SaleInterest, SaleLike, SaleViewLog · dep: faq, product · msg: kafka style-view-log-topic→sale
- **shipment** (25) · api: ShipmentCommandController /addresses, ShipmentQueryController /addresses · ent: Shipment · dep: product
- **style** (24) · api: StyleBidCommandController /sale-bids, StyleCommandController /sales, StyleBidQueryController /sale-bids · ent: Style, StyleBankAccount, StyleBid · dep: address, faq, notification, payment, product, user
- **user** (106) · api: BrandController /brands, CategoryController /categories, CollectionController /collections, InterestCommandController /interests, UserColorCommandController /product-colors, UserCommandController /products/command, IndexedUserQueryController /products/indexed, InterestQueryController /interests, UserQueryController /products/query, UserQueryControllerForAdmin /admin/products/query, UserColorSearchController /es/products · ent: Brand, Category, Collection, Interest, User, UserColor, UserColorViewLog, UserDetailImage, UserImage, UserPriceHistory, UserSize · dep: faq, product, sale · msg: kafka view-log-topic→user
- **warehouseStorage** (20) · api: WarehouseStorageCommandController /notices, WarehouseStorageQueryController /notices · ent: WarehouseStorage, WarehouseStorageImage · dep: payment, product
- **weather** (20) · api: WeatherCommandController /inspections, WeatherQueryController /inspections · ent: WeatherStandard, WeatherStandardImage · dep: product

## global (`src/main/java/com/fream/back/global/`)

- **(root)** (1): FreamBackApplication
- **config** (13): AddressBatchScheduler, ApplicationShutdownConfig, BatchInfraConfig, CacheConfig, DataInitializer, EmailBasedProductDestinationResolver, GPTConfig, LogViewerConfig, QueryDslConfig, RedisConfig, RestTemplateConfig, SwaggerConfig, WebConfig
- **config.aop** (1): PersonalDataEncryptionAspect
- **config.kafka** (3): FaqKafkaConfig, NotificationKafkaConfig, ProductInquiryKafkaConfig
- **config.security** (9): EncoderConfig, JwtAuthenticationFilter, JwtTokenProvider, SecurityConfig, TokenDto, LoginAuthenticationFilter, LogoutAuthenticationFilter, TokenRefreshFilter, FilterAuthService
- **config.websocket** (2): WebSocketAuthInterceptor, WebSocketConfig
- **controller** (1): LogController
- **dto** (5): LogFileDTO, LogLineDTO, LogResponseDTO, ResponseDto, commonDto
- **entity** (2): BaseEntity, BaseTimeEntity
- **exception** (6): EntityNotFoundException, ErrorCode, GlobalErrorCode, GlobalException, GlobalExceptionHandler, InvalidRequestException
- **exception.file** (7): DirectoryCreationException, FileDownloadException, FileErrorCode, FileException, FileNotFoundException, FileUploadException, UnsupportedFileTypeException
- **exception.security** (8): ExpiredTokenException, InvalidRefreshTokenException, InvalidTokenException, SecurityErrorCode, SecurityException, SecurityProductNotFoundException, TokenCreationException, TokenNotFoundException
- **exception.websocket** (5): WebSocketAuthenticationException, WebSocketConnectionException, WebSocketErrorCode, WebSocketException, WebSocketMessageSendException
- **response** (1): ErrorResponse
- **security.filter** (1): IpBlockingFilter
- **security.redis** (1): IpBlockingRedisService
- **utils** (7): CjTrackingPlaywright, FileUtils, NginxCachePurgeUtil, PageUtils, PersonalDataEncryptionUtil, PlaywrightBrowserManager, SecurityUtils
