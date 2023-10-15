package com.exadel.frs.core.trainservice.service;

import static com.exadel.frs.core.trainservice.system.global.Constants.PREDICTION_COUNT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import com.exadel.frs.commonservice.exception.IncorrectPredictionCountException;
import com.exadel.frs.commonservice.repository.EmbeddingRepository;
import com.exadel.frs.commonservice.sdk.faces.FacesApiClient;
import com.exadel.frs.core.trainservice.DbHelper;
import com.exadel.frs.core.trainservice.EmbeddedPostgreSQLTest;
import com.exadel.frs.core.trainservice.component.FaceClassifierPredictor;
import com.exadel.frs.core.trainservice.dto.ProcessEmbeddingsParams;
import com.exadel.frs.core.trainservice.mapper.FacesMapper;
import com.exadel.frs.core.trainservice.repository.AppRepository;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class EmbeddingsRecognizeProcessServiceImplTest extends EmbeddedPostgreSQLTest {

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private ImageExtensionValidator imageExtensionValidator;

    @Autowired
    private FacesMapper facesMapper;

    @Autowired
    private AppRepository appRepository;

    @Autowired
    private EmbeddingRepository embeddingRepository;

    @MockBean
    private FacesApiClient facesApiClient;

    @MockBean
    private FaceClassifierPredictor predictor;

    @Autowired
    private EmbeddingsRecognizeProcessServiceImpl recognizeProcessService;

    @BeforeEach
    void cleanUp() {
        appRepository.deleteAll();
        appRepository.flush();
    }

    @Test
    void processEmbeddings_TheInputEmbeddingExistsInTheDatabase_ShouldReturnCompleteSimilarity() {
        var model = dbHelper.insertModel();
        var subject = dbHelper.insertSubject(model, "subject");
        var embedding = dbHelper.insertEmbeddingNoImg(subject);

        var params = ProcessEmbeddingsParams.builder()
                                            .apiKey(model.getApiKey())
                                            .embeddings(new double[][]{embedding.getEmbedding()})
                                            .additionalParams(Collections.singletonMap(PREDICTION_COUNT, 1))
                                            .build();

        when(predictor.predict(any(), any(), anyInt())).thenReturn(List.of(Pair.of(1.0, "subject")));
        assertThat(embeddingRepository.findAll()).containsOnly(embedding);

        var results = recognizeProcessService.processEmbeddings(params).getResult();

        assertThat(embeddingRepository.findAll()).containsOnly(embedding);
        assertThat(results).isNotEmpty().hasSize(1);

        var result = results.get(0);

        assertThat(result.getEmbedding()).isEqualTo(embedding.getEmbedding());
        assertThat(result.getSimilarities()).isNotEmpty().hasSize(1);
        assertThat(result.getSimilarities().get(0).getSimilarity()).isEqualTo(1.0F);
        assertThat(result.getSimilarities().get(0).getSubject()).isEqualTo("subject");
    }

    @Test
    void processEmbeddings_TheInputEmbeddingDoesNotExistInTheDatabase_ShouldNotReturnCompleteSimilarity() {
        var model = dbHelper.insertModel();
        var subject = dbHelper.insertSubject(model, "subject");
        var embedding = dbHelper.insertEmbeddingNoImg(subject);

        var params = ProcessEmbeddingsParams.builder()
                                            .apiKey(model.getApiKey())
                                            .embeddings(new double[][]{new double[]{7.3, 8.4, 9.5}})
                                            .additionalParams(Collections.singletonMap(PREDICTION_COUNT, 1))
                                            .build();

        when(predictor.predict(any(), any(), anyInt())).thenReturn(List.of(Pair.of(0.0, "subject")));
        assertThat(embeddingRepository.findAll()).containsOnly(embedding);

        var results = recognizeProcessService.processEmbeddings(params).getResult();

        assertThat(embeddingRepository.findAll()).containsOnly(embedding);
        assertThat(results).isNotEmpty().hasSize(1);

        var result = results.get(0);

        assertThat(result.getEmbedding()).isNotEqualTo(embedding.getEmbedding());
        assertThat(result.getSimilarities()).isNotEmpty().hasSize(1);
        assertThat(result.getSimilarities().get(0).getSimilarity()).isEqualTo(0.0F);
        assertThat(result.getSimilarities().get(0).getSubject()).isEqualTo("subject");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -2})
    void processEmbeddings_PredictionCountIsIncorrect_ShouldThrowIncorrectPredictionCountException(int predictionCount) {
        var params = ProcessEmbeddingsParams.builder()
                                            .additionalParams(Collections.singletonMap(PREDICTION_COUNT, predictionCount))
                                            .build();

        assertThatThrownBy(() -> recognizeProcessService.processEmbeddings(params))
                .isInstanceOf(IncorrectPredictionCountException.class);
    }

    @Test
    void processEmbeddings_PredictionCountIsNull_ShouldThrowIncorrectPredictionCountException() {
        var params = ProcessEmbeddingsParams.builder()
                                            .additionalParams(Collections.singletonMap(PREDICTION_COUNT, null))
                                            .build();

        assertThatThrownBy(() -> recognizeProcessService.processEmbeddings(params))
                .isInstanceOf(IncorrectPredictionCountException.class);
    }
}
