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

import static com.exadel.frs.enums.OrganizationRole.ADMINISTRATOR;
import static com.exadel.frs.enums.OrganizationRole.OWNER;
import static com.exadel.frs.enums.OrganizationRole.USER;
import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import com.exadel.frs.dto.ui.UserDeleteDto;
import com.exadel.frs.dto.ui.UserRoleUpdateDto;
import com.exadel.frs.entity.Organization;
import com.exadel.frs.entity.User;
import com.exadel.frs.entity.UserOrganizationRole;
import com.exadel.frs.entity.UserOrganizationRoleId;
import com.exadel.frs.enums.OrganizationRole;
import com.exadel.frs.exception.OrganizationNotFoundException;
import com.exadel.frs.exception.SelfRoleChangeException;
import com.exadel.frs.repository.OrganizationRepository;
import com.exadel.frs.service.OrganizationService;
import com.exadel.frs.service.UserService;
import com.exadel.frs.system.security.AuthorizationManager;
import java.util.List;
import java.util.Optional;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class OrganizationServiceTest {

    private static final String ORGANIZATION_GUID = "org-guid";
    private static final Long USER_ID = 1L;
    private static final Long ORGANIZATION_ID = 2L;

    @Mock
    private UserService userServiceMock;

    @Mock
    private OrganizationRepository organizationRepositoryMock;

    @Mock
    private AuthorizationManager authManagerMock;

    @InjectMocks
    private OrganizationService organizationService;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    private User user(Long id) {
        return User.builder()
                   .id(id)
                   .build();
    }

    @Test
    void successGetOrganization() {
        val organization = Organization.builder()
                                       .id(ORGANIZATION_ID)
                                       .build();

        when(organizationRepositoryMock.findByGuid(ORGANIZATION_GUID)).thenReturn(Optional.of(organization));

        val result = organizationService.getOrganization(ORGANIZATION_GUID, USER_ID);

        assertThat(result.getId()).isEqualTo(ORGANIZATION_ID);

        verify(organizationRepositoryMock).findByGuid(ORGANIZATION_GUID);
        verify(authManagerMock).verifyReadPrivilegesToOrg(USER_ID, organization);
        verifyNoMoreInteractions(organizationRepositoryMock, authManagerMock);
    }

    @Test
    void successGetOrganizations() {
        when(organizationRepositoryMock.findAllByUserOrganizationRoles_Id_UserId(anyLong()))
                .thenReturn(List.of(Organization.builder().build()));

        val organizations = organizationService.getOrganizations(1L);

        assertThat(organizations).hasSize(1);
    }

    @Test
    void failUpdateOrganizationSelfRoleChange() {
        val userRoleUpdateDto = UserRoleUpdateDto.builder()
                                                 .userId("userGuid")
                                                 .role(USER.toString())
                                                 .build();

        val user = user(USER_ID);
        val organization = Organization.builder()
                                       .id(ORGANIZATION_ID)
                                       .guid(ORGANIZATION_GUID)
                                       .build();

        val organizationUpdate = Organization.builder().build();
        organizationUpdate.addUserOrganizationRole(user, USER);

        when(organizationRepositoryMock.findByGuid(ORGANIZATION_GUID)).thenReturn(Optional.of(organization));
        when(userServiceMock.getUserByGuid(any())).thenReturn(user);

        assertThrows(
                SelfRoleChangeException.class,
                () -> organizationService.updateUserOrgRole(userRoleUpdateDto, ORGANIZATION_GUID, USER_ID)
        );

        verify(organizationRepositoryMock).findByGuid(ORGANIZATION_GUID);
        verify(authManagerMock).verifyWritePrivilegesToOrg(USER_ID, organization);
        verify(userServiceMock).getUserByGuid(any());
        verifyNoMoreInteractions(authManagerMock, userServiceMock, organizationRepositoryMock);
    }

    @Test
    void getDefaultOrg() {
        val defaultOrg = Organization.builder().build();
        when(organizationRepositoryMock.findFirstByIsDefaultTrue()).thenReturn(Optional.of(defaultOrg));

        val actual = organizationService.getDefaultOrg();

        assertThat(actual).isNotNull()
                          .isEqualTo(defaultOrg);

        verify(organizationRepositoryMock).findFirstByIsDefaultTrue();
        verifyNoMoreInteractions(organizationRepositoryMock);
        verifyNoInteractions(userServiceMock);
    }

    @Test
    void failGetDefaultOrg() {
        assertThrows(
                OrganizationNotFoundException.class,
                () -> organizationService.getDefaultOrg()
        );

        verify(organizationRepositoryMock).findFirstByIsDefaultTrue();
        verifyNoMoreInteractions(organizationRepositoryMock);
        verifyNoInteractions(userServiceMock);
    }

    @Test
    void addFirstUserToDefaultOrg() {
        val email = "email";
        val user = User.builder()
                       .id(1L)
                       .build();

        val defaultOrg = Organization.builder().build();

        when(userServiceMock.getUser(email)).thenReturn(user);
        when(organizationRepositoryMock.findFirstByIsDefaultTrue()).thenReturn(Optional.of(defaultOrg));

        val actual = organizationService.addUserToDefaultOrg(email);

        assertThat(actual).isNotNull();
        assertThat(actual.getUser()).isEqualTo(user);
        assertThat(actual.getRole()).isEqualTo(OWNER);
        assertThat(actual.getOrganization()).isEqualTo(defaultOrg);

        verify(userServiceMock).getUser(email);
        verify(organizationRepositoryMock).findFirstByIsDefaultTrue();
        verify(organizationRepositoryMock).save(defaultOrg);
        verifyNoMoreInteractions(userServiceMock, organizationRepositoryMock);
    }

    @Test
    void addSecondUserToDefaultOrg() {
        val email = "email";
        val fistUserRole = UserOrganizationRoleId.builder()
                                                 .userId(0L)
                                                 .build();

        val firstUser = UserOrganizationRole.builder()
                                            .id(fistUserRole)
                                            .build();

        val defaultOrg = Organization.builder()
                                     .userOrganizationRoles(newArrayList(firstUser))
                                     .build();

        val secondUser = User.builder()
                             .id(1L)
                             .build();

        when(userServiceMock.getUser(email)).thenReturn(secondUser);
        when(organizationRepositoryMock.findFirstByIsDefaultTrue()).thenReturn(Optional.of(defaultOrg));

        val actual = organizationService.addUserToDefaultOrg(email);

        assertThat(actual).isNotNull();
        assertThat(actual.getUser()).isEqualTo(secondUser);
        assertThat(actual.getRole()).isEqualTo(USER);
        assertThat(actual.getOrganization()).isEqualTo(defaultOrg);

        verify(userServiceMock).getUser(email);
        verify(organizationRepositoryMock).findFirstByIsDefaultTrue();
        verify(organizationRepositoryMock).save(defaultOrg);
        verifyNoMoreInteractions(userServiceMock, organizationRepositoryMock);
    }

    @Test
    void successRemoveFromOrganization() {
        val userToDelete = user(1L);
        val deleter = user(2L);

        val organization = Organization.builder().build();
        organization.addUserOrganizationRole(deleter, ADMINISTRATOR);
        organization.addUserOrganizationRole(userToDelete, USER);

        val userRemoveDto = UserDeleteDto.builder()
                                         .userToDelete(userToDelete)
                                         .deleter(deleter)
                                         .defaultOrg(organization)
                                         .build();

        organizationService.removeUserFromOrganization(userRemoveDto);

        assertThat(organization.getUserOrganizationRoles()).hasSize(1);

        verify(organizationRepositoryMock).save(organization);
        verifyNoMoreInteractions(organizationRepositoryMock);
    }

    @Test
    void successGetOrgUsers() {
        val fistUserRole = UserOrganizationRoleId.builder()
                .userId(0L)
                .build();

        val user = UserOrganizationRole.builder()
                .id(fistUserRole)
                .build();

        val organization = Organization.builder()
                .userOrganizationRoles(newArrayList(user))
                .id(ORGANIZATION_ID)
                .build();

        when(organizationRepositoryMock.findByGuid(ORGANIZATION_GUID)).thenReturn(Optional.of(organization));

        val actual = organizationService.getOrgUsers(ORGANIZATION_GUID, USER_ID);

        assertThat(actual).hasSize(1);

        verify(organizationRepositoryMock).findByGuid(ORGANIZATION_GUID);
        verify(authManagerMock).verifyReadPrivilegesToOrg(USER_ID, organization);
        verifyNoMoreInteractions(organizationRepositoryMock, authManagerMock);
    }

    @Nested
    public class GetOrgRolesToAssignTest {

        private final Organization organization;
        private final long orgOwnerId = 1L;
        private final long orgAdminId = 2L;
        private final long orgUserRole = 3L;

        public GetOrgRolesToAssignTest() {
            val ownerRole = makeOrgRole(OWNER, orgOwnerId);
            val adminRole = makeOrgRole(ADMINISTRATOR, orgAdminId);
            val userRole = makeOrgRole(USER, orgUserRole);

            val userOrgRoles = List.of(ownerRole, adminRole, userRole);
            this.organization = Organization.builder()
                                            .userOrganizationRoles(userOrgRoles)
                                            .build();
        }

        @BeforeEach
        void init() {
            when(organizationRepositoryMock.findByGuid(ORGANIZATION_GUID))
                    .thenReturn(Optional.of(organization));
        }

        @Test
        void orgOwnerCanAssignAnyRole() {
            val actual = organizationService.getOrgRolesToAssign(ORGANIZATION_GUID, orgOwnerId);

            assertThat(actual).containsExactly(OrganizationRole.values());
        }

        @Test
        void AdminCanAssignLimitedRoles() {
            val actual = organizationService.getOrgRolesToAssign(ORGANIZATION_GUID, orgAdminId);

            assertThat(actual).contains(ADMINISTRATOR)
                              .contains(USER)
                              .doesNotContain(OWNER);
        }

        @Test
        void userCanAssignNoRole() {
            val actual = organizationService.getOrgRolesToAssign(ORGANIZATION_GUID, orgUserRole);

            assertThat(actual).doesNotHaveAnyElementsOfTypes(OrganizationRole.class);
        }

        private UserOrganizationRole makeOrgRole(final OrganizationRole owner, final long orgOwnerId) {
            return UserOrganizationRole.builder()
                                       .id(UserOrganizationRoleId.builder()
                                                                 .userId(orgOwnerId)
                                                                 .build())
                                       .role(owner)
                                       .build();
        }
    }
}