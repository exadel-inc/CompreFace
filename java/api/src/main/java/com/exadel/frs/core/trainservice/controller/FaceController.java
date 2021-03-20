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

import com.exadel.frs.core.trainservice.aspect.WriteEndpoint;
import com.exadel.frs.core.trainservice.dto.FaceResponseDto;
import com.exadel.frs.core.trainservice.dto.FaceVerification;
import com.exadel.frs.core.trainservice.dto.FacesRecognitionResponseDto;
import com.exadel.frs.core.trainservice.dto.ProcessImageParams;
import com.exadel.frs.core.trainservice.service.FaceProcessService;
import com.exadel.frs.core.trainservice.service.FaceRecognizeProcessServiceImpl;
import com.exadel.frs.core.trainservice.service.FaceService;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Min;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static com.exadel.frs.core.trainservice.system.global.Constants.X_FRS_API_KEY_HEADER;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping(API_V1 + "/recognition/faces")
@RequiredArgsConstructor
public class FaceController {

    private final FaceService faceService;
    private final ImageExtensionValidator imageValidator;

    @WriteEndpoint
    @ResponseStatus(CREATED)
    @PostMapping
    public FaceResponseDto addFaces(
            @ApiParam(value = "A picture with at least one face (accepted formats: jpeg, png).", required = true)
            @RequestParam final MultipartFile file,
            @ApiParam(value = "Person's name to whom the face belongs to.", required = true)
            @RequestParam("subject") final String faceName,
            @ApiParam(value = "The minimal percent confidence that found face is actually a face.")
            @RequestParam(value = "det_prob_threshold", required = false) final Double detProbThreshold,
            @ApiParam(value = "api key", required = true)
            @RequestHeader(X_FRS_API_KEY_HEADER) final String apiKey
    ) throws IOException {
        imageValidator.validate(file);
        return faceService.findAndSaveFace(file, faceName, detProbThreshold, apiKey);
    }

    @GetMapping
    public Map<String, List<FaceResponseDto>> findFacesByModel(
            @ApiParam(value = "api key", required = true)
            @RequestHeader(name = X_FRS_API_KEY_HEADER) final String apiKey
    ) {
        return Map.of("faces", faceService.findFaces(apiKey));
    }

    @WriteEndpoint
    @DeleteMapping
    public List<FaceResponseDto> deleteFaces(
            @ApiParam(value = "Person's name to whom the face belongs to.", required = true)
            @RequestParam(name = "subject", required = false) final String subject,
            @ApiParam(value = "api key", required = true)
            @RequestHeader(name = X_FRS_API_KEY_HEADER) final String apiKey
    ) {
        val faces = new ArrayList<FaceResponseDto>();
        if (isBlank(subject)) {
            faceService.deleteFacesByModel(apiKey);
        } else {
            faces.addAll(faceService.deleteFaceByName(subject, apiKey));
        }

        return faces;
    }

    @WriteEndpoint
    @DeleteMapping("/{image_id}")
    public FaceResponseDto deleteFaceById(
            @PathVariable final String image_id,
            @ApiParam(value = "api key", required = true)
            @RequestHeader(name = X_FRS_API_KEY_HEADER) final String apiKey
    ) {
        return faceService.deleteFaceById(image_id, apiKey);
    }

    @PostMapping(value = "/{image_id}/verify")
    public Map<String, List<FaceVerification>> recognize(
            @ApiParam(value = "Api key of application and model", required = true)
            @RequestHeader(X_FRS_API_KEY_HEADER) final String apiKey,
            @ApiParam(value = "A picture with one face (accepted formats: jpeg, png).", required = true)
            @RequestParam final MultipartFile file,
            @ApiParam(value = "Maximum number of faces to be verified")
            @RequestParam(defaultValue = "0", required = false)
            @Min(value = 0, message = "Limit should be equal or greater than 0") final Integer limit,
            @ApiParam(value = "Image Id from collection to compare with face.", required = true)
            @PathVariable final String image_id,
            @ApiParam(value = "The minimal percent confidence that found face is actually a face.")
            @RequestParam(value = "det_prob_threshold", required = false) final Double detProbThreshold,
            @ApiParam(value = "Comma-separated types of face plugins. Empty value - face plugins disabled, returns only bounding boxes")
            @RequestParam(value = "face_plugins", required = false, defaultValue = "") final String facePlugins,
            @ApiParam(value = "Special parameter to show execution_time and plugin_version fields. Empty value - both fields eliminated, true - both fields included")
            @RequestParam(value = "status", required = false, defaultValue = "false") final Boolean status
    ) {
        imageValidator.validate(file);
        ProcessImageParams processImageParams = ProcessImageParams.builder()
                .additionalParams(Map.of("image_id", image_id))
                .apiKey(apiKey)
                .detProbThreshold(detProbThreshold)
                .file(file)
                .facePlugins(facePlugins)
                .limit(limit)
                .status(status)
                .build();
        return faceService.verifyFace(processImageParams);
    }
}