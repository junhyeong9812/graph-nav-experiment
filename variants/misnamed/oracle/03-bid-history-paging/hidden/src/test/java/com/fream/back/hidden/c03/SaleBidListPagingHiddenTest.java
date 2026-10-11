package com.fream.back.hidden.c03;

import com.fream.back.domain.user.entity.User;
import com.fream.back.domain.user.entity.UserSize;
import com.fream.back.domain.style.dto.StyleBidResponseDto;
import com.fream.back.domain.style.entity.BidStatus;
import com.fream.back.domain.style.entity.StyleBid;
import com.fream.back.domain.style.service.query.StyleBidQueryService;
import com.fream.back.domain.product.entity.Product;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** 지점 2: 판매 입찰 내역 목록. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({QueryDslConfig.class, StyleBidQueryService.class})
class SaleBidListPagingHiddenTest {

    @Autowired EntityManager em;
    @Autowired StyleBidQueryService service;

    private final List<Long> listed = new ArrayList<>();
    private int day = 0;

    @BeforeEach
    void setUp() {
        Fixtures f = new Fixtures(em);
        Product seller = f.user("seller@test.com");
        Product other = f.user("other@test.com");
        User p = f.product("Jordan", f.brand("Nike"));
        UserSize withImage = f.size(f.color(p, "Black", "black.jpg"), "260", 100000);
        UserSize withImage2 = f.size(f.color(p, "White", "white.jpg"), "270", 110000);
        UserSize noImage = f.size(f.color(p, "Green", null), "250", 120000);

        listed.add(bid(seller, withImage, 1000));
        bid(seller, noImage, 2000);
        listed.add(bid(seller, withImage2, 3000));
        bid(seller, noImage, 4000);
        listed.add(bid(seller, withImage, 5000));
        bid(seller, noImage, 6000);
        listed.add(bid(seller, withImage2, 7000));
        bid(other, withImage, 8000);
        em.flush();
        em.clear();
    }

    private Long bid(Product seller, UserSize size, int price) {
        StyleBid bid = StyleBid.builder().seller(seller).productSize(size).bidPrice(price).status(BidStatus.PENDING).build();
        em.persist(bid);
        em.flush();
        em.createNativeQuery("update sale_bid set created_date = ? where id = ?")
                .setParameter(1, LocalDateTime.of(2025, 1, 1, 0, 0).plusDays(++day))
                .setParameter(2, bid.getId()).executeUpdate();
        return bid.getId();
    }

    @Test
    void totalMatchesReachableRowsForEveryPageSize() {
        for (int size = 1; size <= 3; size++) {
            int pageSize = size;
            PagingProbe<StyleBidResponseDto, Long> probe = PagingProbe.walk(
                    page -> service.getSaleBids("seller@test.com", null, null, PageRequest.of(page, pageSize)),
                    StyleBidResponseDto::getSaleBidId);
            probe.assertConsistent();
            assertThat(probe.keys).containsAll(listed);
        }
    }

    @Test
    void totalMatchesReachableRowsWithStatusFilter() {
        PagingProbe<StyleBidResponseDto, Long> probe = PagingProbe.walk(
                page -> service.getSaleBids("seller@test.com", "PENDING", null, PageRequest.of(page, 2)),
                StyleBidResponseDto::getSaleBidId);
        probe.assertConsistent();
        assertThat(probe.keys).containsAll(listed);
    }
}
