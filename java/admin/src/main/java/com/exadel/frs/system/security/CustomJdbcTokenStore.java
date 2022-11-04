package com.exadel.frs.system.security;

import java.sql.Types;
import java.time.LocalDateTime;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.common.DefaultExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class CustomJdbcTokenStore extends JdbcTokenStore {

    private static final String INSERT_ACCESS_TOKEN_WITH_EXPIRATION_SQL = "insert into oauth_access_token (token_id, token, authentication_id, user_name, client_id, authentication, refresh_token, expiration) values (?, ?, ?, ?, ?, ?, ?,?)";
    private static final String INSERT_REFRESH_TOKEN_WITH_EXPIRATION_SQL = "insert into oauth_refresh_token (token_id, token, authentication, expiration) values (?, ?, ?, ?)";
    private static final String REMOVE_EXPIRED_ACCESS_TOKENS_SQL = "delete from oauth_access_token where expiration < ?";
    private static final String REMOVE_EXPIRED_REFRESH_TOKENS_SQL = "delete from oauth_refresh_token where expiration < ?";

    private final JdbcTemplate jdbcTemplate;

    public CustomJdbcTokenStore(DataSource dataSource) {
        super(dataSource);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.setAuthenticationKeyGenerator(new AuthenticationKeyGeneratorImpl());
    }

    @Override
    public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
        String refreshToken = getRefreshToken(token);
        if (this.readAccessToken(token.getValue()) != null) {
            this.removeAccessToken(token.getValue());
        }

        DefaultAuthenticationKeyGenerator defaultAuthenticationKeyGenerator = new DefaultAuthenticationKeyGenerator();

        this.jdbcTemplate.update(
                INSERT_ACCESS_TOKEN_WITH_EXPIRATION_SQL,
                new Object[]{this.extractTokenKey(token.getValue()),
                        new SqlLobValue(this.serializeAccessToken(token)),
                        defaultAuthenticationKeyGenerator.extractKey(authentication),
                        authentication.isClientOnly() ? null : authentication.getName(),
                        authentication.getOAuth2Request().getClientId(),
                        new SqlLobValue(this.serializeAuthentication(authentication)),
                        this.extractTokenKey(refreshToken), token.getExpiration()},
                new int[]{Types.VARCHAR, Types.BLOB, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.BLOB, Types.VARCHAR, Types.TIMESTAMP}
        );
    }

    private String getRefreshToken(OAuth2AccessToken token) {
        if (token.getRefreshToken() != null) {
            return token.getRefreshToken().getValue();
        }
        return null;
    }

    @Override
    public void storeRefreshToken(OAuth2RefreshToken refreshToken, OAuth2Authentication authentication) {
        DefaultExpiringOAuth2RefreshToken oAuth2RefreshToken = (DefaultExpiringOAuth2RefreshToken) refreshToken;
        this.jdbcTemplate.update(
                INSERT_REFRESH_TOKEN_WITH_EXPIRATION_SQL,
                new Object[]{this.extractTokenKey(refreshToken.getValue()), new SqlLobValue(this.serializeRefreshToken(refreshToken)), new SqlLobValue(
                        this.serializeAuthentication(authentication)), oAuth2RefreshToken.getExpiration()},
                new int[]{Types.VARCHAR, Types.BLOB, Types.BLOB, Types.TIMESTAMP}
        );
    }

    @Transactional
    @Scheduled(cron = "@weekly")
    public void removeExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        int accessTokenCount = this.jdbcTemplate.update(
                REMOVE_EXPIRED_ACCESS_TOKENS_SQL,
                now
        );
        int refreshTokenCount = this.jdbcTemplate.update(
                REMOVE_EXPIRED_REFRESH_TOKENS_SQL,
                now
        );
        log.info(
                "Removed {} expired access tokens and {} expired update tokens",
                accessTokenCount,
                refreshTokenCount
        );
    }
}
