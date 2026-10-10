package com.fream.back.domain.address.service.command;

import com.fream.back.domain.address.dto.AddressCreateDto;
import com.fream.back.domain.address.dto.AddressUpdateDto;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * AddressCommandService 의 생성·수정·삭제에서 필드별 암호화 방식, 기본 배송지 해제,
 * 사용자 조회 키(평문 이메일), 예외 분기와 감싸기를 고정한다.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AddressCommandServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private PersonalDataEncryptionUtil encryptionUtil;

    @InjectMocks
    private AddressCommandService service;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("me@fream.com").build();
        when(encryptionUtil.deterministicEncrypt(anyString())).thenAnswer(inv -> "D(" + inv.getArgument(0) + ")");
        when(encryptionUtil.encrypt(anyString())).thenAnswer(inv -> "E(" + inv.getArgument(0) + ")");
    }

    private Address existing(long id, boolean isDefault) {
        Address a = Address.builder().id(id).recipientName("D(old)").phoneNumber("D(01000000000)")
                .zipCode("D(00000)").address("D(old addr)").detailedAddress("E(old detail)")
                .isDefault(isDefault).build();
        user.addAddress(a);
        return a;
    }

    private AddressCreateDto createDto(Boolean isDefault, String detail) {
        AddressCreateDto dto = new AddressCreateDto();
        dto.setRecipientName("홍길동");
        dto.setPhoneNumber("01012345678");
        dto.setZipCode("12345");
        dto.setAddress("서울시");
        dto.setDetailedAddress(detail);
        dto.setIsDefault(isDefault);
        return dto;
    }

    @Test
    void createAddress_looksUpUserByPlainEmail_andEncryptsFieldsByKind() {
        when(userRepository.findByEmail("me@fream.com")).thenReturn(Optional.of(user));

        service.createAddress("me@fream.com", createDto(null, "101호"));

        ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);
        verify(addressRepository).save(captor.capture());
        Address saved = captor.getValue();
        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.getRecipientName()).isEqualTo("D(홍길동)");
        assertThat(saved.getPhoneNumber()).isEqualTo("D(01012345678)");
        assertThat(saved.getZipCode()).isEqualTo("D(12345)");
        assertThat(saved.getAddress()).isEqualTo("D(서울시)");
        assertThat(saved.getDetailedAddress()).isEqualTo("E(101호)");
        assertThat(saved.isDefault()).isFalse();
        verify(encryptionUtil, never()).deterministicEncrypt("me@fream.com");
    }

    @Test
    void createAddress_default_clearsExistingDefaults_andNullDetailStaysNull() {
        Address oldDefault = existing(10L, true);
        when(userRepository.findByEmail("me@fream.com")).thenReturn(Optional.of(user));

        service.createAddress("me@fream.com", createDto(true, null));

        assertThat(oldDefault.isDefault()).isFalse();
        ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);
        verify(addressRepository).save(captor.capture());
        assertThat(captor.getValue().isDefault()).isTrue();
        assertThat(captor.getValue().getDetailedAddress()).isNull();
        verify(encryptionUtil, never()).encrypt(any());
        assertThat(user.getAddresses()).containsExactly(oldDefault);
    }

    @Test
    void createAddress_unknownUser_throwsUserNotFound_andRepositoryErrorIsWrapped() {
        when(userRepository.findByEmail("ghost@fream.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.createAddress("ghost@fream.com", createDto(false, null)))
                .isInstanceOf(AddressUserNotFoundException.class);

        when(userRepository.findByEmail("me@fream.com")).thenReturn(Optional.of(user));
        when(addressRepository.save(any(Address.class))).thenThrow(new RuntimeException("db"));
        assertThatThrownBy(() -> service.createAddress("me@fream.com", createDto(false, null)))
                .isInstanceOf(AddressException.class)
                .extracting(e -> ((AddressException) e).getErrorCode())
                .isEqualTo(AddressErrorCode.ADDRESS_CREATE_ERROR);
    }

    @Test
    void updateAddress_changesOnlyProvidedFields_withEncryption() {
        Address target = existing(10L, false);
        when(userRepository.findByEmail("me@fream.com")).thenReturn(Optional.of(user));
        when(addressRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(target));
        AddressUpdateDto dto = new AddressUpdateDto();
        dto.setAddressId(10L);
        dto.setRecipientName("김철수");
        dto.setDetailedAddress("202호");

        service.updateAddress("me@fream.com", dto);

        assertThat(target.getRecipientName()).isEqualTo("D(김철수)");
        assertThat(target.getDetailedAddress()).isEqualTo("E(202호)");
        assertThat(target.getPhoneNumber()).isEqualTo("D(01000000000)");
        assertThat(target.getZipCode()).isEqualTo("D(00000)");
        assertThat(target.getAddress()).isEqualTo("D(old addr)");
        assertThat(target.isDefault()).isFalse();
        verify(addressRepository, never()).save(any());
    }

    @Test
    void updateAddress_setDefault_clearsOthers_andMarksTarget() {
        Address oldDefault = existing(10L, true);
        Address target = existing(11L, false);
        when(userRepository.findByEmail("me@fream.com")).thenReturn(Optional.of(user));
        when(addressRepository.findByIdAndUser(11L, user)).thenReturn(Optional.of(target));
        AddressUpdateDto dto = new AddressUpdateDto();
        dto.setAddressId(11L);
        dto.setIsDefault(true);

        service.updateAddress("me@fream.com", dto);

        assertThat(oldDefault.isDefault()).isFalse();
        assertThat(target.isDefault()).isTrue();
    }

    @Test
    void updateAddress_otherUsersAddress_throwsAddressNotFound() {
        when(userRepository.findByEmail("me@fream.com")).thenReturn(Optional.of(user));
        when(addressRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());
        AddressUpdateDto dto = new AddressUpdateDto();
        dto.setAddressId(99L);

        assertThatThrownBy(() -> service.updateAddress("me@fream.com", dto))
                .isInstanceOf(AddressNotFoundException.class)
                .hasMessage("주소 ID '99'에 해당하는 주소를 찾을 수 없습니다.");
    }

    @Test
    void deleteAddress_deletesOwned_orThrowsNotFound() {
        Address target = existing(10L, false);
        when(userRepository.findByEmail("me@fream.com")).thenReturn(Optional.of(user));
        when(addressRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(target));
        when(addressRepository.findByIdAndUser(11L, user)).thenReturn(Optional.empty());

        service.deleteAddress("me@fream.com", 10L);
        verify(addressRepository).delete(target);

        assertThatThrownBy(() -> service.deleteAddress("me@fream.com", 11L))
                .isInstanceOf(AddressNotFoundException.class)
                .extracting(e -> ((AddressException) e).getErrorCode())
                .isEqualTo(AddressErrorCode.ADDRESS_NOT_FOUND);
    }
}
