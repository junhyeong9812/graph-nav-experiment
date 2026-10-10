package com.fream.back.hidden.c03;

import com.fream.back.domain.order.dto.OrderBidResponseDto;
import com.fream.back.domain.order.entity.BidStatus;
import com.fream.back.domain.order.entity.OrderBid;
import com.fream.back.domain.order.service.query.OrderBidQueryService;
import com.fream.back.domain.product.entity.Product;
import com.fream.back.domain.product.entity.ProductSize;
import com.fream.back.domain.user.entity.User;
import com.fream.back.global.config.QueryDslConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** 지점 1: 구매 입찰 내역 목록. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({QueryDslConfig.class, OrderBidQueryService.class})
class PurchaseBidListPagingHiddenTest {

    @Autowired EntityManager em;
    @Autowired OrderBidQueryService service;

    private final List<Long> listed = new ArrayList<>();

    @BeforeEach
    void setUp() {
        Fixtures f = new Fixtures(em);
        User buyer = f.user("buyer@test.com");
        User other = f.user("other@test.com");
        Product p = f.product("Dunk", f.brand("Nike"));
        ProductSize withImage = f.size(f.color(p, "Black", "black.jpg"), "260", 100000);
        ProductSize withImage2 = f.size(f.color(p, "White", "white.jpg"), "270", 110000);
        ProductSize noImage = f.size(f.color(p, "Green", null), "250", 120000);

        listed.add(bid(buyer, withImage, 1000));
        bid(buyer, noImage, 2000);
        listed.add(bid(buyer, withImage2, 3000));
        bid(buyer, noImage, 4000);
        listed.add(bid(buyer, withImage, 5000));
        bid(buyer, noImage, 6000);
        listed.add(bid(buyer, withImage2, 7000));
        bid(other, withImage, 8000);
        em.flush();
        em.clear();
    }

    private Long bid(User user, ProductSize size, int price) {
        OrderBid bid = OrderBid.builder().user(user).productSize(size).bidPrice(price).status(BidStatus.PENDING).build();
        em.persist(bid);
        return bid.getId();
    }

    @Test
    void totalMatchesReachableRowsForEveryPageSize() {
        for (int size = 1; size <= 3; size++) {
            int pageSize = size;
            PagingProbe<OrderBidResponseDto, Long> probe = PagingProbe.walk(
                    page -> service.getOrderBids("buyer@test.com", null, null, PageRequest.of(page, pageSize)),
                    OrderBidResponseDto::getOrderBidId);
            probe.assertConsistent();
            assertThat(probe.keys).containsAll(listed);
        }
    }

    @Test
    void totalMatchesReachableRowsWithStatusFilter() {
        PagingProbe<OrderBidResponseDto, Long> probe = PagingProbe.walk(
                page -> service.getOrderBids("buyer@test.com", "PENDING", null, PageRequest.of(page, 2)),
                OrderBidResponseDto::getOrderBidId);
        probe.assertConsistent();
        assertThat(probe.keys).containsAll(listed);
    }
}
