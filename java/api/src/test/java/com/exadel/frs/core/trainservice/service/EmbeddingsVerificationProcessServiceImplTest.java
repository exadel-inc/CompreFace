package com.exadel.frs.core.trainservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import com.exadel.frs.commonservice.exception.WrongEmbeddingCountException;
import com.exadel.frs.commonservice.sdk.faces.FacesApiClient;
import com.exadel.frs.core.trainservice.DbHelper;
import com.exadel.frs.core.trainservice.EmbeddedPostgreSQLTest;
import com.exadel.frs.core.trainservice.component.FaceClassifierPredictor;
import com.exadel.frs.core.trainservice.dto.ProcessEmbeddingsParams;
import com.exadel.frs.core.trainservice.mapper.FacesMapper;
import com.exadel.frs.core.trainservice.repository.AppRepository;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class EmbeddingsVerificationProcessServiceImplTest extends EmbeddedPostgreSQLTest {

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private ImageExtensionValidator imageExtensionValidator;

    @Autowired
    private FacesMapper facesMapper;

    @Autowired
    private AppRepository appRepository;

    @MockBean
    private FaceClassifierPredictor predictor;

    @MockBean
    private FacesApiClient facesApiClient;

    @Autowired
    private EmbeddingsVerificationProcessServiceImpl verificationProcessService;

    @BeforeEach
    void cleanUp() {
        appRepository.deleteAll();
        appRepository.flush();
    }

    @Test
    void processEmbeddings_ThereAreTwoEmbeddingsInTheDatabase_ShouldReturnTwoSimilarityResultInSortedOrder() {
        var source = new double[]{1.0, 2.0, 3.0};
        var targets = new double[][]{
                new double[]{4.0, 5.0, 6.0},
                new double[]{7.0, 8.0, 9.0}
        };
        var similarities = new double[]{0.3, 0.5};
        var params = buildParams(source, targets);

        when(predictor.verify(source, targets)).thenReturn(similarities);

        var results = verificationProcessService.processEmbeddings(params).getResult();

        assertThat(results).isNotEmpty().hasSize(2);

        var result1 = results.get(0);
        var result2 = results.get(1);

        assertThat(result1.getSimilarity()).isEqualTo(0.5F);
        assertThat(result2.getSimilarity()).isEqualTo(0.3F);
        assertThat(result1.getEmbedding()).isEqualTo(targets[1]);
        assertThat(result2.getEmbedding()).isEqualTo(targets[0]);
    }

    @Test
    void processEmbeddings_TooFewTargets_ShouldThrowWrongEmbeddingCountException() {
        var source = new double[]{1.0, 2.0, 3.0};
        var targets = new double[0][];
        var params = buildParams(source, targets);

        assertThatThrownBy(() -> verificationProcessService.processEmbeddings(params))
                .isInstanceOf(WrongEmbeddingCountException.class);
    }

    @Test
    void processEmbeddings_EmbeddingsAreNull_ShouldThrowWrongEmbeddingCountException() {
        var params = ProcessEmbeddingsParams.builder().build();

        assertThatThrownBy(() -> verificationProcessService.processEmbeddings(params))
                .isInstanceOf(WrongEmbeddingCountException.class);
    }

    private ProcessEmbeddingsParams buildParams(double[] source, double[][] targets) {
        return ProcessEmbeddingsParams.builder()
                                      .embeddings(ArrayUtils.insert(0, targets, source))
                                      .build();
    }
}
