package com.exadel.frs.core.trainservice.exception;

import static java.lang.String.format;
import lombok.Value;

@Value
public class ModelNotFoundException extends RuntimeException {

    private static final String MESSAGE = "Model not found";

    public ModelNotFoundException() {
        super(format(MESSAGE));
    }
}
