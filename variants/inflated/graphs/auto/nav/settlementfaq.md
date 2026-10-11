# settlementfaq 도메인

기준 경로: `src/main/java/com/fream/back/domain/settlementfaq/` · 파일 18개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/settlement/api/faq` | `SettlementFAQCommandController#createFAQ` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `SettlementFAQCommandService#createFAQ` |
| PUT | `/settlement/api/faq/{id}` | `SettlementFAQCommandController#updateFAQ` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `SettlementFAQCommandService#updateFAQ` |
| DELETE | `/settlement/api/faq/{id}` | `SettlementFAQCommandController#deleteFAQ` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `SettlementFAQCommandService#deleteFAQ` |
| GET | `/settlement/api/faq` | `SettlementFAQQueryController#getFAQs` | `SettlementFAQQueryService#getFAQsByCategory`, `SettlementFAQQueryService#getFAQs` |
| GET | `/settlement/api/faq/{id}` | `SettlementFAQQueryController#getFAQ` | `SettlementFAQQueryService#getFAQ` |
| GET | `/settlement/api/faq/search` | `SettlementFAQQueryController#searchFAQs` | `SettlementFAQQueryService#searchFAQs` |
| GET | `/settlement/api/faq/all` | `SettlementFAQQueryController#getAllFAQs` | `SettlementFAQQueryService#getAllFAQs` |
| GET | `/settlement/api/faq/files/{faqId}/{fileName}` | `SettlementFAQQueryController#downloadFAQFile` |  |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `SettlementFAQAuditAspect`
  - `audit` → this#buildAuditContext, this#logAuditStart, this#processAuditSuccess, this#processAuditFailure
  - `buildAuditContext` → this#getClientIpAddress, this#extractParameters, this#determineEventType
  - `processAuditSuccess` → this#trackViewCount, this#collectCategoryStats, this#logAuditEvent, this#persistAuditLog, this#sendToExternalSystem, this#publishRealTimeEvent
  - `processAuditFailure` → this#alertCriticalEvent, this#persistAuditLog
  - `trackViewCount` → this#extractFaqId, this#updateViewCountInDB
  - `collectCategoryStats` → this#extractCategory
  - `persistAuditLog` → this#cleanOldAuditLogs
  - `extractParameters` → this#isSensitiveParam, this#maskValue
- `SettlementFAQCacheAspect`
  - `manageFAQCache` → this#generateCacheKey, this#determineCacheName, this#extractFaqId, this#updateAverageLoadTime, this#logCacheStatistics
  - `generateCacheKey` → this#extractFaqId, this#extractCategory, this#extractPageable, this#extractKeyword
  - `determineCacheName` → this#extractCategory
  - `getCacheStatistics` → this#getPopularFAQs
  - `warmUpCache` → this#getPopularFAQs
- `SettlementFAQCommandService`
  - `createFAQ` → FAQRepository#save~, this#processImagesAndUpdateHtml, SettlementFAQImageRepository#findAllByFaqId
  - `updateFAQ` → FAQRepository#findById~, SettlementFAQImageRepository#findAllByFaqId, this#deleteImages, this#processImagesAndUpdateHtml
  - `deleteFAQ` → FAQRepository#findById~, SettlementFAQImageRepository#findAllByFaqId, this#deleteImages, FileUtils#deleteDirectory, FAQRepository#delete~
  - `processImagesAndUpdateHtml` → FileUtils#saveFile, SettlementFAQImageRepository#save~, this#updateHtmlImageSources
  - `deleteImages` → FileUtils#deleteFile, SettlementFAQImageRepository#deleteAll~
- `SettlementFAQContentProcessingAspect`
  - `processContent` → this#processFAQCreateRequest, this#processFAQUpdateRequest, this#processOutput, this#handleProcessingFailure
  - `processFAQCreateRequest` → this#processHtmlContent, this#optimizeImages, this#generateSummary
  - `processFAQUpdateRequest` → this#processHtmlContent, this#optimizeImages
  - `processHtmlContent` → this#preventXSS, this#sanitizeWithWhitelist, this#optimizeImageTags, this#processLinks, this#truncateContent, this#handleContentError
  - `processLinks` → this#isValidUrl
  - `optimizeImages` → this#optimizeImage
  - `optimizeImage` → this#resizeImage, this#compressImage
- `SettlementFAQFileStorageUtil`
  - `saveFiles` → this#saveFileInternal
  - `saveFileInternal` → this#validateFileExtension
- `SettlementFAQQueryService`
  - `getFAQs` → FAQRepository#findAll~, SettlementFAQImageRepository#findAllByFaqId
  - `getFAQ` → FAQRepository#findById~, SettlementFAQImageRepository#findAllByFaqId
  - `getFAQsByCategory` → FAQRepository#findByCategory, SettlementFAQImageRepository#findAllByFaqId
  - `searchFAQs` → FAQRepository#searchFAQs, SettlementFAQImageRepository#findAllByFaqId
  - `getAllFAQs` → FAQRepository#findAll~, SettlementFAQImageRepository#findAllByFaqId
- `SettlementFAQSearchOptimizationAspect`
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
- `SettlementFAQValidationAspect`
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

- `SettlementFAQCommandController` controller/command/SettlementFAQCommandController.java · public: createFAQ, updateFAQ, deleteFAQ · 주입: SettlementFAQCommandService, SettlementUserQueryService
- `SettlementFAQQueryController` controller/query/SettlementFAQQueryController.java · public: getFAQs, getFAQ, searchFAQs, getAllFAQs, downloadFAQFile · 주입: SettlementFAQQueryService, FileUtils

### service-command (2)

- `SettlementFAQCommandService` service/command/SettlementFAQCommandService.java · public: createFAQ, updateFAQ, deleteFAQ · 주입: FAQRepository, SettlementFAQImageRepository, FileUtils
- `SettlementFAQFileStorageUtil` service/command/SettlementFAQFileStorageUtil.java · public: saveFiles, updateImagePaths, extractImagePaths, deleteFiles, hasFiles

### service-query (1)

- `SettlementFAQQueryService` service/query/SettlementFAQQueryService.java · public: getFAQs, getFAQ, getFAQsByCategory, searchFAQs, getAllFAQs · 주입: FAQRepository, SettlementFAQImageRepository

### repository (1)

- `SettlementFAQImageRepository` (interface) repository/SettlementFAQImageRepository.java · extends/implements JpaRepository · public: findAllByFaqId

### custom (2)

- `SettlementFAQRepositoryCustom` (interface) repository/SettlementFAQRepositoryCustom.java · public: searchFAQs
- `SettlementFAQRepositoryImpl` repository/SettlementFAQRepositoryImpl.java · extends/implements SettlementFAQRepositoryCustom · public: searchFAQs · 주입: JPAQueryFactory

### aop (5)

- `SettlementFAQAuditAspect` aop/SettlementFAQAuditAspect.java · public: audit · 주입: JdbcTemplate
- `SettlementFAQCacheAspect` aop/SettlementFAQCacheAspect.java · public: manageFAQCache, invalidateCategoryCache, invalidateAllCaches, invalidateFaqCache, getPopularFAQs, getCacheStatistics, warmUpCache · 주입: CacheManager
- `SettlementFAQContentProcessingAspect` aop/SettlementFAQContentProcessingAspect.java · public: processContent
- `SettlementFAQSearchOptimizationAspect` aop/SettlementFAQSearchOptimizationAspect.java · public: init, optimizeSearch, updatePopularKeywords · 주입: JdbcTemplate, CacheManager
- `SettlementFAQValidationAspect` aop/SettlementFAQValidationAspect.java · public: init, validationPointcut, validateFAQ · 주입: FAQRepository

### other (5)

- `SettlementFAQAudit` (@interface) aop/annotation/SettlementFAQAudit.java
- `SettlementFAQCache` (@interface) aop/annotation/SettlementFAQCache.java
- `SettlementFAQContentProcessing` (@interface) aop/annotation/SettlementFAQContentProcessing.java
- `SettlementFAQSearchOptimization` (@interface) aop/annotation/SettlementFAQSearchOptimization.java
- `SettlementFAQValidation` (@interface) aop/annotation/SettlementFAQValidation.java

### 다른 도메인 참조 (import·주입 기준)

- **faq**: `FAQ` ← SettlementFAQAuditAspect, SettlementFAQCommandService, SettlementFAQQueryService, SettlementFAQRepositoryCustom, SettlementFAQRepositoryImpl, SettlementFAQSearchOptimizationAspect; `FAQCategory` ← SettlementFAQAuditAspect, SettlementFAQCacheAspect, SettlementFAQQueryController, SettlementFAQQueryService, SettlementFAQValidationAspect; `FAQImage` ← SettlementFAQCommandService, SettlementFAQImageRepository, SettlementFAQQueryService; `FAQRepository` ← SettlementFAQCommandService, SettlementFAQQueryService, SettlementFAQValidationAspect
- **settlementuser**: `SettlementUserQueryService` ← SettlementFAQCommandController

### 이 도메인을 참조하는 다른 도메인

- **settlementchatquestion**: SettlementFAQQueryService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

- `SettlementFAQAuditAspect#audit` @Around("@annotation(faqAudit)") · 바인딩: `@SettlementFAQAudit`
- `SettlementFAQCacheAspect#manageFAQCache` @Around("@annotation(faqCache)") · 바인딩: `@SettlementFAQCache`
- `SettlementFAQSearchOptimizationAspect#optimizeSearch` @Around("@annotation(searchOptimization)") · 바인딩: `@SettlementFAQSearchOptimization`
- `SettlementFAQValidationAspect#validationPointcut` @Pointcut("@annotation(com.fream.back.domain.settlementfaq.aop.annotation.SettlementFAQValidation)")
- `SettlementFAQValidationAspect#validateFAQ` @Around("validationPointcut() && @annotation(validation)") · 바인딩: `@SettlementFAQValidation`
- `SettlementFAQContentProcessingAspect#processContent` @Around("@annotation(contentProcessing)") · 바인딩: `@SettlementFAQContentProcessing`

어노테이션 적용 지점(메서드에 붙은 곳):

- `@SettlementFAQAudit`: (없음)
- `@SettlementFAQCache`: (없음)
- `@SettlementFAQContentProcessing`: (없음)
- `@SettlementFAQSearchOptimization`: (없음)
- `@SettlementFAQValidation`: (없음)

