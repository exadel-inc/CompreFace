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

import static com.exadel.frs.enums.GlobalRole.ADMINISTRATOR;
import static com.exadel.frs.enums.GlobalRole.OWNER;
import static com.exadel.frs.enums.GlobalRole.USER;
import static com.exadel.frs.system.global.Constants.DEMO_GUID;
import static com.exadel.frs.validation.EmailValidator.isInvalid;
import static org.apache.commons.lang3.BooleanUtils.isNotTrue;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.StringUtils.isEmpty;
import com.exadel.frs.dto.ui.UserCreateDto;
import com.exadel.frs.dto.ui.UserDeleteDto;
import com.exadel.frs.dto.ui.UserRoleUpdateDto;
import com.exadel.frs.dto.ui.UserUpdateDto;
import com.exadel.frs.entity.User;
import com.exadel.frs.enums.GlobalRole;
import com.exadel.frs.enums.Replacer;
import com.exadel.frs.exception.EmailAlreadyRegisteredException;
import com.exadel.frs.exception.EmptyRequiredFieldException;
import com.exadel.frs.exception.InsufficientPrivilegesException;
import com.exadel.frs.exception.InvalidEmailException;
import com.exadel.frs.exception.RegistrationTokenExpiredException;
import com.exadel.frs.exception.SelfRoleChangeException;
import com.exadel.frs.exception.UserDoesNotExistException;
import com.exadel.frs.helpers.EmailSender;
import com.exadel.frs.repository.UserRepository;
import com.exadel.frs.system.security.AuthorizationManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final EmailSender emailSender;
    private final Environment env;
    private final AuthorizationManager authManager;

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
                       .globalRole(USER)
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
                + env.getProperty("host.frs")
                + "/admin/user/registration/confirm?token="
                + user.getRegistrationToken();

        val subject = "CompreFace Registration";

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
    public void deleteUser(final UserDeleteDto userDeleteDto) {
        manageOwnedAppsByUserBeingDeleted(userDeleteDto);
        userRepository.deleteByGuid(userDeleteDto.getUserToDelete().getGuid());
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

    private void manageOwnedAppsByUserBeingDeleted(final UserDeleteDto userDeleteDto) {
        authManager.verifyCanDeleteUser(userDeleteDto);
        updateAppsOwnership(userDeleteDto);
    }

    public boolean hasOnlyDemoUser() {
        return userRepository.existsByGuid(DEMO_GUID);
    }

    public User updateDemoUser(UserCreateDto userCreateDto) {
        val isMailServerEnabled = Boolean.valueOf(env.getProperty("spring.mail.enable"));

        validateUserCreateDto(userCreateDto);
        val isAccountEnabled = isNotTrue(isMailServerEnabled);

        val user = getUserByGuid(DEMO_GUID);
        user.setEmail(userCreateDto.getEmail());
        user.setEnabled(isAccountEnabled);
        user.setFirstName(userCreateDto.getFirstName());
        user.setLastName(userCreateDto.getLastName());
        user.setPassword(encoder.encode(userCreateDto.getPassword()));
        user.setGuid(UUID.randomUUID().toString());

        if (isMailServerEnabled) {
            user.setRegistrationToken(generateRegistrationToken());
            sendRegistrationTokenToUser(user);
        }

        return userRepository.save(user);
    }

    private void updateAppsOwnership(final UserDeleteDto userDeleteDto) {
        val newOwner = decideNewOwner(userDeleteDto);
        val userBeingDeleted = userDeleteDto.getUserToDelete();
        val updateAppsConsumer = userDeleteDto.getUpdateAppsConsumer();

        updateAppsConsumer.accept(userBeingDeleted, newOwner);
    }

    public User updateUserGlobalRole(final UserRoleUpdateDto userRoleUpdateDto, final Long currentUserId) {
        val currentUser = getUser(currentUserId);

        authManager.verifyGlobalWritePrivileges(currentUser);

        val user = getUserByGuid(userRoleUpdateDto.getUserId());
        if (user.getId().equals(currentUserId)) {
            throw new SelfRoleChangeException();
        }

        val newGlobalRole = GlobalRole.valueOf(userRoleUpdateDto.getRole());

        if (ADMINISTRATOR.equals(currentUser.getGlobalRole()) &&
                (OWNER.equals(newGlobalRole)) || OWNER.equals(user.getGlobalRole())) {
            throw new InsufficientPrivilegesException();
        }

        if (newGlobalRole == OWNER) {
            currentUser.setGlobalRole(GlobalRole.ADMINISTRATOR);
        }

        user.setGlobalRole(newGlobalRole);

        userRepository.save(user);

        return user;
    }

    public GlobalRole[] getGlobalRolesToAssign(final Long userId) {
        val role = getUser(userId).getGlobalRole();
        var roles = new GlobalRole[0];

        if (role == OWNER) {
            roles = GlobalRole.values();
        } else if (role == ADMINISTRATOR) {
            roles = new GlobalRole[]{ADMINISTRATOR, USER};
        }

        return roles;
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    private User decideNewOwner(final UserDeleteDto userDeleteDto) {
        val deleter = userDeleteDto.getDeleter();
        val globalOwner = userRepository.findByGlobalRole(GlobalRole.OWNER);
        val userToDelete = userDeleteDto.getUserToDelete();
        val replacer = userDeleteDto.getReplacer();
        val selfRemoval = userToDelete.equals(deleter);

        if (selfRemoval) {
            return globalOwner;
        }

        return replacer == Replacer.DELETER ? deleter : globalOwner;
    }
}