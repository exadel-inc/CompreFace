package com.exadel.frs.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class SelfRemoveException extends RuntimeException {

    public SelfRemoveException() {
        super("Owner cannot remove himself from organization");
    }

}
