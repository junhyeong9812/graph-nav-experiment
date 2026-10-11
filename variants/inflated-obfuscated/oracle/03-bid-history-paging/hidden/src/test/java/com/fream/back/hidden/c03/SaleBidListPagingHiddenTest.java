package com.fream.back.hidden.c03;

import com.fream.back.domain.m30.entity.Xnr50;
import com.fream.back.domain.m30.entity.Xcc44;
import com.fream.back.domain.m85.dto.Xzt63ResponseDto;
import com.fream.back.domain.m85.entity.Xkh73Status;
import com.fream.back.domain.m85.entity.Xzt63;
import com.fream.back.domain.m85.service.query.Xzt63QueryService;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** 지점 2: 판매 입찰 내역 목록. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({Xzj77Config.class, Xzt63QueryService.class})
class SaleBidListPagingHiddenTest {

    @Autowired EntityManager em;
    @Autowired Xzt63QueryService service;

    private final List<Long> listed = new ArrayList<>();
    private int day = 0;

    @BeforeEach
    void setUp() {
        Fixtures f = new Fixtures(em);
        Xjm45 seller = f.user("seller@test.com");
        Xjm45 other = f.user("other@test.com");
        Xnr50 p = f.product("Jordan", f.brand("Nike"));
        Xcc44 withImage = f.size(f.color(p, "Black", "black.jpg"), "260", 100000);
        Xcc44 withImage2 = f.size(f.color(p, "White", "white.jpg"), "270", 110000);
        Xcc44 noImage = f.size(f.color(p, "Green", null), "250", 120000);

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

    private Long bid(Xjm45 seller, Xcc44 size, int price) {
        Xzt63 bid = Xzt63.builder().seller(seller).productSize(size).bidPrice(price).status(Xkh73Status.PENDING).build();
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
            PagingProbe<Xzt63ResponseDto, Long> probe = PagingProbe.walk(
                    page -> service.getSaleBids("seller@test.com", null, null, PageRequest.of(page, pageSize)),
                    Xzt63ResponseDto::getSaleBidId);
            probe.assertConsistent();
            assertThat(probe.keys).containsAll(listed);
        }
    }

    @Test
    void totalMatchesReachableRowsWithStatusFilter() {
        PagingProbe<Xzt63ResponseDto, Long> probe = PagingProbe.walk(
                page -> service.getSaleBids("seller@test.com", "PENDING", null, PageRequest.of(page, 2)),
                Xzt63ResponseDto::getSaleBidId);
        probe.assertConsistent();
        assertThat(probe.keys).containsAll(listed);
    }
}
