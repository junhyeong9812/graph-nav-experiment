# monitoring 도메인 (관리자 모니터링 페이지)

## 1. 개요
- 관리자용 서버 모니터링 페이지 1개. 자체 메트릭 수집은 없고 Kibana 대시보드(Metricbeat system overview)를 iframe으로 임베드하는 뷰를 반환.
- 서비스/DTO는 전부 주석 처리된 레거시 코드.

## 2. 패키지/파일 구조
| 경로 | 상태 |
|---|---|
| `src/main/java/com/fream/back/domain/monitoring/controller/AdminMonitoringController.java` | 활성 (`@Controller`) |
| `src/main/java/com/fream/back/domain/monitoring/service/MonitoringService.java` | 전체 주석 처리 (`getBasicMetrics`, `getSystemInfo`, `getRequestStats` 흔적) |
| `src/main/java/com/fream/back/domain/monitoring/dto/MetricsDTO.java`, `RequestStatsDTO.java`, `SystemInfoDTO.java` | 전체 주석 처리 |
| `src/main/resources/templates/admin/monitoring.html` | Thymeleaf 문법 템플릿 (Kibana iframe, 하단에 Grafana 버전 주석) |

## 3. 엔티티
없음.

## 4. API 엔드포인트
| Method | Path | Controller#method | 동작 | 권한 |
|---|---|---|---|---|
| GET | `/admin/monitoring` | `AdminMonitoringController#monitoringDashboard` | model `kibanaUrl` 추가 후 뷰 이름 `"admin/monitoring"` 반환 | SecurityConfig상 인증만 필요(`/admin/auth/**`만 whitelist). 관리자 역할 체크 코드 없음 |

## 5~6. 서비스 / Repository
- 활성 서비스·리포지토리 없음.

## 7. 외부 연동
- 설정 `kibana.url` (`@Value("${kibana.url:https://www.pinjun.xyz/kibana}")`, `application-prod.yml` L134-135 동일 값).
- 템플릿 iframe src: `${kibanaUrl} + '/app/dashboards#/view/Metricbeat-system-overview-ecs'`.
- Kafka/스케줄러/AOP 없음.

## 8. 다른 도메인과의 의존
- 없음 (다른 도메인에서 import하지 않음).

## 9. 예외
- 도메인 전용 ErrorCode 없음.

## 10. 작업 팁 / 주의점
- `build.gradle`에 `spring-boot-starter-thymeleaf` 의존성이 없음 → `@Controller`가 반환하는 뷰 이름 `admin/monitoring`을 렌더링할 템플릿 엔진이 등록되어 있지 않음. 템플릿을 실제로 쓰려면 의존성 확인 필요.
- 관리자 권한 검증이 없으므로 로그인한 일반 사용자도 접근 가능 (코드 기준).
- 액추에이터: `application.yml`에서 `management.endpoints.web.exposure.include: "*"`, SecurityConfig whitelist에 `/actuator/**` 포함 (monitoring 도메인과 직접 연결 코드는 없음).
