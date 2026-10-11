# user 도메인

기준 경로: `src/main/java/com/fream/back/domain/user/` · 파일 117개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/bank-account` | `BankAccountController#createOrUpdateBankAccount` | `this#extractEmailFromSecurityContext`, `BankAccountCommandService#createOrUpdateBankAccount` |
| GET | `/bank-account` | `BankAccountController#getBankAccount` | `this#extractEmailFromSecurityContext`, `BankAccountQueryService#getBankAccount` |
| DELETE | `/bank-account` | `BankAccountController#deleteBankAccount` | `this#extractEmailFromSecurityContext`, `BankAccountCommandService#deleteBankAccount` |
| POST | `/profiles/blocked` | `BlockedProfileController#blockProfile` | `this#extractEmailFromSecurityContext`, `BlockedProfileCommandService#blockProfile` |
| DELETE | `/profiles/blocked` | `BlockedProfileController#unblockProfile` | `this#extractEmailFromSecurityContext`, `BlockedProfileCommandService#unblockProfile` |
| GET | `/profiles/blocked` | `BlockedProfileController#getBlockedProfiles` | `this#extractEmailFromSecurityContext`, `BlockedProfileQueryService#getBlockedProfiles` |
| POST | `/follows/{profileId}` | `FollowController#createFollow` | `this#extractEmailFromSecurityContext`, `FollowCommandService#createFollow` |
| DELETE | `/follows/{profileId}` | `FollowController#deleteFollow` | `this#extractEmailFromSecurityContext`, `FollowCommandService#deleteFollow` |
| GET | `/follows/followers` | `FollowController#getFollowers` | `this#extractEmailFromSecurityContext`, `FollowQueryService#getFollowers` |
| GET | `/follows/followings` | `FollowController#getFollowings` | `this#extractEmailFromSecurityContext`, `FollowQueryService#getFollowings` |
| GET | `/profiles` | `ProfileController#getProfile` | `this#extractEmailFromSecurityContext`, `ProfileQueryService#getProfileInfo` |
| PUT | `/profiles` | `ProfileController#updateProfile` | `this#extractEmailFromSecurityContext`, `ProfileCommandService#updateProfile` |
| GET | `/profiles/{profileId}/image` | `ProfileController#getProfileImage` | `ProfileQueryService#getProfileImageFileName` |
| GET | `/admin/users/grades` | `AdminGradeController#getAllGrades` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `AdminGradeService#getAllGrades` |
| GET | `/admin/users/grades/{gradeId}` | `AdminGradeController#getGradeById` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `AdminGradeService#getGradeById` |
| POST | `/admin/users/grades` | `AdminGradeController#createGrade` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `AdminGradeService#createGrade` |
| PUT | `/admin/users/grades/{gradeId}` | `AdminGradeController#updateGrade` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `AdminGradeService#updateGrade` |
| DELETE | `/admin/users/grades/{gradeId}` | `AdminGradeController#deleteGrade` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `AdminGradeService#deleteGrade` |
| GET | `/admin/users/grades/counts` | `AdminGradeController#getGradeUserCounts` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `AdminGradeService#getGradeUserCounts` |
| GET | `/admin/users/grades/statistics` | `AdminGradeController#getGradeStatistics` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `AdminGradeService#getGradeStatistics` |
| POST | `/admin/users/grades/auto-assign` | `AdminGradeController#runGradeAutoAssignment` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `AdminGradeService#runGradeAutoAssignment` |
| GET | `/admin/users/points/user/{userId}` | `AdminPointController#getUserPointHistory` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `AdminPointService#getUserPointHistory` |
| GET | `/admin/users/points/user/{userId}/available` | `AdminPointController#getUserAvailablePoints` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `AdminPointService#getUserAvailablePoints` |
| GET | `/admin/users/points/user/{userId}/summary` | `AdminPointController#getUserPointSummary` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `AdminPointService#getUserPointSummary` |
| GET | `/admin/users/points/{pointId}` | `AdminPointController#getPointDetail` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `AdminPointService#getPointDetail` |
| POST | `/admin/users/points/user/{userId}/add` | `AdminPointController#addPointByAdmin` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `AdminPointService#addPointByAdmin` |
| POST | `/admin/users/points/user/{userId}/deduct` | `AdminPointController#deductPointByAdmin` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `AdminPointService#deductPointByAdmin` |
| GET | `/admin/users/points/statistics` | `AdminPointController#getPointStatistics` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `AdminPointService#getPointStatistics` |
| GET | `/admin/users/sanctions/search` | `AdminSanctionController#searchSanctions` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `AdminSanctionService#searchSanctions` |
| GET | `/admin/users/sanctions/{sanctionId}` | `AdminSanctionController#getSanctionById` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `AdminSanctionService#getSanctionById` |
| POST | `/admin/users/sanctions` | `AdminSanctionController#createSanction` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `AdminSanctionService#createSanction` |
| PUT | `/admin/users/sanctions/{sanctionId}` | `AdminSanctionController#updateSanction` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `AdminSanctionService#updateSanction` |
| PATCH | `/admin/users/sanctions/{sanctionId}/review` | `AdminSanctionController#reviewSanction` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `AdminSanctionService#reviewSanction` |
| PATCH | `/admin/users/sanctions/{sanctionId}/cancel` | `AdminSanctionController#cancelSanction` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `AdminSanctionService#cancelSanction` |
| GET | `/admin/users/sanctions/user/{userId}` | `AdminSanctionController#getUserSanctions` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `AdminSanctionService#getUserSanctions` |
| GET | `/admin/users/sanctions/statistics` | `AdminSanctionController#getSanctionStatistics` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `AdminSanctionService#getSanctionStatistics` |
| GET | `/admin/users/search` | `AdminUserController#searchUsers` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `AdminUserService#searchUsers` |
| GET | `/admin/users/{userId}` | `AdminUserController#getUserDetail` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `AdminUserService#getUserDetail` |
| PATCH | `/admin/users/{userId}/status` | `AdminUserController#updateUserStatus` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `AdminUserService#updateUserStatus` |
| PATCH | `/admin/users/{userId}/grade` | `AdminUserController#updateUserGrade` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `AdminUserService#updateUserGrade` |
| PATCH | `/admin/users/{userId}/role` | `AdminUserController#updateUserRole` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `AdminUserService#updateUserRole` |
| POST | `/admin/users/{userId}/points` | `AdminUserController#manageUserPoints` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `AdminUserService#manageUserPoints` |
| GET | `/admin/users/export` | `AdminUserController#exportUserData` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `AdminUserService#exportUsersCsv` |
| POST | `/admin/auth/login` | `AdminAuthController#adminLogin` | `this#getClientIp`, `AuthService#login`, `JwtTokenProvider#getEmailFromToken`, `UserQueryService#checkAdminRole`, `this#setCookie` |
| POST | `/admin/auth/find-email` | `AdminAuthController#findAdminEmail` | `UserQueryService#findEmailByPhoneNumber`, `UserQueryService#checkAdminRole` |
| GET | `/admin/auth/check` | `AdminAuthController#checkAdminToken` | `this#getCookieValue`, `JwtTokenProvider#validateToken`, `JwtTokenProvider#getEmailFromToken`, `UserQueryService#checkAdminRole` |
| POST | `/admin/auth/refresh` | `AdminAuthController#refreshAdminToken` | `this#getCookieValue`, `JwtTokenProvider#validateToken`, `AuthRedisService#isRefreshTokenValid`, `JwtTokenProvider#getEmailFromToken`, `UserQueryService#findByEmail`, `UserQueryService#checkAdminRole`, `this#getClientIp`, `JwtTokenProvider#generateTokenPair`, `this#setCookie` |
| POST | `/admin/auth/find-password` | `AdminAuthController#findAdminPassword` | `UserQueryService#findByEmail`, `PasswordResetService#AdminCheckPasswordResetAndSendEmail` |
| POST | `/admin/auth/logout` | `AdminAuthController#logout` | `this#getCookieValue`, `JwtTokenProvider#validateToken`, `JwtTokenProvider#getEmailFromToken`, `UserQueryService#checkAdminRole`, `AuthRedisService#removeAccessToken`, `AuthRedisService#removeRefreshToken`, `this#expireCookie` |
| POST | `/auth/login` | `AuthController#login` |  |
| POST | `/auth/logout` | `AuthController#logout` |  |
| POST | `/auth/refresh` | `AuthController#refresh` |  |
| GET | `/auth/email` | `AuthController#getUserEmail` |  |
| GET | `/auth/status` | `AuthController#checkAuthStatus` |  |
| POST | `/identity-verification/verify` | `IdentityVerificationController#verifyIdentity` | `IdentityVerificationService#verifyIdentity` |
| GET | `/oauth2/authorization/{registrationId}` | `OAuth2AuthorizationController#redirectToOAuthProvider` |  |
| POST | `/oauth/complete-signup` | `OAuthController#completeOAuthSignup` | `this#extractEmailFromToken`, `UserRepository#findByEmail`, `UserRepository#findByReferralCode`, `UserRepository#save~`, `this#getClientIp`, `JwtTokenProvider#generateTokenPair`, `this#setCookie` |
| GET | `/oauth/status` | `OAuthController#checkAuthStatus` | `this#getCookieValue`, `JwtTokenProvider#validateToken`, `AuthRedisService#isAccessTokenValid`, `JwtTokenProvider#getEmailFromToken` |
| GET | `/oauth/connections` | `OAuthController#getOAuthConnections` | `this#getCookieValue`, `JwtTokenProvider#getEmailFromToken`, `UserRepository#findByEmail` |
| POST | `/users/register` | `UserCommandController#registerUser` | `UserCommandService#registerUser` |
| POST | `/users/verify-identity` | `UserCommandController#verifyIdentity` | `IdentityVerificationService#verifyIdentity` |
| PUT | `/users/update` | `UserCommandController#updateUserInfo` | `UserUpdateService#updateLoginInfo`, `UserQueryService#getLoginInfo`, `this#getCookieValue`, `AuthRedisService#removeAccessToken`, `AuthRedisService#removeRefreshToken`, `UserUpdateService#reissueTokenAfterEmailChange`, `this#setCookie` |
| POST | `/points/commands` | `PointCommandController#addPoint` | `PointCommandService#addPoint` |
| POST | `/points/commands/use` | `PointCommandController#usePoint` | `PointCommandService#usePoint` |
| GET | `/points/queries` | `PointQueryController#getAllPointHistory` | `PointQueryService#getAllPointHistory` |
| GET | `/points/queries/available` | `PointQueryController#getAvailablePoints` | `PointQueryService#getAvailablePoints` |
| GET | `/points/queries/summary` | `PointQueryController#getPointSummary` | `PointQueryService#getPointSummary` |
| GET | `/points/queries/{pointId}` | `PointQueryController#getPointDetail` | `PointQueryService#getPointDetail` |
| POST | `/users/find-email` | `UserQueryController#findEmail` | `UserQueryService#findEmailByPhoneNumber` |
| POST | `/users/reset-password` | `UserQueryController#resetPasswordEligibility` | `PasswordResetService#checkPasswordResetEligibility` |
| POST | `/users/reset-password-sandEmail` | `UserQueryController#resetPasswordToEmail` | `PasswordResetService#checkPasswordResetAndSendEmail` |
| POST | `/users/reset` | `UserQueryController#resetPassword` | `PasswordResetService#resetPassword` |
| GET | `/users/login-info` | `UserQueryController#getLoginInfo` | `UserQueryService#getLoginInfo` |
| DELETE | `/users/delete-account` | `UserQueryController#deleteAccount` | `UserCommandService#deleteAccount` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `AdminGradeService`
  - `getAllGrades` → UserGradeRepository#findAllByOrderByLevelAsc
  - `getGradeById` → UserGradeRepository#findById~, this#convertToGradeResponseDto
  - `createGrade` → UserGradeRepository#findByLevel, UserGradeRepository#findByName, UserGradeRepository#save~, this#convertToGradeResponseDto
  - `updateGrade` → UserGradeRepository#findById~, UserGradeRepository#findByName, this#convertToGradeResponseDto
  - `deleteGrade` → UserGradeRepository#findById~, UserGradeRepository#countUsersByGradeId, UserGradeRepository#delete~
  - `getGradeUserCounts` → UserGradeRepository#countUsersGroupByGrade, UserGradeRepository#findById~
  - `getGradeStatistics` → UserGradeRepository#findAllByOrderByLevelAsc, UserGradeRepository#countUsersByGradeId
  - `runGradeAutoAssignment` → UserRepository#findAll~, UserGradeRepository#findGradeByPurchaseAmount
  - `convertToGradeResponseDto` → UserGradeRepository#countUsersByGradeId
- `AdminPointService`
  - `getUserPointHistory` → this#getUserById, PointRepository#findByUserOrderByCreatedDateDesc
  - `getUserAvailablePoints` → this#getUserById, PointRepository#findByUserAndStatusOrderByExpirationDateAsc
  - `getUserPointSummary` → this#getUserById, PointRepository#findByUserAndStatusOrderByExpirationDateAsc
  - `getPointDetail` → PointRepository#findById~
  - `addPointByAdmin` → this#getUserById, PointRepository#save~
  - `deductPointByAdmin` → this#getUserById, PointRepository#findByUserAndStatusOrderByExpirationDateAsc, PointRepository#save~
  - `getUserById` → UserRepository#findById~
- `AdminSanctionService`
  - `searchSanctions` → UserSanctionRepository#findAll~
  - `getSanctionById` → UserSanctionRepository#findById~, this#convertToSanctionResponseDto
  - `createSanction` → UserRepository#findById~, UserSanctionRepository#save~, this#convertToSanctionResponseDto
  - `updateSanction` → UserSanctionRepository#findById~, this#convertToSanctionResponseDto
  - `reviewSanction` → UserSanctionRepository#findById~, this#convertToSanctionResponseDto
  - `cancelSanction` → UserSanctionRepository#findById~, this#hasOtherActiveBanSanctions, this#convertToSanctionResponseDto
  - `getUserSanctions` → UserRepository#findById~, UserSanctionRepository#findByUserOrderByCreatedDateDesc
  - `getSanctionStatistics` → UserSanctionRepository#countTotalSanctions, UserSanctionRepository#countActiveSanctions, UserSanctionRepository#countExpiredSanctions, UserSanctionRepository#countPendingSanctions
  - `processExpiredSanctions` → UserSanctionRepository#findExpirableSanctions, this#hasOtherActiveBanSanctions
  - `hasOtherActiveBanSanctions` → UserSanctionRepository#findByUserAndStatusOrderByStartDateDesc
- `AdminUserService`
  - `searchUsers` → UserRepository#findAll~
  - `getUserDetail` → UserRepository#findById~, this#convertToUserDetailResponseDto
  - `updateUserStatus` → UserRepository#findById~, UserSanctionRepository#save~, this#convertToUserDetailResponseDto
  - `updateUserGrade` → UserRepository#findById~, UserGradeRepository#findById~, this#convertToUserDetailResponseDto
  - `updateUserRole` → UserRepository#findById~, this#convertToUserDetailResponseDto
  - `manageUserPoints` → UserRepository#findById~, PointRepository#save~, PointRepository#findByUserAndStatusOrderByExpirationDateAsc
  - `exportUsersCsv` → UserRepository#findAll~
  - `convertToUserSearchResponseDto` → ProfileRepository#findByUser
  - `convertToUserDetailResponseDto` → ProfileRepository#findByUser, BankAccountRepository#findByUser_Email, UserSanctionRepository#findByUserAndStatusOrderByStartDateDesc, this#convertToProfileDto, this#convertToBankAccountDto, this#convertToSanctionBriefDtoList
- `AuthRedisService`
  - `getRoleByAccessToken` → this#safeReadFromReplica, this#parseRole
  - `isAccessTokenValid` → this#safeHasKey
  - `isRefreshTokenValid` → this#safeHasKey
  - `getEmailByAccessToken` → this#safeReadFromReplica
  - `getAgeByAccessToken` → this#safeReadFromReplica, this#parseAge
  - `getGenderByAccessToken` → this#safeReadFromReplica, this#parseGender
- `AuthService`
  - `login` → UserRepository#findByEmail, JwtTokenProvider#generateTokenPair
- `BankAccountCommandService`
  - `createOrUpdateBankAccount` → UserRepository#findByEmail
  - `deleteBankAccount` → UserRepository#findByEmail
- `BankAccountQueryService`
  - `getBankAccount` → BankAccountRepository#findByUser_Email
- `BlockedProfileCommandService`
  - `blockProfile` → ProfileRepository#findByUserEmailWithFetchJoin, ProfileRepository#findById~, BlockedProfileRepository#findByProfileAndBlockedProfile, BlockedProfileRepository#save~
  - `unblockProfile` → ProfileRepository#findByUserEmailWithFetchJoin, ProfileRepository#findById~, BlockedProfileRepository#findByProfileAndBlockedProfile, BlockedProfileRepository#delete~
- `BlockedProfileQueryService`
  - `getBlockedProfiles` → BlockedProfileRepository#findAllByProfileWithBlocked, BlockedProfileRepository#findAllByProfileEmailWithBlocked
- `CustomOAuth2UserService`
  - `loadUser` → this#saveOrUpdate
  - `saveOrUpdate` → UserRepository#findByEmail, this#extractProviderId, this#generateUniqueReferralCode, UserRepository#save~, ProfileCommandService#createDefaultProfile
  - `generateUniqueReferralCode` → UserRepository#findByReferralCode
- `CustomUserDetailsService`
  - `loadUserByUsername` → UserRepository#findByEmail
- `FollowCommandService`
  - `createFollow` → ProfileRepository#findByUserEmailWithFetchJoin, ProfileRepository#findById~, FollowRepository#findByFollowerAndFollowing, FollowRepository#save~, NotificationCommandService#createNotification
  - `deleteFollow` → ProfileRepository#findByUserEmailWithFetchJoin, ProfileRepository#findById~, FollowRepository#findByFollowerAndFollowing, FollowRepository#delete~
- `FollowQueryService`
  - `getFollowers` → ProfileRepository#findByUserEmailWithFetchJoin, FollowRepository#findFollowersByProfileId
  - `getFollowings` → ProfileRepository#findByUserEmailWithFetchJoin, FollowRepository#findFollowingsByProfileId
- `OAuth2AuthenticationSuccessHandler`
  - `onAuthenticationSuccess` → UserRepository#findByEmail, this#getClientIp, this#generateTempToken, JwtTokenProvider#generateTokenPair, this#setCookie
- `OAuthAttributes`
  - `of` → this#ofGoogle, this#ofNaver, this#ofKakao
- `PasswordResetService`
  - `checkPasswordResetEligibility` → UserRepository#findByEmailAndPhoneNumber
  - `resetPassword` → UserRepository#findByEmailAndPhoneNumber
  - `checkPasswordResetAndSendEmail` → UserRepository#findByEmailAndPhoneNumber, EmailService#sendEmail
  - `AdminCheckPasswordResetAndSendEmail` → UserRepository#findByEmailAndPhoneNumber, EmailService#sendEmail
- `PasswordUtils`
  - `generateRandomPassword` → this#isValidPassword
- `PointCommandService`
  - `addPoint` → this#validateAddPointRequest, UserQueryService#findByEmail, PointRepository#save~
  - `usePoint` → this#validateUsePointRequest, UserQueryService#findByEmail, PointRepository#getTotalAvailablePoints, PointRepository#findByUserAndStatusOrderByExpirationDateAsc, PointRepository#save~
  - `expirePoints` → PointRepository#findByStatusAndExpirationDateBefore
- `PointExpirationScheduler`
  - `checkExpiredPoints` → PointCommandService#expirePoints
- `PointQueryService`
  - `getAllPointHistory` → UserQueryService#findByEmail, PointRepository#findByUserOrderByCreatedDateDesc
  - `getAvailablePoints` → UserQueryService#findByEmail, PointRepository#findByUserAndStatusOrderByExpirationDateAsc
  - `getPointSummary` → UserQueryService#findByEmail, PointRepository#getTotalAvailablePoints, this#getAllPointHistory, PointRepository#findExpiringPoints
  - `getPointDetail` → UserQueryService#findByEmail, PointRepository#findById~
- `ProfileCommandService`
  - `createDefaultProfile` → this#generateRandomProfileName, ProfileRepository#save~
  - `updateProfile` → ProfileRepository#findByUserEmailWithFetchJoin, this#validateProfileImage, FileUtils#deleteFile, FileUtils#saveFile
- `ProfileQueryService`
  - `getProfileInfo` → ProfileRepository#findByUserEmailWithFetchJoin, BlockedProfileQueryService#getBlockedProfiles
  - `getProfileByEmail` → ProfileRepository#findByUserEmailWithFetchJoin
  - `getProfileImageFileName` → ProfileRepository#findById~
- `ProfileService`
  - `createDefaultProfile` → this#generateRandomProfileName
- `UserCommandService`
  - `registerUser` → UserRepository#existsByEmail, IdentityVerificationService#verifyIdentity, UserRepository#findByCi, this#validateAge, UserRepository#findByReferralCode, this#convertToGender, this#calculateAge, this#generateUniqueReferralCode, UserRepository#save~, ProfileCommandService#createDefaultProfile
  - `deleteAccount` → UserRepository#findByEmail, NotificationCommandService#deleteNotificationsByUser, FileUtils#deleteFile, UserRepository#delete~
  - `generateUniqueReferralCode` → UserRepository#findByReferralCode
- `UserControllerValidator`
  - `validateUserRegistrationDto` → this#isValidPassword
  - `validatePasswordResetDto` → this#isValidPassword
  - `validateLoginInfoUpdateDto` → this#isValidPassword
- `UserQueryService`
  - `checkAdminRole` → UserRepository#findByEmail
  - `findEmailByPhoneNumber` → UserRepository#findByPhoneNumber
  - `getLoginInfo` → UserRepository#findByEmail
  - `findByEmail` → UserRepository#findByEmail
- `UserSanction`
  - `expire` → this#isExpired
- `UserUpdateService`
  - `updateLoginInfo` → UserRepository#findByEmail, UserRepository#existsByEmail
  - `reissueTokenAfterEmailChange` → UserRepository#findByEmail, AuthRedisService#removeAccessToken, AuthRedisService#removeRefreshToken, JwtTokenProvider#generateTokenPair

## 3. 클래스 목록 (역할별)

### controller (17)

- `BankAccountController` controller/BankAccountController.java · public: createOrUpdateBankAccount, getBankAccount, deleteBankAccount · 주입: BankAccountCommandService, BankAccountQueryService
- `BlockedProfileController` controller/BlockedProfileController.java · public: blockProfile, unblockProfile, getBlockedProfiles · 주입: BlockedProfileCommandService, BlockedProfileQueryService
- `FollowController` controller/FollowController.java · public: createFollow, deleteFollow, getFollowers, getFollowings · 주입: FollowCommandService, FollowQueryService
- `ProfileController` controller/ProfileController.java · public: getProfile, updateProfile, getProfileImage · 주입: ProfileQueryService, ProfileCommandService, FileUtils
- `AdminGradeController` controller/admin/AdminGradeController.java · public: getAllGrades, getGradeById, createGrade, updateGrade, deleteGrade, getGradeUserCounts, getGradeStatistics, runGradeAutoAssignment · 주입: AdminGradeService, UserQueryService
- `AdminPointController` controller/admin/AdminPointController.java · public: getUserPointHistory, getUserAvailablePoints, getUserPointSummary, getPointDetail, addPointByAdmin, deductPointByAdmin, getPointStatistics · 주입: AdminPointService, UserQueryService
- `AdminSanctionController` controller/admin/AdminSanctionController.java · public: searchSanctions, getSanctionById, createSanction, updateSanction, reviewSanction, cancelSanction, getUserSanctions, getSanctionStatistics · 주입: AdminSanctionService, UserQueryService
- `AdminUserController` controller/admin/AdminUserController.java · public: searchUsers, getUserDetail, updateUserStatus, updateUserGrade, updateUserRole, manageUserPoints, exportUserData · 주입: AdminUserService, UserQueryService
- `AdminAuthController` controller/command/AdminAuthController.java · public: adminLogin, findAdminEmail, checkAdminToken, refreshAdminToken, findAdminPassword, logout · 주입: AuthService, UserQueryService, JwtTokenProvider, PasswordResetService, AuthRedisService
- `AuthController` controller/command/AuthController.java · public: login, logout, refresh, getUserEmail, checkAuthStatus
- `IdentityVerificationController` controller/command/IdentityVerificationController.java · public: verifyIdentity · 주입: IdentityVerificationService
- `OAuth2AuthorizationController` controller/command/OAuth2AuthorizationController.java · public: redirectToOAuthProvider
- `OAuthController` controller/command/OAuthController.java · public: completeOAuthSignup, checkAuthStatus, getOAuthConnections · 주입: UserRepository, UserUpdateService, JwtTokenProvider, AuthRedisService
- `UserCommandController` controller/command/UserCommandController.java · public: registerUser, verifyIdentity, updateUserInfo · 주입: UserCommandService, UserUpdateService, UserQueryService, AuthRedisService, IdentityVerificationService
- `PointCommandController` controller/point/PointCommandController.java · public: addPoint, usePoint · 주입: PointCommandService
- `PointQueryController` controller/point/PointQueryController.java · public: getAllPointHistory, getAvailablePoints, getPointSummary, getPointDetail · 주입: PointQueryService
- `UserQueryController` controller/query/UserQueryController.java · public: findEmail, resetPasswordEligibility, resetPasswordToEmail, resetPassword, getLoginInfo, deleteAccount · 주입: AuthService, UserQueryService, PasswordResetService, UserUpdateService, JwtTokenProvider, UserCommandService, AuthRedisService

### service-command (8)

- `AuthService` service/command/AuthService.java · public: login · 주입: UserRepository, PasswordEncoder, JwtTokenProvider
- `EmailService` service/command/EmailService.java · public: sendEmail · 주입: JavaMailSender
- `IdentityVerificationService` service/command/IdentityVerificationService.java · public: verifyIdentity · 주입: RestTemplate, ObjectMapper
- `PasswordResetService` service/command/PasswordResetService.java · public: checkPasswordResetEligibility, resetPassword, checkPasswordResetAndSendEmail, AdminCheckPasswordResetAndSendEmail · 주입: UserRepository, PasswordEncoder, EmailService
- `ProfileService` service/command/ProfileService.java · public: createDefaultProfile
- `UserCommandService` service/command/UserCommandService.java · public: registerUser, deleteAccount · 주입: UserRepository, PasswordEncoder, ProfileCommandService, FileUtils, NotificationCommandService, IdentityVerificationService
- `UserSanctionService` service/command/UserSanctionService.java
- `UserUpdateService` service/command/UserUpdateService.java · public: updateLoginInfo, reissueTokenAfterEmailChange · 주입: UserRepository, PasswordEncoder, AuthRedisService, JwtTokenProvider

### service-query (1)

- `UserQueryService` service/query/UserQueryService.java · public: checkAdminRole, findEmailByPhoneNumber, getLoginInfo, findByEmail · 주입: UserRepository

### service (17)

- `AuthRedisService` redis/AuthRedisService.java · public: addAccessToken, getRoleByAccessToken, addRefreshToken, isAccessTokenValid, isRefreshTokenValid, getEmailByAccessToken, getAgeByAccessToken, getGenderByAccessToken, removeAccessToken, removeRefreshToken · 주입: RedisTemplate, RedisTemplate, RedisTemplate
- `CustomUserDetailsService` security/CustomUserDetailsService.java · extends/implements UserDetailsService · public: loadUserByUsername · 주입: UserRepository, PasswordEncoder
- `CustomOAuth2UserService` security/oauth2/CustomOAuth2UserService.java · extends/implements OAuth2UserService · public: loadUser · 주입: UserRepository, ProfileCommandService, PasswordEncoder
- `BlockedProfileCommandService` service/BlockProfile/BlockedProfileCommandService.java · public: blockProfile, unblockProfile · 주입: ProfileRepository, BlockedProfileRepository
- `BlockedProfileQueryService` service/BlockProfile/BlockedProfileQueryService.java · public: getBlockedProfiles · 주입: BlockedProfileRepository
- `AdminGradeService` service/admin/AdminGradeService.java · public: getAllGrades, getGradeById, createGrade, updateGrade, deleteGrade, getGradeUserCounts, getGradeStatistics, runGradeAutoAssignment · 주입: UserGradeRepository, UserRepository
- `AdminPointService` service/admin/AdminPointService.java · public: getUserPointHistory, getUserAvailablePoints, getUserPointSummary, getPointDetail, addPointByAdmin, deductPointByAdmin, getPointStatistics · 주입: UserRepository, PointRepository
- `AdminSanctionService` service/admin/AdminSanctionService.java · public: searchSanctions, getSanctionById, createSanction, updateSanction, reviewSanction, cancelSanction, getUserSanctions, getSanctionStatistics, processExpiredSanctions · 주입: UserSanctionRepository, UserRepository
- `AdminUserService` service/admin/AdminUserService.java · public: searchUsers, getUserDetail, updateUserStatus, updateUserGrade, updateUserRole, manageUserPoints, exportUsersCsv · 주입: UserRepository, ProfileRepository, PointRepository, UserGradeRepository, UserSanctionRepository, BankAccountRepository
- `BankAccountCommandService` service/bankaccount/BankAccountCommandService.java · public: createOrUpdateBankAccount, deleteBankAccount · 주입: UserRepository
- `BankAccountQueryService` service/bankaccount/BankAccountQueryService.java · public: getBankAccount · 주입: BankAccountRepository
- `FollowCommandService` service/follow/FollowCommandService.java · public: createFollow, deleteFollow · 주입: FollowRepository, ProfileRepository, NotificationCommandService
- `FollowQueryService` service/follow/FollowQueryService.java · public: getFollowers, getFollowings · 주입: FollowRepository, ProfileRepository
- `PointCommandService` service/point/PointCommandService.java · public: addPoint, usePoint, expirePoints · 주입: PointRepository, UserQueryService
- `PointQueryService` service/point/PointQueryService.java · public: getAllPointHistory, getAvailablePoints, getPointSummary, getPointDetail · 주입: PointRepository, UserQueryService
- `ProfileCommandService` service/profile/ProfileCommandService.java · public: createDefaultProfile, updateProfile · 주입: ProfileRepository, FileUtils
- `ProfileQueryService` service/profile/ProfileQueryService.java · public: getProfileInfo, getProfileByEmail, getProfileImageFileName · 주입: ProfileRepository, BlockedProfileQueryService

### repository (8)

- `BankAccountRepository` (interface) repository/BankAccountRepository.java · extends/implements JpaRepository · public: findByUser_Email
- `BlockedProfileRepository` (interface) repository/BlockedProfileRepository.java · extends/implements JpaRepository · public: findByProfile, findByProfileAndBlockedProfileId, findBlockedProfileIdsByProfile, findAllByProfileWithBlocked, findByProfileAndBlockedProfile, findAllByProfileEmailWithBlocked
- `FollowRepository` (interface) repository/FollowRepository.java · extends/implements JpaRepository · public: findByFollowerAndFollowing, findFollowersByProfileId, findFollowingsByProfileId
- `PointRepository` (interface) repository/PointRepository.java · extends/implements JpaRepository · public: findByUserOrderByCreatedDateDesc, findByUserAndStatusOrderByExpirationDateAsc, findByStatusAndExpirationDateBefore, getTotalAvailablePoints, findExpiringPoints
- `ProfileRepository` (interface) repository/ProfileRepository.java · extends/implements JpaRepository · public: findByUserEmailWithFetchJoin, findByUser
- `UserGradeRepository` (interface) repository/UserGradeRepository.java · extends/implements JpaRepository · public: findByLevel, findByName, findAllByOrderByLevelAsc, findGradeByPurchaseAmount, countUsersByGradeId, countUsersGroupByGrade
- `UserRepository` (interface) repository/UserRepository.java · extends/implements JpaRepository, JpaSpecificationExecutor · public: findByReferralCode, findByEmail, findByEmailAndPhoneNumber, existsByEmail, findByCi, findByDi, findByPhoneNumber, findByMultipleCriteria, searchUsers, countUsersByGrade, countActiveUsers, findRecentlyJoinedUsers
- `UserSanctionRepository` (interface) repository/UserSanctionRepository.java · extends/implements JpaRepository, JpaSpecificationExecutor · public: findByUserOrderByCreatedDateDesc, findByUserAndStatusOrderByStartDateDesc, findByStatus, findByType, findExpirableSanctions, findByCreatedDateBetween, findByUserEmail, countActiveSanctions, countExpiredSanctions, countPendingSanctions, countTotalSanctions

### entity (15)

- `BankAccount` entity/BankAccount.java · extends/implements BaseTimeEntity · public: updateBankAccount, assignUser, unassignUser
- `BlockedProfile` entity/BlockedProfile.java · extends/implements BaseTimeEntity · public: assignProfile, assignBlockedProfile
- `Follow` entity/Follow.java · extends/implements BaseTimeEntity · public: addFollower, addFollowing
- `Gender` (enum) entity/Gender.java
- `OAuthConnection` entity/OAuthConnection.java · public: setUser
- `Point` entity/Point.java · extends/implements BaseTimeEntity · public: assignUser, unassignUser, use, expire, setReason, isValid, setExpirationDate, setStatus
- `PointStatus` (enum) entity/PointStatus.java
- `Profile` entity/Profile.java · extends/implements BaseTimeEntity · public: updateProfile, addFollower, addFollowing, removeFollower, removeFollowing, addStyle, addDataStyle, removeStyle, addBlockedProfile, removeBlockedProfile, addUser, updateProfileImageUrl
- `Role` (enum) entity/Role.java
- `SanctionStatus` (enum) entity/SanctionStatus.java
- `SanctionType` (enum) entity/SanctionType.java
- `ShoeSize` (enum) entity/ShoeSize.java · public: getValue
- `User` entity/User.java · extends/implements BaseTimeEntity · public: addOAuthConnection, addGrade, addActive, setVerified, hasOAuthConnection, changeRole, updateUser, updatePassword, updateConsent, updateLoginInfo, addReferrer, removeReferrer, addProfile, removeProfile, assignBankAccount, removeBankAccount, addPaymentInfo, removePaymentInfo, addAddress, removeAddress, addInterest, removeInterest, addPoint, removePoint, calculateTotalPoints
- `UserGrade` entity/UserGrade.java · extends/implements BaseTimeEntity · public: updateGrade, addUser, removeUser
- `UserSanction` entity/UserSanction.java · extends/implements BaseTimeEntity · public: assignUser, updateStatus, approve, reject, cancel, isExpired, expire, setReason, setDetails, setType, setStartDate, setEndDate

### dto (18)

- `BankAccountDto` dto/BankAccount/BankAccountDto.java
- `BankAccountInfoDto` dto/BankAccount/BankAccountInfoDto.java
- `BlockedProfileDto` dto/BlockedProfileDto.java
- `EmailFindRequestDto` dto/EmailFindRequestDto.java
- `LoginInfoDto` dto/LoginInfoDto.java
- `LoginInfoUpdateDto` dto/LoginInfoUpdateDto.java
- `LoginRequestDto` dto/LoginRequestDto.java
- `OAuthSignupCompleteDto` dto/OAuthSignupCompleteDto.java
- `PasswordResetRequestDto` dto/PasswordResetRequestDto.java
- `ProfileInfoDto` dto/ProfileInfoDto.java
- `ProfileUpdateDto` dto/ProfileUpdateDto.java
- `SanctionDto` dto/SanctionDto.java
- `UserGradeDto` dto/UserGradeDto.java
- `UserManagementDto` dto/UserManagementDto.java
- `UserRegistrationDto` dto/UserRegistrationDto.java
- `VerifiedCustomerDto` dto/VerifiedCustomerDto.java
- `FollowDto` dto/follow/FollowDto.java
- `PointDto` dto/point/PointDto.java

### exception (23)

- `AlreadyBlockedException` exception/AlreadyBlockedException.java
- `AlreadyFollowingException` exception/AlreadyFollowingException.java
- `BankAccountNotFoundException` exception/BankAccountNotFoundException.java
- `CannotBlockSelfException` exception/CannotBlockSelfException.java
- `CannotFollowSelfException` exception/CannotFollowSelfException.java
- `DuplicateEmailException` exception/DuplicateEmailException.java
- `DuplicatePhoneNumberException` exception/DuplicatePhoneNumberException.java
- `IdentityVerificationFailedException` exception/IdentityVerificationFailedException.java
- `InsufficientPointsException` exception/InsufficientPointsException.java
- `InvalidBankAccountException` exception/InvalidBankAccountException.java
- `InvalidPasswordException` exception/InvalidPasswordException.java
- `InvalidPointAmountException` exception/InvalidPointAmountException.java
- `InvalidProfileImageException` exception/InvalidProfileImageException.java
- `NotBlockedException` exception/NotBlockedException.java
- `NotFollowingException` exception/NotFollowingException.java
- `OAuthAuthenticationFailedException` exception/OAuthAuthenticationFailedException.java
- `OAuthProviderNotSupportedException` exception/OAuthProviderNotSupportedException.java
- `PointHistoryNotFoundException` exception/PointHistoryNotFoundException.java
- `ProfileImageTooLargeException` exception/ProfileImageTooLargeException.java
- `ProfileNotFoundException` exception/ProfileNotFoundException.java
- `UserErrorCode` exception/UserErrorCode.java
- `UserException` exception/UserException.java
- `UserNotFoundException` exception/UserNotFoundException.java

### other (10)

- `UserSanctionSpecifications` repository/UserSanctionSpecifications.java · public: withSearchCriteria, hasUserId, hasStatus, hasType, createdBetween, startDateBetween, endDateBetween, userEmailLike
- `PointExpirationScheduler` scheduler/PointExpirationScheduler.java · public: checkExpiredPoints · 주입: PointCommandService
- `OAuth2AuthenticationSuccessHandler` security/oauth2/OAuth2AuthenticationSuccessHandler.java · extends/implements SimpleUrlAuthenticationSuccessHandler · public: onAuthenticationSuccess · 주입: JwtTokenProvider, AuthRedisService, UserRepository
- `OAuthAttributes` security/oauth2/OAuthAttributes.java · public: of
- `PasswordUtils` utils/PasswordUtils.java · public: generateRandomPassword
- `BankAccountControllerValidator` validate/BankAccountControllerValidator.java · public: validateBankAccountDto
- `BlockedProfileControllerValidator` validate/BlockedProfileControllerValidator.java
- `FollowControllerValidator` validate/FollowControllerValidator.java
- `ProfileControllerValidator` validate/ProfileControllerValidator.java
- `UserControllerValidator` validate/UserControllerValidator.java · public: validateUserRegistrationDto, validateLoginRequestDto, validateEmailFindRequestDto, validatePasswordResetEligibilityDto, validatePasswordResetDto, validateLoginInfoUpdateDto

### 다른 도메인 참조 (import·주입 기준)

- **address**: `Address` ← User
- **notification**: `NotificationCategory` ← FollowCommandService; `NotificationCommandService` ← FollowCommandService, UserCommandService; `NotificationType` ← FollowCommandService
- **payment**: `PaymentInfo` ← User
- **product**: `Interest` ← User
- **style**: `Style` ← Profile

### 이 도메인을 참조하는 다른 도메인

- **address**: User, UserRepository
- **adminaddress**: User, UserRepository
- **adminchatquestion**: User
- **admininquiry**: User, UserRepository
- **adminnotification**: User, UserRepository
- **adminorder**: User
- **adminpayment**: User, UserRepository
- **adminproduct**: Gender, User
- **adminsale**: User
- **adminstyle**: Gender, Profile
- **adminuser**: AuthRedisService, AuthService, BankAccount, BankAccountRepository, BlockedProfile, Follow, Gender, Point, PointStatus, Profile, ProfileCommandService, ProfileRepository, Role, SanctionStatus, SanctionType, ShoeSize, User, UserGrade, UserRepository, UserSanction
- **chatQuestion**: User, UserQueryService
- **event**: UserQueryService
- **faq**: UserQueryService
- **global**: AuthRedisService, AuthService, BankAccountRepository, CustomOAuth2UserService, Gender, ProfileCommandService, ProfileRepository, Role, User, UserRepository
- **inquiry**: User, UserQueryService, UserRepository
- **inspection**: UserQueryService
- **internaladdress**: User, UserRepository
- **internalchatquestion**: User
- **internalinquiry**: User, UserRepository
- **internalnotification**: User, UserRepository
- **internalorder**: User
- **internalpayment**: User, UserRepository
- **internalproduct**: Gender, User
- **internalsale**: User
- **internalstyle**: Gender, Profile
- **internaluser**: AuthRedisService, AuthService, BankAccount, BankAccountRepository, BlockedProfile, Follow, Gender, Point, PointStatus, Profile, ProfileCommandService, ProfileRepository, Role, SanctionStatus, SanctionType, ShoeSize, User, UserGrade, UserRepository, UserSanction
- **legacyaddress**: User, UserRepository
- **legacychatquestion**: User
- **legacyinquiry**: User, UserRepository
- **legacynotification**: User, UserRepository
- **legacyorder**: User
- **legacypayment**: User, UserRepository
- **legacyproduct**: Gender, User
- **legacysale**: User
- **legacystyle**: Gender, Profile
- **legacyuser**: AuthRedisService, AuthService, BankAccount, BankAccountRepository, BlockedProfile, Follow, Gender, Point, PointStatus, Profile, ProfileCommandService, ProfileRepository, Role, SanctionStatus, SanctionType, ShoeSize, User, UserGrade, UserRepository, UserSanction
- **notice**: UserQueryService
- **notification**: User, UserRepository
- **order**: User, UserQueryService
- **partneraddress**: User, UserRepository
- **partnerchatquestion**: User
- **partnerinquiry**: User, UserRepository
- **partnernotification**: User, UserRepository
- **partnerorder**: User
- **partnerpayment**: User, UserRepository
- **partnerproduct**: Gender, User
- **partnersale**: User
- **partnerstyle**: Gender, Profile
- **partneruser**: AuthRedisService, AuthService, BankAccount, BankAccountRepository, BlockedProfile, Follow, Gender, Point, PointStatus, Profile, ProfileCommandService, ProfileRepository, Role, SanctionStatus, SanctionType, ShoeSize, User, UserGrade, UserRepository, UserSanction
- **payment**: User, UserQueryService, UserRepository
- **product**: Gender, User, UserQueryService
- **sale**: BankAccountQueryService, User, UserQueryService
- **settlementaddress**: User, UserRepository
- **settlementchatquestion**: User
- **settlementinquiry**: User, UserRepository
- **settlementnotification**: User, UserRepository
- **settlementorder**: User
- **settlementpayment**: User, UserRepository
- **settlementproduct**: Gender, User
- **settlementsale**: User
- **settlementstyle**: Gender, Profile
- **settlementuser**: AuthRedisService, AuthService, BankAccount, BankAccountRepository, BlockedProfile, Follow, Gender, Point, PointStatus, Profile, ProfileCommandService, ProfileRepository, Role, SanctionStatus, SanctionType, ShoeSize, User, UserGrade, UserRepository, UserSanction
- **shipment**: User
- **style**: Gender, Profile, ProfileQueryService
- **warehouseStorage**: User

## 4. JPA 엔티티 연관

- `BankAccount` @Entity · extends BaseTimeEntity
  - user: @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) → `User`
- `BlockedProfile` @Entity · extends BaseTimeEntity
  - profile: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "profile_id", nullable = false) → `Profile`
  - blockedProfile: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "blocked_profile_id", nullable = false) → `Profile`
- `Follow` @Entity @Table(name = "follows") · extends BaseTimeEntity
  - follower: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "follower_profile_id", nullable = false) → `Profile`
  - following: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "following_profile_id", nullable = false) → `Profile`
- `OAuthConnection` @Entity
  - user: @ManyToOne @JoinColumn(name = "user_id") → `User`
- `Point` @Entity · extends BaseTimeEntity
  - user: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") → `User`
- `Profile` @Entity · extends BaseTimeEntity
  - user: @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) → `User`
  - followings: @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true) → `Follow`
  - followers: @OneToMany(mappedBy = "following", cascade = CascadeType.ALL, orphanRemoval = true) → `Follow`
  - blockedByProfiles: @OneToMany(mappedBy = "blockedProfile", cascade = CascadeType.ALL, orphanRemoval = true) → `BlockedProfile`
  - styles: @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true) → `Style` [style]
- `User` @Entity @Table(name = "users") · extends BaseTimeEntity
  - profile: @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true) → `Profile`
  - interests: @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true) → `Interest` [product]
  - addresses: @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true) → `Address` [address]
  - paymentInfos: @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true) → `PaymentInfo` [payment]
  - bankAccount: @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true) → `BankAccount`
  - points: @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true) → `Point`
  - referredUsers: @OneToMany(mappedBy = "referrer", cascade = CascadeType.ALL, orphanRemoval = true) → `User`
  - referrer: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "referrerCode", referencedColumnName = "referralCode", insertable = false, updatable = false) → `User`
  - oauthConnections: @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true) → `OAuthConnection`
  - grade: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "grade_id") → `UserGrade`
- `UserGrade` @Entity @Table(name = "user_grades") · extends BaseTimeEntity
  - users: @OneToMany(mappedBy = "grade", fetch = FetchType.LAZY) → `User`
- `UserSanction` @Entity @Table(name = "user_sanctions") · extends BaseTimeEntity
  - user: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) → `User`

다른 도메인 엔티티에서 들어오는 연관:

- `Address.user` @ManyToOne → `User` [address]
- `ChatQuestion.user` @ManyToOne → `User` [chatQuestion]
- `GPTUsageLog.user` @ManyToOne → `User` [chatQuestion]
- `Inquiry.user` @ManyToOne → `User` [inquiry]
- `Notification.user` @ManyToOne → `User` [notification]
- `Order.user` @ManyToOne → `User` [order]
- `OrderBid.user` @ManyToOne → `User` [order]
- `Payment.user` @ManyToOne → `User` [payment]
- `PaymentInfo.user` @ManyToOne → `User` [payment]
- `Interest.user` @ManyToOne → `User` [product]
- `Sale.seller` @ManyToOne → `User` [sale]
- `SaleBid.seller` @ManyToOne → `User` [sale]
- `Style.profile` @ManyToOne → `Profile` [style]
- `StyleComment.profile` @ManyToOne → `Profile` [style]
- `StyleCommentLike.profile` @ManyToOne → `Profile` [style]
- `StyleInterest.profile` @ManyToOne → `Profile` [style]
- `StyleLike.profile` @ManyToOne → `Profile` [style]
- `WarehouseStorage.user` @ManyToOne → `User` [warehouseStorage]

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

