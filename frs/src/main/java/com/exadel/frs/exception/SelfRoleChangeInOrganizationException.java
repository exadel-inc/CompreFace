package com.exadel.frs.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class SelfRoleChangeInOrganizationException extends RuntimeException {

    public SelfRoleChangeInOrganizationException() {
        super("You can not change your own role in organization");
    }
}
