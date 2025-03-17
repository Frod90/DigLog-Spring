package api.store.diglog.model.dto.login;

import api.store.diglog.model.constant.Platform;

public interface OAuth2Response {

    String getProvider();

    String getProviderId();

    String getEmail();

    String getName();

    Platform getPlatform();
}
