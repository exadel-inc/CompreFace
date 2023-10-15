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

package com.exadel.frs.core.trainservice.controller;

import com.exadel.frs.commonservice.enums.ValidationResult;
import com.exadel.frs.core.trainservice.EmbeddedPostgreSQLTest;
import com.exadel.frs.core.trainservice.component.migration.MigrationComponent;
import com.exadel.frs.core.trainservice.component.migration.MigrationStatusStorage;
import com.exadel.frs.core.trainservice.config.IntegrationTest;
import com.exadel.frs.core.trainservice.dto.ModelValidationResult;
import com.exadel.frs.core.trainservice.service.ModelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.exadel.frs.commonservice.enums.ValidationResult.OK;
import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
@AutoConfigureMockMvc
class MigrateControllerTest extends EmbeddedPostgreSQLTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MigrationStatusStorage migrationStatusStorage;

    @MockBean
    private MigrationComponent migrationComponent;

    @MockBean
    private ModelService modelService;

    @Test
    void migrate() throws Exception {
        var validationResult = new ModelValidationResult(1L, OK);

        when(modelService.validateModelKey(anyString(), any())).thenReturn(validationResult);

        mockMvc.perform(post(API_V1 + "/migrate"))
                .andExpect(status().isOk())
                .andExpect(content().string("Migration started"));

        verify(migrationStatusStorage).startMigration();
        verify(migrationComponent).migrate();
        verifyNoMoreInteractions(migrationStatusStorage);
        verifyNoMoreInteractions(migrationComponent);
    }
}