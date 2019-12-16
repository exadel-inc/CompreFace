package com.exadel.frs.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class IncorrectOrganizationRoleException extends RuntimeException {

    public IncorrectOrganizationRoleException() {
        super("Organization role can not be null");
    }

    public IncorrectOrganizationRoleException(String organizationRole) {
        super("Organization role " + organizationRole + " does not exists");
    }

}
