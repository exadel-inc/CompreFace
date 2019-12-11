package com.exadel.frs.exception;

import static com.exadel.frs.handler.ExceptionCode.APP_OR_MODEL_NOT_FOUND_OR_PERMISSION_ISSUE;

public class AppOrModelNotFoundException extends BasicException {

    private static final String MESSAGE = "App or model does not exists, or app do not have permission to this model";

    public AppOrModelNotFoundException() {
        super(APP_OR_MODEL_NOT_FOUND_OR_PERMISSION_ISSUE, MESSAGE);
    }

}
