package com.exadel.frs.exception;

import static com.exadel.frs.handler.ExceptionCode.ORGANIZATION_MISMATCH;

public class OrganizationMismatchException extends BasicException {

    private static final String MESSAGE = "Model cannot give access to apps from different organizations";

    public OrganizationMismatchException() {
        super(ORGANIZATION_MISMATCH, MESSAGE);
    }

}
