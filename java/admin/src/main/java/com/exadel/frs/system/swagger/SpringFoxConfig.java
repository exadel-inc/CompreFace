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

package com.exadel.frs.system.swagger;

import static com.exadel.frs.system.global.Constants.ADMIN;
import static com.exadel.frs.system.security.client.OAuthClientProperties.ClientType.COMMON;
import com.exadel.frs.system.security.client.OAuthClientProperties;
import com.google.common.base.Predicates;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.GrantType;
import springfox.documentation.service.OAuth;
import springfox.documentation.service.ResourceOwnerPasswordCredentialsGrant;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Profile("!local-test")
@RequiredArgsConstructor
@Configuration
@EnableSwagger2
public class SpringFoxConfig {

    public final SwaggerInfoProperties swaggerInfoProperties;
    @Value("${host.full.dns.auth.link}")
    private String authLink;

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis((RequestHandlerSelectors.basePackage("com.exadel.frs.controller")))
                .paths(Predicates.and(
                        Predicates.not(PathSelectors.regex("/error.*")),
                        Predicates.not(PathSelectors.regex("/oauth.*"))
                )).build()
                .apiInfo(swaggerInfoProperties.getApiInfo())
                .securitySchemes(Collections.singletonList(securitySchema()))
                .securityContexts(Collections.singletonList(securityContext())).pathMapping("/");
    }

    private OAuth securitySchema() {
        val authorizationScopeList = new ArrayList<AuthorizationScope>();
        authorizationScopeList.add(new AuthorizationScope("read write ", "all"));
        val grantTypes = new ArrayList<GrantType>();
        val creGrant = new ResourceOwnerPasswordCredentialsGrant(authLink + ADMIN + "/oauth/token");
        grantTypes.add(creGrant);

        return new OAuth("oauth2schema", authorizationScopeList, grantTypes);
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                              .securityReferences(defaultAuth())
                              .forPaths(PathSelectors.any())
                              .build();
    }

    private List<SecurityReference> defaultAuth() {
        val authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = new AuthorizationScope("read write", "all");

        return Collections.singletonList(new SecurityReference("oauth2schema", authorizationScopes));
    }

    @Bean
    public SecurityConfiguration securityInfo(OAuthClientProperties oAuthClientProperties) {
        var clientId = oAuthClientProperties.getClients().get(COMMON).getClientId();
        var clientSecret = oAuthClientProperties.getClients().get(COMMON)
                                                .getClientSecret();

        return SecurityConfigurationBuilder.builder()
                                           .clientId(clientId)
                                           .clientSecret(clientSecret)
                                           .build();
    }
}