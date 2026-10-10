# domain/product 내비게이션 맵

> 기준 경로: `src/main/java/com/fream/back/domain/product/` (이하 `product/`로 축약하지 않고 표 안에서는 상대경로 사용 시 `domain/product/` 생략 가능)
> 파일 106개 (Java, 약 10.4k LOC). 모든 내용은 코드에서 직접 확인한 사실만 기재.

## 1. 개요

상품 카탈로그 도메인. `Product`(모델) → `ProductColor`(색상별 상세/판매 단위, 실제 검색·관심·조회수의 단위) → `ProductSize`(사이즈별 구매가/판매가/재고)의 3단 계층과, 부속인 `Brand`/`Category`(2단 계층: 메인/서브)/`Collection`, 이미지(`ProductImage` 썸네일·일반, `ProductDetailImage`), 관심상품 `Interest`(User↔ProductColor), 조회 로그 `ProductColorViewLog`(Kafka 경유 저장), `ProductPriceHistory`(엔티티만 존재, 미사용)를 관리한다. 검색은 3가지 경로: ① QueryDSL(`/products/query`), ② 이름 기반 QueryDSL(`/products/indexed/search`), ③ Elasticsearch `product-colors` 인덱스(`/es/products`). order/sale 도메인은 `ProductSize`를 FK로 참조하는 핵심 소비자이다.

## 2. 패키지/파일 구조

| 디렉터리 | 주요 파일 |
|---|---|
| `src/main/java/com/fream/back/domain/product/controller/` | `BrandController`, `CategoryController`, `CollectionController` |
| `.../product/controller/command/` | `ProductCommandController`, `ProductColorCommandController`, `InterestCommandController` |
| `.../product/controller/query/` | `ProductQueryController`(검색·상세·이미지·필터), `ProductQueryControllerForAdmin`, `IndexedProductQueryController`, `InterestQueryController` |
| `.../product/elasticsearch/config/` | `ProductColorIndexInitializer` (CommandLineRunner, 인덱스 재생성+전체 색인) |
| `.../product/elasticsearch/controller/` | `ProductColorSearchController` (`/es/products`) |
| `.../product/elasticsearch/index/` | `ProductColorIndex` (`@Document(indexName="product-colors")`) |
| `.../product/elasticsearch/repository/` | `ProductColorEsRepository`(ElasticsearchRepository), `ProductColorIndexQueryRepository`(색인용 QueryDSL) |
| `.../product/elasticsearch/service/` | `ProductColorIndexingService`, `ProductColorSearchService` |
| `.../product/elasticsearch/dto/` | `ProductColorIndexDto`, `ProductColorSizeRow` |
| `.../product/entity/` | `Product`, `ProductColor`, `ProductSize`, `ProductImage`, `ProductDetailImage`, `Brand`, `Category`, `Collection`, `Interest`, `ProductColorViewLog`, `ProductPriceHistory` |
| `.../product/entity/enumType/` | `ColorType`, `GenderType`, `SizeType` |
| `.../product/dto/` | 요청/응답 DTO (`ProductSearchDto`, `ProductSearchByNameDto`, `ProductSearchResponseDto`, `ProductDetailResponseDto`, `ProductCreate/UpdateRequestDto`, `ProductColorCreate/UpdateRequestDto`, `FilterDataResponseDto`, `SizeCreationItem` 등) |
| `.../product/dto/kafka/` | `ViewEvent` |
| `.../product/exception/` | `ProductErrorCode`, `ProductException`, `BrandNotFoundException`, `CategoryNotFoundException`, `InvalidSizeTypeException`, `ProductColorNotFoundException` |
| `.../product/repository/` | JPA 리포지토리 11개 + `ProductQueryDslRepository`, `IndexedProductRepository`, `InterestQueryDslRepository`(QueryDSL 클래스) + `SortOption`(정렬 DTO) |
| `.../product/service/brand/` | `BrandCommandService`, `BrandQueryService`, `BrandEntityService` |
| `.../product/service/category/` | `CategoryCommandService`, `CategoryQueryService`, `CategoryEntityService` |
| `.../product/service/collection/` | `CollectionCommandService`, `CollectionQueryService` |
| `.../product/service/filter/` | `FilterService` |
| `.../product/service/interest/` | `InterestCommandService`, `InterestQueryService` |
| `.../product/service/kafka/` (+`config/`) | `ViewEventProducer`, `ViewEventConsumer`, `ViewEventKafkaConfig` |
| `.../product/service/product/` | `ProductCommandService`, `ProductQueryService`, `ProductEntityService`, `IndexedProductQueryService` |
| `.../product/service/productColor/` | `ProductColorCommandService`, `ProductColorQueryService` |
| `.../product/service/productSize/` | `ProductSizeCommandService`, `ProductSizeQueryService`, `CreateSizesJobConfig`(Spring Batch, 미사용) |
| `.../product/service/productImage/`, `productDetailImage/` | 각 Command/Query 서비스 |

## 3. 엔티티

모든 엔티티(ViewLog 제외)는 `global.entity.BaseTimeEntity` 상속. PK는 모두 `IDENTITY`.

| 엔티티 (테이블) | 주요 필드 | 연관관계 |
|---|---|---|
| `Product` (`product`) | name, englishName, releasePrice(int), modelNumber, releaseDate(**String** "YYYY-MM-DD"), gender(`GenderType`, STRING) | `@ManyToOne` Brand(`brand_id`), Category(`category_id`, 서브카테고리), Collection(`collection_id`, nullable) / `@OneToMany(mappedBy="product", cascade ALL, orphanRemoval)` colors: List<ProductColor> |
| `ProductColor` (`product_color`) | colorName, content(`@Lob`, HTML) | `@ManyToOne` Product / `@OneToOne(cascade ALL, orphanRemoval)` thumbnailImage: ProductImage (`thumbnail_image_id`) / `@OneToMany(cascade ALL, orphanRemoval)` sizes, productImages, productDetailImages, interests |
| `ProductSize` (`product_size`) | sizeType(`SizeType`), size(String), purchasePrice(`purchase_price`, 구매가), salePrice(판매가), quantity | `@ManyToOne` ProductColor. **타 도메인 역참조**: `order.entity.OrderBid.productSize`, `order.entity.OrderItem.productSize`, `sale.entity.Sale.productSize`, `sale.entity.SaleBid.productSize` |
| `ProductImage` | imageUrl(파일명만 저장, 예 `thumbnail_<uuid>.jpg`) | `@ManyToOne` ProductColor (`product_color_id`, nullable=false). 썸네일도 `addThumbnailImage`에서 `assignProductColor` 호출 → 썸네일도 product_color_id를 가짐 |
| `ProductDetailImage` | imageUrl | `@ManyToOne` ProductColor (nullable=false) |
| `Brand` (`brand`) | name | 역참조: `event.entity.Event.brand`(@ManyToOne) |
| `Category` (`category`) | name | 자기참조 `parentCategory`(`parent_category_id`) / `subCategories`(cascade ALL, orphanRemoval). parent=null이면 메인 카테고리 |
| `Collection` (`collection`) | name | - |
| `Interest` | - | `@ManyToOne` **`user.entity.User`**(`user_id`), ProductColor(`product_color_id`). `User.interests` 리스트와 양방향(assignUser/unassignUser) |
| `ProductColorViewLog` | viewedAt, userEmail, userAge, userGender(**`user.entity.Gender`**) | `@ManyToOne` ProductColor. BaseTimeEntity 미상속 |
| `ProductPriceHistory` | price, priceType(String) | `@ManyToOne` ProductSize. **엔티티·리포지토리 외 사용처 없음** |

Enum (`entity/enumType/`):
- `GenderType`: MALE, FEMALE, KIDS, UNISEX
- `SizeType`(허용 사이즈 배열 보유): CLOTHING(XXS~XXXL, 28~36), SHOES("70","80","90","110"~"330" 5단위), ACCESSORIES("ONE_SIZE")
- `ColorType`(displayName): BLACK(블랙), GREY, WHITE, IVORY, BEIGE, BROWN, KHAKI, GREEN, LIGHT_GREEN, MINT, NAVY, BLUE, SKY_BLUE, PURPLE, PINK, RED, ORANGE, YELLOW, SILVER, GOLD, MIX(믹스). **엔티티 필드로 쓰이지 않음** — `ProductColor.colorName`은 String이며 생성 시 검증용으로만 사용.

## 4. API 엔드포인트

인증: `global/config/security/SecurityConfig.java`의 whitelist에 product 관련 경로가 없고 `anyRequest().authenticated()` → **모든 product 엔드포인트는 인증 필요**(SecurityConfig 기준). "ADMIN" 표시는 컨트롤러에서 `userQueryService.checkAdminRole(email)`을 호출하는 경우.

### Product / ProductColor
| Method | Path | Controller#method | Service#method | 권한 |
|---|---|---|---|---|
| POST | `/products/command` | `ProductCommandController#createProduct` (JSON) | `ProductCommandService#createProduct` | ADMIN |
| PUT | `/products/command/{productId}` | `#updateProduct` | `ProductCommandService#updateProduct` | ADMIN |
| DELETE | `/products/command/{productId}` | `#deleteProduct` | `ProductCommandService#deleteProduct` | ADMIN |
| POST | `/product-colors/{productId}` | `ProductColorCommandController#createProductColor` (multipart: `requestDto`, `thumbnailImage`(필수), `images`, `detailImages`) | `ProductColorCommandService#createProductColor` + Nginx purge | ADMIN |
| PUT | `/product-colors/{productColorId}` | `#updateProductColor` (multipart: `requestDto`, `thumbnailImage`, `newImages`, `newDetailImages`) | `ProductColorCommandService#updateProductColor` + Nginx purge | ADMIN |
| DELETE | `/product-colors/{productColorId}` | `#deleteProductColor` | `ProductColorCommandService#deleteProductColor` + Nginx purge | ADMIN |
| GET | `/products/query` | `ProductQueryController#searchProducts` (`@ModelAttribute ProductSearchDto`, Pageable) | `ProductQueryService#searchProducts` | 인증 |
| GET | `/products/query/{productId}/detail?colorName=` | `#getProductDetail` | `ProductQueryService#getProductDetail` + `ViewEventProducer#sendViewEvent` | 인증 |
| GET | `/products/query/{productId}/images?imageName=` | `#getProductImage` | (직접 파일 읽기 `/home/ubuntu/fream/product/{productId}/{imageName}`) | 인증 |
| GET | `/products/query/filters` | `#getFilterData` | `FilterService#getAllFilterData` | 인증 |
| POST | `/products/query/filters/count` | `#countProductsByFilter` (JSON ProductSearchDto) | `ProductQueryService#countProductsByFilter` | 인증 |
| GET | `/products/indexed/search` | `IndexedProductQueryController#searchProductsByNames` (`ProductSearchByNameDto`) | `IndexedProductQueryService#searchProductsByNames` | 인증 |
| GET | `/admin/products/query/{productId}/detail` | `ProductQueryControllerForAdmin#getProductDetailForAdmin` | `ProductEntityService#findById` → `ProductDetailAdminResponseDto.fromEntity` | ADMIN |
| GET | `/admin/products/query/colors/{colorId}` | `#getProductColorDetail` | `ProductColorQueryService#findById` | ADMIN |
| GET | `/admin/products/query/{productId}/colors` | `#getProductColors` | `ProductEntityService#findById` → `product.getColors()` | ADMIN |

### Elasticsearch (`ProductColorSearchController`, `/es/products`)
| Method | Path | Service#method | 비고 |
|---|---|---|---|
| GET | `/es/products` | `ProductColorSearchService#searchToDto` | sortOption null이면 `interestCount desc` 기본값 |
| GET | `/es/products/autocomplete?q=` | `#autocomplete(q, 10)` | q 공백이면 빈 리스트 |
| GET | `/es/products/raw-indices` | `#getAllIndexedColors` | 디버깅용 |
| GET | `/es/products/raw-indices/category/{categoryId}` | `#getIndexedColorsByCategory` | 디버깅용 |
| GET | `/es/products/index-stats` | `#getIndexStats` | 디버깅용, sneakersId=5L 하드코딩 |

### Brand / Category / Collection
| Method | Path | Controller#method → Service#method | 권한 |
|---|---|---|---|
| POST | `/brands` | `BrandController#createBrand` → `BrandCommandService#createBrand` | ADMIN |
| PUT | `/brands/{brandId}` | `#updateBrand` → `BrandCommandService#updateBrand` | ADMIN |
| DELETE | `/brands/{brandName}` (**이름** 기준) | `#deleteBrand` → `BrandCommandService#deleteBrand` | ADMIN |
| GET | `/brands`, `/brands/page`, `/brands/search?keyword=`, `/brands/{brandId:[0-9]+}`, `/brands/name/{brandName}` | `BrandQueryService#findAllBrands / findBrandsPaging / searchBrandsByName / findBrandById / findByName` | 인증 |
| POST | `/categories` | `CategoryController#createCategory` → `CategoryCommandService#createCategory` (subCategoryName null이면 메인 생성) | ADMIN |
| PUT | `/categories/{categoryId}` | `#updateCategory` → `CategoryCommandService#updateCategory` | ADMIN |
| DELETE | `/categories/{categoryId}` | `#deleteCategory` → `CategoryCommandService#deleteCategory` | ADMIN |
| GET | `/categories/main` | `CategoryQueryService#findAllMainCategories` | 인증 |
| GET | `/categories/sub/{mainCategoryName}` | `CategoryQueryService#findSubCategoriesByMainCategory` | 인증 |
| POST/PUT/DELETE | `/collections`, `/collections/{collectionId}`, `/collections/{collectionName}`(DELETE는 이름) | `CollectionController` → `CollectionCommandService#create/update/deleteCollection` | ADMIN |
| GET | `/collections`, `/page`, `/search`, `/{collectionId:[0-9]+}`, `/name/{collectionName}` | `CollectionQueryService` | 인증 |

Brand/Collection 컨트롤러는 `ResponseDto<T>` 래퍼로 응답, Category·Product 컨트롤러는 DTO 직접 반환.

### Interest
| Method | Path | Controller#method → Service#method | 비고 |
|---|---|---|---|
| POST | `/interests/{productColorId}/toggle` | `InterestCommandController#toggleInterest` → `InterestCommandService#toggleInterest(email, colorId)` | SecurityContext 이메일 사용 |
| GET | `/interests/{userId}?field=&order=` | `InterestQueryController#getUserInterestProducts` → `InterestQueryService#findUserInterestProducts` → `InterestQueryDslRepository#findUserInterestProducts` | path의 userId 그대로 사용(본인 확인 코드 없음) |

## 5. 서비스 계층

| 서비스 | 주요 메서드 | 의존 |
|---|---|---|
| `ProductCommandService` | createProduct, updateProduct, deleteProduct | ProductRepository, BrandEntityService, CategoryEntityService(`findSubCategoryByName(sub, main)`), CollectionCommandService(`createOrGetCollection` — 없으면 자동 생성), ProductColorCommandService(삭제 시 색상별 위임) |
| `ProductQueryService` (readOnly) | searchProducts, getProductDetail, countProductsByFilter, searchProductsByColorIds(호출처 없음) | ProductQueryDslRepository, **style.StyleRepository#styleCountByColorIds**, **order.OrderBidRepository#tradeCountByColorIds** |
| `IndexedProductQueryService` | searchProductsByNames | IndexedProductRepository, ProductQueryDslRepository#styleCountQuery/tradeCountQuery |
| `ProductEntityService` | findById (없으면 PRODUCT_NOT_FOUND) | ProductRepository |
| `ProductColorCommandService` | createProductColor, updateProductColor, deleteProductColor, validateColorType(private) | ProductColorRepository, ProductSizeCommandService/QueryService, FileUtils, ProductEntityService, InterestCommandService, ProductImage/DetailImage Command·Query 서비스. `NginxCachePurgeUtil`, `JobLauncher`, `createSizesJob`은 주입만 되고 미사용 |
| `ProductColorQueryService` | findById, findAllByProductId | ProductColorRepository |
| `ProductSizeCommandService` | createProductSizes(color, categoryId, sizes, releasePrice), deleteProductSize, updateProductSize(**호출처 없음**), deleteAllSizesByProductColor | ProductSizeRepository, CategoryQueryService#findRootCategoryById |
| `ProductSizeQueryService` | findByColorIdAndSize, findSizesByColorId, findById, findAllByProductColorId | ProductSizeRepository. `findById`는 order/sale에서 호출 |
| `ProductImage*/ProductDetailImage*Service` | create*, delete*, findAllByProductColorId, existsByProductColorId | 각 Repository |
| `BrandCommandService` / `BrandQueryService` / `BrandEntityService` | CRUD; delete 시 `ProductRepository.existsByBrand` → BRAND_IN_USE | BrandRepository, ProductRepository. `BrandQueryService#findById`는 event 도메인에서 호출 |
| `CategoryCommandService` | create(main/sub), update, delete(하위카테고리 존재 또는 상품 존재 시 CATEGORY_IN_USE) | CategoryRepository, ProductRepository |
| `CategoryQueryService` | findAllMainCategories, findSubCategoriesByMainCategory, findRootCategory, findRootCategoryById, findSubCategoriesByParentId/Name | CategoryRepository |
| `CategoryEntityService` | findMainCategoryByName, findSubCategoryByName | CategoryRepository |
| `CollectionCommandService` / `CollectionQueryService` | CRUD + createOrGetCollection | CollectionRepository, ProductRepository |
| `FilterService` | getAllFilterData: SizeType/GenderType/ColorType 정적 목록 + 하드코딩 할인/가격대 + 카테고리(메인 "Clothing"의 서브들 + 메인 "Shoes", 각 하위 포함) + 전체 브랜드/컬렉션 | Brand/Category/CollectionQueryService |
| `InterestCommandService` | toggleInterest(있으면 삭제, 없으면 생성), deleteAllInterestsByProductColor | InterestRepository, **user.UserQueryService#findByEmail**, ProductColorQueryService |
| `InterestQueryService` | findUserInterestProducts | InterestQueryDslRepository |
| `ViewEventProducer` / `ViewEventConsumer` | 7절 참고 | KafkaTemplate / ProductColorRepository, ProductColorViewLogRepository |
| `ProductColorIndexingService` / `ProductColorSearchService` | 7절 참고 | |

## 6. Repositories

- JPA 파생 쿼리: `BrandRepository`(findByName, existsByName, findAllByOrderByNameDesc, findByNameContainingIgnoreCase), `CollectionRepository`(동일 패턴), `ProductRepository`(existsByBrand/Category/Collection), `ProductColorRepository.findByProductId`, `ProductSizeRepository`(findByProductColorId, findAllByProductColorId, findByProductColorIdAndSize), `InterestRepository`(existsByUserAndProductColor, findByUserAndProductColor, findAllByUserId), 이미지 리포지토리(existsByProductColorId, findAllByProductColorId), `ProductPriceHistoryRepository.findByProductSizeId`.
- `CategoryRepository` `@Query`: `findWithParentById`(`LEFT JOIN FETCH c.parentCategory`), `findByParentCategoryName`(`c.parentCategory.name = :parentName`). 메인/서브 목록은 이름 **내림차순**(`OrderByNameDesc`).
- `ProductQueryDslRepository` (`repository/ProductQueryDslRepository.java`):
  - `searchProducts(...)`: Product ⟕ colors ⟕ brand ⟕ thumbnailImage ⟕ sizes ⟕ interests, `groupBy(product.id, productColor.id, imageUrl)`. 결과 행 단위 = **ProductColor**. price = `min(purchasePrice)`, interestCount = `interest.count()`.
  - 키워드: 공백/하이픈/괄호로 분리 → 각 토큰마다 `name|englishName|colorName lower LIKE %k%` 또는 `size LIKE` → 토큰들 **AND** 결합 (263~290행).
  - 필터: categoryIds/genders/brandIds/collectionIds `IN`, colors → `colorName IN`, sizes → `size IN`, 가격 → `purchasePrice >= min AND <= max`(사이즈 행 기준).
  - total: `product.id.countDistinct()` (상품 수, 결과 행은 색상 수 → 페이지 total과 content 단위 불일치).
  - `findProductDetail(productId, colorName)`, `searchProductsByColorIds`, `countProductsByFilter`, `tradeCountQuery`(OrderBid status=COMPLETED, ProductColor별), `styleCountQuery`(StyleOrderItem→OrderItem→ProductSize).
- `IndexedProductRepository`: brand/category/collection **이름 → ID** 변환 후 ID 기반 검색(`searchProductsByIdsOptimized`), 정렬은 `applySortingOptimized`(switch, toLowerCase).
- `InterestQueryDslRepository#findUserInterestProducts(userId, sortOption, pageable)`.
- **정렬 옵션** (`SortOption{field, order}`): 공통 field = `price`(min purchasePrice), `releaseDate`, `interestCount`(`productColor.interests.size().sum()`), order `asc`/그 외는 desc.
  - ProductQueryDsl#searchProducts: sortOption null → `product.id asc`; 알 수 없는 field면 정렬 미적용.
  - Indexed: 알 수 없는 field → `product.id asc`.
  - Interest: field/order 하나라도 null → `product.id asc`; price는 `productSize.purchasePrice`(집계 아님).
  - ES: `releaseDate`, `interestCount`, `price`→`minPrice`, 그 외 → `productId`; order가 `desc`일 때만 Desc.

## 7. 외부 연동/비동기

### Kafka (조회 로그)
- 토픽: `view-log-topic` (상수, `service/kafka/ViewEventProducer.java:16`).
- Producer: `ViewEventProducer#sendViewEvent(colorId, email, age, gender)` → `ViewEvent{productColorId, email, age, gender(user.Gender), viewedAt=now}`. 호출처: `ProductQueryController#getProductDetail` 단 한 곳.
- Consumer: `ViewEventConsumer#listen` `@KafkaListener(topics="view-log-topic", groupId="my-group", containerFactory="viewEventKafkaListenerContainerFactory")` → ProductColor 조회 → `ProductColorViewLog` 저장.
- 설정: `service/kafka/config/ViewEventKafkaConfig.java` (JsonSerializer/JsonDeserializer, `${spring.kafka.bootstrap-servers}`).
- `ProductColorViewLog`를 읽는 코드는 도메인 내외 어디에도 없음(저장만).

### Elasticsearch
- 인덱스: `product-colors`, 문서 = `elasticsearch/index/ProductColorIndex` (@Id `colorId`). 필드: productId, productName, productEnglishName, brandName, categoryName, collectionName, colorName(Text), gender(Keyword), thumbnailUrl, brandId, categoryId, collectionId, releaseDate(Date yyyy-MM-dd), releasePrice, minPrice/maxPrice(사이즈 purchasePrice min/max), interestCount, sizes(Keyword 배열).
- 생성/색인 시점: `elasticsearch/config/ProductColorIndexInitializer#run` (CommandLineRunner, `@Order(2)`; `global/config/DataInitializer`는 `@Order(1)`). **앱 기동 시마다 기존 인덱스 삭제(30행) → nori 토크나이저(`analysis/userdict_ko.txt`), synonym_graph(`analysis/synonyms.txt`), edge_ngram(2~4) 분석기 설정으로 재생성 → `indexAllColors()`(188행)**. 사전 파일 원본: `docker/elasticsearch/synonyms.txt`, `userdict_ko.txt`.
- `ProductColorIndexingService`: `indexAllColors()`(ProductColorIndexQueryRepository로 DTO+사이즈 행 조회 후 saveAll), `indexColorById(colorId)`, `deleteColorFromIndex(colorId)`. **indexColorById/deleteColorFromIndex는 호출처 없음** → 상품/색상/사이즈/관심 변경이 ES에 반영되는 것은 재기동 시뿐.
- 검색: `ProductColorSearchService#search` — keyword: Phrase multi_match(productName^3, productEnglishName^3, brandName^2, ...; boost 2) + fuzzy multi_match(AUTO) + 다중 토큰이면 토큰별 must bool → should 결합. 필터: categoryId/brandId/collectionId term, gender match+term, colorName match(+fuzzy), sizes term, 가격 range. 조건 없으면 match_all. `searchToDto`는 결과에 `styleRepository.styleCountByColorIds`, `orderBidRepository.tradeCountByColorIds`를 붙임.
- autocomplete: best_fields multi_match(fuzzy) must + prefix(productName/brandName/colorName) should, `interestCount desc`, `"브랜드 - 상품명 (색상)"` 문자열 반환.

### 파일 저장 / 이미지
- `global/utils/FileUtils` BASE_DIR = `/home/ubuntu/fream`. 색상 이미지 디렉터리 `product/{productId}` (색상 ID 아님). 접두사: `thumbnail_`, `ProductImage_`, `ProductDetailImage_` + UUID + 확장자. DB에는 **파일명만** 저장. image/·video/ content-type만 허용.
- 서빙: `GET /products/query/{productId}/images?imageName=` (ProductQueryController 172~). 

### Nginx 캐시 purge
- `global/utils/NginxCachePurgeUtil`: `purgeProductCache()` → `PURGE http://nginx:80/purge_products/api/products/`, `purgeEsCache()` → `/purge_es/api/es/products/`.
- 호출: **`ProductColorCommandController`의 create/update/delete 성공 후에만**(76~77, 117~118, 148~149행). `ProductCommandController`, Brand/Category/Collection, Interest 토글에서는 purge 호출 없음.
- Nginx 설정: `docker/dev/nginx/nginx.conf`, `docker/prod/nginx/nginx.conf` (`location /api/products`, `/api/es/products`에 proxy_cache, `/purge_*`에 proxy_cache_purge).

### Spring Batch
- `service/productSize/CreateSizesJobConfig`: `createSizesJob` / `createSizesStep`(chunk 10, skip InvalidSizeType/ProductColorNotFound/CategoryNotFound/IllegalArgument, skipLimit 100), jobParameters `productColorId, categoryId, requestedSizes(CSV), releasePrice`. **JobLauncher로 실행하는 코드 없음**(실제 사이즈 생성은 `ProductSizeCommandService#createProductSizes` 동기 처리).

### Redis / 캐시 / 스케줄러
- product 도메인 내 `@Cacheable`, `RedisTemplate`, `@Scheduled` 사용 없음.

## 8. 상품 생성/수정 흐름

1. **Product 생성** `POST /products/command` → `ProductCommandService#createProduct`
   - `BrandEntityService#findByName(brandName)` (없으면 BRAND_NOT_FOUND, 자동생성 안 함)
   - `CategoryEntityService#findSubCategoryByName(categoryName, mainCategoryName)` (메인/서브 이름 쌍 필수)
   - `CollectionCommandService#createOrGetCollection(collectionName)` (null 아니면 없을 때 자동 생성)
   - `productRepository.save` → `ProductCreateResponseDto`. 색상/사이즈/이미지 없음, ES 색인 없음.
2. **Color+이미지+사이즈 생성** `POST /product-colors/{productId}` (multipart) → `ProductColorCommandService#createProductColor`
   - `validateColorType(colorName)`: `ColorType.name()` 대소문자무시 또는 `displayName` 일치 필요 (예: "BLACK"/"black"/"블랙"). 저장값은 요청 문자열 그대로.
   - `FileUtils#saveFile("product/{productId}", prefix, file)`로 썸네일/이미지/상세이미지 저장 → `addThumbnailImage`/`addProductImage`/`addProductDetailImage` → `productColorRepository.save` (cascade로 이미지 저장)
   - `ProductSizeCommandService#createProductSizes(savedColor, product.category.id, sizes, product.releasePrice)`:
     - `CategoryQueryService#findRootCategoryById` → 루트 카테고리명 `toUpperCase()`가 `CLOTHING`/`SHOES`/`ACCESSORIES` 중 하나여야 SizeType 결정(아니면 INVALID_SIZE_TYPE)
     - 기존 사이즈 제외 → `SizeType.getSizes()`에 포함된 것만 저장, **purchasePrice = salePrice = releasePrice, quantity = 0**. 무효 사이즈가 하나라도 있으면 (유효분 저장 후) INVALID_SIZE_TYPE 예외 → 트랜잭션 롤백.
   - 컨트롤러가 `nginxCachePurgeUtil.purgeProductCache()` / `purgeEsCache()` 호출.
   - **ES 색인 호출 없음** (재기동 시 `indexAllColors`로만 반영).
3. **Product 수정** `PUT /products/command/{id}` → `Product#updateProduct`(null 아닌 필드만 반영). 카테고리는 categoryName+mainCategoryName 둘 다 있어야 변경. 응답의 gender는 request 값(엔티티 값 아님).
4. **Color 수정** `PUT /product-colors/{id}` → `updateProductColor`: 색상명이 바뀌면 재검증 → 썸네일 교체(기존 파일 삭제) → `existingImages`/`existingDetailImages`에 없는 이미지 파일·엔티티 제거(리스트가 null이면 손대지 않음) → 신규 이미지 추가 → `sizes` 주어지면 빠진 사이즈 `deleteProductSize`, 신규 사이즈 `createProductSizes` → `productColor.update(colorName, content)`.
5. **삭제** `DELETE /products/command/{id}` → 색상마다 `deleteProductColor` → `InterestCommandService#deleteAllInterestsByProductColor` → `ProductSizeCommandService#deleteAllSizesByProductColor` → 썸네일/이미지/상세이미지 파일+엔티티 삭제 → color 삭제 → product 삭제. 사이즈를 참조하는 OrderBid/SaleBid 등의 존재 여부 검사 코드는 없음.

## 9. 상품 검색 흐름

| 경로 | 흐름 |
|---|---|
| QueryDSL (ID 필터) | `GET /products/query` → `ProductSearchDto#validate`(min>max 금지) → `ProductQueryService#searchProducts` → `ProductQueryDslRepository#searchProducts` → colorIds로 `StyleRepository#styleCountByColorIds`, `OrderBidRepository#tradeCountByColorIds` 조회해 DTO에 styleCount/tradeCount 세팅 → `commonDto.PageDto` |
| QueryDSL (이름 필터) | `GET /products/indexed/search` → `ProductSearchByNameDto#validate`(min>max, 음수 금지) → `IndexedProductQueryService#searchProductsByNames` → `IndexedProductRepository#searchProductsByNames`(이름→ID 변환 → `searchProductsByIdsOptimized`) → `ProductQueryDslRepository#styleCountQuery/tradeCountQuery` |
| Elasticsearch | `GET /es/products` → (sortOption 기본 interestCount desc, GenderType→String 변환) → `ProductColorSearchService#searchToDto` → `#search`(NativeQuery, `IndexCoordinates.of("product-colors")`) → `toDto`(price = index.minPrice) → style/trade count 보강 |
| 필터 카운트 | `POST /products/query/filters/count` → `ProductQueryService#countProductsByFilter` → `ProductQueryDslRepository#countProductsByFilter` |
| 상세 | `GET /products/query/{productId}/detail?colorName=` → `ProductQueryDslRepository#findProductDetail` (기본정보+사이즈목록+다른 색상 목록) → Kafka 조회 이벤트 발행 |

응답 DTO 공통: `dto/ProductSearchResponseDto` (id=productId, name, englishName, releasePrice, thumbnailImageUrl, price, colorName, colorId, interestCount, brandName, styleCount, tradeCount).

## 10. 다른 도메인과의 의존

**product → 타 도메인**
- `user`: `User`(Interest 엔티티), `Gender`(ViewLog/ViewEvent), `UserQueryService#checkAdminRole`(관리자 컨트롤러 전부), `UserQueryService#findByEmail`(InterestCommandService).
- `order`: `OrderBidRepository#tradeCountByColorIds`(ProductQueryService, ProductColorSearchService), `QOrderBid`/`QOrderItem`/`BidStatus.COMPLETED`(ProductQueryDslRepository#tradeCountQuery/styleCountQuery).
- `style`: `StyleRepository#styleCountByColorIds`, `QStyle`/`QStyleOrderItem`.
- `global`: `FileUtils`, `NginxCachePurgeUtil`, `SecurityUtils`, `JwtAuthenticationFilter.UserInfo`, `BaseTimeEntity`, `GlobalException`, `commonDto.PageDto`, `ResponseDto`.

**타 도메인 → product**
- `order`: `OrderBid.productSize`, `OrderItem.productSize`(엔티티 FK), `OrderBidCommandService`가 `ProductSizeQueryService#findById`, `OrderCommandService#createOrderFromBid(user, ProductSize, price)`, `OrderItemCommandService#createOrderItem`, `OrderItemRepository#findByProductSize`, `OrderBidRepositoryImpl`(QProduct/QProductColor/QProductImage/QProductSize 조인).
- `sale`: `Sale.productSize`, `SaleBid.productSize`, `SaleBidCommandService`가 `ProductSizeQueryService#findById`, `SaleCommandService#createSale(..., ProductSize, ...)`, `SaleBidRepositoryImpl`(Q클래스 조인).
- `style`: `StyleRepositoryCustomImpl`(QProduct/QProductColor/QProductSize 조인, StyleOrderItem→OrderItem→ProductSize→ProductColor).
- `event`: `Event.brand`(@ManyToOne Brand), `EventCommandService`/`EventValidationAspect`/`EventBusinessRuleAspect`가 `BrandQueryService#findById`, `BrandNotFoundException`.
- `user`: `User.interests`(List<Interest>), addInterest/removeInterest.
- `global/config/DataInitializer`(CommandLineRunner @Order(1)): Brand/Category/Product/ProductColor/ProductSize/ProductImage 시드 데이터 생성. `global/exception/GlobalExceptionHandler`가 `ProductException` 처리(413행).

**ProductSize 가격 갱신 여부**: `ProductSize.update(purchasePrice, salePrice, quantity)`를 호출하는 곳은 `ProductSizeCommandService#updateProductSize`뿐이며, 이 메서드의 호출처는 코드 전체에 없음. 즉 **order/sale 입찰·체결이 ProductSize의 purchasePrice/salePrice/quantity를 갱신하지 않는다**. 가격은 생성 시 releasePrice로 고정. `ProductPriceHistory`도 기록되지 않음.

## 11. 예외

- `exception/ProductErrorCode` (implements `global.exception.ErrorCode`, 필드 code/message/status). 접두사:
  - `P`(P001 PRODUCT_NOT_FOUND 404, P002~P005), `PC`(PC001~PC005, INVALID_COLOR_TYPE=PC005), `PS`(PS001~PS005, INVALID_SIZE_TYPE=PS005), `CT`(CT001~CT006, CATEGORY_ALREADY_EXISTS=409), `BD`(BD001~BD006, BRAND_ALREADY_EXISTS=409), `CL`(CL001~CL006), `IN`(IN001 INTEREST_NOT_FOUND, IN002 INTEREST_TOGGLE_FAILED), `IMG`(IMG001~003), `FT`(FT001 FILTER_INVALID_PARAMS), `KF`(KF001/KF002, 사용처 없음).
- `ProductException extends GlobalException` (생성자: errorCode / +message / +cause).
- 하위: `BrandNotFoundException`, `CategoryNotFoundException`, `ProductColorNotFoundException`, `InvalidSizeTypeException` (모두 `extends ProductException`; 주로 CreateSizesJobConfig·event 도메인에서 사용).
- 패턴: 서비스는 `catch (ProductException) rethrow; catch (Exception) → *_FAILED/NOT_FOUND로 래핑`. 컨트롤러도 대부분 동일하게 다시 래핑 → 원래 코드가 바뀌는 경우 있음(예: 검색 중 어떤 예외든 PRODUCT_NOT_FOUND).

## 12. 작업 팁 / 주의점 (코드 사실 기반)

- **ES 인덱스 자동 동기화 없음**: `indexColorById`/`deleteColorFromIndex` 호출처 0건. 기동 시 `ProductColorIndexInitializer`가 인덱스를 **삭제 후 재생성**(개발용 주석)하고 전체 색인. 변경 반영 필요 시 이 서비스 메서드를 command 흐름에 연결해야 함.
- `ProductQueryController.java:134` — `SecurityUtils.extractEmailOrAnonymous()`는 미인증 시 `"anonymous"`를 반환하는데 비교 문자열은 `"anonymousUser"`. 또한 `SecurityUtils.extractUserInfo()`는 null을 반환할 수 있으나 137~139행에서 null 체크 없이 `userInfo.getAge()` 호출(NPE는 바깥 `catch (Exception)`에서 PRODUCT_NOT_FOUND로 변환).
- `ProductQueryDslRepository.java:388` — 상세 조회의 사이즈 목록이 `productSize.productColor.colorName.eq(colorName)`만으로 필터링(productId 조건 없음) → 같은 색상명을 가진 다른 상품의 사이즈까지 포함될 수 있음.
- `ProductQueryDslRepository#searchProducts`/Indexed/Interest 쿼리는 sizes와 interests를 동시에 left join하고 `interest.count()`(distinct 아님)를 사용 → 관심 수가 사이즈 수만큼 곱해질 수 있음. ES 색인 쿼리는 `it.id.countDistinct()` 사용(일관성 차이).
- `ProductQueryDslRepository#searchProducts`의 total은 `product.id.countDistinct()`인데 content는 색상 단위 행 → 페이지 수 계산 불일치 가능. 또한 `System.out.println` 디버그 출력 21건.
- ES 가격 필터(`ProductColorSearchService.java:384,389`): `minPrice` 파라미터 → `minPrice(필드) <= 값`, `maxPrice` 파라미터 → `maxPrice(필드) >= 값`. QueryDSL 쪽(`purchasePrice BETWEEN`)과 의미가 다름.
- `IndexedProductRepository.java:299` — `sortOption`이 non-null이고 field가 null이면 `toLowerCase()`에서 NPE. 또 이름 필터에서 일치하는 이름이 하나도 없으면 ID 리스트가 비어 해당 조건이 **무시**됨(전체 반환).
- `InterestCommandService.java:98` — `productColor.getInterests().forEach` 안에서 `interest.unassignProductColor()`가 같은 리스트에서 `remove` 수행(ConcurrentModification 위험).
- `ProductSizeCommandService#createProductSizes`는 `productSizeRepository.save`만 하고 `productColor.addProductSize`를 호출하지 않음 → 같은 트랜잭션 내 `productColor.getSizes()`에는 신규 사이즈가 반영되지 않음.
- 썸네일 `ProductImage`도 product_color_id를 가지므로 `ProductImageRepository.findAllByProductColorId` 결과에 썸네일이 포함됨(삭제 로직에서 썸네일 삭제 후 다시 목록 조회·삭제 순서 주의, `ProductColorCommandService.java:423~447`).
- `GET /products/query/{productId}/images`는 `imageName`을 정규화 없이 경로에 이어붙임(`ProductQueryController.java:182`).
- 이미지 디렉터리는 `product/{productId}` — 색상 간 파일이 한 디렉터리에 섞임. 파일명은 UUID라 충돌 없음.
- `GET /interests/{userId}`는 path userId를 그대로 신뢰(요청자 본인 확인 없음).
- Brand/Collection 삭제는 **이름**으로(`/brands/{brandName}`), Category 삭제는 ID로.
- `ProductColorSearchController`에 `ProductQueryService`가 주입되지만 사용 안 함. `ProductColorSearchService#convertGender`(private) 사용처 없음. `ProductQueryService#searchProductsByColorIds` 호출처 없음. `ProductColorCommandService`의 `nginxCachePurgeUtil`/`jobLauncher`/`createSizesJob` 미사용.
- `ProductDetailResponseDto`/검색 응답의 `thumbnailImageUrl`은 파일명만 들어 있음 → 클라이언트는 `/products/query/{productId}/images?imageName=`로 조합해야 함.
- `Product.releaseDate`는 String 컬럼, ES 매핑은 Date(`yyyy-MM-dd`) → 형식이 다르면 색인 실패 가능(색인 예외는 `indexAllColors`에서 `System.err`로만 출력되고 삼켜짐).
- 사이즈 타입 결정은 루트 카테고리 **이름**(`CLOTHING`/`SHOES`/`ACCESSORIES`, 대소문자 무시)에 의존. FilterService는 메인 카테고리명 `"Clothing"`, `"Shoes"`를 정확히 비교.
