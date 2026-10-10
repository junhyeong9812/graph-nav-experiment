# monitoring 도메인

기준 경로: `src/main/java/com/fream/back/domain/monitoring/` · 파일 5개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

타입 선언이 없는 파일(파싱 결과 클래스 0개): `src/main/java/com/fream/back/domain/monitoring/dto/MetricsDTO.java`, `src/main/java/com/fream/back/domain/monitoring/dto/RequestStatsDTO.java`, `src/main/java/com/fream/back/domain/monitoring/dto/SystemInfoDTO.java`, `src/main/java/com/fream/back/domain/monitoring/service/MonitoringService.java`

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| GET | `/admin/monitoring` | `AdminMonitoringController#monitoringDashboard` |  |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

(없음)

## 3. 클래스 목록 (역할별)

### controller (1)

- `AdminMonitoringController` controller/AdminMonitoringController.java · public: monitoringDashboard

### 다른 도메인 참조 (import·주입 기준)

(없음)

### 이 도메인을 참조하는 다른 도메인

(없음)

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

