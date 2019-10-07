package com.exadel.frs.exception;

public class ModelNotFoundException extends RuntimeException {

    public ModelNotFoundException(Long modelId) {
        super("Model " + modelId + " not found or you do not have access to it");
    }

}
