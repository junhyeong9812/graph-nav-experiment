# internaladdress 도메인

기준 경로: `src/main/java/com/fream/back/domain/internaladdress/` · 파일 14개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/internal/addresses` | `InternalAddressCommandController#createAddress` | `InternalAddressCommandService#createAddress` |
| PUT | `/internal/addresses` | `InternalAddressCommandController#updateAddress` | `InternalAddressCommandService#updateAddress` |
| DELETE | `/internal/addresses/{addressId}` | `InternalAddressCommandController#deleteAddress` | `InternalAddressCommandService#deleteAddress` |
| GET | `/internal/addresses` | `InternalAddressQueryController#getAddresses` | `InternalAddressQueryService#getAddresses` |
| GET | `/internal/addresses/{addressId}` | `InternalAddressQueryController#getAddress` | `InternalAddressQueryService#getAddress` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `InternalAddressAuditAspect`
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
- `InternalAddressCommandService`
  - `createAddress` → UserRepository#findByEmail, PersonalDataEncryptionUtil#deterministicEncrypt, PersonalDataEncryptionUtil#encrypt, AddressRepository#save~
  - `updateAddress` → UserRepository#findByEmail, AddressRepository#findByIdAndUser, PersonalDataEncryptionUtil#deterministicEncrypt, PersonalDataEncryptionUtil#encrypt
  - `deleteAddress` → UserRepository#findByEmail, AddressRepository#findByIdAndUser, AddressRepository#delete~
- `InternalAddressExceptionAspect`
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
- `InternalAddressLoggingAspect`
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
- `InternalAddressPerformanceAspect`
  - `monitorAnnotatedPerformance` → this#extractUserEmailSafely, this#getDefaultWarningThreshold, this#getDefaultErrorThreshold, this#executePerformanceMonitoring
  - `monitorControllerPerformance` → this#monitorMethodPerformance
  - `monitorServicePerformance` → this#monitorMethodPerformance
  - `monitorRepositoryPerformance` → this#monitorMethodPerformance
  - `executePerformanceMonitoring` → this#updatePerformanceStats, this#logAdvancedPerformanceMetrics, this#shouldGeneratePerformanceReport, this#generatePerformanceReport
  - `monitorMethodPerformance` → this#extractUserEmailSafely, this#updatePerformanceStats, this#logPerformanceMetrics, this#shouldGeneratePerformanceReport, this#generatePerformanceReport
- `InternalAddressQueryService`
  - `getAddresses` → PersonalDataEncryptionUtil#deterministicEncrypt, UserRepository#findByEmail, PersonalDataEncryptionUtil#deterministicDecrypt, PersonalDataEncryptionUtil#decrypt
  - `getAddress` → PersonalDataEncryptionUtil#deterministicEncrypt, UserRepository#findByEmail, AddressRepository#findByIdAndUser, PersonalDataEncryptionUtil#deterministicDecrypt, PersonalDataEncryptionUtil#decrypt
  - `searchAddressesByRecipientName` → PersonalDataEncryptionUtil#deterministicEncrypt, UserRepository#findByEmail, PersonalDataEncryptionUtil#encryptForSearch, AddressRepository#findByUserAndRecipientName, PersonalDataEncryptionUtil#deterministicDecrypt, PersonalDataEncryptionUtil#decrypt
  - `searchAddressesByPhoneNumber` → PersonalDataEncryptionUtil#deterministicEncrypt, UserRepository#findByEmail, PersonalDataEncryptionUtil#encryptForSearch, AddressRepository#findByUserAndPhoneNumber, PersonalDataEncryptionUtil#deterministicDecrypt, PersonalDataEncryptionUtil#decrypt
- `InternalAddressSecurityAspect`
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

- `InternalAddressCommandController` controller/command/InternalAddressCommandController.java · public: createAddress, updateAddress, deleteAddress · 주입: InternalAddressCommandService
- `InternalAddressQueryController` controller/query/InternalAddressQueryController.java · public: getAddresses, getAddress · 주입: InternalAddressQueryService

### service-command (1)

- `InternalAddressCommandService` service/command/InternalAddressCommandService.java · public: createAddress, updateAddress, deleteAddress · 주입: UserRepository, AddressRepository, PersonalDataEncryptionUtil

### service-query (1)

- `InternalAddressQueryService` service/query/InternalAddressQueryService.java · public: getAddresses, getAddress, searchAddressesByRecipientName, searchAddressesByPhoneNumber · 주입: UserRepository, AddressRepository, PersonalDataEncryptionUtil

### aop (5)

- `InternalAddressAuditAspect` aop/InternalAddressAuditAspect.java · public: auditAnnotatedMethod, auditAddressCreation, auditAddressUpdate, auditAddressDeletion, auditAddressAccess, auditSecurityViolation
- `InternalAddressExceptionAspect` aop/InternalAddressExceptionAspect.java · public: handleAnnotatedException, handleServiceException, handleControllerException, handleRepositoryException
- `InternalAddressLoggingAspect` aop/InternalAddressLoggingAspect.java · public: logAnnotatedMethod, logControllerMethods, logServiceMethods, logRepositoryMethods, logAddressCreation, logAddressUpdate, logAddressDeletion
- `InternalAddressPerformanceAspect` aop/InternalAddressPerformanceAspect.java · public: monitorAnnotatedPerformance, monitorControllerPerformance, monitorServicePerformance, monitorRepositoryPerformance
- `InternalAddressSecurityAspect` aop/InternalAddressSecurityAspect.java · public: secureAnnotatedMethod

### other (5)

- `InternalAddressAudit` (@interface) aop/annotation/InternalAddressAudit.java
- `InternalAddressExceptionHandling` (@interface) aop/annotation/InternalAddressExceptionHandling.java
- `InternalAddressLogging` (@interface) aop/annotation/InternalAddressLogging.java
- `InternalAddressPerformance` (@interface) aop/annotation/InternalAddressPerformance.java
- `InternalAddressSecurity` (@interface) aop/annotation/InternalAddressSecurity.java

### 다른 도메인 참조 (import·주입 기준)

- **address**: `Address` ← InternalAddressCommandService, InternalAddressQueryService; `AddressRepository` ← InternalAddressCommandService, InternalAddressQueryService
- **user**: `User` ← InternalAddressCommandService, InternalAddressQueryService; `UserRepository` ← InternalAddressCommandService, InternalAddressQueryService

### 이 도메인을 참조하는 다른 도메인

- **internalorder**: InternalAddressQueryService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

- `InternalAddressAuditAspect#auditAnnotatedMethod` @Around("@annotation(addressAudit)") · 바인딩: `@InternalAddressAudit`
- `InternalAddressAuditAspect#auditAddressCreation` @AfterReturning(pointcut = "execution(* com.fream.back.domain.internaladdress.service.command.InternalAddressCommandService.createAddress(..))", returning = "result")
- `InternalAddressAuditAspect#auditAddressUpdate` @AfterReturning(pointcut = "execution(* com.fream.back.domain.internaladdress.service.command.InternalAddressCommandService.updateAddress(..))", returning = "result")
- `InternalAddressAuditAspect#auditAddressDeletion` @AfterReturning(pointcut = "execution(* com.fream.back.domain.internaladdress.service.command.InternalAddressCommandService.deleteAddress(..))", returning = "result")
- `InternalAddressAuditAspect#auditAddressAccess` @AfterReturning(pointcut = "execution(* com.fream.back.domain.internaladdress.service.query.InternalAddressQueryService.*(..))", returning = "result")
- `InternalAddressAuditAspect#auditSecurityViolation` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.internaladdress..*(..))", throwing = "exception")
- `InternalAddressExceptionAspect#handleAnnotatedException` @Around("@annotation(exceptionHandling)") · 바인딩: `@InternalAddressExceptionHandling`
- `InternalAddressExceptionAspect#handleServiceException` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.internaladdress.service..*(..))", throwing = "exception")
- `InternalAddressExceptionAspect#handleControllerException` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.internaladdress.controller..*(..))", throwing = "exception")
- `InternalAddressExceptionAspect#handleRepositoryException` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.internaladdress.repository..*(..))", throwing = "exception")
- `InternalAddressLoggingAspect#logAnnotatedMethod` @Around("@annotation(addressLogging)") · 바인딩: `@InternalAddressLogging`
- `InternalAddressLoggingAspect#logControllerMethods` @Around("execution(* com.fream.back.domain.internaladdress.controller..*(..))")
- `InternalAddressLoggingAspect#logServiceMethods` @Around("execution(* com.fream.back.domain.internaladdress.service..*(..))")
- `InternalAddressLoggingAspect#logRepositoryMethods` @Around("execution(* com.fream.back.domain.internaladdress.repository..*(..))")
- `InternalAddressLoggingAspect#logAddressCreation` @AfterReturning(pointcut = "execution(* com.fream.back.domain.internaladdress.service.command.InternalAddressCommandService.createAddress(..))", returning = "result")
- `InternalAddressLoggingAspect#logAddressUpdate` @AfterReturning(pointcut = "execution(* com.fream.back.domain.internaladdress.service.command.InternalAddressCommandService.updateAddress(..))", returning = "result")
- `InternalAddressLoggingAspect#logAddressDeletion` @AfterReturning(pointcut = "execution(* com.fream.back.domain.internaladdress.service.command.InternalAddressCommandService.deleteAddress(..))", returning = "result")
- `InternalAddressPerformanceAspect#monitorAnnotatedPerformance` @Around("@annotation(addressPerformance)") · 바인딩: `@InternalAddressPerformance`
- `InternalAddressPerformanceAspect#monitorControllerPerformance` @Around("execution(* com.fream.back.domain.internaladdress.controller..*(..))")
- `InternalAddressPerformanceAspect#monitorServicePerformance` @Around("execution(* com.fream.back.domain.internaladdress.service..*(..))")
- `InternalAddressPerformanceAspect#monitorRepositoryPerformance` @Around("execution(* com.fream.back.domain.internaladdress.repository..*(..))")
- `InternalAddressSecurityAspect#secureAnnotatedMethod` @Around("@annotation(addressSecurity)") · 바인딩: `@InternalAddressSecurity`

어노테이션 적용 지점(메서드에 붙은 곳):

- `@InternalAddressAudit`: (없음)
- `@InternalAddressExceptionHandling`: (없음)
- `@InternalAddressLogging`: (없음)
- `@InternalAddressPerformance`: (없음)
- `@InternalAddressSecurity`: (없음)

