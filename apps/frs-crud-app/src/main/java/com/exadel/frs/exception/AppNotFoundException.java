package com.exadel.frs.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class AppNotFoundException extends RuntimeException {

    public AppNotFoundException(Long appId) {
        super("App " + appId + " not found");
    }

}
