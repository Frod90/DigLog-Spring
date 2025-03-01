package api.store.diglog.model.dto.member;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberProfileInfoResponse {

    private String username;
    private String profileUrl;

    public MemberProfileInfoResponse(String username, String profileUrl) {
        this.username = username;
        this.profileUrl = profileUrl;
    }
}
