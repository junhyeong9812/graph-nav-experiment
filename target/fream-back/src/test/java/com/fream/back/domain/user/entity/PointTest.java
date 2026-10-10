package com.fream.back.domain.user.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Point 엔티티의 사용·소멸·유효성 판정과 User 합계 계산을 고정한다.
 */
class PointTest {

    private Point availablePoint(int amount, LocalDate expiration) {
        return Point.builder()
                .amount(amount)
                .remainingAmount(amount)
                .reason("적립")
                .expirationDate(expiration)
                .status(PointStatus.AVAILABLE)
                .build();
    }

    @Test
    void constructor_setsRemainingToAmount_available_and90DaysExpiry() {
        LocalDate before = LocalDate.now();
        Point point = new Point(null, 1000, "가입 축하");

        assertThat(point.getRemainingAmount()).isEqualTo(1000);
        assertThat(point.getStatus()).isEqualTo(PointStatus.AVAILABLE);
        assertThat(point.getExpirationDate()).isBetween(before.plusDays(90), LocalDate.now().plusDays(90));
    }

    @Test
    void use_partialKeepsAvailable_exactOrOverMarksUsed() {
        Point partial = availablePoint(1000, LocalDate.now().plusDays(10));
        assertThat(partial.use(300)).isEqualTo(300);
        assertThat(partial.getRemainingAmount()).isEqualTo(700);
        assertThat(partial.getStatus()).isEqualTo(PointStatus.AVAILABLE);

        Point exact = availablePoint(500, LocalDate.now().plusDays(10));
        assertThat(exact.use(500)).isEqualTo(500);
        assertThat(exact.getStatus()).isEqualTo(PointStatus.USED);

        Point over = availablePoint(1000, LocalDate.now().plusDays(10));
        assertThat(over.use(1500)).isEqualTo(1000);
        assertThat(over.getRemainingAmount()).isZero();
        assertThat(over.getStatus()).isEqualTo(PointStatus.USED);
    }

    @Test
    void use_onNonAvailable_returnsZero_andExpireOnlyFromAvailable() {
        Point expired = availablePoint(1000, LocalDate.now().plusDays(10));
        expired.setStatus(PointStatus.EXPIRED);
        assertThat(expired.use(100)).isZero();
        assertThat(expired.getRemainingAmount()).isEqualTo(1000);

        Point available = availablePoint(1000, LocalDate.now().minusDays(1));
        available.expire();
        assertThat(available.getStatus()).isEqualTo(PointStatus.EXPIRED);
        assertThat(available.getRemainingAmount()).isZero();

        Point used = availablePoint(1000, LocalDate.now().minusDays(1));
        used.setStatus(PointStatus.USED);
        used.expire();
        assertThat(used.getStatus()).isEqualTo(PointStatus.USED);
        assertThat(used.getRemainingAmount()).isEqualTo(1000);
    }

    @Test
    void isValid_requiresAvailable_andExpirationAfterToday() {
        assertThat(availablePoint(1, LocalDate.now().plusDays(1)).isValid()).isTrue();
        assertThat(availablePoint(1, LocalDate.now()).isValid()).isFalse();

        Point used = availablePoint(1, LocalDate.now().plusDays(1));
        used.setStatus(PointStatus.USED);
        assertThat(used.isValid()).isFalse();
    }

    @Test
    void userCalculateTotalPoints_sumsAmountOfValidPoints_notRemaining() {
        User user = User.builder().email("a@a.com").build();
        Point partlyUsed = availablePoint(1000, LocalDate.now().plusDays(5));
        partlyUsed.use(400);
        user.addPoint(partlyUsed);
        user.addPoint(availablePoint(200, LocalDate.now().plusDays(5)));
        user.addPoint(availablePoint(700, LocalDate.now().minusDays(1)));

        assertThat(user.calculateTotalPoints()).isEqualTo(1200);
        assertThat(partlyUsed.getUser()).isSameAs(user);
    }
}
