package com.exadel.frs;

import com.exadel.frs.entity.App;
import com.exadel.frs.entity.Organization;
import com.exadel.frs.entity.User;
import com.exadel.frs.enums.AppRole;
import com.exadel.frs.enums.OrganizationRole;
import com.exadel.frs.exception.EmptyRequiredFieldException;
import com.exadel.frs.exception.InsufficientPrivilegesException;
import com.exadel.frs.exception.UserDoesNotBelongToOrganization;
import com.exadel.frs.repository.AppRepository;
import com.exadel.frs.service.AppService;
import com.exadel.frs.service.OrganizationService;
import com.exadel.frs.service.UserService;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
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
    public void successGetAppOrganizationOwner(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long appId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = organization(organizationId);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(appId)
                .organization(organization)
                .build();

        when(appRepositoryMock.findById(anyLong())).thenReturn(Optional.of(app));
        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);

        App result = appService.getApp(appId, userId);

        assertThat(result.getId(), is(appId));
    }

    @Test
    public void failGetAppUserDoesNotBelongToOrganization() {
        Long userId = 1L;
        Long appId = 1L;
        Long organizationId = 1L;

        Organization organization = organization(organizationId);

        App app = App.builder()
                .id(appId)
                .organization(organization)
                .build();

        when(appRepositoryMock.findById(anyLong())).thenReturn(Optional.of(app));
        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);

        Assertions.assertThrows(UserDoesNotBelongToOrganization.class, () -> appService.getApp(appId, userId));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    public void successGetAppOrganizationUser(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long appId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = organization(organizationId);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(appId)
                .organization(organization)
                .build();
        app.addUserAppRole(user, AppRole.USER);

        when(appRepositoryMock.findById(anyLong())).thenReturn(Optional.of(app));
        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);

        App result = appService.getApp(appId, userId);

        assertThat(result.getId(), is(appId));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    public void failGetAppInsufficientPrivileges(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long appId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = organization(organizationId);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(appId)
                .organization(organization)
                .build();

        when(appRepositoryMock.findById(anyLong())).thenReturn(Optional.of(app));
        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> appService.getApp(appId, userId));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void successGetAppsOrganizationOwner(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long appId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = organization(organizationId);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(appId)
                .organization(organization)
                .build();

        when(appRepositoryMock.findAllByOrganizationId(anyLong())).thenReturn(List.of(app));
        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);

        List<App> result = appService.getApps(organizationId, userId);

        assertThat(result.size(), is(1));
    }

    @Test
    public void failGetAppsUserDoesNotBelongToOrganization() {
        Long userId = 1L;
        Long organizationId = 1L;

        Organization organization = organization(organizationId);

        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);

        Assertions.assertThrows(UserDoesNotBelongToOrganization.class, () -> appService.getApps(organizationId, userId));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    public void successGetAppsOrganizationUser(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long appId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = organization(organizationId);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .id(appId)
                .organization(organization)
                .build();
        app.addUserAppRole(user, AppRole.USER);

        when(appRepositoryMock.findAllByOrganizationIdAndUserAppRoles_Id_UserId(anyLong(), anyLong())).thenReturn(List.of(app));
        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);

        List<App> result = appService.getApps(organizationId, userId);

        assertThat(result.size(), is(1));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void successCreateAppOrganizationOwner(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = organization(organizationId);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .name("name")
                .organization(organization)
                .build();

        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);
        when(userServiceMock.getUser(anyLong())).thenReturn(user);

        appService.createApp(app, userId);

        verify(appRepositoryMock).save(any(App.class));

        assertThat(app.getGuid(), not(isEmptyOrNullString()));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void failCreateAppEmptyName(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = organization(organizationId);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .organization(organization)
                .build();

        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);
        when(userServiceMock.getUser(anyLong())).thenReturn(user);

        Assertions.assertThrows(EmptyRequiredFieldException.class, () -> appService.createApp(app, userId));
    }

    @Test
    public void failCreateAppUserDoesNotBelongToOrganization() {
        Long userId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = organization(organizationId);

        App app = App.builder()
                .name("name")
                .organization(organization)
                .build();

        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);
        when(userServiceMock.getUser(anyLong())).thenReturn(user);

        Assertions.assertThrows(UserDoesNotBelongToOrganization.class, () -> appService.createApp(app, userId));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    public void failCreateAppInsufficientPrivileges(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = organization(organizationId);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .name("name")
                .organization(organization)
                .build();

        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);
        when(userServiceMock.getUser(anyLong())).thenReturn(user);

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> appService.createApp(app, userId));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void successUpdateAppOrganizationOwner(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long appId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = organization(organizationId);
        organization.addUserOrganizationRole(user, organizationRole);

        App repoApp = App.builder()
                .name("name")
                .guid("guid")
                .organization(organization)
                .build();

        App app = App.builder()
                .name("new_name")
                .guid("new_guid")
                .build();
        app.addUserAppRole(user, AppRole.USER);

        when(appRepositoryMock.findById(anyLong())).thenReturn(Optional.of(repoApp));
        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);

        appService.updateApp(appId, app, userId);

        verify(appRepositoryMock).save(any(App.class));

        assertThat(repoApp.getName(), is(app.getName()));
        assertThat(repoApp.getGuid(), is("guid"));
        assertThat(repoApp.getUserAppRoles().size(), is(1));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    public void failUpdateAppInsufficientPrivileges(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long appId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = organization(organizationId);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .organization(organization)
                .build();

        when(appRepositoryMock.findById(anyLong())).thenReturn(Optional.of(app));
        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> appService.updateApp(appId, app, userId));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void failUpdateAppUserDoesNotExist(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long appId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = organization(organizationId);
        organization.addUserOrganizationRole(user, organizationRole);

        App repoApp = App.builder()
                .name("name")
                .organization(organization)
                .build();

        App app = App.builder()
                .build();
        app.addUserAppRole(user(2L), AppRole.USER);

        when(appRepositoryMock.findById(anyLong())).thenReturn(Optional.of(repoApp));
        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);

        Assertions.assertThrows(UserDoesNotBelongToOrganization.class, () -> appService.updateApp(appId, app, userId));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void successRegenerateGuid(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long appId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = organization(organizationId);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .name("name")
                .guid("guid")
                .organization(organization)
                .build();

        when(appRepositoryMock.findById(anyLong())).thenReturn(Optional.of(app));
        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);

        appService.regenerateGuid(appId, userId);

        verify(appRepositoryMock).save(any(App.class));

        assertThat(app.getGuid(), not("guid"));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    public void failRegenerateGuidInsufficientPrivileges(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long appId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = organization(organizationId);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .name("name")
                .guid("guid")
                .organization(organization)
                .build();

        when(appRepositoryMock.findById(anyLong())).thenReturn(Optional.of(app));
        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> appService.regenerateGuid(appId, userId));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void successDeleteApp(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long appId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = organization(organizationId);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .name("name")
                .organization(organization)
                .build();

        when(appRepositoryMock.findById(anyLong())).thenReturn(Optional.of(app));
        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);

        appService.deleteApp(appId, userId);

        verify(appRepositoryMock).deleteById(anyLong());
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    public void failDeleteAppInsufficientPrivileges(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long appId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = organization(organizationId);
        organization.addUserOrganizationRole(user, organizationRole);

        App app = App.builder()
                .name("name")
                .guid("guid")
                .organization(organization)
                .build();

        when(appRepositoryMock.findById(anyLong())).thenReturn(Optional.of(app));
        when(organizationServiceMock.getOrganization(anyLong())).thenReturn(organization);

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> appService.deleteApp(appId, userId));
    }

}
