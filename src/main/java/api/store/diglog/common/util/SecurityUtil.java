package api.store.diglog.common.util;

import java.util.Set;
import java.util.stream.Collectors;

import api.store.diglog.model.constant.Role;
import api.store.diglog.model.vo.member.MemberInfoVO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    static public MemberInfoVO getAuthenticationMemberInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();
        Set<Role> roles = authentication.getAuthorities().stream()
                .map((role) -> Role.valueOf(role.getAuthority()))
                .collect(Collectors.toSet());

        return MemberInfoVO.builder()
                .email(email)
                .roles(roles)
                .build();
    }

}
