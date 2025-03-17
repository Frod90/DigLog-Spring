package api.store.diglog.controller;

import api.store.diglog.model.dto.login.LoginRequest;
import api.store.diglog.model.dto.login.LogoutRequest;
import api.store.diglog.model.dto.member.MemberInfoResponse;
import api.store.diglog.model.vo.login.LoginTokenVO;
import api.store.diglog.model.vo.login.LogoutTokenVO;
import api.store.diglog.model.vo.login.RenewRefreshTokenVO;
import api.store.diglog.model.vo.login.TokenVO;
import api.store.diglog.service.MemberService;
import api.store.diglog.service.RefreshService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class LoginController {

    private final MemberService memberService;
    private final RefreshService refreshService;

    @PostMapping("/login")
    public ResponseEntity<MemberInfoResponse> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {
        LoginTokenVO loginTokenVO = memberService.login(loginRequest);

        response.addHeader("Authorization", "Bearer " + loginTokenVO.getAccessToken());
        response.addCookie(loginTokenVO.getRefreshTokenCookie());

        return ResponseEntity.ok().body(loginTokenVO.getMemberInfoResponse());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody LogoutRequest logoutRequest,
            HttpServletResponse response) {
        LogoutTokenVO logoutTokenVO = memberService.logout(logoutRequest);

        response.addCookie(logoutTokenVO.getLogoutCookie());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/refresh")
    public ResponseEntity<MemberInfoResponse> refresh(
            @CookieValue(value = "refreshToken", defaultValue = "")
            String refreshToken,
            HttpServletResponse response) {
        RenewRefreshTokenVO renewRefreshTokenVO = refreshService.renewRefresh(refreshToken);

        TokenVO tokenVO = renewRefreshTokenVO.getTokenVO();
        if (tokenVO != null) {
            response.addHeader("Authorization", "Bearer " + tokenVO.getAccessToken());
            if (tokenVO.getRefreshTokenCookie() != null) {
                response.addCookie(tokenVO.getRefreshTokenCookie());
            }
        }

        return ResponseEntity.ok().body(renewRefreshTokenVO.getMemberInfoResponse());
    }
}
