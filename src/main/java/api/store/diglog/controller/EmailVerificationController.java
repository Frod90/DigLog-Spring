package api.store.diglog.controller;

import api.store.diglog.model.dto.emailVerification.EmailVerificationCodeRequest;
import api.store.diglog.model.dto.emailVerification.EmailVerificationRequest;
import api.store.diglog.model.dto.emailVerification.EmailVerificationSignupRequest;
import api.store.diglog.service.EmailVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/verify")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @PostMapping
    public ResponseEntity<Void> sendMail(@RequestBody @Valid EmailVerificationCodeRequest emailVerificationCodeRequest) {
        emailVerificationService.sendMail(emailVerificationCodeRequest.getEmail());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/code")
    public ResponseEntity<Void> checkCode(@RequestBody EmailVerificationRequest emailVerificationRequest) {
        emailVerificationService.checkCode(emailVerificationRequest);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/signup")
    public ResponseEntity<Void> verifyAndSignup(@RequestBody @Valid EmailVerificationSignupRequest signupRequest) {
        emailVerificationService.verifyAndSignup(signupRequest);

        return ResponseEntity.ok().build();
    }
}
