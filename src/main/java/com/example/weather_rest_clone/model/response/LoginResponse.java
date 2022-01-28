package com.example.weather_rest_clone.model.response;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class LoginResponse {

    private final String jwtToken;

    public LoginResponse(String jwtToken) {
        this.jwtToken = jwtToken;
    }
}
