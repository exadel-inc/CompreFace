package com.exadel.frs.controller;

import com.exadel.frs.commonservice.repository.FacesRepository;
import com.exadel.frs.commonservice.sdk.faces.FacesApiClient;
import com.exadel.frs.commonservice.system.global.ImageProperties;
import com.exadel.frs.dto.VersionConsistenseDto;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/consistence")
@RequiredArgsConstructor
public class ConsistenceController {

    private final FacesRepository facesRepository;
    private final FacesApiClient facesApiClient;
    private final ImageProperties imageProperties;

    @GetMapping("/status")
    public VersionConsistenseDto getCheckDemo() {
        val calculatorVersion = facesApiClient.getStatus().getCalculatorVersion();
        return VersionConsistenseDto
                .builder()
                .demoFaceCollectionIsInconsistent(facesRepository.isDemoFaceCollectionInconsistent())
                .dbIsInconsistent(facesRepository.isDbInconsistent(calculatorVersion))
                .saveImagesToDB(imageProperties.isSaveImagesToDB())
                .build();
    }
}
