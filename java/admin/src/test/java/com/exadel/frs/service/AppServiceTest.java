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

import com.exadel.frs.commonservice.entity.*;
import com.exadel.frs.commonservice.enums.AppRole;
import com.exadel.frs.commonservice.enums.GlobalRole;
import com.exadel.frs.dto.AppCreateDto;
import com.exadel.frs.dto.AppUpdateDto;
import com.exadel.frs.dto.UserInviteDto;
import com.exadel.frs.dto.UserRoleUpdateDto;
import com.exadel.frs.exception.InsufficientPrivilegesException;
import com.exadel.frs.exception.NameIsNotUniqueException;
import com.exadel.frs.exception.UserAlreadyHasAccessToAppException;
import com.exadel.frs.repository.AppRepository;
import com.exadel.frs.service.AppService;
import com.exadel.frs.service.UserService;
import com.exadel.frs.system.security.AuthorizationManager;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.exadel.frs.commonservice.enums.AppRole.OWNER;
import static com.exadel.frs.commonservice.enums.GlobalRole.ADMINISTRATOR;
import static com.exadel.frs.commonservice.enums.GlobalRole.USER;
import static java.util.Collections.nCopies;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class AppServiceTest {

    private static final String APPLICATION_GUID = "app-guid";
    private static final long APPLICATION_ID = 1L;
    private static final long USER_ID = 3L;
    private static final long ADMIN_ID = 4L;

    @Mock
    private AppRepository appRepositoryMock;

    @Mock
    private AuthorizationManager authManagerMock;

    @Mock
    private UserService userServiceMock;

    @InjectMocks
    private AppService appService;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    private User user(final Long id, final GlobalRole role) {
        return User.builder()
                .id(id)
                .guid(UUID.randomUUID().toString())
                .globalRole(role)
                .build();
    }

    @Test
    void successGetApp() {
        val user = user(USER_ID, ADMINISTRATOR);

        val app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(userServiceMock.getUser(USER_ID)).thenReturn(user);

        val result = appService.getApp(APPLICATION_GUID, USER_ID);

        assertThat(result.getId()).isEqualTo(APPLICATION_ID);

        verify(appRepositoryMock).findByGuid(APPLICATION_GUID);
        verify(authManagerMock).verifyReadPrivilegesToApp(user, app);
        verifyNoMoreInteractions(appRepositoryMock, authManagerMock);
    }

    @Test
    void successGetAppsForGlobalAdmin() {
        val user = user(USER_ID, ADMINISTRATOR);

        val app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .build();

        when(appRepositoryMock.findAll()).thenReturn(List.of(app));
        when(userServiceMock.getUser(USER_ID)).thenReturn(user);

        val result = appService.getApps(USER_ID);

        assertThat(result).hasSize(1);
    }

    @Test
    void successGetAppsForGlobalUser() {
        val user = user(USER_ID, USER);

        val app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .build();

        when(appRepositoryMock.findAllByUserAppRoles_Id_UserId(USER_ID)).thenReturn(List.of(app));
        when(userServiceMock.getUser(USER_ID)).thenReturn(user);

        val result = appService.getApps(USER_ID);

        assertThat(result).hasSize(1);
    }

    @Test
    void successCreateApp() {
        val appCreateDto = AppCreateDto.builder()
                .name("appName")
                .build();
        val user = user(USER_ID, ADMINISTRATOR);

        when(userServiceMock.getUser(anyLong())).thenReturn(user);

        appService.createApp(appCreateDto, USER_ID);

        val varArgs = ArgumentCaptor.forClass(App.class);
        verify(appRepositoryMock).save(varArgs.capture());
        verify(appRepositoryMock).existsByName(anyString());
        verify(authManagerMock).verifyGlobalWritePrivileges(user);
        verifyNoMoreInteractions(appRepositoryMock, authManagerMock);

        assertThat(varArgs.getValue().getName()).isEqualTo(appCreateDto.getName());
        assertThat(varArgs.getValue().getGuid()).isNotEmpty();
        assertThat(varArgs.getValue().getApiKey()).isNotEmpty();
    }

    @Test
    void failCreateAppNameIsNotUnique() {
        val appCreateDto = AppCreateDto.builder()
                .name("appName")
                .build();

        when(appRepositoryMock.existsByName(anyString())).thenReturn(true);

        assertThatThrownBy(() -> appService.createApp(
                appCreateDto,
                USER_ID
        )).isInstanceOf(NameIsNotUniqueException.class);
    }

    @Test
    void successUpdateApp() {
        val appUpdateDto = AppUpdateDto.builder()
                .name("appName")
                .build();
        val app = App.builder()
                .name("name")
                .build();

        val user = user(USER_ID, ADMINISTRATOR);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(userServiceMock.getUser(USER_ID)).thenReturn(user);

        appService.updateApp(appUpdateDto, APPLICATION_GUID, USER_ID);

        val varArgs = ArgumentCaptor.forClass(App.class);
        verify(appRepositoryMock).save(varArgs.capture());
        verify(appRepositoryMock).findByGuid(APPLICATION_GUID);
        verify(appRepositoryMock).existsByName("appName");
        verify(authManagerMock).verifyWritePrivilegesToApp(user, app, true);
        verify(authManagerMock).verifyReadPrivilegesToApp(user, app);
        verifyNoMoreInteractions(appRepositoryMock, authManagerMock);

        assertThat(varArgs.getValue().getName()).isEqualTo(appUpdateDto.getName());
    }

    @Test
    void failUpdateUserAppSelfRoleOwnerChange() {
        val userRoleUpdateDto = UserRoleUpdateDto.builder()
                .userId("userGuid")
                .role(AppRole.ADMINISTRATOR.toString())
                .build();
        val user = user(USER_ID, USER);
        val admin = user(ADMIN_ID, USER);

        val app = App.builder()
                .name("name")
                .guid(APPLICATION_GUID)
                .build();

        app.addUserAppRole(user, AppRole.OWNER);
        app.addUserAppRole(admin, AppRole.ADMINISTRATOR);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(userServiceMock.getUserByGuid(any())).thenReturn(user);
        when(userServiceMock.getUser(ADMIN_ID)).thenReturn(admin);
        when(appRepositoryMock.save(any())).thenReturn(app);

        assertThatThrownBy(() -> appService.updateUserAppRole(
                userRoleUpdateDto,
                APPLICATION_GUID,
                ADMIN_ID
        )).isInstanceOf(InsufficientPrivilegesException.class);

        verify(authManagerMock).verifyWritePrivilegesToApp(admin, app, true);
        verify(authManagerMock).verifyReadPrivilegesToApp(admin, app);
        verify(appRepositoryMock).findByGuid(APPLICATION_GUID);
        verifyNoMoreInteractions(authManagerMock);
        verifyNoMoreInteractions(appRepositoryMock);
    }

    @Test
    void failUpdateAppNameIsNotUnique() {
        val appUpdateDto = AppUpdateDto.builder()
                .name("new_name")
                .build();

        val app = App.builder()
                .name("name")
                .guid(APPLICATION_GUID)
                .build();

        when(appRepositoryMock.existsByName(anyString())).thenReturn(true);
        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        assertThatThrownBy(() -> appService.updateApp(
                appUpdateDto,
                APPLICATION_GUID,
                USER_ID
        )).isInstanceOf(NameIsNotUniqueException.class);
    }

    @Test
    void successRegenerateGuid() {
        val app = App.builder()
                .name("name")
                .guid(APPLICATION_GUID)
                .build();

        val user = user(USER_ID, USER);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(userServiceMock.getUser(USER_ID)).thenReturn(user);

        appService.regenerateApiKey(APPLICATION_GUID, USER_ID);

        verify(appRepositoryMock).save(any(App.class));
        verify(appRepositoryMock).findByGuid(APPLICATION_GUID);
        verify(authManagerMock).verifyWritePrivilegesToApp(user, app, true);
        verify(authManagerMock).verifyReadPrivilegesToApp(user, app);
        verifyNoMoreInteractions(appRepositoryMock, authManagerMock);

        assertThat(app.getGuid()).isNotEqualTo("guid");
    }

    @Test
    void successDeleteApp() {
        val models = nCopies(
                3,
                Model.builder()
                        .apiKey("modelKey")
                        .build()
        );

        val app = App.builder()
                .id(APPLICATION_ID)
                .name("name")
                .guid(APPLICATION_GUID)
                .models(models)
                .build();

        val user = user(USER_ID, USER);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(userServiceMock.getUser(USER_ID)).thenReturn(user);

        appService.deleteApp(APPLICATION_GUID, USER_ID);

        verify(appRepositoryMock).findByGuid(APPLICATION_GUID);
        verify(appRepositoryMock).deleteById(anyLong());
        verify(authManagerMock).verifyWritePrivilegesToApp(user, app, true);
        verify(authManagerMock).verifyReadPrivilegesToApp(user, app);
        verifyNoMoreInteractions(appRepositoryMock, authManagerMock);
    }

    @Test
    void successGetAppRoles() {
        val user = user(USER_ID, USER);

        val app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .build();

        app.addUserAppRole(user, OWNER);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(userServiceMock.getUser(USER_ID)).thenReturn(user);

        val result = appService.getAppUsers("", APPLICATION_GUID, USER_ID);

        assertThat(result).hasSize(1);

        verify(appRepositoryMock).findByGuid(APPLICATION_GUID);
        verify(authManagerMock).verifyReadPrivilegesToApp(user, app);
        verifyNoMoreInteractions(appRepositoryMock, authManagerMock);
    }

    @Test
    void successAppUsersSearch() {
        val user1Id = 1L;
        val user2Id = 2L;
        val user3Id = 3L;

        val user1 = User.builder()
                .id(user1Id)
                .firstName("Will")
                .lastName("Smith")
                .email("ws@example.com")
                .globalRole(USER)
                .build();
        val user2 = User.builder()
                .id(user2Id)
                .firstName("Maria")
                .lastName("Smith")
                .email("sj@example.com")
                .globalRole(USER)
                .build();
        val user3 = User.builder()
                .id(user3Id)
                .firstName("Steve")
                .lastName("Jobs")
                .email("sj@example.com")
                .globalRole(USER)
                .build();

        val app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .build();

        app.addUserAppRole(user1, OWNER);
        app.addUserAppRole(user2, AppRole.USER);
        app.addUserAppRole(user3, AppRole.USER);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(userServiceMock.getUser(user1Id)).thenReturn(user1);

        val result = appService.getAppUsers("smith", APPLICATION_GUID, user1Id);

        assertThat(result).hasSize(2);

        verify(appRepositoryMock).findByGuid(APPLICATION_GUID);
        verify(authManagerMock).verifyReadPrivilegesToApp(user1, app);
        verifyNoMoreInteractions(appRepositoryMock, authManagerMock);
    }

    @Test
    void successUserInvite() {
        val userEmail = "email";
        val userInviteDto = UserInviteDto.builder()
                .userEmail("userEmail")
                .role(AppRole.USER.toString())
                .build();
        val admin = user(USER_ID, ADMINISTRATOR);

        val userId = nextLong();
        val userRole = AppRole.USER;
        val user = User.builder()
                .id(userId)
                .email(userEmail)
                .globalRole(USER)
                .build();

        val app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(userServiceMock.getUser(anyString())).thenReturn(user);
        when(userServiceMock.getUser(USER_ID)).thenReturn(admin);
        when(appRepositoryMock.save(any())).thenReturn(app);

        val userAppRole = appService.inviteUser(userInviteDto, APPLICATION_GUID, USER_ID);

        assertThat(userAppRole.getUser().getEmail()).isEqualTo(userEmail);
        assertThat(userAppRole.getRole()).isEqualTo(userRole);

        verify(authManagerMock).verifyWritePrivilegesToApp(admin, app, true);
        verify(authManagerMock).verifyReadPrivilegesToApp(admin, app);
        verifyNoMoreInteractions(authManagerMock);
    }

    @Test
    void successUserWithAppRoleOwnerInvite() {
        val admin = user(USER_ID, ADMINISTRATOR);

        val userEmail = "email";
        val user = User.builder()
                .id(nextLong())
                .email(userEmail)
                .globalRole(USER)
                .build();

        val userRole = AppRole.OWNER;
        val userInviteDto = UserInviteDto.builder()
                .userEmail(userEmail)
                .role(userRole.toString())
                .build();

        val app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .build();

        app.addUserAppRole(admin, userRole);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(userServiceMock.getUser(anyString())).thenReturn(user);
        when(userServiceMock.getUser(USER_ID)).thenReturn(admin);
        when(appRepositoryMock.save(any())).thenReturn(app);

        val actual = appService.inviteUser(userInviteDto, APPLICATION_GUID, USER_ID);

        assertThat(actual.getUser().getEmail()).isEqualTo(userEmail);
        assertThat(actual.getRole()).isEqualTo(userRole);
        assertThat(app.getOwner().get().getRole()).isEqualTo(userRole);

        verify(appRepositoryMock).save(app);
        verify(authManagerMock).verifyWritePrivilegesToApp(admin, app, true);
        verify(authManagerMock).verifyReadPrivilegesToApp(admin, app);
        verifyNoMoreInteractions(authManagerMock);
    }

    @Test
    void failUserInviteUserAlreadyHasAccessToApp() {
        val userInviteDto = UserInviteDto.builder()
                .userEmail("email")
                .role(AppRole.USER.toString())
                .build();
        val userId = nextLong();
        val admin = user(USER_ID, ADMINISTRATOR);
        val user = user(userId, USER);
        val app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .build();

        app.addUserAppRole(user, AppRole.USER);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(userServiceMock.getUser(anyString())).thenReturn(user);
        when(userServiceMock.getUser(USER_ID)).thenReturn(admin);

        assertThatThrownBy(() -> appService.inviteUser(
                userInviteDto,
                APPLICATION_GUID,
                USER_ID
        )).isInstanceOf(UserAlreadyHasAccessToAppException.class);
    }

    @Test
    void deleteUserFromApp() {
        val userGuid = randomAlphabetic(36);
        val userId = nextLong();
        val user = user(userId, USER);
        user.setGuid(userGuid);
        val admin = user(USER_ID, USER);

        val app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .build();

        app.addUserAppRole(user, AppRole.USER);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(appRepositoryMock.save(any())).thenReturn(app);
        when(userServiceMock.getUserByGuid(any())).thenReturn(user);
        when(userServiceMock.getUser(userId)).thenReturn(user);
        when(userServiceMock.getUser(USER_ID)).thenReturn(admin);

        assertThat(app.getUserAppRoles()).hasSize(1);
        assertThat(app.getUserAppRoles()).allSatisfy(
                userAppRole -> {
                    assertThat(userAppRole.getUser().getGuid()).isEqualTo(userGuid);
                    assertThat(user.getUserAppRoles()).contains(userAppRole);
                }
        );

        appService.deleteUserFromApp(userGuid, APPLICATION_GUID, USER_ID);

        assertThat(app.getUserAppRoles()).isEmpty();
        assertThat(user.getUserAppRoles()).isEmpty();

        verify(appRepositoryMock).findByGuid(APPLICATION_GUID);
        verify(appRepositoryMock).save(any());
        verify(authManagerMock).verifyUserDeletionFromApp(admin, userGuid, app);
        verify(authManagerMock).verifyReadPrivilegesToApp(user, app);
        verifyNoMoreInteractions(appRepositoryMock, authManagerMock);
    }

    @Test
    void successPassAllOwnedAppsToNewOwnerAndLeave() {
        val app1 = mock(App.class);
        val app2 = mock(App.class);
        val app3 = mock(App.class);
        val app4 = mock(App.class);
        val apps = List.of(app1, app2, app3, app4);

        val oldOwner = user(1L, USER);
        val newOwner = user(2L, ADMINISTRATOR);

        when(app1.getUserAppRole(1L)).thenReturn(Optional.of(UserAppRole.builder()
                .role(OWNER)
                .build())
        );
        when(app2.getUserAppRole(1L)).thenReturn(Optional.of(UserAppRole.builder()
                .role(OWNER)
                .build())
        );
        when(app3.getUserAppRole(1L)).thenReturn(Optional.of(UserAppRole.builder()
                .role(AppRole.ADMINISTRATOR)
                .build())
        );
        when(app4.getUserAppRole(1L)).thenReturn(Optional.of(UserAppRole.builder()
                .role(AppRole.USER)
                .build())
        );

        when(appRepositoryMock.findAllByUserAppRoles_Id_UserId(anyLong())).thenReturn(apps);
        when(userServiceMock.getUser(1L)).thenReturn(oldOwner);

        appService.passAllOwnedAppsToNewOwnerAndLeaveAllApps(oldOwner, newOwner);

        verify(app1).getUserAppRole(oldOwner.getId());
        verify(app1).deleteUserAppRole(oldOwner.getGuid());
        verify(app1).deleteUserAppRole(newOwner.getGuid());
        verify(app1).addUserAppRole(newOwner, OWNER);

        verify(app2).getUserAppRole(oldOwner.getId());
        verify(app2).deleteUserAppRole(oldOwner.getGuid());
        verify(app2).deleteUserAppRole(newOwner.getGuid());
        verify(app2).addUserAppRole(newOwner, OWNER);

        verify(app3).getUserAppRole(oldOwner.getId());
        verify(app3).deleteUserAppRole(oldOwner.getGuid());

        verify(app4).getUserAppRole(oldOwner.getId());
        verify(app4).deleteUserAppRole(oldOwner.getGuid());

        verifyNoMoreInteractions(app1, app2, app3, app4);
    }

    @Test
    void getAppRolesToAssignReturnsToOwner() {
        val user = user(USER_ID, USER);

        val app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .build();

        app.addUserAppRole(user, OWNER);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(userServiceMock.getUser(USER_ID)).thenReturn(user);

        val actual = appService.getAppRolesToAssign(APPLICATION_GUID, USER_ID);

        assertThat(actual).hasSize(AppRole.values().length);

        verify(authManagerMock).verifyReadPrivilegesToApp(user, app);
        verifyNoMoreInteractions(authManagerMock);
    }

    @Test
    void getAppRolesToAssignReturnsToUser() {
        val user = user(USER_ID, USER);

        val app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(userServiceMock.getUser(USER_ID)).thenReturn(user);

        val actual = appService.getAppRolesToAssign(APPLICATION_GUID, USER_ID);

        assertThat(actual).isEmpty();

        verify(authManagerMock).verifyReadPrivilegesToApp(user, app);
        verifyNoMoreInteractions(authManagerMock);
    }

    @Test
    void getAppRolesToAssignReturnsDefault() {
        val user = user(USER_ID, GlobalRole.OWNER);

        val app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .build();

        app.addUserAppRole(user, AppRole.USER);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(userServiceMock.getUser(USER_ID)).thenReturn(user);

        val actual = appService.getAppRolesToAssign(APPLICATION_GUID, USER_ID);

        assertThat(actual).hasSize(AppRole.values().length);

        verify(authManagerMock).verifyReadPrivilegesToApp(user, app);
        verifyNoMoreInteractions(authManagerMock);
    }

    @Test
    void successUpdateUserAppRole() {
        val userRoleUpdateDto = UserRoleUpdateDto.builder()
                .userId("userGuid")
                .role(ADMINISTRATOR.toString())
                .build();
        val user = user(USER_ID, USER);
        val admin = user(ADMIN_ID, USER);

        val app = App.builder()
                .name("name")
                .guid(APPLICATION_GUID)
                .userAppRoles(List.of(
                        UserAppRole.builder()
                                .id(new UserAppRoleId(USER_ID, APPLICATION_ID))
                                .role(AppRole.USER)
                                .build(),
                        UserAppRole.builder()
                                .id(new UserAppRoleId(ADMIN_ID, APPLICATION_ID))
                                .role(AppRole.ADMINISTRATOR)
                                .build()
                ))
                .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(userServiceMock.getUserByGuid(any())).thenReturn(user);
        when(userServiceMock.getUser(ADMIN_ID)).thenReturn(admin);
        when(userServiceMock.getUser(USER_ID)).thenReturn(user);

        appService.updateUserAppRole(userRoleUpdateDto, APPLICATION_GUID, ADMIN_ID);

        verify(authManagerMock).verifyWritePrivilegesToApp(admin, app, true);
        verify(authManagerMock).verifyReadPrivilegesToApp(admin, app);
        verify(appRepositoryMock).save(any(App.class));
        verifyNoMoreInteractions(authManagerMock);
    }

    @Test
    void successUpdateUserAppRoleByGlobalAdmin() {
        val userRoleUpdateDto = UserRoleUpdateDto.builder()
                .userId("userGuid")
                .role(ADMINISTRATOR.toString())
                .build();
        val user = user(USER_ID, USER);
        val admin = user(ADMIN_ID, USER);

        val app = App.builder()
                .name("name")
                .guid(APPLICATION_GUID)
                .userAppRoles(List.of(
                        UserAppRole.builder()
                                .id(new UserAppRoleId(USER_ID, APPLICATION_ID))
                                .role(AppRole.USER)
                                .build()
                ))
                .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(userServiceMock.getUserByGuid(any())).thenReturn(user);
        when(userServiceMock.getUser(ADMIN_ID)).thenReturn(admin);
        when(userServiceMock.getUser(USER_ID)).thenReturn(user);

        appService.updateUserAppRole(userRoleUpdateDto, APPLICATION_GUID, ADMIN_ID);

        verify(authManagerMock).verifyWritePrivilegesToApp(admin, app, true);
        verify(authManagerMock).verifyReadPrivilegesToApp(admin, app);
        verify(appRepositoryMock).save(any(App.class));
        verifyNoMoreInteractions(authManagerMock);
    }

    @Test
    void failUpdateOwnerAppRoleByAdmin() {
        val userRoleUpdateDto = UserRoleUpdateDto.builder()
                .userId("userGuid")
                .role(ADMINISTRATOR.toString())
                .build();
        val user = user(USER_ID, USER);
        val admin = user(ADMIN_ID, USER);

        val app = App.builder()
                .name("name")
                .guid(APPLICATION_GUID)
                .userAppRoles(List.of(
                        UserAppRole.builder()
                                .id(new UserAppRoleId(USER_ID, APPLICATION_ID))
                                .role(OWNER)
                                .build()
                ))
                .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(userServiceMock.getUserByGuid(any())).thenReturn(user);
        when(userServiceMock.getUser(ADMIN_ID)).thenReturn(admin);
        when(userServiceMock.getUser(USER_ID)).thenReturn(user);

        assertThatThrownBy(() ->
                appService.updateUserAppRole(userRoleUpdateDto, APPLICATION_GUID, ADMIN_ID)
        ).isInstanceOf(InsufficientPrivilegesException.class);

        verify(authManagerMock).verifyWritePrivilegesToApp(admin, app, true);
        verify(authManagerMock).verifyReadPrivilegesToApp(admin, app);
        verifyNoMoreInteractions(authManagerMock);
    }

    @Test
    void successUpdateAppRoleToOwnerByAdmin() {
        val userRoleUpdateDto = UserRoleUpdateDto.builder()
                .userId("userGuid")
                .role(OWNER.toString())
                .build();
        val user = user(USER_ID, USER);
        val admin = user(ADMIN_ID, ADMINISTRATOR);

        val app = App.builder()
                .name("name")
                .guid(APPLICATION_GUID)
                .userAppRoles(List.of(
                        UserAppRole.builder()
                                .id(new UserAppRoleId(USER_ID, APPLICATION_ID))
                                .role(AppRole.USER)
                                .build()
                ))
                .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(userServiceMock.getUserByGuid(any())).thenReturn(user);
        when(userServiceMock.getUser(ADMIN_ID)).thenReturn(admin);
        when(userServiceMock.getUser(USER_ID)).thenReturn(user);

        appService.updateUserAppRole(userRoleUpdateDto, APPLICATION_GUID, ADMIN_ID);

        verify(authManagerMock).verifyWritePrivilegesToApp(admin, app, true);
        verify(authManagerMock).verifyReadPrivilegesToApp(admin, app);
        verifyNoMoreInteractions(authManagerMock);
    }

    @Test
    void successUpdateAppRoleToOwnerByGlobalOwner() {
        val userRoleUpdateDto = UserRoleUpdateDto.builder()
                .userId("userGuid")
                .role(OWNER.toString())
                .build();
        val user = user(USER_ID, USER);
        val admin = user(ADMIN_ID, GlobalRole.OWNER);

        val app = App.builder()
                .name("name")
                .guid(APPLICATION_GUID)
                .userAppRoles(List.of(
                        UserAppRole.builder()
                                .id(new UserAppRoleId(USER_ID, APPLICATION_ID))
                                .role(AppRole.USER)
                                .build()
                ))
                .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(userServiceMock.getUserByGuid(any())).thenReturn(user);
        when(userServiceMock.getUser(ADMIN_ID)).thenReturn(admin);
        when(userServiceMock.getUser(USER_ID)).thenReturn(user);

        appService.updateUserAppRole(userRoleUpdateDto, APPLICATION_GUID, ADMIN_ID);

        verify(authManagerMock).verifyWritePrivilegesToApp(admin, app, true);
        verify(authManagerMock).verifyReadPrivilegesToApp(admin, app);
        verify(appRepositoryMock).save(app);
        verifyNoMoreInteractions(authManagerMock);
    }

    @Test
    void successUpdateAppRoleToOwnerByAppOwner() {
        val userRoleUpdateDto = UserRoleUpdateDto.builder()
                .userId("userGuid")
                .role(OWNER.toString())
                .build();
        val user = user(USER_ID, USER);
        val admin = user(ADMIN_ID, USER);

        val app = App.builder()
                .name("name")
                .guid(APPLICATION_GUID)
                .userAppRoles(List.of(
                        UserAppRole.builder()
                                .id(new UserAppRoleId(USER_ID, APPLICATION_ID))
                                .role(AppRole.USER)
                                .build(),
                        UserAppRole.builder()
                                .id(new UserAppRoleId(USER_ID, APPLICATION_ID))
                                .role(AppRole.OWNER)
                                .build()
                ))
                .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(userServiceMock.getUserByGuid(any())).thenReturn(user);
        when(userServiceMock.getUser(ADMIN_ID)).thenReturn(admin);
        when(userServiceMock.getUser(USER_ID)).thenReturn(user);

        appService.updateUserAppRole(userRoleUpdateDto, APPLICATION_GUID, ADMIN_ID);

        verify(authManagerMock).verifyWritePrivilegesToApp(admin, app, true);
        verify(authManagerMock).verifyReadPrivilegesToApp(admin, app);
        verify(appRepositoryMock).save(app);
        verifyNoMoreInteractions(authManagerMock);
    }
}