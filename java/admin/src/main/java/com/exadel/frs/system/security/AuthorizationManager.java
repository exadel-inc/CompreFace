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

package com.exadel.frs.system.security;

import static com.exadel.frs.commonservice.enums.GlobalRole.ADMINISTRATOR;
import static com.exadel.frs.commonservice.enums.GlobalRole.OWNER;
import static com.exadel.frs.commonservice.enums.GlobalRole.USER;
import com.exadel.frs.dto.UserDeleteDto;
import com.exadel.frs.commonservice.entity.App;
import com.exadel.frs.commonservice.entity.Model;
import com.exadel.frs.commonservice.entity.User;
import com.exadel.frs.commonservice.enums.AppRole;
import com.exadel.frs.exception.InsufficientPrivilegesException;
import com.exadel.frs.exception.ModelDoesNotBelongToAppException;
import java.util.List;
import lombok.val;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationManager {

    public void verifyGlobalWritePrivileges(final User user) {
        val role = user.getGlobalRole();
        if (!List.of(OWNER, ADMINISTRATOR).contains(role)) {
            throw new InsufficientPrivilegesException();
        }
    }

    public void verifyReadPrivilegesToApp(final User user, final App app) {
        if (USER == user.getGlobalRole()) {
            app.getUserAppRole(user.getId())
               .orElseThrow(InsufficientPrivilegesException::new);
        }
    }

    public void verifyWritePrivilegesToApp(final User user, final App app) {
        verifyWritePrivilegesToApp(user, app, false);
    }

    public void verifyWritePrivilegesToApp(final User user, final App app, boolean adminDenied) {
        if (List.of(OWNER, ADMINISTRATOR).contains(user.getGlobalRole())) {
            return;
        }

        val appRole = app.getUserAppRole(user.getId())
                         .orElseThrow(InsufficientPrivilegesException::new)
                         .getRole();

        if (AppRole.USER == appRole || (adminDenied && AppRole.ADMINISTRATOR == appRole)) {
            throw new InsufficientPrivilegesException();
        }
    }

    public void verifyUserDeletionFromApp(final User deleter, final String userGuid, final App app) {
        if (List.of(OWNER, ADMINISTRATOR).contains(deleter.getGlobalRole())) {
            if (isAppOwnerRemoval(userGuid, app)) {
                throw new InsufficientPrivilegesException();
            } else {
                return;
            }
        }

        AppRole appRole = app.getUserAppRole(deleter.getId())
                             .orElseThrow(InsufficientPrivilegesException::new)
                             .getRole();
        boolean isSelfRemoval = userGuid.equals(deleter.getGuid());
        if (
                ((AppRole.USER == appRole || AppRole.ADMINISTRATOR == appRole) && !isSelfRemoval)
                        || (AppRole.OWNER == appRole && isSelfRemoval)
        ) {
            throw new InsufficientPrivilegesException();
        }
    }

    private boolean isAppOwnerRemoval(final String userGuid, final App app) {
        boolean ownerRemoval = false;
        val owner = app.getOwner();
        if (owner.isPresent()) {
            String ownerGuid = owner
                    .get()
                    .getUser()
                    .getGuid();
            ownerRemoval = userGuid.equals(ownerGuid);
        }
        return ownerRemoval;
    }

    public void verifyAppHasTheModel(final String appGuid, final Model model) {
        if (!model.getApp().getGuid().equals(appGuid)) {
            throw new ModelDoesNotBelongToAppException(model.getGuid(), appGuid);
        }
    }

    public void verifyCanDeleteUser(final UserDeleteDto userDeleteDto) {
        val userToDelete = userDeleteDto.getUserToDelete();
        val deleter = userDeleteDto.getDeleter();

        val isOwnerBeingDeleted = userToDelete.getGlobalRole() == OWNER;

        if (isOwnerBeingDeleted) {
            throw new InsufficientPrivilegesException("Global owner cannot be removed!");
        }

        val deleterRole = deleter.getGlobalRole();

        val isSelfRemoval = userToDelete.equals(deleter);

        if (deleterRole == USER && !isSelfRemoval) {
            throw new InsufficientPrivilegesException();
        }
    }
}
