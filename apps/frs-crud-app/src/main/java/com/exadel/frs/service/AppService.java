package com.exadel.frs.service;

import com.exadel.frs.entity.*;
import com.exadel.frs.enums.AppRole;
import com.exadel.frs.enums.OrganizationRole;
import com.exadel.frs.exception.*;
import com.exadel.frs.repository.AppRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppService {

    private final AppRepository appRepository;
    private final OrganizationService organizationService;
    private final UserService userService;

    public App getApp(final String appGuid) {
        return appRepository.findByGuid(appGuid)
                .orElseThrow(() -> new AppNotFoundException(appGuid));
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

    private void verifyNameIsUnique(String name, Long orgId) {
        if (appRepository.existsByNameAndOrganizationId(name, orgId)) {
            throw new NameIsNotUniqueException(name);
        }
    }

    public App getApp(final String appGuid, Long userId) {
        App repoApp = getApp(appGuid);
        verifyUserHasReadPrivileges(userId, repoApp);
        return repoApp;
    }

    public List<App> getApps(final String organizationGuid, final Long userId) {
        final Organization organization = organizationService.getOrganization(organizationGuid);

        if (OrganizationRole.USER == getUserOrganizationRole(organization, userId)) {
            return appRepository.findAllByOrganizationIdAndUserAppRoles_Id_UserId(organization.getId(), userId);
        }

        return appRepository.findAllByOrganizationId(organization.getId());
    }

    public List<UserAppRole> getAppUsers(final String searchText, final String organizationGuid, final String appGuid, final Long userId) {
        final App app = getApp(appGuid);
        verifyUserHasReadPrivileges(userId, app);
        if (!app.getOrganization().getGuid().equals(organizationGuid)) {
            throw new AppDoesNotBelongToOrgException(appGuid, organizationGuid);
        }
        if (!StringUtils.isEmpty(searchText)) {
            List<UserAppRole> result = new ArrayList<>();
            app.getUserAppRoles().forEach(userAppRole -> {
                String searchTextLowerCase = searchText.toLowerCase();
                if (userAppRole.getUser().getFirstName().toLowerCase().contains(searchTextLowerCase) ||
                        userAppRole.getUser().getLastName().toLowerCase().contains(searchTextLowerCase) ||
                        userAppRole.getUser().getEmail().toLowerCase().contains(searchTextLowerCase)) {
                    result.add(userAppRole);
                }
            });
            return result;
        }
        return app.getUserAppRoles();
    }

    public UserAppRole inviteUser(final String userEmail, final AppRole userRole, final String organizationGuid,
                                  final String appGuid, final Long userId) {
        final App app = getApp(appGuid);
        verifyUserHasWritePrivileges(userId, app.getOrganization());

        if (!app.getOrganization().getGuid().equals(organizationGuid)) {
            throw new AppDoesNotBelongToOrgException(appGuid, organizationGuid);
        }
        final User user = userService.getUser(userEmail);
        UserOrganizationRole userOrganizationRole = app.getOrganization().getUserOrganizationRoleOrThrow(user.getId());
        if (!OrganizationRole.USER.equals(userOrganizationRole.getRole()) || app.getUserAppRole(user.getId()).isPresent()) {
            throw new UserAlreadyHasAccessToAppException(userEmail, appGuid);
        }

        app.addUserAppRole(user, userRole);
        final App savedApp = appRepository.save(app);
        return savedApp.getUserAppRole(user.getId()).orElseThrow();
    }

    public App createApp(String organizationGuid, String appName, Long userId) {
        Organization organization = organizationService.getOrganization(organizationGuid);
        verifyUserHasWritePrivileges(userId, organization);
        if (StringUtils.isEmpty(appName)) {
            throw new EmptyRequiredFieldException("name");
        }
        verifyNameIsUnique(appName, organization.getId());
        App app = App.builder()
                .name(appName)
                .organization(organization)
                .guid(UUID.randomUUID().toString())
                .apiKey(UUID.randomUUID().toString())
                .build();
        app.addUserAppRole(userService.getUser(userId), AppRole.OWNER);
        return appRepository.save(app);
    }

//    private long getNumberOfOwners(List<UserAppRole> userAppRoles) {
//        long ownersCount = userAppRoles.stream()
//                .filter(userAppRole -> AppRole.OWNER.equals(userAppRole.getRole()))
//                .count();
//        if (ownersCount > 1) {
//            throw new MultipleOwnersException();
//        }
//        return ownersCount;
//    }

    public App updateApp(final String appGuid, final String appName, final Long userId) {
        App repoApp = getApp(appGuid);
        verifyUserHasWritePrivileges(userId, repoApp.getOrganization());
        if (!StringUtils.isEmpty(appName) && !repoApp.getName().equals(appName)) {
            verifyNameIsUnique(appName, repoApp.getOrganization().getId());
            repoApp.setName(appName);
        }
//        if (app.getUserAppRoles() != null) {
//            if (repoApp.getUserAppRoles() != null) {
//                if (getNumberOfOwners(app.getUserAppRoles()) == 0) {
//                    repoApp.getUserAppRoles().removeIf(userAppRole -> !AppRole.OWNER.equals(userAppRole.getRole()));
//                } else {
//                    repoApp.getUserAppRoles().clear();
//                }
//            }
//            app.getUserAppRoles().forEach(userAppRole -> {
//                if (userId.equals(userAppRole.getId().getUserId())) {
//                    throw new SelfRoleChangeException();
//                }
//                UserOrganizationRole userOrganizationRole = repoApp.getOrganization()
//                        .getUserOrganizationRoleOrThrow(userAppRole.getId().getUserId());
//                repoApp.addUserAppRole(userOrganizationRole.getUser(), userAppRole.getRole());
//            });
//        }
        return appRepository.save(repoApp);
    }

    public void regenerateApiKey(final String guid, final Long userId) {
        App repoApp = getApp(guid);
        verifyUserHasWritePrivileges(userId, repoApp.getOrganization());
        repoApp.setApiKey(UUID.randomUUID().toString());
        appRepository.save(repoApp);
    }

    public void deleteApp(final String guid, final Long userId) {
        App repoApp = getApp(guid);
        verifyUserHasWritePrivileges(userId, repoApp.getOrganization());
        appRepository.deleteById(repoApp.getId());
    }

}
