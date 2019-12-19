package com.exadel.frs.exception;

import static com.exadel.frs.handler.ExceptionCode.SELF_ROLE_CHANGE;

public class SelfRoleChangeException extends BasicException {

    public static final String MESSAGE = "Owner cannot change his own organization/application role";

    public SelfRoleChangeException() {
        super(SELF_ROLE_CHANGE, MESSAGE);
    }

}
