package com.fream.back.domain.payment;

import com.fream.back.domain.payment.dto.paymentInfo.PaymentInfoCreateDto;
import com.fream.back.domain.payment.dto.paymentInfo.PaymentInfoDto;
import com.fream.back.domain.payment.entity.PaymentInfo;
import com.fream.back.domain.payment.exception.PaymentErrorCode;
import com.fream.back.domain.payment.exception.PaymentException;
import com.fream.back.domain.payment.exception.PaymentInfoNotFoundException;
import com.fream.back.domain.payment.portone.PortOneApiClient;
import com.fream.back.domain.payment.repository.PaymentInfoRepository;
import com.fream.back.domain.payment.service.PaymentEncryptionService;
import com.fream.back.domain.payment.service.command.PaymentInfoCommandService;
import com.fream.back.domain.payment.service.query.PaymentInfoQueryService;
import com.fream.back.domain.user.entity.User;
import com.fream.back.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 결제수단(카드) 등록·삭제 명령과 조회 시 복호화·마스킹 동작을 고정한다.
 */
@ExtendWith(MockitoExtension.class)
class PaymentInfoServiceTest {

    @Mock UserRepository userRepository;
    @Mock PortOneApiClient portOneApiClient;
    @Mock PaymentInfoRepository paymentInfoRepository;
    @Mock PaymentEncryptionService encryptionService;

    private PaymentInfoCommandService commandService() {
        return new PaymentInfoCommandService(userRepository, portOneApiClient);
    }

    private PaymentInfoQueryService queryService() {
        return new PaymentInfoQueryService(paymentInfoRepository, encryptionService);
    }

    private PaymentInfoCreateDto validDto() {
        return new PaymentInfoCreateDto("1234567812345678", "12", "12/30", "900101");
    }

    @Test
    void createPaymentInfoRunsTestPaymentThenStoresCardAsGiven() {
        User user = User.builder().id(1L).email("u@test.com").build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(portOneApiClient.requestTestPayment(any())).thenReturn("imp_test");
        when(portOneApiClient.cancelTestPayment("imp_test")).thenReturn(true);

        commandService().createPaymentInfo("u@test.com", validDto());

        assertThat(user.getPaymentInfos()).hasSize(1);
        PaymentInfo saved = user.getPaymentInfos().get(0);
        assertThat(saved.getUser()).isSameAs(user);
        // 저장 시 별도 암호화 없이 입력값 그대로 들어간다
        assertThat(saved.getCardNumber()).isEqualTo("1234567812345678");
        assertThat(saved.getCardPassword()).isEqualTo("12");
        assertThat(saved.getBirthDate()).isEqualTo("900101");
        verify(portOneApiClient).cancelTestPayment("imp_test");
    }

    @Test
    void createPaymentInfoRejectsSixthCardAndFailedRefund() {
        User full = User.builder().id(1L).email("full@test.com").build();
        for (int i = 0; i < 5; i++) {
            full.addPaymentInfo(PaymentInfo.builder().cardNumber("c" + i).build());
        }
        User normal = User.builder().id(2L).email("n@test.com").build();
        when(userRepository.findByEmail("full@test.com")).thenReturn(Optional.of(full));
        when(userRepository.findByEmail("n@test.com")).thenReturn(Optional.of(normal));
        when(portOneApiClient.requestTestPayment(any())).thenReturn("imp_test");
        when(portOneApiClient.cancelTestPayment("imp_test")).thenReturn(false);

        assertThatThrownBy(() -> commandService().createPaymentInfo("full@test.com", validDto()))
                .isInstanceOf(PaymentException.class)
                .extracting(e -> ((PaymentException) e).getErrorCode())
                .isEqualTo(PaymentErrorCode.PAYMENT_INFO_LIMIT_EXCEEDED);

        assertThatThrownBy(() -> commandService().createPaymentInfo("n@test.com", validDto()))
                .isInstanceOf(PaymentException.class)
                .extracting(e -> ((PaymentException) e).getErrorCode())
                .isEqualTo(PaymentErrorCode.PAYMENT_INFO_CREATION_FAILED);
        assertThat(normal.getPaymentInfos()).isEmpty();
    }

    @Test
    void createPaymentInfoValidatesCardFormatBeforeCallingPortOne() {
        User user = User.builder().id(1L).email("u@test.com").build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> commandService().createPaymentInfo("u@test.com",
                new PaymentInfoCreateDto("1234", "12", "12/30", "900101")))
                .isInstanceOf(PaymentException.class)
                .hasMessage("카드 번호는 15-16자리 숫자여야 합니다.");
        assertThatThrownBy(() -> commandService().createPaymentInfo("u@test.com",
                new PaymentInfoCreateDto("1234567812345678", "12", "13/30", "900101")))
                .hasMessage("카드 유효기간은 MM/YY 형식이어야 합니다.");
        // 이 경로는 만료 여부를 검사하지 않는다
        when(portOneApiClient.requestTestPayment(any())).thenReturn("imp");
        when(portOneApiClient.cancelTestPayment("imp")).thenReturn(true);
        commandService().createPaymentInfo("u@test.com",
                new PaymentInfoCreateDto("123456781234567", "12", "01/20", "900101"));
        assertThat(user.getPaymentInfos()).hasSize(1);

        assertThatThrownBy(() -> commandService().createPaymentInfo(" ", validDto()))
                .isInstanceOf(PaymentException.class)
                .extracting(e -> ((PaymentException) e).getErrorCode())
                .isEqualTo(PaymentErrorCode.PAYMENT_INFO_CREATION_FAILED);
        verify(portOneApiClient, times(1)).requestTestPayment(any());
    }

    @Test
    void deletePaymentInfoRemovesMatchingCardOrThrowsNotFound() {
        User user = User.builder().id(1L).email("u@test.com").build();
        PaymentInfo info = PaymentInfo.builder().id(11L).cardNumber("x").build();
        user.addPaymentInfo(info);
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> commandService().deletePaymentInfo("u@test.com", 12L))
                .isInstanceOf(PaymentInfoNotFoundException.class);

        commandService().deletePaymentInfo("u@test.com", 11L);
        assertThat(user.getPaymentInfos()).isEmpty();
        assertThat(info.getUser()).isNull();
    }

    @Test
    void queryMasksDecryptedValuesAndAlwaysMasksPassword() {
        PaymentInfo info = PaymentInfo.builder().id(1L)
                .cardNumber("ENC_CARD").cardPassword("12").expirationDate("12/30").birthDate("ENC_BIRTH").build();
        when(paymentInfoRepository.findAllByUser_Email("u@test.com")).thenReturn(List.of(info));
        when(encryptionService.decrypt("ENC_CARD")).thenReturn("1234567812345678");
        when(encryptionService.decrypt("ENC_BIRTH")).thenReturn("900101");

        List<PaymentInfoDto> result = queryService().getPaymentInfos("u@test.com");

        assertThat(result).hasSize(1);
        PaymentInfoDto dto = result.get(0);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getCardNumber()).isEqualTo("1234********5678");
        assertThat(dto.getCardPassword()).isEqualTo("**");
        assertThat(dto.getExpirationDate()).isEqualTo("12/30");
        assertThat(dto.getBirthDate()).isEqualTo("**01**");
    }

    @Test
    void queryEntityReturnsDecryptedCopyAndWrapsDecryptFailure() {
        PaymentInfo info = PaymentInfo.builder().id(1L)
                .cardNumber("E1").cardPassword("E2").expirationDate("12/30").birthDate("E3").build();
        when(paymentInfoRepository.findByIdAndUser_Email(1L, "u@test.com")).thenReturn(Optional.of(info));
        when(paymentInfoRepository.findByIdAndUser_Email(2L, "u@test.com")).thenReturn(Optional.empty());
        when(encryptionService.decrypt("E1")).thenReturn("card");
        when(encryptionService.decrypt("E2")).thenReturn("pw");
        when(encryptionService.decrypt("E3")).thenReturn("birth");

        PaymentInfo decrypted = queryService().getPaymentInfoEntity("u@test.com", 1L);
        assertThat(decrypted).isNotSameAs(info);
        assertThat(decrypted.getCardNumber()).isEqualTo("card");
        assertThat(decrypted.getCardPassword()).isEqualTo("pw");
        assertThat(decrypted.getBirthDate()).isEqualTo("birth");
        assertThat(info.getCardNumber()).isEqualTo("E1");

        assertThatThrownBy(() -> queryService().getPaymentInfoEntity("u@test.com", 2L))
                .isInstanceOf(PaymentInfoNotFoundException.class);

        when(encryptionService.decrypt("E1")).thenThrow(new IllegalStateException("민감 정보 복호화 실패"));
        assertThatThrownBy(() -> queryService().getPaymentInfoEntity("u@test.com", 1L))
                .isInstanceOf(PaymentException.class)
                .extracting(e -> ((PaymentException) e).getErrorCode())
                .isEqualTo(PaymentErrorCode.PAYMENT_RETRIEVAL_FAILED);
    }

    @Test
    void encryptionRoundTripsDeterministicallyAndRejectsPlainText() {
        PaymentEncryptionService real = new PaymentEncryptionService();
        ReflectionTestUtils.setField(real, "secretKey", "test-secret");
        ReflectionTestUtils.setField(real, "salt", "test-salt");
        ReflectionTestUtils.setField(real, "iv", "0123456789abcdef");

        String encrypted = real.encrypt("1234567812345678");
        assertThat(encrypted).isNotEqualTo("1234567812345678");
        assertThat(real.encrypt("1234567812345678")).isEqualTo(encrypted);
        assertThat(real.decrypt(encrypted)).isEqualTo("1234567812345678");

        assertThatThrownBy(() -> real.decrypt("1234567812345678"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("민감 정보 복호화 실패");
    }
}
