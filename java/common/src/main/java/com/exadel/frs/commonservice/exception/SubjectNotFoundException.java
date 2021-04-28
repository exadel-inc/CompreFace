package com.exadel.frs.commonservice.exception;

import static com.exadel.frs.commonservice.handler.CommonExceptionCode.SUBJECT_NOT_FOUND;

public class SubjectNotFoundException extends BasicException {

    private static final String MESSAGE = "Subject %s not found";

    public SubjectNotFoundException(final String subject) {
        super(SUBJECT_NOT_FOUND, String.format(MESSAGE, subject));
    }
}
