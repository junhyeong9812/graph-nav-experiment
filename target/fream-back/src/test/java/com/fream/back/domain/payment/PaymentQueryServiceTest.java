package com.fream.back.domain.payment;

import com.fream.back.domain.payment.dto.AccountPaymentDto;
import com.fream.back.domain.payment.dto.CardPaymentDto;
import com.fream.back.domain.payment.dto.GeneralPaymentDto;
import com.fream.back.domain.payment.dto.PaymentDto;
import com.fream.back.domain.payment.entity.AccountPayment;
import com.fream.back.domain.payment.entity.CardPayment;
import com.fream.back.domain.payment.entity.GeneralPayment;
import com.fream.back.domain.payment.entity.Payment;
import com.fream.back.domain.payment.entity.PaymentStatus;
import com.fream.back.domain.payment.exception.PaymentErrorCode;
import com.fream.back.domain.payment.exception.PaymentException;
import com.fream.back.domain.payment.exception.PaymentNotFoundException;
import com.fream.back.domain.payment.repository.PaymentRepository;
import com.fream.back.domain.payment.service.query.PaymentQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * 결제 조회 서비스의 결제 유형별 DTO 변환과 입력 검증·예외 변환을 고정한다.
 */
@ExtendWith(MockitoExtension.class)
class PaymentQueryServiceTest {

    @Mock PaymentRepository paymentRepository;
    @InjectMocks PaymentQueryService service;

    @Test
    void getPaymentDetailsMapsEachSubtypeToItsDto() {
        CardPayment card = CardPayment.builder().impUid("imp_c").paidAmount(1000).cardType("VISA").build();
        card.updateStatus(PaymentStatus.PAID);
        AccountPayment account = AccountPayment.builder().bankName("KB").paidAmount(2000).build();
        GeneralPayment general = GeneralPayment.builder().impUid("imp_g").paidAmount(3000).build();
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(card));
        when(paymentRepository.findById(2L)).thenReturn(Optional.of(account));
        when(paymentRepository.findById(3L)).thenReturn(Optional.of(general));

        PaymentDto c = service.getPaymentDetails(1L);
        assertThat(c).isInstanceOf(CardPaymentDto.class);
        assertThat(c.getPaymentType()).isEqualTo("CARD");
        assertThat(c.getImpUid()).isEqualTo("imp_c");
        assertThat(c.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(c.getPaidAmount()).isEqualTo(1000.0);

        PaymentDto a = service.getPaymentDetails(2L);
        assertThat(a).isInstanceOf(AccountPaymentDto.class);
        assertThat(a.getPaymentType()).isEqualTo("ACCOUNT");
        assertThat(a.getStatus()).isEqualTo(PaymentStatus.PENDING);

        PaymentDto g = service.getPaymentDetails(3L);
        assertThat(g).isInstanceOf(GeneralPaymentDto.class);
        assertThat(g.getPaymentType()).isEqualTo("GENERAL");
        assertThat(g.getImpUid()).isEqualTo("imp_g");
    }

    @Test
    void getPaymentDetailsNotFoundAndNullId() {
        when(paymentRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPaymentDetails(9L))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessage("결제 ID: 9를 찾을 수 없습니다.");
        assertThatThrownBy(() -> service.getPaymentDetails(null))
                .isInstanceOf(PaymentNotFoundException.class);
    }

    @Test
    void userAndSellerPaymentListsRequireEmailAndWrapRepositoryErrors() {
        Payment p = GeneralPayment.builder().impUid("g").paidAmount(1).build();
        when(paymentRepository.findByOrder_User_EmailAndIsSuccess("b@test.com", true)).thenReturn(List.of(p));
        when(paymentRepository.findBySale_Seller_EmailAndIsSuccess("s@test.com", true))
                .thenThrow(new RuntimeException("boom"));

        assertThat(service.getUserPayments("b@test.com", true))
                .extracting(PaymentDto::getImpUid).containsExactly("g");

        assertThatThrownBy(() -> service.getUserPayments(" ", true))
                .isInstanceOf(PaymentException.class)
                .extracting(e -> ((PaymentException) e).getErrorCode())
                .isEqualTo(PaymentErrorCode.PAYMENT_ACCESS_DENIED);
        assertThatThrownBy(() -> service.getSellerPayments("s@test.com", true))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("boom")
                .extracting(e -> ((PaymentException) e).getErrorCode())
                .isEqualTo(PaymentErrorCode.PAYMENT_RETRIEVAL_FAILED);
    }

    @Test
    void findByImpUidReturnsNullWhenMissingAndRejectsBlank() {
        when(paymentRepository.findByImpUid("none")).thenReturn(Optional.empty());

        assertThat(service.findByImpUid("none")).isNull();
        assertThatThrownBy(() -> service.findByImpUid(""))
                .isInstanceOf(PaymentException.class)
                .extracting(e -> ((PaymentException) e).getErrorCode())
                .isEqualTo(PaymentErrorCode.PAYMENT_RETRIEVAL_FAILED);
    }
}
