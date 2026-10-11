# legacychatquestion 도메인

기준 경로: `src/main/java/com/fream/back/domain/legacychatquestion/` · 파일 21개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/legacy/chat/question` | `LegacyChatController#askQuestion` | `this#validateQuestionRequest`, `LegacyChatService#processQuestion` |
| GET | `/legacy/chat/history` | `LegacyChatController#getChatHistory` | `LegacyChatService#getChatHistory` |
| GET | `/legacy/chat/history/count` | `LegacyChatController#getChatHistoryPageCount` | `LegacyChatService#getChatHistoryPageCount` |
| GET | `/legacy/chat/recent` | `LegacyChatController#getRecentQuestions` | `LegacyChatService#getRecentQuestions` |
| GET | `/legacy/admin/gpt/stats` | `LegacyGPTUsageController#getUsageStatistics` | `LegacyUserQueryService#checkAdminRole`, `this#validateDateRange`, `LegacyGPTUsageService#getUsageStatistics` |
| GET | `/legacy/admin/gpt/logs` | `LegacyGPTUsageController#getUsageLogs` | `LegacyUserQueryService#checkAdminRole`, `LegacyGPTUsageService#getUsageLogs` |
| GET | `/legacy/admin/gpt/total-tokens` | `LegacyGPTUsageController#getTotalTokensUsed` | `LegacyUserQueryService#checkAdminRole`, `LegacyGPTUsageService#getTotalTokensUsed` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `LegacyChatAuditAspect`
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
- `LegacyChatExceptionAspect`
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
- `LegacyChatLoggingAspect`
  - `handleAnnotatedLogging` → this#extractUserEmailSafely, this#generateRequestId, this#trackUserSession, this#logBefore, this#logAfter, this#trackGPTApiCall, this#logException
  - `logServiceMethodEntry` → this#extractUserEmailSafely
  - `logControllerMethodEntry` → this#extractUserEmailSafely, this#formatArgsForLogging
  - `logBefore` → this#formatArgsForLogging, this#extractQuestionSummary, this#logAtLevel
  - `logAfter` → this#extractTokenUsageInfo, this#extractResponseSummary, this#logAtLevel
  - `logException` → this#formatArgsForLogging
  - `formatArgsForLogging` → this#maskEmail
- `LegacyChatPerformanceAspect`
  - `monitorAnnotatedPerformance` → this#extractUserEmailSafely, this#getDefaultWarningThreshold, this#getDefaultErrorThreshold, this#executeAdvancedPerformanceMonitoring
  - `monitorControllerPerformance` → this#monitorMethodPerformance
  - `monitorServicePerformance` → this#monitorMethodPerformance
  - `executeAdvancedPerformanceMonitoring` → this#extractTokenUsage, this#evaluateResponseQuality, this#updatePerformanceStats, this#logAdvancedPerformanceMetrics, this#shouldGeneratePerformanceReport, this#generatePerformanceReport
  - `monitorMethodPerformance` → this#extractUserEmailSafely, this#extractTokenUsage, this#updatePerformanceStats, this#logPerformanceMetrics, this#shouldGeneratePerformanceReport, this#generatePerformanceReport
- `LegacyChatSecurityAspect`
  - `handleSecurityChecks` → this#getCurrentRequest, this#getClientIpAddress, this#performAuthenticationCheck, this#performSecurityCheck, this#logSecurityAudit, this#handleSecurityViolation
  - `performAuthenticationCheck` → LegacyUserQueryService#checkAdminRole, this#checkUserRoles, this#handleViolationAction
  - `performSecurityCheck` → this#handleViolationAction, this#performAuthorizationCheck, this#checkRateLimit, this#checkIpRestriction, this#checkTimeRestriction, this#checkTokenLimit, this#checkContentFilter, this#checkSpamDetection, this#checkRepeatedQuestion, this#checkSensitiveDataRequest
  - `performAuthorizationCheck` → this#checkUserRoles
  - `checkUserRoles` → LegacyUserQueryService#findByEmail
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
- `LegacyChatService`
  - `processQuestion` → this#validateQuestionRequest, LegacyUserQueryService#findByEmail, LegacyFAQQueryService#getAllFAQs, LegacyGPTService#getGPTResponseWithUsage, this#saveChatQuestion, LegacyGPTUsageService#logGPTUsage
  - `saveChatQuestion` → LegacyChatQuestionRepository#save~
  - `getChatHistory` → LegacyUserQueryService#findByEmail, LegacyChatQuestionRepository#findByUserIdOrderByCreatedAtDesc
  - `getChatHistoryPageCount` → LegacyUserQueryService#findByEmail, LegacyChatQuestionRepository#countByUserId
  - `getRecentQuestions` → LegacyUserQueryService#findByEmail, LegacyChatQuestionRepository#findRecentQuestionsByUserId
- `LegacyGPTService`
  - `getGPTResponse` → this#validateQuestion, this#getGPTResponseWithUsage
  - `getGPTResponseWithUsage` → this#validateQuestion, this#truncateQuestion, this#formatFAQData, GPTConfig#getModel, this#createHttpRequestEntity, this#callGptApi, this#logApiResponse
  - `createHttpRequestEntity` → GPTConfig#getApiKey
  - `callGptApi` → GPTConfig#getApiUrl
- `LegacyGPTUsageService`
  - `logGPTUsage` → LegacyGPTUsageLogRepository#save~
  - `getUsageStatistics` → this#validateDateRange, LegacyGPTUsageLogRepository#getTotalTokensUsedBetweenDates, LegacyGPTUsageLogRepository#getDailyTokenUsage, LegacyGPTUsageLogRepository#getTokenUsageByModelBetweenDates, LegacyGPTUsageLogRepository#getTokenUsageByRequestTypeBetweenDates, this#calculateEstimatedCost
  - `getUsageLogs` → LegacyGPTUsageLogRepository#findAllByOrderByCreatedDateDesc
  - `convertToDto` → this#extractQuestionContent
  - `getTotalTokensUsed` → LegacyGPTUsageLogRepository#getTotalTokensUsedBetweenDates

## 3. 클래스 목록 (역할별)

### controller (2)

- `LegacyChatController` controller/LegacyChatController.java · public: askQuestion, getChatHistory, getChatHistoryPageCount, getRecentQuestions · 주입: LegacyChatService
- `LegacyGPTUsageController` controller/LegacyGPTUsageController.java · public: getUsageStatistics, getUsageLogs, getTotalTokensUsed · 주입: LegacyGPTUsageService, LegacyUserQueryService

### service (3)

- `LegacyChatService` service/LegacyChatService.java · public: processQuestion, getChatHistory, getChatHistoryPageCount, getRecentQuestions · 주입: LegacyChatQuestionRepository, LegacyFAQQueryService, LegacyGPTService, LegacyUserQueryService, LegacyGPTUsageService
- `LegacyGPTService` service/LegacyGPTService.java · public: getGPTResponse, getGPTResponseWithUsage · 주입: RestTemplate, GPTConfig
- `LegacyGPTUsageService` service/LegacyGPTUsageService.java · public: logGPTUsage, getUsageStatistics, getUsageLogs, getTotalTokensUsed · 주입: LegacyGPTUsageLogRepository

### repository (2)

- `LegacyChatQuestionRepository` (interface) repository/LegacyChatQuestionRepository.java · extends/implements JpaRepository, LegacyChatQuestionRepositoryCustom · public: findByUserIdOrderByCreatedAtDesc, countByUserId
- `LegacyGPTUsageLogRepository` (interface) repository/LegacyGPTUsageLogRepository.java · extends/implements JpaRepository, LegacyGPTUsageLogRepositoryCustom · public: findAllByOrderByCreatedDateDesc

### custom (4)

- `LegacyChatQuestionRepositoryCustom` (interface) repository/LegacyChatQuestionRepositoryCustom.java · public: findRecentQuestionsByUserId, findRecentQuestionsByClientIp, searchQuestionsByKeyword
- `LegacyChatQuestionRepositoryImpl` repository/LegacyChatQuestionRepositoryImpl.java · extends/implements LegacyChatQuestionRepositoryCustom · public: findRecentQuestionsByUserId, findRecentQuestionsByClientIp, searchQuestionsByKeyword · 주입: JPAQueryFactory
- `LegacyGPTUsageLogRepositoryCustom` (interface) repository/LegacyGPTUsageLogRepositoryCustom.java · public: getTotalTokensUsedBetweenDates, getTokenUsageByModelBetweenDates, getDailyTokenUsage, getTokenUsageByRequestTypeBetweenDates, getTotalTokensUsedByUser
- `LegacyGPTUsageLogRepositoryImpl` repository/LegacyGPTUsageLogRepositoryImpl.java · extends/implements LegacyGPTUsageLogRepositoryCustom · public: getTotalTokensUsedBetweenDates, getTokenUsageByModelBetweenDates, getDailyTokenUsage, getTokenUsageByRequestTypeBetweenDates, getTotalTokensUsedByUser · 주입: JPAQueryFactory

### aop (5)

- `LegacyChatAuditAspect` aop/LegacyChatAuditAspect.java · public: handleAuditLogging, auditGPTApiCall, auditAdminAccess, auditHighTokenUsage
- `LegacyChatExceptionAspect` aop/LegacyChatExceptionAspect.java · public: handleAnnotatedException, handleServiceException, handleControllerException
- `LegacyChatLoggingAspect` aop/LegacyChatLoggingAspect.java · public: handleAnnotatedLogging, logServiceMethodEntry, logControllerMethodEntry
- `LegacyChatPerformanceAspect` aop/LegacyChatPerformanceAspect.java · public: monitorAnnotatedPerformance, monitorControllerPerformance, monitorServicePerformance
- `LegacyChatSecurityAspect` aop/LegacyChatSecurityAspect.java · public: handleSecurityChecks · 주입: LegacyUserQueryService

### other (5)

- `LegacyChatAudit` (@interface) aop/annotation/LegacyChatAudit.java
- `LegacyChatExceptionHandling` (@interface) aop/annotation/LegacyChatExceptionHandling.java
- `LegacyChatLogging` (@interface) aop/annotation/LegacyChatLogging.java
- `LegacyChatPerformance` (@interface) aop/annotation/LegacyChatPerformance.java
- `LegacyChatSecurity` (@interface) aop/annotation/LegacyChatSecurity.java

### 다른 도메인 참조 (import·주입 기준)

- **chatQuestion**: `ChatQuestion` ← LegacyChatAuditAspect, LegacyChatQuestionRepository, LegacyChatQuestionRepositoryCustom, LegacyChatQuestionRepositoryImpl, LegacyChatService, LegacyGPTUsageService; `GPTUsageLog` ← LegacyGPTUsageLogRepository, LegacyGPTUsageService
- **legacyfaq**: `LegacyFAQQueryService` ← LegacyChatService
- **legacyuser**: `LegacyUserQueryService` ← LegacyChatSecurityAspect, LegacyChatService, LegacyGPTUsageController
- **user**: `User` ← LegacyChatSecurityAspect, LegacyChatService, LegacyGPTUsageService

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

- `LegacyChatAuditAspect#handleAuditLogging` @Around("@annotation(chatAudit)") · 바인딩: `@LegacyChatAudit`
- `LegacyChatAuditAspect#auditGPTApiCall` @AfterReturning(pointcut = "execution(* com.fream.back.domain.legacychatquestion.service.LegacyGPTService.getGPTResponseWithUsage(..))", returning = "result")
- `LegacyChatAuditAspect#auditAdminAccess` @Before("execution(* com.fream.back.domain.legacychatquestion.controller.LegacyGPTUsageController.*(..))")
- `LegacyChatAuditAspect#auditHighTokenUsage` @AfterReturning(pointcut = "execution(* com.fream.back.domain.legacychatquestion.service.LegacyChatService.processQuestion(..))", returning = "result")
- `LegacyChatExceptionAspect#handleAnnotatedException` @Around("@annotation(exceptionHandling)") · 바인딩: `@LegacyChatExceptionHandling`
- `LegacyChatExceptionAspect#handleServiceException` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.legacychatquestion.service..*(..))", throwing = "exception")
- `LegacyChatExceptionAspect#handleControllerException` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.legacychatquestion.controller..*(..))", throwing = "exception")
- `LegacyChatLoggingAspect#handleAnnotatedLogging` @Around("@annotation(chatLogging)") · 바인딩: `@LegacyChatLogging`
- `LegacyChatLoggingAspect#logServiceMethodEntry` @Before("execution(* com.fream.back.domain.legacychatquestion.service..*(..))")
- `LegacyChatLoggingAspect#logControllerMethodEntry` @Before("execution(* com.fream.back.domain.legacychatquestion.controller..*(..))")
- `LegacyChatPerformanceAspect#monitorAnnotatedPerformance` @Around("@annotation(chatPerformance)") · 바인딩: `@LegacyChatPerformance`
- `LegacyChatPerformanceAspect#monitorControllerPerformance` @Around("execution(* com.fream.back.domain.legacychatquestion.controller..*(..))")
- `LegacyChatPerformanceAspect#monitorServicePerformance` @Around("execution(* com.fream.back.domain.legacychatquestion.service..*(..))")
- `LegacyChatSecurityAspect#handleSecurityChecks` @Around("@annotation(chatSecurity)") · 바인딩: `@LegacyChatSecurity`

어노테이션 적용 지점(메서드에 붙은 곳):

- `@LegacyChatAudit`: (없음)
- `@LegacyChatExceptionHandling`: (없음)
- `@LegacyChatLogging`: (없음)
- `@LegacyChatPerformance`: (없음)
- `@LegacyChatSecurity`: (없음)

