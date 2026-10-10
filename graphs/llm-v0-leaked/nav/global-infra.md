# global-infra — 전역 설정/인프라 네비게이션

> 범위: `src/main/java/com/fream/back/global/**` (security 제외: `global/config/security/**`, `global/security/**`, `SecurityUtils` 는 별도 문서), 그리고 코드베이스 전체에 걸친 Kafka/Redis/캐시/WebSocket/스케줄러 목록.
> 모든 경로는 `src/main/java/com/fream/back/` 기준.

## 자주 할 작업 → 여기를 보라

| 작업 | 볼 곳 |
|---|---|
| 새 도메인 예외 추가 | `XxxErrorCode implements global/exception/ErrorCode` + `XxxException extends GlobalException` → `GlobalExceptionHandler`에 핸들러 추가는 선택(없어도 `GlobalException` 핸들러가 잡음) |
| 에러 응답 포맷 변경 | `global/response/ErrorResponse.java`, `global/exception/GlobalExceptionHandler.java` |
| 새 Kafka 토픽/이벤트 | 기존 패턴: `global/config/kafka/XxxKafkaConfig.java`(ProducerFactory/KafkaTemplate/ConsumerFactory/ListenerContainerFactory) + 도메인 `service/kafka/XxxProducer`, `XxxConsumer` |
| 새 `@Cacheable` 캐시 이름 | **반드시** `global/config/CacheConfig.java`의 `setCacheNames(...)` 목록에 추가 (아래 gotcha 참고) |
| Redis 접속 정보/템플릿 | `global/config/RedisConfig.java` |
| STOMP 엔드포인트/브로커 prefix | `global/config/websocket/WebSocketConfig.java`, 핸드셰이크 인증 `WebSocketAuthInterceptor.java` |
| 파일 업로드 저장 경로 | `global/utils/FileUtils.java` (`BASE_DIR` 하드코딩) |
| 페이지 파라미터 기본값 | `global/config/WebConfig.java` (1-based, 기본 size 10, max 2000) |
| 로컬 개발 시드 데이터 | `global/config/DataInitializer.java` |
| 개인정보 암복호화 | `global/utils/PersonalDataEncryptionUtil.java` |

---

## 1. global/config/* 클래스별 역할

| 클래스 | 역할 / 등록 빈 |
|---|---|
| `CacheConfig` | `@EnableCaching`. `ConcurrentMapCacheManager` 빈 `cacheManager`, 캐시 이름 고정 목록: `faqList, faqDetail, faqCategoryList, faqSearchResults, inspectionStandards, inspectionStandardsByCategory, inspectionStandardDetail, inspectionStandardSearchResults` |
| `RedisConfig` | Lettuce. `@Primary redisConnectionFactory`(StaticMasterReplica, `ReadFrom.REPLICA_PREFERRED`), `primaryRedisConnectionFactory`, `replicaRedisConnectionFactory`. 템플릿: `@Primary redisTemplate`(`<String,Object>`, JSON value), `stringRedisTemplate`(`<String,String>`), `writeRedisTemplate`(primary 전용), `readRedisTemplate`(replica 전용). 프로퍼티: `spring.redis.primary.host`(기본 `redis-primary`), `spring.redis.primary.port`(6379), `spring.redis.replica.host`(`redis-replica`), `spring.redis.replica.port`(6380), `spring.redis.timeout`(2000) |
| `QueryDslConfig` | `JPAQueryFactory` 빈. `@DataJpaTest`에서는 `@Import(QueryDslConfig.class)` 필요 |
| `WebConfig` | `PageableHandlerMethodArgumentResolver`: `setOneIndexedParameters(true)`, fallback `PageRequest.of(0,10)`, max 2000 |
| `WebMvcConfig` | **전체 주석 처리됨** (CORS 설정 없음) |
| `SwaggerConfig` | springdoc `OpenAPI` 빈, `bearerAuth`(HTTP bearer JWT) 보안 스킴 |
| `RestTemplateConfig` | `RestTemplate` 빈 (HttpComponents, connect 5s / read 10s, 요청/응답 debug 로깅 인터셉터). 사용처: weather `OpenMeteoWeatherApiClient` 등 |
| `DataInitializer` | `CommandLineRunner`, `@Order(1)`, **`@Profile` 없음 → 모든 프로필에서 실행**. user1/user2/admin 사용자·주소·계좌, 상품/공지/FAQ/검수기준/알림/입찰/주문/스타일 시드 생성 (계정 비밀번호는 코드 참조, 여기 기재 안 함). 이메일 존재 시 사용자 생성 skip |
| `ApplicationShutdownConfig` | `ContextClosedEvent` 리스너 → `StyleViewLogBufferManager.flushBufferOnShutdown()` |
| `LogViewerConfig` | `@EnableMethodSecurity`. `String logDirectoryPath` 빈(`logging.file.path`, 기본 `/home/ubuntu/springlog`), `CorsConfigurationSource` 빈(`/api/logs/**`, origin `https://www.pinjun.xyz`, GET만) |
| `EmailBasedUserDestinationResolver` | `@Component`. SecurityContext principal(String 이메일) → `"/user/" + email + destination` 문자열 생성 헬퍼 |
| `BatchInfraConfig` | `@EnableBatchProcessing`, `JobRepository`(DatabaseType MYSQL) |
| `ShipmentBatchScheduler` | `updateShipmentStatusesJob` 배치를 cron으로 실행 (스케줄러 표 참고) |
| `GPTConfig` | `openai.api.key`(기본값 없음 → 필수), `openai.model`, `openai.api.url` |
| `aop/PersonalDataEncryptionAspect` | **빈 클래스** (애노테이션/로직 없음) |

## 2. Kafka — 코드베이스 전체 토픽

모든 Kafka 설정은 `@Value("${spring.kafka.bootstrap-servers}")` 사용. 토픽명은 **코드에 하드코딩**(yml의 `spring.kafka.order.*`, `payment.kafka.*`, `access-log.kafka.*` 키는 Java 코드에서 참조되지 않음).

| 토픽 | Producer (클래스 → KafkaTemplate 빈) | Consumer (클래스, groupId, containerFactory) | 설정 클래스 |
|---|---|---|---|
| `order-processing-topic` | `domain/order/service/kafka/OrderEventProducer` (`orderProcessingEventKafkaTemplate`) | `OrderEventConsumer`, `order-processing-group`, `orderProcessingKafkaListenerContainerFactory` | `global/config/kafka/OrderKafkaConfig` (concurrency 2, AckMode MANUAL_IMMEDIATE) |
| `order-retry-topic` | `OrderEventProducer` | `OrderEventConsumer`, `order-retry-group`, 동일 factory | `OrderKafkaConfig` |
| `notification-topic` | `OrderEventConsumer` (`notificationKafkaTemplate`, `KafkaTemplate<String, Map<String,Object>>`, key=userEmail) | **컨슈머 없음** (`@KafkaListener` 미존재) | `OrderKafkaConfig` |
| `payment-processing-topic` | `domain/payment/service/kafka/PaymentEventProducer` (`paymentEventKafkaTemplate`) | `PaymentEventConsumer`, `payment-processing-group`, `paymentEventKafkaListenerContainerFactory` | `global/config/kafka/PaymentKafkaConfig` (concurrency 1, MANUAL_IMMEDIATE) |
| `payment-retry-topic` | `PaymentEventProducer` | `PaymentEventConsumer`, `payment-retry-group` | `PaymentKafkaConfig` |
| `user-access-log-topic` | `domain/accessLog/service/kafka/UserAccessLogProducer` (`userAccessLogKafkaTemplate`) — `access-log.use-kafka=true`일 때만 호출 | `UserAccessLogConsumer` **및** `UserAccessLogConsumer_backup`(둘 다 활성 `@Service`), `user-access-log-group`, `userAccessLogKafkaListenerContainerFactory` | `global/config/kafka/UserAccessLogKafkaConfig` |
| `view-log-topic` | `domain/product/service/kafka/ViewEventProducer` | `ViewEventConsumer`, `my-group`, `viewEventKafkaListenerContainerFactory` | `domain/product/service/kafka/config/ViewEventKafkaConfig` |
| `style-view-log-topic` | `domain/style/service/kafka/StyleViewEventProducer` (`styleViewEventKafkaTemplate`) | `StyleViewEventConsumer`, `style-view-group`, `styleViewEventKafkaListenerContainerFactory` (버퍼링 후 `StyleViewLogBufferManager`가 flush) | `domain/style/service/kafka/config/StyleViewEventKafkaConfig` |

## 3. Redis 사용처 (키 패턴)

| 키 패턴 | 타입/TTL | 소유 클래스 | 템플릿 |
|---|---|---|---|
| `WebSocket:User:{email}` | value `"CONNECTED"`, TTL 30분 (남은 TTL ≤600s일 때 갱신) | `domain/notification/service/command/NotificationCommandService`(hasKey로 접속 여부 판단, TTL 갱신), `global/config/websocket/WebSocketAuthInterceptor.afterHandshake`(set) | `RedisTemplate<String,String>` (qualifier 없음) |
| `access:{accessToken}` | hash(email/role/age/gender 등), expire | `domain/user/redis/AuthRedisService` | 쓰기 `writeRedisTemplate`, 읽기 `readRedisTemplate`(replica) |
| `refresh:{refreshToken}` | hash(email), expire | `AuthRedisService` | 동일 |
| `rate_limit:ip:{ip}`, `blocked_ip:{ip}` | — | `global/security/redis/IpBlockingRedisService` (security 문서 참조) | — |

참고: `AddressSecurityAspect`의 rate limit 키(`email:class:method`)는 Redis가 아니라 in-memory Map. `EventCachingAspect`/`FAQCacheAspect`/`FAQSearchOptimizationAspect`는 Redis가 아닌 Spring `CacheManager`/메모리 사용.

## 4. 캐시 (`@Cacheable` / CacheManager)

| 캐시 이름 | 사용 위치 | CacheConfig 등록 여부 |
|---|---|---|
| `faqList`, `faqDetail`, `faqCategoryList`, `faqSearchResults` | `domain/faq/service/query/FAQQueryService` (`@Cacheable`), `FAQCommandService` (`@CacheEvict`) | O |
| `inspectionStandards`, `inspectionStandardsByCategory`, `inspectionStandardSearchResults`, `inspectionStandardDetail` | `domain/inspection/service/query/InspectionStandardQueryService` (evict는 `InspectionCommandController`가 CacheManager 직접 사용) | O |
| `eventDetails`, `activeEvents`, `brandEvents` | `domain/event/service/query/EventQueryService` | **X (미등록)** |
| `geoIpCache`, `todayAccessCount`, `todayUniqueVisitors`, `recent7DaysStats`, `countryStats`, `popularPages`, `userTypeStats`, `hourlyStats`, `deviceStats`, `browserStats`, `performanceStats`, `methodStats` | `domain/accessLog/config/AccessLogAopConfiguration`의 별도 빈 `accessLogCacheManager`에 선언. `@Cacheable`로 쓰는 곳은 없음 | 별도 매니저 |

## 5. WebSocket / STOMP

| 항목 | 값 | 위치 |
|---|---|---|
| 엔드포인트 | `/ws` (SockJS), allowed origin `https://www.pinjun.xyz` | `global/config/websocket/WebSocketConfig` |
| 핸드셰이크 인증 | 쿠키 `ACCESS_TOKEN` → `JwtTokenProvider.validateToken` → 이메일로 `UserRepository.findByEmail` → `attributes.put("email", ...)`; 실패 시 `false` 반환(연결 거부) | `WebSocketAuthInterceptor` |
| 브로커 | simple broker `/topic`, `/queue`; app prefix `/app`; user prefix `/user` | `WebSocketConfig` |
| 클라→서버 | `/app/ping` → `NotificationCommandController.handlePing()` → `updateWebSocketConnectionTTL` | notification |
| 서버→클라 | `convertAndSendToUser(email, "/queue/notifications", ...)` (NotificationCommandService, OrderEventConsumer); `/topic/order/{orderId}` (OrderEventConsumer, `OrderCommandController`가 응답에 `websocketTopic`으로 안내) | |
| 예외 | `global/exception/websocket/*` (`WebSocketErrorCode` W001~W005) | |
| nginx | prod nginx `location /api/ws` → `http://app:8080/ws` | `docker/prod/nginx/nginx.conf` |

## 6. 예외 처리 패턴

```
ErrorCode (interface: getCode/getMessage/getStatus)
  ├─ GlobalErrorCode (G001~G031), FileErrorCode (F001~F005), WebSocketErrorCode (W001~W005),
  │  global/exception/security/SecurityErrorCode (S001~S006)
  └─ 도메인별 XxxErrorCode enum (예: AccessLogErrorCode AL*, NotificationErrorCode N*, AddressErrorCode ADR*, WeatherErrorCode WE*)
GlobalException (abstract RuntimeException, ErrorCode 보유; 생성자 4종: code / code+msg / code+cause / code+msg+cause)
  ├─ EntityNotFoundException(RESOURCE_NOT_FOUND), InvalidRequestException(INVALID_INPUT_VALUE)
  ├─ FileException(abstract) → FileUploadException, FileDownloadException, FileNotFoundException, UnsupportedFileTypeException, DirectoryCreationException
  ├─ WebSocketException → WebSocketAuthenticationException, WebSocketConnectionException, WebSocketMessageSendException
  └─ 도메인 XxxException ...
GlobalExceptionHandler (@RestControllerAdvice) → ResponseEntity<ErrorResponse>
```

- `ErrorResponse { code, message, status, timestamp, path }` (`@JsonInclude(NON_NULL)`), `ErrorResponse.of(code, message, status, path)`.
- 핸들러 목록: `GlobalException`, `SecurityException`, `FileException`, `WebSocketException`, `WeatherException`, `AccessLogException`, `ChatQuestionException`, `FAQException`, `InspectionException`, `NoticeException`, `NotificationException`, `AddressException`, `OrderException`, `PaymentException`, `WarehouseStorageException`, `EventException`, `ProductException`, `SaleException`, `ShipmentException`, `StyleException`, `InquiryException`, `UserException`, `MethodArgumentNotValidException`/`BindException`(G002, 첫 에러 메시지), `Exception`(G001, 500).
- **gotcha**: 모든 핸들러가 응답 `message`에 `errorCode.getMessage()`를 사용 → 예외 생성 시 넘긴 상세 메시지(`e.getMessage()`)는 **로그에만** 남고 클라이언트에는 enum 기본 메시지가 나감.
- 별도 응답 래퍼 `global/dto/ResponseDto<T>` (`success/message/data/errorCode/timestamp`, `success()/fail()/error()`)도 존재 — 일부 컨트롤러만 사용.

## 7. 파일 저장 (`global/utils/FileUtils`)

- `BASE_DIR = "/home/ubuntu/fream"` 하드코딩 (Windows 경로는 주석). prod compose에서 이 경로를 호스트 볼륨에 마운트.
- `saveFile(directory, prefix, file)`: content-type이 `image/*` 또는 `video/*`가 아니면 `UnsupportedFileTypeException`; 파일명 `prefix + UUID + 확장자`; 반환값은 파일명만. `saveMediaFile`은 `saveFile` 위임.
- `existsFile`, `deleteFile`, `deleteDirectory`, `isFileExist(fullPath)`, `getFileUrl(dir, name)` → `"/api/files/{dir}/{name}"` 문자열.
- 사용처: event, notice, faq, inquiry, inspection, product(ProductColor/SimpleImage), style(MediaUrl), user(Profile/UserCommand).

## 8. 스케줄러 (코드베이스 전체 `@Scheduled`)

`@EnableScheduling`은 `EventStatusScheduler`, `AccessLogAopConfiguration`, `WeatherDataScheduler` 3곳에 선언(어느 하나만 있어도 전역 활성).

| 클래스 | 주기 | 용도 |
|---|---|---|
| `global/config/ShipmentBatchScheduler` | `cron = "0 0 */6 * * *"` | Spring Batch `updateShipmentStatusesJob` 실행 (배송 상태 갱신, 완료 시 `notifyShipmentCompleted`) |
| `domain/event/scheduler/EventStatusScheduler` | `cron = "0 0 * * * *"` | 이벤트 상태 전환(UPCOMING→ACTIVE 등) |
| `domain/user/scheduler/PointExpirationScheduler` | `cron = "0 0 * * * *"` | `pointCommandService.expirePoints()` |
| `domain/style/service/scheduler/StyleViewLogBufferManager` | `fixedDelay = 10000` | `StyleViewEventConsumer.flushBuffer()` |
| `domain/faq/aop/FAQSearchOptimizationAspect` | `fixedDelay = 3600000` | 인기 검색어 갱신/검색 로그 정리 |
| `domain/accessLog/config/AccessLogAopConfiguration` | `fixedRate = 600000, initialDelay = 60000` | AccessLog AOP 통계 로그 출력 |
| `domain/weather/config/WeatherDataScheduler` | `fixedRateString=${weather.scheduler.interval:86400000}`, `initialDelayString=${weather.scheduler.initial-delay:10000}` | Open-Meteo 날씨 수집·upsert |

## 9. 공통 DTO / 엔티티 / 유틸

| 파일 | 내용 |
|---|---|
| `global/entity/BaseTimeEntity` | `@MappedSuperclass`, `createdDate`(updatable=false), `modifiedDate`; `@PrePersist`/`@PreUpdate`로 `LocalDateTime.now()` 세팅 |
| `global/entity/BaseEntity` | `BaseTimeEntity` + `@CreatedBy createdBy`, `@LastModifiedBy modifiedBy` |
| `global/dto/ResponseDto<T>` | 성공/실패 응답 래퍼 |
| `global/dto/commonDto.PageDto<T>` | `content,totalElements,totalPages,page,size` |
| `global/dto/LogFileDTO, LogLineDTO, LogResponseDTO` | LogController용 |
| `global/utils/PageUtils.toPageDto(Page)` | Page → PageDto (현재 호출처 없음) |
| `global/utils/NginxCachePurgeUtil` | `http://nginx:80`으로 `PURGE` 요청: `/purge_products/api/products/`, `/purge_es/api/es/products/`, `/purge_styles/api/styles/queries`; 예외 무시. 사용처: `ProductColorCommandService`, `ProductColorCommandController`, `StyleCommandController` |
| `global/utils/PersonalDataEncryptionUtil` | `personal-data.encryption.secret-key`, `personal-data.encryption.iv`. `encrypt/decrypt`(AES/CBC/PKCS5), `deterministicEncrypt/deterministicDecrypt`(AES/ECB/PKCS5), `encryptForSearch`(= deterministicEncrypt 결과와 동일, 테스트로 고정), `isEncrypted` |
| `global/utils/CjTrackingPlaywright`, `PlaywrightBrowserManager` | CJ 택배 조회 Playwright (shipment 문서 범위) |
| `global/controller/LogController` | `@RequestMapping("/api/logs")`, `@PreAuthorize("hasRole('ADMIN')")`. `GET /api/logs` 로그 파일 목록(`.log`), `GET /api/logs/{fileName}?page&size&search&level&reverse&startTime&endTime` (파일명에 `..`,`/`,`\` 금지) |

## 10. 관찰된 gotcha

1. **캐시 미등록**: `CacheConfig`가 `setCacheNames(...)`로 고정 목록을 지정(ConcurrentMapCacheManager의 동적 생성 비활성)하는데 `EventQueryService`의 `eventDetails/activeEvents/brandEvents`는 목록에 없음 → 호출 시 "Cannot find cache" 류 예외 가능 (실행 검증은 안 함).
2. **CacheManager 빈 2개** (`cacheManager`, `accessLogCacheManager`), `@Primary` 없음. 타입 주입처(`FAQCacheAspect`, `EventCachingAspect`, `FAQSearchOptimizationAspect`, `InspectionCommandController`)는 필드명 `cacheManager`로 이름 매칭에 의존.
3. **JPA Auditing 미활성**: `@EnableJpaAuditing`/`AuditorAware`가 코드 어디에도 없음 → `BaseEntity.createdBy/modifiedBy`는 채워지지 않음. 시간 필드는 `@PrePersist/@PreUpdate`로 채워짐(즉, `modifiedDate`는 최초 저장 시 null).
4. **WebSocket TTL 키 세팅 경로**: `WebSocketAuthInterceptor.afterHandshake`는 서블릿 request attribute `"attributes"`를 읽는데 이를 set하는 코드가 없음(grep 결과 0건) → 핸드셰이크 시 `WebSocket:User:{email}` 키가 생성되지 않을 수 있음. `NotificationCommandService`는 이 키가 있어야만 실시간 push.
5. `WebSocketConfig`/`WebSocketAuthInterceptor`/`NotificationCommandService`는 `RedisTemplate<String,String>`을 qualifier 없이 주입 — 후보 빈 다수(`stringRedisTemplate`, `writeRedisTemplate`, `readRedisTemplate`) 중 어느 것이 주입되는지 (미확인).
6. `RedisConfig` replica 기본 포트 6380 vs docker prod의 `redis-replica` 컨테이너 내부 포트 6379(`6380:6379`는 호스트 매핑). `application-prod.yml`에 `spring.redis.primary/replica.*` 키가 없으므로 기본값 사용됨.
7. `notification-topic`으로 발행만 하고 소비자는 없음.
8. `UserAccessLogConsumer_backup`이 활성 빈으로 같은 토픽·그룹을 구독.
9. `LogController`만 경로에 `/api` 접두사를 직접 포함. prod nginx는 `location /api/` → `proxy_pass http://app:8080/`(접두사 제거)이므로 다른 컨트롤러들은 `/api` 없이 매핑됨.
10. `DataInitializer`는 프로필 무관 실행 + prod `ddl-auto: create` → prod 기동 때마다 스키마 재생성 후 시드 데이터 삽입.
