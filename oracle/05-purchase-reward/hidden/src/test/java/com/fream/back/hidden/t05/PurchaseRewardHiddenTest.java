package com.fream.back.hidden.t05;

import com.fream.back.domain.notification.entity.Notification;
import com.fream.back.domain.notification.entity.NotificationCategory;
import com.fream.back.domain.notification.entity.NotificationType;
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
import com.fream.back.domain.user.entity.PointStatus;
import com.fream.back.domain.user.entity.User;
import com.fream.back.global.config.QueryDslConfig;
import com.fream.back.global.utils.CjTrackingPlaywright;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 배송 완료 구매 적립: 모든 배송 완료 경로에서 주문당 1회, 주문 총액 1%(버림), 혜택 알림, 기존 동작 보존.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
class PurchaseRewardHiddenTest {

    @Autowired EntityManager em;
    @Autowired ApplicationContext ctx;

    private static int seq = 0;

    private CjTrackingPlaywright tracker;
    private OrderShipmentCommandService service;
    private User buyer;
    private User seller;

    @BeforeEach
    void setUp() {
        tracker = Mockito.mock(CjTrackingPlaywright.class);
        service = new Wiring(ctx).with(CjTrackingPlaywright.class, tracker).get(OrderShipmentCommandService.class);
        buyer = user("buyer");
        seller = user("seller");
        em.flush();
    }

    private User user(String prefix) {
        seq++;
        User u = User.builder().email(prefix + "-h5-" + seq + "@test.com").password("pw").referralCode("h5-" + seq)
                .phoneNumber("010-5500-" + seq).build();
        em.persist(u);
        return u;
    }

    private record Fixture(Long orderId, Long shipmentId, Long saleId, String tracking) {}

    /** 배송 중인 주문(판매·판매 입찰·구매 입찰 연결)을 만든다. */
    private Fixture orderInTransit(int totalAmount) {
        seq++;
        Product product = Product.builder().name("에어 맥스 " + seq).englishName("Air Max").releasePrice(179000)
                .modelNumber("AM-" + seq).releaseDate("2024-03-01").gender(GenderType.UNISEX).build();
        em.persist(product);
        ProductColor color = ProductColor.builder().colorName("Grey").product(product).build();
        em.persist(color);
        ProductSize size = ProductSize.builder().sizeType(SizeType.SHOES).size("265")
                .purchasePrice(totalAmount).salePrice(totalAmount).quantity(1).productColor(color).build();
        em.persist(size);

        Order order = Order.builder().user(buyer).totalAmount(totalAmount).discountAmount(0).usedPoints(0)
                .status(OrderStatus.IN_TRANSIT).build();
        em.persist(order);
        String tracking = "TRK-" + seq;
        OrderShipment shipment = OrderShipment.builder().order(order).receiverName("구매자").receiverPhone("010-1111-2222")
                .postalCode("12345").address("서울").courier("CJ").trackingNumber(tracking)
                .status(ShipmentStatus.IN_TRANSIT).build();
        em.persist(shipment);

        Sale sale = Sale.builder().seller(seller).productSize(size).status(SaleStatus.SELLING).build();
        em.persist(sale);
        em.persist(SaleBid.builder().seller(seller).productSize(size).bidPrice(totalAmount)
                .status(com.fream.back.domain.sale.entity.BidStatus.MATCHED).sale(sale).order(order).build());
        em.persist(OrderBid.builder().user(buyer).productSize(size).bidPrice(totalAmount)
                .status(BidStatus.MATCHED).order(order).sale(sale).build());
        em.flush();
        return new Fixture(order.getId(), shipment.getId(), sale.getId(), tracking);
    }

    private void sync() {
        em.flush();
        em.clear();
    }

    private List<Point> pointsOf(User u) {
        return em.createQuery("select p from Point p where p.user.id = :uid order by p.amount", Point.class)
                .setParameter("uid", u.getId()).getResultList();
    }

    private List<Notification> notificationsOf(User u) {
        return em.createQuery("select n from Notification n where n.user.id = :uid", Notification.class)
                .setParameter("uid", u.getId()).getResultList();
    }

    private List<Notification> benefitNotificationsOf(User u) {
        return notificationsOf(u).stream().filter(n -> n.getType() == NotificationType.BENEFIT).toList();
    }

    private static boolean mentionsAmount(String message, String plain, String comma) {
        return message != null && (message.contains(plain) || message.contains(comma));
    }

    @Test
    void completeOrderRewardsOnePercentFlooredWithStandardValidity() {
        Fixture f = orderInTransit(123_456);
        sync();

        service.completeOrder(f.orderId());
        sync();

        List<Point> points = pointsOf(buyer);
        assertThat(points).hasSize(1);
        Point p = points.get(0);
        assertThat(p.getAmount()).isEqualTo(1234);
        assertThat(p.getRemainingAmount()).isEqualTo(1234);
        assertThat(p.getStatus()).isEqualTo(PointStatus.AVAILABLE);
        assertThat(p.getExpirationDate()).isEqualTo(LocalDate.now().plusDays(90));
        assertThat(em.find(Order.class, f.orderId()).getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void completeOrderSendsBenefitNotificationAndKeepsExistingOnes() {
        Fixture f = orderInTransit(123_456);
        sync();

        service.completeOrder(f.orderId());
        sync();

        List<Notification> benefit = benefitNotificationsOf(buyer);
        assertThat(benefit).hasSize(1);
        assertThat(benefit.get(0).getCategory()).isEqualTo(NotificationCategory.SHOPPING);
        assertThat(mentionsAmount(benefit.get(0).getMessage(), "1234", "1,234")).isTrue();

        // 기존 주문 완료 알림(구매자)과 판매 완료 알림(판매자)은 그대로 남는다
        assertThat(notificationsOf(buyer)).anyMatch(n -> n.getType() == NotificationType.BID
                && n.getMessage().contains("주문이 완료"));
        assertThat(notificationsOf(seller)).anyMatch(n -> n.getMessage().contains("판매 완료"));
        assertThat(em.find(Sale.class, f.saleId()).getStatus()).isEqualTo(SaleStatus.SOLD);
    }

    @Test
    void sellerReceivesNoPurchaseReward() {
        Fixture f = orderInTransit(80_000);
        sync();

        service.completeOrder(f.orderId());
        sync();

        assertThat(pointsOf(seller)).isEmpty();
        assertThat(benefitNotificationsOf(seller)).isEmpty();
        assertThat(pointsOf(buyer)).extracting(Point::getAmount).containsExactly(800);
    }

    @Test
    void periodicTrackingUpdateRewardsWhenDelivered() throws Exception {
        Fixture f = orderInTransit(50_000);
        sync();
        when(tracker.getCurrentTrackingStatus(anyString())).thenReturn("배송완료");

        service.updateShipmentStatuses();
        sync();

        assertThat(em.find(OrderShipment.class, f.shipmentId()).getStatus()).isEqualTo(ShipmentStatus.DELIVERED);
        assertThat(em.find(Order.class, f.orderId()).getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(pointsOf(buyer)).extracting(Point::getAmount).containsExactly(500);
        assertThat(benefitNotificationsOf(buyer)).hasSize(1);
        assertThat(mentionsAmount(benefitNotificationsOf(buyer).get(0).getMessage(), "500", "500")).isTrue();
    }

    @Test
    void periodicTrackingUpdateRewardsEachDeliveredOrderSeparately() throws Exception {
        orderInTransit(30_000);
        orderInTransit(70_000);
        sync();
        when(tracker.getCurrentTrackingStatus(anyString())).thenReturn("배송완료");

        service.updateShipmentStatuses();
        sync();

        assertThat(pointsOf(buyer)).extracting(Point::getAmount).containsExactly(300, 700);
    }

    @Test
    void periodicTrackingUpdateDoesNotRewardBeforeDelivery() throws Exception {
        Fixture f = orderInTransit(50_000);
        sync();
        when(tracker.getCurrentTrackingStatus(anyString())).thenReturn("배송출발");

        service.updateShipmentStatuses();
        sync();

        assertThat(em.find(OrderShipment.class, f.shipmentId()).getStatus()).isEqualTo(ShipmentStatus.OUT_FOR_DELIVERY);
        assertThat(em.find(Order.class, f.orderId()).getStatus()).isEqualTo(OrderStatus.IN_TRANSIT);
        assertThat(pointsOf(buyer)).isEmpty();
        assertThat(benefitNotificationsOf(buyer)).isEmpty();
    }

    @Test
    void immediateTrackingCheckRewardsExactlyOnce() throws Exception {
        Fixture f = orderInTransit(100_000);
        sync();
        when(tracker.getCurrentTrackingStatus(anyString())).thenReturn("배송완료");

        ShipmentStatus result = service.updateAndCheckShipmentStatus(f.shipmentId(), "CJ", f.tracking());
        sync();

        assertThat(result).isEqualTo(ShipmentStatus.DELIVERED);
        assertThat(pointsOf(buyer)).extracting(Point::getAmount).containsExactly(1000);
        assertThat(benefitNotificationsOf(buyer)).hasSize(1);
    }

    @Test
    void rewardBelowOnePointIsSkippedButOrderStillCompletes() {
        Fixture f = orderInTransit(99);
        sync();

        service.completeOrder(f.orderId());
        sync();

        assertThat(em.find(Order.class, f.orderId()).getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(pointsOf(buyer)).isEmpty();
        assertThat(benefitNotificationsOf(buyer)).isEmpty();
        assertThat(notificationsOf(buyer)).anyMatch(n -> n.getMessage().contains("주문이 완료"));
    }
}
