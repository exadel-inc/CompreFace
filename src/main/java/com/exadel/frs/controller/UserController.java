package com.exadel.frs.controller;

import com.exadel.frs.dto.UserDto;
import com.exadel.frs.helpers.SecurityUtils;
import com.exadel.frs.mapper.UserMapper;
import com.exadel.frs.service.UserService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/me")
    @ApiOperation(value = "Get information about user, that logged in")
    public UserDto getUser() {
        return userMapper.toDto(SecurityUtils.getPrincipal());
    }

    @PostMapping("/register")
    @ApiOperation(value = "Register new user")
    @ApiResponses({
            @ApiResponse(code = 400, message = "Such username or email already registered | One or more of required fields are empty")
    })
    public void createUser(@ApiParam(value = "User object that needs to be created", required = true) @RequestBody UserDto userDto) {
        userService.createUser(userMapper.toEntity(userDto));
    }

    @PutMapping("/update")
    @ApiOperation(value = "Update user data")
    @ApiResponses({
            @ApiResponse(code = 400, message = "Such username or email already registered")
    })
    public void updateUser(@ApiParam(value = "User data that needs to be updated", required = true) @RequestBody UserDto userDto) {
        userService.updateUser(SecurityUtils.getPrincipalId(), userMapper.toEntity(userDto));
    }

    @DeleteMapping("/delete")
    @ApiOperation(value = "Delete user")
    public void deleteUser() {
        userService.deleteUser(SecurityUtils.getPrincipalId());
    }

}
