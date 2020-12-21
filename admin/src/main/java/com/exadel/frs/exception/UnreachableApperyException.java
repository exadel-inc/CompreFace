package com.exadel.frs.exception;

import static com.exadel.frs.handler.ExceptionCode.UNREACHABLE_APPERY_SERVICE;

public class UnreachableApperyException extends BasicException {

    public static final String MESSAGE = "We couldn't send statistics to Appery.io. Please make sure that it's correct!";

    public UnreachableApperyException() {
        super(UNREACHABLE_APPERY_SERVICE, MESSAGE);
    }
}
