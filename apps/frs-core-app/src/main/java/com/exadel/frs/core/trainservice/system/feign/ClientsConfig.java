package com.exadel.frs.core.trainservice.system.feign;

import static com.exadel.frs.core.trainservice.system.global.EnvironmentProperties.ServerType.PYTHON;
import com.exadel.frs.core.trainservice.system.global.EnvironmentProperties;
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
    public FacesClient getScanFacesClient() {
        return Feign.builder()
                    .encoder(new SpringFormEncoder())
                    .decoder(new JacksonDecoder())
                    .logLevel(Logger.Level.FULL)
                    .target(FacesClient.class, properties.getServers().get(PYTHON).getUrl());
    }
}