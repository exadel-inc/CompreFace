package com.exadel.frs.controller;


import com.exadel.frs.commonservice.enums.AppStatus;
import com.exadel.frs.dto.ui.AppStatusResponseDto;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;

@RestController
@RequiredArgsConstructor
public class AppStatusController {

    private final DataSource dataSource;

    @GetMapping("/status")
    @ApiOperation(value = "Get status of application")
    public AppStatusResponseDto getAppStatus() {
        AppStatusResponseDto responseDto = new AppStatusResponseDto();
        try {
            responseDto.setStatus(dataSource.getConnection().isValid(1000) ? AppStatus.OK : AppStatus.NOT_READY);
            return responseDto;
        } catch (Exception e) {
            responseDto.setStatus(AppStatus.NOT_READY);
            return responseDto;
        }
    }
}
