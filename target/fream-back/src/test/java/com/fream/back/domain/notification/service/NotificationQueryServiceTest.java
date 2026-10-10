package com.fream.back.domain.notification.service;

import com.fream.back.domain.notification.dto.NotificationDTO;
import com.fream.back.domain.notification.entity.Notification;
import com.fream.back.domain.notification.entity.NotificationCategory;
import com.fream.back.domain.notification.entity.NotificationType;
import com.fream.back.domain.notification.exception.NotificationErrorCode;
import com.fream.back.domain.notification.exception.NotificationException;
import com.fream.back.domain.notification.repository.NotificationRepository;
import com.fream.back.domain.notification.service.query.NotificationQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * NotificationQueryService의 입력 검증·DTO 변환·예외 래핑을 고정한다.
 */
@ExtendWith(MockitoExtension.class)
class NotificationQueryServiceTest {

    @Mock NotificationRepository notificationRepository;
    @InjectMocks NotificationQueryService service;

    private static Notification notification(long id, boolean read) {
        Notification n = Notification.builder().id(id).category(NotificationCategory.STYLE)
                .type(NotificationType.COMMENT).message("msg" + id).isRead(read).build();
        ReflectionTestUtils.setField(n, "createdDate", LocalDateTime.of(2025, 5, 5, 5, 5));
        return n;
    }

    @Test
    void filterByCategoryNullIsInvalidCategory() {
        Throwable t = catchThrowable(() -> service.filterByCategory("a@test.com", null));

        assertThat(t).isInstanceOf(NotificationException.class);
        assertThat(((NotificationException) t).getErrorCode()).isEqualTo(NotificationErrorCode.INVALID_NOTIFICATION_CATEGORY);
        verifyNoInteractions(notificationRepository);
    }

    @Test
    void filterByTypeNullIsInvalidType() {
        Throwable t = catchThrowable(() -> service.filterByType("a@test.com", null));

        assertThat(((NotificationException) t).getErrorCode()).isEqualTo(NotificationErrorCode.INVALID_NOTIFICATION_TYPE);
        verifyNoInteractions(notificationRepository);
    }

    @Test
    void filterByCategoryMapsToDtoInRepositoryOrder() {
        when(notificationRepository.findByUserEmailAndCategory("a@test.com", NotificationCategory.STYLE))
                .thenReturn(List.of(notification(2, true), notification(1, false)));

        List<NotificationDTO> result = service.filterByCategory("a@test.com", NotificationCategory.STYLE);

        assertThat(result).extracting(NotificationDTO::getId).containsExactly(2L, 1L);
        assertThat(result.get(0).isRead()).isTrue();
        assertThat(result.get(0).getCreatedAt()).isEqualTo("2025-05-05T05:05");
        assertThat(result.get(0).getMessage()).isEqualTo("msg2");
    }

    @Test
    void filterByTypeRepositoryErrorIsWrappedAsRetrievalFailed() {
        when(notificationRepository.findByUserEmailAndType(any(), any())).thenThrow(new RuntimeException("db"));

        Throwable t = catchThrowable(() -> service.filterByType("a@test.com", NotificationType.LIKE));

        assertThat(((NotificationException) t).getErrorCode()).isEqualTo(NotificationErrorCode.NOTIFICATION_RETRIEVAL_FAILED);
    }

    @Test
    void filterByCategoryAndIsReadPassesArgumentsThrough() {
        Pageable pageable = PageRequest.of(0, 5);
        when(notificationRepository.findByUserEmailAndCategoryAndIsRead("a@test.com", null, false, pageable))
                .thenReturn(new PageImpl<>(List.of(notification(3, false)), pageable, 1));

        List<NotificationDTO> result = service.filterByCategoryAndIsRead("a@test.com", null, false, pageable);

        assertThat(result).extracting(NotificationDTO::getId).containsExactly(3L);
    }

    @Test
    void filterByTypeAndIsReadPassesArgumentsThrough() {
        Pageable pageable = PageRequest.of(1, 5);
        when(notificationRepository.findByUserEmailAndTypeAndIsRead("a@test.com", NotificationType.COMMENT, true, pageable))
                .thenReturn(new PageImpl<>(List.of(notification(4, true)), pageable, 6));

        List<NotificationDTO> result = service.filterByTypeAndIsRead("a@test.com", NotificationType.COMMENT, true, pageable);

        assertThat(result).extracting(NotificationDTO::getId).containsExactly(4L);
    }

    @Test
    void countUnreadUsesUnpagedFalseQueryTotal() {
        when(notificationRepository.findByUserEmailAndIsRead("a@test.com", false, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(notification(1, false), notification(2, false))));

        assertThat(service.countUnreadNotifications("a@test.com")).isEqualTo(2);
    }
}
