# notification 도메인 네비게이션

## 1. 개요
- 사용자 대상 알림(쇼핑/스타일)을 DB(`Notification`)에 저장하고, WebSocket(STOMP, `/user/{email}/queue/notifications`)으로 실시간 푸시.
- 실시간 푸시는 Redis 키 `WebSocket:User:{email}` 존재 시에만 시도(연결된 사용자 판단). 푸시 실패해도 저장은 유지.
- 알림 생성은 주로 **다른 도메인 서비스가 `NotificationCommandService`를 직접 호출**하는 방식(Kafka 경유 아님). REST로도 생성 가능(관리자 체크 미구현).
- CQRS 분리: `controller/command`, `controller/query`, `service/command`, `service/query`.

## 2. 패키지/파일 구조 (14 files)
```
src/main/java/com/fream/back/domain/notification/
├── controller/command/NotificationCommandController.java   # POST/PATCH/DELETE /notifications, STOMP @MessageMapping("/ping")
├── controller/query/NotificationQueryController.java       # GET /notifications/...
├── dto/NotificationDTO.java            # id, category, type, message, isRead, createdAt(String)
├── dto/NotificationRequestDTO.java     # category(@NotNull), type(@NotNull), message(@NotBlank, 1~500자)
├── entity/Notification.java
├── entity/NotificationCategory.java
├── entity/NotificationType.java
├── exception/NotificationErrorCode.java
├── exception/NotificationException.java   # extends GlobalException
├── repository/NotificationRepository.java            # JpaRepository + NotificationRepositoryCustom
├── repository/NotificationRepositoryCustom.java
├── repository/NotificationRepositoryCustomImpl.java  # QueryDSL
├── service/command/NotificationCommandService.java   # 생성/읽음/삭제/실시간 전송/Redis TTL
└── service/query/NotificationQueryService.java       # 필터 조회, 미읽음 카운트
```
관련 글로벌 파일:
- `src/main/java/com/fream/back/global/config/websocket/WebSocketConfig.java`
- `src/main/java/com/fream/back/global/config/websocket/WebSocketAuthInterceptor.java`
- `src/main/java/com/fream/back/global/config/EmailBasedUserDestinationResolver.java`
- `src/main/java/com/fream/back/global/config/security/SecurityConfig.java` (`/ws/**` whitelist)

## 3. 엔티티
### `Notification` (extends `BaseTimeEntity` → `createdDate` 사용)
| 필드 | 타입 | 비고 |
|---|---|---|
| id | Long | IDENTITY |
| category | `NotificationCategory` | `@Enumerated(STRING)` |
| type | `NotificationType` | `@Enumerated(STRING)` |
| message | String | |
| isRead | boolean | `@Builder.Default false` |
| user | `User` (user 도메인) | `@ManyToOne(LAZY)`, `user_id` NOT NULL |
- 메서드: `markAsRead()`, `markAsUnread()`.

### Enums
- `NotificationCategory`: `SHOPPING`("쇼핑 관련 알림"), `STYLE`("스타일 관련 알림").
- `NotificationType` (category 소속 포함):
  - SHOPPING: `TRADE`, `BID`, `STORAGE`, `FAVORITE`, `BENEFIT`, `ANNOUNCEMENT`, `REFUND`
  - STYLE: `LIKE`, `COMMENT`, `FOLLOW`
  - `static getTypesByCategory(category)` (null이면 전체).
- 주의: type의 category와 엔티티 category 일치 여부를 검증하는 코드는 없음(호출자가 각각 지정).

## 4. API 엔드포인트
모든 경로는 `SecurityConfig` whitelist에 없으므로 `anyRequest().authenticated()` 적용(인증 필요). 이메일은 `SecurityContextHolder` principal(String)에서 추출 — 컨트롤러 내부 private `extractEmailFromSecurityContext()`; 없으면 `NOTIFICATION_USER_NOT_FOUND`(N011).

| Method | Path | Controller#method | Service#method | 비고 |
|---|---|---|---|---|
| POST | `/notifications?userId={id}` (body `NotificationRequestDTO`) | `NotificationCommandController#createNotification` | `NotificationCommandService#createNotification(userId, category, type, message)` | 201. 관리자 체크 "미구현" 주석(L53) |
| POST | `/notifications/broadcast` | `NotificationCommandController#createNotificationForAll` | `NotificationCommandService#createNotificationForAll(dto)` | 201. 관리자 체크 미구현(L76) |
| PATCH | `/notifications/{id}/read` | `NotificationCommandController#markAsRead` | `NotificationCommandService#markAsRead(id, email)` | 소유자 검증 있음(N012) |
| PATCH | `/notifications/read-all` | `NotificationCommandController#markAllAsRead` | `NotificationCommandService#markAllAsRead(email)` | |
| DELETE | `/notifications/{id}` | `NotificationCommandController#deleteNotification` | `NotificationCommandService#deleteNotification(id)` | 204. **소유자 검증 없음** (email 추출만, 서비스에 전달 안 함) |
| DELETE | `/notifications/user` | `NotificationCommandController#deleteAllUserNotifications` | `NotificationCommandService#deleteNotificationsByUser(email)` | 204 |
| GET | `/notifications/filter/category?category=` | `NotificationQueryController#filterByCategory` | `NotificationQueryService#filterByCategory` | List, 최신순 |
| GET | `/notifications/filter/type?type=` | `NotificationQueryController#filterByType` | `NotificationQueryService#filterByType` | List, 최신순 |
| GET | `/notifications/filter/category/read-status?category=&isRead=&page=0&size=10&sort=createdDate,desc` | `NotificationQueryController#filterByCategoryAndIsRead` | `NotificationQueryService#filterByCategoryAndIsRead` | category optional. 페이징이지만 응답은 `List`(Page 메타 없음) |
| GET | `/notifications/filter/type/read-status?type=&isRead=&page=&size=&sort=` | `NotificationQueryController#filterByTypeAndIsRead` | `NotificationQueryService#filterByTypeAndIsRead` | type optional. 응답 `List` |
| GET | `/notifications/count/unread` | `NotificationQueryController#countUnreadNotifications` | `NotificationQueryService#countUnreadNotifications` | `{"count": n}` |
| STOMP SEND | `/app/ping` | `NotificationCommandController#handlePing` (`@MessageMapping("/ping")`) | `NotificationCommandService#updateWebSocketConnectionTTL(email)` | Redis TTL 갱신 |

- 페이징 검증: `page < 0` 또는 `size <= 0 || size > 100` → `NotificationException(GlobalErrorCode.INVALID_INPUT_VALUE)` (`NotificationQueryController#validatePaginationParams`).
- sort 문자열은 `"field,dir"` 파싱 → Repository `getOrderSpecifiers`가 `createdDate|category|type|isRead`만 인식, 그 외는 createdDate DESC.

## 5. 서비스 계층
### `NotificationCommandService` (`@Transactional`)
의존: `NotificationRepository`, `UserRepository`(user 도메인), `SimpMessagingTemplate`, `RedisTemplate<String,String>`.
상수: `REDIS_KEY_PREFIX="WebSocket:User:"`, `WEBSOCKET_DESTINATION="/queue/notifications"`, `REDIS_TTL_MINUTES=30` (L37-39).
| 메서드 | 동작 |
|---|---|
| `createNotification(Long userId, category, type, message)` L44 | `userRepository.findById` → save → `sendRealTimeNotification(email, dto)` → DTO 반환. 실패 시 N011/N001 |
| `createNotificationForAll(NotificationRequestDTO)` L72 | `userRepository.findAll()` → 사용자별 Notification `saveAll` → `broadcastNotificationsToConnectedUsers` |
| `markAsRead(id, email)` L104 | 소유자 이메일 불일치 시 N012. dirty checking |
| `markAllAsRead(email)` L140 | `findAllByUserEmail` 후 루프 markAsRead |
| `deleteNotification(id)` L168 | `existsById` → `deleteById` (소유자 검증 없음) |
| `deleteNotificationsByUser(email)` L196 | `findAllByUserEmail` → `deleteAll` |
| `notifyShipmentCompleted(Order)` L219 | 구매자에게 `SHOPPING/BID` "[배송 완료] ..." 생성. 예외는 로깅만(전파 안 함) |
| `updateWebSocketConnectionTTL(email)` L328 | 남은 TTL ≤ 600초일 때만 30분으로 `expire` (키가 없으면 getExpire 결과 음수 → expire 호출되지만 키 없으므로 무효) |
| private `sendRealTimeNotification` L303 | Redis 키 존재 시 `convertAndSendToUser(email, "/queue/notifications", NotificationDTO)` |
| private `broadcastNotificationsToConnectedUsers` L269 | 연결 사용자마다 **`List<NotificationDTO>`** 를 전송 (단건 전송과 payload 형태가 다름) |

### `NotificationQueryService` (`@Transactional(readOnly = true)`)
- `filterByCategory(email, category)` — category null이면 N021.
- `filterByType(email, type)` — type null이면 N020.
- `filterByCategoryAndIsRead(email, category, isRead, pageable)`, `filterByTypeAndIsRead(...)` — Page → List 변환.
- `countUnreadNotifications(email)` — `findByUserEmailAndIsRead(email,false,Pageable.unpaged())`의 `getTotalElements()`.

## 6. Repository (QueryDSL: `NotificationRepositoryCustomImpl`)
모든 쿼리는 `notification.user` fetch join + `user.email = :email` 조건(동적 `BooleanExpression`, null이면 조건 생략).
- `findByUserEmailAndCategory(email, category)` — createdDate DESC
- `findByUserEmailAndType(email, type)` — createdDate DESC
- `findByUserEmailAndIsRead(email, isRead, pageable)` — Page, count 쿼리 별도
- `findByUserEmailAndCategoryAndIsRead(email, category, isRead, pageable)` — category null이면 필터 생략
- `findByUserEmailAndTypeAndIsRead(email, type, isRead, pageable)` — type null이면 필터 생략
- `findAllByUserEmail(email)` — createdDate DESC
- `@Query` 없음.

## 7. 외부 연동 / WebSocket
### STOMP 설정 (`global/config/websocket/WebSocketConfig.java`)
- `@EnableWebSocketMessageBroker`, SimpleBroker: `/topic`, `/queue` (L28)
- Application prefix: `/app` (L29), User prefix: `/user` (L30)
- Endpoint: `/ws`, `setAllowedOrigins("https://www.pinjun.xyz")`, `.withSockJS()`, HandshakeInterceptor = `new WebSocketAuthInterceptor(...)` (L35-38)
- `SecurityConfig` whitelist에 `/ws/**` 포함(HTTP 레벨 permitAll) → 인증은 인터셉터가 담당.

### 인증 (`WebSocketAuthInterceptor`, HandshakeInterceptor)
- `beforeHandshake` (L51): 쿠키 `ACCESS_TOKEN` 추출 → `jwtTokenProvider.validateToken` → `getEmailFromToken` → `userRepository.findByEmail` 존재 확인 → handshake `attributes.put("email", email)`. 어떤 예외든 `false` 반환(핸드셰이크 거부).
- `afterHandshake` (L111): **서블릿 request attribute `"attributes"`** 에서 Map을 읽어 `email`이 있으면 Redis `WebSocket:User:{email}` = `"CONNECTED"`, TTL 30분 설정(TTL null 또는 ≤600초일 때). beforeHandshake가 쓰는 handshake attributes 맵과는 다른 저장소이며, 코드베이스에 `setAttribute("attributes", ...)` 하는 곳은 grep상 없음 → 이 경로에서 Redis 키가 설정되지 않을 수 있음(실시간 전송 조건과 직결, 작업 시 확인 필요).
- 커스텀 `HandshakeHandler` / `ChannelInterceptor` 없음 → STOMP 세션 Principal은 Spring 기본 동작(HTTP 요청 Principal)에 의존. `convertAndSendToUser(email, ...)`는 그 Principal 이름이 email일 때 전달됨.

### 목적지(Destination)
| 목적지 | 송신자 | payload |
|---|---|---|
| `/user/{email}/queue/notifications` (`convertAndSendToUser(email, "/queue/notifications", ...)`) | `NotificationCommandService#sendRealTimeNotification` | `NotificationDTO` |
| 동일 | `NotificationCommandService#broadcastNotificationsToConnectedUsers` | `List<NotificationDTO>` |
| 동일 | `domain/order/service/kafka/OrderEventConsumer.java` L300, L347 (`sendSuccessNotification`, `sendFailureNotification`) | `Map` (orderId, status, type=ORDER_COMPLETED/ORDER_FAILED, title, message, timestamp...) — **DB 저장 없음**, Redis 연결 체크 없음 |
| `/topic/order/{orderId}` | `OrderEventConsumer` L304, L351 | 위와 동일 Map |
| `/app/ping` (client→server) | 클라이언트 | TTL 갱신 |

- `EmailBasedUserDestinationResolver#resolveUserDestination(dest)` → `"/user/" + email + dest` 반환. **코드베이스 어디에서도 사용되지 않음**(grep 기준).
- Kafka: notification 도메인 자체에는 Kafka producer/consumer 없음. 참고로 `OrderEventConsumer`가 `notification-topic`으로 발행(L318, L364)하지만 이를 소비하는 notification 코드는 없음.
- 스케줄러 없음.

### Redis 키
- `WebSocket:User:{email}` — 값 `"CONNECTED"`, TTL 30분. 설정: `WebSocketAuthInterceptor#afterHandshake`; 갱신: `NotificationCommandService#updateWebSocketConnectionTTL`; 조회: `sendRealTimeNotification`, `broadcastNotificationsToConnectedUsers` (`hasKey`).

## 알림 발송 흐름
### (1) 알림 생성 호출 지점 (전체 목록)
| 호출 위치 | 메서드 | 호출 | category/type | 메시지 |
|---|---|---|---|---|
| `src/main/java/com/fream/back/domain/user/service/follow/FollowCommandService.java` L64 | `createFollow` | `createNotification` | STYLE / FOLLOW | |
| `src/main/java/com/fream/back/domain/payment/service/command/PaymentCommandService.java` L438 | `sendNotifications(Order, Sale)` | `createNotification` | SHOPPING / BID | "결제가 완료되었습니다. 주문 ID: ..." |
| 同 L449 | `sendNotifications` | `createNotification` | SHOPPING / BID | "구매자가 결제를 완료하였습니다. 판매 ID: ..." |
| 同 L614 | `sendRefundNotification(Payment)` | `createNotification` | SHOPPING / REFUND | "결제가 환불되었습니다. ..." |
| 同 L641 | `sendRefundNotification` | `createNotification` | SHOPPING / REFUND | "구매자의 결제가 환불되었습니다. 판매 ID: ..." |
| `src/main/java/com/fream/back/domain/sale/service/command/SaleCommandService.java` L84 | `createInstantSale` | `createNotification` | SHOPPING / BID | "구매 입찰에 판매자가 등록되었습니다. 확인해보세요!" |
| 同 L125 | `createSale` | `createNotification` | SHOPPING / BID | "판매 입찰이 등록되었습니다." |
| `src/main/java/com/fream/back/domain/shipment/service/command/OrderShipmentCommandService.java` L133, L164 | `updateSaleStatusToSold` | `createNotification` | SHOPPING / BID | "상품이 성공적으로 판매 완료되었습니다. 판매 ID: ..." |
| 同 L212 | `completeOrder` | `createNotification` | SHOPPING / BID | "[completeOrder] 주문이 완료 처리되었습니다. ..." |
| 同 L266 | `updateTrackingInfo` | `createNotification` | SHOPPING / BID | "상품이 출발하였습니다. ..." |
| 同 L359 | `updateShipmentStatuses` | `createNotification` | SHOPPING / BID | "상품이 배송 완료되었습니다. ..." |
| `src/main/java/com/fream/back/domain/shipment/config/ShipmentItemProcessor.java` L117 | (Batch processor) | `notifyShipmentCompleted(order)` | SHOPPING / BID | "[배송 완료] ..." |
| `src/main/java/com/fream/back/domain/shipment/config/UpdateShipmentStatusesJobConfig.java` L147 | (Batch job) | `notifyShipmentCompleted(order)` | SHOPPING / BID | "[배송 완료] ..." |
| `src/main/java/com/fream/back/domain/notice/service/command/NoticeCommandService.java` L495 | `sendNotificationToAllUsers(Notice)` (L79에서 호출) | `createNotificationForAll` | SHOPPING / ANNOUNCEMENT | "새로운 공지사항: {title}". 실패 시 `NoticeException(NOTICE_NOTIFICATION_ERROR)` 던짐 |
| REST `POST /notifications`, `/notifications/broadcast` | `NotificationCommandController` | 각각 | 요청값 | |

- 기타 의존: `src/main/java/com/fream/back/domain/user/service/command/UserCommandService.java` L177 `deleteAccount` → `deleteNotificationsByUser(email)`.
- `NotificationType.LIKE`, `COMMENT` 를 사용하는 코드는 없음(style 도메인은 알림을 생성하지 않음). `FOLLOW`는 FollowCommandService에서만 사용.

### (2) 저장 + 푸시
1. `NotificationCommandService#createNotification` → `UserRepository.findById` → `Notification.builder()...isRead(false)` → `notificationRepository.save`.
2. `sendRealTimeNotification(email, dto)`: `redisTemplate.hasKey("WebSocket:User:"+email)` true일 때만 `messagingTemplate.convertAndSendToUser(email, "/queue/notifications", dto)`. 전송 예외는 warn 로그만.
3. 전체 발송(`createNotificationForAll`): `saveAll` 후 사용자별로 Redis 키 확인 → 해당 사용자 알림 리스트(List)를 전송.
4. 호출자 트랜잭션 안에서 실행됨(`@Transactional` 전파). `createNotification`은 실패 시 `NotificationException`을 던지므로 호출자에서 try/catch 여부에 따라 비즈니스 롤백 가능 — `notifyShipmentCompleted`만 내부에서 예외를 삼킴.

### (3) 클라이언트 수신/조회/읽음
1. SockJS `/ws` 연결(쿠키 `ACCESS_TOKEN` 필수) → `/user/queue/notifications` 구독.
2. 주기적으로 `/app/ping` SEND → Redis TTL 연장.
3. REST 조회: `GET /notifications/filter/...`, 배지: `GET /notifications/count/unread`.
4. 읽음: `PATCH /notifications/{id}/read`, `PATCH /notifications/read-all`; 삭제: `DELETE /notifications/{id}`, `DELETE /notifications/user`.

## 8. 다른 도메인과의 의존
- notification → user: `User` 엔티티(ManyToOne), `UserRepository` (`findById`, `findAll`).
- notification → order: `Order` (`notifyShipmentCompleted(Order)` 파라미터, `order.getUser()`).
- 다른 도메인 → notification: user(FollowCommandService, UserCommandService), payment(PaymentCommandService), sale(SaleCommandService), shipment(OrderShipmentCommandService, ShipmentItemProcessor, UpdateShipmentStatusesJobConfig), notice(NoticeCommandService). 위 표 참고.
- 병렬 경로: order 도메인 `OrderEventConsumer`가 notification 서비스를 거치지 않고 `SimpMessagingTemplate`로 같은 `/queue/notifications`에 직접 푸시(DB 미저장).
- `NotificationQueryService`는 notification 컨트롤러 외에서 사용되지 않음.

## 9. 예외
- `NotificationException extends GlobalException` (`ErrorCode` 아무거나 받음 — 컨트롤러에서 `GlobalErrorCode.INVALID_INPUT_VALUE`도 사용).
- `NotificationErrorCode` (접두사 `N`):
  | 코드 | enum | HTTP |
  |---|---|---|
  | N001 | NOTIFICATION_CREATION_FAILED | 500 |
  | N002 | NOTIFICATION_NOT_FOUND | 404 |
  | N003 | NOTIFICATION_RETRIEVAL_FAILED | 500 |
  | N004 | NOTIFICATION_UPDATE_UNAUTHORIZED | 403 (미사용) |
  | N005 | NOTIFICATION_UPDATE_FAILED | 500 (미사용) |
  | N006 | NOTIFICATION_DELETE_UNAUTHORIZED | 403 (미사용) |
  | N007 | NOTIFICATION_DELETE_FAILED | 500 |
  | N008 | NOTIFICATION_READ_FAILED | 500 |
  | N009 | NOTIFICATION_DELIVERY_FAILED | 500 (미사용) |
  | N010 | WEBSOCKET_CONNECTION_MAINTENANCE_FAILED | 500 |
  | N011 | NOTIFICATION_USER_NOT_FOUND | 404 |
  | N012 | NOTIFICATION_ACCESS_DENIED | 403 |
  | N020 | INVALID_NOTIFICATION_TYPE | 400 |
  | N021 | INVALID_NOTIFICATION_CATEGORY | 400 |
  ("미사용" = notification 패키지 내 throw 지점 없음, grep 기준)

## 10. 작업 팁 / 주의점
- `NotificationCommandService` 클래스 레벨 `@Transactional` → 다른 도메인 트랜잭션에 참여. `createNotification` 예외는 래핑되어 전파됨(호출자가 catch 안 하면 상위 트랜잭션 rollback-only).
- `markAllAsRead` (L140)는 모든 알림을 로드해 루프 처리(벌크 update 아님). `deleteNotificationsByUser`도 엔티티 로드 후 `deleteAll`.
- `deleteNotification` (L168)은 소유자 검증 없음 — 컨트롤러 주석(L120)에 "서비스에서 구현 필요".
- `POST /notifications`, `/broadcast`는 관리자 권한 체크 미구현(L53, L76). 인증만 되면 호출 가능.
- 페이징 조회 API는 `List`를 반환(총 개수 없음). 총 미읽음 수는 `/count/unread` 사용.
- 실시간 payload 형태가 단건(`NotificationDTO`)/전체(`List<NotificationDTO>`)/주문(`Map`)으로 섞여 같은 큐로 들어옴 — 클라이언트 파싱 시 주의.
- `NotificationDTO.createdAt`은 `LocalDateTime.toString()` 문자열.
- `handlePing`의 `extractEmailFromSecurityContext()`는 STOMP 메시지 처리 스레드의 `SecurityContextHolder`를 읽음(별도 ChannelInterceptor 없음).
- 의미상 배송/결제/판매 알림 대부분이 `NotificationType.BID`로 생성됨(TRADE/STORAGE 등은 사용처 없음).
- WebSocket allowed origin은 `https://www.pinjun.xyz` 하드코딩(WebSocketConfig L36).
