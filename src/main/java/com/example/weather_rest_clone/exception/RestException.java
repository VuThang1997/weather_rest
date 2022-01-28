package com.example.weather_rest_clone.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
public class RestException extends RuntimeException {

    protected String description;

    public RestException(String description) {
        super();
        this.description = description;
    }
}
