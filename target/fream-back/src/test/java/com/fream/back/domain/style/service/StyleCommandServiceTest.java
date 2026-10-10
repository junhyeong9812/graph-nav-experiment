package com.fream.back.domain.style.service;

import com.fream.back.domain.style.dto.HashtagCreateRequestDto;
import com.fream.back.domain.style.dto.HashtagResponseDto;
import com.fream.back.domain.style.entity.Hashtag;
import com.fream.back.domain.style.entity.MediaUrl;
import com.fream.back.domain.style.entity.Style;
import com.fream.back.domain.style.entity.StyleOrderItem;
import com.fream.back.domain.style.exception.StyleErrorCode;
import com.fream.back.domain.style.exception.StyleException;
import com.fream.back.domain.style.repository.HashtagRepository;
import com.fream.back.domain.style.repository.StyleRepository;
import com.fream.back.domain.style.service.command.*;
import com.fream.back.domain.user.entity.Profile;
import com.fream.back.domain.user.service.profile.ProfileQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * StyleCommandService 생성(주문아이템·미디어·해시태그 부분 실패 허용)·수정·삭제·조회수 증가 고정.
 */
@ExtendWith(MockitoExtension.class)
class StyleCommandServiceTest {

    @Mock private StyleRepository styleRepository;
    @Mock private ProfileQueryService profileQueryService;
    @Mock private StyleOrderItemCommandService styleOrderItemCommandService;
    @Mock private MediaUrlCommandService mediaUrlCommandService;
    @Mock private HashtagRepository hashtagRepository;
    @Mock private HashtagCommandService hashtagCommandService;
    @Mock private StyleHashtagCommandService styleHashtagCommandService;

    @InjectMocks
    private StyleCommandService service;

    private final Profile profile = Profile.builder().id(1L).profileName("alice").build();

    private static StyleErrorCode code(Throwable e) {
        return (StyleErrorCode) ((StyleException) e).getErrorCode();
    }

    @Test
    void createRejectsMissingEmailOrBlankContentBeforeLookup() {
        assertThatThrownBy(() -> service.createStyle(null, null, "c", null, null))
                .hasMessage("사용자 이메일이 필요합니다.")
                .satisfies(e -> assertThat(code(e)).isEqualTo(StyleErrorCode.STYLE_INVALID_REQUEST));
        assertThatThrownBy(() -> service.createStyle("a@test.com", null, "   ", null, null))
                .hasMessage("스타일 내용이 필요합니다.");
        verifyNoInteractions(profileQueryService, styleRepository);
    }

    @Test
    void createSavesStyleWithZeroViewsAndToleratesPartialOrderItemAndMediaFailures() {
        when(profileQueryService.getProfileByEmail("a@test.com")).thenReturn(profile);
        when(styleRepository.save(any(Style.class))).thenAnswer(inv -> inv.getArgument(0));
        when(styleOrderItemCommandService.createStyleOrderItem(eq(10L), any(Style.class)))
                .thenReturn(StyleOrderItem.builder().id(100L).build());
        when(styleOrderItemCommandService.createStyleOrderItem(eq(11L), any(Style.class)))
                .thenThrow(new IllegalStateException("not mine"));
        MultipartFile empty = new MockMultipartFile("f", "empty.jpg", "image/jpeg", new byte[0]);
        MultipartFile ok = new MockMultipartFile("f", "ok.jpg", "image/jpeg", new byte[]{1});
        MultipartFile broken = new MockMultipartFile("f", "broken.jpg", "image/jpeg", new byte[]{2});
        when(mediaUrlCommandService.saveMediaFile(any(Style.class), eq(ok))).thenReturn(MediaUrl.builder().id(5L).url("u").build());
        when(mediaUrlCommandService.saveMediaFile(any(Style.class), eq(broken))).thenThrow(new RuntimeException("io"));

        Style style = service.createStyle("a@test.com", List.of(10L, 11L), "look", List.of(empty, ok, broken), null);

        assertThat(style.getContent()).isEqualTo("look");
        assertThat(style.getViewCount()).isZero();
        assertThat(style.getProfile()).isSameAs(profile);
        assertThat(profile.getStyles()).containsExactly(style);
        assertThat(style.getStyleOrderItems()).extracting(StyleOrderItem::getId).containsExactly(100L);
        assertThat(style.getStyleOrderItems().get(0).getStyle()).isSameAs(style);
        verify(mediaUrlCommandService, never()).saveMediaFile(any(), eq(empty));
        verify(mediaUrlCommandService, times(2)).saveMediaFile(any(Style.class), any());
        verifyNoInteractions(styleHashtagCommandService);
    }

    @Test
    void createStripsHashPrefixReusesExistingTagsCreatesMissingAndSkipsEmpty() {
        when(profileQueryService.getProfileByEmail("a@test.com")).thenReturn(profile);
        when(styleRepository.save(any(Style.class))).thenAnswer(inv -> inv.getArgument(0));
        Hashtag ootd = Hashtag.builder().id(1L).name("ootd").count(3L).build();
        Hashtag street = Hashtag.builder().id(9L).name("street").count(0L).build();
        when(hashtagRepository.findByName("ootd")).thenReturn(Optional.of(ootd));
        when(hashtagRepository.findByName("street")).thenReturn(Optional.empty());
        when(hashtagCommandService.create(any(HashtagCreateRequestDto.class)))
                .thenReturn(HashtagResponseDto.builder().id(9L).name("street").count(0L).build());
        when(hashtagRepository.findById(9L)).thenReturn(Optional.of(street));

        Style style = service.createStyle("a@test.com", null, "look", null, List.of("#ootd", "street", "#"));

        ArgumentCaptor<HashtagCreateRequestDto> createCaptor = ArgumentCaptor.forClass(HashtagCreateRequestDto.class);
        verify(hashtagCommandService).create(createCaptor.capture());
        assertThat(createCaptor.getValue().getName()).isEqualTo("street");
        InOrder inOrder = inOrder(styleHashtagCommandService);
        inOrder.verify(styleHashtagCommandService).addHashtagToStyle(style, ootd);
        inOrder.verify(styleHashtagCommandService).addHashtagToStyle(style, street);
        verifyNoMoreInteractions(styleHashtagCommandService);
    }

    @Test
    void updateDeletesMediaNotInKeepListAndReplacesHashtags() {
        Style style = Style.builder().id(3L).profile(profile).content("old").viewCount(0L).build();
        MediaUrl keep = MediaUrl.builder().id(1L).url("keep.jpg").build();
        MediaUrl drop = MediaUrl.builder().id(2L).url("drop.jpg").build();
        style.addMediaUrl(keep);
        style.addMediaUrl(drop);
        when(styleRepository.findById(3L)).thenReturn(Optional.of(style));
        Hashtag ootd = Hashtag.builder().id(1L).name("ootd").build();
        when(hashtagRepository.findByName("ootd")).thenReturn(Optional.of(ootd));

        service.updateStyle(3L, "new", null, List.of("keep.jpg"), List.of("ootd"));

        assertThat(style.getContent()).isEqualTo("new");
        verify(mediaUrlCommandService).deleteMediaUrl(drop);
        verify(mediaUrlCommandService, never()).deleteMediaUrl(keep);
        InOrder inOrder = inOrder(styleHashtagCommandService, styleRepository);
        inOrder.verify(styleHashtagCommandService).removeAllHashtagsFromStyle(3L);
        inOrder.verify(styleHashtagCommandService).addHashtagToStyle(style, ootd);
        inOrder.verify(styleRepository).save(style);
    }

    @Test
    void updateWithNullListsLeavesMediaAndHashtagsUntouched() {
        Style style = Style.builder().id(3L).profile(profile).content("old").viewCount(0L).build();
        when(styleRepository.findById(3L)).thenReturn(Optional.of(style));

        service.updateStyle(3L, null, null, null, null);

        assertThat(style.getContent()).isEqualTo("old");
        verifyNoInteractions(mediaUrlCommandService, styleHashtagCommandService);
        verify(styleRepository).save(style);
    }

    @Test
    void deleteContinuesWhenHashtagRemovalFailsAndDeletesEveryMedia() {
        Style style = Style.builder().id(3L).profile(profile).content("c").viewCount(0L).build();
        MediaUrl m1 = MediaUrl.builder().id(1L).url("a").build();
        MediaUrl m2 = MediaUrl.builder().id(2L).url("b").build();
        style.addMediaUrl(m1);
        style.addMediaUrl(m2);
        when(styleRepository.findById(3L)).thenReturn(Optional.of(style));
        doThrow(new RuntimeException("tag")).when(styleHashtagCommandService).removeAllHashtagsFromStyle(3L);
        doThrow(new RuntimeException("file")).when(mediaUrlCommandService).deleteMediaUrl(m1);

        service.deleteStyle(3L);

        verify(mediaUrlCommandService).deleteMediaUrl(m1);
        verify(mediaUrlCommandService).deleteMediaUrl(m2);
        verify(styleRepository).delete(style);
    }

    @Test
    void incrementViewCountAddsOneAndSaves() {
        Style style = Style.builder().id(3L).viewCount(41L).build();
        when(styleRepository.findById(3L)).thenReturn(Optional.of(style));

        service.incrementViewCount(3L);

        assertThat(style.getViewCount()).isEqualTo(42L);
        verify(styleRepository).save(style);
    }

    @Test
    void incrementViewCountOnNullCountIsWrappedAsInvalidRequest() {
        Style style = Style.builder().id(3L).build();
        when(styleRepository.findById(3L)).thenReturn(Optional.of(style));

        assertThatThrownBy(() -> service.incrementViewCount(3L))
                .satisfies(e -> assertThat(code(e)).isEqualTo(StyleErrorCode.STYLE_INVALID_REQUEST))
                .hasCauseInstanceOf(NullPointerException.class);
        verify(styleRepository, never()).save(any());
    }
}
