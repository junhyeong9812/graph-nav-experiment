# partnerfaq 도메인

기준 경로: `src/main/java/com/fream/back/domain/partnerfaq/` · 파일 18개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/partner/api/faq` | `PartnerFAQCommandController#createFAQ` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `PartnerFAQCommandService#createFAQ` |
| PUT | `/partner/api/faq/{id}` | `PartnerFAQCommandController#updateFAQ` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `PartnerFAQCommandService#updateFAQ` |
| DELETE | `/partner/api/faq/{id}` | `PartnerFAQCommandController#deleteFAQ` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `PartnerFAQCommandService#deleteFAQ` |
| GET | `/partner/api/faq` | `PartnerFAQQueryController#getFAQs` | `PartnerFAQQueryService#getFAQsByCategory`, `PartnerFAQQueryService#getFAQs` |
| GET | `/partner/api/faq/{id}` | `PartnerFAQQueryController#getFAQ` | `PartnerFAQQueryService#getFAQ` |
| GET | `/partner/api/faq/search` | `PartnerFAQQueryController#searchFAQs` | `PartnerFAQQueryService#searchFAQs` |
| GET | `/partner/api/faq/all` | `PartnerFAQQueryController#getAllFAQs` | `PartnerFAQQueryService#getAllFAQs` |
| GET | `/partner/api/faq/files/{faqId}/{fileName}` | `PartnerFAQQueryController#downloadFAQFile` |  |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `PartnerFAQAuditAspect`
  - `audit` → this#buildAuditContext, this#logAuditStart, this#processAuditSuccess, this#processAuditFailure
  - `buildAuditContext` → this#getClientIpAddress, this#extractParameters, this#determineEventType
  - `processAuditSuccess` → this#trackViewCount, this#collectCategoryStats, this#logAuditEvent, this#persistAuditLog, this#sendToExternalSystem, this#publishRealTimeEvent
  - `processAuditFailure` → this#alertCriticalEvent, this#persistAuditLog
  - `trackViewCount` → this#extractFaqId, this#updateViewCountInDB
  - `collectCategoryStats` → this#extractCategory
  - `persistAuditLog` → this#cleanOldAuditLogs
  - `extractParameters` → this#isSensitiveParam, this#maskValue
- `PartnerFAQCacheAspect`
  - `manageFAQCache` → this#generateCacheKey, this#determineCacheName, this#extractFaqId, this#updateAverageLoadTime, this#logCacheStatistics
  - `generateCacheKey` → this#extractFaqId, this#extractCategory, this#extractPageable, this#extractKeyword
  - `determineCacheName` → this#extractCategory
  - `getCacheStatistics` → this#getPopularFAQs
  - `warmUpCache` → this#getPopularFAQs
- `PartnerFAQCommandService`
  - `createFAQ` → FAQRepository#save~, this#processImagesAndUpdateHtml, PartnerFAQImageRepository#findAllByFaqId
  - `updateFAQ` → FAQRepository#findById~, PartnerFAQImageRepository#findAllByFaqId, this#deleteImages, this#processImagesAndUpdateHtml
  - `deleteFAQ` → FAQRepository#findById~, PartnerFAQImageRepository#findAllByFaqId, this#deleteImages, FileUtils#deleteDirectory, FAQRepository#delete~
  - `processImagesAndUpdateHtml` → FileUtils#saveFile, PartnerFAQImageRepository#save~, this#updateHtmlImageSources
  - `deleteImages` → FileUtils#deleteFile, PartnerFAQImageRepository#deleteAll~
- `PartnerFAQContentProcessingAspect`
  - `processContent` → this#processFAQCreateRequest, this#processFAQUpdateRequest, this#processOutput, this#handleProcessingFailure
  - `processFAQCreateRequest` → this#processHtmlContent, this#optimizeImages, this#generateSummary
  - `processFAQUpdateRequest` → this#processHtmlContent, this#optimizeImages
  - `processHtmlContent` → this#preventXSS, this#sanitizeWithWhitelist, this#optimizeImageTags, this#processLinks, this#truncateContent, this#handleContentError
  - `processLinks` → this#isValidUrl
  - `optimizeImages` → this#optimizeImage
  - `optimizeImage` → this#resizeImage, this#compressImage
- `PartnerFAQFileStorageUtil`
  - `saveFiles` → this#saveFileInternal
  - `saveFileInternal` → this#validateFileExtension
- `PartnerFAQQueryService`
  - `getFAQs` → FAQRepository#findAll~, PartnerFAQImageRepository#findAllByFaqId
  - `getFAQ` → FAQRepository#findById~, PartnerFAQImageRepository#findAllByFaqId
  - `getFAQsByCategory` → FAQRepository#findByCategory, PartnerFAQImageRepository#findAllByFaqId
  - `searchFAQs` → FAQRepository#searchFAQs, PartnerFAQImageRepository#findAllByFaqId
  - `getAllFAQs` → FAQRepository#findAll~, PartnerFAQImageRepository#findAllByFaqId
- `PartnerFAQSearchOptimizationAspect`
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
- `PartnerFAQValidationAspect`
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

- `PartnerFAQCommandController` controller/command/PartnerFAQCommandController.java · public: createFAQ, updateFAQ, deleteFAQ · 주입: PartnerFAQCommandService, PartnerUserQueryService
- `PartnerFAQQueryController` controller/query/PartnerFAQQueryController.java · public: getFAQs, getFAQ, searchFAQs, getAllFAQs, downloadFAQFile · 주입: PartnerFAQQueryService, FileUtils

### service-command (2)

- `PartnerFAQCommandService` service/command/PartnerFAQCommandService.java · public: createFAQ, updateFAQ, deleteFAQ · 주입: FAQRepository, PartnerFAQImageRepository, FileUtils
- `PartnerFAQFileStorageUtil` service/command/PartnerFAQFileStorageUtil.java · public: saveFiles, updateImagePaths, extractImagePaths, deleteFiles, hasFiles

### service-query (1)

- `PartnerFAQQueryService` service/query/PartnerFAQQueryService.java · public: getFAQs, getFAQ, getFAQsByCategory, searchFAQs, getAllFAQs · 주입: FAQRepository, PartnerFAQImageRepository

### repository (1)

- `PartnerFAQImageRepository` (interface) repository/PartnerFAQImageRepository.java · extends/implements JpaRepository · public: findAllByFaqId

### custom (2)

- `PartnerFAQRepositoryCustom` (interface) repository/PartnerFAQRepositoryCustom.java · public: searchFAQs
- `PartnerFAQRepositoryImpl` repository/PartnerFAQRepositoryImpl.java · extends/implements PartnerFAQRepositoryCustom · public: searchFAQs · 주입: JPAQueryFactory

### aop (5)

- `PartnerFAQAuditAspect` aop/PartnerFAQAuditAspect.java · public: audit · 주입: JdbcTemplate
- `PartnerFAQCacheAspect` aop/PartnerFAQCacheAspect.java · public: manageFAQCache, invalidateCategoryCache, invalidateAllCaches, invalidateFaqCache, getPopularFAQs, getCacheStatistics, warmUpCache · 주입: CacheManager
- `PartnerFAQContentProcessingAspect` aop/PartnerFAQContentProcessingAspect.java · public: processContent
- `PartnerFAQSearchOptimizationAspect` aop/PartnerFAQSearchOptimizationAspect.java · public: init, optimizeSearch, updatePopularKeywords · 주입: JdbcTemplate, CacheManager
- `PartnerFAQValidationAspect` aop/PartnerFAQValidationAspect.java · public: init, validationPointcut, validateFAQ · 주입: FAQRepository

### other (5)

- `PartnerFAQAudit` (@interface) aop/annotation/PartnerFAQAudit.java
- `PartnerFAQCache` (@interface) aop/annotation/PartnerFAQCache.java
- `PartnerFAQContentProcessing` (@interface) aop/annotation/PartnerFAQContentProcessing.java
- `PartnerFAQSearchOptimization` (@interface) aop/annotation/PartnerFAQSearchOptimization.java
- `PartnerFAQValidation` (@interface) aop/annotation/PartnerFAQValidation.java

### 다른 도메인 참조 (import·주입 기준)

- **faq**: `FAQ` ← PartnerFAQAuditAspect, PartnerFAQCommandService, PartnerFAQQueryService, PartnerFAQRepositoryCustom, PartnerFAQRepositoryImpl, PartnerFAQSearchOptimizationAspect; `FAQCategory` ← PartnerFAQAuditAspect, PartnerFAQCacheAspect, PartnerFAQQueryController, PartnerFAQQueryService, PartnerFAQValidationAspect; `FAQImage` ← PartnerFAQCommandService, PartnerFAQImageRepository, PartnerFAQQueryService; `FAQRepository` ← PartnerFAQCommandService, PartnerFAQQueryService, PartnerFAQValidationAspect
- **partneruser**: `PartnerUserQueryService` ← PartnerFAQCommandController

### 이 도메인을 참조하는 다른 도메인

- **partnerchatquestion**: PartnerFAQQueryService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

- `PartnerFAQAuditAspect#audit` @Around("@annotation(faqAudit)") · 바인딩: `@PartnerFAQAudit`
- `PartnerFAQCacheAspect#manageFAQCache` @Around("@annotation(faqCache)") · 바인딩: `@PartnerFAQCache`
- `PartnerFAQSearchOptimizationAspect#optimizeSearch` @Around("@annotation(searchOptimization)") · 바인딩: `@PartnerFAQSearchOptimization`
- `PartnerFAQValidationAspect#validationPointcut` @Pointcut("@annotation(com.fream.back.domain.partnerfaq.aop.annotation.PartnerFAQValidation)")
- `PartnerFAQValidationAspect#validateFAQ` @Around("validationPointcut() && @annotation(validation)") · 바인딩: `@PartnerFAQValidation`
- `PartnerFAQContentProcessingAspect#processContent` @Around("@annotation(contentProcessing)") · 바인딩: `@PartnerFAQContentProcessing`

어노테이션 적용 지점(메서드에 붙은 곳):

- `@PartnerFAQAudit`: (없음)
- `@PartnerFAQCache`: (없음)
- `@PartnerFAQContentProcessing`: (없음)
- `@PartnerFAQSearchOptimization`: (없음)
- `@PartnerFAQValidation`: (없음)

