package com.exadel.frs.core.trainservice.controller;

import com.exadel.frs.core.trainservice.component.FaceClassifierManager;
import com.exadel.frs.core.trainservice.component.FaceClassifierPredictor;
import com.exadel.frs.core.trainservice.dto.RetrainResponse;
import com.exadel.frs.core.trainservice.system.SystemService;
import com.exadel.frs.core.trainservice.system.python.FacePrediction;
import com.exadel.frs.core.trainservice.system.python.ScanFacesClient;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static com.exadel.frs.core.trainservice.system.global.Constants.X_FRS_API_KEY_HEADER;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.springframework.http.HttpStatus.LOCKED;

@RestController
@RequestMapping(API_V1)
@RequiredArgsConstructor
public class RecognizeController {

    private final FaceClassifierManager manager;
    private final FaceClassifierPredictor classifierPredictor;
    private final ScanFacesClient client;
    private final SystemService systemService;
    private final ImageExtensionValidator imageValidator;

    @PostMapping(value = "/recognize")
    public ResponseEntity recognize(
            @ApiParam(value = "Api key of application and model", required = true)
            @RequestHeader(X_FRS_API_KEY_HEADER) final String apiKey,
            @ApiParam(value = "Image for recognizing", required = true)
            @RequestParam final MultipartFile file,
            @ApiParam(value = "Maximum number of faces to be recognized")
            @RequestParam(required = false) final Integer limit
    ) {
        val token = systemService.buildToken(apiKey);

        val lock = manager.isTraining(token.getAppApiKey(), token.getModelApiKey());
        if (lock) {
            return ResponseEntity.status(LOCKED)
                                 .body(new RetrainResponse("Model is locked now, try later"));
        }

        imageValidator.validate(file);

        val classifier = storage.getFaceClassifier(token.getAppApiKey(), token.getModelApiKey());

        val scanResponse = client.scanFaces(file, defaultIfNull(limit, 1), 0.5D);
        val scanResult = scanResponse.getResult().get(0);

        val prediction = classifierPredictor
                .predict(
                        token.getAppApiKey(),
                        token.getModelApiKey(),
                        scanResult.getEmbedding().stream().mapToDouble(d -> d).toArray());

        val result = new FacePrediction(
                scanResult.getBox(),
                prediction.getSecond(),
                scanResult.getEmbedding().get(0).floatValue(),
                scanResult.getBox().getProbability().floatValue()
        );

        return ResponseEntity.status(HttpStatus.OK)
                             .body(result);
    }
}