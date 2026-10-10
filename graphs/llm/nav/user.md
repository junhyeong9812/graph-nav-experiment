# domain/user 네비게이션 맵

기준 경로: `src/main/java/com/fream/back/domain/user` (이하 `user/`). 117개 Java 파일.

## 1. 개요

회원(User)·프로필(Profile)·인증(일반 로그인 JWT, OAuth2 google/naver/kakao, PortOne 본인인증)·팔로우/차단·정산 계좌·포인트·회원 등급·제재(Sanction)·관리자 회원관리를 담당하는 핵심 도메인. 다른 거의 모든 도메인이 `User` 엔티티, `UserRepository`, `UserQueryService`(`findByEmail`, `checkAdminRole`)에 의존한다. 인증 토큰은 **HttpOnly 쿠키(`ACCESS_TOKEN`/`REFRESH_TOKEN`)** 로 전달되고, Redis에 화이트리스트로 저장된다(`user/redis/AuthRedisService`). 실제 로그인/로그아웃/토큰갱신 처리는 컨트롤러가 아니라 `global/config/security/filter/*` 서블릿 필터가 담당한다.

## 2. 패키지/파일 구조

```
src/main/java/com/fream/back/domain/user/
├── controller/
│   ├── BankAccountController.java          /bank-account
│   ├── BlockedProfileController.java       /profiles/blocked
│   ├── FollowController.java               /follows
│   ├── ProfileController.java              /profiles
│   ├── admin/  AdminUserController, AdminGradeController, AdminPointController, AdminSanctionController   (/admin/users/**)
│   ├── command/ AuthController(@Deprecated), AdminAuthController, OAuthController, OAuth2AuthorizationController,
│   │            IdentityVerificationController, UserCommandController
│   ├── point/  PointCommandController, PointQueryController
│   └── query/  UserQueryController
├── dto/        LoginRequestDto, UserRegistrationDto, OAuthSignupCompleteDto, LoginInfoDto, LoginInfoUpdateDto,
│               EmailFindRequestDto, PasswordResetRequestDto, ProfileInfoDto, ProfileUpdateDto, BlockedProfileDto,
│               VerifiedCustomerDto, SanctionDto(내부 static DTO들), UserGradeDto(내부), UserManagementDto(내부),
│               BankAccount/{BankAccountDto, BankAccountInfoDto}, follow/FollowDto,
│               point/PointDto.java  ← 파일 위치는 dto/point 이지만 package 선언은 `com.fream.back.domain.user.dto`
├── entity/     User, Profile, Follow, BlockedProfile, BankAccount, Point, UserGrade, UserSanction, OAuthConnection
│               enums: Role, Gender, ShoeSize, PointStatus, SanctionStatus, SanctionType
├── exception/  UserErrorCode, UserException + 20개 세부 예외
├── redis/      AuthRedisService.java   (토큰 화이트리스트)
├── repository/ UserRepository, ProfileRepository, FollowRepository, BlockedProfileRepository, BankAccountRepository,
│               PointRepository, UserGradeRepository, UserSanctionRepository, UserSanctionSpecifications
├── scheduler/  PointExpirationScheduler.java
├── security/   CustomUserDetailsService.java
│   └── oauth2/ CustomOAuth2UserService, OAuth2AuthenticationSuccessHandler, OAuthAttributes
├── service/
│   ├── command/ AuthService, UserCommandService, UserUpdateService, PasswordResetService, EmailService,
│   │            IdentityVerificationService, ProfileService(미사용), UserSanctionService(빈 클래스)
│   ├── query/   UserQueryService
│   ├── admin/   AdminUserService, AdminGradeService, AdminPointService, AdminSanctionService
│   ├── point/   PointCommandService, PointQueryService
│   ├── profile/ ProfileCommandService, ProfileQueryService
│   ├── follow/  FollowCommandService, FollowQueryService
│   ├── BlockProfile/ BlockedProfileCommandService, BlockedProfileQueryService
│   └── bankaccount/  BankAccountCommandService, BankAccountQueryService
├── utils/      PasswordUtils.java (임시 비밀번호 8자 생성)
└── validate/   UserControllerValidator, BankAccountControllerValidator,
                BlockedProfile/Follow/ProfileControllerValidator(빈 클래스)
```

인증 관련 global 파일:
- `src/main/java/com/fream/back/global/config/security/SecurityConfig.java` — 필터 체인, 화이트리스트
- `src/main/java/com/fream/back/global/config/security/JwtTokenProvider.java` — JWT 생성/검증 (HMAC512)
- `src/main/java/com/fream/back/global/config/security/JwtAuthenticationFilter.java` — 쿠키 → SecurityContext
- `src/main/java/com/fream/back/global/config/security/filter/{LoginAuthenticationFilter, LogoutAuthenticationFilter, TokenRefreshFilter}.java`
- `src/main/java/com/fream/back/global/config/security/service/FilterAuthService.java` — 어디서도 참조되지 않음
- `src/main/java/com/fream/back/global/config/security/dto/TokenDto.java` (accessToken, refreshToken)
- `src/main/java/com/fream/back/global/security/filter/IpBlockingFilter.java` + `global/security/redis/IpBlockingRedisService.java`
- `src/main/java/com/fream/back/global/utils/SecurityUtils.java` — `extractEmailFromSecurityContext()`, `extractUserInfo()` 등

## 3. 엔티티

모든 엔티티(OAuthConnection 제외)는 `global.entity.BaseTimeEntity` 상속(createdDate 등).

### User (`@Table(name="users")`) — `user/entity/User.java`
- 필드: id, email(unique, not null), password(not null), referralCode(unique, not null), phoneNumber(not null), shoeSize(ShoeSize), termsAgreement, phoneNotificationConsent, emailNotificationConsent, optionalPrivacyAgreement, isVerified(default false), ci, di, role(Role), sellerGrade(Integer), age(Integer), gender(Gender), isActive(`BIT(1) DEFAULT 1`, default true)
- 관계:
  - `@OneToOne(mappedBy="user", cascade ALL, orphanRemoval)` Profile
  - `@OneToMany(mappedBy="user", cascade ALL, orphanRemoval)` → `product.entity.Interest` (interests), `address.entity.Address` (addresses), `payment.entity.PaymentInfo` (paymentInfos), Point (points), OAuthConnection (oauthConnections)
  - `@OneToOne(mappedBy="user", cascade ALL, orphanRemoval)` BankAccount
  - 추천인: `@ManyToOne referrer` (`@JoinColumn(name="referrerCode", referencedColumnName="referralCode", insertable=false, updatable=false)`, User.java:97) / `@OneToMany(mappedBy="referrer") referredUsers`
  - `@ManyToOne(LAZY) @JoinColumn("grade_id")` UserGrade
- 주요 메서드: addOAuthConnection/hasOAuthConnection, addGrade, addActive, setVerified, changeRole, updateUser(7인자/9인자 오버로드), updateLoginInfo, updateConsent, addReferrer, addProfile, assignBankAccount/removeBankAccount, add/remove Address·PaymentInfo·Interest·Point, calculateTotalPoints
- 다른 도메인에서 User를 FK로 참조하는 엔티티(import 기준): address, chatQuestion, inquiry, notification, order, payment, product, sale, warehouseStorage. QueryDSL `QUser`가 inquiry/notification/order repository에서 사용됨.

### Profile — `user/entity/Profile.java`
- 필드: id, user(`@OneToOne LAZY`, `user_id` not null), profileName(unique, not null), `Name`(대문자 필드명, Profile.java:32), bio, isPublic, profileImageUrl
- 관계: `@OneToMany(mappedBy="follower")` followings(Follow), `@OneToMany(mappedBy="following")` followers(Follow), `@OneToMany(mappedBy="blockedProfile")` blockedByProfiles(BlockedProfile), `@OneToMany(mappedBy="profile")` styles → `style.entity.Style` (모두 cascade ALL, orphanRemoval)
- style 도메인이 Profile을 FK로 참조(`style/entity` 5곳 import, `QProfile` 사용)

### Follow (`@Table(name="follows")`)
- follower(`@ManyToOne` Profile, `follower_profile_id`), following(`@ManyToOne` Profile, `following_profile_id`)

### BlockedProfile
- profile(`@ManyToOne` 차단한 쪽, `profile_id`), blockedProfile(`@ManyToOne` 차단된 쪽, `blocked_profile_id`)

### BankAccount
- user(`@OneToOne LAZY`, `user_id`), bankName, accountNumber, accountHolder

### Point
- user(`@ManyToOne LAZY`), amount, remainingAmount, reason, expirationDate(LocalDate), status(PointStatus)
- 생성자 `Point(User,int,String)`: 만료일 now+90일, AVAILABLE. `use(int)` 사용 처리(남은 금액 소진 시 USED), `expire()`, `isValid()`

### UserGrade (`@Table(name="user_grades")`)
- level(unique), name(unique), description, minPurchaseAmount, pointRate(Double, not null), benefits, `@OneToMany(mappedBy="grade") users`

### UserSanction (`@Table(name="user_sanctions")`)
- user(`@ManyToOne LAZY`), reason, details, type(SanctionType), status(SanctionStatus), startDate, endDate(영구정지는 null), targetId, targetType(문자열 "STYLE","COMMENT" 등), createdBy, approvedBy, rejectedBy, rejectionReason
- approve(adminEmail)→ACTIVE, reject→REJECTED, cancel→CANCELED, expire()(ACTIVE & endDate 경과 → EXPIRED)

### OAuthConnection (BaseTimeEntity 미상속)
- user(`@ManyToOne`, `user_id`), provider("google"/"naver"/"kakao"), providerId, accessToken, connectedAt

### Enums
| Enum | 값 |
|---|---|
| Role | USER, ADMIN |
| Gender | MALE, FEMALE, OTHER |
| ShoeSize | SIZE_220 ~ SIZE_300 (5 단위, `getValue()` int) |
| PointStatus | AVAILABLE, USED, EXPIRED |
| SanctionStatus | PENDING, ACTIVE, EXPIRED, REJECTED, CANCELED |
| SanctionType | WARNING, TEMPORARY_BAN, PERMANENT_BAN, FEATURE_RESTRICTION |

## 4. API 엔드포인트

인증 표기: **WL** = SecurityConfig 화이트리스트(permitAll), **AUTH** = 인증 필요(`anyRequest().authenticated()`), **ADMIN** = AUTH + 컨트롤러에서 `UserQueryService.checkAdminRole(email)` 호출. 이메일 추출은 `SecurityUtils.extractEmailFromSecurityContext()` 또는 각 컨트롤러의 private `extractEmailFromSecurityContext()`(동일 로직: principal이 String이면 반환).

### 인증 (필터 처리 — 컨트롤러 아님)
| Method | Path | 처리 | 비고 |
|---|---|---|---|
| POST | /auth/login | `LoginAuthenticationFilter` → `AuthService.login(dto, ip)` | WL. body `LoginRequestDto`(email,password). 쿠키 set + JSON `{success,message,timestamp}`; 실패 401 |
| POST | /auth/logout | `LogoutAuthenticationFilter.handleLogout` → `AuthRedisService.removeAccessToken/removeRefreshToken` | WL. 쿠키 maxAge 0 |
| POST | /auth/refresh | `TokenRefreshFilter.handleTokenRefresh` → `JwtTokenProvider.generateTokenPair` | WL. REFRESH_TOKEN 쿠키 필요, 새 ACCESS_TOKEN 쿠키 + body accessToken |
| GET | /oauth2/authorization/{registrationId} | Spring Security OAuth2 (SecurityConfig authorizationEndpoint) | WL |
| GET | /login/oauth2/code/* | `CustomOAuth2UserService.loadUser` → `OAuth2AuthenticationSuccessHandler` | WL |

`AuthController`(`/auth`, @Deprecated): POST /login, /logout, /refresh는 필터가 먼저 응답하므로 도달하지 않음(도달 시 400 ENDPOINT_DEPRECATED). GET /auth/email(400 NOT_IMPLEMENTED), GET /auth/status(안내 메시지만) — 둘 다 화이트리스트 아님.

### AdminAuthController (`/admin/auth`, 전부 WL)
| Method | Path | Controller#method | Service |
|---|---|---|---|
| POST | /admin/auth/login | adminLogin | AuthService.login → UserQueryService.checkAdminRole → 쿠키 set |
| POST | /admin/auth/find-email | findAdminEmail | UserQueryService.findEmailByPhoneNumber, checkAdminRole |
| GET | /admin/auth/check | checkAdminToken | JwtTokenProvider.validateToken, checkAdminRole |
| POST | /admin/auth/refresh | refreshAdminToken | AuthRedisService.isRefreshTokenValid, UserQueryService.findByEmail, JwtTokenProvider.generateTokenPair |
| POST | /admin/auth/find-password | findAdminPassword | PasswordResetService.AdminCheckPasswordResetAndSendEmail |
| POST | /admin/auth/logout | logout | AuthRedisService.removeAccessToken/removeRefreshToken |

### OAuthController (`/oauth`, WL)
| POST | /oauth/complete-signup | completeOAuthSignup (@Transactional, 컨트롤러에서 UserRepository 직접 사용) | 임시토큰 디코드 → user 갱신, setVerified(true) → generateTokenPair → 쿠키 |
|---|---|---|---|
| GET | /oauth/status | checkAuthStatus | validateToken + AuthRedisService.isAccessTokenValid |
| GET | /oauth/connections | getOAuthConnections | UserRepository.findByEmail → oauthConnections |

`OAuth2AuthorizationController`: GET `/oauth2/authorization/{registrationId}` — 로깅 후 200 문자열 반환. Security의 OAuth2 authorization endpoint baseUri와 같은 경로.

### IdentityVerificationController (`/identity-verification`, WL)
| POST | /identity-verification/verify | verifyIdentity | IdentityVerificationService.verifyIdentity (body `{identityVerificationId}`) |
|---|---|---|---|

### UserCommandController (`/users`)
| Method | Path | Controller#method | Service | Auth |
|---|---|---|---|---|
| POST | /users/register | registerUser | UserCommandService.registerUser | WL |
| POST | /users/verify-identity | verifyIdentity | IdentityVerificationService.verifyIdentity | WL |
| PUT | /users/update | updateUserInfo | UserUpdateService.updateLoginInfo, UserQueryService.getLoginInfo, (이메일 변경 시) AuthRedisService.remove* + UserUpdateService.reissueTokenAfterEmailChange → 쿠키 재설정 | AUTH (SecurityUtils) |

### UserQueryController (`/users`)
| Method | Path | Controller#method | Service | Auth |
|---|---|---|---|---|
| POST | /users/find-email | findEmail | UserQueryService.findEmailByPhoneNumber | WL |
| POST | /users/reset-password | resetPasswordEligibility | PasswordResetService.checkPasswordResetEligibility | WL |
| POST | /users/reset-password-sandEmail | resetPasswordToEmail | PasswordResetService.checkPasswordResetAndSendEmail | WL |
| POST | /users/reset | resetPassword | PasswordResetService.resetPassword | WL |
| GET | /users/login-info | getLoginInfo | UserQueryService.getLoginInfo | AUTH |
| DELETE | /users/delete-account | deleteAccount | UserCommandService.deleteAccount | AUTH |

### ProfileController (`/profiles`, AUTH)
| GET | /profiles | getProfile | ProfileQueryService.getProfileInfo |
|---|---|---|---|
| PUT | /profiles (multipart: part `profileImage` optional, part `dto`) | updateProfile | ProfileCommandService.updateProfile |
| GET | /profiles/{profileId}/image | getProfileImage | ProfileQueryService.getProfileImageFileName → 파일 `/home/ubuntu/fream/profile_images/<name>` 직접 읽음 |

### BlockedProfileController (`/profiles/blocked`, AUTH)
| POST | /profiles/blocked (body `{blockedProfileId}`) | blockProfile | BlockedProfileCommandService.blockProfile |
|---|---|---|---|
| DELETE | /profiles/blocked?blockedProfileId= | unblockProfile | BlockedProfileCommandService.unblockProfile |
| GET | /profiles/blocked | getBlockedProfiles | BlockedProfileQueryService.getBlockedProfiles(email) |

### FollowController (`/follows`, AUTH)
| POST | /follows/{profileId} | createFollow | FollowCommandService.createFollow |
|---|---|---|---|
| DELETE | /follows/{profileId} | deleteFollow | FollowCommandService.deleteFollow |
| GET | /follows/followers (Pageable) | getFollowers | FollowQueryService.getFollowers |
| GET | /follows/followings (Pageable) | getFollowings | FollowQueryService.getFollowings |

### BankAccountController (`/bank-account`, AUTH)
| POST | /bank-account | createOrUpdateBankAccount | BankAccountControllerValidator.validateBankAccountDto → BankAccountCommandService.createOrUpdateBankAccount |
|---|---|---|---|
| GET | /bank-account | getBankAccount | BankAccountQueryService.getBankAccount |
| DELETE | /bank-account | deleteBankAccount | BankAccountCommandService.deleteBankAccount |

### Point (AUTH, SecurityUtils)
| POST | /points/commands | PointCommandController#addPoint | PointCommandService.addPoint |
|---|---|---|---|
| POST | /points/commands/use | PointCommandController#usePoint | PointCommandService.usePoint |
| GET | /points/queries | PointQueryController#getAllPointHistory | PointQueryService.getAllPointHistory |
| GET | /points/queries/available | #getAvailablePoints | PointQueryService.getAvailablePoints |
| GET | /points/queries/summary | #getPointSummary | PointQueryService.getPointSummary |
| GET | /points/queries/{pointId} | #getPointDetail | PointQueryService.getPointDetail |

### Admin (전부 ADMIN)
`AdminUserController` (`/admin/users`) → `AdminUserService`
| GET | /admin/users/search (keyword,email,phoneNumber,ageStart/End,gender,registrationDateStart/End(yyyy-MM-dd),isVerified,sellerGrade,shoeSize,role,sort,direction,page,size) | searchUsers |
|---|---|---|
| GET | /admin/users/{userId} | getUserDetail |
| PATCH | /admin/users/{userId}/status | updateUserStatus |
| PATCH | /admin/users/{userId}/grade | updateUserGrade |
| PATCH | /admin/users/{userId}/role | updateUserRole |
| POST | /admin/users/{userId}/points | manageUserPoints (amount 양수=지급, 음수=차감) |
| GET | /admin/users/export | exportUsersCsv (text/csv) |

`AdminGradeController` (`/admin/users/grades`) → `AdminGradeService`: GET `` getAllGrades, GET `/{gradeId}`, POST `` createGrade, PUT `/{gradeId}`, DELETE `/{gradeId}`, GET `/counts` getGradeUserCounts, GET `/statistics`, POST `/auto-assign` runGradeAutoAssignment

`AdminPointController` (`/admin/users/points`) → `AdminPointService`: GET `/user/{userId}`, GET `/user/{userId}/available`, GET `/user/{userId}/summary`, GET `/{pointId}`, POST `/user/{userId}/add` addPointByAdmin, POST `/user/{userId}/deduct` deductPointByAdmin, GET `/statistics?startDate&endDate`

`AdminSanctionController` (`/admin/users/sanctions`) → `AdminSanctionService`: GET `/search`, GET `/{sanctionId}`, POST `` createSanction (헤더 `X-Admin-Email` 필수), PUT `/{sanctionId}` updateSanction, PATCH `/{sanctionId}/review` reviewSanction (헤더 `X-Admin-Email` 필수), PATCH `/{sanctionId}/cancel`, GET `/user/{userId}`, GET `/statistics`

## 5. 인증 흐름 상세

**필터 순서** (`SecurityConfig.java:77-81`): `IpBlockingFilter`(UsernamePasswordAuthenticationFilter 앞) → `LoginAuthenticationFilter` → `LogoutAuthenticationFilter` → `TokenRefreshFilter` → `JwtAuthenticationFilter`. CSRF/formLogin/httpBasic/session 비활성. 화이트리스트: `SecurityConfig.whiteListPaths()` (89-127).

1. **로그인** `POST /auth/login`
   - `LoginAuthenticationFilter.isLoginRequest` (POST && URI == "/auth/login") → body를 `LoginRequestDto`로 파싱 → IP 추출(X-Forwarded-For → X-Real-IP → Proxy-Client-IP → WL-Proxy-Client-IP → remoteAddr)
   - `AuthService.login` (`user/service/command/AuthService.java:30`): findByEmail(없으면 UserNotFoundException) → `passwordEncoder.matches`(불일치 InvalidPasswordException) → `isActive` 체크(ACCOUNT_INACTIVE) → `JwtTokenProvider.generateTokenPair(email, age, gender, ip, role)`. **isVerified는 검사하지 않음.**
   - `JwtTokenProvider.generateTokenPair` (JwtTokenProvider.java:53): Access = subject email + claim `role`, Refresh = subject email (claim 없음), 둘 다 HMAC512(`${jwt.secret}`). 만료: `${jwt.expiration}`, `${jwt.refreshExpiration}` (application.yml:23-24 둘 다 3600000ms). 생성 직후 Redis 저장.
   - 쿠키: `ACCESS_TOKEN` maxAge 30분, `REFRESH_TOKEN` maxAge 24시간, HttpOnly, Secure, SameSite=None, path=/ (LoginAuthenticationFilter.java:91-92, 123-132)
2. **요청 인증** `JwtAuthenticationFilter.doFilterInternal` (JwtAuthenticationFilter.java:27-62)
   - 쿠키 `ACCESS_TOKEN`만 읽음 (Authorization 헤더는 보지 않음)
   - `validateToken` → `authRedisService.isAccessTokenValid` (Redis에 `access:<token>` 존재) → `UsernamePasswordAuthenticationToken(email, null, null)` + details `JwtAuthenticationFilter.UserInfo(age, gender)` (Redis에서 조회)
   - 권한(authorities)을 넣지 않으므로 Spring Security 역할 기반 인가는 없음. 관리자 판별은 각 컨트롤러가 `checkAdminRole`로 DB의 `User.role` 확인.
3. **토큰 갱신** `POST /auth/refresh` (`TokenRefreshFilter`): REFRESH_TOKEN 쿠키 → validateToken → Redis `refresh:<token>` 존재 확인 → DB 사용자 조회 → generateTokenPair → ACCESS_TOKEN 쿠키만 재설정(30분), 응답 JSON에 accessToken.
4. **로그아웃** `POST /auth/logout` (`LogoutAuthenticationFilter`): 두 쿠키 읽음 → access 검증은 실패해도 진행 → Redis `access:`/`refresh:` 키 삭제 → 두 쿠키 만료.
5. **OAuth2**: `/oauth2/authorization/{google|naver|kakao}` → 콜백 `/login/oauth2/code/*` → `CustomOAuth2UserService.loadUser`(`OAuthAttributes.of`로 정규화, 기존 이메일이면 OAuthConnection 추가 / 신규면 랜덤 비번·phoneNumber ""·isVerified=false·Role.USER로 User 저장 + `ProfileCommandService.createDefaultProfile`) → `OAuth2AuthenticationSuccessHandler.onAuthenticationSuccess`:
   - `!isVerified || age==null || gender==null` 이면 `/oauth/complete-signup?token=<Base64(email|timestamp)>` 로 redirect (OAuth2AuthenticationSuccessHandler.java:94-101)
   - 아니면 generateTokenPair → 쿠키 set → `http://www.pinjun.xyz` redirect (실패 시 `http://www.pinjun.xyz/login?error=oauth_failed`)
   - 이후 `POST /oauth/complete-signup` (OAuthController.java:42): 임시 토큰 Base64 디코드, 30분 유효, 필수 동의 확인 → 정보 갱신 + `setVerified(true)` → 토큰 발급/쿠키
6. **관리자 로그인** `POST /admin/auth/login`: `AuthService.login`으로 토큰 발급(=Redis 저장) 후 `checkAdminRole`, 쿠키 set. `/admin/auth/refresh`는 Redis 화이트리스트 확인 + 관리자 확인 후 ACCESS_TOKEN만 재설정.
7. **이메일 변경 시 재발급**: `PUT /users/update` → 이메일 변경이면 기존 토큰 Redis 삭제 후 `UserUpdateService.reissueTokenAfterEmailChange` → 두 쿠키 재설정.

### Redis 키 (`user/redis/AuthRedisService.java`)
| 키 | 타입 | 필드 | TTL |
|---|---|---|---|
| `access:<accessToken>` | Hash | email, age, gender, role, ip(옵션) | jwt.expiration |
| `refresh:<refreshToken>` | Hash | email | jwt.refreshExpiration |
- 쓰기: `@Qualifier("writeRedisTemplate")`(Primary), 읽기: `readRedisTemplate`(Replica) → 값 없거나 실패 시 Primary로 재조회 (`safeReadFromReplica`, `safeHasKey`). 템플릿 빈 정의: `global/config/RedisConfig.java:179, 201`.
- 메서드: addAccessToken, addRefreshToken, isAccessTokenValid, isRefreshTokenValid, getEmail/Age/Gender/RoleByAccessToken, removeAccessToken, removeRefreshToken.
- WebSocket: `global/config/websocket/WebSocketAuthInterceptor`/`WebSocketConfig`가 UserRepository, JwtTokenProvider, RedisTemplate를 주입받아 사용.

## 6. 서비스 계층

| 서비스 | 주요 메서드 | 의존 |
|---|---|---|
| `service/command/AuthService` | login(dto, ip) → TokenDto | UserRepository, PasswordEncoder, JwtTokenProvider |
| `service/command/UserCommandService` | registerUser(dto): 필수동의(isOver14/terms/privacy) → existsByEmail → PortOne 본인인증 → CI 중복(findByCi) → 만14세 검증 → 추천인 → gender/age는 본인인증 결과로 설정, isVerified=true, ci/di 저장 → save → createDefaultProfile. deleteAccount(email): 알림 삭제 → 프로필 이미지 파일 삭제 → userRepository.delete | UserRepository, PasswordEncoder, ProfileCommandService, FileUtils, **notification.NotificationCommandService**(deleteNotificationsByUser), IdentityVerificationService |
| `service/command/UserUpdateService` | updateLoginInfo(email, dto), reissueTokenAfterEmailChange(...) | UserRepository, PasswordEncoder, AuthRedisService, JwtTokenProvider |
| `service/command/PasswordResetService` | checkPasswordResetEligibility, resetPassword(dto), checkPasswordResetAndSendEmail(임시 비번 메일 발송 후 저장), AdminCheckPasswordResetAndSendEmail | UserRepository, PasswordEncoder, EmailService, PasswordUtils |
| `service/command/EmailService` | sendEmail(to, subject, html) | JavaMailSender |
| `service/command/IdentityVerificationService` | verifyIdentity(id): GET `https://api.portone.io/identity-verifications/{id}` (헤더 `Authorization: PortOne ${imp.secret}`), status=="VERIFIED" 확인 → VerifiedCustomerDto(name, phoneNumber, gender, birthDate, isForeigner, ci, di) | RestTemplate, ObjectMapper |
| `service/query/UserQueryService` | checkAdminRole(email)(ADMIN 아니면 UserException INSUFFICIENT_PERMISSIONS), findEmailByPhoneNumber, getLoginInfo, findByEmail | UserRepository |
| `service/profile/ProfileCommandService` | createDefaultProfile(user)(랜덤 10자 profileName, 이미지 "user.jpg", Name=이메일 앞부분, isPublic true), updateProfile(email, dto, image)(5MB, jpeg/png/gif만; 파일 `profile_images/profile_<id>_...`) | ProfileRepository, FileUtils |
| `service/profile/ProfileQueryService` | getProfileInfo(email)(차단목록 포함), getProfileByEmail(email), getProfileImageFileName(id) | ProfileRepository, BlockedProfileQueryService |
| `service/follow/FollowCommandService` | createFollow(email, profileId)(자기자신/중복 검사 후 저장 + 알림 `NotificationCategory.STYLE, NotificationType.FOLLOW`), deleteFollow | FollowRepository, ProfileRepository, **NotificationCommandService.createNotification** |
| `service/follow/FollowQueryService` | getFollowers, getFollowings (Page<FollowDto>) | FollowRepository, ProfileRepository |
| `service/BlockProfile/BlockedProfileCommandService` | blockProfile, unblockProfile | ProfileRepository, BlockedProfileRepository |
| `service/BlockProfile/BlockedProfileQueryService` | getBlockedProfiles(Profile), getBlockedProfiles(String email) | BlockedProfileRepository |
| `service/bankaccount/BankAccountCommandService` | createOrUpdateBankAccount, deleteBankAccount | UserRepository |
| `service/bankaccount/BankAccountQueryService` | getBankAccount(email) → BankAccountInfoDto | BankAccountRepository |
| `service/point/PointCommandService` | addPoint(최대 1,000,000, 만료 90일), usePoint(만료 임박순 차감), expirePoints() | PointRepository, UserQueryService |
| `service/point/PointQueryService` | getAllPointHistory, getAvailablePoints, getPointSummary(30일 내 만료 예정 포함), getPointDetail(소유자 확인) | PointRepository, UserQueryService |
| `service/admin/AdminUserService` | searchUsers(Specification 동적 조건), getUserDetail, updateUserStatus(비활성화+reason 시 TEMPORARY_BAN/ACTIVE, endDate 없는 제재 레코드 생성, createdBy "admin"), updateUserGrade, updateUserRole, manageUserPoints, exportUsersCsv | User/Profile/Point/UserGrade/UserSanction/BankAccount Repository |
| `service/admin/AdminGradeService` | CRUD, getGradeUserCounts, getGradeStatistics, runGradeAutoAssignment | UserGradeRepository, UserRepository |
| `service/admin/AdminPointService` | 사용자별 이력/가용/요약, addPointByAdmin(기본 만료 1년), deductPointByAdmin, getPointStatistics | UserRepository, PointRepository |
| `service/admin/AdminSanctionService` | searchSanctions(Specification), create(PENDING 생성, 비영구는 endDate 필수), update(PENDING만), review(승인 시 BAN류면 user.isActive=false / 거부 시 사유 필수), cancel(ACTIVE만, 다른 BAN 없으면 isActive=true), getUserSanctions, statistics, processExpiredSanctions | UserSanctionRepository, UserRepository |
| `security/oauth2/CustomOAuth2UserService` | loadUser, saveOrUpdate | UserRepository, ProfileCommandService, PasswordEncoder |
| `security/oauth2/OAuth2AuthenticationSuccessHandler` | onAuthenticationSuccess | JwtTokenProvider, UserRepository |
| `security/CustomUserDetailsService` | loadUserByUsername (항상 roles("USER")) | UserRepository |

admin 서비스들은 `global.exception.EntityNotFoundException` / `InvalidRequestException` 을 던짐(UserException 아님).

## 7. Repositories

- `UserRepository` (JpaRepository + `JpaSpecificationExecutor<User>`): findByReferralCode, findByEmail, findByEmailAndPhoneNumber, existsByEmail, findByCi, findByDi, findByPhoneNumber; `@Query` findByMultipleCriteria, searchUsers(keyword/isVerified/isActive/gender/createdDate, Page), countUsersByGrade, countActiveUsers, findRecentlyJoinedUsers. (AdminUserService는 `@Query searchUsers`가 아니라 Specification + `findAll(spec, pageable)` 사용)
- `ProfileRepository`: `@Query findByUserEmailWithFetchJoin(email)` (JOIN FETCH user — 거의 모든 프로필/팔로우/차단 로직의 진입점), findByUser
- `FollowRepository`: findByFollowerAndFollowing, `@Query` findFollowersByProfileId / findFollowingsByProfileId (Page)
- `BlockedProfileRepository`: `@Query` findAllByProfileWithBlocked(JOIN FETCH blockedProfile), `@Query` findAllByProfileEmailWithBlocked(email), findByProfileAndBlockedProfile, findByProfile, findByProfileAndBlockedProfileId, findBlockedProfileIdsByProfile(List<Long> 반환 derived query)
- `BankAccountRepository`: findByUser_Email(email) (Optional 아님, null 반환)
- `PointRepository`: findByUserOrderByCreatedDateDesc, findByUserAndStatusOrderByExpirationDateAsc, findByStatusAndExpirationDateBefore, `@Query getTotalAvailablePoints`(SUM remainingAmount, status AVAILABLE), `@Query findExpiringPoints`
- `UserGradeRepository`: findByLevel, findByName, findAllByOrderByLevelAsc, `@Query` findGradeByPurchaseAmount, countUsersByGradeId, countUsersGroupByGrade
- `UserSanctionRepository` (+ JpaSpecificationExecutor): findByUserOrderByCreatedDateDesc, findByUserAndStatusOrderByStartDateDesc, `@Query` findExpirableSanctions, findByUserEmail, countActive/Expired/Pending/TotalSanctions
- `UserSanctionSpecifications`: withSearchCriteria(SanctionSearchRequestDto) 외 hasUserId/hasStatus/... 정적 Specification
- user 도메인 내부에는 QueryDSL 구현(RepositoryImpl/JPAQueryFactory) 없음. 단 Q클래스(`QUser`, `QProfile`)는 다른 도메인 repository에서 사용.

## 8. 외부 연동 / 비동기

- **Scheduler**: `user/scheduler/PointExpirationScheduler.checkExpiredPoints()` — `@Scheduled(cron = "0 0 * * * *")` (매시 정각) → `PointCommandService.expirePoints()` (AVAILABLE & expirationDate < today → EXPIRED)
- **Redis**: 위 5절 키 표 참조
- **외부 API**: PortOne 본인인증 `https://api.portone.io/identity-verifications/{id}` (`IdentityVerificationService`, `${imp.secret}`)
- **메일**: `EmailService` (JavaMailSender, HTML) — 임시 비밀번호 발송
- **OAuth2 제공자**: google, naver(`response` 객체 안에 정보), kakao(`kakao_account`/`profile`)
- **알림**: `NotificationCommandService.createNotification`(팔로우 시), `deleteNotificationsByUser`(탈퇴 시) 동기 호출
- **파일**: `global.utils.FileUtils` saveFile/deleteFile(디렉터리 "profile_images")
- user 도메인 내에는 Kafka producer/consumer, `@Aspect`, `@EventListener`, `@Async` 없음 (grep 결과)

## 9. 다른 도메인과의 의존

**user → 타 도메인**
- entity: `address.Address`, `payment.PaymentInfo`, `product.Interest` (User), `style.Style` (Profile)
- service: `notification.service.command.NotificationCommandService` (UserCommandService, FollowCommandService), `notification.entity.NotificationCategory/NotificationType`
- global: `JwtTokenProvider`, `TokenDto`, `SecurityUtils`, `FileUtils`, `BaseTimeEntity`, `GlobalException/ErrorCode`, `EntityNotFoundException`, `InvalidRequestException`

**타 도메인/global → user** (import 기준)
- `UserQueryService`: product(controller 7곳), order, sale, payment, chatQuestion(controller/service/aop `ChatSecurityAspect`), event, faq, inquiry, inspection, notice. 호출 메서드 집계: `checkAdminRole` 37회, `findByEmail` 19회
- `User` 엔티티: address, chatQuestion, inquiry, notification, order, payment, product, sale, shipment, warehouseStorage
- `UserRepository`: address, inquiry, notification, payment 서비스, `global/config/security`, `global/config/websocket`, `global/config/DataInitializer`
- `Profile` / `ProfileQueryService.getProfileByEmail`(13회): style 도메인
- `Gender`: product, style
- `BankAccountQueryService` + `BankAccountInfoDto`: `domain/sale/service/command/SaleCommandService.java`
- `AuthRedisService`, `AuthService`, `LoginRequestDto`, `CustomOAuth2UserService`, `OAuth2AuthenticationSuccessHandler`: `global/config/security`
- `UserException`: `global/exception/GlobalExceptionHandler.java`
- `global/config/DataInitializer.java`: UserRepository, ProfileRepository, BankAccountRepository, ProfileCommandService

## 10. 예외

- `user/exception/UserErrorCode` (implements `global.exception.ErrorCode`, 필드 code/message/status)
  - U0xx 일반: U001 USER_NOT_FOUND(404), U002 DUPLICATE_EMAIL(409), U003 DUPLICATE_PHONE_NUMBER, U004 INVALID_PASSWORD, U005 INVALID_VERIFICATION_CODE, U006 IDENTITY_VERIFICATION_FAILED, U007 INSUFFICIENT_PERMISSIONS(403)
  - U1xx 계정: U101 ACCOUNT_LOCKED, U102 ACCOUNT_INACTIVE(403), U103 ACCOUNT_ALREADY_VERIFIED
  - U2xx 프로필: U201 PROFILE_NOT_FOUND, U202 INVALID_PROFILE_IMAGE, U203 PROFILE_IMAGE_TOO_LARGE
  - U3xx 포인트: U301 INSUFFICIENT_POINTS, U302 POINT_HISTORY_NOT_FOUND, U303 INVALID_POINT_AMOUNT
  - U4xx 팔로우: U401 ALREADY_FOLLOWING, U402 NOT_FOLLOWING, U403 CANNOT_FOLLOW_SELF
  - U5xx 차단: U501 ALREADY_BLOCKED, U502 NOT_BLOCKED, U503 CANNOT_BLOCK_SELF
  - U6xx 계좌: U601 BANK_ACCOUNT_NOT_FOUND, U602 INVALID_BANK_ACCOUNT
  - U7xx 등급: U701 GRADE_NOT_FOUND / U8xx 제재: U801 SANCTION_NOT_FOUND, U802 USER_SANCTIONED / U9xx OAuth: U901 OAUTH_PROVIDER_NOT_SUPPORTED, U902 OAUTH_AUTHENTICATION_FAILED
- `UserException extends global.exception.GlobalException`(RuntimeException). 세부 예외 모두 UserException 상속: UserNotFoundException, DuplicateEmailException, DuplicatePhoneNumberException, InvalidPasswordException, IdentityVerificationFailedException, ProfileNotFoundException, InvalidProfileImageException, ProfileImageTooLargeException, InsufficientPointsException, InvalidPointAmountException, PointHistoryNotFoundException, AlreadyFollowingException, NotFollowingException, CannotFollowSelfException, AlreadyBlockedException, NotBlockedException, CannotBlockSelfException, BankAccountNotFoundException, InvalidBankAccountException, OAuthAuthenticationFailedException, OAuthProviderNotSupportedException
- 보안 관련 예외는 `global/exception/security/*` (SecurityErrorCode, ExpiredTokenException, InvalidTokenException, TokenCreationException, SecurityUserNotFoundException 등) — `JwtTokenProvider.validateToken`은 false가 아니라 예외를 던짐.

## 11. 작업 팁 / 주의점 (코드에서 확인한 사실)

**인증/토큰**
- 토큰은 헤더가 아니라 쿠키 `ACCESS_TOKEN`/`REFRESH_TOKEN`. `JwtAuthenticationFilter`는 Authorization 헤더를 읽지 않음 (JwtAuthenticationFilter.java:33).
- `JwtAuthenticationFilter`는 authorities를 null로 설정(…:45-46) → `hasRole`/`@PreAuthorize` 류 사용 불가. 관리자 체크는 반드시 `userQueryService.checkAdminRole(email)` 패턴을 따를 것.
- `JwtTokenProvider.validateToken`은 만료/위조 시 `ExpiredTokenException`/`InvalidTokenException`을 던지며, `JwtAuthenticationFilter`(…:36)에서 catch하지 않음.
- 쿠키 maxAge(ACCESS 30분 / REFRESH 24시간)와 JWT·Redis TTL(application.yml:23-24 `jwt.expiration`/`jwt.refreshExpiration` 모두 3600000ms)이 서로 다름. application-prod.yml의 `jwt.token-validity-in-seconds`는 JwtTokenProvider에서 사용하지 않음.
- `/auth/refresh`(TokenRefreshFilter)와 `/admin/auth/refresh`는 새 refresh 토큰을 Redis에 저장하지만 쿠키로 내려주지 않고, 기존 refresh 토큰도 삭제하지 않음.
- 실제 로그인 경로는 `AuthService.login` (FilterAuthService는 어디서도 참조되지 않음). `AuthService.login`은 `isVerified`를 확인하지 않음(FilterAuthService는 확인).
- `AuthRedisService.addAccessToken`은 `gender.toString()`, `role.toString()` 호출(AuthRedisService.java:63-64) → gender가 null인 사용자는 저장 실패 → `UserException(USER_NOT_FOUND, "토큰 저장 중 오류")`. `UserCommandService.convertToGender`는 male/M/female/F 외 값이면 null 반환.
- `AdminAuthController`의 `catch (SecurityException e)`는 import가 없어 `java.lang.SecurityException`. `checkAdminRole`은 `UserException`을 던지므로 이 catch에 걸리지 않고 외부 `catch (Exception)`(500)으로 감. 또 adminLogin은 토큰 발급(Redis 저장) 후에 관리자 여부를 확인함(AdminAuthController.java:53-57).
- `AdminAuthController.expireCookie`는 `secure(false)`(…:318), 다른 곳(LogoutAuthenticationFilter)은 `secure(true)`.
- OAuth 임시 토큰은 서명 없는 `Base64(email|timestamp)` (OAuth2AuthenticationSuccessHandler.java:128-131), `/oauth/complete-signup`은 이를 디코드만 하고 `setVerified(true)` 처리.
- OAuth 신규 가입 리다이렉트 경로 `/oauth/complete-signup?token=`(상대경로, GET)인데 OAuthController에는 POST 매핑만 존재. 최종 리다이렉트 URL `http://www.pinjun.xyz` 하드코딩.
- `OAuth2AuthorizationController`는 Security의 `/oauth2/authorization` baseUri와 같은 경로를 매핑.

**엔티티/데이터**
- `User.addReferrer`는 자기 `referralCode`를 추천인의 코드로 덮어씀 (User.java:254-256). referralCode는 `unique=true`. 또한 `referrer` 매핑은 `insertable=false, updatable=false`(User.java:97)라 JPA로는 추천인 FK가 저장되지 않음.
- `User.calculateTotalPoints()`는 `Point::getAmount`(최초 금액) 합산(User.java:347-350), `PointRepository.getTotalAvailablePoints`는 `remainingAmount` 합산 → admin 계열(AdminPointService, AdminUserService)과 사용자 계열(PointCommand/QueryService)의 가용 포인트 계산이 다름.
- 포인트 기본 만료: `PointCommandService.addPoint` 90일, `Point` 생성자 90일, `AdminPointService.addPointByAdmin` 기본 1년.
- `Profile` 필드명이 대문자 `Name`(Profile.java:32), `ProfileUpdateDto`도 `Name`.
- `PointDto.java`는 `dto/point/` 폴더에 있지만 package는 `com.fream.back.domain.user.dto`.

**미사용/중복/미완성 코드**
- `service/command/ProfileService.java`: `ProfileCommandService.createDefaultProfile`과 거의 중복, 참조 없음(save·Name 설정도 없음).
- `service/command/UserSanctionService.java`: 빈 클래스(어노테이션 없음). `validate/{BlockedProfile,Follow,Profile}ControllerValidator`: 빈 클래스.
- `UserControllerValidator.validateUserRegistrationDto`, `validateLoginRequestDto`는 호출처 없음(회원가입은 검증기 없이 UserCommandService 내부 검증만).
- `AdminSanctionService.processExpiredSanctions()`: 호출처/스케줄러 없음 → 제재 자동 만료 안 됨.
- `AdminGradeService.runGradeAutoAssignment`: 구매액을 `0`으로 하드코딩(AdminGradeService.java:182). `AdminPointService.getPointStatistics`: 0 반환(미구현).
- `OAuthAttributes.extractProviderId`(private) 미사용. `CustomUserDetailsService`는 직접 참조 없음, 항상 `roles("USER")`.
- `UserQueryController`의 `/users/update-login-info` 블록 전체 주석 처리 → 현재는 `PUT /users/update`(UserCommandController) 사용.

**기타**
- `ProfileController.updateProfile`은 `profileImage`가 `required=false`인데 `profileImage.getContentType()`을 바로 호출(ProfileController.java:49) → 이미지 없이 호출 시 NPE.
- `GET /profiles/{profileId}/image`는 `/home/ubuntu/fream/profile_images` 하드코딩 경로를 직접 읽음, 화이트리스트에 없으므로 인증 필요.
- `AdminSanctionController` create/review는 기록용 관리자 이메일을 SecurityContext가 아닌 `X-Admin-Email` 헤더에서 받음. `AdminUserService.updateUserStatus`의 제재 기록은 `createdBy("admin")` 하드코딩.
- 로그인 차단은 `User.isActive`만 확인 → 제재는 `reviewSanction` 승인 시 TEMPORARY/PERMANENT_BAN이면 `isActive=false`로 반영됨. 상태 PENDING 제재는 영향 없음.
- 시스템 오류(토큰 저장, 메일 발송, 회원가입 실패 등)도 `UserErrorCode.USER_NOT_FOUND`(404)로 래핑하는 곳이 많음(AuthService, AuthRedisService, UserCommandService 등).
- 회원가입 시 phoneNumber는 DTO 값이 아니라 본인인증 결과(`verifiedCustomer.getPhoneNumber()`)로 저장.
- 비밀번호 찾기(`/users/reset-password-sandEmail`)는 메일 발송 성공 후 비밀번호 변경, 실패 시 false 반환.
- 전화번호 형식 검증은 `^010-\d{4}-\d{4}$` (UserControllerValidator.java:14).
