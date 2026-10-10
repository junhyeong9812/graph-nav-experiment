# weather 도메인 (날씨 데이터 수집/조회)

## 1. 개요
- Open-Meteo 예보 API를 서버 시작 시 1회 + 주기적으로 호출해 시간별 날씨(기온, 강수확률, 강수량, 비, 눈)를 `WeatherData`에 upsert.
- 조회 API: 현재 시각에 가장 가까운 데이터, 오늘 전체 데이터.

## 2. 패키지/파일 구조
루트: `src/main/java/com/fream/back/domain/weather/`
| 경로 | 역할 |
|---|---|
| `api/WeatherApiClient.java`, `api/impl/OpenMeteoWeatherApiClient.java` | 외부 API 클라이언트 |
| `config/WeatherDataInitializer.java` | `ApplicationReadyEvent` 시 1회 수집 |
| `config/WeatherDataScheduler.java` | `@Scheduled` 주기 수집 (`@EnableScheduling`) |
| `controller/WeatherQueryController.java` | `/weather/query/**` |
| `service/command/WeatherDataCommandService{,Impl}.java` | `fetchAndStore24HourWeatherData()` |
| `service/query/WeatherQueryService{,Impl}.java` | 조회 |
| `entity/WeatherData.java`, `repository/WeatherDataRepository.java` | 영속 |
| `dto/WeatherApiResponse.java`(API 응답 매핑, `hourly`), `dto/WeatherDataDto.java`(`fromEntity`), `dto/commonDto.java`(`PageDto<T>`, 미사용) | DTO |
| `exception/WeatherErrorCode.java`, `WeatherException.java`(정적 팩토리), `WeatherApiException`, `WeatherDataException`, `WeatherParameterException` | 예외 |

## 3. 엔티티
`WeatherData` (인덱스 `idx_weather_data_timestamp(timestamp)` unique)
- `id` IDENTITY, `timestamp`(not null, unique), `temperature`, `precipitationProbability`, `precipitation`, `rain`, `snowfall`, `retrievedAt`(`retrieved_at`)
- `updateWeatherData(...)` (timestamp 제외 갱신). 연관관계/`BaseTimeEntity` 없음.

## 4. API 엔드포인트
SecurityConfig whitelist 밖 → 인증 필요(메서드 레벨 권한 없음).
| Method | Path | Controller#method | Service#method | 응답 |
|---|---|---|---|---|
| GET | `/weather/query/current` | `WeatherQueryController#getClosestWeatherData` | `WeatherQueryServiceImpl#getClosestWeatherData` | 200 `WeatherDataDto` / 204 |
| GET | `/weather/query/today` | `WeatherQueryController#getTodayWeatherData` | `WeatherQueryServiceImpl#getTodayWeatherData` | 200 `List<WeatherDataDto>` / 204 |

## 5. 서비스 계층
- `WeatherDataCommandServiceImpl#fetchAndStore24HourWeatherData` (`@Transactional`): API 호출 → `min(weather.data.hours-count(기본 48), 응답 시간 수)`개 timestamp 파싱(`ISO_DATE_TIME`) → `findByTimestampBetween(start,end)`로 기존 맵 구성 → 존재 시 update, 없으면 insert → `saveAll`.
- `WeatherQueryServiceImpl` (클래스 `@Transactional(readOnly=true)`):
  - `getClosestWeatherData()`: `findWithinTimeRange(now-1h, now+1h)` 후 Java에서 |Δt| 최소 선택
  - `getTodayWeatherData()`: 오늘 00:00~23:59:59 `findByTimestampBetween` 후 시간순 정렬

## 6. Repository
`WeatherDataRepository`
- `@Query findClosestToTimeWithinRange(target, start, end)` — `ORDER BY ABS(TIMESTAMPDIFF(SECOND, ...))`, 반환 `Optional` (서비스에서 미사용)
- `@Query findWithinTimeRange(start, end)`
- 파생: `findByTimestamp`, `findByTimestampBetween`, `existsByTimestamp`

## 7. 외부 연동 / 스케줄러
- 외부 API: `OpenMeteoWeatherApiClient#getWeatherData` — `RestTemplate.getForObject`, URL = `${weather.api.url}` + `latitude`, `longitude`, `hourly`, `timezone` 쿼리 파라미터.
- 설정 (`application-prod.yml` L211-226):
  - `weather.api.url: https://api.open-meteo.com/v1/forecast`
  - `weather.api.params.latitude: 36.5`, `longitude: 127.75`, `hourly: temperature_2m,precipitation_probability,precipitation,rain,snowfall`, `timezone: auto`
  - `weather.scheduler.interval: 86400000` (24h), `weather.scheduler.initial-delay: 10000`
  - `weather.data.hours-count: 48`
- 스케줄러: `WeatherDataScheduler#scheduleWeatherDataFetch` — `@Scheduled(fixedRateString="${weather.scheduler.interval:86400000}", initialDelayString="${weather.scheduler.initial-delay:10000}")` (cron 아님). 예외는 로그만.
- 초기화: `WeatherDataInitializer#initializeWeatherData` — `@EventListener(ApplicationReadyEvent.class)`, 예외는 로그만.
- Kafka/AOP 없음.

## 8. 다른 도메인과의 의존
- 다른 도메인 의존 없음, 다른 도메인에서 weather를 import하지 않음 (`GlobalExceptionHandler`의 `WeatherException` 핸들러만).
- global: `RestTemplate` 빈 (`src/main/java/com/fream/back/global/config/RestTemplateConfig.java`).

## 9. 예외
`WeatherErrorCode`, 접두사 `WE`:
- `WE001` WEATHER_API_ERROR(500), `WE002` WEATHER_API_PARSING_ERROR(500)
- `WE101` WEATHER_DATA_NOT_FOUND(404), `WE102` WEATHER_DATA_SAVE_ERROR(500), `WE103` WEATHER_DATA_QUERY_ERROR(500)
- `WE201` INVALID_DATETIME_FORMAT(400), `WE202` INVALID_TIME_RANGE(400)
- `WeatherException` 정적 팩토리: `apiError`, `apiParsingError`, `dataNotFound`, `dataSaveError`, `dataQueryError`, `invalidDateTimeFormat`, `invalidTimeRange`.

## 10. 작업 팁 / 주의점
- 기동 직후 Initializer(ApplicationReadyEvent)와 Scheduler(initial-delay 10초)가 모두 실행되어 시작 시 API가 2번 호출됨. upsert라 데이터 중복은 없지만 동시 실행 시 unique(timestamp) 충돌 가능성은 있음.
- 메서드명은 "24Hour"지만 실제 처리 개수는 `weather.data.hours-count`(prod 48).
- `timezone: auto`이므로 저장 timestamp는 위경도 기준 현지 시간(LocalDateTime). 조회는 서버 `LocalDateTime.now()` 기준.
- `weather.*` 설정은 `application-prod.yml`에만 있음. `weather.api.*`는 `@Value` 기본값이 없어 다른 프로필에서 누락 시 기동 실패.
- `WeatherApiException`/`WeatherDataException`/`WeatherParameterException` 하위 클래스는 존재하나 서비스는 `WeatherException` 정적 팩토리를 사용.
