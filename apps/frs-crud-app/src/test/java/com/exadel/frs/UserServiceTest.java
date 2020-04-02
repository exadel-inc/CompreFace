package com.exadel.frs;

import com.exadel.frs.dto.ui.UserCreateDto;
import com.exadel.frs.dto.ui.UserUpdateDto;
import com.exadel.frs.entity.User;
import com.exadel.frs.exception.EmailAlreadyRegisteredException;
import com.exadel.frs.exception.EmptyRequiredFieldException;
import com.exadel.frs.exception.InvalidEmailException;
import com.exadel.frs.exception.UserDoesNotExistException;
import com.exadel.frs.helpers.EmailSender;
import com.exadel.frs.repository.UserRepository;
import com.exadel.frs.service.UserService;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private UserRepository userRepositoryMock;
    private UserService userService;
    private EmailSender emailSenderMock;

    UserServiceTest() {
        userRepositoryMock = mock(UserRepository.class);
        emailSenderMock = mock(EmailSender.class);
        userService = new UserService(userRepositoryMock, PasswordEncoderFactories.createDelegatingPasswordEncoder(), emailSenderMock);
        userService.setEnv(new MockEnvironment());
    }

    @Test
    void successGetUser() {
        Long userId = 1L;

        User user = User.builder().id(userId).build();

        when(userRepositoryMock.findById(anyLong())).thenReturn(Optional.of(user));

        User result = userService.getUser(userId);

        assertThat(result.getId(), is(userId));
    }

    @Test
    void failGetUser() {
        Long userId = 1L;

        when(userRepositoryMock.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserDoesNotExistException.class, () -> userService.getUser(userId));
    }

    @Test
    void successCreateUser() {
        UserCreateDto userCreateDto = UserCreateDto.builder()
                .email("email@example.com")
                .password("password")
                .firstName("firstName")
                .lastName("lastName")
                .build();

        userService.createUser(userCreateDto);

        verify(emailSenderMock).sendMail(anyString(), anyString(), anyString());
        verify(userRepositoryMock).save(any(User.class));
    }

    @Test
    void failCreateUserEmptyPassword() {
        UserCreateDto userCreateDto = UserCreateDto.builder()
                .email("email@example.com")
                .password("")
                .firstName("firstName")
                .lastName("lastName")
                .build();

        assertThrows(EmptyRequiredFieldException.class, () -> userService.createUser(userCreateDto));
    }

    @Test
    void failCreateUserEmptyEmail() {
        UserCreateDto userCreateDto = UserCreateDto.builder()
                .email("")
                .password("password")
                .firstName("firstName")
                .lastName("lastName")
                .build();

        assertThrows(EmptyRequiredFieldException.class, () -> userService.createUser(userCreateDto));
    }

    @Test
    void failCreateUserDuplicateEmail() {
        UserCreateDto userCreateDto = UserCreateDto.builder()
                .email("email@example.com")
                .password("password")
                .firstName("firstName")
                .lastName("lastName")
                .build();

        when(userRepositoryMock.existsByEmail(anyString())).thenReturn(true);

        assertThrows(EmailAlreadyRegisteredException.class, () -> userService.createUser(userCreateDto));
    }

    @Test
    void successUpdateUser() {
        Long userId = 1L;

        User repoUser = User.builder()
                .id(userId)
                .email("email")
                .password("password")
                .firstName("firstName")
                .lastName("lastName")
                .build();

        UserUpdateDto userUpdateDto = UserUpdateDto.builder()
                .password("password")
                .firstName("firstName")
                .lastName("lastName")
                .build();

        when(userRepositoryMock.findById(anyLong())).thenReturn(Optional.of(repoUser));

        userService.updateUser(userUpdateDto, userId);

        verify(userRepositoryMock).save(any(User.class));

        assertThat(repoUser.getPassword(), not(userUpdateDto.getPassword()));
        assertThat(repoUser.getFirstName(), is(userUpdateDto.getFirstName()));
        assertThat(repoUser.getLastName(), is(userUpdateDto.getLastName()));
    }

    @Test
    void successDeleteUser() {
        Long userId = 1L;

        userService.deleteUser(userId);

        verify(userRepositoryMock).deleteById(anyLong());
    }

    @Test
    void cannotCreateNewUserWithIncorrectEmail() {
        val userWithIncorrectEmial = UserCreateDto.builder()
                .email("wrong_email")
                .password("password")
                .firstName("firstName")
                .lastName("lastName")
                .build();

        assertThrows(InvalidEmailException.class, () -> userService.createUser(userWithIncorrectEmial));
    }

    @Test
    void cannotCreateNewUserWithoutFirstName() {
        val userWithoutFirstName = UserCreateDto.builder()
                .email("email@example.com")
                .password("password")
                .firstName(null)
                .lastName("lastName")
                .build();

        assertThrows(EmptyRequiredFieldException.class, () -> userService.createUser(userWithoutFirstName));
    }

    @Test
    void cannotCreateNewUserWithoutLastName() {
        val userWithoutFirstName = UserCreateDto.builder()
                .email("email@example.com")
                .password("password")
                .firstName("firstName")
                .lastName(null)
                .build();

        assertThrows(EmptyRequiredFieldException.class, () -> userService.createUser(userWithoutFirstName));
    }
}
