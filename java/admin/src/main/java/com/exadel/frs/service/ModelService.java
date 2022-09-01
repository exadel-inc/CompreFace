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

import static java.time.LocalDateTime.now;
import static java.time.ZoneOffset.UTC;
import static java.util.UUID.randomUUID;
import com.exadel.frs.commonservice.annotation.CollectStatistics;
import com.exadel.frs.commonservice.entity.App;
import com.exadel.frs.commonservice.entity.Model;
import com.exadel.frs.commonservice.entity.ModelStatisticProjection;
import com.exadel.frs.commonservice.entity.Subject;
import com.exadel.frs.commonservice.entity.User;
import com.exadel.frs.commonservice.enums.ModelType;
import com.exadel.frs.commonservice.enums.StatisticsType;
import com.exadel.frs.commonservice.exception.ModelNotFoundException;
import com.exadel.frs.commonservice.repository.ImgRepository;
import com.exadel.frs.commonservice.repository.ModelRepository;
import com.exadel.frs.commonservice.repository.ModelStatisticRepository;
import com.exadel.frs.commonservice.repository.SubjectRepository;
import com.exadel.frs.dto.ui.ModelCloneDto;
import com.exadel.frs.dto.ui.ModelCreateDto;
import com.exadel.frs.dto.ui.ModelResponseDto;
import com.exadel.frs.dto.ui.ModelUpdateDto;
import com.exadel.frs.exception.NameIsNotUniqueException;
import com.exadel.frs.mapper.MlModelMapper;
import com.exadel.frs.system.security.AuthorizationManager;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModelService {

    @Value("${statistic.model.months}")
    private int statisticMonths;

    private final ModelRepository modelRepository;
    private final AppService appService;
    private final AuthorizationManager authManager;
    private final UserService userService;
    private final SubjectRepository subjectRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ModelCloneService modelCloneService;
    private final MlModelMapper modelMapper;
    private final ImgRepository imgRepository;
    private final ModelStatisticRepository statisticRepository;

    public Model getModel(final String modelGuid) {
        return modelRepository.findByGuid(modelGuid)
                              .orElseThrow(() -> new ModelNotFoundException(modelGuid, ""));
    }

    private void validateName(final String newName, final String oldName, final Long appId) {
        if (oldName.equals(newName)) {
            throw new NameIsNotUniqueException(newName);
        }
        boolean hasNewNameEntryInDb = modelRepository.existsByUniqueNameAndAppId(newName, appId);
        if (hasNewNameEntryInDb) {
            boolean hasNewNameMoreThanOneEntryInDb = modelRepository.countByUniqueNameAndAppId(newName, appId) > 1;
            boolean isNotEqualsIgnoreCase = !oldName.equalsIgnoreCase(newName);
            if (hasNewNameMoreThanOneEntryInDb || isNotEqualsIgnoreCase) {
                throw new NameIsNotUniqueException(newName);
            }
        }
    }

    private void verifyNameIsUnique(final String name, final Long appId) {
        if (modelRepository.existsByUniqueNameAndAppId(name, appId)) {
            throw new NameIsNotUniqueException(name);
        }
    }

    private Model getModel(final String appGuid, final String modelGuid, final Long userId) {
        val model = getModel(modelGuid);
        val user = userService.getUser(userId);

        authManager.verifyReadPrivilegesToApp(user, model.getApp());
        authManager.verifyAppHasTheModel(appGuid, model);

        return model;
    }

    public ModelResponseDto getModelDto(final String appGuid, final String modelGuid, final Long userId) {
        Model model = getModel(appGuid, modelGuid, userId);
        return getModelResponseDto(appGuid, model);
    }

    private ModelResponseDto getModelResponseDto(String appGuid, Model model) {
        String apiKey = model.getApiKey();
        Long subjectCount = subjectRepository.countAllByApiKey(apiKey);
        Long imageCount = imgRepository.getImageCountByApiKey(apiKey);

        ModelResponseDto modelResponseDto = modelMapper.toResponseDto(model, appGuid);
        modelResponseDto.setSubjectCount(subjectCount);
        modelResponseDto.setImageCount(imageCount);
        return modelResponseDto;
    }

    public List<ModelResponseDto> getModels(final String appGuid, final Long userId) {
        val app = appService.getApp(appGuid);
        val user = userService.getUser(userId);

        authManager.verifyReadPrivilegesToApp(user, app);

        return modelRepository.findAllByAppId(app.getId())
                              .stream()
                              .map(model -> getModelResponseDto(model.getApiKey(), model)).collect(Collectors.toList());
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
                    .createdDate(now())
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

        val clonedModel = modelCloneService.cloneModel(model, modelCloneDto);

        // caution: time consuming operation
        cloneSubjects(model.getApiKey(), clonedModel.getApiKey());

        return clonedModel;
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
                            UUID.randomUUID(),
                            newSubjectId,
                            sourceImgId2NewImgId.get(sourceImgId),
                            sourceEmbeddingId
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

        validateName(modelUpdateDto.getName(), model.getName(), model.getApp().getId());
        model.setName(modelUpdateDto.getName());

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

    public List<ModelStatisticProjection> getSummarizedByDayModelStatistics(final String appGuid, final String guid, final Long userId) {
        val model = getModel(guid);
        val user = userService.getUser(userId);

        authManager.verifyReadPrivilegesToApp(user, model.getApp());
        authManager.verifyAppHasTheModel(appGuid, model);

        val now = LocalDate.now(UTC);
        val endDate = Date.from(now.atStartOfDay(UTC).toInstant());
        val startDate = Date.from(now.minusMonths(statisticMonths).atStartOfDay(UTC).toInstant());

        return statisticRepository.findAllSummarizedByDay(guid, startDate, endDate);
    }
}
