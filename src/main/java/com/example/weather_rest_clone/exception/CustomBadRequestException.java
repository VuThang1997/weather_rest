package com.example.weather_rest_clone.exception;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CustomBadRequestException extends RestException {

    public CustomBadRequestException(String description) {
        super(description);
    }
}
