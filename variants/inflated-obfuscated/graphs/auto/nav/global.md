# global (global 패키지와 루트)

기준 경로: `src/main/java/com/fream/back/` · 파일 74개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

타입 선언이 없는 파일(파싱 결과 클래스 0개): `src/main/java/com/fream/back/global/config/Xkn13Config.java`

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| GET | `/api/logs` | `Xqv72Controller#getAvailableLogFiles` |  |
| GET | `/api/logs/{fileName}` | `Xqv72Controller#getLogContent` | `this#readLogFileWithPaging` |

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
- `Xdh20Service`
  - `authenticateUser` → Xjm45Repository#findByEmail, Xxk70Provider#generateTokenPair
  - `refreshAccessToken` → Xxk70Provider#validateToken, Xpk42RedisService#isRefreshTokenValid, Xxk70Provider#getEmailFromToken, Xjm45Repository#findByEmail, Xxk70Provider#generateTokenPair
  - `logout` → Xpk42RedisService#removeAccessToken, Xpk42RedisService#removeRefreshToken
  - `validateUser` → Xjm45Repository#findByEmail
- `Xdq35`
  - `run` → this#createUserWithProfile, this#createAddress, this#createBankAccount, this#createProductData, this#createNoticeData, this#createFAQData, this#createInspectionStandardData, this#createNotificationData, Xcc44Repository#findAll~, this#createOrderBid, this#createSaleBid, Xtw92Repository#findAll~, this#createSale, this#linkOrderBidToSale, this#createSellerShipment, Xzt63Repository#findAll~, this#processPurchaseAndShipment, this#createStyleData
  - `createUserWithProfile` → Xjm45Repository#existsByEmail, Xjm45Repository#findByEmail, this#generateReferralCode, Xjm45Repository#save~, Xwt67CommandService#createDefaultProfile
  - `createNoticeData` → Xnx58Repository#save~
  - `createFAQData` → Xfr38Repository#save~
  - `createInspectionStandardData` → Xqr84Repository#save~
  - `createNotificationData` → Xbp24Repository#save~
  - `createProductData` → Xft86Repository#save~, Xzj27Repository#save~, Xnr50Repository#save~, Xvm17Repository#save~, Xdt35Repository#save~, Xcc44Repository#save~
  - `createOrderBid` → this#createOrder, Xtw92Repository#save~
  - `createOrder` → Xhq12Repository#save~
  - `createSaleBid` → Xpj42Repository#save~, Xzt63Repository#save~
  - `createSale` → Xpj42Repository#save~
  - `linkOrderBidToSale` → Xtw92Repository#save~
  - `createSellerShipment` → Xrg51Repository#save~
  - `processPurchaseAndShipment` → Xhq12Repository#save~, Xnd96Repository#save~, Xtw92Repository#save~, Xjt45Repository#save~, Xfr60CommandService#createOrderShipment, Xms75CommandService#createOrderStorage, Xzt63Repository#save~
  - `createAddress` → Xhr91Util#deterministicEncrypt, Xhr91Util#encrypt, Xms98Repository#save~
  - `createBankAccount` → Xfs39Repository#save~
  - `createStyleData` → Xhq12Repository#findByUserAndStatus, Xnd96Repository#findByOrderIn, Xcc44Repository#findById~, Xvm17Repository#findById~, Xwt67Repository#findByUser, this#createStylesForUserWithTransaction
  - `createStylesForUserWithTransaction` → Xwt67Repository#findById~, Xnd96Repository#findById~, Xnr50Repository#findById~, Xft86Repository#findById~, Xhr53Repository#save~, Xrw15Repository#save~, Xmp75Repository#save~, Xhr53Repository#saveAndFlush~
- `Xhq12KafkaConfig`
  - `orderProcessingEventKafkaTemplate` → this#orderProcessingEventProducerFactory
  - `orderProcessingKafkaListenerContainerFactory` → this#orderProcessingEventConsumerFactory
  - `notificationKafkaTemplate` → this#notificationProducerFactory
- `Xhr91Util`
  - `deterministicEncrypt` → this#generateDeterministicKeyBytes
  - `encryptForSearch` → this#deterministicEncrypt
- `Xjt45KafkaConfig`
  - `paymentEventKafkaTemplate` → this#paymentEventProducerFactory
  - `paymentEventKafkaListenerContainerFactory` → this#paymentEventConsumerFactory
- `Xkh22`
  - `openBrowser` → this#closeBrowser
  - `newPage` → this#openBrowser
- `Xpw46Filter`
  - `doFilterInternal` → this#isLogoutRequest, this#handleLogout, this#handleLogoutError
  - `handleLogout` → this#getCookieValue, Xxk70Provider#validateToken, Xxk70Provider#getEmailFromToken, Xpk42RedisService#removeAccessToken, Xpk42RedisService#removeRefreshToken, this#expireCookie, this#handleLogoutSuccess
- `Xqw80TemplateConfig`
  - `restTemplate` → this#loggingInterceptor
- `Xrp33KafkaConfig`
  - `userAccessLogKafkaTemplate` → this#userAccessLogProducerFactory
  - `userAccessLogKafkaListenerContainerFactory` → this#userAccessLogConsumerFactory
- `Xrs89Filter`
  - `doFilterInternal` → this#getClientIp, this#isLocalhost, Xrs89RedisService#isIpAllowed, this#handleBlockedIp
  - `handleBlockedIp` → Xrs89RedisService#getBlockRemainingTime
- `Xrs89RedisService`
  - `isIpAllowed` → this#isIpBlocked, this#blockIp
- `Xsc81Filter`
  - `doFilterInternal` → this#getCookieValue, Xxk70Provider#validateToken, Xpk42RedisService#isAccessTokenValid, Xxk70Provider#getEmailFromToken, Xpk42RedisService#getAgeByAccessToken, Xpk42RedisService#getGenderByAccessToken
- `Xtv68Filter`
  - `doFilterInternal` → this#isLoginRequest, this#extractLoginRequest, this#handleLogin, this#handleLoginError
  - `handleLogin` → this#getClientIp, Xpk42Service#login, this#setCookie, this#handleLoginSuccess
- `Xvj80Filter`
  - `doFilterInternal` → this#isRefreshRequest, this#handleTokenRefresh, this#handleRefreshError
  - `handleTokenRefresh` → this#getCookieValue, Xxk70Provider#validateToken, Xpk42RedisService#isRefreshTokenValid, Xxk70Provider#getEmailFromToken, Xjm45Repository#findByEmail, this#getClientIp, Xxk70Provider#generateTokenPair, this#setCookie, this#handleRefreshSuccess
- `Xvp92Interceptor`
  - `beforeHandshake` → this#extractAccessToken, Xxk70Provider#validateToken, Xxk70Provider#getEmailFromToken, Xjm45Repository#findByEmail
- `Xwg86`
  - `getCurrentTrackingStatus` → this#parseStatusFromHtml
- `Xxf23Config`
  - `shutdownHook` → Xrg71#flushBufferOnShutdown
- `Xxk70Provider`
  - `generateTokenPair` → Xpk42RedisService#addAccessToken, Xpk42RedisService#addRefreshToken
- `Xzq99Utils`
  - `saveMediaFile` → this#saveFile

## 3. 클래스 목록 (역할별)

### controller (1)

- `Xqv72Controller` global/controller/Xqv72Controller.java · public: getAvailableLogFiles, getLogContent

### service (1)

- `Xrs89RedisService` global/security/redis/Xrs89RedisService.java · public: isIpAllowed, isIpBlocked, blockIp, unblockIp, getCurrentRequestCount, getBlockRemainingTime · 주입: RedisTemplate

### entity (2)

- `BaseEntity` (abstract class) global/entity/BaseEntity.java · extends/implements Xzh80Entity
- `Xzh80Entity` (abstract class) global/entity/Xzh80Entity.java

### config (29)

- `CacheConfig` global/config/CacheConfig.java · public: cacheManager
- `RedisConfig` global/config/RedisConfig.java · public: redisConnectionFactory, primaryRedisConnectionFactory, replicaRedisConnectionFactory, redisTemplate, stringRedisTemplate, writeRedisTemplate, readRedisTemplate
- `WebConfig` global/config/WebConfig.java · extends/implements WebMvcConfigurer · public: addArgumentResolvers
- `Xdq35` global/config/Xdq35.java · extends/implements CommandLineRunner · public: run, createStylesForUserWithTransaction · 주입: Xjm45Repository, Xwt67CommandService, PasswordEncoder, Xnx58Repository, Xfr38Repository, Xqr84Repository, Xbp24Repository, Xft86Repository, Xzj27Repository, Xnr50Repository, Xvm17Repository, Xcc44Repository, Xdt35Repository, Xtw92Repository, Xzt63Repository, Xhq12Repository, Xpj42Repository, Xnd96Repository, Xrg51Repository, Xms75Repository, Xjt45CommandService, Xfr60CommandService, Xms75CommandService, Xjt45Repository, Xms98Repository, Xfs39Repository, Xhr53Repository, Xrw15Repository, Xmp75Repository, Xwt67Repository, Xhr91Util
- `Xff76Config` global/config/Xff76Config.java · public: jobRepository
- `Xkx75` global/config/Xkx75.java · public: resolveUserDestination · 주입: SimpUserRegistry
- `Xpd12Config` global/config/Xpd12Config.java · public: getApiKey, getModel, getApiUrl
- `Xpd58Config` global/config/Xpd58Config.java · public: logDirectoryPath, corsConfigurationSource
- `Xqw80TemplateConfig` global/config/Xqw80TemplateConfig.java · public: restTemplate
- `Xss98Config` global/config/Xss98Config.java · public: openAPI
- `Xxf23Config` global/config/Xxf23Config.java · public: shutdownHook · 주입: Xrg71
- `Xzj77Config` global/config/Xzj77Config.java · public: jpaQueryFactory · 주입: EntityManager
- `Xzn53BatchScheduler` global/config/Xzn53BatchScheduler.java · public: scheduleShipmentStatusJob · 주입: JobLauncher, Job
- `Xhr91Aspect` global/config/aop/Xhr91Aspect.java
- `Xhq12KafkaConfig` global/config/kafka/Xhq12KafkaConfig.java · public: orderProcessingEventProducerFactory, orderProcessingEventKafkaTemplate, orderProcessingEventConsumerFactory, orderProcessingKafkaListenerContainerFactory, notificationProducerFactory, notificationKafkaTemplate
- `Xjt45KafkaConfig` global/config/kafka/Xjt45KafkaConfig.java · public: paymentEventProducerFactory, paymentEventKafkaTemplate, paymentEventConsumerFactory, paymentEventKafkaListenerContainerFactory
- `Xrp33KafkaConfig` global/config/kafka/Xrp33KafkaConfig.java · public: userAccessLogProducerFactory, userAccessLogKafkaTemplate, userAccessLogConsumerFactory, userAccessLogKafkaListenerContainerFactory
- `SecurityConfig` global/config/security/SecurityConfig.java · public: securityFilterChain, authenticationManager · 주입: Xxk70Provider, Xpk42RedisService, Xrs89RedisService, Xhx14Service, Xtn16Handler, Xjm45Repository, PasswordEncoder, Xpk42Service
- `Xsc81Filter` global/config/security/Xsc81Filter.java · extends/implements OncePerRequestFilter · 주입: Xxk70Provider, Xpk42RedisService
- `Xvt76Config` global/config/security/Xvt76Config.java · public: passwordEncoder
- `Xxk70Provider` global/config/security/Xxk70Provider.java · public: generateTokenPair, getRoleFromToken, validateToken, getEmailFromToken · 주입: Xpk42RedisService
- `Xww28Dto` global/config/security/dto/Xww28Dto.java
- `Xpw46Filter` global/config/security/filter/Xpw46Filter.java · extends/implements OncePerRequestFilter · 주입: Xpk42RedisService, Xxk70Provider
- `Xtv68Filter` global/config/security/filter/Xtv68Filter.java · extends/implements OncePerRequestFilter · 주입: Xpk42Service
- `Xvj80Filter` global/config/security/filter/Xvj80Filter.java · extends/implements OncePerRequestFilter · 주입: Xxk70Provider, Xpk42RedisService, Xjm45Repository
- `Xdh20Service` global/config/security/service/Xdh20Service.java · public: authenticateUser, refreshAccessToken, logout, validateUser · 주입: Xjm45Repository, PasswordEncoder, Xxk70Provider, Xpk42RedisService
- `WebSocketConfig` global/config/websocket/WebSocketConfig.java · extends/implements WebSocketMessageBrokerConfigurer · public: configureMessageBroker, registerStompEndpoints · 주입: Xjm45Repository, Xxk70Provider, RedisTemplate
- `Xvp92Interceptor` global/config/websocket/Xvp92Interceptor.java · extends/implements HandshakeInterceptor · public: beforeHandshake, afterHandshake · 주입: Xjm45Repository, Xxk70Provider, RedisTemplate
- `Xwg86` global/utils/Xwg86.java · public: getCurrentTrackingStatus

### dto (5)

- `ResponseDto` global/dto/ResponseDto.java
- `Xmg87DTO` global/dto/Xmg87DTO.java
- `Xqv72ResponseDTO` global/dto/Xqv72ResponseDTO.java
- `Xvs90DTO` global/dto/Xvs90DTO.java
- `commonDto` global/dto/commonDto.java

### exception (26)

- `ErrorCode` global/exception/ErrorCode.java
- `Xdf64Exception` global/exception/Xdf64Exception.java
- `Xkt44ErrorCode` global/exception/Xkt44ErrorCode.java
- `Xkt44Exception` global/exception/Xkt44Exception.java
- `Xkt44ExceptionHandler` global/exception/Xkt44ExceptionHandler.java
- `Xkx40RequestException` global/exception/Xkx40RequestException.java
- `Xcs83Exception` global/exception/file/Xcs83Exception.java
- `Xkj24Exception` global/exception/file/Xkj24Exception.java
- `Xmq55Exception` global/exception/file/Xmq55Exception.java
- `Xrp21Exception` global/exception/file/Xrp21Exception.java
- `Xwt13TypeException` global/exception/file/Xwt13TypeException.java
- `Xzq99ErrorCode` global/exception/file/Xzq99ErrorCode.java
- `Xzq99Exception` global/exception/file/Xzq99Exception.java
- `SecurityErrorCode` global/exception/security/SecurityErrorCode.java
- `SecurityException` global/exception/security/SecurityException.java
- `Xbz64Exception` global/exception/security/Xbz64Exception.java
- `Xkr85Exception` global/exception/security/Xkr85Exception.java
- `Xnb37Exception` global/exception/security/Xnb37Exception.java
- `Xrs33Exception` global/exception/security/Xrs33Exception.java
- `Xwq85Exception` global/exception/security/Xwq85Exception.java
- `Xxv33Exception` global/exception/security/Xxv33Exception.java
- `WebSocketErrorCode` global/exception/websocket/WebSocketErrorCode.java
- `WebSocketException` global/exception/websocket/WebSocketException.java
- `Xqb30Exception` global/exception/websocket/Xqb30Exception.java
- `Xqx11Exception` global/exception/websocket/Xqx11Exception.java
- `Xtd49Exception` global/exception/websocket/Xtd49Exception.java

### other (9)

- `FreamBackApplication` FreamBackApplication.java · public: main
- `ErrorResponse` global/response/ErrorResponse.java · public: of
- `Xrs89Filter` global/security/filter/Xrs89Filter.java · extends/implements OncePerRequestFilter · 주입: Xrs89RedisService
- `PageUtils` global/utils/PageUtils.java · public: toPageDto
- `SecurityUtils` global/utils/SecurityUtils.java · public: extractEmailFromSecurityContext, extractEmailOrAnonymous, extractUserInfo, extractAndValidateEmailForOrderBid, extractAndValidateEmailForOrder
- `Xbh78Util` global/utils/Xbh78Util.java · public: purgeProductCache, purgeEsCache, purgeStyleCache
- `Xhr91Util` global/utils/Xhr91Util.java · public: encrypt, decrypt, deterministicEncrypt, deterministicDecrypt, encryptForSearch, isEncrypted
- `Xkh22` global/utils/Xkh22.java · public: openBrowser, closeBrowser, newPage, isInitialized
- `Xzq99Utils` global/utils/Xzq99Utils.java · public: existsFile, saveFile, deleteFile, getFileUrl, deleteDirectory, isFileExist, saveMediaFile

### 다른 도메인 참조 (import·주입 기준)

- **m08**: `Xfr38` ← Xdq35; `Xfr38Repository` ← Xdq35; `Xkr46` ← Xdq35
- **m16**: `Xnx58` ← Xdq35; `Xnx58Repository` ← Xdq35; `Xwm58` ← Xdq35
- **m22**: `Xdg35` ← Xsc81Filter, Xxk70Provider; `Xfs39Repository` ← Xdq35; `Xhx14Service` ← SecurityConfig; `Xjm45` ← Xdh20Service, Xvj80Filter; `Xjm45Repository` ← SecurityConfig, WebSocketConfig, Xdh20Service, Xdq35, Xvj80Filter, Xvp92Interceptor; `Xpk42RedisService` ← SecurityConfig, Xdh20Service, Xpw46Filter, Xsc81Filter, Xvj80Filter, Xxk70Provider; `Xpk42Service` ← SecurityConfig, Xtv68Filter; `Xqj67` ← Xxk70Provider; `Xwt67CommandService` ← Xdq35; `Xwt67Repository` ← Xdq35
- **m30**: `Xcc44Repository` ← Xdq35; `Xcn23Type` ← Xdq35; `Xdg35Type` ← Xdq35; `Xdt35Repository` ← Xdq35; `Xft86Repository` ← Xdq35; `Xks61Type` ← Xdq35; `Xnr50Repository` ← Xdq35; `Xvm17Repository` ← Xdq35; `Xzj27Repository` ← Xdq35
- **m37**: `Xpn74` ← Xdq35; `Xqr84` ← Xdq35; `Xqr84Repository` ← Xdq35
- **m42**: `Xhr53` ← Xdq35; `Xhr53Repository` ← Xdq35; `Xmp75` ← Xdq35; `Xmp75Repository` ← Xdq35; `Xrg71` ← Xxf23Config; `Xrw15` ← Xdq35; `Xrw15Repository` ← Xdq35
- **m45**: `Xfr60` ← Xdq35; `Xfr60CommandService` ← Xdq35; `Xrg51` ← Xdq35; `Xrg51Repository` ← Xdq35; `Xzn53Status` ← Xdq35
- **m52**: `Xbp24` ← Xdq35; `Xbp24Repository` ← Xdq35; `Xbp24Type` ← Xdq35; `Xrv73` ← Xdq35
- **m53**: `Xms98` ← Xdq35; `Xms98Repository` ← Xdq35
- **m63**: `Xms75` ← Xdq35; `Xms75CommandService` ← Xdq35; `Xms75Repository` ← Xdq35
- **m64**: `Xgg74` ← Xdq35; `Xjt45CommandService` ← Xdq35; `Xjt45Event` ← Xjt45KafkaConfig; `Xjt45Repository` ← Xdq35; `Xjt45Status` ← Xdq35
- **m85**: `Xpj42` ← Xdq35; `Xpj42Repository` ← Xdq35; `Xpj42Status` ← Xdq35; `Xzt63` ← Xdq35; `Xzt63Repository` ← Xdq35
- **m99**: `Xhq12Repository` ← Xdq35; `Xnd96Repository` ← Xdq35; `Xrb42Event` ← Xhq12KafkaConfig; `Xtw92Repository` ← Xdq35

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

- `BaseEntity` @MappedSuperclass · extends Xzh80Entity
- `Xzh80Entity` @MappedSuperclass

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

