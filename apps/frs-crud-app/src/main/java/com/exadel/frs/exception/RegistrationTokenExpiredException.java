package com.exadel.frs.exception;

import static com.exadel.frs.handler.ExceptionCode.USER_REGISTRATION_TOKEN_EXPIRED;

public class RegistrationTokenExpiredException extends BasicException {

    private static final String MESSAGE = "User registration token is expired!";

    public RegistrationTokenExpiredException() {
        super(USER_REGISTRATION_TOKEN_EXPIRED, MESSAGE);
    }
}