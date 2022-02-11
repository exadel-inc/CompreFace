package com.exadel.frs.controller;


import com.exadel.frs.commonservice.entity.User;
import com.exadel.frs.dto.ui.AppStatusResponseDto;
import com.exadel.frs.service.UserService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AppStatusController {

    private final UserService userService;

    @GetMapping("/status")
    @ApiOperation(value = "Get status of application")
    public AppStatusResponseDto getAppStatus() {
        List<User> userList = userService.getUsers();
        AppStatusResponseDto responseDto = new AppStatusResponseDto();
        responseDto.setStatus(!CollectionUtils.isEmpty(userList));
        return responseDto;
    }
}
