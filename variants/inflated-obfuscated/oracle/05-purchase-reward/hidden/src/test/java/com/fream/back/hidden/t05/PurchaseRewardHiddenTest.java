package com.fream.back.hidden.t05;

import com.fream.back.domain.m52.entity.Xbp24;
import com.fream.back.domain.m52.entity.Xrv73;
import com.fream.back.domain.m52.entity.Xbp24Type;
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
import com.fream.back.domain.m22.entity.Xmt80Status;
import com.fream.back.domain.m22.entity.Xjm45;
import com.fream.back.global.config.Xzj77Config;
import com.fream.back.global.utils.Xwg86;
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
@Import(Xzj77Config.class)
class PurchaseRewardHiddenTest {

    @Autowired EntityManager em;
    @Autowired ApplicationContext ctx;

    private static int seq = 0;

    private Xwg86 tracker;
    private Xfr60CommandService service;
    private Xjm45 buyer;
    private Xjm45 seller;

    @BeforeEach
    void setUp() {
        tracker = Mockito.mock(Xwg86.class);
        service = new Wiring(ctx).with(Xwg86.class, tracker).get(Xfr60CommandService.class);
        buyer = user("buyer");
        seller = user("seller");
        em.flush();
    }

    private Xjm45 user(String prefix) {
        seq++;
        Xjm45 u = Xjm45.builder().email(prefix + "-h5-" + seq + "@test.com").password("pw").referralCode("h5-" + seq)
                .phoneNumber("010-5500-" + seq).build();
        em.persist(u);
        return u;
    }

    private record Fixture(Long orderId, Long shipmentId, Long saleId, String tracking) {}

    /** 배송 중인 주문(판매·판매 입찰·구매 입찰 연결)을 만든다. */
    private Fixture orderInTransit(int totalAmount) {
        seq++;
        Xnr50 product = Xnr50.builder().name("에어 맥스 " + seq).englishName("Air Max").releasePrice(179000)
                .modelNumber("AM-" + seq).releaseDate("2024-03-01").gender(Xdg35Type.UNISEX).build();
        em.persist(product);
        Xvm17 color = Xvm17.builder().colorName("Grey").product(product).build();
        em.persist(color);
        Xcc44 size = Xcc44.builder().sizeType(Xcn23Type.SHOES).size("265")
                .purchasePrice(totalAmount).salePrice(totalAmount).quantity(1).productColor(color).build();
        em.persist(size);

        Xhq12 order = Xhq12.builder().user(buyer).totalAmount(totalAmount).discountAmount(0).usedPoints(0)
                .status(Xhq12Status.IN_TRANSIT).build();
        em.persist(order);
        String tracking = "TRK-" + seq;
        Xfr60 shipment = Xfr60.builder().order(order).receiverName("구매자").receiverPhone("010-1111-2222")
                .postalCode("12345").address("서울").courier("CJ").trackingNumber(tracking)
                .status(Xzn53Status.IN_TRANSIT).build();
        em.persist(shipment);

        Xpj42 sale = Xpj42.builder().seller(seller).productSize(size).status(Xpj42Status.SELLING).build();
        em.persist(sale);
        em.persist(Xzt63.builder().seller(seller).productSize(size).bidPrice(totalAmount)
                .status(com.fream.back.domain.m85.entity.Xkh73Status.MATCHED).sale(sale).order(order).build());
        em.persist(Xtw92.builder().user(buyer).productSize(size).bidPrice(totalAmount)
                .status(Xkh73Status.MATCHED).order(order).sale(sale).build());
        em.flush();
        return new Fixture(order.getId(), shipment.getId(), sale.getId(), tracking);
    }

    private void sync() {
        em.flush();
        em.clear();
    }

    private List<Xmt80> pointsOf(Xjm45 u) {
        return em.createQuery("select p from Xmt80 p where p.user.id = :uid order by p.amount", Xmt80.class)
                .setParameter("uid", u.getId()).getResultList();
    }

    private List<Xbp24> notificationsOf(Xjm45 u) {
        return em.createQuery("select n from Xbp24 n where n.user.id = :uid", Xbp24.class)
                .setParameter("uid", u.getId()).getResultList();
    }

    private List<Xbp24> benefitNotificationsOf(Xjm45 u) {
        return notificationsOf(u).stream().filter(n -> n.getType() == Xbp24Type.BENEFIT).toList();
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

        List<Xmt80> points = pointsOf(buyer);
        assertThat(points).hasSize(1);
        Xmt80 p = points.get(0);
        assertThat(p.getAmount()).isEqualTo(1234);
        assertThat(p.getRemainingAmount()).isEqualTo(1234);
        assertThat(p.getStatus()).isEqualTo(Xmt80Status.AVAILABLE);
        assertThat(p.getExpirationDate()).isEqualTo(LocalDate.now().plusDays(90));
        assertThat(em.find(Xhq12.class, f.orderId()).getStatus()).isEqualTo(Xhq12Status.COMPLETED);
    }

    @Test
    void completeOrderSendsBenefitNotificationAndKeepsExistingOnes() {
        Fixture f = orderInTransit(123_456);
        sync();

        service.completeOrder(f.orderId());
        sync();

        List<Xbp24> benefit = benefitNotificationsOf(buyer);
        assertThat(benefit).hasSize(1);
        assertThat(benefit.get(0).getCategory()).isEqualTo(Xrv73.SHOPPING);
        assertThat(mentionsAmount(benefit.get(0).getMessage(), "1234", "1,234")).isTrue();

        // 기존 주문 완료 알림(구매자)과 판매 완료 알림(판매자)은 그대로 남는다
        assertThat(notificationsOf(buyer)).anyMatch(n -> n.getType() == Xbp24Type.BID
                && n.getMessage().contains("주문이 완료"));
        assertThat(notificationsOf(seller)).anyMatch(n -> n.getMessage().contains("판매 완료"));
        assertThat(em.find(Xpj42.class, f.saleId()).getStatus()).isEqualTo(Xpj42Status.SOLD);
    }

    @Test
    void sellerReceivesNoPurchaseReward() {
        Fixture f = orderInTransit(80_000);
        sync();

        service.completeOrder(f.orderId());
        sync();

        assertThat(pointsOf(seller)).isEmpty();
        assertThat(benefitNotificationsOf(seller)).isEmpty();
        assertThat(pointsOf(buyer)).extracting(Xmt80::getAmount).containsExactly(800);
    }

    @Test
    void periodicTrackingUpdateRewardsWhenDelivered() throws Exception {
        Fixture f = orderInTransit(50_000);
        sync();
        when(tracker.getCurrentTrackingStatus(anyString())).thenReturn("배송완료");

        service.updateShipmentStatuses();
        sync();

        assertThat(em.find(Xfr60.class, f.shipmentId()).getStatus()).isEqualTo(Xzn53Status.DELIVERED);
        assertThat(em.find(Xhq12.class, f.orderId()).getStatus()).isEqualTo(Xhq12Status.COMPLETED);
        assertThat(pointsOf(buyer)).extracting(Xmt80::getAmount).containsExactly(500);
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

        assertThat(pointsOf(buyer)).extracting(Xmt80::getAmount).containsExactly(300, 700);
    }

    @Test
    void periodicTrackingUpdateDoesNotRewardBeforeDelivery() throws Exception {
        Fixture f = orderInTransit(50_000);
        sync();
        when(tracker.getCurrentTrackingStatus(anyString())).thenReturn("배송출발");

        service.updateShipmentStatuses();
        sync();

        assertThat(em.find(Xfr60.class, f.shipmentId()).getStatus()).isEqualTo(Xzn53Status.OUT_FOR_DELIVERY);
        assertThat(em.find(Xhq12.class, f.orderId()).getStatus()).isEqualTo(Xhq12Status.IN_TRANSIT);
        assertThat(pointsOf(buyer)).isEmpty();
        assertThat(benefitNotificationsOf(buyer)).isEmpty();
    }

    @Test
    void immediateTrackingCheckRewardsExactlyOnce() throws Exception {
        Fixture f = orderInTransit(100_000);
        sync();
        when(tracker.getCurrentTrackingStatus(anyString())).thenReturn("배송완료");

        Xzn53Status result = service.updateAndCheckShipmentStatus(f.shipmentId(), "CJ", f.tracking());
        sync();

        assertThat(result).isEqualTo(Xzn53Status.DELIVERED);
        assertThat(pointsOf(buyer)).extracting(Xmt80::getAmount).containsExactly(1000);
        assertThat(benefitNotificationsOf(buyer)).hasSize(1);
    }

    @Test
    void rewardBelowOnePointIsSkippedButOrderStillCompletes() {
        Fixture f = orderInTransit(99);
        sync();

        service.completeOrder(f.orderId());
        sync();

        assertThat(em.find(Xhq12.class, f.orderId()).getStatus()).isEqualTo(Xhq12Status.COMPLETED);
        assertThat(pointsOf(buyer)).isEmpty();
        assertThat(benefitNotificationsOf(buyer)).isEmpty();
        assertThat(notificationsOf(buyer)).anyMatch(n -> n.getMessage().contains("주문이 완료"));
    }
}
