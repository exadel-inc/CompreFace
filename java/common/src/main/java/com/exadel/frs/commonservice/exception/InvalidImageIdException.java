package com.exadel.frs.commonservice.exception;

import static com.exadel.frs.commonservice.handler.CommonExceptionCode.EMBEDDING_NOT_FOUND;

public class InvalidImageIdException extends BasicException {

    public InvalidImageIdException() {
        super(EMBEDDING_NOT_FOUND, "Image Id is not valid");
    }
}