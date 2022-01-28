package com.example.weather_rest_clone.service.validator;


import com.example.weather_rest_clone.exception.CustomBadRequestException;
import com.example.weather_rest_clone.model.request.LoginRequest;
import org.springframework.stereotype.Component;

@Component
public class AccessRequestValidator {

    public void validateLoginRequest(LoginRequest request) {
        validateUsername(request.getUsername());
        validatePassword(request.getPassword());
    }

    void validateUsername(String username) {
        if (username == null || username.isEmpty()) {
            throw new CustomBadRequestException("Invalid username");
        }
    }

    void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new CustomBadRequestException("Invalid password");
        }
    }
}
