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

import static com.exadel.frs.commonservice.enums.AppRole.ADMINISTRATOR;
import static com.exadel.frs.commonservice.enums.AppRole.OWNER;
import static com.exadel.frs.commonservice.enums.GlobalRole.USER;
import static com.exadel.frs.commonservice.enums.StatisticsType.APP_CREATE;
import static org.apache.commons.lang3.BooleanUtils.isNotTrue;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import com.exadel.frs.commonservice.annotation.CollectStatistics;
import com.exadel.frs.dto.ui.AppCreateDto;
import com.exadel.frs.dto.ui.AppUpdateDto;
import com.exadel.frs.dto.ui.UserInviteDto;
import com.exadel.frs.dto.ui.UserRoleUpdateDto;
import com.exadel.frs.commonservice.entity.App;
import com.exadel.frs.commonservice.entity.User;
import com.exadel.frs.commonservice.entity.UserAppRole;
import com.exadel.frs.commonservice.enums.AppRole;
import com.exadel.frs.exception.AppNotFoundException;
import com.exadel.frs.exception.InsufficientPrivilegesException;
import com.exadel.frs.exception.NameIsNotUniqueException;
import com.exadel.frs.exception.SelfRoleChangeException;
import com.exadel.frs.exception.UserAlreadyHasAccessToAppException;
import com.exadel.frs.repository.AppRepository;
import com.exadel.frs.system.security.AuthorizationManager;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppService {

    private final AppRepository appRepository;
    private final UserService userService;
    private final AuthorizationManager authManager;

    public App getApp(final String appGuid) {
        return appRepository.findByGuid(appGuid)
                            .orElseThrow(() -> new AppNotFoundException(appGuid));
    }

    @Transactional
    public void passAllOwnedAppsToNewOwnerAndLeaveAllApps(final User oldOwner, final User newOwner) {
        val apps = getApps(oldOwner.getId());

        apps.forEach(app -> {
            val userAppRole = app.getUserAppRole(oldOwner.getId());

            if (userAppRole.isPresent()) {
                val isOwnedApp = userAppRole.get().getRole() == OWNER;
                app.deleteUserAppRole(oldOwner.getGuid());

                if (isOwnedApp) {
                    app.deleteUserAppRole(newOwner.getGuid());
                    app.addUserAppRole(newOwner, AppRole.OWNER);
                }
                appRepository.save(app);
            }
        });
    }

    private void verifyNameIsUnique(final String name) {
        if (appRepository.existsByName(name)) {
            throw new NameIsNotUniqueException(name);
        }
    }

    public App getApp(final String appGuid, final Long userId) {
        val app = getApp(appGuid);
        val user = userService.getUser(userId);

        authManager.verifyReadPrivilegesToApp(user, app);

        return app;
    }

    public List<App> getApps(final Long userId) {
        val user = userService.getUser(userId);

        if (USER == user.getGlobalRole()) {
            return appRepository.findAllByUserAppRoles_Id_UserId(userId);
        }

        return appRepository.findAll();
    }

    public AppRole[] getAppRolesToAssign(final String appGuid, final Long userId) {
        val app = getApp(appGuid, userId);

        val userAppRole = app.getUserAppRole(userId);
        if (userAppRole.isPresent() && OWNER == userAppRole.get().getRole()) {
            return AppRole.values();
        }

        val user = userService.getUser(userId);

        if (USER == user.getGlobalRole()) {
            return new AppRole[0];
        }

        return AppRole.values();
    }

    public List<UserAppRole> getAppUsers(final String searchText, final String appGuid, final Long userId) {
        val app = getApp(appGuid, userId);

        if (isNotEmpty(searchText)) {
            val result = new ArrayList<UserAppRole>();
            app.getUserAppRoles().forEach(userAppRole -> {
                val searchTextLowerCase = searchText.toLowerCase();
                if (userAppRole.getUser().getFirstName().toLowerCase().contains(searchTextLowerCase) ||
                        userAppRole.getUser().getLastName().toLowerCase().contains(searchTextLowerCase) ||
                        userAppRole.getUser().getEmail().toLowerCase().contains(searchTextLowerCase)) {
                    result.add(userAppRole);
                }
            });

            return result;
        }

        return app.getUserAppRoles();
    }

    public UserAppRole inviteUser(
            final UserInviteDto userInviteDto,
            final String appGuid,
            final Long userId
    ) {
        val app = getApp(appGuid, userId);
        val user = userService.getUser(userInviteDto.getUserEmail());
        val admin = userService.getUser(userId);

        authManager.verifyWritePrivilegesToApp(admin, app, true);

        val userAppRole = app.getUserAppRole(user.getId());
        if (userAppRole.isPresent()) {
            throw new UserAlreadyHasAccessToAppException(userInviteDto.getUserEmail(), app.getName());
        }

        val appRole = AppRole.valueOf(userInviteDto.getRole());
        if (OWNER == appRole) {
            app.getOwner().ifPresent(previousOwner -> previousOwner.setRole(ADMINISTRATOR));
        }

        app.addUserAppRole(user, appRole);
        val savedApp = appRepository.save(app);

        return savedApp.getUserAppRole(user.getId()).orElseThrow();
    }

    @CollectStatistics(type = APP_CREATE)
    public App createApp(final AppCreateDto appCreateDto, final Long userId) {
        verifyNameIsUnique(appCreateDto.getName());

        val user = userService.getUser(userId);

        authManager.verifyGlobalWritePrivileges(user);

        val app = App.builder()
                     .name(appCreateDto.getName())
                     .guid(UUID.randomUUID().toString())
                     .apiKey(UUID.randomUUID().toString())
                     .build();

        app.addUserAppRole(user, OWNER);

        return appRepository.save(app);
    }

    public App updateApp(final AppUpdateDto appUpdateDto, final String appGuid, final Long userId) {
        val app = getApp(appGuid, userId);
        val user = userService.getUser(userId);

        authManager.verifyWritePrivilegesToApp(user, app, true);

        val isSameName = app.getName().equals(appUpdateDto.getName());
        if (isNotTrue(isSameName)) {
            verifyNameIsUnique(appUpdateDto.getName());
            app.setName(appUpdateDto.getName());
        }

        return appRepository.save(app);
    }

    public UserAppRole updateUserAppRole(final UserRoleUpdateDto userRoleUpdateDto, final String guid, final Long adminId) {
        val app = getApp(guid, adminId);
        val admin = userService.getUser(adminId);

        authManager.verifyWritePrivilegesToApp(admin, app, true);

        val userToUpdate = userService.getUserByGuid(userRoleUpdateDto.getUserId());
        val userToUpdateAppRole = app.getUserAppRole(userToUpdate.getId()).orElseThrow();
        val newAppRole = AppRole.valueOf(userRoleUpdateDto.getRole());

        if (userToUpdateAppRole.getRole().equals(OWNER)) {
            throw new InsufficientPrivilegesException();
        }

        if (newAppRole.equals(OWNER)) {
            app.getOwner().ifPresent(previousOwner -> previousOwner.setRole(ADMINISTRATOR));
        }

        userToUpdateAppRole.setRole(newAppRole);

        appRepository.save(app);

        return userToUpdateAppRole;
    }

    public void deleteUserFromApp(final String userGuid, final String guid, final Long adminId) {
        val userId = userService.getUserByGuid(userGuid).getId();
        val app = getApp(guid, userId);
        val admin = userService.getUser(adminId);

        authManager.verifyUserDeletionFromApp(admin, userGuid, app);

        app.deleteUserAppRole(userGuid);

        appRepository.save(app);
    }

    public void regenerateApiKey(final String guid, final Long userId) {
        val app = getApp(guid, userId);
        val user = userService.getUser(userId);

        authManager.verifyWritePrivilegesToApp(user, app, true);

        app.setApiKey(UUID.randomUUID().toString());

        appRepository.save(app);
    }

    @Transactional
    public void deleteApp(final String guid, final Long userId) {
        val app = getApp(guid, userId);
        val user = userService.getUser(userId);

        authManager.verifyWritePrivilegesToApp(user, app, true);

        appRepository.deleteById(app.getId());
    }
}