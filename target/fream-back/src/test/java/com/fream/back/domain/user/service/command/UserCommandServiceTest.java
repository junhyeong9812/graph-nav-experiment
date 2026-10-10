package com.fream.back.domain.user.service.command;

import com.fream.back.domain.notification.service.command.NotificationCommandService;
import com.fream.back.domain.user.dto.UserRegistrationDto;
import com.fream.back.domain.user.dto.VerifiedCustomerDto;
import com.fream.back.domain.user.entity.Gender;
import com.fream.back.domain.user.entity.Profile;
import com.fream.back.domain.user.entity.ShoeSize;
import com.fream.back.domain.user.entity.User;
import com.fream.back.domain.user.exception.*;
import com.fream.back.domain.user.repository.UserRepository;
import com.fream.back.domain.user.service.profile.ProfileCommandService;
import com.fream.back.global.utils.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * UserCommandService 회원가입(동의·중복·본인인증·연령·추천인 분기와 생성 값)과 계정 삭제 흐름을 고정한다.
 */
@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ProfileCommandService profileService;
    @Mock private FileUtils fileUtils;
    @Mock private NotificationCommandService notificationCommandService;
    @Mock private IdentityVerificationService identityVerificationService;

    @InjectMocks
    private UserCommandService userCommandService;

    private UserRegistrationDto.UserRegistrationDtoBuilder validDto() {
        return UserRegistrationDto.builder()
                .email("new@fream.com")
                .password("raw-pw")
                .identityVerificationId("iv-1")
                .shoeSize(ShoeSize.SIZE_260)
                .isOver14(true)
                .termsAgreement(true)
                .privacyAgreement(true)
                .optionalPrivacyAgreement(true)
                .adConsent(false);
    }

    private VerifiedCustomerDto verified(String birthDate, String gender, String ci) {
        return VerifiedCustomerDto.builder()
                .phoneNumber("01099998888")
                .birthDate(birthDate)
                .gender(gender)
                .ci(ci)
                .di("di-1")
                .build();
    }

    private String adultBirth() {
        return LocalDate.now().minusYears(20).toString();
    }

    @Test
    void register_preVerificationGuards_consentDuplicateEmailAndMissingVerificationId() {
        assertThatThrownBy(() -> userCommandService.registerUser(validDto().privacyAgreement(false).build()))
                .isInstanceOf(UserException.class)
                .hasMessage("필수 동의 조건을 만족하지 않았습니다.")
                .extracting(e -> ((UserException) e).getErrorCode())
                .isEqualTo(UserErrorCode.INVALID_VERIFICATION_CODE);
        verifyNoInteractions(userRepository);

        when(userRepository.existsByEmail("new@fream.com")).thenReturn(true, false);

        assertThatThrownBy(() -> userCommandService.registerUser(validDto().build()))
                .isInstanceOf(DuplicateEmailException.class);
        assertThatThrownBy(() -> userCommandService.registerUser(validDto().identityVerificationId("").build()))
                .isInstanceOf(IdentityVerificationFailedException.class);
        verifyNoInteractions(identityVerificationService);
    }

    @Test
    void register_verificationServiceFailure_andDuplicateCi_areRejected() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(identityVerificationService.verifyIdentity("iv-1"))
                .thenThrow(new RuntimeException("portone down"))
                .thenReturn(verified("1990-01-01", "M", "ci-dup"));
        when(userRepository.findByCi("ci-dup"))
                .thenReturn(Optional.of(User.builder().email("old@fream.com").build()));

        assertThatThrownBy(() -> userCommandService.registerUser(validDto().build()))
                .isInstanceOf(IdentityVerificationFailedException.class)
                .hasMessage("본인인증 검증에 실패했습니다.");
        assertThatThrownBy(() -> userCommandService.registerUser(validDto().build()))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("이미 가입된 사용자입니다. 다른 계정으로 로그인해주세요.");
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_under14ByBirthDate_andUnknownReferralCode_areRejected() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(identityVerificationService.verifyIdentity("iv-1"))
                .thenReturn(verified(LocalDate.now().minusYears(10).toString(), "F", null))
                .thenReturn(verified(adultBirth(), "M", null));
        when(userRepository.findByReferralCode("NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userCommandService.registerUser(validDto().referralCode("NOPE").build()))
                .isInstanceOf(UserException.class)
                .hasMessage("만 14세 이상만 가입 가능합니다.");
        verify(userRepository, never()).findByReferralCode(anyString());

        assertThatThrownBy(() -> userCommandService.registerUser(validDto().referralCode("NOPE").build()))
                .isInstanceOf(UserException.class)
                .hasMessage("유효하지 않은 추천인 코드입니다.")
                .extracting(e -> ((UserException) e).getErrorCode())
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    void register_success_buildsVerifiedUser_andCreatesDefaultProfile() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        String birth = LocalDate.now().minusYears(30).minusDays(1).toString();
        when(identityVerificationService.verifyIdentity("iv-1")).thenReturn(verified(birth, "female", "ci-new"));
        when(userRepository.findByCi("ci-new")).thenReturn(Optional.empty());
        when(userRepository.findByReferralCode(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("raw-pw")).thenReturn("ENC(raw-pw)");

        User user = userCommandService.registerUser(validDto().build());

        assertThat(user.getEmail()).isEqualTo("new@fream.com");
        assertThat(user.getPassword()).isEqualTo("ENC(raw-pw)");
        assertThat(user.getPhoneNumber()).isEqualTo("01099998888");
        assertThat(user.getGender()).isEqualTo(Gender.FEMALE);
        assertThat(user.getAge()).isEqualTo(30);
        assertThat(user.isVerified()).isTrue();
        assertThat(user.getCi()).isEqualTo("ci-new");
        assertThat(user.getReferralCode()).matches("[0-9A-F]{8}");
        assertThat(user.isPhoneNotificationConsent()).isFalse();
        assertThat(user.isEmailNotificationConsent()).isTrue();
        assertThat(user.isOptionalPrivacyAgreement()).isTrue();
        verify(userRepository).save(user);
        verify(profileService).createDefaultProfile(user);
    }

    @Test
    void register_withReferrer_replacesOwnReferralCodeWithReferrersCode() {
        User referrer = User.builder().email("ref@fream.com").referralCode("REFCODE1").build();
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(identityVerificationService.verifyIdentity("iv-1")).thenReturn(verified(adultBirth(), "X", null));
        when(userRepository.findByReferralCode("REFCODE1")).thenReturn(Optional.of(referrer));
        when(userRepository.findByReferralCode(argThat(s -> s != null && !s.equals("REFCODE1"))))
                .thenReturn(Optional.empty());

        User user = userCommandService.registerUser(validDto().referralCode("REFCODE1").adConsent(true).build());

        assertThat(user.getReferrer()).isSameAs(referrer);
        assertThat(user.getReferralCode()).isEqualTo("REFCODE1");
        assertThat(referrer.getReferredUsers()).containsExactly(user);
        assertThat(user.getGender()).isNull();
        assertThat(user.isPhoneNotificationConsent()).isTrue();
    }

    @Test
    void register_profileCreationFailure_isRethrownAsProfileNotFound() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(identityVerificationService.verifyIdentity("iv-1")).thenReturn(verified(adultBirth(), "M", null));
        when(userRepository.findByReferralCode(anyString())).thenReturn(Optional.empty());
        doThrow(new RuntimeException("x")).when(profileService).createDefaultProfile(any());

        assertThatThrownBy(() -> userCommandService.registerUser(validDto().build()))
                .isInstanceOf(ProfileNotFoundException.class)
                .hasMessage("기본 프로필 생성에 실패했습니다.");
    }

    @Test
    void deleteAccount_unknownUserThrows_otherwiseToleratesSideEffectFailures() {
        when(userRepository.findByEmail("none@fream.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userCommandService.deleteAccount("none@fream.com"))
                .isInstanceOf(UserNotFoundException.class);
        verifyNoInteractions(notificationCommandService);

        User user = User.builder().id(7L).email("bye@fream.com").build();
        user.addProfile(Profile.builder().profileName("p").profileImageUrl("img.jpg").build());
        when(userRepository.findByEmail("bye@fream.com")).thenReturn(Optional.of(user));
        doThrow(new RuntimeException("n")).when(notificationCommandService).deleteNotificationsByUser("bye@fream.com");
        doThrow(new RuntimeException("f")).when(fileUtils).deleteFile("profile_images", "img.jpg");

        userCommandService.deleteAccount("bye@fream.com");

        verify(userRepository).delete(user);
    }
}
