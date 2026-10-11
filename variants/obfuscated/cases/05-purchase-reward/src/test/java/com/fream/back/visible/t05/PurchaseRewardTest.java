package com.fream.back.visible.t05;

import com.fream.back.domain.m16.entity.Xtz73Status;
import com.fream.back.domain.m16.entity.Xdg64;
import com.fream.back.domain.m16.entity.Xzj27;
import com.fream.back.domain.m16.entity.Xdg64Status;
import com.fream.back.domain.m02.entity.Xkr46;
import com.fream.back.domain.m02.entity.Xfp92;
import com.fream.back.domain.m02.entity.Xdh20;
import com.fream.back.domain.m02.entity.enumType.Xbj22Type;
import com.fream.back.domain.m02.entity.enumType.Xnp24Type;
import com.fream.back.domain.m10.entity.Xgg74;
import com.fream.back.domain.m10.entity.Xzz58;
import com.fream.back.domain.m10.entity.Xgg74Status;
import com.fream.back.domain.m12.entity.Xhx14;
import com.fream.back.domain.m12.entity.Xpb98Status;
import com.fream.back.domain.m12.service.command.Xhx14CommandService;
import com.fream.back.domain.m13.entity.Xnj18;
import com.fream.back.domain.m13.entity.Xrd81;
import com.fream.back.global.config.Xsd33Config;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;




@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(Xsd33Config.class)
class PurchaseRewardTest {

    @Autowired EntityManager em;
    @Autowired ApplicationContext ctx;

    private static int seq = 0;

    private Xrd81 user(String email) {
        seq++;
        Xrd81 u = Xrd81.builder().email(email).password("pw").referralCode("v5-" + seq)
                .phoneNumber("010-5000-" + seq).build();
        em.persist(u);
        return u;
    }


    private Long orderInTransit(Xrd81 buyer, Xrd81 seller, int totalAmount) {
        Xkr46 product = Xkr46.builder().name("덩크 로우").englishName("Dunk Low").releasePrice(139000)
                .modelNumber("DD-" + seq).releaseDate("2024-01-01").gender(Xbj22Type.UNISEX).build();
        em.persist(product);
        Xfp92 color = Xfp92.builder().colorName("Panda").product(product).build();
        em.persist(color);
        Xdh20 size = Xdh20.builder().sizeType(Xnp24Type.SHOES).size("270")
                .purchasePrice(totalAmount).salePrice(totalAmount).quantity(1).productColor(color).build();
        em.persist(size);

        Xdg64 order = Xdg64.builder().user(buyer).totalAmount(totalAmount).discountAmount(0).usedPoints(0)
                .status(Xdg64Status.IN_TRANSIT).build();
        em.persist(order);
        em.persist(Xhx14.builder().order(order).receiverName("구매자").receiverPhone("010-1111-2222")
                .postalCode("12345").address("서울").courier("CJ").trackingNumber("T-" + seq)
                .status(Xpb98Status.IN_TRANSIT).build());

        Xgg74 sale = Xgg74.builder().seller(seller).productSize(size).status(Xgg74Status.SELLING).build();
        em.persist(sale);
        em.persist(Xzz58.builder().seller(seller).productSize(size).bidPrice(totalAmount)
                .status(com.fream.back.domain.m10.entity.Xtz73Status.MATCHED).sale(sale).order(order).build());
        em.persist(Xzj27.builder().user(buyer).productSize(size).bidPrice(totalAmount)
                .status(Xtz73Status.MATCHED).order(order).sale(sale).build());
        em.flush();
        em.clear();
        return order.getId();
    }

    private List<Xnj18> pointsOf(Xrd81 u) {
        return em.createQuery("select p from Xnj18 p where p.user.id = :uid", Xnj18.class)
                .setParameter("uid", u.getId()).getResultList();
    }

    @Test
    void deliveredOrderGivesBuyerOnePercentPoints() {
        Xrd81 buyer = user("buyer-v5@test.com");
        Xrd81 seller = user("seller-v5@test.com");
        Long orderId = orderInTransit(buyer, seller, 123_456);

        new Wiring(ctx).get(Xhx14CommandService.class).completeOrder(orderId);
        em.flush();
        em.clear();

        List<Xnj18> points = pointsOf(buyer);
        assertThat(points).hasSize(1);
        assertThat(points.get(0).getAmount()).isEqualTo(1234);
        assertThat(points.get(0).getRemainingAmount()).isEqualTo(1234);
        assertThat(em.find(Xdg64.class, orderId).getStatus()).isEqualTo(Xdg64Status.COMPLETED);
    }
}
