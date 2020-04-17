package com.exadel.frs.core.trainservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import com.exadel.frs.core.trainservice.component.classifiers.FaceClassifier;
import com.exadel.frs.core.trainservice.dao.FaceDao;
import com.exadel.frs.core.trainservice.repository.FaceClassifierAdapter;
import com.exadel.frs.core.trainservice.repository.FaceClassifierStorage;
import java.util.Random;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;

class RetrainServiceImplTest {

    private static final String APP_KEY = "app_key";
    private static final String MODEL_KEY = "model_key";

    @Mock
    private FaceClassifierStorage storage;

    @Mock
    private FaceClassifier faceClassifier;

    @Mock
    private FaceDao faceDao;

    @InjectMocks
    private RetrainServiceImpl retrainService;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void startRetrain() {
        val classifier = new FaceClassifierAdapter(
                mock(ApplicationContext.class),
                mock(FaceClassifierStorage.class)
        );
        classifier.setClassifier(faceClassifier);

        when(storage.getFaceClassifier(APP_KEY, MODEL_KEY)).thenReturn(classifier);

        retrainService.startRetrain(APP_KEY, MODEL_KEY);

        verify(storage).lock(APP_KEY, MODEL_KEY);
        verify(storage).getFaceClassifier(APP_KEY, MODEL_KEY);
        verify(faceDao).findAllFaceEmbeddingsByApiKey(MODEL_KEY);
        verifyNoMoreInteractions(storage, faceDao);
    }

    @Test
    void isTrainingRun() {
        val isLocked = new Random().nextBoolean();
        when(storage.isLocked(APP_KEY, MODEL_KEY)).thenReturn(isLocked);

        val actual = retrainService.isTrainingRun(APP_KEY, MODEL_KEY);

        assertThat(actual).isEqualTo(isLocked);

        verify(storage).isLocked(APP_KEY, MODEL_KEY);
        verifyNoInteractions(faceDao);
        verifyNoMoreInteractions(storage);
    }

    @Test
    void abortTraining() {
        retrainService.abortTraining(APP_KEY, MODEL_KEY);

        verify(storage).unlock(APP_KEY, MODEL_KEY);
        verifyNoInteractions(faceDao);
        verifyNoMoreInteractions(storage);
    }
}