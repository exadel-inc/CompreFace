package com.exadel.frs.exception;

import static com.exadel.frs.handler.ExceptionCode.ACCESS_DENIED;

public class AccessDeniedException extends BasicException {

    private static final String MESSAGE = "Access Denied. Application has read only access to model";

    public AccessDeniedException() {
        super(ACCESS_DENIED, MESSAGE);
    }

}
