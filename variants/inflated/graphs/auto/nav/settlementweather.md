# settlementweather 도메인

기준 경로: `src/main/java/com/fream/back/domain/settlementweather/` · 파일 10개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| GET | `/settlement/weather/query/current` | `SettlementWeatherQueryController#getClosestWeatherData` | `SettlementWeatherQueryService#getClosestWeatherData` |
| GET | `/settlement/weather/query/today` | `SettlementWeatherQueryController#getTodayWeatherData` | `SettlementWeatherQueryService#getTodayWeatherData` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `SettlementWeatherDataCommandServiceImpl`
  - `fetchAndStore24HourWeatherData` → SettlementWeatherApiClient#getWeatherData, this#parseTimestamps, SettlementWeatherDataRepository#findByTimestampBetween, this#saveOrUpdateWeatherData, SettlementWeatherDataRepository#saveAll~
- `SettlementWeatherDataInitializer`
  - `initializeWeatherData` → SettlementWeatherDataCommandService#fetchAndStore24HourWeatherData
- `SettlementWeatherDataScheduler`
  - `scheduleWeatherDataFetch` → SettlementWeatherDataCommandService#fetchAndStore24HourWeatherData
- `SettlementWeatherQueryServiceImpl`
  - `getClosestWeatherData` → SettlementWeatherDataRepository#findWithinTimeRange
  - `getTodayWeatherData` → SettlementWeatherDataRepository#findByTimestampBetween

## 3. 클래스 목록 (역할별)

### controller (1)

- `SettlementWeatherQueryController` controller/SettlementWeatherQueryController.java · public: getClosestWeatherData, getTodayWeatherData · 주입: SettlementWeatherQueryService

### service-command (2)

- `SettlementWeatherDataCommandService` (interface) service/command/SettlementWeatherDataCommandService.java · public: fetchAndStore24HourWeatherData
- `SettlementWeatherDataCommandServiceImpl` service/command/SettlementWeatherDataCommandServiceImpl.java · extends/implements SettlementWeatherDataCommandService · public: fetchAndStore24HourWeatherData · 주입: SettlementWeatherDataRepository, SettlementWeatherApiClient

### service-query (2)

- `SettlementWeatherQueryService` (interface) service/query/SettlementWeatherQueryService.java · public: getClosestWeatherData, getTodayWeatherData
- `SettlementWeatherQueryServiceImpl` service/query/SettlementWeatherQueryServiceImpl.java · extends/implements SettlementWeatherQueryService · public: getClosestWeatherData, getTodayWeatherData · 주입: SettlementWeatherDataRepository

### service (1)

- `SettlementOpenMeteoWeatherApiClient` api/impl/SettlementOpenMeteoWeatherApiClient.java · extends/implements SettlementWeatherApiClient · public: getWeatherData · 주입: RestTemplate

### repository (1)

- `SettlementWeatherDataRepository` (interface) repository/SettlementWeatherDataRepository.java · extends/implements JpaRepository · public: findClosestToTimeWithinRange, findWithinTimeRange, findByTimestamp, findByTimestampBetween, existsByTimestamp

### config (2)

- `SettlementWeatherDataInitializer` config/SettlementWeatherDataInitializer.java · public: initializeWeatherData · 주입: SettlementWeatherDataCommandService
- `SettlementWeatherDataScheduler` config/SettlementWeatherDataScheduler.java · public: scheduleWeatherDataFetch · 주입: SettlementWeatherDataCommandService

### other (1)

- `SettlementWeatherApiClient` (interface) api/SettlementWeatherApiClient.java · public: getWeatherData

### 다른 도메인 참조 (import·주입 기준)

- **weather**: `WeatherData` ← SettlementWeatherDataCommandServiceImpl, SettlementWeatherDataRepository, SettlementWeatherQueryServiceImpl

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

- 구독 event `ApplicationReadyEvent` @EventListener: `SettlementWeatherDataInitializer#initializeWeatherData` ← (소스 내 발행 없음)

## 6. AOP

(없음)

