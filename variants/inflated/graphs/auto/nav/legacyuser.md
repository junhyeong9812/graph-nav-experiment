# legacyuser 도메인

기준 경로: `src/main/java/com/fream/back/domain/legacyuser/` · 파일 53개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/legacy/bank-account` | `LegacyBankAccountController#createOrUpdateBankAccount` | `this#extractEmailFromSecurityContext`, `LegacyBankAccountCommandService#createOrUpdateBankAccount` |
| GET | `/legacy/bank-account` | `LegacyBankAccountController#getBankAccount` | `this#extractEmailFromSecurityContext`, `LegacyBankAccountQueryService#getBankAccount` |
| DELETE | `/legacy/bank-account` | `LegacyBankAccountController#deleteBankAccount` | `this#extractEmailFromSecurityContext`, `LegacyBankAccountCommandService#deleteBankAccount` |
| POST | `/legacy/profiles/blocked` | `LegacyBlockedProfileController#blockProfile` | `this#extractEmailFromSecurityContext`, `LegacyBlockedProfileCommandService#blockProfile` |
| DELETE | `/legacy/profiles/blocked` | `LegacyBlockedProfileController#unblockProfile` | `this#extractEmailFromSecurityContext`, `LegacyBlockedProfileCommandService#unblockProfile` |
| GET | `/legacy/profiles/blocked` | `LegacyBlockedProfileController#getBlockedProfiles` | `this#extractEmailFromSecurityContext`, `LegacyBlockedProfileQueryService#getBlockedProfiles` |
| POST | `/legacy/follows/{profileId}` | `LegacyFollowController#createFollow` | `this#extractEmailFromSecurityContext`, `LegacyFollowCommandService#createFollow` |
| DELETE | `/legacy/follows/{profileId}` | `LegacyFollowController#deleteFollow` | `this#extractEmailFromSecurityContext`, `LegacyFollowCommandService#deleteFollow` |
| GET | `/legacy/follows/followers` | `LegacyFollowController#getFollowers` | `this#extractEmailFromSecurityContext`, `LegacyFollowQueryService#getFollowers` |
| GET | `/legacy/follows/followings` | `LegacyFollowController#getFollowings` | `this#extractEmailFromSecurityContext`, `LegacyFollowQueryService#getFollowings` |
| GET | `/legacy/profiles` | `LegacyProfileController#getProfile` | `this#extractEmailFromSecurityContext`, `LegacyProfileQueryService#getProfileInfo` |
| PUT | `/legacy/profiles` | `LegacyProfileController#updateProfile` | `this#extractEmailFromSecurityContext`, `ProfileCommandService#updateProfile` |
| GET | `/legacy/profiles/{profileId}/image` | `LegacyProfileController#getProfileImage` | `LegacyProfileQueryService#getProfileImageFileName` |
| GET | `/legacy/admin/users/grades` | `LegacyAdminGradeController#getAllGrades` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyAdminGradeService#getAllGrades` |
| GET | `/legacy/admin/users/grades/{gradeId}` | `LegacyAdminGradeController#getGradeById` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyAdminGradeService#getGradeById` |
| POST | `/legacy/admin/users/grades` | `LegacyAdminGradeController#createGrade` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyAdminGradeService#createGrade` |
| PUT | `/legacy/admin/users/grades/{gradeId}` | `LegacyAdminGradeController#updateGrade` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyAdminGradeService#updateGrade` |
| DELETE | `/legacy/admin/users/grades/{gradeId}` | `LegacyAdminGradeController#deleteGrade` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyAdminGradeService#deleteGrade` |
| GET | `/legacy/admin/users/grades/counts` | `LegacyAdminGradeController#getGradeUserCounts` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyAdminGradeService#getGradeUserCounts` |
| GET | `/legacy/admin/users/grades/statistics` | `LegacyAdminGradeController#getGradeStatistics` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyAdminGradeService#getGradeStatistics` |
| POST | `/legacy/admin/users/grades/auto-assign` | `LegacyAdminGradeController#runGradeAutoAssignment` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyAdminGradeService#runGradeAutoAssignment` |
| GET | `/legacy/admin/users/points/user/{userId}` | `LegacyAdminPointController#getUserPointHistory` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyAdminPointService#getUserPointHistory` |
| GET | `/legacy/admin/users/points/user/{userId}/available` | `LegacyAdminPointController#getUserAvailablePoints` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyAdminPointService#getUserAvailablePoints` |
| GET | `/legacy/admin/users/points/user/{userId}/summary` | `LegacyAdminPointController#getUserPointSummary` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyAdminPointService#getUserPointSummary` |
| GET | `/legacy/admin/users/points/{pointId}` | `LegacyAdminPointController#getPointDetail` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyAdminPointService#getPointDetail` |
| POST | `/legacy/admin/users/points/user/{userId}/add` | `LegacyAdminPointController#addPointByAdmin` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyAdminPointService#addPointByAdmin` |
| POST | `/legacy/admin/users/points/user/{userId}/deduct` | `LegacyAdminPointController#deductPointByAdmin` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyAdminPointService#deductPointByAdmin` |
| GET | `/legacy/admin/users/points/statistics` | `LegacyAdminPointController#getPointStatistics` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyAdminPointService#getPointStatistics` |
| GET | `/legacy/admin/users/sanctions/search` | `LegacyAdminSanctionController#searchSanctions` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyAdminSanctionService#searchSanctions` |
| GET | `/legacy/admin/users/sanctions/{sanctionId}` | `LegacyAdminSanctionController#getSanctionById` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyAdminSanctionService#getSanctionById` |
| POST | `/legacy/admin/users/sanctions` | `LegacyAdminSanctionController#createSanction` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyAdminSanctionService#createSanction` |
| PUT | `/legacy/admin/users/sanctions/{sanctionId}` | `LegacyAdminSanctionController#updateSanction` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyAdminSanctionService#updateSanction` |
| PATCH | `/legacy/admin/users/sanctions/{sanctionId}/review` | `LegacyAdminSanctionController#reviewSanction` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyAdminSanctionService#reviewSanction` |
| PATCH | `/legacy/admin/users/sanctions/{sanctionId}/cancel` | `LegacyAdminSanctionController#cancelSanction` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyAdminSanctionService#cancelSanction` |
| GET | `/legacy/admin/users/sanctions/user/{userId}` | `LegacyAdminSanctionController#getUserSanctions` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyAdminSanctionService#getUserSanctions` |
| GET | `/legacy/admin/users/sanctions/statistics` | `LegacyAdminSanctionController#getSanctionStatistics` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyAdminSanctionService#getSanctionStatistics` |
| GET | `/legacy/admin/users/search` | `LegacyAdminUserController#searchUsers` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyAdminUserService#searchUsers` |
| GET | `/legacy/admin/users/{userId}` | `LegacyAdminUserController#getUserDetail` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyAdminUserService#getUserDetail` |
| PATCH | `/legacy/admin/users/{userId}/status` | `LegacyAdminUserController#updateUserStatus` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyAdminUserService#updateUserStatus` |
| PATCH | `/legacy/admin/users/{userId}/grade` | `LegacyAdminUserController#updateUserGrade` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyAdminUserService#updateUserGrade` |
| PATCH | `/legacy/admin/users/{userId}/role` | `LegacyAdminUserController#updateUserRole` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyAdminUserService#updateUserRole` |
| POST | `/legacy/admin/users/{userId}/points` | `LegacyAdminUserController#manageUserPoints` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyAdminUserService#manageUserPoints` |
| GET | `/legacy/admin/users/export` | `LegacyAdminUserController#exportUserData` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyAdminUserService#exportUsersCsv` |
| POST | `/legacy/admin/auth/login` | `LegacyAdminAuthController#adminLogin` | `this#getClientIp`, `AuthService#login`, `JwtTokenProvider#getEmailFromToken`, `LegacyUserQueryService#checkAdminRole`, `this#setCookie` |
| POST | `/legacy/admin/auth/find-email` | `LegacyAdminAuthController#findAdminEmail` | `LegacyUserQueryService#findEmailByPhoneNumber`, `LegacyUserQueryService#checkAdminRole` |
| GET | `/legacy/admin/auth/check` | `LegacyAdminAuthController#checkAdminToken` | `this#getCookieValue`, `JwtTokenProvider#validateToken`, `JwtTokenProvider#getEmailFromToken`, `LegacyUserQueryService#checkAdminRole` |
| POST | `/legacy/admin/auth/refresh` | `LegacyAdminAuthController#refreshAdminToken` | `this#getCookieValue`, `JwtTokenProvider#validateToken`, `AuthRedisService#isRefreshTokenValid`, `JwtTokenProvider#getEmailFromToken`, `LegacyUserQueryService#findByEmail`, `LegacyUserQueryService#checkAdminRole`, `this#getClientIp`, `JwtTokenProvider#generateTokenPair`, `this#setCookie` |
| POST | `/legacy/admin/auth/find-password` | `LegacyAdminAuthController#findAdminPassword` | `LegacyUserQueryService#findByEmail`, `LegacyPasswordResetService#AdminCheckPasswordResetAndSendEmail` |
| POST | `/legacy/admin/auth/logout` | `LegacyAdminAuthController#logout` | `this#getCookieValue`, `JwtTokenProvider#validateToken`, `JwtTokenProvider#getEmailFromToken`, `LegacyUserQueryService#checkAdminRole`, `AuthRedisService#removeAccessToken`, `AuthRedisService#removeRefreshToken`, `this#expireCookie` |
| POST | `/legacy/auth/login` | `LegacyAuthController#login` |  |
| POST | `/legacy/auth/logout` | `LegacyAuthController#logout` |  |
| POST | `/legacy/auth/refresh` | `LegacyAuthController#refresh` |  |
| GET | `/legacy/auth/email` | `LegacyAuthController#getUserEmail` |  |
| GET | `/legacy/auth/status` | `LegacyAuthController#checkAuthStatus` |  |
| POST | `/legacy/identity-verification/verify` | `LegacyIdentityVerificationController#verifyIdentity` | `LegacyIdentityVerificationService#verifyIdentity` |
| GET | `/oauth2/authorization/{registrationId}` | `LegacyOAuth2AuthorizationController#redirectToOAuthProvider` |  |
| POST | `/legacy/oauth/complete-signup` | `LegacyOAuthController#completeOAuthSignup` | `this#extractEmailFromToken`, `UserRepository#findByEmail`, `UserRepository#findByReferralCode`, `UserRepository#save~`, `this#getClientIp`, `JwtTokenProvider#generateTokenPair`, `this#setCookie` |
| GET | `/legacy/oauth/status` | `LegacyOAuthController#checkAuthStatus` | `this#getCookieValue`, `JwtTokenProvider#validateToken`, `AuthRedisService#isAccessTokenValid`, `JwtTokenProvider#getEmailFromToken` |
| GET | `/legacy/oauth/connections` | `LegacyOAuthController#getOAuthConnections` | `this#getCookieValue`, `JwtTokenProvider#getEmailFromToken`, `UserRepository#findByEmail` |
| POST | `/legacy/users/register` | `LegacyUserCommandController#registerUser` | `LegacyUserCommandService#registerUser` |
| POST | `/legacy/users/verify-identity` | `LegacyUserCommandController#verifyIdentity` | `LegacyIdentityVerificationService#verifyIdentity` |
| PUT | `/legacy/users/update` | `LegacyUserCommandController#updateUserInfo` | `LegacyUserUpdateService#updateLoginInfo`, `LegacyUserQueryService#getLoginInfo`, `this#getCookieValue`, `AuthRedisService#removeAccessToken`, `AuthRedisService#removeRefreshToken`, `LegacyUserUpdateService#reissueTokenAfterEmailChange`, `this#setCookie` |
| POST | `/legacy/points/commands` | `LegacyPointCommandController#addPoint` | `LegacyPointCommandService#addPoint` |
| POST | `/legacy/points/commands/use` | `LegacyPointCommandController#usePoint` | `LegacyPointCommandService#usePoint` |
| GET | `/legacy/points/queries` | `LegacyPointQueryController#getAllPointHistory` | `LegacyPointQueryService#getAllPointHistory` |
| GET | `/legacy/points/queries/available` | `LegacyPointQueryController#getAvailablePoints` | `LegacyPointQueryService#getAvailablePoints` |
| GET | `/legacy/points/queries/summary` | `LegacyPointQueryController#getPointSummary` | `LegacyPointQueryService#getPointSummary` |
| GET | `/legacy/points/queries/{pointId}` | `LegacyPointQueryController#getPointDetail` | `LegacyPointQueryService#getPointDetail` |
| POST | `/legacy/users/find-email` | `LegacyUserQueryController#findEmail` | `LegacyUserQueryService#findEmailByPhoneNumber` |
| POST | `/legacy/users/reset-password` | `LegacyUserQueryController#resetPasswordEligibility` | `LegacyPasswordResetService#checkPasswordResetEligibility` |
| POST | `/legacy/users/reset-password-sandEmail` | `LegacyUserQueryController#resetPasswordToEmail` | `LegacyPasswordResetService#checkPasswordResetAndSendEmail` |
| POST | `/legacy/users/reset` | `LegacyUserQueryController#resetPassword` | `LegacyPasswordResetService#resetPassword` |
| GET | `/legacy/users/login-info` | `LegacyUserQueryController#getLoginInfo` | `LegacyUserQueryService#getLoginInfo` |
| DELETE | `/legacy/users/delete-account` | `LegacyUserQueryController#deleteAccount` | `LegacyUserCommandService#deleteAccount` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `LegacyAdminGradeService`
  - `getAllGrades` → LegacyUserGradeRepository#findAllByOrderByLevelAsc
  - `getGradeById` → LegacyUserGradeRepository#findById~, this#convertToGradeResponseDto
  - `createGrade` → LegacyUserGradeRepository#findByLevel, LegacyUserGradeRepository#findByName, LegacyUserGradeRepository#save~, this#convertToGradeResponseDto
  - `updateGrade` → LegacyUserGradeRepository#findById~, LegacyUserGradeRepository#findByName, this#convertToGradeResponseDto
  - `deleteGrade` → LegacyUserGradeRepository#findById~, LegacyUserGradeRepository#countUsersByGradeId, LegacyUserGradeRepository#delete~
  - `getGradeUserCounts` → LegacyUserGradeRepository#countUsersGroupByGrade, LegacyUserGradeRepository#findById~
  - `getGradeStatistics` → LegacyUserGradeRepository#findAllByOrderByLevelAsc, LegacyUserGradeRepository#countUsersByGradeId
  - `runGradeAutoAssignment` → UserRepository#findAll~, LegacyUserGradeRepository#findGradeByPurchaseAmount
  - `convertToGradeResponseDto` → LegacyUserGradeRepository#countUsersByGradeId
- `LegacyAdminPointService`
  - `getUserPointHistory` → this#getUserById, LegacyPointRepository#findByUserOrderByCreatedDateDesc
  - `getUserAvailablePoints` → this#getUserById, LegacyPointRepository#findByUserAndStatusOrderByExpirationDateAsc
  - `getUserPointSummary` → this#getUserById, LegacyPointRepository#findByUserAndStatusOrderByExpirationDateAsc
  - `getPointDetail` → LegacyPointRepository#findById~
  - `addPointByAdmin` → this#getUserById, LegacyPointRepository#save~
  - `deductPointByAdmin` → this#getUserById, LegacyPointRepository#findByUserAndStatusOrderByExpirationDateAsc, LegacyPointRepository#save~
  - `getUserById` → UserRepository#findById~
- `LegacyAdminSanctionService`
  - `searchSanctions` → LegacyUserSanctionRepository#findAll~
  - `getSanctionById` → LegacyUserSanctionRepository#findById~, this#convertToSanctionResponseDto
  - `createSanction` → UserRepository#findById~, LegacyUserSanctionRepository#save~, this#convertToSanctionResponseDto
  - `updateSanction` → LegacyUserSanctionRepository#findById~, this#convertToSanctionResponseDto
  - `reviewSanction` → LegacyUserSanctionRepository#findById~, this#convertToSanctionResponseDto
  - `cancelSanction` → LegacyUserSanctionRepository#findById~, this#hasOtherActiveBanSanctions, this#convertToSanctionResponseDto
  - `getUserSanctions` → UserRepository#findById~, LegacyUserSanctionRepository#findByUserOrderByCreatedDateDesc
  - `getSanctionStatistics` → LegacyUserSanctionRepository#countTotalSanctions, LegacyUserSanctionRepository#countActiveSanctions, LegacyUserSanctionRepository#countExpiredSanctions, LegacyUserSanctionRepository#countPendingSanctions
  - `processExpiredSanctions` → LegacyUserSanctionRepository#findExpirableSanctions, this#hasOtherActiveBanSanctions
  - `hasOtherActiveBanSanctions` → LegacyUserSanctionRepository#findByUserAndStatusOrderByStartDateDesc
- `LegacyAdminUserService`
  - `searchUsers` → UserRepository#findAll~
  - `getUserDetail` → UserRepository#findById~, this#convertToUserDetailResponseDto
  - `updateUserStatus` → UserRepository#findById~, LegacyUserSanctionRepository#save~, this#convertToUserDetailResponseDto
  - `updateUserGrade` → UserRepository#findById~, LegacyUserGradeRepository#findById~, this#convertToUserDetailResponseDto
  - `updateUserRole` → UserRepository#findById~, this#convertToUserDetailResponseDto
  - `manageUserPoints` → UserRepository#findById~, LegacyPointRepository#save~, LegacyPointRepository#findByUserAndStatusOrderByExpirationDateAsc
  - `exportUsersCsv` → UserRepository#findAll~
  - `convertToUserSearchResponseDto` → ProfileRepository#findByUser
  - `convertToUserDetailResponseDto` → ProfileRepository#findByUser, BankAccountRepository#findByUser_Email, LegacyUserSanctionRepository#findByUserAndStatusOrderByStartDateDesc, this#convertToProfileDto, this#convertToBankAccountDto, this#convertToSanctionBriefDtoList
- `LegacyBankAccountCommandService`
  - `createOrUpdateBankAccount` → UserRepository#findByEmail
  - `deleteBankAccount` → UserRepository#findByEmail
- `LegacyBankAccountQueryService`
  - `getBankAccount` → BankAccountRepository#findByUser_Email
- `LegacyBlockedProfileCommandService`
  - `blockProfile` → ProfileRepository#findByUserEmailWithFetchJoin, ProfileRepository#findById~, LegacyBlockedProfileRepository#findByProfileAndBlockedProfile, LegacyBlockedProfileRepository#save~
  - `unblockProfile` → ProfileRepository#findByUserEmailWithFetchJoin, ProfileRepository#findById~, LegacyBlockedProfileRepository#findByProfileAndBlockedProfile, LegacyBlockedProfileRepository#delete~
- `LegacyBlockedProfileQueryService`
  - `getBlockedProfiles` → LegacyBlockedProfileRepository#findAllByProfileWithBlocked, LegacyBlockedProfileRepository#findAllByProfileEmailWithBlocked
- `LegacyCustomUserDetailsService`
  - `loadUserByUsername` → UserRepository#findByEmail
- `LegacyFollowCommandService`
  - `createFollow` → ProfileRepository#findByUserEmailWithFetchJoin, ProfileRepository#findById~, LegacyFollowRepository#findByFollowerAndFollowing, LegacyFollowRepository#save~, LegacyNotificationCommandService#createNotification
  - `deleteFollow` → ProfileRepository#findByUserEmailWithFetchJoin, ProfileRepository#findById~, LegacyFollowRepository#findByFollowerAndFollowing, LegacyFollowRepository#delete~
- `LegacyFollowQueryService`
  - `getFollowers` → ProfileRepository#findByUserEmailWithFetchJoin, LegacyFollowRepository#findFollowersByProfileId
  - `getFollowings` → ProfileRepository#findByUserEmailWithFetchJoin, LegacyFollowRepository#findFollowingsByProfileId
- `LegacyOAuthAttributes`
  - `of` → this#ofGoogle, this#ofNaver, this#ofKakao
- `LegacyPasswordResetService`
  - `checkPasswordResetEligibility` → UserRepository#findByEmailAndPhoneNumber
  - `resetPassword` → UserRepository#findByEmailAndPhoneNumber
  - `checkPasswordResetAndSendEmail` → UserRepository#findByEmailAndPhoneNumber, LegacyEmailService#sendEmail
  - `AdminCheckPasswordResetAndSendEmail` → UserRepository#findByEmailAndPhoneNumber, LegacyEmailService#sendEmail
- `LegacyPasswordUtils`
  - `generateRandomPassword` → this#isValidPassword
- `LegacyPointCommandService`
  - `addPoint` → this#validateAddPointRequest, LegacyUserQueryService#findByEmail, LegacyPointRepository#save~
  - `usePoint` → this#validateUsePointRequest, LegacyUserQueryService#findByEmail, LegacyPointRepository#getTotalAvailablePoints, LegacyPointRepository#findByUserAndStatusOrderByExpirationDateAsc, LegacyPointRepository#save~
  - `expirePoints` → LegacyPointRepository#findByStatusAndExpirationDateBefore
- `LegacyPointExpirationScheduler`
  - `checkExpiredPoints` → LegacyPointCommandService#expirePoints
- `LegacyPointQueryService`
  - `getAllPointHistory` → LegacyUserQueryService#findByEmail, LegacyPointRepository#findByUserOrderByCreatedDateDesc
  - `getAvailablePoints` → LegacyUserQueryService#findByEmail, LegacyPointRepository#findByUserAndStatusOrderByExpirationDateAsc
  - `getPointSummary` → LegacyUserQueryService#findByEmail, LegacyPointRepository#getTotalAvailablePoints, this#getAllPointHistory, LegacyPointRepository#findExpiringPoints
  - `getPointDetail` → LegacyUserQueryService#findByEmail, LegacyPointRepository#findById~
- `LegacyProfileQueryService`
  - `getProfileInfo` → ProfileRepository#findByUserEmailWithFetchJoin, LegacyBlockedProfileQueryService#getBlockedProfiles
  - `getProfileByEmail` → ProfileRepository#findByUserEmailWithFetchJoin
  - `getProfileImageFileName` → ProfileRepository#findById~
- `LegacyProfileService`
  - `createDefaultProfile` → this#generateRandomProfileName
- `LegacyUserCommandService`
  - `registerUser` → UserRepository#existsByEmail, LegacyIdentityVerificationService#verifyIdentity, UserRepository#findByCi, this#validateAge, UserRepository#findByReferralCode, this#convertToGender, this#calculateAge, this#generateUniqueReferralCode, UserRepository#save~, ProfileCommandService#createDefaultProfile
  - `deleteAccount` → UserRepository#findByEmail, LegacyNotificationCommandService#deleteNotificationsByUser, FileUtils#deleteFile, UserRepository#delete~
  - `generateUniqueReferralCode` → UserRepository#findByReferralCode
- `LegacyUserControllerValidator`
  - `validateUserRegistrationDto` → this#isValidPassword
  - `validatePasswordResetDto` → this#isValidPassword
  - `validateLoginInfoUpdateDto` → this#isValidPassword
- `LegacyUserQueryService`
  - `checkAdminRole` → UserRepository#findByEmail
  - `findEmailByPhoneNumber` → UserRepository#findByPhoneNumber
  - `getLoginInfo` → UserRepository#findByEmail
  - `findByEmail` → UserRepository#findByEmail
- `LegacyUserUpdateService`
  - `updateLoginInfo` → UserRepository#findByEmail, UserRepository#existsByEmail
  - `reissueTokenAfterEmailChange` → UserRepository#findByEmail, AuthRedisService#removeAccessToken, AuthRedisService#removeRefreshToken, JwtTokenProvider#generateTokenPair

## 3. 클래스 목록 (역할별)

### controller (17)

- `LegacyBankAccountController` controller/LegacyBankAccountController.java · public: createOrUpdateBankAccount, getBankAccount, deleteBankAccount · 주입: LegacyBankAccountCommandService, LegacyBankAccountQueryService
- `LegacyBlockedProfileController` controller/LegacyBlockedProfileController.java · public: blockProfile, unblockProfile, getBlockedProfiles · 주입: LegacyBlockedProfileCommandService, LegacyBlockedProfileQueryService
- `LegacyFollowController` controller/LegacyFollowController.java · public: createFollow, deleteFollow, getFollowers, getFollowings · 주입: LegacyFollowCommandService, LegacyFollowQueryService
- `LegacyProfileController` controller/LegacyProfileController.java · public: getProfile, updateProfile, getProfileImage · 주입: LegacyProfileQueryService, ProfileCommandService, FileUtils
- `LegacyAdminGradeController` controller/admin/LegacyAdminGradeController.java · public: getAllGrades, getGradeById, createGrade, updateGrade, deleteGrade, getGradeUserCounts, getGradeStatistics, runGradeAutoAssignment · 주입: LegacyAdminGradeService, LegacyUserQueryService
- `LegacyAdminPointController` controller/admin/LegacyAdminPointController.java · public: getUserPointHistory, getUserAvailablePoints, getUserPointSummary, getPointDetail, addPointByAdmin, deductPointByAdmin, getPointStatistics · 주입: LegacyAdminPointService, LegacyUserQueryService
- `LegacyAdminSanctionController` controller/admin/LegacyAdminSanctionController.java · public: searchSanctions, getSanctionById, createSanction, updateSanction, reviewSanction, cancelSanction, getUserSanctions, getSanctionStatistics · 주입: LegacyAdminSanctionService, LegacyUserQueryService
- `LegacyAdminUserController` controller/admin/LegacyAdminUserController.java · public: searchUsers, getUserDetail, updateUserStatus, updateUserGrade, updateUserRole, manageUserPoints, exportUserData · 주입: LegacyAdminUserService, LegacyUserQueryService
- `LegacyAdminAuthController` controller/command/LegacyAdminAuthController.java · public: adminLogin, findAdminEmail, checkAdminToken, refreshAdminToken, findAdminPassword, logout · 주입: AuthService, LegacyUserQueryService, JwtTokenProvider, LegacyPasswordResetService, AuthRedisService
- `LegacyAuthController` controller/command/LegacyAuthController.java · public: login, logout, refresh, getUserEmail, checkAuthStatus
- `LegacyIdentityVerificationController` controller/command/LegacyIdentityVerificationController.java · public: verifyIdentity · 주입: LegacyIdentityVerificationService
- `LegacyOAuth2AuthorizationController` controller/command/LegacyOAuth2AuthorizationController.java · public: redirectToOAuthProvider
- `LegacyOAuthController` controller/command/LegacyOAuthController.java · public: completeOAuthSignup, checkAuthStatus, getOAuthConnections · 주입: UserRepository, LegacyUserUpdateService, JwtTokenProvider, AuthRedisService
- `LegacyUserCommandController` controller/command/LegacyUserCommandController.java · public: registerUser, verifyIdentity, updateUserInfo · 주입: LegacyUserCommandService, LegacyUserUpdateService, LegacyUserQueryService, AuthRedisService, LegacyIdentityVerificationService
- `LegacyPointCommandController` controller/point/LegacyPointCommandController.java · public: addPoint, usePoint · 주입: LegacyPointCommandService
- `LegacyPointQueryController` controller/point/LegacyPointQueryController.java · public: getAllPointHistory, getAvailablePoints, getPointSummary, getPointDetail · 주입: LegacyPointQueryService
- `LegacyUserQueryController` controller/query/LegacyUserQueryController.java · public: findEmail, resetPasswordEligibility, resetPasswordToEmail, resetPassword, getLoginInfo, deleteAccount · 주입: AuthService, LegacyUserQueryService, LegacyPasswordResetService, LegacyUserUpdateService, JwtTokenProvider, LegacyUserCommandService, AuthRedisService

### service-command (7)

- `LegacyEmailService` service/command/LegacyEmailService.java · public: sendEmail · 주입: JavaMailSender
- `LegacyIdentityVerificationService` service/command/LegacyIdentityVerificationService.java · public: verifyIdentity · 주입: RestTemplate, ObjectMapper
- `LegacyPasswordResetService` service/command/LegacyPasswordResetService.java · public: checkPasswordResetEligibility, resetPassword, checkPasswordResetAndSendEmail, AdminCheckPasswordResetAndSendEmail · 주입: UserRepository, PasswordEncoder, LegacyEmailService
- `LegacyProfileService` service/command/LegacyProfileService.java · public: createDefaultProfile
- `LegacyUserCommandService` service/command/LegacyUserCommandService.java · public: registerUser, deleteAccount · 주입: UserRepository, PasswordEncoder, ProfileCommandService, FileUtils, LegacyNotificationCommandService, LegacyIdentityVerificationService
- `LegacyUserSanctionService` service/command/LegacyUserSanctionService.java
- `LegacyUserUpdateService` service/command/LegacyUserUpdateService.java · public: updateLoginInfo, reissueTokenAfterEmailChange · 주입: UserRepository, PasswordEncoder, AuthRedisService, JwtTokenProvider

### service-query (1)

- `LegacyUserQueryService` service/query/LegacyUserQueryService.java · public: checkAdminRole, findEmailByPhoneNumber, getLoginInfo, findByEmail · 주입: UserRepository

### service (14)

- `LegacyCustomUserDetailsService` security/LegacyCustomUserDetailsService.java · extends/implements UserDetailsService · public: loadUserByUsername · 주입: UserRepository, PasswordEncoder
- `LegacyBlockedProfileCommandService` service/BlockProfile/LegacyBlockedProfileCommandService.java · public: blockProfile, unblockProfile · 주입: ProfileRepository, LegacyBlockedProfileRepository
- `LegacyBlockedProfileQueryService` service/BlockProfile/LegacyBlockedProfileQueryService.java · public: getBlockedProfiles · 주입: LegacyBlockedProfileRepository
- `LegacyAdminGradeService` service/admin/LegacyAdminGradeService.java · public: getAllGrades, getGradeById, createGrade, updateGrade, deleteGrade, getGradeUserCounts, getGradeStatistics, runGradeAutoAssignment · 주입: LegacyUserGradeRepository, UserRepository
- `LegacyAdminPointService` service/admin/LegacyAdminPointService.java · public: getUserPointHistory, getUserAvailablePoints, getUserPointSummary, getPointDetail, addPointByAdmin, deductPointByAdmin, getPointStatistics · 주입: UserRepository, LegacyPointRepository
- `LegacyAdminSanctionService` service/admin/LegacyAdminSanctionService.java · public: searchSanctions, getSanctionById, createSanction, updateSanction, reviewSanction, cancelSanction, getUserSanctions, getSanctionStatistics, processExpiredSanctions · 주입: LegacyUserSanctionRepository, UserRepository
- `LegacyAdminUserService` service/admin/LegacyAdminUserService.java · public: searchUsers, getUserDetail, updateUserStatus, updateUserGrade, updateUserRole, manageUserPoints, exportUsersCsv · 주입: UserRepository, ProfileRepository, LegacyPointRepository, LegacyUserGradeRepository, LegacyUserSanctionRepository, BankAccountRepository
- `LegacyBankAccountCommandService` service/bankaccount/LegacyBankAccountCommandService.java · public: createOrUpdateBankAccount, deleteBankAccount · 주입: UserRepository
- `LegacyBankAccountQueryService` service/bankaccount/LegacyBankAccountQueryService.java · public: getBankAccount · 주입: BankAccountRepository
- `LegacyFollowCommandService` service/follow/LegacyFollowCommandService.java · public: createFollow, deleteFollow · 주입: LegacyFollowRepository, ProfileRepository, LegacyNotificationCommandService
- `LegacyFollowQueryService` service/follow/LegacyFollowQueryService.java · public: getFollowers, getFollowings · 주입: LegacyFollowRepository, ProfileRepository
- `LegacyPointCommandService` service/point/LegacyPointCommandService.java · public: addPoint, usePoint, expirePoints · 주입: LegacyPointRepository, LegacyUserQueryService
- `LegacyPointQueryService` service/point/LegacyPointQueryService.java · public: getAllPointHistory, getAvailablePoints, getPointSummary, getPointDetail · 주입: LegacyPointRepository, LegacyUserQueryService
- `LegacyProfileQueryService` service/profile/LegacyProfileQueryService.java · public: getProfileInfo, getProfileByEmail, getProfileImageFileName · 주입: ProfileRepository, LegacyBlockedProfileQueryService

### repository (5)

- `LegacyBlockedProfileRepository` (interface) repository/LegacyBlockedProfileRepository.java · extends/implements JpaRepository · public: findByProfile, findByProfileAndBlockedProfileId, findBlockedProfileIdsByProfile, findAllByProfileWithBlocked, findByProfileAndBlockedProfile, findAllByProfileEmailWithBlocked
- `LegacyFollowRepository` (interface) repository/LegacyFollowRepository.java · extends/implements JpaRepository · public: findByFollowerAndFollowing, findFollowersByProfileId, findFollowingsByProfileId
- `LegacyPointRepository` (interface) repository/LegacyPointRepository.java · extends/implements JpaRepository · public: findByUserOrderByCreatedDateDesc, findByUserAndStatusOrderByExpirationDateAsc, findByStatusAndExpirationDateBefore, getTotalAvailablePoints, findExpiringPoints
- `LegacyUserGradeRepository` (interface) repository/LegacyUserGradeRepository.java · extends/implements JpaRepository · public: findByLevel, findByName, findAllByOrderByLevelAsc, findGradeByPurchaseAmount, countUsersByGradeId, countUsersGroupByGrade
- `LegacyUserSanctionRepository` (interface) repository/LegacyUserSanctionRepository.java · extends/implements JpaRepository, JpaSpecificationExecutor · public: findByUserOrderByCreatedDateDesc, findByUserAndStatusOrderByStartDateDesc, findByStatus, findByType, findExpirableSanctions, findByCreatedDateBetween, findByUserEmail, countActiveSanctions, countExpiredSanctions, countPendingSanctions, countTotalSanctions

### other (9)

- `LegacyUserSanctionSpecifications` repository/LegacyUserSanctionSpecifications.java · public: withSearchCriteria, hasUserId, hasStatus, hasType, createdBetween, startDateBetween, endDateBetween, userEmailLike
- `LegacyPointExpirationScheduler` scheduler/LegacyPointExpirationScheduler.java · public: checkExpiredPoints · 주입: LegacyPointCommandService
- `LegacyOAuthAttributes` security/oauth2/LegacyOAuthAttributes.java · public: of
- `LegacyPasswordUtils` utils/LegacyPasswordUtils.java · public: generateRandomPassword
- `LegacyBankAccountControllerValidator` validate/LegacyBankAccountControllerValidator.java · public: validateBankAccountDto
- `LegacyBlockedProfileControllerValidator` validate/LegacyBlockedProfileControllerValidator.java
- `LegacyFollowControllerValidator` validate/LegacyFollowControllerValidator.java
- `LegacyProfileControllerValidator` validate/LegacyProfileControllerValidator.java
- `LegacyUserControllerValidator` validate/LegacyUserControllerValidator.java · public: validateUserRegistrationDto, validateLoginRequestDto, validateEmailFindRequestDto, validatePasswordResetEligibilityDto, validatePasswordResetDto, validateLoginInfoUpdateDto

### 다른 도메인 참조 (import·주입 기준)

- **legacynotification**: `LegacyNotificationCommandService` ← LegacyFollowCommandService, LegacyUserCommandService
- **notification**: `NotificationCategory` ← LegacyFollowCommandService; `NotificationType` ← LegacyFollowCommandService
- **user**: `AuthRedisService` ← LegacyAdminAuthController, LegacyOAuthController, LegacyUserCommandController, LegacyUserQueryController, LegacyUserUpdateService; `AuthService` ← LegacyAdminAuthController, LegacyUserQueryController; `BankAccount` ← LegacyBankAccountCommandService, LegacyBankAccountQueryService; `BankAccountRepository` ← LegacyAdminUserService, LegacyBankAccountQueryService; `BlockedProfile` ← LegacyBlockedProfileCommandService, LegacyBlockedProfileQueryService, LegacyBlockedProfileRepository; `Follow` ← LegacyFollowCommandService, LegacyFollowQueryService, LegacyFollowRepository; `Gender` ← LegacyAdminAuthController, LegacyUserCommandService; `Point` ← LegacyAdminPointService, LegacyPointCommandService, LegacyPointQueryService, LegacyPointRepository; `PointStatus` ← LegacyAdminPointService, LegacyPointCommandService, LegacyPointQueryService, LegacyPointRepository; `Profile` ← LegacyBlockedProfileCommandService, LegacyBlockedProfileQueryService, LegacyBlockedProfileRepository, LegacyFollowCommandService, LegacyFollowQueryService, LegacyFollowRepository, LegacyProfileQueryService, LegacyProfileService; `ProfileCommandService` ← LegacyProfileController, LegacyUserCommandService; `ProfileRepository` ← LegacyAdminUserService, LegacyBlockedProfileCommandService, LegacyFollowCommandService, LegacyFollowQueryService, LegacyProfileQueryService; `Role` ← LegacyAdminAuthController, LegacyUserQueryService; `SanctionStatus` ← LegacyAdminSanctionController, LegacyUserSanctionRepository, LegacyUserSanctionSpecifications; `SanctionType` ← LegacyAdminSanctionController, LegacyUserSanctionRepository, LegacyUserSanctionSpecifications; `ShoeSize` ← LegacyUserUpdateService; `User` ← LegacyAdminAuthController, LegacyAdminGradeService, LegacyAdminPointService, LegacyBankAccountCommandService, LegacyCustomUserDetailsService, LegacyOAuthController, LegacyPasswordResetService, LegacyPointCommandService, LegacyPointQueryService, LegacyPointRepository, LegacyProfileService, LegacyUserCommandController, LegacyUserCommandService, LegacyUserQueryController, LegacyUserQueryService, LegacyUserSanctionRepository, LegacyUserSanctionSpecifications, LegacyUserUpdateService; `UserGrade` ← LegacyAdminGradeService, LegacyUserGradeRepository; `UserRepository` ← LegacyAdminGradeService, LegacyAdminPointService, LegacyAdminSanctionService, LegacyAdminUserService, LegacyBankAccountCommandService, LegacyCustomUserDetailsService, LegacyOAuthController, LegacyPasswordResetService, LegacyUserCommandService, LegacyUserQueryService, LegacyUserUpdateService; `UserSanction` ← LegacyUserSanctionRepository, LegacyUserSanctionSpecifications

### 이 도메인을 참조하는 다른 도메인

- **legacychatquestion**: LegacyUserQueryService
- **legacyevent**: LegacyUserQueryService
- **legacyfaq**: LegacyUserQueryService
- **legacyinquiry**: LegacyUserQueryService
- **legacyinspection**: LegacyUserQueryService
- **legacynotice**: LegacyUserQueryService
- **legacyorder**: LegacyUserQueryService
- **legacypayment**: LegacyUserQueryService
- **legacyproduct**: LegacyUserQueryService
- **legacysale**: LegacyBankAccountQueryService, LegacyUserQueryService
- **legacystyle**: LegacyProfileQueryService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

