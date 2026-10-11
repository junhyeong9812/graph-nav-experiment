package com.fream.back.visible.t05;

import com.fream.back.domain.order.entity.BidStatus;
import com.fream.back.domain.order.entity.Order;
import com.fream.back.domain.order.entity.OrderBid;
import com.fream.back.domain.order.entity.OrderStatus;
import com.fream.back.domain.product.entity.Product;
import com.fream.back.domain.product.entity.ProductColor;
import com.fream.back.domain.product.entity.ProductSize;
import com.fream.back.domain.product.entity.enumType.GenderType;
import com.fream.back.domain.product.entity.enumType.SizeType;
import com.fream.back.domain.sale.entity.Sale;
import com.fream.back.domain.sale.entity.SaleBid;
import com.fream.back.domain.sale.entity.SaleStatus;
import com.fream.back.domain.shipment.entity.OrderShipment;
import com.fream.back.domain.shipment.entity.ShipmentStatus;
import com.fream.back.domain.shipment.service.command.OrderShipmentCommandService;
import com.fream.back.domain.user.entity.Point;
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
 * 배송 완료 시 구매 금액의 1%가 구매자에게 포인트로 적립되는지 본다.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
class PurchaseRewardTest {

    @Autowired EntityManager em;
    @Autowired ApplicationContext ctx;

    private static int seq = 0;

    private User user(String email) {
        seq++;
        User u = User.builder().email(email).password("pw").referralCode("v5-" + seq)
                .phoneNumber("010-5000-" + seq).build();
        em.persist(u);
        return u;
    }

    /** 배송 중인 주문 하나(판매 입찰·구매 입찰이 연결된 상태)를 만들고 주문 ID를 돌려준다. */
    private Long orderInTransit(User buyer, User seller, int totalAmount) {
        Product product = Product.builder().name("덩크 로우").englishName("Dunk Low").releasePrice(139000)
                .modelNumber("DD-" + seq).releaseDate("2024-01-01").gender(GenderType.UNISEX).build();
        em.persist(product);
        ProductColor color = ProductColor.builder().colorName("Panda").product(product).build();
        em.persist(color);
        ProductSize size = ProductSize.builder().sizeType(SizeType.SHOES).size("270")
                .purchasePrice(totalAmount).salePrice(totalAmount).quantity(1).productColor(color).build();
        em.persist(size);

        Order order = Order.builder().user(buyer).totalAmount(totalAmount).discountAmount(0).usedPoints(0)
                .status(OrderStatus.IN_TRANSIT).build();
        em.persist(order);
        em.persist(OrderShipment.builder().order(order).receiverName("구매자").receiverPhone("010-1111-2222")
                .postalCode("12345").address("서울").courier("CJ").trackingNumber("T-" + seq)
                .status(ShipmentStatus.IN_TRANSIT).build());

        Sale sale = Sale.builder().seller(seller).productSize(size).status(SaleStatus.SELLING).build();
        em.persist(sale);
        em.persist(SaleBid.builder().seller(seller).productSize(size).bidPrice(totalAmount)
                .status(com.fream.back.domain.sale.entity.BidStatus.MATCHED).sale(sale).order(order).build());
        em.persist(OrderBid.builder().user(buyer).productSize(size).bidPrice(totalAmount)
                .status(BidStatus.MATCHED).order(order).sale(sale).build());
        em.flush();
        em.clear();
        return order.getId();
    }

    private List<Point> pointsOf(User u) {
        return em.createQuery("select p from Point p where p.user.id = :uid", Point.class)
                .setParameter("uid", u.getId()).getResultList();
    }

    @Test
    void deliveredOrderGivesBuyerOnePercentPoints() {
        User buyer = user("buyer-v5@test.com");
        User seller = user("seller-v5@test.com");
        Long orderId = orderInTransit(buyer, seller, 123_456);

        new Wiring(ctx).get(OrderShipmentCommandService.class).completeOrder(orderId);
        em.flush();
        em.clear();

        List<Point> points = pointsOf(buyer);
        assertThat(points).hasSize(1);
        assertThat(points.get(0).getAmount()).isEqualTo(1234);
        assertThat(points.get(0).getRemainingAmount()).isEqualTo(1234);
        assertThat(em.find(Order.class, orderId).getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }
}
