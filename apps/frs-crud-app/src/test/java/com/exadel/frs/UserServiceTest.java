package com.exadel.frs;

import com.exadel.frs.dto.ui.UserCreateDto;
import com.exadel.frs.dto.ui.UserUpdateDto;
import com.exadel.frs.entity.User;
import com.exadel.frs.exception.EmailAlreadyRegisteredException;
import com.exadel.frs.exception.EmptyRequiredFieldException;
import com.exadel.frs.exception.UserDoesNotExistException;
import com.exadel.frs.repository.UserRepository;
import com.exadel.frs.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepositoryMock;
    private UserService userService;

    UserServiceTest() {
        userRepositoryMock = mock(UserRepository.class);
        userService = new UserService(userRepositoryMock, PasswordEncoderFactories.createDelegatingPasswordEncoder());
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

        Assertions.assertThrows(UserDoesNotExistException.class, () -> userService.getUser(userId));
    }

    @Test
    void successCreateUser() {
        UserCreateDto userCreateDto = UserCreateDto.builder()
                .email("email")
                .password("password")
                .firstName("firstName")
                .lastName("lastName")
                .build();

        userService.createUser(userCreateDto);

        verify(userRepositoryMock).save(any(User.class));
    }

    @Test
    void failCreateUserEmptyPassword() {
        UserCreateDto userCreateDto = UserCreateDto.builder()
                .email("email")
                .password("")
                .firstName("firstName")
                .lastName("lastName")
                .build();

        Assertions.assertThrows(EmptyRequiredFieldException.class, () -> userService.createUser(userCreateDto));
    }

    @Test
    void failCreateUserEmptyEmail() {
        UserCreateDto userCreateDto = UserCreateDto.builder()
                .email("")
                .password("password")
                .firstName("firstName")
                .lastName("lastName")
                .build();

        Assertions.assertThrows(EmptyRequiredFieldException.class, () -> userService.createUser(userCreateDto));
    }

    @Test
    void failCreateUserDuplicateEmail() {
        UserCreateDto userCreateDto = UserCreateDto.builder()
                .email("email")
                .password("password")
                .firstName("firstName")
                .lastName("lastName")
                .build();

        when(userRepositoryMock.existsByEmail(anyString())).thenReturn(true);

        Assertions.assertThrows(EmailAlreadyRegisteredException.class, () -> userService.createUser(userCreateDto));
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

}
