package com.exadel.frs.system.security.client;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.util.StringUtils;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.*;

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