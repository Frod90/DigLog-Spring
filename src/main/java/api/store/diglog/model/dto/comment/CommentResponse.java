package api.store.diglog.model.dto.comment;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentResponse {

    private UUID id;
    private CommentMember member;
    private String content;
    private boolean isDeleted;
    private String taggedUsername;
    private LocalDateTime createdAt;
    private int replyCount;
}
