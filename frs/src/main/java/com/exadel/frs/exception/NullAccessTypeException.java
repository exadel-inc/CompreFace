package com.exadel.frs.exception;

public class NullAccessTypeException extends RuntimeException {

    public NullAccessTypeException() {
        super("Access type can not be null");
    }

}
