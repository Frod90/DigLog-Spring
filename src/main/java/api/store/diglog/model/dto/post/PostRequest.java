package api.store.diglog.model.dto.post;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PostRequest {

    private String title;
    private String content;
    private UUID folderId;
    private List<String> tagNames;
    private List<String> urls;
}
