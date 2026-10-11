# adminchatquestion 도메인

기준 경로: `src/main/java/com/fream/back/domain/adminchatquestion/` · 파일 21개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/admin/chat/question` | `AdminChatController#askQuestion` | `this#validateQuestionRequest`, `AdminChatService#processQuestion` |
| GET | `/admin/chat/history` | `AdminChatController#getChatHistory` | `AdminChatService#getChatHistory` |
| GET | `/admin/chat/history/count` | `AdminChatController#getChatHistoryPageCount` | `AdminChatService#getChatHistoryPageCount` |
| GET | `/admin/chat/recent` | `AdminChatController#getRecentQuestions` | `AdminChatService#getRecentQuestions` |
| GET | `/admin/admin/gpt/stats` | `AdminGPTUsageController#getUsageStatistics` | `AdminUserQueryService#checkAdminRole`, `this#validateDateRange`, `AdminGPTUsageService#getUsageStatistics` |
| GET | `/admin/admin/gpt/logs` | `AdminGPTUsageController#getUsageLogs` | `AdminUserQueryService#checkAdminRole`, `AdminGPTUsageService#getUsageLogs` |
| GET | `/admin/admin/gpt/total-tokens` | `AdminGPTUsageController#getTotalTokensUsed` | `AdminUserQueryService#checkAdminRole`, `AdminGPTUsageService#getTotalTokensUsed` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `AdminChatAuditAspect`
  - `handleAuditLogging` → this#prepareAuditContext, this#logAuditEvent, this#calculateAndRecordCost, this#recordQualityMetrics, this#checkCostThreshold, this#sendToExternalSystem, this#sendRealTimeAlert
  - `auditGPTApiCall` → this#extractUserEmailSafely, this#incrementEventCounter
  - `auditAdminAccess` → this#extractUserEmailSafely, this#getClientIp, this#getUserAgent, this#incrementEventCounter
  - `auditHighTokenUsage` → this#extractUserEmailSafely, this#incrementEventCounter
  - `prepareAuditContext` → this#generateAuditId, this#extractUserEmailSafely, this#getClientIp, this#getUserAgent, this#getRequestHeaders, this#extractAndMaskQuestionContent
  - `logAuditEvent` → this#incrementEventCounter
  - `calculateAndRecordCost` → this#calculateTokenCost
  - `recordQualityMetrics` → this#evaluateResponseQuality
  - `checkCostThreshold` → this#incrementEventCounter
  - `extractAndMaskQuestionContent` → this#maskSensitiveContent
- `AdminChatExceptionAspect`
  - `handleAnnotatedException` → this#extractUserEmailSafely, this#isCircuitOpen, this#handleCircuitOpenFallback, this#recordCircuitBreakerSuccess, this#shouldHandleException, this#recordCircuitBreakerFailure, this#isRetryableException, this#executeExceptionStrategy
  - `handleServiceException` → this#extractUserEmailSafely, this#handleGPTApiException, this#handleChatQuestionException, this#handleGenericException
  - `handleControllerException` → this#extractUserEmailSafely, this#logControllerExceptionMetrics
  - `executeExceptionStrategy` → this#logExceptionDetails, this#getDefaultReturnValue, this#executeFallbackLogic
  - `handleGPTApiException` → this#formatArgs
  - `handleChatQuestionException` → this#formatArgs
  - `handleGenericException` → this#formatArgs
  - `executeFallbackLogic` → this#getDefaultReturnValue
  - `handleCircuitOpenFallback` → this#executeFallbackLogic
  - `logExceptionDetails` → this#formatArgs, this#sendAlert
  - `formatArgs` → this#maskEmail
- `AdminChatLoggingAspect`
  - `handleAnnotatedLogging` → this#extractUserEmailSafely, this#generateRequestId, this#trackUserSession, this#logBefore, this#logAfter, this#trackGPTApiCall, this#logException
  - `logServiceMethodEntry` → this#extractUserEmailSafely
  - `logControllerMethodEntry` → this#extractUserEmailSafely, this#formatArgsForLogging
  - `logBefore` → this#formatArgsForLogging, this#extractQuestionSummary, this#logAtLevel
  - `logAfter` → this#extractTokenUsageInfo, this#extractResponseSummary, this#logAtLevel
  - `logException` → this#formatArgsForLogging
  - `formatArgsForLogging` → this#maskEmail
- `AdminChatPerformanceAspect`
  - `monitorAnnotatedPerformance` → this#extractUserEmailSafely, this#getDefaultWarningThreshold, this#getDefaultErrorThreshold, this#executeAdvancedPerformanceMonitoring
  - `monitorControllerPerformance` → this#monitorMethodPerformance
  - `monitorServicePerformance` → this#monitorMethodPerformance
  - `executeAdvancedPerformanceMonitoring` → this#extractTokenUsage, this#evaluateResponseQuality, this#updatePerformanceStats, this#logAdvancedPerformanceMetrics, this#shouldGeneratePerformanceReport, this#generatePerformanceReport
  - `monitorMethodPerformance` → this#extractUserEmailSafely, this#extractTokenUsage, this#updatePerformanceStats, this#logPerformanceMetrics, this#shouldGeneratePerformanceReport, this#generatePerformanceReport
- `AdminChatSecurityAspect`
  - `handleSecurityChecks` → this#getCurrentRequest, this#getClientIpAddress, this#performAuthenticationCheck, this#performSecurityCheck, this#logSecurityAudit, this#handleSecurityViolation
  - `performAuthenticationCheck` → AdminUserQueryService#checkAdminRole, this#checkUserRoles, this#handleViolationAction
  - `performSecurityCheck` → this#handleViolationAction, this#performAuthorizationCheck, this#checkRateLimit, this#checkIpRestriction, this#checkTimeRestriction, this#checkTokenLimit, this#checkContentFilter, this#checkSpamDetection, this#checkRepeatedQuestion, this#checkSensitiveDataRequest
  - `performAuthorizationCheck` → this#checkUserRoles
  - `checkUserRoles` → AdminUserQueryService#findByEmail
  - `checkRateLimit` → this#handleViolationAction
  - `checkIpRestriction` → this#handleViolationAction
  - `checkTimeRestriction` → this#handleViolationAction
  - `checkTokenLimit` → this#handleViolationAction
  - `checkContentFilter` → this#extractQuestionContent
  - `checkSpamDetection` → this#extractQuestionContent
  - `checkRepeatedQuestion` → this#extractQuestionContent
  - `checkSensitiveDataRequest` → this#extractQuestionContent
  - `handleSecurityViolation` → this#logSecurityAudit
  - `logSecurityAudit` → this#formatArgsForAudit
- `AdminChatService`
  - `processQuestion` → this#validateQuestionRequest, AdminUserQueryService#findByEmail, AdminFAQQueryService#getAllFAQs, AdminGPTService#getGPTResponseWithUsage, this#saveChatQuestion, AdminGPTUsageService#logGPTUsage
  - `saveChatQuestion` → AdminChatQuestionRepository#save~
  - `getChatHistory` → AdminUserQueryService#findByEmail, AdminChatQuestionRepository#findByUserIdOrderByCreatedAtDesc
  - `getChatHistoryPageCount` → AdminUserQueryService#findByEmail, AdminChatQuestionRepository#countByUserId
  - `getRecentQuestions` → AdminUserQueryService#findByEmail, AdminChatQuestionRepository#findRecentQuestionsByUserId
- `AdminGPTService`
  - `getGPTResponse` → this#validateQuestion, this#getGPTResponseWithUsage
  - `getGPTResponseWithUsage` → this#validateQuestion, this#truncateQuestion, this#formatFAQData, GPTConfig#getModel, this#createHttpRequestEntity, this#callGptApi, this#logApiResponse
  - `createHttpRequestEntity` → GPTConfig#getApiKey
  - `callGptApi` → GPTConfig#getApiUrl
- `AdminGPTUsageService`
  - `logGPTUsage` → AdminGPTUsageLogRepository#save~
  - `getUsageStatistics` → this#validateDateRange, AdminGPTUsageLogRepository#getTotalTokensUsedBetweenDates, AdminGPTUsageLogRepository#getDailyTokenUsage, AdminGPTUsageLogRepository#getTokenUsageByModelBetweenDates, AdminGPTUsageLogRepository#getTokenUsageByRequestTypeBetweenDates, this#calculateEstimatedCost
  - `getUsageLogs` → AdminGPTUsageLogRepository#findAllByOrderByCreatedDateDesc
  - `convertToDto` → this#extractQuestionContent
  - `getTotalTokensUsed` → AdminGPTUsageLogRepository#getTotalTokensUsedBetweenDates

## 3. 클래스 목록 (역할별)

### controller (2)

- `AdminChatController` controller/AdminChatController.java · public: askQuestion, getChatHistory, getChatHistoryPageCount, getRecentQuestions · 주입: AdminChatService
- `AdminGPTUsageController` controller/AdminGPTUsageController.java · public: getUsageStatistics, getUsageLogs, getTotalTokensUsed · 주입: AdminGPTUsageService, AdminUserQueryService

### service (3)

- `AdminChatService` service/AdminChatService.java · public: processQuestion, getChatHistory, getChatHistoryPageCount, getRecentQuestions · 주입: AdminChatQuestionRepository, AdminFAQQueryService, AdminGPTService, AdminUserQueryService, AdminGPTUsageService
- `AdminGPTService` service/AdminGPTService.java · public: getGPTResponse, getGPTResponseWithUsage · 주입: RestTemplate, GPTConfig
- `AdminGPTUsageService` service/AdminGPTUsageService.java · public: logGPTUsage, getUsageStatistics, getUsageLogs, getTotalTokensUsed · 주입: AdminGPTUsageLogRepository

### repository (2)

- `AdminChatQuestionRepository` (interface) repository/AdminChatQuestionRepository.java · extends/implements JpaRepository, AdminChatQuestionRepositoryCustom · public: findByUserIdOrderByCreatedAtDesc, countByUserId
- `AdminGPTUsageLogRepository` (interface) repository/AdminGPTUsageLogRepository.java · extends/implements JpaRepository, AdminGPTUsageLogRepositoryCustom · public: findAllByOrderByCreatedDateDesc

### custom (4)

- `AdminChatQuestionRepositoryCustom` (interface) repository/AdminChatQuestionRepositoryCustom.java · public: findRecentQuestionsByUserId, findRecentQuestionsByClientIp, searchQuestionsByKeyword
- `AdminChatQuestionRepositoryImpl` repository/AdminChatQuestionRepositoryImpl.java · extends/implements AdminChatQuestionRepositoryCustom · public: findRecentQuestionsByUserId, findRecentQuestionsByClientIp, searchQuestionsByKeyword · 주입: JPAQueryFactory
- `AdminGPTUsageLogRepositoryCustom` (interface) repository/AdminGPTUsageLogRepositoryCustom.java · public: getTotalTokensUsedBetweenDates, getTokenUsageByModelBetweenDates, getDailyTokenUsage, getTokenUsageByRequestTypeBetweenDates, getTotalTokensUsedByUser
- `AdminGPTUsageLogRepositoryImpl` repository/AdminGPTUsageLogRepositoryImpl.java · extends/implements AdminGPTUsageLogRepositoryCustom · public: getTotalTokensUsedBetweenDates, getTokenUsageByModelBetweenDates, getDailyTokenUsage, getTokenUsageByRequestTypeBetweenDates, getTotalTokensUsedByUser · 주입: JPAQueryFactory

### aop (5)

- `AdminChatAuditAspect` aop/AdminChatAuditAspect.java · public: handleAuditLogging, auditGPTApiCall, auditAdminAccess, auditHighTokenUsage
- `AdminChatExceptionAspect` aop/AdminChatExceptionAspect.java · public: handleAnnotatedException, handleServiceException, handleControllerException
- `AdminChatLoggingAspect` aop/AdminChatLoggingAspect.java · public: handleAnnotatedLogging, logServiceMethodEntry, logControllerMethodEntry
- `AdminChatPerformanceAspect` aop/AdminChatPerformanceAspect.java · public: monitorAnnotatedPerformance, monitorControllerPerformance, monitorServicePerformance
- `AdminChatSecurityAspect` aop/AdminChatSecurityAspect.java · public: handleSecurityChecks · 주입: AdminUserQueryService

### other (5)

- `AdminChatAudit` (@interface) aop/annotation/AdminChatAudit.java
- `AdminChatExceptionHandling` (@interface) aop/annotation/AdminChatExceptionHandling.java
- `AdminChatLogging` (@interface) aop/annotation/AdminChatLogging.java
- `AdminChatPerformance` (@interface) aop/annotation/AdminChatPerformance.java
- `AdminChatSecurity` (@interface) aop/annotation/AdminChatSecurity.java

### 다른 도메인 참조 (import·주입 기준)

- **adminfaq**: `AdminFAQQueryService` ← AdminChatService
- **adminuser**: `AdminUserQueryService` ← AdminChatSecurityAspect, AdminChatService, AdminGPTUsageController
- **chatQuestion**: `ChatQuestion` ← AdminChatAuditAspect, AdminChatQuestionRepository, AdminChatQuestionRepositoryCustom, AdminChatQuestionRepositoryImpl, AdminChatService, AdminGPTUsageService; `GPTUsageLog` ← AdminGPTUsageLogRepository, AdminGPTUsageService
- **user**: `User` ← AdminChatSecurityAspect, AdminChatService, AdminGPTUsageService

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

- `AdminChatAuditAspect#handleAuditLogging` @Around("@annotation(chatAudit)") · 바인딩: `@AdminChatAudit`
- `AdminChatAuditAspect#auditGPTApiCall` @AfterReturning(pointcut = "execution(* com.fream.back.domain.adminchatquestion.service.AdminGPTService.getGPTResponseWithUsage(..))", returning = "result")
- `AdminChatAuditAspect#auditAdminAccess` @Before("execution(* com.fream.back.domain.adminchatquestion.controller.AdminGPTUsageController.*(..))")
- `AdminChatAuditAspect#auditHighTokenUsage` @AfterReturning(pointcut = "execution(* com.fream.back.domain.adminchatquestion.service.AdminChatService.processQuestion(..))", returning = "result")
- `AdminChatExceptionAspect#handleAnnotatedException` @Around("@annotation(exceptionHandling)") · 바인딩: `@AdminChatExceptionHandling`
- `AdminChatExceptionAspect#handleServiceException` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.adminchatquestion.service..*(..))", throwing = "exception")
- `AdminChatExceptionAspect#handleControllerException` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.adminchatquestion.controller..*(..))", throwing = "exception")
- `AdminChatLoggingAspect#handleAnnotatedLogging` @Around("@annotation(chatLogging)") · 바인딩: `@AdminChatLogging`
- `AdminChatLoggingAspect#logServiceMethodEntry` @Before("execution(* com.fream.back.domain.adminchatquestion.service..*(..))")
- `AdminChatLoggingAspect#logControllerMethodEntry` @Before("execution(* com.fream.back.domain.adminchatquestion.controller..*(..))")
- `AdminChatPerformanceAspect#monitorAnnotatedPerformance` @Around("@annotation(chatPerformance)") · 바인딩: `@AdminChatPerformance`
- `AdminChatPerformanceAspect#monitorControllerPerformance` @Around("execution(* com.fream.back.domain.adminchatquestion.controller..*(..))")
- `AdminChatPerformanceAspect#monitorServicePerformance` @Around("execution(* com.fream.back.domain.adminchatquestion.service..*(..))")
- `AdminChatSecurityAspect#handleSecurityChecks` @Around("@annotation(chatSecurity)") · 바인딩: `@AdminChatSecurity`

어노테이션 적용 지점(메서드에 붙은 곳):

- `@AdminChatAudit`: (없음)
- `@AdminChatExceptionHandling`: (없음)
- `@AdminChatLogging`: (없음)
- `@AdminChatPerformance`: (없음)
- `@AdminChatSecurity`: (없음)

