package com.exadel.frs.exception;

import static com.exadel.frs.handler.ExceptionCode.NAME_IS_NOT_UNIQUE;
import static java.lang.String.format;

public class NameIsNotUniqueException extends BasicException {

    public static final String MESSAGE = "Name %s is not unique";

    public NameIsNotUniqueException(String fieldName) {
        super(NAME_IS_NOT_UNIQUE, format(MESSAGE, fieldName));
    }

}
