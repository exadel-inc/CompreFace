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
import static com.exadel.frs.core.trainservice.system.global.Constants.MIN_FACES_TO_TRAIN;
import static com.exadel.frs.core.trainservice.system.global.Constants.X_FRS_API_KEY_HEADER;
import static org.apache.commons.lang3.StringUtils.isBlank;
import com.exadel.frs.core.trainservice.aspect.WriteEndpoint;
import com.exadel.frs.core.trainservice.cache.CachedFace;
import com.exadel.frs.core.trainservice.dto.ui.FaceResponseDto;
import com.exadel.frs.core.trainservice.mapper.FaceMapper;
import com.exadel.frs.core.trainservice.service.FaceService;
import com.exadel.frs.core.trainservice.service.RetrainService;
import com.exadel.frs.core.trainservice.service.ScanService;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import io.swagger.annotations.ApiParam;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(API_V1 + "/faces")
@RequiredArgsConstructor
public class FaceController {

    private final ScanService scanService;
    private final RetrainService retrainService;
    private final FaceService faceService;
    private final FaceMapper faceMapper;
    private final ImageExtensionValidator imageValidator;

    private static final int FIRST_ITEM_ADDED_NUM = 1;

    @WriteEndpoint
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public FaceResponseDto addFaces(
            @ApiParam(value = "A picture with at least one face (accepted formats: jpeg, png).", required = true)
            @RequestParam
            final MultipartFile file,
            @ApiParam(value = "Person's name to whom the face belongs to.", required = true)
            @RequestParam("subject")
            final String faceName,
            @ApiParam(value = "Specify whether the model should start retraining immediately after the request is completed " +
                    "(set this parameter to value \"no\", if operating with a lot of images one after another). " +
                    "Allowed values: \"yes\", \"no\", \"force\". \"Force\" option will abort already running processes of " +
                    "classifier training.")
            @RequestParam(value = "retrain", required = false)
            final String retrainOption,
            @ApiParam(value = "The minimal percent confidence that found face is actually a face.")
            @RequestParam(value = "det_prob_threshold", required = false)
            final Double detProbThreshold,
            @ApiParam(value = "api key", required = true)
            @RequestHeader(X_FRS_API_KEY_HEADER)
            final String apiKey
    ) throws IOException {
        imageValidator.validate(file);
        val face = scanService.scanAndSaveFace(file, faceName, detProbThreshold, apiKey);
        if (!(isBlank(retrainOption) &&
                faceService.countFacesInModel(apiKey) == FIRST_ITEM_ADDED_NUM)) {
            getTrainingOption(retrainOption).run(apiKey, retrainService);
        }

        return faceMapper.toResponseDto(face);
    }

    @GetMapping
    public Map<String, List<FaceResponseDto>> findFacesByModel(
            @ApiParam(value = "api key", required = true)
            @RequestHeader(name = X_FRS_API_KEY_HEADER)
            final String apiKey
    ) {
        return Map.of("faces", faceMapper.toResponseDto(faceService.findFaces(apiKey)));
    }

    @WriteEndpoint
    @DeleteMapping
    public List<FaceResponseDto> deleteFaces(
            @ApiParam(value = "Person's name to whom the face belongs to.", required = true)
            @RequestParam(name = "subject", required = false)
            final String subject,
            @ApiParam(value = "Specify whether the model should start retraining immediately after the request is completed " +
                    "(set this parameter to value \"no\", if operating with a lot of images one after another). " +
                    "Allowed values: \"yes\", \"no\", \"force\". \"Force\" option will abort already running processes of " +
                    "classifier training.")
            @RequestParam(value = "retrain", required = false)
            final String retrain,
            @ApiParam(value = "api key", required = true)
            @RequestHeader(name = X_FRS_API_KEY_HEADER)
            final String apiKey
    ) {
        val faces = new HashSet<CachedFace>();
        if (isBlank(subject)) {
            faces.addAll(faceService.deleteFacesByModel(apiKey));
        } else {
            faces.addAll(faceService.deleteFaceByName(subject, apiKey));
            if (!faces.isEmpty() &&
                    !(isBlank(retrain) && faceService.countFacesInModel(apiKey) < MIN_FACES_TO_TRAIN)) {
                getTrainingOption(retrain).run(apiKey, retrainService);
            }
        }

        return faceMapper.toResponseDto(faces);
    }

    @WriteEndpoint
    @DeleteMapping("/{image_id}")
    public FaceResponseDto deleteFaceById(
            @PathVariable
            final String image_id,
            @ApiParam(value = "Specify whether the model should start retraining immediately after the request is completed " +
                    "(set this parameter to value \"no\", if operating with a lot of images one after another). " +
                    "Allowed values: \"yes\", \"no\", \"force\". \"Force\" option will abort already running processes of " +
                    "classifier training.")
            @RequestParam(name = "retrain", required = false)
            final String retrain,
            @ApiParam(value = "api key", required = true)
            @RequestHeader(name = X_FRS_API_KEY_HEADER)
            final String apiKey
    ) {
        val face = faceService.deleteFaceById(image_id, apiKey);
        if (face != null &&
                !(isBlank(retrain) && faceService.countFacesInModel(apiKey) < MIN_FACES_TO_TRAIN)) {
            getTrainingOption(retrain).run(apiKey, retrainService);
        }

        return faceMapper.toResponseDto(face);
    }
}