package com.exadel.frs.exception;

import static com.exadel.frs.handler.ExceptionCode.USER_DOES_NOT_EXIST;
import static java.lang.String.format;

public class UserDoesNotExistException extends BasicException {

    public static final String MESSAGE = "User %s does not exist";

    public UserDoesNotExistException(final Long userId) {
        super(USER_DOES_NOT_EXIST, format(MESSAGE, userId));
    }

}