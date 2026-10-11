# adminweather 도메인

기준 경로: `src/main/java/com/fream/back/domain/adminweather/` · 파일 10개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| GET | `/admin/weather/query/current` | `AdminWeatherQueryController#getClosestWeatherData` | `AdminWeatherQueryService#getClosestWeatherData` |
| GET | `/admin/weather/query/today` | `AdminWeatherQueryController#getTodayWeatherData` | `AdminWeatherQueryService#getTodayWeatherData` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `AdminWeatherDataCommandServiceImpl`
  - `fetchAndStore24HourWeatherData` → AdminWeatherApiClient#getWeatherData, this#parseTimestamps, AdminWeatherDataRepository#findByTimestampBetween, this#saveOrUpdateWeatherData, AdminWeatherDataRepository#saveAll~
- `AdminWeatherDataInitializer`
  - `initializeWeatherData` → AdminWeatherDataCommandService#fetchAndStore24HourWeatherData
- `AdminWeatherDataScheduler`
  - `scheduleWeatherDataFetch` → AdminWeatherDataCommandService#fetchAndStore24HourWeatherData
- `AdminWeatherQueryServiceImpl`
  - `getClosestWeatherData` → AdminWeatherDataRepository#findWithinTimeRange
  - `getTodayWeatherData` → AdminWeatherDataRepository#findByTimestampBetween

## 3. 클래스 목록 (역할별)

### controller (1)

- `AdminWeatherQueryController` controller/AdminWeatherQueryController.java · public: getClosestWeatherData, getTodayWeatherData · 주입: AdminWeatherQueryService

### service-command (2)

- `AdminWeatherDataCommandService` (interface) service/command/AdminWeatherDataCommandService.java · public: fetchAndStore24HourWeatherData
- `AdminWeatherDataCommandServiceImpl` service/command/AdminWeatherDataCommandServiceImpl.java · extends/implements AdminWeatherDataCommandService · public: fetchAndStore24HourWeatherData · 주입: AdminWeatherDataRepository, AdminWeatherApiClient

### service-query (2)

- `AdminWeatherQueryService` (interface) service/query/AdminWeatherQueryService.java · public: getClosestWeatherData, getTodayWeatherData
- `AdminWeatherQueryServiceImpl` service/query/AdminWeatherQueryServiceImpl.java · extends/implements AdminWeatherQueryService · public: getClosestWeatherData, getTodayWeatherData · 주입: AdminWeatherDataRepository

### service (1)

- `AdminOpenMeteoWeatherApiClient` api/impl/AdminOpenMeteoWeatherApiClient.java · extends/implements AdminWeatherApiClient · public: getWeatherData · 주입: RestTemplate

### repository (1)

- `AdminWeatherDataRepository` (interface) repository/AdminWeatherDataRepository.java · extends/implements JpaRepository · public: findClosestToTimeWithinRange, findWithinTimeRange, findByTimestamp, findByTimestampBetween, existsByTimestamp

### config (2)

- `AdminWeatherDataInitializer` config/AdminWeatherDataInitializer.java · public: initializeWeatherData · 주입: AdminWeatherDataCommandService
- `AdminWeatherDataScheduler` config/AdminWeatherDataScheduler.java · public: scheduleWeatherDataFetch · 주입: AdminWeatherDataCommandService

### other (1)

- `AdminWeatherApiClient` (interface) api/AdminWeatherApiClient.java · public: getWeatherData

### 다른 도메인 참조 (import·주입 기준)

- **weather**: `WeatherData` ← AdminWeatherDataCommandServiceImpl, AdminWeatherDataRepository, AdminWeatherQueryServiceImpl

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

- 구독 event `ApplicationReadyEvent` @EventListener: `AdminWeatherDataInitializer#initializeWeatherData` ← (소스 내 발행 없음)

## 6. AOP

(없음)

