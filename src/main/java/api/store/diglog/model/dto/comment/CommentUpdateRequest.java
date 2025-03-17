package api.store.diglog.model.dto.comment;

import lombok.*;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentUpdateRequest {

    private UUID id;
    private String content;
    private String taggedUsername;
}
