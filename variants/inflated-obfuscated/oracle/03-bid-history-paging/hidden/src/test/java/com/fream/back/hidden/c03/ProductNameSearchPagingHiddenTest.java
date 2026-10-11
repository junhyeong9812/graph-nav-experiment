package com.fream.back.hidden.c03;

import com.fream.back.domain.m30.dto.Xnr50SearchResponseDto;
import com.fream.back.domain.m30.entity.Xft86;
import com.fream.back.domain.m30.entity.Xnr50;
import com.fream.back.domain.m30.entity.Xvm17;
import com.fream.back.domain.m30.repository.Xdr94Repository;
import com.fream.back.domain.m30.repository.Xgx95Repository;
import com.fream.back.domain.m30.repository.Xgq51;
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

/** 지점 4: 상품 검색(이름 필터) 목록. */
class ProductNameSearchPagingHiddenTest extends ProductSearchFixture {

    @Test
    void searchByNamesTotalMatchesReachableRows() {
        for (int size = 2; size <= 4; size++) {
            int pageSize = size;
            PagingProbe<Xnr50SearchResponseDto, Long> probe = PagingProbe.walk(
                    page -> indexedRepository.searchProductsByNames(null, null, null, null, null, null, null, null,
                            null, byPrice, PageRequest.of(page, pageSize)),
                    Xnr50SearchResponseDto::getColorId);
            probe.assertConsistent();
            assertThat(probe.keys).containsAll(colorsWithImage);
        }
    }

    @Test
    void searchByNamesWithBrandFilterTotalMatchesReachableRows() {
        PagingProbe<Xnr50SearchResponseDto, Long> probe = PagingProbe.walk(
                page -> indexedRepository.searchProductsByNames(null, null, null, List.of("Nike"), null, null, null,
                        null, null, byPrice, PageRequest.of(page, 2)),
                Xnr50SearchResponseDto::getColorId);
        probe.assertConsistent();
        assertThat(probe.keys).containsExactlyInAnyOrderElementsOf(nikeColorsWithImage);
    }
}
