package com.exadel.frs.commonservice.exception;

import static com.exadel.frs.commonservice.handler.CommonExceptionCode.MISSING_REQUEST_PART;
import static java.lang.String.format;

public class MissingRequestPartException extends BasicException {

    public static final String MISSING_REQUEST_PART_MESSAGE = "Required part %s is missing";

    public MissingRequestPartException(final String partName) {
        super(MISSING_REQUEST_PART, format(MISSING_REQUEST_PART_MESSAGE, partName));
    }
}
