package com.exadel.frs.exception;

import static com.exadel.frs.handler.ExceptionCode.EMPTY_REQUIRED_FIELD;
import static java.lang.String.format;

public class EmptyRequiredFieldException extends BasicException {

    public static final String MESSAGE = "Field %s cannot be empty";

    public EmptyRequiredFieldException(final String fieldName) {
        super(EMPTY_REQUIRED_FIELD, format(MESSAGE, fieldName));
    }

}
