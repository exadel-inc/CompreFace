package com.exadel.frs.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class IncorrectAccessTypeException extends RuntimeException {

    public IncorrectAccessTypeException() {
        super("Access type can not be null");
    }

    public IncorrectAccessTypeException(String accessType) {
        super("Access type " + accessType + " does not exists");
    }

}
