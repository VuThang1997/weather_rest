package com.example.weather_rest_clone.model.response;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@ToString
public class DeleteWeatherCityResponse {
    private final String cityName;
    private final LocalDate retrieveDate;
    private final String message;

    public DeleteWeatherCityResponse(String cityName, LocalDate retrieveDate, String message) {
        this.cityName = cityName;
        this.retrieveDate = retrieveDate;
        this.message = message;
    }
}
