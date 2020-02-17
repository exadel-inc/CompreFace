package com.exadel.frs.core.trainservice.scan;

import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class ScanController {
    private final ScanService scanService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/faces/{face_name}")
    public void scanFaces(
            @ApiParam(value = "A picture with at least one face (accepted formats: jpeg, png).", required = true)
            @RequestParam MultipartFile file,
            @ApiParam(value = "Person''s name to whom the face belongs to.", required = true)
            @PathVariable("face_name") String faceName,
            @ApiParam(value = "Specify whether the model should start retraining immediately after the request is completed (set this parameter to value \"no\", if operating with a lot of images one after another). Allowed values: \"yes\", \"no\", \"force\". \"Force\" option will abort already running processes of classifier training.", required = true)
            @RequestParam("retrain") RetrainStatus retrainStatus,
            @ApiParam(value = "The minimal percent confidence that found face is actually a face.")
            @RequestParam(value = "det_prob_threshold", required = false) Double detProbThreshold,
            @ApiParam(value = "api key", required = true)
            @RequestHeader("X-Api-Key") String xApiKey) {
        //need to check if need to retrain
        //and after save
        scanService.scanAndSaveFace(file, faceName, detProbThreshold, xApiKey);
    }
}
