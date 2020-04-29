package com.exadel.frs.core.trainservice.system.feign;


import feign.Feign;
import feign.Logger;
import feign.form.spring.SpringFormEncoder;
import feign.jackson.JacksonDecoder;
import org.springframework.stereotype.Component;

@Component
public class FeignClientFactory {

    public <T> T getFeignClient(Class<T> clazz, String clientUrl) {
        return Feign.builder()
                .encoder(new SpringFormEncoder())
                .decoder(new JacksonDecoder())
                .logLevel(Logger.Level.FULL)
                .target(clazz, clientUrl);
    }

}
