package com.exadel.frs.exception;

import static com.exadel.frs.handler.ExceptionCode.ORGANIZATION_NOT_FOUND;
import static java.lang.String.format;

public class OrganizationNotFoundException extends BasicException {

    private static final String MESSAGE = "Organization %s not found";

    public OrganizationNotFoundException(final Long organizationId) {
        super(ORGANIZATION_NOT_FOUND, format(MESSAGE, organizationId));
    }

}
