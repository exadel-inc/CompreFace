package com.exadel.frs.service;

import static com.exadel.frs.enums.AppModelAccess.READONLY;
import static com.exadel.frs.enums.OrganizationRole.USER;
import static java.util.UUID.randomUUID;
import static org.springframework.util.StringUtils.isEmpty;
import com.exadel.frs.dto.ui.ModelCreateDto;
import com.exadel.frs.dto.ui.ModelShareDto;
import com.exadel.frs.dto.ui.ModelUpdateDto;
import com.exadel.frs.entity.App;
import com.exadel.frs.entity.AppModel;
import com.exadel.frs.entity.Model;
import com.exadel.frs.entity.Organization;
import com.exadel.frs.enums.AppRole;
import com.exadel.frs.enums.OrganizationRole;
import com.exadel.frs.exception.AppDoesNotBelongToOrgException;
import com.exadel.frs.exception.EmptyRequiredFieldException;
import com.exadel.frs.exception.InsufficientPrivilegesException;
import com.exadel.frs.exception.ModelDoesNotBelongToAppException;
import com.exadel.frs.exception.ModelNotFoundException;
import com.exadel.frs.exception.ModelShareRequestNotFoundException;
import com.exadel.frs.exception.NameIsNotUniqueException;
import com.exadel.frs.helpers.SecurityUtils;
import com.exadel.frs.system.python.CoreDeleteFacesClient;
import com.exadel.frs.repository.AppModelRepository;
import com.exadel.frs.repository.ModelRepository;
import com.exadel.frs.repository.ModelShareRequestRepository;
import java.util.List;
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
    private final CoreDeleteFacesClient facesClient;

    public Model getModel(final String modelGuid) {
        return modelRepository.findByGuid(modelGuid)
                .orElseThrow(() -> new ModelNotFoundException(modelGuid));
    }

    private OrganizationRole getUserOrganizationRole(final Organization organization, final Long userId) {
        return organization.getUserOrganizationRoleOrThrow(userId).getRole();
    }

    private void verifyUserHasReadPrivileges(final Long userId, final App app) {
        if (USER == getUserOrganizationRole(app.getOrganization(), userId)) {
            app.getUserAppRole(userId)
                    .orElseThrow(() -> new InsufficientPrivilegesException(userId));
        }
    }

    private void verifyUserHasWritePrivileges(final Long userId, final App app) {
        if (USER == getUserOrganizationRole(app.getOrganization(), userId)) {
            val userAppRole = app.getUserAppRole(userId);
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

    private void verifyOrganizationHasTheApp(final String orgGuid, final App app) {
        if (!app.getOrganization().getGuid().equals(orgGuid)) {
            throw new AppDoesNotBelongToOrgException(app.getGuid(), orgGuid);
        }
    }

    private void verifyNameIsNotEmpty(final String newNameForModel) {
        if (isEmpty(newNameForModel)) {
            throw new EmptyRequiredFieldException("name");
        }
    }

    private void verifyAppHasTheModel(final String appGuid, final Model model) {
        if (!model.getApp().getGuid().equals(appGuid)) {
            throw new ModelDoesNotBelongToAppException(model.getGuid(), appGuid);
        }
    }

    public Model getModel(final String orgGuid, final String appGuid, final String modelGuid, final Long userId) {
        val model = getModel(modelGuid);

        verifyUserHasReadPrivileges(userId, model.getApp());
        verifyOrganizationHasTheApp(orgGuid, model.getApp());
        verifyAppHasTheModel(appGuid, model);

        return model;
    }

    public List<Model> getModels(final String appGuid, final Long userId) {
        val app = appService.getApp(appGuid);

        verifyUserHasReadPrivileges(userId, app);

        return modelRepository.findAllByAppId(app.getId());
    }

    public Model createModel(final ModelCreateDto modelCreateDto, final String orgGuid, final String appGuid, final Long userId) {
        val app = appService.getApp(appGuid);

        verifyUserHasWritePrivileges(userId, app);

        if (isEmpty(modelCreateDto.getName())) {
            throw new EmptyRequiredFieldException("name");
        }
        if (!app.getOrganization().getGuid().equals(orgGuid)) {
            throw new AppDoesNotBelongToOrgException(appGuid, orgGuid);
        }

        verifyNameIsUnique(modelCreateDto.getName(), app.getId());

        val model = Model.builder()
                .name(modelCreateDto.getName())
                .guid(randomUUID().toString())
                .apiKey(randomUUID().toString())
                .app(app)
                .build();

        return modelRepository.save(model);
    }

    public Model updateModel(
            final ModelUpdateDto modelUpdateDto,
            final String orgGuid,
            final String appGuid,
            final String modelGuid,
            final Long userId
    ) {
        verifyNameIsNotEmpty(modelUpdateDto.getName());

        val model = getModel(orgGuid, appGuid, modelGuid, userId);

        verifyUserHasWritePrivileges(userId, model.getApp());

        if (!model.getName().equals(modelUpdateDto.getName())) {
            verifyNameIsUnique(modelUpdateDto.getName(), model.getApp().getId());
            model.setName(modelUpdateDto.getName());
        }

        return modelRepository.save(model);
    }

    public void regenerateApiKey(final String orgGuid, final String appGuid, final String guid, final Long userId) {
        val repoModel = getModel(orgGuid, appGuid, guid, userId);

        verifyUserHasWritePrivileges(userId, repoModel.getApp());

        repoModel.setApiKey(randomUUID().toString());

        modelRepository.save(repoModel);
    }

    @Transactional
    public void deleteModel(final String orgGuid, final String appGuid, final String guid, final Long userId) {
        val model = getModel(orgGuid, appGuid, guid, userId);

        verifyUserHasWritePrivileges(userId, model.getApp());

        facesClient.deleteFaces(appGuid + guid);
        modelRepository.deleteById(model.getId());
    }

    @Transactional
    public App share(
            final ModelShareDto modelShare,
            final String orgGuid,
            final String appGuid,
            final String modelGuid
    ) {
        verifyShareRequest(modelShare);

        val modelBeingShared = getModel(orgGuid, appGuid, modelGuid, SecurityUtils.getPrincipalId());

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