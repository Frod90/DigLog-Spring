package api.store.diglog.model.dto.login;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class LogoutRequest {

    private String email;
}
