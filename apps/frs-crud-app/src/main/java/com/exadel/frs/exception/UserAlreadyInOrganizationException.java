package com.exadel.frs.exception;

import static com.exadel.frs.handler.ExceptionCode.USER_ALREADY_IN_ORG;
import static java.lang.String.format;

public class UserAlreadyInOrganizationException extends BasicException {

    private static final String MESSAGE = "User %s already in organization %s";

    public UserAlreadyInOrganizationException(String user, String orgGuid) {
        super(USER_ALREADY_IN_ORG, format(MESSAGE, user, orgGuid));
    }

}
