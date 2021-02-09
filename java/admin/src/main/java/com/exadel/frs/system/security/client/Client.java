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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.util.StringUtils;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@Entity
@Table(name = "oauth_client_details")
public class Client implements ClientDetails {

    @Id
    @NotNull
    @Size(max = 50)
    private String clientId;

    private String clientSecret;

    @NotNull
    private String resourceIds;

    @NotNull
    private String scope;

    @NotNull
    private String authorizedGrantTypes;

    @NotNull
    private String authorities;

    private Integer accessTokenValidity;

    private Integer refreshTokenValidity;

    private String additionalInformation;

    private String autoApprove;

    @Override
    public boolean isSecretRequired() {
        return true;
    }

    @Override
    public boolean isScoped() {
        return !getScope().isEmpty();
    }

    @Override
    public Set<String> getScope() {
        if (StringUtils.hasText(scope)) {
            return StringUtils.commaDelimitedListToSet(scope);
        }
        return Collections.emptySet();
    }

    @Override
    public Set<String> getResourceIds() {
        Set<String> result = new HashSet<>();
        if (StringUtils.hasText(resourceIds)) {
            result = StringUtils.commaDelimitedListToSet(resourceIds);
        }
        return result;
    }

    @Override
    public Set<String> getAuthorizedGrantTypes() {
        Set<String> result = new HashSet<>();
        if (StringUtils.hasText(authorizedGrantTypes)) {
            result = StringUtils.commaDelimitedListToSet(authorizedGrantTypes);
        }
        return result;
    }

    @Override
    public Set<String> getRegisteredRedirectUri() {
        return new HashSet<>();
    }

    @Override
    public Set<GrantedAuthority> getAuthorities() {
        return Collections.emptySet();
    }

    @Override
    public Integer getAccessTokenValiditySeconds() {
        return accessTokenValidity;
    }

    @Override
    public Integer getRefreshTokenValiditySeconds() {
        return refreshTokenValidity;
    }

    @Override
    public boolean isAutoApprove(String scope) {
        boolean autoApproveScope = false;
        if (StringUtils.hasText(authorities)) {
            autoApproveScope = Arrays.stream(autoApprove.split(",")).anyMatch(scope::matches);
        }
        return autoApproveScope;
    }

    @Override
    public Map<String, Object> getAdditionalInformation() {
        return new HashMap<>();
    }
}