# shipment 도메인

기준 경로: `src/main/java/com/fream/back/domain/shipment/` · 파일 25개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/addresses` | `ShipmentCommandController#createAddress` | `ShipmentCommandService#createAddress` |
| PUT | `/addresses` | `ShipmentCommandController#updateAddress` | `ShipmentCommandService#updateAddress` |
| DELETE | `/addresses/{addressId}` | `ShipmentCommandController#deleteAddress` | `ShipmentCommandService#deleteAddress` |
| GET | `/addresses` | `ShipmentQueryController#getAddresses` | `ShipmentQueryService#getAddresses` |
| GET | `/addresses/{addressId}` | `ShipmentQueryController#getAddress` | `ShipmentQueryService#getAddress` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `ShipmentAuditAspect`
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
- `ShipmentCommandService`
  - `createAddress` → ProductRepository#findByEmail, PersonalDataEncryptionUtil#deterministicEncrypt, PersonalDataEncryptionUtil#encrypt, ShipmentRepository#save~
  - `updateAddress` → ProductRepository#findByEmail, ShipmentRepository#findByIdAndUser, PersonalDataEncryptionUtil#deterministicEncrypt, PersonalDataEncryptionUtil#encrypt
  - `deleteAddress` → ProductRepository#findByEmail, ShipmentRepository#findByIdAndUser, ShipmentRepository#delete~
- `ShipmentExceptionAspect`
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
- `ShipmentLoggingAspect`
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
- `ShipmentPerformanceAspect`
  - `monitorAnnotatedPerformance` → this#extractUserEmailSafely, this#getDefaultWarningThreshold, this#getDefaultErrorThreshold, this#executePerformanceMonitoring
  - `monitorControllerPerformance` → this#monitorMethodPerformance
  - `monitorServicePerformance` → this#monitorMethodPerformance
  - `monitorRepositoryPerformance` → this#monitorMethodPerformance
  - `executePerformanceMonitoring` → this#updatePerformanceStats, this#logAdvancedPerformanceMetrics, this#shouldGeneratePerformanceReport, this#generatePerformanceReport
  - `monitorMethodPerformance` → this#extractUserEmailSafely, this#updatePerformanceStats, this#logPerformanceMetrics, this#shouldGeneratePerformanceReport, this#generatePerformanceReport
- `ShipmentQueryService`
  - `getAddresses` → PersonalDataEncryptionUtil#deterministicEncrypt, ProductRepository#findByEmail, PersonalDataEncryptionUtil#deterministicDecrypt, PersonalDataEncryptionUtil#decrypt
  - `getAddress` → PersonalDataEncryptionUtil#deterministicEncrypt, ProductRepository#findByEmail, ShipmentRepository#findByIdAndUser, PersonalDataEncryptionUtil#deterministicDecrypt, PersonalDataEncryptionUtil#decrypt
  - `searchAddressesByRecipientName` → PersonalDataEncryptionUtil#deterministicEncrypt, ProductRepository#findByEmail, PersonalDataEncryptionUtil#encryptForSearch, ShipmentRepository#findByUserAndRecipientName, PersonalDataEncryptionUtil#deterministicDecrypt, PersonalDataEncryptionUtil#decrypt
  - `searchAddressesByPhoneNumber` → PersonalDataEncryptionUtil#deterministicEncrypt, ProductRepository#findByEmail, PersonalDataEncryptionUtil#encryptForSearch, ShipmentRepository#findByUserAndPhoneNumber, PersonalDataEncryptionUtil#deterministicDecrypt, PersonalDataEncryptionUtil#decrypt
- `ShipmentSecurityAspect`
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

- `ShipmentCommandController` controller/command/ShipmentCommandController.java · public: createAddress, updateAddress, deleteAddress · 주입: ShipmentCommandService
- `ShipmentQueryController` controller/query/ShipmentQueryController.java · public: getAddresses, getAddress · 주입: ShipmentQueryService

### service-command (1)

- `ShipmentCommandService` service/command/ShipmentCommandService.java · public: createAddress, updateAddress, deleteAddress · 주입: ProductRepository, ShipmentRepository, PersonalDataEncryptionUtil

### service-query (1)

- `ShipmentQueryService` service/query/ShipmentQueryService.java · public: getAddresses, getAddress, searchAddressesByRecipientName, searchAddressesByPhoneNumber · 주입: ProductRepository, ShipmentRepository, PersonalDataEncryptionUtil

### repository (1)

- `ShipmentRepository` (interface) repository/ShipmentRepository.java · extends/implements JpaRepository · public: findByUser, findByIdAndUser, findByUserAndIsDefaultTrue, findByUserAndRecipientName, findByUserAndPhoneNumber, findByUserAndAddress, findByUserAndZipCode, findByUserAndMultipleFields, countByUser, existsByUserAndIsDefaultTrue, findByUserAndIsDefaultFalse

### entity (1)

- `Shipment` entity/Shipment.java · extends/implements BaseTimeEntity · public: updateAddress, assignUser, unassignUser

### aop (5)

- `ShipmentAuditAspect` aop/ShipmentAuditAspect.java · public: auditAnnotatedMethod, auditAddressCreation, auditAddressUpdate, auditAddressDeletion, auditAddressAccess, auditSecurityViolation
- `ShipmentExceptionAspect` aop/ShipmentExceptionAspect.java · public: handleAnnotatedException, handleServiceException, handleControllerException, handleRepositoryException
- `ShipmentLoggingAspect` aop/ShipmentLoggingAspect.java · public: logAnnotatedMethod, logControllerMethods, logServiceMethods, logRepositoryMethods, logAddressCreation, logAddressUpdate, logAddressDeletion
- `ShipmentPerformanceAspect` aop/ShipmentPerformanceAspect.java · public: monitorAnnotatedPerformance, monitorControllerPerformance, monitorServicePerformance, monitorRepositoryPerformance
- `ShipmentSecurityAspect` aop/ShipmentSecurityAspect.java · public: secureAnnotatedMethod

### dto (4)

- `ShipmentCreateDto` dto/ShipmentCreateDto.java
- `ShipmentListResponseDto` dto/ShipmentListResponseDto.java
- `ShipmentResponseDto` dto/ShipmentResponseDto.java
- `ShipmentUpdateDto` dto/ShipmentUpdateDto.java

### exception (5)

- `ShipmentAccessDeniedException` exception/ShipmentAccessDeniedException.java
- `ShipmentErrorCode` exception/ShipmentErrorCode.java
- `ShipmentException` exception/ShipmentException.java
- `ShipmentNotFoundException` exception/ShipmentNotFoundException.java
- `ShipmentProductNotFoundException` exception/ShipmentProductNotFoundException.java

### other (5)

- `ShipmentAudit` (@interface) aop/annotation/ShipmentAudit.java
- `ShipmentExceptionHandling` (@interface) aop/annotation/ShipmentExceptionHandling.java
- `ShipmentLogging` (@interface) aop/annotation/ShipmentLogging.java
- `ShipmentPerformance` (@interface) aop/annotation/ShipmentPerformance.java
- `ShipmentSecurity` (@interface) aop/annotation/ShipmentSecurity.java

### 다른 도메인 참조 (import·주입 기준)

- **product**: `Product` ← Shipment, ShipmentCommandService, ShipmentQueryService, ShipmentRepository; `ProductRepository` ← ShipmentCommandService, ShipmentQueryService

### 이 도메인을 참조하는 다른 도메인

- **faq**: ShipmentQueryService
- **global**: Shipment, ShipmentRepository
- **product**: Shipment

## 4. JPA 엔티티 연관

- `Shipment` @Entity @Table(name = "address", indexes = { @Index(name = "idx_user_id", columnList = "user_id"), @Index(name = "idx_is_default", columnList = "is_default") }) · extends BaseTimeEntity
  - user: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") → `Product` [product]

다른 도메인 엔티티에서 들어오는 연관:

- `Product.addresses` @OneToMany → `Shipment` [product]

## 5. 이벤트·메시징

(없음)

## 6. AOP

- `ShipmentAuditAspect#auditAnnotatedMethod` @Around("@annotation(addressAudit)") · 바인딩: `@ShipmentAudit`
- `ShipmentAuditAspect#auditAddressCreation` @AfterReturning(pointcut = "execution(* com.fream.back.domain.shipment.service.command.ShipmentCommandService.createAddress(..))", returning = "result")
- `ShipmentAuditAspect#auditAddressUpdate` @AfterReturning(pointcut = "execution(* com.fream.back.domain.shipment.service.command.ShipmentCommandService.updateAddress(..))", returning = "result")
- `ShipmentAuditAspect#auditAddressDeletion` @AfterReturning(pointcut = "execution(* com.fream.back.domain.shipment.service.command.ShipmentCommandService.deleteAddress(..))", returning = "result")
- `ShipmentAuditAspect#auditAddressAccess` @AfterReturning(pointcut = "execution(* com.fream.back.domain.shipment.service.query.ShipmentQueryService.*(..))", returning = "result")
- `ShipmentAuditAspect#auditSecurityViolation` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.shipment..*(..))", throwing = "exception")
- `ShipmentExceptionAspect#handleAnnotatedException` @Around("@annotation(exceptionHandling)") · 바인딩: `@ShipmentExceptionHandling`
- `ShipmentExceptionAspect#handleServiceException` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.shipment.service..*(..))", throwing = "exception")
- `ShipmentExceptionAspect#handleControllerException` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.shipment.controller..*(..))", throwing = "exception")
- `ShipmentExceptionAspect#handleRepositoryException` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.shipment.repository..*(..))", throwing = "exception")
- `ShipmentLoggingAspect#logAnnotatedMethod` @Around("@annotation(addressLogging)") · 바인딩: `@ShipmentLogging`
- `ShipmentLoggingAspect#logControllerMethods` @Around("execution(* com.fream.back.domain.shipment.controller..*(..))")
- `ShipmentLoggingAspect#logServiceMethods` @Around("execution(* com.fream.back.domain.shipment.service..*(..))")
- `ShipmentLoggingAspect#logRepositoryMethods` @Around("execution(* com.fream.back.domain.shipment.repository..*(..))")
- `ShipmentLoggingAspect#logAddressCreation` @AfterReturning(pointcut = "execution(* com.fream.back.domain.shipment.service.command.ShipmentCommandService.createAddress(..))", returning = "result")
- `ShipmentLoggingAspect#logAddressUpdate` @AfterReturning(pointcut = "execution(* com.fream.back.domain.shipment.service.command.ShipmentCommandService.updateAddress(..))", returning = "result")
- `ShipmentLoggingAspect#logAddressDeletion` @AfterReturning(pointcut = "execution(* com.fream.back.domain.shipment.service.command.ShipmentCommandService.deleteAddress(..))", returning = "result")
- `ShipmentPerformanceAspect#monitorAnnotatedPerformance` @Around("@annotation(addressPerformance)") · 바인딩: `@ShipmentPerformance`
- `ShipmentPerformanceAspect#monitorControllerPerformance` @Around("execution(* com.fream.back.domain.shipment.controller..*(..))")
- `ShipmentPerformanceAspect#monitorServicePerformance` @Around("execution(* com.fream.back.domain.shipment.service..*(..))")
- `ShipmentPerformanceAspect#monitorRepositoryPerformance` @Around("execution(* com.fream.back.domain.shipment.repository..*(..))")
- `ShipmentSecurityAspect#secureAnnotatedMethod` @Around("@annotation(addressSecurity)") · 바인딩: `@ShipmentSecurity`

어노테이션 적용 지점(메서드에 붙은 곳):

- `@ShipmentAudit`: (없음)
- `@ShipmentExceptionHandling`: (없음)
- `@ShipmentLogging`: (없음)
- `@ShipmentPerformance`: (없음)
- `@ShipmentSecurity`: (없음)

