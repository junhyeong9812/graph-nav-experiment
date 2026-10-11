# legacyproduct 도메인

기준 경로: `src/main/java/com/fream/back/domain/legacyproduct/` · 파일 52개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/legacy/brands` | `LegacyBrandController#createBrand` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyBrandCommandService#createBrand` |
| PUT | `/legacy/brands/{brandId}` | `LegacyBrandController#updateBrand` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyBrandCommandService#updateBrand` |
| DELETE | `/legacy/brands/{brandName}` | `LegacyBrandController#deleteBrand` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyBrandCommandService#deleteBrand` |
| GET | `/legacy/brands` | `LegacyBrandController#getAllBrands` | `LegacyBrandQueryService#findAllBrands` |
| GET | `/legacy/brands/page` | `LegacyBrandController#getBrandsPaging` | `LegacyBrandQueryService#findBrandsPaging` |
| GET | `/legacy/brands/search` | `LegacyBrandController#searchBrands` | `LegacyBrandQueryService#searchBrandsByName` |
| GET | `/legacy/brands/{brandId:[0-9]+}` | `LegacyBrandController#getBrandById` | `LegacyBrandQueryService#findBrandById` |
| GET | `/legacy/brands/name/{brandName}` | `LegacyBrandController#getBrandByName` | `LegacyBrandQueryService#findByName` |
| POST | `/legacy/categories` | `LegacyCategoryController#createCategory` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyCategoryCommandService#createCategory` |
| PUT | `/legacy/categories/{categoryId}` | `LegacyCategoryController#updateCategory` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyCategoryCommandService#updateCategory` |
| DELETE | `/legacy/categories/{categoryId}` | `LegacyCategoryController#deleteCategory` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyCategoryCommandService#deleteCategory` |
| GET | `/legacy/categories/main` | `LegacyCategoryController#getAllMainCategories` | `LegacyCategoryQueryService#findAllMainCategories` |
| GET | `/legacy/categories/sub/{mainCategoryName}` | `LegacyCategoryController#getSubCategoriesByMain` | `LegacyCategoryQueryService#findSubCategoriesByMainCategory` |
| POST | `/legacy/collections` | `LegacyCollectionController#createCollection` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyCollectionCommandService#createCollection` |
| PUT | `/legacy/collections/{collectionId}` | `LegacyCollectionController#updateCollection` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyCollectionCommandService#updateCollection` |
| DELETE | `/legacy/collections/{collectionName}` | `LegacyCollectionController#deleteCollection` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyCollectionCommandService#deleteCollection` |
| GET | `/legacy/collections` | `LegacyCollectionController#getAllCollections` | `LegacyCollectionQueryService#findAllCollections` |
| GET | `/legacy/collections/page` | `LegacyCollectionController#getCollectionsPaging` | `LegacyCollectionQueryService#findCollectionsPaging` |
| GET | `/legacy/collections/search` | `LegacyCollectionController#searchCollections` | `LegacyCollectionQueryService#searchCollectionsByName` |
| GET | `/legacy/collections/{collectionId:[0-9]+}` | `LegacyCollectionController#getCollectionById` | `LegacyCollectionQueryService#findCollectionById` |
| GET | `/legacy/collections/name/{collectionName}` | `LegacyCollectionController#getCollectionByName` | `LegacyCollectionQueryService#findByName` |
| POST | `/legacy/interests/{productColorId}/toggle` | `LegacyInterestCommandController#toggleInterest` | `this#extractEmailFromSecurityContext`, `LegacyInterestCommandService#toggleInterest` |
| POST | `/legacy/product-colors/{productId}` | `LegacyProductColorCommandController#createProductColor` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyProductColorCommandService#createProductColor`, `NginxCachePurgeUtil#purgeProductCache`, `NginxCachePurgeUtil#purgeEsCache` |
| PUT | `/legacy/product-colors/{productColorId}` | `LegacyProductColorCommandController#updateProductColor` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyProductColorCommandService#updateProductColor`, `NginxCachePurgeUtil#purgeProductCache`, `NginxCachePurgeUtil#purgeEsCache` |
| DELETE | `/legacy/product-colors/{productColorId}` | `LegacyProductColorCommandController#deleteProductColor` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyProductColorCommandService#deleteProductColor`, `NginxCachePurgeUtil#purgeProductCache`, `NginxCachePurgeUtil#purgeEsCache` |
| POST | `/legacy/products/command` | `LegacyProductCommandController#createProduct` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyProductCommandService#createProduct` |
| PUT | `/legacy/products/command/{productId}` | `LegacyProductCommandController#updateProduct` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyProductCommandService#updateProduct` |
| DELETE | `/legacy/products/command/{productId}` | `LegacyProductCommandController#deleteProduct` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyProductCommandService#deleteProduct` |
| GET | `/legacy/products/indexed/search` | `LegacyIndexedProductQueryController#searchProductsByNames` | `LegacyIndexedProductQueryService#searchProductsByNames`, `this#toPageDto` |
| GET | `/legacy/interests/{userId}` | `LegacyInterestQueryController#getUserInterestProducts` | `LegacyInterestQueryService#findUserInterestProducts` |
| GET | `/legacy/products/query` | `LegacyProductQueryController#searchProducts` | `LegacyProductQueryService#searchProducts`, `this#toPageDto` |
| GET | `/legacy/products/query/{productId}/detail` | `LegacyProductQueryController#getProductDetail` | `LegacyProductQueryService#getProductDetail`, `LegacyViewEventProducer#sendViewEvent` |
| GET | `/legacy/products/query/{productId}/images` | `LegacyProductQueryController#getProductImage` |  |
| GET | `/legacy/products/query/filters` | `LegacyProductQueryController#getFilterData` | `LegacyFilterService#getAllFilterData` |
| POST | `/legacy/products/query/filters/count` | `LegacyProductQueryController#countProductsByFilter` | `LegacyProductQueryService#countProductsByFilter` |
| GET | `/legacy/admin/products/query/{productId}/detail` | `LegacyProductQueryControllerForAdmin#getProductDetailForAdmin` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyProductEntityService#findById` |
| GET | `/legacy/admin/products/query/colors/{colorId}` | `LegacyProductQueryControllerForAdmin#getProductColorDetail` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyProductColorQueryService#findById` |
| GET | `/legacy/admin/products/query/{productId}/colors` | `LegacyProductQueryControllerForAdmin#getProductColors` | `this#extractEmailFromSecurityContext`, `LegacyUserQueryService#checkAdminRole`, `LegacyProductEntityService#findById` |
| GET | `/legacy/es/products` | `LegacyProductColorSearchController#esSearchProducts` | `this#convertGenders`, `LegacyProductColorSearchService#searchToDto`, `this#toPageDto` |
| GET | `/legacy/es/products/autocomplete` | `LegacyProductColorSearchController#autocompleteProducts` | `LegacyProductColorSearchService#autocomplete` |
| GET | `/legacy/es/products/raw-indices` | `LegacyProductColorSearchController#getRawIndices` | `LegacyProductColorSearchService#getAllIndexedColors`, `this#toPageDto` |
| GET | `/legacy/es/products/raw-indices/category/{categoryId}` | `LegacyProductColorSearchController#getRawIndicesByCategory` | `LegacyProductColorSearchService#getIndexedColorsByCategory`, `this#toPageDto` |
| GET | `/legacy/es/products/index-stats` | `LegacyProductColorSearchController#getIndexStats` | `LegacyProductColorSearchService#getIndexStats` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `LegacyBrandCommandService`
  - `createBrand` → BrandRepository#existsByName, BrandRepository#save~
  - `updateBrand` → BrandRepository#findById~, BrandRepository#existsByName
  - `deleteBrand` → BrandRepository#findByName, ProductRepository#existsByBrand, BrandRepository#delete~
- `LegacyBrandEntityService`
  - `findByName` → BrandRepository#findByName
- `LegacyBrandQueryService`
  - `findByName` → BrandRepository#findByName
  - `findAllBrands` → BrandRepository#findAllByOrderByNameDesc
  - `findById` → BrandRepository#findById~
  - `findBrandById` → BrandRepository#findById~
  - `findBrandsPaging` → BrandRepository#findAll~
  - `searchBrandsByName` → BrandRepository#findByNameContainingIgnoreCase
- `LegacyCategoryCommandService`
  - `createCategory` → CategoryRepository#existsByNameAndParentCategoryIsNull, CategoryRepository#save~, CategoryRepository#findByNameAndParentCategoryIsNull, CategoryRepository#existsByNameAndParentCategory
  - `updateCategory` → CategoryRepository#findById~, CategoryRepository#existsByNameAndParentCategoryIsNull, CategoryRepository#existsByNameAndParentCategory
  - `deleteCategory` → CategoryRepository#findById~, CategoryRepository#existsByParentCategory, ProductRepository#existsByCategory, CategoryRepository#delete~
- `LegacyCategoryEntityService`
  - `findMainCategoryByName` → CategoryRepository#findByNameAndParentCategoryIsNull
  - `findSubCategoryByName` → this#findMainCategoryByName, CategoryRepository#findByNameAndParentCategory
- `LegacyCategoryQueryService`
  - `findAllMainCategories` → CategoryRepository#findByParentCategoryIsNullOrderByNameDesc
  - `findSubCategoriesByMainCategory` → CategoryRepository#findByNameAndParentCategoryIsNull, CategoryRepository#findByParentCategoryOrderByNameDesc
  - `findRootCategoryById` → CategoryRepository#findWithParentById, this#findRootCategory
  - `findSubCategoriesByParentId` → CategoryRepository#findByParentCategoryId
  - `findSubCategoriesByParentName` → CategoryRepository#findByParentCategoryName
- `LegacyCollectionCommandService`
  - `createCollection` → LegacyCollectionRepository#findByName, LegacyCollectionRepository#save~
  - `updateCollection` → LegacyCollectionRepository#findById~, LegacyCollectionRepository#existsByName
  - `deleteCollection` → LegacyCollectionRepository#findByName, ProductRepository#existsByCollection, LegacyCollectionRepository#delete~
  - `createOrGetCollection` → LegacyCollectionRepository#findByName, LegacyCollectionRepository#save~
- `LegacyCollectionQueryService`
  - `findAllCollections` → LegacyCollectionRepository#findAllByOrderByNameDesc
  - `findByName` → LegacyCollectionRepository#findByName
  - `findCollectionById` → LegacyCollectionRepository#findById~
  - `findCollectionsPaging` → LegacyCollectionRepository#findAll~
  - `searchCollectionsByName` → LegacyCollectionRepository#findByNameContainingIgnoreCase
- `LegacyCreateSizesJobConfig`
  - `createSizesJob` → this#createSizesStep
  - `createSizesStep` → this#sizeItemReader, this#sizeItemProcessor, this#sizeItemWriter, this#skipListener
  - `sizeItemReader` → LegacyCategoryQueryService#findRootCategoryById, this#determineSizeType, ProductSizeRepository#findAllByProductColorId
  - `sizeItemProcessor` → this#isValidSize, ProductColorRepository#findById~
  - `sizeItemWriter` → ProductSizeRepository#save~
- `LegacyFilterService`
  - `getAllFilterData` → this#getGenderLabel, this#getDiscountData, this#getPriceRangeData, LegacyCategoryQueryService#findAllMainCategories, LegacyCategoryQueryService#findSubCategoriesByMainCategory, this#processCategoryData, LegacyBrandQueryService#findAllBrands, LegacyCollectionQueryService#findAllCollections
  - `getPriceRangeData` → this#createPriceRange
  - `processCategoryData` → LegacyCategoryQueryService#findSubCategoriesByParentId
- `LegacyIndexedProductQueryService`
  - `searchProductsByNames` → LegacyIndexedProductRepository#searchProductsByNames, LegacyProductQueryDslRepository#styleCountQuery, LegacyProductQueryDslRepository#tradeCountQuery
- `LegacyIndexedProductRepository`
  - `searchProductsByNames` → this#findBrandIdsByNames, this#findCategoryIdsByNames, this#findCollectionIdsByNames, this#searchProductsByIdsOptimized
  - `searchProductsByIdsOptimized` → this#buildKeywordPredicateOptimized, this#buildCategoryIdsPredicate, this#buildGenderPredicate, this#buildBrandIdsPredicate, this#buildCollectionIdsPredicate, this#buildColorPredicate, this#buildSizePredicate, this#buildPricePredicate, this#applySortingOptimized, this#countProductsByConditions
  - `countProductsByConditions` → this#buildKeywordPredicateOptimized, this#buildCategoryIdsPredicate, this#buildGenderPredicate, this#buildBrandIdsPredicate, this#buildCollectionIdsPredicate, this#buildColorPredicate, this#buildSizePredicate, this#buildPricePredicate
- `LegacyInterestCommandService`
  - `toggleInterest` → LegacyUserQueryService#findByEmail, LegacyProductColorQueryService#findById, LegacyInterestRepository#findByUserAndProductColor, LegacyInterestRepository#delete~, LegacyInterestRepository#save~
  - `deleteAllInterestsByProductColor` → LegacyInterestRepository#delete~
- `LegacyInterestQueryDslRepository`
  - `findUserInterestProducts` → this#getOrderByClause
- `LegacyInterestQueryService`
  - `findUserInterestProducts` → LegacyInterestQueryDslRepository#findUserInterestProducts
- `LegacyProductColorCommandService`
  - `createProductColor` → this#validateColorType, LegacyProductEntityService#findById, FileUtils#saveFile, ProductColorRepository#save~, LegacyProductSizeCommandService#createProductSizes
  - `updateProductColor` → ProductColorRepository#findById~, this#validateColorType, FileUtils#deleteFile, FileUtils#saveFile, LegacyProductSizeCommandService#deleteProductSize, LegacyProductSizeCommandService#createProductSizes, ProductColorRepository#save~
  - `deleteProductColor` → ProductColorRepository#findById~, LegacyInterestCommandService#deleteAllInterestsByProductColor, LegacyProductSizeCommandService#deleteAllSizesByProductColor, FileUtils#deleteFile, LegacyProductImageCommandService#deleteProductImage, LegacyProductImageQueryService#existsByProductColorId, LegacyProductImageQueryService#findAllByProductColorId, FileUtils#existsFile, LegacyProductDetailImageQueryService#existsByProductColorId, LegacyProductDetailImageQueryService#findAllByProductColorId, LegacyProductDetailImageCommandService#deleteProductDetailImage, ProductColorRepository#delete~
- `LegacyProductColorIndexInitializer`
  - `run` → LegacyProductColorIndexingService#indexAllColors
- `LegacyProductColorIndexingService`
  - `indexAllColors` → LegacyProductColorIndexQueryRepository#findAllForIndexingDto, LegacyProductColorIndexQueryRepository#findAllSizesForIndexing, LegacyProductColorEsRepository#saveAll~
  - `indexColorById` → LegacyProductColorIndexQueryRepository#findOneForIndexingDto, LegacyProductColorIndexQueryRepository#findSizesByColorId, LegacyProductColorEsRepository#save~
  - `deleteColorFromIndex` → LegacyProductColorEsRepository#deleteById~
- `LegacyProductColorQueryService`
  - `findById` → ProductColorRepository#findById~
  - `findAllByProductId` → ProductColorRepository#findByProductId
- `LegacyProductColorSearchService`
  - `searchToDto` → this#search, StyleRepository#styleCountByColorIds, OrderBidRepository#tradeCountByColorIds
  - `search` → this#rangeQuery
- `LegacyProductCommandService`
  - `createProduct` → LegacyBrandEntityService#findByName, LegacyCategoryEntityService#findSubCategoryByName, LegacyCollectionCommandService#createOrGetCollection, ProductRepository#save~
  - `updateProduct` → ProductRepository#findById~, LegacyBrandEntityService#findByName, LegacyCategoryEntityService#findSubCategoryByName, LegacyCollectionCommandService#createOrGetCollection
  - `deleteProduct` → ProductRepository#findById~, LegacyProductColorCommandService#deleteProductColor, ProductRepository#delete~
- `LegacyProductDetailImageCommandService`
  - `createProductDetailImage` → LegacyProductDetailImageRepository#save~
  - `deleteProductDetailImage` → LegacyProductDetailImageRepository#findById~, LegacyProductDetailImageRepository#delete~
- `LegacyProductDetailImageQueryService`
  - `findAllByProductColorId` → LegacyProductDetailImageRepository#findAllByProductColorId
  - `existsByProductColorId` → LegacyProductDetailImageRepository#existsByProductColorId
- `LegacyProductEntityService`
  - `findById` → ProductRepository#findById~
- `LegacyProductImageCommandService`
  - `createProductImage` → ProductImageRepository#save~
  - `deleteProductImage` → ProductImageRepository#findById~, ProductImageRepository#delete~
- `LegacyProductImageQueryService`
  - `findAllByProductColorId` → ProductImageRepository#findAllByProductColorId
  - `existsByProductColorId` → ProductImageRepository#existsByProductColorId
- `LegacyProductQueryDslRepository`
  - `searchProducts` → this#logPredicate, this#buildKeywordPredicate, this#buildCategoryPredicate, this#buildGenderPredicate, this#buildBrandPredicate, this#buildCollectionPredicate, this#buildColorPredicate, this#buildSizePredicate, this#buildPricePredicate
  - `countProductsByFilter` → this#buildKeywordPredicate, this#buildCategoryPredicate, this#buildGenderPredicate, this#buildBrandPredicate, this#buildCollectionPredicate, this#buildColorPredicate, this#buildSizePredicate, this#buildPricePredicate
- `LegacyProductQueryService`
  - `searchProducts` → LegacyProductQueryDslRepository#searchProducts, StyleRepository#styleCountByColorIds, OrderBidRepository#tradeCountByColorIds
  - `getProductDetail` → LegacyProductQueryDslRepository#findProductDetail
  - `searchProductsByColorIds` → LegacyProductQueryDslRepository#searchProductsByColorIds
  - `countProductsByFilter` → LegacyProductQueryDslRepository#countProductsByFilter
- `LegacyProductSizeCommandService`
  - `createProductSizes` → LegacyCategoryQueryService#findRootCategoryById, this#determineSizeType, ProductSizeRepository#findAllByProductColorId, this#isValidSize, ProductSizeRepository#save~
  - `deleteProductSize` → ProductSizeRepository#findById~, ProductSizeRepository#delete~
  - `updateProductSize` → ProductSizeRepository#findById~
  - `deleteAllSizesByProductColor` → ProductSizeRepository#findAllByProductColorId, this#deleteProductSize, ProductSizeRepository#flush~
- `LegacyProductSizeQueryService`
  - `findByColorIdAndSize` → ProductSizeRepository#findByProductColorIdAndSize
  - `findSizesByColorId` → ProductSizeRepository#findAllByProductColorId
  - `findById` → ProductSizeRepository#findById~
  - `findAllByProductColorId` → ProductSizeRepository#findAllByProductColorId
- `LegacyViewEventConsumer`
  - `listen` → ProductColorRepository#findById~, LegacyProductColorViewLogRepository#save~
- `LegacyViewEventKafkaConfig`
  - `viewEventKafkaTemplate` → this#viewEventProducerFactory
  - `viewEventKafkaListenerContainerFactory` → this#viewEventConsumerFactory
- `LegacyViewEventProducer`
  - `sendViewEvent` → kafka:view-log-topic

## 3. 클래스 목록 (역할별)

### controller (11)

- `LegacyBrandController` controller/LegacyBrandController.java · public: createBrand, updateBrand, deleteBrand, getAllBrands, getBrandsPaging, searchBrands, getBrandById, getBrandByName · 주입: LegacyBrandCommandService, LegacyBrandQueryService, LegacyUserQueryService
- `LegacyCategoryController` controller/LegacyCategoryController.java · public: createCategory, updateCategory, deleteCategory, getAllMainCategories, getSubCategoriesByMain · 주입: LegacyCategoryCommandService, LegacyCategoryQueryService, LegacyUserQueryService
- `LegacyCollectionController` controller/LegacyCollectionController.java · public: createCollection, updateCollection, deleteCollection, getAllCollections, getCollectionsPaging, searchCollections, getCollectionById, getCollectionByName · 주입: LegacyCollectionCommandService, LegacyCollectionQueryService, LegacyUserQueryService
- `LegacyInterestCommandController` controller/command/LegacyInterestCommandController.java · public: toggleInterest · 주입: LegacyInterestCommandService
- `LegacyProductColorCommandController` controller/command/LegacyProductColorCommandController.java · public: createProductColor, updateProductColor, deleteProductColor · 주입: LegacyProductColorCommandService, LegacyUserQueryService, NginxCachePurgeUtil
- `LegacyProductCommandController` controller/command/LegacyProductCommandController.java · public: createProduct, updateProduct, deleteProduct · 주입: LegacyProductCommandService, LegacyUserQueryService
- `LegacyIndexedProductQueryController` controller/query/LegacyIndexedProductQueryController.java · public: searchProductsByNames · 주입: LegacyIndexedProductQueryService
- `LegacyInterestQueryController` controller/query/LegacyInterestQueryController.java · public: getUserInterestProducts · 주입: LegacyInterestQueryService
- `LegacyProductQueryController` controller/query/LegacyProductQueryController.java · public: searchProducts, getProductDetail, getProductImage, getFilterData, countProductsByFilter · 주입: LegacyProductQueryService, LegacyUserQueryService, LegacyViewEventProducer, LegacyFilterService
- `LegacyProductQueryControllerForAdmin` controller/query/LegacyProductQueryControllerForAdmin.java · public: getProductDetailForAdmin, getProductColorDetail, getProductColors · 주입: LegacyProductQueryService, LegacyProductEntityService, LegacyProductColorQueryService, LegacyUserQueryService
- `LegacyProductColorSearchController` elasticsearch/controller/LegacyProductColorSearchController.java · public: esSearchProducts, autocompleteProducts, getRawIndices, getRawIndicesByCategory, getIndexStats · 주입: LegacyProductColorSearchService, LegacyProductQueryService

### service (25)

- `LegacyProductColorIndexingService` elasticsearch/service/LegacyProductColorIndexingService.java · public: indexAllColors, indexColorById, deleteColorFromIndex · 주입: LegacyProductColorIndexQueryRepository, LegacyProductColorEsRepository
- `LegacyProductColorSearchService` elasticsearch/service/LegacyProductColorSearchService.java · public: searchToDto, search, autocomplete, getAllIndexedColors, getIndexedColorsByCategory, getIndexStats · 주입: ElasticsearchOperations, LegacyProductColorEsRepository, StyleRepository, OrderBidRepository, ObjectMapper
- `LegacyBrandCommandService` service/brand/LegacyBrandCommandService.java · public: createBrand, updateBrand, deleteBrand · 주입: BrandRepository, ProductRepository
- `LegacyBrandEntityService` service/brand/LegacyBrandEntityService.java · public: findByName · 주입: BrandRepository
- `LegacyBrandQueryService` service/brand/LegacyBrandQueryService.java · public: findByName, findAllBrands, findById, findBrandById, findBrandsPaging, searchBrandsByName · 주입: BrandRepository
- `LegacyCategoryCommandService` service/category/LegacyCategoryCommandService.java · public: createCategory, updateCategory, deleteCategory · 주입: CategoryRepository, ProductRepository
- `LegacyCategoryEntityService` service/category/LegacyCategoryEntityService.java · public: findMainCategoryByName, findSubCategoryByName · 주입: CategoryRepository
- `LegacyCategoryQueryService` service/category/LegacyCategoryQueryService.java · public: findAllMainCategories, findSubCategoriesByMainCategory, findRootCategory, findRootCategoryById, findSubCategoriesByParentId, findSubCategoriesByParentName · 주입: CategoryRepository
- `LegacyCollectionCommandService` service/collection/LegacyCollectionCommandService.java · public: createCollection, updateCollection, deleteCollection, createOrGetCollection · 주입: LegacyCollectionRepository, ProductRepository
- `LegacyCollectionQueryService` service/collection/LegacyCollectionQueryService.java · public: findAllCollections, findByName, findCollectionById, findCollectionsPaging, searchCollectionsByName · 주입: LegacyCollectionRepository
- `LegacyFilterService` service/filter/LegacyFilterService.java · public: getAllFilterData · 주입: LegacyBrandQueryService, LegacyCategoryQueryService, LegacyCollectionQueryService
- `LegacyInterestCommandService` service/interest/LegacyInterestCommandService.java · public: toggleInterest, deleteAllInterestsByProductColor · 주입: LegacyInterestRepository, LegacyUserQueryService, LegacyProductColorQueryService
- `LegacyInterestQueryService` service/interest/LegacyInterestQueryService.java · public: findUserInterestProducts · 주입: LegacyInterestQueryDslRepository
- `LegacyIndexedProductQueryService` service/product/LegacyIndexedProductQueryService.java · public: searchProductsByNames · 주입: LegacyIndexedProductRepository, LegacyProductQueryDslRepository
- `LegacyProductCommandService` service/product/LegacyProductCommandService.java · public: createProduct, updateProduct, deleteProduct · 주입: ProductRepository, LegacyBrandEntityService, LegacyCategoryEntityService, LegacyCollectionCommandService, LegacyProductColorCommandService
- `LegacyProductEntityService` service/product/LegacyProductEntityService.java · public: findById · 주입: ProductRepository
- `LegacyProductQueryService` service/product/LegacyProductQueryService.java · public: searchProducts, getProductDetail, searchProductsByColorIds, countProductsByFilter · 주입: LegacyProductQueryDslRepository, StyleRepository, OrderBidRepository
- `LegacyProductColorCommandService` service/productColor/LegacyProductColorCommandService.java · public: createProductColor, updateProductColor, deleteProductColor · 주입: ProductColorRepository, LegacyProductSizeCommandService, FileUtils, LegacyProductEntityService, LegacyProductSizeQueryService, LegacyInterestCommandService, LegacyProductImageCommandService, LegacyProductDetailImageCommandService, LegacyProductImageQueryService, LegacyProductDetailImageQueryService, NginxCachePurgeUtil, JobLauncher, Job
- `LegacyProductColorQueryService` service/productColor/LegacyProductColorQueryService.java · public: findById, findAllByProductId · 주입: ProductColorRepository
- `LegacyProductDetailImageCommandService` service/productDetailImage/LegacyProductDetailImageCommandService.java · public: createProductDetailImage, deleteProductDetailImage · 주입: LegacyProductDetailImageRepository
- `LegacyProductDetailImageQueryService` service/productDetailImage/LegacyProductDetailImageQueryService.java · public: findAllByProductColorId, existsByProductColorId · 주입: LegacyProductDetailImageRepository
- `LegacyProductImageCommandService` service/productImage/LegacyProductImageCommandService.java · public: createProductImage, deleteProductImage · 주입: ProductImageRepository
- `LegacyProductImageQueryService` service/productImage/LegacyProductImageQueryService.java · public: findAllByProductColorId, existsByProductColorId · 주입: ProductImageRepository
- `LegacyProductSizeCommandService` service/productSize/LegacyProductSizeCommandService.java · public: createProductSizes, deleteProductSize, updateProductSize, deleteAllSizesByProductColor · 주입: ProductSizeRepository, LegacyCategoryQueryService
- `LegacyProductSizeQueryService` service/productSize/LegacyProductSizeQueryService.java · public: findByColorIdAndSize, findSizesByColorId, findById, findAllByProductColorId · 주입: ProductSizeRepository

### repository (10)

- `LegacyProductColorEsRepository` (interface) elasticsearch/repository/LegacyProductColorEsRepository.java · extends/implements ElasticsearchRepository
- `LegacyProductColorIndexQueryRepository` elasticsearch/repository/LegacyProductColorIndexQueryRepository.java · public: findAllForIndexingDto, findAllSizesForIndexing, findOneForIndexingDto, findSizesByColorId · 주입: JPAQueryFactory
- `LegacyCollectionRepository` (interface) repository/LegacyCollectionRepository.java · extends/implements JpaRepository · public: findByName, existsByName, findAllByOrderByNameDesc, findByNameContainingIgnoreCase
- `LegacyIndexedProductRepository` repository/LegacyIndexedProductRepository.java · public: findBrandIdsByNames, findCategoryIdsByNames, findCollectionIdsByNames, searchProductsByNames · 주입: JPAQueryFactory
- `LegacyInterestQueryDslRepository` repository/LegacyInterestQueryDslRepository.java · public: findUserInterestProducts · 주입: JPAQueryFactory
- `LegacyInterestRepository` (interface) repository/LegacyInterestRepository.java · extends/implements JpaRepository · public: existsByUserAndProductColor, findByUserAndProductColor, findAllByUserId
- `LegacyProductColorViewLogRepository` (interface) repository/LegacyProductColorViewLogRepository.java · extends/implements JpaRepository
- `LegacyProductDetailImageRepository` (interface) repository/LegacyProductDetailImageRepository.java · extends/implements JpaRepository · public: existsByProductColorId, findByProductColorId, findAllByProductColorId
- `LegacyProductPriceHistoryRepository` (interface) repository/LegacyProductPriceHistoryRepository.java · extends/implements JpaRepository · public: findByProductSizeId
- `LegacyProductQueryDslRepository` repository/LegacyProductQueryDslRepository.java · public: searchProducts, countProductsByFilter, findProductDetail, searchProductsByColorIds, tradeCountQuery, styleCountQuery · 주입: JPAQueryFactory

### event (2)

- `LegacyViewEventConsumer` service/kafka/LegacyViewEventConsumer.java · public: listen · 주입: ProductColorRepository, LegacyProductColorViewLogRepository
- `LegacyViewEventProducer` service/kafka/LegacyViewEventProducer.java · public: sendViewEvent · 주입: KafkaTemplate

### config (2)

- `LegacyViewEventKafkaConfig` service/kafka/config/LegacyViewEventKafkaConfig.java · public: viewEventProducerFactory, viewEventKafkaTemplate, viewEventConsumerFactory, viewEventKafkaListenerContainerFactory
- `LegacyCreateSizesJobConfig` service/productSize/LegacyCreateSizesJobConfig.java · public: createSizesJob, createSizesStep, sizeItemReader, sizeItemProcessor, sizeItemWriter, skipListener · 주입: JobRepository, PlatformTransactionManager, ProductColorRepository, ProductSizeRepository, LegacyCategoryQueryService

### other (2)

- `LegacyProductColorIndexInitializer` elasticsearch/config/LegacyProductColorIndexInitializer.java · extends/implements CommandLineRunner · public: run · 주입: LegacyProductColorIndexingService, ElasticsearchOperations
- `LegacyProductColorIndex` elasticsearch/index/LegacyProductColorIndex.java

### 다른 도메인 참조 (import·주입 기준)

- **legacyuser**: `LegacyUserQueryService` ← LegacyBrandController, LegacyCategoryController, LegacyCollectionController, LegacyInterestCommandService, LegacyProductColorCommandController, LegacyProductCommandController, LegacyProductQueryController, LegacyProductQueryControllerForAdmin
- **order**: `BidStatus` ← LegacyProductQueryDslRepository; `OrderBidRepository` ← LegacyProductColorSearchService, LegacyProductQueryService
- **product**: `Brand` ← LegacyBrandCommandService, LegacyBrandEntityService, LegacyBrandQueryService, LegacyProductCommandService; `BrandRepository` ← LegacyBrandCommandService, LegacyBrandEntityService, LegacyBrandQueryService; `Category` ← LegacyCategoryCommandService, LegacyCategoryEntityService, LegacyCategoryQueryService, LegacyCreateSizesJobConfig, LegacyProductCommandService, LegacyProductSizeCommandService; `CategoryRepository` ← LegacyCategoryCommandService, LegacyCategoryEntityService, LegacyCategoryQueryService; `Collection` ← LegacyCollectionCommandService, LegacyCollectionQueryService, LegacyCollectionRepository, LegacyProductCommandService; `ColorType` ← LegacyFilterService, LegacyProductColorCommandService; `GenderType` ← LegacyFilterService, LegacyIndexedProductRepository, LegacyProductColorSearchController, LegacyProductQueryDslRepository, LegacyProductQueryService; `Interest` ← LegacyInterestCommandService, LegacyInterestRepository; `Product` ← LegacyProductCommandService, LegacyProductEntityService, LegacyProductQueryControllerForAdmin; `ProductColor` ← LegacyCreateSizesJobConfig, LegacyInterestCommandService, LegacyInterestRepository, LegacyProductColorQueryService, LegacyProductDetailImageCommandService, LegacyProductImageCommandService, LegacyProductQueryControllerForAdmin, LegacyProductSizeCommandService, LegacyViewEventConsumer; `ProductColorRepository` ← LegacyCreateSizesJobConfig, LegacyProductColorCommandService, LegacyProductColorQueryService, LegacyViewEventConsumer; `ProductColorViewLog` ← LegacyProductColorViewLogRepository, LegacyViewEventConsumer; `ProductDetailImage` ← LegacyProductDetailImageCommandService, LegacyProductDetailImageQueryService, LegacyProductDetailImageRepository; `ProductImage` ← LegacyProductImageCommandService, LegacyProductImageQueryService; `ProductImageRepository` ← LegacyProductImageCommandService, LegacyProductImageQueryService; `ProductPriceHistory` ← LegacyProductPriceHistoryRepository; `ProductRepository` ← LegacyBrandCommandService, LegacyCategoryCommandService, LegacyCollectionCommandService, LegacyProductCommandService, LegacyProductEntityService; `ProductSize` ← LegacyCreateSizesJobConfig, LegacyProductSizeCommandService, LegacyProductSizeQueryService; `ProductSizeRepository` ← LegacyCreateSizesJobConfig, LegacyProductSizeCommandService, LegacyProductSizeQueryService; `SizeType` ← LegacyCreateSizesJobConfig, LegacyFilterService, LegacyProductSizeCommandService; `ViewEvent` ← LegacyViewEventConsumer, LegacyViewEventKafkaConfig, LegacyViewEventProducer
- **style**: `StyleRepository` ← LegacyProductColorSearchService, LegacyProductQueryService
- **user**: `Gender` ← LegacyProductQueryController, LegacyViewEventProducer; `User` ← LegacyInterestCommandService, LegacyInterestRepository

### 이 도메인을 참조하는 다른 도메인

- **legacyevent**: LegacyBrandQueryService
- **legacyorder**: LegacyProductSizeQueryService
- **legacysale**: LegacyProductSizeQueryService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

- 발행 kafka `view-log-topic`: `LegacyViewEventProducer#sendViewEvent` → `AdminViewEventConsumer#listen` [adminproduct], `InternalViewEventConsumer#listen` [internalproduct], `LegacyViewEventConsumer#listen` [legacyproduct], `PartnerViewEventConsumer#listen` [partnerproduct], `ViewEventConsumer#listen` [product], `SettlementViewEventConsumer#listen` [settlementproduct]
- 구독 kafka `view-log-topic`: `LegacyViewEventConsumer#listen` (groupId=my-group containerFactory=viewEventKafkaListenerContainerFactory) ← `AdminViewEventProducer#sendViewEvent` [adminproduct], `InternalViewEventProducer#sendViewEvent` [internalproduct], `LegacyViewEventProducer#sendViewEvent` [legacyproduct], `PartnerViewEventProducer#sendViewEvent` [partnerproduct], `ViewEventProducer#sendViewEvent` [product], `SettlementViewEventProducer#sendViewEvent` [settlementproduct]

## 6. AOP

(없음)

