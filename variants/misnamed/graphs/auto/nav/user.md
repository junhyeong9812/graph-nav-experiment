# user 도메인

기준 경로: `src/main/java/com/fream/back/domain/user/` · 파일 106개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)

## 1. 엔드포인트

| HTTP | 경로 | 핸들러 | 직접 호출 |
|---|---|---|---|
| POST | `/brands` | `BrandController#createBrand` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `BrandCommandService#createBrand` |
| PUT | `/brands/{brandId}` | `BrandController#updateBrand` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `BrandCommandService#updateBrand` |
| DELETE | `/brands/{brandName}` | `BrandController#deleteBrand` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `BrandCommandService#deleteBrand` |
| GET | `/brands` | `BrandController#getAllBrands` | `BrandQueryService#findAllBrands` |
| GET | `/brands/page` | `BrandController#getBrandsPaging` | `BrandQueryService#findBrandsPaging` |
| GET | `/brands/search` | `BrandController#searchBrands` | `BrandQueryService#searchBrandsByName` |
| GET | `/brands/{brandId:[0-9]+}` | `BrandController#getBrandById` | `BrandQueryService#findBrandById` |
| GET | `/brands/name/{brandName}` | `BrandController#getBrandByName` | `BrandQueryService#findByName` |
| POST | `/categories` | `CategoryController#createCategory` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `CategoryCommandService#createCategory` |
| PUT | `/categories/{categoryId}` | `CategoryController#updateCategory` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `CategoryCommandService#updateCategory` |
| DELETE | `/categories/{categoryId}` | `CategoryController#deleteCategory` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `CategoryCommandService#deleteCategory` |
| GET | `/categories/main` | `CategoryController#getAllMainCategories` | `CategoryQueryService#findAllMainCategories` |
| GET | `/categories/sub/{mainCategoryName}` | `CategoryController#getSubCategoriesByMain` | `CategoryQueryService#findSubCategoriesByMainCategory` |
| POST | `/collections` | `CollectionController#createCollection` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `CollectionCommandService#createCollection` |
| PUT | `/collections/{collectionId}` | `CollectionController#updateCollection` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `CollectionCommandService#updateCollection` |
| DELETE | `/collections/{collectionName}` | `CollectionController#deleteCollection` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `CollectionCommandService#deleteCollection` |
| GET | `/collections` | `CollectionController#getAllCollections` | `CollectionQueryService#findAllCollections` |
| GET | `/collections/page` | `CollectionController#getCollectionsPaging` | `CollectionQueryService#findCollectionsPaging` |
| GET | `/collections/search` | `CollectionController#searchCollections` | `CollectionQueryService#searchCollectionsByName` |
| GET | `/collections/{collectionId:[0-9]+}` | `CollectionController#getCollectionById` | `CollectionQueryService#findCollectionById` |
| GET | `/collections/name/{collectionName}` | `CollectionController#getCollectionByName` | `CollectionQueryService#findByName` |
| POST | `/interests/{productColorId}/toggle` | `InterestCommandController#toggleInterest` | `this#extractEmailFromSecurityContext`, `InterestCommandService#toggleInterest` |
| POST | `/product-colors/{productId}` | `UserColorCommandController#createProductColor` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `UserColorCommandService#createProductColor`, `NginxCachePurgeUtil#purgeProductCache`, `NginxCachePurgeUtil#purgeEsCache` |
| PUT | `/product-colors/{productColorId}` | `UserColorCommandController#updateProductColor` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `UserColorCommandService#updateProductColor`, `NginxCachePurgeUtil#purgeProductCache`, `NginxCachePurgeUtil#purgeEsCache` |
| DELETE | `/product-colors/{productColorId}` | `UserColorCommandController#deleteProductColor` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `UserColorCommandService#deleteProductColor`, `NginxCachePurgeUtil#purgeProductCache`, `NginxCachePurgeUtil#purgeEsCache` |
| POST | `/products/command` | `UserCommandController#createProduct` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `UserCommandService#createProduct` |
| PUT | `/products/command/{productId}` | `UserCommandController#updateProduct` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `UserCommandService#updateProduct` |
| DELETE | `/products/command/{productId}` | `UserCommandController#deleteProduct` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `UserCommandService#deleteProduct` |
| GET | `/products/indexed/search` | `IndexedUserQueryController#searchProductsByNames` | `IndexedUserQueryService#searchProductsByNames`, `this#toPageDto` |
| GET | `/interests/{userId}` | `InterestQueryController#getUserInterestProducts` | `InterestQueryService#findUserInterestProducts` |
| GET | `/products/query` | `UserQueryController#searchProducts` | `UserQueryService#searchProducts`, `this#toPageDto` |
| GET | `/products/query/{productId}/detail` | `UserQueryController#getProductDetail` | `UserQueryService#getProductDetail`, `ViewEventProducer#sendViewEvent` |
| GET | `/products/query/{productId}/images` | `UserQueryController#getProductImage` |  |
| GET | `/products/query/filters` | `UserQueryController#getFilterData` | `FilterService#getAllFilterData` |
| POST | `/products/query/filters/count` | `UserQueryController#countProductsByFilter` | `UserQueryService#countProductsByFilter` |
| GET | `/admin/products/query/{productId}/detail` | `UserQueryControllerForAdmin#getProductDetailForAdmin` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `UserEntityService#findById` |
| GET | `/admin/products/query/colors/{colorId}` | `UserQueryControllerForAdmin#getProductColorDetail` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `UserColorQueryService#findById` |
| GET | `/admin/products/query/{productId}/colors` | `UserQueryControllerForAdmin#getProductColors` | `this#extractEmailFromSecurityContext`, `ProductQueryService#checkAdminRole`, `UserEntityService#findById` |
| GET | `/es/products` | `UserColorSearchController#esSearchProducts` | `this#convertGenders`, `UserColorSearchService#searchToDto`, `this#toPageDto` |
| GET | `/es/products/autocomplete` | `UserColorSearchController#autocompleteProducts` | `UserColorSearchService#autocomplete` |
| GET | `/es/products/raw-indices` | `UserColorSearchController#getRawIndices` | `UserColorSearchService#getAllIndexedColors`, `this#toPageDto` |
| GET | `/es/products/raw-indices/category/{categoryId}` | `UserColorSearchController#getRawIndicesByCategory` | `UserColorSearchService#getIndexedColorsByCategory`, `this#toPageDto` |
| GET | `/es/products/index-stats` | `UserColorSearchController#getIndexStats` | `UserColorSearchService#getIndexStats` |

## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)

- `BrandCommandService`
  - `createBrand` → BrandRepository#existsByName, BrandRepository#save~
  - `updateBrand` → BrandRepository#findById~, BrandRepository#existsByName
  - `deleteBrand` → BrandRepository#findByName, UserRepository#existsByBrand, BrandRepository#delete~
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
  - `deleteCategory` → CategoryRepository#findById~, CategoryRepository#existsByParentCategory, UserRepository#existsByCategory, CategoryRepository#delete~
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
  - `deleteCollection` → CollectionRepository#findByName, UserRepository#existsByCollection, CollectionRepository#delete~
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
  - `sizeItemReader` → CategoryQueryService#findRootCategoryById, this#determineSizeType, UserSizeRepository#findAllByProductColorId
  - `sizeItemProcessor` → this#isValidSize, UserColorRepository#findById~
  - `sizeItemWriter` → UserSizeRepository#save~
- `FilterService`
  - `getAllFilterData` → this#getGenderLabel, this#getDiscountData, this#getPriceRangeData, CategoryQueryService#findAllMainCategories, CategoryQueryService#findSubCategoriesByMainCategory, this#processCategoryData, BrandQueryService#findAllBrands, CollectionQueryService#findAllCollections
  - `getPriceRangeData` → this#createPriceRange
  - `processCategoryData` → CategoryQueryService#findSubCategoriesByParentId
- `IndexedUserQueryService`
  - `searchProductsByNames` → IndexedUserRepository#searchProductsByNames, UserQueryDslRepository#styleCountQuery, UserQueryDslRepository#tradeCountQuery
- `IndexedUserRepository`
  - `searchProductsByNames` → this#findBrandIdsByNames, this#findCategoryIdsByNames, this#findCollectionIdsByNames, this#searchProductsByIdsOptimized
  - `searchProductsByIdsOptimized` → this#buildKeywordPredicateOptimized, this#buildCategoryIdsPredicate, this#buildGenderPredicate, this#buildBrandIdsPredicate, this#buildCollectionIdsPredicate, this#buildColorPredicate, this#buildSizePredicate, this#buildPricePredicate, this#applySortingOptimized, this#countProductsByConditions
  - `countProductsByConditions` → this#buildKeywordPredicateOptimized, this#buildCategoryIdsPredicate, this#buildGenderPredicate, this#buildBrandIdsPredicate, this#buildCollectionIdsPredicate, this#buildColorPredicate, this#buildSizePredicate, this#buildPricePredicate
- `InterestCommandService`
  - `toggleInterest` → ProductQueryService#findByEmail, UserColorQueryService#findById, InterestRepository#findByUserAndProductColor, InterestRepository#delete~, InterestRepository#save~
  - `deleteAllInterestsByProductColor` → InterestRepository#delete~
- `InterestQueryDslRepository`
  - `findUserInterestProducts` → this#getOrderByClause
- `InterestQueryService`
  - `findUserInterestProducts` → InterestQueryDslRepository#findUserInterestProducts
- `UserColorCommandService`
  - `createProductColor` → this#validateColorType, UserEntityService#findById, FileUtils#saveFile, UserColorRepository#save~, UserSizeCommandService#createProductSizes
  - `updateProductColor` → UserColorRepository#findById~, this#validateColorType, FileUtils#deleteFile, FileUtils#saveFile, UserSizeCommandService#deleteProductSize, UserSizeCommandService#createProductSizes, UserColorRepository#save~
  - `deleteProductColor` → UserColorRepository#findById~, InterestCommandService#deleteAllInterestsByProductColor, UserSizeCommandService#deleteAllSizesByProductColor, FileUtils#deleteFile, UserImageCommandService#deleteProductImage, UserImageQueryService#existsByProductColorId, UserImageQueryService#findAllByProductColorId, FileUtils#existsFile, UserDetailImageQueryService#existsByProductColorId, UserDetailImageQueryService#findAllByProductColorId, UserDetailImageCommandService#deleteProductDetailImage, UserColorRepository#delete~
- `UserColorIndexInitializer`
  - `run` → UserColorIndexingService#indexAllColors
- `UserColorIndexingService`
  - `indexAllColors` → UserColorIndexQueryRepository#findAllForIndexingDto, UserColorIndexQueryRepository#findAllSizesForIndexing, UserColorEsRepository#saveAll~
  - `indexColorById` → UserColorIndexQueryRepository#findOneForIndexingDto, UserColorIndexQueryRepository#findSizesByColorId, UserColorEsRepository#save~
  - `deleteColorFromIndex` → UserColorEsRepository#deleteById~
- `UserColorQueryService`
  - `findById` → UserColorRepository#findById~
  - `findAllByProductId` → UserColorRepository#findByProductId
- `UserColorSearchService`
  - `searchToDto` → this#search, SaleRepository#styleCountByColorIds, FaqBidRepository#tradeCountByColorIds
  - `search` → this#rangeQuery
- `UserCommandService`
  - `createProduct` → BrandEntityService#findByName, CategoryEntityService#findSubCategoryByName, CollectionCommandService#createOrGetCollection, UserRepository#save~
  - `updateProduct` → UserRepository#findById~, BrandEntityService#findByName, CategoryEntityService#findSubCategoryByName, CollectionCommandService#createOrGetCollection
  - `deleteProduct` → UserRepository#findById~, UserColorCommandService#deleteProductColor, UserRepository#delete~
- `UserDetailImageCommandService`
  - `createProductDetailImage` → UserDetailImageRepository#save~
  - `deleteProductDetailImage` → UserDetailImageRepository#findById~, UserDetailImageRepository#delete~
- `UserDetailImageQueryService`
  - `findAllByProductColorId` → UserDetailImageRepository#findAllByProductColorId
  - `existsByProductColorId` → UserDetailImageRepository#existsByProductColorId
- `UserEntityService`
  - `findById` → UserRepository#findById~
- `UserImageCommandService`
  - `createProductImage` → UserImageRepository#save~
  - `deleteProductImage` → UserImageRepository#findById~, UserImageRepository#delete~
- `UserImageQueryService`
  - `findAllByProductColorId` → UserImageRepository#findAllByProductColorId
  - `existsByProductColorId` → UserImageRepository#existsByProductColorId
- `UserQueryDslRepository`
  - `searchProducts` → this#logPredicate, this#buildKeywordPredicate, this#buildCategoryPredicate, this#buildGenderPredicate, this#buildBrandPredicate, this#buildCollectionPredicate, this#buildColorPredicate, this#buildSizePredicate, this#buildPricePredicate
  - `countProductsByFilter` → this#buildKeywordPredicate, this#buildCategoryPredicate, this#buildGenderPredicate, this#buildBrandPredicate, this#buildCollectionPredicate, this#buildColorPredicate, this#buildSizePredicate, this#buildPricePredicate
- `UserQueryService`
  - `searchProducts` → UserQueryDslRepository#searchProducts, SaleRepository#styleCountByColorIds, FaqBidRepository#tradeCountByColorIds
  - `getProductDetail` → UserQueryDslRepository#findProductDetail
  - `searchProductsByColorIds` → UserQueryDslRepository#searchProductsByColorIds
  - `countProductsByFilter` → UserQueryDslRepository#countProductsByFilter
- `UserSizeCommandService`
  - `createProductSizes` → CategoryQueryService#findRootCategoryById, this#determineSizeType, UserSizeRepository#findAllByProductColorId, this#isValidSize, UserSizeRepository#save~
  - `deleteProductSize` → UserSizeRepository#findById~, UserSizeRepository#delete~
  - `updateProductSize` → UserSizeRepository#findById~
  - `deleteAllSizesByProductColor` → UserSizeRepository#findAllByProductColorId, this#deleteProductSize, UserSizeRepository#flush~
- `UserSizeQueryService`
  - `findByColorIdAndSize` → UserSizeRepository#findByProductColorIdAndSize
  - `findSizesByColorId` → UserSizeRepository#findAllByProductColorId
  - `findById` → UserSizeRepository#findById~
  - `findAllByProductColorId` → UserSizeRepository#findAllByProductColorId
- `ViewEventConsumer`
  - `listen` → UserColorRepository#findById~, UserColorViewLogRepository#save~
- `ViewEventKafkaConfig`
  - `viewEventKafkaTemplate` → this#viewEventProducerFactory
  - `viewEventKafkaListenerContainerFactory` → this#viewEventConsumerFactory
- `ViewEventProducer`
  - `sendViewEvent` → kafka:view-log-topic

## 3. 클래스 목록 (역할별)

### controller (11)

- `BrandController` controller/BrandController.java · public: createBrand, updateBrand, deleteBrand, getAllBrands, getBrandsPaging, searchBrands, getBrandById, getBrandByName · 주입: BrandCommandService, BrandQueryService, ProductQueryService
- `CategoryController` controller/CategoryController.java · public: createCategory, updateCategory, deleteCategory, getAllMainCategories, getSubCategoriesByMain · 주입: CategoryCommandService, CategoryQueryService, ProductQueryService
- `CollectionController` controller/CollectionController.java · public: createCollection, updateCollection, deleteCollection, getAllCollections, getCollectionsPaging, searchCollections, getCollectionById, getCollectionByName · 주입: CollectionCommandService, CollectionQueryService, ProductQueryService
- `InterestCommandController` controller/command/InterestCommandController.java · public: toggleInterest · 주입: InterestCommandService
- `UserColorCommandController` controller/command/UserColorCommandController.java · public: createProductColor, updateProductColor, deleteProductColor · 주입: UserColorCommandService, ProductQueryService, NginxCachePurgeUtil
- `UserCommandController` controller/command/UserCommandController.java · public: createProduct, updateProduct, deleteProduct · 주입: UserCommandService, ProductQueryService
- `IndexedUserQueryController` controller/query/IndexedUserQueryController.java · public: searchProductsByNames · 주입: IndexedUserQueryService
- `InterestQueryController` controller/query/InterestQueryController.java · public: getUserInterestProducts · 주입: InterestQueryService
- `UserQueryController` controller/query/UserQueryController.java · public: searchProducts, getProductDetail, getProductImage, getFilterData, countProductsByFilter · 주입: UserQueryService, ProductQueryService, ViewEventProducer, FilterService
- `UserQueryControllerForAdmin` controller/query/UserQueryControllerForAdmin.java · public: getProductDetailForAdmin, getProductColorDetail, getProductColors · 주입: UserQueryService, UserEntityService, UserColorQueryService, ProductQueryService
- `UserColorSearchController` elasticsearch/controller/UserColorSearchController.java · public: esSearchProducts, autocompleteProducts, getRawIndices, getRawIndicesByCategory, getIndexStats · 주입: UserColorSearchService, UserQueryService

### service (25)

- `UserColorIndexingService` elasticsearch/service/UserColorIndexingService.java · public: indexAllColors, indexColorById, deleteColorFromIndex · 주입: UserColorIndexQueryRepository, UserColorEsRepository
- `UserColorSearchService` elasticsearch/service/UserColorSearchService.java · public: searchToDto, search, autocomplete, getAllIndexedColors, getIndexedColorsByCategory, getIndexStats · 주입: ElasticsearchOperations, UserColorEsRepository, SaleRepository, FaqBidRepository, ObjectMapper
- `BrandCommandService` service/brand/BrandCommandService.java · public: createBrand, updateBrand, deleteBrand · 주입: BrandRepository, UserRepository
- `BrandEntityService` service/brand/BrandEntityService.java · public: findByName · 주입: BrandRepository
- `BrandQueryService` service/brand/BrandQueryService.java · public: findByName, findAllBrands, findById, findBrandById, findBrandsPaging, searchBrandsByName · 주입: BrandRepository
- `CategoryCommandService` service/category/CategoryCommandService.java · public: createCategory, updateCategory, deleteCategory · 주입: CategoryRepository, UserRepository
- `CategoryEntityService` service/category/CategoryEntityService.java · public: findMainCategoryByName, findSubCategoryByName · 주입: CategoryRepository
- `CategoryQueryService` service/category/CategoryQueryService.java · public: findAllMainCategories, findSubCategoriesByMainCategory, findRootCategory, findRootCategoryById, findSubCategoriesByParentId, findSubCategoriesByParentName · 주입: CategoryRepository
- `CollectionCommandService` service/collection/CollectionCommandService.java · public: createCollection, updateCollection, deleteCollection, createOrGetCollection · 주입: CollectionRepository, UserRepository
- `CollectionQueryService` service/collection/CollectionQueryService.java · public: findAllCollections, findByName, findCollectionById, findCollectionsPaging, searchCollectionsByName · 주입: CollectionRepository
- `FilterService` service/filter/FilterService.java · public: getAllFilterData · 주입: BrandQueryService, CategoryQueryService, CollectionQueryService
- `InterestCommandService` service/interest/InterestCommandService.java · public: toggleInterest, deleteAllInterestsByProductColor · 주입: InterestRepository, ProductQueryService, UserColorQueryService
- `InterestQueryService` service/interest/InterestQueryService.java · public: findUserInterestProducts · 주입: InterestQueryDslRepository
- `IndexedUserQueryService` service/product/IndexedUserQueryService.java · public: searchProductsByNames · 주입: IndexedUserRepository, UserQueryDslRepository
- `UserCommandService` service/product/UserCommandService.java · public: createProduct, updateProduct, deleteProduct · 주입: UserRepository, BrandEntityService, CategoryEntityService, CollectionCommandService, UserColorCommandService
- `UserEntityService` service/product/UserEntityService.java · public: findById · 주입: UserRepository
- `UserQueryService` service/product/UserQueryService.java · public: searchProducts, getProductDetail, searchProductsByColorIds, countProductsByFilter · 주입: UserQueryDslRepository, SaleRepository, FaqBidRepository
- `UserColorCommandService` service/productColor/UserColorCommandService.java · public: createProductColor, updateProductColor, deleteProductColor · 주입: UserColorRepository, UserSizeCommandService, FileUtils, UserEntityService, UserSizeQueryService, InterestCommandService, UserImageCommandService, UserDetailImageCommandService, UserImageQueryService, UserDetailImageQueryService, NginxCachePurgeUtil, JobLauncher, Job
- `UserColorQueryService` service/productColor/UserColorQueryService.java · public: findById, findAllByProductId · 주입: UserColorRepository
- `UserDetailImageCommandService` service/productDetailImage/UserDetailImageCommandService.java · public: createProductDetailImage, deleteProductDetailImage · 주입: UserDetailImageRepository
- `UserDetailImageQueryService` service/productDetailImage/UserDetailImageQueryService.java · public: findAllByProductColorId, existsByProductColorId · 주입: UserDetailImageRepository
- `UserImageCommandService` service/productImage/UserImageCommandService.java · public: createProductImage, deleteProductImage · 주입: UserImageRepository
- `UserImageQueryService` service/productImage/UserImageQueryService.java · public: findAllByProductColorId, existsByProductColorId · 주입: UserImageRepository
- `UserSizeCommandService` service/productSize/UserSizeCommandService.java · public: createProductSizes, deleteProductSize, updateProductSize, deleteAllSizesByProductColor · 주입: UserSizeRepository, CategoryQueryService
- `UserSizeQueryService` service/productSize/UserSizeQueryService.java · public: findByColorIdAndSize, findSizesByColorId, findById, findAllByProductColorId · 주입: UserSizeRepository

### repository (16)

- `UserColorEsRepository` (interface) elasticsearch/repository/UserColorEsRepository.java · extends/implements ElasticsearchRepository
- `UserColorIndexQueryRepository` elasticsearch/repository/UserColorIndexQueryRepository.java · public: findAllForIndexingDto, findAllSizesForIndexing, findOneForIndexingDto, findSizesByColorId · 주입: JPAQueryFactory
- `BrandRepository` (interface) repository/BrandRepository.java · extends/implements JpaRepository · public: findByName, existsByName, findAllByOrderByNameDesc, findByNameContainingIgnoreCase
- `CategoryRepository` (interface) repository/CategoryRepository.java · extends/implements JpaRepository · public: findByNameAndParentCategoryIsNull, findByNameAndParentCategory, findByParentCategoryIsNullOrderByNameDesc, findByParentCategoryOrderByNameDesc, findWithParentById, findByNameAndParentCategoryIsNotNull, existsByNameAndParentCategoryIsNull, existsByNameAndParentCategory, existsByParentCategory, findByParentCategoryId, findByParentCategoryName
- `CollectionRepository` (interface) repository/CollectionRepository.java · extends/implements JpaRepository · public: findByName, existsByName, findAllByOrderByNameDesc, findByNameContainingIgnoreCase
- `IndexedUserRepository` repository/IndexedUserRepository.java · public: findBrandIdsByNames, findCategoryIdsByNames, findCollectionIdsByNames, searchProductsByNames · 주입: JPAQueryFactory
- `InterestQueryDslRepository` repository/InterestQueryDslRepository.java · public: findUserInterestProducts · 주입: JPAQueryFactory
- `InterestRepository` (interface) repository/InterestRepository.java · extends/implements JpaRepository · public: existsByUserAndProductColor, findByUserAndProductColor, findAllByUserId
- `UserColorRepository` (interface) repository/UserColorRepository.java · extends/implements JpaRepository · public: findByProductId
- `UserColorViewLogRepository` (interface) repository/UserColorViewLogRepository.java · extends/implements JpaRepository
- `UserDetailImageRepository` (interface) repository/UserDetailImageRepository.java · extends/implements JpaRepository · public: existsByProductColorId, findByProductColorId, findAllByProductColorId
- `UserImageRepository` (interface) repository/UserImageRepository.java · extends/implements JpaRepository · public: existsByProductColorId, findByProductColorId, findAllByProductColorId
- `UserPriceHistoryRepository` (interface) repository/UserPriceHistoryRepository.java · extends/implements JpaRepository · public: findByProductSizeId
- `UserQueryDslRepository` repository/UserQueryDslRepository.java · public: searchProducts, countProductsByFilter, findProductDetail, searchProductsByColorIds, tradeCountQuery, styleCountQuery · 주입: JPAQueryFactory
- `UserRepository` (interface) repository/UserRepository.java · extends/implements JpaRepository · public: existsByBrand, existsByCategory, existsByCollection
- `UserSizeRepository` (interface) repository/UserSizeRepository.java · extends/implements JpaRepository · public: findByProductColorId, findByProductColorIdAndSize, findAllByProductColorId

### entity (14)

- `Brand` entity/Brand.java · extends/implements BaseTimeEntity · public: updateName
- `Category` entity/Category.java · extends/implements BaseTimeEntity · public: addSubCategory, assignParentCategory, updateName, updateParentCategory, getParentCategoryName
- `Collection` entity/Collection.java · extends/implements BaseTimeEntity · public: updateName
- `Interest` entity/Interest.java · extends/implements BaseTimeEntity · public: assignUser, unassignUser, assignProductColor, unassignProductColor
- `User` entity/User.java · extends/implements BaseTimeEntity · public: addProductColor, update, updateProduct
- `UserColor` entity/UserColor.java · extends/implements BaseTimeEntity · public: assignProduct, addThumbnailImage, addProductImage, addProductDetailImage, addProductSize, update
- `UserColorViewLog` entity/UserColorViewLog.java · public: create, addViewedAt
- `UserDetailImage` entity/UserDetailImage.java · extends/implements BaseTimeEntity · public: assignProductColor
- `UserImage` entity/UserImage.java · extends/implements BaseTimeEntity · public: assignProductColor, unassignProductColor
- `UserPriceHistory` entity/UserPriceHistory.java · extends/implements BaseTimeEntity · public: assignProductSize
- `UserSize` entity/UserSize.java · extends/implements BaseTimeEntity · public: assignProductColor, update
- `ColorType` (enum) entity/enumType/ColorType.java · public: getDisplayName
- `GenderType` (enum) entity/enumType/GenderType.java
- `SizeType` (enum) entity/enumType/SizeType.java · public: getSizes

### event (3)

- `ViewEvent` dto/kafka/ViewEvent.java
- `ViewEventConsumer` service/kafka/ViewEventConsumer.java · public: listen · 주입: UserColorRepository, UserColorViewLogRepository
- `ViewEventProducer` service/kafka/ViewEventProducer.java · public: sendViewEvent · 주입: KafkaTemplate

### config (2)

- `ViewEventKafkaConfig` service/kafka/config/ViewEventKafkaConfig.java · public: viewEventProducerFactory, viewEventKafkaTemplate, viewEventConsumerFactory, viewEventKafkaListenerContainerFactory
- `CreateSizesJobConfig` service/productSize/CreateSizesJobConfig.java · public: createSizesJob, createSizesStep, sizeItemReader, sizeItemProcessor, sizeItemWriter, skipListener · 주입: JobRepository, PlatformTransactionManager, UserColorRepository, UserSizeRepository, CategoryQueryService

### dto (25)

- `BrandRequestDto` dto/BrandRequestDto.java
- `BrandResponseDto` dto/BrandResponseDto.java
- `CategoryRequestDto` dto/CategoryRequestDto.java
- `CategoryResponseDto` dto/CategoryResponseDto.java
- `CollectionRequestDto` dto/CollectionRequestDto.java
- `CollectionResponseDto` dto/CollectionResponseDto.java
- `FilterCountResponseDto` dto/FilterCountResponseDto.java
- `FilterDataResponseDto` dto/FilterDataResponseDto.java
- `SizeCreationItem` dto/SizeCreationItem.java
- `UserColorCreateRequestDto` dto/UserColorCreateRequestDto.java
- `UserColorDetailAdminResponseDto` dto/UserColorDetailAdminResponseDto.java
- `UserColorUpdateRequestDto` dto/UserColorUpdateRequestDto.java
- `UserCreateRequestDto` dto/UserCreateRequestDto.java
- `UserCreateResponseDto` dto/UserCreateResponseDto.java
- `UserDetailAdminResponseDto` dto/UserDetailAdminResponseDto.java
- `UserDetailImageDto` dto/UserDetailImageDto.java
- `UserDetailResponseDto` dto/UserDetailResponseDto.java
- `UserImageDto` dto/UserImageDto.java
- `UserSearchByNameDto` dto/UserSearchByNameDto.java
- `UserSearchDto` dto/UserSearchDto.java
- `UserSearchResponseDto` dto/UserSearchResponseDto.java
- `UserSizeDto` dto/UserSizeDto.java
- `UserUpdateRequestDto` dto/UserUpdateRequestDto.java
- `UserUpdateResponseDto` dto/UserUpdateResponseDto.java
- `UserColorIndexDto` elasticsearch/dto/UserColorIndexDto.java

### exception (6)

- `BrandNotFoundException` exception/BrandNotFoundException.java
- `CategoryNotFoundException` exception/CategoryNotFoundException.java
- `InvalidSizeTypeException` exception/InvalidSizeTypeException.java
- `UserColorNotFoundException` exception/UserColorNotFoundException.java
- `UserErrorCode` exception/UserErrorCode.java
- `UserException` exception/UserException.java

### other (4)

- `UserColorIndexInitializer` elasticsearch/config/UserColorIndexInitializer.java · extends/implements CommandLineRunner · public: run · 주입: UserColorIndexingService, ElasticsearchOperations
- `UserColorSizeRow` elasticsearch/dto/UserColorSizeRow.java
- `UserColorIndex` elasticsearch/index/UserColorIndex.java
- `SortOption` repository/SortOption.java

### 다른 도메인 참조 (import·주입 기준)

- **faq**: `BidStatus` ← UserQueryDslRepository; `FaqBidRepository` ← UserColorSearchService, UserQueryService
- **product**: `Gender` ← UserColorViewLog, UserQueryController, ViewEvent, ViewEventProducer; `Product` ← Interest, InterestCommandService, InterestRepository; `ProductQueryService` ← BrandController, CategoryController, CollectionController, InterestCommandService, UserColorCommandController, UserCommandController, UserQueryController, UserQueryControllerForAdmin
- **sale**: `SaleRepository` ← UserColorSearchService, UserQueryService

### 이 도메인을 참조하는 다른 도메인

- **event**: Brand, BrandQueryService
- **faq**: UserSize, UserSizeQueryService
- **global**: BrandRepository, CategoryRepository, ColorType, GenderType, SizeType, UserColorRepository, UserImageRepository, UserRepository, UserSizeRepository
- **product**: Interest
- **style**: UserSize, UserSizeQueryService

## 4. JPA 엔티티 연관

- `Brand` @Entity @Table(name = "brand", indexes = { @Index(name = "idx_brand_name", columnList = "name") }) · extends BaseTimeEntity
- `Category` @Entity @Table(name = "category", indexes = { @Index(name = "idx_category_name", columnList = "name"), @Index(name = "idx_category_parent", columnList = "parent_category_id") }) · extends BaseTimeEntity
  - parentCategory: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "parent_category_id") → `Category`
  - subCategories: @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL, orphanRemoval = true) → `Category`
- `Collection` @Entity @Table(name = "collection", indexes = { @Index(name = "idx_collection_name", columnList = "name") }) · extends BaseTimeEntity
- `Interest` @Entity · extends BaseTimeEntity
  - user: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") → `Product` [product]
  - productColor: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_color_id") → `UserColor`
- `User` @Entity @Table(name = "product", indexes = { @Index(name = "idx_product_brand", columnList = "brand_id"), @Index(name = "idx_product_category", columnList = "category_id"), @Index(name = "idx_product_collection", columnList = "collection_id"), @Index(name = "idx_product_gender", columnList = "gender"), @Index(name = "idx_product_price", columnList = "release_price"), @Index(name = "idx_product_date", columnList = "release_date"), @Index(name = "idx_product_brand_category", columnList = "brand_id, category_id"), @Index(name = "idx_product_name", columnList = "name"), @Index(name = "idx_product_english_name", columnList = "english_name") }) · extends BaseTimeEntity
  - brand: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "brand_id") → `Brand`
  - category: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "category_id") → `Category`
  - collection: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "collection_id") → `Collection`
  - colors: @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true) → `UserColor`
- `UserColor` @Entity @Table(name = "product_color", indexes = { @Index(name = "idx_product_color_name", columnList = "color_name"), @Index(name = "idx_product_color_product", columnList = "product_id") }) · extends BaseTimeEntity
  - product: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id") → `User`
  - thumbnailImage: @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true) @JoinColumn(name = "thumbnail_image_id", nullable = true) → `UserImage`
  - sizes: @OneToMany(mappedBy = "productColor", cascade = CascadeType.ALL, orphanRemoval = true) → `UserSize`
  - productImages: @OneToMany(mappedBy = "productColor", cascade = CascadeType.ALL, orphanRemoval = true) → `UserImage`
  - productDetailImages: @OneToMany(mappedBy = "productColor", cascade = CascadeType.ALL, orphanRemoval = true) → `UserDetailImage`
  - interests: @OneToMany(mappedBy = "productColor", cascade = CascadeType.ALL, orphanRemoval = true) → `Interest`
- `UserColorViewLog` @Entity
  - productColor: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_color_id") → `UserColor`
- `UserDetailImage` @Entity · extends BaseTimeEntity
  - productColor: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_color_id", nullable = false) → `UserColor`
- `UserImage` @Entity · extends BaseTimeEntity
  - productColor: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_color_id", nullable = false) → `UserColor`
- `UserPriceHistory` @Entity · extends BaseTimeEntity
  - productSize: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_size_id") → `UserSize`
- `UserSize` @Entity @Table(name = "product_size", indexes = { @Index(name = "idx_product_size_color", columnList = "product_color_id"), @Index(name = "idx_product_size_purchase_price", columnList = "purchase_price") }) · extends BaseTimeEntity
  - productColor: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_color_id") → `UserColor`

다른 도메인 엔티티에서 들어오는 연관:

- `Event.brand` @ManyToOne → `Brand` [event]
- `FaqBid.productSize` @ManyToOne → `UserSize` [faq]
- `FaqItem.productSize` @ManyToOne → `UserSize` [faq]
- `Product.interests` @OneToMany → `Interest` [product]
- `Style.productSize` @ManyToOne → `UserSize` [style]
- `StyleBid.productSize` @ManyToOne → `UserSize` [style]

## 5. 이벤트·메시징

- 발행 kafka `view-log-topic`: `ViewEventProducer#sendViewEvent` → `ViewEventConsumer#listen` [user]
- 구독 kafka `view-log-topic`: `ViewEventConsumer#listen` (groupId=my-group containerFactory=viewEventKafkaListenerContainerFactory) ← `ViewEventProducer#sendViewEvent` [user]

## 6. AOP

(없음)

