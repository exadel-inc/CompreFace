package com.exadel.frs.core.trainservice.service;

public interface RetrainService {

    void startRetrain(final String appKey, final String modelId);

    boolean isTraining(final String appKey, final String modelId);

    void abortTraining(final String appKey, final String modelId);
}