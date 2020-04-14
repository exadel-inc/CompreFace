package com.exadel.frs.core.trainservice.controller;

import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import com.exadel.frs.core.trainservice.dao.FaceDao;
import com.exadel.frs.core.trainservice.dto.RetrainResponse;
import com.exadel.frs.core.trainservice.repository.FaceClassifierStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(API_V1)
@RequiredArgsConstructor
public class TrainController {

    private final FaceClassifierStorage storage;

    private final FaceDao faceDao;

    @PostMapping("/retrain")
    public ResponseEntity train(
            @RequestHeader("apiKey")
            final String appKey,
            @RequestHeader("modelKey")
            final String modelKey
    ) {
        storage.lock(appKey, modelKey);
        storage.getFaceClassifier(appKey, modelKey)
               .train(faceDao.findAllFaceEmbeddingsByApiKey(modelKey), appKey, modelKey);

        return ResponseEntity.status(ACCEPTED)
                             .body(new RetrainResponse("Retraining has just been started (this one already exists)"));
    }

    @GetMapping("/retrain")
    public ResponseEntity getStatus(
            @RequestHeader("apiKey")
            final String appKey,
            @RequestHeader("modelKey")
            final String modelKey
    ) {
        var lock = storage.isLocked(appKey, modelKey);
        if (lock) {
            return ResponseEntity.status(ACCEPTED)
                                 .body(new RetrainResponse("Retraining has been previously started"));
        }

        return ResponseEntity.status(OK)
                             .body(new RetrainResponse("Ready to start training"));
    }

    @DeleteMapping(value = "/retrain")
    public ResponseEntity abortRetrain(
            @RequestHeader("apiKey")
            final String appKey,
            @RequestHeader("modelKey")
            final String modelKey
    ) {
        storage.unlock(appKey, modelKey);

        return ResponseEntity.status(NO_CONTENT)
                             .body(new RetrainResponse("Retraining is ensured to be stopped"));
    }
}