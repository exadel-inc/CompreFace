package com.exadel.frs.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class OrganizationNotFoundException extends RuntimeException {

    public OrganizationNotFoundException(Long organizationId) {
        super("Organization " + organizationId + " not found");
    }

}
