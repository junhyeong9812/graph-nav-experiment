# product 도메인

기준 경로: `src/main/java/com/fream/back/domain/product/` · 파일 106개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/brands` | `BrandController#createBrand` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `BrandCommandService#createBrand` |
| PUT | `/brands/{brandId}` | `BrandController#updateBrand` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `BrandCommandService#updateBrand` |
| DELETE | `/brands/{brandName}` | `BrandController#deleteBrand` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `BrandCommandService#deleteBrand` |
| GET | `/brands` | `BrandController#getAllBrands` | `BrandQueryService#findAllBrands` |
| GET | `/brands/page` | `BrandController#getBrandsPaging` | `BrandQueryService#findBrandsPaging` |
| GET | `/brands/search` | `BrandController#searchBrands` | `BrandQueryService#searchBrandsByName` |
| GET | `/brands/{brandId:[0-9]+}` | `BrandController#getBrandById` | `BrandQueryService#findBrandById` |
| GET | `/brands/name/{brandName}` | `BrandController#getBrandByName` | `BrandQueryService#findByName` |
| POST | `/categories` | `CategoryController#createCategory` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `CategoryCommandService#createCategory` |
| PUT | `/categories/{categoryId}` | `CategoryController#updateCategory` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `CategoryCommandService#updateCategory` |
| DELETE | `/categories/{categoryId}` | `CategoryController#deleteCategory` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `CategoryCommandService#deleteCategory` |
| GET | `/categories/main` | `CategoryController#getAllMainCategories` | `CategoryQueryService#findAllMainCategories` |
| GET | `/categories/sub/{mainCategoryName}` | `CategoryController#getSubCategoriesByMain` | `CategoryQueryService#findSubCategoriesByMainCategory` |
| POST | `/collections` | `CollectionController#createCollection` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `CollectionCommandService#createCollection` |
| PUT | `/collections/{collectionId}` | `CollectionController#updateCollection` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `CollectionCommandService#updateCollection` |
| DELETE | `/collections/{collectionName}` | `CollectionController#deleteCollection` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `CollectionCommandService#deleteCollection` |
| GET | `/collections` | `CollectionController#getAllCollections` | `CollectionQueryService#findAllCollections` |
| GET | `/collections/page` | `CollectionController#getCollectionsPaging` | `CollectionQueryService#findCollectionsPaging` |
| GET | `/collections/search` | `CollectionController#searchCollections` | `CollectionQueryService#searchCollectionsByName` |
| GET | `/collections/{collectionId:[0-9]+}` | `CollectionController#getCollectionById` | `CollectionQueryService#findCollectionById` |
| GET | `/collections/name/{collectionName}` | `CollectionController#getCollectionByName` | `CollectionQueryService#findByName` |
| POST | `/interests/{productColorId}/toggle` | `InterestCommandController#toggleInterest` | `this#extractEmailFromSecurityContext`, `InterestCommandService#toggleInterest` |
| POST | `/product-colors/{productId}` | `ProductColorCommandController#createProductColor` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `ProductColorCommandService#createProductColor`, `NginxCachePurgeUtil#purgeProductCache`, `NginxCachePurgeUtil#purgeEsCache` |
| PUT | `/product-colors/{productColorId}` | `ProductColorCommandController#updateProductColor` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `ProductColorCommandService#updateProductColor`, `NginxCachePurgeUtil#purgeProductCache`, `NginxCachePurgeUtil#purgeEsCache` |
| DELETE | `/product-colors/{productColorId}` | `ProductColorCommandController#deleteProductColor` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `ProductColorCommandService#deleteProductColor`, `NginxCachePurgeUtil#purgeProductCache`, `NginxCachePurgeUtil#purgeEsCache` |
| POST | `/products/command` | `ProductCommandController#createProduct` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `ProductCommandService#createProduct` |
| PUT | `/products/command/{productId}` | `ProductCommandController#updateProduct` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `ProductCommandService#updateProduct` |
| DELETE | `/products/command/{productId}` | `ProductCommandController#deleteProduct` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `ProductCommandService#deleteProduct` |
| GET | `/products/indexed/search` | `IndexedProductQueryController#searchProductsByNames` | `IndexedProductQueryService#searchProductsByNames`, `this#toPageDto` |
| GET | `/interests/{userId}` | `InterestQueryController#getUserInterestProducts` | `InterestQueryService#findUserInterestProducts` |
| GET | `/products/query` | `ProductQueryController#searchProducts` | `ProductQueryService#searchProducts`, `this#toPageDto` |
| GET | `/products/query/{productId}/detail` | `ProductQueryController#getProductDetail` | `ProductQueryService#getProductDetail`, `ViewEventProducer#sendViewEvent` |
| GET | `/products/query/{productId}/images` | `ProductQueryController#getProductImage` |  |
| GET | `/products/query/filters` | `ProductQueryController#getFilterData` | `FilterService#getAllFilterData` |
| POST | `/products/query/filters/count` | `ProductQueryController#countProductsByFilter` | `ProductQueryService#countProductsByFilter` |
| GET | `/admin/products/query/{productId}/detail` | `ProductQueryControllerForAdmin#getProductDetailForAdmin` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `ProductEntityService#findById` |
| GET | `/admin/products/query/colors/{colorId}` | `ProductQueryControllerForAdmin#getProductColorDetail` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `ProductColorQueryService#findById` |
| GET | `/admin/products/query/{productId}/colors` | `ProductQueryControllerForAdmin#getProductColors` | `this#extractEmailFromSecurityContext`, `UserQueryService#checkAdminRole`, `ProductEntityService#findById` |
| GET | `/es/products` | `ProductColorSearchController#esSearchProducts` | `this#convertGenders`, `ProductColorSearchService#searchToDto`, `this#toPageDto` |
| GET | `/es/products/autocomplete` | `ProductColorSearchController#autocompleteProducts` | `ProductColorSearchService#autocomplete` |
| GET | `/es/products/raw-indices` | `ProductColorSearchController#getRawIndices` | `ProductColorSearchService#getAllIndexedColors`, `this#toPageDto` |
| GET | `/es/products/raw-indices/category/{categoryId}` | `ProductColorSearchController#getRawIndicesByCategory` | `ProductColorSearchService#getIndexedColorsByCategory`, `this#toPageDto` |
| GET | `/es/products/index-stats` | `ProductColorSearchController#getIndexStats` | `ProductColorSearchService#getIndexStats` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `BrandCommandService`
  - `createBrand` → BrandRepository#existsByName, BrandRepository#save~
  - `updateBrand` → BrandRepository#findById~, BrandRepository#existsByName
  - `deleteBrand` → BrandRepository#findByName, ProductRepository#existsByBrand, BrandRepository#delete~
- `BrandEntityService`
  - `findByName` → BrandRepository#findByName
- `BrandQueryService`
  - `findByName` → BrandRepository#findByName
  - `findAllBrands` → BrandRepository#findAllByOrderByNameDesc
  - `findById` → BrandRepository#findById~
  - `findBrandById` → BrandRepository#findById~
  - `findBrandsPaging` → BrandRepository#findAll~
  - `searchBrandsByName` → BrandRepository#findByNameContainingIgnoreCase
- `CategoryCommandService`
  - `createCategory` → CategoryRepository#existsByNameAndParentCategoryIsNull, CategoryRepository#save~, CategoryRepository#findByNameAndParentCategoryIsNull, CategoryRepository#existsByNameAndParentCategory
  - `updateCategory` → CategoryRepository#findById~, CategoryRepository#existsByNameAndParentCategoryIsNull, CategoryRepository#existsByNameAndParentCategory
  - `deleteCategory` → CategoryRepository#findById~, CategoryRepository#existsByParentCategory, ProductRepository#existsByCategory, CategoryRepository#delete~
- `CategoryEntityService`
  - `findMainCategoryByName` → CategoryRepository#findByNameAndParentCategoryIsNull
  - `findSubCategoryByName` → this#findMainCategoryByName, CategoryRepository#findByNameAndParentCategory
- `CategoryQueryService`
  - `findAllMainCategories` → CategoryRepository#findByParentCategoryIsNullOrderByNameDesc
  - `findSubCategoriesByMainCategory` → CategoryRepository#findByNameAndParentCategoryIsNull, CategoryRepository#findByParentCategoryOrderByNameDesc
  - `findRootCategoryById` → CategoryRepository#findWithParentById, this#findRootCategory
  - `findSubCategoriesByParentId` → CategoryRepository#findByParentCategoryId
  - `findSubCategoriesByParentName` → CategoryRepository#findByParentCategoryName
- `CollectionCommandService`
  - `createCollection` → CollectionRepository#findByName, CollectionRepository#save~
  - `updateCollection` → CollectionRepository#findById~, CollectionRepository#existsByName
  - `deleteCollection` → CollectionRepository#findByName, ProductRepository#existsByCollection, CollectionRepository#delete~
  - `createOrGetCollection` → CollectionRepository#findByName, CollectionRepository#save~
- `CollectionQueryService`
  - `findAllCollections` → CollectionRepository#findAllByOrderByNameDesc
  - `findByName` → CollectionRepository#findByName
  - `findCollectionById` → CollectionRepository#findById~
  - `findCollectionsPaging` → CollectionRepository#findAll~
  - `searchCollectionsByName` → CollectionRepository#findByNameContainingIgnoreCase
- `CreateSizesJobConfig`
  - `createSizesJob` → this#createSizesStep
  - `createSizesStep` → this#sizeItemReader, this#sizeItemProcessor, this#sizeItemWriter, this#skipListener
  - `sizeItemReader` → CategoryQueryService#findRootCategoryById, this#determineSizeType, ProductSizeRepository#findAllByProductColorId
  - `sizeItemProcessor` → this#isValidSize, ProductColorRepository#findById~
  - `sizeItemWriter` → ProductSizeRepository#save~
- `FilterService`
  - `getAllFilterData` → this#getGenderLabel, this#getDiscountData, this#getPriceRangeData, CategoryQueryService#findAllMainCategories, CategoryQueryService#findSubCategoriesByMainCategory, this#processCategoryData, BrandQueryService#findAllBrands, CollectionQueryService#findAllCollections
  - `getPriceRangeData` → this#createPriceRange
  - `processCategoryData` → CategoryQueryService#findSubCategoriesByParentId
- `IndexedProductQueryService`
  - `searchProductsByNames` → IndexedProductRepository#searchProductsByNames, ProductQueryDslRepository#styleCountQuery, ProductQueryDslRepository#tradeCountQuery
- `IndexedProductRepository`
  - `searchProductsByNames` → this#findBrandIdsByNames, this#findCategoryIdsByNames, this#findCollectionIdsByNames, this#searchProductsByIdsOptimized
  - `searchProductsByIdsOptimized` → this#buildKeywordPredicateOptimized, this#buildCategoryIdsPredicate, this#buildGenderPredicate, this#buildBrandIdsPredicate, this#buildCollectionIdsPredicate, this#buildColorPredicate, this#buildSizePredicate, this#buildPricePredicate, this#applySortingOptimized, this#countProductsByConditions
  - `countProductsByConditions` → this#buildKeywordPredicateOptimized, this#buildCategoryIdsPredicate, this#buildGenderPredicate, this#buildBrandIdsPredicate, this#buildCollectionIdsPredicate, this#buildColorPredicate, this#buildSizePredicate, this#buildPricePredicate
- `InterestCommandService`
  - `toggleInterest` → UserQueryService#findByEmail, ProductColorQueryService#findById, InterestRepository#findByUserAndProductColor, InterestRepository#delete~, InterestRepository#save~
  - `deleteAllInterestsByProductColor` → InterestRepository#delete~
- `InterestQueryDslRepository`
  - `findUserInterestProducts` → this#getOrderByClause
- `InterestQueryService`
  - `findUserInterestProducts` → InterestQueryDslRepository#findUserInterestProducts
- `ProductColorCommandService`
  - `createProductColor` → this#validateColorType, ProductEntityService#findById, FileUtils#saveFile, ProductColorRepository#save~, ProductSizeCommandService#createProductSizes
  - `updateProductColor` → ProductColorRepository#findById~, this#validateColorType, FileUtils#deleteFile, FileUtils#saveFile, ProductSizeCommandService#deleteProductSize, ProductSizeCommandService#createProductSizes, ProductColorRepository#save~
  - `deleteProductColor` → ProductColorRepository#findById~, InterestCommandService#deleteAllInterestsByProductColor, ProductSizeCommandService#deleteAllSizesByProductColor, FileUtils#deleteFile, ProductImageCommandService#deleteProductImage, ProductImageQueryService#existsByProductColorId, ProductImageQueryService#findAllByProductColorId, FileUtils#existsFile, ProductDetailImageQueryService#existsByProductColorId, ProductDetailImageQueryService#findAllByProductColorId, ProductDetailImageCommandService#deleteProductDetailImage, ProductColorRepository#delete~
- `ProductColorIndexInitializer`
  - `run` → ProductColorIndexingService#indexAllColors
- `ProductColorIndexingService`
  - `indexAllColors` → ProductColorIndexQueryRepository#findAllForIndexingDto, ProductColorIndexQueryRepository#findAllSizesForIndexing, ProductColorEsRepository#saveAll~
  - `indexColorById` → ProductColorIndexQueryRepository#findOneForIndexingDto, ProductColorIndexQueryRepository#findSizesByColorId, ProductColorEsRepository#save~
  - `deleteColorFromIndex` → ProductColorEsRepository#deleteById~
- `ProductColorQueryService`
  - `findById` → ProductColorRepository#findById~
  - `findAllByProductId` → ProductColorRepository#findByProductId
- `ProductColorSearchService`
  - `searchToDto` → this#search, StyleRepository#styleCountByColorIds, OrderBidRepository#tradeCountByColorIds
  - `search` → this#rangeQuery
- `ProductCommandService`
  - `createProduct` → BrandEntityService#findByName, CategoryEntityService#findSubCategoryByName, CollectionCommandService#createOrGetCollection, ProductRepository#save~
  - `updateProduct` → ProductRepository#findById~, BrandEntityService#findByName, CategoryEntityService#findSubCategoryByName, CollectionCommandService#createOrGetCollection
  - `deleteProduct` → ProductRepository#findById~, ProductColorCommandService#deleteProductColor, ProductRepository#delete~
- `ProductDetailImageCommandService`
  - `createProductDetailImage` → ProductDetailImageRepository#save~
  - `deleteProductDetailImage` → ProductDetailImageRepository#findById~, ProductDetailImageRepository#delete~
- `ProductDetailImageQueryService`
  - `findAllByProductColorId` → ProductDetailImageRepository#findAllByProductColorId
  - `existsByProductColorId` → ProductDetailImageRepository#existsByProductColorId
- `ProductEntityService`
  - `findById` → ProductRepository#findById~
- `ProductImageCommandService`
  - `createProductImage` → ProductImageRepository#save~
  - `deleteProductImage` → ProductImageRepository#findById~, ProductImageRepository#delete~
- `ProductImageQueryService`
  - `findAllByProductColorId` → ProductImageRepository#findAllByProductColorId
  - `existsByProductColorId` → ProductImageRepository#existsByProductColorId
- `ProductQueryDslRepository`
  - `searchProducts` → this#logPredicate, this#buildKeywordPredicate, this#buildCategoryPredicate, this#buildGenderPredicate, this#buildBrandPredicate, this#buildCollectionPredicate, this#buildColorPredicate, this#buildSizePredicate, this#buildPricePredicate
  - `countProductsByFilter` → this#buildKeywordPredicate, this#buildCategoryPredicate, this#buildGenderPredicate, this#buildBrandPredicate, this#buildCollectionPredicate, this#buildColorPredicate, this#buildSizePredicate, this#buildPricePredicate
- `ProductQueryService`
  - `searchProducts` → ProductQueryDslRepository#searchProducts, StyleRepository#styleCountByColorIds, OrderBidRepository#tradeCountByColorIds
  - `getProductDetail` → ProductQueryDslRepository#findProductDetail
  - `searchProductsByColorIds` → ProductQueryDslRepository#searchProductsByColorIds
  - `countProductsByFilter` → ProductQueryDslRepository#countProductsByFilter
- `ProductSizeCommandService`
  - `createProductSizes` → CategoryQueryService#findRootCategoryById, this#determineSizeType, ProductSizeRepository#findAllByProductColorId, this#isValidSize, ProductSizeRepository#save~
  - `deleteProductSize` → ProductSizeRepository#findById~, ProductSizeRepository#delete~
  - `updateProductSize` → ProductSizeRepository#findById~
  - `deleteAllSizesByProductColor` → ProductSizeRepository#findAllByProductColorId, this#deleteProductSize, ProductSizeRepository#flush~
- `ProductSizeQueryService`
  - `findByColorIdAndSize` → ProductSizeRepository#findByProductColorIdAndSize
  - `findSizesByColorId` → ProductSizeRepository#findAllByProductColorId
  - `findById` → ProductSizeRepository#findById~
  - `findAllByProductColorId` → ProductSizeRepository#findAllByProductColorId
- `ViewEventConsumer`
  - `listen` → ProductColorRepository#findById~, ProductColorViewLogRepository#save~
- `ViewEventKafkaConfig`
  - `viewEventKafkaTemplate` → this#viewEventProducerFactory
  - `viewEventKafkaListenerContainerFactory` → this#viewEventConsumerFactory
- `ViewEventProducer`
  - `sendViewEvent` → kafka:view-log-topic

## 3. 클래스 목록 (역할별)

### controller (11)

- `BrandController` controller/BrandController.java · public: createBrand, updateBrand, deleteBrand, getAllBrands, getBrandsPaging, searchBrands, getBrandById, getBrandByName · 주입: BrandCommandService, BrandQueryService, UserQueryService
- `CategoryController` controller/CategoryController.java · public: createCategory, updateCategory, deleteCategory, getAllMainCategories, getSubCategoriesByMain · 주입: CategoryCommandService, CategoryQueryService, UserQueryService
- `CollectionController` controller/CollectionController.java · public: createCollection, updateCollection, deleteCollection, getAllCollections, getCollectionsPaging, searchCollections, getCollectionById, getCollectionByName · 주입: CollectionCommandService, CollectionQueryService, UserQueryService
- `InterestCommandController` controller/command/InterestCommandController.java · public: toggleInterest · 주입: InterestCommandService
- `ProductColorCommandController` controller/command/ProductColorCommandController.java · public: createProductColor, updateProductColor, deleteProductColor · 주입: ProductColorCommandService, UserQueryService, NginxCachePurgeUtil
- `ProductCommandController` controller/command/ProductCommandController.java · public: createProduct, updateProduct, deleteProduct · 주입: ProductCommandService, UserQueryService
- `IndexedProductQueryController` controller/query/IndexedProductQueryController.java · public: searchProductsByNames · 주입: IndexedProductQueryService
- `InterestQueryController` controller/query/InterestQueryController.java · public: getUserInterestProducts · 주입: InterestQueryService
- `ProductQueryController` controller/query/ProductQueryController.java · public: searchProducts, getProductDetail, getProductImage, getFilterData, countProductsByFilter · 주입: ProductQueryService, UserQueryService, ViewEventProducer, FilterService
- `ProductQueryControllerForAdmin` controller/query/ProductQueryControllerForAdmin.java · public: getProductDetailForAdmin, getProductColorDetail, getProductColors · 주입: ProductQueryService, ProductEntityService, ProductColorQueryService, UserQueryService
- `ProductColorSearchController` elasticsearch/controller/ProductColorSearchController.java · public: esSearchProducts, autocompleteProducts, getRawIndices, getRawIndicesByCategory, getIndexStats · 주입: ProductColorSearchService, ProductQueryService

### service (25)

- `ProductColorIndexingService` elasticsearch/service/ProductColorIndexingService.java · public: indexAllColors, indexColorById, deleteColorFromIndex · 주입: ProductColorIndexQueryRepository, ProductColorEsRepository
- `ProductColorSearchService` elasticsearch/service/ProductColorSearchService.java · public: searchToDto, search, autocomplete, getAllIndexedColors, getIndexedColorsByCategory, getIndexStats · 주입: ElasticsearchOperations, ProductColorEsRepository, StyleRepository, OrderBidRepository, ObjectMapper
- `BrandCommandService` service/brand/BrandCommandService.java · public: createBrand, updateBrand, deleteBrand · 주입: BrandRepository, ProductRepository
- `BrandEntityService` service/brand/BrandEntityService.java · public: findByName · 주입: BrandRepository
- `BrandQueryService` service/brand/BrandQueryService.java · public: findByName, findAllBrands, findById, findBrandById, findBrandsPaging, searchBrandsByName · 주입: BrandRepository
- `CategoryCommandService` service/category/CategoryCommandService.java · public: createCategory, updateCategory, deleteCategory · 주입: CategoryRepository, ProductRepository
- `CategoryEntityService` service/category/CategoryEntityService.java · public: findMainCategoryByName, findSubCategoryByName · 주입: CategoryRepository
- `CategoryQueryService` service/category/CategoryQueryService.java · public: findAllMainCategories, findSubCategoriesByMainCategory, findRootCategory, findRootCategoryById, findSubCategoriesByParentId, findSubCategoriesByParentName · 주입: CategoryRepository
- `CollectionCommandService` service/collection/CollectionCommandService.java · public: createCollection, updateCollection, deleteCollection, createOrGetCollection · 주입: CollectionRepository, ProductRepository
- `CollectionQueryService` service/collection/CollectionQueryService.java · public: findAllCollections, findByName, findCollectionById, findCollectionsPaging, searchCollectionsByName · 주입: CollectionRepository
- `FilterService` service/filter/FilterService.java · public: getAllFilterData · 주입: BrandQueryService, CategoryQueryService, CollectionQueryService
- `InterestCommandService` service/interest/InterestCommandService.java · public: toggleInterest, deleteAllInterestsByProductColor · 주입: InterestRepository, UserQueryService, ProductColorQueryService
- `InterestQueryService` service/interest/InterestQueryService.java · public: findUserInterestProducts · 주입: InterestQueryDslRepository
- `IndexedProductQueryService` service/product/IndexedProductQueryService.java · public: searchProductsByNames · 주입: IndexedProductRepository, ProductQueryDslRepository
- `ProductCommandService` service/product/ProductCommandService.java · public: createProduct, updateProduct, deleteProduct · 주입: ProductRepository, BrandEntityService, CategoryEntityService, CollectionCommandService, ProductColorCommandService
- `ProductEntityService` service/product/ProductEntityService.java · public: findById · 주입: ProductRepository
- `ProductQueryService` service/product/ProductQueryService.java · public: searchProducts, getProductDetail, searchProductsByColorIds, countProductsByFilter · 주입: ProductQueryDslRepository, StyleRepository, OrderBidRepository
- `ProductColorCommandService` service/productColor/ProductColorCommandService.java · public: createProductColor, updateProductColor, deleteProductColor · 주입: ProductColorRepository, ProductSizeCommandService, FileUtils, ProductEntityService, ProductSizeQueryService, InterestCommandService, ProductImageCommandService, ProductDetailImageCommandService, ProductImageQueryService, ProductDetailImageQueryService, NginxCachePurgeUtil, JobLauncher, Job
- `ProductColorQueryService` service/productColor/ProductColorQueryService.java · public: findById, findAllByProductId · 주입: ProductColorRepository
- `ProductDetailImageCommandService` service/productDetailImage/ProductDetailImageCommandService.java · public: createProductDetailImage, deleteProductDetailImage · 주입: ProductDetailImageRepository
- `ProductDetailImageQueryService` service/productDetailImage/ProductDetailImageQueryService.java · public: findAllByProductColorId, existsByProductColorId · 주입: ProductDetailImageRepository
- `ProductImageCommandService` service/productImage/ProductImageCommandService.java · public: createProductImage, deleteProductImage · 주입: ProductImageRepository
- `ProductImageQueryService` service/productImage/ProductImageQueryService.java · public: findAllByProductColorId, existsByProductColorId · 주입: ProductImageRepository
- `ProductSizeCommandService` service/productSize/ProductSizeCommandService.java · public: createProductSizes, deleteProductSize, updateProductSize, deleteAllSizesByProductColor · 주입: ProductSizeRepository, CategoryQueryService
- `ProductSizeQueryService` service/productSize/ProductSizeQueryService.java · public: findByColorIdAndSize, findSizesByColorId, findById, findAllByProductColorId · 주입: ProductSizeRepository

### repository (16)

- `ProductColorEsRepository` (interface) elasticsearch/repository/ProductColorEsRepository.java · extends/implements ElasticsearchRepository
- `ProductColorIndexQueryRepository` elasticsearch/repository/ProductColorIndexQueryRepository.java · public: findAllForIndexingDto, findAllSizesForIndexing, findOneForIndexingDto, findSizesByColorId · 주입: JPAQueryFactory
- `BrandRepository` (interface) repository/BrandRepository.java · extends/implements JpaRepository · public: findByName, existsByName, findAllByOrderByNameDesc, findByNameContainingIgnoreCase
- `CategoryRepository` (interface) repository/CategoryRepository.java · extends/implements JpaRepository · public: findByNameAndParentCategoryIsNull, findByNameAndParentCategory, findByParentCategoryIsNullOrderByNameDesc, findByParentCategoryOrderByNameDesc, findWithParentById, findByNameAndParentCategoryIsNotNull, existsByNameAndParentCategoryIsNull, existsByNameAndParentCategory, existsByParentCategory, findByParentCategoryId, findByParentCategoryName
- `CollectionRepository` (interface) repository/CollectionRepository.java · extends/implements JpaRepository · public: findByName, existsByName, findAllByOrderByNameDesc, findByNameContainingIgnoreCase
- `IndexedProductRepository` repository/IndexedProductRepository.java · public: findBrandIdsByNames, findCategoryIdsByNames, findCollectionIdsByNames, searchProductsByNames · 주입: JPAQueryFactory
- `InterestQueryDslRepository` repository/InterestQueryDslRepository.java · public: findUserInterestProducts · 주입: JPAQueryFactory
- `InterestRepository` (interface) repository/InterestRepository.java · extends/implements JpaRepository · public: existsByUserAndProductColor, findByUserAndProductColor, findAllByUserId
- `ProductColorRepository` (interface) repository/ProductColorRepository.java · extends/implements JpaRepository · public: findByProductId
- `ProductColorViewLogRepository` (interface) repository/ProductColorViewLogRepository.java · extends/implements JpaRepository
- `ProductDetailImageRepository` (interface) repository/ProductDetailImageRepository.java · extends/implements JpaRepository · public: existsByProductColorId, findByProductColorId, findAllByProductColorId
- `ProductImageRepository` (interface) repository/ProductImageRepository.java · extends/implements JpaRepository · public: existsByProductColorId, findByProductColorId, findAllByProductColorId
- `ProductPriceHistoryRepository` (interface) repository/ProductPriceHistoryRepository.java · extends/implements JpaRepository · public: findByProductSizeId
- `ProductQueryDslRepository` repository/ProductQueryDslRepository.java · public: searchProducts, countProductsByFilter, findProductDetail, searchProductsByColorIds, tradeCountQuery, styleCountQuery · 주입: JPAQueryFactory
- `ProductRepository` (interface) repository/ProductRepository.java · extends/implements JpaRepository · public: existsByBrand, existsByCategory, existsByCollection
- `ProductSizeRepository` (interface) repository/ProductSizeRepository.java · extends/implements JpaRepository · public: findByProductColorId, findByProductColorIdAndSize, findAllByProductColorId

### entity (14)

- `Brand` entity/Brand.java · extends/implements BaseTimeEntity · public: updateName
- `Category` entity/Category.java · extends/implements BaseTimeEntity · public: addSubCategory, assignParentCategory, updateName, updateParentCategory, getParentCategoryName
- `Collection` entity/Collection.java · extends/implements BaseTimeEntity · public: updateName
- `Interest` entity/Interest.java · extends/implements BaseTimeEntity · public: assignUser, unassignUser, assignProductColor, unassignProductColor
- `Product` entity/Product.java · extends/implements BaseTimeEntity · public: addProductColor, update, updateProduct
- `ProductColor` entity/ProductColor.java · extends/implements BaseTimeEntity · public: assignProduct, addThumbnailImage, addProductImage, addProductDetailImage, addProductSize, update
- `ProductColorViewLog` entity/ProductColorViewLog.java · public: create, addViewedAt
- `ProductDetailImage` entity/ProductDetailImage.java · extends/implements BaseTimeEntity · public: assignProductColor
- `ProductImage` entity/ProductImage.java · extends/implements BaseTimeEntity · public: assignProductColor, unassignProductColor
- `ProductPriceHistory` entity/ProductPriceHistory.java · extends/implements BaseTimeEntity · public: assignProductSize
- `ProductSize` entity/ProductSize.java · extends/implements BaseTimeEntity · public: assignProductColor, update
- `ColorType` (enum) entity/enumType/ColorType.java · public: getDisplayName
- `GenderType` (enum) entity/enumType/GenderType.java
- `SizeType` (enum) entity/enumType/SizeType.java · public: getSizes

### event (3)

- `ViewEvent` dto/kafka/ViewEvent.java
- `ViewEventConsumer` service/kafka/ViewEventConsumer.java · public: listen · 주입: ProductColorRepository, ProductColorViewLogRepository
- `ViewEventProducer` service/kafka/ViewEventProducer.java · public: sendViewEvent · 주입: KafkaTemplate

### config (2)

- `ViewEventKafkaConfig` service/kafka/config/ViewEventKafkaConfig.java · public: viewEventProducerFactory, viewEventKafkaTemplate, viewEventConsumerFactory, viewEventKafkaListenerContainerFactory
- `CreateSizesJobConfig` service/productSize/CreateSizesJobConfig.java · public: createSizesJob, createSizesStep, sizeItemReader, sizeItemProcessor, sizeItemWriter, skipListener · 주입: JobRepository, PlatformTransactionManager, ProductColorRepository, ProductSizeRepository, CategoryQueryService

### dto (25)

- `BrandRequestDto` dto/BrandRequestDto.java
- `BrandResponseDto` dto/BrandResponseDto.java
- `CategoryRequestDto` dto/CategoryRequestDto.java
- `CategoryResponseDto` dto/CategoryResponseDto.java
- `CollectionRequestDto` dto/CollectionRequestDto.java
- `CollectionResponseDto` dto/CollectionResponseDto.java
- `FilterCountResponseDto` dto/FilterCountResponseDto.java
- `FilterDataResponseDto` dto/FilterDataResponseDto.java
- `ProductColorCreateRequestDto` dto/ProductColorCreateRequestDto.java
- `ProductColorDetailAdminResponseDto` dto/ProductColorDetailAdminResponseDto.java
- `ProductColorUpdateRequestDto` dto/ProductColorUpdateRequestDto.java
- `ProductCreateRequestDto` dto/ProductCreateRequestDto.java
- `ProductCreateResponseDto` dto/ProductCreateResponseDto.java
- `ProductDetailAdminResponseDto` dto/ProductDetailAdminResponseDto.java
- `ProductDetailImageDto` dto/ProductDetailImageDto.java
- `ProductDetailResponseDto` dto/ProductDetailResponseDto.java
- `ProductImageDto` dto/ProductImageDto.java
- `ProductSearchByNameDto` dto/ProductSearchByNameDto.java
- `ProductSearchDto` dto/ProductSearchDto.java
- `ProductSearchResponseDto` dto/ProductSearchResponseDto.java
- `ProductSizeDto` dto/ProductSizeDto.java
- `ProductUpdateRequestDto` dto/ProductUpdateRequestDto.java
- `ProductUpdateResponseDto` dto/ProductUpdateResponseDto.java
- `SizeCreationItem` dto/SizeCreationItem.java
- `ProductColorIndexDto` elasticsearch/dto/ProductColorIndexDto.java

### exception (6)

- `BrandNotFoundException` exception/BrandNotFoundException.java
- `CategoryNotFoundException` exception/CategoryNotFoundException.java
- `InvalidSizeTypeException` exception/InvalidSizeTypeException.java
- `ProductColorNotFoundException` exception/ProductColorNotFoundException.java
- `ProductErrorCode` exception/ProductErrorCode.java
- `ProductException` exception/ProductException.java

### other (4)

- `ProductColorIndexInitializer` elasticsearch/config/ProductColorIndexInitializer.java · extends/implements CommandLineRunner · public: run · 주입: ProductColorIndexingService, ElasticsearchOperations
- `ProductColorSizeRow` elasticsearch/dto/ProductColorSizeRow.java
- `ProductColorIndex` elasticsearch/index/ProductColorIndex.java
- `SortOption` repository/SortOption.java

### 다른 도메인 참조 (import·주입 기준)

- **order**: `BidStatus` ← ProductQueryDslRepository; `OrderBidRepository` ← ProductColorSearchService, ProductQueryService
- **style**: `StyleRepository` ← ProductColorSearchService, ProductQueryService
- **user**: `Gender` ← ProductColorViewLog, ProductQueryController, ViewEvent, ViewEventProducer; `User` ← Interest, InterestCommandService, InterestRepository; `UserQueryService` ← BrandController, CategoryController, CollectionController, InterestCommandService, ProductColorCommandController, ProductCommandController, ProductQueryController, ProductQueryControllerForAdmin

### 이 도메인을 참조하는 다른 도메인

- **adminevent**: Brand
- **adminorder**: ProductSize
- **adminproduct**: Brand, BrandRepository, Category, CategoryRepository, Collection, ColorType, GenderType, Interest, Product, ProductColor, ProductColorRepository, ProductColorViewLog, ProductDetailImage, ProductImage, ProductImageRepository, ProductPriceHistory, ProductRepository, ProductSize, ProductSizeRepository, SizeType, ViewEvent
- **adminsale**: ProductSize
- **event**: Brand, BrandQueryService
- **global**: BrandRepository, CategoryRepository, ColorType, GenderType, ProductColorRepository, ProductImageRepository, ProductRepository, ProductSizeRepository, SizeType
- **internalevent**: Brand
- **internalorder**: ProductSize
- **internalproduct**: Brand, BrandRepository, Category, CategoryRepository, Collection, ColorType, GenderType, Interest, Product, ProductColor, ProductColorRepository, ProductColorViewLog, ProductDetailImage, ProductImage, ProductImageRepository, ProductPriceHistory, ProductRepository, ProductSize, ProductSizeRepository, SizeType, ViewEvent
- **internalsale**: ProductSize
- **legacyevent**: Brand
- **legacyorder**: ProductSize
- **legacyproduct**: Brand, BrandRepository, Category, CategoryRepository, Collection, ColorType, GenderType, Interest, Product, ProductColor, ProductColorRepository, ProductColorViewLog, ProductDetailImage, ProductImage, ProductImageRepository, ProductPriceHistory, ProductRepository, ProductSize, ProductSizeRepository, SizeType, ViewEvent
- **legacysale**: ProductSize
- **order**: ProductSize, ProductSizeQueryService
- **partnerevent**: Brand
- **partnerorder**: ProductSize
- **partnerproduct**: Brand, BrandRepository, Category, CategoryRepository, Collection, ColorType, GenderType, Interest, Product, ProductColor, ProductColorRepository, ProductColorViewLog, ProductDetailImage, ProductImage, ProductImageRepository, ProductPriceHistory, ProductRepository, ProductSize, ProductSizeRepository, SizeType, ViewEvent
- **partnersale**: ProductSize
- **sale**: ProductSize, ProductSizeQueryService
- **settlementevent**: Brand
- **settlementorder**: ProductSize
- **settlementproduct**: Brand, BrandRepository, Category, CategoryRepository, Collection, ColorType, GenderType, Interest, Product, ProductColor, ProductColorRepository, ProductColorViewLog, ProductDetailImage, ProductImage, ProductImageRepository, ProductPriceHistory, ProductRepository, ProductSize, ProductSizeRepository, SizeType, ViewEvent
- **settlementsale**: ProductSize
- **user**: Interest

## 4. JPA 엔티티 연관

- `Brand` @Entity @Table(name = "brand", indexes = { @Index(name = "idx_brand_name", columnList = "name") }) · extends BaseTimeEntity
- `Category` @Entity @Table(name = "category", indexes = { @Index(name = "idx_category_name", columnList = "name"), @Index(name = "idx_category_parent", columnList = "parent_category_id") }) · extends BaseTimeEntity
  - parentCategory: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "parent_category_id") → `Category`
  - subCategories: @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL, orphanRemoval = true) → `Category`
- `Collection` @Entity @Table(name = "collection", indexes = { @Index(name = "idx_collection_name", columnList = "name") }) · extends BaseTimeEntity
- `Interest` @Entity · extends BaseTimeEntity
  - user: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") → `User` [user]
  - productColor: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_color_id") → `ProductColor`
- `Product` @Entity @Table(name = "product", indexes = { @Index(name = "idx_product_brand", columnList = "brand_id"), @Index(name = "idx_product_category", columnList = "category_id"), @Index(name = "idx_product_collection", columnList = "collection_id"), @Index(name = "idx_product_gender", columnList = "gender"), @Index(name = "idx_product_price", columnList = "release_price"), @Index(name = "idx_product_date", columnList = "release_date"), @Index(name = "idx_product_brand_category", columnList = "brand_id, category_id"), @Index(name = "idx_product_name", columnList = "name"), @Index(name = "idx_product_english_name", columnList = "english_name") }) · extends BaseTimeEntity
  - brand: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "brand_id") → `Brand`
  - category: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "category_id") → `Category`
  - collection: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "collection_id") → `Collection`
  - colors: @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true) → `ProductColor`
- `ProductColor` @Entity @Table(name = "product_color", indexes = { @Index(name = "idx_product_color_name", columnList = "color_name"), @Index(name = "idx_product_color_product", columnList = "product_id") }) · extends BaseTimeEntity
  - product: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id") → `Product`
  - thumbnailImage: @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true) @JoinColumn(name = "thumbnail_image_id", nullable = true) → `ProductImage`
  - sizes: @OneToMany(mappedBy = "productColor", cascade = CascadeType.ALL, orphanRemoval = true) → `ProductSize`
  - productImages: @OneToMany(mappedBy = "productColor", cascade = CascadeType.ALL, orphanRemoval = true) → `ProductImage`
  - productDetailImages: @OneToMany(mappedBy = "productColor", cascade = CascadeType.ALL, orphanRemoval = true) → `ProductDetailImage`
  - interests: @OneToMany(mappedBy = "productColor", cascade = CascadeType.ALL, orphanRemoval = true) → `Interest`
- `ProductColorViewLog` @Entity
  - productColor: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_color_id") → `ProductColor`
- `ProductDetailImage` @Entity · extends BaseTimeEntity
  - productColor: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_color_id", nullable = false) → `ProductColor`
- `ProductImage` @Entity · extends BaseTimeEntity
  - productColor: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_color_id", nullable = false) → `ProductColor`
- `ProductPriceHistory` @Entity · extends BaseTimeEntity
  - productSize: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_size_id") → `ProductSize`
- `ProductSize` @Entity @Table(name = "product_size", indexes = { @Index(name = "idx_product_size_color", columnList = "product_color_id"), @Index(name = "idx_product_size_purchase_price", columnList = "purchase_price") }) · extends BaseTimeEntity
  - productColor: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_color_id") → `ProductColor`

다른 도메인 엔티티에서 들어오는 연관:

- `Event.brand` @ManyToOne → `Brand` [event]
- `OrderBid.productSize` @ManyToOne → `ProductSize` [order]
- `OrderItem.productSize` @ManyToOne → `ProductSize` [order]
- `Sale.productSize` @ManyToOne → `ProductSize` [sale]
- `SaleBid.productSize` @ManyToOne → `ProductSize` [sale]
- `User.interests` @OneToMany → `Interest` [user]

## 5. 이벤트·메시징

- 발행 kafka `view-log-topic`: `ViewEventProducer#sendViewEvent` → `AdminViewEventConsumer#listen` [adminproduct], `InternalViewEventConsumer#listen` [internalproduct], `LegacyViewEventConsumer#listen` [legacyproduct], `PartnerViewEventConsumer#listen` [partnerproduct], `ViewEventConsumer#listen` [product], `SettlementViewEventConsumer#listen` [settlementproduct]
- 구독 kafka `view-log-topic`: `ViewEventConsumer#listen` (groupId=my-group containerFactory=viewEventKafkaListenerContainerFactory) ← `AdminViewEventProducer#sendViewEvent` [adminproduct], `InternalViewEventProducer#sendViewEvent` [internalproduct], `LegacyViewEventProducer#sendViewEvent` [legacyproduct], `PartnerViewEventProducer#sendViewEvent` [partnerproduct], `ViewEventProducer#sendViewEvent` [product], `SettlementViewEventProducer#sendViewEvent` [settlementproduct]

## 6. AOP

(없음)

