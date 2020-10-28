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

import static com.exadel.frs.enums.GlobalRole.ADMINISTRATOR;
import static com.exadel.frs.enums.GlobalRole.OWNER;
import static com.exadel.frs.enums.GlobalRole.USER;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.exadel.frs.dto.ui.UserDeleteDto;
import com.exadel.frs.entity.App;
import com.exadel.frs.entity.Model;
import com.exadel.frs.entity.User;
import com.exadel.frs.entity.UserAppRole;
import com.exadel.frs.entity.UserAppRoleId;
import com.exadel.frs.enums.AppRole;
import com.exadel.frs.enums.GlobalRole;
import com.exadel.frs.exception.InsufficientPrivilegesException;
import com.exadel.frs.exception.ModelDoesNotBelongToAppException;
import java.util.List;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AuthorizationManagerTest {

    private static final long APP_ID = 20001L;
    private static final long MODEL_ID = 30003L;
    private static final String APP_GUID = UUID.randomUUID().toString();
    private static final long GLOBAL_USER_ID = 1L;
    private static final long GLOBAL_ADMIN_ID = 2L;
    private static final long GLOBAL_OWNER_ID = 3L;
    private static final long GLOBAL_USER_APP_USER_ID = 5L;
    private static final long GLOBAL_USER_APP_ADMIN_ID = 6L;
    private static final long GLOBAL_USER_APP_OWNER_ID = 7L;
    private static final long GLOBAL_ADMIN_APP_USER_ID = 8L;
    private static final long GLOBAL_ADMIN_APP_ADMIN_ID = 9L;
    private static final long GLOBAL_ADMIN_APP_OWNER_ID = 10L;
    private static final long GLOBAL_OWNER_APP_USER_ID = 11L;
    private static final long GLOBAL_OWNER_APP_ADMIN_ID = 12L;
    private static final long GLOBAL_OWNER_APP_OWNER_ID = 13L;

    private final AuthorizationManager authManager;
    private final App application;
    private final List<User> users;

    public AuthorizationManagerTest() {
        authManager = new AuthorizationManager();

        users = getUsers();

        application = App.builder()
                         .id(APP_ID)
                         .guid(APP_GUID)
                         .userAppRoles(appRoles())
                         .build();
    }

    @Nested
    public class TestGlobalPrivileges {

        @Test
        void globalAdminAndGlobalOwnerCanWriteGlobal() {
            val admin = getUser(GLOBAL_ADMIN_ID);
            val owner = getUser(GLOBAL_OWNER_ID);

            authManager.verifyGlobalWritePrivileges(admin);
            authManager.verifyGlobalWritePrivileges(owner);
        }

        @Test
        void globalUserCannotWriteGlobal() {
            assertThatThrownBy(() -> {
                val user = getUser(GLOBAL_USER_ID);

                authManager.verifyGlobalWritePrivileges(user);
            }).isInstanceOf(InsufficientPrivilegesException.class);
        }
    }

    @Nested
    public class TestAppPrivileges {

        @Test
        void globalUserNotInvitedToAppCannotReadApp() {
            assertThatThrownBy(() -> {
                val user = getUser(GLOBAL_USER_ID);

                authManager.verifyReadPrivilegesToApp(user, application);
            }).isInstanceOf(InsufficientPrivilegesException.class);
        }

        @Test
        void globalOwnerAndGlobalAdminCanReadApp() {
            val owner = getUser(GLOBAL_OWNER_ID);
            val admin = getUser(GLOBAL_ADMIN_ID);

            authManager.verifyReadPrivilegesToApp(owner, application);
            authManager.verifyReadPrivilegesToApp(admin, application);
        }

        @Test
        void userWithAnyRoleToAppCanReadApp() {
            for (User user : users) {
                authManager.verifyReadPrivilegesToApp(user, application);
            }
        }

        @Test
        void globalOwnerAndGlobalAdminNotInvitedToAppCanWriteApp() {
            assertThatCode(() -> {
                val globalAdmin = getUser(GLOBAL_ADMIN_ID);

                authManager.verifyWritePrivilegesToApp(globalAdmin, application);
            }).doesNotThrowAnyException();

            assertThatCode(() -> {
                val globalOwner = getUser(GLOBAL_OWNER_ID);

                authManager.verifyWritePrivilegesToApp(globalOwner, application);
            }).doesNotThrowAnyException();
        }

        @Test
        void appUserCanWriteAppIfTheyAreGlobalWriters() {
            assertThatCode(() -> {
                val globalAdminAppUser = getUser(GLOBAL_ADMIN_APP_USER_ID);

                authManager.verifyWritePrivilegesToApp(globalAdminAppUser, application);
            }).doesNotThrowAnyException();

            assertThatCode(() -> {
                val globalOwnerAppUser = getUser(GLOBAL_OWNER_APP_USER_ID);

                authManager.verifyWritePrivilegesToApp(globalOwnerAppUser, application);
            }).doesNotThrowAnyException();
        }

        @Test
        void appUserCannotWriteApp() {
            assertThatThrownBy(() -> {
                val globalUserAppUser = getUser(GLOBAL_USER_APP_USER_ID);

                authManager.verifyWritePrivilegesToApp(globalUserAppUser, application);
            }).isInstanceOf(InsufficientPrivilegesException.class);
        }

        @Test
        void appAdminAndAppOwnerCanWriteApp() {
            val globalUserAppAdmin = getUser(GLOBAL_USER_APP_ADMIN_ID);
            val globalUserAppOwner = getUser(GLOBAL_USER_APP_OWNER_ID);
            val globalAdminAppAdmin = getUser(GLOBAL_ADMIN_APP_ADMIN_ID);
            val globalAdminAppOwner = getUser(GLOBAL_ADMIN_APP_OWNER_ID);
            val globalOwnerAppAdmin = getUser(GLOBAL_OWNER_APP_ADMIN_ID);
            val globalOwnerAppOwner = getUser(GLOBAL_ADMIN_APP_OWNER_ID);

            authManager.verifyWritePrivilegesToApp(globalUserAppAdmin, application);
            authManager.verifyWritePrivilegesToApp(globalUserAppOwner, application);
            authManager.verifyWritePrivilegesToApp(globalAdminAppAdmin, application);
            authManager.verifyWritePrivilegesToApp(globalAdminAppOwner, application);
            authManager.verifyWritePrivilegesToApp(globalOwnerAppAdmin, application);
            authManager.verifyWritePrivilegesToApp(globalOwnerAppOwner, application);
        }
    }

    @Nested
    public class TestDeleteUserPrivileges {

        final User orgOwner;
        final User orgAdmin;
        final User orgAdmin2;
        final User orgUser;
        final User orgUser2;

        public TestDeleteUserPrivileges() {
            orgOwner = getUser(GLOBAL_OWNER_ID);
            orgAdmin = getUser(GLOBAL_ADMIN_ID);
            orgAdmin2 = getUser(GLOBAL_ADMIN_APP_ADMIN_ID);
            orgUser = getUser(GLOBAL_USER_ID);
            orgUser2 = getUser(GLOBAL_USER_APP_USER_ID);
        }

        @Test
        void orgOwnerCannotBeDeleted() {
            val ownerRemovalByAdmin = UserDeleteDto.builder()
                                                   .deleter(orgAdmin)
                                                   .userToDelete(orgOwner)
                                                   .build();

            val ownerRemovalByItself = UserDeleteDto.builder()
                                                    .deleter(orgOwner)
                                                    .userToDelete(orgOwner)
                                                    .build();

            assertThatThrownBy(() -> authManager.verifyCanDeleteUser(
                    ownerRemovalByAdmin
            )).isInstanceOf(InsufficientPrivilegesException.class)
              .hasMessage(
                      "Organization owner cannot be removed!");

            assertThatThrownBy(() -> authManager.verifyCanDeleteUser(
                    ownerRemovalByItself
            )).isInstanceOf(InsufficientPrivilegesException.class)
              .hasMessage("Organization owner cannot be removed!");
        }

        @Test
        void orgAdminCanDeleteItself() {
            val adminRemovalByItself = UserDeleteDto.builder()
                                                    .deleter(orgAdmin)
                                                    .userToDelete(orgAdmin)
                                                    .build();

            authManager.verifyCanDeleteUser(adminRemovalByItself);
        }

        @Test
        void orgAdminCanDeleteOtherAdminOrUsers() {
            val adminRemovalByOtherAdmin = UserDeleteDto.builder()
                                                        .deleter(orgAdmin)
                                                        .userToDelete(orgAdmin2)
                                                        .build();

            val userRemovalByAdmin = UserDeleteDto.builder()
                                                  .deleter(orgAdmin)
                                                  .userToDelete(orgUser)
                                                  .build();

            authManager.verifyCanDeleteUser(adminRemovalByOtherAdmin);
            authManager.verifyCanDeleteUser(userRemovalByAdmin);
        }

        @Test
        void orgUserCannotDeleteOthers() {
            val adminRemovalByUser = UserDeleteDto.builder()
                                                  .deleter(orgUser)
                                                  .userToDelete(orgAdmin)
                                                  .build();

            val userRemovalByOtherUser = UserDeleteDto.builder()
                                                      .deleter(orgUser)
                                                      .userToDelete(orgUser2)
                                                      .build();

            assertThatThrownBy(() -> authManager.verifyCanDeleteUser(
                    adminRemovalByUser
            )).isInstanceOf(InsufficientPrivilegesException.class)
              .hasMessage("Action not allowed for current user");

            assertThatThrownBy(() -> authManager.verifyCanDeleteUser(
                    userRemovalByOtherUser
            )).isInstanceOf(InsufficientPrivilegesException.class)
              .hasMessage("Action not allowed for current user");
        }

        @Test
        void orgUserCanDeleteItself() {
            val userRemovalByItself = UserDeleteDto.builder()
                                                   .deleter(orgUser)
                                                   .userToDelete(orgUser)
                                                   .build();

            authManager.verifyCanDeleteUser(userRemovalByItself);
        }
    }

    @Test
    void verifyAppHasModel() {
        val model = Model.builder()
                         .id(MODEL_ID)
                         .app(application)
                         .build();

        application.setModels(List.of(model));

        authManager.verifyAppHasTheModel(APP_GUID, model);
    }

    @Test
    void strangeModelThrowsException() {
        val otherApp = App.builder()
                          .guid(UUID.randomUUID().toString())
                          .build();

        val strangeModel = Model.builder()
                                .app(otherApp)
                                .build();

        assertThatThrownBy(() -> authManager.verifyAppHasTheModel(
                APP_GUID, strangeModel
        )).isInstanceOf(ModelDoesNotBelongToAppException.class);
    }

    private User getUser(final Long id) {
        return users.stream()
                    .filter(user -> user.getId().equals(id))
                    .findFirst()
                    .get();
    }

    private List<User> getUsers() {

        return List.of(
                makeUser(GLOBAL_USER_ID, USER),
                makeUser(GLOBAL_USER_APP_USER_ID, USER),
                makeUser(GLOBAL_USER_APP_ADMIN_ID, USER),
                makeUser(GLOBAL_USER_APP_OWNER_ID, USER),
                makeUser(GLOBAL_ADMIN_ID, ADMINISTRATOR),
                makeUser(GLOBAL_ADMIN_APP_USER_ID, ADMINISTRATOR),
                makeUser(GLOBAL_ADMIN_APP_ADMIN_ID, ADMINISTRATOR),
                makeUser(GLOBAL_ADMIN_APP_OWNER_ID, ADMINISTRATOR),
                makeUser(GLOBAL_OWNER_ID, OWNER),
                makeUser(GLOBAL_OWNER_APP_USER_ID, OWNER),
                makeUser(GLOBAL_OWNER_APP_ADMIN_ID, OWNER),
                makeUser(GLOBAL_OWNER_APP_OWNER_ID, OWNER)
        );
    }

    private List<UserAppRole> appRoles() {

        return List.of(
                makeAppRole(GLOBAL_USER_APP_USER_ID, AppRole.USER),
                makeAppRole(GLOBAL_ADMIN_APP_USER_ID, AppRole.USER),
                makeAppRole(GLOBAL_OWNER_APP_USER_ID, AppRole.USER),
                makeAppRole(GLOBAL_USER_APP_ADMIN_ID, AppRole.ADMINISTRATOR),
                makeAppRole(GLOBAL_ADMIN_APP_ADMIN_ID, AppRole.ADMINISTRATOR),
                makeAppRole(GLOBAL_OWNER_APP_ADMIN_ID, AppRole.ADMINISTRATOR),
                makeAppRole(GLOBAL_USER_APP_OWNER_ID, AppRole.OWNER),
                makeAppRole(GLOBAL_ADMIN_APP_OWNER_ID, AppRole.OWNER),
                makeAppRole(GLOBAL_OWNER_APP_OWNER_ID, AppRole.OWNER)
        );
    }

    private User makeUser(final long userId, GlobalRole role) {
        return User.builder()
                   .globalRole(role)
                   .id(userId)
                   .build();
    }

    private UserAppRole makeAppRole(final long userId, final AppRole role) {
        val id = new UserAppRoleId(userId, APP_ID);

        return UserAppRole.builder()
                          .id(id)
                          .app(application)
                          .role(role)
                          .user(User.builder()
                                    .id(userId)
                                    .build()
                          )
                          .build();
    }
}