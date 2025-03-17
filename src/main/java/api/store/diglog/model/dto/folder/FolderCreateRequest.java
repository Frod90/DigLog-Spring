package api.store.diglog.model.dto.folder;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class FolderCreateRequest {

	private UUID id;

	@NotBlank(message = "제목을 입력해주세요")
	private String title;

	@PositiveOrZero(message = "폴더 깊이는 0이상의 숫자만 입력 가능합니다.")
	private int depth;

	@PositiveOrZero(message = "폴더 인덱스는 0이상의 숫자만 입력 가능합니다.")
	private int orderIndex;

	private int parentOrderIndex;
}
