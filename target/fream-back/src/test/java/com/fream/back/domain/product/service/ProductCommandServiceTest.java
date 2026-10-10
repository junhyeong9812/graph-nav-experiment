package com.fream.back.domain.product.service;

import com.fream.back.domain.product.dto.ProductCreateRequestDto;
import com.fream.back.domain.product.dto.ProductCreateResponseDto;
import com.fream.back.domain.product.dto.ProductUpdateRequestDto;
import com.fream.back.domain.product.dto.ProductUpdateResponseDto;
import com.fream.back.domain.product.entity.*;
import com.fream.back.domain.product.entity.enumType.GenderType;
import com.fream.back.domain.product.exception.ProductErrorCode;
import com.fream.back.domain.product.exception.ProductException;
import com.fream.back.domain.product.repository.ProductRepository;
import com.fream.back.domain.product.service.brand.BrandEntityService;
import com.fream.back.domain.product.service.category.CategoryEntityService;
import com.fream.back.domain.product.service.collection.CollectionCommandService;
import com.fream.back.domain.product.service.product.ProductCommandService;
import com.fream.back.domain.product.service.productColor.ProductColorCommandService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * ProductCommandService 생성·수정·삭제 흐름과 예외 변환 고정.
 */
@ExtendWith(MockitoExtension.class)
class ProductCommandServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private BrandEntityService brandEntityService;
    @Mock private CategoryEntityService categoryEntityService;
    @Mock private CollectionCommandService collectionCommandService;
    @Mock private ProductColorCommandService productColorCommandService;

    @InjectMocks
    private ProductCommandService service;

    private final Brand nike = Brand.builder().id(1L).name("Nike").build();
    private final Category sneakers = Category.builder().id(2L).name("Sneakers").build();
    private final Collection jordan = Collection.builder().id(3L).name("Jordan").build();

    private ProductCreateRequestDto createRequest(String collectionName) {
        return ProductCreateRequestDto.builder()
                .name("Air Jordan 1").englishName("AJ1").releasePrice(200000)
                .modelNumber("555088").releaseDate("2023-01-01")
                .brandName("Nike").mainCategoryName("Shoes").categoryName("Sneakers")
                .collectionName(collectionName).gender(GenderType.MALE)
                .build();
    }

    @Test
    void createProductResolvesReferencesAndSavesProduct() {
        when(brandEntityService.findByName("Nike")).thenReturn(nike);
        when(categoryEntityService.findSubCategoryByName("Sneakers", "Shoes")).thenReturn(sneakers);
        when(collectionCommandService.createOrGetCollection("Jordan")).thenReturn(jordan);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductCreateResponseDto response = service.createProduct(createRequest("Jordan"));

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        Product saved = captor.getValue();
        assertThat(saved.getBrand()).isSameAs(nike);
        assertThat(saved.getCategory()).isSameAs(sneakers);
        assertThat(saved.getCollection()).isSameAs(jordan);
        assertThat(saved.getGender()).isEqualTo(GenderType.MALE);
        assertThat(saved.getReleasePrice()).isEqualTo(200000);

        assertThat(response.getBrandName()).isEqualTo("Nike");
        assertThat(response.getCategoryName()).isEqualTo("Sneakers");
        assertThat(response.getCollectionName()).isEqualTo("Jordan");
        assertThat(response.getModelNumber()).isEqualTo("555088");
    }

    @Test
    void createProductWithoutCollectionDoesNotTouchCollectionService() {
        when(brandEntityService.findByName("Nike")).thenReturn(nike);
        when(categoryEntityService.findSubCategoryByName("Sneakers", "Shoes")).thenReturn(sneakers);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductCreateResponseDto response = service.createProduct(createRequest(null));

        verifyNoInteractions(collectionCommandService);
        assertThat(response.getCollectionName()).isNull();
    }

    @Test
    void createProductPropagatesProductExceptionAsIs() {
        ProductException notFound = new ProductException(ProductErrorCode.BRAND_NOT_FOUND, "no brand");
        when(brandEntityService.findByName("Nike")).thenThrow(notFound);

        assertThatThrownBy(() -> service.createProduct(createRequest(null))).isSameAs(notFound);
        verify(productRepository, never()).save(any());
    }

    @Test
    void createProductWrapsUnexpectedException() {
        when(brandEntityService.findByName("Nike")).thenReturn(nike);
        when(categoryEntityService.findSubCategoryByName(anyString(), anyString())).thenReturn(sneakers);
        when(productRepository.save(any(Product.class))).thenThrow(new RuntimeException("db down"));

        assertThatThrownBy(() -> service.createProduct(createRequest(null)))
                .isInstanceOf(ProductException.class)
                .satisfies(e -> assertThat(((ProductException) e).getErrorCode()).isEqualTo(ProductErrorCode.PRODUCT_CREATION_FAILED))
                .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    void updateProductForUnknownIdThrowsNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateProduct(99L, new ProductUpdateRequestDto()))
                .isInstanceOf(ProductException.class)
                .satisfies(e -> assertThat(((ProductException) e).getErrorCode()).isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND));
    }

    @Test
    void updateProductChangesOnlyProvidedFieldsAndEchoesRequestGender() {
        Product product = Product.builder().id(10L).name("Old").englishName("OldEn").releasePrice(100)
                .modelNumber("M1").releaseDate("2020-01-01").gender(GenderType.FEMALE)
                .brand(nike).category(sneakers).build();
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        ProductUpdateRequestDto request = ProductUpdateRequestDto.builder()
                .name("New").releasePrice(300)
                // 카테고리는 메인 카테고리 없이 이름만 주면 무시된다
                .categoryName("Other")
                .build();

        ProductUpdateResponseDto response = service.updateProduct(10L, request);

        assertThat(product.getName()).isEqualTo("New");
        assertThat(product.getEnglishName()).isEqualTo("OldEn");
        assertThat(product.getReleasePrice()).isEqualTo(300);
        assertThat(product.getGender()).isEqualTo(GenderType.FEMALE);
        assertThat(product.getCategory()).isSameAs(sneakers);
        verifyNoInteractions(brandEntityService, categoryEntityService, collectionCommandService);

        assertThat(response.getName()).isEqualTo("New");
        assertThat(response.getBrandName()).isEqualTo("Nike");
        assertThat(response.getCollectionName()).isNull();
        // 응답 gender는 엔티티가 아니라 요청값(null)
        assertThat(response.getGender()).isNull();
    }

    @Test
    void updateProductWithCollectionNameCreatesOrGetsCollection() {
        Product product = Product.builder().id(10L).name("Old").englishName("OldEn").releasePrice(100)
                .modelNumber("M1").releaseDate("2020-01-01").gender(GenderType.FEMALE)
                .brand(nike).category(sneakers).build();
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(collectionCommandService.createOrGetCollection("Jordan")).thenReturn(jordan);

        ProductUpdateResponseDto response = service.updateProduct(10L,
                ProductUpdateRequestDto.builder().collectionName("Jordan").gender(GenderType.KIDS).build());

        assertThat(product.getCollection()).isSameAs(jordan);
        assertThat(product.getGender()).isEqualTo(GenderType.KIDS);
        assertThat(response.getCollectionName()).isEqualTo("Jordan");
        assertThat(response.getGender()).isEqualTo(GenderType.KIDS);
    }

    @Test
    void deleteProductDeletesEachColorThenProduct() {
        Product product = Product.builder().id(10L).name("P").build();
        product.addProductColor(ProductColor.builder().id(100L).colorName("Black").build());
        product.addProductColor(ProductColor.builder().id(101L).colorName("White").build());
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        service.deleteProduct(10L);

        InOrder inOrder = inOrder(productColorCommandService, productRepository);
        inOrder.verify(productColorCommandService).deleteProductColor(100L);
        inOrder.verify(productColorCommandService).deleteProductColor(101L);
        inOrder.verify(productRepository).delete(product);
    }

    @Test
    void deleteProductWrapsColorDeletionFailure() {
        Product product = Product.builder().id(10L).name("P").build();
        product.addProductColor(ProductColor.builder().id(100L).colorName("Black").build());
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        doThrow(new IllegalStateException("boom")).when(productColorCommandService).deleteProductColor(100L);

        assertThatThrownBy(() -> service.deleteProduct(10L))
                .isInstanceOf(ProductException.class)
                .satisfies(e -> assertThat(((ProductException) e).getErrorCode()).isEqualTo(ProductErrorCode.PRODUCT_DELETION_FAILED));
        verify(productRepository, never()).delete(any());
    }
}
