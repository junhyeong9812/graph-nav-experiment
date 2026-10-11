package com.fream.back.hidden.t05;

import com.fream.back.domain.m15.entity.Xkh73;
import com.fream.back.domain.m15.entity.Xzv64;
import com.fream.back.domain.m15.entity.Xkh73Type;
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
import com.fream.back.domain.m13.entity.Xnj18Status;
import com.fream.back.domain.m13.entity.Xrd81;
import com.fream.back.global.config.Xsd33Config;
import com.fream.back.global.utils.Xxx20;
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
@Import(Xsd33Config.class)
class PurchaseRewardHiddenTest {

    @Autowired EntityManager em;
    @Autowired ApplicationContext ctx;

    private static int seq = 0;

    private Xxx20 tracker;
    private Xhx14CommandService service;
    private Xrd81 buyer;
    private Xrd81 seller;

    @BeforeEach
    void setUp() {
        tracker = Mockito.mock(Xxx20.class);
        service = new Wiring(ctx).with(Xxx20.class, tracker).get(Xhx14CommandService.class);
        buyer = user("buyer");
        seller = user("seller");
        em.flush();
    }

    private Xrd81 user(String prefix) {
        seq++;
        Xrd81 u = Xrd81.builder().email(prefix + "-h5-" + seq + "@test.com").password("pw").referralCode("h5-" + seq)
                .phoneNumber("010-5500-" + seq).build();
        em.persist(u);
        return u;
    }

    private record Fixture(Long orderId, Long shipmentId, Long saleId, String tracking) {}

    /** 배송 중인 주문(판매·판매 입찰·구매 입찰 연결)을 만든다. */
    private Fixture orderInTransit(int totalAmount) {
        seq++;
        Xkr46 product = Xkr46.builder().name("에어 맥스 " + seq).englishName("Air Max").releasePrice(179000)
                .modelNumber("AM-" + seq).releaseDate("2024-03-01").gender(Xbj22Type.UNISEX).build();
        em.persist(product);
        Xfp92 color = Xfp92.builder().colorName("Grey").product(product).build();
        em.persist(color);
        Xdh20 size = Xdh20.builder().sizeType(Xnp24Type.SHOES).size("265")
                .purchasePrice(totalAmount).salePrice(totalAmount).quantity(1).productColor(color).build();
        em.persist(size);

        Xdg64 order = Xdg64.builder().user(buyer).totalAmount(totalAmount).discountAmount(0).usedPoints(0)
                .status(Xdg64Status.IN_TRANSIT).build();
        em.persist(order);
        String tracking = "TRK-" + seq;
        Xhx14 shipment = Xhx14.builder().order(order).receiverName("구매자").receiverPhone("010-1111-2222")
                .postalCode("12345").address("서울").courier("CJ").trackingNumber(tracking)
                .status(Xpb98Status.IN_TRANSIT).build();
        em.persist(shipment);

        Xgg74 sale = Xgg74.builder().seller(seller).productSize(size).status(Xgg74Status.SELLING).build();
        em.persist(sale);
        em.persist(Xzz58.builder().seller(seller).productSize(size).bidPrice(totalAmount)
                .status(com.fream.back.domain.m10.entity.Xtz73Status.MATCHED).sale(sale).order(order).build());
        em.persist(Xzj27.builder().user(buyer).productSize(size).bidPrice(totalAmount)
                .status(Xtz73Status.MATCHED).order(order).sale(sale).build());
        em.flush();
        return new Fixture(order.getId(), shipment.getId(), sale.getId(), tracking);
    }

    private void sync() {
        em.flush();
        em.clear();
    }

    private List<Xnj18> pointsOf(Xrd81 u) {
        return em.createQuery("select p from Xnj18 p where p.user.id = :uid order by p.amount", Xnj18.class)
                .setParameter("uid", u.getId()).getResultList();
    }

    private List<Xkh73> notificationsOf(Xrd81 u) {
        return em.createQuery("select n from Xkh73 n where n.user.id = :uid", Xkh73.class)
                .setParameter("uid", u.getId()).getResultList();
    }

    private List<Xkh73> benefitNotificationsOf(Xrd81 u) {
        return notificationsOf(u).stream().filter(n -> n.getType() == Xkh73Type.BENEFIT).toList();
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

        List<Xnj18> points = pointsOf(buyer);
        assertThat(points).hasSize(1);
        Xnj18 p = points.get(0);
        assertThat(p.getAmount()).isEqualTo(1234);
        assertThat(p.getRemainingAmount()).isEqualTo(1234);
        assertThat(p.getStatus()).isEqualTo(Xnj18Status.AVAILABLE);
        assertThat(p.getExpirationDate()).isEqualTo(LocalDate.now().plusDays(90));
        assertThat(em.find(Xdg64.class, f.orderId()).getStatus()).isEqualTo(Xdg64Status.COMPLETED);
    }

    @Test
    void completeOrderSendsBenefitNotificationAndKeepsExistingOnes() {
        Fixture f = orderInTransit(123_456);
        sync();

        service.completeOrder(f.orderId());
        sync();

        List<Xkh73> benefit = benefitNotificationsOf(buyer);
        assertThat(benefit).hasSize(1);
        assertThat(benefit.get(0).getCategory()).isEqualTo(Xzv64.SHOPPING);
        assertThat(mentionsAmount(benefit.get(0).getMessage(), "1234", "1,234")).isTrue();

        // 기존 주문 완료 알림(구매자)과 판매 완료 알림(판매자)은 그대로 남는다
        assertThat(notificationsOf(buyer)).anyMatch(n -> n.getType() == Xkh73Type.BID
                && n.getMessage().contains("주문이 완료"));
        assertThat(notificationsOf(seller)).anyMatch(n -> n.getMessage().contains("판매 완료"));
        assertThat(em.find(Xgg74.class, f.saleId()).getStatus()).isEqualTo(Xgg74Status.SOLD);
    }

    @Test
    void sellerReceivesNoPurchaseReward() {
        Fixture f = orderInTransit(80_000);
        sync();

        service.completeOrder(f.orderId());
        sync();

        assertThat(pointsOf(seller)).isEmpty();
        assertThat(benefitNotificationsOf(seller)).isEmpty();
        assertThat(pointsOf(buyer)).extracting(Xnj18::getAmount).containsExactly(800);
    }

    @Test
    void periodicTrackingUpdateRewardsWhenDelivered() throws Exception {
        Fixture f = orderInTransit(50_000);
        sync();
        when(tracker.getCurrentTrackingStatus(anyString())).thenReturn("배송완료");

        service.updateShipmentStatuses();
        sync();

        assertThat(em.find(Xhx14.class, f.shipmentId()).getStatus()).isEqualTo(Xpb98Status.DELIVERED);
        assertThat(em.find(Xdg64.class, f.orderId()).getStatus()).isEqualTo(Xdg64Status.COMPLETED);
        assertThat(pointsOf(buyer)).extracting(Xnj18::getAmount).containsExactly(500);
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

        assertThat(pointsOf(buyer)).extracting(Xnj18::getAmount).containsExactly(300, 700);
    }

    @Test
    void periodicTrackingUpdateDoesNotRewardBeforeDelivery() throws Exception {
        Fixture f = orderInTransit(50_000);
        sync();
        when(tracker.getCurrentTrackingStatus(anyString())).thenReturn("배송출발");

        service.updateShipmentStatuses();
        sync();

        assertThat(em.find(Xhx14.class, f.shipmentId()).getStatus()).isEqualTo(Xpb98Status.OUT_FOR_DELIVERY);
        assertThat(em.find(Xdg64.class, f.orderId()).getStatus()).isEqualTo(Xdg64Status.IN_TRANSIT);
        assertThat(pointsOf(buyer)).isEmpty();
        assertThat(benefitNotificationsOf(buyer)).isEmpty();
    }

    @Test
    void immediateTrackingCheckRewardsExactlyOnce() throws Exception {
        Fixture f = orderInTransit(100_000);
        sync();
        when(tracker.getCurrentTrackingStatus(anyString())).thenReturn("배송완료");

        Xpb98Status result = service.updateAndCheckShipmentStatus(f.shipmentId(), "CJ", f.tracking());
        sync();

        assertThat(result).isEqualTo(Xpb98Status.DELIVERED);
        assertThat(pointsOf(buyer)).extracting(Xnj18::getAmount).containsExactly(1000);
        assertThat(benefitNotificationsOf(buyer)).hasSize(1);
    }

    @Test
    void rewardBelowOnePointIsSkippedButOrderStillCompletes() {
        Fixture f = orderInTransit(99);
        sync();

        service.completeOrder(f.orderId());
        sync();

        assertThat(em.find(Xdg64.class, f.orderId()).getStatus()).isEqualTo(Xdg64Status.COMPLETED);
        assertThat(pointsOf(buyer)).isEmpty();
        assertThat(benefitNotificationsOf(buyer)).isEmpty();
        assertThat(notificationsOf(buyer)).anyMatch(n -> n.getMessage().contains("주문이 완료"));
    }
}
