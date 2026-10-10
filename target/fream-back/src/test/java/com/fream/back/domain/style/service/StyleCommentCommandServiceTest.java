package com.fream.back.domain.style.service;

import com.fream.back.domain.style.entity.Style;
import com.fream.back.domain.style.entity.StyleComment;
import com.fream.back.domain.style.exception.StyleErrorCode;
import com.fream.back.domain.style.exception.StyleException;
import com.fream.back.domain.style.repository.StyleCommentRepository;
import com.fream.back.domain.style.service.command.StyleCommentCommandService;
import com.fream.back.domain.style.service.query.StyleQueryService;
import com.fream.back.domain.user.entity.Profile;
import com.fream.back.domain.user.service.profile.ProfileQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * StyleCommentCommandService 댓글·대댓글 생성 규칙(2단계 제한·같은 스타일)과 수정·삭제 검증 고정.
 */
@ExtendWith(MockitoExtension.class)
class StyleCommentCommandServiceTest {

    @Mock private StyleCommentRepository styleCommentRepository;
    @Mock private ProfileQueryService profileQueryService;
    @Mock private StyleQueryService styleQueryService;

    @InjectMocks
    private StyleCommentCommandService service;

    private final Profile profile = Profile.builder().id(1L).profileName("alice").build();
    private final Style style = Style.builder().id(3L).viewCount(0L).build();

    private static StyleErrorCode code(Throwable e) {
        return (StyleErrorCode) ((StyleException) e).getErrorCode();
    }

    private void givenProfileAndStyle() {
        when(profileQueryService.getProfileByEmail("a@test.com")).thenReturn(profile);
        when(styleQueryService.findStyleById(3L)).thenReturn(style);
    }

    @Test
    void addTopLevelCommentLinksStyleAndSaves() {
        givenProfileAndStyle();
        when(styleCommentRepository.save(any(StyleComment.class))).thenAnswer(inv -> inv.getArgument(0));

        StyleComment comment = service.addComment("a@test.com", 3L, "nice", null);

        assertThat(comment.getContent()).isEqualTo("nice");
        assertThat(comment.getProfile()).isSameAs(profile);
        assertThat(comment.getParentComment()).isNull();
        assertThat(style.getComments()).containsExactly(comment);
    }

    @Test
    void addReplyLinksParentAndChild() {
        givenProfileAndStyle();
        StyleComment parent = StyleComment.builder().id(20L).style(style).content("p").build();
        when(styleCommentRepository.findById(20L)).thenReturn(Optional.of(parent));
        when(styleCommentRepository.save(any(StyleComment.class))).thenAnswer(inv -> inv.getArgument(0));

        StyleComment reply = service.addComment("a@test.com", 3L, "reply", 20L);

        assertThat(reply.getParentComment()).isSameAs(parent);
        assertThat(parent.getChildComments()).containsExactly(reply);
    }

    @Test
    void replyToReplyIsRejected() {
        givenProfileAndStyle();
        StyleComment root = StyleComment.builder().id(19L).style(style).build();
        StyleComment reply = StyleComment.builder().id(20L).style(style).parentComment(root).build();
        when(styleCommentRepository.findById(20L)).thenReturn(Optional.of(reply));

        assertThatThrownBy(() -> service.addComment("a@test.com", 3L, "x", 20L))
                .hasMessage("대댓글에는 댓글을 달 수 없습니다.")
                .satisfies(e -> assertThat(code(e)).isEqualTo(StyleErrorCode.COMMENT_CONTENT_INVALID));
        verify(styleCommentRepository, never()).save(any());
    }

    @Test
    void replyToCommentOfAnotherStyleIsRejected() {
        givenProfileAndStyle();
        Style other = Style.builder().id(4L).build();
        when(styleCommentRepository.findById(20L)).thenReturn(Optional.of(StyleComment.builder().id(20L).style(other).build()));

        assertThatThrownBy(() -> service.addComment("a@test.com", 3L, "x", 20L))
                .hasMessage("다른 스타일의 댓글에 대댓글을 달 수 없습니다.");
    }

    @Test
    void inputValidationHappensBeforeAnyLookup() {
        assertThatThrownBy(() -> service.addComment("a@test.com", null, "x", null))
                .satisfies(e -> assertThat(code(e)).isEqualTo(StyleErrorCode.STYLE_INVALID_REQUEST));
        assertThatThrownBy(() -> service.addComment("a@test.com", 3L, " ", null))
                .satisfies(e -> assertThat(code(e)).isEqualTo(StyleErrorCode.COMMENT_CONTENT_INVALID));
        assertThatThrownBy(() -> service.updateComment(null, "x"))
                .satisfies(e -> assertThat(code(e)).isEqualTo(StyleErrorCode.COMMENT_NOT_FOUND));
        assertThatThrownBy(() -> service.deleteComment(null))
                .satisfies(e -> assertThat(code(e)).isEqualTo(StyleErrorCode.COMMENT_NOT_FOUND));
        verifyNoInteractions(profileQueryService, styleQueryService, styleCommentRepository);
    }

    @Test
    void updateChangesContentAndSaves() {
        StyleComment comment = StyleComment.builder().id(20L).content("old").build();
        when(styleCommentRepository.findById(20L)).thenReturn(Optional.of(comment));

        service.updateComment(20L, "new");

        assertThat(comment.getContent()).isEqualTo("new");
        verify(styleCommentRepository).save(comment);
    }

    @Test
    void deleteRemovesCommentEvenWithReplies() {
        StyleComment comment = StyleComment.builder().id(20L).build();
        comment.addChildComment(StyleComment.builder().id(21L).build());
        when(styleCommentRepository.findById(20L)).thenReturn(Optional.of(comment));

        service.deleteComment(20L);

        verify(styleCommentRepository).delete(comment);
    }
}
