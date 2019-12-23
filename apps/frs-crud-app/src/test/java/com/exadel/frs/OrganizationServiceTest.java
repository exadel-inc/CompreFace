package com.exadel.frs;


import com.exadel.frs.entity.Organization;
import com.exadel.frs.entity.User;
import com.exadel.frs.enums.OrganizationRole;
import com.exadel.frs.exception.*;
import com.exadel.frs.repository.OrganizationRepository;
import com.exadel.frs.service.OrganizationService;
import com.exadel.frs.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class OrganizationServiceTest {

    private static final String ORGANISATION_GUID = "organisation-guid";

    private UserService userServiceMock;
    private OrganizationRepository organizationRepositoryMock;
    private OrganizationService organizationService;

    public OrganizationServiceTest() {
        userServiceMock = mock(UserService.class);
        organizationRepositoryMock = mock(OrganizationRepository.class);
        organizationService = new OrganizationService(organizationRepositoryMock, userServiceMock);
    }

    private User user(Long id) {
        return User.builder()
                .id(id)
                .build();
    }

    private static Stream<Arguments> readRoles() {
        return Stream.of(Arguments.of(OrganizationRole.ADMINISTRATOR),
                Arguments.of(OrganizationRole.USER));
    }

    private static Stream<Arguments> writeRoles() {
        return Stream.of(Arguments.of(OrganizationRole.OWNER));
    }

    @ParameterizedTest
    @MethodSource({"readRoles", "writeRoles"})
    public void successGetOrganization(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = Organization.builder()
                .id(organizationId)
                .build();
        organization.addUserOrganizationRole(user, organizationRole);

        when(organizationRepositoryMock.findByGuid(ORGANISATION_GUID)).thenReturn(Optional.of(organization));

        Organization result = organizationService.getOrganization(ORGANISATION_GUID, userId);

        assertThat(result.getId(), is(organizationId));
    }

    @Test
    public void successGetOrganizations() {
        when(organizationRepositoryMock.findAllByUserOrganizationRoles_Id_UserId(anyLong()))
                .thenReturn(List.of(Organization.builder().build()));

        List<Organization> organizations = organizationService.getOrganizations(1L);

        assertThat(organizations.size(), is(1));
    }

    @Test
    public void successCreateOrganization() {
        Long userId = 1L;

        Organization organization = Organization.builder()
                .name("Organization")
                .build();

        User user = user(userId);

        when(userServiceMock.getUser(anyLong())).thenReturn(user);

        organizationService.createOrganization(organization, userId);

        assertThat(organization.getUserOrganizationRoles().size(), is(1));
        assertThat(organization.getUserOrganizationRole(userId).get().getRole(), is(OrganizationRole.OWNER));
    }

    @Test
    public void failCreateOrganizationNameIsNotUnique() {
        Organization organization = Organization.builder()
                .name("Organization")
                .build();

        when(organizationRepositoryMock.existsByName(anyString())).thenReturn(true);

        Assertions.assertThrows(NameIsNotUniqueException.class, () -> organizationService.createOrganization(organization, null));
    }

    @Test
    public void failCreateOrganizationEmptyRequiredField() {
        Organization organization = Organization.builder()
                .name("")
                .build();

        Assertions.assertThrows(EmptyRequiredFieldException.class, () -> organizationService.createOrganization(organization, null));
    }

    @Test
    public void successUpdateOrganization() {
        Long userId1 = 1L;
        Long userId2 = 2L;
        Long organizationId = 1L;

        User user1 = user(userId1);
        User user2 = user(userId2);

        Organization organization = Organization.builder()
                .name("Organization 1")
                .build();
        organization.addUserOrganizationRole(user1, OrganizationRole.OWNER);
        organization.addUserOrganizationRole(user2, OrganizationRole.USER);

        Organization organizationUpdate = Organization.builder()
                .name("Organization 2")
                .build();
        organizationUpdate.addUserOrganizationRole(user2, OrganizationRole.OWNER);

        when(organizationRepositoryMock.findByGuid(ORGANISATION_GUID)).thenReturn(Optional.of(organization));

        organizationService.updateOrganization(ORGANISATION_GUID, organizationUpdate, userId1);

        assertThat(organization.getName(), is(organizationUpdate.getName()));
        assertThat(organization.getUserOrganizationRoles().size(), is(2));
        assertThat(organization.getUserOrganizationRole(userId1).get().getRole(), is(OrganizationRole.ADMINISTRATOR));
        assertThat(organization.getUserOrganizationRole(userId2).get().getRole(), is(OrganizationRole.OWNER));
    }

    @Test
    public void failUpdateOrganizationMultipleOwners() {
        Long userId1 = 1L;
        Long userId2 = 1L;
        Long userId3 = 1L;
        Long organizationId = 1L;

        User user1 = user(userId1);
        User user2 = user(userId2);
        User user3 = user(userId3);

        Organization organization = Organization.builder()
                .name("Organization 1")
                .guid(ORGANISATION_GUID)
                .build();
        organization.addUserOrganizationRole(user1, OrganizationRole.OWNER);

        Organization organizationUpdate = Organization.builder().build();
        organizationUpdate.addUserOrganizationRole(user2, OrganizationRole.OWNER);
        organizationUpdate.addUserOrganizationRole(user3, OrganizationRole.OWNER);

        when(organizationRepositoryMock.findByGuid(anyString())).thenReturn(Optional.of(organization));

        Assertions.assertThrows(MultipleOwnersException.class, () -> organizationService.updateOrganization(ORGANISATION_GUID, organizationUpdate, userId1));
    }

    @Test
    public void failUpdateOrganizationNameIsNotUnique() {
        Long userId = 1L;

        User user = user(userId);

        Organization organization = Organization.builder()
                .name("Organization 1")
                .guid(ORGANISATION_GUID)
                .build();
        organization.addUserOrganizationRole(user, OrganizationRole.OWNER);

        Organization organizationUpdate = Organization.builder()
                .name("Organization 2")
                .build();

        when(organizationRepositoryMock.findByGuid(anyString())).thenReturn(Optional.of(organization));
        when(organizationRepositoryMock.existsByName(anyString())).thenReturn(true);

        Assertions.assertThrows(NameIsNotUniqueException.class, () -> organizationService.updateOrganization(ORGANISATION_GUID, organizationUpdate, userId));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    public void failUpdateOrganizationInsufficientPrivileges(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = Organization.builder()
                .id(organizationId)
                .build();
        organization.addUserOrganizationRole(user, organizationRole);

        when(organizationRepositoryMock.findByGuid(ORGANISATION_GUID)).thenReturn(Optional.of(organization));

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> organizationService.updateOrganization(ORGANISATION_GUID, Organization.builder().build(), userId));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void failUpdateOrganizationSelfRoleChange(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = Organization.builder()
                .build();
        organization.addUserOrganizationRole(user, organizationRole);

        Organization organizationUpdate = Organization.builder()
                .build();
        organizationUpdate.addUserOrganizationRole(user, OrganizationRole.USER);

        when(organizationRepositoryMock.findByGuid(ORGANISATION_GUID)).thenReturn(Optional.of(organization));

        Assertions.assertThrows(SelfRoleChangeException.class, () -> organizationService.updateOrganization(ORGANISATION_GUID, organizationUpdate, userId));
    }

    // todo implement user invitation to organization by email. then delete this test method
    @ParameterizedTest
    @MethodSource("writeRoles")
    public void successAddToOrganization(OrganizationRole organizationRole) {
        Long userId1 = 1L;
        Long userId2 = 2L;
        Long organizationId = 1L;

        User user1 = user(userId1);
        User user2 = user(userId2);

        Organization organization = Organization.builder()
                .build();
        organization.addUserOrganizationRole(user1, organizationRole);

        Organization organizationUpdate = Organization.builder()
                .build();
        organizationUpdate.addUserOrganizationRole(user1, organizationRole);
        organizationUpdate.addUserOrganizationRole(user2, OrganizationRole.USER);

        when(organizationRepositoryMock.findByGuid(ORGANISATION_GUID)).thenReturn(Optional.of(organization));
        when(userServiceMock.getUser(anyLong())).thenReturn(user2);

        organizationService.addUserToOrganization(ORGANISATION_GUID, organizationUpdate, userId1);

        assertThat(organization.getUserOrganizationRoles().size(), is(2));
        assertThat(organization.getUserOrganizationRole(userId2).get().getRole(), is(OrganizationRole.USER));
    }

    // todo implement user invitation to organization by email. then delete this test method
    @ParameterizedTest
    @MethodSource("readRoles")
    public void failAddToOrganizationInsufficientPrivileges(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = Organization.builder()
                .build();
        organization.addUserOrganizationRole(user, organizationRole);

        when(organizationRepositoryMock.findByGuid(ORGANISATION_GUID)).thenReturn(Optional.of(organization));

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> organizationService.addUserToOrganization(ORGANISATION_GUID, null, userId));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void successRemoveFromOrganization(OrganizationRole organizationRole) {
        Long userId1 = 1L;
        Long userId2 = 2L;
        Long organizationId = 1L;

        User user1 = user(userId1);
        User user2 = user(userId2);

        Organization organization = Organization.builder()
                .build();
        organization.addUserOrganizationRole(user1, organizationRole);
        organization.addUserOrganizationRole(user2, OrganizationRole.USER);

        Organization organizationUpdate = Organization.builder()
                .build();
        organizationUpdate.addUserOrganizationRole(user2, OrganizationRole.USER);

        when(organizationRepositoryMock.findByGuid(ORGANISATION_GUID)).thenReturn(Optional.of(organization));

        organizationService.removeUserFromOrganization(ORGANISATION_GUID, organizationUpdate, userId1);

        assertThat(organization.getUserOrganizationRoles().size(), is(1));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    public void failRemoveFromOrganizationInsufficientPrivileges(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = Organization.builder()
                .build();
        organization.addUserOrganizationRole(user, organizationRole);

        when(organizationRepositoryMock.findByGuid(ORGANISATION_GUID)).thenReturn(Optional.of(organization));

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> organizationService.removeUserFromOrganization(ORGANISATION_GUID, null, userId));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void failRemoveFromOrganizationRemoveYourself(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = Organization.builder()
                .build();
        organization.addUserOrganizationRole(user, organizationRole);

        when(organizationRepositoryMock.findByGuid(ORGANISATION_GUID)).thenReturn(Optional.of(organization));

        Assertions.assertThrows(SelfRemoveException.class, () -> organizationService.removeUserFromOrganization(ORGANISATION_GUID, organization, userId));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    public void successDeleteOrganization(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = Organization.builder()
                .id(organizationId)
                .build();
        organization.addUserOrganizationRole(user, organizationRole);

        when(organizationRepositoryMock.findByGuid(ORGANISATION_GUID)).thenReturn(Optional.of(organization));

        organizationService.deleteOrganization(ORGANISATION_GUID, userId);

        verify(organizationRepositoryMock).deleteById(anyLong());
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    public void failDeleteOrganizationInsufficientPrivileges(OrganizationRole organizationRole) {
        Long userId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = Organization.builder()
                .id(organizationId)
                .build();
        organization.addUserOrganizationRole(user, organizationRole);

        when(organizationRepositoryMock.findByGuid(ORGANISATION_GUID)).thenReturn(Optional.of(organization));

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> organizationService.deleteOrganization(ORGANISATION_GUID, userId));
    }

}
