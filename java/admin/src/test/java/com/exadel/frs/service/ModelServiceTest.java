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

import com.exadel.frs.commonservice.entity.App;
import com.exadel.frs.commonservice.entity.Model;
import com.exadel.frs.commonservice.projection.ModelProjection;
import com.exadel.frs.commonservice.entity.User;
import com.exadel.frs.commonservice.repository.ImgRepository;
import com.exadel.frs.commonservice.repository.ModelRepository;
import com.exadel.frs.commonservice.repository.ModelStatisticRepository;
import com.exadel.frs.commonservice.repository.SubjectRepository;
import com.exadel.frs.dto.ModelCloneDto;
import com.exadel.frs.dto.ModelCreateDto;
import com.exadel.frs.dto.ModelResponseDto;
import com.exadel.frs.dto.ModelUpdateDto;
import com.exadel.frs.exception.NameIsNotUniqueException;
import com.exadel.frs.mapper.MlModelMapper;
import com.exadel.frs.service.AppService;
import com.exadel.frs.service.ModelCloneService;
import com.exadel.frs.service.ModelService;
import com.exadel.frs.service.UserService;
import com.exadel.frs.system.security.AuthorizationManager;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static com.exadel.frs.commonservice.enums.ModelType.RECOGNITION;
import static java.time.LocalDateTime.now;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

class ModelServiceTest {

    private static final String MODEL_GUID = "model-guid";
    private static final String MODEL_API_KEY = "model-key";
    private static final String APPLICATION_GUID = "app-guid";
    private static final String APPLICATION_API_KEY = "app-key";

    private static final Long USER_ID = 1L;
    private static final Long MODEL_ID = 2L;
    private static final Long APPLICATION_ID = 3L;

    private AppService appServiceMock;
    private ModelRepository modelRepositoryMock;
    private ModelService modelService;
    private UserService userServiceMock;
    private ModelCloneService modelCloneService;
    private MlModelMapper modelMapper;
    private ImgRepository imgRepository;
    private final SubjectRepository subjectRepositry;
    private final ModelStatisticRepository statisticRepository;
    private final JdbcTemplate jdbcTemplate;

    private AuthorizationManager authManager;

    ModelServiceTest() {
        modelRepositoryMock = mock(ModelRepository.class);
        appServiceMock = mock(AppService.class);
        authManager = mock(AuthorizationManager.class);
        userServiceMock = mock(UserService.class);
        subjectRepositry = mock(SubjectRepository.class);
        jdbcTemplate = mock(JdbcTemplate.class);
        modelCloneService = mock(ModelCloneService.class);
        modelMapper = mock(MlModelMapper.class);
        imgRepository = mock(ImgRepository.class);
        statisticRepository = mock(ModelStatisticRepository.class);

        modelService = new ModelService(
                modelRepositoryMock,
                appServiceMock,
                authManager,
                userServiceMock,
                subjectRepositry,
                jdbcTemplate,
                modelCloneService,
                modelMapper,
                imgRepository,
                statisticRepository
        );
    }

    @Test
    void successGetModel() {
        val app = App.builder()
                .id(APPLICATION_ID)
                .build();

        val model = Model.builder()
                .id(MODEL_ID)
                .guid(MODEL_GUID)
                .app(app)
                .build();

        val user = User.builder()
                .id(USER_ID)
                .build();

        val modelDto = ModelResponseDto.builder()
                .id(MODEL_GUID)
                .build();

        when(modelRepositoryMock.findByGuid(MODEL_GUID)).thenReturn(Optional.of(model));
        when(userServiceMock.getUser(USER_ID)).thenReturn(user);
        when(modelMapper.toResponseDto(model, APPLICATION_GUID)).thenReturn(modelDto);

        val result = modelService.getModelDto(APPLICATION_GUID, MODEL_GUID, USER_ID);

        assertThat(result.getId(), is(MODEL_GUID));

        verify(authManager).verifyReadPrivilegesToApp(user, app);
        verify(authManager).verifyAppHasTheModel(APPLICATION_GUID, model);
        verifyNoMoreInteractions(authManager);
    }

    @Test
    void successGetModels() {
        val app = App.builder()
                .id(APPLICATION_ID)
                .build();

        val model = new ModelProjection(MODEL_GUID, "1REC", MODEL_API_KEY, RECOGNITION, now());

        val user = User.builder()
                .id(USER_ID)
                .build();

        val modelDto = ModelResponseDto.builder()
                 .id(MODEL_GUID)
                 .build();

        when(modelRepositoryMock.findAllByAppId(anyLong())).thenReturn(List.of(model));
        when(appServiceMock.getApp(APPLICATION_GUID)).thenReturn(app);
        when(userServiceMock.getUser(USER_ID)).thenReturn(user);
        when(modelMapper.toResponseDto(model, MODEL_API_KEY)).thenReturn(modelDto);

        val result = modelService.getModels(APPLICATION_GUID, USER_ID);

        assertThat(result.size(), is(1));

        verify(authManager).verifyReadPrivilegesToApp(user, app);
        verifyNoMoreInteractions(authManager);
    }

    @Test
    void successCreateModel() {
        val modelCreateDto = ModelCreateDto.builder()
                .name("model-name")
                .type("RECOGNITION")
                .build();

        val app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .build();

        val user = User.builder()
                .id(USER_ID)
                .build();

        when(appServiceMock.getApp(APPLICATION_GUID)).thenReturn(app);
        when(userServiceMock.getUser(USER_ID)).thenReturn(user);

        modelService.createRecognitionModel(modelCreateDto, APPLICATION_GUID, USER_ID);

        val varArgs = ArgumentCaptor.forClass(Model.class);
        verify(modelRepositoryMock).existsByUniqueNameAndAppId("model-name", APPLICATION_ID);
        verify(modelRepositoryMock).save(varArgs.capture());
        verify(authManager).verifyWritePrivilegesToApp(user, app);
        verifyNoMoreInteractions(modelRepositoryMock, authManager);

        assertThat(varArgs.getValue().getName(), is(modelCreateDto.getName()));
        assertThat(varArgs.getValue().getGuid(), not(emptyOrNullString()));
    }

    @Test
    void failCreateModelNameIsNotUnique() {
        val modelCreateDto = ModelCreateDto.builder()
                .name("model-name")
                .build();

        val app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .build();

        when(appServiceMock.getApp(anyString())).thenReturn(app);
        when(modelRepositoryMock.existsByUniqueNameAndAppId(anyString(), anyLong())).thenReturn(true);

        assertThatThrownBy(() ->
                modelService.createRecognitionModel(modelCreateDto, APPLICATION_GUID, USER_ID)
        ).isInstanceOf(NameIsNotUniqueException.class);
    }

    @Test
    void successCloneModel() {
        val user = User.builder()
                .id(USER_ID)
                .build();

        val modelCloneDto = ModelCloneDto.builder()
                .name("name_of_clone")
                .build();

        val app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .build();

        val repoModel = Model.builder()
                .id(MODEL_ID)
                .name("name")
                .guid(MODEL_GUID)
                .app(app)
                .build();

        val cloneModel = Model.builder()
                .id(new Random().nextLong())
                .name("name_of_clone")
                .apiKey(randomUUID().toString())
                .guid(randomUUID().toString())
                .app(app)
                .build();

        when(modelRepositoryMock.findByGuid(MODEL_GUID)).thenReturn(Optional.of(repoModel));
        when(appServiceMock.getApp(APPLICATION_GUID)).thenReturn(app);
        when(modelCloneService.cloneModel(any(Model.class), any(ModelCloneDto.class))).thenReturn(cloneModel);
        when(userServiceMock.getUser(USER_ID)).thenReturn(user);

        val clonedModel = modelService.cloneModel(modelCloneDto, APPLICATION_GUID, MODEL_GUID, USER_ID);

        verify(modelRepositoryMock).findByGuid(MODEL_GUID);
        verify(modelRepositoryMock).existsByUniqueNameAndAppId("name_of_clone", APPLICATION_ID);
        verify(modelCloneService).cloneModel(any(Model.class), any(ModelCloneDto.class));
        verify(authManager).verifyAppHasTheModel(APPLICATION_GUID, repoModel);
        verify(authManager).verifyWritePrivilegesToApp(user, app);

        assertThat(clonedModel.getId(), not(repoModel.getId()));
        assertThat(clonedModel.getName(), is(modelCloneDto.getName()));
    }

    @Test
    void failCloneModelNameIsNotUnique() {
        val modelCloneDto = ModelCloneDto.builder()
                .name("new_name")
                .build();

        val app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .build();

        val repoModel = Model.builder()
                .id(MODEL_ID)
                .name("name")
                .guid(MODEL_GUID)
                .app(app)
                .build();

        when(modelRepositoryMock.findByGuid(anyString())).thenReturn(Optional.of(repoModel));
        when(appServiceMock.getApp(anyString())).thenReturn(app);
        when(modelRepositoryMock.existsByUniqueNameAndAppId(anyString(), anyLong())).thenReturn(true);

        assertThatThrownBy(() ->
                modelService.cloneModel(modelCloneDto, APPLICATION_GUID, MODEL_GUID, USER_ID)
        ).isInstanceOf(NameIsNotUniqueException.class);
    }

    @Test
    void successUpdateModel() {
        ModelUpdateDto modelUpdateDto = ModelUpdateDto.builder()
                .name("new_name")
                .build();

        val app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .build();

        val repoModel = Model.builder()
                .id(MODEL_ID)
                .name("name")
                .guid(MODEL_GUID)
                .app(app)
                .build();

        val user = User.builder()
                .id(USER_ID)
                .build();

        when(modelRepositoryMock.findByGuid(MODEL_GUID)).thenReturn(Optional.of(repoModel));
        when(appServiceMock.getApp(APPLICATION_GUID)).thenReturn(app);
        when(userServiceMock.getUser(USER_ID)).thenReturn(user);

        modelService.updateModel(modelUpdateDto, APPLICATION_GUID, MODEL_GUID, USER_ID);

        verify(modelRepositoryMock).findByGuid(MODEL_GUID);
        verify(modelRepositoryMock).existsByUniqueNameAndAppId("new_name", APPLICATION_ID);
        verify(modelRepositoryMock).save(any(Model.class));
        verify(authManager).verifyReadPrivilegesToApp(user, app);
        verify(authManager).verifyAppHasTheModel(APPLICATION_GUID, repoModel);
        verify(authManager).verifyWritePrivilegesToApp(user, app);
        verifyNoMoreInteractions(modelRepositoryMock, authManager);

        assertThat(repoModel.getName(), is(modelUpdateDto.getName()));
    }

    @Test
    void failUpdateModelNameIsNotUnique() {
        val modelUpdateDto = ModelUpdateDto.builder()
                .name("new_name")
                .build();

        val app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .build();

        val repoModel = Model.builder()
                .id(MODEL_ID)
                .name("name")
                .guid(MODEL_GUID)
                .app(app)
                .build();

        when(modelRepositoryMock.findByGuid(anyString())).thenReturn(Optional.of(repoModel));
        when(appServiceMock.getApp(anyString())).thenReturn(app);
        when(modelRepositoryMock.existsByUniqueNameAndAppId(anyString(), anyLong())).thenReturn(true);

        assertThatThrownBy(() ->
                modelService.updateModel(modelUpdateDto, APPLICATION_GUID, MODEL_GUID, USER_ID)
        ).isInstanceOf(NameIsNotUniqueException.class);
    }

    @Test
    void successRegenerateApiKey() {
        val app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .apiKey(APPLICATION_API_KEY)
                .build();

        val model = Model.builder()
                .id(MODEL_ID)
                .guid(MODEL_GUID)
                .apiKey(MODEL_API_KEY)
                .app(app)
                .build();

        val user = User.builder()
                .id(USER_ID)
                .build();

        when(modelRepositoryMock.findByGuid(MODEL_GUID)).thenReturn(Optional.of(model));
        when(userServiceMock.getUser(USER_ID)).thenReturn(user);

        modelService.regenerateApiKey(APPLICATION_GUID, MODEL_GUID, USER_ID);

        verify(modelRepositoryMock).findByGuid(MODEL_GUID);
        verify(modelRepositoryMock).save(any());
        verify(authManager).verifyReadPrivilegesToApp(user, app);
        verify(authManager).verifyAppHasTheModel(APPLICATION_GUID, model);
        verify(authManager).verifyWritePrivilegesToApp(user, app);
        verifyNoMoreInteractions(modelRepositoryMock, authManager);
    }

    @Test
    void successDeleteModel() {
        val appKey = "app_key";
        val modelKey = "model_key";

        val app = App.builder()
                .id(APPLICATION_ID)
                .guid(APPLICATION_GUID)
                .apiKey(appKey)
                .build();

        val model = Model.builder()
                .id(MODEL_ID)
                .guid(MODEL_GUID)
                .apiKey(modelKey)
                .app(app)
                .build();

        val user = User.builder()
                .id(USER_ID)
                .build();

        when(modelRepositoryMock.findByGuid(MODEL_GUID)).thenReturn(Optional.of(model));
        when(userServiceMock.getUser(USER_ID)).thenReturn(user);

        modelService.deleteModel(APPLICATION_GUID, MODEL_GUID, USER_ID);

        verify(modelRepositoryMock).findByGuid(anyString());
        verify(modelRepositoryMock).deleteById(anyLong());
        verify(authManager).verifyReadPrivilegesToApp(user, app);
        verify(authManager).verifyAppHasTheModel(APPLICATION_GUID, model);
        verify(authManager).verifyWritePrivilegesToApp(user, app);
        verifyNoMoreInteractions(modelRepositoryMock, authManager);
    }
}