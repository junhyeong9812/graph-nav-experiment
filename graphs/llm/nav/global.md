# global 패키지 네비게이션 (`src/main/java/com/fream/back/global`)

> 공통 인프라 코드. 모든 경로는 repo 루트 기준. `G/` = `src/main/java/com/fream/back/global/` 약어.
> 엔트리포인트: `src/main/java/com/fream/back/FreamBackApplication.java` (`@SpringBootApplication` 만 있음. `@EnableJpaAuditing` 없음, `@EnableScheduling`은 도메인 클래스들에 붙어 있음).

## 1. 파일 목록 (서브패키지별)

### config/ (일반 설정)
| 파일 | 역할 |
|---|---|
| `G/config/ApplicationShutdownConfig.java` | `ContextClosedEvent` 리스너: 종료 시 `StyleViewLogBufferManager.flushBufferOnShutdown()` 호출 |
| `G/config/BatchInfraConfig.java` | `@EnableBatchProcessing`, `JobRepository` 수동 생성 (DatabaseType=MYSQL) |
| `G/config/CacheConfig.java` | `@EnableCaching`, `ConcurrentMapCacheManager` (인메모리, TTL 없음) |
| `G/config/DataInitializer.java` | `CommandLineRunner` 시드 데이터 생성 (§11) |
| `G/config/EmailBasedUserDestinationResolver.java` | SecurityContext 이메일로 `/user/{email}{dest}` STOMP 목적지 문자열 생성 |
| `G/config/GPTConfig.java` | `openai.api.key`, `openai.model`(기본 gpt-3.5-turbo), `openai.api.url` 값 보관 |
| `G/config/LogViewerConfig.java` | `@EnableMethodSecurity`, `logDirectoryPath` 빈(`logging.file.path`, 기본 `/home/ubuntu/springlog`), `/api/logs/**` 용 `CorsConfigurationSource` 빈 |
| `G/config/QueryDslConfig.java` | `JPAQueryFactory` 빈 |
| `G/config/RedisConfig.java` | Redis Primary/Replica 커넥션 팩토리 + 템플릿 (§7). 파일 상단에 구버전 전체가 주석으로 남아 있음 |
| `G/config/RestTemplateConfig.java` | `RestTemplate` 빈 (HttpComponents, connect 5s / read 10s, debug 로깅 인터셉터) |
| `G/config/ShipmentBatchScheduler.java` | `@Scheduled(cron="0 0 */6 * * *")` → `updateShipmentStatusesJob` 실행 (Job 정의: `src/main/java/com/fream/back/domain/shipment/config/UpdateShipmentStatusesJobConfig.java`) |
| `G/config/SwaggerConfig.java` | springdoc `OpenAPI` 빈 (§12) |
| `G/config/WebConfig.java` | `PageableHandlerMethodArgumentResolver`: 1-based 페이지, fallback `PageRequest.of(0,10)`, max 2000 |
| `G/config/WebMvcConfig.java` | **전체 주석 처리** (과거 CORS 설정) |
| `G/config/aop/PersonalDataEncryptionAspect.java` | **빈 클래스** (어노테이션/로직 없음, 4줄) |

### config/kafka/
| 파일 | 역할 |
|---|---|
| `G/config/kafka/OrderKafkaConfig.java` | `OrderProcessingEvent` producer/consumer + 알림용 `Map` producer |
| `G/config/kafka/PaymentKafkaConfig.java` | `PaymentEvent` producer/consumer |
| `G/config/kafka/UserAccessLogKafkaConfig.java` | `UserAccessLogEvent` producer/consumer |

### config/security/
| 파일 | 역할 |
|---|---|
| `G/config/security/SecurityConfig.java` | `SecurityFilterChain` (§2) |
| `G/config/security/JwtTokenProvider.java` | JWT 생성/검증 (§3) |
| `G/config/security/JwtAuthenticationFilter.java` | 쿠키 `ACCESS_TOKEN` → SecurityContext (§3). 내부 static class `UserInfo(age, gender)` |
| `G/config/security/EncoderConfig.java` | `BCryptPasswordEncoder` 빈 |
| `G/config/security/dto/TokenDto.java` | `accessToken`, `refreshToken` |
| `G/config/security/filter/LoginAuthenticationFilter.java` | `POST /auth/login` 가로채서 `AuthService.login()` → 쿠키 설정 |
| `G/config/security/filter/LogoutAuthenticationFilter.java` | `POST /auth/logout` 가로채서 Redis 토큰 제거 + 쿠키 만료 |
| `G/config/security/filter/TokenRefreshFilter.java` | `POST /auth/refresh` 가로채서 새 토큰쌍 발급 |
| `G/config/security/service/FilterAuthService.java` | 필터용 인증 서비스(`authenticateUser`, `refreshAccessToken`, `logout`, `validateUser`). **현재 필터들은 이 클래스를 사용하지 않음** (Login 필터는 `domain.user.service.command.AuthService` 사용) |

### config/websocket/
| 파일 | 역할 |
|---|---|
| `G/config/websocket/WebSocketConfig.java` | STOMP 브로커 설정 (§8) |
| `G/config/websocket/WebSocketAuthInterceptor.java` | 핸드셰이크 인증 (쿠키 `ACCESS_TOKEN`) + Redis `WebSocket:User:{email}` TTL 관리 |

### controller/, dto/, entity/, response/
| 파일 | 역할 |
|---|---|
| `G/controller/LogController.java` | 서버 로그 파일 뷰어 API (§13) |
| `G/dto/LogFileDTO.java`, `G/dto/LogLineDTO.java`, `G/dto/LogResponseDTO.java` | LogController 응답 DTO |
| `G/dto/ResponseDto.java` | 공통 성공/실패 래퍼 (§5) |
| `G/dto/commonDto.java` | 내부 static `PageDto<T>` (content, totalElements, totalPages, page, size) |
| `G/entity/BaseTimeEntity.java` | `createdDate`, `modifiedDate` (§6) |
| `G/entity/BaseEntity.java` | BaseTimeEntity + `createdBy`, `modifiedBy` (§6) |
| `G/response/ErrorResponse.java` | 예외 응답 바디 (§4) |

### exception/
| 파일 | 역할 |
|---|---|
| `G/exception/ErrorCode.java` | 인터페이스: `getCode()`, `getMessage()`, `getStatus()`(int) |
| `G/exception/GlobalException.java` | abstract `RuntimeException`, 필드 `errorCode`. 생성자 4종 (code / code+msg / code+cause / code+msg+cause) |
| `G/exception/GlobalErrorCode.java` | 공통 코드 G001~G031 |
| `G/exception/GlobalExceptionHandler.java` | `@RestControllerAdvice` (§4) |
| `G/exception/EntityNotFoundException.java` | GlobalException, `RESOURCE_NOT_FOUND` |
| `G/exception/InvalidRequestException.java` | GlobalException, `INVALID_INPUT_VALUE` |
| `G/exception/file/FileErrorCode.java` | F001~F005 |
| `G/exception/file/FileException.java` | GlobalException 하위 기반 클래스 |
| `G/exception/file/{DirectoryCreationException, FileDownloadException, FileNotFoundException, FileUploadException, UnsupportedFileTypeException}.java` | FileException 하위 |
| `G/exception/security/SecurityErrorCode.java` | S001~S006 |
| `G/exception/security/SecurityException.java` | GlobalException 하위 기반 클래스 (java.lang.SecurityException 아님 주의) |
| `G/exception/security/{ExpiredTokenException, InvalidRefreshTokenException, InvalidTokenException, SecurityUserNotFoundException, TokenCreationException, TokenNotFoundException}.java` | SecurityException 하위 |
| `G/exception/websocket/WebSocketErrorCode.java` | W001~W005 |
| `G/exception/websocket/WebSocketException.java` | GlobalException 하위 기반 클래스 |
| `G/exception/websocket/{WebSocketAuthenticationException, WebSocketConnectionException, WebSocketMessageSendException}.java` | WebSocketException 하위 |

### security/ (config/security 와 별개 패키지)
| 파일 | 역할 |
|---|---|
| `G/security/filter/IpBlockingFilter.java` | IP 레이트리밋/차단 필터 (§3.4) |
| `G/security/redis/IpBlockingRedisService.java` | Redis 기반 카운터/블랙리스트 (§3.4) |

### utils/
| 파일 | 역할 |
|---|---|
| `G/utils/SecurityUtils.java` | 현재 사용자 이메일 추출 static 유틸 (§3.5) |
| `G/utils/FileUtils.java` | 로컬 파일 저장/삭제 (§10) |
| `G/utils/NginxCachePurgeUtil.java` | nginx `PURGE` 요청 (§10) |
| `G/utils/PageUtils.java` | `Page<T>` → `commonDto.PageDto<T>` |
| `G/utils/PersonalDataEncryptionUtil.java` | AES 개인정보 암복호화 (§10) |
| `G/utils/PlaywrightBrowserManager.java` | Playwright 브라우저 lifecycle (`openBrowser`, `closeBrowser`, `newPage`, `isInitialized`) |
| `G/utils/CjTrackingPlaywright.java` | `@Configuration` 클래스. CJ대한통운 추적 페이지(`trace.cjlogistics.com`) 스크래핑 `getCurrentTrackingStatus(trackingNumber)` |

## 2. SecurityConfig (`G/config/security/SecurityConfig.java`)

- `csrf` disable, `formLogin` disable, `httpBasic` disable, `sessionManagement` disable.
- **URL 규칙**: `requestMatchers(whiteListPaths()).permitAll()` + `anyRequest().authenticated()`. `hasRole`/`hasAuthority` URL 규칙은 **없음**.
- whitelist (코드 그대로):
  `/favicon.ico`, `/error`, `/oauth2/**`, `/login/oauth2/**`, `/oauth/**`, `/auth/login`, `/auth/logout`, `/auth/refresh`, `/users/register`, `/users/find-email`, `/users/reset-password`, `/users/reset-password-sandEmail`, `/users/reset`, `/users/verify-identity`, `/identity-verification/**`, `/admin/auth/**`, `/health`, `/actuator/**`, `/ws/**`, `/swagger-ui/**`, `/v3/api-docs/**`
  - 경로에 `/api` 접두어 없음: prod nginx 가 `/api/` 를 떼고 `app:8080/` 으로 프록시하기 때문 (infra.md 참조).
  - 그 외 모든 경로(상품 조회 등 GET 포함)는 인증 필요. 비인증 시 처리 방식은 Spring Security 기본값 (별도 entryPoint 설정 없음).
- **커스텀 필터 순서** (`addFilterBefore/After`):
  1. `IpBlockingFilter` — `UsernamePasswordAuthenticationFilter` 앞
  2. `LoginAuthenticationFilter` — IpBlockingFilter 뒤
  3. `LogoutAuthenticationFilter` — Login 뒤
  4. `TokenRefreshFilter` — Logout 뒤
  5. `JwtAuthenticationFilter` — Refresh 뒤
  필터들은 `@Component` 가 아니라 `securityFilterChain()` 안에서 `new` 로 생성됨.
- **OAuth2 login**: authorizationEndpoint baseUri `/oauth2/authorization`, redirectionEndpoint `/login/oauth2/code/*`, userService = `domain.user.security.oauth2.CustomOAuth2UserService`, successHandler = `domain.user.security.oauth2.OAuth2AuthenticationSuccessHandler`. Provider 설정(google/naver/kakao)은 `application-prod.yml`.
- **CORS**: SecurityConfig 에 `.cors()` 호출 없음. `WebMvcConfig` 는 주석 처리. 유일한 `CorsConfigurationSource` 빈은 `LogViewerConfig` (origin `https://www.pinjun.xyz`, GET만, headers `Authorization`,`Content-Type`, credentials true, 경로 `/api/logs/**`). WebSocket 은 `setAllowedOrigins("https://www.pinjun.xyz")`. (prod 는 프론트/백이 같은 도메인 뒤 nginx 로 묶여 있음)
- `AuthenticationManager` 빈 노출.

## 3. 인증 흐름

### 3.1 JwtTokenProvider (`G/config/security/JwtTokenProvider.java`)
- 라이브러리: `com.auth0:java-jwt`, `Algorithm.HMAC512(jwt.secret)`.
- 설정키: `jwt.secret`, `jwt.expiration`(access ms), `jwt.refreshExpiration`(refresh ms). `application.yml` 에 둘 다 3600000(1h).
- `generateTokenPair(email, age, gender, ip, role)`:
  - Access: subject=email, iat, exp, claim `role`=Role.name().
  - Refresh: subject=email, iat, exp (role 없음).
  - 생성 후 `AuthRedisService.addAccessToken(token, email, age, gender, ttl, ip, role)` / `addRefreshToken(token, email, ttl)` 로 Redis 화이트리스트 저장 (Redis 키 `access:{token}` 해시 — `domain/user/redis/AuthRedisService.java`).
- `validateToken` 은 실패 시 false 가 아니라 **예외를 던짐** (`ExpiredTokenException` / `InvalidTokenException`).
- `getEmailFromToken`, `getRoleFromToken`.

### 3.2 JwtAuthenticationFilter
- 토큰 소스: **쿠키 `ACCESS_TOKEN` 만** (Authorization 헤더 미사용).
- `validateToken` → `authRedisService.isAccessTokenValid(token)` (Redis 화이트리스트) → email(토큰), age/gender(Redis) 조회.
- `UsernamePasswordAuthenticationToken(email, null, null)` — **principal = email 문자열, authorities = null**. details = `JwtAuthenticationFilter.UserInfo(age, gender)`.
- 주의: 만료/위조 토큰이면 `validateToken` 이 던진 예외가 필터에서 잡히지 않음 (필터 체인 단계라 `GlobalExceptionHandler` 대상 아님).
- 주의: authorities 가 비어 있으므로 `hasRole('ADMIN')` 류 검사는 통과 불가 → 관리자 확인은 서비스 레벨 `UserQueryService.checkAdminRole(email)` 로 함 (conventions.md).

### 3.3 Login / Logout / Refresh 필터
- 공통: `POST` + `request.getRequestURI()` 정확히 일치할 때만 처리, 아니면 다음 필터로.
- 응답 JSON: `{success, message, timestamp}` (Map 직렬화, `ResponseDto` 아님).
- 쿠키 속성: `httpOnly`, `secure`, `SameSite=None`, `path=/`.
- Login (`/auth/login`): body = `domain.user.dto.LoginRequestDto` → `AuthService.login(dto, clientIp)` → `ACCESS_TOKEN`(maxAge 30분), `REFRESH_TOKEN`(24시간) 쿠키. 실패 401.
- Logout (`/auth/logout`): 두 쿠키 읽어 Redis `removeAccessToken/removeRefreshToken`, 쿠키 maxAge 0. 실패 500.
- Refresh (`/auth/refresh`): 쿠키 `REFRESH_TOKEN` → validate → Redis `isRefreshTokenValid` → DB 사용자 조회 → `generateTokenPair` → **ACCESS_TOKEN 쿠키만 재설정** (새 refresh 토큰은 Redis 에만 저장되고 쿠키로 내려가지 않음). 응답에 `accessToken`, `accessTokenExpiry`(1800) 포함. 실패 401.
- 클라이언트 IP: `X-Forwarded-For` → `X-Real-IP` → `Proxy-Client-IP` → `WL-Proxy-Client-IP` → remoteAddr.

### 3.4 IpBlockingFilter + IpBlockingRedisService
- localhost(`127.0.0.1`, `::1`, `0:0:0:0:0:0:0:1`, `localhost`) 는 스킵. `shouldNotFilter`: `/health`, `/actuator/**`.
- IP 추출: X-Forwarded-For 첫 값 → X-Real-IP → Proxy-Client-IP → WL-Proxy-Client-IP → HTTP_CLIENT_IP → HTTP_X_FORWARDED_FOR → remoteAddr.
- 정책 상수: 10초 윈도우에 100회 초과 시 30분 차단.
- Redis 키: `rate_limit:ip:{ip}` (카운터, TTL=윈도우), `blocked_ip:{ip}` (값 "blocked", TTL 30분).
- 차단 응답: 429, JSON `{error:"TOO_MANY_REQUESTS", message, status:429, retryAfter(초), retryAfterMinutes}`.
- Redis 오류 시 허용(fail-open). 메서드: `isIpAllowed`, `isIpBlocked`, `blockIp`, `unblockIp`, `getCurrentRequestCount`, `getBlockRemainingTime`.

### 3.5 SecurityUtils (`G/utils/SecurityUtils.java`, static)
| 메서드 | 동작 |
|---|---|
| `extractEmailFromSecurityContext()` | principal 이 String 이면 반환, 아니면 `SecurityUserNotFoundException` |
| `extractEmailOrAnonymous()` | 실패 시 `"anonymous"` |
| `extractUserInfo()` | details 의 `JwtAuthenticationFilter.UserInfo` 또는 null |
| `extractAndValidateEmailForOrderBid(op)` | 실패 시 `OrderBidAccessDeniedException` |
| `extractAndValidateEmailForOrder(op)` | 실패 시 `OrderAccessDeniedException` |

## 4. 예외 처리

- 계층: `RuntimeException` ← `GlobalException`(abstract, `ErrorCode errorCode`) ← 도메인 `<Domain>Exception` ← 세부 예외.
- `ErrorCode` 구현은 모두 enum + `@Getter @RequiredArgsConstructor`, 필드 `(code, message, status:int)`.
- **ErrorResponse** (`G/response/ErrorResponse.java`, `@JsonInclude(NON_NULL)`): `{code, message, status, timestamp(LocalDateTime), path}`.
- **GlobalExceptionHandler** (`G/exception/GlobalExceptionHandler.java`):
  - `@ExceptionHandler` 대상: `GlobalException`, `SecurityException`(global), `FileException`, `WebSocketException`, `WeatherException`, `AccessLogException`, `ChatQuestionException`, `FAQException`, `InspectionException`, `NoticeException`, `NotificationException`, `AddressException`, `OrderException`, `PaymentException`, `WarehouseStorageException`, `EventException`, `ProductException`, `SaleException`, `ShipmentException`, `StyleException`, `InquiryException`, `UserException` → 모두 `status=errorCode.getStatus()`, 바디 `ErrorResponse.of(code, errorCode.getMessage(), status, uri)`.
  - `MethodArgumentNotValidException`, `BindException` → 400, code `G002`, message = 첫 번째 바인딩 에러 defaultMessage.
  - `Exception` → 500, `G001`.
  - **주의**: 응답 message 는 항상 `errorCode.getMessage()` (enum 기본 메시지). 예외 생성 시 넘긴 커스텀 메시지는 로그에만 남고 응답에 안 나감.
  - 도메인 `chatQuestion`, `inquiry` 등 새 도메인 예외는 GlobalException 핸들러로도 잡히므로 별도 핸들러 추가는 선택.
- **GlobalErrorCode**: G001(500) 서버오류, G002/G003(400) 입력/타입, G004(404), G005(405), G006(401), G007(403), G008/G009(401) 토큰, G010(401), G020/G021(500) 파일, G022(400), G023(404), G030(500), G031(401).
- **ErrorCode 구현 목록** (코드 접두):
  | 클래스 | 접두/범위 |
  |---|---|
  | `G/exception/GlobalErrorCode.java` | G001~G031 |
  | `G/exception/security/SecurityErrorCode.java` | S001~S006 |
  | `G/exception/file/FileErrorCode.java` | F001~F005 |
  | `G/exception/websocket/WebSocketErrorCode.java` | W001~W005 |
  | `src/main/java/com/fream/back/domain/accessLog/exception/AccessLogErrorCode.java` | AL |
  | `.../domain/address/exception/AddressErrorCode.java` | ADR |
  | `.../domain/chatQuestion/exception/ChatQuestionErrorCode.java` | CQ |
  | `.../domain/event/exception/EventErrorCode.java` | EVENT… |
  | `.../domain/faq/exception/FAQErrorCode.java` | FAQ |
  | `.../domain/inquiry/exception/InquiryErrorCode.java` | INQUIRY-xxx-n |
  | `.../domain/inspection/exception/InspectionErrorCode.java` | INS |
  | `.../domain/notice/exception/NoticeErrorCode.java` | NOT |
  | `.../domain/notification/exception/NotificationErrorCode.java` | N |
  | `.../domain/order/exception/OrderErrorCode.java` | ORD |
  | `.../domain/payment/exception/PaymentErrorCode.java` | PAY |
  | `.../domain/product/exception/ProductErrorCode.java` | P, PC, PS, BD, CL, CT, IMG, IN, FT, KF |
  | `.../domain/sale/exception/SaleErrorCode.java` | S |
  | `.../domain/shipment/exception/ShipmentErrorCode.java` | S |
  | `.../domain/style/exception/StyleErrorCode.java` | ST |
  | `.../domain/user/exception/UserErrorCode.java` | U |
  | `.../domain/warehouseStorage/exception/WarehouseStorageErrorCode.java` | WH |
  | `.../domain/weather/exception/WeatherErrorCode.java` | WE |
  - 주의: `S001` 등 `S` 접두 코드가 Security/Sale/Shipment 에서 중복됨.
  - `monitoring` 도메인에는 ErrorCode 없음.

## 5. ResponseDto (`G/dto/ResponseDto.java`)
- 필드: `success`, `message`, `data<T>`, `errorCode`, `timestamp(Long, epoch ms)`.
- 팩토리: `success(data)`, `success(data, message)`, `fail(message)`, `error(errorCode, message)`, `error(errorCode, message, data)`.
- 컨트롤러 69개 중 15개만 사용 (order, sale, faq, inquiry, inspection, chatQuestion, product Brand/Collection 등). 나머지는 `ResponseEntity<Dto>` 직접 반환.
- 페이지 응답 보조: `commonDto.PageDto<T>` + `PageUtils.toPageDto(page)` (page 는 0-based `page.getNumber()` 그대로).

## 6. BaseEntity / BaseTimeEntity (`G/entity/`)
- `BaseTimeEntity` (`@MappedSuperclass`): `createdDate`(updatable=false), `modifiedDate`. 값 세팅은 `@PrePersist`/`@PreUpdate` 로 `LocalDateTime.now()` (Auditing 아님).
- `BaseEntity extends BaseTimeEntity`: `@CreatedBy createdBy`, `@LastModifiedBy modifiedBy`. **`@EnableJpaAuditing`/`AuditorAware` 가 프로젝트에 없어 채워지지 않음**. 도메인 엔티티 중 BaseEntity 상속은 0개, BaseTimeEntity 상속은 45개.

## 7. Redis / Cache
- `RedisConfig` (`G/config/RedisConfig.java`) 설정키: `spring.redis.primary.host`(기본 `redis-primary`), `spring.redis.primary.port`(6379), `spring.redis.replica.host`(`redis-replica`), `spring.redis.replica.port`(**6380**), `spring.redis.timeout`(2000ms). prod yml 에는 이 키들이 없어 기본값 사용 (prod yml 의 `spring.redis.host/port` 는 이 클래스에서 안 읽음).
- 빈:
  | 빈 | 내용 |
  |---|---|
  | `redisConnectionFactory` (@Primary) | `RedisStaticMasterReplicaConfiguration`, `ReadFrom.REPLICA_PREFERRED` |
  | `primaryRedisConnectionFactory` / `replicaRedisConnectionFactory` | Standalone |
  | `redisTemplate` (@Primary) `RedisTemplate<String,Object>` | key String, value `GenericJackson2JsonRedisSerializer` |
  | `stringRedisTemplate` `RedisTemplate<String,String>` | 전부 String |
  | `writeRedisTemplate` / `readRedisTemplate` `RedisTemplate<String,String>` | primary 전용 / replica 전용 |
- `CacheConfig`: `ConcurrentMapCacheManager` (Redis 캐시 아님, TTL 없음). 캐시명: `faqList`, `faqDetail`, `faqCategoryList`, `faqSearchResults`, `inspectionStandards`, `inspectionStandardsByCategory`, `inspectionStandardDetail`, `inspectionStandardSearchResults`.
- Redis 키 (global 에서 사용): `rate_limit:ip:*`, `blocked_ip:*`, `WebSocket:User:{email}`.

## 8. WebSocket (`G/config/websocket/WebSocketConfig.java`)
- `@EnableWebSocketMessageBroker`. 엔드포인트 `/ws` + SockJS, allowedOrigins `https://www.pinjun.xyz`, 인터셉터 `WebSocketAuthInterceptor`.
- SimpleBroker `/topic`, `/queue`; app prefix `/app`; user prefix `/user`.
- 핸드셰이크: 쿠키 `ACCESS_TOKEN` → validate → DB 사용자 존재 확인 → `attributes["email"]`. 실패 시 false 반환(연결 거부).
- afterHandshake: Redis `WebSocket:User:{email}` = "CONNECTED", TTL 30분 (남은 TTL ≤600초일 때 갱신).
- prod nginx: `/api/ws` → `app:8080/ws`.

## 9. Kafka 설정 (global 3개 파일 + 도메인 2개)
모든 bootstrap: `spring.kafka.bootstrap-servers`. Value 직렬화 `JsonSerializer`/`JsonDeserializer`.

| 설정 파일 | Producer/Template 빈 | Consumer/ListenerFactory 빈 | 특이 설정 |
|---|---|---|---|
| `G/config/kafka/OrderKafkaConfig.java` | `orderProcessingEventProducerFactory`, `orderProcessingEventKafkaTemplate` (`<String, OrderProcessingEvent>`), `notificationProducerFactory`, `notificationKafkaTemplate` (`<String, Map<String,Object>>`) | `orderProcessingEventConsumerFactory`, `orderProcessingKafkaListenerContainerFactory` | producer acks=all, retries 3, idempotence; consumer autoCommit false, earliest, maxPoll 5; concurrency 2; AckMode `MANUAL_IMMEDIATE`; 알림 producer acks=1 |
| `G/config/kafka/PaymentKafkaConfig.java` | `paymentEventProducerFactory`, `paymentEventKafkaTemplate` (`<String, PaymentEvent>`) | `paymentEventConsumerFactory`, `paymentEventKafkaListenerContainerFactory` | acks=all, retries 3, idempotence; autoCommit false; concurrency 1; `MANUAL_IMMEDIATE` |
| `G/config/kafka/UserAccessLogKafkaConfig.java` | `userAccessLogProducerFactory`, `userAccessLogKafkaTemplate` | `userAccessLogConsumerFactory`, `userAccessLogKafkaListenerContainerFactory` | 기본값 |
| `src/main/java/com/fream/back/domain/product/service/kafka/config/ViewEventKafkaConfig.java` | (product 조회 이벤트) | `viewEventKafkaListenerContainerFactory` | — |
| `src/main/java/com/fream/back/domain/style/service/kafka/config/StyleViewEventKafkaConfig.java` | (style 조회 이벤트) | `styleViewEventKafkaListenerContainerFactory` | — |

토픽/그룹 (config 클래스에는 토픽명이 없고 Producer/Consumer 클래스에 하드코딩):
| 토픽 | Producer | Consumer (`groupId`, containerFactory) |
|---|---|---|
| `order-processing-topic` | `domain/order/service/kafka/OrderEventProducer.java` | `OrderEventConsumer` (`order-processing-group`, orderProcessing…Factory) |
| `order-retry-topic` | OrderEventProducer | `OrderEventConsumer` (`order-retry-group`, 동일 factory) |
| `notification-topic` | `OrderEventConsumer` 가 `notificationKafkaTemplate` 로 발행 | **@KafkaListener 없음** |
| `payment-processing-topic` | `domain/payment/service/kafka/PaymentEventProducer.java` | `PaymentEventConsumer` (`payment-processing-group`, paymentEvent…Factory) |
| `payment-retry-topic` | PaymentEventProducer | `PaymentEventConsumer` (`payment-retry-group`) |
| `user-access-log-topic` | `domain/accessLog/service/kafka/UserAccessLogProducer.java` (+ `_backup`) | `UserAccessLogConsumer` 와 `UserAccessLogConsumer_backup` 둘 다 `@Service` 로 활성 (`user-access-log-group`) |
| `view-log-topic` | `domain/product/service/kafka/ViewEventProducer.java` | `ViewEventConsumer` (`my-group`) |
| `style-view-log-topic` | `domain/style/service/kafka/StyleViewEventProducer.java` | `StyleViewEventConsumer` (`style-view-group`) |

- prod yml `access-log.use-kafka: false` (접근 로그는 직접 DB 저장 설정).
- prod yml 의 `spring.kafka.order.*`, `spring.kafka.payment.*`, `payment.kafka.*` 블록은 토픽/그룹명을 문서화하지만, 위 클래스들은 문자열을 하드코딩함.

## 10. 유틸
- **FileUtils** (`@Component`): `BASE_DIR = "/home/ubuntu/fream"` (하드코딩; Windows 경로 주석). `saveFile(directory, prefix, MultipartFile)` → MIME `image/*`·`video/*` 만 허용, 파일명 `prefix + UUID + 확장자`, `{BASE_DIR}/{directory}` 생성 후 저장, **파일명만 반환**. 그 외 `existsFile`, `deleteFile`, `getFileUrl` (`/api/files/{dir}/{name}` 문자열), `deleteDirectory`, `isFileExist(fullPath)`, `saveMediaFile`(=saveFile). 예외: `UnsupportedFileTypeException`, `DirectoryCreationException`, `FileUploadException`.
- **NginxCachePurgeUtil** (`@Component`): 자체 `new RestTemplate()`, 대상 `http://nginx:80`, HTTP method `PURGE`:
  `purgeProductCache()` → `/purge_products/api/products/`, `purgeEsCache()` → `/purge_es/api/es/products/`, `purgeStyleCache()` → `/purge_styles/api/styles/queries`. 예외는 무시(로그도 없음).
- **PersonalDataEncryptionUtil** (`@Component`): 키 `personal-data.encryption.secret-key`, `personal-data.encryption.iv`. `encrypt/decrypt` = AES/CBC/PKCS5Padding; `deterministicEncrypt` = AES/ECB, 키를 `SHA-256(secretKey+입력)` 앞 16바이트로 유도; `deterministicDecrypt` 는 `secretKey` 바이트를 그대로 키로 사용(암호화와 키 유도 방식이 다름); `encryptForSearch`(=deterministicEncrypt), `isEncrypted`(Base64 판별). 사용처: `domain/address/service/command/AddressCommandService.java`, `domain/address/service/query/AddressQueryService.java`, `DataInitializer`.
- **PersonalDataEncryptionAspect**: 빈 클래스. 어노테이션/포인트컷 없음 → 자동 암호화 AOP 는 존재하지 않음 (주소 도메인은 서비스에서 직접 호출; `domain/address/aop/` 는 address 담당 문서 참조).

## 11. DataInitializer (`G/config/DataInitializer.java`)
- `@Configuration @Order(1) CommandLineRunner`, `run()` 에 `@Transactional`. **`@Profile` 없음, 기존 데이터 존재 체크 없음 → 모든 프로필에서 기동 시마다 실행** (prod yml `ddl-auto: create` 와 함께 매 기동마다 초기화된 DB 에 시드).
- 생성 데이터:
  - 사용자 3명 + 프로필: `user1@example.com`(USER, 25, MALE, SIZE_270), `user2@example.com`(USER, 30, FEMALE, SIZE_280), `admin@example.com`(ADMIN, 35, MALE). (비밀번호는 코드 참조)
  - 각 사용자 기본 주소(암호화 유틸 사용), 은행 계좌.
  - 상품: 브랜드 Nike, New Balance, Adidas, Jordan, Stussy, IAB Studio, NewJeans; 카테고리 Clothing>Tops>Short Sleeve T-Shirts, Shoes>Sneakers; 상품 20개 (1~10 Sneakers, 11~20 Short Sleeve T-Shirts) + 색상/사이즈/이미지.
  - 공지 10건, FAQ 10건, 검수기준(InspectionCategory 전체), 알림(사용자별).
  - user1/user2 각 OrderBid 5건 + SaleBid 5건, user1 OrderBid 3건을 user2 Sale 과 매칭 + SellerShipment, user2 가 user1 SaleBid 3건 구매(결제/배송/창고), user2 완료 주문 기반 Style 데이터.

## 12. 스케줄러 / 기타
- global: `ShipmentBatchScheduler` (6시간마다 배송상태 배치). 도메인 스케줄러(참고): `domain/event/scheduler/EventStatusScheduler.java`(매시), `domain/style/service/scheduler/StyleViewLogBufferManager.java`(10s), `domain/faq/aop/FAQSearchOptimizationAspect.java`(1h), `domain/accessLog/config/AccessLogAopConfiguration.java`(10min), `domain/user/scheduler/PointExpirationScheduler.java`(매시), `domain/weather/config/WeatherDataScheduler.java`.
- **SwaggerConfig**: title "Fream API Documentation", v1.0.0, SecurityScheme `bearerAuth`(HTTP bearer JWT, header Authorization) 전역 적용. 실제 인증은 쿠키 기반이라 Swagger 의 bearer 입력은 JwtAuthenticationFilter 가 읽지 않음. `/swagger-ui/**`, `/v3/api-docs/**` permitAll.

## 13. LogController (`G/controller/LogController.java`)
- `@RequestMapping("/api/logs")`, 클래스 레벨 `@PreAuthorize("hasRole('ADMIN')")` (`LogViewerConfig` 의 `@EnableMethodSecurity` 로 활성).
- `GET /api/logs` → 로그 디렉토리 파일 목록 `ResponseEntity<Map<String,Object>>`.
- `GET /api/logs/{fileName}` params `page`(0), `size`(100), `search`, `level`, `reverse`(true), `startTime`, `endTime` → `LogResponseDTO`. `..`, `/`, `\` 포함 파일명 거부.
- 로그 디렉토리: `logging.file.path` (기본 `/path/to/springlog`; LogViewerConfig 기본은 `/home/ubuntu/springlog`).
- 주의: JwtAuthenticationFilter 가 authorities 를 넣지 않으므로 `hasRole('ADMIN')` 은 현재 구조상 충족되지 않음. 또 nginx 가 `/api` 를 제거하므로 외부 경로는 `/api/api/logs` 가 됨.

## 14. 주의사항 요약 (global)
1. 토큰 만료/위조 시 JwtAuthenticationFilter 에서 예외가 그대로 전파됨 (catch 없음).
2. Authentication 에 권한 없음 → `@PreAuthorize("hasRole(...)")` 무력; 관리자 체크는 `UserQueryService.checkAdminRole`.
3. Refresh 시 새 refresh 토큰은 쿠키로 안 내려감. JWT access 만료(1h) ≠ 쿠키 maxAge(30분), refresh JWT(1h) ≠ 쿠키(24h).
4. GlobalExceptionHandler 응답 message 는 enum 기본 메시지 고정.
5. `BaseEntity.createdBy/modifiedBy` 채워지지 않음 (Auditing 미활성).
6. `DataInitializer` 프로필 무관 실행.
7. `notification-topic` 소비자 없음; `UserAccessLogConsumer_backup` 이 활성 빈이라 같은 그룹에 컨슈머 2개.
8. RedisConfig replica 기본 포트 6380 (compose 는 컨테이너 내부 6379, 호스트 6380 매핑).
9. `PersonalDataEncryptionAspect` 빈 클래스, `FilterAuthService` 미사용, `WebMvcConfig` 전체 주석.
