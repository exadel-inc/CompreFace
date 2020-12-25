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

import static com.exadel.frs.utils.TestUtils.buildUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.exadel.frs.dto.ui.UserAutocompleteDto;
import com.exadel.frs.dto.ui.UserCreateDto;
import com.exadel.frs.dto.ui.UserResponseDto;
import com.exadel.frs.dto.ui.UserUpdateDto;
import com.exadel.frs.entity.User;
import com.exadel.frs.exception.AccessDeniedException;
import com.exadel.frs.exception.ConstraintViolationException;
import com.exadel.frs.exception.EmptyRequiredFieldException;
import com.exadel.frs.exception.UserDoesNotExistException;
import com.exadel.frs.mapper.UserGlobalRoleMapper;
import com.exadel.frs.mapper.UserMapper;
import com.exadel.frs.service.AppService;
import com.exadel.frs.service.ModelService;
import com.exadel.frs.service.UserService;
import com.exadel.frs.system.security.config.AuthServerConfig;
import com.exadel.frs.system.security.config.ResourceServerConfig;
import com.exadel.frs.system.security.config.WebSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = { WebSecurityConfig.class, AuthServerConfig.class, ResourceServerConfig.class}
        )
)
public class UserControllerTest {

    private static final String USER_GUID = "user-guid";

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private AppService appService;

    @MockBean
    private ModelService modelService;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserGlobalRoleMapper userGlobalRoleMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnErrorMessageWhenUpdateFirstNameIsEmpty() throws Exception {
        val expectedContent = "{\"message\":\"" + String.format(EmptyRequiredFieldException.MESSAGE, "firstName") + "\",\"code\":5}";
        val bodyWithEmptyFirstName = new UserUpdateDto();
        bodyWithEmptyFirstName.setLastName("gdsag");
        bodyWithEmptyFirstName.setPassword("adsfadsfasg");

        val createNewModelRequest = put("/user/update")
                .with(csrf())
                .with(user(buildUser()))
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(createNewModelRequest.content(mapper.writeValueAsString(bodyWithEmptyFirstName)))
               .andExpect(status().isBadRequest())
               .andExpect(content().string(expectedContent));
    }

    @Test
    void shouldReturnErrorMessageWhenUpdateLastNameIsEmpty() throws Exception {
        val expectedContent = "{\"message\":\"" + String.format(EmptyRequiredFieldException.MESSAGE, "lastName") + "\",\"code\":5}";
        val bodyWithEmptyLastName = new UserUpdateDto();
        bodyWithEmptyLastName.setFirstName("gdsag");
        bodyWithEmptyLastName.setPassword("adsfadsfasg");

        val createNewModelRequest = put("/user/update")
                .with(csrf())
                .with(user(buildUser()))
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(createNewModelRequest.content(mapper.writeValueAsString(bodyWithEmptyLastName)))
               .andExpect(status().isBadRequest())
               .andExpect(content().string(expectedContent));
    }

    @Test
    void shouldReturnErrorMessageWhenUpdatePasswordIsEmpty() throws Exception {
        val expectedContent = "{\"message\":\"" + String.format(EmptyRequiredFieldException.MESSAGE, "password") + "\",\"code\":5}";
        val bodyWithEmptyPassword = new UserUpdateDto();
        bodyWithEmptyPassword.setLastName("gdsag");
        bodyWithEmptyPassword.setFirstName("test");

        val createNewModelRequest = put("/user/update")
                .with(csrf())
                .with(user(buildUser()))
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(createNewModelRequest.content(mapper.writeValueAsString(bodyWithEmptyPassword)))
               .andExpect(status().isBadRequest())
               .andExpect(content().string(expectedContent));
    }

    @Test
    void shouldReturnErrorMessageWhenUpdatePasswordTooSmall() throws Exception {
        val expectedContent = "{\"message\":\"" + String.format(
                ConstraintViolationException.MESSAGE,
                "password",
                "size must be between 8 and 255"
        ) + "\"," +
                "\"code" +
                "\":26}";

        val bodyWithShortPassword = new UserUpdateDto();
        bodyWithShortPassword.setLastName("gdsag");
        bodyWithShortPassword.setFirstName("test");
        bodyWithShortPassword.setPassword("aaaa");

        val createNewModelRequest = put("/user/update")
                .with(csrf())
                .with(user(buildUser()))
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(createNewModelRequest.content(mapper.writeValueAsString(bodyWithShortPassword)))
               .andExpect(status().isBadRequest())
               .andExpect(content().string(expectedContent));
    }

    @Test
    void shouldReturnUpdatedUser() throws Exception {
        val updateDto = new UserUpdateDto();
        updateDto.setLastName("gdsag");
        updateDto.setFirstName("test");
        updateDto.setPassword("test-password");

        val createRequest = put("/user/update")
                .with(csrf())
                .with(user(buildUser()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateDto));

        val responseDto = new UserResponseDto();
        responseDto.setFirstName(updateDto.getFirstName());
        responseDto.setLastName(updateDto.getLastName());

        when(userService.updateUser(any(UserUpdateDto.class), anyLong())).thenReturn(new User());
        when(userMapper.toResponseDto(any(User.class))).thenReturn(responseDto);

        mockMvc.perform(createRequest)
               .andExpect(status().isOk())
               .andExpect(content().string(mapper.writeValueAsString(responseDto)));
    }

    @Test
    void shouldReturnOkWhenDeleteUser() throws Exception {
        val updateDto = new UserUpdateDto();
        updateDto.setLastName("gdsag");
        updateDto.setFirstName("test");
        updateDto.setPassword("test-password");

        val deleteRequest = delete("/user/" + USER_GUID)
                .with(csrf())
                .with(user(buildUser()))
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(deleteRequest)
               .andExpect(status().isOk());

        verify(userService).getUser(anyLong());
        verify(userService).getUserByGuid(eq(USER_GUID));
        verify(userService).deleteUser(any());
        verifyNoMoreInteractions(userService);
        verifyNoInteractions(appService);
    }

    @Test
    void shouldReturnAutocomplete() throws Exception {
        val createRequest = get("/user/autocomplete")
                .with(csrf())
                .with(user(buildUser()))
                .contentType(MediaType.APPLICATION_JSON)
                .param("query", "query-string");

        val responseDto = new UserResponseDto();
        responseDto.setFirstName("name-test");
        val responseDtoList = List.of(responseDto, responseDto);

        when(userService.autocomplete(eq("query-string"))).thenReturn(List.of(new User()));
        when(userMapper.toResponseDto(anyList())).thenReturn(responseDtoList);

        val expectedResult = UserAutocompleteDto.builder()
                                                .length(responseDtoList.size())
                                                .query("query-string")
                                                .results(responseDtoList)
                                                .build();

        mockMvc.perform(createRequest)
               .andExpect(status().isOk())
               .andExpect(content().string(mapper.writeValueAsString(expectedResult)));
    }

    @Test
    void shouldReturnSendRedirect() throws Exception {
        val createRequest = get("/user/registration/confirm")
                .with(csrf())
                .with(user(buildUser()))
                .contentType(MediaType.APPLICATION_JSON)
                .param("token", "token-test");

        mockMvc.perform(createRequest)
               .andExpect(status().isFound())
               .andExpect(redirectedUrlPattern("http:/**"));
    }

    @Test
    void shouldReturnErrorMessageWhenNoUser() throws Exception {
        val expectedContent = "{\"message\":\"" + AccessDeniedException.MESSAGE + "\",\"code\":1}";

        val createNewModelRequest = get("/user/me")
                .with(csrf())
                .with(user(buildUser()))
                .contentType(MediaType.APPLICATION_JSON);

        when(userService.getUser(anyLong())).thenThrow(new UserDoesNotExistException("user-id"));

        mockMvc.perform(createNewModelRequest)
               .andExpect(status().isForbidden())
               .andExpect(content().string(expectedContent));
    }

    @Test
    void shouldReturnOkWhenUserNotEnabled() throws Exception {
        val createDto = new UserCreateDto("email", "name", "last", "password");

        val createRequest = post("/user/register")
                .with(csrf())
                .with(user(buildUser()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createDto));

        when(userService.createUser(any())).thenReturn(User.builder()
                                                           .enabled(false)
                                                           .build());

        mockMvc.perform(createRequest)
               .andExpect(status().isOk());
    }
}