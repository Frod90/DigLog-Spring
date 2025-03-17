package api.store.diglog.model.dto.emailVerification;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerificationSignupRequest {

    @NotBlank
    @Email(message = "이메일 형식에 맞게 입력해주세요.")
    private String email;

    @NotBlank
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,16}$", message = "영문, 숫자를 포함하여 8-16자리로 작성해주세요.")
    private String password;

    @NotBlank
    private String code;
}
