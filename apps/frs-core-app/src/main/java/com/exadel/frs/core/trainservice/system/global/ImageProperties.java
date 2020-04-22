package com.exadel.frs.core.trainservice.system.global;

import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "image")
@Data
public class ImageProperties {

    @NotNull
    @Size(min = 1)
    private final List types;

}