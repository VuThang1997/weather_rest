package com.example.weather_rest_clone.model.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class GetTodayWeatherCityRequest {

    @Setter
    private String cityName;

    public GetTodayWeatherCityRequest(String cityName) {
        this.cityName = cityName;
    }
}
