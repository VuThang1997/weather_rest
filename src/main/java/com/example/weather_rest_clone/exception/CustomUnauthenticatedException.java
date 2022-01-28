package com.example.weather_rest_clone.exception;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CustomUnauthenticatedException extends RestException {

    public CustomUnauthenticatedException(String description) {
        super(description);
    }
}
