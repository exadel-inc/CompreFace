package com.exadel.frs.core.trainservice.enums;

import static com.exadel.frs.core.trainservice.enums.RetrainOption.FORCE;
import static com.exadel.frs.core.trainservice.enums.RetrainOption.NO;
import static com.exadel.frs.core.trainservice.enums.RetrainOption.YES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import com.exadel.frs.core.trainservice.exception.ClassifierIsAlreadyTrainingException;
import com.exadel.frs.core.trainservice.service.RetrainService;
import com.exadel.frs.core.trainservice.system.Token;
import lombok.val;
import org.junit.jupiter.api.Test;

class RetrainOptionTest {

    private static final String MODEL_KEY = "model_key";

    @Test
    void runNO() {
        val token = new Token(MODEL_KEY);
        val retrainService = mock(RetrainService.class);

        NO.run(token, retrainService);

        verifyNoInteractions(retrainService);
    }

    @Test
    void runYES() {
        val token = new Token(MODEL_KEY);
        val retrainService = mock(RetrainService.class);
        when(retrainService.isTrainingRun(MODEL_KEY)).thenReturn(false);

        YES.run(token, retrainService);

        verify(retrainService).isTrainingRun(MODEL_KEY);
        verify(retrainService).startRetrain(MODEL_KEY);
        verifyNoMoreInteractions(retrainService);
    }

    @Test
    void runYESException() {
        val token = new Token(MODEL_KEY);
        val retrainService = mock(RetrainService.class);
        when(retrainService.isTrainingRun(MODEL_KEY)).thenReturn(true);

        assertThrows(ClassifierIsAlreadyTrainingException.class, () ->
                YES.run(token, retrainService));

        verify(retrainService).isTrainingRun(MODEL_KEY);
        verifyNoMoreInteractions(retrainService);
    }

    @Test
    void runFORCE() {
        val token = new Token(MODEL_KEY);
        val retrainService = mock(RetrainService.class);
        when(retrainService.isTrainingRun(MODEL_KEY)).thenReturn(true);

        FORCE.run(token, retrainService);

        verify(retrainService).abortTraining(MODEL_KEY);
        verify(retrainService).startRetrain(MODEL_KEY);
        verifyNoMoreInteractions(retrainService);
    }

    @Test
    void getTrainingOptionNO() {
        val actual = RetrainOption.getTrainingOption("no");

        assertThat(actual).isEqualTo(NO);
    }

    @Test
    void getTrainingOptionYES() {
        val actual = RetrainOption.getTrainingOption("yes");

        assertThat(actual).isEqualTo(YES);
    }

    @Test
    void getTrainingOptionFORCE() {
        val actual = RetrainOption.getTrainingOption("force");

        assertThat(actual).isEqualTo(FORCE);
    }

    @Test
    void getTrainingOptionDefault() {
        val actual = RetrainOption.getTrainingOption(null);

        assertThat(actual).isEqualTo(FORCE);
    }
}