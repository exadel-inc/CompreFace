package com.exadel.frs.core.trainservice.system;

import org.springframework.stereotype.Service;

@Service
public class SystemServiceImpl implements SystemService {

    @Override
    public Token buildToken(final String apiKey) {
        return new Token(apiKey);
    }
}