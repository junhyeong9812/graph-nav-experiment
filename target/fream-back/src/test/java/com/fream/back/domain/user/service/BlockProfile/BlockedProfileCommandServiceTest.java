package com.fream.back.domain.user.service.BlockProfile;

import com.fream.back.domain.user.entity.BlockedProfile;
import com.fream.back.domain.user.entity.Profile;
import com.fream.back.domain.user.exception.AlreadyBlockedException;
import com.fream.back.domain.user.exception.CannotBlockSelfException;
import com.fream.back.domain.user.exception.NotBlockedException;
import com.fream.back.domain.user.exception.ProfileNotFoundException;
import com.fream.back.domain.user.repository.BlockedProfileRepository;
import com.fream.back.domain.user.repository.ProfileRepository;
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
import static org.mockito.Mockito.*;

/**
 * BlockedProfileCommandService 의 차단·해제 분기(자기 자신·중복·미차단·프로필 없음)와 저장 값을 고정한다.
 */
@ExtendWith(MockitoExtension.class)
class BlockedProfileCommandServiceTest {

    @Mock private ProfileRepository profileRepository;
    @Mock private BlockedProfileRepository blockedProfileRepository;

    @InjectMocks
    private BlockedProfileCommandService service;

    private final Profile me = Profile.builder().id(1L).profileName("me").build();
    private final Profile other = Profile.builder().id(2L).profileName("other").build();

    @Test
    void blockProfile_savesBlockedProfile() {
        when(profileRepository.findByUserEmailWithFetchJoin("me@fream.com")).thenReturn(Optional.of(me));
        when(profileRepository.findById(2L)).thenReturn(Optional.of(other));
        when(blockedProfileRepository.findByProfileAndBlockedProfile(me, other)).thenReturn(Optional.empty());

        service.blockProfile("me@fream.com", 2L);

        ArgumentCaptor<BlockedProfile> captor = ArgumentCaptor.forClass(BlockedProfile.class);
        verify(blockedProfileRepository).save(captor.capture());
        assertThat(captor.getValue().getProfile()).isSameAs(me);
        assertThat(captor.getValue().getBlockedProfile()).isSameAs(other);
    }

    @Test
    void blockProfile_guards_selfAlreadyBlockedAndMissingOwnProfile() {
        when(profileRepository.findByUserEmailWithFetchJoin("me@fream.com")).thenReturn(Optional.of(me));
        when(profileRepository.findByUserEmailWithFetchJoin("ghost@fream.com")).thenReturn(Optional.empty());
        when(profileRepository.findById(1L)).thenReturn(Optional.of(me));
        when(profileRepository.findById(2L)).thenReturn(Optional.of(other));
        when(blockedProfileRepository.findByProfileAndBlockedProfile(me, other))
                .thenReturn(Optional.of(BlockedProfile.builder().profile(me).blockedProfile(other).build()));

        assertThatThrownBy(() -> service.blockProfile("me@fream.com", 1L))
                .isInstanceOf(CannotBlockSelfException.class);
        assertThatThrownBy(() -> service.blockProfile("me@fream.com", 2L))
                .isInstanceOf(AlreadyBlockedException.class);
        assertThatThrownBy(() -> service.blockProfile("ghost@fream.com", 2L))
                .isInstanceOf(ProfileNotFoundException.class)
                .hasMessage("내 프로필을 찾을 수 없습니다.");

        verify(blockedProfileRepository, never()).save(any());
    }

    @Test
    void unblockProfile_deletesExisting_orThrowsNotBlocked() {
        BlockedProfile entity = BlockedProfile.builder().profile(me).blockedProfile(other).build();
        when(profileRepository.findByUserEmailWithFetchJoin("me@fream.com")).thenReturn(Optional.of(me));
        when(profileRepository.findById(2L)).thenReturn(Optional.of(other));
        when(blockedProfileRepository.findByProfileAndBlockedProfile(me, other))
                .thenReturn(Optional.of(entity))
                .thenReturn(Optional.empty());

        service.unblockProfile("me@fream.com", 2L);
        verify(blockedProfileRepository).delete(entity);

        assertThatThrownBy(() -> service.unblockProfile("me@fream.com", 2L))
                .isInstanceOf(NotBlockedException.class);
    }
}
