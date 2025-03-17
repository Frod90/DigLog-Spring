package api.store.diglog.model.dto.tag;

import api.store.diglog.model.entity.Tag;
import lombok.*;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class TagResponse {

    private UUID id;
    private String name;

    public TagResponse(Tag tag) {
        this.id = tag.getId();
        this.name = tag.getName();
    }
}
