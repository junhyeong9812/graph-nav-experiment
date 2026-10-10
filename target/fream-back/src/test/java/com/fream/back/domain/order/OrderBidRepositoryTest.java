package com.fream.back.domain.order;

import com.fream.back.domain.order.dto.OrderBidResponseDto;
import com.fream.back.domain.order.dto.OrderBidStatusCountDto;
import com.fream.back.domain.order.entity.BidStatus;
import com.fream.back.domain.order.entity.Order;
import com.fream.back.domain.order.entity.OrderBid;
import com.fream.back.domain.order.entity.OrderItem;
import com.fream.back.domain.order.entity.OrderStatus;
import com.fream.back.domain.order.repository.OrderBidRepository;
import com.fream.back.domain.order.repository.OrderItemRepository;
import com.fream.back.domain.order.repository.OrderRepository;
import com.fream.back.domain.product.entity.Product;
import com.fream.back.domain.product.entity.ProductColor;
import com.fream.back.domain.product.entity.ProductImage;
import com.fream.back.domain.product.entity.ProductSize;
import com.fream.back.domain.product.entity.enumType.GenderType;
import com.fream.back.domain.product.entity.enumType.SizeType;
import com.fream.back.domain.shipment.entity.OrderShipment;
import com.fream.back.domain.shipment.entity.ShipmentStatus;
import com.fream.back.domain.user.entity.User;
import com.fream.back.global.config.QueryDslConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 주문 입찰·주문·주문 항목 리포지토리 쿼리(QueryDSL 커스텀 포함)의 필터·집계·조인 결과를 고정한다.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
class OrderBidRepositoryTest {

    @Autowired
    private EntityManager em;
    @Autowired
    private OrderBidRepository orderBidRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;

    private User alice;
    private User bob;
    private ProductColor black;
    private ProductColor white;
    private ProductSize black260;
    private ProductSize white270;

    @BeforeEach
    void setUp() {
        alice = persistUser("alice@test.com", "REF-A");
        bob = persistUser("bob@test.com", "REF-B");

        Product product = Product.builder()
                .name("덩크 로우").englishName("Dunk Low").releasePrice(129000)
                .modelNumber("DD1391-100").releaseDate("2021-01-01").gender(GenderType.UNISEX)
                .build();
        em.persist(product);

        black = persistColor(product, "Black", "black.jpg");
        white = persistColor(product, "White", "white.jpg");
        black260 = persistSize(black, "260");
        white270 = persistSize(white, "270");
        em.flush();
    }

    private User persistUser(String email, String referralCode) {
        User user = User.builder()
                .email(email).password("pw").referralCode(referralCode).phoneNumber("010-0000-0000")
                .build();
        em.persist(user);
        return user;
    }

    private ProductColor persistColor(Product product, String colorName, String imageUrl) {
        ProductColor color = ProductColor.builder().colorName(colorName).product(product).build();
        em.persist(color);
        ProductImage image = ProductImage.builder().imageUrl(imageUrl).productColor(color).build();
        em.persist(image);
        color.addThumbnailImage(image);
        return color;
    }

    private ProductSize persistSize(ProductColor color, String size) {
        ProductSize productSize = ProductSize.builder()
                .sizeType(SizeType.SHOES).size(size).purchasePrice(100000).salePrice(90000).quantity(10)
                .productColor(color)
                .build();
        em.persist(productSize);
        return productSize;
    }

    private OrderBid persistBid(User user, ProductSize size, int price, BidStatus status, Order order) {
        OrderBid bid = OrderBid.builder()
                .user(user).productSize(size).bidPrice(price).status(status).order(order)
                .build();
        em.persist(bid);
        return bid;
    }

    private Order persistOrder(User user, OrderStatus status, ShipmentStatus shipmentStatus) {
        Order order = Order.builder().user(user).totalAmount(1000).status(status).build();
        em.persist(order);
        if (shipmentStatus != null) {
            OrderShipment shipment = OrderShipment.builder()
                    .order(order).receiverName("r").status(shipmentStatus).build();
            em.persist(shipment);
            order.assignOrderShipment(shipment);
        }
        return order;
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    @Test
    void findOrderBidsByFiltersReturnsOnlyRequestedUsersBidsWithJoinedProductData() {
        Order order = persistOrder(alice, OrderStatus.PREPARING, ShipmentStatus.PENDING);
        OrderBid matched = persistBid(alice, black260, 150000, BidStatus.MATCHED, order);
        persistBid(alice, white270, 120000, BidStatus.PENDING, null);
        persistBid(bob, black260, 999000, BidStatus.PENDING, null);
        flushAndClear();

        Page<OrderBidResponseDto> page = orderBidRepository.findOrderBidsByFilters(
                "alice@test.com", null, null, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(OrderBidResponseDto::getBidPrice)
                .containsExactlyInAnyOrder(150000, 120000);

        OrderBidResponseDto dto = page.getContent().stream()
                .filter(d -> d.getOrderBidId().equals(matched.getId())).findFirst().orElseThrow();
        assertThat(dto.getProductName()).isEqualTo("덩크 로우");
        assertThat(dto.getProductEnglishName()).isEqualTo("Dunk Low");
        assertThat(dto.getSize()).isEqualTo("260");
        assertThat(dto.getColorName()).isEqualTo("Black");
        assertThat(dto.getImageUrl()).isEqualTo("black.jpg");
        assertThat(dto.getBidStatus()).isEqualTo("MATCHED");
        assertThat(dto.getOrderStatus()).isEqualTo("PREPARING");
        assertThat(dto.getShipmentStatus()).isEqualTo("PENDING");
        assertThat(dto.getCreatedDate()).isNotNull();

        OrderBidResponseDto noOrder = page.getContent().stream()
                .filter(d -> !d.getOrderBidId().equals(matched.getId())).findFirst().orElseThrow();
        assertThat(noOrder.getOrderStatus()).isNull();
        assertThat(noOrder.getShipmentStatus()).isNull();
    }

    @Test
    void findOrderBidsByFiltersAppliesBidStatusAndOrderStatusFilters() {
        Order preparing = persistOrder(alice, OrderStatus.PREPARING, null);
        Order completed = persistOrder(alice, OrderStatus.COMPLETED, null);
        persistBid(alice, black260, 1000, BidStatus.MATCHED, preparing);
        persistBid(alice, black260, 2000, BidStatus.COMPLETED, completed);
        persistBid(alice, white270, 3000, BidStatus.PENDING, null);
        flushAndClear();

        Page<OrderBidResponseDto> byBid = orderBidRepository.findOrderBidsByFilters(
                "alice@test.com", "PENDING", null, PageRequest.of(0, 10));
        assertThat(byBid.getContent()).extracting(OrderBidResponseDto::getBidPrice).containsExactly(3000);

        Page<OrderBidResponseDto> byOrder = orderBidRepository.findOrderBidsByFilters(
                "alice@test.com", null, "COMPLETED", PageRequest.of(0, 10));
        assertThat(byOrder.getContent()).extracting(OrderBidResponseDto::getBidPrice).containsExactly(2000);
        assertThat(byOrder.getTotalElements()).isEqualTo(1);

        Page<OrderBidResponseDto> noEmail = orderBidRepository.findOrderBidsByFilters(
                "", "MATCHED", null, PageRequest.of(0, 10));
        assertThat(noEmail.getContent()).extracting(OrderBidResponseDto::getBidPrice).containsExactly(1000);
    }

    @Test
    void findOrderBidsByFiltersPagesContentButCountsAll() {
        for (int i = 1; i <= 5; i++) {
            persistBid(alice, black260, i * 1000, BidStatus.PENDING, null);
        }
        flushAndClear();

        Page<OrderBidResponseDto> second = orderBidRepository.findOrderBidsByFilters(
                "alice@test.com", null, null, PageRequest.of(1, 2));

        assertThat(second.getContent()).hasSize(2);
        assertThat(second.getTotalElements()).isEqualTo(5);
        assertThat(second.getTotalPages()).isEqualTo(3);
    }

    @Test
    void findOrderBidsByFiltersExcludesColorWithoutThumbnailFromContentButNotFromTotal() {
        ProductColor noThumb = ProductColor.builder().colorName("Red").product(black.getProduct()).build();
        em.persist(noThumb);
        ProductSize red250 = persistSize(noThumb, "250");
        persistBid(alice, black260, 1000, BidStatus.PENDING, null);
        persistBid(alice, red250, 2000, BidStatus.PENDING, null);
        flushAndClear();

        Page<OrderBidResponseDto> all = orderBidRepository.findOrderBidsByFilters(
                "alice@test.com", null, null, PageRequest.of(0, 10));
        assertThat(all.getContent()).extracting(OrderBidResponseDto::getBidPrice).containsExactly(1000);

        // count 쿼리는 썸네일 조인을 하지 않는다
        Page<OrderBidResponseDto> first = orderBidRepository.findOrderBidsByFilters(
                "alice@test.com", null, null, PageRequest.of(0, 1));
        assertThat(first.getContent()).extracting(OrderBidResponseDto::getBidPrice).containsExactly(1000);
        assertThat(first.getTotalElements()).isEqualTo(2);

        Page<OrderBidResponseDto> second = orderBidRepository.findOrderBidsByFilters(
                "alice@test.com", null, null, PageRequest.of(1, 1));
        assertThat(second.getContent()).isEmpty();
        assertThat(second.getTotalElements()).isEqualTo(2);
    }

    @Test
    void countOrderBidsByStatusGroupsCancelledAndCompletedTogether() {
        persistBid(alice, black260, 1000, BidStatus.PENDING, null);
        persistBid(alice, black260, 1000, BidStatus.PENDING, null);
        persistBid(alice, black260, 1000, BidStatus.MATCHED, null);
        persistBid(alice, black260, 1000, BidStatus.CANCELLED, null);
        persistBid(alice, black260, 1000, BidStatus.COMPLETED, null);
        persistBid(bob, black260, 1000, BidStatus.PENDING, null);
        flushAndClear();

        OrderBidStatusCountDto counts = orderBidRepository.countOrderBidsByStatus("alice@test.com");

        assertThat(counts.getPendingCount()).isEqualTo(2);
        assertThat(counts.getMatchedCount()).isEqualTo(1);
        assertThat(counts.getCancelledOrCompletedCount()).isEqualTo(2);

        OrderBidStatusCountDto none = orderBidRepository.countOrderBidsByStatus("nobody@test.com");
        assertThat(none.getPendingCount()).isZero();
        assertThat(none.getMatchedCount()).isZero();
        assertThat(none.getCancelledOrCompletedCount()).isZero();
    }

    @Test
    void findOrderBidByIdRequiresOwnerEmail() {
        OrderBid bid = persistBid(alice, white270, 7000, BidStatus.PENDING, null);
        flushAndClear();

        OrderBidResponseDto own = orderBidRepository.findOrderBidById(bid.getId(), "alice@test.com");
        assertThat(own).isNotNull();
        assertThat(own.getBidPrice()).isEqualTo(7000);
        assertThat(own.getColorName()).isEqualTo("White");

        assertThat(orderBidRepository.findOrderBidById(bid.getId(), "bob@test.com")).isNull();
    }

    @Test
    void tradeCountByColorIdsCountsOnlyCompletedBidsPerColor() {
        persistBid(alice, black260, 1000, BidStatus.COMPLETED, null);
        persistBid(bob, black260, 1000, BidStatus.COMPLETED, null);
        persistBid(alice, black260, 1000, BidStatus.MATCHED, null);
        persistBid(alice, white270, 1000, BidStatus.COMPLETED, null);
        flushAndClear();

        Map<Long, Long> counts = orderBidRepository.tradeCountByColorIds(List.of(black.getId(), white.getId()));
        assertThat(counts).containsEntry(black.getId(), 2L).containsEntry(white.getId(), 1L).hasSize(2);

        Map<Long, Long> onlyBlack = orderBidRepository.tradeCountByColorIds(List.of(black.getId()));
        assertThat(onlyBlack).containsOnlyKeys(black.getId());

        assertThat(orderBidRepository.tradeCountByColorIds(List.of())).isEmpty();
        assertThat(orderBidRepository.tradeCountByColorIds(null)).isEmpty();
    }

    @Test
    void derivedQueriesOnOrderBidOrderAndOrderItem() {
        Order pending = persistOrder(alice, OrderStatus.PENDING_PAYMENT, null);
        persistOrder(alice, OrderStatus.COMPLETED, null);
        persistOrder(bob, OrderStatus.PENDING_PAYMENT, null);
        OrderBid bid = persistBid(alice, black260, 1000, BidStatus.MATCHED, pending);
        em.persist(OrderItem.builder().order(pending).productSize(black260).quantity(1).price(1000).build());
        em.persist(OrderItem.builder().order(pending).productSize(white270).quantity(3).price(2000).build());
        flushAndClear();

        assertThat(orderBidRepository.findByOrderId(pending.getId())).get()
                .extracting(OrderBid::getId).isEqualTo(bid.getId());
        assertThat(orderBidRepository.findByOrderId(-1L)).isEmpty();

        assertThat(orderRepository.findByUser_EmailAndStatus("alice@test.com", OrderStatus.PENDING_PAYMENT))
                .extracting(Order::getId).containsExactly(pending.getId());

        assertThat(orderItemRepository.findByOrder_Id(pending.getId()))
                .extracting(OrderItem::getPrice).containsExactlyInAnyOrder(1000, 2000);
        assertThat(orderItemRepository.findByQuantityGreaterThan(1))
                .extracting(OrderItem::getQuantity).containsExactly(3);
    }
}
