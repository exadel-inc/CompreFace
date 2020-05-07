package com.exadel.frs.core.trainservice.exception;

import lombok.Value;

import static java.lang.String.format;

@Value
public class MigrationExecutionException extends RuntimeException {
    private static final String MESSAGE = "Write operations during migration aren't allowed";

    public MigrationExecutionException() {
        super(format(MESSAGE));
    }

}
