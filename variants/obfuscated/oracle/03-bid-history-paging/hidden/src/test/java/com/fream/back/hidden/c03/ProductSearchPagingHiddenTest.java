package com.fream.back.hidden.c03;

import com.fream.back.domain.m02.dto.Xkr46SearchResponseDto;
import com.fream.back.domain.m02.entity.Xfw75;
import com.fream.back.domain.m02.entity.Xkr46;
import com.fream.back.domain.m02.entity.Xfp92;
import com.fream.back.domain.m02.repository.Xvw73Repository;
import com.fream.back.domain.m02.repository.Xmb97Repository;
import com.fream.back.domain.m02.repository.Xpk87;
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

/** 지점 3: 상품 검색(ID 필터) 목록. */
class ProductSearchPagingHiddenTest extends ProductSearchFixture {

    @Test
    void searchByIdsTotalMatchesReachableRows() {
        for (int size = 2; size <= 4; size++) {
            int pageSize = size;
            PagingProbe<Xkr46SearchResponseDto, Long> probe = PagingProbe.walk(
                    page -> productRepository.searchProducts(null, null, null, null, null, null, null, null, null,
                            byPrice, PageRequest.of(page, pageSize)),
                    Xkr46SearchResponseDto::getColorId);
            probe.assertConsistent();
            // 지금 검색 결과에 나오는 색상 행(썸네일 없는 색상 포함)은 그대로 나와야 한다
            assertThat(probe.keys).containsExactlyInAnyOrderElementsOf(allColors);
        }
    }

    @Test
    void searchByIdsWithBrandFilterTotalMatchesReachableRows() {
        PagingProbe<Xkr46SearchResponseDto, Long> probe = PagingProbe.walk(
                page -> productRepository.searchProducts(null, null, null, List.of(nike.getId()), null, null, null,
                        null, null, byPrice, PageRequest.of(page, 2)),
                Xkr46SearchResponseDto::getColorId);
        probe.assertConsistent();
        assertThat(probe.keys).containsExactlyInAnyOrderElementsOf(nikeColorsWithImage);
    }
}
