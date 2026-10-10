# accessLog 도메인 (사용자 접근 로그)

## 1. 개요
- 프론트엔드가 보내는 페이지 방문 정보(UA, 화면 크기, 언어 등)를 받아 IP/Referer를 보강하고 GeoIP(MaxMind GeoLite2)로 국가/지역/도시를 채워 `user_access_log` 테이블에 저장.
- 저장 경로는 설정 `access-log.use-kafka`(기본 `false`)로 분기: `false` → 동기 직접 저장, `true` → Kafka `user-access-log-topic` 발행 후 컨슈머가 저장.
- 조회는 오늘 고유 방문자 수(IP distinct), 최근 7일 일별 접속 수 2개뿐.
- 도메인 전용 AOP(예외/재시도, 메서드 로깅, 성능 측정) 3종이 서비스 메서드에 붙어 있음.

## 2. 패키지/파일 구조
루트: `src/main/java/com/fream/back/domain/accessLog/`
| 경로 | 역할 |
|---|---|
| `controller/command/UserAccessLogCommandController.java` | POST `/access-log/commands/create`, IP 추출/검증 |
| `controller/query/UserAccessLogQueryController.java` | GET `/access-log/queries/today`, `/week` |
| `service/command/UserAccessLogCommandService.java` | 생성 로직 (Kafka/직접저장 분기) |
| `service/query/UserAccessLogQueryService.java` | 통계 조회 |
| `service/kafka/UserAccessLogProducer.java` | Kafka 발행 |
| `service/kafka/UserAccessLogConsumer.java` | Kafka 소비 → GeoIP → 저장 |
| `service/geo/GeoIPService.java` | `GeoLite2-City.mmdb` 조회, 내부 클래스 `Location` |
| `service/**/*_backup.java` (5개) | 구버전 사본. 주의사항 참고 (일부는 활성 빈) |
| `entity/UserAccessLog.java` | 엔티티 |
| `dto/UserAccessLogDto.java` | 요청 DTO, `toEvent()` 로 Kafka 이벤트 변환 |
| `dto/UserAccessLogEvent.java` | Kafka 메시지 페이로드 |
| `dto/DailyAccessCountDto.java` | (dateString, count) |
| `repository/UserAccessLogRepository{,Custom,Impl}.java` | JPA + QueryDSL |
| `aop/annotation/AccessLog{ExceptionHandler,MethodLogger,PerformanceMonitor}.java` | 커스텀 어노테이션 |
| `aop/aspect/AccessLog{Exception,MethodLogging,Performance}Aspect.java` | 어스펙트 |
| `config/AccessLogAopConfiguration.java` | `@EnableAspectJAutoProxy(proxyTargetClass, exposeProxy)`, `@EnableScheduling`, 캐시매니저, AOP 통계 스케줄 |
| `exception/*` | `AccessLogErrorCode`, `AccessLogException` 및 하위 예외 5종 |

관련 외부 파일
- `src/main/java/com/fream/back/global/config/kafka/UserAccessLogKafkaConfig.java` — 전용 Producer/Consumer factory
- `src/main/resources/GeoLite2-City.mmdb` — GeoIP DB (클래스패스 로드)
- `src/main/resources/application-prod.yml` `access-log:` 블록 (L230 근처)

## 3. 엔티티
`UserAccessLog` (`@Table(name="user_access_log")`, 인덱스 `idx_access_time(ACCESS_TIME)`, `idx_ip_access_time(ipAddress, ACCESS_TIME)`)
- PK `id`: SEQUENCE 전략 (`USER_ACCESS_LOG_SEQ`, allocationSize=1)
- 필드: `refererUrl`, `userAgent`, `os`, `browser`, `deviceType`, `accessTime`(기본 now), `ipAddress`, `country`, `region`, `city`, `pageUrl`, `email`, `isAnonymous`(기본 true, `IS_ANONYMOUS`), `networkType`, `browserLanguage`, `screenWidth`, `screenHeight`, `devicePixelRatio`
- 연관관계 없음. User 엔티티와 FK 없이 `email` 문자열로만 저장 (익명 시 `"Anonymous"`). `BaseTimeEntity` 상속 안 함.

## 4. API 엔드포인트
SecurityConfig(`global/config/security/SecurityConfig.java`)의 whitelist에 `/access-log/**`가 없으므로 아래 모두 `anyRequest().authenticated()` 대상(인증 필요). 메서드 레벨 권한 체크는 없음.

| Method | Path | Controller#method | Service#method | 비고 |
|---|---|---|---|---|
| POST | `/access-log/commands/create` | `UserAccessLogCommandController#createAccessLog` | `UserAccessLogCommandService#createAccessLog` | 204 No Content. body=`UserAccessLogDto` |
| GET | `/access-log/queries/today` | `UserAccessLogQueryController#getTodayAccessCount` | `UserAccessLogQueryService#getTodayUniqueVisitorCount` | long 반환 (IP distinct) |
| GET | `/access-log/queries/week` | `UserAccessLogQueryController#getWeekAccessCount` | `UserAccessLogQueryService#getRecent7DaysAccessCount` | `List<DailyAccessCountDto>` |

## 5. 서비스 계층
- `UserAccessLogCommandService#createAccessLog(dto)` (`@Transactional`): `validateLogDto`(IP 필수, pageUrl 없으면 `"Unknown"`) → `useKafka`면 `UserAccessLogProducer#sendAccessLog`, 아니면 `enrichWithGeoData` → `mapToEntity` → `save`.
- `UserAccessLogQueryService`: `getTodayAccessCount()`(현재 컨트롤러 미사용, 주석처리), `getTodayUniqueVisitorCount()`, `getRecent7DaysAccessCount()`. `DataAccessException` → `AccessLogQueryException`.
- `GeoIPService`: 생성자에서 `ClassPathResource("GeoLite2-City.mmdb")`로 `DatabaseReader` 생성(실패 시 `GeoIPException(GEO_IP_DATABASE_ERROR)` → 빈 생성 실패). `getLocation(ip)`: null/빈값/`localhost`/`127.0.0.1`/`0:0:0:0:0:0:0:1` → `Unknown` 3종, `UnknownHostException` → `GeoIPException(INVALID_IP_ADDRESS)`, 그 외 예외 → `Unknown` 반환.

## 6. Repository
- `UserAccessLogRepository`: `existsByEmailOrIpAddress`, `findByIsAnonymous` (파생 쿼리)
- `UserAccessLogRepositoryImpl` (QueryDSL):
  - `countTodayAccesses()` — 오늘 00:00~now count
  - `countTodayUniqueVisitors()` — `ipAddress.countDistinct()`
  - `findRecent7DaysAccessCount()` — `function('DATE_FORMAT', accessTime, '%Y-%m-%d')` group by → MySQL 종속 함수

## 7. 외부 연동 / 비동기
### Kafka
| 항목 | 값 |
|---|---|
| Topic | `user-access-log-topic` (Producer `TOPIC_NAME` 상수 L26, Consumer 어노테이션 하드코딩) |
| Producer | `UserAccessLogProducer#sendAccessLog` — `KafkaTemplate<String, UserAccessLogEvent>`, key 없이 send, 실패는 콜백에서 로그만 |
| Consumer | `UserAccessLogConsumer#consume` (L60) |
| Group ID | `user-access-log-group` |
| Container factory | `userAccessLogKafkaListenerContainerFactory` (`UserAccessLogKafkaConfig`) |
| 직렬화 | Producer: StringSerializer / JsonSerializer. Consumer: `JsonDeserializer<>(UserAccessLogEvent.class)` |
| bootstrap | `${spring.kafka.bootstrap-servers}` (prod: `kafka:9092`) |
- yml의 `access-log.kafka.topic`, `access-log.kafka.group-id`, `access-log.geoip.database-path` 값은 코드에서 `@Value`로 읽지 않음(하드코딩 사용). 실제로 읽는 키는 `access-log.use-kafka`뿐.

### 스케줄러
- `AccessLogAopConfiguration#logAopStatistics` — `@Scheduled(fixedRate = 600000, initialDelay = 60000)` (10분마다 AOP 통계 로그 출력).

### AOP 어노테이션 (`aop/annotation`, 모두 `@Target(METHOD)`)
| 어노테이션 | Aspect (Order) | 동작 | 주요 속성 |
|---|---|---|---|
| `@AccessLogExceptionHandler` | `AccessLogExceptionAspect` `@Around` (Order 1) | `retry=true`면 지수 백오프 재시도(`Thread.sleep`, 1s→2s→4s… 최대 10s, 총 retryCount+1회), 최종 실패/일반 실패 시 `defaultType`에 맞는 AccessLog 예외로 변환 | `defaultType`(GENERAL/SAVE/QUERY/KAFKA/GEO_IP/VALIDATION), `message`, `logLevel`, `retry`, `retryCount` |
| `@AccessLogMethodLogger` | `AccessLogMethodLoggingAspect` `@Before/@AfterReturning/@AfterThrowing` (Order 2) | 진입/반환/예외 로그, 실행시간 | `level`, `logParameters`, `logReturnValue`, `measureExecutionTime`, `customMessage` |
| `@AccessLogPerformanceMonitor` | `AccessLogPerformanceAspect` `@Around` (Order 3) | 실행시간·메모리 측정, 임계치/슬로우 판정, 메서드별 통계 | `thresholdMs`, `measureMemory`, `collectMetrics`, `logPerformance`, `slowQueryThresholdMs`, `enablePerformanceGrading` |

적용 위치: `UserAccessLogCommandService#createAccessLog`, `UserAccessLogProducer#sendAccessLog`, `UserAccessLogConsumer#consume`, `GeoIPService#getLocation`, `UserAccessLogQueryService` 3개 메서드 (public). 그 외 private 메서드(`validateLogDto`, `enrichWithGeoData`, `mapToEntity`, `buildUserAccessLog`)에도 붙어 있으나 프록시 기반 Spring AOP 특성상 private/self-invocation은 가로채지 않음.

## 접근 로그 수집 흐름
1. `POST /access-log/commands/create` → `UserAccessLogCommandController#createAccessLog`
   - `enrichAccessLogWithRequestData`: `extractClientIp`(X-Forwarded-For → Proxy-Client-IP → WL-Proxy-Client-IP → HTTP_CLIENT_IP → HTTP_X_FORWARDED_FOR → remoteAddr, 첫 번째 IP, 숫자/점 외 제거, IPv4 정규식 불일치 시 `"0.0.0.0"`), `User-Agent`, `Referer` 헤더 세팅, email 없으면 `"Anonymous"` + `anonymous=true`
2. `UserAccessLogCommandService#createAccessLog` → `validateLogDto`
3. 분기 (`access-log.use-kafka`, prod=false):
   - **false (기본)**: `enrichWithGeoData` → `GeoIPService#getLocation` → `mapToEntity` → `UserAccessLogRepository#save` (accessTime은 엔티티 기본값 now)
   - **true**: `UserAccessLogProducer#sendAccessLog` → `UserAccessLogDto#toEvent()` → `kafkaTemplate.send("user-access-log-topic", event)` → `UserAccessLogConsumer#consume` → `GeoIPService#getLocation` → `buildUserAccessLog` (accessTime null이면 now) → `save`
4. 조회: `UserAccessLogQueryController` → `UserAccessLogQueryService` → `UserAccessLogRepositoryImpl` (QueryDSL)

## 8. 다른 도메인과의 의존
- 다른 도메인에서 accessLog 클래스를 import하지 않음 (참조는 `global/config/kafka/UserAccessLogKafkaConfig.java`, `global/exception/GlobalExceptionHandler.java`뿐).
- accessLog도 다른 도메인을 의존하지 않음 (User FK 없음, email 문자열만).

## 9. 예외
- `AccessLogErrorCode implements ErrorCode`, 접두사 `AL`:
  - `AL001` ACCESS_LOG_SAVE_ERROR(500), `AL002` KAFKA_SEND_ERROR(500), `AL003` KAFKA_RECEIVE_ERROR(500)
  - `AL101` ACCESS_LOG_QUERY_ERROR(500), `AL102` STATISTICS_QUERY_ERROR(500)
  - `AL201` GEO_IP_LOOKUP_ERROR(500), `AL202` GEO_IP_DATABASE_ERROR(500)
  - `AL301` INVALID_ACCESS_LOG_DATA(400), `AL302` INVALID_IP_ADDRESS(400), `AL303` INVALID_DATE_RANGE(400)
- 계층: `AccessLogException extends GlobalException` ← `AccessLogKafkaException`, `AccessLogQueryException`, `AccessLogSaveException`, `GeoIPException`, `InvalidParameterException`. `GlobalExceptionHandler`에 `AccessLogException` 핸들러 존재.

## 10. 작업 팁 / 주의점
- **활성 백업 빈**: `UserAccessLogConsumer_backup`, `UserAccessLogProducer_backup`, `GeoIPService_backup`, `UserAccessLogQueryService_backup`은 `@Service`가 살아있음 (`UserAccessLogCommandService_backup`만 주석처리). 특히 `UserAccessLogConsumer_backup#consume`(L36)도 같은 topic/groupId의 `@KafkaListener` → 같은 그룹에 리스너 2개가 등록되어 파티션을 나눠 소비. 컨슈머 로직 수정 시 둘 다 확인하거나 백업을 비활성화할 것. `GeoIPService_backup`도 mmdb를 별도로 로드함.
- `@KafkaListener`는 `use-kafka` 값과 무관하게 항상 등록됨.
- `UserAccessLogConsumer#consume`는 모든 예외를 catch 후 로그만 남김(재throw/DLQ 없음) → `@AccessLogExceptionHandler(retry=true)` 재시도는 실질적으로 동작할 예외가 거의 없음.
- `AccessLogExceptionAspect`의 재시도는 `Thread.sleep`으로 요청 스레드를 블로킹 (createAccessLog retryCount=3 → 최악 1+2+4초 대기).
- `/access-log/commands/create`가 인증 필요 경로이므로 비로그인 방문자 로그는 SecurityConfig 상 차단됨 (코드상 익명 처리 로직은 존재).
- IP는 IPv4만 허용, IPv6는 `"0.0.0.0"`으로 치환됨 → GeoIP 결과 의미 없음.
- `AccessLogAopConfiguration#accessLogCacheManager`는 캐시 이름만 정의, accessLog 도메인 내 `@Cacheable`/`@CacheEvict` 사용처 없음.
- `findRecent7DaysAccessCount`는 데이터 없는 날짜를 0으로 채우지 않음 (group by 결과만 반환).
