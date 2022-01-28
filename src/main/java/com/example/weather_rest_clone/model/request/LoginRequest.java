package com.example.weather_rest_clone.model.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class LoginRequest {

    private String username;

    @ToString.Exclude private String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
