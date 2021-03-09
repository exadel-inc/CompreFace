package com.exadel.frs.core.trainservice.service;

import com.exadel.frs.core.trainservice.dto.FacesDetectionResponseDto;
import com.exadel.frs.core.trainservice.dto.ProcessImageParams;
import com.exadel.frs.core.trainservice.mapper.FacesMapper;
import com.exadel.frs.core.trainservice.sdk.faces.FacesApiClient;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Service("detectionService")
@RequiredArgsConstructor
public class FaceDetectionProcessServiceImpl implements FaceProcessService {

    private final FacesApiClient client;
    private final ImageExtensionValidator imageValidator;
    private final FacesMapper mapper;

    @Override
    public FacesDetectionResponseDto processImage(ProcessImageParams processImageParams) {
        MultipartFile file = (MultipartFile) processImageParams.getFile();
        imageValidator.validate(file);

        FacesDetectionResponseDto facesDetectionResponseDto = mapper.toFacesDetectionResponseDto(
                client.findFaces(file, processImageParams.getLimit(), processImageParams.getDetProbThreshold(), processImageParams.getFacePlugins()));
        return cleanupResult(facesDetectionResponseDto, !processImageParams.getStatus());
    }

    private FacesDetectionResponseDto cleanupResult(FacesDetectionResponseDto facesDetectionResponseDto, boolean shouldClean) {
        if (shouldClean) {
            facesDetectionResponseDto.setPluginsVersions(null);
            facesDetectionResponseDto.getResult().forEach(r -> r.setExecutionTime(null));
        }

        return facesDetectionResponseDto;
    }
}
