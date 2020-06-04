package com.exadel.frs.exception;

import static com.exadel.frs.handler.ExceptionCode.APP_DOES_NOT_BELONG_TO_ORG;
import static java.lang.String.format;

public class AppDoesNotBelongToOrgException extends BasicException {

    private static final String MESSAGE = "App %s does not belong to organization %s";

    public AppDoesNotBelongToOrgException(String appGuid, String orgGuid) {
        super(APP_DOES_NOT_BELONG_TO_ORG, format(MESSAGE, appGuid, orgGuid));
    }
}