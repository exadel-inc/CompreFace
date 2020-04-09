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
            if (retrainService.isTraining(token.getAppApiKey(), token.getModelApiKey())) {
                throw new ClassifierIsAlreadyTrainingException();
            }

            retrainService.startRetrain(token.getAppApiKey(), token.getModelApiKey());
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
            retrainService.abortTraining(token.getAppApiKey(), token.getModelApiKey());
            retrainService.startRetrain(token.getAppApiKey(), token.getModelApiKey());
        }
    };

    public abstract void run(final Token token, final RetrainService retrainService);

    public static RetrainOption getTrainingOption(final String option) {
        return getIfPresent(RetrainOption.class, firstNonNull(option.toUpperCase(), ""))
                .or(FORCE);
    }
}