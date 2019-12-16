package com.exadel.frs.exception;

import static com.exadel.frs.handler.ExceptionCode.USER_DOES_NOT_BELONG_TO_ORGANIZATION;
import static java.lang.String.format;

public class UserDoesNotBelongToOrganization extends BasicException {

    public static final String MESSAGE = "The organization %s doesn't exist or " +
            "organization exists, but user %s doesn't have view permission for that organization";

    public UserDoesNotBelongToOrganization(final Long userId, final Long organizationId) {
        super(USER_DOES_NOT_BELONG_TO_ORGANIZATION, format(MESSAGE, organizationId, userId));
    }

}
