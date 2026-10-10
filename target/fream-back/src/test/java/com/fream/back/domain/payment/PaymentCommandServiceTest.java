package com.fream.back.domain.payment;

import com.fream.back.domain.notification.entity.NotificationCategory;
import com.fream.back.domain.notification.entity.NotificationType;
import com.fream.back.domain.notification.service.command.NotificationCommandService;
import com.fream.back.domain.order.entity.Order;
import com.fream.back.domain.order.entity.OrderBid;
import com.fream.back.domain.payment.dto.AccountPaymentRequestDto;
import com.fream.back.domain.payment.dto.CardPaymentRequestDto;
import com.fream.back.domain.payment.dto.GeneralPaymentRequestDto;
import com.fream.back.domain.payment.entity.*;
import com.fream.back.domain.payment.exception.PaymentErrorCode;
import com.fream.back.domain.payment.exception.PaymentException;
import com.fream.back.domain.payment.exception.PaymentNotFoundException;
import com.fream.back.domain.payment.exception.PaymentProcessingException;
import com.fream.back.domain.payment.portone.PortOneApiClient;
import com.fream.back.domain.payment.repository.PaymentRepository;
import com.fream.back.domain.payment.service.command.PaymentCommandService;
import com.fream.back.domain.payment.service.query.PaymentInfoQueryService;
import com.fream.back.domain.sale.entity.Sale;
import com.fream.back.domain.sale.entity.SaleBid;
import com.fream.back.domain.sale.entity.SaleStatus;
import com.fream.back.domain.sale.service.query.SaleBidQueryService;
import com.fream.back.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 결제 명령 서비스의 유형별 결제 생성 결과, 검증 분기, 판매 상태·알림 연동, 환불 분기를 고정한다.
 */
@ExtendWith(MockitoExtension.class)
class PaymentCommandServiceTest {

    @Mock PaymentRepository paymentRepository;
    @Mock PaymentInfoQueryService paymentInfoQueryService;
    @Mock PortOneApiClient portOneApiClient;
    @Mock NotificationCommandService notificationCommandService;
    @Mock SaleBidQueryService saleBidQueryService;

    @InjectMocks PaymentCommandService service;

    private final User buyer = User.builder().id(1L).email("buyer@test.com").build();
    private final User seller = User.builder().id(2L).email("seller@test.com").build();

    private Order order() {
        return Order.builder().id(100L).user(buyer).build();
    }

    private GeneralPaymentRequestDto general(double amount) {
        return GeneralPaymentRequestDto.builder()
                .orderId(100L).userEmail("buyer@test.com").paidAmount(amount)
                .impUid("imp_g").pgProvider("kcp").receiptUrl("r").buyerName("구매자").buyerEmail("buyer@test.com")
                .build();
    }

    private void saveReturnsArgument() {
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void generalPaymentIsPaidSuccessfulAndLinkedToOrderAndUser() {
        saveReturnsArgument();
        Order order = order();
        when(saleBidQueryService.findByOrderId(100L)).thenReturn(null);

        Payment payment = service.processPayment(order, buyer, general(50000));

        assertThat(payment).isInstanceOf(GeneralPayment.class);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(payment.isSuccess()).isTrue();
        assertThat(payment.getPaymentDate()).isNotNull();
        assertThat(payment.getPaidAmount()).isEqualTo(50000.0);
        assertThat(payment.getOrder()).isSameAs(order);
        assertThat(payment.getUser()).isSameAs(buyer);
        assertThat(payment.getImpUid()).isEqualTo("imp_g");
        verifyNoInteractions(notificationCommandService);
    }

    @Test
    void generalPaymentWithoutImpUidFailsValidation() {
        GeneralPaymentRequestDto req = general(50000);
        req.setImpUid(" ");

        assertThatThrownBy(() -> service.processPayment(order(), buyer, req))
                .isInstanceOf(PaymentException.class)
                .extracting(e -> ((PaymentException) e).getErrorCode())
                .isEqualTo(PaymentErrorCode.PAYMENT_VALIDATION_FAILED);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void requestValidationChecksOrderIdEmailAndAmount() {
        GeneralPaymentRequestDto wrongOrder = general(1000);
        wrongOrder.setOrderId(999L);
        GeneralPaymentRequestDto wrongEmail = general(1000);
        wrongEmail.setUserEmail("x@test.com");

        assertThatThrownBy(() -> service.processPayment(order(), buyer, wrongOrder))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("요청ID: 999, 실제ID: 100");
        assertThatThrownBy(() -> service.processPayment(order(), buyer, wrongEmail))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("사용자 이메일이 일치하지 않습니다");
        assertThatThrownBy(() -> service.processPayment(order(), buyer, general(0)))
                .isInstanceOf(PaymentException.class)
                .extracting(e -> ((PaymentException) e).getErrorCode())
                .isEqualTo(PaymentErrorCode.INVALID_PAYMENT_AMOUNT);
        assertThatThrownBy(() -> service.processPayment(null, buyer, general(1000)))
                .isInstanceOf(PaymentException.class)
                .extracting(e -> ((PaymentException) e).getErrorCode())
                .isEqualTo(PaymentErrorCode.PAYMENT_PROCESSING_FAILED);
        verifyNoInteractions(paymentRepository);
    }

    @Test
    void cardPaymentUsesStoredCardAndApiResponse() {
        saveReturnsArgument();
        PaymentInfo info = PaymentInfo.builder()
                .cardNumber("1234567812345678").cardPassword("12").expirationDate("12/30").birthDate("900101")
                .build();
        when(paymentInfoQueryService.getPaymentInfoEntity("buyer@test.com", 7L)).thenReturn(info);
        when(portOneApiClient.processCardPayment(info, 30000.0)).thenReturn(Map.of(
                "status", "success", "imp_uid", "imp_c", "card_name", "신한카드",
                "receipt_url", "url", "pg_provider", "nice", "pg_tid", "tid"));
        CardPaymentRequestDto req = CardPaymentRequestDto.builder()
                .orderId(100L).userEmail("buyer@test.com").paidAmount(30000).paymentInfoId(7L).build();

        CardPayment payment = (CardPayment) service.processPayment(order(), buyer, req);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(payment.isSuccess()).isTrue();
        assertThat(payment.getPaymentDate()).isNotNull();
        assertThat(payment.getCardNumber()).isEqualTo("1234567812345678");
        assertThat(payment.getCardType()).isEqualTo("신한카드");
        assertThat(payment.getImpUid()).isEqualTo("imp_c");
        assertThat(payment.getPgTid()).isEqualTo("tid");
    }

    @Test
    void cardPaymentWithNonSuccessResponseStaysPendingButSuccessFlagFollowsAmount() {
        saveReturnsArgument();
        PaymentInfo info = PaymentInfo.builder().cardNumber("1").build();
        when(paymentInfoQueryService.getPaymentInfoEntity(anyString(), any())).thenReturn(info);
        when(portOneApiClient.processCardPayment(any(), anyDouble())).thenReturn(Map.of("status", "failed"));
        CardPaymentRequestDto req = CardPaymentRequestDto.builder()
                .orderId(100L).userEmail("buyer@test.com").paidAmount(30000).paymentInfoId(7L).build();

        Payment payment = service.processPayment(order(), buyer, req);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.isSuccess()).isTrue();
        assertThat(payment.getPaymentDate()).isNull();
    }

    @Test
    void cardPaymentApiFailureBecomesPaymentApiError() {
        when(paymentInfoQueryService.getPaymentInfoEntity(anyString(), any()))
                .thenReturn(PaymentInfo.builder().build());
        when(portOneApiClient.processCardPayment(any(), anyDouble())).thenThrow(new RuntimeException("timeout"));
        CardPaymentRequestDto req = CardPaymentRequestDto.builder()
                .orderId(100L).userEmail("buyer@test.com").paidAmount(30000).paymentInfoId(7L).build();

        assertThatThrownBy(() -> service.processPayment(order(), buyer, req))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("timeout")
                .extracting(e -> ((PaymentException) e).getErrorCode())
                .isEqualTo(PaymentErrorCode.PAYMENT_API_ERROR);
    }

    @Test
    void accountPaymentWithValidBankFailsOnPendingToPendingTransition() {
        AccountPaymentRequestDto req = AccountPaymentRequestDto.builder()
                .orderId(100L).userEmail("buyer@test.com").paidAmount(20000)
                .bankName("kb").accountNumber("123-456").accountHolder("홍길동").build();

        assertThatThrownBy(() -> service.processPayment(order(), buyer, req))
                .isInstanceOf(PaymentProcessingException.class)
                .hasMessageContaining("결제 상태 전환이 허용되지 않습니다: PENDING -> PENDING");
        verifyNoInteractions(paymentRepository);
    }

    @Test
    void accountPaymentWithUnknownBankFailsAsInvalidBankInfo() {
        AccountPaymentRequestDto req = AccountPaymentRequestDto.builder()
                .orderId(100L).userEmail("buyer@test.com").paidAmount(20000)
                .bankName("없는은행").accountNumber("1").accountHolder("a").build();

        assertThatThrownBy(() -> service.processPayment(order(), buyer, req))
                .isInstanceOf(PaymentException.class)
                .hasMessage("유효하지 않은 은행 정보: 없는은행")
                .extracting(e -> ((PaymentException) e).getErrorCode())
                .isEqualTo(PaymentErrorCode.INVALID_BANK_INFO);
    }

    @Test
    void paymentWithOrderBidSaleMarksSaleSoldAndNotifiesBuyerAndSeller() {
        saveReturnsArgument();
        Sale sale = Sale.builder().id(50L).seller(seller).status(SaleStatus.IN_STORAGE).build();
        Order order = order();
        OrderBid bid = OrderBid.builder().sale(sale).build();
        order.assignOrderBid(bid);

        service.processPayment(order, buyer, general(1000));

        assertThat(sale.getStatus()).isEqualTo(SaleStatus.SOLD);
        verify(notificationCommandService).createNotification(1L, NotificationCategory.SHOPPING,
                NotificationType.BID, "결제가 완료되었습니다. 주문 ID: 100");
        verify(notificationCommandService).createNotification(2L, NotificationCategory.SHOPPING,
                NotificationType.BID, "구매자가 결제를 완료하였습니다. 판매 ID: 50");
        verifyNoInteractions(saleBidQueryService);
    }

    @Test
    void paymentWithoutOrderBidFallsBackToSaleBidAndSwallowsNotificationFailure() {
        saveReturnsArgument();
        Sale sale = Sale.builder().id(51L).seller(seller).status(SaleStatus.SELLING).build();
        when(saleBidQueryService.findByOrderId(100L)).thenReturn(SaleBid.builder().sale(sale).build());
        when(notificationCommandService.createNotification(any(), any(), any(), anyString()))
                .thenThrow(new RuntimeException("redis down"));

        Payment payment = service.processPayment(order(), buyer, general(1000));

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(sale.getStatus()).isEqualTo(SaleStatus.SOLD);
    }

    @Test
    void refundPaymentSuccessMarksRefundedAndNotifiesUser() {
        Payment payment = GeneralPayment.builder().impUid("imp_r").paidAmount(1000).build();
        payment.updateStatus(PaymentStatus.PAID);
        payment.assignUser(buyer);
        when(paymentRepository.findById(9L)).thenReturn(Optional.of(payment));
        when(portOneApiClient.cancelPayment("imp_r")).thenReturn(true);

        String result = service.refundPayment(9L);

        assertThat(result).isEqualTo("환불이 성공적으로 완료되었습니다.");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        verify(paymentRepository).save(payment);
        verify(notificationCommandService).createNotification(1L, NotificationCategory.SHOPPING,
                NotificationType.REFUND, "결제가 환불되었습니다. 환불금액: 1000.0원");
    }

    @Test
    void refundPaymentFailureMarksRefundRequested() {
        Payment payment = GeneralPayment.builder().impUid("imp_r").paidAmount(1000).build();
        payment.updateStatus(PaymentStatus.PAID);
        when(paymentRepository.findById(9L)).thenReturn(Optional.of(payment));
        when(portOneApiClient.cancelPayment("imp_r")).thenReturn(false);

        String result = service.refundPayment(9L);

        assertThat(result).isEqualTo("환불 요청이 실패하였습니다. 관리자에게 문의하세요.");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUND_REQUESTED);
        verifyNoInteractions(notificationCommandService);
    }

    @Test
    void refundPaymentGuardsStatusImpUidAndExistence() {
        Payment pending = GeneralPayment.builder().impUid("imp").paidAmount(1000).build();
        Payment paidNoImp = GeneralPayment.builder().paidAmount(1000).build();
        paidNoImp.updateStatus(PaymentStatus.PAID);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(pending));
        when(paymentRepository.findById(2L)).thenReturn(Optional.of(paidNoImp));
        when(paymentRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.refundPayment(1L))
                .isInstanceOf(PaymentException.class)
                .extracting(e -> ((PaymentException) e).getErrorCode())
                .isEqualTo(PaymentErrorCode.INVALID_PAYMENT_STATUS);
        assertThatThrownBy(() -> service.refundPayment(2L))
                .isInstanceOf(PaymentException.class)
                .extracting(e -> ((PaymentException) e).getErrorCode())
                .isEqualTo(PaymentErrorCode.PAYMENT_CANCELLATION_FAILED);
        assertThatThrownBy(() -> service.refundPayment(3L))
                .isInstanceOf(PaymentNotFoundException.class);
        verifyNoInteractions(portOneApiClient);
    }

    @Test
    void updatePaymentStatusAppliesAllowedTransitionAndRejectsOthers() {
        Payment payment = GeneralPayment.builder().paidAmount(1000).build();

        service.updatePaymentStatus(payment, PaymentStatus.PAID);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        verify(paymentRepository).save(payment);

        assertThatThrownBy(() -> service.updatePaymentStatus(payment, PaymentStatus.PENDING))
                .isInstanceOf(PaymentException.class)
                .hasMessage("현재 상태(PAID)에서 PENDING 상태로 변경할 수 없습니다.");
        assertThatThrownBy(() -> service.updatePaymentStatus(null, PaymentStatus.PAID))
                .isInstanceOf(PaymentNotFoundException.class);
    }
}
