package api.store.diglog.model.dto.comment;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentListRequest {

    private UUID postId;
    private UUID parentCommentId;
    private int page;
    private int size;
}
