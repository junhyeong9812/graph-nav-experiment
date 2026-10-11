package com.fream.back.hidden.c03;

import com.fream.back.domain.m02.entity.Xkr46;
import com.fream.back.domain.m02.entity.Xdh20;
import com.fream.back.domain.m10.dto.Xzz58ResponseDto;
import com.fream.back.domain.m10.entity.Xtz73Status;
import com.fream.back.domain.m10.entity.Xzz58;
import com.fream.back.domain.m10.service.query.Xzz58QueryService;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** 지점 2: 판매 입찰 내역 목록. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({Xsd33Config.class, Xzz58QueryService.class})
class SaleBidListPagingHiddenTest {

    @Autowired EntityManager em;
    @Autowired Xzz58QueryService service;

    private final List<Long> listed = new ArrayList<>();
    private int day = 0;

    @BeforeEach
    void setUp() {
        Fixtures f = new Fixtures(em);
        Xrd81 seller = f.user("seller@test.com");
        Xrd81 other = f.user("other@test.com");
        Xkr46 p = f.product("Jordan", f.brand("Nike"));
        Xdh20 withImage = f.size(f.color(p, "Black", "black.jpg"), "260", 100000);
        Xdh20 withImage2 = f.size(f.color(p, "White", "white.jpg"), "270", 110000);
        Xdh20 noImage = f.size(f.color(p, "Green", null), "250", 120000);

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

    private Long bid(Xrd81 seller, Xdh20 size, int price) {
        Xzz58 bid = Xzz58.builder().seller(seller).productSize(size).bidPrice(price).status(Xtz73Status.PENDING).build();
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
            PagingProbe<Xzz58ResponseDto, Long> probe = PagingProbe.walk(
                    page -> service.getSaleBids("seller@test.com", null, null, PageRequest.of(page, pageSize)),
                    Xzz58ResponseDto::getSaleBidId);
            probe.assertConsistent();
            assertThat(probe.keys).containsAll(listed);
        }
    }

    @Test
    void totalMatchesReachableRowsWithStatusFilter() {
        PagingProbe<Xzz58ResponseDto, Long> probe = PagingProbe.walk(
                page -> service.getSaleBids("seller@test.com", "PENDING", null, PageRequest.of(page, 2)),
                Xzz58ResponseDto::getSaleBidId);
        probe.assertConsistent();
        assertThat(probe.keys).containsAll(listed);
    }
}
