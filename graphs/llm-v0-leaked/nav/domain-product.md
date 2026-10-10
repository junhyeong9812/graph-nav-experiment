# domain: product

## 역할 요약
- 상품 카탈로그 도메인: `Product`(상품) → `ProductColor`(색상별 SKU 묶음, 이미지/상세HTML/관심) → `ProductSize`(사이즈별 구매가/판매가/재고) 계층과 `Brand`/`Category`(2단계 계층)/`Collection` 마스터 데이터를 관리한다.
- 상품 목록 검색은 두 경로가 공존한다: **QueryDSL(DB)** `/products/query`, `/products/indexed/search`와 **Elasticsearch** `/es/products`. 상세 내용은 `nav/flow-product-search.md`.
- 상품 상세 조회 시 Kafka(`view-log-topic`)로 조회 이벤트를 발행해 `ProductColorViewLog` 테이블에 적재한다.
- `ProductSize`는 order/sale 도메인의 거래 대상(FK `product_size_id`)이다.

## 패키지/파일 맵
루트: `src/main/java/com/fream/back/domain/product/`

| 레이어 | 파일 | 용도 |
|---|---|---|
| controller | `controller/BrandController.java` | `/brands` CRUD + 페이징/검색 (쓰기는 admin 체크) |
| | `controller/CategoryController.java` | `/categories` 메인/서브 카테고리 CRUD |
| | `controller/CollectionController.java` | `/collections` CRUD + 페이징/검색 |
| controller/command | `ProductCommandController.java` | `/products/command` 상품 생성/수정/삭제 (admin) |
| | `ProductColorCommandController.java` | `/product-colors` 색상 생성/수정/삭제 (multipart, admin) + Nginx 캐시 purge |
| | `InterestCommandController.java` | `/interests/{productColorId}/toggle` 관심 토글 |
| controller/query | `ProductQueryController.java` | `/products/query` 검색, 상세(+Kafka 조회 이벤트), 이미지 바이너리, 필터 데이터/카운트 |
| | `IndexedProductQueryController.java` | `/products/indexed/search` 이름 기반 필터 검색 |
| | `ProductQueryControllerForAdmin.java` | `/admin/products/query` 관리자 상세 조회 |
| | `InterestQueryController.java` | `/interests/{userId}` 사용자 관심상품 목록 |
| dto | `dto/*.java` | 요청/응답 DTO. 검색 조건: `ProductSearchDto`, `ProductSearchByNameDto` (둘 다 `validate()` 보유) |
| dto/kafka | `dto/kafka/ViewEvent.java` | Kafka 조회 이벤트 페이로드 |
| entity | `entity/*.java` | Product, ProductColor, ProductSize, Brand, Category, Collection, Interest, ProductImage, ProductDetailImage, ProductPriceHistory, ProductColorViewLog |
| entity/enumType | `ColorType.java`, `GenderType.java`, `SizeType.java` | 색상/성별/사이즈 enum |
| exception | `ProductErrorCode.java`, `ProductException.java`, `BrandNotFoundException`, `CategoryNotFoundException`, `InvalidSizeTypeException`, `ProductColorNotFoundException` | 에러코드/예외 |
| repository | `ProductQueryDslRepository.java` | 메인 검색/상세/색상ID 검색/styleCount·tradeCount 쿼리 (QueryDSL) |
| | `IndexedProductRepository.java` | 이름→ID 변환 후 검색 (QueryDSL) |
| | `InterestQueryDslRepository.java` | 사용자 관심상품 페이징 |
| | `SortOption.java` | 정렬 옵션 DTO (`field`, `order`) — repository 패키지에 위치 |
| | 그 외 `*Repository.java` | Spring Data JPA 인터페이스 |
| service/product | `ProductCommandService`, `ProductQueryService`, `ProductEntityService`, `IndexedProductQueryService` | 상품 CUD / 검색·상세 / 엔티티 조회 / 이름 기반 검색 |
| service/productColor | `ProductColorCommandService`, `ProductColorQueryService` | 색상 CUD(파일 저장 포함) / 조회 |
| service/productSize | `ProductSizeCommandService`, `ProductSizeQueryService`, `CreateSizesJobConfig` | 사이즈 생성·삭제·수정 / 조회 / Spring Batch 잡 정의(미사용, 아래 Gotchas) |
| service/productImage, productDetailImage | `*CommandService`, `*QueryService` | 이미지 엔티티 삭제/조회 |
| service/brand, category, collection | `*CommandService`, `*QueryService`, `*EntityService` | 마스터 데이터 |
| service/interest | `InterestCommandService`, `InterestQueryService` | 관심 토글/일괄삭제, 목록 |
| service/filter | `FilterService.java` | 필터 UI용 데이터(enum, 카테고리, 브랜드, 컬렉션, 가격대 하드코딩) |
| service/kafka | `ViewEventProducer`, `ViewEventConsumer`, `config/ViewEventKafkaConfig` | 조회 이벤트 파이프라인 |
| elasticsearch | `config/ProductColorIndexInitializer`, `index/ProductColorIndex`, `repository/ProductColorEsRepository`, `repository/ProductColorIndexQueryRepository`, `service/ProductColorIndexingService`, `service/ProductColorSearchService`, `controller/ProductColorSearchController`, `dto/*` | ES 인덱스 `product-colors` 생성/색인/검색 |

## REST endpoints
모든 경로는 컨텍스트 루트 기준. `SecurityConfig.whiteListPaths()`(`src/main/java/com/fream/back/global/config/security/SecurityConfig.java`)에 product 경로가 없으므로 Security 설정상 전부 `authenticated()` 대상이다. "admin"은 컨트롤러에서 `userQueryService.checkAdminRole(email)` 호출.

| Method | Path | Controller#method | Service | 비고 |
|---|---|---|---|---|
| POST | `/products/command` | ProductCommandController#createProduct | ProductCommandService#createProduct | admin, JSON |
| PUT | `/products/command/{productId}` | #updateProduct | ProductCommandService#updateProduct | admin |
| DELETE | `/products/command/{productId}` | #deleteProduct | ProductCommandService#deleteProduct | admin |
| POST | `/product-colors/{productId}` | ProductColorCommandController#createProductColor | ProductColorCommandService#createProductColor | admin, multipart: `requestDto`, `thumbnailImage`(필수), `images`, `detailImages` |
| PUT | `/product-colors/{productColorId}` | #updateProductColor | ProductColorCommandService#updateProductColor | admin, multipart: `requestDto`, `thumbnailImage`, `newImages`, `newDetailImages` |
| DELETE | `/product-colors/{productColorId}` | #deleteProductColor | ProductColorCommandService#deleteProductColor | admin |
| GET | `/products/query` | ProductQueryController#searchProducts | ProductQueryService#searchProducts | QueryDSL 검색 (`@ModelAttribute ProductSearchDto`) |
| GET | `/products/query/{productId}/detail?colorName=` | #getProductDetail | ProductQueryService#getProductDetail + ViewEventProducer#sendViewEvent | Kafka 발행 |
| GET | `/products/query/{productId}/images?imageName=` | #getProductImage | (없음, 컨트롤러에서 직접 파일 읽기) | `/home/ubuntu/fream/product/{productId}/{imageName}` |
| GET | `/products/query/filters` | #getFilterData | FilterService#getAllFilterData | |
| POST | `/products/query/filters/count` | #countProductsByFilter | ProductQueryService#countProductsByFilter | JSON body `ProductSearchDto` |
| GET | `/products/indexed/search` | IndexedProductQueryController#searchProductsByNames | IndexedProductQueryService#searchProductsByNames | 이름 기반 필터 |
| GET | `/admin/products/query/{productId}/detail` | ProductQueryControllerForAdmin#getProductDetailForAdmin | ProductEntityService#findById | admin |
| GET | `/admin/products/query/colors/{colorId}` | #getProductColorDetail | ProductColorQueryService#findById | admin |
| GET | `/admin/products/query/{productId}/colors` | #getProductColors | ProductEntityService#findById | admin |
| GET | `/es/products` | ProductColorSearchController#esSearchProducts | ProductColorSearchService#searchToDto | ES 검색 |
| GET | `/es/products/autocomplete?q=` | #autocompleteProducts | ProductColorSearchService#autocomplete(q,10) | |
| GET | `/es/products/raw-indices` | #getRawIndices | #getAllIndexedColors | 디버깅용 |
| GET | `/es/products/raw-indices/category/{categoryId}` | #getRawIndicesByCategory | #getIndexedColorsByCategory | 디버깅용 |
| GET | `/es/products/index-stats` | #getIndexStats | #getIndexStats | 디버깅용 |
| POST | `/interests/{productColorId}/toggle` | InterestCommandController#toggleInterest | InterestCommandService#toggleInterest | 로그인 이메일 사용 |
| GET | `/interests/{userId}?field=&order=` | InterestQueryController#getUserInterestProducts | InterestQueryService#findUserInterestProducts | |
| POST | `/brands` | BrandController#createBrand | BrandCommandService#createBrand | admin |
| PUT | `/brands/{brandId}` | #updateBrand | BrandCommandService#updateBrand | admin |
| DELETE | `/brands/{brandName}` | #deleteBrand | BrandCommandService#deleteBrand | admin, **이름**으로 삭제 |
| GET | `/brands` | #getAllBrands | BrandQueryService#findAllBrands | |
| GET | `/brands/page` | #getBrandsPaging | #findBrandsPaging | |
| GET | `/brands/search?keyword=` | #searchBrands | #searchBrandsByName | |
| GET | `/brands/{brandId:[0-9]+}` | #getBrandById | #findBrandById | |
| GET | `/brands/name/{brandName}` | #getBrandByName | #findByName | |
| POST | `/categories` | CategoryController#createCategory | CategoryCommandService#createCategory | admin |
| PUT | `/categories/{categoryId}` | #updateCategory | #updateCategory | admin |
| DELETE | `/categories/{categoryId}` | #deleteCategory | #deleteCategory | admin |
| GET | `/categories/main` | #getAllMainCategories | CategoryQueryService#findAllMainCategories | |
| GET | `/categories/sub/{mainCategoryName}` | #getSubCategoriesByMain | #findSubCategoriesByMainCategory | |
| POST | `/collections` | CollectionController#createCollection | CollectionCommandService#createCollection | admin |
| PUT | `/collections/{collectionId}` | #updateCollection | #updateCollection | admin |
| DELETE | `/collections/{collectionName}` | #deleteCollection | #deleteCollection | admin, **이름**으로 삭제 |
| GET | `/collections`, `/collections/page`, `/collections/search?keyword=`, `/collections/{collectionId:[0-9]+}`, `/collections/name/{collectionName}` | CollectionController 조회 메서드들 | CollectionQueryService | Brand와 동일 패턴 |

ProductSize 전용 REST 엔드포인트는 없다(색상 생성/수정 시 `sizes` 리스트로 처리).

## 핵심 엔티티와 관계
(`BaseTimeEntity` 상속: ProductColorViewLog 제외 전부)

| 엔티티 (테이블) | 주요 필드 | 연관관계 |
|---|---|---|
| `Product` (`product`) | name, englishName, releasePrice(int), modelNumber, releaseDate(String "YYYY-MM-DD"), gender(`GenderType`, STRING) | `@ManyToOne(LAZY)` brand/category/collection; `@OneToMany(mappedBy="product", cascade=ALL, orphanRemoval)` colors |
| `ProductColor` (`product_color`) | colorName, content(`@Lob` HTML) | `@ManyToOne(LAZY)` product; `@OneToOne(cascade=ALL, orphanRemoval)` thumbnailImage (`thumbnail_image_id`); `@OneToMany(cascade=ALL, orphanRemoval)` sizes, productImages, productDetailImages, interests |
| `ProductSize` (`product_size`) | sizeType(`SizeType`), size(String), purchasePrice, salePrice, quantity | `@ManyToOne(LAZY)` productColor. `update(purchase, sale, qty)` |
| `ProductImage` | imageUrl(파일명만) | `@ManyToOne(LAZY)` productColor (nullable=false). 썸네일도 이 엔티티 |
| `ProductDetailImage` | imageUrl | `@ManyToOne(LAZY)` productColor |
| `Brand` / `Collection` | name | 단독 |
| `Category` (`category`) | name | self `@ManyToOne` parentCategory(`parent_category_id`), `@OneToMany` subCategories(cascade ALL). 메인=parent null, 서브=parent 있음 |
| `Interest` | — | `@ManyToOne` user(`user_id`, user 도메인 `User`), productColor. `User.interests`와 양방향 |
| `ProductPriceHistory` | price, priceType(String) | `@ManyToOne` productSize. **코드에서 생성/사용처 없음** (repository만 존재) |
| `ProductColorViewLog` | viewedAt, userEmail, userAge, userGender(user 도메인 `Gender`) | `@ManyToOne` productColor. Kafka consumer가 저장 |

Enum 값:
- `GenderType`: MALE, FEMALE, KIDS, UNISEX
- `SizeType`: CLOTHING(XXS~XXXL, 28~36), SHOES(70~330), ACCESSORIES(ONE_SIZE) — `getSizes()`가 허용 사이즈 목록
- `ColorType`(displayName): BLACK(블랙), GREY, WHITE, IVORY, BEIGE, BROWN, KHAKI, GREEN, LIGHT_GREEN, MINT, NAVY, BLUE, SKY_BLUE, PURPLE, PINK, RED, ORANGE, YELLOW, SILVER, GOLD, MIX(믹스)

## 주요 서비스 메서드와 비즈니스 규칙
| 메서드 | 규칙 |
|---|---|
| `ProductCommandService#createProduct` | 클래스 `@Transactional`. brand는 `BrandEntityService.findByName`(없으면 BRAND_NOT_FOUND), category는 `findSubCategoryByName(categoryName, mainCategoryName)` — **서브 카테고리 필수**. collectionName 있으면 `createOrGetCollection`(없으면 자동 생성). 예상외 예외 → PRODUCT_CREATION_FAILED |
| `#updateProduct` | null 아닌 필드만 갱신(`Product.updateProduct`). category는 categoryName+mainCategoryName 둘 다 있을 때만 변경. 응답 gender는 request 값을 그대로 반환 |
| `#deleteProduct` | 각 color에 대해 `ProductColorCommandService.deleteProductColor` 호출 후 product 삭제 |
| `ProductColorCommandService#createProductColor` | `validateColorType`: colorName이 `ColorType.name()`(대소문자 무시) 또는 displayName과 일치해야 함 → 아니면 INVALID_COLOR_TYPE. 파일 저장 디렉터리 `product/{productId}`, 접두사 `thumbnail_`, `ProductImage_`, `ProductDetailImage_` (`FileUtils` BASE_DIR=`/home/ubuntu/fream`, 파일명 = prefix+UUID+확장자). 저장 후 `ProductSizeCommandService.createProductSizes(color, categoryId, sizes, releasePrice)` 동기 호출 |
| `#updateProductColor` | colorName 변경 시에만 검증. 썸네일 교체 시 기존 파일 삭제. `existingImages`/`existingDetailImages`에 없는 기존 이미지는 파일+엔티티 삭제. `sizes`가 주어지면 diff로 제거/추가 |
| `#deleteProductColor` | interests 삭제 → sizes 삭제 → 썸네일/이미지/상세이미지 파일 및 엔티티 삭제 → color 삭제 |
| `ProductSizeCommandService#createProductSizes` | 루트 카테고리명(`CLOTHING`/`SHOES`/`ACCESSORIES`, 대소문자 무시)으로 SizeType 결정, 아니면 INVALID_SIZE_TYPE. 이미 있는 사이즈 skip. 초기값 purchasePrice=salePrice=releasePrice, quantity=0. 유효 사이즈는 먼저 저장하고 무효 사이즈가 있으면 마지막에 INVALID_SIZE_TYPE 예외(트랜잭션 롤백 여부는 호출 트랜잭션에 따름) |
| `#updateProductSize` | 가격/재고 수정 메서드. **main 코드 내 호출처 없음**(테스트만) |
| `InterestCommandService#toggleInterest` | 이메일로 User 조회, 존재하면 삭제/없으면 생성(양방향 편의 메서드 사용). 실패 → INTEREST_TOGGLE_FAILED |
| `BrandCommandService` | 이름 중복 → BRAND_ALREADY_EXISTS(409). 삭제 시 `productRepository.existsByBrand` → BRAND_IN_USE |
| `CategoryCommandService` | `subCategoryName == null`이면 메인 생성, 아니면 메인 하위에 서브 생성. 삭제 시 하위 카테고리 존재 또는 상품 연결 → CATEGORY_IN_USE |
| `CollectionCommandService` | 중복 → COLLECTION_ALREADY_EXISTS, 삭제 시 상품 연결 → COLLECTION_IN_USE. `createOrGetCollection` |
| `FilterService#getAllFilterData` | 카테고리 필터는 메인 "Clothing"의 서브 카테고리들 + 메인 "Shoes"(이름 하드코딩)를 1단계로 사용. 할인/가격대 옵션은 하드코딩 |

## 다른 도메인과의 의존
**product를 사용하는 쪽** (grep `import com.fream.back.domain.product` 결과)

| 도메인 | 사용 대상 |
|---|---|
| order | `OrderBid.productSize`, `OrderItem.productSize` (`@ManyToOne`, `product_size_id`); `OrderBidCommandService`가 `ProductSizeQueryService.findById`; `OrderCommandService`, `OrderItemCommandService`, `OrderItemRepository`; `OrderBidRepositoryImpl`(QProduct/QProductColor/QProductImage/QProductSize) |
| sale | `Sale.productSize`, `SaleBid.productSize`; `SaleBidCommandService`가 `ProductSizeQueryService.findById`; `SaleCommandService`; `SaleBidRepositoryImpl`(Q타입) |
| style | `StyleRepositoryCustomImpl` (`entity.*` import) |
| event | `Event.brand` (`@ManyToOne` Brand); `EventCommandService`/`EventValidationAspect`/`EventBusinessRuleAspect`가 `BrandQueryService`, `BrandNotFoundException` |
| user | `User.interests` (`@OneToMany(mappedBy="user")` Interest) |
| global | `GlobalExceptionHandler`(ProductException 핸들러), `DataInitializer`(`@Order(1)` CommandLineRunner, 상품 시드 데이터) |

**product가 사용하는 쪽**
- user: `UserQueryService`(checkAdminRole, findByEmail), `User`, `Gender`
- style: `StyleRepository.styleCountByColorIds` (검색 결과 styleCount)
- order: `OrderBidRepository.tradeCountByColorIds`, `QOrderBid`/`BidStatus.COMPLETED`(ProductQueryDslRepository#tradeCountQuery)
- global: `FileUtils`, `NginxCachePurgeUtil`(PURGE `http://nginx:80/purge_products/api/products/`, `/purge_es/api/es/products/`), `SecurityUtils`, `JwtAuthenticationFilter.UserInfo`

**인프라**
- Redis/@Cacheable: product 패키지에 없음 (캐시는 Nginx 레벨 purge만).
- Kafka: topic `view-log-topic`, groupId `my-group`, containerFactory `viewEventKafkaListenerContainerFactory`.
- Elasticsearch: 인덱스 `product-colors` (startup 시 재생성).
- 스케줄러(`@Scheduled`): product 패키지에 없음. Spring Batch `createSizesJob` 정의만 존재.

## 예외/에러코드 위치
- `src/main/java/com/fream/back/domain/product/exception/ProductErrorCode.java` — 접두사: P(상품), PC(색상), PS(사이즈), CT(카테고리), BD(브랜드), CL(컬렉션), IN(관심), IMG(이미지), FT(필터), KF(Kafka; 정의만 있고 사용처 없음)
- `ProductException extends GlobalException`, 처리: `src/main/java/com/fream/back/global/exception/GlobalExceptionHandler.java#handleProductException`
- 서비스 대부분이 `catch (ProductException) rethrow; catch (Exception) → *_FAILED` 패턴. 조회 컨트롤러는 예외를 PRODUCT_NOT_FOUND/FILTER_INVALID_PARAMS 등으로 다시 감싼다.

## 관련 테스트 파일
| 파일 | 종류 | 내용 |
|---|---|---|
| `src/test/java/com/fream/back/domain/product/repository/ProductQueryDslRepositoryTest.java` | `@DataJpaTest` + `@ActiveProfiles("test")` | 검색/카운트/상세/색상ID 검색. 현재 동작(버그 포함)을 고정하는 테스트가 있음: `interestCountIsMultipliedBySizeRowsOfTheColor`, `countProductsByFilterIgnoresCollectionFilter`, `productDetailCollectsSizesBySameColorNameAcrossProducts`, `searchByColorIdsDropsColorsWithoutThumbnail`, `pagingAppliesToColorRowsWhileCountQueryCountsDistinctProducts` |
| `src/test/java/com/fream/back/domain/product/service/ProductCommandServiceTest.java` | Mockito | 생성/수정/삭제 |
| `.../service/ProductSizeCommandServiceTest.java` | Mockito | 사이즈 생성/수정/삭제 |
| `.../service/InterestCommandServiceTest.java` | Mockito | 토글, `deleteAllInterestsFailsWhenListIsModifiedDuringIteration` |
| `.../service/BrandAndCollectionCommandServiceTest.java` | Mockito | 중복/사용중 검증 |
- ES/Kafka/컨트롤러 테스트 없음. 버그를 고치면 위 characterization 테스트의 기대값도 함께 바뀐다.

## 자주 할 작업 → 여기를 보라
| 작업 | 위치 |
|---|---|
| 검색 필터 추가 (DB) | `ProductSearchDto` → `ProductQueryController#searchProducts`/`#countProductsByFilter` → `ProductQueryService` → `ProductQueryDslRepository#searchProducts` + `#countProductsByFilter`(쿼리 2벌) + `build*Predicate` |
| 검색 필터 추가 (ES) | `ProductColorIndex`(필드) + `ProductColorIndexQueryRepository`(projection) + `ProductColorIndexingService`(builder 2곳) + `ProductColorSearchService#search` |
| 상세 응답 필드 추가 | `ProductDetailResponseDto` + `ProductQueryDslRepository#findProductDetail` |
| 허용 색상/사이즈 변경 | `entity/enumType/ColorType.java`, `SizeType.java` (FilterService도 자동 반영) |
| 이미지 저장 경로 | `global/utils/FileUtils.java`(BASE_DIR), `ProductQueryController#getProductImage`(경로 하드코딩) |
| 거래/스타일 수 집계 | `OrderBidRepositoryImpl#tradeCountByColorIds`, `StyleRepositoryCustomImpl#styleCountByColorIds` |
| 에러코드 추가 | `exception/ProductErrorCode.java` |

## Gotchas (코드에서 확인됨)
1. **ES 인덱스 동기화 없음**: `ProductColorIndexingService#indexColorById`/`#deleteColorFromIndex`는 정의만 있고 호출처가 없다. 인덱스는 앱 시작 시 `ProductColorIndexInitializer`가 삭제→재생성→전체 색인할 때만 갱신된다. CUD 후에는 Nginx 캐시 purge만 호출된다.
2. **상세 조회 사이즈 버그**: `ProductQueryDslRepository#findProductDetail`의 사이즈 조회가 `productSize.productColor.colorName.eq(colorName)`만 조건으로 써서, 같은 colorName을 가진 **다른 상품의 사이즈까지** 섞인다(테스트로 고정됨).
3. **interestCount 부풀림**: 검색 쿼리가 sizes와 interests를 함께 left join 후 `interest.count()` → 관심 수 × 사이즈 행 수.
4. **countProductsByFilter**: 같은 쿼리를 두 번 실행하며, 실제 반환하는 두 번째 쿼리에 collection 조건이 빠져 있다.
5. **페이징 불일치**: 검색 content는 (product, color) 단위 행인데 total은 `product.id.countDistinct()`.
6. `searchProductsByColorIds`: `productColor.thumbnailImage.imageUrl` 경로 참조로 썸네일 없는 색상이 결과에서 빠진다.
7. **ProductColor 삭제 실패 가능**: `InterestCommandService#deleteAllInterestsByProductColor`가 `productColor.getInterests().forEach` 안에서 `unassignProductColor()`(같은 리스트 remove)를 호출 → 관심이 있는 색상은 ConcurrentModificationException → ProductException(INTEREST_TOGGLE_FAILED). 테스트로 확인됨. 상품 삭제도 이 경로를 탄다.
8. `CreateSizesJobConfig`의 `createSizesJob`이 `ProductColorCommandService`에 `JobLauncher`와 함께 주입되지만 `jobLauncher.run` 호출은 없다(사이즈는 동기 생성).
9. `ProductQueryController#getProductDetail`: `SecurityUtils.extractEmailOrAnonymous()`는 미인증 시 `"anonymous"`를 반환하지만 컨트롤러는 `"anonymousUser"`와 비교한다. `extractUserInfo()`가 null이면 `userInfo.getAge()`에서 NPE → 외부 `catch (Exception)`에서 PRODUCT_NOT_FOUND로 변환된다(실제 발생 여부는 인증 필터 동작에 따름, 미확인).
10. QueryDSL 검색에서 `sortOption`이 non-null인데 field가 price/releaseDate/interestCount가 아니면 정렬이 아예 없다. `ProductSearchDto.validate()`는 sortOption null이면 `interestCount desc` 기본값을 넣는다. `IndexedProductRepository#applySortingOptimized`는 `getField().toLowerCase()`라 field null이면 NPE.
11. `IndexedProductRepository`: 이름으로 ID를 찾지 못하면(빈 리스트) 해당 필터가 **무시**되어 전체가 조회된다.
12. 페이징: `application.yml`의 `spring.data.web.pageable.one-indexed-parameters: true`, default-page-size 10.
13. 브랜드/컬렉션 DELETE는 path가 ID가 아니라 **이름**이다. PUT은 ID.
14. `ProductQueryDslRepository#searchProducts`에 `System.out.println` 디버그 출력이 다수 있다(로거 아님).
15. 카테고리 기반 SizeType 결정은 루트 카테고리 이름이 정확히 Clothing/Shoes/Accessories(대소문자 무시)여야 한다.
