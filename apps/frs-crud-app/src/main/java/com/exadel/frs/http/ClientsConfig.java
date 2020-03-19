package com.exadel.frs.http;

import feign.Feign;
import feign.Logger;
import feign.form.spring.SpringFormEncoder;
import feign.jackson.JacksonDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ClientsConfig {

    @Bean
    public CoreDeleteFacesClient getClient() {
        return Feign.builder()
                    .encoder(new SpringFormEncoder())
                    .decoder(new JacksonDecoder())
                    .logLevel(Logger.Level.FULL)
                    .target(CoreDeleteFacesClient.class, "http://localhost:8081");
    }
}