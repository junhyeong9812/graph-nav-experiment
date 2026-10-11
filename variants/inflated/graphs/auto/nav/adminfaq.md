# adminfaq 도메인

기준 경로: `src/main/java/com/fream/back/domain/adminfaq/` · 파일 18개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/admin/api/faq` | `AdminFAQCommandController#createFAQ` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `AdminFAQCommandService#createFAQ` |
| PUT | `/admin/api/faq/{id}` | `AdminFAQCommandController#updateFAQ` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `AdminFAQCommandService#updateFAQ` |
| DELETE | `/admin/api/faq/{id}` | `AdminFAQCommandController#deleteFAQ` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `AdminFAQCommandService#deleteFAQ` |
| GET | `/admin/api/faq` | `AdminFAQQueryController#getFAQs` | `AdminFAQQueryService#getFAQsByCategory`, `AdminFAQQueryService#getFAQs` |
| GET | `/admin/api/faq/{id}` | `AdminFAQQueryController#getFAQ` | `AdminFAQQueryService#getFAQ` |
| GET | `/admin/api/faq/search` | `AdminFAQQueryController#searchFAQs` | `AdminFAQQueryService#searchFAQs` |
| GET | `/admin/api/faq/all` | `AdminFAQQueryController#getAllFAQs` | `AdminFAQQueryService#getAllFAQs` |
| GET | `/admin/api/faq/files/{faqId}/{fileName}` | `AdminFAQQueryController#downloadFAQFile` |  |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `AdminFAQAuditAspect`
  - `audit` → this#buildAuditContext, this#logAuditStart, this#processAuditSuccess, this#processAuditFailure
  - `buildAuditContext` → this#getClientIpAddress, this#extractParameters, this#determineEventType
  - `processAuditSuccess` → this#trackViewCount, this#collectCategoryStats, this#logAuditEvent, this#persistAuditLog, this#sendToExternalSystem, this#publishRealTimeEvent
  - `processAuditFailure` → this#alertCriticalEvent, this#persistAuditLog
  - `trackViewCount` → this#extractFaqId, this#updateViewCountInDB
  - `collectCategoryStats` → this#extractCategory
  - `persistAuditLog` → this#cleanOldAuditLogs
  - `extractParameters` → this#isSensitiveParam, this#maskValue
- `AdminFAQCacheAspect`
  - `manageFAQCache` → this#generateCacheKey, this#determineCacheName, this#extractFaqId, this#updateAverageLoadTime, this#logCacheStatistics
  - `generateCacheKey` → this#extractFaqId, this#extractCategory, this#extractPageable, this#extractKeyword
  - `determineCacheName` → this#extractCategory
  - `getCacheStatistics` → this#getPopularFAQs
  - `warmUpCache` → this#getPopularFAQs
- `AdminFAQCommandService`
  - `createFAQ` → FAQRepository#save~, this#processImagesAndUpdateHtml, AdminFAQImageRepository#findAllByFaqId
  - `updateFAQ` → FAQRepository#findById~, AdminFAQImageRepository#findAllByFaqId, this#deleteImages, this#processImagesAndUpdateHtml
  - `deleteFAQ` → FAQRepository#findById~, AdminFAQImageRepository#findAllByFaqId, this#deleteImages, FileUtils#deleteDirectory, FAQRepository#delete~
  - `processImagesAndUpdateHtml` → FileUtils#saveFile, AdminFAQImageRepository#save~, this#updateHtmlImageSources
  - `deleteImages` → FileUtils#deleteFile, AdminFAQImageRepository#deleteAll~
- `AdminFAQContentProcessingAspect`
  - `processContent` → this#processFAQCreateRequest, this#processFAQUpdateRequest, this#processOutput, this#handleProcessingFailure
  - `processFAQCreateRequest` → this#processHtmlContent, this#optimizeImages, this#generateSummary
  - `processFAQUpdateRequest` → this#processHtmlContent, this#optimizeImages
  - `processHtmlContent` → this#preventXSS, this#sanitizeWithWhitelist, this#optimizeImageTags, this#processLinks, this#truncateContent, this#handleContentError
  - `processLinks` → this#isValidUrl
  - `optimizeImages` → this#optimizeImage
  - `optimizeImage` → this#resizeImage, this#compressImage
- `AdminFAQFileStorageUtil`
  - `saveFiles` → this#saveFileInternal
  - `saveFileInternal` → this#validateFileExtension
- `AdminFAQQueryService`
  - `getFAQs` → FAQRepository#findAll~, AdminFAQImageRepository#findAllByFaqId
  - `getFAQ` → FAQRepository#findById~, AdminFAQImageRepository#findAllByFaqId
  - `getFAQsByCategory` → FAQRepository#findByCategory, AdminFAQImageRepository#findAllByFaqId
  - `searchFAQs` → FAQRepository#searchFAQs, AdminFAQImageRepository#findAllByFaqId
  - `getAllFAQs` → FAQRepository#findAll~, AdminFAQImageRepository#findAllByFaqId
- `AdminFAQSearchOptimizationAspect`
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
- `AdminFAQValidationAspect`
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

- `AdminFAQCommandController` controller/command/AdminFAQCommandController.java · public: createFAQ, updateFAQ, deleteFAQ · 주입: AdminFAQCommandService, AdminUserQueryService
- `AdminFAQQueryController` controller/query/AdminFAQQueryController.java · public: getFAQs, getFAQ, searchFAQs, getAllFAQs, downloadFAQFile · 주입: AdminFAQQueryService, FileUtils

### service-command (2)

- `AdminFAQCommandService` service/command/AdminFAQCommandService.java · public: createFAQ, updateFAQ, deleteFAQ · 주입: FAQRepository, AdminFAQImageRepository, FileUtils
- `AdminFAQFileStorageUtil` service/command/AdminFAQFileStorageUtil.java · public: saveFiles, updateImagePaths, extractImagePaths, deleteFiles, hasFiles

### service-query (1)

- `AdminFAQQueryService` service/query/AdminFAQQueryService.java · public: getFAQs, getFAQ, getFAQsByCategory, searchFAQs, getAllFAQs · 주입: FAQRepository, AdminFAQImageRepository

### repository (1)

- `AdminFAQImageRepository` (interface) repository/AdminFAQImageRepository.java · extends/implements JpaRepository · public: findAllByFaqId

### custom (2)

- `AdminFAQRepositoryCustom` (interface) repository/AdminFAQRepositoryCustom.java · public: searchFAQs
- `AdminFAQRepositoryImpl` repository/AdminFAQRepositoryImpl.java · extends/implements AdminFAQRepositoryCustom · public: searchFAQs · 주입: JPAQueryFactory

### aop (5)

- `AdminFAQAuditAspect` aop/AdminFAQAuditAspect.java · public: audit · 주입: JdbcTemplate
- `AdminFAQCacheAspect` aop/AdminFAQCacheAspect.java · public: manageFAQCache, invalidateCategoryCache, invalidateAllCaches, invalidateFaqCache, getPopularFAQs, getCacheStatistics, warmUpCache · 주입: CacheManager
- `AdminFAQContentProcessingAspect` aop/AdminFAQContentProcessingAspect.java · public: processContent
- `AdminFAQSearchOptimizationAspect` aop/AdminFAQSearchOptimizationAspect.java · public: init, optimizeSearch, updatePopularKeywords · 주입: JdbcTemplate, CacheManager
- `AdminFAQValidationAspect` aop/AdminFAQValidationAspect.java · public: init, validationPointcut, validateFAQ · 주입: FAQRepository

### other (5)

- `AdminFAQAudit` (@interface) aop/annotation/AdminFAQAudit.java
- `AdminFAQCache` (@interface) aop/annotation/AdminFAQCache.java
- `AdminFAQContentProcessing` (@interface) aop/annotation/AdminFAQContentProcessing.java
- `AdminFAQSearchOptimization` (@interface) aop/annotation/AdminFAQSearchOptimization.java
- `AdminFAQValidation` (@interface) aop/annotation/AdminFAQValidation.java

### 다른 도메인 참조 (import·주입 기준)

- **adminuser**: `AdminUserQueryService` ← AdminFAQCommandController
- **faq**: `FAQ` ← AdminFAQAuditAspect, AdminFAQCommandService, AdminFAQQueryService, AdminFAQRepositoryCustom, AdminFAQRepositoryImpl, AdminFAQSearchOptimizationAspect; `FAQCategory` ← AdminFAQAuditAspect, AdminFAQCacheAspect, AdminFAQQueryController, AdminFAQQueryService, AdminFAQValidationAspect; `FAQImage` ← AdminFAQCommandService, AdminFAQImageRepository, AdminFAQQueryService; `FAQRepository` ← AdminFAQCommandService, AdminFAQQueryService, AdminFAQValidationAspect

### 이 도메인을 참조하는 다른 도메인

- **adminchatquestion**: AdminFAQQueryService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

(없음)

## 6. AOP

- `AdminFAQAuditAspect#audit` @Around("@annotation(faqAudit)") · 바인딩: `@AdminFAQAudit`
- `AdminFAQCacheAspect#manageFAQCache` @Around("@annotation(faqCache)") · 바인딩: `@AdminFAQCache`
- `AdminFAQSearchOptimizationAspect#optimizeSearch` @Around("@annotation(searchOptimization)") · 바인딩: `@AdminFAQSearchOptimization`
- `AdminFAQValidationAspect#validationPointcut` @Pointcut("@annotation(com.fream.back.domain.adminfaq.aop.annotation.AdminFAQValidation)")
- `AdminFAQValidationAspect#validateFAQ` @Around("validationPointcut() && @annotation(validation)") · 바인딩: `@AdminFAQValidation`
- `AdminFAQContentProcessingAspect#processContent` @Around("@annotation(contentProcessing)") · 바인딩: `@AdminFAQContentProcessing`

어노테이션 적용 지점(메서드에 붙은 곳):

- `@AdminFAQAudit`: (없음)
- `@AdminFAQCache`: (없음)
- `@AdminFAQContentProcessing`: (없음)
- `@AdminFAQSearchOptimization`: (없음)
- `@AdminFAQValidation`: (없음)

