package api.store.diglog.model.vo.login;

import jakarta.servlet.http.Cookie;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class TokenVO {

    private String email;
    private String username;
    private Set<String> roles;
    private String accessToken;
    private Cookie refreshTokenCookie;
}
