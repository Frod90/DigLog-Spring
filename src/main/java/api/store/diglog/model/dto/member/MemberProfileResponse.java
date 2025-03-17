package api.store.diglog.model.dto.member;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberProfileResponse {

    private String email;
    private String username;
    private String profileUrl;
}
