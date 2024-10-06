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

import static com.exadel.frs.commonservice.enums.ValidationResult.OK;
import com.exadel.frs.commonservice.enums.ModelType;
import com.exadel.frs.commonservice.repository.ModelRepository;
import com.exadel.frs.core.trainservice.dto.ModelValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional("tmPg")
public class ModelService {

    private final ModelRepository modelRepository;

    public ModelValidationResult validateModelKey(final String modelKey, ModelType type) {
        return modelRepository.findByApiKeyAndType(modelKey, type)
                              .map(model -> new ModelValidationResult(model.getId(), OK))
                              .orElseGet(ModelValidationResult::new);
    }
}
