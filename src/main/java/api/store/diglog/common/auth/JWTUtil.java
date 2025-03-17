package api.store.diglog.common.auth;

import api.store.diglog.model.constant.Role;
import api.store.diglog.model.entity.Member;
import api.store.diglog.model.vo.member.MemberInfoVO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JWTUtil implements InitializingBean {

    @Value("${jwt.key}")
    private String key;
    @Value("${jwt.domain}")
    private String domain;
    @Value("${jwt.expire.access}")
    private int accessSeconds;
    @Value("${jwt.expire.refresh}")
    private int refreshSeconds;
    @Value("${jwt.expire.renew}")
    private int refreshRenewSeconds;
    @Value("${spring.profiles.default}")
    private String envMode;
    private SecretKey secretKey;
    private final String AUTHORITIES_KEY = "auth";

    @Override
    public void afterPropertiesSet() {
        this.secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS512.key().build().getAlgorithm());
    }

    public String generateAccessToken(Member member) {
        return generateToken(member, "accessToken");
    }

    private String generateToken(String email, String authorities, String type) {
        long now = (new Date()).getTime();
        Date validity;
        if (type.equals("accessToken")) {
            validity = new Date(now + this.accessSeconds * 1000L);
        } else if (type.equals("refreshToken")) {
            validity = new Date(now + this.refreshSeconds * 1000L);
        } else {
            validity = new Date(now);
        }

        return Jwts.builder()
                .subject(email)
                .claim(AUTHORITIES_KEY, authorities)
                .signWith(secretKey)
                .expiration(validity)
                .compact();
    }

    private String generateToken(Member member, String type) {
        String email = member.getEmail();
        String authorities = member.getRoles().stream()
                .map(Role::getRole)
                .collect(Collectors.joining(","));

        return generateToken(email, authorities, type);
    }

    public Cookie generateRefreshCookie(String email, String authorities) {
        String type = "refreshToken";
        String refreshToken = generateToken(email, authorities, type);

        return generateCookie(type, refreshToken, refreshSeconds);
    }

    public Cookie generateRefreshCookie(Member member) {
        String type = "refreshToken";
        String refreshToken = generateToken(member, type);

        return generateCookie(type, refreshToken, refreshSeconds);
    }

    public Cookie generateLogoutCookie() {
        return generateCookie("refreshToken", "", 0);
    }

    private Cookie generateCookie(String type, String value, int maxAge) {
        Cookie cookie = new Cookie(type, value);
        cookie.setDomain(domain);
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure((envMode.equals("prod")));

        return cookie;
    }

    // 유효 토큰 확인
    public boolean validateToken(String authorization) {
        if (authorization == null || authorization.length() < 7) {
            return false;
        }

        String accessToken = getAccessTokenFromAuthorization(authorization);
        if (!StringUtils.hasText(accessToken)) {
            return false;
        }

        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(accessToken);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException | ExpiredJwtException ignored) {
        }

        return false;
    }

    public boolean validateRefreshToken(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            return false;
        }

        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(refreshToken);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException | ExpiredJwtException ignored) {
        }

        return false;
    }

    // refresh 토큰 유효기간 확인
    public boolean shouldRenewRefresh(String jwt) {
        Date expirationDate = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(jwt)
                .getPayload()
                .getExpiration();

        long remainingTime = expirationDate.getTime() - System.currentTimeMillis();
        return remainingTime < refreshRenewSeconds * 1000L;
    }

    // jwt -> Authentication
    public Authentication getAuthentication(String jwt) {
        Claims claims = getClaims(jwt);

        Collection<? extends GrantedAuthority> authorities = Arrays
                .stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        User principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, jwt, authorities);
    }

    // jwt -> MemberInfo
    public MemberInfoVO getMemberInfo(String jwt) {
        Claims claims = getClaims(jwt);

        Set<Role> roles = Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                .map(Role::valueOf)
                .collect(Collectors.toSet());

        return MemberInfoVO.builder()
                .email(claims.getSubject())
                .roles(roles)
                .build();
    }

    private Claims getClaims(String jwt) {
        return Jwts
                .parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }

    public String getAccessTokenFromAuthorization(String authorization) {
        return authorization.substring(7);
    }
}
