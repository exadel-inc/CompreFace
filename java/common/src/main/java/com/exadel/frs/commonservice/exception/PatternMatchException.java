package com.exadel.frs.commonservice.exception;

import static com.exadel.frs.commonservice.handler.CommonExceptionCode.INCORRECT_ARGUMENT;

public class PatternMatchException extends BasicException {

    public PatternMatchException(final String message) {
        super(INCORRECT_ARGUMENT, message);
    }
}
