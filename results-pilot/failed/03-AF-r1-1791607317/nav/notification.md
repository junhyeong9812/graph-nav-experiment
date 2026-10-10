# notification 도메인

기준 경로: `src/main/java/com/fream/back/domain/notification/` · 파일 14개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/notifications` | `NotificationCommandController#createNotification` | `NotificationCommandService#createNotification` |
| POST | `/notifications/broadcast` | `NotificationCommandController#createNotificationForAll` | `NotificationCommandService#createNotificationForAll` |
| PATCH | `/notifications/{id}/read` | `NotificationCommandController#markAsRead` | `this#extractEmailFromSecurityContext`, `NotificationCommandService#markAsRead` |
| PATCH | `/notifications/read-all` | `NotificationCommandController#markAllAsRead` | `this#extractEmailFromSecurityContext`, `NotificationCommandService#markAllAsRead` |
| DELETE | `/notifications/{id}` | `NotificationCommandController#deleteNotification` | `this#extractEmailFromSecurityContext`, `NotificationCommandService#deleteNotification` |
| DELETE | `/notifications/user` | `NotificationCommandController#deleteAllUserNotifications` | `this#extractEmailFromSecurityContext`, `NotificationCommandService#deleteNotificationsByUser` |
| GET | `/notifications/filter/category` | `NotificationQueryController#filterByCategory` | `this#extractEmailFromSecurityContext`, `NotificationQueryService#filterByCategory` |
| GET | `/notifications/filter/type` | `NotificationQueryController#filterByType` | `this#extractEmailFromSecurityContext`, `NotificationQueryService#filterByType` |
| GET | `/notifications/filter/category/read-status` | `NotificationQueryController#filterByCategoryAndIsRead` | `this#validatePaginationParams`, `this#extractEmailFromSecurityContext`, `NotificationQueryService#filterByCategoryAndIsRead` |
| GET | `/notifications/filter/type/read-status` | `NotificationQueryController#filterByTypeAndIsRead` | `this#validatePaginationParams`, `this#extractEmailFromSecurityContext`, `NotificationQueryService#filterByTypeAndIsRead` |
| GET | `/notifications/count/unread` | `NotificationQueryController#countUnreadNotifications` | `this#extractEmailFromSecurityContext`, `NotificationQueryService#countUnreadNotifications` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `NotificationCommandService`
  - `createNotification` → UserRepository#findById~, this#buildNotification, NotificationRepository#save~, this#sendRealTimeNotification, this#toDTO
  - `createNotificationForAll` → UserRepository#findAll~, this#createNotificationsForUsers, NotificationRepository#saveAll~, this#broadcastNotificationsToConnectedUsers, this#toDTOList
  - `markAsRead` → NotificationRepository#findById~
  - `markAllAsRead` → NotificationRepository#findAllByUserEmail
  - `deleteNotification` → NotificationRepository#existsById~, NotificationRepository#deleteById~
  - `deleteNotificationsByUser` → NotificationRepository#findAllByUserEmail, NotificationRepository#deleteAll~
  - `notifyShipmentCompleted` → this#createNotification
  - `createNotificationsForUsers` → this#buildNotification
- `NotificationQueryService`
  - `filterByCategory` → NotificationRepository#findByUserEmailAndCategory
  - `filterByType` → NotificationRepository#findByUserEmailAndType
  - `filterByCategoryAndIsRead` → NotificationRepository#findByUserEmailAndCategoryAndIsRead
  - `filterByTypeAndIsRead` → NotificationRepository#findByUserEmailAndTypeAndIsRead
  - `countUnreadNotifications` → NotificationRepository#findByUserEmailAndIsRead
- `NotificationRepositoryCustomImpl`
  - `findByUserEmailAndCategory` → this#userEmailEq, this#categoryEq
  - `findByUserEmailAndType` → this#userEmailEq, this#typeEq
  - `findByUserEmailAndIsRead` → this#userEmailEq, this#isReadEq, this#getOrderSpecifiers
  - `findByUserEmailAndCategoryAndIsRead` → this#userEmailEq, this#categoryEq, this#isReadEq, this#getOrderSpecifiers
  - `findByUserEmailAndTypeAndIsRead` → this#userEmailEq, this#typeEq, this#isReadEq, this#getOrderSpecifiers
  - `findAllByUserEmail` → this#userEmailEq

## 3. 클래스 목록 (역할별)

### controller (2)

- `NotificationCommandController` controller/command/NotificationCommandController.java · public: createNotification, createNotificationForAll, markAsRead, markAllAsRead, deleteNotification, deleteAllUserNotifications, handlePing · 주입: NotificationCommandService
- `NotificationQueryController` controller/query/NotificationQueryController.java · public: filterByCategory, filterByType, filterByCategoryAndIsRead, filterByTypeAndIsRead, countUnreadNotifications · 주입: NotificationQueryService

### service-command (1)

- `NotificationCommandService` service/command/NotificationCommandService.java · public: createNotification, createNotificationForAll, markAsRead, markAllAsRead, deleteNotification, deleteNotificationsByUser, notifyShipmentCompleted, updateWebSocketConnectionTTL · 주입: NotificationRepository, UserRepository, SimpMessagingTemplate, RedisTemplate

### service-query (1)

- `NotificationQueryService` service/query/NotificationQueryService.java · public: filterByCategory, filterByType, filterByCategoryAndIsRead, filterByTypeAndIsRead, countUnreadNotifications · 주입: NotificationRepository

### repository (1)

- `NotificationRepository` (interface) repository/NotificationRepository.java · extends/implements JpaRepository, NotificationRepositoryCustom

### custom (2)

- `NotificationRepositoryCustom` (interface) repository/NotificationRepositoryCustom.java · public: findByUserEmailAndCategory, findByUserEmailAndType, findByUserEmailAndIsRead, findByUserEmailAndCategoryAndIsRead, findByUserEmailAndTypeAndIsRead, findAllByUserEmail
- `NotificationRepositoryCustomImpl` repository/NotificationRepositoryCustomImpl.java · extends/implements NotificationRepositoryCustom · public: findByUserEmailAndCategory, findByUserEmailAndType, findByUserEmailAndIsRead, findByUserEmailAndCategoryAndIsRead, findByUserEmailAndTypeAndIsRead, findAllByUserEmail · 주입: JPAQueryFactory

### entity (3)

- `Notification` entity/Notification.java · extends/implements BaseTimeEntity · public: markAsRead, markAsUnread
- `NotificationCategory` (enum) entity/NotificationCategory.java · public: getDescription
- `NotificationType` (enum) entity/NotificationType.java · public: getCategory, getDescription, getTypesByCategory

### dto (2)

- `NotificationDTO` dto/NotificationDTO.java
- `NotificationRequestDTO` dto/NotificationRequestDTO.java

### exception (2)

- `NotificationErrorCode` exception/NotificationErrorCode.java
- `NotificationException` exception/NotificationException.java

### 다른 도메인 참조 (import·주입 기준)

- **order**: `Order` ← NotificationCommandService
- **user**: `User` ← Notification, NotificationCommandService; `UserRepository` ← NotificationCommandService

### 이 도메인을 참조하는 다른 도메인

- **global**: Notification, NotificationCategory, NotificationRepository, NotificationType
- **notice**: NotificationCategory, NotificationCommandService, NotificationType
- **payment**: NotificationCategory, NotificationCommandService, NotificationType
- **sale**: NotificationCategory, NotificationCommandService, NotificationType
- **shipment**: NotificationCategory, NotificationCommandService, NotificationType
- **user**: NotificationCategory, NotificationCommandService, NotificationType

## 4. JPA 엔티티 연관

- `Notification` @Entity · extends BaseTimeEntity
  - user: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) → `User` [user]

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

