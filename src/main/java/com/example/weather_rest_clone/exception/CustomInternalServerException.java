package com.example.weather_rest_clone.exception;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CustomInternalServerException extends RestException {

    public CustomInternalServerException() {
        super("Internal server error");
    }
}
