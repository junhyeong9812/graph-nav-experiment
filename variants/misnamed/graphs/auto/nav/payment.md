# payment 도메인

기준 경로: `src/main/java/com/fream/back/domain/payment/` · 파일 14개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/notifications` | `PaymentCommandController#createNotification` | `PaymentCommandService#createNotification` |
| POST | `/notifications/broadcast` | `PaymentCommandController#createNotificationForAll` | `PaymentCommandService#createNotificationForAll` |
| PATCH | `/notifications/{id}/read` | `PaymentCommandController#markAsRead` | `this#extractEmailFromSecurityContext`, `PaymentCommandService#markAsRead` |
| PATCH | `/notifications/read-all` | `PaymentCommandController#markAllAsRead` | `this#extractEmailFromSecurityContext`, `PaymentCommandService#markAllAsRead` |
| DELETE | `/notifications/{id}` | `PaymentCommandController#deleteNotification` | `this#extractEmailFromSecurityContext`, `PaymentCommandService#deleteNotification` |
| DELETE | `/notifications/user` | `PaymentCommandController#deleteAllUserNotifications` | `this#extractEmailFromSecurityContext`, `PaymentCommandService#deleteNotificationsByUser` |
| GET | `/notifications/filter/category` | `PaymentQueryController#filterByCategory` | `this#extractEmailFromSecurityContext`, `PaymentQueryService#filterByCategory` |
| GET | `/notifications/filter/type` | `PaymentQueryController#filterByType` | `this#extractEmailFromSecurityContext`, `PaymentQueryService#filterByType` |
| GET | `/notifications/filter/category/read-status` | `PaymentQueryController#filterByCategoryAndIsRead` | `this#validatePaginationParams`, `this#extractEmailFromSecurityContext`, `PaymentQueryService#filterByCategoryAndIsRead` |
| GET | `/notifications/filter/type/read-status` | `PaymentQueryController#filterByTypeAndIsRead` | `this#validatePaginationParams`, `this#extractEmailFromSecurityContext`, `PaymentQueryService#filterByTypeAndIsRead` |
| GET | `/notifications/count/unread` | `PaymentQueryController#countUnreadNotifications` | `this#extractEmailFromSecurityContext`, `PaymentQueryService#countUnreadNotifications` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `PaymentCommandService`
  - `createNotification` → ProductRepository#findById~, this#buildNotification, PaymentRepository#save~, this#sendRealTimeNotification, this#toDTO
  - `createNotificationForAll` → ProductRepository#findAll~, this#createNotificationsForUsers, PaymentRepository#saveAll~, this#broadcastNotificationsToConnectedUsers, this#toDTOList
  - `markAsRead` → PaymentRepository#findById~
  - `markAllAsRead` → PaymentRepository#findAllByUserEmail
  - `deleteNotification` → PaymentRepository#existsById~, PaymentRepository#deleteById~
  - `deleteNotificationsByUser` → PaymentRepository#findAllByUserEmail, PaymentRepository#deleteAll~
  - `notifyShipmentCompleted` → this#createNotification
  - `createNotificationsForUsers` → this#buildNotification
- `PaymentQueryService`
  - `filterByCategory` → PaymentRepository#findByUserEmailAndCategory
  - `filterByType` → PaymentRepository#findByUserEmailAndType
  - `filterByCategoryAndIsRead` → PaymentRepository#findByUserEmailAndCategoryAndIsRead
  - `filterByTypeAndIsRead` → PaymentRepository#findByUserEmailAndTypeAndIsRead
  - `countUnreadNotifications` → PaymentRepository#findByUserEmailAndIsRead
- `PaymentRepositoryCustomImpl`
  - `findByUserEmailAndCategory` → this#userEmailEq, this#categoryEq
  - `findByUserEmailAndType` → this#userEmailEq, this#typeEq
  - `findByUserEmailAndIsRead` → this#userEmailEq, this#isReadEq, this#getOrderSpecifiers
  - `findByUserEmailAndCategoryAndIsRead` → this#userEmailEq, this#categoryEq, this#isReadEq, this#getOrderSpecifiers
  - `findByUserEmailAndTypeAndIsRead` → this#userEmailEq, this#typeEq, this#isReadEq, this#getOrderSpecifiers
  - `findAllByUserEmail` → this#userEmailEq

## 3. 클래스 목록 (역할별)

### controller (2)

- `PaymentCommandController` controller/command/PaymentCommandController.java · public: createNotification, createNotificationForAll, markAsRead, markAllAsRead, deleteNotification, deleteAllUserNotifications, handlePing · 주입: PaymentCommandService
- `PaymentQueryController` controller/query/PaymentQueryController.java · public: filterByCategory, filterByType, filterByCategoryAndIsRead, filterByTypeAndIsRead, countUnreadNotifications · 주입: PaymentQueryService

### service-command (1)

- `PaymentCommandService` service/command/PaymentCommandService.java · public: createNotification, createNotificationForAll, markAsRead, markAllAsRead, deleteNotification, deleteNotificationsByUser, notifyShipmentCompleted, updateWebSocketConnectionTTL · 주입: PaymentRepository, ProductRepository, SimpMessagingTemplate, RedisTemplate

### service-query (1)

- `PaymentQueryService` service/query/PaymentQueryService.java · public: filterByCategory, filterByType, filterByCategoryAndIsRead, filterByTypeAndIsRead, countUnreadNotifications · 주입: PaymentRepository

### repository (1)

- `PaymentRepository` (interface) repository/PaymentRepository.java · extends/implements JpaRepository, PaymentRepositoryCustom

### custom (2)

- `PaymentRepositoryCustom` (interface) repository/PaymentRepositoryCustom.java · public: findByUserEmailAndCategory, findByUserEmailAndType, findByUserEmailAndIsRead, findByUserEmailAndCategoryAndIsRead, findByUserEmailAndTypeAndIsRead, findAllByUserEmail
- `PaymentRepositoryCustomImpl` repository/PaymentRepositoryCustomImpl.java · extends/implements PaymentRepositoryCustom · public: findByUserEmailAndCategory, findByUserEmailAndType, findByUserEmailAndIsRead, findByUserEmailAndCategoryAndIsRead, findByUserEmailAndTypeAndIsRead, findAllByUserEmail · 주입: JPAQueryFactory

### entity (3)

- `Payment` entity/Payment.java · extends/implements BaseTimeEntity · public: markAsRead, markAsUnread
- `PaymentCategory` (enum) entity/PaymentCategory.java · public: getDescription
- `PaymentType` (enum) entity/PaymentType.java · public: getCategory, getDescription, getTypesByCategory

### dto (2)

- `PaymentDTO` dto/PaymentDTO.java
- `PaymentRequestDTO` dto/PaymentRequestDTO.java

### exception (2)

- `PaymentErrorCode` exception/PaymentErrorCode.java
- `PaymentException` exception/PaymentException.java

### 다른 도메인 참조 (import·주입 기준)

- **faq**: `Faq` ← PaymentCommandService
- **product**: `Product` ← Payment, PaymentCommandService; `ProductRepository` ← PaymentCommandService

### 이 도메인을 참조하는 다른 도메인

- **address**: PaymentCategory, PaymentCommandService, PaymentType
- **global**: Payment, PaymentCategory, PaymentRepository, PaymentType
- **notification**: PaymentCategory, PaymentCommandService, PaymentType
- **product**: PaymentCategory, PaymentCommandService, PaymentType
- **style**: PaymentCategory, PaymentCommandService, PaymentType
- **warehouseStorage**: PaymentCategory, PaymentCommandService, PaymentType

## 4. JPA 엔티티 연관

- `Payment` @Entity · extends BaseTimeEntity
  - user: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) → `Product` [product]

## 5. 이벤트·메시징

(없음)

## 6. AOP

(없음)

