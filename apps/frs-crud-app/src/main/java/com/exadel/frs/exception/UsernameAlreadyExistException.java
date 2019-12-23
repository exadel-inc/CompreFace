package com.exadel.frs.exception;

import static com.exadel.frs.handler.ExceptionCode.USERNAME_ALREADY_EXIST;

public class UsernameAlreadyExistException extends BasicException {

    public static final String MESSAGE = "Such username already exists";

    public UsernameAlreadyExistException() {
        super(USERNAME_ALREADY_EXIST, MESSAGE);
    }

}
