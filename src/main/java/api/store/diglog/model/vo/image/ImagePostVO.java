package api.store.diglog.model.vo.image;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class ImagePostVO {

    private UUID id;
    private List<String> urls;
}
