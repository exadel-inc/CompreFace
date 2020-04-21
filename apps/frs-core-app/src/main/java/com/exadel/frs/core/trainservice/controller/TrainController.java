package com.exadel.frs.core.trainservice.controller;

import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;

import com.exadel.frs.core.trainservice.component.FaceClassifierManager;
import com.exadel.frs.core.trainservice.dto.RetrainResponse;
import com.exadel.frs.core.trainservice.dao.FaceDao;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(API_V1)
@RequiredArgsConstructor
public class TrainController {

    private final FaceClassifierManager manager;

    @RequestMapping(value = "/retrain", method = RequestMethod.POST)
    public ResponseEntity train(
            @RequestHeader("apikey") final String appkey,
            @RequestHeader("modelid") final String modelId
    ) {
        manager.initNewClassifier(appkey, modelId);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                             .body(new RetrainResponse("Retraining has just been started (this one already exists)"));
    }

    @RequestMapping(value = "/retrain", method = RequestMethod.GET)
    public ResponseEntity getStatus(
            @RequestHeader("apikey") final String appkey,
            @RequestHeader("modelid") final String modelId
    ) {
        var training = manager.isTraining(appkey, modelId);
        if (training) {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                                 .body(new RetrainResponse("Retraining has been previously started"));
        }

        return ResponseEntity.status(HttpStatus.OK)
                             .body(new RetrainResponse("Ready to start training"));
    }

    @RequestMapping(value = "/retrain", method = RequestMethod.DELETE)
    public ResponseEntity abortRetrain(
            @RequestHeader("apikey") final String appkey,
            @RequestHeader("modelid") final String modelId
    ) {
        manager.abortClassifierTraining(appkey, modelId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                             .body(new RetrainResponse("Retraining is ensured to be stopped"));
    }
}