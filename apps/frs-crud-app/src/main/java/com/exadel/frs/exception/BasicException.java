package com.exadel.frs.exception;

import com.exadel.frs.handler.ExceptionCode;
import lombok.Getter;

@Getter
public class BasicException extends RuntimeException {
    private final ExceptionCode exceptionCode;
    private final String message;

    protected BasicException(final ExceptionCode exceptionCode, final String message) {
        super(message);
        this.exceptionCode = exceptionCode;
        this.message = message;
    }
}
