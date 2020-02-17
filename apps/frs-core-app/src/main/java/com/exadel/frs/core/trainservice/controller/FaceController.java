package com.exadel.frs.core.trainservice.controller;

import com.exadel.frs.core.trainservice.service.FaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
