package com.exadel.frs.controller;

import static com.exadel.frs.system.global.Constants.ADMIN;
import com.exadel.frs.dto.ui.ConfigDto;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ADMIN + "/config")
@RequiredArgsConstructor
public class ConfigController {

    private final Environment env;

    @GetMapping
    @ApiOperation(value = "Returns configuration properties of the application")
    public ConfigDto getConfig() {
        return ConfigDto.builder()
                        .mailServiceEnabled(Boolean.parseBoolean(env.getProperty("spring.mail.enable")))
                        .build();
    }
}
