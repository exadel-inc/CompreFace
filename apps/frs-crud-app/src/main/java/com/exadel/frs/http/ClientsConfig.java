package com.exadel.frs.http;

import static com.exadel.frs.system.global.EnvironmentProperties.ServerType.FRS_CORE;
import com.exadel.frs.system.global.EnvironmentProperties;
import feign.Feign;
import feign.Logger;
import feign.form.spring.SpringFormEncoder;
import feign.jackson.JacksonDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClientsConfig {

    private final EnvironmentProperties properties;

    @Bean
    public CoreDeleteFacesClient getClient() {
        return Feign.builder()
                    .encoder(new SpringFormEncoder())
                    .decoder(new JacksonDecoder())
                    .logLevel(Logger.Level.FULL)
                    .target(CoreDeleteFacesClient.class, properties.getServers().get(FRS_CORE).getUrl());
    }
}