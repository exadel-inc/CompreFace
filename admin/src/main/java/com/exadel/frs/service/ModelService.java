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
import com.exadel.frs.repository.AppModelRepository;
import com.exadel.frs.repository.ModelRepository;
import com.exadel.frs.repository.ModelShareRequestRepository;
import com.exadel.frs.system.rest.CoreFacesClient;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

import static com.exadel.frs.enums.AppModelAccess.READONLY;
import static com.exadel.frs.enums.OrganizationRole.USER;
import static java.util.UUID.randomUUID;

@Service
@RequiredArgsConstructor
public class ModelService {

    private final ModelRepository modelRepository;
    private final AppService appService;
    private final ModelShareRequestRepository modelShareRequestRepository;
    private final AppModelRepository appModelRepository;
    private final CoreFacesClient coreFacesClient;

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
                    .orElseThrow(() -> new InsufficientPrivilegesException());
        }
    }

    private void verifyUserHasWritePrivileges(final Long userId, final App app) {
        if (USER == getUserOrganizationRole(app.getOrganization(), userId)) {
            val userAppRole = app.getUserAppRole(userId);
            if (userAppRole.isEmpty() || AppRole.USER == userAppRole.get().getRole()) {
                throw new InsufficientPrivilegesException();
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

        val model = getModel(orgGuid, appGuid, modelGuid, userId);

        verifyUserHasWritePrivileges(userId, model.getApp());

        if (!model.getName().equals(modelUpdateDto.getName())) {
            verifyNameIsUnique(modelUpdateDto.getName(), model.getApp().getId());
            model.setName(modelUpdateDto.getName());
        }

        return modelRepository.save(model);
    }

    @Transactional
    public void regenerateApiKey(final String orgGuid, final String appGuid, final String guid, final Long userId) {
        val repoModel = getModel(orgGuid, appGuid, guid, userId);

        verifyUserHasWritePrivileges(userId, repoModel.getApp());

        val newApiKey = randomUUID().toString();

        coreFacesClient.updateModelKeyForFaces(repoModel.getApiKey(), newApiKey);

        repoModel.setApiKey(newApiKey);
        modelRepository.save(repoModel);
    }

    @Transactional
    public void deleteModel(final String orgGuid, final String appGuid, final String guid, final Long userId) {
        val model = getModel(orgGuid, appGuid, guid, userId);

        verifyUserHasWritePrivileges(userId, model.getApp());

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