package api.store.diglog.model.dto.post;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PostListMemberRequest {

    private String username;
    private List<UUID> folderIds;
    private int page;
    private int size;
}
