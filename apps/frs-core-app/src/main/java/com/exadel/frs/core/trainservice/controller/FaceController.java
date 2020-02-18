package com.exadel.frs.core.trainservice.controller;

import com.exadel.frs.core.trainservice.service.FaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RequestMapping("/faces")
@RestController
@RequiredArgsConstructor
public class FaceController {
    public static final String X_FRS_API_KEY_HEADER = "x-frs-api-key";
    public static final String X_API_KEY_HEADER = "X-Api-Key";
    private final FaceService faceService;

    @GetMapping
    public Map<String, List<String>> findAllFaceNamesByApiKey(@RequestHeader(name = X_FRS_API_KEY_HEADER) String appKey) {
        return faceService.findAllFaceNamesByApiKey(appKey);
    }

    @DeleteMapping("/{faceName}")
    public void deleteFaceByName(@PathVariable String faceName,
                                 @RequestHeader(name = X_FRS_API_KEY_HEADER) String appKey,
                                 @RequestHeader(name = X_API_KEY_HEADER) String modelGuid,
                                 @RequestParam(required = false, defaultValue = "force") String retrain) throws UnsupportedEncodingException {
        faceName = UriUtils.encode(faceName, StandardCharsets.UTF_8);

        faceService.deleteFaceByNameAndTrainModelIfRequired(faceName, appKey, modelGuid, retrain);
    }
}
