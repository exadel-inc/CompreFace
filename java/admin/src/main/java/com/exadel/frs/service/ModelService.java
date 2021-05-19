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

import com.exadel.frs.commonservice.annotation.CollectStatistics;
import com.exadel.frs.commonservice.entity.*;
import com.exadel.frs.commonservice.enums.ModelType;
import com.exadel.frs.commonservice.enums.StatisticsType;
import com.exadel.frs.commonservice.exception.ModelNotFoundException;
import com.exadel.frs.commonservice.repository.ModelRepository;
import com.exadel.frs.commonservice.repository.SubjectRepository;
import com.exadel.frs.dto.ui.ModelCloneDto;
import com.exadel.frs.dto.ui.ModelCreateDto;
import com.exadel.frs.dto.ui.ModelUpdateDto;
import com.exadel.frs.exception.NameIsNotUniqueException;
import com.exadel.frs.system.security.AuthorizationManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

import static java.util.UUID.randomUUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModelService {

    private final ModelRepository modelRepository;
    private final AppService appService;
    private final AuthorizationManager authManager;
    private final UserService userService;
    private final SubjectRepository subjectRepository;
    private final JdbcTemplate jdbcTemplate;

    public Model getModel(final String modelGuid) {
        return modelRepository.findByGuid(modelGuid)
                .orElseThrow(() -> new ModelNotFoundException(modelGuid, ""));
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

    private Model createModel(final ModelCreateDto modelCreateDto, final String appGuid, final Long userId) {
        App app = appService.getApp(appGuid);
        User user = userService.getUser(userId);

        authManager.verifyWritePrivilegesToApp(user, app);

        verifyNameIsUnique(modelCreateDto.getName(), app.getId());

        log.info("model type: {}", modelCreateDto.getType());

        return modelRepository.save(buildModel(modelCreateDto, app));
    }

    public Model buildModel(ModelCreateDto modelCreateDto, App app) {
        return Model.builder()
                .name(modelCreateDto.getName())
                .guid(randomUUID().toString())
                .apiKey(randomUUID().toString())
                .app(app)
                .type(ModelType.valueOf(modelCreateDto.getType()))
                .build();
    }

    @CollectStatistics(type = StatisticsType.FACE_RECOGNITION_CREATE)
    public Model createRecognitionModel(ModelCreateDto modelCreateDto, final String appGuid, final Long userId) {
        Model model = createModel(modelCreateDto, appGuid, userId);
        log.info("recognition model created: {} ", model);
        return model;
    }

    @CollectStatistics(type = StatisticsType.FACE_VERIFICATION_CREATE)
    public Model createVerificationModel(ModelCreateDto modelCreateDto, final String appGuid, final Long userId) {
        Model model = createModel(modelCreateDto, appGuid, userId);
        log.info("verification model created: {}", model);
        return model;
    }

    @CollectStatistics(type = StatisticsType.FACE_DETECTION_CREATE)
    public Model createDetectionModel(ModelCreateDto modelCreateDto, final String appGuid, final Long userId) {
        Model model = createModel(modelCreateDto, appGuid, userId);
        log.info("detection model created: {}", model);
        return model;
    }

    @Transactional
    public Model cloneModel(
            final ModelCloneDto modelCloneDto,
            final String appGuid,
            final String modelGuid,
            final Long userId
    ) {
        val user = userService.getUser(userId);
        val model = getModel(appGuid, modelGuid, userId);

        authManager.verifyWritePrivilegesToApp(user, model.getApp());

        verifyNameIsUnique(modelCloneDto.getName(), model.getApp().getId());

        val clone = new Model(model);
        clone.setId(null);
        clone.setName(modelCloneDto.getName());

        val clonedModel = modelRepository.save(clone);

        List<AppModel> clonedAppModelAccessList = cloneAppModels(model, clonedModel);
        clonedModel.setAppModelAccess(clonedAppModelAccessList);

        // caution: time consuming operation
        cloneSubjects(model.getApiKey(), clone.getApiKey());

        return clonedModel;
    }

    private List<AppModel> cloneAppModels(final Model model, final Model clonedModel) {
        val cloneAppModelAccessList = new ArrayList<AppModel>();
        for (val appModel : model.getAppModelAccess()) {
            AppModel cloneAppModelAccess = new AppModel(appModel);
            cloneAppModelAccess.setId(new AppModelId(clonedModel.getApp().getId(), clonedModel.getId()));
            cloneAppModelAccess.setModel(clonedModel);

            cloneAppModelAccessList.add(cloneAppModelAccess);
        }
        return cloneAppModelAccessList;
    }

    @Transactional
    public void cloneSubjects(final String sourceApiKey, final String newApiKey) {
        subjectRepository
                .findByApiKey(sourceApiKey)
                .forEach(subject -> cloneSubject(subject, newApiKey));
    }

    private void cloneSubject(Subject subject, String newApiKey) {
        var newSubjectId = UUID.randomUUID();
        jdbcTemplate.update(
                "insert into subject(id, api_key, subject_name) values (?, ?, ?)",
                newSubjectId, newApiKey, subject.getSubjectName()
        );

        Map<UUID, UUID> sourceImgId2NewImgId = new HashMap<>();
        jdbcTemplate.query(
                "select i.id as img_id from embedding e inner join subject s on e.subject_id = s.id inner join img i on e.img_id = i.id where s.id = ?",
                new Object[]{subject.getId()},
                rs -> {
                    var sourceImgId = rs.getObject("img_id", UUID.class);
                    var newImgId = UUID.randomUUID();
                    jdbcTemplate.update(
                            "insert into img(id, content) select ?, i.content from img i where i.id = ?",
                            newImgId, sourceImgId
                    );
                    sourceImgId2NewImgId.put(sourceImgId, newImgId);
                }
        );

        String sql = "select " +
                "   e.id as embedding_id, " +
                "   i.id as img_id " +
                " from " +
                "   embedding e left join img i on e.img_id = i.id " +
                "   inner join subject s on s.id = e.subject_id " +
                " where " +
                "   s.id = ?";

        jdbcTemplate.query(
                sql,
                new Object[]{subject.getId()},
                rc -> {
                    var sourceEmbeddingId = rc.getObject("embedding_id", UUID.class);
                    var sourceImgId = rc.getObject("img_id", UUID.class); // could be null (for demo embeddings)
                    jdbcTemplate.update(
                            "insert into embedding(id, subject_id, embedding, calculator, img_id) select ?, ?, e.embedding, e.calculator, ? from embedding e where e.id = ?",
                            UUID.randomUUID(), newSubjectId, sourceImgId2NewImgId.get(sourceImgId), sourceEmbeddingId
                    );
                }
        );
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
}