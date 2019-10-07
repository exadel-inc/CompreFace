package com.exadel.frs.exception;

public class AppNotFoundException extends RuntimeException {

    public AppNotFoundException(Long appId) {
        super("App " + appId + " not found or you do not have access to it");
    }

}
