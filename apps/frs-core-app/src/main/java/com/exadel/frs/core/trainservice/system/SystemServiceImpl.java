package com.exadel.frs.core.trainservice.system;

import lombok.val;
import org.springframework.stereotype.Service;

@Service
public class SystemServiceImpl implements SystemService {

    @Override
    public Token buildToken(final String apiKey) {
        val appApiKey = apiKey.length() / 2;

        return new Token(
                apiKey.substring(0, appApiKey),
                apiKey.substring(appApiKey)
        );
    }
}