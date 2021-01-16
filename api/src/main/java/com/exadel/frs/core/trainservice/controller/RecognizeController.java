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
import static com.exadel.frs.core.trainservice.system.global.Constants.X_FRS_API_KEY_HEADER;
import static java.math.RoundingMode.HALF_UP;
import com.exadel.frs.core.trainservice.component.FaceClassifierPredictor;
import com.exadel.frs.core.trainservice.dto.FaceSimilarityDto;
import com.exadel.frs.core.trainservice.dto.FacesRecognitionResponseDto;
import com.exadel.frs.core.trainservice.mapper.FacesMapper;
import com.exadel.frs.core.trainservice.sdk.faces.FacesApiClient;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import io.swagger.annotations.ApiParam;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.stream.Stream;
import javax.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(API_V1)
@RequiredArgsConstructor
@Validated
public class RecognizeController {

    private final FaceClassifierPredictor classifierPredictor;
    private final FacesApiClient client;
    private final ImageExtensionValidator imageValidator;
    private final FacesMapper mapper;

    @PostMapping(value = "/faces/recognize")
    public FacesRecognitionResponseDto recognize(
            @ApiParam(value = "Api key of application and model", required = true)
            @RequestHeader(X_FRS_API_KEY_HEADER)
            final String apiKey,
            @ApiParam(value = "Image for recognizing", required = true)
            @RequestParam
            final MultipartFile file,
            @ApiParam(value = "Maximum number of faces to be recognized")
            @RequestParam(defaultValue = "0", required = false)
            @Min(value = 0, message = "Limit should be equal or greater than 0")
            final Integer limit,
            @ApiParam(value = "Maximum number of predictions per faces")
            @RequestParam(defaultValue = "1", name = "prediction_count", required = false)
            @Min(value = 1, message = "prediction_count should be equal or greater than 1")
            final Integer predictionCount,
            @ApiParam(value = "The minimal percent confidence that found face is actually a face.")
            @RequestParam(value = "det_prob_threshold", required = false)
            final Double detProbThreshold,
            @ApiParam(value = "Comma-separated types of face plugins. Empty value - face plugins disabled, returns only bounding boxes")
            @RequestParam(value = "face_plugins", required = false)
            final String facePlugins
    ) {
        imageValidator.validate(file);

        val findFacesResponse = client.findFacesWithCalculator(file, limit, detProbThreshold, facePlugins);
        val facesRecognitionDto = mapper.toFacesRecognitionResponseDto(findFacesResponse);

        for (val findResult : facesRecognitionDto.getResult()) {
            val predictions = classifierPredictor.predict(
                    apiKey,
                    Stream.of(findResult.getEmbedding())
                          .mapToDouble(d -> d)
                          .toArray(),
                    predictionCount
            );

            val faces = new ArrayList<FaceSimilarityDto>();

            for (val prediction : predictions) {
                var pred = BigDecimal.valueOf(prediction.getLeft());
                pred = pred.setScale(5, HALF_UP);
                faces.add(new FaceSimilarityDto(prediction.getRight(), pred.floatValue()));
            }

            var inBoxProb = BigDecimal.valueOf(findResult.getBox().getProbability());
            inBoxProb = inBoxProb.setScale(5, HALF_UP);
            findResult.getBox().setProbability(inBoxProb.doubleValue());

            findResult.setFaces(faces);
        }

        return facesRecognitionDto;
    }
}