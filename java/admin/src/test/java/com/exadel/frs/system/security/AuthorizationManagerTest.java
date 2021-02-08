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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import com.exadel.frs.dto.ui.UserDeleteDto;
import com.exadel.frs.commonservice.entity.App;
import com.exadel.frs.commonservice.entity.Model;
import com.exadel.frs.commonservice.entity.User;
import com.exadel.frs.commonservice.entity.UserAppRole;
import com.exadel.frs.commonservice.entity.UserAppRoleId;
import com.exadel.frs.commonservice.enums.AppRole;
import com.exadel.frs.commonservice.enums.GlobalRole;
import com.exadel.frs.exception.InsufficientPrivilegesException;
import com.exadel.frs.exception.ModelDoesNotBelongToAppException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AuthorizationManagerTest {

    private static final long APP_ID = 20001L;
    private static final long MODEL_ID = 30003L;
    private static final String APP_GUID = UUID.randomUUID().toString();
    private static final long GLOBAL_USER_ID = 1L;
    private static final long GLOBAL_ADMIN_ID = 2L;
    private static final long GLOBAL_OWNER_ID = 3L;
    private static final long GLOBAL_USER_APP_USER_ID = 5L;
    private static final long GLOBAL_USER_APP_ANOTHER_USER_ID = 55L;
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
            // exclude global user
            for (User user : users.subList(1, users.size())) {
                authManager.verifyReadPrivilegesToApp(user, application);
            }
        }
    }

    @Nested
    public class TestDeleteUserPrivileges {

        final User globalOwner;
        final User globalAdmin;
        final User globalAdmin2;
        final User globalUser;
        final User globalUser2;

        public TestDeleteUserPrivileges() {
            globalOwner = getUser(GLOBAL_OWNER_ID);
            globalAdmin = getUser(GLOBAL_ADMIN_ID);
            globalAdmin2 = getUser(GLOBAL_ADMIN_APP_ADMIN_ID);
            globalUser = getUser(GLOBAL_USER_ID);
            globalUser2 = getUser(GLOBAL_USER_APP_USER_ID);
        }

        @Test
        void globalOwnerCannotBeDeleted() {
            val ownerRemovalByAdmin = UserDeleteDto.builder()
                                                   .deleter(globalAdmin)
                                                   .userToDelete(globalOwner)
                                                   .build();

            val ownerRemovalByItself = UserDeleteDto.builder()
                                                    .deleter(globalOwner)
                                                    .userToDelete(globalOwner)
                                                    .build();

            assertThatThrownBy(() -> authManager.verifyCanDeleteUser(
                    ownerRemovalByAdmin
            )).isInstanceOf(InsufficientPrivilegesException.class)
              .hasMessage(
                      "Global owner cannot be removed!");

            assertThatThrownBy(() -> authManager.verifyCanDeleteUser(
                    ownerRemovalByItself
            )).isInstanceOf(InsufficientPrivilegesException.class)
              .hasMessage("Global owner cannot be removed!");
        }

        @Test
        void globalAdminCanDeleteItself() {
            val adminRemovalByItself = UserDeleteDto.builder()
                                                    .deleter(globalAdmin)
                                                    .userToDelete(globalAdmin)
                                                    .build();

            authManager.verifyCanDeleteUser(adminRemovalByItself);
        }

        @Test
        void globalAdminCanDeleteOtherAdminOrUsers() {
            val adminRemovalByOtherAdmin = UserDeleteDto.builder()
                                                        .deleter(globalAdmin)
                                                        .userToDelete(globalAdmin2)
                                                        .build();

            val userRemovalByAdmin = UserDeleteDto.builder()
                                                  .deleter(globalAdmin)
                                                  .userToDelete(globalUser)
                                                  .build();

            authManager.verifyCanDeleteUser(adminRemovalByOtherAdmin);
            authManager.verifyCanDeleteUser(userRemovalByAdmin);
        }

        @Test
        void globalUserCannotDeleteOthers() {
            val adminRemovalByUser = UserDeleteDto.builder()
                                                  .deleter(globalUser)
                                                  .userToDelete(globalAdmin)
                                                  .build();

            val userRemovalByOtherUser = UserDeleteDto.builder()
                                                      .deleter(globalUser)
                                                      .userToDelete(globalUser2)
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
        void globalUserCanDeleteItself() {
            val userRemovalByItself = UserDeleteDto.builder()
                                                   .deleter(globalUser)
                                                   .userToDelete(globalUser)
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

    static Stream<Arguments> verifyUserDeletionFromAppGlobalProvider() {
        return Stream.of(
                Arguments.of(GLOBAL_ADMIN_APP_USER_ID, GLOBAL_USER_APP_ADMIN_ID),
                Arguments.of(GLOBAL_ADMIN_APP_USER_ID, GLOBAL_USER_APP_USER_ID),
                Arguments.of(GLOBAL_OWNER_APP_USER_ID, GLOBAL_USER_APP_ADMIN_ID),
                Arguments.of(GLOBAL_OWNER_APP_USER_ID, GLOBAL_USER_APP_USER_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("verifyUserDeletionFromAppGlobalProvider")
    void testVerifyUserDeletionFromAppByAllowedGlobalUser(Long deleterId, Long deletionSubjectId) {
        // given
        val deleter = getUser(deleterId);
        val deletionSubject = getUser(deletionSubjectId);
        val spyApp = spy(application);

        // when
        authManager.verifyUserDeletionFromApp(deleter, deletionSubject.getGuid(), spyApp);

        // then
        verify(spyApp, times(0)).getUserAppRole(deleterId);
    }

    static Stream<Arguments> verifyUserDeletionFromAppGlobalNotAllowedProvider() {
        return Stream.of(
                Arguments.of(GLOBAL_OWNER_APP_USER_ID, GLOBAL_USER_APP_OWNER_ID),
                Arguments.of(GLOBAL_ADMIN_APP_USER_ID, GLOBAL_USER_APP_OWNER_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("verifyUserDeletionFromAppGlobalNotAllowedProvider")
    void testVerifyUserDeletionFromAppByGlobalUserNotAllowed(Long deleterId, Long deletionSubjectId) {
        // given
        val deleter = getUser(deleterId);
        val deletionSubject = getUser(deletionSubjectId);
        val spyApp = spy(application);
        when(spyApp.getOwner()).thenReturn(Optional.of(UserAppRole.builder()
                                                                  .user(deletionSubject)
                                                                  .build()));

        // when
        Executable action = () -> authManager.verifyUserDeletionFromApp(deleter, deletionSubject.getGuid(), spyApp);

        // then
        assertThrows(InsufficientPrivilegesException.class, action);
    }

    static Stream<Arguments> verifyUserDeletionFromAppGlobalUserProvider() {
        return Stream.of(
                Arguments.of(GLOBAL_USER_APP_ADMIN_ID, GLOBAL_USER_APP_ADMIN_ID),
                Arguments.of(GLOBAL_USER_APP_OWNER_ID, GLOBAL_USER_APP_ADMIN_ID),
                Arguments.of(GLOBAL_USER_APP_OWNER_ID, GLOBAL_USER_APP_USER_ID),
                Arguments.of(GLOBAL_USER_APP_USER_ID, GLOBAL_USER_APP_USER_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("verifyUserDeletionFromAppGlobalUserProvider")
    void testVerifyUserDeletionFromAppByAllowedAppUser(Long deleterId, Long deletionSubjectId) {
        // given
        val deleter = getUser(deleterId);
        val deletionSubject = getUser(deletionSubjectId);
        val spyApp = spy(application);

        // when
        authManager.verifyUserDeletionFromApp(deleter, deletionSubject.getGuid(), spyApp);

        // then
        verify(spyApp, times(1)).getUserAppRole(deleter.getId());
    }

    static Stream<Arguments> verifyUserDeletionFromAppByNotAllowedAppUserProvider() {
        return Stream.of(
                Arguments.of(GLOBAL_USER_APP_OWNER_ID, GLOBAL_USER_APP_OWNER_ID),
                Arguments.of(GLOBAL_USER_APP_ADMIN_ID, GLOBAL_USER_APP_USER_ID),
                Arguments.of(GLOBAL_USER_APP_ADMIN_ID, GLOBAL_USER_APP_OWNER_ID),
                Arguments.of(GLOBAL_USER_APP_USER_ID, GLOBAL_USER_APP_ADMIN_ID),
                Arguments.of(GLOBAL_USER_APP_USER_ID, GLOBAL_USER_APP_OWNER_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("verifyUserDeletionFromAppByNotAllowedAppUserProvider")
    void testVerifyUserDeletionFromAppByNotAllowedAppUser(Long deleterId, Long deletionSubjectId) {
        // given
        val deleter = getUser(deleterId);
        val deletionSubject = getUser(deletionSubjectId);

        // when
        Executable exec = () -> authManager.verifyUserDeletionFromApp(deleter, deletionSubject.getGuid(), application);

        // then
        assertThrows(InsufficientPrivilegesException.class, exec);
    }

    static Stream<Arguments> verifyWritePrivilegesToAppByGlobalUsersProvider() {
        return Stream.of(
                Arguments.of(GLOBAL_ADMIN_APP_USER_ID),
                Arguments.of(GLOBAL_OWNER_APP_USER_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("verifyWritePrivilegesToAppByGlobalUsersProvider")
    void testVerifyWritePrivilegesToAppByGlobalUsers(Long userId) {
        // given
        val user = getUser(userId);
        val spyApp = spy(application);

        // when
        authManager.verifyWritePrivilegesToApp(user, spyApp);

        // then
        verifyNoInteractions(spyApp);
    }

    static Stream<Arguments> verifyWritePrivilegesToAppByAllowedAppUsersProvider() {
        return Stream.of(
                Arguments.of(GLOBAL_USER_APP_OWNER_ID),
                Arguments.of(GLOBAL_USER_APP_ADMIN_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("verifyWritePrivilegesToAppByAllowedAppUsersProvider")
    void testVerifyWritePrivilegesToAppByAllowedAppUsers(Long userId) {
        // given
        val user = getUser(userId);
        val spyApp = spy(application);

        // when
        authManager.verifyWritePrivilegesToApp(user, spyApp);

        // then
        verify(spyApp, times(1)).getUserAppRole(userId);
    }

    static Stream<Arguments> verifyWritePrivilegesToAppByNotAllowedAppUsersProvider() {
        return Stream.of(
                Arguments.of(GLOBAL_USER_APP_USER_ID, false),
                Arguments.of(GLOBAL_USER_APP_ADMIN_ID, true)
        );
    }

    @ParameterizedTest
    @MethodSource("verifyWritePrivilegesToAppByNotAllowedAppUsersProvider")
    void testVerifyWritePrivilegesToAppByNotAllowedAppUsers(Long userId, boolean adminDenied) {
        // given
        val user = getUser(userId);

        // when
        Executable action = () -> authManager.verifyWritePrivilegesToApp(user, application, adminDenied);

        // then
        assertThrows(InsufficientPrivilegesException.class, action);
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
                   .guid(UUID.randomUUID().toString())
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