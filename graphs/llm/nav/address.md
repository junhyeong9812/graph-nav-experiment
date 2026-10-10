# address 도메인 (배송지 주소록)

## 1. 개요
- 로그인 사용자의 배송지(수령인, 연락처, 우편번호, 주소, 상세주소, 기본배송지 여부) CRUD.
- 개인정보는 `PersonalDataEncryptionUtil`로 암호화 저장: 수령인/전화/우편번호/주소 = 결정적 암호화(`deterministicEncrypt`, 검색 가능), 상세주소 = 양방향 암호화(`encrypt`).
- CQRS 형태로 command/query 컨트롤러·서비스 분리. 도메인 AOP 5종(로깅/성능/감사/예외/보안) 존재.

## 2. 패키지/파일 구조
루트: `src/main/java/com/fream/back/domain/address/`
| 경로 | 역할 |
|---|---|
| `controller/command/AddressCommandController.java` | POST/PUT/DELETE `/addresses` |
| `controller/query/AddressQueryController.java` | GET `/addresses`, `/addresses/{id}` |
| `service/command/AddressCommandService.java` | 생성/수정/삭제 + 암호화 |
| `service/query/AddressQueryService.java` | 목록/단건/검색 + 복호화 |
| `entity/Address.java` | 엔티티 |
| `repository/AddressRepository.java` | JPA + `@Query` |
| `dto/AddressCreateDto.java`, `AddressUpdateDto.java`, `AddressResponseDto.java`, `AddressListResponseDto.java` | DTO |
| `aop/Address{Logging,Performance,Audit,Exception,Security}Aspect.java` | 어스펙트 |
| `aop/annotation/Address{Logging,Performance,Audit,ExceptionHandling,Security}.java` | 커스텀 어노테이션 |
| `exception/AddressErrorCode.java`, `AddressException.java`, `AddressNotFoundException.java`, `AddressUserNotFoundException.java`, `AddressAccessDeniedException.java` | 예외 |

관련 global: `src/main/java/com/fream/back/global/utils/PersonalDataEncryptionUtil.java`, `src/main/java/com/fream/back/global/utils/SecurityUtils.java`

## 3. 엔티티
`Address extends BaseTimeEntity` (인덱스 `idx_user_id(user_id)`, `idx_is_default(is_default)`)
- `id` IDENTITY
- `user` : `@ManyToOne(LAZY) @JoinColumn(user_id)` → `domain/user/entity/User`
  - 역방향: `User.addresses` `@OneToMany(mappedBy="user", cascade=ALL, orphanRemoval=true)` (`User.java` L76-78), `User#addAddress/removeAddress`
- `recipientName`, `phoneNumber`, `zipCode`, `address` (nullable=false, 결정적 암호문 저장), `detailedAddress` (양방향 암호문), `isDefault` (`is_default`)
- 메서드: `updateAddress(...)` (null 아닌 값만 갱신), `assignUser`, `unassignUser`

DTO 검증: `AddressCreateDto` — phoneNumber `\d{10,11}`, zipCode `\d{5}`, recipientName/phone/zip/address `@NotBlank`. `AddressUpdateDto` — `addressId @NotNull`, phone/zip은 빈문자 허용 패턴.

## 4. API 엔드포인트
모두 SecurityConfig whitelist 밖 → 인증 필요. 사용자 식별은 `SecurityUtils.extractEmailFromSecurityContext()`. 관리자 전용 없음.

| Method | Path | Controller#method | Service#method |
|---|---|---|---|
| POST | `/addresses` | `AddressCommandController#createAddress` | `AddressCommandService#createAddress(email, dto)` |
| PUT | `/addresses` | `AddressCommandController#updateAddress` (body에 `addressId`) | `AddressCommandService#updateAddress(email, dto)` |
| DELETE | `/addresses/{addressId}` | `AddressCommandController#deleteAddress` | `AddressCommandService#deleteAddress(email, id)` |
| GET | `/addresses` | `AddressQueryController#getAddresses` | `AddressQueryService#getAddresses(email)` → `AddressListResponseDto` |
| GET | `/addresses/{addressId}` | `AddressQueryController#getAddress` | `AddressQueryService#getAddress(email, id)` |

Command 응답은 문자열 메시지(`"주소록 생성이 완료되었습니다."` 등).

## 5. 서비스 계층
- `AddressCommandService`
  - `createAddress`: `userRepository.findByEmail(email)`(평문) → `isDefault=true`면 `user.getAddresses()` 전부 `isDefault=false` → 필드 암호화 → save
  - `updateAddress`: `findByIdAndUser`로 소유 검증 → 기본배송지 처리 → 변경 필드만 암호화 후 `updateAddress`
  - `deleteAddress`: `findByIdAndUser` → `addressRepository.delete`
  - 일반 예외는 `AddressException(ADDRESS_CREATE/UPDATE/DELETE_ERROR)`로 래핑
- `AddressQueryService` (`@Transactional(readOnly=true)`)
  - `getAddresses`, `getAddress`, `searchAddressesByRecipientName`, `searchAddressesByPhoneNumber` (검색 2종은 컨트롤러 미노출)
  - 모든 메서드가 **`encryptionUtil.deterministicEncrypt(email)` 후 `findByEmail(encryptedEmail)`** (L38, L75, L120, L165)
  - 응답 시 `deterministicDecrypt` / `decrypt`로 복호화

## 6. Repository
`AddressRepository extends JpaRepository<Address, Long>`
- 파생: `findByUser`, `findByIdAndUser`, `findByUserAndIsDefaultTrue`, `countByUser`, `existsByUserAndIsDefaultTrue`, `findByUserAndIsDefaultFalse`
- `@Query`(JPQL, 암호문 일치 검색): `findByUserAndRecipientName`, `findByUserAndPhoneNumber`, `findByUserAndAddress`, `findByUserAndZipCode`, `findByUserAndMultipleFields`(각 조건 `IS NULL OR =`)
- QueryDSL Custom 없음.

## 7. 외부 연동 / 비동기 / AOP
- Kafka/스케줄러/외부 API 없음.
- AOP (모두 `@Aspect @Component`, `@Order` 없음):
  | Aspect | 어노테이션 기반 advice | 패키지 기반 advice (어노테이션 없이도 항상 동작) |
  |---|---|---|
  | `AddressLoggingAspect` | `@Around("@annotation(addressLogging)")` | `@Around` controller/service/repository 전체 + `@AfterReturning` Command 3메서드 |
  | `AddressPerformanceAspect` | `@Around("@annotation(addressPerformance)")` | `@Around` controller/service/repository 전체 |
  | `AddressAuditAspect` | `@Around("@annotation(addressAudit)")` | `@AfterReturning` create/update/delete, `AddressQueryService.*`; `@AfterThrowing` `domain.address..*` |
  | `AddressExceptionAspect` | `@Around("@annotation(exceptionHandling)")` | `@AfterThrowing` service/controller/repository |
  | `AddressSecurityAspect` | `@Around("@annotation(addressSecurity)")` | 없음 |
- 커스텀 어노테이션(`@AddressLogging`, `@AddressPerformance`, `@AddressAudit`, `@AddressExceptionHandling`, `@AddressSecurity`, `@Target({METHOD, TYPE})`)은 **코드 어디에도 적용되어 있지 않음** (aop 패키지 외 사용처 0). 실제 동작하는 건 execution 포인트컷 advice뿐. `AddressSecurityAspect`는 사실상 미동작.
  - 속성 예: `@AddressSecurity(requiredRoles, ownerOnly, ownerParamIndex, checks, violationAction, checkIpRestriction, enableRateLimit…)`, `@AddressAudit(level, containsSensitiveData, trackChanges, retentionDays…)`, `@AddressExceptionHandling(strategy, retryable, maxRetries, retryDelay…)`, `@AddressPerformance(warningThreshold, errorThreshold, monitorMemory…)`, `@AddressLogging(level, types, maskSensitiveData…)`.

## 8. 다른 도메인과의 의존
- address → user: `User` 엔티티, `UserRepository#findByEmail`.
- address → global: `PersonalDataEncryptionUtil`, `SecurityUtils`, `BaseTimeEntity`.
- order → address: `src/main/java/com/fream/back/domain/order/service/command/OrderCommandService.java` (L45 주입, L238) `addressQueryService.getAddress(buyer.getEmail(), addressId)`로 즉시구매 배송지 조회 후 수령인/전화/우편번호/주소 사용.
- `src/main/java/com/fream/back/global/config/DataInitializer.java` (L114-116, L562~) 초기 주소 데이터를 동일 암호화 방식으로 생성.

## 9. 예외
`AddressErrorCode`, 접두사 `ADR`:
- `ADR001` ADDRESS_NOT_FOUND(404), `ADR002` ADDRESS_QUERY_ERROR(500), `ADR003` ADDRESS_CREATE_ERROR(500), `ADR004` ADDRESS_UPDATE_ERROR(500), `ADR005` ADDRESS_DELETE_ERROR(500)
- `ADR101` ADDRESS_USER_NOT_FOUND(404), `ADR201` ADDRESS_ACCESS_DENIED(403)
- `ADR301` ADDRESS_INVALID_DATA(400), `ADR302` ADDRESS_INVALID_ZIP_CODE(400), `ADR303` ADDRESS_INVALID_PHONE_NUMBER(400)
- `AddressException extends GlobalException`; 하위 `AddressNotFoundException`, `AddressUserNotFoundException`, `AddressAccessDeniedException`. `GlobalExceptionHandler`에서 처리.

## 10. 작업 팁 / 주의점
- **이메일 조회 불일치**: Command 서비스는 평문 email로 `findByEmail`(L32/81/137), Query 서비스는 email을 결정적 암호화 후 `findByEmail`(L38/75/120/165). user 도메인에는 `PersonalDataEncryptionUtil` 사용처가 없고 `DataInitializer`도 email을 평문으로 저장(L176) → Query 경로(및 이를 호출하는 OrderCommandService 즉시구매)는 사용자 조회에 실패할 수 있음. 수정 시 양쪽 일관성 확인 필요.
- `deleteAddress`는 `addressRepository.delete`만 호출하고 `user.getAddresses()` 컬렉션에서 제거하지 않음(`User#removeAddress` 미사용). 같은 영속성 컨텍스트에서 컬렉션이 로딩된 상태면 orphanRemoval/cascade와 충돌 가능성에 유의.
- `createAddress`는 `Address.builder().user(user)`로만 연관 설정, `user.addAddress()` 미호출.
- 결정적 암호화 필드는 검색용 `@Query`가 암호문 완전일치로만 동작(부분검색 불가). `encryptForSearch`는 `deterministicEncrypt`의 별칭.
- 컨트롤러들이 모든 예외를 catch → 로그 → rethrow 하는 패턴(동작상 의미는 로깅뿐).
