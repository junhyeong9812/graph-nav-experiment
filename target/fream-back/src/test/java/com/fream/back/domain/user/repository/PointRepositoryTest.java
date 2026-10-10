package com.fream.back.domain.user.repository;

import com.fream.back.domain.user.entity.Point;
import com.fream.back.domain.user.entity.PointStatus;
import com.fream.back.domain.user.entity.User;
import com.fream.back.global.config.QueryDslConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PointRepository 커스텀 쿼리(가용 합계·만료 임박·만료 대상)의 필터·정렬 결과를 고정한다.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
class PointRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private PointRepository pointRepository;

    private User user;
    private User other;
    private final LocalDate today = LocalDate.now();

    private User persistUser(String email, String referral) {
        User u = User.builder()
                .email(email)
                .password("pw")
                .referralCode(referral)
                .phoneNumber("01000000000")
                .build();
        return em.persist(u);
    }

    private Point persistPoint(User owner, int remaining, PointStatus status, LocalDate expiration) {
        Point p = Point.builder()
                .user(owner)
                .amount(remaining)
                .remainingAmount(remaining)
                .reason("r")
                .expirationDate(expiration)
                .status(status)
                .build();
        return em.persist(p);
    }

    @BeforeEach
    void setUp() {
        user = persistUser("p1@test.com", "REFP1");
        other = persistUser("p2@test.com", "REFP2");
    }

    @Test
    void getTotalAvailablePoints_sumsRemainingOfAvailableOnly_perUser() {
        persistPoint(user, 100, PointStatus.AVAILABLE, today.plusDays(10));
        persistPoint(user, 250, PointStatus.AVAILABLE, today.plusDays(20));
        persistPoint(user, 999, PointStatus.USED, today.plusDays(20));
        persistPoint(user, 888, PointStatus.EXPIRED, today.minusDays(1));
        persistPoint(other, 5000, PointStatus.AVAILABLE, today.plusDays(10));
        em.flush();

        assertThat(pointRepository.getTotalAvailablePoints(user)).isEqualTo(350);
    }

    @Test
    void getTotalAvailablePoints_nullWhenNone_andIgnoresExpirationDate() {
        persistPoint(user, 100, PointStatus.USED, today.plusDays(10));
        persistPoint(other, 70, PointStatus.AVAILABLE, today.minusDays(3));
        em.flush();

        assertThat(pointRepository.getTotalAvailablePoints(user)).isNull();
        assertThat(pointRepository.getTotalAvailablePoints(other)).isEqualTo(70);
    }

    @Test
    void findByUserAndStatusOrderByExpirationDateAsc_ordersSoonestFirst() {
        Point late = persistPoint(user, 1, PointStatus.AVAILABLE, today.plusDays(30));
        Point early = persistPoint(user, 2, PointStatus.AVAILABLE, today.plusDays(1));
        Point mid = persistPoint(user, 3, PointStatus.AVAILABLE, today.plusDays(10));
        persistPoint(user, 4, PointStatus.USED, today);
        em.flush();

        List<Point> result = pointRepository.findByUserAndStatusOrderByExpirationDateAsc(user, PointStatus.AVAILABLE);

        assertThat(result).extracting(Point::getId)
                .containsExactly(early.getId(), mid.getId(), late.getId());
    }

    @Test
    void findExpiringPoints_inclusiveRange_availableOnly() {
        Point onToday = persistPoint(user, 1, PointStatus.AVAILABLE, today);
        Point onEnd = persistPoint(user, 2, PointStatus.AVAILABLE, today.plusDays(30));
        persistPoint(user, 3, PointStatus.AVAILABLE, today.plusDays(31));
        persistPoint(user, 4, PointStatus.AVAILABLE, today.minusDays(1));
        persistPoint(user, 5, PointStatus.USED, today.plusDays(5));
        persistPoint(other, 6, PointStatus.AVAILABLE, today.plusDays(5));
        em.flush();

        List<Point> result = pointRepository.findExpiringPoints(user, today, today.plusDays(30));

        assertThat(result).extracting(Point::getId).containsExactly(onToday.getId(), onEnd.getId());
    }

    @Test
    void findByStatusAndExpirationDateBefore_isStrictlyBefore_acrossUsers() {
        Point yesterday = persistPoint(user, 1, PointStatus.AVAILABLE, today.minusDays(1));
        Point otherYesterday = persistPoint(other, 1, PointStatus.AVAILABLE, today.minusDays(2));
        persistPoint(user, 1, PointStatus.AVAILABLE, today);
        persistPoint(user, 1, PointStatus.USED, today.minusDays(5));
        em.flush();

        List<Point> result = pointRepository.findByStatusAndExpirationDateBefore(PointStatus.AVAILABLE, today);

        assertThat(result).extracting(Point::getId)
                .containsExactlyInAnyOrder(yesterday.getId(), otherYesterday.getId());
    }
}
