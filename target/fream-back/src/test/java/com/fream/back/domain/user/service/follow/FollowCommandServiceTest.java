package com.fream.back.domain.user.service.follow;

import com.fream.back.domain.notification.entity.NotificationCategory;
import com.fream.back.domain.notification.entity.NotificationType;
import com.fream.back.domain.notification.service.command.NotificationCommandService;
import com.fream.back.domain.user.entity.Follow;
import com.fream.back.domain.user.entity.Profile;
import com.fream.back.domain.user.entity.User;
import com.fream.back.domain.user.exception.AlreadyFollowingException;
import com.fream.back.domain.user.exception.CannotFollowSelfException;
import com.fream.back.domain.user.exception.NotFollowingException;
import com.fream.back.domain.user.exception.ProfileNotFoundException;
import com.fream.back.domain.user.repository.FollowRepository;
import com.fream.back.domain.user.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * FollowCommandService 의 팔로우 생성·삭제 분기와 알림 호출(실패 허용)을 고정한다.
 */
@ExtendWith(MockitoExtension.class)
class FollowCommandServiceTest {

    @Mock private FollowRepository followRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private NotificationCommandService notificationCommandService;

    @InjectMocks
    private FollowCommandService followCommandService;

    private Profile me;
    private Profile target;

    @BeforeEach
    void setUp() {
        me = Profile.builder().id(10L).profileName("me").Name("나").build();
        User targetUser = User.builder().id(200L).email("t@fream.com").build();
        target = Profile.builder().id(20L).profileName("target").user(targetUser).build();
    }

    @Test
    void createFollow_savesFollow_andNotifiesTargetUser() {
        when(profileRepository.findByUserEmailWithFetchJoin("me@fream.com")).thenReturn(Optional.of(me));
        when(profileRepository.findById(20L)).thenReturn(Optional.of(target));
        when(followRepository.findByFollowerAndFollowing(me, target)).thenReturn(Optional.empty());

        followCommandService.createFollow("me@fream.com", 20L);

        ArgumentCaptor<Follow> captor = ArgumentCaptor.forClass(Follow.class);
        verify(followRepository).save(captor.capture());
        assertThat(captor.getValue().getFollower()).isSameAs(me);
        assertThat(captor.getValue().getFollowing()).isSameAs(target);
        verify(notificationCommandService).createNotification(200L, NotificationCategory.STYLE,
                NotificationType.FOLLOW, "나님이 회원님을 팔로우 했습니다.");
    }

    @Test
    void createFollow_notificationFailureTolerated_butSaveFailureWrappedAsAlreadyFollowing() {
        when(profileRepository.findByUserEmailWithFetchJoin("me@fream.com")).thenReturn(Optional.of(me));
        when(profileRepository.findById(20L)).thenReturn(Optional.of(target));
        when(followRepository.findByFollowerAndFollowing(me, target)).thenReturn(Optional.empty());
        when(notificationCommandService.createNotification(anyLong(), any(), any(), any()))
                .thenThrow(new RuntimeException("redis"));
        when(followRepository.save(any(Follow.class)))
                .thenAnswer(inv -> inv.getArgument(0))
                .thenThrow(new RuntimeException("db"));

        followCommandService.createFollow("me@fream.com", 20L);

        assertThatThrownBy(() -> followCommandService.createFollow("me@fream.com", 20L))
                .isInstanceOf(AlreadyFollowingException.class)
                .hasMessage("팔로우 처리 중 오류가 발생했습니다.");
        verify(followRepository, times(2)).save(any(Follow.class));
    }

    @Test
    void createFollow_guards_selfAlreadyFollowingAndMissingTarget() {
        when(profileRepository.findByUserEmailWithFetchJoin("me@fream.com")).thenReturn(Optional.of(me));
        when(profileRepository.findById(10L)).thenReturn(Optional.of(me));
        when(profileRepository.findById(20L)).thenReturn(Optional.of(target));
        when(profileRepository.findById(99L)).thenReturn(Optional.empty());
        when(followRepository.findByFollowerAndFollowing(me, target))
                .thenReturn(Optional.of(Follow.builder().follower(me).following(target).build()));

        assertThatThrownBy(() -> followCommandService.createFollow("me@fream.com", 10L))
                .isInstanceOf(CannotFollowSelfException.class);
        assertThatThrownBy(() -> followCommandService.createFollow("me@fream.com", 20L))
                .isInstanceOf(AlreadyFollowingException.class)
                .hasMessageContaining("20");
        assertThatThrownBy(() -> followCommandService.createFollow("me@fream.com", 99L))
                .isInstanceOf(ProfileNotFoundException.class)
                .hasMessage("ID가 99인 프로필을 찾을 수 없습니다.");

        verify(followRepository, never()).save(any());
        verifyNoInteractions(notificationCommandService);
    }

    @Test
    void deleteFollow_deletesExisting_orThrowsNotFollowing() {
        Follow follow = Follow.builder().follower(me).following(target).build();
        when(profileRepository.findByUserEmailWithFetchJoin("me@fream.com")).thenReturn(Optional.of(me));
        when(profileRepository.findById(20L)).thenReturn(Optional.of(target));
        when(followRepository.findByFollowerAndFollowing(me, target))
                .thenReturn(Optional.of(follow))
                .thenReturn(Optional.empty());

        followCommandService.deleteFollow("me@fream.com", 20L);
        verify(followRepository).delete(follow);

        assertThatThrownBy(() -> followCommandService.deleteFollow("me@fream.com", 20L))
                .isInstanceOf(NotFollowingException.class);
    }
}
