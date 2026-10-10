package com.fream.back.domain.product.service;

import com.fream.back.domain.product.entity.Category;
import com.fream.back.domain.product.entity.ProductColor;
import com.fream.back.domain.product.entity.ProductSize;
import com.fream.back.domain.product.entity.enumType.SizeType;
import com.fream.back.domain.product.exception.ProductErrorCode;
import com.fream.back.domain.product.exception.ProductException;
import com.fream.back.domain.product.repository.ProductSizeRepository;
import com.fream.back.domain.product.service.category.CategoryQueryService;
import com.fream.back.domain.product.service.productSize.ProductSizeCommandService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ProductSizeCommandService 사이즈 생성 규칙(카테고리별 SizeType·중복 제외·초기 가격/수량)과 수정·삭제 고정.
 */
@ExtendWith(MockitoExtension.class)
class ProductSizeCommandServiceTest {

    @Mock private ProductSizeRepository productSizeRepository;
    @Mock private CategoryQueryService categoryQueryService;

    @InjectMocks
    private ProductSizeCommandService service;

    private final ProductColor color = ProductColor.builder().id(7L).colorName("Black").build();

    private ProductSize existing(long id, String size) {
        return ProductSize.builder().id(id).size(size).sizeType(SizeType.SHOES)
                .purchasePrice(1).salePrice(1).quantity(1).productColor(color).build();
    }

    @Test
    void createSizesSkipsExistingAndInitializesPricesAndQuantity() {
        when(categoryQueryService.findRootCategoryById(5L)).thenReturn(Category.builder().id(1L).name("shoes").build());
        when(productSizeRepository.findAllByProductColorId(7L)).thenReturn(List.of(existing(1L, "250")));

        service.createProductSizes(color, 5L, List.of("250", "260", "270"), 159000);

        ArgumentCaptor<ProductSize> captor = ArgumentCaptor.forClass(ProductSize.class);
        verify(productSizeRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(ProductSize::getSize, ProductSize::getSizeType, ProductSize::getPurchasePrice,
                        ProductSize::getSalePrice, ProductSize::getQuantity)
                .containsExactly(
                        tuple("260", SizeType.SHOES, 159000, 159000, 0),
                        tuple("270", SizeType.SHOES, 159000, 159000, 0));
        assertThat(captor.getAllValues()).allSatisfy(s -> assertThat(s.getProductColor()).isSameAs(color));
    }

    @Test
    void createSizesSavesValidOnesBeforeRejectingInvalidOnes() {
        when(categoryQueryService.findRootCategoryById(5L)).thenReturn(Category.builder().name("Clothing").build());
        when(productSizeRepository.findAllByProductColorId(7L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.createProductSizes(color, 5L, List.of("M", "250", "XL", "FREE"), 10000))
                .isInstanceOf(ProductException.class)
                .hasMessage("유효하지 않은 사이즈가 있습니다: 250, FREE")
                .satisfies(e -> assertThat(((ProductException) e).getErrorCode()).isEqualTo(ProductErrorCode.INVALID_SIZE_TYPE));

        ArgumentCaptor<ProductSize> captor = ArgumentCaptor.forClass(ProductSize.class);
        verify(productSizeRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues()).extracting(ProductSize::getSize).containsExactly("M", "XL");
    }

    @Test
    void createSizesForUnknownRootCategoryThrowsInvalidSizeType() {
        when(categoryQueryService.findRootCategoryById(5L)).thenReturn(Category.builder().name("Bags").build());

        assertThatThrownBy(() -> service.createProductSizes(color, 5L, List.of("ONE_SIZE"), 10000))
                .isInstanceOf(ProductException.class)
                .hasMessage("해당 카테고리에 맞는 SizeType이 존재하지 않습니다: Bags");
        verify(productSizeRepository, never()).save(any());
    }

    @Test
    void createSizesWrapsUnexpectedFailure() {
        when(categoryQueryService.findRootCategoryById(5L)).thenReturn(Category.builder().name("ACCESSORIES").build());
        when(productSizeRepository.findAllByProductColorId(7L)).thenThrow(new IllegalStateException("db"));

        assertThatThrownBy(() -> service.createProductSizes(color, 5L, List.of("ONE_SIZE"), 10000))
                .isInstanceOf(ProductException.class)
                .satisfies(e -> assertThat(((ProductException) e).getErrorCode()).isEqualTo(ProductErrorCode.PRODUCT_SIZE_CREATION_FAILED));
    }

    @Test
    void updateSizeChangesPricesAndQuantity() {
        ProductSize size = existing(3L, "260");
        when(productSizeRepository.findById(3L)).thenReturn(Optional.of(size));

        service.updateProductSize(3L, 210000, 190000, 4);

        assertThat(size.getPurchasePrice()).isEqualTo(210000);
        assertThat(size.getSalePrice()).isEqualTo(190000);
        assertThat(size.getQuantity()).isEqualTo(4);
    }

    @Test
    void deleteAllSizesDeletesEachThenFlushesAndClearsCollection() {
        ProductSize s1 = existing(1L, "250");
        ProductSize s2 = existing(2L, "260");
        color.getSizes().add(s1);
        color.getSizes().add(s2);
        when(productSizeRepository.findAllByProductColorId(7L)).thenReturn(List.of(s1, s2));
        when(productSizeRepository.findById(1L)).thenReturn(Optional.of(s1));
        when(productSizeRepository.findById(2L)).thenReturn(Optional.of(s2));

        service.deleteAllSizesByProductColor(color);

        verify(productSizeRepository).delete(s1);
        verify(productSizeRepository).delete(s2);
        verify(productSizeRepository).flush();
        assertThat(color.getSizes()).isEmpty();
    }

    @Test
    void deleteAllSizesRewrapsNotFoundAsDeletionFailed() {
        ProductSize s1 = existing(1L, "250");
        when(productSizeRepository.findAllByProductColorId(7L)).thenReturn(List.of(s1));
        when(productSizeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteAllSizesByProductColor(color))
                .isInstanceOf(ProductException.class)
                .satisfies(e -> assertThat(((ProductException) e).getErrorCode()).isEqualTo(ProductErrorCode.PRODUCT_SIZE_DELETION_FAILED))
                .hasCauseInstanceOf(ProductException.class);
        verify(productSizeRepository, never()).flush();
    }
}
