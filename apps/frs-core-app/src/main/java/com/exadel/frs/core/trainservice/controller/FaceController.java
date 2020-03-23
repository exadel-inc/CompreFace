package com.exadel.frs.core.trainservice.controller;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.web.util.UriUtils.encode;
import com.exadel.frs.core.trainservice.service.FaceService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/faces")
@RestController
@RequiredArgsConstructor
public class FaceController {

    public static final String X_FRS_API_KEY_HEADER = "x-frs-api-key";
    public static final String X_API_KEY_HEADER = "X-Api-Key";
    private final FaceService faceService;

    @GetMapping
    public Map<String, List<String>> findAllFaceNamesByApiKey(
            @RequestHeader(name = X_FRS_API_KEY_HEADER)
            final String appKey
    ) {
        return faceService.findAllFaceNamesByApiKey(appKey);
    }

    @DeleteMapping("/{faceName}")
    public void deleteFaceByName(
            @PathVariable
            final String faceName,
            @RequestHeader(name = X_FRS_API_KEY_HEADER)
            final String appKey,
            @RequestHeader(name = X_API_KEY_HEADER)
            final String modelGuid,
            @RequestParam(required = false, defaultValue = "force")
            final String retrain
    ) {
        val faceNameEncoded = encode(faceName, UTF_8);

        faceService.deleteFaceByNameAndTrainModelIfRequired(faceNameEncoded, appKey, modelGuid, retrain);
    }

    @DeleteMapping
    public int deleteFacesByModel(
            @RequestHeader(name = X_FRS_API_KEY_HEADER)
            final String apiKey
    ) {
        val appKeyLength = apiKey.length() / 2;
        val appApiKey = apiKey.substring(0, appKeyLength);
        val modelApiKey = apiKey.substring(appKeyLength);

        val deletedFaces = faceService.deleteFacesByApiKey(appApiKey, modelApiKey);

        return deletedFaces.size();
    }
}