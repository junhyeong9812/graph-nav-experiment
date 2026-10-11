package com.fream.back.visible.t06;

import com.fream.back.domain.payment.entity.Payment;
import com.fream.back.domain.payment.entity.PaymentType;
import com.fream.back.domain.user.entity.Interest;
import com.fream.back.domain.user.entity.User;
import com.fream.back.domain.user.entity.UserColor;
import com.fream.back.domain.user.entity.UserSize;
import com.fream.back.domain.user.entity.enumType.GenderType;
import com.fream.back.domain.user.entity.enumType.SizeType;
import com.fream.back.domain.style.service.command.StyleBidCommandService;
import com.fream.back.domain.product.entity.BankAccount;
import com.fream.back.domain.product.entity.Product;
import com.fream.back.global.config.QueryDslConfig;
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
@Import(QueryDslConfig.class)
class ListingAlertTest {

    @Autowired EntityManager em;
    @Autowired ApplicationContext ctx;

    private static int seq = 0;

    private Product user(String prefix) {
        seq++;
        Product u = Product.builder().email(prefix + "-v6-" + seq + "@test.com").password("pw").referralCode("v6-" + seq)
                .phoneNumber("010-6000-" + seq).build();
        em.persist(u);
        return u;
    }

    private List<Payment> favoriteAlertsOf(Product u) {
        return em.createQuery("select n from Payment n where n.user.id = :uid", Payment.class)
                .setParameter("uid", u.getId()).getResultList().stream()
                .filter(n -> n.getType() == PaymentType.FAVORITE).toList();
    }

    @Test
    void newListingAlertsUsersInterestedInThatColorOnly() {
        Product seller = user("seller");
        em.persist(BankAccount.builder().user(seller).bankName("국민").accountNumber("111-222").accountHolder("판매자").build());
        Product whiteFan = user("white-fan");
        Product blackFan = user("black-fan");

        User product = User.builder().name("에어 포스 1").englishName("Air Force 1").releasePrice(139000)
                .modelNumber("AF-1").releaseDate("2024-01-01").gender(GenderType.UNISEX).build();
        em.persist(product);
        UserColor white = UserColor.builder().colorName("White").product(product).build();
        UserColor black = UserColor.builder().colorName("Black").product(product).build();
        em.persist(white);
        em.persist(black);
        UserSize white260 = UserSize.builder().sizeType(SizeType.SHOES).size("260")
                .purchasePrice(139000).salePrice(139000).quantity(1).productColor(white).build();
        em.persist(white260);
        em.persist(Interest.builder().user(whiteFan).productColor(white).build());
        em.persist(Interest.builder().user(blackFan).productColor(black).build());
        em.flush();
        em.clear();

        new Wiring(ctx).get(StyleBidCommandService.class)
                .createSaleBid(seller.getEmail(), white260.getId(), 150000, "서울", "12345", "010-0000-0000", false);
        em.flush();
        em.clear();

        List<Payment> alerts = favoriteAlertsOf(whiteFan);
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getMessage()).contains("에어 포스 1");
        assertThat(favoriteAlertsOf(blackFan)).isEmpty();
    }
}
