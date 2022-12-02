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

package com.exadel.frs.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import com.exadel.frs.commonservice.entity.User;
import com.exadel.frs.commonservice.enums.GlobalRole;
import com.exadel.frs.commonservice.enums.Replacer;
import com.exadel.frs.commonservice.exception.EmptyRequiredFieldException;
import com.exadel.frs.commonservice.exception.IllegalReplacerException;
import com.exadel.frs.commonservice.repository.UserRepository;
import com.exadel.frs.dto.UserCreateDto;
import com.exadel.frs.dto.UserDeleteDto;
import com.exadel.frs.dto.UserUpdateDto;
import com.exadel.frs.exception.EmailAlreadyRegisteredException;
import com.exadel.frs.exception.IncorrectUserPasswordException;
import com.exadel.frs.exception.InvalidEmailException;
import com.exadel.frs.exception.RegistrationTokenExpiredException;
import com.exadel.frs.exception.UserDoesNotExistException;
import com.exadel.frs.helpers.EmailSender;
import com.exadel.frs.service.AppService;
import com.exadel.frs.service.UserService;
import com.exadel.frs.system.security.AuthorizationManager;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserServiceTest {

    private final String EXPIRED_TOKEN = "expired_token";
    private final Long USER_ID = 1L;

    @Mock
    private UserRepository userRepositoryMock;

    @Mock
    private EmailSender emailSenderMock;

    @Mock
    private Environment env;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthorizationManager authManager;

    @Mock
    private AppService appService;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void successGetUser() {
        val user = User.builder()
                       .id(USER_ID)
                       .build();

        when(userRepositoryMock.findById(anyLong())).thenReturn(Optional.of(user));

        val actual = userService.getUser(USER_ID);

        assertThat(actual.getId()).isEqualTo(USER_ID);
    }

    @Test
    void failGetUser() {
        when(userRepositoryMock.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(
                USER_ID
        )).isInstanceOf(UserDoesNotExistException.class);
    }

    @Test
    void successCreateUserWhenMailServerEnabled() {
        when(env.getProperty("spring.mail.enable")).thenReturn("true");
        when(userRepositoryMock.save(any())).thenAnswer(returnsFirstArg());
        val userCreateDto = UserCreateDto.builder()
                                         .email("email@example.com")
                                         .password("password")
                                         .firstName("firstName")
                                         .lastName("lastName")
                                         .build();

        val actual = userService.createUser(userCreateDto);
        assertThat(actual.isEnabled()).isFalse();

        verify(emailSenderMock).sendMail(anyString(), anyString(), anyString());
        verify(userRepositoryMock).save(any(User.class));
    }

    @Test
    void successCreateUserWhenMailServerDisabled() {
        when(env.getProperty("spring.mail.enable")).thenReturn("false");
        when(userRepositoryMock.save(any())).thenAnswer(returnsFirstArg());
        val userCreateDto = UserCreateDto.builder()
                                         .email("email@example.com")
                                         .password("password")
                                         .firstName("firstName")
                                         .lastName("lastName")
                                         .build();

        val actual = userService.createUser(userCreateDto);
        assertThat(actual.isEnabled()).isTrue();

        verify(userRepositoryMock).existsByEmail(anyString());
        verify(userRepositoryMock).save(any(User.class));
        verifyNoMoreInteractions(userRepositoryMock);
        verifyNoInteractions(emailSenderMock);
    }

    @Test
    void failCreateUserEmptyPassword() {
        val userCreateDto = UserCreateDto.builder()
                                         .email("email@example.com")
                                         .password("")
                                         .firstName("firstName")
                                         .lastName("lastName")
                                         .build();

        assertThatThrownBy(() ->
                userService.createUser(userCreateDto)
        ).isInstanceOf(EmptyRequiredFieldException.class);
    }

    @Test
    void failCreateUserEmptyEmail() {
        val userCreateDto = UserCreateDto.builder()
                                         .email("")
                                         .password("password")
                                         .firstName("firstName")
                                         .lastName("lastName")
                                         .build();

        assertThatThrownBy(() ->
                userService.createUser(userCreateDto)
        ).isInstanceOf(EmptyRequiredFieldException.class);
    }

    @Test
    void failCreateUserDuplicateEmail() {
        val userCreateDto = UserCreateDto.builder()
                                         .email("email@example.com")
                                         .password("password")
                                         .firstName("firstName")
                                         .lastName("lastName")
                                         .build();

        when(userRepositoryMock.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() ->
                userService.createUser(userCreateDto)
        ).isInstanceOf(EmailAlreadyRegisteredException.class);
    }

    @Test
    void successUpdateUser() {
        val repoUser = User.builder()
                           .id(USER_ID)
                           .email("email")
                           .password("password")
                           .firstName("firstName")
                           .lastName("lastName")
                           .build();

        val userUpdateDto = UserUpdateDto.builder()
                                         .firstName("firstName")
                                         .lastName("lastName")
                                         .build();

        when(userRepositoryMock.findById(anyLong())).thenReturn(Optional.of(repoUser));

        userService.updateUser(userUpdateDto, USER_ID);

        assertThat(repoUser.getFirstName()).isEqualTo(userUpdateDto.getFirstName());
        assertThat(repoUser.getLastName()).isEqualTo(userUpdateDto.getLastName());

        verify(userRepositoryMock).save(any(User.class));
    }

    @Test
    void cannotCreateNewUserWithIncorrectEmail() {
        val userWithIncorrectEmial = UserCreateDto.builder()
                                                  .email("wrong_email")
                                                  .password("password")
                                                  .firstName("firstName")
                                                  .lastName("lastName")
                                                  .build();

        assertThatThrownBy(() ->
                userService.createUser(userWithIncorrectEmial)
        ).isInstanceOf(InvalidEmailException.class);
    }

    @Test
    void cannotCreateNewUserWithoutFirstName() {
        val userWithoutFirstName = UserCreateDto.builder()
                                                .email("email@example.com")
                                                .password("password")
                                                .firstName(null)
                                                .lastName("lastName")
                                                .build();

        assertThatThrownBy(() ->
                userService.createUser(userWithoutFirstName)
        ).isInstanceOf(EmptyRequiredFieldException.class);
    }

    @Test
    void cannotCreateNewUserWithoutLastName() {
        val userWithoutFirstName = UserCreateDto.builder()
                                                .email("email@example.com")
                                                .password("password")
                                                .firstName("firstName")
                                                .lastName(null)
                                                .build();

        assertThatThrownBy(() ->
                userService.createUser(userWithoutFirstName)
        ).isInstanceOf(EmptyRequiredFieldException.class);
    }

    @Test
    void confirmRegistrationReturns403WhenTokenIsExpired() {
        assertThatThrownBy(() ->
                userService.confirmRegistration(EXPIRED_TOKEN)
        ).isInstanceOf(RegistrationTokenExpiredException.class);
    }

    @Test
    void confirmRegistrationEnablesUserAndRemovesTokenWhenSuccess() {
        when(userRepositoryMock.save(any())).thenAnswer(returnsFirstArg());
        when(env.getProperty("spring.mail.enable")).thenReturn("true");
        val userCreateDto = UserCreateDto.builder()
                                         .email("email@example.com")
                                         .password("password")
                                         .firstName("firstName")
                                         .lastName("lastName")
                                         .build();

        val createdUser = userService.createUser(userCreateDto);
        assertThat(createdUser.isEnabled()).isFalse();

        when(userRepositoryMock.findByRegistrationToken(createdUser.getRegistrationToken())).thenReturn(Optional.of(createdUser));

        userService.confirmRegistration(createdUser.getRegistrationToken());

        assertThat(createdUser.isEnabled()).isTrue();
        assertThat(createdUser.getRegistrationToken()).isNull();
    }

    @Test
    void createsUserWithLowerCaseEmail() {
        when(userRepositoryMock.save(any())).thenAnswer(returnsFirstArg());
        val userCreateDto = UserCreateDto.builder()
                                         .email("Email@example.COm")
                                         .password("password")
                                         .firstName("firstName")
                                         .lastName("lastName")
                                         .build();

        val actual = userService.createUser(userCreateDto);

        assertThat(actual.getEmail()).isEqualTo(userCreateDto.getEmail().toLowerCase());
    }

    @Nested
    public class DeleteUserTest {

        final User globalOwner;
        final User globalAdmin;
        final User globalUser;
        final BiConsumer<User, User> updateAppsConsumer;

        public DeleteUserTest() {
            globalOwner = makeUser(1L);
            globalAdmin = makeUser(2L);
            globalUser = makeUser(3L);
            updateAppsConsumer =
                    (oldOwner, newOwner) -> appService.passAllOwnedAppsToNewOwnerAndLeaveAllApps(
                            oldOwner, newOwner
                    );
        }

        @Test
        void successDeleteUserWhenDeleterIsReplacer() {
            val deleteUserDto = UserDeleteDto.builder()
                                             .replacer(Replacer.from("deleter"))
                                             .userToDelete(globalUser)
                                             .deleter(globalAdmin)
                                             .updateAppsConsumer(updateAppsConsumer)
                                             .build();

            userService.deleteUser(deleteUserDto);

            verify(authManager).verifyCanDeleteUser(deleteUserDto);
            verify(appService).passAllOwnedAppsToNewOwnerAndLeaveAllApps(globalUser, globalAdmin);
            verify(userRepositoryMock).deleteByGuid(globalUser.getGuid());
        }

        @Test
        void successDeleteUserWhenGlobalOwnerIsReplacer() {
            val deleteUserDto = UserDeleteDto.builder()
                                             .replacer(Replacer.from("owner"))
                                             .userToDelete(globalUser)
                                             .deleter(globalAdmin)
                                             .updateAppsConsumer(updateAppsConsumer)
                                             .build();

            when(userRepositoryMock.findByGlobalRole(GlobalRole.OWNER)).thenReturn(globalOwner);

            userService.deleteUser(deleteUserDto);

            verify(authManager).verifyCanDeleteUser(deleteUserDto);
            verify(appService).passAllOwnedAppsToNewOwnerAndLeaveAllApps(globalUser, globalOwner);
            verify(userRepositoryMock).deleteByGuid(globalUser.getGuid());
        }

        @Test
        void exceptionWhenWrongReplacerParamIsPassed() {
            assertThatThrownBy(() -> UserDeleteDto.builder()
                                                  .replacer(Replacer.from("wrong_param"))
                                                  .userToDelete(globalUser)
                                                  .deleter(globalAdmin)
                                                  .updateAppsConsumer(updateAppsConsumer)
                                                  .build())
                    .isInstanceOf(IllegalReplacerException.class)
                    .hasMessage(String.format("Illegal replacer value=%s!", "wrong_param"));
        }

        private User makeUser(final long globalUserId) {
            return User.builder()
                       .id(globalUserId)
                       .guid(UUID.randomUUID().toString())
                       .build();
        }
    }

    @Nested
    class ChangePasswordTest {

        private static final String oldPwd = "old-pwd";
        private static final String newPwd = "new-pwd";
        private final Long userId = 1L;

        private final User user = User.builder()
                                      .password("old-pwd")
                                      .build();

        @Test
        void testChangePasswordNotExistingUser() {
            // given & when
            Executable action = () -> userService.changePassword(userId, oldPwd, newPwd);

            // then
            assertThrows(UserDoesNotExistException.class, action);
        }

        @Test
        void testChangePasswordIncorrectPassword() {
            // given
            when(userRepositoryMock.findById(userId)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

            //when
            Executable action = () -> userService.changePassword(userId, oldPwd, newPwd);

            // then
            assertThrows(IncorrectUserPasswordException.class, action);
        }

        @Test
        void testChangePassword() {
            // given
            when(userRepositoryMock.findById(userId)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            User newUser = User.builder()
                               .password(newPwd)
                               .build();

            //when
            userService.changePassword(userId, oldPwd, newPwd);

            // then
            verify(passwordEncoder).encode(newPwd);
            verify(userRepositoryMock).save(newUser);
        }
    }
}