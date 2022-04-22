package com.claudiodornelles.webflux.exception;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Map;

@Component
public class CustomAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        var errorAttributes = super.getErrorAttributes(request, options);
        var throwable = getError(request);
        errorAttributes.put("message", throwable.getMessage());
        if (throwable instanceof ServiceValidationException) {
            errorAttributes.put("error", "Service Validation Exception");
            errorAttributes.put("status", HttpStatus.BAD_REQUEST.value());
        }
        if (throwable instanceof NotFoundException) {
            errorAttributes.put("error", "Resource Not Found");
            errorAttributes.put("status", HttpStatus.NOT_FOUND.value());
        }
        return errorAttributes;
    }
}
