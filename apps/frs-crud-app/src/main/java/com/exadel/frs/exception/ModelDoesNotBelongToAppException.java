package com.exadel.frs.exception;

import static com.exadel.frs.handler.ExceptionCode.MODEL_DOES_NOT_BELONG_TO_APP;
import static java.lang.String.format;

public class ModelDoesNotBelongToAppException extends BasicException {

    private static final String MESSAGE = "Model %s does not belong to application %s";

    public ModelDoesNotBelongToAppException(String modelGuid, String appGuid) {
        super(MODEL_DOES_NOT_BELONG_TO_APP, format(MESSAGE, modelGuid, appGuid));
    }
}