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

import com.exadel.frs.core.trainservice.dto.FacesRecognitionResponseDto;
import com.exadel.frs.core.trainservice.dto.ProcessImageParams;
import com.exadel.frs.core.trainservice.service.FaceProcessService;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Min;
import java.util.Collections;

import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static com.exadel.frs.core.trainservice.system.global.Constants.X_FRS_API_KEY_HEADER;

@RestController
@RequestMapping(API_V1)
@RequiredArgsConstructor
@Validated
public class RecognizeController {

    private final FaceProcessService recognitionService;

    @PostMapping(value = "/faces/recognize")
    public FacesRecognitionResponseDto recognize(
            @ApiParam(value = "Api key of application and model", required = true)
            @RequestHeader(X_FRS_API_KEY_HEADER) final String apiKey,
            @ApiParam(value = "Image for recognizing", required = true)
            @RequestParam final MultipartFile file,
            @ApiParam(value = "Maximum number of faces to be recognized")
            @RequestParam(defaultValue = "0", required = false)
            @Min(value = 0, message = "Limit should be equal or greater than 0") final Integer limit,
            @ApiParam(value = "Maximum number of predictions per faces")
            @RequestParam(defaultValue = "1", name = "prediction_count", required = false)
            @Min(value = 1, message = "prediction_count should be equal or greater than 1") final Integer predictionCount,
            @ApiParam(value = "The minimal percent confidence that found face is actually a face.")
            @RequestParam(value = "det_prob_threshold", required = false) final Double detProbThreshold,
            @ApiParam(value = "Comma-separated types of face plugins. Empty value - face plugins disabled, returns only bounding boxes")
            @RequestParam(value = "face_plugins", required = false, defaultValue = "") final String facePlugins
    ) {
        ProcessImageParams processImageParams = ProcessImageParams
                .builder()
                .apiKey(apiKey)
                .file(file)
                .limit(limit)
                .detProbThreshold(detProbThreshold)
                .facePlugins(facePlugins)
                .additionalParams(Collections.singletonMap("predictionCount", predictionCount))
                .build();
        return (FacesRecognitionResponseDto) recognitionService.processImage(processImageParams);
    }
}