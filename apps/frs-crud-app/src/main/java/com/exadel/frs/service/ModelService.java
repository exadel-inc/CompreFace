package com.exadel.frs.service;

import static com.exadel.frs.enums.AppModelAccess.READONLY;
import static com.exadel.frs.enums.OrganizationRole.USER;
import static org.springframework.util.StringUtils.isEmpty;
import com.exadel.frs.dto.ui.ModelCreateDto;
import com.exadel.frs.dto.ui.ModelShareDto;
import com.exadel.frs.dto.ui.ModelUpdateDto;
import com.exadel.frs.entity.App;
import com.exadel.frs.entity.AppModel;
import com.exadel.frs.entity.Model;
import com.exadel.frs.entity.Organization;
import com.exadel.frs.entity.UserAppRole;
import com.exadel.frs.enums.AppRole;
import com.exadel.frs.enums.OrganizationRole;
import com.exadel.frs.exception.AppDoesNotBelongToOrgException;
import com.exadel.frs.exception.EmptyRequiredFieldException;
import com.exadel.frs.exception.InsufficientPrivilegesException;
import com.exadel.frs.exception.ModelNotFoundException;
import com.exadel.frs.exception.ModelShareRequestNotFoundException;
import com.exadel.frs.exception.NameIsNotUniqueException;
import com.exadel.frs.helpers.SecurityUtils;
import com.exadel.frs.repository.AppModelRepository;
import com.exadel.frs.repository.ModelRepository;
import com.exadel.frs.repository.ModelShareRequestRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ModelService {

    private final ModelRepository modelRepository;
    private final AppService appService;
    private final ModelShareRequestRepository modelShareRequestRepository;
    private final AppModelRepository appModelRepository;

    public Model getModel(final String modelGuid) {
        return modelRepository.findByGuid(modelGuid)
                .orElseThrow(() -> new ModelNotFoundException(modelGuid));
    }

    private OrganizationRole getUserOrganizationRole(final Organization organization, final Long userId) {
        return organization.getUserOrganizationRoleOrThrow(userId).getRole();
    }

    private void verifyUserHasReadPrivileges(final Long userId, final App app) {
        OrganizationRole organizationRole = getUserOrganizationRole(app.getOrganization(), userId);
        if (USER == organizationRole) {
            app.getUserAppRole(userId)
                    .orElseThrow(() -> new InsufficientPrivilegesException(userId));
        }
    }

    private void verifyUserHasWritePrivileges(final Long userId, final App app) {
        OrganizationRole organizationRole = getUserOrganizationRole(app.getOrganization(), userId);
        if (USER == organizationRole) {
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
        if (isEmpty(modelCreateDto.getName())) {
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

    public Model updateModel(final ModelUpdateDto modelUpdateDto, final String modelGuid, final Long userId) {
        verifyNameIsNotEmpty(modelUpdateDto.getName());
        Model repoModel = getModel(modelGuid);
        verifyUserHasWritePrivileges(userId, repoModel.getApp());
        if (!repoModel.getName().equals(modelUpdateDto.getName())) {
            verifyNameIsUnique(modelUpdateDto.getName(), repoModel.getApp().getId());
            repoModel.setName(modelUpdateDto.getName());
        }

        return modelRepository.save(repoModel);
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
        if (isEmpty(newNameForModel)) {
            throw new EmptyRequiredFieldException("name");
        }
    }

    @Transactional
    public App share(final ModelShareDto modelShare, final String modelGuid) {
        verifyShareRequest(modelShare);
        val modelBeingShared = getModel(modelGuid);
        verifyUserHasWritePrivileges(SecurityUtils.getPrincipalId(), modelBeingShared.getApp());

        val modelShareRequest = modelShareRequestRepository.findModelShareRequestByRequestId(modelShare.getRequestId());
        if (modelShareRequest == null) {
            throw new ModelShareRequestNotFoundException(modelShare.getRequestId());
        }
        val appFromRequest = modelShareRequest.getApp();

        appModelRepository.save(new AppModel(appFromRequest, modelBeingShared, READONLY));
        modelShareRequestRepository.delete(modelShareRequest);

        return appFromRequest;
    }

    private void verifyShareRequest(final ModelShareDto modelShare) {
        if (modelShare.getRequestId() == null) {
            throw new EmptyRequiredFieldException("requestId");
        }
    }
}