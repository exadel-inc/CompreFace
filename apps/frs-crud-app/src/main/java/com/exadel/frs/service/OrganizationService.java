package com.exadel.frs.service;

import static com.exadel.frs.enums.OrganizationRole.OWNER;
import static java.util.stream.Collectors.toList;
import com.exadel.frs.dto.ui.OrgCreateDto;
import com.exadel.frs.dto.ui.OrgUpdateDto;
import com.exadel.frs.dto.ui.UserInviteDto;
import com.exadel.frs.dto.ui.UserRemoveDto;
import com.exadel.frs.dto.ui.UserRoleUpdateDto;
import com.exadel.frs.entity.Organization;
import com.exadel.frs.entity.UserOrganizationRole;
import com.exadel.frs.enums.OrganizationRole;
import com.exadel.frs.exception.InsufficientPrivilegesException;
import com.exadel.frs.exception.NameIsNotUniqueException;
import com.exadel.frs.exception.OrganizationNotFoundException;
import com.exadel.frs.exception.SelfRemoveException;
import com.exadel.frs.exception.SelfRoleChangeException;
import com.exadel.frs.exception.UserAlreadyInOrganizationException;
import com.exadel.frs.repository.OrganizationRepository;
import java.util.List;
import java.util.UUID;
import javax.transaction.Transactional;
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

    public Organization createOrganization(final OrgCreateDto orgCreateDto, final Long userId) {
        verifyNameIsUnique(orgCreateDto.getName());
        val organization = Organization.builder()
                                       .name(orgCreateDto.getName())
                                       .guid(UUID.randomUUID().toString())
                                       .build();

        organization.addUserOrganizationRole(userService.getUser(userId), OWNER);

        return organizationRepository.save(organization);
    }

    public Organization updateOrganization(final OrgUpdateDto orgUpdateDto, final String guid, final Long userId) {
        val organizationFromRepo = getOrganization(guid);
        verifyUserHasWritePrivileges(userId, organizationFromRepo);
        val isNewName = !organizationFromRepo.getName().equals(orgUpdateDto.getName());

        if (isNewName) {
            verifyNameIsUnique(orgUpdateDto.getName());
            organizationFromRepo.setName(orgUpdateDto.getName());
        }

        return organizationRepository.save(organizationFromRepo);
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

    public UserOrganizationRole inviteUser(final UserInviteDto userInviteDto, final String guid, final Long adminId) {
        val organization = getOrganization(guid);
        verifyUserHasWritePrivileges(adminId, organization);

        val user = userService.getEnabledUserByEmail(userInviteDto.getUserEmail());
        val userOrganizationRole = organization.getUserOrganizationRole(user.getId());
        if (userOrganizationRole.isPresent()) {
            throw new UserAlreadyInOrganizationException(userInviteDto.getUserEmail(), guid);
        }

        val newOrgRole = OrganizationRole.valueOf(userInviteDto.getRole());
        if (newOrgRole == OWNER) {
            organization.getUserOrganizationRoleOrThrow(adminId).setRole(OrganizationRole.ADMINISTRATOR);
        }

        organization.addUserOrganizationRole(user, newOrgRole);
        val savedOrg = organizationRepository.save(organization);

        return savedOrg.getUserOrganizationRole(user.getId()).orElseThrow();
    }

    public void removeUserFromOrganization(final UserRemoveDto userRemoveDto, final String guid, final Long adminId) {
        val organization = getOrganization(guid);
        verifyUserHasWritePrivileges(adminId, organization);

        val user = userService.getUserByGuid(userRemoveDto.getUserId());
        if (user.getId().equals(adminId)) {
            throw new SelfRemoveException();
        }

        organization.getUserOrganizationRoles().removeIf(userOrganizationRole ->
                userOrganizationRole.getId().getUserId().equals(user.getId()));

        organizationRepository.save(organization);
    }

    @Transactional
    public void deleteOrganization(final String guid, final Long userId) {
        val organization = getOrganization(guid);
        verifyUserHasWritePrivileges(userId, organization);

        organizationRepository.deleteById(organization.getId());
    }
}