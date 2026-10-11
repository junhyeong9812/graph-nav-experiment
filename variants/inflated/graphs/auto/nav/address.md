# address 도메인

기준 경로: `src/main/java/com/fream/back/domain/address/` · 파일 25개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/addresses` | `AddressCommandController#createAddress` | `AddressCommandService#createAddress` |
| PUT | `/addresses` | `AddressCommandController#updateAddress` | `AddressCommandService#updateAddress` |
| DELETE | `/addresses/{addressId}` | `AddressCommandController#deleteAddress` | `AddressCommandService#deleteAddress` |
| GET | `/addresses` | `AddressQueryController#getAddresses` | `AddressQueryService#getAddresses` |
| GET | `/addresses/{addressId}` | `AddressQueryController#getAddress` | `AddressQueryService#getAddress` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `AddressAuditAspect`
  - `auditAnnotatedMethod` → this#extractUserEmailSafely, this#generateAuditId, this#getCurrentRequest, this#extractIpAddress, this#extractUserAgent, this#extractHeaders, this#logAuditStart, this#logAuditSuccess, this#logChangeTracking, this#sendToExternalAuditSystem, this#sendRealTimeAlert, this#logAuditFailure
  - `auditAddressCreation` → this#extractUserEmailSafely, this#logBusinessAudit
  - `auditAddressUpdate` → this#extractUserEmailSafely, this#logBusinessAudit
  - `auditAddressDeletion` → this#extractUserEmailSafely, this#logBusinessAudit
  - `auditAddressAccess` → this#extractUserEmailSafely, this#logBusinessAudit
  - `auditSecurityViolation` → this#isSecurityViolation, this#extractUserEmailSafely, this#logSecurityAudit
  - `logAuditStart` → this#formatBusinessContext, this#logWithAuditLevel
  - `logAuditSuccess` → this#logWithAuditLevel
  - `logBusinessAudit` → this#generateAuditId, this#getCurrentRequest, this#extractIpAddress, this#formatBusinessContext, this#logWithAuditLevel
  - `logSecurityAudit` → this#generateAuditId, this#getCurrentRequest, this#extractIpAddress, this#extractUserAgent, this#maskUserAgent
  - `logChangeTracking` → this#generateAuditId, this#formatBusinessContext
  - `formatBusinessContext` → this#formatSensitiveString
  - `formatSensitiveString` → this#maskEmail, this#maskPhoneNumber, this#maskZipCode
  - `extractHeaders` → this#isSensitiveHeader
- `AddressCommandService`
  - `createAddress` → UserRepository#findByEmail, PersonalDataEncryptionUtil#deterministicEncrypt, PersonalDataEncryptionUtil#encrypt, AddressRepository#save~
  - `updateAddress` → UserRepository#findByEmail, AddressRepository#findByIdAndUser, PersonalDataEncryptionUtil#deterministicEncrypt, PersonalDataEncryptionUtil#encrypt
  - `deleteAddress` → UserRepository#findByEmail, AddressRepository#findByIdAndUser, AddressRepository#delete~
- `AddressExceptionAspect`
  - `handleAnnotatedException` → this#extractUserEmailSafely, this#shouldHandleException, this#executeExceptionStrategy
  - `handleServiceException` → this#extractUserEmailSafely, this#handleAddressException, this#handleGenericException
  - `handleControllerException` → this#extractUserEmailSafely, this#logControllerExceptionMetrics
  - `handleRepositoryException` → this#extractUserEmailSafely, this#logDatabaseExceptionMetrics
  - `executeExceptionStrategy` → this#logExceptionDetails, this#getDefaultReturnValue, this#executeFallbackLogic
  - `logExceptionDetails` → this#formatArgs, this#sendAlert
  - `handleAddressException` → this#formatArgs
  - `handleGenericException` → this#formatArgs
  - `executeFallbackLogic` → this#getDefaultReturnValue
  - `formatArgs` → this#maskEmail, this#maskPhoneNumber
- `AddressLoggingAspect`
  - `logAnnotatedMethod` → this#extractUserEmailSafely, this#generateRequestId, this#logMethodStart, this#logMethodSuccess, this#logMethodException
  - `logControllerMethods` → this#extractUserEmailSafely, this#generateRequestId, this#formatArgsForLogging
  - `logServiceMethods` → this#extractUserEmailSafely, this#formatArgsForLogging, this#isImportantServiceMethod
  - `logRepositoryMethods` → this#extractUserEmailSafely, this#formatArgsForLogging
  - `logAddressCreation` → this#extractUserEmailSafely, this#formatArgsForLogging
  - `logAddressUpdate` → this#extractUserEmailSafely, this#formatArgsForLogging
  - `logAddressDeletion` → this#extractUserEmailSafely, this#formatArgsForLogging
  - `logMethodStart` → this#formatArgsForAnnotation, this#logWithLevel
  - `logMethodSuccess` → this#formatResultForLogging, this#logWithLevel
  - `formatArgsForAnnotation` → this#formatSensitiveArg
  - `formatSensitiveArg` → this#maskEmail, this#maskPhoneNumber, this#maskZipCode
  - `formatArgsForLogging` → this#maskEmail, this#maskPhoneNumber, this#maskZipCode
- `AddressPerformanceAspect`
  - `monitorAnnotatedPerformance` → this#extractUserEmailSafely, this#getDefaultWarningThreshold, this#getDefaultErrorThreshold, this#executePerformanceMonitoring
  - `monitorControllerPerformance` → this#monitorMethodPerformance
  - `monitorServicePerformance` → this#monitorMethodPerformance
  - `monitorRepositoryPerformance` → this#monitorMethodPerformance
  - `executePerformanceMonitoring` → this#updatePerformanceStats, this#logAdvancedPerformanceMetrics, this#shouldGeneratePerformanceReport, this#generatePerformanceReport
  - `monitorMethodPerformance` → this#extractUserEmailSafely, this#updatePerformanceStats, this#logPerformanceMetrics, this#shouldGeneratePerformanceReport, this#generatePerformanceReport
- `AddressQueryService`
  - `getAddresses` → PersonalDataEncryptionUtil#deterministicEncrypt, UserRepository#findByEmail, PersonalDataEncryptionUtil#deterministicDecrypt, PersonalDataEncryptionUtil#decrypt
  - `getAddress` → PersonalDataEncryptionUtil#deterministicEncrypt, UserRepository#findByEmail, AddressRepository#findByIdAndUser, PersonalDataEncryptionUtil#deterministicDecrypt, PersonalDataEncryptionUtil#decrypt
  - `searchAddressesByRecipientName` → PersonalDataEncryptionUtil#deterministicEncrypt, UserRepository#findByEmail, PersonalDataEncryptionUtil#encryptForSearch, AddressRepository#findByUserAndRecipientName, PersonalDataEncryptionUtil#deterministicDecrypt, PersonalDataEncryptionUtil#decrypt
  - `searchAddressesByPhoneNumber` → PersonalDataEncryptionUtil#deterministicEncrypt, UserRepository#findByEmail, PersonalDataEncryptionUtil#encryptForSearch, AddressRepository#findByUserAndPhoneNumber, PersonalDataEncryptionUtil#deterministicDecrypt, PersonalDataEncryptionUtil#decrypt
- `AddressSecurityAspect`
  - `secureAnnotatedMethod` → this#extractUserEmailSafely, this#getCurrentRequest, this#performSecurityChecks, this#logSecuritySuccess, this#handleSecurityViolation
  - `performSecurityChecks` → this#checkAuthentication, this#checkAuthorization, this#checkOwnership, this#checkIpRestriction, this#checkTimeRestriction, this#checkRateLimit, this#logEncryptedDataAccess, this#logAuditTrail, this#logPersonalDataAccess
  - `checkAuthorization` → this#checkUserRoles
  - `checkOwnership` → this#maskEmail
  - `checkIpRestriction` → this#extractIpAddress
  - `logEncryptedDataAccess` → this#formatArgsForLogging
  - `logAuditTrail` → this#extractIpAddress, this#maskUserAgent
  - `logPersonalDataAccess` → this#extractIpAddress
  - `handleSecurityViolation` → this#extractIpAddress, this#formatArgsForLogging, this#logDetailedSecurityViolation
  - `logDetailedSecurityViolation` → this#maskUserAgent, this#formatArgsForLogging
  - `logSecuritySuccess` → this#extractIpAddress
  - `formatArgsForLogging` → this#maskEmail

## 3. 클래스 목록 (역할별)

### controller (2)

- `AddressCommandController` controller/command/AddressCommandController.java · public: createAddress, updateAddress, deleteAddress · 주입: AddressCommandService
- `AddressQueryController` controller/query/AddressQueryController.java · public: getAddresses, getAddress · 주입: AddressQueryService

### service-command (1)

- `AddressCommandService` service/command/AddressCommandService.java · public: createAddress, updateAddress, deleteAddress · 주입: UserRepository, AddressRepository, PersonalDataEncryptionUtil

### service-query (1)

- `AddressQueryService` service/query/AddressQueryService.java · public: getAddresses, getAddress, searchAddressesByRecipientName, searchAddressesByPhoneNumber · 주입: UserRepository, AddressRepository, PersonalDataEncryptionUtil

### repository (1)

- `AddressRepository` (interface) repository/AddressRepository.java · extends/implements JpaRepository · public: findByUser, findByIdAndUser, findByUserAndIsDefaultTrue, findByUserAndRecipientName, findByUserAndPhoneNumber, findByUserAndAddress, findByUserAndZipCode, findByUserAndMultipleFields, countByUser, existsByUserAndIsDefaultTrue, findByUserAndIsDefaultFalse

### entity (1)

- `Address` entity/Address.java · extends/implements BaseTimeEntity · public: updateAddress, assignUser, unassignUser

### aop (5)

- `AddressAuditAspect` aop/AddressAuditAspect.java · public: auditAnnotatedMethod, auditAddressCreation, auditAddressUpdate, auditAddressDeletion, auditAddressAccess, auditSecurityViolation
- `AddressExceptionAspect` aop/AddressExceptionAspect.java · public: handleAnnotatedException, handleServiceException, handleControllerException, handleRepositoryException
- `AddressLoggingAspect` aop/AddressLoggingAspect.java · public: logAnnotatedMethod, logControllerMethods, logServiceMethods, logRepositoryMethods, logAddressCreation, logAddressUpdate, logAddressDeletion
- `AddressPerformanceAspect` aop/AddressPerformanceAspect.java · public: monitorAnnotatedPerformance, monitorControllerPerformance, monitorServicePerformance, monitorRepositoryPerformance
- `AddressSecurityAspect` aop/AddressSecurityAspect.java · public: secureAnnotatedMethod

### dto (4)

- `AddressCreateDto` dto/AddressCreateDto.java
- `AddressListResponseDto` dto/AddressListResponseDto.java
- `AddressResponseDto` dto/AddressResponseDto.java
- `AddressUpdateDto` dto/AddressUpdateDto.java

### exception (5)

- `AddressAccessDeniedException` exception/AddressAccessDeniedException.java
- `AddressErrorCode` exception/AddressErrorCode.java
- `AddressException` exception/AddressException.java
- `AddressNotFoundException` exception/AddressNotFoundException.java
- `AddressUserNotFoundException` exception/AddressUserNotFoundException.java

### other (5)

- `AddressAudit` (@interface) aop/annotation/AddressAudit.java
- `AddressExceptionHandling` (@interface) aop/annotation/AddressExceptionHandling.java
- `AddressLogging` (@interface) aop/annotation/AddressLogging.java
- `AddressPerformance` (@interface) aop/annotation/AddressPerformance.java
- `AddressSecurity` (@interface) aop/annotation/AddressSecurity.java

### 다른 도메인 참조 (import·주입 기준)

- **user**: `User` ← Address, AddressCommandService, AddressQueryService, AddressRepository; `UserRepository` ← AddressCommandService, AddressQueryService

### 이 도메인을 참조하는 다른 도메인

- **adminaddress**: Address, AddressRepository
- **global**: Address, AddressRepository
- **internaladdress**: Address, AddressRepository
- **legacyaddress**: Address, AddressRepository
- **order**: AddressQueryService
- **partneraddress**: Address, AddressRepository
- **settlementaddress**: Address, AddressRepository
- **user**: Address

## 4. JPA 엔티티 연관

- `Address` @Entity @Table(indexes = { @Index(name = "idx_user_id", columnList = "user_id"), @Index(name = "idx_is_default", columnList = "is_default") }) · extends BaseTimeEntity
  - user: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") → `User` [user]

다른 도메인 엔티티에서 들어오는 연관:

- `User.addresses` @OneToMany → `Address` [user]

## 5. 이벤트·메시징

(없음)

## 6. AOP

- `AddressAuditAspect#auditAnnotatedMethod` @Around("@annotation(addressAudit)") · 바인딩: `@AddressAudit`
- `AddressAuditAspect#auditAddressCreation` @AfterReturning(pointcut = "execution(* com.fream.back.domain.address.service.command.AddressCommandService.createAddress(..))", returning = "result")
- `AddressAuditAspect#auditAddressUpdate` @AfterReturning(pointcut = "execution(* com.fream.back.domain.address.service.command.AddressCommandService.updateAddress(..))", returning = "result")
- `AddressAuditAspect#auditAddressDeletion` @AfterReturning(pointcut = "execution(* com.fream.back.domain.address.service.command.AddressCommandService.deleteAddress(..))", returning = "result")
- `AddressAuditAspect#auditAddressAccess` @AfterReturning(pointcut = "execution(* com.fream.back.domain.address.service.query.AddressQueryService.*(..))", returning = "result")
- `AddressAuditAspect#auditSecurityViolation` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.address..*(..))", throwing = "exception")
- `AddressExceptionAspect#handleAnnotatedException` @Around("@annotation(exceptionHandling)") · 바인딩: `@AddressExceptionHandling`
- `AddressExceptionAspect#handleServiceException` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.address.service..*(..))", throwing = "exception")
- `AddressExceptionAspect#handleControllerException` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.address.controller..*(..))", throwing = "exception")
- `AddressExceptionAspect#handleRepositoryException` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.address.repository..*(..))", throwing = "exception")
- `AddressLoggingAspect#logAnnotatedMethod` @Around("@annotation(addressLogging)") · 바인딩: `@AddressLogging`
- `AddressLoggingAspect#logControllerMethods` @Around("execution(* com.fream.back.domain.address.controller..*(..))")
- `AddressLoggingAspect#logServiceMethods` @Around("execution(* com.fream.back.domain.address.service..*(..))")
- `AddressLoggingAspect#logRepositoryMethods` @Around("execution(* com.fream.back.domain.address.repository..*(..))")
- `AddressLoggingAspect#logAddressCreation` @AfterReturning(pointcut = "execution(* com.fream.back.domain.address.service.command.AddressCommandService.createAddress(..))", returning = "result")
- `AddressLoggingAspect#logAddressUpdate` @AfterReturning(pointcut = "execution(* com.fream.back.domain.address.service.command.AddressCommandService.updateAddress(..))", returning = "result")
- `AddressLoggingAspect#logAddressDeletion` @AfterReturning(pointcut = "execution(* com.fream.back.domain.address.service.command.AddressCommandService.deleteAddress(..))", returning = "result")
- `AddressPerformanceAspect#monitorAnnotatedPerformance` @Around("@annotation(addressPerformance)") · 바인딩: `@AddressPerformance`
- `AddressPerformanceAspect#monitorControllerPerformance` @Around("execution(* com.fream.back.domain.address.controller..*(..))")
- `AddressPerformanceAspect#monitorServicePerformance` @Around("execution(* com.fream.back.domain.address.service..*(..))")
- `AddressPerformanceAspect#monitorRepositoryPerformance` @Around("execution(* com.fream.back.domain.address.repository..*(..))")
- `AddressSecurityAspect#secureAnnotatedMethod` @Around("@annotation(addressSecurity)") · 바인딩: `@AddressSecurity`

어노테이션 적용 지점(메서드에 붙은 곳):

- `@AddressAudit`: (없음)
- `@AddressExceptionHandling`: (없음)
- `@AddressLogging`: (없음)
- `@AddressPerformance`: (없음)
- `@AddressSecurity`: (없음)

