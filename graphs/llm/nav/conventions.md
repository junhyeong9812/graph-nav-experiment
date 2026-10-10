# 코드 컨벤션 (도메인 공통 관찰 결과)

> `D/` = `src/main/java/com/fream/back/domain/`. 샘플: notice, order, faq, product, user, address, style, event.

## 1. 패키지 레이아웃
도메인 19개: accessLog, address, chatQuestion, event, faq, inquiry, inspection, monitoring, notice, notification, order, payment, product, sale, shipment, style, user, warehouseStorage, weather.

표준형 (예: `D/notice/`, `D/faq/`, `D/order/`):
```
<domain>/
  controller/command/<X>CommandController.java   // POST/PUT/DELETE
  controller/query/<X>QueryController.java       // GET
  service/command/<X>CommandService.java
  service/query/<X>QueryService.java
  repository/<X>Repository.java                  // extends JpaRepository<..>, <X>RepositoryCustom
  repository/<X>RepositoryCustom.java
  repository/<X>RepositoryImpl.java              // JPAQueryFactory 주입 QueryDSL 구현
  dto/   entity/
  exception/<Domain>ErrorCode.java  <Domain>Exception.java  <세부>Exception.java
  (선택) aop/, aop/annotation/, service/kafka/, scheduler/, config/
```
- CQRS 분리(command/query) 는 대부분 도메인이 따르지만 예외 있음: `D/product/controller/BrandController.java`, `CategoryController.java`, `CollectionController.java` 처럼 하위 폴더 없는 컨트롤러, product 의 `service/brand`, `service/productColor` 등 하위 엔티티별 서비스, user 의 `controller/admin`, `service/admin`.
- QueryDSL Custom+Impl 예: `D/notice/repository/NoticeRepository{,Custom,Impl}.java`, `D/order/repository/OrderBidRepositoryImpl.java`, `D/faq/repository/FAQRepositoryImpl.java`, `D/sale/repository/SaleBidRepositoryImpl.java` (Custom 인터페이스 12개).
- 엔티티는 `global/entity/BaseTimeEntity` 상속(45개). `BaseEntity` 상속은 없음.

## 2. 예외 패턴
- `<Domain>ErrorCode` = `enum implements ErrorCode`, `@Getter @RequiredArgsConstructor`, `(code, message, int status)`. 예: `D/order/exception/OrderErrorCode.java` (`ORDER_NOT_FOUND("ORD001", "...", 404)`), inquiry 는 `HttpStatus.X.value()` 사용.
- `<Domain>Exception extends GlobalException` 생성자 4종 (errorCode / +message / +cause / +message+cause). 예: `D/order/exception/OrderException.java`.
- 세부 예외는 `<Domain>Exception` 상속 (예: `OrderNotFoundException`, `NoticePermissionException`).
- `global/exception/GlobalExceptionHandler.java` 에 도메인 기본 예외별 핸들러가 나열돼 있음 (GlobalException 핸들러도 있어 새 도메인은 추가 안 해도 처리됨). 응답 바디는 `ErrorResponse{code,message,status,timestamp,path}`, message 는 enum 기본 메시지.

## 3. 현재 사용자 얻기
- 인증 principal = 이메일 문자열 (쿠키 `ACCESS_TOKEN` → `JwtAuthenticationFilter`).
- 방식 A (공용 유틸): `SecurityUtils.extractEmailFromSecurityContext()` — domain 내 27개 파일에서 사용 (컨트롤러 및 AOP). 주문 도메인은 `SecurityUtils.extractAndValidateEmailForOrder("작업명")` / `...ForOrderBid(...)` (예: `D/order/controller/command/OrderCommandController.java`).
- 방식 B: 컨트롤러 private 메서드 `extractEmailFromSecurityContext()` 를 직접 구현 (21개 컨트롤러; 예: `D/notice/controller/command/NoticeCommandController.java`, product Brand/Category/Collection, user admin 컨트롤러들). 도메인 예외(`NoticePermissionException` 등)로 변환.
- 비로그인 허용 시 `SecurityUtils.extractEmailOrAnonymous()`. 나이/성별은 `SecurityUtils.extractUserInfo()`.

## 4. 관리자 체크
- URL 규칙/`@PreAuthorize("hasRole")` 가 아니라 **`UserQueryService.checkAdminRole(email)`** (`D/user/service/query/UserQueryService.java`; DB 에서 `Role.ADMIN` 확인, 아니면 `UserException(INSUFFICIENT_PERMISSIONS)` 또는 `UserNotFoundException`).
- 사용처 예: `D/notice/controller/command/NoticeCommandController.java` (`checkAdminPermission` 으로 감싸 `NoticePermissionException` 변환), `D/faq/controller/command/FAQCommandController.java`, `D/product/controller/command/ProductCommandController.java`, `D/user/controller/admin/AdminUserController.java`, `D/chatQuestion/aop/ChatSecurityAspect.java`.
- `@PreAuthorize` 사용은 `D/chatQuestion/controller/ChatController.java` (`isAuthenticated()`) 와 `global/controller/LogController.java` (`hasRole('ADMIN')`, 권한이 세팅되지 않아 실질적으로 불통) 뿐.

## 5. 응답 래핑
- 혼재: 컨트롤러 69개 중 15개가 `ResponseEntity<ResponseDto<T>>` (`ResponseDto.success(data[, msg])`, `ResponseDto.error(code, msg)`), 나머지는 `ResponseEntity<Dto>`/`ResponseEntity<Void>` 직접 반환.
  - ResponseDto 사용: order(전 컨트롤러), sale `SaleBidQueryController`, faq, inquiry, inspection, chatQuestion, product `BrandController`/`CollectionController`.
  - 직접 반환 예: `D/notice/controller/command/NoticeCommandController.java` (`ResponseEntity.ok(NoticeResponseDto)`).
- 해당 도메인 기존 스타일을 따를 것. 비동기 Kafka 처리 API 는 202 Accepted + ResponseDto (OrderCommandController).
- 페이징: `Pageable` 파라미터는 1-based(`global/config/WebConfig.java`), 응답은 `Page<T>` 또는 `commonDto.PageDto` (`PageUtils.toPageDto`).

## 6. 트랜잭션
- 쿼리 서비스: 클래스 레벨 `@Transactional(readOnly = true)` (예: `D/notice/service/query/NoticeQueryService.java`, `D/order/service/query/OrderBidQueryService.java`). 커맨드 서비스: 클래스 또는 메서드 레벨 `@Transactional` (예: `D/notice/service/command/NoticeCommandService.java` 클래스 레벨). 컨트롤러에 `@Transactional` 은 `D/user/controller/command/OAuthController.java` 정도로 예외적.

## 7. 로깅
- Lombok `@Slf4j` 거의 전 클래스. 메시지는 한국어, `key={}` 형식 (`log.info("공지사항 생성 API 완료: id={}, title={}", ...)`). 컨트롤러 진입 debug/info, 예외 시 warn(권한)/error(시스템).
- 특정 패키지 로그 파일 분리는 `src/main/resources/logback.xml` (security, FileUtils, websocket, exception).

## 8. 파일 업로드
- `global/utils/FileUtils.saveFile(directory, prefix, MultipartFile)` → `/home/ubuntu/fream/{directory}/{prefix}{UUID}{ext}` 저장, 파일명만 반환 → 엔티티에 파일명 저장. image/* · video/* 만 허용.
  - directory 관례: `"product/{id}"`, `"styles/{id}"`, `notice/...` 등 (예: `D/event/service/command/EventCommandService.java` `fileUtils.saveFile(directory, "thumbnail_" + id + "_", file)`, `D/notice/service/command/NoticeCommandService.java` prefix `"notice_"`).
- 도메인 전용 저장 유틸도 있음: `D/notice/service/command/NoticeFileStorageUtil.java` (`/home/ubuntu/fream/notice`), `D/faq/service/command/FAQFileStorageUtil.java`, `D/inspection/service/command/InspectionFileStorageUtil.java`.
- 컨트롤러는 `@ModelAttribute` DTO(MultipartFile 포함)로 multipart 수신 (NoticeCommandController). 최대 1GB (`application.yml`).
- 파일 서빙은 각 Query 컨트롤러에서 직접 경로 조립 (예: `D/product/controller/query/ProductQueryController.java`, `D/event/controller/query/EventQueryController.java`).
- 상품/스타일 데이터 변경 후 nginx 캐시 무효화는 `NginxCachePurgeUtil.purgeProductCache()/purgeStyleCache()/purgeEsCache()`.

## 9. 기타 관찰
- 생성자 주입: `@RequiredArgsConstructor` + `private final`.
- 컨트롤러 경로에 `/api` 를 붙이지 않음 (nginx 가 제거). 예: `@RequestMapping("/notices")`, `@RequestMapping("/orders")`. (예외: `global/controller/LogController` 의 `/api/logs`)
- 도메인별 AOP 다수 (`D/faq/aop/*`, `D/address/aop/*`, `D/event/aop/*`, `D/chatQuestion/aop/*`, `D/accessLog/config/AccessLogAopConfiguration.java`) — 커스텀 어노테이션 + Aspect 쌍.
- Kafka: 토픽명은 Producer/Consumer 클래스에 `private static final String` 또는 `@KafkaListener(topics=...)` 로 하드코딩, 이벤트 DTO 는 `<domain>/dto/kafka/`, 설정은 `global/config/kafka/` 또는 `<domain>/service/kafka/config/`.

## 10. 새 기능 추가 체크리스트
1. 위치 결정: 기존 도메인 `D/<domain>/` 의 command/query 구분 폴더에 컨트롤러·서비스 추가 (새 도메인이면 §1 레이아웃 복제).
2. URL 은 `/api` 없이 매핑. 비로그인 접근이 필요하면 `global/config/security/SecurityConfig.java` `whiteListPaths()` 에 추가 (그 외는 전부 authenticated).
3. 현재 사용자: `SecurityUtils.extractEmailFromSecurityContext()` (또는 해당 도메인의 기존 private 헬퍼). 관리자 기능이면 `userQueryService.checkAdminRole(email)` 호출 — `@PreAuthorize("hasRole")` 는 동작하지 않음.
4. 에러: `<Domain>ErrorCode` enum 에 코드 추가(도메인 접두 유지, 다른 도메인과 중복 피하기) → `<Domain>Exception` 또는 하위 예외 throw. 새 도메인이면 `<Domain>Exception extends GlobalException` 생성 (핸들러 추가는 선택).
5. 응답: 해당 도메인 기존 스타일(ResponseDto vs 직접 DTO) 따르기.
6. 서비스: query 는 `@Transactional(readOnly = true)`, command 는 `@Transactional`. 복잡 조회는 `<X>RepositoryCustom` + `<X>RepositoryImpl`(JPAQueryFactory).
7. 엔티티: `BaseTimeEntity` 상속 (createdDate/modifiedDate 자동). prod 는 `ddl-auto: create` 라 스키마 마이그레이션 도구 없음.
8. 파일: `FileUtils.saveFile(dir, prefix, file)` 사용, 반환 파일명 저장. 캐시되는 상품/스타일 조회에 영향이 있으면 `NginxCachePurgeUtil` 호출.
9. 캐시(`@Cacheable`) 사용 시 `global/config/CacheConfig.java` 의 캐시명 목록에 추가 (`ConcurrentMapCacheManager` 에 이름 고정).
10. Kafka 사용 시 global 또는 도메인 kafka config 에 Producer/Consumer/ListenerContainerFactory 빈 추가, 토픽/그룹 상수 정의.
11. 시드 데이터가 필요하면 `global/config/DataInitializer.java` (모든 프로필에서 실행됨에 유의).
12. 테스트는 사실상 없음(`contextLoads` 1개, prod 프로필 기본) — 테스트 추가 시 `@ActiveProfiles("test")` 및 외부 의존 설정 보완 필요.
