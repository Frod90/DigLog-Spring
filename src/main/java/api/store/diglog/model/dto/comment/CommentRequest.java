package api.store.diglog.model.dto.comment;

import lombok.*;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentRequest {

    private String content;
    private UUID postId;
    private UUID parentCommentId;
    private String taggedUsername;
}
