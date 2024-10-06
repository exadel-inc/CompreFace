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

package com.exadel.frs.system.security.config;

import static com.exadel.frs.system.global.Constants.ADMIN;
import com.exadel.frs.system.security.CustomOAuth2Exception;
import com.exadel.frs.system.security.CustomUserDetailsService;
import com.exadel.frs.system.security.TokenServicesImpl;
import com.exadel.frs.system.security.client.Client;
import com.exadel.frs.system.security.client.ClientService;
import com.exadel.frs.system.security.client.OAuthClientProperties;
import com.exadel.frs.system.security.endpoint.CustomTokenEndpoint;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@EnableAuthorizationServer
@RequiredArgsConstructor
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter {

    @Qualifier("authenticationManagerBean")
    private final AuthenticationManager authenticationManager;
    private final ClientService clientService;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final OAuthClientProperties authClientProperties;
    private final JdbcTokenStore tokenStore;

    @Bean
    @Primary
    public TokenEndpoint tokenEndpoint(AuthorizationServerEndpointsConfiguration conf) {
        TokenEndpoint tokenEndpoint = new CustomTokenEndpoint();
        tokenEndpoint.setClientDetailsService(conf.getEndpointsConfigurer().getClientDetailsService());
        tokenEndpoint.setProviderExceptionHandler(conf.getEndpointsConfigurer().getExceptionTranslator());
        tokenEndpoint.setTokenGranter(conf.getEndpointsConfigurer().getTokenGranter());
        tokenEndpoint.setOAuth2RequestFactory(conf.getEndpointsConfigurer().getOAuth2RequestFactory());
        tokenEndpoint.setOAuth2RequestValidator(conf.getEndpointsConfigurer().getOAuth2RequestValidator());
        tokenEndpoint.setAllowedRequestMethods(conf.getEndpointsConfigurer().getAllowedTokenEndpointRequestMethods());
        return tokenEndpoint;
    }

    @Bean
    public DefaultTokenServices tokenServices() {
        TokenServicesImpl tokenServices = new TokenServicesImpl(tokenStore);
        tokenServices.setClientDetailsService(clientService);
        return tokenServices;
    }

    @Override
    public void configure(final AuthorizationServerSecurityConfigurer oauthServer) {
        oauthServer.tokenKeyAccess("permitAll()")
                   .checkTokenAccess("isAuthenticated()");
    }

    @Override
    @Transactional
    public void configure(final ClientDetailsServiceConfigurer clients) throws Exception {
        clients.withClientDetails(clientService);

        var appClients = authClientProperties.getClients().values()
                                             .stream()
                                             .map(it ->
                                                     new Client()
                                                             .setClientId(it.getClientId())
                                                             .setAuthorizedGrantTypes(String.join(",", it.getAuthorizedGrantTypes()))
                                                             .setAuthorities(String.join(",", it.getAuthorities()))
                                                             .setResourceIds(String.join(",", it.getResourceIds()))
                                                             .setScope(String.join(",", it.getClientScope()))
                                                             .setClientSecret(passwordEncoder.encode(it.getClientSecret()))
                                                             .setAccessTokenValidity(it.getAccessTokenValidity())
                                                             .setRefreshTokenValidity(it.getRefreshTokenValidity())
                                                             .setAutoApprove("*"))
                                             .toList();
        clientService.saveAll(appClients);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints
                .tokenStore(tokenStore)
                .tokenServices(tokenServices())
                .authenticationManager(authenticationManager)
                .userDetailsService(userDetailsService)
                .pathMapping("/oauth/token", ADMIN + "/oauth/token");

        endpoints.exceptionTranslator(exception -> {
            if (exception instanceof OAuth2Exception oAuth2Exception) {
                return ResponseEntity
                        .status(oAuth2Exception.getHttpErrorCode())
                        .body(new CustomOAuth2Exception(oAuth2Exception.getMessage()));
            } else {
                throw exception;
            }
        });
    }
}
