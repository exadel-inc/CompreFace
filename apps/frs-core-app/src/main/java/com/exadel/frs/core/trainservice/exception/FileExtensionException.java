package com.exadel.frs.core.trainservice.exception;

import static java.lang.String.format;
import lombok.Value;

@Value
public class FileExtensionException extends RuntimeException {

    private static final String MESSAGE = "File [%s] has an unavailable extension";

    public FileExtensionException(final String fileName) {
        super(format(MESSAGE, fileName));
    }
}