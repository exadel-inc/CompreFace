package com.exadel.frs.core.trainservice.controller;

import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static com.exadel.frs.core.trainservice.system.global.Constants.X_FRS_API_KEY_HEADER;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import com.exadel.frs.core.trainservice.dto.RetrainResponse;
import com.exadel.frs.core.trainservice.service.RetrainService;
import com.exadel.frs.core.trainservice.system.SystemService;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.val;
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

    private final RetrainService retrainService;
    private final SystemService systemService;

    @PostMapping("/retrain")
    public ResponseEntity train(
            @ApiParam(value = "Api key of application and model", required = true)
            @RequestHeader(X_FRS_API_KEY_HEADER)
            final String apiKey
    ) {
        val token = systemService.buildToken(apiKey);
        retrainService.startRetrain(token.getAppApiKey(), token.getModelApiKey());

        return ResponseEntity.status(ACCEPTED)
                             .body(new RetrainResponse("Retraining has just been started (this one already exists)"));
    }

    @GetMapping("/retrain")
    public ResponseEntity getStatus(
            @ApiParam(value = "Api key of application and model", required = true)
            @RequestHeader(X_FRS_API_KEY_HEADER)
            final String apiKey
    ) {
        val token = systemService.buildToken(apiKey);
        val isRun = retrainService.isTrainingRun(token.getAppApiKey(), token.getModelApiKey());

        if (isRun) {
            return ResponseEntity.status(ACCEPTED)
                                 .body(new RetrainResponse("Retraining has been previously started"));
        }

        return ResponseEntity.status(OK)
                             .body(new RetrainResponse("Ready to start training"));
    }

    @DeleteMapping(value = "/retrain")
    public ResponseEntity abortRetrain(
            @ApiParam(value = "Api key of application and model", required = true)
            @RequestHeader(X_FRS_API_KEY_HEADER)
            final String apiKey
    ) {
        val token = systemService.buildToken(apiKey);
        retrainService.abortTraining(token.getAppApiKey(), token.getModelApiKey());

        return ResponseEntity.status(NO_CONTENT)
                             .body(new RetrainResponse("Retraining is ensured to be stopped"));
    }
}