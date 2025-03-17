package api.store.diglog.service;

import api.store.diglog.common.exception.CustomException;
import api.store.diglog.model.constant.Platform;
import api.store.diglog.model.constant.Role;
import api.store.diglog.model.dto.emailVerification.EmailVerificationRequest;
import api.store.diglog.model.dto.emailVerification.EmailVerificationSignupRequest;
import api.store.diglog.model.entity.EmailVerification;
import api.store.diglog.model.entity.Member;
import api.store.diglog.repository.EmailVerificationRepository;
import api.store.diglog.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private EmailVerificationRepository emailVerificationRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmailVerificationService emailVerificationService;


    @Nested
    class CheckCodeTest {

        private static final String EMAIL = "test@example.com";
        private static final String CODE = "012345";
        private static final LocalDateTime CREATED_AT = LocalDateTime.now().minusMinutes(9);

        private static final String INVALID_EMAIL = "test2@example.com";
        private static final String INVALID_CODE = "567890";
        private static final LocalDateTime INVALID_CREATED_AT = LocalDateTime.now().minusMinutes(11);

        @Test
        @DisplayName("코드를 인증하면 verified = true로 설정한다.")
        void success() {
            // given
            EmailVerificationRequest dto = EmailVerificationRequest.builder()
                    .email(EMAIL)
                    .code(CODE)
                    .build();

            EmailVerification updatedEmailVerification = getEmailVerification(EMAIL, CODE, true, CREATED_AT);

            when(emailVerificationRepository.findByEmail(EMAIL)).thenReturn(Optional.of(updatedEmailVerification));

            // when
            Throwable throwable = catchThrowable(() -> emailVerificationService.checkCode(dto));

            // then
            assertThat(throwable).isNull();
            assertThat(emailVerificationRepository.findByEmail(EMAIL).get().isVerified()).isTrue();
        }

        @ParameterizedTest
        @MethodSource("provideCheckCode")
        @DisplayName("코드 인증에 문제가 있는 경우 에러를 띄운다.")
        void fail(LocalDateTime createdAt, String testEmail, String testCode, Class<?> exceptionClass) {
            // given
            EmailVerification emailVerification = getEmailVerification(EMAIL, CODE, false, createdAt);

            EmailVerificationRequest dto = EmailVerificationRequest.builder()
                    .email(testEmail)
                    .code(testCode)
                    .build();

            lenient().when(emailVerificationRepository.findByEmail(EMAIL)).thenReturn(Optional.of(emailVerification));
            lenient().when(emailVerificationRepository.findByEmail(INVALID_EMAIL)).thenReturn(Optional.empty());

            // when
            Throwable throwable = catchThrowable(() -> emailVerificationService.checkCode(dto));

            // then
            assertThat(throwable).isInstanceOf(exceptionClass);
        }

        static Stream<Arguments> provideCheckCode() {
            return Stream.of(
                    Arguments.of(INVALID_CREATED_AT, EMAIL, CODE, CustomException.class),
                    Arguments.of(CREATED_AT, INVALID_EMAIL, CODE, CustomException.class),
                    Arguments.of(CREATED_AT, EMAIL, INVALID_CODE, CustomException.class)
            );
        }
    }

    @Nested
    class verifyAndSignupTest {
        private static final String EMAIL = "test@example.com";
        private static final String CODE = "012345";
        private static final LocalDateTime CREATED_AT = LocalDateTime.now().minusMinutes(19);
        private static final String PASSWORD = "qwer1234";

        private static final String INVALID_EMAIL = "test2@example.com";
        private static final String INVALID_CODE = "567890";
        private static final LocalDateTime INVALID_CREATED_AT = LocalDateTime.now().minusMinutes(21);

        @Test
        @DisplayName("유효한 인증 코드, 이메일, 비밀번호를 입력하면 회원가입이 된다.")
        void success() {
            // given
            LocalDateTime createdAt = LocalDateTime.now().minusMinutes(19);

            EmailVerificationSignupRequest dto = EmailVerificationSignupRequest.builder()
                    .email(EMAIL)
                    .password(PASSWORD)
                    .code(CODE)
                    .build();

            EmailVerification emailVerification = getEmailVerification(EMAIL, CODE, true, createdAt);

            Member member = Member.builder()
                    .email(EMAIL)
                    .username(EMAIL.split("@")[0])
                    .password(PASSWORD)
                    .roles(Set.of(Role.ROLE_USER))
                    .platform(Platform.SERVER)
                    .build();

            when(passwordEncoder.encode(PASSWORD)).thenReturn(PASSWORD);
            when(emailVerificationRepository.findByEmail(EMAIL)).thenReturn(Optional.of(emailVerification));
            when(memberRepository.findByEmail(EMAIL)).thenReturn(Optional.of(member));

            // when
            Throwable throwable = catchThrowable(() -> emailVerificationService.verifyAndSignup(dto));

            // then
            assertThat(throwable).isNull();
            assertThat(memberRepository.findByEmail(EMAIL).get().getEmail()).isEqualTo(EMAIL);
        }

        @ParameterizedTest
        @MethodSource("provideVerifyAndSignupTest")
        @DisplayName("회원가입에 문제가 있는 경우 에러를 띄운다.")
        void fail(boolean isVerified, LocalDateTime createdAt, String testEmail, String testCode, Class<?> exceptionClass) {
            // given
            EmailVerification emailVerification = getEmailVerification(EMAIL, CODE, isVerified, createdAt);

            EmailVerificationSignupRequest dto = EmailVerificationSignupRequest.builder()
                    .email(testEmail)
                    .password(PASSWORD)
                    .code(testCode)
                    .build();

            lenient().when(emailVerificationRepository.findByEmail(EMAIL)).thenReturn(Optional.of(emailVerification));
            lenient().when(emailVerificationRepository.findByEmail(INVALID_EMAIL)).thenReturn(Optional.empty());

            // when
            Throwable throwable = catchThrowable(() -> emailVerificationService.verifyAndSignup(dto));

            // then
            assertThat(throwable).isInstanceOf(exceptionClass);
        }

        private static Stream<Arguments> provideVerifyAndSignupTest() {
            return Stream.of(
                    Arguments.of(false, INVALID_CREATED_AT, EMAIL, CODE, CustomException.class),
                    Arguments.of(true, INVALID_CREATED_AT, EMAIL, CODE, CustomException.class),
                    Arguments.of(true, CREATED_AT, INVALID_EMAIL, CODE, CustomException.class),
                    Arguments.of(true, CREATED_AT, EMAIL, INVALID_CODE, CustomException.class)
            );
        }
    }


    private EmailVerification getEmailVerification(String email, String code, boolean isVerified, LocalDateTime createdAt) {
        return EmailVerification.builder()
                .email(email)
                .code(code)
                .verified(isVerified)
                .createdAt(createdAt)
                .build();
    }
}