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

package com.exadel.frs;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import com.exadel.frs.dto.ui.ModelCreateDto;
import com.exadel.frs.dto.ui.ModelUpdateDto;
import com.exadel.frs.entity.App;
import com.exadel.frs.entity.Model;
import com.exadel.frs.entity.User;
import com.exadel.frs.enums.AppModelAccess;
import com.exadel.frs.exception.NameIsNotUniqueException;
import com.exadel.frs.repository.AppModelRepository;
import com.exadel.frs.repository.ModelRepository;
import com.exadel.frs.repository.ModelShareRequestRepository;
import com.exadel.frs.service.AppService;
import com.exadel.frs.service.ModelService;
import com.exadel.frs.service.UserService;
import com.exadel.frs.system.security.AuthorizationManager;
import java.util.List;
import java.util.Optional;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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
    private ModelShareRequestRepository modelShareRequestRepository;
    private AppModelRepository appModelRepository;
    private UserService userServiceMock;

    private AuthorizationManager authManager;

    ModelServiceTest() {
        modelRepositoryMock = mock(ModelRepository.class);
        appServiceMock = mock(AppService.class);
        modelShareRequestRepository = mock(ModelShareRequestRepository.class);
        appModelRepository = mock(AppModelRepository.class);
        authManager = mock(AuthorizationManager.class);
        userServiceMock = mock(UserService.class);
        modelService = new ModelService(
                modelRepositoryMock,
                appServiceMock,
                modelShareRequestRepository,
                appModelRepository,
                authManager,
                userServiceMock
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

        when(modelRepositoryMock.findByGuid(MODEL_GUID)).thenReturn(Optional.of(model));
        when(userServiceMock.getUser(USER_ID)).thenReturn(user);

        val result = modelService.getModel(APPLICATION_GUID, MODEL_GUID, USER_ID);

        assertThat(result.getGuid(), is(MODEL_GUID));
        assertThat(result.getId(), is(MODEL_ID));

        verify(authManager).verifyReadPrivilegesToApp(user, app);
        verify(authManager).verifyAppHasTheModel(APPLICATION_GUID, model);
        verifyNoMoreInteractions(authManager);
    }

    @Test
    void successGetModels() {
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

        when(modelRepositoryMock.findAllByAppId(anyLong())).thenReturn(List.of(model));
        when(appServiceMock.getApp(APPLICATION_GUID)).thenReturn(app);
        when(userServiceMock.getUser(USER_ID)).thenReturn(user);

        val result = modelService.getModels(APPLICATION_GUID, USER_ID);

        assertThat(result.size(), is(1));

        verify(authManager).verifyReadPrivilegesToApp(user, app);
        verifyNoMoreInteractions(authManager);
    }

    @Test
    void successCreateModel() {
        val modelCreateDto = ModelCreateDto.builder()
                                           .name("model-name")
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

        modelService.createModel(modelCreateDto, APPLICATION_GUID, USER_ID);

        val varArgs = ArgumentCaptor.forClass(Model.class);
        verify(modelRepositoryMock).existsByNameAndAppId("model-name", APPLICATION_ID);
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
        when(modelRepositoryMock.existsByNameAndAppId(anyString(), anyLong())).thenReturn(true);

        assertThatThrownBy(() ->
                modelService.createModel(modelCreateDto, APPLICATION_GUID, USER_ID)
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

        repoModel.addAppModelAccess(app, AppModelAccess.READONLY);

        when(modelRepositoryMock.findByGuid(MODEL_GUID)).thenReturn(Optional.of(repoModel));
        when(appServiceMock.getApp(APPLICATION_GUID)).thenReturn(app);
        when(userServiceMock.getUser(USER_ID)).thenReturn(user);

        modelService.updateModel(modelUpdateDto, APPLICATION_GUID, MODEL_GUID, USER_ID);

        verify(modelRepositoryMock).findByGuid(MODEL_GUID);
        verify(modelRepositoryMock).existsByNameAndAppId("new_name", APPLICATION_ID);
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
        when(modelRepositoryMock.existsByNameAndAppId(anyString(), anyLong())).thenReturn(true);

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