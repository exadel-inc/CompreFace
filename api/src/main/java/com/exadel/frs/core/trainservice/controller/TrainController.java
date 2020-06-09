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
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.OK;
import com.exadel.frs.core.trainservice.aspect.WriteEndpoint;
import com.exadel.frs.core.trainservice.dto.RetrainResponse;
import com.exadel.frs.core.trainservice.service.RetrainService;
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

    @WriteEndpoint
    @PostMapping("/retrain")
    public ResponseEntity train(
            @ApiParam(value = "Api key of application and model", required = true)
            @RequestHeader(X_FRS_API_KEY_HEADER)
            final String apiKey
    ) {
        retrainService.startRetrain(apiKey);

        return ResponseEntity.status(ACCEPTED)
                             .body(new RetrainResponse("Training is started"));
    }

    @WriteEndpoint
    @GetMapping("/retrain")
    public ResponseEntity getStatus(
            @ApiParam(value = "Api key of application and model", required = true)
            @RequestHeader(X_FRS_API_KEY_HEADER)
            final String apiKey
    ) {
        val isRun = retrainService.isTrainingRun(apiKey);

        if (isRun) {
            return ResponseEntity.status(ACCEPTED)
                                 .body(new RetrainResponse("Retraining has been previously started"));
        }

        return ResponseEntity.status(OK)
                             .body(new RetrainResponse("Ready to start training"));
    }

    @WriteEndpoint
    @DeleteMapping(value = "/retrain")
    public ResponseEntity abortRetrain(
            @ApiParam(value = "Api key of application and model", required = true)
            @RequestHeader(X_FRS_API_KEY_HEADER)
            final String apiKey
    ) {
        retrainService.abortTraining(apiKey);

        return ResponseEntity.status(ACCEPTED)
                             .body(new RetrainResponse("Retraining is ensured to be stopped"));
    }
}