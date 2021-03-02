package com.exadel.frs.core.trainservice.controller;


import com.exadel.frs.commonservice.annotation.CollectStatistics;
import com.exadel.frs.commonservice.enums.StatisticsType;
import com.exadel.frs.core.trainservice.dto.ProcessImageParams;
import com.exadel.frs.core.trainservice.dto.VerifyFacesResponse;
import com.exadel.frs.core.trainservice.service.FaceProcessService;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Min;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.exadel.frs.core.trainservice.service.FaceVerificationProcessServiceImpl.RESULT;
import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static com.exadel.frs.core.trainservice.system.global.Constants.X_FRS_API_KEY_HEADER;

@RestController
@RequestMapping(API_V1)
@RequiredArgsConstructor
@Validated
public class VerifyController {

    private final FaceProcessService verificationService;

    @PostMapping(value = "/verify")
    @CollectStatistics(type = StatisticsType.FACE_VERIFICATION_CREATE)
    public Map<String, List<VerifyFacesResponse>> verify(
            @ApiParam(value = "Api key of application and model", required = true)
            @RequestHeader(X_FRS_API_KEY_HEADER) final String apiKey,
            @ApiParam(value = "File to be verified", required = true)
            @RequestParam final MultipartFile processFile,
            @ApiParam(value = "Reference file to check the processed file", required = true)
            @RequestParam final MultipartFile checkFile,
            @ApiParam(value = "Maximum number of faces to be verified")
            @RequestParam(defaultValue = "0", required = false)
            @Min(value = 0, message = "Limit should be equal or greater than 0") final Integer limit,
            @ApiParam(value = "The minimal percent confidence that found face is actually a face.")
            @RequestParam(value = "det_prob_threshold", required = false) final Double detProbThreshold,
            @ApiParam(value = "Comma-separated types of face plugins. Empty value - face plugins disabled, returns only bounding boxes")
            @RequestParam(value = "face_plugins", required = false, defaultValue = "") final String facePlugins
    ) {
        Map<String, MultipartFile> fileMap = Map.of("processFile", processFile, "checkFile", checkFile);
        ProcessImageParams processImageParams = ProcessImageParams
                .builder()
                .apiKey(apiKey)
                .file(fileMap)
                .limit(limit)
                .detProbThreshold(detProbThreshold)
                .facePlugins(facePlugins)
                .build();
        return Map.of(RESULT, Collections.singletonList(
                (VerifyFacesResponse) verificationService.processImage(processImageParams)
        ));
    }

}
