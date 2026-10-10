package com.fream.back.domain.user.service.admin;

import com.fream.back.domain.user.dto.SanctionDto.*;
import com.fream.back.domain.user.entity.*;
import com.fream.back.domain.user.repository.UserRepository;
import com.fream.back.domain.user.repository.UserSanctionRepository;
import com.fream.back.global.exception.EntityNotFoundException;
import com.fream.back.global.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AdminSanctionService 의 제재 생성·승인/거부·취소·만료 처리에 따른 상태 전이와 사용자 활성 상태 변경을 고정한다.
 */
@ExtendWith(MockitoExtension.class)
class AdminSanctionServiceTest {

    @Mock private UserSanctionRepository userSanctionRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private AdminSanctionService service;

    private final LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);

    private User user(long id) {
        return User.builder().id(id).email("u" + id + "@fream.com").build();
    }

    private UserSanction sanction(long id, User user, SanctionType type, SanctionStatus status, LocalDateTime end) {
        return UserSanction.builder().id(id).user(user).reason("r").type(type).status(status)
                .startDate(start).endDate(end).createdBy("admin@fream.com").build();
    }

    @Test
    void createSanction_unknownUserAndMissingEndDateRejected_otherwiseSavedAsPending() {
        User user = user(5L);
        when(userRepository.findById(9L)).thenReturn(Optional.empty());
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.createSanction(
                SanctionCreateRequestDto.builder().userId(9L).type(SanctionType.WARNING).build(), "a"))
                .isInstanceOf(EntityNotFoundException.class);
        assertThatThrownBy(() -> service.createSanction(
                SanctionCreateRequestDto.builder().userId(5L).reason("spam").type(SanctionType.TEMPORARY_BAN).build(),
                "admin@fream.com"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("End date is required for non-permanent sanctions");
        verify(userSanctionRepository, never()).save(any());

        SanctionResponseDto response = service.createSanction(SanctionCreateRequestDto.builder()
                .userId(5L).reason("spam").type(SanctionType.PERMANENT_BAN).startDate(start).build(), "admin@fream.com");

        ArgumentCaptor<UserSanction> captor = ArgumentCaptor.forClass(UserSanction.class);
        verify(userSanctionRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(SanctionStatus.PENDING);
        assertThat(captor.getValue().getEndDate()).isNull();
        assertThat(captor.getValue().getCreatedBy()).isEqualTo("admin@fream.com");
        assertThat(response.getUserEmail()).isEqualTo("u5@fream.com");
        assertThat(user.isActive()).isTrue();
    }

    @Test
    void reviewSanction_approve_deactivatesUserOnlyForBans() {
        User banned = user(5L);
        User warned = user(6L);
        UserSanction ban = sanction(1L, banned, SanctionType.TEMPORARY_BAN, SanctionStatus.PENDING, start.plusDays(7));
        UserSanction warning = sanction(2L, warned, SanctionType.WARNING, SanctionStatus.PENDING, start.plusDays(7));
        when(userSanctionRepository.findById(1L)).thenReturn(Optional.of(ban));
        when(userSanctionRepository.findById(2L)).thenReturn(Optional.of(warning));

        service.reviewSanction(1L, SanctionReviewRequestDto.builder().approved(true).build(), "boss@fream.com");
        service.reviewSanction(2L, SanctionReviewRequestDto.builder().approved(true).build(), "boss@fream.com");

        assertThat(ban.getStatus()).isEqualTo(SanctionStatus.ACTIVE);
        assertThat(ban.getApprovedBy()).isEqualTo("boss@fream.com");
        assertThat(banned.isActive()).isFalse();
        assertThat(warning.getStatus()).isEqualTo(SanctionStatus.ACTIVE);
        assertThat(warned.isActive()).isTrue();
    }

    @Test
    void reviewSanction_rejectRequiresReason_andOnlyFromPending() {
        UserSanction s = sanction(1L, user(5L), SanctionType.WARNING, SanctionStatus.PENDING, start.plusDays(7));
        when(userSanctionRepository.findById(1L)).thenReturn(Optional.of(s));

        assertThatThrownBy(() -> service.reviewSanction(1L,
                SanctionReviewRequestDto.builder().approved(false).rejectionReason("").build(), "boss"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Rejection reason is required");

        service.reviewSanction(1L,
                SanctionReviewRequestDto.builder().approved(false).rejectionReason("오탐").build(), "boss");
        assertThat(s.getStatus()).isEqualTo(SanctionStatus.REJECTED);
        assertThat(s.getRejectedBy()).isEqualTo("boss");
        assertThat(s.getRejectionReason()).isEqualTo("오탐");

        assertThatThrownBy(() -> service.reviewSanction(1L,
                SanctionReviewRequestDto.builder().approved(true).build(), "boss"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Only pending sanctions can be reviewed");
    }

    @Test
    void cancelSanction_reactivatesUserOnlyWithoutOtherActiveBan_andOnlyFromActive() {
        User stillBanned = user(5L);
        stillBanned.addActive(false);
        UserSanction t1 = sanction(1L, stillBanned, SanctionType.TEMPORARY_BAN, SanctionStatus.ACTIVE, start.plusDays(7));
        UserSanction otherBan = sanction(2L, stillBanned, SanctionType.PERMANENT_BAN, SanctionStatus.ACTIVE, null);

        User released = user(6L);
        released.addActive(false);
        UserSanction t3 = sanction(3L, released, SanctionType.TEMPORARY_BAN, SanctionStatus.ACTIVE, start.plusDays(7));
        UserSanction warning = sanction(4L, released, SanctionType.WARNING, SanctionStatus.ACTIVE, start.plusDays(7));

        when(userSanctionRepository.findById(1L)).thenReturn(Optional.of(t1));
        when(userSanctionRepository.findById(3L)).thenReturn(Optional.of(t3));
        when(userSanctionRepository.findByUserAndStatusOrderByStartDateDesc(stillBanned, SanctionStatus.ACTIVE))
                .thenReturn(List.of(t1, otherBan));
        when(userSanctionRepository.findByUserAndStatusOrderByStartDateDesc(released, SanctionStatus.ACTIVE))
                .thenReturn(List.of(t3, warning));

        service.cancelSanction(1L);
        service.cancelSanction(3L);

        assertThat(t1.getStatus()).isEqualTo(SanctionStatus.CANCELED);
        assertThat(stillBanned.isActive()).isFalse();
        assertThat(t3.getStatus()).isEqualTo(SanctionStatus.CANCELED);
        assertThat(released.isActive()).isTrue();

        assertThatThrownBy(() -> service.cancelSanction(1L))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Only active sanctions can be canceled");
    }

    @Test
    void processExpiredSanctions_expiresAll_reactivatesOnlyForTemporaryBan() {
        User banned = user(5L);
        banned.addActive(false);
        User restricted = user(6L);
        restricted.addActive(false);
        UserSanction tempBan = sanction(1L, banned, SanctionType.TEMPORARY_BAN, SanctionStatus.ACTIVE,
                LocalDateTime.now().minusDays(1));
        UserSanction featureRestriction = sanction(2L, restricted, SanctionType.FEATURE_RESTRICTION,
                SanctionStatus.ACTIVE, LocalDateTime.now().minusDays(1));
        when(userSanctionRepository.findExpirableSanctions(any(LocalDateTime.class)))
                .thenReturn(List.of(tempBan, featureRestriction));
        when(userSanctionRepository.findByUserAndStatusOrderByStartDateDesc(banned, SanctionStatus.ACTIVE))
                .thenReturn(List.of());

        int processed = service.processExpiredSanctions();

        assertThat(processed).isEqualTo(2);
        assertThat(tempBan.getStatus()).isEqualTo(SanctionStatus.EXPIRED);
        assertThat(featureRestriction.getStatus()).isEqualTo(SanctionStatus.EXPIRED);
        assertThat(banned.isActive()).isTrue();
        assertThat(restricted.isActive()).isFalse();
    }
}
