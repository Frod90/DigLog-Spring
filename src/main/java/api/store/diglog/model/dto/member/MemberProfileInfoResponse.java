package api.store.diglog.model.dto.member;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberProfileInfoResponse {

    private String username;
    private String profileUrl;
}
