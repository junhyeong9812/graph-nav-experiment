# partnerproduct 도메인

기준 경로: `src/main/java/com/fream/back/domain/partnerproduct/` · 파일 52개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/partner/brands` | `PartnerBrandController#createBrand` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerBrandCommandService#createBrand` |
| PUT | `/partner/brands/{brandId}` | `PartnerBrandController#updateBrand` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerBrandCommandService#updateBrand` |
| DELETE | `/partner/brands/{brandName}` | `PartnerBrandController#deleteBrand` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerBrandCommandService#deleteBrand` |
| GET | `/partner/brands` | `PartnerBrandController#getAllBrands` | `PartnerBrandQueryService#findAllBrands` |
| GET | `/partner/brands/page` | `PartnerBrandController#getBrandsPaging` | `PartnerBrandQueryService#findBrandsPaging` |
| GET | `/partner/brands/search` | `PartnerBrandController#searchBrands` | `PartnerBrandQueryService#searchBrandsByName` |
| GET | `/partner/brands/{brandId:[0-9]+}` | `PartnerBrandController#getBrandById` | `PartnerBrandQueryService#findBrandById` |
| GET | `/partner/brands/name/{brandName}` | `PartnerBrandController#getBrandByName` | `PartnerBrandQueryService#findByName` |
| POST | `/partner/categories` | `PartnerCategoryController#createCategory` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerCategoryCommandService#createCategory` |
| PUT | `/partner/categories/{categoryId}` | `PartnerCategoryController#updateCategory` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerCategoryCommandService#updateCategory` |
| DELETE | `/partner/categories/{categoryId}` | `PartnerCategoryController#deleteCategory` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerCategoryCommandService#deleteCategory` |
| GET | `/partner/categories/main` | `PartnerCategoryController#getAllMainCategories` | `PartnerCategoryQueryService#findAllMainCategories` |
| GET | `/partner/categories/sub/{mainCategoryName}` | `PartnerCategoryController#getSubCategoriesByMain` | `PartnerCategoryQueryService#findSubCategoriesByMainCategory` |
| POST | `/partner/collections` | `PartnerCollectionController#createCollection` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerCollectionCommandService#createCollection` |
| PUT | `/partner/collections/{collectionId}` | `PartnerCollectionController#updateCollection` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerCollectionCommandService#updateCollection` |
| DELETE | `/partner/collections/{collectionName}` | `PartnerCollectionController#deleteCollection` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerCollectionCommandService#deleteCollection` |
| GET | `/partner/collections` | `PartnerCollectionController#getAllCollections` | `PartnerCollectionQueryService#findAllCollections` |
| GET | `/partner/collections/page` | `PartnerCollectionController#getCollectionsPaging` | `PartnerCollectionQueryService#findCollectionsPaging` |
| GET | `/partner/collections/search` | `PartnerCollectionController#searchCollections` | `PartnerCollectionQueryService#searchCollectionsByName` |
| GET | `/partner/collections/{collectionId:[0-9]+}` | `PartnerCollectionController#getCollectionById` | `PartnerCollectionQueryService#findCollectionById` |
| GET | `/partner/collections/name/{collectionName}` | `PartnerCollectionController#getCollectionByName` | `PartnerCollectionQueryService#findByName` |
| POST | `/partner/interests/{productColorId}/toggle` | `PartnerInterestCommandController#toggleInterest` | `this#extractEmailFromSecurityContext`, `PartnerInterestCommandService#toggleInterest` |
| POST | `/partner/product-colors/{productId}` | `PartnerProductColorCommandController#createProductColor` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerProductColorCommandService#createProductColor`, `NginxCachePurgeUtil#purgeProductCache`, `NginxCachePurgeUtil#purgeEsCache` |
| PUT | `/partner/product-colors/{productColorId}` | `PartnerProductColorCommandController#updateProductColor` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerProductColorCommandService#updateProductColor`, `NginxCachePurgeUtil#purgeProductCache`, `NginxCachePurgeUtil#purgeEsCache` |
| DELETE | `/partner/product-colors/{productColorId}` | `PartnerProductColorCommandController#deleteProductColor` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerProductColorCommandService#deleteProductColor`, `NginxCachePurgeUtil#purgeProductCache`, `NginxCachePurgeUtil#purgeEsCache` |
| POST | `/partner/products/command` | `PartnerProductCommandController#createProduct` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerProductCommandService#createProduct` |
| PUT | `/partner/products/command/{productId}` | `PartnerProductCommandController#updateProduct` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerProductCommandService#updateProduct` |
| DELETE | `/partner/products/command/{productId}` | `PartnerProductCommandController#deleteProduct` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerProductCommandService#deleteProduct` |
| GET | `/partner/products/indexed/search` | `PartnerIndexedProductQueryController#searchProductsByNames` | `PartnerIndexedProductQueryService#searchProductsByNames`, `this#toPageDto` |
| GET | `/partner/interests/{userId}` | `PartnerInterestQueryController#getUserInterestProducts` | `PartnerInterestQueryService#findUserInterestProducts` |
| GET | `/partner/products/query` | `PartnerProductQueryController#searchProducts` | `PartnerProductQueryService#searchProducts`, `this#toPageDto` |
| GET | `/partner/products/query/{productId}/detail` | `PartnerProductQueryController#getProductDetail` | `PartnerProductQueryService#getProductDetail`, `PartnerViewEventProducer#sendViewEvent` |
| GET | `/partner/products/query/{productId}/images` | `PartnerProductQueryController#getProductImage` |  |
| GET | `/partner/products/query/filters` | `PartnerProductQueryController#getFilterData` | `PartnerFilterService#getAllFilterData` |
| POST | `/partner/products/query/filters/count` | `PartnerProductQueryController#countProductsByFilter` | `PartnerProductQueryService#countProductsByFilter` |
| GET | `/partner/admin/products/query/{productId}/detail` | `PartnerProductQueryControllerForAdmin#getProductDetailForAdmin` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerProductEntityService#findById` |
| GET | `/partner/admin/products/query/colors/{colorId}` | `PartnerProductQueryControllerForAdmin#getProductColorDetail` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerProductColorQueryService#findById` |
| GET | `/partner/admin/products/query/{productId}/colors` | `PartnerProductQueryControllerForAdmin#getProductColors` | `this#extractEmailFromSecurityContext`, `PartnerUserQueryService#checkAdminRole`, `PartnerProductEntityService#findById` |
| GET | `/partner/es/products` | `PartnerProductColorSearchController#esSearchProducts` | `this#convertGenders`, `PartnerProductColorSearchService#searchToDto`, `this#toPageDto` |
| GET | `/partner/es/products/autocomplete` | `PartnerProductColorSearchController#autocompleteProducts` | `PartnerProductColorSearchService#autocomplete` |
| GET | `/partner/es/products/raw-indices` | `PartnerProductColorSearchController#getRawIndices` | `PartnerProductColorSearchService#getAllIndexedColors`, `this#toPageDto` |
| GET | `/partner/es/products/raw-indices/category/{categoryId}` | `PartnerProductColorSearchController#getRawIndicesByCategory` | `PartnerProductColorSearchService#getIndexedColorsByCategory`, `this#toPageDto` |
| GET | `/partner/es/products/index-stats` | `PartnerProductColorSearchController#getIndexStats` | `PartnerProductColorSearchService#getIndexStats` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `PartnerBrandCommandService`
  - `createBrand` → BrandRepository#existsByName, BrandRepository#save~
  - `updateBrand` → BrandRepository#findById~, BrandRepository#existsByName
  - `deleteBrand` → BrandRepository#findByName, ProductRepository#existsByBrand, BrandRepository#delete~
- `PartnerBrandEntityService`
  - `findByName` → BrandRepository#findByName
- `PartnerBrandQueryService`
  - `findByName` → BrandRepository#findByName
  - `findAllBrands` → BrandRepository#findAllByOrderByNameDesc
  - `findById` → BrandRepository#findById~
  - `findBrandById` → BrandRepository#findById~
  - `findBrandsPaging` → BrandRepository#findAll~
  - `searchBrandsByName` → BrandRepository#findByNameContainingIgnoreCase
- `PartnerCategoryCommandService`
  - `createCategory` → CategoryRepository#existsByNameAndParentCategoryIsNull, CategoryRepository#save~, CategoryRepository#findByNameAndParentCategoryIsNull, CategoryRepository#existsByNameAndParentCategory
  - `updateCategory` → CategoryRepository#findById~, CategoryRepository#existsByNameAndParentCategoryIsNull, CategoryRepository#existsByNameAndParentCategory
  - `deleteCategory` → CategoryRepository#findById~, CategoryRepository#existsByParentCategory, ProductRepository#existsByCategory, CategoryRepository#delete~
- `PartnerCategoryEntityService`
  - `findMainCategoryByName` → CategoryRepository#findByNameAndParentCategoryIsNull
  - `findSubCategoryByName` → this#findMainCategoryByName, CategoryRepository#findByNameAndParentCategory
- `PartnerCategoryQueryService`
  - `findAllMainCategories` → CategoryRepository#findByParentCategoryIsNullOrderByNameDesc
  - `findSubCategoriesByMainCategory` → CategoryRepository#findByNameAndParentCategoryIsNull, CategoryRepository#findByParentCategoryOrderByNameDesc
  - `findRootCategoryById` → CategoryRepository#findWithParentById, this#findRootCategory
  - `findSubCategoriesByParentId` → CategoryRepository#findByParentCategoryId
  - `findSubCategoriesByParentName` → CategoryRepository#findByParentCategoryName
- `PartnerCollectionCommandService`
  - `createCollection` → PartnerCollectionRepository#findByName, PartnerCollectionRepository#save~
  - `updateCollection` → PartnerCollectionRepository#findById~, PartnerCollectionRepository#existsByName
  - `deleteCollection` → PartnerCollectionRepository#findByName, ProductRepository#existsByCollection, PartnerCollectionRepository#delete~
  - `createOrGetCollection` → PartnerCollectionRepository#findByName, PartnerCollectionRepository#save~
- `PartnerCollectionQueryService`
  - `findAllCollections` → PartnerCollectionRepository#findAllByOrderByNameDesc
  - `findByName` → PartnerCollectionRepository#findByName
  - `findCollectionById` → PartnerCollectionRepository#findById~
  - `findCollectionsPaging` → PartnerCollectionRepository#findAll~
  - `searchCollectionsByName` → PartnerCollectionRepository#findByNameContainingIgnoreCase
- `PartnerCreateSizesJobConfig`
  - `createSizesJob` → this#createSizesStep
  - `createSizesStep` → this#sizeItemReader, this#sizeItemProcessor, this#sizeItemWriter, this#skipListener
  - `sizeItemReader` → PartnerCategoryQueryService#findRootCategoryById, this#determineSizeType, ProductSizeRepository#findAllByProductColorId
  - `sizeItemProcessor` → this#isValidSize, ProductColorRepository#findById~
  - `sizeItemWriter` → ProductSizeRepository#save~
- `PartnerFilterService`
  - `getAllFilterData` → this#getGenderLabel, this#getDiscountData, this#getPriceRangeData, PartnerCategoryQueryService#findAllMainCategories, PartnerCategoryQueryService#findSubCategoriesByMainCategory, this#processCategoryData, PartnerBrandQueryService#findAllBrands, PartnerCollectionQueryService#findAllCollections
  - `getPriceRangeData` → this#createPriceRange
  - `processCategoryData` → PartnerCategoryQueryService#findSubCategoriesByParentId
- `PartnerIndexedProductQueryService`
  - `searchProductsByNames` → PartnerIndexedProductRepository#searchProductsByNames, PartnerProductQueryDslRepository#styleCountQuery, PartnerProductQueryDslRepository#tradeCountQuery
- `PartnerIndexedProductRepository`
  - `searchProductsByNames` → this#findBrandIdsByNames, this#findCategoryIdsByNames, this#findCollectionIdsByNames, this#searchProductsByIdsOptimized
  - `searchProductsByIdsOptimized` → this#buildKeywordPredicateOptimized, this#buildCategoryIdsPredicate, this#buildGenderPredicate, this#buildBrandIdsPredicate, this#buildCollectionIdsPredicate, this#buildColorPredicate, this#buildSizePredicate, this#buildPricePredicate, this#applySortingOptimized, this#countProductsByConditions
  - `countProductsByConditions` → this#buildKeywordPredicateOptimized, this#buildCategoryIdsPredicate, this#buildGenderPredicate, this#buildBrandIdsPredicate, this#buildCollectionIdsPredicate, this#buildColorPredicate, this#buildSizePredicate, this#buildPricePredicate
- `PartnerInterestCommandService`
  - `toggleInterest` → PartnerUserQueryService#findByEmail, PartnerProductColorQueryService#findById, PartnerInterestRepository#findByUserAndProductColor, PartnerInterestRepository#delete~, PartnerInterestRepository#save~
  - `deleteAllInterestsByProductColor` → PartnerInterestRepository#delete~
- `PartnerInterestQueryDslRepository`
  - `findUserInterestProducts` → this#getOrderByClause
- `PartnerInterestQueryService`
  - `findUserInterestProducts` → PartnerInterestQueryDslRepository#findUserInterestProducts
- `PartnerProductColorCommandService`
  - `createProductColor` → this#validateColorType, PartnerProductEntityService#findById, FileUtils#saveFile, ProductColorRepository#save~, PartnerProductSizeCommandService#createProductSizes
  - `updateProductColor` → ProductColorRepository#findById~, this#validateColorType, FileUtils#deleteFile, FileUtils#saveFile, PartnerProductSizeCommandService#deleteProductSize, PartnerProductSizeCommandService#createProductSizes, ProductColorRepository#save~
  - `deleteProductColor` → ProductColorRepository#findById~, PartnerInterestCommandService#deleteAllInterestsByProductColor, PartnerProductSizeCommandService#deleteAllSizesByProductColor, FileUtils#deleteFile, PartnerProductImageCommandService#deleteProductImage, PartnerProductImageQueryService#existsByProductColorId, PartnerProductImageQueryService#findAllByProductColorId, FileUtils#existsFile, PartnerProductDetailImageQueryService#existsByProductColorId, PartnerProductDetailImageQueryService#findAllByProductColorId, PartnerProductDetailImageCommandService#deleteProductDetailImage, ProductColorRepository#delete~
- `PartnerProductColorIndexInitializer`
  - `run` → PartnerProductColorIndexingService#indexAllColors
- `PartnerProductColorIndexingService`
  - `indexAllColors` → PartnerProductColorIndexQueryRepository#findAllForIndexingDto, PartnerProductColorIndexQueryRepository#findAllSizesForIndexing, PartnerProductColorEsRepository#saveAll~
  - `indexColorById` → PartnerProductColorIndexQueryRepository#findOneForIndexingDto, PartnerProductColorIndexQueryRepository#findSizesByColorId, PartnerProductColorEsRepository#save~
  - `deleteColorFromIndex` → PartnerProductColorEsRepository#deleteById~
- `PartnerProductColorQueryService`
  - `findById` → ProductColorRepository#findById~
  - `findAllByProductId` → ProductColorRepository#findByProductId
- `PartnerProductColorSearchService`
  - `searchToDto` → this#search, StyleRepository#styleCountByColorIds, OrderBidRepository#tradeCountByColorIds
  - `search` → this#rangeQuery
- `PartnerProductCommandService`
  - `createProduct` → PartnerBrandEntityService#findByName, PartnerCategoryEntityService#findSubCategoryByName, PartnerCollectionCommandService#createOrGetCollection, ProductRepository#save~
  - `updateProduct` → ProductRepository#findById~, PartnerBrandEntityService#findByName, PartnerCategoryEntityService#findSubCategoryByName, PartnerCollectionCommandService#createOrGetCollection
  - `deleteProduct` → ProductRepository#findById~, PartnerProductColorCommandService#deleteProductColor, ProductRepository#delete~
- `PartnerProductDetailImageCommandService`
  - `createProductDetailImage` → PartnerProductDetailImageRepository#save~
  - `deleteProductDetailImage` → PartnerProductDetailImageRepository#findById~, PartnerProductDetailImageRepository#delete~
- `PartnerProductDetailImageQueryService`
  - `findAllByProductColorId` → PartnerProductDetailImageRepository#findAllByProductColorId
  - `existsByProductColorId` → PartnerProductDetailImageRepository#existsByProductColorId
- `PartnerProductEntityService`
  - `findById` → ProductRepository#findById~
- `PartnerProductImageCommandService`
  - `createProductImage` → ProductImageRepository#save~
  - `deleteProductImage` → ProductImageRepository#findById~, ProductImageRepository#delete~
- `PartnerProductImageQueryService`
  - `findAllByProductColorId` → ProductImageRepository#findAllByProductColorId
  - `existsByProductColorId` → ProductImageRepository#existsByProductColorId
- `PartnerProductQueryDslRepository`
  - `searchProducts` → this#logPredicate, this#buildKeywordPredicate, this#buildCategoryPredicate, this#buildGenderPredicate, this#buildBrandPredicate, this#buildCollectionPredicate, this#buildColorPredicate, this#buildSizePredicate, this#buildPricePredicate
  - `countProductsByFilter` → this#buildKeywordPredicate, this#buildCategoryPredicate, this#buildGenderPredicate, this#buildBrandPredicate, this#buildCollectionPredicate, this#buildColorPredicate, this#buildSizePredicate, this#buildPricePredicate
- `PartnerProductQueryService`
  - `searchProducts` → PartnerProductQueryDslRepository#searchProducts, StyleRepository#styleCountByColorIds, OrderBidRepository#tradeCountByColorIds
  - `getProductDetail` → PartnerProductQueryDslRepository#findProductDetail
  - `searchProductsByColorIds` → PartnerProductQueryDslRepository#searchProductsByColorIds
  - `countProductsByFilter` → PartnerProductQueryDslRepository#countProductsByFilter
- `PartnerProductSizeCommandService`
  - `createProductSizes` → PartnerCategoryQueryService#findRootCategoryById, this#determineSizeType, ProductSizeRepository#findAllByProductColorId, this#isValidSize, ProductSizeRepository#save~
  - `deleteProductSize` → ProductSizeRepository#findById~, ProductSizeRepository#delete~
  - `updateProductSize` → ProductSizeRepository#findById~
  - `deleteAllSizesByProductColor` → ProductSizeRepository#findAllByProductColorId, this#deleteProductSize, ProductSizeRepository#flush~
- `PartnerProductSizeQueryService`
  - `findByColorIdAndSize` → ProductSizeRepository#findByProductColorIdAndSize
  - `findSizesByColorId` → ProductSizeRepository#findAllByProductColorId
  - `findById` → ProductSizeRepository#findById~
  - `findAllByProductColorId` → ProductSizeRepository#findAllByProductColorId
- `PartnerViewEventConsumer`
  - `listen` → ProductColorRepository#findById~, PartnerProductColorViewLogRepository#save~
- `PartnerViewEventKafkaConfig`
  - `viewEventKafkaTemplate` → this#viewEventProducerFactory
  - `viewEventKafkaListenerContainerFactory` → this#viewEventConsumerFactory
- `PartnerViewEventProducer`
  - `sendViewEvent` → kafka:view-log-topic

## 3. 클래스 목록 (역할별)

### controller (11)

- `PartnerBrandController` controller/PartnerBrandController.java · public: createBrand, updateBrand, deleteBrand, getAllBrands, getBrandsPaging, searchBrands, getBrandById, getBrandByName · 주입: PartnerBrandCommandService, PartnerBrandQueryService, PartnerUserQueryService
- `PartnerCategoryController` controller/PartnerCategoryController.java · public: createCategory, updateCategory, deleteCategory, getAllMainCategories, getSubCategoriesByMain · 주입: PartnerCategoryCommandService, PartnerCategoryQueryService, PartnerUserQueryService
- `PartnerCollectionController` controller/PartnerCollectionController.java · public: createCollection, updateCollection, deleteCollection, getAllCollections, getCollectionsPaging, searchCollections, getCollectionById, getCollectionByName · 주입: PartnerCollectionCommandService, PartnerCollectionQueryService, PartnerUserQueryService
- `PartnerInterestCommandController` controller/command/PartnerInterestCommandController.java · public: toggleInterest · 주입: PartnerInterestCommandService
- `PartnerProductColorCommandController` controller/command/PartnerProductColorCommandController.java · public: createProductColor, updateProductColor, deleteProductColor · 주입: PartnerProductColorCommandService, PartnerUserQueryService, NginxCachePurgeUtil
- `PartnerProductCommandController` controller/command/PartnerProductCommandController.java · public: createProduct, updateProduct, deleteProduct · 주입: PartnerProductCommandService, PartnerUserQueryService
- `PartnerIndexedProductQueryController` controller/query/PartnerIndexedProductQueryController.java · public: searchProductsByNames · 주입: PartnerIndexedProductQueryService
- `PartnerInterestQueryController` controller/query/PartnerInterestQueryController.java · public: getUserInterestProducts · 주입: PartnerInterestQueryService
- `PartnerProductQueryController` controller/query/PartnerProductQueryController.java · public: searchProducts, getProductDetail, getProductImage, getFilterData, countProductsByFilter · 주입: PartnerProductQueryService, PartnerUserQueryService, PartnerViewEventProducer, PartnerFilterService
- `PartnerProductQueryControllerForAdmin` controller/query/PartnerProductQueryControllerForAdmin.java · public: getProductDetailForAdmin, getProductColorDetail, getProductColors · 주입: PartnerProductQueryService, PartnerProductEntityService, PartnerProductColorQueryService, PartnerUserQueryService
- `PartnerProductColorSearchController` elasticsearch/controller/PartnerProductColorSearchController.java · public: esSearchProducts, autocompleteProducts, getRawIndices, getRawIndicesByCategory, getIndexStats · 주입: PartnerProductColorSearchService, PartnerProductQueryService

### service (25)

- `PartnerProductColorIndexingService` elasticsearch/service/PartnerProductColorIndexingService.java · public: indexAllColors, indexColorById, deleteColorFromIndex · 주입: PartnerProductColorIndexQueryRepository, PartnerProductColorEsRepository
- `PartnerProductColorSearchService` elasticsearch/service/PartnerProductColorSearchService.java · public: searchToDto, search, autocomplete, getAllIndexedColors, getIndexedColorsByCategory, getIndexStats · 주입: ElasticsearchOperations, PartnerProductColorEsRepository, StyleRepository, OrderBidRepository, ObjectMapper
- `PartnerBrandCommandService` service/brand/PartnerBrandCommandService.java · public: createBrand, updateBrand, deleteBrand · 주입: BrandRepository, ProductRepository
- `PartnerBrandEntityService` service/brand/PartnerBrandEntityService.java · public: findByName · 주입: BrandRepository
- `PartnerBrandQueryService` service/brand/PartnerBrandQueryService.java · public: findByName, findAllBrands, findById, findBrandById, findBrandsPaging, searchBrandsByName · 주입: BrandRepository
- `PartnerCategoryCommandService` service/category/PartnerCategoryCommandService.java · public: createCategory, updateCategory, deleteCategory · 주입: CategoryRepository, ProductRepository
- `PartnerCategoryEntityService` service/category/PartnerCategoryEntityService.java · public: findMainCategoryByName, findSubCategoryByName · 주입: CategoryRepository
- `PartnerCategoryQueryService` service/category/PartnerCategoryQueryService.java · public: findAllMainCategories, findSubCategoriesByMainCategory, findRootCategory, findRootCategoryById, findSubCategoriesByParentId, findSubCategoriesByParentName · 주입: CategoryRepository
- `PartnerCollectionCommandService` service/collection/PartnerCollectionCommandService.java · public: createCollection, updateCollection, deleteCollection, createOrGetCollection · 주입: PartnerCollectionRepository, ProductRepository
- `PartnerCollectionQueryService` service/collection/PartnerCollectionQueryService.java · public: findAllCollections, findByName, findCollectionById, findCollectionsPaging, searchCollectionsByName · 주입: PartnerCollectionRepository
- `PartnerFilterService` service/filter/PartnerFilterService.java · public: getAllFilterData · 주입: PartnerBrandQueryService, PartnerCategoryQueryService, PartnerCollectionQueryService
- `PartnerInterestCommandService` service/interest/PartnerInterestCommandService.java · public: toggleInterest, deleteAllInterestsByProductColor · 주입: PartnerInterestRepository, PartnerUserQueryService, PartnerProductColorQueryService
- `PartnerInterestQueryService` service/interest/PartnerInterestQueryService.java · public: findUserInterestProducts · 주입: PartnerInterestQueryDslRepository
- `PartnerIndexedProductQueryService` service/product/PartnerIndexedProductQueryService.java · public: searchProductsByNames · 주입: PartnerIndexedProductRepository, PartnerProductQueryDslRepository
- `PartnerProductCommandService` service/product/PartnerProductCommandService.java · public: createProduct, updateProduct, deleteProduct · 주입: ProductRepository, PartnerBrandEntityService, PartnerCategoryEntityService, PartnerCollectionCommandService, PartnerProductColorCommandService
- `PartnerProductEntityService` service/product/PartnerProductEntityService.java · public: findById · 주입: ProductRepository
- `PartnerProductQueryService` service/product/PartnerProductQueryService.java · public: searchProducts, getProductDetail, searchProductsByColorIds, countProductsByFilter · 주입: PartnerProductQueryDslRepository, StyleRepository, OrderBidRepository
- `PartnerProductColorCommandService` service/productColor/PartnerProductColorCommandService.java · public: createProductColor, updateProductColor, deleteProductColor · 주입: ProductColorRepository, PartnerProductSizeCommandService, FileUtils, PartnerProductEntityService, PartnerProductSizeQueryService, PartnerInterestCommandService, PartnerProductImageCommandService, PartnerProductDetailImageCommandService, PartnerProductImageQueryService, PartnerProductDetailImageQueryService, NginxCachePurgeUtil, JobLauncher, Job
- `PartnerProductColorQueryService` service/productColor/PartnerProductColorQueryService.java · public: findById, findAllByProductId · 주입: ProductColorRepository
- `PartnerProductDetailImageCommandService` service/productDetailImage/PartnerProductDetailImageCommandService.java · public: createProductDetailImage, deleteProductDetailImage · 주입: PartnerProductDetailImageRepository
- `PartnerProductDetailImageQueryService` service/productDetailImage/PartnerProductDetailImageQueryService.java · public: findAllByProductColorId, existsByProductColorId · 주입: PartnerProductDetailImageRepository
- `PartnerProductImageCommandService` service/productImage/PartnerProductImageCommandService.java · public: createProductImage, deleteProductImage · 주입: ProductImageRepository
- `PartnerProductImageQueryService` service/productImage/PartnerProductImageQueryService.java · public: findAllByProductColorId, existsByProductColorId · 주입: ProductImageRepository
- `PartnerProductSizeCommandService` service/productSize/PartnerProductSizeCommandService.java · public: createProductSizes, deleteProductSize, updateProductSize, deleteAllSizesByProductColor · 주입: ProductSizeRepository, PartnerCategoryQueryService
- `PartnerProductSizeQueryService` service/productSize/PartnerProductSizeQueryService.java · public: findByColorIdAndSize, findSizesByColorId, findById, findAllByProductColorId · 주입: ProductSizeRepository

### repository (10)

- `PartnerProductColorEsRepository` (interface) elasticsearch/repository/PartnerProductColorEsRepository.java · extends/implements ElasticsearchRepository
- `PartnerProductColorIndexQueryRepository` elasticsearch/repository/PartnerProductColorIndexQueryRepository.java · public: findAllForIndexingDto, findAllSizesForIndexing, findOneForIndexingDto, findSizesByColorId · 주입: JPAQueryFactory
- `PartnerCollectionRepository` (interface) repository/PartnerCollectionRepository.java · extends/implements JpaRepository · public: findByName, existsByName, findAllByOrderByNameDesc, findByNameContainingIgnoreCase
- `PartnerIndexedProductRepository` repository/PartnerIndexedProductRepository.java · public: findBrandIdsByNames, findCategoryIdsByNames, findCollectionIdsByNames, searchProductsByNames · 주입: JPAQueryFactory
- `PartnerInterestQueryDslRepository` repository/PartnerInterestQueryDslRepository.java · public: findUserInterestProducts · 주입: JPAQueryFactory
- `PartnerInterestRepository` (interface) repository/PartnerInterestRepository.java · extends/implements JpaRepository · public: existsByUserAndProductColor, findByUserAndProductColor, findAllByUserId
- `PartnerProductColorViewLogRepository` (interface) repository/PartnerProductColorViewLogRepository.java · extends/implements JpaRepository
- `PartnerProductDetailImageRepository` (interface) repository/PartnerProductDetailImageRepository.java · extends/implements JpaRepository · public: existsByProductColorId, findByProductColorId, findAllByProductColorId
- `PartnerProductPriceHistoryRepository` (interface) repository/PartnerProductPriceHistoryRepository.java · extends/implements JpaRepository · public: findByProductSizeId
- `PartnerProductQueryDslRepository` repository/PartnerProductQueryDslRepository.java · public: searchProducts, countProductsByFilter, findProductDetail, searchProductsByColorIds, tradeCountQuery, styleCountQuery · 주입: JPAQueryFactory

### event (2)

- `PartnerViewEventConsumer` service/kafka/PartnerViewEventConsumer.java · public: listen · 주입: ProductColorRepository, PartnerProductColorViewLogRepository
- `PartnerViewEventProducer` service/kafka/PartnerViewEventProducer.java · public: sendViewEvent · 주입: KafkaTemplate

### config (2)

- `PartnerViewEventKafkaConfig` service/kafka/config/PartnerViewEventKafkaConfig.java · public: viewEventProducerFactory, viewEventKafkaTemplate, viewEventConsumerFactory, viewEventKafkaListenerContainerFactory
- `PartnerCreateSizesJobConfig` service/productSize/PartnerCreateSizesJobConfig.java · public: createSizesJob, createSizesStep, sizeItemReader, sizeItemProcessor, sizeItemWriter, skipListener · 주입: JobRepository, PlatformTransactionManager, ProductColorRepository, ProductSizeRepository, PartnerCategoryQueryService

### other (2)

- `PartnerProductColorIndexInitializer` elasticsearch/config/PartnerProductColorIndexInitializer.java · extends/implements CommandLineRunner · public: run · 주입: PartnerProductColorIndexingService, ElasticsearchOperations
- `PartnerProductColorIndex` elasticsearch/index/PartnerProductColorIndex.java

### 다른 도메인 참조 (import·주입 기준)

- **order**: `BidStatus` ← PartnerProductQueryDslRepository; `OrderBidRepository` ← PartnerProductColorSearchService, PartnerProductQueryService
- **partneruser**: `PartnerUserQueryService` ← PartnerBrandController, PartnerCategoryController, PartnerCollectionController, PartnerInterestCommandService, PartnerProductColorCommandController, PartnerProductCommandController, PartnerProductQueryController, PartnerProductQueryControllerForAdmin
- **product**: `Brand` ← PartnerBrandCommandService, PartnerBrandEntityService, PartnerBrandQueryService, PartnerProductCommandService; `BrandRepository` ← PartnerBrandCommandService, PartnerBrandEntityService, PartnerBrandQueryService; `Category` ← PartnerCategoryCommandService, PartnerCategoryEntityService, PartnerCategoryQueryService, PartnerCreateSizesJobConfig, PartnerProductCommandService, PartnerProductSizeCommandService; `CategoryRepository` ← PartnerCategoryCommandService, PartnerCategoryEntityService, PartnerCategoryQueryService; `Collection` ← PartnerCollectionCommandService, PartnerCollectionQueryService, PartnerCollectionRepository, PartnerProductCommandService; `ColorType` ← PartnerFilterService, PartnerProductColorCommandService; `GenderType` ← PartnerFilterService, PartnerIndexedProductRepository, PartnerProductColorSearchController, PartnerProductQueryDslRepository, PartnerProductQueryService; `Interest` ← PartnerInterestCommandService, PartnerInterestRepository; `Product` ← PartnerProductCommandService, PartnerProductEntityService, PartnerProductQueryControllerForAdmin; `ProductColor` ← PartnerCreateSizesJobConfig, PartnerInterestCommandService, PartnerInterestRepository, PartnerProductColorQueryService, PartnerProductDetailImageCommandService, PartnerProductImageCommandService, PartnerProductQueryControllerForAdmin, PartnerProductSizeCommandService, PartnerViewEventConsumer; `ProductColorRepository` ← PartnerCreateSizesJobConfig, PartnerProductColorCommandService, PartnerProductColorQueryService, PartnerViewEventConsumer; `ProductColorViewLog` ← PartnerProductColorViewLogRepository, PartnerViewEventConsumer; `ProductDetailImage` ← PartnerProductDetailImageCommandService, PartnerProductDetailImageQueryService, PartnerProductDetailImageRepository; `ProductImage` ← PartnerProductImageCommandService, PartnerProductImageQueryService; `ProductImageRepository` ← PartnerProductImageCommandService, PartnerProductImageQueryService; `ProductPriceHistory` ← PartnerProductPriceHistoryRepository; `ProductRepository` ← PartnerBrandCommandService, PartnerCategoryCommandService, PartnerCollectionCommandService, PartnerProductCommandService, PartnerProductEntityService; `ProductSize` ← PartnerCreateSizesJobConfig, PartnerProductSizeCommandService, PartnerProductSizeQueryService; `ProductSizeRepository` ← PartnerCreateSizesJobConfig, PartnerProductSizeCommandService, PartnerProductSizeQueryService; `SizeType` ← PartnerCreateSizesJobConfig, PartnerFilterService, PartnerProductSizeCommandService; `ViewEvent` ← PartnerViewEventConsumer, PartnerViewEventKafkaConfig, PartnerViewEventProducer
- **style**: `StyleRepository` ← PartnerProductColorSearchService, PartnerProductQueryService
- **user**: `Gender` ← PartnerProductQueryController, PartnerViewEventProducer; `User` ← PartnerInterestCommandService, PartnerInterestRepository

### 이 도메인을 참조하는 다른 도메인

- **partnerevent**: PartnerBrandQueryService
- **partnerorder**: PartnerProductSizeQueryService
- **partnersale**: PartnerProductSizeQueryService

## 4. JPA 엔티티 연관

(없음)

## 5. 이벤트·메시징

- 발행 kafka `view-log-topic`: `PartnerViewEventProducer#sendViewEvent` → `AdminViewEventConsumer#listen` [adminproduct], `InternalViewEventConsumer#listen` [internalproduct], `LegacyViewEventConsumer#listen` [legacyproduct], `PartnerViewEventConsumer#listen` [partnerproduct], `ViewEventConsumer#listen` [product], `SettlementViewEventConsumer#listen` [settlementproduct]
- 구독 kafka `view-log-topic`: `PartnerViewEventConsumer#listen` (groupId=my-group containerFactory=viewEventKafkaListenerContainerFactory) ← `AdminViewEventProducer#sendViewEvent` [adminproduct], `InternalViewEventProducer#sendViewEvent` [internalproduct], `LegacyViewEventProducer#sendViewEvent` [legacyproduct], `PartnerViewEventProducer#sendViewEvent` [partnerproduct], `ViewEventProducer#sendViewEvent` [product], `SettlementViewEventProducer#sendViewEvent` [settlementproduct]

## 6. AOP

(없음)

