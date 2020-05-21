package com.exadel.frs.core.trainservice.exception;

import static java.lang.String.format;
import lombok.Value;

@Value
public class BadFormatModelKeyException extends RuntimeException {

    private static final String MESSAGE = "Model key should be UUID";

    public BadFormatModelKeyException() {
        super(format(MESSAGE));
    }
}
