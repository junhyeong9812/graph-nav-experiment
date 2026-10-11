package com.fream.back.visible.t06;

import com.fream.back.domain.m15.entity.Xkh73;
import com.fream.back.domain.m15.entity.Xkh73Type;
import com.fream.back.domain.m02.entity.Xrf21;
import com.fream.back.domain.m02.entity.Xkr46;
import com.fream.back.domain.m02.entity.Xfp92;
import com.fream.back.domain.m02.entity.Xdh20;
import com.fream.back.domain.m02.entity.enumType.Xbj22Type;
import com.fream.back.domain.m02.entity.enumType.Xnp24Type;
import com.fream.back.domain.m10.service.command.Xzz58CommandService;
import com.fream.back.domain.m13.entity.Xbr34;
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
class ListingAlertTest {

    @Autowired EntityManager em;
    @Autowired ApplicationContext ctx;

    private static int seq = 0;

    private Xrd81 user(String prefix) {
        seq++;
        Xrd81 u = Xrd81.builder().email(prefix + "-v6-" + seq + "@test.com").password("pw").referralCode("v6-" + seq)
                .phoneNumber("010-6000-" + seq).build();
        em.persist(u);
        return u;
    }

    private List<Xkh73> favoriteAlertsOf(Xrd81 u) {
        return em.createQuery("select n from Xkh73 n where n.user.id = :uid", Xkh73.class)
                .setParameter("uid", u.getId()).getResultList().stream()
                .filter(n -> n.getType() == Xkh73Type.FAVORITE).toList();
    }

    @Test
    void newListingAlertsUsersInterestedInThatColorOnly() {
        Xrd81 seller = user("seller");
        em.persist(Xbr34.builder().user(seller).bankName("국민").accountNumber("111-222").accountHolder("판매자").build());
        Xrd81 whiteFan = user("white-fan");
        Xrd81 blackFan = user("black-fan");

        Xkr46 product = Xkr46.builder().name("에어 포스 1").englishName("Air Force 1").releasePrice(139000)
                .modelNumber("AF-1").releaseDate("2024-01-01").gender(Xbj22Type.UNISEX).build();
        em.persist(product);
        Xfp92 white = Xfp92.builder().colorName("White").product(product).build();
        Xfp92 black = Xfp92.builder().colorName("Black").product(product).build();
        em.persist(white);
        em.persist(black);
        Xdh20 white260 = Xdh20.builder().sizeType(Xnp24Type.SHOES).size("260")
                .purchasePrice(139000).salePrice(139000).quantity(1).productColor(white).build();
        em.persist(white260);
        em.persist(Xrf21.builder().user(whiteFan).productColor(white).build());
        em.persist(Xrf21.builder().user(blackFan).productColor(black).build());
        em.flush();
        em.clear();

        new Wiring(ctx).get(Xzz58CommandService.class)
                .createSaleBid(seller.getEmail(), white260.getId(), 150000, "서울", "12345", "010-0000-0000", false);
        em.flush();
        em.clear();

        List<Xkh73> alerts = favoriteAlertsOf(whiteFan);
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getMessage()).contains("에어 포스 1");
        assertThat(favoriteAlertsOf(blackFan)).isEmpty();
    }
}
