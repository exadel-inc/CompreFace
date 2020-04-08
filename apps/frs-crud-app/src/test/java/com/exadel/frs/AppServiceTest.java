package com.exadel.frs;

import static com.exadel.frs.enums.AppRole.OWNER;
import static com.exadel.frs.enums.OrganizationRole.ADMINISTRATOR;
import static com.exadel.frs.enums.OrganizationRole.USER;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import com.exadel.frs.dto.ui.AppCreateDto;
import com.exadel.frs.dto.ui.AppUpdateDto;
import com.exadel.frs.dto.ui.UserInviteDto;
import com.exadel.frs.dto.ui.UserRoleUpdateDto;
import com.exadel.frs.entity.App;
import com.exadel.frs.entity.Organization;
import com.exadel.frs.entity.User;
import com.exadel.frs.enums.AppRole;
import com.exadel.frs.enums.OrganizationRole;
import com.exadel.frs.exception.AppDoesNotBelongToOrgException;
import com.exadel.frs.exception.EmptyRequiredFieldException;
import com.exadel.frs.exception.InsufficientPrivilegesException;
import com.exadel.frs.exception.NameIsNotUniqueException;
import com.exadel.frs.exception.SelfRoleChangeException;
import com.exadel.frs.exception.UserAlreadyHasAccessToAppException;
import com.exadel.frs.exception.UserDoesNotBelongToOrganization;
import com.exadel.frs.repository.AppRepository;
import com.exadel.frs.repository.ModelShareRequestRepository;
import com.exadel.frs.service.AppService;
import com.exadel.frs.service.OrganizationService;
import com.exadel.frs.service.UserService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

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
    private ModelShareRequestRepository modelShareRequestRepository;

    AppServiceTest() {
        appRepositoryMock = mock(AppRepository.class);
        organizationServiceMock = mock(OrganizationService.class);
        userServiceMock = mock(UserService.class);
        modelShareRequestRepository = mock(ModelShareRequestRepository.class);
        appService = new AppService(appRepositoryMock, organizationServiceMock, userServiceMock, modelShareRequestRepository);
    }

    private User user(final Long id) {
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
        return Stream.of(
                Arguments.of(OrganizationRole.OWNER),
                Arguments.of(ADMINISTRATOR)
        );
    }

    private static Stream<Arguments> readRoles() {
        return Stream.of(Arguments.of(USER));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void successGetApp(final OrganizationRole organizationRole) {
        val user = user(USER_ID);

        val organization = organization();
        organization.addUserOrganizationRole(user, organizationRole);

        val app = App.builder()
                     .id(APPLICATION_ID)
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        val result = appService.getApp(ORGANISATION_GUID, APPLICATION_GUID, USER_ID);

        assertThat(result.getId()).isEqualTo(APPLICATION_ID);
    }

    @Test
    void failGetAppUserDoesNotBelongToOrganization() {
        val organization = organization();

        val app = App.builder()
                     .id(APPLICATION_ID)
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        assertThrows(UserDoesNotBelongToOrganization.class, () ->
                appService.getApp(ORGANISATION_GUID, APPLICATION_GUID, USER_ID)
        );
    }

    @Test
    void failGetAppWithUnknownOrgGuid() {
        val user = user(USER_ID);

        val organization = organization();
        organization.addUserOrganizationRole(user, USER);

        val app = App.builder()
                     .id(APPLICATION_ID)
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();
        app.addUserAppRole(user, AppRole.USER);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        assertThrows(AppDoesNotBelongToOrgException.class, () ->
                appService.getApp(randomAlphabetic(10), APPLICATION_GUID, USER_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void successGetAppOrganizationUser(final OrganizationRole organizationRole) {
        val user = user(USER_ID);

        val organization = organization();
        organization.addUserOrganizationRole(user, organizationRole);

        val app = App.builder()
                     .id(APPLICATION_ID)
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();
        app.addUserAppRole(user, AppRole.USER);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        val result = appService.getApp(ORGANISATION_GUID, APPLICATION_GUID, USER_ID);

        assertThat(result.getId()).isEqualTo(APPLICATION_ID);
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void failGetAppInsufficientPrivileges(final OrganizationRole organizationRole) {
        val user = user(USER_ID);

        val organization = organization();
        organization.addUserOrganizationRole(user, organizationRole);

        val app = App.builder()
                     .id(APPLICATION_ID)
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        assertThrows(InsufficientPrivilegesException.class, () ->
                appService.getApp(ORGANISATION_GUID, APPLICATION_GUID, USER_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void successGetApps(final OrganizationRole organizationRole) {
        val user = user(USER_ID);

        val organization = organization();
        organization.setGuid(ORGANISATION_GUID);
        organization.addUserOrganizationRole(user, organizationRole);

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
    void failGetAppsUserDoesNotBelongToOrganization() {
        val organization = organization();
        organization.setGuid(ORGANISATION_GUID);

        when(organizationServiceMock.getOrganization(ORGANISATION_GUID)).thenReturn(organization);

        assertThrows(UserDoesNotBelongToOrganization.class, () -> appService.getApps(ORGANISATION_GUID, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void successGetAppsOrganizationUser(final OrganizationRole organizationRole) {
        val user = user(USER_ID);

        val organization = organization();
        organization.setGuid(ORGANISATION_GUID);
        organization.addUserOrganizationRole(user, organizationRole);

        val app = App.builder()
                     .id(APPLICATION_ID)
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();
        app.addUserAppRole(user, AppRole.USER);

        when(appRepositoryMock.findAllByOrganizationIdAndUserAppRoles_Id_UserId(anyLong(), anyLong())).thenReturn(List.of(app));
        when(organizationServiceMock.getOrganization(ORGANISATION_GUID)).thenReturn(organization);

        val result = appService.getApps(ORGANISATION_GUID, USER_ID);

        assertThat(result).hasSize(1);
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void successCreateApp(final OrganizationRole organizationRole) {
        AppCreateDto appCreateDto = AppCreateDto.builder().name("appName").build();
        val user = user(USER_ID);

        val organization = organization();
        organization.addUserOrganizationRole(user, organizationRole);

        when(organizationServiceMock.getOrganization(anyString())).thenReturn(organization);
        when(userServiceMock.getUser(anyLong())).thenReturn(user);

        appService.createApp(appCreateDto, ORGANISATION_GUID, USER_ID);

        val varArgs = ArgumentCaptor.forClass(App.class);
        verify(appRepositoryMock).save(varArgs.capture());

        assertThat(varArgs.getValue().getName()).isEqualTo(appCreateDto.getName());
        assertThat(varArgs.getValue().getGuid()).isNotEmpty();
        assertThat(varArgs.getValue().getApiKey()).isNotEmpty();
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void failCreateOrganizationNameIsNotUnique(final OrganizationRole organizationRole) {
        val appCreateDto = AppCreateDto.builder().name("appName").build();
        val user = user(USER_ID);

        val organization = organization();
        organization.addUserOrganizationRole(user, organizationRole);

        when(organizationServiceMock.getOrganization(anyString())).thenReturn(organization);
        when(appRepositoryMock.existsByNameAndOrganizationId(anyString(), anyLong())).thenReturn(true);

        assertThrows(NameIsNotUniqueException.class, () -> appService.createApp(appCreateDto, ORGANISATION_GUID, USER_ID));
    }

    @Test
    void failCreateAppUserDoesNotBelongToOrganization() {
        val appCreateDto = AppCreateDto.builder().name("appName").build();
        val user = user(USER_ID);
        val organization = organization();

        when(organizationServiceMock.getOrganization(anyString())).thenReturn(organization);
        when(userServiceMock.getUser(anyLong())).thenReturn(user);

        assertThrows(UserDoesNotBelongToOrganization.class, () -> appService.createApp(appCreateDto, ORGANISATION_GUID, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void failCreateAppInsufficientPrivileges(final OrganizationRole organizationRole) {
        val appCreateDto = AppCreateDto.builder().name("appName").build();
        val user = user(USER_ID);
        val organization = organization();
        organization.addUserOrganizationRole(user, organizationRole);

        when(organizationServiceMock.getOrganization(anyString())).thenReturn(organization);
        when(userServiceMock.getUser(anyLong())).thenReturn(user);

        assertThrows(InsufficientPrivilegesException.class, () -> appService.createApp(appCreateDto, ORGANISATION_GUID, USER_ID));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void successUpdateApp(final OrganizationRole organizationRole) {
        val appUpdateDto = AppUpdateDto.builder().name("appName").build();
        val user = user(USER_ID);
        val organization = organization();
        organization.addUserOrganizationRole(user, organizationRole);

        val app = App.builder()
                     .name("name")
                     .organization(organization)
                     .build();
        app.addUserAppRole(user, OWNER);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        appService.updateApp(appUpdateDto, ORGANISATION_GUID, APPLICATION_GUID, USER_ID);

        val varArgs = ArgumentCaptor.forClass(App.class);
        verify(appRepositoryMock).save(varArgs.capture());

        assertThat(varArgs.getValue().getName()).isEqualTo(appUpdateDto.getName());
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void failUpdateAppSelfRoleChange(final OrganizationRole organizationRole) {
        val userRoleUpdateDto = UserRoleUpdateDto.builder()
                                                 .userId("userGuid")
                                                 .role(AppRole.USER.toString())
                                                 .build();
        val user = user(USER_ID);
        val organization = organization();
        organization.addUserOrganizationRole(user, organizationRole);

        val app = App.builder()
                     .name("name")
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();
        app.addUserAppRole(user, OWNER);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(userServiceMock.getUserByGuid(any())).thenReturn(user);

        assertThrows(SelfRoleChangeException.class, () ->
                appService.updateUserAppRole(userRoleUpdateDto, ORGANISATION_GUID, APPLICATION_GUID, USER_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void failUpdateAppNameIsNotUnique(final OrganizationRole organizationRole) {
        val appUpdateDto = AppUpdateDto.builder().name("new_name").build();
        val user = user(USER_ID);
        val organization = organization();
        organization.addUserOrganizationRole(user, organizationRole);

        val app = App.builder()
                     .name("name")
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();
        app.addUserAppRole(user, OWNER);

        when(appRepositoryMock.existsByNameAndOrganizationId(anyString(), anyLong())).thenReturn(true);
        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        assertThrows(NameIsNotUniqueException.class, () ->
                appService.updateApp(appUpdateDto, ORGANISATION_GUID, APPLICATION_GUID, USER_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void failUpdateAppInsufficientPrivileges(final OrganizationRole organizationRole) {
        val appUpdateDto = AppUpdateDto.builder().name("new_name").build();
        val user = user(USER_ID);
        val organization = organization();
        organization.addUserOrganizationRole(user, organizationRole);

        val app = App.builder()
                     .organization(organization)
                     .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        assertThrows(InsufficientPrivilegesException.class, () ->
                appService.updateApp(appUpdateDto, ORGANISATION_GUID, APPLICATION_GUID, USER_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void successRegenerateGuid(final OrganizationRole organizationRole) {
        val user = user(USER_ID);
        val organization = organization();
        organization.addUserOrganizationRole(user, organizationRole);

        val app = App.builder()
                     .name("name")
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        appService.regenerateApiKey(ORGANISATION_GUID, APPLICATION_GUID, USER_ID);

        verify(appRepositoryMock).save(any(App.class));
        assertThat(app.getGuid()).isNotEqualTo("guid");
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void failRegenerateGuidInsufficientPrivileges(final OrganizationRole organizationRole) {
        val user = user(USER_ID);

        val organization = organization();
        organization.addUserOrganizationRole(user, organizationRole);

        val app = App.builder()
                     .name("name")
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        assertThrows(InsufficientPrivilegesException.class, () ->
                appService.regenerateApiKey(ORGANISATION_GUID, APPLICATION_GUID, USER_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void successDeleteApp(final OrganizationRole organizationRole) {
        val user = user(USER_ID);
        val organization = organization();
        organization.addUserOrganizationRole(user, organizationRole);

        val app = App.builder()
                     .id(APPLICATION_ID)
                     .name("name")
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        appService.deleteApp(ORGANISATION_GUID, APPLICATION_GUID, USER_ID);

        verify(appRepositoryMock).deleteById(anyLong());
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void failDeleteAppInsufficientPrivileges(final OrganizationRole organizationRole) {
        val user = user(USER_ID);
        val organization = organization();
        organization.addUserOrganizationRole(user, organizationRole);

        val app = App.builder()
                     .name("name")
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        assertThrows(InsufficientPrivilegesException.class, () ->
                appService.deleteApp(ORGANISATION_GUID, APPLICATION_GUID, USER_ID)
        );
    }

    @ParameterizedTest
    @MethodSource({"readRoles", "writeRoles"})
    void successGetAppRoles(final OrganizationRole organizationRole) {
        val user = user(USER_ID);
        val organization = organization();
        organization.addUserOrganizationRole(user, organizationRole);

        val app = App.builder()
                     .id(APPLICATION_ID)
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();
        app.addUserAppRole(user, OWNER);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        val result = appService.getAppUsers("", ORGANISATION_GUID, APPLICATION_GUID, USER_ID);

        assertThat(result).hasSize(1);
    }

    @ParameterizedTest
    @MethodSource({"readRoles", "writeRoles"})
    void successAppUsersSearch(final OrganizationRole organizationRole) {
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
        organization.addUserOrganizationRole(user1, organizationRole);
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
    }

    @Test
    void failGetAppRolesUserDoesNotBelongToOrganization() {
        val organization = organization();
        val app = App.builder()
                     .id(APPLICATION_ID)
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        assertThrows(UserDoesNotBelongToOrganization.class, () -> appService.getAppUsers("", ORGANISATION_GUID, APPLICATION_GUID, USER_ID));
    }

    @ParameterizedTest
    @MethodSource({"readRoles", "writeRoles"})
    void failGetAppRolesAppDoesNotBelongToOrg(final OrganizationRole organizationRole) {
        val user = user(USER_ID);
        val org2Id = nextLong();
        val org2Guid = "org-guid-3";
        val organization2 = Organization.builder()
                                        .id(org2Id)
                                        .guid(org2Guid)
                                        .build();
        organization2.addUserOrganizationRole(user, organizationRole);

        val app = App.builder()
                     .id(APPLICATION_ID)
                     .guid(APPLICATION_GUID)
                     .organization(organization2)
                     .build();
        app.addUserAppRole(user, OWNER);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        assertThrows(AppDoesNotBelongToOrgException.class, () ->
                appService.getAppUsers("", ORGANISATION_GUID, APPLICATION_GUID, USER_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void successUserInvite(final OrganizationRole organizationRole) {
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
        organization.addUserOrganizationRole(admin, organizationRole);
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
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void failUserInviteInsufficientPrivileges(final OrganizationRole organizationRole) {
        val userInviteDto = UserInviteDto.builder()
                                         .userEmail("email")
                                         .role(AppRole.USER.toString())
                                         .build();
        val user = user(USER_ID);
        val organization = organization();
        organization.addUserOrganizationRole(user, organizationRole);

        val app = App.builder()
                     .id(APPLICATION_ID)
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        assertThrows(
                InsufficientPrivilegesException.class,
                () -> appService.inviteUser(userInviteDto, ORGANISATION_GUID, APPLICATION_GUID, USER_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void failUserInviteAppDoesNotBelongToOrg(final OrganizationRole organizationRole) {
        val userInviteDto = UserInviteDto.builder()
                                         .userEmail("email")
                                         .role(AppRole.USER.toString())
                                         .build();
        val admin = user(USER_ID);
        val organization = organization();
        organization.addUserOrganizationRole(admin, organizationRole);

        val app = App.builder()
                     .id(APPLICATION_ID)
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));

        assertThrows(
                AppDoesNotBelongToOrgException.class,
                () -> appService.inviteUser(userInviteDto, "org-guid-2", APPLICATION_GUID, USER_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void failUserInviteUserDoesNotBelongToOrg(final OrganizationRole organizationRole) {
        val userInviteDto = UserInviteDto.builder()
                                         .userEmail("email")
                                         .role(AppRole.USER.toString())
                                         .build();
        val userId = nextLong();
        val admin = user(USER_ID);
        val user = user(userId);
        val organization = organization();
        organization.addUserOrganizationRole(admin, organizationRole);

        val app = App.builder()
                     .id(APPLICATION_ID)
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(userServiceMock.getUser(anyString())).thenReturn(user);

        assertThrows(
                UserDoesNotBelongToOrganization.class,
                () -> appService.inviteUser(userInviteDto, ORGANISATION_GUID, APPLICATION_GUID, USER_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void failUserInviteUserAlreadyHasAccessToApp(final OrganizationRole organizationRole) {
        val userInviteDto = UserInviteDto.builder()
                                         .userEmail("email")
                                         .role(AppRole.USER.toString())
                                         .build();
        val userId = nextLong();
        val admin = user(USER_ID);
        val user = user(userId);

        val organization = organization();
        organization.addUserOrganizationRole(admin, organizationRole);
        organization.addUserOrganizationRole(user, USER);

        val app = App.builder()
                     .id(APPLICATION_ID)
                     .guid(APPLICATION_GUID)
                     .organization(organization)
                     .build();
        app.addUserAppRole(user, AppRole.USER);

        when(appRepositoryMock.findByGuid(APPLICATION_GUID)).thenReturn(Optional.of(app));
        when(userServiceMock.getUser(anyString())).thenReturn(user);

        assertThrows(
                UserAlreadyHasAccessToAppException.class,
                () -> appService.inviteUser(userInviteDto, ORGANISATION_GUID, APPLICATION_GUID, USER_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void deleteUserFromApp(final OrganizationRole organizationRole) {
        val userGuid = randomAlphabetic(36);
        val userId = nextLong();
        val admin = user(USER_ID);
        val user = user(userId);
        user.setGuid(userGuid);

        val organization = organization();
        organization.addUserOrganizationRole(admin, organizationRole);
        organization.addUserOrganizationRole(user, USER);

        val app = App.builder()
                     .id(APPLICATION_ID)
                     .guid(APPLICATION_GUID)
                     .organization(organization)
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
        verifyNoMoreInteractions(appRepositoryMock);
    }
}