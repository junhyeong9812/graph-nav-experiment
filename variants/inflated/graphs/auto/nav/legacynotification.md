# legacynotification 도메인

기준 경로: `src/main/java/com/fream/back/domain/legacynotification/` · 파일 6개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/legacy/notifications` | `LegacyNotificationCommandController#createNotification` | `LegacyNotificationCommandService#createNotification` |
| POST | `/legacy/notifications/broadcast` | `LegacyNotificationCommandController#createNotificationForAll` | `LegacyNotificationCommandService#createNotificationForAll` |
| PATCH | `/legacy/notifications/{id}/read` | `LegacyNotificationCommandController#markAsRead` | `this#extractEmailFromSecurityContext`, `LegacyNotificationCommandService#markAsRead` |
| PATCH | `/legacy/notifications/read-all` | `LegacyNotificationCommandController#markAllAsRead` | `this#extractEmailFromSecurityContext`, `LegacyNotificationCommandService#markAllAsRead` |
| DELETE | `/legacy/notifications/{id}` | `LegacyNotificationCommandController#deleteNotification` | `this#extractEmailFromSecurityContext`, `LegacyNotificationCommandService#deleteNotification` |
| DELETE | `/legacy/notifications/user` | `LegacyNotificationCommandController#deleteAllUserNotifications` | `this#extractEmailFromSecurityContext`, `LegacyNotificationCommandService#deleteNotificationsByUser` |
| GET | `/legacy/notifications/filter/category` | `LegacyNotificationQueryController#filterByCategory` | `this#extractEmailFromSecurityContext`, `LegacyNotificationQueryService#filterByCategory` |
| GET | `/legacy/notifications/filter/type` | `LegacyNotificationQueryController#filterByType` | `this#extractEmailFromSecurityContext`, `LegacyNotificationQueryService#filterByType` |
| GET | `/legacy/notifications/filter/category/read-status` | `LegacyNotificationQueryController#filterByCategoryAndIsRead` | `this#validatePaginationParams`, `this#extractEmailFromSecurityContext`, `LegacyNotificationQueryService#filterByCategoryAndIsRead` |
| GET | `/legacy/notifications/filter/type/read-status` | `LegacyNotificationQueryController#filterByTypeAndIsRead` | `this#validatePaginationParams`, `this#extractEmailFromSecurityContext`, `LegacyNotificationQueryService#filterByTypeAndIsRead` |
| GET | `/legacy/notifications/count/unread` | `LegacyNotificationQueryController#countUnreadNotifications` | `this#extractEmailFromSecurityContext`, `LegacyNotificationQueryService#countUnreadNotifications` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `LegacyNotificationCommandService`
  - `createNotification` → UserRepository#findById~, this#buildNotification, NotificationRepository#save~, this#sendRealTimeNotification, this#toDTO
  - `createNotificationForAll` → UserRepository#findAll~, this#createNotificationsForUsers, NotificationRepository#saveAll~, this#broadcastNotificationsToConnectedUsers, this#toDTOList
  - `markAsRead` → NotificationRepository#findById~
  - `markAllAsRead` → NotificationRepository#findAllByUserEmail
  - `deleteNotification` → NotificationRepository#existsById~, NotificationRepository#deleteById~
  - `deleteNotificationsByUser` → NotificationRepository#findAllByUserEmail, NotificationRepository#deleteAll~
  - `notifyShipmentCompleted` → this#createNotification
  - `createNotificationsForUsers` → this#buildNotification
- `LegacyNotificationQueryService`
  - `filterByCategory` → NotificationRepository#findByUserEmailAndCategory
  - `filterByType` → NotificationRepository#findByUserEmailAndType
  - `filterByCategoryAndIsRead` → NotificationRepository#findByUserEmailAndCategoryAndIsRead
  - `filterByTypeAndIsRead` → NotificationRepository#findByUserEmailAndTypeAndIsRead
  - `countUnreadNotifications` → NotificationRepository#findByUserEmailAndIsRead
- `LegacyNotificationRepositoryCustomImpl`
  - `findByUserEmailAndCategory` → this#userEmailEq, this#categoryEq
  - `findByUserEmailAndType` → this#userEmailEq, this#typeEq
  - `findByUserEmailAndIsRead` → this#userEmailEq, this#isReadEq, this#getOrderSpecifiers
  - `findByUserEmailAndCategoryAndIsRead` → this#userEmailEq, this#categoryEq, this#isReadEq, this#getOrderSpecifiers
  - `findByUserEmailAndTypeAndIsRead` → this#userEmailEq, this#typeEq, this#isReadEq, this#getOrderSpecifiers
  - `findAllByUserEmail` → this#userEmailEq

## 3. 클래스 목록 (역할별)

### controller (2)

- `LegacyNotificationCommandController` controller/command/LegacyNotificationCommandController.java · public: createNotification, createNotificationForAll, markAsRead, markAllAsRead, deleteNotification, deleteAllUserNotifications, handlePing · 주입: LegacyNotificationCommandService
- `LegacyNotificationQueryController` controller/query/LegacyNotificationQueryController.java · public: filterByCategory, filterByType, filterByCategoryAndIsRead, filterByTypeAndIsRead, countUnreadNotifications · 주입: LegacyNotificationQueryService

### service-command (1)

- `LegacyNotificationCommandService` service/command/LegacyNotificationCommandService.java · public: createNotification, createNotificationForAll, markAsRead, markAllAsRead, deleteNotification, deleteNotificationsByUser, notifyShipmentCompleted, updateWebSocketConnectionTTL · 주입: NotificationRepository, UserRepository, SimpMessagingTemplate, RedisTemplate

### service-query (1)

- `LegacyNotificationQueryService` service/query/LegacyNotificationQueryService.java · public: filterByCategory, filterByType, filterByCategoryAndIsRead, filterByTypeAndIsRead, countUnreadNotifications · 주입: NotificationRepository

### custom (2)

- `LegacyNotificationRepositoryCustom` (interface) repository/LegacyNotificationRepositoryCustom.java · public: findByUserEmailAndCategory, findByUserEmailAndType, findByUserEmailAndIsRead, findByUserEmailAndCategoryAndIsRead, findByUserEmailAndTypeAndIsRead, findAllByUserEmail
- `LegacyNotificationRepositoryCustomImpl` repository/LegacyNotificationRepositoryCustomImpl.java · extends/implements LegacyNotificationRepositoryCustom · public: findByUserEmailAndCategory, findByUserEmailAndType, findByUserEmailAndIsRead, findByUserEmailAndCategoryAndIsRead, findByUserEmailAndTypeAndIsRead, findAllByUserEmail · 주입: JPAQueryFactory

### 다른 도메인 참조 (import·주입 기준)

- **notification**: `Notification` ← LegacyNotificationCommandService, LegacyNotificationQueryService, LegacyNotificationRepositoryCustom, LegacyNotificationRepositoryCustomImpl; `NotificationCategory` ← LegacyNotificationCommandService, LegacyNotificationQueryController, LegacyNotificationQueryService, LegacyNotificationRepositoryCustom, LegacyNotificationRepositoryCustomImpl; `NotificationRepository` ← LegacyNotificationCommandService, LegacyNotificationQueryService; `NotificationType` ← LegacyNotificationCommandService, LegacyNotificationQueryController, LegacyNotificationQueryService, LegacyNotificationRepositoryCustom, LegacyNotificationRepositoryCustomImpl
- **order**: `Order` ← LegacyNotificationCommandService
- **user**: `User` ← LegacyNotificationCommandService; `UserRepository` ← LegacyNotificationCommandService

### 이 도메인을 참조하는 다른 도메인

- **legacynotice**: LegacyNotificationCommandService
- **legacysale**: LegacyNotificationCommandService
- **legacyshipment**: LegacyNotificationCommandService
- **legacyuser**: LegacyNotificationCommandService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

