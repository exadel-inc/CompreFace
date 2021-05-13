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
import com.exadel.frs.core.trainservice.dto.Base64File;
import com.exadel.frs.core.trainservice.dto.FaceResponseDto;
import com.exadel.frs.core.trainservice.dto.FaceVerification;
import com.exadel.frs.core.trainservice.dto.ProcessImageParams;
import com.exadel.frs.core.trainservice.service.FaceService;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.exadel.frs.commonservice.system.global.Constants.DET_PROB_THRESHOLD;
import static com.exadel.frs.core.trainservice.system.global.Constants.*;
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
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FaceResponseDto addFaces(
            @ApiParam(value = API_KEY_DESC, required = true) @RequestHeader(X_FRS_API_KEY_HEADER) final String apiKey,
            @ApiParam(value = IMAGE_WITH_ONE_FACE_DESC, required = true) @RequestParam final MultipartFile file,
            @ApiParam(value = SUBJECT_DESC, required = true) @RequestParam(SUBJECT) final String subject,
            @ApiParam(value = DET_PROB_THRESHOLD_DESC) @RequestParam(value = DET_PROB_THRESHOLD, required = false) final Double detProbThreshold
    ) throws IOException {
        imageValidator.validate(file);

        return faceService.findAndSaveFace(
                file,
                subject,
                detProbThreshold,
                apiKey
        );
    }

    @WriteEndpoint
    @ResponseStatus(CREATED)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public FaceResponseDto addFacesBase64(
            @ApiParam(value = API_KEY_DESC, required = true) @RequestHeader(X_FRS_API_KEY_HEADER) final String apiKey,
            @ApiParam(value = SUBJECT_DESC) @RequestParam(value = SUBJECT) String subject,
            @ApiParam(value = DET_PROB_THRESHOLD_DESC) @RequestParam(value = DET_PROB_THRESHOLD, required = false) final Double detProbThreshold,
            @Valid @RequestBody Base64File request) throws IOException {

        imageValidator.validateBase64(request.getContent());

        return faceService.findAndSaveFace(
                request.getContent(),
                subject,
                detProbThreshold,
                apiKey
        );
    }

    @GetMapping
    public Map<String, List<FaceResponseDto>> findFacesByModel(
            @ApiParam(value = API_KEY_DESC, required = true)
            @RequestHeader(name = X_FRS_API_KEY_HEADER) final String apiKey
    ) {
        return Map.of(SUBJECTS, faceService.findFaces(apiKey));
    }

    @WriteEndpoint
    @DeleteMapping
    public List<FaceResponseDto> deleteFaces(
            @ApiParam(value = SUBJECT_DESC, required = true)
            @RequestParam(name = SUBJECT, required = false) final String subject,
            @ApiParam(value = API_KEY_DESC, required = true)
            @RequestHeader(name = X_FRS_API_KEY_HEADER) final String apiKey
    ) {
        if (isBlank(subject)) {
            faceService.deleteFacesByModel(apiKey);
            return Collections.emptyList();
        } else {
            return faceService.deleteFaceByName(subject, apiKey);
        }
    }

    @WriteEndpoint
    @PutMapping
    public Map<String, Object> updateSubject(
            @ApiParam(value = SUBJECT_DESC, required = true)
            @RequestParam(name = SUBJECT) final @NotBlank String subject,
            @ApiParam(value = API_KEY_DESC, required = true)
            @RequestHeader(name = X_FRS_API_KEY_HEADER) final String apiKey,
            @ApiParam(value = "New " + SUBJECT_DESC, required = true)
            @Valid @RequestBody final UpdateSubjectDto request
    ) {
        return Map.of(
                "updated",
                faceService.updateSubject(apiKey, subject, request.getSubject())
        );
    }

    @WriteEndpoint
    @DeleteMapping("/{image_id}")
    public FaceResponseDto deleteFaceById(
            @PathVariable final String image_id,
            @ApiParam(value = API_KEY_DESC, required = true)
            @RequestHeader(name = X_FRS_API_KEY_HEADER) final String apiKey
    ) {
        return faceService.deleteFaceById(image_id, apiKey);
    }

    @PostMapping(value = "/{image_id}/verify",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, List<FaceVerification>> recognizeFile(
            @ApiParam(value = API_KEY_DESC, required = true) @RequestHeader(X_FRS_API_KEY_HEADER) final String apiKey,
            @ApiParam(value = IMAGE_ID_DESC, required = true) @PathVariable final String image_id,
            @ApiParam(value = LIMIT_DESC) @RequestParam(defaultValue = LIMIT_DEFAULT_VALUE, required = false) @Min(value = 0, message = LIMIT_MIN_DESC) final Integer limit,
            @ApiParam(value = DET_PROB_THRESHOLD_DESC) @RequestParam(value = DET_PROB_THRESHOLD, required = false) final Double detProbThreshold,
            @ApiParam(value = FACE_PLUGINS_DESC) @RequestParam(value = FACE_PLUGINS, required = false, defaultValue = "") final String facePlugins,
            @ApiParam(value = STATUS_DESC) @RequestParam(value = STATUS, required = false, defaultValue = STATUS_DEFAULT_VALUE) final Boolean status,
            @ApiParam(value = IMAGE_WITH_ONE_FACE_DESC, required = true) @RequestParam final MultipartFile file
    ) {
        imageValidator.validate(file);

        var processImageParams = ProcessImageParams.builder()
                .additionalParams(Map.of(IMAGE_ID, image_id))
                .apiKey(apiKey)
                .detProbThreshold(detProbThreshold)
                .file(file)
                .facePlugins(facePlugins)
                .limit(limit)
                .status(status)
                .build();

        return faceService.verifyFace(processImageParams);
    }

    @PostMapping(value = "/{image_id}/verify", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<FaceVerification>> recognizeBase64(
            @ApiParam(value = API_KEY_DESC, required = true) @RequestHeader(X_FRS_API_KEY_HEADER) final String apiKey,
            @ApiParam(value = IMAGE_ID_DESC, required = true) @PathVariable final String image_id,
            @ApiParam(value = LIMIT_DESC) @RequestParam(defaultValue = LIMIT_DEFAULT_VALUE, required = false) @Min(value = 0, message = LIMIT_MIN_DESC) final Integer limit,
            @ApiParam(value = DET_PROB_THRESHOLD_DESC) @RequestParam(value = DET_PROB_THRESHOLD, required = false) final Double detProbThreshold,
            @ApiParam(value = FACE_PLUGINS_DESC) @RequestParam(value = FACE_PLUGINS, required = false, defaultValue = "") final String facePlugins,
            @ApiParam(value = STATUS_DESC) @RequestParam(value = STATUS, required = false, defaultValue = STATUS_DEFAULT_VALUE) final Boolean status,
            @RequestBody @Valid Base64File request) {

        imageValidator.validateBase64(request.getContent());

        var processImageParams = ProcessImageParams.builder()
                .additionalParams(Map.of(IMAGE_ID, image_id))
                .apiKey(apiKey)
                .detProbThreshold(detProbThreshold)
                .imageBase64(request.getContent())
                .facePlugins(facePlugins)
                .limit(limit)
                .status(status)
                .build();

        return faceService.verifyFace(processImageParams);
    }
}