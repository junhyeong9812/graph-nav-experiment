# internaluser 도메인

기준 경로: `src/main/java/com/fream/back/domain/internaluser/` · 파일 53개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/internal/bank-account` | `InternalBankAccountController#createOrUpdateBankAccount` | `this#extractEmailFromSecurityContext`, `InternalBankAccountCommandService#createOrUpdateBankAccount` |
| GET | `/internal/bank-account` | `InternalBankAccountController#getBankAccount` | `this#extractEmailFromSecurityContext`, `InternalBankAccountQueryService#getBankAccount` |
| DELETE | `/internal/bank-account` | `InternalBankAccountController#deleteBankAccount` | `this#extractEmailFromSecurityContext`, `InternalBankAccountCommandService#deleteBankAccount` |
| POST | `/internal/profiles/blocked` | `InternalBlockedProfileController#blockProfile` | `this#extractEmailFromSecurityContext`, `InternalBlockedProfileCommandService#blockProfile` |
| DELETE | `/internal/profiles/blocked` | `InternalBlockedProfileController#unblockProfile` | `this#extractEmailFromSecurityContext`, `InternalBlockedProfileCommandService#unblockProfile` |
| GET | `/internal/profiles/blocked` | `InternalBlockedProfileController#getBlockedProfiles` | `this#extractEmailFromSecurityContext`, `InternalBlockedProfileQueryService#getBlockedProfiles` |
| POST | `/internal/follows/{profileId}` | `InternalFollowController#createFollow` | `this#extractEmailFromSecurityContext`, `InternalFollowCommandService#createFollow` |
| DELETE | `/internal/follows/{profileId}` | `InternalFollowController#deleteFollow` | `this#extractEmailFromSecurityContext`, `InternalFollowCommandService#deleteFollow` |
| GET | `/internal/follows/followers` | `InternalFollowController#getFollowers` | `this#extractEmailFromSecurityContext`, `InternalFollowQueryService#getFollowers` |
| GET | `/internal/follows/followings` | `InternalFollowController#getFollowings` | `this#extractEmailFromSecurityContext`, `InternalFollowQueryService#getFollowings` |
| GET | `/internal/profiles` | `InternalProfileController#getProfile` | `this#extractEmailFromSecurityContext`, `InternalProfileQueryService#getProfileInfo` |
| PUT | `/internal/profiles` | `InternalProfileController#updateProfile` | `this#extractEmailFromSecurityContext`, `ProfileCommandService#updateProfile` |
| GET | `/internal/profiles/{profileId}/image` | `InternalProfileController#getProfileImage` | `InternalProfileQueryService#getProfileImageFileName` |
| GET | `/internal/admin/users/grades` | `InternalAdminGradeController#getAllGrades` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalAdminGradeService#getAllGrades` |
| GET | `/internal/admin/users/grades/{gradeId}` | `InternalAdminGradeController#getGradeById` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalAdminGradeService#getGradeById` |
| POST | `/internal/admin/users/grades` | `InternalAdminGradeController#createGrade` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalAdminGradeService#createGrade` |
| PUT | `/internal/admin/users/grades/{gradeId}` | `InternalAdminGradeController#updateGrade` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalAdminGradeService#updateGrade` |
| DELETE | `/internal/admin/users/grades/{gradeId}` | `InternalAdminGradeController#deleteGrade` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalAdminGradeService#deleteGrade` |
| GET | `/internal/admin/users/grades/counts` | `InternalAdminGradeController#getGradeUserCounts` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalAdminGradeService#getGradeUserCounts` |
| GET | `/internal/admin/users/grades/statistics` | `InternalAdminGradeController#getGradeStatistics` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalAdminGradeService#getGradeStatistics` |
| POST | `/internal/admin/users/grades/auto-assign` | `InternalAdminGradeController#runGradeAutoAssignment` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalAdminGradeService#runGradeAutoAssignment` |
| GET | `/internal/admin/users/points/user/{userId}` | `InternalAdminPointController#getUserPointHistory` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalAdminPointService#getUserPointHistory` |
| GET | `/internal/admin/users/points/user/{userId}/available` | `InternalAdminPointController#getUserAvailablePoints` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalAdminPointService#getUserAvailablePoints` |
| GET | `/internal/admin/users/points/user/{userId}/summary` | `InternalAdminPointController#getUserPointSummary` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalAdminPointService#getUserPointSummary` |
| GET | `/internal/admin/users/points/{pointId}` | `InternalAdminPointController#getPointDetail` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalAdminPointService#getPointDetail` |
| POST | `/internal/admin/users/points/user/{userId}/add` | `InternalAdminPointController#addPointByAdmin` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalAdminPointService#addPointByAdmin` |
| POST | `/internal/admin/users/points/user/{userId}/deduct` | `InternalAdminPointController#deductPointByAdmin` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalAdminPointService#deductPointByAdmin` |
| GET | `/internal/admin/users/points/statistics` | `InternalAdminPointController#getPointStatistics` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalAdminPointService#getPointStatistics` |
| GET | `/internal/admin/users/sanctions/search` | `InternalAdminSanctionController#searchSanctions` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalAdminSanctionService#searchSanctions` |
| GET | `/internal/admin/users/sanctions/{sanctionId}` | `InternalAdminSanctionController#getSanctionById` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalAdminSanctionService#getSanctionById` |
| POST | `/internal/admin/users/sanctions` | `InternalAdminSanctionController#createSanction` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalAdminSanctionService#createSanction` |
| PUT | `/internal/admin/users/sanctions/{sanctionId}` | `InternalAdminSanctionController#updateSanction` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalAdminSanctionService#updateSanction` |
| PATCH | `/internal/admin/users/sanctions/{sanctionId}/review` | `InternalAdminSanctionController#reviewSanction` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalAdminSanctionService#reviewSanction` |
| PATCH | `/internal/admin/users/sanctions/{sanctionId}/cancel` | `InternalAdminSanctionController#cancelSanction` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalAdminSanctionService#cancelSanction` |
| GET | `/internal/admin/users/sanctions/user/{userId}` | `InternalAdminSanctionController#getUserSanctions` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalAdminSanctionService#getUserSanctions` |
| GET | `/internal/admin/users/sanctions/statistics` | `InternalAdminSanctionController#getSanctionStatistics` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalAdminSanctionService#getSanctionStatistics` |
| GET | `/internal/admin/users/search` | `InternalAdminUserController#searchUsers` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalAdminUserService#searchUsers` |
| GET | `/internal/admin/users/{userId}` | `InternalAdminUserController#getUserDetail` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalAdminUserService#getUserDetail` |
| PATCH | `/internal/admin/users/{userId}/status` | `InternalAdminUserController#updateUserStatus` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalAdminUserService#updateUserStatus` |
| PATCH | `/internal/admin/users/{userId}/grade` | `InternalAdminUserController#updateUserGrade` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalAdminUserService#updateUserGrade` |
| PATCH | `/internal/admin/users/{userId}/role` | `InternalAdminUserController#updateUserRole` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalAdminUserService#updateUserRole` |
| POST | `/internal/admin/users/{userId}/points` | `InternalAdminUserController#manageUserPoints` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalAdminUserService#manageUserPoints` |
| GET | `/internal/admin/users/export` | `InternalAdminUserController#exportUserData` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalAdminUserService#exportUsersCsv` |
| POST | `/internal/admin/auth/login` | `InternalAdminAuthController#adminLogin` | `this#getClientIp`, `AuthService#login`, `JwtTokenProvider#getEmailFromToken`, `InternalUserQueryService#checkAdminRole`, `this#setCookie` |
| POST | `/internal/admin/auth/find-email` | `InternalAdminAuthController#findAdminEmail` | `InternalUserQueryService#findEmailByPhoneNumber`, `InternalUserQueryService#checkAdminRole` |
| GET | `/internal/admin/auth/check` | `InternalAdminAuthController#checkAdminToken` | `this#getCookieValue`, `JwtTokenProvider#validateToken`, `JwtTokenProvider#getEmailFromToken`, `InternalUserQueryService#checkAdminRole` |
| POST | `/internal/admin/auth/refresh` | `InternalAdminAuthController#refreshAdminToken` | `this#getCookieValue`, `JwtTokenProvider#validateToken`, `AuthRedisService#isRefreshTokenValid`, `JwtTokenProvider#getEmailFromToken`, `InternalUserQueryService#findByEmail`, `InternalUserQueryService#checkAdminRole`, `this#getClientIp`, `JwtTokenProvider#generateTokenPair`, `this#setCookie` |
| POST | `/internal/admin/auth/find-password` | `InternalAdminAuthController#findAdminPassword` | `InternalUserQueryService#findByEmail`, `InternalPasswordResetService#AdminCheckPasswordResetAndSendEmail` |
| POST | `/internal/admin/auth/logout` | `InternalAdminAuthController#logout` | `this#getCookieValue`, `JwtTokenProvider#validateToken`, `JwtTokenProvider#getEmailFromToken`, `InternalUserQueryService#checkAdminRole`, `AuthRedisService#removeAccessToken`, `AuthRedisService#removeRefreshToken`, `this#expireCookie` |
| POST | `/internal/auth/login` | `InternalAuthController#login` |  |
| POST | `/internal/auth/logout` | `InternalAuthController#logout` |  |
| POST | `/internal/auth/refresh` | `InternalAuthController#refresh` |  |
| GET | `/internal/auth/email` | `InternalAuthController#getUserEmail` |  |
| GET | `/internal/auth/status` | `InternalAuthController#checkAuthStatus` |  |
| POST | `/internal/identity-verification/verify` | `InternalIdentityVerificationController#verifyIdentity` | `InternalIdentityVerificationService#verifyIdentity` |
| GET | `/oauth2/authorization/{registrationId}` | `InternalOAuth2AuthorizationController#redirectToOAuthProvider` |  |
| POST | `/internal/oauth/complete-signup` | `InternalOAuthController#completeOAuthSignup` | `this#extractEmailFromToken`, `UserRepository#findByEmail`, `UserRepository#findByReferralCode`, `UserRepository#save~`, `this#getClientIp`, `JwtTokenProvider#generateTokenPair`, `this#setCookie` |
| GET | `/internal/oauth/status` | `InternalOAuthController#checkAuthStatus` | `this#getCookieValue`, `JwtTokenProvider#validateToken`, `AuthRedisService#isAccessTokenValid`, `JwtTokenProvider#getEmailFromToken` |
| GET | `/internal/oauth/connections` | `InternalOAuthController#getOAuthConnections` | `this#getCookieValue`, `JwtTokenProvider#getEmailFromToken`, `UserRepository#findByEmail` |
| POST | `/internal/users/register` | `InternalUserCommandController#registerUser` | `InternalUserCommandService#registerUser` |
| POST | `/internal/users/verify-identity` | `InternalUserCommandController#verifyIdentity` | `InternalIdentityVerificationService#verifyIdentity` |
| PUT | `/internal/users/update` | `InternalUserCommandController#updateUserInfo` | `InternalUserUpdateService#updateLoginInfo`, `InternalUserQueryService#getLoginInfo`, `this#getCookieValue`, `AuthRedisService#removeAccessToken`, `AuthRedisService#removeRefreshToken`, `InternalUserUpdateService#reissueTokenAfterEmailChange`, `this#setCookie` |
| POST | `/internal/points/commands` | `InternalPointCommandController#addPoint` | `InternalPointCommandService#addPoint` |
| POST | `/internal/points/commands/use` | `InternalPointCommandController#usePoint` | `InternalPointCommandService#usePoint` |
| GET | `/internal/points/queries` | `InternalPointQueryController#getAllPointHistory` | `InternalPointQueryService#getAllPointHistory` |
| GET | `/internal/points/queries/available` | `InternalPointQueryController#getAvailablePoints` | `InternalPointQueryService#getAvailablePoints` |
| GET | `/internal/points/queries/summary` | `InternalPointQueryController#getPointSummary` | `InternalPointQueryService#getPointSummary` |
| GET | `/internal/points/queries/{pointId}` | `InternalPointQueryController#getPointDetail` | `InternalPointQueryService#getPointDetail` |
| POST | `/internal/users/find-email` | `InternalUserQueryController#findEmail` | `InternalUserQueryService#findEmailByPhoneNumber` |
| POST | `/internal/users/reset-password` | `InternalUserQueryController#resetPasswordEligibility` | `InternalPasswordResetService#checkPasswordResetEligibility` |
| POST | `/internal/users/reset-password-sandEmail` | `InternalUserQueryController#resetPasswordToEmail` | `InternalPasswordResetService#checkPasswordResetAndSendEmail` |
| POST | `/internal/users/reset` | `InternalUserQueryController#resetPassword` | `InternalPasswordResetService#resetPassword` |
| GET | `/internal/users/login-info` | `InternalUserQueryController#getLoginInfo` | `InternalUserQueryService#getLoginInfo` |
| DELETE | `/internal/users/delete-account` | `InternalUserQueryController#deleteAccount` | `InternalUserCommandService#deleteAccount` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `InternalAdminGradeService`
  - `getAllGrades` → InternalUserGradeRepository#findAllByOrderByLevelAsc
  - `getGradeById` → InternalUserGradeRepository#findById~, this#convertToGradeResponseDto
  - `createGrade` → InternalUserGradeRepository#findByLevel, InternalUserGradeRepository#findByName, InternalUserGradeRepository#save~, this#convertToGradeResponseDto
  - `updateGrade` → InternalUserGradeRepository#findById~, InternalUserGradeRepository#findByName, this#convertToGradeResponseDto
  - `deleteGrade` → InternalUserGradeRepository#findById~, InternalUserGradeRepository#countUsersByGradeId, InternalUserGradeRepository#delete~
  - `getGradeUserCounts` → InternalUserGradeRepository#countUsersGroupByGrade, InternalUserGradeRepository#findById~
  - `getGradeStatistics` → InternalUserGradeRepository#findAllByOrderByLevelAsc, InternalUserGradeRepository#countUsersByGradeId
  - `runGradeAutoAssignment` → UserRepository#findAll~, InternalUserGradeRepository#findGradeByPurchaseAmount
  - `convertToGradeResponseDto` → InternalUserGradeRepository#countUsersByGradeId
- `InternalAdminPointService`
  - `getUserPointHistory` → this#getUserById, InternalPointRepository#findByUserOrderByCreatedDateDesc
  - `getUserAvailablePoints` → this#getUserById, InternalPointRepository#findByUserAndStatusOrderByExpirationDateAsc
  - `getUserPointSummary` → this#getUserById, InternalPointRepository#findByUserAndStatusOrderByExpirationDateAsc
  - `getPointDetail` → InternalPointRepository#findById~
  - `addPointByAdmin` → this#getUserById, InternalPointRepository#save~
  - `deductPointByAdmin` → this#getUserById, InternalPointRepository#findByUserAndStatusOrderByExpirationDateAsc, InternalPointRepository#save~
  - `getUserById` → UserRepository#findById~
- `InternalAdminSanctionService`
  - `searchSanctions` → InternalUserSanctionRepository#findAll~
  - `getSanctionById` → InternalUserSanctionRepository#findById~, this#convertToSanctionResponseDto
  - `createSanction` → UserRepository#findById~, InternalUserSanctionRepository#save~, this#convertToSanctionResponseDto
  - `updateSanction` → InternalUserSanctionRepository#findById~, this#convertToSanctionResponseDto
  - `reviewSanction` → InternalUserSanctionRepository#findById~, this#convertToSanctionResponseDto
  - `cancelSanction` → InternalUserSanctionRepository#findById~, this#hasOtherActiveBanSanctions, this#convertToSanctionResponseDto
  - `getUserSanctions` → UserRepository#findById~, InternalUserSanctionRepository#findByUserOrderByCreatedDateDesc
  - `getSanctionStatistics` → InternalUserSanctionRepository#countTotalSanctions, InternalUserSanctionRepository#countActiveSanctions, InternalUserSanctionRepository#countExpiredSanctions, InternalUserSanctionRepository#countPendingSanctions
  - `processExpiredSanctions` → InternalUserSanctionRepository#findExpirableSanctions, this#hasOtherActiveBanSanctions
  - `hasOtherActiveBanSanctions` → InternalUserSanctionRepository#findByUserAndStatusOrderByStartDateDesc
- `InternalAdminUserService`
  - `searchUsers` → UserRepository#findAll~
  - `getUserDetail` → UserRepository#findById~, this#convertToUserDetailResponseDto
  - `updateUserStatus` → UserRepository#findById~, InternalUserSanctionRepository#save~, this#convertToUserDetailResponseDto
  - `updateUserGrade` → UserRepository#findById~, InternalUserGradeRepository#findById~, this#convertToUserDetailResponseDto
  - `updateUserRole` → UserRepository#findById~, this#convertToUserDetailResponseDto
  - `manageUserPoints` → UserRepository#findById~, InternalPointRepository#save~, InternalPointRepository#findByUserAndStatusOrderByExpirationDateAsc
  - `exportUsersCsv` → UserRepository#findAll~
  - `convertToUserSearchResponseDto` → ProfileRepository#findByUser
  - `convertToUserDetailResponseDto` → ProfileRepository#findByUser, BankAccountRepository#findByUser_Email, InternalUserSanctionRepository#findByUserAndStatusOrderByStartDateDesc, this#convertToProfileDto, this#convertToBankAccountDto, this#convertToSanctionBriefDtoList
- `InternalBankAccountCommandService`
  - `createOrUpdateBankAccount` → UserRepository#findByEmail
  - `deleteBankAccount` → UserRepository#findByEmail
- `InternalBankAccountQueryService`
  - `getBankAccount` → BankAccountRepository#findByUser_Email
- `InternalBlockedProfileCommandService`
  - `blockProfile` → ProfileRepository#findByUserEmailWithFetchJoin, ProfileRepository#findById~, InternalBlockedProfileRepository#findByProfileAndBlockedProfile, InternalBlockedProfileRepository#save~
  - `unblockProfile` → ProfileRepository#findByUserEmailWithFetchJoin, ProfileRepository#findById~, InternalBlockedProfileRepository#findByProfileAndBlockedProfile, InternalBlockedProfileRepository#delete~
- `InternalBlockedProfileQueryService`
  - `getBlockedProfiles` → InternalBlockedProfileRepository#findAllByProfileWithBlocked, InternalBlockedProfileRepository#findAllByProfileEmailWithBlocked
- `InternalCustomUserDetailsService`
  - `loadUserByUsername` → UserRepository#findByEmail
- `InternalFollowCommandService`
  - `createFollow` → ProfileRepository#findByUserEmailWithFetchJoin, ProfileRepository#findById~, InternalFollowRepository#findByFollowerAndFollowing, InternalFollowRepository#save~, InternalNotificationCommandService#createNotification
  - `deleteFollow` → ProfileRepository#findByUserEmailWithFetchJoin, ProfileRepository#findById~, InternalFollowRepository#findByFollowerAndFollowing, InternalFollowRepository#delete~
- `InternalFollowQueryService`
  - `getFollowers` → ProfileRepository#findByUserEmailWithFetchJoin, InternalFollowRepository#findFollowersByProfileId
  - `getFollowings` → ProfileRepository#findByUserEmailWithFetchJoin, InternalFollowRepository#findFollowingsByProfileId
- `InternalOAuthAttributes`
  - `of` → this#ofGoogle, this#ofNaver, this#ofKakao
- `InternalPasswordResetService`
  - `checkPasswordResetEligibility` → UserRepository#findByEmailAndPhoneNumber
  - `resetPassword` → UserRepository#findByEmailAndPhoneNumber
  - `checkPasswordResetAndSendEmail` → UserRepository#findByEmailAndPhoneNumber, InternalEmailService#sendEmail
  - `AdminCheckPasswordResetAndSendEmail` → UserRepository#findByEmailAndPhoneNumber, InternalEmailService#sendEmail
- `InternalPasswordUtils`
  - `generateRandomPassword` → this#isValidPassword
- `InternalPointCommandService`
  - `addPoint` → this#validateAddPointRequest, InternalUserQueryService#findByEmail, InternalPointRepository#save~
  - `usePoint` → this#validateUsePointRequest, InternalUserQueryService#findByEmail, InternalPointRepository#getTotalAvailablePoints, InternalPointRepository#findByUserAndStatusOrderByExpirationDateAsc, InternalPointRepository#save~
  - `expirePoints` → InternalPointRepository#findByStatusAndExpirationDateBefore
- `InternalPointExpirationScheduler`
  - `checkExpiredPoints` → InternalPointCommandService#expirePoints
- `InternalPointQueryService`
  - `getAllPointHistory` → InternalUserQueryService#findByEmail, InternalPointRepository#findByUserOrderByCreatedDateDesc
  - `getAvailablePoints` → InternalUserQueryService#findByEmail, InternalPointRepository#findByUserAndStatusOrderByExpirationDateAsc
  - `getPointSummary` → InternalUserQueryService#findByEmail, InternalPointRepository#getTotalAvailablePoints, this#getAllPointHistory, InternalPointRepository#findExpiringPoints
  - `getPointDetail` → InternalUserQueryService#findByEmail, InternalPointRepository#findById~
- `InternalProfileQueryService`
  - `getProfileInfo` → ProfileRepository#findByUserEmailWithFetchJoin, InternalBlockedProfileQueryService#getBlockedProfiles
  - `getProfileByEmail` → ProfileRepository#findByUserEmailWithFetchJoin
  - `getProfileImageFileName` → ProfileRepository#findById~
- `InternalProfileService`
  - `createDefaultProfile` → this#generateRandomProfileName
- `InternalUserCommandService`
  - `registerUser` → UserRepository#existsByEmail, InternalIdentityVerificationService#verifyIdentity, UserRepository#findByCi, this#validateAge, UserRepository#findByReferralCode, this#convertToGender, this#calculateAge, this#generateUniqueReferralCode, UserRepository#save~, ProfileCommandService#createDefaultProfile
  - `deleteAccount` → UserRepository#findByEmail, InternalNotificationCommandService#deleteNotificationsByUser, FileUtils#deleteFile, UserRepository#delete~
  - `generateUniqueReferralCode` → UserRepository#findByReferralCode
- `InternalUserControllerValidator`
  - `validateUserRegistrationDto` → this#isValidPassword
  - `validatePasswordResetDto` → this#isValidPassword
  - `validateLoginInfoUpdateDto` → this#isValidPassword
- `InternalUserQueryService`
  - `checkAdminRole` → UserRepository#findByEmail
  - `findEmailByPhoneNumber` → UserRepository#findByPhoneNumber
  - `getLoginInfo` → UserRepository#findByEmail
  - `findByEmail` → UserRepository#findByEmail
- `InternalUserUpdateService`
  - `updateLoginInfo` → UserRepository#findByEmail, UserRepository#existsByEmail
  - `reissueTokenAfterEmailChange` → UserRepository#findByEmail, AuthRedisService#removeAccessToken, AuthRedisService#removeRefreshToken, JwtTokenProvider#generateTokenPair

## 3. 클래스 목록 (역할별)

### controller (17)

- `InternalBankAccountController` controller/InternalBankAccountController.java · public: createOrUpdateBankAccount, getBankAccount, deleteBankAccount · 주입: InternalBankAccountCommandService, InternalBankAccountQueryService
- `InternalBlockedProfileController` controller/InternalBlockedProfileController.java · public: blockProfile, unblockProfile, getBlockedProfiles · 주입: InternalBlockedProfileCommandService, InternalBlockedProfileQueryService
- `InternalFollowController` controller/InternalFollowController.java · public: createFollow, deleteFollow, getFollowers, getFollowings · 주입: InternalFollowCommandService, InternalFollowQueryService
- `InternalProfileController` controller/InternalProfileController.java · public: getProfile, updateProfile, getProfileImage · 주입: InternalProfileQueryService, ProfileCommandService, FileUtils
- `InternalAdminGradeController` controller/admin/InternalAdminGradeController.java · public: getAllGrades, getGradeById, createGrade, updateGrade, deleteGrade, getGradeUserCounts, getGradeStatistics, runGradeAutoAssignment · 주입: InternalAdminGradeService, InternalUserQueryService
- `InternalAdminPointController` controller/admin/InternalAdminPointController.java · public: getUserPointHistory, getUserAvailablePoints, getUserPointSummary, getPointDetail, addPointByAdmin, deductPointByAdmin, getPointStatistics · 주입: InternalAdminPointService, InternalUserQueryService
- `InternalAdminSanctionController` controller/admin/InternalAdminSanctionController.java · public: searchSanctions, getSanctionById, createSanction, updateSanction, reviewSanction, cancelSanction, getUserSanctions, getSanctionStatistics · 주입: InternalAdminSanctionService, InternalUserQueryService
- `InternalAdminUserController` controller/admin/InternalAdminUserController.java · public: searchUsers, getUserDetail, updateUserStatus, updateUserGrade, updateUserRole, manageUserPoints, exportUserData · 주입: InternalAdminUserService, InternalUserQueryService
- `InternalAdminAuthController` controller/command/InternalAdminAuthController.java · public: adminLogin, findAdminEmail, checkAdminToken, refreshAdminToken, findAdminPassword, logout · 주입: AuthService, InternalUserQueryService, JwtTokenProvider, InternalPasswordResetService, AuthRedisService
- `InternalAuthController` controller/command/InternalAuthController.java · public: login, logout, refresh, getUserEmail, checkAuthStatus
- `InternalIdentityVerificationController` controller/command/InternalIdentityVerificationController.java · public: verifyIdentity · 주입: InternalIdentityVerificationService
- `InternalOAuth2AuthorizationController` controller/command/InternalOAuth2AuthorizationController.java · public: redirectToOAuthProvider
- `InternalOAuthController` controller/command/InternalOAuthController.java · public: completeOAuthSignup, checkAuthStatus, getOAuthConnections · 주입: UserRepository, InternalUserUpdateService, JwtTokenProvider, AuthRedisService
- `InternalUserCommandController` controller/command/InternalUserCommandController.java · public: registerUser, verifyIdentity, updateUserInfo · 주입: InternalUserCommandService, InternalUserUpdateService, InternalUserQueryService, AuthRedisService, InternalIdentityVerificationService
- `InternalPointCommandController` controller/point/InternalPointCommandController.java · public: addPoint, usePoint · 주입: InternalPointCommandService
- `InternalPointQueryController` controller/point/InternalPointQueryController.java · public: getAllPointHistory, getAvailablePoints, getPointSummary, getPointDetail · 주입: InternalPointQueryService
- `InternalUserQueryController` controller/query/InternalUserQueryController.java · public: findEmail, resetPasswordEligibility, resetPasswordToEmail, resetPassword, getLoginInfo, deleteAccount · 주입: AuthService, InternalUserQueryService, InternalPasswordResetService, InternalUserUpdateService, JwtTokenProvider, InternalUserCommandService, AuthRedisService

### service-command (7)

- `InternalEmailService` service/command/InternalEmailService.java · public: sendEmail · 주입: JavaMailSender
- `InternalIdentityVerificationService` service/command/InternalIdentityVerificationService.java · public: verifyIdentity · 주입: RestTemplate, ObjectMapper
- `InternalPasswordResetService` service/command/InternalPasswordResetService.java · public: checkPasswordResetEligibility, resetPassword, checkPasswordResetAndSendEmail, AdminCheckPasswordResetAndSendEmail · 주입: UserRepository, PasswordEncoder, InternalEmailService
- `InternalProfileService` service/command/InternalProfileService.java · public: createDefaultProfile
- `InternalUserCommandService` service/command/InternalUserCommandService.java · public: registerUser, deleteAccount · 주입: UserRepository, PasswordEncoder, ProfileCommandService, FileUtils, InternalNotificationCommandService, InternalIdentityVerificationService
- `InternalUserSanctionService` service/command/InternalUserSanctionService.java
- `InternalUserUpdateService` service/command/InternalUserUpdateService.java · public: updateLoginInfo, reissueTokenAfterEmailChange · 주입: UserRepository, PasswordEncoder, AuthRedisService, JwtTokenProvider

### service-query (1)

- `InternalUserQueryService` service/query/InternalUserQueryService.java · public: checkAdminRole, findEmailByPhoneNumber, getLoginInfo, findByEmail · 주입: UserRepository

### service (14)

- `InternalCustomUserDetailsService` security/InternalCustomUserDetailsService.java · extends/implements UserDetailsService · public: loadUserByUsername · 주입: UserRepository, PasswordEncoder
- `InternalBlockedProfileCommandService` service/BlockProfile/InternalBlockedProfileCommandService.java · public: blockProfile, unblockProfile · 주입: ProfileRepository, InternalBlockedProfileRepository
- `InternalBlockedProfileQueryService` service/BlockProfile/InternalBlockedProfileQueryService.java · public: getBlockedProfiles · 주입: InternalBlockedProfileRepository
- `InternalAdminGradeService` service/admin/InternalAdminGradeService.java · public: getAllGrades, getGradeById, createGrade, updateGrade, deleteGrade, getGradeUserCounts, getGradeStatistics, runGradeAutoAssignment · 주입: InternalUserGradeRepository, UserRepository
- `InternalAdminPointService` service/admin/InternalAdminPointService.java · public: getUserPointHistory, getUserAvailablePoints, getUserPointSummary, getPointDetail, addPointByAdmin, deductPointByAdmin, getPointStatistics · 주입: UserRepository, InternalPointRepository
- `InternalAdminSanctionService` service/admin/InternalAdminSanctionService.java · public: searchSanctions, getSanctionById, createSanction, updateSanction, reviewSanction, cancelSanction, getUserSanctions, getSanctionStatistics, processExpiredSanctions · 주입: InternalUserSanctionRepository, UserRepository
- `InternalAdminUserService` service/admin/InternalAdminUserService.java · public: searchUsers, getUserDetail, updateUserStatus, updateUserGrade, updateUserRole, manageUserPoints, exportUsersCsv · 주입: UserRepository, ProfileRepository, InternalPointRepository, InternalUserGradeRepository, InternalUserSanctionRepository, BankAccountRepository
- `InternalBankAccountCommandService` service/bankaccount/InternalBankAccountCommandService.java · public: createOrUpdateBankAccount, deleteBankAccount · 주입: UserRepository
- `InternalBankAccountQueryService` service/bankaccount/InternalBankAccountQueryService.java · public: getBankAccount · 주입: BankAccountRepository
- `InternalFollowCommandService` service/follow/InternalFollowCommandService.java · public: createFollow, deleteFollow · 주입: InternalFollowRepository, ProfileRepository, InternalNotificationCommandService
- `InternalFollowQueryService` service/follow/InternalFollowQueryService.java · public: getFollowers, getFollowings · 주입: InternalFollowRepository, ProfileRepository
- `InternalPointCommandService` service/point/InternalPointCommandService.java · public: addPoint, usePoint, expirePoints · 주입: InternalPointRepository, InternalUserQueryService
- `InternalPointQueryService` service/point/InternalPointQueryService.java · public: getAllPointHistory, getAvailablePoints, getPointSummary, getPointDetail · 주입: InternalPointRepository, InternalUserQueryService
- `InternalProfileQueryService` service/profile/InternalProfileQueryService.java · public: getProfileInfo, getProfileByEmail, getProfileImageFileName · 주입: ProfileRepository, InternalBlockedProfileQueryService

### repository (5)

- `InternalBlockedProfileRepository` (interface) repository/InternalBlockedProfileRepository.java · extends/implements JpaRepository · public: findByProfile, findByProfileAndBlockedProfileId, findBlockedProfileIdsByProfile, findAllByProfileWithBlocked, findByProfileAndBlockedProfile, findAllByProfileEmailWithBlocked
- `InternalFollowRepository` (interface) repository/InternalFollowRepository.java · extends/implements JpaRepository · public: findByFollowerAndFollowing, findFollowersByProfileId, findFollowingsByProfileId
- `InternalPointRepository` (interface) repository/InternalPointRepository.java · extends/implements JpaRepository · public: findByUserOrderByCreatedDateDesc, findByUserAndStatusOrderByExpirationDateAsc, findByStatusAndExpirationDateBefore, getTotalAvailablePoints, findExpiringPoints
- `InternalUserGradeRepository` (interface) repository/InternalUserGradeRepository.java · extends/implements JpaRepository · public: findByLevel, findByName, findAllByOrderByLevelAsc, findGradeByPurchaseAmount, countUsersByGradeId, countUsersGroupByGrade
- `InternalUserSanctionRepository` (interface) repository/InternalUserSanctionRepository.java · extends/implements JpaRepository, JpaSpecificationExecutor · public: findByUserOrderByCreatedDateDesc, findByUserAndStatusOrderByStartDateDesc, findByStatus, findByType, findExpirableSanctions, findByCreatedDateBetween, findByUserEmail, countActiveSanctions, countExpiredSanctions, countPendingSanctions, countTotalSanctions

### other (9)

- `InternalUserSanctionSpecifications` repository/InternalUserSanctionSpecifications.java · public: withSearchCriteria, hasUserId, hasStatus, hasType, createdBetween, startDateBetween, endDateBetween, userEmailLike
- `InternalPointExpirationScheduler` scheduler/InternalPointExpirationScheduler.java · public: checkExpiredPoints · 주입: InternalPointCommandService
- `InternalOAuthAttributes` security/oauth2/InternalOAuthAttributes.java · public: of
- `InternalPasswordUtils` utils/InternalPasswordUtils.java · public: generateRandomPassword
- `InternalBankAccountControllerValidator` validate/InternalBankAccountControllerValidator.java · public: validateBankAccountDto
- `InternalBlockedProfileControllerValidator` validate/InternalBlockedProfileControllerValidator.java
- `InternalFollowControllerValidator` validate/InternalFollowControllerValidator.java
- `InternalProfileControllerValidator` validate/InternalProfileControllerValidator.java
- `InternalUserControllerValidator` validate/InternalUserControllerValidator.java · public: validateUserRegistrationDto, validateLoginRequestDto, validateEmailFindRequestDto, validatePasswordResetEligibilityDto, validatePasswordResetDto, validateLoginInfoUpdateDto

### 다른 도메인 참조 (import·주입 기준)

- **internalnotification**: `InternalNotificationCommandService` ← InternalFollowCommandService, InternalUserCommandService
- **notification**: `NotificationCategory` ← InternalFollowCommandService; `NotificationType` ← InternalFollowCommandService
- **user**: `AuthRedisService` ← InternalAdminAuthController, InternalOAuthController, InternalUserCommandController, InternalUserQueryController, InternalUserUpdateService; `AuthService` ← InternalAdminAuthController, InternalUserQueryController; `BankAccount` ← InternalBankAccountCommandService, InternalBankAccountQueryService; `BankAccountRepository` ← InternalAdminUserService, InternalBankAccountQueryService; `BlockedProfile` ← InternalBlockedProfileCommandService, InternalBlockedProfileQueryService, InternalBlockedProfileRepository; `Follow` ← InternalFollowCommandService, InternalFollowQueryService, InternalFollowRepository; `Gender` ← InternalAdminAuthController, InternalUserCommandService; `Point` ← InternalAdminPointService, InternalPointCommandService, InternalPointQueryService, InternalPointRepository; `PointStatus` ← InternalAdminPointService, InternalPointCommandService, InternalPointQueryService, InternalPointRepository; `Profile` ← InternalBlockedProfileCommandService, InternalBlockedProfileQueryService, InternalBlockedProfileRepository, InternalFollowCommandService, InternalFollowQueryService, InternalFollowRepository, InternalProfileQueryService, InternalProfileService; `ProfileCommandService` ← InternalProfileController, InternalUserCommandService; `ProfileRepository` ← InternalAdminUserService, InternalBlockedProfileCommandService, InternalFollowCommandService, InternalFollowQueryService, InternalProfileQueryService; `Role` ← InternalAdminAuthController, InternalUserQueryService; `SanctionStatus` ← InternalAdminSanctionController, InternalUserSanctionRepository, InternalUserSanctionSpecifications; `SanctionType` ← InternalAdminSanctionController, InternalUserSanctionRepository, InternalUserSanctionSpecifications; `ShoeSize` ← InternalUserUpdateService; `User` ← InternalAdminAuthController, InternalAdminGradeService, InternalAdminPointService, InternalBankAccountCommandService, InternalCustomUserDetailsService, InternalOAuthController, InternalPasswordResetService, InternalPointCommandService, InternalPointQueryService, InternalPointRepository, InternalProfileService, InternalUserCommandController, InternalUserCommandService, InternalUserQueryController, InternalUserQueryService, InternalUserSanctionRepository, InternalUserSanctionSpecifications, InternalUserUpdateService; `UserGrade` ← InternalAdminGradeService, InternalUserGradeRepository; `UserRepository` ← InternalAdminGradeService, InternalAdminPointService, InternalAdminSanctionService, InternalAdminUserService, InternalBankAccountCommandService, InternalCustomUserDetailsService, InternalOAuthController, InternalPasswordResetService, InternalUserCommandService, InternalUserQueryService, InternalUserUpdateService; `UserSanction` ← InternalUserSanctionRepository, InternalUserSanctionSpecifications

### 이 도메인을 참조하는 다른 도메인

- **internalchatquestion**: InternalUserQueryService
- **internalevent**: InternalUserQueryService
- **internalfaq**: InternalUserQueryService
- **internalinquiry**: InternalUserQueryService
- **internalinspection**: InternalUserQueryService
- **internalnotice**: InternalUserQueryService
- **internalorder**: InternalUserQueryService
- **internalpayment**: InternalUserQueryService
- **internalproduct**: InternalUserQueryService
- **internalsale**: InternalBankAccountQueryService, InternalUserQueryService
- **internalstyle**: InternalProfileQueryService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

