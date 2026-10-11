package com.fream.back.visible.c03;

import com.fream.back.domain.m16.dto.Xzj27ResponseDto;
import com.fream.back.domain.m16.entity.Xtz73Status;
import com.fream.back.domain.m16.entity.Xzj27;
import com.fream.back.domain.m16.service.query.Xzj27QueryService;
import com.fream.back.domain.m02.entity.Xkr46;
import com.fream.back.domain.m02.entity.Xfp92;
import com.fream.back.domain.m02.entity.Xzq99;
import com.fream.back.domain.m02.entity.Xdh20;
import com.fream.back.domain.m02.entity.enumType.Xbj22Type;
import com.fream.back.domain.m02.entity.enumType.Xnp24Type;
import com.fream.back.domain.m13.entity.Xrd81;
import com.fream.back.global.config.Xsd33Config;
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
@Import({Xsd33Config.class, Xzj27QueryService.class})
class PurchaseBidHistoryPagingTest {

    @Autowired EntityManager em;
    @Autowired Xzj27QueryService orderBidQueryService;

    private final List<Long> bidsOnListedProducts = new ArrayList<>();

    @BeforeEach
    void setUp() {
        Xrd81 buyer = Xrd81.builder().email("buyer@test.com").password("pw").referralCode("REF-B")
                .phoneNumber("010-1111-2222").build();
        em.persist(buyer);
        Xrd81 other = Xrd81.builder().email("other@test.com").password("pw").referralCode("REF-O")
                .phoneNumber("010-3333-4444").build();
        em.persist(other);

        Xkr46 product = Xkr46.builder().name("덩크 로우").englishName("Dunk Low").releasePrice(129000)
                .modelNumber("DD1391").releaseDate("2024-01-01").gender(Xbj22Type.UNISEX).build();
        em.persist(product);

        Xdh20 black = size(color(product, "Black", "black.jpg"), "260");
        Xdh20 white = size(color(product, "White", "white.jpg"), "270");
        Xdh20 green = size(color(product, "Green", null), "250");

        bidsOnListedProducts.add(bid(buyer, black, 150000));
        bidsOnListedProducts.add(bid(buyer, white, 140000));
        bid(buyer, green, 130000);
        bidsOnListedProducts.add(bid(buyer, black, 120000));
        bid(buyer, green, 110000);
        bid(other, black, 100000);

        em.flush();
        em.clear();
    }

    private Xfp92 color(Xkr46 product, String name, String imageUrl) {
        Xfp92 color = Xfp92.builder().colorName(name).product(product).build();
        em.persist(color);
        if (imageUrl != null) {
            Xzq99 image = Xzq99.builder().imageUrl(imageUrl).productColor(color).build();
            em.persist(image);
            color.addThumbnailImage(image);
        }
        return color;
    }

    private Xdh20 size(Xfp92 color, String size) {
        Xdh20 productSize = Xdh20.builder().sizeType(Xnp24Type.SHOES).size(size)
                .purchasePrice(100000).salePrice(90000).quantity(5).productColor(color).build();
        em.persist(productSize);
        return productSize;
    }

    private Long bid(Xrd81 user, Xdh20 size, int price) {
        Xzj27 bid = Xzj27.builder().user(user).productSize(size).bidPrice(price)
                .status(Xtz73Status.PENDING).build();
        em.persist(bid);
        return bid.getId();
    }

    @Test
    void everyPageReportsTheSameTotalAsTheRowsReachableByPaging() {
        List<Long> totals = new ArrayList<>();
        List<Long> seen = new ArrayList<>();
        for (int page = 0; page < 10; page++) {
            Page<Xzj27ResponseDto> result =
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
