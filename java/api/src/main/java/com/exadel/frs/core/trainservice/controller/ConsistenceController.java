package com.exadel.frs.core.trainservice.controller;

import com.exadel.frs.commonservice.repository.FacesRepository;
import com.exadel.frs.commonservice.sdk.faces.FacesApiClient;
import com.exadel.frs.commonservice.system.global.ImageProperties;
import com.exadel.frs.core.trainservice.dto.VersionConsistenceDto;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;

@RestController
@RequestMapping(API_V1 + "/consistence")
@RequiredArgsConstructor
public class ConsistenceController {

    private final String FACENET = "facenet.Calculator";

    private final FacesRepository facesRepository;
    private final FacesApiClient facesApiClient;
    private final ImageProperties imageProperties;

    @GetMapping("/status")
    public VersionConsistenceDto getCheckDemo() {
        val calculatorVersion = facesApiClient.getStatus().getCalculatorVersion();
        return VersionConsistenceDto
                .builder()
                .demoFaceCollectionIsInconsistent(!FACENET.equals(calculatorVersion))
//                .dbIsInconsistent(facesRepository.isDbInconsistent(calculatorVersion))
                .saveImagesToDB(imageProperties.isSaveImagesToDB())
                .build();
    }
}
