package com.exadel.frs.exception;

import static com.exadel.frs.handler.ExceptionCode.VALIDATION_CONSTRAINT_VIOLATION;

public class ConstraintViolationException extends BasicException {

    public static final String MESSAGE = "Validation for field %s failed: %s";
    public static final String CUSTOM_MESSAGE = "%s";

    public ConstraintViolationException(String field, String validationMessage) {
        super(VALIDATION_CONSTRAINT_VIOLATION, String.format(MESSAGE, field, validationMessage));
    }

    public ConstraintViolationException(String validationMessage) {
        super(VALIDATION_CONSTRAINT_VIOLATION, String.format(CUSTOM_MESSAGE, validationMessage));
    }
}
