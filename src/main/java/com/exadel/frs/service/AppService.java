package com.exadel.frs.service;

import com.exadel.frs.entity.App;
import com.exadel.frs.entity.Organization;
import com.exadel.frs.entity.User;
import com.exadel.frs.enums.OrganizationRole;
import com.exadel.frs.exception.*;
import com.exadel.frs.repository.AppRepository;
import com.exadel.frs.repository.OrganizationRepository;
import com.exadel.frs.repository.UserRepository;
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
    private final UserRepository userRepository;

    private App getAppFromRepo(Long appId) {
        return appRepository.findById(appId)
                .orElseThrow(() -> new AppNotFoundException(appId));
    }

    private Organization getOrganizationFromRepo(Long organizationId) {
        return organizationRepository
                .findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException(organizationId));
    }

    private OrganizationRole getUserOrganizationRole(Long organizationId, Long userId) {
        return getOrganizationFromRepo(organizationId).getUserOrganizationRoleOrThrow(userId).getRole();
    }

    private void verifyUserHasReadPrivileges(Long userId, App app) {
        OrganizationRole organizationRole = getUserOrganizationRole(app.getOrganization().getId(), userId);
        if (OrganizationRole.USER == organizationRole) {
            app.getUserAppRole(userId)
                    .orElseThrow(() -> new InsufficientPrivilegesException(userId));
        }
    }

    private void verifyUserHasWritePrivileges(Long userId, Long organizationId) {
        if (OrganizationRole.USER == getUserOrganizationRole(organizationId, userId)) {
            throw new InsufficientPrivilegesException(userId);
        }
    }

    public App getApp(Long id, Long userId) {
        App app = getAppFromRepo(id);
        verifyUserHasReadPrivileges(userId, app);
        return app;
    }

    public List<App> getApps(Long organizationId, Long userId) {
        if (OrganizationRole.USER == getUserOrganizationRole(organizationId, userId)) {
            return appRepository.findAllByOrganizationIdAndUserAppRoles_Id_UserId(organizationId, userId);
        }
        return appRepository.findAllByOrganizationId(organizationId);
    }

    public void createApp(App app, Long userId) {
        if (StringUtils.isEmpty(app.getName())) {
            throw new EmptyRequiredFieldException("name");
        }
        verifyUserHasWritePrivileges(userId, app.getOrganization().getId());
        app.setGuid(UUID.randomUUID().toString());
        appRepository.save(app);
    }

    public void updateApp(Long id, App app, Long userId) {
        App repoApp = getAppFromRepo(id);
        verifyUserHasWritePrivileges(userId, repoApp.getOrganization().getId());
        if (!StringUtils.isEmpty(app.getName())) {
            repoApp.setName(app.getName());
        }
        if (app.getUserAppRoles() != null) {
            repoApp.getUserAppRoles().clear();
            app.getUserAppRoles().forEach(userAppRoleDto -> {
                if (repoApp.getUserAppRole(userAppRoleDto.getId().getUserId()).isEmpty()) {
                    User user = userRepository.findById(userAppRoleDto.getId().getUserId())
                            .orElseThrow(() -> new UserDoesNotExistException(userAppRoleDto.getId().getUserId()));
                    repoApp.addUserAppRole(user, userAppRoleDto.getRole());
                }
            });
        }
        appRepository.save(repoApp);
    }

    public void regenerateGuid(Long id, Long userId) {
        App app = getAppFromRepo(id);
        verifyUserHasWritePrivileges(userId, app.getOrganization().getId());
        app.setGuid(UUID.randomUUID().toString());
        appRepository.save(app);
    }

    public void deleteApp(Long id, Long userId) {
        App app = getAppFromRepo(id);
        verifyUserHasWritePrivileges(userId, app.getOrganization().getId());
        appRepository.deleteById(id);
    }

}
