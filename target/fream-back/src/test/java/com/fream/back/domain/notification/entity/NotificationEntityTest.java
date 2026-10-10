package com.fream.back.domain.notification.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 알림 엔티티 읽음 상태와 유형·카테고리 매핑을 고정한다.
 */
class NotificationEntityTest {

    @Test
    void typesByShoppingCategory() {
        assertThat(NotificationType.getTypesByCategory(NotificationCategory.SHOPPING))
                .containsExactly(NotificationType.TRADE, NotificationType.BID, NotificationType.STORAGE,
                        NotificationType.FAVORITE, NotificationType.BENEFIT, NotificationType.ANNOUNCEMENT,
                        NotificationType.REFUND);
    }

    @Test
    void typesByStyleCategory() {
        assertThat(NotificationType.getTypesByCategory(NotificationCategory.STYLE))
                .containsExactly(NotificationType.LIKE, NotificationType.COMMENT, NotificationType.FOLLOW);
    }

    @Test
    void nullCategoryReturnsAllTypes() {
        assertThat(NotificationType.getTypesByCategory(null)).containsExactly(NotificationType.values());
        assertThat(NotificationType.values()).hasSize(10);
    }

    @Test
    void readStateDefaultsToFalseAndToggles() {
        Notification n = Notification.builder().message("m").build();
        assertThat(n.isRead()).isFalse();

        n.markAsRead();
        assertThat(n.isRead()).isTrue();

        n.markAsUnread();
        assertThat(n.isRead()).isFalse();
    }
}
