# faq 도메인

기준 경로: `src/main/java/com/fream/back/domain/faq/` · 파일 30개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/api/faq` | `FAQCommandController#createFAQ` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `FAQCommandService#createFAQ` |
| PUT | `/api/faq/{id}` | `FAQCommandController#updateFAQ` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `FAQCommandService#updateFAQ` |
| DELETE | `/api/faq/{id}` | `FAQCommandController#deleteFAQ` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `FAQCommandService#deleteFAQ` |
| GET | `/api/faq` | `FAQQueryController#getFAQs` | `FAQQueryService#getFAQsByCategory`, `FAQQueryService#getFAQs` |
| GET | `/api/faq/{id}` | `FAQQueryController#getFAQ` | `FAQQueryService#getFAQ` |
| GET | `/api/faq/search` | `FAQQueryController#searchFAQs` | `FAQQueryService#searchFAQs` |
| GET | `/api/faq/all` | `FAQQueryController#getAllFAQs` | `FAQQueryService#getAllFAQs` |
| GET | `/api/faq/files/{faqId}/{fileName}` | `FAQQueryController#downloadFAQFile` |  |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `FAQAuditAspect`
  - `audit` → this#buildAuditContext, this#logAuditStart, this#processAuditSuccess, this#processAuditFailure
  - `buildAuditContext` → this#getClientIpAddress, this#extractParameters, this#determineEventType
  - `processAuditSuccess` → this#trackViewCount, this#collectCategoryStats, this#logAuditEvent, this#persistAuditLog, this#sendToExternalSystem, this#publishRealTimeEvent
  - `processAuditFailure` → this#alertCriticalEvent, this#persistAuditLog
  - `trackViewCount` → this#extractFaqId, this#updateViewCountInDB
  - `collectCategoryStats` → this#extractCategory
  - `persistAuditLog` → this#cleanOldAuditLogs
  - `extractParameters` → this#isSensitiveParam, this#maskValue
- `FAQCacheAspect`
  - `manageFAQCache` → this#generateCacheKey, this#determineCacheName, this#extractFaqId, this#updateAverageLoadTime, this#logCacheStatistics
  - `generateCacheKey` → this#extractFaqId, this#extractCategory, this#extractPageable, this#extractKeyword
  - `determineCacheName` → this#extractCategory
  - `getCacheStatistics` → this#getPopularFAQs
  - `warmUpCache` → this#getPopularFAQs
- `FAQCommandService`
  - `createFAQ` → FAQRepository#save~, this#processImagesAndUpdateHtml, FAQImageRepository#findAllByFaqId
  - `updateFAQ` → FAQRepository#findById~, FAQImageRepository#findAllByFaqId, this#deleteImages, this#processImagesAndUpdateHtml
  - `deleteFAQ` → FAQRepository#findById~, FAQImageRepository#findAllByFaqId, this#deleteImages, FileUtils#deleteDirectory, FAQRepository#delete~
  - `processImagesAndUpdateHtml` → FileUtils#saveFile, FAQImageRepository#save~, this#updateHtmlImageSources
  - `deleteImages` → FileUtils#deleteFile, FAQImageRepository#deleteAll~
- `FAQContentProcessingAspect`
  - `processContent` → this#processFAQCreateRequest, this#processFAQUpdateRequest, this#processOutput, this#handleProcessingFailure
  - `processFAQCreateRequest` → this#processHtmlContent, this#optimizeImages, this#generateSummary
  - `processFAQUpdateRequest` → this#processHtmlContent, this#optimizeImages
  - `processHtmlContent` → this#preventXSS, this#sanitizeWithWhitelist, this#optimizeImageTags, this#processLinks, this#truncateContent, this#handleContentError
  - `processLinks` → this#isValidUrl
  - `optimizeImages` → this#optimizeImage
  - `optimizeImage` → this#resizeImage, this#compressImage
- `FAQFileStorageUtil`
  - `saveFiles` → this#saveFileInternal
  - `saveFileInternal` → this#validateFileExtension
- `FAQQueryService`
  - `getFAQs` → FAQRepository#findAll~, FAQImageRepository#findAllByFaqId
  - `getFAQ` → FAQRepository#findById~, FAQImageRepository#findAllByFaqId
  - `getFAQsByCategory` → FAQRepository#findByCategory, FAQImageRepository#findAllByFaqId
  - `searchFAQs` → FAQRepository#searchFAQs, FAQImageRepository#findAllByFaqId
  - `getAllFAQs` → FAQRepository#findAll~, FAQImageRepository#findAllByFaqId
- `FAQSearchOptimizationAspect`
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
- `FAQValidationAspect`
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

- `FAQCommandController` controller/command/FAQCommandController.java · public: createFAQ, updateFAQ, deleteFAQ · 주입: FAQCommandService, UserQueryService
- `FAQQueryController` controller/query/FAQQueryController.java · public: getFAQs, getFAQ, searchFAQs, getAllFAQs, downloadFAQFile · 주입: FAQQueryService, FileUtils

### service-command (2)

- `FAQCommandService` service/command/FAQCommandService.java · public: createFAQ, updateFAQ, deleteFAQ · 주입: FAQRepository, FAQImageRepository, FileUtils
- `FAQFileStorageUtil` service/command/FAQFileStorageUtil.java · public: saveFiles, updateImagePaths, extractImagePaths, deleteFiles, hasFiles

### service-query (1)

- `FAQQueryService` service/query/FAQQueryService.java · public: getFAQs, getFAQ, getFAQsByCategory, searchFAQs, getAllFAQs · 주입: FAQRepository, FAQImageRepository

### repository (2)

- `FAQImageRepository` (interface) repository/FAQImageRepository.java · extends/implements JpaRepository · public: findAllByFaqId
- `FAQRepository` (interface) repository/FAQRepository.java · extends/implements JpaRepository, FAQRepositoryCustom · public: findWithImagesById, findAllWithPaging, findByCategory

### custom (2)

- `FAQRepositoryCustom` (interface) repository/FAQRepositoryCustom.java · public: searchFAQs
- `FAQRepositoryImpl` repository/FAQRepositoryImpl.java · extends/implements FAQRepositoryCustom · public: searchFAQs · 주입: JPAQueryFactory

### entity (3)

- `FAQ` entity/FAQ.java · extends/implements BaseTimeEntity · public: update, getFileDirectory, getImageUrlPath
- `FAQCategory` (enum) entity/FAQCategory.java
- `FAQImage` entity/FAQImage.java · public: getFullImageUrl

### aop (5)

- `FAQAuditAspect` aop/FAQAuditAspect.java · public: audit · 주입: JdbcTemplate
- `FAQCacheAspect` aop/FAQCacheAspect.java · public: manageFAQCache, invalidateCategoryCache, invalidateAllCaches, invalidateFaqCache, getPopularFAQs, getCacheStatistics, warmUpCache · 주입: CacheManager
- `FAQContentProcessingAspect` aop/FAQContentProcessingAspect.java · public: processContent
- `FAQSearchOptimizationAspect` aop/FAQSearchOptimizationAspect.java · public: init, optimizeSearch, updatePopularKeywords · 주입: JdbcTemplate, CacheManager
- `FAQValidationAspect` aop/FAQValidationAspect.java · public: init, validationPointcut, validateFAQ · 주입: FAQRepository

### dto (3)

- `FAQCreateRequestDto` dto/FAQCreateRequestDto.java
- `FAQResponseDto` dto/FAQResponseDto.java
- `FAQUpdateRequestDto` dto/FAQUpdateRequestDto.java

### exception (5)

- `FAQErrorCode` exception/FAQErrorCode.java
- `FAQException` exception/FAQException.java
- `FAQFileException` exception/FAQFileException.java
- `FAQNotFoundException` exception/FAQNotFoundException.java
- `FAQPermissionException` exception/FAQPermissionException.java

### other (5)

- `FAQAudit` (@interface) aop/annotation/FAQAudit.java
- `FAQCache` (@interface) aop/annotation/FAQCache.java
- `FAQContentProcessing` (@interface) aop/annotation/FAQContentProcessing.java
- `FAQSearchOptimization` (@interface) aop/annotation/FAQSearchOptimization.java
- `FAQValidation` (@interface) aop/annotation/FAQValidation.java

### 다른 도메인 참조 (import·주입 기준)

- **user**: `UserQueryService` ← FAQCommandController

### 이 도메인을 참조하는 다른 도메인

- **chatQuestion**: FAQQueryService
- **global**: FAQ, FAQCategory, FAQRepository

## 4. JPA 엔티티 연관

- `FAQ` @Entity · extends BaseTimeEntity
- `FAQImage` @Entity
  - faq: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "faq_id") → `FAQ`

## 5. 이벤트·메시징

(없음)

## 6. AOP

- `FAQAuditAspect#audit` @Around("@annotation(faqAudit)") · 바인딩: `@FAQAudit`
- `FAQCacheAspect#manageFAQCache` @Around("@annotation(faqCache)") · 바인딩: `@FAQCache`
- `FAQSearchOptimizationAspect#optimizeSearch` @Around("@annotation(searchOptimization)") · 바인딩: `@FAQSearchOptimization`
- `FAQValidationAspect#validationPointcut` @Pointcut("@annotation(com.fream.back.domain.faq.aop.annotation.FAQValidation)")
- `FAQValidationAspect#validateFAQ` @Around("validationPointcut() && @annotation(validation)") · 바인딩: `@FAQValidation`
- `FAQContentProcessingAspect#processContent` @Around("@annotation(contentProcessing)") · 바인딩: `@FAQContentProcessing`

어노테이션 적용 지점(메서드에 붙은 곳):

- `@FAQAudit`: (없음)
- `@FAQCache`: (없음)
- `@FAQContentProcessing`: (없음)
- `@FAQSearchOptimization`: (없음)
- `@FAQValidation`: (없음)

