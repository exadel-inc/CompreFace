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

package com.exadel.frs.system.security.client;

import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

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