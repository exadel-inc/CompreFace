package com.exadel.frs.exception;

import static com.exadel.frs.handler.ExceptionCode.UNREACHABLE_EMAIL_ADDRESS;

public class UnreachableEmailException extends BasicException {

    public static final String MESSAGE = "We couldn't send email to the given address(%s). Please make sure that it's correct!";

    public UnreachableEmailException(String email) {
        super(UNREACHABLE_EMAIL_ADDRESS, String.format(MESSAGE, email));
    }
}