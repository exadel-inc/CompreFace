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

import static com.exadel.frs.enums.OrganizationRole.ADMINISTRATOR;
import static com.exadel.frs.enums.OrganizationRole.OWNER;
import static com.exadel.frs.enums.OrganizationRole.USER;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.exadel.frs.dto.ui.UserDeleteDto;
import com.exadel.frs.entity.App;
import com.exadel.frs.entity.Model;
import com.exadel.frs.entity.Organization;
import com.exadel.frs.entity.User;
import com.exadel.frs.entity.UserAppRole;
import com.exadel.frs.entity.UserAppRoleId;
import com.exadel.frs.entity.UserOrganizationRole;
import com.exadel.frs.entity.UserOrganizationRoleId;
import com.exadel.frs.enums.AppRole;
import com.exadel.frs.enums.OrganizationRole;
import com.exadel.frs.exception.AppDoesNotBelongToOrgException;
import com.exadel.frs.exception.InsufficientPrivilegesException;
import com.exadel.frs.exception.ModelDoesNotBelongToAppException;
import java.util.List;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AuthorizationManagerTest {

    private static final long ORG_ID = 10001L;
    private static final long APP_ID = 20001L;
    private static final long MODEL_ID = 30003L;
    private static final String ORG_GUID = UUID.randomUUID().toString();
    private static final String APP_GUID = UUID.randomUUID().toString();
    private static final long ORG_USER_ID = 1L;
    private static final long ORG_ADMIN_ID = 2L;
    private static final long ORG_OWNER_ID = 3L;
    private static final long STRANGE_USER_ID = 4L;
    private static final long ORG_USER_APP_USER_ID = 5L;
    private static final long ORG_USER_APP_ADMIN_ID = 6L;
    private static final long ORG_USER_APP_OWNER_ID = 7L;
    private static final long ORG_ADMIN_APP_USER_ID = 8L;
    private static final long ORG_ADMIN_APP_ADMIN_ID = 9L;
    private static final long ORG_ADMIN_APP_OWNER_ID = 10L;
    private static final long ORG_OWNER_APP_USER_ID = 11L;
    private static final long ORG_OWNER_APP_ADMIN_ID = 12L;
    private static final long ORG_OWNER_APP_OWNER_ID = 13L;

    private final AuthorizationManager authManager;
    private final Organization organization;
    private final App application;

    public AuthorizationManagerTest() {
        authManager = new AuthorizationManager();

        application = App.builder()
                         .id(APP_ID)
                         .guid(APP_GUID)
                         .userAppRoles(appRoles())
                         .build();

        organization = Organization.builder()
                                   .id(ORG_ID)
                                   .guid(ORG_GUID)
                                   .apps(List.of(application))
                                   .userOrganizationRoles(orgRoles())
                                   .build();

        application.setOrganization(organization);
    }

    @Nested
    public class TestOrganizationPrivileges {

        @Test
        void orgUserOrgAdminAndOrgOwnerCanReadOrg() {
            authManager.verifyReadPrivilegesToOrg(ORG_USER_ID, organization);
            authManager.verifyReadPrivilegesToOrg(ORG_ADMIN_ID, organization);
            authManager.verifyReadPrivilegesToOrg(ORG_OWNER_ID, organization);
        }

        @Test
        void outsideOrgUsersCannotReadOrg() {
            assertThatThrownBy(() -> {
                authManager.verifyReadPrivilegesToOrg(STRANGE_USER_ID, organization);
            }).isInstanceOf(InsufficientPrivilegesException.class);
        }

        @Test
        void orgAdminAndOrgOwnerCanWriteOrg() {
            authManager.verifyWritePrivilegesToOrg(ORG_ADMIN_ID, organization);
            authManager.verifyWritePrivilegesToOrg(ORG_OWNER_ID, organization);
        }

        @Test
        void orgUserCannotWriteOrg() {
            assertThatThrownBy(() -> {
                authManager.verifyWritePrivilegesToOrg(ORG_USER_ID, organization);
            }).isInstanceOf(InsufficientPrivilegesException.class);
        }

        @Test
        void outsideOrgUsersCannotWriteOrg() {
            assertThatThrownBy(() -> {
                authManager.verifyWritePrivilegesToOrg(STRANGE_USER_ID, organization);
            }).isInstanceOf(InsufficientPrivilegesException.class);
        }
    }

    @Nested
    public class TestAppPrivileges {

        @Test
        void orgUserNotInvitedToAppCannotReadApp() {
            assertThatThrownBy(() -> {
                authManager.verifyReadPrivilegesToApp(ORG_USER_ID, application);
            }).isInstanceOf(InsufficientPrivilegesException.class);
        }

        @Test
        void outsideOrgUsersCannotReadApp() {
            assertThatThrownBy(() -> {
                authManager.verifyReadPrivilegesToApp(STRANGE_USER_ID, application);
            }).isInstanceOf(InsufficientPrivilegesException.class);
        }

        @Test
        void uninvitedOrgOwnerAndOrgAdminCanReadApp() {
            authManager.verifyReadPrivilegesToApp(ORG_OWNER_ID, application);
            authManager.verifyReadPrivilegesToApp(ORG_ADMIN_ID, application);
        }

        @Test
        void userWithAnyRoleToAppCanReadApp() {
            authManager.verifyReadPrivilegesToApp(ORG_USER_APP_USER_ID, application);
            authManager.verifyReadPrivilegesToApp(ORG_USER_APP_ADMIN_ID, application);
            authManager.verifyReadPrivilegesToApp(ORG_USER_APP_OWNER_ID, application);
            authManager.verifyReadPrivilegesToApp(ORG_ADMIN_APP_USER_ID, application);
            authManager.verifyReadPrivilegesToApp(ORG_ADMIN_APP_ADMIN_ID, application);
            authManager.verifyReadPrivilegesToApp(ORG_ADMIN_APP_OWNER_ID, application);
            authManager.verifyReadPrivilegesToApp(ORG_OWNER_APP_USER_ID, application);
            authManager.verifyReadPrivilegesToApp(ORG_OWNER_APP_ADMIN_ID, application);
            authManager.verifyReadPrivilegesToApp(ORG_OWNER_APP_OWNER_ID, application);
        }

        @Test
        void orgOwnerAndOrgAdminNotInvitedToAppCanWriteApp() {
            assertThatCode(() -> {
                authManager.verifyWritePrivilegesToApp(ORG_ADMIN_ID, application);
            }).doesNotThrowAnyException();

            assertThatCode(() -> {
                authManager.verifyWritePrivilegesToApp(ORG_OWNER_ID, application);
            }).doesNotThrowAnyException();
        }

        @Test
        void appUserCanWriteAppIfTheyAreOrgWriters() {
            assertThatCode(() -> {
                authManager.verifyWritePrivilegesToApp(ORG_ADMIN_APP_USER_ID, application);
            }).doesNotThrowAnyException();

            assertThatCode(() -> {
                authManager.verifyWritePrivilegesToApp(ORG_OWNER_APP_USER_ID, application);
            }).doesNotThrowAnyException();
        }

        @Test
        void appUserCannotWriteApp() {
            assertThatThrownBy(() -> {
                authManager.verifyWritePrivilegesToApp(ORG_USER_APP_USER_ID, application);
            }).isInstanceOf(InsufficientPrivilegesException.class);
        }

        @Test
        void appAdminAndAppOwnerCanWriteApp() {
            authManager.verifyWritePrivilegesToApp(ORG_USER_APP_ADMIN_ID, application);
            authManager.verifyWritePrivilegesToApp(ORG_USER_APP_OWNER_ID, application);
            authManager.verifyWritePrivilegesToApp(ORG_ADMIN_APP_ADMIN_ID, application);
            authManager.verifyWritePrivilegesToApp(ORG_ADMIN_APP_OWNER_ID, application);
            authManager.verifyWritePrivilegesToApp(ORG_OWNER_APP_ADMIN_ID, application);
            authManager.verifyWritePrivilegesToApp(ORG_OWNER_APP_OWNER_ID, application);
        }
    }

    @Nested
    public class TestDeleteUserPrivileges {

        final Organization defaultOrg;
        final User orgOwner;
        final User orgAdmin;
        final User orgAdmin2;
        final User orgUser;
        final User orgUser2;

        public TestDeleteUserPrivileges() {
            orgOwner = makeUser(ORG_OWNER_ID);
            orgAdmin = makeUser(ORG_ADMIN_ID);
            orgAdmin2 = makeUser(ORG_ADMIN_APP_ADMIN_ID);
            orgUser = makeUser(ORG_USER_ID);
            orgUser2 = makeUser(ORG_USER_APP_USER_ID);

            val orgOwnerRole = makeRole(orgOwner, OWNER);
            val orgAdminRole = makeRole(orgAdmin, ADMINISTRATOR);
            val orgAdmin2Role = makeRole(orgAdmin2, ADMINISTRATOR);
            val orgUserRole = makeRole(orgUser, USER);
            val orgUser2Role = makeRole(orgUser2, USER);

            val roles = List.of(orgOwnerRole, orgAdminRole, orgAdmin2Role, orgUserRole, orgUser2Role);
            defaultOrg = Organization.builder()
                                     .isDefault(true)
                                     .userOrganizationRoles(roles)
                                     .build();
        }

        @Test
        void orgOwnerCannotBeDeleted() {
            val ownerRemovalByAdmin = UserDeleteDto.builder()
                                                   .defaultOrg(defaultOrg)
                                                   .deleter(orgAdmin)
                                                   .userToDelete(orgOwner)
                                                   .build();

            val ownerRemovalByItself = UserDeleteDto.builder()
                                                    .defaultOrg(defaultOrg)
                                                    .deleter(orgOwner)
                                                    .userToDelete(orgOwner)
                                                    .build();

            assertThatThrownBy(() -> {
                authManager.verifyCanDeleteUser(ownerRemovalByAdmin);
            }).isInstanceOf(InsufficientPrivilegesException.class)
              .hasMessage("Organization owner cannot be removed!");

            assertThatThrownBy(() -> {
                authManager.verifyCanDeleteUser(ownerRemovalByItself);
            }).isInstanceOf(InsufficientPrivilegesException.class)
              .hasMessage("Organization owner cannot be removed!");
        }

        @Test
        void orgAdminCanDeleteItself() {
            val adminRemovalByItself = UserDeleteDto.builder()
                                                    .defaultOrg(defaultOrg)
                                                    .deleter(orgAdmin)
                                                    .userToDelete(orgAdmin)
                                                    .build();

            authManager.verifyCanDeleteUser(adminRemovalByItself);
        }

        @Test
        void orgAdminCanDeleteOtherAdminOrUsers() {
            val adminRemovalByOtherAdmin = UserDeleteDto.builder()
                                                        .defaultOrg(defaultOrg)
                                                        .deleter(orgAdmin)
                                                        .userToDelete(orgAdmin2)
                                                        .build();

            val userRemovalByAdmin = UserDeleteDto.builder()
                                                  .defaultOrg(defaultOrg)
                                                  .deleter(orgAdmin)
                                                  .userToDelete(orgUser)
                                                  .build();

            authManager.verifyCanDeleteUser(adminRemovalByOtherAdmin);
            authManager.verifyCanDeleteUser(userRemovalByAdmin);
        }

        @Test
        void orgUserCannotDeleteOthers() {
            val adminRemovalByUser = UserDeleteDto.builder()
                                                  .defaultOrg(defaultOrg)
                                                  .deleter(orgUser)
                                                  .userToDelete(orgAdmin)
                                                  .build();

            val userRemovalByOtherUser = UserDeleteDto.builder()
                                                      .defaultOrg(defaultOrg)
                                                      .deleter(orgUser)
                                                      .userToDelete(orgUser2)
                                                      .build();

            assertThatThrownBy(() -> {
                authManager.verifyCanDeleteUser(adminRemovalByUser);
            }).isInstanceOf(InsufficientPrivilegesException.class)
              .hasMessage("Action not allowed for current user");

            assertThatThrownBy(() -> {
                authManager.verifyCanDeleteUser(userRemovalByOtherUser);
            }).isInstanceOf(InsufficientPrivilegesException.class)
              .hasMessage("Action not allowed for current user");
        }

        @Test
        void orgUserCanDeleteItself() {
            val userRemovalByItself = UserDeleteDto.builder()
                                                   .defaultOrg(defaultOrg)
                                                   .deleter(orgUser)
                                                   .userToDelete(orgUser)
                                                   .build();

            authManager.verifyCanDeleteUser(userRemovalByItself);
        }

        private User makeUser(final long orgUserId) {
            return User.builder()
                       .id(orgUserId)
                       .guid(UUID.randomUUID().toString())
                       .build();
        }

        private UserOrganizationRole makeRole(final User user, OrganizationRole role) {
            val roleId = UserOrganizationRoleId.builder()
                                               .userId(user.getId())
                                               .organizationId(ORG_ID)
                                               .build();

            return UserOrganizationRole.builder()
                                       .id(roleId)
                                       .user(user)
                                       .role(role)
                                       .build();
        }
    }

    @Test
    void verifyOrganizationHasTheApp() {
        authManager.verifyOrganizationHasTheApp(ORG_GUID, application);
    }

    @Test
    void strangeAppThrowsException() {
        val otherOrg = Organization.builder()
                                   .guid(UUID.randomUUID().toString())
                                   .build();

        val strangeApp = App.builder()
                            .organization(otherOrg)
                            .build();

        assertThatThrownBy(() -> {
            authManager.verifyOrganizationHasTheApp(ORG_GUID, strangeApp);
        }).isInstanceOf(AppDoesNotBelongToOrgException.class);
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

        assertThatThrownBy(() -> {
            authManager.verifyAppHasTheModel(APP_GUID, strangeModel);
        }).isInstanceOf(ModelDoesNotBelongToAppException.class);
    }

    private List<UserOrganizationRole> orgRoles() {

        return List.of(
                makeOrgRole(ORG_USER_ID, USER),
                makeOrgRole(ORG_USER_APP_USER_ID, USER),
                makeOrgRole(ORG_USER_APP_ADMIN_ID, USER),
                makeOrgRole(ORG_USER_APP_OWNER_ID, USER),
                makeOrgRole(ORG_ADMIN_ID, ADMINISTRATOR),
                makeOrgRole(ORG_ADMIN_APP_USER_ID, ADMINISTRATOR),
                makeOrgRole(ORG_ADMIN_APP_ADMIN_ID, ADMINISTRATOR),
                makeOrgRole(ORG_ADMIN_APP_OWNER_ID, ADMINISTRATOR),
                makeOrgRole(ORG_OWNER_ID, OWNER),
                makeOrgRole(ORG_OWNER_APP_USER_ID, OWNER),
                makeOrgRole(ORG_OWNER_APP_ADMIN_ID, OWNER),
                makeOrgRole(ORG_OWNER_APP_OWNER_ID, OWNER)
        );
    }

    private List<UserAppRole> appRoles() {

        return List.of(
                makeAppRole(ORG_USER_APP_USER_ID, AppRole.USER),
                makeAppRole(ORG_ADMIN_APP_USER_ID, AppRole.USER),
                makeAppRole(ORG_OWNER_APP_USER_ID, AppRole.USER),
                makeAppRole(ORG_USER_APP_ADMIN_ID, AppRole.ADMINISTRATOR),
                makeAppRole(ORG_ADMIN_APP_ADMIN_ID, AppRole.ADMINISTRATOR),
                makeAppRole(ORG_OWNER_APP_ADMIN_ID, AppRole.ADMINISTRATOR),
                makeAppRole(ORG_USER_APP_OWNER_ID, AppRole.OWNER),
                makeAppRole(ORG_ADMIN_APP_OWNER_ID, AppRole.OWNER),
                makeAppRole(ORG_OWNER_APP_OWNER_ID, AppRole.OWNER)
        );
    }

    private UserOrganizationRole makeOrgRole(final long userId, OrganizationRole role) {
        val id = UserOrganizationRoleId.builder()
                                       .userId(userId)
                                       .organizationId(ORG_ID)
                                       .build();

        return UserOrganizationRole.builder()
                                   .id(id)
                                   .role(role)
                                   .organization(organization)
                                   .user(User.builder().id(userId).build())
                                   .build();
    }

    private UserAppRole makeAppRole(final long userId, final AppRole role) {
        val id = new UserAppRoleId(userId, APP_ID);

        return UserAppRole.builder()
                          .id(id)
                          .app(application)
                          .role(role)
                          .user(User.builder().id(userId).build())
                          .build();
    }
}