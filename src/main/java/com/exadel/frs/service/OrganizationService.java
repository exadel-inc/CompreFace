package com.exadel.frs.service;

import com.exadel.frs.entity.Organization;
import com.exadel.frs.entity.User;
import com.exadel.frs.entity.UserOrganizationRole;
import com.exadel.frs.enums.OrganizationRole;
import com.exadel.frs.exception.*;
import com.exadel.frs.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserService userService;

    public Organization getOrganization(Long organizationId) {
        return organizationRepository
                .findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException(organizationId));
    }

    private void verifyUserHasReadPrivileges(Long userId, Organization organization) {
        organization.getUserOrganizationRoleOrThrow(userId);
    }

    private void verifyUserHasWritePrivileges(Long userId, Organization organization) {
        if (OrganizationRole.OWNER != organization.getUserOrganizationRoleOrThrow(userId).getRole()) {
            throw new InsufficientPrivilegesException(userId);
        }
    }

    public Organization getOrganization(Long id, Long userId) {
        Organization organization = getOrganization(id);
        verifyUserHasReadPrivileges(userId, organization);
        return organization;
    }

    public List<Organization> getOrganizations(Long userId) {
        return organizationRepository.findAllByUserOrganizationRoles_Id_UserId(userId);
    }

    public void createOrganization(Organization organization, Long userId) {
        if (StringUtils.isEmpty(organization.getName())) {
            throw new EmptyRequiredFieldException("name");
        }
        organizationRepository.findByName(organization.getName())
                .ifPresent(organization1 -> {
                    throw new NameIsNotUniqueException(organization1.getName());
                });
        organization.addUserOrganizationRole(userService.getUser(userId), OrganizationRole.OWNER);
        organizationRepository.save(organization);
    }

    private void verifyNumberOfOwners(List<UserOrganizationRole> userOrganizationRoles) {
        long ownersCount = userOrganizationRoles.stream()
                .filter(userOrganizationRole -> OrganizationRole.OWNER.equals(userOrganizationRole.getRole()))
                .count();
        if (ownersCount > 1) {
            throw new MultipleOwnersException();
        }
    }

    public void updateOrganization(Long id, Organization organization, Long userId) {
        Organization repoOrganization = getOrganization(id);
        verifyUserHasWritePrivileges(userId, repoOrganization);
        if (!StringUtils.isEmpty(organization.getName()) && !repoOrganization.getName().equals(organization.getName())) {
            organizationRepository.findByName(organization.getName())
                    .ifPresent(organization1 -> {
                        throw new NameIsNotUniqueException(organization1.getName());
                    });
            repoOrganization.setName(organization.getName());
        }
        if (!CollectionUtils.isEmpty(organization.getUserOrganizationRoles())) {
            verifyNumberOfOwners(organization.getUserOrganizationRoles());
            organization.getUserOrganizationRoles().forEach(userOrganizationRole -> {
                if (userId.equals(userOrganizationRole.getId().getUserId())) {
                    throw new SelfRoleChangeException();
                }
                if (OrganizationRole.OWNER.equals(userOrganizationRole.getRole())) {
                    repoOrganization.getUserOrganizationRoleOrThrow(userId)
                            .setRole(OrganizationRole.ADMINISTRATOR);
                }
                repoOrganization.getUserOrganizationRoleOrThrow(userOrganizationRole.getId().getUserId())
                        .setRole(userOrganizationRole.getRole());
            });
        }
        organizationRepository.save(repoOrganization);
    }

    // todo implement user invitation to organization by email. then delete this method
    public void addUserToOrganization(Long id, Organization organization, Long userId) {
        Organization repoOrganization = getOrganization(id);
        verifyUserHasWritePrivileges(userId, repoOrganization);
        if (!CollectionUtils.isEmpty(organization.getUserOrganizationRoles())) {
            organization.getUserOrganizationRoles().forEach(userOrganizationRole -> {
                if (repoOrganization.getUserOrganizationRole(userOrganizationRole.getId().getUserId()).isEmpty()) {
                    User user = userService.getUser(userOrganizationRole.getId().getUserId());
                    repoOrganization.addUserOrganizationRole(user, OrganizationRole.USER);
                }
            });
        }
        organizationRepository.save(repoOrganization);
    }

    public void removeUserFromOrganization(Long id, Organization organization, Long userId) {
        Organization repoOrganization = getOrganization(id);
        verifyUserHasWritePrivileges(userId, repoOrganization);
        if (!CollectionUtils.isEmpty(organization.getUserOrganizationRoles())) {
            organization.getUserOrganizationRoles().forEach(userOrganizationRole -> {
                if (userId.equals(userOrganizationRole.getId().getUserId())) {
                    throw new SelfRemoveException();
                }
                repoOrganization.getUserOrganizationRoles().removeIf(userOrganizationRole1 ->
                        userOrganizationRole1.getId().getUserId().equals(userOrganizationRole.getId().getUserId()));
            });
        }
        organizationRepository.save(repoOrganization);
    }

    public void deleteOrganization(Long id, Long userId) {
        Organization organization = getOrganization(id);
        verifyUserHasWritePrivileges(userId, organization);
        organizationRepository.deleteById(id);
    }

}
