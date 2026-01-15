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

package com.exadel.frs.core.trainservice.system.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("!local-test")
@RequiredArgsConstructor
@Configuration
public class SwaggerApiConfig {

    public final SwaggerInfoProperties swaggerInfoProperties;

    @Bean
    public OpenAPI apiOpenAPI() {
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

        return new OpenAPI().info(info);
    }

    @Bean
    public GroupedOpenApi swaggerApi() {
        return GroupedOpenApi.builder()
                .group("api")
                .packagesToScan("com.exadel.frs.core.trainservice.controller")
                .pathsToExclude("/error.*", "/actuator.*")
                .build();
    }
}
