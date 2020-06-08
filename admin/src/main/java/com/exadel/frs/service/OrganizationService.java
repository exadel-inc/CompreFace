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

import static com.exadel.frs.enums.OrganizationRole.OWNER;
import static java.util.stream.Collectors.toList;
import com.exadel.frs.dto.ui.UserRoleUpdateDto;
import com.exadel.frs.entity.Organization;
import com.exadel.frs.entity.UserOrganizationRole;
import com.exadel.frs.enums.OrganizationRole;
import com.exadel.frs.exception.InsufficientPrivilegesException;
import com.exadel.frs.exception.NameIsNotUniqueException;
import com.exadel.frs.exception.OrganizationNotFoundException;
import com.exadel.frs.exception.SelfRoleChangeException;
import com.exadel.frs.repository.OrganizationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserService userService;

    public Organization getOrganization(final String organizationGuid) {
        return organizationRepository
                .findByGuid(organizationGuid)
                .orElseThrow(() -> new OrganizationNotFoundException(organizationGuid));
    }

    public void verifyUserHasReadPrivileges(final Long userId, final Organization organization) {
        organization.getUserOrganizationRoleOrThrow(userId);
    }

    public void verifyUserHasWritePrivileges(final Long userId, final Organization organization) {
        if (OWNER != organization.getUserOrganizationRoleOrThrow(userId).getRole()) {
            throw new InsufficientPrivilegesException();
        }
    }

    private void verifyNameIsUnique(final String name) {
        if (organizationRepository.existsByName(name)) {
            throw new NameIsNotUniqueException(name);
        }
    }

    public Organization getOrganization(final String guid, final Long userId) {
        val organization = getOrganization(guid);
        verifyUserHasReadPrivileges(userId, organization);

        return organization;
    }

    public List<Organization> getOrganizations(final Long userId) {
        return organizationRepository.findAllByUserOrganizationRoles_Id_UserId(userId);
    }

    public List<Organization> getOwnedOrganizations(final Long userId) {
        return getOrganizations(userId).stream()
                                       .filter(org -> org.getUserOrganizationRoleOrThrow(userId).getRole().equals(OWNER))
                                       .collect(toList());
    }

    public OrganizationRole[] getOrgRolesToAssign(final String guid, final Long userId) {
        val organization = getOrganization(guid);
        val role = organization.getUserOrganizationRoleOrThrow(userId);
        if (role.getRole() == OWNER) {
            return OrganizationRole.values();
        }

        return new OrganizationRole[0];
    }

    public List<UserOrganizationRole> getOrgUsers(final String guid, final Long userId) {
        val organization = getOrganization(guid);
        verifyUserHasReadPrivileges(userId, organization);

        return organization.getUserOrganizationRoles();
    }

    public UserOrganizationRole updateUserOrgRole(final UserRoleUpdateDto userRoleUpdateDto, final String guid, final Long adminId) {
        val organization = getOrganization(guid);
        verifyUserHasWritePrivileges(adminId, organization);

        val user = userService.getUserByGuid(userRoleUpdateDto.getUserId());
        if (user.getId().equals(adminId)) {
            throw new SelfRoleChangeException();
        }

        val userOrganizationRole = organization.getUserOrganizationRoleOrThrow(user.getId());
        val newOrgRole = OrganizationRole.valueOf(userRoleUpdateDto.getRole());
        if (newOrgRole == OWNER) {
            organization.getUserOrganizationRoleOrThrow(adminId).setRole(OrganizationRole.ADMINISTRATOR);
        }

        userOrganizationRole.setRole(newOrgRole);

        organizationRepository.save(organization);

        return userOrganizationRole;
    }
}