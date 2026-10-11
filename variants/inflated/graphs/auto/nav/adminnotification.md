# adminnotification 도메인

기준 경로: `src/main/java/com/fream/back/domain/adminnotification/` · 파일 6개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/admin/notifications` | `AdminNotificationCommandController#createNotification` | `AdminNotificationCommandService#createNotification` |
| POST | `/admin/notifications/broadcast` | `AdminNotificationCommandController#createNotificationForAll` | `AdminNotificationCommandService#createNotificationForAll` |
| PATCH | `/admin/notifications/{id}/read` | `AdminNotificationCommandController#markAsRead` | `this#extractEmailFromSecurityContext`, `AdminNotificationCommandService#markAsRead` |
| PATCH | `/admin/notifications/read-all` | `AdminNotificationCommandController#markAllAsRead` | `this#extractEmailFromSecurityContext`, `AdminNotificationCommandService#markAllAsRead` |
| DELETE | `/admin/notifications/{id}` | `AdminNotificationCommandController#deleteNotification` | `this#extractEmailFromSecurityContext`, `AdminNotificationCommandService#deleteNotification` |
| DELETE | `/admin/notifications/user` | `AdminNotificationCommandController#deleteAllUserNotifications` | `this#extractEmailFromSecurityContext`, `AdminNotificationCommandService#deleteNotificationsByUser` |
| GET | `/admin/notifications/filter/category` | `AdminNotificationQueryController#filterByCategory` | `this#extractEmailFromSecurityContext`, `AdminNotificationQueryService#filterByCategory` |
| GET | `/admin/notifications/filter/type` | `AdminNotificationQueryController#filterByType` | `this#extractEmailFromSecurityContext`, `AdminNotificationQueryService#filterByType` |
| GET | `/admin/notifications/filter/category/read-status` | `AdminNotificationQueryController#filterByCategoryAndIsRead` | `this#validatePaginationParams`, `this#extractEmailFromSecurityContext`, `AdminNotificationQueryService#filterByCategoryAndIsRead` |
| GET | `/admin/notifications/filter/type/read-status` | `AdminNotificationQueryController#filterByTypeAndIsRead` | `this#validatePaginationParams`, `this#extractEmailFromSecurityContext`, `AdminNotificationQueryService#filterByTypeAndIsRead` |
| GET | `/admin/notifications/count/unread` | `AdminNotificationQueryController#countUnreadNotifications` | `this#extractEmailFromSecurityContext`, `AdminNotificationQueryService#countUnreadNotifications` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `AdminNotificationCommandService`
  - `createNotification` → UserRepository#findById~, this#buildNotification, NotificationRepository#save~, this#sendRealTimeNotification, this#toDTO
  - `createNotificationForAll` → UserRepository#findAll~, this#createNotificationsForUsers, NotificationRepository#saveAll~, this#broadcastNotificationsToConnectedUsers, this#toDTOList
  - `markAsRead` → NotificationRepository#findById~
  - `markAllAsRead` → NotificationRepository#findAllByUserEmail
  - `deleteNotification` → NotificationRepository#existsById~, NotificationRepository#deleteById~
  - `deleteNotificationsByUser` → NotificationRepository#findAllByUserEmail, NotificationRepository#deleteAll~
  - `notifyShipmentCompleted` → this#createNotification
  - `createNotificationsForUsers` → this#buildNotification
- `AdminNotificationQueryService`
  - `filterByCategory` → NotificationRepository#findByUserEmailAndCategory
  - `filterByType` → NotificationRepository#findByUserEmailAndType
  - `filterByCategoryAndIsRead` → NotificationRepository#findByUserEmailAndCategoryAndIsRead
  - `filterByTypeAndIsRead` → NotificationRepository#findByUserEmailAndTypeAndIsRead
  - `countUnreadNotifications` → NotificationRepository#findByUserEmailAndIsRead
- `AdminNotificationRepositoryCustomImpl`
  - `findByUserEmailAndCategory` → this#userEmailEq, this#categoryEq
  - `findByUserEmailAndType` → this#userEmailEq, this#typeEq
  - `findByUserEmailAndIsRead` → this#userEmailEq, this#isReadEq, this#getOrderSpecifiers
  - `findByUserEmailAndCategoryAndIsRead` → this#userEmailEq, this#categoryEq, this#isReadEq, this#getOrderSpecifiers
  - `findByUserEmailAndTypeAndIsRead` → this#userEmailEq, this#typeEq, this#isReadEq, this#getOrderSpecifiers
  - `findAllByUserEmail` → this#userEmailEq

## 3. 클래스 목록 (역할별)

### controller (2)

- `AdminNotificationCommandController` controller/command/AdminNotificationCommandController.java · public: createNotification, createNotificationForAll, markAsRead, markAllAsRead, deleteNotification, deleteAllUserNotifications, handlePing · 주입: AdminNotificationCommandService
- `AdminNotificationQueryController` controller/query/AdminNotificationQueryController.java · public: filterByCategory, filterByType, filterByCategoryAndIsRead, filterByTypeAndIsRead, countUnreadNotifications · 주입: AdminNotificationQueryService

### service-command (1)

- `AdminNotificationCommandService` service/command/AdminNotificationCommandService.java · public: createNotification, createNotificationForAll, markAsRead, markAllAsRead, deleteNotification, deleteNotificationsByUser, notifyShipmentCompleted, updateWebSocketConnectionTTL · 주입: NotificationRepository, UserRepository, SimpMessagingTemplate, RedisTemplate

### service-query (1)

- `AdminNotificationQueryService` service/query/AdminNotificationQueryService.java · public: filterByCategory, filterByType, filterByCategoryAndIsRead, filterByTypeAndIsRead, countUnreadNotifications · 주입: NotificationRepository

### custom (2)

- `AdminNotificationRepositoryCustom` (interface) repository/AdminNotificationRepositoryCustom.java · public: findByUserEmailAndCategory, findByUserEmailAndType, findByUserEmailAndIsRead, findByUserEmailAndCategoryAndIsRead, findByUserEmailAndTypeAndIsRead, findAllByUserEmail
- `AdminNotificationRepositoryCustomImpl` repository/AdminNotificationRepositoryCustomImpl.java · extends/implements AdminNotificationRepositoryCustom · public: findByUserEmailAndCategory, findByUserEmailAndType, findByUserEmailAndIsRead, findByUserEmailAndCategoryAndIsRead, findByUserEmailAndTypeAndIsRead, findAllByUserEmail · 주입: JPAQueryFactory

### 다른 도메인 참조 (import·주입 기준)

- **notification**: `Notification` ← AdminNotificationCommandService, AdminNotificationQueryService, AdminNotificationRepositoryCustom, AdminNotificationRepositoryCustomImpl; `NotificationCategory` ← AdminNotificationCommandService, AdminNotificationQueryController, AdminNotificationQueryService, AdminNotificationRepositoryCustom, AdminNotificationRepositoryCustomImpl; `NotificationRepository` ← AdminNotificationCommandService, AdminNotificationQueryService; `NotificationType` ← AdminNotificationCommandService, AdminNotificationQueryController, AdminNotificationQueryService, AdminNotificationRepositoryCustom, AdminNotificationRepositoryCustomImpl
- **order**: `Order` ← AdminNotificationCommandService
- **user**: `User` ← AdminNotificationCommandService; `UserRepository` ← AdminNotificationCommandService

### 이 도메인을 참조하는 다른 도메인

- **adminnotice**: AdminNotificationCommandService
- **adminsale**: AdminNotificationCommandService
- **adminshipment**: AdminNotificationCommandService
- **adminuser**: AdminNotificationCommandService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

