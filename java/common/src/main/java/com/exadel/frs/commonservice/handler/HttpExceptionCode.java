package com.exadel.frs.commonservice.handler;

import org.springframework.http.HttpStatus;

public interface HttpExceptionCode {
    Integer getCode();

    HttpStatus getHttpStatus();
}
