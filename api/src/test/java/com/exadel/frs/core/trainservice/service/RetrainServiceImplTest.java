package com.exadel.frs.core.trainservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import com.exadel.frs.core.trainservice.component.FaceClassifierManager;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class RetrainServiceImplTest {

    private static final String MODEL_KEY = "model_key";

    @Mock
    private FaceClassifierManager manager;

    @InjectMocks
    private RetrainServiceImpl retrainService;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void startRetrain() {
        retrainService.startRetrain(MODEL_KEY);

        verify(manager).initNewClassifier(MODEL_KEY);
        verifyNoMoreInteractions(manager);
    }

    @Test
    void isTrainingRun() {
        when(manager.isTraining(MODEL_KEY)).thenReturn(true);

        val actual = retrainService.isTrainingRun(MODEL_KEY);

        assertThat(actual).isTrue();

        verify(manager).isTraining(MODEL_KEY);
        verifyNoMoreInteractions(manager);
    }

    @Test
    void abortTraining() {
        retrainService.abortTraining(MODEL_KEY);

        verify(manager).finishClassifierTraining(MODEL_KEY);
        verifyNoMoreInteractions(manager);
    }
}