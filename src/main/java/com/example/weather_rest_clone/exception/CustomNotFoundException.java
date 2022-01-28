package com.example.weather_rest_clone.exception;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CustomNotFoundException extends RestException {

    public CustomNotFoundException(String description) {
        super(description);
    }
}
