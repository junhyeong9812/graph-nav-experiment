# build-run-config — 빌드/실행/설정/테스트

## 자주 할 작업 → 여기를 보라

| 작업 | 볼 곳 |
|---|---|
| 테스트 실행 | `./gradlew test` (단일: `./gradlew test --tests 'com.fream.back.domain.address.*'`) |
| 의존성 추가 | `build.gradle` |
| 프로필별 설정 | `src/main/resources/application.yml`(공통, active=prod), `application-prod.yml`, `application-test.yml` |
| 로컬 실행 설정 만들기 | `application-local.yml.example` → `src/main/resources/application-local.yml`로 복사 후 `-Dspring.profiles.active=local` |
| 비밀값 env 이름 | `.env.example` (prod compose의 `app` 서비스가 `env_file: .env`) |
| 새 리포지토리 슬라이스 테스트 | 기존 패턴 복사: `@DataJpaTest` + `@AutoConfigureTestDatabase(replace = NONE)` + `@ActiveProfiles("test")` + `@Import(QueryDslConfig.class)` |
| 인프라 컨테이너 | `docker/prod/docker-compose.prod.yml`, `docker/dev/docker-compose.dev.yml` |

## 1. 기술 스택 / 버전 (build.gradle, settings.gradle)

| 항목 | 값 |
|---|---|
| 프로젝트 | `rootProject.name = 'fream-back'`, group `com.fream`, version `0.0.1-SNAPSHOT` |
| Java | toolchain 17 |
| Spring Boot | 3.4.1 (`io.spring.dependency-management` 1.1.7) |
| Gradle wrapper | 8.11.1 |
| Starters | web, validation, data-jpa, data-redis, websocket, oauth2-client, security, batch, data-elasticsearch, mail, actuator |
| 기타 | spring-kafka, httpclient5, springdoc-openapi-starter-webmvc-ui 2.3.0, p6spy-spring-boot-starter 1.9.0, java-jwt 3.18.2, jsoup 1.16.1, geoip2 3.0.1, playwright 1.38.0, elasticsearch-java 8.9.1, QueryDSL 5.0.0 (jakarta, apt), Lombok |
| DB 드라이버 | `runtimeOnly` h2, mysql-connector-java 8.0.33 |
| 테스트 | spring-boot-starter-test, spring-security-test, spring-kafka-test, spring-batch-test, junit-platform-launcher. `useJUnitPlatform()` |
| QueryDSL 생성 경로 | `$buildDir/generated/querydsl` (main sourceSet에 추가), 컴파일 옵션 `-parameters` |
| 없음 | Thymeleaf 의존성 없음(그런데 `templates/admin/monitoring.html` 존재), Testcontainers 없음 |

## 2. 프로필

| 파일 | 핵심 |
|---|---|
| `application.yml` | `spring.profiles.active: prod` (**기본 프로필이 prod**), multipart 1GB, `spring.data.web.pageable`(size 10, max 2000, one-indexed), `jwt.expiration`/`jwt.refreshExpiration`, actuator `exposure.include: "*"` |
| `application-prod.yml` | MySQL `jdbc:mysql://mysql:3306/freamdb`, **`ddl-auto: create`**, `spring.redis.host: redis`, Gmail SMTP, Kafka `kafka:9092`(producer acks all/idempotence, consumer group `my-group`, auto-commit false), Elasticsearch `http://elasticsearch:9200`, `spring.batch.jdbc.initialize-schema: never`, OAuth2(google/naver/kakao, redirect `https://www.pinjun.xyz/api/login/oauth2/code/*`), actuator `health,info,metrics`, `kibana.url`, `logging.file.name: /logs/spring-app.log`, `jwt.*`, `imp.*`, `personal-data.encryption.*`, `openai.*`, `server.use-forward-headers`, `payment.*`, `weather.*`, `access-log.*`(`use-kafka: false`) |
| `application-test.yml` | H2 `jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL`, `ddl-auto: create-drop` (그 외 설정 없음) |
| `application-local.yml.example` | H2 메모리(MySQL 모드) + h2-console, Redis/Kafka/ES `localhost`, batch schema always, server.port 8080 (리포에 실제 `application-local.yml`은 없음) |
| `logback.xml` | CONSOLE + `logs/application.log`, `logs/error.log`(ERROR), `logs/security.log`, `logs/file-utils.log`, `logs/websocket.log` (일별 롤링, 30일) |

### 프로퍼티 / 환경변수 키 (값은 기재하지 않음)
- prod yml이 참조하는 env: `DB_USERNAME`, `DB_PASSWORD`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `NAVER_CLIENT_ID`, `NAVER_CLIENT_SECRET`, `KAKAO_CLIENT_ID`, `KAKAO_CLIENT_SECRET`, `JWT_SECRET`, `IMP_KEY`, `IMP_SECRET`, `IMP_PG`, `IMP_STOREID`, `PERSONAL_ENCRYPTION_KEY`, `PERSONAL_ENCRYPTION_IV`, `OPENAPI_KEY`.
- 코드가 `@Value`로 요구하는 주요 키: `spring.kafka.bootstrap-servers`, `spring.redis.primary.host/port`, `spring.redis.replica.host/port`, `spring.redis.timeout`(모두 기본값 있음), `openai.api.key`(기본값 없음), `personal-data.encryption.secret-key`, `personal-data.encryption.iv`, `weather.api.url`, `weather.api.params.*`(기본값 없음), `weather.scheduler.*`, `weather.data.hours-count`, `access-log.use-kafka`, `logging.file.path`, `kibana.url`.
- `.env.example`에 정의된 키: 위 env 목록과 동일 계열(DB/JWT/MAIL/IMP/OAuth/OPENAPI_KEY).

## 3. 필요한 외부 서비스

| 서비스 | 용도 | prod 호스트 |
|---|---|---|
| MySQL | 메인 DB (`freamdb`), Spring Batch 메타 테이블 | `mysql:3306` |
| Redis primary / replica | 토큰(`access:`/`refresh:`), WebSocket 접속 키, IP 차단 | `redis-primary:6379`, `redis-replica`(RedisConfig 기본 포트 6380) |
| Kafka + Zookeeper | 주문/결제/조회수/접속로그 이벤트 | `kafka:9092`, zookeeper 2181 |
| Elasticsearch + Kibana | 상품 검색(ES), 모니터링 대시보드(Kibana, Metricbeat) | `elasticsearch:9200`, `kibana:5601` |
| nginx | 리버스 프록시, `/api/` → `app:8080/`, `/api/ws` → `app:8080/ws`, proxy_cache + PURGE 엔드포인트 | `nginx:80/443` |
| 외부 API | Open-Meteo(날씨), OpenAI, PortOne(imp), OAuth2 공급자, Gmail SMTP, CJ 택배(Playwright) | — |

## 4. Docker

| 파일 | 내용 |
|---|---|
| `docker/Dockerfile` | `openjdk:17-jdk-slim`, Playwright용 라이브러리 설치, `build/libs/fream-back-0.0.1-SNAPSHOT.jar` → `/app.jar`, 8080 |
| `docker/prod/docker-compose.prod.yml` | 서비스: `mysql`, `redis-primary`(6379), `redis-replica`(6380:6379, `replicaof redis-primary 6379`), `zookeeper`, `kafka`(advertised `kafka:9092`), `elasticsearch`, `kibana`, `nginx`(80/443), `app`(`SPRING_PROFILES_ACTIVE: prod`, `env_file: .env`, 볼륨 → `/home/ubuntu/fream`, `/logs`), `front`, `stt_tts`(5000), `certbot`, `cache-cleaner`(alpine cron 02:00 `scripts/clear_caches.sh`), `metricbeat`. 파일 하단에 GHCR 이미지 기반 대체 구성과 prometheus/grafana/loki 등이 주석으로 존재 |
| `docker/dev/docker-compose.dev.yml` | `redis`(단일), `zookeeper`, `kafka`(9092/9095), `elasticsearch`, `kibana`, `nginx`. MySQL·app 없음 |
| `docker/redis/*`, `docker/kafka/*`, `docker/mysql/*`, `docker/elasticsearch/*`(userdict_ko.txt, synonyms.txt) | 각 이미지 Dockerfile/설정 |
| `docker/prod/nginx/nginx.conf` | proxy_cache_path products/styles/es_products, `/purge_products`, `/purge_styles`, `/purge_es`, `/kibana/` |

## 5. CI/CD (`.github/workflows/ci-cd.yml`)

**파일 전체가 주석 처리되어 현재 비활성.** 주석 내용상 단계: checkout → JDK 17(temurin) → `./gradlew clean build -x test`(테스트 스킵) → GHCR 로그인 → `docker build -f ./docker/Dockerfile -t ghcr.io/junhyeong9812/fream-back:latest .` → push → (main) EC2에 SSH 후 `docker-compose -f docker-compose.prod.yml pull app && up -d app`.

## 6. 테스트 인프라

- `src/test/resources` **없음** → 테스트 설정은 `src/main/resources/application-test.yml` 사용.
- 공통 베이스 클래스·Testcontainers·`@EmbeddedKafka` **없음**.
- 패턴 3종:
  1. **JPA 슬라이스** (11개): `@DataJpaTest` + `@AutoConfigureTestDatabase(replace = NONE)` + `@ActiveProfiles("test")` + `@Import(QueryDslConfig.class)` → H2(MySQL 모드). `ProductQueryDslRepositoryTest`는 `@Import({QueryDslConfig.class, ProductQueryDslRepository.class})`.
  2. **Mockito 단위** (25개): `@ExtendWith(MockitoExtension.class)`.
  3. **순수 단위** (8개): 애노테이션 없음(엔티티/DTO/유틸).
- `FreamBackApplicationTests`: `@SpringBootTest` + 프로필 지정 없음 → `application.yml`의 `active: prod` 적용 → MySQL/Redis/Kafka/ES 및 env 변수가 없으면 컨텍스트 로드 실패할 것으로 보임 (미확인, 실행 안 함).
- `PersistenceSmokeTest`: 전체 엔티티 모델이 H2에서 스키마 생성되는지 검증하는 스모크 테스트.
- 여러 테스트가 "현재 동작 고정(특성화)" 성격 — 버그성 동작도 assert함(예: `AddressEncryptionRoundTripTest`, `NotificationRepositoryTest.countUnreadWithRealRepositoryFailsOnUnpagedPageable`). 동작을 고칠 때 해당 테스트도 의도적으로 갱신할 것.

### 테스트 클래스 (도메인별, `src/test/java/com/fream/back/`)
| 도메인 | 클래스 (S=DataJpaTest, M=Mockito, U=순수) |
|---|---|
| 루트 | `FreamBackApplicationTests`(SpringBootTest), `domain/PersistenceSmokeTest`(S) |
| address | `entity/AddressTest`(U), `repository/AddressRepositoryTest`(S), `service/command/AddressCommandServiceTest`(M), `service/query/AddressQueryServiceTest`(M), `service/query/AddressEncryptionRoundTripTest`(M) |
| notification | `entity/NotificationEntityTest`(U), `repository/NotificationRepositoryTest`(S), `service/NotificationCommandServiceTest`(M), `service/NotificationQueryServiceTest`(M) |
| order | `OrderBidCommandServiceTest`(M), `OrderBidRepositoryTest`(S), `OrderCommandServiceTest`(M), `OrderEntityStateTest`(U) |
| payment | `PaymentCardUtilsTest`(U), `PaymentCommandServiceTest`(M), `PaymentEntityStateTest`(U), `PaymentInfoServiceTest`(M), `PaymentQueryServiceTest`(M), `PaymentRepositoryTest`(S) |
| product | `repository/ProductQueryDslRepositoryTest`(S), `service/BrandAndCollectionCommandServiceTest`, `InterestCommandServiceTest`, `ProductCommandServiceTest`, `ProductSizeCommandServiceTest`(M) |
| sale | `dto/SaleBidDtoTest`(U), `entity/SaleEntityTest`(U), `repository/SaleBidRepositoryTest`(S), `service/SaleBidCommandServiceTest`, `SaleCommandServiceTest`(M) |
| style | `repository/StyleRelationRepositoryTest`(S), `repository/StyleRepositoryCustomImplTest`(S), `service/HashtagCommandServiceTest`, `StyleCommandServiceTest`, `StyleCommentCommandServiceTest`, `StyleLikeAndInterestCommandServiceTest`(M) |
| user | `entity/PointTest`(U), `repository/PointRepositoryTest`(S), `repository/UserRepositoryTest`(S), `service/BlockProfile/BlockedProfileCommandServiceTest`, `service/admin/AdminSanctionServiceTest`, `service/command/UserCommandServiceTest`, `service/follow/FollowCommandServiceTest`, `service/point/PointCommandServiceTest`(M) |
| 테스트 없음 | accessLog, weather, monitoring, event, faq, inquiry, inspection, notice, shipment, warehouseStorage, chatQuestion, global |

## 7. 실행 방법

```bash
./gradlew test                                   # 전체 (FreamBackApplicationTests는 prod 인프라 필요할 수 있음)
./gradlew test --tests 'com.fream.back.domain.*' # 도메인 테스트만 (H2/Mockito)
./gradlew clean build -x test                    # jar: build/libs/fream-back-0.0.1-SNAPSHOT.jar
./gradlew bootRun --args='--spring.profiles.active=local'   # application-local.yml 준비 필요
```

## 8. 관찰된 gotcha

1. 기본 active 프로필이 **prod**. 프로필 미지정 실행/테스트는 prod 설정(MySQL `mysql` 호스트 등)을 탐.
2. prod `ddl-auto: create` + 프로필 무관 `DataInitializer` → 기동 시마다 스키마 재생성·시드 삽입.
3. `spring.kafka.order.*`, `payment.kafka.*`, `access-log.kafka.*` yml 키는 Java 코드에서 참조되지 않음(토픽/그룹은 하드코딩).
4. Redis: prod yml에는 `spring.redis.host/port`만 있고 `RedisConfig`는 `spring.redis.primary.*`/`replica.*`를 읽음(기본값 사용). replica 기본 포트 6380은 compose 내부 포트(6379)와 다름.
5. `docker/prod/scripts/clear_caches.sh`는 `redis-cli -h redis FLUSHDB` — prod compose의 Redis 서비스명은 `redis-primary`/`redis-replica`(`redis` 서비스는 주석).
6. prod compose 볼륨이 Windows 경로(`C:\Users\...`)로 하드코딩.
7. CI 워크플로 비활성, 활성화되어도 `-x test`로 테스트 미실행.
8. Elasticsearch/Kafka/Redis 자동구성이 `@DataJpaTest` 슬라이스에서는 로드되지 않으므로 슬라이스 테스트는 외부 인프라 불필요.
