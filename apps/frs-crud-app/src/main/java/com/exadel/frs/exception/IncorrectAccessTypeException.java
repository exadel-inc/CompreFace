package com.exadel.frs.exception;

import static com.exadel.frs.handler.ExceptionCode.INCORRECT_ACCESS_TYPE;
import static java.lang.String.format;

public class IncorrectAccessTypeException extends BasicException {

    public static final String ACCESS_TYPE_NOT_EXISTS_MESSAGE = "Access type %s does not exists";

    public IncorrectAccessTypeException(final String accessType) {
        super(INCORRECT_ACCESS_TYPE, format(ACCESS_TYPE_NOT_EXISTS_MESSAGE, accessType));
    }

}
