package api.store.diglog.model.dto.folder;

import java.util.UUID;

import api.store.diglog.model.entity.Folder;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class FolderResponse {

	private UUID folderId;
	private String title;
	private int depth;
	private int orderIndex;
	private String parentFolderId;

	@Builder
	private FolderResponse(Folder folder) {

		Folder parentFolder = folder.getParentFolder();
		String parentFolderId = "none";
		if (parentFolder != null) {
			parentFolderId = parentFolder.getId().toString();
		}

		this.folderId = folder.getId();
		this.title = folder.getTitle();
		this.depth = folder.getDepth();
		this.orderIndex = folder.getOrderIndex();
		this.parentFolderId = parentFolderId;
	}
}
