package com.example.weather_rest_clone.service.domain_service;


import com.example.weather_rest_clone.model.request.LoginRequest;
import com.example.weather_rest_clone.model.response.LoginResponse;

public interface AccessService {

    LoginResponse login(LoginRequest request);
}
