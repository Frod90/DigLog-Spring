package api.store.diglog.model.dto.member;

import lombok.*;

import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberInfoResponse {

    private int status;
    private String email;
    private String username;
    private Set<String> roles;
}
