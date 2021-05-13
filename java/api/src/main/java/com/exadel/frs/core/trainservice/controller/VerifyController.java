package com.exadel.frs.core.trainservice.controller;


import com.exadel.frs.core.trainservice.dto.ProcessImageParams;
import com.exadel.frs.core.trainservice.dto.VerifyFacesResponse;
import com.exadel.frs.core.trainservice.dto.VerifySourceTargetRequest;
import com.exadel.frs.core.trainservice.service.FaceProcessService;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.exadel.frs.commonservice.system.global.Constants.DET_PROB_THRESHOLD;
import static com.exadel.frs.core.trainservice.service.FaceVerificationProcessServiceImpl.RESULT;
import static com.exadel.frs.core.trainservice.system.global.Constants.*;

@RestController
@RequestMapping(API_V1)
@RequiredArgsConstructor
@Validated
public class VerifyController {

    private final FaceProcessService verificationService;

    @PostMapping(value = "/verification/verify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, List<VerifyFacesResponse>> verify(
            @ApiParam(value = API_KEY_DESC, required = true) @RequestHeader(X_FRS_API_KEY_HEADER) final String apiKey,
            @ApiParam(value = SOURCE_IMAGE_DESC, required = true)
            @RequestParam(name = SOURCE_IMAGE) final MultipartFile sourceImage,
            @ApiParam(value = TARGET_IMAGE_DESC, required = true)
            @RequestParam(name = TARGET_IMAGE) final MultipartFile targetImage,
            @ApiParam(value = LIMIT_DESC)
            @RequestParam(defaultValue = LIMIT_DEFAULT_VALUE, required = false)
            @Min(value = 0, message = LIMIT_MIN_DESC) final Integer limit,
            @ApiParam(value = DET_PROB_THRESHOLD_DESC)
            @RequestParam(value = DET_PROB_THRESHOLD, required = false) final Double detProbThreshold,
            @ApiParam(value = FACE_PLUGINS_DESC)
            @RequestParam(value = FACE_PLUGINS, required = false, defaultValue = "") final String facePlugins,
            @ApiParam(value = STATUS_DESC)
            @RequestParam(value = STATUS, required = false, defaultValue = STATUS_DEFAULT_VALUE) final Boolean status
    ) {
        Map<String, MultipartFile> fileMap = Map.of(
                SOURCE_IMAGE, sourceImage,
                TARGET_IMAGE, targetImage
        );

        ProcessImageParams processImageParams = ProcessImageParams
                .builder()
                .apiKey(apiKey)
                .file(fileMap)
                .limit(limit)
                .detProbThreshold(detProbThreshold)
                .facePlugins(facePlugins)
                .status(status)
                .build();

        final VerifyFacesResponse response = (VerifyFacesResponse) verificationService.processImage(processImageParams);
        return Map.of(RESULT, Collections.singletonList(response));
    }

    @PostMapping(value = "/verification/verify", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<VerifyFacesResponse>> verifyBase64(
            @ApiParam(value = API_KEY_DESC, required = true) @RequestHeader(X_FRS_API_KEY_HEADER) final String apiKey,
            @ApiParam(value = LIMIT_DESC) @RequestParam(defaultValue = LIMIT_DEFAULT_VALUE, required = false) @Min(value = 0, message = LIMIT_MIN_DESC) final Integer limit,
            @ApiParam(value = DET_PROB_THRESHOLD_DESC) @RequestParam(value = DET_PROB_THRESHOLD, required = false) final Double detProbThreshold,
            @ApiParam(value = FACE_PLUGINS_DESC) @RequestParam(value = FACE_PLUGINS, required = false, defaultValue = "") final String facePlugins,
            @ApiParam(value = STATUS_DESC) @RequestParam(value = STATUS, required = false, defaultValue = STATUS_DEFAULT_VALUE) final Boolean status,
            @RequestBody @Valid VerifySourceTargetRequest request) {

        Map<String, String> fileMap = Map.of(
                SOURCE_IMAGE, request.getSourceImageBase64(),
                TARGET_IMAGE, request.getTargetImageBase64()
        );

        ProcessImageParams processImageParams = ProcessImageParams
                .builder()
                .apiKey(apiKey)
                .file(fileMap)
                .limit(limit)
                .detProbThreshold(detProbThreshold)
                .facePlugins(facePlugins)
                .status(status)
                .build();

        final VerifyFacesResponse response = (VerifyFacesResponse) verificationService.processImage(processImageParams);
        return Map.of(RESULT, Collections.singletonList(response));
    }
}
