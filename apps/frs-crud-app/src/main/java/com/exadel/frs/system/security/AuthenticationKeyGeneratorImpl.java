package com.exadel.frs.system.security;

import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

public class AuthenticationKeyGeneratorImpl extends DefaultAuthenticationKeyGenerator {

    public static final String NO_CACHE_UUID = "no_cache_uuid";
    private static final String CLIENT_ID = "client_id";
    private static final String SCOPE = "scope";
    private static final String USERNAME = "username";

    public String extractKey(OAuth2Authentication authentication) {
        Map<String, String> values = new LinkedHashMap<String, String>();
        OAuth2Request authorizationRequest = authentication.getOAuth2Request();
        values.put(USERNAME, authentication.getName());
        values.put(CLIENT_ID, authorizationRequest.getClientId());
        values.put(NO_CACHE_UUID, authorizationRequest.getExtensions().get(NO_CACHE_UUID).toString());
        if (authorizationRequest.getScope() != null) {
            values.put(SCOPE, OAuth2Utils.formatParameterList(new TreeSet<String>(authorizationRequest.getScope())));
        }
        return generateKey(values);
    }
}
