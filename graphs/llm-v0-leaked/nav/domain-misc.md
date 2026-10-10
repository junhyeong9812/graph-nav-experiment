# domain-misc — 소규모 도메인 (accessLog, monitoring, notification, address, weather)

> 경로 기준: `src/main/java/com/fream/back/domain/`. 테스트 기준: `src/test/java/com/fream/back/domain/`.
> 컨트롤러 경로는 애플리케이션 기준. prod nginx는 `/api/` 접두사를 제거해서 프록시하므로 외부 URL은 `/api` + 아래 경로.

## 자주 할 작업 → 여기를 보라

| 작업 | 볼 곳 |
|---|---|
| 다른 도메인에서 사용자에게 알림 보내기 | `notification/service/command/NotificationCommandService.createNotification(userId, category, type, message)` |
| 알림 유형 추가 | `notification/entity/NotificationType` (enum이 `NotificationCategory`를 가짐) |
| 접속 로그를 Kafka/DB 직접저장 전환 | `access-log.use-kafka` (prod yml은 `false`) → `accessLog/service/command/UserAccessLogCommandService` |
| GeoIP DB 교체 | `src/main/resources/GeoLite2-City.mmdb` (`GeoIPService` 생성자에서 classpath 로드) |
| 주소 암호화 방식 변경 | `address/service/{command,query}/*` + `global/utils/PersonalDataEncryptionUtil` (특성화 테스트 먼저 확인) |
| 날씨 좌표/주기 변경 | `application-prod.yml`의 `weather.*` |
| 모니터링 대시보드 URL | `kibana.url` 프로퍼티, `templates/admin/monitoring.html` |

---

## 1. accessLog

**역할**: 프론트가 보낸 접속 로그를 받아 IP→위치(GeoIP) 보강 후 `user_access_log` 테이블에 저장. Kafka 경유(옵션)와 자체 AOP(예외/로깅/성능) 포함.

### 파일 맵
| 영역 | 파일 |
|---|---|
| 컨트롤러 | `accessLog/controller/command/UserAccessLogCommandController`, `accessLog/controller/query/UserAccessLogQueryController` |
| 서비스 | `service/command/UserAccessLogCommandService`, `service/query/UserAccessLogQueryService`, `service/geo/GeoIPService`, `service/kafka/UserAccessLogProducer`, `service/kafka/UserAccessLogConsumer` |
| `_backup` 파일 | `UserAccessLogCommandService_backup`(전체 주석), `GeoIPService_backup`·`UserAccessLogProducer_backup`·`UserAccessLogConsumer_backup`·`UserAccessLogQueryService_backup`(**활성 `@Service`**) |
| AOP | `aop/annotation/{AccessLogExceptionHandler, AccessLogMethodLogger, AccessLogPerformanceMonitor}`, `aop/aspect/{AccessLogExceptionAspect(@Order 1), AccessLogMethodLoggingAspect(@Order 2), AccessLogPerformanceAspect(@Order 3)}` — `@Around("@annotation(...)")` |
| 설정 | `config/AccessLogAopConfiguration` (`@EnableAspectJAutoProxy(proxyTargetClass, exposeProxy)`, `accessLogCacheManager`, 10분 주기 통계 로그), Kafka 설정은 `global/config/kafka/UserAccessLogKafkaConfig` |
| 엔티티/리포 | `entity/UserAccessLog`, `repository/UserAccessLogRepository` + `UserAccessLogRepositoryCustom/Impl`(QueryDSL: `countTodayAccesses`, `countTodayUniqueVisitors`, `findRecent7DaysAccessCount`) |
| DTO | `dto/UserAccessLogDto`, `UserAccessLogEvent`, `DailyAccessCountDto` |
| 예외 | `exception/AccessLogErrorCode`(AL*), `AccessLogException`, `AccessLogKafkaException`, `AccessLogQueryException`, `AccessLogSaveException`, `GeoIPException`, `InvalidParameterException` |

### REST
| Method | Path | Controller → Service |
|---|---|---|
| POST | `/access-log/commands/create` | `UserAccessLogCommandController.createAccessLog` → `UserAccessLogCommandService.createAccessLog` (204) |
| GET | `/access-log/queries/today` | `UserAccessLogQueryController.getTodayAccessCount` → `getTodayUniqueVisitorCount()` (원래 `getTodayAccessCount` 호출은 주석) |
| GET | `/access-log/queries/week` | → `getRecent7DaysAccessCount()` |

### 엔티티
`UserAccessLog`(테이블 `user_access_log`, `@SequenceGenerator USER_ACCESS_LOG_SEQ`, 인덱스 `idx_access_time`, `idx_ip_access_time`): refererUrl, userAgent, os, browser, deviceType, accessTime(기본 now), ipAddress, country, region, city, pageUrl, email, isAnonymous(기본 true), networkType, browserLanguage, screenWidth/Height, devicePixelRatio. BaseTimeEntity 미상속.

### 핵심 로직
1. 컨트롤러가 IP(`X-Forwarded-For` → `Proxy-Client-IP` → ... 순), `User-Agent`, `Referer` 채움. email 없으면 `"Anonymous"` + `anonymous=true`.
2. 서비스: IP 필수 검증, pageUrl 없으면 `"Unknown"`. `useKafka`면 Producer로 `user-access-log-topic` 전송, 아니면 GeoIP 보강 후 즉시 save.
3. Consumer: GeoIP 조회 → save. 예외는 로깅만(재throw 안 함).

### 의존 / 테스트
- 외부 도메인에서 호출하는 곳 없음(자체 REST 진입). 
- 테스트: **없음**.

### gotcha
- `validateLogDto`, `enrichWithGeoData`, `mapToEntity` 등 **private 메서드**에 AOP 애노테이션이 붙어 있으나 Spring 프록시 AOP는 private/자기호출을 가로채지 않음 → 해당 애노테이션은 무효. public 진입 메서드에만 적용됨.
- `UserAccessLogConsumer_backup`도 같은 토픽/그룹을 구독하는 활성 빈.
- `accessLogCacheManager`의 캐시들은 어디서도 `@Cacheable`로 사용되지 않음.

---

## 2. monitoring

**역할**: 관리자용 Kibana(Metricbeat) 대시보드를 iframe으로 띄우는 단일 페이지.

| 파일 | 내용 |
|---|---|
| `monitoring/controller/AdminMonitoringController` | `@Controller`, `GET /admin/monitoring` → 뷰 `"admin/monitoring"`, model `kibanaUrl` (`${kibana.url:https://www.pinjun.xyz/kibana}`) |
| `src/main/resources/templates/admin/monitoring.html` | Thymeleaf 문법 `th:src="${kibanaUrl} + '/app/dashboards#/view/Metricbeat-system-overview-ecs'"` (Grafana 버전은 HTML 주석) |
| `monitoring/service/MonitoringService` | **전체 주석 처리** |
| `monitoring/dto/{MetricsDTO, RequestStatsDTO, SystemInfoDTO}` | 전체 주석 처리 |

gotcha: `build.gradle`에 Thymeleaf 의존성이 **없음** → 템플릿 렌더링이 실제 동작하는지 (미확인). 엔티티·테스트 없음. 접근 권한은 security 설정 문서 참조.

---

## 3. notification

**역할**: 알림 엔티티 저장 + WebSocket(STOMP) 실시간 push + 조회/읽음/삭제 REST. 다른 도메인들이 알림 생성 시 호출하는 공용 서비스.

### 파일 맵
| 영역 | 파일 |
|---|---|
| 컨트롤러 | `notification/controller/command/NotificationCommandController`(REST + `@MessageMapping("/ping")`), `controller/query/NotificationQueryController` |
| 서비스 | `service/command/NotificationCommandService`, `service/query/NotificationQueryService` |
| 엔티티 | `entity/Notification`, `entity/NotificationCategory`(SHOPPING, STYLE), `entity/NotificationType`(TRADE, BID, STORAGE, FAVORITE, BENEFIT, ANNOUNCEMENT, REFUND → SHOPPING / LIKE, COMMENT, FOLLOW → STYLE; `getTypesByCategory`) |
| 리포 | `repository/NotificationRepository`(JpaRepository + Custom), `NotificationRepositoryCustom/CustomImpl`(QueryDSL) |
| DTO | `dto/NotificationDTO`, `dto/NotificationRequestDTO` |
| 예외 | `exception/NotificationErrorCode`(N*), `NotificationException` |

### REST / STOMP
모든 사용자 식별은 SecurityContext principal(String 이메일).

| Method | Path | Controller → Service |
|---|---|---|
| POST | `/notifications?userId=` (body `NotificationRequestDTO`) | `createNotification` → `NotificationCommandService.createNotification` (201, 관리자 체크 미구현 주석) |
| POST | `/notifications/broadcast` | → `createNotificationForAll` (전체 사용자, 관리자 체크 미구현) |
| PATCH | `/notifications/{id}/read` | → `markAsRead(id, email)` (소유자 아니면 `NOTIFICATION_ACCESS_DENIED`) |
| PATCH | `/notifications/read-all` | → `markAllAsRead(email)` |
| DELETE | `/notifications/{id}` | → `deleteNotification(id)` (**소유자 검증 없음**) |
| DELETE | `/notifications/user` | → `deleteNotificationsByUser(email)` |
| GET | `/notifications/filter/category?category=` | `NotificationQueryController` → `NotificationQueryService.filterByCategory` |
| GET | `/notifications/filter/type?type=` | → `filterByType` |
| GET | `/notifications/filter/category/read-status?category&isRead&page=0&size=10&sort=createdDate,desc` | → `filterByCategoryAndIsRead` (page는 0-based로 직접 `PageRequest` 생성) |
| GET | `/notifications/filter/type/read-status?type&isRead&page&size&sort` | → `filterByTypeAndIsRead` |
| GET | `/notifications/count/unread` | → `countUnreadNotifications` (응답 Map) |
| STOMP SEND | `/app/ping` | `handlePing` → `updateWebSocketConnectionTTL(email)` |
| STOMP 구독 | `/user/queue/notifications` | 서버가 `convertAndSendToUser(email, "/queue/notifications", dto)` |

### 엔티티
`Notification extends BaseTimeEntity`: id(IDENTITY), category(STRING), type(STRING), message, isRead(기본 false), user(`@ManyToOne LAZY`, `user_id` not null). `markAsRead()`, `markAsUnread()`.

### 핵심 로직
- `createNotification`: user 조회 → 저장 → Redis 키 `WebSocket:User:{email}` 존재 시에만 push (push 실패는 무시). 기타 예외는 `NOTIFICATION_CREATION_FAILED`로 래핑.
- `createNotificationForAll`: 전체 사용자 `saveAll` 후 접속(Redis 키 존재) 사용자에게만 리스트 push.
- `notifyShipmentCompleted(Order)`: 구매자에게 SHOPPING/BID 알림, 예외는 로깅만.
- `updateWebSocketConnectionTTL`: 남은 TTL ≤600초일 때만 30분으로 연장.
- `toDTO`는 `getCreatedDate().toString()` 호출 → createdDate null이면 NPE → 생성 실패로 래핑(테스트로 고정됨).

### 다른 도메인 → notification 호출처
| 호출 클래스 | 메서드 |
|---|---|
| `notice/service/command/NoticeCommandService` | `createNotificationForAll` |
| `user/service/follow/FollowCommandService` | `createNotification` |
| `user/service/command/UserCommandService` | `deleteNotificationsByUser` (회원 탈퇴 시) |
| `payment/service/command/PaymentCommandService` | `createNotification` (여러 곳) |
| `shipment/service/command/OrderShipmentCommandService` | `createNotification` (여러 곳) |
| `shipment/config/ShipmentItemProcessor`, `shipment/config/UpdateShipmentStatusesJobConfig` | `notifyShipmentCompleted` |
| `sale/service/command/SaleCommandService` | `createNotification` |
| `global/config/DataInitializer` | `NotificationRepository` 직접 사용(시드) |

별도로 `order/service/kafka/OrderEventConsumer`는 notification 서비스를 거치지 않고 `SimpMessagingTemplate`로 `/user/queue/notifications`, `/topic/order/{orderId}`에 직접 push하고 `notification-topic`에 발행.

### 테스트
| 파일 | 종류 |
|---|---|
| `notification/entity/NotificationEntityTest` | 순수 단위 |
| `notification/repository/NotificationRepositoryTest` | `@DataJpaTest` + test 프로필(H2) |
| `notification/service/NotificationCommandServiceTest`, `NotificationQueryServiceTest` | Mockito |

### gotcha
- `countUnreadNotifications`는 `Pageable.unpaged()`를 QueryDSL 커스텀 구현에 넘기며, 실제 리포지토리에서는 `UnsupportedOperationException` → `NOTIFICATION_RETRIEVAL_FAILED`가 발생. `NotificationRepositoryTest.countUnreadWithRealRepositoryFailsOnUnpagedPageable`가 **현재 동작을 고정**함(고치면 테스트도 수정 필요).
- 실시간 push는 Redis 키 존재에 의존하는데, 키를 세팅하는 핸드셰이크 경로에 문제가 있을 수 있음 (`global-infra.md` gotcha 4).

---

## 4. address

**역할**: 사용자 배송지 CRUD. 필드 단위 개인정보 암호화.

### 파일 맵
| 영역 | 파일 |
|---|---|
| 컨트롤러 | `address/controller/command/AddressCommandController`, `controller/query/AddressQueryController` (이메일은 `SecurityUtils.extractEmailFromSecurityContext()`) |
| 서비스 | `service/command/AddressCommandService`, `service/query/AddressQueryService` |
| 엔티티/리포 | `entity/Address`, `repository/AddressRepository` (`findByIdAndUser`, 암호문 비교 `@Query`: recipientName/phoneNumber/address/zipCode) |
| DTO | `dto/AddressCreateDto`, `AddressUpdateDto`, `AddressResponseDto`, `AddressListResponseDto` |
| AOP | `aop/{AddressAuditAspect, AddressExceptionAspect, AddressLoggingAspect, AddressPerformanceAspect, AddressSecurityAspect}` + `aop/annotation/*` |
| 예외 | `exception/AddressErrorCode`(ADR*), `AddressException`, `AddressNotFoundException`, `AddressAccessDeniedException`, `AddressUserNotFoundException` |

### REST
| Method | Path | Controller → Service |
|---|---|---|
| POST | `/addresses` | `AddressCommandController.createAddress` → `AddressCommandService.createAddress(email, dto)` |
| PUT | `/addresses` (body에 `addressId`) | → `updateAddress(email, dto)` |
| DELETE | `/addresses/{addressId}` | → `deleteAddress(email, id)` |
| GET | `/addresses` | `AddressQueryController.getAddresses` → `AddressQueryService.getAddresses(email)` |
| GET | `/addresses/{addressId}` | → `getAddress(email, id)` |

(`searchAddressesByRecipientName`, `searchAddressesByPhoneNumber`는 서비스에만 있고 REST 노출 없음.)

### 엔티티
`Address extends BaseTimeEntity`: id, user(ManyToOne), recipientName, phoneNumber, zipCode, address(이상 not null), detailedAddress, isDefault(`is_default`). `updateAddress(...)`는 null 인자는 기존 값 유지, `assignUser/unassignUser`.

### 핵심 로직 (암호화)
- 저장: recipientName/phoneNumber/zipCode/address → `deterministicEncrypt`(AES/ECB), detailedAddress → `encrypt`(AES/CBC).
- 조회: 앞 4개 필드 `deterministicDecrypt`, detailedAddress `decrypt`.
- 기본 배송지 지정 시 해당 user의 다른 주소들 `isDefault=false`.
- AOP: 애노테이션 기반 pointcut(`@annotation(...)`)용 애노테이션은 **어떤 메서드에도 부착되어 있지 않음**. 실제 동작하는 것은 `execution(* com.fream.back.domain.address.{controller,service,repository}..*(..))` 기반 로깅/성능/예외/감사 advice.

### 의존
- `order/service/command/OrderCommandService` → `addressQueryService.getAddress(buyer.getEmail(), addressId)` (주문 시 배송지 조회).
- `global/config/DataInitializer` → `AddressRepository` 직접 사용.

### 테스트
| 파일 | 종류 |
|---|---|
| `address/entity/AddressTest` | 단위 |
| `address/repository/AddressRepositoryTest` | `@DataJpaTest` (H2) |
| `address/service/command/AddressCommandServiceTest` | Mockito |
| `address/service/query/AddressQueryServiceTest` | Mockito |
| `address/service/query/AddressEncryptionRoundTripTest` | 실제 `PersonalDataEncryptionUtil` 사용 특성화 테스트 |

### gotcha (테스트로 고정된 현재 동작)
- **사용자 조회 키 불일치**: Command 서비스는 `userRepository.findByEmail(email)`(평문), Query 서비스는 `findByEmail(encryptionUtil.deterministicEncrypt(email))`(암호문). 테스트명 `createAddress_looksUpUserByPlainEmail_...`, `getAddresses_findsUserByEncryptedEmail_...`.
- `deterministicDecrypt`가 `deterministicEncrypt` 결과를 복원하지 못함 → 실제 암호화된 행으로 `getAddresses` 호출 시 `ADDRESS_QUERY_ERROR` (`AddressEncryptionRoundTripTest`). 이 영역을 고치면 위 특성화 테스트들도 함께 갱신해야 함.

---

## 5. weather

**역할**: Open-Meteo API에서 시간별 날씨를 가져와 `WeatherData`에 timestamp 기준 upsert, 현재/오늘 날씨 조회 제공.

### 파일 맵
| 영역 | 파일 |
|---|---|
| API 클라이언트 | `weather/api/WeatherApiClient`(interface), `api/impl/OpenMeteoWeatherApiClient`(`RestTemplate`, `weather.api.url`, `weather.api.params.{latitude,longitude,hourly,timezone}` — 기본값 없음) |
| 기동/스케줄 | `config/WeatherDataInitializer`(`@EventListener(ApplicationReadyEvent)` 1회 실행, 실패해도 로깅만), `config/WeatherDataScheduler`(fixedRate `weather.scheduler.interval`, 기본 86400000ms / initialDelay 10000ms) |
| 서비스 | `service/command/WeatherDataCommandService`(+Impl: `fetchAndStore24HourWeatherData`), `service/query/WeatherQueryService`(+Impl) |
| 컨트롤러 | `controller/WeatherQueryController` |
| 엔티티/리포 | `entity/WeatherData`, `repository/WeatherDataRepository` |
| DTO | `dto/WeatherApiResponse`, `WeatherDataDto`, `commonDto` |
| 예외 | `exception/WeatherErrorCode`(WE*), `WeatherException`(정적 팩토리 `apiError`, `apiParsingError`, `dataSaveError` 등), `WeatherApiException`, `WeatherDataException`, `WeatherParameterException` |

### REST
| Method | Path | Controller → Service |
|---|---|---|
| GET | `/weather/query/current` | `getClosestWeatherData` → `WeatherQueryService.getClosestWeatherData()` (없으면 204) |
| GET | `/weather/query/today` | `getTodayWeatherData` → `getTodayWeatherData()` (비면 204) |

### 엔티티
`WeatherData`: id, timestamp(unique, not null), temperature, precipitationProbability, precipitation, rain, snowfall, retrievedAt(`retrieved_at`). `updateWeatherData(...)`.

### 핵심 로직
- 수집: API 응답의 앞 `min(weather.data.hours-count(48), 응답 길이)`개 시간 파싱 → `[start,end]` 범위 기존 행을 Map으로 → 있으면 update, 없으면 insert → `saveAll`.
- 현재: now±1시간 범위(`findWithinTimeRange`)에서 가장 가까운 것. 오늘: 당일 00:00~23:59:59 (`findByTimestampBetween`).
- `findClosestToTimeWithinRange`는 JPQL에 `TIMESTAMPDIFF` 사용(MySQL 함수).

### 의존 / 테스트
- 다른 도메인에서 호출 없음. 테스트 **없음**.
- `weather.*` 프로퍼티는 `application-prod.yml`에만 존재 → 다른 프로필로 전체 컨텍스트를 띄우면 `weather.api.url` 등 placeholder 해석 실패 가능.
