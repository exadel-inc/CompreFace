package com.exadel.frs.exception;

import static com.exadel.frs.handler.ExceptionCode.INCORRECT_ORGANIZATION_ROLE;
import static java.lang.String.format;

public class IncorrectOrganizationRoleException extends BasicException {

    public static final String ORGANIZATION_ROLE_NOT_EXISTS_MESSAGE = "Organization role %s does not exists";

    public IncorrectOrganizationRoleException(final String organizationRole) {
        super(INCORRECT_ORGANIZATION_ROLE, format(ORGANIZATION_ROLE_NOT_EXISTS_MESSAGE, organizationRole));
    }

}
