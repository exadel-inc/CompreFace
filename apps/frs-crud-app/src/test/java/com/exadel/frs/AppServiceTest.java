package com.exadel.frs;

import com.exadel.frs.entity.App;
import com.exadel.frs.entity.Organization;
import com.exadel.frs.entity.User;
import com.exadel.frs.entity.UserAppRole;
import com.exadel.frs.enums.AppRole;
import com.exadel.frs.enums.OrganizationRole;
import com.exadel.frs.exception.*;
import com.exadel.frs.repository.AppRepository;
import com.exadel.frs.service.AppService;
import com.exadel.frs.service.OrganizationService;
import com.exadel.frs.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

class AppServiceTest {

    private static final String APPLICATION_GUID = "app-guid";
    private static final String ORGANISATION_GUID = "org-guid";
    private static final long APPLICATION_ID = 1L;
    private static final long ORGANISATION_ID = 2L;
    private static final long USER_ID = 3L;

    private AppRepository appRepositoryMock;
    private OrganizationService organizationServiceMock;
    private UserService userServiceMock;
    private AppService appService;

    AppServiceTest() {
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

    private Organization organization() {
        return Organization.builder()
                .id(ORGANISATION_ID)
                .guid(ORGANISATION_GUID)
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
    void successGetApp(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization();
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        App result = appService.getApp(APPLICATION_GUID, USER_ID);

        assertThat(result.getId(), is(APPLICATION_ID));
    }

    @Test
    void failGetAppUserDoesNotBelongToOrganization() {
        Organization organization = organization();

        App app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        Assertions.assertThrows(UserDoesNotBelongToOrganization.class, () -> appService.getApp(APPLICATION_GUID, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void successGetAppOrganizationUser(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization();
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();
        app.addUserAppRole(user, AppRole.USER);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        App result = appService.getApp(APPLICATION_GUID, USER_ID);

        assertThat(result.getId(), is(APPLICATION_ID));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void failGetAppInsufficientPrivileges(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization();
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> appService.getApp(APPLICATION_GUID, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void successGetApps(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization();
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
    void failGetAppsUserDoesNotBelongToOrganization() {
        Organization organization = organization();
        organization.setGuid(ORGANISATION_GUID);

        when(organizationServiceMock.getOrganization(ORGANISATION_GUID)).thenReturn(organization);

        Assertions.assertThrows(UserDoesNotBelongToOrganization.class, () -> appService.getApps(ORGANISATION_GUID, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void successGetAppsOrganizationUser(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization();
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
    void successCreateApp(OrganizationRole organizationRole) {
        String appName = "appName";
        User user = user(USER_ID);

        Organization organization = organization();
        organization.addUserOrganizationRole(user, organizationRole);

        when(organizationServiceMock.getOrganization(anyString())).thenReturn(organization);
        when(userServiceMock.getUser(anyLong())).thenReturn(user);

        appService.createApp(ORGANISATION_GUID, appName, USER_ID);

        ArgumentCaptor<App> varArgs = ArgumentCaptor.forClass(App.class);
        verify(appRepositoryMock).save(varArgs.capture());

        assertThat(varArgs.getValue().getName(), is(appName));
        assertThat(varArgs.getValue().getGuid(), not(isEmptyOrNullString()));
        assertThat(varArgs.getValue().getApiKey(), not(isEmptyOrNullString()));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void failCreateOrganizationNameIsNotUnique(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization();
        organization.addUserOrganizationRole(user, organizationRole);

        when(organizationServiceMock.getOrganization(anyString())).thenReturn(organization);
        when(appRepositoryMock.existsByNameAndOrganizationId(anyString(), anyLong())).thenReturn(true);

        Assertions.assertThrows(NameIsNotUniqueException.class, () -> appService.createApp(ORGANISATION_GUID, "appName", USER_ID));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void failCreateAppEmptyName(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization();
        organization.addUserOrganizationRole(user, organizationRole);

        when(organizationServiceMock.getOrganization(anyString())).thenReturn(organization);
        when(userServiceMock.getUser(anyLong())).thenReturn(user);

        Assertions.assertThrows(EmptyRequiredFieldException.class, () -> appService.createApp(ORGANISATION_GUID, "", USER_ID));
    }

    @Test
    void failCreateAppUserDoesNotBelongToOrganization() {
        User user = user(USER_ID);

        Organization organization = organization();

        when(organizationServiceMock.getOrganization(anyString())).thenReturn(organization);
        when(userServiceMock.getUser(anyLong())).thenReturn(user);

        Assertions.assertThrows(UserDoesNotBelongToOrganization.class, () -> appService.createApp(ORGANISATION_GUID, "appName", USER_ID));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void failCreateAppInsufficientPrivileges(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization();
        organization.addUserOrganizationRole(user, organizationRole);

        when(organizationServiceMock.getOrganization(anyString())).thenReturn(organization);
        when(userServiceMock.getUser(anyLong())).thenReturn(user);

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> appService.createApp(ORGANISATION_GUID, "appName", USER_ID));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void successUpdateApp(OrganizationRole organizationRole) {
        String newAppName = "appName";
        User user = user(USER_ID);

        Organization organization = organization();
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .name("name")
                .organization(organization)
                .build();
        app.addUserAppRole(user, AppRole.OWNER);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        appService.updateApp(APPLICATION_GUID, newAppName, USER_ID);

        ArgumentCaptor<App> varArgs = ArgumentCaptor.forClass(App.class);
        verify(appRepositoryMock).save(varArgs.capture());

        assertThat(varArgs.getValue().getName(), is(newAppName));
    }

//    @ParameterizedTest
//    @MethodSource("writeRoles")
//    void failUpdateAppSelfRoleChange(OrganizationRole organizationRole) {
//        User user = user(USER_ID);
//
//        Organization organization = organization();
//        organization.addUserOrganizationRole(user, organizationRole);
//
//        App repoApp = App.builder()
//                .name("name")
//                .guid(APPLICATION_GUID)
//                .organization(organization)
//                .build();
//        repoApp.addUserAppRole(user, AppRole.OWNER);
//
//        App appUpdate = App.builder().build();
//        appUpdate.addUserAppRole(user, AppRole.USER);
//
//        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(repoApp));
//
//        Assertions.assertThrows(SelfRoleChangeException.class, () -> appService.updateApp(APPLICATION_GUID, appUpdate, USER_ID));
//    }

//    @ParameterizedTest
//    @MethodSource("writeRoles")
//    void failUpdateAppMultipleOwners(OrganizationRole organizationRole) {
//        User user1 = user(USER_ID);
//        User user2 = user(4L);
//        User user3 = user(5L);
//
//        Organization organization = organization();
//        organization.addUserOrganizationRole(user1, organizationRole);
//
//        App repoApp = App.builder()
//                .name("name")
//                .guid(APPLICATION_GUID)
//                .organization(organization)
//                .build();
//        repoApp.addUserAppRole(user1, AppRole.OWNER);
//
//        App appUpdate = App.builder().build();
//        appUpdate.addUserAppRole(user2, AppRole.OWNER);
//        appUpdate.addUserAppRole(user3, AppRole.OWNER);
//
//        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(repoApp));
//
//        Assertions.assertThrows(MultipleOwnersException.class, () -> appService.updateApp(APPLICATION_GUID, appUpdate, USER_ID));
//    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void failUpdateAppNameIsNotUnique(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization();
        organization.addUserOrganizationRole(user, organizationRole);

        App repoApp = App.builder()
                .name("name")
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();
        repoApp.addUserAppRole(user, AppRole.OWNER);

        when(appRepositoryMock.existsByNameAndOrganizationId(anyString(), anyLong())).thenReturn(true);
        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(repoApp));

        Assertions.assertThrows(NameIsNotUniqueException.class, () -> appService.updateApp(APPLICATION_GUID, "new_name", USER_ID));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void failUpdateAppInsufficientPrivileges(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization();
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .organization(organization)
                .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> appService.updateApp(APPLICATION_GUID, "appName", USER_ID));
    }

//    @ParameterizedTest
//    @MethodSource("writeRoles")
//    void failUpdateAppUserDoesNotExist(OrganizationRole organizationRole) {
//        User user = user(USER_ID);
//
//        Organization organization = organization();
//        organization.addUserOrganizationRole(user, organizationRole);
//
//        App repoApp = App.builder()
//                .name("name")
//                .organization(organization)
//                .build();
//
//        App app = App.builder()
//                .build();
//        app.addUserAppRole(user(2L), AppRole.USER);
//
//        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(repoApp));
//
//        Assertions.assertThrows(UserDoesNotBelongToOrganization.class, () -> appService.updateApp(APPLICATION_GUID, app, USER_ID));
//    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void successRegenerateGuid(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization();
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .name("name")
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        appService.regenerateApiKey(APPLICATION_GUID, USER_ID);

        verify(appRepositoryMock).save(any(App.class));

        assertThat(app.getGuid(), not("guid"));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void failRegenerateGuidInsufficientPrivileges(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization();
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .name("name")
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> appService.regenerateApiKey(APPLICATION_GUID, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void successDeleteApp(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization();
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(APPLICATION_ID)
                .name("name")
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        appService.deleteApp(APPLICATION_GUID, USER_ID);

        verify(appRepositoryMock).deleteById(anyLong());
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void failDeleteAppInsufficientPrivileges(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization();
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .name("name")
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> appService.deleteApp(APPLICATION_GUID, USER_ID));
    }

    @ParameterizedTest
    @MethodSource({"readRoles", "writeRoles"})
    void successGetAppRoles(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization();
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();
        app.addUserAppRole(user, AppRole.OWNER);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        List<UserAppRole> result = appService.getAppUsers("", ORGANISATION_GUID, APPLICATION_GUID, USER_ID);

        assertThat(result.size(), is(1));
    }

    @ParameterizedTest
    @MethodSource({"readRoles", "writeRoles"})
    void successAppUsersSearch(OrganizationRole organizationRole) {
        Long user1Id = 1L;
        Long user2Id = 2L;
        Long user3Id = 3L;

        User user1 = User.builder()
                .id(user1Id)
                .firstName("Will")
                .lastName("Smith")
                .email("ws@example.com")
                .build();
        User user2 = User.builder()
                .id(user2Id)
                .firstName("Maria")
                .lastName("Smith")
                .email("sj@example.com")
                .build();
        User user3 = User.builder()
                .id(user3Id)
                .firstName("Steve")
                .lastName("Jobs")
                .email("sj@example.com")
                .build();

        Organization organization = organization();
        organization.addUserOrganizationRole(user1, organizationRole);
        organization.addUserOrganizationRole(user2, OrganizationRole.USER);
        organization.addUserOrganizationRole(user3, OrganizationRole.USER);

        App app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();
        app.addUserAppRole(user1, AppRole.OWNER);
        app.addUserAppRole(user2, AppRole.USER);
        app.addUserAppRole(user3, AppRole.USER);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        List<UserAppRole> result = appService.getAppUsers("smith", ORGANISATION_GUID, APPLICATION_GUID, USER_ID);

        assertThat(result.size(), is(2));
    }

    @Test
    void failGetAppRolesUserDoesNotBelongToOrganization() {
        Organization organization = organization();

        App app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        Assertions.assertThrows(UserDoesNotBelongToOrganization.class, () -> appService.getAppUsers("", ORGANISATION_GUID, APPLICATION_GUID, USER_ID));
    }

    @ParameterizedTest
    @MethodSource({"readRoles", "writeRoles"})
    void failGetAppRolesAppDoesNotBelongToOrg(OrganizationRole organizationRole) {
        User user = user(USER_ID);
        Long org2Id = 3L;
        String org2Guid = "org-guid-3";

        Organization organization2 = Organization.builder()
                .id(org2Id)
                .guid(org2Guid)
                .build();
        organization2.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization2)
                .build();
        app.addUserAppRole(user, AppRole.OWNER);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        Assertions.assertThrows(AppDoesNotBelongToOrgException.class, () -> appService.getAppUsers("", ORGANISATION_GUID, APPLICATION_GUID, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void successUserInvite(OrganizationRole organizationRole) {
        User admin = user(USER_ID);

        Long userId = 4L;
        String userEmail = "email@example.com";
        AppRole userRole = AppRole.USER;
        User user = User.builder()
                .id(userId)
                .email(userEmail)
                .build();

        Organization organization = organization();
        organization.addUserOrganizationRole(admin, organizationRole);
        organization.addUserOrganizationRole(user, OrganizationRole.USER);

        App app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(userServiceMock.getUser(anyString())).thenReturn(user);
        when(appRepositoryMock.save(any())).thenReturn(app);

        UserAppRole userAppRole = appService.inviteUser(userEmail, userRole, ORGANISATION_GUID, APPLICATION_GUID, USER_ID);

        assertThat(userAppRole.getUser().getEmail(), is(userEmail));
        assertThat(userAppRole.getRole(), is(userRole));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void failUserInviteInsufficientPrivileges(OrganizationRole organizationRole) {
        User user = user(USER_ID);

        Organization organization = organization();
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> appService.inviteUser(null, null, ORGANISATION_GUID, APPLICATION_GUID, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void failUserInviteAppDoesNotBelongToOrg(OrganizationRole organizationRole) {
        User admin = user(USER_ID);

        Organization organization = organization();
        organization.addUserOrganizationRole(admin, organizationRole);

        App app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        Assertions.assertThrows(AppDoesNotBelongToOrgException.class, () -> appService.inviteUser(null, null, "org-guid-2", APPLICATION_GUID, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void failUserInviteUserDoesNotBelongToOrg(OrganizationRole organizationRole) {
        Long userId = 4L;
        User admin = user(USER_ID);
        User user = user(userId);

        Organization organization = organization();
        organization.addUserOrganizationRole(admin, organizationRole);

        App app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(userServiceMock.getUser(anyString())).thenReturn(user);

        Assertions.assertThrows(UserDoesNotBelongToOrganization.class, () -> appService.inviteUser("email", null, ORGANISATION_GUID, APPLICATION_GUID, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void failUserInviteUserAlreadyHasAccessToApp(OrganizationRole organizationRole) {
        Long userId = 4L;
        User admin = user(USER_ID);
        User user = user(userId);

        Organization organization = organization();
        organization.addUserOrganizationRole(admin, organizationRole);
        organization.addUserOrganizationRole(user, OrganizationRole.USER);

        App app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .organization(organization)
                .build();
        app.addUserAppRole(user, AppRole.USER);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(userServiceMock.getUser(anyString())).thenReturn(user);

        Assertions.assertThrows(UserAlreadyHasAccessToAppException.class, () -> appService.inviteUser("email", AppRole.USER, ORGANISATION_GUID, APPLICATION_GUID, USER_ID));
    }

}
