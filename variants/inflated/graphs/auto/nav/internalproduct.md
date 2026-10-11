# internalproduct 도메인

기준 경로: `src/main/java/com/fream/back/domain/internalproduct/` · 파일 52개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/internal/brands` | `InternalBrandController#createBrand` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalBrandCommandService#createBrand` |
| PUT | `/internal/brands/{brandId}` | `InternalBrandController#updateBrand` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalBrandCommandService#updateBrand` |
| DELETE | `/internal/brands/{brandName}` | `InternalBrandController#deleteBrand` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalBrandCommandService#deleteBrand` |
| GET | `/internal/brands` | `InternalBrandController#getAllBrands` | `InternalBrandQueryService#findAllBrands` |
| GET | `/internal/brands/page` | `InternalBrandController#getBrandsPaging` | `InternalBrandQueryService#findBrandsPaging` |
| GET | `/internal/brands/search` | `InternalBrandController#searchBrands` | `InternalBrandQueryService#searchBrandsByName` |
| GET | `/internal/brands/{brandId:[0-9]+}` | `InternalBrandController#getBrandById` | `InternalBrandQueryService#findBrandById` |
| GET | `/internal/brands/name/{brandName}` | `InternalBrandController#getBrandByName` | `InternalBrandQueryService#findByName` |
| POST | `/internal/categories` | `InternalCategoryController#createCategory` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalCategoryCommandService#createCategory` |
| PUT | `/internal/categories/{categoryId}` | `InternalCategoryController#updateCategory` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalCategoryCommandService#updateCategory` |
| DELETE | `/internal/categories/{categoryId}` | `InternalCategoryController#deleteCategory` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalCategoryCommandService#deleteCategory` |
| GET | `/internal/categories/main` | `InternalCategoryController#getAllMainCategories` | `InternalCategoryQueryService#findAllMainCategories` |
| GET | `/internal/categories/sub/{mainCategoryName}` | `InternalCategoryController#getSubCategoriesByMain` | `InternalCategoryQueryService#findSubCategoriesByMainCategory` |
| POST | `/internal/collections` | `InternalCollectionController#createCollection` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalCollectionCommandService#createCollection` |
| PUT | `/internal/collections/{collectionId}` | `InternalCollectionController#updateCollection` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalCollectionCommandService#updateCollection` |
| DELETE | `/internal/collections/{collectionName}` | `InternalCollectionController#deleteCollection` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalCollectionCommandService#deleteCollection` |
| GET | `/internal/collections` | `InternalCollectionController#getAllCollections` | `InternalCollectionQueryService#findAllCollections` |
| GET | `/internal/collections/page` | `InternalCollectionController#getCollectionsPaging` | `InternalCollectionQueryService#findCollectionsPaging` |
| GET | `/internal/collections/search` | `InternalCollectionController#searchCollections` | `InternalCollectionQueryService#searchCollectionsByName` |
| GET | `/internal/collections/{collectionId:[0-9]+}` | `InternalCollectionController#getCollectionById` | `InternalCollectionQueryService#findCollectionById` |
| GET | `/internal/collections/name/{collectionName}` | `InternalCollectionController#getCollectionByName` | `InternalCollectionQueryService#findByName` |
| POST | `/internal/interests/{productColorId}/toggle` | `InternalInterestCommandController#toggleInterest` | `this#extractEmailFromSecurityContext`, `InternalInterestCommandService#toggleInterest` |
| POST | `/internal/product-colors/{productId}` | `InternalProductColorCommandController#createProductColor` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalProductColorCommandService#createProductColor`, `NginxCachePurgeUtil#purgeProductCache`, `NginxCachePurgeUtil#purgeEsCache` |
| PUT | `/internal/product-colors/{productColorId}` | `InternalProductColorCommandController#updateProductColor` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalProductColorCommandService#updateProductColor`, `NginxCachePurgeUtil#purgeProductCache`, `NginxCachePurgeUtil#purgeEsCache` |
| DELETE | `/internal/product-colors/{productColorId}` | `InternalProductColorCommandController#deleteProductColor` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalProductColorCommandService#deleteProductColor`, `NginxCachePurgeUtil#purgeProductCache`, `NginxCachePurgeUtil#purgeEsCache` |
| POST | `/internal/products/command` | `InternalProductCommandController#createProduct` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalProductCommandService#createProduct` |
| PUT | `/internal/products/command/{productId}` | `InternalProductCommandController#updateProduct` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalProductCommandService#updateProduct` |
| DELETE | `/internal/products/command/{productId}` | `InternalProductCommandController#deleteProduct` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalProductCommandService#deleteProduct` |
| GET | `/internal/products/indexed/search` | `InternalIndexedProductQueryController#searchProductsByNames` | `InternalIndexedProductQueryService#searchProductsByNames`, `this#toPageDto` |
| GET | `/internal/interests/{userId}` | `InternalInterestQueryController#getUserInterestProducts` | `InternalInterestQueryService#findUserInterestProducts` |
| GET | `/internal/products/query` | `InternalProductQueryController#searchProducts` | `InternalProductQueryService#searchProducts`, `this#toPageDto` |
| GET | `/internal/products/query/{productId}/detail` | `InternalProductQueryController#getProductDetail` | `InternalProductQueryService#getProductDetail`, `InternalViewEventProducer#sendViewEvent` |
| GET | `/internal/products/query/{productId}/images` | `InternalProductQueryController#getProductImage` |  |
| GET | `/internal/products/query/filters` | `InternalProductQueryController#getFilterData` | `InternalFilterService#getAllFilterData` |
| POST | `/internal/products/query/filters/count` | `InternalProductQueryController#countProductsByFilter` | `InternalProductQueryService#countProductsByFilter` |
| GET | `/internal/admin/products/query/{productId}/detail` | `InternalProductQueryControllerForAdmin#getProductDetailForAdmin` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalProductEntityService#findById` |
| GET | `/internal/admin/products/query/colors/{colorId}` | `InternalProductQueryControllerForAdmin#getProductColorDetail` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalProductColorQueryService#findById` |
| GET | `/internal/admin/products/query/{productId}/colors` | `InternalProductQueryControllerForAdmin#getProductColors` | `this#extractEmailFromSecurityContext`, `InternalUserQueryService#checkAdminRole`, `InternalProductEntityService#findById` |
| GET | `/internal/es/products` | `InternalProductColorSearchController#esSearchProducts` | `this#convertGenders`, `InternalProductColorSearchService#searchToDto`, `this#toPageDto` |
| GET | `/internal/es/products/autocomplete` | `InternalProductColorSearchController#autocompleteProducts` | `InternalProductColorSearchService#autocomplete` |
| GET | `/internal/es/products/raw-indices` | `InternalProductColorSearchController#getRawIndices` | `InternalProductColorSearchService#getAllIndexedColors`, `this#toPageDto` |
| GET | `/internal/es/products/raw-indices/category/{categoryId}` | `InternalProductColorSearchController#getRawIndicesByCategory` | `InternalProductColorSearchService#getIndexedColorsByCategory`, `this#toPageDto` |
| GET | `/internal/es/products/index-stats` | `InternalProductColorSearchController#getIndexStats` | `InternalProductColorSearchService#getIndexStats` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `InternalBrandCommandService`
  - `createBrand` → BrandRepository#existsByName, BrandRepository#save~
  - `updateBrand` → BrandRepository#findById~, BrandRepository#existsByName
  - `deleteBrand` → BrandRepository#findByName, ProductRepository#existsByBrand, BrandRepository#delete~
- `InternalBrandEntityService`
  - `findByName` → BrandRepository#findByName
- `InternalBrandQueryService`
  - `findByName` → BrandRepository#findByName
  - `findAllBrands` → BrandRepository#findAllByOrderByNameDesc
  - `findById` → BrandRepository#findById~
  - `findBrandById` → BrandRepository#findById~
  - `findBrandsPaging` → BrandRepository#findAll~
  - `searchBrandsByName` → BrandRepository#findByNameContainingIgnoreCase
- `InternalCategoryCommandService`
  - `createCategory` → CategoryRepository#existsByNameAndParentCategoryIsNull, CategoryRepository#save~, CategoryRepository#findByNameAndParentCategoryIsNull, CategoryRepository#existsByNameAndParentCategory
  - `updateCategory` → CategoryRepository#findById~, CategoryRepository#existsByNameAndParentCategoryIsNull, CategoryRepository#existsByNameAndParentCategory
  - `deleteCategory` → CategoryRepository#findById~, CategoryRepository#existsByParentCategory, ProductRepository#existsByCategory, CategoryRepository#delete~
- `InternalCategoryEntityService`
  - `findMainCategoryByName` → CategoryRepository#findByNameAndParentCategoryIsNull
  - `findSubCategoryByName` → this#findMainCategoryByName, CategoryRepository#findByNameAndParentCategory
- `InternalCategoryQueryService`
  - `findAllMainCategories` → CategoryRepository#findByParentCategoryIsNullOrderByNameDesc
  - `findSubCategoriesByMainCategory` → CategoryRepository#findByNameAndParentCategoryIsNull, CategoryRepository#findByParentCategoryOrderByNameDesc
  - `findRootCategoryById` → CategoryRepository#findWithParentById, this#findRootCategory
  - `findSubCategoriesByParentId` → CategoryRepository#findByParentCategoryId
  - `findSubCategoriesByParentName` → CategoryRepository#findByParentCategoryName
- `InternalCollectionCommandService`
  - `createCollection` → InternalCollectionRepository#findByName, InternalCollectionRepository#save~
  - `updateCollection` → InternalCollectionRepository#findById~, InternalCollectionRepository#existsByName
  - `deleteCollection` → InternalCollectionRepository#findByName, ProductRepository#existsByCollection, InternalCollectionRepository#delete~
  - `createOrGetCollection` → InternalCollectionRepository#findByName, InternalCollectionRepository#save~
- `InternalCollectionQueryService`
  - `findAllCollections` → InternalCollectionRepository#findAllByOrderByNameDesc
  - `findByName` → InternalCollectionRepository#findByName
  - `findCollectionById` → InternalCollectionRepository#findById~
  - `findCollectionsPaging` → InternalCollectionRepository#findAll~
  - `searchCollectionsByName` → InternalCollectionRepository#findByNameContainingIgnoreCase
- `InternalCreateSizesJobConfig`
  - `createSizesJob` → this#createSizesStep
  - `createSizesStep` → this#sizeItemReader, this#sizeItemProcessor, this#sizeItemWriter, this#skipListener
  - `sizeItemReader` → InternalCategoryQueryService#findRootCategoryById, this#determineSizeType, ProductSizeRepository#findAllByProductColorId
  - `sizeItemProcessor` → this#isValidSize, ProductColorRepository#findById~
  - `sizeItemWriter` → ProductSizeRepository#save~
- `InternalFilterService`
  - `getAllFilterData` → this#getGenderLabel, this#getDiscountData, this#getPriceRangeData, InternalCategoryQueryService#findAllMainCategories, InternalCategoryQueryService#findSubCategoriesByMainCategory, this#processCategoryData, InternalBrandQueryService#findAllBrands, InternalCollectionQueryService#findAllCollections
  - `getPriceRangeData` → this#createPriceRange
  - `processCategoryData` → InternalCategoryQueryService#findSubCategoriesByParentId
- `InternalIndexedProductQueryService`
  - `searchProductsByNames` → InternalIndexedProductRepository#searchProductsByNames, InternalProductQueryDslRepository#styleCountQuery, InternalProductQueryDslRepository#tradeCountQuery
- `InternalIndexedProductRepository`
  - `searchProductsByNames` → this#findBrandIdsByNames, this#findCategoryIdsByNames, this#findCollectionIdsByNames, this#searchProductsByIdsOptimized
  - `searchProductsByIdsOptimized` → this#buildKeywordPredicateOptimized, this#buildCategoryIdsPredicate, this#buildGenderPredicate, this#buildBrandIdsPredicate, this#buildCollectionIdsPredicate, this#buildColorPredicate, this#buildSizePredicate, this#buildPricePredicate, this#applySortingOptimized, this#countProductsByConditions
  - `countProductsByConditions` → this#buildKeywordPredicateOptimized, this#buildCategoryIdsPredicate, this#buildGenderPredicate, this#buildBrandIdsPredicate, this#buildCollectionIdsPredicate, this#buildColorPredicate, this#buildSizePredicate, this#buildPricePredicate
- `InternalInterestCommandService`
  - `toggleInterest` → InternalUserQueryService#findByEmail, InternalProductColorQueryService#findById, InternalInterestRepository#findByUserAndProductColor, InternalInterestRepository#delete~, InternalInterestRepository#save~
  - `deleteAllInterestsByProductColor` → InternalInterestRepository#delete~
- `InternalInterestQueryDslRepository`
  - `findUserInterestProducts` → this#getOrderByClause
- `InternalInterestQueryService`
  - `findUserInterestProducts` → InternalInterestQueryDslRepository#findUserInterestProducts
- `InternalProductColorCommandService`
  - `createProductColor` → this#validateColorType, InternalProductEntityService#findById, FileUtils#saveFile, ProductColorRepository#save~, InternalProductSizeCommandService#createProductSizes
  - `updateProductColor` → ProductColorRepository#findById~, this#validateColorType, FileUtils#deleteFile, FileUtils#saveFile, InternalProductSizeCommandService#deleteProductSize, InternalProductSizeCommandService#createProductSizes, ProductColorRepository#save~
  - `deleteProductColor` → ProductColorRepository#findById~, InternalInterestCommandService#deleteAllInterestsByProductColor, InternalProductSizeCommandService#deleteAllSizesByProductColor, FileUtils#deleteFile, InternalProductImageCommandService#deleteProductImage, InternalProductImageQueryService#existsByProductColorId, InternalProductImageQueryService#findAllByProductColorId, FileUtils#existsFile, InternalProductDetailImageQueryService#existsByProductColorId, InternalProductDetailImageQueryService#findAllByProductColorId, InternalProductDetailImageCommandService#deleteProductDetailImage, ProductColorRepository#delete~
- `InternalProductColorIndexInitializer`
  - `run` → InternalProductColorIndexingService#indexAllColors
- `InternalProductColorIndexingService`
  - `indexAllColors` → InternalProductColorIndexQueryRepository#findAllForIndexingDto, InternalProductColorIndexQueryRepository#findAllSizesForIndexing, InternalProductColorEsRepository#saveAll~
  - `indexColorById` → InternalProductColorIndexQueryRepository#findOneForIndexingDto, InternalProductColorIndexQueryRepository#findSizesByColorId, InternalProductColorEsRepository#save~
  - `deleteColorFromIndex` → InternalProductColorEsRepository#deleteById~
- `InternalProductColorQueryService`
  - `findById` → ProductColorRepository#findById~
  - `findAllByProductId` → ProductColorRepository#findByProductId
- `InternalProductColorSearchService`
  - `searchToDto` → this#search, StyleRepository#styleCountByColorIds, OrderBidRepository#tradeCountByColorIds
  - `search` → this#rangeQuery
- `InternalProductCommandService`
  - `createProduct` → InternalBrandEntityService#findByName, InternalCategoryEntityService#findSubCategoryByName, InternalCollectionCommandService#createOrGetCollection, ProductRepository#save~
  - `updateProduct` → ProductRepository#findById~, InternalBrandEntityService#findByName, InternalCategoryEntityService#findSubCategoryByName, InternalCollectionCommandService#createOrGetCollection
  - `deleteProduct` → ProductRepository#findById~, InternalProductColorCommandService#deleteProductColor, ProductRepository#delete~
- `InternalProductDetailImageCommandService`
  - `createProductDetailImage` → InternalProductDetailImageRepository#save~
  - `deleteProductDetailImage` → InternalProductDetailImageRepository#findById~, InternalProductDetailImageRepository#delete~
- `InternalProductDetailImageQueryService`
  - `findAllByProductColorId` → InternalProductDetailImageRepository#findAllByProductColorId
  - `existsByProductColorId` → InternalProductDetailImageRepository#existsByProductColorId
- `InternalProductEntityService`
  - `findById` → ProductRepository#findById~
- `InternalProductImageCommandService`
  - `createProductImage` → ProductImageRepository#save~
  - `deleteProductImage` → ProductImageRepository#findById~, ProductImageRepository#delete~
- `InternalProductImageQueryService`
  - `findAllByProductColorId` → ProductImageRepository#findAllByProductColorId
  - `existsByProductColorId` → ProductImageRepository#existsByProductColorId
- `InternalProductQueryDslRepository`
  - `searchProducts` → this#logPredicate, this#buildKeywordPredicate, this#buildCategoryPredicate, this#buildGenderPredicate, this#buildBrandPredicate, this#buildCollectionPredicate, this#buildColorPredicate, this#buildSizePredicate, this#buildPricePredicate
  - `countProductsByFilter` → this#buildKeywordPredicate, this#buildCategoryPredicate, this#buildGenderPredicate, this#buildBrandPredicate, this#buildCollectionPredicate, this#buildColorPredicate, this#buildSizePredicate, this#buildPricePredicate
- `InternalProductQueryService`
  - `searchProducts` → InternalProductQueryDslRepository#searchProducts, StyleRepository#styleCountByColorIds, OrderBidRepository#tradeCountByColorIds
  - `getProductDetail` → InternalProductQueryDslRepository#findProductDetail
  - `searchProductsByColorIds` → InternalProductQueryDslRepository#searchProductsByColorIds
  - `countProductsByFilter` → InternalProductQueryDslRepository#countProductsByFilter
- `InternalProductSizeCommandService`
  - `createProductSizes` → InternalCategoryQueryService#findRootCategoryById, this#determineSizeType, ProductSizeRepository#findAllByProductColorId, this#isValidSize, ProductSizeRepository#save~
  - `deleteProductSize` → ProductSizeRepository#findById~, ProductSizeRepository#delete~
  - `updateProductSize` → ProductSizeRepository#findById~
  - `deleteAllSizesByProductColor` → ProductSizeRepository#findAllByProductColorId, this#deleteProductSize, ProductSizeRepository#flush~
- `InternalProductSizeQueryService`
  - `findByColorIdAndSize` → ProductSizeRepository#findByProductColorIdAndSize
  - `findSizesByColorId` → ProductSizeRepository#findAllByProductColorId
  - `findById` → ProductSizeRepository#findById~
  - `findAllByProductColorId` → ProductSizeRepository#findAllByProductColorId
- `InternalViewEventConsumer`
  - `listen` → ProductColorRepository#findById~, InternalProductColorViewLogRepository#save~
- `InternalViewEventKafkaConfig`
  - `viewEventKafkaTemplate` → this#viewEventProducerFactory
  - `viewEventKafkaListenerContainerFactory` → this#viewEventConsumerFactory
- `InternalViewEventProducer`
  - `sendViewEvent` → kafka:view-log-topic

## 3. 클래스 목록 (역할별)

### controller (11)

- `InternalBrandController` controller/InternalBrandController.java · public: createBrand, updateBrand, deleteBrand, getAllBrands, getBrandsPaging, searchBrands, getBrandById, getBrandByName · 주입: InternalBrandCommandService, InternalBrandQueryService, InternalUserQueryService
- `InternalCategoryController` controller/InternalCategoryController.java · public: createCategory, updateCategory, deleteCategory, getAllMainCategories, getSubCategoriesByMain · 주입: InternalCategoryCommandService, InternalCategoryQueryService, InternalUserQueryService
- `InternalCollectionController` controller/InternalCollectionController.java · public: createCollection, updateCollection, deleteCollection, getAllCollections, getCollectionsPaging, searchCollections, getCollectionById, getCollectionByName · 주입: InternalCollectionCommandService, InternalCollectionQueryService, InternalUserQueryService
- `InternalInterestCommandController` controller/command/InternalInterestCommandController.java · public: toggleInterest · 주입: InternalInterestCommandService
- `InternalProductColorCommandController` controller/command/InternalProductColorCommandController.java · public: createProductColor, updateProductColor, deleteProductColor · 주입: InternalProductColorCommandService, InternalUserQueryService, NginxCachePurgeUtil
- `InternalProductCommandController` controller/command/InternalProductCommandController.java · public: createProduct, updateProduct, deleteProduct · 주입: InternalProductCommandService, InternalUserQueryService
- `InternalIndexedProductQueryController` controller/query/InternalIndexedProductQueryController.java · public: searchProductsByNames · 주입: InternalIndexedProductQueryService
- `InternalInterestQueryController` controller/query/InternalInterestQueryController.java · public: getUserInterestProducts · 주입: InternalInterestQueryService
- `InternalProductQueryController` controller/query/InternalProductQueryController.java · public: searchProducts, getProductDetail, getProductImage, getFilterData, countProductsByFilter · 주입: InternalProductQueryService, InternalUserQueryService, InternalViewEventProducer, InternalFilterService
- `InternalProductQueryControllerForAdmin` controller/query/InternalProductQueryControllerForAdmin.java · public: getProductDetailForAdmin, getProductColorDetail, getProductColors · 주입: InternalProductQueryService, InternalProductEntityService, InternalProductColorQueryService, InternalUserQueryService
- `InternalProductColorSearchController` elasticsearch/controller/InternalProductColorSearchController.java · public: esSearchProducts, autocompleteProducts, getRawIndices, getRawIndicesByCategory, getIndexStats · 주입: InternalProductColorSearchService, InternalProductQueryService

### service (25)

- `InternalProductColorIndexingService` elasticsearch/service/InternalProductColorIndexingService.java · public: indexAllColors, indexColorById, deleteColorFromIndex · 주입: InternalProductColorIndexQueryRepository, InternalProductColorEsRepository
- `InternalProductColorSearchService` elasticsearch/service/InternalProductColorSearchService.java · public: searchToDto, search, autocomplete, getAllIndexedColors, getIndexedColorsByCategory, getIndexStats · 주입: ElasticsearchOperations, InternalProductColorEsRepository, StyleRepository, OrderBidRepository, ObjectMapper
- `InternalBrandCommandService` service/brand/InternalBrandCommandService.java · public: createBrand, updateBrand, deleteBrand · 주입: BrandRepository, ProductRepository
- `InternalBrandEntityService` service/brand/InternalBrandEntityService.java · public: findByName · 주입: BrandRepository
- `InternalBrandQueryService` service/brand/InternalBrandQueryService.java · public: findByName, findAllBrands, findById, findBrandById, findBrandsPaging, searchBrandsByName · 주입: BrandRepository
- `InternalCategoryCommandService` service/category/InternalCategoryCommandService.java · public: createCategory, updateCategory, deleteCategory · 주입: CategoryRepository, ProductRepository
- `InternalCategoryEntityService` service/category/InternalCategoryEntityService.java · public: findMainCategoryByName, findSubCategoryByName · 주입: CategoryRepository
- `InternalCategoryQueryService` service/category/InternalCategoryQueryService.java · public: findAllMainCategories, findSubCategoriesByMainCategory, findRootCategory, findRootCategoryById, findSubCategoriesByParentId, findSubCategoriesByParentName · 주입: CategoryRepository
- `InternalCollectionCommandService` service/collection/InternalCollectionCommandService.java · public: createCollection, updateCollection, deleteCollection, createOrGetCollection · 주입: InternalCollectionRepository, ProductRepository
- `InternalCollectionQueryService` service/collection/InternalCollectionQueryService.java · public: findAllCollections, findByName, findCollectionById, findCollectionsPaging, searchCollectionsByName · 주입: InternalCollectionRepository
- `InternalFilterService` service/filter/InternalFilterService.java · public: getAllFilterData · 주입: InternalBrandQueryService, InternalCategoryQueryService, InternalCollectionQueryService
- `InternalInterestCommandService` service/interest/InternalInterestCommandService.java · public: toggleInterest, deleteAllInterestsByProductColor · 주입: InternalInterestRepository, InternalUserQueryService, InternalProductColorQueryService
- `InternalInterestQueryService` service/interest/InternalInterestQueryService.java · public: findUserInterestProducts · 주입: InternalInterestQueryDslRepository
- `InternalIndexedProductQueryService` service/product/InternalIndexedProductQueryService.java · public: searchProductsByNames · 주입: InternalIndexedProductRepository, InternalProductQueryDslRepository
- `InternalProductCommandService` service/product/InternalProductCommandService.java · public: createProduct, updateProduct, deleteProduct · 주입: ProductRepository, InternalBrandEntityService, InternalCategoryEntityService, InternalCollectionCommandService, InternalProductColorCommandService
- `InternalProductEntityService` service/product/InternalProductEntityService.java · public: findById · 주입: ProductRepository
- `InternalProductQueryService` service/product/InternalProductQueryService.java · public: searchProducts, getProductDetail, searchProductsByColorIds, countProductsByFilter · 주입: InternalProductQueryDslRepository, StyleRepository, OrderBidRepository
- `InternalProductColorCommandService` service/productColor/InternalProductColorCommandService.java · public: createProductColor, updateProductColor, deleteProductColor · 주입: ProductColorRepository, InternalProductSizeCommandService, FileUtils, InternalProductEntityService, InternalProductSizeQueryService, InternalInterestCommandService, InternalProductImageCommandService, InternalProductDetailImageCommandService, InternalProductImageQueryService, InternalProductDetailImageQueryService, NginxCachePurgeUtil, JobLauncher, Job
- `InternalProductColorQueryService` service/productColor/InternalProductColorQueryService.java · public: findById, findAllByProductId · 주입: ProductColorRepository
- `InternalProductDetailImageCommandService` service/productDetailImage/InternalProductDetailImageCommandService.java · public: createProductDetailImage, deleteProductDetailImage · 주입: InternalProductDetailImageRepository
- `InternalProductDetailImageQueryService` service/productDetailImage/InternalProductDetailImageQueryService.java · public: findAllByProductColorId, existsByProductColorId · 주입: InternalProductDetailImageRepository
- `InternalProductImageCommandService` service/productImage/InternalProductImageCommandService.java · public: createProductImage, deleteProductImage · 주입: ProductImageRepository
- `InternalProductImageQueryService` service/productImage/InternalProductImageQueryService.java · public: findAllByProductColorId, existsByProductColorId · 주입: ProductImageRepository
- `InternalProductSizeCommandService` service/productSize/InternalProductSizeCommandService.java · public: createProductSizes, deleteProductSize, updateProductSize, deleteAllSizesByProductColor · 주입: ProductSizeRepository, InternalCategoryQueryService
- `InternalProductSizeQueryService` service/productSize/InternalProductSizeQueryService.java · public: findByColorIdAndSize, findSizesByColorId, findById, findAllByProductColorId · 주입: ProductSizeRepository

### repository (10)

- `InternalProductColorEsRepository` (interface) elasticsearch/repository/InternalProductColorEsRepository.java · extends/implements ElasticsearchRepository
- `InternalProductColorIndexQueryRepository` elasticsearch/repository/InternalProductColorIndexQueryRepository.java · public: findAllForIndexingDto, findAllSizesForIndexing, findOneForIndexingDto, findSizesByColorId · 주입: JPAQueryFactory
- `InternalCollectionRepository` (interface) repository/InternalCollectionRepository.java · extends/implements JpaRepository · public: findByName, existsByName, findAllByOrderByNameDesc, findByNameContainingIgnoreCase
- `InternalIndexedProductRepository` repository/InternalIndexedProductRepository.java · public: findBrandIdsByNames, findCategoryIdsByNames, findCollectionIdsByNames, searchProductsByNames · 주입: JPAQueryFactory
- `InternalInterestQueryDslRepository` repository/InternalInterestQueryDslRepository.java · public: findUserInterestProducts · 주입: JPAQueryFactory
- `InternalInterestRepository` (interface) repository/InternalInterestRepository.java · extends/implements JpaRepository · public: existsByUserAndProductColor, findByUserAndProductColor, findAllByUserId
- `InternalProductColorViewLogRepository` (interface) repository/InternalProductColorViewLogRepository.java · extends/implements JpaRepository
- `InternalProductDetailImageRepository` (interface) repository/InternalProductDetailImageRepository.java · extends/implements JpaRepository · public: existsByProductColorId, findByProductColorId, findAllByProductColorId
- `InternalProductPriceHistoryRepository` (interface) repository/InternalProductPriceHistoryRepository.java · extends/implements JpaRepository · public: findByProductSizeId
- `InternalProductQueryDslRepository` repository/InternalProductQueryDslRepository.java · public: searchProducts, countProductsByFilter, findProductDetail, searchProductsByColorIds, tradeCountQuery, styleCountQuery · 주입: JPAQueryFactory

### event (2)

- `InternalViewEventConsumer` service/kafka/InternalViewEventConsumer.java · public: listen · 주입: ProductColorRepository, InternalProductColorViewLogRepository
- `InternalViewEventProducer` service/kafka/InternalViewEventProducer.java · public: sendViewEvent · 주입: KafkaTemplate

### config (2)

- `InternalViewEventKafkaConfig` service/kafka/config/InternalViewEventKafkaConfig.java · public: viewEventProducerFactory, viewEventKafkaTemplate, viewEventConsumerFactory, viewEventKafkaListenerContainerFactory
- `InternalCreateSizesJobConfig` service/productSize/InternalCreateSizesJobConfig.java · public: createSizesJob, createSizesStep, sizeItemReader, sizeItemProcessor, sizeItemWriter, skipListener · 주입: JobRepository, PlatformTransactionManager, ProductColorRepository, ProductSizeRepository, InternalCategoryQueryService

### other (2)

- `InternalProductColorIndexInitializer` elasticsearch/config/InternalProductColorIndexInitializer.java · extends/implements CommandLineRunner · public: run · 주입: InternalProductColorIndexingService, ElasticsearchOperations
- `InternalProductColorIndex` elasticsearch/index/InternalProductColorIndex.java

### 다른 도메인 참조 (import·주입 기준)

- **internaluser**: `InternalUserQueryService` ← InternalBrandController, InternalCategoryController, InternalCollectionController, InternalInterestCommandService, InternalProductColorCommandController, InternalProductCommandController, InternalProductQueryController, InternalProductQueryControllerForAdmin
- **order**: `BidStatus` ← InternalProductQueryDslRepository; `OrderBidRepository` ← InternalProductColorSearchService, InternalProductQueryService
- **product**: `Brand` ← InternalBrandCommandService, InternalBrandEntityService, InternalBrandQueryService, InternalProductCommandService; `BrandRepository` ← InternalBrandCommandService, InternalBrandEntityService, InternalBrandQueryService; `Category` ← InternalCategoryCommandService, InternalCategoryEntityService, InternalCategoryQueryService, InternalCreateSizesJobConfig, InternalProductCommandService, InternalProductSizeCommandService; `CategoryRepository` ← InternalCategoryCommandService, InternalCategoryEntityService, InternalCategoryQueryService; `Collection` ← InternalCollectionCommandService, InternalCollectionQueryService, InternalCollectionRepository, InternalProductCommandService; `ColorType` ← InternalFilterService, InternalProductColorCommandService; `GenderType` ← InternalFilterService, InternalIndexedProductRepository, InternalProductColorSearchController, InternalProductQueryDslRepository, InternalProductQueryService; `Interest` ← InternalInterestCommandService, InternalInterestRepository; `Product` ← InternalProductCommandService, InternalProductEntityService, InternalProductQueryControllerForAdmin; `ProductColor` ← InternalCreateSizesJobConfig, InternalInterestCommandService, InternalInterestRepository, InternalProductColorQueryService, InternalProductDetailImageCommandService, InternalProductImageCommandService, InternalProductQueryControllerForAdmin, InternalProductSizeCommandService, InternalViewEventConsumer; `ProductColorRepository` ← InternalCreateSizesJobConfig, InternalProductColorCommandService, InternalProductColorQueryService, InternalViewEventConsumer; `ProductColorViewLog` ← InternalProductColorViewLogRepository, InternalViewEventConsumer; `ProductDetailImage` ← InternalProductDetailImageCommandService, InternalProductDetailImageQueryService, InternalProductDetailImageRepository; `ProductImage` ← InternalProductImageCommandService, InternalProductImageQueryService; `ProductImageRepository` ← InternalProductImageCommandService, InternalProductImageQueryService; `ProductPriceHistory` ← InternalProductPriceHistoryRepository; `ProductRepository` ← InternalBrandCommandService, InternalCategoryCommandService, InternalCollectionCommandService, InternalProductCommandService, InternalProductEntityService; `ProductSize` ← InternalCreateSizesJobConfig, InternalProductSizeCommandService, InternalProductSizeQueryService; `ProductSizeRepository` ← InternalCreateSizesJobConfig, InternalProductSizeCommandService, InternalProductSizeQueryService; `SizeType` ← InternalCreateSizesJobConfig, InternalFilterService, InternalProductSizeCommandService; `ViewEvent` ← InternalViewEventConsumer, InternalViewEventKafkaConfig, InternalViewEventProducer
- **style**: `StyleRepository` ← InternalProductColorSearchService, InternalProductQueryService
- **user**: `Gender` ← InternalProductQueryController, InternalViewEventProducer; `User` ← InternalInterestCommandService, InternalInterestRepository

### 이 도메인을 참조하는 다른 도메인

- **internalevent**: InternalBrandQueryService
- **internalorder**: InternalProductSizeQueryService
- **internalsale**: InternalProductSizeQueryService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

- 발행 kafka `view-log-topic`: `InternalViewEventProducer#sendViewEvent` → `AdminViewEventConsumer#listen` [adminproduct], `InternalViewEventConsumer#listen` [internalproduct], `LegacyViewEventConsumer#listen` [legacyproduct], `PartnerViewEventConsumer#listen` [partnerproduct], `ViewEventConsumer#listen` [product], `SettlementViewEventConsumer#listen` [settlementproduct]
- 구독 kafka `view-log-topic`: `InternalViewEventConsumer#listen` (groupId=my-group containerFactory=viewEventKafkaListenerContainerFactory) ← `AdminViewEventProducer#sendViewEvent` [adminproduct], `InternalViewEventProducer#sendViewEvent` [internalproduct], `LegacyViewEventProducer#sendViewEvent` [legacyproduct], `PartnerViewEventProducer#sendViewEvent` [partnerproduct], `ViewEventProducer#sendViewEvent` [product], `SettlementViewEventProducer#sendViewEvent` [settlementproduct]

## 6. AOP

(없음)

