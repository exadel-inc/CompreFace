package com.exadel.frs.core.trainservice.controller;

import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static com.exadel.frs.commonservice.enums.AppStatus.OK;
import com.exadel.frs.commonservice.sdk.faces.FacesApiClient;
import com.exadel.frs.commonservice.system.global.ImageProperties;
import com.exadel.frs.core.trainservice.dto.VersionConsistenceDto;
import com.exadel.frs.core.trainservice.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(API_V1 + "/consistence")
@RequiredArgsConstructor
public class ConsistenceController {

    private final EmbeddingService embeddingService;
    private final FacesApiClient facesApiClient;
    private final ImageProperties imageProperties;

    @GetMapping("/status")
    public VersionConsistenceDto getCheckDemo() {
        var calculatorVersion = facesApiClient.getStatus().getCalculatorVersion();

        return VersionConsistenceDto
                .builder()
                .demoFaceCollectionIsInconsistent(embeddingService.isDemoCollectionInconsistent())
                .dbIsInconsistent(embeddingService.isDbInconsistent(calculatorVersion))
                .saveImagesToDB(imageProperties.isSaveImagesToDB())
                .status(OK)
                .build();
    }
}
