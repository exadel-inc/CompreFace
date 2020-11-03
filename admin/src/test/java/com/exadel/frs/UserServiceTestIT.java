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

package com.exadel.frs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import com.exadel.frs.dto.ui.UserCreateDto;
import com.exadel.frs.exception.UserDoesNotExistException;
import com.exadel.frs.helpers.EmailSender;
import com.exadel.frs.repository.UserRepository;
import com.exadel.frs.service.UserService;
import java.util.UUID;
import liquibase.integration.spring.SpringLiquibase;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(properties = "spring.mail.enable=true")
class UserServiceTestIT {

    private static final String ENABLED_USER_EMAIL = "enabled_user@email.com";
    private static final String DISABLED_USER_EMAIL = "disabled_user@email.com";
    private static final String USER_EMAIL = "user@email.com";
    private static final String USER_EMAIL_2 = "user_2@email.com";
    private static final String USER_GUID = "testUserGuid";
    private static final String USER_EMAIL_PART = "user";

    @MockBean
    private SpringLiquibase springLiquibase;

    @MockBean
    private EmailSender emailSender;

    @SpyBean
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanDB() {
        deleteUserIfExists(ENABLED_USER_EMAIL);
        deleteUserIfExists(DISABLED_USER_EMAIL);
        deleteUserIfExists(USER_EMAIL);
        deleteUserIfExists(USER_EMAIL_2);
    }

    @Test
    void getEnabledUserByEmailReturnsActiveUser() {
        createAndEnableUser(ENABLED_USER_EMAIL);

        val enabledUser = userService.getEnabledUserByEmail(ENABLED_USER_EMAIL);

        assertThat(enabledUser).isNotNull();
        assertThat(enabledUser.isEnabled()).isTrue();
    }

    @Test
    void getEnabledUserByEmailThrowsExceptionIfUserIsDisabled() {
        createUser(DISABLED_USER_EMAIL);

        val disabledUser = userRepository.findByEmail(DISABLED_USER_EMAIL).get();

        assertThat(disabledUser).isNotNull();
        assertThat(disabledUser.isEnabled()).isFalse();

        assertThatThrownBy(() -> userService.getEnabledUserByEmail(
                DISABLED_USER_EMAIL
        )).isInstanceOf(UserDoesNotExistException.class);
    }

    @Test
    void getUserByEmailReturnsUser() {
        createUser(USER_EMAIL);

        val actual = userService.getUser(USER_EMAIL);

        assertThat(actual).isNotNull();
    }

    @Test
    void getUserByEmailThrowsExceptionIfNoUser() {
        assertThatThrownBy(() -> userService.getUser(
                USER_EMAIL
        )).isInstanceOf(UserDoesNotExistException.class);
    }

    @Test
    void getUserByGuidReturnsUser() {
        createUser(USER_EMAIL);
        val createdUser = userRepository.findByEmail(USER_EMAIL).get();

        val actual = userService.getUserByGuid(createdUser.getGuid());

        assertThat(actual).isNotNull();
    }

    @Test
    void getUserByGuidThrowsExceptionIfNoUser() {
        assertThatThrownBy(() -> userService.getUserByGuid(
                USER_GUID
        )).isInstanceOf(UserDoesNotExistException.class);
    }

    @Test
    void autocompleteReturnsEmptyList() {
        val actual = userService.autocomplete("");

        assertThat(actual).isEmpty();
    }

    @Test
    void autocompleteReturnsUsers() {
        createUser(USER_EMAIL);
        createUser(USER_EMAIL_2);

        val actual = userService.autocomplete(USER_EMAIL_PART);

        assertThat(actual).hasSize(2);
    }

    private void createAndEnableUser(final String email) {
        val regToken = UUID.randomUUID().toString();
        when(userService.generateRegistrationToken()).thenReturn(regToken);
        createUser(email);
        confirmRegistration(regToken);
    }

    private void createUser(final String email) {
        val user = UserCreateDto.builder()
                                .email(email)
                                .firstName("first_name")
                                .lastName("last_name")
                                .password("password")
                                .build();

        userService.createUser(user);
    }

    private void confirmRegistration(final String regToken) {
        userService.confirmRegistration(regToken);
    }

    private void deleteUserIfExists(final String email) {
        val user = userRepository.findByEmail(email);
        user.ifPresent(value -> userRepository.delete(value));
    }
}