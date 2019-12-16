package com.exadel.frs.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class UserDoesNotBelongToOrganization extends RuntimeException {

    public UserDoesNotBelongToOrganization(Long userId, Long organizationId) {
        super("User " + userId + " does not belong to organization " + organizationId);
    }

}
