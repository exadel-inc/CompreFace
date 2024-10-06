package com.exadel.frs.exception;

import static com.exadel.frs.commonservice.handler.CrudExceptionCode.MAIL_SERVER_EXCEPTION;
import com.exadel.frs.commonservice.exception.BasicException;

public class MailServerDisabledException extends BasicException {

    public static final String MESSAGE = "We cannot send an email. No email server enabled!";

    public MailServerDisabledException() {
        super(MAIL_SERVER_EXCEPTION, MESSAGE);
    }
}
