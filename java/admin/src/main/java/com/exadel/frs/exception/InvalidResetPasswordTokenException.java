package com.exadel.frs.exception;

import static com.exadel.frs.commonservice.handler.CrudExceptionCode.INVALID_RESET_PASSWORD_TOKEN;
import com.exadel.frs.commonservice.exception.BasicException;

public class InvalidResetPasswordTokenException extends BasicException {

    public static final String MESSAGE = "The reset password token is invalid!";

    public InvalidResetPasswordTokenException() {
        super(INVALID_RESET_PASSWORD_TOKEN, MESSAGE);
    }
}
