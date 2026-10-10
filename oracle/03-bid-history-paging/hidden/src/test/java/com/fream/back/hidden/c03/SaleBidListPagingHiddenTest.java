package com.fream.back.hidden.c03;

import com.fream.back.domain.product.entity.Product;
import com.fream.back.domain.product.entity.ProductSize;
import com.fream.back.domain.sale.dto.SaleBidResponseDto;
import com.fream.back.domain.sale.entity.BidStatus;
import com.fream.back.domain.sale.entity.SaleBid;
import com.fream.back.domain.sale.service.query.SaleBidQueryService;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** 지점 2: 판매 입찰 내역 목록. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({QueryDslConfig.class, SaleBidQueryService.class})
class SaleBidListPagingHiddenTest {

    @Autowired EntityManager em;
    @Autowired SaleBidQueryService service;

    private final List<Long> listed = new ArrayList<>();
    private int day = 0;

    @BeforeEach
    void setUp() {
        Fixtures f = new Fixtures(em);
        User seller = f.user("seller@test.com");
        User other = f.user("other@test.com");
        Product p = f.product("Jordan", f.brand("Nike"));
        ProductSize withImage = f.size(f.color(p, "Black", "black.jpg"), "260", 100000);
        ProductSize withImage2 = f.size(f.color(p, "White", "white.jpg"), "270", 110000);
        ProductSize noImage = f.size(f.color(p, "Green", null), "250", 120000);

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

    private Long bid(User seller, ProductSize size, int price) {
        SaleBid bid = SaleBid.builder().seller(seller).productSize(size).bidPrice(price).status(BidStatus.PENDING).build();
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
            PagingProbe<SaleBidResponseDto, Long> probe = PagingProbe.walk(
                    page -> service.getSaleBids("seller@test.com", null, null, PageRequest.of(page, pageSize)),
                    SaleBidResponseDto::getSaleBidId);
            probe.assertConsistent();
            assertThat(probe.keys).containsAll(listed);
        }
    }

    @Test
    void totalMatchesReachableRowsWithStatusFilter() {
        PagingProbe<SaleBidResponseDto, Long> probe = PagingProbe.walk(
                page -> service.getSaleBids("seller@test.com", "PENDING", null, PageRequest.of(page, 2)),
                SaleBidResponseDto::getSaleBidId);
        probe.assertConsistent();
        assertThat(probe.keys).containsAll(listed);
    }
}
