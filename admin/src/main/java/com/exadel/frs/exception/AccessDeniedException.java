package com.exadel.frs.exception;

import static com.exadel.frs.handler.ExceptionCode.APP_ACCESS_DENIED;

public class AccessDeniedException extends BasicException {

    private static final String MESSAGE = "Access denied";

    public AccessDeniedException() {
        super(APP_ACCESS_DENIED, MESSAGE);
    }

}
