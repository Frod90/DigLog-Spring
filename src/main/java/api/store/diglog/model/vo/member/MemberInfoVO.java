package api.store.diglog.model.vo.member;

import api.store.diglog.model.constant.Role;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class MemberInfoVO {

    private String email;
    private Set<Role> roles;
}
