# partnernotification 도메인

기준 경로: `src/main/java/com/fream/back/domain/partnernotification/` · 파일 6개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/partner/notifications` | `PartnerNotificationCommandController#createNotification` | `PartnerNotificationCommandService#createNotification` |
| POST | `/partner/notifications/broadcast` | `PartnerNotificationCommandController#createNotificationForAll` | `PartnerNotificationCommandService#createNotificationForAll` |
| PATCH | `/partner/notifications/{id}/read` | `PartnerNotificationCommandController#markAsRead` | `this#extractEmailFromSecurityContext`, `PartnerNotificationCommandService#markAsRead` |
| PATCH | `/partner/notifications/read-all` | `PartnerNotificationCommandController#markAllAsRead` | `this#extractEmailFromSecurityContext`, `PartnerNotificationCommandService#markAllAsRead` |
| DELETE | `/partner/notifications/{id}` | `PartnerNotificationCommandController#deleteNotification` | `this#extractEmailFromSecurityContext`, `PartnerNotificationCommandService#deleteNotification` |
| DELETE | `/partner/notifications/user` | `PartnerNotificationCommandController#deleteAllUserNotifications` | `this#extractEmailFromSecurityContext`, `PartnerNotificationCommandService#deleteNotificationsByUser` |
| GET | `/partner/notifications/filter/category` | `PartnerNotificationQueryController#filterByCategory` | `this#extractEmailFromSecurityContext`, `PartnerNotificationQueryService#filterByCategory` |
| GET | `/partner/notifications/filter/type` | `PartnerNotificationQueryController#filterByType` | `this#extractEmailFromSecurityContext`, `PartnerNotificationQueryService#filterByType` |
| GET | `/partner/notifications/filter/category/read-status` | `PartnerNotificationQueryController#filterByCategoryAndIsRead` | `this#validatePaginationParams`, `this#extractEmailFromSecurityContext`, `PartnerNotificationQueryService#filterByCategoryAndIsRead` |
| GET | `/partner/notifications/filter/type/read-status` | `PartnerNotificationQueryController#filterByTypeAndIsRead` | `this#validatePaginationParams`, `this#extractEmailFromSecurityContext`, `PartnerNotificationQueryService#filterByTypeAndIsRead` |
| GET | `/partner/notifications/count/unread` | `PartnerNotificationQueryController#countUnreadNotifications` | `this#extractEmailFromSecurityContext`, `PartnerNotificationQueryService#countUnreadNotifications` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `PartnerNotificationCommandService`
  - `createNotification` → UserRepository#findById~, this#buildNotification, NotificationRepository#save~, this#sendRealTimeNotification, this#toDTO
  - `createNotificationForAll` → UserRepository#findAll~, this#createNotificationsForUsers, NotificationRepository#saveAll~, this#broadcastNotificationsToConnectedUsers, this#toDTOList
  - `markAsRead` → NotificationRepository#findById~
  - `markAllAsRead` → NotificationRepository#findAllByUserEmail
  - `deleteNotification` → NotificationRepository#existsById~, NotificationRepository#deleteById~
  - `deleteNotificationsByUser` → NotificationRepository#findAllByUserEmail, NotificationRepository#deleteAll~
  - `notifyShipmentCompleted` → this#createNotification
  - `createNotificationsForUsers` → this#buildNotification
- `PartnerNotificationQueryService`
  - `filterByCategory` → NotificationRepository#findByUserEmailAndCategory
  - `filterByType` → NotificationRepository#findByUserEmailAndType
  - `filterByCategoryAndIsRead` → NotificationRepository#findByUserEmailAndCategoryAndIsRead
  - `filterByTypeAndIsRead` → NotificationRepository#findByUserEmailAndTypeAndIsRead
  - `countUnreadNotifications` → NotificationRepository#findByUserEmailAndIsRead
- `PartnerNotificationRepositoryCustomImpl`
  - `findByUserEmailAndCategory` → this#userEmailEq, this#categoryEq
  - `findByUserEmailAndType` → this#userEmailEq, this#typeEq
  - `findByUserEmailAndIsRead` → this#userEmailEq, this#isReadEq, this#getOrderSpecifiers
  - `findByUserEmailAndCategoryAndIsRead` → this#userEmailEq, this#categoryEq, this#isReadEq, this#getOrderSpecifiers
  - `findByUserEmailAndTypeAndIsRead` → this#userEmailEq, this#typeEq, this#isReadEq, this#getOrderSpecifiers
  - `findAllByUserEmail` → this#userEmailEq

## 3. 클래스 목록 (역할별)

### controller (2)

- `PartnerNotificationCommandController` controller/command/PartnerNotificationCommandController.java · public: createNotification, createNotificationForAll, markAsRead, markAllAsRead, deleteNotification, deleteAllUserNotifications, handlePing · 주입: PartnerNotificationCommandService
- `PartnerNotificationQueryController` controller/query/PartnerNotificationQueryController.java · public: filterByCategory, filterByType, filterByCategoryAndIsRead, filterByTypeAndIsRead, countUnreadNotifications · 주입: PartnerNotificationQueryService

### service-command (1)

- `PartnerNotificationCommandService` service/command/PartnerNotificationCommandService.java · public: createNotification, createNotificationForAll, markAsRead, markAllAsRead, deleteNotification, deleteNotificationsByUser, notifyShipmentCompleted, updateWebSocketConnectionTTL · 주입: NotificationRepository, UserRepository, SimpMessagingTemplate, RedisTemplate

### service-query (1)

- `PartnerNotificationQueryService` service/query/PartnerNotificationQueryService.java · public: filterByCategory, filterByType, filterByCategoryAndIsRead, filterByTypeAndIsRead, countUnreadNotifications · 주입: NotificationRepository

### custom (2)

- `PartnerNotificationRepositoryCustom` (interface) repository/PartnerNotificationRepositoryCustom.java · public: findByUserEmailAndCategory, findByUserEmailAndType, findByUserEmailAndIsRead, findByUserEmailAndCategoryAndIsRead, findByUserEmailAndTypeAndIsRead, findAllByUserEmail
- `PartnerNotificationRepositoryCustomImpl` repository/PartnerNotificationRepositoryCustomImpl.java · extends/implements PartnerNotificationRepositoryCustom · public: findByUserEmailAndCategory, findByUserEmailAndType, findByUserEmailAndIsRead, findByUserEmailAndCategoryAndIsRead, findByUserEmailAndTypeAndIsRead, findAllByUserEmail · 주입: JPAQueryFactory

### 다른 도메인 참조 (import·주입 기준)

- **notification**: `Notification` ← PartnerNotificationCommandService, PartnerNotificationQueryService, PartnerNotificationRepositoryCustom, PartnerNotificationRepositoryCustomImpl; `NotificationCategory` ← PartnerNotificationCommandService, PartnerNotificationQueryController, PartnerNotificationQueryService, PartnerNotificationRepositoryCustom, PartnerNotificationRepositoryCustomImpl; `NotificationRepository` ← PartnerNotificationCommandService, PartnerNotificationQueryService; `NotificationType` ← PartnerNotificationCommandService, PartnerNotificationQueryController, PartnerNotificationQueryService, PartnerNotificationRepositoryCustom, PartnerNotificationRepositoryCustomImpl
- **order**: `Order` ← PartnerNotificationCommandService
- **user**: `User` ← PartnerNotificationCommandService; `UserRepository` ← PartnerNotificationCommandService

### 이 도메인을 참조하는 다른 도메인

- **partnernotice**: PartnerNotificationCommandService
- **partnersale**: PartnerNotificationCommandService
- **partnershipment**: PartnerNotificationCommandService
- **partneruser**: PartnerNotificationCommandService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

