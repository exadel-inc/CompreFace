package com.exadel.frs.commonservice.exception;

import java.util.UUID;

import static com.exadel.frs.commonservice.handler.CommonExceptionCode.EMBEDDING_NOT_FOUND;

public class EmbeddingNotFoundException extends BasicException {

    private static final String MESSAGE = "Image %s not found";

    public EmbeddingNotFoundException(final UUID embeddingId) {
        super(EMBEDDING_NOT_FOUND, String.format(MESSAGE, embeddingId));
    }
}
