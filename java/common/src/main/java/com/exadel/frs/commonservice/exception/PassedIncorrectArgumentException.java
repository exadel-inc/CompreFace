package com.exadel.frs.commonservice.exception;

import static com.exadel.frs.commonservice.handler.CommonExceptionCode.MISSING_PATH_VARIABLE;
import static java.lang.String.format;

public class PassedIncorrectArgumentException extends BasicException {

    public static final String INCORRECT_ARGUMENT_MESSAGE = "Passed argument is incorrect: %s";

    public PassedIncorrectArgumentException(final String exceptionMessage) {
        super(MISSING_PATH_VARIABLE, format(INCORRECT_ARGUMENT_MESSAGE, exceptionMessage));
    }
}
