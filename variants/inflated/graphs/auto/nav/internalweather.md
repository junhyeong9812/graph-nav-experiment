# internalweather 도메인

기준 경로: `src/main/java/com/fream/back/domain/internalweather/` · 파일 10개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| GET | `/internal/weather/query/current` | `InternalWeatherQueryController#getClosestWeatherData` | `InternalWeatherQueryService#getClosestWeatherData` |
| GET | `/internal/weather/query/today` | `InternalWeatherQueryController#getTodayWeatherData` | `InternalWeatherQueryService#getTodayWeatherData` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `InternalWeatherDataCommandServiceImpl`
  - `fetchAndStore24HourWeatherData` → InternalWeatherApiClient#getWeatherData, this#parseTimestamps, InternalWeatherDataRepository#findByTimestampBetween, this#saveOrUpdateWeatherData, InternalWeatherDataRepository#saveAll~
- `InternalWeatherDataInitializer`
  - `initializeWeatherData` → InternalWeatherDataCommandService#fetchAndStore24HourWeatherData
- `InternalWeatherDataScheduler`
  - `scheduleWeatherDataFetch` → InternalWeatherDataCommandService#fetchAndStore24HourWeatherData
- `InternalWeatherQueryServiceImpl`
  - `getClosestWeatherData` → InternalWeatherDataRepository#findWithinTimeRange
  - `getTodayWeatherData` → InternalWeatherDataRepository#findByTimestampBetween

## 3. 클래스 목록 (역할별)

### controller (1)

- `InternalWeatherQueryController` controller/InternalWeatherQueryController.java · public: getClosestWeatherData, getTodayWeatherData · 주입: InternalWeatherQueryService

### service-command (2)

- `InternalWeatherDataCommandService` (interface) service/command/InternalWeatherDataCommandService.java · public: fetchAndStore24HourWeatherData
- `InternalWeatherDataCommandServiceImpl` service/command/InternalWeatherDataCommandServiceImpl.java · extends/implements InternalWeatherDataCommandService · public: fetchAndStore24HourWeatherData · 주입: InternalWeatherDataRepository, InternalWeatherApiClient

### service-query (2)

- `InternalWeatherQueryService` (interface) service/query/InternalWeatherQueryService.java · public: getClosestWeatherData, getTodayWeatherData
- `InternalWeatherQueryServiceImpl` service/query/InternalWeatherQueryServiceImpl.java · extends/implements InternalWeatherQueryService · public: getClosestWeatherData, getTodayWeatherData · 주입: InternalWeatherDataRepository

### service (1)

- `InternalOpenMeteoWeatherApiClient` api/impl/InternalOpenMeteoWeatherApiClient.java · extends/implements InternalWeatherApiClient · public: getWeatherData · 주입: RestTemplate

### repository (1)

- `InternalWeatherDataRepository` (interface) repository/InternalWeatherDataRepository.java · extends/implements JpaRepository · public: findClosestToTimeWithinRange, findWithinTimeRange, findByTimestamp, findByTimestampBetween, existsByTimestamp

### config (2)

- `InternalWeatherDataInitializer` config/InternalWeatherDataInitializer.java · public: initializeWeatherData · 주입: InternalWeatherDataCommandService
- `InternalWeatherDataScheduler` config/InternalWeatherDataScheduler.java · public: scheduleWeatherDataFetch · 주입: InternalWeatherDataCommandService

### other (1)

- `InternalWeatherApiClient` (interface) api/InternalWeatherApiClient.java · public: getWeatherData

### 다른 도메인 참조 (import·주입 기준)

- **weather**: `WeatherData` ← InternalWeatherDataCommandServiceImpl, InternalWeatherDataRepository, InternalWeatherQueryServiceImpl

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

- 구독 event `ApplicationReadyEvent` @EventListener: `InternalWeatherDataInitializer#initializeWeatherData` ← (소스 내 발행 없음)

## 6. AOP

(없음)

