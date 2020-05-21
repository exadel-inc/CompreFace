package com.exadel.frs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import com.exadel.frs.dto.ui.UserCreateDto;
import com.exadel.frs.dto.ui.UserUpdateDto;
import com.exadel.frs.entity.User;
import com.exadel.frs.exception.EmailAlreadyRegisteredException;
import com.exadel.frs.exception.EmptyRequiredFieldException;
import com.exadel.frs.exception.InvalidEmailException;
import com.exadel.frs.exception.RegistrationTokenExpiredException;
import com.exadel.frs.exception.UserDoesNotExistException;
import com.exadel.frs.helpers.EmailSender;
import com.exadel.frs.repository.UserRepository;
import com.exadel.frs.service.OrganizationService;
import com.exadel.frs.service.UserService;
import java.util.Optional;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
    private OrganizationService organizationServiceMock;

    @Mock
    private Environment env;

    @Mock
    private PasswordEncoder passwordEncoder;

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

        assertThrows(
                UserDoesNotExistException.class,
                () -> userService.getUser(USER_ID)
        );
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

        val createdUser = userService.createUser(userCreateDto);
        assertFalse(createdUser.isEnabled());

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

        val createdUser = userService.createUser(userCreateDto);
        assertTrue(createdUser.isEnabled());

        verifyNoInteractions(emailSenderMock);
        verify(userRepositoryMock).save(any(User.class));
    }

    @Test
    void failCreateUserEmptyPassword() {
        val userCreateDto = UserCreateDto.builder()
                                         .email("email@example.com")
                                         .password("")
                                         .firstName("firstName")
                                         .lastName("lastName")
                                         .build();

        assertThrows(
                EmptyRequiredFieldException.class,
                () -> userService.createUser(userCreateDto)
        );
    }

    @Test
    void failCreateUserEmptyEmail() {
        val userCreateDto = UserCreateDto.builder()
                                         .email("")
                                         .password("password")
                                         .firstName("firstName")
                                         .lastName("lastName")
                                         .build();

        assertThrows(
                EmptyRequiredFieldException.class,
                () -> userService.createUser(userCreateDto)
        );
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

        assertThrows(
                EmailAlreadyRegisteredException.class,
                () -> userService.createUser(userCreateDto)
        );
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
                                         .password("password")
                                         .firstName("firstName")
                                         .lastName("lastName")
                                         .build();

        when(userRepositoryMock.findById(anyLong())).thenReturn(Optional.of(repoUser));

        userService.updateUser(userUpdateDto, USER_ID);

        assertThat(repoUser.getPassword()).isNotEqualTo(userUpdateDto.getPassword());
        assertThat(repoUser.getFirstName()).isEqualTo(userUpdateDto.getFirstName());
        assertThat(repoUser.getLastName()).isEqualTo(userUpdateDto.getLastName());

        verify(userRepositoryMock).save(any(User.class));
    }

    @Test
    void successDeleteUser() {
        userService.deleteUser(USER_ID);

        verify(organizationServiceMock).getOwnedOrganizations(USER_ID);
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

        assertThrows(
                InvalidEmailException.class,
                () -> userService.createUser(userWithIncorrectEmial)
        );
    }

    @Test
    void cannotCreateNewUserWithoutFirstName() {
        val userWithoutFirstName = UserCreateDto.builder()
                                                .email("email@example.com")
                                                .password("password")
                                                .firstName(null)
                                                .lastName("lastName")
                                                .build();

        assertThrows(
                EmptyRequiredFieldException.class,
                () -> userService.createUser(userWithoutFirstName)
        );
    }

    @Test
    void cannotCreateNewUserWithoutLastName() {
        val userWithoutFirstName = UserCreateDto.builder()
                                                .email("email@example.com")
                                                .password("password")
                                                .firstName("firstName")
                                                .lastName(null)
                                                .build();

        assertThrows(
                EmptyRequiredFieldException.class,
                () -> userService.createUser(userWithoutFirstName)
        );
    }

    @Test
    void confirmRegistrationReturns403WhenTokenIsExpired() {
        assertThrows(
                RegistrationTokenExpiredException.class,
                () -> userService.confirmRegistration(EXPIRED_TOKEN)
        );
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
}