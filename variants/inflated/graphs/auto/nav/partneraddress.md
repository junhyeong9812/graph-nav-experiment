# partneraddress 도메인

기준 경로: `src/main/java/com/fream/back/domain/partneraddress/` · 파일 14개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/partner/addresses` | `PartnerAddressCommandController#createAddress` | `PartnerAddressCommandService#createAddress` |
| PUT | `/partner/addresses` | `PartnerAddressCommandController#updateAddress` | `PartnerAddressCommandService#updateAddress` |
| DELETE | `/partner/addresses/{addressId}` | `PartnerAddressCommandController#deleteAddress` | `PartnerAddressCommandService#deleteAddress` |
| GET | `/partner/addresses` | `PartnerAddressQueryController#getAddresses` | `PartnerAddressQueryService#getAddresses` |
| GET | `/partner/addresses/{addressId}` | `PartnerAddressQueryController#getAddress` | `PartnerAddressQueryService#getAddress` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `PartnerAddressAuditAspect`
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
- `PartnerAddressCommandService`
  - `createAddress` → UserRepository#findByEmail, PersonalDataEncryptionUtil#deterministicEncrypt, PersonalDataEncryptionUtil#encrypt, AddressRepository#save~
  - `updateAddress` → UserRepository#findByEmail, AddressRepository#findByIdAndUser, PersonalDataEncryptionUtil#deterministicEncrypt, PersonalDataEncryptionUtil#encrypt
  - `deleteAddress` → UserRepository#findByEmail, AddressRepository#findByIdAndUser, AddressRepository#delete~
- `PartnerAddressExceptionAspect`
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
- `PartnerAddressLoggingAspect`
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
- `PartnerAddressPerformanceAspect`
  - `monitorAnnotatedPerformance` → this#extractUserEmailSafely, this#getDefaultWarningThreshold, this#getDefaultErrorThreshold, this#executePerformanceMonitoring
  - `monitorControllerPerformance` → this#monitorMethodPerformance
  - `monitorServicePerformance` → this#monitorMethodPerformance
  - `monitorRepositoryPerformance` → this#monitorMethodPerformance
  - `executePerformanceMonitoring` → this#updatePerformanceStats, this#logAdvancedPerformanceMetrics, this#shouldGeneratePerformanceReport, this#generatePerformanceReport
  - `monitorMethodPerformance` → this#extractUserEmailSafely, this#updatePerformanceStats, this#logPerformanceMetrics, this#shouldGeneratePerformanceReport, this#generatePerformanceReport
- `PartnerAddressQueryService`
  - `getAddresses` → PersonalDataEncryptionUtil#deterministicEncrypt, UserRepository#findByEmail, PersonalDataEncryptionUtil#deterministicDecrypt, PersonalDataEncryptionUtil#decrypt
  - `getAddress` → PersonalDataEncryptionUtil#deterministicEncrypt, UserRepository#findByEmail, AddressRepository#findByIdAndUser, PersonalDataEncryptionUtil#deterministicDecrypt, PersonalDataEncryptionUtil#decrypt
  - `searchAddressesByRecipientName` → PersonalDataEncryptionUtil#deterministicEncrypt, UserRepository#findByEmail, PersonalDataEncryptionUtil#encryptForSearch, AddressRepository#findByUserAndRecipientName, PersonalDataEncryptionUtil#deterministicDecrypt, PersonalDataEncryptionUtil#decrypt
  - `searchAddressesByPhoneNumber` → PersonalDataEncryptionUtil#deterministicEncrypt, UserRepository#findByEmail, PersonalDataEncryptionUtil#encryptForSearch, AddressRepository#findByUserAndPhoneNumber, PersonalDataEncryptionUtil#deterministicDecrypt, PersonalDataEncryptionUtil#decrypt
- `PartnerAddressSecurityAspect`
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

- `PartnerAddressCommandController` controller/command/PartnerAddressCommandController.java · public: createAddress, updateAddress, deleteAddress · 주입: PartnerAddressCommandService
- `PartnerAddressQueryController` controller/query/PartnerAddressQueryController.java · public: getAddresses, getAddress · 주입: PartnerAddressQueryService

### service-command (1)

- `PartnerAddressCommandService` service/command/PartnerAddressCommandService.java · public: createAddress, updateAddress, deleteAddress · 주입: UserRepository, AddressRepository, PersonalDataEncryptionUtil

### service-query (1)

- `PartnerAddressQueryService` service/query/PartnerAddressQueryService.java · public: getAddresses, getAddress, searchAddressesByRecipientName, searchAddressesByPhoneNumber · 주입: UserRepository, AddressRepository, PersonalDataEncryptionUtil

### aop (5)

- `PartnerAddressAuditAspect` aop/PartnerAddressAuditAspect.java · public: auditAnnotatedMethod, auditAddressCreation, auditAddressUpdate, auditAddressDeletion, auditAddressAccess, auditSecurityViolation
- `PartnerAddressExceptionAspect` aop/PartnerAddressExceptionAspect.java · public: handleAnnotatedException, handleServiceException, handleControllerException, handleRepositoryException
- `PartnerAddressLoggingAspect` aop/PartnerAddressLoggingAspect.java · public: logAnnotatedMethod, logControllerMethods, logServiceMethods, logRepositoryMethods, logAddressCreation, logAddressUpdate, logAddressDeletion
- `PartnerAddressPerformanceAspect` aop/PartnerAddressPerformanceAspect.java · public: monitorAnnotatedPerformance, monitorControllerPerformance, monitorServicePerformance, monitorRepositoryPerformance
- `PartnerAddressSecurityAspect` aop/PartnerAddressSecurityAspect.java · public: secureAnnotatedMethod

### other (5)

- `PartnerAddressAudit` (@interface) aop/annotation/PartnerAddressAudit.java
- `PartnerAddressExceptionHandling` (@interface) aop/annotation/PartnerAddressExceptionHandling.java
- `PartnerAddressLogging` (@interface) aop/annotation/PartnerAddressLogging.java
- `PartnerAddressPerformance` (@interface) aop/annotation/PartnerAddressPerformance.java
- `PartnerAddressSecurity` (@interface) aop/annotation/PartnerAddressSecurity.java

### 다른 도메인 참조 (import·주입 기준)

- **address**: `Address` ← PartnerAddressCommandService, PartnerAddressQueryService; `AddressRepository` ← PartnerAddressCommandService, PartnerAddressQueryService
- **user**: `User` ← PartnerAddressCommandService, PartnerAddressQueryService; `UserRepository` ← PartnerAddressCommandService, PartnerAddressQueryService

### 이 도메인을 참조하는 다른 도메인

- **partnerorder**: PartnerAddressQueryService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

- `PartnerAddressAuditAspect#auditAnnotatedMethod` @Around("@annotation(addressAudit)") · 바인딩: `@PartnerAddressAudit`
- `PartnerAddressAuditAspect#auditAddressCreation` @AfterReturning(pointcut = "execution(* com.fream.back.domain.partneraddress.service.command.PartnerAddressCommandService.createAddress(..))", returning = "result")
- `PartnerAddressAuditAspect#auditAddressUpdate` @AfterReturning(pointcut = "execution(* com.fream.back.domain.partneraddress.service.command.PartnerAddressCommandService.updateAddress(..))", returning = "result")
- `PartnerAddressAuditAspect#auditAddressDeletion` @AfterReturning(pointcut = "execution(* com.fream.back.domain.partneraddress.service.command.PartnerAddressCommandService.deleteAddress(..))", returning = "result")
- `PartnerAddressAuditAspect#auditAddressAccess` @AfterReturning(pointcut = "execution(* com.fream.back.domain.partneraddress.service.query.PartnerAddressQueryService.*(..))", returning = "result")
- `PartnerAddressAuditAspect#auditSecurityViolation` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.partneraddress..*(..))", throwing = "exception")
- `PartnerAddressExceptionAspect#handleAnnotatedException` @Around("@annotation(exceptionHandling)") · 바인딩: `@PartnerAddressExceptionHandling`
- `PartnerAddressExceptionAspect#handleServiceException` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.partneraddress.service..*(..))", throwing = "exception")
- `PartnerAddressExceptionAspect#handleControllerException` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.partneraddress.controller..*(..))", throwing = "exception")
- `PartnerAddressExceptionAspect#handleRepositoryException` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.partneraddress.repository..*(..))", throwing = "exception")
- `PartnerAddressLoggingAspect#logAnnotatedMethod` @Around("@annotation(addressLogging)") · 바인딩: `@PartnerAddressLogging`
- `PartnerAddressLoggingAspect#logControllerMethods` @Around("execution(* com.fream.back.domain.partneraddress.controller..*(..))")
- `PartnerAddressLoggingAspect#logServiceMethods` @Around("execution(* com.fream.back.domain.partneraddress.service..*(..))")
- `PartnerAddressLoggingAspect#logRepositoryMethods` @Around("execution(* com.fream.back.domain.partneraddress.repository..*(..))")
- `PartnerAddressLoggingAspect#logAddressCreation` @AfterReturning(pointcut = "execution(* com.fream.back.domain.partneraddress.service.command.PartnerAddressCommandService.createAddress(..))", returning = "result")
- `PartnerAddressLoggingAspect#logAddressUpdate` @AfterReturning(pointcut = "execution(* com.fream.back.domain.partneraddress.service.command.PartnerAddressCommandService.updateAddress(..))", returning = "result")
- `PartnerAddressLoggingAspect#logAddressDeletion` @AfterReturning(pointcut = "execution(* com.fream.back.domain.partneraddress.service.command.PartnerAddressCommandService.deleteAddress(..))", returning = "result")
- `PartnerAddressPerformanceAspect#monitorAnnotatedPerformance` @Around("@annotation(addressPerformance)") · 바인딩: `@PartnerAddressPerformance`
- `PartnerAddressPerformanceAspect#monitorControllerPerformance` @Around("execution(* com.fream.back.domain.partneraddress.controller..*(..))")
- `PartnerAddressPerformanceAspect#monitorServicePerformance` @Around("execution(* com.fream.back.domain.partneraddress.service..*(..))")
- `PartnerAddressPerformanceAspect#monitorRepositoryPerformance` @Around("execution(* com.fream.back.domain.partneraddress.repository..*(..))")
- `PartnerAddressSecurityAspect#secureAnnotatedMethod` @Around("@annotation(addressSecurity)") · 바인딩: `@PartnerAddressSecurity`

어노테이션 적용 지점(메서드에 붙은 곳):

- `@PartnerAddressAudit`: (없음)
- `@PartnerAddressExceptionHandling`: (없음)
- `@PartnerAddressLogging`: (없음)
- `@PartnerAddressPerformance`: (없음)
- `@PartnerAddressSecurity`: (없음)

