package com.fream.back.visible.t06;

import com.fream.back.domain.m52.entity.Xbp24;
import com.fream.back.domain.m52.entity.Xbp24Type;
import com.fream.back.domain.m30.entity.Xrc72;
import com.fream.back.domain.m30.entity.Xnr50;
import com.fream.back.domain.m30.entity.Xvm17;
import com.fream.back.domain.m30.entity.Xcc44;
import com.fream.back.domain.m30.entity.enumType.Xdg35Type;
import com.fream.back.domain.m30.entity.enumType.Xcn23Type;
import com.fream.back.domain.m85.service.command.Xzt63CommandService;
import com.fream.back.domain.m22.entity.Xfs39;
import com.fream.back.domain.m22.entity.Xjm45;
import com.fream.back.global.config.Xzj77Config;
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
@Import(Xzj77Config.class)
class ListingAlertTest {

    @Autowired EntityManager em;
    @Autowired ApplicationContext ctx;

    private static int seq = 0;

    private Xjm45 user(String prefix) {
        seq++;
        Xjm45 u = Xjm45.builder().email(prefix + "-v6-" + seq + "@test.com").password("pw").referralCode("v6-" + seq)
                .phoneNumber("010-6000-" + seq).build();
        em.persist(u);
        return u;
    }

    private List<Xbp24> favoriteAlertsOf(Xjm45 u) {
        return em.createQuery("select n from Xbp24 n where n.user.id = :uid", Xbp24.class)
                .setParameter("uid", u.getId()).getResultList().stream()
                .filter(n -> n.getType() == Xbp24Type.FAVORITE).toList();
    }

    @Test
    void newListingAlertsUsersInterestedInThatColorOnly() {
        Xjm45 seller = user("seller");
        em.persist(Xfs39.builder().user(seller).bankName("국민").accountNumber("111-222").accountHolder("판매자").build());
        Xjm45 whiteFan = user("white-fan");
        Xjm45 blackFan = user("black-fan");

        Xnr50 product = Xnr50.builder().name("에어 포스 1").englishName("Air Force 1").releasePrice(139000)
                .modelNumber("AF-1").releaseDate("2024-01-01").gender(Xdg35Type.UNISEX).build();
        em.persist(product);
        Xvm17 white = Xvm17.builder().colorName("White").product(product).build();
        Xvm17 black = Xvm17.builder().colorName("Black").product(product).build();
        em.persist(white);
        em.persist(black);
        Xcc44 white260 = Xcc44.builder().sizeType(Xcn23Type.SHOES).size("260")
                .purchasePrice(139000).salePrice(139000).quantity(1).productColor(white).build();
        em.persist(white260);
        em.persist(Xrc72.builder().user(whiteFan).productColor(white).build());
        em.persist(Xrc72.builder().user(blackFan).productColor(black).build());
        em.flush();
        em.clear();

        new Wiring(ctx).get(Xzt63CommandService.class)
                .createSaleBid(seller.getEmail(), white260.getId(), 150000, "서울", "12345", "010-0000-0000", false);
        em.flush();
        em.clear();

        List<Xbp24> alerts = favoriteAlertsOf(whiteFan);
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getMessage()).contains("에어 포스 1");
        assertThat(favoriteAlertsOf(blackFan)).isEmpty();
    }
}
