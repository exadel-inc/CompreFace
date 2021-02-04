package com.exadel.frs.core.trainservice.exception;

import com.exadel.frs.core.trainservice.handler.ExceptionCode;
import lombok.Getter;

@Getter
public class BasicException extends RuntimeException {

    private final ExceptionCode exceptionCode;
    private final String message;

    public BasicException(final ExceptionCode exceptionCode, final String message) {
        super(message);
        this.exceptionCode = exceptionCode;
        this.message = message;
    }
}