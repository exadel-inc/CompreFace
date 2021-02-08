package com.exadel.frs.exception;

import com.exadel.frs.commonservice.exception.BasicException;

import static com.exadel.frs.handler.CrudExceptionCode.APPERY_SERVICE_EXCEPTION;

public class ApperyServiceException extends BasicException {

    public static final String MESSAGE = "We couldn't send statistics to Appery.io. Please make sure that it's correct!";

    public ApperyServiceException() {
        super(APPERY_SERVICE_EXCEPTION, MESSAGE);
    }
}