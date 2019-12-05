package com.exadel.frs.service;

import com.exadel.frs.entity.*;
import com.exadel.frs.enums.AppRole;
import com.exadel.frs.enums.OrganizationRole;
import com.exadel.frs.exception.*;
import com.exadel.frs.repository.AppRepository;
import com.exadel.frs.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppService {

    private final AppRepository appRepository;
    private final OrganizationRepository organizationRepository;

    private App getAppFromRepo(Long appId) {
        return appRepository.findById(appId)
                .orElseThrow(() -> new AppNotFoundException(appId));
    }

    private Organization getOrganizationFromRepo(Long organizationId) {
        return organizationRepository
                .findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException(organizationId));
    }

    private OrganizationRole getUserOrganizationRole(Organization organization, Long userId) {
        return organization.getUserOrganizationRoleOrThrow(userId).getRole();
    }

    private void verifyUserHasReadPrivileges(Long userId, App app) {
        OrganizationRole organizationRole = getUserOrganizationRole(app.getOrganization(), userId);
        if (OrganizationRole.USER == organizationRole) {
            app.getUserAppRole(userId)
                    .orElseThrow(() -> new InsufficientPrivilegesException(userId));
        }
    }

    private void verifyUserHasWritePrivileges(Long userId, Organization organization) {
        if (OrganizationRole.USER == getUserOrganizationRole(organization, userId)) {
            throw new InsufficientPrivilegesException(userId);
        }
    }

    public App getApp(Long id, Long userId) {
        App repoApp = getAppFromRepo(id);
        verifyUserHasReadPrivileges(userId, repoApp);
        return repoApp;
    }

    public List<App> getApps(Long organizationId, Long userId) {
        if (OrganizationRole.USER == getUserOrganizationRole(getOrganizationFromRepo(organizationId), userId)) {
            return appRepository.findAllByOrganizationIdAndUserAppRoles_Id_UserId(organizationId, userId);
        }
        return appRepository.findAllByOrganizationId(organizationId);
    }

    public void createApp(App app, User user) {
        if (StringUtils.isEmpty(app.getName())) {
            throw new EmptyRequiredFieldException("name");
        }
        verifyUserHasWritePrivileges(user.getId(), getOrganizationFromRepo(app.getOrganization().getId()));
        app.setGuid(UUID.randomUUID().toString());
        app.addUserAppRole(user, AppRole.OWNER);
        appRepository.save(app);
    }

    private long getNumberOfOwners(List<UserAppRole> userAppRoles) {
        long ownersCount = userAppRoles.stream()
                .filter(userAppRole -> AppRole.OWNER.equals(userAppRole.getRole()))
                .count();
        if (ownersCount > 1) {
            throw new MultipleOwnersException();
        }
        return ownersCount;
    }

    public void updateApp(Long id, App app, Long userId) {
        App repoApp = getAppFromRepo(id);
        verifyUserHasWritePrivileges(userId, repoApp.getOrganization());
        if (!StringUtils.isEmpty(app.getName())) {
            repoApp.setName(app.getName());
        }
        if (app.getUserAppRoles() != null) {
            if (repoApp.getUserAppRoles() != null) {
                if (getNumberOfOwners(app.getUserAppRoles()) == 0) {
                    repoApp.getUserAppRoles().removeIf(userAppRole -> !AppRole.OWNER.equals(userAppRole.getRole()));
                } else {
                    repoApp.getUserAppRoles().clear();
                }
            }
            app.getUserAppRoles().forEach(userAppRole -> {
                UserOrganizationRole userOrganizationRole = repoApp.getOrganization()
                        .getUserOrganizationRoleOrThrow(userAppRole.getId().getUserId());
                repoApp.addUserAppRole(userOrganizationRole.getUser(), userAppRole.getRole());
            });
        }
        appRepository.save(repoApp);
    }

    public void regenerateGuid(Long id, Long userId) {
        App repoApp = getAppFromRepo(id);
        verifyUserHasWritePrivileges(userId, repoApp.getOrganization());
        repoApp.setGuid(UUID.randomUUID().toString());
        appRepository.save(repoApp);
    }

    public void deleteApp(Long id, Long userId) {
        App repoApp = getAppFromRepo(id);
        verifyUserHasWritePrivileges(userId, repoApp.getOrganization());
        appRepository.deleteById(id);
    }

}
