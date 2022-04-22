package com.claudiodornelles.webflux.exception;

public class ServiceValidationException extends RuntimeException {

    public ServiceValidationException(String message) {
        super(message);
    }
}
