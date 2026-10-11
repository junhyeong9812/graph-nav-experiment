# internalchatquestion 도메인

기준 경로: `src/main/java/com/fream/back/domain/internalchatquestion/` · 파일 21개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/internal/chat/question` | `InternalChatController#askQuestion` | `this#validateQuestionRequest`, `InternalChatService#processQuestion` |
| GET | `/internal/chat/history` | `InternalChatController#getChatHistory` | `InternalChatService#getChatHistory` |
| GET | `/internal/chat/history/count` | `InternalChatController#getChatHistoryPageCount` | `InternalChatService#getChatHistoryPageCount` |
| GET | `/internal/chat/recent` | `InternalChatController#getRecentQuestions` | `InternalChatService#getRecentQuestions` |
| GET | `/internal/admin/gpt/stats` | `InternalGPTUsageController#getUsageStatistics` | `InternalUserQueryService#checkAdminRole`, `this#validateDateRange`, `InternalGPTUsageService#getUsageStatistics` |
| GET | `/internal/admin/gpt/logs` | `InternalGPTUsageController#getUsageLogs` | `InternalUserQueryService#checkAdminRole`, `InternalGPTUsageService#getUsageLogs` |
| GET | `/internal/admin/gpt/total-tokens` | `InternalGPTUsageController#getTotalTokensUsed` | `InternalUserQueryService#checkAdminRole`, `InternalGPTUsageService#getTotalTokensUsed` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `InternalChatAuditAspect`
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
- `InternalChatExceptionAspect`
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
- `InternalChatLoggingAspect`
  - `handleAnnotatedLogging` → this#extractUserEmailSafely, this#generateRequestId, this#trackUserSession, this#logBefore, this#logAfter, this#trackGPTApiCall, this#logException
  - `logServiceMethodEntry` → this#extractUserEmailSafely
  - `logControllerMethodEntry` → this#extractUserEmailSafely, this#formatArgsForLogging
  - `logBefore` → this#formatArgsForLogging, this#extractQuestionSummary, this#logAtLevel
  - `logAfter` → this#extractTokenUsageInfo, this#extractResponseSummary, this#logAtLevel
  - `logException` → this#formatArgsForLogging
  - `formatArgsForLogging` → this#maskEmail
- `InternalChatPerformanceAspect`
  - `monitorAnnotatedPerformance` → this#extractUserEmailSafely, this#getDefaultWarningThreshold, this#getDefaultErrorThreshold, this#executeAdvancedPerformanceMonitoring
  - `monitorControllerPerformance` → this#monitorMethodPerformance
  - `monitorServicePerformance` → this#monitorMethodPerformance
  - `executeAdvancedPerformanceMonitoring` → this#extractTokenUsage, this#evaluateResponseQuality, this#updatePerformanceStats, this#logAdvancedPerformanceMetrics, this#shouldGeneratePerformanceReport, this#generatePerformanceReport
  - `monitorMethodPerformance` → this#extractUserEmailSafely, this#extractTokenUsage, this#updatePerformanceStats, this#logPerformanceMetrics, this#shouldGeneratePerformanceReport, this#generatePerformanceReport
- `InternalChatSecurityAspect`
  - `handleSecurityChecks` → this#getCurrentRequest, this#getClientIpAddress, this#performAuthenticationCheck, this#performSecurityCheck, this#logSecurityAudit, this#handleSecurityViolation
  - `performAuthenticationCheck` → InternalUserQueryService#checkAdminRole, this#checkUserRoles, this#handleViolationAction
  - `performSecurityCheck` → this#handleViolationAction, this#performAuthorizationCheck, this#checkRateLimit, this#checkIpRestriction, this#checkTimeRestriction, this#checkTokenLimit, this#checkContentFilter, this#checkSpamDetection, this#checkRepeatedQuestion, this#checkSensitiveDataRequest
  - `performAuthorizationCheck` → this#checkUserRoles
  - `checkUserRoles` → InternalUserQueryService#findByEmail
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
- `InternalChatService`
  - `processQuestion` → this#validateQuestionRequest, InternalUserQueryService#findByEmail, InternalFAQQueryService#getAllFAQs, InternalGPTService#getGPTResponseWithUsage, this#saveChatQuestion, InternalGPTUsageService#logGPTUsage
  - `saveChatQuestion` → InternalChatQuestionRepository#save~
  - `getChatHistory` → InternalUserQueryService#findByEmail, InternalChatQuestionRepository#findByUserIdOrderByCreatedAtDesc
  - `getChatHistoryPageCount` → InternalUserQueryService#findByEmail, InternalChatQuestionRepository#countByUserId
  - `getRecentQuestions` → InternalUserQueryService#findByEmail, InternalChatQuestionRepository#findRecentQuestionsByUserId
- `InternalGPTService`
  - `getGPTResponse` → this#validateQuestion, this#getGPTResponseWithUsage
  - `getGPTResponseWithUsage` → this#validateQuestion, this#truncateQuestion, this#formatFAQData, GPTConfig#getModel, this#createHttpRequestEntity, this#callGptApi, this#logApiResponse
  - `createHttpRequestEntity` → GPTConfig#getApiKey
  - `callGptApi` → GPTConfig#getApiUrl
- `InternalGPTUsageService`
  - `logGPTUsage` → InternalGPTUsageLogRepository#save~
  - `getUsageStatistics` → this#validateDateRange, InternalGPTUsageLogRepository#getTotalTokensUsedBetweenDates, InternalGPTUsageLogRepository#getDailyTokenUsage, InternalGPTUsageLogRepository#getTokenUsageByModelBetweenDates, InternalGPTUsageLogRepository#getTokenUsageByRequestTypeBetweenDates, this#calculateEstimatedCost
  - `getUsageLogs` → InternalGPTUsageLogRepository#findAllByOrderByCreatedDateDesc
  - `convertToDto` → this#extractQuestionContent
  - `getTotalTokensUsed` → InternalGPTUsageLogRepository#getTotalTokensUsedBetweenDates

## 3. 클래스 목록 (역할별)

### controller (2)

- `InternalChatController` controller/InternalChatController.java · public: askQuestion, getChatHistory, getChatHistoryPageCount, getRecentQuestions · 주입: InternalChatService
- `InternalGPTUsageController` controller/InternalGPTUsageController.java · public: getUsageStatistics, getUsageLogs, getTotalTokensUsed · 주입: InternalGPTUsageService, InternalUserQueryService

### service (3)

- `InternalChatService` service/InternalChatService.java · public: processQuestion, getChatHistory, getChatHistoryPageCount, getRecentQuestions · 주입: InternalChatQuestionRepository, InternalFAQQueryService, InternalGPTService, InternalUserQueryService, InternalGPTUsageService
- `InternalGPTService` service/InternalGPTService.java · public: getGPTResponse, getGPTResponseWithUsage · 주입: RestTemplate, GPTConfig
- `InternalGPTUsageService` service/InternalGPTUsageService.java · public: logGPTUsage, getUsageStatistics, getUsageLogs, getTotalTokensUsed · 주입: InternalGPTUsageLogRepository

### repository (2)

- `InternalChatQuestionRepository` (interface) repository/InternalChatQuestionRepository.java · extends/implements JpaRepository, InternalChatQuestionRepositoryCustom · public: findByUserIdOrderByCreatedAtDesc, countByUserId
- `InternalGPTUsageLogRepository` (interface) repository/InternalGPTUsageLogRepository.java · extends/implements JpaRepository, InternalGPTUsageLogRepositoryCustom · public: findAllByOrderByCreatedDateDesc

### custom (4)

- `InternalChatQuestionRepositoryCustom` (interface) repository/InternalChatQuestionRepositoryCustom.java · public: findRecentQuestionsByUserId, findRecentQuestionsByClientIp, searchQuestionsByKeyword
- `InternalChatQuestionRepositoryImpl` repository/InternalChatQuestionRepositoryImpl.java · extends/implements InternalChatQuestionRepositoryCustom · public: findRecentQuestionsByUserId, findRecentQuestionsByClientIp, searchQuestionsByKeyword · 주입: JPAQueryFactory
- `InternalGPTUsageLogRepositoryCustom` (interface) repository/InternalGPTUsageLogRepositoryCustom.java · public: getTotalTokensUsedBetweenDates, getTokenUsageByModelBetweenDates, getDailyTokenUsage, getTokenUsageByRequestTypeBetweenDates, getTotalTokensUsedByUser
- `InternalGPTUsageLogRepositoryImpl` repository/InternalGPTUsageLogRepositoryImpl.java · extends/implements InternalGPTUsageLogRepositoryCustom · public: getTotalTokensUsedBetweenDates, getTokenUsageByModelBetweenDates, getDailyTokenUsage, getTokenUsageByRequestTypeBetweenDates, getTotalTokensUsedByUser · 주입: JPAQueryFactory

### aop (5)

- `InternalChatAuditAspect` aop/InternalChatAuditAspect.java · public: handleAuditLogging, auditGPTApiCall, auditAdminAccess, auditHighTokenUsage
- `InternalChatExceptionAspect` aop/InternalChatExceptionAspect.java · public: handleAnnotatedException, handleServiceException, handleControllerException
- `InternalChatLoggingAspect` aop/InternalChatLoggingAspect.java · public: handleAnnotatedLogging, logServiceMethodEntry, logControllerMethodEntry
- `InternalChatPerformanceAspect` aop/InternalChatPerformanceAspect.java · public: monitorAnnotatedPerformance, monitorControllerPerformance, monitorServicePerformance
- `InternalChatSecurityAspect` aop/InternalChatSecurityAspect.java · public: handleSecurityChecks · 주입: InternalUserQueryService

### other (5)

- `InternalChatAudit` (@interface) aop/annotation/InternalChatAudit.java
- `InternalChatExceptionHandling` (@interface) aop/annotation/InternalChatExceptionHandling.java
- `InternalChatLogging` (@interface) aop/annotation/InternalChatLogging.java
- `InternalChatPerformance` (@interface) aop/annotation/InternalChatPerformance.java
- `InternalChatSecurity` (@interface) aop/annotation/InternalChatSecurity.java

### 다른 도메인 참조 (import·주입 기준)

- **chatQuestion**: `ChatQuestion` ← InternalChatAuditAspect, InternalChatQuestionRepository, InternalChatQuestionRepositoryCustom, InternalChatQuestionRepositoryImpl, InternalChatService, InternalGPTUsageService; `GPTUsageLog` ← InternalGPTUsageLogRepository, InternalGPTUsageService
- **internalfaq**: `InternalFAQQueryService` ← InternalChatService
- **internaluser**: `InternalUserQueryService` ← InternalChatSecurityAspect, InternalChatService, InternalGPTUsageController
- **user**: `User` ← InternalChatSecurityAspect, InternalChatService, InternalGPTUsageService

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

- `InternalChatAuditAspect#handleAuditLogging` @Around("@annotation(chatAudit)") · 바인딩: `@InternalChatAudit`
- `InternalChatAuditAspect#auditGPTApiCall` @AfterReturning(pointcut = "execution(* com.fream.back.domain.internalchatquestion.service.InternalGPTService.getGPTResponseWithUsage(..))", returning = "result")
- `InternalChatAuditAspect#auditAdminAccess` @Before("execution(* com.fream.back.domain.internalchatquestion.controller.InternalGPTUsageController.*(..))")
- `InternalChatAuditAspect#auditHighTokenUsage` @AfterReturning(pointcut = "execution(* com.fream.back.domain.internalchatquestion.service.InternalChatService.processQuestion(..))", returning = "result")
- `InternalChatExceptionAspect#handleAnnotatedException` @Around("@annotation(exceptionHandling)") · 바인딩: `@InternalChatExceptionHandling`
- `InternalChatExceptionAspect#handleServiceException` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.internalchatquestion.service..*(..))", throwing = "exception")
- `InternalChatExceptionAspect#handleControllerException` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.internalchatquestion.controller..*(..))", throwing = "exception")
- `InternalChatLoggingAspect#handleAnnotatedLogging` @Around("@annotation(chatLogging)") · 바인딩: `@InternalChatLogging`
- `InternalChatLoggingAspect#logServiceMethodEntry` @Before("execution(* com.fream.back.domain.internalchatquestion.service..*(..))")
- `InternalChatLoggingAspect#logControllerMethodEntry` @Before("execution(* com.fream.back.domain.internalchatquestion.controller..*(..))")
- `InternalChatPerformanceAspect#monitorAnnotatedPerformance` @Around("@annotation(chatPerformance)") · 바인딩: `@InternalChatPerformance`
- `InternalChatPerformanceAspect#monitorControllerPerformance` @Around("execution(* com.fream.back.domain.internalchatquestion.controller..*(..))")
- `InternalChatPerformanceAspect#monitorServicePerformance` @Around("execution(* com.fream.back.domain.internalchatquestion.service..*(..))")
- `InternalChatSecurityAspect#handleSecurityChecks` @Around("@annotation(chatSecurity)") · 바인딩: `@InternalChatSecurity`

어노테이션 적용 지점(메서드에 붙은 곳):

- `@InternalChatAudit`: (없음)
- `@InternalChatExceptionHandling`: (없음)
- `@InternalChatLogging`: (없음)
- `@InternalChatPerformance`: (없음)
- `@InternalChatSecurity`: (없음)

