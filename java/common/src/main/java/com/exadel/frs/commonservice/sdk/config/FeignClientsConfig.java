/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.exadel.frs.commonservice.sdk.config;

import static com.exadel.frs.commonservice.system.global.EnvironmentProperties.ServerType.PYTHON;
import static com.zaxxer.hikari.util.ClockSource.toMillis;
import static feign.Logger.Level.FULL;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import com.exadel.frs.commonservice.sdk.faces.feign.FacesFeignClient;
import com.exadel.frs.commonservice.system.global.EnvironmentProperties;
import feign.Feign;
import feign.Request;
import feign.Retryer;
import feign.form.spring.SpringFormEncoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FeignClientsConfig {

    @Value("${app.feign.faces.connect-timeout}")
    private int facesConnectTimeout;

    @Value("${app.feign.faces.read-timeout}")
    private int facesReadTimeout;

    @Value("${app.feign.faces.retryer.max-attempts}")
    private int facesRetryerMaxAttempts;

    private final EnvironmentProperties properties;

    @Bean
    public FacesFeignClient facesFeignClient() {
        return Feign.builder()
                    .encoder(new SpringFormEncoder(new JacksonEncoder()))
                    .decoder(new JacksonDecoder())
                    .logLevel(FULL)
                    .retryer(facesFeignRetryer())
                    .options(new Request.Options(facesConnectTimeout, MILLISECONDS, facesReadTimeout, MILLISECONDS, true))
                    .target(FacesFeignClient.class, properties.getServers().get(PYTHON).getUrl());
    }

    @Bean
    public Retryer facesFeignRetryer() {
        return new Retryer.Default(100, toMillis(1), facesRetryerMaxAttempts);
    }
}
