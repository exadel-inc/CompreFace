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

import static org.springframework.util.StringUtils.isEmpty;
import lombok.Data;
import lombok.val;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;

@Profile("!local-test")
@Data
@ConfigurationProperties(prefix = "swagger.info")
@Component
public class SwaggerInfoProperties {

    private String contactName;
    private String contactUrl;
    private String contactEmail;
    private String description;
    private String termsOfServiceUrl;
    private String title;
    private String license;
    private String licenseUrl;
    private String version;

    public ApiInfo getApiInfo() {

        val builder = new ApiInfoBuilder();

        if (!isEmpty(this.contactName)
                || !isEmpty(this.contactUrl)
                || !isEmpty(this.contactEmail)) {
            builder.contact(new Contact(this.contactName, this.contactUrl, this.contactEmail));
        }
        if (!isEmpty(this.description)) {
            builder.description(this.description);
        }
        if (!isEmpty(this.termsOfServiceUrl)) {
            builder.termsOfServiceUrl(this.termsOfServiceUrl);
        }
        if (!isEmpty(this.title)) {
            builder.title(this.title);
        }
        if (!isEmpty(this.license)) {
            builder.license(this.license);
        }
        if (!isEmpty(this.version)) {
            builder.version(this.version);
        }

        return builder.build();
    }
}
