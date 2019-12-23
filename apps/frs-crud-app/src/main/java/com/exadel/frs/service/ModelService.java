package com.exadel.frs.service;

import com.exadel.frs.entity.App;
import com.exadel.frs.entity.Model;
import com.exadel.frs.entity.Organization;
import com.exadel.frs.entity.UserAppRole;
import com.exadel.frs.enums.AppRole;
import com.exadel.frs.enums.OrganizationRole;
import com.exadel.frs.exception.*;
import com.exadel.frs.repository.ModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ModelService {

    private final ModelRepository modelRepository;
    private final AppService appService;

    public Model getModel(final String modelGuid) {
        return modelRepository.findByGuid(modelGuid)
                .orElseThrow(() -> new ModelNotFoundException(modelGuid));
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

    private void verifyUserHasWritePrivileges(Long userId, App app) {
        OrganizationRole organizationRole = getUserOrganizationRole(app.getOrganization(), userId);
        if (OrganizationRole.USER == organizationRole) {
            Optional<UserAppRole> userAppRole = app.getUserAppRole(userId);
            if (userAppRole.isEmpty() || AppRole.USER == userAppRole.get().getRole()) {
                throw new InsufficientPrivilegesException(userId);
            }
        }
    }

    public Model getModel(final String modelGuid, final Long userId) {
        Model model = getModel(modelGuid);
        verifyUserHasReadPrivileges(userId, model.getApp());
        return model;
    }

    public List<Model> getModels(final String appGuid, final Long userId) {
        App app = appService.getApp(appGuid);
        verifyUserHasReadPrivileges(userId, app);
        return modelRepository.findAllByAppId(app.getId());
    }

    public void createModel(Model model, Long userId) {
        App repoApp = appService.getApp(model.getApp().getGuid());
        verifyUserHasWritePrivileges(userId, repoApp);
        if (StringUtils.isEmpty(model.getName())) {
            throw new EmptyRequiredFieldException("name");
        }
        if (modelRepository.existsByNameAndAppId(model.getName(), model.getApp().getId())) {
            throw new NameIsNotUniqueException(model.getName());
        }
        model.setGuid(UUID.randomUUID().toString());
        model.setApiKey(UUID.randomUUID().toString());
        modelRepository.save(model);
    }

    public void updateModel(final String modelGuid, Model model, Long userId) {
        Model repoModel = getModel(modelGuid);
        verifyUserHasWritePrivileges(userId, repoModel.getApp());
        if (!StringUtils.isEmpty(model.getName()) && !repoModel.getName().equals(model.getName())) {
            if (modelRepository.existsByNameAndAppId(model.getName(), repoModel.getApp().getId())) {
                throw new NameIsNotUniqueException(model.getName());
            }
            repoModel.setName(model.getName());
        }
        if (model.getAppModelAccess() != null) {
            Long repoModelOrganizationId = repoModel.getApp().getOrganization().getId();
            if (repoModel.getAppModelAccess() != null) {
                repoModel.getAppModelAccess().clear();
            }
            model.getAppModelAccess().forEach(appModel -> {
                App app = appService.getApp(appModel.getApp().getGuid());
                if (!repoModelOrganizationId.equals(app.getOrganization().getId())) {
                    throw new OrganizationMismatchException();
                }
                repoModel.addAppModelAccess(app, appModel.getAccessType());
            });
        }
        modelRepository.save(repoModel);
    }

    public void regenerateApiKey(final String guid, final Long userId) {
        Model repoModel = getModel(guid);
        verifyUserHasWritePrivileges(userId, repoModel.getApp());
        repoModel.setApiKey(UUID.randomUUID().toString());
        modelRepository.save(repoModel);
    }

    public void deleteModel(final String guid, final Long userId) {
        Model model = getModel(guid);
        verifyUserHasWritePrivileges(userId, model.getApp());
        modelRepository.deleteById(model.getId());
    }

}
