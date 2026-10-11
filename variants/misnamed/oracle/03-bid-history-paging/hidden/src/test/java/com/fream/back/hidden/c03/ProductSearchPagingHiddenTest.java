package com.fream.back.hidden.c03;

import com.fream.back.domain.user.dto.UserSearchResponseDto;
import com.fream.back.domain.user.entity.Brand;
import com.fream.back.domain.user.entity.User;
import com.fream.back.domain.user.entity.UserColor;
import com.fream.back.domain.user.repository.IndexedUserRepository;
import com.fream.back.domain.user.repository.UserQueryDslRepository;
import com.fream.back.domain.user.repository.SortOption;
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

/** 지점 3: 상품 검색(ID 필터) 목록. */
class ProductSearchPagingHiddenTest extends ProductSearchFixture {

    @Test
    void searchByIdsTotalMatchesReachableRows() {
        for (int size = 2; size <= 4; size++) {
            int pageSize = size;
            PagingProbe<UserSearchResponseDto, Long> probe = PagingProbe.walk(
                    page -> productRepository.searchProducts(null, null, null, null, null, null, null, null, null,
                            byPrice, PageRequest.of(page, pageSize)),
                    UserSearchResponseDto::getColorId);
            probe.assertConsistent();
            // 지금 검색 결과에 나오는 색상 행(썸네일 없는 색상 포함)은 그대로 나와야 한다
            assertThat(probe.keys).containsExactlyInAnyOrderElementsOf(allColors);
        }
    }

    @Test
    void searchByIdsWithBrandFilterTotalMatchesReachableRows() {
        PagingProbe<UserSearchResponseDto, Long> probe = PagingProbe.walk(
                page -> productRepository.searchProducts(null, null, null, List.of(nike.getId()), null, null, null,
                        null, null, byPrice, PageRequest.of(page, 2)),
                UserSearchResponseDto::getColorId);
        probe.assertConsistent();
        assertThat(probe.keys).containsExactlyInAnyOrderElementsOf(nikeColorsWithImage);
    }
}
