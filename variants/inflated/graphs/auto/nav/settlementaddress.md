# settlementaddress 도메인

기준 경로: `src/main/java/com/fream/back/domain/settlementaddress/` · 파일 14개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/settlement/addresses` | `SettlementAddressCommandController#createAddress` | `SettlementAddressCommandService#createAddress` |
| PUT | `/settlement/addresses` | `SettlementAddressCommandController#updateAddress` | `SettlementAddressCommandService#updateAddress` |
| DELETE | `/settlement/addresses/{addressId}` | `SettlementAddressCommandController#deleteAddress` | `SettlementAddressCommandService#deleteAddress` |
| GET | `/settlement/addresses` | `SettlementAddressQueryController#getAddresses` | `SettlementAddressQueryService#getAddresses` |
| GET | `/settlement/addresses/{addressId}` | `SettlementAddressQueryController#getAddress` | `SettlementAddressQueryService#getAddress` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `SettlementAddressAuditAspect`
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
- `SettlementAddressCommandService`
  - `createAddress` → UserRepository#findByEmail, PersonalDataEncryptionUtil#deterministicEncrypt, PersonalDataEncryptionUtil#encrypt, AddressRepository#save~
  - `updateAddress` → UserRepository#findByEmail, AddressRepository#findByIdAndUser, PersonalDataEncryptionUtil#deterministicEncrypt, PersonalDataEncryptionUtil#encrypt
  - `deleteAddress` → UserRepository#findByEmail, AddressRepository#findByIdAndUser, AddressRepository#delete~
- `SettlementAddressExceptionAspect`
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
- `SettlementAddressLoggingAspect`
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
- `SettlementAddressPerformanceAspect`
  - `monitorAnnotatedPerformance` → this#extractUserEmailSafely, this#getDefaultWarningThreshold, this#getDefaultErrorThreshold, this#executePerformanceMonitoring
  - `monitorControllerPerformance` → this#monitorMethodPerformance
  - `monitorServicePerformance` → this#monitorMethodPerformance
  - `monitorRepositoryPerformance` → this#monitorMethodPerformance
  - `executePerformanceMonitoring` → this#updatePerformanceStats, this#logAdvancedPerformanceMetrics, this#shouldGeneratePerformanceReport, this#generatePerformanceReport
  - `monitorMethodPerformance` → this#extractUserEmailSafely, this#updatePerformanceStats, this#logPerformanceMetrics, this#shouldGeneratePerformanceReport, this#generatePerformanceReport
- `SettlementAddressQueryService`
  - `getAddresses` → PersonalDataEncryptionUtil#deterministicEncrypt, UserRepository#findByEmail, PersonalDataEncryptionUtil#deterministicDecrypt, PersonalDataEncryptionUtil#decrypt
  - `getAddress` → PersonalDataEncryptionUtil#deterministicEncrypt, UserRepository#findByEmail, AddressRepository#findByIdAndUser, PersonalDataEncryptionUtil#deterministicDecrypt, PersonalDataEncryptionUtil#decrypt
  - `searchAddressesByRecipientName` → PersonalDataEncryptionUtil#deterministicEncrypt, UserRepository#findByEmail, PersonalDataEncryptionUtil#encryptForSearch, AddressRepository#findByUserAndRecipientName, PersonalDataEncryptionUtil#deterministicDecrypt, PersonalDataEncryptionUtil#decrypt
  - `searchAddressesByPhoneNumber` → PersonalDataEncryptionUtil#deterministicEncrypt, UserRepository#findByEmail, PersonalDataEncryptionUtil#encryptForSearch, AddressRepository#findByUserAndPhoneNumber, PersonalDataEncryptionUtil#deterministicDecrypt, PersonalDataEncryptionUtil#decrypt
- `SettlementAddressSecurityAspect`
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

- `SettlementAddressCommandController` controller/command/SettlementAddressCommandController.java · public: createAddress, updateAddress, deleteAddress · 주입: SettlementAddressCommandService
- `SettlementAddressQueryController` controller/query/SettlementAddressQueryController.java · public: getAddresses, getAddress · 주입: SettlementAddressQueryService

### service-command (1)

- `SettlementAddressCommandService` service/command/SettlementAddressCommandService.java · public: createAddress, updateAddress, deleteAddress · 주입: UserRepository, AddressRepository, PersonalDataEncryptionUtil

### service-query (1)

- `SettlementAddressQueryService` service/query/SettlementAddressQueryService.java · public: getAddresses, getAddress, searchAddressesByRecipientName, searchAddressesByPhoneNumber · 주입: UserRepository, AddressRepository, PersonalDataEncryptionUtil

### aop (5)

- `SettlementAddressAuditAspect` aop/SettlementAddressAuditAspect.java · public: auditAnnotatedMethod, auditAddressCreation, auditAddressUpdate, auditAddressDeletion, auditAddressAccess, auditSecurityViolation
- `SettlementAddressExceptionAspect` aop/SettlementAddressExceptionAspect.java · public: handleAnnotatedException, handleServiceException, handleControllerException, handleRepositoryException
- `SettlementAddressLoggingAspect` aop/SettlementAddressLoggingAspect.java · public: logAnnotatedMethod, logControllerMethods, logServiceMethods, logRepositoryMethods, logAddressCreation, logAddressUpdate, logAddressDeletion
- `SettlementAddressPerformanceAspect` aop/SettlementAddressPerformanceAspect.java · public: monitorAnnotatedPerformance, monitorControllerPerformance, monitorServicePerformance, monitorRepositoryPerformance
- `SettlementAddressSecurityAspect` aop/SettlementAddressSecurityAspect.java · public: secureAnnotatedMethod

### other (5)

- `SettlementAddressAudit` (@interface) aop/annotation/SettlementAddressAudit.java
- `SettlementAddressExceptionHandling` (@interface) aop/annotation/SettlementAddressExceptionHandling.java
- `SettlementAddressLogging` (@interface) aop/annotation/SettlementAddressLogging.java
- `SettlementAddressPerformance` (@interface) aop/annotation/SettlementAddressPerformance.java
- `SettlementAddressSecurity` (@interface) aop/annotation/SettlementAddressSecurity.java

### 다른 도메인 참조 (import·주입 기준)

- **address**: `Address` ← SettlementAddressCommandService, SettlementAddressQueryService; `AddressRepository` ← SettlementAddressCommandService, SettlementAddressQueryService
- **user**: `User` ← SettlementAddressCommandService, SettlementAddressQueryService; `UserRepository` ← SettlementAddressCommandService, SettlementAddressQueryService

### 이 도메인을 참조하는 다른 도메인

- **settlementorder**: SettlementAddressQueryService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

- `SettlementAddressAuditAspect#auditAnnotatedMethod` @Around("@annotation(addressAudit)") · 바인딩: `@SettlementAddressAudit`
- `SettlementAddressAuditAspect#auditAddressCreation` @AfterReturning(pointcut = "execution(* com.fream.back.domain.settlementaddress.service.command.SettlementAddressCommandService.createAddress(..))", returning = "result")
- `SettlementAddressAuditAspect#auditAddressUpdate` @AfterReturning(pointcut = "execution(* com.fream.back.domain.settlementaddress.service.command.SettlementAddressCommandService.updateAddress(..))", returning = "result")
- `SettlementAddressAuditAspect#auditAddressDeletion` @AfterReturning(pointcut = "execution(* com.fream.back.domain.settlementaddress.service.command.SettlementAddressCommandService.deleteAddress(..))", returning = "result")
- `SettlementAddressAuditAspect#auditAddressAccess` @AfterReturning(pointcut = "execution(* com.fream.back.domain.settlementaddress.service.query.SettlementAddressQueryService.*(..))", returning = "result")
- `SettlementAddressAuditAspect#auditSecurityViolation` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.settlementaddress..*(..))", throwing = "exception")
- `SettlementAddressExceptionAspect#handleAnnotatedException` @Around("@annotation(exceptionHandling)") · 바인딩: `@SettlementAddressExceptionHandling`
- `SettlementAddressExceptionAspect#handleServiceException` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.settlementaddress.service..*(..))", throwing = "exception")
- `SettlementAddressExceptionAspect#handleControllerException` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.settlementaddress.controller..*(..))", throwing = "exception")
- `SettlementAddressExceptionAspect#handleRepositoryException` @AfterThrowing(pointcut = "execution(* com.fream.back.domain.settlementaddress.repository..*(..))", throwing = "exception")
- `SettlementAddressLoggingAspect#logAnnotatedMethod` @Around("@annotation(addressLogging)") · 바인딩: `@SettlementAddressLogging`
- `SettlementAddressLoggingAspect#logControllerMethods` @Around("execution(* com.fream.back.domain.settlementaddress.controller..*(..))")
- `SettlementAddressLoggingAspect#logServiceMethods` @Around("execution(* com.fream.back.domain.settlementaddress.service..*(..))")
- `SettlementAddressLoggingAspect#logRepositoryMethods` @Around("execution(* com.fream.back.domain.settlementaddress.repository..*(..))")
- `SettlementAddressLoggingAspect#logAddressCreation` @AfterReturning(pointcut = "execution(* com.fream.back.domain.settlementaddress.service.command.SettlementAddressCommandService.createAddress(..))", returning = "result")
- `SettlementAddressLoggingAspect#logAddressUpdate` @AfterReturning(pointcut = "execution(* com.fream.back.domain.settlementaddress.service.command.SettlementAddressCommandService.updateAddress(..))", returning = "result")
- `SettlementAddressLoggingAspect#logAddressDeletion` @AfterReturning(pointcut = "execution(* com.fream.back.domain.settlementaddress.service.command.SettlementAddressCommandService.deleteAddress(..))", returning = "result")
- `SettlementAddressPerformanceAspect#monitorAnnotatedPerformance` @Around("@annotation(addressPerformance)") · 바인딩: `@SettlementAddressPerformance`
- `SettlementAddressPerformanceAspect#monitorControllerPerformance` @Around("execution(* com.fream.back.domain.settlementaddress.controller..*(..))")
- `SettlementAddressPerformanceAspect#monitorServicePerformance` @Around("execution(* com.fream.back.domain.settlementaddress.service..*(..))")
- `SettlementAddressPerformanceAspect#monitorRepositoryPerformance` @Around("execution(* com.fream.back.domain.settlementaddress.repository..*(..))")
- `SettlementAddressSecurityAspect#secureAnnotatedMethod` @Around("@annotation(addressSecurity)") · 바인딩: `@SettlementAddressSecurity`

어노테이션 적용 지점(메서드에 붙은 곳):

- `@SettlementAddressAudit`: (없음)
- `@SettlementAddressExceptionHandling`: (없음)
- `@SettlementAddressLogging`: (없음)
- `@SettlementAddressPerformance`: (없음)
- `@SettlementAddressSecurity`: (없음)

