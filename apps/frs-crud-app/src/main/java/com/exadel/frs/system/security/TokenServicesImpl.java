package com.exadel.frs.system.security;


import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.ExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import java.util.Collection;
import java.util.UUID;

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
            if (accessToken.getRefreshToken() instanceof ExpiringOAuth2RefreshToken) {
                ExpiringOAuth2RefreshToken expiring = (ExpiringOAuth2RefreshToken) accessToken.getRefreshToken();
                if (System.currentTimeMillis() > expiring.getExpiration().getTime()) {
                    tokenStore.removeAccessToken(accessToken);
                    tokenStore.removeRefreshToken(accessToken.getRefreshToken());
                }
            }
        });
        return super.createAccessToken(authentication);
    }
}
