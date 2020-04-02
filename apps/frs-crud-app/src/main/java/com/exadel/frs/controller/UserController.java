package com.exadel.frs.controller;

import com.exadel.frs.dto.ui.UserAutocompleteDto;
import com.exadel.frs.dto.ui.UserCreateDto;
import com.exadel.frs.dto.ui.UserResponseDto;
import com.exadel.frs.dto.ui.UserUpdateDto;
import com.exadel.frs.helpers.SecurityUtils;
import com.exadel.frs.mapper.UserMapper;
import com.exadel.frs.service.UserService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/me")
    @ApiOperation(value = "Get information about user, that logged in")
    public UserResponseDto getUser() {
        return userMapper.toResponseDto(SecurityUtils.getPrincipal());
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/register")
    @ApiOperation(value = "Register new user")
    @ApiResponses({
            @ApiResponse(code = 400, message = "Such username or email already registered | One or more of required fields are empty | Incorrect email format")
    })
    public void createUser(
            @ApiParam(value = "User object that needs to be created", required = true)
            @RequestBody
            final UserCreateDto userCreateDto
    ) {
        userService.createUser(userCreateDto);
    }

    @PutMapping("/update")
    @ApiOperation(value = "Update user data")
    @ApiResponses({
            @ApiResponse(code = 400, message = "Such username or email already registered")
    })
    public void updateUser(
            @ApiParam(value = "User data that needs to be updated", required = true)
            @RequestBody
            final UserUpdateDto userUpdateDto
    ) {
        userService.updateUser(userUpdateDto, SecurityUtils.getPrincipalId());
    }

    @DeleteMapping("/delete")
    @ApiOperation(value = "Delete user")
    public void deleteUser() {
        userService.deleteUser(SecurityUtils.getPrincipalId());
    }

    @GetMapping("/autocomplete")
    @ApiOperation(value = "User autocomplete by (email, first name or last name)")
    public UserAutocompleteDto autocomplete(@RequestParam final String query) {

        val results =  userMapper.toResponseDto(userService.autocomplete(query));

        return UserAutocompleteDto
                    .builder()
                    .length(results.size())
                    .query(query)
                    .results(results)
                    .build();
    }
}