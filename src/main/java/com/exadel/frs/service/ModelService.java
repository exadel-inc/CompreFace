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

    public Model getModel(Long modelId) {
        return modelRepository.findById(modelId)
                .orElseThrow(() -> new ModelNotFoundException(modelId));
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

    public Model getModel(Long id, Long userId) {
        Model model = getModel(id);
        verifyUserHasReadPrivileges(userId, model.getApp());
        return model;
    }

    public List<Model> getModels(Long appId, Long userId) {
        verifyUserHasReadPrivileges(userId, appService.getApp(appId));
        return modelRepository.findAllByAppId(appId);
    }

    public void createModel(Model model, Long userId) {
        App repoApp = appService.getApp(model.getApp().getId());
        verifyUserHasWritePrivileges(userId, repoApp);
        if (StringUtils.isEmpty(model.getName())) {
            throw new EmptyRequiredFieldException("name");
        }
        modelRepository.findByNameAndAppId(model.getName(), model.getApp().getId())
                .ifPresent(model1 -> {
                    throw new NameIsNotUniqueException(model1.getName());
                });
        model.setGuid(UUID.randomUUID().toString());
        modelRepository.save(model);
    }

    public void updateModel(Long id, Model model, Long userId) {
        Model repoModel = getModel(id);
        verifyUserHasWritePrivileges(userId, repoModel.getApp());
        if (!StringUtils.isEmpty(model.getName()) && !repoModel.getName().equals(model.getName())) {
            modelRepository.findByNameAndAppId(model.getName(), repoModel.getApp().getId())
                    .ifPresent(model1 -> {
                        throw new NameIsNotUniqueException(model1.getName());
                    });
            repoModel.setName(model.getName());
        }
        if (model.getAppModelAccess() != null) {
            Long repoModelOrganizationId = repoModel.getApp().getOrganization().getId();
            if (repoModel.getAppModelAccess() != null) {
                repoModel.getAppModelAccess().clear();
            }
            model.getAppModelAccess().forEach(appModel -> {
                App app = appService.getApp(appModel.getApp().getId());
                if (!repoModelOrganizationId.equals(app.getOrganization().getId())) {
                    throw new OrganizationMismatchException();
                }
                repoModel.addAppModelAccess(app, appModel.getAccessType());
            });
        }
        modelRepository.save(repoModel);
    }

    public void regenerateGuid(Long id, Long userId) {
        Model repoModel = getModel(id);
        verifyUserHasWritePrivileges(userId, repoModel.getApp());
        repoModel.setGuid(UUID.randomUUID().toString());
        modelRepository.save(repoModel);
    }

    public void deleteModel(Long id, Long userId) {
        verifyUserHasWritePrivileges(userId, getModel(id).getApp());
        modelRepository.deleteById(id);
    }

}
