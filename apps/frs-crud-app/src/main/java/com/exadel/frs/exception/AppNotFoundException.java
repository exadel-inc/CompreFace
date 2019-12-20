package com.exadel.frs.exception;

import static com.exadel.frs.handler.ExceptionCode.APP_NOT_FOUND;
import static java.lang.String.format;

public class AppNotFoundException extends BasicException {

    private static final String MESSAGE = "App %s not found";

    public AppNotFoundException(final String guid) {
        super(APP_NOT_FOUND, format(MESSAGE, guid));
    }

}
