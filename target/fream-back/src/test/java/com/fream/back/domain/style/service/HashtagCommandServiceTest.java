package com.fream.back.domain.style.service;

import com.fream.back.domain.style.dto.HashtagCreateRequestDto;
import com.fream.back.domain.style.dto.HashtagResponseDto;
import com.fream.back.domain.style.dto.HashtagUpdateRequestDto;
import com.fream.back.domain.style.entity.Hashtag;
import com.fream.back.domain.style.entity.Style;
import com.fream.back.domain.style.entity.StyleHashtag;
import com.fream.back.domain.style.exception.StyleErrorCode;
import com.fream.back.domain.style.exception.StyleException;
import com.fream.back.domain.style.repository.HashtagRepository;
import com.fream.back.domain.style.repository.StyleHashtagRepository;
import com.fream.back.domain.style.service.command.HashtagCommandService;
import com.fream.back.domain.style.service.command.StyleHashtagCommandService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * HashtagCommandService(생성·수정·삭제)와 StyleHashtagCommandService(연결·사용 횟수 증감) 고정.
 */
@ExtendWith(MockitoExtension.class)
class HashtagCommandServiceTest {

    @Mock private HashtagRepository hashtagRepository;
    @Mock private StyleHashtagRepository styleHashtagRepository;

    private HashtagCommandService hashtagService;
    private StyleHashtagCommandService styleHashtagService;

    @BeforeEach
    void setUp() {
        hashtagService = new HashtagCommandService(hashtagRepository, styleHashtagRepository);
        styleHashtagService = new StyleHashtagCommandService(styleHashtagRepository, hashtagRepository);
    }

    private static StyleErrorCode code(Throwable e) {
        return (StyleErrorCode) ((StyleException) e).getErrorCode();
    }

    @Test
    void createTrimsNameKeepsCaseAndStartsCountAtZero() {
        when(hashtagRepository.existsByName("OOTD")).thenReturn(false);
        when(hashtagRepository.save(any(Hashtag.class))).thenAnswer(inv -> inv.getArgument(0));

        HashtagResponseDto dto = hashtagService.create(new HashtagCreateRequestDto("  OOTD "));

        assertThat(dto.getName()).isEqualTo("OOTD");
        assertThat(dto.getCount()).isZero();
    }

    @Test
    void createRejectsBlankWithAlreadyExistsCodeAndDuplicates() {
        assertThatThrownBy(() -> hashtagService.create(new HashtagCreateRequestDto(" ")))
                .hasMessage("해시태그 이름이 필요합니다.")
                .satisfies(e -> assertThat(code(e)).isEqualTo(StyleErrorCode.HASHTAG_ALREADY_EXISTS));

        when(hashtagRepository.existsByName("ootd")).thenReturn(true);
        assertThatThrownBy(() -> hashtagService.create(new HashtagCreateRequestDto("ootd")))
                .hasMessage("이미 존재하는 해시태그입니다: ootd");
        verify(hashtagRepository, never()).save(any());
    }

    @Test
    void updateSavesNewInstanceWithSameIdAndCount() {
        when(hashtagRepository.findById(1L)).thenReturn(Optional.of(Hashtag.builder().id(1L).name("old").count(7L).build()));
        when(hashtagRepository.existsByName("new")).thenReturn(false);
        when(hashtagRepository.save(any(Hashtag.class))).thenAnswer(inv -> inv.getArgument(0));

        HashtagResponseDto dto = hashtagService.update(1L, new HashtagUpdateRequestDto("new"));

        ArgumentCaptor<Hashtag> captor = ArgumentCaptor.forClass(Hashtag.class);
        verify(hashtagRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(1L);
        assertThat(captor.getValue().getCount()).isEqualTo(7L);
        assertThat(dto.getName()).isEqualTo("new");
    }

    @Test
    void deleteInUseIsRejectedWithConnectionCount() {
        when(hashtagRepository.findById(1L)).thenReturn(Optional.of(Hashtag.builder().id(1L).name("ootd").build()));
        when(styleHashtagRepository.countByHashtagId(1L)).thenReturn(3L);

        assertThatThrownBy(() -> hashtagService.delete(1L))
                .hasMessage("이 해시태그는 3개의 스타일에서 사용 중이므로 삭제할 수 없습니다.")
                .satisfies(e -> assertThat(code(e)).isEqualTo(StyleErrorCode.HASHTAG_IN_USE));
        verify(hashtagRepository, never()).delete(any());
    }

    @Test
    void addHashtagToStyleReturnsExistingLinkWithoutIncrementing() {
        Style style = Style.builder().id(3L).build();
        Hashtag hashtag = Hashtag.builder().id(1L).name("ootd").count(4L).build();
        StyleHashtag link = StyleHashtag.builder().id(50L).style(style).hashtag(hashtag).build();
        when(styleHashtagRepository.findByStyleIdAndHashtagId(3L, 1L)).thenReturn(Optional.of(link));

        assertThat(styleHashtagService.addHashtagToStyle(style, hashtag)).isSameAs(link);
        assertThat(hashtag.getCount()).isEqualTo(4L);
        verify(hashtagRepository, never()).save(any());
    }

    @Test
    void addHashtagToStyleIncrementsCountAndLinksStyle() {
        Style style = Style.builder().id(3L).build();
        Hashtag hashtag = Hashtag.builder().id(1L).name("ootd").count(4L).build();
        when(styleHashtagRepository.findByStyleIdAndHashtagId(3L, 1L)).thenReturn(Optional.empty());
        when(styleHashtagRepository.save(any(StyleHashtag.class))).thenAnswer(inv -> inv.getArgument(0));

        StyleHashtag saved = styleHashtagService.addHashtagToStyle(style, hashtag);

        assertThat(hashtag.getCount()).isEqualTo(5L);
        verify(hashtagRepository).save(hashtag);
        assertThat(saved.getStyle()).isSameAs(style);
        assertThat(saved.getHashtag()).isSameAs(hashtag);
        assertThat(style.getHashtags()).containsExactly(saved);
    }

    @Test
    void removeAllDecrementsCountsWithoutGoingBelowZeroThenDeletesLinks() {
        Hashtag used = Hashtag.builder().id(1L).name("a").count(2L).build();
        Hashtag zero = Hashtag.builder().id(2L).name("b").count(0L).build();
        when(styleHashtagRepository.findByStyleId(3L)).thenReturn(List.of(
                StyleHashtag.builder().hashtag(used).build(),
                StyleHashtag.builder().hashtag(zero).build()));

        styleHashtagService.removeAllHashtagsFromStyle(3L);

        assertThat(used.getCount()).isEqualTo(1L);
        assertThat(zero.getCount()).isZero();
        InOrder inOrder = inOrder(hashtagRepository, styleHashtagRepository);
        inOrder.verify(hashtagRepository).save(used);
        inOrder.verify(hashtagRepository).save(zero);
        inOrder.verify(styleHashtagRepository).deleteByStyleId(3L);
    }
}
