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

import static com.exadel.frs.commonservice.enums.AppRole.OWNER;
import static com.exadel.frs.commonservice.enums.AppRole.USER;
import static com.exadel.frs.system.global.Constants.ADMIN;
import static com.exadel.frs.utils.TestUtils.USER_ID;
import static com.exadel.frs.utils.TestUtils.buildExceptionResponse;
import static com.exadel.frs.utils.TestUtils.buildUndefinedExceptionResponse;
import static com.exadel.frs.utils.TestUtils.buildUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.exadel.frs.commonservice.exception.BasicException;
import com.exadel.frs.dto.AppCreateDto;
import com.exadel.frs.dto.AppResponseDto;
import com.exadel.frs.dto.AppUpdateDto;
import com.exadel.frs.dto.UserInviteDto;
import com.exadel.frs.dto.UserRoleResponseDto;
import com.exadel.frs.dto.UserRoleUpdateDto;
import com.exadel.frs.commonservice.entity.App;
import com.exadel.frs.commonservice.entity.UserAppRole;
import com.exadel.frs.commonservice.enums.AppRole;
import com.exadel.frs.exception.AppNotFoundException;
import com.exadel.frs.mapper.AppMapper;
import com.exadel.frs.mapper.UserAppRoleMapper;
import com.exadel.frs.service.AppService;
import com.exadel.frs.system.security.config.AuthServerConfig;
import com.exadel.frs.system.security.config.ResourceServerConfig;
import com.exadel.frs.system.security.config.WebSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = AppController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        WebSecurityConfig.class,
                        AuthServerConfig.class,
                        ResourceServerConfig.class
                })
)
class AppControllerTest {

    @MockBean
    private AppService appService;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppMapper appMapper;

    @MockBean
    private UserAppRoleMapper userAppRoleMapper;

    private ObjectMapper mapper = new ObjectMapper();

    private static final long APP_ID = 1L;
    private static final String APP_GUID = "app-guid";
    private static final String ORG_GUID = "org-guid";
    private static final String USER_GUID = "user-id";
    private static final String APP_NAME = "test-app";

    @Test
    public void shouldReturnMessageAndCodeWhenAppNotFoundExceptionThrown() throws Exception {
        final BasicException expectedException = new AppNotFoundException(APP_GUID);

        when(appService.getApp(APP_GUID, USER_ID)).thenThrow(expectedException);

        String expectedContent = mapper.writeValueAsString(buildExceptionResponse(expectedException));
        mockMvc.perform(get(ADMIN + "/app/" + APP_GUID).with(user(buildUser())))
               .andExpect(status().isNotFound())
               .andExpect(content().string(expectedContent));
    }

    @Test
    public void shouldReturnMessageAndCodeWhenUnexpectedExceptionThrown() throws Exception {
        final Exception expectedException = new NullPointerException("Something went wrong, please try again");

        when(appService.getApps(USER_ID)).thenThrow(expectedException);

        String expectedContent = mapper.writeValueAsString(buildUndefinedExceptionResponse(expectedException));
        mockMvc.perform(get(ADMIN + "/apps").with(user(buildUser())))
               .andExpect(status().isBadRequest())
               .andExpect(content().string(expectedContent));
    }

    @Test
    public void shouldReturnMessageAndCodeWhenAppNameIsMissing() throws Exception {
        val request = post(ADMIN + "/app")
                .with(csrf())
                .with(user(buildUser()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(
                        App.builder()
                           .id(APP_ID)
                           .build()
                        )
                );

        val expectedContent = "{\"message\":\"Application name cannot be empty\",\"code\":26}";

        mockMvc.perform(request)
               .andExpect(status().isBadRequest())
               .andExpect(content().string(expectedContent));
    }

    @Test
    public void shouldReturn400AndErrorMessageWhenRenameAppToEmpty() throws Exception {
        val expectedContent = "{\"message\":\"Application name cannot be empty\",\"code\":26}";

        val bodyWithEmptyName = new AppUpdateDto();
        bodyWithEmptyName.setName(null);
        val bodyWithNoName = new AppUpdateDto();

        val updateRequest = put(ADMIN + "/app/" + APP_GUID)
                .with(csrf())
                .with(user(buildUser()))
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(updateRequest.content(mapper.writeValueAsString(bodyWithEmptyName)))
               .andExpect(status().isBadRequest())
               .andExpect(content().string(expectedContent));

        mockMvc.perform(updateRequest.content(mapper.writeValueAsString(bodyWithNoName)))
               .andExpect(status().isBadRequest())
               .andExpect(content().string(expectedContent));
    }

    @ParameterizedTest
    @ValueSource(strings = {APP_NAME, "_[my_new app.]_"})
    public void shouldReturnNewApp() throws Exception {
        val appCreateDto = AppCreateDto.builder()
                                       .name(APP_NAME)
                                       .build();

        val request = post(ADMIN + "/app")
                .with(csrf())
                .with(user(buildUser()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(appCreateDto));

        val app = App.builder()
                     .name(appCreateDto.getName())
                     .build();

        val appResponseDto = new AppResponseDto();
        appResponseDto.setName(APP_NAME);

        when(appService.createApp(any(), anyLong())).thenReturn(app);
        when(appMapper.toResponseDto(any(App.class), anyLong())).thenReturn(appResponseDto);

        mockMvc.perform(request)
               .andExpect(status().isCreated())
               .andExpect(content().string(mapper.writeValueAsString(appResponseDto)));
    }

    @Test
    public void shouldReturnUpdatedApp() throws Exception {
        val appCreateDto = AppCreateDto.builder()
                                       .name(APP_NAME)
                                       .build();

        val request = put(ADMIN + "/app/" + APP_GUID)
                .with(csrf())
                .with(user(buildUser()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(appCreateDto));

        val app = App.builder()
                     .name(appCreateDto.getName())
                     .build();

        val appResponseDto = new AppResponseDto();
        appResponseDto.setName(APP_NAME);

        when(appService.updateApp(any(AppUpdateDto.class), eq(APP_GUID), anyLong())).thenReturn(app);
        when(appMapper.toResponseDto(any(App.class), anyLong())).thenReturn(appResponseDto);

        mockMvc.perform(request)
               .andExpect(status().isOk())
               .andExpect(content().string(mapper.writeValueAsString(appResponseDto)));
    }

    @Test
    public void shouldReturnUpdatedWithApiKeyApp() throws Exception {
        val request = put(ADMIN + "/app/" + APP_GUID + "/apikey")
                .with(csrf())
                .with(user(buildUser()))
                .contentType(MediaType.APPLICATION_JSON);

        val app = App.builder()
                     .name(APP_NAME)
                     .build();

        val appResponseDto = new AppResponseDto();
        appResponseDto.setName(APP_NAME);

        when(appService.getApp(eq(APP_GUID), anyLong())).thenReturn(app);
        when(appMapper.toResponseDto(any(App.class), anyLong())).thenReturn(appResponseDto);

        mockMvc.perform(request)
               .andExpect(status().isOk())
               .andExpect(content().string(mapper.writeValueAsString(appResponseDto)));
    }

    @Test
    public void shouldReturnOkWhenDelete() throws Exception {
        val request = delete(ADMIN + "/app/" + APP_GUID)
                .with(csrf())
                .with(user(buildUser()))
                .contentType(MediaType.APPLICATION_JSON);

        doNothing().when(appService).deleteApp(eq(APP_GUID), anyLong());

        mockMvc.perform(request)
               .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnGlobalRolesToAssign() throws Exception {
        val request = get(ADMIN + "/app/" + APP_GUID + "/assign-roles")
                .with(csrf())
                .with(user(buildUser()))
                .contentType(MediaType.APPLICATION_JSON);

        val rolesToAssign = new AppRole[]{OWNER, USER};

        when(appService.getAppRolesToAssign(eq(APP_GUID), anyLong())).thenReturn(rolesToAssign);

        mockMvc.perform(request)
               .andExpect(status().isOk())
               .andExpect(content().string(mapper.writeValueAsString(rolesToAssign)));
    }

    @Test
    public void shouldReturnAppUsers() throws Exception {
        val request = get(ADMIN + "/app/" + APP_GUID + "/roles")
                .with(csrf())
                .with(user(buildUser()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString("Search string"));

        val appUsers = List.of(UserAppRole.builder()
                                          .role(OWNER)
                                          .build()
        );
        val userRoleResponseDto = new UserRoleResponseDto();
        val userRoleResponseDtoList = List.of(userRoleResponseDto);

        when(appService.getAppUsers(eq("Search string"), eq(APP_GUID), anyLong())).thenReturn(appUsers);
        when(userAppRoleMapper.toUserRoleResponseDto(anyList())).thenReturn(userRoleResponseDtoList);

        mockMvc.perform(request)
               .andExpect(status().isOk())
               .andExpect(content().string(mapper.writeValueAsString(userRoleResponseDtoList)));
    }

    @Test
    public void shouldReturnInvitedUser() throws Exception {
        val appCreateDto = UserInviteDto.builder()
                                        .role(USER.name())
                                        .userEmail("email@test.com")
                                        .build();

        val request = post(ADMIN + "/app/" + APP_GUID + "/invite")
                .with(csrf())
                .with(user(buildUser()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(appCreateDto));

        val userAppRole = UserAppRole.builder()
                                     .role(USER)
                                     .build();

        val userRoleResponseDto = new UserRoleResponseDto();
        userRoleResponseDto.setRole(USER.getCode());

        when(appService.inviteUser(any(UserInviteDto.class), eq(APP_GUID), anyLong())).thenReturn(userAppRole);
        when(userAppRoleMapper.toUserRoleResponseDto(any(UserAppRole.class))).thenReturn(userRoleResponseDto);

        mockMvc.perform(request)
               .andExpect(status().isCreated())
               .andExpect(content().string(mapper.writeValueAsString(userRoleResponseDto)));
    }

    @Test
    public void shouldReturnUpdatedUserAppRole() throws Exception {
        val userRoleUpdateDto = UserRoleUpdateDto.builder()
                                                 .role(USER.name())
                                                 .userId(USER_GUID)
                                                 .build();

        val request = put(ADMIN + "/app/" + APP_GUID + "/role")
                .with(csrf())
                .with(user(buildUser()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(userRoleUpdateDto));

        val userAppRole = UserAppRole.builder()
                                     .role(USER)
                                     .build();

        val userRoleResponseDto = new UserRoleResponseDto();
        userRoleResponseDto.setRole(USER.name());

        when(appService.updateUserAppRole(any(UserRoleUpdateDto.class), eq(APP_GUID), anyLong())).thenReturn(userAppRole);
        when(userAppRoleMapper.toUserRoleResponseDto(any(UserAppRole.class))).thenReturn(userRoleResponseDto);

        mockMvc.perform(request)
               .andExpect(status().isOk())
               .andExpect(content().string(mapper.writeValueAsString(userRoleResponseDto)));
    }

    @Test
    public void shouldReturnOkWhenDeleteUserFromApp() throws Exception {
        val request = delete(ADMIN + "/app/" + APP_GUID + "/user/" + USER_GUID)
                .with(csrf())
                .with(user(buildUser()))
                .contentType(MediaType.APPLICATION_JSON);

        doNothing().when(appService).deleteUserFromApp(eq(USER_GUID), eq(APP_GUID), anyLong());

        mockMvc.perform(request)
               .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    public void shouldReturn400WhenTryingToSaveAppThatContainsSpecialCharactersWithinName() {
        var app = App.builder()
                     .id(APP_ID)
                     .name("\\new;app//")
                     .build();

        val request = post(ADMIN + "/app")
                .with(csrf())
                .with(user(buildUser()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(app)
                );

        val expectedContent = "{\"message\":\"The name cannot contain the following special characters: ';', '/', '\\\\'\",\"code\":36}";

        mockMvc.perform(request)
               .andExpect(status().isBadRequest())
               .andExpect(content().string(expectedContent));
    }
}
