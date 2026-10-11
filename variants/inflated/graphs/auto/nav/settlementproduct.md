# settlementproduct 도메인

기준 경로: `src/main/java/com/fream/back/domain/settlementproduct/` · 파일 52개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/settlement/brands` | `SettlementBrandController#createBrand` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementBrandCommandService#createBrand` |
| PUT | `/settlement/brands/{brandId}` | `SettlementBrandController#updateBrand` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementBrandCommandService#updateBrand` |
| DELETE | `/settlement/brands/{brandName}` | `SettlementBrandController#deleteBrand` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementBrandCommandService#deleteBrand` |
| GET | `/settlement/brands` | `SettlementBrandController#getAllBrands` | `SettlementBrandQueryService#findAllBrands` |
| GET | `/settlement/brands/page` | `SettlementBrandController#getBrandsPaging` | `SettlementBrandQueryService#findBrandsPaging` |
| GET | `/settlement/brands/search` | `SettlementBrandController#searchBrands` | `SettlementBrandQueryService#searchBrandsByName` |
| GET | `/settlement/brands/{brandId:[0-9]+}` | `SettlementBrandController#getBrandById` | `SettlementBrandQueryService#findBrandById` |
| GET | `/settlement/brands/name/{brandName}` | `SettlementBrandController#getBrandByName` | `SettlementBrandQueryService#findByName` |
| POST | `/settlement/categories` | `SettlementCategoryController#createCategory` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementCategoryCommandService#createCategory` |
| PUT | `/settlement/categories/{categoryId}` | `SettlementCategoryController#updateCategory` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementCategoryCommandService#updateCategory` |
| DELETE | `/settlement/categories/{categoryId}` | `SettlementCategoryController#deleteCategory` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementCategoryCommandService#deleteCategory` |
| GET | `/settlement/categories/main` | `SettlementCategoryController#getAllMainCategories` | `SettlementCategoryQueryService#findAllMainCategories` |
| GET | `/settlement/categories/sub/{mainCategoryName}` | `SettlementCategoryController#getSubCategoriesByMain` | `SettlementCategoryQueryService#findSubCategoriesByMainCategory` |
| POST | `/settlement/collections` | `SettlementCollectionController#createCollection` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementCollectionCommandService#createCollection` |
| PUT | `/settlement/collections/{collectionId}` | `SettlementCollectionController#updateCollection` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementCollectionCommandService#updateCollection` |
| DELETE | `/settlement/collections/{collectionName}` | `SettlementCollectionController#deleteCollection` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementCollectionCommandService#deleteCollection` |
| GET | `/settlement/collections` | `SettlementCollectionController#getAllCollections` | `SettlementCollectionQueryService#findAllCollections` |
| GET | `/settlement/collections/page` | `SettlementCollectionController#getCollectionsPaging` | `SettlementCollectionQueryService#findCollectionsPaging` |
| GET | `/settlement/collections/search` | `SettlementCollectionController#searchCollections` | `SettlementCollectionQueryService#searchCollectionsByName` |
| GET | `/settlement/collections/{collectionId:[0-9]+}` | `SettlementCollectionController#getCollectionById` | `SettlementCollectionQueryService#findCollectionById` |
| GET | `/settlement/collections/name/{collectionName}` | `SettlementCollectionController#getCollectionByName` | `SettlementCollectionQueryService#findByName` |
| POST | `/settlement/interests/{productColorId}/toggle` | `SettlementInterestCommandController#toggleInterest` | `this#extractEmailFromSecurityContext`, `SettlementInterestCommandService#toggleInterest` |
| POST | `/settlement/product-colors/{productId}` | `SettlementProductColorCommandController#createProductColor` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementProductColorCommandService#createProductColor`, `NginxCachePurgeUtil#purgeProductCache`, `NginxCachePurgeUtil#purgeEsCache` |
| PUT | `/settlement/product-colors/{productColorId}` | `SettlementProductColorCommandController#updateProductColor` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementProductColorCommandService#updateProductColor`, `NginxCachePurgeUtil#purgeProductCache`, `NginxCachePurgeUtil#purgeEsCache` |
| DELETE | `/settlement/product-colors/{productColorId}` | `SettlementProductColorCommandController#deleteProductColor` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementProductColorCommandService#deleteProductColor`, `NginxCachePurgeUtil#purgeProductCache`, `NginxCachePurgeUtil#purgeEsCache` |
| POST | `/settlement/products/command` | `SettlementProductCommandController#createProduct` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementProductCommandService#createProduct` |
| PUT | `/settlement/products/command/{productId}` | `SettlementProductCommandController#updateProduct` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementProductCommandService#updateProduct` |
| DELETE | `/settlement/products/command/{productId}` | `SettlementProductCommandController#deleteProduct` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementProductCommandService#deleteProduct` |
| GET | `/settlement/products/indexed/search` | `SettlementIndexedProductQueryController#searchProductsByNames` | `SettlementIndexedProductQueryService#searchProductsByNames`, `this#toPageDto` |
| GET | `/settlement/interests/{userId}` | `SettlementInterestQueryController#getUserInterestProducts` | `SettlementInterestQueryService#findUserInterestProducts` |
| GET | `/settlement/products/query` | `SettlementProductQueryController#searchProducts` | `SettlementProductQueryService#searchProducts`, `this#toPageDto` |
| GET | `/settlement/products/query/{productId}/detail` | `SettlementProductQueryController#getProductDetail` | `SettlementProductQueryService#getProductDetail`, `SettlementViewEventProducer#sendViewEvent` |
| GET | `/settlement/products/query/{productId}/images` | `SettlementProductQueryController#getProductImage` |  |
| GET | `/settlement/products/query/filters` | `SettlementProductQueryController#getFilterData` | `SettlementFilterService#getAllFilterData` |
| POST | `/settlement/products/query/filters/count` | `SettlementProductQueryController#countProductsByFilter` | `SettlementProductQueryService#countProductsByFilter` |
| GET | `/settlement/admin/products/query/{productId}/detail` | `SettlementProductQueryControllerForAdmin#getProductDetailForAdmin` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementProductEntityService#findById` |
| GET | `/settlement/admin/products/query/colors/{colorId}` | `SettlementProductQueryControllerForAdmin#getProductColorDetail` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementProductColorQueryService#findById` |
| GET | `/settlement/admin/products/query/{productId}/colors` | `SettlementProductQueryControllerForAdmin#getProductColors` | `this#extractEmailFromSecurityContext`, `SettlementUserQueryService#checkAdminRole`, `SettlementProductEntityService#findById` |
| GET | `/settlement/es/products` | `SettlementProductColorSearchController#esSearchProducts` | `this#convertGenders`, `SettlementProductColorSearchService#searchToDto`, `this#toPageDto` |
| GET | `/settlement/es/products/autocomplete` | `SettlementProductColorSearchController#autocompleteProducts` | `SettlementProductColorSearchService#autocomplete` |
| GET | `/settlement/es/products/raw-indices` | `SettlementProductColorSearchController#getRawIndices` | `SettlementProductColorSearchService#getAllIndexedColors`, `this#toPageDto` |
| GET | `/settlement/es/products/raw-indices/category/{categoryId}` | `SettlementProductColorSearchController#getRawIndicesByCategory` | `SettlementProductColorSearchService#getIndexedColorsByCategory`, `this#toPageDto` |
| GET | `/settlement/es/products/index-stats` | `SettlementProductColorSearchController#getIndexStats` | `SettlementProductColorSearchService#getIndexStats` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `SettlementBrandCommandService`
  - `createBrand` → BrandRepository#existsByName, BrandRepository#save~
  - `updateBrand` → BrandRepository#findById~, BrandRepository#existsByName
  - `deleteBrand` → BrandRepository#findByName, ProductRepository#existsByBrand, BrandRepository#delete~
- `SettlementBrandEntityService`
  - `findByName` → BrandRepository#findByName
- `SettlementBrandQueryService`
  - `findByName` → BrandRepository#findByName
  - `findAllBrands` → BrandRepository#findAllByOrderByNameDesc
  - `findById` → BrandRepository#findById~
  - `findBrandById` → BrandRepository#findById~
  - `findBrandsPaging` → BrandRepository#findAll~
  - `searchBrandsByName` → BrandRepository#findByNameContainingIgnoreCase
- `SettlementCategoryCommandService`
  - `createCategory` → CategoryRepository#existsByNameAndParentCategoryIsNull, CategoryRepository#save~, CategoryRepository#findByNameAndParentCategoryIsNull, CategoryRepository#existsByNameAndParentCategory
  - `updateCategory` → CategoryRepository#findById~, CategoryRepository#existsByNameAndParentCategoryIsNull, CategoryRepository#existsByNameAndParentCategory
  - `deleteCategory` → CategoryRepository#findById~, CategoryRepository#existsByParentCategory, ProductRepository#existsByCategory, CategoryRepository#delete~
- `SettlementCategoryEntityService`
  - `findMainCategoryByName` → CategoryRepository#findByNameAndParentCategoryIsNull
  - `findSubCategoryByName` → this#findMainCategoryByName, CategoryRepository#findByNameAndParentCategory
- `SettlementCategoryQueryService`
  - `findAllMainCategories` → CategoryRepository#findByParentCategoryIsNullOrderByNameDesc
  - `findSubCategoriesByMainCategory` → CategoryRepository#findByNameAndParentCategoryIsNull, CategoryRepository#findByParentCategoryOrderByNameDesc
  - `findRootCategoryById` → CategoryRepository#findWithParentById, this#findRootCategory
  - `findSubCategoriesByParentId` → CategoryRepository#findByParentCategoryId
  - `findSubCategoriesByParentName` → CategoryRepository#findByParentCategoryName
- `SettlementCollectionCommandService`
  - `createCollection` → SettlementCollectionRepository#findByName, SettlementCollectionRepository#save~
  - `updateCollection` → SettlementCollectionRepository#findById~, SettlementCollectionRepository#existsByName
  - `deleteCollection` → SettlementCollectionRepository#findByName, ProductRepository#existsByCollection, SettlementCollectionRepository#delete~
  - `createOrGetCollection` → SettlementCollectionRepository#findByName, SettlementCollectionRepository#save~
- `SettlementCollectionQueryService`
  - `findAllCollections` → SettlementCollectionRepository#findAllByOrderByNameDesc
  - `findByName` → SettlementCollectionRepository#findByName
  - `findCollectionById` → SettlementCollectionRepository#findById~
  - `findCollectionsPaging` → SettlementCollectionRepository#findAll~
  - `searchCollectionsByName` → SettlementCollectionRepository#findByNameContainingIgnoreCase
- `SettlementCreateSizesJobConfig`
  - `createSizesJob` → this#createSizesStep
  - `createSizesStep` → this#sizeItemReader, this#sizeItemProcessor, this#sizeItemWriter, this#skipListener
  - `sizeItemReader` → SettlementCategoryQueryService#findRootCategoryById, this#determineSizeType, ProductSizeRepository#findAllByProductColorId
  - `sizeItemProcessor` → this#isValidSize, ProductColorRepository#findById~
  - `sizeItemWriter` → ProductSizeRepository#save~
- `SettlementFilterService`
  - `getAllFilterData` → this#getGenderLabel, this#getDiscountData, this#getPriceRangeData, SettlementCategoryQueryService#findAllMainCategories, SettlementCategoryQueryService#findSubCategoriesByMainCategory, this#processCategoryData, SettlementBrandQueryService#findAllBrands, SettlementCollectionQueryService#findAllCollections
  - `getPriceRangeData` → this#createPriceRange
  - `processCategoryData` → SettlementCategoryQueryService#findSubCategoriesByParentId
- `SettlementIndexedProductQueryService`
  - `searchProductsByNames` → SettlementIndexedProductRepository#searchProductsByNames, SettlementProductQueryDslRepository#styleCountQuery, SettlementProductQueryDslRepository#tradeCountQuery
- `SettlementIndexedProductRepository`
  - `searchProductsByNames` → this#findBrandIdsByNames, this#findCategoryIdsByNames, this#findCollectionIdsByNames, this#searchProductsByIdsOptimized
  - `searchProductsByIdsOptimized` → this#buildKeywordPredicateOptimized, this#buildCategoryIdsPredicate, this#buildGenderPredicate, this#buildBrandIdsPredicate, this#buildCollectionIdsPredicate, this#buildColorPredicate, this#buildSizePredicate, this#buildPricePredicate, this#applySortingOptimized, this#countProductsByConditions
  - `countProductsByConditions` → this#buildKeywordPredicateOptimized, this#buildCategoryIdsPredicate, this#buildGenderPredicate, this#buildBrandIdsPredicate, this#buildCollectionIdsPredicate, this#buildColorPredicate, this#buildSizePredicate, this#buildPricePredicate
- `SettlementInterestCommandService`
  - `toggleInterest` → SettlementUserQueryService#findByEmail, SettlementProductColorQueryService#findById, SettlementInterestRepository#findByUserAndProductColor, SettlementInterestRepository#delete~, SettlementInterestRepository#save~
  - `deleteAllInterestsByProductColor` → SettlementInterestRepository#delete~
- `SettlementInterestQueryDslRepository`
  - `findUserInterestProducts` → this#getOrderByClause
- `SettlementInterestQueryService`
  - `findUserInterestProducts` → SettlementInterestQueryDslRepository#findUserInterestProducts
- `SettlementProductColorCommandService`
  - `createProductColor` → this#validateColorType, SettlementProductEntityService#findById, FileUtils#saveFile, ProductColorRepository#save~, SettlementProductSizeCommandService#createProductSizes
  - `updateProductColor` → ProductColorRepository#findById~, this#validateColorType, FileUtils#deleteFile, FileUtils#saveFile, SettlementProductSizeCommandService#deleteProductSize, SettlementProductSizeCommandService#createProductSizes, ProductColorRepository#save~
  - `deleteProductColor` → ProductColorRepository#findById~, SettlementInterestCommandService#deleteAllInterestsByProductColor, SettlementProductSizeCommandService#deleteAllSizesByProductColor, FileUtils#deleteFile, SettlementProductImageCommandService#deleteProductImage, SettlementProductImageQueryService#existsByProductColorId, SettlementProductImageQueryService#findAllByProductColorId, FileUtils#existsFile, SettlementProductDetailImageQueryService#existsByProductColorId, SettlementProductDetailImageQueryService#findAllByProductColorId, SettlementProductDetailImageCommandService#deleteProductDetailImage, ProductColorRepository#delete~
- `SettlementProductColorIndexInitializer`
  - `run` → SettlementProductColorIndexingService#indexAllColors
- `SettlementProductColorIndexingService`
  - `indexAllColors` → SettlementProductColorIndexQueryRepository#findAllForIndexingDto, SettlementProductColorIndexQueryRepository#findAllSizesForIndexing, SettlementProductColorEsRepository#saveAll~
  - `indexColorById` → SettlementProductColorIndexQueryRepository#findOneForIndexingDto, SettlementProductColorIndexQueryRepository#findSizesByColorId, SettlementProductColorEsRepository#save~
  - `deleteColorFromIndex` → SettlementProductColorEsRepository#deleteById~
- `SettlementProductColorQueryService`
  - `findById` → ProductColorRepository#findById~
  - `findAllByProductId` → ProductColorRepository#findByProductId
- `SettlementProductColorSearchService`
  - `searchToDto` → this#search, StyleRepository#styleCountByColorIds, OrderBidRepository#tradeCountByColorIds
  - `search` → this#rangeQuery
- `SettlementProductCommandService`
  - `createProduct` → SettlementBrandEntityService#findByName, SettlementCategoryEntityService#findSubCategoryByName, SettlementCollectionCommandService#createOrGetCollection, ProductRepository#save~
  - `updateProduct` → ProductRepository#findById~, SettlementBrandEntityService#findByName, SettlementCategoryEntityService#findSubCategoryByName, SettlementCollectionCommandService#createOrGetCollection
  - `deleteProduct` → ProductRepository#findById~, SettlementProductColorCommandService#deleteProductColor, ProductRepository#delete~
- `SettlementProductDetailImageCommandService`
  - `createProductDetailImage` → SettlementProductDetailImageRepository#save~
  - `deleteProductDetailImage` → SettlementProductDetailImageRepository#findById~, SettlementProductDetailImageRepository#delete~
- `SettlementProductDetailImageQueryService`
  - `findAllByProductColorId` → SettlementProductDetailImageRepository#findAllByProductColorId
  - `existsByProductColorId` → SettlementProductDetailImageRepository#existsByProductColorId
- `SettlementProductEntityService`
  - `findById` → ProductRepository#findById~
- `SettlementProductImageCommandService`
  - `createProductImage` → ProductImageRepository#save~
  - `deleteProductImage` → ProductImageRepository#findById~, ProductImageRepository#delete~
- `SettlementProductImageQueryService`
  - `findAllByProductColorId` → ProductImageRepository#findAllByProductColorId
  - `existsByProductColorId` → ProductImageRepository#existsByProductColorId
- `SettlementProductQueryDslRepository`
  - `searchProducts` → this#logPredicate, this#buildKeywordPredicate, this#buildCategoryPredicate, this#buildGenderPredicate, this#buildBrandPredicate, this#buildCollectionPredicate, this#buildColorPredicate, this#buildSizePredicate, this#buildPricePredicate
  - `countProductsByFilter` → this#buildKeywordPredicate, this#buildCategoryPredicate, this#buildGenderPredicate, this#buildBrandPredicate, this#buildCollectionPredicate, this#buildColorPredicate, this#buildSizePredicate, this#buildPricePredicate
- `SettlementProductQueryService`
  - `searchProducts` → SettlementProductQueryDslRepository#searchProducts, StyleRepository#styleCountByColorIds, OrderBidRepository#tradeCountByColorIds
  - `getProductDetail` → SettlementProductQueryDslRepository#findProductDetail
  - `searchProductsByColorIds` → SettlementProductQueryDslRepository#searchProductsByColorIds
  - `countProductsByFilter` → SettlementProductQueryDslRepository#countProductsByFilter
- `SettlementProductSizeCommandService`
  - `createProductSizes` → SettlementCategoryQueryService#findRootCategoryById, this#determineSizeType, ProductSizeRepository#findAllByProductColorId, this#isValidSize, ProductSizeRepository#save~
  - `deleteProductSize` → ProductSizeRepository#findById~, ProductSizeRepository#delete~
  - `updateProductSize` → ProductSizeRepository#findById~
  - `deleteAllSizesByProductColor` → ProductSizeRepository#findAllByProductColorId, this#deleteProductSize, ProductSizeRepository#flush~
- `SettlementProductSizeQueryService`
  - `findByColorIdAndSize` → ProductSizeRepository#findByProductColorIdAndSize
  - `findSizesByColorId` → ProductSizeRepository#findAllByProductColorId
  - `findById` → ProductSizeRepository#findById~
  - `findAllByProductColorId` → ProductSizeRepository#findAllByProductColorId
- `SettlementViewEventConsumer`
  - `listen` → ProductColorRepository#findById~, SettlementProductColorViewLogRepository#save~
- `SettlementViewEventKafkaConfig`
  - `viewEventKafkaTemplate` → this#viewEventProducerFactory
  - `viewEventKafkaListenerContainerFactory` → this#viewEventConsumerFactory
- `SettlementViewEventProducer`
  - `sendViewEvent` → kafka:view-log-topic

## 3. 클래스 목록 (역할별)

### controller (11)

- `SettlementBrandController` controller/SettlementBrandController.java · public: createBrand, updateBrand, deleteBrand, getAllBrands, getBrandsPaging, searchBrands, getBrandById, getBrandByName · 주입: SettlementBrandCommandService, SettlementBrandQueryService, SettlementUserQueryService
- `SettlementCategoryController` controller/SettlementCategoryController.java · public: createCategory, updateCategory, deleteCategory, getAllMainCategories, getSubCategoriesByMain · 주입: SettlementCategoryCommandService, SettlementCategoryQueryService, SettlementUserQueryService
- `SettlementCollectionController` controller/SettlementCollectionController.java · public: createCollection, updateCollection, deleteCollection, getAllCollections, getCollectionsPaging, searchCollections, getCollectionById, getCollectionByName · 주입: SettlementCollectionCommandService, SettlementCollectionQueryService, SettlementUserQueryService
- `SettlementInterestCommandController` controller/command/SettlementInterestCommandController.java · public: toggleInterest · 주입: SettlementInterestCommandService
- `SettlementProductColorCommandController` controller/command/SettlementProductColorCommandController.java · public: createProductColor, updateProductColor, deleteProductColor · 주입: SettlementProductColorCommandService, SettlementUserQueryService, NginxCachePurgeUtil
- `SettlementProductCommandController` controller/command/SettlementProductCommandController.java · public: createProduct, updateProduct, deleteProduct · 주입: SettlementProductCommandService, SettlementUserQueryService
- `SettlementIndexedProductQueryController` controller/query/SettlementIndexedProductQueryController.java · public: searchProductsByNames · 주입: SettlementIndexedProductQueryService
- `SettlementInterestQueryController` controller/query/SettlementInterestQueryController.java · public: getUserInterestProducts · 주입: SettlementInterestQueryService
- `SettlementProductQueryController` controller/query/SettlementProductQueryController.java · public: searchProducts, getProductDetail, getProductImage, getFilterData, countProductsByFilter · 주입: SettlementProductQueryService, SettlementUserQueryService, SettlementViewEventProducer, SettlementFilterService
- `SettlementProductQueryControllerForAdmin` controller/query/SettlementProductQueryControllerForAdmin.java · public: getProductDetailForAdmin, getProductColorDetail, getProductColors · 주입: SettlementProductQueryService, SettlementProductEntityService, SettlementProductColorQueryService, SettlementUserQueryService
- `SettlementProductColorSearchController` elasticsearch/controller/SettlementProductColorSearchController.java · public: esSearchProducts, autocompleteProducts, getRawIndices, getRawIndicesByCategory, getIndexStats · 주입: SettlementProductColorSearchService, SettlementProductQueryService

### service (25)

- `SettlementProductColorIndexingService` elasticsearch/service/SettlementProductColorIndexingService.java · public: indexAllColors, indexColorById, deleteColorFromIndex · 주입: SettlementProductColorIndexQueryRepository, SettlementProductColorEsRepository
- `SettlementProductColorSearchService` elasticsearch/service/SettlementProductColorSearchService.java · public: searchToDto, search, autocomplete, getAllIndexedColors, getIndexedColorsByCategory, getIndexStats · 주입: ElasticsearchOperations, SettlementProductColorEsRepository, StyleRepository, OrderBidRepository, ObjectMapper
- `SettlementBrandCommandService` service/brand/SettlementBrandCommandService.java · public: createBrand, updateBrand, deleteBrand · 주입: BrandRepository, ProductRepository
- `SettlementBrandEntityService` service/brand/SettlementBrandEntityService.java · public: findByName · 주입: BrandRepository
- `SettlementBrandQueryService` service/brand/SettlementBrandQueryService.java · public: findByName, findAllBrands, findById, findBrandById, findBrandsPaging, searchBrandsByName · 주입: BrandRepository
- `SettlementCategoryCommandService` service/category/SettlementCategoryCommandService.java · public: createCategory, updateCategory, deleteCategory · 주입: CategoryRepository, ProductRepository
- `SettlementCategoryEntityService` service/category/SettlementCategoryEntityService.java · public: findMainCategoryByName, findSubCategoryByName · 주입: CategoryRepository
- `SettlementCategoryQueryService` service/category/SettlementCategoryQueryService.java · public: findAllMainCategories, findSubCategoriesByMainCategory, findRootCategory, findRootCategoryById, findSubCategoriesByParentId, findSubCategoriesByParentName · 주입: CategoryRepository
- `SettlementCollectionCommandService` service/collection/SettlementCollectionCommandService.java · public: createCollection, updateCollection, deleteCollection, createOrGetCollection · 주입: SettlementCollectionRepository, ProductRepository
- `SettlementCollectionQueryService` service/collection/SettlementCollectionQueryService.java · public: findAllCollections, findByName, findCollectionById, findCollectionsPaging, searchCollectionsByName · 주입: SettlementCollectionRepository
- `SettlementFilterService` service/filter/SettlementFilterService.java · public: getAllFilterData · 주입: SettlementBrandQueryService, SettlementCategoryQueryService, SettlementCollectionQueryService
- `SettlementInterestCommandService` service/interest/SettlementInterestCommandService.java · public: toggleInterest, deleteAllInterestsByProductColor · 주입: SettlementInterestRepository, SettlementUserQueryService, SettlementProductColorQueryService
- `SettlementInterestQueryService` service/interest/SettlementInterestQueryService.java · public: findUserInterestProducts · 주입: SettlementInterestQueryDslRepository
- `SettlementIndexedProductQueryService` service/product/SettlementIndexedProductQueryService.java · public: searchProductsByNames · 주입: SettlementIndexedProductRepository, SettlementProductQueryDslRepository
- `SettlementProductCommandService` service/product/SettlementProductCommandService.java · public: createProduct, updateProduct, deleteProduct · 주입: ProductRepository, SettlementBrandEntityService, SettlementCategoryEntityService, SettlementCollectionCommandService, SettlementProductColorCommandService
- `SettlementProductEntityService` service/product/SettlementProductEntityService.java · public: findById · 주입: ProductRepository
- `SettlementProductQueryService` service/product/SettlementProductQueryService.java · public: searchProducts, getProductDetail, searchProductsByColorIds, countProductsByFilter · 주입: SettlementProductQueryDslRepository, StyleRepository, OrderBidRepository
- `SettlementProductColorCommandService` service/productColor/SettlementProductColorCommandService.java · public: createProductColor, updateProductColor, deleteProductColor · 주입: ProductColorRepository, SettlementProductSizeCommandService, FileUtils, SettlementProductEntityService, SettlementProductSizeQueryService, SettlementInterestCommandService, SettlementProductImageCommandService, SettlementProductDetailImageCommandService, SettlementProductImageQueryService, SettlementProductDetailImageQueryService, NginxCachePurgeUtil, JobLauncher, Job
- `SettlementProductColorQueryService` service/productColor/SettlementProductColorQueryService.java · public: findById, findAllByProductId · 주입: ProductColorRepository
- `SettlementProductDetailImageCommandService` service/productDetailImage/SettlementProductDetailImageCommandService.java · public: createProductDetailImage, deleteProductDetailImage · 주입: SettlementProductDetailImageRepository
- `SettlementProductDetailImageQueryService` service/productDetailImage/SettlementProductDetailImageQueryService.java · public: findAllByProductColorId, existsByProductColorId · 주입: SettlementProductDetailImageRepository
- `SettlementProductImageCommandService` service/productImage/SettlementProductImageCommandService.java · public: createProductImage, deleteProductImage · 주입: ProductImageRepository
- `SettlementProductImageQueryService` service/productImage/SettlementProductImageQueryService.java · public: findAllByProductColorId, existsByProductColorId · 주입: ProductImageRepository
- `SettlementProductSizeCommandService` service/productSize/SettlementProductSizeCommandService.java · public: createProductSizes, deleteProductSize, updateProductSize, deleteAllSizesByProductColor · 주입: ProductSizeRepository, SettlementCategoryQueryService
- `SettlementProductSizeQueryService` service/productSize/SettlementProductSizeQueryService.java · public: findByColorIdAndSize, findSizesByColorId, findById, findAllByProductColorId · 주입: ProductSizeRepository

### repository (10)

- `SettlementProductColorEsRepository` (interface) elasticsearch/repository/SettlementProductColorEsRepository.java · extends/implements ElasticsearchRepository
- `SettlementProductColorIndexQueryRepository` elasticsearch/repository/SettlementProductColorIndexQueryRepository.java · public: findAllForIndexingDto, findAllSizesForIndexing, findOneForIndexingDto, findSizesByColorId · 주입: JPAQueryFactory
- `SettlementCollectionRepository` (interface) repository/SettlementCollectionRepository.java · extends/implements JpaRepository · public: findByName, existsByName, findAllByOrderByNameDesc, findByNameContainingIgnoreCase
- `SettlementIndexedProductRepository` repository/SettlementIndexedProductRepository.java · public: findBrandIdsByNames, findCategoryIdsByNames, findCollectionIdsByNames, searchProductsByNames · 주입: JPAQueryFactory
- `SettlementInterestQueryDslRepository` repository/SettlementInterestQueryDslRepository.java · public: findUserInterestProducts · 주입: JPAQueryFactory
- `SettlementInterestRepository` (interface) repository/SettlementInterestRepository.java · extends/implements JpaRepository · public: existsByUserAndProductColor, findByUserAndProductColor, findAllByUserId
- `SettlementProductColorViewLogRepository` (interface) repository/SettlementProductColorViewLogRepository.java · extends/implements JpaRepository
- `SettlementProductDetailImageRepository` (interface) repository/SettlementProductDetailImageRepository.java · extends/implements JpaRepository · public: existsByProductColorId, findByProductColorId, findAllByProductColorId
- `SettlementProductPriceHistoryRepository` (interface) repository/SettlementProductPriceHistoryRepository.java · extends/implements JpaRepository · public: findByProductSizeId
- `SettlementProductQueryDslRepository` repository/SettlementProductQueryDslRepository.java · public: searchProducts, countProductsByFilter, findProductDetail, searchProductsByColorIds, tradeCountQuery, styleCountQuery · 주입: JPAQueryFactory

### event (2)

- `SettlementViewEventConsumer` service/kafka/SettlementViewEventConsumer.java · public: listen · 주입: ProductColorRepository, SettlementProductColorViewLogRepository
- `SettlementViewEventProducer` service/kafka/SettlementViewEventProducer.java · public: sendViewEvent · 주입: KafkaTemplate

### config (2)

- `SettlementViewEventKafkaConfig` service/kafka/config/SettlementViewEventKafkaConfig.java · public: viewEventProducerFactory, viewEventKafkaTemplate, viewEventConsumerFactory, viewEventKafkaListenerContainerFactory
- `SettlementCreateSizesJobConfig` service/productSize/SettlementCreateSizesJobConfig.java · public: createSizesJob, createSizesStep, sizeItemReader, sizeItemProcessor, sizeItemWriter, skipListener · 주입: JobRepository, PlatformTransactionManager, ProductColorRepository, ProductSizeRepository, SettlementCategoryQueryService

### other (2)

- `SettlementProductColorIndexInitializer` elasticsearch/config/SettlementProductColorIndexInitializer.java · extends/implements CommandLineRunner · public: run · 주입: SettlementProductColorIndexingService, ElasticsearchOperations
- `SettlementProductColorIndex` elasticsearch/index/SettlementProductColorIndex.java

### 다른 도메인 참조 (import·주입 기준)

- **order**: `BidStatus` ← SettlementProductQueryDslRepository; `OrderBidRepository` ← SettlementProductColorSearchService, SettlementProductQueryService
- **product**: `Brand` ← SettlementBrandCommandService, SettlementBrandEntityService, SettlementBrandQueryService, SettlementProductCommandService; `BrandRepository` ← SettlementBrandCommandService, SettlementBrandEntityService, SettlementBrandQueryService; `Category` ← SettlementCategoryCommandService, SettlementCategoryEntityService, SettlementCategoryQueryService, SettlementCreateSizesJobConfig, SettlementProductCommandService, SettlementProductSizeCommandService; `CategoryRepository` ← SettlementCategoryCommandService, SettlementCategoryEntityService, SettlementCategoryQueryService; `Collection` ← SettlementCollectionCommandService, SettlementCollectionQueryService, SettlementCollectionRepository, SettlementProductCommandService; `ColorType` ← SettlementFilterService, SettlementProductColorCommandService; `GenderType` ← SettlementFilterService, SettlementIndexedProductRepository, SettlementProductColorSearchController, SettlementProductQueryDslRepository, SettlementProductQueryService; `Interest` ← SettlementInterestCommandService, SettlementInterestRepository; `Product` ← SettlementProductCommandService, SettlementProductEntityService, SettlementProductQueryControllerForAdmin; `ProductColor` ← SettlementCreateSizesJobConfig, SettlementInterestCommandService, SettlementInterestRepository, SettlementProductColorQueryService, SettlementProductDetailImageCommandService, SettlementProductImageCommandService, SettlementProductQueryControllerForAdmin, SettlementProductSizeCommandService, SettlementViewEventConsumer; `ProductColorRepository` ← SettlementCreateSizesJobConfig, SettlementProductColorCommandService, SettlementProductColorQueryService, SettlementViewEventConsumer; `ProductColorViewLog` ← SettlementProductColorViewLogRepository, SettlementViewEventConsumer; `ProductDetailImage` ← SettlementProductDetailImageCommandService, SettlementProductDetailImageQueryService, SettlementProductDetailImageRepository; `ProductImage` ← SettlementProductImageCommandService, SettlementProductImageQueryService; `ProductImageRepository` ← SettlementProductImageCommandService, SettlementProductImageQueryService; `ProductPriceHistory` ← SettlementProductPriceHistoryRepository; `ProductRepository` ← SettlementBrandCommandService, SettlementCategoryCommandService, SettlementCollectionCommandService, SettlementProductCommandService, SettlementProductEntityService; `ProductSize` ← SettlementCreateSizesJobConfig, SettlementProductSizeCommandService, SettlementProductSizeQueryService; `ProductSizeRepository` ← SettlementCreateSizesJobConfig, SettlementProductSizeCommandService, SettlementProductSizeQueryService; `SizeType` ← SettlementCreateSizesJobConfig, SettlementFilterService, SettlementProductSizeCommandService; `ViewEvent` ← SettlementViewEventConsumer, SettlementViewEventKafkaConfig, SettlementViewEventProducer
- **settlementuser**: `SettlementUserQueryService` ← SettlementBrandController, SettlementCategoryController, SettlementCollectionController, SettlementInterestCommandService, SettlementProductColorCommandController, SettlementProductCommandController, SettlementProductQueryController, SettlementProductQueryControllerForAdmin
- **style**: `StyleRepository` ← SettlementProductColorSearchService, SettlementProductQueryService
- **user**: `Gender` ← SettlementProductQueryController, SettlementViewEventProducer; `User` ← SettlementInterestCommandService, SettlementInterestRepository

### 이 도메인을 참조하는 다른 도메인

- **settlementevent**: SettlementBrandQueryService
- **settlementorder**: SettlementProductSizeQueryService
- **settlementsale**: SettlementProductSizeQueryService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

- 발행 kafka `view-log-topic`: `SettlementViewEventProducer#sendViewEvent` → `AdminViewEventConsumer#listen` [adminproduct], `InternalViewEventConsumer#listen` [internalproduct], `LegacyViewEventConsumer#listen` [legacyproduct], `PartnerViewEventConsumer#listen` [partnerproduct], `ViewEventConsumer#listen` [product], `SettlementViewEventConsumer#listen` [settlementproduct]
- 구독 kafka `view-log-topic`: `SettlementViewEventConsumer#listen` (groupId=my-group containerFactory=viewEventKafkaListenerContainerFactory) ← `AdminViewEventProducer#sendViewEvent` [adminproduct], `InternalViewEventProducer#sendViewEvent` [internalproduct], `LegacyViewEventProducer#sendViewEvent` [legacyproduct], `PartnerViewEventProducer#sendViewEvent` [partnerproduct], `ViewEventProducer#sendViewEvent` [product], `SettlementViewEventProducer#sendViewEvent` [settlementproduct]

## 6. AOP

(없음)

