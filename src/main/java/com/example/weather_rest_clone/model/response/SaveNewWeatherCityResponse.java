package com.example.weather_rest_clone.model.response;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class SaveNewWeatherCityResponse {

    private final int weatherCityId;

    public SaveNewWeatherCityResponse(int weatherCityId) {
        this.weatherCityId = weatherCityId;
    }
}
