package com.fream.back.domain.notification.service;

import com.fream.back.domain.notification.dto.NotificationDTO;
import com.fream.back.domain.notification.dto.NotificationRequestDTO;
import com.fream.back.domain.notification.entity.Notification;
import com.fream.back.domain.notification.entity.NotificationCategory;
import com.fream.back.domain.notification.entity.NotificationType;
import com.fream.back.domain.notification.exception.NotificationErrorCode;
import com.fream.back.domain.notification.exception.NotificationException;
import com.fream.back.domain.notification.repository.NotificationRepository;
import com.fream.back.domain.notification.service.command.NotificationCommandService;
import com.fream.back.domain.order.entity.Order;
import com.fream.back.domain.user.entity.User;
import com.fream.back.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * NotificationCommandService의 생성·읽음·삭제·실시간 전송 분기와 예외 코드를 고정한다.
 */
@ExtendWith(MockitoExtension.class)
class NotificationCommandServiceTest {

    @Mock NotificationRepository notificationRepository;
    @Mock UserRepository userRepository;
    @Mock SimpMessagingTemplate messagingTemplate;
    @Mock RedisTemplate<String, String> redisTemplate;

    @InjectMocks NotificationCommandService service;

    private static final LocalDateTime CREATED = LocalDateTime.of(2025, 3, 1, 12, 30, 15);

    private final User user = User.builder().id(1L).email("a@test.com").build();
    private final User user2 = User.builder().id(2L).email("b@test.com").build();

    private void stubSaveAssignsIdAndCreatedDate() {
        AtomicLong seq = new AtomicLong(100);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            ReflectionTestUtils.setField(n, "id", seq.incrementAndGet());
            ReflectionTestUtils.setField(n, "createdDate", CREATED);
            return n;
        });
    }

    private static NotificationException notificationException(Throwable t) {
        assertThat(t).isInstanceOf(NotificationException.class);
        return (NotificationException) t;
    }

    @Test
    void createNotificationSavesUnreadAndReturnsDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        stubSaveAssignsIdAndCreatedDate();
        when(redisTemplate.hasKey("WebSocket:User:a@test.com")).thenReturn(false);

        NotificationDTO dto = service.createNotification(1L, NotificationCategory.SHOPPING, NotificationType.BID, "hello");

        assertThat(dto.getId()).isEqualTo(101L);
        assertThat(dto.getCategory()).isEqualTo(NotificationCategory.SHOPPING);
        assertThat(dto.getType()).isEqualTo(NotificationType.BID);
        assertThat(dto.getMessage()).isEqualTo("hello");
        assertThat(dto.isRead()).isFalse();
        assertThat(dto.getCreatedAt()).isEqualTo("2025-03-01T12:30:15");
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isSameAs(user);
        verifyNoInteractions(messagingTemplate);
    }

    @Test
    void createNotificationPushesToConnectedUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        stubSaveAssignsIdAndCreatedDate();
        when(redisTemplate.hasKey("WebSocket:User:a@test.com")).thenReturn(true);

        service.createNotification(1L, NotificationCategory.STYLE, NotificationType.LIKE, "liked");

        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSendToUser(eq("a@test.com"), eq("/queue/notifications"), payload.capture());
        assertThat(payload.getValue()).isInstanceOf(NotificationDTO.class);
        assertThat(((NotificationDTO) payload.getValue()).getMessage()).isEqualTo("liked");
    }

    @Test
    void createNotificationIgnoresPushFailure() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        stubSaveAssignsIdAndCreatedDate();
        when(redisTemplate.hasKey(anyString())).thenReturn(true);
        doThrow(new RuntimeException("ws down")).when(messagingTemplate)
                .convertAndSendToUser(anyString(), anyString(), any(Object.class));

        NotificationDTO dto = service.createNotification(1L, NotificationCategory.SHOPPING, NotificationType.TRADE, "x");

        assertThat(dto.getId()).isEqualTo(101L);
    }

    @Test
    void createNotificationUnknownUserThrowsUserNotFound() {
        when(userRepository.findById(9L)).thenReturn(Optional.empty());

        Throwable t = catchThrowable(() -> service.createNotification(9L, NotificationCategory.SHOPPING, NotificationType.BID, "x"));

        NotificationException e = notificationException(t);
        assertThat(e.getErrorCode()).isEqualTo(NotificationErrorCode.NOTIFICATION_USER_NOT_FOUND);
        assertThat(e.getMessage()).isEqualTo("사용자 ID: 9를 찾을 수 없습니다.");
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void createNotificationWithoutCreatedDateIsWrappedAsCreationFailed() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        Throwable t = catchThrowable(() -> service.createNotification(1L, NotificationCategory.SHOPPING, NotificationType.BID, "x"));

        NotificationException e = notificationException(t);
        assertThat(e.getErrorCode()).isEqualTo(NotificationErrorCode.NOTIFICATION_CREATION_FAILED);
        assertThat(e.getCause()).isInstanceOf(NullPointerException.class);
    }

    @Test
    void createNotificationForAllSavesOnePerUserAndPushesOnlyConnected() {
        when(userRepository.findAll()).thenReturn(List.of(user, user2));
        when(notificationRepository.saveAll(anyList())).thenAnswer(inv -> {
            List<Notification> list = inv.getArgument(0);
            long id = 200;
            for (Notification n : list) {
                ReflectionTestUtils.setField(n, "id", ++id);
                ReflectionTestUtils.setField(n, "createdDate", CREATED);
            }
            return list;
        });
        when(redisTemplate.hasKey("WebSocket:User:a@test.com")).thenReturn(false);
        when(redisTemplate.hasKey("WebSocket:User:b@test.com")).thenReturn(true);

        NotificationRequestDTO req = new NotificationRequestDTO(NotificationCategory.SHOPPING, NotificationType.ANNOUNCEMENT, "공지");
        List<NotificationDTO> result = service.createNotificationForAll(req);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(NotificationDTO::getMessage).containsOnly("공지");
        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate, times(1)).convertAndSendToUser(eq("b@test.com"), eq("/queue/notifications"), payload.capture());
        @SuppressWarnings("unchecked")
        List<NotificationDTO> pushed = (List<NotificationDTO>) payload.getValue();
        assertThat(pushed).extracting(NotificationDTO::getId).containsExactly(202L);
    }

    @Test
    void createNotificationForAllWithNoUsersReturnsEmpty() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<NotificationDTO> result = service.createNotificationForAll(
                new NotificationRequestDTO(NotificationCategory.SHOPPING, NotificationType.ANNOUNCEMENT, "공지"));

        assertThat(result).isEmpty();
        verify(notificationRepository, never()).saveAll(anyList());
    }

    @Test
    void markAsReadByOwner() {
        Notification n = Notification.builder().id(5L).user(user).message("m").build();
        when(notificationRepository.findById(5L)).thenReturn(Optional.of(n));

        service.markAsRead(5L, "a@test.com");

        assertThat(n.isRead()).isTrue();
    }

    @Test
    void markAsReadByOtherUserIsDenied() {
        Notification n = Notification.builder().id(5L).user(user).message("m").build();
        when(notificationRepository.findById(5L)).thenReturn(Optional.of(n));

        Throwable t = catchThrowable(() -> service.markAsRead(5L, "b@test.com"));

        assertThat(notificationException(t).getErrorCode()).isEqualTo(NotificationErrorCode.NOTIFICATION_ACCESS_DENIED);
        assertThat(n.isRead()).isFalse();
    }

    @Test
    void markAsReadMissingNotification() {
        when(notificationRepository.findById(5L)).thenReturn(Optional.empty());

        Throwable t = catchThrowable(() -> service.markAsRead(5L, "a@test.com"));

        assertThat(notificationException(t).getErrorCode()).isEqualTo(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
    }

    @Test
    void markAllAsReadMarksEveryNotification() {
        Notification n1 = Notification.builder().id(1L).user(user).build();
        Notification n2 = Notification.builder().id(2L).user(user).isRead(true).build();
        when(notificationRepository.findAllByUserEmail("a@test.com")).thenReturn(List.of(n1, n2));

        service.markAllAsRead("a@test.com");

        assertThat(n1.isRead()).isTrue();
        assertThat(n2.isRead()).isTrue();
    }

    @Test
    void deleteNotificationChecksExistence() {
        when(notificationRepository.existsById(5L)).thenReturn(true);
        service.deleteNotification(5L);
        verify(notificationRepository).deleteById(5L);

        when(notificationRepository.existsById(6L)).thenReturn(false);
        Throwable t = catchThrowable(() -> service.deleteNotification(6L));
        assertThat(notificationException(t).getErrorCode()).isEqualTo(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
        verify(notificationRepository, never()).deleteById(6L);
    }

    @Test
    void deleteNotificationsByUserDeletesFoundOnes() {
        List<Notification> list = List.of(Notification.builder().id(1L).user(user).build());
        when(notificationRepository.findAllByUserEmail("a@test.com")).thenReturn(list);
        when(notificationRepository.findAllByUserEmail("b@test.com")).thenReturn(List.of());

        service.deleteNotificationsByUser("a@test.com");
        service.deleteNotificationsByUser("b@test.com");

        verify(notificationRepository, times(1)).deleteAll(anyIterable());
        verify(notificationRepository).deleteAll(list);
    }

    @Test
    void notifyShipmentCompletedSendsToBuyerAndSwallowsErrors() {
        Order order = Order.builder().id(77L).user(user).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        stubSaveAssignsIdAndCreatedDate();
        when(redisTemplate.hasKey(anyString())).thenReturn(false);

        service.notifyShipmentCompleted(order);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getMessage()).isEqualTo("[배송 완료] 상품이 배송 완료되었습니다. 주문 ID: 77");
        assertThat(captor.getValue().getType()).isEqualTo(NotificationType.BID);
        assertThat(captor.getValue().getCategory()).isEqualTo(NotificationCategory.SHOPPING);

        Order orphan = Order.builder().id(78L).user(User.builder().id(9L).build()).build();
        when(userRepository.findById(9L)).thenReturn(Optional.empty());
        service.notifyShipmentCompleted(orphan); // 예외가 밖으로 나오지 않는다
    }

    @Test
    void updateWebSocketTtlExtendsOnlyWhenTenMinutesOrLess() {
        when(redisTemplate.getExpire("WebSocket:User:a@test.com", TimeUnit.SECONDS)).thenReturn(600L);
        when(redisTemplate.getExpire("WebSocket:User:b@test.com", TimeUnit.SECONDS)).thenReturn(601L);

        service.updateWebSocketConnectionTTL("a@test.com");
        service.updateWebSocketConnectionTTL("b@test.com");
        service.updateWebSocketConnectionTTL(null);

        verify(redisTemplate).expire("WebSocket:User:a@test.com", 30, TimeUnit.MINUTES);
        verify(redisTemplate, never()).expire(eq("WebSocket:User:b@test.com"), anyLong(), any(TimeUnit.class));
        verify(redisTemplate, times(2)).getExpire(anyString(), any(TimeUnit.class));
    }

    @Test
    void updateWebSocketTtlWrapsRedisError() {
        when(redisTemplate.getExpire(anyString(), any(TimeUnit.class))).thenThrow(new RuntimeException("redis down"));

        assertThatThrownBy(() -> service.updateWebSocketConnectionTTL("a@test.com"))
                .isInstanceOf(NotificationException.class)
                .extracting(e -> ((NotificationException) e).getErrorCode())
                .isEqualTo(NotificationErrorCode.WEBSOCKET_CONNECTION_MAINTENANCE_FAILED);
    }
}
