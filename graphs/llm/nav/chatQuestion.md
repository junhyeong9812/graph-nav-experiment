# chatQuestion 도메인 (GPT 기반 FAQ 상담 챗봇 + 사용량 로그)

## 1. 개요
- 로그인 사용자의 질문을 받아 **FAQ 전체 목록을 시스템 프롬프트에 주입**한 뒤 OpenAI Chat Completions API(RestTemplate)로 답변을 생성, `ChatQuestion`으로 저장하고 토큰 사용량을 `GPTUsageLog`에 기록.
- 관리자용 GPT 사용량 통계/로그/총 토큰 조회 API 제공.
- 도메인 AOP 5종(로깅/성능/감사/예외/보안) 존재.

## 2. 패키지/파일 구조
루트: `src/main/java/com/fream/back/domain/chatQuestion/`
| 경로 | 역할 |
|---|---|
| `controller/ChatController.java` | `/chat/**` 사용자 API |
| `controller/GPTUsageController.java` | `/admin/gpt/**` 관리자 API |
| `service/ChatService.java` | 질문 처리, 기록 조회 |
| `service/GPTService.java` | OpenAI 호출 |
| `service/GPTUsageService.java` | 사용량 기록/통계 |
| `entity/ChatQuestion.java`, `entity/GPTUsageLog.java` | 엔티티 |
| `repository/ChatQuestionRepository{,Custom,Impl}.java` | JPA + QueryDSL |
| `repository/GPTUsageLogRepository{,Custom,Impl}.java` | JPA + QueryDSL 집계 |
| `dto/chat/` | `QuestionRequestDto`, `QuestionResponseDto`, `ChatHistoryDto` |
| `dto/gpt/` | OpenAI 요청/응답 매핑: `GPTRequestDto`(프롬프트 생성 `of(...)`), `GPTMessageDto`, `GPTResponseDto`(`getAnswer()`), `GPTChoiceDto`, `GPTUsageDto` |
| `dto/log/` | `GPTUsageStatsDto`, `GPTDailyUsageDto`, `GPTModelUsageDto`, `GPTRequestTypeUsageDto`, `GPTUsageLogDto` |
| `aop/Chat{Logging,Performance,Audit,Exception,Security}Aspect.java`, `aop/annotation/Chat*.java` | AOP |
| `exception/*` | `ChatQuestionErrorCode`, `ChatQuestionException` 및 하위 5종 |

관련 global: `src/main/java/com/fream/back/global/config/GPTConfig.java`, `src/main/java/com/fream/back/global/config/RestTemplateConfig.java`

## 3. 엔티티
- `ChatQuestion extends BaseTimeEntity`
  - `id` IDENTITY, `question`(not null), `answer`(TEXT, not null), `user` `@ManyToOne(LAZY)` `user_id` not null → `User`, `isAnswered`, `createdAt`(자체 `@PrePersist onCreateAt`), `clientIp`
  - `setAnswer(answer)` → isAnswered=true
- `GPTUsageLog extends BaseTimeEntity`
  - `id`, `user` `@ManyToOne(LAZY)` nullable(시스템 사용 시 null), `requestType`(예: `"FAQ_CHAT"`), `promptTokens`, `completionTokens`, `totalTokens`, `modelName`, `requestId`(OpenAI 응답 id), `chatQuestion` `@ManyToOne(LAZY)` `chat_question_id`
- enum 없음 (requestType은 문자열).

## 4. API 엔드포인트
| Method | Path | Controller#method | Service#method | 권한 |
|---|---|---|---|---|
| POST | `/chat/question` | `ChatController#askQuestion` | `ChatService#processQuestion` | `@PreAuthorize("isAuthenticated()")` |
| GET | `/chat/history` (pageable, 기본 size=2) | `ChatController#getChatHistory` | `ChatService#getChatHistory` | 인증 |
| GET | `/chat/history/count?size=2` | `ChatController#getChatHistoryPageCount` | `ChatService#getChatHistoryPageCount` | 인증 |
| GET | `/chat/recent?limit=5` (1~20) | `ChatController#getRecentQuestions` | `ChatService#getRecentQuestions` | 인증 |
| GET | `/admin/gpt/stats?startDate&endDate` (ISO date) | `GPTUsageController#getUsageStatistics` | `GPTUsageService#getUsageStatistics` | 인증 + `UserQueryService#checkAdminRole(email)` |
| GET | `/admin/gpt/logs` (기본 size=20, sort=createdDate) | `GPTUsageController#getUsageLogs` | `GPTUsageService#getUsageLogs` | 관리자 |
| GET | `/admin/gpt/total-tokens` | `GPTUsageController#getTotalTokensUsed` | `GPTUsageService#getTotalTokensUsed` | 관리자 |

- 응답은 `ResponseDto.success(...)` 래핑. 질문 최대 2000자(컨트롤러 `MAX_QUESTION_LENGTH`, 서비스, GPTService 3중 체크).
- `@PreAuthorize`는 `global/config/LogViewerConfig.java`의 `@EnableMethodSecurity`로 활성화.
- `/admin/gpt/stats` 날짜 검증: null 불가, start ≤ end, 최대 6개월(`GPTUsageController#validateDateRange`).

## GPT 질문 흐름
1. `POST /chat/question` → `ChatController#askQuestion` → `validateQuestionRequest` → `SecurityUtils.extractEmailFromSecurityContext()`
2. `ChatService#processQuestion` (`@Transactional`)
   - `UserQueryService#findByEmail(email)` (`domain/user/service/query/UserQueryService.java`)
   - **FAQ 주입 데이터**: `FAQQueryService#getAllFAQs()` (`domain/faq/service/query/FAQQueryService.java` L130) — 페이징 없이 FAQ 전체
3. `GPTService#getGPTResponseWithUsage(question, faqList)`
   - `formatFAQData`: 각 FAQ를 `"카테고리: …\n질문: …\n답변: …\n"` 문자열로 변환
   - `GPTRequestDto.of(model, question, faqData, true)`:
     - system 메시지: `"당신은 온라인 쇼핑몰 상담 도우미입니다. 다음 FAQ 데이터를 기반으로…"` + `FAQ 데이터:` + 모든 FAQ + (answerUnknownQuestions=true) FAQ에 없으면 일반지식 답변하되 `'해당 질문에 대한 정확한 정보는 FAQ에 없지만, 제가 알고 있는 정보로는...'` 문구로 시작, 불확실하면 고객센터 안내
     - user 메시지: 질문 원문
     - `temperature=0.7`, `max_tokens=800`
   - `createHttpRequestEntity`: JSON + `Bearer ${openai.api.key}`
   - `callGptApi`: `restTemplate.postForObject(openai.api.url, …, GPTResponseDto.class)`; 429 → `GPT_USAGE_LIMIT_EXCEEDED`, 401/기타 4xx/5xx/네트워크 → `GPT_API_ERROR`
   - `logApiResponse`: 응답 null이면 `GPT_RESPONSE_PROCESSING_ERROR`
   - 답변 = `GPTResponseDto#getAnswer()` (choices[0].message.content, 없으면 고정 오류 문구)
4. `ChatService#saveChatQuestion` → `ChatQuestionRepository#save` (isAnswered=true)
5. `GPTUsageService#logGPTUsage(response, user, "FAQ_CHAT", chatQuestion)` → `GPTUsageLog` 저장 (prompt/completion/total tokens, model, requestId). 예외는 내부에서 삼킴.
6. `QuestionResponseDto(question, answer, createdAt)` 반환

설정 (`GPTConfig`): `openai.api.key`(필수, prod=`${OPENAPI_KEY}`), `openai.model`(기본 `gpt-3.5-turbo`), `openai.api.url`(기본 `https://api.openai.com/v1/chat/completions`). `application-prod.yml` L169-173.

## 5. 서비스 계층
- `ChatService`: `processQuestion`, `getChatHistory`(`findByUserIdOrderByCreatedAtDesc`), `getChatHistoryPageCount`(`countByUserId`/size 올림), `getRecentQuestions`(QueryDSL)
- `GPTService`: `getGPTResponse`(문자열 반환, 예외 시 오류 문자열 반환 — 현재 도메인 내 호출처 없음), `getGPTResponseWithUsage`
- `GPTUsageService`: `logGPTUsage`, `getUsageStatistics(start,end)`(총 토큰, 일별, 모델별, 요청유형별 + `calculateEstimatedCost`), `getUsageLogs(pageable)`, `getTotalTokensUsed()`(2000-01-01~now 합계)

## 6. Repository
- `ChatQuestionRepository`: `findByUserIdOrderByCreatedAtDesc(userId, pageable)`, `countByUserId`
- `ChatQuestionRepositoryImpl` (QueryDSL): `findRecentQuestionsByUserId(userId, limit)`, `findRecentQuestionsByClientIp(ip, limit)`, `searchQuestionsByKeyword(keyword, pageable)` (뒤 2개는 서비스 미사용)
- `GPTUsageLogRepository`: `findAllByOrderByCreatedDateDesc(pageable)`
- `GPTUsageLogRepositoryImpl` (QueryDSL): `getTotalTokensUsedBetweenDates`, `getTokenUsageByModelBetweenDates`, `getDailyTokenUsage`(`DATE_FORMAT({0}, '%Y-%m-%d')` → MySQL 종속), `getTokenUsageByRequestTypeBetweenDates`, `getTotalTokensUsedByUser`

## 7. 외부 연동 / 비동기 / AOP
- 외부 API: OpenAI Chat Completions (위 설정 키). Kafka/스케줄러 없음.
- `GPTService#getGPTResponseWithUsage`에 `@Retryable(ResourceAccessException, HttpServerErrorException; maxAttempts=3, backoff 1000ms x2)` 선언.
- AOP (모두 `@Aspect`, `@Order` 없음):
  | Aspect | 어노테이션 advice | 패키지/메서드 기반 advice (항상 동작) |
  |---|---|---|
  | `ChatLoggingAspect` | `@Around("@annotation(chatLogging)")` | `@Before` service/controller 전체 |
  | `ChatPerformanceAspect` | `@Around("@annotation(chatPerformance)")` | `@Around` controller/service 전체 |
  | `ChatAuditAspect` | `@Around("@annotation(chatAudit)")` | `@AfterReturning` `GPTService.getGPTResponseWithUsage`, `ChatService.processQuestion`; `@Before` `GPTUsageController.*` |
  | `ChatExceptionAspect` | `@Around("@annotation(exceptionHandling)")` | `@AfterThrowing` service/controller |
  | `ChatSecurityAspect` | `@Around("@annotation(chatSecurity)")` | 없음 |
- 커스텀 어노테이션 `@ChatLogging`, `@ChatPerformance`, `@ChatAudit`, `@ChatExceptionHandling`, `@ChatSecurity`는 aop 패키지 외 **적용처 없음** → 어노테이션 기반 advice(특히 `ChatSecurityAspect`)는 미동작.

## 8. 다른 도메인과의 의존
- chatQuestion → user: `User` 엔티티(FK), `UserQueryService#findByEmail`, `#checkAdminRole`.
- chatQuestion → faq: `FAQQueryService#getAllFAQs`, `FAQResponseDto`(category/question/answer).
- chatQuestion → global: `GPTConfig`, `RestTemplate` 빈(`RestTemplateConfig`), `SecurityUtils`, `ResponseDto`, `BaseTimeEntity`.
- 다른 도메인에서 chatQuestion을 import하지 않음 (`GlobalExceptionHandler` 제외).

## 9. 예외
`ChatQuestionErrorCode`, 접두사 `CQ`:
- `CQ001` GPT_API_ERROR(500), `CQ002` GPT_RESPONSE_PROCESSING_ERROR(500), `CQ003` GPT_USAGE_LOG_ERROR(500)
- `CQ101` CHAT_QUESTION_SAVE_ERROR(500), `CQ102` CHAT_HISTORY_QUERY_ERROR(500), `CQ103` USAGE_STATS_QUERY_ERROR(500)
- `CQ201` ADMIN_PERMISSION_REQUIRED(403), `CQ202` QUESTION_PERMISSION_DENIED(403)
- `CQ301` INVALID_QUESTION_DATA(400), `CQ302` INVALID_DATE_RANGE(400), `CQ303` QUESTION_LENGTH_EXCEEDED(400), `CQ304` GPT_USAGE_LIMIT_EXCEEDED(429)
- 계층: `ChatQuestionException extends GlobalException` ← `GPTApiException`, `ChatQueryException`, `ChatPermissionException`, `InvalidQuestionException`, `GPTUsageException`.

## 10. 작업 팁 / 주의점
- **GPT 오류 코드가 덮어써짐**: `ChatService#processQuestion`(L81-92)은 `AccessDeniedException`, `InvalidQuestionException|ChatQueryException`만 그대로 전파하고 나머지는 `catch (Exception)`(L88)에서 `ChatQueryException(CHAT_QUESTION_SAVE_ERROR, CQ101)`로 래핑 → `GPTApiException`(429 → CQ304 등)은 클라이언트에 CQ101/500으로 나감.
- **@Retryable 미동작 가능성**: `src/main/java`에 `@EnableRetry` 선언이 없음(grep 결과 0). 또한 `callGptApi`가 `ResourceAccessException`/`HttpServerErrorException`을 `GPTApiException`으로 변환해 던지므로 retry 대상 예외가 메서드 밖으로 나오지 않음. (spring-retry는 build.gradle에 직접 선언 없음, import만 존재)
- `logGPTUsage`의 호출 주석은 "별도 트랜잭션"이지만 `@Transactional(rollbackFor=Exception.class)` 기본 전파(REQUIRED)라 `processQuestion` 트랜잭션에 참여, 같은 클래스 외부 호출이라 프록시는 적용됨.
- 모든 질문마다 FAQ 전체(`getAllFAQs`, FAQ 이미지 조회 포함)를 프롬프트에 넣음 → FAQ 수가 늘면 prompt 토큰/지연 증가.
- `ChatQuestion`은 `BaseTimeEntity.createdDate`와 별도로 `createdAt` 필드를 가짐. 정렬/조회는 `createdAt`(ChatQuestion), `createdDate`(GPTUsageLog) 사용. `clientIp`는 저장 로직에서 세팅하지 않음.
- `calculateEstimatedCost`는 `int`로 누적 (`tokens * 0.0018` 등을 int 캐스팅, L198-212) → 소수 비용은 0으로 절삭.
- `/admin/gpt/**`는 URL 레벨 admin 제한 없이 컨트롤러에서 `checkAdminRole` 호출로만 보호.
