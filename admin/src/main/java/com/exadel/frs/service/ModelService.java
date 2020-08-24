/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.exadel.frs.service;

import static com.exadel.frs.enums.AppModelAccess.READONLY;
import static java.util.UUID.randomUUID;
import com.exadel.frs.dto.ui.ModelCreateDto;
import com.exadel.frs.dto.ui.ModelShareDto;
import com.exadel.frs.dto.ui.ModelUpdateDto;
import com.exadel.frs.entity.App;
import com.exadel.frs.entity.AppModel;
import com.exadel.frs.entity.Model;
import com.exadel.frs.entity.Organization;
import com.exadel.frs.enums.OrganizationRole;
import com.exadel.frs.exception.EmptyRequiredFieldException;
import com.exadel.frs.exception.ModelNotFoundException;
import com.exadel.frs.exception.ModelShareRequestNotFoundException;
import com.exadel.frs.exception.NameIsNotUniqueException;
import com.exadel.frs.helpers.SecurityUtils;
import com.exadel.frs.repository.AppModelRepository;
import com.exadel.frs.repository.ModelRepository;
import com.exadel.frs.repository.ModelShareRequestRepository;
import com.exadel.frs.system.rest.CoreFacesClient;
import com.exadel.frs.system.security.AuthorizationManager;
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
    private final CoreFacesClient coreFacesClient;
    private final AuthorizationManager authManager;

    public Model getModel(final String modelGuid) {
        return modelRepository.findByGuid(modelGuid)
                .orElseThrow(() -> new ModelNotFoundException(modelGuid));
    }

    private OrganizationRole getUserOrganizationRole(final Organization organization, final Long userId) {
        return organization.getUserOrganizationRoleOrThrow(userId).getRole();
    }

    private void verifyNameIsUnique(final String name, final Long appId) {
        if (modelRepository.existsByNameAndAppId(name, appId)) {
            throw new NameIsNotUniqueException(name);
        }
    }

    public Model getModel(final String orgGuid, final String appGuid, final String modelGuid, final Long userId) {
        val model = getModel(modelGuid);

        authManager.verifyReadPrivilegesToApp(userId, model.getApp());
        authManager.verifyOrganizationHasTheApp(orgGuid, model.getApp());
        authManager.verifyAppHasTheModel(appGuid, model);

        return model;
    }

    public List<Model> getModels(final String appGuid, final Long userId) {
        val app = appService.getApp(appGuid);

        authManager.verifyReadPrivilegesToApp(userId, app);

        return modelRepository.findAllByAppId(app.getId());
    }

    public Model createModel(final ModelCreateDto modelCreateDto, final String orgGuid, final String appGuid, final Long userId) {
        val app = appService.getApp(appGuid);

        authManager.verifyWritePrivilegesToApp(userId, app);
        authManager.verifyOrganizationHasTheApp(orgGuid, app);

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

        val model = getModel(orgGuid, appGuid, modelGuid, userId);

        authManager.verifyWritePrivilegesToApp(userId, model.getApp());

        if (!model.getName().equals(modelUpdateDto.getName())) {
            verifyNameIsUnique(modelUpdateDto.getName(), model.getApp().getId());
            model.setName(modelUpdateDto.getName());
        }

        return modelRepository.save(model);
    }

    @Transactional
    public void regenerateApiKey(final String orgGuid, final String appGuid, final String guid, final Long userId) {
        val repoModel = getModel(orgGuid, appGuid, guid, userId);

        authManager.verifyWritePrivilegesToApp(userId, repoModel.getApp());

        val newApiKey = randomUUID().toString();

        repoModel.setApiKey(newApiKey);
        modelRepository.save(repoModel);
    }

    @Transactional
    public void deleteModel(final String orgGuid, final String appGuid, final String guid, final Long userId) {
        val model = getModel(orgGuid, appGuid, guid, userId);

        authManager.verifyWritePrivilegesToApp(userId, model.getApp());

        coreFacesClient.deleteFaces(model.getApiKey());
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

        authManager.verifyWritePrivilegesToApp(SecurityUtils.getPrincipalId(), modelBeingShared.getApp());

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