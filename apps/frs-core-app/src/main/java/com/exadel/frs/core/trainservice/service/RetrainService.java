package com.exadel.frs.core.trainservice.service;

public interface RetrainService {

    void startRetrain(final String modelKey);

    boolean isTrainingRun(final String modelKey);

    void abortTraining(final String modelKey);
}