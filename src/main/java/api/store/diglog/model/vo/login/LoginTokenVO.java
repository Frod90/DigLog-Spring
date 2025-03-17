package api.store.diglog.model.vo.login;

import api.store.diglog.model.dto.member.MemberInfoResponse;
import jakarta.servlet.http.Cookie;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginTokenVO {

    private String accessToken;
    private Cookie refreshTokenCookie;
    private MemberInfoResponse memberInfoResponse;
}