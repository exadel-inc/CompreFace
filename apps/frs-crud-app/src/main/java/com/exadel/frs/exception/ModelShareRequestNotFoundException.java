package com.exadel.frs.exception;

import java.util.UUID;

import static com.exadel.frs.handler.ExceptionCode.MODEL_SHARE_REQUEST_NOT_FOUND;
import static java.lang.String.format;

public class ModelShareRequestNotFoundException extends BasicException{
    public static final String MESSAGE = "Model Share Request with requestId = %s not found. It might be expired.";

    public ModelShareRequestNotFoundException(UUID requestId) {
        super(MODEL_SHARE_REQUEST_NOT_FOUND, format(MESSAGE, requestId.toString()));
    }
}