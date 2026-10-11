# inspection 도메인

기준 경로: `src/main/java/com/fream/back/domain/inspection/` · 파일 19개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| GET | `/weather/query/current` | `InspectionQueryController#getClosestWeatherData` | `InspectionQueryService#getClosestWeatherData` |
| GET | `/weather/query/today` | `InspectionQueryController#getTodayWeatherData` | `InspectionQueryService#getTodayWeatherData` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `InspectionDataCommandServiceImpl`
  - `fetchAndStore24HourWeatherData` → InspectionApiClient#getWeatherData, this#parseTimestamps, InspectionDataRepository#findByTimestampBetween, this#saveOrUpdateWeatherData, InspectionDataRepository#saveAll~
- `InspectionDataInitializer`
  - `initializeWeatherData` → InspectionDataCommandService#fetchAndStore24HourWeatherData
- `InspectionDataScheduler`
  - `scheduleWeatherDataFetch` → InspectionDataCommandService#fetchAndStore24HourWeatherData
- `InspectionQueryServiceImpl`
  - `getClosestWeatherData` → InspectionDataRepository#findWithinTimeRange
  - `getTodayWeatherData` → InspectionDataRepository#findByTimestampBetween

## 3. 클래스 목록 (역할별)

### controller (1)

- `InspectionQueryController` controller/InspectionQueryController.java · public: getClosestWeatherData, getTodayWeatherData · 주입: InspectionQueryService

### service-command (2)

- `InspectionDataCommandService` (interface) service/command/InspectionDataCommandService.java · public: fetchAndStore24HourWeatherData
- `InspectionDataCommandServiceImpl` service/command/InspectionDataCommandServiceImpl.java · extends/implements InspectionDataCommandService · public: fetchAndStore24HourWeatherData · 주입: InspectionDataRepository, InspectionApiClient

### service-query (2)

- `InspectionQueryService` (interface) service/query/InspectionQueryService.java · public: getClosestWeatherData, getTodayWeatherData
- `InspectionQueryServiceImpl` service/query/InspectionQueryServiceImpl.java · extends/implements InspectionQueryService · public: getClosestWeatherData, getTodayWeatherData · 주입: InspectionDataRepository

### service (1)

- `OpenMeteoInspectionApiClient` api/impl/OpenMeteoInspectionApiClient.java · extends/implements InspectionApiClient · public: getWeatherData · 주입: RestTemplate

### repository (1)

- `InspectionDataRepository` (interface) repository/InspectionDataRepository.java · extends/implements JpaRepository · public: findClosestToTimeWithinRange, findWithinTimeRange, findByTimestamp, findByTimestampBetween, existsByTimestamp

### entity (1)

- `InspectionData` entity/InspectionData.java · public: updateWeatherData

### config (2)

- `InspectionDataInitializer` config/InspectionDataInitializer.java · public: initializeWeatherData · 주입: InspectionDataCommandService
- `InspectionDataScheduler` config/InspectionDataScheduler.java · public: scheduleWeatherDataFetch · 주입: InspectionDataCommandService

### dto (3)

- `InspectionApiResponse` dto/InspectionApiResponse.java
- `InspectionDataDto` dto/InspectionDataDto.java
- `commonDto` dto/commonDto.java

### exception (5)

- `InspectionApiException` exception/InspectionApiException.java
- `InspectionDataException` exception/InspectionDataException.java
- `InspectionErrorCode` exception/InspectionErrorCode.java
- `InspectionException` exception/InspectionException.java
- `InspectionParameterException` exception/InspectionParameterException.java

### other (1)

- `InspectionApiClient` (interface) api/InspectionApiClient.java · public: getWeatherData

### 다른 도메인 참조 (import·주입 기준)

(없음)

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

- `InspectionData` @Entity @Table(name = "weather_data", indexes = { @Index(name = "idx_weather_data_timestamp", columnList = "timestamp", unique = true) })

## 5. 이벤트·메시징

- 구독 event `ApplicationReadyEvent` @EventListener: `InspectionDataInitializer#initializeWeatherData` ← (소스 내 발행 없음)

## 6. AOP

(없음)

