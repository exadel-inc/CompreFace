package com.exadel.frs.commonservice.exception;

import static com.exadel.frs.commonservice.handler.CrudExceptionCode.APPERY_SERVICE_EXCEPTION;

public class ApperyServiceException extends BasicException {

    public static final String MESSAGE = "We couldn't send statistics to Appery.io. Please make sure that it's correct!";

    public ApperyServiceException() {
        super(APPERY_SERVICE_EXCEPTION, MESSAGE);
    }
}