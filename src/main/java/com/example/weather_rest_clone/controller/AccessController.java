package com.example.weather_rest_clone.controller;

import com.example.weather_rest_clone.model.request.LoginRequest;
import com.example.weather_rest_clone.model.response.LoginResponse;
import com.example.weather_rest_clone.service.domain_service.AccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccessController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessController.class);

    private final AccessService accessService;

    @Autowired
    public AccessController(AccessService accessService) {
        this.accessService = accessService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        LOGGER.info("request = {}", request);

        LoginResponse response = accessService.login(request);
        LOGGER.info("response = {}", response);

        return response;
    }
    
}
