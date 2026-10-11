package com.fream.back.hidden.c03;

import com.fream.back.domain.m16.dto.Xzj27ResponseDto;
import com.fream.back.domain.m16.entity.Xtz73Status;
import com.fream.back.domain.m16.entity.Xzj27;
import com.fream.back.domain.m16.service.query.Xzj27QueryService;
import com.fream.back.domain.m02.entity.Xkr46;
import com.fream.back.domain.m02.entity.Xdh20;
import com.fream.back.domain.m13.entity.Xrd81;
import com.fream.back.global.config.Xsd33Config;
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
@Import({Xsd33Config.class, Xzj27QueryService.class})
class PurchaseBidListPagingHiddenTest {

    @Autowired EntityManager em;
    @Autowired Xzj27QueryService service;

    private final List<Long> listed = new ArrayList<>();

    @BeforeEach
    void setUp() {
        Fixtures f = new Fixtures(em);
        Xrd81 buyer = f.user("buyer@test.com");
        Xrd81 other = f.user("other@test.com");
        Xkr46 p = f.product("Dunk", f.brand("Nike"));
        Xdh20 withImage = f.size(f.color(p, "Black", "black.jpg"), "260", 100000);
        Xdh20 withImage2 = f.size(f.color(p, "White", "white.jpg"), "270", 110000);
        Xdh20 noImage = f.size(f.color(p, "Green", null), "250", 120000);

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

    private Long bid(Xrd81 user, Xdh20 size, int price) {
        Xzj27 bid = Xzj27.builder().user(user).productSize(size).bidPrice(price).status(Xtz73Status.PENDING).build();
        em.persist(bid);
        return bid.getId();
    }

    @Test
    void totalMatchesReachableRowsForEveryPageSize() {
        for (int size = 1; size <= 3; size++) {
            int pageSize = size;
            PagingProbe<Xzj27ResponseDto, Long> probe = PagingProbe.walk(
                    page -> service.getOrderBids("buyer@test.com", null, null, PageRequest.of(page, pageSize)),
                    Xzj27ResponseDto::getOrderBidId);
            probe.assertConsistent();
            assertThat(probe.keys).containsAll(listed);
        }
    }

    @Test
    void totalMatchesReachableRowsWithStatusFilter() {
        PagingProbe<Xzj27ResponseDto, Long> probe = PagingProbe.walk(
                page -> service.getOrderBids("buyer@test.com", "PENDING", null, PageRequest.of(page, 2)),
                Xzj27ResponseDto::getOrderBidId);
        probe.assertConsistent();
        assertThat(probe.keys).containsAll(listed);
    }
}
