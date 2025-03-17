package api.store.diglog.model.vo.tag;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TagPostVO {

    List<String> tagNames;
}
