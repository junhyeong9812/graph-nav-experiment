# settlementnotification 도메인

기준 경로: `src/main/java/com/fream/back/domain/settlementnotification/` · 파일 6개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/settlement/notifications` | `SettlementNotificationCommandController#createNotification` | `SettlementNotificationCommandService#createNotification` |
| POST | `/settlement/notifications/broadcast` | `SettlementNotificationCommandController#createNotificationForAll` | `SettlementNotificationCommandService#createNotificationForAll` |
| PATCH | `/settlement/notifications/{id}/read` | `SettlementNotificationCommandController#markAsRead` | `this#extractEmailFromSecurityContext`, `SettlementNotificationCommandService#markAsRead` |
| PATCH | `/settlement/notifications/read-all` | `SettlementNotificationCommandController#markAllAsRead` | `this#extractEmailFromSecurityContext`, `SettlementNotificationCommandService#markAllAsRead` |
| DELETE | `/settlement/notifications/{id}` | `SettlementNotificationCommandController#deleteNotification` | `this#extractEmailFromSecurityContext`, `SettlementNotificationCommandService#deleteNotification` |
| DELETE | `/settlement/notifications/user` | `SettlementNotificationCommandController#deleteAllUserNotifications` | `this#extractEmailFromSecurityContext`, `SettlementNotificationCommandService#deleteNotificationsByUser` |
| GET | `/settlement/notifications/filter/category` | `SettlementNotificationQueryController#filterByCategory` | `this#extractEmailFromSecurityContext`, `SettlementNotificationQueryService#filterByCategory` |
| GET | `/settlement/notifications/filter/type` | `SettlementNotificationQueryController#filterByType` | `this#extractEmailFromSecurityContext`, `SettlementNotificationQueryService#filterByType` |
| GET | `/settlement/notifications/filter/category/read-status` | `SettlementNotificationQueryController#filterByCategoryAndIsRead` | `this#validatePaginationParams`, `this#extractEmailFromSecurityContext`, `SettlementNotificationQueryService#filterByCategoryAndIsRead` |
| GET | `/settlement/notifications/filter/type/read-status` | `SettlementNotificationQueryController#filterByTypeAndIsRead` | `this#validatePaginationParams`, `this#extractEmailFromSecurityContext`, `SettlementNotificationQueryService#filterByTypeAndIsRead` |
| GET | `/settlement/notifications/count/unread` | `SettlementNotificationQueryController#countUnreadNotifications` | `this#extractEmailFromSecurityContext`, `SettlementNotificationQueryService#countUnreadNotifications` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `SettlementNotificationCommandService`
  - `createNotification` → UserRepository#findById~, this#buildNotification, NotificationRepository#save~, this#sendRealTimeNotification, this#toDTO
  - `createNotificationForAll` → UserRepository#findAll~, this#createNotificationsForUsers, NotificationRepository#saveAll~, this#broadcastNotificationsToConnectedUsers, this#toDTOList
  - `markAsRead` → NotificationRepository#findById~
  - `markAllAsRead` → NotificationRepository#findAllByUserEmail
  - `deleteNotification` → NotificationRepository#existsById~, NotificationRepository#deleteById~
  - `deleteNotificationsByUser` → NotificationRepository#findAllByUserEmail, NotificationRepository#deleteAll~
  - `notifyShipmentCompleted` → this#createNotification
  - `createNotificationsForUsers` → this#buildNotification
- `SettlementNotificationQueryService`
  - `filterByCategory` → NotificationRepository#findByUserEmailAndCategory
  - `filterByType` → NotificationRepository#findByUserEmailAndType
  - `filterByCategoryAndIsRead` → NotificationRepository#findByUserEmailAndCategoryAndIsRead
  - `filterByTypeAndIsRead` → NotificationRepository#findByUserEmailAndTypeAndIsRead
  - `countUnreadNotifications` → NotificationRepository#findByUserEmailAndIsRead
- `SettlementNotificationRepositoryCustomImpl`
  - `findByUserEmailAndCategory` → this#userEmailEq, this#categoryEq
  - `findByUserEmailAndType` → this#userEmailEq, this#typeEq
  - `findByUserEmailAndIsRead` → this#userEmailEq, this#isReadEq, this#getOrderSpecifiers
  - `findByUserEmailAndCategoryAndIsRead` → this#userEmailEq, this#categoryEq, this#isReadEq, this#getOrderSpecifiers
  - `findByUserEmailAndTypeAndIsRead` → this#userEmailEq, this#typeEq, this#isReadEq, this#getOrderSpecifiers
  - `findAllByUserEmail` → this#userEmailEq

## 3. 클래스 목록 (역할별)

### controller (2)

- `SettlementNotificationCommandController` controller/command/SettlementNotificationCommandController.java · public: createNotification, createNotificationForAll, markAsRead, markAllAsRead, deleteNotification, deleteAllUserNotifications, handlePing · 주입: SettlementNotificationCommandService
- `SettlementNotificationQueryController` controller/query/SettlementNotificationQueryController.java · public: filterByCategory, filterByType, filterByCategoryAndIsRead, filterByTypeAndIsRead, countUnreadNotifications · 주입: SettlementNotificationQueryService

### service-command (1)

- `SettlementNotificationCommandService` service/command/SettlementNotificationCommandService.java · public: createNotification, createNotificationForAll, markAsRead, markAllAsRead, deleteNotification, deleteNotificationsByUser, notifyShipmentCompleted, updateWebSocketConnectionTTL · 주입: NotificationRepository, UserRepository, SimpMessagingTemplate, RedisTemplate

### service-query (1)

- `SettlementNotificationQueryService` service/query/SettlementNotificationQueryService.java · public: filterByCategory, filterByType, filterByCategoryAndIsRead, filterByTypeAndIsRead, countUnreadNotifications · 주입: NotificationRepository

### custom (2)

- `SettlementNotificationRepositoryCustom` (interface) repository/SettlementNotificationRepositoryCustom.java · public: findByUserEmailAndCategory, findByUserEmailAndType, findByUserEmailAndIsRead, findByUserEmailAndCategoryAndIsRead, findByUserEmailAndTypeAndIsRead, findAllByUserEmail
- `SettlementNotificationRepositoryCustomImpl` repository/SettlementNotificationRepositoryCustomImpl.java · extends/implements SettlementNotificationRepositoryCustom · public: findByUserEmailAndCategory, findByUserEmailAndType, findByUserEmailAndIsRead, findByUserEmailAndCategoryAndIsRead, findByUserEmailAndTypeAndIsRead, findAllByUserEmail · 주입: JPAQueryFactory

### 다른 도메인 참조 (import·주입 기준)

- **notification**: `Notification` ← SettlementNotificationCommandService, SettlementNotificationQueryService, SettlementNotificationRepositoryCustom, SettlementNotificationRepositoryCustomImpl; `NotificationCategory` ← SettlementNotificationCommandService, SettlementNotificationQueryController, SettlementNotificationQueryService, SettlementNotificationRepositoryCustom, SettlementNotificationRepositoryCustomImpl; `NotificationRepository` ← SettlementNotificationCommandService, SettlementNotificationQueryService; `NotificationType` ← SettlementNotificationCommandService, SettlementNotificationQueryController, SettlementNotificationQueryService, SettlementNotificationRepositoryCustom, SettlementNotificationRepositoryCustomImpl
- **order**: `Order` ← SettlementNotificationCommandService
- **user**: `User` ← SettlementNotificationCommandService; `UserRepository` ← SettlementNotificationCommandService

### 이 도메인을 참조하는 다른 도메인

- **settlementnotice**: SettlementNotificationCommandService
- **settlementsale**: SettlementNotificationCommandService
- **settlementshipment**: SettlementNotificationCommandService
- **settlementuser**: SettlementNotificationCommandService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

