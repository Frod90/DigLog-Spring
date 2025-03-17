package api.store.diglog.model.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Platform {
    KAKAO("KAKAO"),
    SERVER("SERVER");

    private final String platform;
}
