package com.exadel.frs.exception;

import static com.exadel.frs.handler.ExceptionCode.SELF_REMOVE;

public class SelfRemoveException extends BasicException {

    private static final String MESSAGE = "Owner cannot remove himself from organization";

    public SelfRemoveException() {
        super(SELF_REMOVE, MESSAGE);
    }

}
