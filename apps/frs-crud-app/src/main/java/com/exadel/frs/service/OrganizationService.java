package com.exadel.frs.service;

import com.exadel.frs.dto.ui.OrgCreateDto;
import com.exadel.frs.dto.ui.OrgUpdateDto;
import com.exadel.frs.dto.ui.UserInviteDto;
import com.exadel.frs.dto.ui.UserRemoveDto;
import com.exadel.frs.dto.ui.UserRoleUpdateDto;
import com.exadel.frs.entity.Organization;
import com.exadel.frs.entity.User;
import com.exadel.frs.entity.UserOrganizationRole;
import com.exadel.frs.enums.OrganizationRole;
import com.exadel.frs.exception.FieldRequiredException;
import com.exadel.frs.exception.InsufficientPrivilegesException;
import com.exadel.frs.exception.NameIsNotUniqueException;
import com.exadel.frs.exception.OrganizationNotFoundException;
import com.exadel.frs.exception.SelfRemoveException;
import com.exadel.frs.exception.SelfRoleChangeException;
import com.exadel.frs.exception.UserAlreadyInOrganizationException;
import com.exadel.frs.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isBlank;

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

    private void verifyUserHasReadPrivileges(final Long userId, final Organization organization) {
        organization.getUserOrganizationRoleOrThrow(userId);
    }

    private void verifyUserHasWritePrivileges(final Long userId, final Organization organization) {
        if (OrganizationRole.OWNER != organization.getUserOrganizationRoleOrThrow(userId).getRole()) {
            throw new InsufficientPrivilegesException(userId);
        }
    }

    private void verifyNameIsUnique(final String name) {
        if (organizationRepository.existsByName(name)) {
            throw new NameIsNotUniqueException(name);
        }
    }

    public Organization getOrganization(final String guid, final Long userId) {
        Organization organization = getOrganization(guid);
        verifyUserHasReadPrivileges(userId, organization);
        return organization;
    }

    public List<Organization> getOrganizations(final Long userId) {
        return organizationRepository.findAllByUserOrganizationRoles_Id_UserId(userId);
    }

    public OrganizationRole[] getOrgRolesToAssign(final String guid, final Long userId) {
        Organization organization = getOrganization(guid);
        UserOrganizationRole role = organization.getUserOrganizationRoleOrThrow(userId);
        if (OrganizationRole.OWNER.equals(role.getRole())) {
            return OrganizationRole.values();
        }
        return new OrganizationRole[0];
    }

    public List<UserOrganizationRole> getOrgUsers(final String guid, final Long userId) {
        final Organization organization = getOrganization(guid);
        verifyUserHasReadPrivileges(userId, organization);
        return organization.getUserOrganizationRoles();
    }

    public Organization createOrganization(final OrgCreateDto orgCreateDto, final Long userId) {
        verifyNameIsUnique(orgCreateDto.getName());
        Organization organization = Organization.builder()
                .name(orgCreateDto.getName())
                .guid(UUID.randomUUID().toString())
                .build();
        organization.addUserOrganizationRole(userService.getUser(userId), OrganizationRole.OWNER);
        return organizationRepository.save(organization);
    }

    public Organization updateOrganization(final OrgUpdateDto orgUpdateDto, final String guid, final Long userId) {
        if (isBlank(orgUpdateDto.getName())) {
            throw new FieldRequiredException("Organization name");
        }
        Organization organizationFromRepo = getOrganization(guid);
        verifyUserHasWritePrivileges(userId, organizationFromRepo);
        val isNewName = !organizationFromRepo.getName().equals(orgUpdateDto.getName());
        if (isNewName) {
            verifyNameIsUnique(orgUpdateDto.getName());
            organizationFromRepo.setName(orgUpdateDto.getName());
        }

        return organizationRepository.save(organizationFromRepo);
    }

    public UserOrganizationRole updateUserOrgRole(final UserRoleUpdateDto userRoleUpdateDto, final String guid, final Long adminId) {
        Organization organization = getOrganization(guid);
        verifyUserHasWritePrivileges(adminId, organization);

        User user = userService.getUserByGuid(userRoleUpdateDto.getUserId());
        if (user.getId().equals(adminId)) {
            throw new SelfRoleChangeException();
        }
        UserOrganizationRole userOrganizationRole = organization.getUserOrganizationRoleOrThrow(user.getId());
        OrganizationRole newOrgRole = OrganizationRole.valueOf(userRoleUpdateDto.getRole());
        if (OrganizationRole.OWNER.equals(newOrgRole)) {
            organization.getUserOrganizationRoleOrThrow(adminId).setRole(OrganizationRole.ADMINISTRATOR);
        }
        userOrganizationRole.setRole(newOrgRole);

        organizationRepository.save(organization);

        return userOrganizationRole;
    }

    public UserOrganizationRole inviteUser(final UserInviteDto userInviteDto, final String guid, final Long adminId) {
        Organization organization = getOrganization(guid);
        verifyUserHasWritePrivileges(adminId, organization);

        final User user = userService.getUser(userInviteDto.getUserEmail());
        Optional<UserOrganizationRole> userOrganizationRole = organization.getUserOrganizationRole(user.getId());
        if (userOrganizationRole.isPresent()) {
            throw new UserAlreadyInOrganizationException(userInviteDto.getUserEmail(), guid);
        }
        OrganizationRole newOrgRole = OrganizationRole.valueOf(userInviteDto.getRole());
        if (OrganizationRole.OWNER.equals(newOrgRole)) {
            organization.getUserOrganizationRoleOrThrow(adminId).setRole(OrganizationRole.ADMINISTRATOR);
        }
        organization.addUserOrganizationRole(user, newOrgRole);
        final Organization savedOrg = organizationRepository.save(organization);
        return savedOrg.getUserOrganizationRole(user.getId()).orElseThrow();
    }

    public void removeUserFromOrganization(final UserRemoveDto userRemoveDto, final String guid, final Long adminId) {
        Organization organization = getOrganization(guid);
        verifyUserHasWritePrivileges(adminId, organization);

        final User user = userService.getUserByGuid(userRemoveDto.getUserId());
        if (user.getId().equals(adminId)) {
            throw new SelfRemoveException();
        }
        organization.getUserOrganizationRoles().removeIf(userOrganizationRole ->
                userOrganizationRole.getId().getUserId().equals(user.getId()));

        organizationRepository.save(organization);
    }

    @Transactional
    public void deleteOrganization(final String guid, final Long userId) {
        Organization organization = getOrganization(guid);
        verifyUserHasWritePrivileges(userId, organization);
        organizationRepository.deleteById(organization.getId());
    }
}
