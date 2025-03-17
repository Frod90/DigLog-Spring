package api.store.diglog.model.vo.login;

import api.store.diglog.model.dto.member.MemberInfoResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RenewRefreshTokenVO {

    private TokenVO tokenVO;
    private MemberInfoResponse memberInfoResponse;
}
