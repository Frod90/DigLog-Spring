package api.store.diglog.model.dto.post;

import api.store.diglog.model.entity.Folder;
import api.store.diglog.model.entity.Post;
import api.store.diglog.model.entity.Tag;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PostUpdateRequest {

    private UUID id;
    private String title;
    private String content;
    private UUID folderId;
    private List<String> tagNames;
    private List<String> urls;

    public Post toPost(Post currentPost, Folder folder, List<Tag> tags) {
        return Post.builder()
                .id(id)
                .member(currentPost.getMember())
                .title(title)
                .content(content)
                .folder(folder)
                .tags(tags)
                .createdAt(currentPost.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
