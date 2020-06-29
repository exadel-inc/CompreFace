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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.exadel.frs.dto.ui.UserUpdateDto;
import com.exadel.frs.exception.ConstraintViolationException;
import com.exadel.frs.exception.EmptyRequiredFieldException;
import com.exadel.frs.mapper.UserMapper;
import com.exadel.frs.service.AppService;
import com.exadel.frs.service.OrganizationService;
import com.exadel.frs.service.UserService;
import com.exadel.frs.system.security.JwtAuthenticationFilter;
import com.exadel.frs.system.security.config.AuthServerConfig;
import com.exadel.frs.system.security.config.ResourceServerConfig;
import com.exadel.frs.system.security.config.WebSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtAuthenticationFilter.class, WebSecurityConfig.class, AuthServerConfig.class, ResourceServerConfig.class}
        )
)
@MockBeans({
        @MockBean(UserMapper.class),
        @MockBean(UserService.class),
        @MockBean(AppService.class),
        @MockBean(OrganizationService.class)
})
public class UserControllerTest {

    @Autowired
    private ObjectMapper mapper;

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
}