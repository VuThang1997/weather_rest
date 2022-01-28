package com.example.weather_rest_clone.model.response;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@ToString
public class UpdateWeatherCityResponse {
    private final String cityName;
    private final LocalDate retrieveDate;
    private final String message;

    public UpdateWeatherCityResponse(String cityName, LocalDate retrieveDate, String message) {
        this.cityName = cityName;
        this.retrieveDate = retrieveDate;
        this.message = message;
    }
}
