package com.fream.back.visible.t05;

import com.fream.back.domain.m99.entity.Xkh73Status;
import com.fream.back.domain.m99.entity.Xhq12;
import com.fream.back.domain.m99.entity.Xtw92;
import com.fream.back.domain.m99.entity.Xhq12Status;
import com.fream.back.domain.m30.entity.Xnr50;
import com.fream.back.domain.m30.entity.Xvm17;
import com.fream.back.domain.m30.entity.Xcc44;
import com.fream.back.domain.m30.entity.enumType.Xdg35Type;
import com.fream.back.domain.m30.entity.enumType.Xcn23Type;
import com.fream.back.domain.m85.entity.Xpj42;
import com.fream.back.domain.m85.entity.Xzt63;
import com.fream.back.domain.m85.entity.Xpj42Status;
import com.fream.back.domain.m45.entity.Xfr60;
import com.fream.back.domain.m45.entity.Xzn53Status;
import com.fream.back.domain.m45.service.command.Xfr60CommandService;
import com.fream.back.domain.m22.entity.Xmt80;
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
class PurchaseRewardTest {

    @Autowired EntityManager em;
    @Autowired ApplicationContext ctx;

    private static int seq = 0;

    private Xjm45 user(String email) {
        seq++;
        Xjm45 u = Xjm45.builder().email(email).password("pw").referralCode("v5-" + seq)
                .phoneNumber("010-5000-" + seq).build();
        em.persist(u);
        return u;
    }


    private Long orderInTransit(Xjm45 buyer, Xjm45 seller, int totalAmount) {
        Xnr50 product = Xnr50.builder().name("덩크 로우").englishName("Dunk Low").releasePrice(139000)
                .modelNumber("DD-" + seq).releaseDate("2024-01-01").gender(Xdg35Type.UNISEX).build();
        em.persist(product);
        Xvm17 color = Xvm17.builder().colorName("Panda").product(product).build();
        em.persist(color);
        Xcc44 size = Xcc44.builder().sizeType(Xcn23Type.SHOES).size("270")
                .purchasePrice(totalAmount).salePrice(totalAmount).quantity(1).productColor(color).build();
        em.persist(size);

        Xhq12 order = Xhq12.builder().user(buyer).totalAmount(totalAmount).discountAmount(0).usedPoints(0)
                .status(Xhq12Status.IN_TRANSIT).build();
        em.persist(order);
        em.persist(Xfr60.builder().order(order).receiverName("구매자").receiverPhone("010-1111-2222")
                .postalCode("12345").address("서울").courier("CJ").trackingNumber("T-" + seq)
                .status(Xzn53Status.IN_TRANSIT).build());

        Xpj42 sale = Xpj42.builder().seller(seller).productSize(size).status(Xpj42Status.SELLING).build();
        em.persist(sale);
        em.persist(Xzt63.builder().seller(seller).productSize(size).bidPrice(totalAmount)
                .status(com.fream.back.domain.m85.entity.Xkh73Status.MATCHED).sale(sale).order(order).build());
        em.persist(Xtw92.builder().user(buyer).productSize(size).bidPrice(totalAmount)
                .status(Xkh73Status.MATCHED).order(order).sale(sale).build());
        em.flush();
        em.clear();
        return order.getId();
    }

    private List<Xmt80> pointsOf(Xjm45 u) {
        return em.createQuery("select p from Xmt80 p where p.user.id = :uid", Xmt80.class)
                .setParameter("uid", u.getId()).getResultList();
    }

    @Test
    void deliveredOrderGivesBuyerOnePercentPoints() {
        Xjm45 buyer = user("buyer-v5@test.com");
        Xjm45 seller = user("seller-v5@test.com");
        Long orderId = orderInTransit(buyer, seller, 123_456);

        new Wiring(ctx).get(Xfr60CommandService.class).completeOrder(orderId);
        em.flush();
        em.clear();

        List<Xmt80> points = pointsOf(buyer);
        assertThat(points).hasSize(1);
        assertThat(points.get(0).getAmount()).isEqualTo(1234);
        assertThat(points.get(0).getRemainingAmount()).isEqualTo(1234);
        assertThat(em.find(Xhq12.class, orderId).getStatus()).isEqualTo(Xhq12Status.COMPLETED);
    }
}
