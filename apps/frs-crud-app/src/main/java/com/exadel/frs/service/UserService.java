package com.exadel.frs.service;

import static com.exadel.frs.validation.EmailValidator.isInvalid;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.StringUtils.isEmpty;
import com.exadel.frs.dto.ui.UserCreateDto;
import com.exadel.frs.dto.ui.UserUpdateDto;
import com.exadel.frs.entity.Organization;
import com.exadel.frs.entity.User;
import com.exadel.frs.exception.EmailAlreadyRegisteredException;
import com.exadel.frs.exception.EmptyRequiredFieldException;
import com.exadel.frs.exception.InvalidEmailException;
import com.exadel.frs.exception.RegistrationTokenExpiredException;
import com.exadel.frs.exception.UserDoesNotExistException;
import com.exadel.frs.helpers.EmailSender;
import com.exadel.frs.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.transaction.Transactional;
import lombok.NonNull;
import lombok.val;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final EmailSender emailSender;
    private final Environment env;
    private final OrganizationService organizationService;

    public UserService(
            @NonNull final UserRepository userRepository,
            @NonNull final PasswordEncoder encoder,
            @NonNull final EmailSender emailSender,
            @NonNull final Environment env,
            @Lazy
            @NonNull final OrganizationService organizationService
    ) {
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.emailSender = emailSender;
        this.env = env;
        this.organizationService = organizationService;
    }

    public User getUser(final Long id) {
        return userRepository.findById(id)
                             .orElseThrow(() -> new UserDoesNotExistException(id.toString()));
    }

    public User getUser(final String email) {
        return userRepository.findByEmail(email)
                             .orElseThrow(() -> new UserDoesNotExistException(email));
    }

    public User getEnabledUserByEmail(final String email) {
        return userRepository.findByEmailAndEnabledTrue(email)
                             .orElseThrow(() -> new UserDoesNotExistException(email));
    }

    public User getUserByGuid(final String guid) {
        return userRepository.findByGuid(guid)
                             .orElseThrow(() -> new UserDoesNotExistException(guid));
    }

    @Transactional
    public User createUser(final UserCreateDto userCreateDto) {
        val isMailServerEnabled = Boolean.valueOf(env.getProperty("spring.mail.enable"));
        validateUserCreateDto(userCreateDto);
        val isAccountEnabled = isMailServerEnabled ? false : true;
        val user = User.builder()
                .email(userCreateDto.getEmail().toLowerCase())
                .firstName(userCreateDto.getFirstName())
                .lastName(userCreateDto.getLastName())
                .password(encoder.encode(userCreateDto.getPassword()))
                .guid(UUID.randomUUID().toString())
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(isAccountEnabled)
                .registrationToken(generateRegistrationToken())
                .build();

        if (isMailServerEnabled) {
            sendRegistrationTokenToUser(user);
        }

        return userRepository.save(user);
    }

    public String generateRegistrationToken() {
        return UUID.randomUUID().toString();
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

    private void validateUserCreateDto(final UserCreateDto userCreateDto) {
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
        if (userRepository.existsByEmail(userCreateDto.getEmail().toLowerCase())) {
            throw new EmailAlreadyRegisteredException();
        }
    }

    public User updateUser(final UserUpdateDto userUpdateDto, final Long userId) {
        val user = getUser(userId);
        if (!isEmpty(userUpdateDto.getFirstName())) {
            user.setFirstName(userUpdateDto.getFirstName());
        }
        if (!isEmpty(userUpdateDto.getLastName())) {
            user.setLastName(userUpdateDto.getLastName());
        }
        if (!isEmpty(userUpdateDto.getPassword())) {
            user.setPassword(encoder.encode(userUpdateDto.getPassword()));
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(final Long id) {
        deleteOrganizationsThatBelongToUser(id);
        userRepository.deleteById(id);
    }

    public List<User> autocomplete(final String query) {
        if (isBlank(query)) {
            return new ArrayList<>();
        }

        val hqlParameter = query + "%";

        return userRepository.autocomplete(hqlParameter);
    }

    @Scheduled(fixedDelayString = "${registration.token.scheduler.period}")
    @Transactional
    public void removeExpiredRegistrationTokens() {
        val registrationExpireTime =
                env.getProperty("registration.token.expires", Integer.class) / 1000;
        val seconds = LocalDateTime.now()
                                   .minusSeconds(registrationExpireTime);

        userRepository.deleteByEnabledFalseAndRegTimeBefore(seconds);
    }

    public void confirmRegistration(final String token) {
        val user = userRepository.findByRegistrationToken(token)
                                 .orElseThrow(RegistrationTokenExpiredException::new);

        user.setEnabled(true);
        user.setRegistrationToken(null);

        userRepository.save(user);
    }

    private void deleteOrganizationsThatBelongToUser(final Long userId) {
        val ownedOrgGuids = organizationService.getOwnedOrganizations(userId).stream()
                                               .map(Organization::getGuid)
                                               .collect(toList());

        ownedOrgGuids.forEach(orgGuid -> organizationService.deleteOrganization(orgGuid, userId));
    }
}