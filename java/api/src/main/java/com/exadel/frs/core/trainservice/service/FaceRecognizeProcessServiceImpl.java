package com.exadel.frs.core.trainservice.service;

import com.exadel.frs.commonservice.annotation.CollectStatistics;
import com.exadel.frs.commonservice.enums.StatisticsType;
import com.exadel.frs.core.trainservice.component.FaceClassifierPredictor;
import com.exadel.frs.core.trainservice.dto.FaceSimilarityDto;
import com.exadel.frs.core.trainservice.dto.FacesRecognitionResponseDto;
import com.exadel.frs.core.trainservice.dto.ProcessImageParams;
import com.exadel.frs.core.trainservice.mapper.FacesMapper;
import com.exadel.frs.core.trainservice.sdk.faces.FacesApiClient;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.stream.Stream;

import static java.math.RoundingMode.HALF_UP;

@Service("recognitionService")
@RequiredArgsConstructor
public class FaceRecognizeProcessServiceImpl implements FaceProcessService {

    private final FaceClassifierPredictor classifierPredictor;
    private final FacesApiClient client;
    private final ImageExtensionValidator imageValidator;
    private final FacesMapper mapper;

    @Override
    @CollectStatistics(type = StatisticsType.FACE_RECOGNITION_CREATE)
    public FacesRecognitionResponseDto processImage(ProcessImageParams processImageParams) {
        MultipartFile file = (MultipartFile) processImageParams.getFile();
        imageValidator.validate(file);

        val findFacesResponse = client.findFacesWithCalculator(file, processImageParams.getLimit(), processImageParams.getDetProbThreshold(), processImageParams.getFacePlugins());
        val facesRecognitionDto = mapper.toFacesRecognitionResponseDto(findFacesResponse);

        for (val findResult : facesRecognitionDto.getResult()) {
            Object predictionCount = processImageParams.getAdditionalParams().get("predictionCount");
            val predictions = classifierPredictor.predict(
                    processImageParams.getApiKey(),
                    Stream.of(findResult.getEmbedding())
                            .mapToDouble(d -> d)
                            .toArray(),
                    (Integer) predictionCount
            );

            val faces = new ArrayList<FaceSimilarityDto>();

            for (val prediction : predictions) {
                var pred = BigDecimal.valueOf(prediction.getLeft());
                pred = pred.setScale(5, HALF_UP);
                faces.add(new FaceSimilarityDto(prediction.getRight(), pred.floatValue()));
            }

            var inBoxProb = BigDecimal.valueOf(findResult.getBox().getProbability());
            inBoxProb = inBoxProb.setScale(5, HALF_UP);
            findResult.getBox().setProbability(inBoxProb.doubleValue());

            findResult.setFaces(faces);
        }

        return facesRecognitionDto;
    }
}
