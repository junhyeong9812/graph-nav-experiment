package com.fream.back.hidden.t06;

import com.fream.back.domain.m52.entity.Xbp24;
import com.fream.back.domain.m52.entity.Xrv73;
import com.fream.back.domain.m52.entity.Xbp24Type;
import com.fream.back.domain.m99.entity.Xkh73Status;
import com.fream.back.domain.m99.entity.Xhq12;
import com.fream.back.domain.m99.entity.Xtw92;
import com.fream.back.domain.m99.entity.Xhq12Status;
import com.fream.back.domain.m30.entity.Xrc72;
import com.fream.back.domain.m30.entity.Xnr50;
import com.fream.back.domain.m30.entity.Xvm17;
import com.fream.back.domain.m30.entity.Xcc44;
import com.fream.back.domain.m30.entity.enumType.Xdg35Type;
import com.fream.back.domain.m30.entity.enumType.Xcn23Type;
import com.fream.back.domain.m85.entity.Xpj42;
import com.fream.back.domain.m85.entity.Xzt63;
import com.fream.back.domain.m85.service.command.Xzt63CommandService;
import com.fream.back.domain.m22.entity.Xfs39;
import com.fream.back.domain.m22.entity.Xjm45;
import com.fream.back.global.config.Xzj77Config;
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
@Import(Xzj77Config.class)
class ListingAlertHiddenTest {

    @Autowired EntityManager em;
    @Autowired ApplicationContext ctx;

    private static int seq = 0;

    private Xzt63CommandService service;
    private Xjm45 seller;
    private Xjm45 whiteFanA;
    private Xjm45 whiteFanB;
    private Xjm45 blackFan;
    private Xjm45 bystander;
    private Xjm45 sleepingFan;
    private Xcc44 white260;
    private Xcc44 white270;

    private Xjm45 user(String prefix, boolean active) {
        seq++;
        Xjm45 u = Xjm45.builder().email(prefix + "-h6-" + seq + "@test.com").password("pw").referralCode("h6-" + seq)
                .phoneNumber("010-6600-" + seq).isActive(active).build();
        em.persist(u);
        return u;
    }

    private Xcc44 size(Xvm17 color, String size) {
        Xcc44 ps = Xcc44.builder().sizeType(Xcn23Type.SHOES).size(size)
                .purchasePrice(139000).salePrice(139000).quantity(1).productColor(color).build();
        em.persist(ps);
        return ps;
    }

    private void interest(Xjm45 u, Xvm17 color) {
        em.persist(Xrc72.builder().user(u).productColor(color).build());
    }

    @BeforeEach
    void setUp() {
        seller = user("seller", true);
        em.persist(Xfs39.builder().user(seller).bankName("국민").accountNumber("111-222").accountHolder("판매자").build());
        whiteFanA = user("white-a", true);
        whiteFanB = user("white-b", true);
        blackFan = user("black", true);
        bystander = user("none", true);
        sleepingFan = user("sleeping", false);

        Xnr50 product = Xnr50.builder().name("에어 포스 1").englishName("Air Force 1").releasePrice(139000)
                .modelNumber("AF-" + seq).releaseDate("2024-01-01").gender(Xdg35Type.UNISEX).build();
        em.persist(product);
        Xnr50 otherProduct = Xnr50.builder().name("가젤").englishName("Gazelle").releasePrice(129000)
                .modelNumber("GZ-" + seq).releaseDate("2024-02-01").gender(Xdg35Type.UNISEX).build();
        em.persist(otherProduct);
        Xvm17 white = Xvm17.builder().colorName("White").product(product).build();
        Xvm17 black = Xvm17.builder().colorName("Black").product(product).build();
        Xvm17 otherWhite = Xvm17.builder().colorName("White").product(otherProduct).build();
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

        service = new Wiring(ctx).get(Xzt63CommandService.class);
    }

    private void sync() {
        em.flush();
        em.clear();
    }

    private List<Xbp24> notificationsOf(Xjm45 u) {
        return em.createQuery("select n from Xbp24 n where n.user.id = :uid", Xbp24.class)
                .setParameter("uid", u.getId()).getResultList();
    }

    private List<Xbp24> favoriteAlertsOf(Xjm45 u) {
        return notificationsOf(u).stream().filter(n -> n.getType() == Xbp24Type.FAVORITE).toList();
    }

    private Xzt63 list(Xcc44 ps, int price) {
        Xzt63 bid = service.createSaleBid(seller.getEmail(), ps.getId(), price, "서울", "12345", "010-0000-0000", false);
        sync();
        return bid;
    }

    @Test
    void everyActiveFanOfThatColorGetsOneAlertWithProductSizeAndPrice() {
        list(white260, 150000);

        for (Xjm45 fan : List.of(whiteFanA, whiteFanB)) {
            List<Xbp24> alerts = favoriteAlertsOf(fan);
            assertThat(alerts).hasSize(1);
            Xbp24 n = alerts.get(0);
            assertThat(n.getCategory()).isEqualTo(Xrv73.SHOPPING);
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

        List<Xbp24> alerts = favoriteAlertsOf(whiteFanA);
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
        Xzt63 bid = list(white260, 150000);

        Xzt63 saved = em.find(Xzt63.class, bid.getId());
        assertThat(saved.getBidPrice()).isEqualTo(150000);
        assertThat(saved.getSeller().getId()).isEqualTo(seller.getId());
        Xpj42 sale = saved.getSale();
        assertThat(sale).isNotNull();
        assertThat(sale.getSaleBankAccount()).isNotNull();
        assertThat(notificationsOf(seller)).anyMatch(n -> n.getType() == Xbp24Type.BID
                && n.getMessage().contains("판매 입찰이 등록"));
    }

    @Test
    void instantSaleToABuyBidDoesNotAlertFans() {
        Xjm45 buyer = user("buyer", true);
        Xhq12 order = Xhq12.builder().user(buyer).totalAmount(160000).status(Xhq12Status.PENDING_PAYMENT).build();
        em.persist(order);
        Xtw92 orderBid = Xtw92.builder().user(buyer).productSize(em.find(Xcc44.class, white260.getId()))
                .bidPrice(160000).status(Xkh73Status.PENDING).order(order).build();
        em.persist(orderBid);
        sync();

        service.createInstantSaleBid(orderBid.getId(), seller.getEmail(), "서울", "12345", "010-0000-0000");
        sync();

        assertThat(favoriteAlertsOf(whiteFanA)).isEmpty();
        assertThat(favoriteAlertsOf(whiteFanB)).isEmpty();
        assertThat(notificationsOf(buyer)).anyMatch(n -> n.getMessage().contains("구매 입찰에 판매자가 등록"));
    }
}
