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
import static org.junit.jupiter.api.Assertions.assertThrows;
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

            assertThrows(
                    InsufficientPrivilegesException.class,
                    () -> authManager.verifyReadPrivilegesToOrg(STRANGE_USER_ID, organization)
            );
        }

        @Test
        void orgAdminAndOrgOwnerCanWriteOrg() {
            authManager.verifyWritePrivilegesToOrg(ORG_ADMIN_ID, organization);
            authManager.verifyWritePrivilegesToOrg(ORG_OWNER_ID, organization);
        }

        @Test
        void orgUserCannotWriteOrg() {
            assertThrows(
                    InsufficientPrivilegesException.class,
                    () -> authManager.verifyWritePrivilegesToOrg(ORG_USER_ID, organization)
            );
        }

        @Test
        void outsideOrgUsersCannotWriteOrg() {
            assertThrows(
                    InsufficientPrivilegesException.class,
                    () -> authManager.verifyWritePrivilegesToOrg(STRANGE_USER_ID, organization)
            );
        }
    }

    @Nested
    public class TestAppPrivileges {

        @Test
        void orgUserNotInvitedToAppCannotReadApp() {

            assertThrows(
                    InsufficientPrivilegesException.class,
                    () -> authManager.verifyReadPrivilegesToApp(ORG_USER_ID, application)
            );
        }

        @Test
        void outsideOrgUsersCannotReadApp() {

            assertThrows(
                    InsufficientPrivilegesException.class,
                    () -> authManager.verifyReadPrivilegesToApp(STRANGE_USER_ID, application)
            );
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
        void orgOwnerAndOrgAdminNotInvitedToAppCannotWriteApp() {
            assertThrows(
                    InsufficientPrivilegesException.class,
                    () -> authManager.verifyWritePrivilegesToApp(ORG_ADMIN_ID, application)
            );

            assertThrows(
                    InsufficientPrivilegesException.class,
                    () -> authManager.verifyWritePrivilegesToApp(ORG_OWNER_ID, application)
            );
        }

        @Test
        void appUserCannotWriteApp() {
            assertThrows(
                    InsufficientPrivilegesException.class,
                    () -> authManager.verifyWritePrivilegesToApp(ORG_USER_APP_USER_ID, application)
            );

            assertThrows(
                    InsufficientPrivilegesException.class,
                    () -> authManager.verifyWritePrivilegesToApp(ORG_ADMIN_APP_USER_ID, application)
            );

            assertThrows(
                    InsufficientPrivilegesException.class,
                    () -> authManager.verifyWritePrivilegesToApp(ORG_OWNER_APP_USER_ID, application)
            );
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

        assertThrows(
                AppDoesNotBelongToOrgException.class,
                () -> authManager.verifyOrganizationHasTheApp(ORG_GUID, strangeApp)
        );
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

        assertThrows(
                ModelDoesNotBelongToAppException.class,
                () -> authManager.verifyAppHasTheModel(APP_GUID, strangeModel)
        );
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