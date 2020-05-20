package com.exadel.frs.core.trainservice.exception;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = NOT_FOUND, reason = "Model is not found")
public class ModelNotFoundException extends RuntimeException{

}
