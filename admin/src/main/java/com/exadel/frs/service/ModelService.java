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
import com.exadel.frs.exception.EmptyRequiredFieldException;
import com.exadel.frs.exception.ModelNotFoundException;
import com.exadel.frs.exception.ModelShareRequestNotFoundException;
import com.exadel.frs.exception.NameIsNotUniqueException;
import com.exadel.frs.helpers.SecurityUtils;
import com.exadel.frs.repository.AppModelRepository;
import com.exadel.frs.repository.ModelRepository;
import com.exadel.frs.repository.ModelShareRequestRepository;
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
    private final AuthorizationManager authManager;
    private final UserService userService;

    public Model getModel(final String modelGuid) {
        return modelRepository.findByGuid(modelGuid)
                              .orElseThrow(() -> new ModelNotFoundException(modelGuid));
    }

    private void verifyNameIsUnique(final String name, final Long appId) {
        if (modelRepository.existsByNameAndAppId(name, appId)) {
            throw new NameIsNotUniqueException(name);
        }
    }

    public Model getModel(final String appGuid, final String modelGuid, final Long userId) {
        val model = getModel(modelGuid);
        val user = userService.getUser(userId);

        authManager.verifyReadPrivilegesToApp(user, model.getApp());
        authManager.verifyAppHasTheModel(appGuid, model);

        return model;
    }

    public List<Model> getModels(final String appGuid, final Long userId) {
        val app = appService.getApp(appGuid);
        val user = userService.getUser(userId);

        authManager.verifyReadPrivilegesToApp(user, app);

        return modelRepository.findAllByAppId(app.getId());
    }

    public Model createModel(final ModelCreateDto modelCreateDto, final String appGuid, final Long userId) {
        val app = appService.getApp(appGuid);
        val user = userService.getUser(userId);

        authManager.verifyWritePrivilegesToApp(user, app);

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
            final String appGuid,
            final String modelGuid,
            final Long userId
    ) {
        val user = userService.getUser(userId);
        val model = getModel(appGuid, modelGuid, userId);

        authManager.verifyWritePrivilegesToApp(user, model.getApp());

        if (!model.getName().equals(modelUpdateDto.getName())) {
            verifyNameIsUnique(modelUpdateDto.getName(), model.getApp().getId());
            model.setName(modelUpdateDto.getName());
        }

        return modelRepository.save(model);
    }

    @Transactional
    public void regenerateApiKey(final String appGuid, final String guid, final Long userId) {
        val repoModel = getModel(appGuid, guid, userId);
        val user = userService.getUser(userId);

        authManager.verifyWritePrivilegesToApp(user, repoModel.getApp());

        val newApiKey = randomUUID().toString();

        repoModel.setApiKey(newApiKey);
        modelRepository.save(repoModel);
    }

    @Transactional
    public void deleteModel(final String appGuid, final String guid, final Long userId) {
        val model = getModel(appGuid, guid, userId);
        val user = userService.getUser(userId);

        authManager.verifyWritePrivilegesToApp(user, model.getApp());

        modelRepository.deleteById(model.getId());
    }

    @Transactional
    public App share(
            final ModelShareDto modelShare,
            final String appGuid,
            final String modelGuid
    ) {
        verifyShareRequest(modelShare);

        val modelBeingShared = getModel(appGuid, modelGuid, SecurityUtils.getPrincipalId());
        val user = userService.getUser(SecurityUtils.getPrincipalId());

        authManager.verifyWritePrivilegesToApp(user, modelBeingShared.getApp());

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