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

import com.exadel.frs.core.trainservice.dto.FacesDetectionResponseDto;
import com.exadel.frs.core.trainservice.dto.ProcessImageParams;
import com.exadel.frs.core.trainservice.service.FaceProcessService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Min;

import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static com.exadel.frs.core.trainservice.system.global.Constants.X_FRS_API_KEY_HEADER;

@RestController
@RequestMapping(API_V1)
@RequiredArgsConstructor
@Validated
public class DetectionController {

    private final FaceProcessService detectionService;

    @PostMapping(value = "/detection")
    @ResponseStatus(HttpStatus.OK)
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = X_FRS_API_KEY_HEADER,
                    dataTypeClass = String.class,
                    paramType = "header",
                    value = "Api key of application and model",
                    required = true)
    })
    public FacesDetectionResponseDto detect(
            @ApiParam(value = "Image for recognizing", required = true)
            @RequestParam final MultipartFile file,
            @ApiParam(value = "Maximum number of faces to be recognized")
            @RequestParam(defaultValue = "0", required = false)
            @Min(value = 0, message = "Limit should be equal or greater than 0") final Integer limit,
            @ApiParam(value = "The minimal percent confidence that found face is actually a face.")
            @RequestParam(value = "det_prob_threshold", required = false) final Double detProbThreshold,
            @ApiParam(value = "Comma-separated types of face plugins. Empty value - face plugins disabled, returns only bounding boxes")
            @RequestParam(value = "face_plugins", required = false) final String facePlugins,
            @RequestParam(value = "status", required = false, defaultValue = "false") final Boolean status
    ) {
        ProcessImageParams processImageParams = ProcessImageParams
                .builder()
                .file(file)
                .limit(limit)
                .detProbThreshold(detProbThreshold)
                .facePlugins(facePlugins)
                .status(status)
                .build();
        return (FacesDetectionResponseDto)detectionService.processImage(processImageParams);
    }
}