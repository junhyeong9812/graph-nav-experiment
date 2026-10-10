# 인증/보안 흐름 (JWT 쿠키 + Redis 화이트리스트 + OAuth2 + IP 차단)

> 회원 도메인 전반은 [domain-user.md](domain-user.md) 참고.

## 역할 요약
- 세션 없이 **HttpOnly 쿠키 `ACCESS_TOKEN` / `REFRESH_TOKEN`**(JWT, HMAC512)으로 인증한다. Authorization 헤더는 사용하지 않는다.
- 발급된 토큰은 Redis(`access:{token}`, `refresh:{token}`)에 저장되고, 요청마다 **JWT 서명 검증 + Redis 존재 여부**를 함께 확인한다 (화이트리스트 방식, 로그아웃 시 키 삭제).
- 로그인/로그아웃/리프레시는 컨트롤러가 아니라 **커스텀 서블릿 필터**가 처리한다. OAuth2(google/naver/kakao) 로그인과 Redis 기반 IP 레이트리밋/차단이 같은 체인에 있다.
- 권한(ROLE)은 Spring Security authority로 세팅되지 않고, 관리자 여부는 컨트롤러에서 `UserQueryService.checkAdminRole(email)`로 DB를 조회해 판단한다.

## 패키지/파일 맵
| 경로 | 목적 |
|---|---|
| `src/main/java/com/fream/back/global/config/security/SecurityConfig.java` | `SecurityFilterChain`, permitAll 목록, OAuth2 설정, 커스텀 필터 순서 |
| `src/main/java/com/fream/back/global/config/security/JwtTokenProvider.java` | 토큰 쌍 발급(+Redis 저장), 검증, email/role 추출 |
| `src/main/java/com/fream/back/global/config/security/JwtAuthenticationFilter.java` | `ACCESS_TOKEN` 쿠키 → `SecurityContext` 세팅, 내부 클래스 `UserInfo(age, gender)` |
| `src/main/java/com/fream/back/global/config/security/EncoderConfig.java` | `BCryptPasswordEncoder` 빈 |
| `src/main/java/com/fream/back/global/config/security/dto/TokenDto.java` | `accessToken`, `refreshToken` |
| `src/main/java/com/fream/back/global/config/security/filter/LoginAuthenticationFilter.java` | `POST /auth/login` 처리 |
| `src/main/java/com/fream/back/global/config/security/filter/LogoutAuthenticationFilter.java` | `POST /auth/logout` 처리 |
| `src/main/java/com/fream/back/global/config/security/filter/TokenRefreshFilter.java` | `POST /auth/refresh` 처리 |
| `src/main/java/com/fream/back/global/config/security/service/FilterAuthService.java` | 필터용 인증 서비스 – **어디서도 사용되지 않음**(grep 결과 참조 없음) |
| `src/main/java/com/fream/back/global/security/filter/IpBlockingFilter.java` | IP 레이트리밋/차단 필터 (429 응답) |
| `src/main/java/com/fream/back/global/security/redis/IpBlockingRedisService.java` | `rate_limit:ip:*`, `blocked_ip:*` 관리 |
| `src/main/java/com/fream/back/domain/user/redis/AuthRedisService.java` | 토큰 화이트리스트 Redis 접근 (write/read 템플릿 분리) |
| `src/main/java/com/fream/back/domain/user/service/command/AuthService.java` | `login(LoginRequestDto, ip)` 실제 인증 로직 |
| `src/main/java/com/fream/back/domain/user/security/oauth2/CustomOAuth2UserService.java` | OAuth2 사용자 로드, 신규 사용자 생성/연결 |
| `src/main/java/com/fream/back/domain/user/security/oauth2/OAuth2AuthenticationSuccessHandler.java` | OAuth2 성공 후 토큰 쿠키 발급 또는 가입완료 페이지로 리다이렉트 |
| `src/main/java/com/fream/back/domain/user/security/oauth2/OAuthAttributes.java` | 제공자별 attribute 파싱 |
| `src/main/java/com/fream/back/domain/user/security/CustomUserDetailsService.java` | `UserDetailsService` (항상 ROLE_USER). 체인에서 사용되는지 미확인 |
| `src/main/java/com/fream/back/domain/user/controller/command/OAuthController.java` | `/oauth/complete-signup`, `/oauth/status`, `/oauth/connections` |
| `src/main/java/com/fream/back/domain/user/controller/command/AdminAuthController.java` | `/admin/auth/**` 관리자 전용 로그인/리프레시/로그아웃 (필터 아님, 컨트롤러) |
| `src/main/java/com/fream/back/domain/user/controller/command/AuthController.java` | `@Deprecated` `/auth/**` 안내용 |
| `src/main/java/com/fream/back/global/utils/SecurityUtils.java` | 현재 사용자 email/UserInfo 추출 static 유틸 |
| `src/main/java/com/fream/back/global/exception/security/*` | `SecurityErrorCode`(S001~S006), `ExpiredTokenException`, `InvalidTokenException`, `TokenCreationException`, `SecurityUserNotFoundException` 등 |

## 필터 체인 순서 (`SecurityConfig.securityFilterChain`)
```
IpBlockingFilter            (addFilterBefore UsernamePasswordAuthenticationFilter)
 → LoginAuthenticationFilter   (after IpBlockingFilter)       POST /auth/login 이면 응답 후 종료
 → LogoutAuthenticationFilter  (after LoginAuthenticationFilter)  POST /auth/logout 이면 응답 후 종료
 → TokenRefreshFilter          (after LogoutAuthenticationFilter) POST /auth/refresh 이면 응답 후 종료
 → JwtAuthenticationFilter     (after TokenRefreshFilter)    ACCESS_TOKEN 쿠키 → SecurityContext
 → (Spring Security OAuth2 로그인 필터 / 인가 필터 …)
```
- 커스텀 필터들은 `@Component`가 아니라 `SecurityConfig`에서 `new`로 생성됨.
- 기타 설정: CSRF disable, formLogin disable, httpBasic disable, `sessionManagement(session -> session.disable())`.
- `IpBlockingFilter.shouldNotFilter`: `/health`, `/actuator/**` 제외. localhost IP(127.0.0.1, ::1 등)는 검사 생략.
- 미인증 요청에 대한 `authenticationEntryPoint`는 별도 설정 없음 (실제 응답 코드는 미확인).

## URL 규칙 (`whiteListPaths()` → permitAll, 나머지는 `anyRequest().authenticated()`)
| 분류 | 경로 |
|---|---|
| 기본 | `/favicon.ico`, `/error` |
| OAuth | `/oauth2/**`, `/login/oauth2/**`, `/oauth/**` |
| 인증 필터 | `/auth/login`, `/auth/logout`, `/auth/refresh` |
| 공개 회원 API | `/users/register`, `/users/find-email`, `/users/reset-password`, `/users/reset-password-sandEmail`, `/users/reset`, `/users/verify-identity`, `/identity-verification/**` |
| 관리자 인증 | `/admin/auth/**` |
| 모니터링 | `/health`, `/actuator/**` |
| 기타 | `/ws/**`, `/swagger-ui/**`, `/v3/api-docs/**` |

- 위 목록 외 모든 경로(다른 도메인의 조회 API 포함)는 인증 필요. 공개 API를 추가하려면 이 배열에 추가해야 한다.
- `/admin/users/**` 등 관리자 API는 URL 레벨에서는 "authenticated"만 요구하고, 실제 ADMIN 체크는 컨트롤러 코드에서 수행.

## 쿠키 / 토큰 사양
| 항목 | 값 (코드 기준) |
|---|---|
| 쿠키 이름 | `ACCESS_TOKEN`, `REFRESH_TOKEN` |
| 쿠키 속성 | `httpOnly(true)`, `secure(true)`, `sameSite("None")`, `path("/")` |
| 쿠키 maxAge | ACCESS 30분(`30*60`), REFRESH 24시간(`24*60*60`) – 각 필터/컨트롤러에 하드코딩 |
| JWT 만료 | `jwt.expiration`, `jwt.refreshExpiration` (ms). `src/main/resources/application.yml`에서 둘 다 `3600000`(1시간) |
| JWT 서명 | `Algorithm.HMAC512(${jwt.secret})`, subject=email, Access에만 `role` claim |
| Redis TTL | 각 JWT 만료 ms와 동일 (`Duration.ofMillis(expirationMillis)`) |

### Redis 키
| 키 | 타입 | 값 | 작성/삭제 |
|---|---|---|---|
| `access:{accessToken}` | Hash | `email`, `age`, `gender`, `role`, `ip`(있을 때) | `JwtTokenProvider.generateTokenPair` → `AuthRedisService.addAccessToken`; 로그아웃/이메일 변경 시 `removeAccessToken` |
| `refresh:{refreshToken}` | Hash | `email` | `addRefreshToken` / `removeRefreshToken` |
| `rate_limit:ip:{ip}` | String(count) | 요청 수, TTL 10초 | `IpBlockingRedisService.isIpAllowed` |
| `blocked_ip:{ip}` | String `"blocked"` | TTL 30분 | `blockIp` / `unblockIp` |

## 흐름별 상세
### 1) 일반 로그인 `POST /auth/login`
1. `LoginAuthenticationFilter.isLoginRequest` – method POST && `request.getRequestURI()` == `"/auth/login"` (정확 일치).
2. body(JSON)를 `LoginRequestDto{email,password}`로 파싱.
3. IP: `X-Forwarded-For` → `X-Real-IP` → `Proxy-Client-IP` → `WL-Proxy-Client-IP` → `remoteAddr`.
4. `AuthService.login(dto, ip)` – 사용자 조회(`UserNotFoundException`), BCrypt 비교(`InvalidPasswordException`), `isActive` 확인(`UserException(ACCOUNT_INACTIVE)`), `jwtTokenProvider.generateTokenPair(email, age, gender, ip, role)`.
5. 쿠키 2개 Set-Cookie, 응답 `200 {"success":true,"message":"로그인이 성공했습니다.","timestamp":...}`.
6. 실패 시 모든 예외를 잡아 `401 {"success":false,"message":e.getMessage()}`.

### 2) 요청 인증 (`JwtAuthenticationFilter`)
1. 쿠키 `ACCESS_TOKEN` 읽기 → `jwtTokenProvider.validateToken`.
2. `authRedisService.isAccessTokenValid` (Redis `access:` 키 존재) 확인.
3. `UsernamePasswordAuthenticationToken(email, null, null)` 생성 – principal = **email 문자열**, authorities = null. details = `JwtAuthenticationFilter.UserInfo(age, gender)` (Redis에서 조회).
4. 실패 시 `SecurityContextHolder.clearContext()` 후 체인 진행.

### 3) 토큰 갱신 `POST /auth/refresh` (`TokenRefreshFilter`)
1. `REFRESH_TOKEN` 쿠키 필수 → `validateToken` → Redis `refresh:` 존재 확인 → email로 `UserRepository.findByEmail`.
2. `generateTokenPair`로 **새 Access+Refresh 쌍** 생성(둘 다 Redis 저장), 그러나 응답 쿠키는 `ACCESS_TOKEN`만 갱신.
3. 응답 `200 {"success":true,"accessToken":..., "accessTokenExpiry":1800,...}`; 실패 시 401.
4. 기존 Refresh 토큰은 삭제하지 않음(회전 없음).

### 4) 로그아웃 `POST /auth/logout` (`LogoutAuthenticationFilter`)
- 쿠키의 Access/Refresh 토큰을 Redis에서 삭제(검증 실패는 무시), 두 쿠키를 `maxAge(0)`으로 만료, `200` JSON. 예외 시 500.

### 5) 관리자 인증 `/admin/auth/**` (`AdminAuthController`, 필터 아님)
| 엔드포인트 | 동작 |
|---|---|
| `POST /admin/auth/login` | `AuthService.login` → 토큰에서 email 추출 → `checkAdminRole` → 쿠키 설정, 본문 `"관리자 로그인 성공"` |
| `GET /admin/auth/check` | ACCESS 쿠키 `validateToken` + `checkAdminRole` (Redis 화이트리스트 확인은 안 함) |
| `POST /admin/auth/refresh` | REFRESH 검증 + Redis 확인 + `checkAdminRole` → 새 쌍 발급, ACCESS 쿠키만 갱신 |
| `POST /admin/auth/logout` | Redis 삭제 + 쿠키 만료 |
| `POST /admin/auth/find-email`, `/find-password` | 전화번호로 이메일 찾기 / 임시 비밀번호 메일 (`PasswordResetService.AdminCheckPasswordResetAndSendEmail`) |
- IP 추출은 `X-Forwarded-For` → `remoteAddr`만 사용.

### 6) OAuth2 로그인 + 가입 완료
1. 프론트가 `/oauth2/authorization/{google|naver|kakao}` 호출 → Spring Security가 제공자로 리다이렉트 (`authorizationEndpoint.baseUri("/oauth2/authorization")`). `OAuth2AuthorizationController`의 동일 매핑은 로깅용.
2. 콜백 `/login/oauth2/code/*` → `CustomOAuth2UserService.loadUser`(`@Transactional`):
   - `OAuthAttributes.of(registrationId, ...)`로 파싱 (google: 최상위 / naver: `response` / kakao: `kakao_account.profile`). 그 외 제공자는 `IllegalArgumentException`.
   - email로 기존 사용자 있으면 `addOAuthConnection(provider, providerId)`만 추가.
   - 없으면 `User` 신규 생성: 랜덤 UUID 비밀번호(BCrypt), 고유 8자리 추천코드, `phoneNumber=""`, `termsAgreement=true`, `role=USER`, **`isVerified=false`** → `ProfileCommandService.createDefaultProfile`.
   - `DefaultOAuth2User` 반환 (authority = `user.getRole().name()`, naver/kakao는 name key `"id"`).
3. `OAuth2AuthenticationSuccessHandler.onAuthenticationSuccess`:
   - 제공자별로 email 추출, 없으면 `/login?error=email_not_found`로 리다이렉트.
   - `!isVerified || age == null || gender == null` 이면 임시 토큰 = `Base64(email + "|" + currentTimeMillis)` 생성 → **`/oauth/complete-signup?token=...`** 으로 리다이렉트.
   - 아니면 `generateTokenPair` → 쿠키 2개 → `http://www.pinjun.xyz`로 리다이렉트. 예외 시 `http://www.pinjun.xyz/login?error=oauth_failed`.
4. `POST /oauth/complete-signup` (`OAuthController.completeOAuthSignup`, `@Transactional`, body `OAuthSignupCompleteDto`):
   - 임시 토큰 Base64 디코드 → 30분 이내인지 확인 (서명 없음).
   - `termsAgreement`·`privacyAgreement` 필수. `updateUser`(shoeSize, adConsent, age, gender) / `updateLoginInfo`(phoneNumber) / `updateConsent` / `setVerified(true)` / 추천인 코드 있으면 `addReferrer`.
   - `generateTokenPair` → 쿠키 2개 설정 → `200 {"success":true}`. 실패 시 400.
5. `GET /oauth/status` – ACCESS 쿠키 검증 + Redis 확인 후 `{authenticated, email}`. `GET /oauth/connections` – 연결된 provider 목록.
- 설정: `application-prod.yml`의 `spring.security.oauth2.client.registration.{google,naver,kakao}` (redirect-uri `https://www.pinjun.xyz/api/login/oauth2/code/{provider}`).

### 7) IP 차단 (`IpBlockingFilter` + `IpBlockingRedisService`)
- IP 추출: `X-Forwarded-For` 첫 값 → `X-Real-IP` → `Proxy-Client-IP` → `WL-Proxy-Client-IP` → `HTTP_CLIENT_IP` → `HTTP_X_FORWARDED_FOR` → `remoteAddr`.
- 규칙(상수): **10초 윈도우에 100회** 초과 시 `blocked_ip:{ip}` 30분 설정. 차단 중이면 `429 {"error":"TOO_MANY_REQUESTS", "retryAfter":초, "retryAfterMinutes":분}`.
- Redis 오류 시 허용(fail-open). `unblockIp`, `getCurrentRequestCount`는 존재하나 이를 호출하는 관리자 API는 없음 (grep 결과 외부 참조 없음).

## 컨트롤러에서 현재 사용자 얻기
| 방법 | 위치 | 실패 시 |
|---|---|---|
| `SecurityUtils.extractEmailFromSecurityContext()` | `global/utils/SecurityUtils.java` – 43개 파일에서 사용 | `SecurityUserNotFoundException` |
| `SecurityUtils.extractEmailOrAnonymous()` | 익명이면 `"anonymous"` (product/style 조회 컨트롤러, address AOP) | – |
| `SecurityUtils.extractUserInfo()` | `JwtAuthenticationFilter.UserInfo`(age, gender) 또는 null | – |
| `SecurityUtils.extractAndValidateEmailForOrder(op)` / `...ForOrderBid(op)` | order 도메인 전용 | `OrderAccessDeniedException` / `OrderBidAccessDeniedException` |
| 컨트롤러별 private `extractEmailFromSecurityContext()` | user 도메인 Admin*/Profile/Follow/BlockedProfile/BankAccount 컨트롤러 | `IllegalStateException` |
- 내부 동작: `authentication.getPrincipal() instanceof String` 이면 그 값이 email.
- 쿠키를 직접 읽어 `jwtTokenProvider.getEmailFromToken`을 쓰는 곳: `AdminAuthController`, `OAuthController`, `global/config/websocket/WebSocketAuthInterceptor`.

## 관리자 권한 체크
- 표준 패턴: `String email = SecurityUtils.extractEmailFromSecurityContext(); userQueryService.checkAdminRole(email);`
  - `UserQueryService.checkAdminRole` → DB 조회, `role != Role.ADMIN`이면 `UserException(UserErrorCode.INSUFFICIENT_PERMISSIONS)` (U007, 403).
  - user 외부에서 약 40회 호출 (product, event, notice, faq, inquiry, inspection, chatQuestion 등).
- JWT의 `role` claim(`getRoleFromToken`)과 Redis `role` 필드(`getRoleByAccessToken`)는 정의돼 있으나 호출처 없음.
- `@PreAuthorize`: `global/config/LogViewerConfig`에 `@EnableMethodSecurity`. `ChatController`는 `isAuthenticated()`, `global/controller/LogController`(`/api/logs`)는 `hasRole('ADMIN')`.

## 예외/에러코드
- `global/exception/security/SecurityErrorCode.java`: `INVALID_TOKEN`(S001), `EXPIRED_TOKEN`(S002), `TOKEN_NOT_FOUND`(S003), `INVALID_REFRESH_TOKEN`(S004), `USER_NOT_FOUND`(S005), `TOKEN_CREATION_ERROR`(S006).
- `JwtTokenProvider.validateToken/getEmailFromToken`은 false를 반환하지 않고 `ExpiredTokenException`/`InvalidTokenException`을 **throw**함.
- 사용자 관련: `domain/user/exception/UserErrorCode.java` (U007 권한, U102 비활성 등).
- 핸들러: `global/exception/GlobalExceptionHandler.java` (`GlobalException`, `SecurityException`, `UserException` 등). 필터에서 직접 쓰는 응답은 각 필터가 JSON을 직접 작성함.

## 관련 테스트
- 보안 스택(필터, `JwtTokenProvider`, `AuthRedisService`, `IpBlocking*`, OAuth2)에 대한 테스트 파일은 `src/test`에 없음.
- `UserQueryService`/`SecurityUtils`를 mock하는 테스트: `src/test/java/com/fream/back/domain/order/OrderCommandServiceTest.java`, `OrderBidCommandServiceTest.java`, `src/test/java/com/fream/back/domain/sale/service/SaleCommandServiceTest.java`, `SaleBidCommandServiceTest.java`, `src/test/java/com/fream/back/domain/product/service/InterestCommandServiceTest.java`, `src/test/java/com/fream/back/domain/user/service/point/PointCommandServiceTest.java`.

## 자주 할 작업 → 여기를 보라
| 작업 | 파일 |
|---|---|
| 공개(비로그인) API 추가 | `SecurityConfig.whiteListPaths()` |
| 로그인 조건/에러 변경 | `AuthService.login`, `LoginAuthenticationFilter.handleLoginError` |
| 쿠키 만료/속성 변경 | `LoginAuthenticationFilter`, `TokenRefreshFilter`, `LogoutAuthenticationFilter`, `OAuth2AuthenticationSuccessHandler`, `OAuthController`, `AdminAuthController`, `UserCommandController` 각각의 `setCookie`/`expireCookie` (중복 구현, 모두 수정 필요) |
| JWT 만료 시간 | `application*.yml`의 `jwt.expiration`, `jwt.refreshExpiration` |
| JWT claim 추가 | `JwtTokenProvider.generateTokenPair` (+ `AuthRedisService.addAccessToken`) |
| SecurityContext에 권한 부여 | `JwtAuthenticationFilter.doFilterInternal` (현재 authorities null) |
| 레이트리밋 수치 | `IpBlockingRedisService` 상수 `DEFAULT_MAX_REQUESTS`/`DEFAULT_WINDOW_SECONDS`/`DEFAULT_BLOCK_MINUTES` |
| OAuth 제공자 추가 | `OAuthAttributes.of`, `CustomOAuth2UserService.extractProviderId`, `OAuth2AuthenticationSuccessHandler`(email 추출), yml registration |
| OAuth 후 리다이렉트 URL | `OAuth2AuthenticationSuccessHandler` (`http://www.pinjun.xyz` 하드코딩) |
| 관리자 체크 | `UserQueryService.checkAdminRole` |

## Gotchas (코드에서 확인)
- **`JwtAuthenticationFilter`는 `validateToken` 예외를 잡지 않음**: 만료/위조된 `ACCESS_TOKEN` 쿠키가 있으면 `ExpiredTokenException`/`InvalidTokenException`이 필터 밖으로 전파됨 (permitAll 경로 포함, `/auth/login|logout|refresh`는 앞선 필터에서 종료되므로 영향 없음). 최종 응답 형태는 미확인.
- **SecurityContext에 authorities가 없음** (`new UsernamePasswordAuthenticationToken(email, null, null)`) → `LogController`의 `@PreAuthorize("hasRole('ADMIN')")`는 JWT 사용자에게 통과되지 않을 것으로 보임(미확인). 관리자 체크는 `checkAdminRole`을 사용할 것.
- **`AdminAuthController`의 `catch (SecurityException e)`는 `java.lang.SecurityException`**(import 없음). `checkAdminRole`은 `UserException`을 던지므로 이 catch에 걸리지 않음 → 비관리자 로그인 시 403이 아니라 `catch (Exception)`의 500 응답이 되고, 이미 발급된 토큰은 Redis에 남음. 마찬가지로 `AuthService` 예외는 `IllegalArgumentException`이 아니라서 잘못된 비밀번호도 500이 됨. `/admin/auth/find-email`, `/check`, `/refresh`의 403 분기도 같은 이유로 동작하지 않음.
- `AdminAuthController.expireCookie`는 `secure(false)`이고 발급 쿠키는 `secure(true)`. 속성이 다름.
- 쿠키 maxAge(REFRESH 24시간)와 JWT/Redis 만료(application.yml에서 1시간)가 불일치. `application-prod.yml`에는 사용되지 않는 `jwt.token-validity-in-seconds: 86400` 키가 있음.
- 리프레시는 새 Refresh 토큰도 발급해 Redis에 저장하지만 쿠키로 내려주지 않음 → 사용되지 않는 `refresh:` 키가 TTL까지 남음.
- `JwtAuthenticationFilter`는 `User.isActive`를 확인하지 않음 → 관리자가 비활성화/제재해도 기존 Access 토큰은 만료까지 유효.
- OAuth 가입 완료 임시 토큰은 서명 없는 `Base64(email|timestamp)` → email만 알면 위조 가능 (코드 주석에도 "더 강력한 암호화 방식 사용 권장").
- `FilterAuthService`는 미사용 중복 코드 (`AuthService`와 거의 동일하며 `isVerified`까지 검사).
- IP 추출 로직이 여러 곳(LoginAuthenticationFilter, TokenRefreshFilter, IpBlockingFilter, OAuth2AuthenticationSuccessHandler, OAuthController, AdminAuthController, UserCommandController)에 각각 다르게 복붙됨 (IpBlockingFilter만 `X-Forwarded-For`의 첫 IP를 split).
- `AuthRedisService.addAccessToken`은 `gender.toString()`을 호출 → gender가 null인 사용자는 토큰 저장 실패(`UserException(USER_NOT_FOUND, "토큰 저장 중 오류...")`). OAuth 흐름은 gender null이면 가입완료로 보내므로 회피됨. 일반 가입은 본인인증 성별 매핑 실패 시 null 가능.
- `CustomOAuth2UserService.saveOrUpdate`는 `private` + `@Transactional` (프록시 미적용; 바깥 `loadUser`의 트랜잭션에 의존).
- `AuthController`의 `/auth/login|logout|refresh` 메서드는 필터가 먼저 응답하므로 POST로는 도달하지 않음. `GET /auth/email`, `/auth/status`는 실제 기능 없음.
- 필터의 경로 매칭은 `request.getRequestURI()` 문자열 정확 일치이므로 context-path가 붙으면 매칭되지 않음 (현재 yml에 context-path 설정 없음; OAuth redirect-uri의 `/api` 접두어는 프록시 측 처리로 보임 – 미확인).
