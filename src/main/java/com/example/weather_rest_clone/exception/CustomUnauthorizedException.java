package com.example.weather_rest_clone.exception;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CustomUnauthorizedException extends RestException {

    public CustomUnauthorizedException(String description) {
        super(description);
    }
}
