package com.fream.back.hidden.t06;

import com.fream.back.domain.m15.entity.Xkh73;
import com.fream.back.domain.m15.entity.Xzv64;
import com.fream.back.domain.m15.entity.Xkh73Type;
import com.fream.back.domain.m16.entity.Xtz73Status;
import com.fream.back.domain.m16.entity.Xdg64;
import com.fream.back.domain.m16.entity.Xzj27;
import com.fream.back.domain.m16.entity.Xdg64Status;
import com.fream.back.domain.m02.entity.Xrf21;
import com.fream.back.domain.m02.entity.Xkr46;
import com.fream.back.domain.m02.entity.Xfp92;
import com.fream.back.domain.m02.entity.Xdh20;
import com.fream.back.domain.m02.entity.enumType.Xbj22Type;
import com.fream.back.domain.m02.entity.enumType.Xnp24Type;
import com.fream.back.domain.m10.entity.Xgg74;
import com.fream.back.domain.m10.entity.Xzz58;
import com.fream.back.domain.m10.service.command.Xzz58CommandService;
import com.fream.back.domain.m13.entity.Xbr34;
import com.fream.back.domain.m13.entity.Xrd81;
import com.fream.back.global.config.Xsd33Config;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
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
 * 관심 상품 새 판매 알림: 대상(같은 색상 관심·활성·판매자 제외), 문구, 즉시 판매 제외, 기존 판매 등록 동작 보존.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(Xsd33Config.class)
class ListingAlertHiddenTest {

    @Autowired EntityManager em;
    @Autowired ApplicationContext ctx;

    private static int seq = 0;

    private Xzz58CommandService service;
    private Xrd81 seller;
    private Xrd81 whiteFanA;
    private Xrd81 whiteFanB;
    private Xrd81 blackFan;
    private Xrd81 bystander;
    private Xrd81 sleepingFan;
    private Xdh20 white260;
    private Xdh20 white270;

    private Xrd81 user(String prefix, boolean active) {
        seq++;
        Xrd81 u = Xrd81.builder().email(prefix + "-h6-" + seq + "@test.com").password("pw").referralCode("h6-" + seq)
                .phoneNumber("010-6600-" + seq).isActive(active).build();
        em.persist(u);
        return u;
    }

    private Xdh20 size(Xfp92 color, String size) {
        Xdh20 ps = Xdh20.builder().sizeType(Xnp24Type.SHOES).size(size)
                .purchasePrice(139000).salePrice(139000).quantity(1).productColor(color).build();
        em.persist(ps);
        return ps;
    }

    private void interest(Xrd81 u, Xfp92 color) {
        em.persist(Xrf21.builder().user(u).productColor(color).build());
    }

    @BeforeEach
    void setUp() {
        seller = user("seller", true);
        em.persist(Xbr34.builder().user(seller).bankName("국민").accountNumber("111-222").accountHolder("판매자").build());
        whiteFanA = user("white-a", true);
        whiteFanB = user("white-b", true);
        blackFan = user("black", true);
        bystander = user("none", true);
        sleepingFan = user("sleeping", false);

        Xkr46 product = Xkr46.builder().name("에어 포스 1").englishName("Air Force 1").releasePrice(139000)
                .modelNumber("AF-" + seq).releaseDate("2024-01-01").gender(Xbj22Type.UNISEX).build();
        em.persist(product);
        Xkr46 otherProduct = Xkr46.builder().name("가젤").englishName("Gazelle").releasePrice(129000)
                .modelNumber("GZ-" + seq).releaseDate("2024-02-01").gender(Xbj22Type.UNISEX).build();
        em.persist(otherProduct);
        Xfp92 white = Xfp92.builder().colorName("White").product(product).build();
        Xfp92 black = Xfp92.builder().colorName("Black").product(product).build();
        Xfp92 otherWhite = Xfp92.builder().colorName("White").product(otherProduct).build();
        em.persist(white);
        em.persist(black);
        em.persist(otherWhite);
        white260 = size(white, "260");
        white270 = size(white, "270");
        size(black, "260");
        size(otherWhite, "260");

        interest(whiteFanA, white);
        interest(whiteFanB, white);
        interest(blackFan, black);
        interest(bystander, otherWhite);
        interest(sleepingFan, white);
        interest(seller, white);
        em.flush();
        em.clear();

        service = new Wiring(ctx).get(Xzz58CommandService.class);
    }

    private void sync() {
        em.flush();
        em.clear();
    }

    private List<Xkh73> notificationsOf(Xrd81 u) {
        return em.createQuery("select n from Xkh73 n where n.user.id = :uid", Xkh73.class)
                .setParameter("uid", u.getId()).getResultList();
    }

    private List<Xkh73> favoriteAlertsOf(Xrd81 u) {
        return notificationsOf(u).stream().filter(n -> n.getType() == Xkh73Type.FAVORITE).toList();
    }

    private Xzz58 list(Xdh20 ps, int price) {
        Xzz58 bid = service.createSaleBid(seller.getEmail(), ps.getId(), price, "서울", "12345", "010-0000-0000", false);
        sync();
        return bid;
    }

    @Test
    void everyActiveFanOfThatColorGetsOneAlertWithProductSizeAndPrice() {
        list(white260, 150000);

        for (Xrd81 fan : List.of(whiteFanA, whiteFanB)) {
            List<Xkh73> alerts = favoriteAlertsOf(fan);
            assertThat(alerts).hasSize(1);
            Xkh73 n = alerts.get(0);
            assertThat(n.getCategory()).isEqualTo(Xzv64.SHOPPING);
            assertThat(n.getMessage()).contains("에어 포스 1").contains("260");
            assertThat(n.getMessage().contains("150000") || n.getMessage().contains("150,000")).isTrue();
        }
    }

    @Test
    void fansOfOtherColorsOrOtherProductsAreNotAlerted() {
        list(white260, 150000);

        assertThat(favoriteAlertsOf(blackFan)).isEmpty();
        assertThat(favoriteAlertsOf(bystander)).isEmpty();
        assertThat(notificationsOf(blackFan)).isEmpty();
        assertThat(notificationsOf(bystander)).isEmpty();
    }

    @Test
    void sellerAndInactiveMembersAreNotAlerted() {
        list(white260, 150000);

        assertThat(favoriteAlertsOf(seller)).isEmpty();
        assertThat(favoriteAlertsOf(sleepingFan)).isEmpty();
    }

    @Test
    void anotherSizeOfTheSameColorAlsoAlerts() {
        list(white270, 171000);

        List<Xkh73> alerts = favoriteAlertsOf(whiteFanA);
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getMessage()).contains("270");
        assertThat(alerts.get(0).getMessage().contains("171000") || alerts.get(0).getMessage().contains("171,000")).isTrue();
    }

    @Test
    void eachListingProducesItsOwnAlert() {
        list(white260, 150000);
        list(white270, 160000);

        assertThat(favoriteAlertsOf(whiteFanA)).hasSize(2);
        assertThat(favoriteAlertsOf(whiteFanB)).hasSize(2);
    }

    @Test
    void listingItselfStillWorksAsBefore() {
        Xzz58 bid = list(white260, 150000);

        Xzz58 saved = em.find(Xzz58.class, bid.getId());
        assertThat(saved.getBidPrice()).isEqualTo(150000);
        assertThat(saved.getSeller().getId()).isEqualTo(seller.getId());
        Xgg74 sale = saved.getSale();
        assertThat(sale).isNotNull();
        assertThat(sale.getSaleBankAccount()).isNotNull();
        assertThat(notificationsOf(seller)).anyMatch(n -> n.getType() == Xkh73Type.BID
                && n.getMessage().contains("판매 입찰이 등록"));
    }

    @Test
    void instantSaleToABuyBidDoesNotAlertFans() {
        Xrd81 buyer = user("buyer", true);
        Xdg64 order = Xdg64.builder().user(buyer).totalAmount(160000).status(Xdg64Status.PENDING_PAYMENT).build();
        em.persist(order);
        Xzj27 orderBid = Xzj27.builder().user(buyer).productSize(em.find(Xdh20.class, white260.getId()))
                .bidPrice(160000).status(Xtz73Status.PENDING).order(order).build();
        em.persist(orderBid);
        sync();

        service.createInstantSaleBid(orderBid.getId(), seller.getEmail(), "서울", "12345", "010-0000-0000");
        sync();

        assertThat(favoriteAlertsOf(whiteFanA)).isEmpty();
        assertThat(favoriteAlertsOf(whiteFanB)).isEmpty();
        assertThat(notificationsOf(buyer)).anyMatch(n -> n.getMessage().contains("구매 입찰에 판매자가 등록"));
    }
}
