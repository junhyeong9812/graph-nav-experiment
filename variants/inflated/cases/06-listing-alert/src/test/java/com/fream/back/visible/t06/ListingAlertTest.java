package com.fream.back.visible.t06;

import com.fream.back.domain.notification.entity.Notification;
import com.fream.back.domain.notification.entity.NotificationType;
import com.fream.back.domain.product.entity.Interest;
import com.fream.back.domain.product.entity.Product;
import com.fream.back.domain.product.entity.ProductColor;
import com.fream.back.domain.product.entity.ProductSize;
import com.fream.back.domain.product.entity.enumType.GenderType;
import com.fream.back.domain.product.entity.enumType.SizeType;
import com.fream.back.domain.sale.service.command.SaleBidCommandService;
import com.fream.back.domain.user.entity.BankAccount;
import com.fream.back.domain.user.entity.User;
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

/**
 * 새 판매 입찰이 등록되면 그 상품 색상을 관심 등록한 회원이 알림을 받는지 본다.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
class ListingAlertTest {

    @Autowired EntityManager em;
    @Autowired ApplicationContext ctx;

    private static int seq = 0;

    private User user(String prefix) {
        seq++;
        User u = User.builder().email(prefix + "-v6-" + seq + "@test.com").password("pw").referralCode("v6-" + seq)
                .phoneNumber("010-6000-" + seq).build();
        em.persist(u);
        return u;
    }

    private List<Notification> favoriteAlertsOf(User u) {
        return em.createQuery("select n from Notification n where n.user.id = :uid", Notification.class)
                .setParameter("uid", u.getId()).getResultList().stream()
                .filter(n -> n.getType() == NotificationType.FAVORITE).toList();
    }

    @Test
    void newListingAlertsUsersInterestedInThatColorOnly() {
        User seller = user("seller");
        em.persist(BankAccount.builder().user(seller).bankName("국민").accountNumber("111-222").accountHolder("판매자").build());
        User whiteFan = user("white-fan");
        User blackFan = user("black-fan");

        Product product = Product.builder().name("에어 포스 1").englishName("Air Force 1").releasePrice(139000)
                .modelNumber("AF-1").releaseDate("2024-01-01").gender(GenderType.UNISEX).build();
        em.persist(product);
        ProductColor white = ProductColor.builder().colorName("White").product(product).build();
        ProductColor black = ProductColor.builder().colorName("Black").product(product).build();
        em.persist(white);
        em.persist(black);
        ProductSize white260 = ProductSize.builder().sizeType(SizeType.SHOES).size("260")
                .purchasePrice(139000).salePrice(139000).quantity(1).productColor(white).build();
        em.persist(white260);
        em.persist(Interest.builder().user(whiteFan).productColor(white).build());
        em.persist(Interest.builder().user(blackFan).productColor(black).build());
        em.flush();
        em.clear();

        new Wiring(ctx).get(SaleBidCommandService.class)
                .createSaleBid(seller.getEmail(), white260.getId(), 150000, "서울", "12345", "010-0000-0000", false);
        em.flush();
        em.clear();

        List<Notification> alerts = favoriteAlertsOf(whiteFan);
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getMessage()).contains("에어 포스 1");
        assertThat(favoriteAlertsOf(blackFan)).isEmpty();
    }
}
