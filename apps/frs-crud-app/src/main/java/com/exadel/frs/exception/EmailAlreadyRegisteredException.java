package com.exadel.frs.exception;

import static com.exadel.frs.handler.ExceptionCode.EMAIL_ALREADY_REGISTERED;

public class EmailAlreadyRegisteredException extends BasicException {

    public static final String MESSAGE = "This email already registered";

    public EmailAlreadyRegisteredException() {
        super(EMAIL_ALREADY_REGISTERED, MESSAGE);
    }

}
