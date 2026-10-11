# settlementuser 도메인

기준 경로: `src/main/java/com/fream/back/domain/settlementuser/` · 파일 53개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/settlement/bank-account` | `SettlementBankAccountController#createOrUpdateBankAccount` | `this#extractEmailFromSecurityContext`, `SettlementBankAccountCommandService#createOrUpdateBankAccount` |
| GET | `/settlement/bank-account` | `SettlementBankAccountController#getBankAccount` | `this#extractEmailFromSecurityContext`, `SettlementBankAccountQueryService#getBankAccount` |
| DELETE | `/settlement/bank-account` | `SettlementBankAccountController#deleteBankAccount` | `this#extractEmailFromSecurityContext`, `SettlementBankAccountCommandService#deleteBankAccount` |
| POST | `/settlement/profiles/blocked` | `SettlementBlockedProfileController#blockProfile` | `this#extractEmailFromSecurityContext`, `SettlementBlockedProfileCommandService#blockProfile` |
| DELETE | `/settlement/profiles/blocked` | `SettlementBlockedProfileController#unblockProfile` | `this#extractEmailFromSecurityContext`, `SettlementBlockedProfileCommandService#unblockProfile` |
| GET | `/settlement/profiles/blocked` | `SettlementBlockedProfileController#getBlockedProfiles` | `this#extractEmailFromSecurityContext`, `SettlementBlockedProfileQueryService#getBlockedProfiles` |
| POST | `/settlement/follows/{profileId}` | `SettlementFollowController#createFollow` | `this#extractEmailFromSecurityContext`, `SettlementFollowCommandService#createFollow` |
| DELETE | `/settlement/follows/{profileId}` | `SettlementFollowController#deleteFollow` | `this#extractEmailFromSecurityContext`, `SettlementFollowCommandService#deleteFollow` |
| GET | `/settlement/follows/followers` | `SettlementFollowController#getFollowers` | `this#extractEmailFromSecurityContext`, `SettlementFollowQueryService#getFollowers` |
| GET | `/settlement/follows/followings` | `SettlementFollowController#getFollowings` | `this#extractEmailFromSecurityContext`, `SettlementFollowQueryService#getFollowings` |
| GET | `/settlement/profiles` | `SettlementProfileController#getProfile` | `this#extractEmailFromSecurityContext`, `SettlementProfileQueryService#getProfileInfo` |
| PUT | `/settlement/profiles` | `SettlementProfileController#updateProfile` | `this#extractEmailFromSecurityContext`, `ProfileCommandService#updateProfile` |
| GET | `/settlement/profiles/{profileId}/image` | `SettlementProfileController#getProfileImage` | `SettlementProfileQueryService#getProfileImageFileName` |
| GET | `/settlement/admin/users/grades` | `SettlementAdminGradeController#getAllGrades` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementAdminGradeService#getAllGrades` |
| GET | `/settlement/admin/users/grades/{gradeId}` | `SettlementAdminGradeController#getGradeById` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementAdminGradeService#getGradeById` |
| POST | `/settlement/admin/users/grades` | `SettlementAdminGradeController#createGrade` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementAdminGradeService#createGrade` |
| PUT | `/settlement/admin/users/grades/{gradeId}` | `SettlementAdminGradeController#updateGrade` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementAdminGradeService#updateGrade` |
| DELETE | `/settlement/admin/users/grades/{gradeId}` | `SettlementAdminGradeController#deleteGrade` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementAdminGradeService#deleteGrade` |
| GET | `/settlement/admin/users/grades/counts` | `SettlementAdminGradeController#getGradeUserCounts` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementAdminGradeService#getGradeUserCounts` |
| GET | `/settlement/admin/users/grades/statistics` | `SettlementAdminGradeController#getGradeStatistics` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementAdminGradeService#getGradeStatistics` |
| POST | `/settlement/admin/users/grades/auto-assign` | `SettlementAdminGradeController#runGradeAutoAssignment` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementAdminGradeService#runGradeAutoAssignment` |
| GET | `/settlement/admin/users/points/user/{userId}` | `SettlementAdminPointController#getUserPointHistory` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementAdminPointService#getUserPointHistory` |
| GET | `/settlement/admin/users/points/user/{userId}/available` | `SettlementAdminPointController#getUserAvailablePoints` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementAdminPointService#getUserAvailablePoints` |
| GET | `/settlement/admin/users/points/user/{userId}/summary` | `SettlementAdminPointController#getUserPointSummary` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementAdminPointService#getUserPointSummary` |
| GET | `/settlement/admin/users/points/{pointId}` | `SettlementAdminPointController#getPointDetail` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementAdminPointService#getPointDetail` |
| POST | `/settlement/admin/users/points/user/{userId}/add` | `SettlementAdminPointController#addPointByAdmin` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementAdminPointService#addPointByAdmin` |
| POST | `/settlement/admin/users/points/user/{userId}/deduct` | `SettlementAdminPointController#deductPointByAdmin` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementAdminPointService#deductPointByAdmin` |
| GET | `/settlement/admin/users/points/statistics` | `SettlementAdminPointController#getPointStatistics` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementAdminPointService#getPointStatistics` |
| GET | `/settlement/admin/users/sanctions/search` | `SettlementAdminSanctionController#searchSanctions` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementAdminSanctionService#searchSanctions` |
| GET | `/settlement/admin/users/sanctions/{sanctionId}` | `SettlementAdminSanctionController#getSanctionById` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementAdminSanctionService#getSanctionById` |
| POST | `/settlement/admin/users/sanctions` | `SettlementAdminSanctionController#createSanction` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementAdminSanctionService#createSanction` |
| PUT | `/settlement/admin/users/sanctions/{sanctionId}` | `SettlementAdminSanctionController#updateSanction` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementAdminSanctionService#updateSanction` |
| PATCH | `/settlement/admin/users/sanctions/{sanctionId}/review` | `SettlementAdminSanctionController#reviewSanction` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementAdminSanctionService#reviewSanction` |
| PATCH | `/settlement/admin/users/sanctions/{sanctionId}/cancel` | `SettlementAdminSanctionController#cancelSanction` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementAdminSanctionService#cancelSanction` |
| GET | `/settlement/admin/users/sanctions/user/{userId}` | `SettlementAdminSanctionController#getUserSanctions` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementAdminSanctionService#getUserSanctions` |
| GET | `/settlement/admin/users/sanctions/statistics` | `SettlementAdminSanctionController#getSanctionStatistics` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementAdminSanctionService#getSanctionStatistics` |
| GET | `/settlement/admin/users/search` | `SettlementAdminUserController#searchUsers` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementAdminUserService#searchUsers` |
| GET | `/settlement/admin/users/{userId}` | `SettlementAdminUserController#getUserDetail` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementAdminUserService#getUserDetail` |
| PATCH | `/settlement/admin/users/{userId}/status` | `SettlementAdminUserController#updateUserStatus` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementAdminUserService#updateUserStatus` |
| PATCH | `/settlement/admin/users/{userId}/grade` | `SettlementAdminUserController#updateUserGrade` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementAdminUserService#updateUserGrade` |
| PATCH | `/settlement/admin/users/{userId}/role` | `SettlementAdminUserController#updateUserRole` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementAdminUserService#updateUserRole` |
| POST | `/settlement/admin/users/{userId}/points` | `SettlementAdminUserController#manageUserPoints` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementAdminUserService#manageUserPoints` |
| GET | `/settlement/admin/users/export` | `SettlementAdminUserController#exportUserData` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementAdminUserService#exportUsersCsv` |
| POST | `/settlement/admin/auth/login` | `SettlementAdminAuthController#adminLogin` | `this#getClientIp`, `AuthService#login`, `JwtTokenProvider#getEmailFromToken`, `SettlementUserQueryService#checkAdminRole`, `this#setCookie` |
| POST | `/settlement/admin/auth/find-email` | `SettlementAdminAuthController#findAdminEmail` | `SettlementUserQueryService#findEmailByPhoneNumber`, `SettlementUserQueryService#checkAdminRole` |
| GET | `/settlement/admin/auth/check` | `SettlementAdminAuthController#checkAdminToken` | `this#getCookieValue`, `JwtTokenProvider#validateToken`, `JwtTokenProvider#getEmailFromToken`, `SettlementUserQueryService#checkAdminRole` |
| POST | `/settlement/admin/auth/refresh` | `SettlementAdminAuthController#refreshAdminToken` | `this#getCookieValue`, `JwtTokenProvider#validateToken`, `AuthRedisService#isRefreshTokenValid`, `JwtTokenProvider#getEmailFromToken`, `SettlementUserQueryService#findByEmail`, `SettlementUserQueryService#checkAdminRole`, `this#getClientIp`, `JwtTokenProvider#generateTokenPair`, `this#setCookie` |
| POST | `/settlement/admin/auth/find-password` | `SettlementAdminAuthController#findAdminPassword` | `SettlementUserQueryService#findByEmail`, `SettlementPasswordResetService#AdminCheckPasswordResetAndSendEmail` |
| POST | `/settlement/admin/auth/logout` | `SettlementAdminAuthController#logout` | `this#getCookieValue`, `JwtTokenProvider#validateToken`, `JwtTokenProvider#getEmailFromToken`, `SettlementUserQueryService#checkAdminRole`, `AuthRedisService#removeAccessToken`, `AuthRedisService#removeRefreshToken`, `this#expireCookie` |
| POST | `/settlement/auth/login` | `SettlementAuthController#login` |  |
| POST | `/settlement/auth/logout` | `SettlementAuthController#logout` |  |
| POST | `/settlement/auth/refresh` | `SettlementAuthController#refresh` |  |
| GET | `/settlement/auth/email` | `SettlementAuthController#getUserEmail` |  |
| GET | `/settlement/auth/status` | `SettlementAuthController#checkAuthStatus` |  |
| POST | `/settlement/identity-verification/verify` | `SettlementIdentityVerificationController#verifyIdentity` | `SettlementIdentityVerificationService#verifyIdentity` |
| GET | `/oauth2/authorization/{registrationId}` | `SettlementOAuth2AuthorizationController#redirectToOAuthProvider` |  |
| POST | `/settlement/oauth/complete-signup` | `SettlementOAuthController#completeOAuthSignup` | `this#extractEmailFromToken`, `UserRepository#findByEmail`, `UserRepository#findByReferralCode`, `UserRepository#save~`, `this#getClientIp`, `JwtTokenProvider#generateTokenPair`, `this#setCookie` |
| GET | `/settlement/oauth/status` | `SettlementOAuthController#checkAuthStatus` | `this#getCookieValue`, `JwtTokenProvider#validateToken`, `AuthRedisService#isAccessTokenValid`, `JwtTokenProvider#getEmailFromToken` |
| GET | `/settlement/oauth/connections` | `SettlementOAuthController#getOAuthConnections` | `this#getCookieValue`, `JwtTokenProvider#getEmailFromToken`, `UserRepository#findByEmail` |
| POST | `/settlement/users/register` | `SettlementUserCommandController#registerUser` | `SettlementUserCommandService#registerUser` |
| POST | `/settlement/users/verify-identity` | `SettlementUserCommandController#verifyIdentity` | `SettlementIdentityVerificationService#verifyIdentity` |
| PUT | `/settlement/users/update` | `SettlementUserCommandController#updateUserInfo` | `SettlementUserUpdateService#updateLoginInfo`, `SettlementUserQueryService#getLoginInfo`, `this#getCookieValue`, `AuthRedisService#removeAccessToken`, `AuthRedisService#removeRefreshToken`, `SettlementUserUpdateService#reissueTokenAfterEmailChange`, `this#setCookie` |
| POST | `/settlement/points/commands` | `SettlementPointCommandController#addPoint` | `SettlementPointCommandService#addPoint` |
| POST | `/settlement/points/commands/use` | `SettlementPointCommandController#usePoint` | `SettlementPointCommandService#usePoint` |
| GET | `/settlement/points/queries` | `SettlementPointQueryController#getAllPointHistory` | `SettlementPointQueryService#getAllPointHistory` |
| GET | `/settlement/points/queries/available` | `SettlementPointQueryController#getAvailablePoints` | `SettlementPointQueryService#getAvailablePoints` |
| GET | `/settlement/points/queries/summary` | `SettlementPointQueryController#getPointSummary` | `SettlementPointQueryService#getPointSummary` |
| GET | `/settlement/points/queries/{pointId}` | `SettlementPointQueryController#getPointDetail` | `SettlementPointQueryService#getPointDetail` |
| POST | `/settlement/users/find-email` | `SettlementUserQueryController#findEmail` | `SettlementUserQueryService#findEmailByPhoneNumber` |
| POST | `/settlement/users/reset-password` | `SettlementUserQueryController#resetPasswordEligibility` | `SettlementPasswordResetService#checkPasswordResetEligibility` |
| POST | `/settlement/users/reset-password-sandEmail` | `SettlementUserQueryController#resetPasswordToEmail` | `SettlementPasswordResetService#checkPasswordResetAndSendEmail` |
| POST | `/settlement/users/reset` | `SettlementUserQueryController#resetPassword` | `SettlementPasswordResetService#resetPassword` |
| GET | `/settlement/users/login-info` | `SettlementUserQueryController#getLoginInfo` | `SettlementUserQueryService#getLoginInfo` |
| DELETE | `/settlement/users/delete-account` | `SettlementUserQueryController#deleteAccount` | `SettlementUserCommandService#deleteAccount` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `SettlementAdminGradeService`
  - `getAllGrades` → SettlementUserGradeRepository#findAllByOrderByLevelAsc
  - `getGradeById` → SettlementUserGradeRepository#findById~, this#convertToGradeResponseDto
  - `createGrade` → SettlementUserGradeRepository#findByLevel, SettlementUserGradeRepository#findByName, SettlementUserGradeRepository#save~, this#convertToGradeResponseDto
  - `updateGrade` → SettlementUserGradeRepository#findById~, SettlementUserGradeRepository#findByName, this#convertToGradeResponseDto
  - `deleteGrade` → SettlementUserGradeRepository#findById~, SettlementUserGradeRepository#countUsersByGradeId, SettlementUserGradeRepository#delete~
  - `getGradeUserCounts` → SettlementUserGradeRepository#countUsersGroupByGrade, SettlementUserGradeRepository#findById~
  - `getGradeStatistics` → SettlementUserGradeRepository#findAllByOrderByLevelAsc, SettlementUserGradeRepository#countUsersByGradeId
  - `runGradeAutoAssignment` → UserRepository#findAll~, SettlementUserGradeRepository#findGradeByPurchaseAmount
  - `convertToGradeResponseDto` → SettlementUserGradeRepository#countUsersByGradeId
- `SettlementAdminPointService`
  - `getUserPointHistory` → this#getUserById, SettlementPointRepository#findByUserOrderByCreatedDateDesc
  - `getUserAvailablePoints` → this#getUserById, SettlementPointRepository#findByUserAndStatusOrderByExpirationDateAsc
  - `getUserPointSummary` → this#getUserById, SettlementPointRepository#findByUserAndStatusOrderByExpirationDateAsc
  - `getPointDetail` → SettlementPointRepository#findById~
  - `addPointByAdmin` → this#getUserById, SettlementPointRepository#save~
  - `deductPointByAdmin` → this#getUserById, SettlementPointRepository#findByUserAndStatusOrderByExpirationDateAsc, SettlementPointRepository#save~
  - `getUserById` → UserRepository#findById~
- `SettlementAdminSanctionService`
  - `searchSanctions` → SettlementUserSanctionRepository#findAll~
  - `getSanctionById` → SettlementUserSanctionRepository#findById~, this#convertToSanctionResponseDto
  - `createSanction` → UserRepository#findById~, SettlementUserSanctionRepository#save~, this#convertToSanctionResponseDto
  - `updateSanction` → SettlementUserSanctionRepository#findById~, this#convertToSanctionResponseDto
  - `reviewSanction` → SettlementUserSanctionRepository#findById~, this#convertToSanctionResponseDto
  - `cancelSanction` → SettlementUserSanctionRepository#findById~, this#hasOtherActiveBanSanctions, this#convertToSanctionResponseDto
  - `getUserSanctions` → UserRepository#findById~, SettlementUserSanctionRepository#findByUserOrderByCreatedDateDesc
  - `getSanctionStatistics` → SettlementUserSanctionRepository#countTotalSanctions, SettlementUserSanctionRepository#countActiveSanctions, SettlementUserSanctionRepository#countExpiredSanctions, SettlementUserSanctionRepository#countPendingSanctions
  - `processExpiredSanctions` → SettlementUserSanctionRepository#findExpirableSanctions, this#hasOtherActiveBanSanctions
  - `hasOtherActiveBanSanctions` → SettlementUserSanctionRepository#findByUserAndStatusOrderByStartDateDesc
- `SettlementAdminUserService`
  - `searchUsers` → UserRepository#findAll~
  - `getUserDetail` → UserRepository#findById~, this#convertToUserDetailResponseDto
  - `updateUserStatus` → UserRepository#findById~, SettlementUserSanctionRepository#save~, this#convertToUserDetailResponseDto
  - `updateUserGrade` → UserRepository#findById~, SettlementUserGradeRepository#findById~, this#convertToUserDetailResponseDto
  - `updateUserRole` → UserRepository#findById~, this#convertToUserDetailResponseDto
  - `manageUserPoints` → UserRepository#findById~, SettlementPointRepository#save~, SettlementPointRepository#findByUserAndStatusOrderByExpirationDateAsc
  - `exportUsersCsv` → UserRepository#findAll~
  - `convertToUserSearchResponseDto` → ProfileRepository#findByUser
  - `convertToUserDetailResponseDto` → ProfileRepository#findByUser, BankAccountRepository#findByUser_Email, SettlementUserSanctionRepository#findByUserAndStatusOrderByStartDateDesc, this#convertToProfileDto, this#convertToBankAccountDto, this#convertToSanctionBriefDtoList
- `SettlementBankAccountCommandService`
  - `createOrUpdateBankAccount` → UserRepository#findByEmail
  - `deleteBankAccount` → UserRepository#findByEmail
- `SettlementBankAccountQueryService`
  - `getBankAccount` → BankAccountRepository#findByUser_Email
- `SettlementBlockedProfileCommandService`
  - `blockProfile` → ProfileRepository#findByUserEmailWithFetchJoin, ProfileRepository#findById~, SettlementBlockedProfileRepository#findByProfileAndBlockedProfile, SettlementBlockedProfileRepository#save~
  - `unblockProfile` → ProfileRepository#findByUserEmailWithFetchJoin, ProfileRepository#findById~, SettlementBlockedProfileRepository#findByProfileAndBlockedProfile, SettlementBlockedProfileRepository#delete~
- `SettlementBlockedProfileQueryService`
  - `getBlockedProfiles` → SettlementBlockedProfileRepository#findAllByProfileWithBlocked, SettlementBlockedProfileRepository#findAllByProfileEmailWithBlocked
- `SettlementCustomUserDetailsService`
  - `loadUserByUsername` → UserRepository#findByEmail
- `SettlementFollowCommandService`
  - `createFollow` → ProfileRepository#findByUserEmailWithFetchJoin, ProfileRepository#findById~, SettlementFollowRepository#findByFollowerAndFollowing, SettlementFollowRepository#save~, SettlementNotificationCommandService#createNotification
  - `deleteFollow` → ProfileRepository#findByUserEmailWithFetchJoin, ProfileRepository#findById~, SettlementFollowRepository#findByFollowerAndFollowing, SettlementFollowRepository#delete~
- `SettlementFollowQueryService`
  - `getFollowers` → ProfileRepository#findByUserEmailWithFetchJoin, SettlementFollowRepository#findFollowersByProfileId
  - `getFollowings` → ProfileRepository#findByUserEmailWithFetchJoin, SettlementFollowRepository#findFollowingsByProfileId
- `SettlementOAuthAttributes`
  - `of` → this#ofGoogle, this#ofNaver, this#ofKakao
- `SettlementPasswordResetService`
  - `checkPasswordResetEligibility` → UserRepository#findByEmailAndPhoneNumber
  - `resetPassword` → UserRepository#findByEmailAndPhoneNumber
  - `checkPasswordResetAndSendEmail` → UserRepository#findByEmailAndPhoneNumber, SettlementEmailService#sendEmail
  - `AdminCheckPasswordResetAndSendEmail` → UserRepository#findByEmailAndPhoneNumber, SettlementEmailService#sendEmail
- `SettlementPasswordUtils`
  - `generateRandomPassword` → this#isValidPassword
- `SettlementPointCommandService`
  - `addPoint` → this#validateAddPointRequest, SettlementUserQueryService#findByEmail, SettlementPointRepository#save~
  - `usePoint` → this#validateUsePointRequest, SettlementUserQueryService#findByEmail, SettlementPointRepository#getTotalAvailablePoints, SettlementPointRepository#findByUserAndStatusOrderByExpirationDateAsc, SettlementPointRepository#save~
  - `expirePoints` → SettlementPointRepository#findByStatusAndExpirationDateBefore
- `SettlementPointExpirationScheduler`
  - `checkExpiredPoints` → SettlementPointCommandService#expirePoints
- `SettlementPointQueryService`
  - `getAllPointHistory` → SettlementUserQueryService#findByEmail, SettlementPointRepository#findByUserOrderByCreatedDateDesc
  - `getAvailablePoints` → SettlementUserQueryService#findByEmail, SettlementPointRepository#findByUserAndStatusOrderByExpirationDateAsc
  - `getPointSummary` → SettlementUserQueryService#findByEmail, SettlementPointRepository#getTotalAvailablePoints, this#getAllPointHistory, SettlementPointRepository#findExpiringPoints
  - `getPointDetail` → SettlementUserQueryService#findByEmail, SettlementPointRepository#findById~
- `SettlementProfileQueryService`
  - `getProfileInfo` → ProfileRepository#findByUserEmailWithFetchJoin, SettlementBlockedProfileQueryService#getBlockedProfiles
  - `getProfileByEmail` → ProfileRepository#findByUserEmailWithFetchJoin
  - `getProfileImageFileName` → ProfileRepository#findById~
- `SettlementProfileService`
  - `createDefaultProfile` → this#generateRandomProfileName
- `SettlementUserCommandService`
  - `registerUser` → UserRepository#existsByEmail, SettlementIdentityVerificationService#verifyIdentity, UserRepository#findByCi, this#validateAge, UserRepository#findByReferralCode, this#convertToGender, this#calculateAge, this#generateUniqueReferralCode, UserRepository#save~, ProfileCommandService#createDefaultProfile
  - `deleteAccount` → UserRepository#findByEmail, SettlementNotificationCommandService#deleteNotificationsByUser, FileUtils#deleteFile, UserRepository#delete~
  - `generateUniqueReferralCode` → UserRepository#findByReferralCode
- `SettlementUserControllerValidator`
  - `validateUserRegistrationDto` → this#isValidPassword
  - `validatePasswordResetDto` → this#isValidPassword
  - `validateLoginInfoUpdateDto` → this#isValidPassword
- `SettlementUserQueryService`
  - `checkAdminRole` → UserRepository#findByEmail
  - `findEmailByPhoneNumber` → UserRepository#findByPhoneNumber
  - `getLoginInfo` → UserRepository#findByEmail
  - `findByEmail` → UserRepository#findByEmail
- `SettlementUserUpdateService`
  - `updateLoginInfo` → UserRepository#findByEmail, UserRepository#existsByEmail
  - `reissueTokenAfterEmailChange` → UserRepository#findByEmail, AuthRedisService#removeAccessToken, AuthRedisService#removeRefreshToken, JwtTokenProvider#generateTokenPair

## 3. 클래스 목록 (역할별)

### controller (17)

- `SettlementBankAccountController` controller/SettlementBankAccountController.java · public: createOrUpdateBankAccount, getBankAccount, deleteBankAccount · 주입: SettlementBankAccountCommandService, SettlementBankAccountQueryService
- `SettlementBlockedProfileController` controller/SettlementBlockedProfileController.java · public: blockProfile, unblockProfile, getBlockedProfiles · 주입: SettlementBlockedProfileCommandService, SettlementBlockedProfileQueryService
- `SettlementFollowController` controller/SettlementFollowController.java · public: createFollow, deleteFollow, getFollowers, getFollowings · 주입: SettlementFollowCommandService, SettlementFollowQueryService
- `SettlementProfileController` controller/SettlementProfileController.java · public: getProfile, updateProfile, getProfileImage · 주입: SettlementProfileQueryService, ProfileCommandService, FileUtils
- `SettlementAdminGradeController` controller/admin/SettlementAdminGradeController.java · public: getAllGrades, getGradeById, createGrade, updateGrade, deleteGrade, getGradeUserCounts, getGradeStatistics, runGradeAutoAssignment · 주입: SettlementAdminGradeService, SettlementUserQueryService
- `SettlementAdminPointController` controller/admin/SettlementAdminPointController.java · public: getUserPointHistory, getUserAvailablePoints, getUserPointSummary, getPointDetail, addPointByAdmin, deductPointByAdmin, getPointStatistics · 주입: SettlementAdminPointService, SettlementUserQueryService
- `SettlementAdminSanctionController` controller/admin/SettlementAdminSanctionController.java · public: searchSanctions, getSanctionById, createSanction, updateSanction, reviewSanction, cancelSanction, getUserSanctions, getSanctionStatistics · 주입: SettlementAdminSanctionService, SettlementUserQueryService
- `SettlementAdminUserController` controller/admin/SettlementAdminUserController.java · public: searchUsers, getUserDetail, updateUserStatus, updateUserGrade, updateUserRole, manageUserPoints, exportUserData · 주입: SettlementAdminUserService, SettlementUserQueryService
- `SettlementAdminAuthController` controller/command/SettlementAdminAuthController.java · public: adminLogin, findAdminEmail, checkAdminToken, refreshAdminToken, findAdminPassword, logout · 주입: AuthService, SettlementUserQueryService, JwtTokenProvider, SettlementPasswordResetService, AuthRedisService
- `SettlementAuthController` controller/command/SettlementAuthController.java · public: login, logout, refresh, getUserEmail, checkAuthStatus
- `SettlementIdentityVerificationController` controller/command/SettlementIdentityVerificationController.java · public: verifyIdentity · 주입: SettlementIdentityVerificationService
- `SettlementOAuth2AuthorizationController` controller/command/SettlementOAuth2AuthorizationController.java · public: redirectToOAuthProvider
- `SettlementOAuthController` controller/command/SettlementOAuthController.java · public: completeOAuthSignup, checkAuthStatus, getOAuthConnections · 주입: UserRepository, SettlementUserUpdateService, JwtTokenProvider, AuthRedisService
- `SettlementUserCommandController` controller/command/SettlementUserCommandController.java · public: registerUser, verifyIdentity, updateUserInfo · 주입: SettlementUserCommandService, SettlementUserUpdateService, SettlementUserQueryService, AuthRedisService, SettlementIdentityVerificationService
- `SettlementPointCommandController` controller/point/SettlementPointCommandController.java · public: addPoint, usePoint · 주입: SettlementPointCommandService
- `SettlementPointQueryController` controller/point/SettlementPointQueryController.java · public: getAllPointHistory, getAvailablePoints, getPointSummary, getPointDetail · 주입: SettlementPointQueryService
- `SettlementUserQueryController` controller/query/SettlementUserQueryController.java · public: findEmail, resetPasswordEligibility, resetPasswordToEmail, resetPassword, getLoginInfo, deleteAccount · 주입: AuthService, SettlementUserQueryService, SettlementPasswordResetService, SettlementUserUpdateService, JwtTokenProvider, SettlementUserCommandService, AuthRedisService

### service-command (7)

- `SettlementEmailService` service/command/SettlementEmailService.java · public: sendEmail · 주입: JavaMailSender
- `SettlementIdentityVerificationService` service/command/SettlementIdentityVerificationService.java · public: verifyIdentity · 주입: RestTemplate, ObjectMapper
- `SettlementPasswordResetService` service/command/SettlementPasswordResetService.java · public: checkPasswordResetEligibility, resetPassword, checkPasswordResetAndSendEmail, AdminCheckPasswordResetAndSendEmail · 주입: UserRepository, PasswordEncoder, SettlementEmailService
- `SettlementProfileService` service/command/SettlementProfileService.java · public: createDefaultProfile
- `SettlementUserCommandService` service/command/SettlementUserCommandService.java · public: registerUser, deleteAccount · 주입: UserRepository, PasswordEncoder, ProfileCommandService, FileUtils, SettlementNotificationCommandService, SettlementIdentityVerificationService
- `SettlementUserSanctionService` service/command/SettlementUserSanctionService.java
- `SettlementUserUpdateService` service/command/SettlementUserUpdateService.java · public: updateLoginInfo, reissueTokenAfterEmailChange · 주입: UserRepository, PasswordEncoder, AuthRedisService, JwtTokenProvider

### service-query (1)

- `SettlementUserQueryService` service/query/SettlementUserQueryService.java · public: checkAdminRole, findEmailByPhoneNumber, getLoginInfo, findByEmail · 주입: UserRepository

### service (14)

- `SettlementCustomUserDetailsService` security/SettlementCustomUserDetailsService.java · extends/implements UserDetailsService · public: loadUserByUsername · 주입: UserRepository, PasswordEncoder
- `SettlementBlockedProfileCommandService` service/BlockProfile/SettlementBlockedProfileCommandService.java · public: blockProfile, unblockProfile · 주입: ProfileRepository, SettlementBlockedProfileRepository
- `SettlementBlockedProfileQueryService` service/BlockProfile/SettlementBlockedProfileQueryService.java · public: getBlockedProfiles · 주입: SettlementBlockedProfileRepository
- `SettlementAdminGradeService` service/admin/SettlementAdminGradeService.java · public: getAllGrades, getGradeById, createGrade, updateGrade, deleteGrade, getGradeUserCounts, getGradeStatistics, runGradeAutoAssignment · 주입: SettlementUserGradeRepository, UserRepository
- `SettlementAdminPointService` service/admin/SettlementAdminPointService.java · public: getUserPointHistory, getUserAvailablePoints, getUserPointSummary, getPointDetail, addPointByAdmin, deductPointByAdmin, getPointStatistics · 주입: UserRepository, SettlementPointRepository
- `SettlementAdminSanctionService` service/admin/SettlementAdminSanctionService.java · public: searchSanctions, getSanctionById, createSanction, updateSanction, reviewSanction, cancelSanction, getUserSanctions, getSanctionStatistics, processExpiredSanctions · 주입: SettlementUserSanctionRepository, UserRepository
- `SettlementAdminUserService` service/admin/SettlementAdminUserService.java · public: searchUsers, getUserDetail, updateUserStatus, updateUserGrade, updateUserRole, manageUserPoints, exportUsersCsv · 주입: UserRepository, ProfileRepository, SettlementPointRepository, SettlementUserGradeRepository, SettlementUserSanctionRepository, BankAccountRepository
- `SettlementBankAccountCommandService` service/bankaccount/SettlementBankAccountCommandService.java · public: createOrUpdateBankAccount, deleteBankAccount · 주입: UserRepository
- `SettlementBankAccountQueryService` service/bankaccount/SettlementBankAccountQueryService.java · public: getBankAccount · 주입: BankAccountRepository
- `SettlementFollowCommandService` service/follow/SettlementFollowCommandService.java · public: createFollow, deleteFollow · 주입: SettlementFollowRepository, ProfileRepository, SettlementNotificationCommandService
- `SettlementFollowQueryService` service/follow/SettlementFollowQueryService.java · public: getFollowers, getFollowings · 주입: SettlementFollowRepository, ProfileRepository
- `SettlementPointCommandService` service/point/SettlementPointCommandService.java · public: addPoint, usePoint, expirePoints · 주입: SettlementPointRepository, SettlementUserQueryService
- `SettlementPointQueryService` service/point/SettlementPointQueryService.java · public: getAllPointHistory, getAvailablePoints, getPointSummary, getPointDetail · 주입: SettlementPointRepository, SettlementUserQueryService
- `SettlementProfileQueryService` service/profile/SettlementProfileQueryService.java · public: getProfileInfo, getProfileByEmail, getProfileImageFileName · 주입: ProfileRepository, SettlementBlockedProfileQueryService

### repository (5)

- `SettlementBlockedProfileRepository` (interface) repository/SettlementBlockedProfileRepository.java · extends/implements JpaRepository · public: findByProfile, findByProfileAndBlockedProfileId, findBlockedProfileIdsByProfile, findAllByProfileWithBlocked, findByProfileAndBlockedProfile, findAllByProfileEmailWithBlocked
- `SettlementFollowRepository` (interface) repository/SettlementFollowRepository.java · extends/implements JpaRepository · public: findByFollowerAndFollowing, findFollowersByProfileId, findFollowingsByProfileId
- `SettlementPointRepository` (interface) repository/SettlementPointRepository.java · extends/implements JpaRepository · public: findByUserOrderByCreatedDateDesc, findByUserAndStatusOrderByExpirationDateAsc, findByStatusAndExpirationDateBefore, getTotalAvailablePoints, findExpiringPoints
- `SettlementUserGradeRepository` (interface) repository/SettlementUserGradeRepository.java · extends/implements JpaRepository · public: findByLevel, findByName, findAllByOrderByLevelAsc, findGradeByPurchaseAmount, countUsersByGradeId, countUsersGroupByGrade
- `SettlementUserSanctionRepository` (interface) repository/SettlementUserSanctionRepository.java · extends/implements JpaRepository, JpaSpecificationExecutor · public: findByUserOrderByCreatedDateDesc, findByUserAndStatusOrderByStartDateDesc, findByStatus, findByType, findExpirableSanctions, findByCreatedDateBetween, findByUserEmail, countActiveSanctions, countExpiredSanctions, countPendingSanctions, countTotalSanctions

### other (9)

- `SettlementUserSanctionSpecifications` repository/SettlementUserSanctionSpecifications.java · public: withSearchCriteria, hasUserId, hasStatus, hasType, createdBetween, startDateBetween, endDateBetween, userEmailLike
- `SettlementPointExpirationScheduler` scheduler/SettlementPointExpirationScheduler.java · public: checkExpiredPoints · 주입: SettlementPointCommandService
- `SettlementOAuthAttributes` security/oauth2/SettlementOAuthAttributes.java · public: of
- `SettlementPasswordUtils` utils/SettlementPasswordUtils.java · public: generateRandomPassword
- `SettlementBankAccountControllerValidator` validate/SettlementBankAccountControllerValidator.java · public: validateBankAccountDto
- `SettlementBlockedProfileControllerValidator` validate/SettlementBlockedProfileControllerValidator.java
- `SettlementFollowControllerValidator` validate/SettlementFollowControllerValidator.java
- `SettlementProfileControllerValidator` validate/SettlementProfileControllerValidator.java
- `SettlementUserControllerValidator` validate/SettlementUserControllerValidator.java · public: validateUserRegistrationDto, validateLoginRequestDto, validateEmailFindRequestDto, validatePasswordResetEligibilityDto, validatePasswordResetDto, validateLoginInfoUpdateDto

### 다른 도메인 참조 (import·주입 기준)

- **notification**: `NotificationCategory` ← SettlementFollowCommandService; `NotificationType` ← SettlementFollowCommandService
- **settlementnotification**: `SettlementNotificationCommandService` ← SettlementFollowCommandService, SettlementUserCommandService
- **user**: `AuthRedisService` ← SettlementAdminAuthController, SettlementOAuthController, SettlementUserCommandController, SettlementUserQueryController, SettlementUserUpdateService; `AuthService` ← SettlementAdminAuthController, SettlementUserQueryController; `BankAccount` ← SettlementBankAccountCommandService, SettlementBankAccountQueryService; `BankAccountRepository` ← SettlementAdminUserService, SettlementBankAccountQueryService; `BlockedProfile` ← SettlementBlockedProfileCommandService, SettlementBlockedProfileQueryService, SettlementBlockedProfileRepository; `Follow` ← SettlementFollowCommandService, SettlementFollowQueryService, SettlementFollowRepository; `Gender` ← SettlementAdminAuthController, SettlementUserCommandService; `Point` ← SettlementAdminPointService, SettlementPointCommandService, SettlementPointQueryService, SettlementPointRepository; `PointStatus` ← SettlementAdminPointService, SettlementPointCommandService, SettlementPointQueryService, SettlementPointRepository; `Profile` ← SettlementBlockedProfileCommandService, SettlementBlockedProfileQueryService, SettlementBlockedProfileRepository, SettlementFollowCommandService, SettlementFollowQueryService, SettlementFollowRepository, SettlementProfileQueryService, SettlementProfileService; `ProfileCommandService` ← SettlementProfileController, SettlementUserCommandService; `ProfileRepository` ← SettlementAdminUserService, SettlementBlockedProfileCommandService, SettlementFollowCommandService, SettlementFollowQueryService, SettlementProfileQueryService; `Role` ← SettlementAdminAuthController, SettlementUserQueryService; `SanctionStatus` ← SettlementAdminSanctionController, SettlementUserSanctionRepository, SettlementUserSanctionSpecifications; `SanctionType` ← SettlementAdminSanctionController, SettlementUserSanctionRepository, SettlementUserSanctionSpecifications; `ShoeSize` ← SettlementUserUpdateService; `User` ← SettlementAdminAuthController, SettlementAdminGradeService, SettlementAdminPointService, SettlementBankAccountCommandService, SettlementCustomUserDetailsService, SettlementOAuthController, SettlementPasswordResetService, SettlementPointCommandService, SettlementPointQueryService, SettlementPointRepository, SettlementProfileService, SettlementUserCommandController, SettlementUserCommandService, SettlementUserQueryController, SettlementUserQueryService, SettlementUserSanctionRepository, SettlementUserSanctionSpecifications, SettlementUserUpdateService; `UserGrade` ← SettlementAdminGradeService, SettlementUserGradeRepository; `UserRepository` ← SettlementAdminGradeService, SettlementAdminPointService, SettlementAdminSanctionService, SettlementAdminUserService, SettlementBankAccountCommandService, SettlementCustomUserDetailsService, SettlementOAuthController, SettlementPasswordResetService, SettlementUserCommandService, SettlementUserQueryService, SettlementUserUpdateService; `UserSanction` ← SettlementUserSanctionRepository, SettlementUserSanctionSpecifications

### 이 도메인을 참조하는 다른 도메인

- **settlementchatquestion**: SettlementUserQueryService
- **settlementevent**: SettlementUserQueryService
- **settlementfaq**: SettlementUserQueryService
- **settlementinquiry**: SettlementUserQueryService
- **settlementinspection**: SettlementUserQueryService
- **settlementnotice**: SettlementUserQueryService
- **settlementorder**: SettlementUserQueryService
- **settlementpayment**: SettlementUserQueryService
- **settlementproduct**: SettlementUserQueryService
- **settlementsale**: SettlementBankAccountQueryService, SettlementUserQueryService
- **settlementstyle**: SettlementProfileQueryService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

