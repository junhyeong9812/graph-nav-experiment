package com.fream.back.visible.c03;

import com.fream.back.domain.m99.dto.Xtw92ResponseDto;
import com.fream.back.domain.m99.entity.Xkh73Status;
import com.fream.back.domain.m99.entity.Xtw92;
import com.fream.back.domain.m99.service.query.Xtw92QueryService;
import com.fream.back.domain.m30.entity.Xnr50;
import com.fream.back.domain.m30.entity.Xvm17;
import com.fream.back.domain.m30.entity.Xdt35;
import com.fream.back.domain.m30.entity.Xcc44;
import com.fream.back.domain.m30.entity.enumType.Xdg35Type;
import com.fream.back.domain.m30.entity.enumType.Xcn23Type;
import com.fream.back.domain.m22.entity.Xjm45;
import com.fream.back.global.config.Xzj77Config;
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
@Import({Xzj77Config.class, Xtw92QueryService.class})
class PurchaseBidHistoryPagingTest {

    @Autowired EntityManager em;
    @Autowired Xtw92QueryService orderBidQueryService;

    private final List<Long> bidsOnListedProducts = new ArrayList<>();

    @BeforeEach
    void setUp() {
        Xjm45 buyer = Xjm45.builder().email("buyer@test.com").password("pw").referralCode("REF-B")
                .phoneNumber("010-1111-2222").build();
        em.persist(buyer);
        Xjm45 other = Xjm45.builder().email("other@test.com").password("pw").referralCode("REF-O")
                .phoneNumber("010-3333-4444").build();
        em.persist(other);

        Xnr50 product = Xnr50.builder().name("덩크 로우").englishName("Dunk Low").releasePrice(129000)
                .modelNumber("DD1391").releaseDate("2024-01-01").gender(Xdg35Type.UNISEX).build();
        em.persist(product);

        Xcc44 black = size(color(product, "Black", "black.jpg"), "260");
        Xcc44 white = size(color(product, "White", "white.jpg"), "270");
        Xcc44 green = size(color(product, "Green", null), "250");

        bidsOnListedProducts.add(bid(buyer, black, 150000));
        bidsOnListedProducts.add(bid(buyer, white, 140000));
        bid(buyer, green, 130000);
        bidsOnListedProducts.add(bid(buyer, black, 120000));
        bid(buyer, green, 110000);
        bid(other, black, 100000);

        em.flush();
        em.clear();
    }

    private Xvm17 color(Xnr50 product, String name, String imageUrl) {
        Xvm17 color = Xvm17.builder().colorName(name).product(product).build();
        em.persist(color);
        if (imageUrl != null) {
            Xdt35 image = Xdt35.builder().imageUrl(imageUrl).productColor(color).build();
            em.persist(image);
            color.addThumbnailImage(image);
        }
        return color;
    }

    private Xcc44 size(Xvm17 color, String size) {
        Xcc44 productSize = Xcc44.builder().sizeType(Xcn23Type.SHOES).size(size)
                .purchasePrice(100000).salePrice(90000).quantity(5).productColor(color).build();
        em.persist(productSize);
        return productSize;
    }

    private Long bid(Xjm45 user, Xcc44 size, int price) {
        Xtw92 bid = Xtw92.builder().user(user).productSize(size).bidPrice(price)
                .status(Xkh73Status.PENDING).build();
        em.persist(bid);
        return bid.getId();
    }

    @Test
    void everyPageReportsTheSameTotalAsTheRowsReachableByPaging() {
        List<Long> totals = new ArrayList<>();
        List<Long> seen = new ArrayList<>();
        for (int page = 0; page < 10; page++) {
            Page<Xtw92ResponseDto> result =
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
