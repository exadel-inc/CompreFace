package com.exadel.frs.commonservice.exception;

import static com.exadel.frs.commonservice.handler.CommonExceptionCode.MISSING_PATH_VARIABLE;
import static java.lang.String.format;

public class MissingPathVarException extends BasicException {

    public static final String MISSING_PATH_VARIABLE_MESSAGE = "Required path variable %s is missing";

    public MissingPathVarException(final String paramName) {
        super(MISSING_PATH_VARIABLE, format(MISSING_PATH_VARIABLE_MESSAGE, paramName));
    }
}
