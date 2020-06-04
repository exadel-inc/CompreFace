package com.exadel.frs.core.trainservice.exception;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = BAD_REQUEST)
public class FileExtensionException extends RuntimeException {

    private static final String MESSAGE = "File [%s] has an unavailable extension";

    public FileExtensionException(final String fileName) {
        super(format(MESSAGE, fileName));
    }
}