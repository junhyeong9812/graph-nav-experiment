# legacyfaq 도메인

기준 경로: `src/main/java/com/fream/back/domain/legacyfaq/` · 파일 18개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/legacy/api/faq` | `LegacyFAQCommandController#createFAQ` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `LegacyFAQCommandService#createFAQ` |
| PUT | `/legacy/api/faq/{id}` | `LegacyFAQCommandController#updateFAQ` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `LegacyFAQCommandService#updateFAQ` |
| DELETE | `/legacy/api/faq/{id}` | `LegacyFAQCommandController#deleteFAQ` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `LegacyFAQCommandService#deleteFAQ` |
| GET | `/legacy/api/faq` | `LegacyFAQQueryController#getFAQs` | `LegacyFAQQueryService#getFAQsByCategory`, `LegacyFAQQueryService#getFAQs` |
| GET | `/legacy/api/faq/{id}` | `LegacyFAQQueryController#getFAQ` | `LegacyFAQQueryService#getFAQ` |
| GET | `/legacy/api/faq/search` | `LegacyFAQQueryController#searchFAQs` | `LegacyFAQQueryService#searchFAQs` |
| GET | `/legacy/api/faq/all` | `LegacyFAQQueryController#getAllFAQs` | `LegacyFAQQueryService#getAllFAQs` |
| GET | `/legacy/api/faq/files/{faqId}/{fileName}` | `LegacyFAQQueryController#downloadFAQFile` |  |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `LegacyFAQAuditAspect`
  - `audit` → this#buildAuditContext, this#logAuditStart, this#processAuditSuccess, this#processAuditFailure
  - `buildAuditContext` → this#getClientIpAddress, this#extractParameters, this#determineEventType
  - `processAuditSuccess` → this#trackViewCount, this#collectCategoryStats, this#logAuditEvent, this#persistAuditLog, this#sendToExternalSystem, this#publishRealTimeEvent
  - `processAuditFailure` → this#alertCriticalEvent, this#persistAuditLog
  - `trackViewCount` → this#extractFaqId, this#updateViewCountInDB
  - `collectCategoryStats` → this#extractCategory
  - `persistAuditLog` → this#cleanOldAuditLogs
  - `extractParameters` → this#isSensitiveParam, this#maskValue
- `LegacyFAQCacheAspect`
  - `manageFAQCache` → this#generateCacheKey, this#determineCacheName, this#extractFaqId, this#updateAverageLoadTime, this#logCacheStatistics
  - `generateCacheKey` → this#extractFaqId, this#extractCategory, this#extractPageable, this#extractKeyword
  - `determineCacheName` → this#extractCategory
  - `getCacheStatistics` → this#getPopularFAQs
  - `warmUpCache` → this#getPopularFAQs
- `LegacyFAQCommandService`
  - `createFAQ` → FAQRepository#save~, this#processImagesAndUpdateHtml, LegacyFAQImageRepository#findAllByFaqId
  - `updateFAQ` → FAQRepository#findById~, LegacyFAQImageRepository#findAllByFaqId, this#deleteImages, this#processImagesAndUpdateHtml
  - `deleteFAQ` → FAQRepository#findById~, LegacyFAQImageRepository#findAllByFaqId, this#deleteImages, FileUtils#deleteDirectory, FAQRepository#delete~
  - `processImagesAndUpdateHtml` → FileUtils#saveFile, LegacyFAQImageRepository#save~, this#updateHtmlImageSources
  - `deleteImages` → FileUtils#deleteFile, LegacyFAQImageRepository#deleteAll~
- `LegacyFAQContentProcessingAspect`
  - `processContent` → this#processFAQCreateRequest, this#processFAQUpdateRequest, this#processOutput, this#handleProcessingFailure
  - `processFAQCreateRequest` → this#processHtmlContent, this#optimizeImages, this#generateSummary
  - `processFAQUpdateRequest` → this#processHtmlContent, this#optimizeImages
  - `processHtmlContent` → this#preventXSS, this#sanitizeWithWhitelist, this#optimizeImageTags, this#processLinks, this#truncateContent, this#handleContentError
  - `processLinks` → this#isValidUrl
  - `optimizeImages` → this#optimizeImage
  - `optimizeImage` → this#resizeImage, this#compressImage
- `LegacyFAQFileStorageUtil`
  - `saveFiles` → this#saveFileInternal
  - `saveFileInternal` → this#validateFileExtension
- `LegacyFAQQueryService`
  - `getFAQs` → FAQRepository#findAll~, LegacyFAQImageRepository#findAllByFaqId
  - `getFAQ` → FAQRepository#findById~, LegacyFAQImageRepository#findAllByFaqId
  - `getFAQsByCategory` → FAQRepository#findByCategory, LegacyFAQImageRepository#findAllByFaqId
  - `searchFAQs` → FAQRepository#searchFAQs, LegacyFAQImageRepository#findAllByFaqId
  - `getAllFAQs` → FAQRepository#findAll~, LegacyFAQImageRepository#findAllByFaqId
- `LegacyFAQSearchOptimizationAspect`
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
- `LegacyFAQValidationAspect`
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

- `LegacyFAQCommandController` controller/command/LegacyFAQCommandController.java · public: createFAQ, updateFAQ, deleteFAQ · 주입: LegacyFAQCommandService, LegacyUserQueryService
- `LegacyFAQQueryController` controller/query/LegacyFAQQueryController.java · public: getFAQs, getFAQ, searchFAQs, getAllFAQs, downloadFAQFile · 주입: LegacyFAQQueryService, FileUtils

### service-command (2)

- `LegacyFAQCommandService` service/command/LegacyFAQCommandService.java · public: createFAQ, updateFAQ, deleteFAQ · 주입: FAQRepository, LegacyFAQImageRepository, FileUtils
- `LegacyFAQFileStorageUtil` service/command/LegacyFAQFileStorageUtil.java · public: saveFiles, updateImagePaths, extractImagePaths, deleteFiles, hasFiles

### service-query (1)

- `LegacyFAQQueryService` service/query/LegacyFAQQueryService.java · public: getFAQs, getFAQ, getFAQsByCategory, searchFAQs, getAllFAQs · 주입: FAQRepository, LegacyFAQImageRepository

### repository (1)

- `LegacyFAQImageRepository` (interface) repository/LegacyFAQImageRepository.java · extends/implements JpaRepository · public: findAllByFaqId

### custom (2)

- `LegacyFAQRepositoryCustom` (interface) repository/LegacyFAQRepositoryCustom.java · public: searchFAQs
- `LegacyFAQRepositoryImpl` repository/LegacyFAQRepositoryImpl.java · extends/implements LegacyFAQRepositoryCustom · public: searchFAQs · 주입: JPAQueryFactory

### aop (5)

- `LegacyFAQAuditAspect` aop/LegacyFAQAuditAspect.java · public: audit · 주입: JdbcTemplate
- `LegacyFAQCacheAspect` aop/LegacyFAQCacheAspect.java · public: manageFAQCache, invalidateCategoryCache, invalidateAllCaches, invalidateFaqCache, getPopularFAQs, getCacheStatistics, warmUpCache · 주입: CacheManager
- `LegacyFAQContentProcessingAspect` aop/LegacyFAQContentProcessingAspect.java · public: processContent
- `LegacyFAQSearchOptimizationAspect` aop/LegacyFAQSearchOptimizationAspect.java · public: init, optimizeSearch, updatePopularKeywords · 주입: JdbcTemplate, CacheManager
- `LegacyFAQValidationAspect` aop/LegacyFAQValidationAspect.java · public: init, validationPointcut, validateFAQ · 주입: FAQRepository

### other (5)

- `LegacyFAQAudit` (@interface) aop/annotation/LegacyFAQAudit.java
- `LegacyFAQCache` (@interface) aop/annotation/LegacyFAQCache.java
- `LegacyFAQContentProcessing` (@interface) aop/annotation/LegacyFAQContentProcessing.java
- `LegacyFAQSearchOptimization` (@interface) aop/annotation/LegacyFAQSearchOptimization.java
- `LegacyFAQValidation` (@interface) aop/annotation/LegacyFAQValidation.java

### 다른 도메인 참조 (import·주입 기준)

- **faq**: `FAQ` ← LegacyFAQAuditAspect, LegacyFAQCommandService, LegacyFAQQueryService, LegacyFAQRepositoryCustom, LegacyFAQRepositoryImpl, LegacyFAQSearchOptimizationAspect; `FAQCategory` ← LegacyFAQAuditAspect, LegacyFAQCacheAspect, LegacyFAQQueryController, LegacyFAQQueryService, LegacyFAQValidationAspect; `FAQImage` ← LegacyFAQCommandService, LegacyFAQImageRepository, LegacyFAQQueryService; `FAQRepository` ← LegacyFAQCommandService, LegacyFAQQueryService, LegacyFAQValidationAspect
- **legacyuser**: `LegacyUserQueryService` ← LegacyFAQCommandController

### 이 도메인을 참조하는 다른 도메인

- **legacychatquestion**: LegacyFAQQueryService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

- `LegacyFAQAuditAspect#audit` @Around("@annotation(faqAudit)") · 바인딩: `@LegacyFAQAudit`
- `LegacyFAQCacheAspect#manageFAQCache` @Around("@annotation(faqCache)") · 바인딩: `@LegacyFAQCache`
- `LegacyFAQSearchOptimizationAspect#optimizeSearch` @Around("@annotation(searchOptimization)") · 바인딩: `@LegacyFAQSearchOptimization`
- `LegacyFAQValidationAspect#validationPointcut` @Pointcut("@annotation(com.fream.back.domain.legacyfaq.aop.annotation.LegacyFAQValidation)")
- `LegacyFAQValidationAspect#validateFAQ` @Around("validationPointcut() && @annotation(validation)") · 바인딩: `@LegacyFAQValidation`
- `LegacyFAQContentProcessingAspect#processContent` @Around("@annotation(contentProcessing)") · 바인딩: `@LegacyFAQContentProcessing`

어노테이션 적용 지점(메서드에 붙은 곳):

- `@LegacyFAQAudit`: (없음)
- `@LegacyFAQCache`: (없음)
- `@LegacyFAQContentProcessing`: (없음)
- `@LegacyFAQSearchOptimization`: (없음)
- `@LegacyFAQValidation`: (없음)

