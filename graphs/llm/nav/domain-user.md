# domain `user` 네비게이션

> 인증/JWT/필터 체인/OAuth2/IP 차단 흐름은 [flow-auth-security.md](flow-auth-security.md) 참고.

## 역할 요약
- 회원(User) 엔티티와 회원가입(PortOne 본인인증), 로그인 정보 수정, 비밀번호 찾기/재설정, 회원 탈퇴를 담당한다.
- 부가 기능: 프로필(Profile)·팔로우·프로필 차단·정산 계좌(BankAccount)·포인트(Point, 90일 만료)·회원 등급(UserGrade)·제재(UserSanction).
- 관리자 API(`/admin/users/**`, `/admin/auth/**`)와 관리자 권한 체크(`UserQueryService.checkAdminRole`)를 제공하며, 다른 도메인 대부분이 이 메서드와 `UserQueryService.findByEmail`에 의존한다.

## 패키지/파일 맵
경로 접두어: `src/main/java/com/fream/back/domain/user/`

### controller
| 파일 | 목적 |
|---|---|
| `controller/command/UserCommandController.java` | `/users` 회원가입, 본인인증 테스트, 로그인정보 수정(+이메일 변경 시 토큰 재발급) |
| `controller/query/UserQueryController.java` | `/users` 이메일 찾기, 비밀번호 찾기/재설정, 로그인정보 조회, 회원탈퇴 (하단에 주석 처리된 구 `update-login-info` 존재) |
| `controller/command/AuthController.java` | `/auth` **@Deprecated**. 실제 로그인/로그아웃/리프레시는 필터가 처리, 컨트롤러 메서드는 400 안내만 반환 |
| `controller/command/AdminAuthController.java` | `/admin/auth` 관리자 로그인/리프레시/로그아웃/이메일·비번 찾기/토큰 체크 |
| `controller/command/OAuthController.java` | `/oauth` OAuth 가입 완료(complete-signup), 상태, 연결 목록 |
| `controller/command/OAuth2AuthorizationController.java` | `GET /oauth2/authorization/{registrationId}` 로깅용 (실제 리다이렉트는 Spring Security) |
| `controller/command/IdentityVerificationController.java` | `/identity-verification/verify` PortOne 본인인증 조회 |
| `controller/ProfileController.java` | `/profiles` 내 프로필 조회/수정(multipart), 프로필 이미지 바이트 제공 |
| `controller/FollowController.java` | `/follows` 팔로우/언팔로우/목록 |
| `controller/BlockedProfileController.java` | `/profiles/blocked` 차단/해제/목록 |
| `controller/BankAccountController.java` | `/bank-account` 정산 계좌 CRUD |
| `controller/point/PointCommandController.java` | `/points/commands` 포인트 적립/사용 |
| `controller/point/PointQueryController.java` | `/points/queries` 포인트 내역/요약 |
| `controller/admin/AdminUserController.java` | `/admin/users` 회원 검색/상세/상태/등급/권한/포인트/CSV |
| `controller/admin/AdminGradeController.java` | `/admin/users/grades` 등급 CRUD/통계/자동배정 |
| `controller/admin/AdminPointController.java` | `/admin/users/points` 포인트 조회/지급/차감/통계 |
| `controller/admin/AdminSanctionController.java` | `/admin/users/sanctions` 제재 검색/생성/수정/검토/취소/통계 |

### service
| 파일 | 목적 |
|---|---|
| `service/command/AuthService.java` | `login(LoginRequestDto, ip)` – 비밀번호/활성 검증 후 `JwtTokenProvider.generateTokenPair` |
| `service/command/UserCommandService.java` | `registerUser`, `deleteAccount` |
| `service/command/UserUpdateService.java` | `updateLoginInfo`, `reissueTokenAfterEmailChange` |
| `service/command/PasswordResetService.java` | 비밀번호 재설정 자격확인/재설정/임시비번 메일 발송(일반/관리자) |
| `service/command/EmailService.java` | `JavaMailSender` HTML 메일 발송 |
| `service/command/IdentityVerificationService.java` | PortOne `GET https://api.portone.io/identity-verifications/{id}` 호출 (`imp.secret`) |
| `service/command/ProfileService.java` | `createDefaultProfile` – **미사용 중복 클래스** (실사용은 `ProfileCommandService`) |
| `service/command/UserSanctionService.java` | 빈 클래스 (내용 없음, `@Service` 아님) |
| `service/query/UserQueryService.java` | `checkAdminRole`, `findByEmail`, `findEmailByPhoneNumber`, `getLoginInfo` |
| `service/profile/ProfileCommandService.java` | 기본 프로필 생성, 프로필 수정(이미지 저장/삭제) |
| `service/profile/ProfileQueryService.java` | 프로필 정보/엔티티 조회, 이미지 파일명 조회 |
| `service/follow/FollowCommandService.java` / `FollowQueryService.java` | 팔로우 생성/삭제(알림 발송), 팔로워/팔로잉 페이지 |
| `service/BlockProfile/BlockedProfileCommandService.java` / `BlockedProfileQueryService.java` | 차단/해제, 차단 목록 |
| `service/bankaccount/BankAccountCommandService.java` / `BankAccountQueryService.java` | 계좌 생성·수정/삭제, 조회 |
| `service/point/PointCommandService.java` / `PointQueryService.java` | 포인트 적립/사용/만료, 내역/요약 |
| `service/admin/AdminUserService.java` | 회원 검색(Specification)/상세/상태/등급/권한/포인트관리/CSV export |
| `service/admin/AdminGradeService.java` | 등급 CRUD, 통계, 자동배정 |
| `service/admin/AdminPointService.java` | 관리자 포인트 조회/지급/차감/통계 |
| `service/admin/AdminSanctionService.java` | 제재 CRUD/검토/취소/통계, `processExpiredSanctions` |

### 기타
| 파일 | 목적 |
|---|---|
| `entity/*.java` | User, Profile, Follow, BlockedProfile, BankAccount, Point, OAuthConnection, UserGrade, UserSanction + enum(Gender, Role, ShoeSize, PointStatus, SanctionType, SanctionStatus) |
| `repository/*.java` | JPA 리포지토리 + `UserSanctionSpecifications` |
| `redis/AuthRedisService.java` | Access/Refresh 토큰 화이트리스트(Redis Hash) |
| `security/CustomUserDetailsService.java` | `UserDetailsService` 구현 (항상 ROLE `USER`) – 사용처 미확인 |
| `security/oauth2/CustomOAuth2UserService.java`, `OAuth2AuthenticationSuccessHandler.java`, `OAuthAttributes.java` | OAuth2(google/naver/kakao) 로그인 |
| `scheduler/PointExpirationScheduler.java` | 포인트 만료 스케줄러 |
| `validate/UserControllerValidator.java`, `BankAccountControllerValidator.java` | 정적 DTO 검증. `BlockedProfile/Follow/ProfileControllerValidator`는 빈 클래스 |
| `utils/PasswordUtils.java` | 임시 비밀번호 생성 |
| `dto/**` | 요청/응답 DTO (`UserManagementDto`, `UserGradeDto`, `SanctionDto`, `PointDto`는 내부 static 클래스 묶음) |
| `exception/**` | `UserErrorCode`, `UserException` 및 하위 예외 |

## REST 엔드포인트
현재 사용자 이메일은 모두 `SecurityContext` principal(String)에서 추출 (아래 "현재 사용자" 참고). 관리자 API는 매 메서드 첫 줄에서 `userQueryService.checkAdminRole(email)` 호출.

### 일반 사용자
| Method | Path | Controller 메서드 | Service 메서드 |
|---|---|---|---|
| POST | `/users/register` | `UserCommandController.registerUser` | `UserCommandService.registerUser` |
| POST | `/users/verify-identity` | `UserCommandController.verifyIdentity` | `IdentityVerificationService.verifyIdentity` |
| PUT | `/users/update` | `UserCommandController.updateUserInfo` | `UserUpdateService.updateLoginInfo` → `UserQueryService.getLoginInfo` → (이메일 변경 시) `UserUpdateService.reissueTokenAfterEmailChange` |
| POST | `/users/find-email` | `UserQueryController.findEmail` | `UserQueryService.findEmailByPhoneNumber` |
| POST | `/users/reset-password` | `UserQueryController.resetPasswordEligibility` | `PasswordResetService.checkPasswordResetEligibility` |
| POST | `/users/reset-password-sandEmail` | `UserQueryController.resetPasswordToEmail` | `PasswordResetService.checkPasswordResetAndSendEmail` |
| POST | `/users/reset` | `UserQueryController.resetPassword` | `PasswordResetService.resetPassword` |
| GET | `/users/login-info` | `UserQueryController.getLoginInfo` | `UserQueryService.getLoginInfo` |
| DELETE | `/users/delete-account` | `UserQueryController.deleteAccount` | `UserCommandService.deleteAccount` |
| POST | `/identity-verification/verify` | `IdentityVerificationController.verifyIdentity` | `IdentityVerificationService.verifyIdentity` |
| GET | `/profiles` | `ProfileController.getProfile` | `ProfileQueryService.getProfileInfo` |
| PUT | `/profiles` (multipart) | `ProfileController.updateProfile` | `ProfileCommandService.updateProfile` |
| GET | `/profiles/{profileId}/image` | `ProfileController.getProfileImage` | `ProfileQueryService.getProfileImageFileName` (+ `/home/ubuntu/fream/profile_images/` 직접 파일 읽기) |
| POST | `/profiles/blocked` (body `{blockedProfileId}`) | `BlockedProfileController.blockProfile` | `BlockedProfileCommandService.blockProfile` |
| DELETE | `/profiles/blocked?blockedProfileId=` | `BlockedProfileController.unblockProfile` | `BlockedProfileCommandService.unblockProfile` |
| GET | `/profiles/blocked` | `BlockedProfileController.getBlockedProfiles` | `BlockedProfileQueryService.getBlockedProfiles(email)` |
| POST | `/follows/{profileId}` | `FollowController.createFollow` | `FollowCommandService.createFollow` |
| DELETE | `/follows/{profileId}` | `FollowController.deleteFollow` | `FollowCommandService.deleteFollow` |
| GET | `/follows/followers` | `FollowController.getFollowers` | `FollowQueryService.getFollowers` |
| GET | `/follows/followings` | `FollowController.getFollowings` | `FollowQueryService.getFollowings` |
| POST | `/bank-account` | `BankAccountController.createOrUpdateBankAccount` | `BankAccountCommandService.createOrUpdateBankAccount` |
| GET | `/bank-account` | `BankAccountController.getBankAccount` | `BankAccountQueryService.getBankAccount` |
| DELETE | `/bank-account` | `BankAccountController.deleteBankAccount` | `BankAccountCommandService.deleteBankAccount` |
| POST | `/points/commands` | `PointCommandController.addPoint` | `PointCommandService.addPoint` |
| POST | `/points/commands/use` | `PointCommandController.usePoint` | `PointCommandService.usePoint` |
| GET | `/points/queries` | `PointQueryController.getAllPointHistory` | `PointQueryService.getAllPointHistory` |
| GET | `/points/queries/available` | `PointQueryController.getAvailablePoints` | `PointQueryService.getAvailablePoints` |
| GET | `/points/queries/summary` | `PointQueryController.getPointSummary` | `PointQueryService.getPointSummary` |
| GET | `/points/queries/{pointId}` | `PointQueryController.getPointDetail` | `PointQueryService.getPointDetail` |

### 인증 관련 (상세는 flow-auth-security.md)
| Method | Path | 처리 주체 |
|---|---|---|
| POST | `/auth/login`, `/auth/logout`, `/auth/refresh` | 필터(`LoginAuthenticationFilter` 등)가 가로챔. `AuthController`의 동명 메서드는 도달 시 400(DEPRECATED) |
| GET | `/auth/email`, `/auth/status` | `AuthController` – 미구현 안내만 반환 |
| POST | `/admin/auth/login` | `AdminAuthController.adminLogin` → `AuthService.login` + `UserQueryService.checkAdminRole` |
| POST | `/admin/auth/find-email` | `AdminAuthController.findAdminEmail` → `UserQueryService.findEmailByPhoneNumber`, `checkAdminRole` |
| GET | `/admin/auth/check` | `AdminAuthController.checkAdminToken` |
| POST | `/admin/auth/refresh` | `AdminAuthController.refreshAdminToken` → `JwtTokenProvider.generateTokenPair` |
| POST | `/admin/auth/find-password` | `AdminAuthController.findAdminPassword` → `PasswordResetService.AdminCheckPasswordResetAndSendEmail` |
| POST | `/admin/auth/logout` | `AdminAuthController.logout` → `AuthRedisService.removeAccessToken/removeRefreshToken` |
| POST | `/oauth/complete-signup` | `OAuthController.completeOAuthSignup` (컨트롤러에서 직접 `UserRepository`/엔티티 수정, `@Transactional`) |
| GET | `/oauth/status` | `OAuthController.checkAuthStatus` |
| GET | `/oauth/connections` | `OAuthController.getOAuthConnections` |
| GET | `/oauth2/authorization/{registrationId}` | `OAuth2AuthorizationController.redirectToOAuthProvider` (Spring Security OAuth2 필터가 먼저 처리) |

### 관리자 (`checkAdminRole` 필수)
| Method | Path | Controller 메서드 | Service 메서드 |
|---|---|---|---|
| GET | `/admin/users/search` | `AdminUserController.searchUsers` | `AdminUserService.searchUsers` |
| GET | `/admin/users/{userId}` | `getUserDetail` | `AdminUserService.getUserDetail` |
| PATCH | `/admin/users/{userId}/status` | `updateUserStatus` | `AdminUserService.updateUserStatus` |
| PATCH | `/admin/users/{userId}/grade` | `updateUserGrade` | `AdminUserService.updateUserGrade` |
| PATCH | `/admin/users/{userId}/role` | `updateUserRole` | `AdminUserService.updateUserRole` |
| POST | `/admin/users/{userId}/points` | `manageUserPoints` | `AdminUserService.manageUserPoints` |
| GET | `/admin/users/export` | `exportUserData` | `AdminUserService.exportUsersCsv` |
| GET | `/admin/users/grades` | `AdminGradeController.getAllGrades` | `AdminGradeService.getAllGrades` |
| GET | `/admin/users/grades/{gradeId}` | `getGradeById` | `getGradeById` |
| POST | `/admin/users/grades` | `createGrade` | `createGrade` |
| PUT | `/admin/users/grades/{gradeId}` | `updateGrade` | `updateGrade` |
| DELETE | `/admin/users/grades/{gradeId}` | `deleteGrade` | `deleteGrade` |
| GET | `/admin/users/grades/counts` | `getGradeUserCounts` | `getGradeUserCounts` |
| GET | `/admin/users/grades/statistics` | `getGradeStatistics` | `getGradeStatistics` |
| POST | `/admin/users/grades/auto-assign` | `runGradeAutoAssignment` | `runGradeAutoAssignment` |
| GET | `/admin/users/points/user/{userId}` | `AdminPointController.getUserPointHistory` | `AdminPointService.getUserPointHistory` |
| GET | `/admin/users/points/user/{userId}/available` | `getUserAvailablePoints` | `getUserAvailablePoints` |
| GET | `/admin/users/points/user/{userId}/summary` | `getUserPointSummary` | `getUserPointSummary` |
| GET | `/admin/users/points/{pointId}` | `getPointDetail` | `getPointDetail` |
| POST | `/admin/users/points/user/{userId}/add` | `addPointByAdmin` | `addPointByAdmin` |
| POST | `/admin/users/points/user/{userId}/deduct` | `deductPointByAdmin` | `deductPointByAdmin` |
| GET | `/admin/users/points/statistics` | `getPointStatistics` | `getPointStatistics(startDate, endDate)` |
| GET | `/admin/users/sanctions/search` | `AdminSanctionController.searchSanctions` | `AdminSanctionService.searchSanctions` |
| GET | `/admin/users/sanctions/{sanctionId}` | `getSanctionById` | `getSanctionById` |
| POST | `/admin/users/sanctions` | `createSanction` | `createSanction(dto, adminEmail)` |
| PUT | `/admin/users/sanctions/{sanctionId}` | `updateSanction` | `updateSanction` |
| PATCH | `/admin/users/sanctions/{sanctionId}/review` | `reviewSanction` | `reviewSanction(id, dto, adminEmail)` |
| PATCH | `/admin/users/sanctions/{sanctionId}/cancel` | `cancelSanction` | `cancelSanction` |
| GET | `/admin/users/sanctions/user/{userId}` | `getUserSanctions` | `getUserSanctions` |
| GET | `/admin/users/sanctions/statistics` | `getSanctionStatistics` | `getSanctionStatistics` |

## 핵심 엔티티와 관계
| 엔티티 (테이블) | 주요 필드 | 연관관계 |
|---|---|---|
| `User` (`users`) | `email`(unique, not null), `password`(BCrypt), `referralCode`(unique, not null), `phoneNumber`(not null), `shoeSize`, `termsAgreement`, `phoneNotificationConsent`, `emailNotificationConsent`, `optionalPrivacyAgreement`, `isVerified`(default false), `ci`, `di`, `role`, `sellerGrade`, `age`, `gender`, `isActive`(default true, `BIT(1) DEFAULT 1`) | `profile` 1:1(mappedBy, cascade ALL), `interests`/`addresses`/`paymentInfos`/`points`/`oauthConnections` 1:N(cascade ALL, orphanRemoval), `bankAccount` 1:1, `referrer` N:1 (`@JoinColumn(name="referrerCode", referencedColumnName="referralCode", insertable=false, updatable=false)`), `referredUsers` 1:N, `grade` N:1 → `UserGrade`(`grade_id`) |
| `Profile` | `profileName`(unique, not null), `Name`(대문자 필드명), `bio`, `isPublic`, `profileImageUrl` | `user` 1:1(`user_id`), `followings`(mappedBy follower)/`followers`(mappedBy following) → `Follow`, `blockedByProfiles` → `BlockedProfile`, `styles` → style 도메인 `Style` (모두 cascade ALL, orphanRemoval) |
| `Follow` (`follows`) | – | `follower` N:1 Profile, `following` N:1 Profile |
| `BlockedProfile` | – | `profile`(차단한 쪽), `blockedProfile`(차단된 쪽) N:1 Profile |
| `BankAccount` | `bankName`, `accountNumber`, `accountHolder` | `user` 1:1 |
| `Point` | `amount`, `remainingAmount`, `reason`, `expirationDate`(생성자 기본 +90일), `status` | `user` N:1 |
| `OAuthConnection` | `provider`("google"/"naver"/"kakao"), `providerId`, `accessToken`, `connectedAt` | `user` N:1 (BaseTimeEntity 미상속) |
| `UserGrade` (`user_grades`) | `level`(unique), `name`(unique), `description`, `minPurchaseAmount`, `pointRate`(not null), `benefits` | `users` 1:N(mappedBy grade) |
| `UserSanction` (`user_sanctions`) | `reason`, `details`, `type`, `status`, `startDate`, `endDate`(영구정지 null), `targetId`, `targetType`, `createdBy`, `approvedBy`, `rejectedBy`, `rejectionReason` | `user` N:1 |

Enum 값
- `Role`: `USER`, `ADMIN`
- `Gender`: `MALE`, `FEMALE`, `OTHER`
- `ShoeSize`: `SIZE_220` ~ `SIZE_300` (5 단위, `getValue()`)
- `PointStatus`: `AVAILABLE`, `USED`, `EXPIRED`
- `SanctionType`: `WARNING`, `TEMPORARY_BAN`, `PERMANENT_BAN`, `FEATURE_RESTRICTION`
- `SanctionStatus`: `PENDING`, `ACTIVE`, `EXPIRED`, `REJECTED`, `CANCELED`

엔티티 상태 메서드: `Point.use(int)`(AVAILABLE만, 전부 소진 시 USED), `Point.expire()`(AVAILABLE→EXPIRED, remaining 0), `Point.isValid()`; `UserSanction.approve/reject/cancel/expire`; `User.addActive(boolean)`, `changeRole`, `addGrade`, `setVerified`, `updateUser(...)`(7/9인자 오버로드), `updateLoginInfo(...)`, `updateConsent`, `addOAuthConnection`, `addReferrer`.

## 주요 서비스 메서드와 비즈니스 규칙
| 메서드 | 규칙 / 예외 / 트랜잭션 |
|---|---|
| `UserCommandService.registerUser` (`@Transactional`) | `isOver14`·`termsAgreement`·`privacyAgreement` 모두 true 필요(아니면 `UserException(INVALID_VERIFICATION_CODE)`), 이메일 중복 → `DuplicateEmailException`, `identityVerificationId` 필수 → PortOne 검증 실패 시 `IdentityVerificationFailedException`, CI 중복 → `DuplicateEmailException`, 생년월일로 만 14세 검증, 추천인 코드가 없으면 `UserException(USER_NOT_FOUND)`. 전화번호/성별/나이는 **본인인증 결과**로 저장, `isVerified=true`, 8자리 대문자 UUID 추천코드(최대 10회 재시도). 저장 후 `ProfileCommandService.createDefaultProfile`. 기타 예외는 `UserException(USER_NOT_FOUND, "회원가입 처리 중...")`로 래핑 |
| `UserCommandService.deleteAccount` | 알림 삭제(`NotificationCommandService.deleteNotificationsByUser`)·프로필 이미지 파일 삭제 실패는 무시 후 `userRepository.delete` (cascade로 연관 삭제) |
| `UserUpdateService.updateLoginInfo` | 새 이메일 중복 → `DuplicateEmailException`; `Password`+`newPassword` 둘 다 있으면 현재 비번 검증(`InvalidPasswordException`); `newShoeSize`는 `ShoeSize.valueOf` |
| `UserUpdateService.reissueTokenAfterEmailChange` | 새 이메일로 사용자 조회 → 구 토큰 Redis 삭제 → `generateTokenPair` |
| `AuthService.login` (`@Transactional`) | 사용자 없음 `UserNotFoundException`, 비번 불일치 `InvalidPasswordException`, `!isActive` → `UserException(ACCOUNT_INACTIVE)`. `isVerified`는 검사하지 않음 |
| `UserQueryService.checkAdminRole` (readOnly) | 사용자 없음 `UserNotFoundException`, `role != ADMIN` → `UserException(INSUFFICIENT_PERMISSIONS)` (403) |
| `PasswordResetService.resetPassword` | `PasswordResetRequestDto.validatePasswords()` 실패 → `InvalidPasswordException`; email+phone 일치 사용자 필요 |
| `PasswordResetService.checkPasswordResetAndSendEmail` / `AdminCheck...` | 임시 비밀번호 생성(`PasswordUtils`) → 메일 발송 성공 후에만 비번 변경. 모든 예외는 `false` 반환 |
| `PointCommandService.addPoint` | amount 1~1,000,000, reason 필수(`InvalidPointAmountException`), 만료 +90일 |
| `PointCommandService.usePoint` | 합계(`getTotalAvailablePoints`) 부족 → `InsufficientPointsException`; 만료 임박순(FIFO)으로 `Point.use` 차감, 사용된 Point의 `reason`을 사용 사유로 덮어씀 |
| `PointCommandService.expirePoints` | `status=AVAILABLE AND expirationDate < today` → `expire()`; 예외 시 0 반환 |
| `ProfileCommandService.updateProfile` | 이미지 5MB 이하(`ProfileImageTooLargeException`), `image/jpeg|png|gif`만(`InvalidProfileImageException`), `FileUtils.saveFile("profile_images", "profile_{id}_", ...)`, 기존 이미지 삭제 |
| `FollowCommandService.createFollow` | 자기 자신 → `CannotFollowSelfException`, 중복 → `AlreadyFollowingException`, 성공 시 `NotificationCommandService.createNotification(..., STYLE, FOLLOW, ...)` (실패 무시) |
| `BlockedProfileCommandService.blockProfile` | 자기 자신 → `CannotBlockSelfException`, 중복 → `AlreadyBlockedException` |
| `AdminSanctionService.createSanction` | PERMANENT_BAN 외에는 `endDate` 필수, 상태 `PENDING`으로 생성 (자동승인 코드 주석 처리됨) |
| `AdminSanctionService.updateSanction` / `reviewSanction` | `PENDING`만 가능. 승인 시 BAN 유형이면 `user.addActive(false)`; 거절 시 사유 필수 |
| `AdminSanctionService.cancelSanction` | `ACTIVE`만 가능; BAN 유형이면 (다른 활성 BAN이 없을 때) `addActive(true)` |
| `AdminUserService.updateUserStatus` | `addActive(status)`; 비활성+사유 시 `TEMPORARY_BAN/ACTIVE` 제재 기록 (`createdBy="admin"` 하드코딩) |
| `AdminGradeService.createGrade/updateGrade/deleteGrade` | level/name 중복, 사용자 있는 등급 삭제 불가 → `InvalidRequestException` |

## 다른 도메인과의 의존
### 이 도메인을 호출하는 곳 (grep 확인)
| 대상 | 호출하는 곳 |
|---|---|
| `UserQueryService` (`checkAdminRole`, `findByEmail`) | order(`OrderCommandService`, `OrderBidCommandService`, `kafka/OrderEventConsumer`), sale(`SaleCommandService`, `SaleBidCommandService`), payment(`kafka/PaymentEventConsumer`), product 컨트롤러 다수 + `InterestCommandService`, event/notice/faq/inquiry/inspection 컨트롤러, chatQuestion(`ChatService`, `GPTUsageController`, `aop/ChatSecurityAspect`). `checkAdminRole`은 user 외부에서 약 40회 호출 |
| `UserRepository` | inquiry(`InquiryCommandServiceImpl`, `InquiryQueryServiceImpl`), payment(`PaymentInfoCommandService`), address(`AddressCommandService`, `AddressQueryService`), notification(`NotificationCommandService`), `global/config/DataInitializer`, `global/config/websocket/WebSocketConfig`, `WebSocketAuthInterceptor` |
| `ProfileQueryService` | style 도메인 command/query 서비스 10개 |
| `BankAccountQueryService` | `domain/sale/service/command/SaleCommandService` |
| `ProfileCommandService`, `ProfileRepository`, `BankAccountRepository` | `global/config/DataInitializer` |
| `User`/`Profile` 엔티티 FK | Order, OrderBid, Sale(seller), SaleBid(seller), Payment, PaymentInfo, Address, Interest, Notification, Inquiry, ChatQuestion, GPTUsageLog, WarehouseStorage → `User`; Style, StyleLike, StyleComment, StyleCommentLike, StyleInterest → `Profile` |
| `UserException` | `global/exception/GlobalExceptionHandler.handleUserException` |

### 이 도메인이 호출하는 것
- `domain/notification/service/command/NotificationCommandService` (`deleteNotificationsByUser`, `createNotification`)
- `global/utils/FileUtils` (프로필 이미지 저장/삭제)
- `global/config/security/JwtTokenProvider` (`AuthService`, `UserUpdateService`, OAuth 컨트롤러)
- 외부: PortOne API(`RestTemplate`), `JavaMailSender`
- 엔티티 참조: `Interest`(product), `Address`(address), `PaymentInfo`(payment), `Style`(style)

### Redis / 스케줄러 / Kafka / 캐시
- Redis: `AuthRedisService` – `access:{accessToken}` Hash(email, age, gender, role, ip), `refresh:{refreshToken}` Hash(email). 쓰기 `writeRedisTemplate`, 읽기 `readRedisTemplate`(실패/누락 시 write로 fallback).
- 스케줄러: `PointExpirationScheduler.checkExpiredPoints` – `@Scheduled(cron = "0 0 * * * *")` (매시 정각) → `PointCommandService.expirePoints`.
- Kafka / `@Cacheable`: user 도메인 내 없음 (grep 확인).

## 예외/에러코드 위치
- `domain/user/exception/UserErrorCode.java` – 코드 체계: U0xx 일반, U1xx 계정, U2xx 프로필, U3xx 포인트, U4xx 팔로우, U5xx 차단, U6xx 계좌, U701 등급, U8xx 제재, U9xx OAuth.
- `domain/user/exception/UserException.java` (extends `global/exception/GlobalException`) 및 하위 예외 22개.
- 관리자 서비스(`Admin*Service`)는 주로 `global/exception/EntityNotFoundException`, `InvalidRequestException` 사용.
- 핸들러: `src/main/java/com/fream/back/global/exception/GlobalExceptionHandler.java` (`@ExceptionHandler(UserException.class)`).

## 관련 테스트
| 파일 | 대상 |
|---|---|
| `src/test/java/com/fream/back/domain/user/service/command/UserCommandServiceTest.java` | 회원가입/탈퇴 |
| `src/test/java/com/fream/back/domain/user/service/point/PointCommandServiceTest.java` | 포인트 적립/사용/만료 |
| `src/test/java/com/fream/back/domain/user/service/follow/FollowCommandServiceTest.java` | 팔로우 |
| `src/test/java/com/fream/back/domain/user/service/BlockProfile/BlockedProfileCommandServiceTest.java` | 차단 |
| `src/test/java/com/fream/back/domain/user/service/admin/AdminSanctionServiceTest.java` | 제재 |
| `src/test/java/com/fream/back/domain/user/repository/UserRepositoryTest.java`, `PointRepositoryTest.java` | 리포지토리 |
| `src/test/java/com/fream/back/domain/user/entity/PointTest.java` | `Point` 엔티티 |
- 보안 스택(필터, `JwtTokenProvider`, `AuthRedisService`) 전용 테스트는 없음.

## 자주 할 작업 → 여기를 보라
| 작업 | 파일 |
|---|---|
| 회원가입 검증 규칙 변경 | `service/command/UserCommandService.java#registerUser`, `dto/UserRegistrationDto.java` |
| 로그인 조건(비활성/미인증 차단 등) 변경 | `service/command/AuthService.java#login` |
| 관리자 권한 체크 로직 | `service/query/UserQueryService.java#checkAdminRole` |
| 포인트 만료 주기/로직 | `scheduler/PointExpirationScheduler.java`, `service/point/PointCommandService.java#expirePoints`, `entity/Point.java` |
| 포인트 차감 순서 | `PointCommandService.usePoint`, `repository/PointRepository.java` |
| 프로필 이미지 제한/경로 | `service/profile/ProfileCommandService.java#validateProfileImage`, `controller/ProfileController.java#getProfileImage`(baseDir 하드코딩) |
| 제재 상태 전이 | `service/admin/AdminSanctionService.java`, `entity/UserSanction.java` |
| 등급 자동배정 | `service/admin/AdminGradeService.java#runGradeAutoAssignment` |
| 본인인증(PortOne) 연동 | `service/command/IdentityVerificationService.java` |
| 비밀번호 찾기 메일 문구 | `service/command/PasswordResetService.java` |
| OAuth 가입 완료 | `controller/command/OAuthController.java#completeOAuthSignup` |

## Gotchas (코드에서 확인)
- **`User.addReferrer`가 자신의 `referralCode`를 추천인의 코드로 덮어씀**. `referralCode`는 unique 컬럼이고 `referrer` 조인 컬럼(`referrerCode`)은 `insertable=false, updatable=false`라서 추천인 관계가 정상 저장되는지 의심스러움(미확인). `registerUser`와 `OAuthController.completeOAuthSignup` 둘 다 호출함.
- `UserUpdateService.updateLoginInfo`: 현재 비밀번호 검증은 `Password`와 `newPassword`가 **둘 다** 있을 때만 수행. 마지막 `user.updateLoginInfo(...)`는 `newPassword`만 있어도 비밀번호를 변경함. `User.updateLoginInfo`의 `adConsent`/`privacyConsent` 분기는 빈 블록이라 무시됨.
- `User.calculateTotalPoints()`는 `remainingAmount`가 아니라 `amount`를 합산함 (`AdminPointService.deductPointByAdmin` 응답에서 사용).
- `POST /points/commands`는 일반 로그인 사용자가 자신에게 포인트(최대 100만)를 적립할 수 있는 API임.
- `AdminSanctionService.processExpiredSanctions()`는 정의돼 있지만 호출하는 스케줄러/코드가 없음. 기간 만료된 ACTIVE 제재가 자동으로 EXPIRED 처리되지 않음.
- `AdminGradeService.runGradeAutoAssignment`는 `totalPurchaseAmount = 0` 임시값을 사용함 (주문 금액 연동 안 됨).
- `AdminUserService.updateUserStatus`의 제재 기록은 `createdBy("admin")` 하드코딩.
- 중복/빈 클래스: `service/command/ProfileService`(미사용, `ProfileCommandService`와 중복), `service/command/UserSanctionService`(빈 클래스), `validate/BlockedProfile|Follow|ProfileControllerValidator`(빈 클래스).
- 현재 사용자 이메일 추출 방식이 섞여 있음: `SecurityUtils.extractEmailFromSecurityContext()`(UserCommand/UserQuery/Point 컨트롤러) vs 컨트롤러별 private `extractEmailFromSecurityContext()` 복붙(Admin*, Profile, Follow, BlockedProfile, BankAccount; `IllegalStateException` 발생).
- 많은 서비스가 원인과 무관한 `UserErrorCode.USER_NOT_FOUND`로 예외를 래핑함 (예: 토큰 저장 실패, 메일 전송 실패, 회원가입 일반 오류). HTTP 404로 응답될 수 있음.
- `ProfileController.getProfileImage`는 `/home/ubuntu/fream` baseDir 하드코딩. `/profiles/**`는 permitAll 목록에 없어 인증 필요.
- `AuthService.login`은 `isVerified`를 검사하지 않음 → OAuth 신규 사용자(`isVerified=false`)도 일반 로그인 API로는 막히지 않음(비번은 랜덤이라 실질적으로 로그인 불가).
- 패키지명 대소문자 불일치: `service/BlockProfile`, `dto/BankAccount` (대문자 시작 패키지).
- `UserQueryController` 하단에 주석 처리된 `PUT /users/update-login-info` 코드가 남아 있음 (현재는 `PUT /users/update` 사용).
