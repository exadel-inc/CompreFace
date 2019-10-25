package com.exadel.frs.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class EmptyModelNameException extends RuntimeException {

    public EmptyModelNameException() {
        super("Model name is required");
    }

}
