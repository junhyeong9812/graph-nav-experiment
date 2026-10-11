# adminproduct 도메인

기준 경로: `src/main/java/com/fream/back/domain/adminproduct/` · 파일 52개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/admin/brands` | `AdminBrandController#createBrand` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminBrandCommandService#createBrand` |
| PUT | `/admin/brands/{brandId}` | `AdminBrandController#updateBrand` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminBrandCommandService#updateBrand` |
| DELETE | `/admin/brands/{brandName}` | `AdminBrandController#deleteBrand` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminBrandCommandService#deleteBrand` |
| GET | `/admin/brands` | `AdminBrandController#getAllBrands` | `AdminBrandQueryService#findAllBrands` |
| GET | `/admin/brands/page` | `AdminBrandController#getBrandsPaging` | `AdminBrandQueryService#findBrandsPaging` |
| GET | `/admin/brands/search` | `AdminBrandController#searchBrands` | `AdminBrandQueryService#searchBrandsByName` |
| GET | `/admin/brands/{brandId:[0-9]+}` | `AdminBrandController#getBrandById` | `AdminBrandQueryService#findBrandById` |
| GET | `/admin/brands/name/{brandName}` | `AdminBrandController#getBrandByName` | `AdminBrandQueryService#findByName` |
| POST | `/admin/categories` | `AdminCategoryController#createCategory` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminCategoryCommandService#createCategory` |
| PUT | `/admin/categories/{categoryId}` | `AdminCategoryController#updateCategory` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminCategoryCommandService#updateCategory` |
| DELETE | `/admin/categories/{categoryId}` | `AdminCategoryController#deleteCategory` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminCategoryCommandService#deleteCategory` |
| GET | `/admin/categories/main` | `AdminCategoryController#getAllMainCategories` | `AdminCategoryQueryService#findAllMainCategories` |
| GET | `/admin/categories/sub/{mainCategoryName}` | `AdminCategoryController#getSubCategoriesByMain` | `AdminCategoryQueryService#findSubCategoriesByMainCategory` |
| POST | `/admin/collections` | `AdminCollectionController#createCollection` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminCollectionCommandService#createCollection` |
| PUT | `/admin/collections/{collectionId}` | `AdminCollectionController#updateCollection` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminCollectionCommandService#updateCollection` |
| DELETE | `/admin/collections/{collectionName}` | `AdminCollectionController#deleteCollection` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminCollectionCommandService#deleteCollection` |
| GET | `/admin/collections` | `AdminCollectionController#getAllCollections` | `AdminCollectionQueryService#findAllCollections` |
| GET | `/admin/collections/page` | `AdminCollectionController#getCollectionsPaging` | `AdminCollectionQueryService#findCollectionsPaging` |
| GET | `/admin/collections/search` | `AdminCollectionController#searchCollections` | `AdminCollectionQueryService#searchCollectionsByName` |
| GET | `/admin/collections/{collectionId:[0-9]+}` | `AdminCollectionController#getCollectionById` | `AdminCollectionQueryService#findCollectionById` |
| GET | `/admin/collections/name/{collectionName}` | `AdminCollectionController#getCollectionByName` | `AdminCollectionQueryService#findByName` |
| POST | `/admin/interests/{productColorId}/toggle` | `AdminInterestCommandController#toggleInterest` | `this#extractEmailFromSecurityContext`, `AdminInterestCommandService#toggleInterest` |
| POST | `/admin/product-colors/{productId}` | `AdminProductColorCommandController#createProductColor` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminProductColorCommandService#createProductColor`, `NginxCachePurgeUtil#purgeProductCache`, `NginxCachePurgeUtil#purgeEsCache` |
| PUT | `/admin/product-colors/{productColorId}` | `AdminProductColorCommandController#updateProductColor` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminProductColorCommandService#updateProductColor`, `NginxCachePurgeUtil#purgeProductCache`, `NginxCachePurgeUtil#purgeEsCache` |
| DELETE | `/admin/product-colors/{productColorId}` | `AdminProductColorCommandController#deleteProductColor` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminProductColorCommandService#deleteProductColor`, `NginxCachePurgeUtil#purgeProductCache`, `NginxCachePurgeUtil#purgeEsCache` |
| POST | `/admin/products/command` | `AdminProductCommandController#createProduct` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminProductCommandService#createProduct` |
| PUT | `/admin/products/command/{productId}` | `AdminProductCommandController#updateProduct` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminProductCommandService#updateProduct` |
| DELETE | `/admin/products/command/{productId}` | `AdminProductCommandController#deleteProduct` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminProductCommandService#deleteProduct` |
| GET | `/admin/products/indexed/search` | `AdminIndexedProductQueryController#searchProductsByNames` | `AdminIndexedProductQueryService#searchProductsByNames`, `this#toPageDto` |
| GET | `/admin/interests/{userId}` | `AdminInterestQueryController#getUserInterestProducts` | `AdminInterestQueryService#findUserInterestProducts` |
| GET | `/admin/products/query` | `AdminProductQueryController#searchProducts` | `AdminProductQueryService#searchProducts`, `this#toPageDto` |
| GET | `/admin/products/query/{productId}/detail` | `AdminProductQueryController#getProductDetail` | `AdminProductQueryService#getProductDetail`, `AdminViewEventProducer#sendViewEvent` |
| GET | `/admin/products/query/{productId}/images` | `AdminProductQueryController#getProductImage` |  |
| GET | `/admin/products/query/filters` | `AdminProductQueryController#getFilterData` | `AdminFilterService#getAllFilterData` |
| POST | `/admin/products/query/filters/count` | `AdminProductQueryController#countProductsByFilter` | `AdminProductQueryService#countProductsByFilter` |
| GET | `/admin/admin/products/query/{productId}/detail` | `AdminProductQueryControllerForAdmin#getProductDetailForAdmin` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminProductEntityService#findById` |
| GET | `/admin/admin/products/query/colors/{colorId}` | `AdminProductQueryControllerForAdmin#getProductColorDetail` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminProductColorQueryService#findById` |
| GET | `/admin/admin/products/query/{productId}/colors` | `AdminProductQueryControllerForAdmin#getProductColors` | `this#extractEmailFromSecurityContext`, `AdminUserQueryService#checkAdminRole`, `AdminProductEntityService#findById` |
| GET | `/admin/es/products` | `AdminProductColorSearchController#esSearchProducts` | `this#convertGenders`, `AdminProductColorSearchService#searchToDto`, `this#toPageDto` |
| GET | `/admin/es/products/autocomplete` | `AdminProductColorSearchController#autocompleteProducts` | `AdminProductColorSearchService#autocomplete` |
| GET | `/admin/es/products/raw-indices` | `AdminProductColorSearchController#getRawIndices` | `AdminProductColorSearchService#getAllIndexedColors`, `this#toPageDto` |
| GET | `/admin/es/products/raw-indices/category/{categoryId}` | `AdminProductColorSearchController#getRawIndicesByCategory` | `AdminProductColorSearchService#getIndexedColorsByCategory`, `this#toPageDto` |
| GET | `/admin/es/products/index-stats` | `AdminProductColorSearchController#getIndexStats` | `AdminProductColorSearchService#getIndexStats` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `AdminBrandCommandService`
  - `createBrand` → BrandRepository#existsByName, BrandRepository#save~
  - `updateBrand` → BrandRepository#findById~, BrandRepository#existsByName
  - `deleteBrand` → BrandRepository#findByName, ProductRepository#existsByBrand, BrandRepository#delete~
- `AdminBrandEntityService`
  - `findByName` → BrandRepository#findByName
- `AdminBrandQueryService`
  - `findByName` → BrandRepository#findByName
  - `findAllBrands` → BrandRepository#findAllByOrderByNameDesc
  - `findById` → BrandRepository#findById~
  - `findBrandById` → BrandRepository#findById~
  - `findBrandsPaging` → BrandRepository#findAll~
  - `searchBrandsByName` → BrandRepository#findByNameContainingIgnoreCase
- `AdminCategoryCommandService`
  - `createCategory` → CategoryRepository#existsByNameAndParentCategoryIsNull, CategoryRepository#save~, CategoryRepository#findByNameAndParentCategoryIsNull, CategoryRepository#existsByNameAndParentCategory
  - `updateCategory` → CategoryRepository#findById~, CategoryRepository#existsByNameAndParentCategoryIsNull, CategoryRepository#existsByNameAndParentCategory
  - `deleteCategory` → CategoryRepository#findById~, CategoryRepository#existsByParentCategory, ProductRepository#existsByCategory, CategoryRepository#delete~
- `AdminCategoryEntityService`
  - `findMainCategoryByName` → CategoryRepository#findByNameAndParentCategoryIsNull
  - `findSubCategoryByName` → this#findMainCategoryByName, CategoryRepository#findByNameAndParentCategory
- `AdminCategoryQueryService`
  - `findAllMainCategories` → CategoryRepository#findByParentCategoryIsNullOrderByNameDesc
  - `findSubCategoriesByMainCategory` → CategoryRepository#findByNameAndParentCategoryIsNull, CategoryRepository#findByParentCategoryOrderByNameDesc
  - `findRootCategoryById` → CategoryRepository#findWithParentById, this#findRootCategory
  - `findSubCategoriesByParentId` → CategoryRepository#findByParentCategoryId
  - `findSubCategoriesByParentName` → CategoryRepository#findByParentCategoryName
- `AdminCollectionCommandService`
  - `createCollection` → AdminCollectionRepository#findByName, AdminCollectionRepository#save~
  - `updateCollection` → AdminCollectionRepository#findById~, AdminCollectionRepository#existsByName
  - `deleteCollection` → AdminCollectionRepository#findByName, ProductRepository#existsByCollection, AdminCollectionRepository#delete~
  - `createOrGetCollection` → AdminCollectionRepository#findByName, AdminCollectionRepository#save~
- `AdminCollectionQueryService`
  - `findAllCollections` → AdminCollectionRepository#findAllByOrderByNameDesc
  - `findByName` → AdminCollectionRepository#findByName
  - `findCollectionById` → AdminCollectionRepository#findById~
  - `findCollectionsPaging` → AdminCollectionRepository#findAll~
  - `searchCollectionsByName` → AdminCollectionRepository#findByNameContainingIgnoreCase
- `AdminCreateSizesJobConfig`
  - `createSizesJob` → this#createSizesStep
  - `createSizesStep` → this#sizeItemReader, this#sizeItemProcessor, this#sizeItemWriter, this#skipListener
  - `sizeItemReader` → AdminCategoryQueryService#findRootCategoryById, this#determineSizeType, ProductSizeRepository#findAllByProductColorId
  - `sizeItemProcessor` → this#isValidSize, ProductColorRepository#findById~
  - `sizeItemWriter` → ProductSizeRepository#save~
- `AdminFilterService`
  - `getAllFilterData` → this#getGenderLabel, this#getDiscountData, this#getPriceRangeData, AdminCategoryQueryService#findAllMainCategories, AdminCategoryQueryService#findSubCategoriesByMainCategory, this#processCategoryData, AdminBrandQueryService#findAllBrands, AdminCollectionQueryService#findAllCollections
  - `getPriceRangeData` → this#createPriceRange
  - `processCategoryData` → AdminCategoryQueryService#findSubCategoriesByParentId
- `AdminIndexedProductQueryService`
  - `searchProductsByNames` → AdminIndexedProductRepository#searchProductsByNames, AdminProductQueryDslRepository#styleCountQuery, AdminProductQueryDslRepository#tradeCountQuery
- `AdminIndexedProductRepository`
  - `searchProductsByNames` → this#findBrandIdsByNames, this#findCategoryIdsByNames, this#findCollectionIdsByNames, this#searchProductsByIdsOptimized
  - `searchProductsByIdsOptimized` → this#buildKeywordPredicateOptimized, this#buildCategoryIdsPredicate, this#buildGenderPredicate, this#buildBrandIdsPredicate, this#buildCollectionIdsPredicate, this#buildColorPredicate, this#buildSizePredicate, this#buildPricePredicate, this#applySortingOptimized, this#countProductsByConditions
  - `countProductsByConditions` → this#buildKeywordPredicateOptimized, this#buildCategoryIdsPredicate, this#buildGenderPredicate, this#buildBrandIdsPredicate, this#buildCollectionIdsPredicate, this#buildColorPredicate, this#buildSizePredicate, this#buildPricePredicate
- `AdminInterestCommandService`
  - `toggleInterest` → AdminUserQueryService#findByEmail, AdminProductColorQueryService#findById, AdminInterestRepository#findByUserAndProductColor, AdminInterestRepository#delete~, AdminInterestRepository#save~
  - `deleteAllInterestsByProductColor` → AdminInterestRepository#delete~
- `AdminInterestQueryDslRepository`
  - `findUserInterestProducts` → this#getOrderByClause
- `AdminInterestQueryService`
  - `findUserInterestProducts` → AdminInterestQueryDslRepository#findUserInterestProducts
- `AdminProductColorCommandService`
  - `createProductColor` → this#validateColorType, AdminProductEntityService#findById, FileUtils#saveFile, ProductColorRepository#save~, AdminProductSizeCommandService#createProductSizes
  - `updateProductColor` → ProductColorRepository#findById~, this#validateColorType, FileUtils#deleteFile, FileUtils#saveFile, AdminProductSizeCommandService#deleteProductSize, AdminProductSizeCommandService#createProductSizes, ProductColorRepository#save~
  - `deleteProductColor` → ProductColorRepository#findById~, AdminInterestCommandService#deleteAllInterestsByProductColor, AdminProductSizeCommandService#deleteAllSizesByProductColor, FileUtils#deleteFile, AdminProductImageCommandService#deleteProductImage, AdminProductImageQueryService#existsByProductColorId, AdminProductImageQueryService#findAllByProductColorId, FileUtils#existsFile, AdminProductDetailImageQueryService#existsByProductColorId, AdminProductDetailImageQueryService#findAllByProductColorId, AdminProductDetailImageCommandService#deleteProductDetailImage, ProductColorRepository#delete~
- `AdminProductColorIndexInitializer`
  - `run` → AdminProductColorIndexingService#indexAllColors
- `AdminProductColorIndexingService`
  - `indexAllColors` → AdminProductColorIndexQueryRepository#findAllForIndexingDto, AdminProductColorIndexQueryRepository#findAllSizesForIndexing, AdminProductColorEsRepository#saveAll~
  - `indexColorById` → AdminProductColorIndexQueryRepository#findOneForIndexingDto, AdminProductColorIndexQueryRepository#findSizesByColorId, AdminProductColorEsRepository#save~
  - `deleteColorFromIndex` → AdminProductColorEsRepository#deleteById~
- `AdminProductColorQueryService`
  - `findById` → ProductColorRepository#findById~
  - `findAllByProductId` → ProductColorRepository#findByProductId
- `AdminProductColorSearchService`
  - `searchToDto` → this#search, StyleRepository#styleCountByColorIds, OrderBidRepository#tradeCountByColorIds
  - `search` → this#rangeQuery
- `AdminProductCommandService`
  - `createProduct` → AdminBrandEntityService#findByName, AdminCategoryEntityService#findSubCategoryByName, AdminCollectionCommandService#createOrGetCollection, ProductRepository#save~
  - `updateProduct` → ProductRepository#findById~, AdminBrandEntityService#findByName, AdminCategoryEntityService#findSubCategoryByName, AdminCollectionCommandService#createOrGetCollection
  - `deleteProduct` → ProductRepository#findById~, AdminProductColorCommandService#deleteProductColor, ProductRepository#delete~
- `AdminProductDetailImageCommandService`
  - `createProductDetailImage` → AdminProductDetailImageRepository#save~
  - `deleteProductDetailImage` → AdminProductDetailImageRepository#findById~, AdminProductDetailImageRepository#delete~
- `AdminProductDetailImageQueryService`
  - `findAllByProductColorId` → AdminProductDetailImageRepository#findAllByProductColorId
  - `existsByProductColorId` → AdminProductDetailImageRepository#existsByProductColorId
- `AdminProductEntityService`
  - `findById` → ProductRepository#findById~
- `AdminProductImageCommandService`
  - `createProductImage` → ProductImageRepository#save~
  - `deleteProductImage` → ProductImageRepository#findById~, ProductImageRepository#delete~
- `AdminProductImageQueryService`
  - `findAllByProductColorId` → ProductImageRepository#findAllByProductColorId
  - `existsByProductColorId` → ProductImageRepository#existsByProductColorId
- `AdminProductQueryDslRepository`
  - `searchProducts` → this#logPredicate, this#buildKeywordPredicate, this#buildCategoryPredicate, this#buildGenderPredicate, this#buildBrandPredicate, this#buildCollectionPredicate, this#buildColorPredicate, this#buildSizePredicate, this#buildPricePredicate
  - `countProductsByFilter` → this#buildKeywordPredicate, this#buildCategoryPredicate, this#buildGenderPredicate, this#buildBrandPredicate, this#buildCollectionPredicate, this#buildColorPredicate, this#buildSizePredicate, this#buildPricePredicate
- `AdminProductQueryService`
  - `searchProducts` → AdminProductQueryDslRepository#searchProducts, StyleRepository#styleCountByColorIds, OrderBidRepository#tradeCountByColorIds
  - `getProductDetail` → AdminProductQueryDslRepository#findProductDetail
  - `searchProductsByColorIds` → AdminProductQueryDslRepository#searchProductsByColorIds
  - `countProductsByFilter` → AdminProductQueryDslRepository#countProductsByFilter
- `AdminProductSizeCommandService`
  - `createProductSizes` → AdminCategoryQueryService#findRootCategoryById, this#determineSizeType, ProductSizeRepository#findAllByProductColorId, this#isValidSize, ProductSizeRepository#save~
  - `deleteProductSize` → ProductSizeRepository#findById~, ProductSizeRepository#delete~
  - `updateProductSize` → ProductSizeRepository#findById~
  - `deleteAllSizesByProductColor` → ProductSizeRepository#findAllByProductColorId, this#deleteProductSize, ProductSizeRepository#flush~
- `AdminProductSizeQueryService`
  - `findByColorIdAndSize` → ProductSizeRepository#findByProductColorIdAndSize
  - `findSizesByColorId` → ProductSizeRepository#findAllByProductColorId
  - `findById` → ProductSizeRepository#findById~
  - `findAllByProductColorId` → ProductSizeRepository#findAllByProductColorId
- `AdminViewEventConsumer`
  - `listen` → ProductColorRepository#findById~, AdminProductColorViewLogRepository#save~
- `AdminViewEventKafkaConfig`
  - `viewEventKafkaTemplate` → this#viewEventProducerFactory
  - `viewEventKafkaListenerContainerFactory` → this#viewEventConsumerFactory
- `AdminViewEventProducer`
  - `sendViewEvent` → kafka:view-log-topic

## 3. 클래스 목록 (역할별)

### controller (11)

- `AdminBrandController` controller/AdminBrandController.java · public: createBrand, updateBrand, deleteBrand, getAllBrands, getBrandsPaging, searchBrands, getBrandById, getBrandByName · 주입: AdminBrandCommandService, AdminBrandQueryService, AdminUserQueryService
- `AdminCategoryController` controller/AdminCategoryController.java · public: createCategory, updateCategory, deleteCategory, getAllMainCategories, getSubCategoriesByMain · 주입: AdminCategoryCommandService, AdminCategoryQueryService, AdminUserQueryService
- `AdminCollectionController` controller/AdminCollectionController.java · public: createCollection, updateCollection, deleteCollection, getAllCollections, getCollectionsPaging, searchCollections, getCollectionById, getCollectionByName · 주입: AdminCollectionCommandService, AdminCollectionQueryService, AdminUserQueryService
- `AdminInterestCommandController` controller/command/AdminInterestCommandController.java · public: toggleInterest · 주입: AdminInterestCommandService
- `AdminProductColorCommandController` controller/command/AdminProductColorCommandController.java · public: createProductColor, updateProductColor, deleteProductColor · 주입: AdminProductColorCommandService, AdminUserQueryService, NginxCachePurgeUtil
- `AdminProductCommandController` controller/command/AdminProductCommandController.java · public: createProduct, updateProduct, deleteProduct · 주입: AdminProductCommandService, AdminUserQueryService
- `AdminIndexedProductQueryController` controller/query/AdminIndexedProductQueryController.java · public: searchProductsByNames · 주입: AdminIndexedProductQueryService
- `AdminInterestQueryController` controller/query/AdminInterestQueryController.java · public: getUserInterestProducts · 주입: AdminInterestQueryService
- `AdminProductQueryController` controller/query/AdminProductQueryController.java · public: searchProducts, getProductDetail, getProductImage, getFilterData, countProductsByFilter · 주입: AdminProductQueryService, AdminUserQueryService, AdminViewEventProducer, AdminFilterService
- `AdminProductQueryControllerForAdmin` controller/query/AdminProductQueryControllerForAdmin.java · public: getProductDetailForAdmin, getProductColorDetail, getProductColors · 주입: AdminProductQueryService, AdminProductEntityService, AdminProductColorQueryService, AdminUserQueryService
- `AdminProductColorSearchController` elasticsearch/controller/AdminProductColorSearchController.java · public: esSearchProducts, autocompleteProducts, getRawIndices, getRawIndicesByCategory, getIndexStats · 주입: AdminProductColorSearchService, AdminProductQueryService

### service (25)

- `AdminProductColorIndexingService` elasticsearch/service/AdminProductColorIndexingService.java · public: indexAllColors, indexColorById, deleteColorFromIndex · 주입: AdminProductColorIndexQueryRepository, AdminProductColorEsRepository
- `AdminProductColorSearchService` elasticsearch/service/AdminProductColorSearchService.java · public: searchToDto, search, autocomplete, getAllIndexedColors, getIndexedColorsByCategory, getIndexStats · 주입: ElasticsearchOperations, AdminProductColorEsRepository, StyleRepository, OrderBidRepository, ObjectMapper
- `AdminBrandCommandService` service/brand/AdminBrandCommandService.java · public: createBrand, updateBrand, deleteBrand · 주입: BrandRepository, ProductRepository
- `AdminBrandEntityService` service/brand/AdminBrandEntityService.java · public: findByName · 주입: BrandRepository
- `AdminBrandQueryService` service/brand/AdminBrandQueryService.java · public: findByName, findAllBrands, findById, findBrandById, findBrandsPaging, searchBrandsByName · 주입: BrandRepository
- `AdminCategoryCommandService` service/category/AdminCategoryCommandService.java · public: createCategory, updateCategory, deleteCategory · 주입: CategoryRepository, ProductRepository
- `AdminCategoryEntityService` service/category/AdminCategoryEntityService.java · public: findMainCategoryByName, findSubCategoryByName · 주입: CategoryRepository
- `AdminCategoryQueryService` service/category/AdminCategoryQueryService.java · public: findAllMainCategories, findSubCategoriesByMainCategory, findRootCategory, findRootCategoryById, findSubCategoriesByParentId, findSubCategoriesByParentName · 주입: CategoryRepository
- `AdminCollectionCommandService` service/collection/AdminCollectionCommandService.java · public: createCollection, updateCollection, deleteCollection, createOrGetCollection · 주입: AdminCollectionRepository, ProductRepository
- `AdminCollectionQueryService` service/collection/AdminCollectionQueryService.java · public: findAllCollections, findByName, findCollectionById, findCollectionsPaging, searchCollectionsByName · 주입: AdminCollectionRepository
- `AdminFilterService` service/filter/AdminFilterService.java · public: getAllFilterData · 주입: AdminBrandQueryService, AdminCategoryQueryService, AdminCollectionQueryService
- `AdminInterestCommandService` service/interest/AdminInterestCommandService.java · public: toggleInterest, deleteAllInterestsByProductColor · 주입: AdminInterestRepository, AdminUserQueryService, AdminProductColorQueryService
- `AdminInterestQueryService` service/interest/AdminInterestQueryService.java · public: findUserInterestProducts · 주입: AdminInterestQueryDslRepository
- `AdminIndexedProductQueryService` service/product/AdminIndexedProductQueryService.java · public: searchProductsByNames · 주입: AdminIndexedProductRepository, AdminProductQueryDslRepository
- `AdminProductCommandService` service/product/AdminProductCommandService.java · public: createProduct, updateProduct, deleteProduct · 주입: ProductRepository, AdminBrandEntityService, AdminCategoryEntityService, AdminCollectionCommandService, AdminProductColorCommandService
- `AdminProductEntityService` service/product/AdminProductEntityService.java · public: findById · 주입: ProductRepository
- `AdminProductQueryService` service/product/AdminProductQueryService.java · public: searchProducts, getProductDetail, searchProductsByColorIds, countProductsByFilter · 주입: AdminProductQueryDslRepository, StyleRepository, OrderBidRepository
- `AdminProductColorCommandService` service/productColor/AdminProductColorCommandService.java · public: createProductColor, updateProductColor, deleteProductColor · 주입: ProductColorRepository, AdminProductSizeCommandService, FileUtils, AdminProductEntityService, AdminProductSizeQueryService, AdminInterestCommandService, AdminProductImageCommandService, AdminProductDetailImageCommandService, AdminProductImageQueryService, AdminProductDetailImageQueryService, NginxCachePurgeUtil, JobLauncher, Job
- `AdminProductColorQueryService` service/productColor/AdminProductColorQueryService.java · public: findById, findAllByProductId · 주입: ProductColorRepository
- `AdminProductDetailImageCommandService` service/productDetailImage/AdminProductDetailImageCommandService.java · public: createProductDetailImage, deleteProductDetailImage · 주입: AdminProductDetailImageRepository
- `AdminProductDetailImageQueryService` service/productDetailImage/AdminProductDetailImageQueryService.java · public: findAllByProductColorId, existsByProductColorId · 주입: AdminProductDetailImageRepository
- `AdminProductImageCommandService` service/productImage/AdminProductImageCommandService.java · public: createProductImage, deleteProductImage · 주입: ProductImageRepository
- `AdminProductImageQueryService` service/productImage/AdminProductImageQueryService.java · public: findAllByProductColorId, existsByProductColorId · 주입: ProductImageRepository
- `AdminProductSizeCommandService` service/productSize/AdminProductSizeCommandService.java · public: createProductSizes, deleteProductSize, updateProductSize, deleteAllSizesByProductColor · 주입: ProductSizeRepository, AdminCategoryQueryService
- `AdminProductSizeQueryService` service/productSize/AdminProductSizeQueryService.java · public: findByColorIdAndSize, findSizesByColorId, findById, findAllByProductColorId · 주입: ProductSizeRepository

### repository (10)

- `AdminProductColorEsRepository` (interface) elasticsearch/repository/AdminProductColorEsRepository.java · extends/implements ElasticsearchRepository
- `AdminProductColorIndexQueryRepository` elasticsearch/repository/AdminProductColorIndexQueryRepository.java · public: findAllForIndexingDto, findAllSizesForIndexing, findOneForIndexingDto, findSizesByColorId · 주입: JPAQueryFactory
- `AdminCollectionRepository` (interface) repository/AdminCollectionRepository.java · extends/implements JpaRepository · public: findByName, existsByName, findAllByOrderByNameDesc, findByNameContainingIgnoreCase
- `AdminIndexedProductRepository` repository/AdminIndexedProductRepository.java · public: findBrandIdsByNames, findCategoryIdsByNames, findCollectionIdsByNames, searchProductsByNames · 주입: JPAQueryFactory
- `AdminInterestQueryDslRepository` repository/AdminInterestQueryDslRepository.java · public: findUserInterestProducts · 주입: JPAQueryFactory
- `AdminInterestRepository` (interface) repository/AdminInterestRepository.java · extends/implements JpaRepository · public: existsByUserAndProductColor, findByUserAndProductColor, findAllByUserId
- `AdminProductColorViewLogRepository` (interface) repository/AdminProductColorViewLogRepository.java · extends/implements JpaRepository
- `AdminProductDetailImageRepository` (interface) repository/AdminProductDetailImageRepository.java · extends/implements JpaRepository · public: existsByProductColorId, findByProductColorId, findAllByProductColorId
- `AdminProductPriceHistoryRepository` (interface) repository/AdminProductPriceHistoryRepository.java · extends/implements JpaRepository · public: findByProductSizeId
- `AdminProductQueryDslRepository` repository/AdminProductQueryDslRepository.java · public: searchProducts, countProductsByFilter, findProductDetail, searchProductsByColorIds, tradeCountQuery, styleCountQuery · 주입: JPAQueryFactory

### event (2)

- `AdminViewEventConsumer` service/kafka/AdminViewEventConsumer.java · public: listen · 주입: ProductColorRepository, AdminProductColorViewLogRepository
- `AdminViewEventProducer` service/kafka/AdminViewEventProducer.java · public: sendViewEvent · 주입: KafkaTemplate

### config (2)

- `AdminViewEventKafkaConfig` service/kafka/config/AdminViewEventKafkaConfig.java · public: viewEventProducerFactory, viewEventKafkaTemplate, viewEventConsumerFactory, viewEventKafkaListenerContainerFactory
- `AdminCreateSizesJobConfig` service/productSize/AdminCreateSizesJobConfig.java · public: createSizesJob, createSizesStep, sizeItemReader, sizeItemProcessor, sizeItemWriter, skipListener · 주입: JobRepository, PlatformTransactionManager, ProductColorRepository, ProductSizeRepository, AdminCategoryQueryService

### other (2)

- `AdminProductColorIndexInitializer` elasticsearch/config/AdminProductColorIndexInitializer.java · extends/implements CommandLineRunner · public: run · 주입: AdminProductColorIndexingService, ElasticsearchOperations
- `AdminProductColorIndex` elasticsearch/index/AdminProductColorIndex.java

### 다른 도메인 참조 (import·주입 기준)

- **adminuser**: `AdminUserQueryService` ← AdminBrandController, AdminCategoryController, AdminCollectionController, AdminInterestCommandService, AdminProductColorCommandController, AdminProductCommandController, AdminProductQueryController, AdminProductQueryControllerForAdmin
- **order**: `BidStatus` ← AdminProductQueryDslRepository; `OrderBidRepository` ← AdminProductColorSearchService, AdminProductQueryService
- **product**: `Brand` ← AdminBrandCommandService, AdminBrandEntityService, AdminBrandQueryService, AdminProductCommandService; `BrandRepository` ← AdminBrandCommandService, AdminBrandEntityService, AdminBrandQueryService; `Category` ← AdminCategoryCommandService, AdminCategoryEntityService, AdminCategoryQueryService, AdminCreateSizesJobConfig, AdminProductCommandService, AdminProductSizeCommandService; `CategoryRepository` ← AdminCategoryCommandService, AdminCategoryEntityService, AdminCategoryQueryService; `Collection` ← AdminCollectionCommandService, AdminCollectionQueryService, AdminCollectionRepository, AdminProductCommandService; `ColorType` ← AdminFilterService, AdminProductColorCommandService; `GenderType` ← AdminFilterService, AdminIndexedProductRepository, AdminProductColorSearchController, AdminProductQueryDslRepository, AdminProductQueryService; `Interest` ← AdminInterestCommandService, AdminInterestRepository; `Product` ← AdminProductCommandService, AdminProductEntityService, AdminProductQueryControllerForAdmin; `ProductColor` ← AdminCreateSizesJobConfig, AdminInterestCommandService, AdminInterestRepository, AdminProductColorQueryService, AdminProductDetailImageCommandService, AdminProductImageCommandService, AdminProductQueryControllerForAdmin, AdminProductSizeCommandService, AdminViewEventConsumer; `ProductColorRepository` ← AdminCreateSizesJobConfig, AdminProductColorCommandService, AdminProductColorQueryService, AdminViewEventConsumer; `ProductColorViewLog` ← AdminProductColorViewLogRepository, AdminViewEventConsumer; `ProductDetailImage` ← AdminProductDetailImageCommandService, AdminProductDetailImageQueryService, AdminProductDetailImageRepository; `ProductImage` ← AdminProductImageCommandService, AdminProductImageQueryService; `ProductImageRepository` ← AdminProductImageCommandService, AdminProductImageQueryService; `ProductPriceHistory` ← AdminProductPriceHistoryRepository; `ProductRepository` ← AdminBrandCommandService, AdminCategoryCommandService, AdminCollectionCommandService, AdminProductCommandService, AdminProductEntityService; `ProductSize` ← AdminCreateSizesJobConfig, AdminProductSizeCommandService, AdminProductSizeQueryService; `ProductSizeRepository` ← AdminCreateSizesJobConfig, AdminProductSizeCommandService, AdminProductSizeQueryService; `SizeType` ← AdminCreateSizesJobConfig, AdminFilterService, AdminProductSizeCommandService; `ViewEvent` ← AdminViewEventConsumer, AdminViewEventKafkaConfig, AdminViewEventProducer
- **style**: `StyleRepository` ← AdminProductColorSearchService, AdminProductQueryService
- **user**: `Gender` ← AdminProductQueryController, AdminViewEventProducer; `User` ← AdminInterestCommandService, AdminInterestRepository

### 이 도메인을 참조하는 다른 도메인

- **adminevent**: AdminBrandQueryService
- **adminorder**: AdminProductSizeQueryService
- **adminsale**: AdminProductSizeQueryService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

- 발행 kafka `view-log-topic`: `AdminViewEventProducer#sendViewEvent` → `AdminViewEventConsumer#listen` [adminproduct], `InternalViewEventConsumer#listen` [internalproduct], `LegacyViewEventConsumer#listen` [legacyproduct], `PartnerViewEventConsumer#listen` [partnerproduct], `ViewEventConsumer#listen` [product], `SettlementViewEventConsumer#listen` [settlementproduct]
- 구독 kafka `view-log-topic`: `AdminViewEventConsumer#listen` (groupId=my-group containerFactory=viewEventKafkaListenerContainerFactory) ← `AdminViewEventProducer#sendViewEvent` [adminproduct], `InternalViewEventProducer#sendViewEvent` [internalproduct], `LegacyViewEventProducer#sendViewEvent` [legacyproduct], `PartnerViewEventProducer#sendViewEvent` [partnerproduct], `ViewEventProducer#sendViewEvent` [product], `SettlementViewEventProducer#sendViewEvent` [settlementproduct]

## 6. AOP

(없음)

