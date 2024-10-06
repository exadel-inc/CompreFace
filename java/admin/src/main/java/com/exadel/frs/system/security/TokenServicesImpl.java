/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.exadel.frs.system.security;

import java.util.Collection;
import java.util.UUID;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.ExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

public class TokenServicesImpl extends DefaultTokenServices {

    private TokenStore tokenStore;

    public TokenServicesImpl(JdbcTokenStore tokenStore) {
        tokenStore.setAuthenticationKeyGenerator(new AuthenticationKeyGeneratorImpl());
        this.tokenStore = tokenStore;
        this.setTokenStore(tokenStore);
        this.setSupportRefreshToken(true);
        this.setReuseRefreshToken(false);
    }

    public OAuth2AccessToken createAccessToken(OAuth2Authentication authentication) throws AuthenticationException {
        authentication.getOAuth2Request().getExtensions().put(
                AuthenticationKeyGeneratorImpl.NO_CACHE_UUID,
                UUID.randomUUID().toString()
        );
        Collection<OAuth2AccessToken> oAuth2AccessTokens = tokenStore.findTokensByClientIdAndUserName(authentication
                .getOAuth2Request().getClientId(), authentication.getName());
        oAuth2AccessTokens.forEach(accessToken -> {
            if (accessToken.getRefreshToken() instanceof ExpiringOAuth2RefreshToken expiring) {
                if (System.currentTimeMillis() > expiring.getExpiration().getTime()) {
                    tokenStore.removeAccessToken(accessToken);
                    tokenStore.removeRefreshToken(accessToken.getRefreshToken());
                }
            }
        });
        return super.createAccessToken(authentication);
    }
}
