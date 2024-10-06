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

import static com.exadel.frs.commonservice.system.global.Constants.DET_PROB_THRESHOLD;
import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static com.exadel.frs.core.trainservice.system.global.Constants.DET_PROB_THRESHOLD_DESC;
import static com.exadel.frs.core.trainservice.system.global.Constants.FACE_PLUGINS;
import static com.exadel.frs.core.trainservice.system.global.Constants.FACE_PLUGINS_DESC;
import static com.exadel.frs.core.trainservice.system.global.Constants.IMAGE_FILE_DESC;
import static com.exadel.frs.core.trainservice.system.global.Constants.LIMIT_DEFAULT_VALUE;
import static com.exadel.frs.core.trainservice.system.global.Constants.LIMIT_DESC;
import static com.exadel.frs.core.trainservice.system.global.Constants.LIMIT_MIN_DESC;
import static com.exadel.frs.core.trainservice.system.global.Constants.NUMBER_VALUE_EXAMPLE;
import static com.exadel.frs.core.trainservice.system.global.Constants.STATUS;
import static com.exadel.frs.core.trainservice.system.global.Constants.STATUS_DEFAULT_VALUE;
import static com.exadel.frs.core.trainservice.system.global.Constants.STATUS_DESC;
import static com.exadel.frs.core.trainservice.system.global.Constants.X_FRS_API_KEY_HEADER;
import com.exadel.frs.core.trainservice.dto.Base64File;
import com.exadel.frs.core.trainservice.dto.FacesDetectionResponseDto;
import com.exadel.frs.core.trainservice.dto.ProcessImageParams;
import com.exadel.frs.core.trainservice.service.FaceProcessService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiParam;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(API_V1)
@RequiredArgsConstructor
@Validated
public class DetectionController {

    private final FaceProcessService detectionService;

    @PostMapping(value = "/detection/detect", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = X_FRS_API_KEY_HEADER,
                    dataTypeClass = String.class,
                    paramType = "header",
                    value = "Api key of application and model",
                    required = true)
    })
    public FacesDetectionResponseDto detect(
            @ApiParam(value = IMAGE_FILE_DESC, required = true)
            @RequestParam
            final MultipartFile file,
            @ApiParam(value = LIMIT_DESC, example = NUMBER_VALUE_EXAMPLE)
            @RequestParam(defaultValue = LIMIT_DEFAULT_VALUE, required = false)
            @Min(value = 0, message = LIMIT_MIN_DESC)
            final Integer limit,
            @ApiParam(value = DET_PROB_THRESHOLD_DESC, example = NUMBER_VALUE_EXAMPLE)
            @RequestParam(value = DET_PROB_THRESHOLD, required = false)
            final Double detProbThreshold,
            @ApiParam(value = FACE_PLUGINS_DESC)
            @RequestParam(value = FACE_PLUGINS, required = false, defaultValue = "")
            final String facePlugins,
            @ApiParam(value = STATUS_DESC)
            @RequestParam(value = STATUS, required = false, defaultValue = STATUS_DEFAULT_VALUE)
            final Boolean status
    ) {
        var processImageParams = ProcessImageParams
                .builder()
                .file(file)
                .limit(limit)
                .detProbThreshold(detProbThreshold)
                .facePlugins(facePlugins)
                .status(status)
                .build();

        return (FacesDetectionResponseDto) detectionService.processImage(processImageParams);
    }

    @PostMapping(value = "/detection/detect", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = X_FRS_API_KEY_HEADER,
                    dataTypeClass = String.class,
                    paramType = "header",
                    value = "Api key of application and model",
                    required = true)
    })
    public FacesDetectionResponseDto detectBase64(
            @ApiParam(value = LIMIT_DESC, example = NUMBER_VALUE_EXAMPLE)
            @RequestParam(defaultValue = LIMIT_DEFAULT_VALUE, required = false)
            @Min(value = 0, message = LIMIT_MIN_DESC)
            final Integer limit,
            @ApiParam(value = DET_PROB_THRESHOLD_DESC, example = NUMBER_VALUE_EXAMPLE)
            @RequestParam(value = DET_PROB_THRESHOLD, required = false)
            final Double detProbThreshold,
            @ApiParam(value = FACE_PLUGINS_DESC)
            @RequestParam(value = FACE_PLUGINS, required = false, defaultValue = "")
            final String facePlugins,
            @ApiParam(value = STATUS_DESC)
            @RequestParam(value = STATUS, required = false, defaultValue = STATUS_DEFAULT_VALUE)
            final Boolean status,
            @Valid
            @RequestBody
            final Base64File request
    ) {
        var processImageParams = ProcessImageParams
                .builder()
                .imageBase64(request.getContent())
                .limit(limit)
                .detProbThreshold(detProbThreshold)
                .facePlugins(facePlugins)
                .status(status)
                .build();

        return (FacesDetectionResponseDto) detectionService.processImage(processImageParams);
    }
}
