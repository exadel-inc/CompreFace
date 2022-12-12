package com.exadel.frs.commonservice.exception;

import static com.exadel.frs.commonservice.handler.CommonExceptionCode.EMBEDDING_NOT_FOUND;

public class IncorrectImageIdException extends BasicException {

    public IncorrectImageIdException() {
        super(EMBEDDING_NOT_FOUND, "Image Id is incorrect");
    }
}
