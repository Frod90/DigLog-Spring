package api.store.diglog.model.dto.comment;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentMember {

    private String username;
    private String profileUrl;
}
