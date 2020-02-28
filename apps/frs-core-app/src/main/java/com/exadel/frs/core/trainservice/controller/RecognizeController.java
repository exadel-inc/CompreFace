package com.exadel.frs.core.trainservice.controller;

import com.exadel.frs.core.trainservice.dto.RetrainResponse;
import com.exadel.frs.core.trainservice.repository.FaceClassifierStorage;
import com.exadel.frs.core.trainservice.scan.FacePrediction;
import com.exadel.frs.core.trainservice.scan.PythonClient;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class RecognizeController {

    private final FaceClassifierStorage storage;
    private final PythonClient client;

    @RequestMapping(value = "/recognize", method = RequestMethod.POST)
    public ResponseEntity recognize(
            @RequestHeader("apikey")
            final String apiKey,
            @RequestHeader("modelid")
            final String modelId,
            @RequestParam
            final MultipartFile file
    ) {
        var lock = storage.isLocked(apiKey, modelId);
        if (lock) {
            return ResponseEntity.status(HttpStatus.LOCKED)
                                 .body(new RetrainResponse("Model is locked now, try later"));
        }

        val classifier = storage.getFaceClassifier(apiKey, modelId);

        val scanResponse = client.scanFaces(file, 1, 0.5D);
        val scanResult = scanResponse.getResult().get(0);

        val prediction = classifier.predict(
                scanResult.getEmbedding().stream().mapToDouble(d -> d).toArray()
        );

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