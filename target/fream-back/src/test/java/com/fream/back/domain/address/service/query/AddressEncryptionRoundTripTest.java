package com.fream.back.domain.address.service.query;

import com.fream.back.domain.address.entity.Address;
import com.fream.back.domain.address.exception.AddressErrorCode;
import com.fream.back.domain.address.exception.AddressException;
import com.fream.back.domain.address.repository.AddressRepository;
import com.fream.back.domain.user.entity.User;
import com.fream.back.domain.user.repository.UserRepository;
import com.fream.back.global.utils.PersonalDataEncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * 실제 PersonalDataEncryptionUtil 로 주소 필드를 암호화했을 때의 검색 키 일치와
 * 조회 시 복호화 결과(현재 동작)를 고정한다.
 */
@ExtendWith(MockitoExtension.class)
class AddressEncryptionRoundTripTest {

    private static final String SECRET = "0123456789abcdef";
    private static final String IV = "fedcba9876543210";

    @Mock private UserRepository userRepository;
    @Mock private AddressRepository addressRepository;

    private PersonalDataEncryptionUtil util;
    private AddressQueryService service;

    @BeforeEach
    void setUp() {
        util = new PersonalDataEncryptionUtil();
        ReflectionTestUtils.setField(util, "secretKey", SECRET);
        ReflectionTestUtils.setField(util, "iv", IV);
        service = new AddressQueryService(userRepository, addressRepository, util);
    }

    @Test
    void deterministicEncrypt_isStable_andEqualsSearchEncryption_whileEncryptRoundTrips() {
        String a = util.deterministicEncrypt("홍길동");

        assertThat(util.deterministicEncrypt("홍길동")).isEqualTo(a);
        assertThat(util.encryptForSearch("홍길동")).isEqualTo(a);
        assertThat(util.deterministicEncrypt("김철수")).isNotEqualTo(a);
        assertThat(util.deterministicEncrypt(null)).isNull();
        assertThat(util.deterministicEncrypt("")).isEmpty();

        assertThat(util.decrypt(util.encrypt("101호"))).isEqualTo("101호");
    }

    @Test
    void deterministicDecrypt_doesNotRecoverPlainText_ofDeterministicEncrypt() {
        String encrypted = util.deterministicEncrypt("홍길동");

        assertThatThrownBy(() -> util.deterministicDecrypt(encrypted))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("결정적 복호화 처리 중 오류가 발생했습니다");
    }

    @Test
    void getAddresses_withRealEncryptedRows_failsWithQueryError() {
        User user = User.builder().id(1L).email(util.deterministicEncrypt("me@fream.com")).build();
        user.addAddress(Address.builder().id(1L)
                .recipientName(util.deterministicEncrypt("홍길동"))
                .phoneNumber(util.deterministicEncrypt("01012345678"))
                .zipCode(util.deterministicEncrypt("12345"))
                .address(util.deterministicEncrypt("서울시"))
                .detailedAddress(util.encrypt("101호"))
                .build());
        when(userRepository.findByEmail(util.deterministicEncrypt("me@fream.com"))).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.getAddresses("me@fream.com"))
                .isInstanceOf(AddressException.class)
                .extracting(e -> ((AddressException) e).getErrorCode())
                .isEqualTo(AddressErrorCode.ADDRESS_QUERY_ERROR);
    }

    @Test
    void getAddresses_withNoAddresses_returnsEmptyList() {
        User user = User.builder().id(1L).email(util.deterministicEncrypt("me@fream.com")).build();
        when(userRepository.findByEmail(util.deterministicEncrypt("me@fream.com"))).thenReturn(Optional.of(user));

        List<?> result = service.getAddresses("me@fream.com");

        assertThat(result).isEmpty();
    }
}
