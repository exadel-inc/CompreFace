package com.exadel.frs;

import com.exadel.frs.dto.ui.OrgCreateDto;
import com.exadel.frs.dto.ui.OrgUpdateDto;
import com.exadel.frs.dto.ui.UserInviteDto;
import com.exadel.frs.dto.ui.UserRemoveDto;
import com.exadel.frs.dto.ui.UserRoleUpdateDto;
import com.exadel.frs.entity.Organization;
import com.exadel.frs.entity.User;
import com.exadel.frs.enums.OrganizationRole;
import com.exadel.frs.exception.FieldRequiredException;
import com.exadel.frs.exception.InsufficientPrivilegesException;
import com.exadel.frs.exception.NameIsNotUniqueException;
import com.exadel.frs.exception.SelfRemoveException;
import com.exadel.frs.exception.SelfRoleChangeException;
import com.exadel.frs.repository.OrganizationRepository;
import com.exadel.frs.service.OrganizationService;
import com.exadel.frs.service.UserService;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class OrganizationServiceTest {

    private static final String ORGANISATION_GUID = "organisation-guid";

    private UserService userServiceMock;
    private OrganizationRepository organizationRepositoryMock;
    private OrganizationService organizationService;

    OrganizationServiceTest() {
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
    void successGetOrganization(OrganizationRole organizationRole) {
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
    void successGetOrganizations() {
        when(organizationRepositoryMock.findAllByUserOrganizationRoles_Id_UserId(anyLong()))
                .thenReturn(List.of(Organization.builder().build()));

        List<Organization> organizations = organizationService.getOrganizations(1L);

        assertThat(organizations.size(), is(1));
    }

    @Test
    void successCreateOrganization() {
        OrgCreateDto orgCreateDto = OrgCreateDto.builder().name("Organization").build();
        Long userId = 1L;

        User user = user(userId);

        when(userServiceMock.getUser(userId)).thenReturn(user);

        organizationService.createOrganization(orgCreateDto, userId);

        ArgumentCaptor<Organization> varArgs = ArgumentCaptor.forClass(Organization.class);
        verify(organizationRepositoryMock).save(varArgs.capture());

        assertThat(varArgs.getValue().getName(), is(orgCreateDto.getName()));
        assertThat(varArgs.getValue().getUserOrganizationRoles().size(), is(1));
        assertThat(varArgs.getValue().getUserOrganizationRole(userId).get().getRole(), is(OrganizationRole.OWNER));
    }

    @Test
    void failCreateOrganizationNameIsNotUnique() {
        OrgCreateDto orgCreateDto = OrgCreateDto.builder().name("Organization").build();

        when(organizationRepositoryMock.existsByName(anyString())).thenReturn(true);

        Assertions.assertThrows(NameIsNotUniqueException.class, () -> organizationService.createOrganization(orgCreateDto, null));
    }

    @Test
    void failCreateOrganizationEmptyRequiredField() {
        OrgCreateDto orgCreateDto = OrgCreateDto.builder().name("").build();
        Assertions.assertThrows(FieldRequiredException.class, () -> organizationService.createOrganization(orgCreateDto, null));
    }

    @Test
    void successUpdateOrganization() {
        OrgUpdateDto orgUpdateDto = OrgUpdateDto.builder().name("Organization 2").build();
        Long userId = 1L;
        Long organizationId = 1L;

        User admin = user(userId);

        Organization organization = Organization.builder()
                .id(organizationId)
                .name("Organization 1")
                .build();
        organization.addUserOrganizationRole(admin, OrganizationRole.OWNER);

        when(organizationRepositoryMock.findByGuid(ORGANISATION_GUID)).thenReturn(Optional.of(organization));

        organizationService.updateOrganization(orgUpdateDto, ORGANISATION_GUID, userId);

        assertThat(organization.getName(), is(orgUpdateDto.getName()));
    }

    @Test
    void failUpdateOrganizationNameIsNotUnique() {
        OrgUpdateDto orgUpdateDto = OrgUpdateDto.builder().name("Organization 2").build();
        Long userId = 1L;

        User user = user(userId);

        Organization organization = Organization.builder()
                .name("Organization 1")
                .guid(ORGANISATION_GUID)
                .build();
        organization.addUserOrganizationRole(user, OrganizationRole.OWNER);

        when(organizationRepositoryMock.findByGuid(ORGANISATION_GUID)).thenReturn(Optional.of(organization));
        when(organizationRepositoryMock.existsByName(anyString())).thenReturn(true);

        Assertions.assertThrows(NameIsNotUniqueException.class, () -> organizationService.updateOrganization(orgUpdateDto, ORGANISATION_GUID, userId));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void failUpdateOrganizationInsufficientPrivileges(OrganizationRole organizationRole) {
        OrgUpdateDto orgUpdateDto = OrgUpdateDto.builder().name("Organization 2").build();
        Long userId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = Organization.builder()
                .id(organizationId)
                .build();
        organization.addUserOrganizationRole(user, organizationRole);

        when(organizationRepositoryMock.findByGuid(ORGANISATION_GUID)).thenReturn(Optional.of(organization));

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> organizationService.updateOrganization(orgUpdateDto, ORGANISATION_GUID, userId));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void failUpdateOrganizationSelfRoleChange(OrganizationRole organizationRole) {
        UserRoleUpdateDto userRoleUpdateDto = UserRoleUpdateDto.builder()
                .userId("userGuid")
                .role(OrganizationRole.USER.toString())
                .build();
        Long userId = 1L;
        Long organizationId = 1L;

        User user = user(userId);

        Organization organization = Organization.builder()
                .id(organizationId)
                .guid(ORGANISATION_GUID)
                .build();
        organization.addUserOrganizationRole(user, organizationRole);

        Organization organizationUpdate = Organization.builder().build();
        organizationUpdate.addUserOrganizationRole(user, OrganizationRole.USER);

        when(organizationRepositoryMock.findByGuid(ORGANISATION_GUID)).thenReturn(Optional.of(organization));
        when(userServiceMock.getUserByGuid(any())).thenReturn(user);

        Assertions.assertThrows(SelfRoleChangeException.class, () -> organizationService.updateUserOrgRole(userRoleUpdateDto, ORGANISATION_GUID, userId));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void successAddToOrganization(OrganizationRole organizationRole) {
        UserInviteDto userInviteDto = UserInviteDto.builder()
                .userEmail("email")
                .role(OrganizationRole.USER.toString())
                .build();
        Long adminId = 1L;
        Long userId = 2L;

        User admin = user(adminId);
        User user = user(userId);

        Organization organization = Organization.builder().build();
        organization.addUserOrganizationRole(admin, organizationRole);

        when(organizationRepositoryMock.findByGuid(ORGANISATION_GUID)).thenReturn(Optional.of(organization));
        when(userServiceMock.getUser(anyString())).thenReturn(user);
        when(organizationRepositoryMock.save(organization)).thenReturn(organization);

        organizationService.inviteUser(userInviteDto, ORGANISATION_GUID, adminId);

        assertThat(organization.getUserOrganizationRoles().size(), is(2));
        assertThat(organization.getUserOrganizationRole(userId).get().getRole(), is(OrganizationRole.USER));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void failAddToOrganizationInsufficientPrivileges(OrganizationRole organizationRole) {
        UserInviteDto userInviteDto = UserInviteDto.builder()
                .userEmail("email")
                .role(OrganizationRole.USER.toString())
                .build();
        Long userId = 1L;

        User user = user(userId);

        Organization organization = Organization.builder().build();
        organization.addUserOrganizationRole(user, organizationRole);

        when(organizationRepositoryMock.findByGuid(ORGANISATION_GUID)).thenReturn(Optional.of(organization));

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> organizationService.inviteUser(userInviteDto, ORGANISATION_GUID, userId));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void successRemoveFromOrganization(OrganizationRole organizationRole) {
        UserRemoveDto userRemoveDto = UserRemoveDto.builder()
                .userId("userGuid")
                .build();
        Long adminId = 1L;
        Long userId = 2L;

        User admin = user(adminId);
        User user = user(userId);

        Organization organization = Organization.builder().build();
        organization.addUserOrganizationRole(admin, organizationRole);
        organization.addUserOrganizationRole(user, OrganizationRole.USER);

        Organization organizationUpdate = Organization.builder().build();
        organizationUpdate.addUserOrganizationRole(user, OrganizationRole.USER);

        when(organizationRepositoryMock.findByGuid(ORGANISATION_GUID)).thenReturn(Optional.of(organization));
        when(userServiceMock.getUserByGuid(any())).thenReturn(user);

        organizationService.removeUserFromOrganization(userRemoveDto, ORGANISATION_GUID, adminId);

        assertThat(organization.getUserOrganizationRoles().size(), is(1));
    }

    @ParameterizedTest
    @MethodSource("readRoles")
    void failRemoveFromOrganizationInsufficientPrivileges(OrganizationRole organizationRole) {
        UserRemoveDto userRemoveDto = UserRemoveDto.builder()
                .userId("userGuid")
                .build();
        Long userId = 1L;

        User user = user(userId);

        Organization organization = Organization.builder().build();
        organization.addUserOrganizationRole(user, organizationRole);

        when(organizationRepositoryMock.findByGuid(ORGANISATION_GUID)).thenReturn(Optional.of(organization));

        Assertions.assertThrows(InsufficientPrivilegesException.class, () -> organizationService.removeUserFromOrganization(userRemoveDto, ORGANISATION_GUID, userId));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void failRemoveFromOrganizationSelfRemove(OrganizationRole organizationRole) {
        UserRemoveDto userRemoveDto = UserRemoveDto.builder()
                .userId("userGuid")
                .build();
        Long adminId = 1L;

        User admin = user(adminId);

        Organization organization = Organization.builder().build();
        organization.addUserOrganizationRole(admin, organizationRole);

        when(organizationRepositoryMock.findByGuid(ORGANISATION_GUID)).thenReturn(Optional.of(organization));
        when(userServiceMock.getUserByGuid(any())).thenReturn(admin);

        Assertions.assertThrows(SelfRemoveException.class, () -> organizationService.removeUserFromOrganization(userRemoveDto, ORGANISATION_GUID, adminId));
    }

    @ParameterizedTest
    @MethodSource("writeRoles")
    void successDeleteOrganization(OrganizationRole organizationRole) {
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
    void failDeleteOrganizationInsufficientPrivileges(OrganizationRole organizationRole) {
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

    @ExtendWith(SpringExtension.class)
    @DataJpaTest
    @Nested
    @MockBeans({@MockBean(SpringLiquibase.class), @MockBean(PasswordEncoder.class)})
    @Import({OrganizationService.class, UserService.class})
    public class RemoveOrganizationTest {
        @Autowired
        private OrganizationRepository repository;
        @Autowired
        private OrganizationService service;
        private final String ORG_GUID = "d098a11e-c4e4-4f56-86b2-85ab3bc83044";
        private final Long USER_ID = 25L;


        @Test
        @Sql("init_remove_org_test.sql")
        public void removesExpectedOrganization() {
            assertEquals(2, repository.findAll().size());
            assertNotNull(repository.findByGuid(ORG_GUID));

            service.deleteOrganization(ORG_GUID, USER_ID);

            assertEquals(1, repository.findAll().size());
            assertTrue(repository.findByGuid(ORG_GUID).isEmpty());
        }


        @Test
        @Sql("init_remove_org_test.sql")
        public void test() {
            assertEquals(2, repository.findAll().size());
        }
    }
}