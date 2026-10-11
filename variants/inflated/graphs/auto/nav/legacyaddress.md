# legacyaddress 도메인

기준 경로: `src/main/java/com/fream/back/domain/legacyaddress/` · 파일 14개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/legacy/addresses` | `LegacyAddressCommandController#createAddress` | `LegacyAddressCommandService#createAddress` |
| PUT | `/legacy/addresses` | `LegacyAddressCommandController#updateAddress` | `LegacyAddressCommandService#updateAddress` |
| DELETE | `/legacy/addresses/{addressId}` | `LegacyAddressCommandController#deleteAddress` | `LegacyAddressCommandService#deleteAddress` |
| GET | `/legacy/addresses` | `LegacyAddressQueryController#getAddresses` | `LegacyAddressQueryService#getAddresses` |
| GET | `/legacy/addresses/{addressId}` | `LegacyAddressQueryController#getAddress` | `LegacyAddressQueryService#getAddress` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `LegacyAddressAuditAspect`
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
- `LegacyAddressCommandService`
  - `createAddress` → UserRepository#findByEmail, PersonalDataEncryptionUtil#deterministicEncrypt, PersonalDataEncryptionUtil#encrypt, AddressRepository#save~
  - `updateAddress` → UserRepository#findByEmail, AddressRepository#findByIdAndUser, PersonalDataEncryptionUtil#deterministicEncrypt, PersonalDataEncryptionUtil#encrypt
  - `deleteAddress` → UserRepository#findByEmail, AddressRepository#findByIdAndUser, AddressRepository#delete~
- `LegacyAddressExceptionAspect`
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
- `LegacyAddressLoggingAspect`
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
- `LegacyAddressPerformanceAspect`
  - `monitorAnnotatedPerformance` → this#extractUserEmailSafely, this#getDefaultWarningThreshold, this#getDefaultErrorThreshold, this#executePerformanceMonitoring
  - `monitorControllerPerformance` → this#monitorMethodPerformance
  - `monitorServicePerformance` → this#monitorMethodPerformance
  - `monitorRepositoryPerformance` → this#monitorMethodPerformance
  - `executePerformanceMonitoring` → this#updatePerformanceStats, this#logAdvancedPerformanceMetrics, this#shouldGeneratePerformanceReport, this#generatePerformanceReport
  - `monitorMethodPerformance` → this#extractUserEmailSafely, this#updatePerformanceStats, this#logPerformanceMetrics, this#shouldGeneratePerformanceReport, this#generatePerformanceReport
- `LegacyAddressQueryService`
  - `getAddresses` → PersonalDataEncryptionUtil#deterministicEncrypt, UserRepository#findByEmail, PersonalDataEncryptionUtil#deterministicDecrypt, PersonalDataEncryptionUtil#decrypt
  - `getAddress` → PersonalDataEncryptionUtil#deterministicEncrypt, UserRepository#findByEmail, AddressRepository#findByIdAndUser, PersonalDataEncryptionUtil#deterministicDecrypt, PersonalDataEncryptionUtil#decrypt
  - `searchAddressesByRecipientName` → PersonalDataEncryptionUtil#deterministicEncrypt, UserRepository#findByEmail, PersonalDataEncryptionUtil#encryptForSearch, AddressRepository#findByUserAndRecipientName, PersonalDataEncryptionUtil#deterministicDecrypt, PersonalDataEncryptionUtil#decrypt
  - `searchAddressesByPhoneNumber` → PersonalDataEncryptionUtil#deterministicEncrypt, UserRepository#findByEmail, PersonalDataEncryptionUtil#encryptForSearch, AddressRepository#findByUserAndPhoneNumber, PersonalDataEncryptionUtil#deterministicDecrypt, PersonalDataEncryptionUtil#decrypt
- `LegacyAddressSecurityAspect`
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

- `LegacyAddressCommandController` controller/command/LegacyAddressCommandController.java · public: createAddress, updateAddress, deleteAddress · 주입: LegacyAddressCommandService
- `LegacyAddressQueryController` controller/query/LegacyAddressQueryController.java · public: getAddresses, getAddress · 주입: LegacyAddressQueryService

### service-command (1)

- `LegacyAddressCommandService` service/command/LegacyAddressCommandService.java · public: createAddress, updateAddress, deleteAddress · 주입: UserRepository, AddressRepository, PersonalDataEncryptionUtil

### service-query (1)

- `LegacyAddressQueryService` service/query/LegacyAddressQueryService.java · public: getAddresses, getAddress, searchAddressesByRecipientName, searchAddressesByPhoneNumber · 주입: UserRepository, AddressRepository, PersonalDataEncryptionUtil

### aop (5)

- `LegacyAddressAuditAspect` aop/LegacyAddressAuditAspect.java · public: auditAnnotatedMethod, auditAddressCreation, auditAddressUpdate, auditAddressDeletion, auditAddressAccess, auditSecurityViolation
- `LegacyAddressExceptionAspect` aop/LegacyAddressExceptionAspect.java · public: handleAnnotatedException, handleServiceException, handleControllerException, handleRepositoryException
- `LegacyAddressLoggingAspect` aop/LegacyAddressLoggingAspect.java · public: logAnnotatedMethod, logControllerMethods, logServiceMethods, logRepositoryMethods, logAddressCreation, logAddressUpdate, logAddressDeletion
- `LegacyAddressPerformanceAspect` aop/LegacyAddressPerformanceAspect.java · public: monitorAnnotatedPerformance, monitorControllerPerformance, monitorServicePerformance, monitorRepositoryPerformance
- `LegacyAddressSecurityAspect` aop/LegacyAddressSecurityAspect.java · public: secureAnnotatedMethod

### other (5)

- `LegacyAddressAudit` (@interface) aop/annotation/LegacyAddressAudit.java
- `LegacyAddressExceptionHandling` (@interface) aop/annotation/LegacyAddressExceptionHandling.java
- `LegacyAddressLogging` (@interface) aop/annotation/LegacyAddressLogging.java
- `LegacyAddressPerformance` (@interface) aop/annotation/LegacyAddressPerformance.java
- `LegacyAddressSecurity` (@interface) aop/annotation/LegacyAddressSecurity.java

### 다른 도메인 참조 (import·주입 기준)

- **address**: `Address` ← LegacyAddressCommandService, LegacyAddressQueryService; `AddressRepository` ← LegacyAddressCommandService, LegacyAddressQueryService
- **user**: `User` ← LegacyAddressCommandService, LegacyAddressQueryService; `UserRepository` ← LegacyAddressCommandService, LegacyAddressQueryService

### 이 도메인을 참조하는 다른 도메인

- **legacyorder**: LegacyAddressQueryService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

- `LegacyAddressAuditAspect#auditAnnotatedMethod` @Around("@annotation(addressAudit)") · 바인딩: `@LegacyAddressAudit`
- `LegacyAddressAuditAspect#auditAddressCreation` @AfterReturning(pointcut = "execution(* com.fream.back.domain.legacyaddress.service.command.LegacyAddressCommandService.createAddress(..))", returning = "result")
- `LegacyAddressAuditAspect#auditAddressUpdate` @AfterReturning(pointcut = "execution(* com.fream.back.domain.legacyaddress.service.command.LegacyAddressCommandService.updateAddress(..))", returning = "result")
- `LegacyAddressAuditAspect#auditAddressDeletion` @AfterReturning(pointcut = "execution(* com.fream.back.domain.legacyaddress.service.command.LegacyAddressCommandService.deleteAddress(..))", returning = "result")
- `LegacyAddressAuditAspect#auditAddressAccess` @AfterReturning(pointcut = "execution(* com.fream.back.domain.legacyaddress.service.query.LegacyAddressQueryService.*(..))", returning = "result")
- `LegacyAddressAuditAspect#auditSecurityViolation` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.legacyaddress..*(..))", throwing = "exception")
- `LegacyAddressExceptionAspect#handleAnnotatedException` @Around("@annotation(exceptionHandling)") · 바인딩: `@LegacyAddressExceptionHandling`
- `LegacyAddressExceptionAspect#handleServiceException` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.legacyaddress.service..*(..))", throwing = "exception")
- `LegacyAddressExceptionAspect#handleControllerException` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.legacyaddress.controller..*(..))", throwing = "exception")
- `LegacyAddressExceptionAspect#handleRepositoryException` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.legacyaddress.repository..*(..))", throwing = "exception")
- `LegacyAddressLoggingAspect#logAnnotatedMethod` @Around("@annotation(addressLogging)") · 바인딩: `@LegacyAddressLogging`
- `LegacyAddressLoggingAspect#logControllerMethods` @Around("execution(* com.fream.back.domain.legacyaddress.controller..*(..))")
- `LegacyAddressLoggingAspect#logServiceMethods` @Around("execution(* com.fream.back.domain.legacyaddress.service..*(..))")
- `LegacyAddressLoggingAspect#logRepositoryMethods` @Around("execution(* com.fream.back.domain.legacyaddress.repository..*(..))")
- `LegacyAddressLoggingAspect#logAddressCreation` @AfterReturning(pointcut = "execution(* com.fream.back.domain.legacyaddress.service.command.LegacyAddressCommandService.createAddress(..))", returning = "result")
- `LegacyAddressLoggingAspect#logAddressUpdate` @AfterReturning(pointcut = "execution(* com.fream.back.domain.legacyaddress.service.command.LegacyAddressCommandService.updateAddress(..))", returning = "result")
- `LegacyAddressLoggingAspect#logAddressDeletion` @AfterReturning(pointcut = "execution(* com.fream.back.domain.legacyaddress.service.command.LegacyAddressCommandService.deleteAddress(..))", returning = "result")
- `LegacyAddressPerformanceAspect#monitorAnnotatedPerformance` @Around("@annotation(addressPerformance)") · 바인딩: `@LegacyAddressPerformance`
- `LegacyAddressPerformanceAspect#monitorControllerPerformance` @Around("execution(* com.fream.back.domain.legacyaddress.controller..*(..))")
- `LegacyAddressPerformanceAspect#monitorServicePerformance` @Around("execution(* com.fream.back.domain.legacyaddress.service..*(..))")
- `LegacyAddressPerformanceAspect#monitorRepositoryPerformance` @Around("execution(* com.fream.back.domain.legacyaddress.repository..*(..))")
- `LegacyAddressSecurityAspect#secureAnnotatedMethod` @Around("@annotation(addressSecurity)") · 바인딩: `@LegacyAddressSecurity`

어노테이션 적용 지점(메서드에 붙은 곳):

- `@LegacyAddressAudit`: (없음)
- `@LegacyAddressExceptionHandling`: (없음)
- `@LegacyAddressLogging`: (없음)
- `@LegacyAddressPerformance`: (없음)
- `@LegacyAddressSecurity`: (없음)

