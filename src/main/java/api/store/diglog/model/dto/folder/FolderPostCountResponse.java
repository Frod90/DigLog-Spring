package api.store.diglog.model.dto.folder;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class FolderPostCountResponse {

	private UUID folderId;
	private String title;
	private int depth;
	private int orderIndex;
	private UUID parentFolderId;
	private long postCount;

}
