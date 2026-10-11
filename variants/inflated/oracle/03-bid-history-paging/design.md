# 03 bid-history-paging — 설계 메모 (pattern: 페이지 total 불일치)

## 의도

- 보고된 증상은 구매 입찰 내역(주문 도메인) 한 곳이다.
- 같은 원인 "본문 쿼리와 count 쿼리가 다른 행 집합을 센다"가 판매 입찰(sale)과 상품 검색 두 곳(product)에도 있다.
- 보고 지점에서 원인(본문만 썸네일을 inner join)을 이해한 뒤, 코드베이스 전체에서 `PageImpl`/count 쿼리를 쓰는 곳을 훑어 본문·count의 조인·단위를 대조해야 모두 찾을 수 있다.

## 지점 목록 (hidden이 모두 검사)

| # | 위치 | 본문 | count | hidden 클래스 |
|---|---|---|---|---|
| 1 (보고) | `domain/order/repository/OrderBidRepositoryImpl.java:85-105` `findOrderBidsByFilters` (호출: `OrderBidQueryService.getOrderBids`) | `productColor.thumbnailImage` inner join(90행) → 썸네일 없는 색상의 입찰이 빠짐 | user·order 조인만(99-105행) → 모두 셈 | `PurchaseBidListPagingHiddenTest` |
| 2 | `domain/sale/repository/SaleBidRepositoryImpl.java:83-110` `findSaleBidsByFilters` (호출: `SaleBidQueryService.getSaleBids`) | 썸네일 inner join(88행) | seller·sale 조인만(101-110행) | `SaleBidListPagingHiddenTest` |
| 3 | `domain/product/repository/ProductQueryDslRepository.java:81-197` `searchProducts` | 상품 x 색상 행(`groupBy(product.id, productColor.id, ...)`) | `product.id.countDistinct()`(178행) → 상품 수 | `ProductSearchPagingHiddenTest` |
| 4 | `domain/product/repository/IndexedProductRepository.java:128-206, 328-351` `searchProductsByNames` → `searchProductsByIdsOptimized` | 상품 x 색상 행 + select 경로 `productColor.thumbnailImage.imageUrl`·`product.brand.name`이 암묵 inner join(썸네일·브랜드 없는 행 탈락) | `countProductsByConditions`: `product.id.countDistinct()` + left join만 | `ProductNameSearchPagingHiddenTest` |

- 1·2는 의심 지점 O4·N2, 3은 P1이다. 4는 목록에 없던 지점으로, 3의 복제본(인덱스 최적화판)을 grep으로 찾았다.
- 판정 방식(결과 기준): 페이지 크기를 바꿔 가며 처음부터 끝까지 넘기고, 모든 페이지의 `getTotalElements()`가 넘겨 본 항목 수와 같은지, 항목이 중복되지 않는지 본다. 본문을 넓히든(left join) count를 좁히든(inner join) 둘 다 통과한다. 단, 지금 목록에 나오는 항목(썸네일 있는 행, 3은 썸네일 없는 색상 행 포함)이 빠지면 실패한다.

## 전수 grep 기록 (누락 방지)

- 명령: `grep -rln "fetchCount\|countQuery\|PageableExecutionUtils\|new PageImpl" src/main`, `grep -rn "countQuery\s*=" src/main`, `@Query` + `Page<` 반환 리포지토리 전체.
- 검토 후 제외(본문·count 행 집합 일치):
  - `InterestQueryDslRepository.findUserInterestProducts`: 본문 select에 `productColor.thumbnailImage.imageUrl` 경로가 있지만 같은 경로를 명시 left join(75행)해 Hibernate가 그 조인을 재사용한다. H2 실측: 4행·total 4 일치.
  - `ProductQueryDslRepository.searchProductsByColorIds`: 암묵 inner join이 있지만 `query.fetchCount()`가 같은 쿼리를 그대로 세서 일치(실측 본문 0·total 0, 브랜드 없는 픽스처). 썸네일 탈락(P5)은 별개 버그.
  - `ProductQueryDslRepository.countProductsByFilter`: 페이지 total이 아니라 필터 결과 "상품 수" API. 컬렉션 필터 무시(P3)는 별개 버그.
  - `EventRepositoryImpl`, `EventSearchRepository`, `NotificationRepositoryCustomImpl`(3곳): 본문 fetch join과 count join이 같은 ManyToOne.
  - `InquiryRepositoryImpl`(3곳): 본문 추가 조인이 `leftJoin(user.profile)`뿐(행 수 불변).
  - `InspectionStandardRepositoryImpl`(2곳)·`InspectionStandardRepository`(@Query 2곳): `selectDistinct` + fetchCount/`COUNT(DISTINCT)`로 일치.
  - `StyleRepositoryCustomImpl.filterStyles`: total = 필터 ID 목록 크기, 본문은 그 ID로 조회. `getStylesByProfile`: 본문·count 모두 profile left join.
  - `ChatQuestionRepositoryImpl`, `FAQRepositoryImpl`, `NoticeRepositoryImpl`: 조인 없음.
  - `@Query` Page 메서드(Event 3, Notice 2, Hashtag, StyleHashtag, StyleComment, FAQ 2, UserSanction, Follow 2, Inquiry): 명시 countQuery 없이 Spring Data가 같은 JPQL에서 count를 파생한다.
  - Elasticsearch(`ProductColorSearchService`), `IndexedProductQueryService`·`ProductQueryService`(리포지토리 total을 그대로 전달): 외부 서버이거나 래퍼.

## 정답 (`fix.patch`)

- 1·2: 목록 본문의 썸네일 조인을 `leftJoin`으로 바꾼다(썸네일 없는 상품의 입찰도 목록에 나오고 imageUrl은 null).
- 3: count를 본문과 같은 단위 `productColor.id.countDistinct()`로 센다.
- 4: 본문에 `leftJoin(product.brand, QBrand.brand)`, `leftJoin(productColor.thumbnailImage, QProductImage.productImage)`를 명시해 암묵 inner join을 없애고, count를 `productColor.id.countDistinct()`로 바꾼다.
- 알려진 상한: 3·4에서 색상이 하나도 없는 상품은 본문에 (상품, null) 한 행으로 나오지만 count에는 안 잡힌다. 정상 등록 흐름에서는 색상 없는 상품이 없어 hidden 픽스처에 넣지 않았다.
- 같은 클래스의 단건 상세(`findOrderBidById`, `findSaleBidById`)도 썸네일 inner join이라 썸네일 없는 입찰은 null이 나오지만, 페이지 total 문제가 아니라 범위에서 뺐다.

## 왜 탐색이 필요한가

- 증상(주문 입찰 목록 total) → `OrderBidQueryService.getOrderBids` → `OrderBidRepositoryImpl`에서 원인을 찾는다.
- 다른 지점은 주문 도메인 밖이다. sale 도메인의 거울 구현, product 도메인의 검색 두 곳은 "썸네일 조인"이 아니라 "count 단위(상품 vs 상품x색상)"와 "암묵 경로 조인" 형태라서, `thumbnailImage`만 grep하면 3을 놓치고 4의 암묵 조인도 놓친다. count 쿼리를 쓰는 리포지토리를 전수로 대조해야 한다.

## 순진한 오답

- 보고 지점만 고친다(부분 정답, 아래 diff): test.sh 통과, hidden에서 2·3·4 실패.
- `thumbnailImage`를 grep해 1·2만 고친다: 3·4 실패.
- 3만 고치고 4를 놓친다(인덱스 최적화판은 별도 파일, 별도 컨트롤러).
- 4의 count만 고치고 본문의 암묵 inner join(썸네일·브랜드)을 그대로 둔다: 썸네일 없는 색상만큼 여전히 어긋난다.
- `Interest`·`searchProductsByColorIds`를 "같은 패턴"으로 보고 고친다: 동작은 원래 일치하므로 판정에 영향은 없지만 불필요한 변경.

## regression-exclude

- `OrderBidRepositoryTest`, `SaleBidRepositoryTest`: 썸네일 없는 입찰이 본문에서 빠지고 total에는 잡히는 현재 동작을 고정한다.
- `ProductQueryDslRepositoryTest`: total이 상품 수(페이지마다 3/4)인 현재 동작을 고정한다.

## 부분 정답 diff (보고 지점만)

```diff
diff --git a/src/main/java/com/fream/back/domain/order/repository/OrderBidRepositoryImpl.java b/src/main/java/com/fream/back/domain/order/repository/OrderBidRepositoryImpl.java
index a3a4f1a..6a5ea68 100644
--- a/src/main/java/com/fream/back/domain/order/repository/OrderBidRepositoryImpl.java
+++ b/src/main/java/com/fream/back/domain/order/repository/OrderBidRepositoryImpl.java
@@ -87,7 +87,7 @@ public class OrderBidRepositoryImpl implements OrderBidRepositoryCustom {
                 .join(orderBid.productSize, productSize)
                 .join(productSize.productColor, productColor)
                 .join(productColor.product, product)
-                .join(productColor.thumbnailImage, productImage)
+                .leftJoin(productColor.thumbnailImage, productImage)
                 .leftJoin(orderBid.order, order)
                 .leftJoin(order.orderShipment, orderShipment)
                 .where(whereBuilder)
```
