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
    private final FaceService faceService;

    @GetMapping
    public Map<String, List<String>> findAllFaceNamesByApiKey(@RequestHeader(name = "x-frs-api-key") String appKey){
        return faceService.findAllFaceNamesByApiKey(appKey);
    }

    @DeleteMapping("/{faceName}")
    public void deleteFaceByName(@PathVariable String faceName,
                                 @RequestHeader(name = "x-frs-api-key") String appKey,
                                 @RequestHeader(name = "X-Api-Key") String modelGuid,
                                 @RequestParam(required = false, defaultValue = "force") String retrain) throws UnsupportedEncodingException {
        faceName = UriUtils.encode(faceName, StandardCharsets.UTF_8);

        faceService.deleteFaceByNameAndTrainModelIfRequired(faceName, appKey, modelGuid, retrain);
    }
}
