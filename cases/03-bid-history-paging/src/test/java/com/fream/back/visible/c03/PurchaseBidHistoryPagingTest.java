package com.fream.back.visible.c03;

import com.fream.back.domain.order.dto.OrderBidResponseDto;
import com.fream.back.domain.order.entity.BidStatus;
import com.fream.back.domain.order.entity.OrderBid;
import com.fream.back.domain.order.service.query.OrderBidQueryService;
import com.fream.back.domain.product.entity.Product;
import com.fream.back.domain.product.entity.ProductColor;
import com.fream.back.domain.product.entity.ProductImage;
import com.fream.back.domain.product.entity.ProductSize;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 마이페이지 구매 입찰 내역: 전체 건수와 넘겨 볼 수 있는 항목 수가 같아야 한다.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({QueryDslConfig.class, OrderBidQueryService.class})
class PurchaseBidHistoryPagingTest {

    @Autowired EntityManager em;
    @Autowired OrderBidQueryService orderBidQueryService;

    private final List<Long> bidsOnListedProducts = new ArrayList<>();

    @BeforeEach
    void setUp() {
        User buyer = User.builder().email("buyer@test.com").password("pw").referralCode("REF-B")
                .phoneNumber("010-1111-2222").build();
        em.persist(buyer);
        User other = User.builder().email("other@test.com").password("pw").referralCode("REF-O")
                .phoneNumber("010-3333-4444").build();
        em.persist(other);

        Product product = Product.builder().name("덩크 로우").englishName("Dunk Low").releasePrice(129000)
                .modelNumber("DD1391").releaseDate("2024-01-01").gender(GenderType.UNISEX).build();
        em.persist(product);

        ProductSize black = size(color(product, "Black", "black.jpg"), "260");
        ProductSize white = size(color(product, "White", "white.jpg"), "270");
        ProductSize green = size(color(product, "Green", null), "250");

        bidsOnListedProducts.add(bid(buyer, black, 150000));
        bidsOnListedProducts.add(bid(buyer, white, 140000));
        bid(buyer, green, 130000);
        bidsOnListedProducts.add(bid(buyer, black, 120000));
        bid(buyer, green, 110000);
        bid(other, black, 100000);

        em.flush();
        em.clear();
    }

    private ProductColor color(Product product, String name, String imageUrl) {
        ProductColor color = ProductColor.builder().colorName(name).product(product).build();
        em.persist(color);
        if (imageUrl != null) {
            ProductImage image = ProductImage.builder().imageUrl(imageUrl).productColor(color).build();
            em.persist(image);
            color.addThumbnailImage(image);
        }
        return color;
    }

    private ProductSize size(ProductColor color, String size) {
        ProductSize productSize = ProductSize.builder().sizeType(SizeType.SHOES).size(size)
                .purchasePrice(100000).salePrice(90000).quantity(5).productColor(color).build();
        em.persist(productSize);
        return productSize;
    }

    private Long bid(User user, ProductSize size, int price) {
        OrderBid bid = OrderBid.builder().user(user).productSize(size).bidPrice(price)
                .status(BidStatus.PENDING).build();
        em.persist(bid);
        return bid.getId();
    }

    @Test
    void everyPageReportsTheSameTotalAsTheRowsReachableByPaging() {
        List<Long> totals = new ArrayList<>();
        List<Long> seen = new ArrayList<>();
        for (int page = 0; page < 10; page++) {
            Page<OrderBidResponseDto> result =
                    orderBidQueryService.getOrderBids("buyer@test.com", null, null, PageRequest.of(page, 2));
            totals.add(result.getTotalElements());
            if (result.getContent().isEmpty()) {
                break;
            }
            result.getContent().forEach(r -> seen.add(r.getOrderBidId()));
        }

        Set<Long> distinct = new HashSet<>(seen);
        assertThat(distinct).hasSameSizeAs(seen);
        assertThat(distinct).containsAll(bidsOnListedProducts);
        assertThat(totals).allSatisfy(total -> assertThat(total).isEqualTo((long) seen.size()));
    }
}
