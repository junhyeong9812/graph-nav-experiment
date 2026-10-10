# flow: 상품 검색 (QueryDSL vs Elasticsearch) + 상품 조회 이벤트 Kafka 파이프라인

## 역할 요약
- 상품 목록 검색 경로는 3개다: (1) QueryDSL `GET /products/query`, (2) 이름 기반 QueryDSL `GET /products/indexed/search`, (3) Elasticsearch `GET /es/products` (+ 자동완성).
- 세 경로 모두 응답은 `ProductSearchResponseDto`(색상 단위 행)이고, 결과 colorId 목록으로 styleCount/tradeCount를 추가로 채운다.
- ES 인덱스 `product-colors`는 **앱 시작 시에만** 삭제 후 재생성·전체 색인된다(증분 동기화 호출 없음).
- 상세 조회(`GET /products/query/{productId}/detail`) 시 `view-log-topic`으로 이벤트를 발행하고 consumer가 `ProductColorViewLog`로 저장한다.

## 패키지/파일 맵
| 구분 | 파일 (repo root 기준) | 용도 |
|---|---|---|
| QueryDSL 검색 | `src/main/java/com/fream/back/domain/product/controller/query/ProductQueryController.java` | `/products/query` 검색·카운트·필터·상세 |
| | `src/main/java/com/fream/back/domain/product/service/product/ProductQueryService.java` | 검색 + styleCount/tradeCount 보강 |
| | `src/main/java/com/fream/back/domain/product/repository/ProductQueryDslRepository.java` | 실제 쿼리 |
| 이름 기반 검색 | `.../controller/query/IndexedProductQueryController.java`, `.../service/product/IndexedProductQueryService.java`, `.../repository/IndexedProductRepository.java` | 브랜드/카테고리/컬렉션 **이름**→ID 변환 후 검색 |
| 검색 조건 | `.../dto/ProductSearchDto.java`, `.../dto/ProductSearchByNameDto.java`, `.../repository/SortOption.java` | `validate()`로 가격 검증 + 기본 정렬 |
| 필터 데이터 | `.../service/filter/FilterService.java`, `.../dto/FilterDataResponseDto.java`, `.../dto/FilterCountResponseDto.java` | |
| ES 인덱스 정의 | `.../elasticsearch/index/ProductColorIndex.java` | `@Document(indexName="product-colors")` |
| ES 초기화 | `.../elasticsearch/config/ProductColorIndexInitializer.java` | CommandLineRunner `@Order(2)`: 삭제→settings/mapping 생성→전체 색인 |
| ES 색인 | `.../elasticsearch/service/ProductColorIndexingService.java`, `.../elasticsearch/repository/ProductColorIndexQueryRepository.java`, `.../elasticsearch/repository/ProductColorEsRepository.java`, `.../elasticsearch/dto/ProductColorIndexDto.java`, `ProductColorSizeRow.java` | DB(QueryDSL) → 문서 변환 → saveAll |
| ES 검색 | `.../elasticsearch/service/ProductColorSearchService.java`, `.../elasticsearch/controller/ProductColorSearchController.java` | bool 쿼리, 자동완성, 디버깅 API |
| ES 분석기 리소스 | `docker/elasticsearch/Dockerfile-elasticsearch`, `docker/elasticsearch/synonyms.txt`, `docker/elasticsearch/userdict_ko.txt` | ES 8.13.4 + analysis-nori, `config/analysis/`에 복사 |
| Kafka | `.../service/kafka/ViewEventProducer.java`, `.../service/kafka/ViewEventConsumer.java`, `.../service/kafka/config/ViewEventKafkaConfig.java`, `.../dto/kafka/ViewEvent.java` | 조회 이벤트 |
| 저장소 | `.../entity/ProductColorViewLog.java`, `.../repository/ProductColorViewLogRepository.java` | 조회 로그 테이블 |
| 보강 쿼리 | `src/main/java/com/fream/back/domain/style/repository/StyleRepositoryCustomImpl.java#styleCountByColorIds`, `src/main/java/com/fream/back/domain/order/repository/OrderBidRepositoryImpl.java#tradeCountByColorIds` | styleCount, tradeCount |

## REST endpoints
| Method | Path | Controller#method | Service → Repository |
|---|---|---|---|
| GET | `/products/query` | ProductQueryController#searchProducts | ProductQueryService#searchProducts → ProductQueryDslRepository#searchProducts |
| POST | `/products/query/filters/count` | #countProductsByFilter | ProductQueryService#countProductsByFilter → ProductQueryDslRepository#countProductsByFilter |
| GET | `/products/query/filters` | #getFilterData | FilterService#getAllFilterData |
| GET | `/products/query/{productId}/detail?colorName=` | #getProductDetail | ProductQueryService#getProductDetail → #findProductDetail; ViewEventProducer#sendViewEvent |
| GET | `/products/indexed/search` | IndexedProductQueryController#searchProductsByNames | IndexedProductQueryService → IndexedProductRepository#searchProductsByNames |
| GET | `/es/products` | ProductColorSearchController#esSearchProducts | ProductColorSearchService#searchToDto → #search |
| GET | `/es/products/autocomplete?q=` | #autocompleteProducts | ProductColorSearchService#autocomplete(q, 10) |
| GET | `/es/products/raw-indices` | #getRawIndices | #getAllIndexedColors (디버깅) |
| GET | `/es/products/raw-indices/category/{categoryId}` | #getRawIndicesByCategory | #getIndexedColorsByCategory (디버깅) |
| GET | `/es/products/index-stats` | #getIndexStats | #getIndexStats (디버깅; sneakers categoryId=5L 하드코딩) |

공통 쿼리 파라미터(`ProductSearchDto`, `@ModelAttribute`): `keyword`, `categoryIds`, `genders`(GenderType), `brandIds`, `collectionIds`, `colors`, `sizes`, `minPrice`, `maxPrice`, `sortOption.field`/`sortOption.order` + Spring `Pageable`(`page`는 1-based: `one-indexed-parameters: true`).
`ProductSearchByNameDto`는 ID 대신 `categoryNames`, `brandNames`, `collectionNames`를 받고 음수 가격도 거부한다.

## 1. QueryDSL 경로 (`ProductQueryDslRepository#searchProducts`)
- FROM `product` LEFT JOIN colors, brand, thumbnailImage, sizes, interests; GROUP BY (product.id, productColor.id, thumbnail imageUrl) → **색상 단위 행**.
- 선택 컬럼: product id/name/englishName/releasePrice, color id/name, thumbnail url, `min(purchasePrice)` → `price`, `interest.count()` → `interestCount`, brand name.

| 필터 | 조건 |
|---|---|
| keyword | 소문자 후 `\s+`, `-`, `(`, `)`로 분리. 각 토큰은 `name`/`englishName`/`colorName` lower LIKE 또는 `size` LIKE 의 OR, 토큰끼리는 **AND** |
| categoryIds | `product.category.id IN` (하위 카테고리 확장 없음) |
| genders | `product.gender IN` |
| brandIds / collectionIds | `IN` |
| colors | `productColor.colorName IN` (정확 일치) |
| sizes | `productSize.size IN` |
| minPrice/maxPrice | `productSize.purchasePrice >= min`, `<= max` (사이즈 행 단위 → min(price)는 조건 만족 사이즈 중 최소) |

| sortOption.field | 정렬 |
|---|---|
| `price` | `min(purchasePrice)` |
| `releaseDate` | `product.releaseDate`(문자열) |
| `interestCount` | `productColor.interests.size().sum()` |
| 그 외 | 정렬 없음. sortOption 자체가 null이면 `product.id asc` (단, 컨트롤러가 `validate()`로 null을 `interestCount desc`로 채움) |
- order는 `"asc"`(대소문자 무시)일 때만 오름차순, 나머지는 내림차순.
- total = 같은 조건의 `countDistinct(product.id)`.
- 이후 `ProductQueryService`가 `styleRepository.styleCountByColorIds`, `orderBidRepository.tradeCountByColorIds`로 styleCount/tradeCount 세팅.
- `IndexedProductRepository`는 동일 구조(브랜드 조인/썸네일 그룹 제외)이고 styleCount/tradeCount는 `ProductQueryDslRepository#styleCountQuery`/`#tradeCountQuery`(OrderBid status `COMPLETED`) 사용.

## 2. Elasticsearch 경로
### 인덱스 구조 (`product-colors`, 문서 ID = `colorId`)
| 필드 | 타입 | 출처 |
|---|---|---|
| colorId(@Id), productId, brandId, categoryId, collectionId | (매핑 미지정, 동적) | ProductColor/Product/연관 ID |
| productName, brandName, categoryName, collectionName, colorName | text, analyzer `my_nori_base_analyzer`, search_analyzer `my_nori_synonym_analyzer` (initializer의 putMapping) | |
| productEnglishName | text (`@Field`) | |
| gender | keyword | `Product.gender.stringValue()` |
| thumbnailUrl | text | 썸네일 ProductImage.imageUrl |
| releaseDate | date, format `yyyy-MM-dd` | Product.releaseDate |
| releasePrice, minPrice, maxPrice, interestCount | (동적) | min/max `purchasePrice`, `countDistinct(interest.id)` |
| sizes | keyword 배열 | 색상의 모든 ProductSize.size |

분석기(`ProductColorIndexInitializer` settings):
- tokenizer `nori_tokenizer_custom`: nori, `decompound_mode: discard`, `user_dictionary: analysis/userdict_ko.txt`
- filter `my_synonym_filter`: synonym_graph, `analysis/synonyms.txt`, `updateable: true`; `my_nori_edge_ngram`: edge_ngram 2~4
- `my_nori_base_analyzer` = tokenizer + lowercase + edge_ngram (색인용)
- `my_nori_synonym_analyzer` = tokenizer + lowercase + synonym + edge_ngram (검색용)
- `docker/elasticsearch/synonyms.txt`: `black,검정,검은색,블랙` / `white,흰색,화이트,하양` (2줄); `userdict_ko.txt`: 나이키, 아디다스 (NNP)

### 인덱스 (재)생성/동기화 시점
| 시점 | 동작 |
|---|---|
| 앱 시작 (`ProductColorIndexInitializer#run`, CommandLineRunner `@Order(2)`; `DataInitializer`가 `@Order(1)`) | 인덱스 존재 시 **삭제** → settings로 create → putMapping → `ProductColorIndexingService#indexAllColors()` |
| `indexAllColors` | `findAllForIndexingDto()`(색상별 groupBy 집계) + `findAllSizesForIndexing()` → sizes 맵핑 → `saveAll`. 예외는 `System.err` 출력 후 삼킴 |
| 상품/색상 생성·수정·삭제 | **인덱스 갱신 없음**. `indexColorById`/`deleteColorFromIndex`는 정의만 있고 호출처 없음. 컨트롤러는 `NginxCachePurgeUtil#purgeProductCache`, `#purgeEsCache`만 호출 |
| 관심 토글/사이즈 가격 변경 | 인덱스 갱신 없음 (interestCount/minPrice는 재시작 전까지 stale) |
| 스케줄러 | 없음 |

ES 접속: `application-prod.yml` `spring.elasticsearch.uris: http://elasticsearch:9200`.

### 검색 쿼리 (`ProductColorSearchService#search`)
| 조건 | ES 쿼리 |
|---|---|
| keyword | must(bool should[ multi_match phrase (`productName^3, productEnglishName^3, brandName^2, categoryName, collectionName, colorName`, boost 2), multi_match fuzziness AUTO (prefixLength 1, maxExpansions 50), (토큰 2개 이상이면) 토큰마다 must(should match productName^1.5/productEnglishName^1.5/brandName^1.2/colorName) ]) |
| categoryIds / brandIds / collectionIds | must(bool should term `categoryId`/`brandId`/`collectionId`) |
| genders | 컨트롤러가 `GenderType.name()`으로 변환 → should(match + term `gender`, 대문자) |
| colors | should(match `colorName` boost 2, match fuzziness AUTO) |
| sizes | should(term `sizes`) |
| minPrice (>0) | `range minPrice lte {minPrice}` |
| maxPrice (>0) | `range maxPrice gte {maxPrice}` |
| 조건 없음 | match_all |

정렬: 컨트롤러가 sortOption null이면 `interestCount desc` 세팅. field 매핑 `releaseDate`→releaseDate, `interestCount`→interestCount, `price`→**minPrice**, 그 외→productId; order `desc`(대소문자 무시)만 내림차순. pageable null이면 `PageRequest.of(0,20)`.
결과 → `toDto`(price=minPrice) → styleCount/tradeCount 보강(QueryDSL 경로와 동일 repository).

자동완성: multi_match(best_fields, fuzziness AUTO; `productName^2, productEnglishName^2, brandName^1.5, collectionName, colorName`) must + prefix(productName/brandName/colorName) should, `interestCount desc`, 최대 10개. 문자열 포맷 `"{brand} - {productName} ({colorName})"`, 중복 제거. 예외 시 빈 리스트.

## 3. 상품 조회 이벤트 Kafka 파이프라인
```
GET /products/query/{productId}/detail?colorName=
  ProductQueryController#getProductDetail
    ├─ ProductQueryService#getProductDetail (DB)
    ├─ email = SecurityUtils.extractEmailOrAnonymous()   // 미인증 "anonymous"
    ├─ age/gender = SecurityUtils.extractUserInfo() (JwtAuthenticationFilter.UserInfo) ; 기본 0 / Gender.OTHER
    └─ ViewEventProducer#sendViewEvent(colorId, email, age, gender)
          kafkaTemplate.send("view-log-topic", ViewEvent{productColorId, email, age, gender, viewedAt=now})  // key 없음, 결과 미확인(fire-and-forget)
                ↓
  ViewEventConsumer#listen  @KafkaListener(topics="view-log-topic", groupId="my-group",
                                           containerFactory="viewEventKafkaListenerContainerFactory")
    ├─ productColorRepository.findById (없으면 IllegalArgumentException)
    ├─ ProductColorViewLog.create(color, email, age(null→0), gender) + addViewedAt(event.viewedAt)
    └─ productColorViewLogRepository.save  → ProductColorViewLog 엔티티 테이블 (@Table 미지정, 실제 테이블명은 네이밍 전략에 따름)
```
| 항목 | 값 |
|---|---|
| Topic | `view-log-topic` (상수 `ViewEventProducer.TOPIC_NAME`) |
| Group ID | `my-group` (`application-prod.yml`의 `spring.kafka.consumer.group-id`도 `"my-group"`) |
| 직렬화 | Producer: `StringSerializer` / `JsonSerializer`; Consumer: `StringDeserializer` / `JsonDeserializer<>(ViewEvent.class)` |
| 설정 Bean | `ViewEventKafkaConfig`: `viewEventProducerFactory`, `viewEventKafkaTemplate`, `viewEventConsumerFactory`, `viewEventKafkaListenerContainerFactory` (bootstrap = `${spring.kafka.bootstrap-servers}`, prod `kafka:9092`) |
| 저장소 | `ProductColorViewLog` (viewedAt, userEmail, userAge, userGender, product_color_id) |
| 조회/통계 사용처 | `ProductColorViewLogRepository`는 기본 JpaRepository뿐, 로그를 읽는 코드는 product 도메인 내 없음 |
- 비슷한 구조로 style 도메인에 `style-view-log-topic`(group `style-view-group`)이 별도로 있다.

## 다른 도메인 의존
- style: `StyleRepository.styleCountByColorIds` (ProductQueryService, ProductColorSearchService)
- order: `OrderBidRepository.tradeCountByColorIds`, `QOrderBid`/`BidStatus.COMPLETED`
- user: `Gender` enum(ViewEvent/ViewLog), `JwtAuthenticationFilter.UserInfo`
- global: `NginxCachePurgeUtil`(Nginx 캐시: `/purge_products/api/products/`, `/purge_es/api/es/products/`)

## 예외/에러코드
- 검색 컨트롤러: `IllegalArgumentException` → `ProductErrorCode.FILTER_INVALID_PARAMS`(FT001), 그 외 → `PRODUCT_NOT_FOUND`(P001). 상세 조회 실패 → P001.
- ES 컨트롤러/서비스는 별도 예외 래핑 없음 (자동완성만 try/catch → 빈 리스트).
- Kafka 에러코드 `KAFKA_EVENT_SENDING_FAILED`/`KAFKA_EVENT_PROCESSING_FAILED`는 정의만 있고 미사용.
- 정의: `src/main/java/com/fream/back/domain/product/exception/ProductErrorCode.java`

## 관련 테스트
- `src/test/java/com/fream/back/domain/product/repository/ProductQueryDslRepositoryTest.java` (QueryDSL 검색/카운트/상세/정렬/페이징; 현 버그 동작 고정)
- ES 검색, 인덱싱, Kafka producer/consumer 테스트는 없음.

## 자주 할 작업 → 여기를 보라
| 작업 | 위치 |
|---|---|
| ES 인덱스 필드 추가 | `ProductColorIndex` + `ProductColorIndexDto` + `ProductColorIndexQueryRepository`(두 projection) + `ProductColorIndexingService`(builder 두 곳) + 필요 시 `ProductColorIndexInitializer` mapping |
| 상품 변경 시 ES 동기화 추가 | `ProductColorCommandService`(create/update/delete) 또는 컨트롤러에서 `ProductColorIndexingService#indexColorById`/`#deleteColorFromIndex` 호출 (현재 미호출) |
| 동의어/사용자사전 | `docker/elasticsearch/synonyms.txt`, `userdict_ko.txt` (이미지 빌드 시 복사) |
| 정렬 옵션 추가 | DB: `ProductQueryDslRepository#searchProducts`(+`IndexedProductRepository#applySortingOptimized`, `InterestQueryDslRepository`), ES: `ProductColorSearchService#search` field 매핑 |
| 조회 로그 필드 추가 | `ViewEvent` + `ViewEventProducer` + `ViewEventConsumer` + `ProductColorViewLog` |

## Gotchas (코드에서 확인됨)
1. **앱 재시작마다 ES 인덱스 삭제/재생성** (주석상 "개발용"이지만 무조건 실행). 운영 중 CUD는 반영되지 않는다.
2. **ES 가격 필터 의미가 DB와 다르다**: `minPrice` 요청값은 `doc.minPrice <= 요청값`, `maxPrice`는 `doc.maxPrice >= 요청값`으로 적용된다(`rangeQuery(field, gte, lte)` 인자 순서). DB 경로는 사이즈 가격이 [min, max] 안에 있는지 검사.
3. ES `price` 정렬은 `minPrice` 필드, DB는 `min(purchasePrice)`.
4. ES 매핑에서 initializer가 putMapping으로 지정한 필드는 5개 text 필드뿐이며, `ProductColorIndex`의 `@Field` 애노테이션(gender keyword, sizes keyword, releaseDate date 등)이 실제 매핑에 반영되는지는 Spring Data 동작에 따름(미확인). `idxOps.create(settings)` 후 `putMapping(mapping)`만 호출한다.
5. DB 검색 total(고유 product 수) ≠ content 행(색상 수). interestCount는 사이즈 행 수만큼 곱해진다. `countProductsByFilter`는 collection 조건을 무시한다.
6. `findProductDetail`의 사이즈 목록은 colorName만으로 조회되어 다른 상품 사이즈가 섞인다.
7. 상세 조회 컨트롤러의 익명 판정이 `"anonymousUser"` 문자열 비교인데 `extractEmailOrAnonymous()`는 미인증 시 `"anonymous"`를 반환한다. `extractUserInfo()`는 null을 반환할 수 있고 그 경우 `getAge()` NPE가 `catch (Exception)`에 잡혀 상세 조회 자체가 P001로 실패할 수 있다(런타임 재현 미확인).
8. Producer는 `kafkaTemplate.send` 결과를 확인하지 않으며, Consumer는 `@Transactional` 없이 save하고 ProductColor가 없으면 예외를 던진다(에러 핸들러/DLT 설정 없음).
9. `ViewEventKafkaConfig`의 consumer/producer factory는 props를 직접 구성하므로 `application-prod.yml`의 `spring.kafka.consumer.*`(auto-offset-reset, enable-auto-commit 등)/`producer.*` 설정이 이 팩토리에는 적용되지 않는다(코드상 props에 bootstrap과 serializer만 넣음).
10. `IndexedProductRepository`는 이름이 하나도 매칭되지 않으면 해당 필터를 생략한다(결과 0건이 아니라 전체).
11. `ProductColorIndexingService#indexAllColors`는 saveAll 예외를 `System.err`로만 출력하고 삼킨다 → 기동은 성공하지만 인덱스가 비어 있을 수 있다.
