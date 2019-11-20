package com.exadel.frs.properties;

import static org.springframework.util.StringUtils.isEmpty;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;

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

    ApiInfoBuilder builder = new ApiInfoBuilder();

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
