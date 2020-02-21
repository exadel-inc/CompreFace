package com.exadel.frs.service;

import com.exadel.frs.dto.ui.ModelCreateDto;
import com.exadel.frs.dto.ui.ModelUpdateDto;
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

    private void verifyUserHasWritePrivileges(final Long userId, final App app) {
        OrganizationRole organizationRole = getUserOrganizationRole(app.getOrganization(), userId);
        if (OrganizationRole.USER == organizationRole) {
            Optional<UserAppRole> userAppRole = app.getUserAppRole(userId);
            if (userAppRole.isEmpty() || AppRole.USER == userAppRole.get().getRole()) {
                throw new InsufficientPrivilegesException(userId);
            }
        }
    }

    private void verifyNameIsUnique(final String name, final Long appId) {
        if (modelRepository.existsByNameAndAppId(name, appId)) {
            throw new NameIsNotUniqueException(name);
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

    public Model createModel(final ModelCreateDto modelCreateDto, final String orgGuid, final String appGuid, final Long userId) {
        App app = appService.getApp(appGuid);
        verifyUserHasWritePrivileges(userId, app);
        if (StringUtils.isEmpty(modelCreateDto.getName())) {
            throw new EmptyRequiredFieldException("name");
        }
        if (!app.getOrganization().getGuid().equals(orgGuid)) {
            throw new AppDoesNotBelongToOrgException(appGuid, orgGuid);
        }
        verifyNameIsUnique(modelCreateDto.getName(), app.getId());
        Model model = Model.builder()
                .name(modelCreateDto.getName())
                .guid(UUID.randomUUID().toString())
                .apiKey(UUID.randomUUID().toString())
                .app(app)
                .build();
        return modelRepository.save(model);
    }

    public void updateModel(final ModelUpdateDto modelUpdateDto, final String modelGuid, final Long userId) {
        verifyNameIsNotEmpty(modelUpdateDto.getName());
        Model repoModel = getModel(modelGuid);
        verifyUserHasWritePrivileges(userId, repoModel.getApp());
        if (!StringUtils.isEmpty(modelUpdateDto.getName()) && !repoModel.getName().equals(modelUpdateDto.getName())) {
            verifyNameIsUnique(modelUpdateDto.getName(), repoModel.getApp().getId());
            repoModel.setName(modelUpdateDto.getName());
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

    private void verifyNameIsNotEmpty(final String newNameForModel) {
        if (StringUtils.isEmpty(newNameForModel)) {
            throw new FieldRequiredException("Model name");
        }
    }
}