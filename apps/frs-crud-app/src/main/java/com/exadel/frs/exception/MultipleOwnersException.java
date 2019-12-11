package com.exadel.frs.exception;

import static com.exadel.frs.handler.ExceptionCode.MULTIPLE_OWNERS;

public class MultipleOwnersException extends BasicException {

    public static final String MESSAGE = "Only one owner allowed per organization or app";

    public MultipleOwnersException() {
        super(MULTIPLE_OWNERS, MESSAGE);
    }

}
