package com.example.weather_rest_clone.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class ExceptionHandling {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandling.class);

    @ExceptionHandler(value = {Exception.class, RestException.class})
    protected ResponseEntity<Object> handleConflict(Exception e) {
        Map<String, Object> body = buildResponseBody(e);
        return buildResponse(e, body);
    }

    private Map<String, Object> buildResponseBody(Exception e) {
        Map<String, Object> body = new LinkedHashMap<>();

        if (e instanceof RestException) {
            body.put("description", ((RestException) e).getDescription());
        } else {
            LOGGER.error("Exception happened: ", e);
            body.put("description", "Internal server error");
        }

        return body;
    }

    private ResponseEntity<Object> buildResponse(Exception e, Map<String, Object> body) {
        if (e instanceof CustomBadRequestException) {
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }

        if (e instanceof CustomNotFoundException) {
            return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        }

        if (e instanceof CustomUnauthenticatedException) {
            return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
        }

        if (e instanceof CustomUnauthorizedException) {
            return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
