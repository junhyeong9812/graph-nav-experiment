# adminaddress 도메인

기준 경로: `src/main/java/com/fream/back/domain/adminaddress/` · 파일 14개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/admin/addresses` | `AdminAddressCommandController#createAddress` | `AdminAddressCommandService#createAddress` |
| PUT | `/admin/addresses` | `AdminAddressCommandController#updateAddress` | `AdminAddressCommandService#updateAddress` |
| DELETE | `/admin/addresses/{addressId}` | `AdminAddressCommandController#deleteAddress` | `AdminAddressCommandService#deleteAddress` |
| GET | `/admin/addresses` | `AdminAddressQueryController#getAddresses` | `AdminAddressQueryService#getAddresses` |
| GET | `/admin/addresses/{addressId}` | `AdminAddressQueryController#getAddress` | `AdminAddressQueryService#getAddress` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `AdminAddressAuditAspect`
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
- `AdminAddressCommandService`
  - `createAddress` → UserRepository#findByEmail, PersonalDataEncryptionUtil#deterministicEncrypt, PersonalDataEncryptionUtil#encrypt, AddressRepository#save~
  - `updateAddress` → UserRepository#findByEmail, AddressRepository#findByIdAndUser, PersonalDataEncryptionUtil#deterministicEncrypt, PersonalDataEncryptionUtil#encrypt
  - `deleteAddress` → UserRepository#findByEmail, AddressRepository#findByIdAndUser, AddressRepository#delete~
- `AdminAddressExceptionAspect`
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
- `AdminAddressLoggingAspect`
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
- `AdminAddressPerformanceAspect`
  - `monitorAnnotatedPerformance` → this#extractUserEmailSafely, this#getDefaultWarningThreshold, this#getDefaultErrorThreshold, this#executePerformanceMonitoring
  - `monitorControllerPerformance` → this#monitorMethodPerformance
  - `monitorServicePerformance` → this#monitorMethodPerformance
  - `monitorRepositoryPerformance` → this#monitorMethodPerformance
  - `executePerformanceMonitoring` → this#updatePerformanceStats, this#logAdvancedPerformanceMetrics, this#shouldGeneratePerformanceReport, this#generatePerformanceReport
  - `monitorMethodPerformance` → this#extractUserEmailSafely, this#updatePerformanceStats, this#logPerformanceMetrics, this#shouldGeneratePerformanceReport, this#generatePerformanceReport
- `AdminAddressQueryService`
  - `getAddresses` → PersonalDataEncryptionUtil#deterministicEncrypt, UserRepository#findByEmail, PersonalDataEncryptionUtil#deterministicDecrypt, PersonalDataEncryptionUtil#decrypt
  - `getAddress` → PersonalDataEncryptionUtil#deterministicEncrypt, UserRepository#findByEmail, AddressRepository#findByIdAndUser, PersonalDataEncryptionUtil#deterministicDecrypt, PersonalDataEncryptionUtil#decrypt
  - `searchAddressesByRecipientName` → PersonalDataEncryptionUtil#deterministicEncrypt, UserRepository#findByEmail, PersonalDataEncryptionUtil#encryptForSearch, AddressRepository#findByUserAndRecipientName, PersonalDataEncryptionUtil#deterministicDecrypt, PersonalDataEncryptionUtil#decrypt
  - `searchAddressesByPhoneNumber` → PersonalDataEncryptionUtil#deterministicEncrypt, UserRepository#findByEmail, PersonalDataEncryptionUtil#encryptForSearch, AddressRepository#findByUserAndPhoneNumber, PersonalDataEncryptionUtil#deterministicDecrypt, PersonalDataEncryptionUtil#decrypt
- `AdminAddressSecurityAspect`
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

- `AdminAddressCommandController` controller/command/AdminAddressCommandController.java · public: createAddress, updateAddress, deleteAddress · 주입: AdminAddressCommandService
- `AdminAddressQueryController` controller/query/AdminAddressQueryController.java · public: getAddresses, getAddress · 주입: AdminAddressQueryService

### service-command (1)

- `AdminAddressCommandService` service/command/AdminAddressCommandService.java · public: createAddress, updateAddress, deleteAddress · 주입: UserRepository, AddressRepository, PersonalDataEncryptionUtil

### service-query (1)

- `AdminAddressQueryService` service/query/AdminAddressQueryService.java · public: getAddresses, getAddress, searchAddressesByRecipientName, searchAddressesByPhoneNumber · 주입: UserRepository, AddressRepository, PersonalDataEncryptionUtil

### aop (5)

- `AdminAddressAuditAspect` aop/AdminAddressAuditAspect.java · public: auditAnnotatedMethod, auditAddressCreation, auditAddressUpdate, auditAddressDeletion, auditAddressAccess, auditSecurityViolation
- `AdminAddressExceptionAspect` aop/AdminAddressExceptionAspect.java · public: handleAnnotatedException, handleServiceException, handleControllerException, handleRepositoryException
- `AdminAddressLoggingAspect` aop/AdminAddressLoggingAspect.java · public: logAnnotatedMethod, logControllerMethods, logServiceMethods, logRepositoryMethods, logAddressCreation, logAddressUpdate, logAddressDeletion
- `AdminAddressPerformanceAspect` aop/AdminAddressPerformanceAspect.java · public: monitorAnnotatedPerformance, monitorControllerPerformance, monitorServicePerformance, monitorRepositoryPerformance
- `AdminAddressSecurityAspect` aop/AdminAddressSecurityAspect.java · public: secureAnnotatedMethod

### other (5)

- `AdminAddressAudit` (@interface) aop/annotation/AdminAddressAudit.java
- `AdminAddressExceptionHandling` (@interface) aop/annotation/AdminAddressExceptionHandling.java
- `AdminAddressLogging` (@interface) aop/annotation/AdminAddressLogging.java
- `AdminAddressPerformance` (@interface) aop/annotation/AdminAddressPerformance.java
- `AdminAddressSecurity` (@interface) aop/annotation/AdminAddressSecurity.java

### 다른 도메인 참조 (import·주입 기준)

- **address**: `Address` ← AdminAddressCommandService, AdminAddressQueryService; `AddressRepository` ← AdminAddressCommandService, AdminAddressQueryService
- **user**: `User` ← AdminAddressCommandService, AdminAddressQueryService; `UserRepository` ← AdminAddressCommandService, AdminAddressQueryService

### 이 도메인을 참조하는 다른 도메인

- **adminorder**: AdminAddressQueryService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

- `AdminAddressAuditAspect#auditAnnotatedMethod` @Around("@annotation(addressAudit)") · 바인딩: `@AdminAddressAudit`
- `AdminAddressAuditAspect#auditAddressCreation` @AfterReturning(pointcut = "execution(* com.fream.back.domain.adminaddress.service.command.AdminAddressCommandService.createAddress(..))", returning = "result")
- `AdminAddressAuditAspect#auditAddressUpdate` @AfterReturning(pointcut = "execution(* com.fream.back.domain.adminaddress.service.command.AdminAddressCommandService.updateAddress(..))", returning = "result")
- `AdminAddressAuditAspect#auditAddressDeletion` @AfterReturning(pointcut = "execution(* com.fream.back.domain.adminaddress.service.command.AdminAddressCommandService.deleteAddress(..))", returning = "result")
- `AdminAddressAuditAspect#auditAddressAccess` @AfterReturning(pointcut = "execution(* com.fream.back.domain.adminaddress.service.query.AdminAddressQueryService.*(..))", returning = "result")
- `AdminAddressAuditAspect#auditSecurityViolation` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.adminaddress..*(..))", throwing = "exception")
- `AdminAddressExceptionAspect#handleAnnotatedException` @Around("@annotation(exceptionHandling)") · 바인딩: `@AdminAddressExceptionHandling`
- `AdminAddressExceptionAspect#handleServiceException` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.adminaddress.service..*(..))", throwing = "exception")
- `AdminAddressExceptionAspect#handleControllerException` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.adminaddress.controller..*(..))", throwing = "exception")
- `AdminAddressExceptionAspect#handleRepositoryException` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.adminaddress.repository..*(..))", throwing = "exception")
- `AdminAddressLoggingAspect#logAnnotatedMethod` @Around("@annotation(addressLogging)") · 바인딩: `@AdminAddressLogging`
- `AdminAddressLoggingAspect#logControllerMethods` @Around("execution(* com.fream.back.domain.adminaddress.controller..*(..))")
- `AdminAddressLoggingAspect#logServiceMethods` @Around("execution(* com.fream.back.domain.adminaddress.service..*(..))")
- `AdminAddressLoggingAspect#logRepositoryMethods` @Around("execution(* com.fream.back.domain.adminaddress.repository..*(..))")
- `AdminAddressLoggingAspect#logAddressCreation` @AfterReturning(pointcut = "execution(* com.fream.back.domain.adminaddress.service.command.AdminAddressCommandService.createAddress(..))", returning = "result")
- `AdminAddressLoggingAspect#logAddressUpdate` @AfterReturning(pointcut = "execution(* com.fream.back.domain.adminaddress.service.command.AdminAddressCommandService.updateAddress(..))", returning = "result")
- `AdminAddressLoggingAspect#logAddressDeletion` @AfterReturning(pointcut = "execution(* com.fream.back.domain.adminaddress.service.command.AdminAddressCommandService.deleteAddress(..))", returning = "result")
- `AdminAddressPerformanceAspect#monitorAnnotatedPerformance` @Around("@annotation(addressPerformance)") · 바인딩: `@AdminAddressPerformance`
- `AdminAddressPerformanceAspect#monitorControllerPerformance` @Around("execution(* com.fream.back.domain.adminaddress.controller..*(..))")
- `AdminAddressPerformanceAspect#monitorServicePerformance` @Around("execution(* com.fream.back.domain.adminaddress.service..*(..))")
- `AdminAddressPerformanceAspect#monitorRepositoryPerformance` @Around("execution(* com.fream.back.domain.adminaddress.repository..*(..))")
- `AdminAddressSecurityAspect#secureAnnotatedMethod` @Around("@annotation(addressSecurity)") · 바인딩: `@AdminAddressSecurity`

어노테이션 적용 지점(메서드에 붙은 곳):

- `@AdminAddressAudit`: (없음)
- `@AdminAddressExceptionHandling`: (없음)
- `@AdminAddressLogging`: (없음)
- `@AdminAddressPerformance`: (없음)
- `@AdminAddressSecurity`: (없음)

