package com.fream.back.domain.address.service.query;

import com.fream.back.domain.address.dto.AddressResponseDto;
import com.fream.back.domain.address.entity.Address;
import com.fream.back.domain.address.exception.AddressErrorCode;
import com.fream.back.domain.address.exception.AddressException;
import com.fream.back.domain.address.exception.AddressNotFoundException;
import com.fream.back.domain.address.exception.AddressUserNotFoundException;
import com.fream.back.domain.address.repository.AddressRepository;
import com.fream.back.domain.user.entity.User;
import com.fream.back.domain.user.repository.UserRepository;
import com.fream.back.global.utils.PersonalDataEncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * AddressQueryService 의 사용자 조회 키(암호화 이메일), 필드별 복호화, 검색어 암호화와 예외 분기를 고정한다.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AddressQueryServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private PersonalDataEncryptionUtil encryptionUtil;

    @InjectMocks
    private AddressQueryService service;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("D(me@fream.com)").build();
        when(encryptionUtil.deterministicEncrypt(anyString())).thenAnswer(inv -> "D(" + inv.getArgument(0) + ")");
        when(encryptionUtil.encryptForSearch(anyString())).thenAnswer(inv -> "D(" + inv.getArgument(0) + ")");
        when(encryptionUtil.deterministicDecrypt(anyString())).thenAnswer(inv -> strip(inv.getArgument(0), "D("));
        when(encryptionUtil.decrypt(anyString())).thenAnswer(inv -> strip(inv.getArgument(0), "E("));
    }

    private static String strip(String s, String prefix) {
        return s.startsWith(prefix) ? s.substring(prefix.length(), s.length() - 1) : "?" + s;
    }

    private Address address(long id, String name, String detail, boolean isDefault) {
        Address a = Address.builder().id(id).recipientName("D(" + name + ")").phoneNumber("D(01012345678)")
                .zipCode("D(12345)").address("D(서울시)").detailedAddress(detail == null ? null : "E(" + detail + ")")
                .isDefault(isDefault).build();
        user.addAddress(a);
        return a;
    }

    @Test
    void getAddresses_findsUserByEncryptedEmail_andDecryptsEachField() {
        address(1L, "홍길동", "101호", true);
        address(2L, "김철수", null, false);
        when(userRepository.findByEmail("D(me@fream.com)")).thenReturn(Optional.of(user));

        List<AddressResponseDto> result = service.getAddresses("me@fream.com");

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo(new AddressResponseDto(1L, "홍길동", "01012345678", "12345", "서울시", "101호", true));
        assertThat(result.get(1).getDetailedAddress()).isNull();
        assertThat(result.get(1).getIsDefault()).isFalse();
        verify(userRepository).findByEmail("D(me@fream.com)");
        verify(userRepository, never()).findByEmail("me@fream.com");
    }

    @Test
    void getAddresses_userMissing_throwsAddressUserNotFound_andDecryptFailureIsWrapped() {
        when(userRepository.findByEmail("D(ghost@fream.com)")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getAddresses("ghost@fream.com"))
                .isInstanceOf(AddressUserNotFoundException.class);

        address(1L, "홍길동", null, false);
        when(userRepository.findByEmail("D(me@fream.com)")).thenReturn(Optional.of(user));
        when(encryptionUtil.deterministicDecrypt(anyString())).thenThrow(new RuntimeException("bad key"));
        assertThatThrownBy(() -> service.getAddresses("me@fream.com"))
                .isInstanceOf(AddressException.class)
                .extracting(e -> ((AddressException) e).getErrorCode())
                .isEqualTo(AddressErrorCode.ADDRESS_QUERY_ERROR);
    }

    @Test
    void getAddress_returnsOwnedAddress_orThrowsNotFound() {
        Address a = address(5L, "홍길동", "101호", false);
        when(userRepository.findByEmail("D(me@fream.com)")).thenReturn(Optional.of(user));
        when(addressRepository.findByIdAndUser(5L, user)).thenReturn(Optional.of(a));
        when(addressRepository.findByIdAndUser(6L, user)).thenReturn(Optional.empty());

        AddressResponseDto dto = service.getAddress("me@fream.com", 5L);
        assertThat(dto.getRecipientName()).isEqualTo("홍길동");
        assertThat(dto.getDetailedAddress()).isEqualTo("101호");

        assertThatThrownBy(() -> service.getAddress("me@fream.com", 6L))
                .isInstanceOf(AddressNotFoundException.class);
    }

    @Test
    void searchByRecipientNameAndPhone_passEncryptedSearchTermToRepository() {
        Address a = address(5L, "홍길동", null, false);
        when(userRepository.findByEmail("D(me@fream.com)")).thenReturn(Optional.of(user));
        when(addressRepository.findByUserAndRecipientName(user, "D(홍길동)")).thenReturn(List.of(a));
        when(addressRepository.findByUserAndPhoneNumber(user, "D(01012345678)")).thenReturn(List.of(a));

        assertThat(service.searchAddressesByRecipientName("me@fream.com", "홍길동"))
                .extracting(AddressResponseDto::getId).containsExactly(5L);
        assertThat(service.searchAddressesByPhoneNumber("me@fream.com", "01012345678"))
                .extracting(AddressResponseDto::getRecipientName).containsExactly("홍길동");

        verify(encryptionUtil).encryptForSearch("홍길동");
        verify(encryptionUtil).encryptForSearch("01012345678");
    }
}
