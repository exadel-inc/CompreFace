package com.exadel.frs.core.trainservice.retrain;

public interface RetrainService {
    void startRetrain(String appKey, String modelId);

    boolean isTraining(String appkey, String modelId);

    void abortTraining(String appKey, String modelId);
}
