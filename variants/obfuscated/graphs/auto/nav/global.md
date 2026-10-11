# global (global 패키지와 루트)

기준 경로: `src/main/java/com/fream/back/` · 파일 74개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

타입 선언이 없는 파일(파싱 결과 클래스 0개): `src/main/java/com/fream/back/global/config/Xnp46Config.java`

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| GET | `/api/logs` | `Xgk12Controller#getAvailableLogFiles` |  |
| GET | `/api/logs/{fileName}` | `Xgk12Controller#getLogContent` | `this#readLogFileWithPaging` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `RedisConfig`
  - `writeRedisTemplate` → this#primaryRedisConnectionFactory
  - `readRedisTemplate` → this#replicaRedisConnectionFactory
- `SecurityConfig`
  - `securityFilterChain` → this#whiteListPaths
- `SecurityUtils`
  - `extractEmailOrAnonymous` → this#extractEmailFromSecurityContext
  - `extractAndValidateEmailForOrderBid` → this#extractEmailFromSecurityContext
  - `extractAndValidateEmailForOrder` → this#extractEmailFromSecurityContext
- `Xbf54KafkaConfig`
  - `paymentEventKafkaTemplate` → this#paymentEventProducerFactory
  - `paymentEventKafkaListenerContainerFactory` → this#paymentEventConsumerFactory
- `Xdg64KafkaConfig`
  - `orderProcessingEventKafkaTemplate` → this#orderProcessingEventProducerFactory
  - `orderProcessingKafkaListenerContainerFactory` → this#orderProcessingEventConsumerFactory
  - `notificationKafkaTemplate` → this#notificationProducerFactory
- `Xfj19Service`
  - `authenticateUser` → Xrd81Repository#findByEmail, Xsc93Provider#generateTokenPair
  - `refreshAccessToken` → Xsc93Provider#validateToken, Xbr72RedisService#isRefreshTokenValid, Xsc93Provider#getEmailFromToken, Xrd81Repository#findByEmail, Xsc93Provider#generateTokenPair
  - `logout` → Xbr72RedisService#removeAccessToken, Xbr72RedisService#removeRefreshToken
  - `validateUser` → Xrd81Repository#findByEmail
- `Xgp51Filter`
  - `doFilterInternal` → this#getClientIp, this#isLocalhost, Xgp51RedisService#isIpAllowed, this#handleBlockedIp
  - `handleBlockedIp` → Xgp51RedisService#getBlockRemainingTime
- `Xgp51RedisService`
  - `isIpAllowed` → this#isIpBlocked, this#blockIp
- `Xkx62Filter`
  - `doFilterInternal` → this#isRefreshRequest, this#handleTokenRefresh, this#handleRefreshError
  - `handleTokenRefresh` → this#getCookieValue, Xsc93Provider#validateToken, Xbr72RedisService#isRefreshTokenValid, Xsc93Provider#getEmailFromToken, Xrd81Repository#findByEmail, this#getClientIp, Xsc93Provider#generateTokenPair, this#setCookie, this#handleRefreshSuccess
- `Xnp67`
  - `run` → this#createUserWithProfile, this#createAddress, this#createBankAccount, this#createProductData, this#createNoticeData, this#createFAQData, this#createInspectionStandardData, this#createNotificationData, Xdh20Repository#findAll~, this#createOrderBid, this#createSaleBid, Xzj27Repository#findAll~, this#createSale, this#linkOrderBidToSale, this#createSellerShipment, Xzz58Repository#findAll~, this#processPurchaseAndShipment, this#createStyleData
  - `createUserWithProfile` → Xrd81Repository#existsByEmail, Xrd81Repository#findByEmail, this#generateReferralCode, Xrd81Repository#save~, Xbn78CommandService#createDefaultProfile
  - `createNoticeData` → Xpk42Repository#save~
  - `createFAQData` → Xbx83Repository#save~
  - `createInspectionStandardData` → Xnf79Repository#save~
  - `createNotificationData` → Xkh73Repository#save~
  - `createProductData` → Xfw75Repository#save~, Xtx29Repository#save~, Xkr46Repository#save~, Xfp92Repository#save~, Xzq99Repository#save~, Xdh20Repository#save~
  - `createOrderBid` → this#createOrder, Xzj27Repository#save~
  - `createOrder` → Xdg64Repository#save~
  - `createSaleBid` → Xgg74Repository#save~, Xzz58Repository#save~
  - `createSale` → Xgg74Repository#save~
  - `linkOrderBidToSale` → Xzj27Repository#save~
  - `createSellerShipment` → Xxn30Repository#save~
  - `processPurchaseAndShipment` → Xdg64Repository#save~, Xhm58Repository#save~, Xzj27Repository#save~, Xbf54Repository#save~, Xhx14CommandService#createOrderShipment, Xnn94CommandService#createOrderStorage, Xzz58Repository#save~
  - `createAddress` → Xtf80Util#deterministicEncrypt, Xtf80Util#encrypt, Xms98Repository#save~
  - `createBankAccount` → Xbr34Repository#save~
  - `createStyleData` → Xdg64Repository#findByUserAndStatus, Xhm58Repository#findByOrderIn, Xdh20Repository#findById~, Xfp92Repository#findById~, Xbn78Repository#findByUser, this#createStylesForUserWithTransaction
  - `createStylesForUserWithTransaction` → Xbn78Repository#findById~, Xhm58Repository#findById~, Xkr46Repository#findById~, Xfw75Repository#findById~, Xcr44Repository#save~, Xsc53Repository#save~, Xdw14Repository#save~, Xcr44Repository#saveAndFlush~
- `Xpz63Utils`
  - `saveMediaFile` → this#saveFile
- `Xqs35Interceptor`
  - `beforeHandshake` → this#extractAccessToken, Xsc93Provider#validateToken, Xsc93Provider#getEmailFromToken, Xrd81Repository#findByEmail
- `Xrk74`
  - `openBrowser` → this#closeBrowser
  - `newPage` → this#openBrowser
- `Xsc93Provider`
  - `generateTokenPair` → Xbr72RedisService#addAccessToken, Xbr72RedisService#addRefreshToken
- `Xtf80Util`
  - `deterministicEncrypt` → this#generateDeterministicKeyBytes
  - `encryptForSearch` → this#deterministicEncrypt
- `Xvh65KafkaConfig`
  - `userAccessLogKafkaTemplate` → this#userAccessLogProducerFactory
  - `userAccessLogKafkaListenerContainerFactory` → this#userAccessLogConsumerFactory
- `Xxb74Filter`
  - `doFilterInternal` → this#getCookieValue, Xsc93Provider#validateToken, Xbr72RedisService#isAccessTokenValid, Xsc93Provider#getEmailFromToken, Xbr72RedisService#getAgeByAccessToken, Xbr72RedisService#getGenderByAccessToken
- `Xxm22TemplateConfig`
  - `restTemplate` → this#loggingInterceptor
- `Xxm98Config`
  - `shutdownHook` → Xgp60#flushBufferOnShutdown
- `Xxt21Filter`
  - `doFilterInternal` → this#isLogoutRequest, this#handleLogout, this#handleLogoutError
  - `handleLogout` → this#getCookieValue, Xsc93Provider#validateToken, Xsc93Provider#getEmailFromToken, Xbr72RedisService#removeAccessToken, Xbr72RedisService#removeRefreshToken, this#expireCookie, this#handleLogoutSuccess
- `Xxx20`
  - `getCurrentTrackingStatus` → this#parseStatusFromHtml
- `Xxz63Filter`
  - `doFilterInternal` → this#isLoginRequest, this#extractLoginRequest, this#handleLogin, this#handleLoginError
  - `handleLogin` → this#getClientIp, Xbr72Service#login, this#setCookie, this#handleLoginSuccess

## 3. 클래스 목록 (역할별)

### controller (1)

- `Xgk12Controller` global/controller/Xgk12Controller.java · public: getAvailableLogFiles, getLogContent

### service (1)

- `Xgp51RedisService` global/security/redis/Xgp51RedisService.java · public: isIpAllowed, isIpBlocked, blockIp, unblockIp, getCurrentRequestCount, getBlockRemainingTime · 주입: RedisTemplate

### entity (2)

- `BaseEntity` (abstract class) global/entity/BaseEntity.java · extends/implements Xwb79Entity
- `Xwb79Entity` (abstract class) global/entity/Xwb79Entity.java

### config (29)

- `CacheConfig` global/config/CacheConfig.java · public: cacheManager
- `RedisConfig` global/config/RedisConfig.java · public: redisConnectionFactory, primaryRedisConnectionFactory, replicaRedisConnectionFactory, redisTemplate, stringRedisTemplate, writeRedisTemplate, readRedisTemplate
- `WebConfig` global/config/WebConfig.java · extends/implements WebMvcConfigurer · public: addArgumentResolvers
- `Xct70Config` global/config/Xct70Config.java · public: jobRepository
- `Xdx96Config` global/config/Xdx96Config.java · public: openAPI
- `Xgd66Config` global/config/Xgd66Config.java · public: getApiKey, getModel, getApiUrl
- `Xnp67` global/config/Xnp67.java · extends/implements CommandLineRunner · public: run, createStylesForUserWithTransaction · 주입: Xrd81Repository, Xbn78CommandService, PasswordEncoder, Xpk42Repository, Xbx83Repository, Xnf79Repository, Xkh73Repository, Xfw75Repository, Xtx29Repository, Xkr46Repository, Xfp92Repository, Xdh20Repository, Xzq99Repository, Xzj27Repository, Xzz58Repository, Xdg64Repository, Xgg74Repository, Xhm58Repository, Xxn30Repository, Xnn94Repository, Xbf54CommandService, Xhx14CommandService, Xnn94CommandService, Xbf54Repository, Xms98Repository, Xbr34Repository, Xcr44Repository, Xsc53Repository, Xdw14Repository, Xbn78Repository, Xtf80Util
- `Xnp72Config` global/config/Xnp72Config.java · public: logDirectoryPath, corsConfigurationSource
- `Xpb98BatchScheduler` global/config/Xpb98BatchScheduler.java · public: scheduleShipmentStatusJob · 주입: JobLauncher, Job
- `Xsd33Config` global/config/Xsd33Config.java · public: jpaQueryFactory · 주입: EntityManager
- `Xwp93` global/config/Xwp93.java · public: resolveUserDestination · 주입: SimpUserRegistry
- `Xxm22TemplateConfig` global/config/Xxm22TemplateConfig.java · public: restTemplate
- `Xxm98Config` global/config/Xxm98Config.java · public: shutdownHook · 주입: Xgp60
- `Xtf80Aspect` global/config/aop/Xtf80Aspect.java
- `Xbf54KafkaConfig` global/config/kafka/Xbf54KafkaConfig.java · public: paymentEventProducerFactory, paymentEventKafkaTemplate, paymentEventConsumerFactory, paymentEventKafkaListenerContainerFactory
- `Xdg64KafkaConfig` global/config/kafka/Xdg64KafkaConfig.java · public: orderProcessingEventProducerFactory, orderProcessingEventKafkaTemplate, orderProcessingEventConsumerFactory, orderProcessingKafkaListenerContainerFactory, notificationProducerFactory, notificationKafkaTemplate
- `Xvh65KafkaConfig` global/config/kafka/Xvh65KafkaConfig.java · public: userAccessLogProducerFactory, userAccessLogKafkaTemplate, userAccessLogConsumerFactory, userAccessLogKafkaListenerContainerFactory
- `SecurityConfig` global/config/security/SecurityConfig.java · public: securityFilterChain, authenticationManager · 주입: Xsc93Provider, Xbr72RedisService, Xgp51RedisService, Xpj20Service, Xft86Handler, Xrd81Repository, PasswordEncoder, Xbr72Service
- `Xsc93Provider` global/config/security/Xsc93Provider.java · public: generateTokenPair, getRoleFromToken, validateToken, getEmailFromToken · 주입: Xbr72RedisService
- `Xxb74Filter` global/config/security/Xxb74Filter.java · extends/implements OncePerRequestFilter · 주입: Xsc93Provider, Xbr72RedisService
- `Xzt60Config` global/config/security/Xzt60Config.java · public: passwordEncoder
- `Xfx15Dto` global/config/security/dto/Xfx15Dto.java
- `Xkx62Filter` global/config/security/filter/Xkx62Filter.java · extends/implements OncePerRequestFilter · 주입: Xsc93Provider, Xbr72RedisService, Xrd81Repository
- `Xxt21Filter` global/config/security/filter/Xxt21Filter.java · extends/implements OncePerRequestFilter · 주입: Xbr72RedisService, Xsc93Provider
- `Xxz63Filter` global/config/security/filter/Xxz63Filter.java · extends/implements OncePerRequestFilter · 주입: Xbr72Service
- `Xfj19Service` global/config/security/service/Xfj19Service.java · public: authenticateUser, refreshAccessToken, logout, validateUser · 주입: Xrd81Repository, PasswordEncoder, Xsc93Provider, Xbr72RedisService
- `WebSocketConfig` global/config/websocket/WebSocketConfig.java · extends/implements WebSocketMessageBrokerConfigurer · public: configureMessageBroker, registerStompEndpoints · 주입: Xrd81Repository, Xsc93Provider, RedisTemplate
- `Xqs35Interceptor` global/config/websocket/Xqs35Interceptor.java · extends/implements HandshakeInterceptor · public: beforeHandshake, afterHandshake · 주입: Xrd81Repository, Xsc93Provider, RedisTemplate
- `Xxx20` global/utils/Xxx20.java · public: getCurrentTrackingStatus

### dto (5)

- `ResponseDto` global/dto/ResponseDto.java
- `Xcm11DTO` global/dto/Xcm11DTO.java
- `Xgk12ResponseDTO` global/dto/Xgk12ResponseDTO.java
- `Xmx58DTO` global/dto/Xmx58DTO.java
- `commonDto` global/dto/commonDto.java

### exception (26)

- `ErrorCode` global/exception/ErrorCode.java
- `Xfh56RequestException` global/exception/Xfh56RequestException.java
- `Xgh87Exception` global/exception/Xgh87Exception.java
- `Xks43ErrorCode` global/exception/Xks43ErrorCode.java
- `Xks43Exception` global/exception/Xks43Exception.java
- `Xks43ExceptionHandler` global/exception/Xks43ExceptionHandler.java
- `Xdg50Exception` global/exception/file/Xdg50Exception.java
- `Xgw12Exception` global/exception/file/Xgw12Exception.java
- `Xnr13TypeException` global/exception/file/Xnr13TypeException.java
- `Xpz63ErrorCode` global/exception/file/Xpz63ErrorCode.java
- `Xpz63Exception` global/exception/file/Xpz63Exception.java
- `Xtp86Exception` global/exception/file/Xtp86Exception.java
- `Xxb18Exception` global/exception/file/Xxb18Exception.java
- `SecurityErrorCode` global/exception/security/SecurityErrorCode.java
- `SecurityException` global/exception/security/SecurityException.java
- `Xdc78Exception` global/exception/security/Xdc78Exception.java
- `Xdr94Exception` global/exception/security/Xdr94Exception.java
- `Xkt72Exception` global/exception/security/Xkt72Exception.java
- `Xrg22Exception` global/exception/security/Xrg22Exception.java
- `Xwm26Exception` global/exception/security/Xwm26Exception.java
- `Xxt33Exception` global/exception/security/Xxt33Exception.java
- `WebSocketErrorCode` global/exception/websocket/WebSocketErrorCode.java
- `WebSocketException` global/exception/websocket/WebSocketException.java
- `Xbb59Exception` global/exception/websocket/Xbb59Exception.java
- `Xfw87Exception` global/exception/websocket/Xfw87Exception.java
- `Xtb29Exception` global/exception/websocket/Xtb29Exception.java

### other (9)

- `FreamBackApplication` FreamBackApplication.java · public: main
- `ErrorResponse` global/response/ErrorResponse.java · public: of
- `Xgp51Filter` global/security/filter/Xgp51Filter.java · extends/implements OncePerRequestFilter · 주입: Xgp51RedisService
- `PageUtils` global/utils/PageUtils.java · public: toPageDto
- `SecurityUtils` global/utils/SecurityUtils.java · public: extractEmailFromSecurityContext, extractEmailOrAnonymous, extractUserInfo, extractAndValidateEmailForOrderBid, extractAndValidateEmailForOrder
- `Xgz45Util` global/utils/Xgz45Util.java · public: purgeProductCache, purgeEsCache, purgeStyleCache
- `Xpz63Utils` global/utils/Xpz63Utils.java · public: existsFile, saveFile, deleteFile, getFileUrl, deleteDirectory, isFileExist, saveMediaFile
- `Xrk74` global/utils/Xrk74.java · public: openBrowser, closeBrowser, newPage, isInitialized
- `Xtf80Util` global/utils/Xtf80Util.java · public: encrypt, decrypt, deterministicEncrypt, deterministicDecrypt, encryptForSearch, isEncrypted

### 다른 도메인 참조 (import·주입 기준)

- **m02**: `Xbj22Type` ← Xnp67; `Xdh20Repository` ← Xnp67; `Xfp92Repository` ← Xnp67; `Xfw75Repository` ← Xnp67; `Xhz82Type` ← Xnp67; `Xkr46Repository` ← Xnp67; `Xnp24Type` ← Xnp67; `Xtx29Repository` ← Xnp67; `Xzq99Repository` ← Xnp67
- **m03**: `Xcr44` ← Xnp67; `Xcr44Repository` ← Xnp67; `Xdw14` ← Xnp67; `Xdw14Repository` ← Xnp67; `Xgp60` ← Xxm98Config; `Xsc53` ← Xnp67; `Xsc53Repository` ← Xnp67
- **m06**: `Xbf54CommandService` ← Xnp67; `Xbf54Event` ← Xbf54KafkaConfig; `Xbf54Repository` ← Xnp67; `Xbf54Status` ← Xnp67; `Xjt86` ← Xnp67
- **m07**: `Xnn94` ← Xnp67; `Xnn94CommandService` ← Xnp67; `Xnn94Repository` ← Xnp67
- **m10**: `Xgg74` ← Xnp67; `Xgg74Repository` ← Xnp67; `Xgg74Status` ← Xnp67; `Xzz58` ← Xnp67; `Xzz58Repository` ← Xnp67
- **m11**: `Xms98` ← Xnp67; `Xms98Repository` ← Xnp67
- **m12**: `Xhx14` ← Xnp67; `Xhx14CommandService` ← Xnp67; `Xpb98Status` ← Xnp67; `Xxn30` ← Xnp67; `Xxn30Repository` ← Xnp67
- **m13**: `Xbj22` ← Xsc93Provider, Xxb74Filter; `Xbn78CommandService` ← Xnp67; `Xbn78Repository` ← Xnp67; `Xbr34Repository` ← Xnp67; `Xbr72RedisService` ← SecurityConfig, Xfj19Service, Xkx62Filter, Xsc93Provider, Xxb74Filter, Xxt21Filter; `Xbr72Service` ← SecurityConfig, Xxz63Filter; `Xpj20Service` ← SecurityConfig; `Xrd81` ← Xfj19Service, Xkx62Filter; `Xrd81Repository` ← SecurityConfig, WebSocketConfig, Xfj19Service, Xkx62Filter, Xnp67, Xqs35Interceptor; `Xsv82` ← Xsc93Provider
- **m14**: `Xhv12` ← Xnp67; `Xnf79` ← Xnp67; `Xnf79Repository` ← Xnp67
- **m15**: `Xkh73` ← Xnp67; `Xkh73Repository` ← Xnp67; `Xkh73Type` ← Xnp67; `Xzv64` ← Xnp67
- **m16**: `Xdg64Repository` ← Xnp67; `Xhm58Repository` ← Xnp67; `Xmr97Event` ← Xdg64KafkaConfig; `Xzj27Repository` ← Xnp67
- **m17**: `Xkc19` ← Xnp67; `Xpk42` ← Xnp67; `Xpk42Repository` ← Xnp67
- **m19**: `Xbx83` ← Xnp67; `Xbx83Repository` ← Xnp67; `Xcx38` ← Xnp67

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

- `BaseEntity` @MappedSuperclass · extends Xwb79Entity
- `Xwb79Entity` @MappedSuperclass

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

