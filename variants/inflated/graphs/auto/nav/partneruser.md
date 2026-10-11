# partneruser 도메인

기준 경로: `src/main/java/com/fream/back/domain/partneruser/` · 파일 53개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/partner/bank-account` | `PartnerBankAccountController#createOrUpdateBankAccount` | `this#extractEmailFromSecurityContext`, `PartnerBankAccountCommandService#createOrUpdateBankAccount` |
| GET | `/partner/bank-account` | `PartnerBankAccountController#getBankAccount` | `this#extractEmailFromSecurityContext`, `PartnerBankAccountQueryService#getBankAccount` |
| DELETE | `/partner/bank-account` | `PartnerBankAccountController#deleteBankAccount` | `this#extractEmailFromSecurityContext`, `PartnerBankAccountCommandService#deleteBankAccount` |
| POST | `/partner/profiles/blocked` | `PartnerBlockedProfileController#blockProfile` | `this#extractEmailFromSecurityContext`, `PartnerBlockedProfileCommandService#blockProfile` |
| DELETE | `/partner/profiles/blocked` | `PartnerBlockedProfileController#unblockProfile` | `this#extractEmailFromSecurityContext`, `PartnerBlockedProfileCommandService#unblockProfile` |
| GET | `/partner/profiles/blocked` | `PartnerBlockedProfileController#getBlockedProfiles` | `this#extractEmailFromSecurityContext`, `PartnerBlockedProfileQueryService#getBlockedProfiles` |
| POST | `/partner/follows/{profileId}` | `PartnerFollowController#createFollow` | `this#extractEmailFromSecurityContext`, `PartnerFollowCommandService#createFollow` |
| DELETE | `/partner/follows/{profileId}` | `PartnerFollowController#deleteFollow` | `this#extractEmailFromSecurityContext`, `PartnerFollowCommandService#deleteFollow` |
| GET | `/partner/follows/followers` | `PartnerFollowController#getFollowers` | `this#extractEmailFromSecurityContext`, `PartnerFollowQueryService#getFollowers` |
| GET | `/partner/follows/followings` | `PartnerFollowController#getFollowings` | `this#extractEmailFromSecurityContext`, `PartnerFollowQueryService#getFollowings` |
| GET | `/partner/profiles` | `PartnerProfileController#getProfile` | `this#extractEmailFromSecurityContext`, `PartnerProfileQueryService#getProfileInfo` |
| PUT | `/partner/profiles` | `PartnerProfileController#updateProfile` | `this#extractEmailFromSecurityContext`, `ProfileCommandService#updateProfile` |
| GET | `/partner/profiles/{profileId}/image` | `PartnerProfileController#getProfileImage` | `PartnerProfileQueryService#getProfileImageFileName` |
| GET | `/partner/admin/users/grades` | `PartnerAdminGradeController#getAllGrades` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerAdminGradeService#getAllGrades` |
| GET | `/partner/admin/users/grades/{gradeId}` | `PartnerAdminGradeController#getGradeById` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerAdminGradeService#getGradeById` |
| POST | `/partner/admin/users/grades` | `PartnerAdminGradeController#createGrade` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerAdminGradeService#createGrade` |
| PUT | `/partner/admin/users/grades/{gradeId}` | `PartnerAdminGradeController#updateGrade` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerAdminGradeService#updateGrade` |
| DELETE | `/partner/admin/users/grades/{gradeId}` | `PartnerAdminGradeController#deleteGrade` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerAdminGradeService#deleteGrade` |
| GET | `/partner/admin/users/grades/counts` | `PartnerAdminGradeController#getGradeUserCounts` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerAdminGradeService#getGradeUserCounts` |
| GET | `/partner/admin/users/grades/statistics` | `PartnerAdminGradeController#getGradeStatistics` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerAdminGradeService#getGradeStatistics` |
| POST | `/partner/admin/users/grades/auto-assign` | `PartnerAdminGradeController#runGradeAutoAssignment` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerAdminGradeService#runGradeAutoAssignment` |
| GET | `/partner/admin/users/points/user/{userId}` | `PartnerAdminPointController#getUserPointHistory` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerAdminPointService#getUserPointHistory` |
| GET | `/partner/admin/users/points/user/{userId}/available` | `PartnerAdminPointController#getUserAvailablePoints` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerAdminPointService#getUserAvailablePoints` |
| GET | `/partner/admin/users/points/user/{userId}/summary` | `PartnerAdminPointController#getUserPointSummary` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerAdminPointService#getUserPointSummary` |
| GET | `/partner/admin/users/points/{pointId}` | `PartnerAdminPointController#getPointDetail` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerAdminPointService#getPointDetail` |
| POST | `/partner/admin/users/points/user/{userId}/add` | `PartnerAdminPointController#addPointByAdmin` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerAdminPointService#addPointByAdmin` |
| POST | `/partner/admin/users/points/user/{userId}/deduct` | `PartnerAdminPointController#deductPointByAdmin` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerAdminPointService#deductPointByAdmin` |
| GET | `/partner/admin/users/points/statistics` | `PartnerAdminPointController#getPointStatistics` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerAdminPointService#getPointStatistics` |
| GET | `/partner/admin/users/sanctions/search` | `PartnerAdminSanctionController#searchSanctions` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerAdminSanctionService#searchSanctions` |
| GET | `/partner/admin/users/sanctions/{sanctionId}` | `PartnerAdminSanctionController#getSanctionById` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerAdminSanctionService#getSanctionById` |
| POST | `/partner/admin/users/sanctions` | `PartnerAdminSanctionController#createSanction` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerAdminSanctionService#createSanction` |
| PUT | `/partner/admin/users/sanctions/{sanctionId}` | `PartnerAdminSanctionController#updateSanction` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerAdminSanctionService#updateSanction` |
| PATCH | `/partner/admin/users/sanctions/{sanctionId}/review` | `PartnerAdminSanctionController#reviewSanction` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerAdminSanctionService#reviewSanction` |
| PATCH | `/partner/admin/users/sanctions/{sanctionId}/cancel` | `PartnerAdminSanctionController#cancelSanction` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerAdminSanctionService#cancelSanction` |
| GET | `/partner/admin/users/sanctions/user/{userId}` | `PartnerAdminSanctionController#getUserSanctions` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerAdminSanctionService#getUserSanctions` |
| GET | `/partner/admin/users/sanctions/statistics` | `PartnerAdminSanctionController#getSanctionStatistics` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerAdminSanctionService#getSanctionStatistics` |
| GET | `/partner/admin/users/search` | `PartnerAdminUserController#searchUsers` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerAdminUserService#searchUsers` |
| GET | `/partner/admin/users/{userId}` | `PartnerAdminUserController#getUserDetail` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerAdminUserService#getUserDetail` |
| PATCH | `/partner/admin/users/{userId}/status` | `PartnerAdminUserController#updateUserStatus` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerAdminUserService#updateUserStatus` |
| PATCH | `/partner/admin/users/{userId}/grade` | `PartnerAdminUserController#updateUserGrade` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerAdminUserService#updateUserGrade` |
| PATCH | `/partner/admin/users/{userId}/role` | `PartnerAdminUserController#updateUserRole` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerAdminUserService#updateUserRole` |
| POST | `/partner/admin/users/{userId}/points` | `PartnerAdminUserController#manageUserPoints` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerAdminUserService#manageUserPoints` |
| GET | `/partner/admin/users/export` | `PartnerAdminUserController#exportUserData` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerAdminUserService#exportUsersCsv` |
| POST | `/partner/admin/auth/login` | `PartnerAdminAuthController#adminLogin` | `this#getClientIp`, `AuthService#login`, `JwtTokenProvider#getEmailFromToken`, `PartnerUserQueryService#checkAdminRole`, `this#setCookie` |
| POST | `/partner/admin/auth/find-email` | `PartnerAdminAuthController#findAdminEmail` | `PartnerUserQueryService#findEmailByPhoneNumber`, `PartnerUserQueryService#checkAdminRole` |
| GET | `/partner/admin/auth/check` | `PartnerAdminAuthController#checkAdminToken` | `this#getCookieValue`, `JwtTokenProvider#validateToken`, `JwtTokenProvider#getEmailFromToken`, `PartnerUserQueryService#checkAdminRole` |
| POST | `/partner/admin/auth/refresh` | `PartnerAdminAuthController#refreshAdminToken` | `this#getCookieValue`, `JwtTokenProvider#validateToken`, `AuthRedisService#isRefreshTokenValid`, `JwtTokenProvider#getEmailFromToken`, `PartnerUserQueryService#findByEmail`, `PartnerUserQueryService#checkAdminRole`, `this#getClientIp`, `JwtTokenProvider#generateTokenPair`, `this#setCookie` |
| POST | `/partner/admin/auth/find-password` | `PartnerAdminAuthController#findAdminPassword` | `PartnerUserQueryService#findByEmail`, `PartnerPasswordResetService#AdminCheckPasswordResetAndSendEmail` |
| POST | `/partner/admin/auth/logout` | `PartnerAdminAuthController#logout` | `this#getCookieValue`, `JwtTokenProvider#validateToken`, `JwtTokenProvider#getEmailFromToken`, `PartnerUserQueryService#checkAdminRole`, `AuthRedisService#removeAccessToken`, `AuthRedisService#removeRefreshToken`, `this#expireCookie` |
| POST | `/partner/auth/login` | `PartnerAuthController#login` |  |
| POST | `/partner/auth/logout` | `PartnerAuthController#logout` |  |
| POST | `/partner/auth/refresh` | `PartnerAuthController#refresh` |  |
| GET | `/partner/auth/email` | `PartnerAuthController#getUserEmail` |  |
| GET | `/partner/auth/status` | `PartnerAuthController#checkAuthStatus` |  |
| POST | `/partner/identity-verification/verify` | `PartnerIdentityVerificationController#verifyIdentity` | `PartnerIdentityVerificationService#verifyIdentity` |
| GET | `/oauth2/authorization/{registrationId}` | `PartnerOAuth2AuthorizationController#redirectToOAuthProvider` |  |
| POST | `/partner/oauth/complete-signup` | `PartnerOAuthController#completeOAuthSignup` | `this#extractEmailFromToken`, `UserRepository#findByEmail`, `UserRepository#findByReferralCode`, `UserRepository#save~`, `this#getClientIp`, `JwtTokenProvider#generateTokenPair`, `this#setCookie` |
| GET | `/partner/oauth/status` | `PartnerOAuthController#checkAuthStatus` | `this#getCookieValue`, `JwtTokenProvider#validateToken`, `AuthRedisService#isAccessTokenValid`, `JwtTokenProvider#getEmailFromToken` |
| GET | `/partner/oauth/connections` | `PartnerOAuthController#getOAuthConnections` | `this#getCookieValue`, `JwtTokenProvider#getEmailFromToken`, `UserRepository#findByEmail` |
| POST | `/partner/users/register` | `PartnerUserCommandController#registerUser` | `PartnerUserCommandService#registerUser` |
| POST | `/partner/users/verify-identity` | `PartnerUserCommandController#verifyIdentity` | `PartnerIdentityVerificationService#verifyIdentity` |
| PUT | `/partner/users/update` | `PartnerUserCommandController#updateUserInfo` | `PartnerUserUpdateService#updateLoginInfo`, `PartnerUserQueryService#getLoginInfo`, `this#getCookieValue`, `AuthRedisService#removeAccessToken`, `AuthRedisService#removeRefreshToken`, `PartnerUserUpdateService#reissueTokenAfterEmailChange`, `this#setCookie` |
| POST | `/partner/points/commands` | `PartnerPointCommandController#addPoint` | `PartnerPointCommandService#addPoint` |
| POST | `/partner/points/commands/use` | `PartnerPointCommandController#usePoint` | `PartnerPointCommandService#usePoint` |
| GET | `/partner/points/queries` | `PartnerPointQueryController#getAllPointHistory` | `PartnerPointQueryService#getAllPointHistory` |
| GET | `/partner/points/queries/available` | `PartnerPointQueryController#getAvailablePoints` | `PartnerPointQueryService#getAvailablePoints` |
| GET | `/partner/points/queries/summary` | `PartnerPointQueryController#getPointSummary` | `PartnerPointQueryService#getPointSummary` |
| GET | `/partner/points/queries/{pointId}` | `PartnerPointQueryController#getPointDetail` | `PartnerPointQueryService#getPointDetail` |
| POST | `/partner/users/find-email` | `PartnerUserQueryController#findEmail` | `PartnerUserQueryService#findEmailByPhoneNumber` |
| POST | `/partner/users/reset-password` | `PartnerUserQueryController#resetPasswordEligibility` | `PartnerPasswordResetService#checkPasswordResetEligibility` |
| POST | `/partner/users/reset-password-sandEmail` | `PartnerUserQueryController#resetPasswordToEmail` | `PartnerPasswordResetService#checkPasswordResetAndSendEmail` |
| POST | `/partner/users/reset` | `PartnerUserQueryController#resetPassword` | `PartnerPasswordResetService#resetPassword` |
| GET | `/partner/users/login-info` | `PartnerUserQueryController#getLoginInfo` | `PartnerUserQueryService#getLoginInfo` |
| DELETE | `/partner/users/delete-account` | `PartnerUserQueryController#deleteAccount` | `PartnerUserCommandService#deleteAccount` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `PartnerAdminGradeService`
  - `getAllGrades` → PartnerUserGradeRepository#findAllByOrderByLevelAsc
  - `getGradeById` → PartnerUserGradeRepository#findById~, this#convertToGradeResponseDto
  - `createGrade` → PartnerUserGradeRepository#findByLevel, PartnerUserGradeRepository#findByName, PartnerUserGradeRepository#save~, this#convertToGradeResponseDto
  - `updateGrade` → PartnerUserGradeRepository#findById~, PartnerUserGradeRepository#findByName, this#convertToGradeResponseDto
  - `deleteGrade` → PartnerUserGradeRepository#findById~, PartnerUserGradeRepository#countUsersByGradeId, PartnerUserGradeRepository#delete~
  - `getGradeUserCounts` → PartnerUserGradeRepository#countUsersGroupByGrade, PartnerUserGradeRepository#findById~
  - `getGradeStatistics` → PartnerUserGradeRepository#findAllByOrderByLevelAsc, PartnerUserGradeRepository#countUsersByGradeId
  - `runGradeAutoAssignment` → UserRepository#findAll~, PartnerUserGradeRepository#findGradeByPurchaseAmount
  - `convertToGradeResponseDto` → PartnerUserGradeRepository#countUsersByGradeId
- `PartnerAdminPointService`
  - `getUserPointHistory` → this#getUserById, PartnerPointRepository#findByUserOrderByCreatedDateDesc
  - `getUserAvailablePoints` → this#getUserById, PartnerPointRepository#findByUserAndStatusOrderByExpirationDateAsc
  - `getUserPointSummary` → this#getUserById, PartnerPointRepository#findByUserAndStatusOrderByExpirationDateAsc
  - `getPointDetail` → PartnerPointRepository#findById~
  - `addPointByAdmin` → this#getUserById, PartnerPointRepository#save~
  - `deductPointByAdmin` → this#getUserById, PartnerPointRepository#findByUserAndStatusOrderByExpirationDateAsc, PartnerPointRepository#save~
  - `getUserById` → UserRepository#findById~
- `PartnerAdminSanctionService`
  - `searchSanctions` → PartnerUserSanctionRepository#findAll~
  - `getSanctionById` → PartnerUserSanctionRepository#findById~, this#convertToSanctionResponseDto
  - `createSanction` → UserRepository#findById~, PartnerUserSanctionRepository#save~, this#convertToSanctionResponseDto
  - `updateSanction` → PartnerUserSanctionRepository#findById~, this#convertToSanctionResponseDto
  - `reviewSanction` → PartnerUserSanctionRepository#findById~, this#convertToSanctionResponseDto
  - `cancelSanction` → PartnerUserSanctionRepository#findById~, this#hasOtherActiveBanSanctions, this#convertToSanctionResponseDto
  - `getUserSanctions` → UserRepository#findById~, PartnerUserSanctionRepository#findByUserOrderByCreatedDateDesc
  - `getSanctionStatistics` → PartnerUserSanctionRepository#countTotalSanctions, PartnerUserSanctionRepository#countActiveSanctions, PartnerUserSanctionRepository#countExpiredSanctions, PartnerUserSanctionRepository#countPendingSanctions
  - `processExpiredSanctions` → PartnerUserSanctionRepository#findExpirableSanctions, this#hasOtherActiveBanSanctions
  - `hasOtherActiveBanSanctions` → PartnerUserSanctionRepository#findByUserAndStatusOrderByStartDateDesc
- `PartnerAdminUserService`
  - `searchUsers` → UserRepository#findAll~
  - `getUserDetail` → UserRepository#findById~, this#convertToUserDetailResponseDto
  - `updateUserStatus` → UserRepository#findById~, PartnerUserSanctionRepository#save~, this#convertToUserDetailResponseDto
  - `updateUserGrade` → UserRepository#findById~, PartnerUserGradeRepository#findById~, this#convertToUserDetailResponseDto
  - `updateUserRole` → UserRepository#findById~, this#convertToUserDetailResponseDto
  - `manageUserPoints` → UserRepository#findById~, PartnerPointRepository#save~, PartnerPointRepository#findByUserAndStatusOrderByExpirationDateAsc
  - `exportUsersCsv` → UserRepository#findAll~
  - `convertToUserSearchResponseDto` → ProfileRepository#findByUser
  - `convertToUserDetailResponseDto` → ProfileRepository#findByUser, BankAccountRepository#findByUser_Email, PartnerUserSanctionRepository#findByUserAndStatusOrderByStartDateDesc, this#convertToProfileDto, this#convertToBankAccountDto, this#convertToSanctionBriefDtoList
- `PartnerBankAccountCommandService`
  - `createOrUpdateBankAccount` → UserRepository#findByEmail
  - `deleteBankAccount` → UserRepository#findByEmail
- `PartnerBankAccountQueryService`
  - `getBankAccount` → BankAccountRepository#findByUser_Email
- `PartnerBlockedProfileCommandService`
  - `blockProfile` → ProfileRepository#findByUserEmailWithFetchJoin, ProfileRepository#findById~, PartnerBlockedProfileRepository#findByProfileAndBlockedProfile, PartnerBlockedProfileRepository#save~
  - `unblockProfile` → ProfileRepository#findByUserEmailWithFetchJoin, ProfileRepository#findById~, PartnerBlockedProfileRepository#findByProfileAndBlockedProfile, PartnerBlockedProfileRepository#delete~
- `PartnerBlockedProfileQueryService`
  - `getBlockedProfiles` → PartnerBlockedProfileRepository#findAllByProfileWithBlocked, PartnerBlockedProfileRepository#findAllByProfileEmailWithBlocked
- `PartnerCustomUserDetailsService`
  - `loadUserByUsername` → UserRepository#findByEmail
- `PartnerFollowCommandService`
  - `createFollow` → ProfileRepository#findByUserEmailWithFetchJoin, ProfileRepository#findById~, PartnerFollowRepository#findByFollowerAndFollowing, PartnerFollowRepository#save~, PartnerNotificationCommandService#createNotification
  - `deleteFollow` → ProfileRepository#findByUserEmailWithFetchJoin, ProfileRepository#findById~, PartnerFollowRepository#findByFollowerAndFollowing, PartnerFollowRepository#delete~
- `PartnerFollowQueryService`
  - `getFollowers` → ProfileRepository#findByUserEmailWithFetchJoin, PartnerFollowRepository#findFollowersByProfileId
  - `getFollowings` → ProfileRepository#findByUserEmailWithFetchJoin, PartnerFollowRepository#findFollowingsByProfileId
- `PartnerOAuthAttributes`
  - `of` → this#ofGoogle, this#ofNaver, this#ofKakao
- `PartnerPasswordResetService`
  - `checkPasswordResetEligibility` → UserRepository#findByEmailAndPhoneNumber
  - `resetPassword` → UserRepository#findByEmailAndPhoneNumber
  - `checkPasswordResetAndSendEmail` → UserRepository#findByEmailAndPhoneNumber, PartnerEmailService#sendEmail
  - `AdminCheckPasswordResetAndSendEmail` → UserRepository#findByEmailAndPhoneNumber, PartnerEmailService#sendEmail
- `PartnerPasswordUtils`
  - `generateRandomPassword` → this#isValidPassword
- `PartnerPointCommandService`
  - `addPoint` → this#validateAddPointRequest, PartnerUserQueryService#findByEmail, PartnerPointRepository#save~
  - `usePoint` → this#validateUsePointRequest, PartnerUserQueryService#findByEmail, PartnerPointRepository#getTotalAvailablePoints, PartnerPointRepository#findByUserAndStatusOrderByExpirationDateAsc, PartnerPointRepository#save~
  - `expirePoints` → PartnerPointRepository#findByStatusAndExpirationDateBefore
- `PartnerPointExpirationScheduler`
  - `checkExpiredPoints` → PartnerPointCommandService#expirePoints
- `PartnerPointQueryService`
  - `getAllPointHistory` → PartnerUserQueryService#findByEmail, PartnerPointRepository#findByUserOrderByCreatedDateDesc
  - `getAvailablePoints` → PartnerUserQueryService#findByEmail, PartnerPointRepository#findByUserAndStatusOrderByExpirationDateAsc
  - `getPointSummary` → PartnerUserQueryService#findByEmail, PartnerPointRepository#getTotalAvailablePoints, this#getAllPointHistory, PartnerPointRepository#findExpiringPoints
  - `getPointDetail` → PartnerUserQueryService#findByEmail, PartnerPointRepository#findById~
- `PartnerProfileQueryService`
  - `getProfileInfo` → ProfileRepository#findByUserEmailWithFetchJoin, PartnerBlockedProfileQueryService#getBlockedProfiles
  - `getProfileByEmail` → ProfileRepository#findByUserEmailWithFetchJoin
  - `getProfileImageFileName` → ProfileRepository#findById~
- `PartnerProfileService`
  - `createDefaultProfile` → this#generateRandomProfileName
- `PartnerUserCommandService`
  - `registerUser` → UserRepository#existsByEmail, PartnerIdentityVerificationService#verifyIdentity, UserRepository#findByCi, this#validateAge, UserRepository#findByReferralCode, this#convertToGender, this#calculateAge, this#generateUniqueReferralCode, UserRepository#save~, ProfileCommandService#createDefaultProfile
  - `deleteAccount` → UserRepository#findByEmail, PartnerNotificationCommandService#deleteNotificationsByUser, FileUtils#deleteFile, UserRepository#delete~
  - `generateUniqueReferralCode` → UserRepository#findByReferralCode
- `PartnerUserControllerValidator`
  - `validateUserRegistrationDto` → this#isValidPassword
  - `validatePasswordResetDto` → this#isValidPassword
  - `validateLoginInfoUpdateDto` → this#isValidPassword
- `PartnerUserQueryService`
  - `checkAdminRole` → UserRepository#findByEmail
  - `findEmailByPhoneNumber` → UserRepository#findByPhoneNumber
  - `getLoginInfo` → UserRepository#findByEmail
  - `findByEmail` → UserRepository#findByEmail
- `PartnerUserUpdateService`
  - `updateLoginInfo` → UserRepository#findByEmail, UserRepository#existsByEmail
  - `reissueTokenAfterEmailChange` → UserRepository#findByEmail, AuthRedisService#removeAccessToken, AuthRedisService#removeRefreshToken, JwtTokenProvider#generateTokenPair

## 3. 클래스 목록 (역할별)

### controller (17)

- `PartnerBankAccountController` controller/PartnerBankAccountController.java · public: createOrUpdateBankAccount, getBankAccount, deleteBankAccount · 주입: PartnerBankAccountCommandService, PartnerBankAccountQueryService
- `PartnerBlockedProfileController` controller/PartnerBlockedProfileController.java · public: blockProfile, unblockProfile, getBlockedProfiles · 주입: PartnerBlockedProfileCommandService, PartnerBlockedProfileQueryService
- `PartnerFollowController` controller/PartnerFollowController.java · public: createFollow, deleteFollow, getFollowers, getFollowings · 주입: PartnerFollowCommandService, PartnerFollowQueryService
- `PartnerProfileController` controller/PartnerProfileController.java · public: getProfile, updateProfile, getProfileImage · 주입: PartnerProfileQueryService, ProfileCommandService, FileUtils
- `PartnerAdminGradeController` controller/admin/PartnerAdminGradeController.java · public: getAllGrades, getGradeById, createGrade, updateGrade, deleteGrade, getGradeUserCounts, getGradeStatistics, runGradeAutoAssignment · 주입: PartnerAdminGradeService, PartnerUserQueryService
- `PartnerAdminPointController` controller/admin/PartnerAdminPointController.java · public: getUserPointHistory, getUserAvailablePoints, getUserPointSummary, getPointDetail, addPointByAdmin, deductPointByAdmin, getPointStatistics · 주입: PartnerAdminPointService, PartnerUserQueryService
- `PartnerAdminSanctionController` controller/admin/PartnerAdminSanctionController.java · public: searchSanctions, getSanctionById, createSanction, updateSanction, reviewSanction, cancelSanction, getUserSanctions, getSanctionStatistics · 주입: PartnerAdminSanctionService, PartnerUserQueryService
- `PartnerAdminUserController` controller/admin/PartnerAdminUserController.java · public: searchUsers, getUserDetail, updateUserStatus, updateUserGrade, updateUserRole, manageUserPoints, exportUserData · 주입: PartnerAdminUserService, PartnerUserQueryService
- `PartnerAdminAuthController` controller/command/PartnerAdminAuthController.java · public: adminLogin, findAdminEmail, checkAdminToken, refreshAdminToken, findAdminPassword, logout · 주입: AuthService, PartnerUserQueryService, JwtTokenProvider, PartnerPasswordResetService, AuthRedisService
- `PartnerAuthController` controller/command/PartnerAuthController.java · public: login, logout, refresh, getUserEmail, checkAuthStatus
- `PartnerIdentityVerificationController` controller/command/PartnerIdentityVerificationController.java · public: verifyIdentity · 주입: PartnerIdentityVerificationService
- `PartnerOAuth2AuthorizationController` controller/command/PartnerOAuth2AuthorizationController.java · public: redirectToOAuthProvider
- `PartnerOAuthController` controller/command/PartnerOAuthController.java · public: completeOAuthSignup, checkAuthStatus, getOAuthConnections · 주입: UserRepository, PartnerUserUpdateService, JwtTokenProvider, AuthRedisService
- `PartnerUserCommandController` controller/command/PartnerUserCommandController.java · public: registerUser, verifyIdentity, updateUserInfo · 주입: PartnerUserCommandService, PartnerUserUpdateService, PartnerUserQueryService, AuthRedisService, PartnerIdentityVerificationService
- `PartnerPointCommandController` controller/point/PartnerPointCommandController.java · public: addPoint, usePoint · 주입: PartnerPointCommandService
- `PartnerPointQueryController` controller/point/PartnerPointQueryController.java · public: getAllPointHistory, getAvailablePoints, getPointSummary, getPointDetail · 주입: PartnerPointQueryService
- `PartnerUserQueryController` controller/query/PartnerUserQueryController.java · public: findEmail, resetPasswordEligibility, resetPasswordToEmail, resetPassword, getLoginInfo, deleteAccount · 주입: AuthService, PartnerUserQueryService, PartnerPasswordResetService, PartnerUserUpdateService, JwtTokenProvider, PartnerUserCommandService, AuthRedisService

### service-command (7)

- `PartnerEmailService` service/command/PartnerEmailService.java · public: sendEmail · 주입: JavaMailSender
- `PartnerIdentityVerificationService` service/command/PartnerIdentityVerificationService.java · public: verifyIdentity · 주입: RestTemplate, ObjectMapper
- `PartnerPasswordResetService` service/command/PartnerPasswordResetService.java · public: checkPasswordResetEligibility, resetPassword, checkPasswordResetAndSendEmail, AdminCheckPasswordResetAndSendEmail · 주입: UserRepository, PasswordEncoder, PartnerEmailService
- `PartnerProfileService` service/command/PartnerProfileService.java · public: createDefaultProfile
- `PartnerUserCommandService` service/command/PartnerUserCommandService.java · public: registerUser, deleteAccount · 주입: UserRepository, PasswordEncoder, ProfileCommandService, FileUtils, PartnerNotificationCommandService, PartnerIdentityVerificationService
- `PartnerUserSanctionService` service/command/PartnerUserSanctionService.java
- `PartnerUserUpdateService` service/command/PartnerUserUpdateService.java · public: updateLoginInfo, reissueTokenAfterEmailChange · 주입: UserRepository, PasswordEncoder, AuthRedisService, JwtTokenProvider

### service-query (1)

- `PartnerUserQueryService` service/query/PartnerUserQueryService.java · public: checkAdminRole, findEmailByPhoneNumber, getLoginInfo, findByEmail · 주입: UserRepository

### service (14)

- `PartnerCustomUserDetailsService` security/PartnerCustomUserDetailsService.java · extends/implements UserDetailsService · public: loadUserByUsername · 주입: UserRepository, PasswordEncoder
- `PartnerBlockedProfileCommandService` service/BlockProfile/PartnerBlockedProfileCommandService.java · public: blockProfile, unblockProfile · 주입: ProfileRepository, PartnerBlockedProfileRepository
- `PartnerBlockedProfileQueryService` service/BlockProfile/PartnerBlockedProfileQueryService.java · public: getBlockedProfiles · 주입: PartnerBlockedProfileRepository
- `PartnerAdminGradeService` service/admin/PartnerAdminGradeService.java · public: getAllGrades, getGradeById, createGrade, updateGrade, deleteGrade, getGradeUserCounts, getGradeStatistics, runGradeAutoAssignment · 주입: PartnerUserGradeRepository, UserRepository
- `PartnerAdminPointService` service/admin/PartnerAdminPointService.java · public: getUserPointHistory, getUserAvailablePoints, getUserPointSummary, getPointDetail, addPointByAdmin, deductPointByAdmin, getPointStatistics · 주입: UserRepository, PartnerPointRepository
- `PartnerAdminSanctionService` service/admin/PartnerAdminSanctionService.java · public: searchSanctions, getSanctionById, createSanction, updateSanction, reviewSanction, cancelSanction, getUserSanctions, getSanctionStatistics, processExpiredSanctions · 주입: PartnerUserSanctionRepository, UserRepository
- `PartnerAdminUserService` service/admin/PartnerAdminUserService.java · public: searchUsers, getUserDetail, updateUserStatus, updateUserGrade, updateUserRole, manageUserPoints, exportUsersCsv · 주입: UserRepository, ProfileRepository, PartnerPointRepository, PartnerUserGradeRepository, PartnerUserSanctionRepository, BankAccountRepository
- `PartnerBankAccountCommandService` service/bankaccount/PartnerBankAccountCommandService.java · public: createOrUpdateBankAccount, deleteBankAccount · 주입: UserRepository
- `PartnerBankAccountQueryService` service/bankaccount/PartnerBankAccountQueryService.java · public: getBankAccount · 주입: BankAccountRepository
- `PartnerFollowCommandService` service/follow/PartnerFollowCommandService.java · public: createFollow, deleteFollow · 주입: PartnerFollowRepository, ProfileRepository, PartnerNotificationCommandService
- `PartnerFollowQueryService` service/follow/PartnerFollowQueryService.java · public: getFollowers, getFollowings · 주입: PartnerFollowRepository, ProfileRepository
- `PartnerPointCommandService` service/point/PartnerPointCommandService.java · public: addPoint, usePoint, expirePoints · 주입: PartnerPointRepository, PartnerUserQueryService
- `PartnerPointQueryService` service/point/PartnerPointQueryService.java · public: getAllPointHistory, getAvailablePoints, getPointSummary, getPointDetail · 주입: PartnerPointRepository, PartnerUserQueryService
- `PartnerProfileQueryService` service/profile/PartnerProfileQueryService.java · public: getProfileInfo, getProfileByEmail, getProfileImageFileName · 주입: ProfileRepository, PartnerBlockedProfileQueryService

### repository (5)

- `PartnerBlockedProfileRepository` (interface) repository/PartnerBlockedProfileRepository.java · extends/implements JpaRepository · public: findByProfile, findByProfileAndBlockedProfileId, findBlockedProfileIdsByProfile, findAllByProfileWithBlocked, findByProfileAndBlockedProfile, findAllByProfileEmailWithBlocked
- `PartnerFollowRepository` (interface) repository/PartnerFollowRepository.java · extends/implements JpaRepository · public: findByFollowerAndFollowing, findFollowersByProfileId, findFollowingsByProfileId
- `PartnerPointRepository` (interface) repository/PartnerPointRepository.java · extends/implements JpaRepository · public: findByUserOrderByCreatedDateDesc, findByUserAndStatusOrderByExpirationDateAsc, findByStatusAndExpirationDateBefore, getTotalAvailablePoints, findExpiringPoints
- `PartnerUserGradeRepository` (interface) repository/PartnerUserGradeRepository.java · extends/implements JpaRepository · public: findByLevel, findByName, findAllByOrderByLevelAsc, findGradeByPurchaseAmount, countUsersByGradeId, countUsersGroupByGrade
- `PartnerUserSanctionRepository` (interface) repository/PartnerUserSanctionRepository.java · extends/implements JpaRepository, JpaSpecificationExecutor · public: findByUserOrderByCreatedDateDesc, findByUserAndStatusOrderByStartDateDesc, findByStatus, findByType, findExpirableSanctions, findByCreatedDateBetween, findByUserEmail, countActiveSanctions, countExpiredSanctions, countPendingSanctions, countTotalSanctions

### other (9)

- `PartnerUserSanctionSpecifications` repository/PartnerUserSanctionSpecifications.java · public: withSearchCriteria, hasUserId, hasStatus, hasType, createdBetween, startDateBetween, endDateBetween, userEmailLike
- `PartnerPointExpirationScheduler` scheduler/PartnerPointExpirationScheduler.java · public: checkExpiredPoints · 주입: PartnerPointCommandService
- `PartnerOAuthAttributes` security/oauth2/PartnerOAuthAttributes.java · public: of
- `PartnerPasswordUtils` utils/PartnerPasswordUtils.java · public: generateRandomPassword
- `PartnerBankAccountControllerValidator` validate/PartnerBankAccountControllerValidator.java · public: validateBankAccountDto
- `PartnerBlockedProfileControllerValidator` validate/PartnerBlockedProfileControllerValidator.java
- `PartnerFollowControllerValidator` validate/PartnerFollowControllerValidator.java
- `PartnerProfileControllerValidator` validate/PartnerProfileControllerValidator.java
- `PartnerUserControllerValidator` validate/PartnerUserControllerValidator.java · public: validateUserRegistrationDto, validateLoginRequestDto, validateEmailFindRequestDto, validatePasswordResetEligibilityDto, validatePasswordResetDto, validateLoginInfoUpdateDto

### 다른 도메인 참조 (import·주입 기준)

- **notification**: `NotificationCategory` ← PartnerFollowCommandService; `NotificationType` ← PartnerFollowCommandService
- **partnernotification**: `PartnerNotificationCommandService` ← PartnerFollowCommandService, PartnerUserCommandService
- **user**: `AuthRedisService` ← PartnerAdminAuthController, PartnerOAuthController, PartnerUserCommandController, PartnerUserQueryController, PartnerUserUpdateService; `AuthService` ← PartnerAdminAuthController, PartnerUserQueryController; `BankAccount` ← PartnerBankAccountCommandService, PartnerBankAccountQueryService; `BankAccountRepository` ← PartnerAdminUserService, PartnerBankAccountQueryService; `BlockedProfile` ← PartnerBlockedProfileCommandService, PartnerBlockedProfileQueryService, PartnerBlockedProfileRepository; `Follow` ← PartnerFollowCommandService, PartnerFollowQueryService, PartnerFollowRepository; `Gender` ← PartnerAdminAuthController, PartnerUserCommandService; `Point` ← PartnerAdminPointService, PartnerPointCommandService, PartnerPointQueryService, PartnerPointRepository; `PointStatus` ← PartnerAdminPointService, PartnerPointCommandService, PartnerPointQueryService, PartnerPointRepository; `Profile` ← PartnerBlockedProfileCommandService, PartnerBlockedProfileQueryService, PartnerBlockedProfileRepository, PartnerFollowCommandService, PartnerFollowQueryService, PartnerFollowRepository, PartnerProfileQueryService, PartnerProfileService; `ProfileCommandService` ← PartnerProfileController, PartnerUserCommandService; `ProfileRepository` ← PartnerAdminUserService, PartnerBlockedProfileCommandService, PartnerFollowCommandService, PartnerFollowQueryService, PartnerProfileQueryService; `Role` ← PartnerAdminAuthController, PartnerUserQueryService; `SanctionStatus` ← PartnerAdminSanctionController, PartnerUserSanctionRepository, PartnerUserSanctionSpecifications; `SanctionType` ← PartnerAdminSanctionController, PartnerUserSanctionRepository, PartnerUserSanctionSpecifications; `ShoeSize` ← PartnerUserUpdateService; `User` ← PartnerAdminAuthController, PartnerAdminGradeService, PartnerAdminPointService, PartnerBankAccountCommandService, PartnerCustomUserDetailsService, PartnerOAuthController, PartnerPasswordResetService, PartnerPointCommandService, PartnerPointQueryService, PartnerPointRepository, PartnerProfileService, PartnerUserCommandController, PartnerUserCommandService, PartnerUserQueryController, PartnerUserQueryService, PartnerUserSanctionRepository, PartnerUserSanctionSpecifications, PartnerUserUpdateService; `UserGrade` ← PartnerAdminGradeService, PartnerUserGradeRepository; `UserRepository` ← PartnerAdminGradeService, PartnerAdminPointService, PartnerAdminSanctionService, PartnerAdminUserService, PartnerBankAccountCommandService, PartnerCustomUserDetailsService, PartnerOAuthController, PartnerPasswordResetService, PartnerUserCommandService, PartnerUserQueryService, PartnerUserUpdateService; `UserSanction` ← PartnerUserSanctionRepository, PartnerUserSanctionSpecifications

### 이 도메인을 참조하는 다른 도메인

- **partnerchatquestion**: PartnerUserQueryService
- **partnerevent**: PartnerUserQueryService
- **partnerfaq**: PartnerUserQueryService
- **partnerinquiry**: PartnerUserQueryService
- **partnerinspection**: PartnerUserQueryService
- **partnernotice**: PartnerUserQueryService
- **partnerorder**: PartnerUserQueryService
- **partnerpayment**: PartnerUserQueryService
- **partnerproduct**: PartnerUserQueryService
- **partnersale**: PartnerBankAccountQueryService, PartnerUserQueryService
- **partnerstyle**: PartnerProfileQueryService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

