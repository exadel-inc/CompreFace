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

package com.exadel.frs.controller;

import static com.exadel.frs.system.global.Constants.GUID_EXAMPLE;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FOUND;
import static org.springframework.http.HttpStatus.OK;
import com.exadel.frs.dto.ui.UserAutocompleteDto;
import com.exadel.frs.dto.ui.UserCreateDto;
import com.exadel.frs.dto.ui.UserDeleteDto;
import com.exadel.frs.dto.ui.UserResponseDto;
import com.exadel.frs.dto.ui.UserUpdateDto;
import com.exadel.frs.exception.AccessDeniedException;
import com.exadel.frs.exception.UserDoesNotExistException;
import com.exadel.frs.helpers.SecurityUtils;
import com.exadel.frs.mapper.UserMapper;
import com.exadel.frs.service.AppService;
import com.exadel.frs.service.OrganizationService;
import com.exadel.frs.service.UserService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final OrganizationService organizationService;
    private final UserMapper userMapper;
    private final AppService appService;

    private Environment env;

    @Autowired
    public void setEnv(Environment env) {
        this.env = env;
    }

    @GetMapping("/me")
    @ApiOperation(value = "Get information about user, that logged in")
    public UserResponseDto getUser() {
        try {
            val user = userService.getUser(SecurityUtils.getPrincipalId());
            return userMapper.toResponseDto(user);
        } catch (UserDoesNotExistException e) {
            throw new AccessDeniedException();
        } catch (Exception e) {
            throw e;
        }
    }

    @PostMapping("/register")
    @ApiOperation(value = "Register new user")
    @ApiResponses({
            @ApiResponse(code = 400, message = "Such username or email already registered | " +
                    "One or more of required fields are empty | " +
                    "Incorrect email format"),
            @ApiResponse(code = 200, message = "200 Means user created, but not confirmed"),
            @ApiResponse(code = 201, message = "201 means user created and enabled")
    })
    public ResponseEntity createUser(
            @ApiParam(value = "User object that needs to be created", required = true)
            @RequestBody final UserCreateDto userCreateDto
    ) {
        val user = userService.createUser(userCreateDto);
        organizationService.addUserToDefaultOrg(user.getEmail());

        if (user.isEnabled()) {
            return new ResponseEntity(CREATED);
        } else {
            return new ResponseEntity(OK);
        }
    }

    @PutMapping("/update")
    @ApiOperation(value = "Update user data")
    @ApiResponses({
            @ApiResponse(code = 400, message = "Such username or email already registered")
    })
    public UserResponseDto updateUser(
            @ApiParam(value = "User data that needs to be updated", required = true)
            @RequestBody
            @Valid final UserUpdateDto userUpdateDto
    ) {
        return userMapper.toResponseDto(userService.updateUser(userUpdateDto, SecurityUtils.getPrincipalId()));
    }

    @DeleteMapping("/delete/{userGuid}")
    @ApiOperation(value = "Delete user")
    public void deleteUser(
            @ApiParam(value = "GUID of the user being deleted", required = true, example = GUID_EXAMPLE)
            @PathVariable final String userGuid,
            @ApiParam(value = "Replacer option to determine next owner of org/apps that the user own", allowableValues = "deleter, owner")
            @RequestParam(defaultValue = "deleter") final String replacer
    ) {
        val deleteUserDto = UserDeleteDto.builder()
                                         .deleter(SecurityUtils.getPrincipal())
                                         .userToDelete(userService.getUserByGuid(userGuid))
                                         .replacer(replacer)
                                         .defaultOrg(organizationService.getDefaultOrg())
                                         .updateAppsConsumer(appService::passAllOwnedAppsToNewOwnerAndLeave)
                                         .build();

        userService.deleteUser(deleteUserDto);
    }

    @GetMapping("/autocomplete")
    @ApiOperation(value = "User autocomplete by (email, first name or last name)")
    public UserAutocompleteDto autocomplete(@RequestParam final String query) {
        val results = userMapper.toResponseDto(userService.autocomplete(query));

        return UserAutocompleteDto.builder()
                                  .length(results.size())
                                  .query(query)
                                  .results(results)
                                  .build();
    }

    @GetMapping("/registration/confirm")
    @ApiOperation("Confirm user registration token")
    public void confirmRegistration(@RequestParam final String token, final HttpServletResponse response) throws IOException {
        userService.confirmRegistration(token);
        redirectToHomePage(response);
    }

    private void redirectToHomePage(final HttpServletResponse response) throws IOException {
        response.setStatus(FOUND.value());
        val url = "https://" + env.getProperty("host.frs");
        response.sendRedirect(url);
    }
}