# settlementchatquestion 도메인

기준 경로: `src/main/java/com/fream/back/domain/settlementchatquestion/` · 파일 21개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/settlement/chat/question` | `SettlementChatController#askQuestion` | `this#validateQuestionRequest`, `SettlementChatService#processQuestion` |
| GET | `/settlement/chat/history` | `SettlementChatController#getChatHistory` | `SettlementChatService#getChatHistory` |
| GET | `/settlement/chat/history/count` | `SettlementChatController#getChatHistoryPageCount` | `SettlementChatService#getChatHistoryPageCount` |
| GET | `/settlement/chat/recent` | `SettlementChatController#getRecentQuestions` | `SettlementChatService#getRecentQuestions` |
| GET | `/settlement/admin/gpt/stats` | `SettlementGPTUsageController#getUsageStatistics` | `SettlementUserQueryService#checkAdminRole`, `this#validateDateRange`, `SettlementGPTUsageService#getUsageStatistics` |
| GET | `/settlement/admin/gpt/logs` | `SettlementGPTUsageController#getUsageLogs` | `SettlementUserQueryService#checkAdminRole`, `SettlementGPTUsageService#getUsageLogs` |
| GET | `/settlement/admin/gpt/total-tokens` | `SettlementGPTUsageController#getTotalTokensUsed` | `SettlementUserQueryService#checkAdminRole`, `SettlementGPTUsageService#getTotalTokensUsed` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `SettlementChatAuditAspect`
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
- `SettlementChatExceptionAspect`
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
- `SettlementChatLoggingAspect`
  - `handleAnnotatedLogging` → this#extractUserEmailSafely, this#generateRequestId, this#trackUserSession, this#logBefore, this#logAfter, this#trackGPTApiCall, this#logException
  - `logServiceMethodEntry` → this#extractUserEmailSafely
  - `logControllerMethodEntry` → this#extractUserEmailSafely, this#formatArgsForLogging
  - `logBefore` → this#formatArgsForLogging, this#extractQuestionSummary, this#logAtLevel
  - `logAfter` → this#extractTokenUsageInfo, this#extractResponseSummary, this#logAtLevel
  - `logException` → this#formatArgsForLogging
  - `formatArgsForLogging` → this#maskEmail
- `SettlementChatPerformanceAspect`
  - `monitorAnnotatedPerformance` → this#extractUserEmailSafely, this#getDefaultWarningThreshold, this#getDefaultErrorThreshold, this#executeAdvancedPerformanceMonitoring
  - `monitorControllerPerformance` → this#monitorMethodPerformance
  - `monitorServicePerformance` → this#monitorMethodPerformance
  - `executeAdvancedPerformanceMonitoring` → this#extractTokenUsage, this#evaluateResponseQuality, this#updatePerformanceStats, this#logAdvancedPerformanceMetrics, this#shouldGeneratePerformanceReport, this#generatePerformanceReport
  - `monitorMethodPerformance` → this#extractUserEmailSafely, this#extractTokenUsage, this#updatePerformanceStats, this#logPerformanceMetrics, this#shouldGeneratePerformanceReport, this#generatePerformanceReport
- `SettlementChatSecurityAspect`
  - `handleSecurityChecks` → this#getCurrentRequest, this#getClientIpAddress, this#performAuthenticationCheck, this#performSecurityCheck, this#logSecurityAudit, this#handleSecurityViolation
  - `performAuthenticationCheck` → SettlementUserQueryService#checkAdminRole, this#checkUserRoles, this#handleViolationAction
  - `performSecurityCheck` → this#handleViolationAction, this#performAuthorizationCheck, this#checkRateLimit, this#checkIpRestriction, this#checkTimeRestriction, this#checkTokenLimit, this#checkContentFilter, this#checkSpamDetection, this#checkRepeatedQuestion, this#checkSensitiveDataRequest
  - `performAuthorizationCheck` → this#checkUserRoles
  - `checkUserRoles` → SettlementUserQueryService#findByEmail
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
- `SettlementChatService`
  - `processQuestion` → this#validateQuestionRequest, SettlementUserQueryService#findByEmail, SettlementFAQQueryService#getAllFAQs, SettlementGPTService#getGPTResponseWithUsage, this#saveChatQuestion, SettlementGPTUsageService#logGPTUsage
  - `saveChatQuestion` → SettlementChatQuestionRepository#save~
  - `getChatHistory` → SettlementUserQueryService#findByEmail, SettlementChatQuestionRepository#findByUserIdOrderByCreatedAtDesc
  - `getChatHistoryPageCount` → SettlementUserQueryService#findByEmail, SettlementChatQuestionRepository#countByUserId
  - `getRecentQuestions` → SettlementUserQueryService#findByEmail, SettlementChatQuestionRepository#findRecentQuestionsByUserId
- `SettlementGPTService`
  - `getGPTResponse` → this#validateQuestion, this#getGPTResponseWithUsage
  - `getGPTResponseWithUsage` → this#validateQuestion, this#truncateQuestion, this#formatFAQData, GPTConfig#getModel, this#createHttpRequestEntity, this#callGptApi, this#logApiResponse
  - `createHttpRequestEntity` → GPTConfig#getApiKey
  - `callGptApi` → GPTConfig#getApiUrl
- `SettlementGPTUsageService`
  - `logGPTUsage` → SettlementGPTUsageLogRepository#save~
  - `getUsageStatistics` → this#validateDateRange, SettlementGPTUsageLogRepository#getTotalTokensUsedBetweenDates, SettlementGPTUsageLogRepository#getDailyTokenUsage, SettlementGPTUsageLogRepository#getTokenUsageByModelBetweenDates, SettlementGPTUsageLogRepository#getTokenUsageByRequestTypeBetweenDates, this#calculateEstimatedCost
  - `getUsageLogs` → SettlementGPTUsageLogRepository#findAllByOrderByCreatedDateDesc
  - `convertToDto` → this#extractQuestionContent
  - `getTotalTokensUsed` → SettlementGPTUsageLogRepository#getTotalTokensUsedBetweenDates

## 3. 클래스 목록 (역할별)

### controller (2)

- `SettlementChatController` controller/SettlementChatController.java · public: askQuestion, getChatHistory, getChatHistoryPageCount, getRecentQuestions · 주입: SettlementChatService
- `SettlementGPTUsageController` controller/SettlementGPTUsageController.java · public: getUsageStatistics, getUsageLogs, getTotalTokensUsed · 주입: SettlementGPTUsageService, SettlementUserQueryService

### service (3)

- `SettlementChatService` service/SettlementChatService.java · public: processQuestion, getChatHistory, getChatHistoryPageCount, getRecentQuestions · 주입: SettlementChatQuestionRepository, SettlementFAQQueryService, SettlementGPTService, SettlementUserQueryService, SettlementGPTUsageService
- `SettlementGPTService` service/SettlementGPTService.java · public: getGPTResponse, getGPTResponseWithUsage · 주입: RestTemplate, GPTConfig
- `SettlementGPTUsageService` service/SettlementGPTUsageService.java · public: logGPTUsage, getUsageStatistics, getUsageLogs, getTotalTokensUsed · 주입: SettlementGPTUsageLogRepository

### repository (2)

- `SettlementChatQuestionRepository` (interface) repository/SettlementChatQuestionRepository.java · extends/implements JpaRepository, SettlementChatQuestionRepositoryCustom · public: findByUserIdOrderByCreatedAtDesc, countByUserId
- `SettlementGPTUsageLogRepository` (interface) repository/SettlementGPTUsageLogRepository.java · extends/implements JpaRepository, SettlementGPTUsageLogRepositoryCustom · public: findAllByOrderByCreatedDateDesc

### custom (4)

- `SettlementChatQuestionRepositoryCustom` (interface) repository/SettlementChatQuestionRepositoryCustom.java · public: findRecentQuestionsByUserId, findRecentQuestionsByClientIp, searchQuestionsByKeyword
- `SettlementChatQuestionRepositoryImpl` repository/SettlementChatQuestionRepositoryImpl.java · extends/implements SettlementChatQuestionRepositoryCustom · public: findRecentQuestionsByUserId, findRecentQuestionsByClientIp, searchQuestionsByKeyword · 주입: JPAQueryFactory
- `SettlementGPTUsageLogRepositoryCustom` (interface) repository/SettlementGPTUsageLogRepositoryCustom.java · public: getTotalTokensUsedBetweenDates, getTokenUsageByModelBetweenDates, getDailyTokenUsage, getTokenUsageByRequestTypeBetweenDates, getTotalTokensUsedByUser
- `SettlementGPTUsageLogRepositoryImpl` repository/SettlementGPTUsageLogRepositoryImpl.java · extends/implements SettlementGPTUsageLogRepositoryCustom · public: getTotalTokensUsedBetweenDates, getTokenUsageByModelBetweenDates, getDailyTokenUsage, getTokenUsageByRequestTypeBetweenDates, getTotalTokensUsedByUser · 주입: JPAQueryFactory

### aop (5)

- `SettlementChatAuditAspect` aop/SettlementChatAuditAspect.java · public: handleAuditLogging, auditGPTApiCall, auditAdminAccess, auditHighTokenUsage
- `SettlementChatExceptionAspect` aop/SettlementChatExceptionAspect.java · public: handleAnnotatedException, handleServiceException, handleControllerException
- `SettlementChatLoggingAspect` aop/SettlementChatLoggingAspect.java · public: handleAnnotatedLogging, logServiceMethodEntry, logControllerMethodEntry
- `SettlementChatPerformanceAspect` aop/SettlementChatPerformanceAspect.java · public: monitorAnnotatedPerformance, monitorControllerPerformance, monitorServicePerformance
- `SettlementChatSecurityAspect` aop/SettlementChatSecurityAspect.java · public: handleSecurityChecks · 주입: SettlementUserQueryService

### other (5)

- `SettlementChatAudit` (@interface) aop/annotation/SettlementChatAudit.java
- `SettlementChatExceptionHandling` (@interface) aop/annotation/SettlementChatExceptionHandling.java
- `SettlementChatLogging` (@interface) aop/annotation/SettlementChatLogging.java
- `SettlementChatPerformance` (@interface) aop/annotation/SettlementChatPerformance.java
- `SettlementChatSecurity` (@interface) aop/annotation/SettlementChatSecurity.java

### 다른 도메인 참조 (import·주입 기준)

- **chatQuestion**: `ChatQuestion` ← SettlementChatAuditAspect, SettlementChatQuestionRepository, SettlementChatQuestionRepositoryCustom, SettlementChatQuestionRepositoryImpl, SettlementChatService, SettlementGPTUsageService; `GPTUsageLog` ← SettlementGPTUsageLogRepository, SettlementGPTUsageService
- **settlementfaq**: `SettlementFAQQueryService` ← SettlementChatService
- **settlementuser**: `SettlementUserQueryService` ← SettlementChatSecurityAspect, SettlementChatService, SettlementGPTUsageController
- **user**: `User` ← SettlementChatSecurityAspect, SettlementChatService, SettlementGPTUsageService

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

- `SettlementChatAuditAspect#handleAuditLogging` @Around("@annotation(chatAudit)") · 바인딩: `@SettlementChatAudit`
- `SettlementChatAuditAspect#auditGPTApiCall` @AfterReturning(pointcut = "execution(* com.fream.back.domain.settlementchatquestion.service.SettlementGPTService.getGPTResponseWithUsage(..))", returning = "result")
- `SettlementChatAuditAspect#auditAdminAccess` @Before("execution(* com.fream.back.domain.settlementchatquestion.controller.SettlementGPTUsageController.*(..))")
- `SettlementChatAuditAspect#auditHighTokenUsage` @AfterReturning(pointcut = "execution(* com.fream.back.domain.settlementchatquestion.service.SettlementChatService.processQuestion(..))", returning = "result")
- `SettlementChatExceptionAspect#handleAnnotatedException` @Around("@annotation(exceptionHandling)") · 바인딩: `@SettlementChatExceptionHandling`
- `SettlementChatExceptionAspect#handleServiceException` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.settlementchatquestion.service..*(..))", throwing = "exception")
- `SettlementChatExceptionAspect#handleControllerException` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.settlementchatquestion.controller..*(..))", throwing = "exception")
- `SettlementChatLoggingAspect#handleAnnotatedLogging` @Around("@annotation(chatLogging)") · 바인딩: `@SettlementChatLogging`
- `SettlementChatLoggingAspect#logServiceMethodEntry` @Before("execution(* com.fream.back.domain.settlementchatquestion.service..*(..))")
- `SettlementChatLoggingAspect#logControllerMethodEntry` @Before("execution(* com.fream.back.domain.settlementchatquestion.controller..*(..))")
- `SettlementChatPerformanceAspect#monitorAnnotatedPerformance` @Around("@annotation(chatPerformance)") · 바인딩: `@SettlementChatPerformance`
- `SettlementChatPerformanceAspect#monitorControllerPerformance` @Around("execution(* com.fream.back.domain.settlementchatquestion.controller..*(..))")
- `SettlementChatPerformanceAspect#monitorServicePerformance` @Around("execution(* com.fream.back.domain.settlementchatquestion.service..*(..))")
- `SettlementChatSecurityAspect#handleSecurityChecks` @Around("@annotation(chatSecurity)") · 바인딩: `@SettlementChatSecurity`

어노테이션 적용 지점(메서드에 붙은 곳):

- `@SettlementChatAudit`: (없음)
- `@SettlementChatExceptionHandling`: (없음)
- `@SettlementChatLogging`: (없음)
- `@SettlementChatPerformance`: (없음)
- `@SettlementChatSecurity`: (없음)

