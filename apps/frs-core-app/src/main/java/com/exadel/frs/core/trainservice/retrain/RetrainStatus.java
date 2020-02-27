package com.exadel.frs.core.trainservice.retrain;

import com.exadel.frs.core.trainservice.system.TokenParts;

public enum RetrainStatus {
    YES {
        @Override
        public void run(TokenParts tokenParts, RetrainService retrainService) {
            if (retrainService.isTraining(tokenParts.getAppApiKey(), tokenParts.getModelApiKey())) {
                throw new ClassifierIsAlreadyTrainingException();
            }
            retrainService.startRetrain(tokenParts.getAppApiKey(), tokenParts.getModelApiKey());
        }
    },
    NO {
        @Override
        public void run(TokenParts tokenParts, RetrainService retrainService) {

        }
    },
    FORCE {
        @Override
        public void run(TokenParts tokenParts, RetrainService retrainService) {
            retrainService.abortTraining(tokenParts.getAppApiKey(), tokenParts.getModelApiKey());
            retrainService.startRetrain(tokenParts.getAppApiKey(), tokenParts.getModelApiKey());
        }
    };

    public abstract void run(TokenParts tokenParts, RetrainService retrainService);
}
