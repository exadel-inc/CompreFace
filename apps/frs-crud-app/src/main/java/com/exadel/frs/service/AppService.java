package com.exadel.frs.service;

import com.exadel.frs.dto.ui.AppCreateDto;
import com.exadel.frs.dto.ui.AppUpdateDto;
import com.exadel.frs.dto.ui.UserInviteDto;
import com.exadel.frs.dto.ui.UserRoleUpdateDto;
import com.exadel.frs.entity.*;
import com.exadel.frs.enums.AppRole;
import com.exadel.frs.enums.OrganizationRole;
import com.exadel.frs.exception.*;
import com.exadel.frs.helpers.SecurityUtils;
import com.exadel.frs.repository.AppRepository;
import com.exadel.frs.repository.ModelShareRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppService {

    private final AppRepository appRepository;
    private final OrganizationService organizationService;
    private final UserService userService;
    private final ModelShareRequestRepository modelShareRequestRepository;

    public App getApp(final String appGuid) {
        return appRepository.findByGuid(appGuid)
                .orElseThrow(() -> new AppNotFoundException(appGuid));
    }

    private OrganizationRole getUserOrganizationRole(final Organization organization, final Long userId) {
        return organization.getUserOrganizationRoleOrThrow(userId).getRole();
    }

    private void verifyUserHasReadPrivileges(final Long userId, final App app) {
        OrganizationRole organizationRole = getUserOrganizationRole(app.getOrganization(), userId);
        if (OrganizationRole.USER == organizationRole) {
            app.getUserAppRole(userId)
                    .orElseThrow(() -> new InsufficientPrivilegesException(userId));
        }
    }

    private void verifyUserHasWritePrivileges(final Long userId, final Organization organization) {
        if (OrganizationRole.USER == getUserOrganizationRole(organization, userId)) {
            throw new InsufficientPrivilegesException(userId);
        }
    }

    private void verifyNameIsUnique(final String name, final Long orgId) {
        if (appRepository.existsByNameAndOrganizationId(name, orgId)) {
            throw new NameIsNotUniqueException(name);
        }
    }

    public App getApp(final String appGuid, final Long userId) {
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

    public AppRole[] getAppRolesToAssign(final String orgGuid, final String appGuid, final Long userId) {
        final App app = getApp(appGuid);
        Optional<UserAppRole> userAppRole = app.getUserAppRole(userId);
        if (userAppRole.isPresent() && AppRole.OWNER.equals(userAppRole.get().getRole())) {
            return AppRole.values();
        }
        UserOrganizationRole orgRole = app.getOrganization().getUserOrganizationRoleOrThrow(userId);
        if (OrganizationRole.USER.equals(orgRole.getRole())) {
            return new AppRole[0];
        }
        return AppRole.values();
    }

    public List<UserAppRole> getAppUsers(final String searchText, final String orgGuid, final String appGuid, final Long userId) {
        final App app = getApp(appGuid);
        verifyUserHasReadPrivileges(userId, app);
        if (!app.getOrganization().getGuid().equals(orgGuid)) {
            throw new AppDoesNotBelongToOrgException(appGuid, orgGuid);
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

    public UserAppRole inviteUser(final UserInviteDto userInviteDto, final String organizationGuid,
                                  final String appGuid, final Long userId) {
        final App app = getApp(appGuid);
        verifyUserHasWritePrivileges(userId, app.getOrganization());

        if (!app.getOrganization().getGuid().equals(organizationGuid)) {
            throw new AppDoesNotBelongToOrgException(appGuid, organizationGuid);
        }
        final User user = userService.getUser(userInviteDto.getUserEmail());
        UserOrganizationRole userOrganizationRole = app.getOrganization().getUserOrganizationRoleOrThrow(user.getId());
        if (!OrganizationRole.USER.equals(userOrganizationRole.getRole()) || app.getUserAppRole(user.getId()).isPresent()) {
            throw new UserAlreadyHasAccessToAppException(userInviteDto.getUserEmail(), appGuid);
        }

        app.addUserAppRole(user, AppRole.valueOf(userInviteDto.getRole()));
        final App savedApp = appRepository.save(app);
        return savedApp.getUserAppRole(user.getId()).orElseThrow();
    }

    public App createApp(final AppCreateDto appCreateDto, final String organizationGuid, final Long userId) {
        Organization organization = organizationService.getOrganization(organizationGuid);
        verifyUserHasWritePrivileges(userId, organization);
        if (StringUtils.isEmpty(appCreateDto.getName())) {
            throw new EmptyRequiredFieldException("name");
        }
        verifyNameIsUnique(appCreateDto.getName(), organization.getId());
        App app = App.builder()
                .name(appCreateDto.getName())
                .organization(organization)
                .guid(UUID.randomUUID().toString())
                .apiKey(UUID.randomUUID().toString())
                .build();
        app.addUserAppRole(userService.getUser(userId), AppRole.OWNER);
        return appRepository.save(app);
    }

    public App updateApp(final AppUpdateDto appUpdateDto, final String appGuid, final Long userId) {
        App repoApp = getApp(appGuid);
        verifyUserHasWritePrivileges(userId, repoApp.getOrganization());
        if (!StringUtils.isEmpty(appUpdateDto.getName()) && !repoApp.getName().equals(appUpdateDto.getName())) {
            verifyNameIsUnique(appUpdateDto.getName(), repoApp.getOrganization().getId());
            repoApp.setName(appUpdateDto.getName());
        }
        return appRepository.save(repoApp);
    }

    public void updateUserAppRole(final UserRoleUpdateDto userRoleUpdateDto, final String guid, final Long adminId) {
        App app = getApp(guid);
        verifyUserHasWritePrivileges(adminId, app.getOrganization());

        User user = userService.getUserByGuid(userRoleUpdateDto.getUserId());
        if (user.getId().equals(adminId)) {
            throw new SelfRoleChangeException();
        }
        UserAppRole userAppRole = app.getUserAppRole(user.getId()).orElseThrow();
        AppRole newAppRole = AppRole.valueOf(userRoleUpdateDto.getRole());
        if (AppRole.OWNER.equals(newAppRole)) {
            app.getOwner().ifPresent(previousOwner -> previousOwner.setRole(AppRole.ADMINISTRATOR));
        }
        userAppRole.setRole(newAppRole);
        appRepository.save(app);
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

    public UUID generateUuidToRequestModelShare(String appGuid) {
        final App repoApp = getApp(appGuid);
        verifyUserHasWritePrivileges(SecurityUtils.getPrincipalId(), repoApp.getOrganization());
        val requestId = UUID.randomUUID();
        val id = ModelShareRequestId.builder().appId(repoApp.getId()).requestId(requestId).build();
        val shareRequest = ModelShareRequest.builder().app(repoApp).id(id).build();
        modelShareRequestRepository.save(shareRequest);
        return requestId;
    }
}
