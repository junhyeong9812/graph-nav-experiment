# internalfaq 도메인

기준 경로: `src/main/java/com/fream/back/domain/internalfaq/` · 파일 18개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/internal/api/faq` | `InternalFAQCommandController#createFAQ` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `InternalFAQCommandService#createFAQ` |
| PUT | `/internal/api/faq/{id}` | `InternalFAQCommandController#updateFAQ` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `InternalFAQCommandService#updateFAQ` |
| DELETE | `/internal/api/faq/{id}` | `InternalFAQCommandController#deleteFAQ` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `InternalFAQCommandService#deleteFAQ` |
| GET | `/internal/api/faq` | `InternalFAQQueryController#getFAQs` | `InternalFAQQueryService#getFAQsByCategory`, `InternalFAQQueryService#getFAQs` |
| GET | `/internal/api/faq/{id}` | `InternalFAQQueryController#getFAQ` | `InternalFAQQueryService#getFAQ` |
| GET | `/internal/api/faq/search` | `InternalFAQQueryController#searchFAQs` | `InternalFAQQueryService#searchFAQs` |
| GET | `/internal/api/faq/all` | `InternalFAQQueryController#getAllFAQs` | `InternalFAQQueryService#getAllFAQs` |
| GET | `/internal/api/faq/files/{faqId}/{fileName}` | `InternalFAQQueryController#downloadFAQFile` |  |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `InternalFAQAuditAspect`
  - `audit` → this#buildAuditContext, this#logAuditStart, this#processAuditSuccess, this#processAuditFailure
  - `buildAuditContext` → this#getClientIpAddress, this#extractParameters, this#determineEventType
  - `processAuditSuccess` → this#trackViewCount, this#collectCategoryStats, this#logAuditEvent, this#persistAuditLog, this#sendToExternalSystem, this#publishRealTimeEvent
  - `processAuditFailure` → this#alertCriticalEvent, this#persistAuditLog
  - `trackViewCount` → this#extractFaqId, this#updateViewCountInDB
  - `collectCategoryStats` → this#extractCategory
  - `persistAuditLog` → this#cleanOldAuditLogs
  - `extractParameters` → this#isSensitiveParam, this#maskValue
- `InternalFAQCacheAspect`
  - `manageFAQCache` → this#generateCacheKey, this#determineCacheName, this#extractFaqId, this#updateAverageLoadTime, this#logCacheStatistics
  - `generateCacheKey` → this#extractFaqId, this#extractCategory, this#extractPageable, this#extractKeyword
  - `determineCacheName` → this#extractCategory
  - `getCacheStatistics` → this#getPopularFAQs
  - `warmUpCache` → this#getPopularFAQs
- `InternalFAQCommandService`
  - `createFAQ` → FAQRepository#save~, this#processImagesAndUpdateHtml, InternalFAQImageRepository#findAllByFaqId
  - `updateFAQ` → FAQRepository#findById~, InternalFAQImageRepository#findAllByFaqId, this#deleteImages, this#processImagesAndUpdateHtml
  - `deleteFAQ` → FAQRepository#findById~, InternalFAQImageRepository#findAllByFaqId, this#deleteImages, FileUtils#deleteDirectory, FAQRepository#delete~
  - `processImagesAndUpdateHtml` → FileUtils#saveFile, InternalFAQImageRepository#save~, this#updateHtmlImageSources
  - `deleteImages` → FileUtils#deleteFile, InternalFAQImageRepository#deleteAll~
- `InternalFAQContentProcessingAspect`
  - `processContent` → this#processFAQCreateRequest, this#processFAQUpdateRequest, this#processOutput, this#handleProcessingFailure
  - `processFAQCreateRequest` → this#processHtmlContent, this#optimizeImages, this#generateSummary
  - `processFAQUpdateRequest` → this#processHtmlContent, this#optimizeImages
  - `processHtmlContent` → this#preventXSS, this#sanitizeWithWhitelist, this#optimizeImageTags, this#processLinks, this#truncateContent, this#handleContentError
  - `processLinks` → this#isValidUrl
  - `optimizeImages` → this#optimizeImage
  - `optimizeImage` → this#resizeImage, this#compressImage
- `InternalFAQFileStorageUtil`
  - `saveFiles` → this#saveFileInternal
  - `saveFileInternal` → this#validateFileExtension
- `InternalFAQQueryService`
  - `getFAQs` → FAQRepository#findAll~, InternalFAQImageRepository#findAllByFaqId
  - `getFAQ` → FAQRepository#findById~, InternalFAQImageRepository#findAllByFaqId
  - `getFAQsByCategory` → FAQRepository#findByCategory, InternalFAQImageRepository#findAllByFaqId
  - `searchFAQs` → FAQRepository#searchFAQs, InternalFAQImageRepository#findAllByFaqId
  - `getAllFAQs` → FAQRepository#findAll~, InternalFAQImageRepository#findAllByFaqId
- `InternalFAQSearchOptimizationAspect`
  - `init` → this#initializeSynonymDictionary, this#initializeTypoCorrections, this#loadPopularKeywords
  - `optimizeSearch` → this#extractKeyword, this#getCachedResult, this#optimizeKeyword, this#modifyArgs, this#processSearchResult, this#logSearchQuery, this#cacheSearchResult, this#trackKeyword
  - `optimizeKeyword` → this#normalizeKeyword, this#removeSpecialCharacters, this#normalizeKorean, this#correctTypo, this#expandWithSynonyms, this#isChosungQuery, this#convertChosungToRegex
  - `processSearchResult` → this#getSuggestions, this#filterByScore, this#sortResults
  - `correctTypo` → this#calculateLevenshteinDistance
  - `filterByScore` → this#calculateScore
  - `sortResults` → this#sortByPopularity, this#sortByRecent, this#sortByAlphabetical
  - `sortByPopularity` → this#getViewCount
  - `sortByRecent` → this#getCreatedDate
  - `cacheSearchResult` → this#cleanOldCache
  - `updatePopularKeywords` → this#loadPopularKeywords
- `InternalFAQValidationAspect`
  - `init` → this#initializeProhibitedWords
  - `validateFAQ` → this#createValidationContext, this#handleValidationFailure, this#performValidation, this#performAsyncValidation, this#cleanValidationCache
  - `performValidation` → this#validateByType, this#validateAdminPermission
  - `validateByType` → this#extractQuestion, this#extractAnswer, this#extractFiles, this#extractCategory, this#validateContent, this#validateLength, this#validateFiles, this#validateCategory, this#checkProhibitedWords, this#checkHtmlInjection, this#checkSqlInjection, this#checkDuplicate
  - `validateFiles` → this#getFileExtension, this#validateFileSignature
  - `checkDuplicate` → this#extractIdFromContext, this#checkQuestionExists
  - `handleValidationFailure` → this#sanitizeAndProceed
  - `sanitizeAndProceed` → this#sanitizeText, this#sanitizeHtml
  - `checkQuestionExists` → FAQRepository#findAll~
  - `initializeProhibitedWords` → this#loadProhibitedWordsFromDatabase

## 3. 클래스 목록 (역할별)

### controller (2)

- `InternalFAQCommandController` controller/command/InternalFAQCommandController.java · public: createFAQ, updateFAQ, deleteFAQ · 주입: InternalFAQCommandService, InternalUserQueryService
- `InternalFAQQueryController` controller/query/InternalFAQQueryController.java · public: getFAQs, getFAQ, searchFAQs, getAllFAQs, downloadFAQFile · 주입: InternalFAQQueryService, FileUtils

### service-command (2)

- `InternalFAQCommandService` service/command/InternalFAQCommandService.java · public: createFAQ, updateFAQ, deleteFAQ · 주입: FAQRepository, InternalFAQImageRepository, FileUtils
- `InternalFAQFileStorageUtil` service/command/InternalFAQFileStorageUtil.java · public: saveFiles, updateImagePaths, extractImagePaths, deleteFiles, hasFiles

### service-query (1)

- `InternalFAQQueryService` service/query/InternalFAQQueryService.java · public: getFAQs, getFAQ, getFAQsByCategory, searchFAQs, getAllFAQs · 주입: FAQRepository, InternalFAQImageRepository

### repository (1)

- `InternalFAQImageRepository` (interface) repository/InternalFAQImageRepository.java · extends/implements JpaRepository · public: findAllByFaqId

### custom (2)

- `InternalFAQRepositoryCustom` (interface) repository/InternalFAQRepositoryCustom.java · public: searchFAQs
- `InternalFAQRepositoryImpl` repository/InternalFAQRepositoryImpl.java · extends/implements InternalFAQRepositoryCustom · public: searchFAQs · 주입: JPAQueryFactory

### aop (5)

- `InternalFAQAuditAspect` aop/InternalFAQAuditAspect.java · public: audit · 주입: JdbcTemplate
- `InternalFAQCacheAspect` aop/InternalFAQCacheAspect.java · public: manageFAQCache, invalidateCategoryCache, invalidateAllCaches, invalidateFaqCache, getPopularFAQs, getCacheStatistics, warmUpCache · 주입: CacheManager
- `InternalFAQContentProcessingAspect` aop/InternalFAQContentProcessingAspect.java · public: processContent
- `InternalFAQSearchOptimizationAspect` aop/InternalFAQSearchOptimizationAspect.java · public: init, optimizeSearch, updatePopularKeywords · 주입: JdbcTemplate, CacheManager
- `InternalFAQValidationAspect` aop/InternalFAQValidationAspect.java · public: init, validationPointcut, validateFAQ · 주입: FAQRepository

### other (5)

- `InternalFAQAudit` (@interface) aop/annotation/InternalFAQAudit.java
- `InternalFAQCache` (@interface) aop/annotation/InternalFAQCache.java
- `InternalFAQContentProcessing` (@interface) aop/annotation/InternalFAQContentProcessing.java
- `InternalFAQSearchOptimization` (@interface) aop/annotation/InternalFAQSearchOptimization.java
- `InternalFAQValidation` (@interface) aop/annotation/InternalFAQValidation.java

### 다른 도메인 참조 (import·주입 기준)

- **faq**: `FAQ` ← InternalFAQAuditAspect, InternalFAQCommandService, InternalFAQQueryService, InternalFAQRepositoryCustom, InternalFAQRepositoryImpl, InternalFAQSearchOptimizationAspect; `FAQCategory` ← InternalFAQAuditAspect, InternalFAQCacheAspect, InternalFAQQueryController, InternalFAQQueryService, InternalFAQValidationAspect; `FAQImage` ← InternalFAQCommandService, InternalFAQImageRepository, InternalFAQQueryService; `FAQRepository` ← InternalFAQCommandService, InternalFAQQueryService, InternalFAQValidationAspect
- **internaluser**: `InternalUserQueryService` ← InternalFAQCommandController

### 이 도메인을 참조하는 다른 도메인

- **internalchatquestion**: InternalFAQQueryService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

- `InternalFAQAuditAspect#audit` @Around("@annotation(faqAudit)") · 바인딩: `@InternalFAQAudit`
- `InternalFAQCacheAspect#manageFAQCache` @Around("@annotation(faqCache)") · 바인딩: `@InternalFAQCache`
- `InternalFAQSearchOptimizationAspect#optimizeSearch` @Around("@annotation(searchOptimization)") · 바인딩: `@InternalFAQSearchOptimization`
- `InternalFAQValidationAspect#validationPointcut` @Pointcut("@annotation(com.fream.back.domain.internalfaq.aop.annotation.InternalFAQValidation)")
- `InternalFAQValidationAspect#validateFAQ` @Around("validationPointcut() && @annotation(validation)") · 바인딩: `@InternalFAQValidation`
- `InternalFAQContentProcessingAspect#processContent` @Around("@annotation(contentProcessing)") · 바인딩: `@InternalFAQContentProcessing`

어노테이션 적용 지점(메서드에 붙은 곳):

- `@InternalFAQAudit`: (없음)
- `@InternalFAQCache`: (없음)
- `@InternalFAQContentProcessing`: (없음)
- `@InternalFAQSearchOptimization`: (없음)
- `@InternalFAQValidation`: (없음)

