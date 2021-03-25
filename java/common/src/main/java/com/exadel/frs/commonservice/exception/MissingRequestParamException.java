package com.exadel.frs.commonservice.exception;

import static com.exadel.frs.commonservice.handler.CommonExceptionCode.MISSING_REQUEST_PARAMETER;
import static java.lang.String.format;

public class MissingRequestParamException extends BasicException {

    public static final String MISSING_REQUEST_PARAMETER_MESSAGE = "Required parameter %s is missing";

    public MissingRequestParamException(final String paramName) {
        super(MISSING_REQUEST_PARAMETER, format(MISSING_REQUEST_PARAMETER_MESSAGE, paramName));
    }
}
