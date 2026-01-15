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

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static com.exadel.frs.system.global.Constants.ADMIN;

@Profile("!local-test")
@RequiredArgsConstructor
@Configuration
public class SpringFoxConfig {

    public final SwaggerInfoProperties swaggerInfoProperties;

    @Value("${host.full.dns.auth.link}")
    private String authLink;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("oauth2", new SecurityScheme()
                                .type(SecurityScheme.Type.OAUTH2)
                                .flows(new OAuthFlows()
                                        .password(new OAuthFlow()
                                                .tokenUrl(authLink + ADMIN + "/oauth/token")
                                                .scopes(new Scopes().addString("read write", "all"))))))
                .addSecurityItem(new SecurityRequirement().addList("oauth2"))
                .info(buildApiInfo());
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .packagesToScan("com.exadel.frs.controller")
                .pathsToExclude("/error.*", "/oauth.*")
                .build();
    }

    private Info buildApiInfo() {
        Info info = new Info()
                .title(swaggerInfoProperties.getTitle())
                .description(swaggerInfoProperties.getDescription())
                .version(swaggerInfoProperties.getVersion())
                .termsOfService(swaggerInfoProperties.getTermsOfServiceUrl());

        if (swaggerInfoProperties.getContactName() != null ||
            swaggerInfoProperties.getContactUrl() != null ||
            swaggerInfoProperties.getContactEmail() != null) {
            info.contact(new Contact()
                    .name(swaggerInfoProperties.getContactName())
                    .url(swaggerInfoProperties.getContactUrl())
                    .email(swaggerInfoProperties.getContactEmail()));
        }

        if (swaggerInfoProperties.getLicense() != null) {
            info.license(new License()
                    .name(swaggerInfoProperties.getLicense())
                    .url(swaggerInfoProperties.getLicenseUrl()));
        }

        return info;
    }
}
