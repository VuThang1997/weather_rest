package com.example.weather_rest_clone.controller;

import com.example.weather_rest_clone.model.request.*;
import com.example.weather_rest_clone.model.response.*;
import com.example.weather_rest_clone.service.domain_service.WeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class WeatherController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherController.class);

    private final WeatherService weatherService;

    @Autowired
    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/weather/today/{cityName}")
    public GetTodayWeatherCityResponse getTodayWeatherCity(@PathVariable(value = "cityName") String cityName) {
        var request = new GetTodayWeatherCityRequest(cityName);
        LOGGER.info("request = {}", request);

        GetTodayWeatherCityResponse response = weatherService.getTodayWeatherCity(request);
        LOGGER.info("response = {}", response);

        return response;
    }

    @PostMapping("/weather/period")
    public GetPeriodWeatherCityResponse getPeriodWeatherCity(@RequestBody GetPeriodWeatherCityRequest request) {
        LOGGER.info("request = {}", request);

        GetPeriodWeatherCityResponse response = weatherService.getPeriodWeatherCity(request);
        LOGGER.info("response = {}", response);

        return response;
    }

    @PostMapping("/weather")
    public SaveNewWeatherCityResponse saveNewWeatherCity(@RequestBody SaveNewWeatherCityRequest request) {
        LOGGER.info("request = {}", request);

        SaveNewWeatherCityResponse response = weatherService.saveNewWeatherCity(request);
        LOGGER.info("response = {}", response);

        return response;
    }

    @PutMapping("/weather")
    public UpdateWeatherCityResponse updateExistingWeatherCity(@RequestBody UpdateWeatherCityRequest request) {
        LOGGER.info("request = {}", request);

        UpdateWeatherCityResponse response = weatherService.updateExistingWeatherCity(request);
        LOGGER.info("response = {}", response);

        return response;
    }

    @DeleteMapping("/weather")
    public DeleteWeatherCityResponse deleteWeatherCity(@RequestBody DeleteWeatherCityRequest request) {
        LOGGER.info("request = {}", request);

        DeleteWeatherCityResponse response = weatherService.deleteWeatherCity(request);
        LOGGER.info("response = {}", response);

        return response;
    }
}