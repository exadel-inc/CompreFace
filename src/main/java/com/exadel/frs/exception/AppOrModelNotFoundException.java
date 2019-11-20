package com.exadel.frs.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class AppOrModelNotFoundException extends RuntimeException {

    public AppOrModelNotFoundException() {
        super("App or model does not exists, or app do not have permission to this model");
    }

}
