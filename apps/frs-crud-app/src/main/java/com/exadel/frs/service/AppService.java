package com.exadel.frs.service;

import com.exadel.frs.entity.App;
import com.exadel.frs.entity.Organization;
import com.exadel.frs.entity.UserAppRole;
import com.exadel.frs.entity.UserOrganizationRole;
import com.exadel.frs.enums.AppRole;
import com.exadel.frs.enums.OrganizationRole;
import com.exadel.frs.exception.*;
import com.exadel.frs.repository.AppRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppService {

    private final AppRepository appRepository;
    private final OrganizationService organizationService;
    private final UserService userService;

    public App getApp(Long appId) {
        return appRepository.findById(appId)
                .orElseThrow(() -> new AppNotFoundException(appId));
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
        App repoApp = getApp(id);
        verifyUserHasReadPrivileges(userId, repoApp);
        return repoApp;
    }

    public List<App> getApps(Long organizationId, Long userId) {
        if (OrganizationRole.USER == getUserOrganizationRole(organizationService.getOrganization(organizationId), userId)) {
            return appRepository.findAllByOrganizationIdAndUserAppRoles_Id_UserId(organizationId, userId);
        }
        return appRepository.findAllByOrganizationId(organizationId);
    }

    public void createApp(App app, Long userId) {
        if (StringUtils.isEmpty(app.getName())) {
            throw new EmptyRequiredFieldException("name");
        }
        verifyUserHasWritePrivileges(userId, organizationService.getOrganization(app.getOrganization().getId()));
        app.setGuid(UUID.randomUUID().toString());
        app.setApiKey(UUID.randomUUID().toString());
        app.addUserAppRole(userService.getUser(userId), AppRole.OWNER);
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
        App repoApp = getApp(id);
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
                if (userId.equals(userAppRole.getId().getUserId())) {
                    throw new SelfRoleChangeException();
                }
                UserOrganizationRole userOrganizationRole = repoApp.getOrganization()
                        .getUserOrganizationRoleOrThrow(userAppRole.getId().getUserId());
                repoApp.addUserAppRole(userOrganizationRole.getUser(), userAppRole.getRole());
            });
        }
        appRepository.save(repoApp);
    }

    public void regenerateGuid(Long id, Long userId) {
        App repoApp = getApp(id);
        verifyUserHasWritePrivileges(userId, repoApp.getOrganization());
        repoApp.setGuid(UUID.randomUUID().toString());
        appRepository.save(repoApp);
    }

    public void deleteApp(Long id, Long userId) {
        App repoApp = getApp(id);
        verifyUserHasWritePrivileges(userId, repoApp.getOrganization());
        appRepository.deleteById(id);
    }

}
