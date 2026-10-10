package com.fream.back.domain.payment;

import com.fream.back.domain.payment.dto.CardPaymentRequestDto;
import com.fream.back.domain.payment.dto.GeneralPaymentRequestDto;
import com.fream.back.domain.payment.dto.paymentInfo.PaymentInfoCreateDto;
import com.fream.back.domain.payment.exception.PaymentException;
import com.fream.back.domain.payment.util.PaymentCardUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 카드 정보 마스킹·검증 유틸과 결제 요청 DTO의 결제 유형 판별 결과를 고정한다.
 */
class PaymentCardUtilsTest {

    @Test
    void maskCardNumberKeepsFirstAndLastFour() {
        assertThat(PaymentCardUtils.maskCardNumber("1234567812345678")).isEqualTo("1234********5678");
        assertThat(PaymentCardUtils.maskCardNumber("123456781234567")).isEqualTo("1234*******4567");
        assertThat(PaymentCardUtils.maskCardNumber("12345678")).isEqualTo("12345678");
        assertThat(PaymentCardUtils.maskCardNumber("1234567")).isEqualTo("Invalid");
        assertThat(PaymentCardUtils.maskCardNumber(null)).isEqualTo("Invalid");
        assertThat(PaymentCardUtils.maskCardNumberForLogging("1234567812345678")).isEqualTo("1234********5678");
    }

    @Test
    void maskBirthDateAndPassword() {
        assertThat(PaymentCardUtils.maskBirthDate("900101")).isEqualTo("**01**");
        assertThat(PaymentCardUtils.maskBirthDate("19900101")).isEqualTo("**90**");
        assertThat(PaymentCardUtils.maskBirthDate("123")).isEqualTo("******");
        assertThat(PaymentCardUtils.maskBirthDate(null)).isEqualTo("******");
        assertThat(PaymentCardUtils.maskCardPassword("99")).isEqualTo("**");
        assertThat(PaymentCardUtils.maskCardPassword(null)).isEqualTo("**");
    }

    @Test
    void validateCardInfoChecksFormatAndExpiry() {
        assertThatCode(() -> PaymentCardUtils.validateCardInfo(
                new PaymentInfoCreateDto("1234567812345678", "12", "12/99", "900101")))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> PaymentCardUtils.validateCardInfo(
                new PaymentInfoCreateDto("1234567812345678", "12", "01/20", "900101")))
                .isInstanceOf(PaymentException.class)
                .hasMessage("만료된 카드입니다.");
        assertThatThrownBy(() -> PaymentCardUtils.validateCardInfo(
                new PaymentInfoCreateDto("1234567812345678", "1", "12/99", "900101")))
                .hasMessage("카드 비밀번호는 앞 2자리 숫자여야 합니다.");
        assertThatThrownBy(() -> PaymentCardUtils.validateCardInfo(
                new PaymentInfoCreateDto("1234567812345678", "12", "12/99", "19900101")))
                .hasMessage("생년월일은 YYMMDD 형식의 6자리 숫자여야 합니다.");
        assertThatThrownBy(() -> PaymentCardUtils.validateCardInfo(null))
                .hasMessage("결제 정보가 없습니다.");
    }

    @Test
    void resolvedPaymentTypeComesFromSubclassNotFromPaymentTypeField() {
        CardPaymentRequestDto card = CardPaymentRequestDto.builder().paymentType("GENERAL").build();
        assertThat(card.getResolvedPaymentType()).isEqualTo("CARD");

        GeneralPaymentRequestDto general = GeneralPaymentRequestDto.builder().build();
        assertThat(general.getResolvedPaymentType()).isEqualTo("GENERAL");

        GeneralPaymentRequestDto preset = GeneralPaymentRequestDto.builder().resolvedType("ACCOUNT").build();
        assertThat(preset.getResolvedPaymentType()).isEqualTo("ACCOUNT");
    }
}
