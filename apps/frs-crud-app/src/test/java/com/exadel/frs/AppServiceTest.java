package com.exadel.frs;

import com.exadel.frs.entity.App;
import com.exadel.frs.entity.Organization;
import com.exadel.frs.entity.User;
import com.exadel.frs.enums.AppRole;
import com.exadel.frs.enums.OrganizationRole;
import com.exadel.frs.exception.*;
import com.exadel.frs.repository.AppRepository;
import com.exadel.frs.service.AppService;
import com.exadel.frs.service.OrganizationService;
import com.exadel.frs.service.UserService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

public class AppServiceTest {

    private static final String APPLICATION_GUID = "app-guid";
    private static final String ORGANISATION_GUID = "org-guid";
    private static final long APPLICATION_ID = 1L;
    private static final long ORGANISATION_ID = 2L;
    private static final long USER_ID = 3L;

    private AppRepository appRepositoryMock;
    private OrganizationService organizationServiceMock;
    private UserService userServiceMock;
    private AppService appService;

    public AppServiceTest() {
        appRepositoryMock = mock(AppRepository.class);
        organizationServiceMock = mock(OrganizationService.class);
        userServiceMock = mock(UserService.class);
        appService = new AppService(appRepositoryMock, organizationServiceMock, userServiceMock);
    }

    private User user(Long id) {
        return User.builder()
                .id(id)
                .build();
    }

    private Organization organization(Long id) {
        return Organization.builder()
                .id(id)
                .build();
    }

    private static Stream<Arguments> writeRoles() {
        return Stream.of(Arguments.of(OrganizationRole.OWNER),
                Arguments.of(OrganizationRole.ADMINISTRATOR));
    }

    private static Stream<Arguments> readRoles() {
        return Stream.of(Arguments.of(OrganizationRole.USER));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void successGetApp(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization(ORGANISATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);

        App result = appService.getApp(APPLICATION_GUID, USER_ID);

        assertThat(result.getId(), Matchers.is(APPLICATION_ID));
    }

    @Test
    public void failGetAppUserDoesNotBelongToOrganization() {
        Organization organization = organization(ORGANISATION_ID);

        App app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);

        Assertions.assertThrows(UserDoesNotBelongToOrganization.class, () -> appService.getApp(APPLICATION_GUID, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    public void successGetAppOrganizationUser(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization(ORGANISATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();
        app.addUserAppRole(user, AppRole.USER);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);

        App result = appService.getApp(APPLICATION_GUID, USER_ID);

        assertThat(result.getId(), Matchers.is(APPLICATION_ID));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    public void failGetAppInsufficientPrivileges(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization(ORGANISATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> appService.getApp(APPLICATION_GUID, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void successGetApps(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization(ORGANISATION_ID);
        organization.setGuid(ORGANISATION_GUID);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        when(appRepositoryMock.findAllByOrganizationId(anyLong())).thenReturn(List.of(app));
        when(organizationServiceMock.getOrganization(ORGANISATION_GUID)).thenReturn(organization);

        List<App> result = appService.getApps(ORGANISATION_GUID, USER_ID);

        assertThat(result.size(), is(1));
    }

    @Test
    public void failGetAppsUserDoesNotBelongToOrganization() {
        Organization organization = organization(ORGANISATION_ID);
        organization.setGuid(ORGANISATION_GUID);

        when(organizationServiceMock.getOrganization(ORGANISATION_GUID)).thenReturn(organization);

        Assertions.assertThrows(UserDoesNotBelongToOrganization.class, () -> appService.getApps(ORGANISATION_GUID, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    public void successGetAppsOrganizationUser(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization(ORGANISATION_ID);
        organization.setGuid(ORGANISATION_GUID);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();
        app.addUserAppRole(user, AppRole.USER);

        when(appRepositoryMock.findAllByOrganizationIdAndUserAppRoles_Id_UserId(anyLong(), anyLong())).thenReturn(List.of(app));
        when(organizationServiceMock.getOrganization(ORGANISATION_GUID)).thenReturn(organization);

        List<App> result = appService.getApps(ORGANISATION_GUID, USER_ID);

        assertThat(result.size(), is(1));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void successCreateApp(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization(ORGANISATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .name("name")
                .organization(organization)
                .build();

        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);
        when(userServiceMock.getUser(anyLong())).thenReturn(user);

        appService.createApp(app, USER_ID);

        verify(appRepositoryMock).save(any(App.class));

        assertThat(app.getGuid(), not(isEmptyOrNullString()));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void failCreateOrganizationNameIsNotUnique(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization(ORGANISATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .name("app")
                .organization(organization)
                .build();

        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);
        when(appRepositoryMock.findByNameAndOrganizationId(anyString(), anyLong())).thenReturn(Optional.of(app));

        Assertions.assertThrows(NameIsNotUniqueException.class, () -> appService.createApp(app, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void failCreateAppEmptyName(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization(ORGANISATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .organization(organization)
                .build();

        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);
        when(userServiceMock.getUser(anyLong())).thenReturn(user);

        Assertions.assertThrows(EmptyRequiredFieldException.class, () -> appService.createApp(app, USER_ID));
    }

    @Test
    public void failCreateAppUserDoesNotBelongToOrganization() {
        User user = user(USER_ID);

        Organization organization = organization(ORGANISATION_ID);

        App app = App.builder()
                .name("name")
                .organization(organization)
                .build();

        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);
        when(userServiceMock.getUser(anyLong())).thenReturn(user);

        Assertions.assertThrows(UserDoesNotBelongToOrganization.class, () -> appService.createApp(app, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    public void failCreateAppInsufficientPrivileges(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization(ORGANISATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .name("name")
                .organization(organization)
                .build();

        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);
        when(userServiceMock.getUser(anyLong())).thenReturn(user);

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> appService.createApp(app, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void successUpdateApp(OrganizationRole organizationRole) {
        User user1 = user(USER_ID);
        User user2 = user(4L);

        Organization organization = organization(ORGANISATION_ID);
        organization.addUserOrganizationRole(user1, organizationRole);
        organization.addUserOrganizationRole(user2, OrganizationRole.USER);

        App repoApp = App.builder()
                .name("name")
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();
        repoApp.addUserAppRole(user1, AppRole.OWNER);

        App app = App.builder()
                .name("new_name")
                .guid("new_guid")
                .build();
        app.addUserAppRole(user2, AppRole.OWNER);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(repoApp));
        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);

        appService.updateApp(APPLICATION_GUID, app, USER_ID);

        verify(appRepositoryMock).save(any(App.class));

        assertThat(repoApp.getName(), is(app.getName()));
        assertThat(repoApp.getGuid(), is("guid"));
        assertThat(repoApp.getUserAppRoles().size(), is(1));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void failUpdateAppSelfRoleChange(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization(ORGANISATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        App repoApp = App.builder()
                .name("name")
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();
        repoApp.addUserAppRole(user, AppRole.OWNER);

        App appUpdate = App.builder().build();
        appUpdate.addUserAppRole(user, AppRole.USER);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(repoApp));
        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);

        Assertions.assertThrows(SelfRoleChangeException.class, () -> appService.updateApp(APPLICATION_GUID, appUpdate, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void failUpdateAppMultipleOwners(OrganizationRole organizationRole) {
        User user1 = user(USER_ID);
        User user2 = user(4L);
        User user3 = user(5L);

        Organization organization = organization(ORGANISATION_ID);
        organization.addUserOrganizationRole(user1, organizationRole);

        App repoApp = App.builder()
                .name("name")
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();
        repoApp.addUserAppRole(user1, AppRole.OWNER);

        App appUpdate = App.builder().build();
        appUpdate.addUserAppRole(user2, AppRole.OWNER);
        appUpdate.addUserAppRole(user3, AppRole.OWNER);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(repoApp));
        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);

        Assertions.assertThrows(MultipleOwnersException.class, () -> appService.updateApp(APPLICATION_GUID, appUpdate, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void failUpdateAppNameIsNotUnique(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization(ORGANISATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        App repoApp = App.builder()
                .name("name")
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();
        repoApp.addUserAppRole(user, AppRole.OWNER);

        App appUpdate = App.builder()
                .name("new_name")
                .build();

        when(appRepositoryMock.findByNameAndOrganizationId(anyString(), anyLong())).thenReturn(Optional.of(appUpdate));
        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(repoApp));
        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);

        Assertions.assertThrows(NameIsNotUniqueException.class, () -> appService.updateApp(APPLICATION_GUID, appUpdate, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    public void failUpdateAppInsufficientPrivileges(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization(ORGANISATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .organization(organization)
                .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> appService.updateApp(APPLICATION_GUID, app, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void failUpdateAppUserDoesNotExist(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization(ORGANISATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        App repoApp = App.builder()
                .name("name")
                .organization(organization)
                .build();

        App app = App.builder()
                .build();
        app.addUserAppRole(user(2L), AppRole.USER);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(repoApp));
        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);

        Assertions.assertThrows(UserDoesNotBelongToOrganization.class, () -> appService.updateApp(APPLICATION_GUID, app, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void successRegenerateGuid(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization(ORGANISATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .name("name")
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);

        appService.regenerateApiKey(APPLICATION_GUID, USER_ID);

        verify(appRepositoryMock).save(any(App.class));

        assertThat(app.getGuid(), not("guid"));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    public void failRegenerateGuidInsufficientPrivileges(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization(ORGANISATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .name("name")
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> appService.regenerateApiKey(APPLICATION_GUID, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void successDeleteApp(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization(ORGANISATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(APPLICATION_ID)
                .name("name")
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);

        appService.deleteApp(APPLICATION_GUID, USER_ID);

        verify(appRepositoryMock).deleteById(anyLong());
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    public void failDeleteAppInsufficientPrivileges(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization(ORGANISATION_ID);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .name("name")
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> appService.deleteApp(APPLICATION_GUID, USER_ID));
    }

}
