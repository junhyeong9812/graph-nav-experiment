# chatQuestion 도메인 (GPT 기반 FAQ 챗봇 + GPT 사용량 로그)

> 경로 루트: `src/main/java/com/fream/back/domain/chatQuestion/` (global은 `src/main/java/com/fream/back/global/`). 코드를 읽고 확인한 사실만 담았다.

## 역할
로그인한 사용자가 질문하면 **모든 FAQ를 system prompt에 넣어** OpenAI Chat Completions API를 호출한다. 질문과 답변은 `ChatQuestion`에, 토큰 사용량은 `GPTUsageLog`에 저장한다. 관리자는 사용량 통계와 로그를 조회한다.

## 파일 맵
| 레이어 | 파일 |
|---|---|
| controller | `controller/ChatController.java`(`/chat`), `controller/GPTUsageController.java`(`/admin/gpt`) |
| service | `service/ChatService.java`(오케스트레이션), `service/GPTService.java`(OpenAI 호출), `service/GPTUsageService.java`(사용량 기록/통계) |
| repository | `ChatQuestionRepository` + `ChatQuestionRepositoryCustom` / `ChatQuestionRepositoryImpl`(QueryDSL), `GPTUsageLogRepository` + `GPTUsageLogRepositoryCustom` / `GPTUsageLogRepositoryImpl` |
| entity | `entity/ChatQuestion.java`, `entity/GPTUsageLog.java` |
| dto | `dto/chat/{QuestionRequestDto, QuestionResponseDto, ChatHistoryDto}`, `dto/gpt/{GPTRequestDto, GPTResponseDto, GPTMessageDto, GPTChoiceDto, GPTUsageDto}`, `dto/log/{GPTUsageStatsDto, GPTUsageLogDto, GPTDailyUsageDto, GPTModelUsageDto, GPTRequestTypeUsageDto}` |
| exception | `exception/ChatQuestionErrorCode.java`(CQxxx), `ChatQuestionException`(extends GlobalException). 하위 클래스: `GPTApiException`, `ChatQueryException`, `GPTUsageException`, `ChatPermissionException`, `InvalidQuestionException` |
| aop | `aop/ChatLoggingAspect`, `ChatPerformanceAspect`, `ChatExceptionAspect`, `ChatAuditAspect`, `ChatSecurityAspect` + `aop/annotation/*` |
| config | `global/config/GPTConfig.java`, `global/config/RestTemplateConfig.java` |

## REST 엔드포인트
| HTTP | 경로 | Controller | Service | 권한 |
|---|---|---|---|---|
| POST | `/chat/question` (JSON `{question}`) | `ChatController.askQuestion` | `ChatService.processQuestion(email, dto)` | `@PreAuthorize("isAuthenticated()")` |
| GET | `/chat/history` (기본 size=2) | `getChatHistory` | `ChatService.getChatHistory` | 인증 |
| GET | `/chat/history/count?size=2` | `getChatHistoryPageCount` | `getChatHistoryPageCount` (ceil(count/size)) | 인증 |
| GET | `/chat/recent?limit=5` (1~20) | `getRecentQuestions` | `getRecentQuestions` | 인증 |
| GET | `/admin/gpt/stats?startDate&endDate` (ISO DATE) | `GPTUsageController.getUsageStatistics` | `GPTUsageService.getUsageStatistics` | `userQueryService.checkAdminRole` |
| GET | `/admin/gpt/logs` (size 20, sort createdDate) | `getUsageLogs` | `getUsageLogs` → `findAllByOrderByCreatedDateDesc` | 관리자 |
| GET | `/admin/gpt/total-tokens` | `getTotalTokensUsed` | `getTotalTokensUsed`(2000-01-01부터 현재까지 합계) | 관리자 |

응답은 모두 `ResponseDto.success(...)`로 감싼다. 이메일은 `global/utils/SecurityUtils.extractEmailFromSecurityContext()`로 꺼낸다.

## 질문 처리 흐름 (`ChatService.processQuestion`, `@Transactional`)
1. 질문 검증: 비어 있음 / 2000자 초과 → `InvalidQuestionException`. 컨트롤러, ChatService, GPTService가 **같은 검증을 세 번** 한다.
2. `userQueryService.findByEmail(email)`
3. `faqQueryService.getAllFAQs()`: **FAQ 전체**를 가져온다(캐시 `faqList::all`).
4. `gptService.getGPTResponseWithUsage(question, faqList)`
   - `formatFAQData`: FAQ마다 `"카테고리: ..\n질문: ..\n답변: ..\n"` 문자열을 만든다(answer는 HTML 원문 그대로).
   - `GPTRequestDto.of(model, question, faqData, true)`:
     - system 메시지: "당신은 온라인 쇼핑몰 상담 도우미입니다. 다음 FAQ 데이터를 기반으로..." + FAQ 목록 + (`answerUnknownQuestions=true`이므로) FAQ에 없는 질문은 일반 지식으로 답하되 "해당 질문에 대한 정확한 정보는 FAQ에 없지만..."으로 시작하라는 지시
     - user 메시지: 질문
     - `temperature 0.7`, `max_tokens 800`
   - `RestTemplate.postForObject(apiUrl, entity, GPTResponseDto.class)`, 헤더는 `Bearer {apiKey}`, JSON
   - 오류 매핑: 429 → `GPT_USAGE_LIMIT_EXCEEDED`(CQ304), 401과 기타 4xx, 5xx, 연결 실패 → `GPT_API_ERROR`(CQ001), null 응답 → `GPT_RESPONSE_PROCESSING_ERROR`(CQ002)
   - 답변: `GPTResponseDto.getAnswer()` = `choices[0].message.content`. choices가 비어 있으면 "응답을 처리하는 중 오류가 발생했습니다." 문자열을 반환한다.
5. `ChatQuestion` 저장(isAnswered=true)
6. `gptUsageService.logGPTUsage(response, user, "FAQ_CHAT", chatQuestion)`: usage가 null이면 건너뛴다. prompt/completion/total 토큰, model, requestId(response.id)를 저장한다. 예외는 삼키고 상위로 전파하지 않는다.
7. `QuestionResponseDto(question, answer, createdAt)` 반환

## OpenAI 설정 (프로퍼티 키만 기록. 값은 적지 않음)
| 키 | GPTConfig 기본값 | 비고 |
|---|---|---|
| `openai.api.key` | 없음(필수) | `application-local.yml.example`에서는 `${OPENAPI_KEY:...}` 환경변수를 참조한다 |
| `openai.api.url` | `https://api.openai.com/v1/chat/completions` | |
| `openai.model` | `gpt-3.5-turbo` | `application-prod.yml`에도 같은 모델이 설정되어 있다 |

- `RestTemplateConfig`: connectTimeout 5초, readTimeout 10초(전역 공용 `RestTemplate` bean).

## 엔티티
| 엔티티 | 필드 |
|---|---|
| `ChatQuestion`(BaseTimeEntity) | question(`@Column(nullable=false)`, 길이 미지정), answer(TEXT), user(ManyToOne LAZY), isAnswered, createdAt(`@PrePersist`로 설정), clientIp(저장하는 코드 없음) |
| `GPTUsageLog`(BaseTimeEntity) | user(ManyToOne, nullable), requestType(현재 `"FAQ_CHAT"`만 사용), promptTokens, completionTokens, totalTokens, modelName, requestId, chatQuestion(ManyToOne) |

## 에러 코드 (`ChatQuestionErrorCode`)
CQ001 GPT_API_ERROR(500), CQ002 GPT_RESPONSE_PROCESSING_ERROR, CQ003 GPT_USAGE_LOG_ERROR, CQ101 CHAT_QUESTION_SAVE_ERROR, CQ102 CHAT_HISTORY_QUERY_ERROR, CQ103 USAGE_STATS_QUERY_ERROR, CQ201 ADMIN_PERMISSION_REQUIRED(403), CQ202 QUESTION_PERMISSION_DENIED(403), CQ301 INVALID_QUESTION_DATA(400), CQ302 INVALID_DATE_RANGE, CQ303 QUESTION_LENGTH_EXCEEDED, CQ304 GPT_USAGE_LIMIT_EXCEEDED(429). `GlobalExceptionHandler.handleChatQuestionException`이 처리한다.

## AOP
**커스텀 어노테이션(`@ChatLogging`, `@ChatPerformance`, `@ChatExceptionHandling`, `@ChatAudit`, `@ChatSecurity`)은 main 코드 어디에도 붙어 있지 않다.** 따라서 `@Around("@annotation(...)")` advice는 실행되지 않는다. 대신 **execution 포인트컷 advice는 실제로 동작한다**:

| Aspect | 실제로 동작하는 advice | 동작 |
|---|---|---|
| `ChatLoggingAspect` | `@Before execution(chatQuestion.service..*)`, `@Before execution(chatQuestion.controller..*)` | 서비스 진입은 DEBUG 로그, 컨트롤러 진입은 INFO 로그(인자 포함, 포맷팅/마스킹 함수 사용) |
| `ChatPerformanceAspect` | `@Around controller..*`, `@Around service..*` | 실행 시간을 측정하고 임계값을 넘으면 경고한다. 메서드명에 `GPT`나 `Api`가 들어가면 GPT용 임계값을 쓴다. 인메모리 통계 |
| `ChatExceptionAspect` | `@AfterThrowing service..*`, `controller..*` | 예외를 ERROR로 기록하고 타입별 후처리(GPTApiException / ChatQuestionException / 기타)를 한다. 예외를 삼키지 않는다 |
| `ChatAuditAspect` | `@AfterReturning GPTService.getGPTResponseWithUsage`, `@Before GPTUsageController.*`, `@AfterReturning ChatService.processQuestion` | `AUDIT_GPT_API_CALL`(모델, 토큰), `AUDIT_ADMIN_ACCESS`(IP, UA, WARN), `AUDIT_QUESTION_PROCESSED` 로그와 인메모리 카운터 |
| `ChatSecurityAspect` | (없음, `@annotation(chatSecurity)`만 있음) | 의도: rate limit, 일/월 토큰 한도, 스팸, 반복 질문, IP/시간 제한. **현재는 미적용** → 사용자별 rate limit이나 토큰 한도가 없다 |

## 다른 도메인과의 의존
- 사용: `faq`(`FAQQueryService.getAllFAQs`, `FAQResponseDto`), `user`(`UserQueryService.findByEmail/checkAdminRole`, `User`), `global/config/GPTConfig`, `global/utils/SecurityUtils`, `global/exception/GlobalException`.
- 다른 도메인이 chatQuestion을 쓰는 곳은 `GlobalExceptionHandler`뿐이다(grep으로 확인).

## 관련 테스트
- 없음(`src/test`에 chatQuestion이나 GPT 관련 테스트가 없다).

## 자주 할 작업 → 여기를 보라
| 작업 | 위치 |
|---|---|
| 프롬프트 문구, temperature, max_tokens 변경 | `dto/gpt/GPTRequestDto.of(...)` |
| FAQ를 프롬프트에 넣는 형식 | `service/GPTService.formatFAQData` |
| "FAQ에 없으면 거절" 모드로 전환 | `GPTService.getGPTResponseWithUsage`의 `GPTRequestDto.of(..., true)` → `false` |
| 모델 / URL / 키 | `openai.*` 프로퍼티 + `global/config/GPTConfig` |
| 타임아웃 | `global/config/RestTemplateConfig`(전역 공유이므로 영향 범위에 주의) |
| 사용량 통계 쿼리 | `repository/GPTUsageLogRepositoryImpl`(일별, 모델별, requestType별 합계) |
| 질문 길이 제한 변경 | `ChatController.MAX_QUESTION_LENGTH`, `ChatService.validateQuestionRequest`, `GPTService.validateQuestion` 세 곳 모두 |
| rate limit 적용 | `ChatSecurityAspect`가 있으니 `@ChatSecurity`를 `askQuestion`에 붙이는 방안을 검토한다(동작은 미검증) |

## gotchas (코드에서 확인)
- **`@Retryable`이 동작하지 않는다**: `GPTService.getGPTResponseWithUsage`에 `@Retryable(ResourceAccessException, HttpServerErrorException, 3회)`가 있지만 ① 프로젝트에 `@EnableRetry`가 없고(grep으로 확인) ② `callGptApi`가 해당 예외를 `GPTApiException`으로 바꿔서 던지므로 조건에도 맞지 않는다.
- `ChatService.processQuestion`의 catch는 `InvalidQuestionException`과 `ChatQueryException`만 그대로 다시 던진다. **`GPTApiException`(429 포함)은 `CHAT_QUESTION_SAVE_ERROR`(CQ101, 500)를 가진 `ChatQueryException`으로 감싸진다.** 그래서 클라이언트는 429/CQ304를 받지 못한다(핸들러가 cause를 보는지는 미확인).
- `GPTUsageService.logGPTUsage`는 `@Transactional(rollbackFor=Exception.class)`이고 바깥 트랜잭션에 참여한다. 내부에서 예외를 삼키지만, 저장 중 런타임 예외가 나면 트랜잭션이 rollback-only로 표시되어 바깥 커밋이 실패할 가능성이 있다(미검증).
- `ChatQuestion.question`에는 길이 지정이 없다(JPA 기본값 255로 추정). 반면 허용하는 질문 길이는 2000자다. DDL이 자동 생성된 경우 긴 질문을 저장하다 실패할 수 있다(실제 스키마는 미확인).
- 매 질문마다 FAQ 전체를 system prompt에 넣는다. FAQ가 늘면 토큰 비용과 컨텍스트 한도 문제가 생긴다. FAQ answer의 HTML(img 태그 등)도 그대로 들어간다.
- `GPTService.getGPTResponse(String, List)`(오류를 문자열로 반환하는 버전)는 호출하는 곳이 없다.
- 페이지는 one-indexed 설정을 따른다(`application.yml`). `/chat/history`의 기본 size는 2다.
