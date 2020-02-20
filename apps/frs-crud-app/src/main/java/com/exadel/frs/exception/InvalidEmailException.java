package com.exadel.frs.exception;

import static com.exadel.frs.handler.ExceptionCode.INVALID_EMAIL_FORMAT;

public class InvalidEmailException extends BasicException {
    public static final String MESSAGE = "Email format is incorrect";

    public InvalidEmailException() {
        super(INVALID_EMAIL_FORMAT, MESSAGE);
    }
}