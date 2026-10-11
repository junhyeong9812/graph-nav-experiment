# legacyweather 도메인

기준 경로: `src/main/java/com/fream/back/domain/legacyweather/` · 파일 10개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| GET | `/legacy/weather/query/current` | `LegacyWeatherQueryController#getClosestWeatherData` | `LegacyWeatherQueryService#getClosestWeatherData` |
| GET | `/legacy/weather/query/today` | `LegacyWeatherQueryController#getTodayWeatherData` | `LegacyWeatherQueryService#getTodayWeatherData` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `LegacyWeatherDataCommandServiceImpl`
  - `fetchAndStore24HourWeatherData` → LegacyWeatherApiClient#getWeatherData, this#parseTimestamps, LegacyWeatherDataRepository#findByTimestampBetween, this#saveOrUpdateWeatherData, LegacyWeatherDataRepository#saveAll~
- `LegacyWeatherDataInitializer`
  - `initializeWeatherData` → LegacyWeatherDataCommandService#fetchAndStore24HourWeatherData
- `LegacyWeatherDataScheduler`
  - `scheduleWeatherDataFetch` → LegacyWeatherDataCommandService#fetchAndStore24HourWeatherData
- `LegacyWeatherQueryServiceImpl`
  - `getClosestWeatherData` → LegacyWeatherDataRepository#findWithinTimeRange
  - `getTodayWeatherData` → LegacyWeatherDataRepository#findByTimestampBetween

## 3. 클래스 목록 (역할별)

### controller (1)

- `LegacyWeatherQueryController` controller/LegacyWeatherQueryController.java · public: getClosestWeatherData, getTodayWeatherData · 주입: LegacyWeatherQueryService

### service-command (2)

- `LegacyWeatherDataCommandService` (interface) service/command/LegacyWeatherDataCommandService.java · public: fetchAndStore24HourWeatherData
- `LegacyWeatherDataCommandServiceImpl` service/command/LegacyWeatherDataCommandServiceImpl.java · extends/implements LegacyWeatherDataCommandService · public: fetchAndStore24HourWeatherData · 주입: LegacyWeatherDataRepository, LegacyWeatherApiClient

### service-query (2)

- `LegacyWeatherQueryService` (interface) service/query/LegacyWeatherQueryService.java · public: getClosestWeatherData, getTodayWeatherData
- `LegacyWeatherQueryServiceImpl` service/query/LegacyWeatherQueryServiceImpl.java · extends/implements LegacyWeatherQueryService · public: getClosestWeatherData, getTodayWeatherData · 주입: LegacyWeatherDataRepository

### service (1)

- `LegacyOpenMeteoWeatherApiClient` api/impl/LegacyOpenMeteoWeatherApiClient.java · extends/implements LegacyWeatherApiClient · public: getWeatherData · 주입: RestTemplate

### repository (1)

- `LegacyWeatherDataRepository` (interface) repository/LegacyWeatherDataRepository.java · extends/implements JpaRepository · public: findClosestToTimeWithinRange, findWithinTimeRange, findByTimestamp, findByTimestampBetween, existsByTimestamp

### config (2)

- `LegacyWeatherDataInitializer` config/LegacyWeatherDataInitializer.java · public: initializeWeatherData · 주입: LegacyWeatherDataCommandService
- `LegacyWeatherDataScheduler` config/LegacyWeatherDataScheduler.java · public: scheduleWeatherDataFetch · 주입: LegacyWeatherDataCommandService

### other (1)

- `LegacyWeatherApiClient` (interface) api/LegacyWeatherApiClient.java · public: getWeatherData

### 다른 도메인 참조 (import·주입 기준)

- **weather**: `WeatherData` ← LegacyWeatherDataCommandServiceImpl, LegacyWeatherDataRepository, LegacyWeatherQueryServiceImpl

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

- 구독 event `ApplicationReadyEvent` @EventListener: `LegacyWeatherDataInitializer#initializeWeatherData` ← (소스 내 발행 없음)

## 6. AOP

(없음)

