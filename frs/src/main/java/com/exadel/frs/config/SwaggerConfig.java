package com.exadel.frs.config;

import com.exadel.frs.config.SwaggerConfig.AuthProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.OAuthBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.GrantType;
import springfox.documentation.service.ResourceOwnerPasswordCredentialsGrant;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;

@Configuration
@EnableSwagger2
@EnableConfigurationProperties(AuthProperties.class)
public class SwaggerConfig {

  @Autowired
  public AuthProperties authProperties;

  @Autowired
  public SwaggerInfoProperties swaggerInfoProperties;

  @Value("${swagger.auth.server}")
  private String AUTH_SERVER;

  @Bean
  public Docket api() {
    Docket docket = new Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(RequestHandlerSelectors.any())
        .paths(PathSelectors.ant("/**"))
        .build();

    docket
        .apiInfo(swaggerInfoProperties.getApiInfo());

    docket
        .securitySchemes(Arrays.asList(securityScheme()))
        .securityContexts(Arrays.asList(securityContext()));

    return docket;
  }

  @Bean
  public SecurityConfiguration security() {
    return SecurityConfigurationBuilder.builder()
        .scopeSeparator(" ")
        .clientId(authProperties.getClientId())
        .clientSecret(authProperties.getClientSecret())
        .useBasicAuthenticationWithAccessCodeGrant(true)
        .build();
  }

  private SecurityScheme securityScheme() {
    GrantType grantType = new ResourceOwnerPasswordCredentialsGrant(AUTH_SERVER);

    SecurityScheme oauth = new OAuthBuilder().name("password")
        .grantTypes(Arrays.asList(grantType))
        .scopes(Arrays.asList(scopes()))
        .build();
    return oauth;
  }

  private SecurityContext securityContext() {
    return SecurityContext.builder()
        .securityReferences(
            Arrays.asList(new SecurityReference("spring_oauth2",
                scopes())))
        .forPaths(PathSelectors.regex("/.*"))
        .build();
  }

  private AuthorizationScope[] scopes() {
    AuthorizationScope[] scopes = new AuthorizationScope[0];
    return scopes;
  }

  @ConfigurationProperties(prefix = "security.oauth2.client")
  @Getter
  @Setter
  class AuthProperties {

    private String clientId;
    private String clientSecret;
  }

}
