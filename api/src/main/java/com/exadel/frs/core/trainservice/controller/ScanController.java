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

import static com.exadel.frs.core.trainservice.enums.RetrainOption.getTrainingOption;
import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static com.exadel.frs.core.trainservice.system.global.Constants.X_FRS_API_KEY_HEADER;

import com.exadel.frs.core.trainservice.aspect.WriteEndpoint;
import com.exadel.frs.core.trainservice.service.RetrainService;
import com.exadel.frs.core.trainservice.service.ScanService;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import io.swagger.annotations.ApiParam;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(API_V1)
@RequiredArgsConstructor
public class ScanController {

    private final ScanService scanService;
    private final RetrainService retrainService;
    private final ImageExtensionValidator imageValidator;

    @WriteEndpoint
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/faces/{face_name}")
    public void scanFaces(
            @ApiParam(value = "A picture with at least one face (accepted formats: jpeg, png).", required = true)
            @RequestParam
            final MultipartFile file,
            @ApiParam(value = "Person's name to whom the face belongs to.", required = true)
            @PathVariable("face_name")
            final String faceName,
            @ApiParam(value = "Specify whether the model should start retraining immediately after the request is completed " +
                    "(set this parameter to value \"no\", if operating with a lot of images one after another). " +
                    "Allowed values: \"yes\", \"no\", \"force\". \"Force\" option will abort already running processes of " +
                    "classifier training.")
            @RequestParam(value = "retrain", required = false, defaultValue = "force")
            final String retrainOption,
            @ApiParam(value = "The minimal percent confidence that found face is actually a face.")
            @RequestParam(value = "det_prob_threshold", required = false)
            final Double detProbThreshold,
            @ApiParam(value = "api key", required = true)
            @RequestHeader(X_FRS_API_KEY_HEADER)
            final String apiKey
    ) throws IOException {

        imageValidator.validate(file);
        scanService.scanAndSaveFace(file, faceName, detProbThreshold, apiKey);
        getTrainingOption(retrainOption).run(apiKey, retrainService);
    }
}