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

import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static com.exadel.frs.core.trainservice.system.global.Constants.NEW_MODEL_KEY_HEADER;
import static com.exadel.frs.core.trainservice.system.global.Constants.X_FRS_API_KEY_HEADER;
import com.exadel.frs.core.trainservice.service.ModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(API_V1 + "/models")
@RequiredArgsConstructor
public class ModelController {

    private final ModelService modelService;

    @PutMapping("/api-key")
    public void updateModelApiKeyForFaces(
            @RequestHeader(name = X_FRS_API_KEY_HEADER)
            final String apiKey,
            @RequestParam(name = NEW_MODEL_KEY_HEADER)
            final String newModelApiKey
    ) {
        modelService.updateModelApiKeyForFaces(apiKey, newModelApiKey);
    }
}