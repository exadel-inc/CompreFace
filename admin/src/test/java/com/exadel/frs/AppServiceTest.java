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

package com.exadel.frs;

import static com.exadel.frs.enums.AppRole.OWNER;
import static com.exadel.frs.enums.OrganizationRole.ADMINISTRATOR;
import static com.exadel.frs.enums.OrganizationRole.USER;
import static java.util.Collections.nCopies;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import com.exadel.frs.dto.ui.AppCreateDto;
import com.exadel.frs.dto.ui.AppUpdateDto;
import com.exadel.frs.dto.ui.UserInviteDto;
import com.exadel.frs.dto.ui.UserRoleUpdateDto;
import com.exadel.frs.entity.App;
import com.exadel.frs.entity.Model;
import com.exadel.frs.entity.ModelShareRequest;
import com.exadel.frs.entity.Organization;
import com.exadel.frs.entity.User;
import com.exadel.frs.entity.UserAppRole;
import com.exadel.frs.entity.UserAppRoleId;
import com.exadel.frs.entity.UserOrganizationRole;
import com.exadel.frs.entity.UserOrganizationRoleId;
import com.exadel.frs.enums.AppRole;
import com.exadel.frs.enums.OrganizationRole;
import com.exadel.frs.exception.NameIsNotUniqueException;
import com.exadel.frs.exception.SelfRoleApplicationChangeException;
import com.exadel.frs.exception.SelfRoleChangeException;
import com.exadel.frs.exception.UserAlreadyHasAccessToAppException;
import com.exadel.frs.exception.UserDoesNotBelongToOrganization;
import com.exadel.frs.repository.AppRepository;
import com.exadel.frs.repository.ModelShareRequestRepository;
import com.exadel.frs.service.AppService;
import com.exadel.frs.service.OrganizationService;
import com.exadel.frs.service.UserService;
import com.exadel.frs.system.security.AuthorizationManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

class AppServiceTest {

    private static final String APPLICATION_GUID = "app-guid";
    private static final String ORGANISATION_GUID = "org-guid";
    private static final long APPLICATION_ID = 1L;
    private static final long ORGANISATION_ID = 2L;
    private static final long USER_ID = 3L;
    private static final long ADMIN_ID = 4L;

    @Mock
    private AppRepository appRepositoryMock;

    @Mock
    private ModelShareRequestRepository modelShareRequestRepositoryMock;

    @Mock
    private OrganizationService organizationServiceMock;

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

    private User user(final Long id) {
        return User.builder()
                   .id(id)
                   .guid(UUID.randomUUID().toString())
                   .build();
    }

    private Organization organization() {
        return Organization.builder()
                           .id(ORGANISATION_ID)
                           .guid(ORGANISATION_GUID)
                           .build();
    }

    @Test
    void successGetApp() {
        val organization = organization();

        val app = App.builder()
                     .id(APPLICATION_ID)
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        val result = appService.getApp(ORGANISATION_GUID, APPLICATION_GUID, USER_ID);

        assertThat(result.getId()).isEqualTo(APPLICATION_ID);

        verify(appRepositoryMock).findByGuid(APPLICATION_GUID);
        verify(authManagerMock).verifyReadPrivilegesToApp(USER_ID, app);
        verify(authManagerMock).verifyOrganizationHasTheApp(ORGANISATION_GUID, app);
        verifyNoMoreInteractions(appRepositoryMock, authManagerMock);
    }

    @Test
    void successGetAppsForOrgAdmin() {
        val user = user(USER_ID);

        val organization = organization();
        organization.addUserOrganizationRole(user, ADMINISTRATOR);

        val app = App.builder()
                     .id(APPLICATION_ID)
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();

        when(appRepositoryMock.findAllByOrganizationId(anyLong())).thenReturn(List.of(app));
        when(organizationServiceMock.getOrganization(ORGANISATION_GUID)).thenReturn(organization);

        val result = appService.getApps(ORGANISATION_GUID, USER_ID);

        assertThat(result).hasSize(1);
    }

    @Test
    void successGetAppsForOrgUser() {
        val user = user(USER_ID);

        val organization = organization();
        organization.addUserOrganizationRole(user, USER);

        val app = App.builder()
                     .id(APPLICATION_ID)
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();

        when(appRepositoryMock.findAllByOrganizationIdAndUserAppRoles_Id_UserId(ORGANISATION_ID, USER_ID)).thenReturn(List.of(app));
        when(organizationServiceMock.getOrganization(ORGANISATION_GUID)).thenReturn(organization);

        val result = appService.getApps(ORGANISATION_GUID, USER_ID);

        assertThat(result).hasSize(1);
    }

    @Test
    void failGetAppsUserDoesNotBelongToOrganization() {
        val organization = organization();

        when(organizationServiceMock.getOrganization(ORGANISATION_GUID)).thenReturn(organization);

        assertThatThrownBy(() -> {
            appService.getApps(ORGANISATION_GUID, USER_ID);
        }).isInstanceOf(UserDoesNotBelongToOrganization.class);
    }

    @Test
    void successCreateApp() {
        AppCreateDto appCreateDto = AppCreateDto.builder().name("appName").build();
        val user = user(USER_ID);

        val organization = organization();

        when(organizationServiceMock.getOrganization(anyString())).thenReturn(organization);
        when(userServiceMock.getUser(anyLong())).thenReturn(user);

        appService.createApp(appCreateDto, ORGANISATION_GUID, USER_ID);

        val varArgs = ArgumentCaptor.forClass(App.class);
        verify(appRepositoryMock).save(varArgs.capture());
        verify(appRepositoryMock).existsByNameAndOrganizationId(anyString(), anyLong());
        verify(authManagerMock).verifyWritePrivilegesToOrg(USER_ID, organization);
        verifyNoMoreInteractions(appRepositoryMock, authManagerMock);

        assertThat(varArgs.getValue().getName()).isEqualTo(appCreateDto.getName());
        assertThat(varArgs.getValue().getGuid()).isNotEmpty();
        assertThat(varArgs.getValue().getApiKey()).isNotEmpty();
    }

    @Test
    void failCreateOrganizationNameIsNotUnique() {
        val appCreateDto = AppCreateDto.builder().name("appName").build();

        val organization = organization();

        when(organizationServiceMock.getOrganization(anyString())).thenReturn(organization);
        when(appRepositoryMock.existsByNameAndOrganizationId(anyString(), anyLong())).thenReturn(true);

        assertThatThrownBy(() -> {
            appService.createApp(appCreateDto, ORGANISATION_GUID, USER_ID);
        }).isInstanceOf(NameIsNotUniqueException.class);
    }

    @Test
    void successUpdateApp() {
        val appUpdateDto = AppUpdateDto.builder().name("appName").build();
        val organization = organization();

        val app = App.builder()
                     .name("name")
                     .organization(organization)
                     .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        appService.updateApp(appUpdateDto, ORGANISATION_GUID, APPLICATION_GUID, USER_ID);

        val varArgs = ArgumentCaptor.forClass(App.class);
        verify(appRepositoryMock).save(varArgs.capture());
        verify(appRepositoryMock).findByGuid(APPLICATION_GUID);
        verify(appRepositoryMock).existsByNameAndOrganizationId("appName", ORGANISATION_ID);
        verify(authManagerMock).verifyWritePrivilegesToApp(USER_ID, app);
        verify(authManagerMock).verifyReadPrivilegesToApp(USER_ID, app);
        verify(authManagerMock).verifyOrganizationHasTheApp(ORGANISATION_GUID, app);
        verifyNoMoreInteractions(appRepositoryMock, authManagerMock);

        assertThat(varArgs.getValue().getName()).isEqualTo(appUpdateDto.getName());
    }

    @Test
    void successUpdateUserAppRole() {
        val userRoleUpdateDto = UserRoleUpdateDto.builder()
                                                 .userId("userGuid")
                                                 .role(AppRole.OWNER.toString())
                                                 .build();
        val user = user(USER_ID);
        val organization = organization();

        val app = App.builder()
                     .name("name")
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();
        app.addUserAppRole(user, AppRole.USER);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(userServiceMock.getUserByGuid(any())).thenReturn(user);
        when(appRepositoryMock.save(any())).thenReturn(app);

        val actual = appService.updateUserAppRole(userRoleUpdateDto, ORGANISATION_GUID, APPLICATION_GUID, ADMIN_ID);

        assertThat(actual.getRole()).isEqualTo(Enum.valueOf(AppRole.class, userRoleUpdateDto.getRole()));

        verify(authManagerMock).verifyWritePrivilegesToApp(ADMIN_ID, app);
        verify(authManagerMock).verifyReadPrivilegesToApp(ADMIN_ID, app);
        verify(authManagerMock).verifyOrganizationHasTheApp(ORGANISATION_GUID, app);
        verify(appRepositoryMock).save(app);
        verify(appRepositoryMock).findByGuid(APPLICATION_GUID);
        verifyNoMoreInteractions(authManagerMock);
        verifyNoMoreInteractions(appRepositoryMock);
    }

    @Test
    void failUpdateUserAppSelfRoleOwnerChange() {
        val userRoleUpdateDto = UserRoleUpdateDto.builder()
                                                 .userId("userGuid")
                                                 .role(AppRole.USER.toString())
                                                 .build();
        val user = user(USER_ID);
        val organization = organization();

        val app = App.builder()
                     .name("name")
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();
        app.addUserAppRole(user, OWNER);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(userServiceMock.getUserByGuid(any())).thenReturn(user);
        when(appRepositoryMock.save(any())).thenReturn(app);

        assertThatThrownBy(() -> {
            appService.updateUserAppRole(userRoleUpdateDto, ORGANISATION_GUID, APPLICATION_GUID, ADMIN_ID);
        }).isInstanceOf(SelfRoleApplicationChangeException.class);

        verify(authManagerMock).verifyWritePrivilegesToApp(ADMIN_ID, app);
        verify(authManagerMock).verifyReadPrivilegesToApp(ADMIN_ID, app);
        verify(authManagerMock).verifyOrganizationHasTheApp(ORGANISATION_GUID, app);
        verify(appRepositoryMock).findByGuid(APPLICATION_GUID);
        verifyNoMoreInteractions(authManagerMock);
        verifyNoMoreInteractions(appRepositoryMock);
    }

    @Test
    void failUpdateAppSelfRoleChange() {
        val userRoleUpdateDto = UserRoleUpdateDto.builder()
                                                 .userId("userGuid")
                                                 .role(AppRole.USER.toString())
                                                 .build();
        val user = user(USER_ID);
        val organization = organization();

        val app = App.builder()
                     .name("name")
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(userServiceMock.getUserByGuid(any())).thenReturn(user);

        assertThatThrownBy(() -> {
            appService.updateUserAppRole(userRoleUpdateDto, ORGANISATION_GUID, APPLICATION_GUID, USER_ID);
        }).isInstanceOf(SelfRoleChangeException.class);

        verify(authManagerMock).verifyWritePrivilegesToApp(USER_ID, app);
        verify(authManagerMock).verifyReadPrivilegesToApp(USER_ID, app);
        verify(authManagerMock).verifyOrganizationHasTheApp(ORGANISATION_GUID, app);
        verifyNoMoreInteractions(authManagerMock);
    }

    @Test
    void failUpdateAppNameIsNotUnique() {
        val appUpdateDto = AppUpdateDto.builder().name("new_name").build();
        val organization = organization();

        val app = App.builder()
                     .name("name")
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();

        when(appRepositoryMock.existsByNameAndOrganizationId(anyString(), anyLong())).thenReturn(true);
        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        assertThatThrownBy(() -> {
            appService.updateApp(appUpdateDto, ORGANISATION_GUID, APPLICATION_GUID, USER_ID);
        }).isInstanceOf(NameIsNotUniqueException.class);
    }

    @Test
    void successRegenerateGuid() {
        val organization = organization();

        val app = App.builder()
                     .name("name")
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        appService.regenerateApiKey(ORGANISATION_GUID, APPLICATION_GUID, USER_ID);

        verify(appRepositoryMock).save(any(App.class));
        verify(appRepositoryMock).findByGuid(APPLICATION_GUID);
        verify(authManagerMock).verifyWritePrivilegesToApp(USER_ID, app);
        verify(authManagerMock).verifyReadPrivilegesToApp(USER_ID, app);
        verify(authManagerMock).verifyOrganizationHasTheApp(ORGANISATION_GUID, app);
        verifyNoMoreInteractions(appRepositoryMock, authManagerMock);

        assertThat(app.getGuid()).isNotEqualTo("guid");
    }

    @Test
    void successDeleteApp() {
        val organization = organization();

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
                     .organization(organization)
                     .models(models)
                     .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        appService.deleteApp(ORGANISATION_GUID, APPLICATION_GUID, USER_ID);

        verify(appRepositoryMock).findByGuid(APPLICATION_GUID);
        verify(appRepositoryMock).deleteById(anyLong());
        verify(authManagerMock).verifyWritePrivilegesToApp(USER_ID, app);
        verify(authManagerMock).verifyReadPrivilegesToApp(USER_ID, app);
        verify(authManagerMock).verifyOrganizationHasTheApp(ORGANISATION_GUID, app);
        verifyNoMoreInteractions(appRepositoryMock, authManagerMock);
    }

    @Test
    void successGetAppRoles() {
        val user = user(USER_ID);
        val organization = organization();

        val app = App.builder()
                     .id(APPLICATION_ID)
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();
        app.addUserAppRole(user, OWNER);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        val result = appService.getAppUsers("", ORGANISATION_GUID, APPLICATION_GUID, USER_ID);

        assertThat(result).hasSize(1);

        verify(appRepositoryMock).findByGuid(APPLICATION_GUID);
        verify(authManagerMock).verifyReadPrivilegesToApp(USER_ID, app);
        verify(authManagerMock).verifyOrganizationHasTheApp(ORGANISATION_GUID, app);
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
                        .build();
        val user2 = User.builder()
                        .id(user2Id)
                        .firstName("Maria")
                        .lastName("Smith")
                        .email("sj@example.com")
                        .build();
        val user3 = User.builder()
                        .id(user3Id)
                        .firstName("Steve")
                        .lastName("Jobs")
                        .email("sj@example.com")
                        .build();

        val organization = organization();
        organization.addUserOrganizationRole(user1, USER);
        organization.addUserOrganizationRole(user2, USER);
        organization.addUserOrganizationRole(user3, USER);

        val app = App.builder()
                     .id(APPLICATION_ID)
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();
        app.addUserAppRole(user1, OWNER);
        app.addUserAppRole(user2, AppRole.USER);
        app.addUserAppRole(user3, AppRole.USER);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        val result = appService.getAppUsers("smith", ORGANISATION_GUID, APPLICATION_GUID, USER_ID);

        assertThat(result).hasSize(2);

        verify(appRepositoryMock).findByGuid(APPLICATION_GUID);
        verify(authManagerMock).verifyReadPrivilegesToApp(USER_ID, app);
        verify(authManagerMock).verifyOrganizationHasTheApp(ORGANISATION_GUID, app);
        verifyNoMoreInteractions(appRepositoryMock, authManagerMock);
    }

    @Test
    void successUserInvite() {
        val userEmail = "email";
        val userInviteDto = UserInviteDto.builder()
                                         .userEmail("userEmail")
                                         .role(AppRole.USER.toString())
                                         .build();
        val admin = user(USER_ID);

        val userId = nextLong();
        val userRole = AppRole.USER;
        val user = User.builder()
                       .id(userId)
                       .email(userEmail)
                       .build();

        val organization = organization();
        organization.addUserOrganizationRole(admin, ADMINISTRATOR);
        organization.addUserOrganizationRole(user, USER);

        val app = App.builder()
                     .id(APPLICATION_ID)
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(userServiceMock.getUser(anyString())).thenReturn(user);
        when(appRepositoryMock.save(any())).thenReturn(app);

        val userAppRole = appService.inviteUser(userInviteDto, ORGANISATION_GUID, APPLICATION_GUID, USER_ID);

        assertThat(userAppRole.getUser().getEmail()).isEqualTo(userEmail);
        assertThat(userAppRole.getRole()).isEqualTo(userRole);

        verify(authManagerMock).verifyWritePrivilegesToApp(USER_ID, app);
        verify(authManagerMock).verifyReadPrivilegesToApp(USER_ID, app);
        verify(authManagerMock).verifyOrganizationHasTheApp(ORGANISATION_GUID, app);
        verifyNoMoreInteractions(authManagerMock);
    }

    @Test
    void successUserWithAppRoleOwnerInvite() {
        val admin = user(USER_ID);

        val userEmail = "email";
        val user = User.builder()
                       .id(nextLong())
                       .email(userEmail)
                       .build();

        val userRole = AppRole.OWNER;
        val userInviteDto = UserInviteDto.builder()
                                         .userEmail(userEmail)
                                         .role(userRole.toString())
                                         .build();

        val organization = organization();
        organization.addUserOrganizationRole(admin, ADMINISTRATOR);
        organization.addUserOrganizationRole(user, USER);

        val app = App.builder()
                     .id(APPLICATION_ID)
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();
        app.addUserAppRole(admin, userRole);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(userServiceMock.getUser(anyString())).thenReturn(user);
        when(appRepositoryMock.save(any())).thenReturn(app);

        val actual = appService.inviteUser(userInviteDto, ORGANISATION_GUID, APPLICATION_GUID, USER_ID);

        assertThat(actual.getUser().getEmail()).isEqualTo(userEmail);
        assertThat(actual.getRole()).isEqualTo(userRole);
        assertThat(app.getOwner().get().getRole()).isEqualTo(userRole);

        verify(appRepositoryMock).save(app);
        verify(authManagerMock).verifyWritePrivilegesToApp(USER_ID, app);
        verify(authManagerMock).verifyReadPrivilegesToApp(USER_ID, app);
        verify(authManagerMock).verifyOrganizationHasTheApp(ORGANISATION_GUID, app);
        verifyNoMoreInteractions(authManagerMock);
    }

    @Test
    void failUserInviteUserAlreadyHasAccessToApp() {
        val userInviteDto = UserInviteDto.builder()
                                         .userEmail("email")
                                         .role(AppRole.USER.toString())
                                         .build();
        val userId = nextLong();
        val admin = user(USER_ID);
        val user = user(userId);

        val organization = organization();
        organization.addUserOrganizationRole(admin, ADMINISTRATOR);
        organization.addUserOrganizationRole(user, USER);

        val app = App.builder()
                     .id(APPLICATION_ID)
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();
        app.addUserAppRole(user, AppRole.USER);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(userServiceMock.getUser(anyString())).thenReturn(user);

        assertThatThrownBy(() -> {
            appService.inviteUser(userInviteDto, ORGANISATION_GUID, APPLICATION_GUID, USER_ID);
        }).isInstanceOf(UserAlreadyHasAccessToAppException.class);
    }

    @Test
    void deleteUserFromApp() {
        val userGuid = randomAlphabetic(36);
        val userId = nextLong();
        val user = user(userId);
        user.setGuid(userGuid);

        val app = App.builder()
                     .id(APPLICATION_ID)
                     .guid(APPLICATION_GUID)
                     .build();
        app.addUserAppRole(user, AppRole.USER);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(appRepositoryMock.save(any())).thenReturn(app);
        when(userServiceMock.getUserByGuid(any())).thenReturn(user);

        assertThat(app.getUserAppRoles()).hasSize(1);
        assertThat(app.getUserAppRoles()).allSatisfy(
                userAppRole -> {
                    userAppRole.getUser().getGuid().equals(userGuid);
                    user.getUserAppRoles().contains(userAppRole);
                }
        );

        appService.deleteUserFromApp(userGuid, ORGANISATION_GUID, APPLICATION_GUID, USER_ID);

        assertThat(app.getUserAppRoles()).isEmpty();
        assertThat(user.getUserAppRoles()).isEmpty();

        verify(appRepositoryMock).findByGuid(APPLICATION_GUID);
        verify(appRepositoryMock).save(any());
        verify(authManagerMock).verifyWritePrivilegesToApp(USER_ID, app);
        verify(authManagerMock).verifyReadPrivilegesToApp(userId, app);
        verify(authManagerMock).verifyOrganizationHasTheApp(ORGANISATION_GUID, app);
        verifyNoMoreInteractions(appRepositoryMock, authManagerMock);
    }

    @Test
    void successPassAllOwnedAppsToNewOwnerAndLeave() {
        val defaultOrg = organization();
        defaultOrg.setUserOrganizationRoles(List.of(makeRole(1L, USER), makeRole(2L, ADMINISTRATOR)));
        val app1 = mock(App.class);
        val app2 = mock(App.class);
        val app3 = mock(App.class);
        val app4 = mock(App.class);
        val apps = List.of(app1, app2, app3, app4);

        val oldOwner = user(1L);
        val newOwner = user(2L);

        when(app1.getUserAppRole(1L)).thenReturn(Optional.of(UserAppRole.builder().role(OWNER).build()));
        when(app2.getUserAppRole(1L)).thenReturn(Optional.of(UserAppRole.builder().role(OWNER).build()));
        when(app3.getUserAppRole(1L)).thenReturn(Optional.of(UserAppRole.builder().role(AppRole.ADMINISTRATOR).build()));
        when(app4.getUserAppRole(1L)).thenReturn(Optional.of(UserAppRole.builder().role(AppRole.USER).build()));

        when(organizationServiceMock.getDefaultOrg()).thenReturn(defaultOrg);
        when(organizationServiceMock.getOrganization(defaultOrg.getGuid())).thenReturn(defaultOrg);
        when(appRepositoryMock.findAllByOrganizationIdAndUserAppRoles_Id_UserId(anyLong(), anyLong())).thenReturn(apps);

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
        val user = user(USER_ID);
        val organization = organization();

        val app = App.builder()
                     .id(APPLICATION_ID)
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();
        app.addUserAppRole(user, OWNER);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        val actual = appService.getAppRolesToAssign(ORGANISATION_GUID, APPLICATION_GUID, USER_ID);

        assertThat(actual).hasSize(AppRole.values().length);

        verify(authManagerMock).verifyReadPrivilegesToApp(USER_ID, app);
        verify(authManagerMock).verifyOrganizationHasTheApp(ORGANISATION_GUID, app);
        verifyNoMoreInteractions(authManagerMock);
    }

    @Test
    void getAppRolesToAssignReturnsToUser() {
        val user = user(USER_ID);
        val organization = organization();
        organization.addUserOrganizationRole(user, USER);

        val app = App.builder()
                     .id(APPLICATION_ID)
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        val actual = appService.getAppRolesToAssign(ORGANISATION_GUID, APPLICATION_GUID, USER_ID);

        assertThat(actual).isEmpty();

        verify(authManagerMock).verifyReadPrivilegesToApp(USER_ID, app);
        verify(authManagerMock).verifyOrganizationHasTheApp(ORGANISATION_GUID, app);
        verifyNoMoreInteractions(authManagerMock);
    }

    @Test
    void getAppRolesToAssignReturnsDefault() {
        val user = user(USER_ID);
        val organization = organization();
        organization.addUserOrganizationRole(user, OrganizationRole.OWNER);

        val app = App.builder()
                     .id(APPLICATION_ID)
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        val actual = appService.getAppRolesToAssign(ORGANISATION_GUID, APPLICATION_GUID, USER_ID);

        assertThat(actual).hasSize(AppRole.values().length);

        verify(authManagerMock).verifyReadPrivilegesToApp(USER_ID, app);
        verify(authManagerMock).verifyOrganizationHasTheApp(ORGANISATION_GUID, app);
        verifyNoMoreInteractions(authManagerMock);
    }

    @Test
    void getAppRolesToAssignThrowsExceptionIfNoOrgUser() {
        val organization = organization();

        val app = App.builder()
                     .id(APPLICATION_ID)
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        assertThatThrownBy(() -> {
            appService.getAppRolesToAssign(ORGANISATION_GUID, APPLICATION_GUID, USER_ID);
        }).isInstanceOf(UserDoesNotBelongToOrganization.class);

        verify(authManagerMock).verifyReadPrivilegesToApp(USER_ID, app);
        verify(authManagerMock).verifyOrganizationHasTheApp(ORGANISATION_GUID, app);
        verifyNoMoreInteractions(authManagerMock);
    }

    @Test
    void successGenerateUuidToRequestModelShare() {
        val user = user(USER_ID);
        val organization = organization();
        organization.addUserOrganizationRole(user, OrganizationRole.OWNER);

        val app = App.builder()
                     .id(APPLICATION_ID)
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();

        val authentication = Mockito.mock(Authentication.class);
        val securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(authentication.getPrincipal()).thenReturn(User.builder().id(USER_ID).build());
        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        val actual = appService.generateUuidToRequestModelShare(ORGANISATION_GUID, APPLICATION_GUID);

        assertThat(actual).isNotNull();

        verify(authManagerMock).verifyWritePrivilegesToApp(USER_ID, app);
        verify(authManagerMock).verifyOrganizationHasTheApp(ORGANISATION_GUID, app);
        verify(modelShareRequestRepositoryMock).save(any(ModelShareRequest.class));
        verifyNoMoreInteractions(authManagerMock, modelShareRequestRepositoryMock);
    }

    @Test
    void successUpdateUserAppRole() {
        val userRoleUpdateDto = UserRoleUpdateDto.builder()
                                                 .userId("userGuid")
                                                 .role(OWNER.toString())
                                                 .build();
        val user = user(USER_ID);
        val organization = organization();

        val app = App.builder()
                     .name("name")
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .userAppRoles(List.of(UserAppRole.builder()
                                                      .id(new UserAppRoleId(USER_ID, APPLICATION_ID))
                                                      .role(AppRole.ADMINISTRATOR)
                                                      .build()
                     ))
                     .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(userServiceMock.getUserByGuid(any())).thenReturn(user);

        appService.updateUserAppRole(userRoleUpdateDto, ORGANISATION_GUID, APPLICATION_GUID, ADMIN_ID);

        verify(authManagerMock).verifyWritePrivilegesToApp(ADMIN_ID, app);
        verify(authManagerMock).verifyReadPrivilegesToApp(ADMIN_ID, app);
        verify(authManagerMock).verifyOrganizationHasTheApp(ORGANISATION_GUID, app);
        verify(appRepositoryMock).save(any(App.class));
        verifyNoMoreInteractions(authManagerMock);
    }

    private UserOrganizationRole makeRole(final long userId, final OrganizationRole role) {
        return UserOrganizationRole.builder()
                                   .id(UserOrganizationRoleId.builder()
                                                             .userId(userId)
                                                             .build()
                                   )
                                   .role(role)
                                   .user(user(userId))
                                   .build();
    }
}