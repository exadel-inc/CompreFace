package com.exadel.frs.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class OrganizationMismatchException extends RuntimeException {

    public OrganizationMismatchException() {
        super("Model cannot give access to apps from different organizations");
    }

}
