package com.fream.back.visible.t05;

import com.fream.back.domain.faq.entity.BidStatus;
import com.fream.back.domain.faq.entity.Faq;
import com.fream.back.domain.faq.entity.FaqBid;
import com.fream.back.domain.faq.entity.FaqStatus;
import com.fream.back.domain.user.entity.User;
import com.fream.back.domain.user.entity.UserColor;
import com.fream.back.domain.user.entity.UserSize;
import com.fream.back.domain.user.entity.enumType.GenderType;
import com.fream.back.domain.user.entity.enumType.SizeType;
import com.fream.back.domain.style.entity.Style;
import com.fream.back.domain.style.entity.StyleBid;
import com.fream.back.domain.style.entity.StyleStatus;
import com.fream.back.domain.address.entity.FaqAddress;
import com.fream.back.domain.address.entity.AddressStatus;
import com.fream.back.domain.address.service.command.FaqAddressCommandService;
import com.fream.back.domain.product.entity.Point;
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
class PurchaseRewardTest {

    @Autowired EntityManager em;
    @Autowired ApplicationContext ctx;

    private static int seq = 0;

    private Product user(String email) {
        seq++;
        Product u = Product.builder().email(email).password("pw").referralCode("v5-" + seq)
                .phoneNumber("010-5000-" + seq).build();
        em.persist(u);
        return u;
    }


    private Long orderInTransit(Product buyer, Product seller, int totalAmount) {
        User product = User.builder().name("덩크 로우").englishName("Dunk Low").releasePrice(139000)
                .modelNumber("DD-" + seq).releaseDate("2024-01-01").gender(GenderType.UNISEX).build();
        em.persist(product);
        UserColor color = UserColor.builder().colorName("Panda").product(product).build();
        em.persist(color);
        UserSize size = UserSize.builder().sizeType(SizeType.SHOES).size("270")
                .purchasePrice(totalAmount).salePrice(totalAmount).quantity(1).productColor(color).build();
        em.persist(size);

        Faq order = Faq.builder().user(buyer).totalAmount(totalAmount).discountAmount(0).usedPoints(0)
                .status(FaqStatus.IN_TRANSIT).build();
        em.persist(order);
        em.persist(FaqAddress.builder().order(order).receiverName("구매자").receiverPhone("010-1111-2222")
                .postalCode("12345").address("서울").courier("CJ").trackingNumber("T-" + seq)
                .status(AddressStatus.IN_TRANSIT).build());

        Style sale = Style.builder().seller(seller).productSize(size).status(StyleStatus.SELLING).build();
        em.persist(sale);
        em.persist(StyleBid.builder().seller(seller).productSize(size).bidPrice(totalAmount)
                .status(com.fream.back.domain.style.entity.BidStatus.MATCHED).sale(sale).order(order).build());
        em.persist(FaqBid.builder().user(buyer).productSize(size).bidPrice(totalAmount)
                .status(BidStatus.MATCHED).order(order).sale(sale).build());
        em.flush();
        em.clear();
        return order.getId();
    }

    private List<Point> pointsOf(Product u) {
        return em.createQuery("select p from Point p where p.user.id = :uid", Point.class)
                .setParameter("uid", u.getId()).getResultList();
    }

    @Test
    void deliveredOrderGivesBuyerOnePercentPoints() {
        Product buyer = user("buyer-v5@test.com");
        Product seller = user("seller-v5@test.com");
        Long orderId = orderInTransit(buyer, seller, 123_456);

        new Wiring(ctx).get(FaqAddressCommandService.class).completeOrder(orderId);
        em.flush();
        em.clear();

        List<Point> points = pointsOf(buyer);
        assertThat(points).hasSize(1);
        assertThat(points.get(0).getAmount()).isEqualTo(1234);
        assertThat(points.get(0).getRemainingAmount()).isEqualTo(1234);
        assertThat(em.find(Faq.class, orderId).getStatus()).isEqualTo(FaqStatus.COMPLETED);
    }
}
