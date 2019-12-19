package com.exadel.frs.exception;

import static com.exadel.frs.handler.ExceptionCode.INCORRECT_APP_ROLE;
import static java.lang.String.format;

public class IncorrectAppRoleException extends BasicException {

    public static final String APP_ROLE_NOT_EXISTS_MESSAGE = "App role %s does not exists";

    public IncorrectAppRoleException(final String appRole) {
        super(INCORRECT_APP_ROLE, format(APP_ROLE_NOT_EXISTS_MESSAGE, appRole));
    }

}
