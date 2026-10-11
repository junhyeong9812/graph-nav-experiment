# partnerweather 도메인

기준 경로: `src/main/java/com/fream/back/domain/partnerweather/` · 파일 10개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| GET | `/partner/weather/query/current` | `PartnerWeatherQueryController#getClosestWeatherData` | `PartnerWeatherQueryService#getClosestWeatherData` |
| GET | `/partner/weather/query/today` | `PartnerWeatherQueryController#getTodayWeatherData` | `PartnerWeatherQueryService#getTodayWeatherData` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `PartnerWeatherDataCommandServiceImpl`
  - `fetchAndStore24HourWeatherData` → PartnerWeatherApiClient#getWeatherData, this#parseTimestamps, PartnerWeatherDataRepository#findByTimestampBetween, this#saveOrUpdateWeatherData, PartnerWeatherDataRepository#saveAll~
- `PartnerWeatherDataInitializer`
  - `initializeWeatherData` → PartnerWeatherDataCommandService#fetchAndStore24HourWeatherData
- `PartnerWeatherDataScheduler`
  - `scheduleWeatherDataFetch` → PartnerWeatherDataCommandService#fetchAndStore24HourWeatherData
- `PartnerWeatherQueryServiceImpl`
  - `getClosestWeatherData` → PartnerWeatherDataRepository#findWithinTimeRange
  - `getTodayWeatherData` → PartnerWeatherDataRepository#findByTimestampBetween

## 3. 클래스 목록 (역할별)

### controller (1)

- `PartnerWeatherQueryController` controller/PartnerWeatherQueryController.java · public: getClosestWeatherData, getTodayWeatherData · 주입: PartnerWeatherQueryService

### service-command (2)

- `PartnerWeatherDataCommandService` (interface) service/command/PartnerWeatherDataCommandService.java · public: fetchAndStore24HourWeatherData
- `PartnerWeatherDataCommandServiceImpl` service/command/PartnerWeatherDataCommandServiceImpl.java · extends/implements PartnerWeatherDataCommandService · public: fetchAndStore24HourWeatherData · 주입: PartnerWeatherDataRepository, PartnerWeatherApiClient

### service-query (2)

- `PartnerWeatherQueryService` (interface) service/query/PartnerWeatherQueryService.java · public: getClosestWeatherData, getTodayWeatherData
- `PartnerWeatherQueryServiceImpl` service/query/PartnerWeatherQueryServiceImpl.java · extends/implements PartnerWeatherQueryService · public: getClosestWeatherData, getTodayWeatherData · 주입: PartnerWeatherDataRepository

### service (1)

- `PartnerOpenMeteoWeatherApiClient` api/impl/PartnerOpenMeteoWeatherApiClient.java · extends/implements PartnerWeatherApiClient · public: getWeatherData · 주입: RestTemplate

### repository (1)

- `PartnerWeatherDataRepository` (interface) repository/PartnerWeatherDataRepository.java · extends/implements JpaRepository · public: findClosestToTimeWithinRange, findWithinTimeRange, findByTimestamp, findByTimestampBetween, existsByTimestamp

### config (2)

- `PartnerWeatherDataInitializer` config/PartnerWeatherDataInitializer.java · public: initializeWeatherData · 주입: PartnerWeatherDataCommandService
- `PartnerWeatherDataScheduler` config/PartnerWeatherDataScheduler.java · public: scheduleWeatherDataFetch · 주입: PartnerWeatherDataCommandService

### other (1)

- `PartnerWeatherApiClient` (interface) api/PartnerWeatherApiClient.java · public: getWeatherData

### 다른 도메인 참조 (import·주입 기준)

- **weather**: `WeatherData` ← PartnerWeatherDataCommandServiceImpl, PartnerWeatherDataRepository, PartnerWeatherQueryServiceImpl

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

- 구독 event `ApplicationReadyEvent` @EventListener: `PartnerWeatherDataInitializer#initializeWeatherData` ← (소스 내 발행 없음)

## 6. AOP

(없음)

