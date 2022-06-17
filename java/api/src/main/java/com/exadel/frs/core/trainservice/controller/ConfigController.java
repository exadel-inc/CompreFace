package com.exadel.frs.core.trainservice.controller;

import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import com.exadel.frs.core.trainservice.dto.ConfigDto;
import io.swagger.annotations.ApiOperation;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(API_V1 + "/config")
@RequiredArgsConstructor
public class ConfigController {

    private final Environment env;

    @GetMapping
    @ApiOperation(value = "Returns configuration properties of the application")
    public ConfigDto getConfig() {
        return ConfigDto.builder()
                        .maxFileSize(getNumericPropertyAsBytes("spring.servlet.multipart.max-file-size"))
                        .maxBodySize(getNumericPropertyAsBytes("spring.servlet.multipart.max-request-size"))
                        .build();
    }

    private Long getNumericPropertyAsBytes(String propertyName) {
        return Optional.ofNullable(env.getProperty(propertyName))
                       .map(DataSize::parse)
                       .map(DataSize::toBytes)
                       .orElse(null);
    }
}
