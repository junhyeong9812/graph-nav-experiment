# adminuser 도메인

기준 경로: `src/main/java/com/fream/back/domain/adminuser/` · 파일 53개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/admin/bank-account` | `AdminBankAccountController#createOrUpdateBankAccount` | `this#extractEmailFromSecurityContext`, `AdminBankAccountCommandService#createOrUpdateBankAccount` |
| GET | `/admin/bank-account` | `AdminBankAccountController#getBankAccount` | `this#extractEmailFromSecurityContext`, `AdminBankAccountQueryService#getBankAccount` |
| DELETE | `/admin/bank-account` | `AdminBankAccountController#deleteBankAccount` | `this#extractEmailFromSecurityContext`, `AdminBankAccountCommandService#deleteBankAccount` |
| POST | `/admin/profiles/blocked` | `AdminBlockedProfileController#blockProfile` | `this#extractEmailFromSecurityContext`, `AdminBlockedProfileCommandService#blockProfile` |
| DELETE | `/admin/profiles/blocked` | `AdminBlockedProfileController#unblockProfile` | `this#extractEmailFromSecurityContext`, `AdminBlockedProfileCommandService#unblockProfile` |
| GET | `/admin/profiles/blocked` | `AdminBlockedProfileController#getBlockedProfiles` | `this#extractEmailFromSecurityContext`, `AdminBlockedProfileQueryService#getBlockedProfiles` |
| POST | `/admin/follows/{profileId}` | `AdminFollowController#createFollow` | `this#extractEmailFromSecurityContext`, `AdminFollowCommandService#createFollow` |
| DELETE | `/admin/follows/{profileId}` | `AdminFollowController#deleteFollow` | `this#extractEmailFromSecurityContext`, `AdminFollowCommandService#deleteFollow` |
| GET | `/admin/follows/followers` | `AdminFollowController#getFollowers` | `this#extractEmailFromSecurityContext`, `AdminFollowQueryService#getFollowers` |
| GET | `/admin/follows/followings` | `AdminFollowController#getFollowings` | `this#extractEmailFromSecurityContext`, `AdminFollowQueryService#getFollowings` |
| GET | `/admin/profiles` | `AdminProfileController#getProfile` | `this#extractEmailFromSecurityContext`, `AdminProfileQueryService#getProfileInfo` |
| PUT | `/admin/profiles` | `AdminProfileController#updateProfile` | `this#extractEmailFromSecurityContext`, `ProfileCommandService#updateProfile` |
| GET | `/admin/profiles/{profileId}/image` | `AdminProfileController#getProfileImage` | `AdminProfileQueryService#getProfileImageFileName` |
| GET | `/admin/admin/users/grades` | `AdminAdminGradeController#getAllGrades` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminAdminGradeService#getAllGrades` |
| GET | `/admin/admin/users/grades/{gradeId}` | `AdminAdminGradeController#getGradeById` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminAdminGradeService#getGradeById` |
| POST | `/admin/admin/users/grades` | `AdminAdminGradeController#createGrade` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminAdminGradeService#createGrade` |
| PUT | `/admin/admin/users/grades/{gradeId}` | `AdminAdminGradeController#updateGrade` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminAdminGradeService#updateGrade` |
| DELETE | `/admin/admin/users/grades/{gradeId}` | `AdminAdminGradeController#deleteGrade` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminAdminGradeService#deleteGrade` |
| GET | `/admin/admin/users/grades/counts` | `AdminAdminGradeController#getGradeUserCounts` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminAdminGradeService#getGradeUserCounts` |
| GET | `/admin/admin/users/grades/statistics` | `AdminAdminGradeController#getGradeStatistics` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminAdminGradeService#getGradeStatistics` |
| POST | `/admin/admin/users/grades/auto-assign` | `AdminAdminGradeController#runGradeAutoAssignment` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminAdminGradeService#runGradeAutoAssignment` |
| GET | `/admin/admin/users/points/user/{userId}` | `AdminAdminPointController#getUserPointHistory` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminAdminPointService#getUserPointHistory` |
| GET | `/admin/admin/users/points/user/{userId}/available` | `AdminAdminPointController#getUserAvailablePoints` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminAdminPointService#getUserAvailablePoints` |
| GET | `/admin/admin/users/points/user/{userId}/summary` | `AdminAdminPointController#getUserPointSummary` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminAdminPointService#getUserPointSummary` |
| GET | `/admin/admin/users/points/{pointId}` | `AdminAdminPointController#getPointDetail` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminAdminPointService#getPointDetail` |
| POST | `/admin/admin/users/points/user/{userId}/add` | `AdminAdminPointController#addPointByAdmin` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminAdminPointService#addPointByAdmin` |
| POST | `/admin/admin/users/points/user/{userId}/deduct` | `AdminAdminPointController#deductPointByAdmin` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminAdminPointService#deductPointByAdmin` |
| GET | `/admin/admin/users/points/statistics` | `AdminAdminPointController#getPointStatistics` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminAdminPointService#getPointStatistics` |
| GET | `/admin/admin/users/sanctions/search` | `AdminAdminSanctionController#searchSanctions` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminAdminSanctionService#searchSanctions` |
| GET | `/admin/admin/users/sanctions/{sanctionId}` | `AdminAdminSanctionController#getSanctionById` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminAdminSanctionService#getSanctionById` |
| POST | `/admin/admin/users/sanctions` | `AdminAdminSanctionController#createSanction` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminAdminSanctionService#createSanction` |
| PUT | `/admin/admin/users/sanctions/{sanctionId}` | `AdminAdminSanctionController#updateSanction` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminAdminSanctionService#updateSanction` |
| PATCH | `/admin/admin/users/sanctions/{sanctionId}/review` | `AdminAdminSanctionController#reviewSanction` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminAdminSanctionService#reviewSanction` |
| PATCH | `/admin/admin/users/sanctions/{sanctionId}/cancel` | `AdminAdminSanctionController#cancelSanction` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminAdminSanctionService#cancelSanction` |
| GET | `/admin/admin/users/sanctions/user/{userId}` | `AdminAdminSanctionController#getUserSanctions` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminAdminSanctionService#getUserSanctions` |
| GET | `/admin/admin/users/sanctions/statistics` | `AdminAdminSanctionController#getSanctionStatistics` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminAdminSanctionService#getSanctionStatistics` |
| GET | `/admin/admin/users/search` | `AdminAdminUserController#searchUsers` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminAdminUserService#searchUsers` |
| GET | `/admin/admin/users/{userId}` | `AdminAdminUserController#getUserDetail` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminAdminUserService#getUserDetail` |
| PATCH | `/admin/admin/users/{userId}/status` | `AdminAdminUserController#updateUserStatus` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminAdminUserService#updateUserStatus` |
| PATCH | `/admin/admin/users/{userId}/grade` | `AdminAdminUserController#updateUserGrade` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminAdminUserService#updateUserGrade` |
| PATCH | `/admin/admin/users/{userId}/role` | `AdminAdminUserController#updateUserRole` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminAdminUserService#updateUserRole` |
| POST | `/admin/admin/users/{userId}/points` | `AdminAdminUserController#manageUserPoints` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminAdminUserService#manageUserPoints` |
| GET | `/admin/admin/users/export` | `AdminAdminUserController#exportUserData` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminAdminUserService#exportUsersCsv` |
| POST | `/admin/admin/auth/login` | `AdminAdminAuthController#adminLogin` | `this#getClientIp`, `AuthService#login`, `JwtTokenProvider#getEmailFromToken`, `AdminUserQueryService#checkAdminRole`, `this#setCookie` |
| POST | `/admin/admin/auth/find-email` | `AdminAdminAuthController#findAdminEmail` | `AdminUserQueryService#findEmailByPhoneNumber`, `AdminUserQueryService#checkAdminRole` |
| GET | `/admin/admin/auth/check` | `AdminAdminAuthController#checkAdminToken` | `this#getCookieValue`, `JwtTokenProvider#validateToken`, `JwtTokenProvider#getEmailFromToken`, `AdminUserQueryService#checkAdminRole` |
| POST | `/admin/admin/auth/refresh` | `AdminAdminAuthController#refreshAdminToken` | `this#getCookieValue`, `JwtTokenProvider#validateToken`, `AuthRedisService#isRefreshTokenValid`, `JwtTokenProvider#getEmailFromToken`, `AdminUserQueryService#findByEmail`, `AdminUserQueryService#checkAdminRole`, `this#getClientIp`, `JwtTokenProvider#generateTokenPair`, `this#setCookie` |
| POST | `/admin/admin/auth/find-password` | `AdminAdminAuthController#findAdminPassword` | `AdminUserQueryService#findByEmail`, `AdminPasswordResetService#AdminCheckPasswordResetAndSendEmail` |
| POST | `/admin/admin/auth/logout` | `AdminAdminAuthController#logout` | `this#getCookieValue`, `JwtTokenProvider#validateToken`, `JwtTokenProvider#getEmailFromToken`, `AdminUserQueryService#checkAdminRole`, `AuthRedisService#removeAccessToken`, `AuthRedisService#removeRefreshToken`, `this#expireCookie` |
| POST | `/admin/auth/login` | `AdminAuthController#login` |  |
| POST | `/admin/auth/logout` | `AdminAuthController#logout` |  |
| POST | `/admin/auth/refresh` | `AdminAuthController#refresh` |  |
| GET | `/admin/auth/email` | `AdminAuthController#getUserEmail` |  |
| GET | `/admin/auth/status` | `AdminAuthController#checkAuthStatus` |  |
| POST | `/admin/identity-verification/verify` | `AdminIdentityVerificationController#verifyIdentity` | `AdminIdentityVerificationService#verifyIdentity` |
| GET | `/oauth2/authorization/{registrationId}` | `AdminOAuth2AuthorizationController#redirectToOAuthProvider` |  |
| POST | `/admin/oauth/complete-signup` | `AdminOAuthController#completeOAuthSignup` | `this#extractEmailFromToken`, `UserRepository#findByEmail`, `UserRepository#findByReferralCode`, `UserRepository#save~`, `this#getClientIp`, `JwtTokenProvider#generateTokenPair`, `this#setCookie` |
| GET | `/admin/oauth/status` | `AdminOAuthController#checkAuthStatus` | `this#getCookieValue`, `JwtTokenProvider#validateToken`, `AuthRedisService#isAccessTokenValid`, `JwtTokenProvider#getEmailFromToken` |
| GET | `/admin/oauth/connections` | `AdminOAuthController#getOAuthConnections` | `this#getCookieValue`, `JwtTokenProvider#getEmailFromToken`, `UserRepository#findByEmail` |
| POST | `/admin/users/register` | `AdminUserCommandController#registerUser` | `AdminUserCommandService#registerUser` |
| POST | `/admin/users/verify-identity` | `AdminUserCommandController#verifyIdentity` | `AdminIdentityVerificationService#verifyIdentity` |
| PUT | `/admin/users/update` | `AdminUserCommandController#updateUserInfo` | `AdminUserUpdateService#updateLoginInfo`, `AdminUserQueryService#getLoginInfo`, `this#getCookieValue`, `AuthRedisService#removeAccessToken`, `AuthRedisService#removeRefreshToken`, `AdminUserUpdateService#reissueTokenAfterEmailChange`, `this#setCookie` |
| POST | `/admin/points/commands` | `AdminPointCommandController#addPoint` | `AdminPointCommandService#addPoint` |
| POST | `/admin/points/commands/use` | `AdminPointCommandController#usePoint` | `AdminPointCommandService#usePoint` |
| GET | `/admin/points/queries` | `AdminPointQueryController#getAllPointHistory` | `AdminPointQueryService#getAllPointHistory` |
| GET | `/admin/points/queries/available` | `AdminPointQueryController#getAvailablePoints` | `AdminPointQueryService#getAvailablePoints` |
| GET | `/admin/points/queries/summary` | `AdminPointQueryController#getPointSummary` | `AdminPointQueryService#getPointSummary` |
| GET | `/admin/points/queries/{pointId}` | `AdminPointQueryController#getPointDetail` | `AdminPointQueryService#getPointDetail` |
| POST | `/admin/users/find-email` | `AdminUserQueryController#findEmail` | `AdminUserQueryService#findEmailByPhoneNumber` |
| POST | `/admin/users/reset-password` | `AdminUserQueryController#resetPasswordEligibility` | `AdminPasswordResetService#checkPasswordResetEligibility` |
| POST | `/admin/users/reset-password-sandEmail` | `AdminUserQueryController#resetPasswordToEmail` | `AdminPasswordResetService#checkPasswordResetAndSendEmail` |
| POST | `/admin/users/reset` | `AdminUserQueryController#resetPassword` | `AdminPasswordResetService#resetPassword` |
| GET | `/admin/users/login-info` | `AdminUserQueryController#getLoginInfo` | `AdminUserQueryService#getLoginInfo` |
| DELETE | `/admin/users/delete-account` | `AdminUserQueryController#deleteAccount` | `AdminUserCommandService#deleteAccount` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `AdminAdminGradeService`
  - `getAllGrades` → AdminUserGradeRepository#findAllByOrderByLevelAsc
  - `getGradeById` → AdminUserGradeRepository#findById~, this#convertToGradeResponseDto
  - `createGrade` → AdminUserGradeRepository#findByLevel, AdminUserGradeRepository#findByName, AdminUserGradeRepository#save~, this#convertToGradeResponseDto
  - `updateGrade` → AdminUserGradeRepository#findById~, AdminUserGradeRepository#findByName, this#convertToGradeResponseDto
  - `deleteGrade` → AdminUserGradeRepository#findById~, AdminUserGradeRepository#countUsersByGradeId, AdminUserGradeRepository#delete~
  - `getGradeUserCounts` → AdminUserGradeRepository#countUsersGroupByGrade, AdminUserGradeRepository#findById~
  - `getGradeStatistics` → AdminUserGradeRepository#findAllByOrderByLevelAsc, AdminUserGradeRepository#countUsersByGradeId
  - `runGradeAutoAssignment` → UserRepository#findAll~, AdminUserGradeRepository#findGradeByPurchaseAmount
  - `convertToGradeResponseDto` → AdminUserGradeRepository#countUsersByGradeId
- `AdminAdminPointService`
  - `getUserPointHistory` → this#getUserById, AdminPointRepository#findByUserOrderByCreatedDateDesc
  - `getUserAvailablePoints` → this#getUserById, AdminPointRepository#findByUserAndStatusOrderByExpirationDateAsc
  - `getUserPointSummary` → this#getUserById, AdminPointRepository#findByUserAndStatusOrderByExpirationDateAsc
  - `getPointDetail` → AdminPointRepository#findById~
  - `addPointByAdmin` → this#getUserById, AdminPointRepository#save~
  - `deductPointByAdmin` → this#getUserById, AdminPointRepository#findByUserAndStatusOrderByExpirationDateAsc, AdminPointRepository#save~
  - `getUserById` → UserRepository#findById~
- `AdminAdminSanctionService`
  - `searchSanctions` → AdminUserSanctionRepository#findAll~
  - `getSanctionById` → AdminUserSanctionRepository#findById~, this#convertToSanctionResponseDto
  - `createSanction` → UserRepository#findById~, AdminUserSanctionRepository#save~, this#convertToSanctionResponseDto
  - `updateSanction` → AdminUserSanctionRepository#findById~, this#convertToSanctionResponseDto
  - `reviewSanction` → AdminUserSanctionRepository#findById~, this#convertToSanctionResponseDto
  - `cancelSanction` → AdminUserSanctionRepository#findById~, this#hasOtherActiveBanSanctions, this#convertToSanctionResponseDto
  - `getUserSanctions` → UserRepository#findById~, AdminUserSanctionRepository#findByUserOrderByCreatedDateDesc
  - `getSanctionStatistics` → AdminUserSanctionRepository#countTotalSanctions, AdminUserSanctionRepository#countActiveSanctions, AdminUserSanctionRepository#countExpiredSanctions, AdminUserSanctionRepository#countPendingSanctions
  - `processExpiredSanctions` → AdminUserSanctionRepository#findExpirableSanctions, this#hasOtherActiveBanSanctions
  - `hasOtherActiveBanSanctions` → AdminUserSanctionRepository#findByUserAndStatusOrderByStartDateDesc
- `AdminAdminUserService`
  - `searchUsers` → UserRepository#findAll~
  - `getUserDetail` → UserRepository#findById~, this#convertToUserDetailResponseDto
  - `updateUserStatus` → UserRepository#findById~, AdminUserSanctionRepository#save~, this#convertToUserDetailResponseDto
  - `updateUserGrade` → UserRepository#findById~, AdminUserGradeRepository#findById~, this#convertToUserDetailResponseDto
  - `updateUserRole` → UserRepository#findById~, this#convertToUserDetailResponseDto
  - `manageUserPoints` → UserRepository#findById~, AdminPointRepository#save~, AdminPointRepository#findByUserAndStatusOrderByExpirationDateAsc
  - `exportUsersCsv` → UserRepository#findAll~
  - `convertToUserSearchResponseDto` → ProfileRepository#findByUser
  - `convertToUserDetailResponseDto` → ProfileRepository#findByUser, BankAccountRepository#findByUser_Email, AdminUserSanctionRepository#findByUserAndStatusOrderByStartDateDesc, this#convertToProfileDto, this#convertToBankAccountDto, this#convertToSanctionBriefDtoList
- `AdminBankAccountCommandService`
  - `createOrUpdateBankAccount` → UserRepository#findByEmail
  - `deleteBankAccount` → UserRepository#findByEmail
- `AdminBankAccountQueryService`
  - `getBankAccount` → BankAccountRepository#findByUser_Email
- `AdminBlockedProfileCommandService`
  - `blockProfile` → ProfileRepository#findByUserEmailWithFetchJoin, ProfileRepository#findById~, AdminBlockedProfileRepository#findByProfileAndBlockedProfile, AdminBlockedProfileRepository#save~
  - `unblockProfile` → ProfileRepository#findByUserEmailWithFetchJoin, ProfileRepository#findById~, AdminBlockedProfileRepository#findByProfileAndBlockedProfile, AdminBlockedProfileRepository#delete~
- `AdminBlockedProfileQueryService`
  - `getBlockedProfiles` → AdminBlockedProfileRepository#findAllByProfileWithBlocked, AdminBlockedProfileRepository#findAllByProfileEmailWithBlocked
- `AdminCustomUserDetailsService`
  - `loadUserByUsername` → UserRepository#findByEmail
- `AdminFollowCommandService`
  - `createFollow` → ProfileRepository#findByUserEmailWithFetchJoin, ProfileRepository#findById~, AdminFollowRepository#findByFollowerAndFollowing, AdminFollowRepository#save~, AdminNotificationCommandService#createNotification
  - `deleteFollow` → ProfileRepository#findByUserEmailWithFetchJoin, ProfileRepository#findById~, AdminFollowRepository#findByFollowerAndFollowing, AdminFollowRepository#delete~
- `AdminFollowQueryService`
  - `getFollowers` → ProfileRepository#findByUserEmailWithFetchJoin, AdminFollowRepository#findFollowersByProfileId
  - `getFollowings` → ProfileRepository#findByUserEmailWithFetchJoin, AdminFollowRepository#findFollowingsByProfileId
- `AdminOAuthAttributes`
  - `of` → this#ofGoogle, this#ofNaver, this#ofKakao
- `AdminPasswordResetService`
  - `checkPasswordResetEligibility` → UserRepository#findByEmailAndPhoneNumber
  - `resetPassword` → UserRepository#findByEmailAndPhoneNumber
  - `checkPasswordResetAndSendEmail` → UserRepository#findByEmailAndPhoneNumber, AdminEmailService#sendEmail
  - `AdminCheckPasswordResetAndSendEmail` → UserRepository#findByEmailAndPhoneNumber, AdminEmailService#sendEmail
- `AdminPasswordUtils`
  - `generateRandomPassword` → this#isValidPassword
- `AdminPointCommandService`
  - `addPoint` → this#validateAddPointRequest, AdminUserQueryService#findByEmail, AdminPointRepository#save~
  - `usePoint` → this#validateUsePointRequest, AdminUserQueryService#findByEmail, AdminPointRepository#getTotalAvailablePoints, AdminPointRepository#findByUserAndStatusOrderByExpirationDateAsc, AdminPointRepository#save~
  - `expirePoints` → AdminPointRepository#findByStatusAndExpirationDateBefore
- `AdminPointExpirationScheduler`
  - `checkExpiredPoints` → AdminPointCommandService#expirePoints
- `AdminPointQueryService`
  - `getAllPointHistory` → AdminUserQueryService#findByEmail, AdminPointRepository#findByUserOrderByCreatedDateDesc
  - `getAvailablePoints` → AdminUserQueryService#findByEmail, AdminPointRepository#findByUserAndStatusOrderByExpirationDateAsc
  - `getPointSummary` → AdminUserQueryService#findByEmail, AdminPointRepository#getTotalAvailablePoints, this#getAllPointHistory, AdminPointRepository#findExpiringPoints
  - `getPointDetail` → AdminUserQueryService#findByEmail, AdminPointRepository#findById~
- `AdminProfileQueryService`
  - `getProfileInfo` → ProfileRepository#findByUserEmailWithFetchJoin, AdminBlockedProfileQueryService#getBlockedProfiles
  - `getProfileByEmail` → ProfileRepository#findByUserEmailWithFetchJoin
  - `getProfileImageFileName` → ProfileRepository#findById~
- `AdminProfileService`
  - `createDefaultProfile` → this#generateRandomProfileName
- `AdminUserCommandService`
  - `registerUser` → UserRepository#existsByEmail, AdminIdentityVerificationService#verifyIdentity, UserRepository#findByCi, this#validateAge, UserRepository#findByReferralCode, this#convertToGender, this#calculateAge, this#generateUniqueReferralCode, UserRepository#save~, ProfileCommandService#createDefaultProfile
  - `deleteAccount` → UserRepository#findByEmail, AdminNotificationCommandService#deleteNotificationsByUser, FileUtils#deleteFile, UserRepository#delete~
  - `generateUniqueReferralCode` → UserRepository#findByReferralCode
- `AdminUserControllerValidator`
  - `validateUserRegistrationDto` → this#isValidPassword
  - `validatePasswordResetDto` → this#isValidPassword
  - `validateLoginInfoUpdateDto` → this#isValidPassword
- `AdminUserQueryService`
  - `checkAdminRole` → UserRepository#findByEmail
  - `findEmailByPhoneNumber` → UserRepository#findByPhoneNumber
  - `getLoginInfo` → UserRepository#findByEmail
  - `findByEmail` → UserRepository#findByEmail
- `AdminUserUpdateService`
  - `updateLoginInfo` → UserRepository#findByEmail, UserRepository#existsByEmail
  - `reissueTokenAfterEmailChange` → UserRepository#findByEmail, AuthRedisService#removeAccessToken, AuthRedisService#removeRefreshToken, JwtTokenProvider#generateTokenPair

## 3. 클래스 목록 (역할별)

### controller (17)

- `AdminBankAccountController` controller/AdminBankAccountController.java · public: createOrUpdateBankAccount, getBankAccount, deleteBankAccount · 주입: AdminBankAccountCommandService, AdminBankAccountQueryService
- `AdminBlockedProfileController` controller/AdminBlockedProfileController.java · public: blockProfile, unblockProfile, getBlockedProfiles · 주입: AdminBlockedProfileCommandService, AdminBlockedProfileQueryService
- `AdminFollowController` controller/AdminFollowController.java · public: createFollow, deleteFollow, getFollowers, getFollowings · 주입: AdminFollowCommandService, AdminFollowQueryService
- `AdminProfileController` controller/AdminProfileController.java · public: getProfile, updateProfile, getProfileImage · 주입: AdminProfileQueryService, ProfileCommandService, FileUtils
- `AdminAdminGradeController` controller/admin/AdminAdminGradeController.java · public: getAllGrades, getGradeById, createGrade, updateGrade, deleteGrade, getGradeUserCounts, getGradeStatistics, runGradeAutoAssignment · 주입: AdminAdminGradeService, AdminUserQueryService
- `AdminAdminPointController` controller/admin/AdminAdminPointController.java · public: getUserPointHistory, getUserAvailablePoints, getUserPointSummary, getPointDetail, addPointByAdmin, deductPointByAdmin, getPointStatistics · 주입: AdminAdminPointService, AdminUserQueryService
- `AdminAdminSanctionController` controller/admin/AdminAdminSanctionController.java · public: searchSanctions, getSanctionById, createSanction, updateSanction, reviewSanction, cancelSanction, getUserSanctions, getSanctionStatistics · 주입: AdminAdminSanctionService, AdminUserQueryService
- `AdminAdminUserController` controller/admin/AdminAdminUserController.java · public: searchUsers, getUserDetail, updateUserStatus, updateUserGrade, updateUserRole, manageUserPoints, exportUserData · 주입: AdminAdminUserService, AdminUserQueryService
- `AdminAdminAuthController` controller/command/AdminAdminAuthController.java · public: adminLogin, findAdminEmail, checkAdminToken, refreshAdminToken, findAdminPassword, logout · 주입: AuthService, AdminUserQueryService, JwtTokenProvider, AdminPasswordResetService, AuthRedisService
- `AdminAuthController` controller/command/AdminAuthController.java · public: login, logout, refresh, getUserEmail, checkAuthStatus
- `AdminIdentityVerificationController` controller/command/AdminIdentityVerificationController.java · public: verifyIdentity · 주입: AdminIdentityVerificationService
- `AdminOAuth2AuthorizationController` controller/command/AdminOAuth2AuthorizationController.java · public: redirectToOAuthProvider
- `AdminOAuthController` controller/command/AdminOAuthController.java · public: completeOAuthSignup, checkAuthStatus, getOAuthConnections · 주입: UserRepository, AdminUserUpdateService, JwtTokenProvider, AuthRedisService
- `AdminUserCommandController` controller/command/AdminUserCommandController.java · public: registerUser, verifyIdentity, updateUserInfo · 주입: AdminUserCommandService, AdminUserUpdateService, AdminUserQueryService, AuthRedisService, AdminIdentityVerificationService
- `AdminPointCommandController` controller/point/AdminPointCommandController.java · public: addPoint, usePoint · 주입: AdminPointCommandService
- `AdminPointQueryController` controller/point/AdminPointQueryController.java · public: getAllPointHistory, getAvailablePoints, getPointSummary, getPointDetail · 주입: AdminPointQueryService
- `AdminUserQueryController` controller/query/AdminUserQueryController.java · public: findEmail, resetPasswordEligibility, resetPasswordToEmail, resetPassword, getLoginInfo, deleteAccount · 주입: AuthService, AdminUserQueryService, AdminPasswordResetService, AdminUserUpdateService, JwtTokenProvider, AdminUserCommandService, AuthRedisService

### service-command (7)

- `AdminEmailService` service/command/AdminEmailService.java · public: sendEmail · 주입: JavaMailSender
- `AdminIdentityVerificationService` service/command/AdminIdentityVerificationService.java · public: verifyIdentity · 주입: RestTemplate, ObjectMapper
- `AdminPasswordResetService` service/command/AdminPasswordResetService.java · public: checkPasswordResetEligibility, resetPassword, checkPasswordResetAndSendEmail, AdminCheckPasswordResetAndSendEmail · 주입: UserRepository, PasswordEncoder, AdminEmailService
- `AdminProfileService` service/command/AdminProfileService.java · public: createDefaultProfile
- `AdminUserCommandService` service/command/AdminUserCommandService.java · public: registerUser, deleteAccount · 주입: UserRepository, PasswordEncoder, ProfileCommandService, FileUtils, AdminNotificationCommandService, AdminIdentityVerificationService
- `AdminUserSanctionService` service/command/AdminUserSanctionService.java
- `AdminUserUpdateService` service/command/AdminUserUpdateService.java · public: updateLoginInfo, reissueTokenAfterEmailChange · 주입: UserRepository, PasswordEncoder, AuthRedisService, JwtTokenProvider

### service-query (1)

- `AdminUserQueryService` service/query/AdminUserQueryService.java · public: checkAdminRole, findEmailByPhoneNumber, getLoginInfo, findByEmail · 주입: UserRepository

### service (14)

- `AdminCustomUserDetailsService` security/AdminCustomUserDetailsService.java · extends/implements UserDetailsService · public: loadUserByUsername · 주입: UserRepository, PasswordEncoder
- `AdminBlockedProfileCommandService` service/BlockProfile/AdminBlockedProfileCommandService.java · public: blockProfile, unblockProfile · 주입: ProfileRepository, AdminBlockedProfileRepository
- `AdminBlockedProfileQueryService` service/BlockProfile/AdminBlockedProfileQueryService.java · public: getBlockedProfiles · 주입: AdminBlockedProfileRepository
- `AdminAdminGradeService` service/admin/AdminAdminGradeService.java · public: getAllGrades, getGradeById, createGrade, updateGrade, deleteGrade, getGradeUserCounts, getGradeStatistics, runGradeAutoAssignment · 주입: AdminUserGradeRepository, UserRepository
- `AdminAdminPointService` service/admin/AdminAdminPointService.java · public: getUserPointHistory, getUserAvailablePoints, getUserPointSummary, getPointDetail, addPointByAdmin, deductPointByAdmin, getPointStatistics · 주입: UserRepository, AdminPointRepository
- `AdminAdminSanctionService` service/admin/AdminAdminSanctionService.java · public: searchSanctions, getSanctionById, createSanction, updateSanction, reviewSanction, cancelSanction, getUserSanctions, getSanctionStatistics, processExpiredSanctions · 주입: AdminUserSanctionRepository, UserRepository
- `AdminAdminUserService` service/admin/AdminAdminUserService.java · public: searchUsers, getUserDetail, updateUserStatus, updateUserGrade, updateUserRole, manageUserPoints, exportUsersCsv · 주입: UserRepository, ProfileRepository, AdminPointRepository, AdminUserGradeRepository, AdminUserSanctionRepository, BankAccountRepository
- `AdminBankAccountCommandService` service/bankaccount/AdminBankAccountCommandService.java · public: createOrUpdateBankAccount, deleteBankAccount · 주입: UserRepository
- `AdminBankAccountQueryService` service/bankaccount/AdminBankAccountQueryService.java · public: getBankAccount · 주입: BankAccountRepository
- `AdminFollowCommandService` service/follow/AdminFollowCommandService.java · public: createFollow, deleteFollow · 주입: AdminFollowRepository, ProfileRepository, AdminNotificationCommandService
- `AdminFollowQueryService` service/follow/AdminFollowQueryService.java · public: getFollowers, getFollowings · 주입: AdminFollowRepository, ProfileRepository
- `AdminPointCommandService` service/point/AdminPointCommandService.java · public: addPoint, usePoint, expirePoints · 주입: AdminPointRepository, AdminUserQueryService
- `AdminPointQueryService` service/point/AdminPointQueryService.java · public: getAllPointHistory, getAvailablePoints, getPointSummary, getPointDetail · 주입: AdminPointRepository, AdminUserQueryService
- `AdminProfileQueryService` service/profile/AdminProfileQueryService.java · public: getProfileInfo, getProfileByEmail, getProfileImageFileName · 주입: ProfileRepository, AdminBlockedProfileQueryService

### repository (5)

- `AdminBlockedProfileRepository` (interface) repository/AdminBlockedProfileRepository.java · extends/implements JpaRepository · public: findByProfile, findByProfileAndBlockedProfileId, findBlockedProfileIdsByProfile, findAllByProfileWithBlocked, findByProfileAndBlockedProfile, findAllByProfileEmailWithBlocked
- `AdminFollowRepository` (interface) repository/AdminFollowRepository.java · extends/implements JpaRepository · public: findByFollowerAndFollowing, findFollowersByProfileId, findFollowingsByProfileId
- `AdminPointRepository` (interface) repository/AdminPointRepository.java · extends/implements JpaRepository · public: findByUserOrderByCreatedDateDesc, findByUserAndStatusOrderByExpirationDateAsc, findByStatusAndExpirationDateBefore, getTotalAvailablePoints, findExpiringPoints
- `AdminUserGradeRepository` (interface) repository/AdminUserGradeRepository.java · extends/implements JpaRepository · public: findByLevel, findByName, findAllByOrderByLevelAsc, findGradeByPurchaseAmount, countUsersByGradeId, countUsersGroupByGrade
- `AdminUserSanctionRepository` (interface) repository/AdminUserSanctionRepository.java · extends/implements JpaRepository, JpaSpecificationExecutor · public: findByUserOrderByCreatedDateDesc, findByUserAndStatusOrderByStartDateDesc, findByStatus, findByType, findExpirableSanctions, findByCreatedDateBetween, findByUserEmail, countActiveSanctions, countExpiredSanctions, countPendingSanctions, countTotalSanctions

### other (9)

- `AdminUserSanctionSpecifications` repository/AdminUserSanctionSpecifications.java · public: withSearchCriteria, hasUserId, hasStatus, hasType, createdBetween, startDateBetween, endDateBetween, userEmailLike
- `AdminPointExpirationScheduler` scheduler/AdminPointExpirationScheduler.java · public: checkExpiredPoints · 주입: AdminPointCommandService
- `AdminOAuthAttributes` security/oauth2/AdminOAuthAttributes.java · public: of
- `AdminPasswordUtils` utils/AdminPasswordUtils.java · public: generateRandomPassword
- `AdminBankAccountControllerValidator` validate/AdminBankAccountControllerValidator.java · public: validateBankAccountDto
- `AdminBlockedProfileControllerValidator` validate/AdminBlockedProfileControllerValidator.java
- `AdminFollowControllerValidator` validate/AdminFollowControllerValidator.java
- `AdminProfileControllerValidator` validate/AdminProfileControllerValidator.java
- `AdminUserControllerValidator` validate/AdminUserControllerValidator.java · public: validateUserRegistrationDto, validateLoginRequestDto, validateEmailFindRequestDto, validatePasswordResetEligibilityDto, validatePasswordResetDto, validateLoginInfoUpdateDto

### 다른 도메인 참조 (import·주입 기준)

- **adminnotification**: `AdminNotificationCommandService` ← AdminFollowCommandService, AdminUserCommandService
- **notification**: `NotificationCategory` ← AdminFollowCommandService; `NotificationType` ← AdminFollowCommandService
- **user**: `AuthRedisService` ← AdminAdminAuthController, AdminOAuthController, AdminUserCommandController, AdminUserQueryController, AdminUserUpdateService; `AuthService` ← AdminAdminAuthController, AdminUserQueryController; `BankAccount` ← AdminBankAccountCommandService, AdminBankAccountQueryService; `BankAccountRepository` ← AdminAdminUserService, AdminBankAccountQueryService; `BlockedProfile` ← AdminBlockedProfileCommandService, AdminBlockedProfileQueryService, AdminBlockedProfileRepository; `Follow` ← AdminFollowCommandService, AdminFollowQueryService, AdminFollowRepository; `Gender` ← AdminAdminAuthController, AdminUserCommandService; `Point` ← AdminAdminPointService, AdminPointCommandService, AdminPointQueryService, AdminPointRepository; `PointStatus` ← AdminAdminPointService, AdminPointCommandService, AdminPointQueryService, AdminPointRepository; `Profile` ← AdminBlockedProfileCommandService, AdminBlockedProfileQueryService, AdminBlockedProfileRepository, AdminFollowCommandService, AdminFollowQueryService, AdminFollowRepository, AdminProfileQueryService, AdminProfileService; `ProfileCommandService` ← AdminProfileController, AdminUserCommandService; `ProfileRepository` ← AdminAdminUserService, AdminBlockedProfileCommandService, AdminFollowCommandService, AdminFollowQueryService, AdminProfileQueryService; `Role` ← AdminAdminAuthController, AdminUserQueryService; `SanctionStatus` ← AdminAdminSanctionController, AdminUserSanctionRepository, AdminUserSanctionSpecifications; `SanctionType` ← AdminAdminSanctionController, AdminUserSanctionRepository, AdminUserSanctionSpecifications; `ShoeSize` ← AdminUserUpdateService; `User` ← AdminAdminAuthController, AdminAdminGradeService, AdminAdminPointService, AdminBankAccountCommandService, AdminCustomUserDetailsService, AdminOAuthController, AdminPasswordResetService, AdminPointCommandService, AdminPointQueryService, AdminPointRepository, AdminProfileService, AdminUserCommandController, AdminUserCommandService, AdminUserQueryController, AdminUserQueryService, AdminUserSanctionRepository, AdminUserSanctionSpecifications, AdminUserUpdateService; `UserGrade` ← AdminAdminGradeService, AdminUserGradeRepository; `UserRepository` ← AdminAdminGradeService, AdminAdminPointService, AdminAdminSanctionService, AdminAdminUserService, AdminBankAccountCommandService, AdminCustomUserDetailsService, AdminOAuthController, AdminPasswordResetService, AdminUserCommandService, AdminUserQueryService, AdminUserUpdateService; `UserSanction` ← AdminUserSanctionRepository, AdminUserSanctionSpecifications

### 이 도메인을 참조하는 다른 도메인

- **adminchatquestion**: AdminUserQueryService
- **adminevent**: AdminUserQueryService
- **adminfaq**: AdminUserQueryService
- **admininquiry**: AdminUserQueryService
- **admininspection**: AdminUserQueryService
- **adminnotice**: AdminUserQueryService
- **adminorder**: AdminUserQueryService
- **adminpayment**: AdminUserQueryService
- **adminproduct**: AdminUserQueryService
- **adminsale**: AdminBankAccountQueryService, AdminUserQueryService
- **adminstyle**: AdminProfileQueryService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

