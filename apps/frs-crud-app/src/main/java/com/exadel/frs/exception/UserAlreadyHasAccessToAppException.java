package com.exadel.frs.exception;

import static com.exadel.frs.handler.ExceptionCode.USER_ALREADY_HAS_ACCESS_TO_APP;
import static java.lang.String.format;

public class UserAlreadyHasAccessToAppException extends BasicException {

    private static final String MESSAGE = "User %s already has access to application %s";

    public UserAlreadyHasAccessToAppException(String user, String appGuid) {
        super(USER_ALREADY_HAS_ACCESS_TO_APP, format(MESSAGE, user, appGuid));
    }

}
