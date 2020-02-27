package com.exadel.frs.core.trainservice.system;

import org.springframework.stereotype.Service;

@Service
public class SystemServiceImpl implements SystemService {

    @Override
    public TokenParts getApiKeyAndModelApiKey(String token) {
        int apiKeyLength = token.length() / 2;
        return TokenParts.builder()
                .appApiKey(token.substring(0, apiKeyLength))
                .modelApiKey(token.substring(apiKeyLength))
                .build();
    }
}
