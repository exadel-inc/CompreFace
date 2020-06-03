package com.exadel.frs.core.trainservice.controller;

import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static com.exadel.frs.core.trainservice.system.global.Constants.NEW_MODEL_KEY_HEADER;
import static com.exadel.frs.core.trainservice.system.global.Constants.X_FRS_API_KEY_HEADER;
import com.exadel.frs.core.trainservice.aspect.WriteEndpoint;
import com.exadel.frs.core.trainservice.service.FaceService;
import com.exadel.frs.core.trainservice.service.ModelService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(API_V1 + "/models")
@RequiredArgsConstructor
public class ModelController {

    private final ModelService modelService;

    @PutMapping("/api-key")
    public void updateModelApiKeyForFaces(
            @RequestHeader(name = X_FRS_API_KEY_HEADER)
            final String apiKey,
            @RequestParam(name = NEW_MODEL_KEY_HEADER)
            final String newModelApiKey
    ) {
        modelService.updateModelApiKeyForFaces(apiKey, newModelApiKey);
    }
}