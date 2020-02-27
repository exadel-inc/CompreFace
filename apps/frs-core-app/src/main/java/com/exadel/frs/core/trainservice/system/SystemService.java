package com.exadel.frs.core.trainservice.system;

public interface SystemService {
    TokenParts getApiKeyAndModelApiKey(String token);
}
