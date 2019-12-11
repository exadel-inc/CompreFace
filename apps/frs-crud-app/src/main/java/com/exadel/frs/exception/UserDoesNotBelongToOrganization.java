package com.exadel.frs.exception;

import static com.exadel.frs.handler.ExceptionCode.USER_DOES_NOT_BELONG_TO_ORGANIZATION;
import static java.lang.String.format;

public class UserDoesNotBelongToOrganization extends BasicException {

    public static final String MESSAGE = "User %s does not belong to organization %s";

    public UserDoesNotBelongToOrganization(final Long userId, final Long organizationId) {
        super(USER_DOES_NOT_BELONG_TO_ORGANIZATION, format(MESSAGE, userId, organizationId));
    }

}
