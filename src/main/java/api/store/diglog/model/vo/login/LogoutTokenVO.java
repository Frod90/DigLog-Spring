package api.store.diglog.model.vo.login;

import jakarta.servlet.http.Cookie;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LogoutTokenVO {

    private Cookie logoutCookie;
}
