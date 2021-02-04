package com.exadel.frs.core.trainservice.system.swagger;

import com.google.common.base.Predicates;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Profile("!local-test")
@RequiredArgsConstructor
@Configuration
public class SwaggerApiConfig {

    public final SwaggerInfoProperties swaggerInfoProperties;

    @Bean
    public Docket swaggerApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("api")
                .select()
                .apis((RequestHandlerSelectors.basePackage("com.exadel.frs.core.trainservice.controller")))
                .paths(Predicates.and(
                        Predicates.not(PathSelectors.regex("/error.*")),
                        Predicates.not(PathSelectors.regex("/actuator.*"))
                )).build()
                .apiInfo(swaggerInfoProperties.getApiInfo());
    }
}
