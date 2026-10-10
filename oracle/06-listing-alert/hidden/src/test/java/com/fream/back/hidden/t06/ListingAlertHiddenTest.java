package com.fream.back.hidden.t06;

import com.fream.back.domain.notification.entity.Notification;
import com.fream.back.domain.notification.entity.NotificationCategory;
import com.fream.back.domain.notification.entity.NotificationType;
import com.fream.back.domain.order.entity.BidStatus;
import com.fream.back.domain.order.entity.Order;
import com.fream.back.domain.order.entity.OrderBid;
import com.fream.back.domain.order.entity.OrderStatus;
import com.fream.back.domain.product.entity.Interest;
import com.fream.back.domain.product.entity.Product;
import com.fream.back.domain.product.entity.ProductColor;
import com.fream.back.domain.product.entity.ProductSize;
import com.fream.back.domain.product.entity.enumType.GenderType;
import com.fream.back.domain.product.entity.enumType.SizeType;
import com.fream.back.domain.sale.entity.Sale;
import com.fream.back.domain.sale.entity.SaleBid;
import com.fream.back.domain.sale.service.command.SaleBidCommandService;
import com.fream.back.domain.user.entity.BankAccount;
import com.fream.back.domain.user.entity.User;
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

    private SaleBidCommandService service;
    private User seller;
    private User whiteFanA;
    private User whiteFanB;
    private User blackFan;
    private User bystander;
    private User sleepingFan;
    private ProductSize white260;
    private ProductSize white270;

    private User user(String prefix, boolean active) {
        seq++;
        User u = User.builder().email(prefix + "-h6-" + seq + "@test.com").password("pw").referralCode("h6-" + seq)
                .phoneNumber("010-6600-" + seq).isActive(active).build();
        em.persist(u);
        return u;
    }

    private ProductSize size(ProductColor color, String size) {
        ProductSize ps = ProductSize.builder().sizeType(SizeType.SHOES).size(size)
                .purchasePrice(139000).salePrice(139000).quantity(1).productColor(color).build();
        em.persist(ps);
        return ps;
    }

    private void interest(User u, ProductColor color) {
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

        Product product = Product.builder().name("에어 포스 1").englishName("Air Force 1").releasePrice(139000)
                .modelNumber("AF-" + seq).releaseDate("2024-01-01").gender(GenderType.UNISEX).build();
        em.persist(product);
        Product otherProduct = Product.builder().name("가젤").englishName("Gazelle").releasePrice(129000)
                .modelNumber("GZ-" + seq).releaseDate("2024-02-01").gender(GenderType.UNISEX).build();
        em.persist(otherProduct);
        ProductColor white = ProductColor.builder().colorName("White").product(product).build();
        ProductColor black = ProductColor.builder().colorName("Black").product(product).build();
        ProductColor otherWhite = ProductColor.builder().colorName("White").product(otherProduct).build();
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

        service = new Wiring(ctx).get(SaleBidCommandService.class);
    }

    private void sync() {
        em.flush();
        em.clear();
    }

    private List<Notification> notificationsOf(User u) {
        return em.createQuery("select n from Notification n where n.user.id = :uid", Notification.class)
                .setParameter("uid", u.getId()).getResultList();
    }

    private List<Notification> favoriteAlertsOf(User u) {
        return notificationsOf(u).stream().filter(n -> n.getType() == NotificationType.FAVORITE).toList();
    }

    private SaleBid list(ProductSize ps, int price) {
        SaleBid bid = service.createSaleBid(seller.getEmail(), ps.getId(), price, "서울", "12345", "010-0000-0000", false);
        sync();
        return bid;
    }

    @Test
    void everyActiveFanOfThatColorGetsOneAlertWithProductSizeAndPrice() {
        list(white260, 150000);

        for (User fan : List.of(whiteFanA, whiteFanB)) {
            List<Notification> alerts = favoriteAlertsOf(fan);
            assertThat(alerts).hasSize(1);
            Notification n = alerts.get(0);
            assertThat(n.getCategory()).isEqualTo(NotificationCategory.SHOPPING);
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

        List<Notification> alerts = favoriteAlertsOf(whiteFanA);
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
        SaleBid bid = list(white260, 150000);

        SaleBid saved = em.find(SaleBid.class, bid.getId());
        assertThat(saved.getBidPrice()).isEqualTo(150000);
        assertThat(saved.getSeller().getId()).isEqualTo(seller.getId());
        Sale sale = saved.getSale();
        assertThat(sale).isNotNull();
        assertThat(sale.getSaleBankAccount()).isNotNull();
        assertThat(notificationsOf(seller)).anyMatch(n -> n.getType() == NotificationType.BID
                && n.getMessage().contains("판매 입찰이 등록"));
    }

    @Test
    void instantSaleToABuyBidDoesNotAlertFans() {
        User buyer = user("buyer", true);
        Order order = Order.builder().user(buyer).totalAmount(160000).status(OrderStatus.PENDING_PAYMENT).build();
        em.persist(order);
        OrderBid orderBid = OrderBid.builder().user(buyer).productSize(em.find(ProductSize.class, white260.getId()))
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
