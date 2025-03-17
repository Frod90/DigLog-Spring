package api.store.diglog.model.dto.emailVerification;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerificationRequest {

    private String email;
    private String code;
}
