package com.fream.back.domain.payment;

import com.fream.back.domain.order.entity.Order;
import com.fream.back.domain.payment.entity.AccountPayment;
import com.fream.back.domain.payment.entity.CardPayment;
import com.fream.back.domain.payment.entity.GeneralPayment;
import com.fream.back.domain.payment.entity.Payment;
import com.fream.back.domain.payment.entity.PaymentStatus;
import com.fream.back.domain.sale.entity.Sale;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static com.fream.back.domain.payment.entity.PaymentStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 결제 엔티티의 상태 전이 표, 금액에 따른 성공 플래그, 주문/판매 배타 연관을 고정한다.
 */
class PaymentEntityStateTest {

    @Test
    void paymentStatusTransitionTableIsFixed() {
        Map<PaymentStatus, Set<PaymentStatus>> expected = Map.of(
                PENDING, EnumSet.of(PAID),
                PAID, EnumSet.of(REFUND_REQUESTED, REFUNDED),
                REFUND_REQUESTED, EnumSet.of(REFUNDED),
                REFUNDED, EnumSet.noneOf(PaymentStatus.class),
                FAILED, EnumSet.noneOf(PaymentStatus.class));

        for (PaymentStatus from : PaymentStatus.values()) {
            for (PaymentStatus to : PaymentStatus.values()) {
                assertThat(from.canTransitionTo(to))
                        .as("%s -> %s", from, to)
                        .isEqualTo(expected.get(from).contains(to));
            }
        }
    }

    @Test
    void newPaymentStartsPendingAndPaidAmountDrivesSuccessFlag() {
        GeneralPayment positive = GeneralPayment.builder().paidAmount(1000).build();
        assertThat(positive.getStatus()).isEqualTo(PENDING);
        assertThat(positive.isSuccess()).isTrue();
        assertThat(positive.getPaymentDate()).isNull();

        AccountPayment zero = AccountPayment.builder().paidAmount(0).build();
        assertThat(zero.isSuccess()).isFalse();

        CardPayment card = CardPayment.builder().paidAmount(-1).impUid("imp_x").build();
        assertThat(card.isSuccess()).isFalse();
        assertThat(card.getImpUid()).isEqualTo("imp_x");
    }

    @Test
    void updateSuccessStatusSetsPaymentDateOnlyWhenSuccessful() {
        Payment payment = GeneralPayment.builder().paidAmount(0).build();

        payment.updateSuccessStatus(false);
        assertThat(payment.getPaymentDate()).isNull();

        LocalDateTime before = LocalDateTime.now();
        payment.updateSuccessStatus(true);
        assertThat(payment.isSuccess()).isTrue();
        assertThat(payment.getPaymentDate()).isNotNull()
                .isAfterOrEqualTo(before).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void updateStatusThrowsIllegalStateOnDisallowedTransitionIncludingSameStatus() {
        Payment payment = GeneralPayment.builder().paidAmount(1000).build();

        assertThatThrownBy(() -> payment.updateStatus(PENDING))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("결제 상태 전환이 허용되지 않습니다: PENDING -> PENDING");

        payment.updateStatus(PAID);
        payment.updateStatus(REFUND_REQUESTED);
        payment.updateStatus(REFUNDED);
        assertThat(payment.getStatus()).isEqualTo(REFUNDED);
    }

    @Test
    void assignOrderAndAssignSaleAreMutuallyExclusive() {
        Payment payment = GeneralPayment.builder().paidAmount(1000).build();
        Order order = Order.builder().id(1L).build();
        Sale sale = Sale.builder().id(2L).build();

        payment.assignOrder(order);
        assertThat(payment.getOrder()).isSameAs(order);
        assertThat(payment.getSale()).isNull();

        payment.assignSale(sale);
        assertThat(payment.getSale()).isSameAs(sale);
        assertThat(payment.getOrder()).isNull();

        order.assignPayment(payment);
        assertThat(order.getPayment()).isSameAs(payment);
        assertThat(payment.getOrder()).isSameAs(order);
        assertThat(payment.getSale()).isNull();
    }
}
