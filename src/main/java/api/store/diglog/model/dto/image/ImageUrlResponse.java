package api.store.diglog.model.dto.image;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageUrlResponse {

    private String url;
}
