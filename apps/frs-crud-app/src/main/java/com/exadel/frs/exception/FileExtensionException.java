package com.exadel.frs.exception;

import static com.exadel.frs.handler.ExceptionCode.UNAVAILABLE_FILE_EXTENSION;
import static java.lang.String.format;
import java.util.Collection;

public class FileExtensionException extends BasicException {

    public FileExtensionException(final String fileName) {
        super(UNAVAILABLE_FILE_EXTENSION, format("File [%s] has an unavailable extension", fileName));
    }

    public FileExtensionException(final Collection<String> fileNames) {
        super(UNAVAILABLE_FILE_EXTENSION, format("Files %s have an unavailable extension", fileNames));
    }
}