package com.claudiodornelles.webflux.mapper;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

public final class ResponseMapper {

    private ResponseMapper() {
    }

    public static <T> Mono<T> statusNotFound(String resourceName) {
        return Mono.error(
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        resourceName + " not found")
        );
    }

}
