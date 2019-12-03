package com.exadel.frs.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class IncorrectAppRoleException extends RuntimeException {

    public IncorrectAppRoleException() {
        super("App role can not be null");
    }

    public IncorrectAppRoleException(String appRole) {
        super("App role " + appRole + " does not exists");
    }

}
