package com.fream.back.domain.product.repository;

import com.fream.back.domain.product.dto.ProductDetailResponseDto;
import com.fream.back.domain.product.dto.ProductSearchResponseDto;
import com.fream.back.domain.product.entity.*;
import com.fream.back.domain.product.entity.enumType.GenderType;
import com.fream.back.domain.product.entity.enumType.SizeType;
import com.fream.back.domain.user.entity.User;
import com.fream.back.global.config.QueryDslConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

/**
 * ProductQueryDslRepository 검색·상세 조회 결과 고정.
 *
 * 데이터:
 *  P1 "Air Jordan 1" (Nike, MALE, 2023-01-01, 컬렉션 Jordan)
 *     - Black: 250=300000, 260=250000, 썸네일 p1-black.jpg, 관심 1건
 *     - White: 250=180000
 *  P2 "Superstar" (Adidas, UNISEX, 2024-05-01)
 *     - Black: 270=120000
 *  P3 "Dunk Low" (Nike, FEMALE, 2022-03-01)
 *     - Grey: 240=90000
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({QueryDslConfig.class, ProductQueryDslRepository.class})
class ProductQueryDslRepositoryTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private ProductQueryDslRepository repository;

    private Brand nike;
    private Collection jordan;
    private Product p1;
    private Product p2;
    private Product p3;
    private ProductColor p1Black;
    private ProductColor p1White;
    private ProductColor p2Black;
    private ProductColor p3Grey;

    @BeforeEach
    void setUp() {
        nike = persist(Brand.builder().name("Nike").build());
        Brand adidas = persist(Brand.builder().name("Adidas").build());
        Category shoes = persist(Category.builder().name("Shoes").build());
        Category sneakers = Category.builder().name("Sneakers").build();
        sneakers.assignParentCategory(shoes);
        persist(sneakers);
        jordan = persist(Collection.builder().name("Jordan").build());

        p1 = persist(product("Air Jordan 1", "Air Jordan 1 High", 200000, "2023-01-01", GenderType.MALE, nike, sneakers, jordan));
        p2 = persist(product("Superstar", "Adidas Superstar", 100000, "2024-05-01", GenderType.UNISEX, adidas, sneakers, null));
        p3 = persist(product("Dunk Low", "Nike Dunk Low", 120000, "2022-03-01", GenderType.FEMALE, nike, sneakers, null));

        p1Black = color(p1, "Black", "p1-black.jpg", "<p>black</p>");
        size(p1Black, "250", 300000);
        size(p1Black, "260", 250000);
        p1White = color(p1, "White", "p1-white.jpg", "<p>white</p>");
        size(p1White, "250", 180000);
        p2Black = color(p2, "Black", "p2-black.jpg", null);
        size(p2Black, "270", 120000);
        p3Grey = color(p3, "Grey", null, null);
        size(p3Grey, "240", 90000);

        User user = persist(User.builder()
                .email("interest@test.com").password("pw").referralCode("REF-1").phoneNumber("01000000000")
                .build());
        Interest interest = Interest.builder().user(user).productColor(p1Black).build();
        persist(interest);

        em.flush();
        em.clear();
    }

    private <T> T persist(T entity) {
        em.persist(entity);
        return entity;
    }

    private Product product(String name, String englishName, int releasePrice, String releaseDate, GenderType gender,
                            Brand brand, Category category, Collection collection) {
        return Product.builder()
                .name(name).englishName(englishName).releasePrice(releasePrice)
                .modelNumber("MN-" + name).releaseDate(releaseDate).gender(gender)
                .brand(brand).category(category).collection(collection)
                .build();
    }

    private ProductColor color(Product product, String colorName, String thumbnailUrl, String content) {
        ProductColor color = ProductColor.builder().colorName(colorName).content(content).product(product).build();
        em.persist(color);
        if (thumbnailUrl != null) {
            ProductImage image = ProductImage.builder().imageUrl(thumbnailUrl).productColor(color).build();
            em.persist(image);
            color.addThumbnailImage(image);
        }
        return color;
    }

    private void size(ProductColor color, String size, int purchasePrice) {
        em.persist(ProductSize.builder()
                .sizeType(SizeType.SHOES).size(size)
                .purchasePrice(purchasePrice).salePrice(purchasePrice + 10000).quantity(5)
                .productColor(color)
                .build());
    }

    private Page<ProductSearchResponseDto> search(String keyword, List<Long> brandIds, List<GenderType> genders,
                                                  List<Long> collectionIds, List<String> colors, List<String> sizes,
                                                  Integer minPrice, Integer maxPrice, SortOption sort, int page, int pageSize) {
        return repository.searchProducts(keyword, null, genders, brandIds, collectionIds, colors, sizes,
                minPrice, maxPrice, sort, PageRequest.of(page, pageSize));
    }

    @Test
    void searchWithoutFilterReturnsOneRowPerColor() {
        Page<ProductSearchResponseDto> result = search(null, null, null, null, null, null, null, null, null, 0, 20);

        assertThat(result.getContent()).hasSize(4);
        // 마지막 페이지이므로 PageImpl이 total을 offset + 행 수(4)로 보정한다
        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getContent()).extracting(ProductSearchResponseDto::getId)
                .containsExactly(p1.getId(), p1.getId(), p2.getId(), p3.getId());
        assertThat(result.getContent())
                .extracting(ProductSearchResponseDto::getColorName, ProductSearchResponseDto::getPrice,
                        ProductSearchResponseDto::getBrandName, ProductSearchResponseDto::getThumbnailImageUrl)
                .containsExactlyInAnyOrder(
                        tuple("Black", 250000, "Nike", "p1-black.jpg"),
                        tuple("White", 180000, "Nike", "p1-white.jpg"),
                        tuple("Black", 120000, "Adidas", "p2-black.jpg"),
                        tuple("Grey", 90000, "Nike", null));
    }

    @Test
    void interestCountIsMultipliedBySizeRowsOfTheColor() {
        Page<ProductSearchResponseDto> result = search(null, null, null, null, List.of("Black"), null, null, null, null, 0, 20);

        ProductSearchResponseDto p1BlackRow = result.getContent().stream()
                .filter(r -> r.getColorId().equals(p1Black.getId())).findFirst().orElseThrow();
        ProductSearchResponseDto p2BlackRow = result.getContent().stream()
                .filter(r -> r.getColorId().equals(p2Black.getId())).findFirst().orElseThrow();
        // 관심 1건 x 사이즈 2행
        assertThat(p1BlackRow.getInterestCount()).isEqualTo(2L);
        assertThat(p2BlackRow.getInterestCount()).isEqualTo(0L);
    }

    @Test
    void keywordTokensAreCombinedWithAnd() {
        Page<ProductSearchResponseDto> both = search("jordan HIGH", null, null, null, null, null, null, null, null, 0, 20);
        assertThat(both.getContent()).extracting(ProductSearchResponseDto::getId).containsOnly(p1.getId());
        assertThat(both.getTotalElements()).isEqualTo(2);

        Page<ProductSearchResponseDto> mismatch = search("jordan superstar", null, null, null, null, null, null, null, null, 0, 20);
        assertThat(mismatch.getContent()).isEmpty();
        assertThat(mismatch.getTotalElements()).isZero();
    }

    @Test
    void keywordMatchesColorNameAndSplitsOnHyphen() {
        Page<ProductSearchResponseDto> byColor = search("grey", null, null, null, null, null, null, null, null, 0, 20);
        assertThat(byColor.getContent()).extracting(ProductSearchResponseDto::getColorId).containsExactly(p3Grey.getId());

        Page<ProductSearchResponseDto> hyphen = search("dunk-low", null, null, null, null, null, null, null, null, 0, 20);
        assertThat(hyphen.getContent()).extracting(ProductSearchResponseDto::getId).containsExactly(p3.getId());
    }

    @Test
    void brandGenderAndCollectionFilters() {
        assertThat(search(null, List.of(nike.getId()), null, null, null, null, null, null, null, 0, 20).getContent())
                .extracting(ProductSearchResponseDto::getColorId)
                .containsExactlyInAnyOrder(p1Black.getId(), p1White.getId(), p3Grey.getId());
        assertThat(search(null, null, List.of(GenderType.UNISEX), null, null, null, null, null, null, 0, 20).getContent())
                .extracting(ProductSearchResponseDto::getId).containsExactly(p2.getId());
        assertThat(search(null, null, null, List.of(jordan.getId()), null, null, null, null, null, 0, 20).getContent())
                .extracting(ProductSearchResponseDto::getId).containsOnly(p1.getId());
    }

    @Test
    void sizeAndPriceFiltersRestrictTheMinimumPriceToMatchingSizes() {
        Page<ProductSearchResponseDto> bySize = search(null, null, null, null, null, List.of("250"), null, null, null, 0, 20);
        assertThat(bySize.getContent())
                .extracting(ProductSearchResponseDto::getColorName, ProductSearchResponseDto::getPrice)
                .containsExactlyInAnyOrder(tuple("Black", 300000), tuple("White", 180000));
        assertThat(bySize.getTotalElements()).isEqualTo(2);

        Page<ProductSearchResponseDto> byPrice = search(null, null, null, null, null, null, 100000, 200000, null, 0, 20);
        assertThat(byPrice.getContent()).extracting(ProductSearchResponseDto::getColorId)
                .containsExactlyInAnyOrder(p1White.getId(), p2Black.getId());
        assertThat(byPrice.getTotalElements()).isEqualTo(2);
    }

    @Test
    void sortByPriceAndReleaseDate() {
        assertThat(search(null, null, null, null, null, null, null, null, new SortOption("price", "asc"), 0, 20).getContent())
                .extracting(ProductSearchResponseDto::getColorId)
                .containsExactly(p3Grey.getId(), p2Black.getId(), p1White.getId(), p1Black.getId());
        assertThat(search(null, null, null, null, null, null, null, null, new SortOption("price", "desc"), 0, 20).getContent())
                .extracting(ProductSearchResponseDto::getColorId)
                .containsExactly(p1Black.getId(), p1White.getId(), p2Black.getId(), p3Grey.getId());
        assertThat(search(null, null, null, null, null, null, null, null, new SortOption("releaseDate", "desc"), 0, 20).getContent())
                .extracting(ProductSearchResponseDto::getId)
                .containsExactly(p2.getId(), p1.getId(), p1.getId(), p3.getId());
    }

    @Test
    void pagingAppliesToColorRowsWhileCountQueryCountsDistinctProducts() {
        Page<ProductSearchResponseDto> page0 = search(null, null, null, null, null, null, null, null, new SortOption("price", "asc"), 0, 2);
        assertThat(page0.getContent()).extracting(ProductSearchResponseDto::getColorId)
                .containsExactly(p3Grey.getId(), p2Black.getId());
        // 중간 페이지에서는 count 쿼리 값(상품 수 3)이 그대로 쓰인다
        assertThat(page0.getTotalElements()).isEqualTo(3);
        assertThat(page0.getTotalPages()).isEqualTo(2);

        Page<ProductSearchResponseDto> page1 = search(null, null, null, null, null, null, null, null, new SortOption("price", "asc"), 1, 2);
        assertThat(page1.getContent()).extracting(ProductSearchResponseDto::getColorId)
                .containsExactly(p1White.getId(), p1Black.getId());
        // 마지막 페이지에서는 offset(2) + 행 수(2)
        assertThat(page1.getTotalElements()).isEqualTo(4);
    }

    @Test
    void countProductsByFilterIgnoresCollectionFilter() {
        assertThat(repository.countProductsByFilter(null, null, null, null, null, null, null, null, null)).isEqualTo(3);
        assertThat(repository.countProductsByFilter(null, null, null, null, List.of(-1L), null, null, null, null)).isEqualTo(3);
        assertThat(repository.countProductsByFilter(null, null, null, List.of(nike.getId()), null, null, null, null, null)).isEqualTo(2);
    }

    @Test
    void productDetailCollectsSizesBySameColorNameAcrossProducts() {
        ProductDetailResponseDto detail = repository.findProductDetail(p1.getId(), "Black");

        assertThat(detail.getId()).isEqualTo(p1.getId());
        assertThat(detail.getColorId()).isEqualTo(p1Black.getId());
        assertThat(detail.getBrandName()).isEqualTo("Nike");
        assertThat(detail.getThumbnailImageUrl()).isEqualTo("p1-black.jpg");
        assertThat(detail.getContent()).isEqualTo("<p>black</p>");
        assertThat(detail.getInterestCount()).isEqualTo(1L);
        // 같은 colorName("Black")을 가진 P2의 사이즈 270도 포함된다
        assertThat(detail.getSizes()).extracting(ProductDetailResponseDto.SizeDetailDto::getSize)
                .containsExactlyInAnyOrder("250", "260", "270");
        assertThat(detail.getOtherColors()).extracting(ProductDetailResponseDto.ColorDetailDto::getColorName,
                        ProductDetailResponseDto.ColorDetailDto::getThumbnailImageUrl)
                .containsExactly(tuple("White", "p1-white.jpg"));
    }

    @Test
    void productDetailForUnknownColorThrowsIllegalArgument() {
        assertThatThrownBy(() -> repository.findProductDetail(p1.getId(), "Grey"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 상품 또는 색상이 존재하지 않습니다.");
    }

    @Test
    void searchByColorIdsDropsColorsWithoutThumbnail() {
        assertThat(repository.searchProductsByColorIds(Collections.emptyList(), null, PageRequest.of(0, 10)).getTotalElements())
                .isZero();

        Page<ProductSearchResponseDto> result = repository.searchProductsByColorIds(
                List.of(p3Grey.getId(), p2Black.getId()), new SortOption("price", "desc"), PageRequest.of(0, 10));
        // 썸네일 경로가 내부 조인이 되어 썸네일 없는 P3 Grey는 빠진다
        assertThat(result.getContent()).extracting(ProductSearchResponseDto::getColorId)
                .containsExactly(p2Black.getId());
        assertThat(result.getContent().get(0).getPrice()).isEqualTo(120000);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}
