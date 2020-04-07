package com.exadel.frs.core.trainservice.enums;

import static com.google.common.base.Enums.getIfPresent;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import com.exadel.frs.core.trainservice.exception.ClassifierIsAlreadyTrainingException;
import com.exadel.frs.core.trainservice.service.RetrainService;
import com.exadel.frs.core.trainservice.system.Token;

public enum RetrainOption {

    YES {
        @Override
        public void run(final Token token, final RetrainService retrainService) {
            if (retrainService.isTraining(token.getAppKey(), token.getModelKey())) {
                throw new ClassifierIsAlreadyTrainingException();
            }

            retrainService.startRetrain(token.getAppKey(), token.getModelKey());
        }
    },
    NO {
        @Override
        public void run(final Token token, final RetrainService retrainService) {

        }
    },
    FORCE {
        @Override
        public void run(final Token token, final RetrainService retrainService) {
            retrainService.abortTraining(token.getAppKey(), token.getModelKey());
            retrainService.startRetrain(token.getAppKey(), token.getModelKey());
        }
    };

    public abstract void run(final Token token, final RetrainService retrainService);

    public static RetrainOption getTrainingOption(String option) {
        return getIfPresent(RetrainOption.class, firstNonNull(option, ""))
                .or(NO);
    }
}