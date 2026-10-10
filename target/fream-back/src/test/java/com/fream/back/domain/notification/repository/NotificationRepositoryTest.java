package com.fream.back.domain.notification.repository;

import com.fream.back.domain.notification.entity.Notification;
import com.fream.back.domain.notification.entity.NotificationCategory;
import com.fream.back.domain.notification.entity.NotificationType;
import com.fream.back.domain.notification.exception.NotificationErrorCode;
import com.fream.back.domain.notification.exception.NotificationException;
import com.fream.back.domain.notification.service.query.NotificationQueryService;
import com.fream.back.domain.user.entity.User;
import com.fream.back.global.config.QueryDslConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * NotificationRepository QueryDSL 커스텀 조회의 필터·정렬·페이징 결과를 고정한다.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
class NotificationRepositoryTest {

    @Autowired EntityManager em;
    @Autowired NotificationRepository notificationRepository;

    private static final LocalDateTime BASE = LocalDateTime.of(2025, 1, 1, 0, 0);

    // n1..n5: a@test.com, n6: b@test.com. 생성 시각은 번호 순으로 증가
    private Notification n1, n2, n3, n4, n5, n6;

    private User user(String email, String phone) {
        User u = User.builder().email(email).password("pw").referralCode("ref-" + email).phoneNumber(phone).build();
        em.persist(u);
        return u;
    }

    private Notification notification(User u, NotificationCategory c, NotificationType t, boolean read, int day) {
        Notification n = Notification.builder().user(u).category(c).type(t).message("m" + day).isRead(read).build();
        em.persist(n);
        em.flush();
        em.createNativeQuery("update notification set created_date = ? where id = ?")
                .setParameter(1, BASE.plusDays(day)).setParameter(2, n.getId()).executeUpdate();
        return n;
    }

    @BeforeEach
    void setUp() {
        User a = user("a@test.com", "010-1");
        User b = user("b@test.com", "010-2");
        n1 = notification(a, NotificationCategory.SHOPPING, NotificationType.BID, false, 1);
        n2 = notification(a, NotificationCategory.STYLE, NotificationType.LIKE, true, 2);
        n3 = notification(a, NotificationCategory.SHOPPING, NotificationType.TRADE, false, 3);
        n4 = notification(a, NotificationCategory.STYLE, NotificationType.COMMENT, false, 4);
        n5 = notification(a, NotificationCategory.SHOPPING, NotificationType.BID, true, 5);
        n6 = notification(b, NotificationCategory.SHOPPING, NotificationType.BID, false, 6);
        em.flush();
        em.clear();
    }

    private static List<Long> ids(List<Notification> list) {
        return list.stream().map(Notification::getId).toList();
    }

    @Test
    void findByCategoryNewestFirst() {
        assertThat(ids(notificationRepository.findByUserEmailAndCategory("a@test.com", NotificationCategory.SHOPPING)))
                .containsExactly(n5.getId(), n3.getId(), n1.getId());
    }

    @Test
    void findByTypeNewestFirst() {
        assertThat(ids(notificationRepository.findByUserEmailAndType("a@test.com", NotificationType.BID)))
                .containsExactly(n5.getId(), n1.getId());
    }

    @Test
    void nullFiltersAreIgnored() {
        assertThat(ids(notificationRepository.findByUserEmailAndCategory("a@test.com", null)))
                .containsExactly(n5.getId(), n4.getId(), n3.getId(), n2.getId(), n1.getId());
        assertThat(notificationRepository.findByUserEmailAndType(null, NotificationType.BID))
                .hasSize(3);
    }

    @Test
    void findAllByUserEmailNewestFirst() {
        assertThat(ids(notificationRepository.findAllByUserEmail("b@test.com"))).containsExactly(n6.getId());
        assertThat(notificationRepository.findAllByUserEmail("none@test.com")).isEmpty();
    }

    @Test
    void isReadPageDefaultsToCreatedDateDescWithTotal() {
        Page<Notification> page = notificationRepository.findByUserEmailAndIsRead("a@test.com", false, PageRequest.of(0, 2));

        assertThat(ids(page.getContent())).containsExactly(n4.getId(), n3.getId());
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    void categoryAndIsReadPageWithNullCategory() {
        Page<Notification> page = notificationRepository.findByUserEmailAndCategoryAndIsRead(
                "a@test.com", null, true, PageRequest.of(0, 10));

        assertThat(ids(page.getContent())).containsExactly(n5.getId(), n2.getId());
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void typeAndIsReadPageSortedAscendingByCreatedDate() {
        Page<Notification> page = notificationRepository.findByUserEmailAndTypeAndIsRead(
                "a@test.com", null, false, PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "createdDate")));

        assertThat(ids(page.getContent())).containsExactly(n1.getId(), n3.getId(), n4.getId());
    }

    @Test
    void unknownSortPropertyFallsBackToCreatedDateDesc() {
        Page<Notification> page = notificationRepository.findByUserEmailAndCategoryAndIsRead(
                "a@test.com", NotificationCategory.SHOPPING, false, PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "message")));

        assertThat(ids(page.getContent())).containsExactly(n3.getId(), n1.getId());
    }

    @Test
    void countUnreadWithRealRepositoryFailsOnUnpagedPageable() {
        NotificationQueryService queryService = new NotificationQueryService(notificationRepository);

        Throwable t = catchThrowable(() -> queryService.countUnreadNotifications("a@test.com"));

        assertThat(t).isInstanceOf(NotificationException.class);
        assertThat(((NotificationException) t).getErrorCode()).isEqualTo(NotificationErrorCode.NOTIFICATION_RETRIEVAL_FAILED);
        assertThat(t.getCause()).isInstanceOf(UnsupportedOperationException.class);
    }
}
