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

import static com.exadel.frs.validation.EmailValidator.isInvalid;
import static org.apache.commons.lang3.BooleanUtils.isNotTrue;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.StringUtils.isEmpty;
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
import com.exadel.frs.system.security.AuthorizationManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.val;
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
    private final OrganizationService orgService;
    private final AuthorizationManager authManager;
    private final AppService appService;

    public UserService(
            @NonNull final UserRepository userRepository,
            @NonNull final PasswordEncoder encoder,
            @NonNull final EmailSender emailSender,
            @NonNull final Environment env,
            @NotNull final OrganizationService orgService,
            @NotNull final AuthorizationManager authManager,
            @NotNull final AppService appService
    ) {
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.emailSender = emailSender;
        this.env = env;
        this.orgService = orgService;
        this.authManager = authManager;
        this.appService = appService;
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

        val isAccountEnabled = isNotTrue(isMailServerEnabled);
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
                       .build();

        if (isMailServerEnabled) {
            user.setRegistrationToken(generateRegistrationToken());
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
    public void deleteUser(final String userGuid,
                           final String replacer,
                           final User deleter) {

        manageOwnedAppsByUserBeingDeleted(userGuid, replacer, deleter);
        userRepository.deleteByGuid(userGuid);
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

    private void manageOwnedAppsByUserBeingDeleted(final String userGuid,
                                                   final String replacer,
                                                   final User deleter) {

        val defaultOrg = orgService.getDefaultOrg();
        val orgOwner = defaultOrg.getOwner();
        val userBeingDeleted = getUserByGuid(userGuid);
        val newOwner = new User();

        //TODO check delete authorization
        //TODO decide replacer
        //TODO update apps

        appService.passAllOwnedAppsToNewOwnerAndLeave(userBeingDeleted, newOwner);

    }
}