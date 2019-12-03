package com.exadel.frs.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class SelfRemoveFromOrganizationException extends RuntimeException {

    public SelfRemoveFromOrganizationException() {
        super("You cannot remove yourself from organization");
    }
}
