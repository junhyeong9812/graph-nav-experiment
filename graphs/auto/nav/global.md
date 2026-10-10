# global (global 패키지와 루트)

기준 경로: `src/main/java/com/fream/back/` · 파일 74개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

타입 선언이 없는 파일(파싱 결과 클래스 0개): `src/main/java/com/fream/back/global/config/WebMvcConfig.java`

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| GET | `/api/logs` | `LogController#getAvailableLogFiles` |  |
| GET | `/api/logs/{fileName}` | `LogController#getLogContent` | `this#readLogFileWithPaging` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `ApplicationShutdownConfig`
  - `shutdownHook` → StyleViewLogBufferManager#flushBufferOnShutdown
- `CjTrackingPlaywright`
  - `getCurrentTrackingStatus` → this#parseStatusFromHtml
- `DataInitializer`
  - `run` → this#createUserWithProfile, this#createAddress, this#createBankAccount, this#createProductData, this#createNoticeData, this#createFAQData, this#createInspectionStandardData, this#createNotificationData, ProductSizeRepository#findAll~, this#createOrderBid, this#createSaleBid, OrderBidRepository#findAll~, this#createSale, this#linkOrderBidToSale, this#createSellerShipment, SaleBidRepository#findAll~, this#processPurchaseAndShipment, this#createStyleData
  - `createUserWithProfile` → UserRepository#existsByEmail, UserRepository#findByEmail, this#generateReferralCode, UserRepository#save~, ProfileCommandService#createDefaultProfile
  - `createNoticeData` → NoticeRepository#save~
  - `createFAQData` → FAQRepository#save~
  - `createInspectionStandardData` → InspectionStandardRepository#save~
  - `createNotificationData` → NotificationRepository#save~
  - `createProductData` → BrandRepository#save~, CategoryRepository#save~, ProductRepository#save~, ProductColorRepository#save~, ProductImageRepository#save~, ProductSizeRepository#save~
  - `createOrderBid` → this#createOrder, OrderBidRepository#save~
  - `createOrder` → OrderRepository#save~
  - `createSaleBid` → SaleRepository#save~, SaleBidRepository#save~
  - `createSale` → SaleRepository#save~
  - `linkOrderBidToSale` → OrderBidRepository#save~
  - `createSellerShipment` → SellerShipmentRepository#save~
  - `processPurchaseAndShipment` → OrderRepository#save~, OrderItemRepository#save~, OrderBidRepository#save~, PaymentRepository#save~, OrderShipmentCommandService#createOrderShipment, WarehouseStorageCommandService#createOrderStorage, SaleBidRepository#save~
  - `createAddress` → PersonalDataEncryptionUtil#deterministicEncrypt, PersonalDataEncryptionUtil#encrypt, AddressRepository#save~
  - `createBankAccount` → BankAccountRepository#save~
  - `createStyleData` → OrderRepository#findByUserAndStatus, OrderItemRepository#findByOrderIn, ProductSizeRepository#findById~, ProductColorRepository#findById~, ProfileRepository#findByUser, this#createStylesForUserWithTransaction
  - `createStylesForUserWithTransaction` → ProfileRepository#findById~, OrderItemRepository#findById~, ProductRepository#findById~, BrandRepository#findById~, StyleRepository#save~, MediaUrlRepository#save~, StyleOrderItemRepository#save~, StyleRepository#saveAndFlush~
- `FileUtils`
  - `saveMediaFile` → this#saveFile
- `FilterAuthService`
  - `authenticateUser` → UserRepository#findByEmail, JwtTokenProvider#generateTokenPair
  - `refreshAccessToken` → JwtTokenProvider#validateToken, AuthRedisService#isRefreshTokenValid, JwtTokenProvider#getEmailFromToken, UserRepository#findByEmail, JwtTokenProvider#generateTokenPair
  - `logout` → AuthRedisService#removeAccessToken, AuthRedisService#removeRefreshToken
  - `validateUser` → UserRepository#findByEmail
- `IpBlockingFilter`
  - `doFilterInternal` → this#getClientIp, this#isLocalhost, IpBlockingRedisService#isIpAllowed, this#handleBlockedIp
  - `handleBlockedIp` → IpBlockingRedisService#getBlockRemainingTime
- `IpBlockingRedisService`
  - `isIpAllowed` → this#isIpBlocked, this#blockIp
- `JwtAuthenticationFilter`
  - `doFilterInternal` → this#getCookieValue, JwtTokenProvider#validateToken, AuthRedisService#isAccessTokenValid, JwtTokenProvider#getEmailFromToken, AuthRedisService#getAgeByAccessToken, AuthRedisService#getGenderByAccessToken
- `JwtTokenProvider`
  - `generateTokenPair` → AuthRedisService#addAccessToken, AuthRedisService#addRefreshToken
- `LoginAuthenticationFilter`
  - `doFilterInternal` → this#isLoginRequest, this#extractLoginRequest, this#handleLogin, this#handleLoginError
  - `handleLogin` → this#getClientIp, AuthService#login, this#setCookie, this#handleLoginSuccess
- `LogoutAuthenticationFilter`
  - `doFilterInternal` → this#isLogoutRequest, this#handleLogout, this#handleLogoutError
  - `handleLogout` → this#getCookieValue, JwtTokenProvider#validateToken, JwtTokenProvider#getEmailFromToken, AuthRedisService#removeAccessToken, AuthRedisService#removeRefreshToken, this#expireCookie, this#handleLogoutSuccess
- `OrderKafkaConfig`
  - `orderProcessingEventKafkaTemplate` → this#orderProcessingEventProducerFactory
  - `orderProcessingKafkaListenerContainerFactory` → this#orderProcessingEventConsumerFactory
  - `notificationKafkaTemplate` → this#notificationProducerFactory
- `PaymentKafkaConfig`
  - `paymentEventKafkaTemplate` → this#paymentEventProducerFactory
  - `paymentEventKafkaListenerContainerFactory` → this#paymentEventConsumerFactory
- `PersonalDataEncryptionUtil`
  - `deterministicEncrypt` → this#generateDeterministicKeyBytes
  - `encryptForSearch` → this#deterministicEncrypt
- `PlaywrightBrowserManager`
  - `openBrowser` → this#closeBrowser
  - `newPage` → this#openBrowser
- `RedisConfig`
  - `writeRedisTemplate` → this#primaryRedisConnectionFactory
  - `readRedisTemplate` → this#replicaRedisConnectionFactory
- `RestTemplateConfig`
  - `restTemplate` → this#loggingInterceptor
- `SecurityConfig`
  - `securityFilterChain` → this#whiteListPaths
- `SecurityUtils`
  - `extractEmailOrAnonymous` → this#extractEmailFromSecurityContext
  - `extractAndValidateEmailForOrderBid` → this#extractEmailFromSecurityContext
  - `extractAndValidateEmailForOrder` → this#extractEmailFromSecurityContext
- `TokenRefreshFilter`
  - `doFilterInternal` → this#isRefreshRequest, this#handleTokenRefresh, this#handleRefreshError
  - `handleTokenRefresh` → this#getCookieValue, JwtTokenProvider#validateToken, AuthRedisService#isRefreshTokenValid, JwtTokenProvider#getEmailFromToken, UserRepository#findByEmail, this#getClientIp, JwtTokenProvider#generateTokenPair, this#setCookie, this#handleRefreshSuccess
- `UserAccessLogKafkaConfig`
  - `userAccessLogKafkaTemplate` → this#userAccessLogProducerFactory
  - `userAccessLogKafkaListenerContainerFactory` → this#userAccessLogConsumerFactory
- `WebSocketAuthInterceptor`
  - `beforeHandshake` → this#extractAccessToken, JwtTokenProvider#validateToken, JwtTokenProvider#getEmailFromToken, UserRepository#findByEmail

## 3. 클래스 목록 (역할별)

### controller (1)

- `LogController` global/controller/LogController.java · public: getAvailableLogFiles, getLogContent

### service (1)

- `IpBlockingRedisService` global/security/redis/IpBlockingRedisService.java · public: isIpAllowed, isIpBlocked, blockIp, unblockIp, getCurrentRequestCount, getBlockRemainingTime · 주입: RedisTemplate

### entity (2)

- `BaseEntity` (abstract class) global/entity/BaseEntity.java · extends/implements BaseTimeEntity
- `BaseTimeEntity` (abstract class) global/entity/BaseTimeEntity.java

### config (29)

- `ApplicationShutdownConfig` global/config/ApplicationShutdownConfig.java · public: shutdownHook · 주입: StyleViewLogBufferManager
- `BatchInfraConfig` global/config/BatchInfraConfig.java · public: jobRepository
- `CacheConfig` global/config/CacheConfig.java · public: cacheManager
- `DataInitializer` global/config/DataInitializer.java · extends/implements CommandLineRunner · public: run, createStylesForUserWithTransaction · 주입: UserRepository, ProfileCommandService, PasswordEncoder, NoticeRepository, FAQRepository, InspectionStandardRepository, NotificationRepository, BrandRepository, CategoryRepository, ProductRepository, ProductColorRepository, ProductSizeRepository, ProductImageRepository, OrderBidRepository, SaleBidRepository, OrderRepository, SaleRepository, OrderItemRepository, SellerShipmentRepository, WarehouseStorageRepository, PaymentCommandService, OrderShipmentCommandService, WarehouseStorageCommandService, PaymentRepository, AddressRepository, BankAccountRepository, StyleRepository, MediaUrlRepository, StyleOrderItemRepository, ProfileRepository, PersonalDataEncryptionUtil
- `EmailBasedUserDestinationResolver` global/config/EmailBasedUserDestinationResolver.java · public: resolveUserDestination · 주입: SimpUserRegistry
- `GPTConfig` global/config/GPTConfig.java · public: getApiKey, getModel, getApiUrl
- `LogViewerConfig` global/config/LogViewerConfig.java · public: logDirectoryPath, corsConfigurationSource
- `QueryDslConfig` global/config/QueryDslConfig.java · public: jpaQueryFactory · 주입: EntityManager
- `RedisConfig` global/config/RedisConfig.java · public: redisConnectionFactory, primaryRedisConnectionFactory, replicaRedisConnectionFactory, redisTemplate, stringRedisTemplate, writeRedisTemplate, readRedisTemplate
- `RestTemplateConfig` global/config/RestTemplateConfig.java · public: restTemplate
- `ShipmentBatchScheduler` global/config/ShipmentBatchScheduler.java · public: scheduleShipmentStatusJob · 주입: JobLauncher, Job
- `SwaggerConfig` global/config/SwaggerConfig.java · public: openAPI
- `WebConfig` global/config/WebConfig.java · extends/implements WebMvcConfigurer · public: addArgumentResolvers
- `PersonalDataEncryptionAspect` global/config/aop/PersonalDataEncryptionAspect.java
- `OrderKafkaConfig` global/config/kafka/OrderKafkaConfig.java · public: orderProcessingEventProducerFactory, orderProcessingEventKafkaTemplate, orderProcessingEventConsumerFactory, orderProcessingKafkaListenerContainerFactory, notificationProducerFactory, notificationKafkaTemplate
- `PaymentKafkaConfig` global/config/kafka/PaymentKafkaConfig.java · public: paymentEventProducerFactory, paymentEventKafkaTemplate, paymentEventConsumerFactory, paymentEventKafkaListenerContainerFactory
- `UserAccessLogKafkaConfig` global/config/kafka/UserAccessLogKafkaConfig.java · public: userAccessLogProducerFactory, userAccessLogKafkaTemplate, userAccessLogConsumerFactory, userAccessLogKafkaListenerContainerFactory
- `EncoderConfig` global/config/security/EncoderConfig.java · public: passwordEncoder
- `JwtAuthenticationFilter` global/config/security/JwtAuthenticationFilter.java · extends/implements OncePerRequestFilter · 주입: JwtTokenProvider, AuthRedisService
- `JwtTokenProvider` global/config/security/JwtTokenProvider.java · public: generateTokenPair, getRoleFromToken, validateToken, getEmailFromToken · 주입: AuthRedisService
- `SecurityConfig` global/config/security/SecurityConfig.java · public: securityFilterChain, authenticationManager · 주입: JwtTokenProvider, AuthRedisService, IpBlockingRedisService, CustomOAuth2UserService, OAuth2AuthenticationSuccessHandler, UserRepository, PasswordEncoder, AuthService
- `TokenDto` global/config/security/dto/TokenDto.java
- `LoginAuthenticationFilter` global/config/security/filter/LoginAuthenticationFilter.java · extends/implements OncePerRequestFilter · 주입: AuthService
- `LogoutAuthenticationFilter` global/config/security/filter/LogoutAuthenticationFilter.java · extends/implements OncePerRequestFilter · 주입: AuthRedisService, JwtTokenProvider
- `TokenRefreshFilter` global/config/security/filter/TokenRefreshFilter.java · extends/implements OncePerRequestFilter · 주입: JwtTokenProvider, AuthRedisService, UserRepository
- `FilterAuthService` global/config/security/service/FilterAuthService.java · public: authenticateUser, refreshAccessToken, logout, validateUser · 주입: UserRepository, PasswordEncoder, JwtTokenProvider, AuthRedisService
- `WebSocketAuthInterceptor` global/config/websocket/WebSocketAuthInterceptor.java · extends/implements HandshakeInterceptor · public: beforeHandshake, afterHandshake · 주입: UserRepository, JwtTokenProvider, RedisTemplate
- `WebSocketConfig` global/config/websocket/WebSocketConfig.java · extends/implements WebSocketMessageBrokerConfigurer · public: configureMessageBroker, registerStompEndpoints · 주입: UserRepository, JwtTokenProvider, RedisTemplate
- `CjTrackingPlaywright` global/utils/CjTrackingPlaywright.java · public: getCurrentTrackingStatus

### dto (5)

- `LogFileDTO` global/dto/LogFileDTO.java
- `LogLineDTO` global/dto/LogLineDTO.java
- `LogResponseDTO` global/dto/LogResponseDTO.java
- `ResponseDto` global/dto/ResponseDto.java
- `commonDto` global/dto/commonDto.java

### exception (26)

- `EntityNotFoundException` global/exception/EntityNotFoundException.java
- `ErrorCode` global/exception/ErrorCode.java
- `GlobalErrorCode` global/exception/GlobalErrorCode.java
- `GlobalException` global/exception/GlobalException.java
- `GlobalExceptionHandler` global/exception/GlobalExceptionHandler.java
- `InvalidRequestException` global/exception/InvalidRequestException.java
- `DirectoryCreationException` global/exception/file/DirectoryCreationException.java
- `FileDownloadException` global/exception/file/FileDownloadException.java
- `FileErrorCode` global/exception/file/FileErrorCode.java
- `FileException` global/exception/file/FileException.java
- `FileNotFoundException` global/exception/file/FileNotFoundException.java
- `FileUploadException` global/exception/file/FileUploadException.java
- `UnsupportedFileTypeException` global/exception/file/UnsupportedFileTypeException.java
- `ExpiredTokenException` global/exception/security/ExpiredTokenException.java
- `InvalidRefreshTokenException` global/exception/security/InvalidRefreshTokenException.java
- `InvalidTokenException` global/exception/security/InvalidTokenException.java
- `SecurityErrorCode` global/exception/security/SecurityErrorCode.java
- `SecurityException` global/exception/security/SecurityException.java
- `SecurityUserNotFoundException` global/exception/security/SecurityUserNotFoundException.java
- `TokenCreationException` global/exception/security/TokenCreationException.java
- `TokenNotFoundException` global/exception/security/TokenNotFoundException.java
- `WebSocketAuthenticationException` global/exception/websocket/WebSocketAuthenticationException.java
- `WebSocketConnectionException` global/exception/websocket/WebSocketConnectionException.java
- `WebSocketErrorCode` global/exception/websocket/WebSocketErrorCode.java
- `WebSocketException` global/exception/websocket/WebSocketException.java
- `WebSocketMessageSendException` global/exception/websocket/WebSocketMessageSendException.java

### other (9)

- `FreamBackApplication` FreamBackApplication.java · public: main
- `ErrorResponse` global/response/ErrorResponse.java · public: of
- `IpBlockingFilter` global/security/filter/IpBlockingFilter.java · extends/implements OncePerRequestFilter · 주입: IpBlockingRedisService
- `FileUtils` global/utils/FileUtils.java · public: existsFile, saveFile, deleteFile, getFileUrl, deleteDirectory, isFileExist, saveMediaFile
- `NginxCachePurgeUtil` global/utils/NginxCachePurgeUtil.java · public: purgeProductCache, purgeEsCache, purgeStyleCache
- `PageUtils` global/utils/PageUtils.java · public: toPageDto
- `PersonalDataEncryptionUtil` global/utils/PersonalDataEncryptionUtil.java · public: encrypt, decrypt, deterministicEncrypt, deterministicDecrypt, encryptForSearch, isEncrypted
- `PlaywrightBrowserManager` global/utils/PlaywrightBrowserManager.java · public: openBrowser, closeBrowser, newPage, isInitialized
- `SecurityUtils` global/utils/SecurityUtils.java · public: extractEmailFromSecurityContext, extractEmailOrAnonymous, extractUserInfo, extractAndValidateEmailForOrderBid, extractAndValidateEmailForOrder

### 다른 도메인 참조 (import·주입 기준)

- **address**: `Address` ← DataInitializer; `AddressRepository` ← DataInitializer
- **faq**: `FAQ` ← DataInitializer; `FAQCategory` ← DataInitializer; `FAQRepository` ← DataInitializer
- **inspection**: `InspectionCategory` ← DataInitializer; `InspectionStandard` ← DataInitializer; `InspectionStandardRepository` ← DataInitializer
- **notice**: `Notice` ← DataInitializer; `NoticeCategory` ← DataInitializer; `NoticeRepository` ← DataInitializer
- **notification**: `Notification` ← DataInitializer; `NotificationCategory` ← DataInitializer; `NotificationRepository` ← DataInitializer; `NotificationType` ← DataInitializer
- **order**: `OrderBidRepository` ← DataInitializer; `OrderItemRepository` ← DataInitializer; `OrderProcessingEvent` ← OrderKafkaConfig; `OrderRepository` ← DataInitializer
- **payment**: `GeneralPayment` ← DataInitializer; `PaymentCommandService` ← DataInitializer; `PaymentEvent` ← PaymentKafkaConfig; `PaymentRepository` ← DataInitializer; `PaymentStatus` ← DataInitializer
- **product**: `BrandRepository` ← DataInitializer; `CategoryRepository` ← DataInitializer; `ColorType` ← DataInitializer; `GenderType` ← DataInitializer; `ProductColorRepository` ← DataInitializer; `ProductImageRepository` ← DataInitializer; `ProductRepository` ← DataInitializer; `ProductSizeRepository` ← DataInitializer; `SizeType` ← DataInitializer
- **sale**: `Sale` ← DataInitializer; `SaleBid` ← DataInitializer; `SaleBidRepository` ← DataInitializer; `SaleRepository` ← DataInitializer; `SaleStatus` ← DataInitializer
- **shipment**: `OrderShipment` ← DataInitializer; `OrderShipmentCommandService` ← DataInitializer; `SellerShipment` ← DataInitializer; `SellerShipmentRepository` ← DataInitializer; `ShipmentStatus` ← DataInitializer
- **style**: `MediaUrl` ← DataInitializer; `MediaUrlRepository` ← DataInitializer; `Style` ← DataInitializer; `StyleOrderItem` ← DataInitializer; `StyleOrderItemRepository` ← DataInitializer; `StyleRepository` ← DataInitializer; `StyleViewLogBufferManager` ← ApplicationShutdownConfig
- **user**: `AuthRedisService` ← FilterAuthService, JwtAuthenticationFilter, JwtTokenProvider, LogoutAuthenticationFilter, SecurityConfig, TokenRefreshFilter; `AuthService` ← LoginAuthenticationFilter, SecurityConfig; `BankAccountRepository` ← DataInitializer; `CustomOAuth2UserService` ← SecurityConfig; `Gender` ← JwtAuthenticationFilter, JwtTokenProvider; `ProfileCommandService` ← DataInitializer; `ProfileRepository` ← DataInitializer; `Role` ← JwtTokenProvider; `User` ← FilterAuthService, TokenRefreshFilter; `UserRepository` ← DataInitializer, FilterAuthService, SecurityConfig, TokenRefreshFilter, WebSocketAuthInterceptor, WebSocketConfig
- **warehouseStorage**: `WarehouseStorage` ← DataInitializer; `WarehouseStorageCommandService` ← DataInitializer; `WarehouseStorageRepository` ← DataInitializer

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

- `BaseEntity` @MappedSuperclass · extends BaseTimeEntity
- `BaseTimeEntity` @MappedSuperclass

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

