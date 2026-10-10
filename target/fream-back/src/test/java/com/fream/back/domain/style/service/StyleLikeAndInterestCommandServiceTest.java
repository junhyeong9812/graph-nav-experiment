package com.fream.back.domain.style.service;

import com.fream.back.domain.style.entity.Style;
import com.fream.back.domain.style.entity.StyleInterest;
import com.fream.back.domain.style.entity.StyleLike;
import com.fream.back.domain.style.exception.StyleErrorCode;
import com.fream.back.domain.style.exception.StyleException;
import com.fream.back.domain.style.repository.StyleInterestRepository;
import com.fream.back.domain.style.repository.StyleLikeRepository;
import com.fream.back.domain.style.service.command.StyleInterestCommandService;
import com.fream.back.domain.style.service.command.StyleLikeCommandService;
import com.fream.back.domain.style.service.query.StyleQueryService;
import com.fream.back.domain.user.entity.Profile;
import com.fream.back.domain.user.service.profile.ProfileQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 스타일 좋아요·관심 토글(추가/취소)과 예외 변환 고정.
 */
@ExtendWith(MockitoExtension.class)
class StyleLikeAndInterestCommandServiceTest {

    @Mock private StyleLikeRepository styleLikeRepository;
    @Mock private StyleInterestRepository styleInterestRepository;
    @Mock private ProfileQueryService profileQueryService;
    @Mock private StyleQueryService styleQueryService;

    private StyleLikeCommandService likeService;
    private StyleInterestCommandService interestService;

    private final Profile profile = Profile.builder().id(1L).profileName("alice").build();
    private Style style;

    @BeforeEach
    void setUp() {
        likeService = new StyleLikeCommandService(styleLikeRepository, profileQueryService, styleQueryService);
        interestService = new StyleInterestCommandService(styleInterestRepository, profileQueryService, styleQueryService);
        style = Style.builder().id(3L).viewCount(0L).build();
    }

    private static StyleErrorCode code(Throwable e) {
        return (StyleErrorCode) ((StyleException) e).getErrorCode();
    }

    private void givenProfileAndStyle() {
        when(profileQueryService.getProfileByEmail("a@test.com")).thenReturn(profile);
        when(styleQueryService.findStyleById(3L)).thenReturn(style);
    }

    @Test
    void likeIsAddedWhenAbsent() {
        givenProfileAndStyle();
        when(styleLikeRepository.findByStyleAndProfile(style, profile)).thenReturn(Optional.empty());
        when(styleLikeRepository.save(any(StyleLike.class))).thenAnswer(inv -> inv.getArgument(0));

        likeService.toggleLike("a@test.com", 3L);

        ArgumentCaptor<StyleLike> captor = ArgumentCaptor.forClass(StyleLike.class);
        verify(styleLikeRepository).save(captor.capture());
        assertThat(captor.getValue().getProfile()).isSameAs(profile);
        assertThat(style.getLikes()).containsExactly(captor.getValue());
    }

    @Test
    void likeIsRemovedWhenPresent() {
        givenProfileAndStyle();
        StyleLike existing = StyleLike.builder().id(7L).profile(profile).build();
        style.addLike(existing);
        when(styleLikeRepository.findByStyleAndProfile(style, profile)).thenReturn(Optional.of(existing));

        likeService.toggleLike("a@test.com", 3L);

        verify(styleLikeRepository).delete(existing);
        verify(styleLikeRepository, never()).save(any());
        assertThat(style.getLikes()).isEmpty();
    }

    @Test
    void likeValidationAndUnexpectedFailureMapping() {
        assertThatThrownBy(() -> likeService.toggleLike("", 3L))
                .satisfies(e -> assertThat(code(e)).isEqualTo(StyleErrorCode.STYLE_INVALID_REQUEST));
        assertThatThrownBy(() -> likeService.toggleLike("a@test.com", null))
                .hasMessage("스타일 ID가 필요합니다.");

        when(profileQueryService.getProfileByEmail("a@test.com")).thenThrow(new IllegalStateException("x"));
        assertThatThrownBy(() -> likeService.toggleLike("a@test.com", 3L))
                .satisfies(e -> assertThat(code(e)).isEqualTo(StyleErrorCode.LIKE_OPERATION_FAILED));
    }

    @Test
    void interestIsAddedWhenAbsentAndRemovedWhenPresent() {
        givenProfileAndStyle();
        when(styleInterestRepository.findByStyleAndProfile(style, profile)).thenReturn(Optional.empty());
        when(styleInterestRepository.save(any(StyleInterest.class))).thenAnswer(inv -> inv.getArgument(0));

        interestService.toggleStyleInterest("a@test.com", 3L);

        assertThat(style.getInterests()).hasSize(1);
        StyleInterest added = style.getInterests().get(0);
        assertThat(added.getStyle()).isSameAs(style);

        when(styleInterestRepository.findByStyleAndProfile(style, profile)).thenReturn(Optional.of(added));
        interestService.toggleStyleInterest("a@test.com", 3L);

        verify(styleInterestRepository).delete(added);
        assertThat(style.getInterests()).isEmpty();
    }
}
