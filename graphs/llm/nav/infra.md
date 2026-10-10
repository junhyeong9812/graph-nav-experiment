# 인프라 / 설정 / 빌드 네비게이션

## 1. 빌드 (`build.gradle`, `settings.gradle`)
- rootProject `fream-back`, group `com.fream`, version `0.0.1-SNAPSHOT`, Spring Boot 3.4.1, Java toolchain 17.
- 주요 의존성: web, validation, data-jpa, data-redis, websocket, oauth2-client, security, batch, data-elasticsearch, mail, actuator, spring-kafka, httpclient5, springdoc-openapi 2.3.0, java-jwt 3.18.2, jsoup, geoip2, playwright 1.38.0, elasticsearch-java 8.9.1, p6spy, QueryDSL 5.0.0 (jakarta), Lombok. DB 드라이버: h2(runtime), mysql-connector-java 8.0.33.
- QueryDSL Q클래스: annotationProcessor 로 생성, `$buildDir/generated/querydsl` 이 main srcDir 에 추가됨.
- 명령: `./gradlew clean build` / `./gradlew build -x test` / `./gradlew bootRun` / `./gradlew test`. jar: `build/libs/fream-back-0.0.1-SNAPSHOT.jar`.

## 2. 프로필 / 설정 파일
| 파일 | 내용 |
|---|---|
| `src/main/resources/application.yml` | **`spring.profiles.active: prod`** (기본 활성 프로필이 prod). multipart 1GB, pageable(기본 10, max 2000, one-indexed), `jwt.expiration`/`jwt.refreshExpiration` = 3600000ms, actuator exposure `*`, `org.hibernate.SQL: debug` |
| `src/main/resources/application-prod.yml` | 운영 설정 (아래) |
| `src/main/resources/application-test.yml` | H2 메모리 `jdbc:h2:mem:testdb;MODE=MySQL`, `ddl-auto: create-drop` 만 정의 (7줄) |
| `application-local.yml.example` (repo 루트) | 로컬용 예시: H2 + h2-console(`/h2-console`), redis/kafka/ES 모두 localhost, `batch.jdbc.initialize-schema: always`, google OAuth redirect `http://localhost:8080/api/login/oauth2/code/google`, server.port 8080 |
| `.env.example` | 환경변수 키 목록 (아래) |
| `src/main/resources/logback.xml` | 콘솔 + `logs/application.log`, `logs/error.log`(ERROR), `logs/security.log`(`global.config.security`), `logs/file-utils.log`(`global.utils.FileUtils`), `logs/websocket.log`(`global.config.websocket`); 일별 롤링 30일. `global.exception` 로거는 ERROR_FILE 로도 |
| `src/main/resources/GeoLite2-City.mmdb` | access-log GeoIP DB (`access-log.geoip.database-path`) |
| `src/main/resources/templates/admin/monitoring.html` | 관리자 모니터링 템플릿 |

### application-prod.yml 핵심 (값이 아닌 키/호스트만)
- DB: `jdbc:mysql://mysql:3306/freamdb`, username `${DB_USERNAME}`, password `${DB_PASSWORD}`. **`jpa.hibernate.ddl-auto: create`** (기동마다 스키마 재생성).
- Redis: `spring.redis.host: redis`, `port: 6379` — 단, `RedisConfig` 는 `spring.redis.primary.*`/`replica.*` 를 읽으므로 기본값 `redis-primary:6379` / `redis-replica:6380` 이 실제 사용됨.
- Mail: smtp.gmail.com:587, `${MAIL_USERNAME}`, `${MAIL_PASSWORD}`.
- Kafka: `kafka:9092`, producer acks all/idempotence, consumer group-id `my-group`, auto-commit false. 문서용 `spring.kafka.order.*`, `spring.kafka.payment.*`, `payment.kafka.*` 블록(토픽/그룹명).
- Elasticsearch: `http://elasticsearch:9200`. `spring.batch.jdbc.initialize-schema: never`.
- OAuth2: google / naver / kakao (`${GOOGLE_CLIENT_ID}` 등), redirect `https://www.pinjun.xyz/api/login/oauth2/code/{provider}`. naver/kakao provider URI 정의.
- actuator: health,info,metrics. `kibana.url: https://www.pinjun.xyz/kibana`.
- logging: `logging.file.name: /logs/spring-app.log`, rolling 10MB/30일/3GB, `org.springframework.security: DEBUG`.
- 키 이름: `jwt.secret` (`${JWT_SECRET}`), `jwt.token-validity-in-seconds`, `imp.key`/`imp.secret`/`imp.pg`/`imp.storeId` (포트원), `personal-data.encryption.secret-key`/`iv` (`${PERSONAL_ENCRYPTION_KEY}`/`${PERSONAL_ENCRYPTION_IV}`), `openai.api.key` (`${OPENAPI_KEY}`), `openai.api.url`, `openai.model`, `payment.encryption.secret-key`/`salt`/`iv` (**env 가 아닌 리터럴 값이 yml 에 있음**), `payment.card-info.max-count: 5`, `payment.retry.*`, `weather.api.*`(open-meteo, scheduler interval 86400000), `access-log.use-kafka: false`, `access-log.kafka.topic`, `server.use-forward-headers: true`.
- `.env.example` 키: `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `IMP_KEY`, `IMP_SECRET`, `IMP_PG`, `IMP_STOREID`, `GOOGLE_CLIENT_ID/SECRET`, `NAVER_CLIENT_ID/SECRET`, `KAKAO_CLIENT_ID/SECRET`, `OPENAPI_KEY` (+ 주석: `ES_JAVA_OPTS`, `PROMETHEUS_RETENTION_DAYS`, `GRAFANA_ADMIN_PASSWORD`). `PERSONAL_ENCRYPTION_KEY/IV` 는 `.env.example` 에 없음.

### 하드코딩된 경로
- 파일 저장: `/home/ubuntu/fream` (`global/utils/FileUtils.java`, notice/event/product/style 일부 클래스에도 중복).
- 로그 뷰어: `logging.file.path` (기본 `/home/ubuntu/springlog`).

## 3. Docker
### `docker/Dockerfile` (앱)
`openjdk:17-jdk-slim` + Playwright/Chromium 의존 lib, `build/libs/fream-back-0.0.1-SNAPSHOT.jar` → `/app.jar`, EXPOSE 8080. (먼저 `./gradlew build` 필요)

### `docker/prod/docker-compose.prod.yml` (볼륨은 Windows 경로 `C:\Users\pickj\webserver\dockerVolums\...` 바인딩)
| 서비스 | 빌드/이미지 | 포트 |
|---|---|---|
| mysql | `docker/mysql/Dockerfile-mysql` (mysql:8.0, DB `freamdb`, user `fream`) | 3306 |
| redis-primary | `docker/redis/Dockerfile-redis-primary` (redis:7.0, AOF) | 6379 |
| redis-replica | `docker/redis/Dockerfile-redis-replica` (`replicaof redis-primary 6379`, read-only) | 6380→6379 |
| zookeeper | `docker/kafka/Dockerfile-zookeeper` (cp-zookeeper 7.4.0) | 2181 |
| kafka | `docker/kafka/Dockerfile-kafka` (cp-kafka 7.4.0, advertised `kafka:9092`) | 9092 |
| elasticsearch | `docker/elasticsearch/Dockerfile-elasticsearch` (8.13.4 + analysis-nori, userdict_ko.txt, synonyms.txt, security off) | 9200, 9300 |
| kibana | `docker/elasticsearch/Dockerfile-kibana` (8.10.2, basePath `/kibana`) | 5601 |
| nginx | `docker/prod/nginx/Dockerfile-nginx-prod` (nginx 1.25.2 소스빌드 + ngx_cache_purge 2.3) | 80, 443 |
| app | `docker/Dockerfile`, `SPRING_PROFILES_ACTIVE=prod`, `env_file: .env`, 볼륨 → `/home/ubuntu/fream`, `/logs` | 8080 |
| front | `../../../fream-front` (별도 저장소) | — |
| stt_tts | `../../../fream-stt-tts` (Flask, 별도 저장소) | 5000 |
| certbot | `docker/prod/nginx/certbot/Dockerfile-certbot`, `www.pinjun.xyz` 인증서 발급/12h renew | — |
| cache-cleaner | alpine cron `0 2 * * *` → `docker/prod/scripts/clear_caches.sh` (ES `_cache/clear`, `redis-cli -h redis FLUSHDB`) | — |
| metricbeat | 8.13.4, `docker/prod/metricbeat.yml` (system, `app:8080/actuator/metrics`, nginx stub_status) | — |
- 파일 하단에 구 버전 compose 전체(ghcr 이미지, prometheus/loki/grafana 등)가 주석으로 남아 있음.
- 주의: `nginx`, `cache-cleaner` 가 `depends_on: redis` 인데 `redis` 서비스는 주석 처리됨(redis-primary/replica 만 존재). `clear_caches.sh` 도 호스트 `redis` 사용.
- 주의: redis primary/replica Dockerfile 의 `COPY redis-*.conf` 는 build context(repo 루트) 기준 경로이고, 실제 파일은 `docker/redis/` 에 있음.

### `docker/dev/docker-compose.dev.yml`
redis(6379), zookeeper(2181), kafka(9092 내부 / 9095 호스트 `localhost:9095`), elasticsearch(9200/9300), kibana(5601), nginx(80, `host.docker.internal:8080` 으로 프록시). 앱은 호스트에서 실행하는 구성. nginx dockerfile 경로가 `docker/nginx/Dockerfile-nginx` 로 지정돼 있으나 실제 파일은 `docker/dev/nginx/Dockerfile-nginx-dev`.

### 기타
- `docker/prod/prometheus/prometheus.yml`: node-exporter, nginx-exporter, `app:8080/actuator/prometheus` (단 micrometer-registry-prometheus 의존성은 build.gradle 에서 주석). compose 의 prometheus 서비스는 주석.
- `docker/prod/promtail/promtail-config.yml`: loki push, nginx 로그 + Windows 경로 spring 로그 (compose 의 promtail 은 주석).
- `docker/prod/nginx/certs/localhost*.pem` — 로컬 IP(192.168.55.164)용 자체 인증서.

## 4. Nginx 라우팅 (`docker/prod/nginx/nginx.conf`)
- :80 `www.pinjun.xyz` → acme-challenge, `/stub_status`(내부망만), 나머지 301 https. `localhost 192.168.55.164` :80 → 301.
- :443 `www.pinjun.xyz` (letsencrypt) 및 `192.168.55.164` (default_server, 자체 인증서) 동일 라우팅:
  | location | 대상 |
  |---|---|
  | `/` | `front:80` |
  | `/api/ws` | `app:8080/ws` (Upgrade, 86400s timeout) |
  | `/api/` | `app:8080/` (**`/api` 접두어 제거**) |
  | `/api/products/query` | `app:8080/products/query`, 캐시 `cache_products` 200→10m, 404→1m |
  | `~ /api/products/query/{id}/detail`, `/images` | rewrite, 캐시 bypass |
  | `/api/styles/queries` | 캐시 `cache_styles` (쿼리스트링 있으면 bypass) |
  | `~ /api/styles/queries/{id}`, `/{id}/media/...` | 캐시 bypass |
  | `/api/es/products` | rewrite → `/es/products...`, 캐시 `cache_es` |
  | `~ /purge_products(/.*)`, `/purge_styles`, `/purge_es` | `proxy_cache_purge` (`allow all`) |
  | `/kibana/` | `kibana:5601/kibana/` |
  | `/stt-tts/`, `/stt-tts/api/`, `/stt-tts/health`, `/talk/`, `/talk/static/` | `stt_tts:5000` (100M body) |
  | `/stacknote`, `/stacknote/api/`, `/stacknote/ws/` | 다른 프로젝트 컨테이너 (`stacknote-frontend:3001`, `stacknote-app:8080`) |
- 캐시 키 `"$scheme://$host$request_uri"` 인데 purge 키는 `$scheme$host$1` (형식 다름). purge location 은 443 서버에만 있고, `NginxCachePurgeUtil` 은 `http://nginx:80` 으로 요청함.
- dev nginx (`docker/dev/nginx/nginx.conf`): :80 localhost, `/` 및 `/api/products`, `/api/styles/queries`, `/api/es/products` 를 `host.docker.internal:8080` 으로 (prefix 제거 없음) + 캐시/purge.

## 5. CI/CD (`.github/workflows/ci-cd.yml`)
- **파일 전체가 주석 처리 → 현재 비활성.**
- 주석 내용: main push/PR → JDK17(temurin) → `./gradlew clean build -x test` → GHCR 로그인(`secrets.GHCR_TOKEN`) → `docker build -f ./docker/Dockerfile -t ghcr.io/junhyeong9812/fream-back:latest .` → push → deploy job: SSH(EC2_HOST/EC2_USER/EC2_SSH_KEY) → `/home/ubuntu/fream-back/docker/prod` 에서 `docker-compose -f docker-compose.prod.yml pull app && up -d app`.

## 6. 실행 / 테스트
- 로컬 실행: `application-local.yml.example` 을 `src/main/resources/application-local.yml` 등으로 복사 후 `--spring.profiles.active=local` 지정 필요 (기본 활성이 prod). Redis/Kafka/ES 는 `docker/dev/docker-compose.dev.yml` 로 기동 가능 (dev kafka 호스트 포트 9095 vs local 예시 `localhost:9092` 주의). RedisConfig 는 `spring.redis.primary.host` 기본값 `redis-primary` 를 쓰므로 로컬에서는 해당 키를 설정해야 함.
- 운영: `./gradlew build -x test` → `cd docker/prod && docker-compose -f docker-compose.prod.yml up -d` (`.env` 필요).
- **테스트 현황**: `src/test/java/com/fream/back/FreamBackApplicationTests.java` 1개 (`@SpringBootTest contextLoads()`), `@ActiveProfiles` 없음 → 기본 prod 프로필로 MySQL/Redis/Kafka/ES 접속 시도. `application-test.yml` 은 존재하지만 사용하려면 `-Dspring.profiles.active=test` 등 지정 필요하고 datasource 외 설정은 없음. CI 에서도 테스트는 `-x test` 로 스킵되어 있었음.
