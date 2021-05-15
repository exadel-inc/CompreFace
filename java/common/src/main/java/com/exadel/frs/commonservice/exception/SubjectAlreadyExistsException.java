package com.exadel.frs.commonservice.exception;

import static com.exadel.frs.commonservice.handler.CommonExceptionCode.SUBJECT_ALREADY_EXISTS;

public class SubjectAlreadyExistsException extends BasicException {

    private static final String MESSAGE = "Subject already exists";

    public SubjectAlreadyExistsException() {
        super(SUBJECT_ALREADY_EXISTS, MESSAGE);
    }
}
