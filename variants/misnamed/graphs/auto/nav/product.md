# product 도메인

기준 경로: `src/main/java/com/fream/back/domain/product/` · 파일 117개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

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
| GET | `/admin/users/grades` | `AdminGradeController#getAllGrades` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `AdminGradeService#getAllGrades` |
| GET | `/admin/users/grades/{gradeId}` | `AdminGradeController#getGradeById` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `AdminGradeService#getGradeById` |
| POST | `/admin/users/grades` | `AdminGradeController#createGrade` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `AdminGradeService#createGrade` |
| PUT | `/admin/users/grades/{gradeId}` | `AdminGradeController#updateGrade` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `AdminGradeService#updateGrade` |
| DELETE | `/admin/users/grades/{gradeId}` | `AdminGradeController#deleteGrade` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `AdminGradeService#deleteGrade` |
| GET | `/admin/users/grades/counts` | `AdminGradeController#getGradeUserCounts` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `AdminGradeService#getGradeUserCounts` |
| GET | `/admin/users/grades/statistics` | `AdminGradeController#getGradeStatistics` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `AdminGradeService#getGradeStatistics` |
| POST | `/admin/users/grades/auto-assign` | `AdminGradeController#runGradeAutoAssignment` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `AdminGradeService#runGradeAutoAssignment` |
| GET | `/admin/users/points/user/{userId}` | `AdminPointController#getUserPointHistory` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `AdminPointService#getUserPointHistory` |
| GET | `/admin/users/points/user/{userId}/available` | `AdminPointController#getUserAvailablePoints` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `AdminPointService#getUserAvailablePoints` |
| GET | `/admin/users/points/user/{userId}/summary` | `AdminPointController#getUserPointSummary` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `AdminPointService#getUserPointSummary` |
| GET | `/admin/users/points/{pointId}` | `AdminPointController#getPointDetail` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `AdminPointService#getPointDetail` |
| POST | `/admin/users/points/user/{userId}/add` | `AdminPointController#addPointByAdmin` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `AdminPointService#addPointByAdmin` |
| POST | `/admin/users/points/user/{userId}/deduct` | `AdminPointController#deductPointByAdmin` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `AdminPointService#deductPointByAdmin` |
| GET | `/admin/users/points/statistics` | `AdminPointController#getPointStatistics` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `AdminPointService#getPointStatistics` |
| GET | `/admin/users/search` | `AdminProductController#searchUsers` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `AdminProductService#searchUsers` |
| GET | `/admin/users/{userId}` | `AdminProductController#getUserDetail` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `AdminProductService#getUserDetail` |
| PATCH | `/admin/users/{userId}/status` | `AdminProductController#updateUserStatus` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `AdminProductService#updateUserStatus` |
| PATCH | `/admin/users/{userId}/grade` | `AdminProductController#updateUserGrade` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `AdminProductService#updateUserGrade` |
| PATCH | `/admin/users/{userId}/role` | `AdminProductController#updateUserRole` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `AdminProductService#updateUserRole` |
| POST | `/admin/users/{userId}/points` | `AdminProductController#manageUserPoints` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `AdminProductService#manageUserPoints` |
| GET | `/admin/users/export` | `AdminProductController#exportUserData` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `AdminProductService#exportUsersCsv` |
| GET | `/admin/users/sanctions/search` | `AdminSanctionController#searchSanctions` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `AdminSanctionService#searchSanctions` |
| GET | `/admin/users/sanctions/{sanctionId}` | `AdminSanctionController#getSanctionById` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `AdminSanctionService#getSanctionById` |
| POST | `/admin/users/sanctions` | `AdminSanctionController#createSanction` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `AdminSanctionService#createSanction` |
| PUT | `/admin/users/sanctions/{sanctionId}` | `AdminSanctionController#updateSanction` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `AdminSanctionService#updateSanction` |
| PATCH | `/admin/users/sanctions/{sanctionId}/review` | `AdminSanctionController#reviewSanction` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `AdminSanctionService#reviewSanction` |
| PATCH | `/admin/users/sanctions/{sanctionId}/cancel` | `AdminSanctionController#cancelSanction` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `AdminSanctionService#cancelSanction` |
| GET | `/admin/users/sanctions/user/{userId}` | `AdminSanctionController#getUserSanctions` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `AdminSanctionService#getUserSanctions` |
| GET | `/admin/users/sanctions/statistics` | `AdminSanctionController#getSanctionStatistics` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `AdminSanctionService#getSanctionStatistics` |
| POST | `/admin/auth/login` | `AdminAuthController#adminLogin` | `this#getClientIp`, `AuthService#login`, `JwtTokenProvider#getEmailFromToken`, `ProductQueryService#checkAdminRole`, `this#setCookie` |
| POST | `/admin/auth/find-email` | `AdminAuthController#findAdminEmail` | `ProductQueryService#findEmailByPhoneNumber`, `ProductQueryService#checkAdminRole` |
| GET | `/admin/auth/check` | `AdminAuthController#checkAdminToken` | `this#getCookieValue`, `JwtTokenProvider#validateToken`, `JwtTokenProvider#getEmailFromToken`, `ProductQueryService#checkAdminRole` |
| POST | `/admin/auth/refresh` | `AdminAuthController#refreshAdminToken` | `this#getCookieValue`, `JwtTokenProvider#validateToken`, `AuthRedisService#isRefreshTokenValid`, `JwtTokenProvider#getEmailFromToken`, `ProductQueryService#findByEmail`, `ProductQueryService#checkAdminRole`, `this#getClientIp`, `JwtTokenProvider#generateTokenPair`, `this#setCookie` |
| POST | `/admin/auth/find-password` | `AdminAuthController#findAdminPassword` | `ProductQueryService#findByEmail`, `PasswordResetService#AdminCheckPasswordResetAndSendEmail` |
| POST | `/admin/auth/logout` | `AdminAuthController#logout` | `this#getCookieValue`, `JwtTokenProvider#validateToken`, `JwtTokenProvider#getEmailFromToken`, `ProductQueryService#checkAdminRole`, `AuthRedisService#removeAccessToken`, `AuthRedisService#removeRefreshToken`, `this#expireCookie` |
| POST | `/auth/login` | `AuthController#login` |  |
| POST | `/auth/logout` | `AuthController#logout` |  |
| POST | `/auth/refresh` | `AuthController#refresh` |  |
| GET | `/auth/email` | `AuthController#getUserEmail` |  |
| GET | `/auth/status` | `AuthController#checkAuthStatus` |  |
| POST | `/identity-verification/verify` | `IdentityVerificationController#verifyIdentity` | `IdentityVerificationService#verifyIdentity` |
| GET | `/oauth2/authorization/{registrationId}` | `OAuth2AuthorizationController#redirectToOAuthProvider` |  |
| POST | `/oauth/complete-signup` | `OAuthController#completeOAuthSignup` | `this#extractEmailFromToken`, `ProductRepository#findByEmail`, `ProductRepository#findByReferralCode`, `ProductRepository#save~`, `this#getClientIp`, `JwtTokenProvider#generateTokenPair`, `this#setCookie` |
| GET | `/oauth/status` | `OAuthController#checkAuthStatus` | `this#getCookieValue`, `JwtTokenProvider#validateToken`, `AuthRedisService#isAccessTokenValid`, `JwtTokenProvider#getEmailFromToken` |
| GET | `/oauth/connections` | `OAuthController#getOAuthConnections` | `this#getCookieValue`, `JwtTokenProvider#getEmailFromToken`, `ProductRepository#findByEmail` |
| POST | `/users/register` | `ProductCommandController#registerUser` | `ProductCommandService#registerUser` |
| POST | `/users/verify-identity` | `ProductCommandController#verifyIdentity` | `IdentityVerificationService#verifyIdentity` |
| PUT | `/users/update` | `ProductCommandController#updateUserInfo` | `ProductUpdateService#updateLoginInfo`, `ProductQueryService#getLoginInfo`, `this#getCookieValue`, `AuthRedisService#removeAccessToken`, `AuthRedisService#removeRefreshToken`, `ProductUpdateService#reissueTokenAfterEmailChange`, `this#setCookie` |
| POST | `/points/commands` | `PointCommandController#addPoint` | `PointCommandService#addPoint` |
| POST | `/points/commands/use` | `PointCommandController#usePoint` | `PointCommandService#usePoint` |
| GET | `/points/queries` | `PointQueryController#getAllPointHistory` | `PointQueryService#getAllPointHistory` |
| GET | `/points/queries/available` | `PointQueryController#getAvailablePoints` | `PointQueryService#getAvailablePoints` |
| GET | `/points/queries/summary` | `PointQueryController#getPointSummary` | `PointQueryService#getPointSummary` |
| GET | `/points/queries/{pointId}` | `PointQueryController#getPointDetail` | `PointQueryService#getPointDetail` |
| POST | `/users/find-email` | `ProductQueryController#findEmail` | `ProductQueryService#findEmailByPhoneNumber` |
| POST | `/users/reset-password` | `ProductQueryController#resetPasswordEligibility` | `PasswordResetService#checkPasswordResetEligibility` |
| POST | `/users/reset-password-sandEmail` | `ProductQueryController#resetPasswordToEmail` | `PasswordResetService#checkPasswordResetAndSendEmail` |
| POST | `/users/reset` | `ProductQueryController#resetPassword` | `PasswordResetService#resetPassword` |
| GET | `/users/login-info` | `ProductQueryController#getLoginInfo` | `ProductQueryService#getLoginInfo` |
| DELETE | `/users/delete-account` | `ProductQueryController#deleteAccount` | `ProductCommandService#deleteAccount` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `AdminGradeService`
  - `getAllGrades` → ProductGradeRepository#findAllByOrderByLevelAsc
  - `getGradeById` → ProductGradeRepository#findById~, this#convertToGradeResponseDto
  - `createGrade` → ProductGradeRepository#findByLevel, ProductGradeRepository#findByName, ProductGradeRepository#save~, this#convertToGradeResponseDto
  - `updateGrade` → ProductGradeRepository#findById~, ProductGradeRepository#findByName, this#convertToGradeResponseDto
  - `deleteGrade` → ProductGradeRepository#findById~, ProductGradeRepository#countUsersByGradeId, ProductGradeRepository#delete~
  - `getGradeUserCounts` → ProductGradeRepository#countUsersGroupByGrade, ProductGradeRepository#findById~
  - `getGradeStatistics` → ProductGradeRepository#findAllByOrderByLevelAsc, ProductGradeRepository#countUsersByGradeId
  - `runGradeAutoAssignment` → ProductRepository#findAll~, ProductGradeRepository#findGradeByPurchaseAmount
  - `convertToGradeResponseDto` → ProductGradeRepository#countUsersByGradeId
- `AdminPointService`
  - `getUserPointHistory` → this#getUserById, PointRepository#findByUserOrderByCreatedDateDesc
  - `getUserAvailablePoints` → this#getUserById, PointRepository#findByUserAndStatusOrderByExpirationDateAsc
  - `getUserPointSummary` → this#getUserById, PointRepository#findByUserAndStatusOrderByExpirationDateAsc
  - `getPointDetail` → PointRepository#findById~
  - `addPointByAdmin` → this#getUserById, PointRepository#save~
  - `deductPointByAdmin` → this#getUserById, PointRepository#findByUserAndStatusOrderByExpirationDateAsc, PointRepository#save~
  - `getUserById` → ProductRepository#findById~
- `AdminProductService`
  - `searchUsers` → ProductRepository#findAll~
  - `getUserDetail` → ProductRepository#findById~, this#convertToUserDetailResponseDto
  - `updateUserStatus` → ProductRepository#findById~, ProductSanctionRepository#save~, this#convertToUserDetailResponseDto
  - `updateUserGrade` → ProductRepository#findById~, ProductGradeRepository#findById~, this#convertToUserDetailResponseDto
  - `updateUserRole` → ProductRepository#findById~, this#convertToUserDetailResponseDto
  - `manageUserPoints` → ProductRepository#findById~, PointRepository#save~, PointRepository#findByUserAndStatusOrderByExpirationDateAsc
  - `exportUsersCsv` → ProductRepository#findAll~
  - `convertToUserSearchResponseDto` → ProfileRepository#findByUser
  - `convertToUserDetailResponseDto` → ProfileRepository#findByUser, BankAccountRepository#findByUser_Email, ProductSanctionRepository#findByUserAndStatusOrderByStartDateDesc, this#convertToProfileDto, this#convertToBankAccountDto, this#convertToSanctionBriefDtoList
- `AdminSanctionService`
  - `searchSanctions` → ProductSanctionRepository#findAll~
  - `getSanctionById` → ProductSanctionRepository#findById~, this#convertToSanctionResponseDto
  - `createSanction` → ProductRepository#findById~, ProductSanctionRepository#save~, this#convertToSanctionResponseDto
  - `updateSanction` → ProductSanctionRepository#findById~, this#convertToSanctionResponseDto
  - `reviewSanction` → ProductSanctionRepository#findById~, this#convertToSanctionResponseDto
  - `cancelSanction` → ProductSanctionRepository#findById~, this#hasOtherActiveBanSanctions, this#convertToSanctionResponseDto
  - `getUserSanctions` → ProductRepository#findById~, ProductSanctionRepository#findByUserOrderByCreatedDateDesc
  - `getSanctionStatistics` → ProductSanctionRepository#countTotalSanctions, ProductSanctionRepository#countActiveSanctions, ProductSanctionRepository#countExpiredSanctions, ProductSanctionRepository#countPendingSanctions
  - `processExpiredSanctions` → ProductSanctionRepository#findExpirableSanctions, this#hasOtherActiveBanSanctions
  - `hasOtherActiveBanSanctions` → ProductSanctionRepository#findByUserAndStatusOrderByStartDateDesc
- `AuthRedisService`
  - `getRoleByAccessToken` → this#safeReadFromReplica, this#parseRole
  - `isAccessTokenValid` → this#safeHasKey
  - `isRefreshTokenValid` → this#safeHasKey
  - `getEmailByAccessToken` → this#safeReadFromReplica
  - `getAgeByAccessToken` → this#safeReadFromReplica, this#parseAge
  - `getGenderByAccessToken` → this#safeReadFromReplica, this#parseGender
- `AuthService`
  - `login` → ProductRepository#findByEmail, JwtTokenProvider#generateTokenPair
- `BankAccountCommandService`
  - `createOrUpdateBankAccount` → ProductRepository#findByEmail
  - `deleteBankAccount` → ProductRepository#findByEmail
- `BankAccountQueryService`
  - `getBankAccount` → BankAccountRepository#findByUser_Email
- `BlockedProfileCommandService`
  - `blockProfile` → ProfileRepository#findByUserEmailWithFetchJoin, ProfileRepository#findById~, BlockedProfileRepository#findByProfileAndBlockedProfile, BlockedProfileRepository#save~
  - `unblockProfile` → ProfileRepository#findByUserEmailWithFetchJoin, ProfileRepository#findById~, BlockedProfileRepository#findByProfileAndBlockedProfile, BlockedProfileRepository#delete~
- `BlockedProfileQueryService`
  - `getBlockedProfiles` → BlockedProfileRepository#findAllByProfileWithBlocked, BlockedProfileRepository#findAllByProfileEmailWithBlocked
- `CustomOAuth2ProductService`
  - `loadUser` → this#saveOrUpdate
  - `saveOrUpdate` → ProductRepository#findByEmail, this#extractProviderId, this#generateUniqueReferralCode, ProductRepository#save~, ProfileCommandService#createDefaultProfile
  - `generateUniqueReferralCode` → ProductRepository#findByReferralCode
- `CustomProductDetailsService`
  - `loadUserByUsername` → ProductRepository#findByEmail
- `FollowCommandService`
  - `createFollow` → ProfileRepository#findByUserEmailWithFetchJoin, ProfileRepository#findById~, FollowRepository#findByFollowerAndFollowing, FollowRepository#save~, PaymentCommandService#createNotification
  - `deleteFollow` → ProfileRepository#findByUserEmailWithFetchJoin, ProfileRepository#findById~, FollowRepository#findByFollowerAndFollowing, FollowRepository#delete~
- `FollowQueryService`
  - `getFollowers` → ProfileRepository#findByUserEmailWithFetchJoin, FollowRepository#findFollowersByProfileId
  - `getFollowings` → ProfileRepository#findByUserEmailWithFetchJoin, FollowRepository#findFollowingsByProfileId
- `OAuth2AuthenticationSuccessHandler`
  - `onAuthenticationSuccess` → ProductRepository#findByEmail, this#getClientIp, this#generateTempToken, JwtTokenProvider#generateTokenPair, this#setCookie
- `OAuthAttributes`
  - `of` → this#ofGoogle, this#ofNaver, this#ofKakao
- `PasswordResetService`
  - `checkPasswordResetEligibility` → ProductRepository#findByEmailAndPhoneNumber
  - `resetPassword` → ProductRepository#findByEmailAndPhoneNumber
  - `checkPasswordResetAndSendEmail` → ProductRepository#findByEmailAndPhoneNumber, EmailService#sendEmail
  - `AdminCheckPasswordResetAndSendEmail` → ProductRepository#findByEmailAndPhoneNumber, EmailService#sendEmail
- `PasswordUtils`
  - `generateRandomPassword` → this#isValidPassword
- `PointCommandService`
  - `addPoint` → this#validateAddPointRequest, ProductQueryService#findByEmail, PointRepository#save~
  - `usePoint` → this#validateUsePointRequest, ProductQueryService#findByEmail, PointRepository#getTotalAvailablePoints, PointRepository#findByUserAndStatusOrderByExpirationDateAsc, PointRepository#save~
  - `expirePoints` → PointRepository#findByStatusAndExpirationDateBefore
- `PointExpirationScheduler`
  - `checkExpiredPoints` → PointCommandService#expirePoints
- `PointQueryService`
  - `getAllPointHistory` → ProductQueryService#findByEmail, PointRepository#findByUserOrderByCreatedDateDesc
  - `getAvailablePoints` → ProductQueryService#findByEmail, PointRepository#findByUserAndStatusOrderByExpirationDateAsc
  - `getPointSummary` → ProductQueryService#findByEmail, PointRepository#getTotalAvailablePoints, this#getAllPointHistory, PointRepository#findExpiringPoints
  - `getPointDetail` → ProductQueryService#findByEmail, PointRepository#findById~
- `ProductCommandService`
  - `registerUser` → ProductRepository#existsByEmail, IdentityVerificationService#verifyIdentity, ProductRepository#findByCi, this#validateAge, ProductRepository#findByReferralCode, this#convertToGender, this#calculateAge, this#generateUniqueReferralCode, ProductRepository#save~, ProfileCommandService#createDefaultProfile
  - `deleteAccount` → ProductRepository#findByEmail, PaymentCommandService#deleteNotificationsByUser, FileUtils#deleteFile, ProductRepository#delete~
  - `generateUniqueReferralCode` → ProductRepository#findByReferralCode
- `ProductControllerValidator`
  - `validateUserRegistrationDto` → this#isValidPassword
  - `validatePasswordResetDto` → this#isValidPassword
  - `validateLoginInfoUpdateDto` → this#isValidPassword
- `ProductQueryService`
  - `checkAdminRole` → ProductRepository#findByEmail
  - `findEmailByPhoneNumber` → ProductRepository#findByPhoneNumber
  - `getLoginInfo` → ProductRepository#findByEmail
  - `findByEmail` → ProductRepository#findByEmail
- `ProductSanction`
  - `expire` → this#isExpired
- `ProductUpdateService`
  - `updateLoginInfo` → ProductRepository#findByEmail, ProductRepository#existsByEmail
  - `reissueTokenAfterEmailChange` → ProductRepository#findByEmail, AuthRedisService#removeAccessToken, AuthRedisService#removeRefreshToken, JwtTokenProvider#generateTokenPair
- `ProfileCommandService`
  - `createDefaultProfile` → this#generateRandomProfileName, ProfileRepository#save~
  - `updateProfile` → ProfileRepository#findByUserEmailWithFetchJoin, this#validateProfileImage, FileUtils#deleteFile, FileUtils#saveFile
- `ProfileQueryService`
  - `getProfileInfo` → ProfileRepository#findByUserEmailWithFetchJoin, BlockedProfileQueryService#getBlockedProfiles
  - `getProfileByEmail` → ProfileRepository#findByUserEmailWithFetchJoin
  - `getProfileImageFileName` → ProfileRepository#findById~
- `ProfileService`
  - `createDefaultProfile` → this#generateRandomProfileName

## 3. 클래스 목록 (역할별)

### controller (17)

- `BankAccountController` controller/BankAccountController.java · public: createOrUpdateBankAccount, getBankAccount, deleteBankAccount · 주입: BankAccountCommandService, BankAccountQueryService
- `BlockedProfileController` controller/BlockedProfileController.java · public: blockProfile, unblockProfile, getBlockedProfiles · 주입: BlockedProfileCommandService, BlockedProfileQueryService
- `FollowController` controller/FollowController.java · public: createFollow, deleteFollow, getFollowers, getFollowings · 주입: FollowCommandService, FollowQueryService
- `ProfileController` controller/ProfileController.java · public: getProfile, updateProfile, getProfileImage · 주입: ProfileQueryService, ProfileCommandService, FileUtils
- `AdminGradeController` controller/admin/AdminGradeController.java · public: getAllGrades, getGradeById, createGrade, updateGrade, deleteGrade, getGradeUserCounts, getGradeStatistics, runGradeAutoAssignment · 주입: AdminGradeService, ProductQueryService
- `AdminPointController` controller/admin/AdminPointController.java · public: getUserPointHistory, getUserAvailablePoints, getUserPointSummary, getPointDetail, addPointByAdmin, deductPointByAdmin, getPointStatistics · 주입: AdminPointService, ProductQueryService
- `AdminProductController` controller/admin/AdminProductController.java · public: searchUsers, getUserDetail, updateUserStatus, updateUserGrade, updateUserRole, manageUserPoints, exportUserData · 주입: AdminProductService, ProductQueryService
- `AdminSanctionController` controller/admin/AdminSanctionController.java · public: searchSanctions, getSanctionById, createSanction, updateSanction, reviewSanction, cancelSanction, getUserSanctions, getSanctionStatistics · 주입: AdminSanctionService, ProductQueryService
- `AdminAuthController` controller/command/AdminAuthController.java · public: adminLogin, findAdminEmail, checkAdminToken, refreshAdminToken, findAdminPassword, logout · 주입: AuthService, ProductQueryService, JwtTokenProvider, PasswordResetService, AuthRedisService
- `AuthController` controller/command/AuthController.java · public: login, logout, refresh, getUserEmail, checkAuthStatus
- `IdentityVerificationController` controller/command/IdentityVerificationController.java · public: verifyIdentity · 주입: IdentityVerificationService
- `OAuth2AuthorizationController` controller/command/OAuth2AuthorizationController.java · public: redirectToOAuthProvider
- `OAuthController` controller/command/OAuthController.java · public: completeOAuthSignup, checkAuthStatus, getOAuthConnections · 주입: ProductRepository, ProductUpdateService, JwtTokenProvider, AuthRedisService
- `ProductCommandController` controller/command/ProductCommandController.java · public: registerUser, verifyIdentity, updateUserInfo · 주입: ProductCommandService, ProductUpdateService, ProductQueryService, AuthRedisService, IdentityVerificationService
- `PointCommandController` controller/point/PointCommandController.java · public: addPoint, usePoint · 주입: PointCommandService
- `PointQueryController` controller/point/PointQueryController.java · public: getAllPointHistory, getAvailablePoints, getPointSummary, getPointDetail · 주입: PointQueryService
- `ProductQueryController` controller/query/ProductQueryController.java · public: findEmail, resetPasswordEligibility, resetPasswordToEmail, resetPassword, getLoginInfo, deleteAccount · 주입: AuthService, ProductQueryService, PasswordResetService, ProductUpdateService, JwtTokenProvider, ProductCommandService, AuthRedisService

### service-command (8)

- `AuthService` service/command/AuthService.java · public: login · 주입: ProductRepository, PasswordEncoder, JwtTokenProvider
- `EmailService` service/command/EmailService.java · public: sendEmail · 주입: JavaMailSender
- `IdentityVerificationService` service/command/IdentityVerificationService.java · public: verifyIdentity · 주입: RestTemplate, ObjectMapper
- `PasswordResetService` service/command/PasswordResetService.java · public: checkPasswordResetEligibility, resetPassword, checkPasswordResetAndSendEmail, AdminCheckPasswordResetAndSendEmail · 주입: ProductRepository, PasswordEncoder, EmailService
- `ProductCommandService` service/command/ProductCommandService.java · public: registerUser, deleteAccount · 주입: ProductRepository, PasswordEncoder, ProfileCommandService, FileUtils, PaymentCommandService, IdentityVerificationService
- `ProductSanctionService` service/command/ProductSanctionService.java
- `ProductUpdateService` service/command/ProductUpdateService.java · public: updateLoginInfo, reissueTokenAfterEmailChange · 주입: ProductRepository, PasswordEncoder, AuthRedisService, JwtTokenProvider
- `ProfileService` service/command/ProfileService.java · public: createDefaultProfile

### service-query (1)

- `ProductQueryService` service/query/ProductQueryService.java · public: checkAdminRole, findEmailByPhoneNumber, getLoginInfo, findByEmail · 주입: ProductRepository

### service (17)

- `AuthRedisService` redis/AuthRedisService.java · public: addAccessToken, getRoleByAccessToken, addRefreshToken, isAccessTokenValid, isRefreshTokenValid, getEmailByAccessToken, getAgeByAccessToken, getGenderByAccessToken, removeAccessToken, removeRefreshToken · 주입: RedisTemplate, RedisTemplate, RedisTemplate
- `CustomProductDetailsService` security/CustomProductDetailsService.java · extends/implements UserDetailsService · public: loadUserByUsername · 주입: ProductRepository, PasswordEncoder
- `CustomOAuth2ProductService` security/oauth2/CustomOAuth2ProductService.java · extends/implements OAuth2UserService · public: loadUser · 주입: ProductRepository, ProfileCommandService, PasswordEncoder
- `BlockedProfileCommandService` service/BlockProfile/BlockedProfileCommandService.java · public: blockProfile, unblockProfile · 주입: ProfileRepository, BlockedProfileRepository
- `BlockedProfileQueryService` service/BlockProfile/BlockedProfileQueryService.java · public: getBlockedProfiles · 주입: BlockedProfileRepository
- `AdminGradeService` service/admin/AdminGradeService.java · public: getAllGrades, getGradeById, createGrade, updateGrade, deleteGrade, getGradeUserCounts, getGradeStatistics, runGradeAutoAssignment · 주입: ProductGradeRepository, ProductRepository
- `AdminPointService` service/admin/AdminPointService.java · public: getUserPointHistory, getUserAvailablePoints, getUserPointSummary, getPointDetail, addPointByAdmin, deductPointByAdmin, getPointStatistics · 주입: ProductRepository, PointRepository
- `AdminProductService` service/admin/AdminProductService.java · public: searchUsers, getUserDetail, updateUserStatus, updateUserGrade, updateUserRole, manageUserPoints, exportUsersCsv · 주입: ProductRepository, ProfileRepository, PointRepository, ProductGradeRepository, ProductSanctionRepository, BankAccountRepository
- `AdminSanctionService` service/admin/AdminSanctionService.java · public: searchSanctions, getSanctionById, createSanction, updateSanction, reviewSanction, cancelSanction, getUserSanctions, getSanctionStatistics, processExpiredSanctions · 주입: ProductSanctionRepository, ProductRepository
- `BankAccountCommandService` service/bankaccount/BankAccountCommandService.java · public: createOrUpdateBankAccount, deleteBankAccount · 주입: ProductRepository
- `BankAccountQueryService` service/bankaccount/BankAccountQueryService.java · public: getBankAccount · 주입: BankAccountRepository
- `FollowCommandService` service/follow/FollowCommandService.java · public: createFollow, deleteFollow · 주입: FollowRepository, ProfileRepository, PaymentCommandService
- `FollowQueryService` service/follow/FollowQueryService.java · public: getFollowers, getFollowings · 주입: FollowRepository, ProfileRepository
- `PointCommandService` service/point/PointCommandService.java · public: addPoint, usePoint, expirePoints · 주입: PointRepository, ProductQueryService
- `PointQueryService` service/point/PointQueryService.java · public: getAllPointHistory, getAvailablePoints, getPointSummary, getPointDetail · 주입: PointRepository, ProductQueryService
- `ProfileCommandService` service/profile/ProfileCommandService.java · public: createDefaultProfile, updateProfile · 주입: ProfileRepository, FileUtils
- `ProfileQueryService` service/profile/ProfileQueryService.java · public: getProfileInfo, getProfileByEmail, getProfileImageFileName · 주입: ProfileRepository, BlockedProfileQueryService

### repository (8)

- `BankAccountRepository` (interface) repository/BankAccountRepository.java · extends/implements JpaRepository · public: findByUser_Email
- `BlockedProfileRepository` (interface) repository/BlockedProfileRepository.java · extends/implements JpaRepository · public: findByProfile, findByProfileAndBlockedProfileId, findBlockedProfileIdsByProfile, findAllByProfileWithBlocked, findByProfileAndBlockedProfile, findAllByProfileEmailWithBlocked
- `FollowRepository` (interface) repository/FollowRepository.java · extends/implements JpaRepository · public: findByFollowerAndFollowing, findFollowersByProfileId, findFollowingsByProfileId
- `PointRepository` (interface) repository/PointRepository.java · extends/implements JpaRepository · public: findByUserOrderByCreatedDateDesc, findByUserAndStatusOrderByExpirationDateAsc, findByStatusAndExpirationDateBefore, getTotalAvailablePoints, findExpiringPoints
- `ProductGradeRepository` (interface) repository/ProductGradeRepository.java · extends/implements JpaRepository · public: findByLevel, findByName, findAllByOrderByLevelAsc, findGradeByPurchaseAmount, countUsersByGradeId, countUsersGroupByGrade
- `ProductRepository` (interface) repository/ProductRepository.java · extends/implements JpaRepository, JpaSpecificationExecutor · public: findByReferralCode, findByEmail, findByEmailAndPhoneNumber, existsByEmail, findByCi, findByDi, findByPhoneNumber, findByMultipleCriteria, searchUsers, countUsersByGrade, countActiveUsers, findRecentlyJoinedUsers
- `ProductSanctionRepository` (interface) repository/ProductSanctionRepository.java · extends/implements JpaRepository, JpaSpecificationExecutor · public: findByUserOrderByCreatedDateDesc, findByUserAndStatusOrderByStartDateDesc, findByStatus, findByType, findExpirableSanctions, findByCreatedDateBetween, findByUserEmail, countActiveSanctions, countExpiredSanctions, countPendingSanctions, countTotalSanctions
- `ProfileRepository` (interface) repository/ProfileRepository.java · extends/implements JpaRepository · public: findByUserEmailWithFetchJoin, findByUser

### entity (15)

- `BankAccount` entity/BankAccount.java · extends/implements BaseTimeEntity · public: updateBankAccount, assignUser, unassignUser
- `BlockedProfile` entity/BlockedProfile.java · extends/implements BaseTimeEntity · public: assignProfile, assignBlockedProfile
- `Follow` entity/Follow.java · extends/implements BaseTimeEntity · public: addFollower, addFollowing
- `Gender` (enum) entity/Gender.java
- `OAuthConnection` entity/OAuthConnection.java · public: setUser
- `Point` entity/Point.java · extends/implements BaseTimeEntity · public: assignUser, unassignUser, use, expire, setReason, isValid, setExpirationDate, setStatus
- `PointStatus` (enum) entity/PointStatus.java
- `Product` entity/Product.java · extends/implements BaseTimeEntity · public: addOAuthConnection, addGrade, addActive, setVerified, hasOAuthConnection, changeRole, updateUser, updatePassword, updateConsent, updateLoginInfo, addReferrer, removeReferrer, addProfile, removeProfile, assignBankAccount, removeBankAccount, addPaymentInfo, removePaymentInfo, addAddress, removeAddress, addInterest, removeInterest, addPoint, removePoint, calculateTotalPoints
- `ProductGrade` entity/ProductGrade.java · extends/implements BaseTimeEntity · public: updateGrade, addUser, removeUser
- `ProductSanction` entity/ProductSanction.java · extends/implements BaseTimeEntity · public: assignUser, updateStatus, approve, reject, cancel, isExpired, expire, setReason, setDetails, setType, setStartDate, setEndDate
- `Profile` entity/Profile.java · extends/implements BaseTimeEntity · public: updateProfile, addFollower, addFollowing, removeFollower, removeFollowing, addStyle, addDataStyle, removeStyle, addBlockedProfile, removeBlockedProfile, addUser, updateProfileImageUrl
- `Role` (enum) entity/Role.java
- `SanctionStatus` (enum) entity/SanctionStatus.java
- `SanctionType` (enum) entity/SanctionType.java
- `ShoeSize` (enum) entity/ShoeSize.java · public: getValue

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
- `ProductGradeDto` dto/ProductGradeDto.java
- `ProductManagementDto` dto/ProductManagementDto.java
- `ProductRegistrationDto` dto/ProductRegistrationDto.java
- `ProfileInfoDto` dto/ProfileInfoDto.java
- `ProfileUpdateDto` dto/ProfileUpdateDto.java
- `SanctionDto` dto/SanctionDto.java
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
- `ProductErrorCode` exception/ProductErrorCode.java
- `ProductException` exception/ProductException.java
- `ProductNotFoundException` exception/ProductNotFoundException.java
- `ProfileImageTooLargeException` exception/ProfileImageTooLargeException.java
- `ProfileNotFoundException` exception/ProfileNotFoundException.java

### other (10)

- `ProductSanctionSpecifications` repository/ProductSanctionSpecifications.java · public: withSearchCriteria, hasUserId, hasStatus, hasType, createdBetween, startDateBetween, endDateBetween, userEmailLike
- `PointExpirationScheduler` scheduler/PointExpirationScheduler.java · public: checkExpiredPoints · 주입: PointCommandService
- `OAuth2AuthenticationSuccessHandler` security/oauth2/OAuth2AuthenticationSuccessHandler.java · extends/implements SimpleUrlAuthenticationSuccessHandler · public: onAuthenticationSuccess · 주입: JwtTokenProvider, AuthRedisService, ProductRepository
- `OAuthAttributes` security/oauth2/OAuthAttributes.java · public: of
- `PasswordUtils` utils/PasswordUtils.java · public: generateRandomPassword
- `BankAccountControllerValidator` validate/BankAccountControllerValidator.java · public: validateBankAccountDto
- `BlockedProfileControllerValidator` validate/BlockedProfileControllerValidator.java
- `FollowControllerValidator` validate/FollowControllerValidator.java
- `ProductControllerValidator` validate/ProductControllerValidator.java · public: validateUserRegistrationDto, validateLoginRequestDto, validateEmailFindRequestDto, validatePasswordResetEligibilityDto, validatePasswordResetDto, validateLoginInfoUpdateDto
- `ProfileControllerValidator` validate/ProfileControllerValidator.java

### 다른 도메인 참조 (import·주입 기준)

- **notification**: `NotificationInfo` ← Product
- **payment**: `PaymentCategory` ← FollowCommandService; `PaymentCommandService` ← FollowCommandService, ProductCommandService; `PaymentType` ← FollowCommandService
- **sale**: `Sale` ← Profile
- **shipment**: `Shipment` ← Product
- **user**: `Interest` ← Product

### 이 도메인을 참조하는 다른 도메인

- **accessLog**: Product, ProductQueryService, ProductRepository
- **address**: Product
- **event**: ProductQueryService
- **faq**: Product, ProductQueryService
- **global**: AuthRedisService, AuthService, BankAccountRepository, CustomOAuth2ProductService, Gender, Product, ProductRepository, ProfileCommandService, ProfileRepository, Role
- **monitoring**: Product, ProductQueryService
- **notice**: Product
- **notification**: Product, ProductQueryService, ProductRepository
- **order**: ProductQueryService
- **payment**: Product, ProductRepository
- **sale**: Gender, Profile, ProfileQueryService
- **shipment**: Product, ProductRepository
- **style**: BankAccountQueryService, Product, ProductQueryService
- **user**: Gender, Product, ProductQueryService
- **warehouseStorage**: ProductQueryService
- **weather**: ProductQueryService

## 4. JPA 엔티티 연관

- `BankAccount` @Entity · extends BaseTimeEntity
  - user: @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) → `Product`
- `BlockedProfile` @Entity · extends BaseTimeEntity
  - profile: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "profile_id", nullable = false) → `Profile`
  - blockedProfile: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "blocked_profile_id", nullable = false) → `Profile`
- `Follow` @Entity @Table(name = "follows") · extends BaseTimeEntity
  - follower: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "follower_profile_id", nullable = false) → `Profile`
  - following: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "following_profile_id", nullable = false) → `Profile`
- `OAuthConnection` @Entity
  - user: @ManyToOne @JoinColumn(name = "user_id") → `Product`
- `Point` @Entity · extends BaseTimeEntity
  - user: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") → `Product`
- `Product` @Entity @Table(name = "users") · extends BaseTimeEntity
  - profile: @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true) → `Profile`
  - interests: @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true) → `Interest` [user]
  - addresses: @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true) → `Shipment` [shipment]
  - paymentInfos: @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true) → `NotificationInfo` [notification]
  - bankAccount: @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true) → `BankAccount`
  - points: @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true) → `Point`
  - referredUsers: @OneToMany(mappedBy = "referrer", cascade = CascadeType.ALL, orphanRemoval = true) → `Product`
  - referrer: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "referrerCode", referencedColumnName = "referralCode", insertable = false, updatable = false) → `Product`
  - oauthConnections: @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true) → `OAuthConnection`
  - grade: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "grade_id") → `ProductGrade`
- `ProductGrade` @Entity @Table(name = "user_grades") · extends BaseTimeEntity
  - users: @OneToMany(mappedBy = "grade", fetch = FetchType.LAZY) → `Product`
- `ProductSanction` @Entity @Table(name = "user_sanctions") · extends BaseTimeEntity
  - user: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) → `Product`
- `Profile` @Entity · extends BaseTimeEntity
  - user: @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) → `Product`
  - followings: @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true) → `Follow`
  - followers: @OneToMany(mappedBy = "following", cascade = CascadeType.ALL, orphanRemoval = true) → `Follow`
  - blockedByProfiles: @OneToMany(mappedBy = "blockedProfile", cascade = CascadeType.ALL, orphanRemoval = true) → `BlockedProfile`
  - styles: @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true) → `Sale` [sale]

다른 도메인 엔티티에서 들어오는 연관:

- `AccessLog.user` @ManyToOne → `Product` [accessLog]
- `Faq.user` @ManyToOne → `Product` [faq]
- `FaqBid.user` @ManyToOne → `Product` [faq]
- `GPTUsageLog.user` @ManyToOne → `Product` [monitoring]
- `Monitoring.user` @ManyToOne → `Product` [monitoring]
- `Notice.user` @ManyToOne → `Product` [notice]
- `Notification.user` @ManyToOne → `Product` [notification]
- `NotificationInfo.user` @ManyToOne → `Product` [notification]
- `Payment.user` @ManyToOne → `Product` [payment]
- `Sale.profile` @ManyToOne → `Profile` [sale]
- `SaleComment.profile` @ManyToOne → `Profile` [sale]
- `SaleCommentLike.profile` @ManyToOne → `Profile` [sale]
- `SaleInterest.profile` @ManyToOne → `Profile` [sale]
- `SaleLike.profile` @ManyToOne → `Profile` [sale]
- `Shipment.user` @ManyToOne → `Product` [shipment]
- `Style.seller` @ManyToOne → `Product` [style]
- `StyleBid.seller` @ManyToOne → `Product` [style]
- `Interest.user` @ManyToOne → `Product` [user]

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

