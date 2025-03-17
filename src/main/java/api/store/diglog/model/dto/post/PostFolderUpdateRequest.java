package api.store.diglog.model.dto.post;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PostFolderUpdateRequest {

    private List<UUID> postIds;
    private UUID folderId;
}
