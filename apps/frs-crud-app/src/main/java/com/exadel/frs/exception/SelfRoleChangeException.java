package com.exadel.frs.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class SelfRoleChangeException extends RuntimeException {

    public SelfRoleChangeException() {
        super("Owner cannot change his own organization/application role");
    }

}
