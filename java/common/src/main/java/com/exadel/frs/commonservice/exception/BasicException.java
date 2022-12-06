package com.exadel.frs.commonservice.exception;

import com.exadel.frs.commonservice.handler.HttpExceptionCode;
import lombok.Getter;

@Getter
public class BasicException extends RuntimeException {

    public enum LogLevel {
        DEBUG, ERROR
    }

    private final HttpExceptionCode exceptionCode;
    private final String message;

    public BasicException(final HttpExceptionCode exceptionCode, final String message) {
        super(message);
        this.exceptionCode = exceptionCode;
        this.message = message;
    }

    public LogLevel getLogLevel() {
        return LogLevel.ERROR;
    }
}
