package com.exadel.frs.core.trainservice.dto;

public abstract class FaceProcessResponse {
    public static final String CALCULATOR="calculator";

    public abstract FaceProcessResponse prepareResponse(FaceProcessResponse response, String facePlugins);
}
