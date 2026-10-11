package com.fream.back.hidden.c03;

import com.fream.back.domain.product.dto.ProductSearchResponseDto;
import com.fream.back.domain.product.entity.Brand;
import com.fream.back.domain.product.entity.Product;
import com.fream.back.domain.product.entity.ProductColor;
import com.fream.back.domain.product.repository.IndexedProductRepository;
import com.fream.back.domain.product.repository.ProductQueryDslRepository;
import com.fream.back.domain.product.repository.SortOption;
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

/** 지점 4: 상품 검색(이름 필터) 목록. */
class ProductNameSearchPagingHiddenTest extends ProductSearchFixture {

    @Test
    void searchByNamesTotalMatchesReachableRows() {
        for (int size = 2; size <= 4; size++) {
            int pageSize = size;
            PagingProbe<ProductSearchResponseDto, Long> probe = PagingProbe.walk(
                    page -> indexedRepository.searchProductsByNames(null, null, null, null, null, null, null, null,
                            null, byPrice, PageRequest.of(page, pageSize)),
                    ProductSearchResponseDto::getColorId);
            probe.assertConsistent();
            assertThat(probe.keys).containsAll(colorsWithImage);
        }
    }

    @Test
    void searchByNamesWithBrandFilterTotalMatchesReachableRows() {
        PagingProbe<ProductSearchResponseDto, Long> probe = PagingProbe.walk(
                page -> indexedRepository.searchProductsByNames(null, null, null, List.of("Nike"), null, null, null,
                        null, null, byPrice, PageRequest.of(page, 2)),
                ProductSearchResponseDto::getColorId);
        probe.assertConsistent();
        assertThat(probe.keys).containsExactlyInAnyOrderElementsOf(nikeColorsWithImage);
    }
}
