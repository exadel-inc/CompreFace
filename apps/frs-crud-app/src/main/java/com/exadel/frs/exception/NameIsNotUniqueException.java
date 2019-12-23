package com.exadel.frs.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class NameIsNotUniqueException extends RuntimeException {

    public NameIsNotUniqueException(String fieldName) {
        super("Name '" + fieldName + "' is not unique");
    }

}
