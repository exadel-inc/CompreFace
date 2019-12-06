package com.exadel.frs.config;

import com.exadel.frs.properties.SwaggerInfoProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

  @Autowired
  public SwaggerInfoProperties swaggerInfoProperties;

  public static final String JWT = "JWT";

  @Bean
  public Docket api() {
    Docket docket = new Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis((RequestHandlerSelectors.basePackage("com.exadel.frs.controller")))
        .paths(PathSelectors.ant("/**"))
        .build();

    docket
        .apiInfo(swaggerInfoProperties.getApiInfo());

    docket
        .securitySchemes(Collections.singletonList(securityScheme()))
        .securityContexts(Collections.singletonList(securityContext()));

    return docket;
  }

  @Bean
  public SecurityConfiguration security() {
    return SecurityConfigurationBuilder.builder()
        .scopeSeparator(" ")
        .useBasicAuthenticationWithAccessCodeGrant(false)
        .build();
  }

  private SecurityScheme securityScheme() {
    return new ApiKey(JWT, HttpHeaders.AUTHORIZATION, "header");
  }

  private SecurityContext securityContext() {
    return SecurityContext.builder()
        .securityReferences(getSecurityReferences())
        .forPaths(PathSelectors.regex("/.*"))
        .build();
  }

  protected List<SecurityReference> getSecurityReferences() {
    return List.of(new SecurityReference(JWT, new AuthorizationScope[0]));
  }

  private AuthorizationScope[] scopes() {
    return new AuthorizationScope[0];
  }

}
