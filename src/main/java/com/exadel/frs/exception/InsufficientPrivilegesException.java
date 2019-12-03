package com.exadel.frs.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InsufficientPrivilegesException extends RuntimeException {

    public InsufficientPrivilegesException(Long userId) {
        super("User " + userId + " does not have permission to do this action");
    }

}
