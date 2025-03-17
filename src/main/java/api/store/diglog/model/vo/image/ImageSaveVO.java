package api.store.diglog.model.vo.image;

import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Getter
@Builder
public class ImageSaveVO {

    private UUID refId;
    private MultipartFile file;
}
