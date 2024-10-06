package com.exadel.frs.commonservice.exception;

import static com.exadel.frs.commonservice.handler.CommonExceptionCode.WRONG_EMBEDDING_COUNT;
import com.exadel.frs.commonservice.handler.HttpExceptionCode;

public class WrongEmbeddingCountException extends BasicException {

    private static final String MESSAGE = "%d embeddings were expected, but %d were provided";

    public WrongEmbeddingCountException(final int expectedCount, final int providedCount) {
        super(WRONG_EMBEDDING_COUNT, String.format(MESSAGE, expectedCount, providedCount));
    }

    public WrongEmbeddingCountException(final HttpExceptionCode exceptionCode, final String message) {
        super(exceptionCode, message);
    }
}
