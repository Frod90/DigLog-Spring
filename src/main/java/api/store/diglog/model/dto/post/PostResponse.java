package api.store.diglog.model.dto.post;

import api.store.diglog.model.dto.folder.FolderPostResponse;
import api.store.diglog.model.dto.tag.TagResponse;
import api.store.diglog.model.entity.Post;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PostResponse {

    private UUID id;
    private String title;
    private String content;
    private String username;
    private FolderPostResponse folder;
    private List<TagResponse> tags;
    private LocalDateTime createdAt;

    public PostResponse(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.username = post.getMember().getUsername();
        this.tags = post.getTags().stream()
                .map(TagResponse::new)
                .toList();
        this.createdAt = post.getCreatedAt();

        if (post.getFolder() != null) {
            this.folder = FolderPostResponse.builder()
                    .id(post.getFolder().getId())
                    .title(post.getFolder().getTitle())
                    .build();
        }
    }
}
