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
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static api.store.diglog.common.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailVerificationService {

    @Value("${spring.mail.username}")
    private String senderEmail;

    private final JavaMailSender javaMailSender;
    private final EmailVerificationRepository emailVerificationRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void sendMail(String email) {
        if (memberRepository.findByEmail(email).isPresent()) {
            throw new CustomException(SIGNUP_MEMBER_EXISTS);
        }

        emailVerificationRepository.deleteAllByEmail(email);

        Random random = new Random();
        String code = String.format("%06d", random.nextInt(1000000));

        EmailVerification emailVerification = EmailVerification.builder()
                .email(email)
                .code(code)
                .build();

        createAndSendMail(email, code);

        emailVerificationRepository.save(emailVerification);
    }

    private void createAndSendMail(String email, String code) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            message.setFrom(senderEmail);
            message.setRecipients(MimeMessage.RecipientType.TO, email);
            message.setSubject("[DIGLOG] 회원가입 인증 메일");
            String body = "";
            body += "<h3>" + "진행중인 회원가입 페이지에서 아래의 6자리 코드를 입력해주세요." + "</h3>";
            body += "<h1>" + code + "</h1>";
            body += "<h3>" + "감사합니다." + "</h3>";
            message.setText(body, "UTF-8", "html");

            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new CustomException(SIGNUP_MAIL_SEND_FAILED);
        }
    }

    @Transactional
    public void checkCode(EmailVerificationRequest emailVerificationRequest) {
        String email = emailVerificationRequest.getEmail();
        String code = emailVerificationRequest.getCode();

        EmailVerification emailVerification = emailVerificationRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(SIGNUP_CODE_NOT_EXISTS));

        if (!emailVerification.getCode().equals(code)) {
            throw new CustomException(SIGNUP_CODE_NOT_MATCHED);
        }

        if (emailVerification.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now())) {
            throw new CustomException(SIGNUP_CODE_EXPIRED);
        }

        emailVerificationRepository.updateVerifiedTrue(email);
    }

    @Transactional
    public void verifyAndSignup(EmailVerificationSignupRequest signupRequest) {
        String email = signupRequest.getEmail();
        String code = signupRequest.getCode();

        EmailVerification emailVerification = emailVerificationRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(SIGNUP_CODE_NOT_EXISTS));

        if (!emailVerification.isVerified()) {
            throw new CustomException(SIGNUP_CODE_NOT_VERIFIED);
        }

        if (!emailVerification.getCreatedAt().plusMinutes(20).isAfter(LocalDateTime.now())) {
            throw new CustomException(SIGNUP_CODE_NOT_VERIFIED);
        }

        if (!emailVerification.getCode().equals(code)) {
            throw new CustomException(SIGNUP_CODE_NOT_MATCHED);
        }

        emailVerificationRepository.deleteAllByEmail(email);

        String username = generateUsernameByEmail(email);

        Member member = Member.builder()
                .email(email)
                .username(username)
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .roles(new HashSet<>(Set.of(Role.ROLE_USER)))
                .platform(Platform.SERVER)
                .build();

        memberRepository.save(member);
    }

    private String generateUsernameByEmail(String email) {
        String username = email.split("@")[0];

        if (memberRepository.countByUsername(username) > 0) {
            return username + "_" + UUID.randomUUID().toString().substring(0, 4);
        }

        return username;
    }
}
