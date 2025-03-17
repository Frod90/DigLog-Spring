package api.store.diglog.model.dto.folder;

import lombok.*;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class FolderPostResponse {

    private UUID id;
    private String title;
}
