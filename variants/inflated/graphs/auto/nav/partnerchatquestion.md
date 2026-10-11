# partnerchatquestion 도메인

기준 경로: `src/main/java/com/fream/back/domain/partnerchatquestion/` · 파일 21개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/partner/chat/question` | `PartnerChatController#askQuestion` | `this#validateQuestionRequest`, `PartnerChatService#processQuestion` |
| GET | `/partner/chat/history` | `PartnerChatController#getChatHistory` | `PartnerChatService#getChatHistory` |
| GET | `/partner/chat/history/count` | `PartnerChatController#getChatHistoryPageCount` | `PartnerChatService#getChatHistoryPageCount` |
| GET | `/partner/chat/recent` | `PartnerChatController#getRecentQuestions` | `PartnerChatService#getRecentQuestions` |
| GET | `/partner/admin/gpt/stats` | `PartnerGPTUsageController#getUsageStatistics` | `PartnerUserQueryService#checkAdminRole`, `this#validateDateRange`, `PartnerGPTUsageService#getUsageStatistics` |
| GET | `/partner/admin/gpt/logs` | `PartnerGPTUsageController#getUsageLogs` | `PartnerUserQueryService#checkAdminRole`, `PartnerGPTUsageService#getUsageLogs` |
| GET | `/partner/admin/gpt/total-tokens` | `PartnerGPTUsageController#getTotalTokensUsed` | `PartnerUserQueryService#checkAdminRole`, `PartnerGPTUsageService#getTotalTokensUsed` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `PartnerChatAuditAspect`
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
- `PartnerChatExceptionAspect`
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
- `PartnerChatLoggingAspect`
  - `handleAnnotatedLogging` → this#extractUserEmailSafely, this#generateRequestId, this#trackUserSession, this#logBefore, this#logAfter, this#trackGPTApiCall, this#logException
  - `logServiceMethodEntry` → this#extractUserEmailSafely
  - `logControllerMethodEntry` → this#extractUserEmailSafely, this#formatArgsForLogging
  - `logBefore` → this#formatArgsForLogging, this#extractQuestionSummary, this#logAtLevel
  - `logAfter` → this#extractTokenUsageInfo, this#extractResponseSummary, this#logAtLevel
  - `logException` → this#formatArgsForLogging
  - `formatArgsForLogging` → this#maskEmail
- `PartnerChatPerformanceAspect`
  - `monitorAnnotatedPerformance` → this#extractUserEmailSafely, this#getDefaultWarningThreshold, this#getDefaultErrorThreshold, this#executeAdvancedPerformanceMonitoring
  - `monitorControllerPerformance` → this#monitorMethodPerformance
  - `monitorServicePerformance` → this#monitorMethodPerformance
  - `executeAdvancedPerformanceMonitoring` → this#extractTokenUsage, this#evaluateResponseQuality, this#updatePerformanceStats, this#logAdvancedPerformanceMetrics, this#shouldGeneratePerformanceReport, this#generatePerformanceReport
  - `monitorMethodPerformance` → this#extractUserEmailSafely, this#extractTokenUsage, this#updatePerformanceStats, this#logPerformanceMetrics, this#shouldGeneratePerformanceReport, this#generatePerformanceReport
- `PartnerChatSecurityAspect`
  - `handleSecurityChecks` → this#getCurrentRequest, this#getClientIpAddress, this#performAuthenticationCheck, this#performSecurityCheck, this#logSecurityAudit, this#handleSecurityViolation
  - `performAuthenticationCheck` → PartnerUserQueryService#checkAdminRole, this#checkUserRoles, this#handleViolationAction
  - `performSecurityCheck` → this#handleViolationAction, this#performAuthorizationCheck, this#checkRateLimit, this#checkIpRestriction, this#checkTimeRestriction, this#checkTokenLimit, this#checkContentFilter, this#checkSpamDetection, this#checkRepeatedQuestion, this#checkSensitiveDataRequest
  - `performAuthorizationCheck` → this#checkUserRoles
  - `checkUserRoles` → PartnerUserQueryService#findByEmail
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
- `PartnerChatService`
  - `processQuestion` → this#validateQuestionRequest, PartnerUserQueryService#findByEmail, PartnerFAQQueryService#getAllFAQs, PartnerGPTService#getGPTResponseWithUsage, this#saveChatQuestion, PartnerGPTUsageService#logGPTUsage
  - `saveChatQuestion` → PartnerChatQuestionRepository#save~
  - `getChatHistory` → PartnerUserQueryService#findByEmail, PartnerChatQuestionRepository#findByUserIdOrderByCreatedAtDesc
  - `getChatHistoryPageCount` → PartnerUserQueryService#findByEmail, PartnerChatQuestionRepository#countByUserId
  - `getRecentQuestions` → PartnerUserQueryService#findByEmail, PartnerChatQuestionRepository#findRecentQuestionsByUserId
- `PartnerGPTService`
  - `getGPTResponse` → this#validateQuestion, this#getGPTResponseWithUsage
  - `getGPTResponseWithUsage` → this#validateQuestion, this#truncateQuestion, this#formatFAQData, GPTConfig#getModel, this#createHttpRequestEntity, this#callGptApi, this#logApiResponse
  - `createHttpRequestEntity` → GPTConfig#getApiKey
  - `callGptApi` → GPTConfig#getApiUrl
- `PartnerGPTUsageService`
  - `logGPTUsage` → PartnerGPTUsageLogRepository#save~
  - `getUsageStatistics` → this#validateDateRange, PartnerGPTUsageLogRepository#getTotalTokensUsedBetweenDates, PartnerGPTUsageLogRepository#getDailyTokenUsage, PartnerGPTUsageLogRepository#getTokenUsageByModelBetweenDates, PartnerGPTUsageLogRepository#getTokenUsageByRequestTypeBetweenDates, this#calculateEstimatedCost
  - `getUsageLogs` → PartnerGPTUsageLogRepository#findAllByOrderByCreatedDateDesc
  - `convertToDto` → this#extractQuestionContent
  - `getTotalTokensUsed` → PartnerGPTUsageLogRepository#getTotalTokensUsedBetweenDates

## 3. 클래스 목록 (역할별)

### controller (2)

- `PartnerChatController` controller/PartnerChatController.java · public: askQuestion, getChatHistory, getChatHistoryPageCount, getRecentQuestions · 주입: PartnerChatService
- `PartnerGPTUsageController` controller/PartnerGPTUsageController.java · public: getUsageStatistics, getUsageLogs, getTotalTokensUsed · 주입: PartnerGPTUsageService, PartnerUserQueryService

### service (3)

- `PartnerChatService` service/PartnerChatService.java · public: processQuestion, getChatHistory, getChatHistoryPageCount, getRecentQuestions · 주입: PartnerChatQuestionRepository, PartnerFAQQueryService, PartnerGPTService, PartnerUserQueryService, PartnerGPTUsageService
- `PartnerGPTService` service/PartnerGPTService.java · public: getGPTResponse, getGPTResponseWithUsage · 주입: RestTemplate, GPTConfig
- `PartnerGPTUsageService` service/PartnerGPTUsageService.java · public: logGPTUsage, getUsageStatistics, getUsageLogs, getTotalTokensUsed · 주입: PartnerGPTUsageLogRepository

### repository (2)

- `PartnerChatQuestionRepository` (interface) repository/PartnerChatQuestionRepository.java · extends/implements JpaRepository, PartnerChatQuestionRepositoryCustom · public: findByUserIdOrderByCreatedAtDesc, countByUserId
- `PartnerGPTUsageLogRepository` (interface) repository/PartnerGPTUsageLogRepository.java · extends/implements JpaRepository, PartnerGPTUsageLogRepositoryCustom · public: findAllByOrderByCreatedDateDesc

### custom (4)

- `PartnerChatQuestionRepositoryCustom` (interface) repository/PartnerChatQuestionRepositoryCustom.java · public: findRecentQuestionsByUserId, findRecentQuestionsByClientIp, searchQuestionsByKeyword
- `PartnerChatQuestionRepositoryImpl` repository/PartnerChatQuestionRepositoryImpl.java · extends/implements PartnerChatQuestionRepositoryCustom · public: findRecentQuestionsByUserId, findRecentQuestionsByClientIp, searchQuestionsByKeyword · 주입: JPAQueryFactory
- `PartnerGPTUsageLogRepositoryCustom` (interface) repository/PartnerGPTUsageLogRepositoryCustom.java · public: getTotalTokensUsedBetweenDates, getTokenUsageByModelBetweenDates, getDailyTokenUsage, getTokenUsageByRequestTypeBetweenDates, getTotalTokensUsedByUser
- `PartnerGPTUsageLogRepositoryImpl` repository/PartnerGPTUsageLogRepositoryImpl.java · extends/implements PartnerGPTUsageLogRepositoryCustom · public: getTotalTokensUsedBetweenDates, getTokenUsageByModelBetweenDates, getDailyTokenUsage, getTokenUsageByRequestTypeBetweenDates, getTotalTokensUsedByUser · 주입: JPAQueryFactory

### aop (5)

- `PartnerChatAuditAspect` aop/PartnerChatAuditAspect.java · public: handleAuditLogging, auditGPTApiCall, auditAdminAccess, auditHighTokenUsage
- `PartnerChatExceptionAspect` aop/PartnerChatExceptionAspect.java · public: handleAnnotatedException, handleServiceException, handleControllerException
- `PartnerChatLoggingAspect` aop/PartnerChatLoggingAspect.java · public: handleAnnotatedLogging, logServiceMethodEntry, logControllerMethodEntry
- `PartnerChatPerformanceAspect` aop/PartnerChatPerformanceAspect.java · public: monitorAnnotatedPerformance, monitorControllerPerformance, monitorServicePerformance
- `PartnerChatSecurityAspect` aop/PartnerChatSecurityAspect.java · public: handleSecurityChecks · 주입: PartnerUserQueryService

### other (5)

- `PartnerChatAudit` (@interface) aop/annotation/PartnerChatAudit.java
- `PartnerChatExceptionHandling` (@interface) aop/annotation/PartnerChatExceptionHandling.java
- `PartnerChatLogging` (@interface) aop/annotation/PartnerChatLogging.java
- `PartnerChatPerformance` (@interface) aop/annotation/PartnerChatPerformance.java
- `PartnerChatSecurity` (@interface) aop/annotation/PartnerChatSecurity.java

### 다른 도메인 참조 (import·주입 기준)

- **chatQuestion**: `ChatQuestion` ← PartnerChatAuditAspect, PartnerChatQuestionRepository, PartnerChatQuestionRepositoryCustom, PartnerChatQuestionRepositoryImpl, PartnerChatService, PartnerGPTUsageService; `GPTUsageLog` ← PartnerGPTUsageLogRepository, PartnerGPTUsageService
- **partnerfaq**: `PartnerFAQQueryService` ← PartnerChatService
- **partneruser**: `PartnerUserQueryService` ← PartnerChatSecurityAspect, PartnerChatService, PartnerGPTUsageController
- **user**: `User` ← PartnerChatSecurityAspect, PartnerChatService, PartnerGPTUsageService

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

- `PartnerChatAuditAspect#handleAuditLogging` @Around("@annotation(chatAudit)") · 바인딩: `@PartnerChatAudit`
- `PartnerChatAuditAspect#auditGPTApiCall` @AfterReturning(pointcut = "execution(* com.fream.back.domain.partnerchatquestion.service.PartnerGPTService.getGPTResponseWithUsage(..))", returning = "result")
- `PartnerChatAuditAspect#auditAdminAccess` @Before("execution(* com.fream.back.domain.partnerchatquestion.controller.PartnerGPTUsageController.*(..))")
- `PartnerChatAuditAspect#auditHighTokenUsage` @AfterReturning(pointcut = "execution(* com.fream.back.domain.partnerchatquestion.service.PartnerChatService.processQuestion(..))", returning = "result")
- `PartnerChatExceptionAspect#handleAnnotatedException` @Around("@annotation(exceptionHandling)") · 바인딩: `@PartnerChatExceptionHandling`
- `PartnerChatExceptionAspect#handleServiceException` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.partnerchatquestion.service..*(..))", throwing = "exception")
- `PartnerChatExceptionAspect#handleControllerException` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.partnerchatquestion.controller..*(..))", throwing = "exception")
- `PartnerChatLoggingAspect#handleAnnotatedLogging` @Around("@annotation(chatLogging)") · 바인딩: `@PartnerChatLogging`
- `PartnerChatLoggingAspect#logServiceMethodEntry` @Before("execution(* com.fream.back.domain.partnerchatquestion.service..*(..))")
- `PartnerChatLoggingAspect#logControllerMethodEntry` @Before("execution(* com.fream.back.domain.partnerchatquestion.controller..*(..))")
- `PartnerChatPerformanceAspect#monitorAnnotatedPerformance` @Around("@annotation(chatPerformance)") · 바인딩: `@PartnerChatPerformance`
- `PartnerChatPerformanceAspect#monitorControllerPerformance` @Around("execution(* com.fream.back.domain.partnerchatquestion.controller..*(..))")
- `PartnerChatPerformanceAspect#monitorServicePerformance` @Around("execution(* com.fream.back.domain.partnerchatquestion.service..*(..))")
- `PartnerChatSecurityAspect#handleSecurityChecks` @Around("@annotation(chatSecurity)") · 바인딩: `@PartnerChatSecurity`

어노테이션 적용 지점(메서드에 붙은 곳):

- `@PartnerChatAudit`: (없음)
- `@PartnerChatExceptionHandling`: (없음)
- `@PartnerChatLogging`: (없음)
- `@PartnerChatPerformance`: (없음)
- `@PartnerChatSecurity`: (없음)

