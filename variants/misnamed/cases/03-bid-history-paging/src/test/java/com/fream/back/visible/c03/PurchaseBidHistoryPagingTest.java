package com.fream.back.visible.c03;

import com.fream.back.domain.faq.dto.FaqBidResponseDto;
import com.fream.back.domain.faq.entity.BidStatus;
import com.fream.back.domain.faq.entity.FaqBid;
import com.fream.back.domain.faq.service.query.FaqBidQueryService;
import com.fream.back.domain.user.entity.User;
import com.fream.back.domain.user.entity.UserColor;
import com.fream.back.domain.user.entity.UserImage;
import com.fream.back.domain.user.entity.UserSize;
import com.fream.back.domain.user.entity.enumType.GenderType;
import com.fream.back.domain.user.entity.enumType.SizeType;
import com.fream.back.domain.product.entity.Product;
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




@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({QueryDslConfig.class, FaqBidQueryService.class})
class PurchaseBidHistoryPagingTest {

    @Autowired EntityManager em;
    @Autowired FaqBidQueryService orderBidQueryService;

    private final List<Long> bidsOnListedProducts = new ArrayList<>();

    @BeforeEach
    void setUp() {
        Product buyer = Product.builder().email("buyer@test.com").password("pw").referralCode("REF-B")
                .phoneNumber("010-1111-2222").build();
        em.persist(buyer);
        Product other = Product.builder().email("other@test.com").password("pw").referralCode("REF-O")
                .phoneNumber("010-3333-4444").build();
        em.persist(other);

        User product = User.builder().name("덩크 로우").englishName("Dunk Low").releasePrice(129000)
                .modelNumber("DD1391").releaseDate("2024-01-01").gender(GenderType.UNISEX).build();
        em.persist(product);

        UserSize black = size(color(product, "Black", "black.jpg"), "260");
        UserSize white = size(color(product, "White", "white.jpg"), "270");
        UserSize green = size(color(product, "Green", null), "250");

        bidsOnListedProducts.add(bid(buyer, black, 150000));
        bidsOnListedProducts.add(bid(buyer, white, 140000));
        bid(buyer, green, 130000);
        bidsOnListedProducts.add(bid(buyer, black, 120000));
        bid(buyer, green, 110000);
        bid(other, black, 100000);

        em.flush();
        em.clear();
    }

    private UserColor color(User product, String name, String imageUrl) {
        UserColor color = UserColor.builder().colorName(name).product(product).build();
        em.persist(color);
        if (imageUrl != null) {
            UserImage image = UserImage.builder().imageUrl(imageUrl).productColor(color).build();
            em.persist(image);
            color.addThumbnailImage(image);
        }
        return color;
    }

    private UserSize size(UserColor color, String size) {
        UserSize productSize = UserSize.builder().sizeType(SizeType.SHOES).size(size)
                .purchasePrice(100000).salePrice(90000).quantity(5).productColor(color).build();
        em.persist(productSize);
        return productSize;
    }

    private Long bid(Product user, UserSize size, int price) {
        FaqBid bid = FaqBid.builder().user(user).productSize(size).bidPrice(price)
                .status(BidStatus.PENDING).build();
        em.persist(bid);
        return bid.getId();
    }

    @Test
    void everyPageReportsTheSameTotalAsTheRowsReachableByPaging() {
        List<Long> totals = new ArrayList<>();
        List<Long> seen = new ArrayList<>();
        for (int page = 0; page < 10; page++) {
            Page<FaqBidResponseDto> result =
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
