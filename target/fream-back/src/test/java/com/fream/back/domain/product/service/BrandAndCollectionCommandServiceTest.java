package com.fream.back.domain.product.service;

import com.fream.back.domain.product.dto.BrandRequestDto;
import com.fream.back.domain.product.dto.BrandResponseDto;
import com.fream.back.domain.product.dto.CollectionRequestDto;
import com.fream.back.domain.product.entity.Brand;
import com.fream.back.domain.product.entity.Collection;
import com.fream.back.domain.product.exception.ProductErrorCode;
import com.fream.back.domain.product.exception.ProductException;
import com.fream.back.domain.product.repository.BrandRepository;
import com.fream.back.domain.product.repository.CollectionRepository;
import com.fream.back.domain.product.repository.ProductRepository;
import com.fream.back.domain.product.service.brand.BrandCommandService;
import com.fream.back.domain.product.service.collection.CollectionCommandService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * BrandCommandService·CollectionCommandService 중복 검사, 사용 중 삭제 차단, createOrGet 고정.
 */
@ExtendWith(MockitoExtension.class)
class BrandAndCollectionCommandServiceTest {

    @Mock private BrandRepository brandRepository;
    @Mock private CollectionRepository collectionRepository;
    @Mock private ProductRepository productRepository;

    private BrandCommandService brandService;
    private CollectionCommandService collectionService;

    @BeforeEach
    void setUp() {
        brandService = new BrandCommandService(brandRepository, productRepository);
        collectionService = new CollectionCommandService(collectionRepository, productRepository);
    }

    private static ProductErrorCode code(Throwable e) {
        return (ProductErrorCode) ((ProductException) e).getErrorCode();
    }

    @Test
    void createBrandRejectsDuplicateName() {
        when(brandRepository.existsByName("Nike")).thenReturn(true);

        assertThatThrownBy(() -> brandService.createBrand(new BrandRequestDto("Nike")))
                .isInstanceOf(ProductException.class)
                .satisfies(e -> assertThat(code(e)).isEqualTo(ProductErrorCode.BRAND_ALREADY_EXISTS));
        verify(brandRepository, never()).save(any());
    }

    @Test
    void updateBrandToSameNameSkipsDuplicateCheckAndBlankNameIsIgnored() {
        Brand brand = Brand.builder().id(5L).name("Nike").build();
        when(brandRepository.findById(5L)).thenReturn(Optional.of(brand));

        assertThat(brandService.updateBrand(5L, new BrandRequestDto("Nike")).getName()).isEqualTo("Nike");
        verify(brandRepository, never()).existsByName(any());

        when(brandRepository.existsByName("  ")).thenReturn(false);
        assertThat(brandService.updateBrand(5L, new BrandRequestDto("  ")).getName()).isEqualTo("Nike");
    }

    @Test
    void updateBrandRejectsNameTakenByAnotherBrand() {
        when(brandRepository.findById(5L)).thenReturn(Optional.of(Brand.builder().id(5L).name("Nike").build()));
        when(brandRepository.existsByName("Adidas")).thenReturn(true);

        assertThatThrownBy(() -> brandService.updateBrand(5L, new BrandRequestDto("Adidas")))
                .satisfies(e -> assertThat(code(e)).isEqualTo(ProductErrorCode.BRAND_ALREADY_EXISTS));
    }

    @Test
    void deleteBrandInUseIsRejected() {
        Brand brand = Brand.builder().id(5L).name("Nike").build();
        when(brandRepository.findByName("Nike")).thenReturn(Optional.of(brand));
        when(productRepository.existsByBrand(brand)).thenReturn(true);

        assertThatThrownBy(() -> brandService.deleteBrand("Nike"))
                .satisfies(e -> assertThat(code(e)).isEqualTo(ProductErrorCode.BRAND_IN_USE));
        verify(brandRepository, never()).delete(any());
    }

    @Test
    void createOrGetCollectionReturnsExistingWithoutSaving() {
        Collection existing = Collection.builder().id(3L).name("Jordan").build();
        when(collectionRepository.findByName("Jordan")).thenReturn(Optional.of(existing));

        assertThat(collectionService.createOrGetCollection("Jordan")).isSameAs(existing);
        verify(collectionRepository, never()).save(any());
    }

    @Test
    void createOrGetCollectionCreatesWhenMissing() {
        when(collectionRepository.findByName("Dunk")).thenReturn(Optional.empty());
        when(collectionRepository.save(any(Collection.class))).thenAnswer(inv -> inv.getArgument(0));

        Collection created = collectionService.createOrGetCollection("Dunk");

        assertThat(created.getName()).isEqualTo("Dunk");
        verify(collectionRepository).save(any(Collection.class));
    }

    @Test
    void createCollectionRejectsDuplicateAndDeleteInUseIsRejected() {
        Collection existing = Collection.builder().id(3L).name("Jordan").build();
        when(collectionRepository.findByName("Jordan")).thenReturn(Optional.of(existing));
        when(productRepository.existsByCollection(existing)).thenReturn(true);

        assertThatThrownBy(() -> collectionService.createCollection(new CollectionRequestDto("Jordan")))
                .satisfies(e -> assertThat(code(e)).isEqualTo(ProductErrorCode.COLLECTION_ALREADY_EXISTS));
        assertThatThrownBy(() -> collectionService.deleteCollection("Jordan"))
                .satisfies(e -> assertThat(code(e)).isEqualTo(ProductErrorCode.COLLECTION_IN_USE));
        verify(collectionRepository, never()).delete(any());
    }
}
