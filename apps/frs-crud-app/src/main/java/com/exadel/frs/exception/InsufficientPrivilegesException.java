package com.exadel.frs.exception;

import static com.exadel.frs.handler.ExceptionCode.INSUFFICIENT_PRIVILEGES;
import static java.lang.String.format;

public class InsufficientPrivilegesException extends BasicException {

    public static final String MESSAGE = "User %s does not have permission to do this action";

    public InsufficientPrivilegesException(final Long userId) {
        super(INSUFFICIENT_PRIVILEGES, format(MESSAGE, userId));
    }

}
