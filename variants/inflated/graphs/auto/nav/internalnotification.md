# internalnotification 도메인

기준 경로: `src/main/java/com/fream/back/domain/internalnotification/` · 파일 6개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/internal/notifications` | `InternalNotificationCommandController#createNotification` | `InternalNotificationCommandService#createNotification` |
| POST | `/internal/notifications/broadcast` | `InternalNotificationCommandController#createNotificationForAll` | `InternalNotificationCommandService#createNotificationForAll` |
| PATCH | `/internal/notifications/{id}/read` | `InternalNotificationCommandController#markAsRead` | `this#extractEmailFromSecurityContext`, `InternalNotificationCommandService#markAsRead` |
| PATCH | `/internal/notifications/read-all` | `InternalNotificationCommandController#markAllAsRead` | `this#extractEmailFromSecurityContext`, `InternalNotificationCommandService#markAllAsRead` |
| DELETE | `/internal/notifications/{id}` | `InternalNotificationCommandController#deleteNotification` | `this#extractEmailFromSecurityContext`, `InternalNotificationCommandService#deleteNotification` |
| DELETE | `/internal/notifications/user` | `InternalNotificationCommandController#deleteAllUserNotifications` | `this#extractEmailFromSecurityContext`, `InternalNotificationCommandService#deleteNotificationsByUser` |
| GET | `/internal/notifications/filter/category` | `InternalNotificationQueryController#filterByCategory` | `this#extractEmailFromSecurityContext`, `InternalNotificationQueryService#filterByCategory` |
| GET | `/internal/notifications/filter/type` | `InternalNotificationQueryController#filterByType` | `this#extractEmailFromSecurityContext`, `InternalNotificationQueryService#filterByType` |
| GET | `/internal/notifications/filter/category/read-status` | `InternalNotificationQueryController#filterByCategoryAndIsRead` | `this#validatePaginationParams`, `this#extractEmailFromSecurityContext`, `InternalNotificationQueryService#filterByCategoryAndIsRead` |
| GET | `/internal/notifications/filter/type/read-status` | `InternalNotificationQueryController#filterByTypeAndIsRead` | `this#validatePaginationParams`, `this#extractEmailFromSecurityContext`, `InternalNotificationQueryService#filterByTypeAndIsRead` |
| GET | `/internal/notifications/count/unread` | `InternalNotificationQueryController#countUnreadNotifications` | `this#extractEmailFromSecurityContext`, `InternalNotificationQueryService#countUnreadNotifications` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `InternalNotificationCommandService`
  - `createNotification` → UserRepository#findById~, this#buildNotification, NotificationRepository#save~, this#sendRealTimeNotification, this#toDTO
  - `createNotificationForAll` → UserRepository#findAll~, this#createNotificationsForUsers, NotificationRepository#saveAll~, this#broadcastNotificationsToConnectedUsers, this#toDTOList
  - `markAsRead` → NotificationRepository#findById~
  - `markAllAsRead` → NotificationRepository#findAllByUserEmail
  - `deleteNotification` → NotificationRepository#existsById~, NotificationRepository#deleteById~
  - `deleteNotificationsByUser` → NotificationRepository#findAllByUserEmail, NotificationRepository#deleteAll~
  - `notifyShipmentCompleted` → this#createNotification
  - `createNotificationsForUsers` → this#buildNotification
- `InternalNotificationQueryService`
  - `filterByCategory` → NotificationRepository#findByUserEmailAndCategory
  - `filterByType` → NotificationRepository#findByUserEmailAndType
  - `filterByCategoryAndIsRead` → NotificationRepository#findByUserEmailAndCategoryAndIsRead
  - `filterByTypeAndIsRead` → NotificationRepository#findByUserEmailAndTypeAndIsRead
  - `countUnreadNotifications` → NotificationRepository#findByUserEmailAndIsRead
- `InternalNotificationRepositoryCustomImpl`
  - `findByUserEmailAndCategory` → this#userEmailEq, this#categoryEq
  - `findByUserEmailAndType` → this#userEmailEq, this#typeEq
  - `findByUserEmailAndIsRead` → this#userEmailEq, this#isReadEq, this#getOrderSpecifiers
  - `findByUserEmailAndCategoryAndIsRead` → this#userEmailEq, this#categoryEq, this#isReadEq, this#getOrderSpecifiers
  - `findByUserEmailAndTypeAndIsRead` → this#userEmailEq, this#typeEq, this#isReadEq, this#getOrderSpecifiers
  - `findAllByUserEmail` → this#userEmailEq

## 3. 클래스 목록 (역할별)

### controller (2)

- `InternalNotificationCommandController` controller/command/InternalNotificationCommandController.java · public: createNotification, createNotificationForAll, markAsRead, markAllAsRead, deleteNotification, deleteAllUserNotifications, handlePing · 주입: InternalNotificationCommandService
- `InternalNotificationQueryController` controller/query/InternalNotificationQueryController.java · public: filterByCategory, filterByType, filterByCategoryAndIsRead, filterByTypeAndIsRead, countUnreadNotifications · 주입: InternalNotificationQueryService

### service-command (1)

- `InternalNotificationCommandService` service/command/InternalNotificationCommandService.java · public: createNotification, createNotificationForAll, markAsRead, markAllAsRead, deleteNotification, deleteNotificationsByUser, notifyShipmentCompleted, updateWebSocketConnectionTTL · 주입: NotificationRepository, UserRepository, SimpMessagingTemplate, RedisTemplate

### service-query (1)

- `InternalNotificationQueryService` service/query/InternalNotificationQueryService.java · public: filterByCategory, filterByType, filterByCategoryAndIsRead, filterByTypeAndIsRead, countUnreadNotifications · 주입: NotificationRepository

### custom (2)

- `InternalNotificationRepositoryCustom` (interface) repository/InternalNotificationRepositoryCustom.java · public: findByUserEmailAndCategory, findByUserEmailAndType, findByUserEmailAndIsRead, findByUserEmailAndCategoryAndIsRead, findByUserEmailAndTypeAndIsRead, findAllByUserEmail
- `InternalNotificationRepositoryCustomImpl` repository/InternalNotificationRepositoryCustomImpl.java · extends/implements InternalNotificationRepositoryCustom · public: findByUserEmailAndCategory, findByUserEmailAndType, findByUserEmailAndIsRead, findByUserEmailAndCategoryAndIsRead, findByUserEmailAndTypeAndIsRead, findAllByUserEmail · 주입: JPAQueryFactory

### 다른 도메인 참조 (import·주입 기준)

- **notification**: `Notification` ← InternalNotificationCommandService, InternalNotificationQueryService, InternalNotificationRepositoryCustom, InternalNotificationRepositoryCustomImpl; `NotificationCategory` ← InternalNotificationCommandService, InternalNotificationQueryController, InternalNotificationQueryService, InternalNotificationRepositoryCustom, InternalNotificationRepositoryCustomImpl; `NotificationRepository` ← InternalNotificationCommandService, InternalNotificationQueryService; `NotificationType` ← InternalNotificationCommandService, InternalNotificationQueryController, InternalNotificationQueryService, InternalNotificationRepositoryCustom, InternalNotificationRepositoryCustomImpl
- **order**: `Order` ← InternalNotificationCommandService
- **user**: `User` ← InternalNotificationCommandService; `UserRepository` ← InternalNotificationCommandService

### 이 도메인을 참조하는 다른 도메인

- **internalnotice**: InternalNotificationCommandService
- **internalsale**: InternalNotificationCommandService
- **internalshipment**: InternalNotificationCommandService
- **internaluser**: InternalNotificationCommandService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

