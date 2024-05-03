package com.safa.logisticintegration.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class IntegrationException extends RuntimeException {

    public IntegrationException(String message, Throwable e) {
        super(message, e);
    }

    public IntegrationException(String message) {
        super(message);
    }
}
