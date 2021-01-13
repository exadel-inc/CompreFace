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

package com.exadel.frs.core.trainservice.service;

import static com.exadel.frs.core.trainservice.enums.ValidationResult.FORBIDDEN;
import static com.exadel.frs.core.trainservice.enums.ValidationResult.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import com.exadel.frs.core.trainservice.entity.Model;
import com.exadel.frs.core.trainservice.enums.ModelType;
import com.exadel.frs.core.trainservice.repository.ModelRepository;
import java.util.Optional;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class ModelServiceTest {

    @Mock
    private ModelRepository modelRepository;

    @InjectMocks
    private ModelService modelService;

    private static final String MODEL_KEY = "model_key";
    private static final ModelType MODEL_TYPE = ModelType.RECOGNITION;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void validateModelKeyOkValidationResult() {
        when(modelRepository.findByApiKeyAndType(MODEL_KEY, MODEL_TYPE)).thenReturn(Optional.of(new Model()));

        val actual = modelService.validateModelKey(MODEL_KEY, MODEL_TYPE);

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(OK);

        verify(modelRepository).findByApiKeyAndType(MODEL_KEY, MODEL_TYPE);
        verifyNoMoreInteractions(modelRepository);
    }

    @Test
    void validateModelKeyForbiddenValidationResult() {
        when(modelRepository.findByApiKeyAndType(MODEL_KEY, MODEL_TYPE)).thenReturn(Optional.empty());

        val actual = modelService.validateModelKey(MODEL_KEY, MODEL_TYPE);

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(FORBIDDEN);

        verify(modelRepository).findByApiKeyAndType(MODEL_KEY, MODEL_TYPE);
        verifyNoMoreInteractions(modelRepository);
    }
}