package com.exadel.frs.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class EmptyAppNameException extends RuntimeException {

    public EmptyAppNameException() {
        super("Application name is required");
    }

}
