package com.exadel.frs.controller;

import static com.exadel.frs.system.global.Constants.ADMIN;
import static com.exadel.frs.commonservice.enums.AppStatus.NOT_READY;
import static com.exadel.frs.commonservice.enums.AppStatus.OK;
import com.exadel.frs.commonservice.enums.AppStatus;
import com.exadel.frs.dto.AppStatusResponseDto;
import io.swagger.annotations.ApiOperation;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(ADMIN)
@RequiredArgsConstructor
public class AppStatusController {

    private final DataSource dataSource;

    @GetMapping("/status")
    @ApiOperation(value = "Get status of application")
    public AppStatusResponseDto getAppStatus() {
        try (Connection connection = dataSource.getConnection()) {
            AppStatus status = connection.isValid(1000) ? OK : NOT_READY;
            return new AppStatusResponseDto(status);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            return new AppStatusResponseDto(NOT_READY);
        }
    }
}
