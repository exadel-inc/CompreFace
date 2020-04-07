package com.exadel.frs.core.trainservice.system;

import lombok.val;
import org.springframework.stereotype.Service;

@Service
public class SystemServiceImpl implements SystemService {

    @Override
    public Token getTokenParts(final String token) {
        val appApiKey = token.length() / 2;

        return new Token(
                token.substring(0, appApiKey),
                token.substring(appApiKey)
        );
    }
}