package com.exadel.frs.service;

import com.exadel.frs.dto.ui.UserCreateDto;
import com.exadel.frs.dto.ui.UserUpdateDto;
import com.exadel.frs.entity.User;
import com.exadel.frs.exception.EmailAlreadyRegisteredException;
import com.exadel.frs.exception.EmptyRequiredFieldException;
import com.exadel.frs.exception.InvalidEmailException;
import com.exadel.frs.exception.UserDoesNotExistException;
import com.exadel.frs.helpers.EmailSender;
import com.exadel.frs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.exadel.frs.validation.EmailValidator.isInvalid;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@EnableScheduling
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final EmailSender emailSender;
    @Autowired
    private Environment env;

    public User getUser(final Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserDoesNotExistException(id.toString()));
    }

    public User getUser(final String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserDoesNotExistException(email));
    }

    public User getUserByGuid(final String guid) {
        return userRepository.findByGuid(guid)
                .orElseThrow(() -> new UserDoesNotExistException(guid));
    }

    @Transactional
    public User createUser(final UserCreateDto userCreateDto) {
        validateUserCreateDto(userCreateDto);
        User user = User.builder()
                .email(userCreateDto.getEmail())
                .firstName(userCreateDto.getFirstName())
                .lastName(userCreateDto.getLastName())
                .password(encoder.encode(userCreateDto.getPassword()))
                .guid(UUID.randomUUID().toString())
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(true)//TODO make it false when EFRS-330 is complete
                .registrationToken(UUID.randomUUID().toString())
                .build();

        sendRegistrationTokenToUser(user);

        return userRepository.save(user);
    }

    private void sendRegistrationTokenToUser(final User user) {
        val message = "Please, confirm your registration clicking the link below:\n"
                        + "https://"
                        + env.getProperty("host.frs")
                        + "/admin/user/registration/confirm?token="
                        + user.getRegistrationToken();

        val subject = "Exadel FRS Registration";
        emailSender.sendMail(user.getEmail(), subject, message);
    }

    private void validateUserCreateDto(UserCreateDto userCreateDto) {
        if (isBlank(userCreateDto.getEmail())) {
            throw new EmptyRequiredFieldException("email");
        }

        if (isInvalid(userCreateDto.getEmail())) {
            throw new InvalidEmailException();
        }

        if (isBlank(userCreateDto.getPassword())) {
            throw new EmptyRequiredFieldException("password");
        }

        if (isBlank(userCreateDto.getFirstName())) {
            throw new EmptyRequiredFieldException("first name");
        }

        if (isBlank(userCreateDto.getLastName())) {
            throw new EmptyRequiredFieldException("last name");
        }

        if (userRepository.existsByEmail(userCreateDto.getEmail())) {
            throw new EmailAlreadyRegisteredException();
        }
    }

    public void updateUser(final UserUpdateDto userUpdateDto, final Long userId) {
        User user = getUser(userId);
        if (!StringUtils.isEmpty(userUpdateDto.getFirstName())) {
            user.setFirstName(userUpdateDto.getFirstName());
        }
        if (!StringUtils.isEmpty(userUpdateDto.getLastName())) {
            user.setLastName(userUpdateDto.getLastName());
        }
        if (!StringUtils.isEmpty(userUpdateDto.getPassword())) {
            user.setPassword(encoder.encode(userUpdateDto.getPassword()));
        }
        userRepository.save(user);
    }

    public void deleteUser(final Long id) {
        userRepository.deleteById(id);
    }

    public List<User> autocomplete(final String query) {
        if (isBlank(query)) {
            return new ArrayList<>();
        }

        val hqlParameter = query + "%";

        return userRepository.autocomplete(hqlParameter);
    }

    @Scheduled(fixedDelayString = "${registration.token.expires}")
    @Transactional
    public void removeExpiredRegistrationTokens() {
        int registrationExpireTime = env.getProperty("registration.token.expires", Integer.class) / 1000;
        val seconds = LocalDateTime
                                    .now()
                                    .minusSeconds(registrationExpireTime);

        userRepository.deleteByEnabledFalseAndRegTimeBefore(seconds);
    }
}