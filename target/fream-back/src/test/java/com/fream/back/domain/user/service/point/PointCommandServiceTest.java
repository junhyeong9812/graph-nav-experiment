package com.fream.back.domain.user.service.point;

import com.fream.back.domain.user.dto.PointDto;
import com.fream.back.domain.user.entity.Point;
import com.fream.back.domain.user.entity.PointStatus;
import com.fream.back.domain.user.entity.User;
import com.fream.back.domain.user.exception.InsufficientPointsException;
import com.fream.back.domain.user.exception.InvalidPointAmountException;
import com.fream.back.domain.user.exception.UserNotFoundException;
import com.fream.back.domain.user.repository.PointRepository;
import com.fream.back.domain.user.service.query.UserQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * PointCommandService 의 적립 검증·생성 값, 사용 시 차감 순서와 부족 판정, 만료 처리를 고정한다.
 */
@ExtendWith(MockitoExtension.class)
class PointCommandServiceTest {

    @Mock
    private PointRepository pointRepository;

    @Mock
    private UserQueryService userQueryService;

    @InjectMocks
    private PointCommandService pointCommandService;

    private final User user = User.builder().id(1L).email("u@fream.com").build();

    private Point available(long id, int remaining, LocalDate expiration) {
        return Point.builder().id(id).amount(remaining).remainingAmount(remaining)
                .reason("earn").expirationDate(expiration).status(PointStatus.AVAILABLE).build();
    }

    @Test
    void addPoint_savesAvailablePointWith90DayExpiry() {
        when(userQueryService.findByEmail("u@fream.com")).thenReturn(user);
        when(pointRepository.save(any(Point.class))).thenAnswer(inv -> inv.getArgument(0));
        LocalDate before = LocalDate.now();

        PointDto.PointResponse response = pointCommandService.addPoint("u@fream.com",
                new PointDto.AddPointRequest(500, "리뷰 작성"));

        ArgumentCaptor<Point> captor = ArgumentCaptor.forClass(Point.class);
        verify(pointRepository).save(captor.capture());
        Point saved = captor.getValue();
        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.getAmount()).isEqualTo(500);
        assertThat(saved.getRemainingAmount()).isEqualTo(500);
        assertThat(saved.getStatus()).isEqualTo(PointStatus.AVAILABLE);
        assertThat(saved.getExpirationDate()).isBetween(before.plusDays(90), LocalDate.now().plusDays(90));
        assertThat(response.getReason()).isEqualTo("리뷰 작성");
    }

    @Test
    void addPoint_amountMustBe1To1Million_andReasonRequired() {
        assertThatThrownBy(() -> pointCommandService.addPoint("u@fream.com", new PointDto.AddPointRequest(0, "r")))
                .isInstanceOf(InvalidPointAmountException.class);
        assertThatThrownBy(() -> pointCommandService.addPoint("u@fream.com", new PointDto.AddPointRequest(1_000_001, "r")))
                .isInstanceOf(InvalidPointAmountException.class)
                .hasMessageContaining("1,000,000");
        assertThatThrownBy(() -> pointCommandService.addPoint("u@fream.com", new PointDto.AddPointRequest(10, "  ")))
                .isInstanceOf(InvalidPointAmountException.class);
        verifyNoInteractions(userQueryService, pointRepository);

        when(userQueryService.findByEmail("u@fream.com")).thenReturn(user);
        when(pointRepository.save(any(Point.class))).thenAnswer(inv -> inv.getArgument(0));
        PointDto.PointResponse response = pointCommandService.addPoint("u@fream.com",
                new PointDto.AddPointRequest(1_000_000, "r"));
        assertThat(response.getAmount()).isEqualTo(1_000_000);
    }

    @Test
    void addPoint_repositoryFailure_isWrappedAsInvalidPointAmount() {
        when(userQueryService.findByEmail("u@fream.com")).thenReturn(user);
        when(pointRepository.save(any(Point.class))).thenThrow(new RuntimeException("db"));

        assertThatThrownBy(() -> pointCommandService.addPoint("u@fream.com", new PointDto.AddPointRequest(10, "r")))
                .isInstanceOf(InvalidPointAmountException.class)
                .hasMessage("포인트 적립 처리 중 오류가 발생했습니다.");
    }

    @Test
    void usePoint_insufficientOrNullTotal_throwsWithoutConsuming_andUnknownUserPropagates() {
        when(userQueryService.findByEmail("u@fream.com")).thenReturn(user);
        when(pointRepository.getTotalAvailablePoints(user)).thenReturn(100, (Integer) null);

        assertThatThrownBy(() -> pointCommandService.usePoint("u@fream.com", new PointDto.UsePointRequest(101, "구매")))
                .isInstanceOf(InsufficientPointsException.class);
        assertThatThrownBy(() -> pointCommandService.usePoint("u@fream.com", new PointDto.UsePointRequest(1, "구매")))
                .isInstanceOf(InsufficientPointsException.class);
        verify(pointRepository, never()).findByUserAndStatusOrderByExpirationDateAsc(any(), any());

        when(userQueryService.findByEmail("x@fream.com")).thenThrow(new UserNotFoundException("x@fream.com"));
        assertThatThrownBy(() -> pointCommandService.usePoint("x@fream.com", new PointDto.UsePointRequest(1, "r")))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void usePoint_consumesInRepositoryOrder_overwritesReason_andSavesOnlyTouched() {
        Point first = available(1L, 300, LocalDate.now().plusDays(1));
        Point second = available(2L, 500, LocalDate.now().plusDays(5));
        Point third = available(3L, 400, LocalDate.now().plusDays(9));
        when(userQueryService.findByEmail("u@fream.com")).thenReturn(user);
        when(pointRepository.getTotalAvailablePoints(user)).thenReturn(1200, 700);
        when(pointRepository.findByUserAndStatusOrderByExpirationDateAsc(user, PointStatus.AVAILABLE))
                .thenReturn(List.of(first, second, third));

        PointDto.UsePointResponse response = pointCommandService.usePoint("u@fream.com",
                new PointDto.UsePointRequest(500, "주문 결제"));

        assertThat(first.getRemainingAmount()).isZero();
        assertThat(first.getStatus()).isEqualTo(PointStatus.USED);
        assertThat(second.getRemainingAmount()).isEqualTo(300);
        assertThat(second.getStatus()).isEqualTo(PointStatus.AVAILABLE);
        assertThat(third.getRemainingAmount()).isEqualTo(400);

        assertThat(first.getReason()).isEqualTo("주문 결제");
        assertThat(second.getReason()).isEqualTo("주문 결제");
        assertThat(third.getReason()).isEqualTo("earn");

        verify(pointRepository).save(first);
        verify(pointRepository).save(second);
        verify(pointRepository, never()).save(third);

        assertThat(response.getUsedPoints()).isEqualTo(500);
        assertThat(response.getRemainingTotalPoints()).isEqualTo(700);
    }

    @Test
    void expirePoints_expiresAllReturned_returnsCount_andZeroOnFailure() {
        Point p1 = available(1L, 100, LocalDate.now().minusDays(1));
        Point p2 = available(2L, 200, LocalDate.now().minusDays(2));
        LocalDate before = LocalDate.now();
        when(pointRepository.findByStatusAndExpirationDateBefore(eq(PointStatus.AVAILABLE), any(LocalDate.class)))
                .thenReturn(List.of(p1, p2))
                .thenThrow(new RuntimeException("db"));

        int count = pointCommandService.expirePoints();

        assertThat(count).isEqualTo(2);
        assertThat(p1.getStatus()).isEqualTo(PointStatus.EXPIRED);
        assertThat(p2.getRemainingAmount()).isZero();
        ArgumentCaptor<LocalDate> dateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(pointRepository).findByStatusAndExpirationDateBefore(eq(PointStatus.AVAILABLE), dateCaptor.capture());
        assertThat(dateCaptor.getValue()).isBetween(before, LocalDate.now());

        assertThat(pointCommandService.expirePoints()).isZero();
    }
}
