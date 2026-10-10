# weather 도메인

기준 경로: `src/main/java/com/fream/back/domain/weather/` · 파일 19개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| GET | `/weather/query/current` | `WeatherQueryController#getClosestWeatherData` | `WeatherQueryService#getClosestWeatherData` |
| GET | `/weather/query/today` | `WeatherQueryController#getTodayWeatherData` | `WeatherQueryService#getTodayWeatherData` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `WeatherDataCommandServiceImpl`
  - `fetchAndStore24HourWeatherData` → WeatherApiClient#getWeatherData, this#parseTimestamps, WeatherDataRepository#findByTimestampBetween, this#saveOrUpdateWeatherData, WeatherDataRepository#saveAll~
- `WeatherDataInitializer`
  - `initializeWeatherData` → WeatherDataCommandService#fetchAndStore24HourWeatherData
- `WeatherDataScheduler`
  - `scheduleWeatherDataFetch` → WeatherDataCommandService#fetchAndStore24HourWeatherData
- `WeatherQueryServiceImpl`
  - `getClosestWeatherData` → WeatherDataRepository#findWithinTimeRange
  - `getTodayWeatherData` → WeatherDataRepository#findByTimestampBetween

## 3. 클래스 목록 (역할별)

### controller (1)

- `WeatherQueryController` controller/WeatherQueryController.java · public: getClosestWeatherData, getTodayWeatherData · 주입: WeatherQueryService

### service-command (2)

- `WeatherDataCommandService` (interface) service/command/WeatherDataCommandService.java · public: fetchAndStore24HourWeatherData
- `WeatherDataCommandServiceImpl` service/command/WeatherDataCommandServiceImpl.java · extends/implements WeatherDataCommandService · public: fetchAndStore24HourWeatherData · 주입: WeatherDataRepository, WeatherApiClient

### service-query (2)

- `WeatherQueryService` (interface) service/query/WeatherQueryService.java · public: getClosestWeatherData, getTodayWeatherData
- `WeatherQueryServiceImpl` service/query/WeatherQueryServiceImpl.java · extends/implements WeatherQueryService · public: getClosestWeatherData, getTodayWeatherData · 주입: WeatherDataRepository

### service (1)

- `OpenMeteoWeatherApiClient` api/impl/OpenMeteoWeatherApiClient.java · extends/implements WeatherApiClient · public: getWeatherData · 주입: RestTemplate

### repository (1)

- `WeatherDataRepository` (interface) repository/WeatherDataRepository.java · extends/implements JpaRepository · public: findClosestToTimeWithinRange, findWithinTimeRange, findByTimestamp, findByTimestampBetween, existsByTimestamp

### entity (1)

- `WeatherData` entity/WeatherData.java · public: updateWeatherData

### config (2)

- `WeatherDataInitializer` config/WeatherDataInitializer.java · public: initializeWeatherData · 주입: WeatherDataCommandService
- `WeatherDataScheduler` config/WeatherDataScheduler.java · public: scheduleWeatherDataFetch · 주입: WeatherDataCommandService

### dto (3)

- `WeatherApiResponse` dto/WeatherApiResponse.java
- `WeatherDataDto` dto/WeatherDataDto.java
- `commonDto` dto/commonDto.java

### exception (5)

- `WeatherApiException` exception/WeatherApiException.java
- `WeatherDataException` exception/WeatherDataException.java
- `WeatherErrorCode` exception/WeatherErrorCode.java
- `WeatherException` exception/WeatherException.java
- `WeatherParameterException` exception/WeatherParameterException.java

### other (1)

- `WeatherApiClient` (interface) api/WeatherApiClient.java · public: getWeatherData

### 다른 도메인 참조 (import·주입 기준)

(없음)

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

- `WeatherData` @Entity @Table(indexes = { @Index(name = "idx_weather_data_timestamp", columnList = "timestamp", unique = true) })

## 5. 이벤트·메시징

- 구독 event `ApplicationReadyEvent` @EventListener: `WeatherDataInitializer#initializeWeatherData` ← (소스 내 발행 없음)

## 6. AOP

(없음)

