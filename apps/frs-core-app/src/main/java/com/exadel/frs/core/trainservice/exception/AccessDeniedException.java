package com.exadel.frs.core.trainservice.exception;

import static java.lang.String.format;
import lombok.Value;

@Value
public class AccessDeniedException extends RuntimeException {

    private static final String MESSAGE = "Access denied";

    public AccessDeniedException() {
        super(format(MESSAGE));
    }
}
