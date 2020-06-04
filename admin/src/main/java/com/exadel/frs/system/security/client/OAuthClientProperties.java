package com.exadel.frs.system.security.client;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "app.security.oauth2")
@Validated
@Getter
@Setter
public class OAuthClientProperties {

    @NotNull
    @Size(min = 1)
    private Map<ClientType, Client> clients;

    public enum ClientType {
        COMMON
    }

    @Data
    public static final class Client {

        private Integer accessTokenValidity;
        private List<String> authorities;
        private List<String> authorizedGrantTypes;
        private String clientId;
        private List<String> clientScope;
        private String clientSecret;
        private Integer refreshTokenValidity;
        private List<String> resourceIds;

    }
}