package com.exadel.frs.core.trainservice.service;

import static com.exadel.frs.core.trainservice.system.global.Constants.PREDICTION_COUNT;
import static java.math.RoundingMode.HALF_UP;
import com.exadel.frs.commonservice.exception.IncorrectPredictionCountException;
import com.exadel.frs.commonservice.sdk.faces.FacesApiClient;
import com.exadel.frs.core.trainservice.component.FaceClassifierPredictor;
import com.exadel.frs.core.trainservice.dto.EmbeddingRecognitionProcessResult;
import com.exadel.frs.core.trainservice.dto.EmbeddingSimilarityResult;
import com.exadel.frs.core.trainservice.dto.EmbeddingsRecognitionProcessResponse;
import com.exadel.frs.core.trainservice.dto.ProcessEmbeddingsParams;
import com.exadel.frs.core.trainservice.mapper.FacesMapper;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

@Service("recognitionService")
public class EmbeddingsRecognizeProcessServiceImpl extends FaceRecognizeProcessServiceImpl implements EmbeddingsProcessService {

    private final FaceClassifierPredictor classifierPredictor;

    public EmbeddingsRecognizeProcessServiceImpl(final FaceClassifierPredictor classifierPredictor,
                                                 final FacesApiClient facesApiClient,
                                                 final ImageExtensionValidator imageExtensionValidator,
                                                 final FacesMapper facesMapper) {
        super(classifierPredictor, facesApiClient, imageExtensionValidator, facesMapper);
        this.classifierPredictor = classifierPredictor;
    }

    @Override
    public EmbeddingsRecognitionProcessResponse processEmbeddings(final ProcessEmbeddingsParams processEmbeddingsParams) {
        Integer predictionCount = (Integer) processEmbeddingsParams.getAdditionalParams().get(PREDICTION_COUNT);
        if (predictionCount == null || (predictionCount == 0 || predictionCount < -1)) {
            throw new IncorrectPredictionCountException();
        }

        String apiKey = processEmbeddingsParams.getApiKey();
        double[][] embeddings = processEmbeddingsParams.getEmbeddings();

        List<EmbeddingRecognitionProcessResult> results =
                Arrays.stream(embeddings)
                      .map(embedding -> processEmbedding(predictionCount, apiKey, embedding))
                      .collect(Collectors.toList());

        return new EmbeddingsRecognitionProcessResponse(results);
    }

    private EmbeddingRecognitionProcessResult processEmbedding(final Integer predictionCount, final String apiKey, final double[] embedding) {
        List<Pair<Double, String>> predictions = classifierPredictor.predict(apiKey, embedding, predictionCount);
        List<EmbeddingSimilarityResult> similarities = predictions.stream()
                                                                  .map(this::processPrediction)
                                                                  .collect(Collectors.toList());

        return new EmbeddingRecognitionProcessResult(embedding, similarities);
    }

    private EmbeddingSimilarityResult processPrediction(final Pair<Double, String> prediction) {
        BigDecimal scaledPrediction = BigDecimal.valueOf(prediction.getLeft()).setScale(5, HALF_UP);
        return new EmbeddingSimilarityResult(prediction.getRight(), scaledPrediction.floatValue());
    }
}
