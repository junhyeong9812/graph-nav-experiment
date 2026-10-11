package com.fream.back.hidden.c03;

import com.fream.back.domain.m99.dto.Xtw92ResponseDto;
import com.fream.back.domain.m99.entity.Xkh73Status;
import com.fream.back.domain.m99.entity.Xtw92;
import com.fream.back.domain.m99.service.query.Xtw92QueryService;
import com.fream.back.domain.m30.entity.Xnr50;
import com.fream.back.domain.m30.entity.Xcc44;
import com.fream.back.domain.m22.entity.Xjm45;
import com.fream.back.global.config.Xzj77Config;
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
@Import({Xzj77Config.class, Xtw92QueryService.class})
class PurchaseBidListPagingHiddenTest {

    @Autowired EntityManager em;
    @Autowired Xtw92QueryService service;

    private final List<Long> listed = new ArrayList<>();

    @BeforeEach
    void setUp() {
        Fixtures f = new Fixtures(em);
        Xjm45 buyer = f.user("buyer@test.com");
        Xjm45 other = f.user("other@test.com");
        Xnr50 p = f.product("Dunk", f.brand("Nike"));
        Xcc44 withImage = f.size(f.color(p, "Black", "black.jpg"), "260", 100000);
        Xcc44 withImage2 = f.size(f.color(p, "White", "white.jpg"), "270", 110000);
        Xcc44 noImage = f.size(f.color(p, "Green", null), "250", 120000);

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

    private Long bid(Xjm45 user, Xcc44 size, int price) {
        Xtw92 bid = Xtw92.builder().user(user).productSize(size).bidPrice(price).status(Xkh73Status.PENDING).build();
        em.persist(bid);
        return bid.getId();
    }

    @Test
    void totalMatchesReachableRowsForEveryPageSize() {
        for (int size = 1; size <= 3; size++) {
            int pageSize = size;
            PagingProbe<Xtw92ResponseDto, Long> probe = PagingProbe.walk(
                    page -> service.getOrderBids("buyer@test.com", null, null, PageRequest.of(page, pageSize)),
                    Xtw92ResponseDto::getOrderBidId);
            probe.assertConsistent();
            assertThat(probe.keys).containsAll(listed);
        }
    }

    @Test
    void totalMatchesReachableRowsWithStatusFilter() {
        PagingProbe<Xtw92ResponseDto, Long> probe = PagingProbe.walk(
                page -> service.getOrderBids("buyer@test.com", "PENDING", null, PageRequest.of(page, 2)),
                Xtw92ResponseDto::getOrderBidId);
        probe.assertConsistent();
        assertThat(probe.keys).containsAll(listed);
    }
}
