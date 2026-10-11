# order 도메인

기준 경로: `src/main/java/com/fream/back/domain/order/` · 파일 30개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/api/faq` | `OrderCommandController#createFAQ` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `OrderCommandService#createFAQ` |
| PUT | `/api/faq/{id}` | `OrderCommandController#updateFAQ` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `OrderCommandService#updateFAQ` |
| DELETE | `/api/faq/{id}` | `OrderCommandController#deleteFAQ` | `this#extractEmailFromSecurityContext`, `this#checkAdminPermission`, `OrderCommandService#deleteFAQ` |
| GET | `/api/faq` | `OrderQueryController#getFAQs` | `OrderQueryService#getFAQsByCategory`, `OrderQueryService#getFAQs` |
| GET | `/api/faq/{id}` | `OrderQueryController#getFAQ` | `OrderQueryService#getFAQ` |
| GET | `/api/faq/search` | `OrderQueryController#searchFAQs` | `OrderQueryService#searchFAQs` |
| GET | `/api/faq/all` | `OrderQueryController#getAllFAQs` | `OrderQueryService#getAllFAQs` |
| GET | `/api/faq/files/{faqId}/{fileName}` | `OrderQueryController#downloadFAQFile` |  |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `OrderAuditAspect`
  - `audit` → this#buildAuditContext, this#logAuditStart, this#processAuditSuccess, this#processAuditFailure
  - `buildAuditContext` → this#getClientIpAddress, this#extractParameters, this#determineEventType
  - `processAuditSuccess` → this#trackViewCount, this#collectCategoryStats, this#logAuditEvent, this#persistAuditLog, this#sendToExternalSystem, this#publishRealTimeEvent
  - `processAuditFailure` → this#alertCriticalEvent, this#persistAuditLog
  - `trackViewCount` → this#extractFaqId, this#updateViewCountInDB
  - `collectCategoryStats` → this#extractCategory
  - `persistAuditLog` → this#cleanOldAuditLogs
  - `extractParameters` → this#isSensitiveParam, this#maskValue
- `OrderCacheAspect`
  - `manageFAQCache` → this#generateCacheKey, this#determineCacheName, this#extractFaqId, this#updateAverageLoadTime, this#logCacheStatistics
  - `generateCacheKey` → this#extractFaqId, this#extractCategory, this#extractPageable, this#extractKeyword
  - `determineCacheName` → this#extractCategory
  - `getCacheStatistics` → this#getPopularFAQs
  - `warmUpCache` → this#getPopularFAQs
- `OrderCommandService`
  - `createFAQ` → OrderRepository#save~, this#processImagesAndUpdateHtml, OrderImageRepository#findAllByFaqId
  - `updateFAQ` → OrderRepository#findById~, OrderImageRepository#findAllByFaqId, this#deleteImages, this#processImagesAndUpdateHtml
  - `deleteFAQ` → OrderRepository#findById~, OrderImageRepository#findAllByFaqId, this#deleteImages, FileUtils#deleteDirectory, OrderRepository#delete~
  - `processImagesAndUpdateHtml` → FileUtils#saveFile, OrderImageRepository#save~, this#updateHtmlImageSources
  - `deleteImages` → FileUtils#deleteFile, OrderImageRepository#deleteAll~
- `OrderContentProcessingAspect`
  - `processContent` → this#processFAQCreateRequest, this#processFAQUpdateRequest, this#processOutput, this#handleProcessingFailure
  - `processFAQCreateRequest` → this#processHtmlContent, this#optimizeImages, this#generateSummary
  - `processFAQUpdateRequest` → this#processHtmlContent, this#optimizeImages
  - `processHtmlContent` → this#preventXSS, this#sanitizeWithWhitelist, this#optimizeImageTags, this#processLinks, this#truncateContent, this#handleContentError
  - `processLinks` → this#isValidUrl
  - `optimizeImages` → this#optimizeImage
  - `optimizeImage` → this#resizeImage, this#compressImage
- `OrderFileStorageUtil`
  - `saveFiles` → this#saveFileInternal
  - `saveFileInternal` → this#validateFileExtension
- `OrderQueryService`
  - `getFAQs` → OrderRepository#findAll~, OrderImageRepository#findAllByFaqId
  - `getFAQ` → OrderRepository#findById~, OrderImageRepository#findAllByFaqId
  - `getFAQsByCategory` → OrderRepository#findByCategory, OrderImageRepository#findAllByFaqId
  - `searchFAQs` → OrderRepository#searchFAQs, OrderImageRepository#findAllByFaqId
  - `getAllFAQs` → OrderRepository#findAll~, OrderImageRepository#findAllByFaqId
- `OrderSearchOptimizationAspect`
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
- `OrderValidationAspect`
  - `init` → this#initializeProhibitedWords
  - `validateFAQ` → this#createValidationContext, this#handleValidationFailure, this#performValidation, this#performAsyncValidation, this#cleanValidationCache
  - `performValidation` → this#validateByType, this#validateAdminPermission
  - `validateByType` → this#extractQuestion, this#extractAnswer, this#extractFiles, this#extractCategory, this#validateContent, this#validateLength, this#validateFiles, this#validateCategory, this#checkProhibitedWords, this#checkHtmlInjection, this#checkSqlInjection, this#checkDuplicate
  - `validateFiles` → this#getFileExtension, this#validateFileSignature
  - `checkDuplicate` → this#extractIdFromContext, this#checkQuestionExists
  - `handleValidationFailure` → this#sanitizeAndProceed
  - `sanitizeAndProceed` → this#sanitizeText, this#sanitizeHtml
  - `checkQuestionExists` → OrderRepository#findAll~
  - `initializeProhibitedWords` → this#loadProhibitedWordsFromDatabase

## 3. 클래스 목록 (역할별)

### controller (2)

- `OrderCommandController` controller/command/OrderCommandController.java · public: createFAQ, updateFAQ, deleteFAQ · 주입: OrderCommandService, ProductQueryService
- `OrderQueryController` controller/query/OrderQueryController.java · public: getFAQs, getFAQ, searchFAQs, getAllFAQs, downloadFAQFile · 주입: OrderQueryService, FileUtils

### service-command (2)

- `OrderCommandService` service/command/OrderCommandService.java · public: createFAQ, updateFAQ, deleteFAQ · 주입: OrderRepository, OrderImageRepository, FileUtils
- `OrderFileStorageUtil` service/command/OrderFileStorageUtil.java · public: saveFiles, updateImagePaths, extractImagePaths, deleteFiles, hasFiles

### service-query (1)

- `OrderQueryService` service/query/OrderQueryService.java · public: getFAQs, getFAQ, getFAQsByCategory, searchFAQs, getAllFAQs · 주입: OrderRepository, OrderImageRepository

### repository (2)

- `OrderImageRepository` (interface) repository/OrderImageRepository.java · extends/implements JpaRepository · public: findAllByFaqId
- `OrderRepository` (interface) repository/OrderRepository.java · extends/implements JpaRepository, OrderRepositoryCustom · public: findWithImagesById, findAllWithPaging, findByCategory

### custom (2)

- `OrderRepositoryCustom` (interface) repository/OrderRepositoryCustom.java · public: searchFAQs
- `OrderRepositoryImpl` repository/OrderRepositoryImpl.java · extends/implements OrderRepositoryCustom · public: searchFAQs · 주입: JPAQueryFactory

### entity (3)

- `Order` entity/Order.java · extends/implements BaseTimeEntity · public: update, getFileDirectory, getImageUrlPath
- `OrderCategory` (enum) entity/OrderCategory.java
- `OrderImage` entity/OrderImage.java · public: getFullImageUrl

### aop (5)

- `OrderAuditAspect` aop/OrderAuditAspect.java · public: audit · 주입: JdbcTemplate
- `OrderCacheAspect` aop/OrderCacheAspect.java · public: manageFAQCache, invalidateCategoryCache, invalidateAllCaches, invalidateFaqCache, getPopularFAQs, getCacheStatistics, warmUpCache · 주입: CacheManager
- `OrderContentProcessingAspect` aop/OrderContentProcessingAspect.java · public: processContent
- `OrderSearchOptimizationAspect` aop/OrderSearchOptimizationAspect.java · public: init, optimizeSearch, updatePopularKeywords · 주입: JdbcTemplate, CacheManager
- `OrderValidationAspect` aop/OrderValidationAspect.java · public: init, validationPointcut, validateFAQ · 주입: OrderRepository

### dto (3)

- `OrderCreateRequestDto` dto/OrderCreateRequestDto.java
- `OrderResponseDto` dto/OrderResponseDto.java
- `OrderUpdateRequestDto` dto/OrderUpdateRequestDto.java

### exception (5)

- `OrderErrorCode` exception/OrderErrorCode.java
- `OrderException` exception/OrderException.java
- `OrderFileException` exception/OrderFileException.java
- `OrderNotFoundException` exception/OrderNotFoundException.java
- `OrderPermissionException` exception/OrderPermissionException.java

### other (5)

- `OrderAudit` (@interface) aop/annotation/OrderAudit.java
- `OrderCache` (@interface) aop/annotation/OrderCache.java
- `OrderContentProcessing` (@interface) aop/annotation/OrderContentProcessing.java
- `OrderSearchOptimization` (@interface) aop/annotation/OrderSearchOptimization.java
- `OrderValidation` (@interface) aop/annotation/OrderValidation.java

### 다른 도메인 참조 (import·주입 기준)

- **product**: `ProductQueryService` ← OrderCommandController

### 이 도메인을 참조하는 다른 도메인

- **global**: Order, OrderCategory, OrderRepository
- **monitoring**: OrderQueryService

## 4. JPA 엔티티 연관

- `Order` @Entity · extends BaseTimeEntity
- `OrderImage` @Entity
  - faq: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "faq_id") → `Order`

## 5. 이벤트·메시징

(없음)

## 6. AOP

- `OrderAuditAspect#audit` @Around("@annotation(faqAudit)") · 바인딩: `@OrderAudit`
- `OrderCacheAspect#manageFAQCache` @Around("@annotation(faqCache)") · 바인딩: `@OrderCache`
- `OrderSearchOptimizationAspect#optimizeSearch` @Around("@annotation(searchOptimization)") · 바인딩: `@OrderSearchOptimization`
- `OrderValidationAspect#validationPointcut` @Pointcut("@annotation(com.fream.back.domain.order.aop.annotation.OrderValidation)")
- `OrderValidationAspect#validateFAQ` @Around("validationPointcut() && @annotation(validation)") · 바인딩: `@OrderValidation`
- `OrderContentProcessingAspect#processContent` @Around("@annotation(contentProcessing)") · 바인딩: `@OrderContentProcessing`

어노테이션 적용 지점(메서드에 붙은 곳):

- `@OrderAudit`: (없음)
- `@OrderCache`: (없음)
- `@OrderContentProcessing`: (없음)
- `@OrderSearchOptimization`: (없음)
- `@OrderValidation`: (없음)

