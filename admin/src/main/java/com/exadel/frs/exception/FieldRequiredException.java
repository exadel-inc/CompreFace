package com.exadel.frs.exception;

import static com.exadel.frs.handler.ExceptionCode.EMPTY_REQUIRED_FIELD;
import static java.lang.String.format;

public class FieldRequiredException extends BasicException {

    public static final String MESSAGE = "%s is required";

    public FieldRequiredException(final String fieldName) {
        super(EMPTY_REQUIRED_FIELD, format(MESSAGE, fieldName));
    }

}