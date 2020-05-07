package com.exadel.frs.core.trainservice.exception;

import lombok.Value;

import static java.lang.String.format;

@Value
public class MigrationAlreadyExecutingException extends RuntimeException {
    private static final String MESSAGE = "Migration already executing";

    public MigrationAlreadyExecutingException() {
        super(format(MESSAGE));
    }

}
