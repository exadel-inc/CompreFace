package com.exadel.frs.commonservice.exception;

import static com.exadel.frs.commonservice.handler.CommonExceptionCode.FACE_NOT_FOUND;

public class FaceNotFoundException extends BasicException {

    private static final String MESSAGE = "Face %s not found";

    public FaceNotFoundException(final String faceName) {
        super(FACE_NOT_FOUND, String.format(MESSAGE, faceName));
    }
}
