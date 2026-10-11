# monitoring 도메인

기준 경로: `src/main/java/com/fream/back/domain/monitoring/` · 파일 43개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/chat/question` | `ChatController#askQuestion` | `this#validateQuestionRequest`, `ChatService#processQuestion` |
| GET | `/chat/history` | `ChatController#getChatHistory` | `ChatService#getChatHistory` |
| GET | `/chat/history/count` | `ChatController#getChatHistoryPageCount` | `ChatService#getChatHistoryPageCount` |
| GET | `/chat/recent` | `ChatController#getRecentQuestions` | `ChatService#getRecentQuestions` |
| GET | `/admin/gpt/stats` | `GPTUsageController#getUsageStatistics` | `ProductQueryService#checkAdminRole`, `this#validateDateRange`, `GPTUsageService#getUsageStatistics` |
| GET | `/admin/gpt/logs` | `GPTUsageController#getUsageLogs` | `ProductQueryService#checkAdminRole`, `GPTUsageService#getUsageLogs` |
| GET | `/admin/gpt/total-tokens` | `GPTUsageController#getTotalTokensUsed` | `ProductQueryService#checkAdminRole`, `GPTUsageService#getTotalTokensUsed` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `ChatAuditAspect`
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
- `ChatExceptionAspect`
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
- `ChatLoggingAspect`
  - `handleAnnotatedLogging` → this#extractUserEmailSafely, this#generateRequestId, this#trackUserSession, this#logBefore, this#logAfter, this#trackGPTApiCall, this#logException
  - `logServiceMethodEntry` → this#extractUserEmailSafely
  - `logControllerMethodEntry` → this#extractUserEmailSafely, this#formatArgsForLogging
  - `logBefore` → this#formatArgsForLogging, this#extractQuestionSummary, this#logAtLevel
  - `logAfter` → this#extractTokenUsageInfo, this#extractResponseSummary, this#logAtLevel
  - `logException` → this#formatArgsForLogging
  - `formatArgsForLogging` → this#maskEmail
- `ChatPerformanceAspect`
  - `monitorAnnotatedPerformance` → this#extractUserEmailSafely, this#getDefaultWarningThreshold, this#getDefaultErrorThreshold, this#executeAdvancedPerformanceMonitoring
  - `monitorControllerPerformance` → this#monitorMethodPerformance
  - `monitorServicePerformance` → this#monitorMethodPerformance
  - `executeAdvancedPerformanceMonitoring` → this#extractTokenUsage, this#evaluateResponseQuality, this#updatePerformanceStats, this#logAdvancedPerformanceMetrics, this#shouldGeneratePerformanceReport, this#generatePerformanceReport
  - `monitorMethodPerformance` → this#extractUserEmailSafely, this#extractTokenUsage, this#updatePerformanceStats, this#logPerformanceMetrics, this#shouldGeneratePerformanceReport, this#generatePerformanceReport
- `ChatSecurityAspect`
  - `handleSecurityChecks` → this#getCurrentRequest, this#getClientIpAddress, this#performAuthenticationCheck, this#performSecurityCheck, this#logSecurityAudit, this#handleSecurityViolation
  - `performAuthenticationCheck` → ProductQueryService#checkAdminRole, this#checkUserRoles, this#handleViolationAction
  - `performSecurityCheck` → this#handleViolationAction, this#performAuthorizationCheck, this#checkRateLimit, this#checkIpRestriction, this#checkTimeRestriction, this#checkTokenLimit, this#checkContentFilter, this#checkSpamDetection, this#checkRepeatedQuestion, this#checkSensitiveDataRequest
  - `performAuthorizationCheck` → this#checkUserRoles
  - `checkUserRoles` → ProductQueryService#findByEmail
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
- `ChatService`
  - `processQuestion` → this#validateQuestionRequest, ProductQueryService#findByEmail, OrderQueryService#getAllFAQs, GPTService#getGPTResponseWithUsage, this#saveChatQuestion, GPTUsageService#logGPTUsage
  - `saveChatQuestion` → MonitoringRepository#save~
  - `getChatHistory` → ProductQueryService#findByEmail, MonitoringRepository#findByUserIdOrderByCreatedAtDesc
  - `getChatHistoryPageCount` → ProductQueryService#findByEmail, MonitoringRepository#countByUserId
  - `getRecentQuestions` → ProductQueryService#findByEmail, MonitoringRepository#findRecentQuestionsByUserId
- `GPTService`
  - `getGPTResponse` → this#validateQuestion, this#getGPTResponseWithUsage
  - `getGPTResponseWithUsage` → this#validateQuestion, this#truncateQuestion, this#formatFAQData, GPTConfig#getModel, this#createHttpRequestEntity, this#callGptApi, this#logApiResponse
  - `createHttpRequestEntity` → GPTConfig#getApiKey
  - `callGptApi` → GPTConfig#getApiUrl
- `GPTUsageService`
  - `logGPTUsage` → GPTUsageLogRepository#save~
  - `getUsageStatistics` → this#validateDateRange, GPTUsageLogRepository#getTotalTokensUsedBetweenDates, GPTUsageLogRepository#getDailyTokenUsage, GPTUsageLogRepository#getTokenUsageByModelBetweenDates, GPTUsageLogRepository#getTokenUsageByRequestTypeBetweenDates, this#calculateEstimatedCost
  - `getUsageLogs` → GPTUsageLogRepository#findAllByOrderByCreatedDateDesc
  - `convertToDto` → this#extractQuestionContent
  - `getTotalTokensUsed` → GPTUsageLogRepository#getTotalTokensUsedBetweenDates

## 3. 클래스 목록 (역할별)

### controller (2)

- `ChatController` controller/ChatController.java · public: askQuestion, getChatHistory, getChatHistoryPageCount, getRecentQuestions · 주입: ChatService
- `GPTUsageController` controller/GPTUsageController.java · public: getUsageStatistics, getUsageLogs, getTotalTokensUsed · 주입: GPTUsageService, ProductQueryService

### service (3)

- `ChatService` service/ChatService.java · public: processQuestion, getChatHistory, getChatHistoryPageCount, getRecentQuestions · 주입: MonitoringRepository, OrderQueryService, GPTService, ProductQueryService, GPTUsageService
- `GPTService` service/GPTService.java · public: getGPTResponse, getGPTResponseWithUsage · 주입: RestTemplate, GPTConfig
- `GPTUsageService` service/GPTUsageService.java · public: logGPTUsage, getUsageStatistics, getUsageLogs, getTotalTokensUsed · 주입: GPTUsageLogRepository

### repository (2)

- `GPTUsageLogRepository` (interface) repository/GPTUsageLogRepository.java · extends/implements JpaRepository, GPTUsageLogRepositoryCustom · public: findAllByOrderByCreatedDateDesc
- `MonitoringRepository` (interface) repository/MonitoringRepository.java · extends/implements JpaRepository, MonitoringRepositoryCustom · public: findByUserIdOrderByCreatedAtDesc, countByUserId

### custom (4)

- `GPTUsageLogRepositoryCustom` (interface) repository/GPTUsageLogRepositoryCustom.java · public: getTotalTokensUsedBetweenDates, getTokenUsageByModelBetweenDates, getDailyTokenUsage, getTokenUsageByRequestTypeBetweenDates, getTotalTokensUsedByUser
- `GPTUsageLogRepositoryImpl` repository/GPTUsageLogRepositoryImpl.java · extends/implements GPTUsageLogRepositoryCustom · public: getTotalTokensUsedBetweenDates, getTokenUsageByModelBetweenDates, getDailyTokenUsage, getTokenUsageByRequestTypeBetweenDates, getTotalTokensUsedByUser · 주입: JPAQueryFactory
- `MonitoringRepositoryCustom` (interface) repository/MonitoringRepositoryCustom.java · public: findRecentQuestionsByUserId, findRecentQuestionsByClientIp, searchQuestionsByKeyword
- `MonitoringRepositoryImpl` repository/MonitoringRepositoryImpl.java · extends/implements MonitoringRepositoryCustom · public: findRecentQuestionsByUserId, findRecentQuestionsByClientIp, searchQuestionsByKeyword · 주입: JPAQueryFactory

### entity (2)

- `GPTUsageLog` entity/GPTUsageLog.java · extends/implements BaseTimeEntity
- `Monitoring` entity/Monitoring.java · extends/implements BaseTimeEntity · public: setAnswer

### aop (5)

- `ChatAuditAspect` aop/ChatAuditAspect.java · public: handleAuditLogging, auditGPTApiCall, auditAdminAccess, auditHighTokenUsage
- `ChatExceptionAspect` aop/ChatExceptionAspect.java · public: handleAnnotatedException, handleServiceException, handleControllerException
- `ChatLoggingAspect` aop/ChatLoggingAspect.java · public: handleAnnotatedLogging, logServiceMethodEntry, logControllerMethodEntry
- `ChatPerformanceAspect` aop/ChatPerformanceAspect.java · public: monitorAnnotatedPerformance, monitorControllerPerformance, monitorServicePerformance
- `ChatSecurityAspect` aop/ChatSecurityAspect.java · public: handleSecurityChecks · 주입: ProductQueryService

### dto (13)

- `ChatHistoryDto` dto/chat/ChatHistoryDto.java
- `QuestionRequestDto` dto/chat/QuestionRequestDto.java
- `QuestionResponseDto` dto/chat/QuestionResponseDto.java
- `GPTChoiceDto` dto/gpt/GPTChoiceDto.java
- `GPTMessageDto` dto/gpt/GPTMessageDto.java
- `GPTRequestDto` dto/gpt/GPTRequestDto.java
- `GPTResponseDto` dto/gpt/GPTResponseDto.java
- `GPTUsageDto` dto/gpt/GPTUsageDto.java
- `GPTDailyUsageDto` dto/log/GPTDailyUsageDto.java
- `GPTModelUsageDto` dto/log/GPTModelUsageDto.java
- `GPTRequestTypeUsageDto` dto/log/GPTRequestTypeUsageDto.java
- `GPTUsageLogDto` dto/log/GPTUsageLogDto.java
- `GPTUsageStatsDto` dto/log/GPTUsageStatsDto.java

### exception (7)

- `ChatPermissionException` exception/ChatPermissionException.java
- `ChatQueryException` exception/ChatQueryException.java
- `GPTApiException` exception/GPTApiException.java
- `GPTUsageException` exception/GPTUsageException.java
- `InvalidQuestionException` exception/InvalidQuestionException.java
- `MonitoringErrorCode` exception/MonitoringErrorCode.java
- `MonitoringException` exception/MonitoringException.java

### other (5)

- `ChatAudit` (@interface) aop/annotation/ChatAudit.java
- `ChatExceptionHandling` (@interface) aop/annotation/ChatExceptionHandling.java
- `ChatLogging` (@interface) aop/annotation/ChatLogging.java
- `ChatPerformance` (@interface) aop/annotation/ChatPerformance.java
- `ChatSecurity` (@interface) aop/annotation/ChatSecurity.java

### 다른 도메인 참조 (import·주입 기준)

- **order**: `OrderQueryService` ← ChatService
- **product**: `Product` ← ChatSecurityAspect, ChatService, GPTUsageLog, GPTUsageService, Monitoring; `ProductQueryService` ← ChatSecurityAspect, ChatService, GPTUsageController

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

- `GPTUsageLog` @Entity · extends BaseTimeEntity
  - user: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") → `Product` [product]
  - chatQuestion: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "chat_question_id") → `Monitoring`
- `Monitoring` @Entity · extends BaseTimeEntity
  - user: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) → `Product` [product]

## 5. 이벤트·메시징

(없음)

## 6. AOP

- `ChatAuditAspect#handleAuditLogging` @Around("@annotation(chatAudit)") · 바인딩: `@ChatAudit`
- `ChatAuditAspect#auditGPTApiCall` @AfterReturning(pointcut = "execution(* com.fream.back.domain.monitoring.service.GPTService.getGPTResponseWithUsage(..))", returning = "result")
- `ChatAuditAspect#auditAdminAccess` @Before("execution(* com.fream.back.domain.monitoring.controller.GPTUsageController.*(..))")
- `ChatAuditAspect#auditHighTokenUsage` @AfterReturning(pointcut = "execution(* com.fream.back.domain.monitoring.service.ChatService.processQuestion(..))", returning = "result")
- `ChatExceptionAspect#handleAnnotatedException` @Around("@annotation(exceptionHandling)") · 바인딩: `@ChatExceptionHandling`
- `ChatExceptionAspect#handleServiceException` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.monitoring.service..*(..))", throwing = "exception")
- `ChatExceptionAspect#handleControllerException` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.monitoring.controller..*(..))", throwing = "exception")
- `ChatLoggingAspect#handleAnnotatedLogging` @Around("@annotation(chatLogging)") · 바인딩: `@ChatLogging`
- `ChatLoggingAspect#logServiceMethodEntry` @Before("execution(* com.fream.back.domain.monitoring.service..*(..))")
- `ChatLoggingAspect#logControllerMethodEntry` @Before("execution(* com.fream.back.domain.monitoring.controller..*(..))")
- `ChatPerformanceAspect#monitorAnnotatedPerformance` @Around("@annotation(chatPerformance)") · 바인딩: `@ChatPerformance`
- `ChatPerformanceAspect#monitorControllerPerformance` @Around("execution(* com.fream.back.domain.monitoring.controller..*(..))")
- `ChatPerformanceAspect#monitorServicePerformance` @Around("execution(* com.fream.back.domain.monitoring.service..*(..))")
- `ChatSecurityAspect#handleSecurityChecks` @Around("@annotation(chatSecurity)") · 바인딩: `@ChatSecurity`

어노테이션 적용 지점(메서드에 붙은 곳):

- `@ChatAudit`: (없음)
- `@ChatExceptionHandling`: (없음)
- `@ChatLogging`: (없음)
- `@ChatPerformance`: (없음)
- `@ChatSecurity`: (없음)

