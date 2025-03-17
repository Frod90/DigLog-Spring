package api.store.diglog.model.dto.post;

import api.store.diglog.model.constant.SearchOption;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PostListSearchRequest {

    private String keyword;
    private SearchOption option;
    private List<String> sorts;
    private int page;
    private int size;
    private Boolean isDescending;
}
