package com.exadel.frs.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class EmptyRequiredFieldException extends RuntimeException {

    public EmptyRequiredFieldException(String message) {
        super(message);
    }

}
