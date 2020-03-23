package com.exadel.frs.system.global;

import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "environment")
@Data
public class EnvironmentProperties {

    @NotNull
    @Size(min = 1)
    private final Map<ServerType, ServerInfo> servers;

    @Data
    public static final class ServerInfo {

        private String url;
    }

    public enum ServerType {
        FRS_CORE
    }
}