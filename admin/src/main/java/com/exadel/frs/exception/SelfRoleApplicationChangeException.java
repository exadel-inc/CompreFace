package com.exadel.frs.exception;

import static com.exadel.frs.handler.ExceptionCode.SELF_ROLE_CHANGE;

public class SelfRoleApplicationChangeException extends BasicException {

    public static final String MESSAGE = "Application should have at least one OWNER";

    public SelfRoleApplicationChangeException() {
        super(SELF_ROLE_CHANGE, MESSAGE);
    }
}
