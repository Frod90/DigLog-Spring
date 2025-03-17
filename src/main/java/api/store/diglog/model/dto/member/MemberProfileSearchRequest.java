package api.store.diglog.model.dto.member;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberProfileSearchRequest {

    private String username;
    private int page;
    private int size;
}
