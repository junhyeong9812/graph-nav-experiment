package com.fream.back.domain.product.service;

import com.fream.back.domain.product.entity.Interest;
import com.fream.back.domain.product.entity.ProductColor;
import com.fream.back.domain.product.exception.ProductErrorCode;
import com.fream.back.domain.product.exception.ProductException;
import com.fream.back.domain.product.repository.InterestRepository;
import com.fream.back.domain.product.service.interest.InterestCommandService;
import com.fream.back.domain.product.service.productColor.ProductColorQueryService;
import com.fream.back.domain.user.entity.User;
import com.fream.back.domain.user.service.query.UserQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ConcurrentModificationException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * InterestCommandService 관심 토글(추가/삭제)과 색상별 일괄 삭제 고정.
 */
@ExtendWith(MockitoExtension.class)
class InterestCommandServiceTest {

    @Mock private InterestRepository interestRepository;
    @Mock private UserQueryService userQueryService;
    @Mock private ProductColorQueryService productColorQueryService;

    @InjectMocks
    private InterestCommandService service;

    private final User user = User.builder().id(1L).email("a@test.com").build();
    private final ProductColor color = ProductColor.builder().id(7L).colorName("Black").build();

    @Test
    void toggleAddsInterestWhenAbsentAndLinksBothSides() {
        when(userQueryService.findByEmail("a@test.com")).thenReturn(user);
        when(productColorQueryService.findById(7L)).thenReturn(color);
        when(interestRepository.findByUserAndProductColor(user, color)).thenReturn(Optional.empty());
        when(interestRepository.save(any(Interest.class))).thenAnswer(inv -> inv.getArgument(0));

        service.toggleInterest("a@test.com", 7L);

        ArgumentCaptor<Interest> captor = ArgumentCaptor.forClass(Interest.class);
        verify(interestRepository).save(captor.capture());
        Interest saved = captor.getValue();
        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.getProductColor()).isSameAs(color);
        assertThat(user.getInterests()).containsExactly(saved);
        assertThat(color.getInterests()).containsExactly(saved);
        verify(interestRepository, never()).delete(any());
    }

    @Test
    void toggleRemovesInterestWhenPresentAndUnlinksBothSides() {
        Interest existing = Interest.builder().id(9L).build();
        existing.assignUser(user);
        existing.assignProductColor(color);
        when(userQueryService.findByEmail("a@test.com")).thenReturn(user);
        when(productColorQueryService.findById(7L)).thenReturn(color);
        when(interestRepository.findByUserAndProductColor(user, color)).thenReturn(Optional.of(existing));

        service.toggleInterest("a@test.com", 7L);

        verify(interestRepository).delete(existing);
        verify(interestRepository, never()).save(any());
        assertThat(existing.getUser()).isNull();
        assertThat(existing.getProductColor()).isNull();
        assertThat(user.getInterests()).isEmpty();
        assertThat(color.getInterests()).isEmpty();
    }

    @Test
    void toggleWrapsIllegalArgumentWithOriginalMessage() {
        when(userQueryService.findByEmail("a@test.com")).thenThrow(new IllegalArgumentException("사용자 없음"));

        assertThatThrownBy(() -> service.toggleInterest("a@test.com", 7L))
                .isInstanceOf(ProductException.class)
                .hasMessage("사용자 없음")
                .satisfies(e -> assertThat(((ProductException) e).getErrorCode()).isEqualTo(ProductErrorCode.INTEREST_TOGGLE_FAILED));
    }

    @Test
    void deleteAllInterestsWithNoInterestsSucceeds() {
        service.deleteAllInterestsByProductColor(color);

        verifyNoInteractions(interestRepository);
        assertThat(color.getInterests()).isEmpty();
    }

    @Test
    void deleteAllInterestsFailsWhenListIsModifiedDuringIteration() {
        Interest existing = Interest.builder().id(9L).build();
        existing.assignUser(user);
        existing.assignProductColor(color);

        // 순회 중 unassignProductColor()가 같은 리스트에서 제거해 ConcurrentModificationException이 난다
        assertThatThrownBy(() -> service.deleteAllInterestsByProductColor(color))
                .isInstanceOf(ProductException.class)
                .hasCauseInstanceOf(ConcurrentModificationException.class)
                .satisfies(e -> assertThat(((ProductException) e).getErrorCode()).isEqualTo(ProductErrorCode.INTEREST_TOGGLE_FAILED));
        verify(interestRepository).delete(existing);
    }
}
