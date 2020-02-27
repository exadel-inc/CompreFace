package com.exadel.frs.core.trainservice.system.python;

import feign.Feign;
import feign.Logger;
import feign.form.spring.SpringFormEncoder;
import feign.jackson.JacksonDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ClientsConfig {

    @Bean
    public PythonClient getClient() {
        return Feign.builder()
                .encoder(new SpringFormEncoder())
                .decoder(new JacksonDecoder())
                .logLevel(Logger.Level.FULL)
                .target(PythonClient.class, "http://localhost:5001");
    }
}