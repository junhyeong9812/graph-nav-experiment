package com.fream.back.hidden.t06;

import com.fream.back.domain.payment.entity.Payment;
import com.fream.back.domain.payment.entity.PaymentCategory;
import com.fream.back.domain.payment.entity.PaymentType;
import com.fream.back.domain.faq.entity.BidStatus;
import com.fream.back.domain.faq.entity.Faq;
import com.fream.back.domain.faq.entity.FaqBid;
import com.fream.back.domain.faq.entity.FaqStatus;
import com.fream.back.domain.user.entity.Interest;
import com.fream.back.domain.user.entity.User;
import com.fream.back.domain.user.entity.UserColor;
import com.fream.back.domain.user.entity.UserSize;
import com.fream.back.domain.user.entity.enumType.GenderType;
import com.fream.back.domain.user.entity.enumType.SizeType;
import com.fream.back.domain.style.entity.Style;
import com.fream.back.domain.style.entity.StyleBid;
import com.fream.back.domain.style.service.command.StyleBidCommandService;
import com.fream.back.domain.product.entity.BankAccount;
import com.fream.back.domain.product.entity.Product;
import com.fream.back.global.config.QueryDslConfig;
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
@Import(QueryDslConfig.class)
class ListingAlertHiddenTest {

    @Autowired EntityManager em;
    @Autowired ApplicationContext ctx;

    private static int seq = 0;

    private StyleBidCommandService service;
    private Product seller;
    private Product whiteFanA;
    private Product whiteFanB;
    private Product blackFan;
    private Product bystander;
    private Product sleepingFan;
    private UserSize white260;
    private UserSize white270;

    private Product user(String prefix, boolean active) {
        seq++;
        Product u = Product.builder().email(prefix + "-h6-" + seq + "@test.com").password("pw").referralCode("h6-" + seq)
                .phoneNumber("010-6600-" + seq).isActive(active).build();
        em.persist(u);
        return u;
    }

    private UserSize size(UserColor color, String size) {
        UserSize ps = UserSize.builder().sizeType(SizeType.SHOES).size(size)
                .purchasePrice(139000).salePrice(139000).quantity(1).productColor(color).build();
        em.persist(ps);
        return ps;
    }

    private void interest(Product u, UserColor color) {
        em.persist(Interest.builder().user(u).productColor(color).build());
    }

    @BeforeEach
    void setUp() {
        seller = user("seller", true);
        em.persist(BankAccount.builder().user(seller).bankName("국민").accountNumber("111-222").accountHolder("판매자").build());
        whiteFanA = user("white-a", true);
        whiteFanB = user("white-b", true);
        blackFan = user("black", true);
        bystander = user("none", true);
        sleepingFan = user("sleeping", false);

        User product = User.builder().name("에어 포스 1").englishName("Air Force 1").releasePrice(139000)
                .modelNumber("AF-" + seq).releaseDate("2024-01-01").gender(GenderType.UNISEX).build();
        em.persist(product);
        User otherProduct = User.builder().name("가젤").englishName("Gazelle").releasePrice(129000)
                .modelNumber("GZ-" + seq).releaseDate("2024-02-01").gender(GenderType.UNISEX).build();
        em.persist(otherProduct);
        UserColor white = UserColor.builder().colorName("White").product(product).build();
        UserColor black = UserColor.builder().colorName("Black").product(product).build();
        UserColor otherWhite = UserColor.builder().colorName("White").product(otherProduct).build();
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

        service = new Wiring(ctx).get(StyleBidCommandService.class);
    }

    private void sync() {
        em.flush();
        em.clear();
    }

    private List<Payment> notificationsOf(Product u) {
        return em.createQuery("select n from Payment n where n.user.id = :uid", Payment.class)
                .setParameter("uid", u.getId()).getResultList();
    }

    private List<Payment> favoriteAlertsOf(Product u) {
        return notificationsOf(u).stream().filter(n -> n.getType() == PaymentType.FAVORITE).toList();
    }

    private StyleBid list(UserSize ps, int price) {
        StyleBid bid = service.createSaleBid(seller.getEmail(), ps.getId(), price, "서울", "12345", "010-0000-0000", false);
        sync();
        return bid;
    }

    @Test
    void everyActiveFanOfThatColorGetsOneAlertWithProductSizeAndPrice() {
        list(white260, 150000);

        for (Product fan : List.of(whiteFanA, whiteFanB)) {
            List<Payment> alerts = favoriteAlertsOf(fan);
            assertThat(alerts).hasSize(1);
            Payment n = alerts.get(0);
            assertThat(n.getCategory()).isEqualTo(PaymentCategory.SHOPPING);
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

        List<Payment> alerts = favoriteAlertsOf(whiteFanA);
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
        StyleBid bid = list(white260, 150000);

        StyleBid saved = em.find(StyleBid.class, bid.getId());
        assertThat(saved.getBidPrice()).isEqualTo(150000);
        assertThat(saved.getSeller().getId()).isEqualTo(seller.getId());
        Style sale = saved.getSale();
        assertThat(sale).isNotNull();
        assertThat(sale.getSaleBankAccount()).isNotNull();
        assertThat(notificationsOf(seller)).anyMatch(n -> n.getType() == PaymentType.BID
                && n.getMessage().contains("판매 입찰이 등록"));
    }

    @Test
    void instantSaleToABuyBidDoesNotAlertFans() {
        Product buyer = user("buyer", true);
        Faq order = Faq.builder().user(buyer).totalAmount(160000).status(FaqStatus.PENDING_PAYMENT).build();
        em.persist(order);
        FaqBid orderBid = FaqBid.builder().user(buyer).productSize(em.find(UserSize.class, white260.getId()))
                .bidPrice(160000).status(BidStatus.PENDING).order(order).build();
        em.persist(orderBid);
        sync();

        service.createInstantSaleBid(orderBid.getId(), seller.getEmail(), "서울", "12345", "010-0000-0000");
        sync();

        assertThat(favoriteAlertsOf(whiteFanA)).isEmpty();
        assertThat(favoriteAlertsOf(whiteFanB)).isEmpty();
        assertThat(notificationsOf(buyer)).anyMatch(n -> n.getMessage().contains("구매 입찰에 판매자가 등록"));
    }
}
